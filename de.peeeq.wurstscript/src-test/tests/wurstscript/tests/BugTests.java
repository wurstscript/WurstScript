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
		testAssertErrorsLines(false, "error", 
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
				"package test",
				"	@extern native FirstOfGroup(group g) returns unit",
				"	@extern native GroupRemoveUnit(group g, unit u)",
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
				"package test",
				"	@extern native FirstOfGroup(group g) returns unit",
				"	@extern native GroupRemoveUnit(group g, unit u)",
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
				"@extern native Cos(real x) returns real",
				"@extern native Sin(real x) returns real",
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
	
	@Test
	public void inlineBug() {
		testAssertOkLines(false, 
				"package test",
				"tuple vec2(real x, real y)",
				"tuple vec3(real x, real y, real z)",
				"public function vec2.withZ(real z) returns vec3",
				"	return vec3(this.x, this.y, z)",
				"init",
				"	vec2(3,4).withZ(5)",
				"endpackage");
	}
	
	@Test
	public void cyclicDepReadVars() {
		testAssertOkLines(false, 
				"package test",
				"function self(int n) returns int",
				"	if n > 0",
				"		return self(n-1)",
				"	return n",
				"init",
				"	self(5)",
				"endpackage");
	}
	

	
	@Test
	public void staticGenerics1() {
		testAssertErrorsLines(false, "Type variables must not be used in static contexts", 
				"package test",
				"class C<T>",
				"	static T t",
				"endpackage");
	}
	@Test
	public void staticGenerics2() {
		testAssertErrorsLines(false, "Type variables must not be used in static contexts", 
				"package test",
				"class C<T>",
				"	static function foo(T t)",
				"endpackage");
	}
	
	@Test
	public void staticGenerics3() {
		testAssertOkLines(false,  
				"package test",
				"class C<T>",
				"	static function foo<T>(T t)",
				"endpackage");
	}

	
	@Test
	public void recursive() {
		testAssertOkLines(true,  
				"package test",
				"native testSuccess()",
				"function f(int n) returns int",
				"	if n <= 0",
				"		return 0",
				"	return n + f(n-1)",
				"init",
				"	if 5050 == f(100)",
				"		testSuccess()",
				"endpackage");
	}
	
	@Test
	public void recursive2() {
		testAssertOkLines(false,  
				"package test",
				"native testSuccess()",
				"enum Stat",
				"	A",
				"	B",
				"int array statSubstat",
				"function getStat(Stat s) returns int",
				"	if statSubstat[s castTo int] != -1",
				"		let a = getStat(1 castTo Stat)",
				"	return 0",
				"init",
				"	if getStat(Stat.A) == 0",
				"		testSuccess()",
				"endpackage");
	}
	
	
	@Test
	public void underscore() {
		testAssertOkLines(false,  
				"package test",
				"@extern native I2S(int i) returns string",
				"init",
				"	int _i = 1",
				"	I2S(_i)",
				"endpackage");
	}
	
	@Test
	public void hotdoc() {
		testAssertOkLines(false,  
				"package test",
				"/** test */",
				"function foo()",
				"endpackage");
	}
	

	@Test
	public void memberMethodParens() {
		testAssertOkLines(false,  
				"package test",
				"class A",
				"	function foo()",
				"init",
				"	(new A()).foo()",
				"endpackage");
	}
	
	@Test
	public void localOptimizerFail() { // essence of #237
		testAssertOkLines(true,  
				"package test",
				"native testSuccess()",
				"function foo(int i) returns int",
				"	return 1",
				"function colors_hexs(int i) returns string",
				"	return \"a\"",
				"init",
				"	var aaa = foo(1)",
				"	var bbb = foo(2)",
				"	var fff = aaa",
				"	var ggg = fff div 16",
				"	var kkk = fff - ggg * 16",
				"	var ccc = \"|cff\" + colors_hexs(ggg) + colors_hexs(kkk)",
				"	var eee = bbb",
				"	var hhh = eee div 16",
				"	var iii = eee - hhh * 16",
				"	var ddd = ccc + colors_hexs(hhh) + colors_hexs(iii)",
				"	testSuccess()",
				"endpackage");
	}
	
	@Test
	public void genericsNull() {
		testAssertOkLines(false,  
				"package test",
				"class A<T>",
				"	function foo(T t)",
				"function handleToIndex(handle h) returns int",
				"	return 1",
				"function handleFromIndex(int i) returns handle",
				"	return null",
				"init",
				"	new A<handle>().foo(null)",
				"endpackage");
	}
	
	@Test
	public void funcrefs1() {
		testAssertOkLines(false,  
				"package test",
				"native do(code c)",
				"int x = 20",
				"function bar() returns int",
				"	if x > 0",
				"		do(function bar)",
				"		bar()",
				"		x--",
				"		return 1",
				"	else",
				"		return 2",
				"init",
				"	do(function bar)",
				"endpackage");
	}
	
	@Test
	public void funcrefs2() {
		testAssertOkLines(false,  
				"package test",
				"native do(code c)",
				"int x = 20",
				"function bar() returns int",
				"	if x > 0",
				"		blub()",
				"		do(function bar)",
				"		x--",
				"		return 1",
				"	else",
				"		return 2",
				"function blub() returns int",
				"	if x > 0",
				"		bar()",
				"		do(function bar)",
				"		x--",
				"		return 1",
				"	else",
				"		return 2",
				"init",
				"	do(function bar)",
				"	do(function bar)",
				"endpackage");
	}
	
	@Test
	public void typenameAsFuncname1() {
		testAssertErrorsLines(false, "The name", 
				"package test",
				"function integer()",
				"init",
				"	integer()",
				"endpackage"
				);
	}
	
	@Test
	public void typenameAsFuncname2() {
		testAssertErrorsLines(false, "The name", 
				"type agent			    extends     handle",
				"package test",
				"function agent()",
				"init",
				"	agent()",
				"endpackage"
				);
	}
	
	@Test
	public void underscores_in_name() {
		testAssertOkLines(false,  
				"package test",
				"native print(string msg)",
				"init",
				"	var _test_ = \"Hello\"",
				"	print(_test_)",
				"endpackage"
				);
	}
	
	@Test
	public void extensionFunc_noreturn() { // see #280
		testAssertErrorsLines(false, "missing a body",  
				"package test",
				"function int.foo() returns int",
				"endpackage"
				);
	}

	@Test
	public void func_noreturn() { // see #280
		testAssertErrorsLines(false, "missing a body",  
				"package test",
				"function foo() returns int",
				"endpackage"
				);
	}
	
}
