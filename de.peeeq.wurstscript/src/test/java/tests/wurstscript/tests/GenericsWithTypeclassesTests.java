package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;
import static tests.wurstscript.tests.BugTests.TEST_DIR;

public class GenericsWithTypeclassesTests extends WurstScriptTest {


    @Test
    public void identity() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function identity<A:>(A a) returns A",
                "		return a",
                "	init",
                "		int x = identity(3)",
                "		string s = identity(\"a\")",
                "		if x == 3 and s == \"a\"",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void identityTrans() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function identity1<A:>(A a) returns A",
                "		return a",
                "	function identity2<B:>(B a) returns B",
                "		return identity1(a)",
                "	function identity3<C:>(C a) returns C",
                "		return identity2(a)",
                "	init",
                "		int x = identity3(3)",
                "		string s = identity3(\"a\")",
                "		if x == 3 and s == \"a\"",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void identityRec() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function identity<A:>(int i, A a) returns A",
                "		if i > 0",
                "			return identity(i - 1, a)",
                "		return a",
                "	init",
                "		int x = identity(5, 3)",
                "		string s = identity(5, \"a\")",
                "		if x == 3 and s == \"a\"",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    @Ignore // TODO
    public void identityRecTypeCreation() {
        testAssertErrorsLines(true, "some error message",
                "package test",
                "	native testSuccess()",
                "	class C<T>",
                "		construct(T t)",
                "	function blub<A:>(int i, A a) returns int",
                "		if i <= 0",
                "			return 0",
                "		return 1 + blub<C<A>>(i-1, new C(a))",
                "	init",
                "		int x = blub(5, 3)",
                "		if x == 5",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void identityRecMut() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function identity1<A:>(int i, A a) returns A",
                "		if i > 0",
                "			return identity2(i - 1, a)",
                "		return a",
                "	function identity2<A:>(int i, A a) returns A",
                "		if i > 0",
                "			return identity1(i - 1, a)",
                "		return a",
                "	init",
                "		int x = identity1(5, 3)",
                "		string s = identity1(5, \"a\")",
                "		if x == 3 and s == \"a\"",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void extensionFunc() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function boolean.choice<A:>(A x, A y) returns A",
                "		if this",
                "			return x",
                "		return y",
                "	init",
                "		int x = true.choice(5, 3)",
                "		string s = false.choice(\"a\", \"b\")",
                "		if x == 5 and s == \"b\"",
                "			testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void extensionFuncReceiver() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function A.choice<A:>(boolean b, A y) returns A",
                "		if b",
                "			return this",
                "		return y",
                "	init",
                "		int x = (5).choice(true, 3)",
                "		string s = \"a\".choice(false, \"b\")",
                "		if x == 5 and s == \"b\"",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void genericsDispatch() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "class Cell<T:>",
                "	T o",
                "init",
                "	Cell<int> x = new Cell<int>()",
                "	Cell<string> y = new Cell<string>()",
                "	x.o = 3",
                "	y.o = \"a\"",
                "	if x.o == 3 and y.o == \"a\"",
                "		testSuccess()"
        );
    }





