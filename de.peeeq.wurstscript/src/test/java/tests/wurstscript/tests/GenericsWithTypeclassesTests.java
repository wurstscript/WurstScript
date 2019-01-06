package tests.wurstscript.tests;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

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

}
