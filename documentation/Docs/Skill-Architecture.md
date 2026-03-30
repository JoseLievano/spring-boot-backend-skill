# Skill Architecture

## Overview

The `spring-boot-backend` skill is organized around a single design goal: **load only
what the agent needs for the current task, nothing more**. Every structural decision
follows from this.

When a developer invokes the skill in a new session, the agent should not have to
process instructions about authentication if it is still in onboarding. It should not
have to read entity-generation patterns if it is working on exception handling. Loading
all skill content unconditionally at every session wastes tokens, introduces noise into
the context window, and makes the agent harder to steer — because irrelevant instructions
can pull its behavior in directions that are not appropriate for the current moment.

The architecture solves this with a **phase-based progressive disclosure** model:

- One small entry-point file that is always loaded
- One focused reference file per phase, loaded only when that phase is active
- A routing protocol that determines the active phase from memory, not from assumptions

---

## File Structure

```
spring-boot-skill/
├── skill.md
└── references/
    ├── phase-0-onboarding.md
    └── phase-N-<name>.md     ← one file added per future phase
```

### `skill.md` — the router

This is the only file that is always in context when the skill is active. It is
intentionally lean. Its job is not to provide implementation instructions — it provides
the rules that govern all phases, the registry of available phases, and the logic for
selecting which phase is active.

**Contents:**
- YAML frontmatter with the skill name and trigger description
- Operating Principles — six universal rules that apply regardless of phase
- Phase Registry — a table listing every phase and its reference file
- Phase Selection Protocol — how the agent determines which reference file to load
- Conflict Resolution Protocol — how to handle convention conflicts in any phase
- Enforcement — the rule that onboarding cannot be skipped
- Prohibited Assumptions — the list of things the agent must never infer without evidence

**What must not go into `skill.md`:**
- Phase-specific instructions of any kind
- Implementation steps, code patterns, or generation templates
- Any content that is only relevant during one phase

When a new phase is added, `skill.md` changes in exactly one place: a new row is added
to the Phase Registry table. The body of `skill.md` does not grow.

### `references/phase-N-<name>.md` — phase instruction files

Each phase has exactly one reference file. This file contains everything the agent
needs to execute that phase — the full protocol, all decision rules, all required
outputs. It is self-contained: an agent reading it alongside `skill.md` has everything
it needs for that phase.

The file is loaded on demand. When the Phase Selection Protocol in `skill.md` determines
that a given phase is active, the agent reads that phase's reference file. When the phase
is not active, the file is never loaded.

**Naming convention:** `phase-N-<descriptive-name>.md` where `N` is the phase number
(zero-indexed) and `<descriptive-name>` is a short kebab-case description of the phase.

---

## The Loading Model

There are three layers of loading, each triggered under different conditions:

### Layer 1 — Metadata (always in context)

The frontmatter of `skill.md` (the `name` and `description` fields) is always present
in the agent's available-skills list. This is approximately 100 words. Its only job is
to tell the agent when to trigger the skill.

### Layer 2 — Skill body (loaded when skill triggers)

When the skill is invoked, the full body of `skill.md` is loaded. This is currently
~110 lines covering the universal rules, the phase registry, and the routing logic.
This content is always needed — it governs behavior across all phases — so it is
appropriate to load unconditionally when the skill is active.

### Layer 3 — Phase reference file (loaded on demand)

Only the reference file for the active phase is loaded. All other reference files
remain unloaded. This is the primary token-saving mechanism.

**Example — a mature project in Phase 3 (entities):**
- Layer 1 is always present (~100 words)
- Layer 2 loads `skill.md` body (~110 lines)
- Layer 3 loads only `references/phase-3-entities.md`
- `phase-0-onboarding.md`, `phase-1-auth.md`, `phase-2-shared-components.md` are
  never loaded

As the skill grows to cover more phases, the total potential content grows — but the
per-session token cost stays bounded to `skill.md` plus one reference file.

---

## Phase Selection Protocol

The agent determines the active phase at the start of every session by reading the
memory bank. This is the authoritative source of phase state — not defaults, not
assumptions, not the user's message alone.

```
Session start
    │
    ▼
Read memory bank (memory-bank skill)
    │
    ├─── No onboarding record found ──► Active phase: 0
    │                                   Load: references/phase-0-onboarding.md
    │
    └─── Onboarding record found ──────► Determine active phase from work context
                                         Load: references/phase-N-<name>.md
```

When the user requests a phase transition (e.g., "let's move on to authentication"),
the agent updates the active phase in memory and loads the new phase's reference file.

### Why memory — not the user message — drives phase selection

