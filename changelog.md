---
layout: simple
title: Changelog
---

# Version 1.3.0.0

## Language Features:

* There now is a warning for unused variables.
* The optimizer (local optimizations) is now more aggressive when inlining local variables.

## Tools:

* New Updater (still beta, not part of the WurstPack release yet)
* Changed MPQ editor to Jmpq (loading StormLib via JNA). With this change it is now possible to compile and run Maps 
	from Linux systems. Macs should also be supported, but this has not been tested yet.
* WurstPack now adds an option for starting the map with SharpCraft, when there is a SharpCraft folder in WurstPack (not included by default).

## Standard library:

* Many functions which used to return 'this' now return nothing. Use the cascade operator for chaining method calls.
* LinkedList: LLIterator is now public
* Some new functions for vectors.
* New package: ClosureForGroups

## Bugfixes:

* Fixed bug #193: Inliner could remove the global init func and mess with later phases in translation.
* Fixed a bug where closures did not work with tuples correctly.
* Fixed bug in tuple elimination
* Fixed bug #187: optimizer removed compiletime functions before they could run.
* Fixed problem in interpreter, where dynamic dispatch no longer worked after recompile.

# Version 1.2.0.0

## Language:

* Added [cascade operator](http://peq.github.io/WurstScript/manual.html#cascade_operator_dotdotoperator) (`..`) for chaining method calls
* Anonymous functions can now be used where a `code` expression is expected.
* A newline at the end of wurst files is no longer necessary
* Anonymous functions can now have an expression with a return value if no return value is expected.
* `destroy x` is now an expression. This means it can be used in anonymous functions more easily.
* Better type inference when calling generic functions

## Tools:

* Eclipse REPL now uses toString function to print results.
* Eclipse REPL now tries to resolve imports
* Improved autocomplete
* WurstPack now supports JassHelper. It is possible to run JassHelper before Wurst and so have limited support for both languages in the same map.

## Standard library:

* ClosureEvents package

## Bugfixes:

* CRITICAL BUG. Changed id-recycler back to simple stack based because of bugs.

# Version 1.1.0.7

## Language:

* It is now possible to call ExecuteFunc with a constant string argument
* It is now possible to have natives in the mapscript. Natives can be annotated with `@extern` if they should not be included in the map script.

## Tools:

* Progress bar in WurstPack is now more precise
* Running a map from Eclipse should be much faster now.


## Bugfixes:

* Fixed a small bug in the backup controller.
* Fixed handling of int-reals subtyping in jass
* Fixed bug in unit object editing (many fields where declared with type `real` but actually had `unreal`)


# Version 1.1.0.6

## Language:

* Added [stacktraces](http://peq.github.io/WurstScript/manual.html#stacktraces)

## Tools:

* Maps can now be launched from Eclipse
* war3map.j is now saved in wurst folder when compiling a map from WurstPack. This makes it possible to use constants from the map in Eclipse.
* Eclipse: Fixed bug in text-hover
* Some improvements of the REPL
* Eclipse: Improved configuration of syntax highlighting
* Eclipse: Hovering over variables, function-calls etc. now shows some nice information.

## Bugfixes:

* fixed error message, when type args could not be inferred
* several fixed related to object editing
* fixed bug in closure translation, when closure implementation had type void


## Standard Library: 

* ClosureEvents and ClosureTimers packages

# Version 1.1.0.5

## Bugfixes:

* Fixed bug with overriding+generics
* Fixed bug with `thistype`


# Version 1.1.0.4

* Fixed problems with Eclipse console
* Fixed a bug with inherited generic type params

# Version 1.1.0.3

* only internal changes

# Version 1.1.0.2

## Bugfixes:

* improved error handling in case of a compiler bug
* fixed bug in closure type calculation
* eclipse plugin: fixed problem in repl with generics and improved reconciler behavior in case of parse errors

## Standard Library: 

* LinkedList: addded closure functions


# version 1.1.0.1

## Language: 

* Added [anonymous functions and closures](http://peq.github.io/WurstScript/manual.html#lambda_expressions_and_closures)
* It is now possible to destroy an object via an interface type

## Bugfixes:

* fixed: check that abstract functions are not private

