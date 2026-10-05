<!-- AUTO-GENERATED — do not edit. Use the glossary CLI to make changes. -->

## Analysis Documentation

### Era Gap

**Term:** Era Gap

**Definition:** A Finding that was acceptable practice when the project was written but is not what a new project should do today (for example javax namespace or an end-of-life Spring Boot line). Distinguished from a defect; era gaps are at most Medium severity unless they carry a security or end-of-life risk, and they fill in the Version note field.

**Examples:** BT-R08-01: Spring Boot 2.7 is past end of OSS support; BT-R08-02: javax namespace.

**Synonyms:** 

**Related:** Finding, Defect

---

### Explanation Doc

**Term:** Explanation Doc

**Definition:** A descriptive analysis document under documentation/Docs/<project>/Explanations/ that explains how one area of a reference project is built (architecture, patterns, design rationale) in enough detail to build a new project the same way. It follows the Explanation template in Analysis-Doc-Conventions and does not judge the code; critique goes in the matching Review Doc.

**Examples:** Docs/backend/Explanations/05-Dynamic-List-Query-Engine.md

**Synonyms:** Explanation

**Related:** Review Doc, Recipe, Reference Project

---

### Finding

**Term:** Finding

**Definition:** A single evidence-backed problem recorded in a Review Doc. Every finding has a Finding ID, title, severity (Critical/High/Medium/Low), category, evidence citations, impact, recommendation, a version-verification line and a confidence level. A finding lives in exactly one review (where its root cause is) and is linked from others.

**Examples:** BE-R02-01: the generic update saves the entity without applying the form.

**Synonyms:** review finding

**Related:** Finding ID, Review Doc, Era Gap

---

### Finding ID

**Term:** Finding ID

**Definition:** The stable identifier of a Finding, formatted <PROJ>-R<NN>-<MM>, where PROJ is BE (backend), BT (BugTracker) or WP (wpmanager), NN is the review doc number and MM is sequential within that doc. IDs are never renumbered; withdrawn findings keep their heading with a Status line. Used by the cross-project comparison (Task 4) to reference findings.

**Examples:** BT-R01-02 (BugTracker, review 01, finding 02); linked as [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02|BT-R01-02]]

**Synonyms:** 

**Related:** Finding, Review Summary

---

### Lineage

**Term:** Lineage

**Definition:** The shared ancestry of the reference projects: BugTracker → wpmanager → backend. The generic CRUD stack evolved across all three, and backend's shared/ package largely mirrors wpmanager's. Each project's Overview doc has a Lineage paragraph stating what it inherited and what it changed.

**Examples:** BugTracker's CommonPathExpression filter engine is the predecessor of backend's shared/query engine.

**Synonyms:** 

**Related:** Reference Project, Generic CRUD Stack

---

### Recipe

**Term:** Recipe

**Definition:** The final Explanation Doc of each reference project ("Recipe — Build a Project This Way"): a numbered, standalone guide to building a new project the same way, with '⚠️ Review:' callouts wherever a step would copy a pattern flagged by a Finding.

**Examples:** Docs/backend/Explanations/11-Recipe-Build-a-Project-This-Way.md

**Synonyms:** 

**Related:** Explanation Doc, Finding

---

### Reference Project

**Term:** Reference Project

**Definition:** One of the three existing Spring Boot backends in this workspace (backend/, BugTracker/, wpmanager/) that are analyzed as source material for the spring-boot-backend skill. Reference projects are read-only: they are studied, never modified.

**Examples:** BugTracker/ is the oldest reference project (Boot 2.7); backend/ is the newest (Boot 3.4.1).

**Synonyms:** reference backend

**Related:** Explanation Doc, Review Doc, Lineage

---

### Review Doc

**Term:** Review Doc

**Definition:** An evaluative analysis document under documentation/Docs/<project>/Reviews/ that critiques one area of a reference project: a verdict, Strengths to Keep, a list of Findings, and a Recommended Target Pattern. It follows the Review template in Analysis-Doc-Conventions.

**Examples:** Docs/wpmanager/Reviews/04-Idempotency-Review.md

**Synonyms:** Review

