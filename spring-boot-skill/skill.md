---
name: spring-boot-backend
description: >
  Use this skill when building a new Spring Boot REST API backend or transforming an
  existing Spring Boot project toward a specific, opinionated target architecture.
  Triggers include: scaffolding Spring Boot APIs, adding backend modules (auth,
  persistence, exception handling, DTOs, tests), refactoring an existing Spring Boot
  backend to meet architectural standards, or any task that modifies the backend
  layer of a Spring Boot application. Do NOT trigger for general Java questions,
  non-Spring projects, or frontend work.
---

# Spring Boot Backend Skill

This skill builds new Spring Boot REST API projects or transforms existing ones toward
a defined, opinionated target architecture. It does not infer. It does not generate
before it inspects. It evaluates existing conventions against the target architecture
and transforms what conflicts.

---

## Operating Principles

These rules apply at all times, regardless of phase. They cannot be overridden.

1. **Repository-first reasoning.** Never infer versions, stack choices, or architecture
   when the repository can answer those questions. All technical facts must be grounded
   in evidence from project files.

2. **Onboarding before everything.** No architecture advice, code generation, dependency
   changes, or structural modifications may occur until onboarding is complete.
   This rule is not negotiable. See [Enforcement](#enforcement).

3. **Target architecture is the authority.** Existing conventions are inventoried and
   evaluated — not preserved by default. What aligns is extended. What conflicts is
   transformed. What is absent is built.

4. **Documentation and memory are part of the workflow.** Phase outputs are written
   using the `documentation-management` skill and the `memory-bank` skill. Ad-hoc
   file writes outside those interfaces are not permitted for phase outputs.

5. **Explicit uncertainty.** Facts, inferences, and unknowns must be separated in all
   outputs. Silent assumption resolution is prohibited.

6. **Convention conflicts require confirmation.** When a requested change conflicts
   with an established project convention, surface the conflict and ask for confirmation
   before proceeding. See [Conflict Resolution Protocol](#conflict-resolution-protocol).

---

## Phase Registry

Each phase has a dedicated reference file. **Load only the reference file for the
active phase.** Do not load reference files for phases not currently in use.

| Phase | Name | Reference file | Status |
|-------|------|---------------|--------|
| 0 | Project Onboarding and Environment Assessment | `references/phase-0-onboarding.md` | Active |

> Future phases (authentication, shared components, entities, etc.) will be added
> here as new rows. Each will have its own reference file.

---

## Phase Selection

At the start of every session, determine the active phase before doing anything else:

1. Read the memory bank using the `memory-bank` skill.
2. Check whether a completed onboarding record exists.
   - **No record found** → active phase is Phase 0. Load `references/phase-0-onboarding.md`
     and follow its instructions.
   - **Record found, onboarding complete** → active phase is determined by current work
     context. Load only the reference file for that phase.
3. Load **only** the reference file for the active phase. Do not preload other phases.
4. Follow the instructions in that reference file.

When the user explicitly requests a phase transition (e.g., "let's move to building
authentication"), update the active phase in memory, then load the corresponding
reference file.

---

## Conflict Resolution Protocol

When a requested change conflicts with an established project convention:

**Default behavior:**
1. Surface the conflict explicitly: name the convention, describe the deviation, and
   cite the project evidence that establishes it.
2. Ask for confirmation before making any changes.
3. Proceed only after the user confirms.

**Override behavior (`--override-convention` or natural language equivalent):**
Recognized forms: the flag `--override-convention`, or phrases such as
"I know this deviates from conventions", "override the convention here",
"force this even if it conflicts".

When an override is present:
1. Skip the confirmation step and proceed.
2. Log the convention deviation in the session output so the change is visible.
3. Never make a convention-breaking change silently, even with an override.

Note: override signals do not apply to the onboarding gate. See [Enforcement](#enforcement).

---

## Enforcement

**Onboarding is mandatory and cannot be skipped.**

If the user asks to skip onboarding or proceed directly to implementation:
1. Decline to proceed with any implementation work.
2. Explain that onboarding is a non-negotiable precondition for this skill.
3. Resume or complete Phase 0 before continuing.

Without onboarding, there is no basis for determining what the project state is, what
aligns with the target architecture, or what work is safe to perform.

---

## Prohibited Assumptions

Never assume any of the following without file evidence:

- Spring Boot or Java version
- Maven as the build tool (check for Gradle first)
- JPA/Hibernate as the persistence mechanism
- Presence or absence of authentication
- Any specific database engine
- Package naming standards
- That the agent is free to restructure the project
- That `application.yml` is used (may be `application.properties`)
- That a Maven wrapper or Gradle wrapper is present
