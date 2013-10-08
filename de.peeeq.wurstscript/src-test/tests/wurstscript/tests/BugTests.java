package tests.wurstscript.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class BugTests extends WurstScriptTest {

	private static final String TEST_DIR = "./testscripts/concept/";
	
	@Test
	public void localsInOndestroy() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "OndestroyL.wurst"), true);
	}
	
	@Test
	public void cyclic() throws IOException {
		testAssertOkFile(new File(TEST_DIR + "CyclicError.wurst"), false);
	}
	
	@Test
	public void forfrom() throws IOException {
		testAssertOkFileWithStdLib(new File(TEST_DIR + "ForFrom.wurst"), false);
	}
	
	@Test
	public void ObjectRecycler() throws IOException {
		testAssertOkFileWithStdLib(new File(TEST_DIR + "ObjectRecycler.wurst"), false);
	}
	
	@Test
	public void intBoundaries() {
		testAssertOkLines(false,
				"package test",
				"	int i1 = -2147483648",
				"	int i2 = 2147483647",
				"	int i3 = 0 + (-2147483648)",
				"endpackage");
	}
	
	@Test
	public void intBoundariesL() {
		testAssertErrorsLines(false, "Invalid number",
				"package test",
				"	int i1 = -2147483649",
				"endpackage");
	}
	
	@Test
	public void intBoundariesH() {
		testAssertErrorsLines(false, "Invalid number",
				"package test",
				"	int i1 = 2147483648",
				"endpackage");
	}
	
	@Test
	public void bug62_codearray() {
		testAssertErrorsLines(false, "Code arrays", 
				"package test",
				"	code array coar",
				"endpackage");
	}
	
	@Test
	public void bug61_break() {
		testAssertErrorsLines(false, "inside a loop", 
				"package test",
				"	init",
				"		break",
				"endpackage");
	}
	
	@Test
	public void test_empty_escapesequence() {
		testAssertErrorsLines(false, "Lexical error", 
				"package test",
				"	function foo() returns string",
				"		return \"\\ \" ",
				"endpackage");
	}
	

	@Test
	public void test_unit_array() {
		testAssertOkLines(false,
				"type unit extends handle",
				"package test",
				"	native testSuccess()",
				"	init",
				"		unit array blub",
				"		blub[4] = null",
				"		if blub[4] == null",
				"			testSuccess()",
				"endpackage");
	}
	
	@Test
	public void test_inline_jass_div() {
		testAssertOkLines(false,
				"function divide takes integer a, integer b returns integer",
				"	return a / b",
				"endfunction",
				"package test",
				"	native testSuccess()",
				"	init",
				"		if divide(17,3) == 5",
				"			testSuccess()",
				"endpackage");
	}
	
	
	@Test
	public void test_for_from() {
		testAssertOkLines(false,
				"type unit extends handle",
				"type group extends handle",
				"native FirstOfGroup takes group g returns unit",
				"native GroupRemoveUnit takes group g, unit u returns nothing",
				"package test",
				"	function group.hasNext() returns boolean",
				"		return FirstOfGroup(this) != null",
				"	function group.next() returns unit",
				"		let u = FirstOfGroup(this)",
				"		GroupRemoveUnit(this, u)",
				"		return u",
				"	function group.close()",
				"		skip",
				"	init",
				"		group g = null",
				"		for unit u from g",
				"			skip",
				"endpackage");
	}
	
	
	@Test
	public void test_for_in() {
		testAssertOkLines(false,
				"type unit extends handle",
				"type group extends handle",
				"native FirstOfGroup takes group g returns unit",
				"native GroupRemoveUnit takes group g, unit u returns nothing",
				"package test",
				"	function group.iterator() returns group",
                "		// a correct implementation would return a copy",
                "		return this",
				"	function group.hasNext() returns boolean",
				"		return FirstOfGroup(this) != null",
				"	function group.next() returns unit",
				"		let u = FirstOfGroup(this)",
				"		GroupRemoveUnit(this, u)",
				"		return u",
				"	function group.close()",
				"		skip",
				"	init",
				"		group g = null",
				"		for unit u in g",
				"			skip",
				"endpackage");
	}
	
	
	@Test
	public void test_correct_escapesequence() {
		testAssertOkLines(false,
				"package test",
				"	function foo() returns string",
				"		return \"\\\\ \" ",
				"endpackage");
	}
	

	@Test
	public void varname_with_interface() {
		testAssertOkLines(false,
				"package test",
				"	interface I",
				"		function foo()",
				"	class C implements I",
				"		function foo()",
				"	init",
				"		I a = new C()",
				"		int a2 = 1337",
				"endpackage");
	}
	
	@Test
	public void testCodeNull() {
		testAssertOkLines(false, 
				"package test",
				"	function foo(code c)",
				"		skip",
				"	init",
				"		foo(null)",
				"endpackage");
	}
	
	@Test
	public void cyclicDependency() {
		testAssertErrorsLines(false, "must not extend themselves", 
				"package test",
				"	class A extends A",
				"endpackage");
	}
	
	@Test
	public void cyclicDependency2() {
		testAssertErrorsLines(false, "must not extend themselves", 
				"package test",
				"	interface I extends I",
				"		function foo()",
				"endpackage");
	}
	
	@Test
	public void nonAbstractClass() {
		testAssertErrorsLines(false, "class A is not abstract", 
				"package test",
				"	class A",
				"		abstract function blub()",
				"endpackage");
	}
	
	@Test
	public void staticOverride() {
		testAssertErrorsLines(false, "Cannot override", 
				"package test",
				"	abstract class A",
				"		abstract function blub()",
				"	class B extends A",
				"		override static function blub()",
				"endpackage");
	}
	
	@Test
	public void realIndex() {
		testAssertOkLines(false,  
				"package test",
				"tuple color(int r, int g, int b)",
				"enum COLOR",
				"	YELLOW",
				"	ORANGE",
				"color array colors",
				"class Test",
				"	COLOR col",
				"	function colorize()",
				"		color c = color(0,0,0)",
				"		if col == COLOR.YELLOW",
				"			c = colors[col castTo int +1]",
				"		else if col == COLOR.ORANGE",
				"			c = colors[col castTo int -1]",
				"		else",
				"			c = colors[col castTo int]",
				"init",
				"	let t = new Test()",
				"	t.colorize()",
				"endpackage");
	}
	
	@Test
	public void constFolding() { // see #124
		testAssertOkLines(false,  
				"package test",
				"init",
				"	let a = 0.00023 * 0.06",
				"	let b = 20.5 * 300.1",
				"endpackage");
	}
	
	@Test
	public void inlinerBugShortCircuit() { // see #123
		testAssertOkLines(true,  
				"package test",
				"native testSuccess()",
				"var z = 0",
				"function sideEffect() returns boolean",
				"	z++",
				"	return true",
				"init",
				"	let b = false and sideEffect()",
				"	if z == 0",
				"		testSuccess()",
				"endpackage");
	}

	@Test
	public void inlinerBugShortCircuit2() { // see #123
		testAssertOkLines(true,  
				"package test",
				"native testSuccess()",
				"var z = 0",
				"function sideEffect() returns boolean",
				"	z++",
				"	return true",
				"init",
				"	if z == 1 and sideEffect()",
				"		skip",
				"	else",
				"		testSuccess()",
				"endpackage");
	}
	
	
	@Test
	public void flattenBug() {
		testAssertOkLines(true,  
				"package test",
				"native testSuccess()",
				"var x = 0",
				"function sideEffect(int r) returns int",
				"	x = 4",
				"	return r",
				"init",
				"	let y = x + sideEffect(2)",
				"	if y == 2",
				"		testSuccess()",
				"endpackage");
	}
	
	@Test
	public void forLoop() { // see #122
		testAssertErrorsLines(false, "must be int", 
				"package test",
				"init",
				"	for s = \"\" to \"aaaa\" step \"a\"",
				"endpackage");
	}
	

	@Test
	public void division() {
		testAssertErrorsLines(false, "div",
				"package test",
				"int x = 5 div 3.",
				"endpackage"
				);
	}
	
	@Test
	public void classNull() {
		testAssertOkLines(true,
				"package test",
				"native testSuccess()",
				"class A",
				"A a = null",
				"init",
				"	if not (a != null)",
				"		testSuccess()",
				"endpackage"
				);
	}

	@Test
	public void dynamicVarFromStaticContext() { // see #139
		testAssertErrorsLines(false, "from static context",
				"package test",
				"class A",
				"	int i",
				"	static function foo() returns int",
				"		return A.i",
				"endpackage");
	}
	
	@Test
	public void dynamicVarFromStaticContext2() {
		testAssertErrorsLines(false, "from context",
				"package test",
				"class A",
				"	int i",
				"class B",
				"	function foo() returns int",
				"		return A.i",
				"endpackage");
	}
	
	@Test
	public void duplicateNamesOk() {
		testAssertOkLines(false, 
				"package test",
				"class A",
				"	int i",
				"class B",
				"	int i",
				"endpackage");
	}
	
	@Test
	public void duplicateNames() {
		testAssertErrorsLines(false, "An element with name i already exists",
				"package test",
				"class A",
				"	int i",
				"	int i",
				"endpackage");
	}
	
	@Test
	public void duplicateNames2() {
		testAssertOkLines(false, 
				"package test",
				"class A",
				"	int i",
				"	function i()",
				"endpackage");
	}
	
	
	@Test
	public void polarOfffsetInline() { // #149
		testAssertOkLines(true, 
				"package test",
				"native testSuccess()",
				"native Cos(real x) returns real",
				"native Sin(real x) returns real",
				"public tuple angle(real radians)",
				"public function angle.toVec(real len) returns vec2",
				"	return vec2(Cos(this.radians)*len, Sin(this.radians)*len)",
				"public tuple vec2( real x, real y )",
				"public function vec2.op_plus( vec2 v )	returns vec2",
				"	return vec2(this.x + v.x, this.y + v.y)",
				"public function vec2.polarOffset(angle ang, real dist) returns vec2",
				"	return this + ang.toVec(dist)",
				"init",
				"	vec2 v = vec2(1,2)",
				"	v = v.polarOffset(angle(1.5708), 10)",
				"	if v.x >= 0.99 and v.x <= 1.01 and v.y >= 11.99 and v.y <= 12.01",
				"		testSuccess()",
				"endpackage"
				);
	}
}
