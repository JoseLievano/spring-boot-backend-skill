"""Tests for scripts/validate-analysis-docs.py.

Each test builds a throwaway repository tree (reference project sources + one analysis doc set) in a
temporary directory and runs the validator CLI against it with --root.

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

CODES = {"backend": "BE", "BugTracker": "BT", "wpmanager": "WP"}


def explanation(project: str) -> str:
    code = CODES[project]
    return textwrap.dedent(f"""\
        # Overview — {project}

        #doc #explanation #ref-{project.lower()} #architecture

        **Project:** `{project}/` · **Index:** [[Docs/{project}/{project}-Index]]

        ## Summary
        The app starts in `{project}/src/App.java:1-3` and is built by `{project}/pom.xml:2`.

        ## Why It Is Built This Way
        Convention over configuration, see `{project}/src`.

        ## How It Works
        ```java
        // `{project}/src/App.java:2`
        // TODO quoted from code is fine inside a fence
        ```
        The mapper says `//TODO Finish mapper` and that is fine in inline code.

        ## Key Components
        | Component | Path | Responsibility |
        |---|---|---|
        | App | `{project}/src/App.java` | entry point |

        ## Conventions and Rules
        One module per feature.

        ### A free sub-heading
        Allowed.

        ## How to Replicate
        1. Create `{project}/src/App.java`.

        ## Known Limitations
        See [[Docs/{project}/Reviews/01-Security-Review#{code}-R01-01|{code}-R01-01]].

        ## Related Documents
        - [[Docs/{project}/Reviews/01-Security-Review]]
        - [[01-Security-Review|short link resolved by unique basename]]
        """)


def finding(fid: str, title: str = "Anonymous token endpoint") -> str:
    return textwrap.dedent(f"""\
        ### {fid}
        **Title:** {title}
        **Severity:** 🔴 Critical
        **Category:** security
        **Principle:** —
        **Evidence:** `PROJECT/src/App.java:2`
        **Impact:** Anyone can mint a token.
        **Recommendation:** Require authentication.
        **Verified against:** N/A — no library API involved
        **Confidence:** Confirmed (read in code)
        """)


def review(project: str, findings: list[str]) -> str:
    body = "\n".join(findings).replace("PROJECT", project)
    return textwrap.dedent(f"""\
        # Security Review — {project}

        #doc #review #ref-{project.lower()} #security

        **Explained in:** [[Docs/{project}/Explanations/01-Overview-and-Design-Philosophy]]

        ## Scope
        Everything in `{project}/src`.

        ## Verdict
        Weak.

        ## Strengths to Keep
        - Small entry point `{project}/src/App.java:1`.

        ## Findings Summary
        | ID | Finding | Severity | Category | Confidence |
        |---|---|---|---|---|

        ## Findings

        """) + body + textwrap.dedent("""
        ## Recommended Target Pattern
        Deny by default.

        ## Related Documents
        - [[Docs/PROJECT/Explanations/01-Overview-and-Design-Philosophy#How It Works|How it works]]
        """).replace("PROJECT", project)


def summary(project: str, ids: list[str]) -> str:
    rows = "\n".join(
        f"| [[Docs/{project}/Reviews/01-Security-Review#{i}\\|{i}]] | t | 🔴 Critical | Security |"
        for i in ids)
    return textwrap.dedent(f"""\
        # Review Summary — {project}

        #doc #review #review-summary #ref-{project.lower()}

        ## Overview
        One critical.

        ## Findings by Severity
        | ID | Title | Severity | Review |
        |---|---|---|---|
        """) + rows + textwrap.dedent("""

        ## Extra Project Section
        Allowed between required ones.

        ## Strengths to Keep
        - Small.

        ## Top 5 Recommendations
        1. Deny by default.

        ## Candidate Patterns for the Skill
        - None.

        ## Related Documents
        - [[Docs/Analysis-Doc-Conventions]]
        """)


def index(project: str) -> str:
    return textwrap.dedent(f"""\
        # {project} — Analysis Index

        #doc #index #ref-{project.lower()}

        ## About the Project
        A service.

        ## Reading Order
        1. [[Docs/{project}/Explanations/01-Overview-and-Design-Philosophy]]

        ## Explanations
        - [[Docs/{project}/Explanations/01-Overview-and-Design-Philosophy|Overview]]

        ## Reviews
        - [[Docs/{project}/Reviews/00-Review-Summary]]
        - [[Docs/{project}/Reviews/01-Security-Review]]

        ## Findings Count
        | Severity | Count |
        |---|---|
        | 🔴 Critical | 1 |

        ## Merges and Skips
        None.
        """)


class ValidatorTestCase(unittest.TestCase):
    project = "backend"

    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.root = Path(self._tmp.name)
        p = self.project
        code = CODES[p]
        self.write(f"{p}/pom.xml", "<project>\n  <version>1</version>\n</project>\n")
        self.write(f"{p}/src/App.java", "class App {\n  void run() {}\n}\n")
        self.write("documentation/Docs/Analysis-Doc-Conventions.md", "# Conventions\n")
        self.docs = f"documentation/Docs/{p}"
        self.write(f"{self.docs}/{p}-Index.md", index(p))
        self.write(f"{self.docs}/Explanations/01-Overview-and-Design-Philosophy.md", explanation(p))
        self.write(f"{self.docs}/Reviews/01-Security-Review.md", review(p, [finding(f"{code}-R01-01")]))
        self.write(f"{self.docs}/Reviews/00-Review-Summary.md", summary(p, [f"{code}-R01-01"]))

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

    @property
    def expl(self) -> str:
        return f"{self.docs}/Explanations/01-Overview-and-Design-Philosophy.md"

    @property
    def rev(self) -> str:
        return f"{self.docs}/Reviews/01-Security-Review.md"

    @property
    def summ(self) -> str:
        return f"{self.docs}/Reviews/00-Review-Summary.md"

    @property
    def idx(self) -> str:
        return f"{self.docs}/{self.project}-Index.md"

    def run_validator(self, project: str | None = None):
        return subprocess.run(
            [sys.executable, str(VALIDATOR), project or self.project, "--root", str(self.root)],
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


class TestValidTree(ValidatorTestCase):
    def test_01_valid_minimal_tree_passes(self):
        self.assertPasses()


class TestCitations(ValidatorTestCase):
    def test_02_citation_to_missing_file_fails(self):
        self.edit(self.expl, "`backend/src/App.java:1-3`", "`backend/src/Missing.java:1`")
        self.assertFailsWith("01-Overview-and-Design-Philosophy.md", "backend/src/Missing.java")

    def test_03a_citation_line_beyond_file_fails(self):
        self.edit(self.expl, "`backend/pom.xml:2`", "`backend/pom.xml:4`")
        self.assertFailsWith("backend/pom.xml:4", "3 lines")

    def test_03b_citation_range_end_beyond_file_fails(self):
        self.edit(self.expl, "`backend/src/App.java:1-3`", "`backend/src/App.java:2-9`")
        self.assertFailsWith("backend/src/App.java:2-9")

    def test_03c_citation_inside_fence_is_checked(self):
        self.edit(self.expl, "// `backend/src/App.java:2`", "// `backend/src/App.java:99`")
        self.assertFailsWith("backend/src/App.java:99")

    def test_03d_reversed_range_fails(self):
        self.edit(self.expl, "`backend/src/App.java:1-3`", "`backend/src/App.java:3-1`")
        self.assertFailsWith("backend/src/App.java:3-1")

    def test_03e_line_citation_on_directory_fails(self):
        self.edit(self.expl, "`backend/src/App.java:1-3`", "`backend/src:1`")
        self.assertFailsWith("backend/src:1", "not a file")

    def test_03f_crlf_line_endings_counted_correctly(self):
        self.write("backend/pom.xml", "a\r\nb\r\nc\r\n")
        self.assertPasses()

    def test_10a_lineless_citation_to_missing_path_fails(self):
        self.edit(self.expl, "`backend/src`", "`backend/nowhere`")
        self.assertFailsWith("backend/nowhere")

    def test_10b_glob_citation_is_skipped(self):
        self.edit(self.expl, "`backend/src`", "`backend/src/**/*.java`")
        self.assertPasses()

    def test_10c_path_with_spaces_is_checked(self):
        self.write("backend/my notes/a.md", "one\n")
        self.edit(self.expl, "`backend/src`", "`backend/my notes/a.md:1`")
        self.assertPasses()


class TestWikiLinks(ValidatorTestCase):
    def test_04a_link_to_missing_doc_fails(self):
        self.edit(self.expl, "- [[Docs/backend/Reviews/01-Security-Review]]",
                  "- [[Docs/backend/Reviews/99-Nope-Review]]")
        self.assertFailsWith("Docs/backend/Reviews/99-Nope-Review")

    def test_04b_alias_is_stripped_and_escaped_pipe_supported(self):
        # the fixture already uses aliases (|Overview) and escaped table pipes (\|) — must pass
        self.assertPasses()

    def test_04c_ambiguous_basename_fails(self):
        self.write("documentation/Docs/Other/01-Security-Review.md", "# other\n")
        self.assertFailsWith("ambiguous")

    def test_04d_links_inside_code_are_not_checked(self):
        self.edit(self.expl, "Convention over configuration",
                  "Template: `[[Docs/<project>/<project>-Index]]`. Convention over configuration")
        self.assertPasses()


class TestHeadings(ValidatorTestCase):
    def test_05a_missing_explanation_heading_fails(self):
        self.edit(self.expl, "## How to Replicate\n", "## How To Copy\n")
        self.assertFailsWith("missing headings", "How to Replicate")

    def test_05b_out_of_order_headings_fail(self):
        text = self.read(self.expl)
        text = text.replace("## Summary\n", "## TMP\n").replace("## Why It Is Built This Way\n", "## Summary\n")
        text = text.replace("## TMP\n", "## Why It Is Built This Way\n")
        self.write(self.expl, text)
        self.assertFailsWith("out of order")

    def test_05c_missing_review_heading_fails(self):
        self.edit(self.rev, "## Verdict\n", "## Opinion\n")
        self.assertFailsWith("01-Security-Review.md", "Verdict")

    def test_05d_missing_summary_heading_fails(self):
        self.edit(self.summ, "## Top 5 Recommendations\n", "## Top Recommendations\n")
        self.assertFailsWith("00-Review-Summary.md", "Top 5 Recommendations")

    def test_05e_missing_index_heading_fails(self):
        self.edit(self.idx, "## Merges and Skips\n", "## Skips\n")
        self.assertFailsWith("backend-Index.md", "Merges and Skips")

    def test_05f_heading_inside_fence_does_not_count(self):
        self.edit(self.expl, "## Known Limitations\n", "```\n## Known Limitations\n```\n")
        self.assertFailsWith("Known Limitations")

    def test_05g_bad_file_name_fails(self):
        self.write(f"{self.docs}/Explanations/Overview.md", explanation("backend"))
        self.assertFailsWith("Overview.md", "file name")

    def test_05h_unexpected_file_kind_fails(self):
        self.write(f"{self.docs}/notes.md", "# notes\n")
        self.assertFailsWith("notes.md")


class TestPlaceholders(ValidatorTestCase):
    def test_06a_placeholder_in_prose_fails(self):
        for marker in ["TODO", "TBD", "FIXME", "lorem ipsum", "<fill in>", "[...]"]:
            with self.subTest(marker=marker):
                self.reset_fixture()
                self.edit(self.expl, "One module per feature.", f"One module per feature. {marker}")
                self.assertFailsWith("placeholder")

    def test_06b_placeholder_in_code_is_allowed(self):
        # fixture has TODO inside a fence and inside inline code
        self.assertPasses()

    def test_06c_todo_as_part_of_a_word_is_allowed(self):
        self.edit(self.expl, "One module per feature.", "One module per feature (see TODOs list in code: no).")
        self.assertPasses()


class TestFindings(ValidatorTestCase):
    def test_07a_heading_with_title_fails(self):
        self.edit(self.rev, "### BE-R01-01\n", "### BE-R01-01 Anonymous token endpoint\n")
        self.assertFailsWith("finding heading")

    def test_07b_duplicate_id_fails(self):
        text = self.read(self.rev).replace(
            "## Recommended Target Pattern", finding("BE-R01-01").replace("PROJECT", "backend")
            + "\n## Recommended Target Pattern")
        self.write(self.rev, text)
        self.assertFailsWith("duplicate", "BE-R01-01")

    def test_07c_missing_required_field_fails(self):
        for field in ["**Title:**", "**Severity:**", "**Evidence:**", "**Recommendation:**",
                      "**Verified against:**", "**Confidence:**"]:
            with self.subTest(field=field):
                self.reset_fixture()
                text = self.read(self.rev)
                text = "\n".join(l for l in text.splitlines() if not l.startswith(field)) + "\n"
                self.write(self.rev, text)
                self.assertFailsWith("BE-R01-01", field)

    def test_07d_nn_mismatch_with_file_fails(self):
        self.edit(self.rev, "### BE-R01-01\n", "### BE-R02-01\n")
        self.edit(self.summ, "| [[Docs/backend/Reviews/01-Security-Review#BE-R01-01\\|BE-R01-01]]",
                  "| BE-R02-01")
        self.edit(self.expl, "#BE-R01-01|BE-R01-01]]", "|BE-R01-01]]")
        self.assertFailsWith("BE-R02-01", "R01")

    def test_07e_wrong_project_code_fails(self):
        self.edit(self.rev, "### BE-R01-01\n", "### WP-R01-01\n")
        self.edit(self.summ, "| [[Docs/backend/Reviews/01-Security-Review#BE-R01-01\\|BE-R01-01]]",
                  "| WP-R01-01")
        self.edit(self.expl, "#BE-R01-01|BE-R01-01]]", "|BE-R01-01]]")
        self.assertFailsWith("WP-R01-01", "BE")

    def test_07f_withdrawn_finding_needs_only_title_and_status(self):
        text = self.read(self.rev).replace(
            "## Recommended Target Pattern",
            "### BE-R01-02\n**Title:** Old claim\n**Status:** Withdrawn — disproved\n\n## Recommended Target Pattern")
        self.write(self.rev, text)
        self.edit(self.summ, "\n\n## Extra Project Section", "\n| BE-R01-02 | withdrawn | — | Security |\n\n## Extra Project Section")
        self.assertPasses()


class TestCoverage(ValidatorTestCase):
    def test_08a_finding_absent_from_summary_fails(self):
        self.write(self.summ, summary("backend", []))
        self.assertFailsWith("BE-R01-01", "00-Review-Summary")

    def test_08b_doc_not_linked_from_index_fails(self):
        self.edit(self.idx, "- [[Docs/backend/Reviews/01-Security-Review]]\n", "")
        self.assertFailsWith("backend-Index.md", "01-Security-Review")

    def test_08c_missing_index_fails(self):
        (self.root / self.idx).unlink()
        self.assertFailsWith("backend-Index.md")

    def test_08d_missing_summary_fails(self):
        (self.root / self.summ).unlink()
        self.edit(self.idx, "- [[Docs/backend/Reviews/00-Review-Summary]]\n", "")
        self.assertFailsWith("00-Review-Summary.md")


class TestFragments(ValidatorTestCase):
    def test_09a_fragment_to_missing_finding_fails(self):
        self.edit(self.expl, "#BE-R01-01|BE-R01-01]]", "#BE-R01-07|BE-R01-07]]")
        self.assertFailsWith("BE-R01-07")

    def test_09b_fragment_to_missing_heading_fails(self):
        self.edit(self.rev, "#How It Works|", "#How It Really Works|")
        self.assertFailsWith("How It Really Works")

    def test_09c_fragment_to_existing_heading_passes(self):
        self.assertPasses()  # fixture links #How It Works and #BE-R01-01


class TestCli(ValidatorTestCase):
    def test_11a_unknown_project_is_usage_error(self):
        result = self.run_validator("auth-server")
        self.assertEqual(result.returncode, 2)

    def test_11b_missing_doc_directory_fails(self):
        result = self.run_validator("wpmanager")
        self.assertEqual(result.returncode, 1)
        self.assertIn("wpmanager", result.stdout)


class TestBugTrackerProject(ValidatorTestCase):
    """The validator must know BugTracker's code (BT) so Task 2 needs no patch."""
    project = "BugTracker"

    def test_12a_bugtracker_tree_passes(self):
        self.assertPasses()

    def test_12b_bugtracker_rejects_be_code(self):
        self.edit(self.rev, "### BT-R01-01\n", "### BE-R01-01\n")
        self.assertFailsWith("BT")


class TestWpmanagerProject(ValidatorTestCase):
    """The validator must know wpmanager's code (WP) so Task 3 needs no patch."""
    project = "wpmanager"

    def test_13a_wpmanager_tree_passes(self):
        self.assertPasses()


if __name__ == "__main__":
    unittest.main()
