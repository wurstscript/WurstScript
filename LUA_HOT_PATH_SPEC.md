# Lua hot-path emission: four compiler tasks

Specification for removing compiler-introduced overhead from the Lua backend's emitted code.
Written from reading the release output (`-inline -localOptimizations`, no `-stacktraces`) of the
current compiler (`1.9.0.0-nightly-2-gd1bf06792`, which already contains #1280 and #1282) for the
standard library's `UnitSpatialIndex` and `SpatialPartition` packages, plus the inliner's own
decision log. Every claim below was read off that output, not inferred.

Do the tasks in the order given. Task 2 is the cheapest and unblocks measuring the rest.

## Policy this spec implements

1. **Wurst-emitted constructs are consumed by Wurst code.** The compiler does not guard, coerce or
   normalise emitted tables, arrays or values against foreign Lua that might mutate them. A user who
   bundles raw Lua that writes into Wurst-emitted tables owns the consequences. Any wrapper, nil
   check or coercion whose only justification is "external code could have written here" is a bug,
   not a safety feature. This retires the "foreign writes" rationale introduced in #1280.
2. **Lua-native mechanisms over emulation.** Where Lua has a direct construct for what Wurst needs
   (a metatable default, a table index, an integer operator, a fixed-arity function), emit that
   construct. Do not emit a helper call, a vararg pack, or an IM-level shim as a workaround.
3. **A call in a hot loop is the most expensive thing the emitted code can do**, after a table
   allocation. Compiler-introduced calls and allocations on ordinary typed code paths are defects.

## Evidence

Inner loop of `spatialIndexBeginQuery` as emitted today, per visited entry:

```lua
next4 = __wurst_ensureInt(UnitSpatialIndex_nextInCell[idx9])
temp34 = __wurst_ensureReal(UnitSpatialIndex_lastX[idx9])
dy1 = (__wurst_ensureReal(UnitSpatialIndex_lastY[idx9]) - center_y)
```

`__wurst_ensureInt` is itself two nested calls (`__wurst_rawToNumberInt` then `__wurst_rawToInteger`),
`__wurst_ensureReal` one. That is seven Lua calls per visited entry before any work happens, on
arrays declared with a metatable whose `__index` already returns the typed default.

Inliner decision log for the same build (`-Dwurst.inliner.log=true`), 1678 call sites:

| decision / reason | count |
|---|---|
| keep: `local_player_context_barrier` | 577 |
| keep: `native` | 556 |
| inline | 326 |
| keep: `rating_too_high` | 146 |
| keep: `not_in_inlinable_set` | 54 |
| keep: `lua_callback_funcref_barrier` | 17 |

105 of 546 distinct callees are refused by the local-player barrier. They include `max`, `min`,
`headSlot`, `groupSlot`, `coarseSlot`, `cellAt`, `cellCoordX`, `blockOfCell`, `currentMaxDisplacement`,
`unit_getX`, `rect_getMinX`. None of them touches a client-local native. Section "Task 2" explains why.

Vararg lowering, `max(vararg int)` as emitted:

```lua
function max(...)
	local __args1 = table.pack(...)
```

One table allocation per call. `cellCoordX` calls `max` and `min` once each, so every relink in the
sweep allocates four tables, and `ArrayList.add(vararg T)` allocates one per element added.

## Ground rules for whoever implements this

- Follow `AGENTS.md`: failing test first, minimal patch, deterministic iteration order, both
  backends validated. Run the focused suites named in each task, then the full suite once at the end.
- **Do not** solve any task with a name-based exclusion (no lists of stdlib function names, no
  package-name checks, no `startsWith("__wurst")` tests beyond those that already exist).
- **Do not** keep the removed behaviour behind a flag, run arg, or annotation "just in case". Delete it.
- **Do not** narrow a task to the stdlib functions named here. The fixes are structural and apply to
  every program.
- **Evidence over reasoning.** For inliner questions, run with `-Dwurst.inliner.log=true` and read the
  `[INLINER]` lines. For emitted-shape questions, read the Lua. The test helper
  `LuaBackendAuditTests.compileOptimizedLua` compiles with release flags and returns the source.
- **One task, one branch, one PR**, in the order given. Do not start Task 3 on a branch that still carries Task 1. Commit and PR conventions from `LOOP.md` apply (no AI or co-author references anywhere).
- **Identity, not names.** Where a task says to recognise a compiler-synthesised function (the raw div/mod natives in Task 4), compare against the `ImFunction` instance that `LuaNativeLowering` created and recorded, not its name string. AGENTS.md §7 forbids name comparison for semantic identity; keep a reference on `ImTranslator` the way `ensureIntFunc` is kept.
- When a listed test's assertion encodes the behaviour being removed, invert or delete that
  assertion and say so in the commit message. Do not weaken unrelated assertions in the same test.

---

## Task 1: Typed primitive array reads are raw table indexes

### Current behaviour

Two places wrap primitive array reads in `__wurst_ensureInt` / `__wurst_ensureReal` /
`__wurst_ensureStr`, or `(x == true)` for booleans:

1. `LuaNativeLowering.lowerPrimitiveArrayBoundaryEnsure` (`translation/imtranslation/LuaNativeLowering.java`).
   Its `visit(ImVarArrayAccess)` wraps **every** rvalue primitive array read in the program. Its
   `visit(ImFunctionCall)` additionally wraps array reads passed to natives. The Javadoc says
   "Arrays can be visible to foreign Lua/Jass code, so a present value can be malformed". That is the
   rationale this spec retires.
2. `ExprTranslation.wrapLuaAtExternalBoundary` (`translation/imtranslation/ExprTranslation.java`)
   wraps `ImVarArrayAccess` arguments at native call sites during AST-to-IM translation.

#1280 exempted class field storage (`X_field_storage[this]`) from this. Package-level and local
arrays still pay. In the probe build, 86 `__wurst_ensureInt` and 6 `__wurst_ensureReal` call sites
remain, every one of them on a typed array read.

### Why the wrapper is dead weight

`LuaTranslator.getOrCreatePrimitiveArrayMetatable` gives every primitive-typed array a metatable
whose `__index` returns the typed default (`0`, `0.`, `false`, `""`). `defaultValue` installs it for
globals, locals, and nested arrays (`newDefaultArray`). A read of an unwritten key therefore already
yields the correct default with no call. A written key holds whatever typed Wurst code wrote, which
the type checker guarantees is a value of the declared type. There is no third case.

`ensureInt` also applies `math.tointeger`. No Wurst integer expression produces a Lua float:
`div` lowers through `//` on integers, `R2I` uses `math.floor`/`math.ceil` which return the integer
subtype for representable values, and bit natives return integers. So removing the coercion changes
no observable value.

### Required change

- Delete `lowerPrimitiveArrayBoundaryEnsure` and its call from `LuaNativeLowering.transform`, plus
  the helpers that exist only for it (`replaceWithEnsure`, `isExternalBoundary`, `isAlreadyNormalized`,
  `isAlreadyNormalizedAccess`, `ensureFunctionFor`, and `callWithStacktrace`/`stacktraceParamIndex`
  if nothing else uses them).
- In `ExprTranslation.wrapLuaAtExternalBoundary`, remove the `ImVarArrayAccess` branch. The method
  should then be a no-op; delete it and its call sites if so.
- Fix the now-wrong Javadoc on `lua.translation.ExprTranslation.translate(ImVarArrayAccess)`, which
  claims reads arrive pre-wrapped.
- **Keep** `ExprTranslation.wrapLua` and the `WurstTypeBoundTypeParam` normalisation. Erased generic
  storage genuinely can hold `nil` for a primitive; that path is out of scope and must not regress.
  The `ensure*` helper functions stay for it.
- Do not add any replacement flag, annotation, or opt-in.

### Tests

Existing assertions that encode the removed behaviour, all in `LuaBackendAuditTests`:

- `erasedGenericPrimitiveDefaultsPropagateThroughCompositeContexts`: the final assertion with
  message "global primitive array reads must remain safe for foreign writes" asserts
  `__wurst_ensureInt(Test_values[0])` is present. Invert it: assert the compiled source contains
  `return Test_values[0]` and does **not** contain `ensureInt(Test_values`. Keep the count assertion
  on `Box_Box_get` unchanged (that is the erased-generic path).
- `seededTypeAssuranceBoundaryFuzz`: `readNormalization` and `arrayArgument` currently assert the
  wrapped form for `read()` and for the native call argument. Change both to assert the **raw**
  access (`TypeAssuranceFuzz_values[N]`) and assert `__wurst_ensure` is absent from `read`'s body
  and from the array argument. Keep the `genericArgument` assertions (erased-generic path) as they
  are. Keep the "ordinary typed values must not be normalized" assertion.
- Grep the whole test tree for `ensureInt(`, `ensureReal(`, `ensureStr(`, `ensureBool(` and
  `== true)` assertions on array reads and update any others the same way.

New tests, in `LuaBackendAuditTests`:

- **Emitted shape.** Compile with `compileOptimizedLua` a package with `int array`, `real array`,
  `bool array`, `string array` globals, a local `int array[8]`, and a function that reads each in a
  `while` loop and passes one read to a `native`. Assert no `__wurst_ensure` and no `== true)`
  appears anywhere in the output except inside functions whose name starts with `__wurst_`.
- **Runtime defaults still hold.** A `test().testLua(true).executeProg()` program that reads never-
  written slots of all four primitive array types (including index `0`, a large index, and a slot of
  a local sized array) and asserts `0`, `0.`, `false`, `""`; then writes `0`/`false`/`""` explicitly
  and reads them back. This pins that the metatable, not the wrapper, was carrying the default.
- **Stacktrace mode.** Compile the same shape with `new RunArgs().with("-lua", "-stacktraces")` and
  assert the same absence. The wrapper used to receive a stacktrace argument; make sure nothing
  else did.

Focused suites: `LuaBackendAuditTests`, `LuaTranslationTests`, `LuaTypecastingTests`,
`LuaNativesTests`, `LuaRunnerTests`, `FastHashMapTests`, `StdLibOwnTests`.

---

## Task 2: The local-player inlining barrier must not fire on control taint

### Root cause, precisely

`LocalPlayerContextAnalyzer` (`intermediatelang/optimizer/LocalPlayerContextAnalyzer.java`) is a
whole-program, flow-insensitive fact propagation. Three edges combine into the over-approximation:

1. `indexFunctionCall`: `addEnclosingControlDependency(controlContext, entryControlFact(called))`.
   A callee's entry-control fact depends on the control context of **every** call site. A function
   body's top-level control context is its own entry-control fact, so this is transitive over the
   call graph.
2. `indexElementAfterChildren`, `ImReturn` case:
   `addEnclosingControlDependency(controlContext, returnFact(owner))`. A function's RETURN fact
   fires whenever its entry control is tainted, regardless of what it returns.
3. `functionInliningIsLocalPlayerSensitive` returns true when `localPlayerDependentReturns.contains(f)`.

Consequence: any function reachable, through any chain of calls, from inside any
`if GetLocalPlayer() == ...` block anywhere in the program has a tainted RETURN fact and is refused
by the inliner at **every** call site, including ones nowhere near client-local code. In a program
that links the standard library that is most of the call graph. `headSlot(cell, groupId)` returns
`cell * 8 + groupId` and is refused.

Edges 1 and 2 are correct for the passes that need control facts (`BranchMerger`,
`ConstantAndCopyPropagation`, `LocalMerger`, `TempMerger`, all via `isLocalPlayerDependent`). They
are irrelevant to inlining: substituting a callee body at a call site executes that body under
exactly the control context the call already had. Nothing moves across a client-local boundary.
Those passes run after inlining and re-analyse the inlined program, where the control context is
explicit, so they lose nothing.

### Required change (implemented on `fix/inliner-local-player-barrier`)

The barrier answers: does this function call a client-local native directly, or is its **return
value derived from one by the data flow of its own body**, independent of what callers pass in.

Two simpler rules were tried first and are wrong; do not go back to them:

- *USE facts only* ("transitively calls a client-local native") breaks `OptimizerTests.testInlineAnnotation`:
  with the stdlib linked, `print` reaches `GetLocalPlayer`, so every function that prints stops
  inlining. Calling something that uses a client-local value is not the same as producing one.
- *Data-only return facts with the ordinary argument-to-parameter edges* still barriers `max`, `min`,
  `headSlot` and `cellCoordX` (75 functions in the probe). The analysis is context-insensitive: a
  parameter fact merges the arguments of **every** call site, so one `max(...)` call anywhere with a
  client-local argument taints `max`, then everything computed from its result.

The implemented shape in `LocalPlayerContextAnalyzer`:

- A second dependency map, `dataDependents`, receives every edge added through `addDependency`.
  Control edges (`addEnclosingControlDependency`) and call-site argument-to-parameter edges
  (`addCallArgumentDependency`, used in `indexFunctionCall` and `indexMethodCall`) go into the full
  graph only.
- `propagateDataFacts()` runs after `propagateFacts()`, walks `dataDependents` from the same sources,
  and publishes only RETURN facts into `localPlayerDataDependentReturns`.
- `functionInliningIsLocalPlayerSensitive(f)` is `isClientLocalValueSource(f) || functionsDirectlyUsingLocalPlayer.contains(f) || localPlayerDataDependentReturns.contains(f)`.

Every other consumer of the analysis (`isLocalPlayerDependent`, `functionUsesLocalPlayer`) is
unchanged and still reads the full graph.

Measured on the stdlib probe with `-Dwurst.inliner.log=true`: `local_player_context_barrier` went
from 577 call sites on 105 functions to 10 call sites on 5 functions (`init_Player`,
`PingMinimapForPlayer`, `GetPlayableMapRect`, `GetCurrentCameraBoundsMapRectBJ`, `InitMapRects`), each
of which really reaches a client-local native.

### Tests

- New, in `OptimizerTests`, Jass output (matches the style of `functionUsingGetLocalPlayerMustNotBeInlined`):
  a pure `@inline function slot(integer a, integer b) returns integer` returning `a * 8 + b`, called
  once inside `if GetLocalPlayer() == Player(0)` and once in plain code. Assert the `_inl.j` output
  contains **no** `call slot(` and no `slot(` at all: both sites inlined. Add a second helper that
  wraps `GetLocalPlayer()` and assert it remains a call at both sites.
- New, in `LuaBackendAuditTests` with `compileOptimizedLua`: an `int array` with two `@inline` index
  helpers, a query loop, and one unrelated function containing a `GetLocalPlayer()` branch that calls
  one of the helpers. Assert the loop body contains the arithmetic inline and no call to either
  helper, and assert no `function <helper>(` definition survives (garbage removal drops it).
- Existing tests that must keep passing unchanged: `OptimizerTests.functionUsingGetLocalPlayerMustNotBeInlined`,
  `localPlayerControlMustPropagateThroughCalledFunctions`, `localPlayerControlMustPropagateIntoFunctionReturns`,
  `statementsAfterLocalEarlyReturnMustRemainLocallyControlled`, `branchMergerMustNotHoistAcrossClientLocalConditions`,
  every `LocalPlayer*` test in `LuaBackendAuditTests`.

Focused suites: `OptimizerTests`, `LuaBackendAuditTests`, `LuaTranslationTests`, `InterpreterTests`.

### Expected effect, to verify with the log

Re-run a stdlib-linked compile with `-Dwurst.inliner.log=true`. `local_player_context_barrier` must
drop from hundreds to the handful of functions that really call a client-local native. `headSlot`,
`groupSlot`, `cellAt`, `max`, `min` (after Task 3), `__wurst_intDiv`, `__wurst_safe_GetUnitX` and
`unit_getX` must show `decision=inline`.

---

## Task 3: Vararg calls with a static argument count are fixed-arity on Lua

### Current behaviour

On Jass, `VarargEliminator` (`translation/imtranslation/VarargEliminator.java`) runs in
`WurstCompilerJassImpl.transformProgToJass` after `StackTraceInjector2` and before inlining. It
generates one copy of each vararg function per distinct call arity, unrolls the `ImVarargLoop`, and
redirects the calls. Varargs never reach the backend.

On Lua, `transformProgToLua` never runs it. `LuaTranslator` renames the last parameter to `...` and
`lua.translation.StmtTranslation.translate(ImVarargLoop, ...)` emits `table.pack(...)` plus a
`while` loop. `ImInliner.isInlineCandidate` refuses vararg functions. So `max(a, b)` allocates a
table, loops over it, and can never be inlined.

### Required change

Run the same elimination on Lua, with three adjustments:

1. **Placement.** In `transformProgToLua`, run `new VarargEliminator(imProg).run()` after
   `StackTraceInjector2` and after the Lua generics specialisation (`transformGenericNewOnly`), and
   before `LuaNativeLowering.transform` and inlining. That is the same relative position Jass uses,
   and it means the specialised copies (`ArrayList_add_specialized`) are what get arity-split.
2. **No Jass parameter cap on Lua.** `generateVarargFunc` throws when the flattened Jass arity exceeds
   `ImHelper.JASS_MAX_PARAMETERS` (31). Lua's limit is far higher. Give the eliminator a target-aware
   limit: on Lua, specialise up to a generous fixed bound (200 parameters is Lua 5.3's local limit;
   pick something below it, e.g. 100) and, above that, **leave that call and that function on the
   existing `...` path** instead of erroring. `VarargTests.varargAllowsMoreThan31ArgumentsInLua`
   (32 arguments) must therefore compile to a 32-parameter function and pass.
3. **Do not delete originals on Lua.** `run()` does `prog.getFunctions().removeIf(f -> f.hasFlag(IS_VARARG))`.
   On Jass classes are already eliminated so every call is an `ImFunctionCall`. On Lua classes are
   still present: a vararg function may also be reached through `ImMethodCall` (virtual dispatch) or
   `ImFuncRef`, and class methods live in `ImClass.getFunctions()`, not `prog.getFunctions()`. On Lua,
   redirect `ImFunctionCall` sites only, keep every original, and let `RemoveGarbage` drop the ones
   that end up unreferenced. Vararg methods still called virtually keep the `...` lowering; that is
   acceptable and rare.

Tuples: `EliminateTuples` runs after this point on both backends and already handles the multi-
parameter form (Jass has done this for years). `EliminateTuples.preserveVarargParameter` only applies
to functions still flagged `IS_VARARG`, which after elimination is only the `...`-path leftovers.

Stack traces: `StackTraceInjector2` inserts the stack parameter second-to-last for vararg functions.
`generateVarargFunc` removes the last parameter and appends the arity copies, so the stack parameter
keeps its position and `redirectCall` keeps argument order. Jass proves this ordering works.

After this task, `ImInliner.isInlineCandidate`'s `IS_VARARG` refusal no longer applies to the
specialised copies, so `max_2(a, b)` (size well under 20) inlines to a conditional.

### Tests

Existing assertions to update, in `LuaBackendAuditTests`:

- `optimizedTupleVarargLoopUsesAttachedScalarLocals` asserts `table.pack(...)` is present. Invert:
  assert it is **absent**, and assert the specialised `add` has two scalar element parameters and no
  loop. Keep `assertFalse(compiled.contains("tupleCopy"))`.
- The `ImVarargLoop` visitor in `compileLuaWithRunArgs` stays; it simply finds no loops.

Existing tests that must pass unchanged: `VarargTests` (all, including
`varargAllowsMoreThan31ArgumentsInLua` and `tupleVarargPreservesElementGroupingInLua`),
`LuaBackendAuditTests.varargLoopWithBareReturn` (runtime + shape),
`localPlayerTaintFlowsThroughVarargLoopValues`, `ClassesTests.constructor_chaining_vararg`,
`LuaTranslationTests.luaFunctionRefWrapperForwardsVarargs` (that one is about `xpcall` wrappers, not
Wurst varargs, and must be untouched).

New tests, in `LuaBackendAuditTests`:

- **Shape.** With `compileOptimizedLua`, a `function biggest(vararg int xs) returns int` called as
  `biggest(a, b)` and `biggest(a, b, c)` from a loop. Assert no `table.pack` in the output and no
  `function biggest(` definition with `...`. Assert the two-argument call site was inlined to
  comparisons (no `biggest` call remains) or, if the inliner rating refuses it, that a
  `biggest_2(` and `biggest_3(` pair exists with fixed parameters.
- **Runtime parity.** `test().testLua(true).executeProg()` covering: zero varargs, one, several,
  tuple varargs, a vararg function forwarding its varargs to another vararg function, an early
  `return` inside the loop, and a vararg class method called directly on a concrete class. Each
  asserts the same results the interpreter gives (`testSuccess()`).
- **Fallback.** A call with more arguments than the chosen Lua bound compiles, runs, and still
  contains `table.pack` for that function only.
- **Virtual dispatch leftover.** An interface with a vararg method and two implementations, called
  through the interface. Compiles and runs; the implementations keep `...`.

Focused suites: `VarargTests`, `LuaBackendAuditTests`, `LuaTranslationTests`, `ClassesTests`,
`GenericsTests`, `StdLibOwnTests`, `OptimizerTests`.

---

## Task 4: Integer `div`/`mod` lower to operators, not helper chains

### Current behaviour

`LuaNativeLowering.lowerDivMod` rewrites `DIV_INT` to `__wurst_intDiv(a, b)`, an IM function whose
body calls `__wurst_rawFloorDivInt(a, b)`, a "native" whose Lua body is `return a // b`
(`lua.translation.LuaNatives`). `MOD_INT` becomes `__wurst_modInt` calling `__wurst_rawFmodInt`
which is `return math.fmod(a, b)`. In the probe build `__wurst_intDiv` was never inlined (local-player
barrier, Task 2), so one `div` was three Lua calls.

### Required change

- The three raw natives (`__wurst_rawFloorDivInt`, `__wurst_rawFmodInt`, `__wurst_rawFmodReal`) are
  intrinsics at the Lua backend, not functions. In `lua.translation.ExprTranslation.translate(ImFunctionCall, ...)`
  (the same place that already pattern-matches the `I2S(1 div 0)` abort trap), translate a call to
  `__wurst_rawFloorDivInt` to the binary `//` expression and calls to the two fmod natives to a direct
  `math.fmod(a, b)` call expression. Do not emit the function definitions when they are only used as
  intrinsics.
- After Task 2, `__wurst_intDiv` and `__wurst_modInt` (size under 20) inline at every call site.
  Verify with the log; if `ImInliner` still refuses them for a reason other than the barrier, fix
  that reason, do not special-case the names.
- Do **not** touch `WurstOperator.moduloInteger`, the interpreter, or constant folding. AGENTS.md §7
  requires all div/mod semantics to stay centralised; this task changes only how the raw primitive is
  spelled in the emitted Lua.
- Leave the `I2S(1 div 0)` abort-trap recognition exactly as it is
  (`i2sDivByZeroAbortTrapSurvivesDivModLowering` pins it).

### Tests

Existing tests that must pass unchanged: `LuaBackendAuditTests.integerDivModMatchJassSemanticsInLua`,
`integerDivModReferenceSemanticsInInterpreter`, `i2sDivByZeroAbortTrapSurvivesDivModLowering`,
`nonConstantDivModCallsUseSharedHelper`. `divModHelpersAreOmittedWhenUnused` asserts the raw native
"always survives somewhere"; update it to assert the `//` operator or `math.fmod` appears instead,
and that no `__wurst_rawFloorDivInt` function definition is emitted.

New test, `LuaBackendAuditTests` with `compileOptimizedLua`: a loop doing `x div 8` and `x mod 8` on
runtime values. Assert the loop body contains `// 8` (or the inlined `intDiv` body using `//`) and
`math.fmod`, and no call to `__wurst_raw`.

Negative-operand semantics are the whole risk here; the runtime parity test
`integerDivModMatchJassSemanticsInLua` already covers `-7 div 2`, `7 mod -2` and friends. Run it under
`testLua(true)`.

---


---

## Task 5 (follow-up, measured after Task 2): the rating formula refuses tiny popular helpers

With the barrier fixed, the inliner log on the stdlib probe shows the next reason small leaves stay
as calls in hot loops is `rating_too_high`:

| callee | body | typical decision |
|---|---|---|
| `real_floor` | `toInt` plus a sign correction | `rating_too_high(875.0>=50.0)` |
| `unit_getX` | `return __wurst_safe_GetUnitX(this)` | `rating_too_high(118.0>=50.0)` |
| `unit_getAbilityLevel` | one nil-safe native wrapper | `rating_too_high(252.0>=50.0)` |
| `__wurst_intDiv` | floor-div plus one correction | `rating_too_high(992.0>=100.0)` |
| `__wurst_ensureInt` | two nested coercions | `rating_too_high(1088.0>=50.0)` (gone after Task 1) |

`ImInliner.getRating` is `size * (callCount - 1)` against a threshold of 50 (100 when an argument is a
constant), with an early "always inline" only when `estimateSize(f) < 20`. `estimateSize` counts every
IM node, so a one-line wrapper around a nil-safe native is already past 20, and any such wrapper with a
handful of callers is refused everywhere. That is backwards for Lua: the cost the formula guards
against is emitted-script size, and duplicating a twenty-node body at each of ten call sites is cheaper
at runtime than ten calls in a loop and negligible in size.

### Required change

- Raise the unconditional small-body threshold for the Lua target so that a body consisting of a
  single return of one call or one arithmetic expression, with or without a nil guard, always inlines
  regardless of call count. Derive the number from `estimateSize` of exactly those shapes (measure
  `unit_getX`, `__wurst_safe_GetUnitX`, `real_floor`, `__wurst_intDiv` in the log with a temporary
  print, then set the threshold just above the largest), and record the measured sizes in the test.
- Keep the Jass behaviour unchanged unless the same measurement shows the same win there; the map
  script size limit is a real constraint on Jass and is not on Lua.
- Do not special-case names. Do not make `@inline` the answer: stdlib authors should not have to
  annotate every one-line accessor, and user code will not.

### Tests

- `LuaBackendAuditTests`, `compileOptimizedLua`: a one-line nil-safe native wrapper and a one-line
  arithmetic helper each called from eight distinct functions. Assert neither helper is called from
  any of the eight bodies.
- `OptimizerTests.testInlineAnnotation` and every existing inliner test unchanged.
- Re-run the stdlib probe log: `real_floor`, `unit_getX`, `unit_getAbilityLevel`, `__wurst_intDiv`
  must show `decision=inline`.

## Acceptance for the whole spec

Compile the standard library's `UnitSpatialIndex` on Lua with release flags (a `withStdLib()` test
that imports `SpatialIndexForUnits` and calls `unitsInRange` is enough) and read
`spatialIndexBeginQuery`. The inner `while` body over a cell chain must be, modulo local names:

```lua
next4 = UnitSpatialIndex_nextInCell[idx9]
dx1 = (UnitSpatialIndex_lastX[idx9] - center_x)
dy1 = (UnitSpatialIndex_lastY[idx9] - center_y)
cachedDistSq = ((dx1 * dx1) + (dy1 * dy1))
```

with no `__wurst_ensure`, no `table.pack`, no call to any `@inline` leaf, and `cellCoordX` reduced to
arithmetic plus at most one `R2I`/`math.floor` call. The `SpatialPartition` query must read
`SpatialPartition_cellHead[((rowBase + cx) * 8) + groupId5]` directly.

Then run the full suite once.

## Measurement note for stdlib authors

`wurst_run.args` in a generated project defaults to `-stacktraces` and no `-inline`. Every emitted
function then pays `wurst_stack` bookkeeping on entry and exit, and no leaf is inlined. Benchmarks of
emitted code that are meant to inform stdlib design must use `-inline -localOptimizations` without
`-stacktraces`, or they measure the debug configuration.
