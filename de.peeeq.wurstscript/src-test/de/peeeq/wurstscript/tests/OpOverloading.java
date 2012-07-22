package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class OpOverloading extends PscriptTest {

	
	@Test
	public void testOverloading1() {
		testAssertOkLines(true,
				"package test",
				"	native testSuccess()",
				"	class A ",
				"		int i = 2",
				"		",
				"		function op_plus(A a) returns int",
				"			return this.i + a.i",
				"	init",
				"		A a1 = new A()",
				"		A a2 = new A()",
				"		int result = a1 + a2",
				"		if result == 4",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void testOverloading1_2() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A ",
				"		int i = 2",
				"		",
				"		function op_minus(A a) returns int",
				"			return this.i - a.i",
				"	init",
				"		A a1 = new A()",
				"		A a2 = new A()",
				"		int result = a1 - a2",
				"		if result == 0",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void testOverloading1_3() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A ",
				"		int i = 2",
				"		",
				"		function op_mult(A a) returns int",
				"			return this.i * a.i",
				"	init",
				"		A a1 = new A()",
				"		A a2 = new A()",
				"		int result = a1 * a2",
				"		if result == 4",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void testOverloading1_4() {
		testAssertOkLines(true, 
				"package test",
				"	native testSuccess()",
				"	class A ",
				"		int i = 2",
				"		",
				"		function op_div(A a) returns real",
				"			return this.i / a.i",
				"	init",
				"		A a1 = new A()",
				"		A a2 = new A()",
				"		real result = a1 / a2",
				"		if result == 1",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void testOverloading2() {
		testAssertOkLines(true,
				"package test",
				"	native testSuccess()",
				"	tuple vec3( real x, real y, real z )",
				"",
				"	public function vec3.op_plus( vec3 v )	returns vec3",
				"			return vec3(this.x + v.x, this.y + v.y, this.z + v.z)",
				"",
				"",
				"	init",
				"		vec3 v1 = vec3(1.,1.,1.)",
				"		vec3 v2 = vec3(1.,1.,1.)",
				"		vec3 v3 = v1 + v2",
				"		if v3.x == 2",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void testOverloading2Abbreviation() {
		testAssertOkLines(true,
				"package test",
				"	native testSuccess()",
				"	tuple vec3( real x, real y, real z )",
				"",
				"	public function vec3.op_plus( vec3 v )	returns vec3",
				"			return vec3(this.x + v.x, this.y + v.y, this.z + v.z)",
				"",
				"",
				"	init",
				"		vec3 v1 = vec3(1.,1.,1.)",
				"		vec3 v2 = vec3(1.,1.,1.)",
				"		v1 += v2",
				"		if v1.x == 2",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void testOverloading3() {
		testAssertErrorsLines(true, "No operator overloading function",
				"package test",
				"	native testSuccess()",
				"	tuple vec3( real x, real y, real z )",
				"",
				"",
				"",
				"	init",
				"		vec3 v1 = vec3(1.,1.,1.)",
				"		vec3 v2 = vec3(1.,1.,1.)",
				"		vec3 v3 = v1 + v2",
				"endpackage");
	}
	
	@Test
	public void testOverloading4() {
		testAssertErrorsLines(true, "No operator overloading function",
				"package test",
				"	native testSuccess()",
				"	nativetype unit",
				"	init",
				"		unit u = null",
				"		unit u2 = null",
				"		unit u3 = u + u2",
				"endpackage");
	}
	
	@Test
	public void testOverloading_shortForm1() {
		testAssertOkLines(true,
				"package test",
				"	native testSuccess()",
				"	tuple vec(real x, real y, real z)",
				"	function vec.op_mult(real r) returns vec",
				"			return vec(this.x*r, this.y*r, this.z*r)",
				"	init",
				"		vec v = vec(1,2,3)",
				"		v *= 2.",
				"		if v == vec(2,4,6)",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void testOverloading_shortForm2() {
		testAssertErrorsLines(true, "expected real",
				"package test",
				"	native testSuccess()",
				"	tuple vec(real x, real y, real z)",
				"	function vec.op_mult(real r) returns vec",
				"			return vec(this.x*r, this.y*r, this.z*r)",
				"	init",
				"		vec v = vec(1,2,3)",
				"		v *= v",
				"		if v == vec(2,4,6)",
				"			testSuccess()",
				"endpackage");
	}


}
