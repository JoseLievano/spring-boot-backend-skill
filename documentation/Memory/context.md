# Active Context

## Current focus
New action plan: [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (supersedes Task 4 and
Phases 2–3 of [[Features/to-do/Spring-Boot-Skill-Creation]], which the user asked to ignore). Its review
[[Bugs/to-do/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] has 17 findings (1 🔴, 5 🟠, 8 🟡, 3 🟢),
all Pending. **Next: resolve the findings with the user (feature-findings-solver), then start Task 1 (ADRs).**

## Recent changes
- **2026-09-30 — Action-plan Feature + review written.** 18 decisions (D1–D18) from a user interview: Guide in
  `Docs/Guide/`, Base Project owns platform modules, JWT now → Clerk/WorkOS later via resource-server seam, S3-compatible +
  local storage port, idempotency in base, PostgreSQL, MapStruct, no multi-tenancy, skill rewritten from scratch.
- **Task 3 executed:** [[Docs/wpmanager/wpmanager-Index]] — 14 Explanations, 11 Reviews + summary, 88 findings
  (9 Critical, 16 High, 43 Medium, 20 Low; 34 share a root cause with `backend/`). Validator exit 0 for all three
  projects; checksum snapshot `scripts/.snapshots/wpmanager.sha256` unchanged. Conventions: reviews may add
  extra `##` sections after `## Findings` (Changelog).
- **Task 2 executed:** [[Docs/BugTracker/BugTracker-Index]] — 86 findings (5 Critical).
- **Task 1 executed:** [[Docs/backend/backend-Index]] — 67 findings; [[Docs/Analysis-Doc-Conventions]] and
  `scripts/validate-analysis-docs.py` (+ 44 unit tests).

## Current skill state
| Phase | Status | File |
|-------|--------|------|
| 0 — Project Onboarding and Environment Assessment | Implemented | `references/phase-0-onboarding.md` |
| 1+ — All remaining phases | Not yet designed | Pending reference project analysis |

## Next steps
- Resolve the 17 review findings (F1 needs the user to change `brief.md:23`; F11 needs a Boot-line decision).
- Then Task 1 of the new Feature: `brief.md` update (user), ADR system init, ADR-0001…0016, glossary terms.
