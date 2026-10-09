# AGENTS.md

This document defines how automated coding assistants (“agents”) should interact with this repository.

## 1. Repository Overview

This repository contains the **WurstScript compiler**. Its main code lives in:

```
de.peeeq.wurstscript/
```

`HelperScripts/` holds the generators for the compiler's game-data resources and the stdlib's ability wrappers (see `HelperScripts/GAMEDATA.md`); change it only for that work. Design notes live in `docs/`. `BACKLOG.md` holds open work and standing notes (test conventions, benchmark flags).

**Read §11 and [`docs/WC3_RUNTIME.md`](docs/WC3_RUNTIME.md) before changing a backend, the optimiser or an interpreter mock.** The game differs from what the code, the interpreter and the tests suggest: Wurst has no garbage collector, the game's Lua is not the test Lua, and nothing optimises the emitted script at run time.

### Sibling projects

Separate repositories, checked out next to this repository's main checkout (in a git worktree `../` is not that folder). We own all of them. A defect is fixed in the project that owns it (§3), then the pin here is bumped where there is one. Read a sibling's sources; never decompile its jar.

* **WurstStdlib2**: the standard library. Tests fetch a pinned copy into `de.peeeq.wurstscript/temp/` (`ensureStdLib`, skipped with `-PskipStdLibFetch`); that copy is a fetched artefact, so change the library in its own repository. The library requires the latest compiler: add no old-compiler fallbacks. Only a change to its API or observable behaviour is breaking.
* **WurstSetup** (`grill`): the CLI that installs dependencies and runs `-build` through `CliBuildMap`.
* **wurst4vscode**: the VS Code extension, the LSP client (§5).
* **wurst-project-config**: the shared `wurst.build` model (§6).
* **wc3libs** (pinned in `build.gradle`) and **JMPQ3** (reached through wc3libs): Warcraft file formats and MPQ.
* **jass-history**: `common.j`, `blizzard.j` and Lua dumps per patch (§6, `luaruntime/README.md`).
* **abstractsyntaxgen**: the `.parseq` generator (above). **casc-ts**: the CASC reader for game data (§12).

### Compiler layout

Inside `de.peeeq.wurstscript`:

* **`src/main/antlr/de/peeeq/wurstscript/antlr/`**
  Contains the **ANTLR grammars** (`.g4`) for Wurst and Jass.
  These produce concrete syntax trees (CSTs).

