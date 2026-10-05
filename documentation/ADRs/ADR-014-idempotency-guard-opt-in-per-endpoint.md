#adr #adr-accepted #api-design #backend

## ADR 014: Idempotency Guard, opt-in per endpoint

### Status
Accepted

### Context

The one reference project with an idempotency mechanism keys it on the file checksum alone, so a reused
archive replays another upload's result; a crash leaves a key pending and blocks retries for 24 hours; the
state keeper is a per-instance cache with an unhandled key race; and every failure inside the upload
becomes 500. It also hashes the upload file twice per request. Decision D14 of
[[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] fixes the guard; review decision F5 made
the fingerprint a declared content digest rather than a request-body hash, so streaming uploads are never
buffered.

**Evidence:**
[[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01|WP-R04-01]] — every failure inside the upload
becomes 500;
[[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-02|WP-R04-02]] — the key is the file checksum only,
so a reused archive replays another upload's result;
[[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-03|WP-R04-03]] — a crash leaves a key pending and
blocks retries;
[[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-04|WP-R04-04]] — single-node assumptions: a
per-instance cache and an unhandled key race;
[[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-05|WP-R04-05]] — a shallow state keeper hard-wired
to one DTO;
[[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-06|WP-R04-06]] — the upload file hashed twice per
request;
[[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-04|WP-R05-04]] — whole objects buffered in
memory, which is what a body hash would force again.

### Decision

1. We will give the guard one entry point: execute an operation under a key and a fingerprint, returning
   the stored result on replay.
2. We will make it opt-in per endpoint through a marker on the controller method. An interceptor reads the
   client `Idempotency-Key` header, scopes it by the current user, builds the fingerprint and calls the
   guard.
3. We will build the fingerprint as: for buffered JSON requests, method + path + canonical non-file
   fields; for file and streaming requests, method + path + canonical non-file fields + the declared
   content digest. No interceptor or guard reads a file body.
4. We will make the digest mandatory for file requests. A request without one is invalid by itself and is
   rejected before the guard runs, with the validation status of
   [[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]] — 400 under the recommendation
   recorded there.
5. We will use these states: in progress (with a lease) → completed (stored response) or failed. Same key
   and fingerprint with a completed state → replay. Same key, different fingerprint → 422. A live lease →
   409. An expired lease → retry allowed.
6. We will rethrow the original exception after the failure is recorded.
7. We will persist only (a unique index), with no per-instance cache; a scheduled cleanup removes expired
   rows.
8. We will never guard searches and downloads.
9. We will document a fallback for a client that cannot compute a digest: spool the upload once and hash it
   in that same pass. It is never a per-adapter default.
10. **Uploads carry the marker by convention, not by the guard.** The mechanism stays opt-in per endpoint
    with no exception (user story 38). The convention's upload recipe opts in: an upload endpoint built
    from the Guide carries the marker unless the feature records why not. The default lives in the recipe a
    developer follows, never in the guard.

**Depth statement:** one method hides the state machine, leases, replay and failure recording.

**Alternatives rejected:**
- A key made of the file checksum only — the reference design; it replays another upload's result.
- Hashing the request body — forces buffering, which breaks streaming uploads (F5).
- Leaving file content out of the fingerprint — a reused key would replay another upload, breaking user
  story 37.
- A per-instance cache — the reference design; it is single-node and racy.
- The Upload Coordinator applies idempotency itself — a second responsibility, and an HTTP-header concern
  reaching a non-HTTP caller.

### Consequences

- A retried POST cannot duplicate work.
- Clients must send a key, and a digest for files.
- Each project that uses the guard carries a table and a cleanup job.
- Stored responses take space until they expire.

**Related:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] (D14; F5),
[[ADRs/ADR-013-object-storage-port-upload-coordinator-and-download-tickets|ADR-013]],
[[ADRs/ADR-009-unchecked-domain-exceptions-as-problem-details|ADR-009]].
