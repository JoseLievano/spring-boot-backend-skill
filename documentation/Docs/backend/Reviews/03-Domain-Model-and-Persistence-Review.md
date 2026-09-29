# Domain Model and Persistence Review — backend

#doc #review #ref-backend #persistence

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/06-Domain-Model-and-Persistence]]

## Scope

Reviewed: `shared/models/baseUser/*`, `models/hq/*/…Entity`, all repositories, transaction annotations,
persistence properties in both profiles, and how forms and entities carry validation. Excluded: mapper
correctness (see [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review]]).

## Verdict

The persistence setup is small and conventional: a JOINED user hierarchy with shared unique username and
e-mail, derived-query repositories, `open-in-view` disabled. The weaknesses are the ones that bite
later in a project's life: roles stored by ordinal, no schema migrations, audit columns that are never
written, validation living on entities instead of inputs, and a test database that is a different
engine in a different compatibility mode. None of these breaks today's two-entity model; all of them
make growth riskier.

## Strengths to Keep

- JOINED inheritance with login-relevant columns on the root lets one `BaseUserRepository` authenticate
  every user type (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:17`,
  `backend/src/main/java/com/agentForgeBackend/shared/securityUser/BaseUserRepository.java:8-12`).
- Uniqueness is enforced by database constraints, not only by service checks
  (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:33`,
  `backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:45`).
- `spring.jpa.open-in-view=false` in both profiles
  (`backend/src/main/resources/application.properties:19`,
  `backend/src/main/resources/application-test.properties:14`).
- Entities use `@Getter @Setter` instead of `@Data`, avoiding Lombok-generated `equals`/`hashCode` over
  lazy associations (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientEntity.java:12-17`).
- Explicit table names (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminEntity.java:11`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-01\|BE-R03-01]] | Anemic entities; invariants scattered in services and mappers | 🟡 Medium | design | Confirmed (read in code) |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-02\|BE-R03-02]] | Audit dates are never written but are exposed and sortable | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03\|BE-R03-03]] | Roles stored as ordinals | 🟠 High | persistence | Confirmed (read in code) |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-04\|BE-R03-04]] | No migrations; `ddl-auto=update` in the runtime config | 🟠 High | persistence | Confirmed (read in code) |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-05\|BE-R03-05]] | Repository finders mix nullable returns and `Optional` | 🟢 Low | design | Confirmed (read in code) |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-06\|BE-R03-06]] | Tests use H2 in MySQL mode; production is PostgreSQL | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-07\|BE-R03-07]] | Validation constraints on entities, not on forms | 🟡 Medium | design | Confirmed (read in code) |

## Findings

### BE-R03-01
**Title:** Anemic entities; invariants scattered in services and mappers
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Information hiding / cohesion — the rules that keep a user valid (role assignment, password hashing, uniqueness, required fields) should sit next to the data they protect.
**Evidence:** Entities are Lombok getter/setter bags (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:13-16`). The "admin has role ADMIN and a hashed password" rule lives in `AdminServiceImpl.insert` (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java:49-54`), is bypassed by `AdminBoostrap` (`backend/src/main/java/com/agentForgeBackend/configuration/boostrap/AdminBoostrap.java:39-45`), and the client equivalent is in `ClientService.insert` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:100-102`).
**Impact:** Each new code path that creates users must re-implement the rules; the seed path already diverges (it trusts roles from a form).
**Recommendation:** Give entities intention-revealing factories and methods, e.g. `AdminEntity.create(username, email, rawPassword, PasswordEncoder)` and `user.disable()`, make setters for invariant fields non-public, and let services orchestrate rather than assemble.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R03-02
**Title:** Audit dates are never written but are exposed and sortable
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** No dead fields in the contract.
**Evidence:** `dateCreated` and `lastLogin` columns (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:52-56`) have no writer in `src/main` (no `@PrePersist`, no auditing, no setter call; login does not touch `lastLogin`, `backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityController.java:34-66`). They are returned in list rows and are filterable/sortable (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminQueryProfile.java:25-28`). Only test fixtures set them (`backend/src/test/java/com/agentForgeBackend/models/hq/ListQueryTestDataFactory.java:37-38`).
**Impact:** In production every row has `null` dates; sorting and date filters return meaningless results, while tests pass because fixtures fill the columns.
**Recommendation:** Enable Spring Data JPA auditing (`@EnableJpaAuditing`, `@EntityListeners(AuditingEntityListener.class)`, `@CreatedDate`/`@LastModifiedDate` with `Instant`) and set `lastLogin` in the login flow (an `AuthenticationSuccessEvent` listener keeps the controller thin).
**Verified against:** `spring-data-jpa-3.4.1.jar` (`EnableJpaAuditing`, `AuditingEntityListener` present) and `spring-data-commons-3.4.1.jar` (`@CreatedDate`, `@LastModifiedDate` present) — jar listing
**Confidence:** Confirmed (read in code)

### BE-R03-03
**Title:** Roles stored as ordinals
**Severity:** 🟠 High
**Category:** persistence
**Principle:** Persisted identifiers must be stable; enum order is not.
**Evidence:** `@ElementCollection` of `UserRoles` with `@Column(name = "role")` and no `@Enumerated` (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:37-43`); JPA's default is `EnumType.ORDINAL`. Enum order: `ADMIN, CLIENT, EMPLOYEE` (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/UserRoles.java:3-7`).
**Impact:** Inserting or reordering a constant (e.g. adding `SUPERADMIN` first) silently changes every stored user's role — a privilege-escalation or lock-out bug introduced by a harmless-looking edit. The DB column (`0`, `1`, `2`) is unreadable for operators.
**Recommendation:** Add `@Enumerated(EnumType.STRING)` to the collection element and migrate existing rows (`UPDATE user_roles SET role = CASE role WHEN '0' THEN 'ADMIN' …`) in a versioned migration.
**Verified against:** N/A — Jakarta Persistence default (`EnumType.ORDINAL`), not library-version specific
**Confidence:** Confirmed (read in code)

