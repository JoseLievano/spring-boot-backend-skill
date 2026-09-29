# Task: Analyze `BugTracker/` — Explanation and Review Documentation

#task #current #high-complexity #parent-spring-boot-skill-creation

**Parent:** [[Features/to-do/Spring-Boot-Skill-Creation|Spring Boot Skill Creation]]
**Parent Type:** Feature
**Related Step(s):** Phase 1 — Task 2 (Analyze `BugTracker/`), plus Phase 1 "Document findings in `documentation/Docs/`" for this project
**Estimated Complexity:** High

---

## Goal

Produce a complete, evidence-backed documentation set for the `BugTracker/` reference project. The
**Explanation** docs describe how this multi-tenant project-management SaaS backend is built. The
**Review** docs critique it, and judge it against **its own stack (Spring Boot 2.7)** as well as against
current practice. Everything follows the conventions and validator established by Task 1.

---

## Parent Context

The parent Feature builds the `spring-boot-backend` skill from patterns in three reference projects.
Phase 1 is analysis. Its output is per-project Explanations and Reviews under `documentation/Docs/<project>/`.
Task 4 (later) compares all three projects, using the stable finding IDs.

Parent constraints that apply here:
- Reference projects are **read-only**.
- Patterns must be grounded in real code.
- Topics that do not exist are skipped, not stubbed.
- Task 1 owns the format. This task **must** follow [[Docs/Analysis-Doc-Conventions]] exactly and must not invent a variant.

What sets BugTracker apart from the other two projects:
- It is the **oldest and largest** project: 341 main Java files (~17.7k LOC), 29 feature modules, 32 entities.
- It runs on **Spring Boot 2.7.0**, with javax namespace and Spring Security 5.7. The other two projects are on Boot 3.4.1.
- Its domain is a **two-sided SaaS**: `models/HQ` is the operator side (MainHQ, plans, admins, HQ employees, customer clients, SaaS invoices). `models/client` is the tenant workspace, keyed by `BusinessEntity`, and holds projects, tasks, channels, comments, mentions, docs, KB, statuses, priorities and types.
- It is the likely **ancestor** of the generic CRUD stack (`DefaultController`/`DefaultServiceImplements`) that wpmanager and `backend/` inherited. Its QueryDSL filter engine (`CommonPathExpression` + per-module `XPredicate`) is the predecessor of backend's `shared/query` engine.
- It has **almost no tests**: one `contextLoads` test that needs a live MySQL.

---

## Preconditions / Dependencies

- **Task 1 must be done**:
  [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend]] (or `Tasks/current/` if not yet moved).
  The following must exist:
  - `documentation/Docs/Analysis-Doc-Conventions.md`
  - `scripts/validate-analysis-docs.py`
  - `scripts/tests/test_validate_analysis_docs.py`, passing
- If Task 1's conventions changed during execution, read its **Changelog** section first. The conventions doc wins over anything in this task.
- **Run Tasks 2 and 3 sequentially, not in parallel.** Both edit the parent Feature, the memory bank and possibly the conventions Changelog, so concurrent runs would clobber each other. <!-- REVIEW-FIX: shared-file conflict -->
- `scripts/.snapshots/` and `scripts/.gitignore` were created by Task 1.
- The validator already knows the `BugTracker` project code (`BT`). If it does not, that is a Task 1 defect: fix it there, with a test, before starting.
- **Secrets constraint:** `BugTracker/src/main/resources/application.properties:3-4` (DB credentials), `configuration/security/SecurityConstant.java:5` (JWT key) and `shared/tools/firstInstallCheck.java` (seed passwords) contain secret values. Cite the locations only, and write `<redacted>` for the values.

---

## Skills and Documentation Preparation

### Skills Reviewed

- `documentation-management` — Selected — doc placement and Obsidian conventions.
- `memory-bank` — Selected — context in, updates out.
- `solid-deep-design` — Selected — the review lens. BugTracker is the clearest test of it: a generic framework that looks deep but has 29 near-identical 10-file modules, 256-line hand-written predicates, and `@Lazy` in 28 files to break mapper cycles. Apply the depth diagnostic and the deletion test explicitly in the CRUD-framework review.
- `find-docs` — Selected — version-matched verification. See the table below.
- `tdd` — Selected — vertical slices: one doc → validator → next.
- `glossary-management` — Selected — reuse the terms confirmed in Task 1. Propose domain terms for this project (e.g. *HQ*, *Business (tenant)*, *bs-prefix module*).
- `solid` — Selected (reference) — code-smell catalogue.
- `superpowers:verification-before-completion` — Selected.

### Documentation Reviewed

