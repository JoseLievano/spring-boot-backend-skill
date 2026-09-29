# Analysis Doc Conventions

#doc #conventions #architecture

The single format contract for the Phase 1 analysis documentation of the three reference projects
(`backend/`, `BugTracker/`, `wpmanager/`). Every project's Explanation and Review docs follow this
document exactly. Tasks 2, 3 and 4 of [[Features/to-do/Spring-Boot-Skill-Creation]] link here instead of
re-specifying the format.

The rules below are checked mechanically by `scripts/validate-analysis-docs.py` (see the Validation
section). When this document and the validator disagree, fix whichever one is wrong and record the
change in the Changelog at the bottom.

---

## 1. Layout and Naming

```
documentation/Docs/<project>/
├── <project>-Index.md
├── Explanations/
│   └── NN-Title-Words.md
└── Reviews/
    ├── 00-Review-Summary.md
    └── NN-Title-Words-Review.md
```

- `<project>` is the exact reference directory name: `backend`, `BugTracker`, `wpmanager`.
- Two-digit `NN` prefixes set the reading order. Title words are `Kebab-Case-Words` (letters, digits
  and hyphens only).
- Review file names end in `-Review.md`. `00-Review-Summary.md` is the only exception.
- No other `.md` file may live under `Docs/<project>/`. An unlisted file is an unvalidated file.
- Topics that do not exist in a project are **skipped, not stubbed**. Never create an empty doc. Record
  merges and skips in the index.
- **Wiki links always use the full path from `documentation/`**, e.g.
  `[[Docs/backend/Explanations/04-Generic-CRUD-Framework]]`. File names repeat across projects (every
  project has `01-Overview-and-Design-Philosophy`), so short links would resolve ambiguously in Obsidian.
- Inside Markdown tables, escape the alias pipe: `[[Docs/backend/Reviews/01-Security-Review#BE-R01-01\|BE-R01-01]]`.

## 2. Tags

Tags go on the line directly under the `#` title.

- Explanation: `#doc #explanation #ref-<project-kebab>` plus 1–3 area tags.
- Review: `#doc #review #ref-<project-kebab>` plus area tags.
- Review Summary: `#doc #review #review-summary #ref-<project-kebab>`.
- Index: `#doc #index #ref-<project-kebab>`.

`<project-kebab>` values: `backend`, `bugtracker`, `wpmanager`.

Area tags: `#security`, `#persistence`, `#architecture`, `#api-design`, `#testing`, `#configuration`,
`#error-handling`, `#build`.

## 3. Citations

- Source references are plain-text code spans with the path **from the repository root**:
  `` `backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:120` `` or a range
  suffix `:120-135`. A path without a line (`` `backend/pom.xml` ``) is allowed for whole files and
  directories.
- A path containing `*` is treated as a glob and is not checked.
- Every non-trivial claim about behavior carries at least one citation.
- Code excerpts: at most ~30 lines each, preceded by their citation, in a fenced block with a language
  tag (`java`, `xml`, `properties`, `json`, `bash`, `mermaid`).
- Citations inside fenced blocks are validated too. A cited excerpt must point at real lines.
- **Secrets:** never reproduce secret values (passwords, keys, tokens, signing secrets). Write
  `<redacted>` and cite the location. For secrets in git history, cite the commit hash, not the value.
- Obsidian links are for documentation only. Source files are never linked with `[[…]]`.

## 4. Explanation Template

Explanations are **descriptive**. They state what the code does, including where intent and behavior
diverge ("`@PreAuthorize` annotations are present but method security is not enabled"). They do not
argue for fixes; they link the Review finding instead.

Required `##` headings, in this order:

```markdown
# <Title> — <project>

#doc #explanation #ref-<project> #<area>

**Project:** `<project>/` · **Stack:** Spring Boot <x.y.z>, Java <n> · **Index:** [[Docs/<project>/<project>-Index]]

## Summary
<3–6 sentences: what this area is and the one idea a reader must take away>

## Why It Is Built This Way
<design philosophy / intent as far as the code shows it; separate stated intent from inferred intent>

## How It Works
<narrative + at least one Mermaid diagram when there is a flow, sequence, or type hierarchy>

## Key Components
| Component | Path | Responsibility |
|---|---|---|

## Conventions and Rules
<the implicit rules a developer must follow to stay consistent: naming, file sets, annotations, generics, where logic goes>

## How to Replicate
<numbered steps to reproduce this area in a new project the same way, naming every file to create>

## Known Limitations
<neutral statement of where behavior diverges from intent; link finding IDs; write "None identified." if none>

## Related Documents
<full-path wiki links to sibling explanations and reviews>
```

`###` sub-headings are free inside any section.

## 5. Review Template

