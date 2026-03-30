# WP Manager - System Architecture

## Overview

WP Manager follows a **layered architecture** pattern with clear separation of concerns. The system is built using Spring Boot 3.4.1 with Java 21, following domain-driven design principles for entity modeling and RESTful API design for client communication.

## Architecture Layers

### 1. Presentation Layer (REST Controllers)
- **Location**: `src/main/java/com/wpmanager/models/*/[Entity]Controller.java`
- **Purpose**: Handle HTTP requests, input validation, and response formatting
- **Pattern**: Controllers extend [`DefaultController`](../../../src/main/java/com/wpmanager/shared/defaultImplements/DefaultController.java) for consistent CRUD operations
- **Key Examples**:
  - [`PluginController`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginController.java) - Plugin management
  - [`ClientController`](../../../src/main/java/com/wpmanager/models/hq/client/ClientController.java) - Client operations
  - [`S3ProviderController`](../../../src/main/java/com/wpmanager/models/storage/s3/S3ProviderController.java) - Storage configuration

### 2. Service Layer (Business Logic)
- **Location**: `src/main/java/com/wpmanager/models/*/[Entity]Service.java`
- **Purpose**: Encapsulate business rules, orchestrate operations, enforce authorization
- **Pattern**: Services extend [`DefaultServiceImplements`](../../../src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java)
- **Transaction Management**: `@Transactional` annotations for data consistency
- **Security**: `@PreAuthorize` annotations for role-based access control
- **Key Examples**:
  - [`PluginService`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java) - Plugin CRUD and upload orchestration
  - [`StorageProviderManager`](../../../src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java) - Multi-provider file operations

### 3. Repository Layer (Data Access)
- **Location**: `src/main/java/com/wpmanager/models/*/[Entity]Repository.java`
- **Purpose**: Data persistence and retrieval using Spring Data JPA
- **Pattern**: Interfaces extend `JpaRepository` through [`DefaultRepository`](../../../src/main/java/com/wpmanager/shared/defaultInterfaces/DefaultRepository.java)
- **Key Examples**:
  - [`PluginRepository`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginRepository.java)
  - [`BaseStorageProviderRepository`](../../../src/main/java/com/wpmanager/models/storage/storageProvider/BaseStorageProviderRepository.java)

### 4. Domain Layer (Entities)
- **Location**: `src/main/java/com/wpmanager/models/*/[Entity]Entity.java`
- **Purpose**: Define data model and relationships
- **ORM**: JPA/Hibernate for object-relational mapping
- **Key Entities**: PluginEntity, ThemeEntity, DownloadableEntity, FileEntity, ClientEntity, PlanEntity

### 5. Infrastructure Layer
- **Storage Clients**: S3-compatible storage provider implementations
- **Security Filters**: JWT authentication and authorization
- **Scheduled Jobs**: Background file replication
- **Utilities**: Checksum, signing, validation tools

## Core Component Architecture

### User Management System

```
BaseUserEntity (Abstract)
├── AdminEntity (System administrators)
└── ClientEntity (Subscribers)
    ├── @ManyToOne → PlanEntity
    ├── @OneToMany → WebsiteEntity
    ├── @OneToMany → FavoritePlugins
    ├── @OneToMany → FavoriteThemes
    └── @OneToMany → FavoriteListEntity
```

**Key Relationships**:
- **Inheritance**: `BaseUserEntity` uses `@Inheritance(strategy = InheritanceType.JOINED)` for table-per-subclass
- **Plan Association**: Clients link to subscription plans via `@ManyToOne`
- **WordPress Mapping**: `ClientEntity.wpID` maps to WordPress user ID

**Implementation Files**:
- [`BaseUserEntity`](../../../src/main/java/com/wpmanager/shared/models/baseUser/BaseUserEntity.java)
- [`ClientEntity`](../../../src/main/java/com/wpmanager/models/hq/client/ClientEntity.java)
- [`AdminEntity`](../../../src/main/java/com/wpmanager/models/hq/admin/AdminEntity.java)
- [`PlanEntity`](../../../src/main/java/com/wpmanager/models/hq/plan/PlanEntity.java)

### Content Management System

```
PluginEntity / ThemeEntity
├── @ManyToOne → AuthorEntity
├── @ManyToMany → PluginCategoryEntity / ThemeCategoryEntity
├── @OneToMany → DownloadableEntity (versions)
├── @OneToOne → ImageEntity (main image)
└── @ManyToMany ← WebsiteEntity (installed on)
```

**Version Management**:
```
DownloadableEntity
├── version: String (e.g., "1.0.0")
├── name: String (e.g., "plugin-name_1.0.0")
├── checkSum: String (SHA-256 hash)
├── filesSignature: String (unique identifier)
└── @OneToMany → FileEntity (one per storage provider)
```

