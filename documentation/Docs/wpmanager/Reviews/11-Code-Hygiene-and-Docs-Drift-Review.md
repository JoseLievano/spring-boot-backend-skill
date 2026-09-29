# Code Hygiene and Docs Drift Review — wpmanager

#doc #review #ref-wpmanager #architecture

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/01-Overview-and-Design-Philosophy]], [[Docs/wpmanager/Explanations/03-Package-Structure-and-Module-Anatomy]]

## Scope

Reviewed: naming, logging, dead code and tooling leftovers across `wpmanager/src/main/java` and the project root;
and the project's own Obsidian vault, `wpmanager/wpManagerDocs` (4 system docs, 151 class notes, 9 done and 30
open bug notes, 12 done tasks, 1 current task, 1 in-progress feature, 1 report), checked claim by claim against
the code where it describes behavior. The vault was read only, never opened in Obsidian. Each open vault bug
was verified; confirmed ones are cited as prior art in the finding that owns them, and the rest are listed
under "Vault Leads Not Confirmed".

## Verdict

Everyday hygiene is modest: a misspelled package, two `System.out` calls, an unsupported log placeholder,
string-concatenated logging, and a few hundred lines of dead code. The bigger issue is the vault. It is
detailed, well linked and clearly used to drive work, which makes its drift consequential: several "done" items
are only partly done, and the system docs describe safeguards (non-transactional storage phase, fail-fast JWT
secret, replication that skips bad items, 400 on validation errors) that the code does not have. A reader who
trusts the vault will misjudge the system's risk.

## Strengths to Keep

- A maintained project vault with system docs, per-class notes, a prioritised bug backlog and task records
  (`wpmanager/wpManagerDocs`); several fixes are traceable from bug note to code, e.g. HMAC signing
  (`wpmanager/wpManagerDocs/Bugs/done/Weak Signature Generation.md`,
  `wpmanager/src/main/java/com/wpmanager/shared/tools/FileSigner.java:37-55`) and the full-length checksum
  (`wpmanager/wpManagerDocs/Bugs/done/Checksum Collision Risk.md`,
  `wpmanager/src/main/java/com/wpmanager/shared/tools/ChecksumUtils.java:12-33`).
- Parameterised SLF4J logging in the newest code (idempotency, storage manager)
  (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:74`).
- Consistent file naming inside modules (`<Feature><Role>.java`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-01\|WP-R11-01]] | `boostrap` typo and `AdminServiceImpl` naming outlier | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-02\|WP-R11-02]] | `System.out`, an unsupported `{:.2f}` placeholder, concatenated logging, validation at `ERROR` | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-03\|WP-R11-03]] | Dead code | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-04\|WP-R11-04]] | The project vault contradicts the code | 🟡 Medium | hygiene | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-05\|WP-R11-05]] | AI-tool leftovers in the project root | 🟢 Low | hygiene | Confirmed (read in code) |

## Findings

### WP-R11-01
**Title:** `boostrap` typo and `AdminServiceImpl` naming outlier
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Names are the first documentation.
**Evidence:** Package `configuration/boostrap` and class `AdminBoostrap`
(`wpmanager/src/main/java/com/wpmanager/configuration/boostrap/AdminBoostrap.java:1-17`). Every service is
`<Feature>Service` except `AdminServiceImpl` (`wpmanager/src/main/java/com/wpmanager/models/hq/admin/AdminServiceImpl.java:15`);
`SecurityUserServiceImpl` also uses the suffix without an interface of its own
(`wpmanager/src/main/java/com/wpmanager/shared/securityUser/SecurityUserServiceImpl.java:10`).
**Impact:** Search misses; the typo propagated to `backend/`.
**Recommendation:** Rename to `bootstrap`/`AdminBootstrap`, `AdminService`, `SecurityUserService` (or
`AppUserDetailsService`).
**Verified against:** N/A — naming
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-02|BE-R09-02]]

### WP-R11-02
**Title:** `System.out`, an unsupported `{:.2f}` placeholder, concatenated logging, validation at `ERROR`
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** One logging facade, parameterised messages, levels that match severity.
**Evidence:** `System.out.println` in `S3StorageClient.deleteFile` and `S3FileUploadController.uploadFile`
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:130`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:64`). A Python-style `{:.2f}`
placeholder, which SLF4J prints literally while dropping the throughput argument
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:250-254`).
String concatenation in log calls across services, e.g.
`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:182`,
`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:66`. Client input errors are logged at
`ERROR` before every validation throw, e.g. `wpmanager/src/main/java/com/wpmanager/models/hq/plan/PlanService.java:35-47`.
**Impact:** Unstructured stdout lines; a misleading throughput log; error-level noise from ordinary 400s that
drowns real errors and can trigger alerts; request data (e-mails, usernames) in error logs.
**Recommendation:** SLF4J only, `{}` placeholders (format numbers before logging), `WARN`/`DEBUG` for client
errors, no personal data at `ERROR`.
**Verified against:** N/A — SLF4J message format (`{}` is the only placeholder)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-06|BE-R09-06]]

