# Review Summary — BugTracker

#doc #review #review-summary #ref-bugtracker

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]

## Overview

**86 findings: 5 🔴 Critical, 19 🟠 High, 34 🟡 Medium, 28 🟢 Low.** 81 are defects (wrong even on Boot 2.7),
5 are era gaps (acceptable in 2022, not what a new project should do).

BugTracker's lasting value is its **domain model** — an operator/tenant split, tenant-configurable
taxonomies, a single user hierarchy, knowledge trees and project collaboration — and its **grid-oriented
request contract** for filtering, sorting and paging. Its generic CRUD stack is the ancestor of the one in
wpmanager and `backend/`, including the no-op `update` that all three share. As a running system it is not
safe: authorization stops at "is authenticated", nothing isolates tenants, the signing key is in source,
several core operations throw at runtime (second HQ invoice, channel with members, deleting a shared task
category), and the filter engine keeps per-request state in a singleton. Almost nothing is tested.

## Findings by Severity

| ID | Title | Severity | Review |
|---|---|---|---|
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01\|BT-R01-01]] | Authorization is authentication only | 🔴 Critical | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02\|BT-R01-02]] | No tenant isolation | 🔴 Critical | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-03\|BT-R01-03]] | JWT key, DB credentials and seed passwords are literals in source | 🔴 Critical | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-01\|BT-R02-01]] | Base `update` ignores the form (22 of 31 services) | 🔴 Critical | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-01\|BT-R03-01]] | `equals` casts to `PlanEntity` in `InvoiceEntity` and `EmployeeEntity` | 🔴 Critical | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-04\|BT-R01-04]] | `POST /user` accepts roles from the caller | 🟠 High | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-05\|BT-R01-05]] | Authors and actors come from the request body | 🟠 High | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-06\|BT-R01-06]] | Usernames unique only per user kind; login picks an arbitrary match | 🟠 High | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-07\|BT-R01-07]] | Spring Data REST likely exports every repository | 🟠 High | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-02\|BT-R02-02]] | A client-supplied `id` on `POST` overwrites an existing row | 🟠 High | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-05\|BT-R03-05]] | Lombok `@Builder` drops collection and flag initializers | 🟠 High | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-09\|BT-R03-09]] | No migrations; `ddl-auto=update` | 🟠 High | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-01\|BT-R04-01]] | Fifteen EAGER collections on the tenant root pull in the tenant graph | 🟠 High | [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review\|Performance and Fetching]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-01\|BT-R05-01]] | Filters are stored on a shared singleton | 🟠 High | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-07\|BT-R05-07]] | No field or sort whitelist (password hashes can be probed) | 🟠 High | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-01\|BT-R06-01]] | Uniqueness of tenant data is checked globally | 🟠 High | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-02\|BT-R06-02]] | Priority delete reorders every tenant's priorities | 🟠 High | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-03\|BT-R06-03]] | Channel insert fails with members; its uniqueness check uses a null project | 🟠 High | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-04\|BT-R06-04]] | Task category delete throws `ConcurrentModificationException` | 🟠 High | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-01\|BT-R07-01]] | Domain exceptions are unmapped and become 500 | 🟠 High | [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-02\|BT-R07-02]] | Checked exceptions commit partial work | 🟠 High | [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-01\|BT-R08-01]] | Spring Boot 2.7 is out of OSS support, and this is 2.7.0 | 🟠 High | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-01\|BT-R09-01]] | One test, and it needs a live MySQL | 🟠 High | [[Docs/BugTracker/Reviews/09-Testing-Review\|Testing]] |
| [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-02\|BT-R09-02]] | No coverage of security, services or the filter engine | 🟠 High | [[Docs/BugTracker/Reviews/09-Testing-Review\|Testing]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-08\|BT-R01-08]] | Token validator fails with 500 instead of 401 | 🟡 Medium | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-09\|BT-R01-09]] | Token generator dereferences before its null check; filters registered twice | 🟡 Medium | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-10\|BT-R01-10]] | Ten-day tokens, no revocation, constant subject | 🟡 Medium | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-11\|BT-R01-11]] | Account-status flags are ignored | 🟡 Medium | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-03\|BT-R02-03]] | Depth diagnostic: a ten-file, six-parameter tax per entity | 🟡 Medium | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-04\|BT-R02-04]] | Deletion test: per-module predicates and mappers are shallow | 🟡 Medium | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-05\|BT-R02-05]] | `GET ""` is unbounded and returns a `Set` | 🟡 Medium | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-07\|BT-R02-07]] | Every success is `200`; create has no `Location` | 🟡 Medium | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-02\|BT-R03-02]] | Subclasses redeclare `@Id` with `IDENTITY` under a `SEQUENCE` JOINED root | 🟡 Medium | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-04\|BT-R03-04]] | Inconsistent entity equality | 🟡 Medium | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-06\|BT-R03-06]] | Anemic model; invariants scattered in services | 🟡 Medium | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-07\|BT-R03-07]] | No auditing, no optimistic locking | 🟡 Medium | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-10\|BT-R03-10]] | Roles are free strings; the `Role` enum is unused and disagrees | 🟡 Medium | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-11\|BT-R03-11]] | Cascade-remove chains delete whole tenants | 🟡 Medium | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-02\|BT-R04-02]] | N+1 queries in list views through `updateListFields` | 🟡 Medium | [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review\|Performance and Fetching]] |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-03\|BT-R04-03]] | List views write to the database (write-on-read) | 🟡 Medium | [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review\|Performance and Fetching]] |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-04\|BT-R04-04]] | Counters drift and several are computed wrongly | 🟡 Medium | [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review\|Performance and Fetching]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-02\|BT-R05-02]] | Type dispatch by value: `"false"` and date-shaped strings are misread | 🟡 Medium | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-03\|BT-R05-03]] | `getDeclaredField` misses inherited fields; unknown fields give 500 | 🟡 Medium | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-04\|BT-R05-04]] | Invalid dates on association paths cause a `NullPointerException` | 🟡 Medium | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-05\|BT-R05-05]] | `POST /list` ignores its filter | 🟡 Medium | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-06\|BT-R05-06]] | `PageableRequest` fails on a missing `sort`; `Optional` as a field | 🟡 Medium | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-05\|BT-R06-05]] | Type update's category diff does not work | 🟡 Medium | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-06\|BT-R06-06]] | HQ client update: eager `orElse` and fallback to another client | 🟡 Medium | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-10\|BT-R06-10]] | Task invoice and channel↔task links are never written | 🟡 Medium | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-12\|BT-R06-12]] | Category tree updates allow cycles and stale parents | 🟡 Medium | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-03\|BT-R07-03]] | Validation is mostly manual `null` checks | 🟡 Medium | [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-04\|BT-R07-04]] | Checked exceptions without a base leak into every generic signature | 🟡 Medium | [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-05\|BT-R07-05]] | Custom error body instead of RFC 7807/9457 problem details | 🟡 Medium | [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-02\|BT-R08-02]] | `javax` namespace ties the code to Boot 2 | 🟡 Medium | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-03\|BT-R08-03]] | `java.version` 11 but compiler 17 | 🟡 Medium | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-04\|BT-R08-04]] | Six unused starters | 🟡 Medium | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-07\|BT-R08-07]] | No profiles; environment values hardcoded | 🟡 Medium | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-05\|BT-R10-05]] | `@Lazy` in 28 files hides cyclic mapper dependencies; mixed injection styles | 🟡 Medium | [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-12\|BT-R01-12]] | Unknown username raises `NoSuchElementException` | 🟢 Low | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-13\|BT-R01-13]] | `ErrorResponseBody.trace` returns exception text | 🟢 Low | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-14\|BT-R01-14]] | Pre-6.x security configuration style | 🟢 Low | [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|Security and Multi-Tenancy]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-06\|BT-R02-06]] | Inconsistent route naming | 🟢 Low | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-08\|BT-R02-08]] | No versioning, no OpenAPI, `Page` serialized directly | 🟢 Low | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-09\|BT-R02-09]] | `bsPrCommentController` departs from the pattern | 🟢 Low | [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|CRUD Framework and API Design]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-03\|BT-R03-03]] | Mixed identifier strategies, no named generators | 🟢 Low | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-08\|BT-R03-08]] | `optional = true` with `nullable = false` on task type | 🟢 Low | [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|Domain Model and Persistence]] |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-05\|BT-R04-05]] | Whole tables loaded for existence checks | 🟢 Low | [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review\|Performance and Fetching]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-08\|BT-R05-08]] | Association date paths are built from a path's `toString()` | 🟢 Low | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-09\|BT-R05-09]] | `countryPredicate` has no root path; dead helper | 🟢 Low | [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|Filter Engine]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-07\|BT-R06-07]] | MainHQ fetched with `findAll().get(0)`; null checks are dead | 🟢 Low | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-08\|BT-R06-08]] | Wrong exception types and `orElseThrow(null)` | 🟢 Low | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-09\|BT-R06-09]] | "Print before delete" workaround in three services | 🟢 Low | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-11\|BT-R06-11]] | Mentions saved twice; naive `@username` parsing | 🟢 Low | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-13\|BT-R06-13]] | Inconsistent input guards cause 500s | 🟢 Low | [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|Service Layer Correctness]] |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-06\|BT-R07-06]] | Misspelled package and class names | 🟢 Low | [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review\|Error Handling and Validation]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-05\|BT-R08-05]] | Batch starter version overrides the BOM | 🟢 Low | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-06\|BT-R08-06]] | H2 on the runtime classpath, unused | 🟢 Low | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-08\|BT-R08-08]] | No README or run instructions | 🟢 Low | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-09\|BT-R08-09]] | Unmaintained `apt-maven-plugin` for QueryDSL | 🟢 Low | [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|Build, Dependencies and Configuration]] |
| [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-03\|BT-R09-03]] | Test dependencies declared but unused | 🟢 Low | [[Docs/BugTracker/Reviews/09-Testing-Review\|Testing]] |
| [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-04\|BT-R09-04]] | The last recorded test run predates the current build | 🟢 Low | [[Docs/BugTracker/Reviews/09-Testing-Review\|Testing]] |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-01\|BT-R10-01]] | `System.out`/`printStackTrace` instead of a logger | 🟢 Low | [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-02\|BT-R10-02]] | Dead code, test endpoints and orphans | 🟢 Low | [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-03\|BT-R10-03]] | Lower-case class names | 🟢 Low | [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-04\|BT-R10-04]] | Repositories annotated `@Service` | 🟢 Low | [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review\|Code Hygiene]] |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-06\|BT-R10-06]] | Redundant annotations, duplicate calls, unused imports | 🟢 Low | [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review\|Code Hygiene]] |

