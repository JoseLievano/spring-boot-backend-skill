# backend — Analysis Index

#doc #index #ref-backend

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Conventions:** [[Docs/Analysis-Doc-Conventions]]

## About the Project

`backend/` (Maven artifact `agentForgeBackend`) is a user-centric REST backend for two user types —
admins and clients — with JSON login, stateless JWTs, a generic CRUD framework and a QueryDSL-based
dynamic list/filter engine. It is the newest of the three reference projects (lineage BugTracker →
wpmanager → `backend/`) and replaces this workspace's former `auth-server/`.

**Stack:** Spring Boot 3.4.1 · Java 21 · Spring Security 6.4.2 · Spring Data JPA / Hibernate 6.6.4 ·
OpenFeign QueryDSL 6.12 · JJWT 0.12.5 · PostgreSQL (runtime) · H2 (tests) · Lombok · Maven.

## Reading Order

1. [[Docs/backend/Explanations/01-Overview-and-Design-Philosophy]] — start here
2. [[Docs/backend/Explanations/03-Package-Structure-and-Module-Anatomy]]
3. [[Docs/backend/Explanations/04-Generic-CRUD-Framework]]
4. [[Docs/backend/Explanations/05-Dynamic-List-Query-Engine]]
5. [[Docs/backend/Explanations/06-Domain-Model-and-Persistence]]
6. [[Docs/backend/Explanations/07-Authentication-and-Authorization]]
7. [[Docs/backend/Explanations/08-Error-Handling]]
8. [[Docs/backend/Explanations/02-Build-Tooling-and-Dependencies]]
9. [[Docs/backend/Explanations/09-Configuration-and-Secrets]]
10. [[Docs/backend/Explanations/10-Testing-Strategy]]
11. [[Docs/backend/Explanations/11-Recipe-Build-a-Project-This-Way]]
12. [[Docs/backend/Reviews/00-Review-Summary]] — then the individual reviews as needed

## Explanations

- [[Docs/backend/Explanations/01-Overview-and-Design-Philosophy|01 Overview and Design Philosophy]] — what the service is, lineage, principles, stack
- [[Docs/backend/Explanations/02-Build-Tooling-and-Dependencies|02 Build, Tooling and Dependencies]] — POM, Lombok + QueryDSL APT, used/unused dependencies, Surefire, Docker, `test.sh`
- [[Docs/backend/Explanations/03-Package-Structure-and-Module-Anatomy|03 Package Structure and Module Anatomy]] — package tree and the ten-file feature module
- [[Docs/backend/Explanations/04-Generic-CRUD-Framework|04 Generic CRUD Framework]] — base controller/service/repository/mapper and their type parameters
- [[Docs/backend/Explanations/05-Dynamic-List-Query-Engine|05 Dynamic List Query Engine]] — `POST /list` contract, operators, whitelisting, traced request
- [[Docs/backend/Explanations/06-Domain-Model-and-Persistence|06 Domain Model and Persistence]] — JOINED user hierarchy, roles, repositories, transactions, databases
- [[Docs/backend/Explanations/07-Authentication-and-Authorization|07 Authentication and Authorization]] — login, JWT, filter, client tokens, intended vs actual authorization
- [[Docs/backend/Explanations/08-Error-Handling|08 Error Handling]] — exceptions, status mapping, `ErrorHTTPRes`
- [[Docs/backend/Explanations/09-Configuration-and-Secrets|09 Configuration and Secrets]] — every property, env variables, `@Value` injection points
- [[Docs/backend/Explanations/10-Testing-Strategy|10 Testing Strategy]] — test inventory, layers, suites, last recorded run
- [[Docs/backend/Explanations/11-Recipe-Build-a-Project-This-Way|11 Recipe: Build a Project This Way]] — step-by-step new project + new feature, with review callouts

## Reviews

- [[Docs/backend/Reviews/00-Review-Summary|00 Review Summary]] — all 67 findings by severity, strengths, top 5, skill candidates
- [[Docs/backend/Reviews/01-Security-Review|01 Security Review]] — 11 findings (3 🔴, 6 🟠)
- [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review|02 CRUD Framework and API Design Review]] — 8 findings (1 🔴)
- [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review|03 Domain Model and Persistence Review]] — 7 findings (2 🟠)
- [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review|04 Feature Module Correctness Review]] — 5 findings
- [[Docs/backend/Reviews/05-Error-Handling-Review|05 Error Handling Review]] — 6 findings
- [[Docs/backend/Reviews/06-Query-Engine-Review|06 Query Engine Review]] — 6 findings (mostly strengths)
- [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review|07 Build, Dependencies and Configuration Review]] — 11 findings
- [[Docs/backend/Reviews/08-Testing-Review|08 Testing Review]] — 5 findings (2 🟠)
- [[Docs/backend/Reviews/09-Code-Hygiene-Review|09 Code Hygiene Review]] — 8 findings

## Findings Count

| Severity | Count |
|---|---|
| 🔴 Critical | 4 |
| 🟠 High | 10 |
| 🟡 Medium | 26 |
| 🟢 Low | 27 |
| **Total** | **67** |

## Merges and Skips

- **No merges or skips of planned documents.** All 11 planned Explanations and all 10 planned Reviews
  exist.
- **Topics skipped because they do not exist in `backend/`:** file uploads, scheduled jobs, caching,
  messaging, API documentation (OpenAPI), database migrations. Where their *absence* matters it is a
  finding (e.g. [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-04|BE-R03-04]],
  [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-05|BE-R02-05]]).
- **Placement decisions:** the HTTP semantics of `POST /list` live in the Query Engine review
  ([[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-03|BE-R06-03]]) rather than the CRUD review; the
  failing `contextLoads` test is a Testing finding
  ([[Docs/backend/Reviews/08-Testing-Review#BE-R08-03|BE-R08-03]]) with its batch-starter cause linked
  from [[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-01|BE-R07-01]].
