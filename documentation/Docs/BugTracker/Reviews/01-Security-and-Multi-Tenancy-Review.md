# Security and Multi-Tenancy Review — BugTracker

#doc #review #ref-bugtracker #security

**Project:** `BugTracker/` · **Stack:** Spring Boot 2.7.0, Java 17 (effective) · **Index:** [[Docs/BugTracker/BugTracker-Index]]
**Explained in:** [[Docs/BugTracker/Explanations/10-Authentication-and-Authorization]]

## Scope

Reviewed: `configuration/security/*`, `configuration/filter/*`, `shared/models/securityUser/*`,
`shared/models/user/*`, every `@PreAuthorize`, the role assignments in the six user services, the tenant
references in all tenant-side services, `exeptions/GlobalExceptionHandler`, `application.properties` and
`shared/tools/firstInstallCheck`. Excluded: dependency CVE scanning and runtime testing (the application was
not started; runtime-dependent claims are marked). Git history of BugTracker was not inspected: the
directory is a gitlink in this workspace.

## Verdict

Authentication works in the conventional Spring Security 5 way — BCrypt, a `UserDetailsService`, a signed,
expiring JWT. Everything after authentication is missing. Any logged-in user of any kind, including a
tenant's customer, can call every endpoint of the operator side and of every other tenant, can create an
admin, and can post as someone else. The signing key is a constant in source, so anyone who has read the
repository can mint tokens without logging in. Most findings are **defects** on Boot 2.7 as well, not era
gaps; only the annotation style of method security is an era gap.

## Strengths to Keep

- Passwords are BCrypt-encoded through the `PasswordEncoder` bean before persisting
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/security/SecurityConfig.java:58-61`,
  `BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/admin/AdminServiceImplements.java:48-49`).
- Credential checking goes through Spring Security's standard Basic filter and `UserDetailsService`, not a
  hand-written password comparison (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/security/SecurityConfig.java:39-43`).
- The validator reloads the user and roles from the database on every request instead of trusting the
  token's `authorities` claim, so a role removal takes effect immediately
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenValidatorFilter.java:58-62`).
- JJWT's `parserBuilder().setSigningKey(...).parseClaimsJws(...)` rejects unsigned and expired tokens
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenValidatorFilter.java:53-57`).
- Deny-by-default at the URL level: `anyRequest().authenticated()` means there is no anonymous endpoint
  other than the Basic challenge (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/security/SecurityConfig.java:41-43`).
- Services assign the role string themselves for the six specific user kinds
  (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsClient/bsClientServiceImplements.java:76-78`).

## Findings Summary

| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01\|BT-R01-01]] | Authorization is authentication only | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-02\|BT-R01-02]] | No tenant isolation | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-03\|BT-R01-03]] | JWT key, DB credentials and seed passwords are literals in source | 🔴 Critical | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-04\|BT-R01-04]] | `POST /user` accepts roles from the caller | 🟠 High | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-05\|BT-R01-05]] | Authors and actors come from the request body | 🟠 High | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-06\|BT-R01-06]] | Usernames unique only per user kind; login picks an arbitrary match | 🟠 High | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-07\|BT-R01-07]] | Spring Data REST likely exports every repository | 🟠 High | security | Needs runtime verification |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-08\|BT-R01-08]] | Token validator fails with 500 instead of 401 | 🟡 Medium | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-09\|BT-R01-09]] | Token generator dereferences before its null check; filters registered twice | 🟡 Medium | security | Needs runtime verification |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-10\|BT-R01-10]] | Ten-day tokens, no revocation, constant subject | 🟡 Medium | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-11\|BT-R01-11]] | Account-status flags are ignored | 🟡 Medium | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-12\|BT-R01-12]] | Unknown username raises `NoSuchElementException` | 🟢 Low | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-13\|BT-R01-13]] | `ErrorResponseBody.trace` returns exception text | 🟢 Low | security | Confirmed (read in code) |
| [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-14\|BT-R01-14]] | Pre-6.x security configuration style | 🟢 Low | security | Confirmed (read in code) |

## Findings

