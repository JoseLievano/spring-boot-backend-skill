# Progress

## 2026-10-05 (Task 4 — Guide 06–09)
- **Executed:**
  [[Tasks/current/Spring-Boot-Architecture-Guide-and-Base-Project-step-4-guide-query-persistence-identity-errors]]
  (Step 2.3 of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]). Parent Step 2.3 ticked.
  The four Guide documents are written as drafts and autonomously reviewed; the user's manual validation
  (read/approve the rules, Obsidian rendering) is still open.
- **Four Guide documents written as drafts** (`#draft`) in `documentation/Docs/Guide/`:
  `06-Query-Engine` (16 rules), `07-Domain-Model-and-Persistence` (19),
  `08-Identity-Authentication-and-Authorization` (29), `09-Errors-and-Validation` (14) — **78 rules**
  (141 in documents 01–09) citing 118 distinct reference findings and 11 ADRs (004–013, 018); 24 new
  `not verified` Version Notes (index total 38). `python3 scripts/validate-analysis-docs.py guide`
  exits **0**; `Guide-Index.md` reports `Draft documents: 9`, `Version Notes not verified: 38`, nine
  rows linked as `draft`, 7 still `planned`.
- **All eleven ★ gaps confirmed as proposed (user, Step 1):** H1 `RowScope` gains `none()` and
  `system(actor)` with AND-only composition (one sentence of document 04 reworded); H2 a request beyond
  a bound is **rejected** (400), never clamped; H3 the predicate technology is the Jakarta Persistence
  criteria API through Spring Data specifications (`not verified` note); H4 features refer to a user
  **by id**, never by association (G07-15); H5 the CRUD base stamps actor labels from its actor
  parameter, feature ops stamp via the base type's one stamping operation (G07-06); H6 a disabled user
  is **401** and roles are read per request (G08-10); H7 issuer routes under `/api/v1/auth/…` with an
  added password-change route (G08-14); H8 a closed public-route list in `platform.access`, Platform
  Modules contribute via a `PublicRoutes` port, features add none (G08-26); H9 a seventh exception kind
  `InvalidRequest` (400) (G09-01); H10 problem types `/problems/<name>` in one registry, every problem
  carries a correlation id (G09-02, G09-04); H11 failure order 401 → 400 → entry point → hook → database
  (G09-14). Unstarred H12–H18 landed as written (422 for dangling ids, identifier as last sort key,
  `platform.crud` base type, `findVisible`, unique login name on `LocalCredential`, administrator role
  in typed configuration, no refresh for a disabled user).
- **The five parent inconsistencies stay uncorrected** (user's choice): Security 7.0 vs 7.1, issuer
  routes without the prefix, "only `CurrentUser`" vs "entities reference `User`", `mustChangePassword`
  with no change route, "non-UUID literal" vs numeric example ids. Reported for the user to fix later.
- **No technology stack was supplied** to the executor; the Task is docs-only (four Markdown documents
  plus the validator's CLI) and proceeded as such (user's choice).
- **Link pass:** the 17 inline-code names of documents 06–09 in documents 02–05 became wiki links
  (`link_pass.py`, 18 replacements); document 04's Row Scope sentence now names all four forms. Two
  lines were re-wrapped by hand. 7 names of documents 10–15 remain inline for Task 5.
- **Glossary (Step 8):** *System Actor*, *Actor Label*, *Identity Mode*, *User Provisioning*,
  *Domain Exception* and *Problem Type* added (all six accepted); *Row Scope* updated to
  "(ownedBy, all, none, system)".
- **Verified:** all Automatic Validation checks pass — `guide` exits 0; rule counts 16/19/29/14;
  `Draft documents: 9`, `Version Notes not verified: 38`; `rule_evidence.py` reports
  `141 rules; rule texts that contain '@': 0` and 0 unresolved IDs; no inline-code name of a written
  document left; no link to a planned document; no Feature/Task/Bug link in 06–09; no secret-looking
  value in `Docs/Guide`; 104 validator tests pass; three project targets exit 0; frozen checksums
  (validator, tests, conventions, doc 01, ADR-001…018) and the three reference-project snapshots
  unchanged. The four documents were extracted byte-exact from the Task text (diff = 0).
- **Traceability:** 61 of the 78 in-scope findings have a rule after documents 01–09 (35 new in this
  Task); 17 remain for documents 10–14 (lists in the Task's Step 7).

## 2026-10-05 (Task 4 document created and reviewed)
- **Task document written:**
  [[Tasks/current/Spring-Boot-Architecture-Guide-and-Base-Project-step-4-guide-query-persistence-identity-errors]]
  for Task 4 (Step 2.3) of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]; linked from
  the parent's Task Breakdown. **Not executed yet** — `Docs/Guide/` still holds documents 01–05 only.
- **Content:** the full text of Guide documents 06–09 as drafts — 78 rules (16, 19, 29, 14) citing 118
  distinct reference findings (81 not cited by 01–05) and 11 ADRs; 24 new `not verified` Version Notes
  (index total 38). A `link_pass.py` turns the 17 inline-code names of documents 06–09 in 02–05 into
  links and adds the `none()` form to document 04's Row Scope sentence.
- **Gaps the Task closes (the user confirms H1–H11 at execution):** `RowScope` gains `none()` and AND-only
  composition; a request beyond a list bound is rejected, not clamped; the predicate technology is the
  Jakarta Persistence criteria API through Spring Data specifications (`not verified`); features refer to
  a user by id, never by association; the CRUD base stamps actor labels (Spring Data's `AuditorAware`
  takes no parameter — an ambient read); a disabled user is 401 and roles are read per request; issuer
  routes sit under `/api/v1/auth` with an added password-change route; the public-route list is closed
  and fed by a `PublicRoutes` port (no anonymous actor); a seventh exception kind `InvalidRequest` (400);
  problem types `/problems/<name>` in one registry; failure order 401 → 400 → entry-point checks → hook →
  database.
- **Five inconsistencies in the parent's section 8** are listed in the Task (Security 7.0 vs 7.1; issuer
  routes without the prefix; "only `CurrentUser`" vs "entities reference `User`"; `mustChangePassword`
  with no change route; "non-UUID literal" vs numeric example ids). The parent is not edited for them.