**Related:** Explanation Doc, Finding, Strength to Keep, Review Summary

---

### Review Summary

**Term:** Review Summary

**Definition:** The Reviews/00-Review-Summary.md document of each reference project: an overview listing every Finding ID of the project ordered by severity, with links. It does not use the Review template.

**Examples:** Docs/BugTracker/Reviews/00-Review-Summary.md

**Synonyms:** 

**Related:** Finding ID, Review Doc

---

### Strength to Keep

**Term:** Strength to Keep

**Definition:** A pattern in a reference project that a Review Doc judges good and worth carrying into the spring-boot-backend skill, recorded with citations in the review's Strengths to Keep section. The positive counterpart of a Finding.

**Examples:** backend's shared/query dynamic list engine is recorded as a Strength to Keep.

**Synonyms:** 

**Related:** Review Doc, Finding, Recommended Target Pattern

---

## Backend Patterns

### Compensation

**Term:** Compensation

**Definition:** An undo action that reverses an already-completed external side effect when a later step fails, used where a database transaction cannot roll the side effect back. In wpmanager, compensateUpload deletes the object from storage when recording the FileEntity fails.

**Examples:** StorageProviderManager: phase-2 exception → compensateUpload → client.deleteFile(name) → throw FailUpload.

**Synonyms:** compensating action

**Related:** Two-Phase Upload, Upload Coordinator

---

### DTO Tiers

**Term:** DTO Tiers

**Definition:** The fixed set of data shapes each entity is exposed through. In the Guide there are three, generated by MapStruct: Request (create and update input; no id, no owner, no server-controlled fields), Response (detail output, also the create response) and Summary (list row). In the Reference Projects there are four: Form (write shape), DTO (full detail), MiniDTO (small reference used inside other DTOs) and ListDTO (grid row with denormalized counters).

**Examples:** Guide: NoteRequest, NoteResponse, NoteSummary. Reference: AdminForm, AdminDTO, AdminMiniDTO, AdminListDTO in backend's admin module.

**Synonyms:** DTO shapes, Request/Response/Summary, Form/DTO/MiniDTO/ListDTO

**Related:** Feature Module, Denormalized Counter

---

### Denormalized Counter

**Term:** Denormalized Counter

**Definition:** A Long xxxCount column on a parent entity that caches the size of a child collection so list views need no join; in BugTracker it is recomputed from collection.size() in each module's updateListFields override.

**Examples:** BusinessEntity.bsPrTaskCount, bsPriorityEntity.taskCount.

**Synonyms:** count column

**Related:** DTO Tiers

---

### Feature Module

**Term:** Feature Module

**Definition:** The standard per-entity unit of code. In the Guide it is one package under <root>.features.<feature> with a fixed file set (Controller, Service, Repository, Entity, Request, Response, Summary, Mapper, Query Profile, Access Policy and a migration) plugged into the Generic CRUD Stack, with an opt-out for features that do not fit. In the Reference Projects the same idea is a package of Controller, ServiceImpl, Repository, Mapper, Entity, Form, DTO, MiniDTO and ListDTO (ten files in backend, with a QueryProfile).

**Examples:** Guide: features/note (NoteController, NoteService, NoteRepository, Note, NoteRequest, NoteResponse, NoteSummary, NoteMapper, NoteQueryProfile, NoteAccessPolicy, V<n>__note.sql). Reference: backend's models.hq.admin (AdminController, AdminServiceImpl, AdminRepository, AdminMapper, AdminQueryProfile, AdminEntity, AdminForm, AdminDTO, AdminMiniDTO, AdminListDTO).

**Synonyms:** ten-file module, module

**Related:** Generic CRUD Stack, DTO Tiers, Platform Module, Query Profile

---

### Generic CRUD Stack

**Term:** Generic CRUD Stack

**Definition:** The reusable CRUD base that gives every Feature Module get, list, search, create, update and delete. In the Guide it is the platform.crud base controller and service: update really applies the request, hooks (validate, apply relations, beforeDelete) carry feature rules, one access policy per feature decides authorization and row visibility, writes are conditional (ETag and If-Match), and transactions are declared per entry point. In the Reference Projects it is DefaultController / DefaultServiceImplements, which originates in BugTracker and was inherited, evolved, by wpmanager and backend.

