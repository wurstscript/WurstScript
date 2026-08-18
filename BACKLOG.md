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

27. **A natively keyed store on Lua, selected by a type class.** Decided with the repo owner:
    **the native path is taken only where the key's equality is identity**, and a type class says
    which keys those are. Everything else keeps today's probing on both targets.

    Why it is worth doing. `FastHashMap` does its own hashing and linear probing on both targets, but
    a Lua table already is a hash map: `t[key] = value` would let Lua hash, and would lift the fixed
    `FASTHASHMAP_CAPACITY`/`FASTHASHMAP_MAX_INSTANCES` limits there entirely. It would also get string
    keys off `StringHash`, which this library documents as unusable for the purpose - case insensitive
    (`String.wurst:81`, so `a` and `A` share a key), collapsing every partial multibyte slice to one
    constant (`MultibyteDiagnostics`), and undocumented and changed between game versions. It *is*
    emulated - `StringProvider` delegates to `Wc3StringHash` and `Wc3StringHashTest` checks parity with
    the Lua shim - so a test does see the real behaviour; an earlier draft of this entry said otherwise
    and was wrong. What makes it unusable is the behaviour itself, not the fidelity of the emulation.
    Other containers - a set, a memo cache, adjacency maps - then build on the one store.

    Why the type class is load bearing rather than incidental. A Lua table matches keys by raw
    identity, which for a class is reference identity. An instance whose `equals` is structural -
    `Hashable<vec2>` comparing components - would therefore have Jass treat two equal-valued keys as
    one key and Lua treat them as two, silently, from one program. So the native path is sound only
    for `int`, `real`, `string`, `boolean` and reference-keyed classes. A second bound states that:

        public interface RawKeyed<T:>          // no requirements; a promise that equality is identity

    How the two are then selected is unsettled, and the obvious spelling does not work: Wurst does not
    overload a type on its bounds, so declaring `FastHashMap<K: Hashable>` beside
    `FastHashMap<K: Hashable and RawKeyed>` makes every mention of the name ambiguous before any
    instance is considered. Either the native variant is a separate type - `RawHashMap`, say, with the
    bound as its entry condition - or one type carries both strategies and picks per operation, which
    costs a branch and gives up the limits being lifted. Decide that before writing any of it.

    A further constraint on admitting reference-keyed classes: `null` is a value of any class type and
    lowers to `nil`, and `t[nil]` is a runtime error in Lua while the probing implementation accepts
    it wherever `Hashable` does. So identity alone is not sufficient for the native path - null keys
    have to be rejected or special-cased.

    Groundwork already established, so the next attempt does not have to find it again:

    - Intrinsics are declared in Wurst with `@compilerintrinsic` in `wurst/_wurst/MagicFunctions.wurst`
      and bounds parse there; `wurstNewInstance<T:>() returns T` is the shape to copy. Recognition is
      by name plus `!AttrFuncDef.hasApplicableUserFunction(call)` in `CompilerIntrinsics`.
    - `ImTranslator.isLuaTarget()` is available during Wurst-to-IM lowering, which is what makes this
      feasible: the intrinsic can lower differently per target rather than needing dead-branch
      elimination to have happened first. An `if isLua` guard alone does not help, because folding
      runs after translation and both branches are lowered.
    - A Wurst array access already lowers to a plain `t[i]` on Lua
      (`lua.translation.ExprTranslation.translateArrayAccessRaw` builds `LuaExprArrayAccess`). Nothing
      in the backend needs changing; the only obstacle is that the language requires an `int` index,
      and typechecking is target independent, so relaxing it for Lua alone would let a program compile
      for one target and fail on the other.
    - `ImTranslator.imError(trace, message)` gives a runtime error call, for the Jass lowering of an
      intrinsic that has no Jass meaning. `ImStatementExpr` is available for pairing statements with a
      value.

    Left to settle. The read is straightforward - `wurstKeyedRead(store, key)` lowering to
    `store[key]`. The write is the open question: as an `ExprFunctionCall` it must lower to an
    `ImExpr`, while what it wants to be is an `ImSet` to an array access. Either wrap it in an
    `ImStatementExpr` with a discarded value, or expand it at AST level after validation the way
    `wurstMapFields` assigns back to fields, which is a different mechanism and may be the cleaner
    one. Settle that before writing the surface.

    **Two things this now has to answer, from discussion.**

    *Which target is `FastHashMap` for.* The owner's read is that it is really a Lua container. That
    is right about speed and not about usefulness. On Jass a native `Table` does the hashing outside
    the script, so probing arrays with a hash computed in Wurst will not beat `HashMap` - the name
    only earns itself on Lua. But `HashMap` cannot take a `vec2`, a tuple or anything else not
    castable, and two keys which cast to one int collide there, which is the niche `FastHashMap`
    exists for and which is target independent. So: keep it working on both, take the native table on
    Lua, and stop presenting the Jass path as the fast one. If that holds, the native table becomes
    the primary implementation rather than an optimisation, and the capacity limits and probing become
    the Jass fallback - which also settles the selection question above, since the two variants stop
    being peers.

    *A `Hashing` package.* Wanted so instances and future containers stop hand-rolling a mix each.
    The modern choices - MurmurHash3's `fmix32`, xxHash, FxHash - are all xor, shift and wrapping
    multiply, and that is where the targets diverge sharply. `Bitwise.bwXor32` extracts eight bytes
    and does four table lookups per xor, so `fmix32` would cost roughly twenty four divisions and
    twelve lookups per integer on Jass, worse than the arithmetic mix it would replace and for
    avalanche nobody observes at `mod 32`. On Lua those ops are native and `fmix32` is the right
    answer outright. `fmix32` also depends on 32 bit wrapping multiply, which Jass has and Lua does
    not, so identical values across targets need masking - two more `and32`, another sixteen
    divisions. Conclusion: one surface, `hashInt`/`hashString`/`combine`, split by target on the same
    `isLuaTarget()` lowering this entry is about, which makes the package a second consumer of it
    rather than separate work. Measure `bwXor32` before committing to any of this; its cost is the
    whole argument and it was read from the source rather than timed.

    Blocks `WurstStdlib2#468`, deliberately: shipping `FastHashMap` first would commit
    `FASTHASHMAP_CAPACITY`, `isFull()` and `Hashable.hash` to the public API when the Lua path makes
    all three meaningless on that target.