Versions (from `BugTracker/pom.xml`):
- Spring Boot **2.7.0** (L5-10).
- `java.version` **11** (L17), but `maven-compiler-plugin` source/target **17** (L183-190). The code uses Java 17 features.
- Spring Security **5.7.x** and Hibernate **5.6.x** (javax.persistence), both managed by Boot 2.7.
- JJWT **0.11.2**.
- `com.querydsl` **5.0.x** (managed), with `apt-maven-plugin` 1.1.3.
- `spring-boot-starter-batch` **2.7.5**, a version override.
- MySQL connector.

| Technology | Context7 ID to use | Notes |
|---|---|---|
| Spring Boot 2.7 | `/spring-projects/spring-boot/v2.7.18` | Closest indexed 2.7.x. State "2.7.18 docs used for a 2.7.0 project". |
| Spring Security 5.7 | *not indexed in Context7* | **Primary exact-version source:** `javap` on `~/.m2/repository/org/springframework/security/spring-security-core/5.7.1/spring-security-core-5.7.1.jar` (and the matching `spring-security-config` jar) for API shape and deprecations. For behavior and prose, use the Boot 2.7.18 docs, or run `ctx7 docs /spring-projects/spring-security "<question>" --research` and label it "not version-pinned". <!-- REVIEW-FIX: exact-version source available locally --> |
| JJWT 0.11.x | `/jwtk/jjwt` | 0.11 API (`Jwts.parserBuilder()`) differs from 0.12 (`Jwts.parser().verifyWith()`). Check which one a recommendation uses. |
| QueryDSL 5 | `/querydsl/querydsl` | The original `com.querydsl` line, not the OpenFeign fork. |
| Boot 3 migration target | `/spring-projects/spring-boot/v3.4.1` | Only for **Version note** fields that recommend a Boot 3 pattern. |

Pre-verified during task review (`javap`, spring-security-core 5.7.1): `UserDetails.isEnabled()` /
`isAccountNonLocked()` / `isAccountNonExpired()` / `isCredentialsNonExpired()` are **abstract**
in 5.7, unlike 6.4 where they are defaults. BugTracker's `SecurityUser` therefore implements them, and
the lead says it hardcodes `true`. Same symptom as backend (flags ignored), but a different mechanism.
Record it with a **Related** link to backend's finding and explain the difference.

**Version-aware reviewing rule (specific to this task):** each finding must separate two things:
- **defects**: wrong even on Boot 2.7, such as no-op update, privilege escalation or thread-unsafe singleton;
- **era gaps**: acceptable in 2022 on 2.7, but not what a new project should do today, such as `@EnableGlobalMethodSecurity`, the javax namespace, and no `ProblemDetail`.

Era gaps are at most 🟡 Medium unless they carry a security or EOL risk, and they must fill in **Version note**.
Spring Boot 2.7 OSS support has ended, and that is itself one finding in the build review. Verify the date
with Context7 or the Spring support page, and cite it.

### Related Existing Code

The inventory below comes from the Explore pass during task creation. Treat it as a **lead list** and verify every item before writing. Paths are relative to `BugTracker/src/main/java/com/bgsystem/bugtracker/` unless noted.

- `BugTracker/pom.xml` — build, APT plugin, Java version conflict, unused starters.
- `configuration/security/SecurityConfig.java` — `@EnableGlobalMethodSecurity(prePostEnabled=true)` (L25); `anyRequest().authenticated()` (L42); httpBasic; filters instantiated with `new` (L39-40).
- `configuration/security/SecurityConstant.java` — JWT key constant (secret; redact).
- `configuration/filter/JWTTokenGeneratorFilter.java` / `JWTTokenValidatorFilter.java` — Basic → JWT issuance on `/login`, validation on every other request.
- `shared/controller/DefaultController.java`, `shared/service/DefaultService.java`, `DefaultServiceImplements.java`, `shared/mapper/DefaultMapper.java`, `shared/repository/DefaultRepository.java` — generic CRUD stack.
- `shared/models/listRequest/CommonPathExpression.java` — reflective QueryDSL filter base. It is an abstract `@Service` with mutable `filters` state (L13-26, verified).
- `shared/models/pageableRequest/PageableRequest.java`, `SortInfo.java`, `listRequest/FilterRequest.java`, `FilterOperator.java` — request contract.
- `shared/models/user/` — `User` (JOINED root), `UserForm` (accepts `roles`, L32, verified), `LoginController`.
- `shared/models/securityUser/` — `SecurityUser`, `SecurityUserServiceImplements`.
- `shared/tools/firstInstallCheck.java` — seeder; `shared/tools/MentionGenerator.java`.
- `shared/service/EntityFactory.java` — string-switch over 26 repositories (reportedly unused).
- `models/HQ/{admin,client,employee,invoice,mainHQ,plan}/` — operator side.
- `models/client/business/BusinessEntity.java` — tenant root with 14 EAGER `@OneToMany` collections (L72-157).
- `models/client/project/bsPrTask/` — representative module for the end-to-end trace (`bsPrTaskServiceImplements.insert` L65-142; `bsPrTaskPredicate` 256 LOC).
- `models/client/project/{bsPrChannel,bsPrComment,bsPrMention}/` — collaboration features.
- `exeptions/` [sic] — 5 checked exceptions, `ErrorResponseBody`, `GlobalExceptionHandler` (handles only `AccessDeniedException`).
- `BugTracker/src/main/resources/application.properties` — the only config (secret; redact).
- `BugTracker/src/test/java/com/bgsystem/bugtracker/BugTrackerApplicationTests.java` — the only test.
- `BugTracker/target/surefire-reports/` — last test run evidence.

