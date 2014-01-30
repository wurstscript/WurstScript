---
layout: simple
title: The Wurstscript Standardlibrary
---

The Wurstscript Standardlibrary (standardlib for short) provides a lot of built-in functionality that comes with the Wurstcompiler.
Below you will find detailed documentation for most packages of the standardlib.
Additionally to the information provided here, most packages come with hot-doc for their functions.
Not only are all the included packages great for creating a map faster and easier, but also
showcase most of WurstScript's features and should be used as reference and help.

<div id="tableofcontents">loading TOC ...</div>

# Systems 

## Fx

The Fx package provides an Fx class used for moveable effects.

### MultiFx

Additionally to the Fx package there is the MultiFx package for Fx-Objects with multiable effects attached to.

# Data Structures 

Suppose you have an interface Closable and classes Door, Chest and Gate  implementing this interface. Furthermore the interface I
provides functions "open" and "close". You can use Lists to store different kind of Closable things in one place.


	Gate gate = ...
	Door door = ...
	Chest chest = ...
	// create a new list
	LinkedList{Closeable} closeables = new LinkedList{Closeable}()
	// add the different items to the list
	closeables.add(gate)
	closeables.add(door)
	closeables.add(chest)
	// now close everything in the list
	for Closeable c1 in closeables
		c1.close()
	// and open everything again (but not the chest, as we want to keep our treasure)
	closeables.remove(chest) // remove the chest from the list
	for Closeable c2 in closeables
		c2.open()