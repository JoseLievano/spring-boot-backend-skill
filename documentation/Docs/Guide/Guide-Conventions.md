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
