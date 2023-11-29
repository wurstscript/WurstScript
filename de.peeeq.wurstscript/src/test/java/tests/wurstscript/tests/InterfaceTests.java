package tests.wurstscript.tests;

import de.peeeq.wurstscript.attributes.CompileError;
import org.hamcrest.CoreMatchers;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class InterfaceTests extends WurstScriptTest {


    @Test
    public void simple() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo() returns int",
                "	class B implements I",
                "		function foo() returns int",
                "			return 2",
                "	class C implements I",
                "		function foo() returns int",
                "			return 3",
                "	init",
                "		I i1 = new B()",
                "		I i2 = new C()",
                "		if i1.foo() == 2 and i2.foo() == 3",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void swap() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo() returns int",
                "	class B implements I",
                "		function foo() returns int",
                "			return 2",
                "	class C implements I",
                "		function foo() returns int",
                "			return 3",
                "	init",
                "		I i1 = new B()",
                "		I i2 = new C()",
                "		I temp = i2",
                "		i2 = i1",
                "		i1 = temp",
                "		if i1.foo() == 3 and i2.foo() == 2",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void swapArray() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	I array ar",
                "	interface I",
                "		function foo() returns int",
                "	class B implements I",
                "		function foo() returns int",
                "			return 2",
                "	class C implements I",
                "		function foo() returns int",
                "			return 3",
                "	init",
                "		ar[5] = new B()",
                "		ar[6] = new C()",
                "		let temp = ar[6]",
                "		ar[6] = ar[5]",
                "		ar[5] = temp",
                "		if ar[5].foo() == 3 and ar[6].foo() == 2",
                "			testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void equality() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo()",
                "	class B implements I",
                "		function foo()",
                "	class C implements I",
                "		function foo()",
                "	init",
                "		I a = new B()",
                "		I b = new B()",
                "		I c = new C()",
                "		if not (a == b or a == c)",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void inequality() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo()",
                "	class B implements I",
                "		function foo()",
                "	class C implements I",
                "		function foo()",
                "	init",
                "		I a = new B()",
                "		I b = new B()",
                "		I c = new C()",
                "		if a != b and a != c",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void hierarchy() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface A",
                "		function f1() returns int",
                "	interface B extends A",
                "		function f2() returns int",
                "	class C implements B",
                "		function f1() returns int",
                "			return 3",
                "		function f2() returns int",
                "			return 4",
                "	init",
                "		B b = new C()",
                "		A a = b",
                "		if a.f1() == 3",
                "			testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void as_argument() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo() returns int",
                "	class B implements I",
                "		function foo() returns int",
                "			return 2",
                "	class C implements I",
                "		function foo() returns int",
                "			return 3",
                "	function test(I i1, I i2)",
                "		if i1.foo() == 2 and i2.foo() == 3",
                "			testSuccess()",
                "	init",
                "		I i1 = new B()",
                "		I i2 = new C()",
                "		test(i1, i2)",
                "endpackage"
        );
    }


    @Test
    public void as_return_value() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo() returns int",
                "	class B implements I",
                "		function foo() returns int",
                "			return 2",
                "	class C implements I",
                "		function foo() returns int",
                "			return 3",
                "	function test(boolean b) returns I",
                "		if b",
                "			return new B()",
                "		else",
                "			return new C()",
                "	init",
                "		I i1 = test(true)",
                "		if i1.foo() == 2 and test(false).foo() == 3",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void type_param1() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface Collection<T>",
                "		function contains(int t) returns boolean",
                "	class Set<T> implements Collection<T>",
                "		function contains(int t) returns boolean",
                "			return false",
                "	init",
                "		Collection<int> c = new Set<int>()",
                "		if not c.contains(4)",
                "			testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void type_param_fail_generics() {
        testAssertErrorsLines(false, "Cannot assign",
                "package test",
                "	native testSuccess()",
                "	interface Collection<T>",
                "		function contains(int t) returns boolean",
                "	class Set<T> implements Collection<T>",
                "		function contains(int t) returns boolean",
                "			return false",
                "	class A",
                "	init",
                "		Collection<int> c = new Set<A>()", // cannot assign set of As to collection of int
                "		if not c.contains(4)",
                "			testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void type_param_complicated1() {
        testAssertOkLines(false,
                "package test",
                "	native testSuccess()",
                "	interface I<S,T,V>",
                "		function test() returns boolean",
                "	class A<X,Y> implements I<B, Y, X>",
                "		function test() returns boolean",
                "			return true",
                "	class B",
                "	class C",
                "	class D",
                "	init",
                "		I<B, C, D> i = new A<D, C>()",
                "		if i.test()",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void module_prob() {
        testAssertErrorsLines(false, "Expected I",
                "package test",
                "	interface I",
                "		function foo()",
                "	module M",
                "		construct()",
                "			bla(this)",
                "	class C implements I",
                "		use M",
                "		function foo()",
                "		",
                "	function bla(I i)",
                "endpackage"
        );
    }

    @Test
    public void type_param_complicated1_fail() {
        testAssertErrorsLines(false, "Cannot assign",
                "package test",
                "	native testSuccess()",
                "	interface I<S,T,V>",
                "		function test() returns boolean",
                "	class A<X,Y> implements I<B, Y, X>",
                "		function test() returns boolean",
                "			return true",
                "	class B",
                "	class C",
                "	class D",
                "	init",
                "		I<B, C, D> i = new A<C, D>()",
                "		if i.test()",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void type_param_complicated2() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I<S,T,V>",
                "		function test(S s, T t, V v) returns boolean",
                "	class A<X,Y> implements I<B, Y, X>",
                "		function test(B b, Y y, X x) returns boolean",
                "			return true",
                "	class B",
                "	class C",
                "	class D",
                "	init",
                "		I<B, C, D> i = new A<D, C>()",
                "		B b = new B()",
                "		C c = new C()",
                "		D d = new D()",
                "		if i.test(b, c, d)",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void type_param_class() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I<A>",
                "		function test(A a) returns boolean",
                "	class C<X> implements I<X>",
                "		function test(X x) returns boolean",
                "			return true",
                "	class B",
                "	init",
                "		I<B> i = new C<B>()",
                "		B b = null",
                "		if i.test(b)",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void missing_method() {
        testAssertErrorsLines(false, "implement",
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo() returns int",
                "	class B implements I",
                "		function bar() returns int",
                "			return 3",
                "endpackage"
        );
    }


    @Test
    public void wrong_method() {
        testAssertErrorsLines(false, "implement",
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo(int x) returns int",
                "	class B implements I",
                "		function foo(real x) returns int",
                "			return 3",
                "endpackage"
        );
    }

    @Test
    public void casts() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo() returns int",
                "	class B implements I",
                "		function foo() returns int",
                "			return 2",
                "	class C implements I",
                "		function foo() returns int",
                "			return 3",
                "	init",
                "		I a = new C()",
                "		int b = a castTo int",
                "		I c = b castTo I",
                "		if c == a",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void twoInterfaces() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	native testFail(string s)",
                "	interface A",
                "		function foo() returns int",
                "	interface B",
                "		function bar() returns int",
                "	class C implements A, B",
                "		function foo() returns int",
                "			return 1",
                "		function bar() returns int",
                "			return 3",
                "	class D implements A, B",
                "		function foo() returns int",
                "			return 2",
                "		function bar() returns int",
                "			return 4",
                "	init",
                "		A x1 = new C()",
                "		A x2 = new D()",
                "		B x3 = new C()",
                "		B x4 = new D()",
                "		if x1.foo() != 1",
                "			testFail(\"1\")",
                "		if x2.foo() != 2",
                "			testFail(\"2\")",
                "		if x3.bar() != 3",
                "			testFail(\"3\")",
                "		if x4.bar() != 4",
                "			testFail(\"4\")",
                "		testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void destroyInterface() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	int x = 0",
                "	interface I",
                "		function foo()",
                "	class B implements I",
                "		override function foo()",
                "		ondestroy",
                "			x = 1",
                "	class C implements I",
                "		override function foo()",
                "		ondestroy",
                "			x = 2",
                "	init",
                "		I i = new C()",
                "		destroy i",
                "		if x == 2",
                "			testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void testOverride() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo(int x)",
                "	class C implements I",
                "		function foo(string x)",
                "		function foo(int x)",
                "		    testSuccess()",
                "	init",
                "		I i = new C()",
                "		i.foo(7)",
                "endpackage"
        );
    }


    @Test
    public void testOverrideFail() {
        testAssertErrorsLines(false, "Non-abstract class C must implement the following functions:",
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo(int x)",
                "	class C implements I",
                "		function foo(string x)",
                "		function foo(int x, int y)",
                "endpackage"
        );
    }

    @Test
    public void testInterfaceDefaultImpl() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	interface I",
                "		function foo(int x) returns int",
                "			return x + 42",
                "	class C implements I",
                "	init",
                "		I i = new C()",
                "		if i.foo(1) == 43",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void implGap() { // #676
        testAssertOkLines(true,
                "package test1",
                "	interface I",
                "		function foo() returns int",
                "	public abstract class B implements I",
                "		override function foo() returns int",
                "			return 2",
                "endpackage",
                "package test2",
                "	import test1",
                "	public class C extends B",
                "endpackage",
                "package test",
                "	import test2",
                "	native testSuccess()",
                "	class D extends C",
                "	init",
                "		C c1 = new C()",
                "		C c2 = new D()",
                "		if c1.foo() == 2 and c2.foo() == 2",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void testEmptyImplements() {
        CompilationResult res = test().executeProg(false)
                .setStopOnFirstError(false)
                .lines(
                        "package test",
                        "native testSuccess()",
                        "class A implements",
                        "init"
                );
        for (CompileError compileError : res.getGui().getErrorList()) {
            System.err.println("" + compileError);
        }
        assertThat(res.getGui().getErrorList(), CoreMatchers.hasItem(
                ExtraMatchers.get("getMessage",
                        CoreMatchers.containsString("Expecting interface name after `implements`"))));


    }
}
