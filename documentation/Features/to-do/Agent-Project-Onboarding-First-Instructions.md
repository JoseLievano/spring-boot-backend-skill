#feature #high #new-feature

# Agent Project Onboarding First Instructions

## Overview
Define the first instruction block for the `spring-boot-backend` skill as a mandatory
project onboarding and environment assessment phase. This skill is not a general-purpose
Spring Boot assistant — it exists to build new projects or transform existing ones
toward a specific, opinionated target architecture. The onboarding phase is how the
agent establishes where the project currently stands relative to that target before any
implementation work begins.

This feature is not about generating code. It is about establishing the minimum
agent-facing operating procedure required to assess the current project state, measure
its distance from the target architecture, and determine what work is needed before any
implementation guidance is produced.

The core design principle is simple:

An agent should never reason from defaults when the repository can be inspected.
The first instructions must force the agent to discover the project state, document
what it found, measure alignment with the target architecture, separate facts from
inferences, and only then continue into generation or transformation work.

## Description
The skill will be used against two broad project states:

1. A bootstrap-only Spring Boot project.
This is a valid Spring Boot repository with minimal starter structure and little or no
domain code yet. It may only contain the basic generated files, a `pom.xml`, minimal
configuration, and starter dependencies.

2. An already active backend project.
This may already contain entities, controllers, services, repositories, database
migrations, authentication and authorization systems, exception handling, DTOs, mapping
logic, tests, and established package conventions.

These two states require fundamentally different behavior from the agent. A bootstrap
project starts from a clean slate and can be built directly toward the target
architecture. An active project must be assessed first — the agent needs to understand
what already exists, identify what aligns with the target architecture, and determine
what needs to be restructured or replaced before any implementation work proceeds.

That is why the first instructions for the skill must not begin with generation,
module selection, package recommendations, or dependency advice. They must begin with
onboarding — specifically, with measuring the current project state against the target
architecture.

## Scope
This feature covers the first instruction block of the skill only:
- project-state discovery
- documentation bootstrap or validation
- memory bank bootstrap or validation
- stack fingerprinting
- structural inventory
- maturity assessment
- risk and uncertainty logging
- recommendation of safe next actions

This feature does not yet define:
- code generation templates
- controller/service/repository generation flows
- security implementation recipes
- persistence implementation patterns
- version-specific coding conventions

Those later parts depend on the onboarding output.

## Affected systems
- `spring-boot-skill/` — the future skill content must begin with this operating mode
- `documentation/` — onboarding findings must be documented in project docs
- `documentation/Memory/` — onboarding must initialize or update persistent context
- [[Spring-Boot-Skill-Creation]] — this feature refines Phase 2 skill design

## Impact analysis
This is a foundational feature. If the first instructions are weak, the entire skill
becomes unreliable.

Without an onboarding-first design, the agent will tend to make unsafe assumptions:
- assume a Spring Boot version based on training priors instead of the repository
- assume Maven wrapper or Gradle use without checking
- assume `application.yml` versus `application.properties`
- assume JPA and Hibernate when the project may use JDBC, MyBatis, or another pattern
- assume MySQL when the project may use PostgreSQL or another database
- assume JWT security when security may be session-based, OAuth2-based, or absent
- assume package structure and naming conventions that conflict with the project
- attempt to scaffold modules that already exist in a mature codebase

These are not cosmetic mistakes. They create low-trust output, architectural drift,
and a higher chance of breaking existing backend conventions.

An onboarding-first design improves the skill in four critical ways:

1. It forces repository-grounded reasoning.
The agent begins with evidence from code and configuration, not from generic Spring
Boot habits.

2. It creates reusable project memory.
Once the onboarding result is written to documentation and the memory bank, later
tasks become faster and more consistent.

3. It adapts behavior to project maturity.
A new project and a mature project must not receive the same kind of guidance.

4. It increases agent readability.
A skill written for agents should be explicit about sequencing, decision gates,
required evidence, and prohibited assumptions. This feature defines that first gate.

## Risk assessment
If this feature is implemented poorly, the main risks are:
- onboarding becomes a vague summary instead of a concrete inspection protocol
- the agent collects versions but misses architectural signals
- the instructions become too human-oriented and not operational enough for an agent
- onboarding is defined as optional, causing later instructions to bypass it
- documentation initialization is treated as unconditional recreation, which risks
  overwriting or duplicating existing project docs
- the memory bank is treated as a one-time setup instead of a persistent context layer

