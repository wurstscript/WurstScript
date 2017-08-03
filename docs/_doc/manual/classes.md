---
title: Classes
sections:
- Constructors
- This
- Ondestroy
- Static Elements
- Inheritance
- Interfaces
- Generics
---

Classes are easy, powerful and very helpful constructs. A _class_ defines data and related functions working with this data. Take a look at this small example:


```wurst
Caster dummyCaster = new Caster(200.0, 400.0)
dummyCaster.castFlameStrike(500.0, 30.0)
destroy dummyCaster
```

In this example we created a Caster named "dummyCaster" at the location(200, 400).
Then we ordered **dummyCaster** to cast a flame strike at another position and finally we destroyed **dummyCaster**.

This example shows you how to create a new object (line 1), invoke a function on an object (line 2) and how to destroy an object (line 3).
But how can you define a new object type like "Caster"? This is where classes come in. A class defines a new kind of object.
A class defines variables and functions, which every instance of this class should understand.
A class can also define how a new object is constructed (_construct_) and what should happen, when it is destroyed (_ondestroy_).

Defining a caster-class might look like this:

```wurst
class Caster // opening the class-block. "Caster" is the name of the class
    unit u // class variables can be defined anywhere inside a class

    construct(real x, real y)
        u = createUnit(...)

    function castFlameStrike(real x, real y)
        u.issueOrder(...)

    ondestroy
        u.kill()
```

## Constructors

WurstScript allows you to define your own constructors for each class. A constructor
is a function to _construct_ a new instance of a class.
The constructor is called when creating the class via the _new_ keyword and allows operations being done to the class-instance before returning it.

```wurst
class Pair
    int a
    int b

    construct(int pA, int pB)
            a = pA
            b = pB

    function add() returns int
            return a + b


function test()
    Pair p = new Pair(2,4)
    int sum = p.add()
    print(sum)
```



In this example the constructor takes two integers a and b as parameters and sets the class variables to those.
You can define more than one constructor as long as the parameters differ.

```wurst
class Pair
    int a
    int b

    construct(int pA, int pB)
        a = pA
        b = pB

    construct(int pA, int pB, int pC)
        a = pA
        b = pB
        a += pC
        b += pC

function test()
    Pair p = new Pair(2,4)
    Pair p2 = new Pair(3,4,5)
```



In this example the class pair has two constructors - one taking 2 and the second one taking three parameters.
Depending on parameter-type and -count Wurst automatically decides which constructor to take when using "new".

## This

The _this_ keyword refers to the current instance of the class on which the function was called. This also allows us to name the parameters the same as the class variables.
However it can be left out in classfunctions, as seen above.
```wurst
class Pair
    int a
    int b

    // With the this keyword we can access the classmembers
    construct(int a, int b)
        this.a = a
        this.b = b
```

## ondestroy

Each class can have one _ondestroy_ block. This block is executed before the instance is destroyed.
Ondestroy blocks are defined as previously shown

```wurst
class UnitWrapper
    unit u
    ...

    ondestroy
        u.remove()
```


## Static Elements

You can declare variables and functions as _static_ by writing the keyword "static" in front of the declaration. Static variables and functions belong to the class whereas
other elements belong to instances of the class. So you can call static functions without having an instance of the class.

```wurst
class Terrain
    static real someVal = 12.

    static int array someArr

    static function getZ( real x, real y ) returns real
        ...

function foo()
    real z = Terrain.getZ( 0, 0 ) // call with $Classname$.$StaticFunctionName$()
    real r = Terrain.someVal // Same with members
    real s = Terrain.someArr[0]
```

## Array Members

Wurstscript supports sized arrays as classmembers by translating it to SIZE times arrays and then resolve the array in a get/set function via binary search.

Example Usage:

```wurst
class Rectangle
    Point array[4] points

    function getPoint(int index)
        return points[index]

    function setPoint(int index, Point nP)
        points[index] = nP
```

## Visibility Rules

By default class elements are visible everywhere. You can add the modifiers _private_ or _protected_ in front of a variable or function definition to restrict its visibility.
Private elements can only be seen from within the class. Protected elements can be seen within the enclosing package and in subclasses.

## Inheritance
A class can _extend_ an other class. The class then inherits all the non-private functions and variables from that class
and can be used anywhere where the super class can be used.

Constructors of the class have to specify how the super class should be constructed. This is done using a _super_ call,
which defines the arguments for the super constructor. There can not be any statement before this call.

If a constructor does not provide a super call, the compiler tries to insert a super call with no arguments.


Functions inherited from super classes can be overridden in the subclass. Such functions have to be annotated with _override_.


### Example
```wurst
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
```

## Typecasting

In order to typecast, you use the _castTo_ operator

You need typecasting for several reasons.

One being to save class instances and for example attaching them onto a timer, like done in TimerUtils.wurst
This process can also be reversed (casting from int to a classtype).

```wurst
class Test
    int val

init
    Test t = new Test()
    int i = t castTo int
```


Typecasting is sometimes useful when using subtyping. If you have an object of static type A but know
that the dynamic type of the object is B, you can cast the object to B to change the static type.

```wurst
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
```


_Note_: You should avoid castTo whenever possible. Casts are not checked at runtime so they can go horribly wrong.
Casts can be avoided by using high level libraries and object oriented programming.


## Dynamic dispatch

Wurst features dynamic dispatching when accessing classinstances.
What this means is simple: If you have a classinstance of a subclass B, casted into a variable of
the superclass A, calling a function with that reference will automatically call the overridden function from
the original type.
It is easier to understand with an example:

