# Phase 0 — Project Onboarding and Environment Assessment

This phase is mandatory at the start of every new project engagement. Its purpose is
to determine the current project state, measure alignment with the target architecture,
and produce a stable, documented context for all subsequent work.

Do not generate code, advise on architecture, or modify any project file until this
phase is complete.

---

## Freshness Check

At the start of each session, run this check before deciding whether full onboarding
is needed:

1. Read the memory bank using the `memory-bank` skill.
2. Check whether a completed onboarding record exists in memory.
3. **No record found** → run the full onboarding sequence below.
4. **Record found** → spot-check the following high-signal files against what is
   recorded in memory:
   - `pom.xml` or `build.gradle` — detects version or dependency changes
   - `application.yml` / `application.properties` — detects config or profile changes
   - root package structure — detects new modules or structural shifts
5. If all spot-checked signals match memory → onboarding is current; proceed to work.
6. If any signal differs from memory → re-run only the affected onboarding sections
   and update memory before proceeding.

### Re-onboarding Commands

The following user requests trigger a full re-run of the onboarding sequence,
bypassing the freshness check:

- `re-onboard project`
- `refresh project assessment`
- `rebuild project context`

Re-onboarding overwrites the prior onboarding output. It does not delete other
documentation or memory bank content.

---

## Full Onboarding Sequence

Execute these steps in order. Do not skip steps. Do not reorder steps.

**Step 1 — Inspect repository structure.**
Use `tree` or equivalent to map the top-level directory layout. Identify the presence
of `pom.xml`, `build.gradle`, `settings.gradle`, `src/`, `documentation/`, and any
module subdirectories.

**Step 2 — Determine whether project documentation exists.**
Check for a `documentation/` directory. Note whether it contains a memory bank at
`documentation/Memory/`.

**Step 3 — Initialize or validate `documentation/`.**
- If `documentation/` does not exist: initialize it using the `documentation-management`
  skill.
- If `documentation/` already exists: read its structure and use the existing layout.
  Do not recreate or restructure existing documentation.

**Step 4 — Initialize or validate `documentation/Memory/`.**
- If `documentation/Memory/` does not exist: initialize the memory bank using the
  `memory-bank` skill.
- If `documentation/Memory/` already exists: proceed to Step 5.

**Step 5 — Read memory bank.**
Using the `memory-bank` skill, read all core memory bank files. Load prior onboarding
findings, known conventions, and any recorded project context into working state.

**Step 6 — Stack fingerprint discovery.**
Read the following files and record evidence for each item below. Do not infer from
defaults — every item must be answered from file evidence or recorded as unknown.

Required files to read:
- `pom.xml` or `build.gradle` (and `build.gradle.kts` if present)
- `settings.gradle` or `settings.gradle.kts`
- `application.yml`, `application.properties`, and all profile-specific variants
- Security configuration classes (search for `@Configuration` + `SecurityFilterChain`
  or `WebSecurityConfigurerAdapter`)
- Persistence configuration classes (search for `@Configuration` + `DataSource` or JPA)

Items to discover and record:
- Java version (from `<java.version>`, toolchain config, or compiler plugin)
- Spring Boot version (from parent POM or BOM import)
- Build tool: Maven or Gradle; whether `mvnw` or `gradlew` wrapper is present
- Dependency management approach: parent POM inheritance, BOM import, or neither
- Database engine and JDBC driver (from dependencies and datasource config)
- Schema migration tooling: Flyway, Liquibase, or absent (check dependencies and
  `resources/db/migration/` or `resources/db/changelog/`)
- Security/authentication stack: Spring Security present or absent; JWT, session-based,
  OAuth2, or no auth visible
- Testing framework: JUnit version, Mockito, Testcontainers, or other test dependencies
- Configuration file style: `.yml` or `.properties`; active profiles declared
- Major integrations visible in dependencies or config: mail, messaging, caching,
  storage, external APIs

**Step 7 — Structural inventory and maturity classification.**

**Multi-module detection — execute first:**
- Check for `<modules>` in a parent `pom.xml` (Maven multi-module).
- Check for `include` directives in `settings.gradle` / `settings.gradle.kts`
  (Gradle composite build).
- If multi-module layout is detected: document the module names, their roles, and
  which module is the application entry point. All subsequent inventory must operate
  at the correct module level.

