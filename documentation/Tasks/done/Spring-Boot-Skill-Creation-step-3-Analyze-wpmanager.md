# Task: Analyze `wpmanager/` — Explanation and Review Documentation

#task #current #high-complexity #parent-spring-boot-skill-creation

**Parent:** [[Features/to-do/Spring-Boot-Skill-Creation|Spring Boot Skill Creation]]
**Parent Type:** Feature
**Related Step(s):** Phase 1 — Task 3 (Analyze `wpmanager/`), plus Phase 1 "Document findings in `documentation/Docs/`" for this project
**Estimated Complexity:** High

---

## Goal

Produce a complete, evidence-backed documentation set for the `wpmanager/` reference project. This is a
premium WordPress plugin/theme **repository and distribution backend**, with multi-provider S3 storage,
a two-phase upload with compensation, and upload idempotency. **Explanation** docs describe how it is
built. **Review** docs critique it. Everything follows the conventions and validator from Task 1.

---

## Parent Context

The parent Feature builds the `spring-boot-backend` skill from patterns in three reference projects.
Phase 1 produces per-project Explanations and Reviews under `documentation/Docs/<project>/`. Task 4
(later) compares the projects using stable finding IDs.

Parent constraints that apply here:
- Reference projects are **read-only**.
- Patterns must be grounded in real code.
- Topics that do not exist are skipped, not stubbed.
- The format is owned by [[Docs/Analysis-Doc-Conventions]] (Task 1) and must be followed exactly.

What sets wpmanager apart:
- **The name is misleading.** It does not remotely manage WordPress sites. There are no WP REST, SSH, or remote-execution clients. Admins upload versioned plugin/theme ZIPs, the files are stored in S3-compatible providers (AWS/Wasabi) and replicated across them, and clients (with plans, website quotas, and favorites) consume them. WordPress appears only as data: `wpID`, `wpVersion`, `Plan.wpIDs`.
- It holds the **most substantial non-CRUD engineering** of the three projects:
  - `shared/idempotency/` (`IdempotencyManager`, 519 LOC): state machine, hybrid in-memory/DB cache, result replay.
  - `models/storage/`: provider abstraction, `S3StorageClient`, `StorageProviderManager` two-phase upload with compensation.
  - `schedule/FileDuplicator`: cross-provider replication.
- **Direct ancestor of `backend/`.** Both use Spring Boot 3.4.1 / Java 21, and share the same `configuration/`, `shared/securityUser`, `shared/tools`, and `DefaultController`/`DefaultServiceImplements` lineage. `backend/` removed the storage/download domain and added the QueryDSL list engine.
- It ships its **own Obsidian vault**, `wpmanager/wpManagerDocs/`, containing:
  - 4 system docs;
  - 151 per-class notes;
  - 9 done and 29 to-do bug notes;
  - 12 done tasks;
  - a login architecture report.

  It records the authors' own intent and backlog. Some of it has drifted from the code.

---

## Preconditions / Dependencies

- **Task 1 must be done:** `documentation/Docs/Analysis-Doc-Conventions.md`, `scripts/validate-analysis-docs.py`, passing tests. Read the conventions' Changelog first.
- **Task 2 is not a hard dependency.** Tasks 2 and 3 are independent in content. If Task 2 is done, read [[Docs/BugTracker/Reviews/00-Review-Summary]] for **Related** links. If it is not done, link only to backend docs.
- **Do not run in parallel with Task 2.** Both edit the parent Feature, the memory bank and possibly the conventions Changelog. <!-- REVIEW-FIX: shared-file conflict -->
- `scripts/.snapshots/` and `scripts/.gitignore` were created by Task 1.
- The validator already knows project code `WP` for `wpmanager`.
- **Secrets constraint:**
  - `wpmanager/src/main/resources/application.properties:4-5` (DB credentials) and `:30` (fallback JWT secret);
  - `configuration/boostrap/AdminBoostrap.java:41-43` (seed credentials);
  - **`wpmanager/src/test/java/com/wpmanager/models/plugin/E2EPluginUploadTest.java:61-62,75-76` (cloud storage access/secret keys, verified present)**.

  Cite locations only; values are `<redacted>`. The Security review must recommend key rotation.

---

## Skills and Documentation Preparation

### Skills Reviewed

- `documentation-management` — Selected — doc placement and conventions. The reference project's own `wpManagerDocs/` is **read, never edited**.
- `memory-bank` — Selected.
- `solid-deep-design` — Selected. This project gives the best material for the depth/seam lens:
  - `StorageProviderEntity.getClient()` is a port-like seam that lives on a JPA entity. It has one real adapter (S3) plus a null FTP stub. Apply "one adapter = hypothetical seam".
  - `IdempotencyManager` is a candidate deep module. Test it with the depth diagnostic.
  - `PluginService` (17 collaborators) and `ThemeService` (a near-copy) are SRP/duplication cases.
