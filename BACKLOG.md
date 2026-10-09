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
  CLI loads the project without checking it, swaps the script in and checks once (`ModelManager.loadProject`,
  `checkProject`); the language server has checked its model before it knows the map, so its first
  run still checks again. The swap's own cost in the language server was not isolated: on castle fight
  without optimisations the first build of a model takes 21 s and later ones 11 to 12 s in one JVM, and
  the stage samples show most of the difference in passes the swap does not touch, so JIT warm-up. Next,
  in this order: keep the unchanged top-level
  declarations of a changed Jass file and re-check only the units which mention a changed name; then
  drop `SafetyLevel` (purging unimported units from the managed model in `compileMap` changes it for the
  next run, and a build could compare file hashes instead of cleaning). Run and build both read the
  open editor buffers today (`readCompilationUnitContents` prefers an open buffer, also in the rebuild after
  `clean()`), so a build can contain text which is not on disk, while grill reads the disk. wurst4vscode
  sends `wurst.buildmap` and `wurst.startmap` without saving the open files first. Whether a build should
  save first, or refuse unsaved files, is open.

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

- **Shake what is left, and earlier.** `TreeShaker` drops the functions (and the methods whose implementation goes
  with them) which nothing reaches, and the globals which no reachable code reads together with the assignments of
  constants to them (not a call, and a division only by a constant: those may have an effect), on both targets, in
  front of the compile-time functions, before the generics and after them. On castle fight (Lua) that takes the 34,200
  functions of the translation to 10,500 and the statements from 258,000 to 110,000 before the generics, and the script
  has 92,000. What it leaves is the weight which is neither: 3,512 classes of which 1,611 reach the script (the
  specialisation of generics adds 220 more, and the class elimination on Jass builds an allocator, a deallocator and
  field arrays for every one), and the fields of the classes. Next, in this order, each its own change with its own
  proof: (1) classes which no live code allocates, names in a type, or inherits from, taking care that type ids are
  numbered over the classes in the program and that a dead class can be the supertype of a live one; (2) globals
  and fields which only assignments with an effect write, which keeps the effect and drops the variable (what
  `ImOptimizer.removeGarbage` does late); (3) the work before the IM exists, which no pass over the IM can reach:
  about 2.6 s of a warm castle fight build is outside the phases the IM passes cover, and the translation takes 1.3 s
  to produce 34,000 functions of which 10,500 are used. Translating only function bodies which are reachable saves
  about 0.17 s of that (measured), because the cost is per class and per method (`getClassesWithImplementation`,
  0.5 s), so the proposal is to translate by demand from `main`, the compile-time functions and what they name. A
  type check of the dependencies only as far as the compilation needs it is the larger prize (the check is the
  largest block of a cold build), but the validator also marks things the translation reads
  (`NamePreservation.preserve`), so it needs its own investigation.
  Whole-program analyses which look at every function now see less, and a dead function can no longer make them
  conservative: `LuaTypedValues` proves more values non-nil without the dead writers (a read of a static which only
  dead code wrote loses its `__wurst_ensureInt`), `GlobalsInliner` inlines a constant of a package initialiser which
  a read in a dead assignment used to keep it from proving safe (`Angle_RADTODEG` in the object recycler tests, so
  the package initialiser disappears too), and `EliminateGenerics.allocatedClasses` decides where a
  specialised method goes from the allocations in the program, so a dead `new_Parent` no longer keeps it on the erased
  class (a generic parent which nothing allocates directly keeps a specialised class table with the method).

- **Build the local-player analysis fewer times, or incrementally.** An optimised Lua build of castle fight
  constructs `LocalPlayerContextAnalyzer` five times: for the compile-time state function the splitter optimises
  (`FunctionSplitter`, once per split target), for `ImInliner.doInlining`, once per `ImOptimizer.runLocalOptimizationSweep`
  (two sweeps) and for `LuaOldGenericsCasts`, about 4 s of the compiler thread (7% of its samples). Each one is the
  whole program, because the fact about a variable or a function depends on every caller and every writer.
  Keeping the analysis of the first sweep for the second is not output-neutral: it changed about 40,000 lines of the
  castle fight script (different local merges), so a construction per sweep stays until the analysis can retract a
  fact (the propagation is a least fixpoint, so a rewrite which removes a flow cannot be undone in place) or the passes
  report which functions they changed. What is left of a construction is the walk itself: the first touch of every IM
  node (`indexElement`, `isInert`, `markReturns`) is about half of the samples, so the cost follows the size of the IM
  and a flatter IM would help more than another rewrite of the analysis.

