# Domain Model and Persistence Review — BugTracker

#doc #review #ref-bugtracker #persistence

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/07-Domain-Model]], [[Docs/BugTracker/Explanations/08-Persistence-and-Transactions]]

## Scope

Reviewed: all 32 entities (mappings, identifiers, `equals`/`hashCode`, Lombok usage, cascades), the `User`
hierarchy, role storage, and schema management. Fetching and counters are reviewed in
[[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review]]. The database was not started; claims that
depend on Hibernate's runtime schema handling are marked.

## Verdict

The domain model is the most valuable part of BugTracker: a clear operator/tenant split, tenant-owned
configurable taxonomies, a single user hierarchy and tree-shaped knowledge categories. The persistence
mechanics under it are fragile. Two entities have an `equals` that casts to the wrong class, which breaks
HQ invoicing after the first invoice; Lombok builders silently drop collection and flag initializers; the
identifier strategy differs between parent and subclass in the user hierarchy; and the schema is managed
by `ddl-auto=update` with no migrations, auditing or optimistic locking. The model is anemic: every
invariant lives in a service.

## Strengths to Keep

- Operator/tenant split with a single tenant root that every tenant row references with a non-null foreign
  key (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:54-56`).
- Tenant-configurable status, priority, type and task category as entities
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsStatus/bsStatusEntity.java`), with a
  category↔type allow-list (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsTaskCategory/bsTaskCategoryEntity.java:31-37`).
- One `User` hierarchy for all principals, so authorship and login work across user kinds
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:11-19`).
- Self-referencing category trees with explicit `level`
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryEntity.java:29-40`).
- `open-in-view=false` forces data access into the service layer
  (`BugTracker/src/main/resources/application.properties:6`).
- Where `equals` is correct it follows the Hibernate-safe pattern (`Hibernate.getClass`, id-based, constant
  hash) (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:89-100`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-01\|BT-R03-01]] | `equals` casts to `PlanEntity` in `InvoiceEntity` and `EmployeeEntity` | 🔴 Critical | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-02\|BT-R03-02]] | Subclasses redeclare `@Id` with `IDENTITY` under a `SEQUENCE` JOINED root | 🟡 Medium | persistence | Needs runtime verification |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-03\|BT-R03-03]] | Mixed identifier strategies, no named generators | 🟢 Low | persistence | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-04\|BT-R03-04]] | Inconsistent entity equality | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-05\|BT-R03-05]] | Lombok `@Builder` drops collection and flag initializers | 🟠 High | correctness | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-06\|BT-R03-06]] | Anemic model; invariants scattered in services | 🟡 Medium | design | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-07\|BT-R03-07]] | No auditing, no optimistic locking | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-08\|BT-R03-08]] | `optional = true` with `nullable = false` on task type | 🟢 Low | persistence | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-09\|BT-R03-09]] | No migrations; `ddl-auto=update` | 🟠 High | persistence | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-10\|BT-R03-10]] | Roles are free strings; the `Role` enum is unused and disagrees | 🟡 Medium | design | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-11\|BT-R03-11]] | Cascade-remove chains delete whole tenants | 🟡 Medium | persistence | Confirmed (read in code) |

## Findings

### BT-R03-01
**Title:** `equals` casts to `PlanEntity` in `InvoiceEntity` and `EmployeeEntity`
**Severity:** 🔴 Critical
**Category:** correctness
**Principle:** Copy-paste defect in a contract method — `equals` must compare the class it lives in.
**Evidence:**

`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/invoice/InvoiceEntity.java:65-76`
```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PlanEntity that = (PlanEntity) o;
        return id != null && Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

The same cast is in `EmployeeEntity` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/employee/EmployeeEntity.java:48-58`).
After the class check passes, `o` is an `InvoiceEntity` (or `EmployeeEntity`), so the cast always throws
`ClassCastException`. Because `hashCode` is the same for every instance, a `HashSet` calls `equals` whenever
it already holds another instance. `BusinessEntity.invoices` is an EAGER set
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessEntity.java:72-73`), and HQ
invoice insert adds to it (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/invoice/InvoiceServiceImplements.java:94-97`).
**Impact:** The second `POST /invoice` for any Business fails with a `ClassCastException` (500, rolled back):
the operator can bill each tenant once. Any code path that loads a set with two or more HQ invoices or HQ
employees into memory — `GET /mainHQ/{id}` maps `employeeEntities`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/mainHQ/MainHQMapper.java:57-59`) — fails the same way.
**Recommendation:** Cast to the enclosing class. Better, remove hand-written `equals`/`hashCode` from
entities that do not need set semantics, or generate them from one template and cover them with a test that
adds two instances to a `HashSet`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R03-02
**Title:** Subclasses redeclare `@Id` with `IDENTITY` under a `SEQUENCE` JOINED root
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** One identifier per inheritance hierarchy, declared on the root.
**Evidence:** `User` declares `@Id @GeneratedValue(strategy = SEQUENCE) Long id` with `JOINED` inheritance
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:18-23`); `ClientEntity` and
`EmployeeEntity` declare another `@Id @GeneratedValue(strategy = IDENTITY) @Column(name = "id") Long id` plus
Lombok getters (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/client/ClientEntity.java:29-32`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/employee/EmployeeEntity.java:23-26`).
**Impact:** JPA does not allow a subclass to redefine the hierarchy's id. Depending on how Hibernate 5.6
treats the duplicate mapping, `ClientEntity.getId()` may return a field that Hibernate never populates (the
subclass getter shadows `User.getId()`), which would break `ClientEntity.equals` (id-based) and every mapper
that reads `getId()`, or the subclass table's `id` column may be mapped twice.
Runtime check: create an HQ client and compare `GET /client` ids with the `user` and `client` tables.
**Recommendation:** Delete the subclass `id` fields; keep the root's identifier only.
**Verified against:** N/A — mapping rule of the JPA specification; Hibernate behavior not observed
**Confidence:** Needs runtime verification

