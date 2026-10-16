# Unoptimized RunMap performance, 2026-10-14

## Scope and method

This investigation measures the VS Code/LSP RunMap compilation path, with `opt`, `inline`, and
`localOptimizations` disabled. It does not use release-build optimization timings to prioritize RunMap work.
Compiler baseline: `768dc92e5`, using compiled classes preserved before the receiver-index experiment.

Isolated project copies contain the local sources, raw assets, map folders, configuration and installed
dependencies of castle-fight (`912d533`, including existing local edits) and zombie-defense (`2efc916`, also
including local edits). Their optimization argument prefixes were removed in the copies only. Argument loading
uses `WurstCommands.getCompileArgs(root, false)`, exactly as RunMap does, and asserts that all three optimization
flags are off. Lua, object injection, compile-time functions and compile-time caching remain enabled. Castle-fight
also retains `-noExtractMapScript`. Original project files are untouched.

The harness initializes and checks a `ModelManagerImpl` before measurement, representing an editor which has
finished its initial build. It then executes three RunMap requests in that JVM. Each uses the same cancellation
scope as `MapRequest.run`, so it takes the actual private-model-copy path. The harness overrides the UI and the
launch-data boundary: real RunMap compilation and map-data injection finish, but Warcraft is never launched.
Existing cached maps are available; these are not first-ever map packing or LSP startup measurements.

Java 27, `-Xmx4g -XX:+UseParallelGC`, JFR ExecutionSample interval 2 ms and allocation sampling at 300/s.
TimeTaker phase durations exclude recording dump time. CPU samples are attribution evidence, not exact wall times.
Allocation weights are estimates; sparse allocation samples do not justify exact per-call byte counts.

## Measured phase costs

Times below are milliseconds. R0 is the first RunMap after editor initialization; R1/R2 are successive unchanged
runs. Initial project loading/checking and game launch are excluded. Every run reached map injection with zero
errors. Warnings remain reported normally.

| Project / run | Full typecheck | AST to IM | Compile-time pass | Required Lua lowering/emission | Total |
| --- | ---: | ---: | ---: | ---: | ---: |
| Castle-fight R0 | 3,539 | 2,166 | 1,048 | 5,018 | 13,043 |
| Castle-fight R1 | 3,094 | 1,262 | 491 | 3,101 | 8,391 |
| Castle-fight R2 | 2,679 | 958 | 267 | 2,769 | 7,146 |
| Zombie-defense R0 | 2,579 | 1,589 | 891 | 2,833 | 9,375 |
| Zombie-defense R1 | 2,980 | 940 | 543 | 1,960 | 6,979 |
| Zombie-defense R2 | 1,923 | 806 | 245 | 1,758 | 5,150 |

Required Lua work in R2 includes:

| Pass | Castle-fight | Zombie-defense |
| --- | ---: | ---: |
| Concrete-operation generic specialization | 705 | 307 |
| Eliminate local types, including tuple elimination/flattening | 628 | 455 |
| Translate IM to Lua | 624 | 388 |

These passes still run when optional optimizations are disabled. The Lua phase is an aggregate, comparable in
cost to the whole checker. It must not be confused with optional inlining/local optimization. There are additional
required passes under that aggregate; its child timings must not be added to the parent timing.

Castle-fight R2 model copying took 71 ms, map-cache preparation 7 ms and injection 56 ms. The expensive part of
copying is therefore not copying bytes: it is reconstructing semantic work afterwards. In this steady-state case,
map packaging, script printing and compile-time execution are much smaller targets than checking and lowering.

## What the checker actually spends time on

R2 CPU samples include 803 checker samples out of 2,247 application samples on castle-fight and 596 out of 1,592
on zombie-defense. Checker attribution:

| Checker work | Castle-fight samples | Zombie-defense samples |
| --- | ---: | ---: |
| Light validation | 469 (58%) | 333 (56%) |
| Heavy validation | 150 (19%) | 94 (16%) |
| Post-checks | 80 (10%) | 68 (11%) |
| Remaining checker setup | 104 (13%) | 101 (17%) |

Within light validation, function signature calculation, overload/argument inference and receiver/name lookup
are substantial, overlapping stacks. `AttrFunctionSignature.calculate` accounts for 112 of 469 light samples in
castle-fight and 64 of 333 in zombie-defense; it also runs during setup when global initializers have their types
resolved. Castle-fight member lookup has 61 light samples. This is evidence for broad semantic-query costs, not
for treating unrelated-class candidate rejection as the dominant cost of checking.

