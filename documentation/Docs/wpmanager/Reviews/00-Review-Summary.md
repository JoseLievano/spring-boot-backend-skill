# Review Summary — wpmanager

#doc #review #review-summary #ref-wpmanager

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]

## Overview

**88 findings: 9 🔴 Critical, 16 🟠 High, 43 🟡 Medium, 20 🟢 Low**, across eleven reviews.

wpmanager has the most substantial engineering of the three reference projects — an idempotent upload with
result replay, a compensating storage write, a provider abstraction that works across S3-compatible vendors,
a replication job — and the only method security that actually runs, proven by end-to-end tests with real
tokens. Its failures cluster in two places. **Authorization** has no outer layer: anything not behind an
annotated service method is anonymous (including a debug controller over the storage bucket), inherited CRUD
operations accept any logged-in client, storage secrets are returned by the API, and live cloud keys are
committed in a test. **Upload consistency** is designed but not achieved: the whole upload sits in one
transaction, a provider failure commits a version row that blocks the version and stalls replication, theme
upload never works, and the idempotency wrapper turns every failure into a 500. The concepts are worth
carrying into the skill; the boundaries around them need to be redrawn.

## Findings by Severity

| ID | Title | Severity | Review |
|---|---|---|---|
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-01\|WP-R01-01]] | No URL-level authorization; unannotated endpoints are anonymous | 🔴 Critical | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-02\|WP-R01-02]] | `/tests3/*` lets anonymous callers upload, overwrite, delete and download bucket objects | 🔴 Critical | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-03\|WP-R01-03]] | Inherited operations need only a login: clients can delete admins, providers and others' data | 🔴 Critical | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-04\|WP-R01-04]] | Storage-provider secret keys are returned by `/s3` and stored in plaintext | 🔴 Critical | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-05\|WP-R01-05]] | Spring Data REST likely exports every repository | 🔴 Critical | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-06\|WP-R01-06]] | Live cloud-storage keys committed in a test file | 🔴 Critical | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-01\|WP-R02-01]] | Base `update` ignores the form (admin, S3 provider, downloadable) | 🔴 Critical | [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-01\|WP-R03-01]] | A failed provider upload commits a file-less version that blocks that version forever | 🔴 Critical | [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review\|Upload Transactions and Consistency]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-01\|WP-R07-01]] | Theme upload always fails | 🔴 Critical | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-07\|WP-R01-07]] | Hardcoded DB credentials, JWT fallback secret and seed admin password | 🟠 High | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-08\|WP-R01-08]] | JWT design: claims-only trust, no revocation, issuer not checked | 🟠 High | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-09\|WP-R01-09]] | Account-status flags are ignored | 🟠 High | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-02\|WP-R03-02]] | Storage I/O runs inside the database transaction; the two phases share it | 🟠 High | [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review\|Upload Transactions and Consistency]] |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-03\|WP-R03-03]] | Compensation covers only the file-row insert; later failures leave objects behind | 🟠 High | [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review\|Upload Transactions and Consistency]] |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01\|WP-R04-01]] | Every failure inside the upload becomes HTTP 500 | 🟠 High | [[Docs/wpmanager/Reviews/04-Idempotency-Review\|Idempotency]] |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-02\|WP-R04-02]] | The key is the file checksum only; a reused ZIP replays another upload's result | 🟠 High | [[Docs/wpmanager/Reviews/04-Idempotency-Review\|Idempotency]] |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-01\|WP-R05-01]] | A new `S3Client` per `getClient()` call, mostly never closed | 🟠 High | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review\|Storage Integration]] |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02\|WP-R05-02]] | One version without a source copy aborts every replication run | 🟠 High | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review\|Storage Integration]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-01\|WP-R06-01]] | Roles stored as ordinals | 🟠 High | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-02\|WP-R06-02]] | No migrations; `ddl-auto=update` | 🟠 High | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-03\|WP-R06-03]] | EAGER collections, including every provider's full file map | 🟠 High | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-02\|WP-R07-02]] | Deleting a plugin or theme with two or more versions throws `ConcurrentModificationException` | 🟠 High | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-03\|WP-R07-03]] | Plugin and theme services are near-copies that have already diverged | 🟠 High | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-01\|WP-R10-01]] | Authorization is tested for one module only | 🟠 High | [[Docs/wpmanager/Reviews/10-Testing-Review\|Testing]] |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-02\|WP-R10-02]] | Upload, idempotency, storage and replication are untested | 🟠 High | [[Docs/wpmanager/Reviews/10-Testing-Review\|Testing]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-10\|WP-R01-10]] | Client passwords are derived; clients authenticate only through admin-minted tokens | 🟡 Medium | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-11\|WP-R01-11]] | Method security is switched on from three service classes | 🟡 Medium | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-12\|WP-R01-12]] | SSRF surface in website URL probing | 🟡 Medium | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-13\|WP-R01-13]] | CORS origin hardcoded; injected source ignored | 🟡 Medium | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-02\|WP-R02-02]] | Repository and service downcasts | 🟡 Medium | [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-03\|WP-R02-03]] | `getAll` is unbounded and returns a `Set`; no pagination anywhere | 🟡 Medium | [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-04\|WP-R02-04]] | Every success is 200; delete returns a body | 🟡 Medium | [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-05\|WP-R02-05]] | Two delete routes for downloadables; the inherited one skips storage cleanup | 🟡 Medium | [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-06\|WP-R02-06]] | Depth diagnostic: the base service is shallow and mostly overridden | 🟡 Medium | [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-04\|WP-R03-04]] | Duplicate-version check is check-then-act with no unique constraint | 🟡 Medium | [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review\|Upload Transactions and Consistency]] |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-05\|WP-R03-05]] | The replication job writes without a transaction | 🟡 Medium | [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review\|Upload Transactions and Consistency]] |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-03\|WP-R04-03]] | A crash leaves the key PENDING or PROCESSING and blocks retries for 24 hours | 🟡 Medium | [[Docs/wpmanager/Reviews/04-Idempotency-Review\|Idempotency]] |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-04\|WP-R04-04]] | Single-node assumptions: per-instance cache and an unhandled key race | 🟡 Medium | [[Docs/wpmanager/Reviews/04-Idempotency-Review\|Idempotency]] |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-03\|WP-R05-03]] | `headBucket` round trip before every operation | 🟡 Medium | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review\|Storage Integration]] |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-04\|WP-R05-04]] | Whole objects buffered in memory; no multipart or streaming copy | 🟡 Medium | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review\|Storage Integration]] |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-05\|WP-R05-05]] | The storage port lives on the JPA entity; one real adapter, FTP returns `null` | 🟡 Medium | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review\|Storage Integration]] |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-06\|WP-R05-06]] | Replication scheduling: 83-minute start, count-based detection, no health signal | 🟡 Medium | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review\|Storage Integration]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-04\|WP-R06-04]] | `hashCode()` returns 42 in three entities; four equality styles | 🟡 Medium | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-05\|WP-R06-05]] | Lombok `@Data` on provider entities | 🟡 Medium | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-06\|WP-R06-06]] | Favorite plugins and themes modeled as one-to-many | 🟡 Medium | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-07\|WP-R06-07]] | Version identity is `pluginName_version`; renames break it | 🟡 Medium | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-08\|WP-R06-08]] | Audit and API fields are never written | 🟡 Medium | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-04\|WP-R07-04]] | `PluginService` is a god class with unused collaborators | 🟡 Medium | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-05\|WP-R07-05]] | Services reach into other modules; eleven `@Lazy` constructors hide mapper cycles | 🟡 Medium | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-06\|WP-R07-06]] | Anemic model; invariants live in transaction scripts | 🟡 Medium | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-07\|WP-R07-07]] | Mapper bugs | 🟡 Medium | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-08\|WP-R07-08]] | Null-pointer paths in client, admin and plan code | 🟡 Medium | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-09\|WP-R07-09]] | Website URL handling: dead uniqueness check, `-` suffix, SSL flag | 🟡 Medium | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-10\|WP-R07-10]] | Unknown category ids are silently dropped; theme categories have no API | 🟡 Medium | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-11\|WP-R07-11]] | `ClientEntity.getBaseUser()` recurses forever | 🟡 Medium | [[Docs/wpmanager/Reviews/07-Service-Design-Review\|Service Design]] |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-01\|WP-R08-01]] | No fallback or framework-exception handlers | 🟡 Medium | [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-02\|WP-R08-02]] | Internal messages on 500; any `IllegalArgumentException` becomes 400 | 🟡 Medium | [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-03\|WP-R08-03]] | Forms carry almost no Bean Validation; constraints sit on entities | 🟡 Medium | [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-04\|WP-R08-04]] | SQL-keyword blacklist rejects legitimate names and protects nothing | 🟡 Medium | [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-05\|WP-R08-05]] | Upload type check trusts client metadata | 🟡 Medium | [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-06\|WP-R08-06]] | Custom body instead of RFC 9457 problem details | 🟡 Medium | [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-01\|WP-R09-01]] | Unused starters and test dependencies | 🟡 Medium | [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-02\|WP-R09-02]] | Test profile with fixed secrets ships in the jar | 🟡 Medium | [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-03\|WP-R09-03]] | No Actuator, health or metrics for jobs and storage | 🟡 Medium | [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-03\|WP-R10-03]] | No service or unit tests; Mockito unused | 🟡 Medium | [[Docs/wpmanager/Reviews/10-Testing-Review\|Testing]] |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-04\|WP-R10-04]] | `URLValidatorTest` depends on the public internet | 🟡 Medium | [[Docs/wpmanager/Reviews/10-Testing-Review\|Testing]] |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-05\|WP-R10-05]] | `contextLoads` needs MySQL and sits outside every suite | 🟡 Medium | [[Docs/wpmanager/Reviews/10-Testing-Review\|Testing]] |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-04\|WP-R11-04]] | The project vault contradicts the code | 🟡 Medium | [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review\|Code Hygiene and Docs Drift]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-14\|WP-R01-14]] | Duplicate `@EnableWebSecurity`; JWT filter also registered as a servlet filter | 🟢 Low | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-15\|WP-R01-15]] | `@PreAuthorize` on the non-bean `S3StorageClient` is inert | 🟢 Low | [[Docs/wpmanager/Reviews/01-Security-Review\|Security]] |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-07\|WP-R02-07]] | Inconsistent route naming | 🟢 Low | [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-08\|WP-R02-08]] | No API versioning, no OpenAPI description | 🟢 Low | [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-06\|WP-R03-06]] | `jakarta.transaction.Transactional` in `WebsiteService` drops the class-level rollback rules | 🟢 Low | [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review\|Upload Transactions and Consistency]] |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-05\|WP-R04-05]] | Depth diagnostic: a shallow state keeper hard-wired to `DownloadableDTO` | 🟢 Low | [[Docs/wpmanager/Reviews/04-Idempotency-Review\|Idempotency]] |
| [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-06\|WP-R04-06]] | The upload file is hashed twice per request | 🟢 Low | [[Docs/wpmanager/Reviews/04-Idempotency-Review\|Idempotency]] |
| [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-07\|WP-R05-07]] | Object keys rebuilt through URL parsing; no timeouts configured | 🟢 Low | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review\|Storage Integration]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-09\|WP-R06-09]] | `mainImage` join-column naming; image is entity-only | 🟢 Low | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-10\|WP-R06-10]] | Deprecated `MySQL8Dialect`; stale `@MappedSuperclass` Javadoc | 🟢 Low | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-07\|WP-R08-07]] | Three ways to write an error; per-error `ObjectMapper` | 🟢 Low | [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-04\|WP-R09-04]] | `spring.task.scheduling.enabled` is not a Spring Boot property | 🟢 Low | [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-05\|WP-R09-05]] | Upload size limit defined in two places | 🟢 Low | [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-06\|WP-R09-06]] | Surefire pinned below Boot; redundant Mockito pin; stale SDK comment | 🟢 Low | [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-07\|WP-R09-07]] | No README, Docker or CI | 🟢 Low | [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-06\|WP-R10-06]] | Recorded test evidence is stale and misses the upload test | 🟢 Low | [[Docs/wpmanager/Reviews/10-Testing-Review\|Testing]] |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-01\|WP-R11-01]] | `boostrap` typo and `AdminServiceImpl` naming outlier | 🟢 Low | [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review\|Code Hygiene and Docs Drift]] |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-02\|WP-R11-02]] | `System.out`, an unsupported `{:.2f}` placeholder, concatenated logging, validation at `ERROR` | 🟢 Low | [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review\|Code Hygiene and Docs Drift]] |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-03\|WP-R11-03]] | Dead code | 🟢 Low | [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review\|Code Hygiene and Docs Drift]] |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-05\|WP-R11-05]] | AI-tool leftovers in the project root | 🟢 Low | [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review\|Code Hygiene and Docs Drift]] |

