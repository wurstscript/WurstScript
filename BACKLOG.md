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

15. **One junk dispatch slot per specialised class.** Left over from item 3, same heuristic in
    the other place it is used. `addDirectAliases` composes `owner.getName() + "_" +
    semanticNameFromMethodName(name)`, and for a specialised method that trailing segment is the
    type argument, so every method of `FastHashMap<int, int>` claims the same
    `FastHashMap_specialized_integer__integer_integer` slot and the alphabetically first wins.
    Nothing calls it, so it is dead weight rather than a wrong result — but it is the same
    mistake, and the alias it *should* produce is the class qualified with the declared name.
    Fixing it changes emitted slot names, so it wants its own commit and its own suite run.

6. **Lua dispatch inside the constructor** of a bounded generic class. Works on Jass; there is now
   a repro for both targets, `TypeClassTests.dispatchInsideConstructor` and
   `dispatchInsideConstructorIsRejectedForLua`, the second pinning the current diagnostic.

   Not the same gap as item 5, and the fix from it does not reach: a constructor belongs to the
   class rather than to a generic function of its own, so the call that runs it carries no type
   arguments at all. The intermediate language has `b = new_Box(21)` with `b` typed
   `Box<integer{show}>`, and `new_Box` still generic; the calls *inside* it
   (`construct_Box<T>`, `Box_init<T>`) do carry the class's type variable, but nothing gives the
   outermost one a concrete argument. `collectGenericNewUse` requires non-empty type arguments, so
   it never starts.

   The instantiation is only on the type of what the call is assigned to. Three ways to get at it,
   roughly in order of how much they would disturb: attach the class's type arguments to
   constructor calls when the intermediate language is built, which is where the frontend still
   knows them and would serve both targets uniformly — but it changes the Jass path, which reaches
   the same answer another way today, so the emitted `.j` needs checking; read them from the
   assignment target on the Lua path, which is a syntactic shape and would miss
   `foo(new Box<int>(21))`; or specialise from the `#alloc` inside the constructor, which is the
   item 5 mechanism but would have to reach back out to the caller. The first looks right; confirm
   it is what the Jass path already relies on before changing it.

7. **Module bounds.** `module M<T: Show>` is rejected with a clear message today. Needs
   receiver rewriting during expansion, or type parameters on `ModuleInstanciation`.

9. **Keep `WURST_LANGUAGE.md` and `CHANGELOG.md` current** as items land — a standing practice
   rather than a task to finish. Fold it into whichever item changes the behaviour rather than
   doing it as a separate pass. Both now cover closures on either target, which is what this item
   originally pointed at.

10. **One `ImTypeVar` per type parameter.** Name-tolerant lookups remain in
   `EliminateGenerics.indexOfTypeVar`, `inheritTypeClassBinding` and
   `ProgramState.getCurrentTypeArgument`, compensating for several nodes standing for one
   source parameter. Making the node canonical lets all three compare by identity and removes
   a class of silent wrong dispatch. Mechanical, well covered by the suite.

11. **Jass temp counter is not reset between compilations.** Two runs of the same commit emit
    different `.j` (`temp151` vs `temp8`) because the counter is JVM-wide and depends on how
    many tests ran before. Not wrong for compiling one map, but it means `.j` cannot be diffed
    across runs to validate a change — only `.lua` can. Fixing it would make Jass diffable.

13. **A bounded generic class cannot be subclassed.** Found while building a repro for item 3;
    both backends break, differently, on the same program:

        interface Show<T:>
            function show(T x) returns int
        implements Show<int>
            function show(int x) returns int
                return x
        class Box<K: Show>
            K key
            construct(K k)
                key = k
            function size(int extra) returns int
                return K.show(key) + extra
            function shift(int extra) returns int
                return 1000 + extra
        class SubBox extends Box<int>
            construct(int k)
                super(k)
            override function size(int extra) returns int
                return super.size(extra) + 100
        init
            Box<int> b = new Box<int>(5)
            Box<int> s = new SubBox(5)
            if b.size(1) == 6 and b.shift(1) == 1001 and s.size(1) == 106
                testSuccess()

    Jass fails to compile: `Typevar dispatch not eliminated.` Lua compiles and runs but never
    reaches `testSuccess`: the override makes `size` dispatched, and the emitted call is
    `b:Box_size_specialized_integer(1)` while `b` was allocated from the *erased* `Box` table,
    which binds only `shift`. The specialised table `Box_specialized_integer` has the slot; the
    instance never gets that table. Split this if the two turn out to have separate causes —
    the Jass one is loud and probably the smaller of the two.

12. **Standing item, never finished.** When nothing above is left, find the next thing worth
    doing and add it here rather than stopping. Good sources, in order: a test that would have
    caught a bug already found; a place where two mechanisms do the same job and disagree; a
    comment claiming something the code no longer does; a path where a wrong result is silent
    rather than loud. Add what is found as a numbered item and start on it.

## Blocked on a decision

- **8. Should `div` and `mod` keep returning the left operand's type?** Tried returning
  `WurstTypeInt.instance()` to match `caseMathOperation` and reverted it: it is a user-visible
  breaking change, and the suite already defines the current behaviour as correct.

  The asymmetry is real and reachable. `WurstTypeIntLiteral` is a proper subtype of both int and
  real, and `caseMathOperation` collapses two literals to int precisely so `real r = 1 + 1` is an
  error. `div`/`mod` return `leftType`, so `real r = 7 div 2` compiles. Changing that made exactly
  one test fail — `OptimizerTests.realFormatting_consistent_fromIntOps`, which opens with
  `real a = 1 div 2` — and AGENTS.md says the existing suite is the authoritative definition of
  behaviour. Real maps will contain the same shape.

  So the question is the owner's: is `real r = 7 div 2` meant to compile? If yes, the branch in
  `AttrExprType` wants a comment saying so, and this item closes. If no, it is a deliberate
  breaking change that needs the changelog, and `realFormatting_consistent_fromIntOps` needs
  rewriting to say what it actually tests, which is real formatting rather than that assignment.
  `ExpressionTests.integerDivisionOfLiteralsIsStillAssignableToReal` pins the behaviour meanwhile,
  so whichever way it goes is deliberate rather than accidental.

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

