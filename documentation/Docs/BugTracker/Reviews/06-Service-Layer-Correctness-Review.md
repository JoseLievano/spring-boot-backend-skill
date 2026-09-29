# Service Layer Correctness Review — BugTracker

#doc #review #ref-bugtracker #architecture

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/09-Service-Layer-Patterns]], [[Docs/BugTracker/Explanations/11-Collaboration-Features]]

## Scope

Reviewed: every overridden method in the 31 `*ServiceImplements` classes, `MentionGenerator`, and the
`firstInstallCheck` seeder, read line by line against the domain rules they implement. Cross-cutting issues
with their own root cause (no-op `update`, tenant isolation, transactions) are reviewed elsewhere and only
linked here.

## Verdict

The services implement a lot of real domain behavior — reference checks, uniqueness, ordering, trees,
defaults — in a consistent style. Correctness, however, depends on each service author remembering every
rule, and many did not: uniqueness is global where it must be per tenant, priority reordering touches every
tenant, two core operations (channel creation with members, deleting a task category used by several
types) throw at runtime, and exception types are sometimes the wrong ones. All findings are **defects** on
Boot 2.7; none is an era gap.

## Strengths to Keep

- Every referenced id is resolved and a missing reference is reported with a specific message
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:75-97`).
- Delete guards protect referential integrity with a clear message ("Can't delete a status with task linked")
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsStatus/bsStatusServiceImplements.java:85-97`).
- Business-scoped uniqueness where it was implemented right
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsTaskCategory/bsTaskCategoryServiceImplements.java:49-54`,
  `BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsKBCategory/bsKBCategoryServiceImplements.java:43-46`).
- A new Business gets default settings in the same transaction
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessServiceImplements.java:67-79`).
- Tree operations maintain `level` and reject a category as its own parent
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryServiceImplements.java:98-109`).
- Duplicate manual mentions return the existing row instead of creating a copy
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrMention/bsPrMentionServiceImplements.java:65-74`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-01\|BT-R06-01]] | Uniqueness of tenant data is checked globally | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-02\|BT-R06-02]] | Priority delete reorders every tenant's priorities | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-03\|BT-R06-03]] | Channel insert fails with members; its uniqueness check uses a null project | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-04\|BT-R06-04]] | Task category delete throws `ConcurrentModificationException` | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-05\|BT-R06-05]] | Type update's category diff does not work | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-06\|BT-R06-06]] | HQ client update: eager `orElse` and fallback to another client | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-07\|BT-R06-07]] | MainHQ fetched with `findAll().get(0)`; null checks are dead | 🟢 Low | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-08\|BT-R06-08]] | Wrong exception types and `orElseThrow(null)` | 🟢 Low | error-handling | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-09\|BT-R06-09]] | "Print before delete" workaround in three services | 🟢 Low | correctness | Needs runtime verification |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-10\|BT-R06-10]] | Task invoice and channel↔task links are never written | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-11\|BT-R06-11]] | Mentions saved twice; naive `@username` parsing | 🟢 Low | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-12\|BT-R06-12]] | Category tree updates allow cycles and stale parents | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-13\|BT-R06-13]] | Inconsistent input guards cause 500s | 🟢 Low | correctness | Confirmed (read in code) |

## Findings

