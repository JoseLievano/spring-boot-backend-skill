# Task: Guide Format and Validator — Make Every Rule Addressable and Machine-Checked

#task #done #medium-complexity #parent-spring-boot-architecture-guide-and-base-project

**Parent:** [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project|Spring Boot Architecture Guide, Base Project and Skill]]
**Parent Type:** Feature
**Related Step(s):** Phase 2 — Step 2.1 (Task 2 in the parent's Task Breakdown)
**Estimated Complexity:** Medium

---

## Goal

Fix the format of the Guide and build the tool that enforces it, before the first Guide document is
written: `Guide-Conventions.md`, `Guide-Index.md`, and a `guide` target in the existing validator with its
own unit tests. After this Task, a Guide rule has a stable ID, its evidence is checked to exist, and
`python3 scripts/validate-analysis-docs.py guide` says in one exit code whether the Guide is well formed.

---

## Parent Context

The parent Feature turns the analysis of the three Reference Projects into a **Guide**, a **Base Project**
and a rewritten **Skill**, and proves them with a **Validation Loop**. Phases execute
**1 → 2 → 4 → 5 → 3 → 6**. This Task is the first Task of Phase 2 and the second of 13.

What the parent says about this Task:

- **Step 2.1:** "Write `Docs/Guide/Guide-Conventions.md` and `Guide-Index.md`; extend the validator with
  the `guide` target and unit tests."
- **Reason for grouping:** "Establishes the format and tooling every later Guide task reuses."
- **Section 2 ("Guide format and validator")** fixes the content:
  - layout `Guide-Index.md` + `NN-Title-Words.md`; tags `#doc #guide` plus area tags;
  - six required headings per document: `Purpose`, `Design`, `Rules`, `Differs From the Reference
    Projects`, `Version Notes`, `Related Documents`;
  - the rule block: a `### G<NN>-<MM>` heading with `Rule`, `Why`, `Evidence` and `Differs from
    references`;
  - **evidence-scoped Version Notes:** each entry is `verified on <line>` (citing a Base Project or
    Validation test, a Finding ID or a version-tagged documentation URL) or `not verified — current-docs
    lookup at execution time`; the `not verified` count is reported in `Guide-Index.md`;
  - the validator's `guide` target: heading order, unique Rule IDs, every rule has Rule/Why/Evidence, every
    cited Finding ID and ADR resolves, every Guide document is listed in the index, every Version Note
    carries a marker and every `verified` marker cites something resolvable; unit tests in `scripts/tests/`;
  - contract-review findings use `GC-R<NN>-<MM>` and live in `Docs/Guide/Reviews/`.
- **[[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]]** (Accepted) is the decision record. Its
  ten decision points are this Task's requirements. Point 9: "We will extend the validator with a `guide`
  target that enforces all of this".

What this Task enables:

- **Tasks 3, 4 and 5** write the sixteen Guide documents in this format and run the `guide` target after
  each one.
- **Task 5** writes the traceability matrix and the contract review; both are checked by what this Task
  builds.
- **Task 6** (the Skill) cites Rule IDs; **Task 7** adds the `validation` target, whose rule-coverage
  ledger needs the list of Rule IDs this Task's code collects.

Constraints from the parent and the memory bank that apply here:

- The three reference projects are read-only. The checksum snapshots must still match at the end.
- [[Docs/Analysis-Doc-Conventions]] is "reused … unchanged" (parent, Affected Systems). This Task does not
  edit it.
- The validator is stdlib-only and targets Python ≥ 3.9 (`Memory/tech.md`). No new dependency, no network
  access.
- No secret value in any new file.
- ADRs are immutable. This Task adds detail **below** ADR-016 (grammar the ADR leaves open). It does not
  change any ADR.

### Gaps in the parent that this Task closes

Found by reading the parent against ADR-016 and against what Tasks 3–5 will need on day one. Each one is a
format decision the parent left open; the Design Decisions section gives the reasoning.

| # | Where | Gap | Resolution in this Task |
|---|---|---|---|
| G1 | Section 2 lists the validator's checks; ADR-016 point 9 says the target "enforces all of this" | The list omits the traceability-matrix scope (ADR-016 point 7), the `draft` marker (point 10) and the contract-review family (point 8) | All three are specified and checked (Design Decisions 8, 6 and 9). |
| G2 | Section 2: "every rule has Rule/Why/Evidence"; the rule block and ADR-016 point 4 list four fields | Is `Differs from references` required? | Required, with an explicit `None — …` value (Design Decision 4). **For the user to confirm.** |
| G3 | "the `not verified` count is reported in `Guide-Index.md`" | No format, so nothing can check it | One status line in the index, compared with the computed count (Design Decision 6). |
| G4 | "every `verified` marker cites a resolvable test, finding ID or version-tagged URL" | "Resolvable" is undefined for a URL (the validator is offline), and "a test" has no path rule | A test is an exact path with `/src/test/` under `base-project/` or `documentation/Docs/Validation/`; a URL counts when its path has a version segment; reachability stays with the author (Design Decision 5). |
| G5 | Testing Decisions give the `CITATION` prefix extension to the later `validation` target | Guide documents must cite `base-project/` tests in Version Notes | A guide-only citation pattern; the three project targets keep the old one (Step 2). |
| G6 | ADR-016 point 5: "Renumbering needs a Guide changelog entry" | No changelog exists, and a renumbered ID would stop resolving | The index carries the Guide's changelog; an ID is withdrawn and replaced, never renumbered (Design Decision 7). |
| G7 | The parent links the Feature from many documents | The Feature and its Tasks move between status directories; a link from a validated Guide document would break on the move | Guide documents never link a Feature, Task or Bug; they link ADRs. The validator rejects such a link (Design Decision 11). |

---

## Preconditions / Dependencies

- **Task 1 is done.**
  [[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-1-decisions-adrs-glossary]] produced
  `documentation/ADRs/ADR-001` … `ADR-017` and `ADR-index.md`. ADR 001–016 are `Accepted`; ADR 017 is
  `Proposed`. Citations use `ADR-NNN` (three digits = the filename prefix), so an ADR resolves by the glob
  `documentation/ADRs/ADR-NNN-*.md`. Task 1 kept its ADR checker as a scratch file on purpose and left
  ADR-citation checking to this Task (its Design Decision 10).
- **State of the code after Task 1 (read at creation, 2026-10-04):** Task 1 changed no code.
  `scripts/validate-analysis-docs.py` is 312 lines with three targets (`backend`, `BugTracker`,
  `wpmanager`); `scripts/tests/test_validate_analysis_docs.py` holds 44 tests that drive the CLI with
  `--root` against a temporary tree. `documentation/Docs/Guide/` does not exist. `documentation/Tasks/current/`
  was empty.
- **Tooling baseline (measured 2026-10-04):** the three project targets exit 0; `python3 -m unittest
  discover -s scripts/tests` runs 44 tests, all passing; Python is 3.10.12.
- **Evidence base:** 241 reference findings. 18 are 🔴 and 45 are 🟠. The three security reviews (`01-*`)
  hold 40 findings. The traceability scope (🔴 + 🟠 + every security-review finding) is **78** findings.
  No reference finding is withdrawn.
- **`base-project/` and `documentation/Docs/Validation/` do not exist yet** (Tasks 9 and 8). Until they do,
  a `verified` Version Note can cite only a Finding ID or a version-tagged URL.
- **`rg` is not a binary on the PATH** in this environment (it is a shell function of the agent harness).
  Every command in this Task uses `grep`.
- **`obsidian.use_cli` is `false`** in `documentation/doc-config.json`. Use direct file operations.
- **The working tree had uncommitted changes when this Task was created** (the Task 1 output: `ADRs/`, the
  glossary, the memory bank, the parent Feature, the Bug Report move). They are not part of this Task. Do
  not revert them.
- **The user must be available** for the Manual Validation items (format approval, glossary terms). All
  automatic work can be done first.

---

## Skills and Documentation Preparation

### Skills Reviewed

- `documentation-management` — **Selected** — Task template, document locations, the "only modify files when
  asked" rule.
- `memory-bank` — **Selected** — project context; Step 13 updates the agent-maintained files.
- `glossary-management` — **Selected** — the Guide vocabulary (*Guide*, *Guide Rule*, *Rule ID*, *Version
  Note*, *Finding ID*) is used as defined; Step 12 proposes three new terms through the `glossary` CLI.
- `solid-deep-design` — **Selected** — the validator stays one deep module (one CLI entry point, one error
  format); see Approach.
- `tdd` — **Selected** — seven vertical slices, one test class and its code at a time; tests drive the CLI,
  never internal functions.
- `find-docs` — **Selected** — Python 3.10 documentation for `re`; see below.
- `doc-exploration` — **Selected (at creation)** — 17 ADRs checked, 4 relevant (001, 003, 015, 016).
- `superpowers:test-driven-development`, `superpowers:verification-before-completion` — **Selected** — red
  before green in every slice; read the command output before ticking a criterion.
- `task-reviewer` — **Selected (at creation)** — reviewed this document.
- `superpowers:brainstorming`, `interview-me` — **Not needed** — the format was decided in D16/D17 and
  ADR-016; the open grammar points are listed for the user's confirmation in Manual Validation.
- `skill-creator`, `superpowers:writing-skills` — **Not needed** until Task 6.

### Documentation Reviewed

| Source | What was checked | Result |
|---|---|---|
| Context7 `/websites/python_3_10` (source URLs carry `/3.10/`, so the snippets are version-matched) | `re`: `MULTILINE`, `re.split`, lookbehind assertions | The 3.10 `re` documentation was returned (`re.MULTILINE`: `^` matches at the start of each line; `re.split`; lookbehind assertions). The exact patterns of this Task — the lookbehind `(?<![\w-])` and `re.split(r"^### ", body, flags=re.MULTILINE)` — were proven by running them (next row), not by the documentation alone. |
| Python 3.10.12 on this machine | Every code block of this Task was run | 103 tests pass (44 existing + 59 new). The code uses nothing newer than 3.9: annotations are not evaluated (`from __future__ import annotations`). |
| `curl` on `docs.spring.io`, 2026-10-04 | The shape of a version-tagged documentation URL | `https://docs.spring.io/spring-security/reference/7.0/…` is served as written. `https://docs.spring.io/spring-boot/4.1/reference/…` and `https://docs.spring.io/spring-framework/reference/7.0/…` **redirect to the unversioned address**, because those are the current lines. The versioned form still works and is the one to write (Design Decision 5). |
| [[ADRs/ADR-016-guide-document-format-and-rule-ids\|ADR-016]], [[ADRs/ADR-001-guide-is-the-source-of-truth\|ADR-001]], [[ADRs/ADR-003-version-baseline-as-a-support-policy\|ADR-003]], [[ADRs/ADR-015-validation-loop-protocol-and-exit-gate\|ADR-015]] | The decisions the format must satisfy | ADR-016: ten points (format, IDs, Version Notes, matrix scope, `GC-R` family, `guide` target, `draft`). ADR-001: every rule cites a Finding ID or an ADR. ADR-003: rule text is behavioural; API names live in Version Notes. ADR-015: the ledger and the evidence packs belong to the later `validation` target. |
| [[Docs/Analysis-Doc-Conventions]] | The format the validator already enforces | Sections 1–3, 5–7 and 11 are reused by reference (naming, tags, citations, review template, severity scale, Finding IDs, placeholders). |
| `glossary` CLI | Existing Guide terms | *Guide*, *Guide Rule*, *Rule ID*, *Version Note*, *Base Project*, *Module Contract* exist. *Contract Review*, *Traceability Matrix* and a term for the draft marker do not. |

### Related Existing Code

- `scripts/validate-analysis-docs.py` — the validator this Task extends. Key locations:
  - `:20` `PROJECT_CODES`; `:46` `FINDING_ID`; `:48` `CITATION` (prefixes `backend|BugTracker|wpmanager`);
    `:49` `WIKI_LINK`.
  - `:58-84` text helpers (`strip_fences`, `prose_only`, `h2_headings`, `all_headings`, `section`).
  - `:89-131` `Context` — `__init__` requires a project name from `PROJECT_CODES`.
  - `:136-220` the checks. `check_findings` (`:191`) already takes the finding code from `ctx.code`, so it
    can check `GC-R` findings unchanged.
  - `:225-242` `classify`; `:245-292` `validate`; `:295-308` `main`.
- `scripts/tests/test_validate_analysis_docs.py` — 44 tests; `ValidatorTestCase` (`:178`) is the pattern the
  new test file follows (temporary tree, `write`/`edit` helpers, `assertPasses`, `assertFailsWith`). **Not
  edited by this Task.**
- `documentation/Docs/<project>/Reviews/NN-*-Review.md` — where a reference Finding ID resolves: a heading
  that is exactly the ID.
- `documentation/ADRs/ADR-NNN-*.md` — where an ADR resolves; the status is the word under `### Status`.
- `scripts/.snapshots/*.sha256` — the read-only guard for the reference projects (git-ignored).

---

## Implementation Details

### Approach

**One validator, one more target.** The validator is a deep module: its whole interface is
`<target> [--root PATH]` → one error line per problem → an exit code. This Task adds one accepted value for
`<target>` and nothing else to that interface. Everything new sits behind it.

**The interface is the test surface.** Every test runs the CLI in a subprocess against a throwaway tree,
exactly like the 44 existing tests. No test imports a function of the validator. The refactoring in Step 2
is therefore free: the 44 tests cannot tell.

**Reuse before adding.** Headings, citations, wiki links, placeholders, finding blocks, summary coverage and
index coverage already exist. The `guide` target reuses all of them. New code is only what the Guide adds:
rule blocks, ID resolution, Version Notes, status counts and the traceability scope.

**Two passes.** A Rule ID can be cited before the document that defines it is read. Pass 1 checks each
document by itself and collects every Rule ID and every contract-review Finding ID. Pass 2 resolves the
references.

**Seams.** None is added. There is one way to read a document tree (the filesystem) in production and in
tests, so a port would be a one-adapter seam. The `--root` argument already gives tests their substitute.

**Vertical slices.** Step 2 refactors under the existing tests. Steps 3–9 are seven slices; each adds one
test class, watches it fail, adds the code, and ends with the whole suite green. The order was checked at
Task creation by building the validator as it stands after each step and running all tests: after every
step the only failing tests belong to later steps (table in Testing Considerations).

**What the reader of a Guide document must know** is in `Guide-Conventions.md`; the validator is the
executable form of that document. The two are written to match, and Step 10 runs one against the other.

### Module map after this Task

| Function (in `scripts/validate-analysis-docs.py`) | Responsibility | Step |
|---|---|---|
| `h3_blocks`, `check_summary_coverage`, `check_index_coverage` | Extracted from existing code so both targets share them | 2 |
| `classify_guide`, `check_guide_tags`, `check_stable_links`, `validate_guide`, `main` | The `guide` target, its file kinds, the title and tag line, no links to documents that move | 3 |
| `field_text`, `adr_file`, `check_evidence`, `check_rules` | Rule blocks and what counts as evidence | 4 |
| `finding_exists`, `check_tokens` | Every Finding ID, ADR and Rule ID resolves | 5 |
| `has_version_evidence`, `check_version_notes` | Version Note grammar; returns the `not verified` count | 6 |
| `check_guide_status` | The index reports true counts | 7 |
| (calls only) | Contract reviews: `check_findings` with code `GC`, summary coverage | 8 |
| `traceability_scope`, `check_traceability` | The matrix covers every in-scope finding | 9 |

### Files to Create/Modify

- [x] `scripts/validate-analysis-docs.py` — refactor for a second target (Step 2); add the `guide` target
  (Steps 3–9)
- [x] `scripts/tests/test_validate_guide_docs.py` — **new**; 60 tests in nine classes (the 59 planned plus
  one added at review — see Post-Review Notes)
- [x] `documentation/Docs/Guide/Guide-Conventions.md` — **new**; the format contract (Step 10)
- [x] `documentation/Docs/Guide/Guide-Index.md` — **new**; the front page with sixteen planned documents
  (Step 10)
- [x] `documentation/Glossary/glossary.json`, `documentation/Glossary/Glossary.md` — **through the `glossary`
  CLI only**, and only the terms the user confirms (Step 12)
- [x] `documentation/Memory/tech.md` — the `guide` command in "Analysis-doc tooling"
- [x] `documentation/Memory/architecture.md` — source-map rows for `Docs/Guide/` and the validator
- [x] `documentation/Memory/known-issues.md` — two notes (versioned-URL redirect; no links from the Guide to
  moving documents)
- [x] `documentation/Memory/context.md`, `documentation/Memory/progress.md` — current focus; dated entry
- [x] `documentation/Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project.md` — tick Step 2.1

**Not modified:** `scripts/tests/test_validate_analysis_docs.py`, `documentation/Docs/Analysis-Doc-Conventions.md`,
every ADR, `documentation/Memory/brief.md`, the three reference projects.

Scratch files (session scratchpad, not committed): `mutation_check.py`, the probe tree of Step 11.

---

## Step-by-Step Implementation

### Step 1: Record the baseline

**Goal:** Confirm the starting state before anything changes.
**Dependencies:** None.

- [x] Run the commands below. Expect three `exit=0` lines, `Ran 44 tests` with `OK`, three `unchanged`
  lines, and `No such file or directory` for the Guide directory.
- [x] Run `git status --short` and note the paths that are already modified. They are not this Task's
  changes.
- [x] If a validator or a test fails **before** any edit, stop and tell the user. Do not repair analysis
  documents in this Task.
- [x] **Ask the user now** to confirm the four format points listed under Manual Validation (plain IDs,
  the required `Differs from references` field, the Version Note grammar, the `#draft` marker with the two
  index counts). Each one shapes tests and code in Steps 4–7. If the user is not available, continue with
  the Task's proposals: every point is a small, local change later (Step 10, edge case 5).
  <!-- REVIEW-FIX: the confirmation was only asked after all code was written -->

**Why this step is critical:**
Step 2 refactors code that 44 tests and three document sets depend on. A red baseline would hide a
regression behind an older failure.

#### Implementation

```bash
cd /home/jlievano/Dropbox/CodeProjects/spring-boot-backend-skill
for p in backend BugTracker wpmanager; do python3 scripts/validate-analysis-docs.py "$p" >/dev/null; echo "$p exit=$?"; done
python3 -m unittest discover -s scripts/tests 2>&1 | tail -3
for s in backend bugtracker wpmanager; do sha256sum --quiet -c scripts/.snapshots/$s.sha256 && echo "$s unchanged"; done
ls documentation/Docs/Guide
git status --short
```

#### Edge Cases
1. **Case:** `documentation/Docs/Guide/` already exists (a second run of this Task) — read what is there
   first; continue from the first unchecked step; never overwrite a Guide document.
2. **Case:** a snapshot check fails before any edit — a reference project changed outside this Task. Report
   it; do not regenerate the snapshot.

---

### Step 2: Refactor the validator so a second target can reuse it

**Goal:** Same behaviour, three extracted functions and two optional parameters. No new feature.
**Dependencies:** Step 1.

- [x] Add `h3_blocks` after `section` and make `check_findings` use it.
- [x] Give `Context.__init__` an optional `code` parameter.
- [x] Give `check_citations` an optional `pattern` parameter (default `CITATION`).
- [x] Extract `check_summary_coverage` and `check_index_coverage` out of `validate` and call them from it.
- [x] Run `python3 -m unittest discover -s scripts/tests` after **each** of the four edits — `Ran 44 tests`,
  `OK` every time.
- [x] Run the three project targets — exit 0 three times.

**Why this step is critical:**
The `guide` target needs the finding-block loop, the summary check and the index check that are inlined in
`validate` today, and it needs a `Context` for a directory that is not a reference project. Extracting them
while the 44 tests are green means the new target never forks a copy of existing logic.

#### Implementation

`h3_blocks` — add it directly after `section`:

````python
def h3_blocks(body: str) -> List[Tuple[str, str]]:
    """(heading, content) of every `### ` block inside a section body."""
    blocks = []
    for block in re.split(r"^### ", body, flags=re.MULTILINE)[1:]:
        heading, _, content = block.partition("\n")
        blocks.append((heading.strip(), content))
    return blocks
````

`Context.__init__` — one new parameter; a project target passes nothing, the Guide passes `"GC"`:

```python
    def __init__(self, root: Path, project: str, code: Optional[str] = None):
        self.root = root
        self.project = project
        self.code = code or PROJECT_CODES[project]
        # the remaining lines of __init__ are unchanged
```

`check_citations` — the pattern becomes a parameter; the body changes in one place
(`CITATION.finditer(text)` → `pattern.finditer(text)`):

```python
def check_citations(ctx: Context, doc: Path, text: str, errors: List[str],
                    pattern: re.Pattern[str] = CITATION) -> None:
    for match in pattern.finditer(text):
        # the rest of the body is unchanged
```

`check_findings` — the block loop now comes from `h3_blocks`; every message is unchanged:

````python
def check_findings(ctx: Context, doc: Path, text: str, file_nn: str, seen: Dict[str, Path],
                   errors: List[str]) -> List[str]:
    """Validate every `###` heading inside `## Findings`; return the finding IDs found."""
    ids: List[str] = []
    for fid, content in h3_blocks(section(text, "Findings")):
        match = FINDING_ID.match(fid)
        if not match:
            errors.append(f"{ctx.rel(doc)}: finding heading '### {fid}' must be exactly "
                          f"'### {ctx.code}-R{file_nn}-<MM>' (put the title in **Title:**)")
            continue
        code, nn, _ = match.groups()
        if code != ctx.code:
            errors.append(f"{ctx.rel(doc)}: finding {fid} uses code {code}; project {ctx.project} uses {ctx.code}")
        if nn != file_nn:
            errors.append(f"{ctx.rel(doc)}: finding {fid} has review number R{nn} but lives in review R{file_nn}")
        if fid in seen:
            errors.append(f"{ctx.rel(doc)}: duplicate finding ID {fid} (also in {ctx.rel(seen[fid])})")
        else:
            seen[fid] = doc
        withdrawn = re.search(r"^\*\*Status:\*\*\s*Withdrawn", content, re.MULTILINE)
        required = WITHDRAWN_FIELDS if withdrawn else FINDING_FIELDS
        for field in required:
            if not re.search(rf"^{re.escape(field)}", content, re.MULTILINE):
                errors.append(f"{ctx.rel(doc)}: finding {fid} is missing field {field}")
        ids.append(fid)
    return ids
````

The two coverage checks, moved out of `validate` (place them after `check_findings`):

````python
def check_summary_coverage(ctx: Context, summary_path: Path, seen_ids: Dict[str, Path],
                           errors: List[str]) -> None:
    """Every finding ID found in the reviews must be listed in the review summary."""
    summary_text = ctx.text(summary_path)
    for fid, where in sorted(seen_ids.items()):
        if not re.search(rf"(?<![\w-]){re.escape(fid)}(?![\w-])", summary_text):
            errors.append(f"{ctx.rel(summary_path)}: finding {fid} (from {where.name}) "
                          f"is not listed in {SUMMARY_NAME}")

def check_index_coverage(ctx: Context, index_path: Path, docs: List[Path], errors: List[str]) -> None:
    """Every doc of the set must be linked from the index."""
    linked = set()
    for match in WIKI_LINK.finditer(prose_only(ctx.text(index_path))):
        path, _ = ctx.resolve_wiki(match.group(1))
        if path is not None:
            linked.add(path.resolve())
    for doc in docs:
        if doc != index_path and doc.resolve() not in linked:
            errors.append(f"{ctx.rel(index_path)}: does not link {ctx.rel(doc)}")
````

`validate` — its last two blocks become two calls:

````python
def validate(root: Path, project: str) -> List[str]:
    ctx = Context(root, project)
    errors: List[str] = []
    if not ctx.docs_dir.is_dir():
        return [f"{ctx.rel(ctx.docs_dir)}: doc directory for project '{project}' does not exist"]

    docs = sorted(ctx.docs_dir.rglob("*.md"))
    index_path = ctx.docs_dir / f"{project}-Index.md"
    summary_path = ctx.docs_dir / "Reviews" / SUMMARY_NAME
    if not index_path.is_file():
        errors.append(f"{ctx.rel(index_path)}: index is missing")
    if not summary_path.is_file():
        errors.append(f"{ctx.rel(summary_path)}: review summary is missing")

    seen_ids: Dict[str, Path] = {}
    headings_for = {"index": INDEX_HEADINGS, "explanation": EXPLANATION_HEADINGS,
                    "summary": SUMMARY_HEADINGS, "review": REVIEW_HEADINGS}
    for doc in docs:
        kind, kind_error = classify(ctx, doc)
        if kind_error:
            errors.append(f"{ctx.rel(doc)}: {kind_error}")
            continue
        text = ctx.text(doc)
        check_headings(ctx, doc, text, headings_for[kind], errors)
        check_citations(ctx, doc, text, errors)
        check_wiki_links(ctx, doc, text, errors)
        check_placeholders(ctx, doc, text, errors)
        if kind == "review":
            file_nn = REVIEW_NAME.match(doc.name).group(1)
            check_findings(ctx, doc, text, file_nn, seen_ids, errors)

    if summary_path.is_file():
        check_summary_coverage(ctx, summary_path, seen_ids, errors)
    if index_path.is_file():
        check_index_coverage(ctx, index_path, docs, errors)
    return errors
````

#### Edge Cases
1. **Case:** `re.Pattern[str]` in a signature on Python 3.9 — safe: the file starts with
   `from __future__ import annotations`, so the annotation is never evaluated.
2. **Case:** a message text changes by accident during the extraction — the 44 tests assert on message
   fragments (`"does not link"`, `"is not listed"`, `"finding heading"`); a changed message fails them.
3. **Case:** the temptation to rename the script (it now validates more than analysis docs) — do not. The
   parent, `Memory/tech.md`, [[Docs/Analysis-Doc-Conventions]] and three done Tasks name it.

---

### Step 3: Slice 1 — the `guide` target, its file kinds, headings and tags

**Goal:** `python3 scripts/validate-analysis-docs.py guide` exists, knows which files may live in
`Docs/Guide/`, and checks headings, the title line and the tags.
**Dependencies:** Step 2.

- [x] Create `scripts/tests/test_validate_guide_docs.py` with the header, the fixtures and the base class
  below, plus the three test classes of this slice.
- [x] Run `python3 -m unittest discover -s scripts/tests -p 'test_validate_guide_docs.py'` — **all 14 tests
  fail** (the validator answers exit 2, "unknown project 'guide'"). This is the red state.
- [x] Replace the module docstring, add the Guide constants, `classify_guide`, `check_guide_tags`,
  `check_stable_links`, `validate_guide` (the Step 3 version) and the new `main`.
- [x] Run the same command — 14 tests, `OK`. Run the full suite — `Ran 58 tests`, `OK`.

**Why this step is critical:**
It is the tracer bullet: one path from the command line to an exit code through a real Guide tree. The
fixture written here is a complete, valid Guide document, so `test_g01a` must stay green through every later
slice — each new check is proven not to reject a correct document.

#### Tests

The file header, the fixtures and the base class. The fixture tree has one reference review set (four
findings, one per severity), two ADRs (one Superseded), one Base Project test file, and a Guide with an
index, the conventions and one document:

````python
"""Tests for the `guide` target of scripts/validate-analysis-docs.py.

Each test builds a throwaway repository tree (one reference review set, two ADRs, one Base Project test
file and a minimal Guide) in a temporary directory and runs the validator CLI against it with --root.

Run: python3 -m unittest discover -s scripts/tests -v
"""
from __future__ import annotations

import subprocess
import sys
import tempfile
import textwrap
import unittest
from pathlib import Path

VALIDATOR = Path(__file__).resolve().parents[1] / "validate-analysis-docs.py"
GUIDE = "documentation/Docs/Guide"

GUIDE_DOC = textwrap.dedent("""\
    # CRUD Base and Service Hooks

    #doc #guide #architecture #draft

    **Guide:** [[Docs/Guide/Guide-Index]] · **Conventions:** [[Docs/Guide/Guide-Conventions]]

    ## Purpose
    One base gives every feature its six entry points.

    ## Design
    The base consults the policy once per entry point. The reference shape is `backend/src/App.java:1-3`.

    ## Rules

    ### G04-01
    **Rule:** An update applies every field of the request.
    **Why:** A generic update that changes nothing loses data without an error.
    **Evidence:** BE-R02-01 · ADR-001
    **Differs from references:** The reference update did nothing.

    ### G04-02
    **Rule:** Authorization is declared once per feature.
    **Why:** A forgotten annotation is fail-open. It completes G04-01.
    **Evidence:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]]
    **Differs from references:** None — kept from the reference projects.

    ## Differs From the Reference Projects
    - The update really updates.

    ## Version Notes
    - **verified on 4.1.x** — the entry points carry `@Transactional`.
      Evidence: `base-project/src/test/java/AppTest.java:1-3`
    - **verified on 3.4.x** — account flags default to true. Evidence: BE-R01-01
    - **verified on 4.1.x** — the decoder is built from a public key. Evidence:
      https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/resource-server/jwt.html
    - **not verified** — current-docs lookup at execution time: the name of the validator bean.

    ## Related Documents
    - [[Docs/Guide/Guide-Conventions]]
    """)

INDEX = textwrap.dedent("""\
    # Guide — Index

    #doc #guide #index

    ## About the Guide
    The convention. Format: [[Docs/Guide/Guide-Conventions]].

    ## Documents
    | # | Document | State |
    |---|---|---|
    | 04 | [[Docs/Guide/04-CRUD-Base-and-Service-Hooks\\|CRUD Base and Service Hooks]] | draft |
    | 06 | `06-Query-Engine` | planned |

    ## Status
    - **Draft documents:** 1
    - **Version Notes not verified:** 1

    ## Reviews
    None yet.

    ## Changelog
    - **2026-10-04** — Created.
    """)

CONVENTIONS = textwrap.dedent("""\
    # Guide Conventions

    #doc #guide #conventions

    A Rule ID looks like G06-03 and a contract-review finding like GC-R01-01. Evidence cites a Finding ID
    such as BE-R01-01 or an ADR such as ADR-001.
    """)


def reference_finding(fid: str, severity: str) -> str:
    return textwrap.dedent(f"""\
        ### {fid}
        **Title:** A defect
        **Severity:** {severity}
        **Evidence:** `backend/src/App.java:2`
        **Recommendation:** Fix it.
        **Verified against:** N/A — no library API involved
        **Confidence:** Confirmed (read in code)
        """)


def review(title: str, findings: list[str]) -> str:
    return textwrap.dedent(f"""\
        # {title}

        #doc #review

        ## Scope
        Everything.

        ## Verdict
        Weak.

        ## Strengths to Keep
        - Small.

        ## Findings Summary
        | ID | Finding | Severity | Category | Confidence |
        |---|---|---|---|---|

        ## Findings

        """) + "\n".join(findings) + textwrap.dedent("""
        ## Recommended Target Pattern
        Deny by default.

        ## Related Documents
        - [[Docs/Guide/Guide-Index]]
        """)


def adr(number: str, status: str) -> str:
    return f"#adr #adr-{status.lower()} #architecture\n\n## ADR {number}: A decision\n\n### Status\n{status}\n"


GC_FINDING = textwrap.dedent("""\
    ### GC-R01-01
    **Title:** The update contract does not say what happens to an absent field
    **Severity:** 🟠 High
    **Evidence:** G04-01 in [[Docs/Guide/04-CRUD-Base-and-Service-Hooks#G04-01|G04-01]]
    **Recommendation:** State it.
    **Verified against:** N/A — no library API involved
    **Confidence:** Confirmed (read in the Guide)
    """)

GC_SUMMARY = textwrap.dedent("""\
    # Contract Review Summary

    #doc #review #review-summary #guide

    ## Overview
    One finding.

    ## Findings by Severity
    | ID | Title | Severity | Review |
    |---|---|---|---|
    | GC-R01-01 | Absent field | 🟠 High | 01 |

    ## Related Documents
    - [[Docs/Guide/Guide-Index]]
    """)

MATRIX_DOC = textwrap.dedent("""\
    # Traceability Matrix

    #doc #guide #architecture #draft

    ## Purpose
    Prove that every in-scope reference finding is covered.

    ## Design
    One row per finding.

    ## Traceability Matrix
    | Finding | Severity | Rule(s) | Note |
    |---|---|---|---|
    | BE-R01-01 | 🔴 | G04-02 | |
    | BE-R01-02 | 🟢 | N/A — the convention has no such endpoint | |
    | BE-R02-01 | 🟠 | G04-01, G04-02 | partial — see the note |

    ## Rules
    None — the matrix maps findings to rules stated in other documents.

    ## Differs From the Reference Projects
    Not applicable.

    ## Version Notes
    None.

    ## Related Documents
    - [[Docs/Guide/Guide-Index]]
    """)


class GuideTestCase(unittest.TestCase):
    doc = f"{GUIDE}/04-CRUD-Base-and-Service-Hooks.md"
    idx = f"{GUIDE}/Guide-Index.md"
    conv = f"{GUIDE}/Guide-Conventions.md"
    rev = f"{GUIDE}/Reviews/01-Contract-Review.md"
    summ = f"{GUIDE}/Reviews/00-Review-Summary.md"
    matrix = f"{GUIDE}/16-Traceability-Matrix.md"

    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.root = Path(self._tmp.name)
        self.write("backend/src/App.java", "class App {\n  void run() {}\n}\n")
        self.write("base-project/src/test/java/AppTest.java", "class AppTest {\n  void runs() {}\n}\n")
        self.write("base-project/src/main/java/App.java", "class App {\n}\n")
        self.write("documentation/ADRs/ADR-001-guide-is-the-source-of-truth.md", adr("001", "Accepted"))
        self.write("documentation/ADRs/ADR-002-an-old-decision.md", adr("002", "Superseded"))
        self.write("documentation/Docs/backend/Reviews/01-Security-Review.md", review("Security Review", [
            reference_finding("BE-R01-01", "🔴 Critical"), reference_finding("BE-R01-02", "🟢 Low")]))
        self.write("documentation/Docs/backend/Reviews/02-CRUD-Review.md", review("CRUD Review", [
            reference_finding("BE-R02-01", "🟠 High"), reference_finding("BE-R02-02", "🟡 Medium")]))
        self.write(self.idx, INDEX)
        self.write(self.conv, CONVENTIONS)
        self.write(self.doc, GUIDE_DOC)

    def tearDown(self):
        self._tmp.cleanup()

    # helpers -------------------------------------------------------------
    def reset_fixture(self) -> None:
        """Rebuild a fresh fixture (used between subTests)."""
        self.tearDown()
        self.setUp()

    def write(self, rel: str, text: str) -> None:
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def read(self, rel: str) -> str:
        return (self.root / rel).read_text(encoding="utf-8")

    def edit(self, rel: str, old: str, new: str) -> None:
        text = self.read(rel)
        self.assertIn(old, text, f"fixture edit anchor not found in {rel}: {old!r}")
        self.write(rel, text.replace(old, new, 1))

    def link_in_index(self, target: str) -> None:
        self.edit(self.idx, "None yet.", f"- [[{target}]]\nNone yet.")

    def add_review(self) -> None:
        self.write(self.rev, review("Contract Review", [GC_FINDING]))
        self.write(self.summ, GC_SUMMARY)
        self.link_in_index("Docs/Guide/Reviews/01-Contract-Review")
        self.link_in_index("Docs/Guide/Reviews/00-Review-Summary")

    def add_matrix(self) -> None:
        self.write(self.matrix, MATRIX_DOC)
        self.link_in_index("Docs/Guide/16-Traceability-Matrix")
        self.edit(self.idx, "**Draft documents:** 1", "**Draft documents:** 2")

    def run_validator(self):
        return subprocess.run(
            [sys.executable, str(VALIDATOR), "guide", "--root", str(self.root)],
            capture_output=True, text=True, check=False)

    def assertPasses(self):
        result = self.run_validator()
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertEqual(result.stdout, "")

    def assertFailsWith(self, *fragments: str):
        result = self.run_validator()
        self.assertEqual(result.returncode, 1, "expected failure, got:\n" + result.stdout + result.stderr)
        for fragment in fragments:
            self.assertIn(fragment, result.stdout)
        return result.stdout
````

The file ends with the usual guard; keep these two lines last as later slices add classes above them:

```python
if __name__ == "__main__":
    unittest.main()
```

The three classes of this slice:

````python
class TestGuideTarget(GuideTestCase):
    def test_g01a_valid_minimal_guide_passes(self):
        self.assertPasses()

    def test_g01b_missing_guide_directory_fails(self):
        self._tmp.cleanup()
        self._tmp = tempfile.TemporaryDirectory()
        self.root = Path(self._tmp.name)
        self.assertFailsWith("documentation/Docs/Guide", "does not exist")

    def test_g01c_missing_index_fails(self):
        (self.root / self.idx).unlink()
        self.assertFailsWith("Guide-Index.md", "index is missing")

    def test_g01d_missing_conventions_fails(self):
        (self.root / self.conv).unlink()
        self.edit(self.idx, " Format: [[Docs/Guide/Guide-Conventions]].", "")
        self.edit(self.doc, " · **Conventions:** [[Docs/Guide/Guide-Conventions]]", "")
        self.edit(self.doc, "- [[Docs/Guide/Guide-Conventions]]", "- [[Docs/Guide/Guide-Index]]")
        self.assertFailsWith("Guide-Conventions.md", "conventions document is missing")

    def test_g01e_unknown_target_is_a_usage_error_that_names_guide(self):
        result = subprocess.run([sys.executable, str(VALIDATOR), "nope", "--root", str(self.root)],
                                capture_output=True, text=True, check=False)
        self.assertEqual(result.returncode, 2)
        self.assertIn("guide", result.stderr)


class TestGuideLayout(GuideTestCase):
    def test_g02a_bad_file_name_fails(self):
        self.write(f"{GUIDE}/Query-Engine.md", GUIDE_DOC)
        self.assertFailsWith("Query-Engine.md", "bad file name")

    def test_g02b_file_in_unknown_subdirectory_fails(self):
        self.write(f"{GUIDE}/Notes/01-Idea.md", "# idea\n")
        self.assertFailsWith("Notes/01-Idea.md", "unexpected file")

    def test_g02c_two_documents_with_one_number_fail(self):
        self.write(f"{GUIDE}/04-Another-Topic.md", GUIDE_DOC.replace("G04-01", "G04-11").replace("G04-02", "G04-12"))
        self.link_in_index("Docs/Guide/04-Another-Topic")
        self.assertFailsWith("document number 04 is already used")

    def test_g02d_link_to_a_document_that_moves_fails(self):
        self.write("documentation/Features/to-do/Some-Feature.md", "# A feature\n")
        self.edit(self.doc, "- [[Docs/Guide/Guide-Conventions]]\n",
                  "- [[Docs/Guide/Guide-Conventions]]\n- [[Features/to-do/Some-Feature]]\n")
        self.assertFailsWith("wiki link into Features/", "status directories")


class TestGuideHeadings(GuideTestCase):
    def test_g03a_missing_guide_heading_fails(self):
        self.edit(self.doc, "## Design\n", "## Architecture\n")
        self.assertFailsWith("missing headings", "Design")

    def test_g03b_out_of_order_headings_fail(self):
        text = self.read(self.doc)
        text = text.replace("## Purpose\n", "## TMP\n").replace("## Design\n", "## Purpose\n")
        self.write(self.doc, text.replace("## TMP\n", "## Design\n"))
        self.assertFailsWith("out of order")

    def test_g03c_missing_guide_tag_fails(self):
        self.edit(self.doc, "#doc #guide #architecture #draft", "#doc #architecture")
        self.edit(self.idx, "**Draft documents:** 1", "**Draft documents:** 0")
        self.assertFailsWith("#doc #guide")

    def test_g03d_missing_index_heading_fails(self):
        self.edit(self.idx, "## Changelog\n", "## History\n")
        self.assertFailsWith("Guide-Index.md", "Changelog")

    def test_g03e_extra_heading_is_allowed(self):
        self.edit(self.doc, "## Rules\n", "## Sequence\nA diagram.\n\n## Rules\n")
        self.assertPasses()
````

#### Implementation

The module docstring:

````python
"""Validate the analysis docs of one reference project, or the Guide, against their conventions.

Usage:
    python3 scripts/validate-analysis-docs.py <target> [--root PATH]

<target> is a reference project (backend, BugTracker, wpmanager), checked against
documentation/Docs/Analysis-Doc-Conventions.md, or `guide`, checked against
documentation/Docs/Guide/Guide-Conventions.md.
Prints one line per error (`<doc path>: <message>`). Exit codes: 0 = valid, 1 = errors, 2 = usage error.
Stdlib only, Python >= 3.9.
"""
````

The Guide constants — add them after `ANY_HEADING`. All of them are added now; the later slices only add
functions:

````python
# --- Guide target (documentation/Docs/Guide/, see Guide-Conventions.md) -------------------
GUIDE_TARGET = "guide"
GUIDE_DIR = "Guide"
GUIDE_CODE = "GC"  # contract-review findings: GC-R<NN>-<MM>
GUIDE_INDEX = "Guide-Index.md"
GUIDE_CONVENTIONS = "Guide-Conventions.md"
GUIDE_HEADINGS = [
    "Purpose", "Design", "Rules", "Differs From the Reference Projects",
    "Version Notes", "Related Documents",
]
GUIDE_INDEX_HEADINGS = ["About the Guide", "Documents", "Status", "Reviews", "Changelog"]
GUIDE_SUMMARY_HEADINGS = ["Overview", "Findings by Severity", "Related Documents"]
RULE_FIELDS = ["**Rule:**", "**Why:**", "**Evidence:**", "**Differs from references:**"]
WITHDRAWN_RULE_FIELDS = ["**Rule:**", "**Status:**"]
MATRIX_HEADING = "Traceability Matrix"
SECURITY_REVIEW_NN = "01"  # BE-R01-*, BT-R01-*, WP-R01-*: every finding is in the matrix scope
DRAFT_LABEL = "Draft documents"
NOT_VERIFIED_LABEL = "Version Notes not verified"
RETIRED_ADR_STATUSES = ("Superseded", "Deprecated")
TEST_ROOTS = ("base-project/", "documentation/Docs/Validation/")  # where a `verified` test may live

GUIDE_NAME = re.compile(r"^(\d{2})-[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*\.md$")
RULE_ID = re.compile(r"^G(\d{2})-(\d{2})$")
RULE_TOKEN = re.compile(r"(?<![\w-])G\d{2}-\d{2}(?![\w-])")
FINDING_TOKEN = re.compile(r"(?<![\w-])(BE|BT|WP|GC)-R(\d{2})-(\d{2})(?![\w-])")
ADR_TOKEN = re.compile(r"(?<![\w-])ADR-(\d+)(?![\w-])")
GUIDE_CITATION = re.compile(
    r"`((?:backend|BugTracker|wpmanager|base-project|documentation/Docs/Validation)/[^`]+?)"
    r"(?::(\d+)(?:-(\d+))?)?`")
FIELD_START = re.compile(r"^\*\*[^*\n]+:\*\*", re.MULTILINE)
VERSION_ENTRY = re.compile(r"^- \*\*(?:verified on \d+\.\d+\.x|(not verified))\*\*")
URL = re.compile(r"https?://[^\s<>`\])]+")
VERSION_SEGMENT = re.compile(r"/v?\d+\.\d+(?:\.\d+)?(?:[.-][0-9A-Za-z]+)*(?=/|$)")
MATRIX_ROW = re.compile(r"^\|([^|\n]*)\|(.*)$", re.MULTILINE)
MOVING_LINK = re.compile(r"\[\[\s*(Features|Tasks|Bugs)/")  # documents that change directory with status
````

`classify_guide` — after `classify`:

````python
def classify_guide(ctx: Context, doc: Path) -> Tuple[Optional[str], Optional[str]]:
    """Return (kind, error). Kinds: index, conventions, guide, summary, review."""
    rel_parts = doc.relative_to(ctx.docs_dir).parts
    name = doc.name
    if rel_parts == (GUIDE_INDEX,):
        return "index", None
    if rel_parts == (GUIDE_CONVENTIONS,):
        return "conventions", None
    if len(rel_parts) == 1:
        if GUIDE_NAME.match(name):
            return "guide", None
        return None, f"bad file name (expected NN-Title-Words.md, {GUIDE_INDEX} or {GUIDE_CONVENTIONS})"
    if len(rel_parts) == 2 and rel_parts[0] == "Reviews":
        if name == SUMMARY_NAME:
            return "summary", None
        if REVIEW_NAME.match(name):
            return "review", None
        return None, "bad file name (expected Reviews/NN-Title-Words-Review.md)"
    return None, ("unexpected file: only Guide-Index.md, Guide-Conventions.md, NN-Title-Words.md and "
                  "Reviews/*.md are allowed (see Guide-Conventions)")
````

`check_guide_tags` and `check_stable_links` — start a new banner section `# --- guide checks` after `check_index_coverage`. Every
later slice adds its functions to this section. The final order, top to bottom, is: `finding_exists`,
`adr_file`, `check_guide_tags`, `check_stable_links`, `check_evidence`, `check_rules`, `has_version_evidence`,
`check_version_notes`, `check_tokens`, `traceability_scope`, `check_traceability`, `check_guide_status`.
(The order is for the reader; Python resolves the names when `validate_guide` runs.)

````python
def check_guide_tags(ctx: Context, doc: Path, text: str, errors: List[str]) -> bool:
    """A Guide document starts with a `# ` title and a tag line carrying #doc #guide. Returns True when
    the tag line also carries #draft."""
    lines = [line for line in text.splitlines() if line.strip()]
    tags = lines[1].split() if len(lines) > 1 else []
    if not lines or not lines[0].startswith("# ") or "#doc" not in tags or "#guide" not in tags:
        errors.append(f"{ctx.rel(doc)}: the first line must be a '# ' title and the next line must "
                      f"carry the tags #doc #guide")
    return "#draft" in tags


def check_stable_links(ctx: Context, doc: Path, text: str, errors: List[str]) -> None:
    """Feature, Task and Bug documents move between status directories, so the Guide never links them."""
    for match in MOVING_LINK.finditer(prose_only(text)):
        errors.append(f"{ctx.rel(doc)}: wiki link into {match.group(1)}/ — those documents move between "
                      f"status directories; link the ADR instead")
````

`validate_guide`, **as it stands at the end of this step** (Steps 4–9 add the lines that call the later
checks; `kinds`, `rules`, `seen_ids`, `summary_path` and `not_verified` are declared now and used later):

````python
def validate_guide(root: Path) -> List[str]:
    ctx = Context(root, GUIDE_DIR, GUIDE_CODE)
    errors: List[str] = []
    if not ctx.docs_dir.is_dir():
        return [f"{ctx.rel(ctx.docs_dir)}: the Guide directory does not exist"]

    docs = sorted(ctx.docs_dir.rglob("*.md"))
    index_path = ctx.docs_dir / GUIDE_INDEX
    conventions_path = ctx.docs_dir / GUIDE_CONVENTIONS
    summary_path = ctx.docs_dir / "Reviews" / SUMMARY_NAME
    if not index_path.is_file():
        errors.append(f"{ctx.rel(index_path)}: index is missing")
    if not conventions_path.is_file():
        errors.append(f"{ctx.rel(conventions_path)}: conventions document is missing")

    # Pass 1 — each document by itself; collects every Rule ID and every contract-review Finding ID.
    kinds: Dict[Path, str] = {}
    numbers: Dict[str, Path] = {}
    rules: Dict[str, Tuple[Path, bool]] = {}
    seen_ids: Dict[str, Path] = {}
    drafts = not_verified = 0
    headings_for = {"index": GUIDE_INDEX_HEADINGS, "guide": GUIDE_HEADINGS,
                    "summary": GUIDE_SUMMARY_HEADINGS, "review": REVIEW_HEADINGS}
    for doc in docs:
        kind, kind_error = classify_guide(ctx, doc)
        if kind_error:
            errors.append(f"{ctx.rel(doc)}: {kind_error}")
            continue
        kinds[doc] = kind
        text = ctx.text(doc)
        if kind in headings_for:
            check_headings(ctx, doc, text, headings_for[kind], errors)
        check_citations(ctx, doc, text, errors, GUIDE_CITATION)
        check_wiki_links(ctx, doc, text, errors)
        check_stable_links(ctx, doc, text, errors)
        check_placeholders(ctx, doc, text, errors)
        if kind == "guide":
            file_nn = GUIDE_NAME.match(doc.name).group(1)
            if file_nn in numbers:
                errors.append(f"{ctx.rel(doc)}: document number {file_nn} is already used by "
                              f"{numbers[file_nn].name}")
            numbers.setdefault(file_nn, doc)
            drafts += check_guide_tags(ctx, doc, text, errors)

    return errors
````

`main` — the positional argument becomes a target:

````python
def main(argv: Optional[List[str]] = None) -> int:
    targets = list(PROJECT_CODES) + [GUIDE_TARGET]
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("target", help="what to validate: " + ", ".join(targets))
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parent.parent,
                        help="repository root (default: parent of scripts/)")
    args = parser.parse_args(argv)
    if args.target not in targets:
        print(f"unknown target '{args.target}'; expected one of {targets}", file=sys.stderr)
        return 2
    root = args.root.resolve()
    errors = validate_guide(root) if args.target == GUIDE_TARGET else validate(root, args.target)
    for error in errors:
        print(error)
    return 1 if errors else 0