Task 1 outputs to read before starting (for format and lineage comparisons):
- [[Docs/Analysis-Doc-Conventions]]
- [[Docs/backend/backend-Index]] and [[Docs/backend/Reviews/00-Review-Summary]]

---

## Implementation Details

### Approach

The same pipeline as Task 1:
- conventions doc → one tracer-bullet doc → validator → the remaining docs one at a time → summary + index → memory update.

BugTracker needs two adaptations:

1. **Pattern-level, not module-level, explanations.** 29 modules × 10 files cannot be documented one by one, and should not be. Explanations describe each pattern once, using `bsPrTask` as the worked example. `03-Package-Structure-and-Module-Anatomy.md` adds a **module catalogue table** with one row per module: path, route, entity, which base methods it overrides, and any extra endpoints. That table is the only place every module appears.
2. **Domain first.** Unlike `backend/` (two user types), BugTracker's value is its domain model. It gets a dedicated domain doc with a Mermaid ER diagram of the HQ side and the tenant side, plus a separate persistence doc.

### Target document set

`documentation/Docs/BugTracker/`:

```
BugTracker-Index.md
Explanations/
  01-Overview-and-Design-Philosophy.md     ← two-sided SaaS, lineage (ancestor of wpmanager/backend stack), stack
  02-Build-Tooling-and-Dependencies.md     ← pom, APT (apt-maven-plugin → target/generated-sources/java), Java 11 vs 17, wrapper
  03-Package-Structure-and-Module-Anatomy.md ← package tree, 10-file module incl. XPredicate, bs-prefix naming, module catalogue
  04-Generic-CRUD-Framework.md             ← Default* stack, type params, 8 inherited endpoints, override points, updateListFields hook
  05-DTO-Tiers-and-Mapping.md              ← Form/DTO/MiniDTO/ListDTO, hand-written mappers, MiniDTO embedding, @Lazy cycles
  06-Dynamic-Filtering-and-Pagination.md   ← PageableRequest/FilterRequest JSON contract, operators, dd/MM/yyyy dates, CommonPathExpression + XPredicate
  07-Domain-Model.md                       ← HQ vs tenant, Business aggregate, User JOINED hierarchy, self-referencing trees, ER diagram
  08-Persistence-and-Transactions.md       ← repositories, ID strategies, fetch types, denormalized counters, @Transactional, ddl-auto, MySQL
  09-Service-Layer-Patterns.md             ← insert workflow (validate → resolve refs → link both sides → save parents), uniqueness, priority ordering
  10-Authentication-and-Authorization.md   ← Basic login → JWT generator filter → validator filter, SecurityUser, roles as strings, CORS
  11-Collaboration-Features.md             ← channels, comments, @mentions (MentionGenerator), channel↔task/member M:N
  12-Error-Handling.md                     ← checked exceptions, AccessDenied-only handler, ErrorResponseBody, what actually reaches clients
  13-Configuration-and-Bootstrap.md        ← application.properties, firstInstallCheck seeding sequence, no profiles
  14-Recipe-Build-a-Project-This-Way.md    ← new project + new tenant-scoped module "the BugTracker way"
Reviews/
  00-Review-Summary.md
  01-Security-and-Multi-Tenancy-Review.md
  02-CRUD-Framework-and-API-Design-Review.md
  03-Domain-Model-and-Persistence-Review.md
  04-Performance-and-Fetching-Review.md
  05-Filter-Engine-Review.md
  06-Service-Layer-Correctness-Review.md
  07-Error-Handling-and-Validation-Review.md
  08-Build-Dependencies-and-Configuration-Review.md
  09-Testing-Review.md
  10-Code-Hygiene-Review.md
```

**Testing explanation is intentionally skipped.** One `contextLoads` test is not a strategy. Record the
skip and the reason in the index. Testing is covered by `09-Testing-Review.md`.

### Files to Create/Modify