**Examples:** Guide: abstract CrudService<REQ, RES, SUM, E, ID>. Reference: BE-R02-01 and BT-R02-01, where the base update re-saves the entity without applying the request form.

**Synonyms:** DefaultController stack, generic CRUD framework, CRUD base, CrudService

**Related:** Feature Module, Lineage, Platform Module, Query Profile

---

### Idempotency Key

**Term:** Idempotency Key

**Definition:** The identifier under which a repeatable request is registered so that a retry or a concurrent duplicate is detected. In the Guide it is the client-supplied Idempotency-Key header, scoped by Current User and paired with a request fingerprint by the Idempotency Guard. In wpmanager it is the uploaded file's SHA-256 checksum: IdempotencyManager tracks each key through PENDING, PROCESSING, then COMPLETED (stores the serialized result) or FAILED, replays the stored result for a completed key, returns HTTP 409 while a key is in flight, and expires keys after 24 hours.

**Examples:** IdempotentUploadService wraps POST /plugin/upload; the checksum-only key is flagged as a limitation in wpmanager's Idempotency Review.

**Synonyms:** upload idempotency

**Related:** Two-Phase Upload, Idempotency Guard

---

### Two-Phase Upload

**Term:** Two-Phase Upload

**Definition:** wpmanager's upload flow in StorageProviderManager: phase 1 stores the object in the default Storage Provider, phase 2 records a FileEntity for it in the database. If phase 2 fails, Compensation deletes the stored object so storage and database stay consistent. Both phases run inside the transaction opened by the service's upload method.

**Examples:** PluginService.upload → StorageProviderManager uploads the ZIP, then records the FileEntity.

**Synonyms:** 

**Related:** Compensation, Storage Provider, Idempotency Key, Upload Coordinator

---

## Guide and Convention

### Base Project

**Term:** Base Project

**Definition:** The tested reference implementation of the Platform Modules in base-project/, built last against the gated Guide contracts to prove the convention is implementable. It pins exactly one Spring Boot line, is never shipped with the Skill and is never a copy source.

**Examples:** base-project/ holds platform.* plus one sample Feature Module (features/note) used by its tests.

**Synonyms:** reference implementation

**Related:** Guide, Platform Module, Validation Run

---

### Contract Review

**Term:** Contract Review

**Definition:** A blind design review of the Guide's module contracts (interfaces, invariants, error modes and the interactions between modules), written in the review format of the analysis documents under Docs/Guide/Reviews/. Its findings use the IDs GC-R<NN>-<MM>, and it gates the Skill: no Critical and no High finding may stay open.

**Examples:** GC-R01-02: the update contract does not say what happens to a field that is absent from the request.

**Synonyms:** GC review, contract design review

**Related:** Guide, Module Contract, Finding ID, Rule ID

---

### Depth Criterion

**Term:** Depth Criterion

**Definition:** The measure that shows whether the CRUD base is deep enough: across a project's CRUD features, fewer than half replace create or update wholesale. It is evaluated in every Validation Run; if it fails in two runs, the decision behind the CRUD base is revisited.

**Examples:** wpmanager would fail it: create was replaced in 11 of 12 services.

**Synonyms:** CRUD depth criterion

**Related:** Generic CRUD Stack, Service Hook, Exit Gate

---

### Draft Marker

**Term:** Draft Marker

**Definition:** The #draft tag on a Guide document. A draft is complete and binding in its rules and module contracts; its narrative, its Differs From the Reference Projects section and its Version Notes are still to be finalised against the Base Project. The Guide index reports the number of drafts.

**Examples:** #doc #guide #api-design #draft on 06-Query-Engine.md until the document is finalised against the Base Project.

**Synonyms:** draft document

**Related:** Guide, Version Note, Base Project

---

### Guide

**Term:** Guide

**Definition:** The version-agnostic architecture and design guide for future Spring Boot REST APIs, kept in documentation/Docs/Guide/. It is the only place rules are stated: the Base Project and the Skill cite its Rule IDs, and the Reference Projects are evidence for it, not authority.

