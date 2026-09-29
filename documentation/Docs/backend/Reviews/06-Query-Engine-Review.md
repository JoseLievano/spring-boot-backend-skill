# Query Engine Review — backend

#doc #review #ref-backend #api-design #persistence

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/05-Dynamic-List-Query-Engine]]

## Scope

Reviewed: all ten classes in `shared/query/`, both query profiles, `DefaultServiceImplements.getListPage`,
the `/list` endpoint, and the query tests at all four layers. Excluded: the JSON shape of `Page`
responses (see [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-08|BE-R02-08]]).

## Verdict

This is the best-designed part of `backend/` and the strongest candidate for the skill. `QueryableField`
is a genuinely deep module: a tiny public surface (`buildPredicate`, `sortable`, `nullable`) hides type
conversion, operator whitelists and predicate construction for six value kinds. Whitelisting, typed
conversion, bounded paging and profile-controlled sort properties close the usual holes of dynamic
filtering. The findings are about limits and reach, not about correctness: no bound on query
complexity, a few unused or missing capabilities, and reads over `POST`.

## Strengths to Keep

- **Whitelist-first:** only fields registered in a profile can be filtered or sorted; unknown fields are a
  400 (`backend/src/main/java/com/agentForgeBackend/shared/query/EntityQueryProfile.java:16-32`).
- **Deep module:** `QueryableField` exposes one method to the builder and keeps conversion + operator
  rules private (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:267-281`,
  `backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:389-560`).
- **Strict typed conversion:** no guessing (`"true"` is not a boolean, `1.5` is not a `Long`), exact
  integral conversion through `BigDecimal`, non-finite doubles rejected
  (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:405-447`,
  `backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:521-540`).
- **Sort injection closed:** sort uses the profile's `sortProperty`, never the client string
  (`backend/src/main/java/com/agentForgeBackend/shared/query/PageableFactory.java:59-64`).
- **Bounded page size** (1–100) enforced twice
  (`backend/src/main/java/com/agentForgeBackend/shared/query/PageableRequest.java:25-28`,
  `backend/src/main/java/com/agentForgeBackend/shared/query/PageableFactory.java:80-82`).
- **Client error vs programming error** separated (`InvalidQueryRequestException` vs
  `IllegalStateException` for a broken default sort)
  (`backend/src/main/java/com/agentForgeBackend/shared/query/PageableFactory.java:65-69`).
- **Stateless singleton builders:** `PageableFactory` and `QueryPredicateBuilder` have no fields, so one
  shared bean per class is safe for all entities
  (`backend/src/main/java/com/agentForgeBackend/shared/query/PageableFactory.java:11-12`,
  `backend/src/main/java/com/agentForgeBackend/shared/query/QueryPredicateBuilder.java:10-11`).
- **Layered tests** from unit to MockMvc, including negative cases for sensitive fields
  (`backend/src/test/java/com/agentForgeBackend/models/hq/client/ClientServiceListQueryIntegrationTest.java:80`).
- **Immutability:** fields copy their operator sets and return new instances from `sortable`/`nullable`
  (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:102`,
  `backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:231-257`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-01\|BE-R06-01]] | `nullable()` is never used by the profiles | 🟢 Low | api-design | Confirmed (read in code) |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-02\|BE-R06-02]] | Enum and collection fields (`roles`) cannot be filtered | 🟢 Low | api-design | Confirmed (read in code) |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-03\|BE-R06-03]] | Reads go through `POST /list` | 🟢 Low | api-design | Confirmed (read in code) |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-04\|BE-R06-04]] | Query complexity is unbounded | 🟡 Medium | performance | Confirmed (read in code) |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-05\|BE-R06-05]] | Field name repeated three times per profile entry | 🟢 Low | design | Confirmed (read in code) |
| [[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-06\|BE-R06-06]] | Zone-less date/time strings are silently read as UTC | 🟢 Low | correctness | Confirmed (read in code) |

## Findings

### BE-R06-01
**Title:** `nullable()` is never used by the profiles
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** —
**Evidence:** `nullable()` adds `IS_NULL`/`IS_NOT_NULL` (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:243-257`); no profile calls it (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminQueryProfile.java:18-29`, `backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientQueryProfile.java:18-29`), although `firstName`, `lastName`, `dateCreated` and `lastLogin` are nullable columns (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:25-56`).
**Impact:** Clients cannot ask for "users who never logged in"; the capability is tested but unreachable over HTTP.
**Recommendation:** Mark nullable columns `.nullable()` in the profiles (and remember that, with [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-02|BE-R03-02]] unfixed, every date is null).
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R06-02
**Title:** Enum and collection fields (`roles`) cannot be filtered
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** —
**Evidence:** `enumField` exists (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:155-169`) but the only enum-typed attribute, `roles`, is a collection (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:37-43`), and `QueryableField` has no factory for collection paths (`any().in(...)`). The client test asserts that `roles` is rejected (`backend/src/test/java/com/agentForgeBackend/models/hq/client/ClientControllerListEndpointTest.java:106`).
**Impact:** "List all users with role X" is not expressible through the list endpoint.
**Recommendation:** Add a `collection(apiName, CollectionPathBase<…>, elementConverter)` factory that maps `EQUALS`/`IN` to `path.any().eq(...)`/`any().in(...)` — or keep it out deliberately and document it.
**Verified against:** N/A — design suggestion; QueryDSL collection `any()` should be confirmed against `querydsl-core` 6.12 when implemented
**Confidence:** Confirmed (read in code)