Reviews are **evaluative**. Every finding has evidence, severity, impact, a recommendation, a
confidence level, and a version-verification line. Reviews also record **Strengths to Keep**, because
Task 4 needs to know what is worth carrying into the skill, not just what is broken.

Required `##` headings, in this order:

```markdown
# <Area> Review — <project>

#doc #review #ref-<project> #<area>

**Project:** `<project>/` · **Stack:** Spring Boot <x.y.z>, Java <n> · **Index:** [[Docs/<project>/<project>-Index]]
**Explained in:** [[Docs/<project>/Explanations/NN-...]]

## Scope
<what was reviewed: packages/files, and what was deliberately excluded>

## Verdict
<3–5 sentences: overall judgment of this area>

## Strengths to Keep
<bullets with citations: what is good and worth carrying into the skill>

## Findings Summary
| ID | Finding | Severity | Category | Confidence |
|---|---|---|---|---|

## Findings

### <ID>
**Title:** <short title>
**Severity:** 🔴 Critical | 🟠 High | 🟡 Medium | 🟢 Low
**Category:** security | correctness | architecture | design | persistence | performance | api-design | error-handling | configuration | testing | hygiene
**Principle:** <SOLID/depth/security principle violated, in solid-deep-design vocabulary; "—" if not applicable>
**Evidence:** <citations + minimal excerpt>
**Impact:** <what goes wrong, for whom, under what conditions>
**Recommendation:** <the better pattern, concrete; code sketch when useful>
**Verified against:** <Context7 library ID + version, javap on an exact jar, or "N/A — no library API involved">
**Confidence:** Confirmed (read in code) | Confirmed (test/runtime evidence) | Needs runtime verification
**Version note:** <optional — when the recommendation needs a newer framework version, say so and name the migration cost>
**Related:** <optional — the same or equivalent finding in another reference project; only link docs that already exist>

## Recommended Target Pattern
<the consolidated "better way" for this area: what a new project should do instead, in 5–15 lines or a short code sketch>

## Related Documents
```

Inside `## Findings`, every `###` heading is a finding heading. A finding's body may use `####`
sub-headings, fenced code and tables. Put each field on its own line (a blank line or a line break
between fields both render acceptably in Obsidian). Extra `##` sections for material that is not a finding
may follow `## Findings` (before `## Recommended Target Pattern`).

## 6. Severity Scale

- 🔴 **Critical** — exploitable security hole, data loss/corruption, or a core feature that does not work.
- 🟠 **High** — likely production incident, significant security weakness, or a design flaw that blocks safe extension.
- 🟡 **Medium** — maintainability or consistency problem with real cost; latent bug under uncommon conditions.
- 🟢 **Low** — hygiene, naming, dead code, style.

Severity reflects the finding **as the code stands**. A dormant bug (in code that nothing calls yet) is
rated for its blast radius once called, and its Impact line says it is dormant.

## 7. Finding IDs

- Format `<PROJ>-R<NN>-<MM>`. `PROJ` is `BE` (backend), `BT` (BugTracker) or `WP` (wpmanager). `NN` is the
  review doc number and `MM` is sequential within that doc. Example: `BE-R01-03`.
- The `###` heading is **exactly the ID** (`### BE-R01-03`). The title goes on the next line as
  `**Title:** Token minting endpoint is anonymous`. Obsidian heading links must match the full heading
  text, so this is the only way `[[…#BE-R01-03|BE-R01-03]]` resolves.
- Findings are linked as `[[Docs/<project>/Reviews/<doc>#<ID>|<ID>]]`.
- IDs are stable once published. Never renumber. If a finding is withdrawn, keep the heading and add
  `**Status:** Withdrawn — <reason>` (a withdrawn finding only needs `**Title:**` and `**Status:**`).
- A finding that spans areas lives in **one** review (where its root cause lives) and is linked from the
  others. Do not duplicate it.
- Every finding ID appears in `00-Review-Summary.md`.

## 8. Review Summary and Index Templates

`Reviews/00-Review-Summary.md` does **not** use the review template. Required `##` headings, in order:

```markdown
# Review Summary — <project>

#doc #review #review-summary #ref-<project>

## Overview
<counts per severity + 3–5 sentence overall verdict>

## Findings by Severity
| ID | Title | Severity | Review |
|---|---|---|---|
<every finding ID in the project, 🔴 → 🟢; each ID is a link [[Docs/<project>/Reviews/<doc>#<ID>\|<ID>]]>

## Strengths to Keep
## Top 5 Recommendations
## Candidate Patterns for the Skill
## Related Documents
```

Projects may add extra `##` sections between the required ones (after `Findings by Severity`). For
example, BugTracker adds `## Defects vs Era Gaps` and wpmanager adds `## Shared with backend`.

