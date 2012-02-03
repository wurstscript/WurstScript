package de.peeeq.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstscript.utils.Utils;

public class ExpressionTests extends PscriptTest {

	@Test
	public void plus() {
		assertOk("3 + 7 == 10");
	}
	
	@Test
	public void real1() {
		assertOk(".3 + .7 == 1.");
	}
	
	@Test
	public void minus() {
		assertOk("3 * 4 == 12");
	}
	
	
	@Test
	public void parantheses() {
		assertOk("(3-1)*(3-1) + (2-5)*(2-5)  == 13");
	}
	
	@Test
	public void div1() {
		assertOk("14 div 3 == 4");
	}
	
	@Test
	public void div2() {
		assertError("Cannot compare types", "14 / 4 == 7");
	}
	
	@Test
	public void div3() {
		assertOk("14 / 3 > 4.0");
	}
	
	@Test
	public void mod1() {
		assertOk("14 mod 3 == 2");
	}
	
	@Test
	public void err_assign() {
		assertError("unexpected '='", "x = 12");
	}
	
	@Test
	public void ints1() {
		assertOk("0153 == 107");
	}
	
	@Test
	public void ints2() {
		assertOk("0xaffe == 45054");
	}
	
//	@Test // not supported (strange notation - do not want ;)
//	public void ints3() {
//		assertOk("$affe == 45054");
//	}
	
	@Test
	public void ints4() {
		assertOk("'a' == 97");
	}
	
//	@Test // not yet supported 
//	public void ints5() {
//		assertOk("'\n' == 11");
//	}
	
	@Test
	public void ints6() {
		assertOk("'hfoo' == 1751543663"); // or 1781543663 ?
	}
	
	@Test
	public void ints7() {
		assertOk("'wc 3' == 2002985011"); // or 2002985611 ? 
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
				"endpackage";
		return prog;
	}
	
	public void assertOk(String booleanExpr) {
		String prog = makeProg(booleanExpr);
		testAssertOk(Utils.getMethodName(1), true, prog);
	}

	
	
	public void assertError(String errorMessage, String booleanExpr) {
		String prog = makeProg(booleanExpr);
		testAssertErrors(Utils.getMethodName(1), true, prog, errorMessage);
	}

}
