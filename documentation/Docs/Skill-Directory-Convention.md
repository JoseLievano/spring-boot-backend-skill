# Skill Directory Convention

## Rule
All skill files for the `spring-boot-backend` skill are authored and stored in
`spring-boot-skill/` at the project root — **not** in the Claude Code system skills directory.

## Why
- Keeps all work (analysis, docs, skill files) in one version-controlled location
- Makes skill files easy to find, review, and iterate on alongside the reference projects
- Avoids polluting the Claude Code system directory with in-progress, unfinished work
- Allows full git history on the skill files themselves

## Directory layout (target)
```
spring-boot-skill/
├── skill.md                  ← main skill entry point (the file Claude loads)
├── references/
│   ├── patterns.md           ← extracted Spring Boot patterns
│   ├── conventions.md        ← naming, packaging, code style conventions
│   ├── templates.md          ← code templates (controller, service, repo, DTO, etc.)
│   └── decision-log.md       ← why each pattern was chosen over alternatives
└── examples/
    └── ...                   ← example invocations / expected outputs
```

## Migration (when ready)
When the skill is complete and approved:
1. Copy the contents of `spring-boot-skill/` to the appropriate Claude Code skills directory
   (e.g., `~/.claude/plugins/marketplaces/local/skills/spring-boot-backend/`)
2. Register the skill in the Claude Code settings if required
3. Smoke-test the installed skill
4. Tag the git commit marking the migration point

## What NOT to do
- Do not write skill files directly to `~/.claude/plugins/` during development
- Do not split skill files between this directory and the system directory
- Do not migrate until the skill has been tested and explicitly signed off
