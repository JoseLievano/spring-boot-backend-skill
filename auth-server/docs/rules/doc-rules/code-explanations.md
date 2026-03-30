# Code Explanation Rules

## Purpose

Code explanation files provide detailed documentation of individual Java source files.

---

## Naming Convention

Convert Java filename to markdown by replacing dots with dashes:
- `MyEntity.java` → `MyEntity-java.md`
- `PluginController.java` → `PluginController-java.md`
- `application.properties` → `application-properties.md`

---

## Required Structure

```markdown
# ClassName.java

#tag1 #tag2 #tag3

## Purpose
[What this class/file does and why it exists]

---

## Class Information

**Type:** [Interface | Abstract Class | Concrete Class | Enum | Configuration]
**Package:** [Full package path]
**Dependencies:** [Key dependencies]

---

## Class-Level Annotations

### @AnnotationName
**Type:** [Spring | Lombok | JPA | Custom]
**Purpose:** [Why this annotation is used]
**Documentation:** [Link to official docs]

[Repeat for each annotation]

---

## Fields

### fieldName
**Type:** [Field type]
**Purpose:** [What this field represents]
**Annotations:** [@Annotation1, @Annotation2]

[Repeat for each field]

---

## Methods

### methodName()
**Purpose:** [What this method does]
**Parameters:** [List parameters]
**Returns:** [Return type and description]
**Throws:** [Exceptions thrown]

**Annotations:**
- `@AnnotationName` ([Type]) - [Purpose] - [Documentation Link]

**Implementation:**
[Detailed explanation of how it works]

**Diagram (if complex):**
```mermaid
sequenceDiagram
    [Diagram here]
```

**Related Files:**
- [[OtherCodeFile-java]] - [Relationship explanation]
- `src/main/java/com/wpmanager/path/to/ActualFile.java:123` - [Why it's related]

[Repeat for each method]

---

## Dependencies

This file depends on:
- [[DependencyFile-java]] - [Why]
- [[AnotherFile-java]] - [Why]

This file is used by:
- [[ConsumerFile-java]] - [How]

---

## Notes
[Any additional important information]
```

---

## Requirements for Each Annotation

1. **Annotation Name:** `@AnnotationName`
2. **Type:** Spring, Lombok, JPA, Jakarta, Custom, etc.
3. **Purpose:** Why this annotation is used in this context
4. **Documentation Link:** Link to official documentation

**Example:**
```markdown
### @Transactional
**Type:** Spring Framework
**Purpose:** Ensures atomic database operations with automatic rollback on exceptions
**Documentation:** [Spring @Transactional](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/annotation/Transactional.html)
```

---

## Required Tags

### Component Type Classification:
- `#controller` - REST controllers handling HTTP requests
- `#service` - Business logic services
- `#repository` - Data access repositories
- `#entity` - JPA entities representing database tables
- `#dto` - Data Transfer Objects for API responses
- `#mini-dto` - Lightweight DTOs without relationships
- `#form` - Input validation objects for API requests
- `#mapper` - Object mapping utilities between layers
- `#exception` - Custom exception classes
- `#filter` - Security or request filters
- `#handler` - Exception or event handlers
- `#configuration` - Spring configuration classes

### Technology Classification:
- `#jpa` - JPA/Hibernate related classes
- `#spring` - Spring Framework components
- `#security` - Security-related classes
- `#rest` - REST API components
- `#data-access` - Database access components
- `#business-logic` - Business logic components
- `#conversion` - Object conversion/mapping
- `#input` - User input handling
- `#data-transfer` - Data transfer between layers
- `#error-handling` - Error and exception handling

**Example:** `#controller #rest #spring`

---

## Link Requirements

- **Existing Code Explanations:** Use Obsidian wiki links: `[[PluginService-java]]`
- **Non-existent Code Explanations:** Create link anyway: `[[NonExistentFile-java]]` (see below)
- **Source Code:** Show path from project root (not as a clickable link): `src/main/java/com/wpmanager/path/to/PluginService.java:163`

---

## Handling Missing Code Explanation Files

When you need to reference a code file that doesn't have an explanation file yet:

1. **Create the link anyway:** `[[MissingFile-java]]`
2. **Ask the user:** "I noticed that `MissingFile.java` doesn't have a code explanation file. Would you like me to create one?"
3. **If yes:** Use these same rules to create the new explanation file
4. **Update the original file** with the now-working link