### BT-R06-01
**Title:** Uniqueness of tenant data is checked globally
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Invariants must carry their scope — in a multi-tenant model, "unique" means "unique within the tenant".
**Evidence:** `findByName`/`findByColor`/`findByTitle` without a Business in: status (name **or colour**)
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsStatus/bsStatusServiceImplements.java:42-45`),
priority (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsPriority/bsPriorityServiceImplements.java:47-50`),
type (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsType/bsTypeServiceImplements.java:50-53`),
docs category (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryServiceImplements.java:43-45`),
doc (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDoc/bsDocServiceImplements.java:44-46`),
KB entry (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsKB/bsKBServiceImplements.java:46-47`).
**Impact:** Once any tenant has a status "Open" (or any status coloured `#ff0000`), a priority "High" or a
type "Bug", no other tenant can create one — the configurable taxonomies, a core feature, work for the first
tenant only. The error also reveals that another tenant uses the name.
**Recommendation:** Scope every check by Business (`existsByBusinessAndNameIgnoreCase`) and back it with a
composite unique constraint `(business_entity_id, name)`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R06-02
**Title:** Priority delete reorders every tenant's priorities
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Same as above — ordering is a per-tenant invariant.
**Evidence:** Delete loads `findByPriorityOrderGreaterThan(order)` — no Business parameter — and decrements
all of them (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsPriority/bsPriorityServiceImplements.java:113-135`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsPriority/bsPriorityRepository.java:14`).
`update-order` accepts any ids without checking they belong to one Business
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsPriority/bsPriorityServiceImplements.java:97-111`).
**Impact:** Deleting priority #2 in tenant A shifts priorities #3… of every tenant down by one, producing
duplicates and gaps in all of them.
**Recommendation:** `findByBusinessAndPriorityOrderGreaterThan(business, order)`; validate that all ids in
`update-order` share the caller's Business; better, move ordering into `BusinessEntity` methods.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R06-03
**Title:** Channel insert fails with members; its uniqueness check uses a null project
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Build the object completely before using it.
**Evidence:** The entity comes from the builder with `members == null`
([[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-05|BT-R03-05]]); the loop calls
`toInsert.getMembers().add(member)` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrChannel/bsPrChannelServiceImplements.java:64-70`).
The duplicate check runs `existsByNameAndProject(form.getName(), toInsert.getProject())` before the project is
set on the entity (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrChannel/bsPrChannelServiceImplements.java:73-75`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrChannel/bsPrChannelServiceImplements.java:85`).
**Impact:** `POST /bs_pr_channel` with any `members` returns 500 — channels can only be created empty, and
nothing adds members later (the base `update` is a no-op). The duplicate check compares against
`project IS NULL` and never finds anything, so duplicate channel names per project are allowed.
**Recommendation:** Initialize `members`; set `project` before the check (or pass `project` directly); add a
unique constraint `(project_id, name)`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R06-04
**Title:** Task category delete throws `ConcurrentModificationException`
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Never mutate a collection while iterating it.
**Evidence:**

`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsTaskCategory/bsTaskCategoryServiceImplements.java:100-109`
```java
        Set<bsTypeEntity> types = toDelete.getTypes();
        if (!types.isEmpty()){
            for (bsTypeEntity type : types){
                if (type.getTaskCategories().size() < 2){
                    throw new InvalidDeleteOperation("Type: " + type.getName() + " has only this task category assigned." + " You should add one more task category to type " + type.getName() + " before deleting " + toDelete.getName() + " task category");
                }else {
                    toDelete.getTypes().remove(type);
                    type.getTaskCategories().remove(toDelete);
                }
            }
        }
```

`types` and `toDelete.getTypes()` are the same set.
**Impact:** Deleting a task category linked to two or more types fails with a `ConcurrentModificationException`
(500, rolled back) as soon as the loop advances after the first removal. Only categories with zero or one
linked type can be deleted. Because the set is emptied as it goes, the later `saveAll(types)` guard also
reads a modified set (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsTaskCategory/bsTaskCategoryServiceImplements.java:120-122`).
**Recommendation:** Validate first over a copy (`List.copyOf(types)`), then unlink over the copy; or use
`removeIf`.
**Verified against:** N/A — JDK `HashSet` iterator fail-fast semantics
**Confidence:** Confirmed (read in code)

### BT-R06-05
**Title:** Type update's category diff does not work
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Use the collection API for set differences, not hand-written nested iterators.
**Evidence:** The inner iterator `newIT` is created once, outside the outer loop, so only the first original
category is compared (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsType/bsTypeServiceImplements.java:103-115`);
`form.getTaskCategories().size()` is called without a null check
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsType/bsTypeServiceImplements.java:89`); unlinking
updates only the owning side and never removes the category from `toEdit.getTaskCategories()`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsType/bsTypeServiceImplements.java:117-122`).
**Impact:** The final join-table state is still correct, because every unmatched original is unlinked and
every requested category relinked — but at the cost of redundant writes; the DTO returned by the call can
still list removed categories; a Form without `taskCategories` gives 500. Combined with
[[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-02|BT-R07-02]], a bad id in the new
list commits the unlinks and leaves the type with fewer categories.
**Recommendation:** `Set<Long> toRemove = difference(original, requested)`, `toAdd = difference(requested, original)`,
validate all ids first, then apply to both sides.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R06-06
**Title:** HQ client update: eager `orElse` and fallback to another client
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** `orElse` evaluates its argument eagerly; lookups by path id must not silently fall back to another key.
**Evidence:** `repository.findById(ID).orElse(clientRepository.findByUsername(form.getUsername()).stream().findFirst().orElseThrow(...))`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/client/ClientServiceImplements.java:106`).
**Impact:** The username lookup and its `orElseThrow` always run: `PUT /client/5` with a username that no
client has fails with "The client is not found" although client 5 exists. When the id does not exist, the
update is applied to whichever client has the given username. The password comparison compares a raw value
with a hash, so the password is re-encoded on every update that sends one
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/client/ClientServiceImplements.java:120-122`).
**Recommendation:** `findById(id).orElseThrow(...)` only; username changes as an explicit, validated field.
**Verified against:** N/A — JDK `Optional.orElse` semantics
**Confidence:** Confirmed (read in code)