- [x] `documentation/Docs/BugTracker/BugTracker-Index.md`
- [x] `documentation/Docs/BugTracker/Explanations/01…14-*.md` — 14 explanation docs
- [x] `documentation/Docs/BugTracker/Reviews/00…10-*.md` — 11 review docs
- [x] `documentation/Docs/Analysis-Doc-Conventions.md` — only if a rule gap is found. **N/A:** no rule gap found; conventions unchanged.
- [x] `documentation/Features/to-do/Spring-Boot-Skill-Creation.md` — tick "Analyze `BugTracker/`", link this task and the index
- [x] `documentation/Memory/context.md`, `progress.md`, `tech.md`, `known-issues.md` — end-of-task update

---

## Step-by-Step Implementation

### Step 0: Preconditions and snapshot

**Goal:** Confirm Task 1 outputs exist, and guard the read-only constraint.
**Dependencies:** Task 1 complete

- [x] `python3 -m unittest discover -s scripts/tests -v` → passes.
- [x] Read `documentation/Docs/Analysis-Doc-Conventions.md` in full, including its Changelog.
- [x] Snapshot:

```bash
cd /home/jlievano/Dropbox/CodeProjects/spring-boot-backend-skill
find BugTracker -type f -not -path 'BugTracker/target/*' -not -path 'BugTracker/.idea/*' -print0 \
  | sort -z | xargs -0 sha256sum > scripts/.snapshots/bugtracker.sha256
```

#### Edge Cases
1. **Case:** Task 1 is incomplete (conventions or validator missing) — **stop**. Report to the user. Do not recreate Task 1's outputs inside this task.
2. **Case:** `BugTracker` is a gitlink (submodule without `.gitmodules`), so `git status` may not report edits inside it. The checksum snapshot is the guard.

---

### Step 1: Tracer bullet — `01-Overview-and-Design-Philosophy.md` + index skeleton

**Goal:** Validate the pipeline on this project before scaling out.
**Dependencies:** Step 0

- [x] Create `BugTracker-Index.md` with planned docs as plain text, plus the "Testing explanation skipped" note.
- [x] Write `01-Overview-and-Design-Philosophy.md`:
  - the two-sided SaaS domain in plain language;
  - a Mermaid context diagram: HQ operator, Client (customer), Business (tenant), and the users inside a Business;
  - the stack table cited to `pom.xml`;
  - the design philosophy: maximal convention (every entity gets the same 10-file module), generic CRUD with override hooks, rich filtering for grid UIs, and denormalized counters for list views;
  - a **Lineage** paragraph, as the conventions require. Verify it by comparing `shared/controller/DefaultController.java` with `backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultController.java` and with wpmanager's equivalent. State what is inherited, and cite both sides.
- [x] `python3 scripts/validate-analysis-docs.py BugTracker` → exit 0.

---

### Step 2: Explanation docs 02–13

**Goal:** Explain each pattern once, precisely enough to reproduce it.
**Dependencies:** Step 1

Write each doc, then validate it, before starting the next. Minimum content per doc, beyond the template:

- **02 Build, Tooling and Dependencies:**
  - each dependency, marked used or unused after grepping;
  - the Java 11 property vs compiler 17 conflict, with evidence of Java 17 features (cite one switch expression and one `Stream.toList()`);
  - the QueryDSL APT plugin and where the Q-classes land;
  - `spring-boot-starter-batch:2.7.5` overriding the BOM;
  - Maven wrapper 3.8.4.
- **03 Package Structure and Module Anatomy:**
  - the full package tree with counts;
  - the 10 files and the role of each;
  - the naming rules (`bs` / `bsPr` prefixes, lowercase class names as fact);
  - the **module catalogue table** covering all 29 modules. Build it from grep, not by hand: routes from `@RequestMapping`, overrides from `@Override` in each `*ServiceImplements`;
  - orphans: `bsFile` (3 files), the empty `geo/state`, and `models/Test`.
- **04 Generic CRUD Framework:**
  - a Mermaid class diagram of the Default* stack with its type parameters;
  - the table of inherited endpoints (`GET /{id}`, `GET ""`, `POST`, `PUT /{id}`, `DELETE /{id}`, `POST /list`, `POST /page`, `POST /page-list-view`);
  - the `updateListFields` hook and when it runs;
  - the class-level `@Transactional`;
  - the fact that base `update` does not apply the form. Link the finding.
- **05 DTO Tiers and Mapping:**
  - each tier's purpose, with one real example of each from `bsPrTask`;
  - how Forms carry associations as `Long` ids and mappers resolve them;
  - MiniDTO embedding;
  - `@Lazy` injection to break mapper cycles, with a Mermaid graph of one real cycle;
  - `@Service` on mappers.
