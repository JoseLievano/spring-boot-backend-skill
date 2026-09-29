# Domain Model and Persistence Review — wpmanager

#doc #review #ref-wpmanager #persistence

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/05-Domain-Model-and-Persistence]]

## Scope

Reviewed: all 18 entities, their repositories, fetch plans, equality, identifier strategy, the JPA and
datasource properties, and the vault's open persistence notes. Transaction boundaries are in
[[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]]; service-level behavior in
[[Docs/wpmanager/Reviews/07-Service-Design-Review]].

## Verdict

The model captures the domain well: a version/copy split that makes multi-provider storage possible, two
sensible `JOINED` hierarchies, a plan with product entitlements. The mapping choices are where it weakens.
Several EAGER collections pull large graphs on every load, most painfully each storage provider's full file
map. Roles are ordinals, the schema is managed by `ddl-auto=update`, four equality styles coexist (three
entities hash every instance to 42), favorites are modeled so that only one client can favorite a plugin, and
a version's identity is a string built from a mutable plugin name.

## Strengths to Keep

- Version (`DownloadableEntity`) separated from stored copy (`FileEntity`), with a per-provider map keyed by
  signature (`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableEntity.java:10-45`,
  `wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/StorageProviderEntity.java:60-64`).
- `JOINED` roots for users and providers, queried polymorphically
  (`wpmanager/src/main/java/com/wpmanager/shared/models/baseUser/BaseUserEntity.java:11-18`,
  `wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/StorageProviderEntity.java:21-27`).
- `open-in-view=false`, with services initialising what they need
  (`wpmanager/src/main/resources/application.properties:17`).
- Many-to-many relations owned explicitly with named join tables
  (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteEntity.java:67-81`).
- Database-level uniqueness on names and ids that matter (`plugin.name`, `client.wp_ID`, `idempotency_key.key_value`)
  (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginEntity.java:29-33`,
  `wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientEntity.java:25-26`).
- Indexes declared for the idempotency table's access paths
  (`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyKeyEntity.java:12-16`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-01\|WP-R06-01]] | Roles stored as ordinals | 🟠 High | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-02\|WP-R06-02]] | No migrations; `ddl-auto=update` | 🟠 High | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-03\|WP-R06-03]] | EAGER collections, including every provider's full file map | 🟠 High | performance | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-04\|WP-R06-04]] | `hashCode()` returns 42 in three entities; four equality styles | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-05\|WP-R06-05]] | Lombok `@Data` on provider entities | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-06\|WP-R06-06]] | Favorite plugins and themes modeled as one-to-many | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-07\|WP-R06-07]] | Version identity is `pluginName_version`; renames break it | 🟡 Medium | correctness | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-08\|WP-R06-08]] | Audit and API fields are never written | 🟡 Medium | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-09\|WP-R06-09]] | `mainImage` join-column naming; image is entity-only | 🟢 Low | persistence | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/06-Domain-Model-and-Persistence-Review#WP-R06-10\|WP-R06-10]] | Deprecated `MySQL8Dialect`; stale `@MappedSuperclass` Javadoc | 🟢 Low | configuration | Confirmed (read in code) |

## Findings

### WP-R06-01
**Title:** Roles stored as ordinals
**Severity:** 🟠 High
**Category:** persistence
**Principle:** Persist enums by name.
**Evidence:** `Set<UserRoles> roles` is an `@ElementCollection` with `@Column(name = "role")` and no
`@Enumerated` (`wpmanager/src/main/java/com/wpmanager/shared/models/baseUser/BaseUserEntity.java:37-43`), so JPA's
default `ORDINAL` applies: `ADMIN`=0, `CLIENT`=1, `EMPLOYEE`=2
(`wpmanager/src/main/java/com/wpmanager/shared/models/baseUser/UserRoles.java:3-6`).
**Impact:** Inserting a new role before an existing one, or reordering the enum, silently changes every stored
user's role — a client could become an admin.
**Recommendation:** `@Enumerated(EnumType.STRING)` on the collection with a data migration of existing rows.
**Verified against:** N/A — JPA specification default
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-03|BE-R03-03]]

### WP-R06-02
**Title:** No migrations; `ddl-auto=update`
**Severity:** 🟠 High
**Category:** persistence
**Principle:** Versioned, reviewable schema changes.
**Evidence:** `spring.jpa.hibernate.ddl-auto=update` in the runtime profile
(`wpmanager/src/main/resources/application.properties:14`); no Flyway or Liquibase dependency (`wpmanager/pom.xml:34-163`)
and no migration files.
**Impact:** Renames and type changes are not applied (Hibernate only adds), constraints added later (such as the
unique version constraint in [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-04|WP-R03-04]])
fail or are skipped silently on existing data, and production schema drifts from the code.
**Recommendation:** Flyway with a baseline of the current schema, `ddl-auto=validate` everywhere except tests.
**Verified against:** N/A — Boot property semantics
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-04|BE-R03-04]], [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-09|BT-R03-09]]

