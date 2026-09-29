# Tech Stack

## Languages & Frameworks
| Technology | Version | Role |
|-----------|---------|------|
| Java | 21 (`backend/`, wpmanager); 17 effective (BugTracker) | Reference project language |
| Spring Boot | 3.4.1 (`backend/`, wpmanager); 2.7.0 (BugTracker) | Framework used in all three reference projects |
| Python | 3.10 (stdlib only; script targets ≥ 3.9) | `scripts/validate-analysis-docs.py` and its tests |
| Markdown | — | Skill file format (Claude Code skill) |

_Exact versions to be filled in during project analysis._

## Key dependencies
- `backend/`: Spring Security 6.4.2, Hibernate 6.6.4, OpenFeign QueryDSL 6.12 (APT `jpa` classifier),
  JJWT 0.12.5, PostgreSQL (runtime), H2 (tests), Lombok. Details: [[Docs/backend/Explanations/02-Build-Tooling-and-Dependencies]].
- `BugTracker/`: Spring Boot 2.7.0 (`javax.*`), Java 17 effective (compiler plugin; `java.version` says 11),
  Spring Security 5.7.1, Hibernate 5.6, QueryDSL 5.0 (`com.querydsl`, `apt-maven-plugin` 1.1.3), JJWT 0.11.2,
  MySQL, Lombok, Maven 3.8.4 wrapper; six unused starters. Details:
  [[Docs/BugTracker/Explanations/02-Build-Tooling-and-Dependencies]].
- wpmanager: Spring Boot 3.4.1, Java 21, Spring Security 6.4, Hibernate 6.6 (`MySQL8Dialect`, deprecated), JJWT
  0.12.5, **AWS SDK for Java v2 `s3` 2.20.12** (S3/Wasabi via `endpointOverride`), MySQL, H2 (tests), Lombok,
  JUnit Platform Suite; six unused starters. Details: [[Docs/wpmanager/Explanations/02-Build-Tooling-and-Dependencies]].

## Development setup
This workspace requires no build step. The three reference projects are read-only inputs.
To explore them:
```
cd backend   # or BugTracker / wpmanager
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

## Analysis-doc tooling
```
python3 scripts/validate-analysis-docs.py <backend|BugTracker|wpmanager>   # exit 0 = valid
python3 -m unittest discover -s scripts/tests -v                          # validator tests
sha256sum --quiet -c scripts/.snapshots/<project>.sha256                  # reference project unchanged
```

## Tooling & patterns
- Claude Code CLI is the runtime for the skill
- Obsidian is used to view/edit the `documentation/` vault (optional — files are plain markdown)
- No CI/CD configured for this workspace (analysis + writing work only)
