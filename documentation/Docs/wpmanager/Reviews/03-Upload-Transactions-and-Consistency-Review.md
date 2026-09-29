# Upload Transactions and Consistency Review — wpmanager

#doc #review #ref-wpmanager #persistence #architecture

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/07-Upload-Pipeline]]

## Scope

Reviewed: `PluginService.upload`, `ThemeService.upload`, `StorageProviderManager`, `FileService`, the
replication job's writes, every transaction declaration in the project, and the vault notes that motivated the
two-phase design. Idempotency bookkeeping is reviewed separately in
[[Docs/wpmanager/Reviews/04-Idempotency-Review]]; S3 client behavior in
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review]].

## Verdict

The authors identified the right problem — a database transaction cannot roll back an object in a bucket —
and wrote the right primitive, a compensating delete. The boundaries around it are wrong. The whole upload
runs inside one transaction opened by the service, so the "non-transactional" phase 1 holds a connection for
the duration of the network transfer, and "phase 2" is not a separate transaction. The rollback rule leaves
out the checked `FailUpload`, so the most common failure — the provider being unreachable — commits a
file-less version row that blocks that version forever and, through the replication job, stops replication
for every version.

## Strengths to Keep

- A compensating delete with an explicit, searchable log message when it fails ("ORPHANED FILE ALERT")
  (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:139-158`,
  `wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:291-321`).
- Persistence of the file row, provider map and version set grouped in one service method
  (`wpmanager/src/main/java/com/wpmanager/models/files/file/FileService.java:78-134`).
- A duplicate-version check before any storage work
  (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:187-190`).
- The design rationale is written down (`wpmanager/wpManagerDocs/Docs/File Upload Process.md:211-280`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-01\|WP-R03-01]] | A failed provider upload commits a file-less version that blocks that version forever | 🔴 Critical | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-02\|WP-R03-02]] | Storage I/O runs inside the database transaction; the two phases share it | 🟠 High | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-03\|WP-R03-03]] | Compensation covers only the file-row insert; later failures leave objects behind | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-04\|WP-R03-04]] | Duplicate-version check is check-then-act with no unique constraint | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-05\|WP-R03-05]] | The replication job writes without a transaction | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-06\|WP-R03-06]] | `jakarta.transaction.Transactional` in `WebsiteService` drops the class-level rollback rules | 🟢 Low | persistence | Confirmed (read in code) |

## Findings