## Shared with backend

Findings whose root cause is the same code, inherited by `backend/` (the lineage runs wpmanager → backend).
Each keeps its own ID so Task 4 can count how widespread an issue is.

| wpmanager | backend | Shared root cause |
|---|---|---|
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-01\|WP-R01-01]] | [[Docs/backend/Reviews/01-Security-Review#BE-R01-01\|BE-R01-01]] | Identical `SecurityFilterChain` without URL rules; `backend/` also lost method security |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-05\|WP-R01-05]] | [[Docs/backend/Reviews/01-Security-Review#BE-R01-03\|BE-R01-03]] | Data REST starter on the classpath |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-07\|WP-R01-07]] | [[Docs/backend/Reviews/01-Security-Review#BE-R01-04\|BE-R01-04]], [[Docs/backend/Reviews/01-Security-Review#BE-R01-11\|BE-R01-11]] | Same `AdminBoostrap` seed; literal secrets in properties |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-08\|WP-R01-08]] | [[Docs/backend/Reviews/01-Security-Review#BE-R01-07\|BE-R01-07]] | Identical `JwtTokenService` and claims-only filter |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-09\|WP-R01-09]] | [[Docs/backend/Reviews/01-Security-Review#BE-R01-08\|BE-R01-08]] | Identical `SecurityUser` with `getX` flag methods |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-10\|WP-R01-10]] | [[Docs/backend/Reviews/01-Security-Review#BE-R01-06\|BE-R01-06]] | Derived client passwords, admin-minted tokens (minting is anonymous in `backend/`, BE-R01-02) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-13\|WP-R01-13]] | [[Docs/backend/Reviews/01-Security-Review#BE-R01-10\|BE-R01-10]] | Identical CORS bean |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-14\|WP-R01-14]] | [[Docs/backend/Reviews/01-Security-Review#BE-R01-09\|BE-R01-09]] | `@Component` JWT filter registered twice |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-01\|WP-R02-01]] | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01\|BE-R02-01]] | No-op base `update` |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-02\|WP-R02-02]] | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-02\|BE-R02-02]] | Repository/service downcasts |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-03\|WP-R02-03]] | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-03\|BE-R02-03]] | Unbounded `getAll` into a `Set` |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-04\|WP-R02-04]] | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-04\|BE-R02-04]] | 200 for every success |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-06\|WP-R02-06]] | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-06\|BE-R02-06]], [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-07\|BE-R02-07]] | Shallow generic stack with checked exceptions |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-08\|WP-R02-08]] | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-05\|BE-R02-05]] | No versioning or OpenAPI |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-01\|WP-R06-01]] | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03\|BE-R03-03]] | Identical `BaseUserEntity` roles mapping |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-02\|WP-R06-02]] | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-04\|BE-R03-04]] | `ddl-auto=update`, no migrations |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-08\|WP-R06-08]] | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-02\|BE-R03-02]] | Identical unwritten audit columns |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-05\|WP-R07-05]] | [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-07\|BE-R09-07]] | `AuthUserUtil` in `shared/` depends on feature repositories |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-06\|WP-R07-06]] | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-01\|BE-R03-01]] | Anemic entities |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-07\|WP-R07-07]] | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03\|BE-R04-03]] | Hand-written mappers with field gaps |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-08\|WP-R07-08]] | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-02\|BE-R04-02]] | Same `ClientService` null handling |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-11\|WP-R07-11]] | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-01\|BE-R04-01]] | Identical recursive `getBaseUser()` |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-01\|WP-R08-01]] | [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-03\|BE-R05-03]] | Advice without fallback |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-02\|WP-R08-02]] | [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-04\|BE-R05-04]] | Messages on 500; global `IllegalArgumentException` → 400 |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-03\|WP-R08-03]] | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-07\|BE-R03-07]] | Constraints on entities, not forms |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-06\|WP-R08-06]] | [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06\|BE-R05-06]] | `ErrorHTTPRes` instead of `ProblemDetail` |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-07\|WP-R08-07]] | [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-02\|BE-R05-02]], [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-08\|BE-R09-08]] | Duplicated error writers, hand-made `ObjectMapper` |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-01\|WP-R09-01]] | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-01\|BE-R07-01]] | Same unused starters |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-02\|WP-R09-02]] | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-06\|BE-R07-06]] | Test profile in `src/main` |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-06\|WP-R09-06]] | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-04\|BE-R07-04]] | Surefire and Mockito pins |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-05\|WP-R10-05]] | [[Docs/backend/Reviews/08-Testing-Review#BE-R08-03\|BE-R08-03]] | `contextLoads` bound to an external database |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-01\|WP-R11-01]] | [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-02\|BE-R09-02]] | `boostrap` typo |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-02\|WP-R11-02]] | [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-06\|BE-R09-06]] | Concatenated logging, personal data at `ERROR` |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-03\|WP-R11-03]] | [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-01\|BE-R09-01]] | Dead `shared/` code carried into `backend/` |

34 of 88 wpmanager findings share a root cause with `backend/`. The findings unique to wpmanager are the upload,
idempotency, storage, replication and catalog findings — the code `backend/` removed.

## Strengths to Keep

- **Idempotency state machine** — persisted key with a unique index, `PENDING → PROCESSING → COMPLETED/FAILED`,
  result replay and TTL cleanup, applied through a wrapper that takes the operation as a `ThrowingSupplier`
  ([[Docs/wpmanager/Reviews/04-Idempotency-Review]]).
- **Compensation on upload failure** — delete the stored object when persistence fails, with a searchable alert
  when compensation itself fails ([[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]]).
- **Provider abstraction intent** — one client contract, a default provider, `endpointOverride` for
  S3-compatible vendors, and a version/copy data model that makes replication possible
  ([[Docs/wpmanager/Reviews/05-Storage-Integration-Review]]).
- **Method security that runs, with tests that prove it** — 401/403 asserted through MockMvc with real JWTs
  ([[Docs/wpmanager/Reviews/01-Security-Review]], [[Docs/wpmanager/Reviews/10-Testing-Review]]).
- **Tag-based test suites** with a launcher, and a `@DataJpaTest` class per entity module
  ([[Docs/wpmanager/Reviews/10-Testing-Review]]).
- **`FileSigner` fail-fast** on a missing secret, and HMAC signatures instead of home-made hashing
  ([[Docs/wpmanager/Reviews/01-Security-Review]]).
- **Ownership through `AuthUserUtil`** for client-owned writes, and entitlement checks where limited resources
  are created ([[Docs/wpmanager/Reviews/07-Service-Design-Review]]).
- **Careful relation maintenance** in the simpler services (diff-based collection updates, delete guards)
  ([[Docs/wpmanager/Reviews/07-Service-Design-Review]]).

## Top 5 Recommendations

1. **Close the authorization gaps.** Add deny-by-default URL rules, move `@EnableMethodSecurity` to
   `SecurityConfig`, default the base service to `hasRole('ADMIN')`, add ownership checks to inherited reads and
   deletes, delete `/tests3`, remove Data REST, and stop returning storage keys
   ([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-01|WP-R01-01]] to
   [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-05|WP-R01-05]]).
