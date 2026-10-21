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
    public void interfaceDispatchThroughNestedModuleWorksInBothBackends() {
        test().testLua(true).luaOnly(false).executeProg().lines(
                "package test",
                "native testSuccess()",
                "interface Greeter",
                "    function greet() returns int",
                "module FirstGreeter",
                "    function greet() returns int",
                "        return 1",
                "module NestedFirstGreeter",
                "    use FirstGreeter",
                "module SecondGreeter",
                "    function greet() returns int",
                "        return 2",
                "module GreeterCaller",
                "    function call(Greeter greeter) returns int",
                "        return greeter.greet()",
                "class First implements Greeter",
                "    use NestedFirstGreeter",
                "    use GreeterCaller",
                "class Second implements Greeter",
                "    use SecondGreeter",
                "    use GreeterCaller",
                "init",
                "    Greeter first = new First()",
                "    Greeter second = new Second()",
                "    First firstObject = new First()",
                "    Second secondObject = new Second()",
                "    if first.greet() == 1 and second.greet() == 2",
                "        if firstObject.call(second) == 2 and secondObject.call(first) == 1",
                "            testSuccess()",
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
    public void interfaceImplementationFromSuperClass() {
        testAssertOkLines(true,
                "package test",
                "       native testSuccess()",
                "       interface Foo",
                "               function doSomething()",
                "       interface Bar",
                "               function doSomething()",
                "       class BaseFoo implements Foo",
                "               override function doSomething()",
                "                       testSuccess()",
                "       class FooBar extends BaseFoo implements Bar",
                "       init",
                "               FooBar fb = new FooBar()",
                "               fb.doSomething()",
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

    /**
     * C implements Omega's abstract m with the m it inherits from Base, which is not an Omega. The dispatch over
     * Omega's implementations reaches C (and D below it) and must call Base's m, not the one of another implementor.
     */
    @Test
    public void anAbstractInterfaceMethodImplementedByAnInheritedMethodIsDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Omega",
            "    function m() returns int",
            "class Base",
            "    function m() returns int",
            "        return 1",
            "class C extends Base implements Omega",
            "class D extends C",
            "class X implements Omega",
            "    function m() returns int",
            "        return 3",
            "function viaOmega(Omega a) returns int",
            "    return a.m()",
            "init",
            "    if viaOmega(new C()) == 1 and viaOmega(new D()) == 1 and viaOmega(new X()) == 3 and new C().m() == 1",
            "        testSuccess()");
    }

    private static final String[] OVERRIDE_BELOW_THE_INHERITING_CLASS = {
        "package test",
        "native testSuccess()",
        "interface Omega",
        "    function m() returns int",
        "class Base",
        "    function m() returns int",
        "        return 1",
        "class C extends Base implements Omega",
        "class D extends C",
        "    override function m() returns int",
        "        return 4",
        "class E extends D",
        "class X implements Omega",
        "    function m() returns int",
        "        return 3",
        "function viaOmega(Omega a) returns int",
        "    return a.m()",
        "function viaBase(Base b) returns int",
        "    return b.m()",
        "init",
        "    if viaOmega(new C()) == 1 and viaOmega(new D()) == 4 and viaOmega(new E()) == 4 and viaOmega(new X()) == 3",
        "        and viaBase(new D()) == 4 and viaBase(new C()) == 1",
        "        testSuccess()"};

    /** An override below the class which inherits the implementation is reached through the interface too. */
    @Test
    public void anOverrideBelowTheClassWhichInheritsTheImplementationIsDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(OVERRIDE_BELOW_THE_INHERITING_CLASS);
    }

    /**
     * The method C has of its own for the m it inherits keeps D's override as a sub-method, as Base's m does: a
     * bridge stays linked to the overrides below it (AGENTS.md section 8), which the dispatch preparation and the
     * specialisation follow.
     */
    @Test
    public void theMethodAClassHasOfItsOwnKeepsTheOverridesBelowIt() {
        de.peeeq.wurstscript.gui.WurstGuiCliImpl gui = new de.peeeq.wurstscript.gui.WurstGuiCliImpl();
        de.peeeq.wurstio.WurstCompilerJassImpl compiler =
            new de.peeeq.wurstio.WurstCompilerJassImpl(null, gui, null, new de.peeeq.wurstscript.RunArgs());
        de.peeeq.wurstscript.ast.WurstModel model = parseFiles(java.util.Collections.emptyList(),
            java.util.Collections.singletonList(new CU("InterfaceTests.wurst", String.join("\n", OVERRIDE_BELOW_THE_INHERITING_CLASS))),
            false, compiler);
        compiler.checkProg(model);
        org.testng.Assert.assertTrue(gui.getErrorList().isEmpty(), gui.getErrorList().toString());
        compiler.translateProgToIm(model);
        java.util.Map<String, de.peeeq.wurstscript.jassIm.ImClass> classes = new java.util.HashMap<>();
        for (de.peeeq.wurstscript.jassIm.ImClass c : compiler.getImProg().getClasses()) {
            classes.put(c.getName(), c);
        }
        de.peeeq.wurstscript.jassIm.ImMethod baseM = classes.get("Base").getMethods().stream()
            .filter(m -> !m.getName().startsWith("destroy")).findFirst().orElseThrow();
        de.peeeq.wurstscript.jassIm.ImMethod bridge = classes.get("C").getMethods().stream()
            .filter(m -> m.getImplementation() == baseM.getImplementation()).findFirst().orElseThrow();
        org.testng.Assert.assertTrue(bridge.getSubMethods().stream().anyMatch(s -> s.attrClass() == classes.get("D")),
            "C's method for Base's m is linked to D's override: " + bridge.getSubMethods());
    }

    /**
     * C gets m from Base and a default m from Default, and Abstract declares m without a body. A default beats an
     * inherited method, so C's m is the default through either interface (Lua binds one implementation for C to
     * both), and D's override below C beats both. Calls through both interfaces are in the program, so Lua keeps both.
     */
    @Test
    public void aDefaultBeatsTheInheritedMethodWhereAnotherInterfaceHasNoBody() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Abstract",
            "    function m() returns int",
            "interface Default",
            "    function m() returns int",
            "        return 2",
            "class Base",
            "    function m() returns int",
            "        return 1",
            "class C extends Base implements Abstract, Default",
            "class D extends C",
            "    override function m() returns int",
            "        return 4",
            "function viaDefault(Default a) returns int",
            "    return a.m()",
            "function viaAbstract(Abstract a) returns int",
            "    return a.m()",
            "init",
            "    if viaDefault(new C()) == 2 and viaDefault(new D()) == 4 and viaAbstract(new C()) == 2 and viaAbstract(new D()) == 4",
            "        testSuccess()");
    }

    /** As {@link #anOverrideBelowTheClassWhichInheritsTheImplementationIsDispatched}, with C and D generic. */
    @Test
    public void aGenericClassImplementingTheInterfaceWithAnInheritedMethodIsDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Omega",
            "    function m() returns int",
            "class Base",
            "    function m() returns int",
            "        return 1",
            "class C<T:> extends Base implements Omega",
            "class D<T:> extends C<T>",
            "    override function m() returns int",
            "        return 4",
            "class E<T:> extends C<T>",
            "class X implements Omega",
            "    function m() returns int",
            "        return 3",
            "function viaOmega(Omega a) returns int",
            "    return a.m()",
            "function viaBase(Base b) returns int",
            "    return b.m()",
            "init",
            "    if viaOmega(new C<int>()) == 1 and viaOmega(new D<int>()) == 4 and viaOmega(new E<real>()) == 1",
            "        and viaOmega(new X()) == 3 and viaBase(new C<int>()) == 1 and viaBase(new D<int>()) == 4",
            "        testSuccess()");
    }

    /** A plain class overrides the method below the generic class which inherits it. */
    @Test
    public void aPlainOverrideBelowAGenericClassImplementingTheInterfaceWithAnInheritedMethodIsDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Omega",
            "    function m() returns int",
            "class Base",
            "    function m() returns int",
            "        return 1",
            "class C<T:> extends Base implements Omega",
            "class D extends C<int>",
            "    override function m() returns int",
            "        return 4",
            "class E extends D",
            "function viaOmega(Omega a) returns int",
            "    return a.m()",
            "function viaBase(Base b) returns int",
            "    return b.m()",
            "init",
            "    if viaOmega(new C<int>()) == 1 and viaOmega(new D()) == 4 and viaOmega(new E()) == 4",
            "        and viaBase(new C<int>()) == 1 and viaBase(new D()) == 4",
            "        testSuccess()");
    }

    /** The method comes from a generic class, which a plain class implementing the interface extends. */
    @Test
    public void aClassImplementingTheInterfaceWithAMethodOfAGenericSuperclassIsDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Omega",
            "    function m() returns int",
            "class Base<T:>",
            "    function m() returns int",
            "        return 1",
            "class C extends Base<int> implements Omega",
            "class D extends C",
            "    override function m() returns int",
            "        return 4",
            "class X implements Omega",
            "    function m() returns int",
            "        return 3",
            "function viaOmega(Omega a) returns int",
            "    return a.m()",
            "init",
            "    if viaOmega(new C()) == 1 and viaOmega(new D()) == 4 and viaOmega(new X()) == 3 and new Base<int>().m() == 1",
            "        testSuccess()");
    }

    /**
     * Both classes and the interface are generic, and the inherited method reads a field of its class's type
     * parameter, so the implementation C gets must be Base's specialised for C's type argument.
     */
    @Test
    public void aGenericInterfaceImplementedWithAMethodOfAGenericSuperclassIsDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Omega<T:>",
            "    function get() returns T",
            "class Base<T:>",
            "    T v",
            "    construct(T v)",
            "        this.v = v",
            "    function get() returns T",
            "        return v",
            "class C<T:> extends Base<T> implements Omega<T>",
            "    construct(T v)",
            "        super(v)",
            "class X implements Omega<int>",
            "    function get() returns int",
            "        return 3",
            "function viaOmega(Omega<int> a) returns int",
            "    return a.get()",
            "function viaOmegaS(Omega<string> a) returns string",
            "    return a.get()",
            "init",
            "    if viaOmega(new C<int>(5)) == 5 and viaOmega(new X()) == 3 and viaOmegaS(new C<string>(\"s\")) == \"s\"",
            "        testSuccess()");
    }

    /**
     * C's type parameters are not Base's: C instantiates Base with a type of its own choosing and with the second of
     * its two parameters, and the method takes a parameter of Base's type parameter. C's method must run Base's for
     * Base's instantiation, not C's.
     */
    @Test
    public void aGenericClassImplementingTheInterfaceWithAMethodOfADifferentlyInstantiatedSuperclassIsDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Named",
            "    function name(string prefix) returns string",
            "interface Swap<T:>",
            "    function swap(T t) returns T",
            "class Base<T:>",
            "    T v",
            "    construct(T v)",
            "        this.v = v",
            "    function name(string prefix) returns string",
            "        return prefix + \"base\"",
            "    function swap(T t) returns T",
            "        let old = v",
            "        v = t",
            "        return old",
            "class Fixed<T:> extends Base<string> implements Named",
            "    T other",
            "    construct(T other)",
            "        super(\"fixed\")",
            "        this.other = other",
            "class Second<A:, B:> extends Base<B> implements Swap<B>",
            "    construct(B b)",
            "        super(b)",
            "class X implements Named",
            "    function name(string prefix) returns string",
            "        return prefix + \"x\"",
            "function viaNamed(Named n) returns string",
            "    return n.name(\"a-\")",
            "function viaSwap(Swap<string> s, string t) returns string",
            "    return s.swap(t)",
            "init",
            "    let s = new Second<int, string>(\"one\")",
            "    if viaNamed(new Fixed<int>(7)) == \"a-base\" and viaNamed(new X()) == \"a-x\"",
            "        and viaSwap(s, \"two\") == \"one\" and viaSwap(s, \"three\") == \"two\"",
            "        testSuccess()");
    }

    /** The inherited implementation takes a vararg, which the function the generic class gets must take too. */
    @Test
    public void aGenericClassImplementingAVarargInterfaceMethodWithAnInheritedMethodIsDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Summer",
            "    function sum(vararg int xs) returns int",
            "class Base",
            "    function sum(vararg int xs) returns int",
            "        var r = 0",
            "        for x in xs",
            "            r += x",
            "        return r",
            "class C<T:> extends Base implements Summer",
            "class D extends Base implements Summer",
            "class X implements Summer",
            "    function sum(vararg int xs) returns int",
            "        return -1",
            "function viaSummer(Summer s) returns int",
            "    return s.sum(1, 2, 3)",
            "init",
            "    if viaSummer(new C<int>()) == 6 and viaSummer(new D()) == 6 and viaSummer(new X()) == -1",
            "        testSuccess()");
    }

    /**
     * Static classes inside a generic class have its type parameter as a variable of their own, which no superclass
     * binds: C sees Base's as the parameter itself. Lua only: on Jass a static class extending another inside a
     * generic class does not compile yet, with or without an interface (the generic elimination specialises Base
     * without the parameter).
     */
    @Test
    public void aStaticClassOfAGenericClassImplementingTheInterfaceWithAnInheritedMethodIsDispatched() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Omega",
            "    function m() returns int",
            "class Outer<T:>",
            "    function make() returns Omega",
            "        return new C()",
            "    static class Base",
            "        function m() returns int",
            "            return 1",
            "    static class C extends Base implements Omega",
            "class X implements Omega",
            "    function m() returns int",
            "        return 3",
            "function viaOmega(Omega a) returns int",
            "    return a.m()",
            "init",
            "    if viaOmega(new Outer<int>().make()) == 1 and viaOmega(new X()) == 3",
            "        testSuccess()");
    }

    /**
     * The function a generic class gets for an inherited implementation only calls it, so an optimised Lua build
     * inlines the call and a dispatch through the interface runs Base's body directly.
     */
    @Test
    public void theFunctionAGenericClassGetsForAnInheritedMethodIsInlined() throws java.io.IOException {
        test().testLua(true).inline().localOptimizations().executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Named",
            "    function name(string prefix) returns string",
            "class Base<T:>",
            "    function name(string prefix) returns string",
            "        return prefix + \"base\"",
            "class Fixed<T:> extends Base<string> implements Named",
            "class X implements Named",
            "    function name(string prefix) returns string",
            "        return prefix + \"x\"",
            "function viaNamed(Named n) returns string",
            "    return n.name(\"a-\")",
            "init",
            "    if viaNamed(new Fixed<int>()) == \"a-base\" and viaNamed(new X()) == \"a-x\"",
            "        testSuccess()");
        String lua = com.google.common.io.Files.asCharSource(new java.io.File(
                "test-output/lua/InterfaceTests_theFunctionAGenericClassGetsForAnInheritedMethodIsInlined.lua"),
            java.nio.charset.StandardCharsets.UTF_8).read();
        int start = lua.indexOf("function Fixed_Fixed_name(");
        org.testng.Assert.assertTrue(start >= 0, lua);
        String body = lua.substring(start, lua.indexOf("\nend", start));
        org.testng.Assert.assertTrue(body.contains("\"base\""), body);
        org.testng.Assert.assertFalse(body.contains("Base_"), body);
    }

    /** As {@link #aDefaultBeatsTheInheritedMethodWhereAnotherInterfaceHasNoBody}, with C and D generic. */
    @Test
    public void aDefaultBeatsTheInheritedMethodOfAGenericClassWhereAnotherInterfaceHasNoBody() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Abstract",
            "    function m() returns int",
            "interface Default",
            "    function m() returns int",
            "        return 2",
            "class Base",
            "    function m() returns int",
            "        return 1",
            "class C<T:> extends Base implements Abstract, Default",
            "class D<T:> extends C<T>",
            "    override function m() returns int",
            "        return 4",
            "function viaDefault(Default a) returns int",
            "    return a.m()",
            "function viaAbstract(Abstract a) returns int",
            "    return a.m()",
            "init",
            "    if viaDefault(new C<int>()) == 2 and viaDefault(new D<int>()) == 4",
            "        and viaAbstract(new C<int>()) == 2 and viaAbstract(new D<int>()) == 4",
            "        testSuccess()");
    }

    /** A class implements an interface method with a type parameter of its own with a method which declares the same one: a call through the interface with either type argument reaches each class's method. */
    @Test
    public void aMethodWithATypeParameterOfItsOwnImplementsTheInterfaceMethod() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Top",
            "    function pick<T:>(T a, T b) returns T",
            "class C implements Top",
            "    function pick<T:>(T a, T b) returns T",
            "        return a",
            "class X implements Top",
            "    function pick<T:>(T a, T b) returns T",
            "        return b",
            "function viaTopInt(Top t) returns int",
            "    return t.pick<int>(1, 2)",
            "function viaTopString(Top t) returns string",
            "    return t.pick<string>(\"a\", \"b\")",
            "init",
            "    if viaTopInt(new C()) == 1 and viaTopString(new C()) == \"a\" and viaTopInt(new X()) == 2 and viaTopString(new X()) == \"b\"",
            "        testSuccess()");
    }

    /**
     * Implementations of an interface method with a type parameter of its own, which each declares under a name of its
     * choosing: one which a generic class inherits from a class outside the interface (the function the class gets of
     * its own for it forwards the type parameter), and overrides below that class and below a class with its own method.
     */
    public static String[] ownTypeParameterDispatchSource() {
        return new String[]{
            "package test",
            "native testSuccess()",
            "interface Top",
            "    function pick<T:>(T a, T b) returns T",
            "class Base",
            "    function pick<T:>(T a, T b) returns T",
            "        return a",
            "class C<Y:> extends Base implements Top",
            "class D<Y:> extends C<Y>",
            "    override function pick<U:>(U a, U b) returns U",
            "        return b",
            "class E implements Top",
            "    function pick<S:>(S a, S b) returns S",
            "        return b",
            "class F extends E",
            "    override function pick<T:>(T a, T b) returns T",
            "        return a",
            "function viaTopInt(Top t) returns int",
            "    return t.pick<int>(1, 2)",
            "function viaTopString(Top t) returns string",
            "    return t.pick<string>(\"a\", \"b\")",
            "init",
            "    if viaTopInt(new C<real>()) == 1 and viaTopString(new C<real>()) == \"a\"",
            "        and viaTopInt(new D<int>()) == 2 and viaTopString(new D<int>()) == \"b\"",
            "        and viaTopInt(new E()) == 2 and viaTopString(new E()) == \"b\"",
            "        and viaTopInt(new F()) == 1 and viaTopString(new F()) == \"a\"",
            "        and new C<int>().pick(5, 6) == 5 and new D<int>().pick(5, 6) == 6",
            "        testSuccess()"};
    }

    @Test
    public void implementationsWithTypeParametersOfTheirOwnAreDispatched() {
        test().testLua(true).luaOnly(false).executeProg().lines(ownTypeParameterDispatchSource());
    }

    /** A plain class implements the interface method with a method with a type parameter of its own which it inherits, and a subclass overrides it. */
    @Test
    public void aPlainClassInheritingAMethodWithATypeParameterOfItsOwnImplementsTheInterfaceMethod() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Top",
            "    function pick<T:>(T a, T b) returns T",
            "class Base",
            "    function pick<T:>(T a, T b) returns T",
            "        return a",
            "class C extends Base implements Top",
            "class D extends C",
            "    override function pick<T:>(T a, T b) returns T",
            "        return b",
            "function viaTopInt(Top t) returns int",
            "    return t.pick<int>(1, 2)",
            "function viaTopString(Top t) returns string",
            "    return t.pick<string>(\"a\", \"b\")",
            "init",
            "    if viaTopInt(new C()) == 1 and viaTopString(new C()) == \"a\" and viaTopInt(new D()) == 2 and viaTopString(new D()) == \"b\"",
            "        testSuccess()");
    }

    /** The method of a generic interface has a type parameter of its own beside the interface's, and a generic superclass implements it: the function a class gets for it substitutes the superclass's type arguments as the class sees them and forwards the method's own. */
    @Test
    public void aMethodWithATypeParameterOfItsOwnOfAGenericSuperclassImplementsAGenericInterfaceMethod() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface GTop<Y:>",
            "    function pick<T:>(T a, Y y) returns T",
            "class Base<Y:>",
            "    Y last",
            "    function pick<T:>(T a, Y y) returns T",
            "        last = y",
            "        return a",
            "class C<Y:> extends Base<Y> implements GTop<Y>",
            "class P extends Base<string> implements GTop<string>",
            "init",
            "    let c = new C<real>()",
            "    GTop<real> g = c",
            "    let p = new P()",
            "    GTop<string> h = p",
            "    if g.pick<int>(3, 1.5) == 3 and c.last == 1.5 and g.pick<string>(\"x\", 2.5) == \"x\" and c.last == 2.5",
            "        and h.pick<int>(4, \"s\") == 4 and p.last == \"s\"",
            "        testSuccess()");
    }

    /** A generic class's own method with a type parameter of its own implements the method of a plain interface and of a generic one. */
    @Test
    public void aGenericClassMethodWithATypeParameterOfItsOwnImplementsTheInterfaceMethod() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Top",
            "    function pick<T:>(T a, T b) returns T",
            "interface GTop<Y:>",
            "    function second<T:>(T a, Y y) returns Y",
            "class C<Y:> implements Top, GTop<Y>",
            "    function pick<T:>(T a, T b) returns T",
            "        return b",
            "    function second<T:>(T a, Y y) returns Y",
            "        return y",
            "init",
            "    Top t = new C<real>()",
            "    GTop<string> g = new C<string>()",
            "    if t.pick<int>(1, 2) == 2 and t.pick<string>(\"a\", \"b\") == \"b\" and g.second<int>(1, \"s\") == \"s\"",
            "        testSuccess()");
    }

    /** A bounded type parameter of the method's own is dispatched with the call's type argument, also through the function a generic class gets for the method it inherits. */
    @Test
    public void aBoundedTypeParameterOfTheMethodsOwnIsDispatchedThroughTheInterface() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "implements Show<string>",
            "    function show(string x) returns string",
            "        return \"s\"",
            "interface Top",
            "    function describe<T: Show>(T value) returns string",
            "class Base",
            "    function describe<T: Show>(T value) returns string",
            "        return \"b\" + T.show(value)",
            "class C<Y:> extends Base implements Top",
            "class X implements Top",
            "    function describe<U: Show>(U value) returns string",
            "        return \"x\" + U.show(value)",
            "function via(Top t) returns string",
            "    return t.describe<int>(3) + t.describe(\"z\")",
            "init",
            "    if via(new C<real>()) == \"bibs\" and via(new X()) == \"xixs\"",
            "        testSuccess()");
    }

    /** The same with an old generic type parameter, which is erased: the implementations are found as for new ones. */
    @Test
    public void anOldGenericTypeParameterOfTheMethodsOwnIsDispatchedThroughTheInterface() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "class A",
            "    int v",
            "    construct(int v)",
            "        this.v = v",
            "interface Top",
            "    function pick<T>(T a, T b) returns T",
            "class Base",
            "    function pick<T>(T a, T b) returns T",
            "        return a",
            "class C<Y:> extends Base implements Top",
            "class X implements Top",
            "    function pick<U>(U a, U b) returns U",
            "        return b",
            "init",
            "    Top c = new C<real>()",
            "    Top x = new X()",
            "    if c.pick<int>(1, 2) == 1 and c.pick<A>(new A(5), new A(6)).v == 5",
            "        and x.pick<int>(1, 2) == 2 and x.pick<A>(new A(5), new A(6)).v == 6",
            "        testSuccess()");
    }

    /** A default with a type parameter of its own beats the inherited method where another interface declares the method without a body, as without type parameters ({@link #aDefaultBeatsTheInheritedMethodWhereAnotherInterfaceHasNoBody}). */
    @Test
    public void aDefaultWithATypeParameterOfItsOwnBeatsTheInheritedMethod() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Abstract",
            "    function wrap<T:>(T value) returns int",
            "interface Default",
            "    function wrap<U:>(U value) returns int",
            "        return 2",
            "class Base",
            "    function wrap<T:>(T value) returns int",
            "        return 1",
            "class C<Y:> extends Base implements Abstract, Default",
            "function viaAbstract(Abstract a) returns int",
            "    return a.wrap<int>(3) + a.wrap(\"x\")",
            "function viaDefault(Default a) returns int",
            "    return a.wrap<int>(3)",
            "init",
            "    if viaAbstract(new C<int>()) == 4 and viaDefault(new C<int>()) == 2",
            "        testSuccess()");
    }

    /** A method with another number of type parameters of its own does not implement the interface method. */
    @Test
    public void aMethodWithMoreTypeParametersOfItsOwnDoesNotImplementTheInterfaceMethod() {
        testAssertErrorsLines(false, "must implement the following functions",
            "package test",
            "native testSuccess()",
            "interface Top",
            "    function pick<T:>(T a, T b) returns T",
            "class C implements Top",
            "    function pick<T:, U:>(T a, T b) returns T",
            "        return a");
    }

    /** A method without the type parameter does not implement the interface method, even where no parameter mentions it. */
    @Test
    public void aMethodWithoutTheTypeParameterDoesNotImplementTheInterfaceMethod() {
        testAssertErrorsLines(false, "must implement the following functions",
            "package test",
            "native testSuccess()",
            "interface Top",
            "    function count<T:>() returns int",
            "class C implements Top",
            "    function count() returns int",
            "        return 1");
    }

    /** A method whose type parameter lacks the bound does not implement the interface method: the bounds must be the same. */
    @Test
    public void aMethodWithoutTheBoundDoesNotImplementTheInterfaceMethod() {
        testAssertErrorsLines(false, "must implement the following functions",
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "implements Show<string>",
            "    function show(string x) returns string",
            "        return \"s\"",
            "interface Top",
            "    function describe<T: Show>(T value) returns string",
            "class C implements Top",
            "    function describe<T:>(T value) returns string",
            "        return \"\"");
    }

    /** An override with another number of type parameters of its own says so. */
    @Test
    public void anOverrideWithAnotherNumberOfTypeParametersSaysSo() {
        testAssertErrorsLines(false, "takes 1 type parameters, but there are 2 type parameters here.",
            "package test",
            "native testSuccess()",
            "class Base",
            "    function pick<T:>(T a, T b) returns T",
            "        return a",
            "class C extends Base",
            "    override function pick<T:, U:>(T a, T b) returns T",
            "        return b");
    }

    /** An override whose type parameter has a bound the overridden one lacks says which it should be. */
    @Test
    public void anOverrideWithAnotherBoundSaysWhichItShouldBe() {
        testAssertErrorsLines(false, "Type parameter T: Show should be T: to override",
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "implements Show<string>",
            "    function show(string x) returns string",
            "        return \"s\"",
            "class Base",
            "    function describe<T:>(T value) returns string",
            "        return \"\"",
            "class C extends Base",
            "    override function describe<T: Show>(T value) returns string",
            "        return T.show(value)");
    }

    /** An override cannot make an old generic type parameter a new one. */
    @Test
    public void anOverrideCannotChangeTheKindOfGeneric() {
        testAssertErrorsLines(false, "Type parameter T: should be T to override",
            "package test",
            "native testSuccess()",
            "class Base",
            "    function pick<T>(T a, T b) returns T",
            "        return a",
            "class C extends Base",
            "    override function pick<T:>(T a, T b) returns T",
            "        return b");
    }

    /**
     * The method's own type argument is a tuple, which Lua specialises, while the generic class implementing the generic
     * interface is constructed with a plain type argument and stays erased: the implementation specialised for the
     * tuple must still be bound to the erased class, which the objects come from.
     */
    @Test
    public void aTupleTypeArgumentOfTheMethodsOwnReachesTheErasedImplementingClass() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "tuple pair(int a, int b)",
            "interface GTop<Y:>",
            "    function second<T:>(T a, Y y) returns int",
            "class C<Y:> implements GTop<Y>",
            "    function second<T:>(T a, Y y) returns int",
            "        return 1",
            "class X<Y:> implements GTop<Y>",
            "    function second<T:>(T a, Y y) returns int",
            "        return 2",
            "function via(GTop<string> g) returns int",
            "    return g.second<pair>(pair(1, 2), \"s\")",
            "init",
            "    if via(new C<string>()) == 1 and via(new X<string>()) == 2",
            "        testSuccess()");
    }

    /**
     * The interface is an old generic bound to real, so the implementation is reached through a function which converts
     * the index the interface passes; that function must take the method's own type parameter and pass it on.
     */
    @Test
    public void aMethodWithATypeParameterOfItsOwnImplementsAnOldGenericInterfaceMethodWhichNeedsConversion() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "real array stored",
            "int count = 0",
            "real last = 0",
            "function realToIndex(real r) returns int",
            "    count++",
            "    stored[count] = r",
            "    return count",
            "function realFromIndex(int i) returns real",
            "    return stored[i]",
            "interface I<X>",
            "    function f<T:>(X x, T t) returns T",
            "class C implements I<real>",
            "    function f<T:>(real x, T t) returns T",
            "        last = x",
            "        return t",
            "init",
            "    I<real> i = new C()",
            "    if i.f<int>(1.5, 3) == 3 and last == 1.5 and i.f<string>(2.5, \"s\") == \"s\" and last == 2.5",
            "        testSuccess()");
    }

    /**
     * A default of another interface is chosen over the inherited method only where it could override the abstract
     * method: an old generic default does not stand for a new generic method, and the inherited method runs.
     */
    @Test
    public void aDefaultOfAnotherKindOfGenericDoesNotBeatTheInheritedMethod() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Abstract",
            "    function wrap<T:>(T value) returns int",
            "interface Default",
            "    function wrap<U>(U value) returns int",
            "        return 2",
            "class Base",
            "    function wrap<T:>(T value) returns int",
            "        return 1",
            "class C<Y:> extends Base implements Abstract, Default",
            "function viaAbstract(Abstract a) returns int",
            "    return a.wrap<int>(3)",
            "init",
            "    if viaAbstract(new C<int>()) == 1",
            "        testSuccess()");
    }

    /** As {@link #aDefaultOfAnotherKindOfGenericDoesNotBeatTheInheritedMethod}, with a default which asks for a bound. */
    @Test
    public void aDefaultWithAnotherBoundDoesNotBeatTheInheritedMethod() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns int",
            "implements Show<int>",
            "    function show(int x) returns int",
            "        return 2",
            "interface Abstract",
            "    function wrap<T:>(T value) returns int",
            "interface Default",
            "    function wrap<U: Show>(U value) returns int",
            "        return U.show(value)",
            "class Base",
            "    function wrap<T:>(T value) returns int",
            "        return 1",
            "class C<Y:> extends Base implements Abstract, Default",
            "function viaAbstract(Abstract a) returns int",
            "    return a.wrap<int>(3)",
            "init",
            "    if viaAbstract(new C<int>()) == 1",
            "        testSuccess()");
    }
}
