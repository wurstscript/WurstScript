# Backlog

Open compiler work that is not derivable from the code or the issue tracker. Keep it short: when an
item lands, delete it here and let the commit and PR carry the history. Durable lessons go under
Notes; finished-work narrative does not.

## Open

- **Incremental map-script declarations and build source policy.** Run, editor build and CLI build use
  the managed model; cancellation is honored at safe boundaries around shared AST preparation and
  throughout the private IM/backend work. See `docs/RUNMAP_PERFORMANCE.md`. What is left:
  a `.j` change makes `calculateCUsToUpdate` return the whole model, and a compilation
  swaps the map script with the project config applied into the model (`replaceBaseScriptWithConfig`).
  The swap changes only function bodies (`config`, `InitCustomPlayerSlots`, ...) yet invalidates every
  attribute, so the first compilation of a model checks all of it again; later ones skip the check. The
  CLI loads the project without checking it, swaps the script in and checks once (`ModelManager.loadProject`,
  `checkProject`); the language server has checked its model before it knows the map, so its first
  run still checks again. The swap's own cost in the language server was not isolated: on castle fight
  without optimisations the first build of a model takes 21 s and later ones 11 to 12 s in one JVM, and
  the stage samples show most of the difference in passes the swap does not touch, so JIT warm-up. Next,
  keep the unchanged top-level declarations of a changed Jass file and re-check only the units which
  mention a changed name. Compilation uses an import-root view and retains the editor's units. Run and build consume the current
  editor model (`readCompilationUnitContents` prefers an open buffer), so a build can contain text
  which is not on disk, while grill reads the disk. wurst4vscode
  sends `wurst.buildmap` and `wurst.startmap` without saving the open files first. Whether a build should
  save first, or refuse unsaved files, is open.

- **Reuse translation work on unchanged map runs.** After checked-model reuse, a large Jass map
  takes 4.5 to 5.2 s for an unoptimised warm RunMap: AST→IM 1.3 to 1.4 s, required Jass lowering 1.3 to 1.7 s,
  compile-time work 0.9 to 1.0 s, map injection 0.4 to 0.6 s. Whole-project typechecking is now skipped.
  Investigate retaining a pristine IM snapshot keyed by model/config/target state, then copying it
  before mutable compile-time/backend passes; compare snapshot cost with translation and test edits,
  configuration changes, cancellation and both targets against fresh builds. This needs an explicit
  AST/IM ownership and invalidation contract before implementation. See `docs/RUNMAP_PERFORMANCE.md`.

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
  has 92,000. Class-metadata pruning was tried and deferred (see Notes). Next, in this order, each its own
  change with its own proof: (1) globals
  and fields which only assignments with an effect write, which keeps the effect and drops the variable (what
  `ImOptimizer.removeGarbage` does late); (2) the work before the IM exists, which no pass over the IM can reach:
  about 2.6 s of a warm castle fight build is outside the phases the IM passes cover, and the translation takes 1.3 s
  to produce 34,000 functions of which 10,500 are used. Translating only function bodies which are reachable saves
  about 0.17 s of that (measured), because the cost is per class and per method (`getClassesWithImplementation`,
  0.5 s), so the proposal is to translate by demand from `main`, the compile-time functions and what they name. A
  type check of the dependencies only as far as the compilation needs it is the larger prize (the check is the
  largest block of a cold build), but the validator also marks things the translation reads
  (`NamePreservation.preserve`), so it needs its own investigation.
  A joint class/function reachability worklist could avoid following every virtual override when only some
  receiver classes can exist. It must distinguish a nominal type dependency from an allocation and preserve
  generic construction, compile-time objects and externally preserved entry points. An initial castle fight
  count found 209 retained method implementations in unallocated classes without direct function references
  (5,818 body nodes of 868,171); it does not count their transitive callees or prove all can go. Measure the
  total build cost before expanding the analysis: removing class metadata alone did not show a consistent gain.
  Whole-program analyses which look at every function now see less, and a dead function can no longer make them
  conservative: `LuaTypedValues` proves more values non-nil without the dead writers (a read of a static which only
  dead code wrote loses its `__wurst_ensureInt`), `GlobalsInliner` inlines a constant of a package initialiser which
  a read in a dead assignment used to keep it from proving safe (`Angle_RADTODEG` in the object recycler tests, so
  the package initialiser disappears too), and `EliminateGenerics.allocatedClasses` decides where a
  specialised method goes from the allocations in the program, so a dead `new_Parent` no longer keeps it on the erased
  class (a generic parent which nothing allocates directly keeps a specialised class table with the method).

