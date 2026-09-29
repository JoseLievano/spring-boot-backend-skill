# Error Handling and Validation Review — wpmanager

#doc #review #ref-wpmanager #error-handling

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/11-Error-Handling]]

## Scope

Reviewed: `exceptions/*`, `shared/tools/ErrorHTTPRes`, `TextFieldValidator`, `ValidationResult`,
`UploadValidator`, the security handlers and JWT filter's error writing, Bean Validation usage on forms and
entities, and the `/tests3` controller's error style. The idempotency wrapper's exception translation is in
[[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01|WP-R04-01]].

## Verdict

There is one error shape and it is used for security errors too, which is more than many projects manage. The
coverage behind it is thin: nothing maps framework exceptions, there is no fallback handler, internal messages
reach clients on 500, and validation is mostly manual `null` checks plus a SQL-keyword blacklist that rejects
legitimate names while protecting nothing. The upload type check trusts the client's metadata. None of this is
RFC 9457.

## Strengths to Keep

- One body shape for application and security errors
  (`wpmanager/src/main/java/com/wpmanager/shared/tools/ErrorHTTPRes.java:7-20`,
  `wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:61-91`).
- Distinct statuses for not-found (404), conflict (409), bad input (400) and duplicate upload in flight (409)
  (`wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:18-105`).
- A reusable `ValidationResult` that returns the trimmed value with the verdict
  (`wpmanager/src/main/java/com/wpmanager/shared/tools/ValidationResult.java:9-33`).
- File checks factored into one component (`wpmanager/src/main/java/com/wpmanager/shared/tools/UploadValidator.java:8-33`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-01\|WP-R08-01]] | No fallback or framework-exception handlers | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-02\|WP-R08-02]] | Internal messages on 500; any `IllegalArgumentException` becomes 400 | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-03\|WP-R08-03]] | Forms carry almost no Bean Validation; constraints sit on entities | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-04\|WP-R08-04]] | SQL-keyword blacklist rejects legitimate names and protects nothing | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-05\|WP-R08-05]] | Upload type check trusts client metadata | 🟡 Medium | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-06\|WP-R08-06]] | Custom body instead of RFC 9457 problem details | 🟡 Medium | api-design | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-07\|WP-R08-07]] | Three ways to write an error; per-error `ObjectMapper` | 🟢 Low | hygiene | Confirmed (read in code) |

## Findings

### WP-R08-01
**Title:** No fallback or framework-exception handlers
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** A closed error contract: every failure leaves through the same mapping.
**Evidence:** `GlobalExceptionHandler` has nine handlers and none for `MethodArgumentNotValidException`,
`HttpMessageNotReadableException`, `MissingServletRequestParameterException`, `MaxUploadSizeExceededException`,
`DataIntegrityViolationException`, `ConstraintViolationException`, `ConcurrentModificationException` or
`Exception` (`wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:15-151`), and it does not
extend `ResponseEntityExceptionHandler`.
**Impact:** Malformed JSON, a missing `file` part, a file over 100 MB, a unique-key race
([[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-04|WP-R04-04]]) or the delete bug in
[[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-02|WP-R07-02]] return Spring Boot's default error body, a
different shape, often as 500.
**Recommendation:** Extend `ResponseEntityExceptionHandler`, add handlers for data-integrity (409) and upload
size (413), and a last-resort `Exception` handler that logs with a correlation id and returns a generic 500.
**Verified against:** Context7 `/spring-projects/spring-boot/v3.4.1` (RFC 9457 `ProblemDetail` via `ResponseEntityExceptionHandler`, verified during task creation)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-03|BE-R05-03]]

### WP-R08-02
**Title:** Internal messages on 500; any `IllegalArgumentException` becomes 400
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** Do not leak internals; map exceptions you own, not JDK types.
**Evidence:** The `IllegalStateException` and `FailUpload` handlers copy `ex.getMessage()` into 500 responses
(`wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:107-120`,
`wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:137-150`). `FailUpload` messages include
provider type, provider name and the SDK's error text
(`wpmanager/src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:260-273`).
Every `IllegalArgumentException`, including ones thrown by libraries, becomes 400
(`wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:122-135`).
**Impact:** Clients see storage vendor names and SDK diagnostics; a programming error inside a library is
reported as the client's fault.
**Recommendation:** Generic text for 5xx (details in logs with a correlation id); throw a project-owned
`InvalidRequestException` for 400 instead of mapping `IllegalArgumentException` globally.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-04|BE-R05-04]]

