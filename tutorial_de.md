---
layout: default
title: Wurst Tutorial
---


Einleitung
==========

Dieses Tutorial richtet sich an Personen ohne Programmiererfahrung. 
Der Anfang ist vielleicht für den Einen oder Anderen zu langsam. In diesem Fall
kann man ein Kapitel auch überspringen. Die Übungen am Ende eines Kapitels helfen
zu erkennen, wenn man zu viel übersprungen hat ;)

Das Tutorial enthält einige Anmerkungen in dreifachen Klammern (((wie diese))). 
Diese Anmerkungen sollten von Anfängern beim ersten Lesen übersprungen werden.
Für Leute, die das Tutorial gegenlesen, macht es das Lesen aber vielleicht interessanter.

Installation
------------

Wir werden in diesem Tutorial das Wurst-Plugin für Eclipse benutzen. Um dies zu installieren sind die folgenden Schritte notwendig:

1. Eclipse Classic herunterladen: [Eclipse Downloads](http://www.eclipse.org/downloads/)
2. Das Heruntergeladene Paket kann an einer beliebigen Stelle entpackt werden. Im entpackten Ordner befindet sich die Anwendung eclipse, welche wir nun starten.
3. Um das Wurst-Plugin zu installieren gehen wir in das Menü unter "Help -> Install New Software"
	Dort tragen wir nun die Update-Seite für das Plugin ein, indem wir auf "Add..." klicken.
	Als "Location" geben wir "http://peeeq.de/wurst/updateSite/" ein und als "Name" geben wir "Wurst" ein.
	Nun müssen wir nur noch die Update-Seite auswählen, dann in der Liste "Wurst IDE support" markieren und mit
	einem Klick auf "Next" fortfahren. Die Warnung während der Installation kann ignoriert werden.
	Nach der Installation muss Eclipse neu gestartet werden.


Außerdem brauchen wir noch das Wurstpack: [Download](http://peeeq.de/wurst/Wurstpack.zip)

Das Wurstpack beinhaltet den Worldeditor mit eingebautem Wurst-Compiler und es beinhaltet die Standard-Bibliothek.
	
Das erste Projekt
=================

Zuerst erstellen wir uns ein neues Projekt in Eclipse, um etwas mit Wurst spielen zu können.
Dazu wählen wir in Eclipse "File -> New ... -> New Project" und im folgenden Dialog "Wurst Project".
Nach einem Klick auf Weiter können wir den Namen des Projekts festlegen. Zum Beispiel auf "HelloWurst".

Als nächstes müssen wir Eclipse sagen, wo sich unsere Standard-Bibliothek befindet. Dazu muss die Datei
"wurst.dependencies", welche automatisch von Eclipse erstellt wurde angepasst werden, so dass darin 
der korrekte Pfad steht.

Jetzt können wir unser erstes Wurst-Paket erstellen. Dazu mit rechts auf das Projekt klicken und dann 
"New -> Wurst Package" wählen. Unter "File name" tragen wir "Hello" ein. Damit erstellt Eclipse dann ein 
leeres Paket, in das wir unseren Code schreiben können.

Ausdrücke
=========

Wir beginnen mit ein paar einfachen mathematischen Ausdrücken. Ausdrücke können direkt in der Konsole getestet werden.
Zum Beispiel können wir "3+4" in die Konsole eingeben und erhalten als Ergebnis 7.

	> 3+4
	res = 7     // integer

Wir können Wurst also als Taschenrechner verwenden! Aber Wurst schluckt nicht jeden Schrott, den man ihm gibt.
Ein Beispiel:
	
	> 3 + * 4
	Error in File <REPL> line 5:14:
	 Grammatical error: unexpected '*' expected: expression, name with type args, 'super'

Ein "Grammatical error" sagt uns, dass unsere Eingabe nicht mit der Grammatik der Wurstsprache übereinstimmt.
Das Multiplikations-Zeichen "\*" passt an dieser Stelle nicht. Statt dessen hätte Wurst dort gerne eine "expression" oder
ein paar andere Dinge. Fehlermeldungen von Wurst sind nicht immer einach zu verstehen, aber meistens reicht ein Blick an 
die Fehlerstelle um das Problem zu erkennen.
Manchmal kommt es auch zu Fehlern in der Konsole, die sich nicht mehr beheben lassen. In diesen Fällen lässt sich die Konsole 
durch die Eingabe von "reset" zurücksetzen.

Weiter mit Fehlerlosen Ausdrücken ... Auch kompliziertere Ausdrücke als "3+4" sind möglich:

	> 1+1+1+1+1+1+1*7
	res = 13     // integer

Wie man sieht kennt Wurst die Punkt-vor-Strich-Regel. Wenn man zuerst die Plus-Operatoren auswerten und danach das
Ganze versiebenfachen will muss man Klammern verwenden.

	> (1+1+1+1+1+1+1)*7
	res = 49     // integer
	
Aber was bedeutet eigentlich das "// integer" hinter dem Ergebnis? 
"//" ist einfach ein Trennzeichen, das sagt, dass der Rest der Zeile nur ein Kommentar ist. 
"integer" ist der Typ des Ergebnisses. Ein integer ist eine ganze Zahl. Eine halbe Zahl ist kein integer.
Aber was ist dann eine halbe Zahl? Nun, das lässt sich leicht herausfinden, indem wir Wurst nach dem Ergebnis von 1 geteilt durch 2 fragen:

	> 1/2
	res = 0.5     // real

(((In manchen anderen Sprachen hätte dieser Ausdruck den Wert 0)))
Aha, es ist ein "real". Auf Deutsch spricht man von reellen Zahlen und benutzt in der Regel ein Komma statt dem Punkt. 
Man sollte im Hinterkopf behalten, dass Wurst mit reellen Zahlen nicht genau rechnen kann, wie man in folgendem Beispiel sieht:

	> 1000000.0 + 0.1 - 1000000.0
	res = 0.125     // real
	
(((solche Fehler können Menschenleben kosten, siehe http://www.ima.umn.edu/~arnold/disasters/patriot.html)))
	
Was gibt es noch für Typen ausser ganzen Zahlen (integer) und ungenauen Komma-Zahlen (real)? 
Es gibt Wahrheitswerte. Fragen wir Wurst einmal, ob 3+4 kleiner als 8 ist:

	> 3+4 < 8
	res = true     // boolean
	
Diese Aussage ist offensichtlich wahr (true) und der Typ ist boolean. Der Typ ist benannt nach dem Mathematiker George Boole.
Solche Aussagen können mit den Operatoren "and", "or" und "not" miteinander kombiniert werden. 
Das folgende Beispiel drückt aus, dass 7 größer als 9 oder kleiner als 9 ist:

	> 7 > 9 or 7 < 9
	res = true     // boolean
	
Betrachten wir noch ein paar Beispiele:
	
	> 9 > 7 or 9 > 6
	res = true     // boolean
	> 9 > 7 and 9 > 6
	res = true     // boolean
	> 7 > 9 and 7 < 9
	res = false     // boolean
	> 3 < 4 and not 3 < 2
	res = true     // boolean
	> (9 > 7 or 9 > 6) == (9 > 7 and 9 > 6)
	res = true     // boolean

Der letzte Ausdruck ist relativ komplex, aber wenn man weiß, dass der linke Teil den Wert "true" hat und der rechte Teil
den Wert "true" hat, dann bleibt davon quasi nur noch "true == true" übrig, was natürlich wahr ist. Dieses Prinzip gilt
übrigens immer. Um den Wert eines komplizierten Ausdrucks in Wurst zu ermitteln, reicht es die Werte aller Teil-Ausdrücke zu kennen.
(((Anmerkung: Das gilt übrigens nicht für Sprachen mit Macros)))
	
Als letzten Typen (für dieses Kapitel) betrachten wir Texte. Diese müssen in Wurst immer in Anführunszeichen gesetzt werden.

	> "Hallo Wurst"
	res = "Hallo Wurst"     // string
	
Der Name "String" kommt von dem Wort Zeichenkette. Man hängt mehrere Zeichen aneinaner um einen Text zu bekommen. Interessanterweise
kann man mit Strings auch rechnen (((Anmerkung: Strings bilden mit dem Plus Operator ein Monoid mit dem leeren String als neutrales Element))):

	> "W" + "u" + "rst"
	res = "Wurst"     // string
	> "4"+"5"
	res = "45"     // string


Wurst bietet die folgenden Operatoren:

	+			// Addition
	-			// Subtraktion
	*			// Multiplikation
	/			// Division 
	div			// ganzzahlige Division
	mod			// Rest der ganzzahligen Division, es gilt 0 <= (x mod y) < y
	and			// Konjunktion
	or			// Disjunktion
	<			// kleiner
	<=			// kleiner oder gleich
	>			// größer
	>=			// größer oder gleich
	==			// gleich
	!=			// ungleich
	
	

#### Übung 1.1

Für and und or gibt es eine ähnliche Regel wie die Punkt-vor-Strich-Regel. Benutze die Konsole um heraus zu finden, ob "and" oder "or" 
stärker bindet (also sich wie das "\*" Zeichen verhält).

#### Übung 1.2
	
Rechne den Wert für folgenden Ausdruck auf dem Papier aus und schreibe für jeden Teil-Ausdruck den Typ auf:
	
	not (not ((3 + 4)*7 < 10 or 100 != 40) and 10. >= 100 / 15)
	
	
	
Variablen
----------	

Mit Variablen kann man sich den Wert eines Ausdrucks merken. Alle Ausdrücke, die wir bisher eingegeben haben wurden in 
einer Variablen mit Namen "res" gespeichert. Diese Variable kann man dann in späteren Ausdrücken wieder verwenden.

	> 5
	res = 5     // integer
	> res + 2
	res = 7     // integer
	> res * 3
	res = 21     // integer

Man kann auch Variablen mit anderen Namen erstellen. Dazu kann man eine *let* Anweisung verwenden.
	
	> let x = "su"
	x = "su"     // string
	> let y = "per"
	y = "per"     // string
	> x+y
	res = "super"     // string
	
Statt dem Ausdruck *let* kann man auch den Typen der Variable angeben. Dies ist manchmal hilfreich, da dadurch Fehler
früher erkannt werden:

	> int i = 42
	i = 42     // integer
	> string s = "42"
	s = "42"     // string
	> int j = "42"
	Error in File <REPL> line 9:0:
	 Cannot assign string to integer

Das ist im Prinzip auch vorerst alles was man über Variablen wissen müssen. Wichtig ist noch, dass nun der gleiche Ausdruck 
verschiedene Werte haben kann. Der Wert eines Ausdrucks hängt nun davon ab, welchen Zustand die Variablen momentan haben.

	
Funktionen
===========

Neben den grundlegenden Operatoren gibt es noch Funktionen. Ein Beispiel dafür ist die Quadrat-Wurzel-Funktion (englisch: square root).

    > SquareRoot(9.0)
    res = 3.0     // real

Hier haben wir die Funktion SquareRoot mit dem *Parameter* 9 aufgerufen. 
SquareRoot ist eine Funktion, welche einen real als Parameter nimmt und ein real zurück gibt. 
Es ist also eine richtige Funktion im mathematischen Sinn.

Eine nicht so mathematische Funktion ist "print". 
Diese nimmt einen string als Parameter und gibt nichts zurück. 
Dafür hat sie aber einen Seiteneffekt, wenn sie aufgerufen wird: Sie gibt ihren Parameter auf der Konsole aus.

	> print("Hello Wurst")
	Hello Wurst

Wir wollen nun unsere erste eigene Funktion schreiben. 
Eigene Funktionen können nicht in der Konsole definiert werden. 
Deshalb schreiben wir diesen Kode in unser Paket "Test", welches wir zu Beginn des Tutorials erstellt hatten (Also in das große Editor-Fenster).

Eine Funktions-Definition besteht aus einem Name, einer Liste von Parametern, einem Rückgabetyp und einer Implementierung.

Beginnen wir mit einem einfachen Beispiel. Eine Funktion "average", die zwei real als Parameter nimmt und einen real zurück gibt. 
Die Funktion soll den Mittelwert der beiden Parameter zurückgeben. 
Zum Beispiel soll der folgenden Ausdrücke wahr sein: 
	
	average(2,3) == 2.5
	
Die fertige Implementierung sieht dann so aus (Erklärung folgt gleich):

	package Test

	function average(real x, real y) returns real
		return (x + y) / 2


Nachdem wir die Funktion definiert haben, können wir sie in der Konsole benutzen. 
In der Regel sollte man die Datei speichern, bevor man neue Funktion in der Konsole verwendet, sonst kann es passieren, dass nicht 
alle Änderungen übernommen werden.

	> average(2,3) == 2.5
	res = true     // boolean
	> average(3,8)
	res = 5.5     // real

Betrachten wir die Definition der Funktion nun etwas genauer:

![Funktions Definition](assets/images/tut1_function.png)

- Jede Funktion hat einen Namen
- Eine Funktion hat eine Liste von Parametern. Jeder Parameter hat einen Typ und einen Namen. 
	Parameter können wie andere Variablen gelesen werden. Man kann sie aber nicht verändern.
- Der Rückgabetyp gibt an, welchen Typ die Funktion zurückgibt.
- Die Implementierung bestimmt was die Funktion macht. Eine Implementierung besteht in der Regel aus
	einer Liste von Anweisungen, wobei jede Anweisung in einer neuen Zeile steht. In unserem Beispiel haben wir nur 
	die *return* Anweisung verwendet. Diese Anweisung gibt den Rückgabewert der Funktion an. Hinter dem *return* kann
	ein beliebiger Ausdruck stehen.

	Wichtig ist, dass die Implementierung weiter eingerückt ist (das heisst mehr Lehrzeichen/Tabs vor dem Beginn der Zeile). 
	Dieser Mechanismus wird in Wurst immmer verwendet, um auszudrücken, dass Zeilen zu einer anderen Zeile gehören.
	In diesem Fall drückt sie aus, dass die Implementierung zur Funktion gehört.
	
Anweisungen
-----------

In diesem Kapitel werden wir nun einige weitere Anweisungen kennen lernen, die man zum implementieren von Funktionen benötigt.

Da wären zuerst einmal die Variablen Definitionen, die wir schon aus der Konsole kennen. Diese kann man zum Beispiel verwenden
um die komplizierte Formel aus der average Funktion in einzelne Teile aufzuteilen, denen man dann sinnvolle Namen geben kann:

	function average(real x, real y) returns real
		let sum = x + y
		let average = sum / 2
		return average

Funktionsaufrufe sind ebenfalls gültige Anweisungen. Dies können wir nutzen um mit der print Funktion die Zwischenergebnisse auszugeben.
Weil die print Funktion einen string als Parameter erwartet, wir aber einen real ausgeben wollen, müssen wir den Wert zuerst umwandeln.
Dafür gibt es die eingebaute Funktion R2S (der Name ist eine Abkürzung für real to string).

	function average(real x, real y) returns real
		let sum = x + y
		print("Die Summe ist " + R2S(sum))
		let average = sum / 2
		return average

Die Ausgabe von Zwischenergebnissen kann manchmal sehr hilfreich sein, um Fehler zu finden.

### If Anweisung

Bisher sind unsere Anweisungen recht langweilig, denn sie werden einfach nur nacheinander ausgeführt. 
Interessant wird das ganze erst, wenn wir den Kontrollfluss verändern können. Eine Möglichkeit, den Kontrollfluss zu verändern,
ist die bedingte Ausführung. Diese betrachtet den Wert eines Ausdrucks. Ist der Wert true, dann wird die eine Anweisung ausgeführt,
ist der Wert false, wird eine andere Anweisung ausgeführt. Damit lässt sich dann zum Beispiel eine Funktion "maximum" implementieren,
welche die größere von zwei Zahlen zurück gibt:

	function maximum(real x, real y) returns real
		if x > y
			return x
		else
			return y
	
Wenn der Ausdruck hinter dem *if* wahr ist (also wenn x größer als y ist) wird die Anweisung `return x` ausgeführt. 
Ansonsten wird die Anweisung `return y` ausgeführt. Insgesamt gibt die Funktion also die größere Zahl zurück. 
Man beachte, dass hier wieder die Einrückung angibt, dass die Anweisungen zum if-Teil oder else-Teil gehören.

Der else-Teil kann auch weg gelassen werden. Ein Beispiel hierfür ist eine Funktion maximum3, welche die größte von drei Zahlen bestimmt.
Hier verwenden wir eine Variable `result`, um uns zu merken welche Zahl am größten ist. *var* und *let* Variablen sind im Prinzip identisch, aber 
Variablen, die mit `let` definiert wurden, dürfen später nicht mehr verändert werden. In diesem Beispiel wollen wir die Variable `result`
verändern, wenn wir eine größere Zahl finden. Deshalb müssen wir `var` verwenden.

	function maximum3(real x, real y, real z) returns real
		var result = x
		if y > result
			result = y
		if z > result
			result = z
		return result

#### Übung 2.1

Implementiere die Funktion maximum3 ohne eine Variable zu benutzen.

#### Übung 2.2

Implementiere die Funktion maximum3 mit nur einer Anweisung, indem du die Funktion maximum (für zwei Zahlen) verwendest.

#### Übung 2.3

Die Funktionen `bla`, `blub` und `naund` unterscheiden sich nur durch ihre Einrückung. 
Was berechnen die Funktionen? Vereinfache jede Funktion so, dass sie nur aus einer `return` Anweisung besteht (und keine anderen Anweisungen, die Implementierung
soll also auf eine Zeile reduziert werden) und immer noch das gleiche macht.

Hinweis: `z++` ist eine Abkürzung für `z = z + 1`

Tipp: Operatoren wie `and`, `or`, `not` könnten hilfreich beim vereinfachen sein.


	
	function bla(boolean x, boolean y) returns boolean
		var z = 0
		if x
			z++
			z++
		if y
			z++
		return z == 1
		
	function blub(boolean x, boolean y) returns boolean
		var z = 0
		if x
			z++
		z++
		if y
			z++
		return z == 1
		
	function naund(boolean x, boolean y) returns boolean
		var z = 0
		if x
			z++
			z++
			if y
				z++
		return z == 1


#### Übung 2.4

Implementiere ein Stein-Schere-Papier-Spiel für 2 Spieler. 

- Das Spiel besteht aus einer einzigen Funktion, welche zwei Parameter hat.
- Der erste Parameter gibt an, welche Wahl der erste Spieler getroffen hat. Der Parameter hat Typ integer, wobei
	die Zahl 0 für Stein steht, 1 für Papier und 2 für Schere. Damit das Programm besser lesbar ist kann man Konstanten definieren (siehe Vorlage)
	und die Konstanten statt den Zahlen verwenden.
- Der zweite Parameter gibt analog zum ersten an, welche Wahl der zweite Spieler getroffen.
- Die Funktion hat keinen Rückgabewert, sondern benutzt die print Funktion, um auszugeben, wer gewonnen hat.
- Wenn beide Spieler die gleiche Wahl getroffen haben soll "Unentschieden" ausgegeben werden. 
	Ansonsten soll "Spieler 1 gewinnt" bzw. "Spieler 2 gewinnt" ausgegeben werden.
- Papier gewinnt gegen Stein
- Stein gewinnt gegen Schere
- Schere gewinnt gegen Papier
	
	

Hier ist die Vorlage mit den Konstanten und dem Anfang der Funktion:

	constant int rock = 0
	constant int scissors = 1	
	constant int paper = 2
		
	function rockPaperScissors(int player1choice, int player2choice)
		print("Implementierung fehlt")

Und so sollte es aussehen, wenn man das Spiel auf der Konsole "spielt":

	> rockPaperScissors(paper, rock)
	Spieler 1 gewinnt
	> rockPaperScissors(rock, rock)
	Unentschieden
	> rockPaperScissors(scissors, rock)
	Spieler 2 gewinnt


	
Zusatzaufgabe: Implementiere das Spiel mit nur einer if-Anweisung. 
Tipp: Der Operator `mod` berechnet den Rest einer Division (zum Beispiel 5 mod 3 == 2).


### For Schleife

### While Schleife