The strongest mitigation is to phrase the first instructions as an execution protocol,
not as general guidance. The skill should tell the agent exactly what to inspect, what
to document, what to classify, what to avoid, and what conditions must be met before
moving on.

---

## Implementation architecture

### Changes required

#### 1. Mandatory onboarding phase at skill start
**Purpose:** Make project discovery the non-optional first behavior of the skill.
**Changes:** The first section of the skill should instruct the agent to inspect the
repository before making any technical recommendations or edits. It should explicitly
state that the agent must not infer versions, stack choices, or architecture when the
repository can answer those questions.

#### 2. Documentation bootstrap-or-validate behavior
**Purpose:** Ensure the skill works for both undocumented and already-documented projects.
**Changes:** The instructions must say:
- if `documentation/` does not exist, initialize documentation using the
  documentation-management skill
- if `documentation/` already exists, read its configuration and use its existing
  structure instead of recreating it
- if `documentation/Memory/` does not exist, initialize the memory bank
- if `documentation/Memory/` already exists, read all core files and update them as needed

This is materially better than saying "create documentation first", because many real
projects will already have docs, and the agent must preserve continuity.

#### 3. Stack fingerprint discovery
**Purpose:** Record the concrete technical baseline of the target project.
**Changes:** The onboarding instructions should require the agent to discover and
document at minimum:
- Java version
- Spring Boot version
- build tool and wrapper presence
- dependency management conventions
- database engine and connection driver
- schema migration tooling
- security/authentication stack
- testing stack
- configuration file style and profile usage
- major integrations and infrastructure dependencies visible in code/config

This step should require evidence from project files such as `pom.xml`, `build.gradle`,
`application.yml`, `application.properties`, dependency declarations, migration folders,
and security/configuration classes.

#### 4. Structural inventory and maturity classification
**Purpose:** Make the agent understand not only the stack, but also the current state
of implementation.
**Changes:** The onboarding instructions should require the agent to inventory:
- modules and subprojects
- package structure
- entities and persistence layer shape
- repositories and data access style
- service layer patterns
- controllers and API surface
- DTOs, mapping strategy, and validation usage
- security configuration and auth flows
- exception handling strategy
- testing structure and coverage clues

The result should classify the project along two dimensions:

**Maturity state** — how much implementation exists:
- bootstrap-only
- partially implemented backend
- mature backend with established conventions
- non-standard layout needing special handling

**Alignment state** — how close the project is to the target architecture:
- fully aligned — all present components match the target architecture
- partially aligned — some components match, others conflict or are absent
- misaligned — existing implementation conflicts significantly with the target
- unknown — insufficient signals to assess alignment; requires clarification

Both dimensions must be recorded. Maturity tells the agent how much work exists.
Alignment tells the agent how much of that work needs to change.

#### 5. Facts versus inferences section
**Purpose:** Reduce hallucination and make later reasoning auditable for the agent.
**Changes:** The onboarding output should explicitly separate:
- facts found directly in the repository
- inferences based on repository signals
- unknowns that still need confirmation

This is one of the most important additions for agent-focused readability. A human may
infer missing context naturally, but an agent benefits from rigid boundaries between
verified state and assumptions.

#### 6. Safe next-step recommendation block
**Purpose:** Turn onboarding from passive analysis into an actionable gate for later work.
**Changes:** At the end of onboarding, the agent should recommend which actions are safe,
framed around the path to the target architecture:
- build missing components directly to the target architecture (for bootstrap or absent areas)
- transform conflicting components toward the target architecture (for misaligned areas)
- extend already-aligned components following target architecture conventions
- document gaps or conflicting areas before making changes
- ask targeted clarification questions if the alignment state is unknown or contradictory

This creates a bridge from discovery to execution without letting the agent jump ahead.

#### 7. Multi-module project detection and handling
**Purpose:** Prevent the agent from treating a multi-module Maven reactor or Gradle
composite build as a single-module project.
**Changes:** The onboarding instructions should detect:
- presence of a parent `pom.xml` with `<modules>` declarations (Maven)
- presence of a `settings.gradle` or `settings.gradle.kts` with `include` directives (Gradle)
- the relationship between modules: shared dependencies, BOM inheritance, module roles
- which module is the application entry point versus shared libraries or config modules

If a multi-module layout is detected, the agent must document it as a structural
fact and adjust all subsequent inventory and convention detection to operate at the
correct module level before any module-level recommendations are made.

