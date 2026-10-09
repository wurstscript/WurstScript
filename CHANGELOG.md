## 1.9 (in progress)

- A call through an interface with an old-style type parameter whose type converts to an index (`I<real>`) reaches
  the implementation of a subclass, and of a class which implements an interface extending it, with the argument
  converted back. Only a class which named the interface in its own `implements` got the bridge which converts it:
  `class D extends B` with `B implements I<real>` got the index as its real (Jass, interpreter), or B's implementation
  (Lua), whether D overrides the function or not, and so did a class implementing `J<real>` where `J<T> extends I<T>`.
  The type arguments are taken from wherever the class implements the interface now. Castle fight and zombie defense
  compile to the same scripts.

- The local optimisations no longer skip the condition of an if whose branches both start with a return or a loop
  exit. The branch merger moved the equal first statement in front of the if, so `if eff(b) > 0 ... return else
  return` became a plain `return` and `eff` was never called (Jass and Lua, `-localOptimizations`). A statement which
  may leave the block is moved now only when the condition has no effect. Castle fight compiles to the same script.

- On Lua, a compile-time expression can run the generic keyed-map intrinsics (`keyedMapPutNative`,
  `keyedMapGetNative`) again when nothing calls their Jass fallbacks at run time. The interpreter runs them through
  those fallbacks and finds them by name, but the tree shake in front of the compile-time run kept the intrinsic
  declarations on Jass only, so the run stopped with "The package declaring keyedMapPutNative must also declare the
  existing keyedMapPut Jass fallback". They are roots of that shake on both targets now; the later shakes on Lua
  still drop them. Castle fight compiles to the same script.

- A file is parsed with ANTLR's SLL prediction first, and only a file which it does not accept is parsed again with the
  full LL prediction. SLL ignores the context of the rule it is in, which makes it much cheaper, and it either returns
  the tree the full prediction returns or reports a syntax error, so a valid file gives the same tree. A broken file is
  parsed again from the first token with the error listener and the recovering strategy it always had, so it reports the
  same diagnostics in the same order, each once (the lexer has run already and does not report twice). Of the 733 Wurst
  files below castle fight (library sources included) 2 need the second parse. Parsing every one of them in a fresh
  JVM, six alternating pairs: 5.7 s instead of 7.1 s on one thread (median, 5 of 6 pairs faster) and 2.9 s instead of
  3.5 s on 8 threads (6 of 6), with 7 s less CPU of about 42 s. Whole builds are not told apart on this machine: the
  median difference of six interleaved pairs was 1.7 s faster for castle fight without optimisations, 0.3 s slower
  optimised and 0.4 s slower for zombie defense, against a scatter of several seconds, because parsing is already
  parallel and what is left of it is a small part of a build. The scripts are byte for byte the same in all 28 pairs.
  A file with a syntax error costs more than before, as it is parsed up to the error twice: 1.1 times as long with the
  error in the middle of the largest library file, 1.2 to 1.3 times at its very end. `WurstParser.setSllFirst(false)`
  restores the single pass. `SllParsingTests` compares trees and diagnostics of both on the standard library, `common.j`,
  `blizzard.j` and about 40 broken sources.

- Putting a file into the model while every unit is unchecked, as in the load of a project and of the libraries it
  imports, no longer searches the whole model for what imports it: that search only adds units to the set of unchecked
  ones, and they are all in it. `ModelManagerImpl.updateModel` was 4.1% of the samples of the compiler's thread in a
  castle fight build without optimisations and is 1.6%. Six interleaved builds against the jar of master: 0.6 s faster
  without optimisations (5 of 6, of about 29 s), 1.0 s optimised (5 of 6, of about 40 s), zombie defense the same within
  the noise (4 of 6, 0.15 s of 24 s). A model which has checked units is reconciled as before, so a file added after a
  check still makes what imports it unchecked.

