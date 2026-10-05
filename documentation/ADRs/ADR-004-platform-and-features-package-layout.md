#adr #adr-accepted #architecture #backend #testing

## ADR 004: Package layout `platform` / `features` with enforced dependency rules

### Status
Accepted

### Context

The reference projects put shared code in one `shared/` package and features in `models/`, with no rule
about which may import which. The result: shared tools reach into feature packages, services reach into
other modules, and lazy injection hides mapper dependency cycles until something fails at run time. A layout
without an enforced direction erodes silently. Decision D4 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes the tree; review decisions F6, F7
and F17 added the dependency rules that keep the local identity module removable, the authorization module
single, and the actor provider at the edge.

**Evidence:**
[[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-07|BE-R09-07]] — a shared tool that depends on feature
packages;
[[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-05|WP-R07-05]] — services reaching into other
modules, with lazy constructors hiding mapper cycles;
[[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-05|BT-R10-05]] — lazy injection in 28 files hiding
cyclic mapper dependencies.

### Decision

We will organise code into one package tree with two roots: `<root>.platform.{identity, identity.local,
access, crud, query, errors, storage, idempotency, config}` and `<root>.features.<feature>`, and we will
enforce these rules with architecture tests:

1. `platform` never imports `features`.
2. Nothing outside `platform.identity.local` imports the local token issuer, so it can be deleted.
3. No method-security annotations appear in `features`.
4. The current-user provider is used only at the HTTP edge and in adapters.
5. A feature reaches another feature only through that feature's service, and uses application events for
   side effects the caller need not wait for.
6. Associations to another feature's entity are allowed; writes go through its service.
7. Controllers never return or accept entities.

**Alternatives rejected:**
- Packages by technical layer — the reference layout; it puts every feature's code in five places and
  enforces nothing.
- The reference `shared/` + `models/` layout without enforcement — already shown to erode.
- Separate build modules — heavier; not needed to enforce direction.

### Consequences

- New code has one obvious place, and the layout cannot erode silently.
- The local identity issuer can be deleted without leaving imports behind.
- Every project carries architecture tests as part of its build.
- Cross-feature calls need care to avoid cycles; the service-only rule is the guard.
- This is one package tree, not compile-time isolation: a mistake is caught by the test, not by the compiler.

**Mechanism on the current line (Spring Boot 4.1.x, 2026-10):** ArchUnit is the tool the parent names for
the architecture tests. Not verified by a test until the Base Project is built.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D4; F6, F7, F17),
[[ADRs/ADR-006-identity-current-user-seam-and-removable-local-issuer|ADR-006]],
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]].
