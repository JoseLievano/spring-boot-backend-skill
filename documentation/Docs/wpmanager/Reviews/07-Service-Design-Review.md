# Service Design Review — wpmanager

#doc #review #ref-wpmanager #architecture

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/03-Package-Structure-and-Module-Anatomy]]

## Scope

Reviewed: every service and mapper under `models/`, their collaborators and cross-module dependencies, the
plugin/theme duplication (by a name-normalised diff of the two services), and the vault's open design notes.
Upload transaction boundaries are in [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]].

## Verdict

Services are transaction scripts over an anemic model: each override validates by hand, loads related rows
from other modules' repositories, updates both sides of every relation and saves several times. That style
works for the simpler modules (plan, category, author, favorite list are careful and correct). It fails where
code was copied: `ThemeService` is a near-copy of `PluginService` that lost two lines in its upload and now
never succeeds, and both services' delete paths throw on any item with two or more versions. Mappers carry
field-mapping bugs, and several input paths throw `NullPointerException`.

## Strengths to Keep

- Relation maintenance is explicit and symmetric, e.g. moving plugins between authors updates owning and
  inverse sides and verifies every id first
  (`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorService.java:155-191`).
- Deletes refuse to orphan dependents: authors with plugins, plans with clients, categories in use
  (`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorService.java:242-248`,
  `wpmanager/src/main/java/com/wpmanager/models/hq/plan/PlanService.java:129-133`,
  `wpmanager/src/main/java/com/wpmanager/models/downloads/pluginCategory/PluginCategoryService.java:68-71`).
- Diff-based collection updates (compute add/remove sets, then apply)
  (`wpmanager/src/main/java/com/wpmanager/models/hq/plan/PlanService.java:85-116`,
  `wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListService.java:147-188`).
