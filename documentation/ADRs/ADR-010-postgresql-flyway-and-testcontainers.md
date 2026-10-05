#adr #adr-accepted #data #testing #backend

## ADR 010: PostgreSQL, Flyway migrations and Testcontainers

### Status
Accepted

### Context

All three reference projects let the ORM generate and mutate the schema at start-up, and none has a
migration file. Two of them test against an in-memory database that is not the production engine, and the
third needs a live database for its only test. The result is a schema with no history, tests that pass where
production would fail, and a runtime classpath carrying a database engine that production never uses.
Decision D10 of [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes the persistence
baseline.

**Evidence:**
[[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-04|BE-R03-04]],
[[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-09|BT-R03-09]] and
[[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-02|WP-R06-02]] — no migrations;
schema generated at start-up;
[[Docs/backend/Reviews/08-Testing-Review#BE-R08-05|BE-R08-05]] and
[[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-06|BE-R03-06]] — tests on an
in-memory database while production uses PostgreSQL;
[[Docs/backend/Reviews/08-Testing-Review#BE-R08-03|BE-R08-03]],
[[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-05|WP-R10-05]] and
[[Docs/BugTracker/Reviews/09-Testing-Review#BT-R09-01|BT-R09-01]] — tests that need a live database and
fail without it;
[[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-03|BE-R07-03]] and
[[Docs/BugTracker/Reviews/08-Build-Dependencies-and-Configuration-Review#BT-R08-06|BT-R08-06]] — an
in-memory database packaged on the runtime classpath.

### Decision

1. We will use PostgreSQL as the one database engine.
2. We will change the schema only through versioned migrations, one file per feature change.
3. We will have the ORM validate the schema and never change it.
4. We will keep the removable local identity module's migrations in that module's own folder, so deleting
   the module does not strand tables.
5. We will run integration tests on real PostgreSQL in a container, and the storage adapter's tests on an
   S3-compatible store in a container.
6. We will use no in-memory substitute database anywhere, in any phase.
7. We will document Docker as a test prerequisite and fail fast with a clear message when it is absent.

**Alternatives rejected:**
- An in-memory database for tests — different engine, different semantics; it is what the reference projects
  did and it hid the defects.
- Letting the ORM update the schema — no history, no review, no rollback.
- Another migration tool — equivalent in capability; plain SQL files match the "one migration per feature"
  rule best.
- MySQL — two reference projects used it; the newest moved to PostgreSQL, and one engine keeps the contract
  test suite single.

### Consequences

- Tests exercise the production engine, so an engine-specific defect fails in the test, not in production.
- A Docker daemon is required and tests are slower.
- Every schema change is a hand-written migration that a reviewer reads.
- Docker is used only as a test dependency; deployment stays out of scope.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D10),
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]],
[[ADRs/ADR-013-object-storage-port-upload-coordinator-and-download-tickets|ADR-013]],
[[ADRs/ADR-015-validation-loop-protocol-and-exit-gate|ADR-015]].