**Examples:** Docs/Guide/06-Query-Engine.md states rule G06-03; the Skill cites G06-03 instead of restating it.

**Synonyms:** architecture guide, the convention

**Related:** Guide Rule, Rule ID, Base Project, Reference Project

---

### Guide Rule

**Term:** Guide Rule

**Definition:** One addressable, checkable statement in a Guide document, written as a block with Rule, Why, Evidence and Differs-from-references fields. Every Guide Rule cites at least one Finding ID or ADR.

**Examples:** G06-03: every filterable or sortable field is declared once in the Query Profile; anything not declared is rejected.

**Synonyms:** rule

**Related:** Guide, Rule ID, Finding ID

---

### Module Contract

**Term:** Module Contract

**Definition:** What the Guide and the Skill state about a Platform Module instead of code: its interface, its invariants and its error modes, plus how it interacts with other modules. Projects implement each contract fresh against current framework documentation.

**Examples:** The Upload Coordinator contract: store(source, key, persist); invariant: no database transaction is open during storage I/O; error mode: a digest mismatch fails with 422 before the object is finalised.

**Synonyms:** contract

**Related:** Platform Module, Guide, Base Project

---

### Rule ID

**Term:** Rule ID

**Definition:** The stable identifier of a Guide Rule, formatted G<NN>-<MM>, where NN is the Guide document number and MM is sequential within that document. Rule IDs are not renumbered once the Skill cites them. Distinct from a Finding ID (<PROJ>-R<NN>-<MM>), a Guide contract review ID (GC-R<NN>-<MM>) and a validation finding ID (V<NN><X>-R<NN>-<MM>).

**Examples:** G04-02 (Guide document 04, rule 02).

**Synonyms:** Guide rule ID

**Related:** Guide Rule, Finding ID

---

### Traceability Matrix

**Term:** Traceability Matrix

**Definition:** The table in the Guide that maps every in-scope reference Finding to the Guide Rules that prevent it, or to N/A with a reason. In scope: every Critical and High finding, plus every finding of a security review whatever its severity. The validator checks that each in-scope finding has exactly one row.

**Examples:** BT-R05-07 | High | G06-03

**Synonyms:** coverage matrix

**Related:** Guide, Guide Rule, Finding, Rule ID

---

### Version Note

**Term:** Version Note

**Definition:** An evidence-scoped entry in the Version Notes section of a Guide document. It holds version-specific detail (class and API names) so that rule text stays behavioural. Each note is marked either verified on a named line, with a resolvable citation, or not verified, meaning the detail is looked up in current documentation at execution time.

**Examples:** verified on 4.1.x: the base-project test that proves the no-transaction contract of the Upload Coordinator.

**Synonyms:** 

**Related:** Guide, Guide Rule, Base Project

---

## Platform Modules

### Access Policy

**Term:** Access Policy

**Definition:** The single authorization module of a Feature Module, <F>AccessPolicy, with two methods: check(action, actor, entityOrNull) and rowScope(actor). It holds role rules, entity-conditioned rules and row visibility in one file and denies when no rule matches. Method-security annotations are banned in feature code.

**Examples:** NoteAccessPolicy.check(Action.update, actor, note) allows the owner and an admin; rowScope(actor) returns ownedBy(owner, actor) for a normal user.

**Synonyms:** <F>AccessPolicy, AccessPolicy

**Related:** Row Scope, Current User, Feature Module, Generic CRUD Stack

---

### Actor Label

**Term:** Actor Label

**Definition:** The text stored in the created-by and updated-by fields of a managed entity: the user id of the entry point's actor, or the word system for the System Actor. It is a label, not a foreign key, and the CRUD base writes it from the actor parameter, never from an ambient read.

**Examples:** A note created by user 42 has createdBy = 42; one created by a scheduled import has createdBy = system.

**Synonyms:** createdBy, updatedBy

**Related:** System Actor, Current User, Entry Point

---

### Claims Mapper

**Term:** Claims Mapper

**Definition:** The port that turns a verified JWT into provider-neutral identity claims (subject, e-mail, display name, roles). It has one adapter per token source: a local adapter for the Token Issuer and an external adapter configured per identity provider.