## Defects vs Era Gaps

Every finding is either a **defect** or an **era gap** (rule from
[[Tasks/current/Spring-Boot-Skill-Creation-step-2-Analyze-BugTracker|Task 2]]: era gaps are at most 🟡 unless they
carry a security or end-of-life risk, and they fill in **Version note**).

### Era gaps

| ID | Gap | Severity | What a new project does instead |
|---|---|---|---|
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-01\|BT-R08-01]] | Spring Boot 2.7 (OSS support ended 2023-06-30), at 2.7.0 | 🟠 High (EOL risk) | A supported Boot 3.x line |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-02\|BT-R08-02]] | `javax.*` namespace | 🟡 Medium | `jakarta.*` |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-14\|BT-R01-14]] | `@EnableGlobalMethodSecurity`, `.and()` DSL | 🟢 Low | `@EnableMethodSecurity`, lambda DSL |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-05\|BT-R07-05]] | Custom error body | 🟡 Medium | `ProblemDetail` (RFC 9457) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-09\|BT-R08-09]] | `apt-maven-plugin` for QueryDSL | 🟢 Low | `querydsl-apt` in `annotationProcessorPaths` |

### Defects by review

| Review | Defects |
|---|---|
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review\|01 Security and Multi-Tenancy]] | BT-R01-01 … BT-R01-13 (13) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review\|02 CRUD Framework and API Design]] | BT-R02-01 … BT-R02-09 (9) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review\|03 Domain Model and Persistence]] | BT-R03-01 … BT-R03-11 (11) |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review\|04 Performance and Fetching]] | BT-R04-01 … BT-R04-05 (5) |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review\|05 Filter Engine]] | BT-R05-01 … BT-R05-09 (9) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review\|06 Service Layer Correctness]] | BT-R06-01 … BT-R06-13 (13) |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review\|07 Error Handling and Validation]] | BT-R07-01 … BT-R07-04, BT-R07-06 (5) |
| [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review\|08 Build, Dependencies and Configuration]] | BT-R08-03 … BT-R08-08 (6) |
| [[Docs/BugTracker/Reviews/09-Testing-Review\|09 Testing]] | BT-R09-01 … BT-R09-04 (4) |
| [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review\|10 Code Hygiene]] | BT-R10-01 … BT-R10-06 (6) |

