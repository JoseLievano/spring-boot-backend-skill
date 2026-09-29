#feature #high #new-feature

# Spring Boot Backend Skill Creation

## Overview
Build a Claude Code skill that scaffolds production-quality Spring Boot REST APIs based on
patterns extracted from three real Spring Boot reference projects.

## Affected systems
- `backend/` — reference project (replaces the former `auth-server/`)
- `BugTracker/` — reference project
- `wpmanager/` — reference project
- Skill output: new standalone skill file

## Phases

### Phase 1 — Analysis
- [x] Analyze `backend/` — structure, dependencies, patterns, conventions → [[Docs/backend/backend-Index]] (via [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend|Task 1]])
- [x] Analyze `BugTracker/` — structure, dependencies, patterns, conventions → [[Docs/BugTracker/BugTracker-Index]] (via [[Tasks/done/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker|Task 2]])
- [x] Analyze `wpmanager/` — structure, dependencies, patterns, conventions → [[Docs/wpmanager/wpmanager-Index]] (via [[Tasks/done/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager|Task 3]])
- [ ] Compare all three: extract common patterns
- [ ] Compare all three: identify differences and decide best approach per area
- [x] Document findings in `documentation/Docs/` — per-project docs for all three projects (Tasks 1–3); the cross-project comparison is Task 4, next

#### Phase 1 — Documentation output structure
Each reference project gets its own directory under `documentation/Docs/`, split into two
subdirectories:

```
documentation/Docs/
├── backend/
│   ├── Explanations/   ← how the project is built (one doc per architectural area)
│   └── Reviews/        ← critiques of the project (one doc per reviewed area)
├── BugTracker/
│   ├── Explanations/
│   └── Reviews/
└── wpmanager/
    ├── Explanations/
    └── Reviews/
```

- **Explanations** describe how the project works: overall architecture, design philosophy,
  package structure, domain model, persistence, services, API layer, security/auth, error
  handling, configuration, shared components, and testing. They must be detailed enough that
  a developer could build a new project "the same way" from them alone. Topics that do not
  exist in a project are skipped, not stubbed.
- **Reviews** critique the project: weak patterns, architectural problems, inconsistencies,
  security/reliability risks, and better alternatives. Every finding cites `file:line`
  evidence, states a severity, and proposes a concrete improvement validated against
  version-matched documentation.

#### Phase 1 — Task breakdown
| Task | Scope | Complexity | Depends on |
|------|-------|------------|------------|
| [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend\|Task 1]] ✅ | Analyze `backend/` → write `Docs/backend/Explanations/*` and `Docs/backend/Reviews/*`. Output: [[Docs/backend/backend-Index]] (11 explanations, 10 reviews, 67 findings), [[Docs/Analysis-Doc-Conventions]], `scripts/validate-analysis-docs.py` | High | — |
| [[Tasks/done/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker\|Task 2]] ✅ | Analyze `BugTracker/` → write `Docs/BugTracker/Explanations/*` and `Docs/BugTracker/Reviews/*`. Output: [[Docs/BugTracker/BugTracker-Index]] (14 explanations, 11 reviews incl. summary, 86 findings: 81 defects, 5 era gaps) | High | Task 1 (reuse doc format) |
| [[Tasks/done/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager\|Task 3]] ✅ | Analyze `wpmanager/` → write `Docs/wpmanager/Explanations/*` and `Docs/wpmanager/Reviews/*`. Output: [[Docs/wpmanager/wpmanager-Index]] (14 explanations, 12 reviews incl. summary, 88 findings: 9 🔴, 16 🟠, 43 🟡, 20 🟢; 34 shared with `backend/`) | High | Task 1 (reuse doc format) |
| Task 4 (later) | Cross-project comparison: common patterns + best approach per area | High | Tasks 1–3 |

Tasks 1–3 are independent in content; the only ordering constraint is that Task 1 establishes
the document format (templates, naming, section order) and the validator that Tasks 2 and 3 reuse.
Run Tasks 2 and 3 sequentially — both update this Feature and the memory bank.

### Phase 2 — Skill Design
- [ ] Define skill structure and trigger conditions
- [ ] Design skill prompts for each generation scenario (full project, controller, service, etc.)
- [ ] Write `references/` files for the skill (patterns, templates, conventions)

### Phase 3 — Skill Authoring
- [ ] Write the skill file
- [ ] Test the skill against hypothetical API scenarios
- [ ] Iterate based on output quality

## Notes
- Do not modify the three reference projects — read-only analysis only
- The skill file format follows Claude Code skill conventions (markdown with front-matter)