28. **Two costs the Lua emission pays which look avoidable.** Found by reading the emitted script
    for `FastHashMapTests_fastHashMapRuntimeLua`, not by profiling. **Measure both before touching
    either**; each proposal below is a structural argument and a call count, which is not the same as
    knowing what the game's interpreter charges for it.

    *Every primitive array read is a function call.* `LuaNativeLowering.lowerPrimitiveArrayEnsure`
    wraps each non-lvalue primitive array access in `ensureInt`/`ensureBool`/`ensureReal`, and the
    wrapper survives the optimiser into the emitted script:

        if not(__wurst_ensureBool(FastHashMap_used[s])) then
        return ((s2 >= this5.FastHashMap_base) and __wurst_ensureBool(FastHashMap_used[s2]))

    Writes are raw, lvalues being skipped, so only reads pay. `slotFor` reads `used` and `dead` every
    probe step, which is two Lua calls per step in the hottest loop the container has. The shim itself
    is needed: an untouched table key is `nil` where Jass reads `0` or `false`, and class and handle
    typed arrays already skip it because `nil` is the right default for them.

    The fix is to move the default off the access site and onto the table, which is the ordinary Lua
    idiom for exactly this:

        __wurst_default_false = ({__index = function() return false end})
        setmetatable(FastHashMap_used, __wurst_default_false)

    A present key is then a raw table index with no call at all, and the function runs only on a miss
    - which is the rare case, a read of a slot never written. One metatable per primitive array global
    replaces a wrapper at every read site. Three shared metatables cover int, bool and real. `pairs`
    is unaffected by `__index`, so nothing which iterates an array changes. What has to be checked:
    where the `setmetatable` calls are emitted (globals init, before any read), that a `0` or `false`
    actually stored still reads back rather than falling through, and whether anything relies on the
    stacktrace argument `callWithStacktrace` adds to the current wrapper.

    *A method with an override stops being a direct call.* Without one, a bounded generic's method
    lowers to a direct call - `Box_render_specialized(Box_new_Box(), 42)`. Add a subclass which
    overrides it and the same call becomes `b:Box_size_specialized_integer(1)`, an instance miss
    followed by an `__index` hop to the class table. The tables are flat rather than chained -
    `SubBox.Box_size_specialized_integer` is assigned on `SubBox` itself, so it is one hop and not a
    walk up the hierarchy - so the cost is that hop plus a dynamic lookup against a global function
    call, and adding an override anywhere silently converts every call site of that slot.

    The fix is class hierarchy analysis at the call site: if the receiver's static type has exactly one
    reachable implementation of the slot, emit the direct call. `ImMethod.getSubMethods()` already
    holds what that question needs, and `RemoveGarbage` already establishes reachability, so this is
    reading data which exists rather than computing anything new. It also composes with the erasure
    model in item 23: fewer virtual slots means fewer of the naming and binding problems items 15 and
    26 came from.