### Example 1
```wurst
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
```
### Example 2
```wurst
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
```

This is especially useful when iterating through ClassInstances of the same supertype,
meaning you don't have to cast the instance to it's proper subtype.

## super calls

When you override a method you can still call to the original implementation by using the *super* keyword. This is useful since
subclasses often just add some functionality to its base classes.

As an example consider a fireball spell for which we want to create a more powerful version:
```wurst
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
```
The **super** keyword also should be used when defining custom constructors. The Constructor of the superclass has to be called in the constructor of the subclass. When no super constructor call is given, the compiler will try to call a constructor with no arguments. If no such constructor exists, then you will get an error like: "Incorrect call to super constructor: Missing parameters."
```wurst
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
```

## instanceof

In Wurst you can check the type of a classinstance with the _instanceof_ keyword.

_Note_: You should avoid instanceof checks whenever possible and prefer object oriented programming.

The instanceof expression "o instanceof C" returns true, if object o is a subtype of type C.

It is not possible to use instanceof with types from different type partitions, as instanceof
is based on typeIds (see following chapter). This is also the reason why you cannot use instanceof
to check the type of an integer.

The compiler will try to reject instanceof expressions, which will always yield true or always yield false.

###Example
```wurst
class A

class B extends A

init
    A a = new B()

    if a instanceof B
        print("It's a B")
```

## typeId

*NOTE*: typeIds are an experimental feature. Try to avoid using them.

Sometimes it is necessary to check the exact type of an object. To do this you can write "a.typeId" if a is an object or "A.typeId" if A is a class.
```wurst
// check if a is of class A
if a.typeId == A.typeId
    print("It's an A")
```

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
```wurst
abstract function onHit()
```

Abstract classes are similar to interfaces, but they can have own, implemented
functions and variables like normal classes.

The advantage of an abstract class is, that you can reference and call the
abstract functions inside the abstract class, without having a vlid
implementation.

Take Collision-Responses as example. You have several Bodies in your map, and
you want each of them to behave differently.
You could do this by centralizing the updating function, or by using abstract
classes like so:
```wurst
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
```


Because CollidableObject requires it's subclasses to implement the function
onHit; it can be called within the abstract class and without knowing it's
type.

If a subclass of an abstract class does not implement all abstract functions
from its superclass, it has to be abstract, too.


# Interfaces

```wurst
interface Listener
    function onClick()

    function onAttack( real x, real y ) returns boolean


class ExpertListener implements Listener
    function onClick()
        print("clicked")


    function onAttack( real x, real y ) returns boolean
        flashEffect(EFFECT_STRING, x ,y)
```


An _interface_ is a group of related functions with empty bodies.
If a class implements a certain interface it has to replace all the empty functions from the interface.
A class can _implement_ multiple interfaces, meaning that it complies to more interface requirements at the same time.
```wurst
class ExampleClass implements Interface1, Interface2, ...
    // all functions from the implemented interfaces
```

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


```wurst
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
```
If we have a class defined like this, we can then use it with a concrete type (e.g. Missile):
```wurst
// create a set of missiles
Set<Missile> missiles = new SetImpl<Missile>();
// add a missile m
missiles.add(m);
```
Generic parameters in Wurst can be bound to integers, class types and interface types directly.
In order to bind generic parameters to primitive-, handle- and tuple types you have to provide the functions
```wurst
function [TYPE]ToIndex([TYPE] t) returns int

function [TYPE]FromIndex(int index) returns [TYPE]
    return ...
```
The typecasting functions for primitive- and handle types are provided in _Typecasting.wurst_ using the fogstate bug.
```wurst
function unitToIndex(unit u) returns int
    return u.getHandleId()

function unitFromIndex(int index) returns unit
    data.saveFogState(0,ConvertFogState(index))
    return data.loadUnit(0)
```
## Generic Functions

Functions can use generic types. The type parameter is written after the name of the function.
In the following example the function *forall* tests if a predicate is true for all elements in a list.
The function has to be generic, because it has to work on all kinds of lists.
```wurst
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
```
When calling a generic function, the type arguments can be omitted if they can be inferred
from the arguments to the function:
```wurst
...
if forall(l, (int x) -> x mod 2 == 0)
    ...
```
Extension functions can also be generic, as shown by the following example:
```wurst
function LinkedList<T>.forall<T>(LinkedListPredicate<T> pred) returns boolean
    for x in this
        if not pred.isTrueFor(x)
            return false
    return true

// usage:
    ...
    if l.forall((int x) -> x mod 2 == 0)
        ...
```

# Modules

*NOTE:* It is likely that the concept of modules will change in later versions of the language. If you want to be on the safe side you should not create modules which use other modules themselves. Plain, flat modules will most likely stay in the language.

A _module_ is a small package which provides some functionality for classes. Classes can _use_ modules to inherit the functionality of the module.

You can use the functions from the used module as if they were declared in the class. You can also _override_ functions defined in a module to adjust its behavior.

If you know object oriented languages like Java or C#: Modules are like abstract classes and using a module is like inheriting from an abstract class but *without the sub-typing*. (WurstScript takes a different approach to enable polymorphism, but this is not implemented yet)

## Example 1

In this example we just have a class which uses a module A. The resulting program behaves as if the code from module A would be pasted into Class C.

```wurst
module A
    public function foo()
        ...

class C
    use A
```

## Example 2

Modules are more than just a mechanism to copy code. Classes and modules can override functions defined in used modules:

```wurst
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
```

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
