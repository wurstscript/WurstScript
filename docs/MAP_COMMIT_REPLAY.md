# Real map commit replay

Measured 26 October 2026 on Windows x64, Java 27, ParallelGC, a 6 GiB profile JVM, compiler master `d6338e6e882eecd5f44ddabeb0c9cdb112bbfffc`. This is an offline, pairwise parent-to-child replay of the last 12 Wurst-changing first-parent commits in each map. Each expected child was built in an independent fresh model **before** warming its parent. Every measured child matched the fresh Lua and full manifest exactly.

The timings run from child reconciliation through payload-file publication. They exclude editor queueing, Warcraft pickup/application, parent startup and independent-oracle construction. They include fallback rebuilds and rejected payload comparisons. The map, raw assets and copied dependencies stay frozen; changes to non-source assets in a commit are listed by the corpus but are not replayed. This is source-payload compatibility evidence, not proof of gameplay equivalence or release readiness.

## Castle Fight

Source ref: `master` at `687ecd231dfd77aa34e44acd54af1eb2ab50be64`. Accepted **8/12** payloads; **3** had no state warnings. Commits touching sources outside obvious test conventions: **5/9** accepted. File-name conventions classify `tests/`, `e2e/`, `*Test.wurst`, `*Tests.wurst` and `*E2E.wurst`; these categories are a navigation aid, not reachability proof.

Median host publication/comparison: **7.01 s**, nearest-rank p95 **11.20 s**.

