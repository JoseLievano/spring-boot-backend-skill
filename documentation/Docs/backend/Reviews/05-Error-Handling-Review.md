# Error Handling Review — backend

#doc #review #ref-backend #error-handling

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/08-Error-Handling]]

## Scope

Reviewed: `exceptions/*`, `shared/tools/ErrorHTTPRes`, the error branches in `SecurityConfig` and
`JWTTokenValidatorFilter`, `rollbackFor` declarations, and `GlobalExceptionHandlerQueryTest`. Excluded:
which exception each service throws for which condition (covered in
[[Docs/backend/Reviews/04-Feature-Module-Correctness-Review]]).

## Verdict

The project has the right instinct — one handler, one JSON shape for application and security errors —
and the query-related mappings are precise and tested. The mechanism around it is costly: five unrelated
checked exceptions that each need a handler, a signature entry and a `rollbackFor` entry; four copies of
the response-building code; no catch-all, so anything unexpected leaves the contract; and internal
messages returned to clients on 500. Spring Boot 3 offers a standard (RFC 9457 `ProblemDetail`) that
would replace most of this code.

## Strengths to Keep

- A single `@ControllerAdvice` as the only place that decides status codes
  (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:16-17`).
- Security errors (401/403 and invalid tokens) use the same body as application errors
  (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:61-91`,
  `backend/src/main/java/com/agentForgeBackend/configuration/filter/JWTTokenValidatorFilter.java:74-89`).
- Validation errors are flattened into readable `field: message` text
  (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:36-38`).
- Malformed JSON gets a fixed, non-leaking message
  (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:53-57`).
- Handlers are unit-tested without a Spring context
  (`backend/src/test/java/com/agentForgeBackend/exceptions/GlobalExceptionHandlerQueryTest.java:23-67`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-01\|BE-R05-01]] | Five checked exceptions with no common base | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-02\|BE-R05-02]] | Response-building code duplicated four ways | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-03\|BE-R05-03]] | No fallback handler; unexpected errors leave the contract | 🟡 Medium | error-handling | Confirmed (read in code) |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-04\|BE-R05-04]] | Internal messages returned on 500; any `IllegalArgumentException` becomes 400 | 🟡 Medium | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-05\|BE-R05-05]] | Inconsistent handler signatures and a zone-less timestamp | 🟢 Low | hygiene | Confirmed (read in code) |
| [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06\|BE-R05-06]] | Custom body instead of RFC 9457 `ProblemDetail` | 🟡 Medium | api-design | Confirmed (read in code) |

## Findings

### BE-R05-01
**Title:** Five checked exceptions with no common base
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** OCP — adding a failure type edits the interface, the base classes, every override, the `rollbackFor` lists and the handler.
**Evidence:** Each exception extends `Exception` directly (`backend/src/main/java/com/agentForgeBackend/exceptions/ItemNotFoundException.java:3`, `backend/src/main/java/com/agentForgeBackend/exceptions/ItemAlreadyExist.java:3`, `backend/src/main/java/com/agentForgeBackend/exceptions/InvalidInsertDetails.java:3`, `backend/src/main/java/com/agentForgeBackend/exceptions/InvalidDeleteOperation.java:3`, `backend/src/main/java/com/agentForgeBackend/exceptions/InvalidQueryRequestException.java:3`); each needs its own handler (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:19-120`) and a `rollbackFor` entry (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:24-30`). `InvalidDeleteOperation` is declared and handled but never thrown.
**Impact:** High ceremony per error type; a forgotten `rollbackFor` entry commits a transaction that threw.
**Recommendation:** One unchecked base, e.g. `abstract class ApiException extends RuntimeException { abstract HttpStatus status(); }` with `NotFoundException`, `ConflictException`, `BadRequestException`; one handler method for the base type. Remove all `rollbackFor` lists.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R05-02
**Title:** Response-building code duplicated four ways
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** DRY — the helper exists but is bypassed.
**Evidence:** `buildErrorResponse` (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:166-179`) is used by three handlers; seven build the body inline (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:66-72`, and the six handlers after it); `SecurityConfig` builds it twice with its own `new ObjectMapper()` (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:34`, `backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:66-73`); the JWT filter builds it again and creates a new `ObjectMapper` per failed request (`backend/src/main/java/com/agentForgeBackend/configuration/filter/JWTTokenValidatorFilter.java:79-87`).
**Impact:** Format changes need four edits; privately constructed `ObjectMapper`s ignore Boot's Jackson configuration (date format, naming strategy).
**Recommendation:** One `ErrorResponseWriter` (or the `ProblemDetail` approach in [[Docs/backend/Reviews/05-Error-Handling-Review#BE-R05-06|BE-R05-06]]) used by the advice, the entry point, the denied handler and the filter, with the Boot-configured `ObjectMapper` injected.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R05-03
**Title:** No fallback handler; unexpected errors leave the contract
**Severity:** 🟡 Medium
**Category:** error-handling
**Principle:** Total error mapping — every exception gets the documented shape.
**Evidence:** The advice handles ten specific types and nothing else (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:19-164`) and does not extend `ResponseEntityExceptionHandler`. Reachable unhandled cases include the NPEs in [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-02|BE-R04-02]], `DataIntegrityViolationException` on a unique-constraint race, and Spring MVC errors such as `GET /admin/abc` (type mismatch) or `PATCH /admin/1` (405).
**Impact:** Those responses use Spring Boot's default error body (different fields), so clients need two parsers, and unexpected exceptions are logged only by the servlet container, without application context such as a correlation id.
**Recommendation:** Extend `ResponseEntityExceptionHandler` to cover Spring MVC exceptions, and add `@ExceptionHandler(Exception.class)` that logs the stack trace with a correlation id and returns a generic 500 body.
**Verified against:** `spring-webmvc-6.2.1.jar` (contains `ResponseEntityExceptionHandler`); `/spring-projects/spring-boot/v3.4.1` (Context7, verified during task creation: a `@ControllerAdvice` extending `ResponseEntityExceptionHandler` is the supported customization point)
**Confidence:** Confirmed (read in code)

