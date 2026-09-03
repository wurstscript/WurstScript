# Backlog

Open compiler work that is not derivable from the code or the issue tracker. Keep it short: when an
item lands, delete it here and let the commit and PR carry the history. Durable lessons go under
Notes; finished-work narrative does not.

The Lua backend performance items live in `LUA_HOT_PATH_SPEC.md`, which carries their root causes,
required changes and acceptance criteria. Task 2 of that spec is done (#1284); Tasks 1, 3, 4 and 5
are open.

## Open

- **A dead dispatch slot survives for overloads inside a specialised class.** Two overloads of one
  source method share a declared name, so a specialised class holding only overloads still composes
  one shared slot and binds it to whichever is reached first. Nothing calls it. A fix needs an
  identity which treats a chain's differing type variables as the same signature while still
  separating real parameter differences; the dispatch group key cannot be used because it embeds each
  class's type variable (`void|T192,real` against `void|T636,real`). Worth doing only if this stops
  being dead weight.

- **`CompilerFuzzTestsSC` writes one fixed output filename for every generated program.** All
  programs go to `test-output/CompilerFuzzTestsSC_assertCompilesForBothBackends_*.j`, so concurrent
  writers tear the file and pjass reports a truncated keyword. Seen once in a full run on 2026-09-03,
  passes when the class runs alone. Give each generated program its own output name.

## Blocked on a decision

- **Eliminating the remaining `castTo int`.** The motivating case is timer data attachment
  (`ClosureTimers.wurst`) and the containers behind it: `Table`, `HashList`, `HashSet`, `HashMap`.
  None can adopt type class bounds as things stand, because an instance is declared one type at a
  time and these accept any type. It needs a way to give an instance for a whole family, every class
  type or every handle type, which is a language design question: syntax, where such an instance may
  be declared under the orphan rule, and whether a specific instance beats a family one. Do not start
  this autonomously.

## Notes

- `%` is real modulo in Wurst; `mod` is integer modulo. `int % 8` types as `real`. `div` and `mod`
  return the left operand's type, so `real r = 7 div 2` compiles and is meant to
  (`ExpressionTests.integerDivisionOfLiteralsIsStillAssignableToReal`).
- Emitted Lua must be byte-identical for identical input (AGENTS.md §8), and emitted Jass can be
  diffed across runs too. `LuaTranslationTests.luaOutputIsDeterministicForGenericOverrideSlots`
  failed once on Windows CI and never again in 250 local compiles; the test now writes both scripts
  and names the first differing lines, so the next occurrence will say what differed. Do not weaken it.
- A name that looks redundant is usually carrying a distinction. The mangled method name separates
  overloads; `leftType` on `div` keeps a literal assignable to a real. Check what a name
  distinguishes before replacing it with a tidier one.
- The suite is the specification. Before changing what the type checker accepts, grep the tests for
  the shape being rejected.
- A test that hangs looks exactly like a test that is slow. If the suite stops making progress, take
  a thread dump of the forked worker (`jstack <pid>`) before killing it.
- Method names are not what the frontend called them. `LuaDispatchPreparation.normalizeMethodNames`
  renames a whole dispatch group to one name, records the slot segment on `ImTranslator`, and only
  then does the backend run. A question about which Lua slot something lands in is a question about
  that pass, not about `LuaTranslator`.
- Tests run five Jass configurations plus the interpreter, then the Lua target separately.
  `testAssertOkLines(true, ...)` covers both the pre-transform interpreter and full monomorphisation.
- `@Test` functions are interpreter unit tests by design; `StdLibOwnTests` and `grill test` run them
  there. There is no Wurst-level end-to-end test feature yet; correctness on the Lua target in a real
  map is asserted in the agent workflow, not by the suite.
- Test forks: eight forks won on eight cores (7m03s wall against 13m11s serial) even though each
  test runs 2.6 times slower there. Wall time cannot go below the slowest class, `ExportToWurstTest`
  at 108s, until it is split.
- The stdlib copy under `de.peeeq.wurstscript/temp/WurstStdlib2` is a fetched artefact for tests.
  Real stdlib changes belong in the WurstStdlib2 repo.
- `WURST_LANGUAGE.md` ships as a compiler resource at
  `de.peeeq.wurstscript/src/main/resources/agent-docs/WURST_LANGUAGE.md`, not at the repository
  root. Keep it and `CHANGELOG.md` current in the PR that changes the behaviour.
