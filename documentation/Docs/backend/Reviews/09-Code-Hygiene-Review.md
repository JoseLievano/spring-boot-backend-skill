# Code Hygiene Review — backend

#doc #review #ref-backend #architecture

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/03-Package-Structure-and-Module-Anatomy]]

## Scope

Reviewed: every main source file for unused code, naming, Lombok usage, logging style, annotations
without effect, and dependency direction between packages. Usage was established by `grep` over
`backend/src` (main and test). Excluded: formatting and import order.

## Verdict

The code is readable and the newer subsystem (`shared/query`) is clean. The rest carries visible
fork residue from wpmanager: about 650 lines of utilities with no caller, a misspelled package, class
names that break Java conventions, redundant annotations, and one `shared/` class that depends on
feature packages. None of it is dangerous on its own, but it inflates what a reader must understand and
what the skill might copy by mistake.

## Strengths to Keep

- Constructor injection with `final` fields everywhere (no field `@Autowired` in main code)
  (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:29-46`).
- Consistent `<Feature><Role>` class naming inside feature packages
  (`backend/src/main/java/com/agentForgeBackend/models/hq/admin`).
- Parameterized logging where the newer code logs
  (`backend/src/main/java/com/agentForgeBackend/shared/tools/FileSigner.java:52`).
- `shared/query` has Javadoc-free but self-describing names, immutable value objects and no dead code
  (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:28-107`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-01\|BE-R09-01]] | ~650 lines of unused code carried over from wpmanager | 🟡 Medium | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-02\|BE-R09-02]] | Misspelled `boostrap` package and class | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-03\|BE-R09-03]] | Lower-case class names and a camel-case root package | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-04\|BE-R09-04]] | Redundant Lombok annotations | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-05\|BE-R09-05]] | Annotations without effect | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-06\|BE-R09-06]] | String-concatenated logging and PII at `ERROR` level | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-07\|BE-R09-07]] | `shared/tools/AuthUserUtil` depends on feature packages | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-08\|BE-R09-08]] | Hand-made `ObjectMapper` instances bypass Boot's configuration | 🟢 Low | hygiene | Confirmed (read in code) |

## Findings

### BE-R09-01
**Title:** ~650 lines of unused code carried over from wpmanager
**Severity:** 🟡 Medium
**Category:** hygiene
**Principle:** Deletion test — deleting these classes removes no behavior; they are pure cost.
**Evidence:** No caller in `backend/src` for:

| Unused element | Lines | Path |
|---|---|---|
| `AuthUserUtil` | 81 | `backend/src/main/java/com/agentForgeBackend/shared/tools/AuthUserUtil.java` |
| `URLValidator` (makes outbound HTTP calls) | 146 | `backend/src/main/java/com/agentForgeBackend/shared/tools/URLValidator.java` |
| `TextFieldValidator` + `ValidationResult` (keyword-blacklist "SQL injection" check) | 162 | `backend/src/main/java/com/agentForgeBackend/shared/tools/TextFieldValidator.java` |
| `DiskBasedMultipartFile` | 82 | `backend/src/main/java/com/agentForgeBackend/shared/tools/DiskBasedMultipartFile.java` |
| `ChecksumUtils` | 34 | `backend/src/main/java/com/agentForgeBackend/shared/tools/ChecksumUtils.java` |
| `UploadValidator` | 33 | `backend/src/main/java/com/agentForgeBackend/shared/tools/UploadValidator.java` |
| `BaseUserMapper`, `BaseUserDTO`, `BaseUserMiniDTO` | 63 | `backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserMapper.java` |
| `ThrowingSupplier`, `DownloadableParent` | 24 | `backend/src/main/java/com/agentForgeBackend/shared/functional/ThrowingSupplier.java` |
| `SecurityUserServiceImpl.getBaseUser` | 7 | `backend/src/main/java/com/agentForgeBackend/shared/securityUser/SecurityUserServiceImpl.java:27-33` |
| `UserRoles.EMPLOYEE` | 1 | `backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/UserRoles.java:6` |
| `GET /test` endpoint | 4 | `backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityController.java:68-71` |
| `ClientEntity.getBaseUser` (also broken, [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-01\|BE-R04-01]]) | 3 | `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientEntity.java:33-35` |

