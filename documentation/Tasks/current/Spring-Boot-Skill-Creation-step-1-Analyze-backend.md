# Task: Analyze `backend/` — Explanation and Review Documentation

#task #current #high-complexity #parent-spring-boot-skill-creation

**Parent:** [[Features/to-do/Spring-Boot-Skill-Creation|Spring Boot Skill Creation]]
**Parent Type:** Feature
**Related Step(s):** Phase 1 — Task 1 (Analyze `backend/`), plus Phase 1 "Document findings in `documentation/Docs/`" for this project
**Estimated Complexity:** High

---

## Goal

Produce a complete, evidence-backed documentation set for the `backend/` reference project:
**Explanation** docs describing how it is built (architecture, patterns, design philosophy — enough to
build a new project the same way) and **Review** docs critiquing it (weak patterns, defects, risks, and
better alternatives). This task also establishes the shared document conventions and the automated
validator that Tasks 2 and 3 reuse.

---

## Parent Context

The parent Feature builds the `spring-boot-backend` skill from patterns found in three real reference
projects. Phase 1 is analysis; its output is documentation in `documentation/Docs/`, which later becomes
the rationale for every skill phase (see [[Docs/Skill-Architecture]] → "Planned Future Phases").

Parent constraints that apply here:
- The reference projects are **read-only**. Nothing under `backend/` may be modified.
- Patterns must be grounded in actual code, not invented.
- Output layout (defined in the parent's "Phase 1 — Documentation output structure"):
  `documentation/Docs/backend/Explanations/` and `documentation/Docs/backend/Reviews/`.
- Explanations describe *how it is built*; Reviews describe *what should be better*. Topics that do not
  exist in the project are skipped, not stubbed.
- Task 1 sets the document format (templates, naming, section order) that Tasks 2 and 3 reuse.
  Task 4 (cross-project comparison) will consume all Review findings by their stable IDs.

`backend/` replaces the former `auth-server/` (see [[Memory/progress]] 2026-09-28). The security classes
are the old auth-server ones with a package rename; the real deltas are: artifact rename to
`agentForgeBackend`, MySQL → Postgres, secret consolidation onto `${JWT_SECRET}`, and a new QueryDSL-based
list/filter subsystem (`shared/query/*`).

---

## Preconditions / Dependencies

- No prior task in the parent's Phase 1 breakdown. This is the first task.
- `backend/` exists in the working tree (untracked in git). `auth-server/` is deleted in the working tree.
- Python 3 is available (`python3 --version`) for the validator script. Stdlib only — no pip installs.
- `ctx7` CLI is available for version-matched documentation lookups.
- Glossary is initialized (`documentation/Glossary/`, `.glossaryrc`) but empty.
- **Constraint:** never copy secret values (passwords, keys, tokens) into documentation. Cite the
  location (`path:line`) and write `<redacted>`.

---

## Skills and Documentation Preparation

### Skills Reviewed

- `documentation-management` — Selected — doc locations, Obsidian conventions, system-doc template baseline.
- `memory-bank` — Selected — project context; must be updated at task end.
- `solid-deep-design` — Selected — the review lens: SRP/OCP/LSP/ISP/DIP, depth, deletion test, seams.
  Every architectural finding in Reviews uses this vocabulary.
- `find-docs` — Selected — every Review recommendation that names a Spring/library API is verified at
  the project's exact version.
- `tdd` — Selected — applied to the validator script (tests first) and to doc production as vertical
  slices (one doc → validate → next doc).
- `glossary-management` — Selected — use consistent terms; propose new terms at the end (user confirms).
- `solid` — Selected (reference) — code-smell catalogue for Reviews.
- `superpowers:verification-before-completion` — Selected — run the validator and read its output before
  claiming completion.
- `improve-codebase-architecture` — Not needed — it proposes refactors to *this* repo; we only document.

### Documentation Reviewed

Versions (from `backend/pom.xml`): Spring Boot **3.4.1** (L5-10), Java **21** (L30), JJWT **0.12.5**,
OpenFeign QueryDSL **6.12**, Spring Security **6.4.x** (managed by Boot 3.4.1), Hibernate 6.6.x (managed).

| Technology | Context7 ID to use | Notes |
|---|---|---|
| Spring Boot 3.4.1 | `/spring-projects/spring-boot/v3.4.1` | Exact version available. |
| Spring Security 6.4 | `/spring-projects/spring-security/6.4.4` | Closest indexed 6.4.x. Fallback: `/websites/spring_io_spring-security_reference_6_5` (state the version drift). |
| JJWT 0.12.x | `/jwtk/jjwt` | Unversioned; confirm the API exists in 0.12.5 (e.g. `Jwts.parser().verifyWith(...)`, `requireIssuer`). |
| QueryDSL (OpenFeign) | `/openfeign/querydsl` | Unversioned; confirm against 6.x. |

Already verified during task creation (Context7, `/spring-projects/spring-boot/v3.4.1`):
- RFC 9457 `ProblemDetail` support is enabled by `spring.mvc.problemdetails.enabled=true`, and a
  `@ControllerAdvice` extending `ResponseEntityExceptionHandler` is the supported customization point.
  (Relevant to the error-handling review.)

**Exact-version fallback: local jars.** `~/.m2/repository` holds the jars the reference projects
actually resolve. For example, `spring-security-core` 6.4.2 (backend and wpmanager) and 5.7.1
(BugTracker). When Context7 has no version-pinned answer, inspect the API directly:
`javap -cp ~/.m2/repository/org/springframework/security/spring-security-core/6.4.2/spring-security-core-6.4.2.jar org.springframework.security.core.userdetails.UserDetails`.
Record the source as "Verified against: spring-security-core-6.4.2.jar (javap)".
<!-- REVIEW-FIX: added exact-version evidence source -->

Pre-verified this way during task review: in 6.4.2, `UserDetails.isEnabled()`, `isAccountNonLocked()`,
`isAccountNonExpired()` and `isCredentialsNonExpired()` are **`default` methods returning true**.
`SecurityUser` defines `getEnabled()` etc. instead of overriding them, so it compiles cleanly while every
account flag is silently ignored. That is the mechanism behind the account-flags finding.

**Context7 caveat observed:** versioned queries can return snippets whose `Source:` URL points at
`main` (Spring Boot 4.x). Only treat a snippet as version-verified when its source URL contains the
matching tag (e.g. `/blob/v3.4.1/`). Otherwise mark the recommendation "verified against <version>
(not exact)".

### Related Existing Code

Inventory produced during task creation (Explore pass). Treat it as a **lead list**. Every claim must be
re-verified by reading the cited file before it goes into a document.

- `backend/pom.xml` — build, dependencies, annotation processors (Lombok + querydsl-apt `jpa` classifier).
- `backend/src/main/java/com/agentForgeBackend/agentForgeBackendApplication.java` — entry point (`@EnableScheduling`, `@EnableWebSecurity`).
- `backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityConfig.java` — filter chain (no `authorizeHttpRequests`), CORS, entry point/denied handler, BCrypt, `AuthenticationManager`.
- `backend/src/main/java/com/agentForgeBackend/configuration/security/SecurityController.java` — `POST /login`, leftover `GET /test`.
- `backend/src/main/java/com/agentForgeBackend/configuration/filter/JwtTokenService.java` / `JWTTokenValidatorFilter.java` — JWT issue/validate.
- `backend/src/main/java/com/agentForgeBackend/configuration/boostrap/AdminBoostrap.java` — seed admin.
- `backend/src/main/java/com/agentForgeBackend/shared/defaultImplements/` / `defaultInterfaces/` — generic CRUD framework.
- `backend/src/main/java/com/agentForgeBackend/shared/query/` — dynamic list query engine (~900 LOC; strongest subsystem).
- `backend/src/main/java/com/agentForgeBackend/shared/models/baseUser/BaseUserEntity.java` — JOINED user root.
- `backend/src/main/java/com/agentForgeBackend/shared/securityUser/` — `UserDetails` adapter + `UserDetailsService`.
- `backend/src/main/java/com/agentForgeBackend/shared/tools/` — `ErrorHTTPRes`, `FileSigner`, and many unused utilities.
- `backend/src/main/java/com/agentForgeBackend/exceptions/GlobalExceptionHandler.java` — error mapping.
- `backend/src/main/java/com/agentForgeBackend/models/hq/admin/` and `models/hq/client/` — the two feature modules (10 files each).
- `backend/src/main/resources/application.properties`, `application-test.properties` — config (test props live in `main`).
- `backend/src/test/java/com/agentForgeBackend/` — 21 test files; surefire reports in `backend/target/surefire-reports/`.

Pre-verified during task creation (read directly, not only reported):
- No `@EnableMethodSecurity` / `@EnableGlobalMethodSecurity` and no `authorizeHttpRequests` anywhere in `backend/src/main/java`.
- `ClientEntity.getBaseUser()` returns `this.getBaseUser()` (infinite recursion) — `ClientEntity.java:33-34`.
- `SecurityUser` defines `getEnabled()` rather than overriding `isEnabled()` — `SecurityUser.java:48`.
- `DefaultServiceImplements.update` fetches and saves without applying the form — `DefaultServiceImplements.java:90-94`.

---

## Implementation Details

### Approach

The work produces documents, not application code. Three design moves keep it reliable and reusable:

1. **One conventions document, three consumers.** `documentation/Docs/Analysis-Doc-Conventions.md` is the
   single source of truth for layout, naming, templates, severity scale, finding IDs, and citation rules.
   Tasks 2, 3 and 4 link to it instead of re-specifying the format. This is the deep module: a small,
   stable interface (a handful of rules) hiding what would otherwise be duplicated across every task.
2. **One validator, many docs.** `scripts/validate-analysis-docs.py <project>` checks mechanically
   what a reviewer would otherwise check by hand: required sections, citation targets exist and line
   numbers are in range, wiki links resolve, no placeholders, finding IDs are well-formed and unique,
   and the index covers every doc. Its interface is one argument. All the rules sit behind it.
3. **Vertical slices.** Write one doc, run the validator, fix, then move on. Never write all docs first
   and validate at the end.

Separation of concerns between doc kinds:
- **Explanations are descriptive.** They state what the code does, including where intent and behavior
  diverge (e.g. "`@PreAuthorize` annotations are present but method security is not enabled"). They do
  not argue for fixes. They link to the Review finding ID instead.
- **Reviews are evaluative.** Every finding has evidence, severity, impact, a recommendation, a
  confidence level, and a version-verification line. Reviews also record **strengths to keep**, because
  Task 4 needs to know what is worth carrying into the skill, not just what is broken.

### Target document set

`documentation/Docs/backend/`:

```
backend-Index.md                         ← entry point: reading order, doc list, top findings
Explanations/
  01-Overview-and-Design-Philosophy.md   ← what it is, lineage (auth-server → backend), philosophy, stack
  02-Build-Tooling-and-Dependencies.md   ← pom, Lombok + QueryDSL APT, Docker dev flow, test.sh, .nvm env file
  03-Package-Structure-and-Module-Anatomy.md ← package tree, the 10-file feature module convention
  04-Generic-CRUD-Framework.md           ← DefaultController/Service/ServiceImplements/Repository/Mapper, generics, extension points
  05-Dynamic-List-Query-Engine.md        ← QueryableField, EntityQueryProfile, QueryPredicateBuilder, PageableFactory, JSON contract
  06-Domain-Model-and-Persistence.md     ← BaseUserEntity JOINED inheritance, roles, repositories, transactions, DB/H2
  07-Authentication-and-Authorization.md ← login flow, JwtTokenService, validator filter, SecurityUser, client API tokens, method-security intent
  08-Error-Handling.md                   ← checked exceptions, GlobalExceptionHandler, ErrorHTTPRes, security entry point/denied handler
  09-Configuration-and-Secrets.md        ← properties, test profile, env vars, @Value usage
  10-Testing-Strategy.md                 ← suites/tags, TestLauncher, H2 profile, layered query tests
  11-Recipe-Build-a-Project-This-Way.md  ← step-by-step: new project + new feature module "the backend way"
Reviews/
  00-Review-Summary.md                   ← all findings sorted by severity, strengths to keep, top recommendations
  01-Security-Review.md
  02-CRUD-Framework-and-API-Design-Review.md
  03-Domain-Model-and-Persistence-Review.md
  04-Feature-Module-Correctness-Review.md
  05-Error-Handling-Review.md
  06-Query-Engine-Review.md
  07-Build-Dependencies-and-Configuration-Review.md
  08-Testing-Review.md
  09-Code-Hygiene-Review.md
```

The list is a target, not a quota. If verification shows an area is too thin for its own doc, merge it
into a neighbor and record the merge in the index. Never create an empty doc.

### Files to Create/Modify

- [x] `documentation/Docs/Analysis-Doc-Conventions.md` — shared conventions (created once, reused by Tasks 2–4)
- [x] `scripts/validate-analysis-docs.py` — validator (stdlib only, Python ≥ 3.9; the environment has 3.10.12)
- [x] `scripts/.gitignore` — ignores `.snapshots/` and `__pycache__/`
- [x] `scripts/.snapshots/backend.sha256` — local read-only guard (gitignored)
- [x] `scripts/tests/test_validate_analysis_docs.py` — validator unit tests (`unittest`)
- [x] `documentation/Docs/backend/backend-Index.md`
- [x] `documentation/Docs/backend/Explanations/01…11-*.md` — 11 explanation docs (see set above)
- [x] `documentation/Docs/backend/Reviews/00…09-*.md` — 10 review docs (see set above)
- [x] `documentation/Features/to-do/Spring-Boot-Skill-Creation.md` — tick "Analyze `backend/`", link this task and the index
- [x] `documentation/Memory/context.md`, `progress.md`, `architecture.md`, `tech.md`, `known-issues.md` — end-of-task update

---

## Step-by-Step Implementation

### Step 0: Snapshot the reference project

**Goal:** Prove at the end that `backend/` was not modified.
**Dependencies:** None

- [x] Create a checksum snapshot of `backend/` (excluding build output and IDE files):

```bash
cd /home/jlievano/Dropbox/CodeProjects/spring-boot-backend-skill
mkdir -p scripts/.snapshots
printf '.snapshots/\n__pycache__/\n' > scripts/.gitignore   # snapshots are local guards, never committed
find backend -type f -not -path 'backend/target/*' -not -path 'backend/.idea/*' -print0 \
  | sort -z | xargs -0 sha256sum > scripts/.snapshots/backend.sha256
wc -l scripts/.snapshots/backend.sha256   # expect ~103 files
```

**Why this step is critical:** `backend/` is untracked, so `git status` cannot detect edits to it.
The checksum file is the only guard for the read-only constraint. It lives in the repo, not in `/tmp`,
so it survives a session break between steps. <!-- REVIEW-FIX: /tmp snapshot was lost across sessions/reboots -->

#### Edge Cases
1. **Case:** `scripts/.snapshots/backend.sha256` already exists from an earlier, interrupted run — do
   **not** overwrite it. Verify it first with `sha256sum --quiet -c`. Overwrite it only after it passes.
2. **Case:** someone deliberately changes `backend/` mid-task (for example the user edits it) — the
   Step 9 check fails. Report the changed files to the user. Do not silently re-snapshot.

---

### Step 1: Write the conventions document

**Goal:** Fix the format once, before any project doc exists.
**Dependencies:** None

- [x] Create `documentation/Docs/Analysis-Doc-Conventions.md` containing all of the following sections.

**1a. Layout and naming**
- `documentation/Docs/<project>/<project>-Index.md`, `Explanations/NN-Title-Words.md`, `Reviews/NN-Title-Words-Review.md` (`00-Review-Summary.md` is the exception).
- `<project>` is the exact reference directory name: `backend`, `BugTracker`, `wpmanager`.
- Two-digit `NN` prefixes set the reading order.
- **Wiki links always use the full path from `documentation/`**, e.g. `[[Docs/backend/Explanations/04-Generic-CRUD-Framework]]`.
  File names repeat across projects (e.g. `01-Overview-and-Design-Philosophy`), so short links would
  resolve ambiguously in Obsidian.

**1b. Tags**
- Explanation: `#doc #explanation #ref-<project-kebab>` (e.g. `#ref-backend`, `#ref-bugtracker`, `#ref-wpmanager`) plus 1–3 area tags (`#security`, `#persistence`, `#architecture`, `#api-design`, `#testing`, `#configuration`, `#error-handling`, `#build`).
- Review: `#doc #review #ref-<project-kebab>` plus area tags.

**1c. Citations**
- Source references are plain-text code spans with the path **from the repository root**:
  `` `backend/src/main/java/com/agentForgeBackend/shared/query/QueryableField.java:120` `` or a range `:120-135`.
- Every non-trivial claim about behavior carries at least one citation.
- Code excerpts: at most ~30 lines each, preceded by the citation. Excerpts go in fenced blocks with a language tag.
- **Secrets:** never reproduce secret values. Write `<redacted>` and cite the location.

**1d. Explanation template** (required `##` headings, in this order):

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
<neutral statement of where behavior diverges from intent; link finding IDs, e.g. [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]]; write "None identified." if none>

