---
title: Wurst for vJass Users
sections:
- Why not vJass?
- Why wurst?
- On performance
- Is vJass bad then?
- Is wurst good?
- Summary
- FAQs
---

{: .answer}
### *&nbsp;*{: .fa .fa-exclamation-circle} This guide expects the reader to be familiar with vJass.

{: .answer}
### *&nbsp;*{: .fa .fa-exclamation-circle} This tutorial is in the format of an informal discussion, but many of the key points are backed by well understood, high-level programming paradigms.
------

When I (evangelize?) discuss wurst, I tend to hear weird arguments against using it.
Some of them are interesting, like "well I can't justify learning it", or "my map is already using vJass", but other arguments tend to be outlandish and worth some education.
More concretely, lots of arguments about wurst are spoilt by misunderstanding or misinformation.

For what it's worth, wurst is an easy language to learn, and you can write wurst in a map that use vJass, but I digress.

As background: I switched to using wurst in the last couple years after fairly extensive experience writing vJass.
The switch was easy - I decided to "try" wurst on a toy map, and immediately realized that I aws previously wearing blinders, and indeed was *misled* by the public opinion of others.

So this tutorial is about learning, but it's also about justice.

## Why not vJass?

This is an argument that wurst users have historically done poor at.
The most interesting problems with vJass are not silly quirks like "extends array" or the macro system - those are small language smells that point to a larger problem - the language is a bit of an abomination for hacking things together.

The more interesting discussion revolves around understanding what vJass is not, as discussed below.

### Preprocessor

Fundamentally, vJass is a pre-processed language that gets simply transformed into Jass, and this has lots of side-effects:

{: .answer}
### *&nbsp;*{: .fa .fa-exclamation-circle} Key point: Type-Safety is the concept of checking that the values and functions a programmer uses are *consistent* - e.g. that the user never tries to give a timer to a function that takes an integer.

* vJass Generates code for you, but otherwise is very limited to abstraction - you just can't express higher level concepts safely in vJass because instances of structs are always integers - the compiler can't check anything for you - so type-safety is lost entirely.
  vJass does at least have some validation, but that's not being done by JassHelper - PJass is checking the emitted jass, but can't do anything smart at the vJass level.
* No "abstract syntax tree" (building blocks of code that a compiler understands) - so adding new features to JassHelper is very expensive and requires detailed knowledge of the system, which makes it hard for anyone else to take up ownership.
* Compiler-aided optimisation is impossible.  Wurst can inline functions, cull unused code, automatically null variables, etc.
  JassHelper can inline 1-line functions, but this is pushing the boundaries of JassHelper's capabilities.

### Limited tooling

JNGP/WEX are excellent tools because they attempt to *centralise tooling* into an integrated experience.

Wurst also centralises tooling - to your code editor, intead of to your world editor.
This adds boundless value because code editors are designed from scratch to be extensible, whereas our world editors have to hack the blizzard interface using Grimoire.

* You can work on your map without wc3 or any particular OS - and you can even get compiler feedback.
* Code highlighting, autocomplete, Jump-to-declaration - these are features that TESH *can't* do.
* Code gets parsed live - the feedback loop for warnings and errors is in real time.

It can feel unapproachable to be launching a map magicaly using a code editor, but it becomes second nature very fast.

### Fragmented

There's more than one JassHelper, and they're hardly different.
Indeed, some very ubiquitous vJass libraries don't even work with the original JassHelper specification!

### Typeless

Because vjass can only expand your code into Jass, it can't make any guarantees about the type of values.
This leads to tricky attaching and detaching of struct intances to timers and the like.

Indeed, it's possible to cast instances of objects to integer in Wurst too - but that's frowned upon!

Wurst at least gives you the capability to prevent entire classes of error like this.

### No Scope for Improvement

Maintaining JassHelper is *hard*, and the language specification designer isn't active anymore.

