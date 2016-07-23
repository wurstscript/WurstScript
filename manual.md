---
layout: simple
title: Manual
---


_by peq & Frotty_ 


WurstScript (short Wurst) is a programming language named after the German word for sausage.

The sausage is a symbol for encapsulation (Peel/Pelle), compactness (sausage meat/Brät) and modularization (cut it into slices!). And because you normally know whats inside a sausage the project is also open source and easy to use (cook).

**Remember**: WurstScript and its related tools are in a probably unstable state and under heavy development, so you may encounter errors and bugs we don't know about. Please report any
problem with our [issue tracker at GitHub](https://github.com/peq/WurstScript/issues/new).

*Note*: WurstScript is written in Java and should therefore be usable on Windows, OS/X and most Linux Distributions. 
This applies only to the compiler & the eclipse plugin, because the Wurstpack is based on the Jass New Gen Pack (and therefore windows-only).

<div id="tableofcontents">loading TOC ...</div>


# Philosophy

WurstScript aims at a fast and easy work-flow with comfort- and safety features.
The execution speed is not the highest priority (even though it is pretty fast, an optimizer is included in the compiler), but instead ease of use
and stress-free map-development.
It should be easy to use and learn (especially with knowledge of (v)Jass) to be beginner-friendly and also understandable to non-Jass users.

While we know that WurstScript won't replace vJass in the WC3 mapping scene (also because of the tons of vJass scripts that can't be simply ported) we still hope it will be a very good alternative, in particular for users that are trying to learn Jass.

### Need help?

If you have any questions regarding Wurst-related tools or the language itself, feel free to write us a message.

You can contact us on Hive:

