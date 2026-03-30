# WP Manager - Technology Stack

## Core Technologies

### Backend Framework
- **Spring Boot**: 3.4.1
- **Java**: 21 (LTS version)
- **Build Tool**: Maven

### Database
- **Primary Database**: MySQL 8
- **Test Database**: H2 (in-memory for tests)
- **ORM**: Hibernate (via Spring Data JPA)
- **Connection Pool**: HikariCP

### Security
- **Authentication**: JSON Web Tokens (JWT)
- **Library**: jjwt (0.12.5)
- **Framework**: Spring Security
- **Password Encoding**: BCrypt

### Storage
- **Provider Type**: S3-Compatible Storage
- **SDK**: AWS SDK for Java v2 (2.20.12)
- **Supported Providers**: AWS S3, Wasabi, DigitalOcean Spaces, etc.

### Testing
- **Framework**: JUnit 5 (Jupiter)
- **Mocking**: Mockito 5.14.2
- **Integration Testing**: Spring Boot Test
- **Test Runner**: Maven Surefire 3.2.5

## Key Dependencies

### Spring Boot Starters

```xml
<!-- Core Web & REST -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-rest</artifactId>
</dependency>

<!-- Data Access -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Reactive (for WebFlux) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Batch Processing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>

<!-- WebSocket Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### Database Drivers

```xml
<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- H2 for Testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### JWT Authentication

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

### S3 Storage

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.12</version>
</dependency>
```

### Utility Libraries

```xml
<!-- Lombok - Reduces boilerplate -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- DevTools - Hot reload in development -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

## Configuration

### Application Properties

**Location**: [`src/main/resources/application.properties`](../../../src/main/resources/application.properties)

```properties
# Application
spring.application.name=wpmanager

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/wp_manager
spring.datasource.username=jlievano
spring.datasource.password=natalie
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.open-in-view=false

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000

# File Upload
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

### Test Configuration

**Location**: [`src/main/resources/application-test.properties`](../../../src/main/resources/application-test.properties)

- Uses H2 in-memory database
- Overrides production settings for testing
- Faster test execution with simplified configuration

## Development Setup

### Prerequisites

1. **Java 21** (JDK 21)
   - Download from: Oracle, OpenJDK, or AdoptOpenJDK
   - Set `JAVA_HOME` environment variable

2. **Maven 3.8+**
   - Comes bundled with project (mvnw)
   - Or install globally

3. **MySQL 8**
   - Install MySQL server
   - Create database: `wp_manager`
   - Configure credentials in `application.properties`

4. **S3-Compatible Storage Account**
   - AWS S3, Wasabi, or similar
   - Access credentials (access key, secret key)
   - Bucket created and configured

### IDE Setup

**Recommended**: IntelliJ IDEA or Eclipse with Spring Tools

**Required Plugins**:
- Lombok plugin (for IDE support)
- Spring Boot plugin

**Configuration**:
- Enable annotation processing (for Lombok)
- Import as Maven project
- Set Java SDK to 21

### Running the Application

**Using Maven Wrapper (Recommended)**:
```bash
./mvnw spring-boot:run
```

**Using Installed Maven**:
```bash
mvn spring-boot:run
```

**From IDE**:
- Run main class: [`WpmanagerApplication`](../../../src/main/java/com/wpmanager/WpmanagerApplication.java)

### Running Tests

**All Tests**:
```bash
./mvnw test
```

**Specific Test Suite**:
```bash
./mvnw test -Dtest=RepositorySuiteTest
```

**Skip Tests**:
```bash
./mvnw clean install -DskipTests
```

### Maven Build Configuration

**Compiler Plugin**:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

**Surefire Plugin (Testing)**:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <argLine>-XX:+EnableDynamicAgentLoading -Djdk.instrument.traceUsage=false</argLine>
        <excludes>
            <exclude>**/*SuiteTest.java</exclude>
        </excludes>
    </configuration>
</plugin>
```

## Annotations Reference

### Spring Framework Annotations

**Component Scanning**:
- `@SpringBootApplication` - Main application class
- `@Component` - Generic Spring component
- `@Service` - Service layer component
- `@Repository` - Data access layer component
- `@Controller` / `@RestController` - Web layer component
- `@Configuration` - Configuration class

**Dependency Injection**:
- `@Autowired` - Automatic dependency injection
- `@Qualifier` - Specify which bean to inject
- Constructor injection (preferred over field injection)

