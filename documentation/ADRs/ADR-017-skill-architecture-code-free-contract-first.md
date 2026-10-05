#adr #adr-proposed #architecture

## ADR 017: Skill architecture as a code-free, contract-first convention

### Status
Proposed

### Context

`spring-boot-skill/` currently holds a phase-based router and a finished Phase 0. Decision D2 changed what
the Skill is: a code-free convention, not a scaffold. Decision D18 says the Skill is rewritten from
scratch, after the Guide exists and its contracts pass the blind contract review — the clause "and Base
Project exist" was dropped when review decisions F8 + F10 reordered the work. This ADR is opened now so
that the decided part of D18 is on record and the number 017 is reserved against the `create adr` workflow
giving it to another decision. Step 4.1 (Task 6) completes this ADR and asks for acceptance. Until then the
Skill has a recorded direction but no recorded structure.

**Evidence:**
[[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-04|WP-R11-04]] — documentation that
drifts from code is what a Skill becomes if it restates rules instead of citing them.

### Decision

Decided now:

1. We will rewrite the Skill from scratch, and nothing of the phase-based design is carried over by
   default.
2. We will write it only after the Guide's contract layer passes the blind contract review gate.
3. We will make it a code-free convention
   ([[ADRs/ADR-002-code-free-skill-and-reference-base-project|ADR-002]]).
4. We will have it cite Guide Rule IDs instead of restating rules.
5. We will look up API syntax from current framework documentation at execution time.
6. We will keep it independent of any one agent runtime.
7. We will keep it in `spring-boot-skill/` until the release step migrates it.

The minimum capabilities are: state the architecture and the module contracts; guide adding a Feature
Module from an entity description; guide enabling an optional capability (upload and storage,
idempotency); review a project against the Guide and report findings by Rule ID.

**Decided in Step 4.1 (Task 6), before acceptance:** the file layout; the loading model; the form in which
contracts are stated; how the manual smoke scenario is run.

**Alternatives rejected:**
- Keep the phase-based design — it was designed for a scaffold skill; D2 changed the product.
- Write the Skill before the contract gate — it would encode contracts the gate may still change.
- Carry over Phase 0's ideas as-is — repository-first reasoning, onboarding before generation and
  progressive disclosure may return, but only through this ADR's completion, not by default.

### Consequences

- The phase-based design and Phase 0 are discarded.
- [[Docs/Skill-Architecture]] and
  [[Features/to-do/Agent-Project-Onboarding-First-Instructions]] are superseded and retired in the release
  step with approval.
- Until Step 4.1 the Skill has a recorded direction but no recorded structure.
- This ADR stays `Proposed` after this Task; only Task 6 completes and accepts it.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D18 as amended by F8+F10),
[[ADRs/ADR-002-code-free-skill-and-reference-base-project|ADR-002]],
[[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]].