#### 8. Agent failure and contradictory signal handling
**Purpose:** Define what the agent must do when it encounters unreadable, malformed,
or contradictory project signals during onboarding.
**Changes:** The onboarding instructions must specify that the agent:
- logs unreadable or missing expected files as high-priority unknowns
- treats contradictory signals (e.g., Spring Boot 2.x in `pom.xml` but 3.x-style
  imports in source code) as requiring explicit user clarification before proceeding
- must not silently resolve contradictions by picking one signal over another
- must not proceed into generation or modification work when critical signals are
  unresolvable without user input
- must surface the unresolved contradiction clearly in the onboarding output, not
  bury it in an unknowns list

#### 9. Scope and depth limits during inspection
**Purpose:** Keep onboarding bounded and consistent on large codebases.
**Changes:** The onboarding instructions should define inspection priority tiers:
- **Always fully read:** `pom.xml` / `build.gradle`, `settings.gradle`, `application.yml`
  / `application.properties`, all profile-specific config files, root package structure,
  and security/persistence configuration classes
- **Sample representatively:** one controller, one service, one entity, one repository,
  one exception handler — enough to detect conventions, not exhaustive reading
- **Flag scope gaps:** for large codebases, document what was inspected and explicitly
  note any areas not covered so the agent and user know where confidence is lower

#### 10. Integration interface with documentation-management and memory-bank skills
**Purpose:** Make the dependency on both skills explicit and operational, not assumed.
**Changes:** The onboarding instructions must specify:
- `documentation/` initialization and validation are performed using the
  `documentation-management` skill, not by free-form file creation
- `documentation/Memory/` initialization and updates are performed using the
  `memory-bank` skill
- the execution sequence is: documentation bootstrap-or-validate first, then
  memory bank bootstrap-or-validate, before any inspection findings are written
- onboarding findings are written using each skill's own update flow, not as
  ad-hoc file writes outside the skill interface

---

## What the first instructions should include

### Required operating principles
- State that the agent is not responsible for initial project generation or starter
  dependency selection.
- State that the agent receives an existing repository in unknown maturity state.
- State that the agent must inspect the repository before proposing architecture,
  dependencies, modules, or implementation steps.
- State that documentation-management and memory-bank workflows are part of onboarding,
  not optional add-ons.
- State that the target architecture is the authority. Existing project conventions are
  inventoried and evaluated against it, not preserved by default.
- State that the agent must identify what aligns with the target architecture, what
  conflicts with it, and what is absent. Conflicting or absent elements are candidates
  for transformation, not preservation.

### Required onboarding sequence
1. Inspect the repository structure.
2. Determine whether project documentation exists.
3. Initialize or validate `documentation/`.
4. Initialize or validate `documentation/Memory/`.
5. Read memory bank files if present.
6. Discover and record: Java version, Spring Boot version, build tool and wrapper
   presence, dependency management approach, database engine and driver, schema
   migration tooling, security stack, testing framework, and configuration file style.
7. Inventory and classify: module layout (including multi-module detection), package
   structure, entities, repositories, data access style, service layer, controllers,
   DTOs and mapping strategy, security config, exception handling, and test structure.
   Assign a maturity state from the defined classification.
8. Document findings.
9. Separate facts, inferences, and unknowns.
10. Recommend safe next actions.

### Required output characteristics
- concise but operationally complete
- evidence-driven
- explicit about uncertainty
- sequenced in execution order
- optimized for agent compliance, not narrative explanation
- written as instructions and decision gates, not as prose essays

### Required documentation outputs from onboarding
- current project state summary
- technology/version summary
- maturity state and alignment state classification
- architecture and module inventory
- components aligned with the target architecture
- components conflicting with the target architecture and transformation notes
- components absent from the target architecture and build notes
- identified gaps or risky unknowns
- recommended safe next tasks toward the target architecture

---

## What should be avoided in the first instructions

### Instructions that should not appear early
- "Start by generating the standard package structure"
- "Choose the best database and set up persistence"
- "Add Spring Security and JWT authentication"
- "Create the base modules needed for the project"
- "Use the latest Spring Boot and Java versions"
- "Scaffold entities, repositories, services, and controllers"

These are bad first instructions because they assume a greenfield workflow. The user has
already defined that the skill may receive an active project with existing choices.

### Assumptions that should be explicitly forbidden
- assuming framework versions without inspection
- assuming Maven instead of checking for Gradle
- assuming JPA/Hibernate without inspecting dependencies and code
- assuming authentication exists, or assuming it does not
- assuming one database engine from habit
- assuming package naming standards from generic examples
- assuming the agent is free to restructure the project

