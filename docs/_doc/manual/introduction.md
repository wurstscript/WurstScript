---
title: Introduction
sections:
- Philosophy
- Values and Types
- Syntax
- Basics
---

### Philosophy

WurstScript is an imperative, object-oriented, statically-typed, beginner-friendly programming language.

WurstScript provides a comfortable workflow to produce readable and maintainable code.
Ease of use and stress-free map development take higher priority than execution speed of the final product.
WurstScript is easy to use and learn, especially with prior knowledge of Jass or any other programming language, while still staying
simple and readable to non-programmers.

While we know that WurstScript won't replace vJass in the WC3 mapping scene (one reason being tons of vJass scripts that can't be easily ported), we still hope it will  serve as a very good alternative, in particular for users that are trying to learn Jass.

> Note that this manual is not a beginner's tutorial and expects the reader to have prior knowledge in programming. 
[Click here for a beginner's guide.](tutorials.html)

### Values and Types

WurstScript is a statically typed language. This means variables can only hold values of the same type. Additionally since all types are determined at compiletime,
incorrect assignments will throw errors.

Wurst has the same 5 basic types (or primitives) as Jass: **null, boolean, int, real, string**

In this example you can see the static types of the variables on the left. If the assignment on the right isn't of the same type, it will throw an error.
```wurst
boolean b = true // true or false
int i = 1337 // integer, i.e. value without decimals
real r = 3.14 // floating point value
string s = "wurst is great" // text

// Examples
int i = 0 // OK
int j = "no" // Error
string r = "yes" // OK
string s = i // Error
```

### Syntax

WurstScript uses indentation based syntax to define Blocks. You can use either spaces or tabs for indentation, but mixing both will throw a warning.
In the following we use the word "tab" to refer to the tab character or to 4 space characters.

```wurst
// Wurst example. 'ifStatements' need to be indented
if condition
	ifStatements
nextStatements

// Other example using curly brackets (e.g. Java or Zinc)
if condition {
	ifStatements
}
nextStatements
// Other example using keywords (e.g. Jass)
if condition
	ifStatements
endif
nextStatements
```

In general newlines come at the end of a statement, except for the following cases:

- A newline is ignored if the line ends with `(` or `[`
- A newline is ignored before a line beginning with `)`, `]`,`.` or `..`
- A newline is ignored, if the line ends of begins with:
    `,`, `+`, `*`, `-`, `div`, `/`, `mod`, `%`, `and`, `or`, `->`

You can use this to break longer expressions or long parameter lists over several lines, or to chain method invocations:
```wurst
someFunction(param1, param2,
	param3, param4)

someUnit..setX(...)
	..setY(...)
	..setName(...)

new Object()
	..setName("Foo")
	..doSomething()
```

WurstScript tries to avoid excessive verbosity and symbols, to stay concise and readable.


### Basics

The following is a short overview of the core language features so you can get coding in no time. For a more detailed documentation of each topic,
refer to the dedicated chapters further down the page.

Wurst code is organized into **packages**. Any piece of code must be inside a package in order to be recognized.
Packages can **import** other packages in order to gain access to variables, functions, classes, defined in the imported package.
Packages have an optional _init_ block that is executed when the map is run.

Let's look at a classic **Hello World** example.

```wurst
package HelloWurst
// to use resources from other packages we have to import them at the top
import Printing

// the init block of each package is called when the map starts
init
	// calling the print function from the Printing package
	print("Hello Wurst!")
```

If you run this example, the message "Hello Wurst!" will be displayed after the map has loaded.

As you might have noticed, the ```import Printing``` will throw a warning, because it is already automatically imported. 
You can remove the import, it just served as illustration on how to import packages.

For more information about packages, refer to the packages section.

You can still use normal Jass syntax/code outside of packages - the Wurst World Editor will parse jass (and vjass, if jasshelper is enabled), in addition to compiling your Wurst code.

For more information about using maps with mixed wurst/jass code, refer to the "Using Wurst with legacy maps" section.

#### Functions

A **function** definition consists of a name, a list of formal parameters and a return
type. The return type is declared after the formal parameters using the **returns** keyword.
If the function does not return a value this part is omitted.
```wurst
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

function foo2(unit u) // parameters
	RemoveUnit(u)

function bar(int i) returns int // "returns" [type]
	return i + 4

function blub() returns int // without parameters
	return someArray[5]

function foobar()
	int i // local variable
	i = i + 1 // variable assignment
	int i2 = i // support for locals anywhere inside a function
```

#### Variables

Global variables can be declared anywhere in the top level of a package.
Local variables can be declared anywhere inside a function.
A constant value may be declared using the _constant_ or _let_ keyword.
Mutable variables are declared by using the _var_ keyword or the name of it's type.
```wurst
// declaring a constant - the type is inferred from the initial expression
constant x = 5
let x2 = 25
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
	let p = u.getOwner()
	var name = u.getName()
	print( name )
	var x = u.getX()
	var y = u.getY()
	let sum = x + y
```


With these basic concepts you should be able to do anything you already know from Jass.
We will touch on more advanced topics in the next chapters.
