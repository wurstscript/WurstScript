package tests.wurstscript.tests;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

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
        testAssertOkLines(true,
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
        testAssertOkLines(true,
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
    public void implicitConversionFail() { // see bug #121
        testAssertOkLinesWithStdLib(false,
                "package Test",
                "import LinkedList",
                "Table data",
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
                "Table data",

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
        testAssertErrorsLines(false, "implement",
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
        testAssertOkLines(true,
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
                "	for i in c",
                "		if i == 5",
                "			testSuccess()"
        );
    }

    @Test
    public void genericForFrom() {
        testAssertOkLines(true,
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

}