### WP-R11-03
**Title:** Dead code
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Delete what nothing uses; version control remembers.
**Evidence:**
- Empty types: `BaseStorageProviderService` (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/BaseStorageProviderService.java:3-4`),
  `DownloadableParent` (`wpmanager/src/main/java/com/wpmanager/shared/defaultInterfaces/DownloadableParent.java:3-4`),
  and the empty `services` directory (`wpmanager/src/main/java/com/wpmanager/services`).
- Unused classes and methods: `BaseUserMapper` (`wpmanager/src/main/java/com/wpmanager/shared/models/baseUser/BaseUserMapper.java:7-28`),
  `IdempotencyManager.updateStatus` (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyManager.java:242-271`),
  `ClientEntity.getBaseUser` ([[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-11|WP-R07-11]]).
- Unused finders: `DownloadableRepository.findByName`, `findByVersion`, `findByFilesSignature`
  (`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableRepository.java:11-15`),
  `AuthorRepository.findByName`, `findByWebsite` (`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorRepository.java:13-14`),
  `FileRepository.findFileEntitiesByDownloadable` (`wpmanager/src/main/java/com/wpmanager/models/files/file/FileRepository.java:10`).
- A test endpoint, `GET /test` (`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityController.java:68-71`).
- A `"kit"` parent type accepted by the downloadable delete check but handled by no branch
  (`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:46-65`).
- Unused service collaborators ([[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-04|WP-R07-04]]) and the
  `StorageProviderManager` class comment's pending idempotency plan (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:28-39`).
**Impact:** Readers must work out that these do nothing; the dead code travelled into `backend/`.
**Recommendation:** Delete them; keep the idempotency plan in the vault backlog rather than a class comment.
**Verified against:** N/A — usage by `grep`
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-01|BE-R09-01]], [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-02|BT-R10-02]]

### WP-R11-04
**Title:** The project vault contradicts the code
**Severity:** 🟡 Medium
**Category:** hygiene
**Principle:** Documentation that states behavior must be verified against it, or marked historical.
**Evidence:**

| Vault claim | Code |
|---|---|
| Upload phase 1 is non-transactional (`wpmanager/wpManagerDocs/Docs/File Upload Process.md:218-227`) | Runs inside `PluginService.upload`'s transaction ([[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-02\|WP-R03-02]]) |
| Upload: "Validation errors → HTTP 400", "All errors → Rollback transaction" (`wpmanager/wpManagerDocs/Docs/File Upload Process.md:685-688`) | Wrapped uploads return 500 ([[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01\|WP-R04-01]]); `FailUpload` commits ([[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-01\|WP-R03-01]]) |
| After a failure, "retries … safely re-upload" (`wpmanager/wpManagerDocs/Docs/File Upload Process.md:288-292`) | A phase-1 failure blocks the version (WP-R03-01) |
| Replication: "Source file missing → Skip downloadable" (`wpmanager/wpManagerDocs/Docs/File Upload Process.md:692`) | The run aborts ([[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02\|WP-R05-02]]) |
| Idempotency table "prevents race conditions across multiple instances" (`wpmanager/wpManagerDocs/Docs/Upload Idempotency System.md:488`) | Per-instance cache can override it ([[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-04\|WP-R04-04]]) |
| JWT secret: `@Value("${tk.key}")`, "no fallback default" (`wpmanager/wpManagerDocs/Docs/Login and Security Architecture.md:134-141`) | Property is `TK_KEY` and has a literal fallback (`wpmanager/src/main/resources/application.properties:30`) |
| Task "Remove Hardcoded Defaults" marked done (`wpmanager/wpManagerDocs/Tasks/done/Optimize-Login-Architecture-step-3-Remove-Hardcoded-Defaults.md:1-13`) | The constant was removed from code, but the fallback moved into the properties file |
| Bug "Transaction Boundary Violation in StorageProviderManager" in `Bugs/done` (`wpmanager/wpManagerDocs/Bugs/done/Transaction Boundary Violation in StorageProviderManager.md`) | Partly true: the manager method lost its `@Transactional` and gained compensation, but its caller's transaction still wraps the upload |
| Class note for `JWTTokenGeneratorFilter` (`wpmanager/wpManagerDocs/Code/JWTTokenGeneratorFilter-java.md`) | No such class exists; login is `SecurityController` |
| Login review: HTTP Basic login, a generator filter, a DB lookup per request, CORS `*` (`wpmanager/wpManagerDocs/Reports/LoginArchitectureReview.md:11-16`, `wpmanager/wpManagerDocs/Reports/LoginArchitectureReview.md:36-40`, `wpmanager/wpManagerDocs/Reports/LoginArchitectureReview.md:61-72`) | All since changed; the report is not marked as historical |
| "God Class - PluginService.java Has 13 Dependencies" (`wpmanager/wpManagerDocs/Bugs/to-do/God Class PluginService.md`) | 17 today ([[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-04\|WP-R07-04]]) |