- Entitlement checked where the limited resource is created (website limit per plan)
  (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:62-69`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-01\|WP-R07-01]] | Theme upload always fails | 🔴 Critical | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-02\|WP-R07-02]] | Deleting a plugin or theme with two or more versions throws `ConcurrentModificationException` | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-03\|WP-R07-03]] | Plugin and theme services are near-copies that have already diverged | 🟠 High | design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-04\|WP-R07-04]] | `PluginService` is a god class with unused collaborators | 🟡 Medium | design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-05\|WP-R07-05]] | Services reach into other modules; eleven `@Lazy` constructors hide mapper cycles | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-06\|WP-R07-06]] | Anemic model; invariants live in transaction scripts | 🟡 Medium | architecture | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-07\|WP-R07-07]] | Mapper bugs | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-08\|WP-R07-08]] | Null-pointer paths in client, admin and plan code | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-09\|WP-R07-09]] | Website URL handling: dead uniqueness check, `-` suffix, SSL flag | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-10\|WP-R07-10]] | Unknown category ids are silently dropped; theme categories have no API | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-11\|WP-R07-11]] | `ClientEntity.getBaseUser()` recurses forever | 🟡 Medium | correctness | Confirmed (read in code) |

## Findings

### WP-R07-01
**Title:** Theme upload always fails
**Severity:** 🔴 Critical
**Category:** correctness
**Principle:** —
**Evidence:** `ThemeService.upload` builds the version entity but never saves it, then passes its id:
```java
// wpmanager/src/main/java/com/wpmanager/models/downloads/theme/ThemeService.java:174-184
DownloadableEntity downloadable = DownloadableEntity.builder()
        .name(dwName)
        .version(form.getVersion())
        .checkSum(checksum)
        .filesSignature(fileSignature)
        .date(today)
        .build();
downloadable = storageProviderManager.uploadFileToDefaultProvider(
        downloadable.getId(),
        form.getFile()
);
```
The id is `null`, so the manager throws `InvalidInsertDetails("File or details are null")`
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:85-88`), which
the idempotency wrapper turns into a 500 ([[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01|WP-R04-01]]).
There is also no duplicate-version check (compare
`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:187-190`).
**Impact:** No theme version can be uploaded; `POST /theme/upload` always returns 500 and marks the file's key
`FAILED`. Half of the product catalog cannot receive files.
**Recommendation:** Extract one upload service used by both types (WP-R07-03) so the fix is made once: save the
version, check duplicates, upload, link.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R07-02
**Title:** Deleting a plugin or theme with two or more versions throws `ConcurrentModificationException`
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Do not mutate a collection while iterating it (directly or through a collaborator that shares it).
**Evidence:** `PluginService.delete` iterates the plugin's `downloadables` set and calls
`downloadableService.deleteDownloadable` for each (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:313-328`).
That method joins the same transaction, reloads the same plugin — the persistence context returns the same
managed instance — and removes the version from that same set
(`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:42-56`,
`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:72-82`). The set's iterator is
fail-fast, so the next `next()` throws. `ThemeService.delete` has the same structure
(`wpmanager/src/main/java/com/wpmanager/models/downloads/theme/ThemeService.java:290-305`,
`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:85-96`).
**Impact:** Any plugin or theme with two or more versions cannot be deleted; the call fails with an unmapped
500 after the first version's objects may already have been deleted from storage (storage deletes are not
transactional), leaving rows that point at missing files.
**Recommendation:** Iterate over a copy (`new ArrayList<>(toDelete.getDownloadables())`) or, better, let the
parent delete own the clean-up: collect versions, delete file rows and version rows in the transaction, and
delete objects after commit (`TransactionSynchronization.afterCommit`).
**Verified against:** N/A — Java collections and JPA persistence-context identity
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-04|BT-R06-04]]

### WP-R07-03
**Title:** Plugin and theme services are near-copies that have already diverged
**Severity:** 🟠 High
**Category:** design
**Principle:** DRY for knowledge, not text: one concept ("a versioned downloadable product") should have one implementation.
**Evidence:** A diff of the two services with `plugin`/`theme` normalised shows only renamed variables, message
texts and ordering, except in `upload`: the theme version never saves the entity and lacks the duplicate-version
check (WP-R07-01), uses `@Transactional` defaults instead of `rollbackFor`, and declares
`ItemNotFoundException` (`wpmanager/src/main/java/com/wpmanager/models/downloads/theme/ThemeService.java:145-190`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:158-207`). Controllers, entities,
mappers and forms are also paired copies. The vault records the duplication
(`wpmanager/wpManagerDocs/Bugs/to-do/Extract Duplicate Code Between Plugin and Theme Upload.md`).
**Impact:** Every fix must be made twice, and the one that was not is a Critical bug. A third product type
("kit" already appears as a parent type, `wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:46`)
would be a third copy.
**Recommendation:** Model the product once: an abstract `ProductEntity` (JOINED or single table with a `type`)
or a `ProductType` enum, one `ProductUploadService`, one version relation. Keep plugin- and theme-specific
fields in subclasses.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)