````

#### Edge Cases
1. **Case:** `Guide-Conventions.md` has no fixed heading set — it gets the generic checks only (citations,
   wiki links, placeholders, and from Step 5 the ID checks). Its structure is prose, like
   [[Docs/Analysis-Doc-Conventions]].
2. **Case:** two Guide documents share a number (`04-A.md`, `04-B.md`) — error. Both would mint `G04-*`
   IDs, and a reader could not tell from an ID which file to open.
3. **Case:** a file one level down that is not under `Reviews/` — "unexpected file". A file the validator
   does not know is a file nothing checks.
4. **Case:** an extra `##` section (`## Sequence`, `## Traceability Matrix`) — allowed anywhere;
   `check_headings` only requires the six headings to be present and in order.
5. **Case:** the existing test `test_11a_unknown_project_is_usage_error` — still passes: an unknown target
   is still exit 2. Only the message wording changes ("unknown target").
6. **Case:** a Guide document links the parent Feature, a Task or a Bug Report — error "wiki link into
   Features/ …". The link would resolve today and break on the day the target moves to another status
   directory, far from its cause. ADRs do not move; link those.
   <!-- REVIEW-FIX: the rule was stated in the conventions but nothing enforced it -->
7. **Case:** a line in a test fixture longer than the surrounding code — the fixtures are Markdown inside
   `textwrap.dedent`; keep their lines as shown, the Markdown table rows must stay on one line.

