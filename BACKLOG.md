# Language feature backlog

Working notes for ongoing work on type class bounds for `T:` generics
(shipped in #1226, #1228, #1229). Ordered: take the top unblocked item.

Keep this file current. It is the only memory that survives between sessions.
When an item is finished, move it to Done with one line on what actually
happened. When something is learned that would have saved time, write it under
Notes rather than leaving it in a commit message.

## Todo

Numbering is stable: finished items leave a gap rather than shifting the ones below,
because `LOOP.md` refers to items by number.

3. **`slotFor` is bound to `get`'s implementation** in the emitted Lua — confirmed real, and
   not an artefact of item 1: it survives sanitisation unchanged. Diagnosed, not yet fixed.
   The alias sets in `LuaDispatchPreparation` decide which slots a method claims, and
   `sharesSemanticName` accepts a match on *either* of two names: the source name (`get`,
   `slotFor`) or `semanticNameFromMethodName`, which is the substring after the last
   underscore. For a specialised method that substring is a type-argument fragment —
   `FastHashMap_get_specialized__integer__integer___integer` and the `slotFor` one both yield
   `integer` — so two unrelated methods count as sharing a name. They also share a dispatch
   signature here (`(pos) returns int` both), which is the other half of the guard, so `get`
   claims `slotFor`'s slot. Fix: when both methods have a real source name, that should decide;
   the substring heuristic is a fallback for when there is no trace to ask, not an alternative.
   Watch the closure and bridge cases in `TypeClassTests`/`LuaBackendAuditTests` — they are what
   the loose match was presumably widened for.

4. **Finish the FastHashMap proof.** `FastHashMapTests` is the first real use of bounds.
   Add `remove` with tombstones, and an assertion that the emitted code stays cheap: no
   dispatch node, no instance dictionary, and no WC3 hashtable natives — array access only,
   which is the whole point versus `HashMap extends Table`.

5. **Lua dispatch inside a closure.** Works on Jass since #1229. On Lua the specialised class
   is built correctly but nothing calls it, because the closure is reached through its
   interface and `specializeMethod` renames the method out of its dispatch slot.
   `TypeClassTests.dispatchInsideClosureIsRejectedForLua` pins the current diagnostic and
   should become a success test. Related to item 1; AGENTS.md flags this machinery.

6. **Lua dispatch inside the constructor** of a bounded generic class. Works on Jass.

7. **Module bounds.** `module M<T: Show>` is rejected with a clear message today. Needs
   receiver rewriting during expansion, or type parameters on `ModuleInstanciation`.

8. **`MOD_INT`/`DIV_INT` return the left operand's type** rather than `int`
   (`AttrExprType.java`, the `case MOD_INT` branch), where `caseMathOperation` returns
   `WurstTypeInt.instance()` for `+`, `-`, `*`. Only observable if something is a proper
   subtype of int, so it may be harmless — establish whether it is reachable, then either fix
   it or leave a comment saying why the asymmetry is intended. Small.

9. **Keep `WURST_LANGUAGE.md` and `CHANGELOG.md` current** as items land. The bounds section
   says nothing about closures, which now work on Jass. Fold this into whichever item changes
   the behaviour rather than doing it as a separate pass.

10. **One `ImTypeVar` per type parameter.** Name-tolerant lookups remain in
   `EliminateGenerics.indexOfTypeVar`, `inheritTypeClassBinding` and
   `ProgramState.getCurrentTypeArgument`, compensating for several nodes standing for one
   source parameter. Making the node canonical lets all three compare by identity and removes
   a class of silent wrong dispatch. Mechanical, well covered by the suite.

11. **Jass temp counter is not reset between compilations.** Two runs of the same commit emit
    different `.j` (`temp151` vs `temp8`) because the counter is JVM-wide and depends on how
    many tests ran before. Not wrong for compiling one map, but it means `.j` cannot be diffed
    across runs to validate a change — only `.lua` can. Fixing it would make Jass diffable.

12. **Standing item, never finished.** When nothing above is left, find the next thing worth
    doing and add it here rather than stopping. Good sources, in order: a test that would have
    caught a bug already found; a place where two mechanisms do the same job and disagree; a
    comment claiming something the code no longer does; a path where a wrong result is silent
    rather than loud. Add what is found as a numbered item and start on it.

## Blocked on a decision

- **Eliminating the remaining `castTo int`.** The motivating case is timer data attachment
  (`ClosureTimers.wurst`), and the containers behind it: `Table` has 81 casts, `HashList` 13,
  `HashSet` 6, `HashMap` 4. None can adopt bounds as things stand, because an instance is
  declared one type at a time and these accept any type. It needs a way to give an instance
  for a whole family — every class type, or every handle type — which is a language design
  question: what the syntax is, where such an instance may be declared under the orphan rule,
  and whether a specific instance always beats a family one. Do not start this autonomously.

## Out of scope

- The stdlib itself. `de.peeeq.wurstscript/temp/WurstStdlib2` is a fetched artefact for tests;
  editing it changes nothing real. `FastHashMap` ships from the WurstStdlib2 repo once the
  compiler-side proof is complete, and that is a separate decision.

## Done

- 1 + 2. Lua method names are sanitised where they are assigned, not where they are printed.
  `LuaDispatchPreparation.normalizeMethodNames` is the pass that gives one name to a whole
  dispatch group, so it now sanitises before uniquing — two names differing only in characters
  Lua has no place for still get a slot each. `LuaTranslator` maps every slot key and every
  `LuaMethod` name through the same function, so call sites and class tables agree. Lua's
  identifier rule now lives in one place, `LuaIdentifiers`. `LuaAssertions.assertNamesAreValidIdentifiers`
  walks the emitted Lua and fails on any name that is not an identifier; it runs for every
  `testLua` compile, so the silent two-target-assignment case cannot come back.
- Substitution now carries the type class binding with the type (#1229). Also fixed the
  type-variable reference on `ImTypeVarDispatch`, which a walk over types alone missed.

## Notes

- `%` is real modulo in Wurst; `mod` is integer modulo. `int % 8` types as `real`.
- Emitted Lua must be byte-identical for identical input (AGENTS.md §8). It is the only
  emitted output that can be diffed across runs — see item 11.
- Method names are not what the frontend called them. `LuaDispatchPreparation` renames a whole
  dispatch group to one name and attaches alias sets, and only then does the backend run. A
  question about which Lua slot something lands in is a question about that pass, not about
  `LuaTranslator`.
- Tests run five Jass configurations plus the interpreter, then the Lua target separately.
  `testAssertOkLines(true, ...)` covers both the pre-transform interpreter and full
  monomorphisation, so it is a stronger check than it looks.
- The stdlib copy under `de.peeeq.wurstscript/temp/WurstStdlib2` is a fetched artefact for
  tests. Real stdlib changes belong in the WurstStdlib2 repo, not here.
