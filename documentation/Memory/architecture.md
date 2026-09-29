# Architecture

## Overview
This repository is a skill-creation workspace, not a deployable application. It contains three
Spring Boot reference projects as subdirectories and a `documentation/` tree used to guide the
analysis and skill-authoring process. The primary output artifact is a Claude Code skill file.

## Source code map
| Path | Role |
|------|------|
| `backend/` | Reference project #1 — Spring Boot 3.4.1 app (artifactId `agentForgeBackend`); replaces the former `auth-server/` with a reorganized auth design |
| `BugTracker/` | Reference project #2 — Spring Boot app (focus: issue/bug tracking domain) |
| `wpmanager/` | Reference project #3 — Spring Boot 3.4.1 premium WordPress plugin/theme repository and distribution backend (S3 storage, upload idempotency, replication); ships its own vault `wpManagerDocs/` |
| `spring-boot-skill/` | **Primary output** — all skill files live here during development |
| `documentation/` | Obsidian-based project docs, memory bank, analysis notes |
| `documentation/Memory/` | Memory Bank — persistent context for Claude across sessions |
| `documentation/Docs/` | System-level docs: analysis findings, pattern comparisons, skill design |
| `documentation/Docs/<project>/` | Phase 1 analysis per reference project: `<project>-Index.md`, `Explanations/NN-*.md`, `Reviews/NN-*-Review.md` + `Reviews/00-Review-Summary.md`. Format: [[Docs/Analysis-Doc-Conventions]] |
| `scripts/validate-analysis-docs.py` | Validator for the analysis docs (headings, citations `path:line`, wiki links + anchors, finding IDs, summary/index coverage). Tests in `scripts/tests/`. Local read-only guard snapshots in `scripts/.snapshots/` (gitignored) |
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
- Analysis docs are split into **Explanations** (descriptive, "how it is built") and **Reviews**
  (evaluative, stable finding IDs `<BE|BT|WP>-R<NN>-<MM>`). Task 4 compares findings by ID.
- `backend/` patterns (candidates for the skill) are listed in [[Docs/backend/Reviews/00-Review-Summary]]
  → "Candidate Patterns for the Skill"; BugTracker's in [[Docs/BugTracker/Reviews/00-Review-Summary]] (which
  also splits findings into defects vs era gaps); wpmanager's in [[Docs/wpmanager/Reviews/00-Review-Summary]] (which
  also maps same-root-cause findings to `BE-` IDs in "Shared with backend").
- **Lineage BugTracker → wpmanager → backend.** The generic `DefaultController`/`DefaultServiceImplements` stack
  goes from five type parameters (BugTracker, with `LISTDTO`) to four (wpmanager) and back to five plus a
  QueryDSL list engine (`backend/`). `backend/` inherited wpmanager's `configuration/`, `shared/` and
  `exceptions/` almost unchanged (27 of 37 shared files identical) and removed the upload/storage domain.

## Component relationships
```
backend/     ──┐
BugTracker/  ──┼──► analysis ──► documentation/Docs/ ──► spring-boot-skill/ ──► (migrate) ──► ~/.claude/plugins/
wpmanager/   ──┘
```

## Critical paths
- The analysis findings in `documentation/Docs/` are the source of truth for the skill
- `documentation/Memory/brief.md` is the authoritative statement of goals and constraints
- `spring-boot-skill/skill.md` is the phase router — keep it lean; never add phase detail to it
- Each phase's full instructions live exclusively in its `references/phase-N-*.md` file
