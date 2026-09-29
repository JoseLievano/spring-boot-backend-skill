# Storage Integration Review — wpmanager

#doc #review #ref-wpmanager #architecture #performance

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/09-Storage-Providers-and-Replication]]

## Scope

Reviewed: `models/storage/**` (provider entities, client base class, `S3StorageClient`, `S3ProviderService`,
`StorageProviderManager`), `schedule/FileDuplicator`, `shared/tools/DiskBasedMultipartFile`, and the vault's open
storage and replication notes. Security of `/tests3` and of provider secrets is in
[[Docs/wpmanager/Reviews/01-Security-Review]]; transaction boundaries in
[[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]].

## Verdict

The abstraction has the right shape on paper — a provider type, a client contract, a default provider, and a
background job that converges every version onto every provider — and `endpointOverride` makes it work across
S3-compatible vendors with no vendor code. In practice the seam sits on a JPA entity and has one real
implementation; the S3 client is rebuilt, with its own HTTP connection pool, on every call and usually not
closed; every operation pays an extra `headBucket` round trip; and the replication job buffers whole files in
memory and aborts its entire run on the first version it cannot find.

## Strengths to Keep

- One client contract (`uploadFile`, `fileExists`, `downloadFile`, `deleteFile`) for every provider
  (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/DefaultStorageProvider.java:9-52`).
- `endpointOverride` support, so Wasabi and other S3-compatible services need no code
  (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:55-58`).
- Clients are `AutoCloseable`, and two call sites use try-with-resources
  (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:206-219`,
  `wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:206-217`).
- Streaming upload with a known content length (`RequestBody.fromInputStream`)
  (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:70-78`).
- Replication writes through a temp file and a disk-backed `MultipartFile` adapter, reusing the same upload
  path (`wpmanager/src/main/java/com/wpmanager/shared/tools/DiskBasedMultipartFile.java:11-82`).
- A single "default provider" invariant maintained on insert
  (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderService.java:44-59`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-01\|WP-R05-01]] | A new `S3Client` per `getClient()` call, mostly never closed | 🟠 High | performance | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02\|WP-R05-02]] | One version without a source copy aborts every replication run | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-03\|WP-R05-03]] | `headBucket` round trip before every operation | 🟡 Medium | performance | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-04\|WP-R05-04]] | Whole objects buffered in memory; no multipart or streaming copy | 🟡 Medium | performance | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-05\|WP-R05-05]] | The storage port lives on the JPA entity; one real adapter, FTP returns `null` | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-06\|WP-R05-06]] | Replication scheduling: 83-minute start, count-based detection, no health signal | 🟡 Medium | design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-07\|WP-R05-07]] | Object keys rebuilt through URL parsing; no timeouts configured | 🟢 Low | correctness | Confirmed (read in code) |

## Findings

### WP-R05-01
**Title:** A new `S3Client` per `getClient()` call, mostly never closed
**Severity:** 🟠 High
**Category:** performance
**Principle:** Expensive, thread-safe clients are created once and shared; whatever you open, close.
**Evidence:** `S3ProviderEntity.getClient()` returns `new S3StorageClient(this)`, whose constructor builds an
`S3Client` (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java:66-69`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:39-61`). Call sites that never close
it: the upload (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:242`),
compensation (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:301`),
source selection (`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:100`), download
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:114`), and two per-provider checks
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:145-146`), plus `/tests3/exist`
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:153`). The vault records the
leak as open (`wpmanager/wpManagerDocs/Bugs/to-do/Resource Leak.md`).
**Impact:** Every replication run creates at least `3 × providers` clients per unreplicated version, and each
one owns an HTTP connection pool and threads. The AWS SDK guidance is to share one client or call `close()`;
unclosed clients hold sockets until garbage collection, and building a client costs far more than a request.
**Recommendation:** Build one `S3Client` per provider and cache it (keyed by provider id and a config version),
close it when the provider changes, and make the client a Spring-managed component rather than an entity
method:
```java
@Component
class StorageClients implements DisposableBean {
    private final Map<Long, S3StorageClient> clients = new ConcurrentHashMap<>();
    S3StorageClient forProvider(S3ProviderEntity p) {
        return clients.computeIfAbsent(p.getId(), id -> new S3StorageClient(p));
    }
    public void destroy() { clients.values().forEach(S3StorageClient::closeQuietly); }
}
```
**Verified against:** Context7 `/aws/aws-sdk-java-v2` (not version-pinned; `docs/BestPractices.md`: "Reuse SDK client if possible… All SDK clients are thread safe… invoke `client.close()` to release the resources")
**Confidence:** Confirmed (read in code)

