# Filter Engine Review — BugTracker

#doc #review #ref-bugtracker #api-design #persistence

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/06-Dynamic-Filtering-and-Pagination]]

## Scope

Reviewed: `shared/models/listRequest/*`, `shared/models/pageableRequest/*`, the list/page methods of
`DefaultServiceImplements`, and the 31 `XPredicate` classes (in depth: `bsPrTaskPredicate`,
`BusinessPredicate`, `countryPredicate`). Excluded: generated Q-classes.

## Verdict

The **request contract** is the best part of the engine and worth keeping: field + operator list, OR within
a field, AND across fields, association filters, multi-sort, paging. The **implementation** is not safe to
copy. The predicate that holds the filters is a mutable singleton shared by all requests; types are guessed
from values; any field — including `password` — can be filtered and any property sorted; and several
inputs crash with a 500. `backend/` later replaced this with a whitelist-first engine, which is the right
direction. All findings are defects on Boot 2.7; none is an era gap.

## Strengths to Keep

- A single JSON contract for filter, sort and page on every entity
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/pageableRequest/PageableRequest.java:19-27`).
- Clear boolean semantics: OR inside a field, AND across fields
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:28-40`,
  `BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:99-106`).
- Operators are restricted per value kind and unknown ones are rejected
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:170-178`).
- Association filters use generated, type-safe Q-paths
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskPredicate.java:85-109`).
- Filtering happens in SQL through `QuerydslPredicateExecutor`, combined with paging
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:95-103`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-01\|BT-R05-01]] | Filters are stored on a shared singleton | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-02\|BT-R05-02]] | Type dispatch by value: `"false"` and date-shaped strings are misread | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-03\|BT-R05-03]] | `getDeclaredField` misses inherited fields; unknown fields give 500 | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-04\|BT-R05-04]] | Invalid dates on association paths cause a `NullPointerException` | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-05\|BT-R05-05]] | `POST /list` ignores its filter | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-06\|BT-R05-06]] | `PageableRequest` fails on a missing `sort`; `Optional` as a field | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-07\|BT-R05-07]] | No field or sort whitelist (password hashes can be probed) | 🟠 High | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-08\|BT-R05-08]] | Association date paths are built from a path's `toString()` | 🟢 Low | correctness | Needs runtime verification |
| [[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-09\|BT-R05-09]] | `countryPredicate` has no root path; dead helper | 🟢 Low | correctness | Confirmed (read in code) |

## Findings

### BT-R05-01
**Title:** Filters are stored on a shared singleton
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Stateless services — per-request data must not live in fields of a singleton bean.
**Evidence:** `CommonPathExpression` is an abstract `@Service` with a mutable `filters` field and a setter
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:13-26`);
every concrete predicate is a `@Service` with default (singleton) scope — there is no `@Scope` anywhere in the
code (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskPredicate.java:12-13`).
The service calls `setFilters(...)` and then `getExpression()` as two steps
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:97-101`).
**Impact:** Two concurrent `/page` requests on the same module can interleave: request A sets its filters,
request B overwrites them, and A builds B's expression. A user receives rows matching someone else's
filter — and because tenant scoping today exists only as a client-side `business` filter
([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02|BT-R01-02]]), that can be another
tenant's data. Rare under light load, certain under concurrency.
**Recommendation:** Make the predicate stateless: `BooleanExpression toExpression(List<FilterRequest> filters)`
with no fields other than the immutable path metadata.
**Verified against:** N/A — Spring's default singleton scope
**Confidence:** Confirmed (read in code)

