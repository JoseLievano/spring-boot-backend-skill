#adr #adr-accepted #api-design #backend

## ADR 009: Unchecked domain exceptions mapped to RFC 9457 problem details

### Status
Accepted

### Context

The three reference projects each invented their own error story: five checked exceptions with no common
base, response-building code duplicated four ways, custom error bodies instead of a standard, internal
messages returned on a 500, domain exceptions that become 500, and checked exceptions that commit partial
work before failing. The same conflict returns 409 on one route and 400 on another. Clients therefore parse
three shapes and cannot switch on anything stable. Decision D9 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes one contract; review decisions F5
gave 422 two content meanings, F9 pinned 412 and 428, F13 pinned 404 and 410 for tickets, and F14 required
one uniform shape for credential failures.

**Evidence:**
[[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-01|BE-R05-01]],
[[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-02|BE-R05-02]] — five checked exceptions with no
common base; response-building duplicated four ways;
[[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-03|BE-R05-03]] — no fallback handler, so unexpected
errors leave the contract;
[[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-04|BE-R05-04]] — internal messages returned on 500;
[[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06|BE-R05-06]] and
[[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-05|BT-R07-05]] and
[[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-06|WP-R08-06]] — a custom body
instead of an RFC 9457 problem detail, in all three projects;
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-07|BE-R02-07]] and
[[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-04|BT-R07-04]] — checked
exceptions leaking into generic signatures;
[[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-01|BT-R07-01]] and
[[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-02|BT-R07-02]] — domain
exceptions unmapped, and checked exceptions committing partial work;
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-08|BT-R01-08]] — a token validator
that fails with 500 instead of 401;
[[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-01|WP-R08-01]],
[[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-02|WP-R08-02]] and
[[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-07|WP-R08-07]] — no fallback
handlers, internal messages on 500, three ways to write an error;
[[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01|WP-R04-01]] — every upload failure becomes 500;
[[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-07|BE-R03-07]],
[[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-03|WP-R08-03]] and
[[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-03|BT-R07-03]] — validation on
entities or done by hand;
[[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-05|BE-R04-05]] — the same conflict
returns 409 on create and 400 on update;
[[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-08|BE-R09-08]] — hand-made object-mapper instances
that bypass the application's configuration.

### Decision

1. We will define one unchecked domain exception hierarchy. Each exception carries an error code: an HTTP
   status, a stable `type` URI and a title. The built-in kinds are: not found 404, conflict 409, forbidden
   403, business rule violation 422, precondition failed 412, precondition required 428. Unchecked means a
   transaction rolls back by default. The parent states 412 and 428 explicitly; 404, 409 and 403 follow from
   their names and from the status list of user story 17. The 422 status for a business rule violation is
   this Task's reading of the parent and is put to the user for confirmation.
2. We will render every error response as an RFC 9457 problem detail, with one `type` URI per failure shape,
   so a client switches on `type` and never on a message.
3. We will pin these meanings: 409 is for state conflicts only — an in-flight idempotency lease, an
   already-bound subject — and never for stale writes; 412 is a stale precondition; 428 is a missing
   precondition; an idempotency fingerprint mismatch and a content-digest mismatch are two distinct 422
   types; an unknown or tampered download ticket reads as 404, an expired one as 410, and one for a deleted
   object as 404; bad credentials and a locked account return one identical shape.
4. We will route every failure through one central handler: domain exceptions to their status; request
   validation to the validation status with a field-error list; anything else to 500 with a generic detail
   and a correlation id, with the full error logged server-side only.
5. We will render authentication and authorization failures through the same problem writer and the
   application's configured JSON mapper, never through a hand-made one.
6. We will place validation constraints on the Request shape, not on entities.
7. **The validation status is 400.** The rule: *400 means the request is invalid by itself — unreadable,
   wrong type, a missing required input, an unknown query field or a failed Bean Validation constraint; no
   server state is needed to judge it. 422 means the request is valid in itself but is refused against
   server state or content — a business rule, an idempotency fingerprint that differs from the stored one, a
   declared digest that differs from the received bytes.* One checkable test classifies every future case;
   it equals the framework default for every input path, so no override exists that a second code path can
   forget; and 422 keeps the two content meanings F5 gave it. A file request with no declared digest is
   invalid by itself, so it is 400.

**Mechanism on the current line (Spring Boot 4.1.x, 2026-10):** a failed request-body validation is turned
into a 400 response by default; the method-validation exception is documented as 400 for input validation
errors (500 for return-value validation). Both verified against the Spring Framework reference and javadoc
on 2026-10-04; not verified by a test until the Base Project is built.

**Alternatives rejected:**
- Checked exceptions — they leak into every signature and let a caller commit partial work.
- A custom error body — clients cannot switch on anything standard.
- 422 for Bean Validation, with 400 only for unreadable requests — semantically defensible, but every
  validation exception path must be re-mapped, the boundary against "wrong type in the JSON" becomes hard to
  explain, and a missing digest would then be 422.

### Consequences

- Clients parse one shape and switch on `type`.
- The status set is fixed and cannot be changed once clients exist.
- Internal messages never leave the server on a 500; the correlation id is the only handle a client gets.
- Every new failure shape needs a new `type` URI, which is a deliberate, reviewable act.
- 422 stays scarce and meaningful: business rule, fingerprint mismatch, digest mismatch.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D9; F5, F9, F13, F14),
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]],
[[ADRs/ADR-013-object-storage-port-upload-coordinator-and-download-tickets|ADR-013]],
[[ADRs/ADR-014-idempotency-guard-opt-in-per-endpoint|ADR-014]].