---

### Step 4: Slice 2 — rule blocks and evidence

**Goal:** Inside `## Rules`, every `###` heading is a Rule ID with the four fields, and its evidence is real.
**Dependencies:** Step 3.

- [x] Add `TestGuideRules` to the test file.
- [x] Run the guide tests — **7 of the 9 new tests fail** (`g04a`, `g04b`, `g04c`, `g04d`, `g04f`, `g04h`,
  `g04i`). `g04e` and `g04g` already pass: they assert that a correct document is accepted.
- [x] Add `field_text` (a text helper, after `h3_blocks`), then `adr_file`, `check_evidence` and
  `check_rules` to the guide-checks section, and the one call in `validate_guide`.
- [x] Run the full suite — `Ran 67 tests`, `OK`.

**Why this step is critical:**
The Rule ID is what the Skill, the Base Project and every validation review will cite. ADR-001 point 3
("every Guide rule cites at least one Finding ID or one ADR") is the property that separates this Guide from
an opinion; this slice is where it becomes a failing exit code.

#### Tests

````python
class TestGuideRules(GuideTestCase):
    def test_g04a_heading_with_title_fails(self):
        self.edit(self.doc, "### G04-02\n", "### G04-02 Authorization\n")
        self.assertFailsWith("rule heading")

    def test_g04b_document_number_mismatch_fails(self):
        self.edit(self.doc, "### G04-02\n", "### G06-02\n")
        self.assertFailsWith("G06-02", "document 04")

    def test_g04c_duplicate_rule_id_fails(self):
        self.edit(self.doc, "### G04-02\n", "### G04-01\n")
        self.assertFailsWith("duplicate rule ID G04-01")

    def test_g04d_missing_required_field_fails(self):
        for field in ["**Rule:**", "**Why:**", "**Evidence:**", "**Differs from references:**"]:
            with self.subTest(field=field):
                self.reset_fixture()
                lines = self.read(self.doc).splitlines()
                lines.remove(next(l for l in lines if l.startswith(field)))  # the first block is G04-01
                self.write(self.doc, "\n".join(lines) + "\n")
                self.assertFailsWith(f"rule G04-01 is missing field {field}")

    def test_g04e_withdrawn_rule_needs_only_rule_and_status(self):
        self.edit(self.doc, "## Differs From the Reference Projects",
                  "### G04-03\n**Rule:** An old rule.\n**Status:** Withdrawn — replaced by G04-02\n\n"
                  "## Differs From the Reference Projects")
        self.assertPasses()

    def test_g04f_rules_section_without_a_rule_fails(self):
        self.write(self.matrix, MATRIX_DOC.replace(
            "None — the matrix maps findings to rules stated in other documents.", "See the matrix."))
        self.link_in_index("Docs/Guide/16-Traceability-Matrix")
        self.edit(self.idx, "**Draft documents:** 1", "**Draft documents:** 2")
        self.assertFailsWith("16-Traceability-Matrix.md", "has no rule block")

    def test_g04g_rules_section_saying_none_passes(self):
        self.add_matrix()
        self.assertPasses()

    def test_g04h_evidence_without_finding_or_adr_fails(self):
        self.edit(self.doc, "**Evidence:** BE-R02-01 · ADR-001", "**Evidence:** Common practice.")
        self.assertFailsWith("G04-01", "cites no Finding ID and no ADR")

    def test_g04i_superseded_adr_as_evidence_fails(self):
        self.edit(self.doc, "**Evidence:** BE-R02-01 · ADR-001", "**Evidence:** ADR-002")
        self.assertFailsWith("G04-01", "ADR-002", "Superseded")