- A build parses the files of the project and of its libraries by several threads. It parsed them one after the other,
  which was 13% (castle fight, optimised) to 18% (zombie defense) of the samples of the compiler's thread. The load
  now parses the files of the project ahead, and the libraries which the imports need level by level (the imports of
  the model, then those of the files they import), by as many threads as there are processors, up to 8. The model
  takes the files in the order it did, and the imports resolve depth first as before, so the compilation units are
  in the same order and the scripts are byte for byte the same (castle fight with and without optimisations, and
  zombie defense, to Lua; nine pairs). Against `-Dwurst.parseThreads=1`, which parses nothing ahead, six interleaved
  pairs of builds each were faster by a median of 1.4 s without optimisations (5 of 6 pairs, of about 31 s), 1.3 s
  optimised (6 of 6, of about 42 s) and 1.6 s for zombie defense (6 of 6, of about 26 s). What is left of loading a
  project is sequential (`BACKLOG.md`).

- The analysis which tells the optimiser what depends on a client-local value (`GetLocalPlayer()`, the camera, the
  keyboard, ...) is built in less than half the time, and once less in an optimised Lua build. It kept every node in
  identity hash maps, with a list of edges per node and a second copy of the data edges, and grew them one doubling
  at a time. It now keeps the flags and the edges in the nodes and in one table sized for the elements, leaves out the
  leaves which cannot depend on anything (constants), resolves the implementations of a method once instead of per
  call, and drops its graph once the answers are known. The wrapper inlining for Lua arithmetic after the local
  optimisations built the analysis, and the liveness of every function, even when no call of a wrapper was left to
  inline; it now builds them for the functions which have one. What the analysis concludes is the same: every query
  was compared with the old implementation for every construction in the test suite, in castle fight and in zombie
  defense, and the scripts of the suite (Jass and Lua) and of castle fight are byte for byte the same, zombie
  defense's except for its build date. On castle fight an optimised Lua build constructs it five times instead of six,
  it is 7% of the samples of the compiler's thread instead of 14% (about 4 s instead of 9 s), and the build takes
  59.7 s instead of 66.8 s (medians of four interleaved pairs, Java 27; single runs vary by about 8 s on a laptop
  which also runs other builds). Constructing it in a loop, new and old alternating on the same program, takes 2.6 s
  instead of 5.7 s for the six constructions of that build.

- A flatten leaves the functions which were not modified since a flatten left them: what a flatten leaves is flat, so
  another one would rebuild the same body. The generated code of the IM counts the modifications of each function
  (`ImFunction.modificationCount()`: every setter and every list of the function and of what is below it), so no
  pass has to say what it changed. Castle fight to Lua skips 17,514 of 42,710 function rebuilds without
  optimisations and 37,597 of 71,300 with them; to Jass without optimisations 1 of 42,315. The scripts of the whole
  test suite and of castle fight are byte for byte the same.

- The garbage removal runs until nothing is left. It stopped after ten rounds and a second removal finished what a long
  chain of assignments had left, and it replaced each assignment to an unread variable by an empty statement
  expression, for the next flatten to unwrap. It puts what the assignment does besides assigning in its place as
  statements now, so no flatten and no second removal follow it. Those statement expressions made `GlobalsInliner` take the package initialisers
  for code which may abort, so it did not inline their constants; it does now: of the 5,304 scripts of the test suite
  358 are different and none is longer. The scripts of castle fight are the same apart from the numbers in
  temporary names (6 of 360,423 lines of the optimised Jass one name another merged local). A round after the first
  looks only at the functions the round before changed, and the removal walks the body of a function once, not three
  times.

- Lua and Jass builds drop the functions nothing reaches (and the methods which go with them), and the globals which
  nothing reachable reads (with the assignments of constants to them), in three places: in front of the compile-time
  functions, before the generics are specialised, and after them. They used to be dropped only after the tuples were
  eliminated, so the compile-time functions, the generics and the passes in between (keyed tables, varargs, native
  lowering, local types, tuples, class elimination) walked about four times the functions, and about five times the
  globals, which end up in the script. What a type argument binds (the implementations of a type class, the
  constructor of a class given to `wurstNewInstance`) counts as reached, and so do the compile-time functions, any
  function holding a compile-time expression (evaluated whether anything calls the function or not), the helper
  functions which the passes call themselves and, on Jass, the declarations of the compiler's own intrinsics. The scripts are the same
  except for the order of independent global initialisers and the numbers in generated names and type ids, and the
  passes no longer report a problem in a function nothing calls: a vararg call which would need more than 31 Jass
  parameters in dead code now compiles. With the dead assignments gone, more constants of package initialisers are
  inlined and the initialisers which held only those disappear. Opt-less builds of castle fight take 32.4 s instead
  of 34.6 s on Lua and 31.8 s instead of 35.0 s on Jass, and zombie defense 36.0 s instead of 37.6 s and 37.4 s
  instead of 39.8 s (command line builds, medians of three, interleaved, Java 27; one of the three zombie defense Jass
  pairs went the other way).