### Human-oriented wording to avoid
- long explanatory prose that does not change agent behavior
- motivational language
- vague instructions like "understand the project first"
- subjective wording like "use best practices" without defining what to inspect
- recommendations that depend on unstated judgment but provide no decision rule

For an agent, readability is not about friendliness. It is about precise execution.
The first instruction block should read like a protocol.

### Destructive or continuity-breaking instructions to avoid
- always recreate documentation
- always initialize the memory bank from scratch
- overwrite agent-maintained memory files without checking existing state
- replace security, persistence, or module structure before onboarding completes
- normalize the project into a preferred architecture before documenting current reality

These are especially harmful in mature backends, where preservation of local conventions
is more important than generic structural purity.

---

## Should onboarding be a command?

Onboarding should be mandatory behavior, not only a command. Making it command-only
creates a failure mode where the agent or user skips it and jumps directly into
implementation work. Onboarding is not a special feature — it is the safety gate for
all other features.

An explicit command is still useful as a re-entry point: forced re-onboarding after
major repository changes, a manual refresh when documentation or memory bank state is
stale, or regenerating the project assessment after a migration.

If a command is added, it should be named to imply the behavior already exists:
- `re-onboard project`
- `refresh project assessment`
- `rebuild project context`

The skill should encode onboarding in two layers:
1. **Core instruction layer** — first instructions always require onboarding before
   architecture advice, generation, or modification work.
2. **Optional command layer** — an explicit command re-runs onboarding when the
   repository state has changed or the user wants the assessment refreshed.

---

## Recommended shape of the first instruction block

The first instruction block should be named:

`Project Onboarding and Environment Assessment`

This tells the agent exactly what the block is for: inspect, assess, document,
classify, and only then proceed. The behavioral goals of this block are:
- detect project maturity and assign a classification state
- establish documentation continuity via the documentation-management skill
- establish memory continuity via the memory-bank skill
- capture verified versions and stack choices from repository evidence
- inventory architecture already in place, including multi-module layout
- preserve local conventions over generic Spring Boot preferences
- prevent premature code generation until onboarding is complete
- create a stable, documented base for all later skill instructions

---

## Implementation steps

### Phase 1: Define the first instruction contract
- [ ] Write the onboarding-first section for the skill in agent-focused language
- [ ] Define explicit preconditions before any generation or modification flow
- [ ] Define the required inspection targets and output structure

### Phase 2: Integrate documentation and memory behavior
- [ ] Add documentation bootstrap-or-validate instructions
- [ ] Add memory-bank bootstrap-or-validate instructions
- [ ] Define how onboarding writes and updates persistent project context

### Phase 3: Decide command handling
- [ ] Make onboarding mandatory in the default execution path
- [ ] Decide whether to add a manual re-onboarding command
- [ ] If added, document it as a refresh action, not the main entrypoint

### Phase 4: Connect onboarding to later skill sections
- [ ] Make later generation instructions depend on onboarding outputs
- [ ] Add rules that preserve existing project conventions by default
- [ ] Ensure later sections do not silently reintroduce forbidden assumptions

### Phase 5: Resolve open design decisions
- [ ] Decide conflict resolution protocol (Decision 1)
- [ ] Decide onboarding freshness and delta logic (Decision 2)
- [ ] Decide maturity classification granularity (Decision 3)
- [ ] Decide user interruption stance (Decision 4)
- [ ] Update implementation instructions to reflect each decision

---

## Potential issues / risks
- The onboarding section may become too large if it mixes protocol and explanation.
- The skill may still drift into human-oriented prose unless each step is phrased as a
  required agent action.
- Later sections may accidentally bypass onboarding unless the dependency is explicit.
- If version discovery is underspecified, the agent may still rely on defaults.
- If project maturity classification is vague, the agent may treat a mature project like
  a starter template.

---

## Open design decisions

The following questions require a decision before implementation can be finalized.
Each should be resolved and the answer recorded in this document.

### Decision 1 — Conflict resolution protocol

**Decision:** Warn and ask for confirmation. Support a convention override mechanism
to allow the user to pre-signal intent and skip the confirmation step.

**Default behavior — no override signal present:**
When the agent detects that the user's request conflicts with an established project
convention, it must:
1. Surface the conflict explicitly: name the convention, describe the deviation, and
   state which project evidence establishes the convention.
2. Ask for confirmation before making any changes.
3. Only proceed after the user confirms.

**Override behavior — override signal present:**
If the user includes an override signal in their request, the agent must:
1. Skip the confirmation step and proceed with the request.
2. Still log the deviation in the session output so the change is visible and auditable.
3. Never make a convention-breaking change silently, even with an override signal.

