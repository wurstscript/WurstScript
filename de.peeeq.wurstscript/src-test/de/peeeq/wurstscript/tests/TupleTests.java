package de.peeeq.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TupleTests extends WurstScriptTest {
	
	
	@Test
	public void simple() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	tuple vec(real x, real y, real z)",
				"	init",
				"		vec v = vec(1,2,3)",
				"		v.x = 4",
				"		vec u = v",
				"		u.y = 5",
				"		if v.x == 4 and v.y == 2 and u.y == 5",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void swap() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	tuple pair(real x, real y)",
				"	init",
				"		pair p = pair(1,2)",
				"		p = pair(p.y, p.x)",
				"		if p.x == 2 and p.y == 1",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void parameter() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	tuple vec(real x, real y, real z)",
				"	function sum(vec v) returns real",
				"		return v.x + v.y + v.z",
				"	init",
				"		let v = vec(4,5,6)",
				"		if sum(v) == 15",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void return_value() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	tuple vec(real x, real y, real z)",
				"	function blub(real x, real y) returns vec",
				"		return vec(x, y, 0)",
				"	init",
				"		let v = blub(4,5)",
				"		if v.x == 4 and v.y == 5 and v.z == 0",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void vecs() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	tuple vec(real x, real y, real z)",
				"	function vec.plus(vec other) returns vec",
				"		return vec(this.x + other.x, this.y + other.y, this.z + other.z)",
				"	init",
				"		let v = vec(1,2,3).plus(vec(4,5,6))",
				"		if v.x == 5 and v.y == 7 and v.z == 9",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	@Test
	public void inClass() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	tuple vec(real x, real y, real z)",
				"	class A",
				"		vec pos",
				"		construct()",
				"			pos = vec(1,1,1)",
				"		function foo()",
				"			pos.x = 3",
				"	init",
				"		A a = new A()",
				"		a.foo()",
				"		if a.pos.x == 3",
				"			testSuccess()",
				"endpackage"
			);
	}
	
	
}