````

#### Implementation

````python
def field_text(content: str, field: str) -> Optional[str]:
    """Text of one `**Field:**` of a block, up to the next field; None when the field is absent."""
    match = re.search(rf"^{re.escape(field)}", content, re.MULTILINE)
    if not match:
        return None
    rest = content[match.end():]
    nxt = FIELD_START.search(rest)
    return rest[: nxt.start()] if nxt else rest
````

````python
def adr_file(ctx: Context, digits: str) -> Optional[Path]:
    """`ADR-NNN` resolves by filename prefix: documentation/ADRs/ADR-NNN-*.md."""
    matches = sorted((ctx.documentation / "ADRs").glob(f"ADR-{digits}-*.md"))
    return matches[0] if matches else None


def check_evidence(ctx: Context, doc: Path, rid: str, evidence: str, errors: List[str]) -> None:
    """A rule rests on at least one reference Finding ID or ADR, and never on a retired ADR."""
    findings = [m for m in FINDING_TOKEN.findall(evidence) if m[0] != GUIDE_CODE]
    adrs = sorted(set(ADR_TOKEN.findall(evidence)))
    if not findings and not adrs:
        errors.append(f"{ctx.rel(doc)}: rule {rid} cites no Finding ID and no ADR in **Evidence:**")
    for digits in adrs:
        path = adr_file(ctx, digits)
        status = re.search(r"^### Status\s*\n+\s*(\w+)", ctx.text(path), re.MULTILINE) if path else None
        if status and status.group(1) in RETIRED_ADR_STATUSES:
            errors.append(f"{ctx.rel(doc)}: rule {rid} cites ADR-{digits}, which is "
                          f"{status.group(1)}; cite the ADR in effect")


def check_rules(ctx: Context, doc: Path, text: str, file_nn: str,
                rules: Dict[str, Tuple[Path, bool]], errors: List[str]) -> None:
    """Validate every `###` heading inside `## Rules` and record it in `rules` as (doc, withdrawn)."""
    if "Rules" not in h2_headings(text):
        return  # reported by check_headings
    body = section(text, "Rules")
    blocks = h3_blocks(body)
    if not blocks and not body.strip().startswith("None"):
        errors.append(f"{ctx.rel(doc)}: '## Rules' has no rule block; a document that states no rule "
                      f"says 'None — <reason>'")
    for rid, content in blocks:
        match = RULE_ID.match(rid)
        if not match:
            errors.append(f"{ctx.rel(doc)}: rule heading '### {rid}' must be exactly "
                          f"'### G{file_nn}-<MM>' (put the text in **Rule:**)")
            continue
        if match.group(1) != file_nn:
            errors.append(f"{ctx.rel(doc)}: rule {rid} has document number {match.group(1)} "
                          f"but lives in document {file_nn}")
        withdrawn = bool(re.search(r"^\*\*Status:\*\*\s*Withdrawn", content, re.MULTILINE))
        if rid in rules:
            errors.append(f"{ctx.rel(doc)}: duplicate rule ID {rid} (also in {ctx.rel(rules[rid][0])})")
        else:
            rules[rid] = (doc, withdrawn)
        for field in WITHDRAWN_RULE_FIELDS if withdrawn else RULE_FIELDS:
            if field_text(content, field) is None:
                errors.append(f"{ctx.rel(doc)}: rule {rid} is missing field {field}")
        evidence = field_text(content, "**Evidence:**")
        if not withdrawn and evidence is not None:
            check_evidence(ctx, doc, rid, evidence, errors)
````

In `validate_guide`, inside `if kind == "guide":`, after the `drafts += …` line:

```python
            check_rules(ctx, doc, text, file_nn, rules, errors)
```

#### Edge Cases
1. **Case:** a document that states no rule (the traceability matrix, perhaps the recipe) — `## Rules` is
   still required and says `None — <reason>`. A section with neither a rule nor `None` is an error, so an
   empty section cannot pass for a finished one.
2. **Case:** a withdrawn rule — the heading stays, `**Status:** Withdrawn — <reason>` is added, and only
   `**Rule:**` and `**Status:**` are required. The ID keeps resolving, so an old citation finds the reason
   instead of nothing.
3. **Case:** the evidence is a contract-review finding only (`GC-R01-02`) — not accepted. A contract review
   judges the Guide; it is not evidence about the reference projects. A reference Finding ID or an ADR is
   needed.
4. **Case:** the evidence cites a Superseded or Deprecated ADR — error naming the status. An ADR that is
   `Proposed` (ADR-017 today) is accepted.
5. **Case:** a field written in the middle of a line — not recognised. Each field starts its own line
   (`^\*\*Rule:\*\*`), the same rule as for finding fields.
6. **Case:** `## Rules` is missing altogether — `check_headings` reports it once; `check_rules` returns
   early and adds no second error.

---

### Step 5: Slice 3 — every ID resolves

**Goal:** A Finding ID, an `ADR-NNN` or a Rule ID written anywhere in a Guide document points at something
that exists.
**Dependencies:** Step 4 (the rule set).

- [x] Add `TestGuideReferences`.
- [x] Run the guide tests — **6 of the 8 new tests fail** (`g05a`–`g05e`, `g05g`). `g05f` and `g05h` already
  pass (`g05h` is proven by the wiki-link fragment check that existed before).
- [x] Add `finding_exists` and `check_tokens` to the guide-checks section, and Pass 2 to `validate_guide`.
- [x] Run the full suite — `Ran 75 tests`, `OK`.

**Why this step is critical:**
The parent itself carried a citation to a finding that does not exist (`WP-R05-08`, corrected in Task 1).
Sixteen documents with several hundred citations cannot be checked by eye, and a rule whose evidence does
not exist is exactly the "invention" ADR-001 forbids.

#### Tests

````python
class TestGuideReferences(GuideTestCase):
    def test_g05a_unknown_finding_fails(self):
        self.edit(self.doc, "**Evidence:** BE-R02-01 · ADR-001", "**Evidence:** BE-R02-09 · ADR-001")
        self.assertFailsWith("finding BE-R02-09 does not exist")

    def test_g05b_unknown_adr_fails(self):
        self.edit(self.doc, "**Evidence:** BE-R02-01 · ADR-001", "**Evidence:** BE-R02-01 · ADR-099")
        self.assertFailsWith("ADR-099 is cited but no such ADR exists")

    def test_g05c_four_digit_adr_fails(self):
        self.edit(self.doc, "**Evidence:** BE-R02-01 · ADR-001", "**Evidence:** BE-R02-01 · ADR-0001")
        self.assertFailsWith("ADR-0001", "three-digit")

    def test_g05d_unknown_rule_reference_fails(self):
        self.edit(self.doc, "It completes G04-01.", "It completes G04-07.")
        self.assertFailsWith("rule G04-07 does not exist")

    def test_g05e_references_inside_code_are_checked(self):
        self.edit(self.doc, "The base consults the policy",
                  "```java\n// see G09-01 and BT-R01-01\n```\nThe base consults the policy")
        self.assertFailsWith("rule G09-01 does not exist", "finding BT-R01-01 does not exist")

    def test_g05f_conventions_may_show_example_rule_ids(self):
        # the fixture conventions mention G06-03 and GC-R01-01, which do not exist — must pass
        self.assertPasses()

    def test_g05g_conventions_findings_and_adrs_are_still_checked(self):
        self.edit(self.conv, "such as ADR-001", "such as ADR-042")
        self.assertFailsWith("Guide-Conventions.md", "ADR-042")

    def test_g05h_wiki_fragment_to_missing_rule_fails(self):
        self.edit(self.doc, "- [[Docs/Guide/Guide-Conventions]]\n",
                  "- [[Docs/Guide/04-CRUD-Base-and-Service-Hooks#G04-01|G04-01]]\n"
                  "- [[Docs/Guide/Guide-Conventions]]\n")
        self.assertPasses()
        self.edit(self.doc, "#G04-01|G04-01]]\n- ", "#G04-05|rule five]]\n- ")
        self.assertFailsWith("no heading 'G04-05'")
````

#### Implementation

````python
def finding_exists(ctx: Context, code: str, nn: str, fid: str) -> bool:
    """A Finding ID resolves when its review document has a heading that is exactly the ID."""
    home = GUIDE_DIR if code == GUIDE_CODE else {v: k for k, v in PROJECT_CODES.items()}[code]
    reviews = ctx.documentation / "Docs" / home / "Reviews"
    return any(fid in all_headings(ctx.text(path)) for path in sorted(reviews.glob(f"{nn}-*-Review.md")))
````

````python
def check_tokens(ctx: Context, doc: Path, text: str, rules: Optional[Dict[str, Tuple[Path, bool]]],
                 errors: List[str]) -> None:
    """Every Finding ID, ADR and Rule ID written in a Guide doc must resolve — in prose and in code.
    With `rules=None` (Guide-Conventions.md, which shows examples) Rule IDs and GC IDs are not resolved."""
    for code, nn, mm in sorted(set(FINDING_TOKEN.findall(text))):
        fid = f"{code}-R{nn}-{mm}"
        if code == GUIDE_CODE and rules is None:
            continue
        if not finding_exists(ctx, code, nn, fid):
            errors.append(f"{ctx.rel(doc)}: finding {fid} does not exist")
    for digits in sorted(set(ADR_TOKEN.findall(text))):
        if len(digits) != 3:
            errors.append(f"{ctx.rel(doc)}: 'ADR-{digits}' is not the three-digit form ADR-NNN")
        elif adr_file(ctx, digits) is None:
            errors.append(f"{ctx.rel(doc)}: ADR-{digits} is cited but no such ADR exists")
    if rules is not None:
        for rid in sorted(set(RULE_TOKEN.findall(text))):
            if rid not in rules:
                errors.append(f"{ctx.rel(doc)}: rule {rid} does not exist")
````

In `validate_guide`, after the Pass 1 loop:

```python
    # Pass 2 — references between documents; needs the complete rule set.
    for doc, kind in kinds.items():
        text = ctx.text(doc)
        check_tokens(ctx, doc, text, None if kind == "conventions" else rules, errors)
```

#### Edge Cases
1. **Case:** an ID inside inline code or a fenced block — **checked**. A sketch that names a rule in a
   comment is a citation like any other. (Wiki links inside code stay unchecked, as before: templates quote
   them.)
2. **Case:** `Guide-Conventions.md` shows `G06-03` and `GC-R01-02` as examples before those exist — Rule IDs
   and contract-review IDs are not resolved in that one file. Its Finding IDs and ADRs **are** resolved, so
   its examples use real ones.
3. **Case:** a pattern such as `BE-R01-*` or the grammar `G<NN>-<MM>` — not an ID; the regular expressions
   need two digits in each position.
4. **Case:** `ADR-016-guide-document-format-and-rule-ids` inside a wiki-link path — not matched as a token
   (the lookahead refuses a following `-`); the link itself is checked by `check_wiki_links`, and the alias
   `|ADR-016]]` is matched and resolved.
5. **Case:** a four-digit `ADR-0016` — error "not the three-digit form". Task 1 normalised the parent for
   this reason.
6. **Case:** a validation finding ID (`V01A-R02-03`) — not matched by any pattern here; that family belongs
   to the `validation` target (Task 7).

---

### Step 6: Slice 4 — Version Notes

**Goal:** Every Version Note declares how far it is verified, and a `verified` note points at evidence.
**Dependencies:** Step 3. (Independent of Steps 4–5.)

- [x] Add `TestGuideVersionNotes`.
- [x] Run the guide tests — **8 of the 11 new tests fail** (`g06a`, `g06b`, `g06c`, `g06e`, `g06f`, `g06g`,
  `g06i`, `g06j`). `g06d` and `g06k` assert acceptance; `g06h` is already proven by the citation check with
  the guide pattern.
- [x] Add `has_version_evidence` and `check_version_notes` to the guide-checks section, and the one call in
  `validate_guide`.
- [x] Run the full suite — `Ran 86 tests`, `OK`.

**Why this step is critical:**
User story 7 asks that "no note is presented as authoritative evidence that nothing tests". The Skill tells
agents to look up framework APIs at execution time; a Version Note that reads as verified fact would
override that instruction with a remembered API name — the failure ADR-003 exists to prevent.

#### Tests

````python
class TestGuideVersionNotes(GuideTestCase):
    def replace_first_note(self, new: str) -> None:
        self.edit(self.doc, "- **verified on 4.1.x** — the entry points carry `@Transactional`.\n"
                            "  Evidence: `base-project/src/test/java/AppTest.java:1-3`\n", new)

    def test_g06a_entry_without_marker_fails(self):
        self.replace_first_note("- The entry points carry `@Transactional`.\n")
        self.assertFailsWith("Version Note", "must start with")

    def test_g06b_text_outside_an_entry_fails(self):
        self.edit(self.doc, "## Version Notes\n", "## Version Notes\nOn 4.1.x everything below holds.\n")
        self.assertFailsWith("text outside an entry")

    def test_g06c_empty_section_fails(self):
        text = self.read(self.doc)
        head, tail = text.split("## Version Notes\n")
        self.write(self.doc, head + "## Version Notes\n\n## Related Documents"
                   + tail.split("## Related Documents")[1])
        self.edit(self.idx, "**Version Notes not verified:** 1", "**Version Notes not verified:** 0")
        self.assertFailsWith("'## Version Notes' is empty")

    def test_g06d_none_passes(self):
        text = self.read(self.doc)
        head, tail = text.split("## Version Notes\n")
        self.write(self.doc, head + "## Version Notes\nNone.\n\n## Related Documents"
                   + tail.split("## Related Documents")[1])
        self.edit(self.idx, "**Version Notes not verified:** 1", "**Version Notes not verified:** 0")
        self.assertPasses()

    def test_g06e_verified_without_evidence_fails(self):
        self.replace_first_note("- **verified on 4.1.x** — the entry points carry `@Transactional`.\n")
        self.assertFailsWith("marked verified but cites no")

    def test_g06f_verified_with_untagged_url_fails(self):
        self.replace_first_note("- **verified on 4.1.x** — see "
                                "https://docs.spring.io/spring-boot/reference/features/external-config.html\n")
        self.assertFailsWith("marked verified but cites no")

    def test_g06g_verified_with_glob_path_fails(self):
        self.replace_first_note("- **verified on 4.1.x** — see `base-project/src/test/**/AppTest.java`\n")
        self.assertFailsWith("marked verified but cites no")

    def test_g06h_verified_citing_missing_test_fails(self):
        self.replace_first_note("- **verified on 4.1.x** — see `base-project/src/test/java/GoneTest.java:1`\n")
        self.assertFailsWith("base-project/src/test/java/GoneTest.java:1", "path does not exist")

    def test_g06i_verified_citing_production_code_fails(self):
        self.replace_first_note("- **verified on 4.1.x** — see `base-project/src/main/java/App.java:1`\n")
        self.assertFailsWith("marked verified but cites no")

    def test_g06j_malformed_line_in_marker_fails(self):
        self.replace_first_note("- **verified on Boot 4** — see `base-project/src/test/java/AppTest.java:1`\n")
        self.assertFailsWith("must start with")

    def test_g06k_evidence_pack_test_counts_as_a_test(self):
        self.write("documentation/Docs/Validation/Run-01/Shop/evidence/src/test/java/NoteTest.java",
                   "class NoteTest {}\n")
        self.replace_first_note("- **verified on 4.1.x** — see "
                                "`documentation/Docs/Validation/Run-01/Shop/evidence/src/test/java/NoteTest.java:1`\n")
        self.assertPasses()
````

#### Implementation

````python
def has_version_evidence(entry: str) -> bool:
    """A `verified` Version Note cites a test (exact path), a reference Finding ID or a version-tagged URL."""
    for match in GUIDE_CITATION.finditer(entry):
        raw = match.group(1)
        if "*" not in raw and raw.startswith(TEST_ROOTS) and "/src/test/" in raw:
            return True
    if any(code != GUIDE_CODE for code, _, _ in FINDING_TOKEN.findall(entry)):
        return True
    return any(VERSION_SEGMENT.search(re.sub(r"^https?://[^/]+", "", url)) for url in URL.findall(entry))


