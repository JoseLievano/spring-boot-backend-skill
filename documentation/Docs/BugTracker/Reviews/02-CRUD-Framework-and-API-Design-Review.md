# CRUD Framework and API Design Review — BugTracker

#doc #review #ref-bugtracker #architecture #api-design

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/04-Generic-CRUD-Framework]]

## Scope

Reviewed: `shared/controller`, `shared/service`, `shared/mapper`, `shared/repository`, all 33 controllers,
the override pattern across the 31 services, the 31 mappers' `toEntity`, and the HTTP contract (routes,
status codes, response shapes). The filter engine is reviewed separately in
[[Docs/BugTracker/Reviews/05-Filter-Engine-Review]]. Excluded: per-module business rules
([[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review]]).

## Verdict

The generic stack does what it was built for: 31 modules get eight endpoints each with almost no controller
code, and the override points are clear. Two defects sit in the shared base and therefore affect most of
the API: `PUT` changes nothing in 22 of 31 services, and `POST` with an `id` in the body overwrites an existing
row. Judged with the depth diagnostic, the stack is deep at the controller layer and shallow everywhere
else — each entity pays for ten files, six type parameters and two ~100-line boilerplate classes (mapper and
predicate) whether it needs them or not. The HTTP contract is 2022-typical and inconsistent in small ways.

## Strengths to Keep

- One place defines the endpoint set and its semantics; a module controller is 15 lines
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskController.java:7-15`).
- Explicit override points (`insert`, `update`, `delete`, `updateListFields`) keep module code focused on
  domain rules (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:50-162`).
- Three response tiers match three UI needs — detail, reference, grid row
  ([[Docs/BugTracker/Explanations/05-DTO-Tiers-and-Mapping]]); `insert` returns a small body.
- Constructor injection of repository, mapper and predicate into the base service
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:27-31`).
- A paged, filterable list endpoint exists for every entity by default
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/controller/DefaultController.java:61-81`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-01\|BT-R02-01]] | Base `update` ignores the form (22 of 31 services) | 🔴 Critical | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-02\|BT-R02-02]] | A client-supplied `id` on `POST` overwrites an existing row | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-03\|BT-R02-03]] | Depth diagnostic: a ten-file, six-parameter tax per entity | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-04\|BT-R02-04]] | Deletion test: per-module predicates and mappers are shallow | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-05\|BT-R02-05]] | `GET ""` is unbounded and returns a `Set` | 🟡 Medium | api-design | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-06\|BT-R02-06]] | Inconsistent route naming | 🟢 Low | api-design | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-07\|BT-R02-07]] | Every success is `200`; create has no `Location` | 🟡 Medium | api-design | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-08\|BT-R02-08]] | No versioning, no OpenAPI, `Page` serialized directly | 🟢 Low | api-design | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-09\|BT-R02-09]] | `bsPrCommentController` departs from the pattern | 🟢 Low | api-design | Confirmed (read in code) |

## Findings