### WP-R03-01
**Title:** A failed provider upload commits a file-less version that blocks that version forever
**Severity:** 🔴 Critical
**Category:** correctness
**Principle:** Failure atomicity: an operation that fails must leave no partial state; checked exceptions need explicit rollback rules.
**Evidence:** The version row is inserted before the upload
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:192-198`). If there is no default
provider, or the provider call fails for any reason (network, credentials, `headBucket`), the manager throws
`FailUpload` (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:94-97`,
`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:113-121`,
`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:257-274`).
`FailUpload` is a checked exception (`wpmanager/src/main/java/com/wpmanager/exceptions/FailUpload.java:3`) and the
method rolls back only on `InvalidInsertDetails`:
```java
// wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:158-160
@PreAuthorize("hasRole('ADMIN')")
@Transactional(rollbackFor = InvalidInsertDetails.class)
public DownloadableDTO upload(PluginUploadForm form) throws InvalidInsertDetails, FailUpload {
```
With Spring's default rules the transaction commits. On retry the duplicate-version check finds the committed
row and throws `InvalidInsertDetails("Version … already exists …")`
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:187-190`), which the idempotency
wrapper turns into a 500 ([[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01|WP-R04-01]]).
**Impact:** One transient storage outage makes that plugin version impossible to upload again without deleting
the row by hand (the row is not linked to its plugin, so no API shows it). The row also has no files, so the
replication job fails on it every run and stops replicating everything else
([[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02|WP-R05-02]]).
**Recommendation:** Upload first, then write all rows in one short transaction, and roll back on every
exception:
```java
public DownloadableDTO upload(PluginUploadForm form) {           // no transaction here
    UploadPlan plan = validateAndPlan(form);                     // read-only checks
    URL url = storage.putToDefault(plan.key(), form.getFile());  // network, outside any tx
    try {
        return tx.execute(s -> persistVersion(plan, url));       // TransactionTemplate, rollback on any exception
    } catch (RuntimeException e) {
        storage.deleteFromDefault(plan.key());                   // compensation
        throw e;
    }
}
```
If a declarative transaction stays, use `@Transactional(rollbackFor = Exception.class)` or make `FailUpload` a
runtime exception.
**Verified against:** Context7 `/websites/spring_io_spring-framework_reference_6_2` (`data-access/transaction/declarative/rolling-back.html` under `/reference/6.2/`: checked exceptions do not cause rollback by default)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-02|BT-R07-02]] (checked exceptions commit partial work)

### WP-R03-02
**Title:** Storage I/O runs inside the database transaction; the two phases share it
**Severity:** 🟠 High
**Category:** persistence
**Principle:** Keep transactions short and free of remote calls; a design's documented boundary must match the code.
**Evidence:** The manager documents "Phase 1 (Non-Transactional)" and "Phase 2 (Transactional)"
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:58-74`), but its
caller is already transactional (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:158-160`),
and `FileService.persistFileWithProvider` uses default `REQUIRED` propagation, so it joins that transaction
(`wpmanager/src/main/java/com/wpmanager/models/files/file/FileService.java:78-79`). The S3 `putObject` of up to 100 MB
runs while the connection is held (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:70-78`).
The pool has 10 connections (`wpmanager/src/main/resources/application.properties:18`). The vault's "done" bug
note proposed the split correctly (`wpmanager/wpManagerDocs/Bugs/done/Transaction Boundary Violation in StorageProviderManager.md`).
**Impact:** Ten concurrent uploads exhaust the pool and stall every other request. A phase-2 failure does not
roll back independently: a runtime exception marks the shared transaction rollback-only, and the caller then
fails at commit with `UnexpectedRollbackException`.
**Recommendation:** Remove `@Transactional` from the service's upload method and use a `TransactionTemplate`
(or a separate bean with `REQUIRES_NEW`) only around the database writes, as in WP-R03-01.
**Verified against:** Context7 `/websites/spring_io_spring-framework_reference_6_2` (declarative transactions and propagation, `/reference/6.2/`)
**Confidence:** Confirmed (read in code)

### WP-R03-03
**Title:** Compensation covers only the file-row insert; later failures leave objects behind
**Severity:** 🟠 High
**Category:** correctness
**Principle:** A saga's compensation must cover every step after the irreversible one.
**Evidence:** Compensation runs only when `persistFileWithProvider` throws
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:128-158`). After it
returns, the service still links the version to the plugin and saves twice
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:202-206`), and the transaction still has
to commit. A failure in any of those — or a rollback at commit because phase 2 marked the transaction
rollback-only — rolls back the rows and leaves the object in the bucket with nothing pointing at it. Theme
upload fails before storage today, so it is unaffected until [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-01|WP-R07-01]] is fixed.
**Impact:** Orphaned objects accumulate silently (no "ORPHANED FILE ALERT" is logged for these paths). The vault
note that motivated compensation assumed this case was covered
(`wpmanager/wpManagerDocs/Bugs/done/Partial Upload Orphans Files.md`).
**Recommendation:** Put the compensation around the database transaction boundary, as in the WP-R03-01 sketch,
so any failure after the upload deletes the object. Add a periodic reconciliation job (list bucket keys vs
file rows) as a backstop.
**Verified against:** N/A — control-flow analysis
**Confidence:** Confirmed (read in code)