- **06 Dynamic Filtering and Pagination:**
  - the full JSON example for `POST /bs_pr_task/page`;
  - operators (`: = != > >= < <=`);
  - date format `dd/MM/yyyy`;
  - OR within a field and AND across fields;
  - how `CommonPathExpression` infers types by reflection;
  - how an `XPredicate` adds association paths, with an excerpt from `bsPrTaskPredicate`;
  - a sequence diagram from controller to repository.
- **07 Domain Model:**
  - a Mermaid ER diagram, split into HQ and tenant if one diagram gets too large;
  - the `User` JOINED hierarchy (6 subclasses);
  - the Business aggregate and its 14 collections;
  - self-referencing trees (docs categories, KB categories);
  - the M:N relations (channel↔member, channel↔task, taskCategory↔type);
  - the rule that tenant-configurable status/priority/type are entities, not enums.
- **08 Persistence and Transactions:**
  - the repository conventions (derived queries only, no `@Query`; `@Service` on repositories as fact);
  - the ID strategies (IDENTITY on HQ, SEQUENCE elsewhere; subclass `@Id` redeclaration as fact);
  - fetch types;
  - the denormalized `xxxCount` columns and their recompute path;
  - transaction scope, and how checked exceptions interact with rollback;
  - `ddl-auto=update` and `open-in-view=false`.
- **09 Service Layer Patterns:**
  - the canonical insert workflow as a Mermaid flowchart, from `bsPrTaskServiceImplements.insert` L65-142;
  - uniqueness checks;
  - the priority reorder algorithm (`bsPriorityServiceImplements`);
  - the update/delete overrides catalogue;
  - `EntityFactory`: state whether it is used, based on grep.
- **10 Authentication and Authorization:**
  - Mermaid sequences for (a) `GET /login` with Basic auth → JWT in the response header, and (b) an authenticated request through the validator filter;
  - a claims table (issuer, lifetime; key location only);
  - `SecurityUser` / `SecurityUserServiceImplements`;
  - roles as raw strings, and where they are assigned;
  - the method-security annotation and the one service that uses it;
  - CORS (`localhost:4200`);
  - filter registration: bean plus `new`. State the observed facts only.
- **11 Collaboration Features:**
  - the channel/comment/mention model;
  - `MentionGenerator` parsing of `@username`;
  - comment paging via `GET /bs_pr_comment/pageable/{channel}/{page}/{size}`;
  - channel↔task linkage.
- **12 Error Handling:**
  - the exception inventory;
  - that the handler covers only `AccessDeniedException`;
  - the `ErrorResponseBody` shape;
  - a table of what a client actually receives for each domain exception. Derive it from Boot 2.7 default error handling and verify with Context7 `/spring-projects/spring-boot/v2.7.18`.
- **13 Configuration and Bootstrap:**
  - property keys and their purpose (values `<redacted>` where secret);
  - no profiles;
  - the `firstInstallCheck` seeding sequence as a Mermaid flowchart: MainHQ → admin → plan → client → business → default settings.

- [x] 02 through 13 written, and each validated before starting the next

#### Edge Cases
1. **Case:** the module catalogue grep misses a route without a leading slash (`"bs_invoice"`) — normalize it in the table and note the inconsistency as fact.
2. **Case:** the inventory says "unused" but grep finds a use (e.g. `EntityFactory`) — the code wins. Record the correction.

---

### Step 3: Review docs 01–10

**Goal:** Findings that are verifiable, prioritized, and version-aware.
**Dependencies:** the matching explanation from Step 2

For each finding:
1. Re-read the cited code.
2. Classify it as a defect or an era gap (rule above).
3. Assign severity.
4. Name the principle in `solid-deep-design` terms.
5. Write the recommendation. Verify any API in it at the right version.
6. Fill in **Version note** when the fix needs Boot 3.
7. Fill in **Related**, linking backend findings with the same root cause (for example the no-op `update` and the ignored account flags).

Leads to verify and classify:

- **01 Security and Multi-Tenancy:**
  - hardcoded secrets (locations only);
  - privilege escalation: `POST /user` accepts `roles` (`UserForm` L32, copied by `UserMapper`);
  - authorization is authentication-only, with one `@PreAuthorize` service;
  - **no tenant isolation**: nothing checks that the Business in a request belongs to the principal;
  - comment and channel authors are taken from the request body, not the principal;
  - Spring Data REST auto-export (*Needs runtime verification*);
  - JWT filter defects: NPE when the header is missing, catch-all rethrow, null-deref order in the generator, possible double registration, ~10-day lifetime with no revocation;
  - the `UserDetailsService` bug (`NoSuchElementException` instead of `UsernameNotFoundException`);
  - `SecurityUser` hardcodes the account flags to true;
  - `ErrorResponseBody.trace` leaks exception text.
