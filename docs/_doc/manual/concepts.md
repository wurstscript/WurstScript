---
title: Basic Concepts
sections:
- Expressions
- Cascade operator
- Statements
- Packages
---

## Expressions

The Wurst expressions in semi-formal syntax:
```wurst
Expr ::=
      Expr + Expr       // addition
    | Expr - Expr       // suctraction
    | Expr / Expr       // real division
    | Expr div Expr     // integer division
    | Expr % Expr       // real modulo
    | Expr mod Expr     // integer modulo
    | Expr and Expr     // boolean and
    | Expr or Expr      // boolean or
    | Expr ? Expr : Expr // Conditional expression / ternary operator
    | Expr < Expr       // smaller than
    | Expr <= Expr      // smaller or equal than
    | Expr > Expr       // greater than
    | Expr >= Expr      // greater than or equal
    | Expr == Expr      // equal
    | Expr != Expr      // unequal
    | - Expr            // unary minus
    | not Expr          // invert expression
    | IDENTIFIER                    // variable access
    | IDENTIFIER(Expr, Expr, ...)   // function call
    | Expr . IDENTIFIER             // member variable
    | Expr . IDENTIFIER(Expr, Expr, ...)    // member function
    | Expr .. IDENTIFIER(Expr, Expr, ...)   // member function, same as single dot
                                            // but returns the receiver type
    | new IDENTIFIER(Expr, Expr, ...)       // constructor call
    | destroy Expr                  // destroy object
    | Expr castTo Type              // casting
    | Expr instanceof Type          // instance checking
    | begin                         // multi-line lambda expression
        Statements
        end // statement expr
    | (param1, param2, ...) -> Expr  // anonymous function
    | (Expr)                        // parantheses
```


An _IDENTIFIER_ is a name of a variable or function. It may start with letters and may
contain letters, numbers and underscores.

**Note**: For calls to generic functions refer to the Generics chapter.


### Cascade operator (dot-dot-operator)

The cascade operator `..` is similar to the normal `.` operator and can be used for calling methods, but instead of returning the result
of the called method, the cascade operator returns the receiver. This makes it possible to easily chain function calls on the same variable.
Take a look at the following example:
```wurst
CreateTrigger()
    ..registerAnyUnitEvent(EVENT_PLAYER_UNIT_ISSUED_ORDER)
    ..addCondition(Condition(function cond))
    ..addAction(function action)
```
The above code is equivalent to:
```wurst
let temp = CreateTrigger()
temp.registerAnyUnitEvent(EVENT_PLAYER_UNIT_ISSUED_ORDER)
temp.addCondition(Condition(function cond))
temp.addAction(function action)
```

### Conditional operator

The conditional operator (also called ternary operator) has the form `condition ? ifTrue : ifFalse`.
First the condition is evaluated.
If the condition is `true`, then the `ifTrue` expression is evaluated and the result of the overall expression.
Otherwise the `ifFalse` expression is evaluated.

The type of the expression is the union-type of both result-expressions, meaning that both expressions must have a type that is allowed where the conditional expression is used.

The conditional operator should only be used in some rare cases.
A typical use is as a shorthand for choosing between two alternatives like in the following example:

```wurst
// handle the special case of one enemy left:
string enemyCount = n.toString() + " " + (n == 1 ? "enemy" : "enemies")
```


## Statements

Wurst adds some modern statements to Jass' existing ones, like switches and for-loops.

### If Statement
The if statement is a conditional control structure that executes certain parts of code when a condition is met.
An optional **else** case can be provided to also handle false results.
```wurst
// simple if
if u.getHP() < 100
    print("unit s hurt") // the content inside the if has to be indented

// if with else case
if x > y
    print("x is greater than y")
else if x < y // closing the if, opening the elseif-block
    print("x is less than y")
else // handle remaining cases
    print("x is equal to y")

// Finish the if by indenting back

// More examples:

if caster.getOwner() == BOT_PLAYER
    print("caster owned by BOT_PLAYER")

if GetSpellAbilityId() == SPELL_ID
    flashEffect(getSpellTargetPos(), FX_PATH)

if x > 0 and x < 100 and y > 0 and y < 100
    print("inside box")
```
### Switch Statement
```wurst
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
```
As you see in the example, a switch statement is basically a nicer syntax for
nesting ifs and else ifs, with the special default case.

### Loops
```wurst
while a > b // while-loop with input condition
    ...

for i = 0 to 10 // for-loop
    ...

for i = 0 to 10 step 2 // for-loop with step 2
    ...

for i = 10 downto 0 // wurst can also count downwards
    ...

for u in someGroup // loop over all units in a group
    ...

for u from someGroup // loop over all units in group and remove the units from the group
    ...

for i in myList
    ...
```