### BT-R06-07
**Title:** MainHQ fetched with `findAll().get(0)`; null checks are dead
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** Model singletons explicitly.
**Evidence:** `mainHQRepository.findAll().get(0)` followed by `if (mainHQEntity == null)` in four services
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/client/ClientServiceImplements.java:66-72`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/employee/EmployeeServiceImplements.java:68-76`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/invoice/InvoiceServiceImplements.java:72-77`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/plan/PlanServiceImplements.java:56-62`).
**Impact:** With no MainHQ the call throws `IndexOutOfBoundsException` (500) instead of the intended
`ElementNotFoundException`; the null branch can never run. Every call loads the whole MainHQ table.
**Recommendation:** A `MainHQProvider` with `findFirstByOrderByIdAsc().orElseThrow(...)` (or a configured id).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R06-08
**Title:** Wrong exception types and `orElseThrow(null)`
**Severity:** 🟢 Low
**Category:** error-handling
**Principle:** Exceptions name what happened.
**Evidence:** A missing client in Business insert throws `ElementAlreadyExist`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessServiceImplements.java:62`; the seeder
copies it, `BugTracker/src/main/java/com/bgsystem/bugtracker/shared/tools/firstInstallCheck.java:176-178`); "cannot
be its own parent" throws `ElementNotFoundException`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryServiceImplements.java:100-102`);
plan insert calls `orElseThrow(null)` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/plan/PlanServiceImplements.java:45-47`).
**Impact:** Once exceptions are mapped to status codes ([[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-01|BT-R07-01]]),
these would produce 409 for "not found" and 404 for "invalid"; `orElseThrow(null)` throws a
`NullPointerException` when the id is unknown.
**Recommendation:** Use `ElementNotFoundException` for missing references and a validation exception for
invalid input; replace `orElseThrow(null)`.
**Verified against:** N/A — JDK `Optional.orElseThrow(Supplier)` semantics
**Confidence:** Confirmed (read in code)

### BT-R06-09
**Title:** "Print before delete" workaround in three services
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** Understand and fix the cause; DTOs should hold copies, not live persistence collections.
**Evidence:** "Bug: is necessary to sout the roles … to return the correct DTO", followed by a `forEach(println)`
before `repository.delete` in admin, HQ client and HQ employee delete
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/admin/AdminServiceImplements.java:66-73`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/client/ClientServiceImplements.java:84-92`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/employee/EmployeeServiceImplements.java:91-98`).
The DTO receives the entity's own `roles` collection (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/admin/AdminServiceImplements.java:68`).
**Impact:** The DTO shares Hibernate's collection instance, which Hibernate clears when the entity is
deleted; the printout is a side effect that appears to change when that happens. The response depends on
logging code. Runtime check: remove the `forEach` and compare the `DELETE /admin/{id}` response.
**Recommendation:** `adminDTO.setRoles(Set.copyOf(toDelete.getRoles()))` (and do the same in mappers), or
return `204` without a body.
**Verified against:** N/A — Hibernate behavior not observed
**Confidence:** Needs runtime verification

