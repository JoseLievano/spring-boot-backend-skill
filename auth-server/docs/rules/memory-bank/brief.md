# WP Manager - Project Brief

## 1. Project Foundation

### 1.1 Technology Stack
- **Backend Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **Frontend**: Angular (separate application)
- **Storage**: S3-compatible providers (AWS S3, Wasabi, etc.)
- **Authentication**: JWT (JSON Web Tokens)
- **Query Layer**: QueryDSL (planned for pagination and filtering)

### 1.2 Project Purpose
WP Manager is a comprehensive WordPress plugin and theme distribution platform designed as a "Netflix for WordPress assets." The system enables clients to download, install, and manage WordPress plugins and themes across multiple websites through a centralized API and Angular frontend.

---

## 2. High-Level Overview

### 2.1 What We're Building
A Spring Boot REST API backend that serves as the core management system for:
- WordPress plugin and theme distribution
- Client subscription and access management
- Multi-website plugin/theme installation and updates
- Secure file storage and download management
- Integration with external WordPress membership systems

### 2.2 System Architecture Components

#### 2.2.1 Main WordPress Business Website
- **Purpose**: Client registration, subscription management, and payments
- **Technology**: WordPress with Ultimate Membership Pro plugin
- **Role**: Acts as the authentication and payment gateway
- **Integration**: Communicates with Spring API via secure JWT-authenticated endpoints

#### 2.2.2 Spring Boot API (This Project)
- **Purpose**: Core business logic and data management
- **Responsibilities**:
  - User and subscription synchronization with WordPress
  - Plugin and theme repository management
  - File storage and download orchestration
  - Website connection and management
  - API key generation and validation (planned)
  - Auto-update coordination (planned)

#### 2.2.3 Angular Frontend Application
- **Purpose**: Client-facing interface for the application
- **Access**: Launched from WordPress site at `ourdomain.com/app`
- **Authentication**: JWT tokens passed from WordPress for seamless login
- **Functionality**: Provides UI for browsing, downloading, and managing plugins/themes

#### 2.2.4 Client WordPress Plugin (Planned)
- **Purpose**: Enables remote management of client websites
- **Functionality**: 
  - Receives installation/update commands from API
  - Reports website status and installed plugins/themes
  - Facilitates secure communication via API keys

---

## 3. Core Requirements and Goals

### 3.1 External System Integration

#### 3.1.1 WordPress Membership Integration
The system integrates with **Ultimate Membership Pro** plugin on the main WordPress website for subscription management.

**Key Integration Points:**

1. **User Level Retrieval**
   - Endpoint: `http://localhost/?ihc_action=api-gate&ihch=xcVDJibb4zep53s2l06Y&action=get_user_levels&uid={userId}`
   - Returns: User subscription details including plan, status, and expiration
   - Example Response:
   ```json
   {
     "response": {
       "1": {
         "id": "1",
         "user_id": "4420",
         "level_id": "1",
         "start_time": "2025-03-17 07:00:28",
         "expire_time": "2026-03-12 00:23:52",
         "status": "1",
         "label": "Free",
         "level_slug": "free",
         "is_expired": false
       }
     }
   }
   ```

2. **Available Plans Retrieval**
   - Endpoint: `http://localhost/?ihc_action=api-gate&ihch=xcVDJibb4zep53s2l06Y&action=list_levels`
   - Returns: List of all membership plans configured in WordPress
   - Example Response:
   ```json
   {
     "response": [
       {
         "level_id": 1,
         "label": "Free",
         "slug": "free"
       },
       {
         "level_id": 2,
         "label": "premium",
         "slug": "premium"
       }
     ]
   }
   ```

**Data Synchronization Strategy:**

1. **Real-Time Sync**: Custom WordPress plugin sends events to Spring API when:
   - New user is created
   - User changes membership plan
   - Membership is created, updated, or expired
   - User data is modified

2. **Scheduled Sync**: Background jobs verify data consistency (daily or bi-daily)

