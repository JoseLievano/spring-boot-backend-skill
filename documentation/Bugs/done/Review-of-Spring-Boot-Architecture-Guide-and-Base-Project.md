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

Findings F1–F8 and F10 have a recorded **Decision** and are marked Done (F8 and F10 were decided jointly).
Every other finding's **Decision** is empty, waiting for the user.

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

**Decision:** Option 1 (refined) — a client-declared content digest over the **stored object's bytes**,
verified inside the coordinator's single streaming pass. (2026-10-03)

The fingerprint becomes `method + path + canonical non-file fields + a declared content digest`. No code path
hashes the request body for idempotency. Four refinements to the option as written above are required for the
mechanism to work at all:

- **The digest's subject is the stored object's bytes, not the HTTP message.** For multipart uploads it travels
  **file-scoped** (a field on the file part or a dedicated form field). RFC 9530 `Content-Digest` hashes the
  whole message content — the multipart envelope with its boundaries — so it can never equal `StoredFile`'s
  SHA-256 and is not used. For raw-stream bodies (content == object) a header is used instead, with exactly one
  pinned spelling (`X-Content-SHA256: sha-256:<hex>`, or RFC 9530 `Repr-Digest`); the obsoleted RFC 3230
  `Digest` / `Want-Digest` are never used.
- **Fail-closed:** a file or streaming request carrying no digest is rejected (400/422) before the guard runs.
  There is no silent accept.
- **`UploadCoordinator.store` verifies the digest during its existing single streaming pass** — the same pass
  that computes `StoredFile`'s SHA-256 — and fails with **422** on mismatch, taking the existing compensation
  path so no object is left behind. Client trust is backed by verification: a lying digest fails at EOF.
- **Buffered JSON endpoints keep the existing `method + path + body hash` fingerprint.** Only file/streaming
  requests change, so US 36/38 clients are unaffected.

Two distinct 422s get separate `ProblemDetail` types (Guide 09/11): **fingerprint mismatch** (same key,
different request) and **content-digest mismatch** (declared digest ≠ stored bytes).

**Option 2 (spool to a temp file once, hash it, hand the file to both the guard and the coordinator) stays as
an explicit documented fallback** for clients that cannot compute a digest. It is **never** a per-adapter
default — that would make upload behavior diverge between storage providers (LSP). When used, the SHA-256 is
computed **in the spool pass** and handed along ("compute once, pass along", closing WP-R04-06) rather than
recomputed as a second read.

**Rationale:** the unrefined Option 1 would have shipped a digest that never matches the stored checksum, so
the refinement is not optional. The fix removes the *enabler* the same way F4 did: the interceptor stops owning
payload identity, so nothing ever buffers or consumes the request body to fingerprint it. US 37 survives
server-side — changed bytes change the digest, which changes the fingerprint, which yields 422 — where Option 3
would have abandoned it and recreated WP-R04-02 in mirrored form. Verification lands in the one module that
already streams and already computes `StoredFile`'s SHA-256, so depth increases with no new port and no second
read; one end-to-end pass honors WP-R05-04 and F4's no-transaction contract. An alternative solution was
analyzed (binder-side fingerprint construction, fail-closed digest rule, server-computed proof on replay); it
concluded it was **not materially better** — the client-declared digest verified while streaming is forced by
the constraint set (S3 `x-amz-checksum-*`, GCS `x-goog-hash` and Tus `Upload-Checksum` all use this pattern) —
but its file-scoped digest channel and fail-closed rule are absorbed above.

**Parent document patched:** yes — section 10 (`UploadSource` gains the expected digest; the coordinator
verifies while streaming and 422s on mismatch), section 11 (fingerprint composition, the fail-closed digest
rule, the two 422 meanings, the documented fallback), the Guide document table rows 10 and 11, Steps 3.5 and
3.6, and Testing Decisions.

**Findings affected:** none directly. F9 (concurrency control) is unaffected — the version/`If-Match` design
is orthogonal. F13 (file downloads) is unaffected: downloads are read-only and never idempotency-guarded.

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

**Decision:** Option 4 (alternative, promoted to first-class) — durable `User`, rebindable subject binding, and
one `UserDirectory` lifecycle contract. (2026-10-03)

The three listed options each leave a hole; the alternative closes all of them at roughly the cost of Option 1.

- **Identity invariant: `User.externalSubject` always equals the token `sub`.** In local mode `sub` = `User.id`
  (the Token Issuer already sets this, BE-R01-05/07). **No `"local:"` prefix.** Namespacing is unnecessary
  because the `iss` claim is already validated by the decoder, and a prefix would force a mode-specific lookup
  step that every generated project could forget — breaking US 30's "the same model works with self-issued and
  external tokens". `UserProvisioning.resolve` is therefore literally `findByExternalSubject(jwt.getSubject())`
  in both modes.
- **Credential state and account state are split at the module boundary.** `platform.identity.local` owns
  `LocalCredential` (password hash, locked flag, failed-attempt counter, `mustChangePassword`) and
  `RefreshToken`, in its own Flyway migrations. The platform `User` owns only `id`, `externalSubject`, e-mail,
  display name, a roles mirror and an app-level `status` (`ACTIVE`/`DISABLED`). Deleting the module strands no
  columns and no local-mode concepts in `User`.
- **`User.status` survives migration.** An app-level ban stays enforceable after switching to Clerk/WorkOS,
  closing BE-R01-08 in both modes: account status is resolved on **every** request in `UserProvisioning`, not
  only at login. Lockout stays in `LocalCredential` — it is a local-credential concern, so F14's prepared
  wording is unaffected.
- **One lifecycle contract: `UserDirectory`** (`create`, `setStatus`, `rebindSubject`) in `platform.identity`,
  the sole path for creating and changing user records in both modes. First-admin bootstrap and the local admin
  surface both compose it. `rebindSubject` is what makes US 28 true at the **data** level: migrating to
  Clerk/WorkOS is a runbook (export users and roles → create provider accounts → `rebindSubject` per user →
  retire unrebound users → delete `platform.identity.local` → set `app.identity.mode=external`), not a
  re-provisioning event that strands every feature foreign key keyed on `User.id`.
- **One role writer per mode.** Local mode: the admin surface writes roles through `UserDirectory`. External
  mode: roles come from claims and `User.roles` is a read-only mirror. There is deliberately **no**
  "role-assignment service that works in both modes" — that would contradict the Feature's own role-sync policy
  (local: roles are ours; external: roles from claims, Feature section 8) and create a two-writers drift trap.
  `<F>AccessPolicy`, `RowScope` and the auth matrix are unchanged.
- **Admin user management lives in `platform.identity.local`** (create, disable, assign roles, set password /
  `mustChangePassword`) and dies with the module — correct, because in external mode those operations belong to
  the provider console. That is the whole HTTP surface: anything more (MFA, sessions, password policy) would
  grow the module that must stay deletable.
- **Multi-write identity work declares `@Transactional` explicitly** (F4): creating a user writes `User` +
  `LocalCredential`, and `UserProvisioning`'s first-request create runs in its own transaction outside the
  caller's read-only one.

**Option 1 as written is not adopted**, because `externalSubject = "local:" + id` clashes with the issuer's
"`sub` = user id" and with US 30 (it yields either a duplicate `User` row or a forgettable mode-specific lookup
step); because "assign roles" split across two modules has no precedence when external claims also carry roles;
and because it has no subject-rebinding path, so migration to an external provider auto-creates brand-new
`User` rows and strands every feature foreign key. Its core shape is kept: `LocalCredential` + `RefreshToken`
in module-owned migrations, a lean `User`, and the admin surface inside the removable module.

