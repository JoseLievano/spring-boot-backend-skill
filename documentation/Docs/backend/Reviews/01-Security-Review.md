# Security Review — backend

#doc #review #ref-backend #security

**Project:** `backend/` · **Stack:** Spring Boot 3.4.1, Java 21 · **Index:** [[Docs/backend/backend-Index]]
**Explained in:** [[Docs/backend/Explanations/07-Authentication-and-Authorization]]

## Scope

Reviewed: `configuration/security/*`, `configuration/filter/*`, `configuration/boostrap/AdminBoostrap`,
`shared/securityUser/*`, `shared/tools/FileSigner`, `shared/tools/AuthUserUtil`, the `@PreAuthorize`
usages in services, `ClientController`/`ClientService` token and password code, both properties files,
`backend/.nvm`, and the `auth-server/` properties in this workspace's git history. Excluded: dependency
CVE scanning and runtime penetration testing (the application was not started).

## Verdict

Authentication is conventional and mostly sound in its parts — BCrypt, an HMAC-signed JWT with
expiry, stateless sessions, a JSON error contract. Authorization, however, does not exist at runtime:
the filter chain has no URL rules and method security is never enabled, so every `@PreAuthorize`
annotation is decoration and every endpoint, including the one that mints client tokens, is anonymous.
On top of that, the account-status flags are silently ignored, one secret serves two cryptographic
purposes, and a seed admin with a hardcoded password is created on first start. The security *design
intent* is reasonable; the security *posture* of the running application is open.

## Strengths to Keep

- Passwords are hashed with BCrypt through the `PasswordEncoder` bean
  (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:42-45`,
  `backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java:49-50`).
- Login goes through the standard `AuthenticationManager`/`UserDetailsService` pipeline rather than
  hand-written credential checks
  (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityController.java:37-43`).
- JWT parsing uses JJWT 0.12's `verifyWith(SecretKey)` + `parseSignedClaims`, which rejects unsigned and
  expired tokens (`backend/src/main/java/com/agentForgeBackend/configuration/filter/JwtTokenService.java:46-52`).
- Secrets have no defaults in the production config and are validated at start-up
  (`backend/src/main/resources/application.properties:26`,
  `backend/src/main/java/com/agentForgeBackend/shared/tools/FileSigner.java:24-35`).
- Security errors use the same JSON shape as application errors
  (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:61-91`).
- Stateless sessions with CSRF disabled is consistent for a header-token API
  (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:50-53`).
