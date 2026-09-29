# Review Summary — backend

#doc #review #review-summary #ref-backend

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]

## Overview

| Severity | Count |
|---|---|
| 🔴 Critical | 4 |
| 🟠 High | 10 |
| 🟡 Medium | 26 |
| 🟢 Low | 27 |
| **Total** | **67** |

`backend/` is a well-structured skeleton with one excellent subsystem and one critical gap. The generic
CRUD framework and the ten-file feature convention make features cheap, and the QueryDSL list engine is
a deep, whitelist-first, thoroughly tested module that is worth carrying into the skill almost as is.
The security *design* (BCrypt, stateless JWT, service-level `@PreAuthorize`) is conventional, but the
running application enforces **no authorization at all**, and the generic `update` silently does nothing.
Most remaining findings are fork residue from wpmanager — unused starters and utilities, stale names and
configuration — plus persistence practices (ordinal enums, `ddl-auto=update`, H2 instead of PostgreSQL
in tests) that grow riskier over time. Tests are strong where they exist (the list engine) and absent
everywhere else.

## Findings by Severity

| ID | Title | Severity | Review |
|---|---|---|---|
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-01\|BE-R01-01]] | No authorization is enforced anywhere | 🔴 Critical | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-02\|BE-R01-02]] | Anyone can mint a client JWT | 🔴 Critical | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-03\|BE-R01-03]] | Spring Data REST likely exports user repositories, including password hashes and write operations | 🔴 Critical | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01\|BE-R02-01]] | Base `update` ignores the form | 🔴 Critical | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-04\|BE-R01-04]] | Seed admin with hardcoded password | 🟠 High | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-05\|BE-R01-05]] | One secret for JWT signing and HMAC password derivation | 🟠 High | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-06\|BE-R01-06]] | Client passwords are derived from e-mail; rotation is dead code | 🟠 High | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-07\|BE-R01-07]] | JWT design: claims-only trust, constant subject, no issuer check, no revocation | 🟠 High | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-08\|BE-R01-08]] | Account-status flags are ignored | 🟠 High | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-11\|BE-R01-11]] | Literal secrets in the working tree and in git history | 🟠 High | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03\|BE-R03-03]] | Roles stored as ordinals | 🟠 High | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-04\|BE-R03-04]] | No migrations; `ddl-auto=update` in the runtime config | 🟠 High | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-01\|BE-R08-01]] | `@WithMockUser` masks the missing authorization | 🟠 High | [[Docs/backend/Reviews/08-Testing-Review\|Testing]] |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-02\|BE-R08-02]] | Login, JWT and all write operations are untested | 🟠 High | [[Docs/backend/Reviews/08-Testing-Review\|Testing]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-10\|BE-R01-10]] | CORS origin hardcoded; injected source ignored | 🟡 Medium | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-02\|BE-R02-02]] | Repository and service downcasts | 🟡 Medium | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-03\|BE-R02-03]] | `getAll` is unbounded and returns a `Set` | 🟡 Medium | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-04\|BE-R02-04]] | Every success is `200`; create has no `Location`, delete returns a body | 🟡 Medium | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-06\|BE-R02-06]] | Six type parameters cost more than they buy | 🟡 Medium | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-07\|BE-R02-07]] | Checked exceptions leak into every generic signature | 🟡 Medium | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-01\|BE-R03-01]] | Anemic entities; invariants scattered in services and mappers | 🟡 Medium | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-02\|BE-R03-02]] | Audit dates are never written but are exposed and sortable | 🟡 Medium | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-06\|BE-R03-06]] | Tests use H2 in MySQL mode; production is PostgreSQL | 🟡 Medium | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-07\|BE-R03-07]] | Validation constraints on entities, not on forms | 🟡 Medium | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-01\|BE-R04-01]] | `ClientEntity.getBaseUser()` recurses forever | 🟡 Medium | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review\|Feature Module Correctness]] |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-02\|BE-R04-02]] | `NullPointerException` on missing input fields | 🟡 Medium | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review\|Feature Module Correctness]] |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03\|BE-R04-03]] | Mappers fill different field subsets per DTO | 🟡 Medium | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review\|Feature Module Correctness]] |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-01\|BE-R05-01]] | Five checked exceptions with no common base | 🟡 Medium | [[Docs/backend/Reviews/05-Error-Handling-Review\|Error Handling]] |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-03\|BE-R05-03]] | No fallback handler; unexpected errors leave the contract | 🟡 Medium | [[Docs/backend/Reviews/05-Error-Handling-Review\|Error Handling]] |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-04\|BE-R05-04]] | Internal messages returned on 500; any `IllegalArgumentException` becomes 400 | 🟡 Medium | [[Docs/backend/Reviews/05-Error-Handling-Review\|Error Handling]] |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06\|BE-R05-06]] | Custom body instead of RFC 9457 `ProblemDetail` | 🟡 Medium | [[Docs/backend/Reviews/05-Error-Handling-Review\|Error Handling]] |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-04\|BE-R06-04]] | Query complexity is unbounded | 🟡 Medium | [[Docs/backend/Reviews/06-Query-Engine-Review\|Query Engine]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-01\|BE-R07-01]] | Unused starters and SDKs, some with runtime side effects | 🟡 Medium | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-05\|BE-R07-05]] | SQL logging on in the runtime config; bind-logging line is inert | 🟡 Medium | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-06\|BE-R07-06]] | Test profile (with fixed secrets) ships in the application jar | 🟡 Medium | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-11\|BE-R07-11]] | Env file and comments name variables the code does not read | 🟡 Medium | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-03\|BE-R08-03]] | `contextLoads` fails outside Docker | 🟡 Medium | [[Docs/backend/Reviews/08-Testing-Review\|Testing]] |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-05\|BE-R08-05]] | No Testcontainers; tests run on H2 | 🟡 Medium | [[Docs/backend/Reviews/08-Testing-Review\|Testing]] |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-01\|BE-R09-01]] | ~650 lines of unused code carried over from wpmanager | 🟡 Medium | [[Docs/backend/Reviews/09-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-07\|BE-R09-07]] | `shared/tools/AuthUserUtil` depends on feature packages | 🟡 Medium | [[Docs/backend/Reviews/09-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-09\|BE-R01-09]] | JWT filter is also registered as a servlet filter | 🟢 Low | [[Docs/backend/Reviews/01-Security-Review\|Security]] |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-05\|BE-R02-05]] | No API versioning, no OpenAPI description | 🟢 Low | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-08\|BE-R02-08]] | `Page` serialized directly; JSON shape unstable | 🟢 Low | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-05\|BE-R03-05]] | Repository finders mix nullable returns and `Optional` | 🟢 Low | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-04\|BE-R04-04]] | `apikey` is `Long` on the entity, `String` in DTOs, and never set | 🟢 Low | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review\|Feature Module Correctness]] |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-05\|BE-R04-05]] | Same conflict returns 409 on create but 400 on update | 🟢 Low | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review\|Feature Module Correctness]] |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-02\|BE-R05-02]] | Response-building code duplicated four ways | 🟢 Low | [[Docs/backend/Reviews/05-Error-Handling-Review\|Error Handling]] |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-05\|BE-R05-05]] | Inconsistent handler signatures and a zone-less timestamp | 🟢 Low | [[Docs/backend/Reviews/05-Error-Handling-Review\|Error Handling]] |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-01\|BE-R06-01]] | `nullable()` is never used by the profiles | 🟢 Low | [[Docs/backend/Reviews/06-Query-Engine-Review\|Query Engine]] |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-02\|BE-R06-02]] | Enum and collection fields (`roles`) cannot be filtered | 🟢 Low | [[Docs/backend/Reviews/06-Query-Engine-Review\|Query Engine]] |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-03\|BE-R06-03]] | Reads go through `POST /list` | 🟢 Low | [[Docs/backend/Reviews/06-Query-Engine-Review\|Query Engine]] |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-05\|BE-R06-05]] | Field name repeated three times per profile entry | 🟢 Low | [[Docs/backend/Reviews/06-Query-Engine-Review\|Query Engine]] |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-06\|BE-R06-06]] | Zone-less date/time strings are silently read as UTC | 🟢 Low | [[Docs/backend/Reviews/06-Query-Engine-Review\|Query Engine]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-02\|BE-R07-02]] | `@EnableScheduling` without any scheduled work | 🟢 Low | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-03\|BE-R07-03]] | H2 packaged at runtime scope | 🟢 Low | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-04\|BE-R07-04]] | Surefire pinned below Boot's version; redundant Mockito pin | 🟢 Low | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-07\|BE-R07-07]] | Database host hardcoded to `db` | 🟢 Low | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-08\|BE-R07-08]] | Properties for features that do not exist | 🟢 Low | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-09\|BE-R07-09]] | Development-only Dockerfile referencing a missing compose file | 🟢 Low | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-10\|BE-R07-10]] | Stale project identity in the POM | 🟢 Low | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/backend/Reviews/08-Testing-Review#BE-R08-04\|BE-R08-04]] | Empty suites, unused helper, misleading test | 🟢 Low | [[Docs/backend/Reviews/08-Testing-Review\|Testing]] |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-02\|BE-R09-02]] | Misspelled `boostrap` package and class | 🟢 Low | [[Docs/backend/Reviews/09-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-03\|BE-R09-03]] | Lower-case class names and a camel-case root package | 🟢 Low | [[Docs/backend/Reviews/09-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-04\|BE-R09-04]] | Redundant Lombok annotations | 🟢 Low | [[Docs/backend/Reviews/09-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-05\|BE-R09-05]] | Annotations without effect | 🟢 Low | [[Docs/backend/Reviews/09-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-06\|BE-R09-06]] | String-concatenated logging and PII at `ERROR` level | 🟢 Low | [[Docs/backend/Reviews/09-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-08\|BE-R09-08]] | Hand-made `ObjectMapper` instances bypass Boot's configuration | 🟢 Low | [[Docs/backend/Reviews/09-Code-Hygiene-Review\|Code Hygiene]] |

