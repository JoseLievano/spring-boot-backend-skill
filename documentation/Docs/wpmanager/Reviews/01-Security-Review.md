# Security Review — wpmanager

#doc #review #ref-wpmanager #security

**Project:** `wpmanager/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/wpmanager/wpmanager-Index]]
**Explained in:** [[Docs/wpmanager/Explanations/06-Authentication-and-Authorization]]

## Scope

Reviewed: `configuration/security/*`, `configuration/filter/*`, `configuration/boostrap/AdminBoostrap`,
`shared/securityUser/*`, `shared/tools/AuthUserUtil`, `shared/tools/URLValidator`, every `@PreAuthorize` and
`@EnableMethodSecurity` usage, the inherited-operation matrix of all controllers, the storage-provider
controllers, DTOs and mapper, both properties files, and the test sources for embedded credentials.
Excluded: dependency CVE scanning and runtime penetration testing (the application was not started, so
Spring Data REST exposure is not observed). Secret values are never reproduced here.

## Verdict

wpmanager is the only one of the three reference projects whose method security actually runs, and its
authentication pieces — BCrypt, JJWT 0.12 verification, stateless sessions, a JSON error contract, end-to-end
tests with real tokens — are sound. The authorization *model* is what fails. With no URL rules, anything not
behind an annotated service method is anonymous, including a debug controller with full read/write access to
the storage bucket. The base service grants every inherited operation to any logged-in user, so a client token
can list and delete admins and read every storage provider's secret key. Live cloud credentials are committed
in a test. These are exploitable today and dominate the verdict.

## Strengths to Keep

- Method security is genuinely active, and it is proven by tests that expect 401 and 403
  (`wpmanager/src/test/java/com/wpmanager/models/author/E2EAuthorTest.java:120-132`,
  `wpmanager/src/test/java/com/wpmanager/models/author/E2EAuthorTest.java:382-401`).
- JSON login through `AuthenticationManager` and BCrypt, not hand-written credential checks
  (`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityController.java:34-66`,
  `wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:42-45`).
- JJWT 0.12 `verifyWith` + `parseSignedClaims` rejects unsigned, tampered and expired tokens
  (`wpmanager/src/main/java/com/wpmanager/configuration/filter/JwtTokenService.java:46-52`).
- Security errors use the application's JSON error shape
  (`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:61-91`).
- Ownership checks through `AuthUserUtil` for client-owned writes
  (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:134-137`,
  `wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListService.java:127-130`).
- The file-signature secret fails fast when unset
  (`wpmanager/src/main/java/com/wpmanager/shared/tools/FileSigner.java:24-35`).
- Client-token minting is admin-only here, unlike in `backend/`
  (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientService.java:241-252`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-01\|WP-R01-01]] | No URL-level authorization; unannotated endpoints are anonymous | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-02\|WP-R01-02]] | `/tests3/*` lets anonymous callers upload, overwrite, delete and download bucket objects | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-03\|WP-R01-03]] | Inherited operations need only a login: clients can delete admins, providers and others' data | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-04\|WP-R01-04]] | Storage-provider secret keys are returned by `/s3` and stored in plaintext | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-05\|WP-R01-05]] | Spring Data REST likely exports every repository | 🔴 Critical | security | Needs runtime verification |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-06\|WP-R01-06]] | Live cloud-storage keys committed in a test file | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-07\|WP-R01-07]] | Hardcoded DB credentials, JWT fallback secret and seed admin password | 🟠 High | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-08\|WP-R01-08]] | JWT design: claims-only trust, no revocation, issuer not checked | 🟠 High | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-09\|WP-R01-09]] | Account-status flags are ignored | 🟠 High | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-10\|WP-R01-10]] | Client passwords are derived; clients authenticate only through admin-minted tokens | 🟡 Medium | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-11\|WP-R01-11]] | Method security is switched on from three service classes | 🟡 Medium | configuration | Confirmed (test/runtime evidence) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-12\|WP-R01-12]] | SSRF surface in website URL probing | 🟡 Medium | security | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-13\|WP-R01-13]] | CORS origin hardcoded; injected source ignored | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-14\|WP-R01-14]] | Duplicate `@EnableWebSecurity`; JWT filter also registered as a servlet filter | 🟢 Low | configuration | Confirmed (read in code) |
| [[Docs/wpmanager/Reviews/01-Security-Review#WP-R01-15\|WP-R01-15]] | `@PreAuthorize` on the non-bean `S3StorageClient` is inert | 🟢 Low | security | Confirmed (read in code) |

## Findings

### WP-R01-01
**Title:** No URL-level authorization; unannotated endpoints are anonymous
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Deny by default. Authorization exists only as a second layer (method security) with no first layer, so every endpoint that does not reach an annotated service method is open.
**Evidence:** The only `SecurityFilterChain` has no `authorizeHttpRequests`
(`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:47-58`). Endpoints with no
annotated service behind them: the `/tests3/*` controller
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:26-161`, see
WP-R01-02), `GET /test` (`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityController.java:68-71`),
and every Spring Data REST resource (WP-R01-05).
**Impact:** Any new controller, actuator endpoint or auto-configured resource is public unless its author
remembers an annotation on a service. This is the root cause behind WP-R01-02 and WP-R01-05.
**Recommendation:** Add a deny-by-default URL layer and move `@EnableMethodSecurity` to the security
configuration:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers("/admin/**", "/s3/**", "/plan/**", "/downloadable/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/plugin/upload", "/theme/upload").hasRole("ADMIN")
                .anyRequest().authenticated());
        // csrf, cors, session, exception handling, JWT filter as today
        return http.build();
    }
}
```
**Verified against:** Context7 `/spring-projects/spring-security/6.4.4` (`authorize-http-requests.adoc` and `method-security.adoc` at tag 6.4.4; closest indexed 6.4.x to the managed 6.4.2)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]] (same chain; in `backend/` method security is also off), [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01|BT-R01-01]]

### WP-R01-02
**Title:** `/tests3/*` lets anonymous callers upload, overwrite, delete and download bucket objects
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Least privilege; no debug surface in production code.
**Evidence:** `S3FileUploadController` is a `@RestController` at `/tests3` with no `@PreAuthorize`, calling the
S3 client directly rather than through an annotated service
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:26-28`). `POST /tests3/upload`
writes any file to the key `plugins` (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:52-76`),
`DELETE /tests3/delete?fileName=` deletes any key
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:78-104`),
`GET /tests3/download?fileName=` returns any object
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:114-139`), and `GET /tests3/exist`
probes providers by id (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:141-161`).
Object keys are predictable: `<pluginName>_<version>`
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:186`).
**Impact:** Anyone who can reach the server can download every premium plugin and theme ZIP (the product this
system sells), delete them from the first S3 provider, and use the bucket as free storage. No token is needed.
**Recommendation:** Delete the controller. If a storage diagnostics endpoint is needed, put it behind
`hasRole('ADMIN')` at URL level, route it through a service, and exclude it from production profiles.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R01-03
**Title:** Inherited operations need only a login: clients can delete admins, providers and others' data
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Least privilege and object-level authorization (OWASP API1/API5). The base class's default rule, `isAuthenticated()`, is too weak to be a default.
**Evidence:** Every base method is `@PreAuthorize("isAuthenticated()")`
(`wpmanager/src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java:34-72`). A CLIENT
token is accepted by these routes, from the override catalogue in
[[Docs/wpmanager/Explanations/04-Generic-CRUD-Framework]]:

| Route | What a client can do |
|---|---|
| `GET /admin`, `DELETE /admin/{id}` | list every admin (names, e-mails), delete any admin (`wpmanager/src/main/java/com/wpmanager/models/hq/admin/AdminServiceImpl.java:14-63` overrides only `insert` and `getOne`) |
| `GET /client`, `DELETE /client/{id}` | list every client, delete any client (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientService.java:26-253`) |
| `GET /s3`, `GET /s3/{id}`, `DELETE /s3/{id}` | read every provider including its keys (WP-R01-04), delete providers (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderService.java:15-64`) |
| `GET/POST/DELETE /downloadable…` | list all versions, create file-less version rows, delete version rows (`wpmanager/src/main/java/com/wpmanager/models/files/downloadable/DownloadableService.java:20-97` overrides nothing) |
| `GET /website`, `GET/DELETE /website/{id}` | read and delete any client's websites (`wpmanager/src/main/java/com/wpmanager/models/hq/website/WebsiteService.java:27-145`) |
| `GET /fav_list`, `GET/DELETE /fav_list/{id}` | read and delete any client's favorite lists (`wpmanager/src/main/java/com/wpmanager/models/hq/favoriteList/FavoriteListService.java:31-197`) |

**Impact:** Any client — and anyone holding any client token for 24 hours — can delete every admin account
(locking operators out until the seed runner recreates one on restart), delete other customers and their
data, and create version rows that break replication
([[Docs/wpmanager/Reviews/05-Storage-Integration-Review#WP-R05-02|WP-R05-02]]). Tests cover none of these routes
with a client token ([[Docs/wpmanager/Reviews/10-Testing-Review#WP-R10-01|WP-R10-01]]).
**Recommendation:** Make the base class deny by default and require each module to state its rules:
```java
public abstract class DefaultServiceImplements<…> {
    @Override
    @PreAuthorize("hasRole('ADMIN')")   // safe default; modules loosen it deliberately
    public DTO getOne(ID id) { … }
    // same for getAll, insert, update, delete
}
```
For client-owned resources, override `getOne`, `getAll` and `delete` with an ownership check (or a
`@PostAuthorize`/`@PostFilter` on the owner id), and add URL rules per WP-R01-01 as the outer layer.
**Verified against:** Context7 `/spring-projects/spring-security/6.4.4` (`method-security.adoc`)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01|BT-R01-01]], [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02|BT-R01-02]]

### WP-R01-04
**Title:** Storage-provider secret keys are returned by `/s3` and stored in plaintext
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Secrets are write-only: never echo credentials back through an API, and encrypt them at rest.
**Evidence:** Both `toDTO` and `toSmallDTO` copy `accessKey` and `secretKey` into the response
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderMapper.java:20-21`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderMapper.java:40-41`); both DTOs declare the fields
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderDTO.java:20-21`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderMiniDTO.java:19-20`). The columns are plain strings
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java:39-46`). `GET /s3` is inherited and
open to any authenticated user (WP-R01-03); even the admin-only `POST /s3` returns the keys in its response.
Lombok `@Data` also puts the secret key into `toString()` (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java:29-33`).
**Impact:** A client token yields full credentials for every storage bucket — read, overwrite and delete
access to the whole product catalog outside the application. A database dump or a log line with the entity
leaks them too.
**Recommendation:** Remove key fields from every response DTO (return a masked hint at most), make them
write-only in the form, and store them encrypted (for example a JPA `AttributeConverter` with a key from the
environment) or, better, reference credentials held in a secret manager. Exclude them from `toString()`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R01-05
**Title:** Spring Data REST likely exports every repository
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Explicit API surface; no auto-exposed persistence.
**Evidence:** `spring-boot-starter-data-rest` is on the classpath (`wpmanager/pom.xml:60-63`); no code
configures it and no repository is annotated `@RepositoryRestResource(exported = false)`. All repositories are
public interfaces, including `BaseUserRepository` (users with password hashes), `S3ProviderRepository` (keys)
and `IdempotencyKeyRepository` (`wpmanager/src/main/java/com/wpmanager/shared/securityUser/BaseUserRepository.java:9`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderRepository.java:6-8`,
`wpmanager/src/main/java/com/wpmanager/shared/idempotency/IdempotencyKeyRepository.java:12`). Data REST
endpoints do not pass through the annotated services, and there are no URL rules (WP-R01-01).
**Impact:** If exported as by default, anonymous callers can read password hashes and storage keys and create,
modify or delete any row, bypassing every `@PreAuthorize`.
**Recommendation:** Remove the starter (no code uses it). If it must stay, set
`spring.data.rest.detection-strategy=annotated` and cover its base path with URL rules.
**Verified against:** `spring-boot-autoconfigure-3.4.1.jar` configuration metadata (`spring.data.rest.detection-strategy` exists)
**Confidence:** Needs runtime verification — start the app with the default profile and run `curl -i http://localhost:8080/` and `curl -i http://localhost:8080/s3ProviderEntities`; a HAL index listing `baseUserEntities`, `s3ProviderEntities` and a 200 with key fields confirms it.
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-03|BE-R01-03]], [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-07|BT-R01-07]]

### WP-R01-06
**Title:** Live cloud-storage keys committed in a test file
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Secrets never live in source control.
**Evidence:** `E2EPluginUploadTest` builds two S3 provider entities with literal access and secret keys for a
Wasabi region (`wpmanager/src/test/java/com/wpmanager/models/plugin/E2EPluginUploadTest.java:61-62`,
`wpmanager/src/test/java/com/wpmanager/models/plugin/E2EPluginUploadTest.java:75-76`; values `<redacted>`). The file is
part of the `wpmanager` repository (a git submodule of this workspace).
**Impact:** Anyone with read access to the repository or its history can use the buckets directly. Removing
the lines does not remove them from history.
**Recommendation:** Treat the keys as compromised: **rotate them at the provider now** (manual action for the
user). Then remove them from the file, read test credentials from environment variables or use a local S3
emulator (MinIO, LocalStack, or Testcontainers), and purge the history (for example `git filter-repo`
with a replace-text rule, followed by a force-push and re-clone by all collaborators). Add a secret scanner
(gitleaks, GitHub secret scanning) to the workflow. Do not run the purge without coordinating with the
repository owner.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### WP-R01-07
**Title:** Hardcoded DB credentials, JWT fallback secret and seed admin password
**Severity:** 🟠 High
**Category:** security
**Principle:** Fail closed on missing secrets; no default credentials.
**Evidence:** Database username and password are literals
(`wpmanager/src/main/resources/application.properties:4-5`, `<redacted>`). The JWT key property falls back to a
literal when `JWT_SECRET_KEY` is unset (`wpmanager/src/main/resources/application.properties:30`, `<redacted>`),
and `JwtTokenService` does not validate it (`wpmanager/src/main/java/com/wpmanager/configuration/filter/JwtTokenService.java:21-29`).
`AdminBoostrap` creates an `ADMIN` with a fixed username and password when no admin exists
(`wpmanager/src/main/java/com/wpmanager/configuration/boostrap/AdminBoostrap.java:38-47`, `<redacted>`).
**Impact:** A deployment without `JWT_SECRET_KEY` signs tokens with a key anyone with the source knows, so
anyone can forge an admin token. Any fresh or wiped database gets an admin with a known password; after
WP-R01-03 lets a client delete all admins, a restart recreates the known one.
**Recommendation:** Use `TK_KEY=${JWT_SECRET_KEY}` with no fallback and validate it in `@PostConstruct` as
`FileSigner` does. Read DB credentials from the environment. Seed the first admin only from explicit
environment input (username + one-time password or an invite flow), never a constant.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-04|BE-R01-04]], [[Docs/backend/Reviews/01-Security-Review#BE-R01-11|BE-R01-11]], [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-03|BT-R01-03]]

### WP-R01-08
**Title:** JWT design: claims-only trust, no revocation, issuer not checked
**Severity:** 🟠 High
**Category:** security
**Principle:** Short-lived credentials with a revocation path; validate every claim you set.
**Evidence:** Tokens last 24 hours and carry the authorities
(`wpmanager/src/main/java/com/wpmanager/configuration/filter/JwtTokenService.java:34-43`). The filter rebuilds the
authentication from those claims without checking that the user still exists or still has the role
(`wpmanager/src/main/java/com/wpmanager/configuration/filter/JWTTokenValidatorFilter.java:45-72`). The parser does
not require the issuer, and the subject is the constant `UP_TK` rather than the user
(`wpmanager/src/main/java/com/wpmanager/configuration/filter/JwtTokenService.java:46-52`,
`wpmanager/src/main/java/com/wpmanager/constant/ApplicationConstants.java:6-7`). The removal of the per-request
lookup was a deliberate vault task (`wpmanager/wpManagerDocs/Tasks/done/Optimize-JWT-Validation-step-1-1-Remove-DB-Hits.md`).
**Impact:** A deleted or demoted admin keeps full rights for up to 24 hours; a stolen token cannot be revoked.
Client tokens are minted by admins and have the same lifetime.
**Recommendation:** Keep stateless validation but shorten access tokens (5–15 minutes) with refresh tokens,
put the user id in `sub`, require `iss` (`requireIssuer`) and audience, and keep a token version (or
`passwordChangedAt`) on the user that the filter compares, cached briefly. Consider Spring Security's resource
server support (`oauth2ResourceServer().jwt()`) instead of a custom filter.
**Verified against:** `jjwt-api-0.12.5.jar` (javap: `JwtParserBuilder.requireIssuer(String)`); Context7 `/jwtk/jjwt` (not version-pinned)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-07|BE-R01-07]], [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-10|BT-R01-10]]

### WP-R01-09
**Title:** Account-status flags are ignored
**Severity:** 🟠 High
**Category:** security
**Principle:** Implement the interface you claim to implement (LSP); a disabled account must not log in.
**Evidence:** `SecurityUser` defines `getAccountNonExpired()`, `getAccountNonLocked()`,
`getCredentialsNonExpired()` and `getEnabled()` (`wpmanager/src/main/java/com/wpmanager/shared/securityUser/SecurityUser.java:36-50`)
instead of overriding `isAccountNonExpired()` and friends, which `UserDetails` declares as `default` methods
returning `true`. The entity columns exist (`wpmanager/src/main/java/com/wpmanager/shared/models/baseUser/BaseUserEntity.java:58-68`).
**Impact:** Setting `enabled = false` or `accountNonLocked = false` has no effect on login.
**Recommendation:** Rename the four methods to the `is…` forms with `@Override`, and add a login test for a
disabled user.
**Verified against:** `spring-security-core-6.4.2.jar` (javap; `UserDetails.isEnabled()` and siblings are `default`, return `true`)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-08|BE-R01-08]], [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-11|BT-R01-11]]

### WP-R01-10
**Title:** Client passwords are derived; clients authenticate only through admin-minted tokens
**Severity:** 🟡 Medium
**Category:** security
**Principle:** Credentials must be secret, rotatable and owned by their subject.
**Evidence:** The password is `BCrypt(HMAC(email + wpID))` with the file-signature secret
(`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientService.java:106-116`), recomputed on e-mail/wpID change
(`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientService.java:232-236`). Clients get tokens only from
`GET /client/token/{username}` (ADMIN) (`wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientController.java:24-30`).
**Impact:** Anyone who holds the file-signature secret can compute every client's password; rotating that
secret silently changes every client password. Clients cannot rotate or revoke their own credential, and
token delivery to clients is out of band.
**Recommendation:** Give clients a real credential: an API key generated randomly, shown once, stored hashed,
and revocable (the unused `apiKey` column is a start, `wpmanager/src/main/java/com/wpmanager/models/hq/client/ClientEntity.java:31-32`),
or an OAuth2 client-credentials flow. Do not reuse the signing secret for credentials.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-06|BE-R01-06]], [[Docs/backend/Reviews/01-Security-Review#BE-R01-02|BE-R01-02]] (in `backend/` the minting endpoint is anonymous)

### WP-R01-11
**Title:** Method security is switched on from three service classes
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Cross-cutting infrastructure belongs in configuration, not in a business class; a hidden global switch is a shallow, surprising seam.
**Evidence:** `@EnableMethodSecurity` is on `PluginService`, `AuthorService` and `PlanService` only
(`wpmanager/src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:36`,
`wpmanager/src/main/java/com/wpmanager/models/downloads/author/AuthorService.java:29`,
`wpmanager/src/main/java/com/wpmanager/models/hq/plan/PlanService.java:20`). It works globally because a `@Service`
is a lite configuration candidate whose `@Import` is processed (see
[[Docs/wpmanager/Explanations/06-Authentication-and-Authorization]]); the E2E author tests confirm enforcement.
**Impact:** Deleting or refactoring those three services silently turns off every `@PreAuthorize` in the
application with no compile or test failure outside the author suite. This already happened in the descendant
project: `backend/` kept the rest of the security code and lost this switch.
**Recommendation:** Declare `@EnableMethodSecurity` once on `SecurityConfig`, remove it from services, and keep
a test that asserts a 403 for a wrong role on at least one endpoint per module.
**Verified against:** `spring-context-6.2.1.jar` (javap: `ConfigurationClassUtils.candidateIndicators` = `@Component`, `@ComponentScan`, `@Import`, `@ImportResource`); `spring-security-config-6.4.2.jar` (javap: `@EnableMethodSecurity` carries `@Import(MethodSecuritySelector.class)`); Context7 `/spring-projects/spring-security/6.4.4` (`method-security.adoc`: annotate a `@Configuration` class)
**Confidence:** Confirmed (test/runtime evidence)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]]

### WP-R01-12
**Title:** SSRF surface in website URL probing
**Severity:** 🟡 Medium
**Category:** security
**Principle:** Server-side requests to user-supplied hosts need an allow-list or network egress controls.
**Evidence:** A client supplies a domain; `URLValidator` sends `HEAD https://<domain>` and falls back to
`HEAD http://<domain>`, following redirects, with 5-second timeouts
(`wpmanager/src/main/java/com/wpmanager/shared/tools/URLValidator.java:23-51`,
`wpmanager/src/main/java/com/wpmanager/shared/tools/URLValidator.java:61-146`). The regex blocks IP literals but not
internal DNS names (any name with a letters-only TLD), and redirects are followed to any host on the same
scheme (`wpmanager/src/main/java/com/wpmanager/shared/tools/URLValidator.java:19-21`,
`wpmanager/src/main/java/com/wpmanager/shared/tools/URLValidator.java:69`).
**Impact:** A client can make the server probe internal hostnames or be redirected to internal addresses
(including cloud metadata endpoints), and learn from timing and from the validity result whether they exist.
Each call can hold a request thread for up to 20 seconds.
**Recommendation:** Resolve the host first and reject private, loopback, link-local and metadata ranges;
disable redirect following (or re-check each hop); run the probe asynchronously with a small pool; rate-limit
per client.
**Verified against:** N/A — JDK `HttpURLConnection` behavior, no Spring API involved
**Confidence:** Confirmed (read in code)

### WP-R01-13
**Title:** CORS origin hardcoded; injected source ignored
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Environment-specific values belong in configuration.
**Evidence:** The chain calls `corsConfigurationSource()` directly instead of using the injected parameter, and
the only origin is `http://localhost:3000`
(`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:48-51`,
`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:93-110`).
**Impact:** A production front end needs a code change and rebuild; `OPTIONS` is not in the allowed methods
list, which the comment itself flags as a preflight risk.
**Recommendation:** Bind allowed origins from a property (`app.cors.allowed-origins`), use the injected bean,
and let Spring handle `OPTIONS` preflight.
**Verified against:** N/A — no version-specific API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-10|BE-R01-10]]

### WP-R01-14
**Title:** Duplicate `@EnableWebSecurity`; JWT filter also registered as a servlet filter
**Severity:** 🟢 Low
**Category:** configuration
**Principle:** One place for each piece of wiring.
**Evidence:** `@EnableWebSecurity` on both `WpmanagerApplication` and `SecurityConfig`
(`wpmanager/src/main/java/com/wpmanager/WpmanagerApplication.java:10`,
`wpmanager/src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:30`). `JWTTokenValidatorFilter` is a
`@Component` `OncePerRequestFilter` (`wpmanager/src/main/java/com/wpmanager/configuration/filter/JWTTokenValidatorFilter.java:26-27`),
so Spring Boot also registers it in the servlet container chain.
**Impact:** Harmless today (`OncePerRequestFilter` runs once), but confusing and a trap if the filter gains
side effects.
**Recommendation:** Keep `@EnableWebSecurity` on `SecurityConfig` only; add a `FilterRegistrationBean` with
`setEnabled(false)` for the JWT filter, or do not make it a `@Component`.
**Verified against:** N/A — no version-specific API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-09|BE-R01-09]]

### WP-R01-15
**Title:** `@PreAuthorize` on the non-bean `S3StorageClient` is inert
**Severity:** 🟢 Low
**Category:** security
**Principle:** Annotations that look like controls but do nothing mislead readers.
**Evidence:** `S3StorageClient.uploadFile` carries `@PreAuthorize("hasRole('ADMIN')")`
(`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:63-65`), but every instance is created
with `new` (`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java:66-69`,
`wpmanager/src/main/java/com/wpmanager/models/storage/s3/S3FileUploadController.java:67`), so no proxy evaluates it.
The replication job calls it with no authentication at all.
**Impact:** A reader may believe storage writes are admin-guarded; `/tests3/upload` shows they are not.
**Recommendation:** Remove the annotation; enforce authorization at the service and URL layers.
**Verified against:** N/A — Spring AOP applies only to container-managed beans
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

1. `SecurityConfig` owns `@EnableWebSecurity` and `@EnableMethodSecurity`, and a chain whose last rule is
   `anyRequest().authenticated()` with explicit `permitAll` and `hasRole` matchers before it.
2. The generic base service defaults to `hasRole('ADMIN')`; modules loosen rules explicitly and add ownership
   checks for client-owned data on every read and delete.
3. No debug controllers; no Data REST unless explicitly exported.
4. Secrets: environment only, no fallbacks, validated at start-up; storage credentials encrypted, never
   returned, never in tests.
5. Tokens: short-lived, `sub` = user id, issuer required, a server-side token version for revocation;
   clients authenticate with their own revocable API keys.
6. An authorization test matrix: anonymous, CLIENT and ADMIN against every route.

## Related Documents

- [[Docs/wpmanager/Explanations/06-Authentication-and-Authorization]]
- [[Docs/wpmanager/Explanations/12-Configuration-and-Secrets]]
- [[Docs/wpmanager/Reviews/00-Review-Summary]]
- [[Docs/backend/Reviews/01-Security-Review]]
- [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review]]