- `find-docs` — Selected — see table.
- `tdd` — Selected — vertical slices, one doc at a time with the validator.
- `glossary-management` — Selected — propose domain terms (*Downloadable*, *Storage Provider*, *Default Provider*, *Replication*, *Idempotency Key*, *Two-Phase Upload*, *Compensation*).
- `solid` — Selected (reference).
- `superpowers:verification-before-completion` — Selected.

### Documentation Reviewed

Versions (from `wpmanager/pom.xml`):
- Spring Boot **3.4.1** and Java **21**;
- Spring Security **6.4.x** and Hibernate **6.6.x** (managed);
- JJWT **0.12.5**;
- AWS SDK for Java v2 `s3` **2.20.12**;
- MySQL (`mysql-connector-j`), with the `MySQL8Dialect` property (deprecated in Hibernate 6).

| Technology | Context7 ID to use | Notes |
|---|---|---|
| Spring Boot 3.4.1 | `/spring-projects/spring-boot/v3.4.1` | Exact. Only trust snippets whose source URL contains `/v3.4.1/`. |
| Spring Security 6.4 | `/spring-projects/spring-security/6.4.4` | Closest 6.4.x. Needed to check whether `@EnableMethodSecurity` on a `@Service` is honored (see lead below). Exact-version fallback: `javap` on `~/.m2/.../spring-security-core/6.4.2/` and `spring-security-config` 6.4.2. Pre-verified: `UserDetails` account-flag methods are `default` → true in 6.4.2, which is the mechanism behind the ignored-flags lead. |
| Spring Framework 6.2 (transactions, scheduling) | resolve with `ctx7 library "Spring Framework" "..."` | `@Transactional` rollback rules for checked exceptions; `jakarta.transaction.Transactional` vs Spring's; `@Scheduled`. |
| AWS SDK Java v2 | `/aws/aws-sdk-java-v2` | `S3Client` lifecycle (reuse vs per-call creation, `close()`), `headBucket`, multipart/Transfer Manager. The project pins 2.20.12, so confirm that any recommended API exists in 2.20.x. |
| Hibernate 6.6 | resolve with `ctx7 library "Hibernate ORM" "..."` | `MySQL8Dialect` deprecation, dialect auto-detection. |
| JJWT 0.12 | `/jwtk/jjwt` | Same as Task 1. |

Already verified during task creation (Context7, Boot 3.4.1): RFC 9457 `ProblemDetail` via
`spring.mvc.problemdetails.enabled` / `ResponseEntityExceptionHandler`.

### Related Existing Code

The Explore-pass inventory is a **lead list**. Verify everything. Paths are relative to `wpmanager/src/main/java/com/wpmanager/` unless noted.

- `wpmanager/pom.xml` — deps, surefire config, Lombok only (no QueryDSL).
- `configuration/security/SecurityConfig.java` (L47-58 chain, no `authorizeHttpRequests`; CORS L97), `SecurityController.java` (`POST /login`, `GET /test`), `configuration/filter/JwtTokenService.java`, `JWTTokenValidatorFilter.java`.
- `@EnableMethodSecurity` on **service classes**:
  - `models/downloads/plugin/PluginService.java:36`
  - `models/downloads/author/AuthorService.java:29`
  - `models/hq/plan/PlanService.java:20` (verified)
- `shared/defaultImplements/`, `shared/defaultInterfaces/` — generic CRUD (4 type params on the controller; no list/query engine).
- `shared/idempotency/` — `IdempotencyManager`, `IdempotentUploadService`, `IdempotencyKeyEntity`, `IdempotencyKeyRepository`, `UploadStatus`.
- `models/storage/` — `storageProvider/StorageProviderEntity` (abstract, JOINED, `getClient()`), `s3/S3ProviderEntity`, `s3/S3StorageClient`, `s3/S3ProviderMapper` (L20-21, 40-41 copy secret keys into DTOs), `s3/S3FileUploadController` (`/tests3/*`), `ftp/FTPProviderEntity` (`getClient()` returns null, L75), `storageProviderManager/StorageProviderManager`.
- `models/downloads/plugin/PluginService.java` (upload L158-207; `findAll()` L144, L260), `PluginController.java` (upload L45).
- `models/downloads/theme/ThemeService.java` — theme upload builds an unsaved `DownloadableEntity` and passes its null id (L174-183, verified).
- `models/files/` — `downloadable/`, `file/`, `image/`.
- `models/hq/` — `admin/`, `client/` (`ClientEntity.getBaseUser()` recursion L77-78), `plan/`, `website/` (`WebsiteService` L15 imports `jakarta.transaction.Transactional`; L70 `URLValidator`), `favoriteList/`.
- `schedule/FileDuplicator.java`, `schedule/IdempotencyKeyCleanup.java`.
- `shared/tools/` — `FileSigner`, `ChecksumUtils`, `UploadValidator`, `URLValidator`, `TextFieldValidator`, `DiskBasedMultipartFile`, `AuthUserUtil`, `ErrorHTTPRes`.
- `exceptions/GlobalExceptionHandler.java`.
- `wpmanager/src/main/resources/application.properties`, `application-test.properties` (both in `main`).
- `wpmanager/src/test/java/com/wpmanager/` — 13 `@DataJpaTest` repository tests, `E2EAuthorTest` (68 tests), `E2EPluginUploadTest`, `URLValidatorTest` (network calls), suites, `TestLauncher`.
- `wpmanager/wpManagerDocs/` — the project's own vault. The key files are:
  - `Docs/File Upload Process.md`
  - `Docs/Upload Idempotency System.md`
  - `Docs/Login and Security Architecture.md`
  - `Docs/Secret Management.md`
  - `Bugs/to-do/*` (29)
  - `Reports/LoginArchitectureReview.md`