    @Test
    public void identity2() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class C",
                "	function identity<A:>(A a) returns A",
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
                "	class List<T:>",
                "		function iterator() returns Iterator<T>",
                "			return new Iterator<T>(this)",
                "	class Iterator<S:>",
                "		S t",
                "		construct(List<S> t)",
                "			int x = 1",
                "		function hasNext() returns boolean",
                "			return true",
                "		function next() returns S",
                "			return t",
                "		function close()",
                "			skip",
                "	class A",
                "	class B",
                "	class C",
                "	init",
                "		List<B> a = new List<B>()",
				"		for B b in a",
//                "		Iterator<B> iterator = a.iterator()",
//                "		while iterator.hasNext()",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void testSubtypeGenericClass() {
        testAssertOkLines(false,
                "package test",
                "	class A<T:>",
                "	class B<S:> extends A<S>",
                "	init",
                "		A<int> x = new B<int>",
                "endpackage"
        );
    }

    @Test
    public void testSubtypeGenericClass2() {
        testAssertOkLines(false,
                "package test",
                "	class A<T:>",
                "	class B<S:> extends A<S>",
                "	function foo<X:>()",
                "		A<X> x = new B<X>",
                "endpackage"
        );
    }

    @Test
    public void testSubtypeGenericInterface() {
        testAssertOkLines(false,
                "package test",
                "	interface I<T:>",
                "	class B<S:> implements I<S>",
                "	init",
                "		I<int> x = new B<int>",
                "endpackage"
        );
    }

    @Test
    public void identityFail1() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	function identity<A:>(A a) returns A",
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
                "	function identity<A:>(A a) returns A",
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
                "	class Cell<T:>",
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
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class Cell<T:>",
                "		T elem",
                "		function set(T t)",
                "			elem = t",
                "		function get() returns T",
                "			return elem",
                "",
                "	tuple bla(int z, int y)",
                "	init",
                "		Cell<bla> c = new Cell<bla>()",
                "		c.set(bla(5, 3))",
                "		if c.get() == bla(5, 3)",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void implicitConversions2() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class Cell<T:>",
                "		T elem",
                "		function set(T t)",
                "			elem = t",
                "		function get() returns T",
                "			return elem",
                "",
                "	tuple bla(int z, int y)",
                "	init",
                "		Cell<bla> c = new Cell<bla>()",
                "		c.set(bla(5, 3))",
                "		c.set(c.get())",
                "		if c.get() == bla(5, 3)",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void implicitConversions3() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	public interface FoldClosure<T:, Q:>",
                "		function apply(T t, Q q) returns Q",
                "	class Cell<T:>",
                "		T elem",
                "		function set(T t)",
                "			elem = t",
                "		function get() returns T",
                "			return elem",
                "		function fold<Q:>(Q start, FoldClosure<T,Q> f) returns Q",
                "			return f.apply(elem, start)",
                "",
                "	tuple bla(int z, int y)",
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
                "interface TFunc<T:>",
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
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "@extern native R2I(real r) returns int",
                "@extern native R2S(real r) returns string",
                "native println(string s)",
                "interface F<A:,R:>",
                "	function apply(A a) returns R",
                "class Cell<T:>",
                "	T elem",
                "	construct(T t)",
                "		this.elem = t",
                "	function get() returns T",
                "		return elem",
                "	function map<R:>(F<T,R> f) returns Cell<R>",
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

    @Test
    public void implicitConversionsFail() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class Cell<T:>",
                "		T elem",
                "		function set(T t)",
                "			elem = t",
                "		function get() returns T",
                "			return elem",
                "",
                "	tuple bla(int z, int y)",
                "	init",
                "		Cell<bla> c = new Cell<bla>()",
                "		c.set(bla(3,4))",
                "		if c.get() == bla(3,4)",
                "			testSuccess()",
                "endpackage"
        );
    }


    @Test
    public void implicitConversionsAssign() {
        testAssertOkLines(false,
                "type unit extends handle",
                "package test",
                "	native testSuccess()",
                "	class Cell<T:>",
                "		T elem",
                "		function set(T t)",
                "			elem = t",
                "		function get() returns T",
                "			return elem",
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
                "class L<T:>",
                "init",
                "	L<effect> l = new L<effect>()"
        );
    }



    @Test
    public void implicitConversionFailSimple() { // see bug #121
        testAssertErrorsLines(false, "cast expression not defined for expression type int",
                "type effect extends handle",
                "package Test",

                "class List<T:>",
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
                "class Cell<T:>",
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
    public void genericsDispatch2() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "class Cell<T:>",
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
                "class A<T:>",
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
                "interface I<S:,T:>",
                "	function bla(T t, S s)",
                "		skip",
                "class A<U:> implements I<U,D>",
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
                "interface I<S:,T:>",
                "	function bla(T t, S s)",
                "		skip",
                "interface J<T:,S:> extends I<S,T>",
                "	function foo()",
                "		skip",
                "class A<U:> implements J<D,U>",
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
                "class A<T:>",
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
                "class A<T:>",
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
                "interface I<S:,T:>",
                "	function bla(S s, T t)",
                "interface J<T:> extends I<int,T>",
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
                "interface I<T:,S:>",
                "	function bla(S s, T t)",
                "interface J<T:> extends I<int,T>",
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
                "class Blub<T:>",
                "function bla<T:>(Blub<T> t)",
                "init",
                "	bla(new Blub<int>)"
        );
    }

    @Test
    public void genericExtensionMethod1() {
        testAssertOkLines(false,
                "package Test",
                "class Blub<T:>",
                "function Blub<T>.bla<T:>()",
                "	skip",
                "init",
                "	new Blub<int>.bla()"
        );
    }

    @Test
    public void genericReturnOverride() {
        testAssertOkLines(false,
                "package Test",
                "interface I<T:>",
                "	function f() returns T",
                "class C<T:> implements I<T>",
                "	function f() returns T",
                "		return null"
        );
    }

    @Test
    public void genericReturnOverride2() {
        testAssertOkLines(false,
                "package Test",
                "interface I<S:>",
                "	function f(S t) returns S",
                "class C<T:> implements I<T>",
                "	function f(T t) returns T",
                "		return t"
        );
    }

    @Test
    public void genericRecursive() {
        testAssertOkLines(false,
                "package Test",
                "public class C<K:>",
                "	C<K> x",
                "	function foo()",
                "		this.x.x = null"
        );
    }

    @Test
    public void genericRecursive2() {
        testAssertOkLines(false,
                "package Test",
                "public class C<K:>",
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
                "public class C<K:>",
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
                "public class C<K:>",
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
                "public class C<K:>",
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
                "public class C<K:>",
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
                "public class C<K:>",
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
                "interface Comparison<T:>",
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
                "class Comparison<T:>",
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
    public void implicitsWithClass3() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class Comparison<T:>",
                "	function leq(T t, T u) returns bool",
                "		return true",
                "class ComparisonX<U:> extends Comparison<U>",
                "class BoolComp extends ComparisonX<bool>",
                "	override function leq(bool a, bool b) returns bool",
                "		return not a or b",
                "ComparisonX<bool> bc = new BoolComp",
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
                "interface Comparison<T:>",
                "	function leq(T t, T u) returns bool",
                "Comparison<bool> bc = (bool a, bool b) -> not a or b",
                "init",
                "	if bc.leq(false, true)",
                "		testSuccess()"
        );
    }


    @Test
    public void genericForIn() {
        testAssertErrorsLines(true, "cast expression not defined for expression type int",
                "package test",
                "native testSuccess()",
                "class C<T:>",
                "	function iterator() returns Iterator<T>",
                "		return new Iterator<T>()",
                "class Iterator<T:>",
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
    @Ignore
    public void genericForFrom() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class C<T:>",
                "	function iterator() returns Iterator<T>",
                "		return new Iterator<T>()",
                "class Iterator<T:>",
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
                "class C<T:>",
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
                "class C<T:>",
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
                "class C<T:>",
                "	var x = id(4)",
                "init",
                "	let x= new C<int>",
                "	if x.x == 4",
                "		testSuccess()"
        );
    }

    @Test
    public void simpleFunctionCall() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class C<T:>",
                "	function foo() returns int",
                "		return 4",
                "init",
                "	let x = new C<int>",
                "	if x.foo() == 4",
                "		testSuccess()"
        );
    }

    @Test
    public void simpleFunctionCall2() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class C<T:>",
                "	function foo() returns int",
                "		return bar()",
                "	function bar() returns int",
                "		return 4",
                "init",
                "	let x = new C<int>",
                "	if x.foo() == 4",
                "		testSuccess()"
        );
    }

    @Test
    public void genericFunctionOverload() { // #628
        testAssertOkLines(false,
                "package test",
                "native testSuccess()",
                "class LinkedList<T:>",
                "	T x",
                "public function LinkedList<string>.foo(string separator) returns string",
                "	this.foo<string>(s -> s, separator) // Doesn't work",
                "	this.foo2<string>(s -> s, separator) // Works",
                "	return separator",
                "interface ToStringClosure<A:>",
                "	function apply(A a) returns A",
                "public function LinkedList<T>.foo<T:>(ToStringClosure<T> cls, string separator)",
                "public function LinkedList<T>.foo2<T:>(ToStringClosure<T> cls, string separator)",
                "init",
                "	let x = new LinkedList<string>",
                "	x.foo(\"a\")"
        );
    }

    @Test
    public void extensionFunc2() { // #718
        testAssertOkLines(false,
                "package test",
                "native testSuccess()",
                "public function T.foo<T:>() returns T",
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
                "class LinkedList<T:>",
                "	T x",
                "	function foldl<Q:, S:>(Q startValue, FoldClosure<S, Q> predicate) returns Q",
                "		return startValue",
                "interface FoldClosure<X:, Y:>",
                "	function run(X t, Y q) returns Y",
                "init",
                "	let x = new LinkedList<int>",
                "	x.foldl<int,int>(0, (x, y) -> x + y)"
        );
    }

    @Test
    @Ignore
    public void normalFoldlInfer() { // #657
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "@extern native I2S(int i) returns string",
                "string array s",
                "int s_max = -1",
                "class LinkedList<T:>",
                "	T x",
                "	function foldl<Q:>(Q startValue, FoldClosure<T, Q> predicate) returns Q",
                "		return predicate.run(x, startValue)",
                "	function toString() returns string",
                "		let fold = foldl(\"[\", (i, q) -> q + I2S(i castTo int) + \",\")",
                "		return fold + \"]\"",
                "interface FoldClosure<T:, Q:>",
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
                "abstract class A<T:>",
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
                "abstract class A<T:>",
                "	int someInt",
                "class B<K:> extends A<K>",
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
                "abstract class A<T:>",
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
        testAssertErrorsLines(false, "Cannot compare types T with null",
                "package test",
                "native testSuccess()",
                "function foo<T:>(T t)",
                "	if t == null",
                "		testSuccess()",
                "init",
                "	foo(null)"
        );
    }

    @Test
    public void missingTypeArgsFunc() {
        testAssertErrorsLines(false, "Cannot infer type for type parameter T, T",
                "package test",
                "function foo<T:>() returns T",
                "	return null",
                "init",
                "	let x = foo()"
        );
    }

    @Test
    public void missingTypeArgsMethod() {
        testAssertErrorsLines(false, "Cannot infer type for type parameter T, T",
                "package test",
                "class C",
                "	function foo<T:>() returns T",
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
                "class C<T:>",
                "init",
                "	let c = new C"
        );
    }


    @Test
    public void tooManyTypeArgsFunc() {
        testAssertErrorsLines(false, "Too many type arguments given",
                "package test",
                "function foo<T:>() returns T",
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
                "	function foo<T:>() returns T",
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
                "class C<T:>",
                "init",
                "	let c = new C<int, int>"
        );
    }

    @Test
    public void capturedType() { // #490
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "native println(string s)",
                "interface F<A:,R:>",
                "	function apply(A a) returns R",
                "function twice<X:>(F<X, X> f) returns F<X, X>",
                "	return x -> f.apply(f.apply(x))", // line 7
                "init",
                "	F<int, int> plus1 = x -> x + 1", // line 9
                "	F<int, int> plus2 = twice(plus1)",
                "	F<string, string> shout = twice((string s) -> s + \"!\")", // line 11
                "	if shout.apply(\"hello\") == \"hello!!\" and plus2.apply(1) == 3",
                "		testSuccess()"
        );
    }

    @Test
    public void severalSubMethods() { // #490
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "native println(string s)",
                "class B<T:>",
                "	function id(T x) returns T",
                "		return x",
                "class A<X:, Y:> extends B<Y>",
                "	override function id(Y y) returns Y",
                "		return y",
                "init",
                "	B<int> a = new A<int,int>",
                "	B<int> b = new A<string, int>",
                "	if a.id(4) == 4 and b.id(2) == 2",
                "		testSuccess()"
        );
    }

    @Test
    public void simpleCastTest() {
        testAssertOkLines(true,
                "package test",
                "	native testSuccess()",
                "	class A",
                "	class B extends A",
                "		int x",
                "	function get() returns A",
                "		let r = new B",
                "		r.x = 5",
                "		return r",
                "	init",
                "		if (get() castTo B).x == 5",
                "			testSuccess()",
                "endpackage"
        );
    }

    @Test
    public void abstractReturnT() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "abstract class F<T:>",
                "	abstract function get() returns T",
                "class X<T:> extends F<T>",
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
    public void genericVar_instanceMethods_runtime() {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class Box<T:>",
            "        private T store",
            "        function put(T v)",
            "            store = v      // instance method writes static generic array",
            "        function get() returns T",
            "            return store   // instance method reads static generic array",
            "    init",
            "        let bi = new Box<int>",
            "        let br = new Box<real>",
            "        bi.put(42)",
            "        br.put(1.5)",
            "        let xi = bi.get()",
            "        let xr = br.get()",
            "        if xi == 42 and xr == 1.5",
            "            testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericStaticVar_instanceMethods_runtime() {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class Box<T:>",
            "        private static T store",
            "        function put(T v)",
            "            store = v      // instance method writes static generic array",
            "        function get() returns T",
            "            let i = 1",
            "            if i < 1",
            "                return null",
            "            return store   // instance method reads static generic array",
            "        function clear()",
            "            store = null",
            "    init",
            "        let bi = new Box<int>",
            "        let br = new Box<real>",
            "        bi.put(42)",
            "        br.put(1.5)",
            "        let xi = bi.get()",
            "        let xr = br.get()",
            "        bi.clear()",
            "        br.clear()",
            "        let xi2 = bi.get()",
            "        let xr2 = br.get()",
            "        if xi == 42 and xr == 1.5 and xi2 == 0 and 1.0001 - xr2 > 1.",
            "            testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericStaticVar_Class() {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class A",
            "    class B",
            "    class Box<T:>",
            "        private static T store",
            "        function put(T v)",
            "            store = v      // instance method writes static generic array",
            "        function get() returns T",
            "            let i = 1",
            "            if i < 1",
            "                return null",
            "            return store   // instance method reads static generic array",
            "        function clear()",
            "            store = null",
            "    init",
            "        let bi = new Box<A>",
            "        let br = new Box<B>",
            "        bi.put(new A())",
            "        br.put(new B())",
            "        let xi = bi.get()",
            "        let xr = br.get()",
            "        bi.clear()",
            "        br.clear()",
            "        let xi2 = bi.get()",
            "        let xr2 = br.get()",
            "        if xi != null and xr != null  and xi2 == null and xr2 == null",
            "            testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericStaticArray_instanceMethods_runtime() {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    class Box<T:>",
            "        private static T array store",
            "        function put(int i, T v)",
            "            store[i] = v      // instance method writes static generic array",
            "        function get(int i) returns T",
            "            let i2 = 1",
            "            if i2 < 1",
            "                return null",
            "            return store[i]   // instance method reads static generic array",
            "        function clear(int i)",
            "            store[i] = null",
            "    init",
            "        let bi = new Box<int>",
            "        let br = new Box<real>",
            "        bi.put(3, 42)",
            "        br.put(1, 1.5)",
            "        let xi = bi.get(3)",
            "        let xr = br.get(1)",
            "        bi.clear(3)",
            "        br.clear(1)",
            "        let xi2 = bi.get(3)",
            "        let xr2 = br.get(1)",
            "        if xi == 42 and xr == 1.5 and xi2 == 0 and 1.0001 - xr2 > 1.",
            "            testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericStaticTuple_runtime() {
        testAssertOkLines(true,
            "package test",
            "    native testSuccess()",
            "    tuple ntup(int i, int i2)",
            "    tuple tup(int i, ntup nt)",
            "    class BoxItr<T:>",
            "        private int i = 0",
            "        private Box<T> box",
            "        construct(Box<T> box)",
            "            this.box = box",
            "        function next() returns T",
            "            return null",
            "    class Box<T:>",
            "        private static T array store",
            "        function iterator() returns BoxItr<T>",
            "            return new BoxItr<T>(this)",
            "        function put(int i, T v)",
            "            store[i] = v      // instance method writes static generic array",
            "        function get(int i) returns T",
            "            let i2 = 1",
            "            if i2 < 1",
            "                return null",
            "            return store[i]   // instance method reads static generic array",
            "        function clear(int i)",
            "            store[i] = null",
            "    init",
            "        let bi = new Box<tup>",
            "        let itr = bi.iterator()",
            "        bi.put(1, tup(3, ntup(42, 17)))",
            "        let xi = bi.get(1).i",
            "        let xr = bi.get(1).nt.i",
            "        bi.clear(1)",
            "        let xr2 = bi.get(1).i",
            "        if xi == 3 and xr == 42 and xr2 == 0 and itr.next().i == 0",
            "            testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void mixingNewOwner_legacyType_classField() {
        testAssertErrorsLines(false,
            "Cannot reference legacy-generic",
            "package test",
            "class B<T>",
            "class A<T:>",
            "    B<T> b"
        );
    }

    @Test
    public void mixingLegacyOwner_newType_classField() {
        testAssertErrorsLines(false,
            "Cannot reference new-generic",
            "package test",
            "class B<T:>",
            "class A<T>",
            "    B<T> b"
        );
    }

    @Test
    public void mixingNewOwner_legacyType_functionReturn() {
        testAssertErrorsLines(false,
            "Cannot reference legacy-generic",
            "package test",
            "class B<T>",
            "function makeB<T:>() returns B<T>",
            "    return null"
        );
    }

    @Test
    public void mixingLegacyOwner_newType_functionReturn() {
        testAssertErrorsLines(false,
            "Cannot reference new-generic",
            "package test",
            "class B<T:>",
            "function makeB<T>() returns B<T>",
            "    return null"
        );
    }

    @Test
    public void mixingNewOwner_legacyType_inExtendsClause() {
        testAssertErrorsLines(false,
            "Cannot reference legacy-generic",
            "package test",
            "interface I<T>",
            "class C<T:> implements I<T>"
        );
    }

    @Test
    public void mixingLegacyOwner_newType_methodReturn() {
        testAssertErrorsLines(false,
            "Cannot reference new-generic",
            "package test",
            "interface I<T:>",
            "class C<T>",
            "    function f() returns I<T>",
            "        return null"
        );
    }

    @Test
    public void mixingNewOwner_legacyType_nestedGenericUse() {
        testAssertErrorsLines(false,
            "Cannot reference legacy-generic",
            "package test",
            "class Box<X>",
            "class B<T>",
            "class A<T:>",
            "    Box<B<T>> field"
        );
    }

    @Test
    public void mixingLegacyOwner_newType_insideGenericClassMethod() {
        testAssertErrorsLines(false,
            "Cannot reference new-generic",
            "package test",
            "class B<T:>",
            "class A<T>",
            "    function usee()",
            "        B<T> x = null"
        );
    }

    @Test
    public void arrayListCapacity_lazyClosure_repro() {
        testAssertOkLines(true,
            "package test",
            "",
            "native testSuccess()",
            "",
            "constant int JASS_MAX_ARRAY_SIZE = 8190",
            "",
            "public function lazy<T:>(Lazy<T> l) returns Lazy<T>",
            "    return l",
            "",
            "public abstract class Lazy<T:>",
            "    T val = null",
            "    boolean wasRetrieved = false",
            "",
            "    abstract function retrieve() returns T",
            "",
            "    function get() returns T",
            "        if not wasRetrieved",
            "            val = retrieve()",
            "            wasRetrieved = true",
            "        return val",
            "",
            "public class ArrayList<T:>",
            "",
            "public class CFBuilding",
            "    ArrayList<CFBuilding> upgrades = null",
            "    Lazy<boolean> hasAAUpgrade = lazy<boolean>(() -> begin",
            "            var result = false",
            "            if upgrades != null",
        "                   result = true",
            "            return result",
            "        end)",
            "",
            "",
            "init",
            "    let a = new CFBuilding()",
            "    let b = new CFBuilding()",
            "    // ensure upgrades is non-null so closure does some work",
            "    b.upgrades = new ArrayList<CFBuilding>()",
            "    // invoke lazy attribute so its closure is actually referenced",
            "    if b.hasAAUpgrade.get() and not a.hasAAUpgrade.get()",
            "        testSuccess()"
        );
    }

    @Test
    public void inheritedField_lazyClosure_uses_enclosing_receiver() {
        testAssertOkLines(true,
            "package test",
            "",
            "native testSuccess()",
            "",
            "public abstract class Lazy<T:>",
            "    T val = null",
            "    boolean wasRetrieved = false",
            "",
            "    abstract function retrieve() returns T",
            "",
            "    function get() returns T",
            "        if not wasRetrieved",
            "            val = retrieve()",
            "            wasRetrieved = true",
            "        return val",
            "",
            "public function lazy<T:>(Lazy<T> l) returns Lazy<T>",
            "    return l",
            "",
            "public class BaseBuilding",
            "    boolean hasDetector = true",
            "",
            "public class AdvancedBuilding extends BaseBuilding",
            "    Lazy<boolean> detectorAvailable = lazy<boolean>(() -> hasDetector)",
            "",
            "init",
            "    let b = new AdvancedBuilding()",
            "    b.hasDetector = true",
            "    if b.detectorAvailable.get()",
            "        testSuccess()"
        );
    }

    @Test
    public void arrayListInClosure() {
        testAssertOkLines(true,
            "package Hello",
            "import NoWurst",
            "",
            "native testSuccess()",
            "",
            "public class ArrayList<T:>",
            "    private static T array store",
            "    private static int nextFreeIndex = 0",
            "",
            "    private static constant int MAX_FREE_SECTIONS = 256",
            "    private static int array freeSectionStart",
            "    private static int array freeSectionCapacity",
            "    private static int freeSectionCount = 0",
            "",
            "    private int startIndex",
            "    private int capacity",
            "    private int size = 0",
            "    private static constant int INITIAL_CAPACITY = 16",
            "",
            "    static function getNextFreeIndex() returns int",
            "        return nextFreeIndex",
            "",
            "    construct()",
            "        allocateStorage(INITIAL_CAPACITY)",
            "",
            "    construct(int initialCapacity)",
            "        allocateStorage(initialCapacity)",
            "",
            "    private function allocateStorage(int cap)",
            "        for i = 0 to freeSectionCount - 1",
            "            if freeSectionCapacity[i] >= cap",
            "                startIndex = freeSectionStart[i]",
            "                capacity = freeSectionCapacity[i]",
            "",
            "                for j = i to freeSectionCount - 2",
            "                    freeSectionStart[j] = freeSectionStart[j + 1]",
            "                    freeSectionCapacity[j] = freeSectionCapacity[j + 1]",
            "                freeSectionCount--",
            "                return",
            "",
            "        if nextFreeIndex + cap > 10000",
            "            compactFreeList()",
            "",
            "            if nextFreeIndex + cap > 10000",
            "                nextFreeIndex = 0",
            "",
            "        startIndex = nextFreeIndex",
            "        capacity = cap",
            "        nextFreeIndex += cap",
            "",
            "    private static function compactFreeList()",
            "        if freeSectionCount <= 1",
            "            return",
            "",
            "        for i = 1 to freeSectionCount - 1",
            "            let keyStart = freeSectionStart[i]",
            "            let keyCap = freeSectionCapacity[i]",
            "            var j = i - 1",
            "",
            "            while j >= 0 and freeSectionStart[j] > keyStart",
            "                freeSectionStart[j + 1] = freeSectionStart[j]",
            "                freeSectionCapacity[j + 1] = freeSectionCapacity[j]",
            "                j--",
            "",
            "            freeSectionStart[j + 1] = keyStart",
            "            freeSectionCapacity[j + 1] = keyCap",
            "",
            "        var writeIdx = 0",
            "        for readIdx = 0 to freeSectionCount - 1",
            "            if writeIdx > 0 and freeSectionStart[writeIdx - 1] + freeSectionCapacity[writeIdx - 1] == freeSectionStart[readIdx]",
            "                freeSectionCapacity[writeIdx - 1] += freeSectionCapacity[readIdx]",
            "            else",
            "                if writeIdx != readIdx",
            "                    freeSectionStart[writeIdx] = freeSectionStart[readIdx]",
            "                    freeSectionCapacity[writeIdx] = freeSectionCapacity[readIdx]",
            "                writeIdx++",
            "",
            "        freeSectionCount = writeIdx",
            "",
            "        if freeSectionCount > 0",
            "            let lastIdx = freeSectionCount - 1",
            "            if freeSectionStart[lastIdx] + freeSectionCapacity[lastIdx] == nextFreeIndex",
            "                nextFreeIndex = freeSectionStart[lastIdx]",
            "                freeSectionCount--",
            "",
            "    private function freeStorage()",
            "        if capacity <= 0",
            "            return",
            "",
            "        if freeSectionCount < MAX_FREE_SECTIONS",
            "            freeSectionStart[freeSectionCount] = startIndex",
            "            freeSectionCapacity[freeSectionCount] = capacity",
            "            freeSectionCount++",
            "",
            "            if startIndex + capacity == nextFreeIndex",
            "                nextFreeIndex = startIndex",
            "                freeSectionCount--",
            "        else",
            "            compactFreeList()",
            "",
            "            if freeSectionCount < MAX_FREE_SECTIONS",
            "                freeSectionStart[freeSectionCount] = startIndex",
            "                freeSectionCapacity[freeSectionCount] = capacity",
            "                freeSectionCount++",
            "",
            "    private function grow()",
            "        let newCapacity = capacity * 2",
            "        let oldStart = startIndex",
            "        let oldCapacity = capacity",
            "",
            "        allocateStorage(newCapacity)",
            "",
            "        for i = 0 to size - 1",
            "            store[startIndex + i] = store[oldStart + i]",
            "",
            "        let tempStart = startIndex",
            "        let tempCap = capacity",
            "        startIndex = oldStart",
            "        capacity = oldCapacity",
            "        freeStorage()",
            "        startIndex = tempStart",
            "        capacity = tempCap",
            "",
            "    ondestroy",
            "        for i = 0 to size - 1",
            "            store[startIndex + i] = null",
            "",
            "        // Return storage to free pool",
            "        freeStorage()",
            "",
            "    /** Adds one or more elements to the end of the list (amortized O(1)) */",
            "    function add(vararg T elems)",
            "        for elem in elems",
            "            if size >= capacity",
            "                grow()",
            "            store[startIndex + size] = elem",
            "            size++",
            "",
            "    /** Returns the element at the specified index (O(1)) */",
            "    function get(int index) returns T",
            "        if index < 0 or index >= size",
            "        return store[startIndex + index]",
            "",
            "    /** Removes the element at the given index and returns it (O(n) - shifts elements) */",
            "    function removeAt(int index) returns T",
            "        if index < 0 or index >= size",
            "",
            "        let elem = store[startIndex + index]",
            "",
            "        // Shift elements left",
            "        for i = index to size - 2",
            "            store[startIndex + i] = store[startIndex + i + 1]",
            "",
            "        size--",
            "        return elem",
            "",
            "    /** Returns the size of the list (O(1)) */",
            "    function size() returns int",
            "        return size",
            "",
            "    /** Checks whether this list is empty (O(1)) */",
            "    function isEmpty() returns boolean",
            "        return size == 0",
            "",
            "",
            "public function lazy<T:>(Lazy<T> l) returns Lazy<T>",
            "    return l",
            "",
            "public abstract class Lazy<T:>",
            "    T val = null",
            "    var wasRetrieved = false",
            "",
            "    abstract function retrieve() returns T",
            "",
            "    function get() returns T",
            "        if not wasRetrieved",
            "            val = retrieve()",
            "            wasRetrieved = true",
            "        return val",
            "",
            "public class CFBuilding",
            "    CFBuilding precursor = null",
            "    ArrayList<CFBuilding> upgrades = null",
            "",
            "    Lazy<boolean> hasAAUpgrade = lazy<boolean>(() -> begin",
            "        var result = false",
            "        if upgrades != null",
            "            result = true",
            "            // perform iterative search through \"tree\"",
            "            var toCheck = new ArrayList<CFBuilding>()",
            "            // toCheck.addAll(upgrades)",
            "            while not toCheck.isEmpty()",
            "                let b = toCheck.removeAt(0)",
            "                if b.isAntiAir",
            "                    result = true",
            "                    break",
            "                if b.upgrades != null",
            "                    // toCheck.addAll(b.upgrades)",
            "",
            "            destroy toCheck",
            "        return result",
            "    end)",
            "",
            "    var netWorthDiv100 = lazy<real>(() -> begin",
            "        var worth = 0",
            "        var cur = this",
            "        while cur != null",
            "            worth += cur.goldCost",
            "            cur = cur.precursor",
            "        return worth / 200.",
            "    end)",
            "",
            "    var goldCost = 0",
            "    var isAntiAir = false // Is the building an anti air unit",
            "",
            "init",
            "    let b = new CFBuilding()",
            "    b.upgrades = new ArrayList<CFBuilding>()",
            "    let aa = b",
            "    .hasAAUpgrade.get()",
            "    if aa == true",
            "        testSuccess()",
            ""
        );
    }

    @Test
    public void linkedListModule() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "module LinkedListModule",
            "    static thistype first = null",
            "    static thistype last = null",
            "    static int size = 0",
            "    thistype prev",
            "    thistype next",
            "    construct()",
            "        size++",
            "        if size == 1",
            "            first = this",
            "            prev = null",
            "        else",
            "            prev = last",
            "            last.next = this",
            "            first.prev = this",
            "        next = null",
            "        last = this",
            "    static function getFirst() returns thistype",
            "        return first",
            "    function getNext() returns thistype",
            "        if next == null",
            "            return first",
            "        return next",
            "    function getPrev() returns thistype",
            "        if prev == null",
            "            return last",
            "        return prev",
            "    function remove()",
            "        size--",
            "        if this != first",
            "            prev.next = next",
            "        else",
            "            first = next",
            "        if this != last",
            "            next.prev = prev",
            "        else",
            "            last = prev",
            "    ondestroy",
            "        remove()",
            "class Node<T:>",
            "    use LinkedListModule",
            "init",
            "    let a = new Node<int>",
            "    let b = new Node<int>",
            "    let c = new Node<int>",
            "    // simple sanity check: circular next traversal should loop",
            "    if a.getNext() != null and a.getPrev() != null",
            "        testSuccess()",
            "endpackage"
        );
    }


    @Test
    @Ignore // TODO
    public void genericClassWithLLModule() {
        testAssertOkLinesWithStdLib(true,
            "package test",
            "import LinkedListModule",
            "class Box<T:>",
            "    use LinkedListModule",
            "    private T value",
            "    function setValue(T v)",
            "        value = v",
            "    function getValue() returns T",
            "        return value",
            "init",
            "    let b = new Box<int>",
            "    b.setValue(42)",
            "    if b.getValue() == 42 and b.prev == null",
            "        testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericClassWithStaticInnerClass() {
        testAssertOkLines(true,
            "package test",
            "   native testSuccess()",
            "   class Outer<T:>",
            "      static T t = null",
            "      construct(T iVal)",
            "         t = iVal",
            "      function iterator() returns Inner",
            "         return new Inner()",
            "      static class Inner",
            "         construct()",
            "         function next() returns T",
            "            return t",
            "   init",
            "      let a = new Outer<int>(3)",
            "      if a.iterator().next() == 3",
            "         testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericModuleThistypeSmall() {
        testAssertOkLines(true,
            "package test",
            "   native testSuccess()",
            "   module LLM",
            "      static thistype t = null",
            "      construct()",
            "         t = this",
            "      function iterator() returns Iterator",
            "         return new Iterator()",
            "      static class Iterator",
            "         function next() returns LLM.thistype",
            "            return t",
            "   class A<T:>",
            "      use LLM",
            "   init",
            "      let a = new A<int>",
            "      if a.iterator().next() == a",
            "         testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericClassWithStaticMember() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A<T:>",
            "		 static int foo = 1",
            "        function setFoo(int v)",
            "            foo = v",
            "        function getFoo() returns int",
            "            return foo",
            "	init",
            "		let a = new A<int>",
            "		let b = new A<string>",
            "		a.setFoo(3)",
            "		if a.getFoo() == 3 and b.getFoo() == 1",
            "			testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericClassWithStaticMemberArray() {
        testAssertOkLines(true,
            "package test",
            "	native testSuccess()",
            "	class A<T:>",
            "		 static int array foo",
            "        function setFoo(int v)",
            "            foo[1] = v",
            "        function getFoo() returns int",
            "            return foo[1]",
            "	init",
            "		let a = new A<int>",
            "		let b = new A<string>",
            "		a.setFoo(3)",
            "		b.setFoo(1)",
            "		if a.getFoo() == 3 and b.getFoo() == 1",
            "			testSuccess()",
            "endpackage"
        );
    }


    @Test
    public void genericStaticGlobalsSpecializedInJassInit() throws IOException {
        test().executeProg(false).lines(
            "package test",
            "   public class Box<T:>",
            "       static int INITIAL = 16",
            "       static int MAX = 256",
            "       construct()",
            "       function total() returns int",
            "           return INITIAL + MAX",
            "   init",
            "       let a = new Box<int>()",
            "       let b = new Box<string>()",
            "       let c = new Box<real>()",
            "       var initSum = a.total() + b.total() + c.total()",
            "       if initSum == 3 * (16 + 256)",
            "           initSum = initSum + 1",
            "endpackage"
        );

        File output = new File(TEST_OUTPUT_PATH + "GenericsWithTypeclassesTests_genericStaticGlobalsSpecializedInJassInit_no_opts.j");
        String compiled = Files.toString(output, Charsets.UTF_8);

        Set<String> initTargets = new HashSet<>();
        for (String line : compiled.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("set Box_INITIAL")) {
                String[] parts = trimmed.split("\s+");
                if (parts.length > 1) {
                    initTargets.add(parts[1]);
                }
            }
        }
        assertEquals(initTargets.size(), 3);

        Set<String> maxTargets = new HashSet<>();
        for (String line : compiled.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("set Box_MAX")) {
                String[] parts = trimmed.split("\s+");
                if (parts.length > 1) {
                    maxTargets.add(parts[1]);
                }
            }
        }
        assertEquals(maxTargets.size(), 3);
    }

    @Test
    public void fullArrayListTest() throws IOException {
        test().withStdLib().executeProg().executeTests().file(new File(TEST_DIR + "arrayList.wurst"));

        String compiled = Files.toString(new File(TEST_OUTPUT_PATH + "GenericsWithTypeclassesTests_fullArrayListTest_no_opts.jim"), Charsets.UTF_8);
        // Count 2 occurences of integer ArrayList_MAX_ARRAY_SIZE
        String target = "integer ArrayList_MAX_ARRAY_SIZE";
        int count = 0;
        int idx = 0;
        while ((idx = compiled.indexOf(target, idx)) != -1) {
            count++;
            idx += target.length();
        }

       assertEquals(count, 2);

        assertFalse(compiled.contains("ArrayList_nextFreeIndex_"));
    }

    @Test
    public void genericNullComparison() {
        testAssertOkLinesWithStdLib(true,
            "package test",
            "import NoWurst",
            "import ClosureTimers",
            "native testSuccess()",
            "public class Reference<T:>",
            "    T val",
            "    construct(T val)",
            "        this.val = val",
            "    function into() returns T",
            "        let rval = val",
            "        destroy this",
            "        return rval",
            "",
            "var r = new Reference(0.0)",
            "",
            "init",
            "    // force member access on the global",
            "    doAfter(.1) ->",
            "       r.val = 1.0",
            "       if r != null and not r == null and r.into() == 1.0",
            "           testSuccess()",
            "    testSuccess() // to make compiler happy",
            "endpackage"
        );
    }

    @Test
    public void genericStaticAccessor_asClassInstanceMethods() {
        testAssertOkLinesWithStdLib(true,
            "package test",
            "import NoWurst",
            "native testSuccess()",

            "public class Box<T:>",
            "    static int count",
            "    function setCount(int v)",
            "        count = v",
            "    function getCount() returns int",
            "        return count",
            "init",
            "    let a = new Box<int>",
            "    let b = new Box<string>",
            "    a.setCount(3)",
            "    b.setCount(1)",
            "    if a.getCount() == 3 and b.getCount() == 1",
            "        testSuccess()",
            "endpackage"
        );
    }

    @Test
    public void genericStaticRawAccessIsRejected() {
        testAssertErrorsLines(false,
            // If your helper expects an error substring, keep it specific and stable:
            "Cannot access members via generic type",

            "package test",

            "public class Box<T:>",
            "    static int count",

            "init",
            "    // Raw generic class static access must be rejected (undefined specialization)",
            "    Box.count = 1",
            "endpackage"
        );
    }

    @Test
    public void staticInitAppliedToAllSpecializations_rawJassNoOpts() throws IOException {
        // Compile a tiny inline program (no external files) and then inspect the *no_opts* Jass output.
        testAssertOkLines( true,
                "package test",
                "native testSuccess()",
                "",
                "class Box<T:>",
                "    private static constant cap = 16",
                "    private static constant cappy = cap + 3",
                "    function getCap() returns int",
                "        return cap",
                "    function getCappy() returns int",
                "        return cappy",
                "",
                "tuple tt(real p, string s)",
                "init",
                "    let bi = new Box<int>",
                "    let br = new Box<real>",
                "    let sr = new Box<string>",
                "    let tr = new Box<tt>",
                "    if bi.getCap() == 16 and br.getCap() == 16 and sr.getCap() == 16 and tr.getCap() == 16 and bi.getCappy() == 19 and br.getCappy() == 19 and sr.getCappy() == 19 and tr.getCappy() == 19",
                "        testSuccess()",
                "endpackage",
                ""
            );

        // Read raw output (prefer .j, fallback .jim if that’s what the harness writes)
        String out = readNoOptsOutput("GenericsWithTypeclassesTests_staticInitAppliedToAllSpecializations_rawJassNoOpts");

        // Core assertion: both specializations must be initialized to 16 in the init function(s)
        assertTrue(out.contains("set Box_cap_integer_u = 16"),
            "Missing init for int specialization (expected: set Box_cap_integer_u = 16)");
        assertTrue(out.contains("set Box_cap_real_u = 16"),
            "Missing init for real specialization (expected: set Box_cap_real_u = 16)");

        // Guard against the observed collapse where everything becomes one specialization:
        int intSets  = countLinesContaining(out, "set Box_cap_integer_u = 16");
        int realSets = countLinesContaining(out, "set Box_cap_real_u = 16");
        assertTrue(intSets >= 1 && realSets >= 1, "Specialization init collapsed.");
    }

    private static String readNoOptsOutput(String baseName) throws IOException {
        // Most runs produce .j; some configs dump IM/Jass into .jim. Pick whichever exists.
        File j = new File(TEST_OUTPUT_PATH + baseName + "_no_opts.j");
        if (j.exists()) return com.google.common.io.Files.toString(j, Charsets.UTF_8);

        File jim = new File(TEST_OUTPUT_PATH + baseName + "_no_opts.jim");
        if (jim.exists()) return com.google.common.io.Files.toString(jim, Charsets.UTF_8);

        throw new IOException("No no_opts output found for " + baseName + " (tried .j and .jim)");
    }

    private static int countLinesContaining(String text, String needle) {
        int c = 0;
        for (String line : text.split("\\R")) {
            if (line.contains(needle)) c++;
        }
        return c;
    }

    @Test
    public void genericStaticInit() throws IOException {
        test().executeProg(false)
            .executeTests(false)
            .withStdLib(false)
            .withInputs(Map.of("test.wurst", Utils.string(
                "package test",
                "public class Box<T:>",
                "    static int cap = 16",
                "    function getCap() returns int",
                "        return cap"
            ),
                "usage.wurst", Utils.string(
                    "package usage",
                    "import test",
                    "native testSuccess()",
                    "native print(int i)",
                    "function useInt()",
                    "    let b = new Box<int>",
                    "    print(b.getCap())",
                    "function useReal()",
                    "    let b = new Box<Box<real>>",
                    "    print(b.getCap())",
                    "init",
                    "    useInt()",
                    "    useReal()",
                    "    testSuccess()"
                )))
            .run();

        String jass = com.google.common.io.Files.toString(
            new File(TEST_OUTPUT_PATH + "GenericsWithTypeclassesTests_genericStaticInit_no_opts.j"),
            Charsets.UTF_8
        );

        assertTrue(jass.contains("set Box_cap_integer_u = 16"));
        assertTrue(jass.contains("set Box_cap_Box_real__u = 16"));
    }

}
