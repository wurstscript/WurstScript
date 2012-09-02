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


TODO Download wurstpack
	
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

Wir beginnen mit ein paar einfachen mathematischen Ausdrücken. Ausdrücke können direkt in der Console getestet werden.
Zum Beispiel können wir "3+4" in die Console eingeben und erhalten als Ergebnis 7.

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
Manchmal kommt es auch zu Fehlern in der Console, die sich nicht mehr beheben lassen. In diesen Fällen lässt sich die Console 
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

	

### Übung 1

Für and und or gibt es eine ähnliche Regel wie die Punkt-vor-Strich-Regel. Benutze die Console um heraus zu finden, ob "and" oder "or" 
stärker bindet (also sich wie das "\*" Zeichen verhält).

### Übung 2
	
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
----------

Neben den grundlegenden Operatoren gibt es noch Funktionen. Ein Beispiel dafür ist die Quadrat-Wurzel-Funktion (englisch: square root). 

	> SquareRoot(9)
	res = 3.0     // real
	> SquareRoot(16)
	res = 4.0     // real