## Strengths to Keep

- **Whitelist-first dynamic query engine** — typed `QueryableField`s, per-entity `EntityQueryProfile`,
  profile-controlled sort properties, bounded page size, precise 400 messages
  ([[Docs/backend/Reviews/06-Query-Engine-Review]]).
- **Four-layer test pyramid** for the query engine: unit → `@DataJpaTest` → service → MockMvc with
  exact error bodies ([[Docs/backend/Reviews/08-Testing-Review]]).
- **Generic CRUD base classes** that reduce a feature controller to a type binding
  ([[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review]]).
- **Fixed feature-module anatomy** — every feature has the same file roles and names
  ([[Docs/backend/Explanations/03-Package-Structure-and-Module-Anatomy]]).
- **JOINED user hierarchy** with login columns and uniqueness on the root, one polymorphic login
  repository ([[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review]]).
- **Stateless JWT via JJWT 0.12** with `verifyWith` + `parseSignedClaims`, BCrypt passwords, standard
  `AuthenticationManager` login ([[Docs/backend/Reviews/01-Security-Review]]).
- **One error JSON shape** for application and security errors, one `@ControllerAdvice`
  ([[Docs/backend/Reviews/05-Error-Handling-Review]]).
- **Secrets as required environment placeholders**, validated at start-up
  ([[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review]]).