### For Loops

The for-in loop lets you iterate over any object which provides an iterator.
A for-in loop can be transformed into an equivalent while-loop very easily:

```wurst
for A a in b
    Statements

// is equivalent to:
let iterator = b.iterator()
while iterator.hasNext()
    A a = iterator.next()
    Statements*
iterator.close()
```

**Note** that iterator.close() will also be called before any return statement inside the body of the while loop.

If you already have an iterator or want to access further functions of the iterator you can use the for-from loop.
The translation is very similar:
```wurst
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
```
**Note** that you have to close the iterator yourself.

### Skip

The most basic statement is the _skip_ statement. It has no effect and can be useful in rare cases.

### Iterators


You can provide Wurst with an iterator for your desired type by providing a set of functions. By providing an iterator, the type can be used in for-loops:
-  function **hasNext()** returns boolean (return if there is another object left)
-  function **next()** returns TYPE (return the next element for your type)

With this two functions you get an iterator which can be used in for-from loops.

To make a type usable in for-in loops you have to provide

-  function **iterator()** returns Iterator

for your type, that returns the object for the iteration.
This can either be a handle, like in the group-iterator or an object like the List-iterator.
Your iterator should also provide a close function which clears all resources allocated by it.
Most often the iterator just destroys itself in the close function.

Look at the 2 examples from the standard library:

**Group-Iterator**
```wurst
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
```
As you can see, the iterator is a group, therefore the group needs to provide the functions mentioned above.
This is done via extension functions.

**LinkedList-Iterator**
```wurst
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
```
The LinkedList Iterator is a little different, because it's a class. Still it provides the needed functions and is therefore viable as iterator.
It also contains some members to help iterating. A new instance if LLIterator is returned from the .iterator() function of the LinkedList.

### Assignment Shorthands

WurstScript supports the following shorthands for assignments:
```wurst
i++         // i = i + 1
i--         // i = i - 1
x += y      // x = x + y
x -= y      // x = x - y
x *= y      // x = x * y
x /= y      // x = x / y
```
Because these shorthands simply get translated into their equivalents, they can
be used with overloaded operators, too.

# Packages
As mentioned above every code-segment written in Wurst has to be inside a _package_,
packages define the code organization and separate name-spaces.
Packages can also have global variables - every variable that is not inside another block (function/class/module)
is declared global for that package.

When working in WurstWE, packages have to end with the **endpackage** keyword and the code inside has to be indent.

In WurstEclipse however, the **endpackage** can be omitted when the code inside is not indented.
```wurst
package SomeWurstWePackage
    // Only for legacy WurstWE
    ...
endpackage

package SomeWursteclipsePackage
...
```

## Imports

Packages can import other packages to access classes, functions, variables, etc. that are defined public.

```wurst
// declaring
package SomePackage

// importing
package Blub
import SomePackage
import AnotherPackge // importing more than 1 package
import MyExternalWurstFile // Import a scriptfile from the eclipseProject
import public PackageX // public import (see below)
```

The import directive searches for packages in the wurst folder of your project and all linked projects from your wurst.dependencies file.

### import public

By default imported names are not exported by the package. For example the following will not compile:
```wurst
package A
public constant x = 5

package B
import A

package C
import B
constant z = x
```

The variable x is usable in package B but it is not exported from B. So in package C we cannot use the variable x.
We could fix this by simply importing A into C but sometimes you want to avoid those imports.
Using public imports solves this problem because they will export everything that is imported. Thus the following code will work:

```wurst
package A
public constant x = 5

package B
import public A

package C
import B
constant z = x
```

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
```wurst
package First
int i // (private by default) Visible inside the package
public int j // public, gets exported

package Second
import First

int k = i // Error
int m = j // Works, because j is public


package Foo
constant int myImportantNumber = 21364 // has to be initialized with declaration

function blub()
    myImportantNumber = 123 // Error

public constant int myPrivateNumber2 = 123 // Correct keyword order
```


## Init blocks
Another package feature are init blocks.
Every package can have one or multiple init blocks anywhere inside it.
All operations inside the init block of a package are executed at mapstart.

At the beginning of an init block you can assume that all global variables inside the
current package are initialized.
```wurst
package MyPackage
init
    print("this is the initblock")
```

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
```wurst
package A
import initlater B
import public initlater C
import D
```
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
```wurst
package Example
@configurable int x = 5

package Example_config
@config int x = 42
```
Configuring functions works basically the same:
```wurst
package Example
@configurable public function math(int x, int y) returns int
    return x + y


package Example_config
@config public function math(int x, int y) returns int
    return x*y

```
