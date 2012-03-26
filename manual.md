---
layout: default
title: WurstScript Manual
---


_by peq & Frotty_ _Version: 0.0002_ 


WurstScript is a programming language named after the german word for sausage.

The sausage is a symbol for encapsulation (Peel/Pelle), compactness (sausage meat/Brät) and modularization (cut it into slices!). And because you normally know whats inside a sausage the project is also open source and easy to use (cook).

Also remember that WurstScript and its related tools are in a probably unstable state and under heavy development, so you may encounter errors and bugs we don't know about. Please report any
problem with our [issue tracker at GitHub](https://github.com/peq/WurstScript/issues/new).

# Basics


Wurst code is organized into packages. All your wurst code has to be inside a _package_. 
Packages can also _import_ other packages to use variables, functions, classes, etc. from the imported package. Packages can have a _init_ block to do stuff when the map script is loaded.


	package HelloWurst
		// you can import stuff from other packages:
		import PrintPackage
	
		// the init block is called at map start
		init
			/* call a function */
			print("Hello Wurst!")

	endpackage

You can still use normal jass code outside of packages, but inside packages you have to adhere
to the wurst rules.

## Functions

A _function_ definition consists of a name, a list of formal parameters and a return 
type. The return type is declared after the formal parameters using the _returns_ keyword.
If the function does not return a value this part is ommitted.

	// this function returns the maximum of two integers
	function max(int a, int b) returns int
		if a > b
			return a
		else
			return b
			
	// this function prints the maximum of two integers
	function printMax(int a, int b)
		print(max(a,b).toString())

## Variables

Global (local) variables can be declared anywhere in a package (function). 
A constant value may be declared using the _val_ keyword. Variables are declared
by writing the type of the variable before its name.

	// declaring a constant - the type is inferred from the inital expression
	val x = 5
	// declaring a variable
	int y = 7



With these basic concepts you should be able to do anything you already know for jass.
The syntax is a little bit different of course, but this is covered in the next chapter.

# Syntax
The WurstScript Syntax uses indention to define Blocks rather than using curly
braces (as in Java) or keywords like 'endif' (as in Jass).

In general WurstScript tries to avoid using symbols as much as possible to
provide a clear and readable look. 

## Expressions

Semi-Formal syntax:

	Expr ::= 
		  Expr + Expr       
		| Expr - Expr   
		| Expr / Expr       // real division
		| Expr div Expr     // integer division
		| Expr % Expr       // real modulo
		| Expr mod Expr     // integer modulo
		| Expr and Expr     
		| Expr or Expr
		| Expr < Expr 
		| Expr <= Expr 
		| Expr > Expr 
		| Expr >= Expr 
		| Expr == Expr 
		| Expr != Expr 
		| - Expr            
		| not Expr 		
		| IDENTIFIER // variable access
		| IDENTIFIER(Expr, Expr, ...) // function call 	
		| Expr . IDENTIFIER // member variable		
		| Expr . IDENTIFIER(Expr, Expr, ...) // member function		

An _IDENTIFIER_ is a name of a variable or function. It may start with letters and may
contain letters, numbers and underscores. 

The definition above does not show calls to generic functions. These will be handled in 
a separate chapter about generics.


## Statements


### Ifs


	if x > y 
		... // the content inside the block simply has to be indent
	else if x < y // closing the if, opening the elseif-block
		...
	else
		...
	// Closing the if by indenting back

	if x > y or x <= z and "blub" != "blah"
		...


### Loops

	for int i = 0 to 10 // for-loop
		...

	for int i = 0 to 10 step 2 // for-loop with step 2
		...

	for int i = 10 downto 0 // wurst can also count downwards


	while a > b // while-loop with input condition
		...




	for unit u in someGroup // loop over all units in a group
		...

	for unit u from someGroup // loop over all units in group and remove the units from the group
		...

	for int i in myList
		...


#### For-in/from Loops

The for-in loop lets you iterate over any object which provides an iterator. 
A for-in loop can be transformed into an equivialent while-loop very easily:


	for A a in b
		Statements
	
	// is equivalent to:
	val iterator = b.iterator()
	while iterator.hasNext()
		A a = iterator.next()
		Statements*
	iterator.close()


Note that iterator.close() will also be called before any return statement inside the body of the while loop.

If you already have an iterator or want to access further functions of the iterator you can use the for-from loop.
The translation is very similar:

	for A a from i
		Statements
	
	// is equivalent to:
	while i.hasNext()
		A a = i.next()
		Statements


Note that you have to close the iterator i yourself.

So how do you write your own iterator? Just add a function "hasNext" which returns a boolean and a function "next" which
returns the next element for your type and you have an iterator which can be used in for-from loops.

To make a type usable in for-in loops you have to provide a function "iterator" which returns an iterator. Such an iterator
should also provide a close functions which clears all ressources allocated by the iterator. Most often the iterator just
destroys itself in the close function.

### Assignment Shorthands

	
WurstScript supports the following shorthands for assignments:
	
	i++         // i = i + 1
	i--         // i = i - 1
	x += y      // x = x + y
	x -= y      // x = x - y
	x *= y      // x = x * y
	x /= y      // x = x / y



## Functions ##

	function foo() // parentheses instead of "takes", "returns nothing" optional
		...

	function foo2( unit u ) // parameters
		RemoveUnit( u )

	function bar( integer i ) returns int // "returns" type
		return i + 4
	
	function blub() returns int // without parameters
		return someArray[5]

	function foobar()
		int i // no "local" keyword
		i = i + 1 // no "set" keyword
		int i2 = i // support for locals anywhere inside a function









# Classes

Classes are easy, powerful and very helpful constructs. A _class_ defines data and related functions working with this data. Take a look at this small example:



	Caster dummyCaster = new Caster(200.0, 400.0)
	dummyCaster.castFlameStrike(500.0, 30.0)
	destroy dummyCaster		


In this example we created a Caster named "dummyCaster" at the location(200, 400). 
Then we ordered dummyCaster to cast a flame strike at another position and finally we destroyed "dummyCaster".

This example shows you how to create a new object (line 1), invoke a function on an object (line 2) and how to destroy an object (line 3).
But how can you define a new objecttype like "Caster"? This is where classes come in. A class defines a new kind of object.
A class defines variables and functions, which every instance of this class should understand.
A class can also define how a new object is constructed ("construct") and what should happen, when it is destroyed ("ondestroy").

Defining a caster-class might look like this:


	class Caster // opening the class-block. "Caster" is the name of the class
		unit u // classvariables can be defined anywhere inside a class
	
		construct(real x, real y)
			u = CreateUnit(...)
	
		function castFlameStrike(real x, real y)
			UnitIssueOrder(u, ...)
	
		ondestroy
			KillUnit(u)



## Constructors

WurstScript allows you to define your own constructors for each class. A constructor
is a function to _construct_ a new instance of a class.
The constructor is called when creating the class via the _new_ keyword and allows operations being done to the classinstance before returning it.


	class Pair
		int a
		int b

		construct( int a, int b )
		    this.a = a
		    this.b = b

		function add() returns int
		    return a + b


	function test()
		Pair p = new Pair(2,4)
		int sum = p.add()
		print(sum)




In this example the constructor takes two integers a and b as parameters and sets the class variables to those. 
You can define more than one constructor.


	class Pair
		int a
		int b

		construct( int a, int b )
		    this.a = a
		    this.b = b
	
		construct( int a, int b, int c )
		    this.a = a
		    this.b = b
			a += c
			b += c

	function test()
		Pair p = new Pair(2,4)
		Pair p2 = new Pair(3,4,5)




In this example the class pair has two constructors - one taking 2 and the second one taking three parameters.
Depending on parameter-type and -count Wurst automatically decides which contructor to take when using "new".

## This 

The _this_ keyword refers to the current instance of the class on which the function was called. This also allows us to name the parameters the same as the class variables.

## ondestroy

Each class can have one _ondestroy_ block. This block is executed before the instance is destroyed.
Ondestroy blocks are defined as previously shown


	class UnitWrapper
		unit u
	
		...
	
		ondestroy
			RemoveUnit(u)



## Static Elements

You can declare variables and functions as _static_ by writing the keyword "static" in front of the declaration. Static variables and functions belong to the class whereas
other elements belong to instances of the class. So you can call static functions without having an instance of the class.


	class Terrain
		static real someVal = 12.
	
		static function getZ( real x, real y ) returns real
			...
	
	function foo()
		real z = Terrain.getZ( 0, 0 ) // call with $Classname$.$StaticFunctionName$()
		real r = Terrain.someVal // Same with members


## Visibility Rules 

By default class elements are visible everywhere. You can add the modifiers "private" or "protected" in front of a variable or function definition to restrict its visibility.
Private elements can only be seen from within the class. Protected elements can be seen within the enclosing package.



# Interfaces 


	interface Listener
		function onClick()

		function onAttack( real x, real y ) returns boolean
	

	class ExpertListener implements Listener
		function onClick()
			print("clicked")

	
		function onAttack( real x, real y ) returns boolean
			AddSpecialEffect(EFFECT_STRING, x ,y)



An _interface_ is a group of related functions with empty bodies.
If a class implements a certain interface it has to replace all the empty functions from the interface.
A class can _implement_ multiple interfaces, meaning that it complies to more interface requirements at the same time.

	class ExampleClass implements Interface1, Interface2, ...
		    // all functions from the implemented interfaces


With interfaces (and modules if implicit) you can now up- and downcast any Class that implements it.
This is especially useful for saving all instances from classes that inherit 1 interface in only 1 List/Array

# Generics

Generics make it possible to abstract from specific types and only program with placeholders
for types. This is especially useful for container types (e.g. Lists) where we do not want to code a
ListOfA, ListOfB, ListOfC for every class A, B, C which we need a list for.

With generics we can instead write only one implementation for lists and use it with all types.
Generic type parameter and arguments are written in angled brackets (< and >) after the identifier.


	// a generic interface for Sets with type parameter T
	interface Set<T>
		// adds an element to the set
		function add(T t)
		
		// removes an element from the set
		function remove(T t)
		
		// returns the number of elements in the set
		function size() returns int
		
		// checks wether a certain element is in the set
		function contains(T t) returns boolean
		
	class SetImpl<T> implements Set<T>
		// [...] implementation of the interface

If we have a class defined like this, we can then use it with a concrete type (e.g. Missile):

	// create a set of missiles
	Set<Missile> missiles = new SetImpl<Missile>();
	// add a missile m
	missiles.add(m);

Generic parameters in Wurst can be bound to integers, class types and interface types but 
not to other native types or to tuple types.


# Modules

A _module_ is a small packages which provides some functionality for classes. Classes can _use_ modules to inherit the functionality of the module.

You can use the functions from the used module as if they were declared in the class. You can also _override_ functions defined in a module to adjust its behavior.

If you know object oriented languages like Java or C#: Modules are like abstract classes and using a module is like inheriting from an abstract class but *without the sub-typing*. (Wurstscript takes a different approach to enable polymorphism, but this is not implemented yet)

## Example 1 

In this example we just have a class which uses a module A. The resulting program behaves as if the code from module A would be pasted into Class C.


	module A
		public function foo()
		    ...


	class C
		use A


## Example 2

Modules are more than just a mechanism to copy code. Classes and modules can override functions defined in used modules:


	// a module to store an integer variable
	module IntContainer
		int x

		public function getX() returns int
		    return int

		public function setX(int x)
		    this.x = x

	// a container which only stores positive ints
	module PositiveIntContainer
		use IntContainer
		
		// override the setter to only store positive integers
		override function setX(int x)
		    if x >= 0
		        IntContainer.setX(x)


## Visibility & Usage Rules

 * Variables of modules are always private
 * private functions are only usable from the module itself
 * each function of a module must be declared public or private
 * if a class uses a module it inherits only the public functions of the module
    * you can use a module with *private* (not implemented yet). This will let you use the functionality of the module without exposing its functions to the outside.




## Overriding Functions

 * You can *override* functions which are defined in used modules by writing the override keyword in front of a function.
 * If two modules are used, which have the same function, it *must* be overridden by the underlying class or module in order to avoid ambiguousness (of course this is only possible if the function signatures match. We are thinking about a solution for this)
 * private functions cannot be overridden

## Abstract Functions

Modules can declare abstract functions: Functions without a given implementation. Abstract functions have to be implemented by the underlying classes.

## Thistype

You can use _thistype_ inside a module to refer to the type of the class which uses the module. This can be useful if you need to cast the class to an integer and back.

# Tuple Types 

With _tuple_ types you can group several variables into one bundle. This can be used to return more than one value from a function, to create custom types and of course for better readability.

Note that tuples are not like classes. There are some important differences:
- You do not destroy tuple values.
- When you assign a tuple to a different variable or pass it to a function you create a copy of the tuple. Changes to this copy will not affect the original tuple.
- Tuple types cannot be bound to type parameters, so you can not have a List{vec} if vec is a tuple type.
- As tuple types are not created on the heap you have no performance overhead compared to using single variables.


	// Example 1:

		// define a tuple
		tuple vec(real x, real y, real z)

		init
			// create a new tuple value
			vec v = vec(1,2,3)
			// change parts of the tuple
			v.x = 4
			// create a copy of v and call it u
			vec u = v
			u.y = 5
			if v.x == 4 and v.y == 2 and u.y == 5
				testSuccess()


	// Example 2:

		tuple pair(real x, real y)
		init
			pair p = pair(1,2)
			// swap the values of p.x and p.y
			p = pair(p.y, p.x)
			if p.x == 2 and p.y == 1
				testSuccess()



# Extension Functions

Extension functions enable you to "add" functions to existing types without creating a new derived type, recompiling, or otherwise modifying the original type. Extension functions are a special kind of static function, but they are called as if they were instance functions on the extended type.

## Declaration

	public function TYPE.EXTFUNCTIONNAME(PARAMETERS) returns ...
		BODY
		// The keyword "this" inside the body refers to the instance of the extended type

## Examples 


	// Declaration
	public function unit.getX() returns real
		return GetUnitX(this)

	// Works with any type
	public function real.half() returns real
		return this/2

	// Parameters
	public function int.add( int value )
		return this + value
	 
	// Usage
	unit u = CreateUnit(...)
	...
	print( u.getX().half() )

	// Also classes, e.g. setter and getter for private vars
	public function BlubClass.getPrivateMember() returns real
		return this.privateMember




# Packages 
As mentioned above every codesegment written in Wurst has to be inside a _package_,
packages define the code organization and separate namespaces.
Packages can also have global variables - every variable that is not inside another block (function/class/module)
is declared global for that package.
Packages can import other packages to access classes, functions, variables, etc. that are not defined private.


	// declaring
	package $PackageName$
		...
	endpackage
	// importing
	package Blub
		import SomePackage
		import AnotherPackge // importing more than 1 package
		import MyExternalWurstFile // Import a scriptfile from the class-lib directory 
	endpackage


When importing an external Scriptfile you just write the Filename without .wurst.
Remember to name the package inside your file the same as the name of the scriptfile.

## Public declarations
All members of a package ar private by default.
If you want to make them visible inside packages that import that package you have to add the keyword "public".

## Constants
You can declare a variable constant to prohibited changes after initialization.
This has no impact on the generated code but throws an error when trying to compile.



### Examples

	package First
		int i // (private by default) Visible inside the package
		public int j // public, gets exported
	endpackage

	package Second
		import First
	
		int k = i // Error
		int m = j // Works, because j is public
	endpackage


	package Foo
		constant int myImportantNumber = 21364 // has to be initialized with declaration

		function blub()
			myImportantNumber = 123 // Error
		
		private constant int myPrivateNumber = 123 // Correct keyword order
	endpackage






## Init blocks
Another package feature are init blocks.
Every package can have one init block anywhere inside it. 
All operations inside the init block of a package are being executed at mapstart. 

At the beginning of an init block you can assume that all global variables inside the 
current package are initialized.

If a given package A imports package B, the initializer in package B is run before A's.
If packages import each other, the order is undefined.


*Note:* Since wc3 has a micro op limitation, too many operations inside initblocks may stop it from fully executing. In order to avoid this you should only place map-init Stuff inside the init blocks and use timers and own inits for the other stuff.





# Standard Library 

Wurst comes with a library of some useful standard functions

## Collections 

### Lists 

Suppose you have an interface Closable and classes Door, Chest and Gate  implementing this interface. Furthermore the interface I
provides functions "open" and "close". You can use Lists to store different kind of Closable things in one place.


	Gate gate = ...
	Door door = ...
	Chest chest = ...
	// create a new list
	LinkedList{Closeable} closeables = new LinkedList{Closeable}()
	// add the different items to the list
	closeables.add(gate)
	closeables.add(door)
	closeables.add(chest)
	// now close everything in the list
	for Closeable c1 in closeables
		c1.close()
	// and open everything again (but not the chest, as we want to keep our treasure)
	closeables.remove(chest) // remove the chest from the list
	for Closeable c2 in closeables
		c2.open()



# vJass vs Wurst

## Feature table

<table><tr><td> <strong>Feature</strong> </td><td> <strong>vJass</strong> </td><td> <strong>Wurst</strong> </td><td></td></tr>
<tr><td> <strong>code organization</strong> </td><td> libraries </td><td> packages </td><td></td></tr>
<tr><td> <strong>nested scopes</strong> </td><td> yes </td><td> -  </td><td></td></tr>
<tr><td> <strong>classes</strong> </td><td> structs </td><td> classes </td><td></td></tr>
<tr><td> <strong>modules</strong> </td><td> yes </td><td> yes </td><td></td></tr>
<tr><td> <strong>delegate</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>function interfaces</strong> </td><td> yes </td><td> <em>not yet</em> </td><td></td></tr>
<tr><td> <strong>interfaces</strong> </td><td> yes </td><td> <em>not yet</em> </td><td></td></tr>
<tr><td> <strong>textmacros</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>keyword</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>struct onInit</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>stub methods</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Dynamic arrays</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Array members</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Delegate</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Array structs</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Keys</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Sized arrays</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>2D arrays</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Structs with more index space</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Dynamic arrays with more index space</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Colon</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Operator overloading</strong> </td><td> limited </td><td> <em>not yet</em> </td><td></td></tr>
<tr><td> <strong>hook</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>inject</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Loading structs from SLK files</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Script optimization</strong> </td><td> only inlining </td><td> yes </td><td></td></tr>
<tr><td> <strong>Function inlining</strong> </td><td> limited </td><td> <em>not yet</em> </td><td></td></tr>
<tr><td> <strong>External tools</strong> </td><td> yes </td><td> - </td><td></td></tr>
<tr><td> <strong>Typechecker</strong> </td><td> limited </td><td> yes </td><td></td></tr>
<tr><td> <strong>Function sorting</strong> </td><td> - </td><td> yes </td><td></td></tr>
<tr><td> <strong>Extension functions</strong> </td><td> - </td><td> yes </td><td></td></tr>
<tr><td> <strong>generics</strong> </td><td> - </td><td> <em>planned</em> </td><td></td></tr>
<tr><td> <strong>tuple types</strong> </td><td> - </td><td> <em>planned</em> </td><td></td></tr>
<tr><td> <strong>closures</strong> </td><td> - </td><td> <em>planned</em> </td><td></td></tr>
<tr><td> <strong>compiletime functions</strong> </td><td> - </td><td> <em>planned</em> </td><td></td></tr>
</table>


## Globals 

Instead of a globals block, every variable outside a block inside a package is considered a global in that package.

_*vJass:*_

	globals
		integer foo
	endglobals


_*Wurst:*_

	package test
		...
		int foo
	endpackage


## Functions

_*vJass:*_

	function foo takes nothing returns nothing

	endfunction

	function bar takes real r returns real

	endfunction


_*Wurst:*_

	function foo()
		...

	function bar(real r) returns real
		...


## Locals 

locals can be declared and initialized anywhere inside a function.

_*vJass:*_

	function foo takes nothing returns nothing
		local integer i = 0
		local integer j
		i = i + 1
		j = i
	endfunction


_*Wurst:*_

	function foo()
		int i = 0
		i++
		int j = i


## Librarys/Scopes 

Namespaces/code organisation in wurst is handled by packages.
All code in Wurst has to be inside a package.

_*vJass:*_

	library foo

	endlibrary


_*Wurst:*_

	package foo

	endpackage


### importing 

_*vJass:*_

	library foo requires/needs/uses bar, blub, stuff

	endlibrary


_*Wurst:*_

	package foo
		import bar
		import blub
		import stuff

	endpackage


### init-blocks 

_*vJass:*_

	library foo initializer init

		private function init takes nothing returns nothing
		    ...
		endfunction

	endlibrary

_*Wurst:*_

	package foo
		
		init
		    ...
	endpackage



## Structs / Classes 

_*vJass:*_

	struct BlubStruct
		static method create takes nothing returns BlubStruct
		    local thistype this = thistype.allocate()
		    // constructor code
		    return this
		endmethod

		method add takes integer a, integer b returns integer
		    return a + b
		endmethod

		static method sub takes integer a, integer b returns integer
		    return a - b
		endmethod

		method onDestroy takes nothing returns nothing
		    // destructor code
		endmethod
	endstruct

	// create instance:
	local BlubStruct blub = BlubStruct.create()
	// call method
	call blub.add( 1, 1 )
	// call static method
	call BlubStruct.sub( 2, 1 )
	// destroy instance
	call blub.destroy


_*Wurst:*_

	class BlubClass
		construct()
		    // constructor code

		function add(int a, int b) returns int
		    return a + b

		static function sub(int a, int b) returns int
		    return a - b

		ondestroy
		    // destructor code

	// create instance:
	BlubClass blub = new BlubClass()
	// call function
	blub.add( 1, 1 )
	// call static function
	BlubClass.sub( 2, 1 )
	// destroy instance
	destroy blub



## Modules 

_*vJass:*_

	module Bla
		...
	endmodule

	struct C
		implement Bla
		...
	endstruct


_*Wurst:*_


	module Bla
		...


	class C
		use Bla
		...



Wurst-modules are also more powerful than vJass modules. They allow overriding and can declare abstract functions.

# Optimizer

The Wurstcompiler has a build-in scriptoptimizer which will, when enabled, optimize the Jass code in various ways.
Jass optimization got very important to provide playable framerates when using very enhanced and complex systems.
On the one hand the optimizer cleans the code, making it smaller in size and removing useless stuff in order to reduce RAM-usage.
On the other hand it also has some optimizations to increase the speed of execution and performance of the code.

## Cleaning


Stuff that is being removed, changed or not even printed

* Comments
* Unneeded White-spaces
* Excessive parentheses 
* number shortening (0.0 -> 0., dec -> hex)
* Replace all "Condition" with "Filter"
* Some useless Jassconstants replaced with "null"

## Name compression

Smaller names execute faster, so all the names of functions and variables are being compressed to the shortest name possible.

If names are used in ExecuteFunction or TriggerRegisterVariableEvent they start with a 'z' and are only lowercase.

Functionnames that are used within EF or TRVE only get replaced when there is only a constant string inside of EF or TRVE. If there is some function called that returns a string e.g. it won't be touched and all functions that contain the constant string won't be compressed either.

## Inlining

Inlining is not an easy task, but brings great performance boosts to systems with much overhead/1-line functions. 

There may be forced inlining one day.

## Removing GUI Elements

There probably will be some module that removes/optimizes some GUI functions and stuff.
Though because Wurst is not supposed to be mixed with GUI/normal Jass especially when programming in WurstEdit,
this has a low priority.