| Commit | Payload | Functions | Warnings | Host seconds |
|---|---|---:|---:|---:|
| [39d76132](https://github.com/Frotty/wurst-castle-fight/commit/39d76132cf37ad6d6e0155a2153c21e14188af4a) | restart | — | — | 11.20 |
| [aac323bc](https://github.com/Frotty/wurst-castle-fight/commit/aac323bcc3e9d454976aca20153405aa72389535) | accepted | 1 | 0 | 7.27 |
| [da656f81](https://github.com/Frotty/wurst-castle-fight/commit/da656f819f08e363c96960d983dd52c006bbaa33) | accepted | 75 | 2 | 6.39 |
| [fa8613ae](https://github.com/Frotty/wurst-castle-fight/commit/fa8613aeb515a7b1da5021734f9895ffae5f5528) | accepted | 123 | 3 | 9.07 |
| [dc182ff1](https://github.com/Frotty/wurst-castle-fight/commit/dc182ff158bd3d1fdac20ad2ceb50e54f4e92e03) | accepted | 200 | 3 | 6.96 |
| [908c77ee](https://github.com/Frotty/wurst-castle-fight/commit/908c77ee032c8f41d663998d9d27c4607b272de5) | accepted | 62 | 1 | 7.06 |
| [fc2ccf5f](https://github.com/Frotty/wurst-castle-fight/commit/fc2ccf5fe7499c298b6579b2aa97a3e00cc61c40) | restart | — | — | 6.91 |
| [190e3ff8](https://github.com/Frotty/wurst-castle-fight/commit/190e3ff8b7d1445e5d9352409d005ff0727865e5) | restart | — | — | 7.06 |
| [91882f2b](https://github.com/Frotty/wurst-castle-fight/commit/91882f2b88173544d961c0179260bdee414cb8ba) | accepted | 6 | 0 | 6.50 |
| [15d728e0](https://github.com/Frotty/wurst-castle-fight/commit/15d728e063297b976f6416a877efd8adac7ca571) | restart | — | — | 7.17 |
| [261635f5](https://github.com/Frotty/wurst-castle-fight/commit/261635f54966008ad0c448c8c266ab864d8bf0fd) | accepted | 46 | 1 | 6.89 |
| [687ecd23](https://github.com/Frotty/wurst-castle-fight/commit/687ecd231dfd77aa34e44acd54af1eb2ab50be64) | accepted | 8 | 0 | 6.44 |

Accepted payloads may keep old initialized values, already registered callbacks and old closure instances. Warnings remain part of the result; a zero-function payload is not evidence that a gameplay edit took effect.

Castle Fight enables `-luaEncapsulation`, which is implemented in its private compiler fork. Its Jenkinsfile explicitly strips that flag for public-compiler builds. This replay uses that **labeled public-CI-equivalent lane**; it does not claim support for the private flag. Four commits require restart: one reuses captured storage across closure lineages; three ambiguously add, change or reorder anonymous closures.

Frozen environment receipts (SHA-256 of ordered relative file paths and bytes; `.git` excluded):

| Input | SHA-256 |
|---|---|
| base.w3x | `44c33249fb126f808f92af9c85abf8e65683998c73399096b193ab46cdcca04c` |
| raw | `4b1b34d1fc27d7f0fc8f097003a4d64c1bdb5ff6e47e434ec1fbc117c2d6b3ae` |
| dependency wc3-e2e-harness | `7375fc07c36a39eb043f584c7e0d1b3533fb4614c4af21140d47f9b3270cc03c` |
| dependency wurst-fsm | `e7c4b3cc30ed569810948db18348a3b29723c24f687d1b826df22fdbb1ec310f` |
| dependency wurst-table-layout | `2afdb8c6f6467c181e8452810d0e534020caddb7baf4ad0e8a9b6bd0e4dad13e` |
| dependency WurstCommand | `178592969530bcceb708f3f35bb8f226c3ee8daf41bf803741147920523cfecc` |
| dependency WurstMMD | `7eee3615f27e92cb852da251928637eee2e7b5412ec40b18e9c93c8ba3e7f38c` |
| dependency wurstStdlib2 | `06eba296fe7bd548a3fa499a45e28629468bd2b0868debe91ba010357c642200` |

Measurement inputs were exported into isolated fixtures under the compiler’s ignored `temp/` directory. The exporter records parent/child SHAs, paths, source categories, environment hashes and explicit dependency overrides. Full local `results.json` receipts retain timings, owner/node work counts, phase reports, exact warnings/rejections and oracle outcomes.

## Zombie Defense

Source ref: `main` at `ad0e1187b9fedc50b2c1afd443533823736ac104`. Accepted **12/12** payloads; **2** had no state warnings. Commits touching sources outside obvious test conventions: **12/12** accepted. File-name conventions classify `tests/`, `e2e/`, `*Test.wurst`, `*Tests.wurst` and `*E2E.wurst`; these categories are a navigation aid, not reachability proof.

Median host publication/comparison: **4.98 s**, nearest-rank p95 **5.54 s**.

| Commit | Payload | Functions | Warnings | Host seconds |
|---|---|---:|---:|---:|
| [45e7a826](https://github.com/Frotty/zombie-defense/commit/45e7a82687f8a6c5bf2c3d0d3eb5e4f24489137e) | accepted | 3 | 0 | 5.36 |
| [c1640215](https://github.com/Frotty/zombie-defense/commit/c16402151374ffd1b00784e7a179fdf34779b4ea) | accepted | 37 | 1 | 5.11 |
| [a9e052bd](https://github.com/Frotty/zombie-defense/commit/a9e052bd7631477a1422c431ac971f4a082bdb77) | accepted | 90 | 2 | 4.80 |
| [5773a1de](https://github.com/Frotty/zombie-defense/commit/5773a1de823604db19d62273516d28841417bb13) | accepted | 0 | 0 | 5.30 |
| [e3a817f3](https://github.com/Frotty/zombie-defense/commit/e3a817f33c9a18c9255214e982d4742e07a1e715) | accepted | 0 | 1 | 4.08 |
| [75fbbda3](https://github.com/Frotty/zombie-defense/commit/75fbbda300da540c8e667c343e6eec9198207610) | accepted | 74 | 2 | 5.02 |
| [613d8788](https://github.com/Frotty/zombie-defense/commit/613d8788745ebc33a060a772f1895a85d34e6d46) | accepted | 51 | 2 | 4.66 |
| [77a63e87](https://github.com/Frotty/zombie-defense/commit/77a63e8717be7751e6cba4870369cfb8c42baa27) | accepted | 123 | 14 | 5.10 |
| [1f85f086](https://github.com/Frotty/zombie-defense/commit/1f85f0864aabe0942cee33bc0c1c047f11654f20) | accepted | 44 | 1 | 4.62 |
| [36f8e7fe](https://github.com/Frotty/zombie-defense/commit/36f8e7fe926680d8aae18327838acccefbfb0cd9) | accepted | 178 | 14 | 5.54 |
| [6a22737b](https://github.com/Frotty/zombie-defense/commit/6a22737b4a9a10b27957e23a95bff4e3a96380a5) | accepted | 54 | 5 | 4.93 |
| [ad0e1187](https://github.com/Frotty/zombie-defense/commit/ad0e1187b9fedc50b2c1afd443533823736ac104) | accepted | 8 | 1 | 4.74 |

Accepted payloads may keep old initialized values, already registered callbacks and old closure instances. Warnings remain part of the result; a zero-function payload is not evidence that a gameplay edit took effect.

The installed Frentity copy (`777d559e1a67e0ddde390a2aa295f2cca7f5d5f8`) cannot compile the newest map commit: `sampleHeightmapTerrainZ` is missing. The complete campaign therefore explicitly overrides only that fixture dependency with Frentity `5bddf50a41a9140e5c36937c60172e54e3525560` (#19). Original map checkouts and installed dependencies were not changed.

Frozen environment receipts (SHA-256 of ordered relative file paths and bytes; `.git` excluded):

| Input | SHA-256 |
|---|---|
| base.w3x | `9c50729f5569233e8f1610af0daa3160281a3cd75df8d0f211a0d73c2475e000` |
| raw | `a9c3dd4a6d57c395d61759aa99a08d578aa6a3108f52d9560608b1e01d797b55` |
| dependency Ability-Tooltip-Generator | `56b9bd1a1370a7e9eae8b48d94e539d875c4929c3d946e92d7f8725bbe045600` |
| dependency Frentity | `e5e4d0d1a674fde0798300cc3002eb83868b5af8d7cccc2bbebdfd39e00d76ed` |
| dependency wc3-e2e-harness | `7375fc07c36a39eb043f584c7e0d1b3533fb4614c4af21140d47f9b3270cc03c` |
| dependency wurst-bonus-handler | `5794f0981857cd82d1ab63fc113f8813fcd63626b8558270b4aaca5132692d4d` |
| dependency wurst-table-layout | `2afdb8c6f6467c181e8452810d0e534020caddb7baf4ad0e8a9b6bd0e4dad13e` |
| dependency WurstCommand | `06229cee1ef5c1e3c0111cd56fac57d746dca256555989f8d718c2073291bac2` |
| dependency WurstMMD | `7eee3615f27e92cb852da251928637eee2e7b5412ec40b18e9c93c8ba3e7f38c` |
| dependency wurstNoiselib | `6fe2859da55c6c6de313bef00f94fece6caaef4138f322f1989e6480d735be8a` |
| dependency wurstStdlib2 | `06eba296fe7bd548a3fa499a45e28629468bd2b0868debe91ba010357c642200` |

Measurement inputs were exported into isolated fixtures under the compiler’s ignored `temp/` directory. The exporter records parent/child SHAs, paths, source categories, environment hashes and explicit dependency overrides. Full local `results.json` receipts retain timings, owner/node work counts, phase reports, exact warnings/rejections and oracle outcomes.

The complete 24-state correctness campaign was repeated after integrating the organization-owned generator pin
from master `61c1c33464c8b5f46e977985f2e51912bbcd8702`. Every child again matched its independent fresh Lua and
manifest, with the same 8/12 and 12/12 payload outcomes. This repeat is a correctness check; the timing table above
remains the recorded earlier baseline, not a controlled before/after generator benchmark.

## Backend result and next exit gate

**In the recorded baseline, 0/24 edits used the incremental scalar backend.** Both maps enable fresh compile-time execution and object injection. That scalar cache excludes those options and ordinary calls, storage, class methods and structural edits. All successful measured requests still copied, lowered and emitted tens of thousands of functions. For example, the first Castle Fight child copied 59,637 functions / 2,313,933 nodes, lowered 36,350 functions and emitted 35,318 functions. This campaign establishes no incremental backend speedup.

In that Castle Fight request, compile-time work took 0.29 s, Lua transformation 3.65 s (including generic specialization 0.84 s and translation/emission 1.29 s), and printing 0.07 s. The rest includes frontend/forking, manifest work, reconciliation and delivery. Compile-time execution must stay fresh; its product boundary is required for cache correctness, rather than being the dominant measured time cost.

The new post-compile-time symbol slice adopts generated owned graph snapshots and symbolic call/storage/type operands. Controlled existing-function probes below measure its initial coverage; structural and class edits and changed vararg demand still need producer contracts and explicit dependencies. A slice counts as a practical gain only when this corpus and ordinary body-edit probes show less unrelated backend work, exact fresh-build agreement, and lower whole-request latency. New declarations and generated demand must invalidate their affected products without silently becoming safe because a payload was writable.

No Warcraft client was launched. No claim is made about engine timing or in-game application. Measurements are Windows-only; Ubuntu/macOS timing and actual save-to-applied latency remain unverified.

## Existing-function backend probes

`SymbolHotReloadProfile` edits an existing function in an isolated exported fixture, warms both compiler variants
with a no-op request, measures reconciliation through `LuaHotReloadSession.send`, and independently rebuilds the
edited project. Complete Lua and manifest equality are mandatory; payload publication alone is insufficient.
The symbol variant also requires exactly one runtime function lowered and one emitted. Compile-time evaluation
runs freshly on both variants. The harness restores source in `finally`, locks each fixture against concurrent
probes and retries complete trials across build-date minute boundaries without normalising output.

The checked-in edits cover Zombie Defense's `tileTakesRock` probability (ordinary calls, global array reads and real
comparison) and Castle Fight's `shouldRestartLeaderForEqualSpawn` threshold (real/boolean control flow). These are
edits to existing map code, rather than synthetic functions added to a map. They do not establish that an entire
historical commit takes the incremental path. The armor-cost edit using Math.min/max is covered by the later
vararg/native caller measurements below.

Measured on 26 October 2026, Windows x64, Java 27, 6 GiB heap and ParallelGC. The baseline is exact master
`1c23fb384c0f6c96da55218a996703378014ee51`; the new compiler code is `ef9e5cb3d87d37920f22aa37290c3c6139ccad19`.
Each map/variant has three trials in separate JVMs, in baseline/symbol/symbol/baseline/baseline/symbol order, using
identical current harness/dependencies and the latest exported source states recorded above. All twelve trials
matched independent fresh Lua and manifests and produced one-function payloads.

| Existing function edit | Previous median through payload | New median | Reduction | Compilation alone, old → new |
| --- | ---: | ---: | ---: | ---: |
| Castle Fight sync threshold | 9.71 s | 6.37 s | 34.4% | 7.44 → 4.33 s |
| Zombie Defense rock probability | 5.82 s | 4.89 s | 15.9% | 3.99 → 3.05 s |

Total ranges were 9.29–9.77 s / 6.35–6.44 s on Castle Fight and 5.39–6.06 s / 4.66–6.32 s on Zombie Defense
(previous / new). Zombie Defense's ranges overlap and one new trial exceeded the previous median. Three trials
show a local median gain, not a p95 guarantee. Editor queueing and game application remain excluded.

Each new request lowered **one runtime function** and emitted one, versus 36,757 lowered / 35,723 emitted on Castle
Fight and 30,624 / 29,761 on Zombie Defense with the previous compiler. One compile-time owner was also lowered
freshly per new request. The compile-time bridge still copied 60,018 functions / 2,339,305 nodes on Castle Fight and
54,551 / 1,519,344 on Zombie Defense. Global receipt validation, output assembly, ABI hashing and payload comparison
remain whole-program work.

Cold admission adds work: the independent fresh-model compilation oracles took medians 8.11 → 14.08 s on Castle
Fight and 4.94 → 9.29 s on Zombie Defense. These are compilation-stage observations after model loading, rather than
end-to-end startup measurements. The warm timing table requires a completed admission snapshot. Eliminating the
remaining global work is still necessary for the subsecond target. Full local trial receipts are retained under
`de.peeeq.wurstscript/test-output/symbol-final-benchmarks.json`; the table includes every trial, including the outlier.

Run from `de.peeeq.wurstscript`, after exporting the corresponding fixture:

```powershell
.\gradlew.bat -I ../docs/profiling/runmap-profile.gradle symbolHotReloadProfile -PprofileRoot=temp/map-commits-zombie/project -PprofileEdit=../docs/profiling/edits/zombie-rock.json -PprofilePublicCompiler
```

For Castle Fight select its fixture and `castle-sync.json`. For the prior compiler, compile its exact source archive
separately and add `-PprofileBaseline -PprofileCompilerClasses=<absolute-main-classes-directory>`. Both runs use the
same current harness, dependencies, fixture, 6 GiB heap and ParallelGC. Alternate variants in separate JVMs, archive
`project/_build/symbol-results.json` after each invocation, and report medians/ranges rather than interpreting the
independent fresh oracle's cold admission work as the old warm compiler's baseline. Editor queueing and Warcraft
pickup/application are outside this measurement.

## Vararg/native caller products

Measured 30 October 2026 on Windows x64, Java 27, a 6 GiB profile JVM and ParallelGC. The baseline is
`168b9a95c`; the new compiler is the working-tree vararg/native extension based on that commit. Both variants
include the same pre-existing local CFG/local-merger changes. The isolated fixtures are
`temp/map-commits-zombie-v3-20261026/project` and `temp/map-commits-castle-v2-20261026/project`, with the
public-compiler lane and dependency overrides described above. Original map checkouts were not changed.

Each map has three trials per variant, each in a separate JVM, in baseline/new/new/baseline/baseline/new order.
Every trial matched independent fresh Lua and the complete manifest exactly and published a one-function payload.
Times include reconciliation through payload publication, excluding editor queueing and Warcraft pickup/application.

| Existing function edit | Baseline median | New median | Compilation alone, baseline to new |
| --- | ---: | ---: | ---: |
| Zombie Defense armor cost, `0.06` to `0.08`, with nested Math.min/max | 8.524 s | 3.930 s | 6.877 to 2.353 s |
| Castle Fight sync threshold, already incremental control | 4.888 s | 5.364 s | 3.142 to 3.532 s |

All host-publication samples, in trial order within each variant:

| Edit | Baseline seconds | New seconds |
| --- | --- | --- |
| Zombie Defense armor cost | 8.524, 8.551, 8.359 | 3.893, 3.930, 3.965 |
| Castle Fight sync threshold | 5.828, 4.888, 4.787 | 6.036, 5.364, 5.262 |

The armor edit's median improved **53.9%**. Its counted runtime lowering/emission work, including batch fallback
and cold readmission, fell from 77,688/53,031 functions to **1/1**. One compile-time owner still ran freshly, and
the bridge still copied 54,545 functions / 1,518,737 nodes. The control stayed at 1/1 runtime functions, with a
median **9.7% higher** (+0.476 s) and overlapping ranges. Three trials do not establish parity or a p95 bound;
this slice improves newly admitted callers, rather than proving lower latency for every edit. Semantic receipts,
full-file output, ABI hashing and payload comparison still process global state.

Cold fresh-model compilation-oracle medians were 7.271 to 7.777 s on Zombie Defense and 11.527 to 11.954 s on
Castle Fight. These are compilation-stage observations after model loading, not end-to-end startup measurements.
Full local receipts are in `de.peeeq.wurstscript/test-output/vararg-native-final-benchmarks.json`.

The initial repros failed on the old batch fallback; the implementation passes 633 targeted checks across
`IncrementalSymbolBackendTests`, `IncrementalScalarBackendTests`, `IncrementalHotReloadCompilationTests`,
`VarargTests`, `LuaBackendAuditTests`, `LuaHashtableTests`, `LuaHotReloadTests` and `LuaNativesTests`, with
`asg.checkOwnership=true` and `-PallBackends=full`. Checks cover helper body/arity invalidation, R2I,
nullable-handle wrappers, GetHandleId, hashtable reads, fresh Lua/manifest/payload equality, 1,000 unrelated live
functions and stock-Lua payload execution through an unchanged caller. Deliberately ignoring a vararg original's
body receipt made the fresh-Lua oracle fail on stale helper output; that mutant was removed before the clean checks.
A separate 500-program/1,500-edit hot-reload fuzz campaign (seed `202610301633`, anonymous insertion enabled)
reported zero failures: 1,258 applied payloads, 241 expected refusals and one permitted ambiguous refusal.

Reproduce the armor probe from `de.peeeq.wurstscript` with:

```powershell
rtk proxy .\gradlew.bat -I ../docs/profiling/runmap-profile.gradle symbolHotReloadProfile '-PprofileRoot=temp/map-commits-zombie-v3-20261026/project' '-PprofileEdit=../docs/profiling/edits/zombie-armor.json' -PprofilePublicCompiler --no-configuration-cache
```

Use the Castle Fight fixture and `castle-sync.json` for the control. Select independently compiled baseline main
classes with `-PprofileBaseline` and `-PprofileCompilerClasses=<absolute-directory>`. Measurements and focused
execution are Windows-only; Ubuntu/macOS and current in-game application remain unverified.

## Running another campaign

From the repository root, export into a **new** fixture directory. The template directory must contain
`base.w3x` and `raw/`; the exporter copies those, committed Wurst/Jass sources and config/arguments,
and the map checkout's installed `_build/dependencies`. It never resets or writes the original map checkout.
Use explicit dependency overrides when an installed dependency cannot compile the chosen commits; the
receipt records the override and exact copied content hashes. These inputs are frozen for every pair,
not reconstructed from historical unpinned dependency URLs.

```powershell
python docs/profiling/export-map-commits.py --repository ../wurst-castle-fight --ref origin/master --count 12 --template <template> --output de.peeeq.wurstscript/temp/map-commits-castle
cd de.peeeq.wurstscript
.\gradlew.bat -I ../docs/profiling/runmap-profile.gradle mapCommitHotReloadProfile -PprofileRoot=temp/map-commits-castle -PprofilePublicCompiler -PprofileMeasure
```

For Zombie Defense use `../zombie-defense`, `origin/main` and its own template, omit
`-PprofilePublicCompiler`, and optionally export with `--dependency Frentity=<verified-local-tree>`.
The public-compiler flag removes **only** `-luaEncapsulation`, matching Castle Fight's Jenkins build;
it does not ignore other unknown flags. `-PprofileMeasure` adds phase timing instrumentation.
`-PprofileCommitLimit=1` runs a smoke pair. Each invocation overwrites its fixture's `results.json`;
archive that receipt before reusing a fixture.

A build failure or fresh/retained mismatch fails the task after recording the row. An explicit
`-PprofileContinueOnError` continues collecting failures; inspect every row before treating the task
as successful validation. A rejected payload is a measured `restart` result, rather than a build failure.

`getBuildDate()` observes the current minute. When unequal oracle outputs straddle a wall-clock minute,
the harness retries the **entire** independent-child / warmed-parent / edited-child sequence at most twice.
It records the retries and still requires exact output equality. It never rewrites timestamps, reuses
compile-time results, or ignores mismatching output. Other changing external inputs must be controlled by
the fixture owner; an unstable oracle is not evidence of a cache defect or proof of correctness.
