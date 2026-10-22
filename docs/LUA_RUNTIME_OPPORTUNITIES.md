# Lua runtime opportunities

Investigation on 2026-10-16, based on master containing PR #1468. This records experiments and
implementation boundaries, not implemented optimizations. Production compiler sources are unchanged.
The probes live in [profiling/lua-opportunities](profiling/lua-opportunities/).

## Priorities

1. Evaluate exact-receiver devirtualization and temporary reuse for complex receivers. Both fit the
   existing IM better than introducing cached descriptor/function values.
2. Profile actual repeated dispatch before implementing descriptor reuse. The bounded version is
   possible without runtime guards, but the proof and register budget are the work.
3. Evaluate bulk copying for sufficiently large, compatible ranges.
4. Treat Lua hot reload as a separate, opt-in development feature, starting with an engine capability
   probe and a deliberately restricted body-replacement contract.

## Two observable-effect repros (fixed)

The investigation found two Lua miscompiles: an unread virtual call was removed although an override
incremented a global, and optimized Lua dropped an unused `new Empty()`, shifting later object IDs.
Jass was correct in both. #1471 fixed both in the effect analysis and the flatten; `ObservableEffectsTests`
holds the regressions, and the two `LuaRuntimeOpportunityProbe` repros now pass.

## What dispatch currently costs

An ordinary Lua virtual call is already a descriptor lookup followed by a slot lookup:

```lua
return __wurst_objectClass[m].A_value(m)
```

There is no additional validity-check helper to remove. Jass checked dispatch is different:
`EliminateClasses` emits null/type checks and a dispatch decision tree. Its default error reporter
can return, so hoisting a check cannot silently change the number or order of reported errors.

The optimized emission fixture demonstrates three remaining shapes:

```lua
-- Repeated local receiver, three descriptor reads.
return __wurst_objectClass[m].A_value(m)
    + __wurst_objectClass[m].A_other(m)
    + __wurst_objectClass[m].A_value(m)

-- Effectful receiver: an extra vararg dispatch stub.
return dispatch_A_value(choose(pick))

-- Exact fresh class hidden behind an interface local: still virtual.
local m = A:create()
A_data_storage[m] = 1
return __wurst_objectClass[m].A_value(m) + __wurst_objectClass[m].A_other(m)
```

`LuaMethodCallLowering` currently devirtualizes methods with no submethods. It does not propagate a
receiver's exact allocated class. A first extension could track exact types through straight-line
local assignments/copies, stop at lifetime/unknown-call barriers, and emit existing direct
`ImFunctionCall` nodes before the inliner. Keep allocation/destruction intact. Generic dispatch must
use structural type arguments and the actual most-specific method, not names. A fresh allocation's
constructor can expose or destroy it; allocation alone is not a proof after arbitrary constructor
code has run.

For a complex receiver, materializing its value once allows the existing inline dispatch path. A
disposable nine-line `Flatten.flatten(ImMethodCall)` prototype did this for call-valued receivers.
It produced `temp = choose(pick); return __wurst_objectClass[temp].A_value(temp)` and passed all 140
`LuaTranslationTests`, 236 `LuaBackendAuditTests`, and two investigation fixtures on Windows.
However, with 199 live locals, unchanged master kept locals while the prototype caused the entire
function to use `__wurst_locals`. The prototype was discarded. A real change needs temporary reuse
or a register-budget decision, correct receiver/argument evaluation order, short-circuit tests,
tuple/multiple-return tests and coverage at the locals limit. Nine source lines are not the complete
implementation.

### Descriptor reuse is a lifetime proof

For a stable local receiver, ordinary field writes do not change its class. Proving that a method
is completely pure is stronger than necessary for reusing its descriptor. But destruction followed
by allocation can recycle that same integer ID for a different class; an unchanged local does not
prove an unchanged descriptor. The lifetime fixture executes this on both lowered targets. The
unlowered IM interpreter rejects the stale object, so the fixture deliberately executes only after
transformation. A second case verifies that a mutable `box.child` must be read again after a setter.