Task 1 outputs to read first: [[Docs/Analysis-Doc-Conventions]], [[Docs/backend/backend-Index]], [[Docs/backend/Reviews/00-Review-Summary]].

---

## Implementation Details

### Approach

The same pipeline as Tasks 1–2, with three adaptations:

1. **Flows get their own docs.** The upload pipeline, the idempotency subsystem, and storage/replication are this project's distinctive engineering. Each gets a dedicated Explanation with sequence and state diagrams, and a dedicated Review.
2. **Use the project's own vault as a secondary source, and audit it.**
   - `wpManagerDocs/Docs/*` shows *intent*. Explanations may quote intent, but must state where the code diverges.
   - `wpManagerDocs/Bugs/to-do/*` is a ready-made lead list for Reviews. Verify each lead against the code, and cite the vault note as prior art in the finding's Evidence, e.g. "Also recorded in `wpmanager/wpManagerDocs/Bugs/to-do/N+1 Eager Fetching.md`".
   - Vault claims that are false today become findings in the Code Hygiene and Docs Drift review. Examples: done bugs that are only partly fixed, the stale `JWTTokenGeneratorFilter` note.
3. **Shared code with `backend/`.** Much of `configuration/` and `shared/` matches backend's. Document it standalone, as the conventions require. Findings with the same root cause get their own `WP-` ID and a **Related** link to the `BE-` ID. The Overview's Lineage paragraph states the direction: wpmanager → backend.

### Target document set

`documentation/Docs/wpmanager/`:

```
wpmanager-Index.md
Explanations/
  01-Overview-and-Design-Philosophy.md      ← what it really is, domain areas (downloads/files/hq/storage), lineage → backend, stack
  02-Build-Tooling-and-Dependencies.md      ← pom, used vs unused starters, surefire, test.sh/TestLauncher, no Docker, .continue rule file
  03-Package-Structure-and-Module-Anatomy.md ← package tree, 8-file module (no ListDTO/QueryProfile), module catalogue, cross-module coupling
  04-Generic-CRUD-Framework.md              ← Default* stack (4/5 type params), inherited endpoints, override catalogue, casts
  05-Domain-Model-and-Persistence.md        ← BaseUser JOINED + StorageProvider JOINED trees, catalog/files/hq relations, IDs, fetch, @Query use, transactions
  06-Authentication-and-Authorization.md    ← JSON login, JWT (claims-only validation), method security on services, AuthUserUtil ownership checks, client API tokens
  07-Upload-Pipeline.md                     ← POST /plugin/upload end-to-end: validate → checksum → sign → persist → two-phase upload → compensate
  08-Upload-Idempotency.md                  ← key derivation, state machine (PENDING/PROCESSING/COMPLETED/FAILED), hybrid cache, replay, cleanup job
  09-Storage-Providers-and-Replication.md   ← provider abstraction, default provider, S3StorageClient, FileDuplicator job, FTP stub
  10-Customer-Domain.md                     ← Client, Plan (wpIDs, quotas), Website, FavoriteList; ownership rules
  11-Error-Handling.md                      ← exception → status table, ErrorHTTPRes, security handlers, DuplicateUploadException
  12-Configuration-and-Secrets.md           ← properties, env vars + fallbacks (redacted), FileSigner fail-fast, test profile
  13-Testing-Strategy.md                    ← tag suites, @DataJpaTest repo tests, MockMvc E2E with real JWTs, TestAuthenticationHelper
  14-Recipe-Build-a-Project-This-Way.md     ← new project + new downloadable-type module + upload flow "the wpmanager way"
Reviews/
  00-Review-Summary.md
  01-Security-Review.md
  02-CRUD-Framework-and-API-Design-Review.md
  03-Upload-Transactions-and-Consistency-Review.md
  04-Idempotency-Review.md
  05-Storage-Integration-Review.md
  06-Domain-Model-and-Persistence-Review.md
  07-Service-Design-Review.md
  08-Error-Handling-and-Validation-Review.md
  09-Build-Dependencies-and-Configuration-Review.md
  10-Testing-Review.md
  11-Code-Hygiene-and-Docs-Drift-Review.md
```

### Files to Create/Modify

