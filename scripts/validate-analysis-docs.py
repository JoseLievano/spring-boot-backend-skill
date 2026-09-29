#!/usr/bin/env python3
"""Validate Explanation/Review docs for one reference project against
documentation/Docs/Analysis-Doc-Conventions.md.

Usage:
    python3 scripts/validate-analysis-docs.py <project> [--root PATH]

<project> is one of: backend, BugTracker, wpmanager.
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


# --------------------------------------------------------------------------- context

class Context:
    """Repository paths and a lazily built index of documentation files for link resolution."""

    def __init__(self, root: Path, project: str):
        self.root = root
        self.project = project
        self.code = PROJECT_CODES[project]
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


def check_citations(ctx: Context, doc: Path, text: str, errors: List[str]) -> None:
    for match in CITATION.finditer(text):
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
    body = section(text, "Findings")
    blocks = re.split(r"^### ", body, flags=re.MULTILINE)[1:]
    for block in blocks:
        heading, _, content = block.partition("\n")
        fid = heading.strip()
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
        summary_text = ctx.text(summary_path)
        for fid, where in sorted(seen_ids.items()):
            if not re.search(rf"(?<![\w-]){re.escape(fid)}(?![\w-])", summary_text):
                errors.append(f"{ctx.rel(summary_path)}: finding {fid} (from {where.name}) "
                              f"is not listed in {SUMMARY_NAME}")

    if index_path.is_file():
        linked = set()
        for match in WIKI_LINK.finditer(prose_only(ctx.text(index_path))):
            path, _ = ctx.resolve_wiki(match.group(1))
            if path is not None:
                linked.add(path.resolve())
        for doc in docs:
            if doc != index_path and doc.resolve() not in linked:
                errors.append(f"{ctx.rel(index_path)}: does not link {ctx.rel(doc)}")
    return errors


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("project", help="reference project: " + ", ".join(PROJECT_CODES))
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parent.parent,
                        help="repository root (default: parent of scripts/)")
    args = parser.parse_args(argv)
    if args.project not in PROJECT_CODES:
        print(f"unknown project '{args.project}'; expected one of {sorted(PROJECT_CODES)}",
              file=sys.stderr)
        return 2
    errors = validate(args.root.resolve(), args.project)
    for error in errors:
        print(error)
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
