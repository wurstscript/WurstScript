package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class VarargTests extends WurstScriptTest {


    @Test
    public void testVarargSyntax() {
        testAssertOkLines(false,
                "package Test",
                "function foo(vararg int ints)"
        );
    }

    @Test
    public void testInvalidVarargFunc() {
        testAssertErrorsLines(false, "may only have one parameter",
                "package Test",
                "function foo(int x, vararg int ints)",
                ""
        );
    }

    @Test
    public void testVarargAccess() {
        testAssertOkLines(false,
                "package Test",
                "function bar(int i)",
                "",
                "function foo(vararg int ints)",
                "    for i in ints",
                "        bar(i)",
                ""
        );
    }

    @Test
    public void testVarargInput() {
        testAssertOkLines(false,
                "package Test",
                "function foo(vararg int ints)",
                "init",
                "    foo(1,2,3)"
        );
    }

    @Test
    public void testVarargForeach() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(vararg int ints)",
                "    var sum = 0",
                "    for i in ints",
                "        sum += i",
                "    if sum == 10",
                "        testSuccess()",
                "init",
                "    foo(1,2,3,4)"
        );
    }

    @Test
    public void testVarargForeach2() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(vararg int ints) returns int",
                "    var sum = 0",
                "    for i in ints",
                "        sum += i",
                "    return sum",
                "init",
                "    if foo(1,2) + foo(3,4) == 10",
                "        testSuccess()"
        );
    }


    @Test
    public void varargsWithOverloading() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(string s)",
                "function foo(vararg int ints)",
                "    var sum = 0",
                "    for i in ints",
                "        sum += i",
                "    if sum == 10",
                "        testSuccess()",
                "init",
                "    foo(1,2,3,4)"
        );
    }

    @Test
    public void varargWithGenerics() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo<T>(vararg T ints)",
                "    var sum = 0",
                "    for i in ints",
                "        sum += i castTo int",
                "    if sum == 10",
                "        testSuccess()",
                "init",
                "    foo(1,2,3,4)"
        );
    }


    @Test
    public void varargWithBreak() {
        testAssertErrorsLines(true, "Cannot use break in vararg for each loops",
                "package Test",
                "native testSuccess()",
                "function foo(vararg int ints)",
                "    var sum = 0",
                "    for i in ints",
                "        sum += i",
                "        if i > 2",
                "            break",
                "    if sum == 3",
                "        testSuccess()",
                "init",
                "    foo(1,2,3,4)"
        );
    }

    @Test
    public void legitNestedBreak() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(vararg int ints)",
                "    var sum = 0",
                "    for i in ints",
                "        sum += i",
                "        for j = 1 to 4",
                "            sum += j",
                "            if j > 2",
                "                break",
                "    if sum == 34",
                "        testSuccess()",
                "init",
                "    foo(1,2,3,4)"
        );
    }

    @Test
    public void varargExtFunc() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function int.foo(vararg int ints)",
                "    var sum = this",
                "    for i in ints",
                "        sum += i",
                "        for j = 1 to 4",
                "            sum += j",
                "            if j > 2",
                "                break",
                "    if sum == 37",
                "        testSuccess()",
                "init",
                "    3 .foo(1,2,3,4)"
        );
    }

    @Test
    public void varargMethod() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "class C",
                "    function foo(vararg int ints)",
                "        var sum = 3",
                "        for i in ints",
                "            sum += i",
                "            for j = 1 to 4",
                "                sum += j",
                "                if j > 2",
                "                    break",
                "        if sum == 37",
                "            testSuccess()",
                "init",
                "    new C.foo(1,2,3,4)"
        );
    }

    @Test
    public void varargOverload() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "function foo(int x, string y)",
                "function foo(vararg int x)",
                "    testSuccess()",
                "init",
                "    foo(1,2)"
        );
    }

    @Test
    public void varargGenericMethodOverload() {
        testAssertOkLines(true,
                "package Test",
                "native testSuccess()",
                "class A<Q>",
                "class C<T>",
                "    function foo(A<T> s)",
                "    function foo(vararg T ints)",
                "        var sum = 3",
                "        for i in ints",
                "            sum++",
                "        if sum == 7",
                "            testSuccess()",
                "init",
                "    new C<int>.foo(new A<int>)",
                "    new C<int>.foo(1,2,3,4)"
        );
    }

    @Test
    public void testVarargInvalidOverload() {
        testAssertErrorsLines(true, "Call to function foo is ambiguous",
                "package Test",
                "native testSuccess()",
                "function foo(int i) returns int",
                "    return i",
                "function foo(vararg int ints) returns int",
                "    var sum = 0",
                "    for i in ints",
                "        sum += i",
                "    return sum",
                "init",
                "    if foo(0) + foo(1,2) + foo(3,4) == 10",
                "        testSuccess()"
        );
    }

    @Test
    public void genericToIndexVarargs() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class C<T>",
                "    T x",
                "    function setX(vararg T xs)",
                "        for x in xs",
                "            this.x = x",
                "    function getX() returns T",
                "        return x",
                "function stringToIndex(string s) returns int",
                "    return 42",
                "function stringFromIndex(int i) returns string",
                "    return \"42\"",
                "init",
                "    let c = new C<string>",
                "    c.setX(\"42\")",
                "    string s = c.getX()",
                "    if s == \"42\"",
                "        testSuccess()");

    }

    @Test
    public void testInvalidArgs() {
        testAssertErrorsLines(false, "Wrong parameter type when calling format",
                "package Test",
                "function string.format(vararg string replacements) returns string",
                "    return this",
                "init",
                "    \"toto\".format(\"test\", 1, 2)"
        );
    }

    @Test
    public void varargOverride() {
        testAssertOkLines(true,
                "package test",
                "native testSuccess()",
                "class C",
                "    function foo(vararg int xs) returns int",
                "        int sum = 0",
                "        for x in xs",
                "            sum += x",
                "        return sum",
                "class D extends C",
                "    override function foo(vararg int xs) returns int",
                "        int prod = 1",
                "        for x in xs",
                "            prod *= x",
                "        return prod",
                "init",
                "    C c = new C",
                "    C d = new D",
                "    if c.foo(1,2) == 3 and d.foo(1,2) == 2 and d.foo(1,2,3,4) == 24",
                "        testSuccess()");

    }


}
