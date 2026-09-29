# Testing Review — backend

#doc #review #ref-backend #testing

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/10-Testing-Strategy]]

## Scope

Reviewed: all 21 files under `backend/src/test`, the Surefire configuration, `test.sh`, and the last
recorded Surefire reports in `backend/target/surefire-reports`. The test suite was **not** re-run for
this review (reports are from an earlier run); all counts come from the source and those reports.

## Verdict

The query engine has an exemplary test pyramid — pure unit tests for every rule, repository tests with
real predicates, service tests and MockMvc contract tests with exact error bodies. Everything else is
essentially untested: login, token handling, authorization, and all write operations. The one test that
would catch configuration regressions (`contextLoads`) fails outside Docker. Because every web and
service test runs as a mocked admin, the suite passes while the application enforces no authorization at
all, and the no-op `update` went unnoticed. The pattern to keep is the list-query pyramid; the pattern to
fix is "test only the new subsystem, as an admin".

## Strengths to Keep

- Four-layer coverage of one subsystem: unit → `@DataJpaTest` → service → MockMvc
  (`backend/src/test/java/com/agentForgeBackend/shared/query/QueryableFieldTest.java:22-106`,
  `backend/src/test/java/com/agentForgeBackend/models/hq/admin/AdminRepositoryQuerydslIntegrationTest.java:32-61`,
  `backend/src/test/java/com/agentForgeBackend/models/hq/admin/AdminServiceListQueryIntegrationTest.java:49`,
  `backend/src/test/java/com/agentForgeBackend/models/hq/admin/AdminControllerListEndpointTest.java:56-163`).
- Web tests assert the full error contract (`status`, `error`, `message`), not just the status code
  (`backend/src/test/java/com/agentForgeBackend/models/hq/admin/AdminControllerListEndpointTest.java:93-102`).
- Negative tests for sensitive fields (`password`, `apikey`, `roles`)
  (`backend/src/test/java/com/agentForgeBackend/models/hq/client/ClientControllerListEndpointTest.java:95-115`).
- Deterministic fixtures with fixed instants
  (`backend/src/test/java/com/agentForgeBackend/models/hq/ListQueryTestDataFactory.java:21-23`).
- Behavior-describing test names
  (`backend/src/test/java/com/agentForgeBackend/shared/query/PageableFactoryTest.java:61`).
- Base-class behavior tested in isolation with Mockito and `InOrder`
  (`backend/src/test/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplementsListQueryTest.java:49-76`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-01\|BE-R08-01]] | `@WithMockUser` masks the missing authorization | 🟠 High | testing | Confirmed (read in code) |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-02\|BE-R08-02]] | Login, JWT and all write operations are untested | 🟠 High | testing | Confirmed (read in code) |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-03\|BE-R08-03]] | `contextLoads` fails outside Docker | 🟡 Medium | testing | Confirmed (test/runtime evidence) |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-04\|BE-R08-04]] | Empty suites, unused helper, misleading test | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-05\|BE-R08-05]] | No Testcontainers; tests run on H2 | 🟡 Medium | testing | Confirmed (read in code) |

## Findings

### BE-R08-01
**Title:** `@WithMockUser` masks the missing authorization
**Severity:** 🟠 High
**Category:** testing
**Principle:** Tests must be able to fail for the property they claim to protect.
**Evidence:** Every Spring-context web and service test runs as `@WithMockUser(roles = "ADMIN")` (`backend/src/test/java/com/agentForgeBackend/models/hq/admin/AdminControllerListEndpointTest.java:29`, `backend/src/test/java/com/agentForgeBackend/models/hq/client/ClientControllerListEndpointTest.java:29`, `backend/src/test/java/com/agentForgeBackend/models/hq/admin/AdminServiceListQueryIntegrationTest.java:25`). No test sends a request without authentication or with the `CLIENT` role.
**Impact:** The suite is green while no endpoint requires authentication ([[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]]); adding or removing security rules changes nothing in the test results.
**Recommendation:** For every protected route, add at least: anonymous → 401, wrong role → 403, correct role → 2xx. Prefer real tokens (via the existing `TestAuthenticationHelper`) in a few end-to-end tests so the JWT filter is exercised too.
**Verified against:** N/A — no library API involved (`spring-security-test` already on the classpath)
**Confidence:** Confirmed (read in code)