- [x] `documentation/Docs/wpmanager/wpmanager-Index.md`
- [x] `documentation/Docs/wpmanager/Explanations/01…14-*.md` — 14 explanation docs
- [x] `documentation/Docs/wpmanager/Reviews/00…11-*.md` — 12 review docs
- [x] `documentation/Docs/Analysis-Doc-Conventions.md` — gap found: reviews may add extra `##` sections after `## Findings` (section 5 + Changelog) — only if a rule gap is found (with a Changelog entry)
- [x] `documentation/Features/to-do/Spring-Boot-Skill-Creation.md` — tick "Analyze `wpmanager/`", link task + index; if Tasks 1–3 are all done, tick "Document findings in `documentation/Docs/`"
- [x] `documentation/Memory/context.md`, `progress.md`, `tech.md`, `known-issues.md`, `architecture.md` — end-of-task update

---

## Step-by-Step Implementation

### Step 0: Preconditions and snapshot

- [x] `python3 -m unittest discover -s scripts/tests -v` → passes.
- [x] Read the conventions doc, including its Changelog.
- [x] Snapshot:

```bash
cd /home/jlievano/Dropbox/CodeProjects/spring-boot-backend-skill
find wpmanager -type f -not -path 'wpmanager/target/*' -print0 \
  | sort -z | xargs -0 sha256sum > scripts/.snapshots/wpmanager.sha256
```

#### Edge Cases
1. **Case:** Task 1 is incomplete — **stop** and report to the user.
2. **Case:** `wpmanager/wpManagerDocs/` contains Obsidian plugin files (`.obsidian/`). They are included in the snapshot. Do not open that vault in Obsidian during this task, because Obsidian rewrites `workspace.json` and the checksum check would fail.

---

### Step 1: Tracer bullet — `01-Overview-and-Design-Philosophy.md` + index skeleton