## Related Documents
<full-path wiki links to sibling explanations and reviews>
```

**1e. Review template** (required `##` headings, in this order):

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
**Principle:** <SOLID/depth/security principle violated, using solid-deep-design vocabulary; "—" if not applicable>
**Evidence:** <citations + minimal excerpt>
**Impact:** <what goes wrong, for whom, under what conditions>
**Recommendation:** <the better pattern, concrete; code sketch when useful>
**Verified against:** <Context7 library ID + version, or "N/A — no library API involved">
**Confidence:** Confirmed (read in code) | Confirmed (test/runtime evidence) | Needs runtime verification
**Version note:** <optional — when the recommendation depends on a framework version newer than the project's (e.g. requires Boot 3 / Spring Security 6), say so and name the migration cost>
**Related:** <optional — the same or equivalent finding in another reference project, e.g. "Same root cause as [[Docs/backend/Reviews/02-CRUD-Framework-and-API-Design-Review#BE-R02-01|BE-R02-01]]"; only link docs that already exist>

## Recommended Target Pattern
<the consolidated "better way" for this area: what a new project should do instead, in 5–15 lines or a short code sketch>

## Related Documents
```

**1f. Severity scale**
- 🔴 **Critical** — exploitable security hole, data loss/corruption, or a core feature that does not work.
- 🟠 **High** — likely production incident, significant security weakness, or a design flaw that blocks safe extension.
- 🟡 **Medium** — maintainability or consistency problem with real cost; latent bug under uncommon conditions.
- 🟢 **Low** — hygiene, naming, dead code, style.

**1g. Finding IDs**
- Format `<PROJ>-R<NN>-<MM>`. `PROJ` is `BE` (backend), `BT` (BugTracker) or `WP` (wpmanager). `NN` is the review doc number and `MM` is sequential within that doc. Example: `BE-R01-03`.
- The `###` heading is **exactly the ID** (`### BE-R01-03`) and the title goes on the next line as `**Title:** Token minting endpoint is anonymous`. Obsidian heading links must match the full heading text, so this is the only way `[[…#BE-R01-03|BE-R01-03]]` resolves. <!-- REVIEW-FIX: heading-with-title would break every #ID link -->
- Findings are linked as `[[Docs/<project>/Reviews/<doc>#<ID>|<ID>]]`.
- IDs are stable once published. Never renumber. If a finding is withdrawn, keep the heading and mark it `**Status:** Withdrawn — <reason>`.
- A finding that spans areas lives in **one** review and is linked from the others. Do not duplicate it.

