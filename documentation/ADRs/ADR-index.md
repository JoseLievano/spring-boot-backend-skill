# ADR Index

## Status Legend
- **Proposed:** The decision is under consideration
- **Accepted:** The decision has been agreed upon and is in effect
- **Deprecated:** The decision is no longer relevant (the thing it was about no longer exists)
- **Superseded:** The decision has been replaced by a newer ADR

ADRs are cited as `ADR-NNN` (three digits, the filename prefix). An accepted ADR is never edited: a changed
decision is a new ADR that supersedes the old one.

| ADR | Title | Status | Date | Superseded By |
|-----|-------|--------|------|---------------|
| ADR 1 | The Guide is the source of truth; the reference projects are evidence | Accepted | 2026-10-04 | — |
| ADR 2 | The Skill is a code-free convention; the Base Project is the workspace reference implementation | Accepted | 2026-10-04 | — |
| ADR 3 | Version baseline is a support policy, not a pin | Accepted | 2026-10-04 | — |
| ADR 4 | Package layout `platform` / `features` with enforced dependency rules | Accepted | 2026-10-04 | — |
| ADR 5 | Deepened generic CRUD base with hooks, three DTO shapes and one Access Policy per feature | Accepted | 2026-10-04 | — |
| ADR 6 | Identity by resource-server token validation, a `CurrentUser` seam and a removable local Token Issuer | Accepted | 2026-10-04 | — |
| ADR 7 | One `User` keyed by the token subject; roles as strings; `UserDirectory` as the single lifecycle contract | Accepted | 2026-10-04 | — |
| ADR 8 | No multi-tenancy in the Base Project; ownership checks only | Accepted | 2026-10-04 | — |
| ADR 9 | Unchecked domain exceptions mapped to RFC 9457 problem details | Accepted | 2026-10-04 | — |
| ADR 10 | PostgreSQL, Flyway migrations and Testcontainers | Accepted | 2026-10-04 | — |
| ADR 11 | List API with `GET` paging, `POST /search` and an owned page response | Accepted | 2026-10-04 | — |
| ADR 12 | MapStruct mapping with unmapped-target errors | Accepted | 2026-10-04 | — |
| ADR 13 | Object storage port with S3-compatible and local adapters, an Upload Coordinator and Download Tickets | Accepted | 2026-10-04 | — |
| ADR 14 | Idempotency Guard, opt-in per endpoint | Accepted | 2026-10-04 | — |
| ADR 15 | Validation Loop protocol and Exit Gate | Accepted | 2026-10-04 | — |
| ADR 16 | Guide document format, rule IDs and citation grammar | Accepted | 2026-10-04 | — |
| ADR 17 | Skill architecture as a code-free, contract-first convention | Proposed | 2026-10-04 | — |
| ADR 18 | At a row entry point the scoped load comes before the policy check (replaces the order sentence of ADR 5, decision 8) | Accepted | 2026-10-04 | — |