- **02 CRUD Framework and API Design:**
  - no-op base `update` (affects ~20 modules). Build the list from the catalogue;
  - depth diagnostic of the Default* stack: interface cost (6 type parameters, 8 endpoints, a 10-file tax per entity) vs leverage;
  - deletion test for per-module `XPredicate` and mapper classes;
  - route naming inconsistency (snake, camel, missing slash);
  - 200 on insert;
  - no versioning or OpenAPI;
  - `bsPrCommentController` deviates: double injection, and paging via path variables.
- **03 Domain Model and Persistence:**
  - subclass `@Id IDENTITY` redeclared over a JOINED parent with SEQUENCE;
  - mixed ID strategies;
  - `hashCode` = class hash;
  - anemic model;
  - no auditing or `@Version`;
  - `optional=true` with `nullable=false` on `bsPrTaskEntity` L66-68;
  - no migrations;
  - `Role` enum unused while roles are raw strings.
- **04 Performance and Fetching:**
  - 14 EAGER collections on `BusinessEntity`, plus other EAGER relations;
  - N+1 in list views via `updateListFields`;
  - **write-on-read**: counter recomputation on managed entities inside a transaction turns reads into UPDATEs. Verify the mechanism and mark *Needs runtime verification* if it is not observable statically;
  - counter drift, and the copy-paste double `bsPriorityCount`.
- **05 Filter Engine:**
  - thread-unsafe mutable state on a singleton (`CommonPathExpression` L13-26, verified abstract `@Service` with a `filters` field). Confirm how the concrete predicates are scoped;
  - `isABoolean` bug;
  - `getDeclaredField` misses inherited fields;
  - bad date → NPE;
  - `getAllListView` ignores its filter;
  - `PageableRequest` NPE on null sort and `Optional` used as a field;
  - sort properties are not whitelisted. Compare with backend's `EntityQueryProfile` as a **Related** strength.
- **06 Service Layer Correctness:**
  - uniqueness checks not scoped to the tenant (status, type, priority, docsCategory);
  - priority reorder touches every business;
  - broken diff in `bsTypeServiceImplements.update` (iterators not reset);
  - mentions saved twice;
  - `mainHQRepository.findAll().get(0)` in 4 services;
  - eager `orElse(...)`;
  - wrong exception type used for not-found;
  - the "Bug:" println workarounds before delete;
  - task insert ignores `form.invoice`.
- **07 Error Handling and Validation:**
  - checked exceptions with no mapping become 500s;
  - checked exceptions do not trigger rollback under default rules. Verify the rule with Context7, Spring Framework 5.3 `@Transactional` rollback semantics;
  - validation is mostly manual (~14 constraints in total);
  - class-level `@Validated` on Forms;
  - misspelled `exeptions` package and `InvalidInsertDeails`.
- **08 Build, Dependencies and Configuration:**
  - Boot 2.7 end of support. Verify and cite;
  - javax→jakarta migration cost (era gap);
  - Java 11 vs 17 mismatch;
  - unused starters (batch, quartz, mail, web-services, data-jdbc alongside data-jpa);
  - batch version override;
  - H2 unused;
  - no profiles;
  - hardcoded config;
  - no Docker or README.
- **09 Testing:**
  - one test, which needs a live DB;
  - zero coverage of security, services and the filter engine;
  - `spring-security-test` / `spring-batch-test` unused;
  - a prioritized "test first" list: security (privilege escalation, tenant isolation) → filter engine → no-op update.
- **10 Code Hygiene:**
  - `System.out` / `printStackTrace` instead of a logger (16 occurrences, 7 files);
  - dead code (`EntityFactory` if unused, `Role`, `bsFile`, `geo/state`, `testController`, `/login/test`, unused `org.apache.tomcat.jni.Time` import);
  - lowercase class names;
  - repositories annotated `@Service`;
  - mixed field and constructor injection;
  - `@Lazy` in 28 files.

- [x] 01 through 10 written, and each validated before starting the next

#### Edge Cases
1. **Case:** a finding is identical in root cause to one in `backend/` — still record it here with its own `BT-` ID. Its Impact and Evidence are specific to this project. Link the backend ID in **Related**.
2. **Case:** the Context7 Spring Security result is not version-pinned — say so in **Verified against**.
3. **Case:** a claim needs a running MySQL (write-on-read, Data REST) — do not start a DB. Mark it *Needs runtime verification* and write the exact check.

---

### Step 4: Recipe doc — `14-Recipe-Build-a-Project-This-Way.md`

**Goal:** Answer "how do I build a new project the BugTracker way?"
**Dependencies:** Steps 2–3