### BT-R06-10
**Title:** Task invoice and channel↔task links are never written
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Accepted input must have an effect; mapped relations need a writer.
**Evidence:** `bsPrTaskForm.invoice` exists (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskForm.java:44`)
but the insert neither validates nor resolves it (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:65-142`).
No code writes `bsPrChannelEntity.tasks` or `bsPrTaskEntity.channels`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrChannel/bsPrChannelEntity.java:64-70`);
`channelCount` is never computed (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:86-87`).
**Impact:** A task can be linked to an invoice only from the invoice side; the channel↔task feature exists in
the schema and the ListDTO but not in behavior.
**Recommendation:** Either implement the links (resolve `invoice` in task insert; a
`PUT /bs_pr_channel/{id}/tasks` endpoint) or remove the fields and table.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R06-11
**Title:** Mentions saved twice; naive `@username` parsing
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** One owner per side effect.
**Evidence:** `MentionGenerator` saves each mention (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/tools/MentionGenerator.java:42`)
and the comment service saves each again (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrComment/bsPrCommentServiceImplements.java:97-104`).
Parsing splits on single spaces and keeps punctuation (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/tools/MentionGenerator.java:18-23`);
users are looked up across all tenants, and `@same` twice creates two mentions.
**Impact:** The second save is a redundant merge (no duplicate row); `@anna,` or `@anna\n` mention nobody;
users of other tenants can be mentioned.
**Recommendation:** Save once; parse with a regex (`@([A-Za-z0-9_.-]+)`), de-duplicate, restrict to channel
members.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R06-12
**Title:** Category tree updates allow cycles and stale parents
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Tree invariants (acyclic, consistent levels, one tenant) enforced in one place.
**Evidence:** Re-parenting rejects only `parent == self`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryServiceImplements.java:98-109`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsKBCategory/bsKBCategoryServiceImplements.java:97-107`);
the old parent's `subCategories` and `isAParentCategory` flag are not updated and descendants keep their old
`level`. On insert, the child's Business is taken from the parent, ignoring `form.getBusiness()`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryServiceImplements.java:59-62`).
**Impact:** A → B then B → A creates a cycle that makes any recursive tree walk loop forever; levels and
parent flags drift after moves; a parent id from another tenant places the new category in that tenant.
**Recommendation:** Walk the new parent's ancestors to reject cycles; update old/new parent flags; recompute
descendant levels (or drop stored levels); require parent and form Business to match the caller's tenant.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R06-13
**Title:** Inconsistent input guards cause 500s
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** Validate at the boundary, uniformly.
**Evidence:** `bsEmployee` insert does not check `password` and calls `encode(null)`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsEmployee/bsEmployeeServiceImplements.java:44-55`);
`bsStatus` update calls `form.toString()` before its null check
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsStatus/bsStatusServiceImplements.java:66-69`);
`bsDocsCategory`/`bsKBCategory` update dereference the form without a check
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryServiceImplements.java:86-90`).
The duplicate message in `bsEmployee` says "Manager already exists" (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsEmployee/bsEmployeeServiceImplements.java:51-52`).
**Impact:** Missing fields surface as `IllegalArgumentException`/`NullPointerException` (500) instead of a
validation error.
**Recommendation:** Bean Validation on Forms (`@NotBlank password`) and one validation path; see
[[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-03|BT-R07-03]].
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- Tenant-scoped repository methods only (`…ByBusinessAnd…`), backed by composite unique constraints.
- Validate everything first, then mutate: resolve all references, check all invariants, then apply.
- Invariants as entity methods (`business.removePriority(p)` renumbers only that tenant's priorities;
  `category.moveUnder(parent)` checks cycles and levels).
- Set differences with collection APIs over copies; no mutation during iteration.
- Actor, tenant and timestamps from context, not from Forms.
- One exception per meaning, mapped to one status.

## Related Documents

- [[Docs/BugTracker/Explanations/09-Service-Layer-Patterns]]
- [[Docs/BugTracker/Explanations/11-Collaboration-Features]]
- [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review]]
- [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review]]
