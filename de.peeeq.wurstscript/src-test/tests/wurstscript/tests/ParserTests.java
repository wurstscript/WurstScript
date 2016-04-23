package tests.wurstscript.tests;

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

	@Test
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
	
	
	
}