- [x] A numbered recipe:
  1. pom essentials (only the used dependencies);
  2. package skeleton (HQ side vs tenant side);
  3. recreate the Default* stack and the filter engine;
  4. security wiring;
  5. add one **tenant-scoped** module end to end: all 10 files, a minimal sketch per file in the project's style, including an `XPredicate` and the `updateListFields` hook;
  6. seeding.
- [x] `> ⚠️ Review:` callouts wherever a step copies a flagged pattern (tenant isolation, thread-unsafe predicate, EAGER collections, no-op update).
- [x] Validate.

---

### Step 5: Summary, index, cross-links

- [x] `Reviews/00-Review-Summary.md` must contain:
  - every finding ID, by severity;
  - counts;
  - **defects vs era gaps** split into two tables (a requirement specific to this project);
  - the Strengths to Keep roll-up (e.g. configurable tenant taxonomies, DTO tiers for grid UIs, rich filter contract);
  - Top 5 Recommendations;
  - candidate patterns for the skill.
- [x] Finalize `BugTracker-Index.md`:
  - reading order;
  - all links;
  - the testing skip with its reason;
  - counts.
- [x] Back-fill the "Known Limitations" links in the explanations.
- [x] `python3 scripts/validate-analysis-docs.py BugTracker` → exit 0.

---

### Step 6: Parent, memory bank, glossary

- [x] Parent Feature: tick `Analyze BugTracker/`, and link this task and `[[Docs/BugTracker/BugTracker-Index]]`.
- [x] Memory bank:
  - `context.md` → next is Task 3;
  - `progress.md` → a dated entry;
  - `tech.md` → the BugTracker stack (Boot 2.7.0, javax, Java 17 effective);
  - `known-issues.md` → no Spring Security 5.7 in Context7, and the gitlink reference projects need checksum guards.
- [x] Propose glossary terms (*HQ*, *Business / Tenant*, *Module Catalogue*, *Era Gap*, *Denormalized Counter*). Add them only after the user confirms. — **Done 2026-09-29:** added with the user's approval (HQ, Tenant with synonym Business, Era Gap, Denormalized Counter; Module Catalogue skipped as too narrow).

### Step 7: Verify the read-only constraint

- [x] `sha256sum --quiet -c scripts/.snapshots/bugtracker.sha256` → exit 0.

---

## Design Decisions

**Decision 1:** Explain patterns once, and use a module catalogue instead of per-module docs.
- **Why:** 29 near-identical modules. Per-module docs would be 29 copies of one explanation (zero depth). The catalogue keeps coverage complete at a fraction of the size.
- **Alternatives considered:** one doc per bounded area (HQ, client, project). Rejected because it still repeats the module pattern three times.

**Decision 2:** Separate "defects" from "era gaps".
- **Why:** BugTracker is on Boot 2.7. Treating every pre-Boot-3 idiom as a defect would drown the real bugs, and Task 4 needs to know which problems come from the design and which come from its age.
- **Alternatives considered:** reviewing only against Boot 2.7. Rejected because the skill targets new projects, so today's baseline matters.

**Decision 3:** Skip the Testing explanation and keep the Testing review.
- **Why:** the conventions forbid stub docs, and a single context test has no strategy to explain. The gap itself is the finding.
- **Alternatives considered:** a short testing explanation. Rejected as a stub.

**Decision 4:** Separate Domain Model and Persistence explanations, unlike `backend/`.
- **Why:** BugTracker's 32-entity, two-sided domain is its most transferable asset and deserves its own doc. The persistence mechanics (IDs, fetch, counters) are a separate concern.
- **Alternatives considered:** a merged doc, as in backend. Rejected because it would exceed a readable size.

---

## Testing Considerations

### Automatic Validation

- [x] Run `python3 -m unittest discover -s scripts/tests -v` → passes (validator still healthy).
- [x] Run `python3 scripts/validate-analysis-docs.py BugTracker` → exit 0, no output lines.
- [x] Run `grep -c '^### BT-R' documentation/Docs/BugTracker/Reviews/0[1-9]-*.md documentation/Docs/BugTracker/Reviews/1[0-9]-*.md` → each review has ≥ 1 finding.
- [x] Run `sha256sum --quiet -c scripts/.snapshots/bugtracker.sha256` → exit 0.
- [x] Run `git status --porcelain -- wpmanager` → empty. Run `sha256sum --quiet -c scripts/.snapshots/backend.sha256` if the file still exists → exit 0.
- [x] Run `grep -rniE '(password|secret|key)\s*[=:]\s*[^<[:space:]]' documentation/Docs/BugTracker` → inspect every hit; no real secret values.
- [x] Run `python3 scripts/validate-analysis-docs.py backend` → still exit 0 (cross-links from this task did not break Task 1 docs).

