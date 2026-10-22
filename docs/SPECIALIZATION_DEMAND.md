# Specialization demand worklist

Lua specialization repeatedly searched shared callees, virtual overrides and generic class members
to decide whether a concrete type argument was needed. Each independent function query started
with fresh visited sets. Completing a method call's type arguments could also clear all remembered
method answers. A local dependency worklist now reuses those facts within a collection round.

## Contract

`SpecializationDemandGraph` lazily builds facts keyed by IM node identity. Functions have local
intrinsic demand and outgoing call/class dependencies; virtual methods depend on implementations
and overrides; class-members nodes depend on their implementations and inherited members.
The graph and reference search share the local demand and dependency predicates.

Intrinsic demand propagates to callers through reverse edges. Queries on built facts read a boolean.
Seedless recursive cycles remain false. When method-call argument completion changes dependency
eligibility, the graph rebuilds that owner's outgoing facts and recomputes the affected reverse
region. Clearing that region before reseeding prevents a cycle from retaining its own stale true
answer when its only external demand disappears.

The graph exists only during `collectGenericNewRoots`. Bodies and dispatch structure stay fixed
during collection; the existing method-call argument mutation site updates dependency eligibility.
The graph is discarded before specialization mutates bodies. Queries while scanning newly
specialized bodies still use the reference search. No facts survive a specialization round,
compilation or source edit; there is no cross-build invalidation contract.

Initial discovery/propagation is linear in the materialized graph. Dependency updates can revisit
affected callers. This removes repeated demand discovery, not necessary instantiation or copying,
and changes neither specialization policy nor emitted script.

## Real-map benchmarks

Measured on baseline `9893fb9accc7ce7a2b7b5bdbceb6f2135c678fef`, which includes incremental LSP
PR #1542, on Windows x64 with Zulu Java 27, `-Xmx4g -XX:+UseParallelGC`. The PR is based on newer
master; the measurements below describe that recorded benchmark baseline, not a new measurement
of the later master changes.

Both algorithms ran in the same JVM on isolated copies of Castle Fight and Zombie Defense, using
the real RunMap pipeline with Lua and optional optimizations disabled. Each session alternated
original/edited source and rotated algorithm order. Twelve rounds left **nine measured pairs**
after discarding three warm-up rounds. Requests include compile-time execution and map injection,
ending before game launch; edit reconciliation is separate. A fixed timestamp made script hashes
comparable. JFR and work counters were disabled during timing. The finer timing labels and earlier
scope-scan pruning were shared by both variants and are outside this PR's production changes.

Final implementation; positive paired savings:

| Map | Specialization legacy / worklist medians | Paired specialization saving | Paired whole-request saving |
| --- | ---: | ---: | ---: |
| Castle Fight | 1,334 / 673 ms | **611 ms / 49.0%** | **827 ms / 17.6%**, faster 9/9 |
| Zombie Defense | 361 / 295 ms | **26 ms / 8.8%** | **135 ms / 6.0%**, faster 6/9 |

Paired savings are medians of within-round differences, not differences between the two medians.
Castle specialization saves 368–1,459 ms across pairs; Zombie ranges from losing 118 ms to saving
279 ms. Whole-request ranges are +477 to +2,539 ms and -377 to +761 ms, respectively. Movement in
untouched phases and GC/JIT/host variability means the whole-request percentage is an observation,
not an exact causal estimate. The phase measurements and work counts are stronger evidence.

An earlier prototype with set-based adjacency saved 240 ms / 33.9% in Castle specialization and
282 ms / 8.5% overall, but was neutral on Zombie (-2 ms in specialization, -6 ms overall). The final
version uses adjacency lists. Independent sessions have substantial absolute timing variation;
do not subtract their medians to estimate the effect of this revision. Both trials support a
substantial Castle improvement; Zombie is approximately neutral to modestly positive.

Untimed audit counters, summed over four collection rounds per request:

| Work | Castle Fight | Zombie Defense |
| --- | ---: | ---: |
| Legacy recursive function visits | 4,137,413 | 225,949 |
| Legacy relevant-node checks | 5,193,453 | 254,497 |
| Worklist fact constructions, including changed owners | 56,912 | 34,605 |
| Worklist relevant-node checks | 184,229 | 86,418 |
| Remaining recursive relevant-node checks outside the graph | 11 | 0 |
| Call-dependency eligibility changes | 2,260 | 612 |

Castle's legacy checks are about **20.4x** Zombie's, while worklist checks are about **2.1x**.
This supports overlapping searches as a source of growth; two maps do not establish an asymptotic
scaling law. Frequently revisited Castle helpers included `int_toString`, `meta`, `player_getId`
and `__wurst_ensureInt`. Names were used only for diagnostics, not as semantic graph keys.

## Inputs and evidence

The map sources are project snapshots rather than compiler test fixtures. The two source states
were produced by these reversible body edits, restored by the driver on exit:

| Map | Map folder | Source | Original | Edited |
| --- | --- | --- | --- | --- |
| Castle Fight | `CFDE_base_map-folder.w3x` | `wurst/BountyRuntime.wurst` | `return max(0, result)` | `return max(1, result)` |
| Zombie Defense | `Zombie_Defense_02-folder.w3x` | `wurst/DamageContext.wurst` | `return ctxSourceIndex` | `return ctxSourceIndex + 1` |

[Recorded samples](profiling/lua-lowering/results) contain `castle-demand-graph*` and
`zombie-demand-graph*`: `*-final-phases.csv` has all twelve paired rounds with exact emitted Lua
hashes and nested phase times, `*-final-work.csv` has the post-timing audit counters for both source
states, and `*-demand-graph-phases.csv` preserves the first prototype's timings. Select `round >= 3`
and pair on `round` for the reported savings. No private map source, MPQ, JFR or large raw log is
included. These are measured samples, not synthetic performance assertions in CI.

## Validation

Each session checked exact emitted Lua SHA-256 equality between algorithms on every timed round,
then built both source states with both algorithms using freshly loaded models. All matched.
Across both prototype trials and both maps that is **112 map requests**. This checks script
identity, not byte identity of the complete MPQ. The original projects were untouched.

The graph tests cover cycles, loss of an external seed, independent diamond paths, a 20,000-node
chain with no repeated fact construction, and 200 deterministic random graph changes compared
with independent searches. A compiler fixture exercises recursive positive/negative demand from
both entry points on Jass and Lua. In unit-test mode every graph query is compared with a fresh
reference search. The benchmarked prototype passed 951 focused cases without failures or skips:
graph, type-class, field-iteration, generic-specialization matrix, Lua translation and backend audit.
The same focused suites passed **955 cases, zero failures or skips**, on PR base `9820e3a98`
with Error Prone enabled and the normal stdlib prefetch; that base adds four backend audit cases.

Identity maps/sets are used for lookup/membership; graph traversal follows stable input lists.
The change adds no platform-specific production code or dependency. Local measurements and tests
ran on Windows; Linux/macOS timings and in-game execution are unverified. The compiler contains
no benchmark comparison flags, counters or diagnostic prints.
