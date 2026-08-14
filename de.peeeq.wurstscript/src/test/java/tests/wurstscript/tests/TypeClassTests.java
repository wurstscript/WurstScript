package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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

    /**
     * A bound must cost nothing at runtime: after specialisation the requirement is an ordinary
     * call to the instance function, with no dispatch, lookup or table left behind.
     */
    @Test
    public void dispatchLowersToDirectCallLua() throws IOException {
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
        String compiled = Files.toString(
            new File("test-output/lua/TypeClassTests_dispatchLowersToDirectCallLua.lua"), Charsets.UTF_8);
        assertTrue(compiled.contains("toIndex"), "the instance function should survive as a real function");
        assertFalse(compiled.contains("TypeVarDispatch"), "dispatch must not reach the backend");
        assertFalse(compiled.contains("typeClassBinding"), "no dictionary should be emitted");
    }

    // --- diagnostics -------------------------------------------------------------------------

    @Test
    public void unsatisfiedBoundIsRejected() {
        testAssertErrorsLines(false, "does not satisfy the bound",
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "class A",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    return Q.toIndex(x)",
            "init",
            "    foo(new A)"
        );
    }

    @Test
    public void duplicateInstanceIsRejected() {
        testAssertErrorsLines(false, "already an instance",
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "class A",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 1",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 2"
        );
    }

    @Test
    public void incompleteInstanceIsRejected() {
        testAssertErrorsLines(false, "must implement",
            "package test",
            "interface Indexable<T:>",
            "    function toIndex(T x) returns int",
            "    function fromIndex(int i) returns T",
            "class A",
            "implements Indexable<A>",
            "    function toIndex(A x) returns int",
            "        return 1"
        );
    }

    @Test
    public void methodNotRequiredByInterfaceIsRejected() {
        testAssertErrorsLines(false, "is not required by",
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "class A",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 1",
            "    function somethingElse(A x) returns int",
            "        return 2"
        );
    }

    /** An instance must live with its interface or with its type, never anywhere else. */
    @Test
    public void orphanInstanceIsRejected() {
        testAssertErrorsLines(false, "must be declared with its interface or with its type",
            "package Iface",
            "public interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "endpackage",
            "package Types",
            "public class A",
            "endpackage",
            "package Orphan",
            "import Iface",
            "import Types",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 1",
            "endpackage"
        );
    }

    /** The instance may live with the type rather than with the interface. */
    @Test
    public void instanceWithTypeIsAccepted() {
        testAssertOkLines(false,
            "package Iface",
            "public interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "endpackage",
            "package Types",
            "import public Iface",
            "public class A",
            "implements ToIndex<A>",
            "    function toIndex(A x) returns int",
            "        return 1",
            "endpackage"
        );
    }

    /** A bound must name an interface with exactly one type parameter. */
    @Test
    public void multiParameterInterfaceIsRejectedAsBound() {
        testAssertErrorsLines(false, "cannot be used as a type class",
            "package test",
            "interface Convert<A:, B:>",
            "    function convert(A a) returns B",
            "class C",
            "implements Convert<C, int>",
            "    function convert(C a) returns int",
            "        return 1"
        );
    }

    /** A type parameter is not a value, so it may only appear as the receiver of a requirement. */
    @Test
    public void typeParameterIsNotAValue() {
        testAssertErrorsLines(false, "Could not find variable Q",
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    let y = Q",
            "    return 0"
        );
    }
}