- A command line build (`-build`, used by grill) checks the project once. It used to check it after reading it, then
  swap in the map script with the project config applied, which invalidates everything the check computed, and check
  it again. It now reads the project, swaps the script in and checks once, with the script the map is compiled with.
  Code which calls a function the config adds to the map script no longer fails the first check. Castle fight without
  optimisations builds in 36.0 s instead of 41.3 s and zombie defense in 28.2 s instead of 31.3 s (medians of three,
  interleaved); the script of castle fight is byte for byte the same.
- The compiler and bundled runtime now use Java 27, with compact object headers enabled by default.
  Tests retain ParallelGC; the shipped runtime uses Java 27's default G1 collector.

- The runtime of the distribution carries the base class data sharing archive of the JVM, which it never did: the
  slim runtime is built with jlink, and the AppCDS archive the build tried to make for the language server needs a
  base archive to sit on, so it could not be made and was never shipped. The build now makes the base archive
  (`jlink --generate-cds-archive`) and fails when the runtime has none, and the dead archive task and its
  `-languageServerAppCdsTrain` option are gone. With wurst4vscode starting the server with an archive of its own
  (written next to the compiler jar when the first session ends), the language server of a small project is ready
  after 3.7 s instead of 4.4 s, and castle fight after 17.3 s instead of 19.0 s (medians of four starts).

- The language server checks every file it was meant to check. A check which cannot start, because an import
  does not resolve, used to forget the files it was planned for. A file edited in the meantime then got no
  diagnostics once the import was fixed, since nothing it imports had changed. Those files are kept and checked
  with the next check. Deleting one of two definitions of a package now also checks the other, which kept
  its "defined multiple times" error. Editing a config package (`Foo_config`) now checks the package it
  configures and everything importing that, which kept calling the function the config package used to
  define until one of them was edited itself. The alternatives listed by a "call is ambiguous" error are sorted, where
  they used to follow the order the files were loaded in.

- Running a map no longer type checks again a model which the language server has checked completely and which
  has not changed since. On castle fight without optimisations, building the same model again in one process took
  14.6 and 12.6 s and takes 12.3 and 10.7 s, the script unchanged byte for byte. The first build of a model
  still checks it, because it replaces the map script in the model with the one the project config was applied to.

- The same source now compiles to the same script however the identity hashes of the compiler's syntax nodes
  happen to fall. A class which gets a function from a module and implements an interface declaring the same
  function made calls like `value.write(x)` bind to the module's implementation or to the interface's
  declaration according to the order of a hash set, so one build had a direct call where the next had a
  dispatched one, and the two scripts held a different number of functions. The call now always binds to the
  implementation, as the type checker already resolved it, and the candidate functions of a class are kept in
  declaration order instead of hash order. The sub-methods of each method, which the backends bind dispatch slots
  in, are sorted by package, name and source position like the classes and functions already were, so neither
  hash order nor the order the compilation units arrive in decides them. That covers the overrides in subclasses,
  in the classes implementing an interface and in closures.