Start with a basic block and stable local IDs. Kill facts at receiver writes, possible destruction,
unknown extern/native calls, callback re-entry and yields. Inspect all virtual implementations and
transitive callees, including cycles. A field-expression cache also needs a field-write/alias proof.
Caching a method's result is separate: reads of mutable state, time or randomness make it unsafe even
when nothing is written. No runtime epochs, per-call guards or global cache should be introduced.
Budget cached locals so a small saving cannot trigger spilling. Existing IM has no ordinary value
for a class descriptor/function slot; any lowering plan needs explicit node-based ownership rather
than reconstructing source identity in the printer. Cache analysis using function modification
counts together with callee/override dependencies; an unchanged caller body alone does not prove an
unchanged transitive summary. Avoid another unconditional whole-program traversal per sweep.

A static scan of a 9,803,082-byte Castle Fight Lua artifact found 101 simple-local receiver call
sites, 10 same-function/same-local groups with repeated sites, and 21 sites beyond the first in those
groups. The artifact also had five dispatch stubs and seven stub call sites. This was an
**unoptimized** build, a syntactic count, and neither eligibility nor runtime hotness. Comparator
helpers accounted for three of the repeated groups. Measure an optimized representative map and
its hot paths before prioritizing the general analysis.

### Stock-Lua timing experiment

Bundled Windows Lua 5.3, nine rounds, rotating variant order, ten million iterations per round,
checksums checked. Raw samples are in `dispatch-stock-lua53-windows.csv`; the driver is
`dispatch-cost.lua`. Times below are median seconds. Functions and tables are local/upvalue-bound;
these isolate mechanics and do not estimate a whole-map or in-game speedup.

| Shape | Baseline | Alternative | Observation |
|---|---:|---:|---|
| Three calls, repeated descriptor | 0.918 | 0.835, descriptor cached | About 9% lower; sample ranges overlap |
| Same, descriptor and repeated slot cached | 0.918 | 0.861 | More caching was worse than descriptor alone |
| Vararg stub around call-valued receiver | 0.591 | 0.423, receiver temporary | About 28% lower |
| Two calls through a field receiver | 0.780 | 0.666, receiver cached | Requires field stability |
| Same, receiver and descriptor cached | 0.780 | 0.627 | Requires both proofs |

The known-direct local-function variant was 0.705 s. It is an isolated lower-overhead comparison,
not a measurement of the compiler's global-function direct-call path. Repeat on the game before
claiming a performance win; its Lua and number representation differ from the test interpreter.

## Lua-native bulk copy