- **Verified while writing (2026-10-04/05):** the four documents pass the real `guide` target in a
  scratch copy, also when rebuilt from the Task text alone; the red state after each step was measured
  (links to later documents only); `rule_evidence.py` reports `141 rules; rule texts that contain '@': 0`.
  Traceability: 61 of the 78 in-scope findings have a rule after documents 01–09 (35 new); 17 remain for
  documents 10–14.
- **Found:** the Spring Boot application-properties page is too long for one fetch — every property name
  in the documents is in a `not verified` note.
- Reviewed with `task-reviewer`: 6 findings patched (0 critical, 0 high, 4 moderate, 2 low).

## 2026-10-04 (Task 3 — Guide 01–05)
- **Executed and closed:** [[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-3-guide-structure-crud-api]]
  (Step 2.2 of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]). Parent Step 2.2 ticked.
  Validated by the user (2026-10-04): the rules of the five documents are approved as written and the
  documents render correctly in Obsidian; the Task was moved to `Tasks/done/` at the user's request.
- **Five Guide documents written as drafts** (`#draft`) in `documentation/Docs/Guide/`: `01-Principles-and-Baseline`
  (9 rules), `02-Project-Layout-and-Module-Boundaries` (11), `03-Feature-Module-Anatomy` (10),
  `04-CRUD-Base-and-Service-Hooks` (20), `05-API-Contract` (13) — **63 rules** citing 87 findings and 13
  ADRs, 14 `not verified` Version Notes. `python3 scripts/validate-analysis-docs.py guide` exits **0**;
  `Guide-Index.md` reports `Draft documents: 5`, `Version Notes not verified: 14`, five rows linked as
  `draft`, 11 still `planned`.
- **Order of checks — user decision: Option A + ADR-018.** At a row entry point: authenticate → scoped
  load (404) → policy check with the loaded row (403) → precondition (428/412) → mutate, at every row
  entry point including download. **ADR-018** written and `Accepted` (replaces only the order sentence of
  ADR-005 decision 8; ADR-005 and ADR-013 unchanged). Parent section 6's check-order sentence, the ADR
  table (row 018) and "ADRs 001–018" updated.
- **All seven ★ gaps confirmed as proposed:** G2 one home per rule; G3 `platform` dependency table
  (`config` ← `errors` ← `identity` ← `access` ← `query` ← `crud`, `RowScope` in `platform.access`);
  G4 seven `Action` values (get, list, search, create, update, delete, download); G6 `PUT` is full
  replacement; G7 zero-based `page`; G10 `G01-09` applies to every project; G12 the CRUD base is the
  named exception to "one to four entry points".
- **Glossary:** *Entry Point*, *Service Hook*, *Concurrency Token*, *Page Response* and *Depth Criterion*
  added (all five accepted by the user).
- **Verified:** expected red state after each step matched exactly (forward references only); `rule_evidence.py`
  reports `63 rules; rule texts that contain '@': 0` and 0 unresolved IDs; 104 validator tests pass; the
  three project targets exit 0; frozen checksums (validator, tests, conventions, ADR-001…017) and the
  three reference-project snapshots unchanged. 24 inline-code names of planned documents remain for the
  Tasks 4–5 link pass.

## 2026-10-04 (Task 3 document created and reviewed)
- **Task document written:**
  [[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-3-guide-structure-crud-api]] for
  Task 3 (Step 2.2) of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]; linked from the
  parent's Task Breakdown. **Not executed yet** — `Docs/Guide/` still holds only the conventions and the
  index, and no ADR was added.
- **Content:** the full text of Guide documents 01–05 as drafts — 63 rules (9, 11, 10, 20, 13) citing 87
  distinct reference findings and 13 ADRs; 14 `not verified` Version Notes. One home per rule; planned
  documents are named in inline code (the validator rejects a link or a Rule ID that does not resolve).
- **Conflict found between two accepted ADRs (the user decides in the Task's Step 1):** ADR-005 decision 8
  orders an entry point as policy check (403) → scoped load (404); ADR-013 decision 7 orders the download
  entry point as scoped load (404) → policy check. With ADR-005 decisions 5 and 6 (the policy receives
  the entity; a hidden row reads as 404, not 403) only the second order is coherent. Recommended: scoped
  load first everywhere, recorded as a narrow **ADR-018** that replaces the order sentence only. The Task
  also carries a tested script for the other option.
- **Second parent inconsistency:** "a small interface (1–4 entry points)" for every Platform Module vs
  six entry points on the CRUD base; rule `G01-02` names the CRUD base as the exception.
- **Gaps closed by the Task (for the user to confirm at execution):** a dependency direction inside
  `platform` (`config` ← `errors` ← `identity` ← `access` ← `query` ← `crud`, `RowScope` in
  `platform.access`); seven `Action` values (the six entry points plus download); only `applyRelations`
  receives the actor; `PUT` is full replacement; zero-based `page`; rule IDs cited in the type-level
  documentation of Platform Modules in every project.
- **Verified while writing (2026-10-04):** both options were built in the scratchpad and pass the real
  `guide` target, also when rebuilt from the Task text alone. Spring Boot **4.1.x** (4.1.2) manages
  Spring Framework 7.0.9, **Spring Security 7.1.1**, Spring Data 4.1.1, Hibernate 7.4.5, Jackson 3.1.5,
  JUnit Jupiter 6.0.3 and Testcontainers 2.0.5; MapStruct (1.6.3), ArchUnit (1.5.1) and springdoc-openapi
  (3.1.1) are not managed. In Framework 7.0.9 `If-Match` on an unsafe method is compared strongly and
  yields 412, but an absent `If-Match` is not rejected — the 428 must come from the CRUD base.
- Reviewed with `task-reviewer`: 12 findings patched (0 critical, 1 high, 6 moderate, 5 low).

## 2026-10-04 (Task 2 — Guide format and validator)
- **Executed and closed:** [[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-2-guide-format-validator]]
  (Step 2.1 of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]). Parent Step 2.1 ticked.
  The user confirmed the Manual Validation items (written format approved; both Guide files render correctly
  in Obsidian) and asked for the Task to be moved to `Tasks/done/`.
