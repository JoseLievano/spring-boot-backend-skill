---
name: glossary-management
description: Manages a ubiquitous language glossary inside the documentation/Glossary/ directory. Use this skill whenever the user wants to add, update, delete, or search terms in the project glossary, or when working with domain-specific language, shared terminology, or ubiquitous language definitions. Also use when the user asks what a term means in the context of this project.
---

# Glossary Management

Manages the project's ubiquitous language glossary — a shared dictionary of terms used by developers and AI agents to communicate clearly and consistently.

## Setup

At the start of any glossary task:

1. Confirm `documentation/Glossary/` exists at the project root. If it doesn't, offer to create it.
2. Discover available categories by listing the files in `documentation/Glossary/` — do NOT assume which categories exist.
3. Load only the category files relevant to the current task to keep token usage low.

## Directory Structure

```
documentation/
└── Glossary/
    ├── Backend.md
    ├── Business.md
    ├── Frontend.md
    └── <any other category>.md
```

Each file represents one category. Categories are created on demand — there is no fixed list.

## Term Format

Every term in every category file follows this exact structure:

```markdown
### <TermName>
**Term:** <TermName>
**Definition:** <A clear, precise definition of what this term means in this project.>
**Examples:** <Concrete examples, code references, or real usages.>
**Related:** <Comma-separated list of related terms, or "None">
```

Keep definitions precise and project-specific — avoid generic dictionary definitions. The goal is to capture what this term means *in this codebase and domain*.

## Commands

### Add a term
`add term <TermName> to <Category>`

1. If the category file doesn't exist, create it with a `# <Category>` heading.
2. Check the file to make sure the term doesn't already exist (to avoid duplicates).
3. Ask the user for: definition, examples, and related terms.
4. Append the new term block at the end of the category file.
5. Confirm: "Added **<TermName>** to `Glossary/<Category>.md`."

### Read / look up a term
`show term <TermName>`

1. Search across all category files in `documentation/Glossary/` for a `### <TermName>` heading.
2. Return the full term block.
3. If not found, say so and offer to add it.

### Update a term
`update term <TermName>`

1. Locate the term across category files.
2. Show the current entry to the user.
3. Ask what they want to change (definition, examples, related terms, or the term name itself).
4. Apply the change and confirm.

### Delete a term
`delete term <TermName>`

1. Locate the term across category files.
2. Show the current entry and ask for confirmation before deleting.
3. Remove the full term block from the file.
4. If the file becomes empty after deletion, offer to delete the category file too.
5. Confirm: "Deleted **<TermName>** from `Glossary/<Category>.md`."

### List terms
`list terms [category?]`

1. If a category is specified, list all `### ` headings in that file.
2. If no category is specified, list all terms across all category files, grouped by category.

### Search terms
`search glossary <query>`

1. Search term names and definitions across all category files for the query string.
2. Return matching term blocks grouped by category.

## Core Rules

- **Always confirm before modifying.** Before writing, moving, or deleting, show the user what you're about to do and ask for confirmation.
- **Discover categories at runtime.** Never hardcode a list of categories — always read what exists in `documentation/Glossary/`.
- **Load lazily.** Only load the category files relevant to the current task. If the task is about backend, don't load frontend or business glossary files.
- **No duplicates.** Before adding a term, check that it doesn't already exist in any category file.
- **Consistent format.** Every term must follow the format above exactly — this ensures agents can parse entries reliably.