def check_version_notes(ctx: Context, doc: Path, text: str, errors: List[str]) -> int:
    """Every Version Note is a `- ` list item that starts with a marker. Returns the `not verified` count."""
    if "Version Notes" not in h2_headings(text):
        return 0  # reported by check_headings
    entries: List[str] = []
    stray: List[str] = []
    for line in section(text, "Version Notes").splitlines():
        if not line.strip() or line.startswith("#"):
            continue
        if line.startswith("- "):
            entries.append(line)
        elif entries and line[0] in " \t":
            entries[-1] += "\n" + line
        else:
            stray.append(line.strip())
    if not entries and stray == ["None."]:
        return 0
    if not entries and not stray:
        errors.append(f"{ctx.rel(doc)}: '## Version Notes' is empty; write 'None.' when there is no note")
    for line in stray:
        errors.append(f"{ctx.rel(doc)}: Version Notes — text outside an entry: '{line[:50]}' "
                      f"(every note is a '- ' list item; continuation lines are indented)")
    not_verified = 0
    for entry in entries:
        shown = entry.splitlines()[0][:60]
        match = VERSION_ENTRY.match(entry)
        if not match:
            errors.append(f"{ctx.rel(doc)}: Version Note '{shown}' must start with "
                          f"'**verified on <major>.<minor>.x**' or '**not verified**'")
        elif match.group(1):
            not_verified += 1
        elif not has_version_evidence(entry):
            errors.append(f"{ctx.rel(doc)}: Version Note '{shown}' is marked verified but cites no "
                          f"test under base-project/ or documentation/Docs/Validation/ (a path with "
                          f"/src/test/), no Finding ID and no version-tagged URL")
    return not_verified
````

In `validate_guide`, inside `if kind == "guide":`, after the `check_rules(…)` line:

```python
            not_verified += check_version_notes(ctx, doc, text, errors)
```

#### Edge Cases
1. **Case:** prose between the heading and the list ("On 4.1.x everything below holds") — error "text
   outside an entry". A sentence outside a note is a version claim with no marker.
2. **Case:** a long note — continuation lines are indented (two spaces). An unindented line that does not
   start with `- ` is text outside an entry.
3. **Case:** a `verified` note with a glob path — not evidence. A glob is not checked for existence, so it
   would be a way to claim a test that is not there.
4. **Case:** a `verified` note that cites `base-project/src/main/…` — not evidence. Production code shows
   what is written, not that it works.
5. **Case:** a `verified` note that cites a test file that does not exist — two things could report it;
   only `check_citations` does ("path does not exist"), because `has_version_evidence` already sees a
   well-formed test citation.
6. **Case:** a URL with no version segment, or with `/current/` — does not count. The redirect found at
   creation (see Documentation Reviewed) means an author who copies the address bar gets an unversioned URL
   for the current line; the error message tells them what is missing.
7. **Case:** the version in the URL and the line in the marker disagree (`verified on 4.1.x` with
   `/reference/7.0/`) — not checked. Product versions differ from the Spring Boot line that ships them, so
   no mechanical rule exists. It stays with the author and the reviewer.
8. **Case:** a `###` sub-heading inside the section — allowed and skipped.
9. **Case:** `None.` together with entries — the `None.` line is reported as text outside an entry.

---

### Step 7: Slice 5 — the index is complete and its counts are true

**Goal:** Every document is linked from `Guide-Index.md`, and the index reports the real number of drafts
and of `not verified` notes.
**Dependencies:** Steps 3 and 6 (the two counts).

- [x] Add `TestGuideIndex`.
- [x] Run the guide tests — **all 4 new tests fail**.
- [x] Add `check_guide_status` to the guide-checks section and the index block at the end of
  `validate_guide`.
- [x] Run the full suite — `Ran 90 tests`, `OK`.

**Why this step is critical:**
ADR-003 says `not verified` notes exist until the Base Project is built "and their count is reported in
`Guide-Index.md`". A number typed by hand goes stale with the first edit; a number the validator recomputes
cannot.

#### Tests

````python
class TestGuideIndex(GuideTestCase):
    def test_g07a_document_not_linked_from_index_fails(self):
        self.edit(self.idx, "[[Docs/Guide/04-CRUD-Base-and-Service-Hooks\\|CRUD Base and Service Hooks]]",
                  "`04-CRUD-Base-and-Service-Hooks`")
        self.assertFailsWith("Guide-Index.md: does not link", "04-CRUD-Base-and-Service-Hooks.md")

    def test_g07b_not_verified_count_mismatch_fails(self):
        self.edit(self.idx, "**Version Notes not verified:** 1", "**Version Notes not verified:** 0")
        self.assertFailsWith("Version Notes not verified: 0", "contain 1")

    def test_g07c_draft_count_mismatch_fails(self):
        self.edit(self.doc, "#doc #guide #architecture #draft", "#doc #guide #architecture")
        self.assertFailsWith("Draft documents: 1", "contain 0")

    def test_g07d_missing_status_line_fails(self):
        self.edit(self.idx, "- **Draft documents:** 1\n", "")
        self.assertFailsWith("missing status line", "Draft documents")
````

#### Implementation

````python
def check_guide_status(ctx: Context, index_path: Path, label: str, actual: int, errors: List[str]) -> None:
    """The index reports a count on a line `- **<label>:** <n>`; it must equal the computed value."""
    match = re.search(rf"^(?:- )?\*\*{re.escape(label)}:\*\*\s*(\d+)\s*$", ctx.text(index_path), re.MULTILINE)
    if not match:
        errors.append(f"{ctx.rel(index_path)}: missing status line '- **{label}:** <count>'")
    elif int(match.group(1)) != actual:
        errors.append(f"{ctx.rel(index_path)}: reports '{label}: {match.group(1)}' "
                      f"but the documents contain {actual}")
````

At the end of `validate_guide`, before `return errors`:

```python
    if index_path.is_file():
        check_index_coverage(ctx, index_path, docs, errors)
        check_guide_status(ctx, index_path, DRAFT_LABEL, drafts, errors)
        check_guide_status(ctx, index_path, NOT_VERIFIED_LABEL, not_verified, errors)
```

#### Edge Cases
1. **Case:** a planned document — it has no file, so the index names it in inline code. Wiki links inside
   inline code are not checked, and "every document is linked" only concerns files that exist.
2. **Case:** the status line written without the list dash — accepted (`(?:- )?`). The list form is the one
   in the template because two bare lines would join into one paragraph in some renderers.
3. **Case:** the index links a Guide document inside a table — the alias pipe is escaped (`\|`), which
   `WIKI_LINK` already handles.
4. **Case:** `Guide-Conventions.md` or a review is not linked — same error as for a Guide document.

---

### Step 8: Slice 6 — contract reviews

**Goal:** A review under `Docs/Guide/Reviews/` is checked like a reference review, with the code `GC`.
**Dependencies:** Step 5.

- [x] Add `TestGuideReviews`.
- [x] Run the guide tests — **3 of the 6 new tests fail** (`g08b`, `g08c`, `g08d`). `g08a` asserts
  acceptance; `g08e` and `g08f` are already proven by `check_tokens`.
- [x] Add the `elif kind == "review":` branch and the summary block to `validate_guide`. No new function.
- [x] Run the full suite — `Ran 96 tests`, `OK`.

**Why this step is critical:**
The contract review of the parent's Step 2.6 **gates the Skill**. Its findings get IDs that Guide corrections and the
changelog will cite; if those IDs are malformed or missing from the summary, the gate count (0 🔴 / 0 🟠)
is taken from an incomplete list.

#### Tests

````python
class TestGuideReviews(GuideTestCase):
    def test_g08a_review_with_summary_passes(self):
        self.add_review()
        self.assertPasses()

    def test_g08b_review_without_summary_fails(self):
        self.add_review()
        (self.root / self.summ).unlink()
        self.edit(self.idx, "- [[Docs/Guide/Reviews/00-Review-Summary]]\n", "")
        self.assertFailsWith("00-Review-Summary.md", "review summary is missing")

    def test_g08c_finding_absent_from_summary_fails(self):
        self.add_review()
        self.edit(self.summ, "| GC-R01-01 | Absent field | 🟠 High | 01 |\n", "")
        self.assertFailsWith("GC-R01-01", "is not listed")

    def test_g08d_reference_project_code_in_contract_review_fails(self):
        self.add_review()
        self.edit(self.rev, "### GC-R01-01\n", "### BE-R01-01\n")
        self.edit(self.summ, "| GC-R01-01 |", "| BE-R01-01 |")
        self.assertFailsWith("uses code BE", "GC")

    def test_g08e_unknown_contract_finding_reference_fails(self):
        self.edit(self.doc, "It completes G04-01.", "It completes G04-01 (raised by GC-R01-01).")
        self.assertFailsWith("finding GC-R01-01 does not exist")

    def test_g08f_review_citing_unknown_rule_fails(self):
        self.add_review()
        self.edit(self.rev, "**Evidence:** G04-01 in", "**Evidence:** G04-08 in")
        self.assertFailsWith("01-Contract-Review.md", "rule G04-08 does not exist")
````

#### Implementation

In `validate_guide`, in the Pass 1 loop, after the `if kind == "guide":` block:

```python
        elif kind == "review":
            check_findings(ctx, doc, text, REVIEW_NAME.match(doc.name).group(1), seen_ids, errors)
```

After the Pass 2 loop, before the index block:

```python
    if summary_path.is_file():
        check_summary_coverage(ctx, summary_path, seen_ids, errors)
    elif seen_ids:
        errors.append(f"{ctx.rel(summary_path)}: review summary is missing")
```

#### Edge Cases
1. **Case:** no review exists yet (the state after this Task and until Task 5) — no summary is required.
   The summary becomes mandatory with the first finding.
2. **Case:** a review file with a reference code (`### BE-R01-01`) — error "uses code BE; project Guide uses
   GC".
3. **Case:** the summary's heading set — `Overview`, `Findings by Severity`, `Related Documents`. The
   reference summaries also require "Top 5 Recommendations" and "Candidate Patterns for the Skill"; neither
   means anything for a contract review. Extra `##` sections are allowed, so Task 5 can add a gate section.
4. **Case:** the gate itself — not evaluated. Whether a finding is still open is a judgement, and the
   format for recording it belongs to Task 5.

---

### Step 9: Slice 7 — the traceability matrix covers its scope

**Goal:** In a `## Traceability Matrix` section, every in-scope reference finding has exactly one row that
names a rule in force or `N/A — <reason>`.
**Dependencies:** Steps 4 and 5.

- [x] Add `TestGuideTraceability`.
- [x] Run the guide tests — **4 of the 7 new tests fail** (`g09b`, `g09c`, `g09d`, `g09e`). `g09a` and
  `g09g` assert acceptance; `g09f` is already proven by `check_tokens`.
- [x] Add `traceability_scope` and `check_traceability` to the guide-checks section, and the two lines in
  Pass 2.
- [x] Run the full suite — `Ran 103 tests`, `OK`.
- [x] Compare `validate_guide` with the complete listing below, line by line.

**Why this step is critical:**
User story 51 asks for proof that the old defects are covered, "including the 🟡 ones the severity-only
scope would skip". BE-R01-10 (CORS) is tagged `configuration` and rated below High; a severity rule would
drop it. Making the scope a computation (78 findings today) means the matrix cannot quietly leave one out.

#### Tests

````python
class TestGuideTraceability(GuideTestCase):
    def test_g09a_complete_matrix_passes(self):
        # BE-R02-02 is Medium and not from a security review: it needs no row
        self.add_matrix()
        self.assertPasses()

    def test_g09b_missing_in_scope_finding_fails(self):
        cases = {"| BE-R01-01 | 🔴 | G04-02 | |\n": "BE-R01-01",            # Critical
                 "| BE-R02-01 | 🟠 | G04-01, G04-02 | partial — see the note |\n": "BE-R02-01",  # High
                 "| BE-R01-02 | 🟢 | N/A — the convention has no such endpoint | |\n": "BE-R01-02"}  # Low, security
        for row, fid in cases.items():
            with self.subTest(finding=fid):
                self.reset_fixture()
                self.add_matrix()
                self.edit(self.matrix, row, "")
                self.assertFailsWith(f"finding {fid} is in the traceability scope but has no row")

    def test_g09c_row_without_rule_or_reason_fails(self):
        self.add_matrix()
        self.edit(self.matrix, "| BE-R01-01 | 🔴 | G04-02 | |", "| BE-R01-01 | 🔴 | N/A | |")
        self.assertFailsWith("traceability row BE-R01-01 maps to no rule")

    def test_g09d_row_mapped_to_withdrawn_rule_fails(self):
        self.add_matrix()
        self.edit(self.doc, "**Why:** A forgotten annotation is fail-open. It completes G04-01.\n"
                            "**Evidence:** [[Docs/backend/Reviews/01-Security-Review#BE-R01-01|BE-R01-01]]\n"
                            "**Differs from references:** None — kept from the reference projects.\n",
                  "**Status:** Withdrawn — merged into G04-01\n")
        self.assertFailsWith("traceability row BE-R01-01 maps to withdrawn rule G04-02")

    def test_g09e_duplicate_row_fails(self):
        self.add_matrix()
        self.edit(self.matrix, "| BE-R01-01 | 🔴 | G04-02 | |\n",
                  "| BE-R01-01 | 🔴 | G04-02 | |\n| BE-R01-01 | 🔴 | G04-01 | |\n")
        self.assertFailsWith("more than one row for BE-R01-01")

    def test_g09f_row_mapped_to_unknown_rule_fails(self):
        self.add_matrix()
        self.edit(self.matrix, "| BE-R01-01 | 🔴 | G04-02 | |", "| BE-R01-01 | 🔴 | G04-09 | |")
        self.assertFailsWith("rule G04-09 does not exist")

    def test_g09g_withdrawn_reference_finding_needs_no_row(self):
        self.add_matrix()
        self.edit("documentation/Docs/backend/Reviews/01-Security-Review.md",
                  "## Recommended Target Pattern",
                  "### BE-R01-03\n**Title:** Old claim\n**Status:** Withdrawn — disproved\n\n"
                  "## Recommended Target Pattern")
        self.assertPasses()
````

#### Implementation

````python
def traceability_scope(ctx: Context) -> List[str]:
    """Reference findings the matrix must map (ADR-016): every Critical and High finding, plus every
    finding of a security review, whatever its severity. Withdrawn findings are out."""
    scope: List[str] = []
    for project in PROJECT_CODES:
        reviews = ctx.documentation / "Docs" / project / "Reviews"
        for review in sorted(reviews.glob("[0-9][0-9]-*-Review.md")):
            for fid, content in h3_blocks(section(ctx.text(review), "Findings")):
                if not FINDING_ID.match(fid) or re.search(r"^\*\*Status:\*\*\s*Withdrawn", content, re.MULTILINE):
                    continue
                severity = field_text(content, "**Severity:**") or ""
                if review.name[:2] == SECURITY_REVIEW_NN or "🔴" in severity or "🟠" in severity:
                    scope.append(fid)
    return scope


def check_traceability(ctx: Context, doc: Path, text: str, rules: Dict[str, Tuple[Path, bool]],
                       errors: List[str]) -> None:
    """In a `## Traceability Matrix` section, every in-scope finding has exactly one table row that maps
    it to at least one rule in force, or to 'N/A — <reason>'."""
    if MATRIX_HEADING not in h2_headings(text):
        return
    mapped = set()
    for first, rest in MATRIX_ROW.findall(section(text, MATRIX_HEADING)):
        ids = [f"{c}-R{n}-{m}" for c, n, m in FINDING_TOKEN.findall(first) if c != GUIDE_CODE]
        if len(ids) != 1:
            continue  # header, separator or a row that is not a mapping
        fid = ids[0]
        if fid in mapped:
            errors.append(f"{ctx.rel(doc)}: traceability matrix has more than one row for {fid}")
        mapped.add(fid)
        row_rules = RULE_TOKEN.findall(rest)
        if not row_rules and not re.search(r"N/A\s*—\s*\S", rest):
            errors.append(f"{ctx.rel(doc)}: traceability row {fid} maps to no rule and gives no "
                          f"'N/A — <reason>'")
        for rid in row_rules:
            if rid in rules and rules[rid][1]:
                errors.append(f"{ctx.rel(doc)}: traceability row {fid} maps to withdrawn rule {rid}")
    for fid in traceability_scope(ctx):
        if fid not in mapped:
            errors.append(f"{ctx.rel(doc)}: finding {fid} is in the traceability scope but has no row")
````

In `validate_guide`, at the end of the Pass 2 loop body:

```python
        if kind == "guide":
            check_traceability(ctx, doc, text, rules, errors)
```

`validate_guide` is now complete. It must read exactly:

````python
def validate_guide(root: Path) -> List[str]:
    ctx = Context(root, GUIDE_DIR, GUIDE_CODE)
    errors: List[str] = []
    if not ctx.docs_dir.is_dir():
        return [f"{ctx.rel(ctx.docs_dir)}: the Guide directory does not exist"]

    docs = sorted(ctx.docs_dir.rglob("*.md"))
    index_path = ctx.docs_dir / GUIDE_INDEX
    conventions_path = ctx.docs_dir / GUIDE_CONVENTIONS
    summary_path = ctx.docs_dir / "Reviews" / SUMMARY_NAME
    if not index_path.is_file():
        errors.append(f"{ctx.rel(index_path)}: index is missing")
    if not conventions_path.is_file():
        errors.append(f"{ctx.rel(conventions_path)}: conventions document is missing")

    # Pass 1 — each document by itself; collects every Rule ID and every contract-review Finding ID.
    kinds: Dict[Path, str] = {}
    numbers: Dict[str, Path] = {}
    rules: Dict[str, Tuple[Path, bool]] = {}
    seen_ids: Dict[str, Path] = {}
    drafts = not_verified = 0
    headings_for = {"index": GUIDE_INDEX_HEADINGS, "guide": GUIDE_HEADINGS,
                    "summary": GUIDE_SUMMARY_HEADINGS, "review": REVIEW_HEADINGS}
    for doc in docs:
        kind, kind_error = classify_guide(ctx, doc)
        if kind_error:
            errors.append(f"{ctx.rel(doc)}: {kind_error}")
            continue
        kinds[doc] = kind
        text = ctx.text(doc)
        if kind in headings_for:
            check_headings(ctx, doc, text, headings_for[kind], errors)
        check_citations(ctx, doc, text, errors, GUIDE_CITATION)
        check_wiki_links(ctx, doc, text, errors)
        check_stable_links(ctx, doc, text, errors)
        check_placeholders(ctx, doc, text, errors)
        if kind == "guide":
            file_nn = GUIDE_NAME.match(doc.name).group(1)
            if file_nn in numbers:
                errors.append(f"{ctx.rel(doc)}: document number {file_nn} is already used by "
                              f"{numbers[file_nn].name}")
            numbers.setdefault(file_nn, doc)
            drafts += check_guide_tags(ctx, doc, text, errors)
            check_rules(ctx, doc, text, file_nn, rules, errors)
            not_verified += check_version_notes(ctx, doc, text, errors)
        elif kind == "review":
            check_findings(ctx, doc, text, REVIEW_NAME.match(doc.name).group(1), seen_ids, errors)

    # Pass 2 — references between documents; needs the complete rule set.
    for doc, kind in kinds.items():
        text = ctx.text(doc)
        check_tokens(ctx, doc, text, None if kind == "conventions" else rules, errors)
        if kind == "guide":
            check_traceability(ctx, doc, text, rules, errors)

    if summary_path.is_file():
        check_summary_coverage(ctx, summary_path, seen_ids, errors)
    elif seen_ids:
        errors.append(f"{ctx.rel(summary_path)}: review summary is missing")
    if index_path.is_file():
        check_index_coverage(ctx, index_path, docs, errors)
        check_guide_status(ctx, index_path, DRAFT_LABEL, drafts, errors)
        check_guide_status(ctx, index_path, NOT_VERIFIED_LABEL, not_verified, errors)
    return errors