### BT-R03-03
**Title:** Mixed identifier strategies, no named generators
**Severity:** 🟢 Low
**Category:** persistence
**Principle:** Consistent persistence conventions.
**Evidence:** `IDENTITY` on `MainHQEntity`, `PlanEntity`, `InvoiceEntity`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/plan/PlanEntity.java:23-25`); `SEQUENCE` without a
`@SequenceGenerator` everywhere else (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:29-31`).
**Impact:** On MySQL, which has no sequences, Hibernate emulates `SEQUENCE` with a table; without named
generators all those entities draw from one shared counter, and `IDENTITY` disables JDBC insert batching
for the other three. Neither breaks correctness; both surprise.
**Recommendation:** Pick one strategy (on MySQL, `IDENTITY`; on PostgreSQL, named sequences with
`allocationSize` matching the database) and apply it via a mapped superclass.
**Verified against:** N/A — Hibernate behavior described, not observed
**Confidence:** Confirmed (read in code)

### BT-R03-04
**Title:** Inconsistent entity equality
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** One identity rule for all entities.
**Evidence:** Seven entities override `equals`/`hashCode` (id-based, constant hash) —
`BusinessEntity`, `bsPrTaskEntity`, `bsClientEntity`, `ClientEntity`, `PlanEntity`, `InvoiceEntity`,
`EmployeeEntity` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessEntity.java:162-172`);
the other 25 use object identity. Collections are `Set`s everywhere.
**Impact:** Identity semantics differ by entity (two loaded copies of a `bsStatusEntity` are different set
members; two copies of a `BusinessEntity` are equal). The constant hash turns every large set of those seven
into a linked list per bucket, and it is what makes [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-01|BT-R03-01]] fire.
**Recommendation:** Use a common `BaseEntity` with the Hibernate-safe id-based `equals` and constant
`hashCode`, or identity everywhere and `List`s for inverse collections.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R03-05
**Title:** Lombok `@Builder` drops collection and flag initializers
**Severity:** 🟠 High
**Category:** correctness
**Principle:** Construction must produce a valid object — the builder is a second constructor that skips the field initializers.
**Evidence:** Entities are built with `.builder()` in mappers
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskMapper.java:106-115`).
Without `@Builder.Default`, Lombok's builder ignores `= new LinkedHashSet<>()` and `= true`:
`BusinessEntity`'s collections (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessEntity.java:72-73`),
`bsPrChannelEntity.tasks` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrChannel/bsPrChannelEntity.java:70`)
and `User`'s four account flags (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:56-66`).
`bsPrChannelEntity.members` has no initializer at all (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrChannel/bsPrChannelEntity.java:59`).
**Impact:** New entities have `null` collections until Hibernate replaces them after a reload — `POST
/bs_pr_channel` with members throws a `NullPointerException`
([[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-03|BT-R06-03]]), and every other
`toInsert.getX().add(...)` works only by accident of ordering. Users created through the builders are stored
with `enabled = false` and the other flags false ([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-11|BT-R01-11]]).
**Recommendation:** Add `@Builder.Default` to every initialized field (or stop using `@Builder` on
entities and use a factory method), and initialize every collection.
**Verified against:** N/A — Lombok documented behavior (`@Builder.Default`); Lombok version managed by Boot 2.7
**Confidence:** Confirmed (read in code)

### BT-R03-06
**Title:** Anemic model; invariants scattered in services
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Information expert / deep modules — the entity that owns the data should own its rules.
**Evidence:** Entities are Lombok getter/setter bags (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:20-27`).
Defaults (`isDone = false`), both-sides linking, priority ordering and tree levels live in services
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:104-129`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsDocsCategory/bsDocsCategoryServiceImplements.java:50-67`).
**Impact:** Every writer must remember every rule; the seeder had to duplicate Business creation, and the
update paths already diverge from insert ([[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-12|BT-R06-12]]).
**Recommendation:** Move invariants into entity methods (`project.addTask(task)`, `category.moveUnder(parent)`,
`business.addPriority(p)` computing the order) and keep services as orchestration.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-01|BE-R03-01]]

