---
title: Manual
---


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


## Using Wurst with legacy maps

The Wurstpack World Editor will compile and link your wurst packages into your map even if that map already has GUI/jass/vJass - Wurst will run in parallel to that.

For the more adventurous, Wurst also has a jass-like dialect called Jurst, and supports automatic extraction of vJass to Jurst.
This feature should be treated as beta.

### Jurst

Jurst is a dialect of Wurst, which has the same features as Wurst, but with a Syntax similar to vJass.
You can use Jurst to adapt vJass code, but there are still a few manual steps involved, because of the difference in supported features.

In general, Jurst is less strict than Wurst.
The following code fragments are each valid Jurst:

    library LooksLikeVjass initializer ini
        private function ini takes nothing returns nothing
            local trigger t = CreateTrigger()
            t = null
        endfunction
    endlibrary

and

    package NearlyTheSameAsWurst
        function act()
            print(GetTriggerUnit().getName() + " died.")
        end

        init
            CreateTrigger()..registerAnyUnitEvent(EVENT_PLAYER_UNIT_DEATH)
                           ..addAction(function act)
        end

    // Note: "end" intentionally not present here.

As you can see, Jurst is able to access wurst code including packages from the wurst standard library, such as `print`.
However, you should not try to access Jurst (or jass/vjass) code from Wurst packages.

The especially keen can read the [Jurst Language Definition](https://github.com/peq/WurstScript/blob/master/de.peeeq.wurstscript/parserspec/Jurst.g4) for an exact reference of legal Jurst.

### Automatic Extraction of vJass to Jurst

One useful way of working with legacy maps is by extracting existing vJass to Jurst automatically.
In Eclipse, you can right-click on a map and export all the custom text triggers into Jurst files.
The files will be stored in the `wurst/exported/` folder.

![Extract to Files](assets/images/extractToFiles.png)

After doing this you'll want to manually delete the members in the trigger editor.
Be warned that the extract-to-files feature is likely to produce at least some files with syntax errors, so be sure to back up your map before proceeding.

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