### BT-R01-01
**Title:** Authorization is authentication only
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Least privilege / deny by default — the role model exists (role strings, `@EnableGlobalMethodSecurity`) but only one of 31 services uses it; the security interface is shallow.
**Evidence:** The only URL rule is `anyRequest().authenticated()`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/security/SecurityConfig.java:41-43`). The only
`@PreAuthorize` annotations in the code are on four methods of `BusinessServiceImplements`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessServiceImplements.java:45-106`);
even there `update` has none (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/business/BusinessServiceImplements.java:96-99`).
Operator endpoints such as `POST /admin`, `DELETE /client/{id}`, `POST /plan` and `POST /invoice` have no rule
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/admin/AdminController.java:9`).
**Impact:** Any authenticated principal — including a tenant's `ROLE_BS_CLIENT` customer — can create HQ
admins, delete HQ clients (which cascades to their businesses, see
[[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-11|BT-R03-11]]), change plans and
issue invoices. This is a defect on Boot 2.7, not an era gap.
**Recommendation:** Define a role matrix and enforce it at the URL level as the baseline
(`requestMatchers("/admin/**", "/plan/**", "/invoice/**", "/mainHQ/**", "/employee/**", "/client/**").hasRole("ADMIN")`
and tenant routes for `BS_*` roles), plus `@PreAuthorize` for finer rules. Start from deny-all and open
routes explicitly.
**Verified against:** spring-security-config-5.7.1.jar (javap) — `authorizeHttpRequests` and `EnableGlobalMethodSecurity` present; 5.7.1 has only `requestMatchers(RequestMatcher...)`; the `String...` form used above needs 5.8+/6.x, so on 5.7 use `mvcMatchers`/`antMatchers`.
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]] — same outcome, different mechanism: backend has no URL rules and no method security at all; BugTracker authenticates every URL but never authorizes.

