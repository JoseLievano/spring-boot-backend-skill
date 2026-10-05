# Active Context

## Current focus
**Task 3 DONE and closed (2026-10-04) — Guide documents 01–05 (Step 2.2).**
[[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-3-guide-structure-crud-api]] is
implemented, autonomously reviewed and validated by the user (the rules of the five documents approved as
written; the documents render correctly in Obsidian); it was moved to `Tasks/done/` at the user's request.
`documentation/Docs/Guide/` now holds documents 01–05 as drafts —
63 rules (9/11/10/20/13) — plus the conventions and the index; `guide` exits 0.
**The user decided (Step 1):** the order of checks is **Option A** — scoped load (404) → policy check with
the row (403) → precondition → mutate at every row entry point, recorded as **ADR-018** (accepted; it
replaces the order sentence of ADR-005 decision 8 only). All seven ★ gaps confirmed as proposed: one home
per rule, the `platform` dependency table with `RowScope` in `platform.access`, seven `Action` values,
`PUT` full replacement, zero-based `page`, `G01-09` for every project, the CRUD base as the exception in
`G01-02`. Glossary gained *Entry Point*, *Service Hook*, *Concurrency Token*, *Page Response* and *Depth
Criterion* (all five accepted). Parent Step 2.2 ticked; its check-order sentence and ADR table updated.

Next: **Task 4** — Guide 06–09 (Step 2.3). **Its Task document is written and reviewed, not executed
(2026-10-05):**
[[Tasks/current/Spring-Boot-Architecture-Guide-and-Base-Project-step-4-guide-query-persistence-identity-errors]].
It carries the full text of documents 06–09 (78 rules: 16/19/29/14), pre-validated in the scratchpad, and
a link pass for the 17 inline-code names of those documents in 02–05 (7 names of documents 10–15 stay for
Task 5). Execution starts with the user confirming eleven contract choices (H1–H11) — among them
`RowScope.none()` with AND-only composition, bounds that reject, users referenced by id, 401 for a
disabled user, issuer routes under `/api/v1/auth`, a closed public-route list, a seventh exception kind
`InvalidRequest`, and problem types of the form `/problems/<name>`. `Docs/Guide/` is unchanged so far.

## What Task 3 produced
- **Five Guide documents** (`01-Principles-and-Baseline`, `02-Project-Layout-and-Module-Boundaries`,
  `03-Feature-Module-Anatomy`, `04-CRUD-Base-and-Service-Hooks`, `05-API-Contract`) — drafts (`#draft`),
  63 rules citing 87 findings and 13 ADRs; 14 `not verified` Version Notes. Rule IDs become permanent at
  the contract review (Task 5).
- **ADR-018** `Accepted` + index row; the parent's section 6 check order, ADR table and "ADRs 001–018".
- ADR-001…ADR-017, the validator, its tests and both conventions documents are unchanged (frozen
  checksums); the three reference projects are unchanged (snapshot checks).

## What Task 1 produced
- **ADRs 001–017** in `documentation/ADRs/`, cited as `ADR-NNN` (three digits = the filename prefix).
  Recorded decisions D1–D18 with review decisions F1–F17 folded in. Two decisions were confirmed by the
  user at acceptance: **ADR 009 → 400** for a failed request validation (422 = valid but refused against
  state or content) and **422** for a business rule violation; **ADR 014 → idempotency stays opt-in per
  endpoint** (US 38) with the upload recipe opting in.
- **Glossary:** 15 new terms (the parent's list) + 12 accepted beyond it (9 new + 3 updates) in three new
  categories (*Guide and Convention*, *Platform Modules*, *Validation Loop*), plus 5 back-links.
- **Parent corrections:** four-digit ADR IDs normalised; `ADR-index.md plus ADRs 001–017`; the non-existent
  `WP-R05-08` citation replaced by `WP-R05-04`; the stale "after the Guide and the Base Project exist"
  sentence replaced; the open 400-vs-422 question closed; the `@Idempotent by default` contradiction
  resolved; the `<F>AccessPolicy>` typo fixed; ADR table titles synced with the index.

## Constraints that still bind
- The three reference projects are read-only; nothing in this Task touched them.
- `Memory/brief.md` is user-owned and is never edited by an agent.
- ADRs are immutable once accepted; a change is a superseding ADR.
- The Skill ships no code artifacts and no Spring app ([[ADRs/ADR-002-code-free-skill-and-reference-base-project|ADR-002]]).

## Next steps
- **Task 4** — Guide 06–09 (Step 2.3): `06-Query-Engine`, `07-Domain-Model-and-Persistence`,
  `08-Identity-Authentication-and-Authorization`, `09-Errors-and-Validation`; link pass over documents
  02–05's 24 inline-code names.
- Task 5 writes documents 10–16, the traceability matrix and the contract review (`GC-R`).
- Task 6 completes and accepts ADR 017.
