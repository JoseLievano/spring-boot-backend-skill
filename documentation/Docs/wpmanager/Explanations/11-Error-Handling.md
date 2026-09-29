# Error Handling — wpmanager

#doc #explanation #ref-wpmanager #error-handling

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]

## Summary

Services signal business failures with four checked exceptions (`ItemNotFoundException`, `ItemAlreadyExist`,
`InvalidInsertDetails`, `InvalidDeleteOperation`) plus the upload-specific checked `FailUpload` and runtime
`DuplicateUploadException`. One `@ControllerAdvice`, `GlobalExceptionHandler`, maps nine exception types to a
status and a five-field JSON body, `ErrorHTTPRes`. The security entry point, access-denied handler and JWT
filter write the same body by hand. Anything else — validation, malformed JSON, oversized uploads,
constraint violations — falls through to Spring Boot's default error handling, and the `/tests3` debug
controller throws `ResponseStatusException`.

## Why It Is Built This Way

One error shape for application and security errors was an explicit goal of the vault's security work: the
entry point and denied handler "return well-structured JSON error responses (`ErrorHTTPRes`) instead of
standard Spring HTML error pages" (`wpmanager/wpManagerDocs/Reports/LoginArchitectureReview.md:28-32`). The
checked exceptions make the failure modes part of every generic signature
(`wpmanager/src/main/java/com/wpmanager/shared/defaultInterfaces/DefaultService.java:10-22`).

## How It Works

### Exception → status

| Exception | Kind | Status | `error` text | Source |
|---|---|---|---|---|
| `InvalidInsertDetails` | checked | 400 | Invalid Details | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:18-31` |
| `ItemAlreadyExist` | checked | 409 | Conflict | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:33-46` |
| `ItemNotFoundException` | checked | 404 | Not Found | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:48-61` |
| `InvalidDeleteOperation` | checked | 400 | Bad Request | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:63-76` |
| `BadCredentialsException` | Spring Security | 401 | Unauthorized | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:78-90` |
| `DuplicateUploadException` | runtime | 409 | Conflict | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:92-105` |
| `IllegalStateException` | runtime | 500 | Internal Server Error | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:107-120` |
| `IllegalArgumentException` | runtime | 400 | Bad Request | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:122-135` |
| `FailUpload` | checked | 500 | Internal Server Error | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java:137-150` |
| missing/invalid token on a protected method | security | 401 | Unauthorized, "Invalid Credentials" | `wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:61-75` |
| authenticated but not allowed | security | 403 | Forbidden, "Access Denied" | `wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:77-91` |
| unparsable or expired JWT | filter | 401 | Unauthorized, "Invalid Token" | `wpmanager/src/main/java/com/wpmanager/configuration/filter/JWTTokenValidatorFilter.java:74-89` |

Every handler copies `ex.getMessage()` into the body, including the 500 handlers. Uploads that go through
the idempotency wrapper reach the handler as `IllegalStateException` or `DuplicateUploadException`, whatever
the original failure was ([[Docs/wpmanager/Explanations/08-Upload-Idempotency]]).

### The body

```java
// wpmanager/src/main/java/com/wpmanager/shared/tools/ErrorHTTPRes.java:7-20
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorHTTPRes {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

}
```

Example (a duplicate author name):

```json
{
  "timestamp": "2026-09-29T10:15:30.123",
  "status": 409,
  "error": "Conflict",
  "message": "Author with that name already exists",
  "path": "/author"
}
```

The timestamp is `LocalDateTime.now().toString()`, with no zone. Handlers take the path from
`WebRequest.getDescription(false)` with `uri=` stripped; the security handlers use `request.getRequestURI()`.

### Security handlers and the filter

`SecurityConfig` holds one `ObjectMapper` field for its two handlers
(`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:34`). The JWT filter creates a
new `ObjectMapper` for every rejected token
(`wpmanager/src/main/java/com/wpmanager/configuration/filter/JWTTokenValidatorFilter.java:86-87`). Neither uses
the Boot-configured mapper.

### What is not handled

No handler exists for `MethodArgumentNotValidException`, `HttpMessageNotReadableException`,
`MissingServletRequestParameterException`, `MaxUploadSizeExceededException`,
`DataIntegrityViolationException`, `ConstraintViolationException` or `Exception`. These get Spring Boot's
default `/error` response, with a different JSON shape. Bean Validation constraints exist mostly on entities
(`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorEntity.java:27-35`), so they fire at flush
time as `ConstraintViolationException`; the only form-level constraint is `@NotBlank` on `AdminForm.username`
(`wpmanager/src/main/java/com/wpmanager/models/hq/admin/AdminForm.java:25`).

### `ResponseStatusException` in `/tests3`

The debug controller reports errors with `ResponseStatusException` (400/500), which Spring renders in the
default error format, not `ErrorHTTPRes`
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:52-76`).

### Validation helpers

- `TextFieldValidator` trims, enforces 3–240 characters, optionally alphanumerics, and optionally rejects SQL
  metacharacters and keywords (`wpmanager/src/main/java/com/wpmanager/shared/tools/TextFieldValidator.java:20-128`);
  `AuthorService` uses it with the SQL check on for name and website
  (`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorService.java:63-79`).
- `ValidationResult` carries valid/invalid, message and trimmed value
  (`wpmanager/src/main/java/com/wpmanager/shared/tools/ValidationResult.java:9-33`).
- `UploadValidator` for files ([[Docs/wpmanager/Explanations/07-Upload-Pipeline]]).

## Key Components

| Component | Path | Responsibility |
|---|---|---|
| Advice | `wpmanager/src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java` | Exception → status + body |
| Body | `wpmanager/src/main/java/com/wpmanager/shared/tools/ErrorHTTPRes.java` | Five-field error DTO |
| Domain exceptions | `wpmanager/src/main/java/com/wpmanager/exceptions` | Checked failures + upload exceptions |
| Security handlers | `wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java` | 401/403 JSON |
| Text validator | `wpmanager/src/main/java/com/wpmanager/shared/tools/TextFieldValidator.java` | Manual string validation |

## Conventions and Rules

- Throw the matching checked exception with a human-readable message; the advice maps it.
- Log at `ERROR` before throwing (every service does this).
- Security code writes `ErrorHTTPRes` itself because it runs outside the MVC advice.
- Validate by hand in the service; `@Valid` on the base controller is present but most forms have no
  constraints.

## How to Replicate

1. Create the checked exceptions and `ErrorHTTPRes`.
2. Create `GlobalExceptionHandler` with one method per exception, as in the table.
3. Build the same body in the security entry point, denied handler and JWT filter.
4. Add handlers for framework exceptions and a fallback, as the review recommends.

## Known Limitations

- Missing framework and fallback handlers
  ([[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-01|WP-R08-01]]); internal messages
  on 500 ([[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-02|WP-R08-02]]).
- Forms without constraints and the SQL keyword blacklist
  ([[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-03|WP-R08-03]],
  [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-04|WP-R08-04]]).
- Not RFC 9457 ([[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-06|WP-R08-06]]); three
  ways to write an error ([[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review#WP-R08-07|WP-R08-07]]).
- Upload failures collapse into 500 ([[Docs/wpmanager/Reviews/04-Idempotency-Review#WP-R04-01|WP-R04-01]]).

## Related Documents

- [[Docs/wpmanager/Reviews/08-Error-Handling-and-Validation-Review]]
- [[Docs/wpmanager/Explanations/06-Authentication-and-Authorization]]
- [[Docs/backend/Explanations/08-Error-Handling]]
