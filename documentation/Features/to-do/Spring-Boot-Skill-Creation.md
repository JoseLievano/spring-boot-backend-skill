#feature #high #new-feature

# Spring Boot Backend Skill Creation

## Overview
Build a Claude Code skill that scaffolds production-quality Spring Boot REST APIs based on
patterns extracted from three real Spring Boot reference projects.

## Affected systems
- `auth-server/` — reference project
- `BugTracker/` — reference project
- `wpmanager/` — reference project
- Skill output: new standalone skill file

## Phases

### Phase 1 — Analysis
- [ ] Analyze `auth-server/` — structure, dependencies, patterns, conventions
- [ ] Analyze `BugTracker/` — structure, dependencies, patterns, conventions
- [ ] Analyze `wpmanager/` — structure, dependencies, patterns, conventions
- [ ] Compare all three: extract common patterns
- [ ] Compare all three: identify differences and decide best approach per area
- [ ] Document findings in `documentation/Docs/`

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