### BT-R03-07
**Title:** No auditing, no optimistic locking
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** Concurrent writes must be detected, not lost.
**Evidence:** No `@Version`, no `@CreatedDate`/`@LastModifiedDate` or `@EnableJpaAuditing` in the code base
(`BugTracker/src/main/java/com/bgsystem/bugtracker/BugTrackerApplication.java:7-16`); creation dates are set by
hand in some services only (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:112-114`).
**Impact:** Two users editing priorities (`PUT /bs_priority/update-order`) or the same task overwrite each other
silently; there is no record of who changed what — a real gap for a project tracker.
**Recommendation:** `@Version Long version` on a base entity; Spring Data auditing for created/modified
by/at, fed by the security context.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R03-08
**Title:** `optional = true` with `nullable = false` on task type
**Severity:** 🟢 Low
**Category:** persistence
**Principle:** One source of truth for optionality.
**Evidence:** `@ManyToOne(optional = true) @JoinColumn(name = "bs_type_id", nullable = false)`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskEntity.java:66-68`); the
service requires a type (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:68`).
**Impact:** The schema says required, the mapping says optional; Hibernate uses an outer join and the
database rejects nulls. Harmless today, confusing when someone makes type optional.
**Recommendation:** Decide and make both attributes agree.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R03-09
**Title:** No migrations; `ddl-auto=update`
**Severity:** 🟠 High
**Category:** persistence
**Principle:** Schema as versioned code.
**Evidence:** `spring.jpa.hibernate.ddl-auto=update` (`BugTracker/src/main/resources/application.properties:1`); no
Flyway or Liquibase dependency (`BugTracker/pom.xml:19-148`) and no SQL scripts (`BugTracker/src/main/resources`).
**Impact:** Column renames and type changes are never applied (update only adds), constraints drift, and the
orphan `bs_file` table exists only because an entity does. Production schema changes cannot be reviewed or
rolled back.
**Recommendation:** Flyway with a baseline migration generated from the current schema, `ddl-auto=validate`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-04|BE-R03-04]]

### BT-R03-10
**Title:** Roles are free strings; the `Role` enum is unused and disagrees
**Severity:** 🟡 Medium
**Category:** design
**Principle:** Closed vocabularies as types.
**Evidence:** `Set<String> roles` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:34-37`);
services assign `ROLE_BS_CLIENT`, `ROLE_BS_MANAGER`, `ROLE_BS_EMPLOYEE`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsEmployee/bsEmployeeServiceImplements.java:61-62`);
the enum lists `ROLE_BSEMPLOYEE`, `ROLE_BSCLIENT` and no manager role, and nothing references it
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/role/Role.java:4-8`).
**Impact:** Typos become new roles; `POST /user` can store any string
([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-04|BT-R01-04]]); a future authorization
rule written against the enum would not match stored values.
**Recommendation:** A single `Role` enum persisted with `@Enumerated(EnumType.STRING)`, matching the stored
strings; migrate existing rows.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03|BE-R03-03]] — backend uses an enum, but stored as ordinals.

### BT-R03-11
**Title:** Cascade-remove chains delete whole tenants
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** Destructive operations must be explicit.
**Evidence:** `ClientEntity.businessEntities` has `cascade = REMOVE, orphanRemoval = true`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/client/ClientEntity.java:49-50`); all of `MainHQEntity`'s
collections cascade `REMOVE` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/mainHQ/MainHQEntity.java:30-49`);
`BusinessEntity`'s fifteen collections have `orphanRemoval = true`, which in Hibernate also cascades removal
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessEntity.java:72-156`).
**Impact:** `DELETE /client/{id}` — open to any authenticated user
([[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01|BT-R01-01]]) — deletes that customer's
businesses and everything below them in one call, or fails midway on a foreign key from a table outside the
cascade (for example `bs_pr_comment` referencing `bs_project`). There is no soft delete.
**Recommendation:** Soft-delete tenants (`deactivatedAt`), remove cascades from HQ → tenant relations, and
make tenant deletion an explicit, audited, admin-only job.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- `BaseEntity` (mapped superclass): one id strategy, `@Version`, auditing fields, correct `equals`/`hashCode`.
- `TenantEntity extends BaseEntity` with the mandatory `business` reference, set by the service from the
  tenant context.
- Collections initialized and `@Builder.Default` (or no builders on entities).
- Rich entity methods for invariants (`addTask`, `moveUnder`, `reorderPriorities`).
- Roles as an enum stored as strings; unique username constraint on the root table.
- Flyway migrations, `ddl-auto=validate`; soft delete for tenants.

## Related Documents

- [[Docs/BugTracker/Explanations/07-Domain-Model]]
- [[Docs/BugTracker/Explanations/08-Persistence-and-Transactions]]
- [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review]]
- [[Docs/BugTracker/Reviews/00-Review-Summary]]