Heavy validation is primarily uninitialized-variable/return-flow analysis through `SccForwardExecution`,
`DataflowAnomalyAnalysis` and `VarStates`, including repeated immutable state/map/set merges. Post-checks include
unused imports and member/local usage scans. Setup includes `NamePreservation.indexGlobals`, which eagerly
resolves global types, pulling in name-link, export and signature work before light validation.

Allocation sampling estimates roughly 1.74 GB allocated under the checker on castle-fight R2 and 1.33 GB on
zombie-defense R2. Heavy validation contributes approximately 650 MB / 475 MB; light validation approximately
726 MB / 474 MB. This is reconstructed work on each compilation, not retained semantic state. GC pauses recorded
within these complete R2 runs total 537 ms and 309 ms. The TimeTaker `GC time` line is cumulative JVM GC time,
including editor initialization, and must not be mistaken for per-request GC time.

## Root cause established by a diagnostic control

The actual RunMap path does not reuse the editor's checked model:

1. `MapRequest.run` establishes `CompilationCancellation` scope.
2. `MapRequest.compileScript` copies the model when `CompilationCancellation.isScoped()` is true, even with
   `QuickAndDirty` safety. This protects the editor model from a canceled compilation's mutations.
3. `ModelManager.copy` copies the AST without its calculated attributes and clears module instantiations.
4. `MapRequest.checkModel` asks `modelManager.isFullyChecked(copy)` before using the checked-model fast path.
5. `ModelManagerImpl.isFullyChecked` explicitly requires `checkedModel == model`. The private copy can never
   satisfy that condition. A successful check of the copy also cannot mark the managed model as checked.
6. Consequently, every unchanged RunMap performs full validation and recreates name/signature/flow facts on a
   fresh AST. The measured checker scope covers the full retained compilation model, not just edited files.

To test that mechanism, an isolated diagnostic invokes the same RunMap request without the cancellation scope.
This exercises the existing non-copying path; it does not modify compiler code or introduce a fake checked flag.
After its first check, unchanged requests can adopt the checked model, and the typecheck phase reports 0 ms:

| Project / run | Normal RunMap total | Diagnostic non-copying total | Diagnostic typecheck |
| --- | ---: | ---: | ---: |
| Castle-fight R0 | 13,043 | 9,488 | 2,760 |
| Castle-fight R1 | 8,391 | 4,319 | 0 |
| Castle-fight R2 | 7,146 | 3,907 | 0 |
| Zombie-defense R0 | 9,375 | 7,354 | 2,061 |
| Zombie-defense R1 | 6,979 | 3,203 | 0 |
| Zombie-defense R2 | 5,150 | 2,695 | 0 |

This is a causal probe of repeated validation/cache reconstruction, not a shippable cancellation policy or a
statistically established performance estimate for a future fix. It uses separate JVM sessions from the normal
case. The R2 total difference is about 45% / 48%, but the directly established effect is that unchanged-run
typechecking can be avoided on the existing checked-model path. Some other phases also benefit from retained
attributes; JIT/GC variation contributes to total-time differences.

Final castle-fight Lua is byte-identical between normal and diagnostic runs (SHA-256
`78803139b466c6160953d45d330e8904a1be6e8365e3136f3fd1057457dc6ee9`). Zombie-defense's sole script difference is
the user project's compile-time `Questlog_BUILD_DATE` string, changing from `2026-10-14T10:24` to
`2026-10-14T10:30`; the remainder matches. This comparison covers emitted script, not full MPQ binary identity
or an in-game execution test.

## Implemented fix and verification

The fix keeps the managed AST and its checked state rather than teaching an unchecked copy to masquerade as
checked. Run, editor build and CLI build now have one model path; the `SafetyLevel` clean/rebuild/copy split is
removed. Existing version/invalidation tracking still determines when checking is necessary. Applying a changed
configured Jass script invalidates its proof normally, and legacy Jass checks retain their separate validation.

Shared-AST preparation completes as a unit under a non-canceling scope: map-script replacement/checking, then
import-root retention, required checking and AST-to-IM translation. Generated attributes leave their cache in the
"computing" state if their calculation throws, so cancellation must not interrupt this work on the managed AST.
Cancellation is checked before and after these phases. Subsequent compilation mutates private IM and remains
cancelable, including interpreter loops. Existing atomic final-map publication is unchanged. Cancellation during
shared preparation waits for that phase to finish; it does not trigger a clean/reparse on the next request.