````

#### Edge Cases
1. **Case:** no document has a `## Traceability Matrix` section (true until Task 5) — nothing is checked.
   Requiring the matrix to exist is a release check (the parent's Step 6.1), not a format check.
2. **Case:** a finding that is below High and not from a security review (`BE-R02-02` in the fixture) — it
   needs no row; it may have one.
3. **Case:** a withdrawn reference finding — out of scope. None exists today; the test covers the day one
   does.
4. **Case:** a row whose first cell is a wiki link in a table (`[[…#BE-R01-01\|BE-R01-01]]`) — the escaped
   pipe ends the first cell early, but the ID is found in what precedes it and the rule IDs in what follows.
5. **Case:** `N/A` with no reason — error. "N/A" alone is a row someone has not thought about.
6. **Case:** a row maps to a withdrawn rule — error. The finding would look covered by a rule that no longer
   holds.
7. **Case:** partial coverage (the parent names password spraying) — written in the row's note. The
   validator cannot tell partial from full; the conventions make the wording mandatory.
8. **Case:** the security scope is "review number 01" — true for the three projects today
   (`01-Security-Review`, `01-Security-and-Multi-Tenancy-Review`, `01-Security-Review`) and stated by the
   parent as `BE-R01-*`, `WP-R01-*`, `BT-R01-*`. It is one named constant (`SECURITY_REVIEW_NN`).

---

### Step 10: Write the two Guide files

**Goal:** The format exists as a document, and the Guide has a front page.
**Dependencies:** Step 9 (the validator checks what is written here).

- [x] Create `documentation/Docs/Guide/Guide-Conventions.md` with the content below.
- [x] Create `documentation/Docs/Guide/Guide-Index.md` with the content below.
- [x] Run `python3 scripts/validate-analysis-docs.py guide; echo "exit=$?"` — no output, `exit=0`.
- [x] Run the three project targets — exit 0 three times (the new directory does not disturb them).
- [x] Read section "Validation" of the conventions against the function list in the module map; every row
  of its table must have a check, and every check a row.

**Why this step is critical:**
`Guide-Conventions.md` is what the authors of Tasks 3–5 read; the validator is what tells them they are
done. If the two disagree, authors follow one and fail the other. Both files were validated at Task creation
against a copy of the real documentation tree (exit 0).

#### Implementation

`documentation/Docs/Guide/Guide-Conventions.md`:

````markdown
# Guide Conventions

#doc #guide #conventions #architecture

The single format contract for the **Guide** — the architecture and design guide for future Spring Boot
REST APIs, kept in `documentation/Docs/Guide/`. Every Guide document follows this document exactly. The
decision behind the format is [[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]]; the reason the
Guide exists is [[ADRs/ADR-001-guide-is-the-source-of-truth|ADR-001]]. Start reading the Guide at
[[Docs/Guide/Guide-Index]].

The rules below are checked mechanically by `python3 scripts/validate-analysis-docs.py guide` (see the
Validation section). When this document and the validator disagree, fix whichever one is wrong and record
the change in the Changelog at the bottom.

---

## 1. Layout and Naming

```
documentation/Docs/Guide/
├── Guide-Index.md
├── Guide-Conventions.md
├── NN-Title-Words.md
└── Reviews/
    ├── 00-Review-Summary.md
    └── NN-Title-Words-Review.md
```

- `NN-Title-Words.md` is a **Guide document**. The two-digit `NN` is the document number: it sets the
  reading order and it is the `NN` of every Rule ID in the document. A number is used by one document only.
  Title words are `Kebab-Case-Words` (letters, digits and hyphens only).
- `Reviews/` holds the contract reviews of the Guide (section 8). The directory appears with the first
  review.
- No other `.md` file may live under `Docs/Guide/`. An unlisted file is an unvalidated file.
- A document that is planned but not written does not exist as a file. Never create an empty document; the
  index lists it as planned (section 7).
- **Wiki links use the full path from `documentation/`**, for example
  `[[Docs/Guide/06-Query-Engine#G06-03|G06-03]]`. Inside a Markdown table, escape the alias pipe (`\|`).
- **A Guide document never links a Feature, Task or Bug document.** Those documents move between status
  directories, and a moved target breaks the link. Link the ADR that records the decision instead.

## 2. Tags and the Draft Marker

The first line of a Guide document is its `# ` title. The next non-empty line holds the tags.

- Guide document: `#doc #guide` plus 1–3 area tags, plus `#draft` while the document is a draft.
- Index: `#doc #guide #index`. Conventions: `#doc #guide #conventions`.
- Contract review: `#doc #review #guide` plus area tags. Review summary: `#doc #review #review-summary #guide`.

Area tags are the ones of [[Docs/Analysis-Doc-Conventions]] section 2: `#security`, `#persistence`,
`#architecture`, `#api-design`, `#testing`, `#configuration`, `#error-handling`, `#build`.

**`#draft` is the draft marker.** A Guide document is a draft from the day it is written until its prose
has been finalised against the Base Project. A draft is complete and binding: its rules, their IDs and its
module contracts are what the contract review gates and what the Skill cites. What is still open in a draft
is the narrative in `Purpose` and `Design`, the `Differs From the Reference Projects` section and the
Version Notes. The marker is removed when the document is finalised, never before.

## 3. Guide Document Template

A Guide document describes modules, their responsibilities, their interfaces and how they talk to each
other. Required `##` headings, in this order:

```markdown
# <Title>

#doc #guide #<area> #draft

**Guide:** [[Docs/Guide/Guide-Index]] · **Conventions:** [[Docs/Guide/Guide-Conventions]]

## Purpose
<3–6 sentences: what this area is for and the one idea a reader must take away>

## Design
<the modules, their responsibilities, their interfaces (entry points, invariants, error modes) and how
they talk to each other; at least one Mermaid diagram when there is a flow or a dependency direction;
code only as short illustrative sketches>

## Rules
<rule blocks, see section 4; a document that states no rule writes "None — <reason>">

## Differs From the Reference Projects
<bullets: every departure from the reference projects, each with the Rule ID that makes it and the
Finding ID that justifies it; "None." when the area is kept as it was>

## Version Notes
<version-specific detail, see section 6; "None." when there is none>

## Related Documents
<full-path wiki links to other Guide documents and to ADRs>
```

Extra `##` sections are allowed between the required ones (for example `## Traceability Matrix`,
section 9). `###` sub-headings are free in every section **except `## Rules`**, where a `###` heading is
always a Rule ID.

## 4. Rule Blocks and Rule IDs

A **Guide Rule** is one addressable, checkable statement. It is written as a block:

```markdown
### G06-03
**Rule:** Every filterable or sortable field is declared once in the entity's Query Profile; anything not
declared is rejected with 400.
**Why:** Unlisted fields let clients probe sensitive columns (password hashes).
**Evidence:** BT-R05-07, BE-R06-05 · ADR-011
**Differs from references:** BugTracker had no whitelist; `backend/` repeated the field name three times.
```

- The `###` heading is **exactly the Rule ID**. The rule text goes in `**Rule:**`. This is the only way a
  link such as `[[Docs/Guide/06-Query-Engine#G06-03|G06-03]]` resolves.
- **Rule ID format:** `G<NN>-<MM>`. `NN` is the number of the document the rule lives in. `MM` is
  sequential inside the document: a new rule takes the next unused number.
- **Required fields**, each starting its own line: `**Rule:**`, `**Why:**`, `**Evidence:**`,
  `**Differs from references:**`.
  - `**Rule:**` — behaviour, in one or two sentences, testable. It names products and standards
    (PostgreSQL, Flyway, RFC 9457, JWT) and the names this convention defines (`CurrentUser`, `RowScope`).
    It never names a framework class, method or annotation — those live in Version Notes
    ([[ADRs/ADR-003-version-baseline-as-a-support-policy|ADR-003]]).
  - `**Why:**` — the failure the rule prevents.
  - `**Evidence:**` — at least one reference Finding ID or one ADR (section 5). A rule with no evidence
    is an invention. A Superseded or Deprecated ADR is not evidence: cite the ADR in effect.
  - `**Differs from references:**` — what the reference projects did instead. A rule that keeps the
    reference shape says `None — <what is kept>`. The field is never left out: an absent field cannot be
    told apart from a forgotten one.
- **Optional field:** `**Optional:** <extension name>` marks a rule that applies only when a project uses
  an optional extension (for example storage replication). A validation review treats such a rule as not
  applicable when the extension is absent.
- A rule block may continue with free text, a `####` sub-heading, a table or a fenced sketch after its
  fields.
- **Rule IDs are stable.** From the day the Guide passes its contract review, an ID is never renumbered
  and never reused, because the Skill and the Base Project cite rules by ID. A rule that no longer holds is
  **withdrawn**: keep the heading and the `**Rule:**` line and add `**Status:** Withdrawn — <reason>`
  (a withdrawn rule needs only those two fields). A rule that must be split or moved is withdrawn and its
  replacement gets a new ID; the withdrawn rule's Status line names it. Each such change gets a line in the
  Changelog of [[Docs/Guide/Guide-Index]].

## 5. Citations and ID Families

Four ID families exist. Each has one form and one home, and they must not be confused:

| Family | Form | Example | Lives in |
|---|---|---|---|
| Reference finding | `<PROJ>-R<NN>-<MM>`, `PROJ` = `BE`, `BT` or `WP` | BE-R06-05 | `Docs/<project>/Reviews/` |
| Guide rule | `G<NN>-<MM>` | G06-03 | `Docs/Guide/NN-*.md`, section `## Rules` |
| Contract-review finding | `GC-R<NN>-<MM>` | GC-R01-02 | `Docs/Guide/Reviews/` |
| Validation finding | `V<NN><X>-R<NN>-<MM>` | V01A-R02-03 | `Docs/Validation/` (written later) |

A decision is cited as `ADR-NNN` — three digits, the filename prefix in `documentation/ADRs/`.

- **Every Finding ID, `ADR-NNN` and Rule ID written in a Guide document must resolve** — in prose, in
  tables, in inline code and inside fenced blocks. A Finding ID resolves to a heading that is exactly the
  ID in its review document; an ADR resolves to its file; a Rule ID resolves to a rule block in the Guide.
  This document is the one exception: its examples of Rule IDs and contract-review IDs are not resolved.
- **Plain IDs are enough.** `BE-R06-05` and `ADR-011` are valid citations as written; the validator
  resolves them. A wiki link (`[[Docs/backend/Reviews/06-Query-Engine-Review#BE-R06-05|BE-R06-05]]`) is
  welcome where a reader will want to click through, and it is checked as well.
- To name a whole family of findings, write the pattern in inline code (`BE-R01-*`); a pattern is not an
  ID and is not resolved.
- **Source citations** follow [[Docs/Analysis-Doc-Conventions]] section 3: a code span with the path from
  the repository root and an optional line or range, for example `backend/pom.xml:30`. The checked
  prefixes are the three reference projects, `base-project/` and `documentation/Docs/Validation/`. The
  path must exist and the lines must be inside the file. A path containing `*` is a glob and is not
  checked.
- **Secrets:** never reproduce a secret value. Cite the Finding ID or the location.
- Placeholder text is forbidden outside code, as in [[Docs/Analysis-Doc-Conventions]] section 11.

## 6. Version Notes

Rules are version-neutral. Everything that depends on a framework version — class names, annotations,
property names, default values — goes in `## Version Notes`, and every note says how far it has been
verified ([[ADRs/ADR-003-version-baseline-as-a-support-policy|ADR-003]],
[[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]]).

The section is a list. **Every note is one `- ` list item that starts with a marker**; continuation lines
are indented. A section with no note contains the single line `None.`

```markdown
## Version Notes
- **verified on 4.1.x** — the Upload Coordinator's no-transaction contract is declared with
  `@Transactional(propagation = NEVER)`.
  Evidence: `base-project/src/test/java/**/UploadCoordinatorContractTest.java:40-58`
- **verified on 3.4.x** — the account-flag methods of `UserDetails` are `default` methods that return
  `true`. Evidence: BE-R01-08
- **verified on 4.1.x** — a resource-server decoder is built from a public key with
  `NimbusJwtDecoder.withPublicKey`. Evidence:
  https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/resource-server/jwt.html
- **not verified** — current-docs lookup at execution time: the property that turns SQL logging off.
```

- **`**verified on <line>**`** — `<line>` is a Spring Boot line written `<major>.<minor>.x`. The note
  must cite at least one piece of evidence that resolves:
  1. **a test** — the exact path of a test file (a path containing `/src/test/`) under `base-project/`,
     or under a validation review's evidence pack in `documentation/Docs/Validation/`, with an optional
     line or range. A glob does not count. (The example above uses `**` only because the Base Project
     does not exist yet.)
  2. **a reference Finding ID** — the finding's own `Verified against` line carries the proof.
  3. **a version-tagged documentation URL** — a URL whose path has a version segment (`/7.0/`,
     `/4.1.3/`, `/v4.1.0/`). Write the versioned form even when the site redirects the current line to
     an unversioned address: the versioned URL keeps pointing at the same text after the next release.
     A URL with no version segment proves nothing about a line and does not count.
- **`**not verified**`** — the detail is the author's best knowledge and nothing tests it. Whoever applies
  the rule looks the detail up in the current framework documentation at execution time.
- **One line per note.** A claim about two lines ("3.x does this, 4.x does that") is written as two notes,
  one per line, and is made only when both are `verified`.
- Never promote a note to `verified` without evidence. An unverified note that reads as fact is the
  failure this section exists to prevent.

The number of `not verified` notes is reported in [[Docs/Guide/Guide-Index]] (section 7). It falls as the
Base Project is built and its tests become evidence.

## 7. Guide Index

`Guide-Index.md` is the front page. Required `##` headings, in this order:

```markdown
# Guide — Index

#doc #guide #index

## About the Guide     ← what the Guide is, the support policy in one line, a link to these conventions
## Documents           ← table: number, document, state (planned | draft | final)
## Status              ← the two counts below
## Reviews             ← every Reviews/ document, one line each ("None yet." before the first review)
## Changelog           ← dated lines: documents added or finalised, rules withdrawn or replaced
```

- Every document under `Docs/Guide/` is linked from the index. A planned document is listed by name in
  inline code and becomes a link when its file is written.
- `## Status` carries exactly these two lines, and both numbers must be true:

```markdown
- **Draft documents:** <number of Guide documents tagged #draft>
- **Version Notes not verified:** <number of `not verified` notes in all Guide documents>
```

## 8. Contract Reviews

A contract review judges the Guide's module contracts — interfaces, invariants, error modes and the
interactions between modules — before anything is built on them.

- Files: `Reviews/NN-Title-Words-Review.md`, in the review template of
  [[Docs/Analysis-Doc-Conventions]] section 5, with its severity scale (section 6).
- Finding IDs: `GC-R<NN>-<MM>` — `NN` is the review document number. The `###` heading is exactly the ID.
  Evidence cites the Rule IDs and the Guide sections the finding is about.
- `Reviews/00-Review-Summary.md` is required as soon as one review exists. Required `##` headings, in this
  order: `Overview`, `Findings by Severity`, `Related Documents`. Every contract-review Finding ID appears
  in it.
- The gate (no Critical and no High finding left open) is judged by the reviewer and the owner, not by the
  validator.

## 9. Traceability Matrix

The matrix proves that the defects of the reference projects are covered. It is a table inside a
`## Traceability Matrix` section of the matrix document.

- **Scope** ([[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]]): every 🔴 Critical and 🟠 High
  reference finding, **plus every finding of a security review** (`BE-R01-*`, `BT-R01-*`, `WP-R01-*`)
  whatever its severity. Withdrawn findings are out of scope. Other findings may be listed as well.
- **One row per finding, exactly.** The first cell holds the Finding ID. The rest of the row names at
  least one Rule ID that prevents the defect, or `N/A — <reason>`.
- A row may not map to a withdrawn rule.
- Coverage that is only partial says so in the row (`partial — <what is left>`); it is never presented as
  full coverage.

```markdown
## Traceability Matrix
| Finding | Severity | Rule(s) | Note |
|---|---|---|---|
| BE-R06-05 | 🟡 | G06-03 | |
| BT-R05-07 | 🟠 | G06-03, G06-04 | partial — operator limits are per project |
```

## Validation

```bash
python3 scripts/validate-analysis-docs.py guide        # exit 0 = valid
python3 -m unittest discover -s scripts/tests -v       # the validator's own tests
```

The validator prints one line per error (`<doc path>: <message>`) and exits 1 on any error, 0 otherwise.
It checks `documentation/Docs/Guide/` only:

| Rule | Section |
|---|---|
| The index and this document exist; file names and allowed file kinds; one document per number; no wiki link into `Features/`, `Tasks/` or `Bugs/` | 1 |
| The title line and the `#doc #guide` tags of every Guide document | 2 |
| Required `##` headings present and in order, per file kind | 3, 7, 8 |
| Rule headings are exactly `G<NN>-<MM>`, use the document's `NN`, are unique and carry all required fields | 4 |
| `**Evidence:**` cites at least one reference Finding ID or ADR, and no Superseded or Deprecated ADR | 4 |
| Every Finding ID, `ADR-NNN` and Rule ID resolves, in prose and in code | 5 |
| Citation paths exist and cited lines are inside the file; wiki links and their `#fragment` resolve; no placeholders outside code | 5 |
| Every Version Note is a list item with a marker; a `verified` note cites a test, a Finding ID or a version-tagged URL | 6 |
| Every document is linked from the index; the two Status counts are true | 7 |
| Contract-review findings are exactly `GC-R<NN>-<MM>`, unique, complete and listed in the review summary | 8 |
| Every in-scope reference finding has exactly one matrix row that names a rule in force or `N/A — <reason>` | 9 |

The validator does **not** judge whether a rule is right, whether its evidence really supports it, whether
rule text names a framework API, whether a URL can be reached or says what the note claims, whether a
note makes a claim about two lines, whether the State column of the index agrees with the `#draft` tags
(the tag is the authority), or whether the contract-review gate is met. Those stay with the author and
the reviewer.

## Changelog

- **2026-10-04** — Created by Task 2 (Step 2.1) of the Guide, Base Project and Skill feature. Additions
  beyond [[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]], all enforced by the validator: the
  ban on links to documents that move, the `#draft` tag as the draft marker,
  `**Differs from references:**` as a required field with an explicit `None —` value, the Version Note
  marker grammar and its three kinds of evidence, the two Status counts in the index, the review-summary
  headings for contract reviews, and the row grammar of the traceability matrix.
````

`documentation/Docs/Guide/Guide-Index.md`:

````markdown
# Guide — Index

#doc #guide #index

## About the Guide

The Guide is the architecture and design convention for future Spring Boot REST APIs. It is the only
place where the rules of the convention are stated
([[ADRs/ADR-001-guide-is-the-source-of-truth|ADR-001]]): the Base Project and the Skill cite its Rule IDs
and never restate a rule. The three reference projects are its evidence, not its authority.

Rules are version-neutral. The support floor is the Spring Boot generation that is under open-source
support, with Java 21 or newer ([[ADRs/ADR-003-version-baseline-as-a-support-policy|ADR-003]]).
Version-specific detail lives in each document's Version Notes.

The format of every document is defined in [[Docs/Guide/Guide-Conventions]] and checked by
`python3 scripts/validate-analysis-docs.py guide`.

## Documents

A planned document is named in inline code. It becomes a link when its file is written.

| # | Document | Content | State |
|---|---|---|---|
| 01 | `01-Principles-and-Baseline` | Design principles, the version support policy, how rules are cited | planned |
| 02 | `02-Project-Layout-and-Module-Boundaries` | `platform` and `features`, allowed dependencies, architecture tests | planned |
| 03 | `03-Feature-Module-Anatomy` | The fixed file set of a Feature Module; when to opt out of the CRUD base | planned |
| 04 | `04-CRUD-Base-and-Service-Hooks` | Base controller and service contract, hooks, Access Policy, Row Scope, conditional writes | planned |
| 05 | `05-API-Contract` | Resource naming, status codes, page response, conditional requests, download tickets | planned |
| 06 | `06-Query-Engine` | Query Profile, Row Scope, request forms, operators, bounds | planned |
| 07 | `07-Domain-Model-and-Persistence` | Entities, concurrency token, auditing, constraints, migrations | planned |
| 08 | `08-Identity-Authentication-and-Authorization` | Current User, token verification, local Token Issuer, User Directory, authorization ownership | planned |
| 09 | `09-Errors-and-Validation` | Exception hierarchy, problem details, validation | planned |
| 10 | `10-Object-Storage-and-Uploads` | Storage port and adapters, Upload Coordinator, download tickets | planned |
| 11 | `11-Idempotency` | Keys, fingerprints, states, leases, replay | planned |
| 12 | `12-Configuration-and-Secrets` | Typed properties, fail-fast validation, profiles, no literal secrets | planned |
| 13 | `13-Testing-Strategy` | Test layers, contract tests, authorization matrix, architecture tests | planned |
| 14 | `14-Observability-and-Operations` | Health indicators, logging rules | planned |
| 15 | `15-Recipe-Add-a-Feature` | End-to-end walkthrough of adding one Feature Module | planned |
| 16 | `16-Traceability-Matrix` | Every in-scope reference finding mapped to the rules that prevent it | planned |

## Status

- **Draft documents:** 0
- **Version Notes not verified:** 0

## Reviews

None yet. The contract review of the Guide is written after document 16.

## Changelog

- **2026-10-04** — Index created with the sixteen planned documents. No Guide document is written yet.
````

#### Edge Cases
1. **Case:** the conventions must show a Version Note that cites a Base Project test, and the Base Project
   does not exist — the example path contains `**`, so it is a glob and is not checked. A real note may not
   do this (Step 6, edge case 3); the conventions say so next to the example.
2. **Case:** the conventions name the directory `base-project/` in inline code — not a citation: the
   citation pattern needs at least one character after the slash.
3. **Case:** placeholder words — the conventions refer to [[Docs/Analysis-Doc-Conventions]] section 11
   instead of spelling the forbidden words, because the placeholder check runs on this file too.
4. **Case:** the changelog entry names "Task 2" in plain text, with no wiki link — by the conventions' own
   rule (section 1): Task and Feature documents move.
5. **Case:** the user asks for a format change during Manual Validation — change the conventions **and**
   the matching test and check in the same edit, and add a Changelog line.

---

### Step 11: Probe the validator on the real evidence base

**Goal:** See the `guide` target work on real findings and real ADRs, without leaving a trace.
**Dependencies:** Step 10.

- [x] Run the probe below from the repository root.
- [x] Expect: `exit=1`; three index lines (the probe document is not linked; `Draft documents: 0` but the
  documents contain 1; `Version Notes not verified: 0` but the documents contain 1); `scope errors: 77`;
  then `without the probe: exit=0`.
- [x] Run `git status --short documentation/Docs/Guide` — only the two files of Step 10.
- [x] Save `mutation_check.py` (Testing Considerations) in the scratchpad and run it from the repository
  root — expect `17 mutations, 0 survived`.

**Why this step is critical:**
The unit tests use a four-finding fixture. The probe uses the 241 real findings: `77` is the 78-finding
scope minus the one row the probe maps. A different number means the scope computation and the review
documents disagree, and Task 5 would inherit the surprise.

#### Implementation

````bash
# Run from the repository root. Builds a throwaway copy of the documentation, adds one probe Guide
# document, and runs the guide target against the real evidence base. Nothing in the repository changes.
PROBE="$(mktemp -d)"
cp -r documentation "$PROBE/"
for p in backend BugTracker wpmanager; do ln -s "$PWD/$p" "$PROBE/$p"; done
G="$PROBE/documentation/Docs/Guide"

cat > "$G/06-Query-Engine.md" <<'DOC'
# Query Engine

#doc #guide #api-design #draft

**Guide:** [[Docs/Guide/Guide-Index]] · **Conventions:** [[Docs/Guide/Guide-Conventions]]

## Purpose
Probe document. It exists only to prove the validator on the real evidence base.

## Design
One entry point runs a list query.

## Rules

### G06-03
**Rule:** Every filterable or sortable field is declared once in the entity's Query Profile; anything not
declared is rejected with 400.
**Why:** Unlisted fields let clients probe sensitive columns (password hashes).
**Evidence:** BT-R05-07, BE-R06-05 · ADR-011
**Differs from references:** BugTracker had no whitelist; `backend/` repeated the field name three times.

## Differs From the Reference Projects
- Fields are whitelisted (G06-03).

## Version Notes
- **verified on 3.4.x** — the account-flag methods of `UserDetails` are `default` methods that return
  `true`. Evidence: BE-R01-08
- **not verified** — current-docs lookup at execution time: the predicate executor interface name.

## Traceability Matrix
| Finding | Severity | Rule(s) | Note |
|---|---|---|---|
| BT-R05-07 | 🟠 | G06-03 | |

## Related Documents
- [[ADRs/ADR-011-list-api-get-paging-and-post-search|ADR-011]]
DOC

python3 scripts/validate-analysis-docs.py guide --root "$PROBE" > "$PROBE/out.txt"; echo "exit=$?"
grep -v "traceability scope" "$PROBE/out.txt"
echo "scope errors: $(grep -c 'traceability scope' "$PROBE/out.txt")"
rm "$G/06-Query-Engine.md"
python3 scripts/validate-analysis-docs.py guide --root "$PROBE"; echo "without the probe: exit=$?"
rm -rf "$PROBE"
````

#### Edge Cases
1. **Case:** `scope errors` is not 77 — count the scope by hand before changing code: 🔴 + 🟠 findings
   (`grep -rhc '^\*\*Severity:\*\* 🔴'` and `🟠` over `documentation/Docs/*/Reviews/[0-9][0-9]-*-Review.md`)
   plus the `###` headings of the three `01-*-Review.md` files, minus the overlap. If a finding was added or
   withdrawn since 2026-10-04, the number moves and the code is right.
2. **Case:** the probe rule's Finding IDs (`BT-R05-07`, `BE-R06-05`, `BE-R01-08`) or `ADR-011` fail to
   resolve — they are real IDs (verified at creation); a failure is a defect in `finding_exists` or
   `adr_file`.
3. **Case:** the temporary directory is on a filesystem without symlinks — replace the three `ln -s` lines
   by nothing: the probe document cites no reference source path.

---

### Step 12: Propose the glossary terms

**Goal:** The three words this Task introduces have one definition (user story 52).
**Dependencies:** Step 10. **Needs the user.**

- [x] Show the user the three proposals below and ask which to accept.
- [x] For each accepted term, run its `glossary add` command from the repository root.
- [x] Run `glossary search "<term>"` for each — the term is returned.

**Why this step is critical:**
"Contract review", "traceability matrix" and "draft" are used by the conventions, by the validator's
messages and by Tasks 5 and 12. The glossary skill requires the user's confirmation before every write, so
the Task proposes and does not add.

#### Implementation

```bash
glossary add --term "Contract Review" --category "Guide and Convention" \
  --definition "A blind design review of the Guide's module contracts (interfaces, invariants, error modes and the interactions between modules), written in the review format of the analysis documents under Docs/Guide/Reviews/. Its findings use the IDs GC-R<NN>-<MM>, and it gates the Skill: no Critical and no High finding may stay open." \
  --examples "GC-R01-02: the update contract does not say what happens to a field that is absent from the request." \
  --synonyms "GC review, contract design review" \
  --related "Guide, Module Contract, Finding ID, Rule ID"

glossary add --term "Traceability Matrix" --category "Guide and Convention" \
  --definition "The table in the Guide that maps every in-scope reference Finding to the Guide Rules that prevent it, or to N/A with a reason. In scope: every Critical and High finding, plus every finding of a security review whatever its severity. The validator checks that each in-scope finding has exactly one row." \
  --examples "BT-R05-07 | High | G06-03" \
  --synonyms "coverage matrix" \
  --related "Guide, Guide Rule, Finding, Rule ID"

glossary add --term "Draft Marker" --category "Guide and Convention" \
  --definition "The #draft tag on a Guide document. A draft is complete and binding in its rules and module contracts; its narrative, its Differs From the Reference Projects section and its Version Notes are still to be finalised against the Base Project. The Guide index reports the number of drafts." \
  --examples "#doc #guide #api-design #draft on 06-Query-Engine.md until the document is finalised against the Base Project." \
  --synonyms "draft document" \
  --related "Guide, Version Note, Base Project"
```

#### Edge Cases
1. **Case:** `glossary add` exits 2 — the term exists. Show the user the existing term and ask whether to
   update it.
2. **Case:** the user declines a term — do not add it; say so in the final report. Nothing else in this Task
   depends on the glossary entry.
3. **Case:** checking whether a word is already a synonym — `glossary search` matches term names only
   (`Memory/known-issues.md`); use `grep -n -i "<word>" documentation/Glossary/Glossary.md`.

---

### Step 13: Update the memory bank, tick the parent and close

**Goal:** The next session knows the Guide format exists and how to check it.
**Dependencies:** Steps 1–12.

- [x] `documentation/Memory/tech.md` — in "Analysis-doc tooling", add the line
  `python3 scripts/validate-analysis-docs.py guide   # the Guide (Docs/Guide/), exit 0 = valid`.
- [x] `documentation/Memory/architecture.md` — add a source-map row for `documentation/Docs/Guide/`
  (`Guide-Index.md`, `Guide-Conventions.md`; documents `NN-Title-Words.md`; `Reviews/`), and extend the
  `scripts/validate-analysis-docs.py` row: targets `backend`, `BugTracker`, `wpmanager`, `guide`; tests in
  `scripts/tests/` (two files).
- [x] `documentation/Memory/known-issues.md` — two notes: (a) under "Framework / Library behaviors":
  `docs.spring.io` redirects the versioned URL of the **current** line to the unversioned address (seen
  2026-10-04 for Spring Boot 4.1 and Spring Framework 7.0); a Version Note cites the versioned form;
  (b) under "Architectural constraints": a Guide document never links a Feature, Task or Bug document,
  because those move and the `guide` target would fail on the dangling link.
- [x] `documentation/Memory/context.md` — current focus: Task 2 done; next: Task 3 (Guide 01–05). Keep it
  short.
- [x] `documentation/Memory/progress.md` — prepend `## <date> (Task 2 — Guide format and validator)`: the
  two Guide files, the `guide` target and what it checks, the test count, the user's decisions on the open
  format points, the glossary terms accepted, and a link to this Task.
- [x] `documentation/Memory/product.md` and `brief.md` — read both; no change is expected; `brief.md` is
  never edited.
- [x] In the parent Feature, change `- [ ] **Step 2.1:**` to `- [x] **Step 2.1:**`.
- [x] Run every command of "Automatic Validation" one last time and read the output.
- [x] Tick this Task's completion criteria. Moving the Task to `Tasks/done/` happens when the user asks.

**Why this step is critical:**
`Memory/context.md` names this Task as "next". An agent that starts Task 3 from a stale memory bank would
not know the `guide` target exists and would write Guide documents unchecked.

#### Edge Cases
1. **Case:** the user changed a format point in Manual Validation — the progress entry records the final
   rule, not the Task's proposal.
2. **Case:** the Task is moved to `Tasks/done/` later — update the link in the parent's Task Breakdown and
   in `Memory/progress.md`. No Guide document links this Task, so the `guide` target is not affected.

---

## Design Decisions

**Decision 1:** The `guide` target is added to `scripts/validate-analysis-docs.py`; no second script.
- **Why:** The parent says "extend". The module's interface (`<target> [--root]`, one line per error, three
  exit codes) does not grow, and the Guide reuses seven existing checks. A second script would copy them or
  import from a file whose name has hyphens. Deletion test: remove the `guide` target and its checks would
  have to be re-created in every Guide task as manual greps.
