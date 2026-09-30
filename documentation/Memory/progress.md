# Progress

## 2026-09-30 (New action plan)
- **Feature written:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] — ADRs → Guide
  (`Docs/Guide/`, rule IDs) → Base Project (platform modules) → rewritten Skill → Validation Loop (two user
  domains, blind review, strict exit gate). Decisions D1–D18 from a user interview. Auth seam verified against the
  Spring Security 7.0 resource-server docs; MapStruct `@MappingTarget` and `ReportingPolicy.ERROR` verified.
- **Reviewed:** [[Bugs/to-do/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] — 17 findings; the Critical
  one is the conflict with `brief.md` "standalone skill" (the Skill would depend on `base-project/`).

## 2026-09-29 (Phase 1 Tasks 1–3 closed)
- User confirmed the manual validation of [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend|Task 1]],
  [[Tasks/done/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker|Task 2]] and
  [[Tasks/done/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager|Task 3]]; all three moved from `Tasks/current/` to
  `Tasks/done/` and every link updated. Next: Task 4 (cross-project comparison).

## 2026-09-29 (Glossary and secrets decision)
- **Glossary populated** — 26 terms in [[Glossary/Glossary]] across *Analysis Documentation* (Reference Project,
  Explanation Doc, Review Doc, Finding, Finding ID, Strength to Keep, Era Gap, Review Summary, Recipe, Lineage),
  *Backend Patterns* (Feature Module, Generic CRUD Stack, DTO Tiers, Denormalized Counter, Idempotency Key,
  Two-Phase Upload, Compensation) and *Reference Domains* (HQ, Tenant, Storage Provider, Default Provider,
  Replication, Downloadable, File Copy, File Signature, Plan Entitlement), with the user's approval.
- **Committed secrets accepted, not to be repeated** — the owner leaves the exposed reference-project and
  git-history credentials in place for now; recorded as a rule in [[Memory/known-issues]] ("Patterns to avoid").

## 2026-09-29 (Task 3 executed)
- **`wpmanager/` analysis written** — [[Tasks/done/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager]].
  Output: [[Docs/wpmanager/wpmanager-Index]], [[Docs/wpmanager/Reviews/00-Review-Summary]] (88 findings: 9 🔴,
  16 🟠, 43 🟡, 20 🟢; 34 shared with `backend/`). Top issues: no URL-level authorization and an anonymous
  `/tests3` bucket controller (WP-R01-01/02), clients can delete admins and read storage keys through inherited
  CRUD (WP-R01-03/04), live cloud keys in a test (WP-R01-06), a failed provider upload commits a version row that
  blocks the version and stalls replication (WP-R03-01), theme upload never works (WP-R07-01).
  Strongest assets: the idempotency state machine, upload compensation, the S3-compatible provider
  abstraction, real-JWT E2E tests.
- **Method security question answered:** `@EnableMethodSecurity` on three `@Service` classes is honored and
  global (`@Import` on a `@Component` lite configuration candidate — `javap` on spring-context 6.2.1 and
  spring-security-config 6.4.2 — plus passing E2E 401/403 tests). `backend/` lost it by dropping those services.
- Corrections to planning leads: 12 (not 13) `@DataJpaTest` classes; 30 (not 29) open vault bug notes;
  `upload.max-file-size` has a default; authorization *is* tested for the author module; `jakarta.transaction`
  defaults match Spring's (the issue is overriding the class-level rollback rule); `spring.task.scheduling.enabled`
  is not a Boot 3.4.1 property.

## 2026-09-29 (Task 2 executed)
- **`BugTracker/` analysis written** — [[Tasks/done/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker]].
  Output: [[Docs/BugTracker/BugTracker-Index]], [[Docs/BugTracker/Reviews/00-Review-Summary]] (86 findings:
  5 🔴, 19 🟠, 34 🟡, 28 🟢; 81 defects vs 5 era gaps). Top issues: authentication-only authorization
  (BT-R01-01), no tenant isolation (BT-R01-02), JWT key in source (BT-R01-03), no-op base `update`
  (BT-R02-01, same as BE-R02-01), `equals` casting to `PlanEntity` breaks HQ invoicing (BT-R03-01).
  Strongest assets: the operator/tenant domain model, tenant-configurable taxonomies, and the
  filter/sort/page request contract.
- Corrections to planning leads: `BusinessEntity` has **15** EAGER collections (not 14); `EntityFactory`
  confirmed unused; mentions are saved twice but not duplicated; the `bsType` update diff is broken but its
  final join-table state is correct.