- [x] Index skeleton listing the planned docs as plain text.
- [x] Overview:
  - the real purpose (repository/distribution, not WP site management), with evidence (no WP clients; WP only in data fields);
  - a Mermaid context diagram: Admin → upload → providers → replication; Client → plan/websites/favorites;
  - the four domain areas;
  - the stack table cited to `pom.xml`;
  - design philosophy: generic CRUD for catalog entities plus hand-built flows for upload, idempotency and storage, with reliability features added after bug reports (cite the vault's `Bugs/done/`);
  - a **Lineage** paragraph (BugTracker-style generic stack → wpmanager → backend), verified by diffing the shared packages with backend. List what backend removed or added.
- [x] `python3 scripts/validate-analysis-docs.py wpmanager` → exit 0.

---

### Step 2: Explanation docs 02–13

Write each doc and validate it before starting the next. Minimum content per doc, beyond the template:

- **02 Build, Tooling and Dependencies:**
  - every dependency, marked used or unused (verified by grep);
  - the stale "aws-java-sdk" comment;
  - the Mockito pin with no Mockito usage;
  - surefire config;
  - `test.sh` → `TestLauncher` → tag suites;
  - no Docker or CI;
  - the `.continue/rules/spring-dev-rule.md` AI rule file.
- **03 Package Structure and Module Anatomy:**
  - the package tree with counts;
  - the 8-file module (`Controller, Service, Repository, Entity, Form, DTO, MiniDTO, Mapper`) and how it differs from backend's 10-file module;
  - a module catalogue table: route, overrides, custom endpoints, and missing pieces (e.g. theme category has no controller, image is entity-only);
  - the empty `services/` package;
  - the cross-module dependency graph for `PluginService` (Mermaid).
- **04 Generic CRUD Framework:**
  - a class diagram;
  - the inherited endpoints;
  - `@PreAuthorize("isAuthenticated()")` defaults;
  - the override catalogue;
  - typed repository access through casts;
  - base `update`, stated as fact, with the finding linked.
- **05 Domain Model and Persistence:**
  - a Mermaid ER/class diagram covering both JOINED trees and the catalog, files and hq relations;
  - `StorageProviderEntity.files` as a `Map` keyed by signature;
  - the ID strategy;
  - the equals/hashCode variants, as fact;
  - repositories and the 4 `@Query`;
  - transaction placement, including the `jakarta.transaction.Transactional` outlier;
  - MySQL, dialect, and `ddl-auto`.
- **06 Authentication and Authorization:**
  - Mermaid sequences for login and for an authenticated request;
  - claims-only validation (no DB hit; cite the vault task that removed the DB hit);
  - **method-security activation**: `@EnableMethodSecurity` sits on three `@Service` classes. Determine and state whether Spring processes it there. Leading hypothesis: `@EnableMethodSecurity` is meta-annotated with `@Import`, and `@Component` classes are processed as lite configuration candidates, so it would be honored and apply globally. Confirm with Context7 (Spring Framework `@Import` on `@Component` / lite mode) and cite the answer. If it cannot be confirmed statically, mark it *Needs runtime verification*;
  - the `@PreAuthorize` inventory (35);
  - `AuthUserUtil` ownership checks (Website, FavoriteList only);
  - admin-minted client tokens;
  - the derived client passwords;
  - the seed admin.
- **07 Upload Pipeline:**
  - a sequence diagram of `POST /plugin/upload` from controller to response;
  - `UploadValidator` rules;
  - checksum and signature derivation;
  - the duplicate-version check;
  - the two phases and the compensation path (flowchart with the failure branches);
  - how theme upload differs, stated as fact.
- **08 Upload Idempotency:**
  - a Mermaid state diagram of `UploadStatus` transitions;
  - how the key is derived (file checksum);
  - the in-memory map vs the DB table and when each is consulted;
  - result replay (`resultJson`);
  - TTL and the `IdempotencyKeyCleanup` schedule;
  - the `DuplicateUploadException` contract;
  - how `IdempotentUploadService` wraps the supplier (`ThrowingSupplier`).
- **09 Storage Providers and Replication:**
  - the provider hierarchy and `getClient()` seam;
  - default provider selection;
  - the `S3StorageClient` operations, including the `headBucket` pre-check, and how Wasabi works through `endpointOverride`;
  - the `FileDuplicator` algorithm: schedule values, static guard, temp files, `DiskBasedMultipartFile`, shown as a flowchart;
  - FTP stub, stated as fact.
- **10 Customer Domain:**
  - Client ↔ Plan (`wpIDs`, quotas) ↔ Website ↔ plugins/themes;
  - FavoriteList;
  - the ownership rules;
  - `URLValidator` probing on website insert.
- **11 Error Handling:**
  - an exception → status table;
  - the `ErrorHTTPRes` example;
  - the security handlers and the filter building the same shape by hand;
  - `ResponseStatusException` in `/tests3`, as fact.
- **12 Configuration and Secrets:**
  - every property key with its purpose (secrets `<redacted>`);
  - env vars and their fallbacks. Say that a fallback exists, never what it is;
  - `FileSigner` fail-fast;
  - `upload.max-file-size` undefined;
  - test properties in `main`;
  - `spring.task.scheduling.enabled`: check with Context7 whether it is a real Boot 3.4 property and state the result;
  - a comparison with the vault's `Docs/Secret Management.md`.
- **13 Testing Strategy:**
  - the test inventory by layer with counts;
  - tags and suites;
  - `TestAuthenticationHelper` (real JWTs in E2E);
  - the H2 test profile;
  - network-dependent `URLValidatorTest`;
  - the latest surefire evidence in `wpmanager/target/surefire-reports/`, if present.

- [x] 02 through 13 written, and each validated before starting the next

#### Edge Cases
1. **Case:** the vault doc and the code disagree — the Explanation follows the code, and the disagreement becomes a finding in review 11.
2. **Case:** the method-security hypothesis turns out false (not honored) — the security review severity changes: every `hasRole('ADMIN')` becomes inert, as in backend. Re-classify before writing Review 01.

---

### Step 3: Review docs 01–11

Per finding:
1. Re-read the code.
2. Assign severity.
3. Name the principle.
4. Write a recommendation verified at the right version.
5. Add a **Related** link to `BE-`/`BT-` IDs where the root cause is shared.
6. Cite the vault note if the authors already recorded the issue.

Leads to verify and classify:

- **01 Security:**
  - **No URL-level authorization.**
  - `/tests3/*` debug controller with no `@PreAuthorize`: anonymous upload, delete and download against the bucket.
  - `@PreAuthorize` on the non-bean `S3StorageClient` is inert.
  - Spring Data REST likely exports all repositories, including `S3ProviderEntity` secret keys and user hashes (*Needs runtime verification* unless proven).
  - IDOR and privilege matrix: non-overridden base operations need only `isAuthenticated()`. Enumerate the affected routes from the override catalogue.
  - S3 secret keys returned in `S3ProviderDTO`/`MiniDTO` and stored in plaintext.
  - **Cloud keys committed in a test file.** Recommend rotation, and removal from history (describe it, do not do it).
  - Hardcoded DB credentials, the JWT fallback secret, and the seed admin.
  - Account flags ignored (`getX` vs `isX`).
  - 24h JWT with no revocation.
  - SSRF surface in `URLValidator`.
  - Duplicate `@EnableWebSecurity`.
  - Method security enabled from service classes (placement smell even if it works).
- **02 CRUD Framework and API Design:**
  - no-op base `update`;
  - casts to the concrete repository or service (DIP/LSP leak);
  - `getAll` → `Set`;
  - no pagination anywhere;
  - 200 on insert, and delete returns a body;
  - `DELETE /downloadable` with a request body alongside the base `DELETE /{id}` that skips storage cleanup;
  - `downloadable` route without a leading slash;
  - no versioning or OpenAPI;
  - depth diagnostic of the Default* stack vs backend's version.
- **03 Upload Transactions and Consistency:**
  - the S3 network call runs inside the DB transaction (`PluginService.upload` `@Transactional`);
  - "phase 2" joins the outer transaction, so the two-phase design is nominal;
  - `rollbackFor` covers only `InvalidInsertDetails`, so a checked `FailUpload` commits an orphan row. Verify the rollback rule in the Spring Framework docs;
  - the vault's "Transaction Boundary Violation" note is marked done but is only partly true;
  - `jakarta.transaction.Transactional` in `WebsiteService` has different rollback semantics;
  - the `FileDuplicator` job has no transaction.
- **04 Idempotency:**
  - `IdempotentUploadService` L112-114 checks `getCause()` while the supplier throws directly, so validation errors become 500s;
  - the key is the file checksum only, so the same ZIP for a different plugin or version replays the wrong result;
  - a crash while PENDING blocks retries until the TTL expires;
  - the hybrid cache has no cross-instance coherence (single-node assumption);
  - `ThrowingSupplier` design. Also run the depth diagnostic, and record strengths.
- **05 Storage Integration:**
  - an `S3Client` is created per `getClient()` call and never closed (`StorageProviderManager` L242, L301; `FileDuplicator` L100, L114, L145-146);
  - `headBucket` runs before every operation;
  - whole-object `byte[]` downloads;
  - no multipart or Transfer Manager, and no retries or backoff (check the SDK defaults with Context7);
  - the `getClient()` seam lives on a JPA entity (infrastructure on the domain model);
  - one real adapter, since the FTP one returns null;
  - `FileDuplicator`: static guard, single thread, ~83 min initial delay, temp-file handling, no health reporting;
  - an EAGER `files` map on the provider.
- **06 Domain Model and Persistence:**
  - `hashCode()` returns 42 in three entities;
  - Lombok `@Data` on JPA entities with an EAGER map;
  - ordinal role storage;
  - `favoritePlugins`/`favoriteThemes` modeled as one-to-many where many-to-many is needed;
  - `mainImage` join column naming;
  - the stale `@MappedSuperclass` Javadoc;
  - EAGER collections;
  - `findAll()`-then-filter;
  - no migrations;
  - deprecated `MySQL8Dialect`;
  - audit fields never set.
- **07 Service Design:**
  - `PluginService` has 17 collaborators (god class);
  - `PluginService` and `ThemeService` are near-duplicates (diff them and cite);
  - services reach into other modules' repositories and mappers;
  - 11 `@Lazy` constructors hide cycles;
  - an anemic model with transaction-script services (cite the vault's own "Anemic Domain Model" note);
  - theme upload is broken (unsaved entity, null id, no duplicate-version check);
  - mapper bugs (`WebsiteMapper` L88, `DownloadableMapper` L45, the `ClientMapper` TODO, `AdminMapper` username);
  - the `WebsiteService` uniqueness check compares a normalized value to a raw one;
  - NPEs in `ClientService` (L58, L70);
  - `ClientEntity.getBaseUser()` recursion.
- **08 Error Handling and Validation:**
  - no handlers for `MethodArgumentNotValidException`, `HttpMessageNotReadable`, `MaxUploadSizeExceeded`, or a generic `Exception`;
  - raw exception messages on 500;
  - a mixed error format (`ResponseStatusException`);
  - `new ObjectMapper()` per error;
  - Forms have almost no Bean Validation;
  - `TextFieldValidator` uses a SQL-keyword blacklist, which is ineffective and rejects legitimate names;
  - the MIME check trusts the client header;
  - not RFC 9457.
- **09 Build, Dependencies and Configuration:**
  - unused starters (batch, webflux, web-services, websocket, data-jdbc, jdbc) and Mockito;
  - Data REST (cross-link to Security);
  - test properties in `main`;
  - `upload.max-file-size` undefined;
  - the `spring.task.scheduling.enabled` validity;
  - hardcoded CORS origin;
  - no Actuator or health checks for the replication job;
  - no Docker.
- **10 Testing:**
  - no authorization tests;
  - no service or unit tests; Mockito is unused;
  - the upload E2E never uploads;
  - network-dependent `URLValidatorTest`;
  - `WpmanagerApplicationTests` needs MySQL;
  - no tests for idempotency, storage, or replication;
  - strengths: extensive `@DataJpaTest` coverage and the 68-test E2E author suite with real JWTs.
- **11 Code Hygiene and Docs Drift:**
  - `boostrap` typo;
  - the `AdminServiceImpl` naming outlier;
  - `System.out` in 2 places;
  - the `{:.2f}` placeholder that SLF4J does not support;
  - logger string concatenation;
  - `logger.error` for validation failures;
  - dead code: `BaseStorageProviderService`, `DownloadableParent`, the empty `services/`, `BaseUserMapper`, the `GET /test` endpoint, and the `"kit"` parent type;
  - **vault drift**: the stale `JWTTokenGeneratorFilter-java.md`, an outdated `LoginArchitectureReview.md`, "Remove Hardcoded Defaults" marked done while the fallback remains, and the partially true "Transaction Boundary Violation" done note.

- [x] 01 through 11 written, and each validated before starting the next

#### Edge Cases
1. **Case:** a vault `Bugs/to-do` item is not supported by the code — do not create a finding. List it under "Vault leads not confirmed" in review 11.
2. **Case:** a runtime-only claim (Data REST exposure, method-security activation) — mark it *Needs runtime verification* with the exact check. Do not start MySQL.

---

### Step 4: Recipe doc — `14-Recipe-Build-a-Project-This-Way.md`

- [x] A numbered recipe:
  1. pom essentials (used dependencies only);
  2. package skeleton;
  3. the Default* stack;
  4. security wiring;
  5. add a **new downloadable type** end to end (8-file module plus upload endpoint), with a minimal sketch per file in the project's style;
  6. wire it into idempotent upload, storage and replication;
  7. tests per layer.
- [x] `> ⚠️ Review:` callouts wherever a step copies a flagged pattern: the transaction around network I/O, the checksum-only idempotency key, the per-call `S3Client`, the Plugin/Theme duplication, and missing URL authorization.
- [x] Validate.

---

### Step 5: Summary, index, cross-links

- [x] `Reviews/00-Review-Summary.md` must contain:
  - every `WP-` finding by severity;
  - counts;
  - the Strengths to Keep roll-up (e.g. the idempotency state machine concept, compensation on upload failure, provider abstraction intent, tag-based test suites, the E2E suite with real JWTs, `FileSigner` fail-fast);
  - Top 5 Recommendations;
  - a **"Shared with backend"** table: `WP-` ID ↔ `BE-` ID for same-root-cause findings;
  - candidate patterns for the skill.
- [x] Finalize `wpmanager-Index.md`: reading order, all links, merges or skips, counts.
- [x] Back-fill the "Known Limitations" links.
- [x] `python3 scripts/validate-analysis-docs.py wpmanager` → exit 0.

---

### Step 6: Parent, memory bank, glossary

- [x] Parent Feature:
  - tick `Analyze wpmanager/`;
  - link this task and `[[Docs/wpmanager/wpmanager-Index]]`;
  - if Tasks 1–3 are all complete, also tick "Document findings in `documentation/Docs/`" and note that Task 4 (cross-project comparison) is next.
- [x] Memory bank:
  - `context.md` → next is Task 4 (or Task 2 if still pending);
  - `progress.md` → a dated entry;
  - `tech.md` → the wpmanager stack, including AWS SDK v2;
  - `known-issues.md` → the reference project's own vault must not be opened in Obsidian during analysis, and cloud keys exist in a test file;
  - `architecture.md` → the lineage BugTracker → wpmanager → backend.
- [x] Propose glossary terms — proposed in the final report, not added (the glossary is handled separately) (*Downloadable*, *Storage Provider*, *Default Provider*, *Replication*, *Idempotency Key*, *Two-Phase Upload*, *Compensation*). Add them only after the user confirms. — **Done 2026-09-29:** all seven added with the user's approval, plus File Copy, File Signature and Plan Entitlement (see [[Glossary/Glossary]]).

### Step 7: Verify the read-only constraint

- [x] `sha256sum --quiet -c scripts/.snapshots/wpmanager.sha256` → exit 0.

---

## Design Decisions

**Decision 1:** Dedicated docs for upload, idempotency, and storage/replication.
- **Why:** these are the project's distinctive, non-CRUD engineering and the most reusable ideas for a skill phase on file handling. Folding them into a generic "services" doc would bury them.
- **Alternatives considered:** a single "File Handling" doc. Rejected because three state machines and flows in one doc make it unreadable and weaken the reviews' focus.

**Decision 2:** Use the project's own vault as intent and prior art, but never as evidence of behavior.
- **Why:** the vault has already drifted (a stale class note, "done" tasks that are only partly true). Code is evidence. The vault explains *why*, and its drift is itself a finding.
- **Alternatives considered:** ignoring the vault. Rejected because it loses the authors' rationale and a 29-item lead list.

**Decision 3:** Standalone docs even where the code matches backend's, with explicit **Related** links.
- **Why:** the user asked for per-project understanding. Task 4 needs per-project IDs to count how widespread each issue is.
- **Alternatives considered:** a shared "common framework" doc. Rejected because it breaks the per-project structure, and Task 4 is where cross-project synthesis belongs.

---

## Testing Considerations

### Automatic Validation

- [x] Run `python3 -m unittest discover -s scripts/tests -v` → passes.
- [x] Run `python3 scripts/validate-analysis-docs.py wpmanager` → exit 0, no output lines.
- [x] Run `python3 scripts/validate-analysis-docs.py backend` → still exit 0. If Task 2 is done, also run it for `BugTracker` → exit 0.
- [x] Run `grep -c '^### WP-R' documentation/Docs/wpmanager/Reviews/0[1-9]-*.md documentation/Docs/wpmanager/Reviews/1[0-9]-*.md` → each review has ≥ 1 finding.
- [x] Run `sha256sum --quiet -c scripts/.snapshots/wpmanager.sha256` → exit 0.
- [x] Run `grep -rniE '(password|secret|key)\s*[=:]\s*[^<[:space:]]' documentation/Docs/wpmanager` → inspect every hit; no real secret values (the test-file cloud keys in particular).

### Manual Validation

- [x] Open `wpmanager-Index.md` in **this workspace's** Obsidian vault (not `wpManagerDocs`), and confirm that the links work and the state/sequence diagrams render.
- [x] Check the upload sequence diagram in `07-…` against `PluginService.upload` and `StorageProviderManager`.
- [x] Spot-check 5 findings against the cited lines.
- ~~Rotate the cloud storage keys~~ — not an open action: the owner knows these (and the git-history secrets) are exposed and has accepted this as a known bad practice for now; it must not be repeated (see [[Memory/known-issues]]).

**Manual validation confirmed by the user on 2026-09-29.**

**Rule:** Run automatic checks when possible. Manual checks are for the user.

---

## Related Code Explanations

- [[Docs/Analysis-Doc-Conventions]] — Format contract (Task 1).
- [[Docs/backend/backend-Index]] — Descendant project, for lineage and **Related** links.
- `wpmanager/wpManagerDocs/Docs/File Upload Process.md`, `Upload Idempotency System.md` — Authors' intent for the upload and idempotency flows (read-only).

---

## Completion Criteria

- [x] Parent document reviewed and reflected accurately in this task
- [x] Task 1 outputs present and conventions followed (including any Changelog changes)
- [x] Relevant skills reviewed and selected for this task
- [x] Version-matched documentation used; non-pinned sources labeled
- [x] All Explanation docs created; module catalogue complete
- [x] Method-security activation question answered (with source) or marked for runtime verification
- [x] All Review docs created; every finding complete; vault drift and unconfirmed vault leads recorded
- [x] Review Summary lists every `WP-` ID, plus the "Shared with backend" table; index links every doc
- [x] `python3 scripts/validate-analysis-docs.py wpmanager` exits 0; earlier projects still validate
- [x] `wpmanager/` unchanged (checksum); other reference projects unchanged
- [x] No secret values in any doc (checked programmatically against the literal values); the exposed keys are documented as an owner-accepted known bad practice (WP-R01-06) rather than a rotation action
- [x] Manual validation steps documented for the user
- [x] Parent Feature updated (Task 3 ticked + linked)
- [x] Memory bank updated; glossary terms proposed to the user

---

## Post-Review Notes

**Status:** all automatic completion criteria met; ready for the user's manual validation (4 items above, the
key-rotation item is superseded by the owner's decision).

### Validation results (2026-09-29)
- `python3 -m unittest discover -s scripts/tests` → 44 tests, OK.
- `python3 scripts/validate-analysis-docs.py wpmanager` / `backend` / `BugTracker` → exit 0, no output.
- `grep -c '^### WP-R' …` → 15, 8, 6, 6, 7, 10, 11, 7, 7, 6, 5 findings in reviews 01–11 (88 total).
- `sha256sum --quiet -c` on `wpmanager.sha256` (created in Step 0), `backend.sha256`, `bugtracker.sha256` → exit 0.
- Secret grep: every hit inspected (log statements, code excerpts, `${VAR:…}` placeholders shown as `<redacted>`);
  a scripted check of the six literal secret values in the properties and test file found none in the docs.

### Output
- [[Docs/wpmanager/wpmanager-Index]]: 14 explanations, 11 reviews + summary, 88 findings (9 🔴, 16 🟠, 43 🟡,
  20 🟢); 34 share a root cause with `backend/` ("Shared with backend" table in [[Docs/wpmanager/Reviews/00-Review-Summary]]).
- Method-security question answered: honored and global — `javap` on `spring-context-6.2.1.jar`
  (`ConfigurationClassUtils.candidateIndicators` includes `@Component` and `@Import`) and
  `spring-security-config-6.4.2.jar` (`@EnableMethodSecurity` → `@Import(MethodSecuritySelector)`), plus the
  recorded E2E run (CLIENT → 403, anonymous → 401). Explained in [[Docs/wpmanager/Explanations/06-Authentication-and-Authorization]];
  placement smell is WP-R01-11.

### Deviations from this task doc (code wins over leads)
- 12 `@DataJpaTest` classes, not 13; 30 open vault bug notes, not 29.
- `upload.max-file-size` is not "undefined": `UploadValidator` has a 100 MB default (WP-R09-05).
- "No authorization tests" is partly wrong: the author E2E suite asserts 401/403; the finding is "one module
  only" (WP-R10-01).
- `jakarta.transaction.Transactional` has the same default rollback rules as Spring's; the real issue is that
  it replaces the class-level Spring rollback rule (WP-R03-06, Low).
- `spring.task.scheduling.enabled` confirmed not to exist in Boot 3.4.1 (configuration metadata, WP-R09-04).
- "No retries" lead: the SDK applies its default retry policy; recorded under unconfirmed vault leads, and
  timeouts/retry configuration folded into WP-R05-07.
- New findings not in the lead list: failed provider upload commits a blocking version row (WP-R03-01),
  plugin/theme delete `ConcurrentModificationException` (WP-R07-02), replication aborts on one bad version
  (WP-R05-02), `/s3` returns secrets to any client (WP-R01-04), idempotency key ignores target (WP-R04-02 was a lead).
- Review 11 adds a `## Vault Leads Not Confirmed` section; the conventions now allow extra `##` sections after
  `## Findings` (section 5 + Changelog).
- Per the user: the committed credentials are documented as an owner-accepted known bad practice, not a
  rotation action; glossary terms are proposed only, not added.

### Unresolved / for the user
- WP-R01-05 (Spring Data REST exposure) stays *Needs runtime verification*; the exact `curl` check is in the finding.
- Surefire reports were read as-is (dated 2025-10-29); the wpmanager tests were not re-run.

