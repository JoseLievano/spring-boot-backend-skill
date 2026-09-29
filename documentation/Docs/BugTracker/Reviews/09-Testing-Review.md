# Testing Review — BugTracker

#doc #review #ref-bugtracker #testing

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/02-Build-Tooling-and-Dependencies]] (no testing explanation — see the index's Merges and Skips)

## Scope

Reviewed: `BugTracker/src/test`, test-scoped dependencies in the POM, and the last recorded Surefire output
under `BugTracker/target/surefire-reports`. The tests were not run (they need a live MySQL, and running them
would write to that database).

## Verdict

There is effectively no test suite: one `contextLoads` test that needs the developer's MySQL. None of the
behavior documented in the other reviews — the authorization gaps, the no-op update, the thread-unsafe filter
engine, the crashing deletes — would have been caught. For the skill, BugTracker contributes nothing to a
testing strategy; its value here is as a list of what must be tested first.

## Strengths to Keep

- The test starter and `spring-security-test` are already declared, so adding tests needs no build change
  (`BugTracker/pom.xml:87-101`).
- The one test is a valid smoke test that proves the context — including all mappings and the `@Lazy`
  wiring — can start (`BugTracker/src/test/java/com/bgsystem/bugtracker/BugTrackerApplicationTests.java:6-13`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-01\|BT-R09-01]] | One test, and it needs a live MySQL | 🟠 High | testing | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-02\|BT-R09-02]] | No coverage of security, services or the filter engine | 🟠 High | testing | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-03\|BT-R09-03]] | Test dependencies declared but unused | 🟢 Low | testing | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-04\|BT-R09-04]] | The last recorded test run predates the current build | 🟢 Low | testing | Confirmed (test/runtime evidence) |

## Findings

### BT-R09-01
**Title:** One test, and it needs a live MySQL
**Severity:** 🟠 High
**Category:** testing
**Principle:** Tests must be hermetic and repeatable.
**Evidence:** `@SpringBootTest class BugTrackerApplicationTests { @Test void contextLoads() {} }`
(`BugTracker/src/test/java/com/bgsystem/bugtracker/BugTrackerApplicationTests.java:6-13`); there are no test
resources (`BugTracker/src/test`), so the test uses the main datasource (`BugTracker/src/main/resources/application.properties:2-4`)
with `ddl-auto=update`, and the seeder runs against it.
**Impact:** `mvn test` fails on any machine without that database, and on the developer's machine it alters
the development schema and may seed data.
**Recommendation:** Testcontainers MySQL (or the production database) with a `test` profile; keep
`contextLoads` as one of many.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/08-Testing-Review#BE-R08-03|BE-R08-03]], [[Docs/backend/Reviews/08-Testing-Review#BE-R08-05|BE-R08-05]]

### BT-R09-02
**Title:** No coverage of security, services or the filter engine
**Severity:** 🟠 High
**Category:** testing
**Principle:** Test the riskiest behavior first.
**Evidence:** The test tree contains one file (`BugTracker/src/test/java/com/bgsystem/bugtracker/BugTrackerApplicationTests.java`).
**Impact:** Every defect in these reviews shipped unnoticed. Prioritized "test first" list for this code base,
by risk:
1. **Security** — a tenant user calling an HQ endpoint gets 403
   ([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01|BT-R01-01]]); tenant A cannot read
   tenant B's task by id or by `/page` ([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02|BT-R01-02]]);
   `POST /user` cannot set roles ([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-04|BT-R01-04]]);
   no token → 401 ([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-08|BT-R01-08]]).
2. **Filter engine** — two concurrent `/page` requests with different filters each get their own rows
   ([[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-01|BT-R05-01]]); `isDone = false`, inherited fields,
   bad dates → 400 ([[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-02|BT-R05-02]] to
   [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-04|BT-R05-04]]); `password` not filterable
   ([[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-07|BT-R05-07]]).
3. **CRUD base** — `PUT` persists a changed field for every module
   ([[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-01|BT-R02-01]]); `POST` with an
   existing `id` creates a new row ([[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-02|BT-R02-02]]).
4. **Service rules** — per-tenant uniqueness, priority reorder isolation, channel with members, category
   delete with several types, second HQ invoice ([[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review]],
   [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-01|BT-R03-01]]).
5. **Error contract** — each domain exception's status ([[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-01|BT-R07-01]]).
**Recommendation:** `@WebMvcTest` + `spring-security-test` for authorization rules with real (not mocked)
security configuration; `@DataJpaTest` with Testcontainers for predicates and repositories; plain unit tests
for mappers and entity invariants; one full-stack test per module for CRUD.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/08-Testing-Review#BE-R08-02|BE-R08-02]]

### BT-R09-03
**Title:** Test dependencies declared but unused
**Severity:** 🟢 Low
**Category:** testing
**Principle:** —
**Evidence:** `spring-batch-test` and `spring-security-test` (`BugTracker/pom.xml:92-101`); nothing in the test
tree imports them.
**Impact:** Suggests test coverage that does not exist; `spring-batch-test` tests a feature the code does not have.
**Recommendation:** Remove `spring-batch-test`; start using `spring-security-test` (see BT-R09-02).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R09-04
**Title:** The last recorded test run predates the current build
**Severity:** 🟢 Low
**Category:** testing
**Principle:** —
**Evidence:** Surefire reports one passing test on Java 16.0.2
(`BugTracker/target/surefire-reports/com.bgsystem.bugtracker.BugTrackerApplicationTests.txt:4`,
`BugTracker/target/surefire-reports/TEST-com.bgsystem.bugtracker.BugTrackerApplicationTests.xml:49`); the POM now
compiles for release 17, which a Java 16 toolchain cannot produce (`BugTracker/pom.xml:183-190`).
**Impact:** There is no evidence that the current code base starts; the only "green" run was made with an
earlier build configuration.
**Recommendation:** Re-establish a CI run on the declared JDK.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (test/runtime evidence)

## Recommended Target Pattern

- Testcontainers-backed integration tests with a `test` profile; no dependency on a developer database.
- Security tests first: role matrix and tenant isolation, asserted per endpoint group.
- Query/predicate tests at the repository level; concurrency test for any shared component.
- One CRUD round-trip test per module (create → read → update → delete), generated from the same template
  as the module.
- CI on the declared JDK.

## Related Documents

- [[Docs/BugTracker/BugTracker-Index]]
- [[Docs/BugTracker/Reviews/00-Review-Summary]]
- [[Docs/backend/Reviews/08-Testing-Review]]