If phase selection were driven by the user message alone, a message like "add an entity"
would load the entities phase reference file even if onboarding had never been completed.
The agent would proceed without the project context that makes its guidance safe. Reading
memory first enforces the onboarding gate and keeps the phase state consistent across
sessions.

---

## Phase Registry

The registry lives in `skill.md` as a markdown table. Every phase has one row.

| Phase | Name | Reference file | Status |
|-------|------|---------------|--------|
| 0 | Project Onboarding and Environment Assessment | `references/phase-0-onboarding.md` | Active |

As new phases are designed and implemented, they are added here. The status column
distinguishes phases that are fully specified and ready to use from ones that are
planned but not yet written.

---

## Phase 0 — Project Onboarding and Environment Assessment

This is the first and currently only implemented phase. It is mandatory: no other
phase work may begin until Phase 0 is complete.

**Reference file:** `references/phase-0-onboarding.md`

**Purpose:** Establish verified project context before any code is generated or any
architectural advice is given. The phase produces a documented snapshot of:
- The technology stack (versions, build tool, database, security, testing)
- The structural inventory (packages, entities, services, controllers, DTOs, etc.)
- A maturity classification (bootstrap-only, partially implemented, mature, non-standard)
- An alignment classification (how close the project is to the target architecture)
- A separation of facts, inferences, and unknowns
- Recommended safe next actions

**Freshness check:** At session start, Phase 0 checks whether a prior onboarding
record exists in memory. If it does, it spot-checks three high-signal files against
the record (`pom.xml` or `build.gradle`, `application.yml` / `application.properties`,
root package structure). If all signals match memory, Phase 0 is considered current and
is not re-run. If any signal differs, only the affected sections are re-run. This avoids
full re-onboarding on every session while still catching meaningful project changes.

**Re-onboarding commands:** The user can force a full re-run with:
- `re-onboard project`
- `refresh project assessment`
- `rebuild project context`

---

## Adding a New Phase

To add a new phase to the skill:

**Step 1 — Write the reference file.**
Create `spring-boot-skill/references/phase-N-<name>.md`. The file must be
self-contained: an agent reading it alongside `skill.md` should have everything it
needs for that phase. Include:
- A clear statement of the phase's purpose
- Any preconditions (e.g., "Phase 0 must be complete")
- The full execution protocol, in agent-facing imperative language
- Required outputs and how to document them
- Phase completion criteria

**Step 2 — Add a row to the Phase Registry in `skill.md`.**
Add one row to the Phase Registry table with the phase number, name, reference file
path, and status. No other change to `skill.md` is needed.

**Step 3 — Update the Phase Selection Protocol if needed.**
If the new phase introduces a new transition condition (beyond "user requests it"), add
the condition to the Phase Selection section of `skill.md`.

**Step 4 — Update the memory bank and this document.**
Add the new phase to the Phase Registry table in this file. Update
`documentation/Memory/architecture.md` and `documentation/Memory/progress.md`.

---

## Planned Future Phases

These phases are anticipated but not yet designed or written. Their final names,
content, and ordering may change as the reference project analysis progresses.

| Anticipated phase | Notes |
|-------------------|-------|
| Authentication and authorization | JWT, session, OAuth2 — depends on stack fingerprint from Phase 0 |
| Shared components and base classes | Common response wrappers, base entities, utility classes |
| Entity and persistence layer | JPA entities, repositories, schema migration |
| Service layer | Transactional patterns, business logic conventions |
| Controller and API layer | REST conventions, request/response mapping, validation |
| DTO and mapping strategy | MapStruct, record DTOs, mapping conventions |
| Exception handling | `@ControllerAdvice`, error response shape, exception hierarchy |
| Testing | Unit and integration test patterns, Testcontainers setup |

The analysis of the three reference projects (auth-server, BugTracker, wpmanager) will
determine the exact shape and ordering of these phases. Patterns chosen for each phase
must be grounded in at least two of the three reference projects.

---

## Design Principles Summary

| Principle | Rationale |
|-----------|-----------|
| `skill.md` stays lean | Universal rules only; never grows with new phases |
| One reference file per phase | Clear ownership; no cross-phase content bleeding |
| Phase files are self-contained | Agent never needs to cross-reference two phase files |
| Memory drives phase selection | Enforces onboarding gate; maintains cross-session consistency |
| Only the active phase is loaded | Token cost bounded regardless of skill size |
| New phase = new file + one table row | Minimal, predictable change surface for extension |

---

## Related Documents

- [[Docs/Skill-Directory-Convention]] — where skill files live during development and how to migrate
- [[Features/to-do/Agent-Project-Onboarding-First-Instructions]] — full feature spec for Phase 0
- [[Features/to-do/Spring-Boot-Skill-Creation]] — overall skill creation roadmap