### BT-R01-02
**Title:** No tenant isolation
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Tenant isolation as a cross-cutting invariant — enforced in one deep module, never left to each caller.
**Evidence:** The principal holds only a username and authorities
(`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenValidatorFilter.java:60-64`);
nothing maps it to a Business. Services take the Business from the request body and trust it —
`businessRepository.findById(form.getBusiness())`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrTask/bsPrTaskServiceImplements.java:76`).
Reads load any id (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:33-39`)
and page queries apply only the client's own filter
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/service/DefaultServiceImplements.java:111-131`).
**Impact:** A user of tenant A can read, create, update and delete tenant B's projects, tasks, users,
documents and settings by id, and list them with `POST /{module}/page` without a business filter. For a
multi-tenant SaaS this is the highest-impact defect in the project.
**Recommendation:** Put the tenant in the principal (a `businessId` claim or a lookup from the user's
`business` association), resolve it in one `TenantContext`, and apply it centrally: add
`business.id = :tenant` to every predicate in the base service, set `business` from the context on insert
(ignore the Form's), and check ownership in `getOne`/`update`/`delete`. Hibernate filters or a
`@PreAuthorize("@tenantGuard.owns(#id)")` are alternatives.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R01-03
**Title:** JWT key, DB credentials and seed passwords are literals in source
**Severity:** 🔴 Critical
**Category:** security
**Principle:** Secrets out of code; configuration injected per environment.
**Evidence:** `SecurityConstant.JWT_KEY` is a string constant (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/security/SecurityConstant.java:5`, value `<redacted>`),
used for signing and verification (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenGeneratorFilter.java:53`).
The datasource URL, user and password are literal (`BugTracker/src/main/resources/application.properties:2-4`, values `<redacted>`).
The seeder creates `admin` and a client with literal passwords
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/tools/firstInstallCheck.java:132`,
`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/tools/firstInstallCheck.java:146`, values `<redacted>`).
**Impact:** Anyone with read access to the repository can sign a token with `username = "admin"` and act as
the admin in every deployment that uses this build — no login needed. The same person knows the seed
admin password and the database credentials.
**Recommendation:** Read the key from configuration (`${JWT_SECRET}`), fail start-up if it is missing or
shorter than 256 bits, and rotate it. Move datasource credentials to environment variables. Take the seed
admin password from configuration or generate one and print it once; force a change on first login.
Treat the committed values as compromised.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-11|BE-R01-11]], [[Docs/backend/Reviews/01-Security-Review#BE-R01-04|BE-R01-04]] — backend reads its key from configuration; BugTracker hardcodes it, which is worse.

### BT-R01-04
**Title:** `POST /user` accepts roles from the caller
**Severity:** 🟠 High
**Category:** security
**Principle:** Mass assignment — the Form exposes a field the caller must not control.
**Evidence:** `UserForm` has `Set<String> roles` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/UserForm.java:32`),
`UserMapper.toEntity` copies it (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/UserMapper.java:45-52`)
and `UserServiceImplements.insert` even requires it to be non-empty
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/UserServiceImplements.java:35-37`). `/user` is
reachable by any authenticated user (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/UserController.java:8-10`).
**Impact:** `POST /user {"username":"x","password":"…","email":"…","roles":["ROLE_ADMIN"]}` creates an
admin-role user. Today this adds little on top of [[Docs/BugTracker/Reviews/01-Security-and-Multi-Tenancy-Review#BT-R01-01|BT-R01-01]],
but it becomes the escalation path the moment real authorization is added.
**Recommendation:** Remove `roles` from `UserForm`; assign roles only in services, and only through an
admin-only endpoint. Consider removing the generic `/user` insert, since each user kind has its own module.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R01-05
**Title:** Authors and actors come from the request body
**Severity:** 🟠 High
**Category:** security
**Principle:** Identity from the security context, never from input.
**Evidence:** Comment insert loads the author from `form.getAuthor()`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrComment/bsPrCommentServiceImplements.java:79-81`);
channel insert does the same (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrChannel/bsPrChannelServiceImplements.java:57`);
`POST /bs_pr_mention` takes both `author` and `mentionedUser`
(`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/project/bsPrMention/bsPrMentionServiceImplements.java:55-59`).
**Impact:** Any user can post comments and open channels in another user's name and fabricate mentions —
impersonation in the collaboration features, with no audit trail to show it.
**Recommendation:** Drop `author` from the Forms and set it from `SecurityContextHolder` (via a small
`CurrentUser` service). Mentions should only be generated server-side from comment text.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R01-06
**Title:** Usernames unique only per user kind; login picks an arbitrary match
**Severity:** 🟠 High
**Category:** security
**Principle:** One identity invariant, enforced once (and in the database).
**Evidence:** Each user service checks username/e-mail only against its own subclass repository, e.g.
`bsManagerRepository.findByUsername` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsManager/bsManagerServiceImplements.java:49-54`),
`adminRepository.findByUsername` (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/HQ/admin/AdminServiceImplements.java:40-46`).
The `username` column has no unique constraint (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:50-51`).
Login and token validation return `userRepository.findByUsername(username).iterator().next()` from a
`Set<User>` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/securityUser/SecurityUserServiceImplements.java:23-32`).
**Impact:** A tenant manager can be created with the username `admin`. Afterwards both users share the
name: which one Basic login checks the password against, and which one's roles the validator loads for a
token carrying `username = "admin"`, depends on `HashSet` iteration order — a token issued to the manager can
be resolved to the admin's authorities on a later request.
**Recommendation:** Enforce global uniqueness on `User.username` (and e-mail) with a unique constraint and a
check against `UserRepository` in one place; make `findByUsername` return `Optional<User>`; put an immutable
user id (not the username) in the token's `sub`.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R01-07
**Title:** Spring Data REST likely exports every repository
**Severity:** 🟠 High
**Category:** security
**Principle:** Secure by default — no endpoint should exist that the code does not declare.
**Evidence:** `spring-boot-starter-data-rest` is on the classpath (`BugTracker/pom.xml:33-36`) with no
configuration; every repository is a public interface
(`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/UserRepository.java:10-11`). The default
detection strategy in spring-data-rest-core 3.7.0 is `DEFAULT` (exports public repositories), checked with
`javap` on `RepositoryRestConfiguration`.
**Impact:** If exported, authenticated users get `GET /users` with every `User` field (including the
BCrypt `password` hash, which no DTO exposes) and `PATCH /users/{id}` that can change `roles`, bypassing all
service logic. Paths are plural (`/users`, `/bsPrTaskEntities`), so they do not collide with the
hand-written ones.
**Recommendation:** Remove the starter. If it must stay, set
`spring.data.rest.detection-strategy=annotated` and annotate nothing.
Runtime check: authenticate and call `GET /users` and `GET /profile`; a `200` HAL response confirms.
**Verified against:** spring-data-rest-core-3.7.0.jar (javap)
**Confidence:** Needs runtime verification
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-03|BE-R01-03]] — same root cause; rated Critical there because backend's endpoints are anonymous.

### BT-R01-08
**Title:** Token validator fails with 500 instead of 401
**Severity:** 🟡 Medium
**Category:** security
**Principle:** Fail closed, and fail with the right status — authentication errors belong to the authentication entry point.
**Evidence:** `request.getHeader(...)` may be `null`, and `initJwt.isEmpty()` is called without a null check
(`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenValidatorFilter.java:40-46`).
A Basic header on any path other than `/login` is cut after 7 characters and parsed as a JWT. Every failure
becomes `throw new BadCredentialsException(...)` inside the filter
(`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenValidatorFilter.java:66-68`),
which runs before Spring Security's `ExceptionTranslationFilter`.
**Impact:** Every unauthenticated request (no header) raises a `NullPointerException`; every bad token
raises an exception out of the filter chain. Clients see 500 instead of 401, so the front end cannot tell
"log in again" from "server broken", and logs fill with stack traces. The request is still refused, so this
is not an access bypass.
**Recommendation:** If the header is missing or does not start with `Bearer `, call `chain.doFilter` and
return; on parse failure, clear the context and let the entry point answer 401 (or write 401 directly).
On Boot 3 prefer `oauth2ResourceServer().jwt()` with a `JwtDecoder`, which does all of this.
**Verified against:** jjwt-api-0.11.2 (`parserBuilder`), spring-security-config-5.7.1.jar (javap)
**Confidence:** Confirmed (read in code)
**Version note:** The resource-server alternative exists on Boot 2.7 too (`spring-boot-starter-oauth2-resource-server`) with `NimbusJwtDecoder.withSecretKey`.