- **Alternatives considered:** a new `scripts/validate-guide-docs.py` (duplicated helpers, two tools to
  remember); renaming the script to a neutral name (touches the parent, the memory bank, the conventions
  and three done Tasks for no behaviour); splitting into a package now (the file is about 670 lines after
  this Task; Task 7 adds the `validation` target and is the better moment to judge).

**Decision 2:** The new tests live in their own file and share no code with the existing test file.
- **Why:** The existing 44 tests stay byte-identical, which keeps them a trustworthy regression net for
  Step 2. The Guide fixture is a different tree (ADRs, a Base Project test, review sets used only as
  evidence). Importing one test module from another depends on how `unittest` was started.
- **Alternatives considered:** adding classes to the existing file (it would pass 1,000 lines and mix two
  fixtures); a shared `conftest`-style helper module (a third file to maintain for about forty shared lines).

**Decision 3:** Plain IDs are citations, and they are resolved everywhere — including inline code and fenced
blocks. `Guide-Conventions.md` is exempt for Rule IDs and contract-review IDs only.
- **Why:** The parent's rule block writes `**Evidence:** BT-R05-07, BE-R06-05 · ADR-011`. With several
  hundred citations, full wiki links would add tens of kilobytes of link syntax to documents that agents
  read as plain text. Resolving plain tokens gives the same guarantee as a link. Checking inside code closes
  the hole the wiki-link check has by design. The conventions must show examples before any rule exists, so
  one file needs one exemption — and it is the narrowest one that works.
- **Alternatives considered:** require wiki links in `Evidence` (the form the ADRs use; clickable in
  Obsidian, heavy to read and write — **the user may prefer it; it is a one-line rule to add**); skip code
  spans as the wiki-link check does (an unchecked citation is the defect this Task exists to prevent);
  exempt the conventions entirely (its Finding IDs and ADRs are real and should stay checked).

**Decision 4:** `**Differs from references:**` is a required field; a rule that keeps the reference shape
writes `None — <what is kept>`.
- **Why:** ADR-001 point 4 and user story 5 ask for every departure to be explicit, and ADR-016 point 4
  lists the field in the block. If the field were optional, a missing field could mean "no departure" or
  "forgot". The project's own grammar is fail-closed: say `None`, as `Known Limitations` does in the
  analysis documents.
- **Alternatives considered:** optional, as the parent's validator list reads (silent omission); only the
  document-level section (a reader of one rule would have to search the section for it).