3. **Security**: All WordPress-to-API communication is:
   - JWT-authenticated
   - Domain-verified (only main website can call sync endpoints)
   - Rate-limited for security

#### 3.1.2 Entity Mapping

**[`ClientEntity`](src/main/java/com/wpmanager/models/hq/client/ClientEntity.java)**: Maps WordPress users to internal system
- `wpID`: WordPress user ID (unique identifier)
- `plan`: Reference to current subscription plan
- `websites`: Collection of managed WordPress sites
- `apiKey`: For website plugin authentication (planned)
- `apiRateLimit`: API usage limits per plan

**[`PlanEntity`](src/main/java/com/wpmanager/models/hq/plan/PlanEntity.java)**: Maps WordPress membership levels
- `wpIDs`: Set of WordPress level IDs (supports multi-level mapping)
- `name`: Plan name
- `websiteCountLimit`: Maximum websites allowed
- `downloadLimit`: Monthly download restrictions
- `clients`: Collection of subscribed users

### 3.2 Core Features

#### 3.2.1 Plugin and Theme Repository

**Primary Functionality:**
- Admins create plugin/theme entries with metadata
- Multiple versions per plugin/theme are supported
- Version history tracking for rollbacks
- Download statistics and popularity metrics

**File Management:**
- Each version uploaded to S3-compatible storage providers
- Multiple provider redundancy for high availability
- URL obfuscation to prevent unauthorized sharing
- Checksum validation for file integrity

**Related Documentation:** See [`docs/Docs/File Upload Process.md`](docs/Docs/File%20Upload%20Process.md) for detailed upload architecture

**Key Entities:**
- [`PluginEntity`](src/main/java/com/wpmanager/models/downloads/plugin/PluginEntity.java): Plugin metadata and relationships
- [`ThemeEntity`](src/main/java/com/wpmanager/models/downloads/theme/ThemeEntity.java): Theme metadata and relationships
- [`DownloadableEntity`](src/main/java/com/wpmanager/models/files/downloadable/DownloadableEntity.java): Specific version of a plugin/theme
- [`FileEntity`](src/main/java/com/wpmanager/models/files/file/FileEntity.java): Physical file representation per storage provider

#### 3.2.2 Client Downloads

**User Experience:**
- Browse available plugins and themes by category
- View version history and changelogs
- Download latest or specific versions
- Favorite plugins/themes for quick access
- Create custom lists for organization

**Technical Implementation:**
- Secure, time-limited download URLs
- Plan-based download restrictions
- Download tracking for analytics
- Bandwidth optimization through CDN-like distribution

#### 3.2.3 Website Management (Planned Feature)

**Vision:**
Clients can remotely manage their WordPress websites from the Angular application.

**Architecture (In Ideation Phase):**

1. **Client Side:**
   - Client installs custom WP Manager plugin on their website
   - Plugin receives unique API key from Spring API
   - Website added to client's account via [`WebsiteEntity`](src/main/java/com/wpmanager/models/hq/website/WebsiteEntity.java)

2. **Communication Flow:**
   - Spring API ↔ Client Website Plugin via API key authentication
   - Plugin reports: installed plugins, themes, WordPress version, pending updates
   - API sends commands: install plugin/theme, update, remove

3. **Features:**
   - View all websites in one dashboard
   - See which sites need updates
   - Push plugin/theme installations across multiple sites
   - Enable/disable auto-updates per website
   - Monitor website health and connectivity

4. **Security:**
   - Encrypted communication channels
   - API key rotation capability
   - Domain verification
   - Rate limiting per client

**Current State:** Architecture not yet finalized, endpoint design pending

### 3.3 Authentication and Authorization Flow

#### 3.3.1 User Login Process

1. User logs into main WordPress website
2. User clicks "Launch App" button
3. WordPress requests JWT token from Spring API (secured endpoint)
4. Spring API generates and returns JWT token
5. WordPress stores token in browser
6. User redirected to `ourdomain.com/app` (Angular application)
7. Angular app reads token from browser
8. Angular authenticates with Spring API using JWT
9. User session established in Angular application

