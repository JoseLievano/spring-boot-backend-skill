# Build, Dependencies and Configuration Review — BugTracker

#doc #review #ref-bugtracker #build #configuration

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/02-Build-Tooling-and-Dependencies]], [[Docs/BugTracker/Explanations/13-Configuration-and-Bootstrap]]

## Scope

Reviewed: `BugTracker/pom.xml`, the Maven wrapper, `application.properties`, configuration literals in code,
and the repository root for build and run documentation. Secrets are reviewed in
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-03|BT-R01-03]] and not repeated here.

## Verdict

The build is small and works, but it is pinned to an out-of-support framework line at its first patch
release, declares a Java level it does not compile with, and carries six unused starters from Initializr.
Configuration is one file with no profiles, and several environment-specific values are Java literals.
The Boot 2.7 support status and the `javax` namespace are **era gaps**, one of which carries an EOL risk;
the Java-level mismatch and the dependency hygiene are **defects**.

## Strengths to Keep

- The Boot parent BOM manages almost every version; only JJWT (not in the BOM) and one starter are pinned
  (`BugTracker/pom.xml:5-10`, `BugTracker/pom.xml:108-126`).
- Maven wrapper for reproducible builds (`BugTracker/.mvn/wrapper/maven-wrapper.properties:1`).
- Lombok excluded from the fat jar (`BugTracker/pom.xml:171-182`).
- `open-in-view=false` set explicitly (`BugTracker/src/main/resources/application.properties:6`).
- QueryDSL metamodel generated at build time rather than hand-written
  (`BugTracker/pom.xml:154-169`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-01\|BT-R08-01]] | Spring Boot 2.7 is out of OSS support, and this is 2.7.0 | 🟠 High | configuration | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-02\|BT-R08-02]] | `javax` namespace ties the code to Boot 2 | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-03\|BT-R08-03]] | `java.version` 11 but compiler 17 | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-04\|BT-R08-04]] | Six unused starters | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-05\|BT-R08-05]] | Batch starter version overrides the BOM | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-06\|BT-R08-06]] | H2 on the runtime classpath, unused | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-07\|BT-R08-07]] | No profiles; environment values hardcoded | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-08\|BT-R08-08]] | No README or run instructions | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-09\|BT-R08-09]] | Unmaintained `apt-maven-plugin` for QueryDSL | 🟢 Low | configuration | Confirmed (read in code) |

## Findings

### BT-R08-01
**Title:** Spring Boot 2.7 is out of OSS support, and this is 2.7.0
**Severity:** 🟠 High
**Category:** configuration
**Principle:** — (era gap with EOL risk)
**Evidence:** Parent `spring-boot-starter-parent` 2.7.0 (`BugTracker/pom.xml:5-10`). The spring.io project
API (`api.spring.io/projects/spring-boot/generations/2.7.x`) lists the 2.7.x line with OSS support end
**2023-06-30** and commercial support end 2029-06-30; the last OSS patch was 2.7.18 (endoflife.date).
**Impact:** No free security fixes for Spring, Spring Security, Tomcat or Hibernate through the Boot BOM
since mid-2023, and the project is 18 patch releases behind even the last free 2.7 release. Transitive
dependencies with published CVEs stay on the classpath.
**Recommendation:** Upgrade to 2.7.18 as a stop-gap only, then migrate to a supported Boot 3.x line (Java 17+,
Jakarta EE 9+, Security 6).
**Verified against:** spring.io support API (`api.spring.io/projects/spring-boot/generations/2.7.x`), fetched 2026-09-29
**Confidence:** Confirmed (read in code)
**Version note:** The migration is the cost described in [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-02|BT-R08-02]] plus the Security 6 changes in [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-14|BT-R01-14]].