Contrast that with the [33 commits](https://github.com/wurstscript/WurstScript/commits/master) so far this month on WurstScript.

Of course, there are costs to using a language that's actively developed too - strange bugs are more numerous, and occasionally the compiler has a regression - but this is a small price to pay.

## Why wurst?

### integrated developer experience with *code* first

As discussed above, wurst also provides a sleek development experience - but unlike vJass, it puts the code first.

### A true compiler

Wurst is a compiled language, which fundamentally gives it advantages and scope for enormous, leaping improvements, like:

* updates that improve performance, by way of optimiser changes
* debug information for failure scenarios - including in-game stack traces
* Automatic nulling of variables to prevent tedious leaks

### Higher levels of abstraction

The basic wurst language implemented is a higher level language that lets you express more while writing less.

Have you ever had an experience with vJass where you slammed out a hundred lines that implemented a stack or linked list, and it all felt very natural and rewarding?

You'll have a similar experience when you write just a few lines of wurst and get the same functionality - these experiences are mind bending and highly valuable in the context of getting work done.

### Standard Libary

The convention in vJass is to share versioned libraries online as code snippets, but not in wurst.
Why is that?

It's not because wurst isn't worth sharing, or there's noone sharing it - wurst just fundamentally approaches the problem at a tooling level (much like cargo, pip, npm, etc).

Most of the basic requirements like data structures and familiar systems like damage detection are implemented in the standard library.
`import DamageType` and done.
Don't like the implementation?
Implement your own and publish it for use with the wurst dependency manager, offer your own improvements upstream to the standard library, or even write your own standard library - why not?

### Object editing

That cool LUA thing where object data gets accessed but kind of doesn't work these days?

Yeah, wurst has that on steroids.
Wurst has a feature called compiletime functions, where functions can be declared compiletime, and thence will be excuted when wurst runs.

Compiletime functions have a slightly different set of features than runtime functions, but this includes a convenience library for object editing.
Let's have the code speak for itself -

```wurst
public let DRAGON_ID = 'h000'

class DragonDefinition extends UnitDefinition
    construct(int id)
        super(id, 'nadr')

        setAttacksEnabled(0)
        setName("Dragon")
        setUpgradesUsed("")
        setStructuresBuilt("")
        setManaRegeneration(1.)
        setManaInitialAmount(0)
        setManaMaximum(400)
        setFoodCost(1)
        setSpeedBase(300)
        setTooltipBasic("Dragon")
        setTooltipExtended("An evolving beast with special powers.")
        setNormalAbilities("")
        setRace(Race.Human)
        setScalingValue(1.)

@compiletime function dragon()
    new DragionDefinition(DRAGON_ID)
```

## On performance

This subject is scary complicated, but it's something people often bring up when discussing vJass and indeed wurst.

Let's get some basics down:

* Coders interested in performance tend to appreciate that in vJass, the shape of the emitted jass tends to be predictable and close to to the low level function designed.
  This lets them reason about performance.
* Higher level languages like C++ use an *optimizer* to improve performance by doing things like automatically unraveling loops, inlining functions, and throwing away unused data or code paths.
* In the past, higher level languages that still provide system-level interaction tended to dominate this area (c++), although advances in compiler technologies has led to languages like rust and go, that manage to show how a programming language can very well provide a high level of abstraction while also being blazing fast.

{: .answer}
### *&nbsp;*{: .fa .fa-exclamation-circle} Key point: Performance and Level of Abstraction are orthogonal concepts - and writing higher level code does not necessarily make it slower.

Standards:

In vJass, the standard way to write code is the maximally low-level and/or accepted way to implement some function.
This leads to things like "use array extends when possible" and "prefer unit indexer".

In wurst, the standard way to write code is the maximally *maintainable* way to implement that function.
This encompasses things like:

* readability
* documentation
* comfortable level of abstraction and formatting

*This is actually harder, because the design level thinking is less obvious, and the coder is being pulled in many directions instead of just the standard route.*
The reason this is better is because *any level of readability improvement, documentation, or comfort* is better than none/flat (as in the case of vJass).

{: .answer}
### *&nbsp;*{: .fa .fa-exclamation-circle} Key point: Thinking about performance in wurst, and indeed many high level programming languages, is a pitfall, and for newcomers this may feel unfamiliar - you have to actively change your behavior to get the most out of wurst.

{: .answer}
### *&nbsp;*{: .fa .fa-exclamation-circle} Key point: Let the compiler/optimiser do the work for you. Instead, write the code that feels right.

## Is vJass bad then?

For many reasons vJass is still better than plain jass, but JassHelper is inherently a fairly basic preprocessor, so you can't have expectations about it being good enough, and *ought not* to settle for it being the best, or the standard.

More to the point: there was a time when vJass was absolutely the best option for mapping in wc3 - let this be a lesson in economics about being first to market :)

