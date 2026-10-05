#adr #adr-accepted #architecture #backend

## ADR 003: Version baseline is a support policy, not a pin

### Status
Accepted

### Context

Two reference projects run Spring Boot 3.4.1 and one runs 2.7.0. The 2.7.0 project is past open-source end of
support, and as of 2026-10-04 every 3.x line is too. The dated facts, re-read from
`https://api.spring.io/projects/spring-boot/generations` on 2026-10-04: 3.4.x open-source support ended
2025-12-31; 3.5.x ended 2026-06-30 and is the terminal 3.x minor; 4.0.x ends 2026-12-31; 4.1.x was released
2026-06-30 and is supported until 2027-07-31; 4.2.x is scheduled for 2026-11-30. Spring Boot 4 changes
things this convention depends on — a new default JSON stack, a new security generation, modularised
auto-configuration — so a hard-coded major would be wrong within a year. Decision D3 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] records the policy; review decision F11
replaced the original pin with it and scoped the Version Notes.

**Evidence:**
[[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-01|BT-R08-01]] — a project
running an end-of-support line;
[[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-02|BT-R08-02]] — the
namespace and code style of the old generation;
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-14|BT-R01-14]] — pre-6.x security
configuration style;
[[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-09|BT-R08-09]] — an
unmaintained build plugin from the same era.

### Decision

1. We will write Guide rules and module contracts as version-neutral behaviour; a rule names a product or a
   standard (PostgreSQL, Flyway, MapStruct, RFC 9457, JWT, JWKS) but never a framework class.
2. We will set the floor at the currently open-source-supported Spring Boot generation — 4.x as of 2026-10 —
   and the Guide will never endorse an end-of-support line. Java 21 or newer.
3. We will have the Base Project pin exactly one line: the current general-availability line at build time,
   decided at Task 9 under this policy.
4. We will put version-specific detail only in evidence-scoped Version Notes
   ([[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]]), never in rule text.

**Alternatives rejected:**
- "Spring Boot 3+, Java 21+" (the original D3) — every 3.x line is end-of-support, so the floor would be a
  line the Guide must not endorse.
- Pin 4.1.x in the rules — wrong within a year, and it is the BT-R08-01 failure in document form.
- Test several lines — the cost is multiplied by the Validation Loop, and the convention is behavioural, so
  it does not need per-line proof.

### Consequences

- A framework upgrade changes Version Notes, not rules; the rules survive.
- The policy must be re-evaluated against dated facts, so someone re-reads the support API when a line
  changes.
- `not verified` Version Notes exist until the Base Project is built, and their count is reported in
  `Guide-Index.md`.
- Agents must look up current framework documentation at execution time instead of recalling API names.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D3; F11),
[[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]],
[[ADRs/ADR-002-code-free-skill-and-reference-base-project|ADR-002]].
