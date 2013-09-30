package tests.wurstscript.tests;

import org.junit.Test;

public class ClosureTests extends WurstScriptTest {
	
	
	@Test
	public void closure1() {
		testAssertOkLines(true, 
				"package test",
				"native testSuccess()",
				"interface SimpleFunc",
				"	function apply(int x, int y) returns int",
				"init",
				"	SimpleFunc f = (int x, int y) => x + y",
				"	if f.apply(3,4) == 7",
				"		testSuccess()"
			);
	}
	
	@Test
	public void closure2() {
		testAssertOkLines(true, 
				"package test",
				"native testSuccess()",
				"interface SimpleFunc",
				"	function apply(int x, int y) returns int",
				"class Test implements SimpleFunc",
				"	override function apply(int x, int y) returns int",
				"		return x*y",
				"init",
				"	SimpleFunc f = (int x, int y) => x + y",
				"	SimpleFunc g = new Test()",
				"	if f.apply(3,4) == 7 and g.apply(3,4) == 12",
				"		testSuccess()"
			);
	}
	
	

	@Test
	public void closure3() {
		testAssertOkLines(true, 
				"package test",
				"native testSuccess()",
				"interface SimpleFunc",
				"	function apply(int x) returns int",
				"init",
				"	int y = 4",
				"	SimpleFunc f = (int x) => x + y",
				"	if f.apply(3) == 7",
				"		testSuccess()"
			);
	}
	
}