## Is wurst good?

Yes.
Wurst is more in line with modern, widely accepted programming language design goals.
Wurst provides higher levels of abstraction that let you write more expressive code in less time, without sacrificing performance.
Wurst's status as an actively developed, compiled language means that there is scope to improve it in any of those orthogonal contexts.

Wurst code can break, and things change with it from month to month.
Wurst can have bugs.
But on average, the value of using wurst just vastly outweighs those negatives - and indeed, most arguments I hear about wurst have nothing to do with these issues.

### But what about cJass, Zinc, vrJass?

No jass compiler that I've seen comes close to the capabilities of wurst, and many of these are just toy projects.
Looking at the state of those projects actually just detracts from understanding the value of wurst.

## Summary

Wurst lets you write better code, faster.

Wurst cuts out some of the warts of vJass in favor of more standard, type-safe ways of approaching the same problem.
For example, there is no macro system - instead, generics let you write types that are modular.

Furthermore, it's a goal of the wurst community to be explicitly about productivity - all this evangelism about language design can only go so far, but the killer argument is how much efficiency can be derived from using wurst, and that starts with the standard library.

## FAQs

{: .question}
### *&nbsp;*{: .fa .fa-question-circle} Wurst seems cool and worth checking out, but I can't get past the syntax. Why does it look like python? I prefer the style of [c, Java, ECMA]

I get this question a lot, but it comes at a weird angle and surprises me.
Let's talk about four different things related to this:

#### The value of syntax

The syntax of a programming language does have implications - what's possible, what's convenient, aesthetics, etc, and indeed wurst's syntax style affects how we write wurst, but I tend to get this question when people aren't actually thinking about these properties.

Instead, the question is about how style affects reading and writing, at a behavioral level.
Programmers who use one language exclusively tend to develop strong expectations for how fast or slow they can read code, and what sort of flow their understanding takes as they traverse a file.

Indeed, this is different for wurst for those highly familiar excluively with c or JASS.
I would compel the reader to bite the bullet and dive into wurst anyway, as these *behavioral* reading and thinking patterns are a small factor in writing and maintaining code, and your ability to understand code will broaden as you experience other styles.

#### Functional and Imperative style

I'm not going to talk about the value of Functional programming here, but rather, about style.
In my experience, people tend to have visited functional programming style briefly, and then *unintentionally* dismissing code or programming style that reminds them of that experience, even if the code/style under scrutiny is vastly different.
More concisely, they judge a book by its cover.

*How much* some programming language is imperative, declarative, functional, strongly typed, or object oriented, are *highly orthogonal concepts*, which people tend to struggle with.
With that in mind, it would be a shame to dismiss every language based on reasons like...

* too much or not enough imperative
* too much or not enough declarative
* too much or not enough functional
* too much or not enough strongly typed
* too much or not enough object oriented

Because you'll leave yourself misunderstanding a very wide range of concepts.
Casual users of wurst would have a hard time deciding "how strongly typed" or "how functional" the language is - so they also shouldn't feel qualified to dismiss it in the context of one of those topics.

{: .answer}
### *&nbsp;*{: .fa .fa-exclamation-circle} Key point: Wurst provides the *capability* to write high-level code, but isn't otherwise anything like python or Javascript - wurst is a strongly typed language.

One last thought: just because wurst supports some abstract concepts like generics, closures, and iterators, doesn't mean you're forced to use them.
On the contrary, I would claim that even very basic, imperative looking wurst will have fewer bugs and run faster than similarly styled vJass.

#### Picking your battles

This is more fundamental.
It isn't interesting to say that wurst's whitespace/block/mutability *style* is grounds for dismissal, because the arguments I have to raise to discuss this are so far beyond what's necessary to talk about the value of wurst.

Wurst's killer features are just *more interesting to talk about*, so dismissing them by talking about python style is (usually unintentionally) a worthless strawman.

#### Jurst

I've saved the best for last, because this is such a middle finger to the whitepace argument.
*You can write wurst with block-style formatting. It's allowed.*
Just see the "jurst" section in the manual.
