# CRUD Framework and API Design Review — backend

#doc #review #ref-backend #architecture #api-design

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/04-Generic-CRUD-Framework]]

## Scope

Reviewed: `shared/defaultImplements/*`, `shared/defaultInterfaces/*`, how `AdminServiceImpl`,
`ClientService`, `AdminController` and `ClientController` use them, and the resulting HTTP API.
Excluded: the list/query subsystem (see [[Docs/backend/Reviews/06-Query-Engine-Review]]) and
authorization (see [[Docs/backend/Reviews/01-Security-Review]]).

## Verdict

The framework achieves its main goal: feature controllers are empty and a new resource costs a handful
of small classes. Its interface, however, is wide and leaky. Six type parameters and five checked
exceptions appear on every signature, the base `update` silently does nothing, and features must downcast
the repository to reach their own finders. The HTTP surface is uniform but not idiomatic REST (200 for
everything, unbounded `getAll`, no versioning or OpenAPI). The idea is worth keeping for the skill; the
implementation needs a smaller, honest interface.

## Strengths to Keep

- Endpoints written once; feature controllers are pure type bindings
  (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminController.java:7-14`).
- Entities never cross the controller boundary; dedicated DTOs per use
  (`backend/src/main/java/com/agentForgeBackend/shared/defaultInterfaces/DefaultMapper.java:3-14`).
- `@Valid` on every request body at the framework level
  (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultController.java:36`,
  `backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultController.java:42`,
  `backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultController.java:48`).
- One repository base (`JpaRepository` + `QuerydslPredicateExecutor`) marked `@NoRepositoryBean`
  (`backend/src/main/java/com/agentForgeBackend/shared/defaultInterfaces/DefaultRepository.java:7-8`).
- Transactions declared once at the base class, with a read-only list query
  (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:24-30`,
  `backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:72`).
- Constructor injection throughout; collaborators are `final`
  (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:34-51`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01\|BE-R02-01]] | Base `update` ignores the form | 🔴 Critical | correctness | Confirmed (read in code) |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-02\|BE-R02-02]] | Repository and service downcasts | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-03\|BE-R02-03]] | `getAll` is unbounded and returns a `Set` | 🟡 Medium | performance | Confirmed (read in code) |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-04\|BE-R02-04]] | Every success is `200`; create has no `Location`, delete returns a body | 🟡 Medium | api-design | Confirmed (read in code) |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-05\|BE-R02-05]] | No API versioning, no OpenAPI description | 🟢 Low | api-design | Confirmed (read in code) |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-06\|BE-R02-06]] | Six type parameters cost more than they buy | 🟡 Medium | design | Confirmed (read in code) |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-07\|BE-R02-07]] | Checked exceptions leak into every generic signature | 🟡 Medium | design | Confirmed (read in code) |
| [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-08\|BE-R02-08]] | `Page` serialized directly; JSON shape unstable | 🟢 Low | api-design | Confirmed (test/runtime evidence) |

## Findings

