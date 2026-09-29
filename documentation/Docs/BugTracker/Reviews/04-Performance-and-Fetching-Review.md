# Performance and Fetching Review — BugTracker

#doc #review #ref-bugtracker #persistence

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/08-Persistence-and-Transactions]]

## Scope

Reviewed: fetch types on every association, the `updateListFields` overrides and where they run, the list
endpoints of the generic service, counter columns and their mappers, and whole-table reads used as
existence checks. No query was executed; SQL counts are derived from mappings and marked where they
depend on Hibernate's runtime choices.

## Verdict

The design intent — precomputed counters so grids stay cheap — is sound, but the implementation does the
opposite. Fifteen EAGER collections on the tenant root, plus JPA's default EAGER many-to-ones, mean that
loading almost any tenant entity loads most of its tenant. List views then walk lazy collections per row
to recompute counters (N+1) and write the results back inside the read request. The counters are stale
everywhere else, and several are computed from the wrong collection. These are **defects** on Boot 2.7 as
well; none is an era gap.

## Strengths to Keep

- Paging is the default for list screens (`POST /page`, `/page-list-view`) and pushes filters into SQL
  through QueryDSL (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:89-109`).
- ListDTOs expose counts instead of nested collections, keeping grid payloads small
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessListDTO.java:35-63`).
- `open-in-view=false` keeps lazy loading inside transactions, which makes the N+1 visible in the service
  rather than hidden in serialization (`BugTracker/src/main/resources/application.properties:6`).
- Most collections are lazy by default; the comment feed has a dedicated, sorted, paged query
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrComment/bsPrCommentRepository.java:13`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-01\|BT-R04-01]] | Fifteen EAGER collections on the tenant root pull in the tenant graph | 🟠 High | performance | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-02\|BT-R04-02]] | N+1 queries in list views through `updateListFields` | 🟡 Medium | performance | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-03\|BT-R04-03]] | List views write to the database (write-on-read) | 🟡 Medium | performance | Needs runtime verification |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-04\|BT-R04-04]] | Counters drift and several are computed wrongly | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-05\|BT-R04-05]] | Whole tables loaded for existence checks | 🟢 Low | performance | Confirmed (read in code) |

## Findings

### BT-R04-01
**Title:** Fifteen EAGER collections on the tenant root pull in the tenant graph
**Severity:** 🟠 High
**Category:** performance
**Principle:** Fetch plans belong to use cases, not to mappings — LAZY by default, fetch joins or entity graphs per query.
**Evidence:** All fifteen `@OneToMany` sets of `BusinessEntity` are `FetchType.EAGER`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessEntity.java:72-156`);
`bsStatusEntity.tasks` is EAGER (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsStatus/bsStatusEntity.java:33`),
as are `bsClientEntity.projects` and `.invoices` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsClient/bsClientEntity.java:53-60`).
Every tenant entity's `business` many-to-one uses the JPA default, EAGER
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:54-56`).
**Impact:** `GET /bs_pr_task/{id}` loads the task, its Business, and through the Business every user,
status (and through each status every task of the tenant), priority, type, project, document, KB entry and
invoice of the tenant. Cost grows with tenant size for every single-row read. With multiple EAGER sets
Hibernate either joins them (row explosion) or issues one select per collection — which one it picks per
query is runtime-dependent, but both scale with the tenant.
**Recommendation:** Make every association LAZY (including `@ManyToOne(fetch = LAZY)`), and fetch what a
screen needs with `@EntityGraph` or fetch-join queries. Replace `BusinessDTO`'s fifteen embedded sets with
counts or separate paged endpoints.
**Verified against:** N/A — JPA default fetch types; Hibernate 5.6 SQL shape not observed
**Confidence:** Confirmed (read in code)

### BT-R04-02
**Title:** N+1 queries in list views through `updateListFields`
**Severity:** 🟡 Medium
**Category:** performance
**Principle:** Aggregate in the database, not by walking collections.
**Evidence:** `getPageableListView` calls `updateListFields` on every entity of the page
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:125-141`), and the
overrides call `.size()` on lazy collections, e.g. seven per project
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsProject/bsProjectServiceImplements.java:90-103`);
`toListDTO` then calls other mappers on EAGER associations.
**Impact:** A page of 50 projects runs about 1 + 50 × 7 collection loads, each loading full child rows only to
count them.
**Recommendation:** Compute counts with one grouped query per child type for the page's ids
(`select p.id, count(t) … group by p.id`), or with `@Formula`/database views, and drop the per-row walk.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R04-03
**Title:** List views write to the database (write-on-read)
**Severity:** 🟡 Medium
**Category:** performance
**Principle:** Queries must not have side effects (command–query separation).
**Evidence:** The service class is `@Transactional` read-write (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:19`);
`updateListFields` sets counter fields on managed entities during `getPageableListView`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:125-141`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsStatus/bsStatusServiceImplements.java:99-106`).
Hibernate's dirty checking flushes changed fields at commit.
**Impact:** Whenever a stored counter differs from the live count, a `POST /page-list-view` issues one
`UPDATE` per changed row. Concurrent readers of the same page race on the same rows, and reads need write
locks and a writable database.
Runtime check: enable `spring.jpa.show-sql`, insert a task, then call `POST /bs_status/page-list-view` and
look for `update bs_status set … task_count=?`.
**Recommendation:** Mark list methods `@Transactional(readOnly = true)` and compute counts into the DTO,
not onto the entity; if counters must be stored, maintain them in the write path.
**Verified against:** spring-tx-5.3.20.jar (javap) — default `@Transactional` is read-write
**Confidence:** Needs runtime verification

