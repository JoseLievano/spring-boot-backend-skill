# Code Hygiene Review — BugTracker

#doc #review #ref-bugtracker #architecture

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/03-Package-Structure-and-Module-Anatomy]], [[Docs/BugTracker/Explanations/05-DTO-Tiers-and-Mapping]]

## Scope

Reviewed across all 341 main source files: logging, dead code, naming, stereotype annotations, injection
style, Lombok usage and imports. Counts come from searches over `BugTracker/src/main/java`.

## Verdict

Hygiene is uneven in the way a fast-moving solo project usually is: console printing instead of logging,
dead classes and endpoints, lower-case class names from the `bs` prefix, repositories annotated as services,
and `@Lazy` used as a general remedy for dependency cycles. None of it breaks behavior on its own; the `@Lazy`
pattern is the one with architectural weight, because it hides a mapper dependency graph that is cyclic by
design. All findings apply equally on Boot 2.7; none is an era gap.

## Strengths to Keep

- Uniform file and suffix naming across 31 modules makes the code base navigable by convention
  ([[Docs/BugTracker/Explanations/03-Package-Structure-and-Module-Anatomy]]).
- Constructor injection with `final` fields is the dominant style in services and controllers
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:27-63`).
- Lombok builders keep mapper code readable (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskMapper.java:59-75`).
- Service code is commented step by step, which makes intent recoverable
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:72-110`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-01\|BT-R10-01]] | `System.out`/`printStackTrace` instead of a logger | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-02\|BT-R10-02]] | Dead code, test endpoints and orphans | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-03\|BT-R10-03]] | Lower-case class names | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-04\|BT-R10-04]] | Repositories annotated `@Service` | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-05\|BT-R10-05]] | `@Lazy` in 28 files hides cyclic mapper dependencies; mixed injection styles | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-06\|BT-R10-06]] | Redundant annotations, duplicate calls, unused imports | 🟢 Low | hygiene | Confirmed (read in code) |

## Findings

### BT-R10-01
**Title:** `System.out`/`printStackTrace` instead of a logger
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Structured logging with levels.
**Evidence:** 16 occurrences in 7 files, e.g. the filter engine prints every boolean value and swallows date
errors with `printStackTrace` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:220`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:237`); comment
insert prints author and mentioned usernames (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrComment/bsPrCommentServiceImplements.java:99-101`);
the seeder prints each created object (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/tools/firstInstallCheck.java:101-114`).
Other files: `AdminServiceImplements`, `ClientServiceImplements`, `EmployeeServiceImplements`, `bsStatusServiceImplements`.
**Impact:** No levels, no correlation, personal data (usernames) on stdout, and output that production log
pipelines cannot filter.
**Recommendation:** SLF4J loggers (`@Slf4j`), `debug` for diagnostics, no personal data at `info`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R10-02
**Title:** Dead code, test endpoints and orphans
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Deletion test — code that nothing calls only costs.
**Evidence:** `EntityFactory` (205 lines, 26 injected repositories, no caller)
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/EntityFactory.java:35-205`); the unused `Role` enum
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/role/Role.java`); `bsFile` entity and DTOs without a
module (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsFile/bsFileEntity.java`); the empty
`geo/state` directory; `GET /test/a`, `POST /test/b` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/Test/testController.java:13-21`)
and `GET /login/test` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/LoginController.java:37-40`);
`CommonPathExpression.isANumber` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:249-257`);
`updateListView` declared on the service but not exposed (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultService.java:29`).
**Impact:** Start-up cost (`EntityFactory` is a bean), extra tables (`bs_file`), extra endpoints in production,
and readers who assume the code is used.
**Recommendation:** Delete them; re-add when a feature needs one.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R10-03
**Title:** Lower-case class names
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Follow Java naming conventions.
**Evidence:** 233 of the 341 top-level types start with a lower-case letter because of the `bs` prefix, plus
`firstInstallCheck`, `testController` and the `country*` classes
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:27`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/tools/firstInstallCheck.java:30`).
**Impact:** Class names read like variables (`bsPrTaskEntity bsPrTaskEntity`), which the predicates show
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskPredicate.java:15`); static
analysis flags every class.
**Recommendation:** PascalCase with the prefix as a word (`BsPrTaskEntity`) or, better, packages instead of
prefixes (`tenant.project.task.TaskEntity`).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-03|BE-R09-03]]

### BT-R10-04
**Title:** Repositories annotated `@Service`
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Stereotypes should say what a class is.
**Evidence:** 28 of the 31 concrete repository interfaces carry `@Service`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskRepository.java:12-13`);
`AdminRepository`, `bsPrDocsRepository` and `countryRepository` do not.
**Impact:** No runtime effect (Spring Data ignores it on interfaces), but it misleads readers and disables
the persistence-exception translation that `@Repository` implies for classes.
**Recommendation:** Remove the annotation from repository interfaces.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R10-05
**Title:** `@Lazy` in 28 files hides cyclic mapper dependencies; mixed injection styles
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** Acyclic dependencies principle — a cycle is a design signal, not something to suppress.
**Evidence:** 27 mappers and `countryServiceImplements` use `@Lazy`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskMapper.java:32-34`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/geo/country/countryServiceImplements.java:17-18`), the
latter without any cycle. `BusinessMapper` depends on 18 mappers, several of which depend back on it
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessMapper.java:67-88`). Four HQ mappers use
`@Lazy @Autowired` field injection (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/mainHQ/MainHQMapper.java:17-19`),
as do `UserServiceImplements` (which also takes the same beans by constructor) and
`SecurityUserServiceImplements` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/UserServiceImplements.java:16-27`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/securityUser/SecurityUserServiceImplements.java:17-18`).
**Impact:** Every mapper call goes through a lazy proxy; wiring errors surface on first use instead of at
start-up; and the real dependency structure (every mapper can reach every other) is invisible. New modules
copy `@Lazy` by habit, as `countryServiceImplements` shows.
**Recommendation:** Break the cycle structurally: MiniDTO mapping as a separate, dependency-free component per
entity (or a generated mapper), used by full mappers; then remove `@Lazy` and field injection.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R10-06
**Title:** Redundant annotations, duplicate calls, unused imports
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** —
**Evidence:** `@Data` together with `@Getter @Setter` on DTOs and request classes
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskDTO.java:15-20`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/FilterRequest.java:10-13`); `.taxID(...)`
called twice in a builder (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessMapper.java:118-119`);
the same repository bean injected twice under two names
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:44-47`);
unused imports such as `org.apache.tomcat.jni.Time` (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenGeneratorFilter.java:8`),
`java.sql.Time` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/controller/DefaultController.java:12`) and
`org.springframework.data.repository.cdi.Eager` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/client/ClientEntity.java:12`).
**Impact:** Noise; the Tomcat import couples a filter to a container-internal class.
**Recommendation:** Enable an import/unused-code check in the build (Checkstyle or the IDE's inspection
profile in CI).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-04|BE-R09-04]]

## Recommended Target Pattern

- SLF4J logging only; no console output in main code.
- Java naming conventions; feature packages instead of name prefixes.
- `@Repository`-free Spring Data interfaces; `@Service` only on services.
- Constructor injection everywhere; no `@Lazy`; an acyclic mapper design.
- A static-analysis gate in the build for unused code and imports.

## Related Documents

- [[Docs/BugTracker/Explanations/03-Package-Structure-and-Module-Anatomy]]
- [[Docs/BugTracker/Explanations/05-DTO-Tiers-and-Mapping]]
- [[Docs/backend/Reviews/09-Code-Hygiene-Review]]