- **Two Guide files created:** `documentation/Docs/Guide/Guide-Conventions.md` (the format contract: layout,
  tags and the `#draft` marker, document template, rule blocks `G<NN>-<MM>`, the four ID families, Version
  Note grammar, index, contract reviews, traceability matrix) and `Guide-Index.md` (front page with the
  sixteen planned documents, the two Status counts and the Changelog).
- **`guide` target added** to `scripts/validate-analysis-docs.py` (same CLI; the three project targets are
  unchanged). It checks file kinds and names, one document per number, title and `#doc #guide` tags, the six
  required headings in order, no wiki link into `Features/`, `Tasks/` or `Bugs/`, rule blocks (ID form,
  document number, uniqueness, the four fields, the withdrawn form), evidence (a reference Finding ID or an
  ADR; no Superseded/Deprecated ADR), that every Finding ID / `ADR-NNN` / Rule ID resolves (prose and code),
  Version Note markers and their three kinds of evidence, index coverage and the two status counts, contract
  reviews (`GC-R<NN>-<MM>` with summary coverage), and the traceability scope (🔴 + 🟠 + every
  security-review finding).
- **Tests:** `scripts/tests/test_validate_guide_docs.py` — 60 tests in nine classes (the 59 planned plus one
  added at review: a contract-review finding alone is not rule evidence). Full suite 104 tests, all passing;
  the 44 existing tests are untouched. 17 of 17 seeded defects killed. Probe on the real evidence base:
  `scope errors: 77`, clean without the probe.
- **User's format decisions (confirmed before any code):** plain IDs are valid citations (wiki links
  optional, resolved everywhere including code); `**Differs from references:**` is required with an explicit
  `None — <what is kept>`; Version Notes are `- ` items starting `**verified on <major>.<minor>.x**` or
  `**not verified**`, no text outside an entry, evidence = exact test path with `/src/test/` under
  `base-project/` or `documentation/Docs/Validation/`, a reference Finding ID, or a URL with a version
  segment (never fetched); `#draft` is the draft marker and the index reports `Draft documents` and
  `Version Notes not verified`, both recomputed by the validator.
- **Glossary:** *Contract Review*, *Traceability Matrix* and *Draft Marker* added (all three accepted by the
  user).
- **Memory bank updated:** `tech` (the `guide` command), `architecture` (source-map rows for `Docs/Guide/`
  and the validator), `known-issues` (docs.spring.io versioned-URL redirect; no Guide links to moving
  documents), `context`, this entry. `brief.md` untouched.

## 2026-10-04 (Task 2 document created and reviewed)
- **Task document written:**
  [[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-2-guide-format-validator]] for Task 2
  (Step 2.1) of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]; linked from the parent's
  Task Breakdown. **Not executed yet** — `scripts/` and `Docs/Guide/` are unchanged.
- **Design in the Task:** the `guide` target is added to `scripts/validate-analysis-docs.py` (same CLI, no
  second script); tests in a new `scripts/tests/test_validate_guide_docs.py`. It checks file kinds, title
  and tags, the six headings, rule blocks (`G<NN>-<MM>`, four fields, withdrawn form), evidence (a reference
  Finding ID or an ADR; no Superseded/Deprecated ADR), that every Finding ID / `ADR-NNN` / Rule ID resolves
  (also inside code), Version Note markers and evidence, index coverage and two status counts, contract
  reviews (`GC-R<NN>-<MM>`), and the traceability scope.
- **Gaps in the parent closed by the Task** (for the user to confirm at execution): plain IDs are valid
  citations (wiki links optional); `**Differs from references:**` is required with an explicit `None —`;
  Version Notes are list items that start with `**verified on <major>.<minor>.x**` or `**not verified**`;
  `#draft` is the draft marker and the index reports the draft and `not verified` counts; a Rule ID is
  withdrawn and replaced, never renumbered; Guide documents never link a Feature, Task or Bug (they move).
- **Verified while writing:** the whole design was prototyped in the scratchpad — 103 tests pass (44
  existing + 59 new), the new tests fail against the current validator, 17 of 17 seeded defects are caught,
  each of the seven slices ends green in the planned order, and the validator rebuilt from the Task text
  alone passes the same 103 tests. Both Guide files validate against a copy of the real documentation.
  The traceability scope on the real findings is **78** (18 🔴 + 45 🟠 + 40 security-review findings,
  minus the overlap).
- **Found:** `docs.spring.io` redirects the versioned URL of the *current* line to the unversioned address
  (seen for Spring Boot 4.1 and Spring Framework 7.0; Spring Security 7.0 is served as written) — a
  Version Note must cite the versioned form. `rg` is a shell function of the agent harness, not a binary;
  Task commands use `grep`.
- Reviewed with `task-reviewer`: 6 findings patched (0 critical, 0 high, 2 moderate, 4 low).

## 2026-10-04 (Task 1 — decisions, ADRs, glossary)
- **Task 1 executed:**
  [[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-1-decisions-adrs-glossary]] of
  [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]. ADR system initialised
  (`doc-config.json` gains `adrs` + four statuses; `documentation/ADRs/ADR-index.md` created).
- **ADRs written and accepted:** `ADR-001`…`ADR-016` are `Accepted`; `ADR-017` (D18, Skill architecture)
  is written and stays `Proposed` until Task 6 completes it. Three-digit IDs per the documentation skill
  (`ADR-[NNN]-[short-title].md`), confirmed by the user. Review decisions F1–F17 are folded into the ADR of
  the decision each one changed. `check_adrs.py` reports `0 error(s); mode=final`.