### Manual Validation

- [x] Open `BugTracker-Index.md` in Obsidian and confirm that the links work and the ER/sequence diagrams render.
- [x] Check the module catalogue in `03-…` against 3 random modules in the code.
- [x] Spot-check 5 findings against the cited lines.
- [x] Read `14-Recipe-…` and judge whether a new tenant-scoped module could be built from it.

**Manual validation confirmed by the user on 2026-09-29.**

**Rule:** Run automatic checks when possible. Manual checks are for the user.

---

## Related Code Explanations

- [[Docs/Analysis-Doc-Conventions]] — The format contract (created by Task 1).
- [[Docs/backend/backend-Index]] — Descendant stack, for lineage and **Related** links.
- [[Docs/Skill-Architecture]] — Planned skill phases these findings feed.
- `BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/` — Representative module for traces.

---

## Completion Criteria

- [x] Parent document reviewed and reflected accurately in this task
- [x] Task 1 outputs present and conventions followed (including any Changelog changes)
- [x] Relevant skills reviewed and selected for this task
- [x] Version-matched documentation used; non-pinned sources labeled
- [x] All Explanation docs created (testing skip recorded)
- [x] Module catalogue covers all 29 modules
- [x] All Review docs created; every finding complete, and each classified as defect or era gap
- [x] Review Summary lists every `BT-` finding ID; index links every doc
- [x] `python3 scripts/validate-analysis-docs.py BugTracker` exits 0; backend validation still exits 0
- [x] `BugTracker/` unchanged (checksum); other reference projects unchanged
- [x] No secret values in any doc
- [x] Manual validation steps documented for the user
- [x] Parent Feature updated (Task 2 ticked + linked)
- [x] Memory bank updated; glossary terms proposed to the user

---

## Post-Review Notes

**Status (2026-09-29):** all automatic completion criteria are met; **ready for the user's manual
validation** (the four checks under Testing Considerations → Manual Validation remain unchecked).

- **Output:** [[Docs/BugTracker/BugTracker-Index]] — 14 explanations, 10 reviews + summary, 86 findings
  (5 🔴, 19 🟠, 34 🟡, 28 🟢; 81 defects, 5 era gaps). The step-level checkboxes above were all carried out;
  only the completion criteria, file list and automatic checks were ticked.
- **Validation run:** 44 validator tests OK; `validate-analysis-docs.py BugTracker` and `… backend` exit 0
  with no output; every review has ≥ 4 findings; both checksum snapshots verify; `git status -- wpmanager`
  empty; the secret-pattern grep has no hits, and a programmatic check confirmed none of the literal secret
  values from `application.properties`, `SecurityConstant` or the seeder appear in the BugTracker docs.
- **Conventions:** no rule gap found, so `Analysis-Doc-Conventions.md` and its Changelog are unchanged.
  Defect vs era-gap classification is recorded in the summary's `## Defects vs Era Gaps` section (allowed
  extra section) and, for era gaps, in each finding's **Principle**/**Version note**.
- **Deviations from this plan (code wins):**
  - `BusinessEntity` has **15** EAGER collections, not 14 (the HQ `invoices` set was not counted).
  - `EntityFactory` confirmed unused (26 repositories, no caller).
  - "Mentions saved twice" is a redundant second save of the same instance, not duplicate rows.
  - The `bsType.update` diff is broken as planned, but its final join-table state is still correct; the
    finding is rated 🟡 and explains why.
  - New defects not in the lead list: `equals` casting to `PlanEntity` in `InvoiceEntity`/`EmployeeEntity`
    (BT-R03-01, 🔴), client-supplied `id` turning `POST` into a merge (BT-R02-02), channel insert NPE with
    members (BT-R06-03), task-category delete `ConcurrentModificationException` (BT-R06-04), usernames unique
    only per user kind (BT-R01-06), password-hash probing through the filter engine (BT-R05-07).
  - Spring Security 5.7 behavior was verified with `javap` on the local 5.7.1 jars; Boot 2.7 defaults with
    `javap` on `spring-boot-autoconfigure-2.7.0.jar` (Context7's `v2.7.18` ID returned `main` snippets); Boot
    2.7 OSS end (2023-06-30) from the spring.io support API.
- **Runtime-dependent findings** (marked *Needs runtime verification*, each with the exact check):
  BT-R01-07, BT-R01-09, BT-R03-02, BT-R04-03, BT-R05-08, BT-R06-09. No database was started.
- **Glossary:** terms proposed to the user, not added: *HQ (operator side)*, *Business / Tenant*,
  *Module Catalogue*, *Era Gap*, *Denormalized Counter*, *bs-prefix module*.
