---
layout: default
title: WurstScript Präsentation inWarcraft
---

Wurst ist eine Sprache, die ähnlich wie vJass, cJass oder zinc zu Jass übersetzt werden kann. 

Warum eine neue Sprache? 
===========================

## Editor Support

vJass basiert hauptsächlich auf Textersetzungen (vJass modules, textmacros). Das macht es schwer einen 
guten Editor, mit Features wie autocomplete, für vJass zu schreiben.

Wurst setzt auf sehr einfach strukturierten Code, welcher in einer einzigen Compiler-Phase analysiert werden kann. 
vJass benötigt hingegen [mehrere Phasen](http://www.wc3c.net/vexorian/zincmanual.html#compileerror).

Der Editor Support für Wurst ist als Eclipse Plugin realisiert. Mehr Informationen dazu folgen später.

## Typsicherheit

vJass ist nicht typsicher. Als Beispiel nehme man den folgenden Code:

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

vJass prüft nicht, ob in einer Variable vom Typ B auch wirklich nur Objekte vom Typ B gespeichert sind.
Dies kann zu Fehlern führen, die man erst spät entdeckt oder man bekommt einen pJass Fehler,
der teilweise nicht leicht zu verstehen ist. 

Der entsprechende Wurst-Code sieht folgendermaßen aus (Screenshot aus dem Eclipse Plugin):

![Screenshot](./assets/images/error_sample1.png)

## Weniger schreiben, einfacher Lesen

vJass hat einige redundate Syntax-Elemente. Beispiele dafür sind "set", "call"
oder "takes nothing returns nothing". Gleichzeitg hat vJass sehr wenige Elemente, 
die es erlauben Code zu schreiben, der besser lesbar ist. Insbesondere gibt es nur 
eine Form von Schleife.



Features
========

Hier nun einige ausgewählte Features von Wurst:

## Local Type Inference + Local Variable Freedom

Der Typ von lokalen Variablen wird automatisch aus dem Startwert abgeleitet, so dass man sich ein wenig Schreibarbeit schreiben kann.
Außerdem können Variablen an einer beliebigen Stelle in der Funktion definiert werden, nicht nur am Anfang.

	// "let" definiert eine lokale konstante
	let harald = CreateUnit(Player(0), 'hfoo', x, y, 0)
	// "var" definiert eine lokale variable
	var otto = CreateUnit(Player(0), 'hfoo', x, y, 0)
	// man kann auch den typ aufschreiben
	unit heinz = CreateUnit(Player(0), 'hfoo', x, y, 0)

## Extension functions

Extension functions erlauben es, einem vorhanden Typ neue Funktionen zu geben, welche mit der Punkt-Syntax von Methodenaufrufen benutzt werden können.

Deklaration: 

	public function unit.getOwner() returns player
		return GetOwningPlayer(this)
		
	function player.getName() returns string
		return GetPlayerName(this)

Benutzung:
	
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

	// Ein Vektor besteht aus zwei reals x und y
	public tuple vec2( real x, real y )
	
	// Operator overloading functions:
	public function vec2.op_plus( vec2 v )	returns vec2
		return vec2(this.x + v.x, this.y + v.y)
	
	public function vec2.op_minus( vec2 v )	returns vec2
		return vec2(this.x - v.x, this.y - v.y)
		
	public function vec2.op_mult(real factor) returns vec2
		return vec2(this.x*factor, this.y*factor) 
	
	// Skalarprodukt:
	public function vec2.dot( vec2 v ) returns real
		return this.x*v.x+this.y*v.y
	
	// Laenge:
	public function vec2.length() returns real
		return SquareRoot(this.x*this.x+this.y*this.y)
	
	// normalisierter Vektor:
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

Benutzung:
	

	// Geschoss dreht sich um die verfolgte Einheit (variable following)
	function followHero()
		// Winkel erhoehen
		angle += TURN_SPEED*DT
		// neue Position berechnen:
		let newPos = following.getPos().polarOffset(angle, RANGE)
		// aktuelle Geschwindigkeit berechnet sich aus Differenz zwischen alter und neuer Position
		vel = (newPos - pos)*(1/DT)
		pos = newPos
		fx.setPos(pos.x, pos.y)
		SetUnitFacing(fx, (angle + bj_PI/2)*bj_RADTODEG)
		checkCollisions()
		
	// Geschoss bewegt sich vorwaerts
	function moveForward()
		// Geschwindigkeit auf die Position addieren:
		pos = pos + vel*DT
		if not pos.inBounds()
			destroyed = true
		else
			fx.setPos(pos.x, pos.y)
			checkCollisions()