#### 3.3.2 API Security

**JWT Authentication:**
- All API requests require valid JWT tokens
- Tokens contain user role and subscription information
- Token expiration and refresh mechanism

**Role-Based Access Control:**
- Admin: Full system access, can manage plugins/themes
- Client: Limited to subscription features and own websites
- Guest: Public endpoints only (if any)

**Endpoint Security:**
- WordPress sync endpoints: JWT + domain verification
- Admin endpoints: Admin role required
- Client endpoints: Valid subscription required
- Download endpoints: Plan-based access control

### 3.4 File Storage Architecture

#### 3.4.1 Multi-Provider Strategy
- Primary storage on multiple S3-compatible providers
- Automatic replication across all configured providers
- Fallback mechanism if primary provider unavailable
- Geographic distribution for performance

#### 3.4.2 Upload Process
1. Admin uploads plugin/theme version via REST endpoint
2. File validated (size, format, integrity)
3. Uploaded immediately to default provider(s)
4. Background job replicates to remaining providers
5. Checksum and signature generated for tracking
6. Database records created for file references

#### 3.4.3 Download Security
- Direct S3 URLs hidden from users
- Time-limited presigned URLs generated per request
- Download tracking for analytics and rate limiting
- Prevention of unauthorized sharing

**Implementation Details:** See [`docs/Docs/File Upload Process.md`](docs/Docs/File%20Upload%20Process.md)

### 3.5 Data Models Overview

#### 3.5.1 User Management
- **AdminEntity**: System administrators with full access
- **ClientEntity**: Subscribers with plan-based access
- **BaseUserEntity**: Common user fields (authentication, profile)

#### 3.5.2 Subscription Management
- **PlanEntity**: Subscription plans with limits and features
- Plan-to-client relationship tracking
- WordPress membership level synchronization

#### 3.5.3 Content Management
- **PluginEntity** / **ThemeEntity**: Asset metadata
- **AuthorEntity**: Plugin/theme authors
- **PluginCategoryEntity** / **ThemeCategoryEntity**: Organization
- **DownloadableEntity**: Version management
- **FileEntity**: Physical file storage references

#### 3.5.4 Client Assets
- **WebsiteEntity**: Client's WordPress websites
- **FavoriteListEntity**: Custom organization lists
- Plugin/theme favorites per client

#### 3.5.5 Storage Infrastructure
- **StorageProviderEntity**: Base provider configuration
- **S3ProviderEntity**: S3-specific configuration
- Multi-provider redundancy support

---

## 4. Current Implementation Status

### 4.1 Completed Features ✅
- Core entity models and relationships
- Plugin and theme repository management
- File upload system with multi-provider support
- S3 storage integration with automatic replication
- Background jobs for file redundancy
- JWT authentication infrastructure
- Basic CRUD operations for all entities
- File checksum and integrity validation

### 4.2 In Progress 🚧
- QueryDSL integration for advanced querying
- Pagination and filtering mechanisms
- Download analytics and tracking
- Admin dashboard endpoints

### 4.3 Planned Features 📋

**High Priority:**
- API key generation and management system
- WordPress sync endpoints (secured with JWT and domain verification)
- Website connection management
- Custom WordPress plugin for client sites
- Remote installation/update functionality
- Download URL generation with expiration

**Medium Priority:**
- Advanced search and filtering with QueryDSL
- Download rate limiting per plan
- Subscription upgrade/downgrade flows
- Email notifications for updates
- Usage analytics dashboard

**Low Priority:**
- Automated testing coverage expansion
- Performance monitoring and optimization
- CDN integration for global distribution
- Multi-language support
- Mobile API optimization

---

## 5. Technical Considerations

### 5.1 Scalability
- Microservices architecture consideration for future growth
- Database optimization with proper indexing
- Caching strategy for frequently accessed data
- Asynchronous processing for long-running operations