- **Fewer whole-program passes between the optimiser's phases.** The garbage removal and the flatten each walk the
  whole program, and a build runs them many times (castle fight: the removal 3 to 8 times with 11 to 29 rounds, the
  flatten 6 to 16 times) while each of them changes a few percent of the functions. Done: the walk over a body is
  one (`FunctionFactsCollector`), a round after the first looks at what the round before changed
  (`ImTranslator.refreshReadVariables`) and the removal runs until nothing is left, the removal puts the effects of
  an assignment in its place as statements (it left a statement expression for the next flatten to unwrap, which
  was 5 of the flatten calls of a build, and which `GlobalsInliner` took for a statement which may abort, so it did
  not inline the constants of a package initialiser), so no flatten and no second removal (#883) follows it, and a flatten
  leaves the functions which were not modified since the last one (`ImFunction.modificationCount()`, which the generator
  keeps: `modification counts: ImFunction` in `jass_im.parseq`). Left:
  (1) more removals which remove nothing: one which the test suite shows to find nothing becomes an `assertNoGarbage`,
  as the second removal, the one in `optimize` and the one in front of the empty package initialisers did (the one in
  front of the first local optimisation is a candidate); (2) the Jass
  pipeline analyses the call relation once more after the last removal (`calculateCallRelationsAndReadVariables`
  before `ImToJassTranslator`), which the removal has just done; (3) facts which live longer than one removal, valid
  while `modificationCount()` of the function is the same (it replaces `ImTranslator.functionChanged`, which a pass
  can forget to call), so that the first round of a removal does not walk the functions no pass touched; (4) `LocalPlayerContextAnalyzer`, built again for the whole program after each
  pass which is not local-player aware (see "Build the local-player analysis fewer times" above); (5) the flatten after the tuples
  are eliminated is the one which does work (about 565 to 800 functions of castle fight): the producers of
  statement expressions (`EliminateTuples`, `SimpleRewrites`, the inliner) could emit statements as the removal does;
  (6) a chain of assignments to unread variables (`v1 = v0; v2 = v1; ...`, nothing reads the last) takes a round for
  each link, and a round costs the functions which changed (a walk of each, the rewrite of its statement list, its
  locals), so the cost grows with the square of the length. Measured in a unit test (the checks of unit-test mode on,
  one removal): a chain inside one function takes 0.24 s for 1,000 links, 1.5 s for 4,000 and 16.7 s for 16,000; with
  each link in a function of its own 0.05, 0.9 and 19.4 s. The programs measured need at most ten rounds, so this is
  not a cost of a real build; master stopped after ten rounds and left the rest. Linear time needs the reads of each
  variable counted per function and the assignments removed from a worklist inside the round (taking the reads of the
  operands which go with an assignment off the count), which the facts, a set of variables per function, do not hold.
- **What is left of `ModelManagerImpl.updateModel` in a load.** A new unit is not reconciled with the model any more
  while every unit is unchecked (a load of a project and of the libraries it imports): `calculateCUsToUpdate` searched
  the whole model for what imports the unit and added what the set held already. `updateModel` fell from 4.1% to 1.6% of
  the samples of the compiler's thread (castle fight without optimisations, 1,697 and 1,671 samples). What remains is
  the scan for the unit a file replaces (`wFile(c).equals(wFile(cu))` over all units, once for each file) and
  `wFile`, which canonicalises the path of each unit once. An index from file to unit would remove the scan, but the
  list of the model has several writers (`updateModel`, `removeCompilationUnit`, `retainCompilationUnits`, the
  incremental checks), so it has to stay in step with all of them; that is about 0.25 s of a castle fight build.
- **Parallel type check and per-function IM passes.** Parsing is parallel (above); the rest of a build is not, and
  JFR of fresh master says where the time is (shares of the compiler thread's samples, castle fight without
  optimisations / optimised / zombie defense): type check light and heavy phases (attribute evaluation, per unit and per
  function) 37% / 17% / 22%, the whole-program parts of the type check 13% / 6% / 12%, flatten, local optimisations and
  Lua translation (per function) 4% / 18% / 8%. Measured in one JVM, castle fight without optimisations: the first,
  cold check takes 19.3 s, the same check with a warm JIT and cleared attributes 6.7 s, and with the attributes
  cached 4.1 s (of which 1.0 s is whole-program). What stops parallel validation is shared mutable state, not the
  work: the generated attributes (a second thread which finds the in-progress state throws a false
  `CyclicDependencyError`, the caches are plain fields; the packed state words of the generator need atomic updates,
  see its readme), `GlobalCaches` (static fastutil maps behind the name lookups), `ErrorHandler` (unsynchronised
  lists and maps), the instance state of `WurstValidator` and `NamePreservation.clearSyntheticMarkers`, which changes
  the tree while validating. The per-function IM passes need a translator whose caches and generated names are per
  function and deterministic. Do not start with the attribute protocol: make the caches, the error handler and the
  validator per unit first, and measure that against the 2.6 s of attribute evaluation in the warm check.
- **Make the grammars SLL-clean.** The SLL-first parse (`AntlrTokenPipeline`) parses a file again with the full LL
  prediction when SLL does not accept it, which also happens to a few valid files: 2 of the 733 `.wurst` files below
  castle fight (one is `util/Cinematic.wurst` of the standard library) and `blizzard.j`, whose trees are the same either
  way. Find the decisions of the Wurst and Jass grammars which need the context of the calling rule (the syntax
  error the SLL pass reports on such a file, with the full-prediction pass switched off, names the place) and
  left-factor them. That removes the second parse for valid files, which would then only be paid by broken ones.
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