**Inventory the following components.** For each, record: present/absent, location,
and observable conventions (naming, patterns, annotations used).

- Package structure: root package name and top-level subpackage organization
- Entities: count, annotations (`@Entity`, `@Document`, etc.), naming patterns
- Repositories: style (JPA, JDBC, custom), naming, base classes or interfaces used
- Service layer: naming conventions, `@Transactional` usage, interface vs. concrete class
- Controllers: REST annotations, request mapping patterns, response handling style
- DTOs: presence, naming conventions, separation from entities
- Mapping strategy: MapStruct, ModelMapper, manual mapping, or none
- Validation: Bean Validation (`@Valid`, `@NotNull`, etc.) usage and placement
- Security configuration: filter chain definition, auth mechanisms, role/permission model
- Exception handling: `@ControllerAdvice`, `@RestControllerAdvice`, custom exception
  hierarchy, error response shape
- Test structure: unit test presence, integration test presence, test naming conventions

**Inspection depth limits:**
- Always fully read: build files, config files, security/persistence configuration classes.
- Sample representatively: one controller, one service, one entity, one repository,
  one exception handler. This is sufficient to detect conventions.
- For large codebases: document what was inspected and explicitly flag areas not covered.

**Assign maturity state** — choose exactly one:
- `bootstrap-only` — minimal starter structure, no domain code
- `partially implemented backend` — some domain layers exist but incomplete
- `mature backend with established conventions` — all major layers present and consistent
- `non-standard layout needing special handling` — structure deviates significantly from
  standard Spring Boot layout; requires clarification before proceeding

**Assign alignment state** — choose exactly one:
- `fully aligned` — all present components match the target architecture
- `partially aligned` — some components match, others conflict or are absent
- `misaligned` — existing implementation conflicts significantly with the target
- `unknown` — insufficient signals to assess alignment; requires user clarification

Both states must be recorded. Maturity determines how cautious the agent should be
overall. Alignment determines which areas are safe to extend, which need documentation
before coding, and which require clarification before transformation.

**Step 8 — Document findings.**
Using the `documentation-management` skill and the `memory-bank` skill, write the
onboarding output document. See [Required Onboarding Output](#required-onboarding-output)
below. Do not write files outside these skill interfaces.

**Step 9 — Separate facts, inferences, and unknowns.**
In the onboarding output, classify every finding as one of:
- **FACT:** directly read from a file in the repository
- **INFERENCE:** conclusion drawn from repository signals (state the signal)
- **UNKNOWN:** signal missing, ambiguous, or unresolvable without user input

Do not resolve contradictions silently. If two signals conflict (e.g., Spring Boot 2.x
in `pom.xml` but 3.x-style imports in source code), record the contradiction, surface
it in the output, and require user clarification before proceeding.

Do not proceed with implementation if critical unknowns remain unresolved. Log
unreadable or missing expected files as high-priority unknowns.

**Step 10 — Recommend safe next actions.**
Produce a prioritized list of safe next steps framed around the path to the target
architecture:
- Build missing components directly to the target architecture (bootstrap or absent areas)
- Transform conflicting components toward the target architecture (misaligned areas)
- Extend already-aligned components following target architecture conventions
- Document gaps or conflicting areas before making changes
- Ask targeted clarification questions if alignment state is unknown or contradictory

---

## Required Onboarding Output

The onboarding findings document must include all of the following sections:

| Section | Content |
|---------|---------|
| Project state summary | One-paragraph description of current project state |
| Technology/version summary | All stack fingerprint items from Step 6 |
| Maturity state | Label + rationale |
| Alignment state | Label + rationale |
| Module inventory | Module layout (including multi-module structure if applicable) |
| Package structure | Root package, top-level subpackages |
| Aligned components | What already matches the target architecture |
| Conflicting components | What conflicts and what transformation is needed |
| Absent components | What is missing and what needs to be built |
| Facts / Inferences / Unknowns | All findings classified by epistemic status |
| Gaps and high-priority unknowns | Anything blocking safe next actions |
| Safe next actions | Prioritized list from Step 10 |

---

## Phase Completion

Phase 0 is complete when:
- All 10 steps have been executed
- The onboarding output document has been written to `documentation/`
- The findings have been saved to the memory bank
- No critical unknowns remain unresolved

When Phase 0 is complete, record its completion in the memory bank and report the
maturity state, alignment state, and recommended next phase to the user.