**Implementation Files**:
- [`PluginEntity`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginEntity.java)
- [`ThemeEntity`](../../../src/main/java/com/wpmanager/models/downloads/theme/ThemeEntity.java)
- [`DownloadableEntity`](../../../src/main/java/com/wpmanager/models/files/downloadable/DownloadableEntity.java)
- [`AuthorEntity`](../../../src/main/java/com/wpmanager/models/downloads/author/AuthorEntity.java)

### Storage Provider System

```
StorageProviderEntity (Abstract, @Inheritance JOINED)
└── S3ProviderEntity
    ├── accessKey, secretKey
    ├── bucketName, region
    ├── endpointOverride (for Wasabi, etc.)
    ├── @OneToMany → FileEntity (via Map<signature, FileEntity>)
    └── getClient() → S3StorageClient
```

**Storage Client Hierarchy**:
```
BaseStorageProviderClient (Abstract, AutoCloseable)
└── S3StorageClient
    ├── uploadFile(MultipartFile, String) → URL
    ├── downloadFile(String) → byte[]
    ├── fileExists(String) → boolean
    ├── deleteFile(String) → boolean
    └── close() (resource cleanup)
```

**File Storage Architecture**:
```
FileEntity (Physical file on provider)
├── url: String (full S3 URL)
├── fileSignature: String (links to DownloadableEntity)
├── @ManyToOne → DownloadableEntity
└── Stored in: StorageProviderEntity.files Map
```

**Implementation Files**:
- [`StorageProviderEntity`](../../../src/main/java/com/wpmanager/models/storage/storageProvider/StorageProviderEntity.java)
- [`S3ProviderEntity`](../../../src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java)
- [`BaseStorageProviderClient`](../../../src/main/java/com/wpmanager/models/storage/storageProvider/BaseStorageProviderClient.java)
- [`S3StorageClient`](../../../src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java)
- [`FileEntity`](../../../src/main/java/com/wpmanager/models/files/file/FileEntity.java)

## Design Patterns

### 1. Template Method Pattern
**Usage**: Default CRUD operations in base classes
- **Base Class**: [`DefaultServiceImplements`](../../../src/main/java/com/wpmanager/shared/defaultImplements/DefaultServiceImplements.java)
- **Concrete Services**: Override specific methods when custom logic needed
- **Example**: [`PluginService.insert()`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:103) overrides to add categories and author relationships

### 2. Strategy Pattern
**Usage**: Storage provider implementations
- **Interface**: [`BaseStorageProviderClient`](../../../src/main/java/com/wpmanager/models/storage/storageProvider/BaseStorageProviderClient.java)
- **Concrete Strategies**: `S3StorageClient` (currently only one, but extensible)
- **Context**: [`StorageProviderEntity.getClient()`](../../../src/main/java/com/wpmanager/models/storage/storageProvider/StorageProviderEntity.java:66) returns appropriate client

### 3. Factory Pattern
**Usage**: Storage client creation
- **Factory Method**: `StorageProviderEntity.getClient()` returns provider-specific client
- **Example**: [`S3ProviderEntity.getClient()`](../../../src/main/java/com/wpmanager/models/storage/s3/S3ProviderEntity.java:67) returns `new S3StorageClient(this)`

### 4. Repository Pattern
**Usage**: Data access abstraction
- **Spring Data JPA**: Repositories extend `JpaRepository`
- **Custom Interface**: [`DefaultRepository<ENTITY, ID>`](../../../src/main/java/com/wpmanager/shared/defaultInterfaces/DefaultRepository.java)
- **Benefit**: Decouples business logic from persistence mechanism

### 5. DTO Pattern (Data Transfer Object)
**Usage**: API request/response objects
- **Mappers**: Convert between entities and DTOs
- **Full DTO**: Complete representation (e.g., `PluginDTO`)
- **Mini DTO**: Lightweight version (e.g., `PluginMiniDTO`)
- **Form Objects**: Input validation (e.g., `PluginForm`)

### 6. Facade Pattern
**Usage**: Simplify complex multi-provider operations
- **Facade**: [`StorageProviderManager`](../../../src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java)
- **Complexity Hidden**: Handles multiple storage providers, transactions, error handling
- **Methods**: `uploadFileToStorageProviders()`, `deleteAllFilesFromDownloadable()`

## Security Architecture

### Authentication Flow