- **Two decisions confirmed at acceptance:** ADR 009 — a failed request validation is **400** ("invalid by
  itself"); 422 stays "valid but refused against state or content" (business rule, fingerprint mismatch,
  digest mismatch), and **422** is confirmed for a business rule violation. ADR 014 — idempotency stays
  **opt-in per endpoint** (US 38); the convention's upload recipe opts in, the guard has no default.
- **Parent corrections P1–P8 applied** to
  [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]: four-digit `ADR-00NN` → `ADR-0NN`
  (11 occurrences + 17 table rows); "ADRs 0001–0018" → "ADRs 001–017"; the Step 1.3 / Step 4.1 / ADR-017
  row texts; the non-existent **`WP-R05-08` → `WP-R05-04`**; the stale "after the Guide and the Base
  Project exist" sentence; the open 400-vs-422 texts; the "`@Idempotent` by default" contradiction; the
  `<F>AccessPolicy>` typo (6 occurrences); ADR table titles synced with the index. Steps 1.1–1.4 ticked.
  [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] left untouched as a closed record.
- **Glossary: 26 → 50 terms in six categories.** Batch A (15 new, the parent's list), Batch B (3 updates:
  *Feature Module*, *Generic CRUD Stack*, *DTO Tiers* now describe the Guide's shape alongside the
  reference shape), Batch C (12 accepted beyond the parent's list: 9 new + 3 updates), Batch D (5
  back-links). New categories *Guide and Convention*, *Platform Modules*, *Validation Loop*. All writes
  through the `glossary` CLI runner (`glossary_terms.py`, scratchpad).
- **Verified at execution:** the Spring Boot generations API re-read 2026-10-04 (4.1.x current GA, OSS
  until 2027-07-31; 4.2.x scheduled 2026-11-30); the method-validation exception is documented as **400
  for input validation errors** (500 for return-value validation) — Spring Framework reference and javadoc,
  2026-10-04. Tooling baseline held: three validators exit 0, 44 unit tests OK, three checksum snapshots
  unchanged.
- Memory bank updated: [[Memory/known-issues]] (the "one project only" rule replaced by the ADR-001
  citation rule; ADR citation and glossary-search notes added), [[Memory/architecture]] (ADRs in the source
  map; the Guide is the source of truth), [[Memory/context]].


## 2026-10-04 (Task 1 document created and reviewed)
- **Task document written:**
  [[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-1-decisions-adrs-glossary]] for Task 1
  (Steps 1.1–1.4) of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]; linked from the
  parent's Task Breakdown. **Not executed yet** — no ADR exists and the glossary is unchanged.
- **Parent inconsistencies found** (the Task corrects them in its Step 7): four-digit ADR IDs vs the
  documentation skill's three-digit `ADR-NNN`; "ADRs 0001–0018" (there are 17); **`WP-R05-08` is cited in
  section 10 but does not exist** (the buffering finding is WP-R05-04); section 14 still says the Skill is
  designed "after the Guide and the Base Project exist" (stale since F8/F10); the open 400-vs-422 validation
  status; "`@Idempotent` by default" vs US 38 opt-in; the stray `>` in `<F>AccessPolicy>`.