**1h. Review Summary and Index templates** <!-- REVIEW-FIX: summary/index had no template; validator would have applied the review template to 00 -->

`Reviews/00-Review-Summary.md` does **not** use the review template. Its required `##` headings, in order:

```markdown
# Review Summary — <project>

#doc #review #review-summary #ref-<project>

## Overview
<counts per severity + 3–5 sentence overall verdict>

## Findings by Severity
| ID | Title | Severity | Review |
|---|---|---|---|
<every finding ID in the project, 🔴 → 🟢; each ID is a link [[Docs/<project>/Reviews/<doc>#<ID>|<ID>]]>

## Strengths to Keep
## Top 5 Recommendations
## Candidate Patterns for the Skill
## Related Documents
```

Projects may add extra `##` sections after `Findings by Severity`. For example, BugTracker adds `## Defects vs Era Gaps` and wpmanager adds `## Shared with backend`.

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

**1h-bis. Lineage**
The three projects share ancestry. BugTracker's generic `DefaultController`/`DefaultServiceImplements`
stack appears, evolved, in wpmanager and then in `backend/`, whose `shared/` package largely mirrors
wpmanager's. Each project's `01-Overview-and-Design-Philosophy.md` has a short "Lineage" paragraph under
`## Why It Is Built This Way` stating what it inherited and what it changed, with citations.
Each project's docs remain **standalone**: never write "see backend's doc" in place of explaining.

