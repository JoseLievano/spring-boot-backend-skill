# Build, Dependencies and Configuration Review — wpmanager

#doc #review #ref-wpmanager #build #configuration

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/02-Build-Tooling-and-Dependencies]], [[Docs/wpmanager/Explanations/12-Configuration-and-Secrets]]

## Scope

Reviewed: `wpmanager/pom.xml`, both properties files, `test.sh`, `HELP.md`, and the absence of operational
tooling (Actuator, Docker, CI). Secret handling is in [[Docs/wpmanager/Reviews/01-Security-Review]]
(WP-R01-06, WP-R01-07); Spring Data REST exposure in
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-05|WP-R01-05]]; CORS in
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-13|WP-R01-13]]; schema management in
[[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-02|WP-R06-02]].

## Verdict

The build is simple and current (Boot 3.4.1, Java 21, Maven wrapper) but carries six unused starters and two
unused test libraries from project generation, one of which (Data REST) changes the attack surface. The test
profile and its fixed secrets ship in the application jar, one test property does not exist, and the upload
limit is defined twice. For a system whose core promise is replicated storage, there is no health, metrics or
alerting endpoint at all.

## Strengths to Keep

- Current parent and Java level; versions come from the Boot BOM except where Boot does not manage them
  (`wpmanager/pom.xml:5-10`, `wpmanager/pom.xml:29-33`).
- Lombok configured as an annotation processor and excluded from the fat jar
  (`wpmanager/pom.xml:178-201`).
- A suite-based test entry point (`wpmanager/test.sh:1`, `wpmanager/src/test/java/com/wpmanager/TestLauncher.java:11-17`).
- `open-in-view=false` and explicit Hikari sizing (`wpmanager/src/main/resources/application.properties:17-20`).
- Secrets referenced through environment placeholders with a documented rule
  (`wpmanager/src/main/resources/application.properties:22-30`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-01\|WP-R09-01]] | Unused starters and test dependencies | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-02\|WP-R09-02]] | Test profile with fixed secrets ships in the jar | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-03\|WP-R09-03]] | No Actuator, health or metrics for jobs and storage | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-04\|WP-R09-04]] | `spring.task.scheduling.enabled` is not a Spring Boot property | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-05\|WP-R09-05]] | Upload size limit defined in two places | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-06\|WP-R09-06]] | Surefire pinned below Boot; redundant Mockito pin; stale SDK comment | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-07\|WP-R09-07]] | No README, Docker or CI | 🟢 Low | configuration | Confirmed (read in code) |

## Findings

### WP-R09-01
**Title:** Unused starters and test dependencies
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Depend only on what you use; every starter brings auto-configuration.
**Evidence:** `spring-boot-starter-batch`, `-data-jdbc`, `-jdbc`, `-web-services`, `-webflux`, `-websocket`
(`wpmanager/pom.xml:48-55`, `wpmanager/pom.xml:64-67`, `wpmanager/pom.xml:80-91`) and the test artifacts
`reactor-test`, `spring-batch-test`, `mockito-core`, `mockito-junit-jupiter`
(`wpmanager/pom.xml:36-47`, `wpmanager/pom.xml:132-141`) have no import anywhere in `wpmanager/src`.
`spring-boot-starter-data-rest` has no code use but is active (WP-R01-05).
**Impact:** Larger jar and start-up; Batch auto-configures a job repository and launcher against the application
datasource; WebFlux adds reactive infrastructure beans; each unused library adds CVE exposure.
**Recommendation:** Remove them; add each back only with the feature that needs it.
**Verified against:** N/A — dependency usage by `grep`
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-01|BE-R07-01]], [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-04|BT-R08-04]]

### WP-R09-02
**Title:** Test profile with fixed secrets ships in the jar
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Test configuration lives with tests.
**Evidence:** `application-test.properties` is in `src/main/resources` and contains fixed values for both secrets
(`wpmanager/src/main/resources/application-test.properties:21-25`, `<redacted>`).
**Impact:** Starting the production jar with `--spring.profiles.active=test` (by mistake or by an attacker who
controls the environment) switches to an in-memory database with known signing secrets.
**Recommendation:** Move the file to `src/test/resources`; generate test secrets at test start-up.
**Verified against:** N/A — Maven resource layout
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-06|BE-R07-06]]

