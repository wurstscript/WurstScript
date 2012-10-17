---
layout: default
title: The Wurstscript Standardlibrary
---

The Wurstscript Standardlibrary (standardlib for short) provides alot of built-in functionality that comes with the Wurstcompiler.
It contains many useful packages, such as the handle packages that include many extensionfunctions 
in order to use with nativetypes.
Next to the extensionfunctions for natives, there are custom constructs like 2d and 3d vector tuples, 
Timer Data-attachment, Data structures, moveable Effects, Knockback and more.
Not only are all the included packages great for creating a map faster and easier, but also
showcase most of WurstScript's features and should be used as reference and help.

In this little introduction to the standardlib we will go over the most important packages and talk about using the premade systems.#
Of course we know, that most advanced vJass-coders like to program/use their own librarys and codesegments
and we also know that the packages in the stdlib aren't always the best solutions or most performant,
but they offer a great and quick start into coding with wurstscript, and can be enhanced with your feedback [issue tracker at GitHub](https://github.com/peq/WurstScript/issues/new).

## Collections 

### Lists 

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