### WP-R03-04
**Title:** Duplicate-version check is check-then-act with no unique constraint
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** Enforce invariants in the database; application checks only give better messages.
**Evidence:** `findByNameAndVersion` then `save`, with no unique index on `(name, version)` or on the version's
parent (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:187-198`,
`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableEntity.java:10-45`). Idempotency blocks
only concurrent uploads of the *same bytes*. The vault records the race as open
(`wpmanager/wpManagerDocs/Bugs/to-do/Race Condition on Concurrent Uploads.md`).
**Impact:** Two different ZIPs for the same version uploaded at the same time both pass the check; both rows
use the same object key, so the second `putObject` overwrites the first file and one row's checksum and
signature describe bytes that no longer exist. If the second then compensates, it deletes the object both
rows point to.
**Recommendation:** Add `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"plugin_id", "version"}))`
(after giving the version a real parent foreign key at insert time, see
[[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-07|WP-R06-07]]), map the violation to 409,
and use content-addressed or id-based object keys so two rows can never share one object.
**Verified against:** N/A — JPA mapping standard
**Confidence:** Confirmed (read in code)

### WP-R03-05
**Title:** The replication job writes without a transaction
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** Group writes that must be consistent.
**Evidence:** `FileDuplicator.duplicateFiles` has no `@Transactional`; per provider it saves a `FileEntity`, then
the provider's map, and at the end the version's file set, each as its own commit
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:139-189`). The vault describes this as
"Non-transactional by design" (`wpmanager/wpManagerDocs/Docs/File Upload Process.md:677-681`).
**Impact:** A failure between writes leaves a file row that is not in the provider map, or a map entry for a
provider whose upload failed. When the map already holds the signature, a new file row is saved anyway
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:147-161`), so a version can end up with more
file rows than providers, which the job's count check then treats as "needs replication" every run.
**Recommendation:** Per version and provider: upload, then write the file row and map entry in one short
transaction (a `TransactionTemplate` in the loop); detect missing copies by comparing the set of providers
holding the version with the set of all providers, not by counting rows.
**Verified against:** N/A — control-flow analysis
**Confidence:** Confirmed (read in code)

### WP-R03-06
**Title:** `jakarta.transaction.Transactional` in `WebsiteService` drops the class-level rollback rules
**Severity:** 🟢 Low
**Category:** persistence
**Principle:** One transaction annotation style per codebase.
**Evidence:** `WebsiteService` imports `jakarta.transaction.Transactional` and puts it on `update`
(`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:15`,
`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:113-115`). Spring honors it, and its
defaults (roll back on runtime exceptions only) equal Spring's. But a method-level annotation replaces the
inherited Spring class-level rule that rolled back on the four checked domain exceptions
(`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java:16-21`).
**Impact:** None today (the method writes only after all checks pass); a later write placed before a
`throw new ItemNotFoundException` would commit.
**Recommendation:** Use `org.springframework.transaction.annotation.Transactional` everywhere, or rely on the
class-level rule.
**Verified against:** Context7 `/websites/spring_io_spring-framework_reference_6_2` (`declarative/annotations.html`: "jakarta.transaction.Transactional … supported as a drop-in replacement")
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

```text
upload(request):
  1. validate + plan (read-only, no transaction): parent exists, version free, key = <parentId>/<version>/<sha256>.zip
  2. put object to default provider (no transaction, bounded retries)
  3. TransactionTemplate: insert version (unique parent+version) + file row + provider link, rollback on any exception
  4. on failure after step 2: delete object (compensation); log + metric if compensation fails
  5. reconciliation job: bucket keys vs file rows, both directions
```

Replication follows the same rule per copy: transfer outside a transaction, one short transaction to record
it, set-based detection of missing copies.

## Related Documents

- [[Docs/wpmanager/Explanations/07-Upload-Pipeline]]
- [[Docs/wpmanager/Reviews/04-Idempotency-Review]]
- [[Docs/wpmanager/Reviews/05-Storage-Integration-Review]]
- [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review]]
