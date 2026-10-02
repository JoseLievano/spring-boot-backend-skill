# Active Context

## Current focus
Resolving the 17 findings of [[Bugs/to-do/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]]
against [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (the `bug-findings-solver`
workflow, one finding at a time with the user). **4 of 17 done (F1, F2, F3, F4). Stopped by the user;
F5–F17 resume later.** Next up is F5 (Idempotency fingerprinting conflicts with streaming uploads, 🟠 High).

## Major decision: D2 was revised (F1)
The Skill is **not** a scaffold and ships **no code artifacts and no Spring app**. It is a project-agnostic
convention in pure markdown — rules, module **contracts** (interfaces, invariants, error modes) and
pseudo-code only. Project initialization is explicitly not the Skill's job, and the Skill is not
Claude-Code-specific. New projects implement the contracts fresh and look up current framework documentation
for API syntax at execution time (so the Skill never teaches deprecated APIs).

`base-project/` remains a workspace deliverable as the **reference implementation** that proves the
convention and is the Validation Loop harness — never shipped with the Skill, never a copy source.

Consequences already patched into the Feature: D2 row, description items 2–3, problem statement, user story
43, the flowchart, sections 13 and 14, ADR-0002/0017 rows, Step 1.1, Step 4.1, Risk Assessment.

## Findings decided so far
- **F1 (Critical) → Done.** Custom option: code-free convention skill. Dissolves the `brief.md:23`
  standalone-skill conflict — `known-issues.md:50` needs **no** change. `brief.md` still needs a user edit,
  but only to replace "scaffolds new Spring Boot APIs" and "Claude Code skill (markdown prompt file)".
- **F2 (High) → Done.** Blind **design** review of the module contracts at end of Phase 2 (finding IDs
  `GC-R<NN>-<MM>`, gate 0 🔴 / 0 🟠 before Phase 3), not a code review of `base-project/`. Step 3.7 becomes a
  contract-conformance checklist.
- **F3 (High) → Done.** Mandatory technology-neutral **`RowScope`** required by `ListQuery.run` and every CRUD
  entry point (fail-closed by signature). Produced by `<F>AccessPolicy` via `ownedBy()/all()/system()`;
  ANDed into the query before paging; invisible rows → 404. `<F>AccessPolicy` added to the Feature Module
  anatomy. Named D8 tenancy extension point.
- **F4 (High) → Done.** Option 4 (alternative): remove the enabler + framework-native fail-fast. The CRUD base
  declares `@Transactional` **per entry point** (six base methods only) instead of class-level, so feature-added
  service methods are non-transactional by default and multi-write custom methods declare it explicitly — a
  documented departure from the reference projects (US 5, Guide 04 "Differs From"). `UploadCoordinator.store` is
  declared `@Transactional(propagation = NEVER)`, failing before any storage I/O if a transaction is active.
  Plus a transaction-boundary contract test and the invariant named in Step 3.7's contract-conformance checklist.

## Findings pre-resolved / prepared by these decisions
- **F16** — auto-resolved (the Skill no longer copies the sample feature); confirm and record when its turn
  comes.
- **F7** — partly prepared: `<F>AccessPolicy` now exists in the anatomy and is the row-scope source. The
  remaining question is the split between URL rules / method security / the policy file / `authorize`.
- **F17** — partly prepared: `RowScope.system()` and the `system()` factory cover scheduled/system work.
  Still needs `CurrentUser.system()` / `runAsSystem` decided.
- **F11** — reframed: only the Base Project pins a version; the Skill does not.
- **F13** — should reuse F4's rule: a download endpoint that streams must not run inside a transaction either.
- **F6 / F9** — note for later: under F4, feature-added service methods are non-transactional by default, so any
  multi-write identity or update method must declare `@Transactional` explicitly.

## Recent changes
- **2026-10-02 — F4 decided and patched.** Per-entry-point transactions on the CRUD base + `propagation = NEVER`
  on `UploadCoordinator.store`. Feature sections 6 and 10, Guide rows 04/10/13, Steps 3.5/3.7, Testing Decisions.
- **2026-09-30 — Action-plan Feature + review written.** 18 decisions (D1–D18) from a user interview.
  Review produced 17 findings.
- **Task 3 executed:** [[Docs/wpmanager/wpmanager-Index]] — 14 Explanations, 11 Reviews + summary, 88 findings.
- **Task 2 executed:** [[Docs/BugTracker/BugTracker-Index]] — 86 findings (5 Critical).
- **Task 1 executed:** [[Docs/backend/backend-Index]] — 67 findings; [[Docs/Analysis-Doc-Conventions]] and
  `scripts/validate-analysis-docs.py` (+ 44 unit tests).

## Current skill state
| Phase | Status | File |
|-------|--------|------|
| 0 — Project Onboarding and Environment Assessment | Implemented | `references/phase-0-onboarding.md` |
| 1+ — All remaining phases | Not yet designed | To be replaced by the rewritten skill (D18) |

## Next steps
- Resume findings resolution at **F5** (idempotency fingerprinting vs streaming uploads), then F6, F7…F17 in
  order. F16 is expected to auto-resolve.
- After all findings: the user updates `brief.md` (Step 1.1 wording), then Task 1 of the Feature
  (ADR system init, ADR-0001…0017, glossary terms).