### 5.2 Security
- Input validation on all endpoints
- SQL injection prevention via JPA
- XSS protection in responses
- Rate limiting to prevent abuse
- Secure credential storage
- Regular security audits

### 5.3 Performance
- Lazy loading for entity relationships
- Database query optimization
- File transfer optimization
- Connection pooling
- Response caching where appropriate

### 5.4 Reliability
- Transaction management for data consistency
- Retry mechanisms for external API calls
- Graceful degradation if providers unavailable
- Comprehensive error logging
- Health check endpoints

### 5.5 Monitoring
- Application performance monitoring (planned)
- File replication job monitoring
- API usage metrics
- Error tracking and alerting
- Storage usage tracking

---

## 6. Future Expansion Possibilities

### 6.1 Content Types
- Elementor template kits
- WooCommerce extensions
- WordPress block libraries
- Documentation and tutorials

### 6.2 Features
- One-click staging environment creation
- Automated backup before updates
- Compatibility checking before installation
- Plugin conflict detection
- Performance optimization suggestions

### 6.3 Business
- Partner/affiliate program for developers
- White-label reseller options
- Enterprise tier with custom features
- API access for third-party integrations

---

## 7. Development Guidelines

### 7.1 Code Standards
- Follow Spring Boot best practices
- Maintain clean separation of concerns
- Write comprehensive JavaDoc comments
- Use meaningful variable and method names
- Avoid underscores in naming (use camelCase)
- Keep classes focused and cohesive

### 7.2 Testing Strategy
- Unit tests for business logic
- Integration tests for API endpoints
- Repository tests for database operations
- End-to-end tests for critical flows
- Manual testing for WordPress integration

### 7.3 Documentation
- Keep API documentation current
- Document all external integrations
- Maintain entity relationship diagrams
- Update brief as features evolve

---

## 8. Key Challenges and Considerations

### 8.1 WordPress Integration
- **Challenge**: Keeping Spring data synchronized with WordPress
- **Solution**: Event-driven updates + scheduled verification jobs

### 8.2 File Distribution
- **Challenge**: Preventing unauthorized file sharing
- **Solution**: Time-limited URLs, download tracking, rate limiting

### 8.3 Remote Website Management
- **Challenge**: Secure communication with multiple client sites
- **Solution**: API keys, encrypted channels, domain verification (architecture pending)

### 8.4 Plan Enforcement
- **Challenge**: Enforcing download and website limits
- **Solution**: Database-level tracking with real-time validation

### 8.5 Multi-Provider Storage
- **Challenge**: Maintaining file consistency across providers
- **Solution**: Checksum validation, scheduled replication jobs, automatic retry

---

## 9. Project Dependencies

### 9.1 External Services
- **WordPress Website**: User management and payments
- **Ultimate Membership Pro**: Subscription API
- **S3 Providers**: File storage (Wasabi, AWS S3, etc.)
- **Angular Frontend**: User interface

### 9.2 Internal Components
- **Spring Boot API**: Core business logic (this project)
- **WordPress Plugin**: Website sync events (to be developed)
- **Client WordPress Plugin**: Remote management (to be developed)

---

## 10. Success Metrics

### 10.1 Technical Metrics
- API response time < 200ms for common operations
- File upload success rate > 99.5%
- Zero data inconsistencies between WordPress and Spring
- 99.9% uptime for core API services

### 10.2 Business Metrics
- Client satisfaction with download speeds
- Number of active subscriptions
- Download volume and trends
- Website connection adoption rate
- Feature usage analytics

---

## Summary

WP Manager is a comprehensive Spring Boot API backend for a WordPress asset distribution platform. It integrates with an external WordPress membership system for authentication and payments while providing robust plugin/theme management, multi-provider file storage, and planned remote website management capabilities. The system prioritizes security, scalability, and reliability while maintaining flexibility for future expansion.

**Current Focus**: Completing API key management, WordPress synchronization endpoints, and QueryDSL integration for advanced filtering and pagination.