**Override signal formats:**
The agent should recognize two forms:
- **Explicit flag:** `--override-convention` anywhere in the user's message
- **Natural language equivalents:** phrases such as "I know this deviates from
  conventions", "override the convention here", "force this even if it conflicts",
  or similar clear statements of informed intent

Both forms carry the same meaning: the user already knows about the deviation and
has made the decision. The agent's job in that case is to execute, not to gatekeep.

**Rationale:**
This design respects user authority while keeping the agent honest about architectural
drift. The flag pattern mirrors the mental model of `--force` in CLI tools — it is not
about bypassing safety, it is about communicating that the user has already weighed the
tradeoff. Without this mechanism, the agent's confirmation step would become friction
for experienced users who already know the project and are making intentional choices.

---

### Decision 2 — Onboarding freshness and delta logic

**Decision:** Freshness check first. The agent reads existing memory, spot-checks a
small set of high-signal files, and only re-runs the portions of onboarding that
appear stale.

**Freshness check protocol:**
At the start of each session, the agent must:
1. Read the memory bank and determine whether a prior onboarding exists.
2. If no prior onboarding exists, run the full onboarding sequence.
3. If a prior onboarding exists, check a defined set of high-signal files against
   what is recorded in memory:
   - `pom.xml` or `build.gradle` — detects version or dependency changes
   - `application.yml` / `application.properties` — detects config or profile changes
   - root package structure — detects new modules or structural shifts
4. If all checked signals match memory, skip full onboarding and proceed to work.
5. If any signal differs from memory, re-run only the affected onboarding sections
   and update memory with the new findings before proceeding.

**Rationale:**
Full re-onboarding on every session is too expensive on large codebases and creates
unnecessary friction for ongoing work. Skipping onboarding entirely risks silent
context drift. The freshness check gives the best tradeoff: fast for stable projects,
self-correcting when the repository changes, and without requiring the user to manage
when onboarding runs. The high-signal file set is intentionally small — these files
are the ones most likely to reflect a meaningful change in stack, structure, or config.

---

### Decision 3 — Maturity classification granularity

**Decision:** Keep the four broad states. Precision is provided by combining the
state label with the structural inventory findings, not by multiplying states.

**Classification states (unchanged):**
- bootstrap-only
- partially implemented backend
- mature backend with established conventions
- non-standard layout needing special handling

**How classification output works:**
The maturity state label sets the agent's general operating mode. The structural
inventory (section 7 of the onboarding sequence) provides the specific detail. The
agent must always produce both together — the label alone is not sufficient output.

Example output shape:
```
Maturity state: partially implemented backend
Alignment state: partially aligned
Aligned: persistence layer matches target pattern, repository naming follows convention
Conflicting: service layer uses transaction management pattern that differs from target
Absent: security configuration, exception handling, DTOs, tests
Partial: controllers (1 of estimated N complete)
```

The agent uses the label to determine how cautious to be overall. It uses the
inventory detail to determine exactly which areas are safe to extend, which need
documentation before coding, and which require clarification before any work.

**Rationale:**
Splitting "partially implemented backend" into sub-states would add classification
complexity without changing agent behavior — the structural inventory already captures
what is present and absent with full precision. The label is a mode selector, not a
complete description. Separating those two responsibilities keeps the classification
simple and the inventory authoritative.

---

### Decision 4 — User interruption of onboarding

**Decision:** Refuse. Onboarding is required and cannot be skipped under any
circumstance.

**Behavior:**
If the user asks to skip onboarding, the agent must:
1. Decline to proceed with any implementation work.
2. Explain that onboarding is a non-negotiable precondition for this skill.
3. Resume or complete the onboarding sequence before continuing.

**Rationale:**
This skill is not a general-purpose Spring Boot assistant. It exists specifically
to create or transform projects toward a defined, opinionated architecture. Every
project it touches is either a new project being built to that architecture, or an
existing project being brought into alignment with it. There is no scenario where
the agent should operate without understanding the current project state relative
to the target architecture.

Skipping onboarding in this context does not just risk low-quality output — it
risks working against the very purpose of the skill. The onboarding phase is the
mechanism by which the agent understands how far the project is from the target
architecture and what steps are safe to take. Without it, the agent has no basis
for any recommendation or change.

This also means that the `--override-convention` flag defined in Decision 1 does
not apply to onboarding itself. Convention overrides are for specific implementation
choices within an already-onboarded project. They are not a mechanism for bypassing
the onboarding phase.
