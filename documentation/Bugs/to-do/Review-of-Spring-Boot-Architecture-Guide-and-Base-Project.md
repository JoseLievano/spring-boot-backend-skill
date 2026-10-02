#critical #architectural

## Bug: Review of Spring-Boot-Architecture-Guide-and-Base-Project

### Summary

This is a review of the Feature document
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]
(`documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md`). The Feature is the action
plan to record the design decisions as ADRs, write a version-agnostic architecture **Guide**, build a tested
**Base Project** that owns the **Platform Modules**, rewrite the `spring-boot-backend` skill, and prove all three
through a generate → blind review → fix **Validation Loop**.

The review found **17 findings: 1 🔴 Critical, 5 🟠 High, 8 🟡 Moderate, 3 🟢 Low.** The overall direction is
sound: the decisions trace back to reference findings, and the Platform Modules are designed as deep modules with
real seams. The problems fall into three groups:

- **Plan-level conflicts.** The plan contradicts the project brief's "standalone skill" constraint (F1), builds
  the Base Project before the Skill in a different order from what the user described (F8), and never gives the
  Base Project the independent review that everything else gets (F2).
- **Design gaps that would repeat reference-project defects.** List endpoints have no ownership scoping (F3),
  uploads can still run inside a service transaction (F4), idempotency fingerprinting clashes with streaming
  uploads (F5), and the local identity lifecycle is undefined (F6).
- **Missing design topics.** Concurrency control, file downloads, auth hardening and CORS are not covered, and
  there are smaller sequencing and tooling issues.

Every finding's **Decision** is empty, waiting for the user.

### Reproduction Conditions
1. Read the Feature document in full.
2. Cross-check it against [[Memory/brief]], [[Memory/known-issues]], [[Docs/Analysis-Doc-Conventions]] and the
   three reference review summaries.
3. Walk each Platform Module through the `solid-deep-design` review questions and the reference findings it
   claims to fix.

### Environment / Preconditions (Optional)
- Documentation-only review. No code exists for the Guide, the Base Project or the new Skill yet.
- There is no ADR directory yet (0 ADRs checked). The Feature creates it in Step 1.2.

### Real-World Scenarios
- A generated project exposes `GET /notes`. User A lists the notes and gets user B's rows, because ownership is
  enforced only per entity (F3).
- A feature service method annotated as transactional through the CRUD base calls `UploadCoordinator`. The S3
  upload runs inside the database transaction, which is the exact wpmanager defect WP-R03-02 (F4).
- The Skill is migrated to `~/.claude/...`. It then cannot find `base-project/`, which lives only in this
  workspace (F1).

### Expected Behavior
The Feature should be consistent with the brief, close every reference defect it claims to close, and give each
artifact (Guide, Base Project, Skill) an independent quality check before downstream work depends on it.

### Actual Behavior
See the Findings below.

### Impact
- Without F1 resolved, the Skill design (Phase 4) contradicts an authoritative constraint.
- F3–F6 would reintroduce Critical/High classes of reference findings (WP-R01-03, WP-R03-02, WP-R04-02,
  BE-R01-04) into the Base Project that every future project starts from.
- F2 and F10 would let early design errors spread into every later artifact before anything catches them.

### Findings

---

#### F1 — The Skill depends on an external Base Project, contradicting the "standalone skill" constraint
**Severity:** 🔴 Critical

**Description:** Decision D2 and section 14 have the Skill "start a project from the Base Project (copy, rename
root package, set configuration)" (Feature line 559), and the Base Project lives at `base-project/` in this
workspace. The brief requires the opposite: "Skill must work as a standalone Claude Code skill (markdown prompt
file)" (`documentation/Memory/brief.md:23`). Known-issues adds "The skill output must be self-contained markdown
— it cannot depend on external scripts or tools" (`documentation/Memory/known-issues.md:50`). The Feature's Step
1.1 relaxes only the "grounded, not invented" constraint (`documentation/Memory/brief.md:24`), not this one. The
Feature also doesn't say where the Base Project lives after the Skill is migrated (Step 6.3).

**Examples:**
- User story 43 ("start a project from the Base Project") fails as soon as the Skill runs outside this
  repository.
- Under the memory-bank rules the brief wins every conflict, so a Task executor would be obliged to reject
  Phase 4.

**Why It Matters:** This is a conflict with an authoritative, user-owned constraint at the heart of the plan
(D2). The Skill's architecture (ADR-0017), its migration, and every Validation Run depend on how the Skill
reaches the Base Project.

**Possible Solutions:**
1. Bundle the Base Project inside the Skill (`spring-boot-skill/assets/base-project/`), so the Skill stays
   self-contained and migrates as one directory. Update the brief to allow non-markdown assets.
2. Publish the Base Project as a separate Git template repository that the Skill clones by URL. Update the brief
   to allow one external dependency.
3. Keep the Skill pure markdown and have it generate the platform from the Guide every time (reverses D2).

**Recommended Solution:** Option 1. It keeps D2 (the platform is written and tested once), makes the Skill
self-contained in practice, migrates atomically, and needs no network access. Add a Step 1.1 item: the user also
updates `brief.md:23` and `known-issues.md:50` to "standalone skill directory (markdown plus bundled template
assets)". Record the location in ADR-0002. Add a sync rule: `base-project/` is the development copy, and Step 6.3
copies a tagged version into the Skill's assets.

**Decision:** Custom option (user-proposed) — the Skill is a code-free convention, not a scaffold. (2026-09-30)

The Skill ships **no code artifacts and no Spring app**: it is pure markdown — rules, module **contracts**
(interfaces, invariants, error modes) and pseudo-code snippets only. Project initialization is explicitly **not**
the Skill's job, and the Skill is not Claude-Code-specific. New projects implement the contracts fresh, and the
Skill instructs the agent to look up current framework documentation for exact API syntax at execution time
(never assume it).