The new regressions first failed on the baseline: scoped requests compiled an unchecked copy and canceling a
check left the managed model unvalidated. `MapModelReuseTests` now passes 14 cases covering run/build on both
backends, unchanged repeated emission, edits to old/new generic and module code, invalid edits and repair,
configured-script changes, cancellation during shared preparation, and cancellation during private compile-time
work followed by compilation again. Comparisons use a fresh parser/checker as the output oracle, and count that
the managed manager is never cleaned by compilation.

Extended `IncrementalModelOracleTests` passed 100 source-edit seeds and 100 configuration-edit seeds, each with
24 states compared against fresh builds. The final focused run passed 162 cases including default oracle seeds,
the new regressions, existing cancellation and CLI checks, patch/map-cache paths, modules, generic modules,
field iteration and deterministic emission. See `temp/model-reuse-oracle-100.log` and
`temp/model-reuse-final-tests.log`.

### Real RunMap after the fix

Baseline and fixed builds were then run sequentially through the same scoped RunMap harness, with optional
optimizations disabled and cancellation still enabled. The fixed case uses the new compiler, not the diagnostic
non-copying flag. Three requests per project/variant in separate Java 27 JVMs; identical project copies and JVM/JFR
settings to the investigation above. Times are TimeTaker totals in milliseconds:

| Project / run | Baseline total | Fixed total | Baseline typecheck | Fixed typecheck |
| --- | ---: | ---: | ---: | ---: |
| Castle-fight R0 | 9,926 | 9,320 | 2,768 | 2,746 |
| Castle-fight R1 | 6,833 | 4,197 | 2,699 | 0 |
| Castle-fight R2 | 6,669 | 3,915 | 2,496 | 0 |
| Zombie-defense R0 | 7,330 | 7,062 | 1,922 | 2,027 |
| Zombie-defense R1 | 5,161 | 3,127 | 1,841 | 0 |
| Zombie-defense R2 | 4,550 | 2,788 | 1,760 | 0 |

Warm unchanged RunMap is about 39-41% faster in both maps. The first configured-script swap still checks the
project, so its improvement is much smaller. These measurements concern repeated unchanged runs, not a promise
that every source edit saves the same amount of time. Both projects emitted identical Lua between baseline and
fix in this comparison, and all 12 requests completed with zero errors. Raw logs/recordings/scripts are retained
as `temp/reuse-{castle,zombie}-{baseline,fixed}*`. The earlier control's timestamp difference is not present here.

Inside genuinely necessary rechecks, overload/signature allocation and immutable flow-state merges remain
measured secondary targets. Required generic/tuple/Lua lowering remains the other major part of RunMap. Keeping
unchanged Jass declarations when map configuration changes a body is still open, as is the editor-build policy
for unsaved buffers; these are separate from the cancellation regression fixed here.

## Reproduction artifacts

### Larger-map follow-up

Measured an isolated copy of a larger Jass map project,
including the existing local `wurst.build` and `wurst_run.args`. The original repository was untouched.
The real arguments select Jass, compile-time functions and object injection, without optional optimisation,
inlining, local optimisation or compile-time caching. PJass remains enabled. The checked editor model has
1,700 compilation units. Editor initialisation is outside these request timings; the harness stops before
launch after completing compilation and map-data injection.

The first reuse implementation failed PJass on this project: references to two `gg_trg_*` globals survived,
but their declarations were omitted. Destructive pruning scanned name references between map-script
invalidation and checking, caching stale lookup results on the AST. It also removed editor-only units.
Compilation now selects an import-root view for AST→IM without changing the managed model. Compile-time
ordering, class/interface discovery and error-helper selection use that view; package ordering follows
the original package index. A focused repro fails with the old pruning path and covers preservation of
editor units and configured globals, alongside fresh-build comparison on both targets and commands.
Validation checks the selected units against the editor model's namespace and records only those units
as checked. Excluded units stay in the editor model and remain unchecked when the configured script
changes, so their reverse dependencies cannot block an otherwise valid compilation. Repeated unchanged
builds still skip validation of the selected units. Because name resolution can find definitions outside
the compilation view, AST→IM rejects
references to excluded function, variable or class definitions before creating their IM representation.
Native types retain their source declarations too; translating one verifies its definition and all native
supertypes, including default-value translation. Regressions cover globals, parameters, return types and
a selected subtype whose parent is excluded, on both targets and commands.
Whole-model validation prepasses also use the compilation view: array-length preparation, module expansion,
global name indexing and variable-event preservation do not inspect excluded code. Reusing checked facts
refreshes synthetic name-preservation markers for the view, so an earlier editor-wide check cannot retain
an otherwise dead map global solely because an excluded unit registers a variable event for it.
Preservation decisions are captured as source identities in private IM. Scoped compilation restores the
editor's original synthetic annotations immediately after AST→IM preparation, including on failure and
cancellation; private backend passes use the IM metadata. A regression translates the full model after
the map build and checks preservation through IM copying, so later consumers do not depend on build history.
Excluded Jass globals/functions have regression coverage on both targets and commands, including
successful recompilation after removing the reference. This prevents incomplete scripts with PJass
disabled and preserves the existing compilation source policy.

