#adr #adr-accepted #architecture #security

## ADR 008: No multi-tenancy in the Base Project; ownership checks only

### Status
Accepted

### Context

One reference project models multi-tenancy and does not enforce it: tenant uniqueness is checked globally,
and a priority delete reorders every tenant's priorities. Tenancy that is modelled but not isolated is a
critical-class defect, and the review called it exactly that. The parent Feature's scope leaves multi-
tenancy out as a user decision (D8), and review decision F3 gave the platform a Row Scope, which is where
visibility lives.

**Evidence:**
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02|BT-R01-02]] — tenancy modelled
with no isolation;
[[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-01|BT-R06-01]] — uniqueness of tenant
data checked globally;
[[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-02|BT-R06-02]] — a priority delete
reorders every tenant's priorities.

### Decision

1. We will implement no tenancy in the Guide's rules or in the Base Project.
2. We will express row visibility as ownership through the Row Scope, and nothing more.
3. We will name exactly one extension point in the Guide: Row Scope composition. A project that needs
   tenants composes them there.
4. We will require its own ADR before tenancy is added to the convention.

**Alternatives rejected:**
- Build tenancy now — a large design with no second project to validate it against, in a workspace where
  the one attempt at it is a critical defect.
- Leave it out without naming an extension point — the next project would invent its own, badly.

### Consequences

- The platform stays smaller and defensible.
- A project that needs tenants gets no tested guidance; it composes at the named point and owns the risk.
- The extension point is named but unproven — no validation run exercises it.
- Ownership is not isolation: two tenants' rows can exist in one table with no enforcement beyond the
  scope rule.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D8),
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]],
[[ADRs/ADR-011-list-api-get-paging-and-post-search|ADR-011]].
