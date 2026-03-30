# Auth Server Integration Guide

This guide explains how to adapt and extend the **Auth Server** codebase when integrating it into a new project or building new features on top of it.

## 1. Environment Setup

Before the application will boot successfully, you must configure its mandatory secrets. The application is built with a "fail-fast" strategy: it refuses to start if cryptographic keys are weak or missing.

Set the following environment variables (or add them to your `application.properties` during local development):

- `TK_KEY`: Used for HMAC SHA-256 JWT signing. **Must be randomly generated and secure.**
- `FILE_SIGNATURE_SECRET` (mapped to `file.signature.secret`): Used by the `FileSigner` utility if your project handles secure file tracking.

*Example generation command (Linux/Mac):*
```bash
openssl rand -base64 32
```

## 2. Extending the User Domain

The application uses JPA inheritance (`InheritanceType.JOINED`) for user management. To add a new type of user (e.g., `ManagerEntity`, `CustomerEntity`):

1. **Create the Entity:**
   Create a new class extending `BaseUserEntity`.
   ```java
   @Entity
   @Table(name = "manager")
   public class ManagerEntity extends BaseUserEntity {
       @Column(name = "department")
       private String department;
       
       // constructors, getters, setters
   }
   ```
2. **Add Roles:**
   Ensure the newly created users are assigned the appropriate enum values from `UserRoles` (e.g., `ROLE_MANAGER`).
3. **Database Schema:**
   The `JOINED` strategy means Hibernate/Flyway will automatically create a `manager` table with a foreign key referencing the `base_user` table's primary key.

## 3. Retrieving the Authenticated User

Because the `JWTTokenValidatorFilter` uses a stateless architecture and relies *only* on the JWT claims to establish the Spring Security context, the `SecurityContext` principal is a simple `String` representing the `username`.

If your business logic requires accessing the full user entity (e.g., to access `ManagerEntity` specific fields):

1. Retrieve the username from context:
   ```java
   String username = AuthUserUtil.getAuthUsername();
   ```
2. Query the database using the username:
   ```java
   BaseUserEntity user = securityUserService.getBaseUser(username);
   ```

*Note: Avoid looking up the user entity for simple authorization checks. Standard `@PreAuthorize("hasRole('ADMIN')")` annotations work natively via the JWT claims without database queries.*

## 4. Adapting CORS for Production

The `SecurityConfig` is hardcoded for a local development frontend (`http://localhost:3000`). When deploying to staging or production, update the `corsConfigurationSource` bean:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    // Update to your production URLs:
    corsConfiguration.setAllowedOrigins(List.of("https://yourapp.com", "https://api.yourapp.com"));
    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    corsConfiguration.setExposedHeaders(List.of("Authorization"));
    corsConfiguration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
}
```

## 5. Token Expiry and Revocation

By default, the token expiration is hardcoded to 24 hours in `JwtTokenService.java`:
```java
.expiration(new Date(System.currentTimeMillis() + 86_400_000L)) // 24 h
```
If you need to change the expiration duration or implement a refresh-token strategy:
- Modify `System.currentTimeMillis() + [YOUR_DURATION_MS]`.
- Note that stateless JWTs cannot be trivially revoked before their expiration time without introducing a database-backed blacklist or short-lived tokens. If your application requires instant session invalidation, consider keeping token lifespans short (e.g., 15 minutes) and implementing a `/refresh` endpoint.
