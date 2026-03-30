# Feature Documentation Rules

## Purpose

Feature files describe new functionality to be added to the application.

---

## Required Structure

### Section 1: Feature Description
- Complete description of the feature
- How it affects the overall product
- Which workflows, processes, or systems are affected
- Links to related `docs/Docs/` files (if systems are affected)
- Links to related `docs/Code/` files (if code will be modified)
- Detailed description of the feature
- Possible side effects or bugs to watch for

### Section 2: Implementation Architecture/Plan
- Detailed explanation of implementation approach
- All files that need to be modified
- How each file needs to be modified
- Links to code explanation files for Java files being changed
- Step-by-step implementation guide
- Each implementation step must include a completion checkbox immediately below the step title (e.g., `- [ ] Step description` for pending, `- [x] Step description` for completed)

---

## Complete Template

```markdown
#high #new-feature

## Feature: [Feature Name]

### Description
[Detailed feature description...]

### Affected Systems
- Link to [[System Documentation]]
- Link to [[Process Documentation]]

### Impact Analysis
[How this affects existing functionality...]

---

## Implementation Architecture

### Changes Required

#### 1. [Component/File Name]
**Purpose:** [Why we're modifying this]
**Changes:** [What needs to change]
**Link:** [[Code Explanation File]] or [ActualFile.java](../../src/path/to/ActualFile.java)

---

## Implementation Steps

### Phase 1: [Phase Name]
[Steps...]

---

## Potential Issues
[List of things to watch for...]
```

---

## Required Tags

### Feature Type:
- `#new-feature` - Brand new functionality
- `#enhancement` - Improvement to existing feature
- `#integration` - Third-party integration

### Importance:
- `#low` - Nice to have
- `#medium` - Should implement soon
- `#high` - Important for product
- `#critical` - Essential feature

**Example:** `#high #new-feature`

---

## Link Requirements

- Link to related `docs/Code/` files using Obsidian syntax: `[[filename]]`
- Link to related `docs/Docs/` files that describe affected systems
- Reference actual source code files by showing the file path from the project root (not as a clickable link): `src/main/java/com/wpmanager/path/to/ClassName.java` or `src/main/java/com/wpmanager/path/to/ClassName.java:123` (with line number)
- For documentation annotations, use full URLs to official documentation

---

## Feature File Lifecycle

1. **Creation**: Feature file created in `docs/Features/to-do/` directory
2. **In Progress**: Feature moved to `docs/Features/in-progress/` when development begins
3. **Completion**: Feature moved to `docs/Features/done/` when implemented
4. **Documentation Update**: After moving to done, update related code explanations and system docs

---

## Integration with Tasks

For complex features requiring detailed implementation steps:

**In Feature File:**
```markdown
#### 2.1 Create Service Layer
- [ ] **→ See Task:** [[Add-QueryDSL-Support-step-2-1-Service-Layer]]
```

**In Task File:**
```markdown
**Parent:** [[Add QueryDSL Support]]
**Related Step:** Phase 2, Step 2.1