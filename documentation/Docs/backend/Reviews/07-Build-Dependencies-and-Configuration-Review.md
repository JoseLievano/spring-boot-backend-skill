# Build, Dependencies and Configuration Review — backend

#doc #review #ref-backend #build #configuration

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/02-Build-Tooling-and-Dependencies]], [[Docs/backend/Explanations/09-Configuration-and-Secrets]]

## Scope

Reviewed: `backend/pom.xml`, `Dockerfile`, `test.sh`, `.nvm` (variable names only), both properties
files, and the `@Value` injection points. Checked version management against the resolved
`spring-boot-dependencies-3.4.1.pom`, and logging/property names against the resolved Hibernate and
Boot jars in `~/.m2`. Excluded: CVE scanning.

## Verdict

The build mechanics are sound: Boot parent for version management, QueryDSL APT wired correctly into the
compiler, and secrets as required environment placeholders. The dependency list and configuration,
however, were carried over from wpmanager without pruning. Seven starters and the S3 SDK are unused, and
two of them change runtime behavior (Spring Data REST exports repositories, Batch touches the database
at start-up). Configuration mixes environments (test profile in `src/main`, SQL logging in the only
runtime file, a hardcoded host), and names in the env file and comments no longer match what the code
reads.

## Strengths to Keep

- Versions are managed by the Boot parent; only non-managed libraries are pinned
  (`backend/pom.xml:5-10`, `backend/pom.xml:120-136`).
- QueryDSL version in one property, reused for runtime and processor
  (`backend/pom.xml:33`, `backend/pom.xml:64`, `backend/pom.xml:196`).
- Lombok and QueryDSL APT declared together in `annotationProcessorPaths`
  (`backend/pom.xml:187-199`); Lombok excluded from the fat jar (`backend/pom.xml:202-213`).
- Required secrets and credentials have no defaults, so a missing variable fails start-up
  (`backend/src/main/resources/application.properties:5-7`, `backend/src/main/resources/application.properties:26`,
  `backend/src/main/resources/application.properties:30`).
- `open-in-view=false` and explicit Hikari sizing (`backend/src/main/resources/application.properties:19-22`).
- Surefire `-XX:+EnableDynamicAgentLoading` for Mockito on JDK 21 (`backend/pom.xml:178`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-01\|BE-R07-01]] | Unused starters and SDKs, some with runtime side effects | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-02\|BE-R07-02]] | `@EnableScheduling` without any scheduled work | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-03\|BE-R07-03]] | H2 packaged at runtime scope | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-04\|BE-R07-04]] | Surefire pinned below Boot's version; redundant Mockito pin | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-05\|BE-R07-05]] | SQL logging on in the runtime config; bind-logging line is inert | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-06\|BE-R07-06]] | Test profile (with fixed secrets) ships in the application jar | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-07\|BE-R07-07]] | Database host hardcoded to `db` | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-08\|BE-R07-08]] | Properties for features that do not exist | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-09\|BE-R07-09]] | Development-only Dockerfile referencing a missing compose file | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-10\|BE-R07-10]] | Stale project identity in the POM | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-11\|BE-R07-11]] | Env file and comments name variables the code does not read | 🟡 Medium | configuration | Confirmed (read in code) |

## Findings

