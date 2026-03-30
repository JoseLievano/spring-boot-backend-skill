# Active Context

## Current focus
Skill authoring — Phase 0 (Project Onboarding and Environment Assessment) has been
fully implemented. The skill file structure has been established using a phase-based
progressive disclosure model. The next major work block is analyzing the three reference
projects to extract patterns for the remaining phases.

## Recent changes
- Created `spring-boot-skill/skill.md` — lean router with universal rules, Phase Registry,
  and Phase Selection Protocol
- Created `spring-boot-skill/references/phase-0-onboarding.md` — full Phase 0 protocol
  (freshness check, 10-step onboarding sequence, required output structure, completion criteria)
- Established phase-based skill file architecture: one reference file per phase, loaded
  only when that phase is active → [[Docs/Skill-Architecture]]
- Documented skill architecture in full → [[Docs/Skill-Architecture]]

## Current skill state
| Phase | Status | File |
|-------|--------|------|
| 0 — Project Onboarding and Environment Assessment | Implemented | `references/phase-0-onboarding.md` |
| 1+ — All remaining phases | Not yet designed | Pending reference project analysis |

## Next steps
- Analyze each of the three Spring Boot reference projects in depth:
  `auth-server/`, `BugTracker/`, `wpmanager/`
- Extract architectural patterns: package structure, entity conventions, service layer,
  controller patterns, auth approach, exception handling, testing setup
- Use findings to design and write the remaining skill phases
- Patterns chosen for each phase must be grounded in at least two of the three projects