**Examples:** The external adapter reads roles from the claim path configured for Clerk or WorkOS.

**Synonyms:** ClaimsMapper

**Related:** Current User, Token Issuer

---

### Concurrency Token

**Term:** Concurrency Token

**Definition:** The server-owned version value every entity managed by the CRUD base carries. Only the persistence layer writes it. Get, create and update hand it to the client as a strong ETag, and update and delete require it back in If-Match.

**Examples:** A note read with ETag "5" can be updated only while its token is still 5.

**Synonyms:** version token, entity version

**Related:** Conditional Write, Generic CRUD Stack

---

### Conditional Write

**Term:** Conditional Write

**Definition:** The rule that update and delete must carry the entity's strong ETag in If-Match. A stale tag fails with 412, a missing one with 428, and If-Match: * is the explicit unconditional write. There is no version field in the request body.

**Examples:** PUT /notes/7 with an outdated If-Match returns 412; without If-Match it returns 428.

**Synonyms:** Conditional-Write Contract

**Related:** Generic CRUD Stack

---

### Current User

**Term:** Current User

**Definition:** The value object (userId, subject, roles) that represents the actor of an entry point, and the only identity type feature code may import. Every entry point receives it as an explicit parameter: the HTTP edge resolves it once and passes it down, and scheduled, worker and bootstrap work pass the system sentinel CurrentUser.system().

**Examples:** A controller resolves the actor once and calls service.update(id, request, actor); a scheduled job calls the same service with CurrentUser.system().

**Synonyms:** CurrentUser, actor

**Related:** Claims Mapper, Token Issuer, Access Policy, Row Scope, User Directory

---

### Domain Exception

**Term:** Domain Exception

**Definition:** An unchecked exception of the convention's single hierarchy, raised by a service, a hook or a Platform Module to report a failure. Each kind carries its error code: status, Problem Type and title. The kinds are invalid request, not found, forbidden, conflict, precondition failed, precondition required and business rule violation.

**Examples:** A delete hook raises Conflict when an order still has payments; the client receives 409 with type /problems/conflict.

**Synonyms:** DomainException

**Related:** Problem Type, Service Hook, Entry Point

---

### Download Ticket

**Term:** Download Ticket

**Definition:** A short-lived HMAC capability token issued for a stored file after the feature's authorization check. One platform redemption route verifies it, re-checks that the object still exists, and then redirects to a presigned URL or streams the bytes. It never exposes the object key.

**Examples:** A tampered ticket reads as 404, an expired one as 410.

**Synonyms:** DownloadTicket

**Related:** Object Storage, Access Policy, File Signature

---

### Entry Point

**Term:** Entry Point

**Definition:** An operation through which work enters a module from outside: a controller route, one of the six operations of the CRUD base, a scheduled trigger or a worker. In the Guide every entry point names its actor as a parameter, consults the Access Policy once, and declares its own transaction.

**Examples:** CrudService.update(id, request, precondition, actor) is an entry point; a private helper it calls is not.

**Synonyms:** 

**Related:** Current User, Access Policy, Generic CRUD Stack

---

### Idempotency Guard

**Term:** Idempotency Guard

**Definition:** The Platform Module that makes a non-repeatable POST safe to retry. It is opt-in per endpoint: it scopes the client Idempotency Key by Current User, compares a request fingerprint, replays the stored response for a completed key, rejects a reused key with a different request (422) and rejects an in-flight duplicate (409).

**Examples:** A retried upload with the same Idempotency-Key and the same content digest returns the original 201 response.

**Synonyms:** IdempotencyGuard

**Related:** Idempotency Key, Upload Coordinator, Current User

---

### Identity Mode

**Term:** Identity Mode

**Definition:** The single setting (app.identity.mode) that chooses how tokens are verified: local, with the public key of the application's own Token Issuer, or external, with the key set an identity provider publishes. It also decides who writes roles and whether a user record is created on first request.

**Examples:** Moving login to an external provider ends with app.identity.mode=external.

**Synonyms:** app.identity.mode, local mode, external mode

