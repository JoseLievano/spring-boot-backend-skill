#adr #adr-accepted #api-design #backend #security

## ADR 011: List API with `GET` paging, `POST /search` and an owned page response

### Status
Accepted

### Context

The newest reference project routes reads through `POST /list`, with unbounded query complexity, a field
name repeated three times per profile entry, enum and collection fields that cannot be filtered, and
zone-less date strings silently read as UTC. The framework's page type is serialised directly, so the wire
shape is not ours. The oldest one stores filters on a shared singleton and has no field whitelist, so
password hashes can be probed through a sort. Decision D11 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes the list contract; review decision
F3 made the row scope a mandatory argument of the engine.

**Evidence:**
[[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-03|BE-R06-03]] — reads go through `POST /list`;
[[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-04|BE-R06-04]] — query complexity is unbounded;
[[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-05|BE-R06-05]] — the field name repeated three times
per profile entry;
[[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-02|BE-R06-02]] — enum and collection fields cannot be
filtered;
[[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-06|BE-R06-06]] — zone-less date strings silently read
as UTC;
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-08|BE-R02-08]] and
[[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-08|BT-R02-08]] — the framework
page type serialised directly;
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-03|BE-R02-03]],
[[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-05|BT-R02-05]] and
[[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-03|WP-R02-03]] — unbounded
"get all";
[[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-01|BT-R05-01]] — filters stored on a shared
singleton;
[[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-07|BT-R05-07]] — no field or sort whitelist, so
password hashes can be probed;
[[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-02|BT-R05-02]] — type dispatch by value;
[[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-03|BT-R05-03]] — unknown fields give 500.

### Decision

1. We will expose simple listing as `GET /resources?page&size&sort`.
2. We will expose rich filters as `POST /resources/search` — operator lists, OR within a field, AND across
   fields, multi-sort.
3. We will parse both forms into one internal list request and run them by one engine, through one entry
   point that takes the Query Profile, the Row Scope, the request and the summary mapping.
4. We will make the Row Scope argument mandatory and AND it before paging — a query without a scope cannot
   be written.
5. We will declare each field once in the Query Profile — name, path, type, operators, sortable — plus the
   default sort and the bounds: maximum page size, maximum filter count, maximum values per filter. Anything
   not declared is rejected.
6. We will support enum and collection fields, and reject a date-time without a zone.
7. We will keep the engine stateless.
8. We will own the wire format: an owned page response with items, page, size, total items, total pages.
9. We will keep the predicate technology an implementation detail behind the interface, recorded in a
   Version Note.
10. We will never guard a search with idempotency.

**Depth statement:** one entry point hides parsing, whitelisting, typing, bounds, scoping, sorting and
paging. The Query Profile restricts fields; the Row Scope restricts rows; the two never mix.

**Alternatives rejected:**
- `POST /list` only — the reference shape; not cacheable, not bookmarkable.
- A filter language in the query string — brittle to parse and to bound.
- Serialising the framework's page type — the wire shape would belong to a dependency.
- Automatic REST export — bypasses every rule above.

### Consequences

- Simple lists are cacheable and bookmarkable; grids get rich filters.
- Sensitive columns cannot be probed, because undeclared fields are rejected.
- Two request forms must be documented and tested.
- The rich form is a read over POST, which is a deliberate trade-off for filter expressiveness.
- Each queryable field costs one profile entry — by design.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D11; F3),
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]],
[[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]].