Options 2 and 3 are rejected. Option 2 makes `User` depend on local-mode concepts (SRP violation in the
highest-blast-radius contract under the revised D2), cannot hold refresh tokens as columns (they are set-valued
across devices), and degrades US 28 into a manual schema edit that `ddl-auto=validate` will not catch if it is
forgotten. Option 3's premise is wrong — Spring Authorization Server does not own users or credentials (its own
getting-started wires a `UserDetailsService` and `RegisteredClientRepository` by hand) — it replaces the three
endpoints with a full OAuth 2.1/OIDC provider surface (Security 7 even removed the password grant), and it names
a product whose standalone line is frozen at 1.5.x (spring-attic) while its successor lives only in Security 7 /
Boot 4, amplifying F11. Its one insight, that local tokens are consumed exactly like external ones (resource
server + decoder), is already what section 8 specifies.

**Parent document patched:** yes — section 4 (`platform.identity.local` description), section 8 (`User`,
`UserProvisioning`, `UserDirectory`, the credential/account state split, the role-writer rule, the admin
surface, the migration runbook), user story 30, the Guide document table row 08, the ADR-0006/0007 rows,
Steps 3.2 and 3.3, Risk Assessment, and Testing Decisions.

**Findings affected:** F14 is prepared — the failed-attempt counter and temporary lockout land in
`LocalCredential` exactly as its recommended wording assumes, and `User.status` adds the app-level ban.
F17 is unaffected: `CurrentUser.system()` / `runAsSystem` and `RowScope.system()` remain its own question.
F3 is unaffected.

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

**Decision:** Option 4 (alternative, promoted to first-class) — one authorization module per feature: fold the
CRUD `authorize` hook into `<F>AccessPolicy`. (2026-10-03)

The finding was partly auto-resolved by F3: `<F>AccessPolicy` is already in the Feature Module anatomy, and
section 8 already assigns URL rules to public-vs-authenticated and row visibility to `RowScope`. What remained
was the ownership rule — and the analysis exposed a deeper defect in the listed options: the Feature labels
**both** `<F>AccessPolicy` ("Role/action rules") and the CRUD `authorize` hook ("role / action rules") as
role-rule homes. That distinction ("role rules" vs "single-entity role/action rules") is F7's own per-run drift,
one level down, and no generator will honour it.

- **`<F>AccessPolicy` is the feature's single authorization module.** Its contract is two methods:
  `check(Action action, CurrentUser actor, E entityOrNull)` and `rowScope(CurrentUser actor)`. It answers one
  question — *who may do what to which rows* — behind one seam the CRUD base consults exactly once per entry
  point. Role rules, entity-conditioned rules and F3's `RowScope` live together in that one file. The `authorize`
  hook is removed from the CRUD base's hook list.
- **Actor-relative vs actor-independent.** Rules that depend on the caller go in `check`. Actor-independent
  invariants ("a SUPERUSER row is never deletable", the WP-R01-03 shape) go in `beforeDelete` / `validate*`.
- **Deny-by-default is structural at three layers:** URL tail `anyRequest().authenticated()` covering every
  route (opt-out and custom endpoints included); the policy denies when **no rule matches** (fail-closed, no
  silent `isAuthenticated()` default — the WP-R01-03 lesson); and the base cannot construct without a policy
  collaborator, so an endpoint with no policy cannot exist.
