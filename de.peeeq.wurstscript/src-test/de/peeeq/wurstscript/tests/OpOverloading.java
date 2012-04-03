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
				"		function plus(A a) returns int",
				"			return this.i + a.i",
				"		function bb(int bg)",
				"			return",
				"	init",
				"		A a1 = new A()",
				"		A a2 = new A()",
				"		int result = a1 + a2",
				"		if result == 4",
				"			a1.bb(4)",
				"			testSuccess()",
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

}
