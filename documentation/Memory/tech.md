# Tech Stack

## Languages & Frameworks
| Technology | Version | Role |
|-----------|---------|------|
| Java | TBD (check each project's pom.xml / build.gradle) | Reference project language |
| Spring Boot | TBD | Framework used in all three reference projects |
| Markdown | — | Skill file format (Claude Code skill) |

_Exact versions to be filled in during project analysis._

## Key dependencies
_To be filled in during project analysis of each reference project._

## Development setup
This workspace requires no build step. The three reference projects are read-only inputs.
To explore them:
```
cd auth-server   # or BugTracker / wpmanager
# check pom.xml or build.gradle for build instructions
```

## Technical constraints
- The skill file must be pure markdown — no external tooling dependency
- Patterns extracted must be applicable to standard Spring Boot projects, not framework-specific forks
- `skill.md` must stay lean (universal rules only). Phase-specific instructions must never be added to it.
- Each phase reference file must be self-contained — it must not depend on content from other phase files.

## Skill file organization pattern
The skill uses a **phase-based progressive disclosure** model to minimize token usage:

- **`skill.md`** — always loaded. Contains: frontmatter trigger, Operating Principles, Phase Registry
  table, Phase Selection logic, Conflict Resolution, Enforcement, Prohibited Assumptions.
- **`references/phase-N-<name>.md`** — loaded only when that phase is active. Full protocol for
  that phase lives here exclusively.

The agent determines the active phase by reading the memory bank at session start, then loads
only that phase's reference file. Inactive phase files are never loaded.

When adding a new phase:
1. Create `references/phase-N-<name>.md` with the full phase instructions.
2. Add one row to the Phase Registry table in `skill.md`. Nothing else in `skill.md` changes.

## Tooling & patterns
- Claude Code CLI is the runtime for the skill
- Obsidian is used to view/edit the `documentation/` vault (optional — files are plain markdown)
- No CI/CD configured for this workspace (analysis + writing work only)