### WP-R06-03
**Title:** EAGER collections, including every provider's full file map
**Severity:** 🟠 High
**Category:** performance
**Principle:** Default to LAZY; fetch what a use case needs with a query.
**Evidence:** EAGER: `StorageProviderEntity.files` (every file row the provider holds)
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/StorageProviderEntity.java:60-64`),
`DownloadableEntity.files` (`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableEntity.java:39-43`),
`ClientEntity.websites` (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientEntity.java:41-46`),
`PlanEntity.clients` (`wpmanager/src/main/java/com/wpmanager/models/hq/plan/PlanEntity.java:43-44`) and user roles.
`FileEntity.downloadable` is a `@ManyToOne` (EAGER by default), whose version then loads its files
(`wpmanager/src/main/java/com/wpmanager/models/files/file/FileEntity.java:36-38`). Each upload loads the default
provider (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:91-92`),
and each replication run loads all providers and all versions
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:59-60`). The vault records both
(`wpmanager/wpManagerDocs/Bugs/to-do/N+1 Query Problem from Eager Fetching.md`,
`wpmanager/wpManagerDocs/Bugs/to-do/Map-Based Files Storage.md`).
**Impact:** Loading one provider loads the whole file table for that provider and every version those files
belong to; the cost of every upload grows with the size of the catalog. Loading a plan loads its clients, their
websites and roles.
**Recommendation:** Make these collections LAZY; replace the provider map with a query
(`existsByProviderIdAndDownloadableId`) and give `FileEntity` a `@ManyToOne` provider instead of the join table.
**Verified against:** N/A — JPA fetch defaults
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/BugTracker/Reviews/04-Performance-and-Fetching-Review#BT-R04-01|BT-R04-01]]

### WP-R06-04
**Title:** `hashCode()` returns 42 in three entities; four equality styles
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** One entity-equality convention, consistent with `equals` and stable across persist.
**Evidence:** `hashCode() { return 42; }` in `PluginCategoryEntity`, `ThemeCategoryEntity` and `WebsiteEntity`
(`wpmanager/src/main/java/com/wpmanager/models/downloads/pluginCategory/PluginCategoryEntity.java:40-43`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/themeCategory/ThemeCategoryEntity.java:40-43`,
`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteEntity.java:90-93`); `Objects.hash(id)` in plugin,
theme and author (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginEntity.java:83-86`); a null-safe id
hash in plan and favorite list (`wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListEntity.java:54-57`);
Lombok all-field equality on providers (WP-R06-05); identity everywhere else.
**Impact:** Sets of websites or categories degrade to linear lists (every element in one bucket). `Objects.hash(id)`
changes when a new entity is saved, so an entity added to a `HashSet` before `save` cannot be found or removed
afterwards; services add entities to sets both before and after saving
(`wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListService.java:91-92`).
**Recommendation:** One convention for all entities: `equals` on id when non-null, `hashCode` returning
`getClass().hashCode()` (constant per type, stable across persist), or a natural or UUID key assigned at
construction.
**Verified against:** N/A — Java/JPA convention
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-04|BT-R03-04]]

### WP-R06-05
**Title:** Lombok `@Data` on provider entities
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** Entities need explicit identity and a safe `toString`.
**Evidence:** `@Data` on `StorageProviderEntity` (with the EAGER `files` map) and on both subclasses with
`@EqualsAndHashCode(callSuper = true)` (`wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/StorageProviderEntity.java:21-27`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java:27-34`,
`wpmanager/src/main/java/com/wpmanager/models/storage/ftp/FTPProviderEntity.java:28-35`).
**Impact:** Equality and hash codes change whenever a file is added to the map, and every hash walks the whole
map; `FileDuplicator` and `FileService` put all providers into a `HashSet` and then modify them
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:60`,
`wpmanager/src/main/java/com/wpmanager/models/files/file/FileService.java:183-191`), which is correct today only because
those sets are iterated and never searched afterwards. `toString()` prints credentials
([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-04|WP-R01-04]]).
**Recommendation:** Replace `@Data` with `@Getter @Setter`, id-based equality as in WP-R06-04, and
`@ToString.Exclude` on secrets and collections.
**Verified against:** N/A — Lombok `@Data` semantics
**Confidence:** Confirmed (read in code)

