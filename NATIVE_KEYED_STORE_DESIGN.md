# Native keyed store design

This note specifies a future `RawHashMap<K, V>` which uses Lua's native table for keys and keeps a
behaviorally equivalent Jass fallback. It records the design and acceptance criteria; the collection
is not implemented by this change.

## Selection and key contract

Keep the existing `FastHashMap` probing implementation for structural equality and unsupported
types. Add a separately named `RawHashMap<K: Hashable and RawKeyed, V:>`; Wurst does not select two
same-named class definitions by generic bounds. The `RawKeyed<T:>` marker is a promise that the type's
equality matches Lua raw-key equality: primitive keys compare by value and reference keys compare by
identity. It must not admit structural keys such as `vec2`.

For the first version, support `int`, `boolean`, and non-null reference keys. Exclude `real` until the
map defines matching behavior for NaN, which Lua rejects as a table key. Exclude strings because
Wurst's Lua representation conflates null strings with empty strings while Jass preserves the
difference. `RawKeyed` cannot exclude null from a reference type, so define null-key operations
explicitly and identically on both targets: `put` and `remove` are no-ops, `has` is false, and `get`
returns the missing-key default. Specialize the null check only for reference keys so valid `0` and
`false` primitive keys are not rejected.

The Jass probing fallback also needs a stable hash while a key is stored. `RawKeyed` therefore
promises both raw-key equality and a hash that does not change during that period. Users must remove a
key before changing state used by its hash. Add a cross-backend regression with a mutable key payload
and a stable hash field: mutate the payload while the key is stored, verify lookup/removal still work,
then remove the key before changing its hash field. Reference keys and values must likewise be removed
or replaced before they are destroyed, since Jass can reuse a destroyed object's integer identity.

## Values, presence, and lowering

Support `int`, `real`, `boolean`, and nullable class/handle references as values. Exclude strings until
null and empty string have distinct representations on both targets, and reject aggregate values until
they have an explicit null/default representation. `put(key, null)` removes a reference value; a
present-null entry is not part of the contract. Preserve the reference specialization through lowering
so this also works when null comes through a nullable local.

Use a separate presence operation. The existing `keyedMapHas` Lua lowering distinguishes an absent
entry from stored `0` or `false`; `RawHashMap.has` must preserve the same contract, using the probing
occupancy check on Jass. Do not infer presence from a typed getter. Typed getters supply `0`, `0.0`,
or `false` for missing primitive values and `nil` for missing nullable references. Keep values
unboxed. The specialized Lua `put`, `get`, `has`, and `remove` paths must use the key itself as the
table key and compile to direct table operations, with one table lookup per read and no handle-to-int
conversion or `GetHandleId` round trip.

Use a distinct backing store per map instance. On Jass, create and destroy/flush its `Table`; on Lua,
create a fresh table and clear it in place on destroy, since class destruction does not make aliases
unreachable. Test teardown separately by target. On Lua, retain an alias and verify `has(alias, key)`
is false after destroy. On Jass, destroy the first map, create a second map (which may reuse IDs), and
verify the new map is empty; never read through the stale first-map alias after destroy, because that
access is unsupported and may reach the second map after ID reuse.

The Jass probing path is for behavioral parity, not a speed claim over Warcraft's native `Table`.
Lua target selection alone does not expose concrete `K` and `V`; keyed operations must seed the
existing targeted generic specialization (or run after equivalent concrete-type propagation) so the
compiler can select typed getters and reference-null behavior. Intrinsic calls remain compile-time
lowering and must not introduce a runtime strategy branch.

Compiler PR #1318 is merged into `master` and supplies the narrower new-generic handle-keyed
intrinsics, including presence and per-instance create/destroy lowering. `RawHashMap` can reuse that
groundwork where its signatures apply, but still needs the broader key/value specializations and Jass
probing behavior described here.

## Acceptance tests

- Compile and run Jass and Lua cases for primitive keys `0` and `false`, missing primitive defaults,
  nullable values, null-key behavior, and `put(key, null)` removal.
- Check that a stored `0` or `false` still makes `has(key)` true, and that removing it makes `has`
  false, on both backends.
- Cover identity-key behavior, stable hashing while stored, and removal before key/value destruction.
  For teardown, use the Lua retained-alias check and the Jass create/destroy/create check described
  above; never assert through a stale Jass map alias.
- Inspect emitted Lua for direct key table operations, typed unboxed values, and no key conversion.
  Verify specialized `put`/`get` calls reach the intrinsic lowering; keep the Jass fallback covered.
- Keep structural `FastHashMap` keys on the existing probing implementation.
