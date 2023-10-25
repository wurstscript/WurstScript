package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class ClosureTests extends WurstScriptTest {


    @Test
    public void closure1() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply(int x, int y) returns int",
                "init",
                "	SimpleFunc f = (int x, int y) -> x + y",
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
                "	SimpleFunc f = (int x, int y) -> x + y",
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
                "	SimpleFunc f = (int x) -> x + y",
                "	if f.apply(3) == 7",
                "		testSuccess()"
        );
    }

    @Test
    public void closure_inferType() {
        testAssertErrorsLines(true, "interface or class type",
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply(int x) returns int",
                "init",
                "	int y = 4",
                "	let f = (int x) -> x + y",
                "	if f.apply(3) == 7",
                "		testSuccess()"
        );
    }

    @Test
    public void closure_begin_end1() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply(int x) returns int",
                "init",
                "	int y = 4",
                "	SimpleFunc f = (int x) -> begin",
                "		let a = y",
                "		let b = a+x",
                "		return b",
                "	end",
                "	if f.apply(3) == 7",
                "		testSuccess()"
        );
    }

    @Test
    public void closure_begin_end2() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply(int x) returns int",
                "init",
                "	int y = 4",
                "	SimpleFunc f = (int x) ->",
                "	begin",
                "		let a = y",
                "		let b = a+x",
                "		return b",
                "	end",
                "	if f.apply(3) == 7",
                "		testSuccess()"
        );
    }

    @Test
    public void closure_begin_end3() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply(int x) returns int",
                "init",
                "	int y = 4",
                "	SimpleFunc f = (int x) ->",
                "		let a = y",
                "		let b = a+x",
                "		return b",
                "	if f.apply(3) == 7",
                "		testSuccess()"
        );
    }

    @Test
    public void captureParam() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply(int x) returns int",
                "function test(int y)",
                "	SimpleFunc f = (int x) -> x + y",
                "	if f.apply(3) == 7",
                "		testSuccess()",
                "init",
                "	test(4)"
        );
    }

    @Test
    public void captureThis() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply() returns int",
                "class C",
                "	int y",
                "	function foo()",
                "		bar(() -> x() + y)",
                "	function bar(SimpleFunc f)",
                "		if f.apply() == 7",
                "			testSuccess()",
                "	function x() returns int",
                "		return 3",
                "init",
                "	new C()",
                "	let c = new C()",
                "	c.y = 4",
                "	c.foo()"
        );
    }

    @Test
    public void captureThis2() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply() returns int",
                "class C",
                "	int y",
                "	function foo()",
                "		bar(() -> this.x() + this.y)",
                "	function bar(SimpleFunc f)",
                "		if f.apply() == 7",
                "			testSuccess()",
                "	function x() returns int",
                "		return 3",
                "init",
                "	new C()",
                "	let c = new C()",
                "	c.y = 4",
                "	c.foo()"
        );
    }


    @Test
    public void beginEndExpr() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "init",
                "	int a = 1",
                "	let b = (((begin",
                "		int c = ((begin",
                "			let d = a",
                "			return d+2",
                "		end)+3)",
                "		return c + 4",
                "	end)))",
                "	if b == 10",
                "		testSuccess()"
        );
    }

    @Test
    public void closureWithGenerics() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface Blub<T,S>",
                "	function foo(T t, S s) returns S",
                "function callMe<T,S>(T t, S s, Blub<T,S> b) returns S",
                "	S s2 = b.foo(t, s)",
                "	return b.foo(t, s2)",
                "class A",
                "	int x",
                "	construct(int x)",
                "		this.x = x",
                "	function foo(B b) returns B",
                "		return new B(x + b.x)",
                "class B",
                "	int x",
                "	construct(int x)",
                "		this.x = x",
                "init",
                "	B i = callMe<A,B>(new A(3), new B(4), (A a, B b) -> a.foo(b))",
                "	if i.x == 10",
                "		testSuccess()"
        );
    }

    @Test
    public void noAbstractMethod() {
        testAssertErrorsLines(false, "Cannot assign () -> Void to A",
                "package test",
                "native testSuccess()",
                "abstract class A",
                "	function foo()",
                "init",
                "	A a = () -> begin",
                "		skip",
                "	end"
        );
    }

    @Test
    public void oneAbstractMethod() {
        testAssertOkLines(false,
                "package test",
                "native testSuccess()",
                "abstract class A",
                "	abstract function foo()",
                "init",
                "	A a = () -> begin",
                "		skip",
                "	end"
        );
    }

    @Test
    public void oneAbstractMethodInherited() {
        testAssertOkLines(false,
                "package test",
                "native testSuccess()",
                "abstract class A",
                "	abstract function foo()",
                "abstract class B extends A",
                "	override function foo()",
                "	abstract function bar()",
                "init",
                "	B b = () -> testSuccess()",
                "	b.bar()"
        );
    }

    @Test
    public void oneAbstractMethodInheritedOverloaded() {
        testAssertOkLines(false,
                "package test",
                "native testSuccess()",
                "abstract class A",
                "	abstract function foo()",
                "abstract class B extends A",
                "	override function foo()",
                "	abstract function foo(int x)",
                "init",
                "	B b = (int i) -> testSuccess()",
                "	b.foo(5)"
        );
    }

    @Test
    public void twoAbstractMethods() {
        testAssertErrorsLines(false, "Cannot assign () -> Void to A",
                "package test",
                "native testSuccess()",
                "abstract class A",
                "	abstract function foo()",
                "	abstract function bar()",
                "init",
                "	A a = () -> begin",
                "		skip",
                "	end"
        );
    }

    @Test
    public void closure_void() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply()",
                "init",
                "	SimpleFunc f = () -> begin",
                "		testSuccess()",
                "	end",
                "	f.apply()"
        );
    }

    @Test
    public void closure_void_call() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply()",
                "function foo() returns int",
                "	testSuccess()",
                "	return 4",
                "init",
                "	SimpleFunc f = () -> foo()",
                "	f.apply()"
        );
    }

    @Test
    public void code_anonfunc1() {
        testAssertOkLines(false,
                "package test",
                "function foo(code c)",
                "function bar()",
                "init",
                "	foo(() -> bar())"
        );
    }

    @Test
    public void code_anonfunc2() {
        testAssertOkLines(false,
                "package test",
                "function foo(code c)",
                "function bar() returns int",
                "	return 3",
                "init",
                "	foo(() -> bar())"
        );
    }

    @Test
    public void code_anonfunc_mixed() {
        testAssertOkLines(false,
                "package test",
                "interface I",
                "	function f()",
                "function foo(code c)",
                "function bar(I i)",
                "init",
                "	foo(() -> begin",
                "		let x = 3",
                "		bar(() -> begin",
                "			let y = x + 1",
                "		end)",
                "	end)"
        );
    }

    @Test
    public void code_anonfunc_mixed_err1() {
        testAssertErrorsLines(false, "Cannot capture local variable 'z'",
                "package test",
                "interface I",
                "	function f()",
                "function foo(code c)",
                "function bar(I i)",
                "init",
                "	let z = 1",
                "	foo(() -> begin",
                "		let x = 3",
                "		bar(() -> begin",
                "			let y = x + z",
                "		end)",
                "	end)"
        );
    }

    @Test
    public void code_anonfuncErr() {
        testAssertErrorsLines(false, "Cannot capture local variable 'x'",
                "package test",
                "function foo(code c)",
                "function bar(int x)",
                "init",
                "	let x = 4",
                "	foo(() -> bar(x))"
        );
    }

    @Test
    public void code_anonfuncErr2() {
        testAssertErrorsLines(false, "Cannot capture local variable 'this'",
                "package test",
                "function foo(code c)",
                "class C",
                "	function bar()",
                "	function fuz()",
                "		foo(() -> bar())"
        );
    }


    @Test
    public void code_anonfuncTuple() {
        testAssertOkLines(true,
                "type trigger extends handle",
                "package test",
                "@extern native CreateTrigger() returns trigger",
                "@extern native TriggerAddAction(trigger t, code c)",
                "@extern native TriggerEvaluate(trigger t)",
                "native testSuccess()",
                "tuple test (int a, int b, int c)",
                "let x = test(1, 2, 3)",
                "function foo(code c)",
                "    let t = CreateTrigger()",
                "    TriggerAddAction(t, c)",
                "    TriggerEvaluate(t)",
                "function bar(int x)",
                "init",
                "    foo() ->",
                "        let y = x.b",
                "        if y == 2",
                "            testSuccess()"
        );
    }

    @Test
    public void tryCaptureArray() {
        testAssertErrorsLines(true, "cannot capture local array",
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply(int x, int y) returns int",
                "init",
                "	int array bar",
                "	SimpleFunc f = (int x, int y) -> bar[1] + bar[2]",
                "	if f.apply(3,4) == 7",
                "		testSuccess()"
        );
    }

    @Test
    public void tryCaptureTuple() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "tuple vec2(real x, real y)",
                "interface SimpleFunc",
                "	function call()",
                "init",
                "	vec2 v = vec2(1,2)",
                "	SimpleFunc f = () -> begin",
                "		v.x = v.y",
                "		if v.x == v.y",
                "			testSuccess()",
                "	end",
                "	f.call()"
        );
    }


    @Test
    public void intliteralClosure() { // see bug #255
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "tuple vec2(real x, real y)",
                "interface SimpleFunc",
                "	function call() returns int",
                "init",
                "	",
                "	SimpleFunc f = () -> 1",
                "	if f.call() == 1",
                "		testSuccess()"
        );
    }

    @Test
    public void uninitialized() {
        testAssertErrorsLines(false, "may not have been initialized",
                "package test",
                "interface SimpleFunc",
                "	function call() returns int",
                "init",
                "	int x",
                "	SimpleFunc f = () -> x + 1"
        );
    }

    @Test
    public void uninitialized2() {
        testAssertErrorsLines(false, "may not have been initialized",
                "package test",
                "interface SimpleFunc",
                "	function call()",
                "init",
                "	int x",
                "	SimpleFunc f = () -> begin",
                "		int y = x + 1",
                "	end"
        );
    }

    @Test
    public void uninitialized3() {
        testAssertErrorsLines(false, "may not have been initialized",
                "package test",
                "interface SimpleFunc",
                "	function call()",
                "init",
                "	SimpleFunc f = () -> begin",
                "		int x",
                "		int y = x + 1",
                "	end"
        );
    }

    @Test
    public void uninitialized4() {
        testAssertOkLines(false,
                "package test",
                "interface SimpleFunc",
                "	function call()",
                "init",
                "	SimpleFunc f = () -> begin",
                "		int x = 1",
                "		int y = x + 1",
                "	end"
        );
    }

    @Test
    public void uninitialized6() {
        testAssertErrorsLines(false, "may not have been initialized",
                "package test",
                "interface SimpleFunc",
                "	function call()",
                "init",
                "	int x",
                "	SimpleFunc f = () -> begin",
                "		x = 1",
                "		int y = x + 1",
                "	end"
        );
    }

    @Test
    public void skipInClosure() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "    function call()",
                "init",
                "    int x",
                "    SimpleFunc f = () -> skip",
                "    f.call()",
                "    testSuccess()"
        );
    }

    @Test
    public void skipInClosure_fail() {
        testAssertErrorsLines(false, "Cannot assign () -> Void to SimpleFunc",
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "    function call() returns int",
                "init",
                "    int x",
                "    SimpleFunc f = () -> skip",
                "    f.call()",
                "    testSuccess()"
        );
    }

    @Test
    public void testDispatch() {
        test().executeProg(true)
            .testLua(false)
                .executeProgOnlyAfterTransforms()
                .lines(
                "package A",
                "   native testSuccess()",
                "   interface B",
                "       function foo() returns int",
                "   function bar(B b)",
                "       destroy b",
                "   init",
                "       bar(() -> 0)",
                "       B b2 = () -> 0",
                "       if (b2 castTo int) == 1",
                "           testSuccess()"
        );

    }


    @Test
    public void withoutParameterType() {
        testAssertOkLines(true,
                "package A",
                "   native testSuccess()",
                "   interface Func",
                "       function apply(int x) returns int",
                "   function bar(Func f) returns int",
                "       return f.apply(5)",
                "   init",
                "       if bar(x -> x + 1) == 6",
                "           testSuccess()");

    }


    @Test
    public void generic1() {
        testAssertOkLines(true,
                "package A",
                "   native testSuccess()",
                "   interface Func<S,T>",
                "       function apply(S x) returns T",
                "   class C<T>",
                "       constant T x",
                "       construct(T x)",
                "           this.x = x",
                "       function map<X>(Func<T,X> f) returns C<X>",
                "           return new C(f.apply(x))",
                "   init",
                "       let a = new C(5)",
                "       let b = a.map<int>((int x) -> 2*x)",
                "       if b.x == 10",
                "           testSuccess()");

    }

    @Test
    public void generic2() {
        testAssertOkLines(true,
                "package A",
                "   native testSuccess()",
                "   interface Func<S,T>",
                "       function apply(S x) returns T",
                "   class C<T>",
                "       constant T x",
                "       construct(T x)",
                "           this.x = x",
                "       function map<X>(Func<T,X> f) returns C<X>",
                "           return new C(f.apply(x))",
                "   init",
                "       let a = new C(5)",
                "       let b = a.map<int>(x -> 2*x)",
                "       if b.x == 10",
                "           testSuccess()");

    }

    @Test
    public void generic3() {
        testAssertOkLines(true,
                "package A",
                "   native testSuccess()",
                "   interface Func<S,T>",
                "       function apply(S x) returns T",
                "   class C<T>",
                "       constant T x",
                "       construct(T x)",
                "           this.x = x",
                "       function map<X>(Func<T,X> f) returns C<X>",
                "           return new C(f.apply(x))",
                "   init",
                "       let a = new C(5)",
                "       let b = a.map(x -> 2*x)",
                "       if b.x == 10",
                "           testSuccess()");

    }

    @Test
    public void overload1() {
        testAssertErrorsLines(true, "No operator overloading function for operator + was found for operands (integer or string) and integer-literal.",
                "package A",
                "   native testSuccess()",
                "   interface Func",
                "       function apply(int x) returns int",
                "   interface Func2",
                "       function apply(string s) returns int",
                "   function bar(Func f) returns int",
                "       return f.apply(1)",
                "   function bar(Func2 f) returns int",
                "       return f.apply(\"a\")",
                "   init",
                "       if bar(x -> x + 1) == 2",
                "           testSuccess()");

    }

    @Test
    public void overload2() {
        testAssertOkLines(true,
                "package A",
                "   native testSuccess()",
                "   interface Func",
                "       function apply(int x) returns int",
                "   interface Func2",
                "       function apply(int x, int y) returns int",
                "   function bar(Func f) returns int",
                "       return f.apply(1)",
                "   function bar(Func2 f) returns int",
                "       return f.apply(1,2)",
                "   init",
                "       if bar((int x) -> x + 1) == 2",
                "           testSuccess()");

    }

    @Test
    public void blockSyntax() {
        testAssertOkLines(true,
                "package A",
                "    native testSuccess()",
                "    interface Runnable",
                "        function run()",
                "    function twice(Runnable r)",
                "        r.run()",
                "        r.run()",
                "    int x = 0",
                "    init",
                "        twice() ->",
                "            x = x + 1",
                "        if x == 2",
                "            testSuccess()");

    }

    @Test
    public void blockSyntax2() {
        testAssertOkLines(true,
                "package A",
                "    native testSuccess()",
                "    interface F",
                "        function apply(int x) returns int",
                "    function blub(int x, F f) returns int",
                "        return f.apply(x)",
                "    init",
                "        int x = blub(5) x ->",
                "            return x + 1",
                "        if x == 6",
                "            testSuccess()");

    }


    @Test
    public void testOverloadingFuncClosureUse() {
        testAssertOkLines(true,
                "package A",
                "    native testSuccess()",
                "    interface ABC",
                "        function foo() returns int",
                "    class B",
                "    function foo(ABC a) returns int",
                "        return a.foo()",
                "    function foo(B a) returns int",
                "        return 13",
                "    init",
                "        if foo(() -> 42) == 42",
                "            testSuccess()");
    }

    @Test
    public void testOverloadingConstructorClosureUse() {
        testAssertOkLines(true,
                "package A",
                "    native testSuccess()",
                "    interface ABC",
                "        function foo() returns int",
                "    class B",
                "    class A",
                "        construct(ABC a)",
                "            testSuccess()",
                "        construct(B a)",
                "    init",
                "        new A(() -> 42)"
        );
    }

    @Test
    public void closureCaptureMain() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "interface SimpleFunc",
                "	function apply(int x) returns int",
                "init",
                "	int main = 4",
                "	SimpleFunc f = (int x) -> x + main",
                "	if f.apply(3) == 7",
                "		testSuccess()"
        );
    }


    @Test
    public void closureInClassInit() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Func",
            "	function apply(int x) returns int",
            "class A",
            "	Func f = x -> x + 1",
            "	construct()",
            "	construct(int x)",
            "init",
            "	let a = new A",
            "	if a.f.apply(3) == 4",
            "		testSuccess()"
        );
    }

    @Test
    public void overloadSameParameterTypeButDifferentReturnType() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface A",
            "	 function foo(int lvl) returns string",
            "interface B",
            "	 function foo(int lvl) returns int",
            "function bar(A a)",
            "    testSuccess()",
            "function bar(B b)",
            "init",
            "	 bar(lvl -> \"\")"
        );
    }

}
