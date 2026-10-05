#adr #adr-accepted #architecture

## ADR 016: Guide document format, rule IDs and citation grammar

### Status
Accepted

### Context

The Skill, the Base Project and later reviews will cite rules and decisions across many sessions and many
agents. A citation only works if the thing it names has a stable identifier and a fixed home. The analysis
phase already proved the pattern: a fixed document format plus a validator turned three codebases into 241
findings whose IDs resolve and whose links open, and 44 unit tests keep the validator honest. Unvalidated
documentation drifts — the wpmanager project's own vault contradicts its code today. Decisions D16 and D17 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fix the formats; review decision F2 added
the contract-review ID family, F11 scoped the Version Notes, and F14 made the traceability-matrix scope
mechanical.

**Evidence:**
[[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-04|WP-R11-04]] — the reference
project's own vault contradicts its code, which is what documentation without validation becomes.

### Decision

1. We will record architectural decisions as ADRs in `documentation/ADRs/`, in Nygard format, with a
   mandatory index. An ADR is cited as `ADR-NNN` — three digits, the filename prefix. An accepted ADR is
   never edited: a changed decision is a new ADR that supersedes the old one.
2. We will keep the Guide in `documentation/Docs/Guide/`, as `Guide-Index.md`, `Guide-Conventions.md` and
   documents named `NN-Title-Words.md`.
3. We will give every Guide document these headings, in this order: Purpose, Design, Rules, Differs From the
   Reference Projects, Version Notes, Related Documents.
4. We will write each rule as a block headed by its Rule ID `G<NN>-<MM>`, with Rule, Why, Evidence and
   Differs-from-references fields. `NN` is the Guide document number; `MM` is sequential inside it.
5. We will keep Rule IDs stable. Renumbering needs a Guide changelog entry and a validator run over the
   Skill files, so no citation silently points at the wrong rule.
6. We will scope every Version Note to its evidence: `verified on <line>` with a resolvable citation, or
   `not verified — current-docs lookup at execution time`. A cross-line claim needs both sides verified.
   Rule text stays behavioural; API names live only in Version Notes.
7. We will make the traceability matrix cover every red and amber reference finding plus every medium and
   low finding raised by a security-category review, so severity alone cannot hide a configuration-class
   defect.
8. We will write contract-review findings as `GC-R<NN>-<MM>` in `Docs/Guide/Reviews/`, a family distinct
   from Guide rule IDs.
9. We will extend the validator with a `guide` target that enforces all of this, including that every cited
   Finding ID and ADR resolves.
10. We will mark a Guide document `draft` until its prose is finalised against the Base Project.

**Alternatives rejected:**
- A free-form guide — nothing is addressable, so nothing can be cited or checked.
- Rules stated in the Skill — the Skill is rewritten and migrated; rules must survive that.
- Rules identified by title — titles change; IDs do not.

### Consequences

- Every rule is addressable and machine-checked; a broken citation fails the build of the documentation.
- The validator must be maintained alongside the format, and its `guide` target grows with the Guide.
- IDs can never be reused, so the numbering is a permanent commitment.
- Four ID families exist and must not be confused: reference findings `<PROJ>-R<NN>-<MM>`, Guide rules
  `G<NN>-<MM>`, contract-review findings `GC-R<NN>-<MM>` and validation findings `V<NN><X>-R<NN>-<MM>`.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D16, D17; F2, F11, F14),
[[ADRs/ADR-001-guide-is-the-source-of-truth|ADR-001]],
[[ADRs/ADR-003-version-baseline-as-a-support-policy|ADR-003]],
[[ADRs/ADR-015-validation-loop-protocol-and-exit-gate|ADR-015]].