- **Design choices in the Task, for the user to confirm at execution:** three-digit ADR IDs; ADR 017
  (D18) opened now as `Proposed` to reserve the number and completed in Step 4.1; F1–F17 folded into the ADR
  of the decision each one changed; ADR 009 recommends 400 for request validation ("400 = invalid by itself,
  422 = valid but refused against state or content"); ADR 014 "opt-in per endpoint, the upload recipe opts
  in"; nine glossary terms proposed beyond the parent's list (Row Scope, Access Policy, …).
- **Verified while writing:** all 75 finding IDs the parent cites resolve except WP-R05-08; the Spring Boot
  generations API (4.1.x current GA, OSS until 2027-07-31); Spring Framework's default 400 for a failed
  `@Valid` request body; `glossary add` creates a new category on first use and `glossary search` matches
  term names only (not synonyms). The Task's ADR checker, glossary runner and parent `sed` edits were
  dry-run on scratch copies.
- Reviewed with `task-reviewer`: 8 findings patched (0 critical, 0 high, 5 moderate, 3 low).

## 2026-10-03 (Findings resolution COMPLETE — F11–F17; all 17 done)
- Resumed [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] at F11 and processed
  F11–F17 to completion. **17 of 17 resolved** (16 Done, F16 Auto-resolved). The parent
  [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] is patched for every accepted decision.
- **Closed out:** the Bug Report moved `Bugs/to-do/` → `Bugs/done/` (all wiki links updated), and Step 1.1
  was applied — [[Memory/brief]] now describes a **project-agnostic convention skill** (rules, module
  contracts, pseudo-code; no code artifacts; no project initialization; not Claude-Code-specific), with the
  "grounded, not invented" constraint relaxed to **"grounded or corrected, always cited"** and the F11
  support-policy baseline added. `auth-server` → `backend` in the reference-project names.
- **F11 (Moderate) → Done.** Option 4 (alternative): the version baseline is a **support policy**, not a pin.
  Verified 2026-10-03: **every Spring Boot 3.x line is OSS-EOL** (3.5 ended 2026-06-30, terminal 3.x minor;
  3.4 ended 2025-12-31); 4.1.x is current GA, 4.2 GA scheduled 2026-11-30. Boot 4 changes the Guide's
  dependencies (Jackson 3 default `tools.jackson.JsonMapper`; Security 7; modularised auto-configuration).
  Floor = "the currently OSS-supported Spring Boot generation" (today 4.x); the Base Project pins one line —
  the current GA at build time, **decided at Task 9**; Version Notes are **evidence-scoped** (`verified on
  <line>` with a resolvable citation, or `not verified — current-docs lookup`), enforced by the `guide`
  validator. US 6/7 and D3 reworded. Java 21+ unchanged (Boot 4's floor is Java 17).
- **F12 (Moderate) → Done.** Option 4 (alternative): **frozen evidence packs** — freeze the exhibits, not the
  specimens. At review time each **cited** file is copied whole and unmodified into
  `Docs/Validation/Run-NN/<Domain>/evidence/<path>` (line numbers 1:1) and the review cites only those;
  `validation-runs/` **stays git-ignored** and is cleaned after the review + evidence are committed. The
  `validation` target **rejects any citation into `validation-runs/`** (fail-closed, fires while the run is
  live) and skips `evidence/` subtrees. Noted: the `CITATION` regex only matches `backend|BugTracker|wpmanager`
  and must be extended either way.
- **F13 (Moderate) → Done.** Option 4 (alternative): **`DownloadTicket` capability tokens** — the
  `UploadCoordinator` twin on the read path. Ordinary entry point → scoped load (404) →
  `policy.check(Action.download)` → HMAC ticket over `storedFileId + expiry` (never exposes `ObjectKey`);
  one redemption controller verifies MAC+TTL, **re-checks existence**, then 302s to a short-lived presigned
  URL (S3 — bandwidth still offloaded) or streams locally. Pinned 404 (invalid/deleted) / 410 (expired);
  centralized `nosniff`+`attachment` header policy; non-transactional (F4); read-only (F5/F9). Honesty
  correction recorded: wpmanager's `FileSigner` signs **file integrity**, not URLs — link signing is a
  designed extension.
- **F14 (Moderate) → Done.** Option 4 (alternative): **three owner-scoped contracts**. (a) Credential failure
  policy = F6's `LocalCredential` (plus the `isAccountNonLocked()` default-`true` trap and a uniform
  bad-vs-locked `ProblemDetail`). (b) CORS = typed config closing **both** halves of BE-R01-10 (origins from
  `@ConfigurationProperties`; the chain consumes the **injected** `CorsConfigurationSource`; fail-fast on
  wildcard+credentials; `local` profile supplies `localhost:3000`). (c) Per-IP throttling = declared
  deployment-layer ownership with **password spraying named as residual risk**. Matrix scope becomes
  mechanical: 🔴/🟠 **plus every 🟡/🟢 from a Security-category review** (BE-R01-10 is tagged `configuration`,
  so a severity rule would miss it). No in-app rate limiter (Spring has none; per-IP counters unsound at scale).
- **F15 (Low) → Done.** Option 4 (alternative): a **rule-coverage ledger** (every applicable `G<NN>-<MM>` is
  `finding <V…ID>` or `checked — no finding` with a frozen-evidence citation; `check_rule_coverage()` in the
  `validation` target; the Exit Gate gains "ledger is complete") **plus a one-time calibration belt in Task 7**
  (blind on `backend/`, must find BE-R01-01/02, BE-R02-01 + the F7 `@PreAuthorize` case as a **lower bound**;
  answer key banned from the context; Task 8 must not start until it passes). The finding's "Task 11" was
  stale under the F8/F10 renumbering. Seeded defects rejected as structurally incompatible (they fail the
  Exit Gate's own 0-🔴/🟠 arithmetic and have no `Root cause` value).
- **F16 (Low) → Auto-resolved** by F1. The Skill is code-free and never copies `base-project/`, so it has no
  handling of the sample feature at all. Parent unchanged.
- **F17 (Low) → Done.** Option 4 (alternative): the **explicit-actor model** — `CurrentUser.system()` is a
  sentinel inside the existing type; **no `runAsSystem`, no ambient switch**; every entry point names its
  actor explicitly (the sibling of F3's "no entry point runs unscoped"); system is an ordinary actor whose
  rules live in `<F>AccessPolicy>`; platform housekeeping confined to `platform.*` tables may not write
  feature rows; audit is a mandatory parameter (`createdBy` = `system` string label, never blank).
  `CurrentUserProvider` confined to the HTTP edge (ArchUnit).
- Pattern across F11–F17: **the alternative subagent's proposal was promoted to first-class in six of seven**
  (all but F16). The recurring move was the project's own grammar — fail-closed by signature, one owner per
  decision, evidence that is checkable rather than assumed.

## 2026-10-03 (Findings resolution — F9 decided; paused)
- **F9 (Moderate) → Done.** [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] finding
  "No concurrency control (lost updates)". User chose Option 4 (alternative, promoted to first-class) — the
  **Conditional-Write Contract**: concurrency token on managed entities, strong `ETag` + required `If-Match`
  on `update`/`delete`, stale → 412, missing → 428, `If-Match: *` unconditional; no body version
  (preserves `<F>Request`); 409 reserved for state conflicts; lock failure → same 412.
  - Option 1 ("body or If-Match", 409) not adopted verbatim: unpinned dual channel, body version contradicts
    `<F>Request>`, 409 violates RFC 9110 on the `If-Match` channel. Option 2 (hidden `@Version`) does not fix
    the two-admin read-modify-write scenario. Option 3 (document last-write-wins) rejected against US 4.
  - Patched [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]: US 17, sections 5/6/9, Guide
    rows 04/05/07/09, Steps 3.4/3.7, Testing Decisions.

## 2026-10-03 (Findings resolution — F8 + F10 decided jointly; paused)
- **F8 + F10 (Moderate) → Done, jointly.** User chose Option A — a single blast-radius reorder.
  Execution order is now **1 → 2 → 4 → 5 → 3 → 6**: the Guide's contract layer leads and is gated by F2's
  blind `GC-R` review; the **Skill is written next**; **run 01** of the Validation Loop is a planned discovery
  run; only then is `base-project/` built per area at the end against converged contracts, finalizing each
  Guide document's prose against the built code. **Step numbers stay stable as identifiers**; the Task
  Breakdown is renumbered 1–13 in execution order (free — no task documents exist yet).
  - **D18 amended:** "Skill rewritten from scratch after the Guide **and Base Project** exist" → "after the
    Guide exists and its contracts pass the `GC-R` gate". Surfaced for explicit user confirmation.
  - F8's premise dissolved with F1 (the Skill never starts from the Base Project), so the Bug Report's
    dismissal of F8 Option 3 as "conflicts with D2" referred to the *original* D2 and is void.
  - F10's premise corrected: F3–F7 were **cross-module contract gaps** (F5 is literally storage × idempotency)
    that per-area vertical slices would have torn apart; F2's `GC-R` review is the instrument that found them
    with no code in existence. Contracts lead, prose follows implementation.
  - Run 01 accounting: discovery run inside D15's 5-run budget; root-cause field `guide`/`skill` only; a
    convention-changing Guide correction in Phase 3 resets the consecutive-pass count.
  - Patched: D18 row, Solution prose + mermaid flowchart, Execution Order note, phase blocks reordered,
    Steps 2.6/3.7/5.2/5.4, sections 13 and 15, US 50, Risk Assessment drift bullet, Task Breakdown.
    Bug Report F8 + F10 Decisions + summary table.
- Four read-only subagents ran (F8's three options + one alternative covering both findings). The alternative
  was judged **materially better** and promoted to first-class; the Bug Report's original pair (F8 O2 + F10 O1)
  was rejected because the two reorders fight each other and would renumber twice.
- Paused after F8/F10. **F9, F11, F12, F13, F14, F15, F16, F17 remain Pending**; F16 is expected to auto-resolve.

## 2026-10-03 (Findings resolution — F6, F7 decided; paused)
- **F7 (Moderate) → Done.** Authorization was split across four mechanisms. User chose the alternative
  (Option 4): **one authorization module per feature** — the CRUD `authorize` hook is removed and folded into
  `<F>AccessPolicy` (`check(action, actor, entityOrNull)` + `rowScope(actor)`). Actor-independent invariants go
  to `beforeDelete`/`validate*`. Deny-by-default is structural at three layers (URL tail; policy denies when no
  rule matches; policy required by constructor). `@PreAuthorize`/`@Secured` banned in `features.*` by an
  ArchUnit member rule; `@EnableMethodSecurity` stays once on the security configuration class as a
  `platform`-only belt. US 13 amended to "hooks (validate, apply relations, beforeDelete) plus one declared
  access policy"; adds a route-coverage test. Rationale: Option 1 left actor-relative authorization in two
  homes ("role/action rules" in the policy AND in the `authorize` hook) — F7's own drift one level down.
  Patched: Feature sections 4, 5, 6, 8 (incl. the mermaid node), US 13, Guide rows 04/08, ADR-0005, Steps
  3.4/3.7, Testing Decisions,
  [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] F7 Decision + summary table.
- **F6 (High) → Done.** Local identity lifecycle undefined. User chose the alternative (Option 4): durable
  `User` + rebindable subject binding + one `UserDirectory` lifecycle contract. `User.externalSubject` always
  equals the token `sub` (local mode `sub` = `User.id`, no `"local:"` prefix — the `iss` claim namespaces).
  Credential/account state split: `LocalCredential` + `RefreshToken` in `platform.identity.local`'s own Flyway
  migrations; `User` keeps id/externalSubject/e-mail/display name/roles mirror/app-level `status`.
  `User.status` enforced on every request and survives migration. `UserDirectory` (`create`, `setStatus`,
  `rebindSubject`) is the sole lifecycle path; `rebindSubject` makes US 28 true at the **data** level (migration
  runbook). One role writer per mode — no both-modes role service. Admin user management lives in the local
  module and dies with it. Patched: Feature sections 4, 8, US 30, Guide row 08, ADR-0006/0007, Steps 3.2/3.3,
  Risk Assessment, Testing Decisions, Bug Report F6 Decision + summary table.
- Four read-only subagents per finding (three listed options + one alternative search). **Both alternatives
  were judged materially better and promoted to first-class.**
- Also fixed two stale items in the Bug Report while editing it: the Summary line ("Every finding's Decision is
  empty") and an accidentally dropped `#### F8` heading (restored).
- Paused after F7. **F8–F17 remain Pending**; F16 is expected to auto-resolve. **F8 and F10 both reorder Tasks
  and should be decided together** to avoid renumbering twice.

## 2026-10-03 (Findings resolution — F5 decided; paused)
- **F5 (High) → Done.** Idempotency fingerprinting conflicted with streaming uploads: the `@Idempotent`
  interceptor hashed the request body, forcing buffering or consuming the only stream. User chose **Option 1
  refined** — the fingerprint is `method + path + canonical non-file fields + a declared content digest`, and no
  code path hashes the request body. The digest's subject is the **stored object's bytes** (file-scoped form
  field for multipart; one pinned header `X-Content-SHA256: sha-256:<hex>` or RFC 9530 `Repr-Digest` for raw
  streams). RFC 9530 `Content-Digest` is explicitly rejected: it hashes the multipart envelope and can never
  equal `StoredFile`'s SHA-256. Fail-closed for file/streaming requests with no digest; `UploadCoordinator.store`
  verifies during its single streaming pass and 422s before finalising; buffered JSON endpoints keep the body
  hash; two 422 `ProblemDetail` types (fingerprint mismatch vs content-digest mismatch); Option 2 (spool-once)
  documented as fallback only, never a per-adapter default, hashing in the spool pass (closes WP-R04-06).
  Patched: Feature sections 10 (`UploadSource` + digest verification) and 11 (fingerprint composition),
  Guide rows 10/11, Steps 3.5/3.6, Testing Decisions,
  [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] F5 Decision + summary table.
- Four read-only solution subagents ran (three listed options + one alternative search). The alternative
  (binder-side fingerprint construction, fail-closed digest, server-computed proof on replay) judged itself
  **not materially better** — the client-declared digest verified while streaming is forced by the constraint
  set — but its file-scoped digest channel and fail-closed rule were absorbed into the decision.
- Option 3 (exclude file content from the fingerprint) was **rejected**: it breaks US 37 and recreates
  WP-R04-02 in mirrored form with no detection path.
- Paused after F5 at the user's request. **F6–F17 remain Pending**; F16 is expected to auto-resolve.
- Noted, not changed (pre-existing, outside F5's scope): section 11 says uploads through `UploadCoordinator`
  are "`@Idempotent` by default" while US 38 says idempotency is opt-in per endpoint; and the Bug Report's
  Summary line still reads "Every finding's Decision is empty" (stale since F1).

## 2026-10-02 (Findings resolution — F4 decided; paused)
- **F4 (High) → Done.** "No transaction open during upload" is not enforced. User chose the alternative
  (Option 4): remove the enabler and enforce the invariant with the framework. The CRUD base now declares
  `@Transactional` **per entry point** (six base methods only) instead of class-level, so feature-added service
  methods are non-transactional by default and multi-write custom methods declare it explicitly — a documented
  departure from the reference projects (US 5). `UploadCoordinator.store` is declared
  `@Transactional(propagation = NEVER)` (framework-native fail-fast before any storage I/O). Added a
  transaction-boundary contract test and named the invariant in Step 3.7's contract-conformance checklist.
  Patched: Feature sections 6 and 10, Guide rows 04/10/13, Steps 3.5/3.7, Testing Decisions,
  [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] F4 Decision + summary table.
- Analysis noted that the Bug Report's Option 3 (ArchUnit "classes calling UploadCoordinator must not be
  `@Transactional`") cannot work as worded: `isAnnotatedWith(Transactional.class)` is blind to the class-level
  `@Transactional` inherited from `CrudService` (spring-tx 6.2.1 `@Transactional` is `@Inherited`;
  TNG/ArchUnit#277), so it would miss the finding's own `NoteService` example. Recorded in the F4 Decision.
- Paused after F4 at the user's request. **F5–F17 remain Pending**; F16 is expected to auto-resolve.

## 2026-09-30 (Findings resolution — F1, F2, F3 decided; paused)
- **D2 revised by the user.** The Skill is a project-agnostic **code-free convention** (rules + module
  contracts + pseudo-code; no Spring app, no code artifacts, project init is not its job) and is not
  Claude-Code-specific. New projects implement the contracts fresh and look up current framework docs for API
  syntax at execution time. `base-project/` stays as the workspace **reference implementation** and Validation
  Loop harness — never shipped, never a copy source. Rationale: a version-pinned Base Project would make the
  Skill teach deprecated APIs. Patched across the Feature (D2, description, user story 43, flowchart,
  sections 13–14, ADR-0002/0017, Steps 1.1/4.1, Risk Assessment).
- **F1 (Critical) → Done.** Dissolved: the Skill has no dependency on `base-project/`, so `brief.md:23` and
  `known-issues.md:50` are satisfied as written. `brief.md` still needs a user edit to drop "scaffolds" and
  "Claude Code skill".
- **F2 (High) → Done.** Blind **contract design review** at end of Phase 2 (IDs `GC-R<NN>-<MM>`, gate 0 🔴 /
  0 🟠 before Phase 3) rather than a code review of `base-project/`; Step 3.7 becomes a contract-conformance
  checklist.
- **F3 (High) → Done.** Mandatory technology-neutral **`RowScope`** required by `ListQuery.run` and every CRUD
  entry point (fail-closed by signature); produced by `<F>AccessPolicy` via `ownedBy()/all()/system()`;
  ANDed before paging; invisible rows → 404. `<F>AccessPolicy` added to the Feature Module anatomy; auth
  matrix gains "list returns only visible rows". Named D8 tenancy extension point.
- Paused after F3 at the user's request. **F4–F17 remain Pending**; F16 is expected to auto-resolve.

## 2026-09-30 (New action plan)
- **Feature written:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] — ADRs → Guide
  (`Docs/Guide/`, rule IDs) → Base Project (platform modules) → rewritten Skill → Validation Loop (two user
  domains, blind review, strict exit gate). Decisions D1–D18 from a user interview. Auth seam verified against the
  Spring Security 7.0 resource-server docs; MapStruct `@MappingTarget` and `ReportingPolicy.ERROR` verified.
- **Reviewed:** [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] — 17 findings; the Critical
  one is the conflict with `brief.md` "standalone skill" (the Skill would depend on `base-project/`).

## 2026-09-29 (Phase 1 Tasks 1–3 closed)
- User confirmed the manual validation of [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend|Task 1]],
  [[Tasks/done/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker|Task 2]] and
  [[Tasks/done/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager|Task 3]]; all three moved from `Tasks/current/` to
  `Tasks/done/` and every link updated. Next: Task 4 (cross-project comparison).

## 2026-09-29 (Glossary and secrets decision)
- **Glossary populated** — 26 terms in [[Glossary/Glossary]] across *Analysis Documentation* (Reference Project,
  Explanation Doc, Review Doc, Finding, Finding ID, Strength to Keep, Era Gap, Review Summary, Recipe, Lineage),
  *Backend Patterns* (Feature Module, Generic CRUD Stack, DTO Tiers, Denormalized Counter, Idempotency Key,
  Two-Phase Upload, Compensation) and *Reference Domains* (HQ, Tenant, Storage Provider, Default Provider,
  Replication, Downloadable, File Copy, File Signature, Plan Entitlement), with the user's approval.
- **Committed secrets accepted, not to be repeated** — the owner leaves the exposed reference-project and
  git-history credentials in place for now; recorded as a rule in [[Memory/known-issues]] ("Patterns to avoid").

## 2026-09-29 (Task 3 executed)
- **`wpmanager/` analysis written** — [[Tasks/done/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager]].
  Output: [[Docs/wpmanager/wpmanager-Index]], [[Docs/wpmanager/Reviews/00-Review-Summary]] (88 findings: 9 🔴,
  16 🟠, 43 🟡, 20 🟢; 34 shared with `backend/`). Top issues: no URL-level authorization and an anonymous
  `/tests3` bucket controller (WP-R01-01/02), clients can delete admins and read storage keys through inherited
  CRUD (WP-R01-03/04), live cloud keys in a test (WP-R01-06), a failed provider upload commits a version row that
  blocks the version and stalls replication (WP-R03-01), theme upload never works (WP-R07-01).
  Strongest assets: the idempotency state machine, upload compensation, the S3-compatible provider
  abstraction, real-JWT E2E tests.
- **Method security question answered:** `@EnableMethodSecurity` on three `@Service` classes is honored and
  global (`@Import` on a `@Component` lite configuration candidate — `javap` on spring-context 6.2.1 and
  spring-security-config 6.4.2 — plus passing E2E 401/403 tests). `backend/` lost it by dropping those services.
- Corrections to planning leads: 12 (not 13) `@DataJpaTest` classes; 30 (not 29) open vault bug notes;
  `upload.max-file-size` has a default; authorization *is* tested for the author module; `jakarta.transaction`
  defaults match Spring's (the issue is overriding the class-level rollback rule); `spring.task.scheduling.enabled`
  is not a Boot 3.4.1 property.

## 2026-09-29 (Task 2 executed)
- **`BugTracker/` analysis written** — [[Tasks/done/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker]].
  Output: [[Docs/BugTracker/BugTracker-Index]], [[Docs/BugTracker/Reviews/00-Review-Summary]] (86 findings:
  5 🔴, 19 🟠, 34 🟡, 28 🟢; 81 defects vs 5 era gaps). Top issues: authentication-only authorization
  (BT-R01-01), no tenant isolation (BT-R01-02), JWT key in source (BT-R01-03), no-op base `update`
  (BT-R02-01, same as BE-R02-01), `equals` casting to `PlanEntity` breaks HQ invoicing (BT-R03-01).
  Strongest assets: the operator/tenant domain model, tenant-configurable taxonomies, and the
  filter/sort/page request contract.
- Corrections to planning leads: `BusinessEntity` has **15** EAGER collections (not 14); `EntityFactory`
  confirmed unused; mentions are saved twice but not duplicated; the `bsType` update diff is broken but its
  final join-table state is correct.
- Exact-version verification used local jars with `javap` (spring-tx 5.3.20, spring-boot-autoconfigure 2.7.0,
  spring-data-jpa 2.7.0, spring-data-rest-core 3.7.0, spring-security 5.7.1, jjwt 0.11.2); Boot 2.7 OSS end
  date (2023-06-30) from the spring.io support API.

## 2026-09-29 (Task 1 executed)
- **`backend/` analysis written** — [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend]].
  Output: [[Docs/backend/backend-Index]], [[Docs/backend/Reviews/00-Review-Summary]] (67 findings: 4 🔴,
  10 🟠, 26 🟡, 27 🟢). Top issues: no authorization enforced (BE-R01-01), anonymous client-token minting
  (BE-R01-02), Spring Data REST exposure (BE-R01-03, needs runtime check), no-op generic `update` (BE-R02-01).
  Strongest pattern: the whitelist-first QueryDSL list engine (`shared/query`).
- **Shared format + tooling:** [[Docs/Analysis-Doc-Conventions]] (templates, finding IDs, citation rules,
  changelog) and `scripts/validate-analysis-docs.py <project>` with `scripts/tests/` (44 tests). Tasks 2–3 reuse both.
- Corrections to planning leads: the Mockito pin equals Boot's managed version (only Surefire is
  downgraded); Hibernate bind-parameter logging is inert on Hibernate 6.6; `upload.max-file-size` has a
  default and does not break start-up.
- `backend/` verified unchanged (checksum snapshot `scripts/.snapshots/backend.sha256`, gitignored).

## 2026-09-29
- **Phase 1 analysis planned.** The parent Feature now defines the `Docs/<project>/{Explanations,Reviews}`
  layout and a Task 1–4 breakdown. Three Task docs were created and reviewed:
  [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend]],
  [[Tasks/done/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker]],
  [[Tasks/done/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager]].
- Key facts found during planning:
  - Stacks: BugTracker is Boot 2.7.0 (javax, Java 17 effective); backend and wpmanager are Boot 3.4.1 / Java 21.
  - Lineage: BugTracker → wpmanager → backend. The generic CRUD stack evolved across all three.
  - All three lack enforced URL-level authorization (see the tasks).
- Glossary initialized at `documentation/Glossary/`.

## 2026-09-28
- **Reference project swap** — `backend/` replaces the former `auth-server/` as reference project #1.
  Auth is organized differently from the old auth-server, so earlier assumptions about auth-server do
  not apply. `backend/` is Spring Boot 3.4.1 (artifactId `agentForgeBackend`, `<name>` still `authServer`).
  Updated AGENTS.md, memory bank, [[Docs/Skill-Architecture]] and [[Features/to-do/Spring-Boot-Skill-Creation]].

## 2026-03-29
- **Skill v1 created** — `spring-boot-skill/skill.md` and `spring-boot-skill/references/phase-0-onboarding.md`
  written. Phase 0 (Project Onboarding and Environment Assessment) is fully specified and is the
  active phase. All four design decisions from the feature spec are encoded:
  - Decision 1: conflict resolution with `--override-convention` flag
  - Decision 2: freshness check + delta onboarding (not full re-run every session)
  - Decision 3: four maturity states + structural inventory for precision
  - Decision 4: onboarding is mandatory and cannot be skipped
- **Skill file organization decided** — phase-based progressive disclosure model adopted.
  `skill.md` is a lean router (universal rules + Phase Registry + Phase Selection logic only).
  Each phase's full instructions live in its own `references/phase-N-<name>.md` file, loaded
  only when that phase is active. This avoids loading inactive phase instructions into context.
  → See `architecture.md` → Skill file architecture section.
- Added a detailed feature spec for the first skill instruction block:
  [[Features/to-do/Agent-Project-Onboarding-First-Instructions]]. Decision: onboarding
  must be mandatory default behavior, not only an optional command. Optional command, if
  added later, should be framed as re-onboarding or project-context refresh.
- Established skill directory convention: all skill files go in `spring-boot-skill/` during
  development; migrate to Claude Code system dir only when finalized. → [[Docs/Skill-Directory-Convention]]
- Memory Bank and documentation structure initialized. Three reference Spring Boot projects
  identified: auth-server, BugTracker, wpmanager. No project analysis performed yet.
  Next: deep analysis of each project to extract architectural patterns.
  → [[Features/to-do/Spring-Boot-Skill-Creation]]
