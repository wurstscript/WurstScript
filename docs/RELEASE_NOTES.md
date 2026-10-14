# Compiler release highlights

Pushing a validated `vMAJOR.MINOR.PATCH` tag triggers the compiler's release workflow.
Its existing publication job obtains GitHub's detailed changelog for that tag and asks an AI
model to condense it into one to three important user-facing highlights. Related
performance changes are grouped into their overall impact rather than individual
internal optimizations. The full changelog remains below `## Highlights`.

Configure the `OPENAI_API_KEY` Actions secret on this repository once. The default
model is `gpt-4.1-mini`; `RELEASE_SUMMARY_MODEL` can override it. The integration uses
the [Responses API with Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs?api-mode=responses)
through Python's standard library and adds no package dependency.

Generation starts after every platform build passed, within the existing publication
job and its unchanged GitHub permissions. Publication requires valid generated notes. Missing
credentials, inference errors, incomplete responses, and malformed summaries fail
the release instead of publishing invented highlights. The generated notes are
retained as a workflow artifact and applied when the draft becomes public. Already
published releases retain the workflow's existing refusal to republish them.

The VS Code extension reads this section from the release for the installed or
available compiler version. Summaries belong to the compiler release; publishing
a new summary does not require updating the extension or maintaining per-version
summary text in either repository.

Run `python scripts/test-generate-release-notes.py` for focused tests. They mock the
AI service and need no credential or network access.