### BT-R08-02
**Title:** `javax` namespace ties the code to Boot 2
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** — (era gap)
**Evidence:** `javax.persistence.*` in all 32 entities (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:7`),
`javax.validation` in Forms and controllers (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/controller/DefaultController.java:11`),
`javax.servlet` in the filters (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenValidatorFilter.java:18-21`).
**Impact:** Correct for 2022. Moving to Boot 3 requires rewriting every import (mechanical, OpenRewrite can
do it), upgrading QueryDSL to the `jakarta` classifier, JJWT to a Jakarta-compatible line, and replacing the
Security 5 DSL.
**Recommendation:** New projects start on `jakarta.*`. For this code, run the OpenRewrite
`UpgradeSpringBoot_3_x` recipe and fix the remaining Security configuration by hand.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Version note:** Boot 3 requires Java 17 and Jakarta EE 9+ namespaces.

### BT-R08-03
**Title:** `java.version` 11 but compiler 17
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** One source of truth for the language level.
**Evidence:** `<java.version>11</java.version>` (`BugTracker/pom.xml:16-18`) versus compiler `source`/`target` 17
(`BugTracker/pom.xml:183-190`); the code uses `Stream.toList()` (Java 16+)
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:85`). The last recorded
test run used Java 16.0.2 (`BugTracker/target/surefire-reports/TEST-com.bgsystem.bugtracker.BugTrackerApplicationTests.xml:49`),
the last compiled classes are Java 17 bytecode.
**Impact:** Tools that read `java.version` (IDE import, CI images, Boot's own defaults) pick 11 and fail; a
JDK 16 build fails with "invalid target release: 17". The recorded test run predates the current setting.
**Recommendation:** Set `java.version` to 17 and delete the compiler-plugin override; use `release`, not
`source`/`target`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R08-04
**Title:** Six unused starters
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Minimal dependency surface.
**Evidence:** `spring-boot-starter-batch`, `-quartz`, `-mail`, `-web-services`, `-data-jdbc` and `-jdbc`
(`BugTracker/pom.xml:20-48`, `BugTracker/pom.xml:61-64`) have no imports in `BugTracker/src`; `spring-boot-starter-data-rest`
(`BugTracker/pom.xml:33-36`) has none either but activates endpoints
([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-07|BT-R01-07]]).
**Impact:** Larger jar and attack surface, slower start-up, auto-configurations (Batch, Quartz scheduler) that
run for nothing, and a misleading picture of what the application does.
**Recommendation:** Remove them; add back only when a feature needs one.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-01|BE-R07-01]]

### BT-R08-05
**Title:** Batch starter version overrides the BOM
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Let the BOM align versions.
**Evidence:** `spring-boot-starter-batch` pinned to `2.7.5` under a 2.7.0 parent (`BugTracker/pom.xml:20-24`).
**Impact:** A starter from a different Boot patch pulls different transitive versions; unused anyway.
**Recommendation:** Remove the version (or the dependency, see BT-R08-04).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R08-06
**Title:** H2 on the runtime classpath, unused
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Test-only dependencies in test scope.
**Evidence:** `h2` with `runtime` scope (`BugTracker/pom.xml:102-106`); no H2 URL anywhere; the only test uses
the MySQL datasource (`BugTracker/src/main/resources/application.properties:2`).
**Impact:** An embedded database in the production artifact; if the datasource URL were ever missing, Boot
could auto-configure an in-memory database instead of failing.
**Recommendation:** Remove it, or move it to `test` scope with a test profile.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-03|BE-R07-03]]

### BT-R08-07
**Title:** No profiles; environment values hardcoded
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Externalized configuration (twelve-factor).
**Evidence:** One `application.properties` with literal values (`BugTracker/src/main/resources/application.properties:1-6`);
CORS origin `http://localhost:4200` (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/security/SecurityConfig.java:49`);
token lifetime (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenGeneratorFilter.java:58`);
default tenant settings text (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessServiceImplements.java:68-73`).
**Impact:** Deploying anywhere but a developer machine requires code changes: the production front end is
rejected by CORS, and tests run against the same database as development.
**Recommendation:** `application.yml` with `${ENV}` placeholders, `dev`/`test`/`prod` profiles, and a
`@ConfigurationProperties` class for security settings (origins, token lifetime, key).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-10|BE-R01-10]]

### BT-R08-08
**Title:** No README or run instructions
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** —
**Evidence:** The project root contains only the Initializr `HELP.md`, the POM, the wrapper and an IntelliJ
`.iml` (`BugTracker/HELP.md`, `BugTracker/BugTracker.iml`); no Dockerfile or compose file.
**Impact:** Setting up the database, creating the schema and knowing the seed accounts requires reading code.
**Recommendation:** A README with prerequisites, a compose file for MySQL, required environment variables,
and how the first admin is created.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R08-09
**Title:** Unmaintained `apt-maven-plugin` for QueryDSL
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** — (era gap)
**Evidence:** `com.mysema.maven:apt-maven-plugin:1.1.3` with `JPAAnnotationProcessor` (`BugTracker/pom.xml:154-169`).
**Impact:** The plugin has had no release in years and duplicates what the compiler's annotation processing
does; IDEs need manual source-root configuration.
**Recommendation:** Declare `querydsl-apt` (classifier `jpa`, or `jakarta` on Boot 3) as an annotation
processor path of `maven-compiler-plugin`, as `backend/` does.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- Current Boot 3.x parent, `java.version` = the compiler release, no overrides except non-BOM libraries.
- Only the starters the code uses; database drivers for tests in `test` scope or Testcontainers.
- QueryDSL through `annotationProcessorPaths`.
- `application.yml` + profiles + `${ENV}` placeholders + `@ConfigurationProperties` for app settings.
- README and a compose file for local dependencies.

## Related Documents

- [[Docs/BugTracker/Explanations/02-Build-Tooling-and-Dependencies]]
- [[Docs/BugTracker/Explanations/13-Configuration-and-Bootstrap]]
- [[Docs/BugTracker/Reviews/09-Testing-Review]]
- [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review]]
