package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class OpOverloading extends PscriptTest {

	
	@Test
	public void testOverloading1() {
		assertOk(true, 
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
				"");
	}
	
	@Test
	public void testOverloading1_2() {
		assertOk(true, 
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
				"");
	}
	
	@Test
	public void testOverloading1_3() {
		assertOk(true, 
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
				"");
	}
	
	@Test
	public void testOverloading1_4() {
		assertOk(true, 
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
				"");
	}
	
	@Test
	public void testOverloading2() {
		assertOk(true, 
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
				"");
	}
	
	@Test
	public void testOverloading3() {
		assertError(true, "is not defined for",
				"	tuple vec3( real x, real y, real z )",
				"",
				"",
				"",
				"	init",
				"		vec3 v1 = vec3(1.,1.,1.)",
				"		vec3 v2 = vec3(1.,1.,1.)",
				"		vec3 v3 = v1 + v2",
				"");
	}
	
	@Test
	public void testOverloading4() {
		assertError(true, "is not defined for",
				"	nativetype unit",
				"	init",
				"		unit u = null",
				"		unit u2 = null",
				"		unit u3 = u + u2",
				"");
	}
	
	


	
	

	public void assertOk(boolean executeProg, String ... input) {
		String prog = "package test\n" +
				"	native testFail(string msg)\n" +
				"	native testSuccess()\n" +
				Utils.join(input, "\n") + "\n" +
				"endpackage\n";
		System.out.println(prog);
		testAssertOk(Utils.getMethodName(1), executeProg, prog);
	}
	
	public void assertError( boolean executeProg, String expected, String ... body) {
		String prog = "package test\n" +
				"	native testFail(string msg)\n" +
				"	native testSuccess()\n" +
				Utils.join(body, "\n") + "\n" +
				"endpackage\n";
		testAssertErrors(Utils.getMethodName(1), executeProg, prog, expected);
	}
	


}
