# Lua collection storage proposal

Investigation on 2026-10-22. This is a proposal and a stock-Lua mechanism probe,
not an implemented collection backend or a measured Warcraft speedup.

## First target: remove the shared ArrayList arena on Lua

`WurstStdlib2/wurst/data/ArrayList.wurst` implements new-generic `ArrayList<T:>`
through one static `store` per element type. Each list owns an offset and capacity.
Allocation scans free sections; compaction sorts and merges them. Growth allocates
another section, copies live elements, clears the old Lua references, and releases
the old section. Lua skips the Jass storage-limit error, but retains this allocator.
Even after the shared store passes the Jass limit, allocation still tries compaction.

That implementation belongs to the standard library. A compiler fix should supply
a storage abstraction the library can use, as the existing KeyedMap intrinsics do;
it must not recognize an ArrayList class by generated name or reverse-engineer its
lowered address arithmetic.

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
| Dense live storage | 0.315 | 0.208 | 0.208 |
| Sparse, aligned offsets (stride 4096) | 1.862 | 1.841 | 0.178 |
| Sparse, staggered offsets (stride 4099) | 0.260 | 0.197 | 0.168 |
| Sparse after both stores were fully populated | 0.224 | 0.177 | 0.178 |
| Append 4096 values, 1024 fresh lists | 0.380 | n/a | 0.046 |

The growth control models only explicit relocation and clearing, with a monotonic
allocator. It excludes free-section searches/compaction, Wurst object bookkeeping,
bounds checks and callbacks. It is not a replacement implementation of ArrayList.
Both variants pay Lua's own allocation and collector costs during growth.

The useful result is removal of a whole growth/allocator layer, rather than a
promise that every table read becomes faster. Dense and previously dense reads
were effectively equal after hoisting. The aligned sparse case exposes a large
representation-sensitive cost, not a general tenfold prediction. Lua's
[table implementation](https://www.lua.org/source/5.3/ltable.c.html) chooses between
array and hash storage using occupancy and hashes integer keys into power-of-two
bucket counts; alignment and allocation history therefore matter. The probe does
not establish the real map's live arena density, history or time spent growing.
Its diagnostic checksums use stock-Lua arithmetic, not Warcraft's numeric model.

## Implementation boundary and acceptance

1. Define compiler-owned vector storage operations, matched by annotated declaration
   and structural types before inlining. Keep concrete primitive/handle values in
   their native representation, using the new generic system. Reuse the existing
   backend-specific intrinsic architecture rather than adding name-based rewrites.
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

Start with the vector storage proposal. Existing direct keyed maps already bypass
Jass-style hashing for supported new-generic keys. A migration of legacy generic
containers or a wholesale class/closure representation change is a separate design,
not an automatic extension of this proposal.
