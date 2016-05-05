package tests.wurstscript.tests;

import org.junit.Ignore;
import org.junit.Test;

public class ParserTests extends WurstScriptTest {
	
	
	@Test
	public void parenthesis1() {
		testAssertErrorsLines(false, "no", 
				"package test",
				"	init",
				"		print(\"hello\" ",
				"endpackage"
			);
	}
	
	@Test
	public void parenthesis2() {
		testAssertErrorsLines(false, "no", 
				"package test",
				"	init",
				"		print(\"hello\")) ",
				"endpackage"
			);
	}
	
	@Test
	public void err_in_closure() {
		testAssertErrorsLines(false, "no", 
				"package test",
				"	init",
				"		doAfter(0.1, ()->begin",
				"			print(\"hello\" + if)",
//				"			\"world\")",
				"		end)",
				"endpackage"
			);
	}

	@Test @Ignore
	public void halfAssign() {
		testAssertErrorsLines(false, "dasfdss",
				"package Test",
				"init",
				"	int x =",
				"	int y =",
				"	x = 1",
				"	y = 2",
				"	foo(x, y)",
				"function foo(int x, int y)");
	}


	@Test
	public void dotTo() {
		testAssertErrorsLines(false, "extraneous input 'to'",
				"package Test",
				"init",
				"	let x = 1",
				"	string s = 1.to");
	}

	@Test
	public void indentWithSpaces() {
		testAssertOkLines(false,
				"package Test",
				"init",
				"    var x = 1",
				"    if x > 10",
				"		x -= 1",
				"	 x += 1");
	}

	@Test
	public void indentWithSpaces2() {
		testAssertOkLines(false,
				"package Test",
				"init",
				"    var x = 1",
				"    int y = 2",
				"    if x > 10",
				"        x -= 1",
				"        y += 1",
				"	 x += 1");
	}

}
