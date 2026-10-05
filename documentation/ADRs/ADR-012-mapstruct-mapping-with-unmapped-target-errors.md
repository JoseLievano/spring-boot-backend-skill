#adr #adr-accepted #backend

## ADR 012: MapStruct mapping with unmapped-target errors

### Status
Accepted

### Context

Every Feature Module needs to turn a request into an entity and an entity into its response shapes. The
three reference projects wrote these mappers by hand. Hand-written mappers drift silently: a field added to
a DTO compiles, ships and stays empty until someone notices. The no-op base `update` is the same failure at
a larger scale — the request never reached the entity. Decision D12 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] chooses generated mappers that fail the
build when a target field is not mapped.

**Evidence:**
[[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-03|BE-R04-03]] — mappers fill different
field subsets per DTO (`username` and `apikey` are returned as `null`);
[[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-07|WP-R07-07]] — mapper bugs (a `url` set from the
name, a `name` set from the version, fields never mapped);
[[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01|BE-R02-01]] — the base `update`
ignores the form;
[[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-05|WP-R07-05]] and
[[Docs/BugTracker/Reviews/10-Code-Hygiene-Review#BT-R10-05|BT-R10-05]] — lazy injection hides cyclic mapper
dependencies.

### Decision

1. We will generate every feature mapper with MapStruct. A mapper has four methods: entity to Response,
   entity to Summary, Request to a new entity, and Request onto an existing entity (the update).
2. We will make an unmapped target property a **build error**. A new field forces a decision.
3. We will list server-controlled fields (id, owner, concurrency token, audit fields) as explicitly ignored
   in the Request mappings. Ignoring is visible in the mapper, never silent.
4. We will resolve relations (ids to entities, the owner from the actor) in the CRUD base's relation hook,
   not in mappers. Mappers depend on no repository and on no other feature's mapper, so they cannot form
   dependency cycles.

Mechanism on the current line (Spring Boot 4.1.x, 2026-10): MapStruct's `unmappedTargetPolicy =
ReportingPolicy.ERROR` and `@MappingTarget`. Both names were checked against the MapStruct documentation on
2026-09-30; no test verifies them until the Base Project is built.

**Alternatives rejected:**
- Hand-written mappers with a round-trip test per DTO — the reference projects' approach plus a test nobody
  wrote; the build does not stop a missing field.
- A reflection-based mapper — maps by name at run time, so a missing field is still silent.

### Consequences

- A field added to an entity or a response cannot be forgotten: the build fails until it is mapped or
  explicitly ignored.
- The generic update really updates, because the base calls the mapper's update method
  ([[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]]).
- Every project needs an annotation processor in its build, and its order relative to other processors
  matters.
- Mappers stay simple; anything that needs a lookup moves to a service hook, which is one more place to read.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D12, section 5),
[[ADRs/ADR-005-deepened-crud-base-with-access-policy|ADR-005]].