- Exact-version verification used local jars with `javap` (spring-tx 5.3.20, spring-boot-autoconfigure 2.7.0,
  spring-data-jpa 2.7.0, spring-data-rest-core 3.7.0, spring-security 5.7.1, jjwt 0.11.2); Boot 2.7 OSS end
  date (2023-06-30) from the spring.io support API.

## 2026-09-29 (Task 1 executed)
- **`backend/` analysis written** — [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend]].
  Output: [[Docs/backend/backend-Index]], [[Docs/backend/Reviews/00-Review-Summary]] (67 findings: 4 🔴,
  10 🟠, 26 🟡, 27 🟢). Top issues: no authorization enforced (BE-R01-01), anonymous client-token minting
  (BE-R01-02), Spring Data REST exposure (BE-R01-03, needs runtime check), no-op generic `update` (BE-R02-01).
  Strongest pattern: the whitelist-first QueryDSL list engine (`shared/query`).
- **Shared format + tooling:** [[Docs/Analysis-Doc-Conventions]] (templates, finding IDs, citation rules,
  changelog) and `scripts/validate-analysis-docs.py <project>` with `scripts/tests/` (44 tests). Tasks 2–3 reuse both.
- Corrections to planning leads: the Mockito pin equals Boot's managed version (only Surefire is
  downgraded); Hibernate bind-parameter logging is inert on Hibernate 6.6; `upload.max-file-size` has a
  default and does not break start-up.
- `backend/` verified unchanged (checksum snapshot `scripts/.snapshots/backend.sha256`, gitignored).

## 2026-09-29
- **Phase 1 analysis planned.** The parent Feature now defines the `Docs/<project>/{Explanations,Reviews}`
  layout and a Task 1–4 breakdown. Three Task docs were created and reviewed:
  [[Tasks/done/Spring-Boot-Skill-Creation-step-1-Analyze-backend]],
  [[Tasks/done/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker]],
  [[Tasks/done/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager]].
- Key facts found during planning:
  - Stacks: BugTracker is Boot 2.7.0 (javax, Java 17 effective); backend and wpmanager are Boot 3.4.1 / Java 21.
  - Lineage: BugTracker → wpmanager → backend. The generic CRUD stack evolved across all three.
  - All three lack enforced URL-level authorization (see the tasks).
- Glossary initialized at `documentation/Glossary/`.

## 2026-09-28
- **Reference project swap** — `backend/` replaces the former `auth-server/` as reference project #1.
  Auth is organized differently from the old auth-server, so earlier assumptions about auth-server do
  not apply. `backend/` is Spring Boot 3.4.1 (artifactId `agentForgeBackend`, `<name>` still `authServer`).
  Updated AGENTS.md, memory bank, [[Docs/Skill-Architecture]] and [[Features/to-do/Spring-Boot-Skill-Creation]].

## 2026-03-29
- **Skill v1 created** — `spring-boot-skill/skill.md` and `spring-boot-skill/references/phase-0-onboarding.md`
  written. Phase 0 (Project Onboarding and Environment Assessment) is fully specified and is the
  active phase. All four design decisions from the feature spec are encoded:
  - Decision 1: conflict resolution with `--override-convention` flag
  - Decision 2: freshness check + delta onboarding (not full re-run every session)
  - Decision 3: four maturity states + structural inventory for precision
  - Decision 4: onboarding is mandatory and cannot be skipped
- **Skill file organization decided** — phase-based progressive disclosure model adopted.
  `skill.md` is a lean router (universal rules + Phase Registry + Phase Selection logic only).
  Each phase's full instructions live in its own `references/phase-N-<name>.md` file, loaded
  only when that phase is active. This avoids loading inactive phase instructions into context.
  → See `architecture.md` → Skill file architecture section.
- Added a detailed feature spec for the first skill instruction block:
  [[Features/to-do/Agent-Project-Onboarding-First-Instructions]]. Decision: onboarding
  must be mandatory default behavior, not only an optional command. Optional command, if
  added later, should be framed as re-onboarding or project-context refresh.
- Established skill directory convention: all skill files go in `spring-boot-skill/` during
  development; migrate to Claude Code system dir only when finalized. → [[Docs/Skill-Directory-Convention]]
- Memory Bank and documentation structure initialized. Three reference Spring Boot projects
  identified: auth-server, BugTracker, wpmanager. No project analysis performed yet.
  Next: deep analysis of each project to extract architectural patterns.
  → [[Features/to-do/Spring-Boot-Skill-Creation]]
