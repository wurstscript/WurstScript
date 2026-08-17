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

The container these bounds were added for now compiles and runs against the standard library on
Jass, and compiles on Lua (#1239). What is left is generality around it rather than the feature
itself, and one gap in what the suite can see.

22. **The library's own tests do not run on Lua.** They run on the interpreter now — all 460 of
    them, collected by importing every package in the checkout whose name ends in `Tests`. That
    half is done; this is the other one.

    `executeTests` runs them through `RunTests` on the intermediate language, so it covers the
    interpreter only. Running them on Lua needs the harness to execute a Wurst test function on
    that target rather than an `init` block, which is new machinery rather than a flag. Worth it:
    the two targets have disagreed before, and every disagreement found so far was found by running
    the same program on both.

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

   Collecting from types on the Lua path runs straight into item 23, so settle that first.

23. **The Lua erasure model.** Decided: **specialise only the paths which need a concrete type and
    leave the object erased throughout.** Generated scripts stay small, which is the reason for the
    choice; the cost is that it is more compiler work than the alternative of not erasing at all.

    What that means in practice. An object keeps coming from the erased class, so it must never need a
    specialised method - the concrete type is threaded to the places which use it rather than to the
    object. Items 6 and 13 both end here: a constructor's dispatch needs the type at the construction
    site, and a subclass's `super` call needs it on the call rather than on the receiver's class.

    Take it seriously rather than working around it. Two class shapes existing at once is what let an
    instance be allocated with no fields at all, and then with its fields under a key nothing read
    (#1239). Both are fixed; the shape which produced them is what this decision removes.

    Unblocks items 6 and 13's Lua half. Not blocking the container, which works on both targets today.

7. **Module bounds.** Decided: the instantiation declares the module's type parameters **only so a
   dispatch receiver has a name to resolve**, and they are excluded from type inference, which keeps
   resolving a generic module's parameters by matching the receiver type as it does today.

   Why that way. A requirement of a bound is called on the parameter itself - `T.show(x)` - and that
   receiver is a name, which the type replacement during expansion never touches. Renaming it to the
   using class's parameter cannot work: `NameResolution.nextScope` sends a `ModuleInstanciation` to
   `attrModuleOrigin()` rather than to the class using it, deliberately, so a module body cannot see
   the names of whoever uses it. The parameter therefore has to be declared where the body can see it.

   Why only for the receiver. Declaring it and letting inference see it collides with the existing
   mechanism: `GenericsModuleTests.genericModuleInGenericClassGet` fails with "Cannot infer type for
   type parameter T". Two mechanisms answering one question is the cost of the alternative; this is
   the smaller change, at the price of the parameter meaning something narrower than it looks.

   Started on `feat/module-instanciation-type-params`, unpushed. The grammar carries `typeParameters`
   and `typeArgs` (resolved, since an argument names something only the user's scope can see),
   resolution binds a declared parameter to its argument through `WurstTypeBoundTypeParam`, and
   `isTypeClassDispatch` accepts a receiver which denotes a parameter through a binding. The error
   chain reached "Could not find function show", which is the requirement lookup not following a
   binding to the underlying parameter's bounds - the same widening, wherever a bound's functions are
   surfaced. Verify before continuing that inference can be told to ignore the declared parameters.

9. **Keep `WURST_LANGUAGE.md` and `CHANGELOG.md` current** as items land — a standing practice
   rather than a task to finish. `WURST_LANGUAGE.md` is tracked, at
   `de.peeeq.wurstscript/src/main/resources/agent-docs/WURST_LANGUAGE.md`, and it already documents
   type class bounds and closure behaviour; it ships as a compiler resource rather than sitting at
   the repository root, which is easy to miss when looking for it. Fold an update into whichever
   item changes the behaviour rather than doing it as a separate pass.

10. **The interpreter still finds a type argument by name.**
    `ProgramState.getCurrentTypeArgument` compares `ImTypeVar` nodes by name, so two parameters which
    merely share one look like the same parameter.

    The two lookups in `EliminateGenerics` no longer do. What they compare is what each node was
    copied from: three places copy a function's or a class's type variables and then empty the copy's
    list, which leaves every reference inside pointing at a node belonging to nothing — the name
    match was not compensating for the odd duplicate, it was the only thing holding those references
    together. Recording the copy before the list is emptied replaces it entirely, and name matching
    is gone from `indexOfTypeVar` and from `inheritTypeClassBinding` through it.

    The record lives on the `ImTranslator`. The interpreter is handed a program rather than the
    translation which produced it, so reaching the record from `ProgramState` means threading the
    translator through the interpreter — four construction sites, three of them tests, but an API
    change all the same. That is what is left of this item.

    Pinned by `TypeClassTests.aBoundedMethodParameterMayShareTheClassParameterName`: a class over int
    whose instance doubles, and a method parameter of the same name bounded and called with string,
    whose instance answers 7. Without the change the string is dispatched through the int instance and
    the interpreter dies casting it to a number, which is what telling the two apart prevents.

    A weaker version of that test - same names, method parameter unbounded - passes either way, so it
    proved nothing. The bound and the second instance are what make the two parameters reach the same
    lookup.

13. **A bounded generic class cannot be subclassed on Lua.** The Jass half landed in #1237: a call
    which names its target outright now carries the class's type arguments, taken from the class its
    first argument is used as, so `super.m()` and `super()` both reach the specialised copy.

    The Lua half is open, pinned by `TypeClassTests.subclassOfBoundedGenericIsStillBrokenOnLua`,
    which asserts the program compiles, runs, and never reaches `testSuccess` rather than leaving
    the difference between the targets to be discovered. `transformGenericNewOnly` runs neither
    `simplifyClasses` nor `addMemberTypeArguments`, so the type variables are never lifted there and
    a super call has nothing to carry. Closing it is item 23.

15. **A dead dispatch slot survives for overloads inside a specialised class.** What is left of the
    junk slot, which is otherwise gone.

    A slot's name is the owner's plus the segment after the last underscore of the method's, and for a
    specialised method that segment is the type argument, so every method of one specialisation
    composes the same name. Both composers now leave such a name uncomposed, deciding by how many
    distinct declared names produce it: a method and its overrides declare one name and must share a
    slot, while siblings declare different ones.

    That leaves overloads. Two overloads of one source method share a declared name, so a specialised
    class holding only overloads still composes one shared name and binds it to whichever is reached
    first. Dead weight as before - nothing calls it - but no longer true of the general case.

    The sharper identity is the dispatch group key, which separates overloads by signature. It cannot
    be used here: the signature embeds each class's type variable, so a generic override chain reads as
    `void|T192,real` against `void|T636,real` and the overrides look unrelated, which drops the slot
    they must share. `LuaTranslationTests.genericOverrideChainBindsRootSlotToMostSpecificImplInLua`
    fails exactly that way, and is how this was found rather than shipped.

    A fix needs an identity which treats a chain's differing type variables as the same signature while
    still separating real parameter differences. Worth doing only if this stops being dead weight.

24. **`luaOutputIsDeterministicForGenericOverrideSlots` failed once and has not since.** It failed
    on Windows CI and passed on a re-run of the same commit, having blocked an unrelated pull request
    in between.

    Do not weaken the assertion. It compiles one repro twice and compares the output byte for byte,
    so a mismatch is evidence of nondeterminism in Lua emission, which is what it exists to catch.

    **Not reproduced.** 250 compiles of that repro in one JVM, caches cleared between each, came out
    byte-identical. Sources of hash-ordered iteration in the emission path were read rather than
    guessed at: `TypeId.calculate` sorts by name and package, `createMethods` groups through a
    `TreeMap`, `assignDispatchAliases` collects into a `TreeSet` and iterates a list, and
    `collectSuperClasses` uses its set only to mark what it has seen. None of those can vary.

    So whatever differs is either rarer than one in 250, or comes from something the local run does
    not vary — a different core count changing the fork layout, memory pressure, or the interpreter
    build on that runner.

    What changed meanwhile is that the failure now carries evidence: both scripts are written beside
    the test output and the first differing lines are named with their numbers. The one occurrence so
    far produced nothing to work from, which is why it cost a re-run and no diagnosis. The next one
    will say what differed.

25. **A bounded type parameter on a method of a generic class is rejected on Lua.**
    `class Holder<T: Show>` with `function convert<Q: Show>(Q other)` fails there with "Generics should
    match class method type variables", while the same program compiles and runs on the other target.
    On master as well, so it is not a regression — found while trying to give item 10's fix Lua
    coverage, which is what it blocks: the only shape reaching that lookup with two parameters at once
    is a class parameter beside a method parameter, and Lua will not compile it.

    Pinned by `TypeClassTests.aBoundedMethodParameterInAGenericClassIsRejectedForLua`. A version with
    the second parameter on a free function does compile on Lua and passes with or without item 10's
    change, so it covers nothing; that is why the rejection is pinned instead.

    Where to start: the message comes from the arity check between a call's generics and the callee's
    type variables. A method of a generic class has the class's variables lifted onto it on the Jass
    path, and `transformGenericNewOnly` does not lift them, so the method's own parameter is counted
    against a list which does not include the class's.

26. **A dispatch slot's name is recovered from another name instead of being asked for, and that is
    where four bugs came from.** The slot name is composed by cutting a method's name at its last
    underscore and taking the tail. That tail is the declared name only when the declared name has no
    underscore in it and the method is not a specialised copy, so:

    - `get_it` contributes `it`, which is nobody's method, and an override named `get_it` in a generic
      hierarchy does not dispatch on Lua. Pinned by
      `LuaTranslationTests.underscoreNamedOverrideInAGenericHierarchyIsStillBrokenOnLua`.
    - a specialised method's tail is the type argument, composing a slot named after a type rather than
      a method - the case `dea459b45` stopped by refusing the composition.
    - an overload numbered by the translation carries the number in the tail, so requiring the tail to
      equal the declared name loses the slot an override of that overload has to replace. Covered by
      `overloadedOverrideOnAGenericBaseIsReachedThroughTheBase`.
    - a type argument named like a numbered overload could in principle collide with a tolerance for
      that number. Not reachable, and covered by `aTypeNamedLikeAnOverloadNumberDoesNotStealTheSlot`.

    Two attempts at the obvious fix both fail, and both failures say what the real one has to be.
    Asking `declaredName` alone collapses overloads, because two overloads share a declared name -
    `overloadedMethodsDoNotAliasInLuaDispatchTables` catches it. Using the method's name whole instead
    of its tail breaks cross-class matching, because at the point slots are composed a method's name is
    still class-prefixed: `GlobalCheckState_update`, where the ancestor's slot is `State_update`. The
    tail is load-bearing precisely because the prefix is there.

    A third attempt gets closest and shows why none of these can work. Stripping the owner's name as a
    known prefix - the boundary is not a guess, the owner is right there to be asked - does fix the
    underscore case, and the pin above flipped to passing, the first time anything has moved it. It
    breaks `genericOverrideChainBindsRootSlotToMostSpecificImplInLua` and
    `genericOverrideChainBindsGlobalStateSlotToMostSpecificImplInLua` instead, because
    `normalizeMethodNames` assigns one name per dispatch group derived from the first member's already
    class-prefixed name and sets it on every member. The prefix a method's name carries is therefore not
    necessarily its own owner's - an ancestor's method can be named after a descendant's class - so no
    prefix known locally is the right anchor. Cutting at the last underscore survives that by accident,
    which is the whole reason it is still here.

    So the segment has to arrive as data, recorded at the one point which knows it:
    `LuaDispatchPreparation.normalizeMethodNames`. That is where a dispatch group is given its name -
    sanitised into a Lua identifier and uniqued against everything already taken - and where the group
    is in hand to strip its own prefix from it. `ImMethod` carries the result in a field beside
    `luaDispatchGroupKey`, so a grammar change and `genAst`, and both composers then read the field
    instead of cutting a string.

    Recording the source declaration and an overload index earlier in translation looks equivalent and
    is not: the assigned name may be derived from a different member of the group, and sanitising and
    uniquing can change it. A declaration-derived pair would have to be matched back to it, which is the
    same recovery problem again under a new name. The authoritative segment is the one
    `normalizeMethodNames` produced, so that is the one to keep.

    The rest of the family, for the same treatment once this exists:
    `ProgramState.identifyGenericStaticGlobals` takes the longest prefix of a global's name ending at an
    underscore which matches a class name, which a class whose name contains an underscore answers
    wrongly and silently. #1249 made the recorded owner preferred where one exists, so this is now only
    the fallback.


12. **Standing item, never finished.** When nothing above is left, find the next thing worth
    doing and add it here rather than stopping. Good sources, in order: a test that would have
    caught a bug already found; a place where two mechanisms do the same job and disagree; a
    comment claiming something the code no longer does; a path where a wrong result is silent
    rather than loud. Add what is found as a numbered item and start on it.

## Blocked on a decision

- **8. Settled: `div` and `mod` keep returning the left operand's type**, so `real r = 7 div 2`
  compiles and is meant to. The branch in `AttrExprType` now says so, rather than looking like an
  oversight next to `caseMathOperation`, which collapses two literals to int precisely so
  `real r = 1 + 1` is an error. `ExpressionTests.integerDivisionOfLiteralsIsStillAssignableToReal`
  pins it and `OptimizerTests.realFormatting_consistent_fromIntOps` depends on it. Nothing to do.

- **Eliminating the remaining `castTo int`.** The motivating case is timer data attachment
  (`ClosureTimers.wurst`), and the containers behind it: `Table` has 81 casts, `HashList` 13,
  `HashSet` 6, `HashMap` 4. None can adopt bounds as things stand, because an instance is
  declared one type at a time and these accept any type. It needs a way to give an instance
  for a whole family — every class type, or every handle type — which is a language design
  question: what the syntax is, where such an instance may be declared under the orphan rule,
  and whether a specific instance always beats a family one. Do not start this autonomously.

  Deferred deliberately, not forgotten. It decides whether type class bounds stay a tool for new
  containers or become how the existing ones work, which is worth deciding when there is appetite for
  the language design rather than alongside compiler work.

## Out of scope

- The stdlib itself. `de.peeeq.wurstscript/temp/WurstStdlib2` is a fetched artefact for tests;
  editing it changes nothing real. `FastHashMap` ships from the WurstStdlib2 repo once the
  compiler-side proof is complete, and that is a separate decision.

## Done

- 21. A standard library program executes on Lua (#1242). Three packages could not initialise, each
  on one native the shim did not define — `StringHash` for Colors, `Location` for Vectors,
  `TimerStart` for GameTimer — and `StringCase` made a fourth once the program itself ran. The
  larger half was the harness: success is read off stdout and the library's own `testSuccess` is
  empty, so such a test could not have passed whatever it did.

- 22 (interpreter half). The library's 460 test functions run. They were invisible because a library
  compiles in only what is imported, so a program importing nothing runs none of them and passes;
  the imports are collected from the checkout so a bump brings its new tests with it. `executeTests`
  now reports how many ran and `expectAtLeastTests` fails when too few do, because "every test
  passed" and "there were no tests" were the same green — which is how the first version of this
  test looked right while running nothing.

- 5, 11, 14. The container these bounds were added for works. `FastHashMapTests` runs a whole hash
  map — two type parameters with only the first bounded, static arrays of a bounded parameter, a
  bound reached from a private method, linear probing calling both requirements, tombstoned removal,
  tuple and class keys, two specialisations at once — on both targets, and against the standard
  library on Jass (#1239). Three bugs were found by compiling it with the library in scope rather
  than alone: a specialised class allocated no fields at all, then kept them under a key nothing
  read, then could be renamed into a method slot. Each is now pinned by a test.

- 13 (Jass half). A call which names its target outright carries the class's type arguments,
  taken from the class its first argument is used as, so `super.m()` and `super()` reach the
  specialised copy (#1237). The note here claiming this could not work — that the callee had no
  type variables of its own — was wrong: `moveFunctionsOutOfClass` lifts the class's onto it. The
  first attempt failed because nothing recorded which class a function had been moved out of.

- Strings are bytes in the interpreter, as they are in the game and in Lua (#1238), which unblocked
  the pinned library bump (#1240). `StringCase` folds only ascii, since the bytes of a multibyte
  character are not letters, and `StringHash` is computed over the bytes because the library's
  version encodes text itself and cannot hash half a character. A compiletime expression returning
  half a character is refused rather than carried across at a different length: the script is
  written as UTF-8 and neither Jass nor the escaping can write a byte down numerically.

- 19. Tried giving Jass the dangling-reference check Lua has, and reverted it. The two backends do
  not agree on which functions exist: `LuaTranslator` requires every reference to be rooted in the
  program, while `ImToJassTranslator` is handed `getCalledFunctions()` and emits whatever is
  called, rooted or not. Three passing tests rely on that — a closure's `construct_Lazy` is
  detached from the program and still called — so the invariant is Lua's rather than universal.
  Worth knowing when reading a Jass error: a function a pass detached is still translated, so the
  error names what was inside it rather than the reference that kept it alive, which is exactly how
  item 13 shows up. (Requiring natives to be rooted also fails: `$debugPrint` is built with
  `IS_NATIVE, IS_BJ` and deliberately never added.)
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

- Fork count is worth measuring rather than reasoning about, because two effects pull against each
  other: more forks means more parallelism but also more contention, and every test gets slower.
  Measured on eight cores, whole suite, wall clock against total reported test time:
  serial 13m11s / 786s; four forks 8m39s / 1230s; eight forks 7m03s / 2048s. Eight wins even
  though each test runs 2.6 times slower there than alone. Sixteen was not tried; the limit by
  then is the slowest single class, not the scheduling.
- Where the suite's 13 minutes went, measured from `build/test-results/test/TEST-*.xml`: 786s of
  test time across 79 classes and 1663 tests, so effectively all of it is the tests themselves
  rather than the build. The top ten classes are 61% of it, led by `ExportToWurstTest` at 108s,
  then `OptimizerTests` 67s, `BugTests` 66s, `RealWorldExamples` 54s,
  `GenericsWithTypeclassesTests` 45s. Gradle hands whole classes to forks, so wall time cannot go
  below the slowest class — 108s is the floor until `ExportToWurstTest` is split.
  Below that floor the remaining costs, in the order worth attacking: 401 tests compile their
  program five times (each Jass configuration) and spawn `pjass.exe` per configuration, which
  re-parses `common.j` and `blizzard.j` every time; and 102 `withStdLib` sites re-parse 169 stdlib
  files because `GlobalCaches.clearAll()` runs before and after every method. Both change what the
  tests actually verify, so neither is a free win the way the scheduling was.

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
