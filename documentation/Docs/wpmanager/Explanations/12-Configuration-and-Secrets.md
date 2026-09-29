# Configuration and Secrets — wpmanager

#doc #explanation #ref-wpmanager #configuration

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]

## Summary

Configuration is two properties files in `src/main/resources`: `application.properties` for the running
application and `application-test.properties` for the `test` profile. There are no YAML files, no
`@ConfigurationProperties` classes and no other profiles. Two application secrets are read from environment
variables through placeholders: the file-signature secret and the JWT signing secret. The file-signature
placeholder falls back to a value that `FileSigner` refuses at start-up (fail-fast); the JWT placeholder falls
back to a literal secret (fail-open). Database credentials are literals in the file. Storage-provider
credentials are not configuration at all: they are rows in the database, entered through `POST /s3`.

## Why It Is Built This Way

The properties file itself states the rule for secrets: "In production, this MUST be set via environment
variable" (`wpmanager/src/main/resources/application.properties:22-30`). The vault's "Secret Management" doc
expands it into a deployment and rotation procedure for `FILE_SIGNATURE_SECRET`
(`wpmanager/wpManagerDocs/Docs/Secret Management.md:13-60`), and the vault task "Remove Hardcoded Defaults"
aimed to make the JWT secret fail fast too
(`wpmanager/wpManagerDocs/Tasks/done/Optimize-Login-Architecture-step-3-Remove-Hardcoded-Defaults.md:13`).
That task removed the fallback constant from Java code, but a fallback remains in the properties file (see
below).

## How It Works

### `application.properties`

| Key | Purpose | Value |
|---|---|---|
| `spring.application.name` | name | `wpmanager` (`wpmanager/src/main/resources/application.properties:1`) |
| `spring.datasource.url` | MySQL URL | `localhost:3306/wp_manager` (`wpmanager/src/main/resources/application.properties:3`) |
| `spring.datasource.username`, `spring.datasource.password` | DB credentials | literals, `<redacted>` (`wpmanager/src/main/resources/application.properties:4-5`) |
| `spring.datasource.driver-class-name` | MySQL driver | (`wpmanager/src/main/resources/application.properties:6`) |
| `spring.servlet.multipart.*` | enabled, 100 MB file and request limits | (`wpmanager/src/main/resources/application.properties:9-11`) |
| `spring.jpa.hibernate.ddl-auto` | schema management | `update` (`wpmanager/src/main/resources/application.properties:14`) |
| `spring.jpa.properties.hibernate.dialect` | dialect | `MySQL8Dialect` (`wpmanager/src/main/resources/application.properties:16`) |
| `spring.jpa.open-in-view` | OSIV | `false` (`wpmanager/src/main/resources/application.properties:17`) |
| `spring.datasource.hikari.*` | pool 10, min idle 5, idle timeout 5 min | (`wpmanager/src/main/resources/application.properties:18-20`) |
| `file.signature.secret` | HMAC key for version signatures and client passwords | `${FILE_SIGNATURE_SECRET:…}` with a fallback, `<redacted>` (`wpmanager/src/main/resources/application.properties:25`) |
| `TK_KEY` | JWT signing key | `${JWT_SECRET_KEY:…}` with a literal fallback, `<redacted>` (`wpmanager/src/main/resources/application.properties:30`) |

A commented `logging.level.org.springframework.security=TRACE` line is marked "To Delete"
(`wpmanager/src/main/resources/application.properties:32-33`). `upload.max-file-size` is not set anywhere;
`UploadValidator` falls back to its annotation default of 100 MB
(`wpmanager/src/main/java/com/wpmanager/shared/tools/UploadValidator.java:11-12`).

### Environment variables and fallbacks

| Variable | Property | Fallback? | Effect when unset |
|---|---|---|---|
| `FILE_SIGNATURE_SECRET` | `file.signature.secret` | yes — the placeholder value that `FileSigner` rejects | start-up fails with `IllegalStateException` (fail-fast) |
| `JWT_SECRET_KEY` | `TK_KEY` | yes — a literal secret in the file | start-up succeeds and tokens are signed with the committed literal |

The property name `TK_KEY` is itself looked up by `@Value("${" + ApplicationConstants.TK_ENV_KEY + "}")`
(`wpmanager/src/main/java/com/wpmanager/configuration/filter/JwtTokenService.java:21-22`,
`wpmanager/src/main/java/com/wpmanager/constant/ApplicationConstants.java:4`), so an environment variable named
`TK_KEY` also overrides it through relaxed binding.

### `FileSigner` fail-fast

```java
// wpmanager/src/main/java/com/wpmanager/shared/tools/FileSigner.java:24-35
@PostConstruct
public void validateConfiguration() {
    if (secret == null || secret.trim().isEmpty() || secret.equals("changeme-in-production")) {
        logger.error("FILE_SIGNATURE_SECRET is not properly configured!");
        throw new IllegalStateException(
            "file.signature.secret must be set to a secure value in production"
        );
    }
    if (secret.length() < 32) {
        logger.warn("file.signature.secret should be at least 32 characters for optimal security");
    }
}
```