### BT-R05-02
**Title:** Type dispatch by value: `"false"` and date-shaped strings are misread
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Type information belongs to the schema (field), not to the input value.
**Evidence:** For non-number fields, the path type is chosen from the **value**: date if it parses as
`dd/MM/yyyy`, boolean if `isABoolean(value)`, else string
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:74-94`).
`isABoolean` returns `Boolean.parseBoolean(value)`, i.e. `true` only for `"true"`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:284-288`).
**Impact:** `{"field":"isDone","operations":[{"operator":"=","value":"false"}]}` builds a **string** path on a
`Boolean` column (`equalsIgnoreCase` on a boolean) — the query fails or matches nothing, so "open tasks"
cannot be filtered. A string field searched for `"01/02/2023"` becomes a date comparison on a string column.
**Recommendation:** Dispatch on the field's Java type (already available via reflection or Q-class metadata):
`Boolean` → `Boolean.valueOf(value)`, `Date`/`LocalDate` → parse, else string. Reject values that do not parse.
**Verified against:** N/A — JDK `Boolean.parseBoolean` semantics
**Confidence:** Confirmed (read in code)

### BT-R05-03
**Title:** `getDeclaredField` misses inherited fields; unknown fields give 500
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Robust input handling — invalid client input is a 400, not a 500.
**Evidence:** `entityClass.getDeclaredField(filter.getField())`, with `NoSuchFieldException` rethrown as a
`RuntimeException` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:65-72`).
`getDeclaredField` only sees fields declared on the class itself.
**Impact:** On the six `User` subclasses (`/admin`, `/client`, `/employee`, `/bs_client`, `/bs_manager`,
`/bs_employee`), filtering by `username`, `email`, `firstName` or `lastName` — the fields a user grid needs —
throws and returns 500. Any typo in a field name also returns 500.
**Recommendation:** Resolve fields through the Q-class/metamodel (or walk superclasses), and map unknown
fields to a 400 with the list of allowed fields.
**Verified against:** N/A — JDK `Class.getDeclaredField` semantics
**Confidence:** Confirmed (read in code)

### BT-R05-04
**Title:** Invalid dates on association paths cause a `NullPointerException`
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Validate before use; do not swallow parse errors.
**Evidence:** `getDateFromString` catches the parse error, prints the stack trace and returns `null`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:230-240`);
`getDatePathBooleanExpression` then calls `date.getTime()`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:191-195`).
Scalar fields are protected by the `isADate` pre-check, but association date filters call the method
directly (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskPredicate.java:53-60`).
**Impact:** `{"field":"project","operations":[{"field":"dueDate","operator":">","value":"2023-12-31"}]}` → 500.
**Recommendation:** Parse with `java.time` and throw a 400-mapped exception on failure; never return `null`
from a parser.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R05-05
**Title:** `POST /list` ignores its filter
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** An accepted parameter must have an effect (or be rejected).
**Evidence:** `getAllListView(Optional<FilterRequest> listRequestRecord)` returns
`repository.findAll()` mapped to ListDTOs; the argument is never read
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:79-87`). The
controller accepts a single `FilterRequest`, not a list (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/controller/DefaultController.java:50-59`).
**Impact:** Clients that send a filter get every row of every tenant, unfiltered and unpaged, and with
stale counters (no `updateListFields`).
**Recommendation:** Remove the endpoint, or implement it on top of the paged path with the same contract.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R05-06
**Title:** `PageableRequest` fails on a missing `sort`; `Optional` as a field
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Request DTOs should be total: every field optional or validated.
**Evidence:** `getPageRequest()` calls `sort.size()` without a null check
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/pageableRequest/PageableRequest.java:29-35`);
`filter` is declared `Optional<ArrayList<FilterRequest>>` and the service calls `getFilter().isPresent()`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/pageableRequest/PageableRequest.java:27`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:95`). `size` has no
upper bound and `SortInfo.isAscending` is a `Boolean` unboxed without a check
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/pageableRequest/PageableRequest.java:35`).
**Impact:** A body without `sort` (or a sort entry without `isAscending`) returns 500; whether an omitted
`filter` arrives as `Optional.empty()` or `null` depends on Jackson's handling of this Lombok class (runtime
check: `POST /bs_status/page {"page":0,"size":5,"sort":[]}`). A client can request any page size.
**Recommendation:** Plain nullable fields with defaults (`sort = List.of()`, `filters = List.of()`),
`@Min(0) page`, `@Min(1) @Max(100) size`, and `@Valid` on the controller parameter.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R05-07
**Title:** No field or sort whitelist (password hashes can be probed)
**Severity:** 🟠 High
**Category:** security
**Principle:** Whitelist-first querying — expose only fields that are meant to be queried.
**Evidence:** Any declared field name is accepted (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:60-97`);
sort properties go straight into `Sort.Order` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/pageableRequest/PageableRequest.java:34-41`).
`UserPredicate` works on `User`, where `password` is declared (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/UserPredicate.java:7-16`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:53-54`).
**Impact:** `POST /user/page` with `{"field":"password","operations":[{"operator":":","value":"$2a$10$Q"}]}`
tells an authenticated caller whether a user's hash contains a substring, which is enough to extract full
BCrypt hashes character by character for offline cracking. Sorting by `password` leaks ordering
information; sorting by an unknown property fails with a 500.
**Recommendation:** A per-entity whitelist (field → path, type, allowed operators, sortable) and rejection
of everything else, as in `backend/`'s query profiles
([[Docs/backend/Explanations/05-Dynamic-List-Query-Engine]]).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** backend's whitelist engine is the fix already in the lineage — listed as a strength in [[Docs/backend/Reviews/06-Query-Engine-Review]].