### BT-R01-09
**Title:** Token generator dereferences before its null check; filters registered twice
**Severity:** 🟡 Medium
**Category:** security
**Principle:** Single registration of cross-cutting components; guard before use.
**Evidence:** `authentication.getName()` is called at line 44 and `if (null != authentication)` is checked at
line 52 (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenGeneratorFilter.java:42-61`).
`GET /login` without credentials reaches the generator with no authentication (the Basic filter passes
requests without a Basic header through). Both filters are `@Service` beans
(`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenValidatorFilter.java:25-26`) **and**
separately `new`-ed into the chain (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/security/SecurityConfig.java:39-40`);
Boot 2.7 registers `Filter` beans with the servlet container (`ServletContextInitializerBeans`, javap).
**Impact:** `GET /login` without a Basic header gives a 500. The bean instances run a second time outside
Spring Security on every request; their `OncePerRequestFilter` marker attribute differs from the chain
instances' (filter name vs class name), so both execute: tokens are parsed twice, and the validator's
NPE can fire again after the chain has already accepted a request.
**Recommendation:** Remove `@Service` from the filters (or add a `FilterRegistrationBean` with
`setEnabled(false)`), inject them into `SecurityConfig` as plain objects, and move the null check first.
Runtime check: send an authenticated request with a debugger or log in each filter and count invocations.
**Verified against:** spring-boot-2.7.0.jar (javap, `ServletContextInitializerBeans.addAdaptableBeans`)
**Confidence:** Needs runtime verification
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-09|BE-R01-09]] — the same double registration was inherited.

### BT-R01-10
**Title:** Ten-day tokens, no revocation, constant subject
**Severity:** 🟡 Medium
**Category:** security
**Principle:** Short-lived credentials; the token's subject identifies the principal.
**Evidence:** `setExpiration(now + 900000000)` (about 10.4 days), `setSubject("JWT Token")`, user in a custom
`username` claim (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenGeneratorFilter.java:54-58`);
no refresh endpoint, deny-list or token version on `User` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:19-66`);
issuer not checked on parse (`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/filter/JWTTokenValidatorFilter.java:53-57`).
**Impact:** A stolen token works for ten days and cannot be revoked (the only lever is deleting the user or
renaming them). Because the principal is identified by a mutable username, renaming a user orphans tokens
and a new user with the old name inherits them.
**Recommendation:** 15–60 minute access tokens with a refresh flow; `sub` = user id; `requireIssuer` on
parse; a `tokenVersion` column checked by the validator for revocation.
**Verified against:** jjwt-api-0.11.2 (`JwtParserBuilder.requireIssuer`)
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-07|BE-R01-07]]

### BT-R01-11
**Title:** Account-status flags are ignored
**Severity:** 🟡 Medium
**Category:** security
**Principle:** An adapter must not silently discard the state it adapts.
**Evidence:** `SecurityUser.isAccountNonExpired/isAccountNonLocked/isCredentialsNonExpired/isEnabled` all return
`true` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/securityUser/SecurityUser.java:38-56`)
although `User` has the four columns (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/user/User.java:56-66`).
In Spring Security 5.7 these methods are abstract on `UserDetails`, so the class had to implement them and
chose constants. Separately, users built through `User.builder()` or the subclass builders get `false` in all
four columns, because Lombok's builder ignores the field initializers — see
[[Docs/BugTracker/Reviews/03-Domain-Model-and-Persistence-Review#BT-R03-05|BT-R03-05]]; only `bsClient`
sets them explicitly (`BugTracker/src/main/java/com/bgsystem/bugtracker/models/client/bsClient/bsClientServiceImplements.java:69-74`).
**Impact:** There is no way to disable or lock an account. The stored flags are also wrong for most users,
so simply wiring `SecurityUser` to the columns would lock almost everyone out.
**Recommendation:** Fix the stored values first (`@Builder.Default` or explicit setting, plus a data
migration), then delegate: `isEnabled() { return user.isEnabled(); }` etc.
**Verified against:** spring-security-core-5.7.1.jar (javap) — the four methods are abstract in 5.7
**Confidence:** Confirmed (read in code)
**Related:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-08|BE-R01-08]] — same symptom, different mechanism: in backend (Security 6.4) the methods are `default` and a mis-named getter is never called; in BugTracker they are implemented as constants.

