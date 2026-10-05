# Known Issues & Constraints

This file tracks architectural gotchas, systemic patterns, and non-obvious constraints.
It is NOT a bug tracker — specific bugs belong in `documentation/Bugs/`.

## Environmental
- The three reference projects have not been built or run — they are used as static analysis subjects only.
  Verify that all source files are present and complete before drawing conclusions from them.

- `backend/` is the replacement for the former `auth-server/`; its `pom.xml` `<name>` is still `authServer`.
  Treat `backend/` as the source of truth — `auth-server/` no longer exists in the working tree.
- `BugTracker/` and `wpmanager/` are tracked as git submodules (gitlinks) in this repo.

- **Spring Boot support status (verified 2026-10-03 via `api.spring.io/projects/spring-boot/generations/...`):**
  every Boot **3.x** line is past OSS end-of-support (3.5 ended 2026-06-30 and is the terminal 3.x minor —
  there is no 3.6; 3.4 ended 2025-12-31). Boot **4.1.x** is the current GA (OSS until 2027-07-31); 4.2 GA is
  scheduled 2026-11-30. Never endorse an EOL line — that is the BT-R08-01 failure mode. Boot 4 changes the
  Guide's dependencies: Jackson **3** is the default JSON stack (`tools.jackson.JsonMapper`; Jackson 2
  `ObjectMapper` deprecated behind `spring.jackson.use-jackson2-defaults`), Spring Security 7 removes the
  `and()` DSL and moves the Access API, and auto-configuration is modularised (starters delegate to
  `spring-boot-<technology>` modules). The version baseline is a **support policy** (floor = the currently
  OSS-supported generation), not a hard-coded major — see the F11 decision in
  [[Bugs/done/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]].


- Context7 versioned Spring IDs can return snippets from `main` (Boot 4). Trust a snippet as
  version-verified only when its source URL has the matching tag. Spring Security 5.7 is not indexed;
  use `javap` on the jars in `~/.m2/repository` (5.7.1, 6.4.2 are present) as the exact-version source.
- Local `~/.m2` holds the exact BugTracker-era jars (spring-tx 5.3.20, spring-boot(-autoconfigure) 2.7.0,
  spring-data-jpa 2.7.0, spring-data-rest-core 3.7.0, spring-security 5.7.1, jjwt 0.11.2, hibernate-core
  5.6.9) — `javap` on them is the exact-version source for Boot 2.7 behavior; Context7's `v2.7.18` ID returned
  `main` snippets. Spring Boot support dates: `https://api.spring.io/projects/spring-boot/generations/<x.y>.x`.
- `BugTracker/` and `wpmanager/` are gitlinks, so `git status` does not see edits inside them; guard each with
  a checksum snapshot (`scripts/.snapshots/bugtracker.sha256` exists).
- `backend/` **is tracked in git** (281 files, commit `9f224e1`), so `git status` does see edits to it.
  A checksum snapshot (`scripts/.snapshots/backend.sha256`) is still kept for parity with the other two and
  to catch changes that are committed-but-unintended during analysis. (An earlier note claiming `backend/`
  was untracked was stale and has been corrected.)
- This workspace's git history (commits `cc7d212`, `e0993c4`) still contains `auth-server/` properties
  with a literal database password and secret defaults. Cite the commit, never the values.
- `wpmanager/src/test/.../E2EPluginUploadTest.java` contains committed cloud-storage keys (lines 61-62, 75-76).
  Never copy them into docs. The owner knows they (and the history secrets) are exposed and has
  accepted this as a known bad practice for now ([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-06|WP-R01-06]]).
- `wpmanager/wpManagerDocs/` is the reference project's own Obsidian vault (with `.obsidian/` config). Never open
  it in Obsidian during analysis: Obsidian rewrites workspace files and the checksum guard
  (`scripts/.snapshots/wpmanager.sha256`) fails. Its docs have drifted from the code (WP-R11-04); use it as intent
  only.

## Framework / Library behaviors
- A `SecurityFilterChain` without `authorizeHttpRequests` permits every request; `@PreAuthorize` does
  nothing without `@EnableMethodSecurity`. Reference projects rely on both being present when they are not.
- `@EnableMethodSecurity` works (globally) even on a `@Service`: any `@Component` is a lite configuration
  candidate whose `@Import` is processed. wpmanager relies on this; deleting those services silently disables
  all method security (what happened in `backend/`).
