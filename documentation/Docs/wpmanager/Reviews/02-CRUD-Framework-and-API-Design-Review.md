# CRUD Framework and API Design Review — wpmanager

#doc #review #ref-wpmanager #api-design #architecture

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/04-Generic-CRUD-Framework]]

## Scope

Reviewed: `shared/defaultImplements/*`, `shared/defaultInterfaces/*`, every controller and its route, the
override catalogue of all twelve generic services, and the HTTP contract (status codes, bodies, collections).
Authorization of inherited operations is covered in
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-03|WP-R01-03]]; error bodies in
[[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review]].

## Verdict

The generic stack does its main job: a module with ordinary rules needs only type bindings, and the four
type parameters are fewer than BugTracker's or `backend/`'s. But the base class supplies correct behavior for
reads and delete only. Its `update` silently does nothing, which leaves three modules without a working
update, and eleven of twelve services replace `insert`. The HTTP contract is minimal — no pagination, 200 for
everything, an unbounded `Set` for lists — and two modules expose conflicting delete routes.

## Strengths to Keep

- Four type parameters on the controller (`DTO, MINIDTO, FORM, ID`), the leanest of the three generations
  (`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultController.java:14`).
- A class-level transaction that rolls back on all four checked domain exceptions, inherited by every module
  (`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java:16-21`).
- A secure-by-annotation default on every base method (`isAuthenticated()`), even if too weak
  (`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java:34-72`).
- Typed-field alternative to casts in the upload controllers
  (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginController.java:18-31`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-01\|WP-R02-01]] | Base `update` ignores the form (admin, S3 provider, downloadable) | 🔴 Critical | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-02\|WP-R02-02]] | Repository and service downcasts | 🟡 Medium | design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-03\|WP-R02-03]] | `getAll` is unbounded and returns a `Set`; no pagination anywhere | 🟡 Medium | api-design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-04\|WP-R02-04]] | Every success is 200; delete returns a body | 🟡 Medium | api-design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-05\|WP-R02-05]] | Two delete routes for downloadables; the inherited one skips storage cleanup | 🟡 Medium | api-design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-06\|WP-R02-06]] | Depth diagnostic: the base service is shallow and mostly overridden | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-07\|WP-R02-07]] | Inconsistent route naming | 🟢 Low | api-design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-08\|WP-R02-08]] | No API versioning, no OpenAPI description | 🟢 Low | api-design | Confirmed (read in code) |

## Findings

### WP-R02-01
**Title:** Base `update` ignores the form (admin, S3 provider, downloadable)
**Severity:** 🔴 Critical
**Category:** correctness
**Principle:** A base class must not offer an operation it cannot perform (LSP); a no-op that reports success is worse than no operation.
**Evidence:** The base loads the entity and saves it unchanged
(`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java:58-64`).
`AdminServiceImpl`, `S3ProviderService` and `DownloadableService` do not override `update`
(`wpmanager/src/main/java/com/wpmanager/models/hq/admin/AdminServiceImpl.java:14-63`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderService.java:15-64`,
`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:20-97`).
**Impact:** `PUT /admin/{id}` cannot change an admin's e-mail, name or password; `PUT /s3/{id}` cannot rotate a
provider's keys, switch the default provider or fix a bucket name; each call returns 200 with the unchanged
entity. Any key rotation (see [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-06|WP-R01-06]]) therefore needs a
delete and re-create of the provider, which drops its file map.
**Recommendation:** Make `update` abstract (or apply the form through `mapper.updateEntity(form, entity)`, a
fourth mapper method), and return 405 for modules that do not support update.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01|BE-R02-01]], [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-01|BT-R02-01]]

### WP-R02-02
**Title:** Repository and service downcasts
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Dependency inversion and LSP: a subclass that needs a narrower type should receive it, not cast to it.
**Evidence:** `((PluginRepository) repository)` (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:113`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:118`), `(ClientRepository) repository`
(`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientService.java:87`),
`((WebsiteRepository)repository)` three times (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:75-86`),
`((ClientService)defaultService)` and `(DownloadableService) this.defaultService` in controllers
(`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientController.java:26`,
`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableController.java:22`).
**Impact:** The compiler cannot catch a wrong binding; each module repeats the cast and the reader must know
which concrete type is behind the field.
**Recommendation:** Add a `REPO extends DefaultRepository<ENTITY, ID>` type parameter (or have subclasses keep a
typed field passed to `super`), as the upload controllers already do for their services.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-02|BE-R02-02]]

### WP-R02-03
**Title:** `getAll` is unbounded and returns a `Set`; no pagination anywhere
**Severity:** 🟡 Medium
**Category:** api-design
**Principle:** Bounded responses; stable ordering.
**Evidence:** `repository.findAll()` mapped into `Collectors.toSet()`
(`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java:42-49`). No endpoint takes
page, size, sort or filter parameters; there is no list DTO (the `LISTDTO` parameter was dropped).
**Impact:** Lists return every row in unspecified order, and `toDTO` walks relations for each (a plugin's
categories, websites and versions), so cost grows with the catalog. DTOs with equal fields collapse in the
`Set`.
**Recommendation:** Return `Page<MiniDTO>` (or a page envelope) from `GET ""` with bounded `size` and a default
sort; the whitelisted query engine in `backend/` is the candidate
([[Docs/backend/Explanations/05-Dynamic-List-Query-Engine]]).
**Verified against:** N/A — design recommendation, no version-specific API
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-03|BE-R02-03]], [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-05|BT-R02-05]]

### WP-R02-04
**Title:** Every success is 200; delete returns a body
**Severity:** 🟡 Medium
**Category:** api-design
**Principle:** Use HTTP semantics: 201 + `Location` on create, 204 on delete.
**Evidence:** All handlers return `ResponseEntity.ok(...)`; delete returns the DTO of the deleted entity
(`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultController.java:32-48`); upload returns 200
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginController.java:71-72`).
**Impact:** Clients cannot tell creation from replay (idempotent upload returns 200 in both cases) and must
parse bodies to find new ids.
**Recommendation:** `201 Created` with `Location: /<route>/{id}` for insert and for a new upload, `200` for an
idempotent replay (or `Idempotency-Replayed: true`), `204 No Content` for delete.
**Verified against:** N/A — HTTP semantics
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-04|BE-R02-04]]

