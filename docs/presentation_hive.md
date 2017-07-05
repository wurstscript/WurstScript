---
layout: default
title: WurstScript Presentation hiveworkshop
---

Wurst is a Scripting language, which can, similar to vJass, cJass and zinc, be translated to Jass.


Why a new Scripting language?
===========================

## Editor Support

vJass is mostly based on rexreplacements and generation (vJass modules, textmacros).
This makes it hard to create a good editor for vJass, including features like autocomplete.

Wurst instead goes for simple, structured code, that can be analyzed in a single compiler-phase.
vJass however needs [more phases](http://www.wc3c.net/vexorian/zincmanual.html#compileerror).

The editor support for wurst is done as an eclipse plugin. More information follow later.

## Type-safety

vJass isn't typesafe. Look at this example:

*vJass Code*

	struct A
	endstruct

	struct B
	endstruct

	private function init takes nothing returns nothing
		local A a = A.create()
		local B b = A.create() // no error ...
		set a = 42 // no error ...
		set a = "bla" // pjass error
	endfunction

vJass doesn't check if a variable of type B only stores values of type B.
This leads to frequent errors, which can be discovered late or you recieve a pJass Error,
which isn't easy to understand.

The Wurst counterpart (Screenshot from the Eclipse Plugin):

![Screenshot](./assets/images/error_sample1.png)

## Less writing, more readability

vJass features several redundant Syntac-Elements. For example "set", "call"
or "takes nothing returns nothing". Additionally vJass doesn't have many features
that allow for writing readable, clear code. Particularly there only exists
one type of loop.


Features
========

Following is a selected List of features from Wurst:
(Look at the wurst manual to see a complete list)

## Local Type Inference + Local Variable Freedom

The type of local variables is automatically derived from the starting value,
which saves alot of writing time, especially when chaning the startingvalue to a different type.
Furthermore variables can be declared at any position in a function, nut just the beginning.

	// "let" defines a local constant
	let harald = CreateUnit(Player(0), 'hfoo', x, y, 0)
	// "var" defines a local variable
	var otto = CreateUnit(Player(0), 'hfoo', x, y, 0)
	// traditional way works too
	unit heinz = CreateUnit(Player(0), 'hfoo', x, y, 0)

## Extension functions

Extension functions allow to add specific functions to an already existing type,
which can be used via dot-syntax.


Declaration:

	public function unit.getOwner() returns player
		return GetOwningPlayer(this)

	function player.getName() returns string
		return GetPlayerName(this)

Usage:

	print(GetKillingUnit().getOwner().getName() + " killed " +
		GetTriggerUnit().getOwner().getName() + "!")


Extension functions haben zwei Vorteile gegenüber normalen Funktionen:

- Sie sind leichter mit der Autovervollständigung zu finden
- In einigen Fällen erhöhen sie die lebarkeit (GetPlayerName(GetOwningPlayer(u)) vs u.getOwner().getName())


## Tuples

Ein Tuple ist ein sehr einfacher Datentyp. Er erlaubt es mehrere Werte zu einem zu vereinen.
Dies erlaubt es Werte die zusammen gehören auch als zusammengehörig zu behandeln.
Im Gegensatz zu Klassen erzeugen sie keinen zusätzlichen Aufwand. Sie müssen nicht erstellt oder zerstört werden
und können wie eingebaute Datentypen (int, string, etc.) benutzt werden. Ein gutes Beispiel dafür sind Vektoren:

Definition:

	// A 2d vector with the components x and y
	public tuple vec2( real x, real y )

	// Operator overloading functions:
	public function vec2.op_plus( vec2 v )	returns vec2
		return vec2(this.x + v.x, this.y + v.y)

	public function vec2.op_minus( vec2 v )	returns vec2
		return vec2(this.x - v.x, this.y - v.y)

	public function vec2.op_mult(real factor) returns vec2
		return vec2(this.x*factor, this.y*factor)

	// dot product:
	public function vec2.dot( vec2 v ) returns real
		return this.x*v.x+this.y*v.y

	// length:
	public function vec2.length() returns real
		return SquareRoot(this.x*this.x+this.y*this.y)

	// normalized Vector:
	public function vec2.norm() returns vec2
        real len = this.length()
        real x = 0
        real y = 0
        if (len != 0.0)
            x = (this.x / len)
            y = (this.y / len)
        return vec2(x,y)

	public function vec2.polarOffset(real angle, real dist) returns vec2
		return vec2(this.x + Cos(angle)*dist, this.y + Sin(angle)*dist)

Usage:


	// A projectile homes the target unit(variable following)
	function followHero()
		// Increase angle
		angle += TURN_SPEED*DT
		// calculate new position
		let newPos = following.getPos().polarOffset(angle, RANGE)
		// current velocity is calculated from the difference between old and new position
		vel = (newPos - pos)*(1/DT)
		pos = newPos
		fx.setPos(pos.x, pos.y)
		SetUnitFacing(fx, (angle + bj_PI/2)*bj_RADTODEG)
		checkCollisions()

	// projectile moving forward
	function moveForward()
		// Add velocity to position
		pos = pos + vel*DT
		if not pos.inBounds()
			destroyed = true
		else
			fx.setPos(pos.x, pos.y)
			checkCollisions()

