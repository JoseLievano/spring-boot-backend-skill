# wpmanager — Analysis Index

#doc #index #ref-wpmanager

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Conventions:** [[Docs/Analysis-Doc-Conventions]]

## About the Project

wpmanager is the backend of a premium WordPress plugin and theme **repository and distribution** service — not a
tool that manages WordPress sites. Admins register plugins and themes and upload versioned ZIPs; the files go to
S3-compatible storage providers (AWS, Wasabi) and are replicated across them by a scheduled job; clients hold
plans, register websites and keep favorite lists. It sits between BugTracker and `backend/` in the lineage and
holds the most non-CRUD engineering of the three: upload idempotency, a compensating two-phase upload, and a
storage provider abstraction (161 main Java files, about 9,100 lines). It ships its own Obsidian vault,
`wpmanager/wpManagerDocs`, which these docs use as intent and prior art, never as evidence.

**Stack:** Spring Boot 3.4.1 · Java 21 · Spring Security 6.4 · Spring Data JPA / Hibernate 6.6 · JJWT 0.12.5 ·
AWS SDK for Java v2 `s3` 2.20.12 · MySQL (H2 for tests) · Lombok · Maven wrapper.

## Reading Order

1. [[Docs/wpmanager/Explanations/01-Overview-and-Design-Philosophy]] — start here
2. [[Docs/wpmanager/Explanations/07-Upload-Pipeline]] — the project's distinctive flow
3. [[Docs/wpmanager/Explanations/08-Upload-Idempotency]]
4. [[Docs/wpmanager/Explanations/09-Storage-Providers-and-Replication]]
5. [[Docs/wpmanager/Explanations/06-Authentication-and-Authorization]] — includes the method-security answer
6. [[Docs/wpmanager/Explanations/03-Package-Structure-and-Module-Anatomy]] — module catalogue
7. [[Docs/wpmanager/Explanations/04-Generic-CRUD-Framework]]
8. [[Docs/wpmanager/Explanations/05-Domain-Model-and-Persistence]]
9. [[Docs/wpmanager/Explanations/10-Customer-Domain]]
10. [[Docs/wpmanager/Explanations/11-Error-Handling]]
11. [[Docs/wpmanager/Explanations/12-Configuration-and-Secrets]]
12. [[Docs/wpmanager/Explanations/02-Build-Tooling-and-Dependencies]]
13. [[Docs/wpmanager/Explanations/13-Testing-Strategy]]
14. [[Docs/wpmanager/Explanations/14-Recipe-Build-a-Project-This-Way]]
15. [[Docs/wpmanager/Reviews/00-Review-Summary]] — then the individual reviews as needed

## Explanations

- [[Docs/wpmanager/Explanations/01-Overview-and-Design-Philosophy|01 Overview and Design Philosophy]] — what it really is, domain areas, lineage → backend, stack
- [[Docs/wpmanager/Explanations/02-Build-Tooling-and-Dependencies|02 Build, Tooling and Dependencies]] — POM, used vs unused dependencies, Surefire, suite launcher, AI rule file
- [[Docs/wpmanager/Explanations/03-Package-Structure-and-Module-Anatomy|03 Package Structure and Module Anatomy]] — package tree, eight-file module, module catalogue, `PluginService` coupling
- [[Docs/wpmanager/Explanations/04-Generic-CRUD-Framework|04 Generic CRUD Framework]] — Default* stack, inherited endpoints, override catalogue, casts
- [[Docs/wpmanager/Explanations/05-Domain-Model-and-Persistence|05 Domain Model and Persistence]] — two JOINED trees, version/copy model, equality styles, transactions, MySQL
- [[Docs/wpmanager/Explanations/06-Authentication-and-Authorization|06 Authentication and Authorization]] — JSON login, claims-only JWT, method-security activation, ownership, client tokens
- [[Docs/wpmanager/Explanations/07-Upload-Pipeline|07 Upload Pipeline]] — `POST /plugin/upload` end to end, two phases, compensation, theme difference
- [[Docs/wpmanager/Explanations/08-Upload-Idempotency|08 Upload Idempotency]] — key, state machine, hybrid cache, replay, cleanup
- [[Docs/wpmanager/Explanations/09-Storage-Providers-and-Replication|09 Storage Providers and Replication]] — provider hierarchy, S3 client, default provider, `FileDuplicator`
- [[Docs/wpmanager/Explanations/10-Customer-Domain|10 Customer Domain]] — clients, plans, websites, favorite lists, URL probing, ownership rules
- [[Docs/wpmanager/Explanations/11-Error-Handling|11 Error Handling]] — exception → status table, `ErrorHTTPRes`, security handlers, gaps
- [[Docs/wpmanager/Explanations/12-Configuration-and-Secrets|12 Configuration and Secrets]] — every property, env vars and fallbacks (redacted), fail-fast, test profile
- [[Docs/wpmanager/Explanations/13-Testing-Strategy|13 Testing Strategy]] — inventory, tags and suites, real-JWT E2E, recorded run
- [[Docs/wpmanager/Explanations/14-Recipe-Build-a-Project-This-Way|14 Recipe: Build a Project This Way]] — new project + a new downloadable type, with review callouts

## Reviews

- [[Docs/wpmanager/Reviews/00-Review-Summary|00 Review Summary]] — all 88 findings by severity, shared-with-backend table, strengths, top 5, skill candidates
- [[Docs/wpmanager/Reviews/01-Security-Review|01 Security Review]] — 15 findings (6 🔴, 3 🟠)
- [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review|02 CRUD Framework and API Design Review]] — 8 findings (1 🔴)
- [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review|03 Upload Transactions and Consistency Review]] — 6 findings (1 🔴, 2 🟠)
- [[Docs/wpmanager/Reviews/04-Idempotency-Review|04 Idempotency Review]] — 6 findings (2 🟠)
- [[Docs/wpmanager/Reviews/05-Storage-Integration-Review|05 Storage Integration Review]] — 7 findings (2 🟠)
- [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review|06 Domain Model and Persistence Review]] — 10 findings (3 🟠)
- [[Docs/wpmanager/Reviews/07-Service-Design-Review|07 Service Design Review]] — 11 findings (1 🔴, 2 🟠)
- [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review|08 Error Handling and Validation Review]] — 7 findings
- [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review|09 Build, Dependencies and Configuration Review]] — 7 findings
- [[Docs/wpmanager/Reviews/10-Testing-Review|10 Testing Review]] — 6 findings (2 🟠)
- [[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review|11 Code Hygiene and Docs Drift Review]] — 5 findings, plus the unconfirmed vault leads

## Findings Count

| Severity | Count |
|---|---|
| 🔴 Critical | 9 |
| 🟠 High | 16 |
| 🟡 Medium | 43 |
| 🟢 Low | 20 |
| **Total** | **88** (34 share a root cause with `backend/`) |

## Merges and Skips

- **Flows get their own docs.** Upload (07), idempotency (08) and storage/replication (09) are separate
  explanations with separate reviews (03, 04, 05), because they are this project's distinctive engineering.
- **Domain model and persistence merged** into one explanation (05) and one review (06); the customer side has
  its own explanation (10) because its ownership rules matter for security.
- **Service design** is reviewed as one area (07) rather than per module; the module catalogue in 03 is the
  only place every module appears.
- **Configuration and secrets** (12) is separate from build (02); secret findings live in the security review,
  configuration findings in review 09.
- **The project's own vault** is audited in review 11 (WP-R11-04) and its unconfirmed open bug notes are listed
  there; confirmed notes are cited as prior art in the owning findings.
- **Nothing skipped.** Every planned topic exists in the code.
