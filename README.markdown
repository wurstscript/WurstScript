Wurstscript is a scripting language which can compile to Jass code which is used in WarCraft III.

*Currently no download is available and we do not have a planned release date*
You may however checkout the source ([HowtoSourceCode read how to get the source code compiled]) and contact us if you want to contribute.

For a description of wursts syntax and semantics read the [Manual](http://peeeq.de/wurst/manual/).

== Examples ==

=== Hello World ===
~~~~~~
package Blub
    init 
        print("Hello World")
endpackage
~~~~~~

== Principles ==
  
=== No preprocessor ===
Pscript will not have any preprocessor commands like textmacros, defines, static ifs, etc.

The reason for this is that it is hard to validate code which uses preprocessors, give good error messages, provide code completion, etc.

=== Static checks / Type safety ===
In general the compiler should detect as many errors as possible at compile time. And the generated code should generate as few silent or fatal errors as possible.

Silent errors are errors that just show some wrong behavior but no concrete error message. In jass division by zero results in a silent error as it stops the current thread without giving an error message. The compiler could fix this by adding an explicit check to every division.

Fatal errors are errors which crash the game. For example in jass this can happen when calling Player(-1). This could be avoided by adding explicit checks to each call to this function.

The compiler should also catch all errors without relying on pjass to check the generated code. If pjass finds errors in the generated code this is considered a bug in the wurstscript frontend.

=== No general purpose language ===
Pscript is no general purpose language. This means that it will/might have language constructs/features which are unique to Wc3/Jass.


== Planned Features ==

  * Nice Loops
    * [Foreach_Loop foreach]
    * [While_Loop while]
    * [For_Loop for]
  * [ClassesAndInterfaces classes and interfaces]
  * [Modules] Modules for inheritance
  * [Interfaces] for subtyping and polymorphism
  * [PrivatePublicDeclarations package based code organizing]

== Maybe-Features ==
  
  * [ImmutableClasses Immutable classes which are garbage collected]
  * [Closures nested methods (closures)]
  * precise wait
  * generics

=== Optimizations ===
The compiler will include some [Optimization]s which can be activated.