**Impact:** The vault is where the authors plan work; drift hides open Critical issues behind "done" labels
and makes new contributors (human or AI, see WP-R11-05) trust safeguards that do not exist.
**Recommendation:** Reopen the two partly-done items; add a "Verified against commit" line to system docs and
re-verify on change; mark reports as dated snapshots; delete or archive notes for removed classes.
**Verified against:** N/A — document comparison
**Confidence:** Confirmed (read in code)

### WP-R11-05
**Title:** AI-tool leftovers in the project root
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Keep repository configuration intentional.
**Evidence:** `wpmanager/.continue/rules/spring-dev-rule.md` tells an AI assistant the project uses MongoDB,
Elasticsearch, Neo4j, Redis, Cassandra, QueryDSL and Docker (`wpmanager/.continue/rules/spring-dev-rule.md:5-21`);
none are in the build. `wpmanager/.continue/mcpServers/new-mcp-server.yaml` is an unfilled template.
**Impact:** An assistant primed with that rule may generate code for technologies the project does not have.
**Recommendation:** Rewrite the rule to describe the actual stack and conventions (or remove it); delete the
template.
**Verified against:** N/A — file contents
**Confidence:** Confirmed (read in code)

## Vault Leads Not Confirmed

Open vault bug notes that did not become findings, with the reason. The other open notes are confirmed and
cited as prior art in the findings above (resource leak, long initial delay, single-threaded replication,
health check, load-all, EAGER fetching, map-based storage, multipart upload, monitoring, race condition,
god class, duplicate plugin/theme code, anemic model, SRP, package structure, memory-intensive checksum,
virus scanning).

| Vault note | Why not a finding |
|---|---|
| `wpmanager/wpManagerDocs/Bugs/to-do/Multiple Unnecessary Saves.md` | True (the upload saves the version twice and the plugin once), but harmless inside one transaction; subsumed by the target pattern in [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]] |
| `wpmanager/wpManagerDocs/Bugs/to-do/Temporary File Cleanup May Fail Silently.md` | The delete failure is logged at `INFO`, not silent; the real leak (temp file skipped on exceptions) is part of WP-R05-02 |
| `wpmanager/wpManagerDocs/Bugs/to-do/Implement Caching for Storage Provider Configuration.md` | An optimisation, not a defect; the per-call client construction is the actual cost (WP-R05-01) |
| `wpmanager/wpManagerDocs/Bugs/to-do/No Retry Mechanism for Failed Uploads.md` | The SDK applies its default retry policy, and a failed upload can be retried by the client once WP-R03-01 is fixed |
| `wpmanager/wpManagerDocs/Bugs/to-do/Poor Error Messages.md` | Service messages are specific; the problem is that the idempotency wrapper discards them (WP-R04-01) |
| `wpmanager/wpManagerDocs/Bugs/to-do/No Rate Limiting.md` | Uploads are admin-only; rate limiting is a deployment concern for this reference review |
| `wpmanager/wpManagerDocs/Bugs/to-do/Synchronous Upload Blocks Request Thread.md` | A design choice for a 100 MB admin upload; the real cost is the database connection held during it (WP-R03-02) |
| `wpmanager/wpManagerDocs/Bugs/to-do/No Progress Indication for Long-Running Uploads.md` | A product feature, not a code defect |
| `wpmanager/wpManagerDocs/Bugs/to-do/Missing Audit Logging.md`, `wpmanager/wpManagerDocs/Bugs/to-do/No Upload History or Audit Trail.md` | Feature requests; the unwritten audit columns are covered by WP-R06-08 |
| `wpmanager/wpManagerDocs/Bugs/to-do/No Storage Quota Management.md` | Feature request |
| `wpmanager/wpManagerDocs/Bugs/to-do/Implement Event-Driven Architecture.md`, `wpmanager/wpManagerDocs/Bugs/to-do/Use Value Objects for Domain Concepts.md` | Refactoring suggestions without a defect; the outbox idea appears in the WP-R05-06 recommendation |

## Recommended Target Pattern

- Names checked by review (`bootstrap`, `<Feature>Service`); SLF4J with `{}` only; client errors below `ERROR`.
- Dead code deleted on sight; empty packages not committed.
- Docs that state behavior carry the commit they were verified against, and "done" means verified in code;
  assistant rule files describe the real stack.

## Related Documents

- [[Docs/wpmanager/Explanations/01-Overview-and-Design-Philosophy]]
- [[Docs/wpmanager/Reviews/00-Review-Summary]]
- [[Docs/backend/Reviews/09-Code-Hygiene-Review]]
