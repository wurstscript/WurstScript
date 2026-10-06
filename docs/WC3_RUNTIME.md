# Warcraft III runtime facts for compiler work

What the game does that the compiler, the optimiser and the interpreter mocks have to respect. The game is the specification. The places a reader would look instead (Blizzard.j, the IM interpreter, the Lua test runtime) each differ from it, so every fact here says how it is known:

* **measured**: observed in the real client, version given. Do not overturn one by reading code; measure again.
* **code**: follows from what this repository emits or does; the pointer is the evidence.
* **reported**: community documentation, not measured here. Likely, not proven.

The rules that follow are in [AGENTS.md](../AGENTS.md) §7 and §11.

## Four places code runs, one of them is the game

| | IM interpreter (compile time, `executeProg()`) | Test Lua (`testLua(true).executeProg()`) | The game |
|---|---|---|---|
| Reals | Java `float`, round to nearest (code: `ILconstReal`) | Lua doubles (stock 5.3) | Jass: 32-bit, literals not correctly rounded. Lua: 24-bit mantissa, arithmetic truncates (measured, below) |
| Lua libraries | none | all of Lua 5.3; `os.exit()` is how `testSuccess` ends a run (code: `LuaNatives`) | Lua 5.3 without some of them (reported, below) |
| Natives | mocks in `wurstio/jassinterpreter/providers` | `wc3shim.lua` plus the Reforged `common.j`/`blizzard.j` Lua dumps (`src/test/resources/luaruntime/README.md`) | the engine |

A test that passes in the first two columns says nothing about the third where they differ. The natives the Lua backend defines itself (`LuaNatives`) are fallbacks: the script installs one only if the native is still undefined, so the real Blizzard.j always wins.

## Objects and memory

* **code** Wurst has no garbage collector. A class instance is an integer id on both targets. `new` takes an id, `destroy` runs `ondestroy` and gives the id back, and a second `destroy` is an error (`Double free: object of type X` on Jass, `Double free or invalid Wurst object.` on Lua). A closure is a class instance too and keeps its id until destroyed. (`RecycleCodeGeneratorQueue`, `LuaTranslator.createObjectManagement`.)
* **code** Both targets reuse the most recently freed id first (a stack, despite the name `RecycleCodeGeneratorQueue`), so `destroy` followed by `new` returns the same id and a stale reference then reads the new object. On Jass the ids are per class and run out at `JASS_MAX_ARRAY_SIZE` live instances (default `Constants.MAX_ARRAY_SIZE`, 32768) with `Out of memory: Could not create X.`. On Lua there is one id space shared by every class and no cap, so a stale id may come back as an object of another class.
* **code** Lua keeps one static table per field, indexed by id, and a class descriptor per live id. `destroy` removes the descriptor and recycles the id; field storage is not cleared, as Jass arrays are not. Until the id is reused a stale reference fails virtual dispatch and `instanceof` is false. Lua's collector therefore frees no Wurst object, and a table stored in a field stays reachable after `destroy`.

## The game's Lua

