# Backlog

Open compiler work that is not derivable from the code or the issue tracker. Keep it short: when an
item lands, delete it here and let the commit and PR carry the history. Durable lessons go under
Notes; finished-work narrative does not.

## Open

- **One model for run and build.** Run and build keep two ways to compile (`SafetyLevel`: the managed
  model as it is, or a clean, rebuild and copy) because the incremental model was not trusted to be
  complete. `IncrementalModelOracleTests` compares the model after random edits with a build from
  scratch (`INCREMENTAL_ORACLE_SEEDS=100` runs 100 seeds) and found two propagation gaps, both fixed.
  What is left: a `.j` change makes `calculateCUsToUpdate` return the whole model, and a compilation
  swaps the map script with the project config applied into the model (`replaceBaseScriptWithConfig`).
  The swap changes only function bodies (`config`, `InitCustomPlayerSlots`, ...) yet invalidates every
  attribute, so the first compilation of a model checks all of it again; later ones skip the check. The
  swap's own cost was not isolated: on castle fight without optimisations the first build of a model
  takes 21 s and later ones 11 to 12 s in one JVM, and the stage samples show most of the difference
  in passes the swap does not touch, so JIT warm-up. Next, in this order: feed the config-applied script
  to the model before its first check (so the CLI checks once); keep the unchanged top-level
  declarations of a changed Jass file and re-check only the units which mention a changed name; then
  drop `SafetyLevel` (purging unimported units from the managed model in `compileMap` changes it for the
  next run, and a build could compare file hashes instead of cleaning). A decision is needed for the
  last: whether a release build ignores unsaved editor buffers, as the clean and rebuild does now.

- **A dead dispatch slot survives for overloads inside a specialised class.** Two overloads of one
  source method share a declared name, so a specialised class holding only overloads still composes
  one shared slot and binds it to whichever is reached first. Nothing calls it. A fix needs an
  identity which treats a chain's differing type variables as the same signature while still
  separating real parameter differences; the dispatch group key cannot be used because it embeds each
  class's type variable (`void|T192,real` against `void|T636,real`). Worth doing only if this stops
  being dead weight.

- **Implement `RawHashMap<K, V>` from [docs/NATIVE_KEYED_STORE_DESIGN.md](docs/NATIVE_KEYED_STORE_DESIGN.md).**
  The note records the cross-backend key/value contract, existing intrinsic reuse, specialization
  requirements, and regression cases; the data structure itself is not implemented.

- **Audit the remaining Lua emission for waste.** The Lua backend began as "make Lua mode usable", and
  recent fixes (`git log --grep "Lua"`) keep finding helper calls, allocations and dead bindings that
  were simply the easiest thing to emit. Method: read the emitted script of a real map next to what
  hand-written Lua would be, list every compiler-introduced call, allocation or table write on an
  ordinary path, and fix the root (AGENTS.md §3, §7).

## Blocked on a decision

- **Is the generated AST the right shape for the passes?** Generated nodes are single-parent mutable
  trees: a node that already has a parent cannot be placed elsewhere (`setParent` throws), so the passes
  copy (`.copy()`) and splice (`replaceBy`) throughout, and attribute caches are cleared by hand
  (`clearAttributes`). Whether an immutable or persistent IM, or a different rewrite API in
  abstractsyntaxgen, would make passes simpler or faster has no recorded evaluation. A proposal needs a
  baseline (compile time on a large map) and one pass ported as a trial. Do not start it autonomously.

- **Replacing `castTo int` in the old generic containers.** The motivating case is timer data
  attachment (`ClosureTimers.wurst`) and the containers behind it: `Table`, `HashList`, `HashSet`,
  `HashMap`. These old generics erase values into a shared integer ID space. Removing those casts
  needs family-wide type class instances (for example, for every class or every handle type), which
  raises language-design questions about syntax, the orphan rule, and precedence over specific
  instances. New generics specialize values to their native types and do not need this representation
  change; the handle-keyed `FastKeyedMap` work is separate. Do not start the old-container migration
  autonomously.

## Notes

- `%` is real modulo in Wurst; `mod` is integer modulo. `int % 8` types as `real`. `div` and `mod`
  return the left operand's type, so `real r = 7 div 2` compiles and is meant to
  (`ExpressionTests.integerDivisionOfLiteralsIsStillAssignableToReal`).
- Emitted Jass and Lua must be byte-identical for identical input (AGENTS.md §3). `LuaTranslationTests.luaOutputIsDeterministicForGenericOverrideSlots`
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
  there. The only Lua-target execution the suite has is `test().testLua(true).executeProg()`, which
  runs the emitted script in a real Lua 5.3 against the shim in `src/test/resources/luaruntime/`. Its
  reach is bounded by how much of the roughly one thousand natives the shim models, so those tests
  stay small and targeted. There is no Wurst-level end-to-end feature; correctness in a real map on
  the Lua target is asserted in the agent workflow.
- `wurst_run.args` in a generated project defaults to `-stacktraces` and no `-inline`, so every
  emitted function pays `wurst_stack` bookkeeping and no leaf is inlined. Benchmarks that inform
  stdlib design must use `-inline -localOptimizations` without `-stacktraces`, or they measure the
  debug configuration.
- Test forks: eight forks won on eight cores (7m03s wall against 13m11s serial) even though each
  test runs 2.6 times slower there. Wall time cannot go below the slowest class, `ExportToWurstTest`
  at 108s, until it is split.
- The stdlib copy under `de.peeeq.wurstscript/temp/WurstStdlib2` is a fetched artefact for tests.
  Real stdlib changes belong in the WurstStdlib2 repo.
- `WURST_LANGUAGE.md` ships as a compiler resource at
  `de.peeeq.wurstscript/src/main/resources/agent-docs/WURST_LANGUAGE.md`, not at the repository
  root. Keep it and `CHANGELOG.md` current in the PR that changes the behaviour.