22. **The library's own tests do not run on Lua.** They run on the interpreter now — all 460 of
    them, collected by importing every package in the checkout whose name ends in `Tests`. That
    half is done; this is the other one.

    `executeTests` runs them through `RunTests` on the intermediate language, so it covers the
    interpreter only. Running them on Lua needs the harness to execute a Wurst test function on
    that target rather than an `init` block, which is new machinery rather than a flag. Worth it:
    the two targets have disagreed before, and every disagreement found so far was found by running
    the same program on both.

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

    **Done.** Items 6, 13 and 25 are closed and the model is what closed them: a specialised method is
    moved to the class its objects are actually allocated from, and every remaining site reads the
    instantiation off something the call already has rather than off the object. A specialisation
    nothing allocates is left with no methods and drops out entirely, so the second class shape is gone
    wherever an ordinary generic object is involved -
    `FastHashMapTests.assertSpecialisedClassesAllocateTheirFields` states both accepted outcomes rather
    than the one that used to hold.

    The type-driven collector this entry expected to need was not needed. Item 6 looked like it wanted
    one, since a constructor has no receiver and the note below reasoned the instantiation was only on
    the type of what the result is assigned to. It is not: `new_Box(21)` carries the type argument
    already. What was missing is that a function of a generic class declares no type variables of its
    own — it uses the class's, which this target does not lift — so specialising it was read as nothing
    to do, the argument was stripped and the dispatch inside was left abstract. Matching such a function
    against its class's variables is the whole fix, and it is the same rule as everywhere else here
    rather than a second mechanism. Written down because three earlier notes argued for the harder one.

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

26. **Done. A dispatch slot's segment is recorded where it is assigned, not recovered from a name.**
    The segment used to be found by cutting a method's name at its last underscore, which is the right
    answer only when the rest contains no underscore and the method is not a specialised copy. Four bugs
    came out of that: a method declared `get_it` composed a slot called `it` and its override never
    reached it; a specialised method composed a slot named after its type argument; requiring the cut to
    equal the declared name lost the slot an override of a numbered overload has to replace.

    `LuaDispatchPreparation.normalizeMethodNames` names a dispatch group after one member, sanitises
    that name into a Lua identifier and uniques it, and now strips the naming member's class from it and
    records the result on `ImTranslator` for every member of the group. Both composers read the record.
    `semanticNameFromMethodName` is gone from both.

    Three earlier attempts are worth remembering, because each looked equivalent and was not. Asking the
    declaration alone collapses overloads, which share a declared name. Using a method's name whole
    breaks cross-class matching, since the name is class-prefixed. Stripping each method's *own* owner
    fixes the underscore case and breaks override chains, because a group is named after one member and
    an ancestor's method can carry a descendant's class in its name - which is exactly why no method can
    work its segment out for itself, and why the recording happens where the group is in hand.

    `LuaTranslationTests.underscoreNamedOverrideDispatchesInAGenericHierarchy` is now a positive test.
    Still in the same family: `ProgramState.identifyGenericStaticGlobals` takes the longest prefix of a
    global's name ending at an underscore which matches a class name. #1249 made the recorded owner
    preferred where one exists, so this is only the fallback, and it should stop being reachable.

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

- 6. A requirement dispatched from inside the constructor of a bounded generic class works on Lua. The
  call running a constructor carries its type argument already; what was missing is that a constructor
  declares no type variables of its own, using its class's, so specialising it was treated as nothing
  to do — the argument was stripped and the dispatch left with no concrete type. Functions of a generic
  class are now matched against the class's type variables, which also covers the constructor body and
  the field initialiser it calls.

- 25. A bounded type parameter on a method of a generic class no longer trips the arity check on Lua. A
  method call there carries the class's type arguments followed by the method's own, and the check for
  whether the class's were still missing asked whether the call had *any* — so a method declaring
  parameters of its own was read as already having both and its specialisation was matched against a
  list one longer than what the call supplied. `aBoundedMethodParameterInAGenericClassLua` runs the
  program now instead of pinning the rejection.

  Removing that rejection is not the same as supporting the shape, and `AGENTS.md` says not to promise
  it: a method combining its own type parameters with its owning generic class's stays outside the Lua
  contract, one running program being one call site rather than a guarantee. The docs say so rather than
  reading the fix as general support.

- 13 (Lua half). A subclass of a bounded generic class works on Lua. Two things were wrong and each hid
  the other. The specialised method was left on the specialised class while the object is allocated
  from the erased one, so the slot a virtual call named resolved to nothing; it is moved to whichever
  class the program actually allocates. And `super.m()` names its target, so it reached the erased
  original whose dispatch had been neutralised as dead — `nil + extra` at runtime. It now takes the
  instantiation from the class its first argument is used as, which is the same answer the Jass path
  gets from the lift it does not do here.

- 7. A module's type parameter can carry a bound. The instantiation declares the module's parameters
  and records the arguments, so the receiver in `T.show(x)` has a name to resolve and something to
  say what it stands for. Excluding them from inference turned out to mean not making a
  `ModuleInstanciation` an `AstElementWithTypeParameters` at all: as one, every method of a generic
  module's instantiation asked its caller to infer a parameter its signature never mentions, which is
  what `genericModuleInGenericClassGet` was reporting. The parameters are registered as type names in
  `TypeNameLinks` instead. The receiver denotes the argument bound to the parameter, offering the
  requirements the parameter declared with the argument's types, and dispatch follows the argument —
  the using class's type variable when it is one, the instance directly when it is concrete, since a
  module used with a concrete argument leaves no variable to substitute. The bound is checked at the
  use, the only place which sees the parameter and the argument together.

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
