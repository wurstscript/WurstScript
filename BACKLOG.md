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

21. **Nothing executes a standard library program on Lua.** Every test which puts the library on
    that target compiles only, because the runtime shim cannot initialise the library's own
    packages — `GameTimer` fails first, and the generated fallback for an undefined native raises
    rather than returning. So the one thing a user actually does, running library code on Lua, is
    the one thing never run here.

    This is why `fastHashMapAgainstTheStandardLibraryLua` asserts on emitted shape instead of on a
    result. It is also how the empty-allocation bug in #1239 survived as long as it did: the paths
    which would have caught it are compiled and never executed.

    Worth finding out how far it is. If it is a handful of missing natives in `wc3shim.lua`, the
    payoff is every existing library test on that target becoming a real one. Start by capturing
    what `GameTimer` actually fails on rather than the message Wurst wraps it in.

22. **A bump of the pinned library is only checked for compiling.** Nothing runs the library's own
    test functions, so a behaviour change in it is invisible here. `StdLibStringTests` (#1240)
    covers the string handling one bump turned on, which is a start rather than a solution: the
    library's multibyte detection degrades quietly to an ascii-only path when it cannot find what
    it probes for, so a version where it silently gave up would otherwise look exactly like one
    where it worked. Running the library's own tests generalises this, and its Lua half depends on
    item 21.

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

23. **The Lua erasure model, which items 6 and 13 both end at.** On Lua a generic class is erased,
    and specialised copies are made only where a construction names the instantiation. Every
    remaining gap on that target is one question: an object allocated from a specialised class while
    its methods are bound to the erased one breaks, and an object allocated from the erased class
    cannot reach a specialised method.

    Two ways out, and it is a decision rather than a patch. Either specialise only the paths which
    need a concrete type and leave the object erased throughout, or stop erasing constructed generic
    classes on Lua and pay the code size.

    #1239 is a reason to take it seriously rather than leave it. Both class shapes existing at once
    is what let an instance be allocated with no fields at all, and then with its fields under a key
    nothing read. Both are fixed; the shape which produced them is still there.

    Do not start this autonomously.

7. **Module bounds.** `module M<T: Show>` is rejected with a clear message today. Needs
   receiver rewriting during expansion, or type parameters on `ModuleInstanciation`.

9. **Keep `WURST_LANGUAGE.md` and `CHANGELOG.md` current** as items land — a standing practice
   rather than a task to finish. `WURST_LANGUAGE.md` is tracked, at
   `de.peeeq.wurstscript/src/main/resources/agent-docs/WURST_LANGUAGE.md`, and it already documents
   type class bounds and closure behaviour; it ships as a compiler resource rather than sitting at
   the repository root, which is easy to miss when looking for it. Fold an update into whichever
   item changes the behaviour rather than doing it as a separate pass.

10. **One `ImTypeVar` per type parameter.** Name-tolerant lookups remain in
   `EliminateGenerics.indexOfTypeVar`, `inheritTypeClassBinding` and
   `ProgramState.getCurrentTypeArgument`, compensating for several nodes standing for one
   source parameter. Making the node canonical lets all three compare by identity and removes
   a class of silent wrong dispatch. Mechanical, well covered by the suite.

   Note before starting: `moveFunctionsOutOfClass` copies a class's type variables onto each
   function it moves out, deliberately, and #1237 depends on that copy. Identity cannot hold across
   that boundary, so what is being made canonical is per scope rather than per program.

13. **A bounded generic class cannot be subclassed on Lua.** The Jass half landed in #1237: a call
    which names its target outright now carries the class's type arguments, taken from the class its
    first argument is used as, so `super.m()` and `super()` both reach the specialised copy.

    The Lua half is open, pinned by `TypeClassTests.subclassOfBoundedGenericIsStillBrokenOnLua`,
    which asserts the program compiles, runs, and never reaches `testSuccess` rather than leaving
    the difference between the targets to be discovered. `transformGenericNewOnly` runs neither
    `simplifyClasses` nor `addMemberTypeArguments`, so the type variables are never lifted there and
    a super call has nothing to carry. Closing it is item 23.

15. **One junk dispatch slot per specialised class.** `addDirectAliases` and
    `LuaTranslator.collectDispatchSlotNames` both compose `owner.getName() + "_" +
    semanticNameFromMethodName(name)`, and for a specialised method that trailing segment is the
    type argument — so every method of `FastHashMap<int, int>` claims one shared
    `FastHashMap_specialized_integer__integer` slot and the alphabetically first wins it.
    Nothing calls it, so it is dead weight rather than a wrong result.

    Tried using the declared name instead and reverted it: overloads share a declared name, so
    `setup(int)` and `setup(string)` collapse into one slot, which is what
    `LuaTranslationTests.overloadedMethodsDoNotAliasInLuaDispatchTables` and
    `moduleProvidedOverloadedOverrideDoesNotCollapseLuaSlots` exist to prevent. Both sources of a
    semantic name are wrong, in opposite directions: the mangled trailing segment collides across
    the siblings of one specialisation, the declared name collides across overloads. A fix needs a
    name separating both — the declared name together with the dispatch signature key would, since
    that is already what distinguishes overloads elsewhere in the same file. Worth doing only if
    this stops being dead weight, because the cost of getting it wrong is a real mis-binding while
    the cost of leaving it is one unused table key per specialised class.

24. **`luaOutputIsDeterministicForGenericOverrideSlots` fails intermittently.** It failed once on
    Windows CI and passed on a re-run of the same commit, having blocked an unrelated pull request
    in between.

    Do not weaken the assertion. It compiles one repro twice and compares the output byte for byte,
    so an intermittent mismatch is evidence of intermittent nondeterminism in Lua emission, which is
    exactly what it exists to catch — a re-run passing says the nondeterminism is intermittent, not
    that the test is at fault. Calling it flaky was too quick.

    Diagnose it instead: capture both outputs on a failing run and diff them, and rule out harness
    interference rather than assuming it. `WurstScriptTest` clears the global caches around every
    test method, so the two compiles do start from the same cache state; whatever differs is
    somewhere else.

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
