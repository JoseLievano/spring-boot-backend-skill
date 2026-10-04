# Active Context

## Current focus
**Findings resolution COMPLETE.** All 17 findings of
[[Bugs/to-do/Review-of-Spring-Boot-Architecture-Guide-and-Base-Project]] are resolved against
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] — 16 Done, F16 Auto-resolved. The parent
Feature is patched for every accepted decision. Nothing is pending.

Next: the user updates [[Memory/brief]] (Step 1.1 wording), then Task 1 of the Feature (ADR system init,
ADR-0001…0017, glossary terms).

## Major decision: D2 was revised (F1)
The Skill is **not** a scaffold and ships **no code artifacts and no Spring app**. It is a project-agnostic
convention in pure markdown — rules, module **contracts** (interfaces, invariants, error modes) and
pseudo-code only. Project initialization is explicitly not the Skill's job, and the Skill is not
Claude-Code-specific. New projects implement the contracts fresh and look up current framework documentation
for API syntax at execution time (so the Skill never teaches deprecated APIs).

`base-project/` remains a workspace deliverable as the **reference implementation** that proves the
convention — never shipped with the Skill, never a copy source.

## Major decision: the execution order changed (F8 + F10)
Phases run **1 → 2 → 4 → 5 → 3 → 6**. The Guide's **contract layer leads** and is gated by F2's blind `GC-R`
review; the **Skill is written next** (D18 amended: "after the Guide exists and its contracts pass the gate");
**run 01** of the Validation Loop is a planned discovery run; only then is **`base-project/` built per area at
the end** against converged contracts, which is where each Guide document's **prose** is finalized. Step
numbers are stable identifiers and are not renumbered; the Task Breakdown is numbered 1–13 in execution order.
Run 01's root-cause field is `guide`/`skill` only.

## Version baseline is now a support policy, not a pin (F11)
Verified 2026-10-03: **every Spring Boot 3.x line is OSS-EOL** (3.5 ended 2026-06-30; 3.4 ended 2025-12-31);
4.1.x is current GA; 4.2 GA is scheduled 2026-11-30. Boot 4 changes the Guide's dependencies (Jackson 3
default `tools.jackson.JsonMapper`; Security 7; modularised auto-configuration). The Guide's floor is "the
currently OSS-supported Spring Boot generation" (today 4.x); the Base Project pins one line — the current GA
at build time, **decided at Task 9**. Version Notes are **evidence-scoped** (`verified on <line>` with a
resolvable citation, or `not verified — current-docs lookup`), enforced by the `guide` validator. Java 21+
unchanged.

## Findings decided in this session (F11–F17)
- **F11 → Done.** Support policy + one tested line + evidence-scoped Version Notes.
- **F12 → Done.** **Frozen evidence packs** — cited files copied whole into `evidence/`; `validation-runs/`
  stays git-ignored and is cleaned after the review + evidence are committed; the `validation` target rejects
  citations into `validation-runs/`.
- **F13 → Done.** **`DownloadTicket` HMAC capability tokens** — ordinary entry point → `policy.check(Action.download)`
  → ticket; one redemption controller verifies MAC+TTL, re-checks existence, 302s to a presigned URL or
  streams. Pinned 404/410; centralized header policy; non-transactional.
- **F14 → Done.** **Three owner-scoped contracts** — credential failure policy (`LocalCredential`), CORS
  (typed config + injected `CorsConfigurationSource`, closing both halves of BE-R01-10), and per-IP
  throttling as **deployment-layer ownership** with password spraying named as residual risk. Matrix scope is
  now mechanical (🔴/🟠 + every 🟡/🟢 from a Security-category review).
- **F15 → Done.** **Rule-coverage ledger** (every applicable `G<NN>-<MM>` cited or `checked — no finding` with
  a frozen-evidence citation; `check_rule_coverage()`; Exit Gate gains "ledger is complete") **plus a
  one-time calibration belt in Task 7** (blind on `backend/`, lower bound on BE-R01-01/02, BE-R02-01 + the F7
  `@PreAuthorize` case; answer key banned from the context).
- **F16 → Auto-resolved** by F1 (the Skill never copies `base-project/`).
- **F17 → Done.** **Explicit-actor model** — `CurrentUser.system()` sentinel; **no `runAsSystem`**; every
  entry point names its actor; system rules live in `<F>AccessPolicy>`; audit is a mandatory parameter.

Pattern: the alternative subagent's proposal was promoted to first-class in six of seven — the recurring move
is the project's own grammar (fail-closed by signature, one owner per decision, checkable evidence).

## Earlier decisions (F1–F10) — still in force
D2 revised (code-free convention skill); F2 blind `GC-R` design review at end of Phase 2 (gate 0 🔴 / 0 🟠);
F3 mandatory `RowScope` (fail-closed by signature, ANDed before paging, invisible → 404); F4 per-entry-point
`@Transactional` + `UploadCoordinator.store` = `propagation = NEVER`; F5 fingerprint = method + path +
canonical non-file fields + declared content digest (never hashes the request body); F6 `UserDirectory` +
rebindable binding + credential/account state split (`User.externalSubject` == token `sub`); F7
`<F>AccessPolicy>` is the single authorization module (`check` + `rowScope`), `authorize` hook removed,
`@PreAuthorize` banned in `features.*`; F8+F10 execution reorder (1→2→4→5→3→6) with D18 amended; F9
Conditional-Write Contract (`ETag` + required `If-Match`, 412/428).

## Recent changes
- **2026-10-03 — F11–F17 decided and patched.** Findings resolution complete. Bug Report Decisions + summary
  table; parent Feature patched for each accepted decision (US 6/7/29/51/53, sections 4/6/8/9/10/12/15,
  Guide rows 04–16, Steps 2.6/3.5/3.7/5.1, Risk Assessment, Testing Decisions, Task Breakdown Tasks 7/8).
- **2026-10-03 — F9 decided and patched.** Conditional-Write Contract.
- **2026-10-03 — F8 + F10 decided and patched (jointly).** Single blast-radius reorder; D18 amended.
- **2026-10-03 — F6 and F7 decided and patched.** Both alternatives promoted to first-class.
- **2026-10-03 — F5 decided and patched.** Option 1 refined.
- **2026-10-02 — F4 decided and patched.** Per-entry-point transactions + `propagation = NEVER`.
- **2026-09-30 — F1, F2, F3 decided and patched.** D2 revised (code-free convention skill).

## Current skill state
| Phase | Status | File |
|-------|--------|------|
| 0 — Project Onboarding and Environment Assessment | Implemented | `references/phase-0-onboarding.md` |
| 1+ — All remaining phases | Not yet designed | To be replaced by the rewritten skill (D18) |

## Next steps
- The user updates [[Memory/brief]] (Step 1.1): replace "scaffolds new Spring Boot APIs" and "Claude Code
  skill (markdown prompt file)" with a project-agnostic convention skill that ships no code artifacts and
  does not initialize projects. The "standalone skill" and "self-contained markdown" constraints are already
  satisfied (F1). `known-issues.md` needs **no** change for F1.
- Then Task 1 of the Feature: ADR system init (`doc-config.json`, `ADRs/ADR-index.md`), ADR-0001…0017 from
  D1–D18 (note D3 is now the support policy, and D18 was amended), glossary terms via the `glossary` CLI.