- **Note for the user:** this is stricter than the parent's check list ("Rule/Why/Evidence"). Confirm it in
  Manual Validation; making it optional is a one-word change in `RULE_FIELDS` and one test.

**Decision 5:** A Version Note is a list item that starts with `**verified on <major>.<minor>.x**` or
`**not verified**`; no text may sit outside an entry; a `verified` note needs an exact test path with
`/src/test/` under `base-project/` or `documentation/Docs/Validation/`, a reference Finding ID, or a URL with
a version segment. URLs are not fetched.
- **Why:** A marker in a fixed position can be parsed without guessing. Forbidding loose text removes the
  place where an unmarked claim would hide. "A test" means a test: citing production code proves that code
  exists, not that it works on the line. A validator that opened URLs would be slow, would fail offline and
  would make the unit tests depend on a website; the version segment is the part a machine can check, and
  the redirect behaviour found on 2026-10-04 makes it the part authors get wrong.
- **Alternatives considered:** a table with a status column (long API statements do not fit cells); a marker
  at the end of the entry (harder to see and to parse); fetching URLs (network in a validator); accepting
  `javap` on a jar as evidence, as the analysis documents do (the parent lists three kinds; a fourth can be
  added when a note needs it).
- **Trade-off:** the line in the marker and the version in a URL are not cross-checked (Step 6, edge case 7).

**Decision 6:** The draft marker is the tag `#draft`; the index carries two status lines whose numbers the
validator recomputes.
- **Why:** ADR-016 point 10 needs a form, and a tag is already the vault's way to mark state (`#current`,
  `#adr-accepted`). Counting drafts in the index gives the owner one place to see how much of the Guide is
  still unfinished, and gives the parent's Step 3.7 a number that must reach zero.
- **Alternatives considered:** a `**Status:** draft` line (a second mechanism next to tags); a `Drafts/`
  directory (moving a file breaks every link to it — the reason for Decision 11).

**Decision 7:** A Rule ID is never renumbered. A rule that changes identity is withdrawn (the heading stays)
and its replacement gets a new ID. The validator does not check the order of rules.
- **Why:** ADR-016's consequence is "IDs can never be reused, so the numbering is a permanent commitment",
  and the Finding ID convention already works this way. A withdrawn heading keeps every old citation
  resolving to the reason. Order is not checked because a later rule belongs next to its topic, not at the
  end.
- **Alternatives considered:** allow renumbering with a changelog entry, as ADR-016 point 5 words it (the
  old ID then resolves nowhere, and the changelog entry itself would fail the ID check); enforce ascending
  order (forces late rules to the end of the section).
- **Relation to ADR-016 point 5:** satisfied, not changed — the changelog entry is still written (index,
  `## Changelog`), and no citation can silently point at a different rule.

**Decision 8:** The traceability scope is checked now, and only inside a `## Traceability Matrix` section.
- **Why:** ADR-016 point 9 makes the target enforce point 7. Building the check here, with a fixture, keeps
  Task 5 a writing Task: it runs the validator and reads a list of unmapped findings. Tying the check to a
  section name instead of a file name means the matrix document can be renamed without touching code.
- **Alternatives considered:** leave it to Task 5 (a 78-row table verified by eye — the situation the
  validator was built to end); require the matrix to exist (it cannot until Task 5; that is a release check).

**Decision 9:** Contract reviews are supported now, with the code `GC` and a three-heading summary.
- **Why:** `check_findings` already takes the code from the context, so the cost is two branches. Without
  them the first review file would be an "unexpected file" and Task 5 would have to extend the validator in
  the middle of a blind review.
- **Alternatives considered:** ignore `Reviews/` (unvalidated findings feeding a gate); reuse the six
  reference-summary headings (two of them have no meaning for a contract review).

**Decision 10:** A Superseded or Deprecated ADR is rejected as evidence.
- **Why:** Accepted ADRs are immutable; a decision changes by a new ADR. The rules that cite the old one are
  then stale, and nothing else in the workspace would notice. Six lines turn the supersede step into a list
  of rules to revisit.
- **Alternatives considered:** only check that the file exists (the drift stays invisible).

**Decision 11:** A Guide document never links a Feature, Task or Bug document.
- **Why:** Those documents move between status directories (`to-do`, `current`, `done`). The parent Feature
  is in `Features/to-do/` today and will move. A validated document that links it would start failing for a
  reason unrelated to the Guide. ADRs do not move and hold the decisions.
- **Enforced:** `check_stable_links` rejects a wiki link into `Features/`, `Tasks/` or `Bugs/` in every
  file under `Docs/Guide/` (test `g02d`). A rule that is only written would fail months later, when the
  Feature moves.
- **Alternatives considered:** allow the links and fix them on every move (the ADRs already carry such links
  and nothing validates them); state the rule without a check (the failure would surface on an unrelated
  day, in an unrelated Task).

**Decision 12:** Deliberately not in this Task.
- Rule-ID citations in Skill files (Task 6, when the files exist); the `validation` target, evidence packs
  and the rule-coverage ledger (Task 7 — `check_rules` already collects what the ledger needs); a release
  mode that fails on any draft or a missing matrix (the parent's Step 6.1); cross-line claims in a single note (a
  judgement, stated as a writing rule).

---

## Testing Considerations

Tests follow the `tdd` skill: each one runs the public interface (the CLI) against a real tree, asserts on
the exit code and on a fragment of the message, and would survive any internal rewrite of the validator.

**Red list per step** (measured at creation by building the validator as it stands after each step and
running all 103 tests):

| After Step | Failing tests (all belong to later steps) |
|---|---|
| 3 | g04 a b c d f h i · g05 a b c d e g · g06 a b c e f g i j · g07 a b c d · g08 b c d e f · g09 b c d e f |
| 4 | g05 a b c d e g · g06 a b c e f g i j · g07 a b c d · g08 b c d e f · g09 b c d e f |
| 5 | g06 a b c e f g i j · g07 a b c d · g08 b c d · g09 b c d e |
| 6 | g07 a b c d · g08 b c d · g09 b c d e |
| 7 | g08 b c d · g09 b c d e |
| 8 | g09 b c d e |
| 9 | none — `Ran 103 tests`, `OK` |

### Automatic Validation

- [x] Run `python3 -m unittest discover -s scripts/tests 2>&1 | tail -3` — expect `Ran 104 tests` and `OK`
  (44 existing + 60 new — the 59 planned plus one added at review)
- [x] Run `python3 -m unittest discover -s scripts/tests -p 'test_validate_guide_docs.py' 2>&1 | tail -3` —
  expect `Ran 60 tests` and `OK`
- [x] Run `python3 scripts/validate-analysis-docs.py guide; echo "exit=$?"` — expect no error line and
  `exit=0`
- [x] Run `for p in backend BugTracker wpmanager; do python3 scripts/validate-analysis-docs.py "$p" >/dev/null; echo "$p exit=$?"; done`
  — expect exit 0 three times (no regression)
- [x] Run `python3 scripts/validate-analysis-docs.py nope; echo "exit=$?"` — expect `unknown target 'nope'`
  naming `guide` among the targets, and `exit=2`
- [x] Run `git diff --stat -- scripts/tests/test_validate_analysis_docs.py documentation/Docs/Analysis-Doc-Conventions.md`
  — expect no output (neither file was edited)
- [x] Run the probe of Step 11 — expect `exit=1`, three index lines, `scope errors: 77`,
  `without the probe: exit=0`
- [x] Run `python3 <scratchpad>/mutation_check.py` from the repository root — expect seventeen `KILLED` lines
  and `17 mutations, 0 survived`
- [x] Run `for s in backend bugtracker wpmanager; do sha256sum --quiet -c scripts/.snapshots/$s.sha256 && echo "$s unchanged"; done`
  — expect three `unchanged` lines
- [x] Run ``grep -c '^| [0-9][0-9] | `' documentation/Docs/Guide/Guide-Index.md`` — expect `16` (the
  sixteen planned documents)
- [x] Run `grep -rn -i -E "password\s*[:=]|secret\s*[:=]\s*\S|AKIA[0-9A-Z]{12}" documentation/Docs/Guide scripts/tests/test_validate_guide_docs.py`
  — expect no output
- [x] Run `git status --short` — the only new or changed paths are `scripts/validate-analysis-docs.py`,
  `scripts/tests/test_validate_guide_docs.py`, `documentation/Docs/Guide/`, the parent Feature, this Task,
  the memory files, and the glossary if a term was accepted (plus the paths already modified before the
  Task started)

`mutation_check.py` (scratch file). It seeds one defect at a time into a temporary copy of the validator and
reports which tests notice. At creation all seventeen were killed:

````python
"""Seed one defect at a time into a copy of the validator and confirm the guide tests notice it.

    python3 mutation_check.py        # run from the repository root; writes only to a temporary directory
"""
import pathlib, re, shutil, subprocess, sys, tempfile

SOURCE = pathlib.Path("scripts/validate-analysis-docs.py").read_text(encoding="utf-8")
TESTS = pathlib.Path("scripts/tests/test_validate_guide_docs.py")
MUTATIONS = {
    "retired ADR accepted as evidence": ("if status and status.group(1) in RETIRED_ADR_STATUSES:", "if False:"),
    "any path counts as a test": ('and "/src/test/" in raw', ""),
    "a glob counts as evidence": ('if "*" not in raw and raw.startswith', "if raw.startswith"),
    "any URL counts as version-tagged": (
        'return any(VERSION_SEGMENT.search(re.sub(r"^https?://[^/]+", "", url)) for url in URL.findall(entry))',
        "return bool(URL.findall(entry))"),
    "security reviews out of the matrix scope": ('SECURITY_REVIEW_NN = "01"', 'SECURITY_REVIEW_NN = "99"'),
    "Critical and High out of the matrix scope": ('or "🔴" in severity or "🟠" in severity', ""),
    "conventions examples resolved": ('None if kind == "conventions" else rules', "rules"),
    "duplicate document number allowed": ("if file_nn in numbers:", "if False:"),
    "withdrawn rule accepted in the matrix": ("if rid in rules and rules[rid][1]:", "if False:"),
    "evidence not required": ("if not findings and not adrs:", "if False:"),
    "text outside a Version Note allowed": ("for line in stray:", "for line in []:"),
    "review summary optional": ("elif seen_ids:", "elif False:"),
    "status counts unchecked": ("elif int(match.group(1)) != actual:", "elif False:"),
    "IDs inside code unchecked": (
        "for code, nn, mm in sorted(set(FINDING_TOKEN.findall(text))):",
        "for code, nn, mm in sorted(set(FINDING_TOKEN.findall(prose_only(text)))):"),
    "links to moving documents allowed": ("for match in MOVING_LINK.finditer(prose_only(text)):", "for match in []:"),
    "tags unchecked": ('or "#doc" not in tags or "#guide" not in tags:', ":"),
    "N/A without a reason accepted": ('re.search(r"N/A\\s*—\\s*\\S", rest)', 're.search(r"N/A", rest)'),
}

survivors = 0
with tempfile.TemporaryDirectory() as tmp:
    scripts = pathlib.Path(tmp) / "scripts"
    (scripts / "tests").mkdir(parents=True)
    shutil.copy(TESTS, scripts / "tests")
    for name, (old, new) in MUTATIONS.items():
        if old not in SOURCE:
            print(f"ANCHOR NOT FOUND  {name} — the code differs from the Task's listing; adapt this entry")
            survivors += 1
            continue
        (scripts / "validate-analysis-docs.py").write_text(SOURCE.replace(old, new, 1), encoding="utf-8")
        run = subprocess.run([sys.executable, "-m", "unittest", "discover", "-s", "scripts/tests"],
                             cwd=tmp, capture_output=True, text=True)
        failed = sorted(set(re.findall(r"^(?:FAIL|ERROR): (test_\w+)", run.stderr, re.MULTILINE)))
        survivors += run.returncode == 0
        print(f"{'KILLED  ' if run.returncode else 'SURVIVED'}  {name:42s} {', '.join(f[5:9] for f in failed)}")
print(f"{len(MUTATIONS)} mutations, {survivors} survived")
sys.exit(1 if survivors else 0)
````

### Manual Validation

- [x] **(User)** Read `documentation/Docs/Guide/Guide-Conventions.md` and approve the format, or ask for
  changes. Four points are this Task's proposals, not the parent's text, and deserve a deliberate yes
  (Step 1 asks for them before any code is written; this is the final approval on the written document):
  1. citations are **plain IDs** resolved by the validator, wiki links optional (Design Decision 3);
  2. `**Differs from references:**` is **required**, with `None — …` when nothing differs (Decision 4);
  3. the Version Note grammar: marker first, no text outside an entry, three kinds of evidence, URLs judged
     by their version segment (Decision 5);
  4. `#draft` as the draft marker and the two counts in the index (Decision 6).
- [x] **(User)** Open `Guide-Conventions.md` and `Guide-Index.md` in Obsidian: the tags appear, the links
  to ADR-001, ADR-003 and ADR-016 open, the tables and the fenced templates render.
- [x] **(User)** Glossary: accept or decline *Contract Review*, *Traceability Matrix* and *Draft Marker*
  (Step 12).

**Rule:** Run automatic checks when possible. If validation requires manual testing, document the steps here
for the user and do not attempt to execute those manual tests yourself.

---

## Related Code Explanations

No code-explanation documents exist (`documentation/Code/` is not used in this workspace). Related
documents and code:

- [[Features/to-do/Spring-Boot-Architecture-Guide-and-Base-Project]] — section 2 (format and validator),
  section 3 (the sixteen documents), section 15 and Testing Decisions (the later `validation` target).
- [[ADRs/ADR-016-guide-document-format-and-rule-ids|ADR-016]] — the ten decision points this Task
  implements.
- [[Docs/Analysis-Doc-Conventions]] — the format the Guide conventions reuse by reference.
- [[Tasks/done/Spring-Boot-Architecture-Guide-and-Base-Project-step-1-decisions-adrs-glossary]] — the ADR
  set and the `ADR-NNN` citation form; its scratch `check_adrs.py` is the prior art for ADR and Finding ID
  resolution.
- `scripts/validate-analysis-docs.py:136-220` — the checks the `guide` target reuses.
- `scripts/tests/test_validate_analysis_docs.py:178-248` — the test-case pattern the new test file follows.

---

## Post-Review Notes

Autonomous review after execution (task-executor Step 7). The implementation matches the Task's listings;
every automatic validation command was run and read.

- **One test gap found and filled.** `check_evidence` filters contract-review findings out of evidence
  (Step 4, edge case 3: `GC-R01-*` is never evidence for a rule), but only the empty-evidence branch was
  tested (`g04h`). A mutation of the filter survived the 59 planned tests. Added
  `test_g04j_contract_finding_is_not_evidence`, which kills that mutation (verified). **Consequence:** the
  suite is 60 guide tests / 104 total, not the 59 / 103 this document planned; the counts above are updated.
  The intermediate slice counts in the step-by-step text remain the measurements taken at each step as
  executed, before the review test existed.
- **The four format points were confirmed by the user before any code was written** (Step 1's request,
  answered at the start of execution): plain IDs as citations, `**Differs from references:**` required with
  `None —`, the Version Note marker grammar with its three kinds of evidence, and `#draft` plus the two
  recomputed index counts. The glossary terms *Contract Review*, *Traceability Matrix* and *Draft Marker*
  were all accepted and added through the `glossary` CLI.
- **No architectural problem found.** The `guide` target stayed inside the validator's existing interface;
  no out-of-scope refactor was needed. The three project targets and the 44 existing tests are untouched
  (`git diff` on `scripts/tests/test_validate_analysis_docs.py` and `Docs/Analysis-Doc-Conventions.md` is
  empty; the three reference-project snapshots are unchanged).
- **Status: DONE (2026-10-04).** All automatic completion criteria are met and the user confirmed the
  Manual Validation items: the written format is approved and both Guide files render correctly in
  Obsidian. The glossary terms were accepted before execution. The Task is moved to `Tasks/done/` at the
  user's request.

---

## Completion Criteria

- [x] Parent document reviewed and reflected accurately in this task
- [x] Relevant skills reviewed and selected for this task
- [x] Up-to-date documentation reviewed for the affected technologies (Python 3.10 `re`; the Spring
  documentation URL shapes)
- [x] `scripts/validate-analysis-docs.py` accepts the target `guide`; the three project targets behave as
  before
- [x] The `guide` target checks: file kinds and names; one document per number; title and tags; the six
  headings; no wiki link into `Features/`, `Tasks/` or `Bugs/`; rule blocks (ID form, document number, uniqueness, four fields, withdrawn form); evidence
  (at least one reference Finding ID or ADR, no retired ADR); every Finding ID, ADR and Rule ID resolves;
  Version Note markers and evidence; index coverage and the two status counts; contract reviews and their
  summary; the traceability scope
- [x] `scripts/tests/test_validate_guide_docs.py` exists with 60 tests; the full suite runs 104 tests, all
  passing; the 44 existing tests are untouched
- [x] `documentation/Docs/Guide/Guide-Conventions.md` and `Guide-Index.md` exist and the `guide` target
  exits 0
- [x] The probe reports 77 scope errors and leaves no file behind; the mutation check reports 0 survivors
- [x] The user approved the format (the four points of Manual Validation) or the requested changes were
  applied to the conventions, the tests and the validator together
- [x] Glossary proposals handled as the user decided
- [x] Memory bank updated (`tech`, `architecture`, `known-issues`, `context`, `progress`); `brief.md`
  untouched
- [x] The reference projects are unchanged (three snapshot checks)
- [x] All implementation steps checked off
- [x] Automatic validation passes
- [x] Manual validation steps documented for the user and performed by the user — done (2026-10-04): the
  user confirmed the written format and the Obsidian rendering of both Guide files
- [x] Code explanation files updated (if new files created) — not applicable: `documentation/Code/` is not
  used
- [x] Parent Feature Step 2.1 ticked; Task 2 linked to this document in the parent's Task Breakdown
