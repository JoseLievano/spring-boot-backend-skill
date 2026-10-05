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

    def test_g04j_contract_finding_is_not_evidence(self):
        self.edit(self.doc, "**Evidence:** BE-R02-01 · ADR-001", "**Evidence:** GC-R01-01")
        self.assertFailsWith("G04-01", "cites no Finding ID and no ADR")


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


if __name__ == "__main__":
    unittest.main()