### BE-R08-02
**Title:** Login, JWT and all write operations are untested
**Severity:** 🟠 High
**Category:** testing
**Principle:** Test the critical paths first.
**Evidence:** No test references `SecurityController`, `/login`, `JwtTokenService` or `JWTTokenValidatorFilter` (other than the unused helper), and none calls `insert`, `update` or `delete` through a service or HTTP (grep over `backend/src/test`). Covered classes are listed in [[Docs/backend/Explanations/10-Testing-Strategy]].
**Impact:** The critical defects in this project — the no-op `update` ([[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01|BE-R02-01]]), ignored account flags ([[Docs/backend/Reviews/01-Security-Review#BE-R01-08|BE-R01-08]]), NPEs on create ([[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-02|BE-R04-02]]) and mapper gaps ([[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03|BE-R04-03]]) — would each have been caught by one straightforward test.
**Recommendation:** Per feature: create (201 + body), read, update (changed field persisted), delete (204, then 404), duplicate (409), invalid input (400). For auth: successful login returns a token, wrong password → 401, disabled user → 401, expired/tampered token → 401.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R08-03
**Title:** `contextLoads` fails outside Docker
**Severity:** 🟡 Medium
**Category:** testing
**Principle:** The default build must be green on a developer machine.
**Evidence:** `@SpringBootTest` without `@ActiveProfiles("test")` (`backend/src/test/java/com/agentForgeBackend/authServerApplicationTests.java:6-11`) loads the production datasource `jdbc:postgresql://db:5432/…` (`backend/src/main/resources/application.properties:5`). The recorded run shows `Tests run: 1, … Errors: 1` (`backend/target/surefire-reports/com.agentForgeBackend.authServerApplicationTests.txt:4`), failing in `batchDataSourceInitializer` (`backend/target/surefire-reports/com.agentForgeBackend.authServerApplicationTests.txt:45`) with `UnknownHostException: db` (`backend/target/surefire-reports/com.agentForgeBackend.authServerApplicationTests.txt:102`).
**Impact:** `mvn test` is red by default, which trains developers to ignore failures; the only whole-context smoke test provides no signal.
**Recommendation:** Annotate it with `@ActiveProfiles("test")` (or run it against Testcontainers), and remove the unused batch starter ([[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-01|BE-R07-01]]). Rename the class to match the application (`AgentForgeBackendApplicationTests`).
**Verified against:** N/A — evidence is the project's own test report
**Confidence:** Confirmed (test/runtime evidence)

### BE-R08-04
**Title:** Empty suites, unused helper, misleading test
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Dead test code misleads as much as dead production code.
**Evidence:** `UtilsSuiteTest` and `E2ESuiteTest` include tags (`utils`, `e2e`) that no test carries (`backend/src/test/java/com/agentForgeBackend/suites/UtilsSuiteTest.java:9`, `backend/src/test/java/com/agentForgeBackend/suites/E2ESuiteTest.java:9`); `TestAuthenticationHelper` is used by no test (`backend/src/test/java/com/agentForgeBackend/testUtils/TestAuthenticationHelper.java:16-18`); `testClientWithPlanRelationship` checks no plan (there is no plan entity) (`backend/src/test/java/com/agentForgeBackend/models/client/ClientRepositoryTest.java:209-217`); `testFindByEmailCaseInsensitive` has its decisive assertion commented out (`backend/src/test/java/com/agentForgeBackend/models/client/ClientRepositoryTest.java:258-262`); `ClientRepositoryTest` sits in `models.client` instead of `models.hq.client`. The suites' `@ExcludeClassNamePatterns(".*Suite*")` is a no-op: as a Java regex it means "…Suit" followed by any number of `e` characters, which never matches a full class name ending in `Test` (`backend/src/test/java/com/agentForgeBackend/suites/RepositorySuiteTest.java:7`); only `@ExcludePackages` keeps the suites from selecting themselves.
**Impact:** Readers assume coverage that does not exist; copy-paste of these patterns spreads them.
**Recommendation:** Delete empty suites and the misleading test, move `ClientRepositoryTest` to its feature package, and either use `TestAuthenticationHelper` in the new security tests (see [[Docs/backend/Reviews/08-Testing-Review#BE-R08-01|BE-R08-01]]) or delete it.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R08-05
**Title:** No Testcontainers; tests run on H2
**Severity:** 🟡 Medium
**Category:** testing
**Principle:** Test against the production engine.
**Evidence:** All integration tests use the H2 `test` profile (`backend/src/main/resources/application-test.properties:4`); no Testcontainers dependency (`backend/pom.xml:35-169`). Root cause and impact are described in [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-06|BE-R03-06]].
**Impact:** SQL differences between H2 (MySQL mode) and PostgreSQL — especially in the case-insensitive `LIKE` operators of the query engine — are never tested.
**Recommendation:** Add `spring-boot-testcontainers` + `org.testcontainers:postgresql`, declare a `@TestConfiguration` with `@Bean @ServiceConnection PostgreSQLContainer<?>`, and import it in repository and integration tests.
**Verified against:** `/spring-projects/spring-boot/v3.4.1` (Context7; `blob/v3.4.1/…/testing/testcontainers.adoc` — service connections for JDBC containers)
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- Keep the four-layer pyramid for every non-trivial subsystem (unit → slice → service → MockMvc).
- For every feature: CRUD happy paths **and** error paths through MockMvc, asserting status and body.
- For every protected route: anonymous, wrong-role and right-role tests; a few tests with real JWTs.
- Integration tests on PostgreSQL via Testcontainers `@ServiceConnection`; configuration in
  `src/test/resources`.
- A context smoke test that passes on a clean checkout; no empty suites.

## Related Documents

- [[Docs/backend/Explanations/10-Testing-Strategy]]
- [[Docs/backend/Reviews/01-Security-Review]]
- [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review]]