### BT-R01-12
**Title:** Unknown username raises `NoSuchElementException`
**Severity:** 🟢 Low
**Category:** security
**Principle:** Honour the framework contract (`UsernameNotFoundException`).
**Evidence:** `findByUsername` returns an empty `Set`, never `null`; the code checks `user == null` and then
calls `iterator().next()` (`BugTracker/src/main/java/com/bgsystem/bugtracker/shared/models/securityUser/SecurityUserServiceImplements.java:23-30`).
**Impact:** Basic login with an unknown name still ends in 401 (the provider wraps the exception as an
`InternalAuthenticationServiceException`), but it is logged as an internal error, and it skips the provider's
constant-time "user not found" path.
**Recommendation:** `userRepository.findByUsername(u).stream().findFirst().map(SecurityUser::new).orElseThrow(() -> new UsernameNotFoundException(u))`.
**Verified against:** spring-security-core-5.7.1.jar (javap) — `UserDetailsService.loadUserByUsername` declares `UsernameNotFoundException`
**Confidence:** Confirmed (read in code)

### BT-R01-13
**Title:** `ErrorResponseBody.trace` returns exception text
**Severity:** 🟢 Low
**Category:** security
**Principle:** Do not leak internals in error bodies.
**Evidence:** `.trace(e.getLocalizedMessage())` in the only handler
(`BugTracker/src/main/java/com/bgsystem/bugtracker/exeptions/GlobalExceptionHandler.java:23`).
**Impact:** Today it returns Spring's "Access is denied" text — harmless. The field invites copying the
pattern to other handlers, where messages can contain SQL or entity details.
**Recommendation:** Drop `trace`; log the exception with a correlation id and return the id.
**Verified against:** N/A — no library API involved
**Confidence:** Confirmed (read in code)

### BT-R01-14
**Title:** Pre-6.x security configuration style
**Severity:** 🟢 Low
**Category:** security
**Principle:** — (era gap)
**Evidence:** `@EnableGlobalMethodSecurity(prePostEnabled = true)` and `.and()`-chained DSL
(`BugTracker/src/main/java/com/bgsystem/bugtracker/configuration/security/SecurityConfig.java:25-43`).
**Impact:** Correct on 5.7 (`EnableGlobalMethodSecurity` is not deprecated in 5.7.1). It does not compile
or is deprecated on Security 6, so copying it into a new project creates immediate migration work.
**Recommendation:** Use `@EnableMethodSecurity` (available since 5.6) and the lambda DSL
(`http.sessionManagement(s -> s.sessionCreationPolicy(STATELESS))`).
**Verified against:** spring-security-config-5.7.1.jar (javap) — `EnableMethodSecurity` present, `EnableGlobalMethodSecurity` not deprecated
**Confidence:** Confirmed (read in code)
**Version note:** Era gap. The lambda DSL and `@EnableMethodSecurity` work on 5.7 already; on Boot 3 / Security 6 they are the only non-deprecated options.

## Recommended Target Pattern

- Deny by default, with a URL-level role matrix plus `@PreAuthorize` for fine rules.
- A `TenantContext` resolved from the principal and applied by the base service to every read, write and
  predicate; tenant-owned Forms never carry `business`.
- Identity (`author`, `createdBy`) always from the security context.
- Globally unique usernames enforced by the database; `sub` = user id.
- Secrets from environment/config, validated at start-up; no seed passwords in code.
- Short-lived access tokens, refresh flow, `tokenVersion` revocation; on Boot 3 the OAuth2 resource server
  support for token validation.
- Filters constructed once and registered only in the security chain; missing/invalid tokens → 401.

## Related Documents

- [[Docs/BugTracker/Explanations/10-Authentication-and-Authorization]]
- [[Docs/BugTracker/Reviews/00-Review-Summary]]
- [[Docs/BugTracker/Reviews/05-Filter-Engine-Review]] — the filter engine can be used to probe password hashes ([[Docs/BugTracker/Reviews/05-Filter-Engine-Review#BT-R05-07|BT-R05-07]])
- [[Docs/backend/Reviews/01-Security-Review]]