- **The inliner's Lua register model still splits locals by locality.** No local optimisation reads the
  local-player analysis any more (copy propagation and the local merger preserve what each client computes), so an
  optimised build constructs it once, in `ImInliner.doInlining`, where it refuses to inline client-local functions and
  feeds the register budget, and again lazily in the late arithmetic-helper phase when a helper call is left. The budget
  (`ImInliner.pressureOf`) still counts client-local and synchronised locals as separate register classes, as the
  merger used to keep them; that over-estimates now, which is safe. Counting per type lets more helpers fit: measured on
  castle fight, the late phase then inlines some and re-runs the local optimisations (+3.2 s), and the main inliner's
  decisions change (+0.17% instructions). Decide it with a runtime measurement of the helpers it would inline.

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
  front of the first local optimisation is a candidate); (2) the flatten after the tuples
  are eliminated is the one which does work (about 565 to 800 functions of castle fight): the producers of
  statement expressions (`EliminateTuples`, `SimpleRewrites`, the inliner) could emit statements as the removal does;
  (3) a chain of assignments to unread variables (`v1 = v0; v2 = v1; ...`, nothing reads the last) takes a round for
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
  On a project of 1,625 files the comparisons were 40% of the loading thread's samples, yet keeping the hash of a
  `WFile` (which made them integer comparisons) changed the load time by nothing measurable: the loading thread
  waits for the parse threads, so the scan is not on the critical path while parsing is parallel.
- **What a large map's build still spends, by step (`-measure`), and why.** Measured on a Jass map of 1,625 files
  with thousands of compile-time functions:
  - *Type check (12 s):* the Guava name tables are about 5% of its samples; the rest is attribute evaluation, see
    the next item.
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
- **Devirtualize proven exact receivers before inlining.** Optimized Lua still dispatches an interface
  local initialized with `new A()` virtually when the method has sibling implementations.
  `LuaMethodCallLowering` currently considers only methods with no submethods. Start with bounded
  straight-line exact-type propagation through locals/copies, stopping at possible destruction,
  unknown calls and callback/yield barriers, and use existing direct IM calls. Constructors can expose
  or destroy the allocation; preserve their effects. See the investigation note for emitted examples.
  Measured on the optimised Lua of two large maps (Oct 2026): 222 and 456 dispatch sites are left, of which 125
  and 105 dispatch on a local assigned a fresh allocation in the same function, nearly all the closure of an
  inlined higher-order function. Only 83 and 19 of those have no call between the allocation and the dispatch,
  mostly code which runs once (building a UI); 11 and 61 are inside a loop (`forEach`), which needs a
  whole-program proof that no call in the loop destroys the receiver; the rest have calls in between. A dispatch
  costs about 10 ns more than a direct call in stock Lua, so the straight-line version alone does not pay for
  an analysis; start from the loop case, and only with evidence that such a loop is hot.
- **Materialize complex Lua dispatch receivers within a locals budget.** A call-valued receiver still
  needs a vararg dispatch stub. A disposable nine-line flatten prototype replaced it with a temporary
  and direct descriptor call, passed 378 tests, and the isolated stock-Lua shape was about 28% faster.
  But it made a 199-local function spill every local into a table where master did not. The prototype
  was discarded. Reuse temporaries or respect the register budget, preserve evaluation order and
  short circuits, and measure optimized real-map hot paths; no in-game speedup is established.
