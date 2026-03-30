# Documentation Rules - INDEX

## Overview

The `docs/` directory is an **Obsidian vault** used for organizing project documentation, bugs, features, and code explanations. Humans read these files using Obsidian, which provides features like tags, links, and custom views.

---

## 🚨 CRITICAL RULE: Modification Restrictions

**YOU CANNOT modify any file in the `docs/` directory on your own initiative.**

- ✅ **You CAN**: Suggest updates to files in this directory
- ✅ **You CAN**: Modify files when **explicitly requested** by the user
- ❌ **You CANNOT**: Modify files on your own initiative
- ❌ **You CANNOT**: Move files between directories without explicit request

When you believe a file needs updating, ask: "Would you like me to update [file name] to reflect these changes?"

---

## Directory Structure

```
docs/
├── .obsidian/              # Obsidian configuration (don't modify)
├── Bugs/                   # Bug tracking
│   ├── to-do/             # Bugs awaiting fix
│   ├── in-progress/       # Bugs currently being fixed
│   └── done/              # Resolved bugs
├── Features/               # Feature tracking
│   ├── to-do/             # Features awaiting implementation
│   ├── in-progress/       # Features being developed
│   └── done/              # Completed features
├── Tasks/                  # Detailed task implementation
│   ├── current/           # Tasks being worked on
│   └── done/              # Completed tasks
├── Docs/                   # System and process documentation
└── Code/                   # Code explanation files
```

---

## 📋 Quick Reference: Which Rules to Read

When you need to create or update documentation, read the relevant specific rule file:

| Documentation Task                      | Files to Read                                                                                                                           |
| --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **Create/update bug documentation**     | [`rules/doc-rules.md`](rules/doc-rules.md) (this file) + [`rules/doc-rules/bugs.md`](rules/doc-rules/bugs.md)                           |
| **Create/update feature documentation** | [`rules/doc-rules.md`](rules/doc-rules.md) (this file) + [`rules/doc-rules/features.md`](rules/doc-rules/features.md)                   |
| **Create/update task documentation**    | [`rules/doc-rules.md`](rules/doc-rules.md) (this file) + [`rules/doc-rules/tasks.md`](rules/doc-rules/tasks.md)                         |
| **Create/update code explanations**     | [`rules/doc-rules.md`](rules/doc-rules.md) (this file) + [`rules/doc-rules/code-explanations.md`](rules/doc-rules/code-explanations.md) |
| **Create/update system docs**           | [`rules/doc-rules.md`](rules/doc-rules.md) (this file) + [`rules/doc-rules/system-docs.md`](rules/doc-rules/system-docs.md)             |

**Each specific rule file is self-contained** and includes everything needed to create that documentation type.

---

## Common Elements (Applies to All Documentation Types)

### Obsidian Features

#### Tags

**Format:** `#tag-name`

**Usage:**
- Add tags at the top of the file
- Use kebab-case for multi-word tags: `#code-quality`
- Tags can be inline: Some text with #inline-tag here

**Common Tags:**
- Bug types: `#optimization`, `#architectural`, `#performance`, `#security`
- Importance: `#low`, `#medium`, `#high`, `#critical`
- Status: `#in-progress`, `#blocked`, `#needs-review`
- Feature types: `#new-feature`, `#enhancement`, `#integration`

#### Links

**Internal Links (Obsidian Wiki Links):**
```markdown
[[FileName]]                    # Links to FileName.md
[[FileName|Display Text]]       # Link with custom text
[[Folder/FileName]]             # Link to file in subfolder
```

**External Links (Standard Markdown):**
```markdown
[Display Text](URL)
```

**Source Code References (NOT as links):**
Show the file path from project root:
```markdown
src/main/java/com/wpmanager/path/to/PluginService.java
src/main/java/com/wpmanager/path/to/File.java:163
```

**Embedding:**
```markdown
![[ImageFile.png]]             # Embed image
![[OtherNote]]                 # Embed another note
```

---

## File Management Rules

### Moving Files Between Directories

**YOU CANNOT move files unless explicitly requested.**

When the user requests a file move:
1. Use the `mv` command in terminal (not copy + delete)
2. Update any links in other files that reference the moved file
3. Verify the move was successful

**Example Command:**
```bash
mv docs/Bugs/to-do/bug-name.md docs/Bugs/in-progress/bug-name.md
```

### After Moving a Bug or Feature to "done/"

When a bug or feature is moved to the `done/` directory:

1. **Ask the user:** "Would you like me to update related documentation files?"
2. **If yes, check:**
   - Code explanation files in `docs/Code/` that were affected
   - Process documentation in `docs/Docs/` that was affected
   - Task files in `docs/Tasks/` that were related to this bug/feature
