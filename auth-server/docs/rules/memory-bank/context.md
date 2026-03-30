# WP Manager - Current Context

## Current Work Focus

The project is in active development with core features implemented and several areas requiring refinement and optimization.

### Active Development Areas
1. **File Upload System Optimization**: Improving the plugin/theme upload process for better performance and reliability
2. **Code Quality Improvements**: Addressing known issues documented in `/docs/Bugs/` directory
3. **Testing**: Expanding test coverage across repositories and services

## Recent Changes

### Completed (As of Project State)
- ✅ Core entity models and JPA relationships established
- ✅ JWT authentication with Spring Security implemented
- ✅ Multi-provider S3 storage integration working
- ✅ Background file replication job (FileDuplicator) operational
- ✅ Basic CRUD operations for all major entities
- ✅ File upload process with checksum validation
- ✅ Admin bootstrap configuration for initial setup
- ✅ Optimized JWT validation by removing DB hits from the filter layer
- ✅ JSON-based login implementation (Problem 4.2)

### In Progress
- 🔄 Security Hardening (CORS, Token logging, etc.)
- 🔄 Addressing performance bottlenecks identified in bug documentation
- 🔄 QueryDSL integration (planned but not yet implemented)
- 🔄 Pagination and filtering mechanisms
- 🔄 Idempotency implementation for upload operations

## Known Issues

The project maintains detailed documentation of technical debt and improvements in `/docs/Bugs/` directory:

**Critical Issues (To-Do)**:
- God Class pattern in [`PluginService`](../../../src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java) - needs refactoring
- N+1 query problems from eager fetching relationships
- Synchronous upload blocks request thread - should be asynchronous
- No retry mechanism for failed uploads
- Race conditions on concurrent uploads
- Single-threaded replication in [`FileDuplicator`](../../../src/main/java/com/wpmanager/schedule/FileDuplicator.java)
- Memory-intensive checksum calculation for large files
- No multipart upload for large files (>100MB may cause issues)

**Completed Issues**:
- ✅ Checksum collision risk addressed
- ✅ LazyInitializationException in PluginService resolved
- ✅ Duplicate version prevention implemented
- ✅ File size validation added

## Next Steps

### Immediate Priorities
1. **WordPress Integration**: Implement sync endpoints for user and plan data
2. **API Key System**: Design and implement API key generation/validation for client websites
3. **QueryDSL Setup**: Add QueryDSL for advanced filtering and pagination
4. **Performance Optimization**: Address synchronous upload and N+1 query issues

### Short-Term Goals
1. Refactor large service classes following Single Responsibility Principle
2. Implement caching strategy for frequently accessed data
3. Add comprehensive logging and monitoring
4. Expand test coverage to >80%
5. Implement rate limiting for API endpoints

### Medium-Term Goals
1. Complete WordPress synchronization architecture
2. Design and develop client WordPress plugin for remote management
3. Implement download URL generation with expiration
4. Add analytics dashboard for admins
5. Email notification system for updates

## Current Architecture State

### Stable Components
- **Entity Layer**: Well-defined JPA entities with proper relationships
- **Security**: JWT authentication working correctly
- **File Storage**: S3 integration reliable with multi-provider support
- **Scheduled Jobs**: FileDuplicator running successfully

### Components Needing Work
- **Service Layer**: Some services too large, need decomposition
- **Query Performance**: Eager fetching causing N+1 problems
- **Upload Process**: Needs asynchronous handling and progress tracking
- **Error Handling**: Some areas lack comprehensive error recovery

## Development Environment

### Database
- **Type**: MySQL 8
- **Connection**: `jdbc:mysql://localhost:3306/wp_manager`
- **JPA Strategy**: `update` (for development; should use migrations for production)
- **Connection Pool**: HikariCP with 10 max connections, 5 min idle

### Key Configuration Files
- [`application.properties`](../../../src/main/resources/application.properties): Main configuration
- [`application-test.properties`](../../../src/main/resources/application-test.properties): Test configuration with H2 database

## Testing Status

### Current Coverage
- Repository tests implemented for all main entities
- E2E tests for Author entity
- Test suites: Repository, E2E, Utils

### Gaps
- Limited service layer testing
- No controller integration tests yet
- Missing tests for scheduled jobs
- No load/performance tests

## Dependencies on External Systems

### Required for Full Operation
1. **WordPress Website**: For user authentication and subscription management
2. **Ultimate Membership Pro API**: For subscription level data
3. **S3-Compatible Storage**: At least one provider must be configured
4. **Angular Frontend**: (separate repo) - not yet integrated

### Current State
- Backend API is functional independently
- WordPress integration endpoints not yet implemented
- Frontend integration pending

## Technical Debt Priority

Based on `/docs/Bugs/` analysis:

**High Priority**:
1. God Class refactoring (PluginService, ThemeService)
2. N+1 query optimization
3. Synchronous upload → Async conversion
4. Race condition prevention

**Medium Priority**:
1. Caching implementation
2. Event-driven architecture adoption
3. Retry mechanisms
4. Progress indication for uploads

**Low Priority**:
1. Anemic domain model improvements
2. Package structure refinement
3. Advanced monitoring integration
4. Multi-language support preparation