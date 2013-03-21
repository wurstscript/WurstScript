package tests.wurstscript.tests;

import org.junit.Test;

import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.utils.Utils;

public class OptimizerTests extends WurstScriptTest {

	
	@Test
	public void test_number_shortening() {
		assertOk(false,
				"package test",
				"	function foo() returns int",
				"		return 800000",
				"endpackage");
	}
	
	@Test
	public void test_number_shortening2() {
		assertOk(false,
				"package test",
				"	function foo() returns real",
				"		if 1.0 > 0.1",
				"			return 0.0",
				"		else",
				"			return 1.10",
				"endpackage");
	}
	

	@Test
	public void test_double_renaming_bug() {
		assertOk(false,
				"package test",
				"	int testVar = 0",
				"	function w() returns int",
				"		return 1",
				"	function s(int j) returns int",
				"		return testVar",
				"	init",
				"		w()",
				"		s(2)",
				"		let c = function w",
				"endpackage");
	}
	
	@Test
	public void test_remove_useless() {
		assertOk(false,
				"package test",
				"	int testVar1 = 1",
				"	real testVar2 = 1.1",
				"	string testVar3 = \"blub\"",
				"	boolean testVar4 = true",
				"	init",
				"		int i = testVar1",
				"endpackage");
	}
	
	@Test
	public void test_inline_globals() {
		assertOk(false,
				"package test",
				"	int testVar1 = 1",
				"	real testVar2 = 1.1",
				"	string testVar3 = \"blub\"",
				"	boolean testVar4 = true",
				"	init",
				"		int i = testVar1",
				"		real r = testVar2",
				"		string s = testVar3",
				"		boolean b = testVar4",
				"endpackage");
	}

	
	@Test
	public void test_nullsetter1() {
		assertOk(true,
				"type player extends handle",
				"native Player takes integer id returns player",
				"native GetPlayerId takes player whichPlayer returns integer",
				"package test",
				"	native testSuccess()",
				"	function foo()",
				"		player p = Player(0)",
				"	init",
				"		foo()",
				"		testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_nullsetter2() {
		assertOk(true,
				"type player extends handle",
				"native Player takes integer id returns player",
				"native GetPlayerId takes player whichPlayer returns integer",
				"package test",
				"	native testSuccess()",
				"	function foo() returns player",
				"		player p = Player(0)",
				"		return p",
				"	init",
				"		foo()",
				"		testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_nullsetter3() {
		assertOk(true,
				"type player extends handle",
				"native Player takes integer id returns player",
				"native GetPlayerId takes player whichPlayer returns integer",
				"package test",
				"	native testSuccess()",
				"	function foo() returns int",
				"		player p = Player(0)",
				"		return GetPlayerId(p)",
				"	init",
				"		foo()",
				"		testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_nullsetter4() {
		assertOk(true,
				"type player extends handle",
				"native Player takes integer id returns player",
				"native GetPlayerId takes player whichPlayer returns integer",
				"package test",
				"	native testSuccess()",
				"	function foo() returns int",
				"		player p = Player(0)",
				"		return 0",
				"	init",
				"		foo()",
				"		testSuccess()",
				"endpackage");
	}
	
//	(04:49:22 PM) Frotty: öh
//	(04:49:24 PM) Frotty: einfach
//	(04:49:28 PM) Frotty: 1 var erstellen
//	(04:49:31 PM) Frotty: constant int = 5
//	(04:49:34 PM) Frotty: nicht benutzen
//	(04:49:36 PM) Frotty: wird nicht entfernt
	@Test
	public void test_varRemoval() {
		assertOk(false,
				"package test",
				"	constant i = 5",
				"endpackage");
	}

	
	private String makeCode(String... body) {
		return Utils.join(body, "\n");
	}
	
	public void assertOk( boolean executeProg, String ... body) {
		String prog = makeCode(body);
		testAssertOk(UtilsIO.getMethodName(1), executeProg, prog);
	}
	
	public void assertError( boolean executeProg, String expected, String ... body) {
		String prog = makeCode(body);
		testAssertErrors(UtilsIO.getMethodName(1), executeProg, prog, expected);
	}
	
	@Test
	public void test_ifTrue() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	constant b = true",
				"	init",
				"		if b",
				"			testSuccess()",
				"		else",
				"			testFail(\"\")",
				"endpackage");
	}
	
	@Test
	public void test_ifFalse() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	constant b = false",
				"	init",
				"		if b",
				"			testFail(\"\")",
				"		else",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_ifDoubleOr1() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	constant b = false",
				"	init",
				"		if b or true",
				"			testSuccess()",
				"		else",
				"			testFail(\"\")",
				"endpackage");
	}
	
	@Test
	public void test_ifDoubleOr2() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	constant b = false",
				"	init",
				"		if b or false",
				"			testFail(\"\")",
				"		else",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_ifDoubleAnd1() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	constant b = true",
				"	init",
				"		if b and true",
				"			testSuccess()",
				"		else",
				"			testFail(\"\")",
				"endpackage");
	}
	
	@Test
	public void test_ifDoubleAnd2() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	constant b = true",
				"	init",
				"		if b and false",
				"			testFail(\"\")",
				"		else",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_ifMulti() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	constant b = true",
				"	constant c = true",
				"	init",
				"		if b and true and c and true and false",
				"			testFail(\"\")",
				"		else",
				"			testSuccess()",
				"endpackage");
	}

}