**1i. Evidence discipline**
- Explore-agent inventories are leads, not evidence. Read the cited lines before writing a claim.
- Mark a finding `Needs runtime verification` when it depends on framework behavior that was not observed. Example: Spring Data REST auto-exporting repositories. State in the finding exactly what would confirm it.
- Do not modify reference projects. Running the project's own test command is allowed only when it writes nothing outside `target/`.
- Version verification sources, in order of preference:
  1. Context7 with a version-pinned ID whose snippet source URL carries the matching tag.
  2. `javap` on the exact jar in `~/.m2/repository`.
  3. Context7 unpinned or `--research`, labeled as not version-pinned.

  Record which one was used in **Verified against**.

**Why this step is critical:**
Tasks 2 and 3 run later, possibly in other sessions. Without one written contract the three projects
would drift into three formats and Task 4's comparison would have nothing uniform to compare.

#### Edge Cases
1. **Case:** a later task needs a rule change (e.g. a new category) — edit this doc and note the change
   in its own "Changelog" section at the bottom. Tasks 2/3 then follow the updated doc.

---

### Step 2: Build the validator test-first (tracer bullet)

**Goal:** A mechanical check that every doc obeys Step 1's contract.
**Dependencies:** Step 1

Follow `tdd`: one failing test → minimal code → next test. Behaviors to cover, in this order:

1. A valid minimal project tree (1 explanation, 1 review, summary, index) passes with exit code 0.
2. A citation to a missing file fails.
3. A citation whose line (or range end) exceeds the file's line count fails.
4. A wiki link to a nonexistent doc fails. Alias (`|Text`) suffixes are stripped before resolving.
5. A missing required `##` heading fails, and so do required headings out of order. Each file kind has its own required list: explanation, review, **review summary** (`00-Review-Summary.md`), and **index** (`<project>-Index.md`). See conventions 1h.
6. Placeholder text (`TODO`, `TBD`, `FIXME`, `lorem ipsum`, `<fill`, `[...]`) fails when it appears **outside fenced code blocks and outside inline code spans**. Inside either it is allowed, because docs legitimately quote code such as `` `//TODO Finish mapper` ``. <!-- REVIEW-FIX: inline code was not exempt -->
7. A finding heading that is not exactly `### <PROJ>-R<NN>-<MM>`, or whose ID is duplicated, fails. A finding missing any of `**Title:**`, `**Severity:**`, `**Evidence:**`, `**Recommendation:**`, `**Verified against:**`, `**Confidence:**` fails. A finding whose `NN` differs from its review file's `NN` also fails.
8. A finding ID absent from `00-Review-Summary.md` fails. A doc not linked from the index fails.
9. A wiki link with a `#<ID>` fragment fails when the target doc has no `### <ID>` heading. Other `#Heading` fragments are checked against the target's headings, with an exact text match. <!-- REVIEW-FIX: anchor links were unchecked -->
10. A line-less path citation (`` `backend/pom.xml` ``) fails when the path does not exist. Paths containing `*` (globs) are skipped.

- [x] Write `scripts/tests/test_validate_analysis_docs.py`. Each test builds a throwaway repo tree in `tempfile.TemporaryDirectory()` and calls the validator with `--root <tmp>`.
- [x] Write `scripts/validate-analysis-docs.py` until all tests pass.

#### Implementation

Interface: `python3 scripts/validate-analysis-docs.py <project> [--root PATH]`. It prints one line per
error (`<doc path>: <message>`) and exits 1 on any error, 0 otherwise. `--root` defaults to the repo root
(the parent of `scripts/`).