- `spring.task.scheduling.enabled` is not a Spring Boot property; `@EnableScheduling` cannot be switched off by it.
- In Spring Security 6.x, `UserDetails.isEnabled()` & co. are `default` methods returning `true`; an
  adapter that defines `getEnabled()` instead compiles fine and silently ignores account flags.
- Hibernate 6 renamed bind-parameter logging to `org.hibernate.orm.jdbc.bind`; Hibernate 5's
  `...type.descriptor.sql.BasicBinder` category is inert.
- Spring Data REST on the classpath exports every public repository by default.
- `docs.spring.io` redirects the versioned URL of the **current** documentation line to the unversioned
  address (seen 2026-10-04 for Spring Boot 4.1 and Spring Framework 7.0; Spring Security 7.0 serves as
  written). A Guide Version Note still cites the **versioned** form: it keeps pointing at the same text
  after the next release, and the `guide` target counts only URLs whose path has a version segment.

- Spring Boot **4.1.x manages Spring Security 7.1.x** (and Spring Framework 7.0.x, Spring Data 4.1.x) —
  read 2026-10-04 from `docs.spring.io/spring-boot/4.1/appendix/dependency-versions/coordinates.html`. A
  `verified on 4.1.x` Version Note about Spring Security must cite the **7.1** documentation; the parent
  Feature's section 8 cites the 7.0 reference. MapStruct, ArchUnit and springdoc-openapi are not in
  Boot's managed set.
- Context7's `/mapstruct/mapstruct/1_6_3` and `/tng/archunit` returned snippets from `main`; the tagged
  sources (`mapstruct.org/documentation/1.6/…`, GitHub `blob/v1.5.1/…`) are the version-matched ones.

## Architectural constraints
- The skill output must be self-contained markdown — it cannot depend on external scripts or tools
- Every Guide rule cites a finding ID or an ADR; the reference projects are evidence, not authority
  ([[ADRs/ADR-001-guide-is-the-source-of-truth|ADR-001]]). A pattern does not have to appear in all three
  projects.
- ADRs are cited as `ADR-NNN` (three digits = the filename prefix). An accepted ADR is never edited: a
  changed decision is a superseding ADR. The Bug Report in `Bugs/done/` keeps its old four-digit spellings
  as history — do not normalise a closed record.
- A Guide document never links a Feature, Task or Bug document: those move between status directories and
  the `guide` target would then fail on the dangling link. Link the ADR that records the decision instead
  (enforced by `check_stable_links` in `scripts/validate-analysis-docs.py`).
- A Guide document names a **planned** document in inline code (`06-Query-Engine`) and cites no Rule ID
  of an unwritten document — the validator rejects a link or a Rule ID that does not resolve. The Task
  that writes the target turns the name into a wiki link (Task 3 left 24 such names for Tasks 4–5).
- `glossary search` matches term **names** only; a synonym is not found. Search
  `documentation/Glossary/Glossary.md` for a synonym (`rg -n "CrudService" documentation/Glossary/Glossary.md`).
- **Skill files must be written to `spring-boot-skill/` — never directly to `~/.claude/plugins/` or
  any Claude Code system directory during development.** Migration happens only when the skill is
  finished and explicitly approved. See [[Docs/Skill-Directory-Convention]].

## Patterns to avoid
- **Never hardcode or commit secrets.** The reference projects commit DB passwords, JWT keys, seed passwords
  and cloud-storage keys (BE-R01-11, BT-R01-03, WP-R01-06, plus git history `cc7d212`/`e0993c4`). The owner
  accepted leaving those in place for now (2026-09-29) — do not re-raise rotation as an action item — but it is
  a known bad practice that must not be repeated: the skill and anything new in this workspace read secrets
  from environment variables / external config only, with no literal fallback defaults in source or committed
  properties.
- Do not write analysis claims from Explore-agent inventories without reading the cited lines; three
  Task 1 leads were wrong on inspection (see [[Memory/progress]] 2026-09-29).
- The `Write` tool may refuse `.md` paths when running as a subagent; authoring in the scratchpad and
  copying into place works.

## Sharp edges
- If the three projects use different Spring Boot major versions, some patterns may not be
  forward-compatible — note version-specific patterns explicitly
