#adr #adr-accepted #security #data #backend

## ADR 007: One `User` keyed by the token subject; roles as strings; `UserDirectory` as the single lifecycle contract

### Status
Accepted

### Context

The reference projects model users three different ways: roles stored as ordinals that break when the enum
changes order, roles as free strings with an unused enum that disagrees, usernames unique only per user
kind so login picks an arbitrary match, an inheritance hierarchy for user kinds with conflicting identifier
strategies, and roles accepted from the caller on user creation. Client passwords are derived from e-mail
addresses. Decision D7 of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes the user
model; review decision F6 added the credential/account split, the rebindable subject binding and the single
lifecycle contract.

**Evidence:**
[[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03|BE-R03-03]] and
[[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-01|WP-R06-01]] — roles stored as
ordinals;
[[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-10|BT-R03-10]] — roles as free
strings, the enum unused and in disagreement;
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-06|BT-R01-06]] — usernames unique
only per user kind;
[[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-02|BT-R03-02]] — identifier problems
in the joined user hierarchy;
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-04|BT-R01-04]] — roles accepted from
the caller;
[[Docs/backend/Reviews/01-Security-Review#BE-R01-06|BE-R01-06]] and
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-10|WP-R01-10]] — derived client passwords.

### Decision

1. We will model one provider-neutral `User`: id, external subject (unique), e-mail, display name, a roles
   mirror, and an app-level status (active / disabled).
2. We will hold this invariant: the external subject always equals the token `sub`, in both identity modes.
   Local mode sets `sub` to the user id with no prefix, because the validated issuer already namespaces it.
3. We will store roles as enum names, never ordinals.
4. We will keep per-type profile data in feature entities that reference `User` — no user class hierarchy.
5. We will keep account state on `User` and credentials elsewhere; `User` never holds a password or a key.
6. We will enforce the status on every request, in both identity modes.
7. We will resolve a user by subject, and create it on first request in external mode only. In local mode
   the user must exist before the first login.
8. We will route every user change through `UserDirectory` (create, set status, rebind subject) — the only
   path that creates or changes a user. Rebinding is admin-only and audited, fails with 409 on an
   already-bound subject, and never links automatically on a matching e-mail.
9. We will have one role writer per mode: the local admin surface in local mode, token claims in external
   mode.

**Alternatives rejected:**
- A user hierarchy per user kind — the reference projects' approach; it brought conflicting identifier
  strategies and cascade traps.
- A `local:` subject prefix — the issuer claim already namespaces the subject.
- Automatic linking by e-mail — account takeover by address reuse.
- One role service for both modes — that is two writers for one fact.

### Consequences

- Moving to an external provider keeps every foreign key on the user id; only the subject binding changes.
- Rebinding is a privileged operation whose misuse is account takeover, hence the admin-only, audited,
  409-on-conflict rule.
- In external mode roles are changed in the provider's console, not in this application.
- First-request provisioning writes during a read, so it must be race-safe and outside the caller's
  read-only transaction.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D7; F6),
[[ADRs/ADR-006-identity-current-user-seam-and-removable-local-issuer|ADR-006]],
[[ADRs/ADR-004-platform-and-features-package-layout|ADR-004]].