**Related:** Token Issuer, Claims Mapper, User Provisioning, User Directory

---

### Object Storage

**Term:** Object Storage

**Definition:** The port for storing and reading binary objects by object key (put, open, exists, delete), with an S3-compatible adapter and a local-filesystem adapter selected by configuration and verified by one shared contract test suite.

**Examples:** app.storage.provider=local in development and s3 in production, with no code change.

**Synonyms:** ObjectStorage, storage port

**Related:** Upload Coordinator, Storage Provider, Download Ticket

---

### Page Response

**Term:** Page Response

**Definition:** The wire format of a list or a search, owned by the convention: items, page (zero-based), size, totalItems, totalPages. The totals count only rows the caller may see. A framework's own page type is never serialised.

**Examples:** GET /api/v1/notes?page=0&size=20 answers {"items": [], "page": 0, "size": 20, "totalItems": 0, "totalPages": 0}.

**Synonyms:** PageResponse

**Related:** Query Profile, Row Scope

---

### Platform Module

**Term:** Platform Module

**Definition:** A shared, feature-independent module under <root>.platform.* with one responsibility and a small interface: identity, access, crud, query, errors, storage, idempotency or config. Platform Modules never import Feature Modules. The Guide and the Skill state each one as a contract (interface, invariants, error modes); the Base Project implements them.

**Examples:** platform.query exposes one deep entry point: ListQuery.run(profile, scope, request, toSummary).

**Synonyms:** platform package

**Related:** Feature Module, Base Project, Guide, Module Contract

---

### Problem Type

**Term:** Problem Type

**Definition:** The stable identifier of one failure shape, of the form /problems/<name>, carried in the type member of an RFC 9457 problem detail. Every type has one meaning and one status and is listed in the application's registry; clients switch on it, never on the detail text.

**Examples:** /problems/validation-failed (400), /problems/precondition-failed (412), /problems/content-digest-mismatch (422).

**Synonyms:** problem type URI, type URI

**Related:** Domain Exception, Conditional Write

---

### Query Profile

**Term:** Query Profile

**Definition:** The per-entity whitelist of filterable and sortable fields (name, path, type, operators, sortable) plus the default sort and the query bounds. Anything not declared is rejected. It restricts which fields a client may query; it does not restrict which rows the caller may see.

**Examples:** NoteQueryProfile declares title (contains, equals; sortable) and createdAt (range; sortable).

**Synonyms:** QueryProfile, field whitelist

**Related:** Feature Module, Generic CRUD Stack, Row Scope

---

### Row Scope

**Term:** Row Scope

**Definition:** The technology-neutral description of which rows an actor may see (ownedBy, all, none, system). It is produced by the Access Policy and ANDed into every get, list, search, update and delete query before paging, so totals and page counts reflect only visible rows. A row outside the scope reads as 404. It is the named extension point for multi-tenancy.

**Examples:** rowScope(actor) = ownedBy(ownerPath, actor): a list returns only the caller's rows and GET on another user's row returns 404.

**Synonyms:** RowScope, row visibility

**Related:** Access Policy, Query Profile, Current User

---

### Service Hook

**Term:** Service Hook

**Definition:** One of the four methods a Feature Module fills in to customise the CRUD base without replacing an operation: validate on create, validate on update, apply relations, before delete. Hooks hold invariants of the data; rules that depend on the caller belong to the Access Policy.

**Examples:** NoteService.beforeDelete(note) refuses to delete a note that has attachments.

**Synonyms:** hook, CRUD hook

**Related:** Generic CRUD Stack, Access Policy, Feature Module

---

### System Actor

**Term:** System Actor

**Definition:** The Current User that stands for work with no caller: CurrentUser.system(), whose user id is the reserved word system. Scheduled triggers, workers and the start-up bootstrap pass it explicitly. It is an ordinary actor: it goes through the same Access Policy, carries no role, and no token or user record can produce it.

**Examples:** A nightly clean-up calls noteService.delete(id, precondition, CurrentUser.system()); NoteAccessPolicy has a narrow rule that allows it.

**Synonyms:** CurrentUser.system(), system sentinel