`JwtTokenService` has no equivalent check; `Keys.hmacShaKeyFor` only rejects keys shorter than 256 bits
(`wpmanager/src/main/java/com/wpmanager/configuration/filter/JwtTokenService.java:26-29`).

### Other secrets in the tree

- Seed admin credentials in code: `wpmanager/src/main/java/com/wpmanager/configuration/boostrap/AdminBoostrap.java:41-43`
  (`<redacted>`).
- Cloud storage access and secret keys for two buckets in a test:
  `wpmanager/src/test/java/com/wpmanager/models/plugin/E2EPluginUploadTest.java:61-62` and
  `wpmanager/src/test/java/com/wpmanager/models/plugin/E2EPluginUploadTest.java:75-76` (`<redacted>`).
- Storage-provider keys stored in plaintext columns
  (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java:39-46`).

### Test profile

`application-test.properties` sits in `src/main/resources`, so it ships in the application jar. It switches to
H2 in MySQL mode with `create-drop`, SQL logging, the same multipart limits, fixed literal values for both
secrets (`<redacted>`), and `spring.task.scheduling.enabled=false`
(`wpmanager/src/main/resources/application-test.properties:1-28`). `spring.task.scheduling.enabled` is not a
Spring Boot 3.4.1 property: the configuration metadata in `spring-boot-autoconfigure-3.4.1.jar` lists only
`spring.task.scheduling.pool.size`, `.shutdown.await-termination`, `.shutdown.await-termination-period`,
`.simple.concurrency-limit` and `.thread-name-prefix`, and `@EnableScheduling` on the application class
registers the jobs regardless. The file comment's intent ("to prevent FileDuplicator from running") is met
only because `FileDuplicator`'s first run is 83 minutes after start.

### Comparison with the vault's "Secret Management"

| Vault statement | Code today |
|---|---|
| `FILE_SIGNATURE_SECRET` is required in production and the app refuses to start without it (`wpmanager/wpManagerDocs/Docs/Secret Management.md:396-402`) | True: the fallback is the rejected placeholder |
| Secret rotation re-signs nothing; old signatures stay as they were (`wpmanager/wpManagerDocs/Docs/Secret Management.md:350-357`) | True, and rotation also changes every client's derived password, which the doc does not mention (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientService.java:106-116`) |
| JWT secret has "no fallback default" (`wpmanager/wpManagerDocs/Docs/Login and Security Architecture.md:134-141`) | False: `application.properties:30` has one |

## Key Components

| Component | Path | Responsibility |
|---|---|---|
| Runtime config | `wpmanager/src/main/resources/application.properties` | DB, JPA, multipart, secrets |
| Test config | `wpmanager/src/main/resources/application-test.properties` | H2, fixed secrets |
| Secret check | `wpmanager/src/main/java/com/wpmanager/shared/tools/FileSigner.java` | Fail-fast on the signature secret |
| Constants | `wpmanager/src/main/java/com/wpmanager/constant/ApplicationConstants.java` | Property name, header, issuer, subject, field limits |

## Conventions and Rules

- Secrets are `${ENV_VAR:fallback}` placeholders, documented with a "MUST be set via environment variable"
  comment.
- A component that needs a secret validates it in `@PostConstruct` (only `FileSigner` does).
- Test-only properties live in `application-test.properties` and are activated with `@ActiveProfiles("test")`.

## How to Replicate

1. Put non-secret defaults in `application.properties`.
2. Reference each secret as `${ENV_VAR}` **without** a fallback, so a missing variable stops start-up, or
   validate it in `@PostConstruct` as `FileSigner` does.
3. Put test settings in `src/test/resources/application-test.properties`.
4. Keep provider credentials out of source; if they must be stored, encrypt them and never return them.

## Known Limitations

- DB credentials, the JWT fallback and the seed admin are literals
  ([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-07|WP-R01-07]]); live cloud keys are committed in a test
  ([[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-06|WP-R01-06]]).
- Test profile ships in the jar ([[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-02|WP-R09-02]]);
  the scheduling switch does not exist ([[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-04|WP-R09-04]]);
  two upload size limits ([[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review#WP-R09-05|WP-R09-05]]).
- Vault drift on the JWT secret ([[Docs/wpmanager/Reviews/11-Code-Hygiene-and-Docs-Drift-Review#WP-R11-04|WP-R11-04]]).

## Related Documents

- [[Docs/wpmanager/Explanations/06-Authentication-and-Authorization]]
- [[Docs/wpmanager/Reviews/01-Security-Review]]
- [[Docs/wpmanager/Reviews/09-Build-Dependencies-and-Configuration-Review]]
- [[Docs/backend/Explanations/09-Configuration-and-Secrets]]
