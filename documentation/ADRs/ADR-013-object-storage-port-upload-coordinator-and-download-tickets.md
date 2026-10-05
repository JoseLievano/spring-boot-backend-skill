#adr #adr-accepted #architecture #backend #infrastructure #security

## ADR 013: Object storage port with S3-compatible and local adapters, an Upload Coordinator and Download Tickets

### Status
Accepted

### Context

The one reference project with file storage commits a file-less row when an upload fails, runs storage I/O
inside the database transaction, compensates only part of a failure, buffers whole objects in memory, keeps
the storage port on a JPA entity with a single real adapter, rebuilds object keys by parsing URLs, has no
timeouts, exposes storage secrets through the API and lets an anonymous route touch the bucket. Its file
signer signs file integrity, not links. Decision D13 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes the storage contract; review
decisions F4 kept storage I/O out of transactions, F5 added the declared content digest, and F13 added
Download Tickets.

**Evidence:**
[[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-01|WP-R03-01]] — a failed
upload commits a file-less row;
[[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-02|WP-R03-02]] — storage
I/O inside the transaction;
[[Docs/wpmanager/Reviews/03-Upload-Transactions-and-Consistency-Review#WP-R03-03|WP-R03-03]] — partial
compensation;
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-01|WP-R05-01]] and
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-03|WP-R05-03]] — a client and a bucket
check per call;
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-04|WP-R05-04]] — whole objects buffered in
memory, no multipart or streaming;
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-05|WP-R05-05]] — the port on the JPA
entity with one real adapter;
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-07|WP-R05-07]] — keys rebuilt from URLs,
no timeouts;
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-02|WP-R01-02]] — anonymous bucket access;
[[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-04|WP-R01-04]] — storage secrets returned by the API;
[[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-05|WP-R08-05]] — the type check
trusts client metadata;
[[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-05|WP-R09-05]] — the
size limit defined twice;
[[Docs/wpmanager/Reviews/02-CRUD-Framework-and-API-Design-Review#WP-R02-05|WP-R02-05]] — a delete route
that skips storage cleanup;
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02|WP-R05-02]] and
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-06|WP-R05-06]] — replication fragility.
**Cite WP-R05-04 for "never buffer a whole object": the parent's section 10 cites a storage finding
number that does not exist, corrected as P4.**

### Decision

1. We will define an `ObjectStorage` port: put, open (streams; not found when absent), exists, delete
   (idempotent).
2. We will ship two production adapters selected by `app.storage.provider`: an S3-compatible adapter (one
   reused client; endpoint override and path-style for non-AWS stores; timeouts; streaming and multipart;
   the bucket checked once at start-up) and a local-filesystem adapter (keys confined to the root, traversal
   rejected; a temporary file then an atomic move; single-node only). One shared contract test suite covers
   both; another store is a new adapter that passes the suite.
3. We will take credentials only from the environment, and never return them from an API.
4. We will build object keys with one function per feature and never parse them back from URLs. A platform
   `StoredFile` entity records key, provider, size, SHA-256, content type and creation time.
5. We will require every upload to declare the expected SHA-256 of the object's bytes: a file-scoped form
   field for multipart, one pinned header for raw streams. Which header is pinned is decided in the Guide,
   not here. The whole-message `Content-Digest` is not used, because it hashes the multipart envelope and
   can never equal the stored hash.
6. We will give the Upload Coordinator one entry point — `store(source, key, persist)`: it validates size
   and type itself, streams to storage with no database transaction open, verifies the digest in the same
   single pass and fails with 422 before the object is finalised, runs `persist` in a short transaction,
   deletes the object if persistence fails, and writes a searchable alert log if that compensation fails.
   Calling it inside an active transaction fails immediately, before any storage I/O.
7. We will treat a feature's download route as an ordinary entry point: scoped load (404), the policy check
   for the download action, then a Download Ticket — an HMAC capability token over the stored-file id and an
   expiry, which never exposes the object key. The signing secret comes from the environment with no
   default, and start-up fails without it. One platform redemption route verifies the signature in constant
   time and the expiry, re-checks that the object exists, then redirects (302) to a short-lived presigned
   URL when the adapter can mint one or streams through the port when it cannot. Statuses are as pinned in
   [[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]]. The header policy is set in
   that one controller. Redemption is non-transactional and never buffers a whole object. Authorization is
   checked when the ticket is issued, and the expiry is the exposure window.
8. We will remove an object whose owning entity is deleted after commit — the Guide defines how, plus an
   optional orphan sweep. This ADR records only the rule that it must not be left to each feature.
9. We will specify multi-provider replication as an optional extension in the Guide only; it is not built.

**Depth statement:** the port has four methods and two production adapters — a real seam. The Upload
Coordinator's one method hides transaction placement, digest verification, compensation and alerting;
callers write none of it.

**Mechanism on the current line (Spring Boot 4.1.x, 2026-10):** the AWS SDK for Java v2 `S3Client` with an
endpoint override; a no-transaction propagation setting (`NEVER`) on `store`. Not verified by a test until
the Base Project is built.

**Alternatives rejected:**
- The storage port on the JPA entity — the reference design; one adapter, and the entity owns I/O.
- Storage I/O inside the transaction — the reference design; it is what strands file-less rows.
- Presigned URLs handed straight to clients — no use-time existence check, and it exposes the key.
- Proxying every download through the application — no bandwidth offload.
- The whole-message `Content-Digest` — hashes the multipart envelope, so it can never equal the stored
  hash.

### Consequences

- Uploads leave no orphan objects and no half-written rows.
- Development works without a cloud account.
- Clients must compute a SHA-256 before uploading.
- Local storage is single-node.
- A ticket is a bearer capability until it expires; there is no revocation before the expiry.
- The reference file signer inspired the mechanism, but link signing is a designed extension, not extracted
  code.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D13; F4, F5, F13),
[[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]],
[[ADRs/ADR-014-idempotency-guard-opt-in-per-endpoint|ADR-014]],
[[ADRs/ADR-010-postgresql-flyway-and-testcontainers|ADR-010]].
