# Bug Documentation Rules

## Purpose

Bug files document issues, their impact, and solutions. They track problems that need to be fixed in the codebase.

---

## Required Structure

### Section 1: Problem Explanation
- Detailed description of the bug
- Why it's important to solve
- Code examples showing where the problem occurs
- Links to related files in `docs/Code/` (if applicable)
- Links to affected processes in `docs/Docs/` (if applicable)
- Explanation of side effects
- How the bug affects related systems or processes

### Section 2: Solution Architecture
- Detailed explanation of the solution approach
- Why this solution is appropriate
- Pros and cons of the solution
- Step-by-step implementation plan
- Steps organized into logical categories
- Each implementation step must include a completion checkbox immediately below the step title (e.g., `- [ ] Step description` for pending, `- [x] Step description` for completed)

---

## Complete Template

```markdown
#critical #reliability

### 🔴 CRITICAL: [Bug Title]
## Problem Explanation

[Detailed description...]

### Real-World Scenarios
[Scenario examples...]

### Impact
[List of impacts...]

---

## Solution Architecture

### Core Components
[Component descriptions...]

---

## Implementation Plan

### Phase 1: [Phase Name]
[Detailed steps...]

### Phase 2: [Phase Name]
[Detailed steps...]

---

## Files to Modify or Create
[List of files...]

---

## Benefits
[List of benefits...]
```

---

## Required Tags

### Bug Type Classification:
- `#optimization` - Performance improvements
- `#architectural` - Design/structure issues
- `#performance` - Speed/efficiency problems
- `#security` - Security vulnerabilities
- `#reliability` - Stability issues
- `#usability` - User experience problems

### Importance Classification:
- `#low` - Minor issues
- `#medium` - Moderate impact
- `#high` - Significant impact
- `#critical` - Must be fixed urgently

**Example:** `#critical #reliability #security`

---

## Link Requirements

- Link to related `docs/Code/` files using Obsidian syntax: `[[filename]]`
- Link to related `docs/Docs/` files that describe affected processes
- Reference actual source code files by showing the file path from the project root (not as a clickable link): `src/main/java/com/wpmanager/path/to/ClassName.java` or `src/main/java/com/wpmanager/path/to/ClassName.java:123` (with line number)
- For documentation annotations, use full URLs to official documentation

---

## Bug File Lifecycle

1. **Creation**: Bug file created in `docs/Bugs/to-do/` directory
2. **In Progress**: Bug moved to `docs/Bugs/in-progress/` when work begins
3. **Completion**: Bug moved to `docs/Bugs/done/` when fixed
4. **Documentation Update**: After moving to done, update related code explanations and system docs

---

## Integration with Tasks

For complex bugs requiring detailed implementation steps:

**In Bug File:**
```markdown
#### 2.1 Create IdempotencyManager Service
- [ ] **→ See Task:** [[Implement-Idempotency-for-Upload-Operations-step-2-1-IdempotencyManager]]
```

**In Task File:**
```markdown
**Parent:** [[Implement Idempotency for Upload Operations]]
**Related Step:** Phase 2, Step 2.1