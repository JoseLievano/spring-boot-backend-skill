#adr #adr-accepted #testing #architecture

## ADR 015: Validation Loop protocol and Exit Gate

### Status
Accepted

### Context

The reference projects have tests written alongside the code, and those tests did not reveal the missing
authorization or the no-op `update`. A convention that is only argued for is not proven; it has to be
exercised and reviewed by someone who did not write it. Decision D15 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes the protocol; review decisions F12
froze the evidence packs, F15 added the rule-coverage ledger and reviewer calibration, and F8 + F10 set the
run-01 accounting.

**Evidence:**
[[Docs/backend/Reviews/08-Testing-Review#BE-R08-01|BE-R08-01]] — tests that mask the missing
authorization;
[[Docs/backend/Reviews/08-Testing-Review#BE-R08-02|BE-R08-02]] — login, token and write operations
untested;
[[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-01|WP-R10-01]] — authorization tested for one module
only;
[[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-02|WP-R10-02]] — upload, idempotency, storage and
replication untested;
[[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-02|BT-R09-02]] — no coverage of security, services or
the filter engine. Tests written alongside the code did not reveal the reference projects' missing
authorization.

### Decision

1. We will have the user define two Validation Domains of four to six entities each that together cover the
   coverage checklist.
2. We will generate, per run, one project per domain under `validation-runs/` (git-ignored); the agent
   builds it, runs all tests and starts it on PostgreSQL.
3. We will have a blind reviewer in a fresh context — given only the generated project, the Guide and the
   review conventions — write review documents in the analysis-document format with IDs
   `V<NN><X>-R<NN>-<MM>` and a root cause of `guide`, `base` or `skill` (`base` is unused in run 01).
4. We will freeze evidence packs: every cited file is copied whole and unmodified into the review's
   `evidence/` tree, and the review cites only those copies. The validator rejects a citation into
   `validation-runs/`.
5. We will carry a rule-coverage ledger in every review, listing each applicable Rule ID once — as a
   finding, or as checked with an evidence citation.
6. We will make reviewer calibration a hard precondition of run 01: the reviewer prompt, run blind on
   `backend/`, must report at least the three named critical findings and flag a method-security annotation
   on a CRUD-based service. That is a lower bound, never an exact match; the answer key is banned from that
   context; the named fallback is a dedicated calibration fixture.
7. We will fix each finding at its root cause, and the next run starts from a fresh generation.
8. We will apply the Exit Gate: both domains build, all tests pass (including architecture tests and the
   authorization matrix), both start on PostgreSQL, the review reports 0 red and 0 amber findings with a
   complete ledger, and the CRUD depth criterion holds.
9. We will end the loop after two consecutive passing runs, or at run 5, when the user decides. Run 01 is a
   planned discovery run inside that budget. A Guide correction in Phase 3 that changes a convention resets
   the consecutive-pass count.

**Alternatives rejected:**
- One domain — overfits the convention to a single shape.
- The generating agent reviews its own output — self-grading bias.
- Seeded defects in run artifacts — they fail the gate's own severity arithmetic and have no root-cause
  value.
- No run limit — the cost is unbounded.
- Committing the generated projects — they are artifacts, not sources.

### Consequences

- The convention is judged by evidence, not argument.
- Each run costs two generated projects, their builds and Docker time.
- Evidence packs add committed files.
- The gate may not be reached by run 5, in which case the user decides what to do.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D15; F12, F15, F8+F10),
[[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]],
[[ADRs/ADR-002-code-free-skill-and-reference-base-project|ADR-002]].
