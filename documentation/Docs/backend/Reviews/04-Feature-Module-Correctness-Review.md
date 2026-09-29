# Feature Module Correctness Review — backend

#doc #review #ref-backend #architecture

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/03-Package-Structure-and-Module-Anatomy]]

## Scope

Reviewed line by line: all 20 files in `models/hq/admin/` and `models/hq/client/`. Framework-level
defects that these modules inherit (the no-op `update`, the downcasts) are reviewed where their root
cause lives, in [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review]], and only linked here.

## Verdict

The two modules follow the ten-file convention faithfully, and their list paths (profile, list DTO,
`toListDTO`) are correct and tested. The hand-written parts around create/read are not: one entity method
recurses forever, two input paths throw `NullPointerException` on missing fields, and the mappers
populate different subsets of fields for the three DTOs of the same feature. These are the bugs that
appear when every feature re-implements mapping and validation by hand and only the list endpoint has
tests.

## Strengths to Keep

- Feature controllers are almost empty and bind types only
  (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminController.java:7-14`).
- List mapping is complete and null-safe in both mappers
  (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminMapper.java:38-58`,
  `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientMapper.java:47-67`).
- Services check uniqueness before saving and return 409 on conflicts at create time
  (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:80-92`).
- `AdminServiceImpl.insert` ignores roles sent by the caller and forces `ADMIN`
  (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java:52-54`), so mass
  assignment of roles is not possible through this path.
- `ClientService.update` performs a proper partial update with per-field uniqueness checks
  (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:122-151`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-01\|BE-R04-01]] | `ClientEntity.getBaseUser()` recurses forever | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-02\|BE-R04-02]] | `NullPointerException` on missing input fields | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03\|BE-R04-03]] | Mappers fill different field subsets per DTO | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-04\|BE-R04-04]] | `apikey` is `Long` on the entity, `String` in DTOs, and never set | 🟢 Low | correctness | Confirmed (read in code) |
| [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-05\|BE-R04-05]] | Same conflict returns 409 on create but 400 on update | 🟢 Low | api-design | Confirmed (read in code) |

## Findings

### BE-R04-01
**Title:** `ClientEntity.getBaseUser()` recurses forever
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** —
**Evidence:**
`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientEntity.java:33-35`
```java
public BaseUserEntity getBaseUser() {
    return this.getBaseUser();
}
```
**Impact:** Dormant today — nothing in `src/main` calls it. Any future caller gets a `StackOverflowError`. Because it is a JavaBean getter, any Jackson serialization of a `ClientEntity` (for example by Spring Data REST, [[Docs/backend/Reviews/01-Security-Review#BE-R01-03|BE-R01-03]]) will call it and fail.
**Recommendation:** Delete the method (a `ClientEntity` *is* a `BaseUserEntity`), or `return this;` if an explicit upcast accessor is wanted.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R04-02
**Title:** `NullPointerException` on missing input fields
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Validate at the boundary (see [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-07|BE-R03-07]]).
**Evidence:** `AdminMapper.toEntity` calls `adminForm.getRoles().isEmpty()` without a null check (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminMapper.java:76`), although `AdminServiceImpl.insert` overwrites the roles anyway (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java:52-54`). `ClientService.insert` calls `getFirstName().isEmpty()` / `getLastName().isEmpty()` after string concatenation that tolerates `null` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:66-67`).
**Impact:** `POST /admin` without `roles`, or `POST /client` without a first or last name, returns a 500 with Spring Boot's default error body instead of a 400 with `ErrorHTTPRes`.
**Recommendation:** Declare `@NotBlank` on the form fields and drop the manual checks; in mappers, treat collections as optional (`Optional.ofNullable(form.getRoles()).orElse(Set.of())`) or remove fields the service ignores from the form.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R04-03
**Title:** Mappers fill different field subsets per DTO
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** DRY at the knowledge level — one feature's "what a user looks like on the wire" is written three times and has drifted.
**Evidence:**

| Mapper method | Missing fields that the target DTO declares | Evidence |
|---|---|---|
| `AdminMapper.toDTO` | `username` | `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminMapper.java:12-23`, `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminDTO.java:17` |
| `AdminMapper.toSmallDTO` | `username` | `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminMapper.java:25-36`, `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminMiniDTO.java:17` |
| `ClientMapper.toDTO` | `apikey` | `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientMapper.java:17-32`, `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientDTO.java:20` |
| `ClientMapper.toSmallDTO` | `roles`, `apiKey` | `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientMapper.java:34-45`, `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientMiniDTO.java:18-19` |

Neither Admin DTO nor MiniDTO has an `id` field at all (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminDTO.java:13-19`).
**Impact:** `GET /admin/{id}` returns `"username": null`; a created admin cannot be addressed by the response (no id); clients see different shapes for the same resource depending on the endpoint.
**Recommendation:** Collapse to one detail response per feature that always includes `id` (see [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-06|BE-R02-06]]), and generate mappers with MapStruct (`unmappedTargetPolicy = ERROR`) so a missing field fails the build — or keep hand-written mappers but add one round-trip test per DTO.
**Verified against:** N/A — no library API involved (MapStruct is not a project dependency; its adoption is a suggestion)
**Confidence:** Confirmed (read in code)

### BE-R04-04
**Title:** `apikey` is `Long` on the entity, `String` in DTOs, and never set
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** One name and one type per concept.
**Evidence:** `private Long apikey` with a unique column (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientEntity.java:19-20`); `private String apikey` in `ClientDTO` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientDTO.java:20`); `private String apiKey` (different casing) in `ClientMiniDTO` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientMiniDTO.java:19`). No code in `src/main` assigns it.
**Impact:** A half-finished feature: the column exists (unique, nullable) but carries no meaning; the DTO fields are always null.
**Recommendation:** Either delete the field or implement API keys properly (random, hashed, stored as `String`/`bytea`, see [[Docs/backend/Reviews/01-Security-Review#BE-R01-06|BE-R01-06]]) with one consistent name.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R04-05
**Title:** Same conflict returns 409 on create but 400 on update
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** Same condition, same error.
**Evidence:** Duplicate username/e-mail throws `ItemAlreadyExist` (409) in `insert` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:82-91`) but `InvalidInsertDetails` (400) in `update` (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:125-129`, `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:137-141`). The `update` signature cannot throw `ItemAlreadyExist` because the generic interface does not declare it (`backend/src/main/java/com/agentForgeBackend/shared/defaultInterfaces/DefaultService.java:21`).
**Impact:** Clients must handle one conflict two ways. The root cause is the checked-exception interface ([[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-07|BE-R02-07]]).
**Recommendation:** Throw a conflict exception from both paths; with an unchecked hierarchy the interface no longer constrains it.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

A feature module should contain the entity, the repository, **one** request form with all Bean
Validation annotations, **one** detail response (with `id`) and **one** summary row, a mapper that is
either generated with strict unmapped-target checking or covered by a round-trip test, a query profile,
the service (only the behavior that differs from the base) and a thin controller. Input validity is
decided by annotations on the form; the service decides business rules (uniqueness, roles) and throws
unchecked domain exceptions that map to one status per condition.

## Related Documents

- [[Docs/backend/Explanations/03-Package-Structure-and-Module-Anatomy]]
- [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review]]
- [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review]]