Total: 81 defects + 5 era gaps = 86.

## Strengths to Keep

- **Operator/tenant domain split** with one tenant root and a mandatory `business` reference on every tenant
  row ([[Docs/BugTracker/Explanations/07-Domain-Model]]).
- **Tenant-configurable taxonomies** — status (with colour), priority (with order), type, task category and
  the category↔type allow-list — as entities, not enums
  ([[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review]]).
- **One user hierarchy** for every kind of principal, so login and authorship are uniform.
- **DTO tiers for grid UIs** — detail DTO, reference MiniDTO, grid ListDTO with counts — and a small `insert`
  response ([[Docs/BugTracker/Explanations/05-DTO-Tiers-and-Mapping]]).
- **Rich filter/sort/page contract**: per-field operator lists, OR within a field, AND across fields,
  association filters, multi-sort ([[Docs/BugTracker/Reviews/05-Filter-Engine-Review]]).
- **Generic controller** giving every entity the same endpoints with a 15-line subclass
  ([[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review]]).
- **Explicit service override points** and step-commented insert workflows
  ([[Docs/BugTracker/Explanations/09-Service-Layer-Patterns]]).
- **Collaboration model**: channels, comments, server-generated mentions, a newest-first comment feed
  ([[Docs/BugTracker/Explanations/11-Collaboration-Features]]).