### WP-R08-03
**Title:** Forms carry almost no Bean Validation; constraints sit on entities
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** Validate at the boundary, declaratively.
**Evidence:** `@Valid` is on the base controller's insert and update
(`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultController.java:33`,
`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultController.java:39`), but the only form constraint
is `@NotBlank` on `AdminForm.username` (`wpmanager/src/main/java/com/wpmanager/models/hq/admin/AdminForm.java:25`).
Constraints on entities (`@Size`, `@NotEmpty`, `@Positive`) fire at flush time instead
(`wpmanager/src/main/java/com/wpmanager/shared/models/baseUser/BaseUserEntity.java:25-47`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorEntity.java:27-35`,
`wpmanager/src/main/java/com/wpmanager/models/hq/plan/PlanEntity.java:32`). Services check `null` by hand, with gaps
([[Docs/wpmanager/Reviews/07-Service-Design-Review#WP-R07-08|WP-R07-08]]).
**Impact:** Invalid input fails late as an unmapped `ConstraintViolationException` or a
`NullPointerException`, or not at all.
**Recommendation:** Constraints on every form field (`@NotBlank`, `@Size`, `@Email`, `@Positive`,
`@NotEmpty` on id sets), `MethodArgumentNotValidException` mapped to 400 with field errors, and entity
constraints kept as a second line only.
**Verified against:** N/A — Jakarta Bean Validation standard
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/03-Domain-Model-and-Persistence-Review#BE-R03-07|BE-R03-07]], [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-03|BT-R07-03]]

### WP-R08-04
**Title:** SQL-keyword blacklist rejects legitimate names and protects nothing
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** Prevent injection with parameter binding and output encoding, not input blacklists.
**Evidence:** `validateNoSqlInjection` rejects quotes, backticks, `;`, `--`, `/*`, `*/` and any string containing
`UNION`, `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `DROP`, `EXEC`, `ALTER` or `CREATE`, case-insensitively and as
substrings (`wpmanager/src/main/java/com/wpmanager/shared/tools/TextFieldValidator.java:115-128`). `AuthorService`
applies it to author names and websites (`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorService.java:63-79`).
All queries are JPA derived queries or parameterised JPQL
(`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorRepository.java:16-20`).
**Impact:** Real authors are rejected: "O'Reilly", "Selection Studio", "WP Updates Inc", a website such as
`https://createwp.com` or `https://executive-themes.com`. There is no injection path to protect.
**Recommendation:** Remove the SQL check; keep length and trimming; validate URLs with a URL validator; encode on
output where HTML is rendered.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R08-05
**Title:** Upload type check trusts client metadata
**Severity:** 🟡 Medium
**Category:** security
**Principle:** Never trust client-declared content types; verify content.
**Evidence:** `UploadValidator` checks the original file name's extension and the `Content-Type` header sent by
the client (`wpmanager/src/main/java/com/wpmanager/shared/tools/UploadValidator.java:21-32`); the bytes are never
inspected. Replication uploads with `application/octet-stream`
(`wpmanager/src/main/java/com/wpmanager/schedule/FileDuplicator.java:130-137`). The vault records the missing virus
scan as open (`wpmanager/wpManagerDocs/Bugs/to-do/No Virus Scanning.md`).
**Impact:** Any file renamed `.zip` and sent with a ZIP content type is stored and distributed to customers'
WordPress sites as a plugin — an admin-only path today, but a supply-chain risk if an admin account is
compromised ([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-07|WP-R01-07]]).
**Recommendation:** Open the upload with `java.util.zip.ZipInputStream` (or check the `PK\x03\x04` magic and walk
the entries), require a WordPress plugin/theme header file, cap entry count and uncompressed size (zip bombs),
and scan with ClamAV or a cloud scanning service before storing.
**Verified against:** N/A — JDK API
**Confidence:** Confirmed (read in code)

### WP-R08-06
**Title:** Custom body instead of RFC 9457 problem details
**Severity:** 🟡 Medium
**Category:** api-design
**Principle:** Use the standard error format your framework supports.
**Evidence:** `ErrorHTTPRes` has `timestamp`, `status`, `error`, `message`, `path`
(`wpmanager/src/main/java/com/wpmanager/shared/tools/ErrorHTTPRes.java:12-20`); no `ProblemDetail` usage and
`spring.mvc.problemdetails.enabled` is unset (`wpmanager/src/main/resources/application.properties:1-33`).
**Impact:** Clients cannot use standard problem-details tooling; no `type` URI to distinguish error kinds
(for example "version already exists" vs "invalid file").
**Recommendation:** Return `ProblemDetail` from the advice (extend `ResponseEntityExceptionHandler`), with a `type`
per domain error and extension members (`errors` for field errors, `idempotencyKey` for 409 on uploads).
**Verified against:** Context7 `/spring-projects/spring-boot/v3.4.1` (`spring.mvc.problemdetails.enabled`, verified during task creation)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06|BE-R05-06]], [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-05|BT-R07-05]]

### WP-R08-07
**Title:** Three ways to write an error; per-error `ObjectMapper`
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** One error writer.
**Evidence:** The advice builds `ErrorHTTPRes` nine times with copied code
(`wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:18-150`); the security handlers and the
filter build it again by hand, the filter with `new ObjectMapper()` per rejected token
(`wpmanager/src/main/java/com/wpmanager/configuration/filter/JWTTokenValidatorFilter.java:79-87`); `/tests3` throws
`ResponseStatusException` (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:54-75`).
Timestamps are zone-less `LocalDateTime` strings.
**Impact:** Drift between the copies; the hand-made mappers ignore Boot's Jackson configuration (dates, naming).
**Recommendation:** One `ErrorResponseWriter` bean (using the Boot `ObjectMapper`) used by the advice and the
security components; `Instant` or `OffsetDateTime` timestamps.
**Verified against:** N/A — design analysis
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-02|BE-R05-02]], [[Docs/backend/Reviews/09-Code-Hygiene-Review#BE-R09-08|BE-R09-08]]

## Recommended Target Pattern

```java
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {   // framework exceptions → ProblemDetail
    @ExceptionHandler(DomainException.class)                          // one runtime hierarchy with status + type
    ProblemDetail domain(DomainException e) { return e.toProblem(); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail conflict(DataIntegrityViolationException e) { … 409 … }
    @ExceptionHandler(Exception.class)
    ProblemDetail fallback(Exception e) { log with correlation id; return generic 500; }
}
```

Forms validated with Bean Validation at the controller; uploads validated by content; the security entry point
and denied handler write `ProblemDetail` through the same writer.

## Related Documents

- [[Docs/wpmanager/Explanations/11-Error-Handling]]
- [[Docs/wpmanager/Reviews/04-Idempotency-Review]]
- [[Docs/backend/Reviews/05-Error-Handling-Review]]
