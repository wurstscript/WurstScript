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
		testAssertErrorsLines(true, "Cannot assign A<--C",  
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

}