### WP-R09-03
**Title:** No Actuator, health or metrics for jobs and storage
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Operability: a system that promises replication must report whether it is replicating.
**Evidence:** No `spring-boot-starter-actuator` or Micrometer dependency (`wpmanager/pom.xml:34-163`). The
replication job's outcome is only logged (`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:57-76`);
compensation failures are only logged ("ORPHANED FILE ALERT",
`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:147-151`); the
code even logs a hand-computed throughput metric (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:250-254`).
The vault records the gap (`wpmanager/wpManagerDocs/Bugs/to-do/Add Monitoring and Metrics.md`,
`wpmanager/wpManagerDocs/Bugs/to-do/No Health Check for Scheduled Job.md`).
**Impact:** Replication can stop ([[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02|WP-R05-02]]) and
orphans can accumulate with no signal to operators; load balancers have no readiness probe.
**Recommendation:** Add Actuator with `health` (a custom `HealthIndicator` per provider and one for the
replication backlog) and Micrometer timers/counters for uploads, compensations and replication outcomes;
expose only `health` and `info` publicly.
**Verified against:** N/A — no current usage to verify
**Confidence:** Confirmed (read in code)

### WP-R09-04
**Title:** `spring.task.scheduling.enabled` is not a Spring Boot property
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Configuration keys must exist; unknown keys fail silently.
**Evidence:** `spring.task.scheduling.enabled=false` with the comment "Disable scheduling during tests (to prevent
FileDuplicator from running)" (`wpmanager/src/main/resources/application-test.properties:27-28`). Boot 3.4.1's
metadata has no such key, and `@EnableScheduling` is on the application class
(`wpmanager/src/main/java/com/wpmanager/WpmanagerApplication.java:9`).
**Impact:** Both jobs are scheduled in every `@SpringBootTest`; the cleanup job runs immediately. The replication
job does not run only because of its 83-minute initial delay, so shortening that delay would make E2E tests call
real storage providers.
**Recommendation:** Put `@EnableScheduling` on a `@Configuration` class guarded by
`@ConditionalOnProperty(name = "app.scheduling.enabled", matchIfMissing = true)` and set it to `false` in tests.
**Verified against:** `spring-boot-autoconfigure-3.4.1.jar` (`META-INF/spring-configuration-metadata.json`: `spring.task.scheduling.*` keys are `pool.size`, `shutdown.await-termination`, `shutdown.await-termination-period`, `simple.concurrency-limit`, `thread-name-prefix`)
**Confidence:** Confirmed (read in code)

### WP-R09-05
**Title:** Upload size limit defined in two places
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** One source of truth per setting.
**Evidence:** Spring's multipart limits are 100 MB (`wpmanager/src/main/resources/application.properties:10-11`);
`UploadValidator` reads `upload.max-file-size`, which no file defines, and falls back to its own 100 MB default
(`wpmanager/src/main/java/com/wpmanager/shared/tools/UploadValidator.java:11-12`). The vault says the limit is
"configurable via upload.max-file-size" (`wpmanager/wpManagerDocs/Docs/File Upload Process.md:705`).
**Impact:** Raising one limit without the other has no effect (multipart rejects first) or a confusing one;
multipart rejections are not mapped ([[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-01|WP-R08-01]]).
**Recommendation:** Bind `UploadValidator` to `spring.servlet.multipart.max-file-size` (a `DataSize`), or set
both from one property.
**Verified against:** N/A — no version-specific API
**Confidence:** Confirmed (read in code)

### WP-R09-06
**Title:** Surefire pinned below Boot; redundant Mockito pin; stale SDK comment
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Let the BOM manage versions unless there is a documented reason.
**Evidence:** `maven.surefire.version` 3.2.5 and `mockito.version` 5.14.2
(`wpmanager/pom.xml:31-32`); Boot 3.4.1 manages Surefire 3.5.2 and Mockito 5.14.2. The S3 dependency's comment
links the version 1 `aws-java-sdk` (`wpmanager/pom.xml:152-157`).
**Impact:** Older Surefire for no stated reason; a Mockito pin that will fall behind the next Boot upgrade; a
misleading comment.
**Recommendation:** Remove both pins; import the AWS SDK BOM and fix the comment.
**Verified against:** `spring-boot-dependencies-3.4.1.pom` (`maven-surefire-plugin.version` 3.5.2, `mockito.version` 5.14.2)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-04|BE-R07-04]]

### WP-R09-07
**Title:** No README, Docker or CI
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** A project should explain how to build, configure and run it.
**Evidence:** `wpmanager/HELP.md` is the unedited Spring Initializr file; there is no README, Dockerfile, compose
file or CI workflow in `wpmanager/`. Run instructions live only in the vault's docs
(`wpmanager/wpManagerDocs/Docs/Secret Management.md:125-240`).
**Impact:** New developers must discover the required MySQL database, `FILE_SIGNATURE_SECRET` and
`JWT_SECRET_KEY` by reading code or the vault; tests are not run automatically.
**Recommendation:** A short README (prerequisites, environment variables, `./mvnw` commands), a compose file for
MySQL, and a CI job running `./test.sh`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-08|BT-R08-08]]

## Recommended Target Pattern

- POM: web, data-jpa, security, validation, actuator, the JDBC driver, Lombok, JJWT, the AWS SDK via its BOM,
  Flyway; tests: starter-test, security-test, suite engine, Testcontainers.
- Config: `application.properties` with non-secret defaults, secrets as `${ENV}` without fallbacks, typed
  `@ConfigurationProperties` for app settings (CORS, upload limits, scheduling), test config in
  `src/test/resources`.
- Ops: Actuator health per storage provider and for the replication backlog; README; CI running the suites.

## Related Documents

- [[Docs/wpmanager/Explanations/02-Build-Tooling-and-Dependencies]]
- [[Docs/wpmanager/Explanations/12-Configuration-and-Secrets]]
- [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review]]
