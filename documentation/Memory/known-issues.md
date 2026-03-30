# Known Issues & Constraints

This file tracks architectural gotchas, systemic patterns, and non-obvious constraints.
It is NOT a bug tracker — specific bugs belong in `documentation/Bugs/`.

## Environmental
- The three reference projects have not been built or run — they are used as static analysis subjects only.
  Verify that all source files are present and complete before drawing conclusions from them.

## Framework / Library behaviors
_To be filled in during project analysis._

## Architectural constraints
- The skill output must be self-contained markdown — it cannot depend on external scripts or tools
- Patterns chosen for the skill must be reconcilable across all three projects; if a pattern only
  appears in one project, it needs stronger justification before being included
- **Skill files must be written to `spring-boot-skill/` — never directly to `~/.claude/plugins/` or
  any Claude Code system directory during development.** Migration happens only when the skill is
  finished and explicitly approved. See [[Docs/Skill-Directory-Convention]].

## Patterns to avoid
_To be filled in during project analysis._

## Sharp edges
- If the three projects use different Spring Boot major versions, some patterns may not be
  forward-compatible — note version-specific patterns explicitly