### BT-R05-08
**Title:** Association date paths are built from a path's `toString()`
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** Use typed paths; do not round-trip them through strings.
**Evidence:** `entityPath.getDate(bsPrTaskEntity.invoice.dateGenerated.toString(), Date.class)`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskPredicate.java:54`), and the
same for `limitDate`, project `created`/`dueDate` and business `dateCreated`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskPredicate.java:178-183`).
A Q-path's `toString()` is `bsPrTaskEntity.invoice.dateGenerated`, so the new property is named with the root
alias included.
**Impact:** Likely renders as `bsPrTaskEntity.bsPrTaskEntity.invoice.dateGenerated` in JPQL and fails, so
date filters on associations would not work. Runtime check: `POST /bs_pr_task/page` with an `invoice`
`dateGenerated` filter and inspect the error or SQL.
**Recommendation:** Use the generated `DateTimePath` directly (`bsPrTaskEntity.invoice.dateGenerated.after(...)`).
**Verified against:** N/A — QueryDSL 5 path semantics not executed
**Confidence:** Needs runtime verification

### BT-R05-09
**Title:** `countryPredicate` has no root path; dead helper
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** —
**Evidence:** `countryPredicate` never sets `entityPath` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/geo/country/countryPredicate.java:6-13`),
so `entityPath.getType()` would throw; `isANumber` is never called
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/listRequest/CommonPathExpression.java:249-257`).
**Impact:** Dormant: `POST /country/page` with a filter returns 500. No other effect.
**Recommendation:** Set the path (or generate it from the Q-class in the base constructor); delete unused helpers.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

Keep the JSON contract, replace the engine:

```java
public record FieldSpec(String name, Path<?> path, Class<?> type, Set<Operator> ops, boolean sortable) {}

public final class QueryProfile<E> {          // one immutable bean per entity
    private final Map<String, FieldSpec> fields;
    public Predicate toPredicate(List<FilterRequest> filters) { /* whitelist, typed parse, 400 on error */ }
    public Sort toSort(List<SortInfo> sort)                  { /* sortable fields only */ }
}
```

- Stateless, whitelist-first, typed by field; association fields declared as dotted names mapped to Q-paths.
- `page ≥ 0`, `1 ≤ size ≤ 100`, defaults for missing `sort`/`filter`.
- Invalid field, operator or value → one exception mapped to 400.
- Tenant predicate ANDed by the service, never supplied by the client.

## Related Documents

- [[Docs/BugTracker/Explanations/06-Dynamic-Filtering-and-Pagination]]
- [[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review]]
- [[Docs/backend/Reviews/06-Query-Engine-Review]]