* **reported** It is Lua 5.3, with `debug` disabled ([Hive guide](https://www.hiveworkshop.com/threads/a-comprehensive-guide-to-mapping-in-lua.341880/)) and `collectgarbage` inaccessible since a patch, "it could cause desync" ([forum post](https://www.hiveworkshop.com/threads/wc3-reforged-lua.326354/)). The repository already treats `debug.traceback` as unavailable (`LuaTranslationTests.luaErrorWrappersAvoidUnavailableNativeTraceback`). Nothing in emitted code needs `io`, `os`, `package` or `require`; they are not known to exist there. `LuaTranslationTests.emittedLuaUsesOnlyLibrariesTheGameProvides` fails if a program's emitted helpers reach for any of these, and `everyLuaNativeFallbackUsesOnlyLibrariesTheGameProvides` checks every native in `LuaNatives`, which are emitted only on demand. Only the test-only `testSuccess` is exempt: it ends a test run with `os.exit`.
* **code** There is no `debug.traceback`, so `-stacktraces` keeps its own call stack in the script (`StackTraceInjector2`: `wurst_stack`, `wurst_stack_depth`) and every call pays for it. `wurst_run.args` defaults to `-stacktraces` without `-inline` (`WurstCommands.defaultArgs`).
* **code** Function references handed to natives get an `xpcall` adapter (`LuaTranslator.callbackAdapterFor`) and package init is `xpcall`-wrapped, so an error is reported through the Wurst error function or `BJDebugMsg` instead of vanishing.
* **code** Stock Lua 5.3 does no run-time optimisation: `luac -l` on a loop that calls a one-line local function and reads a global and `math.floor` shows a `CALL` for the function and a `GETTABUP`/`SETTABUP` or `GETTABUP`+`GETTABLE` for each access, every iteration. If the game's VM is the reference 5.3 (reported above), every helper call, global read, table index and allocation the compiler leaves in runs, and the compiler's inliner and local optimisations are the only optimiser.
* **code** Jass has no `throw`. `I2S(1 div 0)` is the deliberate thread abort (`ErrorHandling.error()` ends with it); Lua lowers exactly that shape to `error("__wurst_abort_thread", 0)`, which the callback handlers ignore (`ExprTranslation.isIntentionalThreadAbortCall`, `LuaNativeLowering`). Folding or reshaping it breaks the abort (`LuaBackendAuditTests`, `OptimizerTests`).

## Lockstep

Every client simulates the same game from the same inputs; any value that differs between machines desyncs it.

* **reported** The iteration order of `pairs()` is not the same for different players ([SyncedTable](https://www.hiveworkshop.com/threads/syncedtable.332894/)). So emitted Lua and stdlib code that compiles to Lua never iterate a table whose order is observable. A table is safe as a membership set (`t[k] = true`, `t[k] ~= nil`, `t[k] = nil`); anything iterated needs a separately kept insertion-ordered array, which is what a sparse set's dense array buys.
* **code** The keyed table offers no iteration (`LuaNatives`). `__wurst_keyedMapDestroy` does loop with `pairs`, but only to set every entry to nil, which does not depend on order.
* **code** The compiler's own output must be byte-identical for identical input (AGENTS.md §3).

## Numbers

* **measured, 3.0.0** Jass literals and `S2R` are not correctly rounded and the two disagree: `0.1` reads one float high, `1.1` one float low, `S2R("100.0")` one above 100. Short exact binary fractions (`0.5`, `0.75`, `100.0`, `123456.78125`) read exactly; integer parts past 2^24 are truncated. This is why `SimpleRewrites.foldRealExactly` folds only exact literals and exact results.
* **measured, 3.0.0** Lua reals have a 24-bit mantissa, not 53: `(2^24 + 1) - 2^24` is 0, the literal `16777217.` reads as 16777216, `0.9999999999999999` reads as exactly 1, and `0.7 + 0.1 + 0.1 + 0.1` computes to 1 - 2^-23. Literals are rounded to nearest and each arithmetic result is truncated, which neither double nor 32-bit float arithmetic reproduces. One `R2I` result is not evidence of a precision: `R2I(0.7 + 0.1 + 0.1 + 0.1)` is 0 for that reason, not because of doubles.
* **measured, 3.0.0** `R2I` and the arithmetic `x // 1 | 0` agree on every value probed, the 32-bit edges and values outside the range included (`LuaNativeLowering.lowerRealToInt`).

## Establishing a fact

* Measure in the real client with discriminating values, such as `2^24 + 1` or sums just below an integer, computed at run time from array operands so that nothing is folded, and read the emitted Lua to confirm. State the client version where the fact is recorded.
* Never launch Warcraft III yourself: it needs the maintainer's Battle.net sign-in and an install that matches the pinned patch. Ask for a probe.
* Benchmark Lua output like a shipping map: `-inline -localOptimizations` and no `-stacktraces`, which inflated call-heavy blocks 3-4x. Accumulate inside a timed loop in a local, not a global: a global's lookup cost follows the global table's hash layout, which shifts when identifiers are renamed, so a block whose Lua is byte-identical moved 15% between builds. Compare alternating runs, at least three per build, and treat shifts under 15% in global-touching blocks as noise.