After correcting this, all three requests passed PJass and map injection. Final sequential measurements,
with identical source/map copies and JVM/JFR settings:

| Run | Baseline total | Reuse total | Baseline typecheck | Reuse typecheck |
| --- | ---: | ---: | ---: | ---: |
| R0 | 11,553 ms | 11,151 ms | 3,482 ms | 3,576 ms |
| R1 | 8,660 ms | 5,194 ms | 3,613 ms | 0 ms |
| R2 | 7,876 ms | 4,520 ms | 3,367 ms | 0 ms |

Warm unchanged requests improve by 40 to 43%. R0 still needs a complete check after the configured-script
swap. The 100,010-line emitted Jass matches baseline exactly except for the compiler-version header.
An earlier successful repetition measured 4,641 to 4,890 ms warm.

The remaining warm costs are:

| Stage | R1 | R2 | Finding |
| --- | ---: | ---: | --- |
| AST→IM | 1,308 ms | 1,403 ms | Rebuilds the entire selected program; class/closure and expression translation dominate. |
| Required Jass lowering | 1,652 ms | 1,331 ms | Tuple elimination/flattening, class lowering and cyclic-function removal remain required with optimisations disabled. |
| Compile-time stage | 1,038 ms | 883 ms | Reachability, interpreter execution and object-data serialisation rerun; no compile-time cache flag is enabled. |
| Map-data injection | 551 ms | 406 ms | Import handling and MPQ finalisation. |
| Applying map config | 284 ms | 240 ms | Rewrites configuration on every request. |

JFR corroborates the translation/lowering concentration. NameLinks/NameResolution are no longer a leading
warm-request cost once the checked model is reused. MPQ saving still allocates heavily. JFR allocation
weights are sampled estimates, and TimeTaker's GC total is cumulative across the JVM, not per-request GC.
The next substantial architectural trial is retaining a pristine IM snapshot and copying it for mutable
compilation passes, with explicit invalidation and fresh-build oracles. Separately, selective map-script
declaration invalidation would address the necessary first-run recheck; it does not reduce these warm costs.

The reusable harness is in `docs/profiling/RunMapProfile.java`, its JFR analyzer in
`docs/profiling/RunMapProfileReport.java`, and the Gradle init script in `docs/profiling/runmap-profile.gradle`.
Use an isolated project copy: the actual pipeline writes cached maps, scripts, object data and build outputs.
The harness stops before launch and asserts optional optimizations are disabled.

From `de.peeeq.wurstscript`, after installing dependencies into the isolated copy:

```powershell
.\gradlew.bat -I ../docs/profiling/runmap-profile.gradle runMapProfile `
  '-PprofileRoot=temp/map-profiles/castle' '-PprofileMap=CFDE_base_map-folder.w3x' `
  '-PprofileLabel=runmap-castle' '-PprofileCompilerClasses=temp/baseline-classes'

.\gradlew.bat -I ../docs/profiling/runmap-profile.gradle runMapProfileReport `
  '-PprofileLabel=runmap-castle-2'
```

Omit `profileCompilerClasses` to profile the current compiler classes. The diagnostic control adds
`-PdiagnosticNoCopy` and must use a distinct profile label. It intentionally bypasses the cancellation snapshot
only in this standalone diagnostic.

Raw artifacts are retained under the compiler's ignored `temp/`: `runmap-{castle,zombie}.log`,
`runmap-{castle,zombie}-{0,1,2}.jfr`, `runmap-{castle,zombie}-2-report.log`, and the equivalent
`runmap-{castle,zombie}-control*` logs/recordings. `runmap-{castle,zombie}-normal.lua` preserves the comparison
outputs. Earlier `map-*-cold*` recordings measure optimized CLI builds and are outside this RunMap analysis.