2. **Rotate the committed cloud keys** and remove secrets and fallbacks from the tree
   ([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-06|WP-R01-06]],
   [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-07|WP-R01-07]]).
3. **Redraw the upload boundary**: transfer outside any transaction, persist in one short transaction that rolls
   back on every exception, compensate around it; one product upload service for plugins and themes
   ([[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-01|WP-R03-01]],
   [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-02|WP-R03-02]],
   [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-01|WP-R07-01]],
   [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-03|WP-R07-03]]).
4. **Fix idempotency's edges**: request-scoped keys, original exceptions rethrown, leases instead of day-long
   locks, no per-instance cache ([[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01|WP-R04-01]] to
   [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-04|WP-R04-04]]).
5. **Make storage and replication robust and observable**: one cached client per provider, per-item error
   isolation in replication, set-based detection, and Actuator health and metrics
   ([[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-01|WP-R05-01]],
   [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02|WP-R05-02]],
   [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-03|WP-R09-03]]).

## Candidate Patterns for the Skill

Input for Task 4 (cross-project comparison). Each candidate names what to take from wpmanager and the findings
that shape the improved version.

| Pattern | Take from wpmanager | Improve per | Confidence it belongs in the skill |
|---|---|---|---|
| Idempotent operation wrapper with persisted state, result replay and TTL cleanup | `wpmanager/src/main/java/com/wpmanager/shared/idempotency` | [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01\|WP-R04-01]], [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-02\|WP-R04-02]], [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-03\|WP-R04-03]], [[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-05\|WP-R04-05]] | High for any upload or payment-like phase; unique to wpmanager |
| Upload-then-persist with compensation (saga step) | `wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java` | [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-01\|WP-R03-01]], [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-02\|WP-R03-02]], [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-03\|WP-R03-03]] | High (as corrected) |
| Object-storage port with an S3-compatible adapter (`endpointOverride`) | `wpmanager/src/main/java/com/wpmanager/models/storage` | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-01\|WP-R05-01]], [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-05\|WP-R05-05]], [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-04\|WP-R01-04]] | Medium — only for projects with file storage |
| Version/copy model for replicated files | `wpmanager/src/main/java/com/wpmanager/models/files` | [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-03\|WP-R06-03]], [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-07\|WP-R06-07]] | Medium |
| Background replication/reconciliation job | `wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java` | [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02\|WP-R05-02]], [[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-06\|WP-R05-06]] | Low–Medium — as an outbox-fed queue |
| E2E tests with real tokens and 401/403 assertions | `wpmanager/src/test/java/com/wpmanager/testUtils/TestAuthenticationHelper.java`, `wpmanager/src/test/java/com/wpmanager/models/author/E2EAuthorTest.java` | [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-01\|WP-R10-01]] (route × role matrix) | High |
| Tag-based suites with a launcher | `wpmanager/src/test/java/com/wpmanager/suites` | [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-05\|WP-R10-05]] | Medium |
| Fail-fast secret validation in `@PostConstruct` | `wpmanager/src/main/java/com/wpmanager/shared/tools/FileSigner.java` | [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-07\|WP-R01-07]] (apply to every secret, no fallbacks) | High |
| Caller-owned resources via a current-user helper | `wpmanager/src/main/java/com/wpmanager/shared/tools/AuthUserUtil.java` | [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-03\|WP-R01-03]], [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-05\|WP-R07-05]] | High (as corrected) |

**Not candidates:** method security enabled from service classes, the SQL-keyword blacklist, derived client
passwords, content-only idempotency keys, per-call SDK clients, the `/tests3` debug controller, and copying a
module per product type.

## Related Documents

- [[Docs/wpmanager/wpmanager-Index]]
- [[Docs/wpmanager/Reviews/01-Security-Review]]
- [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review]]
- [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]]
- [[Docs/wpmanager/Reviews/04-Idempotency-Review]]
- [[Docs/wpmanager/Reviews/05-Storage-Integration-Review]]
- [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review]]
- [[Docs/wpmanager/Reviews/07-Service-Design-Review]]
- [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review]]
- [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review]]
- [[Docs/wpmanager/Reviews/10-Testing-Review]]
- [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review]]
- [[Docs/backend/Reviews/00-Review-Summary]]
- [[Docs/BugTracker/Reviews/00-Review-Summary]]
