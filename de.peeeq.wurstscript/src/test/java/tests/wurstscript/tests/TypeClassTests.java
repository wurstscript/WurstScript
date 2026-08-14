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
        testAssertErrorsLines(false, "does not implement any requirement of",
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

    /**
     * An instance method must match its requirement, not merely its name. Matching on the name
     * alone let a wrongly typed implementation be selected and emitted, which pjass then rejected.
     */
    @Test
    public void instanceMethodWithWrongParameterTypeIsRejected() {
        testAssertErrorsLines(false, "should have type int",
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(string x) returns int",
            "        return 1"
        );
    }

    @Test
    public void instanceMethodWithWrongParameterCountIsRejected() {
        testAssertErrorsLines(false, "must take 1 parameter",
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x, int y) returns int",
            "        return 1"
        );
    }

    @Test
    public void instanceMethodWithWrongReturnTypeIsRejected() {
        testAssertErrorsLines(false, "should return int",
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x) returns string",
            "        return \"a\""
        );
    }

    /** The substituted requirement is what must be matched, so T becomes the instance type. */
    @Test
    public void instanceMethodMatchingSubstitutedRequirementIsAccepted() {
        testAssertOkLines(false,
            "package test",
            "interface Indexable<T:>",
            "    function toIndex(T x) returns int",
            "    function fromIndex(int i) returns T",
            "implements Indexable<int>",
            "    function toIndex(int x) returns int",
            "        return x",
            "    function fromIndex(int i) returns int",
            "        return i"
        );
    }

    /**
     * An abstract type argument can only supply a bound it declares itself. Accepting it silently
     * produced a program that type checked but failed at runtime with no instance to dispatch to.
     */
    @Test
    public void unboundedTypeParameterCannotSatisfyBound() {
        testAssertErrorsLines(false, "does not satisfy the bound",
            "package test",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "function inner<Q: Show>(Q x) returns string",
            "    return Q.show(x)",
            "function outer<R:>(R x) returns string",
            "    return inner(x)",
            "init",
            "    outer(42)"
        );
    }

    /** The same shape is fine once the outer parameter declares the bound it passes on. */
    @Test
    public void boundedTypeParameterSatisfiesBound() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "function inner<Q: Show>(Q x) returns string",
            "    return Q.show(x)",
            "function outer<R: Show>(R x) returns string",
            "    return inner(x)",
            "init",
            "    if outer(42) == \"i\"",
            "        testSuccess()"
        );
    }

    /** A bound naming something that is not a usable type class must be reported, not ignored. */
    @Test
    public void classAsBoundIsRejected() {
        testAssertErrorsLines(false, "is not an interface",
            "package test",
            "class Marker",
            "function foo<Q: Marker>(Q x) returns int",
            "    return 0",
            "init",
            "    foo(1)"
        );
    }

    @Test
    public void appliedInterfaceAsBoundIsRejected() {
        testAssertErrorsLines(false, "without type arguments",
            "package test",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "function foo<Q: ToIndex<int>>(Q x) returns int",
            "    return 0"
        );
    }

    @Test
    public void multiParameterInterfaceAsBoundIsRejected() {
        testAssertErrorsLines(false, "exactly one type parameter",
            "package test",
            "interface Convert<A:, B:>",
            "    function convert(A a) returns B",
            "function foo<Q: Convert>(Q x) returns int",
            "    return 0"
        );
    }

    /** Requirements are the interface's own functions, so an extending interface is not a bound. */
    @Test
    public void extendingInterfaceAsBoundIsRejected() {
        testAssertErrorsLines(false, "extends another interface",
            "package test",
            "interface Base<T:>",
            "    function base(T x) returns int",
            "interface Derived<T:> extends Base<T>",
            "    function derived(T x) returns int",
            "function foo<Q: Derived>(Q x) returns int",
            "    return 0"
        );
    }

    /**
     * A bound may belong to a generic class rather than to the method, which is the shape the
     * documentation uses for HashMap. The receiver carries the arguments the class was made with.
     */
    @Test
    public void boundOnGenericClass() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "class Box<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "init",
            "    if new Box<int>().render(42) == \"i\"",
            "        testSuccess()"
        );
    }

    /** The same, with the class holding the value, as a container actually would. */
    @Test
    public void boundOnGenericClassWithField() {
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
            "class Cell<T: Indexable>",
            "    private int stored",
            "    function put(T value)",
            "        stored = T.toIndex(value)",
            "    function get() returns T",
            "        return T.fromIndex(stored)",
            "init",
            "    let c = new Cell<int>()",
            "    c.put(7)",
            "    if c.get() == 7",
            "        testSuccess()"
        );
    }

    @Test
    public void boundOnGenericClassLua() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "class Box<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "init",
            "    if new Box<int>().render(42) == \"i\"",
            "        testSuccess()"
        );
    }

    /** Two classes at different types must each pick their own instance. */
    @Test
    public void boundOnGenericClassTwoTypes() {
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
            "class Box<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "init",
            "    if new Box<int>().render(1) == \"i\" and new Box<string>().render(\"a\") == \"s\"",
            "        testSuccess()"
        );
    }

    /**
     * An interface may overload a requirement name. Each requirement pairs with the implementation
     * matching its signature, and the lowering must select the same one.
     */
    @Test
    public void overloadedRequirementsAreMatchedBySignature() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Convert<T:>",
            "    function convert(T x) returns int",
            "    function convert(int scale, T x) returns int",
            "implements Convert<string>",
            "    function convert(string x) returns int",
            "        return 1",
            "    function convert(int scale, string x) returns int",
            "        return scale * 2",
            "function useBoth<Q: Convert>(Q x) returns int",
            "    return Q.convert(x) + Q.convert(20, x)",
            "init",
            "    if useBoth(\"a\") == 41",
            "        testSuccess()"
        );
    }

    /** A missing overload is still reported, even when the name is present. */
    @Test
    public void missingOverloadIsRejected() {
        testAssertErrorsLines(false, "must implement",
            "package test",
            "interface Convert<T:>",
            "    function convert(T x) returns int",
            "    function convert(int scale, T x) returns int",
            "implements Convert<string>",
            "    function convert(string x) returns int",
            "        return 1"
        );
    }

    /** A subclass may fix its parent's bounded type parameter. */
    @Test
    public void boundInheritedFromGenericParent() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "class Parent<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "class Child extends Parent<int>",
            "init",
            "    if new Child().render(1) == \"i\"",
            "        testSuccess()"
        );
    }

    @Test
    public void boundInheritedFromGenericParentLua() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "class Parent<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "class Child extends Parent<int>",
            "init",
            "    if new Child().render(1) == \"i\"",
            "        testSuccess()"
        );
    }

    /** A generic subclass may forward its own parameter to its parent's bound. */
    @Test
    public void boundForwardedByGenericSubclass() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "class Parent<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "class Child<U: Show> extends Parent<U>",
            "init",
            "    if new Child<int>().render(1) == \"i\"",
            "        testSuccess()"
        );
    }

    @Test
    public void boundForwardedByGenericSubclassLua() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "class Parent<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "class Child<U: Show> extends Parent<U>",
            "init",
            "    if new Child<int>().render(1) == \"i\"",
            "        testSuccess()"
        );
    }

    /** Two levels of forwarding, so the substitution has to compose rather than apply once. */
    @Test
    public void boundForwardedThroughTwoLevels() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "class Top<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "class Middle<M: Show> extends Top<M>",
            "class Bottom<B: Show> extends Middle<B>",
            "init",
            "    if new Bottom<int>().render(1) == \"i\"",
            "        testSuccess()"
        );
    }

    /** A bound on a module type parameter is rejected: using a module copies its body out of scope. */
    @Test
    public void boundOnGenericModule() {
        testAssertErrorsLines(false, "not supported on a module type parameter",
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "module M<T: Show>",
            "    function render(T x) returns string",
            "        return T.show(x)",
            "class C",
            "    use M<int>",
            "init",
            "    if new C().render(1) == \"i\"",
            "        testSuccess()"
        );
    }

    /** A method with its own type parameters must not disturb the class binding taken from the receiver. */
    @Test
    public void classBoundWithIndependentMethodTypeParam() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"int\"",
            "implements Show<string>",
            "    function show(string x) returns string",
            "        return \"string\"",
            "class Box<T: Show>",
            "    function describe<U:>(T x, U other) returns string",
            "        return T.show(x)",
            "init",
            "    if new Box<int>().describe<string>(1, \"a\") == \"int\"",
            "        testSuccess()"
        );
    }

    /** A requirement may only use the interface's type parameter, and says so plainly. */
    @Test
    public void genericRequirement() {
        testAssertErrorsLines(false, "has its own type parameters",
            "package test",
            "interface Pairing<T:>",
            "    function pair<U:>(T x, U y) returns U",
            "implements Pairing<int>",
            "    function pair<U:>(int x, U y) returns U",
            "        return y"
        );
    }

    /**
     * Two classes with the same simple name in different packages, dispatched through a bounded
     * generic class so the lookup by type is the path used. Selecting by printed name made the
     * second silently dispatch through the first's implementation.
     */
    @Test
    public void sameSimpleNameThroughRegistryFallback() {
        testAssertOkLines(true,
            "package Iface",
            "public interface Show<T:>",
            "    function show(T x) returns string",
            "public class Renderer<Q: Show>",
            "    function render(Q x) returns string",
            "        return Q.show(x)",
            "endpackage",
            "",
            "package First",
            "import public Iface",
            "public class Item",
            "implements Show<Item>",
            "    function show(Item x) returns string",
            "        return \"first\"",
            "public function firstResult() returns string",
            "    return new Renderer<Item>().render(new Item())",
            "endpackage",
            "",
            "package Second",
            "import public Iface",
            "public class Item",
            "implements Show<Item>",
            "    function show(Item x) returns string",
            "        return \"second\"",
            "public function secondResult() returns string",
            "    return new Renderer<Item>().render(new Item())",
            "endpackage",
            "",
            "package test",
            "import First",
            "import Second",
            "native testSuccess()",
            "init",
            "    if firstResult() == \"first\" and secondResult() == \"second\"",
            "        testSuccess()",
            "endpackage"
        );
    }

    /**
     * Two bounds may require the same operation. Bounds are ordered and the earlier one wins,
     * rather than every call to the shared operation becoming ambiguous.
     */
    @Test
    public void duplicateRequirementAcrossBounds() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface First<T:>",
            "    function show(T x) returns string",
            "interface Second<T:>",
            "    function other(T x) returns int",
            "    function show(T x) returns string",
            "implements First<int>",
            "    function show(int x) returns string",
            "        return \"first\"",
            "implements Second<int>",
            "    function other(int x) returns int",
            "        return 1",
            "    function show(int x) returns string",
            "        return \"second\"",
            "function render<Q: First and Second>(Q x) returns string",
            "    return Q.show(x)",
            "init",
            "    if render(1) == \"first\"",
            "        testSuccess()"
        );
    }

    /**
     * Bounds only shadow each other when they require the same shape. A later bound still supplies
     * a differently shaped overload, which overload resolution then chooses between.
     */
    @Test
    public void overloadFromLaterBoundStaysAvailable() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface First<T:>",
            "    function show(T x) returns string",
            "interface Second<T:>",
            "    function show(T x) returns string",
            "    function show(int scale, T x) returns string",
            "implements First<int>",
            "    function show(int x) returns string",
            "        return \"first\"",
            "implements Second<int>",
            "    function show(int x) returns string",
            "        return \"second\"",
            "    function show(int scale, int x) returns string",
            "        return \"scaled\"",
            "function render<Q: First and Second>(Q x) returns string",
            "    return Q.show(x) + Q.show(2, x)",
            "init",
            "    if render(1) == \"firstscaled\"",
            "        testSuccess()"
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
