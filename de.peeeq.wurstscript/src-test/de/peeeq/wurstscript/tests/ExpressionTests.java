package de.peeeq.wurstscript.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class ExpressionTests extends PscriptTest {

	@Test
	public void plus() {
		assertOk("plus", "3 + 7 == 10");
	}
	
	@Test
	public void real1() {
		assertOk("plus", ".3 + .7 == 1.");
	}
	
	@Test
	public void minus() {
		assertOk("minus", "3 * 4 == 12");
	}
	
	@Test
	public void div1() {
		assertOk("minus", "14 div 3 == 4");
	}
	
	@Test
	public void div2() {
		assertError("minus", "Cannot compare types", "14 / 4 == 7");
	}
	
	@Test
	public void div3() {
		assertOk("minus", "14 / 3 > 4.0");
	}
	
	@Test
	public void mod1() {
		assertOk("minus", "14 mod 3 == 2");
	}
	
	@Test
	public void err_assign() {
		assertError("minus", "unexpected '='", "x = 12");
	}
	
	private String makeProg(String booleanExpr) {
		String prog = "package test \n" +
				"	native testFail(string msg)\n" +
				"	native testSuccess()\n" +
				"	init \n" +
				"		int x = 3\n" +
				"		int y = 4\n" +
				"		int z = 5\n" +
				"		string a = \"bla\"\n" +
				"		string b = \"blub\"\n" +
				"		if " + booleanExpr + "\n" +
				"			testSuccess()\n" +
				"" +
				"";
		return prog;
	}
	
	public void assertOk(String name, String booleanExpr) {
		String prog = makeProg(booleanExpr);
		testAssertOk(Utils.getMethodName(1), true, prog);
	}

	
	
	public void assertError(String name, String errorMessage, String booleanExpr) {
		String prog = makeProg(booleanExpr);
		testAssertErrors(Utils.getMethodName(1), true, prog, errorMessage);
	}

}
