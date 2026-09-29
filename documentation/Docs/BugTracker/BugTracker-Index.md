# BugTracker — Analysis Index

#doc #index #ref-bugtracker

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Conventions:** [[Docs/Analysis-Doc-Conventions]]

## About the Project

BugTracker is the backend of a two-sided project-management SaaS: an operator side (`models/HQ` — MainHQ,
plans, HQ clients, admins, HQ employees, SaaS invoices) and a tenant side (`models/client` — each Business
with its users, configurable statuses/priorities/types/task categories, projects, tasks, channels, comments,
mentions, docs and knowledge base). It is the oldest and largest of the three reference projects (341 main
Java files, ~17.7k lines, 32 entities, 29 feature modules) and the ancestor of the generic CRUD stack used by
wpmanager and `backend/`.

**Stack:** Spring Boot 2.7.0 · Java 17 effective (`java.version` says 11) · Spring Security 5.7 · Spring Data
JPA / Hibernate 5.6 (`javax.persistence`) · QueryDSL 5 (`apt-maven-plugin`) · JJWT 0.11.2 · MySQL · Lombok ·
Maven 3.8.4 wrapper.

## Reading Order

1. [[Docs/BugTracker/Explanations/01-Overview-and-Design-Philosophy]] — start here
2. [[Docs/BugTracker/Explanations/07-Domain-Model]] — the project's most transferable asset
3. [[Docs/BugTracker/Explanations/03-Package-Structure-and-Module-Anatomy]] — module catalogue
4. [[Docs/BugTracker/Explanations/04-Generic-CRUD-Framework]]
5. [[Docs/BugTracker/Explanations/05-DTO-Tiers-and-Mapping]]
6. [[Docs/BugTracker/Explanations/06-Dynamic-Filtering-and-Pagination]]
7. [[Docs/BugTracker/Explanations/09-Service-Layer-Patterns]]
8. [[Docs/BugTracker/Explanations/08-Persistence-and-Transactions]]
9. [[Docs/BugTracker/Explanations/10-Authentication-and-Authorization]]
10. [[Docs/BugTracker/Explanations/11-Collaboration-Features]]
11. [[Docs/BugTracker/Explanations/12-Error-Handling]]
12. [[Docs/BugTracker/Explanations/02-Build-Tooling-and-Dependencies]]
13. [[Docs/BugTracker/Explanations/13-Configuration-and-Bootstrap]]
14. [[Docs/BugTracker/Explanations/14-Recipe-Build-a-Project-This-Way]]
15. [[Docs/BugTracker/Reviews/00-Review-Summary]] — then the individual reviews as needed

## Explanations

- [[Docs/BugTracker/Explanations/01-Overview-and-Design-Philosophy|01 Overview and Design Philosophy]] — two-sided SaaS, principles, lineage, stack
- [[Docs/BugTracker/Explanations/02-Build-Tooling-and-Dependencies|02 Build, Tooling and Dependencies]] — POM, APT, Java 11 vs 17, used/unused dependencies
- [[Docs/BugTracker/Explanations/03-Package-Structure-and-Module-Anatomy|03 Package Structure and Module Anatomy]] — package tree, ten-file module, catalogue of all 29 modules
- [[Docs/BugTracker/Explanations/04-Generic-CRUD-Framework|04 Generic CRUD Framework]] — Default* stack, eight endpoints, override points, `updateListFields`
- [[Docs/BugTracker/Explanations/05-DTO-Tiers-and-Mapping|05 DTO Tiers and Mapping]] — Form/DTO/MiniDTO/ListDTO, hand-written mappers, `@Lazy` cycles
- [[Docs/BugTracker/Explanations/06-Dynamic-Filtering-and-Pagination|06 Dynamic Filtering and Pagination]] — JSON contract, operators, `CommonPathExpression` + `XPredicate`
- [[Docs/BugTracker/Explanations/07-Domain-Model|07 Domain Model]] — HQ and tenant ER diagrams, Business aggregate, user hierarchy, trees, M:N
- [[Docs/BugTracker/Explanations/08-Persistence-and-Transactions|08 Persistence and Transactions]] — repositories, ids, fetch types, counters, rollback rules
- [[Docs/BugTracker/Explanations/09-Service-Layer-Patterns|09 Service Layer Patterns]] — insert workflow, uniqueness, priority ordering, overrides
- [[Docs/BugTracker/Explanations/10-Authentication-and-Authorization|10 Authentication and Authorization]] — Basic login → JWT, validator filter, roles, CORS
- [[Docs/BugTracker/Explanations/11-Collaboration-Features|11 Collaboration Features]] — channels, comments, mentions, comment feed
- [[Docs/BugTracker/Explanations/12-Error-Handling|12 Error Handling]] — exception inventory, the one handler, what clients receive
- [[Docs/BugTracker/Explanations/13-Configuration-and-Bootstrap|13 Configuration and Bootstrap]] — properties, literals in code, seeding sequence
- [[Docs/BugTracker/Explanations/14-Recipe-Build-a-Project-This-Way|14 Recipe: Build a Project This Way]] — new project + new tenant-scoped module, with review callouts

## Reviews

- [[Docs/BugTracker/Reviews/00-Review-Summary|00 Review Summary]] — all 86 findings by severity, defects vs era gaps, strengths, top 5, skill candidates
- [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review|01 Security and Multi-Tenancy Review]] — 14 findings (3 🔴, 4 🟠)
- [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review|02 CRUD Framework and API Design Review]] — 9 findings (1 🔴, 1 🟠)
- [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review|03 Domain Model and Persistence Review]] — 11 findings (1 🔴, 2 🟠)
- [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review|04 Performance and Fetching Review]] — 5 findings (1 🟠)
- [[Docs/BugTracker/Reviews/05-Filter-Engine-Review|05 Filter Engine Review]] — 9 findings (2 🟠)
- [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review|06 Service Layer Correctness Review]] — 13 findings (4 🟠)
- [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review|07 Error Handling and Validation Review]] — 6 findings (2 🟠)
- [[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review|08 Build, Dependencies and Configuration Review]] — 9 findings (1 🟠)
- [[Docs/BugTracker/Reviews/09-Testing-Review|09 Testing Review]] — 4 findings (2 🟠)
- [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review|10 Code Hygiene Review]] — 6 findings

## Findings Count

| Severity | Count |
|---|---|
| 🔴 Critical | 5 |
| 🟠 High | 19 |
| 🟡 Medium | 34 |
| 🟢 Low | 28 |
| **Total** | **86** (81 defects, 5 era gaps) |

## Merges and Skips

- **Testing explanation skipped.** The project has a single `contextLoads` test that needs a live MySQL
  (`BugTracker/src/test/java/com/bgsystem/bugtracker/BugTrackerApplicationTests.java`); there is no testing
  strategy to explain, and the conventions forbid stub docs. Testing is covered by
  [[Docs/BugTracker/Reviews/09-Testing-Review]].
- **Domain model and persistence split** into two explanations (07 and 08), unlike `backend/`, because the
  32-entity two-sided domain is the project's main asset.
- **Pattern-level explanations.** The 29 near-identical modules are explained once, using `bsPrTask` as the
  worked example; the module catalogue in 03 is the only place every module appears.
- **Configuration and secrets** are merged with bootstrap/seeding (13); secret findings live in the
  security review.
