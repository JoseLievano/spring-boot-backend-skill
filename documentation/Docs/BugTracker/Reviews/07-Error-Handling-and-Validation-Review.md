# Error Handling and Validation Review — BugTracker

#doc #review #ref-bugtracker #error-handling

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/12-Error-Handling]]

## Scope

Reviewed: `exeptions/*`, exception declarations in the generic stack, how services throw, transaction
rollback rules, Bean Validation usage on the 31 Forms, and what Boot 2.7's default error handling does with
unmapped exceptions. Security-filter errors are covered in
[[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-08|BT-R01-08]].

## Verdict

The vocabulary is right — not-found, already-exists, invalid-input, invalid-delete, bad-operator — and
services use it with specific messages. The plumbing is missing: none of the five exceptions is mapped, so
all become `500` with the message stripped, and because they are checked exceptions the surrounding
transaction commits instead of rolling back. Input validation is almost entirely hand-written `null` checks.
The first two findings are **defects** on Boot 2.7; the error body format is an **era gap**.

## Strengths to Keep

- A small, named exception set with default messages
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/ElementNotFoundException.java:3-10`).
- Specific messages at throw sites ("Business not found", "Can't delete a type containing tasks")
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsType/bsTypeServiceImplements.java:142-143`).
- A single `@ControllerAdvice` and one error body type as the intended place for the mapping
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/GlobalExceptionHandler.java:12-27`).
- `@Valid` is already on every generic `POST`/`PUT` body, so adding constraints to Forms takes effect
  immediately (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/controller/DefaultController.java:35-43`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-01\|BT-R07-01]] | Domain exceptions are unmapped and become 500 | 🟠 High | error-handling | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-02\|BT-R07-02]] | Checked exceptions commit partial work | 🟠 High | error-handling | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-03\|BT-R07-03]] | Validation is mostly manual `null` checks | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-04\|BT-R07-04]] | Checked exceptions without a base leak into every generic signature | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-05\|BT-R07-05]] | Custom error body instead of RFC 7807/9457 problem details | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/07-Error-Handling-and-Validation-Review#BT-R07-06\|BT-R07-06]] | Misspelled package and class names | 🟢 Low | hygiene | Confirmed (read in code) |

## Findings

### BT-R07-01
**Title:** Domain exceptions are unmapped and become 500
**Severity:** 🟠 High
**Category:** error-handling
**Principle:** Errors are part of the API contract — each failure kind has one status and one body.
**Evidence:** The only `@ExceptionHandler` handles `AccessDeniedException`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/GlobalExceptionHandler.java:15-26`); no domain exception
carries `@ResponseStatus` (`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/ElementNotFoundException.java:3`).
Boot 2.7's `ErrorProperties` defaults `includeMessage` to `NEVER` and no `server.error.*` property is set
(`BugTracker/src/main/resources/application.properties:1-6`).
**Impact:** `GET /bs_pr_task/999`, a duplicate status, a missing required field and an invalid delete all
answer `500 Internal Server Error` with no message. The front end cannot distinguish user errors from
server faults; monitoring sees user mistakes as outages.
**Recommendation:** Map in the advice: not found → 404, already exists → 409, invalid insert/delete and bad
operator → 400 (or 422), each with the exception's message; add a fallback handler for `Exception` → 500
with a correlation id and no internals.
**Verified against:** spring-boot-autoconfigure-2.7.0.jar (javap, `ErrorProperties` constructor)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-03|BE-R05-03]] — backend maps its exceptions but has no fallback.

### BT-R07-02
**Title:** Checked exceptions commit partial work
**Severity:** 🟠 High
**Category:** error-handling
**Principle:** A failed operation leaves no trace (atomicity).
**Evidence:** All five exceptions extend `Exception` (`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/InvalidDeleteOperation.java:3`);
the service transaction uses the default rule (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:19`),
which in spring-tx 5.3.20 rolls back only for `RuntimeException` and `Error`. Example: `bsType.update` unlinks
and saves the removed categories, then throws `ElementNotFoundException` if a requested category id does not
exist (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsType/bsTypeServiceImplements.java:117-127`).
**Impact:** The request fails, but the unlinks are committed: the type silently loses categories. Any
service that writes before its last check has the same exposure; most inserts happen to check first.
Descendant wpmanager fixed this with an explicit rollback list
(`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java:17-22`).
**Recommendation:** Make domain exceptions unchecked (extend a common `DomainException extends
RuntimeException`), or declare `@Transactional(rollbackFor = Exception.class)` on the base service. Validate
before writing regardless.
**Verified against:** spring-tx-5.3.20.jar (javap, `DefaultTransactionAttribute.rollbackOn`)
**Confidence:** Confirmed (read in code)

### BT-R07-03
**Title:** Validation is mostly manual `null` checks
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** Declarative validation at the boundary.
**Evidence:** 15 constraint annotations across all 31 Forms (e.g. `@NotBlank` on `bsStatusForm.name`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsStatus/bsStatusForm.java:20-26`); every insert
repeats `if (form == null || form.getX() == null …) throw new InvalidInsertDeails(...)`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:68-70`).
All 31 Forms carry a class-level `@Validated` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskForm.java:10`),
which has no effect on a POJO. There is no handler for `MethodArgumentNotValidException`, so constraint
violations return Boot's default 400 without field errors.
**Impact:** Rules differ per service ([[Docs/BugTracker/Reviews/06-Service-Layer-Correctness-Review#BT-R06-13|BT-R06-13]]),
no lengths or formats are checked (e-mail, colour, dates), and clients never learn which field failed.
**Recommendation:** Put constraints on Forms (`@NotBlank`, `@Email`, `@Size`, `@Pattern`), use validation
groups for create/update, remove the manual checks, and return field errors in the error body.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R07-04
**Title:** Checked exceptions without a base leak into every generic signature
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** Interfaces should not leak implementation failure modes (interface cost of a deep module).
**Evidence:** `DefaultService` declares up to three checked exceptions per method
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultService.java:13-29`); controllers redeclare
them (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/controller/DefaultController.java:35-48`); the five
classes share no base (`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/BadOperator.java:3`).
**Impact:** Adding a failure mode changes every signature; lambdas cannot throw them (hence `orElseThrow`
gymnastics); a handler must list five types.
**Recommendation:** One unchecked `DomainException` hierarchy with a status/code per subtype.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-01|BE-R05-01]], [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-07|BE-R02-07]]