### BE-R07-01
**Title:** Unused starters and SDKs, some with runtime side effects
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** YAGNI; every auto-configured starter is behavior you did not ask for.
**Evidence:** No class under `backend/src` references Spring Batch, Spring Data JDBC, Spring Data REST, `JdbcTemplate`, SOAP web services, WebFlux/`WebClient`, WebSocket or the AWS SDK (grep over `backend/src`), yet all are declared (`backend/pom.xml:49-56`, `backend/pom.xml:66-73`, `backend/pom.xml:86-97`, `backend/pom.xml:158-163`), plus the matching test libraries `reactor-test` and `spring-batch-test` (`backend/pom.xml:143-152`).
**Impact:** Larger jar and start-up; wider attack surface. Two are not inert: Spring Data REST may export the repositories ([[Docs/backend/Reviews/01-Security-Review#BE-R01-03|BE-R01-03]]), and Batch's `batchDataSourceInitializer` needs a database connection during context start-up, which is what fails `contextLoads` ([[Docs/backend/Reviews/08-Testing-Review#BE-R08-03|BE-R08-03]]). Having both WebMVC and WebFlux on the classpath also invites accidental mixing.
**Recommendation:** Remove `batch`, `data-jdbc`, `data-rest`, `jdbc`, `web-services`, `webflux`, `websocket`, the S3 SDK, `reactor-test` and `spring-batch-test`. Re-add a starter only with the feature that needs it.
**Verified against:** `spring-boot-autoconfigure-3.4.1.jar` (contains `batch/BatchAutoConfiguration`, `data/rest/RepositoryRestMvcAutoConfiguration`); failure chain in `backend/target/surefire-reports/com.agentForgeBackend.authServerApplicationTests.txt:45`
**Confidence:** Confirmed (read in code)

### BE-R07-02
**Title:** `@EnableScheduling` without any scheduled work
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** YAGNI.
**Evidence:** `@EnableScheduling` on the application class (`backend/src/main/java/com/agentForgeBackend/agentForgeBackendApplication.java:9`); no `@Scheduled` method exists in `backend/src`. The test profile tries to disable a scheduler with a property Boot does not define (`backend/src/main/resources/application-test.properties:27-28`).
**Impact:** Creates a scheduler infrastructure for nothing; the test property suggests a behavior (disabling jobs) that it does not have.
**Recommendation:** Remove `@EnableScheduling` and the property; when jobs are added, gate them with `@ConditionalOnProperty` on a project-owned key.
**Verified against:** `spring-boot-autoconfigure-3.4.1.jar` configuration metadata (defines `spring.task.scheduling.pool.size`, `…shutdown.*`, `…thread-name-prefix`, `…simple.*`; no `spring.task.scheduling.enabled`)
**Confidence:** Confirmed (read in code)

### BE-R07-03
**Title:** H2 packaged at runtime scope
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Test-only code stays out of the production artifact.
**Evidence:** `<scope>runtime</scope>` for `com.h2database:h2` (`backend/pom.xml:105-109`).
**Impact:** The production jar contains an embedded database engine (and, with [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-06|BE-R07-06]], a profile that activates it).
**Recommendation:** `<scope>test</scope>`, or drop H2 entirely in favor of Testcontainers ([[Docs/backend/Reviews/08-Testing-Review#BE-R08-05|BE-R08-05]]).
**Verified against:** N/A — Maven scope semantics
**Confidence:** Confirmed (read in code)

### BE-R07-04
**Title:** Surefire pinned below Boot's version; redundant Mockito pin
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Let the platform BOM manage versions; pin only with a reason.
**Evidence:** `maven.surefire.version` 3.2.5 is used for the plugin (`backend/pom.xml:32`, `backend/pom.xml:176`) while Boot 3.4.1 manages 3.5.2 (`spring-boot-dependencies-3.4.1.pom`, property `maven-surefire-plugin.version`). `mockito.version` 5.14.2 is set explicitly and repeated on both Mockito dependencies (`backend/pom.xml:31`, `backend/pom.xml:36-48`) — the same value Boot manages, and `mockito-core` already comes with `spring-boot-starter-test`.
**Impact:** Surefire is silently downgraded; the Mockito pin will hold Mockito back at the next Boot upgrade. This corrects the planning lead, which assumed the Mockito pin *overrode* Boot's version: today it is equal, only redundant.
**Recommendation:** Remove both properties and the explicit Mockito dependencies; rely on the parent.
**Verified against:** `~/.m2/.../spring-boot-dependencies/3.4.1/spring-boot-dependencies-3.4.1.pom` (`mockito.version` 5.14.2, `maven-surefire-plugin.version` 3.5.2)
**Confidence:** Confirmed (read in code)

### BE-R07-05
**Title:** SQL logging on in the runtime config; bind-logging line is inert
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Production defaults should be quiet and safe; environment-specific verbosity belongs in a profile.
**Evidence:** `spring.jpa.show-sql=true` and `logging.level.org.hibernate.SQL=DEBUG` (`backend/src/main/resources/application.properties:18`, `backend/src/main/resources/application.properties:35`) — the same SQL is printed twice (stdout and logger). `logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE` (`backend/src/main/resources/application.properties:36`) targets a Hibernate 5 category; in the resolved Hibernate 6.6.4 the class is gone and bind logging uses `org.hibernate.orm.jdbc.bind`.
**Impact:** Every statement is logged in production, costing I/O and flooding logs. If someone "fixes" the category name, bind values (e-mails, password hashes) will be logged. This corrects the planning lead that bind parameters are currently logged: they are not.
**Recommendation:** Remove all three lines from `application.properties`; put SQL logging in an opt-in `dev` profile, and never enable bind-parameter logging outside local development.
**Verified against:** `hibernate-core-6.6.4.Final.jar` (javap: `JdbcBindingLogging.NAME = "org.hibernate.orm.jdbc.bind"`; no `org/hibernate/type/descriptor/sql/BasicBinder` class in the jar); Hibernate 6.6.4 is the version managed by Boot 3.4.1
**Confidence:** Confirmed (read in code)

### BE-R07-06
**Title:** Test profile (with fixed secrets) ships in the application jar
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Separate test and production configuration by source set.
**Evidence:** `application-test.properties` lives in `src/main/resources` (`backend/src/main/resources/application-test.properties:1-28`) and contains literal JWT and HMAC secrets (`<redacted>`, lines 22 and 25).
**Impact:** Starting the production jar with `--spring.profiles.active=test` (a plausible mistake) runs on an in-memory database with **publicly known** signing keys, so anyone can forge valid JWTs.
**Recommendation:** Move the file to `src/test/resources` (Boot picks it up from the test classpath), or rename the profile to something that cannot be confused with an environment name.
**Verified against:** N/A — Maven source-set semantics
**Confidence:** Confirmed (read in code)

### BE-R07-07
**Title:** Database host hardcoded to `db`
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Twelve-factor config.
**Evidence:** `spring.datasource.url=jdbc:postgresql://db:5432/${POSTGRES_DB}` (`backend/src/main/resources/application.properties:5`).
**Impact:** Only works where a host named `db` resolves (a compose network). Running locally or in any other environment requires editing the file; tests without the `test` profile fail with `UnknownHostException: db` (`backend/target/surefire-reports/com.agentForgeBackend.authServerApplicationTests.txt:102`).
**Recommendation:** `jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB}` or simply `spring.datasource.url=${DATABASE_URL}`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R07-08
**Title:** Properties for features that do not exist
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Configuration should describe the application you have.
**Evidence:** Multipart limits of 100 MB (`backend/src/main/resources/application.properties:11-13`) with no upload endpoint; `UploadValidator` reads `${upload.max-file-size:104857600}`, a key defined nowhere, so its default always applies (`backend/src/main/java/com/agentForgeBackend/shared/tools/UploadValidator.java:11-12`). This corrects the planning lead: the missing key does **not** break start-up, because the placeholder has a default.
**Impact:** Misleading configuration; a 100 MB request body limit on an API that accepts only small JSON bodies.
**Recommendation:** Remove the multipart settings and the dead upload utilities ([[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-01|BE-R09-01]]); keep Boot's defaults until uploads exist.
**Verified against:** N/A — Spring `${key:default}` placeholder semantics
**Confidence:** Confirmed (read in code)

### BE-R07-09
**Title:** Development-only Dockerfile referencing a missing compose file
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** —
**Evidence:** The image runs `mvn spring-boot:run` from a Maven base image and expects sources mounted by a `docker-compose.yml` (`backend/Dockerfile:2`, `backend/Dockerfile:14-15`, `backend/Dockerfile:22`); no compose file exists in `backend/`.
**Impact:** There is no production image definition; the dev setup cannot be reproduced from the repository alone. (Deployment is out of scope for the skill per the project brief, so this is recorded for completeness.)
**Recommendation:** Commit the compose file used for development, and if an image is needed, build a layered jar (`spring-boot:build-image` or a multi-stage Dockerfile on a JRE base).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R07-10
**Title:** Stale project identity in the POM
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** —
**Evidence:** `groupId` `com`, `<name>authServer</name>`, description "Auth microservice" (`backend/pom.xml:11-15`); an unmodified Initializr `HELP.md` (`backend/HELP.md:1-5`).
**Impact:** Confusing artifact metadata; `com` as a group id collides with anything else published the same way.
**Recommendation:** Use a reverse-domain `groupId`, a matching `name`/`description`, and replace `HELP.md` with a real README.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R07-11
**Title:** Env file and comments name variables the code does not read
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** One name per setting, end to end.
**Evidence:** `backend/.nvm` defines `FILE_SIGNATURE_SECRET` and `JWT_SECRET_KEY` (`backend/.nvm:1-2`, values `<redacted>`), while the properties read only `JWT_SECRET` (`backend/src/main/resources/application.properties:26`, `backend/src/main/resources/application.properties:30`); the comment above `file.signature.secret` still says `FILE_SIGNATURE_SECRET` (`backend/src/main/resources/application.properties:25`).
**Impact:** Sourcing the env file does not configure the application, which then fails to start with an unresolved `${JWT_SECRET}`; operators following the comment set a variable that is ignored.
**Recommendation:** Bind secrets through one `@ConfigurationProperties` record (e.g. `app.security.jwt-key`, `app.security.client-key-pepper`) with `@Validated` constraints, document the exact variable names once (README or `.env.example`), and keep the example file free of real values.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- POM: Boot parent, only the starters the code uses, H2 (if any) at test scope, no redundant version
  properties, QueryDSL + Lombok APT as today.
- `application.properties`: environment-neutral defaults, every external value an `${ENV}` placeholder,
  no SQL logging.
- `application-dev.properties` (opt-in) for SQL logging and local hosts; `src/test/resources` for test
  configuration.
- Secrets bound through validated `@ConfigurationProperties`, one purpose per key, documented once in an
  `.env.example` that contains no real values.

## Related Documents

- [[Docs/backend/Explanations/02-Build-Tooling-and-Dependencies]]
- [[Docs/backend/Explanations/09-Configuration-and-Secrets]]
- [[Docs/backend/Reviews/01-Security-Review]]
- [[Docs/backend/Reviews/08-Testing-Review]]
