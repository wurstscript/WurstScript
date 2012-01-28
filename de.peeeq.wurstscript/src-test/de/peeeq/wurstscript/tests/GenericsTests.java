package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class GenericsTests extends PscriptTest {
	
	
	@Test
	public void identity() {
		testAssertOkLines(true,  
				"package test",
				"	native testSuccess()",
				"	function identity{A}(A a) returns A",
				"		return a",
				"	init",
				"		int x = identity(3)",
				"		if x == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	

	@Test
	public void function() {
		testAssertOkLines(true,  
				"package test",
				"	native testSuccess()",
				"	class List{T}",
				"		function iterator() returns Iterator{T}",
				"			return new Iterator{T}(this)",
				"	class Iterator{T}",
				"		T t",
				"		construct(List{T} t)",
				"			int x = 1",
				"		function hasNext() returns boolean",
				"			return true",
				"		function next() returns T",
				"			return t",
				"	class A",
				"	class B",
				"	class C",
				"	init",
				"		List{B} a = new List{B}()",
//				"		for B b in a",
				"		Iterator{B} iterator = a.iterator()",
				"		while iterator.hasNext()",
				"			B b = iterator.next()",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void identityFail1() {
		testAssertErrorsLines(true, "only be bound",  
				"package test",
				"	native testSuccess()",
				"	function identity{A}(A a) returns A",
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
		testAssertErrorsLines(true, "Cannot assign C",  
				"package test",
				"	function identity{A}(A a) returns A",
				"		return a",
				"	class C",
				"		int y",
				"	init",
				"		real x = identity(new C())",
				"endpackage"
			);
	}

}
