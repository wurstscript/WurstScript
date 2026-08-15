# Overnight loop brief

Work on the WurstScript compiler in this repository, on the branch
`feat/fasthashmap-proof`. Read `AGENTS.md` and `BACKLOG.md` before doing anything.

## Each iteration

1. Read `BACKLOG.md`. Take the top item that isn't blocked.
2. Do it. Root-cause it — adjust the underlying system rather than patching a symptom.
   If it turns out to be bigger than one iteration, split it in the backlog and do the
   first part.
3. Verify. Targeted tests while iterating; the full suite before any commit that touches
   main source:

       cd de.peeeq.wurstscript && ./gradlew test

   It must be green. Emitted Lua must stay byte-identical unless the change is meant to
   alter it — compare two runs to check. Do not diff `.j` across runs; it is not stable,
   and backlog item 11 explains why.
4. Commit and push. Small commits, one concern each. Never end an iteration with
   uncommitted work.
5. Update `BACKLOG.md`: move finished items to Done with one line on what actually
   happened, add anything learned to Notes, reorder if something more urgent turned up.

## Rules

- Never force-push, rewrite history, merge, push to master, or open a PR. The branch gets
  reviewed in the morning.
- Commits are authored as the repository owner. No AI, assistant, or co-author references
  anywhere in commit messages or code comments.
- Never stop to ask. If something needs a decision from the owner, write the question into
  `BACKLOG.md` under that item, mark it blocked, and move to the next item.
- If the full suite goes red and it can't be fixed within the iteration, revert the change,
  note why in `BACKLOG.md`, and move on. Leave the branch green.
- Comments explain why, not what. Match the surrounding style.
- Do not touch `de.peeeq.wurstscript/temp/WurstStdlib2` — a fetched test artefact, editing
  it changes nothing real.

## Keeping going

- Finishing an item is not the end of the run. Go back to step 1 and take the next one. Do
  not stop to summarise, and do not treat a green suite as a finish line.
- At most one item may be marked blocked per iteration, and only if it genuinely needs a
  decision rather than more work. If everything left looks blocked, that is wrong about at
  least one of them — re-read and start the one that can be moved furthest.
- If an item goes three iterations without landing a commit, split it in the backlog and
  move on. Don't spend the whole run on one thing.
- Backlog item 12 is a standing item that never completes. There is always a next item.