### BT-R02-01
**Title:** Base `update` ignores the form (22 of 31 services)
**Severity:** 🔴 Critical
**Category:** correctness
**Principle:** A default that silently does nothing is worse than no default — the base class's contract ("update") and implementation ("reload and save") disagree (Liskov: subclasses cannot rely on the base behavior).
**Evidence:** `update(ID id, FORM uform)` finds the entity, saves it unchanged and returns it; `uform` is never
read (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:55-65`).
From the module catalogue, 20 feature modules plus `user` and `country` do not override `update`:
`HQ/admin`, `HQ/employee`, `HQ/invoice`, `HQ/mainHQ`, `HQ/plan`, `bsClient`, `bsEmployee`,
`bsGeneralSettings`, `bsInvoice`, `bsManager`, all nine `project/*` modules (including `bsPrTask` and
`bsProject`), and `business`, whose override only calls `super`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessServiceImplements.java:96-99`).
**Impact:** `PUT /bs_pr_task/{id}` returns `200` with the old values: tasks cannot be moved between statuses,
renamed or completed through the API, and the same holds for projects, channels, users, settings and plans.
Clients see success and lose the edit. Core features of a project tracker do not work.
**Recommendation:** Make the base method abstract, or give `DefaultMapper` an `updateEntity(FORM, ENTITY)`
method that each mapper implements (null-skip semantics for PATCH, full replace for PUT), and call it in the
base `update`. Add one integration test per module that asserts a changed field is persisted.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01|BE-R02-01]] — the identical method, inherited through wpmanager.

### BT-R02-02
**Title:** A client-supplied `id` on `POST` overwrites an existing row
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Mass assignment of identity — the server, not the client, decides whether a write is a create.
**Evidence:** 29 of the 31 mappers copy `form.getId()` into the new entity, e.g. `.id(form.getId())`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskMapper.java:106-108`);
the exceptions are `UserMapper` and `MainHQMapper`. `insert` then calls `repository.save(...)`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:131`).
In spring-data-jpa 2.7.0, `SimpleJpaRepository.save` calls `persist` only when `isNew(entity)` (id `null`, no
version attribute) and `merge` otherwise (checked with `javap`).
**Impact:** `POST /bs_pr_task {"id": 42, …}` merges into task 42 instead of creating a task, replacing its
fields and references — including tasks of other tenants
([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02|BT-R01-02]]). With a non-existent id,
Hibernate inserts a new row with a generated id, so the bug is invisible in normal use.
**Recommendation:** Remove `id` from Forms (take it from the path on update), or ignore it in `toEntity`.
Add `@Version` to entities so `isNew` is based on the version field and stale merges fail.
**Verified against:** spring-data-jpa-2.7.0.jar (javap, `SimpleJpaRepository.save`)
**Confidence:** Confirmed (read in code)

### BT-R02-03
**Title:** Depth diagnostic: a ten-file, six-parameter tax per entity
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** Deep modules — interface cost must be small relative to the functionality hidden.
**Evidence:** Interface cost per module: six type parameters on the base service
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:20`), four mapping
methods, a predicate subclass and ten files, even for `bsGeneralSettings`, which overrides nothing and whose
predicate is 17 lines (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsGeneralSettings/bsGeneralSettingsPredicate.java`).
Leverage: the controller layer (8 endpoints for 15 lines) and paging/filter plumbing. Most real behavior —
validation, reference resolution, linking — is re-written by hand in each `insert`
([[Docs/BugTracker/Explanations/09-Service-Layer-Patterns]]).
**Impact:** 341 files for 32 entities. The framework is deep where the work is trivial (routing) and shallow
where it is hard (associations, validation, tenant scoping), so every module re-implements the hard parts,
with the inconsistencies documented in [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review]].
**Recommendation:** Keep the generic controller/service, but collapse the DTO tiers to what each entity
needs, generate mappers (MapStruct), and move association resolution and tenant assignment into the base
service (e.g. a declarative "reference resolver" keyed by Form field). The per-entity footprint should be
entity, form, response DTO, repository and a small service.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-06|BE-R02-06]]

### BT-R02-04
**Title:** Deletion test: per-module predicates and mappers are shallow
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** Deletion test — if deleting a module and inlining it removes no complexity, the module is a pass-through.
**Evidence:** The 31 predicates total 3,081 lines and the 31 mappers 3,184 lines. Each predicate method is the
same `for … switch (operation.getField())` over a Q-path, differing only in the path
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskPredicate.java:85-213`).
Each mapper copies fields by name and calls other mappers' `toSmallDTO`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskMapper.java:52-145`).
**Impact:** Deleting the predicates and replacing them with one generic association resolver (walk
`status.name` on a `PathBuilder`) would remove ~3,000 lines and no behavior. Mappers likewise encode no rule a
generator could not derive. The volume is where copy-paste bugs live
([[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-04|BT-R04-04]],
[[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-08|BT-R05-08]]).
**Recommendation:** Replace per-module predicates with a whitelist (field name → path, type, allowed
operators) as `backend/` later did, and generate mappers.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R02-05
**Title:** `GET ""` is unbounded and returns a `Set`
**Severity:** 🟡 Medium
**Category:** api-design
**Principle:** Bounded reads by default.
**Evidence:** `getAll` maps `repository.findAll()` to DTOs and collects into a `Set`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:41-48`); `POST /list`
also returns every row (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:79-87`).
`GET /business` maps fifteen collections per Business
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessMapper.java:110-205`).
**Impact:** Response size and load time grow with the table (and, for `/business`, with every tenant's
data). The `Set` loses ordering, and DTOs with `@Data` equality would collapse identical rows.
**Recommendation:** Remove unbounded list endpoints or cap them; return `List` with a stable sort; use the
paged endpoint for UI lists.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-03|BE-R02-03]]

### BT-R02-06
**Title:** Inconsistent route naming
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** Consistent, predictable resource names.
**Evidence:** Ten controllers omit the leading slash (`"bs_invoice"`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsInvoice/bsInvoiceController.java:8`);
`/mainHQ` is camelCase (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/mainHQ/MainHQController.java:9`)
while the rest are snake_case; resources are singular; extra endpoints mix kebab (`/update-order`) and snake
(`/bulk_add`).
**Impact:** Cosmetic — Spring normalizes the missing slash — but the front end must memorize exceptions.
**Recommendation:** Plural kebab-case resources with a leading slash (`/tasks`, `/task-categories`), one
style for sub-resources.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R02-07
**Title:** Every success is `200`; create has no `Location`
**Severity:** 🟡 Medium
**Category:** api-design
**Principle:** HTTP semantics as the contract.
**Evidence:** `insert`, `update`, `delete` all return `ResponseEntity.ok(...)`; `delete` returns the deleted
entity's DTO; `/page` without a body returns an empty `400`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/controller/DefaultController.java:35-81`).
**Impact:** Clients and caches cannot rely on status codes; the delete response serializes an entity that
no longer exists (lazy associations after deletion are fragile).
**Recommendation:** `201 Created` + `Location` for create, `204 No Content` for delete, and a documented
error body for `400`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-04|BE-R02-04]]

### BT-R02-08
**Title:** No versioning, no OpenAPI, `Page` serialized directly
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** Explicit, documented contract.
**Evidence:** No version prefix on any route, no springdoc dependency (`BugTracker/pom.xml:19-148`), and
`Page<DTO>` returned as the body (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/controller/DefaultController.java:61-81`).
**Impact:** Breaking changes cannot be staged; the paged JSON is Spring Data's internal `PageImpl` shape,
which Spring Data 3.3+ warns about.
**Recommendation:** `/api/v1` prefix, springdoc-openapi, and a small `PageResponse<T>` record.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Version note:** On Boot 3.3+ `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` gives a stable page shape.
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-05|BE-R02-05]], [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-08|BE-R02-08]]

### BT-R02-09
**Title:** `bsPrCommentController` departs from the pattern
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** One way to page.
**Evidence:** The same service bean is injected twice (`service`, `extraService`), paging uses path variables,
`size` is unbounded, and the response is a bare `List` without totals
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrComment/bsPrCommentController.java:16-27`).
**Impact:** A second paging contract to maintain; a client can request `size=1000000`.
**Recommendation:** Serve comments through the standard paged endpoint with a `channel.id` filter, or keep
the dedicated route but use `?page=&size=` with a maximum size and return the page metadata.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

```java
public abstract class CrudService<E, ID, F, R> {
    protected abstract E newEntity(F form);          // server assigns id
    protected abstract void apply(F form, E entity); // used by create and update
    protected abstract R toResponse(E entity);

    public R create(F form) { E e = newEntity(form); tenant.assign(e); apply(form, e); return toResponse(repo.save(e)); }
    public R update(ID id, F form) { E e = tenant.load(repo, id); apply(form, e); return toResponse(e); }
    public Page<R> list(QueryRequest q) { return repo.findAll(tenant.and(queryProfile.toPredicate(q)), q.pageable()).map(this::toResponse); }
}
```

- One response DTO plus an optional summary DTO per entity; MapStruct mappers.
- `201`/`204`/`404`/`409` status codes; `/api/v1` prefix; OpenAPI.
- No unbounded list endpoints; paging with a maximum size.

## Related Documents

- [[Docs/BugTracker/Explanations/04-Generic-CRUD-Framework]]
- [[Docs/BugTracker/Explanations/03-Package-Structure-and-Module-Anatomy]]
- [[Docs/BugTracker/Reviews/00-Review-Summary]]
- [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review]]