**Related:** Current User, Access Policy, Row Scope, Actor Label

---

### Token Issuer

**Term:** Token Issuer

**Definition:** The removable local module (platform.identity.local) that issues our own JWTs: login, refresh and logout, the LocalCredential and RefreshToken tables in its own migrations, and the local admin user management. Nothing outside the module imports it, so deleting it and switching the identity mode to external moves login to an external identity provider without touching business code.

**Examples:** POST /auth/login returns a short-lived access token signed with an asymmetric key read from configuration.

**Synonyms:** local issuer, local token issuer

**Related:** Current User, Claims Mapper

---

### Upload Coordinator

**Term:** Upload Coordinator

**Definition:** The deep entry point that makes storing a file and recording it atomic for the caller: it streams the content to Object Storage with no database transaction open while verifying the declared content digest, persists in a short transaction, and deletes the object (Compensation) if persistence fails. It is the Guide replacement for the reference Two-Phase Upload.

**Examples:** uploadCoordinator.store(source, key, storedFile -> attachToNote(noteId, storedFile))

**Synonyms:** UploadCoordinator

**Related:** Object Storage, Compensation, Two-Phase Upload, Idempotency Guard

---

### User Directory

**Term:** User Directory

**Definition:** The single lifecycle contract for user records in both identity modes: create, setStatus and rebindSubject. It is the only path that creates or changes a user. Rebinding a user to an external subject is what turns the move to an external identity provider into a data-preserving runbook.

**Examples:** Migration to an external provider: rebindSubject(userId, providerSubject) per user, then delete the Token Issuer module.

**Synonyms:** UserDirectory

**Related:** Current User, Token Issuer, Claims Mapper

---

### User Provisioning

**Term:** User Provisioning

**Definition:** The operation that finds the user record of a verified token by its subject and, in external Identity Mode only, creates it on the first request. The creation is safe against two simultaneous first requests and runs in its own transaction.

**Examples:** The first request of a new provider user creates the user record; in local mode an unknown subject is refused.

**Synonyms:** UserProvisioning, provisioning on first request

**Related:** User Directory, Claims Mapper, Identity Mode, Current User

---

## Reference Domains

### Default Provider

**Term:** Default Provider

**Definition:** The one Storage Provider that uploads are written to synchronously during the request; every other provider receives the file later through Replication.

**Examples:** Two-Phase Upload phase 1 always targets the default provider.

**Synonyms:** 

**Related:** Storage Provider, Replication

---

### Downloadable

**Term:** Downloadable

**Definition:** In wpmanager, one uploaded version of a plugin or theme (DownloadableEntity), carrying its checksum, signature and a map of its File Copies per Storage Provider. Separating the version from its copies is what makes multi-provider storage possible.

**Examples:** Uploading plugin v1.2.0 creates one DownloadableEntity.

**Synonyms:** version, DownloadableEntity

**Related:** File Copy, File Signature

---

### File Copy

**Term:** File Copy

**Definition:** In wpmanager, a FileEntity: the record that one Downloadable is stored in one specific Storage Provider. A Downloadable has one copy per provider it has been uploaded or replicated to.

**Examples:** After Replication, a version stored in AWS and Wasabi has two FileEntity rows.

**Synonyms:** FileEntity, copy

**Related:** Downloadable, Storage Provider, Replication

---

### File Signature

**Term:** File Signature

**Definition:** An HMAC-SHA256 (Base64) of a version's checksum plus its name_version, computed by FileSigner with the file.signature.secret key when a Downloadable is uploaded, so a file's integrity and origin can be checked later.

**Examples:** PluginService.upload: signature = HMAC(checksum + name_version).

**Synonyms:** 

**Related:** Downloadable

---

### HQ

**Term:** HQ

**Definition:** The operator side of BugTracker's two-sided SaaS (package models/HQ): a single MainHQ that sells plans to clients, employs HQ employees and admins, and issues SaaS invoices. Contrasted with the tenant side (models/client).

**Examples:** HQ invoices, HQ employees, MainHQ.

**Synonyms:** operator side

**Related:** Tenant

---

### Plan Entitlement

**Term:** Plan Entitlement