- The list engine's field whitelist prevents filtering on `password`
  (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminQueryProfile.java:18-29`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-01\|BE-R01-01]] | No authorization is enforced anywhere | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-02\|BE-R01-02]] | Anyone can mint a client JWT | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-03\|BE-R01-03]] | Spring Data REST likely exports user repositories | 🔴 Critical | security | Needs runtime verification |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-04\|BE-R01-04]] | Seed admin with hardcoded password | 🟠 High | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-05\|BE-R01-05]] | One secret for JWT signing and HMAC password derivation | 🟠 High | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-06\|BE-R01-06]] | Client passwords are derived from e-mail; rotation is dead code | 🟠 High | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-07\|BE-R01-07]] | JWT design: claims-only trust, no issuer check, no revocation | 🟠 High | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-08\|BE-R01-08]] | Account-status flags are ignored | 🟠 High | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-09\|BE-R01-09]] | JWT filter also registered as a servlet filter | 🟢 Low | security | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-10\|BE-R01-10]] | CORS origin hardcoded; injected source ignored | 🟡 Medium | configuration | Confirmed (read in code) |
| [[Docs/backend/Reviews/01-Security-Review#BE-R01-11\|BE-R01-11]] | Literal secrets in the working tree and in git history | 🟠 High | security | Confirmed (read in code) |

## Findings

### BE-R01-01
**Title:** No authorization is enforced anywhere
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Secure by default / deny by default; the authorization *interface* (`@PreAuthorize`) exists but its implementation is never wired — a shallow facade over nothing.
**Evidence:** The only `SecurityFilterChain` has no `authorizeHttpRequests(...)`
(`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:47-59`), and no class
carries `@EnableMethodSecurity` (`backend/src/main/java/com/agentForgeBackend/agentForgeBackendApplication.java:8-11`,
`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:29-31`). Annotations such as
`@PreAuthorize("hasRole('ADMIN')")` (`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminServiceImpl.java:34`)
and `@PreAuthorize("isAuthenticated()")` (`backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/DefaultServiceImplements.java:54`)
are therefore never evaluated. `ClientService.getOne` has no annotation at all
(`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:48-49`).
**Impact:** Any anonymous caller can list, read, create, update and delete admins and clients (`/admin/**`, `/client/**`), including creating a new admin through `POST /admin`. The JWT is only checked when a caller chooses to send one.
**Recommendation:** Deny by default at the URL level and enable method security as a second layer:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());
        // csrf, cors, session, filter as today
        return http.build();
    }
}
```
Add negative tests (anonymous → 401, wrong role → 403) so the rules cannot silently disappear again (see [[Docs/backend/Reviews/08-Testing-Review#BE-R08-01|BE-R08-01]]).
**Verified against:** `/spring-projects/spring-security/6.4.4` (Context7; snippet sources carry the `blob/6.4.4/` tag: `authorize-http-requests.adoc`, `method-security.adoc`). Project resolves 6.4.2; same minor line.
**Confidence:** Confirmed (read in code)

### BE-R01-02
**Title:** Anyone can mint a client JWT
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Least privilege; token issuance must be the most protected operation.
**Evidence:** `GET /client/token/{username}` calls `generateTokenForClient`
(`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientController.java:23-29`), which is only
"protected" by an inert `@PreAuthorize("hasRole('ADMIN')")` and returns a signed 24-hour token carrying the
client's roles (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:161-172`).
**Impact:** Knowing (or guessing) a client username is enough to obtain a valid token for that client. Combined with [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]] the token is not even needed today, but the moment authorization is added this endpoint becomes a full impersonation primitive unless it is fixed too.
**Recommendation:** Restrict the endpoint to admins at the URL level (`/client/token/**` → `hasRole('ADMIN')`), return `201` with an opaque, revocable API key (stored hashed, e.g. in the unused `apikey` column) instead of a user JWT, and audit-log every issuance.
**Verified against:** N/A — no library API involved (URL rule syntax as in [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]])
**Confidence:** Confirmed (read in code)

### BE-R01-03
**Title:** Spring Data REST likely exports user repositories, including password hashes and write operations
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Minimize attack surface; no auto-exposed persistence endpoints.
**Evidence:** `spring-boot-starter-data-rest` is on the classpath (`backend/pom.xml:66-69`). The repositories are
public interfaces without `@RepositoryRestResource(exported = false)`
(`backend/src/main/java/com/agentForgeBackend/models/hq/admin/AdminRepository.java:6-7`,
`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientRepository.java:8-9`,
`backend/src/main/java/com/agentForgeBackend/shared/securityUser/BaseUserRepository.java:8`), and
`BaseUserEntity` exposes a `password` getter via Lombok
(`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:15-16`,
`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:49-50`). No
`spring.data.rest.*` property narrows detection (`backend/src/main/resources/application.properties:1-36`).
**Impact:** With the default detection strategy, Spring Data REST publishes a HAL resource per public repository (e.g. `/adminEntities`, `/clientEntities`, `/baseUserEntities`), supporting `GET` (returning entities with the BCrypt `password`) and `POST`/`PUT`/`PATCH`/`DELETE` that bypass all service rules. Combined with [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]] this is anonymous. Serializing `ClientEntity` may also hit [[Docs/backend/Reviews/04-Feature-Module-Correctness-Review#BE-R04-01|BE-R04-01]].
**Recommendation:** Remove `spring-boot-starter-data-rest` (nothing uses it). If it must stay, set `spring.data.rest.detection-strategy=annotated` and never serialize entities. Also mark `password` `@JsonIgnore` as defense in depth. **To confirm:** start the app with the `test` profile and run `curl -i http://localhost:8080/adminEntities` — expected `200` with `_embedded.adminEntities[*].password` present; `curl -i http://localhost:8080/profile` lists the exported resources.
**Verified against:** `spring-data-rest-core-4.4.1.jar` (javap: `RepositoryDetectionStrategies` = `ALL`, `DEFAULT`, `VISIBILITY`, `ANNOTATED`); `spring-boot-autoconfigure-3.4.1.jar` (contains `RepositoryRestMvcAutoConfiguration`; metadata defines `spring.data.rest.detection-strategy`); `/spring-projects/spring-data-rest` (Context7, not version-pinned: "By default, all public Spring Data repositories expose HTTP resources").
**Confidence:** Needs runtime verification

### BE-R01-04
**Title:** Seed admin with hardcoded password
**Severity:** 🟠 High
**Category:** security
**Principle:** No default credentials (OWASP A07).
**Evidence:** On every start with an empty `admin` table, `AdminBoostrap` saves username `admin` with a fixed e-mail and a hardcoded trivial password (`<redacted>`) (`backend/src/main/java/com/agentForgeBackend/configuration/boostrap/AdminBoostrap.java:37-45`).
**Impact:** Every fresh deployment starts with a known admin credential. Nothing forces a change. Because `POST /login` is public, the account is usable immediately.
**Recommendation:** Read the initial admin's username and password (or a one-time setup token) from environment variables with no default, fail start-up when missing, and route creation through `AdminServiceImpl.insert` so the same validation applies. Consider a `mustChangePassword` flag.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R01-05
**Title:** One secret for JWT signing and HMAC password derivation
**Severity:** 🟠 High
**Category:** security
**Principle:** Key separation — one key, one purpose.
**Evidence:** Both `file.signature.secret` and `TK_KEY` resolve to `${JWT_SECRET}`
(`backend/src/main/resources/application.properties:26`, `backend/src/main/resources/application.properties:30`),
consumed by `FileSigner` (`backend/src/main/java/com/agentForgeBackend/shared/tools/FileSigner.java:21-22`) and
`JwtTokenService` (`backend/src/main/java/com/agentForgeBackend/configuration/filter/JwtTokenService.java:21-28`).
**Impact:** Leaking or rotating the JWT key also changes every client's derived password ([[Docs/backend/Reviews/01-Security-Review#BE-R01-06|BE-R01-06]]); both are HMAC-SHA256 with the same key, so `FileSigner.sign(x)` and a JWT signature are computed under one key and one algorithm family.
**Recommendation:** Use two independent variables (e.g. `JWT_SIGNING_KEY`, `CLIENT_KEY_PEPPER`), bound through a validated `@ConfigurationProperties` record so both are required and length-checked at start-up.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R01-06
**Title:** Client passwords are derived from e-mail; rotation is dead code
**Severity:** 🟠 High
**Category:** security
**Principle:** Credentials must be random and independently rotatable; a credential derived from public data plus a global secret is a single point of failure.
**Evidence:** `password = BCrypt(Base64(HMAC-SHA256(secret, email)))`
(`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:93`,
`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:102`). On update the e-mail is
copied first (`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:130`), then compared
with itself, so the re-derivation branch never runs
(`backend/src/main/java/com/agentForgeBackend/models/hq/client/ClientService.java:153-156`).
**Impact:** Anyone holding `JWT_SECRET` can compute every client's login password. A client's password cannot be changed without changing the global secret, and after an e-mail change the stored hash still corresponds to the *old* e-mail, so the "password" becomes undiscoverable by the intended derivation.
**Recommendation:** Treat clients as API-key principals: generate a random key (e.g. 32 bytes from `SecureRandom`), show it once, store only its hash, support rotation and revocation per client. If clients need interactive login, give them a real password flow.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BE-R01-07
**Title:** JWT design: claims-only trust, constant subject, no issuer check, no revocation
**Severity:** 🟠 High
**Category:** security
**Principle:** Validate every claim you rely on; keep authorization data fresh.
**Evidence:** Every token has the same `sub` and a constant issuer
(`backend/src/main/java/com/agentForgeBackend/configuration/filter/JwtTokenService.java:35-36`); expiry is a
hardcoded 24 h (`backend/src/main/java/com/agentForgeBackend/configuration/filter/JwtTokenService.java:41`); the
parser does not call `requireIssuer` (`backend/src/main/java/com/agentForgeBackend/configuration/filter/JwtTokenService.java:46-52`);
the filter builds the principal and authorities from claims only, without a user lookup, and accepts a token with
or without the `Bearer ` prefix (`backend/src/main/java/com/agentForgeBackend/configuration/filter/JWTTokenValidatorFilter.java:39-72`).
**Impact:** A deleted, disabled or demoted user keeps full access for up to 24 hours; there is no logout, refresh or revocation. The subject cannot identify the user, so standard tooling (`JwtAuthenticationConverter`, audit logs) cannot use it.
**Recommendation:** Put the user id in `sub`, require `iss` (and ideally `aud`) when parsing (`Jwts.parser().verifyWith(key).requireIssuer(ISSUER).build()`), make the lifetime configurable and short (15–60 min) with a refresh token, and either re-load the user on each request or keep a token-version column checked by the filter. Alternatively replace the custom filter with Spring Security's resource server (`oauth2ResourceServer(o -> o.jwt(...))` with `NimbusJwtDecoder.withSecretKey(key)`).
**Verified against:** `jjwt-api-0.12.5.jar` (javap: `JwtParserBuilder.requireIssuer(String)`, `requireSubject(String)`, `clockSkewSeconds(long)`, `verifyWith(SecretKey)`); `/spring-projects/spring-security/6.4.4` (Context7, `blob/6.4.4/…/oauth2/resource-server/jwt.adoc`: `NimbusJwtDecoder.withSecretKey`).
**Confidence:** Confirmed (read in code)

### BE-R01-08
**Title:** Account-status flags are ignored
**Severity:** 🟠 High
**Category:** security
**Principle:** Liskov substitution — `SecurityUser` claims to be a `UserDetails` but does not honour its contract (the flag methods keep their permissive defaults).
**Evidence:** `SecurityUser` defines `getAccountNonExpired()`, `getAccountNonLocked()`, `getCredentialsNonExpired()`, `getEnabled()` (`backend/src/main/java/com/agentForgeBackend/shared/securityUser/SecurityUser.java:36-50`) instead of overriding the `is…()` methods of `UserDetails`, which in 6.4.2 are `default` methods returning `true`.
**Impact:** Setting `enabled = false` or `accountNonLocked = false` on a user (`backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java:58-68`) has no effect: the user can still log in. The code compiles cleanly, so the bug is invisible.
**Recommendation:** Override the interface methods with `@Override` so the compiler checks the contract:
```java
@Override public boolean isEnabled()             { return baseUser.isEnabled(); }
@Override public boolean isAccountNonLocked()    { return baseUser.isAccountNonLocked(); }
@Override public boolean isAccountNonExpired()   { return baseUser.isAccountNonExpired(); }
@Override public boolean isCredentialsNonExpired() { return baseUser.isCredentialsNonExpired(); }
```
Also check `enabled` in the JWT filter path (see [[Docs/backend/Reviews/01-Security-Review#BE-R01-07|BE-R01-07]]).
**Verified against:** `spring-security-core-6.4.2.jar` (javap: `UserDetails.isAccountNonExpired/isAccountNonLocked/isCredentialsNonExpired/isEnabled` are `public default boolean`)
**Confidence:** Confirmed (read in code)

### BE-R01-09
**Title:** JWT filter is also registered as a servlet filter
**Severity:** 🟢 Low
**Category:** security
**Principle:** One owner per responsibility — the filter should be registered by the security chain only.
**Evidence:** `JWTTokenValidatorFilter` is a `@Component` (`backend/src/main/java/com/agentForgeBackend/configuration/filter/JWTTokenValidatorFilter.java:26-27`) and is also added with `addFilterBefore` (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:57`). Spring Boot registers every `Filter` bean with the servlet container.
**Impact:** The filter sits in two chains. `OncePerRequestFilter` skips the second invocation, so behavior is correct today, but ordering becomes surprising (the container copy runs outside Spring Security) and any change to the "already filtered" logic would double-process requests.
**Recommendation:** Either create the filter with `new` inside `SecurityConfig` (not a bean), or keep the bean and disable container registration with a `FilterRegistrationBean<JWTTokenValidatorFilter>` whose `setEnabled(false)`.
**Verified against:** `/spring-projects/spring-security/6.4.4` (Context7, `blob/6.4.4/docs/modules/ROOT/pages/servlet/architecture.adoc`: "Disable Container Registration … `registration.setEnabled(false)`").
**Confidence:** Confirmed (read in code)

### BE-R01-10
**Title:** CORS origin hardcoded; injected source ignored
**Severity:** 🟡 Medium
**Category:** configuration
**Principle:** Configuration over code; do not ignore injected collaborators.
**Evidence:** The chain takes a `CorsConfigurationSource` parameter but calls the `@Bean` method directly (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:48-51`); the only allowed origin is `http://localhost:3000` (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:97`) and `PATCH` is not allowed (`backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java:99`).
**Impact:** Every deployment to a real frontend origin needs a code change; credentials are allowed for the hardcoded origin.
**Recommendation:** Bind allowed origins/methods from configuration (`app.cors.allowed-origins`) and use the injected source: `.cors(c -> c.configurationSource(corsConfigurationSource))`.
**Verified against:** N/A — no library API involved (standard `CorsConfiguration` setters already used by the project)
**Confidence:** Confirmed (read in code)

### BE-R01-11
**Title:** Literal secrets in the working tree and in git history
**Severity:** 🟠 High
**Category:** security
**Principle:** Secrets never in source control.
**Evidence:** `backend/.nvm` holds two literal secret values (`<redacted>`, `backend/.nvm:1-2`). `application-test.properties` holds literal test keys (`<redacted>`, `backend/src/main/resources/application-test.properties:22`, `backend/src/main/resources/application-test.properties:25`) and ships in the main jar ([[Docs/backend/Reviews/07-Build-Dependencies-and-Configuration-Review#BE-R07-06|BE-R07-06]]). This workspace's git history still contains the predecessor's configuration with a literal MySQL password and literal secret defaults: commit `cc7d2121aaa740c1c82414074788aa79ddf4a8ab`, file `auth-server/src/main/resources/application.properties` lines 5, 25 and 30 (values `<redacted>`), also present in commit `e0993c4`.
**Impact:** Anyone with repository access can read these values. If any of them was ever used outside development, it must be treated as compromised.
**Recommendation:** Rotate the MySQL password and any secret that was used beyond local development; add `.nvm`/`.env` to `.gitignore` in the source repository; keep test secrets in `src/test/resources`; consider history rewriting only if the repository is shared. Add a secret scanner (e.g. gitleaks) to CI.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

## Recommended Target Pattern

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity                                   // @PreAuthorize becomes real
class SecurityConfig {
    @Bean SecurityFilterChain api(HttpSecurity http, JwtAuthFilter jwt, CorsConfigurationSource cors) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(c -> c.configurationSource(cors))      // origins from @ConfigurationProperties
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.POST, "/login").permitAll()
                .anyRequest().authenticated())           // deny by default
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

Plus: `SecurityUser` overrides the four `is…()` flags; tokens carry `sub = userId`, are short-lived,
issuer-checked and revocable via a token-version column; separate keys per purpose from validated
configuration; no seed credentials in code; clients authenticate with random, hashed, revocable API keys;
negative authorization tests for every protected route.

## Related Documents

- [[Docs/backend/Explanations/07-Authentication-and-Authorization]]
- [[Docs/backend/Explanations/09-Configuration-and-Secrets]]
- [[Docs/backend/Reviews/08-Testing-Review]]
- [[Docs/backend/Reviews/00-Review-Summary]]