1. **Login Request**: User submits credentials
2. **Basic Auth Validation**: Spring Security validates credentials
3. **JWT Generation**: [`JWTTokenGeneratorFilter`](../../../src/main/java/com/wpmanager/configuration/filter/JWTTokenGeneratorFilter.java) creates token
4. **Token Return**: JWT returned in `Authorization` header
5. **Subsequent Requests**: Token included in requests
6. **JWT Validation**: [`JWTTokenValidatorFilter`](../../../src/main/java/com/wpmanager/configuration/filter/JWTTokenValidatorFilter.java) validates and extracts user

### Authorization Layers

**Controller Level**:
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<PluginDTO> upload(...) { }
```

**Service Level**:
```java
@PreAuthorize("hasRole('ADMIN')")
public DownloadableDTO upload(PluginUploadForm form) { }
```

**Security Configuration**: [`SecurityConfig`](../../../src/main/java/com/wpmanager/configuration/security/SecurityConfig.java)
- JWT filters configured in filter chain
- CORS configuration for Angular frontend
- Stateless session management
- Custom authentication/access denied handlers

## File Upload Architecture

### Two-Phase Upload Strategy

**Phase 1: Immediate Upload (Synchronous)**
```
Client Request
  → PluginController.upload()
  → PluginService.upload()
      → Validate file (size, type, extension)
      → Calculate checksum (SHA-256)
      → Create DownloadableEntity
      → StorageProviderManager.uploadFileToStorageProviders()
          → Upload to DEFAULT providers only
          → Create FileEntity for each upload
          → Link to DownloadableEntity
      → Return DownloadableDTO to client
```

**Phase 2: Background Replication (Asynchronous)**
```
@Scheduled Job: FileDuplicator
  → Every ~3.3 minutes (fixed delay)
  → Find DownloadableEntities with incomplete replication
  → For each incomplete:
      → Select source provider (has file)
      → Download to temporary disk file
      → Wrap in DiskBasedMultipartFile
      → Upload to remaining providers
      → Create FileEntity for each
      → Delete temporary file
```

**Key Implementation Files**:
- [`PluginController.upload()`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginController.java:23)
- [`PluginService.upload()`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:163)
- [`StorageProviderManager.uploadFileToStorageProviders()`](../../../src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:54)
- [`FileDuplicator.duplicateFiles()`](../../../src/main/java/com/wpmanager/schedule/FileDuplicator.java:52)

### File Integrity System

**Checksum Calculation**:
- **Algorithm**: SHA-256
- **Implementation**: [`ChecksumUtils`](../../../src/main/java/com/wpmanager/shared/tools/ChecksumUtils.java)
- **Purpose**: Detect corruption, prevent duplicates
- **Storage**: `DownloadableEntity.checkSum`

**File Signature**:
- **Generation**: [`FileSigner.sign(checksum + downloadableName)`](../../../src/main/java/com/wpmanager/shared/tools/FileSigner.java)
- **Purpose**: Unique identifier linking FileEntity to DownloadableEntity
- **Usage**: Stored in both `FileEntity.fileSignature` and `DownloadableEntity.filesSignature`

## Transaction Management

### Service Layer Transactions
```java
@Transactional(rollbackFor = {
    ItemNotFoundException.class,
    InvalidInsertDetails.class,
    InvalidDeleteOperation.class,
    ItemAlreadyExist.class
})
public abstract class DefaultServiceImplements { }
```

**Propagation**: Default `REQUIRED` - joins existing transaction or creates new
**Isolation**: Default `READ_COMMITTED` from database
**Rollback**: Specified exceptions trigger rollback

### Transaction Boundaries

**Upload Operation** (Transactional):
- Create DownloadableEntity
- Upload to default providers
- Create FileEntity records
- Link entities
- **On Error**: All rolled back, no orphaned records

**Replication Job** (Non-transactional):
- Provider failures don't affect others
- Continues processing remaining items
- Next execution attempts retry
- **Philosophy**: Eventual consistency over strict ACID

## Scheduled Jobs

### FileDuplicator
- **Schedule**: `@Scheduled(fixedDelay = 200400, initialDelay = 5000000)`
  - Initial delay: 83 minutes (allows system stabilization)
  - Fixed delay: 3.3 minutes between completions
- **Concurrency**: `AtomicBoolean` prevents overlapping executions
- **Purpose**: Replicate files across all storage providers
- **Implementation**: [`FileDuplicator`](../../../src/main/java/com/wpmanager/schedule/FileDuplicator.java)

## Exception Handling

### Global Exception Handler
- **Location**: [`GlobalExceptionHandler`](../../../src/main/java/com/wpmanager/exceptions/GlobalExceptionHandler.java)
- **Annotations**: `@ControllerAdvice` intercepts controller exceptions
- **Handled Exceptions**:
  - `InvalidInsertDetails` → 400 Bad Request
  - `ItemAlreadyExist` → 409 Conflict
  - `ItemNotFoundException` → 404 Not Found
  - `InvalidDeleteOperation` → 400 Bad Request
  - `BadCredentialsException` → 401 Unauthorized

### Error Response Format
```java
ErrorHTTPRes {
    String timestamp;
    int status;
    String error;
    String message;
    String path;
}
```

## Package Structure

```
com.wpmanager
├── configuration/          # Security, filters, bootstrap
├── constant/              # Application constants
├── exceptions/            # Custom exception classes
├── models/
│   ├── downloads/         # Plugin, theme, author entities
│   ├── files/            # Downloadable, file entities
│   ├── hq/               # Client, plan, website, admin
│   └── storage/          # Storage provider implementations
├── schedule/             # Scheduled background jobs
└── shared/
    ├── defaultImplements/  # Base controller/service classes
    ├── defaultInterfaces/  # Interfaces for defaults
    ├── idempotency/       # Idempotency key tracking (planned)
    ├── models/           # Base user entity
    ├── securityUser/     # Security user implementation
    └── tools/            # Utilities (checksum, validation, etc.)