### WP-R02-05
**Title:** Two delete routes for downloadables; the inherited one skips storage cleanup
**Severity:** 🟡 Medium
**Category:** api-design
**Principle:** One way to perform an operation; a generic default must not bypass domain rules.
**Evidence:** `DownloadableController` adds `DELETE /downloadable` with a request body, which unlinks the version
from its parent and deletes its objects in every provider
(`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableController.java:20-24`,
`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:42-70`). The inherited
`DELETE /downloadable/{id}` remains, needs only a login, and calls `repository.delete` directly
(`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java:66-72`).
**Impact:** The inherited route either fails with a foreign-key error when file rows exist (surfacing as an
unhandled 500) or, for a version without files, deletes it without touching storage or the parent. `DELETE`
with a body is also poorly supported by some HTTP clients and proxies.
**Recommendation:** Override `delete(id)` in `DownloadableService` to do the full clean-up (the parent can be
found from the version), and remove the body-based route; or return 405 from the base route.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R02-06
**Title:** Depth diagnostic: the base service is shallow and mostly overridden
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** Deep modules: the interface should be small relative to the behavior it hides. Deletion test: removing the base `insert`/`update` would change almost nothing.
**Evidence:** Of 60 method slots (12 services × 5 methods), 29 are overridden: `insert` in 11 of 12 services,
`update` in 9 of 12 (the other 3 are the no-op of WP-R02-01), `delete` in 6
([[Docs/wpmanager/Explanations/04-Generic-CRUD-Framework]]). What the base still provides is `findById → map`,
`findAll → map` and `findById → delete → map`. Every signature carries four checked exceptions
(`wpmanager/src/main/java/com/wpmanager/shared/defaultInterfaces/DefaultService.java:10-22`), and every module needs
eight files. Compared with `backend/`, which kept the same base and added a list engine, wpmanager's version is
the thinnest of the three generations.
**Impact:** The abstraction costs five type bindings, eight files and a checked-exception contract per module,
but saves little logic; the parts that matter (validation, relations, authorization) live in overrides with
no shared structure, which is how the plugin and theme services drifted apart
([[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-03|WP-R07-03]]).
**Recommendation:** Keep a generic read/delete base, but move write rules into explicit hooks
(`validateCreate(form)`, `applyRelations(entity, form)`, `authorize(action, entity)`) so modules supply rules
without replacing the method; replace checked exceptions with one runtime `DomainException` hierarchy mapped
to problem details.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-06|BE-R02-06]], [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-07|BE-R02-07]], [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-03|BT-R02-03]]

### WP-R02-07
**Title:** Inconsistent route naming
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** Predictable resource naming.
**Evidence:** `@RequestMapping("downloadable")` without a leading slash
(`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableController.java:13`), `"/fav_list"`
(snake case, `wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListController.java:8`),
`"/plugin-category"` (kebab case, `wpmanager/src/main/java/com/wpmanager/models/downloads/pluginCategory/PluginCategoryController.java:8`),
`"/s3"` for a provider type rather than a resource
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderController.java:8`), singular nouns elsewhere.
**Impact:** Clients must learn each route; the missing slash works only because Spring normalizes it.
**Recommendation:** Plural kebab-case resources under one prefix (`/api/v1/plugins`, `/api/v1/plugin-categories`,
`/api/v1/storage-providers`, `/api/v1/favorite-lists`).
**Verified against:** N/A — naming convention
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-06|BT-R02-06]]

### WP-R02-08
**Title:** No API versioning, no OpenAPI description
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** A published contract.
**Evidence:** No version prefix on any `@RequestMapping` and no springdoc or OpenAPI dependency
(`wpmanager/pom.xml:34-163`).
**Impact:** The React front end implied by the CORS origin has no machine-readable contract; breaking changes
cannot be staged.
**Recommendation:** Add `springdoc-openapi-starter-webmvc-ui` and a `/api/v1` prefix.
**Verified against:** N/A — no library call in the current code
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-05|BE-R02-05]]

## Recommended Target Pattern

A generic module base that is deep where it is generic and explicit where it is not:

```java
public abstract class CrudService<E, ID, FORM, DTO, SUMMARY> {
    protected abstract JpaRepository<E, ID> repo();
    protected abstract E create(FORM form);            // required: no silent default
    protected abstract void apply(FORM form, E entity); // required: update is never a no-op
    @PreAuthorize("hasRole('ADMIN')")                  // deny by default; modules loosen deliberately
    public Page<SUMMARY> list(Pageable p) { … }
    …
}
```

Controllers return `201` + `Location`, `204`, and `Page` envelopes; routes are versioned plural nouns; there is
exactly one delete path per resource, and it performs the domain clean-up.

## Related Documents

- [[Docs/wpmanager/Explanations/04-Generic-CRUD-Framework]]
- [[Docs/wpmanager/Reviews/01-Security-Review]]
- [[Docs/wpmanager/Reviews/07-Service-Design-Review]]
- [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review]]
