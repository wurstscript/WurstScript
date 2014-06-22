---
layout: simple
title: Changelog
---

Version 1.4.1.0
==================

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


Version 1.4.0.0
==================

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

Version 1.3.0.0
==================

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

Version 1.2.0.0
==================

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

Version 1.1.0.7
=========================

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


Version 1.1.0.6
=========================

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

Version 1.1.0.5
=========================

### Bugfixes:

* Fixed bug with overriding+generics
* Fixed bug with `thistype`


Version 1.1.0.4
=========================

* Fixed problems with Eclipse console
* Fixed a bug with inherited generic type params

Version 1.1.0.3
=========================

* only internal changes

Version 1.1.0.2
=========================

### Bugfixes:

* improved error handling in case of a compiler bug
* fixed bug in closure type calculation
* eclipse plugin: fixed problem in repl with generics and improved reconciler behavior in case of parse errors

### Standard Library: 

* LinkedList: addded closure functions


version 1.1.0.1
=========================

### Language: 

* Added [anonymous functions and closures](http://peq.github.io/WurstScript/manual.html#lambda_expressions_and_closures)
* It is now possible to destroy an object via an interface type

### Bugfixes:

* fixed: check that abstract functions are not private

