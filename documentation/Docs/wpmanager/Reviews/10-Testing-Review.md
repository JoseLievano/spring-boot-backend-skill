# Testing Review — wpmanager

#doc #review #ref-wpmanager #testing

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/13-Testing-Strategy]]

## Scope

Reviewed: every class under `wpmanager/src/test`, the suites and launcher, the test profile, the test fixtures,
and the latest Surefire reports in `wpmanager/target/surefire-reports` (not regenerated; running the suites
would have written only to `target/`, but the E2E tests need the full context and were not required for this
review).

## Verdict

The testing *style* is the best of the three projects: tag-based suites, repository tests for every mapped
entity, and an end-to-end suite that authenticates with real JWTs and asserts 401 and 403. The *coverage* is
narrow. Only the author module has E2E tests, so the authorization holes in other modules went unnoticed, and
the project's most complex and most broken code — upload, idempotency, storage, replication, theme upload — has
no test that executes it. The one test named after uploads never uploads.

## Strengths to Keep

- Real tokens through the production `JwtTokenService` instead of `@WithMockUser`, so the filter and method
  security are exercised (`wpmanager/src/test/java/com/wpmanager/testUtils/TestAuthenticationHelper.java:79-88`).
- Authorization asserted per operation for the author module: anonymous → 401, CLIENT → 403
  (`wpmanager/src/test/java/com/wpmanager/models/author/E2EAuthorTest.java:120-132`,
  `wpmanager/src/test/java/com/wpmanager/models/author/E2EAuthorTest.java:382-401`).
- 68 author E2E tests organised in nested `Get/Insert/Update/Delete` classes with operation tags
  (`wpmanager/src/test/java/com/wpmanager/models/author/E2EAuthorTest.java:115-118`).
- A `@DataJpaTest` class for each of twelve entity modules (142 tests)
  (`wpmanager/src/test/java/com/wpmanager/models/plugin/PluginRepositoryTest.java:23-25`).
- Tag suites and a launcher for selective runs (`wpmanager/src/test/java/com/wpmanager/suites/E2ESuiteTest.java:5-11`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-01\|WP-R10-01]] | Authorization is tested for one module only | 🟠 High | testing | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-02\|WP-R10-02]] | Upload, idempotency, storage and replication are untested | 🟠 High | testing | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-03\|WP-R10-03]] | No service or unit tests; Mockito unused | 🟡 Medium | testing | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-04\|WP-R10-04]] | `URLValidatorTest` depends on the public internet | 🟡 Medium | testing | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-05\|WP-R10-05]] | `contextLoads` needs MySQL and sits outside every suite | 🟡 Medium | testing | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-06\|WP-R10-06]] | Recorded test evidence is stale and misses the upload test | 🟢 Low | testing | Confirmed (test/runtime evidence) |

## Findings

### WP-R10-01
**Title:** Authorization is tested for one module only
**Severity:** 🟠 High
**Category:** testing
**Principle:** Test the security contract of every route, especially inherited ones.
**Evidence:** `E2EAuthorTest` is the only class that calls endpoints with a CLIENT token or none
(`wpmanager/src/test/java/com/wpmanager/models/author/E2EAuthorTest.java:32-37`); the other E2E class never calls an
endpoint (`wpmanager/src/test/java/com/wpmanager/models/plugin/E2EPluginUploadTest.java:144-170`). No test covers
`/admin`, `/client`, `/s3`, `/downloadable`, `/website`, `/fav_list`, `/plan`, `/tests3` or `/client/token`.
**Impact:** The critical authorization gaps — clients deleting admins, reading storage keys, anonymous bucket
access ([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-02|WP-R01-02]],
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-03|WP-R01-03]],
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-04|WP-R01-04]]) — would each have been caught by one test in
the style the author suite already uses.
**Recommendation:** A parameterised authorization matrix: for every controller route × {anonymous, CLIENT,
ADMIN}, assert the expected status; generate the route list from `RequestMappingHandlerMapping` so new routes
fail the test until classified.
**Verified against:** N/A — test design
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/08-Testing-Review#BE-R08-01|BE-R08-01]], [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-02|BT-R09-02]]

### WP-R10-02
**Title:** Upload, idempotency, storage and replication are untested
**Severity:** 🟠 High
**Category:** testing
**Principle:** Test where the risk is.
**Evidence:** No test references `PluginService.upload`, `ThemeService.upload`, `IdempotentUploadService`,
`IdempotencyManager`, `StorageProviderManager`, `S3StorageClient` or `FileDuplicator` (searched under
`wpmanager/src/test`). `E2EPluginUploadTest` saves two providers and a plugin and asserts that two providers exist
(`wpmanager/src/test/java/com/wpmanager/models/plugin/E2EPluginUploadTest.java:152-169`); its fixtures are unused
(`wpmanager/src/test/resources/test-files`).
**Impact:** The Critical upload defects ([[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-01|WP-R03-01]],
[[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-01|WP-R07-01]]) and the idempotency error mapping
([[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01|WP-R04-01]]) all surface on the first run of a simple
test.
**Recommendation:** Integration tests with an S3 emulator (MinIO or LocalStack via Testcontainers) for: plugin
and theme upload happy path, duplicate version, provider down (no row left, retry succeeds), compensation,
replay of a completed key, 409 on an in-flight key, and one replication run including a version with no source.
Unit tests of `IdempotencyManager` transitions with a mocked repository.
**Verified against:** N/A — test design
**Confidence:** Confirmed (read in code)