### WP-R05-02
**Title:** One version without a source copy aborts every replication run
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Bulkhead failures per item in batch jobs.
**Evidence:** `selectSourceProvider` throws `InvalidInsertDetails` when no provider has the object
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:96-110`); `processDownloadable` converts it into a
`RuntimeException` (`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:91-93`), which escapes the
`forEach` over all versions (`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:65-72`). Unexpected
upload errors in `getURL` also become `RuntimeException` and escape the per-provider catch, which handles only
`InvalidInsertDetails | IOException` (`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:181-185`,
`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:213-217`), skipping the temp-file delete.
File-less version rows are produced by every failed plugin upload
([[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-01|WP-R03-01]]) and can be created by
any client through the inherited `POST /downloadable`
([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-03|WP-R01-03]]). The vault describes the opposite behavior:
"Source file missing → Skip downloadable, log error" (`wpmanager/wpManagerDocs/Docs/File Upload Process.md:689-693`).
**Impact:** Once such a row exists, every run processes a random subset of versions (the set is a `HashSet`) and
stops at that row, so some versions are never replicated and multi-provider redundancy silently degrades. Temp
files leak on unexpected upload errors.
**Recommendation:** Catch per version and per provider, record the outcome (last error, attempt count, next
retry) on the version, skip versions with no source copy, and delete temp files in `finally`.
**Verified against:** N/A — control-flow analysis
**Confidence:** Confirmed (read in code)

### WP-R05-03
**Title:** `headBucket` round trip before every operation
**Severity:** 🟡 Medium
**Category:** performance
**Principle:** Validate configuration once, not per call.
**Evidence:** `validateConfiguration()` calls `headBucket` and is invoked by `uploadFile`, `fileExists`,
`downloadFile` and (again) by `constructFileUrl` (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:69`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:88`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:107`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:158`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:180-194`). A failure becomes
`IllegalStateException`, and in `fileExists` any exception becomes `false`
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:98-101`).
**Impact:** Each upload makes two extra requests (one before `putObject`, one in URL construction); each
replication check doubles its request count. `headBucket` needs bucket-level permission, so a least-privilege
key limited to object operations breaks every call. A transient bucket error reads as "file does not exist",
which triggers re-uploads.
**Recommendation:** Validate once when the provider is created or updated (a "test connection" action), then
rely on the object calls' own errors; distinguish "not found" from "unknown" in `fileExists`.
**Verified against:** N/A — design analysis (SDK `headBucket` call as used in the code)
**Confidence:** Confirmed (read in code)

### WP-R05-04
**Title:** Whole objects buffered in memory; no multipart or streaming copy
**Severity:** 🟡 Medium
**Category:** performance
**Principle:** Stream large payloads end to end.
**Evidence:** `downloadFile` uses `getObjectAsBytes(...).asByteArray()`
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:117-118`); `FileDuplicator` then writes the
byte array to a temp file (`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:112-128`), contrary to its
own Javadoc ("keeping the entire content in memory can be problematic",
`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:27-33`). `/tests3/download` returns a `byte[]`
body (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:128-133`). Uploads are a single
`putObject` (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:78`); the vault records the
missing multipart upload (`wpmanager/wpManagerDocs/Bugs/to-do/No Multipart Upload for Large Files.md`).
**Impact:** Each copy of a 100 MB version allocates 100 MB of heap; concurrent downloads through `/tests3` can
exhaust memory. Single-part uploads of large files are slower and cannot resume.
**Recommendation:** Use `getObject(request, ResponseTransformer.toFile(path))` (or stream
`ResponseInputStream` into the upload), and multipart or the S3 Transfer Manager (the separate
`s3-transfer-manager` module; check availability for the pinned 2.20.x line before adopting) for large objects;
where providers are both S3-compatible and on the same service, use server-side `copyObject`.
**Verified against:** Context7 `/aws/aws-sdk-java-v2` (not version-pinned)
**Version note:** The Transfer Manager's sync-client support and CRT requirements changed across 2.20–2.2x; upgrading the SDK (BOM import) is advisable before adopting it.
**Confidence:** Confirmed (read in code)