### BE-R03-04
**Title:** No migrations; `ddl-auto=update` in the runtime config
**Severity:** 🟠 High
**Category:** persistence
**Principle:** Schema is versioned code.
**Evidence:** `spring.jpa.hibernate.ddl-auto=update` in the only non-test configuration (`backend/src/main/resources/application.properties:16`); no Flyway/Liquibase dependency (`backend/pom.xml:35-169`).
**Impact:** Hibernate never drops or renames columns, cannot migrate data (e.g. the ordinal fix in [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03|BE-R03-03]]), and schema drift between environments is invisible. Start-up DDL on a shared database is an operational risk.
**Recommendation:** Add Flyway (`flyway-core` + `flyway-database-postgresql`), put `V1__baseline.sql` under `db/migration`, and set `spring.jpa.hibernate.ddl-auto=validate` so entity/schema mismatches fail at start-up.
**Verified against:** `spring-boot-autoconfigure-3.4.1.jar` (contains `flyway/FlywayAutoConfiguration`); `/spring-projects/spring-boot/v3.4.1` (Context7; the returned Flyway smoke-test snippet — `ddl-auto` set to `validate` when Flyway manages the schema — came from `main`, so not version-pinned)
**Confidence:** Confirmed (read in code)

### BE-R03-05
**Title:** Repository finders mix nullable returns and `Optional`
**Severity:** 🟢 Low
**Category:** design
**Principle:** Consistent interfaces reduce cognitive load.
**Evidence:** `AdminEntity findByUsername(String)` / `findByEmail` return `null` when absent (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminRepository.java:9-11`); `ClientRepository` and `BaseUserRepository` return `Optional` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientRepository.java:11-13`, `backend/src/main/java/com/agentForgeBackend/shared/securityUser/BaseUserRepository.java:10`). `AuthUserUtil` must use `map` for one and `flatMap` for the other (`backend/src/main/java/com/agentForgeBackend/shared/tools/AuthUserUtil.java:30-38`).
**Impact:** Null checks are easy to forget on the Admin side; code that works for one feature does not compile for the other.
**Recommendation:** Return `Optional<T>` from all single-result finders; add `existsByUsername`/`existsByEmail` for the uniqueness checks.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R03-06
**Title:** Tests use H2 in MySQL mode; production is PostgreSQL
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** Test against the engine you run.
**Evidence:** `jdbc:h2:mem:testdb;MODE=MySQL;…` with `H2Dialect` (`backend/src/main/resources/application-test.properties:4`, `backend/src/main/resources/application-test.properties:12`) versus `jdbc:postgresql://…` with `PostgreSQLDialect` (`backend/src/main/resources/application.properties:5`, `backend/src/main/resources/application.properties:17`). MySQL mode is a leftover from the MySQL-based predecessor. `ClientRepositoryTest` even notes that case-sensitivity "depends on database configuration" and leaves the assertion commented out (`backend/src/test/java/com/agentForgeBackend/models/client/ClientRepositoryTest.java:248-264`).
**Impact:** Case sensitivity, `ILIKE`/`lower()` behavior, identity generation, reserved words and constraint names can differ; the query engine's case-insensitive operators are verified on the wrong engine.
**Recommendation:** Run repository and integration tests on PostgreSQL via Testcontainers with `@ServiceConnection` (Boot ≥ 3.1). See [[Docs/backend/Reviews/08-Testing-Review#BE-R08-05|BE-R08-05]].
**Verified against:** `/spring-projects/spring-boot/v3.4.1` (Context7; `blob/v3.4.1/…/testing/testcontainers.adoc` lists service-connection support for JDBC containers)
**Confidence:** Confirmed (read in code)

### BE-R03-07
**Title:** Validation constraints on entities, not on forms
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Validate at the boundary; fail with 400 before touching the database.
**Evidence:** `@Size` constraints on `BaseUserEntity` (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:25-47`); `AdminForm` only has `@NotBlank` on `username` plus a class-level `@Validated` that does nothing on a DTO (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminForm.java:11-30`); `ClientForm` has no constraints (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientForm.java:5-16`), so `ClientService.insert` validates by hand (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:62-79`).
**Impact:** `@Valid` on the controller accepts almost anything; entity violations surface at flush time as unhandled exceptions (500, Boot's default body) instead of a clean 400; manual checks duplicate and drift (e.g. a `null` name causes an NPE, [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-02|BE-R04-02]]).
**Recommendation:** Put `@NotBlank`, `@Email`, `@Size` on form fields, remove the manual null/empty checks, and keep entity constraints only as a DB-level safety net (column lengths, `nullable = false`).
**Verified against:** N/A — no library API involved (standard Jakarta Bean Validation annotations already used by the project)
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- JOINED user root as today, with `@Enumerated(EnumType.STRING)` roles and `Instant` audit columns
  filled by Spring Data auditing.
- Flyway migrations under `src/main/resources/db/migration`, `ddl-auto=validate`.
- Entities expose factory methods and behavior (`create`, `changeEmail`, `disable`) and guard their
  invariants; setters for protected fields are not public.
- Forms carry all input validation; entities carry only storage constraints.
- Repositories return `Optional` and offer `existsBy…` finders.
- Repository/integration tests run on PostgreSQL via Testcontainers `@ServiceConnection`.

## Related Documents

- [[Docs/backend/Explanations/06-Domain-Model-and-Persistence]]
- [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review]]
- [[Docs/backend/Reviews/08-Testing-Review]]