### WP-R10-03
**Title:** No service or unit tests; Mockito unused
**Severity:** 🟡 Medium
**Category:** testing
**Principle:** Fast tests at the level where logic lives.
**Evidence:** Every test is `@DataJpaTest` or `@SpringBootTest`, except `URLValidatorTest`, which calls the real
network. Mockito is declared (`wpmanager/pom.xml:36-47`) and never imported.
**Impact:** Service rules (plan changes, author moves, website limits, favorite-list diffs) are tested only
through the database or not at all; the feedback loop is slow.
**Recommendation:** Unit tests for services with mocked repositories and storage, especially the rule-heavy
`ClientService.update` and `AuthorService.update`.
**Verified against:** N/A — test design
**Confidence:** Confirmed (read in code)

### WP-R10-04
**Title:** `URLValidatorTest` depends on the public internet
**Severity:** 🟡 Medium
**Category:** testing
**Principle:** Deterministic tests.
**Evidence:** The test probes real domains (`wikipedia.org`, `google.com`, `commons.wikimedia.org`, `x.com`) and a
third-party site chosen for having no TLS (`wpmanager/src/test/java/com/wpmanager/utils/URLValidatorTest.java:28-40`,
`wpmanager/src/test/java/com/wpmanager/utils/URLValidatorTest.java:146-155`).
**Impact:** Fails offline, behind proxies, in CI sandboxes, and whenever a third-party site changes its TLS setup.
**Recommendation:** Split format validation (pure) from reachability (behind an interface); test reachability
against a local server (WireMock with HTTP and a self-signed HTTPS port).
**Verified against:** N/A — test design
**Confidence:** Confirmed (read in code)

### WP-R10-05
**Title:** `contextLoads` needs MySQL and sits outside every suite
**Severity:** 🟡 Medium
**Category:** testing
**Principle:** Tests should run anywhere the build runs.
**Evidence:** `WpmanagerApplicationTests` is an untagged `@SpringBootTest` without a profile
(`wpmanager/src/test/java/com/wpmanager/WpmanagerApplicationTests.java:6-13`), so it uses the MySQL datasource
(`wpmanager/src/main/resources/application.properties:3-6`). The suites select by tag, so it only runs under a
plain `mvn test`.
**Impact:** `mvn test` fails without a local MySQL; the launcher never runs it.
**Recommendation:** Activate the test profile (or Testcontainers MySQL) and tag it.
**Verified against:** N/A — test design
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/08-Testing-Review#BE-R08-03|BE-R08-03]], [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-01|BT-R09-01]]

### WP-R10-06
**Title:** Recorded test evidence is stale and misses the upload test
**Severity:** 🟢 Low
**Category:** testing
**Principle:** Evidence should match the code it claims to cover.
**Evidence:** `wpmanager/target/surefire-reports` has reports for the repository, author E2E and URL validator
tests with no failures, but none for `E2EPluginUploadTest`, although it is tagged `e2e` and the E2E suite would
select it (`wpmanager/src/test/java/com/wpmanager/models/plugin/E2EPluginUploadTest.java:26-30`).
**Impact:** The last recorded run predates the upload test (or excluded it); "all green" does not describe the
current test set. That test would write two provider rows carrying live credentials into the test database.
**Recommendation:** Run the suites in CI on every change ([[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-07|WP-R09-07]]);
rewrite the upload test per WP-R10-02 without real credentials
([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-06|WP-R01-06]]).
**Verified against:** N/A — Surefire reports read as files
**Confidence:** Confirmed (test/runtime evidence)
**Related:** [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-04|BT-R09-04]]

## Recommended Target Pattern

- Unit: services and the idempotency state machine with mocks.
- Slice: `@DataJpaTest` per module (keep).
- Integration: `@SpringBootTest` + Testcontainers (MySQL, MinIO) for upload, replication and compensation.
- Security: a generated route × role matrix using real tokens (keep the helper).
- Tags and a launcher as today; CI runs all suites; no network or real credentials in tests.

## Related Documents

- [[Docs/wpmanager/Explanations/13-Testing-Strategy]]
- [[Docs/wpmanager/Reviews/01-Security-Review]]
- [[Docs/backend/Reviews/08-Testing-Review]]