- `open-in-view=false`, constructor injection with `final` fields, QueryDSL APT wired through
  `annotationProcessorPaths`.

## Top 5 Recommendations

1. **Enforce authorization** — `authorizeHttpRequests(... anyRequest().authenticated())` plus
   `@EnableMethodSecurity`, lock down `/client/token/**`, remove Spring Data REST, and add
   anonymous/wrong-role tests
   ([[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]],
   [[Docs/backend/Reviews/01-Security-Review#BE-R01-02|BE-R01-02]],
   [[Docs/backend/Reviews/01-Security-Review#BE-R01-03|BE-R01-03]],
   [[Docs/backend/Reviews/08-Testing-Review#BE-R08-01|BE-R08-01]]).
2. **Fix the generic `update`** with a mapper `update(form, entity)` method and give the framework a
   typed repository parameter instead of downcasts
   ([[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01|BE-R02-01]],
   [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-02|BE-R02-02]]).
3. **Harden identity and tokens** — honour account flags in `SecurityUser`, user id as `sub`, issuer
   check, short-lived revocable tokens, separate keys per purpose, random hashed client API keys, no
   hardcoded seed credentials
   ([[Docs/backend/Reviews/01-Security-Review#BE-R01-08|BE-R01-08]],
   [[Docs/backend/Reviews/01-Security-Review#BE-R01-07|BE-R01-07]],
   [[Docs/backend/Reviews/01-Security-Review#BE-R01-05|BE-R01-05]],
   [[Docs/backend/Reviews/01-Security-Review#BE-R01-06|BE-R01-06]],
   [[Docs/backend/Reviews/01-Security-Review#BE-R01-04|BE-R01-04]]).
4. **Make persistence evolvable** — `@Enumerated(EnumType.STRING)` roles, Flyway with
   `ddl-auto=validate`, JPA auditing for dates, PostgreSQL Testcontainers in tests
   ([[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03|BE-R03-03]],
   [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-04|BE-R03-04]],
   [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-02|BE-R03-02]],
   [[Docs/backend/Reviews/08-Testing-Review#BE-R08-05|BE-R08-05]]).
5. **Adopt a standard, unchecked error model** — one unchecked `ApiException` hierarchy, `ProblemDetail`
   via `ResponseEntityExceptionHandler`, a catch-all handler, and input validation on forms
   ([[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-01|BE-R05-01]],
   [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-03|BE-R05-03]],
   [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06|BE-R05-06]],
   [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-07|BE-R03-07]]).

## Candidate Patterns for the Skill

Input for Task 4 (cross-project comparison). Each candidate names what to take from `backend/` and the
findings that shape the improved version.

| Pattern | Take from `backend/` | Improve per | Confidence it belongs in the skill |
|---|---|---|---|
| Whitelisted dynamic list/search engine (`QueryableField` + `EntityQueryProfile`) | `backend/src/main/java/com/agentForgeBackend/shared/query` | [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-04\|BE-R06-04]], [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-05\|BE-R06-05]], [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-08\|BE-R02-08]] | High — unique to `backend/`, well tested |
| Generic CRUD base controller/service with per-feature type binding | `backend/src/main/java/com/agentForgeBackend/shared/defaultImplements` | [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01\|BE-R02-01]], [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-02\|BE-R02-02]], [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-06\|BE-R02-06]] | High — present in all three projects (lineage) |
| Fixed feature-module file set | `backend/src/main/java/com/agentForgeBackend/models/hq/client` | [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03\|BE-R04-03]] (fewer DTO shapes) | High |
| JOINED user root + polymorphic login repository | `backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java` | [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03\|BE-R03-03]], [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-02\|BE-R03-02]] | Medium — fits multi-user-type systems only |
| Stateless JWT login + validator filter | `backend/src/main/java/com/agentForgeBackend/configuration` | [[Docs/backend/Reviews/01-Security-Review#BE-R01-01\|BE-R01-01]], [[Docs/backend/Reviews/01-Security-Review#BE-R01-07\|BE-R01-07]], [[Docs/backend/Reviews/01-Security-Review#BE-R01-08\|BE-R01-08]], [[Docs/backend/Reviews/01-Security-Review#BE-R01-09\|BE-R01-09]] | High for the idea; compare with resource-server JWT in Task 4 |
| Service-level `@PreAuthorize` + deny-by-default URL rules | intent in `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java` | [[Docs/backend/Reviews/01-Security-Review#BE-R01-01\|BE-R01-01]] | High (as corrected) |
| Central `@ControllerAdvice` with one error shape incl. security errors | `backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java` | [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06\|BE-R05-06]], [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-01\|BE-R05-01]] | High (as `ProblemDetail`) |
| Four-layer test pyramid per subsystem | `backend/src/test/java/com/agentForgeBackend` | [[Docs/backend/Reviews/08-Testing-Review#BE-R08-01\|BE-R08-01]], [[Docs/backend/Reviews/08-Testing-Review#BE-R08-05\|BE-R08-05]] | High |
| QueryDSL APT via `annotationProcessorPaths` (OpenFeign fork) | `backend/pom.xml` | [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-01\|BE-R07-01]] (lean POM) | High if the query engine is adopted |
| Required secrets as `${ENV}` placeholders validated at start-up | `backend/src/main/resources/application.properties` | [[Docs/backend/Reviews/01-Security-Review#BE-R01-05\|BE-R01-05]], [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-11\|BE-R07-11]] | High |

**Not candidates:** the `shared/tools` utilities, the client password derivation, Spring Data REST, the
seed admin with a fixed password, and `POST`-only reads for simple listing.

## Related Documents

- [[Docs/backend/backend-Index]]
- [[Docs/Analysis-Doc-Conventions]]
- [[Docs/backend/Reviews/01-Security-Review]]
- [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review]]
- [[Docs/backend/Reviews/06-Query-Engine-Review]]
