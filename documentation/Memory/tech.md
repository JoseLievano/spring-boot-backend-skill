# Tech Stack

## Languages & Frameworks
| Technology | Version | Role |
|-----------|---------|------|
| Java | 21 (`backend/`, wpmanager); 17 effective (BugTracker) | Reference project language |
| Spring Boot | 3.4.1 (`backend/`, wpmanager); 2.7.0 (BugTracker) | Framework used in all three reference projects |
| Python | 3.10 (stdlib only; script targets ≥ 3.9) | `scripts/validate-analysis-docs.py` and its tests |
| Markdown | — | Skill file format (agent skill) |

### Spring Boot support policy (the guide's baseline — F11)
Rules stay **version-neutral**; the floor is **the currently OSS-supported Spring Boot generation**; the
Base Project pins exactly one line (the current GA at build time, decided at Task 9); version-specific
details live in **evidence-scoped** version notes (`verified on <line>` with a resolvable citation, or
`not verified — current-docs lookup at execution time`).

Verified 2026-10-03 (`api.spring.io/projects/spring-boot/generations/...`):

| Line | OSS support ends | Status |
|------|------------------|--------|
| 3.4.x | 2025-12-31 | ended |
| 3.5.x | 2026-06-30 | ended (terminal 3.x minor — no 3.6) |
| 4.0.x | 2026-12-31 | active |
| **4.1.x** | 2027-07-31 | **current GA** |
| 4.2.x | 2027-12-31 | scheduled 2026-11-30 (M1 only) |

Boot 4 deltas the Guide depends on: Jackson **3** default (`tools.jackson.JsonMapper`; Jackson 2
`ObjectMapper` deprecated behind `spring.jackson.use-jackson2-defaults`); Spring Security 7 (no `and()`
DSL, Access API moved to `spring-security-access`); modularised auto-configuration (starters delegate to
`spring-boot-<technology>` modules). Java floor on Boot 4 is 17, so **Java 21+ is unaffected**.

What **Spring Boot 4.1.x manages** (verified 2026-10-04): Spring Framework **7.0.x**, Spring Security
**7.1.x** (a `verified on 4.1.x` note about security must cite the 7.1 docs — the parent Feature's
section 8 cites 7.0), Spring Data **4.1.x**, Hibernate ORM **7.4.x**, Jakarta Persistence 3.2, Flyway
12.x, Jackson **3.1.x**, JUnit Jupiter **6.0.x**, Testcontainers 2.0.x. **Not managed:** MapStruct
(1.6.x), ArchUnit (1.5.x) and springdoc-openapi (3.1.x) — a project pins their versions itself and
states the reason (Guide rule G01-07).

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
- The skill must be **pure markdown and self-contained** — no code artifacts, no external tooling dependency,
  no bundled project files (D2 revision, F1)
- The skill is **not Claude-Code-specific** — it is an agent skill expressed as markdown
- Patterns extracted must be applicable to standard Spring Boot projects, not framework-specific forks
- API syntax is looked up from **current framework documentation at execution time** — never assumed
- `skill.md` must stay lean (universal rules only). Phase-specific instructions must never be added to it.
  (**Note:** D18 replaces the whole phase-based design with a contract-first convention — see
  [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] section 14.)
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
python3 scripts/validate-analysis-docs.py guide   # the Guide (Docs/Guide/), exit 0 = valid
python3 -m unittest discover -s scripts/tests -v                          # validator tests
sha256sum --quiet -c scripts/.snapshots/<project>.sha256                  # reference project unchanged
```

## Tooling & patterns
- Any agent runtime can host the skill (it is not Claude-Code-specific); it is authored here as markdown
- Obsidian is used to view/edit the `documentation/` vault (optional — files are plain markdown)
- No CI/CD configured for this workspace (analysis + writing work only)