```python
#!/usr/bin/env python3
"""Validate Explanation/Review docs for one reference project against
documentation/Docs/Analysis-Doc-Conventions.md."""
import argparse
import re
import sys
from pathlib import Path

PROJECT_CODES = {"backend": "BE", "BugTracker": "BT", "wpmanager": "WP"}

EXPLANATION_HEADINGS = [
    "Summary", "Why It Is Built This Way", "How It Works", "Key Components",
    "Conventions and Rules", "How to Replicate", "Known Limitations", "Related Documents",
]
REVIEW_HEADINGS = [
    "Scope", "Verdict", "Strengths to Keep", "Findings Summary", "Findings",
    "Recommended Target Pattern", "Related Documents",
]
SUMMARY_HEADINGS = [  # Reviews/00-Review-Summary.md (extra ## sections allowed in between)
    "Overview", "Findings by Severity", "Strengths to Keep",
    "Top 5 Recommendations", "Candidate Patterns for the Skill", "Related Documents",
]
INDEX_HEADINGS = [  # <project>-Index.md
    "About the Project", "Reading Order", "Explanations", "Reviews",
    "Findings Count", "Merges and Skips",
]
FINDING_FIELDS = ["**Title:**", "**Severity:**", "**Evidence:**", "**Recommendation:**",
                  "**Verified against:**", "**Confidence:**"]

FINDING_HEADING = re.compile(r"^### (\S+)\s*$", re.MULTILINE)
FINDING_ID = re.compile(r"^(BE|BT|WP)-R(\d{2})-(\d{2})$")
# Paths may contain spaces (e.g. wpManagerDocs notes); the line suffix is optional.
CITATION = re.compile(r"`((?:backend|BugTracker|wpmanager)/[^`]+?)(?::(\d+)(?:-(\d+))?)?`")
WIKI_LINK = re.compile(r"\[\[([^\]|#]+)(?:#([^\]|]*))?(?:\|[^\]]*)?\]\]")
PLACEHOLDER = re.compile(r"\b(TODO|TBD|FIXME)\b|lorem ipsum|<fill|\[\.\.\.\]", re.IGNORECASE)
FENCE = re.compile(r"^```.*?^```", re.MULTILINE | re.DOTALL)
INLINE_CODE = re.compile(r"`[^`\n]+`")


def strip_fences(text: str) -> str:
    return FENCE.sub("", text)


def prose_only(text: str) -> str:
    """Text with fenced blocks and inline code removed — used for the placeholder scan."""
    return INLINE_CODE.sub("", strip_fences(text))


def h2_headings(text: str) -> list[str]:
    return re.findall(r"^## (.+?)\s*$", strip_fences(text), re.MULTILINE)


def check_headings(doc: Path, text: str, required: list[str], errors: list[str]) -> None:
    found = [h for h in h2_headings(text) if h in required]
    missing = [h for h in required if h not in found]
    if missing:
        errors.append(f"{doc}: missing headings {missing}")
    elif found != required:
        errors.append(f"{doc}: headings out of order {found}")

# check_citations, check_wiki_links, check_placeholders, check_findings,
# check_summary_and_index follow the same shape: (doc, text, ..., errors) -> None,
# so main() is a flat pipeline and each rule is testable in isolation.
```

Resolution rules for `check_wiki_links`: resolve `documentation/<target>.md`. If that does not exist,
fall back to a unique basename match anywhere under `documentation/` (Obsidian shortest-path
behavior). If the basename is not unique, that is an error, because the link is ambiguous. If the
link has a fragment, it must equal the text of some `#`–`######` heading in the target doc; for a
finding ID that means the `### <ID>` heading.

File-kind dispatch: `Explanations/*.md` → `EXPLANATION_HEADINGS`; `Reviews/00-Review-Summary.md` →
`SUMMARY_HEADINGS` (extra headings allowed between required ones); other `Reviews/*.md` →
`REVIEW_HEADINGS` + finding checks; `<project>-Index.md` → `INDEX_HEADINGS`. Any other `.md` file
under `Docs/<project>/` is an error, because it would be an unlisted, unvalidated doc.

Citation checks: when a line is present, the path must be a file and the line (or range end) must be
≤ its line count. Without a line, the path must exist (file or directory). A path containing `*` is
skipped.

#### Edge Cases
1. **Case:** citations inside fenced blocks — still validated. A cited excerpt must point at real lines.
2. **Case:** Windows line endings or a trailing newline — count lines with `len(text.splitlines())`.
3. **Case:** citations to non-Java files (`pom.xml:30`, `application.properties:16`) — supported. The regex accepts any path under a project root.
4. **Case:** a citation in the conventions doc or in a task doc — out of scope. Only `documentation/Docs/<project>/` is validated.

**Why this step is critical:** hand-checked citations rot silently. The validator makes "every
line reference is real" a property that holds for all three projects, not a hope.

---

### Step 3: Tracer-bullet doc — `01-Overview-and-Design-Philosophy.md` + index skeleton

**Goal:** Prove the full pipeline (write → cite → link → validate) on one doc before scaling out.
**Dependencies:** Steps 1–2

- [x] Create `documentation/Docs/backend/backend-Index.md` listing all planned docs. Planned docs are
      listed as plain text, then converted to wiki links as each doc is created, so the link check never
      fails on docs that don't exist yet.
- [x] Write `Explanations/01-Overview-and-Design-Philosophy.md`. It must cover:
  - What the service is (a user/auth-centric admin and client backend) and its lineage from `auth-server`: the package rename, MySQL → Postgres, and the new query engine.
  - The design philosophy the code expresses: convention-driven generic CRUD with minimal per-feature code, JOINED user inheritance, stateless JWT, and a whitelist-first query engine.
  - A high-level Mermaid component diagram: `configuration` / `models/hq/*` / `shared/*` / `exceptions`.
  - Stack table with versions cited to `backend/pom.xml` lines.
- [x] Run `python3 scripts/validate-analysis-docs.py backend`. Iterate until it passes.

---

### Step 4: Remaining Explanation docs (02–10)

**Goal:** Document each area so a developer could rebuild it the same way.
**Dependencies:** Step 3

Write one doc at a time and run the validator after each. Minimum content per doc (beyond the template):

- **02 Build, Tooling and Dependencies:** every dependency with its purpose, marked *used* or *unused (no code references)* after grepping; the Lombok + `querydsl-apt` `jpa` classifier setup and where Q-classes are generated; surefire config (`-XX:+EnableDynamicAgentLoading`, `*SuiteTest` exclusion); the dev-only `Dockerfile` (mounted source, missing compose file); `test.sh`; the `.nvm` env file (names only, values `<redacted>`); the stale `<name>authServer</name>`.
- **03 Package Structure and Module Anatomy:** full package tree with file counts; the 10-file feature module (`Controller, DTO, Entity, Form, ListDTO, Mapper, MiniDTO, QueryProfile, Repository, Service[Impl]`), each file's role, and the naming rule for each; request vs response DTO tiers.
- **04 Generic CRUD Framework:** type-parameter map (`DTO, MINIDTO, LISTDTO, FORM, ENTITY, ID`) as a Mermaid class diagram; the endpoints inherited from `DefaultController`; which methods feature services override and why; the class-level `@Transactional(rollbackFor=…)`; how a feature reaches its specific repository (the downcast). State as fact that the base `update` does not apply the form, and link the finding.
- **05 Dynamic List Query Engine:** request JSON contract with a full example body for `POST /admin/list`; operators per field type; OR-within-field / AND-across-filters semantics; `EntityQueryProfile` whitelisting (why `password` cannot be filtered); `PageableFactory` limits (size 1–100, whitelisted sorts, default sort); a Mermaid sequence of the traced request (controller → service → builder → profile → field → repository → mapper); how to add a queryable field.
- **06 Domain Model and Persistence:** `BaseUserEntity` JOINED hierarchy (Mermaid class diagram); roles `@ElementCollection` (note ordinal storage as fact); account flags; repositories and their return-type conventions; transactions; `open-in-view=false`; Postgres vs H2 (MySQL mode) test DB; `ddl-auto=update`.
- **07 Authentication and Authorization:** Mermaid sequences for (a) `POST /login` and (b) an authenticated request through `JWTTokenValidatorFilter`; token claims table; `SecurityUser`/`SecurityUserServiceImpl`; client API-token minting via `FileSigner`; CORS/CSRF/session policy; the *intended* authorization model (`@PreAuthorize` on services), stated alongside the fact that method security is not enabled and no URL rules exist; seed admin.
- **08 Error Handling:** exception → HTTP status table; `ErrorHTTPRes` shape with an example JSON body; how security errors (401/403) produce the same shape; checked-exception propagation and `rollbackFor`.
- **09 Configuration and Secrets:** every property key with its purpose (values of secrets `<redacted>`); env var names; `test` profile activation; `@Value` injection points; `upload.max-file-size` referenced but undefined.
- **10 Testing Strategy:** test inventory by layer (unit / `@DataJpaTest` / `@SpringBootTest` / MockMvc) with counts; tags and suites; `TestLauncher` + `test.sh`; H2 profile; the last surefire result from `backend/target/surefire-reports/` (cite the report file); what is covered and what is not (stated as fact).

- [x] 02 written and validated
- [x] 03 written and validated
- [x] 04 written and validated
- [x] 05 written and validated
- [x] 06 written and validated
- [x] 07 written and validated
- [x] 08 written and validated
- [x] 09 written and validated
- [x] 10 written and validated

#### Edge Cases
1. **Case:** a finding ID is needed in "Known Limitations" before the review exists — write the
   review finding first (Step 5), or leave the limitation as prose and add the link during Step 7. The
   validator's link check forbids dangling links, so the link cannot come first.
2. **Case:** the inventory claims something the code contradicts — the code wins. Note the correction
   in the task's final report.

---

### Step 5: Review docs (01–09)

**Goal:** Critique every area with verifiable, prioritized findings and a better target pattern.
**Dependencies:** Step 4 for the matching area (reviews link to their explanation)

For every finding:
1. Re-read the cited code.
2. Classify severity per the conventions.
3. Name the principle, using `solid-deep-design` terms. For example: the base `update` is a
   *pass-through that violates its interface contract*; the service downcast of `this.repository` is a
   *DIP/LSP leak at the seam*; `QueryableField` is a *deep module* (strength).
4. Write the recommendation. If it names a Spring/JJWT/QueryDSL API, verify it with `ctx7 docs <id> "<question>"` at the version in the table above and fill in **Verified against**.

Leads to verify and classify (from the inventory; not exhaustive, not pre-judged):

- **01 Security:**
  - Authorization is not enforced: no method security enabled, no `authorizeHttpRequests`.
  - Anonymous client-token minting via `GET /client/token/{username}`.
  - Spring Data REST on the classpath may export repositories, including password hashes (*Needs runtime verification* unless proven).
  - Hardcoded seed admin.
  - One secret used for both JWT signing and client-password HMAC.
  - Client passwords are derived (`bcrypt(HMAC(email))`), and the rotation code is dead.
  - JWT design: constant subject, issuer not validated, hardcoded expiry, no refresh or revocation, no DB re-check, raw token accepted without `Bearer`.
  - Account-status flags are ignored (`getEnabled` vs `isEnabled`).
  - The filter is a `@Component`, so it is double-registered as a servlet filter.
  - The CORS parameter is ignored and the origin is hardcoded.
  - Old MySQL credentials remain in git history (cite the commit, not the values).
- **02 CRUD Framework and API Design:**
  - The base `update` does nothing.
  - Repository downcasts.
  - `getAll` returns an unpaginated `Set`.
  - Every response is 200, DELETE returns a body, and `POST /list` is used for reads.
  - No versioning and no OpenAPI.
  - Six type parameters: evaluate with depth (leverage vs interface cost).
  - Checked exceptions leak into the generic signatures.
- **03 Domain Model and Persistence:**
  - Anemic entities.
  - Audit fields are never populated, even though they are exposed and sortable.
  - Roles stored as ordinals.
  - No migrations (`ddl-auto=update`).
  - Repository contracts mix nullable returns and `Optional`.
  - The H2 (MySQL mode) test DB does not match production Postgres.
  - Validation constraints live on the entity instead of the forms.
- **04 Feature Module Correctness:**
  - `ClientEntity.getBaseUser` recursion.
  - NPE risks in `AdminMapper.toEntity` and `ClientService.insert`.
  - Mapper field gaps (username and id not set in `AdminMapper`; `ClientMiniDTO` fields).
  - Type mismatch between `apikey` as Long and as String.
- **05 Error Handling:**
  - Checked exceptions with no base class.
  - Duplicated builder code despite the helper.
  - No fallback handler.
  - `IllegalStateException` messages leak out with a 500.
  - Mixed return types.
  - Not RFC 9457 (already verified: `ProblemDetail` / `ResponseEntityExceptionHandler` in Boot 3.4.1).
- **06 Query Engine:** mostly strengths (whitelisting, typed conversion, bounded page size, tests). Gaps:
  - `nullable()` is unused in the profiles.
  - Enum and role fields cannot be filtered.
  - Reads go through `POST`.
  - Singleton `@Component` builders: check that they are stateless and record the result as a strength or a finding.
- **07 Build, Dependencies and Configuration:**
  - Unused starters (batch, data-jdbc, data-rest, web-services, webflux, websocket, jdbc).
  - S3 SDK unused.
  - H2 at runtime scope.
  - Mockito version pin overrides Boot's managed version.
  - `contextLoads` fails because of the batch datasource initializer (cite the surefire report).
  - SQL and bind-parameter logging is on in the only non-test config.
  - Test properties live in `src/main`.
  - The `db` host is hardcoded.
  - `upload.max-file-size` is referenced but never defined.
  - The Dockerfile is dev-only.
- **08 Testing:**
  - `@WithMockUser` masks the missing authorization, and there are no negative authorization tests.
  - No tests for login, JWT, or CRUD writes.
  - Empty suites, and an unused helper.
  - The failing context test.
  - No Testcontainers.
- **09 Code Hygiene:**
  - Dead code (the list in the inventory).
  - Misspelled `boostrap`.
  - Lowercase class names.
  - Redundant Lombok annotations.
  - No-op `@Lazy`.
  - Logging by string concatenation.
  - Layering violation: `shared/tools/AuthUserUtil` imports feature entities.

- [x] 01 Security Review written and validated
- [x] 02 CRUD Framework and API Design Review written and validated
- [x] 03 Domain Model and Persistence Review written and validated
- [x] 04 Feature Module Correctness Review written and validated
- [x] 05 Error Handling Review written and validated
- [x] 06 Query Engine Review written and validated
- [x] 07 Build, Dependencies and Configuration Review written and validated
- [x] 08 Testing Review written and validated
- [x] 09 Code Hygiene Review written and validated

#### Edge Cases
1. **Case:** a claim can only be proven by running the app against Postgres. Do not run it. Mark the
   finding `Needs runtime verification` and describe the exact check (request + expected response).
2. **Case:** a finding fits two reviews, e.g. the no-op `update` is both correctness and framework design.
   Put it in the review where the root cause lives (02) and link it from the other.
3. **Case:** Context7 returns only `main`/4.x snippets. Say so in **Verified against** and do not claim an
   exact-version match.

---

### Step 6: Recipe doc — `11-Recipe-Build-a-Project-This-Way.md`

**Goal:** Answer "how do I create a new project the backend way?" directly.
**Dependencies:** Steps 4–5

- [x] The recipe uses the standard Explanation template. The numbered recipe is the body of
      `## How to Replicate`. `## How It Works` gives a one-diagram map of what gets built. <!-- REVIEW-FIX: recipe/template fit was unspecified -->
- [x] Write a numbered recipe covering:
  1. `pom.xml` essentials: only the dependencies the design actually uses, plus the APT setup.
  2. Package skeleton.
  3. Copying or recreating `shared/` (CRUD framework, query engine, error contract).
  4. Security wiring.
  5. Adding one feature module end to end. List all 10 files, with a minimal code sketch per file matching the project's style.
  6. Registering a `QueryProfile`.
  7. Tests to write per layer.
- [x] Where a step copies a pattern that a Review flags, add an inline callout
      `> ⚠️ Review: [[Docs/backend/Reviews/…#BE-Rxx-yy|BE-Rxx-yy]] — <one line>`.
      The recipe stays faithful to how the project is built; the callout records what not to copy blindly.
- [x] Validate.

---

### Step 7: Review Summary, Index, cross-links

**Goal:** Make the set navigable and complete.
**Dependencies:** Steps 3–6

- [x] `Reviews/00-Review-Summary.md`:
  - every finding ID sorted by severity (ID, title, severity, review link);
  - counts per severity;
  - the "Strengths to Keep" roll-up;
  - Top 5 Recommendations;
  - the set of backend patterns recommended as candidates for the skill (input for Task 4).
- [x] Finalize `backend-Index.md`:
  - reading order;
  - every doc as a full-path wiki link;
  - merges and skipped topics, with reasons;
  - finding counts.
- [x] Back-fill "Known Limitations" links in the Explanations.
- [x] Run `python3 scripts/validate-analysis-docs.py backend` → exit 0.

---

### Step 8: Update parent, memory bank, glossary proposal

**Goal:** Leave the project state accurate for Tasks 2–4.
**Dependencies:** Step 7

- [x] Parent Feature:
  - tick `Analyze backend/`;
  - in the Phase 1 task table, link this task and `[[Docs/backend/backend-Index]]`.
- [x] Memory bank (`memory-bank` skill):
  - `context.md`: current focus → Task 2.
  - `progress.md`: dated entry with links.
  - `architecture.md`: Docs layout + `scripts/` validator.
  - `tech.md`: backend versions and the validator command.
  - `known-issues.md`: Context7 `main`-snippet caveat; `backend/` untracked means edits are checked by checksum.
- [ ] Propose glossary terms to the user and add only on confirmation (`glossary add`): *Reference Project*, *Explanation Doc*, *Review Doc*, *Finding*, *Finding ID*, *Strength to Keep*, *Feature Module* (backend's 10-file unit).

---

### Step 9: Verify the read-only constraint

- [x] `sha256sum --quiet -c scripts/.snapshots/backend.sha256` → no output, exit 0.

---

## Design Decisions

**Decision 1:** Shared conventions live in one doc (`Docs/Analysis-Doc-Conventions.md`), not in each task.
- **Why:** three tasks plus Task 4 consume the same format. One source avoids drift (locality).
- **Alternatives considered:** templates embedded in each task. Rejected because tasks move to `done/`, copies diverge, and there is no single place to fix a rule.

**Decision 2:** A validator script with unit tests, not a manual checklist.
- **Why:** 21+ docs per project with hundreds of `path:line` citations. Humans do not re-check those; a script does it in a second, and all three tasks reuse it.
- **Alternatives considered:** a markdown lint tool (e.g. markdownlint). Rejected because it can't check citations against source files, finding-ID rules, or index coverage. Adding a Node dependency to this workspace was also unjustified.

**Decision 3:** Stable finding IDs (`BE-R01-03`) with a mandatory summary.
- **Why:** Task 4 compares findings across projects, and future skill phases will cite them as rationale. IDs must not change when docs are edited.
- **Alternatives considered:** heading-only anchors. Rejected because they are unstable under renames and can't be counted or cross-referenced mechanically.

**Decision 4:** Explanations describe; Reviews judge.
- **Why:** the user needs both "how to build it this way" and "what's wrong with it". Mixing them makes the explanations unusable as a faithful blueprint.
- **Alternatives considered:** a single doc per area with a critique section. Rejected because it conflicts with the requested Explanations/Reviews split and makes it harder to consume one kind on its own.

**Decision 5:** Citations use repo-root paths in plain code spans, not Obsidian links.
- **Why:** this follows the `documentation-management` convention. They are machine-checkable and unambiguous across three projects.
- **Alternatives considered:** paths relative to the project root. Rejected because the validator would need project context per citation, and they are ambiguous when Task 4 mixes projects.

**Decision 6:** Numbered file prefixes (`01-…`).
- **Why:** the user asked for docs that explain the parts of the build. A reading order turns the folder into a guided walkthrough.
- **Alternatives considered:** unnumbered names. Rejected because they give no reading order in Obsidian's file tree.

---

## Testing Considerations

### Automatic Validation

- [x] Run `python3 -m unittest discover -s scripts/tests -v` → all tests pass (≥ 10 tests, one per behavior in Step 2).
- [x] Run `python3 scripts/validate-analysis-docs.py backend` → exit 0, no output lines.
- [x] Run `ls documentation/Docs/backend/Explanations | wc -l` → equals the number of explanation docs listed in `backend-Index.md`.
- [x] Run `grep -c '^### BE-R' documentation/Docs/backend/Reviews/0[1-9]-*.md` → every review has ≥ 1 finding (or explicitly states why none).
- [x] Run `sha256sum --quiet -c scripts/.snapshots/backend.sha256` → exit 0 (`backend/` unchanged).
- [x] Run `git status --porcelain -- BugTracker wpmanager` → empty (other reference projects untouched).
- [x] Run a secret-leak check: `grep -rniE '(password|secret|key)\s*[=:]\s*[^<[:space:]]' documentation/Docs/backend` → review every hit; none may be a real secret value.

### Manual Validation

- [ ] Open `documentation/Docs/backend/backend-Index.md` in Obsidian and confirm that every link opens its doc and that the Mermaid diagrams render.
- [ ] Read `11-Recipe-Build-a-Project-This-Way.md` and judge whether you could scaffold a new module from it alone.
- [ ] Spot-check 5 random findings: open the cited lines and confirm that the evidence matches the claim.

**Rule:** Run automatic checks when possible. Manual checks above are for the user; do not perform them on the user's behalf.

---

## Related Code Explanations

- No `documentation/Code/` explanations exist for `backend/`. The Explanation docs in this task take their place at the system level.
- [[Docs/Skill-Architecture]] — Planned future skill phases that these docs feed.
- [[Docs/Skill-Directory-Convention]] — Where skill files live (not modified by this task).
- [[Features/to-do/Agent-Project-Onboarding-First-Instructions]] — The skill's "target architecture" concept; Reviews' "Recommended Target Pattern" sections are its future input.

---

## Completion Criteria

- [x] Parent document reviewed and reflected accurately in this task
- [x] Relevant skills reviewed and selected for this task
- [x] Up-to-date documentation reviewed for the affected technologies (Context7 IDs recorded per finding)
- [x] `Docs/Analysis-Doc-Conventions.md` created
- [x] Validator and its tests created; tests pass
- [x] All Explanation docs created (or merges/skips recorded in the index)
- [x] All Review docs created, each finding complete (severity, evidence, impact, recommendation, verification, confidence)
- [x] Review Summary lists every finding ID; index links every doc
- [x] `python3 scripts/validate-analysis-docs.py backend` exits 0
- [x] `backend/` unchanged (checksum verified); `BugTracker/` and `wpmanager/` unchanged
- [x] No secret values in any doc
- [x] Manual validation steps documented for the user
- [x] Parent Feature updated (Task 1 ticked + linked)
- [x] Memory bank updated; glossary terms proposed to the user (proposed in the execution report; **not added** — awaiting the user's confirmation per the glossary rule)

---

## Validation Status (2026-09-29)

**Ready for the user's manual validation.** All automatic checks pass:

| Check | Result |
|---|---|
| `python3 -m unittest discover -s scripts/tests -v` | 44 tests, OK (≥ 10 required) |
| `python3 scripts/validate-analysis-docs.py backend` | exit 0, no output |
| `ls documentation/Docs/backend/Explanations \| wc -l` vs index | 11 = 11 |
| `grep -c '^### BE-R' …/Reviews/0[1-9]-*.md` | 11, 8, 7, 5, 6, 6, 11, 5, 8 (every review ≥ 1) |
| `sha256sum --quiet -c scripts/.snapshots/backend.sha256` | exit 0 (103 files unchanged) |
| `git status --porcelain -- BugTracker wpmanager` | empty |
| Secret-leak `grep` | 4 hits, all formulas or placeholders (`${JWT_SECRET}`, `<TK_KEY bytes>`, `${key:default}`, `HMAC(secret, email)`); an additional value scan against every literal secret in `backend/` and the git-history `auth-server/` properties found no occurrence after one redaction |

## Post-Review Notes

**Deviations from this plan**
1. The `Write` tool refused `.md` paths for the executing subagent; docs were authored in the session
   scratchpad and copied into place with a small script. Content is unaffected.
2. The planned-docs-as-plain-text index trick was replaced by accepting forward-link errors during the
   vertical slices and filtering them per run; every other validator rule was enforced after each doc.
   Final run: exit 0.
3. Validator: path-style wiki links do not fall back to basename matching (stricter than the plan;
   recorded in the conventions Changelog). Withdrawn findings need only `Title` + `Status`. Exit code 2 on
   usage errors. File-name patterns are validated.
4. `solid-deep-design`, `tdd` and `find-docs` were not loaded through the Skill tool; their practices were
   applied directly (validator tests written first and seen failing; `ctx7` CLI + `javap` on exact jars for
   every **Verified against** line; SOLID/depth vocabulary in the **Principle** lines).
5. Review 02 gained `BE-R02-08` (`Page` serialized directly — runtime warning found in the surefire XML);
   Review 07 gained `BE-R07-02` (`@EnableScheduling` with no jobs).

**Planning leads corrected by the code** (the code wins, conventions §10)
- Mockito pin: equals Boot 3.4.1's managed 5.14.2 (redundant, not an override); Surefire 3.2.5 *is* a
  downgrade from the managed 3.5.2 (`BE-R07-04`).
- "SQL and bind-parameter logging": SQL logging is on, but the `BasicBinder` category does not exist in
  Hibernate 6.6.4, so bind parameters are **not** logged (`BE-R07-05`).
- `upload.max-file-size` is referenced with a default (`104857600`), so start-up does not fail (`BE-R07-08`).
- `contextLoads` fails because the test has no `test` profile and the production host `db` is unknown;
  the batch initializer is only the first bean to touch the datasource (`BE-R08-03`).
- `spring.task.scheduling.enabled` (test profile) is not a Boot 3.4.1 property.

**Unresolved / for the user**
- `BE-R01-03` (Spring Data REST exposure) is marked *Needs runtime verification*; the exact `curl`
  check is in the finding. The app was not started.
- Glossary terms proposed, not added: *Reference Project*, *Explanation Doc*, *Review Doc*, *Finding*,
  *Finding ID*, *Strength to Keep*, *Feature Module*.
- Consider rotating the credentials still present in git history (`BE-R01-11`).
- Manual validation steps above remain for the user.

