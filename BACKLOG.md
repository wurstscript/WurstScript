# Language feature backlog

Working notes for ongoing work on type class bounds for `T:` generics
(shipped in #1226, #1228, #1229). Ordered: take the top unblocked item.

Keep this file current. It is the only memory that survives between sessions.
When an item is finished, move it to Done with one line on what actually
happened. When something is learned that would have saved time, write it under
Notes rather than leaving it in a commit message.

## Todo

1. **Lua method names reach the backend unsanitised.** `LuaTranslator.luaMethod.initFor`
   passes `a.getName()` raw; `luaVar`, `luaFunc` and `luaClassVar` all sanitise via
   `uniqueName`. Method names become Lua table keys, so they must be valid identifiers.
   `EliminateGenerics.specializeMethod` builds `name + "_specialized_" + generics.makeName()`
   on the Lua path, and `makeName` joins type arguments with `", "` — so a class method
   specialised with two type arguments emits `Class.get_specialized_integer, integer = impl`,
   which is valid Lua assigning to two targets, and a tuple argument emits `⦅⦆` and fails
   the syntax check. Repro: `FastHashMapTests.tupleKeyLua`. Commas are already visible in
   `test-output/lua/FastHashMapTests_fastHashMapRuntimeLua.lua`.
   Overriding methods must keep landing in the same slot, so normalise per distinct original
   name, not per method node.

2. **`slotFor` looks bound to `get`'s implementation** in the same emitted Lua. May be a real
   mis-binding in `specializeMethod`/`adaptSubmethods`, may be an artefact of item 1 mangling
   the output. Diagnose only after item 1, from freshly emitted Lua.

3. **Finish the FastHashMap proof.** `FastHashMapTests` is the first real use of bounds.
   Add `remove` with tombstones, and an assertion that the emitted code stays cheap: no
   dispatch node, no instance dictionary, and no WC3 hashtable natives — array access only,
   which is the whole point versus `HashMap extends Table`.

4. **Lua dispatch inside a closure.** Works on Jass since #1229. On Lua the specialised class
   is built correctly but nothing calls it, because the closure is reached through its
   interface and `specializeMethod` renames the method out of its dispatch slot.
   `TypeClassTests.dispatchInsideClosureIsRejectedForLua` pins the current diagnostic and
   should become a success test. Related to item 1; AGENTS.md flags this machinery.

5. **Lua dispatch inside the constructor** of a bounded generic class. Works on Jass.

6. **Module bounds.** `module M<T: Show>` is rejected with a clear message today. Needs
   receiver rewriting during expansion, or type parameters on `ModuleInstanciation`.

7. **One `ImTypeVar` per type parameter.** Name-tolerant lookups remain in
   `EliminateGenerics.indexOfTypeVar`, `inheritTypeClassBinding` and
   `ProgramState.getCurrentTypeArgument`, compensating for several nodes standing for one
   source parameter. Making the node canonical lets all three compare by identity and removes
   a class of silent wrong dispatch. Mechanical, well covered by the suite.

8. **Jass temp counter is not reset between compilations.** Two runs of the same commit emit
   different `.j` (`temp151` vs `temp8`) because the counter is JVM-wide and depends on how
   many tests ran before. Not wrong for compiling one map, but it means `.j` cannot be diffed
   across runs to validate a change — only `.lua` can. Fixing it would make Jass diffable.

## Done

- Substitution now carries the type class binding with the type (#1229). Also fixed the
  type-variable reference on `ImTypeVarDispatch`, which a walk over types alone missed.

## Notes

- `%` is real modulo in Wurst; `mod` is integer modulo. `int % 8` types as `real`.
- Emitted Lua must be byte-identical for identical input (AGENTS.md §8). It is the only
  emitted output that can be diffed across runs — see item 8.
- Tests run five Jass configurations plus the interpreter, then the Lua target separately.
  `testAssertOkLines(true, ...)` covers both the pre-transform interpreter and full
  monomorphisation, so it is a stronger check than it looks.
- The stdlib copy under `de.peeeq.wurstscript/temp/WurstStdlib2` is a fetched artefact for
  tests. Real stdlib changes belong in the WurstStdlib2 repo, not here.