### WP-R07-04
**Title:** `PluginService` is a god class with unused collaborators
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Single responsibility; a constructor with 17 parameters signals several responsibilities.
**Evidence:** 17 constructor dependencies (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:56-74`)
serving catalog CRUD, category and author maintenance, upload orchestration and cascade deletes. Five are
never used: `pluginCategoryMapper`, `authorMapper`, `fileRepository`, `fileMapper`, `authUserUtil`
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:39-53`); four imports are unused
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:17-28`). `ThemeService` has 16, four
unused. The vault counts 13 and records it as open
(`wpmanager/wpManagerDocs/Bugs/to-do/God Class PluginService.md`,
`wpmanager/wpManagerDocs/Bugs/to-do/Single Responsibility Principle Violation.md`).
**Impact:** Hard to test in isolation (no unit tests exist, [[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-03|WP-R10-03]]);
every change risks unrelated behavior.
**Recommendation:** Split into `PluginCatalogService` (CRUD + categories + author), the shared upload service
from WP-R07-03, and a deletion service; remove unused dependencies.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)

### WP-R07-05
**Title:** Services reach into other modules; eleven `@Lazy` constructors hide mapper cycles
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** Module boundaries: talk to another module through its service, not its repository; cycles are a design smell, not something to annotate away.
**Evidence:** `PluginService` uses the category, author, downloadable and website repositories directly
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:129-153`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:298-312`); `FavoriteListService` uses the
client, plugin and theme repositories (`wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListService.java:34-62`).
Eleven mapper constructors are `@Lazy` to break cycles such as plugin → website → client → plan → client
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginMapper.java:21-32`,
`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientMapper.java:18-25`). `shared/tools/AuthUserUtil`
depends on two feature repositories (`wpmanager/src/main/java/com/wpmanager/shared/tools/AuthUserUtil.java:18-24`).
The vault flags package structure as open (`wpmanager/wpManagerDocs/Bugs/to-do/Package Structure Violations.md`).
**Impact:** Any module's invariants can be bypassed by another module writing its tables; the dependency graph
is cyclic, so modules cannot be extracted or tested alone; `@Lazy` proxies defer wiring errors to runtime.
**Recommendation:** Cross-module calls go through services with explicit methods (`categoryService.attach(plugin,
ids)`); mappers map one aggregate and take ids or mini DTOs for neighbours, which removes the cycles.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-05|BT-R10-05]], [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-07|BE-R09-07]]

### WP-R07-06
**Title:** Anemic model; invariants live in transaction scripts
**Severity:** 🟡 Medium
**Category:** architecture
**Principle:** Put behavior with the data it protects (information expert); keep counters derived or updated in one place.
**Evidence:** Entities have only accessors; services maintain both sides of relations and counters by hand, e.g.
`numberOfElements` incremented and decremented in eight places across plugin and theme code
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:149-153`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:266-277`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:305-311`). The vault names it
(`wpmanager/wpManagerDocs/Bugs/to-do/Anemic Domain Model.md`).
**Impact:** Every writer must remember every rule; a missed decrement (for example a failed delete after the
counter save) leaves the category undeletable (`numberOfElements > 0`).
**Recommendation:** Methods on the aggregate (`plugin.assignCategories(set)`, `author.adopt(plugin)`), and derive
counts with queries instead of stored counters.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-01|BE-R03-01]], [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-06|BT-R03-06]]

### WP-R07-07
**Title:** Mapper bugs
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** —
**Evidence:**
- `WebsiteMapper.toEntity` sets `url` from `getName()` (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteMapper.java:88`).
- `DownloadableMapper.toSmallDTO` sets `name` from `getVersion()` (`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableMapper.java:45`),
  so every version list shows the version twice and no name.
- `ClientMapper` is marked `//TODO Finish mapper` (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientMapper.java:14`);
  `apiKey` and `apiRateLimit` exist in `ClientDTO` but are never mapped
  (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientDTO.java:24-25`).
- `AdminMapper` never maps `username` although `AdminDTO` has it, and `toEntity` calls
  `adminForm.getRoles().isEmpty()` without a null check (`wpmanager/src/main/java/com/wpmanager/models/hq/admin/AdminMapper.java:12-61`,
  `wpmanager/src/main/java/com/wpmanager/models/hq/admin/AdminDTO.java:18`).
**Impact:** API responses carry wrong or missing fields; admin creation without `roles` fails with a 500.
**Recommendation:** Generate mappers (MapStruct with `unmappedTargetPolicy = ERROR`) or add a mapping test per
DTO that fails on unset fields.
**Verified against:** N/A — no library call in the current code
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03|BE-R04-03]]

