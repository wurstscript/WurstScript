# Incremental Lua backend plan

Status: proposed design and implementation sequence, 25 October 2026. This document authorizes no change to language
semantics or runtime representation. The initial implementation target is Lua hot reload; the batch compiler remains
the independent correctness oracle while backend stages are migrated.

The first scalar ownership slice is implemented under the restrictions in
[LUA_HOT_RELOAD.md](LUA_HOT_RELOAD.md#first-incremental-backend-slice). It freezes a deliberately closed integer IM
subset into immutable records and retains parameter slots and emitted declaration frames. It requires no generated
schema changes: every other node kind is rejected by the eligibility check. It does not implement a general IM codec,
persistent global dependency worklists, source-map relocations or incremental payload indexing. Those remain planned.

## Outcome and acceptance contract

After changing `return x + 1` to `return x + 2`, with unchanged declarations, dependencies and compile-time outputs:

* Translate, lower, emit and update the manifest entry of that function alone. Existing specialisations of that
  function are affected functions too; an unrelated function must not be copied or processed by a backend pass.
* Keep existing generated code, class layouts, dispatch bindings and runtime helpers when their inputs are unchanged.
* Produce the same Lua and manifest as a fresh compilation with identical options and external inputs. Reusing code
  must not change diagnostics, stack traces, function identities, initialisation order or hot-reload rejection rules.
* Preserve allocation/destruction order, object IDs and reuse, both generic systems, tuple representation, native
  lowering, callback boundaries, arithmetic and all existing optimisations. Add no runtime indirection to make the
  compiler easier to cache.

The cost target is proportional to changed input plus the affected dependency region, not total source/IM size.
Writing a complete requested output file is still proportional to its bytes; hot reload must not need that file.

Performance gates are **targets, not measured promises**: on the recorded Castle Fight and Zombie Defense fixtures,
aim for a warm ordinary body edit at p50 <= 1 second and p95 <= 2 seconds from edit reconciliation through payload
file publication. Measure editor queueing and game pickup/application separately, then report actual save-to-applied
latency. A result for compilation alone does not satisfy this goal. If unchanged compile-time execution prevents
the target, report that floor and profile it rather than weakening semantics.

## Evidence and present limitation

PR #1578 retains frontend IM, then forks the entire graph and reruns compile-time execution, backend lowering,
emission and manifest construction. Its measured compilation improvement is about 11-12%, not a demonstrated
solution to the earlier 14-30 second editor reloads. See [RUNMAP_PERFORMANCE.md](RUNMAP_PERFORMANCE.md).

A subsequent isolated Castle Fight diagnostic included reconciliation, compilation and `LuaHotReloadSession.send`:
six requests took 6.69-8.73 seconds to write a one-function payload. Excluding the first request, median total was
7.13 seconds; compilation 6.14 seconds, reconciliation 0.42 seconds, diff/delivery 0.55 seconds (component medians
need not sum to the total median). Editor queueing and Warcraft application were excluded. This identifies backend
work as the first target; it does not establish current latency in the real editor/client workflow.

Current ownership and phase entry points:

* `WurstCompilerJassImpl.transformProgToLua`: whole-program pass orchestration.
* `IncrementalImTranslation`, `ImTranslator.forkForLowering`, `ImProgramFork`: retained frontend and full private copy.
* `EliminateGenerics`, `SpecializationDemandGraph`: demand and concrete instantiations. The current graph deliberately
  does not survive collection rounds or compilations ([SPECIALIZATION_DEMAND.md](SPECIALIZATION_DEMAND.md)).
* `EliminateTuples`, `StackTraceInjector2`, `VarargEliminator`: body rewrites mixed with signature/helper production.
* `LuaDispatchPreparation`, `LuaTranslator.translate`: global metadata and body emission currently share one run.
* `LuaHotReload.install`, `LuaHotReloadSession.send`: complete manifest construction followed by baseline comparison.

## 1. Persistent artifacts, private mutable work

Keep the existing IM's semantics and phase order. Introduce a persistent artifact boundary, rather than making a
shared mutable `ImProg` the cache. A build reads one immutable snapshot and accumulates replacements in a candidate
snapshot. The language worker remains the single writer; parallel backend execution is not part of this project.

The snapshot contains these independently versioned products:

| Artifact | Owns | Refers to |
| --- | --- | --- |
| Declaration | Signature, flags, generic parameters and immutable semantic metadata | Other declaration keys |
| Source body | One function's pre-lowering statements, parameters, locals and origins | Symbol/type keys |
| Body analysis | Calls, field/global reads and writes, allocations, intrinsic specialisation demand, local effects | Body and queried metadata versions |
| Function instance | Original declaration plus structural type arguments and any fixed vararg arity | Template body, type/ABI/demand facts |
| Lowered body | One function instance at an explicit phase boundary | Resolved ABI/layout/helper facts |
| Class/global product | Layout, type identity, slot bindings, storage, initializer or runtime helper | Exactly the declarations/facts it consumes |
| Lua fragment | Printed function or declaration fragment, relocations, manifest contribution | Lowered body and emission facts |
| Compile-time product | This request's generated declarations, initialisers, values and object-data output | Fresh execution result |

Start with an in-memory cache only. Each query/artifact key includes its phase and a `BackendEnvironment`: compiler/
artifact schema version, target and patch/native ABI, semantic compiler options and compilation-view/config inputs.
An environment change invalidates the products it governs; never reuse Lua/Jass, stack-trace or optimisation variants
interchangeably. Query inputs are phase-specific: pre-compile-time reachability and runtime reachability have different
root sets, and an ABI before tuple/stack-trace lowering is not the final calling convention.

Replace project-wide frontend body discovery with a source revision journal owned by `ModelManager`. Parsing and
`BodyOnlyEdit` record body replacements and position updates as part of applying an edit; declaration and selected-unit
membership changes have their own revisions. Derive changes from those owned operations, not a handwritten list of
invalidations in each backend pass. Reconcile the edited unit, not every unit, to obtain the initial dirty owners.
The completed path must also retire the full-project `sourceBodies`/preservation scans used by the current prototype.

`ImTranslator` becomes a request facade over explicit services and owned scratch state. Move each map to its owning
service when porting its producer/consumers; do not perform a preliminary mass split of the class. No artifact's
correctness may depend on copying an undocumented collection of translator fields.

### Keys and identity

Use a session-local `SymbolKey` allocated for a resolved declaration, retained across body edits. It is a compiler
handle, never an emitted integer or a runtime identity. Declaration replacement is explicitly reconciled or
invalidated; never match symbols by generated names. Initially, ambiguous declaration changes can rebuild the snapshot.

`InstanceKey = (SymbolKey, structural type arguments, explicit generated role, optional fixed arity)`.
Type arguments use declaration/type-parameter keys and recursively structured types, including dictionary bindings.
Generated roles identify their producer and semantic operands, not the producer's output name. This includes closure
classes, bridges, callback adapters, allocators and compile-time initialisers.

Keep cache keys separate from runtime names, dispatch slots and numeric type IDs. Those must retain the existing
fresh-build deterministic rules. Inserting a declaration can change numeric type IDs: rebuild the affected identity
table and its consumers, or conservatively rebuild, rather than assigning history-dependent IDs.

### Frozen representation and the existing generated IM

Published artifacts are immutable, detached from mutable editor AST nodes, and contain no foreign mutable IM nodes.
Within a function, parameter/local references use owned slots. References outside it use typed `SymbolKey` operands;
types are immutable structural values. Source origins resolve through a separate source-map revision.

Implement a lossless freeze/materialise boundary for the generated IM: owned node data, local slots, symbolic
external references and explicit metadata. Existing IM remains the mutable working format of a pass while that pass
is ported. Materialise only dirty owners; readers obtain dependency summaries/declaration views through queries.
A header view must not masquerade as a valid function with an empty body. A pass that needs a callee body must ask
for it and record that dependency. It cannot silently traverse an arbitrary foreign IM graph.

Generate structural traversal/reference encoding and ownership checks in the owning `abstractsyntaxgen` project,
then update its pin here where applicable. Change `.parseq` contracts and regenerate; do not hand-edit generated
Java or maintain a second handwritten list of IM reference fields. Prototype this boundary on one lowered scalar
function before committing to generator-wide work. Unsupported node kinds fail explicitly during the prototype.

Freezing/materialising costs O(size of dirty owner). No whole-program `ImProgramFork` may remain on the completed
body-edit path. Published data is never passed to a destructive pass. Tests must detect attempted foreign mutation,
shared mutable types, stale AST traces and references to an obsolete snapshot.

## 2. Dependency queries, not blanket invalidation

Provide `BackendQueries` with typed reads such as `signature`, `bodyFacts`, `specialisationDemand`, `layout`,
`dispatchGroup`, `constantValue` and `sourceMap`. Evaluating a query records every input version it reads, including
collection membership and negative lookups. Register new dependencies before publishing its result.

Maintain reverse edges and an explicit change worklist. Re-evaluate dirty queries; propagate only when their result
changes. Do not recursively validate the whole dependency closure on every cache hit. Immutable artifact replacement
provides change notifications; legacy mutable owners use generated mutation tracking plus `modificationCount()`.
Individual passes must not remember to call `cache.functionChanged`.

Separate revisions for body, ABI, layout, dispatch membership/binding, demand, effects, constant value, source map
and emission naming. A single program epoch attached to every function would recreate whole-program invalidation.

| Consumer | Dependency that can invalidate it | Does a callee literal-only body edit propagate? |
| --- | --- | --- |
| Ordinary direct caller | Callee ABI and selected target | No |
| Local optimisation using callee purity/effects | Callee effect result | Only if that result changes |
| Inlined caller | Callee body and relevant ABI/facts | Yes; existing hot reload forbids `-inline` |
| Generic caller | Callee demand result, dictionary target and relevant type facts | Only if those results change |
| Virtual call site | Group ABI/slot identity; possible-target facts if used for direct-call lowering | Only if those facts change |
| Class slot binding | Override membership and chosen implementation identity | No; replacing that implementation's body keeps the binding |
| Folded constant consumer | Evaluated constant value | Only if the value changes |
| Function fragment with stack-trace positions | Source-map relocations | Re-render affected text; do not repeat semantic lowering |

An ABI includes the *lowered* calling convention: tuple components/results, stack-trace parameters, varargs,
callback adapters and native representation. Checking only the Wurst source signature is insufficient.

## 3. Separate global facts from transformations

Preserve current pass ordering and semantics during extraction. Do not introduce a separate simplified development
backend. Cold and incremental execution use the same producers once each producer has been migrated.

| Existing stage | Persistent product / incremental responsibility |
| --- | --- |
| Reachability / tree shaking | Per-owner outgoing edges and root sets; retain dormant artifacts outside the emitted reachable view. Recompute affected cyclic regions on edge/root removal; reference counts alone cannot collect disconnected cycles. |
| Generic demand | Versioned intrinsic facts and reverse dependencies. Derive the same least fixed point after edge additions/removals, including seed loss in recursive cycles. Port the existing predicates/oracle, not a weaker approximation. |
| Generic specialisation | Registry of `InstanceKey` products. Dirty templates invalidate their existing instances. New demand discovers instances to a fixed point; removed demand removes emitted instances only when no reachable root requires them. |
| Stack traces / tuples / varargs | Compute calling-convention and fixed-arity products separately from rewriting bodies. Callers subscribe to those products; preserve multi-result evaluation and callback rules. |
| Native/keyed-table lowering | Versioned intrinsic/helper descriptors and per-body rewrites; helper discovery emits requests to the worklist rather than editing an unrelated function. |
| Local types / flatten / local optimisations / old-generics casts | Function-owned transformations with explicitly queried callee/dispatch facts. Retain current flatness and zero-sentinel invariants. |
| Field defaults / garbage removal | Separate local data-flow results from global liveness/effect facts. A changed constructor summary may invalidate allocations that consumed it. Preserve effectful dead-result expressions and allocation/destruction order. |
| Dispatch preparation / reflection / type IDs | Independently versioned class/group products. Regenerate affected descriptors and subscribers, preserving root-slot bindings, aliases, bridges, deterministic IDs and reflection results. |
| Lua emission / locals spilling | Emit one function using its lowered body and immutable declaration environment. Keep current local/register limits, helper choices and storage representation. |

Dirty analysis is not automatically dirty code generation. If a function adds a call to an existing helper, update its
outgoing edges; if reachability/demand/ABI results do not change elsewhere, other bodies remain reusable. Whole-program
facts are allowed; whole-program recomputation after every body edit is the behaviour being removed.

Global analyses that cannot yet be updated safely may use an explicitly recorded batch rebuild during rollout.
That is a correctness bridge, not completion of the incremental backend. Work counters must expose every such rebuild.

## 4. Compile-time execution and request ownership

Continue executing compile-time code on every compilation under its existing semantics. Do not infer purity from
unchanged source: time, files, map data and native effects can change independently. External-input tracking and
result caching are separate future work, not a prerequisite or an undocumented assumption here.

Use fresh interpreter state and request-owned evaluation results. Materialise the needed compile-time code from
immutable artifacts; reading one does not dirty it. Keep expression-result caches outside published IM. Compare the
resulting semantic products with the previous request, including values, reference sharing, storage, generated code,
read type IDs and ordered effects. Equal products retain their revisions; changed products invalidate their real
runtime consumers. Equality must preserve object/hashtable graph aliasing, not merely compare printed values.

Cancellation/failure discards candidate compiler products and cannot mutate the last valid snapshot. This transaction
does not claim to roll back arbitrary external effects performed by user compile-time code; keep the existing effect
behaviour. Compiler snapshot publication and the running map's session baseline are separate: a successful compile
does not mean that a payload was delivered/applied. Preserve the current session launch/send recovery protocol.

## 5. Incremental emission and payload production

Cache Lua *fragments and manifest contributions*, not one already-bound mutable Lua AST. Each fragment has a semantic
owner, body revision and the exact ABI/layout/name/source-map dependencies consumed while emitting it. Function-local
temporary names cannot depend on the order other bodies happen to compile.

Source positions need explicit handling: moving an unchanged function can change diagnostic/stack-trace strings.
Keep origin relocations in artifacts and resolve them when rendering. If the current naming/type-ID policy observes
the move, update those facts and their subscribers too. Do not return stale strings to claim a cache hit.

Maintain the full manifest as a persistent indexed view of contributions. Maintain candidate differences against the
active session baseline and cumulative history, not just the previous compiled version. Reverting an edit, retrying
delivery, adding/removing functions and switching sessions must behave like today's full `LuaHotReload.payload`.
All compatibility, lineage, captured-definition, initialisation and ABI checks still apply.

The dirty set must include consumers whose compatibility facts changed even when their text did not. Record
collection-level dependencies for global compatibility checks; do not validate only changed function strings.
An internal incremental payload entry point can query persistent indexes and affected groups. In oracle tests,
compare it with the existing full-manifest algorithm after every edit.

For hot reload, publish the payload without printing/writing a complete `02_compiled.lua` or serialising the whole
manifest. Build/export requests still concatenate cached fragments in the existing canonical order and must match
a fresh build byte for byte. Runtime bootstrap, chunk-local storage aliases and names are separately owned products;
their bindings must not change merely because an unrelated fragment was reused.

## 6. Example invalidation walkthroughs

1. **Literal-only edit in `f`.** Replace its source-body artifact; refresh local facts. If outgoing edges, ABI,
   effects and demand are equal, lower its existing instances, emit their fragments, update their manifest entries,
   and compare those contributions with the active baseline. A caller of `f` is untouched.
2. **`f<T:>` starts constructing `T`.** Its intrinsic demand changes. Propagate through affected generic callers and
   virtual dependencies; materialise newly required instances, layouts and bindings, then lower/emit their consumers.
   An unrelated non-generic subtree remains reusable.
3. **A body starts calling dormant `g`.** Activate the reachable closure of `g` and its required products. Removing
   the last entry to a recursive group deactivates the group. Cached dormant code can remain under the memory policy.
4. **A class gains an override or field.** Update class/group facts and consumers. Root-slot and field-default
   behaviour remains the current behaviour. Existing hot-reload compatibility decides whether the payload can apply.
5. **Compile-time input changes without a source edit.** Re-execute it; replace changed initialiser/value/storage
   products and update their consumers. Unrelated function bodies keep their artifacts.
6. **An unchanged closure moves down a line.** Resolve new source positions without semantic lowering. A closure
   insertion/removal invalidates its owning function and generated products and retains existing lineage/rejection rules.

## 7. Implementation sequence and stop conditions

Each step is a reviewable PR or a small series when ownership crosses into `abstractsyntaxgen`. No step is advertised
as a reload-speed solution before whole-request measurements and work counts establish it.

| Step | Concrete work | Exit gate |
| --- | --- | --- |
| 0. Baseline and executable contract | Extend profiling through reconciliation, actual editor request queue, compile-time work, each backend phase, manifest/diff and delivery. Add per-stage owner/node/copy counters and record current whole-program work. | Reproducible same-input cold/warm timings; distinguish host publication from game application; no new runtime code. |
| 1. Ownership vertical slice | First demonstrate a failing literal-edit reuse assertion, then implement frozen symbolic artifacts, source origins, versioned declaration queries and one scalar-function materialisation. Extract the necessary body-local transformations and one-function Lua emitter from their owning passes. | Merge the assertion with its implementation green: a literal edit skips the full backend and touches only `f`; exact fresh Lua/manifest/payload and zero foreign writes. If achieving this requires a whole-graph copy, revise the design before widening it. |
| 2. Complete body pipeline | Port tuples/multiple results, stack traces, native lowering, old generics, local types/flattening, field-default/effect consumers, optional local optimisations and spill handling. Inventory every consumed ABI/fact. | Supported ordinary functions share one implementation with cold compilation; body edits preserve runtime output shape and unaffected artifacts. Unsupported cases still take an observable batch path. |
| 3. Persistent global worklists | Port reachability, demand, instance production, vararg variants, layouts/dispatch/identity products and their removals. Separate registry discovery from owned-body rewriting. | Class/generic fixtures work without rebuilding independent regions; cycle/seed-removal tests and fresh specialisation oracle pass. |
| 4. Fresh compile-time bridge | Replace full private graph copying with request-owned interpreter inputs/results and versioned generated products. Keep all required execution/effects. | Unrelated runtime body edits do not lower compile-time or generated runtime functions again when products are equal; changed products and failures recover correctly. |
| 5. Complete reload output path | Persistent fragments/contributions, source-map rendering, indexed compatibility/diff, direct payload publication. Keep full-output assembly for build/export. | Simple edits do not emit/print/hash/serialise all functions; full payload oracle and cumulative delivery/retry histories agree. |
| 6. Rollout and retirement | Real-map edit campaigns, cold-start/memory checks, all existing tests/CI and latest-head Codex review. Retire the whole-program fork from the migrated path and temporary batch adapters. | Correctness, bounded-work and end-to-end performance gates below pass. One automatic path, no quick/safe mode split. |

The first useful proof is step 1, not a large framework followed by a late performance experiment. Begin with the
counter test, artifact boundary and scalar vertical slice. Do not begin with replacing the object model, disabling
optimisations, broad source-file rearrangements, parallelism or rewriting every pass at once.

Step 1 starts with a fixture having no compile-time expressions, class/schema changes or new helper demands. It
must bypass the full backend for its eligible body edit; unsupported inputs still use the existing path. Full-output
assembly and batch compatibility checks may remain until step 5, but must be timed and counted separately. Exact
oracle comparisons can assemble cached contributions for tests without treating that assembly as incremental work.
Widen eligibility only as the corresponding producers and dependencies gain explicit contracts and tests.

## 8. Validation and performance gates

* **Independent oracle:** build every expected edit state with the batch backend before replaying the incremental
  sequence, so shared AST/lookup caches cannot hide invalidation bugs. Compare diagnostics, Lua, full manifest and
  payload/rejection/warnings, plus runtime observations. Use controlled external inputs for deterministic comparisons.
* **Bounded work:** a warmed scalar literal edit lowers/emits exactly its affected instance(s), copies zero unrelated
  function nodes, and rebuilds zero class/dispatch/demand regions with unchanged facts. Add thousands of unrelated
  functions and assert these counters stay constant. Do not use brittle wall-clock assertions in CI.
* **Dependency adversaries:** added/removed edges, disconnected cycles, loss of demand seeds, unchanged summary
  cutoffs, generic dictionaries/instances, native helpers, callback boundaries, signature/tuple changes, overloads,
  negative lookups, root slots, sibling overrides, type-ID renumbering, constants and source movement.
* **Execution:** old/new generics, allocation/destruction and ID reuse, effects and evaluation order, closure lineage,
  shared compile-time object graphs, handles/hashtables, runtime initialisation, reflection and Lua local/register
  limits. Keep normal Jass and Lua behaviour checks; this project changes no representation or arithmetic policy.
* **Recovery:** fail/cancel each phase, repair source, retry delivery, restart/switch sessions, change options/patch/
  configured map script, clean the model and rebuild. Eviction drops performance only, never dependencies needed to
  detect stale results; snapshots cannot retain obsolete AST/model graphs indefinitely. Bound retained artifact
  generations and measure heap retention as well as transient allocation.
* **Randomised validation:** extend `IncrementalModelOracleTests`, `IncrementalHotReloadCompilationTests` and the hot
  reload fuzzer to run persistent warm backend sessions against uncached references. Before merging correctness
  changes, run at least 500 programs with a fresh seed and anonymous insertion enabled; plant a dependency/ownership
  mutant and demonstrate a failure before the final clean campaign.
* **Real-map measurements:** same source snapshots, JVM, flags and external inputs; alternate fresh/incremental order,
  report p50/p95, first reload after CLI launch, ordinary and dependency-changing edits, allocations and retained heap.
  Include a release-options runtime/output comparison so faster compilation cannot hide slower generated code.
* **Platforms and game:** focused checks on the available host, Ubuntu PR CI, and explicit Windows/macOS gaps. A
  maintainer-run Warcraft probe measures save-to-applied latency and checks state preservation; stock Lua does not
  establish engine timing, numerics or preload behaviour. Do not launch Warcraft automatically.

Completion means both bounded backend work and useful reload latency. A warm request that still walks, clones,
lowers or emits the complete function set is an intermediate milestone, even if a microbenchmark improves.
