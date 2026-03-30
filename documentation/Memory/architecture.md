# Architecture

## Overview
This repository is a skill-creation workspace, not a deployable application. It contains three
Spring Boot reference projects as subdirectories and a `documentation/` tree used to guide the
analysis and skill-authoring process. The primary output artifact is a Claude Code skill file.

## Source code map
| Path | Role |
|------|------|
| `auth-server/` | Reference project #1 — Spring Boot app (focus: authentication/authorization) |
| `BugTracker/` | Reference project #2 — Spring Boot app (focus: issue/bug tracking domain) |
| `wpmanager/` | Reference project #3 — Spring Boot app (focus: TBD — not yet analyzed) |
| `spring-boot-skill/` | **Primary output** — all skill files live here during development |
| `documentation/` | Obsidian-based project docs, memory bank, analysis notes |
| `documentation/Memory/` | Memory Bank — persistent context for Claude across sessions |
| `documentation/Docs/` | System-level docs: analysis findings, pattern comparisons, skill design |
| `documentation/Features/` | Feature tracking for the skill-authoring work |
| `documentation/Tasks/` | Task breakdown for analysis and writing work |

## Key technical decisions
- **Three-project comparative approach:** Rather than designing the skill from scratch, patterns are
  grounded in real, working codebases. This ensures the skill produces battle-tested conventions.
- **Documentation-first:** All findings from the analysis phase are written into `documentation/Docs/`
  before the skill file is written, so the skill has a clear, reviewable rationale.
- **`spring-boot-skill/` as development sandbox:** All skill files are authored and iterated inside
  `spring-boot-skill/` in this repo. Only when the skill is finalized does it get migrated to the
  actual Claude Code skills directory (`~/.claude/plugins/...`). This keeps all work in one place
  and makes the files easy to review and version-control. See [[Docs/Skill-Directory-Convention]].

## Skill file architecture

The skill is organized for token efficiency using a **phase-based progressive disclosure** pattern.

### Structure
```
spring-boot-skill/
├── skill.md                        ← always loaded; lean router (~110 lines)
└── references/
    ├── phase-0-onboarding.md       ← loaded only during Phase 0
    └── phase-N-<name>.md           ← one file per future phase (auth, entities, etc.)
```

### Loading model
| Layer | What's in it | When loaded |
|-------|-------------|-------------|
| `skill.md` frontmatter | name + trigger description | Always (metadata layer) |
| `skill.md` body | Operating Principles, Phase Registry, Phase Selection logic, Conflict Resolution, Enforcement, Prohibited Assumptions | Always (skill is active) |
| `references/phase-N-*.md` | Full instructions for that phase only | Only when that phase is active |

### Phase routing protocol
1. Agent reads the memory bank at session start.
2. Checks whether a completed onboarding record exists.
3. Loads **only** the reference file for the active phase.
4. Never preloads reference files for inactive phases.

This keeps the token footprint minimal: once Phase 0 is complete and recorded in memory,
its ~140-line reference file is never loaded again unless re-onboarding is triggered.

### Phase registry (current)
| Phase | Reference file | Status |
|-------|---------------|--------|
| 0 — Project Onboarding and Environment Assessment | `references/phase-0-onboarding.md` | Active |

Future phases (authentication, shared components, entities, persistence, exception handling,
testing, etc.) will each get a new row and a new reference file. `skill.md` is extended
only by adding a row to the Phase Registry table — its body does not grow with phases.

### Design principles
- `skill.md` must stay lean. Universal rules only — nothing phase-specific.
- Each reference file is self-contained for its phase.
- No phase detail leaks into `skill.md`.
- The Phase Registry in `skill.md` is the only cross-phase index.

## Design patterns
_Spring Boot architectural patterns to be filled in after project analysis._

## Component relationships
```
auth-server/ ──┐
BugTracker/  ──┼──► analysis ──► documentation/Docs/ ──► spring-boot-skill/ ──► (migrate) ──► ~/.claude/plugins/
wpmanager/   ──┘
```

## Critical paths
- The analysis findings in `documentation/Docs/` are the source of truth for the skill
- `documentation/Memory/brief.md` is the authoritative statement of goals and constraints
- `spring-boot-skill/skill.md` is the phase router — keep it lean; never add phase detail to it
- Each phase's full instructions live exclusively in its `references/phase-N-*.md` file