`<project>-Index.md` required `##` headings, in order:

```markdown
# <project> — Analysis Index

#doc #index #ref-<project>

## About the Project      ← 2–4 sentences + stack line
## Reading Order          ← numbered list of full-path wiki links
## Explanations           ← every Explanations/ doc, one line each
## Reviews                ← every Reviews/ doc, one line each
## Findings Count         ← table: severity → count
## Merges and Skips       ← topics merged or skipped, with reasons ("None." if none)
```

Every doc under `Docs/<project>/` must be linked from the index.

## 9. Lineage

The three projects share ancestry. BugTracker's generic `DefaultController`/`DefaultServiceImplements`
stack appears, evolved, in wpmanager and then in `backend/`, whose `shared/` package largely mirrors
wpmanager's. Each project's `01-Overview-and-Design-Philosophy.md` has a short **Lineage** paragraph
under `## Why It Is Built This Way` stating what it inherited and what it changed, with citations.

Each project's docs remain **standalone**: never write "see backend's doc" in place of explaining.

## 10. Evidence Discipline

- Explore-agent inventories are leads, not evidence. Read the cited lines before writing a claim.
- The code wins over any plan or inventory. When they disagree, write what the code does.
- Mark a finding `Needs runtime verification` when it depends on framework behavior that was not
  observed (e.g. Spring Data REST auto-exporting repositories). State in the finding exactly what would
  confirm it (request + expected response).
- Do not modify reference projects. Running a project's own test command is allowed only when it writes
  nothing outside `target/`.
- Version verification sources, in order of preference:
  1. Context7 with a version-pinned ID whose snippet source URL carries the matching tag
     (e.g. `/blob/v3.4.1/`).
  2. `javap` on the exact jar in `~/.m2/repository`
     (record as "Verified against: spring-security-core-6.4.2.jar (javap)").
  3. Context7 unpinned or `--research`, labeled "not version-pinned".

  Record which one was used in **Verified against**. Context7 versioned IDs can return snippets from
  `main`; do not claim an exact-version match from those.

## 11. Placeholders

Placeholder text (the words todo, tbd and fixme in capitals, lorem ipsum, an angle-bracket `fill`
marker, or a bracketed ellipsis) is forbidden in prose. Quoting code that contains such text is fine
inside a fenced block or an inline code span (e.g. `//TODO Finish mapper` in backticks).

## Validation

```bash
python3 scripts/validate-analysis-docs.py <project>     # backend | BugTracker | wpmanager
python3 -m unittest discover -s scripts/tests -v        # validator's own tests
```

The validator prints one line per error (`<doc path>: <message>`) and exits 1 on any error, 0 otherwise
(2 on a usage error such as an unknown project). It checks `documentation/Docs/<project>/` only:

| Rule | Section |
|---|---|
| File names and allowed file kinds | 1 |
| Required `##` headings present and in order, per file kind | 4, 5, 8 |
| Citation paths exist; cited lines/ranges are within the file | 3 |
| Wiki links resolve (full path first, unique basename fallback); a `#fragment` matches a heading in the target | 1, 7 |
| No placeholders outside code | 11 |
| Finding headings are exactly `<PROJ>-R<NN>-<MM>`, use the project's code and the file's `NN`, are unique, and carry all required fields | 7 |
| Every finding ID appears in the Review Summary; every doc is linked from the index | 7, 8 |

Wiki links inside fenced blocks and inline code are not checked (templates quote them). The validator
does **not** judge content quality, severity choices or secret leakage. Those stay with the author and
the reviewer; the tasks' secret-leak `grep` covers the last one.

## Changelog

- **2026-09-29** — Created by [[Tasks/current/Spring-Boot-Skill-Creation-step-1-Analyze-backend|Task 1]].
  Additions beyond the Task 1 spec, all enforced by the validator: file-name patterns (section 1),
  escaped-pipe aliases in tables (section 1), withdrawn findings need only `Title` and `Status`
  (section 7), wiki links inside code are not checked, exit code 2 for usage errors, and the severity
  note on dormant bugs (section 6).
  Link resolution is stricter than "basename fallback" for path-style links: `[[Docs/backend/X]]` must
  exist at exactly that path; only bare names (`[[X]]`) fall back to a unique basename.
- **2026-09-29** — [[Tasks/current/Spring-Boot-Skill-Creation-step-3-Analyze-wpmanager|Task 3]]: a review may add
  extra `##` sections after `## Findings` (as the summary already may), for material that is not a finding —
  wpmanager's review 11 adds `## Vault Leads Not Confirmed`. The validator already tolerates extra `##`
  headings; `###` headings inside `## Findings` remain reserved for finding IDs.