**Web Layer**:
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` - HTTP method mappings
- `@PathVariable` - Extract URL path variables
- `@RequestBody` - Map request body to object
- `@RequestParam` - Extract query parameters
- `@Valid` - Enable validation

**Data Layer**:
- `@Entity` - JPA entity
- `@Table` - Specify table name
- `@Id` - Primary key
- `@GeneratedValue` - Auto-generated ID
- `@Column` - Column mapping
- `@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@OneToOne` - Relationships
- `@JoinColumn` - Foreign key specification
- `@JoinTable` - Join table for many-to-many

**Transaction Management**:
- `@Transactional` - Transaction boundary
- `@EnableTransactionManagement` - Enable transaction support

**Security**:
- `@EnableWebSecurity` - Enable Spring Security
- `@EnableMethodSecurity` - Enable method-level security
- `@PreAuthorize` - Authorization rules
- `@PostAuthorize` - Post-invocation authorization

**Scheduling**:
- `@EnableScheduling` - Enable scheduled tasks
- `@Scheduled` - Mark method as scheduled task

### Lombok Annotations

- `@Data` - Generates getters, setters, toString, equals, hashCode
- `@Getter` / `@Setter` - Generate getters/setters
- `@NoArgsConstructor` - Generate no-arg constructor
- `@AllArgsConstructor` - Generate all-args constructor
- `@Builder` - Builder pattern implementation
- `@SuperBuilder` - Builder for inheritance hierarchies
- `@EqualsAndHashCode` - Generate equals and hashCode methods

### Validation Annotations

- `@NotNull` - Field cannot be null
- `@NotEmpty` - String/Collection cannot be empty
- `@Size` - String/Collection size constraints
- `@Positive` - Number must be positive
- `@Valid` - Enable nested validation

## Architecture Patterns

### Layered Architecture
- **Presentation** → Controllers
- **Business Logic** → Services
- **Data Access** → Repositories
- **Domain Model** → Entities

### Design Patterns Used
1. **Template Method**: `DefaultServiceImplements` provides base CRUD
2. **Strategy**: `BaseStorageProviderClient` with multiple implementations
3. **Factory**: `StorageProviderEntity.getClient()`
4. **Repository**: Spring Data JPA repositories
5. **DTO**: Separate data transfer objects from entities
6. **Facade**: `StorageProviderManager` simplifies multi-provider operations

## Code Organization

### Package by Feature
```
com.wpmanager
├── models
│   ├── downloads (plugins, themes, authors, categories)
│   ├── files (downloadables, files, images)
│   ├── hq (clients, plans, websites, admins)
│   └── storage (providers, S3 implementation)
├── configuration (security, filters, bootstrap)
├── schedule (background jobs)
├── shared (base classes, utilities)
└── exceptions (custom exceptions)
```

### Naming Conventions

**Classes**:
- Entities: `*Entity` (e.g., `PluginEntity`)
- DTOs: `*DTO`, `*MiniDTO` (e.g., `PluginDTO`, `PluginMiniDTO`)
- Forms: `*Form` (e.g., `PluginForm`)
- Services: `*Service` (e.g., `PluginService`)
- Controllers: `*Controller` (e.g., `PluginController`)
- Repositories: `*Repository` (e.g., `PluginRepository`)
- Mappers: `*Mapper` (e.g., `PluginMapper`)

**Methods**:
- camelCase (no underscores)
- Descriptive names
- CRUD operations: `insert()`, `update()`, `delete()`, `getOne()`, `getAll()`

**Variables**:
- camelCase
- No underscores
- Meaningful names

## Performance Considerations

### Database
- **Connection Pooling**: HikariCP with 10 max connections
- **Query Optimization**: Needed - some N+1 problems exist
- **Indexing**: Applied on frequently queried columns
- **Fetch Strategy**: Mix of LAZY and EAGER (needs optimization)

### File Operations
- **Checksum Calculation**: SHA-256, memory-efficient streaming
- **Temporary Files**: Disk-based for large file handling
- **Multi-Provider**: Parallel upload capability (not yet implemented)
- **Background Jobs**: Async replication for non-blocking uploads

### Caching
- **Current State**: No caching implemented
- **Planned**: Redis or Caffeine for hot data
- **Targets**: Storage provider configs, frequently accessed plugins/themes

## Security Best Practices

### Implemented
✅ JWT-based authentication
✅ BCrypt password hashing
✅ Role-based authorization (`@PreAuthorize`)
✅ CORS configuration for Angular frontend
✅ Input validation with Bean Validation
✅ SQL injection prevention (JPA prepared statements)

### To Implement
🔄 Rate limiting per client
🔄 API key management for websites
🔄 Audit logging for sensitive operations
🔄 File virus scanning
🔄 Download URL expiration

## Known Limitations

### Current System
1. **Synchronous Uploads**: Blocks request thread (needs async)
2. **Single-Threaded Replication**: Sequential processing in FileDuplicator
3. **Large File Handling**: No multipart upload for files >100MB
4. **Memory Usage**: Checksum calculation loads full file in memory
5. **Query Performance**: N+1 problems from eager fetching

### Planned Improvements
- Async/reactive upload processing
- Thread pool for parallel replication
- Multipart upload for large files
- Streaming checksum calculation
- QueryDSL for optimized queries
- Redis caching layer
- Event-driven architecture

## External Service Integration

### WordPress Membership Pro API
- **Purpose**: User and subscription synchronization
- **Authentication**: Custom JWT tokens
- **Endpoints**: User levels, plan listings
- **Status**: Integration planned, not yet implemented

### Angular Frontend
- **Purpose**: Client-facing UI
- **Authentication**: JWT token from WordPress
- **Communication**: RESTful API calls
- **Status**: Separate repository, integration pending

### S3-Compatible Storage
- **Purpose**: File storage and distribution
- **Providers**: AWS S3, Wasabi, etc.
- **Configuration**: Access keys, bucket names, regions
- **Status**: Fully implemented and operational

## Monitoring and Logging

### Logging Framework
- **Library**: SLF4J with Logback (Spring Boot default)
- **Levels**: ERROR, WARN, INFO, DEBUG, TRACE
- **Usage**: Extensive logging in services and scheduled jobs

### Log Configuration
```java
private final Logger logger = LoggerFactory.getLogger(ClassName.class);
```

### Planned Monitoring
- Application performance monitoring (APM)
- File replication job status tracking
- API usage metrics
- Error tracking and alerting
- Storage usage monitoring