# Idempotency Review — wpmanager

#doc #review #ref-wpmanager #architecture #error-handling

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/08-Upload-Idempotency]]

## Scope

Reviewed: `shared/idempotency/*`, `shared/functional/ThrowingSupplier`, `exceptions/DuplicateUploadException`,
`schedule/IdempotencyKeyCleanup`, the two upload controllers that use the wrapper, and the vault's
"Upload Idempotency System" doc. The upload's own transaction problems are in
[[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]].

## Verdict

The concept is the most reusable idea in the project: a persisted state machine with a unique key, result
replay, a TTL and a cleanup job, applied around a side-effecting operation through a small wrapper. The
implementation gets the states and guards right but the edges wrong. Every failure — including a simple
"version already exists" — comes back as HTTP 500 because the error path inspects the wrong exception. The key
is the file's content alone, so the same ZIP sent for another plugin or version silently returns the earlier
result. A crash in flight locks the key for a day, and the in-memory map assumes a single instance.

## Strengths to Keep

- Explicit status enum and guarded transitions, each in its own transaction, so state survives crashes and
  restarts (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:116-433`).
- Unique index on the key plus an expiry index for cheap cleanup
  (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyKeyEntity.java:12-16`).
- Result replay from a serialized DTO, so duplicates cost one query
  (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:454-502`).
- `FAILED` keys can be retried with the same file
  (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:140-146`).
- A wrapper that takes the operation as a `ThrowingSupplier`, so feature code stays unaware of the protocol
  (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotentUploadService.java:70-75`,
  `wpmanager/src/main/java/com/wpmanager/shared/functional/ThrowingSupplier.java:12-21`).
- Concurrent duplicates get a 409 with a clear message
  (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:130-138`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01\|WP-R04-01]] | Every failure inside the upload becomes HTTP 500 | 🟠 High | error-handling | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-02\|WP-R04-02]] | The key is the file checksum only; a reused ZIP replays another upload's result | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-03\|WP-R04-03]] | A crash leaves the key PENDING or PROCESSING and blocks retries for 24 hours | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-04\|WP-R04-04]] | Single-node assumptions: per-instance cache and an unhandled key race | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-05\|WP-R04-05]] | Depth diagnostic: a shallow state keeper hard-wired to `DownloadableDTO` | 🟢 Low | design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-06\|WP-R04-06]] | The upload file is hashed twice per request | 🟢 Low | performance | Confirmed (read in code) |

## Findings

### WP-R04-01
**Title:** Every failure inside the upload becomes HTTP 500
**Severity:** 🟠 High
**Category:** error-handling
**Principle:** Preserve the failure's type across a wrapper; a translation layer must not erase the contract of what it wraps.
**Evidence:** The supplier throws `InvalidInsertDetails` or `FailUpload` directly
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:160-190`), but the wrapper tests the
*cause*:
```java
// wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotentUploadService.java:108-114
} catch (Exception e) {
    String msg = "Upload failed for key: " + idempotencyKey + ", error: " + e.getMessage();
    logger.error(msg);
    idempotencyManager.markFailed(idempotencyKey, e.getMessage());
    if (e.getCause() instanceof InvalidInsertDetails)
        throw new InvalidInsertDetails(msg);
    throw new IllegalStateException("Something goes wrong processing key: " + idempotencyKey); // Re-throw to preserve exception type
```
A directly thrown exception has no cause, so every failure leaves as `IllegalStateException`, mapped to 500
(`wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:107-120`).
**Impact:** A non-ZIP file, an unknown plugin id, a missing default provider and a duplicate version all return
`500 "Something goes wrong processing key: <sha256>"`; the real message is only in the log. Clients cannot
tell a bad request from an outage, and retry logic retries validation errors. If `e.getMessage()` is null,
`markFailed` itself throws and the key stays `PROCESSING` (WP-R04-03).
**Recommendation:** Rethrow the original exception after recording the failure:
```java
} catch (Exception e) {
    idempotencyManager.markFailed(key, Objects.requireNonNullElse(e.getMessage(), e.getClass().getName()));
    if (e instanceof RuntimeException re) throw re;
    throw new UploadFailedException(e);   // runtime, carries the original as cause and its status mapping
}
```
Better still, make domain exceptions runtime types so the supplier and wrapper need no checked plumbing.
Consider not marking validation failures as `FAILED` at all (validate before registering the key).
**Verified against:** N/A — Java exception semantics
**Confidence:** Confirmed (read in code)

### WP-R04-02
**Title:** The key is the file checksum only; a reused ZIP replays another upload's result
**Severity:** 🟠 High
**Category:** correctness
**Principle:** An idempotency key must identify the *request*, not just one of its inputs.
**Evidence:** The key is `checksumUtils.calculateChecksum(file)`
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginController.java:53-54`). The fast path returns the
stored result for any `COMPLETED` key before looking at the target plugin or version
(`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotentUploadService.java:79-84`,
`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:454-502`); the stored
`entityType`/`entityId` are never compared. Plugin and theme uploads share one key space.
**Impact:** For 24 hours after a successful upload, sending the same bytes as a different version, for a
different plugin, or as a theme returns `200` with the *earlier* version's DTO and creates nothing. An admin
who re-uploads a ZIP to fix a wrong version number sees success and gets no new version. After a `FAILED`
attempt for plugin A, the same file for plugin B re-uses A's key row.
**Recommendation:** Derive the key from the whole request, or accept a client-supplied `Idempotency-Key`
header and store a request fingerprint alongside it:
```java
String key = sha256(entityType + ":" + parentId + ":" + version + ":" + fileSha256);
// or: header key + stored fingerprint; a mismatch returns 422 (per the IETF Idempotency-Key draft)
```
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)