### WP-R07-08
**Title:** Null-pointer paths in client, admin and plan code
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Validate input at the boundary.
**Evidence:** `ClientService.getOne` dereferences the plan of a client that may have none
(`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientService.java:58`; the relation is optional,
`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientEntity.java:37-39`). `ClientService.insert` concatenates the
names and then calls `getFirstName().isEmpty()` without a null check
(`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientService.java:69-73`). `PlanService.update` calls
`planForm.getName().isEmpty()` (`wpmanager/src/main/java/com/wpmanager/models/hq/plan/PlanService.java:77-78`).
`AdminMapper.toEntity` (WP-R07-07). `DownloadableService.deleteDownloadable` calls `parentType.equals(...)` on a
possibly null field (`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:45-46`).
**Impact:** `GET /client/{id}` for a client without a plan, a client insert without a first name, and a plan
update without a name all return an unmapped 500.
**Recommendation:** Bean Validation on forms (`@NotBlank`, `@NotNull`) with `@Valid` (already on the base
controller), and `Optional`-safe navigation for optional relations.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-02|BE-R04-02]]

### WP-R07-09
**Title:** Website URL handling: dead uniqueness check, `-` suffix, SSL flag
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Compare values in the same normal form you store.
**Evidence:** The service checks `existsByUrl(url.get())` with the normalised `https://…` or `http://…` value
(`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:70-78`), but stores the raw domain as the
URL (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:80-81`,
`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteMapper.java:88`), so the check never matches (the name
and domain checks catch duplicates instead). `URLValidator` returns `https://<domain>-` for an invalid
certificate (`wpmanager/src/main/java/com/wpmanager/shared/tools/URLValidator.java:36-37`), and the service sets
`sslCertificate = url.contains("https")`, so invalid certificates are recorded as valid
(`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:79`). The error log prints an
`Optional` (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:72`).
**Impact:** Stored data misstates certificate status; a sentinel suffix leaks into what should be a URL.
**Recommendation:** Have the validator return a small record (`normalizedUrl`, `sslStatus`), store and compare
the normalised URL, and add a unique constraint on it.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R07-10
**Title:** Unknown category ids are silently dropped; theme categories have no API
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Reject invalid references instead of ignoring them.
**Evidence:** Plugin and theme insert require a non-empty `categories` id set, then load all categories and keep
the matching ones, ignoring ids that do not exist (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:102-112`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:144-154`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/theme/ThemeService.java:131-141`). `ThemeCategoryService` exists
with admin rules, but no controller exposes it (`wpmanager/src/main/java/com/wpmanager/models/downloads/themeCategory`).
**Impact:** A theme can be created "with categories" that end up empty; theme categories can only be created
through the database or Spring Data REST. The whole category table is loaded for each insert.
**Recommendation:** `findAllById(ids)` and a 400 listing missing ids (as `FavoriteListService` and `AuthorService`
already do, `wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListService.java:147-152`); add a
`ThemeCategoryController` or remove the service.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R07-11
**Title:** `ClientEntity.getBaseUser()` recurses forever
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** —
**Evidence:** `public BaseUserEntity getBaseUser() { return this.getBaseUser(); }`
(`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientEntity.java:77-79`).
**Impact:** Dormant: nothing calls it today. Any caller (or a serializer that picks up the getter, such as Spring
Data REST exposing clients) gets a `StackOverflowError`.
**Recommendation:** Delete the method (a `ClientEntity` already is a `BaseUserEntity`).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-01|BE-R04-01]]

## Recommended Target Pattern

- One product abstraction with one upload/version service, so plugin, theme and any future type share the
  same code path and tests.
- Services talk to other modules through their services; mappers are acyclic and map one aggregate.
- Aggregates own their relation and counter rules; services orchestrate, validate at the boundary with Bean
  Validation, and delete through a single parent-owned path that defers storage deletes to after commit.
- Mappers are generated or covered by per-DTO tests.

## Related Documents

- [[Docs/wpmanager/Explanations/03-Package-Structure-and-Module-Anatomy]]
- [[Docs/wpmanager/Explanations/10-Customer-Domain]]
- [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]]
- [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review]]
