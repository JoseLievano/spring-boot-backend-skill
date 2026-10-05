# Active Context

## Current focus
**Task 4 DONE (2026-10-05) — Guide documents 06–09 (Step 2.3).**
[[Tasks/current/Spring-Boot-Architecture-Guide-and-Base-Project-step-4-guide-query-persistence-identity-errors]]
is implemented and autonomously reviewed; awaiting the user's manual validation (read the four documents,
open them in Obsidian). `documentation/Docs/Guide/` now holds documents **01–09 as drafts** — 141 rules
(9/11/10/20/13/16/19/29/14) — plus the conventions and the index; `guide` exits 0; index reports
`Draft documents: 9`, `Version Notes not verified: 38`.

**The user decided (Step 1):** all eleven ★ gaps **H1–H11 confirmed as proposed** — `RowScope.none()`
with AND-only composition, bounds that reject (never clamp), Jakarta Persistence criteria through Spring
Data specifications (`not verified`), features refer to a user by id (no association), the CRUD base
stamps actor labels (never an ambient read), disabled user = 401 with roles read per request, issuer
routes under `/api/v1/auth/…` plus a password-change route, a closed public-route list fed by a
`PublicRoutes` port (no feature routes, no anonymous actor), a seventh exception kind `InvalidRequest`
(400), problem types `/problems/<name>` in one registry with a correlation id on every problem, and the
failure order 401 → 400 → entry point → hook → database. The five parent inconsistencies stay in the
parent uncorrected (user's choice) and are listed in the Task's report. **Stack note:** no technology
stack was supplied; the Task is docs-only and proceeded as such.

**Glossary (Step 8, all accepted):** *System Actor*, *Actor Label*, *Identity Mode*, *User Provisioning*,
*Domain Exception*, *Problem Type* added; *Row Scope* updated to "(ownedBy, all, none, system)".

Next: **Task 5** — Guide 10–16 (storage, idempotency, configuration, testing, observability, recipe,
traceability) and the blind contract review (`GC-R`). Rule IDs become permanent at that review.

## What Task 4 produced
- **Four Guide documents** (`06-Query-Engine` 16 rules, `07-Domain-Model-and-Persistence` 19,
  `08-Identity-Authentication-and-Authorization` 29, `09-Errors-and-Validation` 14) — drafts, 78 rules
  citing 118 distinct findings and 11 ADRs; 24 new `not verified` Version Notes (index total 38).
- **Link pass:** the 17 inline-code names of documents 06–09 in documents 02–05 became wiki links;
  document 04's Row Scope sentence now names the four forms (`ownedBy`, `all()`, `none()`,
  `system(actor)`). 7 names of documents 10–15 stay for Task 5.
- Parent Step 2.3 ticked.

## Constraints that still bind
- The three reference projects are read-only; nothing in this Task touched them.
- `Memory/brief.md` is user-owned and is never edited by an agent.
- ADRs are immutable once accepted; a change is a superseding ADR.
- The Skill ships no code artifacts and no Spring app ([[ADRs/ADR-002-code-free-skill-and-reference-base-project|ADR-002]]).
- A Guide rule ID is never renumbered from the day the Guide passes the contract review (Task 5).

## Next steps
- **Manual validation (user):** read documents 06–09 and approve the rules; open them in Obsidian
  (tags, links, tables, seven Mermaid diagrams); check the new links in documents 02–05.
- **Task 5** — Guide 10–16, the traceability matrix and the contract review (`GC-R`).
- Task 6 completes and accepts ADR 017 and rewrites the Skill.