- The bundled pjass is updated on all three platforms to lep/pjass master (`378a1ca`) plus its `--each` option
  (lep/pjass#20). Windows had a December 2022 build and Linux and macOS a January 2019 one, so the three did not
  check the same things. The 2022 Windows build also got slower the more files were in the directory of the script
  and of the executable, because of its MinGW runtime: 69.2 ms against 49.5 ms to check a script with 10,000 files
  beside it, and 37.6 against 25.2 ms to start from the temp directory. The new Windows build is not affected. The
  Linux build needs glibc symbols up to 2.10 only, and the macOS build runs on macOS 10.14 or newer.

- On Lua, joining two strings no longer checks for nil an operand which cannot be nil. Every `a + b` on
  strings went through a helper that checks both sides, which `-inline` then expanded into a nest of
  comparisons at each use. A literal and the result of `I2S` are always strings, so with both
  known the join is the `..` operator, and with one known only the other side is guarded, as `x or ""`. Two
  operands which may both be nil still use the helper. A nil on either side still reads as nothing.

- A closure is named after the call it is passed to once, however many levels of that call enclose it. A
  closure passed to `doAfter(..)` inside a closure passed to `doAfter(..)` inside another used to get
  `doAfter` three times in its class name, and the Lua function implementing it repeated the whole name a
  second time, so the script held identifiers such as
  `Callback_doAfter_doAfter_doAfter_Pkg_call_doAfter_doAfter_doAfter_Pkg`. A name which directly repeats the
  one before it is now left out, which gives `Callback_doAfter_Pkg`, and on Lua the function is named after its
  class and the method alone: `Callback_doAfter_Pkg_call`. Closures which end up with the same name are told
  apart by a number, as closures in the same function already were, on Jass and on Lua. Dispatch is
  unaffected. Only the names change, and type ids with them, since those are numbered in name order,
  as they already change when a class is added or renamed.

- Code completion no longer suggests code while writing documentation, block, or line comments.

- On Lua, class tables bind only the dispatch slots a call site reads. Every method used to be bound under the
  names of its overrides, of the closures sharing its interface and of its class-prefixed forms, so a family of
  closures cost one table write per sibling in every member: quadratic in the size of the family, and 36,000 of
  the 38,000 bindings in a large map were never read. The unread ones are no longer emitted, which takes about
  40% off that map's script. Dispatch itself is unchanged.

- On Lua, `-inline` now expands one-line getters and setters at every call site, whatever the number of
  callers and wherever the call is. Method calls with a single implementation used to reach the inliner only
  inside loops, so `list.size()` outside a loop stayed a call however small it was. A getter or setter is also
  expanded in functions too large for the inliner's register budget, since it declares no local, and
  `KeyedMap` reads are written as the table index (`t[k] or 0`) instead of calling a helper, so an unused
  read is dropped.

- Added type class bounds for `T:` generics. A bound requires operations of the type it is bound to, so a
  generic can do more than store and return values, without giving up static dispatch:

        public interface Indexable<T:>
            function toIndex(T x) returns int
            function fromIndex(int i) returns T

        implements Indexable<vec2>
            function toIndex(vec2 v) returns int
                ...
            function fromIndex(int i) returns vec2
                ...

        class HashMap<K: Indexable, V: Indexable>
            function get(K key) returns V
                return V.fromIndex(loadInt(K.toIndex(key)))

    A bound names the interface unapplied, so `<K: Indexable>` means "there is an instance of `Indexable<K>`",
    and several combine with `and`. Requirements are called on the type parameter (`K.toIndex(key)`), which
    keeps operations that produce a value of the type, such as `fromIndex`, in the same form as the rest.

    Unlike an interface used as a supertype, a bound is satisfiable by `int`, `real`, `string`, tuples and
    handle types, and costs nothing at runtime: after specialisation each requirement is a direct call to the
    instance function, on both Jass and Lua.

    An instance of `I` for type `X` may only be declared in the package declaring `I` or the one declaring
    `X`, and only once, so `I` for `X` means the same thing throughout a program regardless of imports.

- A type class bound is now usable from inside a closure, so a bounded generic can hand work to one:

        interface Producer
            function produce() returns int

        function indexLater<T: Indexable>(T x) returns Producer
            return () -> T.toIndex(x)

    Substituting a type variable now carries the instance chosen for it along with the type, rather than the
    type alone, so lifting a body into a class of its own no longer loses it. This works on both targets.
    Lua reaches such a class through the interface it implements, so no call names the instantiation and the
    construction is what the specialisation is taken from.

- A module's type parameter may now carry a type class bound, and the class using the module supplies the
  argument:

        module Shower<T: Show>
            T held
            function shown() returns string
                return T.show(held)

        class Holder<K: Show>
            use Shower<K>

    Using a module copies its body into the class and replaces the module's type parameters wherever they
    are used as types. The receiver in `T.show(held)` is a name rather than a type, so the replacement never
    reached it and the bound was rejected. The instantiation now declares the parameters and records the
    arguments chosen for them, so that name resolves and says what it stands for. The argument must satisfy
    the bound, which is reported at the `use`. This works on both targets.

- On the Lua target, a bounded generic class can now be subclassed, and a requirement can be dispatched
  from inside a constructor. A generic object stays erased there and only the paths needing a concrete
  type are specialised, so the concrete type has to reach those paths rather than the object: a
  specialised method is bound to the class its objects are allocated from, a call which names its target
  — `super.m()` is one — takes the instantiation from the class its receiver is used as, and a function
  of a generic class is matched against that class's type variables rather than being read as having
  none of its own. A specialisation nothing allocates is no longer emitted at all.

    One shape remains unsupported on Lua: a method combining its own type parameters with those of the
    generic class owning it, though it is no longer rejected outright — the arity check it tripped over
    counted the class's type arguments against a call that had only supplied the method's.

- Added new pseudo-natives for debugging memory leaks:

        // returns the maximum type id, can be usd to
        // iterate over all type-ids from 1 to maxTypeId()
        native maxTypeId() returns int
        // returns the class name for a given type id
        native typeIdToTypeName(int typeId) returns string
        // returns the number of active instances for a typeId
        native instanceCount(int typeId) returns int
        // returns the maximum number of instances reached for the given type id
        native maxInstanceCount(int typeId) returns int

    Remember, that when translated to Jass all types related by a subtype relation share the same object id recycler and will thus give the same numbers for `instanceCount`.
    The interpreter will give more precise numbers.

## 1.8.1.0 (2019-04-11)

### Changelog reactivated

The changelog was on a longer hiatus, but we plan to add a few version increments with important highlights every now and then, to provide up to date information compared to infrequent blog posts. The standard library will most likely receive its own changelog.

### Features

* The setup tool received a face lift and can now be used completely from the command line using `grill`
* Wurst now works better on linux and osx with native binaries and improved support
* A new type of improved generics that works without the need for the `Typecasting` package.
* Can now retain most data from compiletime to runtime, including tuple and object allocations
* Many bug fixes and improvements


### Website

* All new website, with new tutorials and standard lib documentation
* Added Showcase for wurst powered maps and resources


## 1.7.0.0 (2017-06-17)

### Gradle Migration

WurstScript is finally using gradle!

Additionally all deprecated projects such as the eclipse plugin and their coresponding issues have been removed to clean up the repository.

#### Other fixes include:

- stacktraces are now build in an array to avoid hitting the string limit
- added missing errors for nested tuples and static access of dynamic variables in certain cases
- cleaned up wurstpack, log output, readme
- local optimizations have been improved and are now run multiple times to produce even more optimized output
- more..

## 1.6.0.0 (2017-05-06)

### Major Update

We havn't really been keeping up to date with the changelog, but steadily developing wurstscript in the background.
Anyhow, a short list of the most important changes over the last 2+ years:

- fixed tons of issues and added many new features to wurstscript and stdlib
- new recommended ide plugin for vscode
- tons of optimizations from build speed to generated jass code and generated mpq
- now using jmpq3, a native java mpq library
- added runmap and wurstpack support for patch 1.28
- optionally execute jasshelper before wurst, allowing both languages in the same map
- much, much more!

1.6.0.0 is the first in a series of updates promoting the new wurstflow and allowing easier setup.

## 1.5.0.0 (2014-08-17)

### Java 8

Wurst now requires *Java 8*. Download it [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

### Language changes

- operator overloading is now enabled for strings
- `@compiletime` annotation is now allowed on static methods
- new pseudo-native: callFunctionsWithAnnotation (check the hotdoc in MagicFunctions.wurst for documentation)


### Tools

- Added a warning for stupid assignments like `this.x = x`
- Improved dataflow anomaly checker, gives warnings when a value is not used
- Experimental compilation server for Wurstpack to make compiles faster (not documented yet and hard to use)
- Eclipse can now do code-folding on imports
- Improved error markers in eclipse
- Autocomplete no longer triggers inside of comments or strings


### Bugfixes:

- fixed #216: Stdlib: ItemDefinition from UnitObjEditing.wurst creates units instead of items.
- also fixed some bugs regarding object editing in the standard lib
- fixed #277: Packages can import themselves, resulting in compiler bug
- fixed #278: Unicode support
- fixed #285: Multiline strings give no parse error
- fixed #296: Underscore crashes Wc3
- fixed a bug in jmpq library
- fixed a bug in the translation of closures
- hopefully fixed #26: Multiline comments were sometimes not highlighted correctly in eclipse
- fixed a really bad bug in the optimizer (TempMerger)
- fixed #280: extension functions with empty body were valid
- fixed bug with empty switch statements
- fixed a bug in the interpreter when handling ints/reals and overloading of natives
- fixed a problem when starting a map from eclipse on certain Windows installations
- fixed #310: cyclic class hierarchies

### Std-Lib:

- replaced the old DummyRecycler with a new, improved one
- added UnitIndexing capabilities with OnUnitEnterLeave package
- added Simulate3dSound (credits to purgeandfire)
- added RegisterEvents
- added AbilityTooltipGenerator
- added auto-tooltip generation for abilities
- Added LList backwards iterator
- added .has to HashMap
- added UpgradeObjEditing capabilities
- added preset-functions to AbilityObjectEditing for use with closures
- added ObjectIdGenerator
- added OrderStringFactory
- revamped ChannelAbilityPreset
- revamped DamageDetection
- fixed QueueModule



## 1.4.1.0 (2014-06-22)

### Language Changes:

- The init order now again depends on the import order.
	A cyclic init order is forbidden.
	Use the `initlater` keyword on imports to break cycles.
- The rules for newlines are changed.
	Previously all newlines inside parenthesis were ignored (similar to Python).
	Now newlines are ignored when a line ends with or begins with special characters (similar to Go).
	For example when a line ends with a comma, the following newline is ignored.
- It is now possible to have mutually recursive functions.
- Wurst now supports array members for classes (#176).
- Packages can now be configured with config-packages.


### Standard library:

- Some bugfixes and refactorings.

### Tools:

- some improvements to auto-complete in Eclipse.
- better error messages for some special cases.
- Eclipse now has a command to extract all Custom-Text-Triggers from a map to separate files in the current project.
- The Wurstpack can now automatically import all files from an import-folder into the map.
- Some improvements in the Wurstpack updater tool.
- .j files are now parsed as Jurst files.


### Bugfixes:

- fixed #226, bug in used global variable analysis.
- fixed #230, read variable analysis.
- fixed #233, StringCase native was wrong in interpreter.
- fixed #237, critical bug in localOptimizations->tempMerger.
- fixed #244: 'null' was not translated correctly, when used with a handle bound to a generic type variable.
- fixed #207, improved flow analysis for closures.
- many other smaller bugs and possible crashes.


### Internal changes:

- Compiler now uses Antlr4 instead of Cup and Jflex for parsing.
- Wurst now uses Jmpq2, a new pure Java mpq library written by Crigges.
	This should eliminate some rare bugs with the old MPQ library and make everything easier to port to different platforms.


## 1.4.0.0 (2014-03-15)

### Language Changes:

* First version of Jurst (a Wurst dialect with a (v)jass-like syntax)
    * not documented yet, you can create `*.jurst` files in eclipse if you want to play with it
* Modules can now be generic
* Operator overloading: op_div must now be named op_divReal
* Better warnings for unused parameters (Variables starting with an underscore will be ignored for this warning)
* The initialization order no longer depends on the imports. Instead the compiler analyses which variables are read.


### Standard library:

* Many smaller changes
* More unit testing functions


### Tools:

* The recommended download for the WurstPack is now the updater made by Crigges
* Slightly better command line interface for the compiler
* Injecting of compiletime generated objects into the map has been enabled again but is still broken, because of MPQ-lib problems
* wurst config file is no longer used
* The eclipse plugin no longer crashes when the Wurst nature is missing, instead the user is asked to add it
* Removed the option for cohadars jasshelper from WurstPack, because it was causing problems for some users
* Added a custom text field to auto error reports


### Bugfixes:

* Fixed bug #177 Remove FileIO dependency from Wurst.wurst
* Interpreter: Comparing two reals for equality was broken
* Fixed a bug in tuple assignment
* Fixed bug #203 - could use type parameters in static places
* fixed a bug with overrides of generic functions
* fixed hanging compiler when recursive functions were used in certain ways
* Fixed bug #220, class names could be used as expressions
* Object-editing: Read and write strings as UTF-8.

## 1.3.0.0 (2014-01-11)

### Language Features:

* There now is a warning for unused variables.
* The optimizer (local optimizations) is now more aggressive when inlining local variables.

### Tools:

* New Updater (still beta, not part of the WurstPack release yet)
* Changed MPQ editor to Jmpq (loading StormLib via JNA). With this change it is now possible to compile and run Maps
	from Linux systems. Macs should also be supported, but this has not been tested yet.
* WurstPack now adds an option for starting the map with SharpCraft, when there is a SharpCraft folder in WurstPack (not included by default).

### Standard library:

* Many functions which used to return 'this' now return nothing. Use the cascade operator for chaining method calls.
* LinkedList: LLIterator is now public
* Some new functions for vectors.
* New package: ClosureForGroups

### Bugfixes:

* Fixed bug #193: Inliner could remove the global init func and mess with later phases in translation.
* Fixed a bug where closures did not work with tuples correctly.
* Fixed bug in tuple elimination
* Fixed bug #187: optimizer removed compiletime functions before they could run.
* Fixed problem in interpreter, where dynamic dispatch no longer worked after recompile.

## 1.2.0.0

### Language:

* Added [cascade operator](http://peq.github.io/WurstScript/manual.html#cascade_operator_dotdotoperator) (`..`) for chaining method calls
* Anonymous functions can now be used where a `code` expression is expected.
* A newline at the end of wurst files is no longer necessary
* Anonymous functions can now have an expression with a return value if no return value is expected.
* `destroy x` is now an expression. This means it can be used in anonymous functions more easily.
* Better type inference when calling generic functions

### Tools:

* Eclipse REPL now uses toString function to print results.
* Eclipse REPL now tries to resolve imports
* Improved autocomplete
* WurstPack now supports JassHelper. It is possible to run JassHelper before Wurst and so have limited support for both languages in the same map.

### Standard library:

* ClosureEvents package

### Bugfixes:

* CRITICAL BUG. Changed id-recycler back to simple stack based because of bugs.

## 1.1.0.7

### Language:

* It is now possible to call ExecuteFunc with a constant string argument
* It is now possible to have natives in the mapscript. Natives can be annotated with `@extern` if they should not be included in the map script.

### Tools:

* Progress bar in WurstPack is now more precise
* Running a map from Eclipse should be much faster now.


### Bugfixes:

* Fixed a small bug in the backup controller.
* Fixed handling of int-reals subtyping in jass
* Fixed bug in unit object editing (many fields where declared with type `real` but actually had `unreal`)


## 1.1.0.6

### Language:

* Added [stacktraces](http://peq.github.io/WurstScript/manual.html#stacktraces)

### Tools:

* Maps can now be launched from Eclipse
* war3map.j is now saved in wurst folder when compiling a map from WurstPack. This makes it possible to use constants from the map in Eclipse.
* Eclipse: Fixed bug in text-hover
* Some improvements of the REPL
* Eclipse: Improved configuration of syntax highlighting
* Eclipse: Hovering over variables, function-calls etc. now shows some nice information.

### Bugfixes:

* fixed error message, when type args could not be inferred
* several fixed related to object editing
* fixed bug in closure translation, when closure implementation had type void


### Standard Library:

* ClosureEvents and ClosureTimers packages

## 1.1.0.5

### Bugfixes:

* Fixed bug with overriding+generics
* Fixed bug with `thistype`


## 1.1.0.4

* Fixed problems with Eclipse console
* Fixed a bug with inherited generic type params

## 1.1.0.3

* only internal changes

## 1.1.0.2

### Bugfixes:

* improved error handling in case of a compiler bug
* fixed bug in closure type calculation
* eclipse plugin: fixed problem in repl with generics and improved reconciler behavior in case of parse errors

### Standard Library:

* LinkedList: addded closure functions


## 1.1.0.1

### Language:

* Added [anonymous functions and closures](http://peq.github.io/WurstScript/manual.html#lambda_expressions_and_closures)
* It is now possible to destroy an object via an interface type

### Bugfixes:

* fixed: check that abstract functions are not private

