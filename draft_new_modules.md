---
layout: default
title: New Modules
---


# The problems with old modules 

The old modules were done before subclassing existed. Now there are 
two conflicting concpets for inheritance, namely subclassing and modules.
Furthermore old modules could be nested 
All this makes the compiler more complicated than it should be in many places.

Old modules have limitations:


- One module can be used only once per class. But if we have something like 
	a timer module or the oncast module one often needs more than one instance.
- no generics	
- a module could not make assumptions about thistype (e.g. assume that
	thistype is a subtype of Entity)

# New Modules

The biggest change is in the usage of modules. 

- module uses now have a name
- all members of the module can only be accessed using the name of the module use
	- static module members are accessed using the module use name with an upper case
	  first letter, dynamic members with a lower case first letter.
- to change the behavior of a module functions can be overridden. Overridden functions
	must be placed nested inside the module use block
- modules can be generic. The type variables can be instantiated with every type 
	(not just types convertable to int as in generic classes and functions).
	
# Complete example

The following example shows all the features of new modules but 
does not make sense in any way.

	// module declaration with type parameters T and S
	// "for class Entity" makes sure that thistype is a subtype 
	// of entity. The "for class" clause is optional
	module WurstModule<T,S> for class Entity
		// module variable
		int x
		
		// variables can be private
		private int y
		
		// or static:
		static int z
		
		// normal function
		function foo1(T t) returns int
			// we can assign 'this' to a variable of type Entity here
			Entity e = this
			// or directly call some entity function
			let p = getPos()
			
		
			return 5
		
		// static/private functions
		private static function triggerAction()
		
		// abstract functions have to be overridden by module use
		abstract function someCallback(S s)
		
		// constructor and destructor as in classes
		construct()
			y = 2
			
		ondestroy
			print("kthxbye")
		
	// using a module in a class
	class SomeStupidEntity extends Entity
		
		int x = initX()
		
		// first usage
		use WurstModule<vec2,string> as bla1
			// implementing the abstract method:
			override function someCallback(string s)
				print(s)
			
		// second usage. Usage can also be private
		private use WurstModule<real,real> as bla2
			override function someCallback(real r)
				// here we can simply call functions from the outer scope
				// of the class
				doSomethingStupid(r)
		
		// example of using members of the module:
		private function doSomethingStupid(real r)
			// access dynamic variable
			let q = bla2.x
			// access static variable, use upper case name
			let w = Bla.z
			// We cannot access private variable y:
			// let e = bla.y
			
			// calling a function:
			bla1.foo1(vec2(3,5))
			
			
			
			
		
			
			
		





