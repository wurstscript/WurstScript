package tests.wurstscript.tests;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WurstModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

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
    public void test_init_order_jass_warning() {
        testAssertErrorsLines(false, "Variable b may not have been initialized",
                "function foo takes nothing returns integer",
                "	local integer a = b",
                "	local integer b = 3",
                "	return a + b",
                "endfunction",
                "package test",
                "	native testSuccess()",
                "	init",
                "		if foo() == 6",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_init_order_globals_warning() {
        testAssertErrorsLines(false, "Global variable b must be declared before it is used.",
                "package test",
                "	integer a = b",
                "	integer b = 3",
                "	native testSuccess()",
                "	init",
                "		if a + b == 6",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void test_init_order_globals_warning_jass() {
        testAssertErrorsLines(false, "Global variable b used before it is declared.",
                "globals",
                "	integer a = b",
                "	integer b = 3",
                "endglobals",
                "package test",
                "	native testSuccess()",
                "	init",
                "		if a + b == 6",
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
        testAssertErrorsLines(false, "cyclic class hierarchy",
                "package test",
                "	class A extends A",
                "endpackage");
    }

    @Test
    public void cyclicDependency2() {
        testAssertErrorsLines(false, "Interface I has a cyclic class hierarchy",
                "package test",
                "	interface I extends I",
                "		function foo()",
                "endpackage");
    }

    @Test
    public void nonAbstractClass() {
        testAssertErrorsLines(false, "Non-abstract class A cannot have abstract functions like blub",
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
                "var x = 1",
                "function sideEffect(int r) returns int",
                "	x = 4",
                "	return r",
                "init",
                "	let y = x + sideEffect(2)",
                "	if y == 3",
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
    public void underscore_end() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "var y_ = 1",
                "function foo_(int x_) returns int",
                "    return x_ + y_",
                "init",
                "    int i_ = 2",
                "    if foo_(i_) == 3",
                "       testSuccess()");

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

    @Test
    public void classesCyclic() { // see #310
        testAssertErrorsLines(false, "cyclic class hierarchy",
                "package test",
                "class A extends B",
                "class B extends A",
                "endpackage"
        );
    }


    @Test
    public void doubleNativeDecl() { // see #353
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "init",
                "	testSuccess()",
                "endpackage",
                "package other",
                "native testSuccess()",
                "init",
                "	testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void optBug() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "@extern native I2S(int x) returns string",
                "init",
                "	int x = 0",
                "	x = x + 1",
                "	x = x",
                "	I2S(x)",
                "	testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void unreadVarWarning() { // #380
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "init",
                "	var done = false",
                "	while not done",
                "		done = true",
                "	testSuccess()"
        );
    }

    @Test
    public void unreadVarWarning2() { // #380
        testAssertErrorsLines(true, "i is never read",
                "package test",
                "@annotation public function annotation()",
                "@annotation public function extern()",
                "@extern native I2S(int x) returns string",
                "native testSuccess()",
                "init",
                "	var i = 5",
                "	I2S(i)",
                "	i = i + 1",
                "	testSuccess()"
        );
    }


    @Test
    public void unreadVarWarningArrays() { // #813
        testAssertOkLines(false,
                "package test",
                "@annotation public function annotation()",
                "@annotation public function extern()",
                "@extern native I2S(int x) returns string",
                "init",
                "    integer array b",
                "    b[0] = 0 // Warning.",
                "    b[1] = 0 // Warning.",
                "    b[2] = 0 // No warning.",
                "    I2S(b[0])",
                "    I2S(b[1])",
                "    for i = 0 to 2",
                "        I2S(b[i])"
                );
    }


    @Test
    public void closureClassConstructor() { // # 440
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "abstract class Hey",
                "	construct()",
                "		testSuccess()",
                "	abstract function foo(int x) returns int",
                "init",
                "	Hey you = (int x) -> x + 1",
                "endpackage"
        );
    }


    @Test
    public void closureClassConstructorInvalid() {
        testAssertErrorsLines(false, "No default constructor for class",
                "package test",
                "abstract class Hey",
                "	construct(int x)",
                "	abstract function foo(int x) returns int",
                "init",
                "	Hey you = (int x) -> x + 1",
                "endpackage"
        );
    }

    @Test
    public void recursiveTuple() {
        testAssertErrorsLines(false, "Parameter bar is recursive",
                "package test",
                "tuple foo(foo bar)",
                "class C",
                "    foo f1 = foo(f2)",
                "    foo f2 = foo(f1)"
        );
    }

    @Test
    public void recursiveTuple2() {
        testAssertErrorsLines(false, "is recursive",
                "package test",
                "tuple foo(bar bar)",
                "tuple bar(foo foo)"
        );
    }

    @Test
    public void testLinePos() { // #462
        String input =
                "package test\n" +
                        "abstract class Hey\n" +
                        "	function foo()";

        WurstModel model = test().executeProg(false).withStdLib(false).withCu(compilationUnit("testLine", input)).run().getModel();

        model.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ClassDef c) {
                super.visit(c);
                Assert.assertEquals(2, c.getSource().getLine());
            }
        });

        model.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(FuncDef funcDef) {
                super.visit(funcDef);
                Assert.assertEquals(3, funcDef.getSource().getLine());
            }
        });
    }


    @Test
    public void functionWithUnderscore() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function _coke()",
                "		testSuccess()",
                "	init",
                "		_coke()",
                "endpackage");
    }

    @Test
    public void testInferInSuper() {
        testAssertOkLines(false,
                "package test",
                "class A",
                "    construct(code c)",
                "class B extends A",
                "    construct()",
                "        super(null)");
    }


    @Test
    public void ovveride_nativeTypeReturn() {
        testAssertOkLinesWithStdLib(false,
                "package test",
                "abstract class A",
                "    abstract function foo() returns rect",
                "class B extends A",
                "    override function foo() returns rect",
                "        return null");
    }

    @Test
    public void parseHexValues() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "init",
                "    int i1 = 12345678",
                "    int i2 = 0xBC614E",
                "    int i3 = $BC614E",
                "    if i1 == i2 and i2 == i3",
                "       testSuccess()");

    }


    @Test
    public void extensionMethodStatic() { // See #614
        testAssertErrorsLines(true, "Reference to A can only be used for calling static methods, but not for calling extension method method 'bar'.",
                "package Test",
                "native testSuccess()",
                "abstract class A",
                "    abstract function foo()",
                "public function A.bar(A listener) returns A",
                "    return listener",
                "init",
                "    let a = A.bar(() -> testSuccess())",
                "    a.foo()",
                ""
        );

    }

    @Test
    public void testCyclicDependencyError() {
        testAssertErrorsLines(true, "For loop target int doesn't provide a iterator() function",
                "package Test",
                "native testSuccess()",
                "function foo() returns bool",
                "    var x = 0",
                "    var sum = 0",
                "    for x in x",
                "        sum += x",
                "    return true",
                "init",
                "    if foo()",
                "        testSuccess()"
        );
    }


    @Test
    public void testStacktrace() {
        testAssertOkLines(true,
                "package MagicFunctions",
                "public function getStackTraceString() returns string",
                "    return \"\"",
                "endpackage",
                "package Test",
                "import MagicFunctions",
                "native testSuccess()",
                "native println(string s)",
                "function foo(int x, int y) returns bool",
                "    println(getStackTraceString())",
                "    return x < y",
                "function bar(int x) returns int",
                "    if x == 2",
                "        println(getStackTraceString())",
                "    return x",
                "init",
                "    if foo(bar(1), bar(2))",
                "        testSuccess()"
        );
    }

    @Test
    public void testClassImplementsClass() {
        testAssertErrorsLines(false, "not an interface",
                "package Test",
                "abstract class A",
                "    construct(int i)",
                "class B implements A"
        );
    }

    @Test
    public void testClassExtendsClassWithoutNoArgConstructor() {
        testAssertErrorsLines(false, "The extended class <A> does not expose a no-arg constructor",
                "package Test",
                "class A",
                "    construct(int i)",
                "class B extends A"
        );
    }

    @Test
    public void ticket706() {
        testAssertErrorsLines(false, "Classes may only extend other classes",
                "package Test",
                "interface I",
                "class A extends I",
                "class B extends A",
                "init",
                "    new B()"
        );
    }

    @Test
    public void ticket709() {
        testAssertOkLinesWithStdLib(false,
                "package Test",
                "init",
                "    var s = \".\"",
                "    for i = 1 to 10",
                "        s += s",
                "    print(s)"
        );
    }


    @Test
    public void stringPlusNull1() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function nullString() returns string",
                "    return null",
                "init",
                "    var s = \"a\" + nullString()",
                "    if s == \"a\"",
                "        testSuccess()"
        );
    }

    @Test
    public void stringPlusNull2() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function nullString() returns string",
                "    return null",
                "init",
                "    var s = nullString() + nullString()",
                "    if s == null",
                "        testSuccess()"
        );
    }

    @Test
    public void stringPlusNull3() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function nullString() returns string",
                "    return null",
                "init",
                "    var s = nullString() + \"a\"",
                "    if s == \"a\"",
                "        testSuccess()"
        );
    }

    @Test
    public void cyclicForLoop() { // #717
        testAssertErrorsLines(true, "Could not find variable a",
                "package Test",
                "native testSuccess()",
                "function nullString() returns string",
                "    return null",
                "init",
                "    for a in a"
        );
    }

    @Test
    public void implementNothing() { // #719
        testAssertErrorsLines(false, "mismatched input '\\n' expecting {'thistype', ID}",
                "package Test",
                "class A implements",
                "init",
                "    new A()"
        );
    }

    @Test(expectedExceptions = {InterpreterException.class})
    public void subStringError() { // #728
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "@extern native SubString(string s, int s, int e) returns string",
                "init",
                "    if SubString(\"blubber\", 1000, 1002) == null",
                "        testSuccess()"
        );
    }

    @Test
    public void underlineArray() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "init",
                "    int array _ar",
                "    _ar[1] = 2",
                "    if _ar[1] == 2",
                "        testSuccess()",
                "    testSuccess()"
        );
    }

    @Test
    public void test_null_in_jass() {
        testAssertOkLines(false,
                "function blub takes integer a returns integer",
                "	if a == null then",
                "		return 1",
                "	else",
                "		return 2",
                "	endif",
                "endfunction",
                "package test",
                "	native testSuccess()",
                "	init",
                "		if blub(0) == 1 and blub(42) == 2",
                "			testSuccess()",
                "endpackage");
    }

    @Test
    public void testNestedTimerClosure() { // see #765
        testAssertOkLines(false,
                "package Hello",
                "native blub(code c)",
                "public function doPeriodically(real time, CallbackPeriodic cb) returns CallbackPeriodic",
                "    cb.start(time)",
                "    return cb",
                "",
                "public tuple dialog_update_fn_res(string name, bool display)",
                "",
                "public abstract class PeriodicDialogUpdateFn",
                "    protected abstract function call(CallbackPeriodic cb) returns dialog_update_fn_res",
                "",
                "public abstract class CallbackPeriodic",
                "    private PeriodicDialogUpdateFn update_fn = null",
                "",
                "    protected abstract function call(thistype cb)",
                "",
                "    function start(real time)",
                "        blub(function staticCallback)",
                "",
                "    private static function staticCallback()",
                "        CallbackPeriodic cb = null",
                "        if cb.update_fn != null",
                "            cb.update_fn.call(cb)",
                "",
                "class X",
                "",
                "class LinkedList<X>",
                "    function forEach(LLItrClosure<X> f)",
                "",
                "public interface LLItrClosure<T>",
                "    function run(T t)",
                "",
                "init",
                "    doPeriodically(1.0) cb ->",
                "        let clean_queue = new LinkedList<X>()",
                "        clean_queue.forEach() itr ->",
                ""
        );
    }


    @Test
    public void negativeNumberLiterals() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "native testFail(string msg)",
                "init",
                "    if 0117 != 79",
                "        testFail(\"a\")",
                "    if -0117 != -79",
                "        testFail(\"b\")",
                "    if 0x4f != 79",
                "        testFail(\"c\")",
                "    if -0x4f != -79",
                "        testFail(\"d\")",
                "    if $4f != 79",
                "        testFail(\"e\")",
                "    if -$4f != -79",
                "        testFail(\"f\")",
                "    testSuccess()"
        );
    }

	@Test
	public void testSelfAssignmentWarning() {
		testAssertErrorsLines(false, "The assignment to local variable i probably has no effect",
			"package test",
			"@annotation public function annotation()",
			"@annotation public function extern()",
			"@extern native I2S(int x) returns string",
			"native testSuccess()",
			"init",
			"	var i = 5",
			"	I2S(i)",
			"	i = i",
			"	I2S(i)",
			"	testSuccess()"
		);
	}

	@Test
	public void testSelfAssignmentWarningDot() {
		testAssertErrorsLines(false, "The assignment to variable i probably has no effect",
			"package test",
			"@annotation public function annotation()",
			"@annotation public function extern()",
			"@extern native I2S(int x) returns string",
			"native testSuccess()",
			"class A",
			"	var i = 5",
			"	construct()",
			"		this.i = i",
			"init",
			"	new A()",
			"	testSuccess()"
		);
	}

	@Test
	public void testSelfAssignmentNoWarning() {
		testAssertOkLines(true,
			"package test",
			"@annotation public function annotation()",
			"@annotation public function extern()",
			"@extern native I2S(int x) returns string",
			"native testSuccess()",
			"class A",
			"	var i = 5",
			"	construct(int i)",
			"		this.i = i",
			"init",
			"	new A(1)",
			"	testSuccess()"
		);
	}

    @Test
    public void bitset_add() {
        testAssertOkLines(true,
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "@extern native I2S(int i) returns string",
            "public tuple bitset(int val)",
            "public function int.pow(int x) returns int",
            "    int result = 1",
            "    for int i=1 to x",
            "        result *= this",
            "    return result",
            "public function bitset.add(int v) returns bitset",
            "    let pow = 2 .pow(v)",
            "    return not this.containsPow(pow) ? bitset(this.val + pow) : this",
            "function bitset.containsPow(int pow) returns boolean",
            "    return (this.val mod (pow * 2)) >= pow",
            "init",
            "    let a = bitset(5)", // {0,2}
            "    let res = a.add(1)",
            "    if res.val == 7",
            "        testSuccess()",
            "    else",
            "        testFail(I2S(res.val))"
        );
    }

    @Test
    public void middlewareOverload() throws IOException {
        testAssertOkFile(new File(TEST_DIR + "MiddlewareOverload.wurst"), true);
    }

    @Test
    public void cycle_with_generics() {
        testAssertOkLines(true,
            "package Test",
            "native testSuccess()",
            "public abstract class VoidFunction<T>",
            "    abstract function call(T t)",
            "int x = 0",
            "function foo(int i)",
            "    x++",
            "    VoidFunction<int> f = j -> bar(j - 1)",
            "    f.call(i)",
            "function bar(int i)",
            "    x++",
            "    VoidFunction<int> f = j -> foo(j - 1)",
            "    if i > 0",
            "        f.call(i)",
            "init",
            "    bar(10)",
            "    if x == 11",
            "        testSuccess()"
        );
    }

    @Test
    public void executeFuncWithStackTrace() {
        testAssertOkLines(true,
            "package Test",
            "native testSuccess()",
            "@extern native ExecuteFunc(string f)",
            "function getStackTraceString() returns string",
            "    return \"foo\"",
            "class A",
            "    function bar()",
            "        testSuccess()",
            "A a = new A",
            "function foo()",
            "    a.bar()", // calling a function to ensure stacktraces are needed
            "init",
            "    ExecuteFunc(\"foo\")"
        );
    }

    @Test
    public void agentTypeComparisonsWurst() {
        testAssertErrorsLinesWithStdLib(true, "Cannot compare types sound with rect",
            "package Test",
            "function compare(sound s, rect r) returns boolean",
            "    return s == r",
            "init",
            "    compare(null, null)"
        );
    }

}
