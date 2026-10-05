#adr #adr-accepted #architecture

## ADR 002: The Skill is a code-free convention; the Base Project is the workspace reference implementation

### Status
Accepted

### Context

The critical findings of the reference projects live in the shared platform design — the CRUD base, the
identity seam, the error contract — so that design must be worked out and proven once, not rediscovered per
project. But shipping version-pinned code in a skill teaches the APIs of the day it was written, and those
decay: one reference project is frozen on an end-of-support line, and all three show era gaps where code is
tied to the framework generation it was written on. The brief requires a standalone, pure-markdown skill that
is not Claude-Code-specific and does not initialise projects. The first draft of decision D2 had the Skill
start projects by copying `base-project/`; review finding F1 (Critical) showed that makes the shipped skill
depend on a workspace artifact. D2 was revised, and F8 + F10 later reordered the work; F16 dissolved once the
Skill stopped copying anything.

**Evidence:**
[[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-01|BT-R08-01]] — a project
frozen on an end-of-support framework line;
[[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-02|BT-R08-02]] and
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-14|BT-R01-14]] — era gaps: code tied
to the framework generation it was written on.

### Decision

1. We will state Platform Modules as contracts — interface, invariants, error modes, interactions — in the
   Guide and the Skill, not as code.
2. We will ship the Skill as rules, contracts and pseudo-code only: no code artifacts, no Spring app, no
   project initialisation.
3. We will have every project implement the contracts fresh, against current framework documentation looked
   up at execution time, so the Skill can never teach a deprecated API.
4. We will keep `base-project/` as the workspace reference implementation that proves the contracts are
   implementable — never shipped, never a copy source.
5. We will work contract-first: phases run 1 → 2 → 4 → 5 → 3 → 6. The Guide's contract layer leads and is
   gated by a blind contract design review (0 red, 0 amber); the Skill is written against the gated
   contracts; validation run 01 is a planned discovery run; the Base Project is built last, per area, and
   that is where each Guide document's prose is finalised.
6. We will treat step numbers as stable identifiers and never renumber them.

**Alternatives rejected:**
- The Skill copies the Base Project (the original D2) — rejected by F1: the shipped skill would depend on a
  workspace artifact and would teach pinned code.
- No Base Project at all — nothing would prove the contracts are implementable, and `verified on` Version
  Notes would have no evidence.
- The Base Project before the Skill (the original order) — a horizontal slice; F10 showed the cross-module
  contract gaps are found by the blind review, not by building first.

### Consequences

- The Skill cannot teach a deprecated API, because it names no API outside dated Version Notes.
- Drift only weakens the Base Project's role as evidence, never the code generated from the Skill.
- Every generated project re-implements the platform, so variance between projects is possible; the
  Validation Loop measures it.
- Guide rules written before code may turn out to be wrong, so the rule is "fix the Guide first", and a
  convention change resets the Exit Gate pass count.
- Validation run 01 has no `base` root cause, because no Base Project exists yet.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D2 as revised; F1, F8+F10,
F16),
[[ADRs/ADR-017-skill-architecture-code-free-contract-first|ADR-017]],
[[ADRs/ADR-003-version-baseline-as-a-support-policy|ADR-003]],
[[ADRs/ADR-015-validation-loop-protocol-and-exit-gate|ADR-015]].
