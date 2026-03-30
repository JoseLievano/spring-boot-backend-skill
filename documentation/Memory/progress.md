# Progress

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