```

## Database Design

### Inheritance Strategies

**User Entities**: `JOINED` - separate tables with foreign keys
```
base_user (id, username, email, ...)
  ↓ FK
admin (id) references base_user(id)
client (id, wp_id, ...) references base_user(id)
```

**Storage Providers**: `JOINED` - separate tables per provider type
```
storage_provider (id, provider_name, ...)
  ↓ FK
s3_provider (id, access_key, bucket_name, ...) references storage_provider(id)
```

### Key Relationships

**Many-to-Many with Join Tables**:
- `plugin_and_category` - Links plugins to categories
- `theme_and_category` - Links themes to categories
- `client_favorite_list` - Links clients to favorite lists

**One-to-Many Eager Fetching** (Performance Concern):
- `PlanEntity.clients` - Eager fetch may cause N+1 queries
- `ClientEntity.websites` - Eager fetch for related websites
- `DownloadableEntity.files` - Eager fetch all provider files

## Critical Paths

### Plugin Upload Path
1. [`PluginController.upload()`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginController.java:23)
2. [`PluginService.upload()`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java:163)
3. [`UploadValidator.validateUpload()`](../../../src/main/java/com/wpmanager/shared/tools/UploadValidator.java)
4. [`ChecksumUtils.calculateChecksum()`](../../../src/main/java/com/wpmanager/shared/tools/ChecksumUtils.java)
5. [`StorageProviderManager.uploadFileToStorageProviders()`](../../../src/main/java/com/wpmanager/models/storage/storageProviderManager/StorageProviderManager.java:54)
6. [`S3StorageClient.uploadFile()`](../../../src/main/java/com/wpmanager/models/storage/s3/S3StorageClient.java:64)

### Authentication Path
1. Login request with Basic Auth
2. [`SecurityConfig.securityFilterChain()`](../../../src/main/java/com/wpmanager/configuration/security/SecurityConfig.java:54) validates
3. [`JWTTokenGeneratorFilter`](../../../src/main/java/com/wpmanager/configuration/filter/JWTTokenGeneratorFilter.java) generates JWT
4. Subsequent requests include JWT
5. [`JWTTokenValidatorFilter`](../../../src/main/java/com/wpmanager/configuration/filter/JWTTokenValidatorFilter.java) validates token
6. [`SecurityUserServiceImpl.loadUserByUsername()`](../../../src/main/java/com/wpmanager/shared/securityUser/SecurityUserServiceImpl.java) loads user details

## Scalability Considerations

### Current Limitations
1. **Synchronous Uploads**: Blocks request thread during file upload
2. **Single-Threaded Replication**: FileDuplicator processes files sequentially
3. **Eager Fetching**: Some relationships load unnecessarily
4. **No Caching**: Database queries repeated for frequently accessed data

### Planned Improvements
1. **Async Upload**: Move file upload to background thread
2. **Parallel Replication**: Use thread pool for concurrent uploads
3. **Lazy Loading**: Convert eager fetches to lazy where appropriate
4. **Caching Layer**: Add Redis or similar for hot data
5. **QueryDSL**: Optimize queries with dynamic query building

## Integration Points

### Future WordPress Integration
- **Sync Endpoints**: Receive user/plan updates from WordPress
- **Authentication**: JWT-based requests from WordPress site
- **Domain Verification**: Only main WordPress site can call sync endpoints
- **Rate Limiting**: Prevent abuse of sync endpoints

### Future Client Website Integration
- **API Keys**: Unique keys per client for website authentication
- **Plugin Communication**: Custom WordPress plugin on client sites
- **Command Execution**: Install/update/remove plugins remotely
- **Status Reporting**: Website health and plugin status