**Definition:** What a wpmanager client's plan grants: the WordPress product IDs it may use (PlanEntity.wpIDs), a website count limit and a download limit. As implemented, no endpoint yet enforces the wpIDs or the download limit.

**Examples:** Plan: name, wpIDs, websiteCountLimit, downloadLimit.

**Synonyms:** plan, wpIDs

**Related:** Downloadable

---

### Replication

**Term:** Replication

**Definition:** wpmanager's scheduled copying of every uploaded version to every Storage Provider that lacks it: the FileDuplicator job downloads the file from a provider that has it into a temporary file and uploads it where it is missing.

**Examples:** schedule/FileDuplicator

**Synonyms:** file duplication

**Related:** Storage Provider, Default Provider, File Copy

---

### Storage Provider

**Term:** Storage Provider

**Definition:** In wpmanager, a persisted configuration row for one file store (an S3-compatible bucket such as AWS S3 or Wasabi, or an FTP server) whose getClient() returns an adapter for it. One provider is the Default Provider; the others receive copies through Replication. S3StorageClient is the only working adapter.

**Examples:** A Wasabi bucket reached through the AWS SDK v2 endpointOverride.

**Synonyms:** provider

**Related:** Default Provider, Replication, File Copy

---

### Tenant

**Term:** Tenant

**Definition:** In BugTracker, a customer Business (BusinessEntity) whose workspace owns its own users, projects, tasks, channels, comments, docs, knowledge base and configurable statuses, priorities and types (package models/client). Tenant-owned modules use the bs prefix.

**Examples:** BT-R01-02: nothing isolates one tenant's data from another's.

**Synonyms:** Business, tenant workspace

**Related:** HQ

---

## Validation Loop

### Evidence Pack

**Term:** Evidence Pack

**Definition:** The frozen, committed copies of every file a validation review cites, stored whole under Docs/Validation/Run-NN/<Domain>/evidence/, so that citations keep resolving after validation-runs/ is cleaned.

**Examples:** evidence/src/main/java/.../NoteService.java:42 cited by a run 01 finding.

**Synonyms:** 

**Related:** Validation Run, Rule-Coverage Ledger

---

### Exit Gate

**Term:** Exit Gate

**Definition:** The condition that ends the Validation Loop. A run passes when both domains build, all tests pass, both start on PostgreSQL, the blind review reports 0 Critical and 0 High findings with a complete rule-coverage ledger, and the CRUD depth criterion holds. The loop ends after two consecutive passing runs, or at run 5, when the user decides.

**Examples:** A run with one High finding fails the gate and resets the consecutive-pass count.

**Synonyms:** 

**Related:** Validation Run, Validation Domain, Rule-Coverage Ledger

---

### Rule-Coverage Ledger

**Term:** Rule-Coverage Ledger

**Definition:** The table in every validation review that lists each applicable Rule ID exactly once, as either a finding or checked with no finding plus an Evidence Pack citation. It makes the absence of findings a checkable claim.

**Examples:** G06-03 | checked - no finding | evidence/src/.../NoteQueryProfile.java:12

**Synonyms:** 

**Related:** Validation Run, Exit Gate, Rule ID, Evidence Pack

---

### Validation Domain

**Term:** Validation Domain

**Definition:** One of the two small business domains (4-6 entities each) that the user defines to test the Skill. Together they must cover one-to-many, many-to-many, user-owned resources, role-restricted endpoints, a filtered and sorted list, and a file upload with idempotency.

**Examples:** Recorded in Docs/Validation/Validation-Domains.md once the user defines them.

**Synonyms:** 

**Related:** Validation Run, Exit Gate

---

### Validation Run

**Term:** Validation Run

**Definition:** One generate, build, test, review and fix cycle of the Validation Loop: the Skill generates a project for each Validation Domain, the projects are built, tested and started on PostgreSQL, a blind reviewer writes findings, and each finding is fixed at its root cause in the Guide, the Skill or the Base Project.

**Examples:** Run 01 is a planned discovery run; its findings have root cause guide or skill only.

**Synonyms:** run

**Related:** Validation Domain, Exit Gate

---