### WP-R04-03
**Title:** A crash leaves the key PENDING or PROCESSING and blocks retries for 24 hours
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Every lock needs a lease.
**Evidence:** `registerKey` and `markProcessing` commit separately
(`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:34-95`,
`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:178-221`). If the process dies, or
`markFailed` throws (it rejects a null or empty message,
`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:383-385`), the row stays `PENDING` or
`PROCESSING`; later requests get `DuplicateUploadException` (409)
(`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:130-156`). The only release is
expiry, 24 hours after registration, plus up to an hour for the cleanup job
(`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:83`,
`wpmanager/src/main/java/com/wpmanager/schedule/IdempotencyKeyCleanup.java:50`).
**Impact:** After a deploy or crash during an upload, that file cannot be uploaded for up to a day. `registerKey`
also does not check `expiresAt`, so an expired key still blocks until the job deletes it.
**Recommendation:** Give in-flight states a short lease (`leaseUntil = now + upload timeout`); treat an
expired lease as `FAILED` in `registerKey`; register and mark processing in one transaction; always record
failure with a non-null message.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)

### WP-R04-04
**Title:** Single-node assumptions: per-instance cache and an unhandled key race
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** A cache in front of a coordination table must never be more authoritative than the table.
**Evidence:** `activeUploads` is a field of a singleton (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:24`)
and `registerKey` consults it before the database
(`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:51-56`). Rows found as `PENDING` or
`PROCESSING` are re-cached (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:64-68`) and
removed only when the same instance completes or fails the key, or when the key expires. Two first requests
that both miss the map and the table both insert; the unique index rejects one with a
`DataIntegrityViolationException` that no handler maps
(`wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:15-151`). The vault claims the table
"prevents race conditions across multiple instances"
(`wpmanager/wpManagerDocs/Docs/Upload Idempotency System.md:483-494`).
**Impact:** On a second instance, a key that another instance has since failed stays cached as `PROCESSING`, so
retries routed there get 409 until expiry. The losing request of a first-time race gets an unstructured 500.
**Recommendation:** Drop the map (the unique-index lookup is already cheap) or use it only as a negative cache;
insert first and treat a unique violation as "already registered" (`INSERT … ON DUPLICATE KEY` or catch
`DataIntegrityViolationException` and re-read).
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)

### WP-R04-05
**Title:** Depth diagnostic: a shallow state keeper hard-wired to `DownloadableDTO`
**Severity:** 🟢 Low
**Category:** design
**Principle:** Deep modules: `IdempotencyManager` exposes five public methods that callers must invoke in the right order; the module's size is mostly comments and logging.
**Evidence:** 519 lines, of which the transition logic is a few dozen; each public method repeats a lookup and
guard block (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:178-433`); the private
`updateStatus` helper that was meant to centralize this is unused
(`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:242-271`). `storeResult` and
`getResult` take and return `DownloadableDTO`, and the wrapper's supplier is typed to it
(`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:295`,
`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotentUploadService.java:70-75`), although the Javadoc
calls it generic.
**Impact:** Reusing the mechanism for any other operation means copying or editing it; the order of calls is a
protocol every future caller must learn (today only the wrapper does).
**Recommendation:** Collapse to one deep method, `<T> T execute(String key, Fingerprint fp, Class<T> type,
ThrowingSupplier<T> op)`, that owns the whole protocol; store the result as JSON with its type; keep the
transitions private.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)

### WP-R04-06
**Title:** The upload file is hashed twice per request
**Severity:** 🟢 Low
**Category:** performance
**Principle:** Compute once, pass along.
**Evidence:** The controller hashes the file for the key
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginController.java:54`) and the service hashes it
again (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:178`). `ChecksumUtils` streams
in 8 KB chunks, so memory use is small (`wpmanager/src/main/java/com/wpmanager/shared/tools/ChecksumUtils.java:12-25`).
The vault's "Memory-Intensive Checksum Calculation" note overstates this
(`wpmanager/wpManagerDocs/Bugs/to-do/Memory-Intensive Checksum Calculation.md`).
**Impact:** Two full reads of up to 100 MB per upload; CPU and disk I/O only.
**Recommendation:** Compute the checksum once and put it on the upload form.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

```java
@Component
class Idempotency {
    <T> T execute(IdempotencyRequest req, Class<T> resultType, ThrowingSupplier<T> op) {
        // key = client Idempotency-Key header, or hash(type, parentId, version, fileSha256)
        // 1. one tx: insert-or-read row (unique key); compare fingerprint → 422 on mismatch
        // 2. COMPLETED → return stored result; IN_FLIGHT with live lease → 409; expired lease/FAILED → take over
        // 3. run op outside any tx; on success store result (typed JSON), on failure record message
        // 4. rethrow the original exception unchanged
    }
}
```

No in-memory map; leases instead of day-long locks; cleanup by expiry index as today.

## Related Documents

- [[Docs/wpmanager/Explanations/08-Upload-Idempotency]]
- [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]]
- [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review]]
