package tests.wurstscript.tests;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GenericsTests extends WurstScriptTest {


    @Test
    public void identity() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	function identity<A>(A a) returns A",
            "		return a",
            "	init",
            "		int x = identity(3)",
            "		if x == 3",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void identity2() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class C",
            "	function identity<A>(A a) returns A",
            "		return a",
            "	init",
            "		C a = new C()",
            "		C b = identity(a)",
            "		if a == b",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void function() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class List<T>",
            "		function iterator() returns Iterator<T>",
            "			return new Iterator<T>(this)",
            "	class Iterator<S>",
            "		S t",
            "		construct(List<S> t)",
            "			int x = 1",
            "		function hasNext() returns boolean",
            "			return true",
            "		function next() returns S",
            "			return t",
            "	class A",
            "	class B",
            "	class C",
            "	init",
            "		List<B> a = new List<B>()",
//				"		for B b in a",
            "		Iterator<B> iterator = a.iterator()",
            "		while iterator.hasNext()",
            "			B b = iterator.next()",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void testSubtypeGenericClass() {
        testAssertOkLines(false,
            "package test",
            "	class A<T>",
            "	class B<S> extends A<S>",
            "	init",
            "		A<int> x = new B<int>",
            "endpackage"
        );
    }

    @Test
    public void testSubtypeGenericClass2() {
        testAssertOkLines(false,
            "package test",
            "	class A<T>",
            "	class B<S> extends A<S>",
            "	function foo<X>()",
            "		A<X> x = new B<X>",
            "endpackage"
        );
    }

    @Test
    public void testSubtypeGenericInterface() {
        testAssertOkLines(false,
            "package test",
            "	interface I<T>",
            "	class B<S> implements I<S>",
            "	init",
            "		I<int> x = new B<int>",
            "endpackage"
        );
    }

    @Test
    public void identityFail1() {
        testAssertErrorsLines(true, "real",
            "package test",
            "	native testSuccess()",
            "	function identity<A>(A a) returns A",
            "		return a",
            "	init",
            "		real x = identity(3.14)",
            "		if x == 3.14",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void identityFail2() {
        testAssertErrorsLines(true, "Cannot assign C to real",
            "package test",
            "	function identity<A>(A a) returns A",
            "		return a",
            "	class C",
            "		int y",
            "	init",
            "		real x = identity(new C())",
            "endpackage"
        );
    }


    @Test
    public void cellExample() {
        testAssertErrorsLines(true, "Wrong parameter type",
            "package test",
            "	native testSuccess()",
            "	class Cell<T>",
            "		T elem",
            "		function set(T t)",
            "			elem = t",
            "		function get() returns T",
            "			return elem",
            "	class A",
            "	class B",
            "	init",
            "		Cell<A> c = new Cell<A>()",
            "		c.set(new B())",
            "endpackage"
        );
    }

    @Test
    public void implicitConversions() {
        test().executeProg(true).testLua(true).luaOnly(false).lines(
            "package test",
            "	native testSuccess()",
            "	class Cell<T>",
            "		T elem",
            "		function set(T t)",
            "			elem = t",
            "		function get() returns T",
            "			return elem",
            "",
            "	tuple bla(int z, int y)",
            "	function blaToIndex(bla b) returns int",
            "		return b.z",
            "	function blaFromIndex(int i) returns bla",
            "		return bla(i, 2)",
            "	init",
            "		Cell<bla> c = new Cell<bla>()",
            "		c.set(bla(5, 3))",
            "		if c.get() == bla(5, 2)",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void implicitConversions2() {
        test().executeProg(true).testLua(true).luaOnly(false).lines(
            "package test",
            "	native testSuccess()",
            "	class Cell<T>",
            "		T elem",
            "		function set(T t)",
            "			elem = t",
            "		function get() returns T",
            "			return elem",
            "",
            "	tuple bla(int z, int y)",
            "	function blaToIndex(bla b) returns int",
            "		return b.z",
            "	function blaFromIndex(int i) returns bla",
            "		return bla(i, 2)",
            "	init",
            "		Cell<bla> c = new Cell<bla>()",
            "		c.set(bla(5, 3))",
            "		c.set(c.get())",
            "		if c.get() == bla(5, 2)",
            "			testSuccess()",
            "endpackage"
        );
    }

    /**
     * A value read from a generic and passed straight back into one: the blaFromIndex of the
     * read and the blaToIndex of the write cancel out. They must cancel on both sides, so the
     * conversions are not symmetric here (blaToIndex multiplies), which shows a missing one.
     */
    private static final String[] NESTED_OLD_GENERIC_CONVERSIONS = {
        "package test",
        "	native testSuccess()",
        "	class Cell<T>",
        "		T elem",
        "		function set(T t)",
        "			elem = t",
        "		function get() returns T",
        "			return elem",
        "",
        "	tuple bla(int z, int y)",
        "	function blaToIndex(bla b) returns int",
        "		return b.z * 10",
        "	function blaFromIndex(int i) returns bla",
        "		return bla(i div 10, 2)",
        "	init",
        "		Cell<bla> c = new Cell<bla>()",
        "		c.set(bla(5, 3))",
        "		c.set(c.get())",
        "		if c.get() == bla(5, 2)",
        "			testSuccess()",
        "endpackage"
    };

    @Test
    public void nestedOldGenericConversionsCancel() {
        test().executeProg(true).testLua(true).luaOnly(false).lines(NESTED_OLD_GENERIC_CONVERSIONS);
    }

    @Test
    public void nestedOldGenericConversionsCancelOptimized() {
        test().executeProg(true).testLua(true).luaOnly(false).inline().localOptimizations()
            .lines(NESTED_OLD_GENERIC_CONVERSIONS);
    }

    @Test
    public void implicitConversions3() {
        test().executeProg(true).testLua(true).luaOnly(false).lines(
            "package test",
            "	native testSuccess()",
            "	public interface FoldClosure<T, Q>",
            "		function apply(T t, Q q) returns Q",
            "	class Cell<T>",
            "		T elem",
            "		function set(T t)",
            "			elem = t",
            "		function get() returns T",
            "			return elem",
            "		function fold<Q>(Q start, FoldClosure<T,Q> f) returns Q",
            "			return f.apply(elem, start)",
            "",
            "	tuple bla(int z, int y)",
            "	function blaToIndex(bla b) returns int",
            "		return b.z",
            "	function blaFromIndex(int i) returns bla",
            "		return bla(i, 2)",
            "	init",
            "		Cell<bla> c = new Cell<bla>()",
            "		c.set(bla(5, 3))",
            "		let x = c.fold(2, (e, a) -> e.z + a)",
            "		if x == 7",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void implicitConversions4() { // #490
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function booleanToIndex(bool b) returns int",
            "	return b ? 1 : 0",
            "function booleanFromIndex(int i) returns bool",
            "	return i == 0 ? false : true",
            "interface TFunc<T>",
            "    abstract function run(T t)",
            "",
            "function runFunc(TFunc<bool> func)",
            "    func.run(false)",
            "",
            "init",
            "    runFunc( (bool b) -> begin",
            "        testSuccess()",
            "    end )"
        );
    }

    @Test
    public void implicitConversions5() { // #490
        test().executeProg(true).testLua(true).luaOnly(false).lines(
            "package test",
            "native testSuccess()",
            "@extern native R2I(real r) returns int",
            "@extern native R2S(real r) returns string",
            "native println(string s)",
            "function realToIndex(real r) returns int",
            "	return R2I(r*1000.)",
            "function realFromIndex(int i) returns real",
            "	return i / 1000.",
            "interface F<A,R>",
            "	function apply(A a) returns R",
            "class Cell<T>",
            "	T elem",
            "	construct(T t)",
            "		this.elem = t",
            "	function get() returns T",
            "		return elem",
            "	function map<R>(F<T,R> f) returns Cell<R>",
            "		return new Cell(f.apply(elem))",
            "function real.assertEquals(real expected)",
            "	if this == expected",
            "		testSuccess()",
            "	else",
            "		println(R2S(this))",
            "init",
            "	let a = new Cell(5)",
            "	let b = a.map(i -> i*10.)",
            "	b.get().assertEquals(50)"
        );
    }

    /** {@code I<real>} with the conversions of real, implemented by B and through B by its subclasses. */
    private static final String[] BRIDGED_INTERFACE = {
        "package test",
        "native testSuccess()",
        "@extern native R2I(real r) returns int",
        "function realToIndex(real r) returns int",
        "    return R2I(r * 10.)",
        "function realFromIndex(int i) returns real",
        "    return i / 10.",
        "interface I<T>",
        "    function foo(T t) returns int",
        "interface J<T> extends I<T>",
        "class B implements I<real>",
        "    override function foo(real t) returns int",
        "        return R2I(t * 100.)",
        "function viaI(I<real> i) returns int",
        "    return i.foo(1.5)"};

    private static String[] bridged(String... rest) {
        String[] result = java.util.Arrays.copyOf(BRIDGED_INTERFACE, BRIDGED_INTERFACE.length + rest.length);
        System.arraycopy(rest, 0, result, BRIDGED_INTERFACE.length, rest.length);
        return result;
    }

    /**
     * The override in a subclass of the class which implements the interface is called through the interface with
     * the argument converted from its index, as the implementation of the superclass is.
     */
    @Test
    public void aSubclassOverrideIsCalledThroughTheInterfaceWithTheConvertedArgument() throws java.io.IOException {
        test().testLua(true).luaOnly(false).executeProg().lines(bridged(
            "class D extends B",
            "    override function foo(real t) returns int",
            "        return R2I(t * 1000.)",
            "init",
            "    if viaI(new B()) == 150 and viaI(new D()) == 1500",
            "        testSuccess()"));

        String name = "GenericsTests_aSubclassOverrideIsCalledThroughTheInterfaceWithTheConvertedArgument";
        // Jass: D has its own bridge, which converts the index back, and the dispatch of I.foo calls it
        String jass = java.nio.file.Files.readString(java.nio.file.Path.of("test-output", name + "_no_opts.j"));
        org.testng.Assert.assertTrue(jass.contains("function D_foo_wrapper takes integer this, integer t returns integer"), jass);
        org.testng.Assert.assertTrue(jass.contains("return D_foo(this, realFromIndex(t))"), jass);
        org.testng.Assert.assertTrue(jass.contains("= D_foo_wrapper(this, t)"), "the dispatch calls D's bridge:\n" + jass);
        // Lua: D binds the slot the call reads to its own bridge, not B's
        String lua = java.nio.file.Files.readString(java.nio.file.Path.of("test-output", "lua", name + ".lua"));
        java.util.regex.Matcher call = java.util.regex.Pattern.compile("__wurst_objectClass\\[\\w+\\]\\.(\\w+)\\(").matcher(lua);
        org.testng.Assert.assertTrue(call.find(), lua);
        String slot = call.group(1);
        org.testng.Assert.assertTrue(lua.contains("D." + slot + " = D_foo_wrapper"), "D binds " + slot + " to its bridge:\n" + lua);
        org.testng.Assert.assertTrue(lua.contains("B." + slot + " = B_foo_wrapper"), "B keeps its own:\n" + lua);
        java.util.regex.Matcher bridge = java.util.regex.Pattern.compile("function D_foo_wrapper\\((\\w+), (\\w+)\\) \\s*return (\\w+)\\(\\1, realFromIndex\\(\\2\\)\\)").matcher(lua);
        org.testng.Assert.assertTrue(bridge.find(), "D's bridge converts the argument back:\n" + lua);
    }

    @Test
    public void aSubclassWithoutAnOverrideIsCalledThroughTheInterfaceWithTheConvertedArgument() {
        test().testLua(true).luaOnly(false).executeProg().lines(bridged(
            "class D extends B",
            "init",
            "    if viaI(new D()) == 150",
            "        testSuccess()"));
    }

    /** The interface is implemented through one which extends it, with the type arguments of that one. */
    @Test
    public void anImplementationOfAnExtendingInterfaceIsCalledWithTheConvertedArgument() {
        test().testLua(true).luaOnly(false).executeProg().lines(bridged(
            "class K implements J<real>",
            "    override function foo(real t) returns int",
            "        return R2I(t * 1000.)",
            "init",
            "    if viaI(new K()) == 1500",
            "        testSuccess()"));
    }

    @Test
    public void implicitConversionsFail() {
        testAssertErrorsLines(true, "Could not find function blaFromIndex",
            "package test",
            "	native testSuccess()",
            "	class Cell<T>",
            "		T elem",
            "		function set(T t)",
            "			elem = t",
            "		function get() returns T",
            "			return elem",
            "",
            "	tuple bla(int z, int y)",
            "	function blaToIndex(bla b) returns int",
            "		return b.z",
            "	function blaaFromIndex(int i) returns bla",
            "		return bla(i, 2)",
            "	init",
            "		Cell<bla> c = new Cell<bla>()",
            "endpackage"
        );
    }

    @Test
    public void implicitConversionsFail2() {
        testAssertErrorsLines(true, "Parameter must be of type int",
            "package test",
            "	native testSuccess()",
            "	class Cell<T>",
            "		T elem",
            "		function set(T t)",
            "			elem = t",
            "		function get() returns T",
            "			return elem",
            "",
            "	tuple bla(int z, int y)",
            "	function blaToIndex(bla b) returns int",
            "		return b.z",
            "	function blaFromIndex(string i) returns bla",
            "		return bla(1, 2)",
            "	init",
            "		Cell<bla> c = new Cell<bla>()",
            "endpackage"
        );
    }

    @Test
    public void implicitConversionsAssign() {
        testAssertOkLines(false,
            "type unit extends handle",
            "package test",
            "	native testSuccess()",
            "	class Cell<T>",
            "		T elem",
            "		function set(T t)",
            "			elem = t",
            "		function get() returns T",
            "			return elem",
            "	function unitToIndex(unit u) returns int",
            "		return 0",
            "	function unitFromIndex(int i) returns unit",
            "		return null",
            "	init",
            "		Cell<unit> c = new Cell<unit>()",
            "		Cell<unit> c2 = c",
            "endpackage"
        );
    }

    @Test
    public void nativeTypes() {
        testAssertOkLines(false,
            "type effect extends handle",
            "package Test",
            "function effectToIndex(effect e) returns int",
            "	return 0",
            "function effectFromIndex(int index) returns effect",
            "	return null",
            "class L<T>",
            "init",
            "	L<effect> l = new L<effect>()"
        );
    }

    @Test
    public void implicitConversionFail() { // see bug #121
        testAssertOkLinesWithStdLib(false,
            "package Test",
            "import LinkedList",
//                "Table data",
//				"function effectToIndex(effect e) returns int",
//				"	return e.getHandleId()",
//
//				"function effectFromIndex(int index) returns effect",
//				"	data.saveFogState(0,ConvertFogState(index))",
//				"	return data.loadEffect(0)",

            "init",
            "	LinkedList<effect> fxs = new LinkedList<effect>()",
            "	for f in fxs",
            "		f.destr()",
            "endpackage");
    }

    @Test
    public void implicitConversionFail2() { // same as implicitConversionFail, but with for-from
        testAssertOkLinesWithStdLib(false,
            "package Test",
            "import LinkedList",
//                "Table data",

            "init",
            "	LinkedList<effect> fxs = new LinkedList<effect>()",
            "	for f from fxs.iterator()",
            "		f.destr()",
            "endpackage");
    }

    @Test
    public void implicitConversionFailSimple() { // see bug #121
        testAssertOkLines(false,
            "type effect extends handle",
            "package Test",
            "function effectToIndex(effect e) returns int",
            "	return 2",

            "function effectFromIndex(int index) returns effect",
            "	return null",

            "class List<T>",
            "	function get() returns T",
            "		return 0 castTo T",

            "init",
            "	List<effect> fxs = new List<effect>()",
            "	let f = fxs.get()",
            "endpackage");
    }


    @Test
    public void cast() {
        testAssertOkLines(false,
            "package Test",
            "native testSuccess()",
            "class Cell<T>",
            "	T o",
            "class A",
            "	function foo() returns int",
            "		return 5",
            "class B extends A",
            "	override function foo() returns int",
            "		return 6",
            "init",
            "	Cell<A> c = new Cell<A>()",
            "	c.o = new B()",
            "	B b = c.o castTo B"
        );
    }

    @Test
    public void genericsDispatch() {
        testAssertOkLines(true,
            "package Test",
            "native testSuccess()",
            "class Cell<T>",
            "	T o",
            "class A",
            "	function foo() returns int",
            "		return 5",
            "class B extends A",
            "	override function foo() returns int",
            "		return 6",
            "init",
            "	Cell<A> c = new Cell<A>()",
            "	c.o = new B()",
            "	if c.o.foo() == 6",
            "		testSuccess()"
        );
    }

    @Test
    public void genericsSubstitute1() {
        testAssertOkLines(false,
            "package Test",
            "native testSuccess()",
            "class A<T>",
            "	function bla(T t)",
            "class B extends A<C>",
            "class C",
            "init",
            "	let b = new B",
            "	b.bla(new C)"
        );
    }

    @Test
    public void genericsSubstitute2() {
        testAssertOkLines(false,
            "package Test",
            "native testSuccess()",
            "interface I<S,T>",
            "	function bla(T t, S s)",
            "		skip",
            "class A<U> implements I<U,D>",
            "class B extends A<C>",
            "class C",
            "class D",
            "init",
            "	let b = new B",
            "	b.bla(new D, new C)"
        );
    }

    @Test
    public void genericsSubstitute3() {
        testAssertOkLines(false,
            "package Test",
            "native testSuccess()",
            "interface I<S,T>",
            "	function bla(T t, S s)",
            "		skip",
            "interface J<T,S> extends I<S,T>",
            "	function foo()",
            "		skip",
            "class A<U> implements J<D,U>",
            "class B extends A<C>",
            "class C",
            "class D",
            "init",
            "	let b = new B",
            "	b.bla(new D, new C)"
        );
    }

    @Test
    public void genericsSubstitute() {
        testAssertOkLines(false,
            "package Test",
            "class A<T>",
            "	function bla(T a)",
            "class B extends A<MyType>",
            "	function do()",
            "		bla(new MyType)",
            "class MyType"

        );
    }

    @Test
    public void genericsSubstitute_override() {
        testAssertOkLines(false,
            "package Test",
            "class A<T>",
            "	function bla(T a)",
            "class B extends A<MyType>",
            "	override function bla(MyType t)",
            "		skip",
            "class MyType"

        );
    }


    @Test
    public void genericsSubstitute_override_interface() {
        testAssertOkLines(false,
            "package Test",
            "interface I<S,T>",
            "	function bla(S s, T t)",
            "interface J<T> extends I<int,T>",
            "	function foo(T t)",
            "class B implements J<MyType>",
            "	override function bla(int s, MyType t)",
            "		skip",
            "	override function foo(MyType t)",
            "class MyType"

        );
    }

    @Test
    public void genericsSubstitute_override_interface_fail() {
        testAssertErrorsLines(false, "Parameter int s should have type MyType to override function bla",
            "package Test",
            "interface I<T,S>",
            "	function bla(S s, T t)",
            "interface J<T> extends I<int,T>",
            "	function foo(T t)",
            "class B implements J<MyType>",
            "	override function bla(int s, MyType t)",
            "		skip",
            "	override function foo(MyType t)",
            "class MyType"

        );
    }


    @Test
    public void genericMethod1() {
        testAssertOkLines(false,
            "package Test",
            "class Blub<T>",
            "function bla<T>(Blub<T> t)",
            "init",
            "	bla(new Blub<int>)"
        );
    }

    @Test
    public void genericExtensionMethod1() {
        testAssertOkLines(false,
            "package Test",
            "class Blub<T>",
            "function Blub<T>.bla<T>()",
            "	skip",
            "init",
            "	new Blub<int>.bla()"
        );
    }

    @Test
    public void genericReturnOverride() {
        testAssertOkLines(false,
            "package Test",
            "interface I<T>",
            "	function f() returns T",
            "class C<T> implements I<T>",
            "	function f() returns T",
            "		return null"
        );
    }

    @Test
    public void genericReturnOverride2() {
        testAssertOkLines(false,
            "package Test",
            "interface I<S>",
            "	function f(S t) returns S",
            "class C<T> implements I<T>",
            "	function f(T t) returns T",
            "		return t"
        );
    }

    @Test
    public void genericRecursive() {
        testAssertOkLines(false,
            "package Test",
            "public class C<K>",
            "	C<K> x",
            "	function foo()",
            "		this.x.x = null"
        );
    }

    @Test
    public void genericRecursive2() {
        testAssertOkLines(false,
            "package Test",
            "public class C<K>",
            "	C<K> x",
            "	function foo()",
            "		C<K> c = new C<K>",
            "		c.x.x = null"
        );
    }

    @Test
    public void genericChain1() {
        testAssertOkLines(false,
            "package Test",
            "class A",
            "public class C<K>",
            "	K x",
            "init",
            "	C<C<C<A>>> c = null",
            "	c.x.x.x = new A"
        );
    }

    @Test
    public void genericChain1Err() {
        testAssertErrorsLines(false, "Cannot assign",
            "package Test",
            "class A",
            "public class C<K>",
            "	K x",
            "init",
            "	C<C<C<A>>> c = null",
            "	c.x.x = new A"
        );
    }

    @Test
    public void genericChain2() {
        testAssertOkLines(false,
            "package Test",
            "class A",
            "public class C<K>",
            "	C<C<K>> x",
            "init",
            "	C<A> c = null",
            "	c.x.x.x = new C<C<C<C<A>>>>"
        );
    }

    @Test
    public void genericChain2ErrA() {
        testAssertErrorsLines(false, "Cannot assign",
            "package Test",
            "class A",
            "public class C<K>",
            "	C<C<K>> x",
            "init",
            "	C<A> c = null",
            "	c.x.x.x = new C<C<C<C<C<A>>>>>"
        );
    }

    @Test
    public void genericChain2ErrB() {
        testAssertErrorsLines(false, "Cannot assign",
            "package Test",
            "class A",
            "public class C<K>",
            "	C<C<K>> x",
            "init",
            "	C<A> c = null",
            "	c.x.x.x = new C<C<C<A>>>"
        );
    }

    @Test
    public void implicitsWithClass() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function booleanToIndex(bool b) returns int",
            "	if b",
            "		return 1",
            "	return 0",
            "function booleanFromIndex(int i) returns bool",
            "	return i != 0",
            "interface Comparison<T>",
            "	function leq(T t, T u) returns bool",
            "class BoolComp implements Comparison<bool>",
            "	override function leq(bool a, bool b) returns bool",
            "		return not a or b",
            "Comparison<bool> bc = new BoolComp",
            "init",
            "	if bc.leq(false, true)",
            "		testSuccess()"
        );
    }

    @Test
    public void implicitsWithClass2() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function booleanToIndex(bool b) returns int",
            "	if b",
            "		return 1",
            "	return 0",
            "function booleanFromIndex(int i) returns bool",
            "	return i != 0",
            "class Comparison<T>",
            "	function leq(T t, T u) returns bool",
            "		return true",
            "class BoolComp extends Comparison<bool>",
            "	override function leq(bool a, bool b) returns bool",
            "		return not a or b",
            "Comparison<bool> bc = new BoolComp",
            "init",
            "	if bc.leq(false, true)",
            "		testSuccess()"
        );
    }

    @Test
    @Ignore // TODO fix this ugly hack and implement generics properly
    public void implicitsWithClass3() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function booleanToIndex(bool b) returns int",
            "	if b",
            "		return 1",
            "	return 0",
            "function booleanFromIndex(int i) returns bool",
            "	return i != 0",
            "class Comparison<T>",
            "	function leq(T t, T u) returns bool",
            "		return true",
            "class Comparison2<U> extends Comparison<U>",
            "class BoolComp extends Comparison2<bool>",
            "	override function leq(bool a, bool b) returns bool",
            "		return not a or b",
            "Comparison2<bool> bc = new BoolComp",
            "init",
            "	if bc.leq(false, true)",
            "		testSuccess()"
        );
    }

    @Test
    public void implicitsWithClosures() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function booleanToIndex(bool b) returns int",
            "	if b",
            "		return 1",
            "	return 0",
            "function booleanFromIndex(int i) returns bool",
            "	return i != 0",
            "interface Comparison<T>",
            "	function leq(T t, T u) returns bool",
            "Comparison<bool> bc = (bool a, bool b) -> not a or b",
            "init",
            "	if bc.leq(false, true)",
            "		testSuccess()"
        );
    }


    @Test
    public void genericForIn() {
        test().executeProg(true)
            .testLua(true).luaOnly(false)
            .lines(
                "package test",
                "native testSuccess()",
                "class C<T>",
                "	function iterator() returns Iterator<T>",
                "		return new Iterator<T>()",
                "class Iterator<T>",
                "	private int i = 0",
                "	function next() returns T",
                "		i = i + 1",
                "		return i castTo T",
                "	function hasNext() returns boolean",
                "		return i < 10",
                "	function close()",
                "		destroy this",
                "init",
                "	let c = new C<int>",
                "	for i in c",
                "		if i == 5",
                "			testSuccess()"
            );
    }

    @Test
    public void genericForFrom() {
        test().executeProg(true)
            .testLua(true).luaOnly(false)
            .lines(
                "package test",
                "native testSuccess()",
                "class C<T>",
                "	function iterator() returns Iterator<T>",
                "		return new Iterator<T>()",
                "class Iterator<T>",
                "	private int i = 0",
                "	function next() returns T",
                "		i = i + 1",
                "		return i castTo T",
                "	function hasNext() returns boolean",
                "		return i < 10",
                "init",
                "	let c = new C<int>",
                "	let iter = c.iterator()",
                "	for i from iter",
                "		if i == 5",
                "			testSuccess()"
            );
    }

    @Test
    public void genericOverload() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class C<T>",
            "	private T x",
            "	construct(T x)",
            "		this.x = x",
            "	function foo(T t)",
            "		foo(new C(t))",
            "	function foo(C<T> t)",
            "		testSuccess()",
            "init",
            "	let c = new C(1)",
            "	c.foo(1)"
        );
    }

    @Test
    public void genericOverload2() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class C<T>",
            "	private T x",
            "	construct(T x)",
            "		this.x = x",
            "	function foo(T t)",
            "		foo(new C(t))",
            "	function foo(C<T> t)",
            "		testSuccess()",
            "	function test()",
            "		let c = new C(1)",
            "		c.foo(1)",
            "init",
            "	new C(1).test()"
        );
    }

    @Test
    public void inferType() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function id(int x) returns int",
            "	return x",
            "class C<T>",
            "	var x = id(4)",
            "init",
            "	let x= new C<int>",
            "	if x.x == 4",
            "		testSuccess()"
        );
    }

    @Test
    public void genericFunctionOverload() { // #628
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "class LinkedList<T>",
            "	T x",
            "function stringToIndex(string b) returns int",
            "	return 0",
            "function stringFromIndex(int i) returns string",
            "	return \"\"",
            "public function LinkedList<string>.foo(string separator) returns string",
            "	this.foo<string>(s -> s, separator) // Doesn't work",
            "	this.foo2<string>(s -> s, separator) // Works",
            "	return separator",
            "interface ToStringClosure<A>",
            "	function apply(A a) returns A",
            "public function LinkedList<T>.foo<T>(ToStringClosure<T> cls, string separator)",
            "public function LinkedList<T>.foo2<T>(ToStringClosure<T> cls, string separator)",
            "init",
            "	let x = new LinkedList<string>",
            "	x.foo(\"a\")"
        );
    }

    @Test
    public void genericToIndexFieldAssignment() { // #647
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class C<T>",
            "    T x",
            "function stringToIndex(string s) returns int",
            "    return 42",
            "function stringFromIndex(int i) returns string",
            "    return \"42\"",
            "init",
            "    C<string> c = new C<string>",
            "    c.x = \"42\"",
            "    if c.x == \"42\"",
            "        testSuccess()"
        );
    }

    @Test
    public void extensionFunc() { // #718
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "function stringToIndex(string b) returns int",
            "	return 0",
            "function stringFromIndex(int i) returns string",
            "	return \"\"",
            "public function T.foo<T>() returns T",
            "	return this",
            "init",
            "	let x = \"hello\".foo()",
            "	if x == \"hello\"",
            "		testSuccess()"
        );
    }

    @Test
    public void strangeFoldl() { // #655
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "class LinkedList<T>",
            "	T x",
            "	function foldl<Q, T>(Q startValue, FoldClosure<T, Q> predicate) returns Q",
            "		return null",
            "interface FoldClosure<T, Q>",
            "	function run(T t, Q q) returns Q",
            "init",
            "	let x = new LinkedList<int>",
            "	x.foldl<int,int>(0, (x, y) -> x + y)"
        );
    }

    @Test
    public void normalFoldlInfer() { // #657
        test().executeProg(true).testLua(true).luaOnly(false).lines(
            "package test",
            "native testSuccess()",
            "@extern native I2S(int i) returns string",
            "string array s",
            "int s_max = -1",
            "function stringToIndex(string b) returns int",
            "	s_max++",
            "	s[s_max] = b",
            "	return s_max",
            "function stringFromIndex(int i) returns string",
            "	return s[i]",
            "class LinkedList<T>",
            "	T x",
            "	function foldl<Q>(Q startValue, FoldClosure<T, Q> predicate) returns Q",
            "		return predicate.run(x, startValue)",
            "	function toString() returns string",
            "		let fold = foldl(\"[\", (i, q) -> q + I2S(i castTo int) + \",\")",
            "		return fold + \"]\"",
            "interface FoldClosure<T, Q>",
            "	function run(T t, Q q) returns Q",
            "init",
            "	let x = new LinkedList<int>",
            "	x.x = 5",
            "	if x.toString() == \"[5,]\"",
            "		testSuccess()"
        );
    }

    @Test
    public void inheritField() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "abstract class A<T>",
            "	int someInt",
            "class B extends A<int>",
            "	construct()",
            "		someInt = 1",
            "init",
            "	if new B().someInt == 1",
            "		testSuccess()"
        );
    }

    @Test
    public void inheritField2() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "abstract class A<T>",
            "	int someInt",
            "class B<K> extends A<K>",
            "	construct()",
            "		someInt = 1",
            "init",
            "	if new B<int>().someInt == 1",
            "		testSuccess()"
        );
    }


    @Test
    public void inheritMethod() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "abstract class A<T>",
            "	function someInt()",
            "		testSuccess()",
            "class B extends A<int>",
            "	construct()",
            "		someInt()",
            "init",
            "	new B()"
        );
    }

    @Test
    public void nullWithGeneric() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "function foo<T>(T t)",
            "	if t == null",
            "		testSuccess()",
            "init",
            "	foo(null)"
        );
    }

    @Test
    public void abstractReturnT() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "abstract class F<T>",
            "	abstract function get() returns T",
            "class X<T> extends F<T>",
            "	T t",
            "	construct(T t)",
            "		this.t = t",
            "	override function get() returns T",
            "		return t",
            "init",
            "	F<int> x = new X(42)",
            "	if x.get() == 42",
            "		testSuccess()"
        );
    }

    @Test
    public void missingTypeArgsFunc() {
        testAssertErrorsLines(false, "Cannot infer type for type parameter T",
            "package test",
            "function foo<T>() returns T",
            "	return null",
            "init",
            "	let x = foo()"
        );
    }

    @Test
    public void missingTypeArgsMethod() {
        testAssertErrorsLines(false, "Cannot infer type for type parameter T",
            "package test",
            "class C",
            "	function foo<T>() returns T",
            "		return null",
            "init",
            "	let c = new C",
            "	let x = c.foo()"
        );
    }

    @Test
    public void missingTypeArgsConstructor() {
        testAssertErrorsLines(false, "Cannot infer type for type parameter T",
            "package test",
            "class C<T>",
            "init",
            "	let c = new C"
        );
    }


    @Test
    public void tooManyTypeArgsFunc() {
        testAssertErrorsLines(false, "Too many type arguments given",
            "package test",
            "function foo<T>() returns T",
            "	return null",
            "init",
            "	let x = foo<int, int>()"
        );
    }

    @Test
    public void tooManyTypeArgsMethod() {
        testAssertErrorsLines(false, "Too many type arguments given",
            "package test",
            "class C",
            "	function foo<T>() returns T",
            "		return null",
            "init",
            "	let c = new C",
            "	let x = c.foo<int, int>()"
        );
    }

    @Test
    public void tooManyTypeArgsConstructor() {
        testAssertErrorsLines(false, "Too many type arguments given",
            "package test",
            "class C<T>",
            "init",
            "	let c = new C<int, int>"
        );
    }


    @Test
    public void nestedList() {
        testAssertOkLines(true,
            "package Test",
            "native testSuccess()",
            "public class LinkedList<T>",
            "    T t",
            "    function add (T t)",
            "        this.t = t",
            "    function getFirst() returns T",
            "        return t",
            "public function asList<T>(vararg T ts) returns LinkedList<T>",
        	"    let ll = new LinkedList<T>",
            "    for t in ts",
            "    	ll.add(t)",
            "    return ll",
            "let x = asList(asList(42)).getFirst()",
            "init",
            "    if x.getFirst() == 42",
            "        testSuccess()"
        );
    }


    @Test
    public void nestedList2() {
        testAssertOkLinesWithStdLib(true,
            "package Test",
            "import LinkedList",
            "let x = asList(asList(42)).getFirst().getFirst()",
            "init",
            "    if x == 42",
            "        testSuccess()"
        );
    }

    @Test
    public void conflictingTypeArgsMemberMethod() {
        // Regression test: calling a generic method via dot notation with conflicting type arguments
        // used to cause a NullPointerException instead of a proper type error.
        // The two arguments infer conflicting types for T (int vs string), which should produce
        // a "Wrong parameter type" compile error, not a compiler crash.
        testAssertErrorsLines(false, "Wrong parameter type",
            "package test",
            "class C",
            "    function combine<T>(T x, T y) returns T",
            "        return x",
            "init",
            "    let c = new C()",
            "    c.combine(1, \"hello\")"
        );
    }

    /** An instanceof of a field of a generic class, against the class's own type parameter. */
    @Test
    public void instanceofOfFieldAgainstTypeParameter() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Marker",
            "class Box<T:> implements Marker",
            "    T item",
            "class G<T:>",
            "    Marker held = null",
            "    function r() returns boolean",
            "        return held instanceof Box<T>",
            "init",
            "    let ints = new G<int>()",
            "    ints.held = new Box<int>()",
            "    let strings = new G<string>()",
            "    strings.held = new Box<int>()",
            "    if ints.r() and not strings.r()",
            "        testSuccess()"
        );
    }

    /** A local captured by a closure becomes a field of the closure's class, so the same holds there. */
    @Test
    public void instanceofOfCapturedLocalAgainstTypeParameter() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Marker",
            "class Box<T:> implements Marker",
            "    T item",
            "interface Pred",
            "    function test() returns boolean",
            "class G<T:>",
            "    function pred(Marker m) returns Pred",
            "        return () -> m instanceof Box<T>",
            "init",
            "    let ints = new G<int>()",
            "    let strings = new G<string>()",
            "    let b = new Box<int>()",
            "    if ints.pred(b).test() and not strings.pred(b).test()",
            "        testSuccess()"
        );
    }

    /** The type id of a field of a generic class, read inside the class. */
    @Test
    public void typeIdOfFieldInGenericClass() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Marker",
            "class Box<T:> implements Marker",
            "    T item",
            "class G<T:>",
            "    Marker held = null",
            "    function tid() returns int",
            "        return held.typeId",
            "init",
            "    let g = new G<int>()",
            "    let b = new Box<int>()",
            "    g.held = b",
            "    if g.tid() == b.typeId",
            "        testSuccess()"
        );
    }

    /**
     * Compile-time code reads a type id before the generics are eliminated, which then adds the classes of the
     * instantiations: their type ids must not come from what compile time saw.
     */
    @Test
    public void typeIdReadAtCompileTimeBeforeInstantiationsExist() {
        test().testLua(true).luaOnly(false).runCompiletimeFunctions(true).executeProg().executeProgOnlyAfterTransforms().lines(
            "package Test",
            "native testSuccess()",
            "interface Marker",
            "class Box<T:> implements Marker",
            "    T item",
            "class Plain implements Marker",
            "function compiletime(int i) returns int",
            "    return i",
            "constant plainId = compiletime(new Plain().typeId)",
            "class G<T:>",
            "    Marker held = null",
            "    function tid() returns int",
            "        return held.typeId",
            "init",
            "    let g = new G<int>()",
            "    let b = new Box<int>()",
            "    g.held = b",
            "    if g.tid() == b.typeId and plainId != 0",
            "        testSuccess()"
        );
    }

    /**
     * A type id compile-time code stored stays the class's type id at run time, although the generics add the
     * classes of instantiations whose names sort before it.
     */
    @Test
    public void typeIdStoredAtCompileTimeMatchesTheRuntimeTypeId() {
        test().testLua(true).luaOnly(false).runCompiletimeFunctions(true).executeProg().executeProgOnlyAfterTransforms().lines(
            "package Test",
            "native testSuccess()",
            "interface Marker",
            "class Box<T:> implements Marker",
            "    T item",
            "class Plain implements Marker",
            "function compiletime(int i) returns int",
            "    return i",
            "constant plainId = compiletime(new Plain().typeId)",
            "init",
            "    let a = new Box<int>()",
            "    let b = new Box<string>()",
            "    if new Plain().typeId == plainId and a != null and b != null",
            "        testSuccess()"
        );
    }

    /** The same when a class which sorts before it is used by nothing, which Lua drops from the script. */
    @Test
    public void typeIdStoredAtCompileTimeSurvivesAnUnusedClassBeforeIt() {
        test().testLua(true).luaOnly(false).runCompiletimeFunctions(true).executeProg().executeProgOnlyAfterTransforms().lines(
            "package Test",
            "native testSuccess()",
            "class Aunused",
            "class Zused",
            "function compiletime(int i) returns int",
            "    return i",
            "constant zid = compiletime(new Zused().typeId)",
            "init",
            "    if new Zused().typeId == zid",
            "        testSuccess()"
        );
    }

    /**
     * Compile-time code sees one class for every instantiation of a new generic class, which Jass makes a class of
     * its own after compile time. So a type id of an instantiation read then can match nothing Jass makes: an error.
     */
    @Test
    public void typeIdOfGenericInstantiationReadAtCompileTimeIsAnErrorOnJass() {
        test().runCompiletimeFunctions(true).executeProg(false)
            .expectError("compile-time code read the type id of Box").lines(
            "package Test",
            "native testSuccess()",
            "class Box<T:>",
            "    T item",
            "function compiletime(int i) returns int",
            "    return i",
            "constant boxId = compiletime(new Box<int>().typeId)",
            "init",
            "    if new Box<int>().typeId == boxId",
            "        testSuccess()"
        );
    }

    /** Lua keeps one class for the instantiations, as compile time does, so the id stays the class's. */
    @Test
    public void typeIdOfGenericInstantiationReadAtCompileTimeMatchesOnLua() {
        test().testLua(true).runCompiletimeFunctions(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Box<T:>",
            "    T item",
            "function compiletime(int i) returns int",
            "    return i",
            "constant boxId = compiletime(new Box<int>().typeId)",
            "init",
            "    if new Box<int>().typeId == boxId",
            "        testSuccess()"
        );
    }

    /** A default method of a bounded interface dispatches its bound by each implementing class's type argument. */
    @Test
    public void interfaceDefaultMethodWithBoundInheritedByConcreteClasses() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Kc<T:>",
            "    function kkey(T v) returns int",
            "implements Kc<int>",
            "    function kkey(int v) returns int",
            "        return v + 100",
            "implements Kc<string>",
            "    function kkey(string v) returns int",
            "        return 200",
            "interface Shower<T: Kc>",
            "    function val() returns T",
            "    function show() returns int",
            "        return T.kkey(val())",
            "class IntShower implements Shower<int>",
            "    function val() returns int",
            "        return 5",
            "class StrShower implements Shower<string>",
            "    function val() returns string",
            "        return \"a\"",
            "init",
            "    Shower<int> s = new IntShower()",
            "    Shower<string> t = new StrShower()",
            "    if s.show() == 105 and t.show() == 200",
            "        testSuccess()"
        );
    }

    /**
     * A default method constructing a class with statics reaches the statics of each implementing class's
     * instantiation. The interpreter runs the translated program only: on untranslated IM a method a class inherits
     * from a concrete instantiation reaches one static for all, a superclass's as well as an interface's.
     */
    @Test
    public void interfaceDefaultMethodUsingStaticsPerInstantiation() {
        test().testLua(true).luaOnly(false).executeProgOnlyAfterTransforms().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Counter<T:>",
            "    static int made = 0",
            "    construct()",
            "        made++",
            "    function count() returns int",
            "        return made",
            "interface Maker<T:>",
            "    function bump() returns int",
            "        return new Counter<T>().count()",
            "class IntMaker implements Maker<int>",
            "class StrMaker implements Maker<string>",
            "init",
            "    Maker<int> a = new IntMaker()",
            "    Maker<string> b = new StrMaker()",
            "    let a1 = a.bump()",
            "    let a2 = a.bump()",
            "    let b1 = b.bump()",
            "    if a1 == 1 and a2 == 2 and b1 == 1",
            "        testSuccess()"
        );
    }

    /**
     * A default method's instanceof of an instantiation of its type parameter tells the instantiations apart. The
     * interpreter runs the translated program only: on untranslated IM the inherited method does not know its
     * instantiation.
     */
    @Test
    public void interfaceDefaultMethodWithInstanceofOfItsTypeParameter() {
        test().testLua(true).luaOnly(false).executeProgOnlyAfterTransforms().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Marker",
            "class Box<T:> implements Marker",
            "interface Checker<T:>",
            "    function owns(Marker m) returns boolean",
            "        return m instanceof Box<T>",
            "class IntChecker implements Checker<int>",
            "class StrChecker implements Checker<string>",
            "init",
            "    Checker<int> c = new IntChecker()",
            "    Checker<string> d = new StrChecker()",
            "    Marker i = new Box<int>()",
            "    Marker s = new Box<string>()",
            "    if c.owns(i) and not c.owns(s) and d.owns(s) and not d.owns(i)",
            "        testSuccess()"
        );
    }

    /**
     * One implementing class overrides the bounded default and another inherits it; each runs its own. On Lua every
     * class binds the slot the call site dispatches to its own override or to the default specialised for int.
     */
    @Test
    public void interfaceDefaultMethodWithBoundOverriddenInOneImplementingClass() throws IOException {
        String name = "interfaceDefaultMethodWithBoundOverriddenInOneImplementingClass";
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(overriddenBoundedDefaultSource());
        String lua = compiledLua(name);
        Matcher call = Pattern.compile("__wurst_objectClass\\[a\\]\\.(\\w+)\\(a\\)").matcher(lua);
        assertTrue(call.find(), lua);
        String slot = call.group(1);
        for (String inherits : new String[] {"Inherits", "AlsoInherits"}) {
            assertTrue(lua.contains("\n\t" + inherits + "." + slot + " = Shower_specialized_integer_Shower_show\n"),
                inherits + " must bind " + slot + " to the default specialised for int:\n" + lua);
        }
        for (String overrides : new String[] {"Overrides", "AlsoOverrides"}) {
            assertTrue(lua.contains("\n\t" + overrides + "." + slot + " = " + overrides + "_" + overrides + "_show\n"),
                overrides + " must bind " + slot + " to its own override:\n" + lua);
        }
        assertFalse(lua.contains("= Shower_Shower_show"), lua);
    }

    /** The source of {@link #interfaceDefaultMethodWithBoundOverriddenInOneImplementingClass}. */
    static String[] overriddenBoundedDefaultSource() {
        return new String[] {
            "package Test",
            "native testSuccess()",
            "interface Kc<T:>",
            "    function kkey(T v) returns int",
            "implements Kc<int>",
            "    function kkey(int v) returns int",
            "        return v + 100",
            "interface Shower<T: Kc>",
            "    function val() returns T",
            "    function show() returns int",
            "        return T.kkey(val())",
            "class Inherits implements Shower<int>",
            "    function val() returns int",
            "        return 5",
            "class Overrides implements Shower<int>",
            "    function val() returns int",
            "        return 6",
            "    override function show() returns int",
            "        return 7",
            "class AlsoInherits implements Shower<int>",
            "    function val() returns int",
            "        return 8",
            "class AlsoOverrides implements Shower<int>",
            "    function val() returns int",
            "        return 9",
            "    override function show() returns int",
            "        return 10",
            "init",
            "    Shower<int> a = new Inherits()",
            "    Shower<int> b = new Overrides()",
            "    Shower<int> c = new AlsoInherits()",
            "    Shower<int> d = new AlsoOverrides()",
            "    if a.show() == 105 and b.show() == 7 and c.show() == 108 and d.show() == 10",
            "        testSuccess()"
        };
    }

    /** A bounded default inherited through an interface which extends the generic interface, generic or concrete. */
    @Test
    public void interfaceDefaultMethodWithBoundInheritedThroughAnExtendingInterface() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Kc<T:>",
            "    function kkey(T v) returns int",
            "implements Kc<int>",
            "    function kkey(int v) returns int",
            "        return v + 100",
            "implements Kc<string>",
            "    function kkey(string v) returns int",
            "        return 200",
            "interface Shower<T: Kc>",
            "    function val() returns T",
            "    function show() returns int",
            "        return T.kkey(val())",
            "interface MidShower<T: Kc> extends Shower<T>",
            "interface IntMidShower extends Shower<int>",
            "class ThroughGeneric implements MidShower<string>",
            "    function val() returns string",
            "        return \"a\"",
            "class ThroughConcrete implements IntMidShower",
            "    function val() returns int",
            "        return 5",
            "init",
            "    Shower<string> s = new ThroughGeneric()",
            "    MidShower<string> m = new ThroughGeneric()",
            "    Shower<int> t = new ThroughConcrete()",
            "    IntMidShower u = new ThroughConcrete()",
            "    if s.show() == 200 and m.show() == 200 and t.show() == 105 and u.show() == 105",
            "        testSuccess()"
        );
    }

    /** A generic class implementing the bounded interface with its own type parameter runs the default per instantiation. */
    @Test
    public void interfaceDefaultMethodWithBoundInheritedByGenericClass() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Kc<T:>",
            "    function kkey(T v) returns int",
            "implements Kc<int>",
            "    function kkey(int v) returns int",
            "        return v + 100",
            "implements Kc<string>",
            "    function kkey(string v) returns int",
            "        return 200",
            "interface Shower<T: Kc>",
            "    function val() returns T",
            "    function show() returns int",
            "        return T.kkey(val())",
            "class G<T: Kc> implements Shower<T>",
            "    T v",
            "    construct(T v)",
            "        this.v = v",
            "    function val() returns T",
            "        return v",
            "init",
            "    Shower<int> s = new G<int>(5)",
            "    Shower<string> t = new G<string>(\"a\")",
            "    let g = new G<int>(7)",
            "    if s.show() == 105 and t.show() == 200 and g.show() == 107",
            "        testSuccess()"
        );
    }

    /**
     * A call through a non-generic interface, which names no instantiation, reaches the bounded default of each
     * implementing class's instantiation. The interpreter runs the translated program only: on untranslated IM such a
     * call leaves the bound unknown.
     */
    @Test
    public void interfaceDefaultMethodWithBoundReachedThroughNonGenericInterface() {
        test().testLua(true).luaOnly(false).executeProgOnlyAfterTransforms().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "interface Kc<T:>",
            "    function kkey(T v) returns int",
            "implements Kc<int>",
            "    function kkey(int v) returns int",
            "        return v + 100",
            "implements Kc<string>",
            "    function kkey(string v) returns int",
            "        return 200",
            "interface Showable",
            "    function show() returns int",
            "interface Shower<T: Kc>",
            "    function val() returns T",
            "    function shown() returns int",
            "        return T.kkey(val())",
            "class IntShower implements Shower<int>, Showable",
            "    function val() returns int",
            "        return 5",
            "    function show() returns int",
            "        return shown()",
            "class StrShower implements Shower<string>, Showable",
            "    function val() returns string",
            "        return \"a\"",
            "    function show() returns int",
            "        return shown()",
            "init",
            "    Showable s = new IntShower()",
            "    Showable t = new StrShower()",
            "    if s.show() == 105 and t.show() == 200",
            "        testSuccess()"
        );
    }

}