### WP-R06-06
**Title:** Favorite plugins and themes modeled as one-to-many
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** Model cardinality as the domain has it.
**Evidence:** `ClientEntity.favoritePlugins` and `favoriteThemes` are unidirectional `@OneToMany` with no
`mappedBy` or join column (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientEntity.java:48-56`). Hibernate
maps this as a join table with a unique constraint on the plugin (or theme) column. No service uses the
fields; favorite lists (a proper many-to-many) replaced them
(`wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListEntity.java:31-45`).
**Impact:** Dormant. If used, a plugin could be a favorite of only one client; a second client's favorite
fails on the unique constraint.
**Recommendation:** Remove the two fields (favorite lists cover the need) or map them `@ManyToMany`.
**Verified against:** N/A — JPA/Hibernate mapping semantics
**Confidence:** Confirmed (read in code)

### WP-R06-07
**Title:** Version identity is `pluginName_version`; renames break it
**Severity:** 🟡 Medium
**Category:** correctness
**Principle:** Identify rows by stable keys, not by derived strings.
**Evidence:** A version's `name` is `pluginEntity.getName() + "_" + version`, and the duplicate check, the file
signature and the object key all use it (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:186-191`).
The version is linked to its plugin only after the upload succeeds (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:202-205`).
`PluginService.update` allows renaming (`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:218-220`).
Theme versions use the theme name the same way (`wpmanager/src/main/java/com/wpmanager/models/downloads/theme/ThemeService.java:172`).
**Impact:** After a rename, the duplicate check looks for `newName_1.0` and lets version 1.0 be uploaded again;
old objects keep the old key. A plugin and a theme with the same name share object keys.
**Recommendation:** Give `DownloadableEntity` a `@ManyToOne` parent set at creation, a unique `(parent, version)`
constraint, and an object key based on ids and the checksum; keep the display name separate.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R06-08
**Title:** Audit and API fields are never written
**Severity:** 🟡 Medium
**Category:** persistence
**Principle:** No speculative columns; audit through the framework.
**Evidence:** `BaseUserEntity.dateCreated` and `lastLogin` (`wpmanager/src/main/java/com/wpmanager/shared/models/baseUser/BaseUserEntity.java:52-56`)
and `ClientEntity.apiKey`, `apiRateLimit`, `websCount` (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientEntity.java:28-35`)
have no setter call anywhere in `wpmanager/src/main/java`. `websCount` is returned in client DTOs
(`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientMapper.java:38`).
**Impact:** Responses show nulls for fields that look meaningful; the website limit uses the collection size
instead (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:66`), so `websCount` and it can
disagree forever.
**Recommendation:** Use Spring Data auditing (`@CreatedDate`, `@LastModifiedDate`, `@EnableJpaAuditing`) and set
`lastLogin` on successful login; drop columns with no feature.
**Verified against:** N/A — no library call in the current code
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-02|BE-R03-02]]

### WP-R06-09
**Title:** `mainImage` join-column naming; image is entity-only
**Severity:** 🟢 Low
**Category:** persistence
**Principle:** Column names should say what they reference.
**Evidence:** `@OneToOne(cascade = ALL) @JoinColumn(name = "plugin_id") ImageEntity mainImage` puts a column named
`plugin_id` in the `plugin` table that references `image.id`; the same pattern uses `theme_id` in `theme`
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginEntity.java:72-74`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/theme/ThemeEntity.java:73-75`). `ImageEntity` has no repository,
service or API (`wpmanager/src/main/java/com/wpmanager/models/files/image/ImageEntity.java:8-31`).
**Impact:** Anyone reading the schema will assume `plugin.plugin_id` is a self-reference.
**Recommendation:** `@JoinColumn(name = "main_image_id")`, or drop the relation until images exist.
**Verified against:** N/A — JPA mapping semantics
**Confidence:** Confirmed (read in code)

### WP-R06-10
**Title:** Deprecated `MySQL8Dialect`; stale `@MappedSuperclass` Javadoc
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** Let Hibernate detect the dialect; keep comments true.
**Evidence:** `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect`
(`wpmanager/src/main/resources/application.properties:16`). `StorageProviderEntity`'s Javadoc says it uses
`@MappedSuperclass` and creates no table, while the class is an `@Entity` with `JOINED` inheritance
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProvider/StorageProviderEntity.java:13-27`).
**Impact:** A deprecation warning at start-up and removal in a future Hibernate major; a misleading comment on
the class that defines the storage schema.
**Recommendation:** Remove the dialect property (Hibernate 6 detects MySQL and its version); fix the Javadoc.
**Verified against:** `hibernate-core-6.6.4.Final.jar` (javap: `org.hibernate.dialect.MySQL8Dialect` is `@Deprecated`)
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

- Entities: `@Getter @Setter`, id-based equality with a class-constant hash, `@ToString.Exclude` on
  collections and secrets, all collections LAZY, enums as `STRING`.
- Versions: `@ManyToOne` parent at creation, unique `(parent_id, version)`, id-based object keys.
- Copies: `FileEntity` with `@ManyToOne provider` and unique `(provider_id, downloadable_id)`; queries instead of
  entity-held maps.
- Schema: Flyway migrations, `ddl-auto=validate`, dialect auto-detected, auditing through Spring Data.

## Related Documents

- [[Docs/wpmanager/Explanations/05-Domain-Model-and-Persistence]]
- [[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review]]
- [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review]]
- [[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review]]