### WP-R05-05
**Title:** The storage port lives on the JPA entity; one real adapter, FTP returns `null`
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** Ports and adapters: infrastructure behind a service-level port, not on the domain model. One adapter makes a hypothetical seam; a `null` adapter makes a broken one.
**Evidence:** `StorageProviderEntity.getClient()` is abstract on the entity
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/StorageProviderEntity.java:66`); S3 returns a new
client (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java:66-69`), FTP returns `null`
(`wpmanager/src/main/java/com/wpmanager/models/storage/ftp/FTPProviderEntity.java:73-76`). Every consumer calls
`provider.getClient().…` without a null check, e.g.
`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:100`. The base class's Javadoc refers to
`FTPProviderService` and `S3ProviderService` subclasses that do not exist
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/BaseStorageProviderClient.java:8-19`).
**Impact:** Dormant: no API creates an FTP provider (it has no controller), but one FTP row — inserted directly or
through Data REST — makes every upload to it and every replication run fail with a `NullPointerException`. The
entity cannot be unit-tested without the SDK, and the client cannot be cached or configured by Spring.
**Recommendation:** Move the port to a service: `StorageGateway` with `put/exists/get/delete`, and a
`StorageClientFactory` that maps a provider type to a Spring-managed adapter (`S3Adapter`, later `FtpAdapter`).
Keep entities as configuration only. Remove the FTP entity until an adapter exists.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)

### WP-R05-06
**Title:** Replication scheduling: 83-minute start, count-based detection, no health signal
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Background jobs need timely start, correct convergence criteria and observability.
**Evidence:** `@Scheduled(fixedDelay = 200400, initialDelay = 5000000)`
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:51`): first run about 83 minutes after start-up.
A static `AtomicBoolean` guards overlap within one JVM only (`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:41`).
Versions and providers are loaded in full each run (`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:59-60`),
processed sequentially, and "needs work" is `files.size() != providers.size()`
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:67`). Outcomes go to logs only. The vault records
these as open (`wpmanager/wpManagerDocs/Bugs/to-do/Extremely Long Initial Delay.md`,
`wpmanager/wpManagerDocs/Bugs/to-do/Single-Threaded Replication.md`,
`wpmanager/wpManagerDocs/Bugs/to-do/No Health Check for Scheduled Job.md`,
`wpmanager/wpManagerDocs/Bugs/to-do/LoadAll Anti-Pattern.md`).
**Impact:** A version uploaded just after a deploy has one copy for well over an hour; frequent deploys can keep
redundancy from ever converging. A count mismatch caused by an extra file row
([[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-05|WP-R03-05]]) makes a version
"need work" forever, re-downloading it every 3 minutes. Running two instances runs two jobs. Nobody is told when
replication stops (WP-R05-02).
**Recommendation:** Enqueue a replication task per version when the upload commits (an outbox row) and process
the queue with a short delay; detect missing copies by provider set; use a distributed lock (ShedLock) if more
than one instance runs; expose last-run time, backlog and failures through Actuator
([[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-03|WP-R09-03]]).
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)

### WP-R05-07
**Title:** Object keys rebuilt through URL parsing; no timeouts configured
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** One key function; explicit timeouts on remote calls.
**Evidence:** Upload uses the raw version name as key (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:73`),
but download and delete build `new URL(baseUrl + "/" + name)` and use its path
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:108-111`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:129-131`). The client builder sets no API
call timeout or HTTP client settings (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:51-60`).
Keys have no prefix and no extension.
**Impact:** A plugin name containing `?`, `#` or a path in the endpoint URL yields a different key on delete
than on upload, so deletes and compensation miss the object. Without timeouts, a hung connection holds the
request thread (and, today, a DB connection) until the SDK's defaults give up.
**Recommendation:** One `keyFor(version)` function (for example `plugins/<pluginId>/<version>/<sha256>.zip`)
used by every operation; configure `apiCallTimeout`/`apiCallAttemptTimeout` and the retry strategy explicitly.
**Verified against:** Context7 `/aws/aws-sdk-java-v2` (not version-pinned; retry strategy and override configuration)
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

```java
public interface ObjectStorage {                       // port, used by services only
    StoredObject put(ObjectKey key, InputStream data, long size, String contentType);
    boolean exists(ObjectKey key);
    void copyTo(ObjectKey key, Path target);           // streaming
    void delete(ObjectKey key);
}
@Component class S3ObjectStorageFactory { ObjectStorage forProvider(ProviderConfig c) { /* cached per provider */ } }
```

Provider rows hold configuration (credentials encrypted, see
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-04|WP-R01-04]]); a replication queue fed at upload commit
converges copies per version with per-item error isolation, metrics and a distributed lock.

## Related Documents

- [[Docs/wpmanager/Explanations/09-Storage-Providers-and-Replication]]
- [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]]
- [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review]]
