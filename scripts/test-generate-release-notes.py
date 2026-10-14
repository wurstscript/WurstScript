"""Release-tag summary generation contract, tested without network or credentials."""
import importlib.util
import io
import json
from pathlib import Path
import unittest
from unittest.mock import patch
from urllib.error import HTTPError

spec = importlib.util.spec_from_file_location("release_notes", Path(__file__).with_name("generate-release-notes.py"))
notes = importlib.util.module_from_spec(spec)
spec.loader.exec_module(notes)


def model_response(highlights, status="completed"):
    return io.BytesIO(json.dumps({"status": status, "output": [{"type": "message", "content": [
        {"type": "output_text", "text": json.dumps({"highlights": highlights})},
    ]}]}).encode("utf-8"))


class ReleaseNotesTests(unittest.TestCase):
    def test_generate_summary_and_preserve_detailed_changelog(self):
        changelog = "## What's Changed\n* Internal optimization by @author\n\n**Full Changelog**: https://example.com/compare"
        with patch.object(notes, "urlopen", return_value=model_response(["Faster overall map builds."])) as api:
            body = notes.generate_release_notes("2.1.0", changelog, "test-key", "test-model")
        self.assertTrue(body.startswith("## Highlights\n\n- Faster overall map builds.\n\n"))
        self.assertTrue(body.endswith(changelog + "\n"))
        request = api.call_args.args[0]
        self.assertEqual(request.full_url, "https://api.openai.com/v1/responses")
        payload = json.loads(request.data)
        self.assertEqual(payload["model"], "test-model")
        self.assertFalse(payload["store"])
        self.assertIn(changelog, payload["input"])
        self.assertEqual(payload["text"]["format"]["type"], "json_schema")
        self.assertIn("overall impact", payload["instructions"])
        self.assertEqual(api.call_args.kwargs["timeout"], 90)

    def test_missing_key_invalid_version_and_changelog_fail_before_network(self):
        for version, changelog, key in [("2.0.0", "Changes", ""), ("nightly", "Changes", "key"),
                                        ("2.0.0", "", "key"), ("2.0.0", "x" * 100_001, "key")]:
            with self.subTest(version=version, key=key), patch.object(notes, "urlopen") as api:
                with self.assertRaises(ValueError):
                    notes.generate_release_notes(version, changelog, key)
                api.assert_not_called()

    def test_invalid_model_highlights_are_rejected(self):
        for highlights in [[], ["Change"] * 4, [""], ["x" * 161], ["Two\nlines"], [123], "Not an array"]:
            with self.subTest(highlights=highlights), patch.object(notes, "urlopen", return_value=model_response(highlights)):
                with self.assertRaises(ValueError):
                    notes.generate_release_notes("2.0.0", "Changes", "test-key")

    def test_incomplete_response_is_not_published(self):
        with patch.object(notes, "urlopen", return_value=model_response(["Change"], "incomplete")):
            with self.assertRaisesRegex(ValueError, "did not complete"):
                notes.generate_release_notes("2.0.0", "Changes", "test-key")

    def test_api_failure_does_not_fabricate_a_summary(self):
        with patch.object(notes, "urlopen", side_effect=HTTPError("https://api.openai.com", 503, "Unavailable", {}, None)):
            with self.assertRaises(HTTPError):
                notes.generate_release_notes("2.0.0", "Changes", "test-key")


if __name__ == "__main__":
    unittest.main()