This **dissolves F1's premise**: the Skill has no dependency on `base-project/`, so `brief.md:23` ("standalone
skill (markdown prompt file)") and `known-issues.md:50` ("self-contained markdown") are both satisfied as
written. **No `known-issues.md` change is required for F1.** `brief.md` still needs a user update, but for
different wording: "scaffolds new Spring Boot APIs" and "Claude Code skill" no longer match (Step 1.1 amended).

`base-project/` remains a workspace deliverable as the **reference implementation** where the platform designs
are worked out and tested — so the convention is not invented — and as the harness for the Validation Loop. It
is never shipped with the Skill and never a copy source.

**Rationale:** (a) the user requires a project-agnostic, code-free skill; (b) a Base Project pinned to one
Spring Boot version would make the Skill teach deprecated APIs as the framework moves on (the F11 Boot 3 vs
Boot 4 concern), whereas contracts plus doc lookup stay current; (c) the architectural defects this Feature
exists to eliminate are prevented by contract-level precision, not by copying tested code.

**Parent document patched:** yes — D2, the description, the problem statement, user story 43, the flowchart,
sections 13 and 14, ADR-0002/0017 rows, Step 1.1 and the Risk Assessment drift bullet.

**Findings affected:** F16 is auto-resolved (the Skill no longer copies the sample feature). F11 is reframed
(only the Base Project pins a version; the Skill does not).

---

#### F2 — The Base Project is never independently reviewed
**Severity:** 🟠 High

**Description:** The Guide gets a validator and a user review (Step 2.6), and generated projects get a blind
review each run (Step 5.3). The Base Project — the shared framework where most of the Critical reference
findings live (authorization, the no-op update, upload consistency) — is built and self-tested by the same
agent (Phase 3). No step reviews it against the Guide with
[[Docs/Analysis-Doc-Conventions]]. Validation Runs review generated projects, but those inherit the platform
unchanged. Reviewers would see platform defects only indirectly, and the Root cause field (`base`) would come
too late: after the Skill is built on top.

**Examples:**
- A platform defect such as the missing list scoping (F3) is present in the Base Project. It reappears
  identically in both domains in run 01 and costs a full run cycle to find.
- The CRUD depth criterion is only measured in Validation Runs, after the Skill has hard-coded the base's hooks.

**Why It Matters:** Everything downstream copies the Base Project. Finding its defects before Phase 4 is far
cheaper than finding them through generated projects.

**Possible Solutions:**
1. Add Step 3.8: a blind review of `base-project/` (fresh agent context, Guide + conventions only) with finding
   IDs `BP-R<NN>-<MM>`, and a gate of 0 🔴 / 0 🟠 before Phase 4 starts.
2. Treat run 01 of the Validation Loop as the Base Project review.
3. Have the user review the Base Project manually.

**Recommended Solution:** Option 1. It uses the same method and gate as the Validation Loop, so results are
comparable, and it catches platform defects once instead of twice per run. Add it to Task 9 or as a new Task
between Tasks 9 and 10.

**Decision:** Option 4 (alternative, promoted to first-class) — review the contracts, not the code. (2026-09-30)

Add a **blind design review of the module contracts** (interfaces, invariants, error modes, cross-module
interactions) at the end of Phase 2, before Phase 3 encodes them: fresh agent context,
[[Docs/Analysis-Doc-Conventions]] review format, finding IDs `GC-R<NN>-<MM>`, and a gate of 0 🔴 / 0 🟠 before
Phase 3 starts. It goes in Task 5 alongside the existing validator + user review.

Step 3.7 becomes an explicit **contract-conformance checklist** for the Base Project: every contract invariant
from the Guide is backed by at least one test. Run 01's blind review stays as the downstream net.

**Rationale:** under the D2 revision recorded in F1, generated projects implement the contracts fresh and never
copy `base-project/` code, so the **contracts** are the highest-blast-radius artifact — a bad contract is
reproduced in every project, while a defect in the never-shipped reference implementation is not. F3–F6 are all
contract-level defects that pure design reasoning found with no code in existence. The review also fires earlier
and reuses the established review format and severity scale.

Option 1 (full blind code review of `base-project/`) is viable but spends the independent check on the
lower-leverage artifact. Option 2 is invalid under the new D2: run 01's reviewer never sees base code. Option 3
is weaker and reinstates self-grading.

**Parent document patched:** yes — Steps 2.6 and 3.7, Tasks 5 and 9, and the Guide-format section (new
`GC-R` finding-ID namespace).

---

#### F3 — List and search endpoints have no ownership or visibility scoping
**Severity:** 🟠 High

**Description:** The CRUD base enforces ownership through `authorize(Action action, E entityOrNull)` (Feature
line 398), which checks one entity at a time. `list` and `search` return pages built by `ListQuery.run(profile,
request, toSummary)` (section 7), and nothing restricts the predicate to rows the caller may see. The Query
Profile whitelists *fields*, not *rows*. Out-of-scope multi-tenancy (D8) was supposed to be replaced by
"ownership checks only", but ownership is never applied to collections.

**Examples:**
- User story 24 ("inherited operations cannot touch other users' data") holds for `GET /{id}` but fails for
  `GET /notes` and `POST /notes/search`. A client sees every user's notes. This is the list-side twin of
  WP-R01-03 and BT-R01-02.
- A per-row check after the query would break paging: page size, totals and page counts would be wrong.

**Why It Matters:** It is a data-exposure hole in the Platform Module that every project starts from, of exactly
the class the Feature exists to eliminate.