The same `shared/tools` set exists in wpmanager, where the upload feature uses it (`wpmanager/src/main/java/com/wpmanager/shared/tools`).
**Impact:** Readers (and the future skill) cannot tell framework from residue; several classes are `@Component`s, so they are instantiated at start-up; `TextFieldValidator` models a discredited defense (keyword blacklists instead of parameterized queries) that someone may later wire in; `GET /test` is a public endpoint with no purpose.
**Recommendation:** Delete all of the above. If ownership checks are needed later, re-introduce `AuthUserUtil` in a non-`shared` package (see [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-07|BE-R09-07]]).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R09-02
**Title:** Misspelled `boostrap` package and class
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** —
**Evidence:** `configuration.boostrap.AdminBoostrap` (`backend/src/main/java/com/agentForgeBackend/configuration/boostrap/AdminBoostrap.java:1`, `backend/src/main/java/com/agentForgeBackend/configuration/boostrap/AdminBoostrap.java:16`).
**Impact:** Searches for "bootstrap" miss it; the typo propagates when copied.
**Recommendation:** Rename to `configuration.bootstrap.AdminBootstrap`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R09-03
**Title:** Lower-case class names and a camel-case root package
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Java naming conventions (types `UpperCamelCase`, packages all lower case).
**Evidence:** `public class agentForgeBackendApplication` (`backend/src/main/java/com/agentForgeBackend/agentForgeBackendApplication.java:11`), `class authServerApplicationTests` (`backend/src/test/java/com/agentForgeBackend/authServerApplicationTests.java:7`), package `com.agentForgeBackend` (`backend/src/main/java/com/agentForgeBackend/agentForgeBackendApplication.java:1`). The test class still carries the old `authServer` name.
**Impact:** Tooling and readers expect conventional names; mixed-case packages can clash on case-insensitive file systems.
**Recommendation:** `com.<org>.agentforge` as the root package, `AgentForgeBackendApplication`, `AgentForgeBackendApplicationTests`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R09-04
**Title:** Redundant Lombok annotations
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Say it once.
**Evidence:** Nine DTO/form classes combine `@Data` with `@Getter` and `@Setter` (which `@Data` already includes), e.g. `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientDTO.java:7-12` and `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminListDTO.java:13-18`; `SecurityConfig` is annotated `@EnableWebSecurity` although the application class already is (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:30`, `backend/src/main/java/com/agentForgeBackend/agentForgeBackendApplication.java:10`).
**Impact:** Noise; suggests the author was unsure what each annotation does.
**Recommendation:** Use Java `record`s for immutable DTOs (Jackson and Bean Validation support them), or `@Data @Builder @NoArgsConstructor @AllArgsConstructor` without the extras; keep `@EnableWebSecurity` only on `SecurityConfig`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R09-05
**Title:** Annotations without effect
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Every annotation should change behavior.
**Evidence:** `@Lazy` on the no-arg constructor of `ClientMapper` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientMapper.java:13-15`) — `@Lazy` on a constructor has no meaning for a bean without dependencies; `@Validated` on the `AdminForm` DTO (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminForm.java:12`) — it only matters on Spring beans; `@Repository` on Spring Data interfaces (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminRepository.java:6`) — harmless but unnecessary.
**Impact:** Misleads readers into thinking lazy initialization or class-level validation is happening.
**Recommendation:** Remove them.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R09-06
**Title:** String-concatenated logging and PII at `ERROR` level
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Parameterized logging; log levels reflect severity for the operator, not for the client.
**Evidence:** `logger.info("Admin count: " + adminCount)` (`backend/src/main/java/com/agentForgeBackend/configuration/boostrap/AdminBoostrap.java:36`); `logger.error("Client not found id : " + id)` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:116`); e-mail addresses and usernames logged at `ERROR` for ordinary validation failures (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:83-90`, `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:126-127`).
**Impact:** Expected client mistakes page on-call engineers as errors; personal data lands in logs.
**Recommendation:** Use `{}` placeholders, log expected 4xx conditions at `DEBUG`/`INFO` without PII, and reserve `ERROR` for server faults.
**Verified against:** N/A — no library API involved (SLF4J placeholders already used elsewhere in the project)
**Confidence:** Confirmed (read in code)

### BE-R09-07
**Title:** `shared/tools/AuthUserUtil` depends on feature packages
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** Dependency direction — shared/core code must not depend on features (acyclic dependencies; DIP).
**Evidence:** `AuthUserUtil` imports `AdminEntity`, `AdminRepository`, `ClientEntity`, `ClientRepository` (`backend/src/main/java/com/agentForgeBackend/shared/tools/AuthUserUtil.java:3-6`) and hardcodes one method per user type (`backend/src/main/java/com/agentForgeBackend/shared/tools/AuthUserUtil.java:30-38`).
**Impact:** `shared/` cannot be extracted or copied into a new project without the features; adding a user type means editing a shared class. (The class is currently unused, [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-01|BE-R09-01]].)
**Recommendation:** Keep a feature-agnostic `CurrentUser` in `shared/` that exposes the authenticated username/id and authorities only; put type-specific lookups in the features (`ClientService.currentClient()`).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R09-08
**Title:** Hand-made `ObjectMapper` instances bypass Boot's configuration
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Use the container-managed collaborator.
**Evidence:** `private ObjectMapper objectMapper = new ObjectMapper();` in `SecurityConfig` (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:34`) and `new ObjectMapper()` per failed request in the JWT filter (`backend/src/main/java/com/agentForgeBackend/configuration/filter/JWTTokenValidatorFilter.java:86`).
**Impact:** Error bodies written by the security layer ignore any Jackson customization (dates, naming, modules) that the rest of the API uses; a mapper is allocated per failed request.
**Recommendation:** Inject Boot's `ObjectMapper` into the components that write JSON (see [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-02|BE-R05-02]]).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- `shared/` contains only feature-agnostic code; features depend on `shared/`, never the reverse.
- No class without a caller; utilities arrive together with the feature that needs them.
- Conventional names (`AgentForgeBackendApplication`, lower-case packages, `bootstrap`).
- DTOs as records; Lombok only where it removes real boilerplate, one annotation per effect.
- SLF4J placeholders, no PII in logs, `ERROR` only for server faults.
- Framework-managed collaborators (`ObjectMapper`, `Clock`) injected, never constructed ad hoc.

## Related Documents

- [[Docs/backend/Explanations/03-Package-Structure-and-Module-Anatomy]]
- [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review]]
- [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review]]
