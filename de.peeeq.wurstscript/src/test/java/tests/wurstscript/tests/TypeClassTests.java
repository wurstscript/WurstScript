package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * Tests for type class bounds on new-style generics:
 * <p>
 * - an ordinary generic {@code interface} declares the requirements,
 * - a top level {@code implements} block binds those requirements to one concrete type,
 * - a bound {@code <T: Iface>} makes the requirements available inside the generic body,
 * - {@code T.method(args)} dispatches through the instance chosen for {@code T}.
 */
public class TypeClassTests extends WurstScriptTest {

    @Test
    public void parseInstanceDecl() {
        testAssertOkLines(false,
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "class A",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 42"
        );
    }

    @Test
    public void dispatchThroughBoundTypeChecks() {
        testAssertOkLines(false,
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "class A",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 42",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    return Q.toIndex(x)"
        );
    }

    @Test
    public void dispatchRuntime() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "class A",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 42",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    return Q.toIndex(x)",
            "init",
            "    if foo(new A) == 42",
            "        testSuccess()"
        );
    }

    /** Each type argument picks its own instance, so one generic serves several types. */
    @Test
    public void twoInstancesOfOneClass() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "class A",
            "class B",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 1",
            "implements ToIndex<B>",
            "    function toIndex(B x) returns int",
            "        return 2",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    return Q.toIndex(x)",
            "init",
            "    if foo(new A) == 1 and foo(new B) == 2",
            "        testSuccess()"
        );
    }

    /** A bound is satisfiable by a primitive, which is the whole point of not using subtyping. */
    @Test
    public void instanceForPrimitive() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x) returns int",
            "        return x * 2",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    return Q.toIndex(x)",
            "init",
            "    if foo(21) == 42",
            "        testSuccess()"
        );
    }

    /** Several requirements combine with 'and', and each resolves independently. */
    @Test
    public void multipleBounds() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Plus<T:>",
            "    function plus(T x, T y) returns T",
            "interface Times<T:>",
            "    function times(T x, T y) returns T",
            "implements Plus<int>",
            "    function plus(int x, int y) returns int",
            "        return x + y",
            "implements Times<int>",
            "    function times(int x, int y) returns int",
            "        return x * y",
            "function calc<Q: Plus and Times>(Q x) returns Q",
            "    return Q.plus(x, Q.times(x, x))",
            "init",
            "    if calc(6) == 42",
            "        testSuccess()"
        );
    }

    /** An interface may require more than one function. */
    @Test
    public void roundTripThroughTwoRequirements() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Indexable<T:>",
            "    function toIndex(T x) returns int",
            "    function fromIndex(int i) returns T",
            "implements Indexable<int>",
            "    function toIndex(int x) returns int",
            "        return x + 1",
            "    function fromIndex(int i) returns int",
            "        return i - 1",
            "function roundTrip<Q: Indexable>(Q x) returns Q",
            "    return Q.fromIndex(Q.toIndex(x))",
            "init",
            "    if roundTrip(7) == 7",
            "        testSuccess()"
        );
    }

    /** A bounded generic may call another one, passing its own still-abstract type parameter on. */
    @Test
    public void transitiveBound() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x) returns int",
            "        return x",
            "function inner<Q: ToIndex>(Q x) returns int",
            "    return Q.toIndex(x)",
            "function outer<R: ToIndex>(R x) returns int",
            "    return inner(x)",
            "init",
            "    if outer(42) == 42",
            "        testSuccess()"
        );
    }

    /** The same generic used at two types must not collapse to one specialisation. */
    @Test
    public void distinctSpecialisationsPerType() {
        testAssertOkLines(true,
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
            "function render<Q: Show>(Q x) returns string",
            "    return Q.show(x)",
            "init",
            "    if render(1) == \"i\" and render(\"a\") == \"s\"",
            "        testSuccess()"
        );
    }

    @Test
    public void dispatchRuntimeLua() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x) returns int",
            "        return x * 2",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    return Q.toIndex(x)",
            "init",
            "    if foo(21) == 42",
            "        testSuccess()"
        );
    }
}