**Possible Solutions:**
1. Add a hook `protected Predicate visibleTo(CurrentUser user)` (default: deny all, or all for admins) that the
   base ANDs into every `get`, `list`, `search`, `update` and `delete` query. `ListQuery.run` takes it as a
   mandatory argument.
2. Use Hibernate filters enabled per request with the current user id.
3. Use PostgreSQL row-level security.

**Recommended Solution:** Option 1. It is explicit, testable at the CRUD interface, independent of the database
engine, and it becomes the natural extension point for tenancy later (D8): a tenant predicate is just another
visibility rule. Make it mandatory (no silent default) so a generated feature must decide. Add it to Guide 04 and
Guide 08, and add "list returns only visible rows" to the auth matrix tests.

**Decision:** Option 4 (alternative, promoted to first-class) — a mandatory `RowScope` contract. (2026-09-30)

Row visibility becomes a **contract-level, technology-neutral value**, not a JPA hook:

- `RowScope` is produced by the feature's `<F>AccessPolicy` through named factories — `ownedBy(ownerPath,
  user)`, `all()` (genuinely global resources), `system()` (F17's scheduled/system actor).
- `ListQuery.run` and every CRUD entry point **require** a `RowScope`. There is no overload without one, so
  deny-by-default is structural rather than a convention a generator can forget.
- The scope is ANDed into the query **before paging**, so page size, totals and page counts stay correct
  (unlike per-row post-filtering).
- Rows outside the scope read as `404`, not `403` — no existence leak.
- Guide 04, 06 and 08 state the contract; the auth matrix gains a "list returns only visible rows" case per
  feature (two users, overlapping pages).
- `RowScope` is the named D8 tenancy extension point: a tenant scope is just another scope composition.

**Rationale:** Option 1's enforcement is right (query-level AND, correct paging, engine-independent) but its
return type leaks a JPA `Predicate` into feature code, contradicting Guide section 7's rule that the predicate
technology is hidden behind `platform.query`. Under the revised D2 a leaked type would be re-implemented
differently on every project. `RowScope` is fail-closed by signature and puts row rules in the same
`<F>AccessPolicy` file as role rules. Options 2 (Hibernate filters) and 3 (PostgreSQL RLS) are both fail-open
or move the invariant outside the application contract, and neither survives a fresh per-project
implementation.

**Parent document patched:** yes — sections 5, 6, 7, 8, the Guide-document table (04/06/08), user story 24,
Testing Decisions, and the D8 scope note.

**Findings affected:** F7 is partly prepared (the `<F>AccessPolicy` is now the row-scope source and a Feature
Module file); F17's system actor is provided for by the `system()` scope factory.

---

#### F4 — "No transaction open during upload" is not enforced, and the CRUD base makes it easy to violate
**Severity:** 🟠 High

**Description:** `UploadCoordinator` "streams to `ObjectStorage` with **no** database transaction open" (Feature
line 514). The CRUD base declares a "class-level Spring `@Transactional`" (line 404). Any feature service that
extends the base and calls the coordinator from one of its methods runs the storage I/O inside that transaction.
This is exactly WP-R03-02, where the vault documented the intent and the code violated it
([[Docs/wpmanager/Explanations/07-Upload-Pipeline]]). Nothing in the Feature detects or prevents it.

**Examples:**
- `NoteService extends CrudService` adds `attach(id, file)`, which calls `uploadCoordinator.store(...)`. The
  class-level transaction is active, the S3 call holds a database connection for the whole transfer, and a
  failed `persist` rolls back rows written before the upload.

**Why It Matters:** It re-creates a High reference defect and its Critical consequence (WP-R03-01) in the Base
Project, and the design as written invites it.

**Possible Solutions:**
1. The coordinator fails fast: it throws if `TransactionSynchronizationManager.isActualTransactionActive()` is
   true. Add a test and a Guide rule. Upload endpoints call a non-transactional application service.
2. Mark the coordinator `@Transactional(propagation = NOT_SUPPORTED)` so it suspends any outer transaction.
3. Add an ArchUnit rule: classes calling `UploadCoordinator` must not be `@Transactional`.

**Recommended Solution:** Option 1 plus Option 3. The runtime guard makes misuse impossible to miss in tests,
and the ArchUnit rule catches it at build time. Option 2 alone would silently suspend a caller's transaction and
hide a design error. Update section 10, Guide 10 and the storage tests.

**Decision:** Option 4 (alternative, promoted to first-class) — remove the enabler and enforce the invariant
with the framework. (2026-10-02)

Two changes, both structural:

- **The CRUD base declares `@Transactional` per entry point, not at class level.** The annotation sits on the
  six base methods only (read-only for `get`/`list`/`search`); unchecked exceptions still roll back by
  default. Feature-added service methods are therefore **non-transactional by default** and must declare
  `@Transactional` explicitly when they perform multiple writes. This is a **documented departure from the
  reference projects** (US 5), recorded in Guide 04's "Differs From the Reference Projects".
- **`UploadCoordinator.store` is declared `@Transactional(propagation = NEVER)`.** A call made while a
  transaction is active fails immediately, before any storage I/O, instead of silently streaming inside it.
  Framework-native fail-fast: the `NEVER` contract is part of the module's stated error mode, and it also
  covers hook misuse and inherited class-level transactions. Upload entry points are non-transactional service
  methods; hooks never call `store`.

Also added: a transaction-boundary contract test in Testing Decisions ("`store` while a transaction is active
fails immediately and performs no storage I/O") and the same invariant named in Step 3.7's
contract-conformance checklist. Guide table rows 04, 10 and 13 updated.

**Rationale:** the finding's title names the *enabler* — the class-level transaction — so the fix removes it
rather than building detectors around it. Making the safe path the default path is more executable and less
ambiguous than two detection layers; `propagation = NEVER` subsumes Option 1's fail-fast intent using framework
machinery instead of hand-rolled `TransactionSynchronizationManager` probing, and it covers paths static
analysis cannot see. Architecturally it is SRP-true: the base stops imposing a transaction policy on methods it
does not own, and it inherits F3's "structural over conventional" stance. The cost is one documented departure
from the reference shape, which US 5 already requires us to record.

Options 1 and 3 remain viable but close the symptom rather than the cause. Note that Option 3 as literally
worded cannot work: `isAnnotatedWith(Transactional.class)` is blind to the class-level `@Transactional`
inherited from `platform.crud.CrudService` (spring-tx 6.2.1 `@Transactional` is `@Inherited`;
TNG/ArchUnit#277), so it would miss the finding's own `NoteService` example and would need reformulation to
`areAssignableTo(annotatedWith(...))`. Option 2 (`NOT_SUPPORTED`) silently suspends the caller's transaction —
hiding the design error, contradicting F3's fail-closed-by-signature precedent — and the suspended outer
transaction still holds a pooled connection for the whole transfer, so WP-R03-02's pool-exhaustion impact
survives.

**Parent document patched:** yes — section 6 (transaction bullet, rewritten as a per-entry-point rule with an
explicit departure note), section 10 (`UploadCoordinator` gains the `NEVER` no-transaction contract), the Guide
document table rows 04/10/13, Step 3.5, Step 3.7 (contract-conformance checklist), and Testing Decisions (the
transaction-boundary contract test).

**Findings affected:** none directly. F9 (concurrency control) is unaffected — the version/`If-Match` design
still belongs on the six entry points. F13 (file downloads) should reuse the same rule: a download endpoint
that streams must not run inside a transaction either.

---

#### F5 — Idempotency fingerprinting conflicts with streaming uploads
**Severity:** 🟠 High

**Description:** The `@Idempotent` interceptor "builds the fingerprint from method, path and body hash"
(Feature line 527) before calling the guard. Uploads are `@Idempotent` by default and must stream large files
(section 10, WP-R05-04). Hashing the body in an interceptor means reading the whole multipart stream before the
controller runs. The request then has to be buffered in memory or on disk, or the stream is consumed and the
controller receives nothing. WP-R04-06 (the file hashed twice) is recreated at best.

**Examples:**
- A 500 MB theme upload: the interceptor must read all 500 MB to fingerprint it, then the coordinator reads it
  again to store it.
- With a raw streaming body (not multipart), the interceptor consumes the only stream.

**Why It Matters:** Two Platform Modules designed in the same Feature are incompatible on the main use case they
were combined for (user stories 33 and 36–38).

**Possible Solutions:**
1. The fingerprint is method + path + canonical *non-file* fields + a client-declared content digest (a
   `Content-Digest` or `X-Content-SHA256` header). The coordinator verifies the digest while streaming and fails
   with 422 on mismatch.
2. Spool the upload to a temporary file once, then hash it and hand the file to both the guard and the
   coordinator.
3. Exclude file content from the fingerprint and accept the risk.

**Recommended Solution:** Option 1. It keeps one streaming pass, gives the server end-to-end integrity checking
for free, and matches how S3 itself verifies checksums. Option 2 is a reasonable fallback for clients that cannot
send a digest; document it as the local-adapter default. Update sections 10 and 11, Guide 10 and 11, and the
idempotency tests.

**Decision:**

---

#### F6 — The local identity lifecycle is undefined: credentials, user creation and module removal
**Severity:** 🟠 High

**Description:** Section 8 designs `UserProvisioning` as "find by `externalSubject` or create on first request"
(line 454). That fits external providers, but in `local` mode a user must exist *before* the first request,
because login needs a stored password. The Feature doesn't say:
- where password hashes, account flags and refresh tokens are stored. If they're columns on the platform `User`,
  deleting `platform.identity.local` (the migration story, user story 28) leaves orphaned columns, and `User`
  depends on local-mode concepts.
- how users are created, disabled or given roles in local mode. There is no user-management design, and only
  the first admin is covered (line 460).
- what `externalSubject` holds for local users. Is it the user id, which the issuer puts in `sub`?

**Examples:**
- An admin wants to create a second user in a generated project. There is no endpoint or service for it, so the
  Skill will invent one differently per run.
- The team migrates to Clerk and deletes `platform.identity.local`. `User.passwordHash` and `User.enabled` stay
  in the schema, and code that still reads them breaks ArchUnit's "nothing imports local" rule.

**Why It Matters:** Identity is the area with the most Critical reference findings. An undefined lifecycle means
each Validation Run invents its own, and the "delete one module to migrate" promise isn't true as designed.

**Possible Solutions:**
1. `platform.identity.local` owns a `LocalCredential` entity (user id, password hash, enabled/locked flags,
   failed-attempt counter) and a `RefreshToken` entity, both in its own Flyway migrations. The platform `User`
   keeps only `id`, `externalSubject`, e-mail, display name and roles. Local mode sets `externalSubject =
   "local:" + id`. Add a small admin-only user-management API (create, disable, assign roles) inside the local
   module, and a role-assignment service in `platform.identity` that works in both modes.
2. Put the credentials on `User` and accept a migration script when switching to an external provider.
3. Use Spring Authorization Server as the local issuer.

**Recommended Solution:** Option 1. It makes the removal promise real (the module owns its tables), keeps `User`
provider-neutral, and gives the Skill one canonical user-management flow. Option 3 is heavier than needed for
"login, refresh, logout". Update section 8, Guide 08, ADR-0006 and ADR-0007, and the Token Issuer tests.

**Decision:**

---

#### F7 — Authorization is split across four mechanisms, and one of them isn't in the Feature Module anatomy
**Severity:** 🟡 Moderate

**Description:** Section 8 lists URL rules (`authorizeHttpRequests`), method security (`@EnableMethodSecurity`),
"role rules per feature in one `<F>` access declaration used by the base controller" (line 469), and the CRUD
`authorize` hook. The access declaration isn't in the Feature Module file table (section 5, line 376), and the
Feature never says which mechanism owns which decision. With four possible places, generated projects will put
rules in different places per run. That is the inconsistency the fixed anatomy is meant to prevent, and it is
how `backend/` ended up with annotations that did nothing (BE-R01-01).

**Examples:**
- Run 01, domain A: role rules as `@PreAuthorize` on the service. Domain B: rules in an access declaration.
  Both pass the auth matrix, but the Skill's output is inconsistent, and the reviewer can't say which one is
  wrong.

**Why It Matters:** It weakens locality, because authorization knowledge spreads across up to four places per
feature, and it makes the Guide rule ambiguous.

**Possible Solutions:**
1. Fix responsibilities: URL rules only separate public from authenticated routes. Role rules live in one
   `<F>AccessPolicy` file (added to the section 5 anatomy) that the CRUD base consults for every action.
   Row-level rules live in `visibleTo` (F3) and `authorize`. Method security is enabled but used only in
   opt-out services.
2. Use method security (`@PreAuthorize`) on service methods for all role rules and drop the access declaration.
3. Use URL rules for roles and the hook for ownership.

**Recommended Solution:** Option 1. One file per feature states who may do what, which makes it easy to review
and generate and gives the auth matrix a single source. Option 2 is also acceptable but spreads rules over
inherited methods that features don't declare.

**Decision:**

---

#### F8 — The build order differs from the order the user described
**Severity:** 🟡 Moderate

**Description:** The user's stated sequence was: write the guidelines, create the skill, use it to create a
project, critique it, update the skill files, and "at the end try to create a base project". The Feature builds
the Base Project in Phase 3, before the Skill (Phase 4), and treats "Base Project v1" as a release tag
(Step 6.1). The user did choose "Base owns core" (D2), which implies the base exists before the Skill can start
from it. But the reordering was never stated to them as a consequence.

**Examples:**
- The user expects to test the Skill early. Under the Feature, the Skill arrives only after nine Tasks.

**Why It Matters:** It changes when the user first sees the Skill working, and how much code is written before
the first real feedback.

**Possible Solutions:**
1. Keep the order (base first) and confirm it explicitly with the user as a consequence of D2.
2. Build a *thin* Base Project first (Tasks 6–8 only: config, errors, identity, CRUD, query), write the Skill,
   run one Validation Run, then add storage and idempotency (Task 9) and continue.
3. Generate the platform with the Skill in the first run and promote the reviewed result to the Base Project at
   the end (the user's original order; conflicts with D2).

**Recommended Solution:** Option 2. It respects D2, gets the Skill and a first Validation Run in front of the
user sooner, and exercises the loop before the most complex modules exist. Reorder the tasks to 6 → 7 → 8 → 10 →
11 → 12 (run 01) → 9 → further runs.

**Decision:**

---

#### F9 — No concurrency control (lost updates)
**Severity:** 🟡 Moderate

**Description:** The CRUD base's `update` loads the entity, applies `mapper.update(request, entity)` and commits
(section 6). Neither the Feature nor the Guide outline (Guide 04, Guide 07) mentions optimistic locking
(`@Version`), `ETag`/`If-Match`, or `409` on stale writes. None of the reference reviews flagged it because none
had a working update to begin with (BE-R02-01).

**Examples:**
- Two admins edit the same record. The second `PUT` silently overwrites the first admin's changes.

**Why It Matters:** Once the update actually works, lost updates become the next correctness problem, and adding
a version column later needs a migration on every table.

**Possible Solutions:**
1. Every entity gets a `@Version` column. The `Response` exposes the version, and `PUT` requires it (in the
   body or `If-Match`). A mismatch returns 409 through `DomainException`.
2. Use `@Version` only, and map `OptimisticLockException` to 409 without exposing the version to clients.
3. Leave it out and document last-write-wins.

**Recommended Solution:** Option 1. The base handles it once for every feature, and clients get a clear signal.
Add it to Guide 04, 05 and 07, the base entity design, and the CRUD tests.

**Decision:**

---

#### F10 — The whole Guide is written before any code (a horizontal slice)
**Severity:** 🟡 Moderate

**Description:** Phase 2 writes all 16 Guide documents (Tasks 2–5) before Phase 3 writes any Base Project code.
Step 3.7 exists to fix Guide rules found wrong while building, which admits the risk. The `tdd` skill warns
against horizontal slicing: rules written in bulk describe imagined behavior. F3–F6 are examples of gaps that
building the modules would have exposed quickly.

**Examples:**
- Guide 11 (idempotency) is finished and validated in Task 5. Task 9 then discovers F5 and rewrites it: two
  passes over the same document.

**Why It Matters:** It means rework, and the Guide passes its validator while still being wrong.

**Possible Solutions:**
1. Interleave by area: write the Guide conventions (Task 2), then for each area write that Guide document and
   immediately build and test its Platform Module, test-first. The traceability matrix (Guide 16) closes the
   phase.
2. Keep the order, but mark Guide documents `draft` until their module is built.
3. Keep the order as is.

**Recommended Solution:** Option 1. It gives vertical slices per area with fast feedback, and each Guide document
is proven by code before anything depends on it. Option 2 is the minimal alternative if the user prefers to read
the whole Guide first.

**Decision:**

---

#### F11 — The version baseline and the Base Project's version line are left open
**Severity:** 🟡 Moderate

**Description:** D3 sets "Spring Boot 3+, Java 21+", while the Base Project uses "current Boot 3.x or later line
when built" (line 549). The Spring Security reference used to verify section 8 is version 7.0, which belongs to
the Spring Boot 4 line (the current Context7 default). Boot 4 changes things the Guide depends on: a new Jackson
major version, Security 7 defaults, and modularised auto-configuration. If the Base Project is built on Boot 4,
the Guide's "3+" claims need version notes for the 3.x differences, and nobody verifies them because the Base
Project doesn't run on 3.x.

**Examples:**
- Guide 09 says the problem writer "uses Boot's `ObjectMapper`". On the Boot 4 line the default JSON stack
  differs, so the rule's version note would have to cover both lines, and only one is tested.

**Why It Matters:** Version-neutral rules are fine, but untested version notes become the same drift the
reference docs showed (WP-R11-04).

**Possible Solutions:**
1. Pin the Base Project to the latest GA line at build time, likely Boot 4.x, and set the Guide baseline to that
   major. Version notes then cover *newer* lines only as they appear.
2. Pin the Base Project to Boot 3.5.x and keep "3+" as the baseline. Boot 4 notes are added when the Base
   Project upgrades.
3. Keep "3+" and verify version notes by documentation only.

**Recommended Solution:** Decide in ADR-0003 before Task 3, with Option 1 as the default. A new project should
start on the current line, and the baseline should match what is actually tested. The user chose "Boot 3+"
before this fact was surfaced, so they need to confirm.

**Decision:**

---

#### F12 — Validation-run citations break when generated projects are git-ignored or cleaned
**Severity:** 🟡 Moderate

**Description:** Validation reviews follow [[Docs/Analysis-Doc-Conventions]], where citations must resolve:
"A cited excerpt must point at real lines". But `validation-runs/` is git-ignored and "may be cleaned after
each run's review is written" (lines 191 and 643). After cleanup, or on another machine, the `validation`
validator target fails, and the review evidence can no longer be checked.

**Examples:**
- Run 03 cites `validation-runs/run-01/a/src/.../NoteService.java:42` to show a recurring defect. After cleanup,
  the citation is unverifiable.

**Why It Matters:** The loop's value is comparable evidence across runs. Evidence that disappears undermines the
Exit Gate.

**Possible Solutions:**
1. Commit each run's generated sources (without build outputs) under `validation-runs/`. They are small text
   projects.
2. Archive each run as a tagged commit or zip, and have the validator skip citation checks for archived runs.
3. Keep them ignored and relax citation validation for validation reviews.

**Recommended Solution:** Option 1. It is simple, diffable (you can see how generated code changes between runs)
and keeps the existing validator rules intact. Remove "may be cleaned" and ignore only `target/` and `build/`.

**Decision:**

---

#### F13 — File download design is missing
**Severity:** 🟡 Moderate

**Description:** Section 10 covers storing files and `ObjectStorage.open()` but not how clients *download* them:
streaming through the app, S3 presigned URLs, or signed app URLs for local storage. It also doesn't say how
download authorization works. wpmanager's `FileSigner` (fail-fast secret, HMAC-signed links) is listed as a
**Strength to Keep** in [[Docs/wpmanager/Reviews/00-Review-Summary]], but the Feature drops it without comment.

**Examples:**
- A domain with attachments: generated project A streams files through a controller, project B returns S3 URLs,
  and neither checks `visibleTo` (F3) before serving.

**Why It Matters:** Downloads are half of the storage use case, and they are where access control and
performance meet.

**Possible Solutions:**
1. Add a `DownloadLinks` port (ISP: separate from `ObjectStorage`). The S3 adapter issues short-lived presigned
   URLs, and the local adapter issues HMAC-signed app URLs served by a platform controller. Links are issued only
   after the feature's `visibleTo`/`authorize` check.
2. Always stream through the application, with authorization in the controller.
3. Leave it to each feature.

**Recommended Solution:** Option 1. It keeps the proven wpmanager strength, offloads bandwidth to S3, and gives
local storage the same contract. Add it to section 10, Guide 10 and the storage contract tests.

**Decision:**

---

#### F14 — Auth endpoints lack brute-force protection, and CORS is not designed
**Severity:** 🟡 Moderate

**Description:** The Token Issuer exposes `POST /auth/login` and `/auth/refresh` (line 460) with no rate
limiting, lockout or failed-attempt policy. CORS was a reference finding (BE-R01-10: hardcoded origin, injected
source ignored), but it appears in neither the Feature nor the Guide outline (Guide 08, 12).

**Examples:**
- Password spraying against `/auth/login` has no limit.
- Generated projects copy the reference habit of hard-coding `localhost:3000`.

**Why It Matters:** These are standard hardening items for a self-issued login, and one of them is a known
reference defect that the traceability matrix would otherwise not cover (it is 🟡, not 🔴/🟠).

**Possible Solutions:**
1. Guide 08: failed-attempt counter plus temporary lockout in `LocalCredential` (F6), and a per-IP/per-user rate
   limit on auth endpoints. Guide 12: CORS origins from typed configuration, with no wildcard when credentials
   are allowed. Add tests for both.
2. Delegate rate limiting to infrastructure (a gateway) and document the assumption.

**Recommended Solution:** Option 1 for lockout and CORS (app-level, testable). Rate limiting can be Option 2 if
the user wants to keep infrastructure out of scope, but the Guide must state the assumption. Also extend the
traceability matrix scope to include 🟡 security findings.

**Decision:**

---

#### F15 — The blind reviewer is never calibrated
**Severity:** 🟢 Low

**Description:** The Exit Gate depends on an LLM reviewer reporting 0 🔴 / 0 🟠 twice in a row (line 578).
Reviews are non-deterministic, and a lenient reviewer passes the gate trivially. Nothing checks that the
reviewer finds real defects.

**Examples:**
- The reviewer misses a missing-authorization defect in run 02, and the gate passes.

**Why It Matters:** The gate's value depends on the reviewer's sensitivity.

**Possible Solutions:**
1. Calibrate once in Task 11: run the reviewer prompt blind on `backend/` and require it to find BE-R01-01,
   BE-R01-02 and BE-R02-01 (known Criticals) before trusting it.
2. Plant known defects (seeded bugs) in each run's projects and require the reviewer to find them.
3. Accept the variance.

**Recommended Solution:** Option 1. It's cheap, reuses existing ground truth, and is done once. Option 2 can be
added if calibration fails.

**Decision:**

---

#### F16 — The Skill's handling of the sample feature is undefined
**Severity:** 🟢 Low

**Description:** The Base Project contains a sample Feature Module (`features/note`) that its tests depend on
(section 13). "Start a project from the Base Project" (line 559) doesn't say whether the sample is kept, removed,
or turned into test-only code. If it's removed, the platform tests that rely on it disappear too.

**Examples:**
- A new project ships a `/api/v1/notes` endpoint nobody asked for, or loses the platform's CRUD and auth-matrix
  coverage.

**Why It Matters:** It's a small but guaranteed source of inconsistency between runs.

**Possible Solutions:**
1. Move the sample feature to `src/test` (a test-only fixture feature), so platform tests keep working and
   production code has no sample.
2. The Skill deletes the sample and its tests when starting a project.
3. Keep the sample in production code as documentation.

**Recommended Solution:** Option 1. Platform coverage survives in every generated project, and no stray endpoint
ships.

**Decision:**

---

#### F17 — There is no identity for scheduled and system work
**Severity:** 🟢 Low

**Description:** `CurrentUser` is "the only identity type feature code may import" (section 8), but the
idempotency cleanup job, the optional replication worker and first-admin bootstrap run without a request. The
Feature doesn't define a system actor, so auditing (`createdBy`) and `visibleTo` (F3) have nothing to use in
those contexts.

**Examples:**
- The replication worker writes a `StoredFile` row, auditing asks `CurrentUserProvider.current()`, and it throws.

**Why It Matters:** It's an edge case, but it will surface in the first scheduled job.

**Possible Solutions:**
1. Define `CurrentUser.system()` and a `runAsSystem(...)` helper in `platform.identity`. Auditing records
   `system` for such work.
2. Have scheduled code bypass auditing and scoping explicitly.

**Recommended Solution:** Option 1. One explicit, testable concept instead of scattered bypasses.

**Decision:**

---

### Investigation Scope
- **Code Reviewed:** The Feature document. `documentation/Memory/*` (all seven files). `spring-boot-skill/skill.md`
  and the headings of `spring-boot-skill/references/phase-0-onboarding.md`. [[Docs/Skill-Architecture]],
  [[Docs/Skill-Directory-Convention]], [[Docs/Analysis-Doc-Conventions]] (layout and citation rules). The three
  review summaries. The Summary/Why sections of [[Docs/wpmanager/Explanations/07-Upload-Pipeline]],
  [[Docs/wpmanager/Explanations/08-Upload-Idempotency]],
  [[Docs/wpmanager/Explanations/09-Storage-Providers-and-Replication]],
  [[Docs/backend/Explanations/07-Authentication-and-Authorization]],
  [[Docs/backend/Explanations/05-Dynamic-List-Query-Engine]] and [[Docs/BugTracker/Explanations/07-Domain-Model]].
  The storage, upload and idempotency review recommendations.
- **Logs Reviewed:** No. This is a design review with no runtime.
- **Runtime Evidence:** None. All 61 finding IDs the Feature cites were checked to exist, and all wiki links
  resolve.

### Root Cause Analysis
**Confirmed** for F1 (the constraint text is quoted), and for F4, F5 and F12 (they follow directly from the
Feature's own statements). The other findings are gaps where the Feature is silent. They are confirmed as
omissions, and their severity is a judgment.

### Evidence in Code
- `documentation/Memory/brief.md:23` — the standalone-skill constraint (F1).
- `documentation/Memory/known-issues.md:50` — the self-contained markdown constraint (F1).
- `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md:398` — per-entity `authorize`
  only (F3).
- `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md:404` and `:514` — class-level
  transaction vs no-transaction upload (F4).
- `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md:527` — body-hash fingerprint
  (F5).
- `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md:454-466` — provisioning and
  Token Issuer (F6).
- `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md:469` and `:376` — access
  declaration missing from the anatomy (F7).
- `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md:191` and `:643` — git-ignored
  and cleaned runs (F12).

### Affected Systems / Modules
- [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] — every finding patches this Feature.
- [[Memory/brief]] — F1 (user-owned; wording change needed).
- [[Memory/known-issues]] — F1.

### Affected Processes
- [[Docs/Analysis-Doc-Conventions]] — F12 (citation validation for validation reviews); F2 and F15 reuse its
  review format.
- [[Docs/Skill-Directory-Convention]] — F1 (migration must include bundled assets).

---

### Confidence Level
Confirmed for F1, F4, F5 and F12. Strong hypothesis (confirmed omission, judged severity) for the rest.

### Remaining Uncertainty / Open Questions
- Whether the user wants to relax the standalone-skill constraint (F1) or reverse D2.
- Whether the user confirms the reordering (F8) and the interleaving (F10). These two interact.
- Whether rate limiting should stay out of scope as infrastructure (F14).

---

## Solution Direction

### Proposed Fix
Resolve each finding's Decision with the user, then patch the Feature. Most fixes add a hook, a rule or a step.
Only F1, F8 and F10 change the plan's shape.

### Why This Fix Is Correct
Each recommendation either restores consistency with an authoritative constraint (F1), closes a
reference-defect class the Feature claims to close (F3–F6, F9, F13, F14), or adds an independent check before
downstream work depends on an artifact (F2, F10, F15).

### Skills and Documentation Used During Analysis and Solution Validation
- `solid-deep-design` — deletion test and seam discipline (F3, F7, F13: `DownloadLinks` as a separate interface
  per ISP), and depth of the CRUD base.
- `tdd` — the vertical-slice argument (F10).
- `memory-bank`, `glossary-management`, `doc-exploration` — constraints, vocabulary, and the 0-ADR inventory.
- Context7: Spring Security reference 7.0 (`/websites/spring_io_spring-security_reference_7_0`) —
  `NimbusJwtDecoder.withSecretKey`/`withPublicKey`, the JWK Set decoder and `JwtAuthenticationConverter`, all as
  the Feature describes. This version also surfaced the Boot 4 line (F11).
- Context7: MapStruct (`/mapstruct/mapstruct`) — `@MappingTarget` update methods and
  `unmappedTargetPolicy = ReportingPolicy.ERROR`, as the Feature describes.

### Files to Modify or Create
- `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md` — patched per decisions.
- `documentation/Memory/brief.md` — the user edits it (F1).
- `documentation/Memory/known-issues.md` — F1.

### Validation Strategy After Fix

#### Automatic Validation
- [ ] All wiki links in the patched Feature resolve, and all cited finding IDs exist (same checks as this
  review).

#### Manual Validation
- [ ] The user confirms each finding's Decision and reads the patched Feature sections.

### Potential Risks / Notes
- F8 and F10 both reorder Tasks. Decide them together to avoid renumbering twice.

---

## Resolution Steps

### Phase 1: Decide
- [ ] **Step 1.1:** Walk the findings with the user (the `feature-findings-solver` skill) and record each
  Decision.

### Phase 2: Patch
- [ ] **Step 2.1:** Patch the Feature per the decisions (sections 6, 8, 10, 11, 13, 14, 15, Implementation
  Steps, Task Breakdown).
- [ ] **Step 2.2:** The user updates `brief.md`, and the agent updates `known-issues.md` (F1).

---

## Task Breakdown

### Task 1: Resolve review findings and patch the Feature
- **Steps Covered:** Step 1.1, Step 2.1, Step 2.2
- **Reason for Grouping:** Decision-driven document edits with no code. One sitting with the user.
- **Planned Task File:** `Review-of-Spring-Boot-Architecture-Guide-and-Base-Project-step-1-resolve-findings.md`
- **Task Document Link:** [Add when the task document is created]

---

## Expected Outcome After Fix
- The Feature is consistent with the brief, and the Skill has a defined way to reach the Base Project.
- The Base Project design closes list-level data exposure, upload transaction leaks, idempotency and streaming
  conflicts, and identity-lifecycle gaps before any code is written.
- Every artifact gets an independent check before downstream work depends on it.

---

## Affected Documentation
- [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] — the reviewed Feature; all findings.
- [[Memory/brief]] — F1: standalone-skill constraint.
- [[Memory/known-issues]] — F1: self-contained skill constraint.
- [[Docs/Analysis-Doc-Conventions]] — F12: citation rules for validation reviews; F2 and F15: review format
  reuse.
- [[Docs/Skill-Directory-Convention]] — F1: migration of bundled assets.
- [[Docs/wpmanager/Explanations/07-Upload-Pipeline]] — F4: the transaction-boundary precedent.
- [[Docs/wpmanager/Explanations/08-Upload-Idempotency]] — F5: the fingerprint precedent.
- [[Docs/wpmanager/Reviews/00-Review-Summary]] — F13: `FileSigner` strength.
- [[Docs/backend/Explanations/07-Authentication-and-Authorization]] — F6, F7, F14.

---

## Findings Summary

| # | Title | Severity | Status |
|---|-------|----------|--------|
| F1 | The Skill depends on an external Base Project, contradicting the "standalone skill" constraint | 🔴 Critical | Done |
| F2 | The Base Project is never independently reviewed | 🟠 High | Done |
| F3 | List and search endpoints have no ownership or visibility scoping | 🟠 High | Done |
| F4 | "No transaction open during upload" is not enforced | 🟠 High | Done |
| F5 | Idempotency fingerprinting conflicts with streaming uploads | 🟠 High | Pending |
| F6 | The local identity lifecycle is undefined | 🟠 High | Pending |
| F7 | Authorization split across four mechanisms; access declaration not in anatomy | 🟡 Moderate | Pending |
| F8 | The build order differs from the order the user described | 🟡 Moderate | Pending |
| F9 | No concurrency control (lost updates) | 🟡 Moderate | Pending |
| F10 | The whole Guide is written before any code | 🟡 Moderate | Pending |
| F11 | The version baseline and the Base Project's version line are left open | 🟡 Moderate | Pending |
| F12 | Validation-run citations break when generated projects are git-ignored or cleaned | 🟡 Moderate | Pending |
| F13 | File download design is missing | 🟡 Moderate | Pending |
| F14 | Auth endpoints lack brute-force protection; CORS not designed | 🟡 Moderate | Pending |
| F15 | The blind reviewer is never calibrated | 🟢 Low | Pending |
| F16 | The Skill's handling of the sample feature is undefined | 🟢 Low | Pending |
| F17 | There is no identity for scheduled and system work | 🟢 Low | Pending |