### BT-R07-05
**Title:** Custom error body instead of RFC 7807/9457 problem details
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** — (era gap)
**Evidence:** `ErrorResponseBody` with `error`, `message`, `path`, `status`, `timestamp` (`LocalDateTime`, no zone),
`trace` (`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/ErrorResponseBody.java:12-21`).
**Impact:** Reasonable in 2022 on Boot 2.7, which had no built-in `ProblemDetail`. Today it is a bespoke
format clients must learn, and its timestamp has no time zone.
**Recommendation:** On Boot 3, `ProblemDetail` via `ResponseEntityExceptionHandler` with `type`, `title`,
`status`, `detail`, `instance` and an `errors` extension for field errors; `Instant` timestamps.
**Verified against:** N/A — no library API involved at this version
**Confidence:** Confirmed (read in code)
**Version note:** `ProblemDetail` requires Spring Framework 6 / Boot 3. On Boot 2.7, keep a custom body but shape it like RFC 7807.
**Related:** [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06|BE-R05-06]]

### BT-R07-06
**Title:** Misspelled package and class names
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** —
**Evidence:** Package `exeptions` and class `InvalidInsertDeails`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/InvalidInsertDeails.java:1-3`).
**Impact:** Search and auto-import friction; the names spread into every service signature.
**Recommendation:** Rename (`exceptions`, `InvalidInsertDetails`) — `backend/` already did.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

```java
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
    public abstract HttpStatus status();
}
public final class NotFoundException extends DomainException { public HttpStatus status() { return NOT_FOUND; } /* ctor */ }

@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {   // Boot 3
    @ExceptionHandler(DomainException.class)
    ProblemDetail domain(DomainException e) { return ProblemDetail.forStatusAndDetail(e.status(), e.getMessage()); }
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) { /* log with id */ return ProblemDetail.forStatus(INTERNAL_SERVER_ERROR); }
}
```

- Unchecked domain exceptions → automatic rollback.
- Bean Validation on Forms; field errors in the response.
- One advice, including a fallback; security errors through the entry point and access-denied handler.

## Related Documents

- [[Docs/BugTracker/Explanations/12-Error-Handling]]
- [[Docs/BugTracker/Explanations/08-Persistence-and-Transactions]]
- [[Docs/backend/Reviews/05-Error-Handling-Review]]
