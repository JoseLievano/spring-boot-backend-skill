#adr #adr-accepted #security #architecture #backend

## ADR 006: Identity by resource-server token validation, a `CurrentUser` seam and a removable local Token Issuer

### Status
Accepted

### Context

None of the reference projects enforces authorization; anyone can mint a token in one of them; repositories
are exported over REST in all three; seed admins and fallback secrets are literals; JWT designs trust claims
without checking issuer and never revoke; account flags are ignored at login; CORS origins are hardcoded and
the injected source is ignored; method security is switched on from service classes, so deleting a service
silently disables it. Decision D6 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes the identity seam; review decisions
F6, F7, F13, F14 and F17 shaped its modules.

**Evidence:**
[[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]],
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01|BT-R01-01]] and
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-01|WP-R01-01]] — no enforced authorization anywhere;
[[Docs/backend/Reviews/01-Security-Review#BE-R01-02|BE-R01-02]] — anyone can mint a client token;
[[Docs/backend/Reviews/01-Security-Review#BE-R01-03|BE-R01-03]],
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-05|WP-R01-05]] and
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-07|BT-R01-07]] — repositories
exported over REST;
[[Docs/backend/Reviews/01-Security-Review#BE-R01-04|BE-R01-04]] and
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-07|WP-R01-07]] — seed admin and fallback secrets as
literals;
[[Docs/backend/Reviews/01-Security-Review#BE-R01-05|BE-R01-05]],
[[Docs/backend/Reviews/01-Security-Review#BE-R01-07|BE-R01-07]],
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-08|WP-R01-08]] and
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-10|BT-R01-10]] — token design:
claims-only trust, constant subject, no issuer check, no revocation;
[[Docs/backend/Reviews/01-Security-Review#BE-R01-08|BE-R01-08]],
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-09|WP-R01-09]] and
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-11|BT-R01-11]] — account flags
ignored at login;
[[Docs/backend/Reviews/01-Security-Review#BE-R01-10|BE-R01-10]] and
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-13|WP-R01-13]] — CORS origin hardcoded, injected
source ignored;
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-11|WP-R01-11]] — method security switched on from
service classes;
[[Docs/backend/Reviews/01-Security-Review#BE-R01-11|BE-R01-11]] and
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-03|BT-R01-03]] — literal secrets.

### Decision

1. We will authenticate every request by validating a JWT as a resource server. The framework's token
   decoder is the port — no custom port is added. Two configurations are chosen by `app.identity.mode`:
   `local` (the issuer's public key) and `external` (the provider's key-set URI). Issuer and audience are
   validated in both.
2. We will turn a token into provider-neutral claims through a Claims Mapper port with a local and an
   external adapter.
3. We will let business code see only `CurrentUser` (user id, subject, roles); the system sentinel
   `CurrentUser.system()` is the same type. Every entry point names its actor as an explicit parameter —
   there is no run-as-system switch and no ambient context. Platform housekeeping confined to platform
   tables may not write feature rows. Audit fields are actor labels taken from that parameter.
4. We will make the Token Issuer a removable module: login, refresh, logout. Short-lived access tokens are
   signed with an asymmetric key pair from configuration (no literal, no fallback); opaque refresh tokens
   are stored hashed and rotated; passwords are hashed through a delegating encoder. Account flags are
   honoured at login — the adapter that exposes a user to the framework must override the account-flag
   checks, because they default to "not locked" and "enabled". The module owns the credential and
   refresh-token tables in its own migrations. The first admin is created once through `UserDirectory` from
   configuration-provided credentials. Local admin user management lives and dies with the module.
5. We will make brute-force protection three owner-scoped contracts: the credential failure policy (counter
   and temporary lockout) in the local credential; one uniform error shape for bad credentials and a locked
   account; per-IP request throttling owned by the deployment layer, with password spraying named as a
   residual risk.
6. We will take CORS origins from typed configuration; the security chain consumes the injected CORS
   source; start-up fails on wildcard origins with credentials; the developer origin exists only in the
   `local` profile.
7. We will give each authorization decision one owner: URL rules separate public from authenticated routes
   only, with an explicit public list and an "everything else is authenticated" tail; the download
   redemption route is public by capability; all feature authorization belongs to the Access Policy
   ([[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]]); method security is switched on once,
   on the security configuration class, as a platform-only safeguard. Deny-by-default holds at three
   layers. Repositories are never exported over REST automatically.
8. We will treat migration to an external provider as a runbook: export users and roles, create provider
   accounts, rebind each subject, retire unrebound users, delete the local module, set the mode to
   `external`. A concrete provider integration is out of scope; a fake key-set issuer in tests proves the
   second configuration.

**Depth statement:** `CurrentUser` is the whole identity interface for feature code. The Claims Mapper seam
is real — two adapters plus a test adapter. No token-verification port was added because the framework's
decoder already is one; a wrapper would be a pass-through.

**Mechanism on the current line (Spring Boot 4.1.x, 2026-10):** `JwtDecoder` and `NimbusJwtDecoder` for token
validation, `CorsConfigurationSource` for the injected CORS source, `@EnableMethodSecurity` as the single
platform-only switch, and the `UserDetails` account-flag defaults that must be overridden. The decoder names
were verified against the Spring Security resource-server reference on 2026-09-30; the rest are not
verified by a test until the Base Project is built.

**Alternatives rejected:**
- A hand-written token filter — the reference projects' approach, which is where the token defects live.
- External provider only — user story 27 needs our own login now.
- A custom verification port — the framework already exposes one.
- An in-application per-IP rate limiter — unsound when scaled horizontally; owned by the deployment layer
  instead.

### Consequences

- Switching provider is configuration plus a runbook, not a rewrite.
- The local issuer is security-sensitive code the project owns and must keep minimal and tested.
- Providers differ in how they deliver roles, so the Claims Mapper must accept "no roles in the token".
- Per-IP throttling is not covered by the convention; it is a declared deployment responsibility.
- An entry point without an actor cannot be written.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D6; F6, F7, F13, F14, F17),
[[ADRs/ADR-007-single-user-keyed-by-token-subject|ADR-007]],
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]],
[[ADRs/ADR-004-platform-and-features-package-layout|ADR-004]].