### BE-R02-01
**Title:** Base `update` ignores the form
**Severity:** 🔴 Critical
**Category:** correctness
**Principle:** Interface contract violation — a pass-through method whose name promises behavior it does not have; the most dangerous kind of shallow module.
**Evidence:**
`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:88-94`
```java
public DTO update(ID id, FORM form) throws ItemNotFoundException, InvalidInsertDetails{
    ENTITY toUpdate = repository.findById(id).orElseThrow(ItemNotFoundException::new);
    repository.save(toUpdate);
    return mapper.toDTO(toUpdate);
}
```
`AdminServiceImpl` does not override it (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java:17-68`).
**Impact:** `PUT /admin/{id}` returns `200` with the unchanged admin. Callers believe the update succeeded; the data is silently discarded. Every future feature that relies on the base method inherits the bug.
**Recommendation:** Give the mapper an explicit update operation and make the base method use it, or make `update` abstract so each feature must decide:
```java
public interface DefaultMapper<DTO, MINIDTO, LISTDTO, FORM, ENTITY> {
    void updateEntity(FORM form, ENTITY target);      // new
    // …
}
public DTO update(ID id, FORM form) throws ItemNotFoundException {
    ENTITY entity = repository.findById(id).orElseThrow(ItemNotFoundException::new);
    mapper.updateEntity(form, entity);                 // dirty checking persists it
    return mapper.toDTO(entity);
}
```
Add a service test per feature asserting that a changed field is persisted.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R02-02
**Title:** Repository and service downcasts
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** DIP/LSP leak at the seam — the base class stores the abstraction, subclasses need the concretion, so they cast.
**Evidence:** `(AdminRepository) this.repository` (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java:44`, `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java:64`); `(ClientRepository) repository` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:80`, `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:120`, `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:163`); `((ClientService)defaultService)` in the controller (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientController.java:25`).
**Impact:** Type errors move from compile time to run time; every feature repeats the cast; wiring a different repository implementation (e.g. a test double) breaks with `ClassCastException`.
**Recommendation:** Keep a typed field in the subclass, or add a repository type parameter to the base class:
```java
public abstract class DefaultServiceImplements<…, ENTITY, ID, REPO extends DefaultRepository<ENTITY, ID>> {
    protected final REPO repository;
}
@Service class ClientService extends DefaultServiceImplements<…, ClientEntity, Long, ClientRepository> { }
```
In the controller, keep the concrete service in its own field instead of casting `defaultService`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R02-03
**Title:** `getAll` is unbounded and returns a `Set`
**Severity:** 🟡 Medium
**Category:** performance
**Principle:** Bounded responses; do not let collection semantics depend on DTO `equals`.
**Evidence:** `repository.findAll().stream().map(mapper::toDTO).collect(Collectors.toSet())` (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:61-68`), exposed as `GET /<feature>` (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultController.java:30-33`). `AdminDTO` has no `id` and Lombok `@Data` equality (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminDTO.java:7-19`).
**Impact:** The whole table is loaded and serialized in one request; order is undefined; two rows whose DTOs are field-equal collapse into one element.
**Recommendation:** Remove `getAll` from the generic contract (the paginated list already covers it) or make it return `Page<LISTDTO>` with the profile's default sort and a size cap.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R02-04
**Title:** Every success is `200`; create has no `Location`, delete returns a body
**Severity:** 🟡 Medium
**Category:** api-design
**Principle:** Use HTTP semantics as the contract (RFC 9110): `201 Created` + `Location`, `204 No Content`.
**Evidence:** `ResponseEntity.ok(...)` for insert, update and delete (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultController.java:41-57`).
**Impact:** Clients cannot distinguish creation from other successes or discover the new resource's URL; delete responses carry a payload that must be mapped from an entity that no longer exists.
**Recommendation:** `return ResponseEntity.created(URI.create(basePath + "/" + id)).body(mini);` for create and `ResponseEntity.noContent().build()` for delete. The mini DTO needs an `id` for that.
**Verified against:** `spring-web-6.2.1.jar` (javap: `ResponseEntity.created(URI)`, `ResponseEntity.noContent()`) — the version Boot 3.4.1 resolves.
**Confidence:** Confirmed (read in code)