- **`@PreAuthorize` / `@Secured` are banned from `features.*`** by an ArchUnit member rule (methods
  `notBeAnnotatedWith`, scoped by package). The six inherited base methods cannot carry per-feature role rules
  without overriding all six (Solution 2's structural flaw), so annotations are wrong on the CRUD path, and a
  forgotten annotation is fail-open. Opt-out features use the same `check`/`rowScope` at each entry point —
  one rule, no exceptions.
- **`@EnableMethodSecurity` gets a bounded purpose:** enabled **once** on the security configuration class, as
  a `platform`-only belt (identity admin, the Token Issuer), never as the feature role matrix. This also removes
  the wording hazard where "used only in opt-out services" could be misread as placing the enabler on a
  deletable `@Service` (the `backend/` failure mode).
- **`RowScope` is unchanged** (F3): the only row-visibility mechanism, ANDed before paging, invisible rows →
  404. The route × role matrix and a **route-coverage test** (`RequestMappingHandlerMapping` → every enumerated
  route appears in the matrix) are the behavioral backstop; the matrix cannot see placement, which is why the
  ArchUnit rule is required.

**US 13 is amended** to "explicit service hooks (validate, apply relations, beforeDelete) plus one declared
access policy" — authorization is declared, not hooked.

**Option 1 (ownership rule only) is viable but not adopted**: it assigns owners yet leaves actor-relative
authorization in two homes, and its literal wording conflates row visibility with the `authorize` hook
("Row-level rules live in `visibleTo` and `authorize`"), contradicting F3's split. Options 2 and 3 are rejected:
Option 2 cannot express per-feature role rules on inherited methods without overriding all six (breaking US 11
and US 13) and is fail-open when an annotation is forgotten; Option 3 contradicts US 23's "URL rules **plus**
method security", violates OCP by making a shared `SecurityFilterChain` a per-feature modification hotspot, and
is fail-open by omission on matcher ordering.

**Parent document patched:** yes — section 4 (ArchUnit rules gain the annotation ban), section 5 (`<F>AccessPolicy`
row), section 6 (the `authorize` hook removed; the policy contract), section 8 (the ownership rule and
`@EnableMethodSecurity`'s bounded purpose), user story 13, the Guide document table rows 04 and 08, Step 3.4,
Step 3.7, Testing Decisions, and the ADR-0005 row.

**Findings affected:** F15 is strengthened — the calibration run (Task 11) should also require the reviewer to
flag `@PreAuthorize` on a CRUD-based `<F>Service` as a finding. F3 is unchanged; F6 is unchanged.

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

**Decision:** Option A — a single blast-radius reorder, decided jointly with F10. (2026-10-03)

**F8's premise dissolved with F1.** It argued that D2 "implies the base exists before the Skill can start from
it". Under the revised D2 the Skill is a code-free convention and never starts from `base-project/` at all, so
the Bug Report's dismissal of Option 3 as "conflicts with D2" referred to the **original** D2 and is void.

**Execution order (F8 + F10):**

```
Phase 1  Decisions and vocabulary          (Steps 1.1–1.4)
Phase 2  The Guide — contracts lead        (Steps 2.1–2.6)  ── gate: blind GC-R design review, 0 🔴 / 0 🟠
Phase 3  The Skill                         (Steps 4.1–4.2)  ← pulled forward
Phase 4  Validation Loop                   (Steps 5.1–5.4)  ← run 01 is a planned discovery run
Phase 5  The Base Project — per area, last (Steps 3.1–3.7)  ← pushed back
Phase 6  Release                           (Steps 6.1–6.4)
```

**Step numbers are stable identifiers and are not renumbered** (they are cited throughout this report and the
Feature). Only the execution order changes. The **Task Breakdown is renumbered 1–13 in execution order**, which
is free because no task documents exist yet.

**One standing decision is amended — D18.** It read "Skill rewritten from scratch after the Guide **and Base
Project** exist." It becomes: *"Skill rewritten from scratch after the Guide exists and its contracts pass the
`GC-R` gate."* The dropped clause's original rationale — a Skill derived from base code — died with the D2
revision. This is surfaced explicitly, which is exactly what F8 said the original reordering failed to do.

**What this fixes:**
- The user's exact stated sequence is honoured: guidelines → skill → create a project → critique → update the
  skill → "at the end try to create a base project". The Skill reaches them after ~5 of 13 tasks instead of 8–9.
- **F10's premise is corrected rather than paid for.** F3–F7 were **cross-module contract gaps** (F5 is
  literally storage × idempotency). Per-area vertical slices — F10's Option 1 — would have torn those apart and
  built them one-sided. F2's blind `GC-R` review, whose stated scope is "interfaces, invariants, error modes
  and **cross-module interactions**", is precisely the instrument that found them with **no code in existence**.
  The Guide's load-bearing layer is therefore already gated before anything is built; its **prose follows the
  code** instead of preceding it.
- Under the revised D2 the never-shipped, never-copied `base-project/` is the **lowest**-blast-radius artifact,
  so building it last and once against converged contracts removes the rework mill where every validation fix
  would otherwise hit Guide prose, base code and Skill files.
- Run 01 exercises **every** contract, including storage and idempotency — coverage F8's Option 2 structurally
  cannot provide, because it defers Task 9 past run 01 and would let the strict exit gate pass while the hardest
  modules sit outside the harness. D15's domain checklist requires "a file upload with idempotency".
- Pure step-level remap: **no second renumbering pass**, which is what the Bug Report's own note warned about.

**Run-01 accounting:** run 01 is a **planned discovery run** inside D15's five-run budget. Findings fix at
source (Guide → Skill). If Task 12's base work later records a Guide correction that changes a convention, the
consecutive-pass count **resets**.

**Not adopted.** F8 Option 1 ("keep the order; confirm as a consequence of D2") keeps a **false rationale** and
front-loads the highest-cost, never-shipped artifact ahead of the highest-leverage one. F8 Option 2 + F10
Option 1 (the Bug Report's original pair) **fight each other** — a block reorder and a per-area dissolve of the
same task blocks — and would renumber twice; they also build coupled contracts one side at a time (the F5 defect
class). F8 Option 3's *shape* survives as a consequence of Option A: `base-project/` is built at the end from
converged contracts, just by construction rather than by promoting a generated project (promotion semantics
were undefined — which run, which domain, how to prune).

**Parent document patched:** yes — the D18 row, the Solution flowchart, section 13 (`base-project/`'s role and
"stable harness" wording), section 15 (the `base` root-cause field is unused in run 01), US 50, the Execution
Order note in Implementation Steps, Step 2.6 (the gate now precedes the Skill), Step 3.7 (now the base's
promotion/conformance gate), the Risk Assessment drift bullet, and the Task Breakdown (renumbered 1–13 in
execution order).

**Findings affected:** **F10 is decided jointly** (see its Decision). F11 is unchanged — the Base Project still
pins its version line; the Skill still does not. F16 is unchanged (auto-resolved).

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

**Decision:** Option 4 (alternative, promoted to first-class) — the Conditional-Write Contract: `ETag` +
`If-Match`, 412/428. (2026-10-03)

Every entity the CRUD base manages carries a **server-owned concurrency token** whose only writer is the
persistence layer (implementation: JPA `@Version`; the Guide states the contract technology-neutrally). The
token is declared in the entity's **first** Flyway migration, so the finding's "migration on every table
later" cost never materialises.

- **One wire channel only: a strong `ETag` derived from the token.** `get`, `create` and `update` return it;
  `update` and `delete` **require** it in `If-Match` (strong comparison — never `W/`). There is **no body
  version**: `<F>Request` keeps its "no `id`, no owner, no server-controlled fields" rule untouched, and the
  MapStruct `update(request, @MappingTarget entity)` contract needs no version-handling rule.
- **Statuses are pinned per RFC 9110.** A stale tag → **412 Precondition Failed** (§13.1.1 names `If-Match`
  as the lost-update prevention mechanism and mandates 412 on failure — Option 1's "mismatch returns 409" is
  an HTTP violation on this channel). A missing `If-Match` → **428 Precondition Required** (fail-closed: no
  write runs unconditionally unless the caller says so). `If-Match: *` is the explicit unconditional write.
  **409 stays reserved for state conflicts** the Feature already defines (the idempotency in-flight lease,
  an already-bound `UserDirectory` subject), each with its own `ProblemDetail` `type` URI (the F5 two-422s
  precedent).
- **Check order preserves F3's no-existence-leak:** authenticate → `policy.check` (403) → scoped load (404
  via `RowScope`) → precondition (412/428) → mutate. An invisible row never surfaces as 412.
- **Persistence-layer backstop:** a flush/commit optimistic-lock failure (the
  `OptimisticLockingFailureException` family Spring's exception translation produces) maps to the **same
  412**, so the compare/flush race and direct writes share one client-visible meaning.
- **Bulk mutation bypasses `@Version`** (HQL/JPQL updates): the contract bans bulk mutation of managed rows
  outside the base — or ANDs the token into the predicate and checks affected rows (F3's ANDed-guard
  pattern), optionally backed by an ArchUnit `@Modifying` ban in `features.*`.

**Rationale:** Option 1's instinct — expose the token, make the client echo it — is the only shape that
prevents the finding's two-admin read-modify-write overwrite. Option 2's hidden `@Version` fires only for
overlapping load-to-flush races inside one transaction, so across HTTP round-trips the second admin still
silently wins; Option 3 would carry BT-R03-07 forward as designed behaviour, against US 4 and the F3/F4/F5
fail-closed precedent. But **Option 1 as written is not adoptable verbatim**: "in the body or `If-Match`" is
unpinned dual-channel drift (the F7 defect class — generated projects would each pick differently), its body
channel contradicts section 5's `<F>Request` contract, and its single 409 is wrong for the `If-Match`
channel. The alternative pins one channel, keeps the Request contract intact, gets the statuses right,
extends the guarantee to `delete` and the compare/flush race, and is fail-closed in the project's structural
grammar (`RowScope` mandatory, digest mandatory, deny-by-default). Under the revised D2 it is pure
contract-level — Guide rules only — and with no clients in existence yet the protocol cost is zero-migration.
It deepens `platform.crud` behind the same six-method interface: callers learn one thing ("echo the `ETag`;
a stale one is 412").

**Option 1 refined to a body-carried version + 409 is viable but not adopted**: it needs the `<F>Request`
"no server-controlled fields" amendment and a never-maps-version mapper rule, and it leaves `delete`
unguarded unless extended. **Option 2 is not adopted** — it does not fix the stated scenario. **Option 3 is
not adopted** — its one true point (late adoption is cheap: one Flyway migration plus one mapped-superclass
field) is an argument for deciding now at zero migration cost, not for documenting data loss.

**Parent document patched:** yes — user story 17 (status codes gain 412/428), section 5 (`<F>` concurrency
token), section 6 (the conditional-write contract, check order and controller `ETag` mapping), section 9
(`PreconditionFailed` / `PreconditionRequired` subclasses; 409 reserved), the Guide document table rows
04/05/07/09, Steps 3.4 and 3.7, and Testing Decisions (the conditional-write protocol tests).

**Findings affected:** none directly. F4's and F5's notes about "the version/`If-Match` design" are now
defined by this decision. F13 (file downloads) is unaffected: downloads are read-only and take no
precondition. F5's two 422s and the 409 idempotency lease are unchanged.

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

**Decision:** Decided jointly with F8 under Option A — the horizontal slice is dissolved by letting **contracts
lead and prose follow implementation**. (2026-10-03)

**F10's premise needs correcting.** It argued that "rules written in bulk describe imagined behavior" and that
F3–F6 were gaps building would have exposed. That is only half true: F3–F7 were **cross-module contract gaps**
— F5 is literally storage × idempotency, F4 is CRUD-base transactions × upload I/O, F3 is query × access — and
per-area vertical slices would have **torn those apart and built them one-sided**. The instrument that actually
found them was F2's blind `GC-R` design review, whose scope is "interfaces, invariants, error modes and
**cross-module interactions**", executed with **no code in existence**.

So the fix is not "interleave by area" (Option 1). It is to split the Guide into its two layers and sequence
them differently:

- **The contract layer leads.** Interfaces, invariants, error modes and cross-module interactions are written
  in Phase 2 and gated by the blind `GC-R` review before anything is built. This is what F10 was really asking
  for, and it is already in place.
- **The prose layer follows.** Each Guide document's descriptive text, "Differs From the Reference Projects"
  (US 5) and "Version Notes" (US 7) are **finalized against the built Base Project code** in Phase 5, per area,
  in dependency order (config/errors → identity/access → query/CRUD → storage/idempotency). Coupled contracts
  (`RowScope` across crud/query/access; the content digest across storage/idempotency) stay in the same slice
  they span.

This removes F10's "two passes over the same document": under the original plan Guide 11 would be written and
validated in Task 5, then rewritten in Task 9. Now it is written once as a contract, reviewed once, and
finalized once.

**Option 1 (interleave by area) is not adopted** because it would build coupled contracts one side at a time —
the very defect class F5 belongs to — and it fights F8's block reorder, forcing two renumbering passes.
**Option 2 (`draft` markers) is absorbed** as a consequence: Guide documents remain `draft` at the prose level
until Phase 5 finalizes them. **Option 3 (keep as is) is rejected** — it is the finding.

**Parent document patched:** yes — jointly with F8: the Execution Order note, Step 2.6 (contract layer gated),
Phase 5's steps (prose finalization per area, in dependency order), Step 3.7 (conformance + promotion gate),
and the Task Breakdown.

**Findings affected:** **F8 is decided jointly.** F11 is unchanged.

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

**Decision:** Option 4 (alternative, promoted to first-class) — a support **policy**, one tested line, and
evidence-scoped Version Notes. (2026-10-03)

ADR-0003 records a **support policy**, not a fossilizable pin. Four parts, all structural:

- **Rules stay version-neutral.** The Guide's contracts (`RowScope`, `<F>AccessPolicy`, `ObjectStorage`,
  `IdempotencyGuard`, `DomainException`) never name a class or API that moves between Boot lines. Rule text
  is behavioural ("the configured JSON mapper", "resource-server `JwtDecoder`"); class and API names live
  only in Version Notes.
- **The floor is "the currently OSS-supported Spring Boot generation"**, not a hard-coded major. Verified
  2026-10-03 (`api.spring.io/projects/spring-boot/generations/...`): 3.5 went OSS-EOL on **2026-06-30**
  (terminal 3.x minor — there is no 3.6), 3.4 on 2025-12-31, so **every Boot 3.x line is past OSS
  end-of-support**; 4.1.x is current GA (OSS until 2027-07-31) and 4.2 GA is scheduled 2026-11-30. The
  original "3+" wording would therefore endorse building new projects on an EOL major — the exact BT-R08-01
  failure (EOL Boot 2.7) that D3's own evidence cites. **US 6 and D3 change wording accordingly and the
  user has confirmed this.** Java 21+ is unchanged (Boot 4's floor is Java 17).
- **The Base Project pins exactly one line: the current GA at build time, decided at Task 9 under the
  policy** (4.1.x as of 2026-10-03). No number is written into the plan today that a future GA could
  invalidate. It is the Guide's **single tested line** and the evidence behind every `verified` note.
- **Version Notes are evidence-scoped.** Each entry is marked `verified on <line>` (citing a Base Project or
  Validation test, a finding ID, or a **version-tagged** documentation URL) or `not verified — current-docs
  lookup at execution time`. A cross-line claim ("3.x does X, 4.x does Y") is written only when both sides
  carry a `verified` mark. The already-planned `guide` validator target (Task 2) enforces the markers and
  reports the `not verified` count in `Guide-Index.md`.

Boot 4's changes are confirmed real and directly relevant (version-tagged sources plus the official
migration guide): Jackson 3 is the default JSON stack (`tools.jackson.JsonMapper`; Jackson 2 `ObjectMapper`
deprecated behind `spring.jackson.use-jackson2-defaults`) — so the finding's Guide-09 `ObjectMapper` example
is factually correct; Spring Security 7 (Boot 4.0 → 7.0, Boot 4.1 → 7.1) removes the `and()` DSL and moves
the Access API; auto-configuration is modularised. Section 8's Security 7.0 citation is now
**baseline-correct** (Security 7 is the Boot 4 line), and its `NimbusJwtDecoder` / JWK-set contract is
unchanged across 6.5→7.0.

**Not adopted.** Option 1 as written ("set the Guide baseline to that major") keeps untested cross-line
version notes as the accepted drift surface and freezes a "that major" floor that fossilizes at 4.2/5.0 — its
pin is absorbed into the policy. **Option 2 is factually invalid as of 2026-06-30**: Boot 3.5.x is terminal
and OSS-EOL, so pinning to it repeats BT-R08-01 exactly, and the deferred Boot 4 notes would be the ones new
projects actually need. Option 3 as written re-creates WP-R11-04; it survives only as the `not verified`
residue inside the evidence-scoped scheme. A dual-tested-line matrix was considered and rejected: with only
one supported generation, a second toolchain tests a dying line at double validation cost (overengineering
for a never-shipped workspace reference).

**Rationale:** the WP-R11-04 failure mode is not "wrong pin" — it is *claims presented as authoritative that
nothing executes*. The policy deletes that class by construction instead of moving it to the next line, and
it matches the Guide's epistemology to the Skill's (F1: look up current docs at execution time), so the two
artifacts stop disagreeing about what is knowable. The floor auto-drops EOL lines and stays correct through
the 4.2 GA and future EOL transitions with no re-decision, which is what "stays valid across Spring Boot
upgrades" (US 6) actually means. It reuses machinery already planned — the `Version Notes` heading (US 7)
and the `guide` validator target (Task 2) — so the cost is marker authoring, not new infrastructure. It also
keeps D3's real rationale intact: never endorse an end-of-support line.

**Parent document patched:** yes — US 6 and US 7, the D3 row, the ADR-0003 row, the Guide format section
(the evidence-scoped `Version Notes` rule), the `guide` validator target, the Guide document table row 01,
section 13 (the Base Project's pinned line and its role as the single tested line), and Step 3.7.

**Findings affected:** none directly. F1 is reinforced — the Skill's "look up current docs at execution time"
rule is now the same epistemology the Guide uses for untested lines. F12–F17 are unaffected.

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

**Decision:** Option 4 (alternative, promoted to first-class) — **frozen evidence packs**: freeze the
exhibits, not the specimens. (2026-10-03)

At review time, before the review is validated, every **cited** file is copied **whole and unmodified** into
`documentation/Docs/Validation/Run-NN/<Domain>/evidence/<project-relative-path>` (line numbers preserved
1:1), and the review cites **only** those committed copies (`evidence/src/.../NoteService.java:42`).
Evidence files are **write-once/frozen** and committed with the review; only then is `validation-runs/`
cleaned. Cross-run claims (run 03 citing run 01's recurring defect) resolve to run 01's frozen evidence
forever.

- **`validation-runs/` stays git-ignored and cleanable** — all three of the Feature's existing statements
  (`:208`, `:738`, `:858`) stay **true** instead of being reversed. The risk bullet is tightened from "may be
  cleaned after each run's review is written" to "is cleaned after each run's review **and its evidence
  pack** are committed".
- **The guard is fail-closed, not a skip.** The `validation` validator target **rejects any citation into
  `validation-runs/`** — it fires while the run is still live, so a forgotten snapshot fails validation
  *before* cleanup can dangle a citation — and skips `evidence/` subtrees when classifying documents (a
  snapshotted generated `README.md` must not be flagged as an unexpected doc file). One citation regime, no
  exceptions: the strongest form of the F11 coherence argument (every validated citation points at committed,
  frozen material).
- **The `CITATION` path prefixes must be extended regardless of option.** `scripts/validate-analysis-docs.py:48`
  currently matches only `backend|BugTracker|wpmanager`, so the planned `validation` target (Task 2) has to
  add `validation-runs/` (and later `base-project/`) either way. The Bug Report's praise of Option 1 as
  "keeps the existing validator rules intact" is therefore illusory — recorded so the rationale is honest.
- Build/test/depth-criterion results are recorded in the run record with citations into the evidence pack,
  so the Exit Gate's numeric comparisons (US 48, US 49) stay anchored.

**Not adopted.** Option 1 (commit the generated sources) is viable but reverses the plan's own resource
posture in three places and permanently commits up to ten generated trees into a docs-first skill-authoring
repo; its safety rests on **never cleaning a tree the plan explicitly invites you to clean**, and an edited
run tree silently shifts cited line numbers (`check_citations` at `:146` checks range-vs-file-length, not
content identity). Its real advantages are absorbed above (the prefix extension) or reduced (whole-tree
diffs become evidence-pack diffs — focused on what findings actually cited, which is the material part;
each run is a fresh generation anyway). **Option 2 is rejected** — its defining mechanism, *skipping*
citation checks for archived runs, is fail-open (the WP-R01-03 permit-by-omission pattern this project
names as a defect) and a citation cannot resolve against a zip or tag under the validator's path-based
logic, making it high-complexity and LSP-breaking (the same citation shape becomes valid or invalid
depending on external archive state). **Option 3 is rejected** — "the check is disabled here" is this
workspace's own defect class (BE-R01-01), it forks two citation regimes inside one validator, and it
guarantees run-03-cites-run-01 can never resolve. A hybrid (commit run 01 fully, snapshot later runs) was
also considered and rejected: two evidence regimes for zero gain, since the traceability matrix maps
findings to rule IDs, not to source trees, and run 01's uncited files have no consumer.

**Rationale:** the deletion test is the deciding instrument — deleting a run's **uncited** files removes
nothing any claim depends on; deleting the **cited** files is exactly what the three listed options each
handle badly. Freezing only the cited files concentrates the evidence-lifecycle complexity at one seam (the
review-writing step) behind a minimal interface ("cited files exist, frozen, at citable paths"), and it turns
the enforcement structural instead of procedural. The one honest loss — browsing full generated trees or
re-running builds after cleanup — has no execution consumer under the F8/F10 order: findings fix at source
(Guide → Skill) and every run regenerates fresh, so the old tree is never executed again.

**Parent document patched:** yes — Affected Systems (`validation-runs/` and `documentation/Docs/Validation/`),
section 15 (the evidence-pack rule in the per-run workflow), the Potential Issues risk bullet, and Testing
Decisions (the `validation` target's prefix extension, the `validation-runs/` citation ban and the
`evidence/` skip).

**Findings affected:** none directly. F15 (reviewer calibration) is unaffected. F11's coherence claim is
strengthened: `guide` and `validation` now both enforce resolvable citations with no skip rules.

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

**Decision:** Option 4 (alternative, promoted to first-class) — **`DownloadTicket` capability tokens**: the
`UploadCoordinator` twin on the read path. (2026-10-03)

One download contract on **every** provider. A feature's download route is an **ordinary entry point**, so
F7's rule covers it with nothing new to remember:

- **Issuance** (non-transactional): scoped load of the owning entity (invisible row → **404**, F3) →
  `policy.check(Action.download, actor, entity)` (deny-by-default) →
  `DownloadTicket issue(StoredFile file, Duration ttl)` on `platform.storage`. `Action` therefore covers the
  CRUD actions **plus `download`**.
- **The ticket is an HMAC capability token** (SHA-256) over `storedFileId | key + expiry`. It is **opaque** —
  it never exposes the `ObjectKey` or any provider URL (the WP-R05-07 class). The signing secret is bound from
  the environment through typed, validated configuration with **no default** (Guide 12, US 26); rotation is a
  stated error mode (outstanding links die on TTL, not on rotation).
- **One platform redemption controller** verifies the MAC and TTL, **re-checks that the object still exists**
  (a use-time re-check a bare presigned URL structurally cannot perform), then serves: **302** to a
  short-lived presigned URL when the adapter can mint one (S3-compatible — the transfer stays client→S3
  directly, so **bandwidth is still offloaded**; the 302 is a redirect, not a proxy) or **stream** through
  `ObjectStorage.open()` when it cannot (local filesystem). Clients treat the URL as opaque.
- **Pinned statuses (one uniform shape, no existence leak):** unknown/tampered ticket → **404**, expired →
  **410 Gone**, deleted object → **404**; constant-time signature comparison.
- **Header policy centralized in the redemption controller:** `X-Content-Type-Options: nosniff`,
  `Content-Disposition: attachment` by default, `inline` only for a safe-media allowlist (blocks
  user-content XSS on the app origin).
- **F4/F5/F9 carry over unchanged:** redemption and streaming are **non-transactional** and never buffer
  `byte[]` (WP-R05-08); downloads are read-only — never idempotency-guarded (F5) and never conditional (F9).
- **Revocation is stated once:** authorization is checked at **issue** time; the TTL is the exposure window.
  Optional actor-id binding for fetch-based clients; `<a href>` / `<img>` / PDF clients cannot attach a Bearer
  header, which is exactly why the ticket is a bearer capability.
- **No new port.** The temporary-read-URL capability extends the **existing** real two-adapter `ObjectStorage`
  seam ("one adapter = hypothetical seam, two adapters = real one").

**Honesty correction recorded.** The finding's "HMAC-signed **links**" wording over-claims the reference:
wpmanager's `FileSigner` (`FileSigner.java:17-55`) signs **file integrity** — `HMAC(checksum + name_version)` —
not download URLs, and wpmanager's actual download path was itself defective (WP-R01-02 anonymous
`/download`, WP-R05-08 `byte[]` heap pressure). The link-signing use is a **designed extension** of the
FileSigner *mechanism* (HMAC-SHA256 + fail-fast `@PostConstruct` secret validation, WP-R01-07), cited under
Step 1.1's "grounded or corrected, always cited" rule — not extracted code.

**Not adopted.** **Option 1 (`DownloadLinks` port) is viable but not adopted**: it unifies issuance yet still
hands clients provider-shaped URLs (S3 presigned embeds the object key and cannot be re-checked or revoked at
use time; the local branch is an app URL), and "issue only after the policy check" is a **caller-side
convention** on a brand-new port — an F7-class drift point, when F7's whole thesis is that deny-by-default is
*structural*. Its local adapter's redemption half also lives outside the interface (a shallow seam). Its one
real advantage — no redirect hop — is a single 302, not a proxy, so the transfer cost is unchanged.
**Option 2 (always stream through the app) is viable only as a baseline plus a reserved offload extension**:
it is the simplest and gives immediate revocation, but makes the app the bandwidth and concurrency bottleneck
for exactly the large-file case wpmanager exists for (plugin/theme distribution), and drops the offload half
of the FileSigner strength. **Option 3 is rejected** — it dissolves F13 instead of resolving it, re-creating
the per-run drift F7 eliminated on the download path, thinning the WP-R01-02 traceability row, and silently
dropping a Strength-to-Keep (violating US 5's explicit-departure rule).

**Rationale:** the deletion test decides it — removing the download entry point scatters signing, expiry,
redirect-vs-stream, header policy and status semantics into every file-bearing feature. Option 4 is the
deepest of the four: one small interface (`issue`) behind which sit authorization, provider differences,
revocation and header policy. It is also the only option that can **re-check existence at redemption**, and
the only one that matches the project's fail-closed grammar (authorization is structural, not a convention).
The 302 preserves Option 1's S3 offload while adding uniformity (the finding's "project A streams, project B
returns S3 URLs" drift is killed outright), key opacity and a real revocation story.

**Parent document patched:** yes — US 53 (new), section 6 (`Action` gains `download`), section 8 (the
public-by-capability redemption route), section 9 (pinned 404/410 ticket statuses), section 10 (the
`DownloadTicket` contract: issuance, ticket, redemption, statuses, header policy, F4/F5/F9 carry-over,
revocation, and the FileSigner grounding note), the Guide document table rows 05/09/10/12, Step 3.5, and
Testing Decisions (the download test category plus the auth-matrix case).

**Findings affected:** none directly. F14 (CORS) is unaffected — the redemption route is public-by-capability,
not a CORS question. F17 (`RowScope.system()`) is unaffected: downloads for scheduled/system work would use
the `system()` scope factory already provided by F3.

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

**Decision:** Option 4 (alternative, promoted to first-class) — **three owner-scoped contracts**. (2026-10-03)

F14 splits along the lines the problem actually splits. Each piece gets exactly one owner and one reason to
change.

- **(a) Credential failure policy — F6's `LocalCredential`, unchanged.** The failed-attempt counter and
  temporary lockout are the app's single failure policy; `User.status` is the app-level ban. Two precision
  points land in Guide 08: the `UserDetails` adapter **must override** `isAccountNonLocked()` / `isEnabled()`
  — they are `default` methods returning `true`, so an adapter that defines `getEnabled()` instead compiles
  fine and silently ignores the flags (the BE-R01-08 trap that would make lockout a no-op) — and
  **bad-credentials and locked-account failures share one uniform `ProblemDetail` shape** (Guide 09), so no
  account enumeration.
- **(b) CORS is one contract with one owner, closing both halves of BE-R01-10.** Allowed origins come from a
  typed `@ConfigurationProperties` record (Guide 12) — never a literal. The security chain consumes the
  **injected** `CorsConfigurationSource` parameter, never calls the bean method directly (BE-R01-10's second
  defect: the reference chain called `corsConfigurationSource()` and ignored the injected source). Start-up
  **fails fast** on `allowedOrigins="*"` with `allowCredentials=true` — which is also Spring's own
  `CorsConfiguration` invariant. Profile-aware defaults: the `local` profile supplies
  `http://localhost:3000` so developer and Validation-Loop projects work out of the box; the production
  default is empty / same-origin. Dev defaults live **only** in the `local` profile. A bean-override test
  asserts the chain honours the injected source, in addition to an origins-from-config test.
- **(c) Per-IP request throttling is deployment-layer ownership, with a named residual risk.** In-app per-IP
  counters are unsound under horizontal scaling; Spring Security ships **no** request-rate limiter (verified
  on the Security 7.0 reference — core resilience is `@Retryable`/`@ConcurrencyLimit`, which throttles
  concurrency, not request rate); and the brief puts deployment/infrastructure out of scope. The Guide states
  the assumption and names **password spraying across many usernames** as an explicit residual exposure, with
  an edge gateway/WAF as the completion path. The matrix records it as **partial** coverage — never as fully
  covered. The request throttle is deliberately **not** folded into `LocalCredential`: IP-keyed request state
  is not credential state and would break F6's deletable-module boundary.
- **Traceability matrix scope becomes mechanical.** Guide 16 / Step 2.6 / US 51 change from "every 🔴/🟠"
  to "every 🔴/🟠 **plus every 🟡/🟢 finding raised by a security-category review** (`BE-R01-*`, `WP-R01-*`,
  `BT-R01-*`)". The category wording matters: BE-R01-10 is internally tagged `configuration`, so a
  severity- or tag-based "🟡 security findings" rule would **miss** it. This catches BE-R01-10, WP-R01-13 and
  BT-R01-08/09/10/11 without dragging in the ~100 non-security 🟡s.

**Not adopted.** **Option 1's in-app per-IP/per-user rate limiter is not mandated.** It is the one mechanism
nothing grounds: Spring has no first-party API (so it is either hand-rolled — not in any reference project —
or Bucket4j/resilience4j, straining the lean-POM rule BE-R07-01), a per-IP in-memory counter is wrong behind a
load balancer while still passing the single-instance Validation Loop, and a `RateLimiter` port with one
in-memory adapter is a hypothetical seam ("one adapter = hypothetical, two = real"). Option 1's **lockout and
CORS halves are adopted** — and CORS is strengthened past the option's wording to cover the injected-source
defect and to add profile-aware defaults. **Option 2 as bare assumption prose is rejected**: "document the
assumption" is not a seam (no port, no adapter, no test surface), it is unfalsifiable inside the Validation
Loop (which runs generated projects against PostgreSQL with no gateway), it names no extension point — unlike
every other out-of-scope item in the Feature, each of which pairs the boundary with a seam (`RowScope`
composition for tenancy, `ClaimsMapper` adapters for providers, an optional replication extension) — and it
leaves BE-R01-10 entirely uncovered. Its one true point, that edge request-rate policy belongs to the
deployment once replicas exist, is adopted as the *ownership* statement in (c), paired with the residual-risk
honesty the bare option lacks. An optional off-by-default framework throttle was also rejected (off-by-default
is fail-open, contradicting the project's fail-closed grammar).

**Rationale:** the three mechanisms layer cleanly at their natural owners — request path, credential record,
app-level ban — so no F6 or F7 decision reopens, and `platform.identity.local` stays deletable (F6) and free
of IP-keyed state. The CORS half is a real reference defect with a testable app-level fix; the throttle half
is genuinely deployment-layer and would otherwise import ungrounded machinery into a convention whose whole
thesis is "grounded or corrected, always cited". Naming the residual risk is what keeps this from being
Option 2's failure mode: the matrix says *partial*, the Guide points at the completion path, and a future
maintainer knows exactly what is and is not covered.

**Parent document patched:** yes — US 51 (matrix scope), section 8 (the Token Issuer's brute-force bullets and
the CORS contract), the Guide document table rows 08/12/16, Step 2.6, Testing Decisions (a "Brute-force
protection and CORS" test category), and Risk Assessment (the password-spraying residual-risk bullet).

**Findings affected:** none directly. F15 (reviewer calibration) is unaffected. F6 is unchanged — the lockout
shape it defined is adopted verbatim. F7 is unchanged.

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

**Decision:** Option 4 (alternative, promoted to first-class) — **a rule-coverage ledger plus a one-time
calibration belt**. (2026-10-03)

The finding's deep failure mode is not "the reviewer is wrong" — it is that **absence of findings is
unverifiable**. `0 🔴 / 0 🟠` is a silent claim today. Two mechanisms, measuring different things:

- **The rule-coverage ledger is load-bearing and runs on every review.** Each validation review carries one
  required section beyond the [[Docs/Analysis-Doc-Conventions]] template: a table of **every applicable Guide
  rule ID** `G<NN>-<MM>`, each row exactly one of `finding <V…ID>` or `checked — no finding` **with a frozen
  evidence-pack citation** (the same citation grammar F12 validates). The `validation` validator target
  gains `check_rule_coverage()`, which diffs the ledger against the Guide's enumerated rule IDs and rejects
  an incomplete ledger, a duplicate or unknown rule ID, or a `checked` row whose evidence path does not
  resolve. The review's Scope declares which Guide documents apply; optional rules (the replication
  extension) are marked as such in the Guide. The Exit Gate gains: *"and its rule-coverage ledger is
  complete (validator-checked)"*.
  - **This fixes the finding's own example.** A run-02 miss now surfaces as a false or incomplete ledger —
    a waved-through `RowScope` bug leaves a `checked` row pointing at the exact line, which the user, a
    later cross-run claim (F12 freezes evidence forever), or the deterministic backstops can contradict.
  - **It subsumes Option 1's intent more strongly.** Via US 51's traceability matrix, each rule's
    `**Evidence:**` line already maps to the 241 ground-truth findings, so "every applicable rule checked"
    transitively means every mapped reference-defect class is checked — deterministically, every run, with
    no planted artifacts.
  - **It absorbs F7's strengthening more robustly than F7 stated.** The `@PreAuthorize`-on-`<F>Service`
    class is caught by the `features.*` annotation ban, which is an ArchUnit rule already inside the gate's
    "all tests pass" clause — regardless of reviewer sensitivity.
- **The calibration belt is a one-time precondition in Task 7.** Before Task 8 begins, the reviewer prompt
  runs **blind** on `backend/` in a fresh context and must report **at least** BE-R01-01, BE-R01-02,
  BE-R02-01 and flag `@PreAuthorize` on a CRUD-based `<F>Service` (F7) as a finding — a **lower bound**,
  never an exact match (an exact-match rule would calibrate toward leniency, the unsafe direction). The
  answer key (`documentation/Docs/backend/Reviews/`) is **banned from the calibration context** or the
  exercise becomes self-graded. The calibration record is Task 7's completion criterion; Task 8 must not
  start until it passes. This proves **recognition ability**, which the ledger cannot measure.

**Placement note.** The finding's "Task 11" refers to the pre-F8/F10 numbering and is **stale**. Under the
renumbered 1–13 execution order, calibration belongs in **Task 7** ("Validation domains, protocol and
reviewer calibration") — which authors the reviewer prompt and therefore owns its test — with Task 8 carrying
a hard precondition. Putting it in Task 8 would be a shape mismatch: Task 8 is explicitly repeatable ("one
Task document per run") and calibration is one-time.

**Not adopted.** **Option 2 (seeded defects per run) is rejected as structurally incompatible**, on five
counts: (1) **Exit Gate arithmetic** — a found seed is 🔴/🟠 "as the code stands", so a correct review
necessarily reports ≥1 🔴/🟠 and "0 🔴 / 0 🟠 twice" fails *by construction*, forcing a rewrite of the
user's own D15 gate; (2) **`Root cause` taxonomy pollution** — a seed is none of `guide`/`base`/`skill`, so
seeds either trigger bogus "fix at root cause" edits or a fourth `seed` value breaks US 48's comparability;
(3) **test-suite collision** — F7's ArchUnit ban on `@PreAuthorize`/`@Secured` in `features.*` means the
mandated seed shape cannot exist in a passing run, and a missing-auth seed fails the auth-matrix tests before
any review runs; (4) it breaks F12's sealed-exhibit invariant (seeding after tests means the evidence pack is
not the tested artifact); (5) **blindness vs answer key** — disclosed seeds turn the review into a scavenger
hunt; undisclosed seeds write false root causes. Its one real virtue (continuous recall measurement) is
retained as the **named fallback**: seeded defects in a *dedicated calibration fixture* if the calibration
belt ever fails — never in run artifacts. **Option 3 (accept the variance) is rejected**: "two consecutive
passes" is (1-p)² per persistent defect — it squares luck when sensitivity *p* is high and is near-guaranteed
to pass when *p* is low, i.e. it doubles the chance of a lucky pass rather than bounding a lenient reviewer;
and it discards F7's strengthening while leaving Risk Assessment's self-grading mitigation overstating its
coverage (it buys independence, not sensitivity).

**Rationale:** the ledger converts an unmeasurable property (LLM sensitivity) into a checkable artifact
property (coverage completeness + citable absence) and reuses infrastructure already mandated — D17's stable
rule IDs, the `guide` target's rule-ID enumeration, US 50's `Root cause: guide (rule ID)` field, F12's frozen
evidence packs and US 51's traceability matrix — at zero extra agent runs. The calibration belt covers what
the ledger structurally cannot: whether the instrument recognizes a defect at all. Together they are
proportionate to a 🟢 Low finding (one table, one validator function, one review run) while making the Exit
Gate's most valuable criterion rest on something checkable rather than assumed.

**Parent document patched:** yes — section 15 (the rule-coverage ledger and the calibration-precondition
bullets), the Exit Gate clause, the Task Breakdown (Task 7 renamed and expanded; Task 8 gains the hard
precondition), Testing Decisions (`check_rule_coverage()` in the `validation` target), and the Risk
Assessment self-grading-bias mitigation.

**Findings affected:** none directly. F2's `GC-R` contract review is a separate instrument and is unchanged.
F7's strengthening is absorbed into both mechanisms. F12's evidence packs are the citation substrate the
ledger depends on.

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

**Decision:** **Auto-resolved** by F1 (2026-10-03). No parent-document patch.

The finding's premise is dissolved. It was written against the original D2, where the Skill "start[s] a
project from the Base Project (copy, rename root package, set configuration)" — so the sample Feature Module
had to have a defined fate (kept, removed, or test-only) and "If it's removed, the platform tests that rely on
it disappear too."

Under F1's revised D2 the Skill is a **code-free convention** that ships no code artifacts and no Spring app;
project initialization is explicitly not its job, and new projects **implement the contracts fresh**. The
Skill therefore has **no handling of the sample feature at all**, and the finding's own example — "a new
project ships a `/api/v1/notes` endpoint nobody asked for, or loses the platform's CRUD and auth-matrix
coverage" — cannot occur: nothing is ever copied.

Verified against the current parent document: no "start a project from the Base Project" language remains
anywhere. The sample Feature Module (`features/note`) exists only as the **Base Project's test fixture**
(section 13: "one sample Feature Module used by the tests"), and `base-project/` is never shipped and never a
copy source. F8/F10 reinforce this by building the Base Project **last**, after the Skill and run 01.

All three listed solutions are therefore moot: Option 1 (move to `src/test`) and Option 2 (the Skill deletes
the sample) both presuppose a copy step that no longer exists; Option 3 (keep as documentation) presupposes
the sample is shipped. A small **residual** point survives but is a Base Project implementation detail, not a
Skill question: whether `features/note` sits in `src/main` or `src/test`. The default — `src/main` as a real
Feature Module the tests exercise through its public interface — matches Testing Decisions' philosophy ("a
good test exercises behavior through a module's public interface") and is settled in Task 11.

**Rationale:** the objective is to decide what the parent document should say. It already says the right
thing; the ambiguity this finding flagged was an artifact of a superseded decision. Patching the parent would
introduce content about a copy step that no longer exists.

**Parent document patched:** no — nothing to change.

**Findings affected:** F1 recorded this outcome in its own Decision ("F16 is auto-resolved"). F8's Decision
likewise notes "F16 is unchanged (auto-resolved)".

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

**Decision:** Option 4 (alternative, promoted to first-class) — **the explicit-actor model**. (2026-10-03)

Four contract sentences, no new mechanism. This is the project's established grammar applied one more time:
F3 made "a scope-less query is unrepresentable"; F17 makes "an entry point without an actor unrepresentable".

- **`CurrentUser.system()` is a first-class sentinel inside the existing `CurrentUser` type** (`userId` = a
  reserved non-UUID literal `system`). There is **no second identity type** and **no `SystemActor` port** (a
  one-adapter hypothetical seam that would break US 29 and force `check`/`rowScope` overloads). The names
  `CurrentUser.system()` and `RowScope.system()` (F3) are kept, so F3/F6/F13 cross-references survive.
- **No `runAsSystem`, no ambient context switch. Every entry point takes its `CurrentUser` explicitly.** The
  HTTP edge (controller, idempotency interceptor) resolves `CurrentUserProvider.current()` **once** and
  passes it down; a scheduled trigger, worker or bootstrap passes `CurrentUser.system()`; an operator "run
  now" passes the operator. This **completes** F7's already-explicit `check(Action, CurrentUser, E)` /
  `rowScope(actor)` parameters rather than adding to them. `CurrentUserProvider` is confined to the HTTP edge
  and adapters by an ArchUnit rule, which also strengthens section 7's statelessness ("no per-request state
  on beans"). No thread-bound set/clear context to leak across pooled `TaskScheduler` threads — the cost
  Spring itself mitigates with `DelegatingSecurityContextRunnable` and friends.
- **Authorization stays deny-by-default and becomes complete by construction.** The system actor flows
  through `check` / `rowScope` like any actor (`rowScope(systemActor)` → F3's `RowScope.system()`). A feature
  with system work **declares narrow system rules in the same `<F>AccessPolicy>` file** as every other actor
  rule; a feature with none simply denies system. Platform housekeeping confined to `platform.*` tables (the
  idempotency cleanup, the replication outbox / `StoredFile` writes) is outside `<F>AccessPolicy>`
  jurisdiction by the existing layering rules and **may not write feature rows** — so all three named cases
  resolve without opening a policy hole.
- **Auditing is total and cannot silently go blank.** `createdBy` / `updatedBy` are the entry-point actor's
  `userId` **string labels** (the `system` sentinel for system work) filled from the mandatory parameter —
  not FKs to `User`, never an ambient read that yields nothing in the job case.

**Not adopted.** **Option 1 (`CurrentUser.system()` + `runAsSystem(...)`) is viable but not adopted**, and as
literally worded it is **incomplete as well as ambient**: it fixes identity *supply* but says nothing about
how `check` treats the system actor, so under F7's deny-by-default every policy must gain system rules anyway
— or someone writes a blanket "allow system" branch, which is Option 2 one level down. `runAsSystem` is a
privilege-adjacent ambient switch whose placement is a forgettable convention (the F7 defect class) and whose
thread-bound context must be set in `try` and cleared in `finally` around pooled scheduler threads, or it
leaks into the next job. Its honest advantages — lower churn, one explicit testable concept, filling F3's
reserved slot — are absorbed above (same sentinel, same `RowScope.system()` twin, same deletion of
wpmanager's `IdempotencyKeyCleanup` / `FileDuplicator` scatter). **Option 2 (explicit bypasses) is rejected**
as the project's own named defect class ("the check is disabled here" — BE-R01-01, WP-R01-03): it
structurally contradicts F3's "there is no entry point that runs unscoped" and F7's "an endpoint with no
policy cannot exist", it orphans F3's already-decided `RowScope.system()` factory, and it is **not even
cleanly implementable** — JPA auditing fires from `AuditingEntityListener` / `AuditorAware` on *every*
persist, so a call-site bypass degrades into a request-aware auditor that silently nulls `createdBy`
(fail-open), a second un-audited persistence path per entity (drift), or inline fake-`Authentication` setup
in each job (i.e. Option 1 under another name). A `SystemActor` port, an `Action.system`, an ambient
"no request → system" fallback and a nullable `createdBy` ("null means system") were each considered and
rejected: the first is a one-adapter seam, the second conflates *who* with *what*, the third is a silent
superuser (WP-R01-03 class), and the fourth is indistinguishable from a bug.

**Rationale:** the explicit-actor model answers *supply, authorization and audit* with one
misuse-unrepresentable rule, where Option 1 answers only supply. It matches the grammar this Feature has
chosen at every prior fork (F3's mandatory `RowScope`, F5's mandatory digest, F9's required `If-Match`,
F7's policy-required-by-constructor), and it holds US 29 *more strictly* — feature code sees a `CurrentUser`
value, never the provider. The one real cost, noisier entry-point signatures, is paid in contracts that are
**not yet written** (F1: the Guide states contracts, projects implement fresh), so the churn is wording, not
migrated code. It also kills the confused-deputy path as a side effect: cross-feature calls must forward the
actor explicitly.

**Parent document patched:** yes — US 29, section 4 (ArchUnit rule: `CurrentUserProvider` confined to the
HTTP edge and adapters), section 8 (`CurrentUser` gains the `system()` sentinel, the explicit-actor rule, the
jurisdiction rule and the audit representation), the Guide document table rows 07 and 08, and Testing
Decisions (a "System and scheduled work" test category).

**Findings affected:** none directly. F3's `RowScope.system()` now has its actor twin. F6's mention of
`runAsSystem` was a *deferral* to F17 ("remain its own question"), not a mandate, so no F6 text changes.
F13's download path is unaffected — system downloads use `RowScope.system()` as already decided.

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
| F5 | Idempotency fingerprinting conflicts with streaming uploads | 🟠 High | Done |
| F6 | The local identity lifecycle is undefined | 🟠 High | Done |
| F7 | Authorization split across four mechanisms; access declaration not in anatomy | 🟡 Moderate | Done |
| F8 | The build order differs from the order the user described | 🟡 Moderate | Done |
| F9 | No concurrency control (lost updates) | 🟡 Moderate | Done |
| F10 | The whole Guide is written before any code | 🟡 Moderate | Done |
| F11 | The version baseline and the Base Project's version line are left open | 🟡 Moderate | Done |
| F12 | Validation-run citations break when generated projects are git-ignored or cleaned | 🟡 Moderate | Done |
| F13 | File download design is missing | 🟡 Moderate | Done |
| F14 | Auth endpoints lack brute-force protection; CORS not designed | 🟡 Moderate | Done |
| F15 | The blind reviewer is never calibrated | 🟢 Low | Done |
| F16 | The Skill's handling of the sample feature is undefined | 🟢 Low | Auto-resolved |
| F17 | There is no identity for scheduled and system work | 🟢 Low | Done |
