# Lua-native ArrayList storage

Implemented on 2026-10-22 in [WurstStdlib2 #517](https://github.com/wurstscript/WurstStdlib2/pull/517)
and this compiler change. The measurements below distinguish actual compiler
output from the earlier layout model. No Warcraft speedup has been measured.

## Implementation and manual memory management

ArrayList keeps its public Wurst object, integer ID, explicit size and destructor.
Jass uses the existing shared arena. Lua uses the compiler's existing typed array
fields: `elements[listId][index]`, with shared metatables for primitive defaults.
Tuple arrays lower to one buffer per component, without a tuple allocation per
element. There is no ArrayList-name recognition, address-pattern recovery, new
intrinsic, integer cast of a generic value, or `#`-based length.

The private field has the fixed initial size required by the Jass type checker.
It is only indexed on Lua, where array tables grow dynamically. Optimized Jass
removes that field and the Lua branches; unoptimized Jass still executes the arena
implementation. The storage accessors inline to ordinary array accesses. Physical
Jass write addresses are evaluated before updater callbacks, as before.

**Table reuse is part of the compiler change.** Previously allocating an object
with an array field always replaced its old table, including on a recycled Wurst
ID. The allocator now resets and reuses the table in that field's ID slot. Wurst
arrays have no first-class table identity, so resetting the buffer preserves fresh
field defaults. It does not alter ID allocation, destruction, reflection counts,
stale-ID aliasing or constructor execution. This applies to array fields generally.
Generic specializations can share a field's canonical storage, so recycling also
restores the current element type's default metatable, including a nil default.
All of an allocator's array resets share one temporary; a wide class does not
acquire a per-construction locals table merely because it has many array fields.

ArrayList's `destroy` clears all live and reset/truncate-retained references; it
does not destroy its Wurst elements. `clear` releases those references while
retaining capacity, and `reset`/`truncate` intentionally retain them. The next
construction resets the buffer to its typed defaults. No Wurst lifetime depends
on Lua GC, and emitted production code calls no collector API.

This deliberately retains high-water capacity, per array field and visited object
ID. It is not immediate physical memory deallocation, nor a pool bounded only by
the current number of lists: unrelated classes share Lua's Wurst ID allocator.
The extra table per list/component and clearing it on ID reuse can cost more for
tiny, short-lived lists. Reusing a scratch list remains preferable.

Two compiler causes found during implementation are fixed before their consumers:
constant branches are simplified before inlining costs are computed, and the
shared default-value helper preserves a class `null` as typed IM null until
backend lowering. The latter previously made a class-valued tuple field compare
numeric `0` with Lua `nil`, with either ArrayList representation.

## Actual generated-code benchmark

The [fixture](profiling/lua-opportunities/LuaArrayListBenchmark.java) compiles the
real arena source from stdlib commit `84ca5c6db082683fd581fe4041a4f9f6b68191a6`
and the native source from `60f7a4ae77c4059c00f4fa92d212dfcd764af43c` with the same
compiler, `-inline -localOptimizations`, and no stack traces or name compression.
The [driver](profiling/lua-opportunities/generated-arraylist.lua) loads the emitted
scripts unchanged, with the test WC3 runtime. It times public ArrayList operations,
including Wurst object management, bounds checks and final cleanup. Seven rounds
rotate old/new process order and check every checksum. Setup and warm-up are outside
the timer; normal Lua GC and explicitly stopped GC are measured separately.

The [raw samples](profiling/lua-opportunities/generated-arraylist-stock-lua53-windows.csv)
record timings, heap deltas, iteration counts and each generated script's SHA-256.
The stopped-GC run rejects native heap growth above 8 KiB after warm-up. This tests
steady-state allocation growth, not a claim about WC3's collector or a measurement
of peak memory for every allocation history.

To reproduce, write the arena source from the recorded git revision to a UTF-8 file
outside the fetched test stdlib. Run from `de.peeeq.wurstscript` (paths are examples):

```powershell
.\gradlew.bat -I ../docs/profiling/lua-opportunities.gradle test --tests tests.wurstscript.tests.LuaArrayListBenchmark -ParrayListSource=<native-ArrayList.wurst> -ParrayListBaseline=<arena-ArrayList.wurst> -ParrayListTests=<ArrayListNativeStorageTests.wurst> -PskipErrorProne
```

Then, after compilation and other CPU-intensive checks finish, run from the repo root:

```powershell
python docs/profiling/lua-opportunities/benchmark-generated-arraylist.py --lua de.peeeq.wurstscript/src/test/resources/lua.exe --output samples.csv
```

Windows Lua 5.3.5 is the available host. Linux/macOS and actual Warcraft III remain
unverified. In-game acceptance still needs alternating old/new map runs for these
workloads and the real consumer, including long sessions and retained memory. The
PRs remain draft; a stock-Lua result is not enough to select this representation
for a release.

## Final generated-code measurements

The checked-in Windows stock Lua 5.3 CSV contains 140 samples, with old/new order rotated across seven rounds and GC both running and stopped. Growth and refill workloads showed gains; scan and tiny-list lifetime timings were inconclusive on this noisy host. These are generated-code measurements, not Warcraft III measurements or whole-map speedups.

With GC stopped, the native implementation's maximum heap increase after warm-up was 1.230 KiB, including 524,288 short list lifetimes. The runner enforces an 8 KiB growth ceiling. This measures retained heap growth, not an exact allocation count or peak memory usage.

Final validation passed 802 focused compiler tests, 755 stdlib tests, and three generated-code benchmark fixture tests. Actual-game profiling and a fresh PR review remain outstanding.

## Original investigation

`WurstStdlib2/wurst/data/ArrayList.wurst` implements new-generic `ArrayList<T:>`
through one static `store` per element type. Each list owns an offset and capacity.
Allocation scans free sections; compaction sorts and merges them. Growth allocates
another section, copies live elements, clears the old Lua references, and releases
the old section. Lua skips the Jass storage-limit error, but retains this allocator.
Even after the shared store passes the Jass limit, allocation still tries compaction.

That implementation belongs to the standard library. The initial proposal was to
add vector intrinsics, following KeyedMap. Implementation found that existing typed
array fields already express the required storage, so no new intrinsic was needed.

A read-only Castle Fight artifact snapshot had 5,622,350 bytes and 256,072 lines
(SHA-256 `3b9a984275d8a82ca94a14e1ac7a3e0b1de9278c9a0c336e80fd398ba74f4fde`).
A syntactic scan found 115 getter definitions returning
`store[start[list] + index]`, sharing one start-index field. There were 1,454
references to that field, 2,335 to the capacity field, and 669 to retained size.
These are static coverage indicators, not call counts or percentages of runtime.
The artifact was regenerated during the investigation, so compressed slot numbers
and line positions must not be treated as stable identifiers.

The proposed Lua backing store is one dense table per list. Its public Wurst class
remains an integer ID with the same allocator and explicit destructor. Lua manages
the backing table's physical capacity; appending does not run a Wurst free-section
manager or relocate the logical list. Jass keeps its arena fallback. Ordered removal
still moves elements; this does not turn an O(n) operation into O(1).

## What the probe establishes

Run from `de.peeeq.wurstscript`:

```powershell
.\src\test\resources\lua.exe ..\docs\profiling\lua-opportunities\collection-layout.lua 7 1024 samples.csv
```

[Driver](profiling/lua-opportunities/collection-layout.lua) and
[raw Windows Lua 5.3.5 samples](profiling/lua-opportunities/collection-layout-stock-lua53-windows.csv).
Seven rotating rounds; identical checksums; locals/upvalues inside timed loops.
The read workload visits 256 lists of 64 elements 1,024 times. Setup is outside
the timer. Arena-hoisted caches each list's offset outside its element loop, so
a representation change is not credited for an ordinary hoisting opportunity.

| Workload | Arena, seconds | Arena-hoisted | Native vector |
| --- | ---: | ---: | ---: |
| Dense live storage | 0.221 | 0.179 | 0.173 |
| Sparse, aligned offsets (stride 4096) | 1.813 | 1.770 | 0.168 |
| Sparse, staggered offsets (stride 4099) | 0.251 | 0.188 | 0.165 |
| Sparse after both stores were fully populated | 0.222 | 0.169 | 0.175 |
| Grow/destroy 1024 sequential lists of 4096 values | 0.230 | n/a | 0.090 |

The growth control reuses one shared arena across all 1,024 sequential list
lifetimes, with perfect offset reclamation between lists. Only the native variant
creates a table per list. Both explicitly clear their final live references, and
the arena additionally relocates and clears at each capacity increase. Free-section
searches/compaction, Wurst object bookkeeping, bounds checks and callbacks are
excluded; this is a favorable arena control, not a replacement implementation of
ArrayList. Lua's own allocation and collector costs during the workload are timed.

The useful result is removal of a whole growth/allocator layer, rather than a
promise that every table read becomes faster. Dense and previously dense reads
were effectively equal after hoisting. The aligned sparse case exposes a large
representation-sensitive cost, not a general tenfold prediction. Lua's
[table implementation](https://www.lua.org/source/5.3/ltable.c.html) chooses between
array and hash storage using occupancy and hashes integer keys into power-of-two
bucket counts; alignment and allocation history therefore matter. The probe does
not establish the real map's live arena density, history or time spent growing.
Its diagnostic checksums use stock-Lua arithmetic, not Warcraft's numeric model.

## Original acceptance plan

1. Use typed storage operations with concrete primitive/handle values in their native
   representation. Existing array fields satisfy this without new intrinsics or
   name-based rewrites.
2. Have ArrayList use this storage in its owning stdlib repository, retaining the
   Jass implementation. Preserve zero-based indexing, bounds/error behavior, null
   defaults, copy/self-copy and ordered/unordered removal. A Lua table's `#` is not
   the logical size: nil elements and holes require an explicit count.
3. Preserve `reset`/`truncate` reference retention and `clear`/destroy release of
   those references, including retained aliases. Preserve Wurst object-ID allocation,
   recycling, casts, destruction effects and observable reflection counts. Do not
   replace Wurst objects with Lua-GC ownership or elide observable allocations.
4. Compare optimized generated programs, including many small lists, growth-heavy
   lists, churn, retained capacity, sparse occupancy, nested users and peak memory.
   Require end-to-end tests on both targets and alternating real-client runs before
   selecting the representation. The extra Lua table per list may lose for small
   lists or increase allocation pressure; the probe does not settle that tradeoff.

## Second target: consume collections without redundant materialization

The map's projectile query path fills the spatial index snapshot, copies it into
an ArrayList, closes/clears the snapshot, then scans the list. The stdlib's
`SpatialIndexForUnits.appendMatches` deliberately closes the snapshot before caller
code: a filter or callback can yield, throw, mutate the registry or start a nested
query. Blindly fusing these loops would change those guarantees.

A broader compiler/library design could transfer ownership of an independent native
result vector to the consumer, or specialize a reduction when its callback is proven
not to escape, yield, re-enter or mutate what is being traversed. It should remove
an entire copied collection/pass, not just cache a dispatch slot. Preserve order,
snapshot visibility, early exit, errors and destruction. This needs a semantic
producer/consumer contract; do not infer one from adjacent generated loops.

The ArrayList implementation is the first step. Existing direct keyed maps already bypass
Jass-style hashing for supported new-generic keys. A migration of legacy generic
containers or a wholesale class/closure representation change is a separate design,
not an automatic extension of this proposal.