### BE-R02-05
**Title:** No API versioning, no OpenAPI description
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** Explicit, documented contract.
**Evidence:** Paths are bare `/admin`, `/client`, `/login` (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminController.java:8`, `backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityController.java:34`); no springdoc or other OpenAPI dependency (`backend/pom.xml:35-169`).
**Impact:** Breaking changes cannot coexist with old clients; consumers learn the API from source code.
**Recommendation:** Prefix routes with `/api/v1` from the start (one constant) and add `springdoc-openapi-starter-webmvc-ui` so the generic controller's endpoints are documented per feature.
**Verified against:** N/A — no library API involved (springdoc not adopted; version choice left to the implementer)
**Confidence:** Confirmed (read in code)

### BE-R02-06
**Title:** Six type parameters cost more than they buy
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Depth = leverage ÷ interface cost. The framework has real leverage (six endpoints for free) but its interface asks every feature for four data shapes, even when they are identical.
**Evidence:** `DefaultServiceImplements<DTO, MINIDTO, LISTDTO, FORM, ENTITY, ID>` (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:31-32`). `AdminDTO` and `AdminMiniDTO` are field-for-field the same (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminDTO.java:13-19`, `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminMiniDTO.java:13-19`); the base constructor takes five collaborators (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:40-44`).
**Impact:** Each feature writes and maintains near-duplicate classes and mapper methods; the duplication already caused the mapper gaps in [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03|BE-R04-03]]. Generic signatures become hard to read and to change.
**Recommendation:** Keep three shapes — `Request` (form), `Response` (detail, includes `id`), `Summary` (list row) — and drop `MINIDTO` (create returns the detail response). Group the query collaborators behind one `ListQuery<ENTITY>` component so the base constructor takes three arguments.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R02-07
**Title:** Checked exceptions leak into every generic signature
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Information hiding — callers must know every failure type of every implementation; OCP — a new failure type changes the interface.
**Evidence:** `DefaultService` declares different checked-exception sets per method (`backend/src/main/java/com/agentForgeBackend/shared/defaultInterfaces/DefaultService.java:15-25`); the base class must repeat all five in `rollbackFor` (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:24-30`), and `ClientService.update` re-declares a shorter list (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:108`). `ItemNotFoundException` is declared on `insert`, which never throws it (`backend/src/main/java/com/agentForgeBackend/shared/defaultInterfaces/DefaultService.java:19`).
**Impact:** Adding a new domain failure means editing the interface, both base classes, every override and the `rollbackFor` lists; forgetting the last one commits a transaction that should roll back.
**Recommendation:** Use one unchecked hierarchy (`abstract class DomainException extends RuntimeException` carrying an HTTP status or error code). Spring rolls back on unchecked exceptions by default, so all `rollbackFor` lists disappear. See [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-01|BE-R05-01]].
**Verified against:** N/A — Spring's default rollback-on-unchecked rule is long-standing framework behavior, not version-specific
**Confidence:** Confirmed (read in code)

### BE-R02-08
**Title:** `Page` serialized directly; JSON shape unstable
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** Own your wire format.
**Evidence:** `ResponseEntity<Page<LISTDTO>>` (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultController.java:35-39`). The test run logs Spring Data's warning "Serializing PageImpl instances as-is is not supported, meaning that there is no guarantee about the stability of the resulting JSON structure!" (`backend/target/surefire-reports/TEST-com.agentForgeBackend.models.hq.admin.AdminControllerListEndpointTest.xml:1266`).
**Impact:** Upgrading Spring Data can change the list response (`pageable`, `sort` sub-objects) and break clients.
**Recommendation:** Enable `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` or return a project-owned `PageResponse<T>(content, page, size, totalElements, totalPages)`.
**Verified against:** `spring-data-commons-3.4.1.jar` (javap: `EnableSpringDataWebSupport.pageSerializationMode()`, `PageSerializationMode.DIRECT` / `VIA_DTO`; `org.springframework.data.web.PagedModel` present)
**Confidence:** Confirmed (test/runtime evidence)

## Recommended Target Pattern

```java
public interface CrudMapper<REQ, RES, SUM, E> {
    E toEntity(REQ req);
    void update(REQ req, E target);
    RES toResponse(E e);
    SUM toSummary(E e);
}

public abstract class CrudService<REQ, RES, SUM, E, ID, R extends DefaultRepository<E, ID>> {
    protected final R repository;                        // typed: no downcasts
    protected final CrudMapper<REQ, RES, SUM, E> mapper;
    protected final ListQuery<E> listQuery;               // profile + predicate + pageable
    @Transactional public RES create(REQ req)            { return mapper.toResponse(repository.save(mapper.toEntity(req))); }
    @Transactional public RES update(ID id, REQ req)     { E e = find(id); mapper.update(req, e); return mapper.toResponse(e); }
    @Transactional public void delete(ID id)             { repository.delete(find(id)); }
    @Transactional(readOnly = true) public PageResponse<SUM> list(PageableRequest q) { … }
    protected E find(ID id) { return repository.findById(id).orElseThrow(() -> new NotFoundException(…)); }
}
```

Controllers: `201 Created` + `Location` on create, `204` on delete, versioned base path, OpenAPI via
springdoc, unchecked domain exceptions mapped centrally.

## Related Documents

- [[Docs/backend/Explanations/04-Generic-CRUD-Framework]]
- [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review]]
- [[Docs/backend/Reviews/05-Error-Handling-Review]]
- [[Docs/backend/Reviews/06-Query-Engine-Review]]