* [Frotty](http://www.hiveworkshop.com/forums/members/frotty/)
* [peq](http://www.hiveworkshop.com/forums/members/peq/)

Or visit the [IRC channel we usually hang out](http://webchat.quakenet.org/?channels=inwc.de-maps).





# Syntax
The WurstScript Syntax uses indention to define Blocks, instead of using curly
brackets (as in Java) or keywords like 'endif' (as in Jass). Indentation must use tabs or 4 spaces, but not mix both styles in the same file. 
In the following we use the word "tab" to refer to the tab character or to 4 space characters.


	// Some language using curly brackets (e.g. Java or Zinc)
	if condition 
	{
		ifStatements
	}
	nextStatements
	// Some language using keywords (e.g. Jass)
	if condition
		ifStatements
	endif
	nextStatements
	// Some language using indention (e.g. WurstScript or phyton)
	if condition
		ifStatements
	nextStatements

A block has to be indented by one or more tabs. Using spaces for indentation is not permitted.

In general newlines come at the end of a statement, with some exceptions:

- A newline is ignored after a line ending with `(` or `[`
- A newline is ignored before a line beginning with `)`, `]`,`.` or `..`
- A newline is ignored, when one of the following tokens comes before or after the newline: 
    `,`, `+`, `*`, `-`, `div`, `/`, `mod`, `%`, `and`, `or`, `->`

You can use this to break longer expressions or long parameter lists over several lines, or to chain method invocations:

		someFunc(param1, param2,
			param3, param4)

		someUnit..setX(...)
			..setY(...)
			..setName(...)


In general WurstScript tries to avoid using symbols as much as possible to
provide a clear and readable look. At the same time it is less verbose than Jass.


# Basics

Wurst code is organized into _packages_. All your wurst code has to be inside a _package_. 
Packages can also _import_ other packages in order to use variables, functions, classes, etc. from the imported package. 
Packages can have an _init_ block that is executed when the map is loaded.


	package HelloWurst
		// you can import stuff from other packages:
		import PrintingHelper
        // NOTE: Since the Wurst.wurst-Update this import isn't needed anymore.
	
		// the init block is called at map start
		init
			/* call a function */
			print("Hello Wurst!")

	endpackage

For more information about packages, read the packages section. 
You can still use normal Jass syntax/code outside of packages(when using WurstWE, those will be parsed by PJass), but inside packages you have to adhere
to the wurst rules.

## Naming Conventions

Wurst enforces several naming conventions to create a common way of writing code and to provide general readability rules:
-  **Functions** have to start with a **lowercase letter**
-  **Variables** have to either start with a **lowercase letter** *or* be **all uppercase letters and "_"**
-  **Class/Module/Interface/Package** names have to start with an **uppercase letter**
-  **Tuplenames** have to start with a **lowercase letter**



## Functions

A _function_ definition consists of a name, a list of formal parameters and a return 
type. The return type is declared after the formal parameters using the _returns_ keyword.
If the function does not return a value this part is omitted.

	// this function returns the maximum of two integers
	function max(int a, int b) returns int
		if a > b
			return a
		else
			return b
			
	// this function prints the maximum of two integers
	function printMax(int a, int b)
		print(max(a,b).toString())

	function foo() // parentheses instead of "takes", "returns nothing" obsolete.
		...

	function foo2( unit u ) // parameters
		RemoveUnit( u )

	function bar( int i ) returns int // "returns" [type]
		return i + 4
	
	function blub() returns int // without parameters
		return someArray[5]

	function foobar()
		int i // local variable
		i = i + 1 // variable assignment
		int i2 = i // support for locals anywhere inside a function

## Variables

Global (local) variables can be declared anywhere in a package (function). 
A constant value may be declared using the _constant_ or _let_ keyword. _let_ is for local variables and
_constant_ is for global variables.
Mutable variables are declared by using the _var_ keyword or by writing the type of the variable before its name.

	// declaring a constant - the type is inferred from the initial expression
	constant x = 5
	// The same but with explicit type
	constant real x = 5
	// declaring a variable - the inferring works the same as 'let', but the value can be changed
	var y = 5
	// declaring a variable with explicit type
	int z = 7
	// declaring an array
	int array a
    
    // inside a function
    function getUnitInfo( unit u )
        player p = u.getOwner()
        var name = u.getName()
        print( name )
        real x = u.getX()
        real y = u.getY()
		let sum = x + y
        
        
With these basic concepts you should be able to do anything you already know for Jass.
The syntax is a little bit different of course, but this is covered in the next chapter.


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
		| IDENTIFIER 				// variable access
		| IDENTIFIER(Expr, Expr, ...) 		// function call 	
		| Expr . IDENTIFIER 			// member variable		
		| Expr . IDENTIFIER(Expr, Expr, ...) 	// member function	
		| Expr .. IDENTIFIER(Expr, Expr, ...) 	// member function, same as single dot
		                                        // but returns the receiver type
		| new IDENTIFIER(Expr, Expr, ...) 		// constructor call
		| destroy Expr                  // destroy object
		| Expr castTo Type				// casting
		| Expr instanceof Type			// instance checking	
		| begin
		    Statements
		  end // statement expr
		| (param1, param2, ...) -> Expr  // anonymous function
		| (Expr)                        // parantheses
		

An _IDENTIFIER_ is a name of a variable or function. It may start with letters and may
contain letters, numbers and underscores. 

**Note**: The definition above does not show calls to generic functions. These will be handled in 
a separate chapter about generics.


### Cascade operator (dot-dot-operator)

Use the cascade operator `..` is similar to the normal `.` operator and can be used for calling methods, but instead of returning the result
of the called method, the cascade operator returns the receiver. This makes it possible to perform a number of operations on the same object.
Here is a small example

	CreateTrigger()
		..registerAnyUnitEvent(EVENT_PLAYER_UNIT_ISSUED_ORDER)
		..addCondition(Condition(function cond))
		..addAction(function action)
		
The above code is basically equivalent to:

	let temp = CreateTrigger()
	temp.registerAnyUnitEvent(EVENT_PLAYER_UNIT_ISSUED_ORDER)
	temp.addCondition(Condition(function cond))
	temp.addAction(function action)



## Statements

### Skip

The simplest statement is the _skip_ statement. It has no effect and can be used to create empty blocks.

### If


	if x > y 
		... // the content inside the block simply has to be indent
	else if x < y // closing the if, opening the elseif-block
		...
	else
		...
	// Closing the if by indenting back

	if x > y or x <= z and "blub" != "blah"
		print("if is true")
	print("if done.")
	
	if GetSpellAbilityId() == 'A000'
		AddSpecialEffect( GetSpellTargetX(), GetSpellTargetY(), FX_PATH )
    
### Switch
    
    // i is of type int
    switch i
        case 1
            print("1")
        case 3
            print("3")
        case 88
            print("88")
        default
            print("not implemented")
            
As you see in the example, a switch statement is basically a nicer syntax for
nesting ifs and else ifs, with the special default case.

### Loops

    while a > b // while-loop with input condition
		...
        
	for int i = 0 to 10 // for-loop
		...

	for int i = 0 to 10 step 2 // for-loop with step 2
		...

	for int i = 10 downto 0 // wurst can also count downwards
        ...

	for unit u in someGroup // loop over all units in a group
		...

	for unit u from someGroup // loop over all units in group and remove the units from the group
		...

	for int i in myList
		...


In for loops you can also omit the type of the loop variable.

	for i = 0 to 10
	for u in someGroup
	...

### For-in/from Loops

The for-in loop lets you iterate over any object which provides an iterator. 
A for-in loop can be transformed into an equivalent while-loop very easily:


	for A a in b
		Statements
	
	// is equivalent to:
	let iterator = b.iterator()
	while iterator.hasNext()
		A a = iterator.next()
		Statements*
	iterator.close()


**Note** that iterator.close() will also be called before any return statement inside the body of the while loop.

If you already have an iterator or want to access further functions of the iterator you can use the for-from loop.
The translation is very similar:

	let iterator = myList.iterator()
	for segment from iterator
		//Statements
	iterator.close()
	
	// is equivalent to:
	let iterator = myList.iterator()
	while iterator.hasNext()
		segment = iterator.next()
		//Statements
	iterator.close()

**Note** that you have to close the iterator yourself.

### Iterators


You can provide Wurst with an iterator for your desired type by providing a set of functions. By providing an iterator, the type can be used in for-loops:
-  function **hasNext()** returns boolean (return if there is another object left)
-  function **next()** returns TYPE (return the next element for your type)

With this two functions you get an iterator which can be used in for-from loops.

To make a type usable in for-in loops you have to provide 

-  function *iterator()* returns Iterator

for your type, that returns the object for the iteration. 
This can either be a handle, like in the group-iterator or an object like the List-iterator.
Your iterator should also provide a close function which clears all resources allocated by it. 
Most often the iterator just destroys itself in the close function.

Look at the 2 examples from the standard library:

**Group-Iterator**

	public function group.iterator() returns group
		// return a copy of the group:
		bj_groupAddGroupDest = CreateGroup()
		ForGroup(this, function GroupAddGroupEnum)
		return bj_groupAddGroupDest

	public function group.hasNext() returns boolean
		return FirstOfGroup(this) != null

	public function group.next() returns unit
		let u = FirstOfGroup(this)
		GroupRemoveUnit(this, u) 
		return u

	public function group.close()
		DestroyGroup(this)
		
As you can see, the iterator is a group, therefore the group needs to provide the functions mentioned above.
This is done via extension functions.
		
**LinkedList-Iterator**

	public class LinkedList<T>
		...
		
		// get an iterator for this list
		function iterator() returns LLIterator<T>
			return new LLIterator(dummy)
			
	class LLIterator<Q>
		LLEntry<Q> dummy
		LLEntry<Q> current

		construct(LLEntry<Q> dummy)
			this.dummy = dummy
			this.current = dummy

		function hasNext() returns boolean
			return current.next != dummy

		function next() returns Q
			current = current.next
			return current.elem

		function close()
			destroy this
			
The LinkedList Iterator is a little different, because it's a class. Still it provides the needed functions and is therefore viable as iterator.
It also contains some members to help iterating. A new instance if LLIterator is returned from the .iterator() function of the LinkedList.

### Assignment Shorthands

	
WurstScript supports the following shorthands for assignments:
	
	i++         // i = i + 1
	i--         // i = i - 1
	x += y      // x = x + y
	x -= y      // x = x - y
	x *= y      // x = x * y
	x /= y      // x = x / y

Because these shorthands simply get translated into their equivalents, they can
be used with overloaded operators, too.

# Packages 
As mentioned above every code-segment written in Wurst has to be inside a _package_,
packages define the code organization and separate name-spaces.
Packages can also have global variables - every variable that is not inside another block (function/class/module)
is declared global for that package.

When working in WurstWE, packages have to end with the **endpackage** keyword and the code inside has to be indent.

In WurstEclipse however, the **endpackage** can be omitted when the code inside is not indented.
	
	package SomeWurstWePackage
		...
		(code)
	endpackage

	package SomeWursteclipsePackage
	...
	(code)



## Imports

Packages can import other packages to access classes, functions, variables, etc. that are defined public.


	// declaring
	package SomePackage
		...
	endpackage
	// importing
	package Blub
		import SomePackage
		import AnotherPackge // importing more than 1 package
		import MyExternalWurstFile // Import a scriptfile from the eclipseProject
		import public PackageX // public import (see below)
	endpackage


When importing an external scriptfile you just write the Filename without .wurst.
**Remember** to name the package inside your file the same as the name of the scriptfile (In the EclipsePlugin this is enforced, when working with WE only, this might be the cause of packages not being found).

### import public

By default imported names are not exported by the package. For example the following will not compile:

	package A
		public constant x = 5
	endpackage
	package B
		import A
	endpackage
	package C
		import B
		constant z = x
	endpackage


The variable x is usable in package B but it is not exported from B. So in package C we cannot use the variable x. 
We could fix this by simply importing A into C but sometimes you want to avoid those imports.
Using public imports solves this problem because they will export everything that is imported. Thus the following code will work:

	package A
		public constant x = 5
	endpackage
	package B
		import public A
	endpackage
	package C
		import B
		constant z = x
	endpackage

### The special Wurst package

By default, every package imports a package named Wurst.wurst which is defined in the standard library. This package exports some
packages which are used very often. 

If you do not want this standard import you can disable it by importing a package NoWurst. This directive is mainly used to
resolve some cyclic dependencies in the standard library.


## Public declarations
All members of a package are private by default.
If you want to make them visible inside packages that import that package you have to add the keyword "public".

## Constants
You can declare a variable constant to prohibit changes after initialization.
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
		
		public constant int myPrivateNumber2 = 123 // Correct keyword order
	endpackage






## Init blocks
Another package feature are init blocks.
Every package can have one or multiple init blocks anywhere inside it. 
All operations inside the init block of a package are executed at mapstart. 

At the beginning of an init block you can assume that all global variables inside the 
current package are initialized.

	package MyPackage
		init
			print("this is the initblock")


*Note:* Since wc3 has a micro op limitation, too many operations inside init-blocks may stop it from fully executing. In order to avoid this you should only place map-init Stuff inside the init blocks and use timers and own inits for the other stuff.


## Initialization and initlater

The initialization rules for Wurst are simple:

1. Inside a package initialization is done from top to bottom.
	The initializer of a package is the union of all global variable static initializers
	(including static class variables) and all init blocks.
2. If package A imports package B and the import is not a `initlater` import, 
	then the initializer of package B is run before A's. 
	Cyclic imports without `initlater` are not allowed.

If you get a Cyclic init dependency between packages, you have to manually define which package can be 
initialized later.
This is done by adding the keyword `initlater` to the import of the package:

	package A
	import initlater B
	import public initlater C
	import D

Here only package `D` is guaranteed to be initialized before package `A`.
Packages `B` and `C` are allowed to be initialized later.

## Configuring Packages

Global variables and functions can be configured. 
Configuration is done via configuration packages.
Each package has at most one configuration package and every configuration package configures exactly one package.
The relation between a package and its configuration package is expressed via the following naming convention: 
For a package named `Blub` the configuration package must be named `Blub_config`.

Inside a configuration global variables can be annotated with the `@config` annotation. 
This has the effect that the variable with the same name in the original package will be overridden with 
the variable in the configuration package.
In the original package, the variable should be annotated with `@configurable` to signal that it is safe to configure.
Here is an example:

	package Example
		@configurable int x = 5
	endpackage

	package Example_config
		@config int x = 42
	endpackage

Configuring functions works basically the same:

	package Example
		@configurable public function math(int x, int y) returns int
			return x + y
		
	endpackage

	package Example_config
		@config public function math(int x, int y) returns int
			return x*y

	endpackage




# Classes

Classes are easy, powerful and very helpful constructs. A _class_ defines data and related functions working with this data. Take a look at this small example:



	Caster dummyCaster = new Caster(200.0, 400.0)
	dummyCaster.castFlameStrike(500.0, 30.0)
	destroy dummyCaster		


In this example we created a Caster named "dummyCaster" at the location(200, 400). 
Then we ordered **dummyCaster** to cast a flame strike at another position and finally we destroyed **dummyCaster**.

This example shows you how to create a new object (line 1), invoke a function on an object (line 2) and how to destroy an object (line 3).
But how can you define a new object type like "Caster"? This is where classes come in. A class defines a new kind of object.
A class defines variables and functions, which every instance of this class should understand.
A class can also define how a new object is constructed (_construct_) and what should happen, when it is destroyed (_ondestroy_).

Defining a caster-class might look like this:


	class Caster // opening the class-block. "Caster" is the name of the class
		unit u // class variables can be defined anywhere inside a class
	
		construct(real x, real y)
			u = CreateUnit(...)
	
		function castFlameStrike(real x, real y)
			UnitIssueOrder(u, ...)
	
		ondestroy
			KillUnit(u)



## Constructors

WurstScript allows you to define your own constructors for each class. A constructor
is a function to _construct_ a new instance of a class.
The constructor is called when creating the class via the _new_ keyword and allows operations being done to the class-instance before returning it.


	class Pair
		int a
		int b

		construct( int pA, int pB )
		    	a = pA
		    	b = pB

		function add() returns int
		    	return a + b


	function test()
		Pair p = new Pair(2,4)
		int sum = p.add()
		print(sum)




In this example the constructor takes two integers a and b as parameters and sets the class variables to those. 
You can define more than one constructor as long as the parameters differ.


	class Pair
		int a
		int b

		construct( int pA, int pB )
		    	a = pA
		    	b = pB
	
		construct( int pA, int pB, int pC )
		    	a = pA
		    	b = pB
			a += pC
			b += pC

	function test()
		Pair p = new Pair(2,4)
		Pair p2 = new Pair(3,4,5)




In this example the class pair has two constructors - one taking 2 and the second one taking three parameters.
Depending on parameter-type and -count Wurst automatically decides which constructor to take when using "new".

## This 

The _this_ keyword refers to the current instance of the class on which the function was called. This also allows us to name the parameters the same as the class variables.
However it can be left out in classfunctions, as seen above.

	class Pair
		int a
		int b

		// With the this keyword we can access the classmembers
		construct( int a, int b )
		    	this.a = a
		    	this.b = b


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
		
		static int array someArr
	
		static function getZ( real x, real y ) returns real
			...
	
	function foo()
		real z = Terrain.getZ( 0, 0 ) // call with $Classname$.$StaticFunctionName$()
		real r = Terrain.someVal // Same with members
		real s = Terrain.someArr[0]
		
## Dynamic, Sized Array-Members

Wurstscript supports sized arrays as classmembers by translating it to SIZE times arrays and then resolve the array in a get/set function via binary search.

Example Usage:

	class Rectangle
		Point array[4] points
		
		function getPoint(int index)
			return points[index]
			
		function setPoint(int index, Point nP)
			points[index] = nP


## Visibility Rules 

By default class elements are visible everywhere. You can add the modifiers _private_ or _protected_ in front of a variable or function definition to restrict its visibility.
Private elements can only be seen from within the class. Protected elements can be seen within the enclosing package and in subclasses.

## Subclassing

A class can _extend_ an other class. The class then inherits all the non-private functions and variables from that class
and can be used anywhere where the super class can be used. 

Constructors of the class have to specify how the super class should be constructed. This is done using a _super_ call, 
which defines the arguments for the super constructor. There can not be any statement before this call.

If a constructor does not provide a super call, the compiler tries to insert a super call with no arguments. 


Functions inherited from super classes can be overridden in the subclass. Such functions have to be annotated with _override_.
    
          
### Example

	class Missile 		
		construct(string fx, real x, real y)
			// ...
		
		function onCollide(unit u)
			skip
	
		// ...
		
	// a fireball missile is a special kind of missile
	class FireballMissile extends Missile	
		// we construct it using a fireball fx
		construct(real x, real y)
			super("Abilities\\Weapons\\RedDragonBreath\\RedDragonMissile.mdl", x, y)
		// a fireball does some special things when it collides
		// with a unit, so we override the onCollide method
		override function onCollide(unit u)
			// create a big explosion here ;)
			//...
			
			
## Typecasting

In order to typecast, you use the _castTo_ operator

You need typecasting for several reasons.

One being to save class instances and for example attaching them onto a timer, like done in TimerUtils.wurst
This process can also be reversed (casting from int to a classtype).


	class Test
		int val
		
	init
		Test t = new Test()
		int i = t castTo int



Typecasting is sometimes useful when using subtyping. If you have an object of static type A but know 
that the dynamic type of the object is B, you can cast the object to B to change the static type.

			
	class A
	
	class B extends A
		function special()
			...
	
	init
		A a = new B()
		// we know that a is actually of type B, so we can safely cast it to B:
		B b = a castTo B
		// now we can call functions from class B
		b.special()



_Note_: You should avoid castTo whenever possible. Casts are not checked at runtime so they can go horribly wrong. 
Casts can be avoided by using high level libraries and object oriented programming.

		
## Dynamic dispatch

Wurst features dynamic dispatching when accessing classinstances.
What this means is simple: If you have a classinstance of a subclass B, casted into a variable of
the superclass A, calling a function with that reference will automatically call the overridden function from
the original type.
It is easier to understand with an example:

###Example 1

	class A
		function printOut()
			print("I'm A")
			
	class B extends A
		override function printOut()
			print("I'm B")
			
	init
		A a = new B()
		a.printOut()
		// this will print "I'm B", even though it's a type A variable
		
### Example 2

    class A
        string name
        
        construct(string name)
            this.name = name
            
        function printName()
            print("Instance of A named: " + name )


    class B extends A
    
        construct(string name)
            super(name)
            
        override function printName()
            print("Instance of B named: " + name )
            
    init 
        A a = new B("first") // This works because B extends A
        a.printName() // This will print "Instance of B named: first", because a is an Instance of B.
        
This is especially useful when iterating through ClassInstances of the same supertype,
meaning you don't have to cast the instance to it's proper subtype.

## super calls

When you override a method you can still call to the original implementation by using the *super* keyword. This is useful since
subclasses often just add some functionality to its base classes.

As an example consider a fireball spell for which we want to create a more powerful version:

	class FireBall
		...
		function hitUnit(unit u, real damage)
			...

	class PowerFireBall extends FireBall
		...
		// here we override the original behavior of the hitUnit function
		override function hitUnit(unit u, real damage)
			// first we create a big explosion effect
			createSomeBigExplosionEffect(u)
			// then we call the original hitUnit function but with doubled damage
			super.hitUnit(u, damage*2)

The **super** keyword also should be used when defining custom constructors. The Constructor of the superclass has to be called in the constructor of the subclass. When no super constructor call is given, the compiler will try to call a constructor with no arguments. If no such constructor exists, then you will get an error like: "Incorrect call to super constructor: Missing parameters."

	class Missile
		...
		construct(vec2 position)
			...

	class TimedMissile extends Missile
		...
		construct(vec2 position, real duration)
			// Here the constructor of the superclass is called
			// The super statement has to be the first statement in the constructor of the subclass
			super(position)
			...


## instanceof

In Wurst you can check the type of a classinstance with the _instanceof_ keyword.

_Note_: You should avoid instanceof checks whenever possible and prefer object oriented programming.

The instanceof expression "o instanceof C" returns true, if object o is a subtype of type C.

It is not possible to use instanceof with types from different type partitions, as instanceof 
is based on typeIds (see following chapter). This is also the reason why you cannot use instanceof 
to check the type of an integer.

The compiler will try to reject instanceof expressions, which will always yield true or always yield false.

###Example

	class A
	
	class B extends A

	init
		A a = new B()
		
		if a instanceof B
			print("It's a B")
			
## typeId

*NOTE*: typeIds are an experimental feature. Try to avoid using them.

Sometimes it is necessary to check the exact type of an object. To do this you can write "a.typeId" if a is an object or "A.typeId" if A is a class.

		// check if a is of class A
		if a.typeId == A.typeId
			print("It's an A")
			
The typeId is an integer which is unique for each class inside a type partition.

### type partitions

The type partition of A is the smallest set containing A such that for all classes or interfaces T1 and T2 it holds that:
If T1 is in the set and T1 is a subtype or supertype of T2, then T2 is also in the set.
Or expressed differently: A and B are in the same partition if and only if their type hierarchies are somehow connected.


## Abstract Classes

An _abstract_ class is a class, that is declared abstract — it may or may not
include abstract functions. You cannot create an instance of an abstract class,
but you can create subclasses for it which are not abstract.

An abstract function is declared with the keyword 'abstract' and by leaving out
an implementation.
    
    abstract function onHit()

Abstract classes are similar to interfaces, but they can have own, implemented
functions and variables like normal classes.

The advantage of an abstract class is, that you can reference and call the
abstract functions inside the abstract class, without having a vlid
implementation.

Take Collision-Responses as example. You have several Bodies in your map, and
you want each of them to behave differently.
You could do this by centralizing the updating function, or by using abstract
classes like so:

    abstract class CollidableObject

		abstract function onHit()
	
		function checkCollision(CollidableObject o)
			if this.inRange(o)
			  onHit()
			  o.onHit()
	    
    class Ball extends CollidableObject
	
		override function onHit()
			print("I'm a ball")
		
    class Rect extends CollidableObject
	
		override function onHit()
			print("I'm a Rect")
	   
	   
	   
Because CollidableObject requires it's subclasses to implement the function
onHit; it can be called within the abstract class and without knowing it's
type.

If a subclass of an abstract class does not implement all abstract functions
from its superclass, it has to be abstract, too.

            
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

## Defender methods

*NOTE:* Defender methods are a very experimental feature and will probably be removed from the language.

An interface can have functions with an implementation. This implementation is used, when a class implementing the interface
does not provide an implementation of the method itself. Usually this is not needed but in some cases it might
be necessary in order to evolve an interface without breaking its implementing classes.



# Generics

Generics make it possible to abstract from specific types and only program with placeholders
for types. This is especially useful for container types (e.g. Lists) where we do not want to code a
ListOfA, ListOfB, ListOfC for every class A, B, C which we need a list for.
Think of it as creating a general List with all it's functionality, but for an
unknown type, that gets defined when creating the instance.

With generics we can instead write only one implementation for lists and use it with all types.
Generic type parameter and arguments are
written in angled  bracket s  (<  an d >) after the identifier.



	// a generic interface for Sets with type parameter T
	interface Set<T>
		// adds an element to the set
		function add(T t)
		
		// removes an element from the set
		function remove(T t)
		
		// returns the number of elements in the set
		function size() returns int
		
		// checks whether a certain element is in the set
		function contains(T t) returns boolean
		
	class SetImpl<T> implements Set<T>
		// [...] implementation of the interface

If we have a class defined like this, we can then use it with a concrete type (e.g. Missile):

	// create a set of missiles
	Set<Missile> missiles = new SetImpl<Missile>();
	// add a missile m
	missiles.add(m);

Generic parameters in Wurst can be bound to integers, class types and interface types directly.
In order to bind generic parameters to primitive-, handle- and tuple types you have to provide the functions
	
	function [TYPE]ToIndex([TYPE] t) returns int

	function [TYPE]FromIndex(int index) returns [TYPE]
		return ...
		
The typecasting functions for primitive- and handle types are provided in _Typecasting.wurst_ using the fogstate bug.

	function unitToIndex(unit u) returns int
		return u.getHandleId()

	function unitFromIndex(int index) returns unit
		data.saveFogState(0,ConvertFogState(index))
		return data.loadUnit(0)

## Generic Functions

Functions can use generic types. The type parameter is written after the name of the function.
In the following example the function *forall* tests if a predicate is true for all elements in a list.
The function has to be generic, because it has to work on all kinds of lists.

	function forall<T>(LinkedList<T> l, LinkedListPredicate<T> pred) returns boolean
		for x in l
			if not pred.isTrueFor(x)
				return false
		return true
		
	// usage:
		LinkedList<int> l = ...
		// check if all elements in the list are even
		if forall<int>(l, (int x) -> x mod 2 == 0)
			print("true")

When calling a generic function, the type arguments can be omitted if they can be inferred 
from the arguments to the function:

	...
	if forall(l, (int x) -> x mod 2 == 0)
		...

Extension functions can also be generic, as shown by the following example:

	function LinkedList<T>.forall<T>(LinkedListPredicate<T> pred) returns boolean
		for x in this
			if not pred.isTrueFor(x)
				return false
		return true	

	// usage:
		...
		if l.forall((int x) -> x mod 2 == 0)
			...


# Modules

*NOTE:* It is likely that the concept of modules will change in later versions of the language. If you want to be on the safe side you should not create modules which use other modules themselves. Plain, flat modules will most likely stay in the language.

A _module_ is a small package which provides some functionality for classes. Classes can _use_ modules to inherit the functionality of the module.

You can use the functions from the used module as if they were declared in the class. You can also _override_ functions defined in a module to adjust its behavior.

If you know object oriented languages like Java or C#: Modules are like abstract classes and using a module is like inheriting from an abstract class but *without the sub-typing*. (WurstScript takes a different approach to enable polymorphism, but this is not implemented yet)

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

# Enums

In Wurst, _Enums_ can be used to set up collections of named (int) constants.
These Constants can then be accessed via the Enum's name:

    enum State
        FLYING
        GROUND
        WATER
        
    init
        State s = State.GROUND
        
You can also use enums inside of classes

    class C
        State currentState
        
        construct( State state )
            currentState = state
            
To check the current value of an enum, you can use the switch statement.
Note that all Enummembers have to be checked (or a defaut).

    switch currentState
        case State.FLYING
            print("flying")
        case State.GROUND
            print("ground")
        case State.WATER
            print("water")
            
In switch statements and variable assignments the qualifier can be ommited so you can also write:

    switch currentState
        case FLYING
            print("flying")
        case GROUND
            print("ground")
        case WATER
            print("water")        


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
				
				
Because tuples don't have any functions themselves, you can add extension
functions to an existing tuple type in order to achieve class-like
functionality.
Remember that you can't modify the value of a tuple in it's extension function
- so you have to return a new tuple every time if you wan't to change something.
Look at the Vector package in the Standard Library for some tuple usage
examples. (Math/Vectors.wurst)



# Extension Functions

Extension functions enable you to "add" functions to existing types without
creating a new derived type, recompiling, or otherwise modifying the original
type. 
Extension functions are a special kind of static function, but they are called
as if they were instance functions of the extended type.

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
		
	// And tuples as mentioned above
	public function vec2.lengthSquared returns real
		return this.x*this.x+this.y*this.y


# Lambda expressions and Closures

A lambda expression (also called anonymous function) is a lightweight way to provide an implementation
of a functional interface or abstract class (To keep the text simple, the following
explanations are all referring to interfaces, but abstract classes can be used in the same way). 

A *functional interface* is an interface which has only one method.
Here is an example:

	// the functional interface:
	interface Predicate<T>
		function isTrueFor(T t)
		
	// a simple implementation
	class IsEven implements Predicate<int>
		function isTrueFor(int x)
			return x mod 2 == 0
		
	// and then we can use it like so:
	let x = 3
	Predicate<int> pred = new IsEven()
	if pred.isTrueFor(x)
		print("x is even")
	else
		print("x is odd")
	destroy pred
	

When using lambda expressions, it is not necessary to define a new class 
implementing the functional interface. Instead the only function of the
functional interface can be implemented where it is used, like so:

	let x = 3
	// Predicate is defined here:
	Predicate<int> pred = (int x) -> x mod 2 == 0
	if pred.isTrueFor(x)
		print("x is even")
	else
		print("x is odd")
	destroy pred

The important part is: 

	(int x) -> x mod 2 == 0
	
This is a lambda expression. It consists of two parts and an arrow symbol *->* 
between the two parts. The left hand side of the arrow is a list of formal parameters, 
as you know them from function definitions. On the right hand side there
is an expression, which is the implementation. The implementation consists only
of a single expressions, because lambda expressions are typically small and used 
in one line. But if one expression is not enough there is the begin-end expression.

Remember that, because closures are just like normal objects, you also have to destroy them 
like normal objects. And you can do all the other stuff you can do with
other objects like putting them in a list or into a table.


## begin-end expression

Sometimes one expression is not enough for a closure. In this case, the begin-end 
expression can be used. It allows to have statements inside an expression. The
begin keyword has to be followed by a newline and an increase in indentation.
The rule that newlines are ignored inside parenthesis is ignored for the begin-end
expression, so that it is possible to have multiple lines of statements within:

	doLater(10.0, () -> begin
		KillUnit(u)
		createNiceExplosion()
		doMoreStuff()
	end)

It is also possible to have a return statement inside a begin-end expression
but only the very last statement can be a return.


## Capturing of Variables


The really cool feature with lambda expressions is, that they create a *closure*.
This means that they can close over local variables outside their scope
and capture them.
Here is a very simple example:

	let min = 10
	let max = 50
	// remove all elements not between min and max:
	myList.removeWhen((int x) ->  x < min or x > max)
	
In this example the lambda expression captured the local variables min and max.

It is important to know, that variables are captured by value. When a closure
is created the value is copied into the closure and the closure only works on that copy.
The variable can still be changed in the environment or in the closure, but this will have no effect
on the respective other copy of the variable.

This can be observed when a variable is changed after the closure is created:

	var s = "Hello!"
	CallbackFunc f = () -> begin
		print(s)
		s = s + "!"
	end
	s = "Bye!"
	f.run()  // will print "Hello!"
	f.run()  // will print "Hello!!"
	print(s) // will print "Bye!"


## Behind the scenes

The compiler will just create a new class for every lambda expression in your code.
This class implements the interface which is given by the context in which
the lambda expression is used.
The generated class has fields for all local variables which are captured.
Whenever the lambda expression is evaluated, a new object of the class is created
and the fields are set. 

So the "Hello!" example above is roughly equivalent to the following code:

	// (the interface was not shown in the above code, but it is the same):
	interface CallbackFunc
		function run()
		
	// compiler creates this closure class implementing the interface:
	class Closure implements CallbackFunc
		// a field for each captured variable:
		string s
	
		function run()	
			// body of the lambda expression == body of the function
			print(s)
			s = s + "!"

	var s = "Hello!"
	CallbackFunc f = new Closure()
	// captured fields are set
	f.s = s
	s = "Bye!"
	f.run()  // will print "Hello!"
	f.run()  // will print "Hello!!"
	print(s) // will print "Bye!"
	
## Function types

A lambda expression has a special type which captures the type of the parameter
and the return type. This type is called a *function type*. Here are some examples with their type:

	() -> 1   						
		// type: () -> integer
	
	(real r) -> 2*r
		// type: (real) -> real
	
	(int x, string s) -> s + I2S(x)  
		// type: (int,string) -> string


While function types are part of the type system, Wurst has no way to write down 
a function type. There are no variables of type "(int,string) -> string".
Because of this, a lambda expression can only be used in places where
a concrete interface or class type is known. 
This can be an assignment where the type of the variable is given. 

	Predicate<int> pred = (int x) -> x mod 2 == 0
	
However it is not possible to use lambda expressions if the type of the variable is only inferred:

	// will not compile, error "Could not get super class for closure"
	let pred = (int x) -> x mod 2 == 0

## Lambda expressions as code-type

Lambda expressions can also be used where an expression of type `code` is expected. 
The prerequisite for this is, that the lambda expression does not have any parameters
and does not capture any variables. For example the following code is _not_ valid, 
because the local variable `x` is captured.


	let t = getTimer()
	let x = 3
	t.start(3.0, () -> doSomething(x)) // error: Cannot capture local variable 'x'

	
This can be fixed by attaching the data to the timer manually:

	let t = getTimer()
	let x = 3
	t.setData(x)
	t.start(3.0, () -> doSomething(GetExpiredTimer().getData()))

If a lambda expression is used as `code`, there is no new object created and
thus there is no object which has to be destroyed. The lambda expression will just
be translated to a normal Jass function, so there is no performance overhead when 
using lambda expressions in this way.

# Advanced Concepts

## Function Overloading

Function overloading allows you to have several functions with the same name.
The compiler will then decide which function to call based on the static type
of the arguments.

Wurst uses a very simple form of overloading. If there is exactly one function in
scope which is feasible for the given arguments, then this function will be used.
If there is more than one feasible function the compiler will give an error.

Note that this is different to many other languages like Java, where the
function with the most specific feasible type is chosen instead of giving an error.

	function unit.setPosition(vec2 pos)
		...

	function unit.setPosition(vec3 pos)
		...

	function real.add(real r)
		...

	function real.add(real r1, real r2)
		...

This works because the parameters are of different types or have a different amount of paramaters and the correct function can therefore be determined at compiletime.

	function real.add(real r1)
		...

	function real.add(real r1) returns real

This does not work because only the returntype is different and the correct function cannot be determined.

	
	class A
	class B extends A
	
	function foo(A c)
		...

	function foo(B c)
		...
	
	// somewhere else:
		foo(new B)
		


This does not work either, because B is a subtype of A. If you would call the function foo 
with a value of type B, both functions would be viable. Other languages just take the 
"most specific type" but Wurst does not allow this. If A and B are incomparable types, the overloading is allowed.


## Operator Overloading

Operator Overloading allows you to change the behavior of internal operators +, -, \* and / for custom arguments.
A quick example from the standard library (Vectors.wurst):

    // Defining the "+" operator for the tupletype vec3
    public function vec3.op_plus( vec3 v ) returns vec3
        return vec3(this.x + v.x, this.y + v.y, this.z + v.z)
        
    // Usage example
    vec3 a = vec3(1.,1.,1.)
    vec3 b = vec3(1.,1.,1.)
    // Without Operator Overloading (the add function was replaced by it)
    vec3 c = a.add( b )
    // With operator Overloading
    vec3 c = a + b
   
You can overload operators for existing types via Extension-Functions or via class-functions for the specific classtype.
In order to define an overloading function it has to be named as following:

    +  "op_plus"
    -  "op_minus"
    *  "op_mult"
    /  "op_divReal"
    
## Object Editing

Creating Object-Editor Objects via Wurst code.

*NOTE:* Object Editing hardly works at the moment, so you should only use it for fun but not for profit. 
Do not use it for a real project yet!

### Compiletime Functions

Compiletime Functions are functions, that are executed when compiling your script/map.
They mainly offer the possibility to create Object-Editor Objects via code.

A compiletime function is just a normal Wurst function annotated with @compiletime.

    @compiltetime function foo()

Compiltetime functions have no parameters and no return value.

In order to run compiletime functions you have to enable the checkbox in the Wurstpack Menu.
When you use compiletime functions to generate objects, Wurst will generate the object files
next to your map and you can import them into your map using the object editors normal import
function. Compared to ObjectMerger this has the advantage, that you can directly see your new 
objects in the object editor.
You can also enable an option to directly inject the objects into the map file, though the changes will not be visible in the object-editor directly.

You can use the same code during runtime and compiletime.
The special constant `compiletime` can be used to distinguish the two.
The constant is `true` when the function was called at compiletime and `false` otherwise.
The following example shows how this could be useful:

	init
		doInit()

	@compiletime
	function doInit()
		for i = 1 to 100
			if compiletime
				// create item object
			else
				// place item on map


### Object Editing Natives

The standard library provides some functions to edit objects in compiletime functions. 
You can find the corresponding natives and higher level libraries in the objediting folder of the standard library.

The package ObjEditingNatives contains natives to create and manipulate objects. If you are familiar with
the object format of Wc3 and know similar tools like [Lua Object Generation](http://www.hiveworkshop.com/forums/jass-ai-scripts-tutorials-280/lua-object-generation-191740/) 
or the ObjectMerger from JNGP, you should have no problems in using them. If you run Wurst with compiletime functions enabled, it will generate 
the object creation code for all the objects in your map. This code is saved in files named similar to "WurstExportedObjects_w3a.wurst.txt" and
can be found right next to your map file. You can use this code as a starting point if you want to use the natives.

Wurst also provides a higher level of abstraction. For example the package AbilityObjEditing provides many classes 
for the different base abilities of Wc3 with readable method names. That way you do not have to look up the IDs.

The following example creates a new spell based on "Thunder Bolt". The created spell has the ID "A005".
In the next line the name of the spell is changed to "Test Spell".
Level specific properties are changed inside the loop.

	package Objects
	import AbilityObjEditing

	@compiletime function myThunderBolt()
		// create new spell based on thunder bolt from mountain king	
		let a = new AbilityDefinitionMountainKingThunderBolt("A005")
		// change the name
		a.setName("Wurst Bolt")
		a.setTooltipLearn("The Wurstinator throws a Salami at the target.")
		for i=1 to 3
			// 400 damage, increase by 100 every level
			a.setDamage(i, 400. + i*100)
			// 10 seconds cooldown
			a.setCooldown(i, 10.)
			// 0 mana, because no magic is needed to master Wurst
			a.setManaCost(i, 0)
			// ... and so on


*NOTE* There are also packages for other object types, but those packages are even more WIP.



## Automated Unit Tests

You can add the annotation @test to a function. Then when you type "tests" into the Wurst Console all functions
annotated with @test are executed.

You have to import the Wurstunit package to use functions like assertEquals.

Example:

		package Test
		import Wurstunit

		@test public function a()
			12 .assertEquals (3*4)
	
		@test public function b()
			12 .assertEquals (5+8) 	 

If you run this, you get the following output:

		> 1+1
		res = 2     // integer
		> tests
		1 tests OK, 1 tests failed
		function b (Test.wurst, line 8)
			test failed: expected 13 but was 12
			at stmtreturn  (.../lib/primitives/Integer.wurst, line 25)
		> 
	
The first line is just to check whether the console is working ;)

You can search the standard library for "@test" to get some more examples.

# Other Stuff

## Stacktraces

You can enable stacktraces in the the menu of WurstPack or with the commandline
switch `-stacktraces`. Each error will then be displayed with a stacktrace showing
the line number and filename of each function-call leading to the error. Errors
must be generated with the magic function `function error(string msg)`.

# Standard Library 

Wurst comes with a library of some useful standard functions
You can find the generated HotDoc pages here: [The Wurstscript Standardlibrary](http://peeeq.de/hudson/job/Wurst/HotDoc_Standard_Library_Documentation/index.html)
However, the best way to learn about the library is still to look at the source code.

# Wurst Style

In this section we describe some style guidelines which you should follow when programming in Wurst.

## Rule 1: Write for readability

You should always write your code so that it can be read easily. Ideally your code should be readable like normal English text.
Use suitable names for functions and variables to make your code sound like normal text.

- Create functions which help you to express your intend on a higher level of abstraction.
- When a function or variable has type boolean, name it like a question. The name should sound natural when used inside an if statement.
- Use class functions and extension methods to make code readable from left to right.


Example:

	// not so readable:
	if GetUnitState(h, UNIT_STATE_LIFE) <= 0.405
		let t = NewTimer()
		t.setData(...)
		t.start(...)
		...

	// better:
	if hero.isDead()
		hero.reviveAt(town, REVIVE_TIME)

	// that timer stuff is in a different small function


### Rule 1.1 Document your Code
### Rule 1.2 Keep functions short
### Rule 1.3 The Golden Path




## Rule 2: Write checkable code

Your code should be checkable by the compiler. The Wurst compiler provides you with some powerful typechecking tools. Use 
them wisely to reduce the mistakes you can do in your program. You might also want to watch [this awesome talk by Yaron Minsky](http://vimeo.com/14313378) who summarizes it pretty nicely: "make invalid state unrepresentable". Of course Wurst is not as sophisticated as ML
but there are still a lot of things you can do.

- avoid the castTo expression whenever possible
- use high level libraries like HashMap instead of lower level libraries like Table or even then hashtable natives
- use tuple types to give meaning to values (see Vector for an example)
- use enums with switch statements

## Rule 3: DRY: Don't repeat yourself

When you find the same lines of code at several places of your project, try to put the common code into a function, class or module.

## Rule 4: KISS: Keep it simple, stupid!

Always try to choose the most simple solution to your problem. Avoid premature optimization. 


## Rule 5: Use Object oriented programming

### Avoid instanceof

Using *instanceof* is usually a sign for bad use of object orientation. Often instanceof checks can 
be replaced by using dynamic dispatch. Let's look at an example:

	if shape instanceof Circle
		Circle c = shape castTo Circle
		area = bj_PI * c.radius*c.radius
 	else if shape instanceof Rect
		Rect r = shape castTo Rect
		area = r.width * r.height

It would be better to have one area function in Shape and then implement it for Circle and Rect. 
That way you can just write:

	area = shape.area()

The right area method will then be automatically selected based on the type of shape.


# vJass vs Wurst

If you plan to convert from using vJass to Wurst, it will probably be helpful
to read this segment.
Specific vJass syntax and features are directly compared to the equivalent
WurstCode.


You might be wondering why Wurst is missing so many vJass features.
The answer is simple: Wurst utilizes several better/smarter constructs to achieve
similar functionality like in vJass and beyond.

Textmacros and member-arrays aside, most vJass features either got useless due
to Wurst or got replaced.

## Feature table

<table>
<tr><td> <strong>Feature</strong> 		</td><td> <strong>vJass</strong> 	</td><td> <strong>Wurst</strong> 	</td><td></td></tr>
<tr><td> <strong>code organization</strong> 	</td><td> libraries 			</td><td> packages 			</td><td></td></tr>
<tr><td> <strong>nested scopes</strong> 	</td><td> yes 				</td><td> -  				</td><td></td></tr>
<tr><td> <strong>classes</strong> 		</td><td> structs 			</td><td> classes 			</td><td></td></tr>
<tr><td> <strong>modules</strong> 		</td><td> yes 		</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>function interfaces</strong> 	</td><td> yes 		</td><td> no </td><td></td></tr>
<tr><td> <strong>interfaces</strong> 		</td><td> yes 		</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>textmacros</strong> 		</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>keyword</strong> 		</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>struct onInit</strong> 	</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>stub methods</strong> 	</td><td> yes 		</td><td> overriding and abstract classes 	</td><td></td></tr>
<tr><td> <strong>Dynamic arrays</strong> 	</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Array members</strong> 	</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Delegate</strong> 		</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Array structs</strong> 	</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Keys</strong> 			</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Sized arrays</strong> 	</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>2D arrays</strong> 		</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Colon</strong> 		</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Operator overloading</strong>	</td><td> for [] 	</td><td> for +,-,*,/ 		</td><td></td></tr>
<tr><td> <strong>hook</strong> 			</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>inject</strong> 		</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Loading from SLK</strong> 	</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Script optimization</strong> 	</td><td> only inlining	</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>Function inlining</strong> 	</td><td> limited 	</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>External tools</strong> 	</td><td> yes 		</td><td> - 					</td><td></td></tr>
<tr><td> <strong>Typechecker</strong> 		</td><td> limited 	</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>Function sorting</strong> 	</td><td> - 		</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>Extension functions</strong> 	</td><td> - 		</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>generics</strong> 		</td><td> - 		</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>tuple types</strong> 		</td><td> - 		</td><td> yes 					</td><td></td></tr>
<tr><td> <strong>closures</strong> 		</td><td> - 		</td><td>yes </td><td></td></tr>
<tr><td> <strong>compiletimefunctions</strong>	</td><td> - 		</td><td> yes 					</td><td></td></tr>
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

Name-spaces/code organisation in wurst is handled by packages.
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

# Eclipse Plugin

Installation: You can find information on how to install the plugin at the [Wurst Homepage](http://peq.github.com/WurstScript/).
[Installation](http://peq.github.com/WurstScript/installation.html)

## Usage

1. Go to *File*-*New Project* and select *New Wurst Project*. Choose a name and press Finish.
2. Add a new file named *wurst.dependencies*. Each line in this file points to a folder on which your project depends.
	As every project depends on the standard library you have to add the path to *Wurstpack/wurstscript/lib* in your Wurstpack folder.
3. Create a new file with the *.wurst* filename extension and an arbitrary name. You can now start writing your code.

### Useful Shortcuts 

* Press *Ctrl*+*space* to open the auto complete assistant.
* Hold down *Ctrl* and click on a function or variable to jump to the point where it was defined. 
* Press *Ctrl*+*Shift*+*R* to search for a wurst filename.
* Hover the mouse over some element in your code to get additional information

### The Wurst-Console

The Wurst-Console provides an Read-Eval-Print-Loop (REPL) for Wurst. 
The Console can be activated via Window -> Show View -> Console.
The Console is always attached to the currently opened document. You can enter 
expressions into the console and the expression will be evaluated in the context of the currently 
opened document. 

![Wurst Console](./assets/images/wurstConsole1.png)

By default the result of the expression will be stored in the variable `res`, but you
can also use a *let* statement to store the result in a different variable.

The console supports other useful commands, which are not (yet) accessible. A list
of commands can be displayed by typing `help` into the console. The most important 
commands are probably `reset` and `tests`. `reset` will reset the state of the 
console. This is sometimes necessary after switching to other files, or if 
the console is in an invalid state because of bugs. `tests` will run all unit tests
(functions annotated with @test) in the current project and show the test results.

# Optimizer

The Wurstcompiler has a set of build-in scriptoptimizing tools which will, when enabled, optimize the generated Jass code in various ways.
Jass optimization got very important to provide playable framerates when using very enhanced and complex systems.
On the one hand the optimizer cleans the code, making it smaller in size and removing useless stuff in order to reduce RAM-usage.
On the other hand it also offers some optimizations to increase the speed of execution and performance of the code.

## Cleaning

Stuff that is being removed, changed or not even printed

* Comments
* Unneeded White-spaces
* Excessive parentheses
* Some useless Jassconstants replaced with "null"

## Name compression

Smaller names execute faster and take less space, so all names of functions and variables are compressed to the shortest name possible.

## Inlining

Inlining is not an easy task, but brings great performance boosts to systems which use many different functions. 
It also makes coding easier and more readable, because you don't have to care about the performance loss
when splitting stuff into too many functions.
Also blizzard.j functions, such as BJs and Swaps, can get inlined.

In the current implementation, a function can be inlined when it has only one exit-point.
This is the case, when there is no return statement or when the only return statement is at the end of a function.

Whether a function actually gets inlined depends on some heuristics in the compiler.
The heuristic tries to balance execution speed and size of the mapscript, as inlining usually makes the code longer but faster.
A function is more likely to get inlined, when it is short.
A function is less likely to get inlined, when it is called in many different places.
It is best to not rely on more guarantees about the inliner, as the heuristics are changed from time to time.

Global variables that have a constant value get inlined as well as constant locals.

## Constant Folding and Constant propagation

Expressions containing only constants are calculated at compiletime.
Ifs with constant conditions are removed. 
Both mechanics work together to remove unneeded and unreachable code.

# Errors and Warnings

Wurst provides some errors and warnings to help finding bugs early in the development process.
In this chapter some of these errors and warnings are explained, as well as some ways to fix them.

## Warnings

* The assignment to local variable x is never read

	This warning means that a value used in an assignment is never read in all possible executions.
	This often means that you forgot to use it, so it probably is a bug you have to fix.
	Sometimes it is just some unnecessary code as in the following example: 

		int x = 0
		if someCondition
			x = 2
		else
			x = 3
		print(x.toString())

	The fix for this warning is straight forward in this case, just remove the unused value:

		int x
		if someCondition
			x = 2
		else
			x = 3
		print(x.toString())
	
	
* The variable x is never read.

	Usually this warning also means that you have some unnecessary code or that you forgot to use something.
	In some rare cases you need to give a parameter but do not want to use it.
	In these cases you can **suppress the warning** by starting the variable name with an underscore:

		function foo(int _x)
			return 5 

* The assignment to ... probably has no effect

	This warning usually appears with assignments like the following:

		construct(int emeny)
			this.enemy = enemy

	Note that there is a typo in the parameter which causes the assignment to be wrong.

* The import .... is never used directly.

	Usually this means that you are importing a package and never using it, so that you can remove the import.
	There are some corner cases like implicit generic conversions, which are not detected correctly at the moment.

Some warnings are just there to ensure some common coding standards in Wurst:

* Function names should start with an lower case character.
* Variable names should start with a lower case character.
* Type names should start with upper case characters.
