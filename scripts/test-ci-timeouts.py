"""Keep every Actions job bounded, including jobs added to future workflows."""
from pathlib import Path
import re
import unittest


def job_blocks(workflow):
    # Repository workflows use block YAML with two-space job keys. Reject an unfamiliar
    # layout rather than silently treating a workflow as having no jobs.
    jobs = re.split(r"^jobs:\s*$", workflow, maxsplit=1, flags=re.MULTILINE)
    if len(jobs) != 2:
        raise ValueError("expected a jobs section")
    headers = list(re.finditer(r"^  ([\w-]+):\s*$", jobs[1], re.MULTILINE))
    if not headers:
        raise ValueError("expected block-style job definitions")
    for index, header in enumerate(headers):
        end = headers[index + 1].start() if index + 1 < len(headers) else len(jobs[1])
        yield header.group(1), jobs[1][header.end():end]


def check_limits(workflow):
    for name, block in job_blocks(workflow):
        limits = re.findall(r"^    timeout-minutes: (\d+)\s*$", block, re.MULTILINE)
        if len(limits) != 1 or not 0 < int(limits[0]) <= 35:
            raise ValueError(f"job {name} needs one positive timeout-minutes, at most 35")


class TimeoutPolicyTests(unittest.TestCase):
    def test_all_workflows_have_bounded_jobs(self):
        directory = Path(__file__).resolve().parents[1] / ".github/workflows"
        workflows = sorted(directory.glob("*.y*ml"))
        self.assertTrue(workflows)
        for workflow in workflows:
            with self.subTest(workflow=workflow.name):
                check_limits(workflow.read_text(encoding="utf-8"))

    def test_missing_or_excessive_limits_fail(self):
        for limit in ["", "    timeout-minutes: 0\n", "    timeout-minutes: 360\n"]:
            with self.subTest(limit=limit), self.assertRaises(ValueError):
                check_limits("jobs:\n  unbounded:\n    runs-on: ubuntu-latest\n" + limit)

    def test_a_second_unbounded_job_is_not_hidden(self):
        with self.assertRaisesRegex(ValueError, "second"):
            check_limits("jobs:\n  first:\n    timeout-minutes: 25\n  second:\n    runs-on: ubuntu-latest\n")


if __name__ == "__main__":
    unittest.main()