- 18. Comparing an unresolved type parameter default is now an error rather than a quiet "not
  equal". Item 16 closed the path that reached a program, but the stand-in is produced by a static
  attribute and could surface anywhere, so the silence was the part worth removing. The whole suite
  is green with it throwing, which says nothing reachable produces one any more — and if something
  starts to, it says so instead of returning a wrong answer.
- 16. A never-written slot of a `T array` reads as the default of what T stands for. The default
  is computed by a static attribute, which cannot see the frames that know the type argument, so
  it produced a stand-in that compares equal only to another stand-in — `Box<int>.first() == 0`
  was quietly false on the interpreter while both backends had it right. `ProgramState` does know
  the substitution, so the stand-in is now resolved where the value is produced, at the array read
  and the member read, rather than at the comparison where the symptom shows. Item 18 covers the
  paths that could still leak one.
- 17. A failing Lua test says so. `translateAndTestLua` now sets the environment label instead of
  reporting under whatever Jass configuration ran last.
- 5 (+ the part of 9 that follows it). A type class bound now dispatches from inside a closure on
  Lua, and `TypeClassTests.dispatchInsideClosureLua` is a success test. The note in this file was
  wrong about the cause: no specialised class was being built at all. Lua specialisation is driven
  by calls that carry type arguments, and a closure has none — it is reached through the interface
  it implements, which is not generic, so only the construction knows the instantiation. Three
  pieces were missing, all present already for Jass: collect the instantiation from `ImAlloc`,
  collect the member access so the capture write lands on the specialised field, and bind the
  specialised methods to the roots the originals were submethods of (registering the original
  implementation as specialised so `settleRemainingDispatches` neutralises what it leaves behind).
  All three are gated on the class being closure-generated. Widening them to any constructed class
  made the two mechanisms disagree — the object came from the specialised class while its methods
  were bound to the erased one — and broke every FastHashMap Lua test, which is the shape of
  regression AGENTS.md §9 warns about. `WURST_LANGUAGE.md` and `CHANGELOG.md` say so now.
- 4. The FastHashMap proof is complete. `remove` leaves a tombstone, which `slotFor` passes over
  when searching and reuses when putting; the probe is bounded by capacity rather than running
  until it finds a gap, so a table full of tombstones cannot spin. `emittedCodeCostsNothingExtra`
  asserts the cost claim on the *least* optimised configuration, because it has to hold by
  construction rather than by inlining: storage is four plain Jass arrays, no WC3 hashtable native
  is reached for, `slotFor` takes nothing beyond the receiver and the key — no instance is threaded
  through at runtime — and both requirements are direct calls, not `ExecuteFunc`, not a dispatch
  wrapper. In the optimised output `hash(key)` becomes `key` and `equals(a, key)` becomes `a != key`.
  The `dispatch_` functions that remain are the nullpointer check every Wurst class method gets,
  not type class dispatch; the test is careful to look at the real function under that wrapper.
- 3. `slotFor`'s slot no longer holds `get`'s implementation. The alias sets in
  `LuaDispatchPreparation` decide which slots a method claims, and `sharesSemanticName` accepted
  a match on either the declared name or `semanticNameFromMethodName` — the substring after the
  last underscore, which for a specialised method is a fragment of the type argument. Both
  `FastHashMap_get_specialized_integer__integer` and the `slotFor` one end in `integer`, and the
  two share a dispatch signature, so `get` claimed `slotFor`'s slot. Declared names now settle it
  whenever both methods have one; the substring is a fallback for closures and bridges, which
  have no declaration to ask. `FastHashMapTests.fastHashMapRuntimeLua` asserts every slot named
  after a method binds that method's implementation — it fails on the old code with exactly the
  binding above.
- 14. The Lua execution harness no longer hangs. It read the spawned interpreter's stderr to EOF
  before touching stdout, and never bounded the wait, so a program that filled the stdout pipe
  deadlocked the whole suite with no output and no timeout — a stray JVM was still sitting on it
  twenty minutes later. `checkLuaSyntax`, ten lines further down the same file, already drained
  both pipes concurrently and waited with a timeout; the execution path now uses the same
  helper. Verified against the item 13 repro: hung indefinitely before, fails in 8s after.
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
- The suite is the specification. Before changing what the type checker accepts, grep the tests for
  the shape being rejected — item 8 looked like an oversight until one optimizer test turned out to
  depend on it.
- A test that hangs looks exactly like a test that is slow. If the suite stops making progress,
  take a thread dump of the forked worker (`jstack <pid>`) before killing it — it names the line.
- Method names are not what the frontend called them. `LuaDispatchPreparation` renames a whole
  dispatch group to one name and attaches alias sets, and only then does the backend run. A
  question about which Lua slot something lands in is a question about that pass, not about
  `LuaTranslator`.
- Tests run five Jass configurations plus the interpreter, then the Lua target separately.
  `testAssertOkLines(true, ...)` covers both the pre-transform interpreter and full
  monomorphisation, so it is a stronger check than it looks.
- The stdlib copy under `de.peeeq.wurstscript/temp/WurstStdlib2` is a fetched artefact for
  tests. Real stdlib changes belong in the WurstStdlib2 repo, not here.
