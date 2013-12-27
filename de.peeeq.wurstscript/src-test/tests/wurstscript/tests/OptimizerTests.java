package tests.wurstscript.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

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
				"package test",
				"	@extern native Player(integer id) returns player",
				"	@extern native GetPlayerId(player whichPlayer) returns integer",
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
				"package test",
				"	@extern native Player(integer id) returns player",
				"	@extern native GetPlayerId(player whichPlayer) returns integer",
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
				"package test",
				"	@extern native Player(integer id) returns player",
				"	@extern native GetPlayerId(player whichPlayer) returns integer",
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
				"package test",
				"	@extern native Player(integer id) returns player",
				"	@extern native GetPlayerId(player whichPlayer) returns integer",
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
	
	@Test
	public void test_ifInt1() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	init",
				"		if 3 > 4",
				"			testFail(\"\")",
				"		else",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_ifInt2() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	init",
				"		if 3 < 4 - 2",
				"			testFail(\"\")",
				"		else",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_ifInt3() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	init",
				"		if 8 >= 8 and 50 != 40",
				"			testSuccess()",
				"		else",
				"			testFail(\"\")",
				"endpackage");
	}
	
	@Test
	public void test_exitwhen() {
		assertOk(false,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	init",
				"		while true",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_ConstFolding() {
		assertOk(false,
				"package test",
				"	init",
				"		int i = 3 + 7 * 2 * 33",
				"endpackage");
	}
	
	@Test
	public void test_ConstFoldingCombined() {
		assertOk(true,
				"package test",
				"	native testSuccess()",
				"	native testFail(string s)",
				"	init",
				"		int i = 3 + 7 * 2 * 33",
				"		if i == 465",
				"			testSuccess()",
				"endpackage");
	}

	@Test
	public void test_tempVarRemover() throws IOException {
		assertOk(false,
				"package test",
				"	@extern native I2S(int i) returns string",
				"	native println(string s)",
				"	@extern native GetRandomInt(int a, int b) returns int",
				"	init",
				"		let blub_a = GetRandomInt(0,100)",
				"		let blub_b = blub_a",
				"		let blub_c = blub_b + blub_b",
				"		println(I2S(blub_c))",
				"endpackage");
		String output = Files.toString(new File("./test-output/OptimizerTests_test_tempVarRemover_inlopt.j"), Charsets.UTF_8);
		assertFalse(output.contains("blub_a"));
		assertFalse(output.contains("blub_c"));
	}
	
	
	@Test
	public void test_tempVarRemover2() throws IOException {
		assertOk(false,
				"package test",
				"	@extern native I2S(int i) returns string",
				"	native println(string s)",
				"	@extern native GetRandomInt(int a, int b) returns int",
				"	init",
				"		let blablub = GetRandomInt(0,100)",
				"		println(I2S(blablub))",
				"endpackage");
		String output = Files.toString(new File("./test-output/OptimizerTests_test_tempVarRemover2_inlopt.j"), Charsets.UTF_8);
		assertFalse(output.contains("blablub"));
	}
	
	/*	let blablub = AddSpecialEffect("Abilities\\Spells\\Undead\\DeathCoil\\DeathCoilSpecialArt.mdl", 1,2)
	DestroyEffect(blablub)
		*/
	
}
