## 1.9 (in progress)

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