* **`parserspec/`**
  Contains **.parseq grammars** for `abstractsyntaxgen`
  ([https://github.com/peterzeller/abstractsyntaxgen](https://github.com/peterzeller/abstractsyntaxgen)).
  These define the AST structure used by the compiler.
  Code is generated via the Gradle task:

  ```
  ./gradlew :gen
  ```

* **`src/main/java/de/peeeq/`**
  Main compiler sources:

  * Parsing and AST infrastructure
  * Type checking
  * Intermediate language (**IM**)
  * Jass and Lua backends
  * Interpreter for executing IM at compile time
    (used for specific compile-time evaluations)

### Compilation pipeline (simplified)

1. Parse Wurst/Jass with ANTLR → CST
2. Abstractsyntaxgen → AST
3. Transform AST → IM
4. Optionally: Run IM in the interpreter, Optimize
5. Transform IM → Backend (Jass or Lua)

### Two generic systems

Two incompatible generic systems coexist. The syntax of the type parameter picks one (`WurstValidator.isTypeParamNewGeneric`), and one declaration cannot mix them.

* **Old `<T>`**: the parameter is erased on both targets. A value enters and leaves as an `int` through casts (`ImCast` between `ImAnyType` and int or class), so values of unrelated types share one integer space, and on Lua int `0` needs a sentinel (`LuaOldGenericsCasts`).
* **New `<T:>`**: `EliminateGenerics` specialises each concrete type argument into its own copy with the type's native representation and its own type-id space; nothing is cast to `int`. Jass specialises every use; Lua only the paths that need the concrete type (construction, bounded dispatch, tuple storage, runtime type identity: `transformGenericNewOnly`) and keeps the rest erased.
* Know which one you are in before changing generic code. A fix that suits one is usually wrong for the other: never `castTo int` a new-generic value, never instantiate an old generic with a new-style type parameter (a compile error), and test both. Do not migrate the old containers (`BACKLOG.md`). Language rules: `WURST_LANGUAGE.md`, "New generics and native representations".


### Language and tooling

* **Java 27**
* **Gradle (9.8.1)**

---

## 2. Agent Expectations

* **All existing tests must continue to pass.** The authoritative behavior is defined by the existing test suite.
* **Test-driven**: new behavior requires tests showing failure before the change and success after, in the existing test style. Bug fixes start with a failing repro.
* **Minimal, well-scoped edits**: small local patches; no large refactors (renames, structural moves, mass rewrites), no altered language semantics without tests demonstrating the intended outcome, no new external dependencies unless requested.
* **Generated code**: never modify files generated by `:gen`; change the `.parseq` specs or grammars and regenerate (`./gradlew :gen`). Adding a node type to a `.parseq` sum type breaks every exhaustive matcher at compile time — fix those compile errors first. Visitors with defaults, `instanceof` chains and `default` switch arms do not break, so also grep for the sibling node types.

---

## 3. Coding Guidelines

* **Fix a defect where it originates.** The whole toolchain is ours: this compiler and the siblings in §1 (jmpq3, wc3libs, grill, the VS Code extension, the stdlib), so there is no external tool to work around. Do not monkey-patch, filter or compensate in a later pass, in a caller or in this repository for something an earlier pass or a sibling got wrong. Change the pass that produced it (§7 for IM) or the project that owns it. A workaround leaves the defect in place for every other consumer.
* **Do not take the current shape of the compiler for granted.** It grew organically, and the Lua backend began as "make Lua mode usable": much of what it emits was simply the easiest thing to emit, and the Lua fixes since then keep finding waste that had been there all along (`git log --grep "Lua"`: unread dispatch slots were about 40% of a real map's script, a closure's name repeated three times, a nil-checking helper ran on every string join). Treat a slow emission, a pass that parses names, or a pass that exists to undo another as a finding, not a precedent: measure it against what the output should be and fix the root. Keep each change small (§2), but raise bigger ideas as a written proposal with evidence in `BACKLOG.md` (a different IM shape, pass order or generated-AST design), and do not start one unasked.
* **The compiler is deterministic.** The same source must emit byte-identical Jass and Lua, whatever was compiled before it and whatever the platform, thread or file order. Output must never depend on filesystem listing order (`File.listFiles()`), the iteration order of a `HashMap`, `HashSet` or `IdentityHashMap`, full paths or path separators, or a counter that survives between compilations (generated names, type ids: a script can read them). Use `LinkedHash*` collections or sort by a stable key (package, name, source position). `DeterministicChecks` compiles twice and shuffles compilation-unit order; add a case there for each new source of variation. Lua slot binding has its own rules in §8.
* Report problems with explicit, descriptive diagnostics; add no silent fallback and swallow no exception. Do not change the text of an existing error message unless required: about 300 tests assert them (`testAssertErrorsLines`).

---

## 4. How to Run Tests and Code Generation

The Gradle wrapper lives inside `de.peeeq.wurstscript/` (not the repo root); run all commands from there.

```
./gradlew test                                                              # all tests
./gradlew test --tests "tests.wurstscript.tests.SomeTestClass.someMethod"   # one test
./gradlew :gen                                                              # regenerate AST (parseq) + ANTLR + CompileTimeInfo
./gradlew build                                                             # compile and package, then run ALL tests (check)
```

* **Run the test classes you touched, not the suite.** The full suite takes 7-13 minutes (`build.gradle`, `testForkCount`) and needs memory for every fork. CI (`.github/workflows/build.yml`) runs it on pull requests and on pushes to master only, so a branch push alone tests nothing.
* To repeat a test use `--rerun --no-build-cache`: a cached result shows identical timings. The real stack trace is in `build/test-results/test/TEST-<class>.xml` when the console summary lacks it. Do not pipe gradle into `tail`: the pipe hides its exit code; read the XML.
* The harness writes an IM dump (`test-output/<name>.jim`) only for a test which asks with `test().imDump()`; a test which reads one must ask. `./gradlew test -PimDumps` writes them for every test, to read a failing test's IM.
* The test JVMs run ParallelGC (`-PtestGc='-XX:+UseG1GC'` or another flag overrides it): G1 spent 9-11% of a serial run in pauses, mostly mixed collections of data which lives for one test, and ParallelGC about 3%.
* Tests fetch the pinned stdlib first (`ensureStdLib`, about a minute on a fresh checkout). `InitialBuildProgressTest` and `LanguageWorkerTest` are in package `de.peeeq.wurstio.languageserver`, so a `--tests` filter needs that prefix.

### Runtime-executing tests

* `test().executeProg()` runs the compiled program in the interpreter and requires a `testSuccess()` call.
* `test().testLua(true).executeProg()` runs the **Lua path only** (`testLua` sets `luaOnly`, which skips the Jass and interpreter runs; add `.luaOnly(false)` to keep them). It syntax-checks the emitted Lua with luac and executes it with a stock Lua interpreter (5.3 on Windows and the Linux CI, see below) against the WC3 runtime in `src/test/resources/luaruntime/` (wc3shim + Reforged `common.j.lua`/`blizzard.j.lua` dumps).
* Interpreter discovery (`WurstScriptTest.getLuaExecutable`): on Linux `lua5.3` from PATH, then the bundled `lua53`; on Windows the bundled `lua.exe`; then `lua53`/`lua` from PATH. The version is never checked: Linux CI asserts 5.3 and the bundled Windows `lua.exe` is 5.3, but CI's macOS job installs whatever `brew install lua` gives. A missing interpreter skips the execution (visibly); a missing `luac` fails the test.
* Use `LuaBackendAuditTests` as the reference style for backend regression repros.
* The test Lua is not the game's Lua (libraries, real arithmetic): see §11.

---

## 5. LSP Structure and Build Pipelines

This repository has multiple entry points that may trigger compilation/build behavior:

* **Language Server runtime**
  `de.peeeq.wurstio.languageserver.*`
* **LSP build request**
  `de.peeeq.wurstio.languageserver.requests.BuildMap`
* **CLI compiler entry point**
  `de.peeeq.wurstio.Main`
* **CLI map build request**
  `de.peeeq.wurstio.languageserver.requests.CliBuildMap`

### LSP architecture (high-level)

* `WurstLanguageServer` wires LSP protocol handlers.
* `LanguageWorker` serializes requests and file-change reconciliation.
* `ModelManagerImpl` owns project model state (wurst files, dependencies, diagnostics).
* Add, replace, remove or purge compilation units of the managed model only through `ModelManager` (`retainCompilationUnits` for purges), never by mutating `getModel()`: lookups from other threads rely on its `modelLock`. A copy from `ModelManager.copy` can be changed freely.
* A load parses the files of the project and of its libraries ahead, by several threads (`-Dwurst.parseThreads=n`, 1 parses nothing ahead), and takes each parse when it comes to the file. Only the parse runs on those threads, because it depends on nothing but the text of one file. The model is changed by the loading thread alone, file by file in the order of the sequential load, and the imports resolve depth first as before: that order is the order of the compilation units, and so of the output. A step which adds to the model or reads it from the parse threads is not safe (the generated attributes, `GlobalCaches` and `ErrorHandler` are not thread safe, see `BACKLOG.md`). `ParallelLoadTests` compares a load with one thread and with four.
* User actions like build/start/tests are implemented in `languageserver.requests.*`.

### Initial workspace readiness

* Start the worker only after `initialized`, after negotiating progress in `initialize`. When clients support `window.workDoneProgress`, create a server-initiated progress token, then emit begin/end around worker initialization and the initial full build. Rejected or timed-out progress creation must still start the build; shutdown must cancel pending startup.
* Client and server opt into `experimental.wurstInitialBuildStatus: true`. Send `wurst/initialBuildStatus` with `{ state: "loading" | "ready" | "failed" }` only to clients that opted in. Standard work-done progress has no success/failure field, so clients must not parse its prose for readiness.
* Completion means `ModelManager.buildProject()` returned, including projects with ordinary source diagnostics. Exceptions report failed. Legacy clients retain their queued `workspace/symbol` readiness barrier. Keep that request ordering intact.
* Model-dependent requests, including formatting, must use `LanguageWorker.handle` so progress negotiation, initialization, and the initial build complete before model access. Formatting keeps duplicate requests because separate documents must each receive an answer.
* Run `InitialBuildProgressTest` and `LanguageWorkerTest` for startup changes; the extension's opt-in `scripts/test-lsp-readiness.js <compiler.jar>` verifies the actual stdio protocol.

### Language server startup archive

wurst4vscode starts the server with `-XX:+AutoCreateSharedArchive`: the JVM writes an AppCDS archive next to the
compiler jar when the first session ends, and the sessions after it start from it. That archive sits on the
runtime's own base archive, which `deploy.gradle` makes (`jlink --generate-cds-archive`; macOS copies a full JDK, which
usually has one, and `assembleSlimCompilerDist` dumps one with `java -Xshare:dump` when the JDK has none: the Temurin
for macOS x64 on CI does not). `assembleSlimCompilerDist` then starts the runtime with `-Xshare:on`, which fails when
there is no usable archive. Without one the JVM silently runs without any, which is how the earlier attempt went
unnoticed. Pull-request CI packages nothing (only pushes to master do, on every host), so a change here is not tested
on macOS before it merges. Java 27 enables compact object headers by default. The build clears the JVM option
environment variables when making and validating the base archive so it matches the shipped runtime defaults.
The extension passes `-Xlog:disable` because the JVM reports archive trouble on stdout, which is the protocol stream:
never print anything of your own there.

### Build-map pipeline (centralized)

Map build behavior is centralized in:

* `MapRequest.executeBuildMapPipeline(...)`

Both:

* `BuildMap` (VSCode/LSP build command), and
* `CliBuildMap` (CLI `-build`, used by grill)

must use that shared backend flow.

This pipeline handles:

1. map/cached-map preparation
2. script extraction/config application
3. compilation (Jass/Lua)
4. script + map data injection (including imports/w3i)
5. final output map write + MPQ compression finalization

### Lock handling policy

* `BuildMap` (LSP/UI) may use interactive retry/rename behavior for locked output files.
* `CliBuildMap` must fail fast with a clear error for locked files (non-interactive environments).

### Agent guardrails for future changes

* Do **not** add separate build-map logic to `Main` or other call sites.
* If map build behavior changes, update the shared `MapRequest` pipeline first, then keep wrappers thin.
* Ensure CLI and LSP builds remain behaviorally aligned unless a difference is explicitly required and tested.

---

## 6. Shared Project Config, Patch Targets, and Run Behavior

The `wurst.build` parsing rules live in a tiny shared dependency. Keep compiler behavior aligned with that shared model.

### Shared project config dependency

* `de.peeeq.wurstscript/build.gradle` depends on `com.github.wurstscript:wurst-project-config`.
* `de.peeeq.wurstio.languageserver.WurstBuildConfig` is a compiler adapter around the shared model, not a second config DAO.
* Do not duplicate YAML parsing rules, patch aliases, or script-mode behavior in compiler-only code unless it is truly compiler-specific.
* Preserve exact `wc3Patch` names for cache invalidation and diagnostics. Broad patch kind is useful for behavior choices, but not enough for hashes.

### Patch target rules

* Use the shared `Wc3PatchTarget` parser for `wc3Patch`.
* Patch family boundaries:
  * below `1.29` => pre-1.29 behavior
  * `1.29` through `1.31` => classic
  * `1.32+`, `1.36`, `2.0`, and `Reforged-*` => Reforged
* Friendly names and jass-history dump names should resolve through shared config. Do not add one-off aliases in compiler code.
* If jass-history has a broken folder name, fix `wurstscript/jass-history` instead of compensating here.

### Build vs run

* Build/typecheck should prefer pinned `wc3Patch` from `wurst.build` and should not parse the installed Warcraft executable just to decide target patch data.
* Config injection should use the pinned project patch when available, not the locally installed game patch.
* User-facing executable version parsing failures must stay short. Do not print PE parser stack traces unless explicit debug logging is requested.
* Run/launch is different from build: the selected Warcraft executable controls launch arguments and map placement.
* When project patch family and selected client family differ, warn and allow the user to choose a different Warcraft III folder.
* If launch folder selection changes the client, all launch decisions must use that selected `W3InstallationData`, not stale request-level `w3data`.
* Legacy clients that need install-dir map placement must copy to the selected launch install's `Maps/Test` folder.

### Focused tests

For config and run-pipeline changes, prefer these focused checks before broader test runs:

```
./gradlew test --tests tests.wurstscript.tests.WurstBuildConfigTests
./gradlew test --tests tests.wurstscript.tests.MapRequestPatchTargetTests
./gradlew make_for_userdir
```

---

## 7. Backend Parity and Lua Guardrails

Rules for backend work:

### Compiler phase ownership and lowering invariants

* Fix malformed or underspecified IR in the phase which creates it. Do not add downstream recovery,
  name parsing, or backend-specific guessing for information an earlier phase discarded.
* Semantic identity and specialization keys must use referenced AST/IM nodes plus structural type
  arguments, never generated names or string comparison.
* Each lowering phase has one explicit input/output contract. After an abstraction is lowered,
  downstream phases consume the lowered representation and must not reconstruct its source meaning.
* Prefer backend-appropriate, state-of-the-art lowering when semantics permit it. Jass limitations may
  require compatibility compromises; do not carry those compromises into Lua without evidence.
* Wurst lets users write high-level, readable, maintainable code and relies on the compiler to turn it
  into high-performance Jass or Lua, so an abstraction must not cost at run time. Behavioral correctness
  is the primary requirement. Runtime and allocation performance are the next requirement: common
  optimized paths must not retain avoidable compiler-introduced allocation, dispatch, copying, or
  bookkeeping overhead. Overhead the compiler adds is a defect, not a trade-off.
* **The IM is flat** (no `ImStatementExpr` outside a compile-time expression) from the flatten before the local
  optimisations until the backend, which needs it: Jass has no statement expression and Lua would make a closure of
  each. A pass which makes statement expressions flattens at its end (`prog.flatten`); a pass which only removes code,
  or replaces a statement by statements (the garbage removal), keeps the program flat and is not followed by a flatten
  or a second run. Statements and expressions in statement position are made by the flatten's own code
  (`Result.intoStatements`), not by hand: an operator expression such as `a and f()` is not a statement the backends
  translate. A unit test checks flatness at these places (`ImTranslator.assertFlat`, `ImOptimizer.assertNoGarbage`).
* **Do not tell a cache which function changed; ask the function.** `ImFunction.modificationCount()` (the generator
  counts the changes of a function and of everything below it, through every setter and list) is the same exactly
  while nothing in the function was modified. A pass which is only needed for the functions which changed, or a
  result which is kept for a function (the flatten does this), compares the count it saw. Prefer it to a
  `functionChanged(f)` call which a pass has to remember (the facts of the garbage removal still use one, see
  `BACKLOG.md`).

### Lua performance policy

* **Wurst-emitted constructs are consumed by Wurst code.** Never add runtime coercion, nil guards,
  normalisation wrappers or other defensive code to emitted Lua whose justification is that foreign
  (non-Wurst) Lua might have mutated an emitted table, array or value. A user who bundles raw Lua that
  writes into Wurst-emitted structures owns the result. Typed arrays already carry a metatable that
  supplies the typed default; a read of a typed array is a raw table index and nothing else.
* **Leverage Lua-native mechanisms wherever semantics permit.** Prefer a metatable default over a
  read-site helper, an operator over a helper call, a fixed-arity function over a `...` pack, and a
  direct table over an emulated hashtable. Emulating Jass limitations on Lua needs evidence that the
  limitation actually applies there.
* **A compiler-introduced call or allocation on an ordinary typed code path is a defect.** The
  optimiser must be able to inline small pure helpers; an analysis barrier that refuses to inline a
  function must be justified by what that function does, not by where else it happens to be called.

### Jass/Lua feature parity

* New language/compiler features must be validated for **both Jass and Lua** backends.
* Behavior should be as close as possible across backends. Lua is deliberately shimmed to behave like Jass so one Wurst program means the same on both targets: native calls with a handle parameter are wrapped in a nil guard because Jass returns defaults on null handles (`LuaNativeLowering`), div/mod follow Blizzard.j (`__wurst_intDiv` and friends), and typed arrays carry their default in a metatable. Shim the behaviour a program can observe, not Jass's limitations or implementation, and only where the targets really differ; make the shim cost nothing on the ordinary path (inlinable helper, metatable, operator). Where a shim cannot be made free, performance wins: real `==` is the deliberate exception, because shimming Jass's 0.001 tolerance would put extra work on every real comparison (see "Reference semantics for arithmetic").
* If behavior differs, treat it as intentional only when:
  * the reason is backend/runtime-specific, and
  * the difference is documented in tests.

### Reference semantics for arithmetic

* Integer/real division and modulo semantics are centralized: `WurstOperator.moduloInteger/moduloReal` implement the Blizzard.j formula (truncated remainder, plus divisor if negative) and Jass `div` truncates toward zero.
* The Lua helpers (`__wurst_intDiv`, `__wurst_modInt`, `__wurst_modReal`, built in `LuaNativeLowering`), the interpreter's `MathProvider` mocks, and constant folding (`SimpleRewrites`) must all stay consistent with those helpers — never reimplement div/mod locally.
* Jass `==` on reals allows a difference of 0.001 (`WurstOperator.jassRealEquals`), while `!=` and the orderings are exact, so on the Jass target `not (a == b)` is not `a != b` for reals. Lua compares exactly, by design: the tolerance would cost every real comparison. Both interpreters use that helper; the optimiser must not swap one for the other or fold nearly equal reals on Jass.
* The game reads Jass real literals inexactly and computes Lua reals with a 24-bit mantissa (measurements: [`docs/WC3_RUNTIME.md`](docs/WC3_RUNTIME.md) "Numbers"). So on both targets real folding (`SimpleRewrites.foldRealExactly`) reads and writes only exact literals and folds an operation only when its exact result is one. Never fold in double or float, and never treat one `R2I` result as proof of a precision. The test runtime's Lua computes in double, so a test that runs the emitted Lua does not show the game's rounding.

### Interpreter native mocks

* The mocks in `wurstio/jassinterpreter/providers` model what the game does. Where the behaviour is not obvious, measure it in game before mocking it, and say in the mock that it was measured.
* Death is a state, entered at life 0.405 or below, never at zero: setting a living unit's life to 0.3 kills it and its life then reads 0, and a dead unit whose life is set again stays dead. Change life through `UnitProvider.setLife`, test it through `isAlive`, and cover a fractional life in the test.
* common.j and blizzard.j constants carry their `ConvertX` handles in every interpreter run, the translated Jass program included (`JassInterpreter` evaluates their initialisers on first read). The engine reads a null enum argument as id 0 (a null unit state is `UNIT_STATE_LIFE`); a mock does the same only where that was measured.

### Error behavior parity expectations

* Prefer matching Jass behavior semantically in Lua output.
* Be explicit that Lua is stricter in some runtime cases where Jass may silently default/swallow invalid operations.
* Do not rely on Lua strictness as a substitute for correct lowering/translation.

### Lua inliner safety: callback/function-reference boundaries

* On Lua target, do **not** inline across callback/function-reference-heavy sites (IM `ImFuncRef`-containing callees).
* This avoids breaking callback context semantics (e.g. wrapper/xpcall/callback-native interactions such as force/group enum callbacks).
* This is a structural rule, not a name-based exclusion.

### Lua locals limit fallback (200 or more locals)

* Lua has a hard local-variable limit per function.
* A function with `LuaTranslator.LUA_LOCALS_LIMIT` (200) or more locals, counting parameters and four registers per numeric `for`, has its locals rewritten to a locals-table fallback. The inliner stays under 190 (`ImInliner.LUA_INLINE_REGISTER_BUDGET`).
* Requirements for fallback correctness:
  * locals-table declaration must be at function top before first use,
  * rewritten accesses must target the declared table (no global fallback),
  * nested block local initializations must be preserved,
  * use deterministic **numeric slot indices** (`tbl[1]`, `tbl[2]`, ...) rather than string keys.

### Regression testing requirements

* Any backend parity fix must add/adjust regression tests in `tests.wurstscript.tests.*`.
* Include tests that check:
  * generated backend output shape for the affected backend,
  * no behavioral regression in the other backend when relevant,
  * known fragile cases (dispatch binding, inlining boundaries, locals spilling).

---

## 8. Virtual Slot Binding and Determinism (New Generics + Lua)

Virtual-slot binding can silently degrade to base/no-op implementations in generated Lua while still compiling. Rules for related changes:

### Root-slot correctness is mandatory

* For FSM-style dispatch (`currentState.<rootSlot>(...)`), each concrete subclass must bind that **same root slot** to its own most-specific implementation.
* Never accept mappings where a subclass has its own update method but the dispatched root slot still points to `NoOpState_*` (or another base implementation).
* When verifying generated Lua, always inspect both:
  * the slot invoked at call-site (`FSM_*update`), and
  * class table assignments for each sibling state class.

### Override-chain integrity (wrapper/bridge cases)

* If override wrappers/bridges are created, preserve transitive override links (`wrapper -> real override`) so deeper subclasses remain reachable during slot/name normalization.
* Avoid transformations that disconnect root methods from concrete overrides in the method union graph.

### Deterministic Lua emission requirements

* Lua output must be deterministic (§3); the rules below are what that means for slot binding.
* Any iteration over methods/supertypes/union groups used for naming or table assignment must be deterministic (stable ordering).
* If multiple candidate methods exist for the same slot in a class, selection must be deterministic and must prefer the most specific non-abstract implementation for that class.

### Required regression tests for slot fixes

* Add a repro with:
  * `State<T:>`, `NoOpState<T:>`, `FSM<T:>`,
  * multiple sibling `NoOpState<Owner>` subclasses (including at least 4+ siblings),
  * early constant state instantiation,
  * root-slot call through `State<T>`.
* In generated Lua assertions:
  * extract the actual dispatched slot name from `FSM_*update` call-site,
  * assert each concrete sibling class binds that slot to its own implementation,
  * assert no sibling binds that dispatched slot to `NoOpState_*`.
* Add a compile-twice determinism assertion for the same repro input.

---

## 9. Compiler-Assisted Field Iteration and Generic Construction

New generic bounds, the native `handle` bound, and the representation rules for compiler-owned
KeyedMap intrinsics are documented in
[`de.peeeq.wurstscript/src/main/resources/agent-docs/WURST_LANGUAGE.md`](de.peeeq.wurstscript/src/main/resources/agent-docs/WURST_LANGUAGE.md)
under “Type class bounds,” “New generics and native representations,” and “KeyedMap intrinsic
representation.” Update that reference when changing these compiler semantics.

The compiler surface used by serialization libraries is intentionally general-purpose and contains no knowledge
of save formats, `ChunkedString`, hashes, or `Serializable`.

### Source-level contract

* The public Wurst names are `wurstForFields`, `wurstMapFields`, and `wurstNewInstance<T>()`; the `wurst` prefix
  makes the compiler-provided surface collision-resistant without underscore-prefixed names. The original
  unprefixed spellings remain supported as compatibility fallbacks. Internal markers must never survive backend lowering.
* Names beginning with the compiler-internal `__wurst` prefix are reserved. Generated temporaries must be fresh
  against user-visible enclosing declarations, but nested callback locals deliberately using that prefix are not
  supported.
* An applicable visible ordinary function with one of these names must resolve normally. Compiler handling is only
  the fallback when no user-visible overload accepts the call.
* `wurstForFields` includes accessible, non-static instance fields, including inherited, module-injected, readonly, and
  constant fields. `wurstMapFields` additionally requires each included field to be mutable.
* Explicit targets are evaluated exactly once. Generated temporaries must be proven fresh in the enclosing scope.
* Preserve module qualification in both field keys and generated accesses so sibling modules with equal field names
  remain distinct.
* `wurstNewInstance<T>()` must invoke the normal accessible zero-argument constructor of a concrete, non-abstract class.
  Never replace it with uninitialized allocation or runtime type lookup.

### Lowering and backend rules

* Field iteration expands after module expansion, when inherited and injected fields are concrete.
* Jass may use normal generic elimination. Lua specialization remains targeted to paths that reach generic
  construction; do not turn this into general Lua generic monomorphization.
* Lua reachability must traverse both `ImFunctionCall` and `ImMethodCall`, including dispatch submethods.
* Targeted specialized methods needed by Lua dispatch must remain attached to the IM classes consumed by
  `LuaDispatchPreparation`. Concrete implementations for erased generic objects must bind the same root slot used
  by the call site.
* Do not promise Lua support for a method which combines type parameters from its owning generic class with
  independent method type parameters. Serialization loaders should be free generic functions, or class methods
  parameterized only by their owning class.
* A method invoked directly on a freshly constructed generic receiver is supported on Lua. The receiver's
  declared type is still generic at that point, so specialization takes the instantiation from the construction
  (`FieldIterationTests.genericConstructionOnFreshlyConstructedReceiver`).
* Lua generic-construction dispatch through multi-parameter generic interfaces is outside the supported loader
  shape. The supported generic loader has a single construction type parameter.
* Do not call `wurstNewInstance<T>()` from the constructor of a generic class. Construct the simple state object in the
  generic loader, then initialize any nested state explicitly after construction.
* `wurstNewInstance<T>()` is a runtime Jass/Lua construction surface, not a supported use inside `compiletime(...)`
  evaluation: the interpreter happens to evaluate the marker (`EvaluateExpr.evaluateGenericNew`), but no test covers it.
  Do not rely on it or extend it.
* Nested modules whose sibling submodules declare equal field names are outside the supported field-key model.
  Dedicated state classes should use direct fields, ordinary inheritance, or non-conflicting shallow module fields.
* Generate no runtime reflection registry, type-name lookup, type-id switch, or serialization-specific metadata.

### Required regression coverage

Use `FieldIterationTests` as the focused suite. Cover both Jass and Lua, direct and explicit targets, target
evaluation count, inherited/module fields, readonly versus mutable behavior, overload selection, constructor
execution and diagnostics, ordinary same-name functions, generic functions and class methods, transitive method
reachability, and generic interface dispatch. Assert generated output contains direct construction/accesses and no
source intrinsic names or runtime reflection machinery.

---

## 10. New-Generic Handle-Keyed Maps

The representation rules (the handle itself is the Lua table key, with no `GetHandleId` or index map; generic values are non-null; destroy clears in place) are in [`WURST_LANGUAGE.md`](de.peeeq.wurstscript/src/main/resources/agent-docs/WURST_LANGUAGE.md) under "KeyedMap intrinsic representation". `LuaKeyedMapTests` is the focused suite; extend it when changing this lowering. The compiler must also hold to:

* Keep the fixed `keyedMapPut(int, handle, int)` signature intact: a generic overload with the same name becomes ambiguous with legacy calls after specialization. The typed operations have distinct compiler-intrinsic names, `keyedMapPutNative<K: handle, V:>` and `keyedMapGetNative<K: handle, V:>`. Lua maps them to `t[k] = v` and a single `t[k]` read with a primitive default; the specialized wrapper adds no conversion around them.
* Jass source cannot cast a new generic `V:` to `int`. After generic elimination and class elimination both `int` and class references have Jass integer representation, so only then rewrite the typed aliases to the fixed integer intrinsics. Reject other specialized value types with a diagnostic; add no generic boxing or permissive cast.
* Handle-bounded keys are nullable: guard a null key before a Lua table write, and never infer a handle-bounded type argument from a bare `null` (`f<unit>(null)` is fine). Jass specializes generic null to the type default, which cannot be told from storing that default, so add no post-specialization null check for generic values; use `remove` for absence.
* `keyedMapDestroy` clears the backing Lua table in place, because Lua `destroy` leaves field storage alone ([`docs/WC3_RUNTIME.md`](docs/WC3_RUNTIME.md) "Objects and memory"); dropping the clear would keep every entry reachable through aliases. A Lua runtime test must destroy a map while retaining an alias and find it empty (`keyedMapDestroyClearsLuaStoreWhileAliasRemains`).

## 11. Warcraft III Runtime Traps

The game is the specification; the IM interpreter, the Lua test runtime and Blizzard.j each differ from it. Facts, measurements and the evidence for each are in [`docs/WC3_RUNTIME.md`](docs/WC3_RUNTIME.md). The rules that follow:

* **Wurst has no garbage collector.** An instance is an integer id on both targets, `destroy` is the only release, and `ondestroy` is an observable side effect. Never elide, duplicate, reorder or "tidy" an alloc, dealloc or `ondestroy`, and never rely on Lua to collect a Wurst object. Both targets hand the most recently freed id out first, so a stale reference reads the next object, which may belong to a related class on Jass (one allocator per inheritance group) and to any class on Lua (one id space).
* **The game's Lua is not the test Lua.** Tests run a stock Lua with every library and double reals (5.3 on Windows and the Linux CI, unchecked elsewhere). Emit no `debug`, `collectgarbage`, `io`, `os`, `package` or `require`. Two tests guard it: `emittedLuaUsesOnlyLibrariesTheGameProvides` over one program's output, and `noLuaBackendLiteralNamesALibraryTheGameWithholds`, which scans the string literals under `translation/` because natives and fallback helpers are emitted only on demand and no program reaches them all. Only the test-only `testSuccess` is exempt. Never take a passing Lua test run as evidence about the game.
* **Nothing optimises the emitted script at run time** (the reference Lua 5.3 neither inlines nor caches lookups, and the game is reported to run it). The inliner and the local optimisations are the only optimiser, so a helper call, global read, table index or allocation left in the script is paid on every execution, and `-stacktraces` adds a push and a pop to each instrumented function (`wurst_stack`). Benchmark like a shipping map: `-inline -localOptimizations`, no `-stacktraces` (the `wurst_run.args` default is the opposite).
* **Lockstep: never iterate a Lua table whose order is observable** (`pairs`, `next`). The order differs between players and desyncs the game. A table is safe as a membership set; anything iterated needs an insertion-ordered array. Decide this when designing a structure, not afterwards.
* **`I2S(1 div 0)` is the deliberate thread abort** (Jass has no `throw`); Lua lowers that exact shape to `error("__wurst_abort_thread", 0)`. A rewrite of `div` or `I2S` must keep the shape (`LuaBackendAuditTests`, `OptimizerTests` cover it).
* **Death is life <= 0.405, never 0**, and reals are not doubles: see "Interpreter native mocks" and "Reference semantics for arithmetic" in §7.
* **Measure, do not infer.** Establish an engine behaviour by measuring in the real client with run-time operands that discriminate (nothing folded), and record it as "measured on the X.Y.Z client" with the values. One result is not proof of a mechanism. Do not launch Warcraft III yourself: it needs the maintainer's Battle.net sign-in, so ask for a probe. Describe a measurement in public text; do not cite private tooling.

## 12. Warcraft III Game Data Generation

For refreshing object-data knowledge and ability-editing sources from the installed game, use the in-house sibling `../casc-ts` reader and follow [`HelperScripts/GAMEDATA.md`](HelperScripts/GAMEDATA.md). Do not use CascView or online game-data mirrors. Keep the extraction snapshot in ignored `HelperScripts/gamedata/`; generated JSON resources and ability-editing Wurst sources are checked in or merged into WurstStdlib2 as described in that guide.