`ArrayList` already contains storage relocation and ordered shifts which are potential consumers.
Lua 5.3 provides [`table.move`](https://www.lua.org/manual/5.3/manual.html#pdf-table.move).
The stock probe confirms typed default reads and the destination metatable are preserved. It also
demonstrates a semantic trap: a forward overlapping loop that writes `t[i+1] = t[i]` propagates the
first value, whereas `table.move(t, 1, 3, 2)` preserves the original range. Blind loop substitution is
incorrect. Prove disjoint ranges or compatible direction/overlap semantics, stable bounds and
side-effect-free indices; preserve the counter's observable final value and empty-range behavior.

Nine alternating rounds copying 16 million total elements per variant gave these median seconds
(`bulk-copy.lua`, raw `copy-stock-lua53-windows.csv`):

| Elements per copy | Element loop | `table.move` |
|---:|---:|---:|
| 8 | 0.151 | 0.200 |
| 64 | 0.159 | 0.132 |
| 1,024 | 0.156 | 0.114 |

Small copies regressed. Establish a game crossover and actual hot consumers before adding a
compiler-owned bulk operation or narrowly lowering proven loops. Wurst arrays are not first-class
values, so an arbitrary new helper cannot simply accept their storage tables. Preserve the Jass
loop implementation and the existing generic/storage representation. A dynamic size test itself
has a cost; do not silently put one on every copy.

## Lua hot reload: feasible mechanism, substantial contract

A prototype now exists behind `-luaHotReload`: [LUA_HOT_RELOAD.md](LUA_HOT_RELOAD.md). The text below is the
investigation it started from.

The existing `wurst.hotreload` command reaches the JHCR update path in `RunMap`; it is not an existing
Lua reload implementation. Lua can load a text factory with an explicit environment and return a
closure capturing supplied storage bindings. Those are standard
[`load`](https://www.lua.org/manual/5.3/manual.html#pdf-load) and lexical-scope mechanisms; they do
not grant access to another chunk's private upvalues.

`reload-model.lua` verifies global replacement, explicit alias/inherited-slot updates, a saved
callback adapter resolving the new implementation, preserved storage/allocator identity, syntax/ABI
rejection and rejection of a competing stale patch. An arbitrary saved raw function value still
calls the old body. `reload-generated.lua` repeats body replacement against the actual optimized
emission fixture: an existing object reads 101 instead of 1, and repeated calls return 204, without
reinitializing its fields or allocator. The driver appends a manifest export in the initial chunk's
lexical scope; current compiler output does not expose such a manifest.

A plausible first implementation is **body replacement only, single-player development mode**:

- Emit an opt-in baseline manifest with stable semantic function IDs, structural generic arguments,
  explicit storage bindings and every class-slot alias. Generated names alone are not stable across
  edits. Preserve live class tables, field stores, globals and allocator state.
- Reject changed signatures, schemas, type IDs, compile-time state and initialization requirements.
  Validate compiler/config/ABI identity plus an expected patch version. Do not rerun bootstrap or
  package initialization. Source text avoids platform/version-specific bytecode.
- Disable inlining of reloadable bodies for the baseline or regenerate all dependent callers.
  Account for specialization, tuple return layouts and constants that have already been folded.
  Keep normal release output untouched.
- Compile and validate a compiler-generated factory, bind storage explicitly, then replace globals
  and the ordered manifest slot list without user calls or yields during installation. Syntax/ABI
  rejection can leave state untouched; arbitrary patch execution and subsequent body errors cannot
  be promised a state rollback. A restricted factory is not a sandbox for arbitrary code.
- Define saved function/closure behavior and active/yielded frames. Existing native callback adapters
  can resolve a rebound global, but captured raw functions and old upvalues remain old. Start with
  named functions and reject incompatible closure changes.
- Prove the real client's text loader and a viable patch delivery mechanism first. No `debug`, `io`,
  `os`, `package`, `require` or collector control is needed by the model. The test driver uses file IO
  outside the reload mechanism. Existing stdlib FileIO/sync facilities are candidates to investigate,
  not a proven transport. Multiplayer additionally requires identical payloads and application order
  on every client; local gameplay patches are not acceptable.

The body-replacement mechanism is small; a useful compiler/editor workflow with these guarantees is
a medium-sized feature. Arbitrary structural edits with live object/closure/frame migration are a
much larger undertaking. Start with the capability probe, then a development baseline manifest,
then explicit single-player body updates. Do not begin general migration or a new coroutine-based
language feature without a separate design and engine evidence.

## Reproduce

From `de.peeeq.wurstscript`, use the normal JDK/Gradle prerequisites:

```powershell
.\gradlew.bat --no-configuration-cache -I ../docs/profiling/lua-opportunities.gradle test --tests tests.wurstscript.tests.LuaRuntimeOpportunityProbe
.\src\test\resources\lua.exe ../docs/profiling/lua-opportunities/dispatch-cost.lua 10000000 9
.\src\test\resources\lua.exe ../docs/profiling/lua-opportunities/bulk-copy.lua 9 16000000
.\src\test\resources\lua.exe ../docs/profiling/lua-opportunities/reload-model.lua
.\src\test\resources\lua.exe ../docs/profiling/lua-opportunities/reload-generated.lua test-output/lua/LuaRuntimeOpportunityProbe_emission.lua
```

Use `lua5.3` on other hosts and `./gradlew` instead of the Windows wrapper. These fixtures were run on
Windows only; Linux/macOS and the real client remain unverified. `game-capabilities.lua` is an
optional maintainer-run Lua-map probe for `load` and `table.move`, never automatically injected or
run by the compiler. Record the exact client version with its results. No game was launched during
this investigation.
