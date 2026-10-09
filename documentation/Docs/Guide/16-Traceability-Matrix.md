# Traceability Matrix

#doc #guide #architecture #testing #draft

**Guide:** [[Docs/Guide/Guide-Index]] · **Conventions:** [[Docs/Guide/Guide-Conventions]]

## Purpose

This document proves that the defects found in the three reference projects are covered. Every finding
that is in scope has one row that names the Guide rules that prevent it. A reader who doubts a rule can
follow it back to the defect it exists for, and a reader who knows a defect can find the rule that stops
it from being built again. The one idea to take away: the Guide keeps the shape of the reference
projects and removes their defects, and this table is where that claim can be checked line by line.

## Design

### Scope

The matrix holds 78 findings ([[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]]):

- every 🔴 Critical and 🟠 High finding of the three review sets, and
- every finding of a security review — `BE-R01-*`, `BT-R01-*`, `WP-R01-*` — whatever its severity, so
  that a medium or low security finding cannot be lost behind its severity.

| Review set | 🔴 | 🟠 | 🟡 | 🟢 | In scope |
|---|---|---|---|---|---|
| `backend/` | 4 | 10 | 1 | 1 | 16 |
| `BugTracker/` | 5 | 19 | 4 | 3 | 31 |
| `wpmanager/` | 9 | 16 | 4 | 2 | 31 |
| **Total** | 18 | 45 | 9 | 6 | 78 |

### How to read a row

| Column | Holds |
|---|---|
| Finding | The Finding ID. It resolves to its review document |
| Severity | The severity the review gave it |
| Defect | The finding's title |
| Rule(s) | The rules that prevent the defect in a project built from the Guide |
| Note | Empty when the rules cover the defect; `partial — <what is left>` when they do not cover all of it |

- A row names the rules that **prevent** the defect, which is fewer than the rules that cite the finding
  as evidence.
- 68 rows are covered in full and 10 are partial. No row is `N/A`: every finding in scope has
  at least one rule.
- A partial row is never counted as covered. What it leaves open is stated in its note and is one of
  three kinds: something outside the convention's reach (a secret already in a history), something a
  project adds on top (tenancy, replication), or a coding defect of one feature that a convention can
  only make a test find.
- The matrix is checked by the validator: every finding in scope has exactly one row, and no row names a
  withdrawn rule.

### When the Guide changes

A rule that is withdrawn is replaced in every row that names it, in the same change. A finding added to
a review set, or raised in severity, gets its row in the same change — the validator fails until it has
one.

## Traceability Matrix