- **First-run seeder** that creates the operator, an admin and a demo tenant through the services.
- BCrypt passwords, stateless tokens, per-request reload of roles, `open-in-view=false`.

## Top 5 Recommendations

1. **Add authorization and tenant isolation together** — a role matrix at the URL level plus a
   `TenantContext` from the principal, applied by the base service to every read, write and predicate; remove
   `business`, `author` and `roles` from client input
   ([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01|BT-R01-01]],
   [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02|BT-R01-02]],
   [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-04|BT-R01-04]],
   [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-05|BT-R01-05]]).
2. **Move secrets out of code and rotate them** ([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-03|BT-R01-03]]).
3. **Fix the generic stack's write path** — a real `update` via `mapper.updateEntity`, server-assigned ids,
   unchecked domain exceptions mapped to 4xx with rollback
   ([[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-01|BT-R02-01]],
   [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-02|BT-R02-02]],
   [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-01|BT-R07-01]],
   [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-02|BT-R07-02]]).
4. **Replace the filter engine's implementation, keep its contract** — stateless, whitelist-first, typed by
   field ([[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-01|BT-R05-01]],
   [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-07|BT-R05-07]]).
5. **Fix the persistence foundations** — correct `equals`, `@Builder.Default`, LAZY associations, per-tenant
   unique constraints, Flyway — and put the prioritized test list of
   [[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-02|BT-R09-02]] in place first
   ([[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-01|BT-R03-01]],
   [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-05|BT-R03-05]],
   [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-01|BT-R04-01]],
   [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-01|BT-R06-01]]).

## Candidate Patterns for the Skill

| Pattern | Take from BugTracker | Change for the skill |
|---|---|---|
| Multi-tenant domain root | `BusinessEntity` + mandatory `business` FK on tenant rows | Tenant from the principal; `TenantEntity` base; LAZY; composite unique keys per tenant |
| Tenant-configurable taxonomies | Status/priority/type/category as tenant-owned entities | Ordering and allow-lists as entity methods; unique per tenant |
| User hierarchy | One JOINED root for all principals | One id on the root; unique username; roles as enum strings |
| DTO tiers | Detail / reference / grid-row response shapes | Only the tiers an entity needs; MapStruct; no cycles |
| Filter/sort/page contract | `FilterRequest` + operator lists + `SortInfo` + `PageableRequest` | Stateless whitelist engine (as in `backend/`), bounded page size, typed parsing |
| Generic controller + service hooks | `DefaultController`, override points | Real `update`; server-assigned ids; unchecked exceptions; tenant enforcement in the base |
| Counters for grids | ListDTO shows counts | Computed by grouped query per page, not stored-and-recomputed |
| Collaboration | Channel/comment/mention model; server-side mention parsing | Author from context; regex parsing; scope mentions to channel members |
| Tree categories | Self-reference + `level` | Cycle check; recompute descendants |
| First-run seeding | `ApplicationRunner` through the services | Credentials from configuration; idempotent |

## Related Documents

- [[Docs/BugTracker/BugTracker-Index]]
- [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review]]
- [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review]]
- [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review]]
- [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review]]
- [[Docs/BugTracker/Reviews/05-Filter-Engine-Review]]
- [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review]]
- [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review]]
- [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review]]
- [[Docs/BugTracker/Reviews/09-Testing-Review]]
- [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review]]
- [[Docs/backend/Reviews/00-Review-Summary]] — the descendant project's findings
