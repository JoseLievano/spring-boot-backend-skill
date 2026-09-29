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

**Related:** Two-Phase Upload

---

### DTO Tiers

**Term:** DTO Tiers

**Definition:** The convention of exposing each entity through three read shapes: MiniDTO (a small reference used inside other DTOs), DTO (full detail, also the insert response) and ListDTO (grid row: DTO fields plus denormalized counters), with a Form as the write shape.

**Examples:** AdminMiniDTO, AdminDTO, AdminListDTO, AdminForm in backend's admin module.

**Synonyms:** 

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

**Definition:** The standard per-entity unit of code in the reference projects: a package holding the same set of files for one entity (Controller, ServiceImpl, Repository, Mapper, Entity, Form, DTO, MiniDTO, ListDTO, and in backend a QueryProfile) plugged into the Generic CRUD Stack. In backend it is exactly ten files.

**Examples:** backend's models.hq.admin (AdminController, AdminServiceImpl, AdminRepository, AdminMapper, AdminQueryProfile, AdminEntity, AdminForm, AdminDTO, AdminMiniDTO, AdminListDTO).

**Synonyms:** ten-file module, module

**Related:** Generic CRUD Stack, DTO Tiers

---

### Generic CRUD Stack

**Term:** Generic CRUD Stack

**Definition:** The reusable base classes (DefaultController / DefaultServiceImplements and their interfaces) that give every Feature Module list/get/insert/update/delete endpoints for free. Originates in BugTracker and was inherited, evolved, by wpmanager and backend.

**Examples:** BE-R02-01 and BT-R02-01: the base update in this stack re-saves the entity without applying the request form.

**Synonyms:** DefaultController stack, generic CRUD framework

**Related:** Feature Module, Lineage

---

### Idempotency Key

**Term:** Idempotency Key

**Definition:** The identifier under which a repeatable request is registered so a retry or concurrent duplicate is detected. In wpmanager it is the uploaded file's SHA-256 checksum; IdempotencyManager tracks each key through PENDING → PROCESSING → COMPLETED (stores the serialized result) or FAILED, replays the stored result for a completed key, returns HTTP 409 while a key is in flight, and expires keys after 24 hours.

**Examples:** IdempotentUploadService wraps POST /plugin/upload; the checksum-only key is flagged as a limitation in wpmanager's Idempotency Review.

**Synonyms:** upload idempotency

**Related:** Two-Phase Upload

---

### Two-Phase Upload

**Term:** Two-Phase Upload

**Definition:** wpmanager's upload flow in StorageProviderManager: phase 1 stores the object in the default Storage Provider, phase 2 records a FileEntity for it in the database. If phase 2 fails, Compensation deletes the stored object so storage and database stay consistent. Both phases run inside the transaction opened by the service's upload method.

**Examples:** PluginService.upload → StorageProviderManager uploads the ZIP, then records the FileEntity.

**Synonyms:** 

**Related:** Compensation, Storage Provider, Idempotency Key

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

