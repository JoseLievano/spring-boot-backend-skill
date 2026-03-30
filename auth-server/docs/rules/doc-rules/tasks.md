# Task Documentation Rules

## Purpose

Task files provide detailed implementation documentation for complex steps within bugs or features. They serve as a dedicated workspace for tactical implementation details that would clutter the parent bug/feature's architectural overview.

## 🚨 CRITICAL: Scope Rules

**A task file must ONLY document the specific implementation step it addresses.**

### What to Include (In Scope)
✅ Detailed implementation of the ONE specific step from the parent bug/feature
✅ Method signatures, code examples, and design decisions for THAT step only
✅ Testing considerations specific to THAT step's implementation
✅ Brief mentions of previous steps (as context: "Step X was completed, now we do Y")
✅ Brief mentions of next steps (as forward reference: "This will be used in step Z")

### What to Exclude (Out of Scope)
❌ Implementation details for previous steps (those belong in their own tasks)
❌ Implementation details for subsequent steps (those belong in their own tasks)
❌ Integration/orchestration logic that combines multiple steps (belongs in a later step)
❌ Testing the entire workflow across multiple steps (focus on the current step's tests)
❌ Logging for other steps (only log what the current step does)

### Example of Proper Scope

**Parent Bug Step 2.4:** "Implement compensation logic"
- ✅ **In Scope**: Create `compensateUpload()` method with error handling and logging
- ❌ **Out of Scope**: How to integrate this method into the main upload flow (that's step 2.5)
- ✅ **Allowed Reference**: "This method will be called by `uploadFileToDefaultProvider()` in step 2.5"
- ❌ **Not Allowed**: Detailed code showing how to modify `uploadFileToDefaultProvider()` to call this method

---

## Naming Convention

Task files follow this pattern:
`[Bug-or-Feature-Name]-step-[step-number]-[short-description].md`

**Examples:**
- `Implement-Idempotency-for-Upload-Operations-step-2-1-IdempotencyManager.md`
- `Add-QueryDSL-Support-step-1-2-Configure-Maven-Dependencies.md`
- `Refactor-God-Class-PluginService-step-3-1-Extract-Upload-Logic.md`

---

## Required Structure

```markdown
# Task: [Descriptive Name]

#task #current #complexity-tag #parent-reference-tag

**Parent:** [[Bug or Feature Name]]
**Related Step:** [e.g., "Phase 2, Step 2.1"]
**Estimated Complexity:** Low | Medium | High

---

## Goal

[1-2 sentences: What this task accomplishes and why it's needed]

---

## Context from Parent

[Brief summary of what the parent bug/feature says about this step. Include key requirements, constraints, or architectural decisions from the parent that inform this task.]

---

## Implementation Details

### Approach

[Detailed explanation of the implementation strategy, including:
- Overall architecture/design pattern being used
- How components will interact
- Why this approach was chosen
- Key technical decisions]

### Files to Create/Modify

- [ ] `path/to/File1.java` - [[File1-java]] - Purpose and role in implementation

## Step-by-Step Implementation

### Step 1 : [Step Title]

- [ ] **Done** (change to [x] when complete)
  - [Detailed description of what this step accomplishes]
  - [Key implementation details]
  - [Prerequisites or setup needed]

**Required Imports/Dependencies:** (if applicable)
```java
[List imports or dependencies needed for this step]
```

#### Why This [Step/Method/Component] Is Critical

[Comprehensive explanation including:
- What problem this solves
- Why it's essential to the system
- Consequences of not having this
- How it fits into the larger architecture]

#### [Method/Component] Signature and Purpose (if applicable)

```java
/**
 * [Comprehensive JavaDoc comment explaining:
 * - What the method/component does
 * - Parameters and their purposes
 * - Return values
 * - Exceptions thrown
 * - Usage context]
 */
[method signature or component interface]
```

#### Implementation Logic

##### Phase 1: [Phase Name]
```java
[Code example with inline comments]
```

**Why**: [Detailed explanation of this phase's purpose, design decisions, and rationale]

##### Phase 2: [Phase Name]
[Continue with additional phases as needed...]

#### Complete [Method/Component] Implementation (if applicable)

```java
[Full, complete implementation code showing all phases integrated]
```

#### Edge Cases Handled

1. **Case 1**: [Description] - [How it's handled]
2. **Case 2**: [Description] - [How it's handled]
[Continue for all edge cases...]

#### Transaction Behavior / Performance Characteristics / Thread Safety (as applicable)

[Detailed technical analysis of:
- Transaction boundaries and rollback behavior
- Performance metrics (execution time, memory usage)
- Scalability considerations
- Thread safety guarantees
- Concurrency handling]

#### Relationship to Other [Methods/Components]

[Explain how this integrates with other parts of the system:
- Dependencies (what it requires)
- Dependents (what depends on it)
- Calling sequence
- Integration patterns]

#### Usage Example (if helpful)

```java
[Practical example showing how to use this implementation]
```

### Step 2 : [Next Step Title]

[Repeat the same comprehensive structure for each major implementation step]

### Diagrams

```mermaid
[Sequence diagrams, state diagrams, flowcharts as needed to illustrate:
- Process flows
- State transitions
- Component interactions
- Data flows]
```

**Related Files:**
- [[CodeExplanationFile-java]] - How it relates
- `src/main/java/path/to/ActualFile.java:123` - Why it's relevant

---

## Design Decisions

**Decision 1:** [What was decided]
- **Why:** [Detailed reasoning including:
  - Technical benefits
  - Trade-offs considered
  - Impact on maintainability/performance/scalability]
- **Alternatives considered:** [What other approaches were evaluated and why they were rejected]

**Decision 2:** [Another decision if applicable]
- **Why:** [Reasoning]
- **Alternatives considered:** [Alternatives and why they weren't chosen]

[Include as many decisions as needed to document the design rationale]

---

## Testing Considerations

[Conceptual testing approach - not actual test cases, but testing strategy including:]

**Basic Functionality:**
- [Core behaviors to verify]
- [Expected outcomes]

**Edge Cases:**
- [Boundary conditions]
- [Error scenarios]

**Performance:**
- [Load testing considerations]
- [Scalability verification]

**Concurrency:**
- [Multi-threaded scenarios]
- [Race condition checks]

**Integration:**
- [How to test integration with other components]
- [Dependencies to mock or stub]

---

## Related Code Explanations

- [[RelatedFile-java]] - [Specific relationship and why it matters]
- [[AnotherFile-java]] - [How it connects to this implementation]
- `src/main/java/path/to/File.java:line` - [Relevant implementation detail]

---

## Completion Criteria

This task is complete when:

1. All files created/modified as specified
2. All implementation steps checked off (marked with [x])
3. Code explanation files updated (if new files created)
4. Parent bug/feature step marked complete
5. [Any additional completion requirements specific to this task]
```

---

## Required Tags

### Status Tags:
- `#task` - Identifies it as a task file
- `#current` - Task is being worked on
- `#done` - Task is completed (only when moved to done/ directory)

### Complexity Tags:
- `#low-complexity` - Simple implementation
- `#medium-complexity` - Moderate complexity
- `#high-complexity` - Complex implementation requiring careful design

### Parent Reference Tags:
- Use kebab-case of parent name: `#parent-idempotency`, `#parent-god-class-refactor`

**Example:** `#task #current #high-complexity #parent-idempotency`

---

## Link Requirements

- Link to parent bug/feature using Obsidian syntax: `[[Bug or Feature Name]]`
- Link to related `docs/Code/` files using Obsidian syntax: `[[filename]]`
- Reference actual source code files by showing the file path from the project root: `src/main/java/com/wpmanager/path/to/ClassName.java:123`
- Link to related `docs/Docs/` files if the task affects documented processes

---

## Integration with Parent Bug/Feature

When a task is created for a complex step in a bug or feature:

**In the Parent Bug/Feature File:**
Mark the step with a link to the task:
```markdown
#### 2.1 Create IdempotencyManager Service
- [ ] **→ See Task:** [[Implement-Idempotency-for-Upload-Operations-step-2-1-IdempotencyManager]]
```

**In the Task File:**
Reference the parent:
```markdown
**Parent:** [[Implement Idempotency for Upload Operations]]
**Related Step:** Phase 2, Step 2.1
```

---

## Task Lifecycle

1. **Creation**: Task is created in `docs/Tasks/current/` when user identifies a complex step
2. **Work**: Task is actively being worked on, checkboxes are marked as progress is made
3. **Completion**: When all checkboxes are complete and completion criteria met
4. **Move to Done**: Task is moved to `docs/Tasks/done/` directory
5. **Update Parent**: Mark the related step in parent bug/feature as complete
6. **Update Documentation**: Update affected code explanation files and process documentation
