# Active Context

## Current focus
Phase 1 (analysis) of [[Features/to-do/Spring-Boot-Skill-Creation]]. Tasks 1 (`backend/`), 2 (`BugTracker/`) and
3 (`wpmanager/`) are implemented and await the user's manual validation. **Next: Task 4 (cross-project
comparison)** using the stable finding IDs and each summary's "Candidate Patterns for the Skill".

## Recent changes
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
- User: manual validation of Tasks 1–3 (Obsidian links/Mermaid, spot checks, recipe usability); decide on the
  proposed glossary terms (all three tasks); **rotate the cloud-storage keys committed in
  `wpmanager/src/test/.../E2EPluginUploadTest.java`**.
- Then Task 4: cross-project comparison using the stable finding IDs (`BE-`/`BT-`/`WP-R<NN>-<MM>`) and the
  "Shared with backend" table in [[Docs/wpmanager/Reviews/00-Review-Summary]].
