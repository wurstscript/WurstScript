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

15. **One junk dispatch slot per specialised class.** `addDirectAliases` and
    `LuaTranslator.collectDispatchSlotNames` both compose `owner.getName() + "_" +
    semanticNameFromMethodName(name)`, and for a specialised method that trailing segment is the
    type argument — so every method of `FastHashMap<int, int>` claims one shared
    `FastHashMap_specialized_integer__integer_integer` slot and the alphabetically first wins it.
    Nothing calls it, so it is dead weight rather than a wrong result.

    Tried using the declared name instead and reverted it: overloads share a declared name, so
    `setup(int)` and `setup(string)` collapse into one slot, which is what
    `LuaTranslationTests.overloadedMethodsDoNotAliasInLuaDispatchTables` and
    `moduleProvidedOverloadedOverrideDoesNotCollapseLuaSlots` exist to prevent. Both sources of a
    semantic name are wrong, in opposite directions: the mangled trailing segment collides across
    the siblings of one specialisation, the declared name collides across overloads. A fix needs a
    name that separates both — the declared name together with the dispatch signature key would,
    since that is already what distinguishes overloads elsewhere in the same file. Worth doing only
    if this stops being dead weight, because the cost of getting it wrong is a real mis-binding
    while the cost of leaving it is one unused table key per specialised class.

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

   What Jass does, from `TypeClassTests_dispatchInsideConstructor_no_opts.jim`: it specialises the
   constructor function itself, `b_8 = new_Box⟪integer⟫(21)`. It gets there from *types*, not from
   the call — `collectGenericUsages` collects a `GenericVar` for the local declared
   `Box<integer{show}>` and a `GenericReturnTypeFunc` for `new_Box`, whose return type is generic.
   The Lua collector has neither; it only ever looks at calls. So attaching type arguments to
   constructor calls, which an earlier note here proposed, is not what the Jass path relies on and
   would be a second mechanism rather than the same one.

   The honest next step is to collect from types on the Lua path too, restricted the way item 5's
   collection is. That runs straight into the same design question, though: `GenericVar` and
   `GenericReturnTypeFunc` specialise the *class*, and item 5 showed that an object coming from a
   specialised class while its methods are bound to the erased one breaks everything. Either the
   collection has to specialise only the constructor path and leave the object erased, or Lua stops
   erasing constructed generic classes — which is a decision about the erasure model, not a patch.

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

- 11. Generated Jass can be compared across runs. The counters naming temporaries were per thread
  and never reset, so a name depended on how much had been compiled before it: the same source gave
  `temp0` alone and `temp70` after other tests, measured directly rather than assumed. They now
  start from zero at the top of each compilation — not at each flattening, which happens again
  after every optimisation and would name two locals of one function alike.
  `DeterministicChecks.temporaryNamesDoNotDependOnEarlierCompilations` compiles the same source,
  then other programs, then the same source again; the compilation in between is the part that
  matters, and it is the inlining configuration that emits a temporary for that source.
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
- Emitted Lua must be byte-identical for identical input (AGENTS.md §8). Emitted Jass can be
  diffed across runs too now, since item 11 — `LOOP.md` still says otherwise.
- Two of this run's reverts were the same mistake: a name that looks redundant is usually carrying
  a distinction. The mangled method name separates overloads; `leftType` on `div` keeps a literal
  assignable to a real. Check what a name distinguishes before replacing it with a tidier one.
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
