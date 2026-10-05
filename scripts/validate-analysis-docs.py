#!/usr/bin/env python3
"""Validate the analysis docs of one reference project, or the Guide, against their conventions.

Usage:
    python3 scripts/validate-analysis-docs.py <target> [--root PATH]

<target> is a reference project (backend, BugTracker, wpmanager), checked against
documentation/Docs/Analysis-Doc-Conventions.md, or `guide`, checked against
documentation/Docs/Guide/Guide-Conventions.md.
Prints one line per error (`<doc path>: <message>`). Exit codes: 0 = valid, 1 = errors, 2 = usage error.
Stdlib only, Python >= 3.9.
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path
from typing import Dict, List, Optional, Tuple

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
WITHDRAWN_FIELDS = ["**Title:**", "**Status:**"]

SUMMARY_NAME = "00-Review-Summary.md"
EXPLANATION_NAME = re.compile(r"^\d{2}-[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*\.md$")
REVIEW_NAME = re.compile(r"^(\d{2})-[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*-Review\.md$")

FINDING_ID = re.compile(r"^([A-Z]{2})-R(\d{2})-(\d{2})$")
# Paths may contain spaces (e.g. wpManagerDocs notes); the line suffix is optional.
CITATION = re.compile(r"`((?:backend|BugTracker|wpmanager)/[^`]+?)(?::(\d+)(?:-(\d+))?)?`")
WIKI_LINK = re.compile(r"\[\[([^\]|#]+)(?:#([^\]|]*))?(?:\|[^\]]*)?\]\]")
PLACEHOLDER = re.compile(r"\b(TODO|TBD|FIXME)\b|lorem ipsum|<fill|\[\.\.\.\]", re.IGNORECASE)
FENCE = re.compile(r"^```.*?^```[^\n]*$", re.MULTILINE | re.DOTALL)
INLINE_CODE = re.compile(r"`[^`\n]+`")
ANY_HEADING = re.compile(r"^#{1,6} (.+?)\s*$", re.MULTILINE)

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


# --------------------------------------------------------------------------- text helpers

def strip_fences(text: str) -> str:
    """Remove fenced blocks, keeping their line breaks so line numbers stay meaningful."""
    return FENCE.sub(lambda m: "\n" * m.group(0).count("\n"), text)


def prose_only(text: str) -> str:
    """Text with fenced blocks and inline code removed — used for placeholder and link scans."""
    return INLINE_CODE.sub("", strip_fences(text))


def h2_headings(text: str) -> List[str]:
    return re.findall(r"^## (.+?)\s*$", strip_fences(text), re.MULTILINE)


def all_headings(text: str) -> List[str]:
    return ANY_HEADING.findall(strip_fences(text))


def section(text: str, heading: str) -> str:
    """Body of the `## <heading>` section (fences stripped), up to the next `## ` heading."""
    body = strip_fences(text)
    match = re.search(rf"^## {re.escape(heading)}\s*$", body, re.MULTILINE)
    if not match:
        return ""
    rest = body[match.end():]
    nxt = re.search(r"^## ", rest, re.MULTILINE)
    return rest[: nxt.start()] if nxt else rest


def h3_blocks(body: str) -> List[Tuple[str, str]]:
    """(heading, content) of every `### ` block inside a section body."""
    blocks = []
    for block in re.split(r"^### ", body, flags=re.MULTILINE)[1:]:
        heading, _, content = block.partition("\n")
        blocks.append((heading.strip(), content))
    return blocks


def field_text(content: str, field: str) -> Optional[str]:
    """Text of one `**Field:**` of a block, up to the next field; None when the field is absent."""
    match = re.search(rf"^{re.escape(field)}", content, re.MULTILINE)
    if not match:
        return None
    rest = content[match.end():]
    nxt = FIELD_START.search(rest)
    return rest[: nxt.start()] if nxt else rest


# --------------------------------------------------------------------------- context

class Context:
    """Repository paths and a lazily built index of documentation files for link resolution."""

    def __init__(self, root: Path, project: str, code: Optional[str] = None):
        self.root = root
        self.project = project
        self.code = code or PROJECT_CODES[project]
        self.documentation = root / "documentation"
        self.docs_dir = self.documentation / "Docs" / project
        self._by_basename: Optional[Dict[str, List[Path]]] = None
        self._text_cache: Dict[Path, str] = {}

    def rel(self, path: Path) -> str:
        try:
            return str(path.relative_to(self.root))
        except ValueError:
            return str(path)

    def text(self, path: Path) -> str:
        if path not in self._text_cache:
            self._text_cache[path] = path.read_text(encoding="utf-8")
        return self._text_cache[path]

    def resolve_wiki(self, target: str) -> Tuple[Optional[Path], Optional[str]]:
        """Return (path, error). Full path from documentation/ first; a bare name (no '/') falls back to
        a unique basename match (Obsidian shortest-path links). A path-style link that does not exist is
        an error, never re-resolved by basename, because basenames repeat across projects."""
        target = target.strip().rstrip("\\").strip()
        if target.endswith(".md"):
            target = target[:-3]
        direct = self.documentation / f"{target}.md"
        if direct.is_file():
            return direct, None
        if self._by_basename is None:
            self._by_basename = {}
            for md in self.documentation.rglob("*.md"):
                self._by_basename.setdefault(md.stem, []).append(md)
        matches = self._by_basename.get(Path(target).name, []) if "/" not in target else []
        if len(matches) == 1:
            return matches[0], None
        if len(matches) > 1:
            return None, f"wiki link [[{target}]] is ambiguous ({len(matches)} files share that name)"
        return None, f"wiki link [[{target}]] does not resolve to a doc"


# --------------------------------------------------------------------------- checks

def check_headings(ctx: Context, doc: Path, text: str, required: List[str], errors: List[str]) -> None:
    """Required `##` headings must all be present, in order; extra `##` headings are tolerated."""
    found = [h for h in h2_headings(text) if h in required]
    missing = [h for h in required if h not in found]
    if missing:
        errors.append(f"{ctx.rel(doc)}: missing headings {missing}")
    elif found != required:
        errors.append(f"{ctx.rel(doc)}: headings out of order {found}")


def check_citations(ctx: Context, doc: Path, text: str, errors: List[str],
                    pattern: re.Pattern[str] = CITATION) -> None:
    for match in pattern.finditer(text):
        raw, start, end = match.group(1), match.group(2), match.group(3)
        shown = match.group(0).strip("`")
        if "*" in raw:
            continue
        path = ctx.root / raw.rstrip("/")
        if start is None:
            if not path.exists():
                errors.append(f"{ctx.rel(doc)}: citation `{shown}` — path does not exist")
            continue
        if not path.is_file():
            reason = "path does not exist" if not path.exists() else "path is not a file"
            errors.append(f"{ctx.rel(doc)}: citation `{shown}` — {reason}")
            continue
        first, last = int(start), int(end) if end else int(start)
        count = len(path.read_text(encoding="utf-8", errors="replace").splitlines())
        if first < 1 or last < first:
            errors.append(f"{ctx.rel(doc)}: citation `{shown}` — invalid line range")
        elif last > count:
            errors.append(f"{ctx.rel(doc)}: citation `{shown}` — beyond end of file ({count} lines)")


def check_wiki_links(ctx: Context, doc: Path, text: str, errors: List[str]) -> None:
    for match in WIKI_LINK.finditer(prose_only(text)):
        target, fragment = match.group(1), match.group(2)
        path, error = ctx.resolve_wiki(target)
        if error:
            errors.append(f"{ctx.rel(doc)}: {error}")
            continue
        if fragment is None:
            continue
        fragment = fragment.rstrip("\\").strip()
        if fragment and fragment not in all_headings(ctx.text(path)):
            errors.append(f"{ctx.rel(doc)}: wiki link [[{target.strip()}#{fragment}]] — "
                          f"no heading '{fragment}' in {ctx.rel(path)}")


def check_placeholders(ctx: Context, doc: Path, text: str, errors: List[str]) -> None:
    for lineno, line in enumerate(prose_only(text).splitlines(), start=1):
        found = PLACEHOLDER.search(line)
        if found:
            errors.append(f"{ctx.rel(doc)}:{lineno}: placeholder text '{found.group(0)}' outside code")


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


# --- guide checks (documentation/Docs/Guide/, see Guide-Conventions.md) --------------------

def finding_exists(ctx: Context, code: str, nn: str, fid: str) -> bool:
    """A Finding ID resolves when its review document has a heading that is exactly the ID."""
    home = GUIDE_DIR if code == GUIDE_CODE else {v: k for k, v in PROJECT_CODES.items()}[code]
    reviews = ctx.documentation / "Docs" / home / "Reviews"
    return any(fid in all_headings(ctx.text(path)) for path in sorted(reviews.glob(f"{nn}-*-Review.md")))


def adr_file(ctx: Context, digits: str) -> Optional[Path]:
    """`ADR-NNN` resolves by filename prefix: documentation/ADRs/ADR-NNN-*.md."""
    matches = sorted((ctx.documentation / "ADRs").glob(f"ADR-{digits}-*.md"))
    return matches[0] if matches else None


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


def check_guide_status(ctx: Context, index_path: Path, label: str, actual: int, errors: List[str]) -> None:
    """The index reports a count on a line `- **<label>:** <n>`; it must equal the computed value."""
    match = re.search(rf"^(?:- )?\*\*{re.escape(label)}:\*\*\s*(\d+)\s*$", ctx.text(index_path), re.MULTILINE)
    if not match:
        errors.append(f"{ctx.rel(index_path)}: missing status line '- **{label}:** <count>'")
    elif int(match.group(1)) != actual:
        errors.append(f"{ctx.rel(index_path)}: reports '{label}: {match.group(1)}' "
                      f"but the documents contain {actual}")


# --------------------------------------------------------------------------- pipeline

def classify(ctx: Context, doc: Path) -> Tuple[Optional[str], Optional[str]]:
    """Return (kind, error). Kinds: index, explanation, summary, review."""
    rel_parts = doc.relative_to(ctx.docs_dir).parts
    name = doc.name
    if rel_parts == (f"{ctx.project}-Index.md",):
        return "index", None
    if len(rel_parts) == 2 and rel_parts[0] == "Explanations":
        if EXPLANATION_NAME.match(name):
            return "explanation", None
        return None, "bad file name (expected Explanations/NN-Title-Words.md)"
    if len(rel_parts) == 2 and rel_parts[0] == "Reviews":
        if name == SUMMARY_NAME:
            return "summary", None
        if REVIEW_NAME.match(name):
            return "review", None
        return None, "bad file name (expected Reviews/NN-Title-Words-Review.md)"
    return None, ("unexpected file: only <project>-Index.md, Explanations/*.md and Reviews/*.md "
                  "are allowed (see Analysis-Doc-Conventions)")


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


if __name__ == "__main__":
    sys.exit(main())