- **Evaluate bounded repeated-dispatch reuse after profiling.** Cache a descriptor only for a stable
  local receiver across calls proven not to destroy/reallocate it; ordinary field writes need not
  invalidate its class, but field-expression caching requires a separate alias/write proof. Include
  every virtual target, callbacks, unknown natives and yields; do not memoize return values based
  only on absence of writes. No runtime epochs or guards. Stock-Lua descriptor caching saved about
  9% in an isolated three-call loop with overlapping sample ranges; caching a slot too was worse.
  An unoptimized map scan found only 10 simple-local repeated groups, not proven hot or eligible.
  Require optimized-map evidence before paying for a general analysis or extending IM value types.
- **Evaluate Lua-native bulk copying for large compatible ranges.** `table.move` is a candidate for
  ArrayList relocation/shifts or a compiler-owned bulk operation, with the Jass loop preserved.
  Stock-Lua medians improved for 64/1,024-element copies but regressed for eight elements. Forward
  overlapping loops can mean something different from `table.move`; prove overlap/direction, bounds,
  defaults and counter semantics. Establish game availability, crossover and hot consumers before
  selecting an API or automatic lowering. Scripts and raw samples are in the investigation note.
- **Scope an opt-in Lua body hot reload.** Stock-Lua model and actual generated-code probes replace
  a body on an existing object without resetting storage/allocators. This is not implemented in the
  existing JHCR command. First verify engine `load` and delivery, then design stable semantic IDs,
  explicit lexical storage bindings, all slot aliases, ABI/version rejection, inlining dependencies,
  saved callbacks and active-frame policy. Begin with named function bodies in single-player dev
  mode; no schema/init/closure migration and no release overhead. Multiplayer needs synchronized
  payload/application ordering. See the investigation note for a staged plan and capability probe.
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

- **Do not revisit early class-metadata pruning without new evidence.** Closed PR [#1457](https://github.com/wurstscript/WurstScript/pull/1457)
  removed 1,459 classes on Castle Fight but showed no consistent end-to-end speedup. On the same current input
  (3,669 classes), the precise version retained 2,210 and the conservative prototype retained 3,584 on both targets,
  stable across three runs per variant. Keeping inheritance families and generic classes, and skipping pruning after
  allocator state exists, removed only 85 classes (2.3%) while saving 44 of 274 net added production lines (16%).
  The prototype passed 705 focused tests after changing the unused-sibling retention expectation and adding two
  conservative-policy tests; it was discarded. Fewer intermediate classes alone do not justify the analysis and its
  type-id, compile-time state, allocator and specialization obligations. Reopen only with a representative measured
  build-time or memory benefit, or evidence of a substantially smaller correct implementation; speculative future
  benefits and raw class counts are insufficient.

- **Fuzz an optimiser change against client-local code.** `LocalPlayerFuzzTests` compiles random programs full of
  `GetLocalPlayer()` and UI-state branches without and with optimisations and compares, client by client, what the Lua
  does. CI runs 25 programs; before changing what a pass does around client-local values, run a campaign
  (`WURST_FUZZ_PROGRAMS=3000`, a fresh `WURST_FUZZ_SEED`, `-PtestTimeoutMillis=3600000`) and plant a bug in the pass
  to see the campaign catch it.

- **Making the grammars SLL-clean is not worth it.** The SLL-first parse (`AntlrTokenPipeline`) parses a file again
  with the full LL prediction when SLL does not accept it, which also happens to a few valid files (`blizzard.j`,
  the standard library's `util/Cinematic.wurst`). In an unoptimised build of a large map that second parse was 2 of
  417 samples of the parse threads and none of the main thread's (Oct 2026).

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
- `WURST_LANGUAGE.md` ships as a compiler resource at
  `de.peeeq.wurstscript/src/main/resources/agent-docs/WURST_LANGUAGE.md`, not at the repository
  root. Keep it current in the PR that changes the behaviour.
- Pull requests add no `CHANGELOG.md` entries: they all edit the top of the file, so every merge
  left the open ones conflicting.
