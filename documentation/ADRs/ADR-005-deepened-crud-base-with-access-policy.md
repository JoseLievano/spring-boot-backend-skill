#adr #adr-accepted #architecture #backend #api-design #security

## ADR 005: Deepened generic CRUD base with hooks, three DTO shapes and one Access Policy per feature

### Status
Accepted

### Context

All three reference projects share a generic CRUD base, inherited down a lineage. It ignores the request on
`update` and reports success, it charges six type parameters and ten files per entity, it puts checked
exceptions in every generic signature, it lets a client-supplied id overwrite a row, it needs only a login
for inherited operations, it runs storage I/O inside the database transaction, it has no optimistic
locking, and every success is 200. wpmanager overrode 29 of the base's 60 methods, which says the base is
shallow where it matters. Decision D5 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] deepens the base instead of deleting it;
review decisions F3 made Row Scope mandatory, F4 moved transactions to the entry points, F7 made the Access
Policy the single authorization module, F9 added conditional writes, and F17 made the actor explicit.

**Evidence:**
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01|BE-R02-01]],
[[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-01|BT-R02-01]] and
[[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-01|WP-R02-01]] — the base
`update` ignores the request, in all three;
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-06|BE-R02-06]] and
[[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-03|BT-R02-03]] — the
type-parameter and file-count tax;
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-07|BE-R02-07]] and
[[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-04|BT-R07-04]] — checked
exceptions in generic signatures;
[[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-06|WP-R02-06]] — the base
service is shallow and mostly overridden;
[[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-02|BT-R02-02]] — a
client-supplied id overwrites a row;
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-03|WP-R01-03]] — inherited operations need only a
login;
[[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-02|WP-R03-02]] — storage
I/O inside the transaction;
[[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-07|BT-R03-07]] — no optimistic
locking;
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-04|BE-R02-04]],
[[Docs/BugTracker/Reviews/02-CRUD-Framework-and-API-Design-Review#BT-R02-07|BT-R02-07]] and
[[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-04|WP-R02-04]] — every success
is 200.

### Decision

1. We will keep a generic CRUD base with six entry points — get, list, search, create, update, delete — that
   takes four collaborators: repository, mapper, Query Profile, Access Policy. It cannot be constructed
   without a policy.
2. We will use three DTO shapes — Request, Response, Summary — in place of the reference quartet.
3. We will apply every request field through the mapper on update; ids are assigned by the server.
4. We will let features customise through hooks only: validate on create, validate on update, apply
   relations, before delete.
5. We will keep authorization out of the hooks: `<F>AccessPolicy` is the feature's single authorization
   module, with `check(action, actor, entityOrNull)` and `rowScope(actor)`. Actions are the CRUD actions
   plus download. The policy denies when no rule matches. The base consults it once per entry point.
   Actor-independent invariants go in the hooks.
6. We will make Row Scope mandatory: it is ANDed into every get, list, search, update and delete query
   before paging, and a row outside the scope reads as 404, not 403.
7. We will pass the actor explicitly to every entry point. The system actor is an ordinary actor that
   passes through the same policy.
8. We will make writes conditional: managed entities carry a server-owned concurrency token, and get,
   create and update return it as a strong `ETag`. Update and delete require `If-Match` — stale is 412,
   missing is 428, `*` is the explicit unconditional write. There is no version field in the request body.
   The check order is: authenticate → policy check (403) → scoped load (404) → precondition (412 or 428) →
   mutate. A persistence-layer lock failure maps to the same 412. Bulk mutation of managed rows is banned
   outside the base.
9. We will declare transactions per entry point on the six base methods, read-only for the three reads. A
   feature-added service method is non-transactional unless it declares otherwise — a documented departure
   from the reference projects.
10. We will use these status codes: create 201 with a location, delete 204, the rest 200.
11. We will let a feature that does not fit opt out with a plain service and controller that still use the
    errors, query and access modules.
12. We will measure depth: across generated features, fewer than half override create or update wholesale.
    If that criterion fails in two validation runs, this ADR is revisited by a superseding ADR.

**Depth statement:** deleting the base would re-create six endpoints, six service methods and the
transaction, authorization and concurrency wiring in every feature. The Access Policy has two methods and
holds every "who may do what to which rows" rule of a feature.

**Mechanism on the current line (Spring Boot 4.1.x, 2026-10):** per-method `@Transactional` and a
persistence-managed `@Version` concurrency token. Not verified by a test until the Base Project is built.

**Alternatives rejected:**
- No base at all — every feature hand-written; the defects return one by one.
- The reference base unchanged — it is where the critical findings live.
- An authorize hook plus method-security annotations — authorization in four places (F7).
- Class-level transactions — feature-added methods would be transactional by default (F4).
- A version in the request body, or 409 for stale writes — unpinned dual channel, and 409 on that channel
  violates the conditional-write semantics (F9).
- A hidden version column only — does not stop a lost update between two readers.

### Consequences

- A new feature is cheap and correct by default.
- An endpoint without an access policy cannot exist.
- Clients must send `If-Match` on update and delete.
- Every signature carries the actor.
- A 404 for an invisible row hides existence but makes "why can't I see it" harder to diagnose.
- Multi-write custom methods must remember to declare a transaction.
- If the depth criterion fails, the base was the wrong shape and must be redesigned.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D5; F3, F4, F7, F9, F17),
[[ADRs/ADR-012-mapstruct-mapping-with-unmapped-target-errors|ADR-012]],
[[ADRs/ADR-011-list-api-get-paging-and-post-search|ADR-011]],
[[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]].
