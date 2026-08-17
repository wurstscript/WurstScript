package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * A closure lifts its body into a class of its own, capturing the enclosing type variables. The
     * requirement is dispatched from inside that class, so the binding has to survive being carried
     * across into it.
     */
    @Test
    public void dispatchInsideClosure() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x) returns int",
            "        return x * 2",
            "interface Producer",
            "    function produce() returns int",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    Producer p = () -> Q.toIndex(x)",
            "    return p.produce()",
            "init",
            "    if foo(21) == 42",
            "        testSuccess()"
        );
    }

    /**
     * The same closure on Lua, which keeps generics erased and specialises only what it can reach
     * from a concrete type. A closure is reached through its interface, so no call names the
     * instantiation — the construction is the only thing that knows it, and that is what the
     * specialisation is now driven from.
     */
    @Test
    public void dispatchInsideClosureLua() throws IOException {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x) returns int",
            "        return x * 2",
            "interface Producer",
            "    function produce() returns int",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    Producer p = () -> Q.toIndex(x)",
            "    return p.produce()",
            "init",
            "    if foo(21) == 42",
            "        testSuccess()"
        );

        // Running is not enough on its own: the same answer comes out whether the closure was
        // specialised or the erased class happened to carry a working implementation. These say
        // which of the two happened.
        String compiled = Files.toString(
            new File("test-output/lua/TypeClassTests_dispatchInsideClosureLua.lua"), Charsets.UTF_8);

        Matcher allocation = Pattern.compile("(\\w+_specialized\\w*):create\\d*\\(").matcher(compiled);
        assertTrue(allocation.find(),
            "the closure should be allocated from its specialised class:\n" + compiled);
        String specialised = allocation.group(1);

        Matcher call = Pattern.compile("\\w+:(\\w*produce\\w*)\\(").matcher(compiled);
        assertTrue(call.find(), "expected a dispatched produce slot:\n" + compiled);
        String slot = call.group(1);

        assertTrue(Pattern.compile(Pattern.quote(specialised) + "\\." + Pattern.quote(slot)
                + "\\s*=\\s*" + Pattern.quote(specialised) + "\\w*").matcher(compiled).find(),
            "the specialised class should bind " + slot + " to its own implementation:\n" + compiled);
        assertFalse(Pattern.compile(Pattern.quote(specialised) + "\\." + Pattern.quote(slot)
                + "\\s*=\\s*Producer_test_produce\\b").matcher(compiled).find(),
            "the specialised class must not bind " + slot + " to the generic original:\n" + compiled);
    }

    /**
     * A constructor runs before the object exists, so the bound has to be resolved from the type
     * argument the construction names rather than from anything reachable on the receiver.
     */
    private static final String[] DISPATCH_IN_CONSTRUCTOR = {
        "package test",
        "native testSuccess()",
        "interface Show<T:>",
        "    function show(T x) returns int",
        "implements Show<int>",
        "    function show(int x) returns int",
        "        return x * 2",
        "class Box<T: Show>",
        "    int cached",
        "    construct(T x)",
        "        cached = T.show(x)",
        "init",
        "    let b = new Box<int>(21)",
        "    if b.cached == 42",
        "        testSuccess()",
    };

    @Test
    public void dispatchInsideConstructor() {
        testAssertOkLines(true, DISPATCH_IN_CONSTRUCTOR);
    }

    /**
     * Still rejected for Lua, and this pins that it is rejected clearly rather than mistranslated.
     * A constructor belongs to the class, not to a generic function of its own, so the call that
     * runs it carries no type arguments — {@code new_Box(21)} in the intermediate language, with
     * the instantiation only on the type of what it is assigned to. Nothing on the Lua path reads
     * it from there, so the dispatch inside the constructor is never given a concrete type.
     * Should that be made to work, this test fails and becomes the success case above.
     */
    @Test
    public void dispatchInsideConstructorIsRejectedForLua() {
        test().testLua(true).executeProg().expectError("could not be resolved for the Lua target")
            .lines(DISPATCH_IN_CONSTRUCTOR);
    }

    /**
     * The closure has no dispatch of its own — it calls something that does. The gate deciding
     * whether a construction is the only place an instantiation is stated has to follow calls to
     * see that, or this reaches the backend with the bound unresolved.
     */
    @Test
    public void dispatchInsideClosureThroughHelperLua() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x) returns int",
            "        return x * 2",
            "interface Producer",
            "    function produce() returns int",
            "function helper<Q: ToIndex>(Q x) returns int",
            "    return Q.toIndex(x)",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    Producer p = () -> helper(x)",
            "    return p.produce()",
            "init",
            "    if foo(21) == 42",
            "        testSuccess()"
        );
    }

    /**
     * Wurst and Lua reserve different words, so a method can be declared {@code repeat} and reach
     * the backend under that name. A closure adds the name it implements as a dispatch alias
     * directly, without the uniquing that protects method names, so the alias arrives as a bare
     * keyword and is emitted as a table key — {@code <name> expected near 'repeat'} from luac.
     */
    @Test
    public void closureImplementingALuaKeywordName() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Producer",
            "    function repeat() returns int",
            "init",
            "    Producer p = () -> 42",
            "    if p.repeat() == 42",
            "        testSuccess()"
        );
    }

    /**
     * A closure written inside another one is still rejected, and this pins that it is rejected in
     * the same words as before rather than falling over inside the rewrite. The inner closure
     * reaches its captured environment through a receiver belonging to the outer one, which has
     * been specialised by then, so specialising the owner again with what is left over does not
     * work. Should that be made to work, this test fails and becomes a success case.
     */
    @Test
    public void nestedClosuresInsideBoundedGenericAreRejectedForLua() {
        test().testLua(true).executeProg().expectError("could not be resolved for the Lua target").lines(
            "package test",
            "native testSuccess()",
            "interface ToIndex<T:>",
            "    function toIndex(T x) returns int",
            "implements ToIndex<int>",
            "    function toIndex(int x) returns int",
            "        return x * 2",
            "interface Producer",
            "    function produce() returns int",
            "function foo<Q: ToIndex>(Q x) returns int",
            "    Producer outer = () -> begin",
            "        Producer inner = () -> Q.toIndex(x)",
            "        return inner.produce()",
            "    end",
            "    return outer.produce()",
            "init",
            "    if foo(21) == 42",
            "        testSuccess()"
        );
    }

    /**
     * A slot of a {@code T array} that was never written reads as the default of whatever T stands
     * for. The default is computed by a static attribute, which cannot see what T is bound to, so
     * it produces a stand-in — and a stand-in compares equal only to another stand-in, which made
     * this quietly false on the interpreter while both backends had it right.
     */
    @Test
    public void unwrittenArrayOfATypeParameterReadsAsItsDefault() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class Box<T:>",
            "    private static T array none",
            "    static function first() returns T",
            "        return none[0]",
            "init",
            "    if Box<int>.first() == 0 and Box<string>.first() == null",
            "        testSuccess()"
        );
    }

    /**
     * The same comparison the other way round. Only the left operand is asked whether it is equal,
     * so a stand-in on the right would have gone quietly false while one on the left complained.
     */
    @Test
    public void unwrittenArrayOfATypeParameterReadsAsItsDefaultReversed() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "class Box<T:>",
            "    private static T array none",
            "    static function first() returns T",
            "        return none[0]",
            "init",
            "    if 0 == Box<int>.first() and null == Box<string>.first()",
            "        testSuccess()"
        );
    }

    private static final String[] SUBCLASS_OF_BOUNDED_GENERIC = {
        "package test",
        "native testSuccess()",
        "interface Show<T:>",
        "    function show(T x) returns int",
        "implements Show<int>",
        "    function show(int x) returns int",
        "        return x",
        "class Box<K: Show>",
        "    K key",
        "    construct(K k)",
        "        key = k",
        "    function size(int extra) returns int",
        "        return K.show(key) + extra",
        "class SubBox extends Box<int>",
        "    construct(int k)",
        "        super(k)",
        "    override function size(int extra) returns int",
        "        return super.size(extra) + 100",
        "init",
        "    Box<int> b = new Box<int>(5)",
        "    Box<int> s = new SubBox(5)",
        "    if b.size(1) == 6 and s.size(1) == 106",
        "        testSuccess()",
    };

    /**
     * A subclass of a bounded generic class reaches its superclass through both a super constructor
     * call and a super method call, and each is a call which names its target rather than going
     * through a receiver. Both carry the class's type arguments, so both reach the copy specialised
     * for the instantiation the subclass extends.
     */
    @Test
    public void subclassOfBoundedGeneric() {
        testAssertOkLines(true, SUBCLASS_OF_BOUNDED_GENERIC);
    }

    /**
     * The same program on Lua, where it still does not work, so the difference between the targets is
     * stated rather than left to be discovered. {@code transformGenericNewOnly} runs neither
     * {@code simplifyClasses} nor {@code addMemberTypeArguments}, so the class's type variables are
     * never lifted onto its functions and there is nothing for a super call to carry. The object is
     * allocated from the erased {@code Box} table while the specialised one holds the method, so it
     * compiles and runs and never reaches {@code testSuccess}. Tracked as backlog item 13, whose
     * remaining half is the erasure decision in item 23.
     */
    @Test(expectedExceptions = Error.class, expectedExceptionsMessageRegExp = ".*Succeed function not called.*")
    public void subclassOfBoundedGenericIsStillBrokenOnLua() {
        test().testLua(true).executeProg().lines(SUBCLASS_OF_BOUNDED_GENERIC);
    }

    /**
     * A method may have type parameters of its own on top of the class's. The call already carries an
     * argument for its own, so what it is short of is the class's prefix rather than everything, and
     * the two lists have to end up in the order the lift put the variables in.
     */
    private static final String[] SUPER_CALL_TO_A_GENERIC_METHOD = {
        "package test",
        "native testSuccess()",
        "interface Show<T:>",
        "    function show(T x) returns int",
        "implements Show<int>",
        "    function show(int x) returns int",
        "        return x",
        "class Box<K: Show>",
        "    K key",
        "    construct(K k)",
        "        key = k",
        "    function choose<Q>(Q q, int extra) returns int",
        "        return K.show(key) + extra",
        "    function size(int extra) returns int",
        "        return extra",
        "class Marker",
        "class SubBox extends Box<int>",
        "    construct(int k)",
        "        super(k)",
        "    override function size(int extra) returns int",
        "        return super.choose<Marker>(new Marker(), extra) + 100",
        "init",
        "    Box<int> s = new SubBox(5)",
        "    if s.size(1) == 106",
        "        testSuccess()",
    };

    @Test
    public void superCallToAGenericMethodOfABoundedGenericClass() {
        testAssertOkLines(true, SUPER_CALL_TO_A_GENERIC_METHOD);
    }

    /**
     * A class and a method inside it may each declare a bounded type parameter spelled the same, and
     * they are different parameters bound to different types at the same moment. Which instance a
     * requirement dispatches to therefore depends on telling the two apart.
     * <p>
     * {@code Holder<T: Show>} is built over int, whose instance doubles; {@code convert<T: Show>} is
     * called with string, whose instance answers 7. Reading the method's parameter as the class's
     * would dispatch through the int instance and answer 42 instead.
     */
    @Test
    public void aBoundedMethodParameterMayShareTheClassParameterName() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns int",
            "implements Show<int>",
            "    function show(int x) returns int",
            "        return x * 2",
            "implements Show<string>",
            "    function show(string x) returns int",
            "        return 7",
            "class Holder<T: Show>",
            "    T held",
            "    construct(T held)",
            "        this.held = held",
            "    function shown() returns int",
            "        return T.show(held)",
            // A different parameter which happens to share the name, bound to a different instance.
            "    function convert<T: Show>(T other) returns int",
            "        return T.show(other)",
            "init",
            "    let h = new Holder<int>(21)",
            "    if h.shown() == 42 and h.convert<string>(\"x\") == 7",
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

    /**
     * A bounded generic which is only declared, never called, stays valid: it is unreachable, so
     * nothing has to supply a concrete type for it.
     */
    @Test
    public void unusedBoundedGenericFunctionLua() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "function unused<Q: Show>(Q x) returns string",
            "    return Q.show(x)",
            "init",
            "    testSuccess()"
        );
    }

    /** The same for a bounded generic class which is never constructed. */
    @Test
    public void unusedBoundedGenericClassLua() {
        test().testLua(true).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "class Unused<Q: Show>",
            "    function render(Q x) returns string",
            "        return Q.show(x)",
            "init",
            "    testSuccess()"
        );
    }

    /** Declared and unused on Jass too, which is where it already worked. */
    @Test
    public void unusedBoundedGenericFunction() {
        testAssertOkLines(true,
            "package test",
            "native testSuccess()",
            "interface Show<T:>",
            "    function show(T x) returns string",
            "implements Show<int>",
            "    function show(int x) returns string",
            "        return \"i\"",
            "function unused<Q: Show>(Q x) returns string",
            "    return Q.show(x)",
            "init",
            "    testSuccess()"
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