| Finding | Severity | Defect | Rule(s) | Note |
|---|---|---|---|---|
| BE-R01-01 | 🔴 | No authorization is enforced anywhere | G04-07, G04-08, G08-25, G13-03 |  |
| BE-R01-02 | 🔴 | Anyone can mint a client JWT | G08-18 |  |
| BE-R01-03 | 🔴 | Spring Data REST likely exports user repositories, including password hashes and write operations | G02-09, G05-10, G08-09, G14-02 |  |
| BE-R01-04 | 🟠 | Seed admin with hardcoded password | G08-12, G08-23 |  |
| BE-R01-05 | 🟠 | One secret for JWT signing and HMAC password derivation | G08-15, G10-18, G12-05 |  |
| BE-R01-06 | 🟠 | Client passwords are derived from e-mail; rotation is dead code | G08-17 |  |
| BE-R01-07 | 🟠 | JWT design: claims-only trust, constant subject, no issuer check, no revocation | G08-01, G08-02, G08-04, G08-10, G08-16 |  |
| BE-R01-08 | 🟠 | Account-status flags are ignored | G08-10, G08-19, G08-20, G08-22, G13-07 | partial — the lockout is per credential; throttling per client address belongs to the deployment layer (G08-22), so password spraying remains a named risk |
| BE-R01-09 | 🟢 | JWT filter is also registered as a servlet filter | G08-01 |  |
| BE-R01-10 | 🟡 | CORS origin hardcoded; injected source ignored | G08-24, G12-10 |  |
| BE-R01-11 | 🟠 | Literal secrets in the working tree and in git history | G08-15, G12-03, G12-04, G12-14 | partial — the rules keep new secrets out; a secret already in the history stays exposed until it is rotated |
| BE-R02-01 | 🔴 | Base `update` ignores the form | G03-06, G04-04, G13-08 |  |
| BE-R03-03 | 🟠 | Roles stored as ordinals | G07-12, G08-13 |  |
| BE-R03-04 | 🟠 | No migrations; `ddl-auto=update` in the runtime config | G07-02, G07-03, G07-14, G15-02 |  |
| BE-R08-01 | 🟠 | `@WithMockUser` masks the missing authorization | G13-01, G13-03, G13-05 |  |
| BE-R08-02 | 🟠 | Login, JWT and all write operations are untested | G13-07, G13-08, G13-10, G15-02 |  |
| BT-R01-01 | 🔴 | Authorization is authentication only | G04-07, G04-08, G08-25 |  |
| BT-R01-02 | 🔴 | No tenant isolation | G04-09, G06-01, G06-10, G06-11 | partial — visibility by owner is enforced in every query; tenancy itself is outside the Base Project (ADR-008) and is added by composing the Row Scope |
| BT-R01-03 | 🔴 | JWT key, DB credentials and seed passwords are literals in source | G08-15, G08-23, G12-03 |  |
| BT-R01-04 | 🟠 | `POST /user` accepts roles from the caller | G03-04, G08-12, G08-13 |  |
| BT-R01-05 | 🟠 | Authors and actors come from the request body | G02-06, G03-04, G04-02, G04-06 |  |
| BT-R01-06 | 🟠 | Usernames unique only per user kind; login picks an arbitrary match | G07-13, G08-04, G08-08, G08-11 |  |
| BT-R01-07 | 🟠 | Spring Data REST likely exports every repository | G05-10 |  |
| BT-R01-08 | 🟡 | Token validator fails with 500 instead of 401 | G08-01, G08-28, G09-08, G09-14 |  |
| BT-R01-09 | 🟡 | Token generator dereferences before its null check; filters registered twice | G08-01 |  |
| BT-R01-10 | 🟡 | Ten-day tokens, no revocation, constant subject | G08-02, G08-04, G08-15, G08-16 |  |
| BT-R01-11 | 🟡 | Account-status flags are ignored | G08-19, G08-20, G08-22 | partial — the lockout is per credential; throttling per client address belongs to the deployment layer (G08-22), so password spraying remains a named risk |
| BT-R01-12 | 🟢 | Unknown username raises `NoSuchElementException` | G08-21 |  |
| BT-R01-13 | 🟢 | `ErrorResponseBody.trace` returns exception text | G09-05, G09-06 |  |
| BT-R01-14 | 🟢 | Pre-6.x security configuration style | G01-08 |  |
| BT-R02-01 | 🔴 | Base `update` ignores the form (22 of 31 services) | G04-04 |  |
| BT-R02-02 | 🟠 | A client-supplied `id` on `POST` overwrites an existing row | G03-04, G04-03, G07-04, G07-08 |  |
| BT-R03-01 | 🔴 | `equals` casts to `PlanEntity` in `InvoiceEntity` and `EmployeeEntity` | G07-09 |  |
| BT-R03-05 | 🟠 | Lombok `@Builder` drops collection and flag initializers | G07-10 |  |
| BT-R03-09 | 🟠 | No migrations; `ddl-auto=update` | G07-02 |  |
| BT-R04-01 | 🟠 | Fifteen EAGER collections on the tenant root pull in the tenant graph | G06-15, G07-11 |  |
| BT-R05-01 | 🟠 | Filters are stored on a shared singleton | G06-13 |  |
| BT-R05-07 | 🟠 | No field or sort whitelist (password hashes can be probed) | G03-08, G06-03, G06-04 |  |
| BT-R06-01 | 🟠 | Uniqueness of tenant data is checked globally | G06-11, G07-13 |  |
| BT-R06-02 | 🟠 | Priority delete reorders every tenant's priorities | G04-14 | partial — a bulk change must carry the scope and the token; that the reorder belongs to one tenant is the feature's own rule |
| BT-R06-03 | 🟠 | Channel insert fails with members; its uniqueness check uses a null project | G07-10 |  |
| BT-R06-04 | 🟠 | Task category delete throws `ConcurrentModificationException` | G13-08 | partial — a coding defect in one feature's delete (a collection changed while it is read); the delete test every feature has finds it only with data that has two links |
| BT-R07-01 | 🟠 | Domain exceptions are unmapped and become 500 | G05-05, G09-03, G09-05, G09-11 |  |
| BT-R07-02 | 🟠 | Checked exceptions commit partial work | G04-16, G09-01, G13-11 |  |
| BT-R08-01 | 🟠 | Spring Boot 2.7 is out of OSS support, and this is 2.7.0 | G01-06 |  |
| BT-R09-01 | 🟠 | One test, and it needs a live MySQL | G13-02, G13-14 |  |
| BT-R09-02 | 🟠 | No coverage of security, services or the filter engine | G13-03, G13-07, G13-08, G13-10 |  |
| WP-R01-01 | 🔴 | No URL-level authorization; unannotated endpoints are anonymous | G08-25, G08-26, G13-04 |  |
| WP-R01-02 | 🔴 | `/tests3/*` lets anonymous callers upload, overwrite, delete and download bucket objects | G08-26, G10-16, G10-19, G10-22, G13-04 |  |
| WP-R01-03 | 🔴 | Inherited operations need only a login: clients can delete admins, providers and others' data | G04-01, G04-07, G04-08, G04-09, G04-10, G13-03 |  |
| WP-R01-04 | 🔴 | Storage-provider secret keys are returned by `/s3` and stored in plaintext | G10-17, G12-06, G12-07, G14-08 |  |
| WP-R01-05 | 🔴 | Spring Data REST likely exports every repository | G02-09, G05-10, G08-09 |  |
| WP-R01-06 | 🔴 | Live cloud-storage keys committed in a test file | G12-03, G12-08, G12-14, G13-02 | partial — the rules keep credentials out of tests; the committed keys stay exposed until they are rotated |
| WP-R01-07 | 🟠 | Hardcoded DB credentials, JWT fallback secret and seed admin password | G08-15, G08-23, G10-18, G12-02, G12-03 |  |
| WP-R01-08 | 🟠 | JWT design: claims-only trust, no revocation, issuer not checked | G08-01, G08-02, G08-03, G08-10, G08-16 |  |
| WP-R01-09 | 🟠 | Account-status flags are ignored | G08-19, G08-20, G08-22, G13-07 | partial — the lockout is per credential; throttling per client address belongs to the deployment layer (G08-22), so password spraying remains a named risk |
| WP-R01-10 | 🟡 | Client passwords are derived; clients authenticate only through admin-minted tokens | G08-17, G08-18 |  |
| WP-R01-11 | 🟡 | Method security is switched on from three service classes | G01-04, G02-05, G08-27 |  |
| WP-R01-12 | 🟡 | SSRF surface in website URL probing | G14-12, G14-13 | the convention ships no module that calls a caller-supplied address; the rule binds a feature that adds one |
| WP-R01-13 | 🟡 | CORS origin hardcoded; injected source ignored | G08-24, G12-10 |  |
| WP-R01-14 | 🟢 | Duplicate `@EnableWebSecurity`; JWT filter also registered as a servlet filter | G08-01 |  |
| WP-R01-15 | 🟢 | `@PreAuthorize` on the non-bean `S3StorageClient` is inert | G01-04, G02-05, G08-27 |  |
| WP-R02-01 | 🔴 | Base `update` ignores the form (admin, S3 provider, downloadable) | G04-04 |  |
| WP-R03-01 | 🔴 | A failed provider upload commits a file-less version that blocks that version forever | G10-10, G10-11, G10-24, G13-11 |  |
| WP-R03-02 | 🟠 | Storage I/O runs inside the database transaction; the two phases share it | G04-15, G10-10, G11-10, G13-11 |  |
| WP-R03-03 | 🟠 | Compensation covers only the file-row insert; later failures leave objects behind | G10-11, G10-12, G10-23, G10-25 |  |
| WP-R04-01 | 🟠 | Every failure inside the upload becomes HTTP 500 | G05-05, G09-03, G11-05, G11-07, G11-15 |  |
| WP-R04-02 | 🟠 | The key is the file checksum only; a reused ZIP replays another upload's result | G10-09, G11-03, G11-04, G11-05 |  |
| WP-R05-01 | 🟠 | A new `S3Client` per `getClient()` call, mostly never closed | G10-04 |  |
| WP-R05-02 | 🟠 | One version without a source copy aborts every replication run | G10-26, G13-06, G14-03, G14-04 | partial — replication is an optional extension (G10-26); the job rules apply to every job |
| WP-R06-01 | 🟠 | Roles stored as ordinals | G07-12 |  |
| WP-R06-02 | 🟠 | No migrations; `ddl-auto=update` | G07-02, G07-03 |  |
| WP-R06-03 | 🟠 | EAGER collections, including every provider's full file map | G07-11 |  |
| WP-R07-01 | 🔴 | Theme upload always fails | G10-01, G13-10, G15-02 | partial — a defect of one feature's code; one shared upload path removes the diverged copy that failed, and the feature's upload test finds such a defect |
| WP-R07-02 | 🟠 | Deleting a plugin or theme with two or more versions throws `ConcurrentModificationException` | G10-23, G10-24, G15-04 |  |
| WP-R07-03 | 🟠 | Plugin and theme services are near-copies that have already diverged | G01-01, G04-05, G15-01 |  |
| WP-R10-01 | 🟠 | Authorization is tested for one module only | G13-03, G13-04, G15-02 |  |
| WP-R10-02 | 🟠 | Upload, idempotency, storage and replication are untested | G13-06, G13-09, G13-10 |  |

## Rules

None — this document maps findings to the rules of documents 01 to 15 and states no rule of its own.

## Differs From the Reference Projects

None.

## Version Notes

None.

## Related Documents

- [[Docs/Guide/Guide-Index]] — the sixteen documents whose rules the rows name.
- [[Docs/Guide/Guide-Conventions]] — the scope and the row grammar of the matrix (section 9).
- [[Docs/backend/Reviews/00-Review-Summary]] — the findings of `backend/`.
- [[Docs/BugTracker/Reviews/00-Review-Summary]] — the findings of BugTracker.
- [[Docs/wpmanager/Reviews/00-Review-Summary]] — the findings of wpmanager.
- [[ADRs/ADR-001-guide-is-the-source-of-truth|ADR-001]] — why the reference projects are evidence and
  not authority.
- [[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]] — the scope of the matrix.
- [[ADRs/ADR-008-no-multi-tenancy-in-the-base-project|ADR-008]] — why tenant isolation is partial.
