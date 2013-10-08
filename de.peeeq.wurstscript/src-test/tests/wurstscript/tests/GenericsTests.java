package tests.wurstscript.tests;

import org.junit.Test;

public class GenericsTests extends WurstScriptTest {
	
	
	@Test
	public void identity() {
		testAssertOkLines(true,  
				"package test",
				"	native testSuccess()",
				"	function identity<A>(A a) returns A",
				"		return a",
				"	init",
				"		int x = identity(3)",
				"		if x == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	

	@Test
	public void identity2() {
		testAssertOkLines(true,  
				"package test",
				"	native testSuccess()",
				"	class C",
				"	function identity<A>(A a) returns A",
				"		return a",
				"	init",
				"		C a = new C()",
				"		C b = identity(a)",
				"		if a == b",
				"			testSuccess()",
				"endpackage"
			);
	}
	

	@Test
	public void function() {
		testAssertOkLines(true,  
				"package test",
				"	native testSuccess()",
				"	class List<T>",
				"		function iterator() returns Iterator<T>",
				"			return new Iterator<T>(this)",
				"	class Iterator<S>",
				"		S t",
				"		construct(List<S> t)",
				"			int x = 1",
				"		function hasNext() returns boolean",
				"			return true",
				"		function next() returns S",
				"			return t",
				"	class A",
				"	class B",
				"	class C",
				"	init",
				"		List<B> a = new List<B>()",
//				"		for B b in a",
				"		Iterator<B> iterator = a.iterator()",
				"		while iterator.hasNext()",
				"			B b = iterator.next()",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void identityFail1() {
		testAssertErrorsLines(true, "real",  
				"package test",
				"	native testSuccess()",
				"	function identity<A>(A a) returns A",
				"		return a",
				"	init",
				"		real x = identity(3.14)",
				"		if x == 3.14",
				"			testSuccess()",
				"endpackage"
			);
	}

	@Test
	public void identityFail2() {
		testAssertErrorsLines(true, "Cannot assign C to real",  
				"package test",
				"	function identity<A>(A a) returns A",
				"		return a",
				"	class C",
				"		int y",
				"	init",
				"		real x = identity(new C())",
				"endpackage"
			);
	}
	
	
	@Test
	public void cellExample() {
		testAssertErrorsLines(true, "Wrong parameter type",  
				"package test",
				"	native testSuccess()",
				"	class Cell<T>",
				"		T elem",
				"		function set(T t)",
				"			elem = t",
				"		function get() returns T",
				"			return elem",
				"	class A",
				"	class B",
				"	init",
				"		Cell<A> c = new Cell<A>()",
				"		c.set(new B())",
				"endpackage"
			);
	}
	
	@Test
	public void implicitConversions() {
		testAssertOkLines(true,  
				"package test",
				"	native testSuccess()",
				"	class Cell<T>",
				"		T elem",
				"		function set(T t)",
				"			elem = t",
				"		function get() returns T",
				"			return elem",
				"",
				"	tuple bla(int z, int y)",	
				"	function blaToIndex(bla b) returns int",
				"		return b.z",
				"	function blaFromIndex(int i) returns bla",
				"		return bla(i, 2)",
				"	init",
				"		Cell<bla> c = new Cell<bla>()",
				"		c.set(bla(5, 3))",
				"		if c.get() == bla(5, 2)",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void implicitConversions2() {
		testAssertOkLines(true,  
				"package test",
				"	native testSuccess()",
				"	class Cell<T>",
				"		T elem",
				"		function set(T t)",
				"			elem = t",
				"		function get() returns T",
				"			return elem",
				"",
				"	tuple bla(int z, int y)",	
				"	function blaToIndex(bla b) returns int",
				"		return b.z",
				"	function blaFromIndex(int i) returns bla",
				"		return bla(i, 2)",
				"	init",
				"		Cell<bla> c = new Cell<bla>()",
				"		c.set(bla(5, 3))",
				"		c.set(c.get())",
				"		if c.get() == bla(5, 2)",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void implicitConversionsFail() {
		testAssertErrorsLines(true,"Could not find function blaFromIndex",  
				"package test",
				"	native testSuccess()",
				"	class Cell<T>",
				"		T elem",
				"		function set(T t)",
				"			elem = t",
				"		function get() returns T",
				"			return elem",
				"",
				"	tuple bla(int z, int y)",	
				"	function blaToIndex(bla b) returns int",
				"		return b.z",
				"	function blaaFromIndex(int i) returns bla",
				"		return bla(i, 2)",
				"	init",
				"		Cell<bla> c = new Cell<bla>()",
				"endpackage"
			);
	}
	
	@Test
	public void implicitConversionsFail2() {
		testAssertErrorsLines(true, "Parameter must be of type int",  
				"package test",
				"	native testSuccess()",
				"	class Cell<T>",
				"		T elem",
				"		function set(T t)",
				"			elem = t",
				"		function get() returns T",
				"			return elem",
				"",
				"	tuple bla(int z, int y)",	
				"	function blaToIndex(bla b) returns int",
				"		return b.z",
				"	function blaFromIndex(string i) returns bla",
				"		return bla(1, 2)",
				"	init",
				"		Cell<bla> c = new Cell<bla>()",
				"endpackage"
			);
	}
	
	@Test
	public void implicitConversionsAssign() {
		testAssertOkLines(false,  
				"type unit extends handle",
				"package test",
				"	native testSuccess()",
				"	class Cell<T>",
				"		T elem",
				"		function set(T t)",
				"			elem = t",
				"		function get() returns T",
				"			return elem",
				"	function unitToIndex(unit u) returns int",
				"		return 0",
				"	function unitFromIndex(int i) returns unit",
				"		return null",
				"	init",
				"		Cell<unit> c = new Cell<unit>()",
				"		Cell<unit> c2 = c",
				"endpackage"
			);
	}
	
	@Test
	public void implicitConversionFail() { // see bug #121
		testAssertOkLinesWithStdLib(false,  
				"package Test",
				"import LinkedList",
				"Table data",
//				"function effectToIndex(effect e) returns int",
//				"	return e.getHandleId()",
//
//				"function effectFromIndex(int index) returns effect",
//				"	data.saveFogState(0,ConvertFogState(index))",
//				"	return data.loadEffect(0)",
				
				"init",
				"	LinkedList<effect> fxs = new LinkedList<effect>()",
				"	for f in fxs",
				"		f.destr()",
				"endpackage");
	}
	
	@Test
	public void implicitConversionFailSimple() { // see bug #121
		testAssertOkLines(false,  
				"type effect extends handle",
				"package Test",
				"function effectToIndex(effect e) returns int",
				"	return 2",

				"function effectFromIndex(int index) returns effect",
				"	return null",
				
				"class List<T>",
				"	function get() returns T",
				"		return 0 castTo T",
				
				"init",
				"	List<effect> fxs = new List<effect>()",
				"	let f = fxs.get()",
				"endpackage");
	}

	
	@Test
	public void cast() { 
		testAssertOkLines(false,  
				"package Test",
				"native testSuccess()",
				"class Cell<T>",
				"	T o",
				"class A",
				"	function foo() returns int",
				"		return 5",
				"class B extends A",
				"	override function foo() returns int",
				"		return 6",
				"init",
				"	Cell<A> c = new Cell<A>()",
				"	c.o = new B()",
				"	B b = c.o castTo B"
				);
	}
	
	@Test
	public void generics_dispatch() { 
		testAssertOkLines(true,  
				"package Test",
				"native testSuccess()",
				"class Cell<T>",
				"	T o",
				"class A",
				"	function foo() returns int",
				"		return 5",
				"class B extends A",
				"	override function foo() returns int",
				"		return 6",
				"init",
				"	Cell<A> c = new Cell<A>()",
				"	c.o = new B()",
				"	if c.o.foo() == 6",
				"		testSuccess()"
				);
	}
	
	@Test
	public void generics_substitute1() { 
		testAssertOkLines(false,  
				"package Test",
				"native testSuccess()",
				"class A<T>",
				"	function bla(T t)",
				"class B extends A<C>",
				"class C",
				"init",
				"	let b = new B",
				"	b.bla(new C)"
				);
	}
	
	@Test
	public void generics_substitute2() { 
		testAssertOkLines(false,  
				"package Test",
				"native testSuccess()",
				"interface I<S,T>",
				"	function bla(T t, S s)",
				"		skip",
				"class A<U> implements I<U,D>",
				"class B extends A<C>",
				"class C",
				"class D",
				"init",
				"	let b = new B",
				"	b.bla(new D, new C)"
				);
	}
	
	@Test
	public void generics_substitute3() { 
		testAssertOkLines(false,  
				"package Test",
				"native testSuccess()",
				"interface I<S,T>",
				"	function bla(T t, S s)",
				"		skip",
				"interface J<T,S> extends I<S,T>",
				"	function foo()",
				"		skip",
				"class A<U> implements J<D,U>",
				"class B extends A<C>",
				"class C",
				"class D",
				"init",
				"	let b = new B",
				"	b.bla(new D, new C)"
				);
	}
	
	@Test
	public void generics_substitute() { 
		testAssertOkLines(false,  
				"package Test",
				"class A<T>",
				"	function bla(T a)",
				"class B extends A<MyType>",
				"	function do()",
				"		bla(new MyType)",
				"class MyType"

				);
	}
	
}
