package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * Tests for the null-safe member access operator {@code ?.} (v1).
 *
 * Semantics: {@code a?.foo(args)} evaluates {@code a} exactly once; if it is
 * null, the call (including argument evaluation) is skipped and the result is
 * null. The receiver type must be nullable. A result that cannot represent
 * null (int, real, bool, ...) may only be discarded (statement position).
 */
public class NullSafeOperatorTests extends WurstScriptTest {

    @Test
    public void callOnNullReceiverIsSkipped() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "class A",
            "    function fail()",
            "        testFail(\"must not be called\")",
            "init",
            "    A a = null",
            "    a?.fail()",
            "    testSuccess()"
        );
    }

    @Test
    public void callOnNonNullReceiverExecutes() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class A",
            "    int x = 0",
            "    function inc()",
            "        x++",
            "init",
            "    A a = new A",
            "    a?.inc()",
            "    if a.x == 1",
            "        testSuccess()"
        );
    }

    @Test
    public void argumentsAreNotEvaluatedForNullReceiver() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int evals = 0",
            "function sideEffect() returns int",
            "    evals++",
            "    return 1",
            "class A",
            "    function take(int x)",
            "init",
            "    A a = null",
            "    a?.take(sideEffect())",
            "    if evals == 0",
            "        testSuccess()"
        );
    }

    @Test
    public void receiverIsEvaluatedExactlyOnce() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int count = 0",
            "A theA = null",
            "function getA() returns A",
            "    count++",
            "    return theA",
            "class A",
            "    int x = 0",
            "    function inc()",
            "        x++",
            "init",
            "    theA = new A",
            "    getA()?.inc()",
            "    if count == 1 and theA.x == 1",
            "        testSuccess()"
        );
    }

    @Test
    public void fieldAccessPropagatesNull() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class A",
            "    A next = null",
            "init",
            "    A a = null",
            "    if a?.next == null",
            "        testSuccess()"
        );
    }

    @Test
    public void fieldAccessReadsFieldWhenNonNull() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class A",
            "    A next = null",
            "init",
            "    A a = new A",
            "    A b = new A",
            "    a.next = b",
            "    if a?.next == b",
            "        testSuccess()"
        );
    }

    @Test
    public void chainedNullSafeAccess() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class A",
            "    A next = null",
            "init",
            "    A a = new A",
            "    if a?.next == null and a?.next?.next == null",
            "        testSuccess()"
        );
    }

    @Test
    public void methodResultNullPropagation() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class A",
            "    function self() returns A",
            "        return this",
            "init",
            "    A a = null",
            "    A b = new A",
            "    if a?.self() == null and b?.self() == b",
            "        testSuccess()"
        );
    }

    @Test
    public void stringResultIsNullable() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class A",
            "    function name() returns string",
            "        return \"a\"",
            "init",
            "    A a = null",
            "    A b = new A",
            "    if a?.name() == null and b?.name() == \"a\"",
            "        testSuccess()"
        );
    }

    /** The grammar change must not break ternaries with real literals after '?'. */
    @Test
    public void ternaryWithRealLiteralStillParses() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "init",
            "    real r = true ? .5 : .25",
            "    if r > .4",
            "        testSuccess()"
        );
    }

    @Test
    public void errorOnNonNullableReceiver() {
        testAssertErrorsLines(false, "can never be null",
            "package test",
            "function int.foo()",
            "init",
            "    int x = 5",
            "    x?.foo()",
            "endpackage"
        );
    }

    @Test
    public void errorOnPrimitiveResultInExpressionPosition() {
        testAssertErrorsLines(false, "cannot represent null",
            "package test",
            "class A",
            "    function count() returns int",
            "        return 1",
            "init",
            "    A a = new A",
            "    int x = a?.count()",
            "endpackage"
        );
    }

    @Test
    public void primitiveResultAllowedInStatementPosition() {
        test().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int calls = 0",
            "class A",
            "    function count() returns int",
            "        calls++",
            "        return 1",
            "init",
            "    A a = new A",
            "    a?.count()",
            "    A b = null",
            "    b?.count()",
            "    if calls == 1",
            "        testSuccess()"
        );
    }

    @Test
    public void errorOnNonNullableFieldType() {
        testAssertErrorsLines(false, "cannot represent null",
            "package test",
            "class A",
            "    int size = 0",
            "init",
            "    A a = new A",
            "    int s = a?.size",
            "endpackage"
        );
    }

    /** End-to-end through the Lua backend with real execution. */
    @Test
    public void luaRuntimeNullSafeSemantics() {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int evals = 0",
            "function sideEffect() returns int",
            "    evals++",
            "    return 1",
            "class A",
            "    A next = null",
            "    int x = 0",
            "    function take(int y)",
            "        x += y",
            "    function self() returns A",
            "        return this",
            "init",
            "    A n = null",
            "    n?.take(sideEffect())",
            "    A a = new A",
            "    a?.take(sideEffect())",
            "    if evals == 1 and a.x == 1 and n?.self() == null and a?.self() == a and a?.next?.next == null",
            "        testSuccess()"
        );
    }
}