### BE-R05-04
**Title:** Internal messages returned on 500; any `IllegalArgumentException` becomes 400
**Severity:** 🟡 Medium
**Category:** security
**Principle:** Do not leak internals (OWASP A05 / CWE-209); map by *meaning*, not by JDK type.
**Evidence:** `IllegalStateException` → 500 with `ex.getMessage()` (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:136-149`); such messages include profile internals, e.g. "Invalid query profile default sort: …" (`backend/src/main/java/com/agentForgeBackend/shared/query/PageableFactory.java:66-67`). `IllegalArgumentException` → 400 with its message (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:151-164`), although it is thrown for programming errors such as a misconfigured `QueryableField` (`backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:83-97`).
**Impact:** Server-side bugs are reported to clients as their fault (400) or with internal detail (500); library exceptions of these JDK types (from Spring, Hibernate, JJWT) are exposed verbatim.
**Recommendation:** Return a generic message for 500 and log the detail; throw a domain exception for client errors instead of relying on JDK types; remove the blanket `IllegalArgumentException` handler.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R05-05
**Title:** Inconsistent handler signatures and a zone-less timestamp
**Severity:** 🟢 Low
**Category:** hygiene
**Principle:** Consistency.
**Evidence:** Handlers return `ResponseEntity<ErrorHTTPRes>` or `ResponseEntity<Object>` interchangeably (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:20`, `backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:78`); the path comes from string-replacing `uri=` in `WebRequest.getDescription` (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:177`); `timestamp` is `LocalDateTime.now().toString()` without offset (`backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java:173`).
**Impact:** Minor: timestamps are ambiguous across time zones; `Object` return types lose type safety.
**Recommendation:** Type every handler to the error body; use `Instant` (serialized as ISO-8601 UTC); take the path from `HttpServletRequest.getRequestURI()`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R05-06
**Title:** Custom body instead of RFC 9457 `ProblemDetail`
**Severity:** 🟡 Medium
**Category:** api-design
**Principle:** Prefer the platform standard over a home-grown equivalent.
**Evidence:** `ErrorHTTPRes {timestamp, status, error, message, path}` (`backend/src/main/java/com/agentForgeBackend/shared/tools/ErrorHTTPRes.java:10-18`); `spring.mvc.problemdetails.enabled` is not set (`backend/src/main/resources/application.properties:1-36`).
**Impact:** Clients and tools cannot rely on the standard `application/problem+json` fields (`type`, `title`, `status`, `detail`, `instance`); the project maintains its own format, writers and tests.
**Recommendation:** Set `spring.mvc.problemdetails.enabled=true`, extend `ResponseEntityExceptionHandler`, and have domain exceptions produce `ProblemDetail` (or extend `ErrorResponseException`). Add custom properties (e.g. `errors` for field violations) with `problemDetail.setProperty(...)`.
**Verified against:** `/spring-projects/spring-boot/v3.4.1` (Context7, verified during task creation: `spring.mvc.problemdetails.enabled=true` + `@ControllerAdvice extends ResponseEntityExceptionHandler`); `spring-web-6.2.1.jar` (contains `ProblemDetail`, `ErrorResponseException`); `spring-boot-autoconfigure-3.4.1.jar` metadata defines `spring.mvc.problemdetails.enabled`
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

```java
public abstract class ApiException extends RuntimeException {        // unchecked: default rollback
    protected ApiException(String message) { super(message); }
    public abstract HttpStatus status();
}
public final class NotFoundException extends ApiException { … HttpStatus.NOT_FOUND … }
public final class ConflictException extends ApiException { … HttpStatus.CONFLICT … }

@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {   // covers Spring MVC errors
    @ExceptionHandler(ApiException.class)
    ProblemDetail handle(ApiException ex) {
        return ProblemDetail.forStatusAndDetail(ex.status(), ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        log.error("Unhandled", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }
}
```

The security entry point, access-denied handler and JWT filter write the same `ProblemDetail` through
one injected writer that uses Boot's `ObjectMapper`.

## Related Documents

- [[Docs/backend/Explanations/08-Error-Handling]]
- [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review]]
- [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review]]