3. **Read the actual source code** to verify changes (don't just trust the bug/feature file)
4. **Update files** to reflect the current state

### After Moving a Task to "done/"

When a task is moved to `docs/Tasks/done/`:

1. **Update the parent bug/feature file** to mark the related step as complete
2. **Ask the user:** "Would you like me to update related code explanation files?"
3. **If yes:**
   - Update code explanation files for any files that were created or modified
   - Update process documentation in `docs/Docs/` if the task affected documented processes

### Using the `tree` Command

You can use the `tree` command to explore the `docs/` directory structure:
```bash
tree docs/
tree docs/Bugs/
tree docs/Code/
```

---

## User Commands Reference

### Command: `update doc [file to update]`

**Purpose:** Update a specific documentation file

**Actions:**
1. Read the current state of the specified file
2. Check all code references in the file
3. Read the actual source code files being referenced
4. Verify everything is up-to-date
5. Identify areas that need modification or updating
6. Update the file accordingly

**Example:**
```
User: update doc docs/Docs/File Upload Process.md
```

### Command: `set bug [bug file] as "status"`

**Purpose:** Move a bug file to a different status directory

**Valid Statuses:** `to-do`, `in-progress`, `done`

**Actions:**
1. Use terminal `mv` command to move file to target directory
2. **If status is "done":**
   - Ask: "Would you like me to update related documentation files?"
   - If yes:
     - Check all related `docs/Docs/` files
     - Check all related `docs/Code/` files
     - Read actual source code to verify changes
     - Update files to reflect current state

**Example:**
```
User: set bug docs/Bugs/in-progress/Idempotency.md as "done"
```

### Command: `update code explanation [actual code file]`

**Purpose:** Create or update a code explanation file

**Supported File Types:** `.java`, `.xml`, `.properties`, etc.

**Actions:**
1. Check if explanation file exists in `docs/Code/`
2. **If doesn't exist:**
   - Create new explanation file following code explanation structure
   - Use proper naming convention (replace `.` with `-`)
3. **If exists:**
   - Read the current explanation file
   - Read the actual source code file
   - Compare them to find differences
   - Update explanation file to match current code

**Example:**
```
User: update code explanation src/main/java/com/wpmanager/models/downloads/plugin/PluginService.java
```

### Command: `create code explanation [actual code file]`

**Purpose:** Same as `update code explanation` but emphasizes creation

**Actions:** Identical to `update code explanation`

---

## Best Practices

### Writing Documentation

1. **Use Clear Language:** Write for humans who may not know the codebase
2. **Be Comprehensive:** Include all relevant details
3. **Use Examples:** Show real code snippets or scenarios
4. **Add Diagrams:** Use Mermaid.js for complex flows
5. **Keep Links Updated:** Ensure all links work and point to correct files
6. **Use Consistent Formatting:** Follow the structures outlined in specific rule files

### Linking

1. **Link Generously:** Connect related documents
2. **Use Descriptive Link Text:** Not "click here", but "PluginService upload method"
3. **Include Line Numbers:** For specific code references: `File.java:123`
4. **Create Bidirectional Links:** If A links to B, consider if B should link to A

### Tags

1. **Be Consistent:** Use established tags
2. **Don't Over-Tag:** 2-4 tags per file is usually sufficient
3. **Use Hierarchy:** Consider `#bug/performance` for sub-categories if needed

### Maintenance

1. **Update After Changes:** When code changes, update related docs
2. **Review Periodically:** Check that documentation matches reality
3. **Archive Completed Items:** Move to `done/` when finished
4. **Clean Up Dead Links:** Fix or remove links to non-existent files

---

## Summary

The `docs/` directory is a critical part of project knowledge management. Follow these rules to maintain high-quality, accurate, and useful documentation:

- ✅ Only modify when explicitly requested
- ✅ Use proper structure for each file type (see specific rule files)
- ✅ Include comprehensive tags and links
- ✅ Write in clear, easy-to-understand language
- ✅ Keep documentation synchronized with code
- ✅ Use Obsidian features effectively
- ❌ Never modify files on your own initiative
- ❌ Never move files without explicit request
- ❌ Never skip required sections in documentation

Following these rules ensures the documentation remains a reliable, maintainable resource for the entire development team.

---

## Documentation File Types Summary

1. **Bugs** (`docs/Bugs/`) - Issues, their impact, and solution architecture → See [`rules/doc-rules/bugs.md`](rules/doc-rules/bugs.md)
2. **Features** (`docs/Features/`) - New functionality and implementation plans → See [`rules/doc-rules/features.md`](rules/doc-rules/features.md)
3. **Tasks** (`docs/Tasks/`) - Detailed implementation for complex bug/feature steps → See [`rules/doc-rules/tasks.md`](rules/doc-rules/tasks.md)
4. **Docs** (`docs/Docs/`) - System workflows and process documentation → See [`rules/doc-rules/system-docs.md`](rules/doc-rules/system-docs.md)
5. **Code** (`docs/Code/`) - Individual source file explanations → See [`rules/doc-rules/code-explanations.md`](rules/doc-rules/code-explanations.md)