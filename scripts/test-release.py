"""Release gate regression tests using real temporary Git histories."""
import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest

spec = importlib.util.spec_from_file_location("validate_release", Path(__file__).with_name("validate-release.py"))
validator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(validator)


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.repo = Path(self.temp.name)
        self.git("init", "--initial-branch=master")
        self.git("config", "user.name", "Release test")
        self.git("config", "user.email", "release-test@example.invalid")
        (self.repo / "de.peeeq.wurstscript").mkdir()
        (self.repo / "de.peeeq.wurstscript/version.txt").write_text("2.0.0\n")
        self.git("add", ".")
        self.git("commit", "-m", "Version baseline")
        self.git("update-ref", "refs/remotes/origin/master", "HEAD")

    def git(self, *args):
        return subprocess.check_output(["git", "-C", str(self.repo), *args], text=True, stderr=subprocess.STDOUT).strip()

    def test_annotated_release_tag_on_master_is_accepted(self):
        self.git("tag", "-a", "v2.0.0", "-m", "Release 2.0.0")
        self.assertEqual(validator.validate_release(self.repo, "refs/tags/v2.0.0"), "2.0.0")

    def test_lightweight_tag_on_master_ancestor_is_accepted(self):
        self.git("tag", "v2.0.0")
        release = self.git("rev-parse", "HEAD")
        self.git("commit", "--allow-empty", "-m", "Later master change")
        self.git("update-ref", "refs/remotes/origin/master", "HEAD")
        self.git("checkout", "--detach", release)
        self.assertEqual(validator.validate_release(self.repo, "refs/tags/v2.0.0"), "2.0.0")

    def test_branch_and_non_semantic_tags_are_rejected(self):
        for ref in ["refs/heads/master", "refs/tags/nightly", "refs/tags/v1.9.0.0", "refs/tags/v02.0.0", "refs/tags/v2.0.0-rc.1"]:
            with self.subTest(ref=ref), self.assertRaises(ValueError):
                validator.validate_release(self.repo, ref)

    def test_tag_version_must_match_checked_in_version(self):
        self.git("tag", "v2.0.1")
        with self.assertRaisesRegex(ValueError, "version.txt"):
            validator.validate_release(self.repo, "refs/tags/v2.0.1")

    def test_tag_must_point_to_checkout(self):
        self.git("tag", "v2.0.0")
        self.git("commit", "--allow-empty", "-m", "Untagged commit")
        self.git("update-ref", "refs/remotes/origin/master", "HEAD")
        with self.assertRaisesRegex(ValueError, "checkout"):
            validator.validate_release(self.repo, "refs/tags/v2.0.0")

    def test_tag_on_unmerged_branch_is_rejected(self):
        self.git("switch", "-c", "feature")
        self.git("commit", "--allow-empty", "-m", "Unmerged change")
        self.git("tag", "v2.0.0")
        with self.assertRaisesRegex(ValueError, "master"):
            validator.validate_release(self.repo, "refs/tags/v2.0.0")


if __name__ == "__main__":
    unittest.main()