### BT-R04-04
**Title:** Counters drift and several are computed wrongly
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Derived data must have one owner that keeps it consistent.
**Evidence:** Counters are refreshed only by `/page-list-view`, `updateListView` and Business insert
([[Docs/BugTracker/Explanations/04-Generic-CRUD-Framework]]); inserts and deletes of children never update
them. Copy-paste defects:
`setBsPriorityCount` is called twice in a row (a copy-paste duplicate)
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessServiceImplements.java:116-117`);
`BusinessListDTO.invoices` is filled from `getBsInvoiceCount()` (tenant invoices), not `getInvoiceCount()`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessMapper.java:252`);
`subCategoriesCount` is guarded by the wrong null check (`getBsDocsCount()`)
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryServiceImplements.java:128`);
`bsPrTaskEntity.channelCount` has no `updateListFields` and is always `null`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:86-87`).
**Impact:** `GET /{id}`, `GET ""`, `POST /page` and `POST /list` show stale counts; the Business grid shows
tenant invoice counts under the "invoices" column.
**Recommendation:** Compute counts at read time ([[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-02|BT-R04-02]])
and delete the columns, or maintain them in entity methods on add/remove.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R04-05
**Title:** Whole tables loaded for existence checks
**Severity:** 🟢 Low
**Category:** performance
**Principle:** Ask the database the question you mean (`exists`, `count`).
**Evidence:** `taskCatIsEmpty` copies `findAll()` into a `HashSet` to test emptiness — across all tenants
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsTaskCategory/bsTaskCategoryServiceImplements.java:128-131`);
the seeder calls `findAll().isEmpty()` on four tables (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/tools/firstInstallCheck.java:88-91`);
`MainHQ` is fetched with `findAll().get(0)` in four services (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/plan/PlanServiceImplements.java:56`).
**Impact:** Loads every row (and, for businesses, every EAGER collection) to answer a yes/no question. The
`is-empty` endpoint also answers for all tenants, not the caller's.
**Recommendation:** `count() == 0`, `existsBy…`, and a dedicated `findFirstBy…()` or a cached MainHQ id.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- All associations LAZY; per-screen `@EntityGraph` or fetch joins; no aggregate DTO that embeds a tenant.
- Read methods `@Transactional(readOnly = true)`, with no entity mutation.
- Counts as a projection query per page (`group by`) or maintained by entity methods in the write path —
  never recomputed on read and written back.
- `exists`/`count` queries for existence.

## Related Documents

- [[Docs/BugTracker/Explanations/08-Persistence-and-Transactions]]
- [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review]]
- [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review]]