### BE-R06-03
**Title:** Reads go through `POST /list`
**Severity:** 🟢 Low
**Category:** api-design
**Principle:** HTTP semantics — safe, cacheable reads use `GET`.
**Evidence:** `@PostMapping("/list")` returning a page (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultController.java:35-39`).
**Impact:** Responses are not cacheable by HTTP intermediaries, list URLs cannot be bookmarked or linked, and the endpoint looks like a write to gateways and audit tools. The trade-off is deliberate: nested JSON filters are awkward in query strings.
**Recommendation:** Keep `POST` for complex searches but name it as a search resource (`POST /admin/search`) and also offer `GET /admin?page=&size=&sort=` for the simple case; document the choice.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R06-04
**Title:** Query complexity is unbounded
**Severity:** 🟡 Medium
**Category:** performance
**Principle:** Bound every client-controlled input.
**Evidence:** `filters` and `sort` have `@NotNull` but no `@Size(max)` (`backend/src/main/java/com/agentForgeBackend/shared/query/PageableRequest.java:30-36`); `operations` has only `@Size(min = 1)` (`backend/src/main/java/com/agentForgeBackend/shared/query/FilterRequest.java:24-26`); `IN` arrays and string values have no length limit (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:542-560`, `backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:389-395`). `CONTAINS`/`ENDS_WITH` compile to case-insensitive `LIKE '%…%'`, which cannot use a B-tree index (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:292-294`).
**Impact:** A single request with thousands of OR-ed `CONTAINS` operations or a huge `IN` list produces a very expensive SQL statement (and can exceed the database's bind-parameter limit); with authorization missing ([[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]]) anyone can send it.
**Recommendation:** Add `@Size(max = 10)` on `filters`, `@Size(max = 3)` on `sort`, `@Size(max = 10)` on `operations`, cap `IN` at ~100 values and string values at ~256 characters in `QueryableField`, and consider trigram indexes (`pg_trgm`) for fields exposed to `CONTAINS`.
**Verified against:** N/A — no library API involved (standard Bean Validation `@Size`)
**Confidence:** Confirmed (read in code)

### BE-R06-05
**Title:** Field name repeated three times per profile entry
**Severity:** 🟢 Low
**Category:** design
**Principle:** Single source of truth.
**Evidence:** `"email", QueryableField.<AdminEntity>string("email", ADMIN.email).sortable("email")` (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminQueryProfile.java:22`); the map key and `apiName` are independent strings, and nothing checks that they match.
**Impact:** A typo makes error messages name a different field than the one the client sent, or sorts by the wrong property. `Map.of` also caps a profile at ten fields.
**Recommendation:** Build the map from the fields (`Stream.of(fields…).collect(toMap(QueryableField::apiName, f -> f))`) and default `sortable()` to the path's own property name (`expression` metadata), keeping the explicit overload for renames.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R06-06
**Title:** Zone-less date/time strings are silently read as UTC
**Severity:** 🟢 Low
**Category:** correctness
**Principle:** Explicit over implicit.
**Evidence:** For `Date`/`Instant` fields, `parseInstant` falls back to `LocalDateTime.parse(value).toInstant(ZoneOffset.UTC)` and then to `LocalDate…atStartOfDay()` in UTC (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:505-519`).
**Impact:** A client in UTC−5 filtering `dateCreated >= "2024-01-01T00:00"` gets rows from five hours earlier than intended, with no error.
**Recommendation:** Require an offset for instant-typed fields (accept `Instant`/`OffsetDateTime` formats only) or make the default zone a documented configuration value.
**Verified against:** N/A — JDK `java.time` only
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

Keep the design as the skill's list/search pattern, with these adjustments:

- Profiles built from a list of `QueryableField`s (key derived from `apiName`), `.nullable()` where the
  column allows null, a collection-field factory for element collections.
- Hard limits on filters, operations, sort entries, `IN` size and string length, declared on the request
  DTOs and enforced again in the builder.
- A project-owned page response ([[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-08|BE-R02-08]]).
- `POST /<resource>/search` for rich queries plus `GET /<resource>` for simple paging.
- Instant fields require an explicit offset.
- The same four-layer test strategy the project already uses.

## Related Documents

- [[Docs/backend/Explanations/05-Dynamic-List-Query-Engine]]
- [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review]]
- [[Docs/backend/Reviews/08-Testing-Review]]
