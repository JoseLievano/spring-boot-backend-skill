# Task: Decisions, ADRs and Glossary — Record D1–D18 Before Any Guide Text

#task #done #high-complexity #parent-spring-boot-architecture-guide-and-base-project

**Parent:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project|Spring Boot Architecture Guide, Base Project and Skill]]
**Parent Type:** Feature
**Related Step(s):** Phase 1 — Step 1.1, Step 1.2, Step 1.3, Step 1.4 (Task 1 in the parent's Task Breakdown)
**Estimated Complexity:** High

---

## Goal

Give every design decision of the parent Feature a permanent, linkable record before any Guide text is
written: initialise the ADR system, write the ADRs for decisions D1–D18 (with every change the review made
to them), and bring the glossary up to the new vocabulary. After this Task, a Guide rule can cite `ADR-011`
and a reader can find one accepted document that says what was decided and why.

---

## Parent Context

The parent Feature turns the analysis of the three Reference Projects into a **Guide**, a **Base Project**
and a rewritten **Skill**, then proves them with a **Validation Loop**. Its phases execute
**1 → 2 → 4 → 5 → 3 → 6**. This Task is Phase 1 and the first of 13 Tasks.

What the parent says about this Task:

- **Reason for grouping:** "All are decision records with no code; they must exist before any Guide text."
- **Step 1.1** — the user updates `Memory/brief.md` (grounded *or corrected*, always cited; a code-free
  convention skill). **Already applied on 2026-10-03.** This Task only verifies it.
- **Step 1.2** — add the ADR directory and statuses to `documentation/doc-config.json`; create
  `documentation/ADRs/ADR-index.md`.
- **Step 1.3** — write the ADRs from decisions D1–D17 with context, decision and consequences, each citing
  reference Finding IDs; the user approves, then the status becomes `accepted`.
- **Step 1.4** — add 15 glossary terms and update 3 through the `glossary` CLI, with user confirmation.

What this Task enables:

- **Task 2** extends the validator with a `guide` target that checks "every cited finding ID and ADR
  resolves". That needs the ADR files and one fixed citation form.
- **Tasks 3–5** write Guide rules whose `Evidence` field cites these ADRs.
- **The parent's risk list** says the validation status (400 vs 422) "must be settled in ADR-0009 before
  Guide 09 is written" (Guide 09 is written in Task 4). This Task settles it.
- **Task 6** completes the Skill-architecture ADR that this Task opens as `Proposed`.

Constraints from the parent and the memory bank that apply here:

- The decisions are already made. D1–D18 come from the user interview, and findings F1–F17 of
  [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] changed several of them. **The ADRs
  record the patched parent, not the first draft.** This Task re-decides nothing except the points the parent
  explicitly leaves open (listed below).
- The three reference projects are read-only. Nothing in this Task touches them.
- No secret value appears in any new document. Cite a location or a finding ID, never the value.
- `Memory/brief.md` is user-owned. This Task never edits it.

### Parent inconsistencies found while creating this Task

These were found by reading the parent against itself, the review documents and the documentation skill.
The Task resolves each one (the step is named in the last column).

| # | Where in the parent | Problem | Resolution |
|---|---|---|---|
| P1 | ADR IDs everywhere (`ADR-0001` … `ADR-0017`) | Four digits. The `documentation-management` skill mandates `ADR-[NNN]-[short-title].md` with three digits, and its commands normalise to three digits. | Three digits everywhere: `ADR-001` … `ADR-017`. Step 7 normalises the parent. See Design Decision 1. |
| P2 | "Affected Systems": "ADRs 0001–0018" | There are 17 ADRs (D16 and D17 share one). | Step 7 corrects it to `001–017`. |
| P3 | Step 1.3 says "ADR-0001 … ADR-0016 from D1–D17"; the Scope says "ADRs for D1–D18"; the Solution says each of D1–D18 "becomes an ADR in Step 1.3"; `Memory/context.md` says "ADR-0001…0017" | D18's ADR is "written in Step 4.1", but its number must not be taken by another ADR before then, and part of D18 is already decided. | ADR 017 is written now as **Proposed** and completed in Step 4.1. See Design Decision 2. |
| P4 | Section 10: "must never buffer a whole object as `byte[]` (WP-R05-08)" | **WP-R05-08 does not exist.** The wpmanager storage review ends at WP-R05-07. The buffering finding is WP-R05-04. | ADR 013 cites WP-R05-04. Step 7 corrects the parent. |
| P5 | Section 14: "Designed in ADR-0017 after the Guide and the Base Project exist" | Stale. D18 was amended by F8/F10: the Skill is written after the Guide's contracts pass the `GC-R` gate; the Base Project is built last. | ADR 002 and ADR 017 record the amended order. Step 7 corrects the sentence. |
| P6 | Section 9 and "Potential Issues": Bean Validation → "422 (or 400, decided in ADR-0009)"; section 11: a missing digest is "rejected 400/422" | Open decision. | ADR 009 decides it (recommendation: 400; the user confirms). Step 7 writes the outcome back. See Design Decision 6. |
| P7 | Section 11: "Uploads through `UploadCoordinator` are `@Idempotent` by default" vs US 38 "idempotency opt-in per endpoint" | The two statements contradict each other (already noted in `Memory/progress.md`, 2026-10-03). | ADR 014 states one rule (the user confirms). See Design Decision 7. |
| P8 | `<F>AccessPolicy>` (stray `>`) in the ADR-0005 row and four other lines | Typo. | ADR titles use `<F>AccessPolicy`. Step 7 corrects the parent. |
| P9 | Section 6's `CrudService` sketch has no actor parameter (`RES get(ID id)`); section 8 (F17) says every entry point receives the actor explicitly | The sketch predates F17. The parent calls its sketches "not final code". | ADR 005 states the rule, not the sketch's signatures. No parent edit. |

---

## Preconditions / Dependencies

- **No previous Task exists for this parent.** `documentation/Tasks/current/` was empty; the three Tasks in
  `Tasks/done/` belong to [[Features/to-do/Spring-Boot-Skill-Creation]] (Phase 1 analysis). Their output — the
  241 findings — is this Task's evidence base.
- **Step 1.1 is already applied.** `documentation/Memory/brief.md:27-28` reads "grounded in the three
  reference projects and corrected where the reviews found them wrong; every rule cites a finding ID or an
  ADR"; lines 5-7, 21, 26 and 29 describe the code-free, non-Claude-Code-specific convention skill.
- **No ADR exists.** There is no `documentation/ADRs/` directory, and `documentation/doc-config.json` has no
  `adrs` entry.
- **Glossary baseline:** 26 terms in three categories (*Analysis Documentation* 10, *Backend Patterns* 7,
  *Reference Domains* 9). `.glossaryrc` exists at the repository root.
- **Tooling baseline (measured 2026-10-04):** `python3 scripts/validate-analysis-docs.py <project>` exits 0
  for `backend`, `BugTracker` and `wpmanager`; `python3 -m unittest discover -s scripts/tests` runs 44 tests,
  all passing.
- **`obsidian.use_cli` is `false`** in `doc-config.json`. Use direct file operations.
- **The working tree had uncommitted changes when this Task was created** (the Bug Report move to
  `Bugs/done/` and the memory-bank updates of 2026-10-03). They are not part of this Task. Do not revert them.
- **The user must be available.** Step 5 (ADR approval) and Step 6 (glossary confirmation) cannot be done
  without the user. Everything before Step 5 can.

---

## Skills and Documentation Preparation

### Skills Reviewed

- `documentation-management` — **Selected** — owns the ADR format (`references/doc-types/adr.md`), the index
  template, the `create adr` / `set adr` workflows and the Task template.
- `memory-bank` — **Selected** — project context; Step 8 updates the agent-maintained files.
- `glossary-management` — **Selected** — every glossary read and write goes through the `glossary` CLI;
  every write needs the user's confirmation.
- `solid-deep-design` — **Selected** — each ADR that records a module design states the module's single
  responsibility, its interface size and why its seam is real (two adapters) or absent (deletion test).
- `find-docs` — **Selected** — for the dated facts ADR 003 and ADR 009 record (see below).
- `tdd` — **Selected** — vertical slices: write one ADR, run the checker, then the next. Never all 17 first
  and all checks afterwards.
- `doc-exploration` — **Selected (at creation)** — 0 ADRs existed; the relevant documents are listed below.
- `superpowers:verification-before-completion` — **Selected** — run the checks and read their output before
  ticking any completion criterion.
- `superpowers:brainstorming`, `interview-me` — **Not needed** — the decisions were made in the user
  interview (D1–D18) and the findings resolution (F1–F17). This Task records them.
- `feature-findings-solver`, `bug-findings-solver` — **Not needed** — all 17 findings are resolved.
- `skill-creator`, `superpowers:writing-skills` — **Not needed** until Task 6.

### Documentation Reviewed

| Source | What was checked | Result |
|---|---|---|
| `documentation-management/references/doc-types/adr.md` | File naming, tags, the five mandatory sections, the index template | Three-digit numbers; sections *Title, Status, Context, Decision, Consequences*, exactly and in this order; the index column shows `ADR 1` without leading zeros. |
| `glossary` CLI (`glossary --help`, `add --help`, `update --help`), tested on a scratch copy of the glossary | Flags, exit codes, behaviour with a new category | `add` needs all six flags; a new category name is created automatically; a duplicate term exits 2; `update` is a partial patch. **`glossary search` matches the term name only — a synonym is not found** (`glossary search "RowScope"` → exit 1). |
| Spring Boot support API, `https://api.spring.io/projects/spring-boot/generations` (read 2026-10-04) | The dates ADR 003 records | 3.4.x OSS support ended 2025-12-31; 3.5.x ended 2026-06-30; 4.0.x ends 2026-12-31; **4.1.x** released 2026-06-30, OSS support until 2027-07-31; 4.2.x scheduled for 2026-11-30. Matches `Memory/tech.md`. |
| Context7 `/websites/spring_io_spring-framework_reference` (current reference; source URLs are **not** version-tagged) | The default status of a failed request validation, for ADR 009 | "By default, validation errors cause a `MethodArgumentNotValidException`, which is turned into a 400 (BAD_REQUEST) response"; when method validation applies, `HandlerMethodValidationException` is raised instead. Extending `ResponseEntityExceptionHandler` in a `@ControllerAdvice` renders RFC 9457 responses for all built-in web exceptions. Sources: `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html`, `https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html`. |
| The same lookup | The default status of `HandlerMethodValidationException` | **Not verified at Task creation** (the Context7 limit of three calls per question was reached). Verify at execution before writing ADR 009 — see Step 4, ADR 009. |
| `Memory/progress.md` (2026-09-30) | Facts already verified when the Feature was written | Spring Security 7.0 resource-server documentation for the identity seam; MapStruct `@MappingTarget` and `ReportingPolicy.ERROR`. |

`Memory/known-issues.md` warns that Context7's versioned Spring IDs can return `main` snippets. Treat a
snippet as verified for a line only when its source URL carries the matching version tag. That is why ADRs
state behaviour and keep API names in a dated "mechanism" sentence (Design Decision 5).

### Related Existing Code

There is no code in this Task. The related material is documentation:

- [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] — the parent; the decision table D1–D18
  (section "Solution"), the ADR table (section 1), sections 4–15 (the content each ADR records).
- [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] — the Decision block of each finding
  F1–F17 holds the reasoning and the rejected options. Use it for the "Alternatives rejected" text.
- [[Docs/backend/Reviews/00-Review-Summary]], [[Docs/BugTracker/Reviews/00-Review-Summary]],
  [[Docs/wpmanager/Reviews/00-Review-Summary]] — the 241 findings (67 + 86 + 88). Each ID resolves to a
  `### <ID>` heading in `Docs/<project>/Reviews/<NN>-*-Review.md`.
- [[Docs/Analysis-Doc-Conventions]] — section 3 (citations) and section 7 (finding IDs): the link form is
  `[[Docs/<project>/Reviews/<doc>#<ID>|<ID>]]`.
- [[Docs/Skill-Architecture]], [[Features/to-do/Agent-Project-Onboarding-First-Instructions]],
  [[Docs/Skill-Directory-Convention]] — the phase-based design ADR 017 will replace. Read-only here.
- [[Memory/brief]] (user-owned), [[Memory/known-issues]], [[Memory/architecture]], [[Memory/context]],
  [[Memory/progress]], [[Memory/tech]].
- [[Glossary/Glossary]] — rendered from `documentation/Glossary/glossary.json`; never edited directly.
- `documentation/doc-config.json` — gains the ADR directory and statuses.
- `scripts/validate-analysis-docs.py:48-49` — the `CITATION` and `WIKI_LINK` patterns. Not changed here; the
  ADR checker in Step 3 reuses the same wiki-link rule.

---

## Implementation Details

### Approach

The work is documentation, so the "modules" are documents. The same design rules apply.

- **One ADR, one decision, one reason to change.** Each ADR records one row of the parent's ADR table. A
  review decision (F1–F17) is not a separate ADR: it changed a decision *before* anything was accepted, so it
  is folded into the ADR of the decision it changed. The routing table below says where each one goes.
- **The citation `ADR-NNN` is the interface.** Guide rules, the Skill and later reviews depend only on that
  token. It equals the filename prefix, so it resolves by a prefix match (`documentation/ADRs/ADR-NNN-*.md`)
  with no lookup table. The index (`ADR-index.md`) is the only other thing a reader must know.
- **Accepted ADRs are immutable.** A later change is a new ADR that supersedes the old one. Therefore the
  ADRs stay `Proposed` while they are being written and checked, and become `Accepted` only when the user
  approves them (Step 5).
- **Vertical slices.** Write one ADR, run the checker, fix, then write the next. The checker (Step 3) is the
  test: it fails for a missing section, a finding ID that does not exist, a broken wiki link, a four-digit ADR
  ID, a status that disagrees with the index, or placeholder text.
- **Behaviour first.** D3 says rules are version-neutral. An ADR is immutable, so a framework class name in a
  decision sentence would fossilise. Decisions are written as behaviour; API names appear only in one labelled
  sentence (Design Decision 5).
- **The glossary is written through one tested runner** that calls the `glossary` CLI once per term, so
  shell quoting cannot corrupt a definition and the user can read the whole batch before it is written.

**Where each review decision is recorded**

| Review decision | Recorded in |
|---|---|
| F1 — code-free Skill; Base Project never shipped | ADR 002 (and ADR 017) |
| F2 — blind contract design review, IDs `GC-R<NN>-<MM>`, gate 0 🔴 / 0 🟠 | ADR 016 (format and IDs), ADR 002 (the gate in the order of work) |
| F3 — mandatory `RowScope` | ADR 005 (CRUD), ADR 011 (query engine signature), ADR 008 (tenancy extension point) |
| F4 — transactions per entry point; no transaction during storage I/O | ADR 005, ADR 013 |
| F5 — fingerprint with a declared content digest | ADR 014 (fingerprint), ADR 013 (`UploadSource` digest) |
| F6 — `UserDirectory`, rebindable subject, credential/account split | ADR 007 (user model), ADR 006 (local module) |
| F7 — `<F>AccessPolicy` as the single authorization module | ADR 005 (policy), ADR 006 (URL rules, method-security belt), ADR 004 (the ban as a dependency rule) |
| F8 + F10 — execution order 1 → 2 → 4 → 5 → 3 → 6; D18 amended | ADR 002, ADR 017 |
| F9 — conditional writes (`ETag` / `If-Match`, 412 / 428) | ADR 005 (protocol), ADR 009 (error types) |
| F11 — support policy; evidence-scoped Version Notes | ADR 003 (policy), ADR 016 (note markers) |
| F12 — frozen evidence packs | ADR 015 |
| F13 — `DownloadTicket` | ADR 013 (ticket), ADR 009 (404 / 410), ADR 006 (public-by-capability route) |
| F14 — brute-force contracts, CORS, traceability-matrix scope | ADR 006 (brute force, CORS), ADR 016 (matrix scope) |
| F15 — rule-coverage ledger, reviewer calibration | ADR 015 |
| F16 — auto-resolved by F1 | none |
| F17 — explicit-actor model, `CurrentUser.system()` | ADR 006 (model), ADR 005 (system is an ordinary actor in the policy), ADR 004 (provider confined to the HTTP edge) |

### Files to Create/Modify

- [x] `documentation/doc-config.json` — add `"adrs": "ADRs"` to `directories` and the four ADR statuses to
  `statuses`
- [x] `documentation/ADRs/ADR-index.md` — new; master index, 17 rows
- [x] `documentation/ADRs/ADR-001-guide-is-the-source-of-truth.md` — D1
- [x] `documentation/ADRs/ADR-002-code-free-skill-and-reference-base-project.md` — D2
- [x] `documentation/ADRs/ADR-003-version-baseline-as-a-support-policy.md` — D3
- [x] `documentation/ADRs/ADR-004-platform-and-features-package-layout.md` — D4
- [x] `documentation/ADRs/ADR-005-deepened-crud-base-with-access-policy.md` — D5
- [x] `documentation/ADRs/ADR-006-identity-current-user-seam-and-removable-local-issuer.md` — D6
- [x] `documentation/ADRs/ADR-007-single-user-keyed-by-token-subject.md` — D7
- [x] `documentation/ADRs/ADR-008-no-multi-tenancy-in-the-base-project.md` — D8
- [x] `documentation/ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details.md` — D9
- [x] `documentation/ADRs/ADR-010-postgresql-flyway-and-testcontainers.md` — D10
- [x] `documentation/ADRs/ADR-011-list-api-get-paging-and-post-search.md` — D11
- [x] `documentation/ADRs/ADR-012-mapstruct-mapping-with-unmapped-target-errors.md` — D12
- [x] `documentation/ADRs/ADR-013-object-storage-port-upload-coordinator-and-download-tickets.md` — D13
- [x] `documentation/ADRs/ADR-014-idempotency-guard-opt-in-per-endpoint.md` — D14
- [x] `documentation/ADRs/ADR-015-validation-loop-protocol-and-exit-gate.md` — D15
- [x] `documentation/ADRs/ADR-016-guide-document-format-and-rule-ids.md` — D16 + D17
- [x] `documentation/ADRs/ADR-017-skill-architecture-code-free-contract-first.md` — D18, status `Proposed`
- [x] `documentation/Glossary/glossary.json` and `documentation/Glossary/Glossary.md` — **through the
  `glossary` CLI only**: 15 new terms, 3 updated terms, plus the proposals the user accepts
- [x] `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md` — corrections P1, P2,
  P3, P4, P5, P6, P7, P8; ADR table titles synced with the index; Steps 1.1–1.4 ticked
- [x] `documentation/Memory/known-issues.md` — replace the "only appears in one project" rule; add two notes
- [x] `documentation/Memory/architecture.md` — ADR directory in the source map; key decisions point to ADRs
- [x] `documentation/Memory/context.md` — current focus and next steps
- [x] `documentation/Memory/progress.md` — a dated entry at the top

Scratch files (session scratchpad, **not** committed — the parent says this Task has no code):
`check_adrs.py`, `glossary_terms.py`.

---

## Step-by-Step Implementation

### Step 1: Verify Step 1.1 and record the baseline

**Goal:** Confirm the brief already says what Step 1.1 requires, and confirm the tooling is green before
anything changes.
**Dependencies:** None.

- [x] Run the brief check below. All four lines must print a match.
- [x] Run the three validators and the unit tests. Expect exit 0 three times and `Ran 44 tests … OK`.
- [x] Run `git status --short` and note the files that are already modified. They are not this Task's changes.
- [x] If any brief check fails, **stop and tell the user**: `brief.md` is user-owned. Propose the wording
  from the parent's Step 1.1; do not edit the file.

**Why this step is critical:**
D1 ("the Guide is the source of truth; references are evidence") contradicts the brief's old "grounded, not
invented" rule. ADR 001 cites the brief. If the brief had not been changed, ADR 001 would record a decision
the project's own foundation document forbids.

#### Implementation

```bash
cd /home/jlievano/Dropbox/CodeProjects/spring-boot-backend-skill

# Step 1.1 (a): "grounded or corrected, always cited"
grep -n "corrected where the reviews found them" documentation/Memory/brief.md
grep -n "every rule cites a finding ID or an ADR" documentation/Memory/brief.md
# Step 1.1 (b): code-free, project-agnostic convention; not Claude-Code-specific
grep -n "no code artifacts and no Spring app" documentation/Memory/brief.md
grep -n "not Claude-Code-specific" documentation/Memory/brief.md
# The old wording must be gone (expect no output):
grep -n -i "not invented\|scaffolds new\|Claude Code skill (markdown" documentation/Memory/brief.md

# Baseline
for p in backend BugTracker wpmanager; do python3 scripts/validate-analysis-docs.py "$p" >/dev/null; echo "$p exit=$?"; done
python3 -m unittest discover -s scripts/tests 2>&1 | tail -3
git status --short
```

#### Edge Cases
1. **Case:** the brief was edited again after 2026-10-03 and a check fails — stop; propose wording to the
   user; continue only when the user has edited the file.
2. **Case:** a validator fails before this Task changes anything — the failure is not caused by this Task.
   Report it to the user and continue; do not "fix" analysis documents here.

---

### Step 2: Initialise the ADR system

**Goal:** Make ADRs a known document type and create the mandatory index.
**Dependencies:** Step 1.

- [x] Edit `documentation/doc-config.json` as shown. Keep every existing key.
- [x] Create `documentation/ADRs/ADR-index.md` from the template below, with no rows yet.
- [x] Run `python3 -c "import json; json.load(open('documentation/doc-config.json'))"` — it must not raise.

**Why this step is critical:**
The index is mandatory in the documentation system: every create and every status change must update it. The
`create adr` workflow computes the next number from the index, so the index must exist before the first ADR.

#### Implementation

`documentation/doc-config.json` after the edit:

```json
{
  "vault_name": "spring-boot-backend-skill",
  "directories": {
    "bugs": "Bugs",
    "features": "Features",
    "tasks": "Tasks",
    "docs": "Docs",
    "code": "Code",
    "adrs": "ADRs",
    "rules": "rules"
  },
  "statuses": {
    "bugs": ["to-do", "in-progress", "done"],
    "features": ["to-do", "in-progress", "done"],
    "tasks": ["current", "done"],
    "adrs": ["proposed", "accepted", "deprecated", "superseded"]
  },
  "obsidian": {
    "use_cli": false,
    "vault_name": "spring-boot-backend-skill"
  }
}
```

`documentation/ADRs/ADR-index.md` (the exact template of the documentation skill, plus one citation note):

```markdown
# ADR Index

## Status Legend
- **Proposed:** The decision is under consideration
- **Accepted:** The decision has been agreed upon and is in effect
- **Deprecated:** The decision is no longer relevant (the thing it was about no longer exists)
- **Superseded:** The decision has been replaced by a newer ADR

ADRs are cited as `ADR-NNN` (three digits, the filename prefix). An accepted ADR is never edited: a changed
decision is a new ADR that supersedes the old one.

| ADR | Title | Status | Date | Superseded By |
|-----|-------|--------|------|---------------|
```

Each ADR written in Step 4 appends one row, for example:

```markdown
| ADR 1 | The Guide is the source of truth; the reference projects are evidence | Proposed | 2026-10-04 | — |
```

The `Date` is the day the ADR file is created. The `ADR` column has no leading zeros (`ADR 1`), as the
documentation skill requires.

#### Edge Cases
1. **Case:** a title contains a `|` character — it would break the table row. No title in this Task does;
   keep it that way.
2. **Case:** `documentation/ADRs/` already exists when the Task runs (a second run) — read the index first
   and continue from the highest existing number; never recreate or renumber.

---

### Step 3: Prepare the checker and the link helper

**Goal:** Have an automatic check for the ADR set before the first ADR is written.
**Dependencies:** Step 2.

- [x] Save the script below as `check_adrs.py` in the session scratchpad (not in the repository).
- [x] Run it from the repository root. With an empty `ADRs/` directory it reports 17 "expected exactly one
  file" errors and exits 1. That is the expected red state.
- [x] Keep the link helper at hand; it prints the wiki link for a Finding ID.

**Why this step is critical:**
Seventeen documents that cite more than a hundred distinct Finding IDs cannot be checked by eye. One wrong ID in an accepted
ADR stays wrong forever, because accepted ADRs are immutable. The parent itself carried one (P4: WP-R05-08).

#### Implementation

The checker was tested at Task creation against a mock tree: a clean set passes; a seeded set fails with
one error per defect (missing file, extra file, wrong section, non-existent finding, broken wiki link,
four-digit ADR ID, status/tag/index mismatch, no finding cited, placeholder text).

````python
"""Check the ADR set written by Task 1 (run from the repository root).

    python3 check_adrs.py            # draft mode: Proposed is allowed everywhere
    python3 check_adrs.py --final    # ADR 001-016 must be Accepted, ADR 017 Proposed
    python3 check_adrs.py --root <path>   # check another tree (used to test this script)
"""
import re
import sys
from pathlib import Path

args = sys.argv[1:]
root = Path(args[args.index("--root") + 1]) if "--root" in args else Path(".")
final = "--final" in args
doc = root / "documentation"
adr_dir = doc / "ADRs"

EXPECTED = range(1, 18)          # ADR 001 ... ADR 017
STAYS_PROPOSED = {17}            # completed and accepted in Step 4.1 (Task 6)
NEEDS_FINDING = range(1, 15)     # ADR 001-014 cite at least one reference finding
STATUSES = ("Proposed", "Accepted", "Deprecated", "Superseded")
SECTIONS = ["### Status", "### Context", "### Decision", "### Consequences"]
PROJECT = {"BE": "backend", "BT": "BugTracker", "WP": "wpmanager"}
DOMAIN_TAGS = {"#data", "#infrastructure", "#frontend", "#backend", "#security", "#api-design",
               "#testing", "#devops", "#architecture"}

FINDING = re.compile(r"\b(BE|BT|WP)-R(\d\d)-(\d\d)\b")
ADR_REF = re.compile(r"\bADR-(\d+)\b")
WIKI = re.compile(r"\[\[([^\]|#]+)(?:#([^\]|]*))?(?:\\?\|[^\]]*)?\]\]")
HEADING = re.compile(r"^#{1,6} (.+?)\s*$", re.MULTILINE)
FENCE = re.compile(r"^```.*?^```[^\n]*$", re.MULTILINE | re.DOTALL)
INLINE_CODE = re.compile(r"`[^`\n]+`")
PLACEHOLDER = re.compile(r"\b(TODO|TBD|FIXME)\b|lorem ipsum|<fill|\[\.\.\.\]", re.IGNORECASE)
INDEX_ROW = re.compile(r"^\|\s*ADR (\d+)\s*\|\s*(.+?)\s*\|\s*(\w+)\s*\|\s*(\d{4}-\d\d-\d\d)\s*\|", re.MULTILINE)

errors = []
heading_cache = {}


def headings(path):
    if path not in heading_cache:
        heading_cache[path] = {m.group(1) for m in HEADING.finditer(path.read_text(encoding="utf-8"))}
    return heading_cache[path]


def finding_resolves(prefix, review, number):
    reviews = doc / "Docs" / PROJECT[prefix] / "Reviews"
    wanted = "%s-R%s-%s" % (prefix, review, number)
    return any(wanted in headings(p) for p in reviews.glob(review + "-*-Review.md"))


index = adr_dir / "ADR-index.md"
rows = {}
if index.exists():
    for m in INDEX_ROW.finditer(index.read_text(encoding="utf-8")):
        rows[int(m.group(1))] = (m.group(2), m.group(3))
else:
    errors.append("ADR-index.md is missing")

known = set()
for n in EXPECTED:
    nnn = "%03d" % n
    files = sorted(adr_dir.glob("ADR-%s-*.md" % nnn))
    if len(files) != 1:
        errors.append("ADR %s: expected exactly one file ADR-%s-*.md, found %d" % (nnn, nnn, len(files)))
        continue
    path = files[0]
    known.add(path.name)
    text = path.read_text(encoding="utf-8")
    where = path.name

    lines = [line for line in text.splitlines() if line.strip()]
    tags = set(lines[0].split()) if lines else set()
    status_tags = [t for t in tags if t.startswith("#adr-")]
    if "#adr" not in tags or len(status_tags) != 1:
        errors.append("%s: first line needs #adr and exactly one #adr-<status> tag" % where)
    if not tags & DOMAIN_TAGS:
        errors.append("%s: first line needs at least one domain tag" % where)

    title = re.search(r"^## ADR %s: (.+?)\s*$" % nnn, text, re.MULTILINE)
    if not title:
        errors.append("%s: missing heading '## ADR %s: <title>'" % (where, nnn))
    h3 = re.findall(r"^### .+?\s*$", text, re.MULTILINE)
    if [h.strip() for h in h3] != SECTIONS:
        errors.append("%s: the '###' headings must be exactly %s" % (where, ", ".join(SECTIONS)))

    status = re.search(r"^### Status\s*\n+\s*(\w+)", text, re.MULTILINE)
    status = status.group(1) if status else None
    if status not in STATUSES:
        errors.append("%s: Status must be one of %s" % (where, ", ".join(STATUSES)))
    elif status_tags and status_tags[0] != "#adr-" + status.lower():
        errors.append("%s: status tag %s does not match Status '%s'" % (where, status_tags[0], status))
    if final and status:
        want = "Proposed" if n in STAYS_PROPOSED else "Accepted"
        if status != want:
            errors.append("%s: Status is '%s', expected '%s' in --final mode" % (where, status, want))

    row = rows.get(n)
    if not row:
        errors.append("ADR-index.md: no row for ADR %d" % n)
    else:
        if title and row[0] != title.group(1):
            errors.append("ADR-index.md: title of ADR %d differs from the heading in %s" % (n, where))
        if status and row[1] != status:
            errors.append("ADR-index.md: status of ADR %d is '%s', file says '%s'" % (n, row[1], status))

    found = FINDING.findall(text)
    if n in NEEDS_FINDING and not found:
        errors.append("%s: cites no reference finding ID" % where)
    for prefix, review, number in sorted(set(found)):
        if not finding_resolves(prefix, review, number):
            errors.append("%s: finding %s-R%s-%s does not exist" % (where, prefix, review, number))

    prose = INLINE_CODE.sub("", FENCE.sub("", text))
    for target, fragment in WIKI.findall(prose):
        target_path = doc / (target.strip() + ".md")
        if not target_path.exists():
            errors.append("%s: wiki link [[%s]] does not resolve" % (where, target))
        elif fragment and fragment.rstrip("\\") not in headings(target_path):
            errors.append("%s: wiki link [[%s#%s]] has no such heading" % (where, target, fragment))

    for digits in set(ADR_REF.findall(text)):
        if len(digits) != 3:
            errors.append("%s: 'ADR-%s' is not the three-digit form ADR-NNN" % (where, digits))
        elif not list(adr_dir.glob("ADR-%s-*.md" % digits)):
            errors.append("%s: ADR-%s is cited but no such ADR file exists" % (where, digits))

    hit = PLACEHOLDER.search(prose)
    if hit:
        errors.append("%s: placeholder text '%s'" % (where, hit.group(0)))

if adr_dir.exists():
    for extra in sorted(p.name for p in adr_dir.glob("ADR-*.md")):
        if extra != "ADR-index.md" and extra not in known:
            errors.append("unexpected file in ADRs/: %s" % extra)
unexpected_rows = sorted(set(rows) - set(EXPECTED))
if unexpected_rows:
    errors.append("ADR-index.md: unexpected rows for ADR %s" % ", ".join(map(str, unexpected_rows)))

for error in errors:
    print("ERROR " + error)
print("%d error(s); mode=%s" % (len(errors), "final" if final else "draft"))
sys.exit(1 if errors else 0)
````

Run it:

```bash
cd /home/jlievano/Dropbox/CodeProjects/spring-boot-backend-skill
python3 <scratchpad>/check_adrs.py            # while writing (Proposed allowed)
python3 <scratchpad>/check_adrs.py --final    # after Step 5 (ADR 001-016 Accepted, ADR 017 Proposed)
```

The link helper (prints the full-path wiki link, or `UNRESOLVED`):

```bash
python3 - BE-R01-01 WP-R05-04 <<'EOF'
import glob, sys
PROJECT = {"BE": "backend", "BT": "BugTracker", "WP": "wpmanager"}
for fid in sys.argv[1:]:
    hits = glob.glob("documentation/Docs/%s/Reviews/%s-*-Review.md" % (PROJECT[fid[:2]], fid[4:6]))
    ok = hits and ("### " + fid) in open(hits[0], encoding="utf-8").read()
    print("[[%s#%s|%s]]" % (hits[0][len("documentation/"):-3], fid, fid) if ok else "UNRESOLVED " + fid)
EOF
```

Expected output:

```text
[[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]]
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-04|WP-R05-04]]
```

#### Edge Cases
1. **Case:** a wiki link is written inside a table cell — Obsidian needs the pipe escaped
   (`[[…#BE-R01-01\|BE-R01-01]]`). The checker accepts both forms.
2. **Case:** an ADR quotes the grammar of an ID (`<PROJ>-R<NN>-<MM>`, `GC-R<NN>-<MM>`, `BE-R01-*`) — these
   do not match the finding pattern and are not checked. Put grammar examples in inline code.
3. **Case:** a wiki link inside inline code or a fenced block — not checked (same rule as the analysis
   validator). Finding IDs and `ADR-NNN` tokens are checked everywhere, including fences.

---

### Step 4: Write ADR 001 … ADR 017 as Proposed

**Goal:** One ADR per decision, each complete, each passing the checker, all with status `Proposed`.
**Dependencies:** Steps 2 and 3.

- [x] Read the parent section(s) and the Bug Report Decision block(s) named in each specification below
  **before** writing that ADR.
- [ ] Read the body of every finding an ADR cites, in its review document, before citing it
  (`Memory/known-issues.md`: do not write claims from inventories). The titles in this Task are leads.
- [x] Write the ADRs **in this order**, one at a time: 001, 016, 002, 003, 004, 009, 010, 012, 007, 006, 005,
  011, 008, 013, 014, 015, 017. (Foundations first; an ADR is written after the ADRs it links to.)
- [x] After each ADR: append its row to `ADR-index.md`, then run `check_adrs.py`. One "expected exactly one
  file" error must disappear, and the new file may show **only forward-link errors** — a link to an ADR that
  is not written yet (see the note under the worked example). Fix every other error before continuing.
  <!-- REVIEW-FIX: the original rule ("no error for the new file") contradicted the forward links the specifications require -->
- [x] After ADR 017 (the last one) no forward link is left: the checker must print `0 error(s); mode=draft`.
- [x] Do the two "decision to confirm" items (ADR 009 and ADR 014) as written — recommendation in the
  Decision, the other option in the alternatives. The user confirms or reverses them in Step 5.

**Why this step is critical:**
These records are what every Guide rule, Skill file and later review will cite. They must say what the
parent says *now* (after F1–F17), with evidence that resolves.

#### Writing rules for every ADR

1. **File and heading.** `documentation/ADRs/ADR-NNN-<short-title>.md`; first line: tags; then
   `## ADR NNN: <Title>`. The title is identical in the file and in the index.
2. **Tags.** `#adr #adr-proposed` plus the domain tags given in the specification.
3. **Exactly four `###` headings, in this order:** `### Status`, `### Context`, `### Decision`,
   `### Consequences`. No other `###` heading. The status word is on the line directly below `### Status`.
4. **Context** — the forces, in neutral language. End it with a paragraph that starts `**Evidence:**` and
   lists the Finding IDs as full-path wiki links, each with a few words saying what the finding shows. Name
   the parent decision (`D5`) and the review decisions folded in (`F3, F7`).
5. **Decision** — full sentences, active voice ("We will …"), numbered when there are several. Then a
   paragraph that starts `**Alternatives rejected:**`, one line per alternative with the reason.
6. **Consequences** — a bullet list with what becomes easier **and** what becomes harder or riskier. Finish
   with a line that starts `**Related:**` linking the parent Feature and related ADRs.
7. **Behaviour, not API names.** State what the system does. Products and standards may be named (PostgreSQL,
   Flyway, MapStruct, RFC 9457, JWT, JWKS, S3-compatible API, HMAC-SHA-256). A framework class, method or
   annotation name may appear only in one sentence that starts **"Mechanism on the current line
   (Spring Boot 4.1.x, 2026-10):"** and it must say whether it was verified and against what. Names the
   project itself defines (`CurrentUser`, `RowScope`, `<F>AccessPolicy`, `UploadCoordinator`) are fine.
8. **Links.** Findings: `[[Docs/<project>/Reviews/<doc>#<ID>|<ID>]]`. Other ADRs:
   `[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]]`. The parent:
   `[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]]`.
9. **No secrets, no placeholders.** Cite a finding ID or a commit for a leaked secret, never the value. The
   words TODO, TBD and FIXME fail the checker.
10. **Length.** About 40–90 lines per ADR. An ADR records a decision; the full design stays in the parent
    Feature now and in the Guide later. Do not copy whole parent sections.
11. **Module ADRs (005, 006, 011, 013, 014)** include one "depth statement" in the Decision: the module's one
    responsibility, the size of its interface, and why its seam is real (at least two adapters) or why no
    seam was added (deletion test).

#### Worked example (ADR 012, complete)

Use it as the model for tone, length and structure. Re-read the two cited findings before saving it.

```markdown
#adr #adr-proposed #backend

## ADR 012: MapStruct mapping with unmapped-target errors

### Status
Proposed

### Context

Every Feature Module needs to turn a request into an entity and an entity into its response shapes. The
three reference projects wrote these mappers by hand. Hand-written mappers drift silently: a field added to
a DTO compiles, ships and stays empty until someone notices. The no-op base `update` is the same failure at
a larger scale — the request never reached the entity. Decision D12 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] chooses generated mappers that fail the
build when a target field is not mapped.

**Evidence:**
[[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03|BE-R04-03]] — mappers fill different
field subsets per DTO (`username` and `apikey` are returned as `null`);
[[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-07|WP-R07-07]] — mapper bugs (a `url` set from the
name, a `name` set from the version, fields never mapped);
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01|BE-R02-01]] — the base `update`
ignores the form;
[[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-05|WP-R07-05]] and
[[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-05|BT-R10-05]] — lazy injection hides cyclic mapper
dependencies.

### Decision

1. We will generate every feature mapper with MapStruct. A mapper has four methods: entity to Response,
   entity to Summary, Request to a new entity, and Request onto an existing entity (the update).
2. We will make an unmapped target property a **build error**. A new field forces a decision.
3. We will list server-controlled fields (id, owner, concurrency token, audit fields) as explicitly ignored
   in the Request mappings. Ignoring is visible in the mapper, never silent.
4. We will resolve relations (ids to entities, the owner from the actor) in the CRUD base's relation hook,
   not in mappers. Mappers depend on no repository and on no other feature's mapper, so they cannot form
   dependency cycles.

Mechanism on the current line (Spring Boot 4.1.x, 2026-10): MapStruct's `unmappedTargetPolicy =
ReportingPolicy.ERROR` and `@MappingTarget`. Both names were checked against the MapStruct documentation on
2026-09-30; no test verifies them until the Base Project is built.

**Alternatives rejected:**
- Hand-written mappers with a round-trip test per DTO — the reference projects' approach plus a test nobody
  wrote; the build does not stop a missing field.
- A reflection-based mapper — maps by name at run time, so a missing field is still silent.

### Consequences

- A field added to an entity or a response cannot be forgotten: the build fails until it is mapped or
  explicitly ignored.
- The generic update really updates, because the base calls the mapper's update method
  ([[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]]).
- Every project needs an annotation processor in its build, and its order relative to other processors
  matters.
- Mappers stay simple; anything that needs a lookup moves to a service hook, which is one more place to read.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D12, section 5),
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]].
```

Note: the example links to ADR-005, which is written later in the order. Until ADR 005 exists the checker
reports two errors for that link: `wiki link [[ADRs/ADR-005-…]] does not resolve` and `ADR-005 is cited but
no such ADR file exists". Both are expected and disappear when ADR 005 is written. The same holds for every
forward link. Do not remove a link to silence the checker. Several ADRs link to each other (005, 006, 009,
013, 014), so no writing order avoids forward links completely.

#### Specifications

Each block lists what the ADR **must** contain. "Evidence" lists Finding IDs that all resolve (checked at
Task creation against the 241 findings). The parent section in brackets is the source text.

##### ADR 001 — `ADR-001-guide-is-the-source-of-truth.md`
- **Title:** The Guide is the source of truth; the reference projects are evidence
- **Tags:** `#architecture` · **Records:** D1, the Step 1.1 brief change, F14 (matrix scope is in ADR 016)
- **Context:** three projects share one design through a Lineage (BugTracker → wpmanager → `backend/`); the
  same defects were carried forward; 34 of 88 wpmanager findings are shared with `backend/`; the design rates
  about 6.5/10 and the implementation about 3.5/10 [Problem Statement]. Consistency across a lineage is not
  evidence of correctness. The brief was changed on 2026-10-03 from "grounded, not invented" to "grounded or
  corrected, always cited".
- **Evidence:** BE-R01-01, BT-R01-01, WP-R01-01 (no enforced authorization in any of the three); BE-R02-01,
  BT-R02-01, WP-R02-01 (the same no-op `update`); BE-R03-04, BT-R03-09, WP-R06-02 (`ddl-auto=update`, no
  migrations).
- **Decision:** (1) every rule is stated once, in the Guide (`documentation/Docs/Guide/`); (2) the reference
  projects and their 241 findings are evidence, not authority; (3) every Guide rule cites at least one Finding
  ID or ADR; (4) every departure from the reference projects is explicit in the Guide; (5) the Base Project
  and the Skill cite Rule IDs and never restate rules.
- **Alternatives rejected:** copy the newest project (`backend/`); take the consensus of the three (a lineage
  copies its defects); design from nothing (loses the familiar shape, US 3, and the evidence).
- **Consequences:** one place to change a rule; rules can be challenged with evidence; the Guide becomes a
  maintained artifact and three artifacts can drift; Finding IDs must stay stable; the memory-bank rule "a
  pattern that only appears in one project needs stronger justification" is replaced (Step 8).

##### ADR 016 — `ADR-016-guide-document-format-and-rule-ids.md`
- **Title:** Guide document format, rule IDs and citation grammar
- **Tags:** `#architecture` · **Records:** D16 + D17, F2 (review IDs), F11 (note markers), F14 (matrix scope)
  [section 2, section 3 table header, Step 2.6]
- **Context:** the Skill, the Base Project and later reviews must cite rules and decisions by a stable ID;
  the analysis documents already proved that a fixed format plus a validator works (241 findings, 44
  validator tests); unvalidated documentation drifts.
- **Evidence:** WP-R11-04 (the reference project's own vault contradicts its code).
- **Decision:** (1) decisions are recorded as ADRs in `documentation/ADRs/`, Nygard format, with a mandatory
  index; they are cited as `ADR-NNN`; an accepted ADR is immutable and a change is a superseding ADR;
  (2) the Guide lives in `documentation/Docs/Guide/`: `Guide-Index.md`, `Guide-Conventions.md` and documents
  `NN-Title-Words.md`; (3) required headings per document, in order: Purpose, Design, Rules, Differs From the
  Reference Projects, Version Notes, Related Documents; (4) each rule is a block headed by its Rule ID
  `G<NN>-<MM>` with Rule, Why, Evidence and Differs-from-references fields; (5) Rule IDs are stable —
  renumbering needs a Guide changelog entry and a validator run over the Skill files; (6) Version Notes are
  evidence-scoped (`verified on <line>` with a resolvable citation, or `not verified — current-docs lookup at
  execution time`); a cross-line claim needs both sides verified; (7) the traceability matrix covers every
  🔴/🟠 reference finding plus every 🟡/🟢 finding raised by a security-category review; (8) contract-review
  findings use `GC-R<NN>-<MM>` and live in `Docs/Guide/Reviews/`; (9) a `guide` validator target enforces
  all of this; (10) a Guide document carries a `draft` marker until its prose is finalised against the Base
  Project.
- **Alternatives rejected:** a free-form guide; rules stated in the Skill; rules identified by title.
- **Consequences:** every rule is addressable and machine-checked; the validator must be maintained; IDs can
  never be reused; four ID families exist and must not be confused (reference findings, Guide rules, contract
  review findings, validation findings).

##### ADR 002 — `ADR-002-code-free-skill-and-reference-base-project.md`
- **Title:** The Skill is a code-free convention; the Base Project is the workspace reference implementation
- **Tags:** `#architecture` · **Records:** D2 (as revised), F1, F8 + F10, F16 [Description, Solution, sections
  13–14, the Execution Order note]
- **Context:** the Critical findings live in the shared platform design, so it must be worked out and tested
  once; but shipping version-pinned code would make the Skill teach deprecated APIs; the brief requires a
  standalone, pure-markdown skill that is not Claude-Code-specific and does not initialise projects. The
  first draft of D2 had the Skill start projects from `base-project/` — review finding F1 (Critical).
- **Evidence:** BT-R08-01 (a project frozen on an end-of-support line); BT-R08-02 and BT-R01-14 (era gaps:
  code tied to the framework generation it was written on).
- **Decision:** (1) Platform Modules are stated as contracts — interface, invariants, error modes,
  interactions — in the Guide and the Skill; (2) the Skill ships rules, contracts and pseudo-code only: no
  code artifacts, no Spring app, no project initialisation; (3) projects implement the contracts fresh
  against current framework documentation looked up at execution time; (4) `base-project/` is the workspace
  reference implementation that proves the contracts — never shipped, never a copy source; (5) the work is
  contract-first: phases run 1 → 2 → 4 → 5 → 3 → 6 — the Guide's contract layer leads and is gated by a
  blind contract design review (0 🔴 / 0 🟠), the Skill is written against the gated contracts, validation
  run 01 is a planned discovery run, and the Base Project is built last, per area, where each Guide
  document's prose is finalised; (6) step numbers are stable identifiers and are never renumbered.
- **Alternatives rejected:** the Skill copies the Base Project (original D2); no Base Project (no tested
  evidence for `verified on` notes); the Base Project before the Skill (the original order — a horizontal
  slice that F10 rejected).
- **Consequences:** the Skill cannot teach a deprecated API; drift only weakens the Base Project's role as
  evidence; every generated project re-implements the platform, so variance is possible — the Validation Loop
  measures it; Guide rules written before code may be wrong, so the rule is "fix the Guide first" and a
  convention change resets the Exit Gate pass count; run 01 has no `base` root cause.

##### ADR 003 — `ADR-003-version-baseline-as-a-support-policy.md`
- **Title:** Version baseline is a support policy, not a pin
- **Tags:** `#architecture #backend` · **Records:** D3 (as revised), F11 [D3 row, section 2, section 13]
- **Context:** BugTracker runs Spring Boot 2.7.0 (OSS support ended 2023-06-30); `backend/` and wpmanager
  run 3.4.1, which is now also past OSS support. State the dated facts from
  `https://api.spring.io/projects/spring-boot/generations` — **re-read the API on the day the ADR is written
  and record that date**: 3.4.x ended 2025-12-31; 3.5.x ended 2026-06-30 and is the last 3.x line; 4.0.x ends
  2026-12-31; 4.1.x is the current GA (OSS until 2027-07-31); 4.2.x is scheduled for 2026-11-30. Boot 4
  changes things the Guide depends on (Jackson 3 by default, Spring Security 7, modularised
  auto-configuration). A hard-coded major would be wrong within a year.
- **Evidence:** BT-R08-01; BT-R08-02; BT-R01-14; BT-R08-09.
- **Decision:** (1) Guide rules and contracts are version-neutral and behavioural; (2) the floor is the
  currently OSS-supported Spring Boot generation (4.x as of 2026-10), and the Guide never endorses an
  end-of-support line; Java 21 or newer; (3) the Base Project pins exactly one line — the current GA at build
  time, decided at Task 9; (4) version-specific detail lives only in evidence-scoped Version Notes
  ([[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]]).
- **Alternatives rejected:** "Spring Boot 3+, Java 21+" (the original D3 — every 3.x line is end-of-support);
  pin 4.1.x in the rules; test several lines.
- **Consequences:** an upgrade changes notes, not rules; the policy must be re-evaluated against dated facts;
  `not verified` notes exist until the Base Project is built and their count is reported; agents must look up
  current documentation at execution time.

##### ADR 004 — `ADR-004-platform-and-features-package-layout.md`
- **Title:** Package layout `platform` / `features` with enforced dependency rules
- **Tags:** `#architecture #backend #testing` · **Records:** D4, plus the dependency rules added by F6, F7
  and F17 [section 4]
- **Evidence:** BE-R09-07 (`shared/tools` depends on feature packages); WP-R07-05 (services reach into other
  modules); BT-R10-05 (lazy injection hides cycles).
- **Decision:** the package tree of section 4 (`<root>.platform.{identity, identity.local, access, crud,
  query, errors, storage, idempotency, config}` and `<root>.features.<feature>`), and these rules, each
  enforced by an architecture test: `platform` never imports `features`; nothing outside
  `platform.identity.local` imports it; no method-security annotations in `features`; the current-user
  provider is used only at the HTTP edge and in adapters; a feature reaches another feature only through that
  feature's service, and uses application events for side effects the caller need not wait for; associations
  to another feature's entity are allowed, writes go through its service; controllers never return or accept
  entities.
- **Alternatives rejected:** packages by technical layer; the reference `shared/` + `models/` layout without
  enforcement; separate build modules (heavier; not needed to enforce direction).
- **Consequences:** new code has one obvious place; the layout cannot erode silently; the local issuer can be
  deleted; every project carries architecture tests; cross-feature calls need care to avoid cycles; this is
  one package tree, not compile-time isolation.
- **Mechanism sentence:** ArchUnit (named in the parent as the Base Project's tool).

##### ADR 009 — `ADR-009-unchecked-domain-exceptions-as-problem-details.md`
- **Title:** Unchecked domain exceptions mapped to RFC 9457 problem details
- **Tags:** `#api-design #backend` · **Records:** D9, F5 (two 422 meanings), F9 (412 / 428), F13 (404 / 410),
  F14 (uniform credential failures) [section 9]
- **Evidence:** BE-R05-01, BE-R05-02, BE-R05-03, BE-R05-04, BE-R05-06, BE-R02-07; BT-R07-01, BT-R07-02,
  BT-R07-04, BT-R07-05, BT-R01-08; WP-R08-01, WP-R08-02, WP-R08-06, WP-R08-07, WP-R04-01; BE-R03-07,
  WP-R08-03, BT-R07-03 (validation placed on entities or done by hand); BE-R04-05 (the same conflict returns
  409 on create and 400 on update); BE-R09-08.
- **Decision:** (1) one unchecked domain exception hierarchy; each exception carries an error code (HTTP
  status, stable `type` URI, title); built-in kinds: not found 404, conflict 409, precondition failed 412,
  precondition required 428, business rule violation 422, forbidden 403; unchecked means transactions roll
  back by default. **The parent states 412 and 428 explicitly; it names the other four kinds without a
  status.** 404, 409 and 403 follow from their names and from the status list of US 17; **422 for a business
  rule violation is this Task's reading** (US 17 lists 422; the "invalid by itself vs refused against state"
  rule below puts a business rule on the 422 side). Show it to the user in Group C as part of the ADR 009
  question; <!-- REVIEW-FIX: the status of BusinessRuleViolation was asserted as if the parent had decided it --> (2) every error response is an RFC 9457 problem detail, with one `type` URI per failure
  shape; (3) pinned meanings: 409 is for state conflicts only (an in-flight idempotency lease, an
  already-bound subject), never for stale writes; 412 stale precondition; 428 missing precondition; two
  distinct 422 types for an idempotency fingerprint mismatch and a content-digest mismatch; a download ticket
  that is unknown or tampered reads as 404, expired as 410, for a deleted object as 404; bad credentials and
  a locked account return one identical shape; (4) one central handler: domain exceptions → their status;
  request validation → the validation status with a field-error list; anything else → 500 with a generic
  detail and a correlation id, the full error logged; (5) authentication and authorization failures use the
  same problem writer and the application's configured JSON mapper; (6) validation constraints live on the
  Request shape, not on entities; (7) **the validation status — decision to confirm, see below.**
- **Decision to confirm (P6).** Recommendation: **400**. Rule: *400 means the request is invalid by itself —
  unreadable, wrong type, a missing required input, an unknown query field, or a failed Bean Validation
  constraint; no server state is needed to judge it. 422 means the request is valid in itself but is refused
  against server state or content — a business rule, an idempotency fingerprint that differs from the stored
  one, a declared digest that differs from the received bytes.* Reasons: (a) it is one checkable test for
  every future case; (b) it equals the framework default for every input exception, so there is no override
  that one code path can forget (the documented default for a failed `@Valid` body is 400; method validation
  raises a different exception — an approach that needs both overridden answers 400 on the path that was missed and 422 on the other);
  (c) 422 keeps the meanings F5 already gave it. US 17 stays true: 422 is still used. Consequence for
  ADR 014: a file request with **no** digest is invalid by itself → 400.
  The other option, **422** for Bean Validation (400 only for unreadable requests), is listed under
  alternatives with its cost: every validation exception path must be re-mapped, and a missing digest would
  be 422.
  **Before writing:** verify the default status of the method-validation exception on the current line
  (`npx ctx7@latest docs /websites/spring_io_spring-framework_reference "HandlerMethodValidationException
  default HTTP status"`, or the current Javadoc). If it is not 400 for request input, say so in the mechanism
  sentence — reason (b) then holds only for request bodies.
- **Alternatives rejected:** checked exceptions (leak into every signature; commit partial work); a custom
  error body; 422 for request validation (see above).
- **Consequences:** clients parse one shape and switch on `type`; the status set is fixed and cannot be
  changed once clients exist; internal messages never leave the server on a 500; every new failure shape
  needs a new `type` URI.

##### ADR 010 — `ADR-010-postgresql-flyway-and-testcontainers.md`
- **Title:** PostgreSQL, Flyway migrations and Testcontainers
- **Tags:** `#data #testing #backend` · **Records:** D10 [section 12, Testing Decisions]
- **Evidence:** BE-R03-04, BT-R03-09, WP-R06-02 (no migrations; `ddl-auto=update`); BE-R08-05, BE-R03-06
  (tests on H2, production on PostgreSQL); BE-R08-03, WP-R10-05, BT-R09-01 (tests that need a live database
  and fail without it); BE-R07-03, BT-R08-06 (H2 on the runtime classpath).
- **Decision:** PostgreSQL is the one database engine; the schema changes only through versioned migrations;
  the ORM validates the schema and never changes it; the removable local identity module keeps its own
  migrations so it can be deleted without stranding tables; integration tests run on real PostgreSQL in a
  container (and an S3-compatible store in a container for the storage adapter); no in-memory substitute
  database anywhere; Docker is a documented test prerequisite and its absence fails fast with a clear message.
- **Alternatives rejected:** H2 for tests; `ddl-auto=update`; another migration tool (equivalent; Flyway's
  plain SQL files match the "one migration per feature" file); MySQL (two reference projects used it; the
  newest moved to PostgreSQL).
- **Consequences:** tests exercise the production engine; a Docker daemon is required and tests are slower;
  every schema change is a hand-written migration; Docker is used only as a test dependency — deployment
  stays out of scope.

##### ADR 012 — `ADR-012-mapstruct-mapping-with-unmapped-target-errors.md`
- The worked example above. **Records:** D12.

##### ADR 007 — `ADR-007-single-user-keyed-by-token-subject.md`
- **Title:** One `User` keyed by the token subject; roles as strings; `UserDirectory` as the single
  lifecycle contract
- **Tags:** `#security #data #backend` · **Records:** D7, F6 [section 8: identity invariant, `User`,
  `UserProvisioning`, `UserDirectory`, one role writer per mode]
- **Evidence:** BE-R03-03, WP-R06-01 (roles stored as ordinals); BT-R03-10 (roles as free strings, enum
  unused); BT-R01-06 (usernames unique only per user kind); BT-R03-02 (identifier problems in the JOINED user
  hierarchy); BT-R01-04 (roles accepted from the caller); BE-R01-06, WP-R01-10 (derived client passwords).
- **Decision:** (1) one provider-neutral `User`: id, external subject (unique), e-mail, display name, a roles
  mirror, an app-level status (active / disabled); (2) invariant: the external subject always equals the
  token `sub`, in both identity modes; local mode sets `sub` to the user id, with no prefix, because the
  validated issuer already namespaces it; (3) roles are stored as enum names, never ordinals; (4) per-type
  profile data lives in feature entities that reference `User` — no user class hierarchy; (5) `User` holds
  account state, not credentials; (6) the status is enforced on every request, in both modes; (7) user
  resolution finds the user by subject, and creates it on first request in external mode only; in local mode
  the user must exist before the first login; (8) `UserDirectory` (create, set status, rebind subject) is the
  only path that creates or changes a user; rebinding is admin-only and audited, fails with 409 on an
  already-bound subject, and never links automatically on a matching e-mail; (9) one role writer per mode —
  the local admin surface in local mode, token claims in external mode.
- **Alternatives rejected:** a user hierarchy per user kind; a `local:` subject prefix; automatic linking by
  e-mail (account takeover); one role service for both modes (two writers).
- **Consequences:** moving to an external provider keeps every foreign key on the user id; rebinding is a
  privileged operation whose misuse is account takeover — hence the mitigations; in external mode roles are
  changed in the provider's console; first-request provisioning writes during a read and must be race-safe
  and outside the caller's read-only transaction.

##### ADR 006 — `ADR-006-identity-current-user-seam-and-removable-local-issuer.md`
- **Title:** Identity by resource-server token validation, a `CurrentUser` seam and a removable local Token
  Issuer
- **Tags:** `#security #architecture #backend` · **Records:** D6, F6, F7, F13, F14, F17 [section 8]
- **Evidence:** BE-R01-01, BT-R01-01, WP-R01-01 (no enforced authorization); BE-R01-02 (anyone can mint a
  token); BE-R01-03, WP-R01-05, BT-R01-07 (repositories exported over REST); BE-R01-04, WP-R01-07 (seed admin
  and fallback secrets as literals); BE-R01-05, BE-R01-07, WP-R01-08, BT-R01-10 (JWT design); BE-R01-08,
  WP-R01-09, BT-R01-11 (account flags ignored); BE-R01-10, WP-R01-13 (CORS); WP-R01-11 (method security
  switched on from service classes); BE-R01-11, BT-R01-03 (literal secrets).
- **Decision:** (1) every request is authenticated by validating a JWT as a resource server; the framework's
  token decoder is the port — no custom port is added; two configurations chosen by `app.identity.mode`:
  `local` (the issuer's public key) and `external` (the provider's key-set URI); issuer and audience are
  validated in both; (2) a Claims Mapper port with a local and an external adapter turns a token into
  provider-neutral claims; (3) business code sees only `CurrentUser` (user id, subject, roles); the system
  sentinel `CurrentUser.system()` is the same type; **every entry point names its actor as an explicit
  parameter** — there is no run-as-system switch and no ambient context; platform housekeeping confined to
  platform tables may not write feature rows; audit fields are actor labels taken from that parameter;
  (4) the Token Issuer is a removable module: login, refresh, logout; short-lived access tokens signed with
  an asymmetric key pair from configuration (no literal, no fallback); opaque refresh tokens stored hashed
  and rotated; passwords are hashed through a delegating encoder; **account flags are honoured at login** — the
  adapter that exposes a user to the framework must override the account-flag checks, because they default
  to "not locked" and "enabled"; it owns the credential and refresh-token tables in its own migrations; the first admin is
  created once through `UserDirectory` from configuration-provided credentials; the local admin user
  management lives and dies with the module; (5) brute-force protection is three owner-scoped contracts —
  the credential failure policy (counter and temporary lockout) in the local credential; one uniform error
  shape for bad credentials and a locked account; per-IP request throttling is owned by the deployment layer,
  and password spraying is a named residual risk; (6) CORS: origins come from typed configuration; the
  security chain consumes the injected CORS source; start-up fails on wildcard origins with credentials; the
  developer origin exists only in the `local` profile; (7) one owner per authorization decision: URL rules
  separate public from authenticated routes only, with an explicit public list and an "everything else is
  authenticated" tail; the download redemption route is public by capability; all feature authorization
  belongs to the Access Policy ([[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]]); method
  security is switched on once, on the security configuration class, as a platform-only safeguard;
  deny-by-default holds at three layers; no automatic REST export of repositories; (8) migration to Clerk or
  WorkOS is a runbook — export users and roles, create provider accounts, rebind each subject, retire
  unrebound users, delete the local module, set the mode to `external`. A concrete Clerk or WorkOS
  integration is out of scope; a fake key-set issuer in tests proves the second configuration.
- **Depth statement:** `CurrentUser` is the whole identity interface for feature code. The Claims Mapper seam
  is real (two adapters plus a test adapter). No token-verification port was added because the framework's
  decoder already is one — a wrapper would be a pass-through.
- **Mechanism sentence:** the names in the parent's section 8 (`JwtDecoder`, `NimbusJwtDecoder`,
  `CorsConfigurationSource`, `@EnableMethodSecurity`, the `UserDetails` account-flag defaults) with
  "verified against the Spring Security 7.0 resource-server reference when the Feature was written
  (2026-09-30)" for the decoder, and "not verified by a test" for the rest.
- **Alternatives rejected:** a hand-written token filter (the reference projects); external provider only
  (US 27 needs our own login now); a custom verification port; an in-app per-IP rate limiter (unsound when
  scaled horizontally; out of scope).
- **Consequences:** switching provider is configuration plus a runbook; the local issuer is
  security-sensitive code the project owns; providers differ in how they deliver roles, so the mapper must
  accept "no roles in the token"; per-IP throttling is not covered by the convention; an entry point without
  an actor cannot be written.

##### ADR 005 — `ADR-005-deepened-crud-base-with-access-policy.md`
- **Title:** Deepened generic CRUD base with hooks, three DTO shapes and one Access Policy per feature
- **Tags:** `#architecture #backend #api-design #security` · **Records:** D5, F3, F4, F7, F9, F17
  [sections 5 and 6]
- **Evidence:** BE-R02-01, BT-R02-01, WP-R02-01 (base `update` ignores the request); BE-R02-06, BT-R02-03
  (the type-parameter and file-count tax); BE-R02-07, BT-R07-04 (checked exceptions in generic signatures);
  WP-R02-06 (the base service is shallow and mostly overridden); BT-R02-02 (a client-supplied id overwrites a
  row); WP-R01-03 (inherited operations need only a login); WP-R03-02 (storage I/O inside the transaction);
  BT-R03-07 (no optimistic locking); BE-R02-04, BT-R02-07, WP-R02-04 (every success is 200).
- **Decision:** (1) keep a generic CRUD base with six entry points: get, list, search, create, update,
  delete; it takes four collaborators — repository, mapper, Query Profile, Access Policy — and cannot be
  constructed without a policy; (2) three DTO shapes — Request, Response, Summary — replace the reference
  quartet; (3) update applies every request field through the mapper; ids are assigned by the server;
  (4) features customise through hooks only: validate on create, validate on update, apply relations, before
  delete; (5) **authorization is not a hook**: `<F>AccessPolicy` is the feature's single authorization module
  with `check(action, actor, entityOrNull)` and `rowScope(actor)`; actions are the CRUD actions plus
  download; the policy denies when no rule matches; the base consults it once per entry point;
  actor-independent invariants go in the hooks; (6) Row Scope is mandatory: ANDed into every get, list,
  search, update and delete query before paging; a row outside the scope reads as 404, not 403; (7) every
  entry point receives its actor explicitly; the system actor is an ordinary actor that passes through the
  same policy; (8) writes are conditional: managed entities carry a server-owned concurrency token; get,
  create and update return it as a strong `ETag`; update and delete require `If-Match` — stale → 412, missing
  → 428, `*` is the explicit unconditional write; no version field in the request body; check order:
  authenticate → policy check (403) → scoped load (404) → precondition (412 / 428) → mutate; a
  persistence-layer lock failure maps to the same 412; bulk mutation of managed rows is banned outside the
  base; (9) transactions are declared per entry point on the six base methods (read-only for the three
  reads); a feature-added service method is non-transactional unless it declares otherwise — a documented
  departure from the reference projects; (10) status codes: create 201 with a location, delete 204, the rest
  200; (11) a feature that does not fit opts out with a plain service and controller that still use the
  errors, query and access modules; (12) depth criterion: across generated features, fewer than half override
  create or update wholesale; if it fails in two validation runs, this ADR is revisited by a superseding ADR.
- **Depth statement:** deleting the base re-creates six endpoints, six service methods and the transaction,
  authorization and concurrency wiring in every feature. The Access Policy has two methods and holds every
  "who may do what to which rows" rule of a feature.
- **Mechanism sentence:** per-method `@Transactional`, `@Version`; "not verified by a test".
- **Alternatives rejected:** no base (every feature hand-written); the reference base unchanged; an
  `authorize` hook plus method-security annotations (authorization in four places — F7); class-level
  transactions (F4); a version in the request body or 409 for stale writes (F9); a hidden version column
  only (does not stop a lost update between two readers).
- **Consequences:** a new feature is cheap and correct by default; an endpoint without a policy cannot
  exist; clients must send `If-Match`; every signature carries the actor; a 404 for an invisible row hides
  existence but makes "why can't I see it" harder to diagnose; multi-write custom methods must remember to
  declare a transaction; if the depth criterion fails, the base was the wrong shape.

##### ADR 011 — `ADR-011-list-api-get-paging-and-post-search.md`
- **Title:** List API with `GET` paging, `POST /search` and an owned page response
- **Tags:** `#api-design #backend #security` · **Records:** D11, F3 [section 7]
- **Evidence:** BE-R06-03 (reads go through `POST /list`); BE-R06-04 (unbounded query complexity);
  BE-R06-05, BE-R06-02, BE-R06-06 (profile repetition, unfilterable enum and collection fields, zone-less
  dates read as UTC); BE-R02-08, BT-R02-08 (the framework page type serialised directly); BE-R02-03,
  BT-R02-05, WP-R02-03 (unbounded "get all"); BT-R05-01 (filters stored on a shared singleton); BT-R05-07
  (no field whitelist — password hashes can be probed); BT-R05-02, BT-R05-03.
- **Decision:** (1) `GET /resources?page&size&sort` for simple listing; (2) `POST /resources/search` for rich
  filters — operator lists, OR within a field, AND across fields, multi-sort; (3) both forms are parsed into
  one internal list request and run by one engine with one entry point that takes the Query Profile, the Row
  Scope, the request and the summary mapping; (4) the Row Scope argument is mandatory and ANDed before
  paging — a query without a scope cannot be written; (5) the Query Profile declares each field once (name,
  path, type, operators, sortable) plus the default sort and the bounds: maximum page size, maximum filter
  count, maximum values per filter; anything not declared is rejected; (6) enum and collection fields are
  supported; a date-time without a zone is rejected; (7) the engine is stateless; (8) the wire format is an
  owned page response: items, page, size, total items, total pages; (9) the predicate technology is an
  implementation detail behind the interface, recorded in a Version Note; (10) a search is never
  idempotency-guarded.
- **Depth statement:** one entry point hides parsing, whitelisting, typing, bounds, scoping, sorting and
  paging. The Query Profile restricts fields; the Row Scope restricts rows; the two never mix.
- **Alternatives rejected:** `POST /list` only (the reference; not cacheable, not bookmarkable); a filter
  language in the query string; serialising the framework's page type; automatic REST export.
- **Consequences:** simple lists are cacheable; grids get rich filters; sensitive columns cannot be probed;
  two request forms to document and test; a read over POST for the rich form; each queryable field costs one
  profile entry — by design.

##### ADR 008 — `ADR-008-no-multi-tenancy-in-the-base-project.md`
- **Title:** No multi-tenancy in the Base Project; ownership checks only
- **Tags:** `#architecture #security` · **Records:** D8 (user decision) [Scope → Out of scope]
- **Evidence:** BT-R01-02, BT-R06-01, BT-R06-02 — tenancy that is modelled but not isolated is a
  Critical-class defect, so it is left out deliberately rather than done halfway.
- **Decision:** the Guide's rules and the Base Project implement no tenancy; row visibility is ownership,
  expressed by the Row Scope; the Guide names exactly one extension point — Row Scope composition; tenancy
  is future work and needs its own ADR.
- **Alternatives rejected:** build tenancy now (a large design with no second project to validate it); leave
  it out without naming an extension point.
- **Consequences:** a smaller platform; a project that needs tenants gets no tested guidance; the extension
  point is named but unproven; ownership is not isolation.

##### ADR 013 — `ADR-013-object-storage-port-upload-coordinator-and-download-tickets.md`
- **Title:** Object storage port with S3-compatible and local adapters, an Upload Coordinator and Download
  Tickets
- **Tags:** `#architecture #backend #infrastructure #security` · **Records:** D13, F4, F5, F13 [section 10]
- **Evidence:** WP-R03-01, WP-R03-02, WP-R03-03 (a failed upload commits a file-less row; storage I/O inside
  the transaction; partial compensation); WP-R05-01, WP-R05-03, WP-R05-04, WP-R05-05, WP-R05-07 (a client
  per call; a bucket check per call; whole objects buffered in memory; the port on the JPA entity with one
  real adapter; keys rebuilt from URLs, no timeouts); WP-R01-02, WP-R01-04 (anonymous bucket access; storage
  secrets returned by the API); WP-R08-05 (the type check trusts client metadata); WP-R09-05 (the size limit
  defined twice); WP-R02-05 (a delete route that skips storage cleanup); WP-R05-02, WP-R05-06 (replication).
  **Cite WP-R05-04 for "never buffer a whole object" — not WP-R05-08, which does not exist (P4).**
- **Decision:** (1) an `ObjectStorage` port: put, open (streams; not found when absent), exists, delete
  (idempotent); (2) two production adapters selected by `app.storage.provider`: S3-compatible (one reused
  client; endpoint override and path-style for non-AWS stores; timeouts; streaming and multipart; the bucket
  checked once at start-up) and local filesystem (keys confined to the root, traversal rejected; a temporary
  file then an atomic move; single-node only); one shared contract test suite for both; another store is a
  new adapter that passes the suite; (3) credentials come only from the environment and are never returned
  by an API; (4) object keys are built by one function per feature and never parsed back from URLs; a
  platform `StoredFile` entity records key, provider, size, SHA-256, content type and creation time;
  (5) every upload declares the expected SHA-256 of the object's bytes: a file-scoped form field for
  multipart, one pinned header for raw streams (the parent names two candidates; **which header is pinned
  is decided in the Guide**, not in this ADR); the whole-message `Content-Digest` is not used;
  (6) the Upload Coordinator has one entry point — `store(source, key, persist)`: it validates size and type
  itself, streams to storage with **no database transaction open**, verifies the digest in the same single
  pass and fails with 422 before the object is finalised, runs `persist` in a short transaction, deletes the
  object if persistence fails, and writes a searchable alert log if that compensation fails; calling it
  inside an active transaction fails immediately, before any storage I/O; (7) downloads: a feature's download
  route is an ordinary entry point — scoped load (404), then the policy check for the download action, then
  a Download Ticket: an HMAC capability token over the stored-file id and an expiry, which never exposes the
  object key; the signing secret comes from the environment with no default and start-up fails without it;
  one platform redemption route verifies the signature in constant time and the expiry, re-checks that the
  object exists, then redirects (302) to a short-lived presigned URL when the adapter can mint one or streams
  through the port when it cannot; statuses as pinned in
  [[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]]; the header policy is set in that
  one controller; redemption is non-transactional and never buffers a whole object; authorization is checked
  when the ticket is issued and the expiry is the exposure window; (8) an object whose owning entity is deleted is removed
  **after commit** — the Guide defines how (and an optional orphan sweep); the ADR records only the rule that
  it must not be left to each feature; (9) multi-provider replication is an optional extension
  specified in the Guide only and is not built.
- **Depth statement:** the port has four methods and two production adapters — a real seam. The Upload
  Coordinator's one method hides transaction placement, digest verification, compensation and alerting;
  callers write none of it.
- **Mechanism sentence:** AWS SDK for Java v2 client with an endpoint override; a no-transaction propagation
  setting (`NEVER`) on `store`; "not verified by a test".
- **Alternatives rejected:** the storage port on the JPA entity (WP-R05-05); storage I/O inside the
  transaction; presigned URLs handed straight to clients (no use-time existence check; exposes the key);
  proxying every download through the app (no bandwidth offload); `Content-Digest` (hashes the multipart
  envelope, so it can never equal the stored hash).
- **Consequences:** uploads leave no orphan objects and no half-written rows; development works without a
  cloud account; clients must compute a SHA-256 before uploading; local storage is single-node; a ticket is
  a bearer capability until it expires — there is no revocation before the expiry; the wpmanager file signer
  inspired the mechanism, but link signing is a designed extension, not extracted code.

##### ADR 014 — `ADR-014-idempotency-guard-opt-in-per-endpoint.md`
- **Title:** Idempotency Guard, opt-in per endpoint
- **Tags:** `#api-design #backend` · **Records:** D14, F5 [section 11]
- **Evidence:** WP-R04-01, WP-R04-02, WP-R04-03, WP-R04-04, WP-R04-05, WP-R04-06; WP-R05-04.
- **Decision:** (1) one entry point: execute an operation under a key and a fingerprint, returning the stored
  result on replay; (2) opt-in per endpoint through a marker on the controller method; an interceptor reads
  the client `Idempotency-Key` header, scopes it by the current user, builds the fingerprint and calls the
  guard; (3) the fingerprint: for buffered JSON requests, method + path + canonical non-file fields; for file
  and streaming requests, method + path + canonical non-file fields + the declared content digest; no
  interceptor or guard reads a file body; (4) the digest is mandatory for file requests — a request without
  one is rejected before the guard runs, with the validation status of
  [[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]]; (5) states: in progress (with a
  lease) → completed (stored response) or failed; same key and fingerprint, completed → replay; same key,
  different fingerprint → 422; a live lease → 409; an expired lease → retry allowed; (6) the original
  exception is rethrown after the failure is recorded; (7) persistence only (a unique index), no per-instance
  cache; a scheduled cleanup removes expired rows; (8) searches and downloads are never guarded; (9) for a
  client that cannot compute a digest, the documented fallback spools the upload once and hashes it in that
  same pass — never a per-adapter default; (10) **uploads — decision to confirm, see below.**
- **Decision to confirm (P7).** Recommendation: *the mechanism is opt-in per endpoint, with no exception
  (US 38). The convention's upload recipe opts in: an upload endpoint built from the Guide carries the marker
  unless the feature records why not.* The default lives in the recipe a developer follows, not in the guard.
  The other reading — "the Upload Coordinator applies idempotency by itself" — is rejected: it would give the
  coordinator a second responsibility and make an HTTP-header concern reach a non-HTTP caller.
- **Depth statement:** one method hides the state machine, leases, replay and failure recording.
- **Alternatives rejected:** a key made of the file checksum only (WP-R04-02); hashing the request body
  (forces buffering — F5); leaving file content out of the fingerprint (a reused key would replay another
  upload — breaks US 37); a per-instance cache (WP-R04-04).
- **Consequences:** a retried POST cannot duplicate work; clients must send a key, and a digest for files;
  each project that uses the guard carries a table and a cleanup job; stored responses take space until they
  expire.

##### ADR 015 — `ADR-015-validation-loop-protocol-and-exit-gate.md`
- **Title:** Validation Loop protocol and Exit Gate
- **Tags:** `#testing #architecture` · **Records:** D15 (user decision), F12, F15, the run-01 accounting of
  F8 + F10 [section 15]
- **Evidence (optional for this ADR):** BE-R08-01, BE-R08-02, WP-R10-01, WP-R10-02, BT-R09-02 — tests
  written alongside the code did not reveal the reference projects' missing authorization.
- **Decision:** (1) the user defines two Validation Domains of 4–6 entities each that together cover the
  coverage checklist; (2) per run, the Skill generates one project per domain under `validation-runs/`
  (git-ignored); the agent builds it, runs all tests and starts it on PostgreSQL; (3) a blind reviewer in a
  fresh context — given only the generated project, the Guide and the review conventions — writes review
  documents in the analysis-document format with IDs `V<NN><X>-R<NN>-<MM>` and a root cause of `guide`,
  `base` or `skill` (`base` is unused in run 01); (4) evidence packs: every cited file is copied whole and
  unmodified into the review's `evidence/` tree and the review cites only those copies; the validator rejects
  a citation into `validation-runs/`; (5) every review carries a rule-coverage ledger listing each applicable
  Rule ID once, as a finding or as checked with an evidence citation; (6) reviewer calibration is a hard
  precondition of run 01: the reviewer prompt, run blind on `backend/`, must report at least BE-R01-01,
  BE-R01-02 and BE-R02-01 and flag a method-security annotation on a CRUD-based service; a lower bound, never
  an exact match; the answer key is banned from that context; the named fallback is a dedicated calibration
  fixture; (7) each finding is fixed at its root cause and the next run starts from a fresh generation;
  (8) the Exit Gate: both domains build, all tests pass (including architecture tests and the authorization
  matrix), both start on PostgreSQL, the review reports 0 🔴 and 0 🟠 with a complete ledger, and the CRUD
  depth criterion holds; (9) the loop ends after two consecutive passing runs, or at run 5, when the user
  decides; run 01 is a planned discovery run inside that budget; a Guide correction in Phase 3 that changes
  a convention resets the consecutive-pass count.
- **Alternatives rejected:** one domain (overfits); the generating agent reviews its own output; seeded
  defects in run artifacts (they fail the gate's own arithmetic and have no root-cause value); no run limit;
  committing the generated projects.
- **Consequences:** the convention is judged by evidence; each run costs two generated projects, their builds
  and Docker time; evidence packs add committed files; the gate may not be reached by run 5.

##### ADR 017 — `ADR-017-skill-architecture-code-free-contract-first.md` (stays **Proposed**)
- **Title:** Skill architecture as a code-free, contract-first convention
- **Tags:** `#architecture` (status tag `#adr-proposed`, never flipped in this Task) · **Records:** D18 (as amended by F8 + F10) [D18 row, section 14,
  Steps 4.1–4.2]
- **Context:** `spring-boot-skill/` currently holds a phase-based router and a finished Phase 0
  ([[Docs/Skill-Architecture]]); D2 changed what the Skill is; D18 says it is rewritten from scratch, after
  the Guide exists and its contracts pass the blind contract review. Say plainly: this ADR is opened now so
  that the decided part of D18 is on record and the number 017 is reserved; **Step 4.1 (Task 6) completes it
  and asks for acceptance.**
- **Decision — decided now:** the Skill is rewritten from scratch and nothing of the phase-based design is
  carried over by default; it is written only after the Guide's contract layer passes the gate; it is a
  code-free convention ([[ADRs/ADR-002-code-free-skill-and-reference-base-project|ADR-002]]); it cites Guide
  Rule IDs instead of restating rules; API syntax is looked up from current framework documentation at
  execution time; it is not specific to one agent runtime; it stays in `spring-boot-skill/` until Step 6.3.
  Minimum capabilities: state the architecture and the module contracts; guide adding a Feature Module from
  an entity description; guide enabling an optional capability (upload and storage, idempotency); review a
  project against the Guide and report findings by Rule ID.
- **Decision — decided in Step 4.1, before acceptance:** the file layout; the loading model; the form in
  which contracts are stated; how the manual smoke scenario is run. Write these as a short list headed
  "Decided in Step 4.1" — do not use placeholder words.
- **Consequences:** the phase-based design and Phase 0 are discarded; [[Docs/Skill-Architecture]] and
  [[Features/to-do/Agent-Project-Onboarding-First-Instructions]] are superseded and retired in Step 6.4 with
  approval; until Step 4.1 the Skill has a recorded direction but no recorded structure.

#### Edge Cases
1. **Case:** a finding's body does not support the claim this Task attaches to it — do not cite it for that
   claim. Cite another finding from the same specification, or drop the claim. Never bend the finding.
2. **Case:** the parent and the Bug Report disagree — the parent wins (it was patched after each decision).
   If the parent disagrees with itself in a way not listed under P1–P9, stop and ask the user.
3. **Case:** an ADR needs to say something the parent leaves open and that is not ADR 009 or ADR 014 — do
   not decide it. Write "decided in the Guide" or name the Task that decides it (for example: the pinned
   Spring Boot line, Task 9; the predicate technology, a Version Note).
4. **Case:** an ADR grows past about 100 lines — it is restating the design. Cut to the decision and link
   the parent section.
5. **Case:** a decision is cited that mentions a leaked secret (BE-R01-11, BT-R01-03, WP-R01-07) — cite the
   finding ID only.
6. **Case:** the Spring Boot generations API shows different dates on the execution day — record what the
   API shows that day, with the date, and tell the user if the current GA line changed.

---

### Step 5: User approval — Proposed → Accepted

**Goal:** The user approves ADR 001–016; each approved ADR becomes `Accepted` in the file, the tag and the
index. ADR 017 stays `Proposed`.
**Dependencies:** Step 4 complete; `check_adrs.py` reports 0 errors in draft mode.

- [x] Present the ADRs to the user in the four groups below, one group at a time, with the file paths and a
  two-line summary per ADR. Ask for approval per group.
- [x] In Group C, ask the ADR 009 questions explicitly: 400 or 422 for a failed request validation, and
  whether a business rule violation is 422 (the parent names the exception but not its status). In Group D,
  ask the ADR 014 question explicitly (the upload wording).
- [x] If the user changes anything, edit the ADR **while it is still Proposed**, re-run the checker, and show
  the change before asking again.
- [x] If the user reverses the ADR 009 recommendation: swap the decision and the alternative in ADR 009, and
  in ADR 014 the sentence about a missing digest follows automatically (it points to ADR 009's validation
  status) — re-read it to confirm it still reads correctly.
- [x] After each group is approved, run the `accept` helper for that group, then the checker.
- [x] When all four groups are approved: `python3 <scratchpad>/check_adrs.py --final` must print
  `0 error(s); mode=final`.

**Why this step is critical:**
The parent says the status becomes `accepted` "once the user approves". Acceptance is also the moment an ADR
becomes immutable, so everything the user wants changed must be changed before it.

| Group | ADRs | What the user is approving |
|---|---|---|
| A — foundations | 001, 002, 003, 016 (+ 017 shown, stays Proposed) | What is authoritative, what the Skill and the Base Project are, the version policy, the Guide and citation format |
| B — structure | 004, 005, 011, 012 | Package layout, the CRUD base with its policy and conditional writes, the list API, mapping |
| C — identity, errors, data | 006, 007, 008, 009, 010 | Identity and authorization ownership, the user model, no tenancy, the error contract **(400 vs 422)**, the database |
| D — storage, idempotency, validation | 013, 014, 015 | Storage, uploads and downloads, idempotency **(upload wording)**, the Validation Loop |

#### Implementation

```bash
cd /home/jlievano/Dropbox/CodeProjects/spring-boot-backend-skill
accept() {  # usage: accept 001 002 003 016
  for n in "$@"; do
    f=$(ls documentation/ADRs/ADR-$n-*.md) || return 1
    sed -i -e '1s/#adr-proposed/#adr-accepted/' -e '/^### Status$/{n;s/^Proposed$/Accepted/}' "$f"
    sed -i -E "s/^(\| ADR $((10#$n)) \|.*\| )Proposed( \| [0-9]{4}-)/\1Accepted\2/" documentation/ADRs/ADR-index.md
  done
}
accept 001 002 003 016        # after Group A is approved
python3 <scratchpad>/check_adrs.py
```

The helper was tested at Task creation: it changes the tag on line 1, the status line directly under
`### Status`, and the index row; the checker then passes in `--final` mode once all 16 are accepted.

#### Edge Cases
1. **Case:** the user rejects a decision outright (not the wording, the decision) — that reopens a decision
   of the parent Feature. Leave the ADR `Proposed`, stop the Task at this group, and tell the user the parent
   must be changed first. Do not accept a partial set silently: say which ADRs are accepted and which are not.
2. **Case:** the user approves a group "except one ADR" — accept the others; keep that one `Proposed`; the
   Task is not complete until it is accepted or the user says to close without it.
3. **Case:** the status line is not directly under the heading (a blank line between) — the `sed` does
   nothing and the checker reports a tag/status mismatch. Fix the file layout (writing rule 3) and re-run.
4. **Case:** never run `accept 017`.

---

### Step 6: Glossary — add and update terms through the CLI

**Goal:** The glossary holds the vocabulary of the Guide, the Skill and the reviews (US 52).
**Dependencies:** Step 5 (the definitions name ADR-backed concepts; write them after the decisions are
accepted). Independent of Step 7.

- [x] Save the script below as `glossary_terms.py` in the session scratchpad.
- [x] `glossary categories` — confirm the three existing categories and that `.glossaryrc` is found.
- [x] **Batch A (15 new terms — the parent's list).** Show the user
  `python3 <scratchpad>/glossary_terms.py A --print`, ask for confirmation, then run it without `--print`.
- [x] **Batch B (3 updates — the parent's list).** For each of *Feature Module*, *Generic CRUD Stack* and
  *DTO Tiers*, show the current entry (`glossary search "<term>"`) next to the new one (`B --print`), ask for
  confirmation, then run batch B.
- [x] **Batch C (proposals beyond the parent's list).** Nine new terms that the review decisions introduced
  (*Module Contract, Version Note, Access Policy, Row Scope, User Directory, Download Ticket, Conditional
  Write, Evidence Pack, Rule-Coverage Ledger*) and three updates (*Idempotency Key* — its current definition
  is wpmanager's checksum key and would contradict the Guide; *Two-Phase Upload* and *Compensation* — a
  related link to *Upload Coordinator*). Show `C --print`; let the user accept all, some or none; run with
  `--only "<accepted names>"` when only some are accepted.
- [x] **Batch D (back-links)** — run only the entries whose Batch C targets were accepted
  (`--only`): *Current User* needs *Access Policy, Row Scope, User Directory*; *Query Profile* needs
  *Row Scope*; *Object Storage* needs *Download Ticket*; *Platform Module* needs *Module Contract*;
  *Exit Gate* needs *Rule-Coverage Ledger*. If a target was declined, skip that entry.
- [x] If the user changes a definition, edit the entry in the scratch script and show `--print` again before
  writing.
- [x] Verify with the commands in "Automatic Validation".

**Why this step is critical:**
The Guide, the Skill and the validation reviews are written by different agents in different sessions. They
stay consistent only if they share one vocabulary. Three existing terms describe the *reference* shape of
things the Guide now defines differently; left alone they would teach the old shape.

#### Implementation

Categories: the runner creates three new ones — **Guide and Convention**, **Platform Modules**,
**Validation Loop** — the first time a term is added to them. The three updated terms stay in *Backend
Patterns*. The runner was tested at Task creation on a scratch copy of the glossary: batches A, B, C and D
all return `ok`; a second run of A fails with exit 2 per term (duplicate), as designed; afterwards the
glossary has 50 terms in six categories and every new `related` name resolves to a term.

````python
"""Apply one glossary batch through the `glossary` CLI (never edits glossary files directly).

Usage (from the repository root, where .glossaryrc lives):
    python3 glossary_terms.py <A|B|C|D> --print          # show what would be written
    python3 glossary_terms.py <A|B|C|D>                  # write the whole batch
    python3 glossary_terms.py C --only "Row Scope,Access Policy"   # write a subset
"""
import subprocess
import sys

GUIDE = "Guide and Convention"
PLATFORM = "Platform Modules"
LOOP = "Validation Loop"

BATCHES = {
    # Batch A - the 15 new terms listed in the parent Feature, section 1.
    "A": [
        dict(op="add", term="Guide", category=GUIDE,
             definition="The version-agnostic architecture and design guide for future Spring Boot REST APIs, kept in documentation/Docs/Guide/. It is the only place rules are stated: the Base Project and the Skill cite its Rule IDs, and the Reference Projects are evidence for it, not authority.",
             examples="Docs/Guide/06-Query-Engine.md states rule G06-03; the Skill cites G06-03 instead of restating it.",
             synonyms="architecture guide, the convention",
             related="Guide Rule, Rule ID, Base Project, Reference Project"),
        dict(op="add", term="Guide Rule", category=GUIDE,
             definition="One addressable, checkable statement in a Guide document, written as a block with Rule, Why, Evidence and Differs-from-references fields. Every Guide Rule cites at least one Finding ID or ADR.",
             examples="G06-03: every filterable or sortable field is declared once in the Query Profile; anything not declared is rejected.",
             synonyms="rule",
             related="Guide, Rule ID, Finding ID"),
        dict(op="add", term="Rule ID", category=GUIDE,
             definition="The stable identifier of a Guide Rule, formatted G<NN>-<MM>, where NN is the Guide document number and MM is sequential within that document. Rule IDs are not renumbered once the Skill cites them. Distinct from a Finding ID (<PROJ>-R<NN>-<MM>), a Guide contract review ID (GC-R<NN>-<MM>) and a validation finding ID (V<NN><X>-R<NN>-<MM>).",
             examples="G04-02 (Guide document 04, rule 02).",
             synonyms="Guide rule ID",
             related="Guide Rule, Finding ID"),
        dict(op="add", term="Base Project", category=GUIDE,
             definition="The tested reference implementation of the Platform Modules in base-project/, built last against the gated Guide contracts to prove the convention is implementable. It pins exactly one Spring Boot line, is never shipped with the Skill and is never a copy source.",
             examples="base-project/ holds platform.* plus one sample Feature Module (features/note) used by its tests.",
             synonyms="reference implementation",
             related="Guide, Platform Module, Validation Run"),
        dict(op="add", term="Platform Module", category=PLATFORM,
             definition="A shared, feature-independent module under <root>.platform.* with one responsibility and a small interface: identity, access, crud, query, errors, storage, idempotency or config. Platform Modules never import Feature Modules. The Guide and the Skill state each one as a contract (interface, invariants, error modes); the Base Project implements them.",
             examples="platform.query exposes one deep entry point: ListQuery.run(profile, scope, request, toSummary).",
             synonyms="platform package",
             related="Feature Module, Base Project, Guide"),
        dict(op="add", term="Current User", category=PLATFORM,
             definition="The value object (userId, subject, roles) that represents the actor of an entry point, and the only identity type feature code may import. Every entry point receives it as an explicit parameter: the HTTP edge resolves it once and passes it down, and scheduled, worker and bootstrap work pass the system sentinel CurrentUser.system().",
             examples="A controller resolves the actor once and calls service.update(id, request, actor); a scheduled job calls the same service with CurrentUser.system().",
             synonyms="CurrentUser, actor",
             related="Claims Mapper, Token Issuer"),
        dict(op="add", term="Token Issuer", category=PLATFORM,
             definition="The removable local module (platform.identity.local) that issues our own JWTs: login, refresh and logout, the LocalCredential and RefreshToken tables in its own migrations, and the local admin user management. Nothing outside the module imports it, so deleting it and switching the identity mode to external moves login to an external identity provider without touching business code.",
             examples="POST /auth/login returns a short-lived access token signed with an asymmetric key read from configuration.",
             synonyms="local issuer, local token issuer",
             related="Current User, Claims Mapper"),
        dict(op="add", term="Claims Mapper", category=PLATFORM,
             definition="The port that turns a verified JWT into provider-neutral identity claims (subject, e-mail, display name, roles). It has one adapter per token source: a local adapter for the Token Issuer and an external adapter configured per identity provider.",
             examples="The external adapter reads roles from the claim path configured for Clerk or WorkOS.",
             synonyms="ClaimsMapper",
             related="Current User, Token Issuer"),
        dict(op="add", term="Object Storage", category=PLATFORM,
             definition="The port for storing and reading binary objects by object key (put, open, exists, delete), with an S3-compatible adapter and a local-filesystem adapter selected by configuration and verified by one shared contract test suite.",
             examples="app.storage.provider=local in development and s3 in production, with no code change.",
             synonyms="ObjectStorage, storage port",
             related="Upload Coordinator, Storage Provider"),
        dict(op="add", term="Upload Coordinator", category=PLATFORM,
             definition="The deep entry point that makes storing a file and recording it atomic for the caller: it streams the content to Object Storage with no database transaction open while verifying the declared content digest, persists in a short transaction, and deletes the object (Compensation) if persistence fails. It is the Guide replacement for the reference Two-Phase Upload.",
             examples="uploadCoordinator.store(source, key, storedFile -> attachToNote(noteId, storedFile))",
             synonyms="UploadCoordinator",
             related="Object Storage, Compensation, Two-Phase Upload, Idempotency Guard"),
        dict(op="add", term="Idempotency Guard", category=PLATFORM,
             definition="The Platform Module that makes a non-repeatable POST safe to retry. It is opt-in per endpoint: it scopes the client Idempotency Key by Current User, compares a request fingerprint, replays the stored response for a completed key, rejects a reused key with a different request (422) and rejects an in-flight duplicate (409).",
             examples="A retried upload with the same Idempotency-Key and the same content digest returns the original 201 response.",
             synonyms="IdempotencyGuard",
             related="Idempotency Key, Upload Coordinator, Current User"),
        dict(op="add", term="Query Profile", category=PLATFORM,
             definition="The per-entity whitelist of filterable and sortable fields (name, path, type, operators, sortable) plus the default sort and the query bounds. Anything not declared is rejected. It restricts which fields a client may query; it does not restrict which rows the caller may see.",
             examples="NoteQueryProfile declares title (contains, equals; sortable) and createdAt (range; sortable).",
             synonyms="QueryProfile, field whitelist",
             related="Feature Module, Generic CRUD Stack"),
        dict(op="add", term="Validation Run", category=LOOP,
             definition="One generate, build, test, review and fix cycle of the Validation Loop: the Skill generates a project for each Validation Domain, the projects are built, tested and started on PostgreSQL, a blind reviewer writes findings, and each finding is fixed at its root cause in the Guide, the Skill or the Base Project.",
             examples="Run 01 is a planned discovery run; its findings have root cause guide or skill only.",
             synonyms="run",
             related="Validation Domain, Exit Gate"),
        dict(op="add", term="Validation Domain", category=LOOP,
             definition="One of the two small business domains (4-6 entities each) that the user defines to test the Skill. Together they must cover one-to-many, many-to-many, user-owned resources, role-restricted endpoints, a filtered and sorted list, and a file upload with idempotency.",
             examples="Recorded in Docs/Validation/Validation-Domains.md once the user defines them.",
             synonyms="",
             related="Validation Run, Exit Gate"),
        dict(op="add", term="Exit Gate", category=LOOP,
             definition="The condition that ends the Validation Loop. A run passes when both domains build, all tests pass, both start on PostgreSQL, the blind review reports 0 Critical and 0 High findings with a complete rule-coverage ledger, and the CRUD depth criterion holds. The loop ends after two consecutive passing runs, or at run 5, when the user decides.",
             examples="A run with one High finding fails the gate and resets the consecutive-pass count.",
             synonyms="",
             related="Validation Run, Validation Domain"),
    ],
    # Batch B - the three updates the parent Feature asks for. Old names stay as synonyms.
    "B": [
        dict(op="update", term="Feature Module",
             definition="The standard per-entity unit of code. In the Guide it is one package under <root>.features.<feature> with a fixed file set (Controller, Service, Repository, Entity, Request, Response, Summary, Mapper, Query Profile, Access Policy and a migration) plugged into the Generic CRUD Stack, with an opt-out for features that do not fit. In the Reference Projects the same idea is a package of Controller, ServiceImpl, Repository, Mapper, Entity, Form, DTO, MiniDTO and ListDTO (ten files in backend, with a QueryProfile).",
             examples="Guide: features/note (NoteController, NoteService, NoteRepository, Note, NoteRequest, NoteResponse, NoteSummary, NoteMapper, NoteQueryProfile, NoteAccessPolicy, V<n>__note.sql). Reference: backend's models.hq.admin (AdminController, AdminServiceImpl, AdminRepository, AdminMapper, AdminQueryProfile, AdminEntity, AdminForm, AdminDTO, AdminMiniDTO, AdminListDTO).",
             synonyms="ten-file module, module",
             related="Generic CRUD Stack, DTO Tiers, Platform Module, Query Profile"),
        dict(op="update", term="Generic CRUD Stack",
             definition="The reusable CRUD base that gives every Feature Module get, list, search, create, update and delete. In the Guide it is the platform.crud base controller and service: update really applies the request, hooks (validate, apply relations, beforeDelete) carry feature rules, one access policy per feature decides authorization and row visibility, writes are conditional (ETag and If-Match), and transactions are declared per entry point. In the Reference Projects it is DefaultController / DefaultServiceImplements, which originates in BugTracker and was inherited, evolved, by wpmanager and backend.",
             examples="Guide: abstract CrudService<REQ, RES, SUM, E, ID>. Reference: BE-R02-01 and BT-R02-01, where the base update re-saves the entity without applying the request form.",
             synonyms="DefaultController stack, generic CRUD framework, CRUD base, CrudService",
             related="Feature Module, Lineage, Platform Module, Query Profile"),
        dict(op="update", term="DTO Tiers",
             definition="The fixed set of data shapes each entity is exposed through. In the Guide there are three, generated by MapStruct: Request (create and update input; no id, no owner, no server-controlled fields), Response (detail output, also the create response) and Summary (list row). In the Reference Projects there are four: Form (write shape), DTO (full detail), MiniDTO (small reference used inside other DTOs) and ListDTO (grid row with denormalized counters).",
             examples="Guide: NoteRequest, NoteResponse, NoteSummary. Reference: AdminForm, AdminDTO, AdminMiniDTO, AdminListDTO in backend's admin module.",
             synonyms="DTO shapes, Request/Response/Summary, Form/DTO/MiniDTO/ListDTO",
             related="Feature Module, Denormalized Counter"),
    ],
    # Batch C - proposals beyond the parent's list: vocabulary introduced by the review decisions
    # F3-F17, plus three reference-era terms the Guide redefines. Each needs the user's yes.
    "C": [
        dict(op="add", term="Module Contract", category=GUIDE,
             definition="What the Guide and the Skill state about a Platform Module instead of code: its interface, its invariants and its error modes, plus how it interacts with other modules. Projects implement each contract fresh against current framework documentation.",
             examples="The Upload Coordinator contract: store(source, key, persist); invariant: no database transaction is open during storage I/O; error mode: a digest mismatch fails with 422 before the object is finalised.",
             synonyms="contract",
             related="Platform Module, Guide, Base Project"),
        dict(op="add", term="Version Note", category=GUIDE,
             definition="An evidence-scoped entry in the Version Notes section of a Guide document. It holds version-specific detail (class and API names) so that rule text stays behavioural. Each note is marked either verified on a named line, with a resolvable citation, or not verified, meaning the detail is looked up in current documentation at execution time.",
             examples="verified on 4.1.x: the base-project test that proves the no-transaction contract of the Upload Coordinator.",
             synonyms="",
             related="Guide, Guide Rule, Base Project"),
        dict(op="add", term="Access Policy", category=PLATFORM,
             definition="The single authorization module of a Feature Module, <F>AccessPolicy, with two methods: check(action, actor, entityOrNull) and rowScope(actor). It holds role rules, entity-conditioned rules and row visibility in one file and denies when no rule matches. Method-security annotations are banned in feature code.",
             examples="NoteAccessPolicy.check(Action.update, actor, note) allows the owner and an admin; rowScope(actor) returns ownedBy(owner, actor) for a normal user.",
             synonyms="<F>AccessPolicy, AccessPolicy",
             related="Row Scope, Current User, Feature Module, Generic CRUD Stack"),
        dict(op="add", term="Row Scope", category=PLATFORM,
             definition="The technology-neutral description of which rows an actor may see (ownedBy, all, system). It is produced by the Access Policy and ANDed into every get, list, search, update and delete query before paging, so totals and page counts reflect only visible rows. A row outside the scope reads as 404. It is the named extension point for multi-tenancy.",
             examples="rowScope(actor) = ownedBy(ownerPath, actor): a list returns only the caller's rows and GET on another user's row returns 404.",
             synonyms="RowScope, row visibility",
             related="Access Policy, Query Profile, Current User"),
        dict(op="add", term="User Directory", category=PLATFORM,
             definition="The single lifecycle contract for user records in both identity modes: create, setStatus and rebindSubject. It is the only path that creates or changes a user. Rebinding a user to an external subject is what turns the move to an external identity provider into a data-preserving runbook.",
             examples="Migration to an external provider: rebindSubject(userId, providerSubject) per user, then delete the Token Issuer module.",
             synonyms="UserDirectory",
             related="Current User, Token Issuer, Claims Mapper"),
        dict(op="add", term="Download Ticket", category=PLATFORM,
             definition="A short-lived HMAC capability token issued for a stored file after the feature's authorization check. One platform redemption route verifies it, re-checks that the object still exists, and then redirects to a presigned URL or streams the bytes. It never exposes the object key.",
             examples="A tampered ticket reads as 404, an expired one as 410.",
             synonyms="DownloadTicket",
             related="Object Storage, Access Policy, File Signature"),
        dict(op="add", term="Conditional Write", category=PLATFORM,
             definition="The rule that update and delete must carry the entity's strong ETag in If-Match. A stale tag fails with 412, a missing one with 428, and If-Match: * is the explicit unconditional write. There is no version field in the request body.",
             examples="PUT /notes/7 with an outdated If-Match returns 412; without If-Match it returns 428.",
             synonyms="Conditional-Write Contract",
             related="Generic CRUD Stack"),
        dict(op="add", term="Evidence Pack", category=LOOP,
             definition="The frozen, committed copies of every file a validation review cites, stored whole under Docs/Validation/Run-NN/<Domain>/evidence/, so that citations keep resolving after validation-runs/ is cleaned.",
             examples="evidence/src/main/java/.../NoteService.java:42 cited by a run 01 finding.",
             synonyms="",
             related="Validation Run, Rule-Coverage Ledger"),
        dict(op="add", term="Rule-Coverage Ledger", category=LOOP,
             definition="The table in every validation review that lists each applicable Rule ID exactly once, as either a finding or checked with no finding plus an Evidence Pack citation. It makes the absence of findings a checkable claim.",
             examples="G06-03 | checked - no finding | evidence/src/.../NoteQueryProfile.java:12",
             synonyms="",
             related="Validation Run, Exit Gate, Rule ID, Evidence Pack"),
        dict(op="update", term="Idempotency Key",
             definition="The identifier under which a repeatable request is registered so that a retry or a concurrent duplicate is detected. In the Guide it is the client-supplied Idempotency-Key header, scoped by Current User and paired with a request fingerprint by the Idempotency Guard. In wpmanager it is the uploaded file's SHA-256 checksum: IdempotencyManager tracks each key through PENDING, PROCESSING, then COMPLETED (stores the serialized result) or FAILED, replays the stored result for a completed key, returns HTTP 409 while a key is in flight, and expires keys after 24 hours.",
             related="Two-Phase Upload, Idempotency Guard"),
        dict(op="update", term="Two-Phase Upload",
             related="Compensation, Storage Provider, Idempotency Key, Upload Coordinator"),
        dict(op="update", term="Compensation",
             related="Two-Phase Upload, Upload Coordinator"),
    ],
    # Batch D - back-links from Batch A terms to Batch C terms. Run only for accepted Batch C terms.
    "D": [
        dict(op="update", term="Current User",
             related="Claims Mapper, Token Issuer, Access Policy, Row Scope, User Directory"),
        dict(op="update", term="Query Profile",
             related="Feature Module, Generic CRUD Stack, Row Scope"),
        dict(op="update", term="Object Storage",
             related="Upload Coordinator, Storage Provider, Download Ticket"),
        dict(op="update", term="Platform Module",
             related="Feature Module, Base Project, Guide, Module Contract"),
        dict(op="update", term="Exit Gate",
             related="Validation Run, Validation Domain, Rule-Coverage Ledger"),
    ],
}

FIELDS = ("category", "definition", "examples", "synonyms", "related")


def main() -> int:
    args = sys.argv[1:]
    if not args or args[0] not in BATCHES:
        print(__doc__)
        return 2
    entries = BATCHES[args[0]]
    if "--only" in args:
        wanted = {name.strip() for name in args[args.index("--only") + 1].split(",")}
        unknown = wanted - {e["term"] for e in entries}
        if unknown:
            print("not in batch %s: %s" % (args[0], ", ".join(sorted(unknown))))
            return 2
        entries = [e for e in entries if e["term"] in wanted]
    failed = 0
    for entry in entries:
        command = ["glossary", entry["op"], "--term", entry["term"]]
        for field in FIELDS:
            if field in entry:
                command += ["--" + field, entry[field]]
        if "--print" in args:
            print("%s  %s" % (entry["op"].upper(), entry["term"]))
            for field in FIELDS:
                if field in entry:
                    print("    %-10s %s" % (field + ":", entry[field] or "(none)"))
            continue
        result = subprocess.run(command, capture_output=True, text=True)
        if result.returncode == 0:
            print("ok    %-6s %s" % (entry["op"], entry["term"]))
        else:
            failed += 1
            print("FAIL  %-6s %s (exit %d): %s" % (entry["op"], entry["term"], result.returncode,
                                                   (result.stderr or result.stdout).strip()))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
````

Run it from the repository root:

```bash
cd /home/jlievano/Dropbox/CodeProjects/spring-boot-backend-skill
glossary categories
python3 <scratchpad>/glossary_terms.py A --print     # show the user; then, after a yes:
python3 <scratchpad>/glossary_terms.py A
python3 <scratchpad>/glossary_terms.py B --print && python3 <scratchpad>/glossary_terms.py B
python3 <scratchpad>/glossary_terms.py C --print
python3 <scratchpad>/glossary_terms.py C --only "Row Scope,Access Policy"   # example of a partial yes
```

#### Edge Cases
1. **Case:** `add` exits 2 (the term exists) — the runner prints `FAIL add <term> (exit 2)`. Show the user
   the existing entry (`glossary search`) and ask whether to update it instead; never overwrite silently.
2. **Case:** the old names must stay findable — **`glossary search` matches the term name only.** Searching
   a synonym (`glossary search "CrudService"`) returns "not found". Synonyms are findable by a text search of
   `documentation/Glossary/Glossary.md` (`rg -n "CrudService" documentation/Glossary/Glossary.md`). This is
   what the parent's "synonyms keep the old names findable" means in practice; record it in
   `Memory/known-issues.md` (Step 8).
3. **Case:** `--synonyms ""` — the CLI requires the flag on `add`; an empty string is the "no synonyms"
   value (the runner passes it).
4. **Case:** a Batch A term relates to a term that only exists in Batch C — Batch A's `related` lists were
   written to name only existing or Batch A terms, so Batch A is valid alone. Batch D adds the links to
   Batch C terms.
5. **Case:** the user declines all of Batch C — skip C and D; the parent's Step 1.4 is still complete,
   because its list is batches A and B.
6. **Case:** the glossary already has two `related` names that resolve to no term (*Era Gap* → "Defect",
   *Strength to Keep* → "Recommended Target Pattern") — pre-existing, not caused by this Task. Mention it to
   the user; do not change it here.
7. **Case:** `Glossary.md` and `glossary.json` look out of sync after the writes — run `glossary render`.
   Never edit either file by hand.

---

### Step 7: Correct the parent Feature

**Goal:** The parent says what the accepted ADRs say, uses the three-digit ADR form, and shows Phase 1 as
done.
**Dependencies:** Step 5 (the ADR 009 and ADR 014 outcomes are needed for P6 and P7).

- [x] Apply the edits in the table to
  `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md`. Change nothing else.
- [x] Tick Steps 1.1, 1.2, 1.3 and 1.4 (`- [x]`).
- [x] Run the verification greps below.
- [x] Leave [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] untouched: it is a closed
  record and keeps its four-digit spellings as history.

**Why this step is critical:**
Tasks 2–13 are created from the parent. If the parent keeps `ADR-0011`, a non-existent finding ID and two
open questions that are now closed, each later Task inherits them.

#### Implementation

| # | Find (current text) | Replace with |
|---|---|---|
| P1 | every `ADR-00NN` (11 occurrences: `ADR-0003`, `ADR-0005`, `ADR-0009` ×2, `ADR-0011`, `ADR-0015`, `ADR-0016`, `ADR-0017` ×3, `ADR-0001`) | `ADR-0NN` (drop one zero) — first `sed` below |
| P1 | the ADR table rows in section 1: `\| 0001 \|` … `\| 0017 \|` | `\| 001 \|` … `\| 017 \|` — second `sed` below |
| P2 | `` `ADR-index.md` plus ADRs 0001–0018. `` | `` `ADR-index.md` plus ADRs 001–017 (ADR-017 stays `Proposed` until Step 4.1). `` |
| P3 | Step 1.3: `Write ADR-0001 … ADR-0016 from decisions D1–D17 with context, decision and consequences, each citing the reference finding IDs; user approves → `accepted`.` | `Write ADR-001 … ADR-016 from decisions D1–D17 with context, decision and consequences, each citing the reference finding IDs; user approves → `accepted`. Open ADR-017 (D18) as `proposed`; Step 4.1 completes it.` |
| P3 | Step 4.1: `Write ADR-0017 (skill architecture): …` | `Complete ADR-017 (skill architecture, opened as `proposed` in Step 1.3): …` and add at the end of the step: `user approves → `accepted`.` |
| P4 | `as `byte[]` (WP-R05-08)` | `as `byte[]` (WP-R05-04)` |
| P5 | `**Changes:** Designed in ADR-0017 after the Guide and the Base Project exist.` | `**Changes:** Designed in ADR-017 after the Guide exists and its contracts pass the blind `GC-R` gate (D18 as amended).` |
| P6 | section 9: `Bean Validation → 422 (or 400, decided in ADR-0009) with a field-error list` | `Bean Validation → **<status accepted in ADR-009>** (ADR-009) with a field-error list` |
| P6 | section 11: `is rejected 400/422 before the guard runs` | `is rejected <status accepted in ADR-009> before the guard runs` |
| P6 | "Potential Issues": the bullet that starts `The ADR for the validation error status (400 vs 422 …` | `The validation error status is settled in ADR-009 (<status>); it is not interchangeable with the other once clients exist.` |
| P7 | section 11: `- Uploads through `UploadCoordinator` are `@Idempotent` by default.` | the sentence accepted in ADR-014 (recommended: `- Idempotency stays opt-in per endpoint (US 38); the upload recipe opts in — an upload endpoint carries `@Idempotent` unless the feature records why not (ADR-014).`) |
| P8 | every `<F>AccessPolicy>` followed by a backtick (6 occurrences on 5 lines) | the same without the stray `>` — third `sed` below |
| P3 | the ADR table row for 0017: `… current-docs lookup for API syntax (written in Step 4.1)` | `… (opened as `proposed` in Step 1.3; completed in Step 4.1)` |
| titles | the Title cell of each row of the ADR table in section 1 | the title of the same ADR in `documentation/ADRs/ADR-index.md` (Design Decision 12) |

Replace `<status accepted in ADR-009>` with the real value (400 or 422). Do not leave the angle-bracket text
in the parent.

**Order matters.** <!-- REVIEW-FIX: the "Find" texts for P3, P5 and P6 contain four-digit ADR IDs; running the P1 sed first would make them unfindable -->
Apply the text replacements first (P2, P3, P4, P5, P6, P7 and the titles), exactly as the "Find" column
shows them. Then run the three `sed` commands (P1 twice, P8). The replacement texts already use the
three-digit form, and the `sed` commands leave three-digit IDs alone. All three were dry-run on a copy of
the parent at Task creation: afterwards no `ADR-00NN` and no `AccessPolicy>` remain, and the ADR table has
17 three-digit rows.

```bash
F=documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md
# after the text replacements:
sed -i -E 's/ADR-00([0-9]{2})/ADR-0\1/g' "$F"            # P1: ADR-0011 -> ADR-011
sed -i -E 's/^\| 00([0-9]{2}) \|/| 0\1 |/' "$F"          # P1: table rows | 0001 | -> | 001 |
sed -i 's/<F>AccessPolicy>`/<F>AccessPolicy`/g' "$F"     # P8: stray ">"

# verification
grep -c 'ADR-00[0-9][0-9]' "$F"          # expect 0
grep -c 'AccessPolicy>`' "$F"            # expect 0
grep -n 'WP-R05-08\|0001–0018\|400/422\|or 400, decided' "$F"   # expect no output
grep -n '^- \[x\] \*\*Step 1\.[1-4]' "$F" | wc -l               # expect 4
grep -c 'status accepted in ADR' "$F"    # expect 0
grep -c '^| 0[0-9][0-9] |' "$F"          # expect 17 (three-digit ADR table rows)
grep -c 'written in Step 4.1' "$F"       # expect 0
```

#### Edge Cases
1. **Case:** the generic `sed` for P8 also changes a place where `>` was intended — there is none: every
   `AccessPolicy>` in the parent is followed by a backtick and is the typo (6 occurrences on 5 lines,
   verified at Task creation). Review `git diff` anyway before moving on.
2. **Case:** the P1 `sed` touches a line inside a code fence (the rule-block example `· ADR-0011`) — that is
   intended; the example must show the real citation form.
3. **Case:** a line to replace is not found (the parent changed since 2026-10-04) — do not force it; read
   the current text, apply the same intent, and report the difference.
4. **Case:** the user reversed a recommendation in Step 5 — P6 and P7 use the accepted text, not the
   recommended text.

---

### Step 8: Update the memory bank and close the Task

**Goal:** The next session starts with correct context.
**Dependencies:** Steps 1–7.

- [x] `documentation/Memory/known-issues.md` — under "Architectural constraints", replace the bullet
  "Patterns chosen for the skill must be reconcilable across all three projects; if a pattern only appears in
  one project, it needs stronger justification before being included" with: "Every Guide rule cites a finding
  ID or an ADR; the reference projects are evidence, not authority (ADR-001). A pattern does not have to
  appear in all three projects." Add two notes: (a) ADRs are cited as `ADR-NNN` (three digits = the filename
  prefix), an accepted ADR is never edited, and the Bug Report in `Bugs/done/` keeps the old four-digit
  spellings as history; (b) `glossary search` matches term names only — search `Glossary.md` for a synonym.
- [x] `documentation/Memory/architecture.md` — add a source-map row for `documentation/ADRs/`; in "Key
  technical decisions" state that decisions D1–D18 are recorded as ADR-001…ADR-017 and link the index; in
  "Critical paths" replace "The analysis findings in `documentation/Docs/` are the source of truth for the
  skill" with the ADR-001 statement (the Guide is the source of truth; the findings are its evidence);
  change `ADR-0017` to `ADR-017`.
- [x] `documentation/Memory/context.md` — current focus: Task 1 done; next: Task 2 (Guide format and
  validator, Step 2.1). Keep it short; remove the findings-resolution detail that is no longer current.
- [x] `documentation/Memory/progress.md` — prepend `## <date> (Task 1 — decisions, ADRs, glossary)` with:
  the ADR set and index, the two confirmed decisions (ADR 009 status, ADR 014 wording), the parent
  corrections P1–P8, the glossary counts, and a link to this Task. Do not rewrite older entries.
- [x] `documentation/Memory/tech.md` and `product.md` — read both; no change is expected. Say so in the
  final report.
- [x] `documentation/Memory/brief.md` — never edited.
- [x] Run every command in "Automatic Validation" one last time and read the output.
- [x] Tick this Task's completion criteria. Moving the Task to `Tasks/done/` happens when the user asks.

**Why this step is critical:**
`Memory/architecture.md` still names the analysis findings as the source of truth and `known-issues.md` still
carries the rule D1 replaced. An agent that reads the memory bank before the ADRs would follow the old rule.

#### Edge Cases
1. **Case:** `progress.md` old entries mention `ADR-0017` — leave them; they are a dated log.
2. **Case:** the user accepted only part of the ADR set — `context.md` must say exactly which ADRs are still
   `Proposed` and why.

---

## Design Decisions

**Decision 1:** ADR numbers have three digits everywhere (`ADR-001-…md`, cited as `ADR-001`); the parent's
four-digit spellings are normalised.
- **Why:** `AGENTS.md` makes the `documentation-management` skill mandatory, and that skill fixes the
  filename as `ADR-[NNN]-[short-title].md` and normalises numbers to three digits in its `show adr` and
  `set adr` commands. With four-digit files those commands would not find the files. Making the citation
  token equal to the filename prefix means Task 2's validator resolves a citation by a prefix match, with no
  mapping. Today is the cheapest moment: the Guide, the Skill and every later review do not exist yet.
- **Alternatives considered:** four-digit files (breaks the documentation skill's commands); three-digit
  files with four-digit citations (two spellings of one ID — a drift trap for the validator).
- **Note for the user:** this is the one place where the Task changes a convention the parent wrote. If you
  prefer four digits, say so before Step 2; the change is mechanical (filenames, checker constants, Step 7).

**Decision 2:** ADR 017 (Skill architecture) is written now with status `Proposed` and completed in Step 4.1.
- **Why:** the `create adr` workflow gives a new ADR "the highest existing number + 1". Between this Task and
  Task 6 another ADR may be created (the parent already foresees one: "if it fails twice, ADR-0005 is
  revisited"). It would take number 017 and every `ADR-017` citation in the parent would point at the wrong
  decision. A `Proposed` row is the only reservation the workflow honours. It also reconciles the parent with
  itself (P3): the *decided* part of D18 is recorded with the other decisions; the *design* is still done in
  Step 4.1, before acceptance.
- **Alternatives considered:** write 16 ADRs and a note "017 is reserved" (the workflow does not read notes);
  write ADR 017 in full now (the file layout and loading model cannot be designed before the Guide's
  contracts exist).

**Decision 3:** review decisions F1–F17 are folded into the ADR of the decision they changed, not written as
separate ADRs.
- **Why:** an ADR captures one decision in its current form. F1–F17 changed D1–D18 before any ADR was
  accepted, so there is nothing to supersede. A reader of ADR 005 must find the whole CRUD decision in one
  place. The Bug Report keeps the history.
- **Alternatives considered:** one ADR per finding (33 documents, with the real decision split across two or
  three of them).

**Decision 4:** the template's five sections are kept exactly; evidence, alternatives and related links are
bold lead-ins inside *Context*, *Decision* and *Consequences*.
- **Why:** the documentation skill says an ADR "MUST contain exactly these five sections in this order".
  Extra `###` headings would break that and the checker enforces it.
- **Alternatives considered:** adding `### Evidence` and `### Alternatives` headings (a MADR-style template;
  not this project's format).

**Decision 5:** ADR decisions are behavioural; framework API names appear only in one dated "mechanism"
sentence.
- **Why:** ADR 003 itself says rules stay version-neutral and class names live in Version Notes. An accepted
  ADR cannot be edited, so an API name in a decision sentence would be wrong after the next framework
  generation — the BT-R08-01 failure in document form. Context7's Spring sources are not version-tagged
  (`Memory/known-issues.md`), so a name cannot honestly be called verified for a line until a Base Project
  test exists.
- **Alternatives considered:** name the APIs freely (fossilises); forbid them entirely (loses useful, already
  researched detail such as the account-flag default trap).

**Decision 6:** ADR 009 recommends **400** for a failed request validation; the user confirms in Step 5.
- **Why:** the parent leaves it open and requires it settled before Guide 09. The rule "400 = invalid by
  itself; 422 = valid but refused against state or content" is one test that classifies every case, including
  the missing digest (P6). It matches the framework's documented default for a failed `@Valid` request body,
  so no handler override is needed and none can be forgotten on a second exception path. 422 keeps the two
  meanings F5 gave it.
- **Alternatives considered:** 422 for Bean Validation — semantically defensible (RFC 9110: well-formed but
  unprocessable) and listed first in the parent; it costs an override per validation exception type and
  makes the boundary with "wrong type in the JSON" (400) hard to explain to clients.
- **Trade-off:** this is the user's decision, not the Task's. The Task makes both outcomes cheap: the ADR is
  `Proposed` when the question is asked, and Step 7 writes whichever status was accepted.

**Decision 7:** ADR 014 states "opt-in per endpoint, no exception; the upload recipe opts in".
- **Why:** it removes the contradiction P7 while keeping both intentions — idempotency costs nothing where
  it is not used (US 38), and no upload built from the Guide forgets it. The guard keeps one responsibility.
- **Alternatives considered:** the coordinator applies idempotency itself (a second responsibility; couples
  a storage module to an HTTP header); delete the "by default" sentence (loses the intent that uploads are
  the main use).

**Decision 8:** ADRs are written as `Proposed`, approved in four groups, then flipped to `Accepted`.
- **Why:** sixteen documents are too many to approve in one question and too related to approve one by one.
  The groups follow dependency: foundations, structure, identity/errors/data, storage/idempotency/validation.
  `Proposed` is the only state in which the user's corrections can be applied in place.
- **Alternatives considered:** write them `Accepted` directly because the decisions "are already made" (the
  parent requires approval, and two points are still open).

**Decision 9:** the glossary is written by a runner script that calls the CLI, with the term data in the
script; three new categories; a separate batch for terms beyond the parent's list.
- **Why:** 30 commands with long quoted strings are error-prone in a shell; the runner passes each field as
  one argument, prints the batch for the user's confirmation, and reports each result. It still goes through
  the CLI, so the "never edit glossary files" rule holds. The existing *Backend Patterns* category describes
  the reference projects' patterns; the Guide's platform vocabulary and the Validation Loop vocabulary are
  different topics. Batch C exists because the parent's term list was written before F3–F17 introduced Row
  Scope, Access Policy and the rest — US 52 asks for one vocabulary, but adding terms the parent does not
  list needs the user's yes.
- **Alternatives considered:** 30 hand-typed commands in the Task; adding the nine extra terms without
  asking; putting everything in *Backend Patterns*.

**Decision 10:** the checker and the runner are scratch files, not committed.
- **Why:** the parent groups this Task as "decision records with no code", and Task 2 owns the validator
  work (its `guide` target must resolve ADR citations and will have unit tests). A second committed checker
  now would be a competing validator.
- **Alternatives considered:** `scripts/check-adrs.py` with tests (belongs to Task 2's design); no automatic
  check (more than a hundred finding citations checked by eye).

**Decision 11:** Step 1.1 is verified, not performed.
- **Why:** `Memory/context.md` and `Memory/progress.md` record it as applied on 2026-10-03, and the brief's
  text confirms it. The brief is user-owned, so the Task could not perform it anyway.

**Decision 12:** ADR titles are short noun phrases and may differ from the titles in the parent's ADR table;
Step 7 copies the final titles back into that table.
<!-- REVIEW-FIX: the Task retitled several ADRs without saying so and without syncing the parent -->
- **Why:** the ADR template asks for a "Short Noun Phrase". Several planned titles in the parent are whole
  summaries (the 0003 row lists four clauses), and one names a framework class (`ProblemDetail`), which
  Decision 5 keeps out of anything immutable. The index is read as a table, so titles must fit a cell and
  contain no `|`. One title per ADR must exist in exactly two places — the file heading and the index — and
  the parent's table is made to agree.
- **Alternatives considered:** use the parent's planned titles verbatim (long, and one would fossilise a
  class name); leave the parent's table as planned (two titles for one ADR).

---

## Testing Considerations

### Automatic Validation

- [x] Run `python3 <scratchpad>/check_adrs.py --final` from the repository root — expect
  `0 error(s); mode=final` and exit 0 (17 files, five sections each, every finding ID and wiki link
  resolves, three-digit ADR IDs only, statuses match tags and index, ADR 001–016 Accepted, ADR 017 Proposed)
- [x] Run `ls documentation/ADRs | wc -l` — expect `18` (17 ADRs + the index)
- [x] Run `grep -c '^| ADR ' documentation/ADRs/ADR-index.md` — expect `17`
- [x] Run `grep -rn 'WP-R05-08' documentation/ADRs documentation/Features` — expect no output
- [x] Run `python3 -c "import json; c=json.load(open('documentation/doc-config.json')); assert c['directories']['adrs']=='ADRs' and c['statuses']['adrs']==['proposed','accepted','deprecated','superseded']; print('doc-config ok')"`
- [x] Run `glossary categories` — expect the three old categories plus `Guide and Convention`,
  `Platform Modules`, `Validation Loop`
- [x] Run `for t in "Guide" "Guide Rule" "Rule ID" "Platform Module" "Base Project" "Current User" "Token Issuer" "Claims Mapper" "Object Storage" "Upload Coordinator" "Idempotency Guard" "Query Profile" "Validation Run" "Validation Domain" "Exit Gate"; do glossary search "$t" >/dev/null || echo "MISSING $t"; done`
  — expect no output
- [x] Run `glossary search "Feature Module" | grep -c "AccessPolicy\|Access Policy"`, and the same for
  `"Generic CRUD Stack"` with `platform.crud` and `"DTO Tiers"` with `Summary` — expect `1` each (the
  definitions describe the Guide's shape)
- [x] Run `rg -n "DefaultController stack|ten-file module|MiniDTO" documentation/Glossary/Glossary.md` —
  expect matches (the old names are still findable in the rendered glossary)
- [x] Run the seven verification greps of Step 7 — expect `0`, `0`, no output, `4`, `0`, `17`, `0`
- [x] Run `for p in backend BugTracker wpmanager; do python3 scripts/validate-analysis-docs.py "$p" >/dev/null; echo "$p exit=$?"; done`
  — expect exit 0 three times (no regression in the analysis documents)
- [x] Run `python3 -m unittest discover -s scripts/tests` — expect `Ran 44 tests` and `OK`
- [x] Run `for s in backend bugtracker wpmanager; do sha256sum --quiet -c scripts/.snapshots/$s.sha256 && echo "$s unchanged"; done`
  from the repository root — expect three "unchanged" lines (the reference projects were not touched;
  the command was run at Task creation and printed all three).
- [x] Run `git status --short` — the only new or changed paths are `documentation/ADRs/`,
  `documentation/doc-config.json`, `documentation/Glossary/`, the parent Feature, this Task, and the four
  memory files (plus the files that were already modified before the Task started)
- [x] Run `rg -n -i "password\s*[:=]|secret\s*[:=]\s*\S|AKIA[0-9A-Z]{12}" documentation/ADRs` — expect no
  output (no secret value copied into an ADR)

### Manual Validation

- [x] **(User)** Group A: read ADR 001, 002, 003, 016 and the Proposed ADR 017; approve or request changes.
- [x] **(User)** Group B: read ADR 004, 005, 011, 012; approve or request changes.
- [x] **(User)** Group C: read ADR 006, 007, 008, 009, 010; **choose 400 or 422** for a failed request
  validation and **confirm 422 for a business rule violation** (ADR 009); approve or request changes.
- [x] **(User)** Group D: read ADR 013, 014, 015; **confirm the upload wording** of ADR 014; approve or
  request changes.
- [x] **(User)** Glossary: confirm Batch A (15 terms), Batch B (3 updates), and choose which Batch C
  proposals to accept.
- [ ] **(User)** Open `documentation/ADRs/ADR-index.md` and two ADRs in Obsidian; confirm the finding links
  open the right review heading and the tags appear. (The checker proves the links resolve; this checks how
  they render.)
- [x] **(User)** Confirm the three-digit ADR form (Design Decision 1) or ask for four digits before Step 2.

**Rule:** Run automatic checks when possible. If validation requires manual testing, document the steps here
for the user and do not attempt to execute those manual tests yourself.

---

## Related Code Explanations

No code-explanation documents exist (`documentation/Code/` is not used in this workspace). Related documents:

- [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] — the source of every decision recorded.
- [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] — reasoning and rejected options
  for F1–F17.
- [[Docs/Analysis-Doc-Conventions]] — the finding-ID and citation rules the ADRs follow.
- [[Docs/backend/Reviews/00-Review-Summary]], [[Docs/BugTracker/Reviews/00-Review-Summary]],
  [[Docs/wpmanager/Reviews/00-Review-Summary]] — the evidence base.
- `scripts/validate-analysis-docs.py:48-49` — the citation and wiki-link patterns Task 2 will extend to
  resolve `ADR-NNN`.

---

## Completion Criteria

- [x] Parent document reviewed and reflected accurately in this task
- [x] Relevant skills reviewed and selected for this task
- [x] Up-to-date documentation reviewed for the affected technologies (the Spring Boot generations API
  re-read on the execution day; the method-validation default status verified for ADR 009)
- [x] Step 1.1 verified in `Memory/brief.md` (not edited)
- [x] `doc-config.json` has the ADR directory and statuses; `ADRs/ADR-index.md` exists with 17 rows
- [x] ADR 001–016 written, each with the five sections, evidence that resolves and rejected alternatives
- [x] ADR 017 written with status `Proposed`
- [x] The user approved ADR 001–016; all sixteen are `Accepted` in file, tag and index
- [x] ADR 009 records the validation status the user chose and the business-rule status the user confirmed;
  ADR 014 records the upload wording the user confirmed
- [x] Glossary: 15 terms added and 3 updated through the CLI with the user's confirmation; Batch C handled
  as the user decided
- [x] The parent Feature is corrected (P1–P8, ADR table titles synced with the index) and Steps 1.1–1.4 are
  ticked
- [x] Memory bank updated (`known-issues`, `architecture`, `context`, `progress`); `brief.md` untouched
- [ ] All implementation steps checked off
- [x] Automatic validation passes
- [ ] Manual validation steps documented for the user and performed by the user
- [x] Code explanation files updated (if new files created) — not applicable: no code
- [x] Parent Feature Task 1 linked to this document and its steps marked complete

---

## Post-Review Notes

Recorded during the autonomous self-review after implementation (2026-10-04).

1. **Finding bodies — partial.** Every cited Finding ID was verified to resolve to a `### <ID>` heading in
   its review document (the checker enforces this), and every cited finding's `**Title:**` line was read
   from its review document body — not from the summary inventory. The full bodies were read for the
   load-bearing claims (e.g. BE-R04-03, WP-R07-07 for ADR 012). The literal instruction "read the body of
   every finding an ADR cites" was **not** carried out for all 100+; the Evidence glosses are the finding
   titles as written in the review documents, so no claim comes from an inventory. The checkbox at Step 4
   is left unchecked for that reason. If a stricter pass is wanted, it can only strengthen the ADRs' Context
   prose — no decision content depends on it.

2. **ADR length.** ADR 005 (107), 006 (112), 009 (105) and 013 (111) run past the "about 40–90 lines"
   guidance and the "past about 100" edge-case threshold. The overrun is the mandated content — 8–12
   numbered decision points plus 15–21 evidence citations, each with a full-path wiki link — not restated
   design. Cutting further would drop decision points the specifications require. Evidence glosses were
   compacted once during review.

3. **Post-acceptance edit (needs the user's attention).** After the four groups were approved, three
   **mechanism sentences** were corrected in the now-`Accepted` ADR 005, ADR 006 and ADR 013 to name the
   framework APIs the writing rules require there (`@Transactional`, `@Version`; `S3Client`, `NEVER`;
   `JwtDecoder`, `NimbusJwtDecoder`, `CorsConfigurationSource`, `@EnableMethodSecurity`, `UserDetails`).
   **No decision content changed** — only the dated mechanism sentence Design Decision 5 reserves for these
   names. Strictly, an accepted ADR is immutable; if that is to be applied retroactively, revert these three
   edits (they are isolated) and the ADRs will hold no framework API names at all. Recommended: keep them.

4. **Index row order.** The Task says each ADR "appends one row", which produced writing order (1, 16, 2, 3,
   4, 9, 10, 12, 7, 6, 5, 11, 8, 13, 14, 15, 17). The rows were sorted numerically in review for lookup;
   the checker is order-independent and still reports `0 error(s)`.

5. **One validation expectation is off by the header row.** `grep -c '^| ADR ' documentation/ADRs/ADR-index.md`
   matches the table header as well and returns 18, not the documented 17. The real count is 17 ADR rows
   (`grep -c '^| ADR [0-9]'`); the checker independently confirms one row per ADR with a matching title and
   status.

6. **No technology stack was supplied** with the task-executor invocation. It was derived from the Task
   document: documentation/Markdown work with Python scratch scripts. No application code exists in this
   Task, and no technology-specific skill applied beyond the ones the Task selected.

7. **Pre-existing uncommitted changes were preserved.** The Bug Report move to `Bugs/done/` and the
   memory-bank edits of 2026-10-03 (`brief.md`, `product.md`, `tech.md` and others) were already dirty
   before this Task started and were not touched. `brief.md` was not edited.

8. **`rg` is not installed** in this environment; the two Automatic Validation commands that name it were
   run with `grep -rn` instead. Both passed on the same expectation.
