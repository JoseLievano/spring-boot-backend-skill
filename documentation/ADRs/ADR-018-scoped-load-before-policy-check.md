#adr #adr-accepted #architecture #backend #security

## ADR 018: At a row entry point the scoped load comes before the policy check

### Status
Accepted

### Context

[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]] contains three statements that cannot all
hold for an entry point that targets a row (get, update, delete):

- decision 5 — the Access Policy's `check(action, actor, entityOrNull)` holds the rules that depend on
  the entity, and the base consults it once per entry point;
- decision 6 — a row outside the Row Scope reads as 404, not 403;
- decision 8 — the order is: authenticate → policy check (403) → scoped load (404) → precondition →
  mutate.

A policy that is consulted before the row is loaded cannot be given the row, so no entity-conditioned
rule can run on an inherited operation. And an actor whose role is denied the action receives 403 for a
row outside its scope, which decision 6 rules out.
[[ADRs/ADR-013-object-storage-port-upload-coordinator-and-download-tickets|ADR-013]] (decision 7) already
states the other order for the download entry point: scoped load (404), then the policy check. The two
accepted ADRs therefore give opposite orders for entry points the convention calls "ordinary" alike. The
conflict was found while the contract of the CRUD base was written for the Guide.

**Evidence:**
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-03|WP-R01-03]] — inherited operations need only a
login; object-level authorization needs the object;
[[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-07|BT-R03-07]] — no optimistic
locking, which is why the precondition has a place in the order at all.

### Decision

1. We will evaluate an entry point that targets a row in this order: authenticate → load the row through
   the Row Scope (absent: 404) → consult the Access Policy once, with the loaded row (no rule: 403) →
   evaluate the precondition (missing: 428; stale: 412) → run the hooks and mutate.
2. We will consult the policy with no entity at an entry point that has no target row — list, search and
   create — and then proceed.
3. We will use the same order at the download entry point and at the entry points of a feature that opts
   out of the CRUD base.
4. This order replaces the order sentence in decision 8 of ADR 005. Every other decision of ADR 005 stays
   in force, and ADR 005 stays Accepted.

**Alternatives rejected:**
- Keep the policy check before the load — the policy never sees the row on inherited operations, and
  download would follow a different order from update.
- Check twice, once before the load for the role and once after for the row — contradicts "once per entry
  point" and gives a rule two places to be forgotten.
- Load silently, check with the row or nothing, and report 403 before 404 — every policy would have to
  handle a missing row for row actions, and whether a hidden row reads as 404 would depend on how each
  rule is written.

### Consequences

- "A hidden row reads as 404" becomes structural: it is decided before any policy rule runs.
- The policy always receives the row for get, update, delete and download, and nothing for list, search
  and create.
- A caller whose role is denied an action receives 404 for a row it may not see and 403 for a row it may
  see.
- A denied caller costs one scoped query before the denial.
- A reader of ADR 005 alone sees the old order. The index title of this ADR names the relationship, and
  the Guide's rule on the order of checks cites this ADR.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D5; F7, F9, F13),
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]],
[[ADRs/ADR-013-object-storage-port-upload-coordinator-and-download-tickets|ADR-013]],
[[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]].
