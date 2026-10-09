package tests.wurstscript.tests;

import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.translation.imoptimizer.TreeShaker;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * The tree shake which runs after the specialisation of generics ({@link TreeShaker}) removes functions nothing
 * reaches. These tests check what it keeps and what it drops on the program as the passes after it see it, and run
 * programs through both backends, where a function it dropped wrongly would be missing.
 */
public class TreeShakerTests extends WurstScriptTest {

    /** The program as translated to IM, where {@link TreeShaker} has not run yet. */
    private WurstCompilerJassImpl translate(boolean lua, String... lines) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        RunArgs runArgs = lua ? new RunArgs().with("-lua") : new RunArgs();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        WurstModel model = parseFiles(Collections.emptyList(),
            Collections.singletonList(new CU("TreeShakerTests.wurst", String.join("\n", lines))), false, compiler);
        assertTrue("unexpected parse/type errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.checkProg(model);
        assertTrue("unexpected compile errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.translateProgToIm(model);
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        return compiler;
    }

    private static Set<String> functionNames(WurstCompilerJassImpl compiler) {
        Set<String> names = new TreeSet<>();
        for (ImFunction f : compiler.getImProg().getFunctions()) {
            names.add(f.getName());
        }
        for (ImClass c : compiler.getImProg().getClasses()) {
            for (ImFunction f : c.getFunctions()) {
                names.add(f.getName());
            }
        }
        return names;
    }

    private static boolean anyNameStartsWith(Set<String> names, String prefix) {
        return names.stream().anyMatch(name -> name.startsWith(prefix));
    }

    @Test
    public void removesTheFunctionsNothingCalls() {
        WurstCompilerJassImpl compiler = translate(false,
            "package Test",
            "native testSuccess()",
            "function calledFromInit() returns int",
            "    return calledByTheCallee() + 1",
            "function calledByTheCallee() returns int",
            "    return 41",
            "function neverCalled() returns int",
            "    return neverCalledEither() + 1",
            "function neverCalledEither() returns int",
            "    return 7",
            "init",
            "    if calledFromInit() == 42",
            "        testSuccess()");
        Set<String> before = functionNames(compiler);
        assertTrue(before.toString(), before.stream().anyMatch(name -> name.startsWith("neverCalled")));

        int removed = TreeShaker.removeUnreachableFunctions(compiler.getImTranslator());

        Set<String> after = functionNames(compiler);
        assertFalse("what nothing calls stays: " + after, anyNameStartsWith(after, "neverCalled"));
        assertTrue("what init calls, and what that calls, stays: " + after,
            anyNameStartsWith(after, "calledFromInit") && anyNameStartsWith(after, "calledByTheCallee"));
        assertTrue("two functions at least are removed: " + removed, removed >= 2);
    }

    /** A method which is called through its base class is implemented by the override nobody names. */
    @Test
    public void keepsTheOverridesOfAMethodWhichIsCalledThroughItsBase() {
        WurstCompilerJassImpl compiler = translate(false,
            "package Test",
            "native testSuccess()",
            "abstract class Base",
            "    abstract function run() returns int",
            "    function neverCalledOnBase() returns int",
            "        return 1",
            "class Derived extends Base",
            "    override function run() returns int",
            "        return derivedHelper()",
            "    function neverCalledOnDerived() returns int",
            "        return 2",
            "function derivedHelper() returns int",
            "    return 42",
            "init",
            "    Base b = new Derived()",
            "    if b.run() == 42",
            "        testSuccess()");

        TreeShaker.removeUnreachableFunctions(compiler.getImTranslator());

        Set<String> after = functionNames(compiler);
        assertTrue("the override is reached through the call on the base: " + after, anyNameStartsWith(after, "Derived_run")
            || after.stream().anyMatch(name -> name.contains("run")));
        assertTrue("what the override calls stays: " + after, anyNameStartsWith(after, "derivedHelper"));
        assertFalse("a method nothing calls goes: " + after, anyNameStartsWith(after, "neverCalledOnDerived"));
        assertFalse("a method nothing calls goes: " + after, anyNameStartsWith(after, "neverCalledOnBase"));
    }

    /** A function used as a callback is reached by its reference, not by a call. */
    @Test
    public void keepsAFunctionWhichIsOnlyReferenced() {
        WurstCompilerJassImpl compiler = translate(false,
            "package Test",
            "native testSuccess()",
            "native takesCode(code c)",
            "function onlyReferenced()",
            "    testSuccess()",
            "function neverReferenced()",
            "    testSuccess()",
            "init",
            "    takesCode(function onlyReferenced)");

        TreeShaker.removeUnreachableFunctions(compiler.getImTranslator());

        Set<String> after = functionNames(compiler);
        assertTrue(after.toString(), anyNameStartsWith(after, "onlyReferenced"));
        assertFalse(after.toString(), anyNameStartsWith(after, "neverReferenced"));
    }

    /**
     * The Lua helpers which the later passes call (string concatenation, integer division) are created with the
     * program and called by no body yet, so they have to be pinned. Without that the Lua translation finds a call to a
     * function which was removed.
     */
    @Test
    public void theHelpersTheLuaPassesCallOutliveTheShake() {
        WurstCompilerJassImpl compiler = translate(true,
            "package Test",
            "native testSuccess()",
            "function neverCalled() returns int",
            "    return 5",
            "init",
            "    testSuccess()");
        Set<ImFunction> inTheProgram = Collections.newSetFromMap(new IdentityHashMap<>());
        inTheProgram.addAll(compiler.getImProg().getFunctions());
        // some are only created, and joined to the program by a pass which needs them
        Set<ImFunction> pinned = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ImFunction helper : compiler.getImTranslator().pinnedFunctions()) {
            if (inTheProgram.contains(helper)) {
                pinned.add(helper);
            }
        }
        assertTrue("the Lua helpers are part of the program: " + compiler.getImTranslator().pinnedFunctions(),
            pinned.size() > 5);

        TreeShaker.removeUnreachableFunctions(compiler.getImTranslator());

        Set<ImFunction> remaining = Collections.newSetFromMap(new IdentityHashMap<>());
        remaining.addAll(compiler.getImProg().getFunctions());
        Set<String> removedPinned = new LinkedHashSet<>();
        for (ImFunction helper : pinned) {
            if (!remaining.contains(helper)) {
                removedPinned.add(helper.getName());
            }
        }
        assertEquals("pinned functions are removed: " + removedPinned, 0, removedPinned.size());
    }

    private static final String[] RUNTIME_HELPERS = {
        "package Test",
        "native testSuccess()",
        "class Box<T>",
        "    T value",
        "    construct(T value)",
        "        this.value = value",
        "function neverCalled(string s) returns string",
        "    return s + \"x\"",
        "@noinline function concat(string a, string b) returns string",
        "    return a + b",
        "@noinline function divide(int a, int b) returns int",
        "    return a div b",
        "@noinline function remainder(int a, int b) returns int",
        "    return a mod b",
        "class Cell",
        "    string name = \"cell\"",
        "init",
        "    let boxInt = new Box<int>(7)",
        "    let boxCell = new Box<Cell>(new Cell())",
        "    if concat(\"a\", \"b\") == \"ab\" and concat(boxCell.value.name, \"!\") == \"cell!\"",
        "        and divide(-7, 2) == -3 and remainder(-7, 2) == 1",
        "        and boxInt.value == 7",
        "        testSuccess()"};

    @Test
    public void luaRunsWithHelpersTheLowerPassesCall() {
        test().testLua(true).executeProg().lines(RUNTIME_HELPERS);
    }

    @Test
    public void jassRunsWithHelpersTheLowerPassesCall() {
        test().executeProg().lines(RUNTIME_HELPERS);
    }

    private static final String[] TYPE_CLASS_PROGRAM = {
        "package test",
        "native testSuccess()",
        "interface ToIndex<T:>",
        "    function toIndex(T x) returns int",
        "class A",
        "implements ToIndex<A>",
        "    function toIndex(A x) returns int",
        "        return 42",
        "class Unused",
        "implements ToIndex<Unused>",
        "    function toIndex(Unused x) returns int",
        "        return 7",
        "function foo<Q: ToIndex>(Q x) returns int",
        "    return Q.toIndex(x)",
        "function neverCalled() returns int",
        "    return 1",
        "init",
        "    if foo(new A) == 42",
        "        testSuccess()"};

    /**
     * Before the generics are specialised nothing calls the implementation of a type class: the generic body
     * dispatches through the binding which the call of {@code foo} carries in its type argument.
     */
    @Test
    public void beforeTheGenericsTheBindingOfATypeArgumentKeepsItsImplementation() {
        WurstCompilerJassImpl compiler = translate(true, TYPE_CLASS_PROGRAM);
        Set<String> before = functionNames(compiler);
        assertTrue(before.toString(), before.stream().filter(name -> name.contains("toIndex")).count() >= 2);

        TreeShaker.removeUnreachableFunctionsBeforeGenerics(compiler.getImTranslator());

        Set<String> after = functionNames(compiler);
        assertTrue("the implementation which the type argument binds stays: " + after,
            after.stream().anyMatch(name -> name.contains("toIndex")));
        assertFalse("what nothing calls goes: " + after, anyNameStartsWith(after, "neverCalled"));
    }

    @Test
    public void typeClassDispatchRunsOnBothBackendsWithTheShake() {
        test().testLua(true).luaOnly(false).executeProg().lines(TYPE_CLASS_PROGRAM);
    }

    /**
     * The implementation is named only by the type argument of the supertype ({@code Parent<Foo>}), which is part of
     * the class and not of any function body.
     */
    private static final String[] SUPERTYPE_BINDING_PROGRAM = {
        "package test",
        "native testSuccess()",
        "interface Show<T:>",
        "    function show(T x) returns int",
        "class Foo",
        "implements Show<Foo>",
        "    function show(Foo x) returns int",
        "        return 42",
        "class Unused",
        "implements Show<Unused>",
        "    function show(Unused x) returns int",
        "        return 7",
        "class Parent<T: Show>",
        "    function run(T x) returns int",
        "        return T.show(x)",
        "class Child extends Parent<Foo>",
        "function neverCalled() returns int",
        "    return 1",
        "init",
        "    let c = new Child()",
        "    if c.run(new Foo()) == 42",
        "        testSuccess()"};

    @Test
    public void aBindingOfASupertypeKeepsItsImplementation() {
        test().testLua(true).luaOnly(false).executeProg().lines(SUPERTYPE_BINDING_PROGRAM);
    }

    /** The bound type is itself a type argument of a type argument: {@code Box<Foo>} is what {@code wrap} is given. */
    private static final String[] NESTED_BINDING_PROGRAM = {
        "package test",
        "native testSuccess()",
        "interface Show<T:>",
        "    function show(T x) returns int",
        "class Foo",
        "implements Show<Foo>",
        "    function show(Foo x) returns int",
        "        return 42",
        "class Unused",
        "implements Show<Unused>",
        "    function show(Unused x) returns int",
        "        return 7",
        "class Box<T: Show>",
        "    T value",
        "    construct(T value)",
        "        this.value = value",
        "    function describe() returns int",
        "        return T.show(value)",
        "class Holder<B:>",
        "    B inner",
        "    construct(B inner)",
        "        this.inner = inner",
        "function wrap<B:>(B inner) returns Holder<B>",
        "    return new Holder<B>(inner)",
        "function neverCalled() returns int",
        "    return 1",
        "init",
        "    let h = wrap(new Box<Foo>(new Foo()))",
        "    if h.inner.describe() == 42",
        "        testSuccess()"};

    @Test
    public void aBindingNestedInATypeArgumentKeepsItsImplementation() {
        test().testLua(true).luaOnly(false).executeProg().lines(NESTED_BINDING_PROGRAM);
    }

    /**
     * Nothing allocates the generic parent itself, only subclasses of {@code Parent<int>}, and the call through the
     * parent is polymorphic, so the specialised method must be where both subclasses inherit it from.
     */
    @Test
    public void aPolymorphicCallOfAGenericParentWhichNothingAllocatesFindsItsSpecialisedMethod() {
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
            "class Child1 extends Parent<int>",
            "class Child2 extends Parent<int>",
            "function renderIt(Parent<int> p) returns string",
            "    return p.render(1)",
            "init",
            "    if renderIt(new Child1()) == \"i\" and renderIt(new Child2()) == \"i\"",
            "        testSuccess()");
    }

    private static final String[] GENERIC_NEW_PROGRAM = {
        "package MagicFunctions",
        "    @annotation function annotation()",
        "    @annotation function compilerintrinsic()",
        "    @compilerintrinsic function wurstNewInstance<T:>() returns T",
        "        return null",
        "endpackage",
        "package Test",
        "    import MagicFunctions",
        "    native testSuccess()",
        "    class State",
        "        int value = 4",
        "    class Unused",
        "        int value = 9",
        "    function make<T:>() returns T",
        "        return wurstNewInstance<T>()",
        "    function neverCalled() returns int",
        "        return new Unused().value",
        "    init",
        "        State s = make<State>()",
        "        if s.value == 4",
        "            testSuccess()",
        "endpackage"};

    /**
     * {@code wurstNewInstance<T>()} becomes a call of the function which constructs {@code T} when the generics are
     * specialised, so before that nothing calls the function which constructs the class named by the type argument.
     */
    @Test
    public void beforeTheGenericsAClassGivenAsATypeArgumentKeepsItsConstruction() {
        WurstCompilerJassImpl compiler = translate(true, GENERIC_NEW_PROGRAM);
        assertTrue(functionNames(compiler).toString(), functionNames(compiler).contains("new_State"));

        TreeShaker.removeUnreachableFunctionsBeforeGenerics(compiler.getImTranslator());

        Set<String> after = functionNames(compiler);
        assertTrue("the construction of State stays: " + after, after.contains("new_State"));
        assertFalse("the construction of a class nothing names goes: " + after, after.contains("new_Unused"));
    }

    @Test
    public void genericConstructionRunsOnBothBackendsWithTheShake() {
        test().testLua(true).luaOnly(false).executeProg().lines(GENERIC_NEW_PROGRAM);
    }

    private static Set<String> globalNames(WurstCompilerJassImpl compiler) {
        Set<String> names = new TreeSet<>();
        for (ImVar global : compiler.getImProg().getGlobals()) {
            names.add(global.getName());
        }
        return names;
    }

    private static final String[] GLOBALS_PROGRAM = {
        "package Test",
        "native testSuccess()",
        "function sideEffect() returns int",
        "    return 3",
        "int neverRead = 5",
        "int chainA = 1",
        "int chainB = chainA + 1",
        "int array table",
        "int counter = 0",
        "int derived = counter + 1",
        "int withEffect = sideEffect()",
        "int deadStatic = 4",
        "int zero = 0",
        "int mayTrap = 10 div zero",
        "int safeDivision = 10 div 2",
        "@preserveName int kept = 9",
        "function onlyDeadReads() returns int",
        "    return deadStatic",
        "init",
        "    table[0] = 7",
        "    if derived == 1",
        "        testSuccess()"};

    /**
     * A global which nothing reachable mentions goes, with the assignments of constants to it. A global which an
     * assignment with an effect, a read, or a name to preserve needs stays, and so does what a needed global's
     * initial value reads.
     */
    @Test
    public void removesTheGlobalsNothingReads() {
        for (boolean lua : new boolean[]{true, false}) {
            WurstCompilerJassImpl compiler = translate(lua, GLOBALS_PROGRAM);
            Set<String> before = globalNames(compiler);
            assertTrue(before.toString(), before.contains("Test_neverRead") && before.contains("Test_deadStatic"));

            TreeShaker.removeUnreachableFunctions(compiler.getImTranslator());

            Set<String> after = globalNames(compiler);
            String target = lua ? "Lua: " : "Jass: ";
            assertFalse(target + "a global nothing reads goes: " + after, after.contains("Test_neverRead"));
            assertFalse(target + "and a chain of them: " + after,
                after.contains("Test_chainA") || after.contains("Test_chainB"));
            assertFalse(target + "and an array which is only written: " + after, after.contains("Test_table"));
            assertFalse(target + "and a global which only a dead function reads: " + after, after.contains("Test_deadStatic"));
            assertTrue(target + "a global which is read stays: " + after, after.contains("Test_derived"));
            assertTrue(target + "and what its value reads: " + after, after.contains("Test_counter"));
            assertTrue(target + "a global with an effect in its initial value stays: " + after, after.contains("Test_withEffect"));
            assertTrue(target + "a global with a name to preserve stays: " + after, after.contains("Test_kept"));
            assertTrue(target + "a division which may stop the thread stays, and its divisor: " + after,
                after.contains("Test_mayTrap") && after.contains("Test_zero"));
            assertFalse(target + "a division by a constant goes: " + after, after.contains("Test_safeDivision"));
        }
    }

    /**
     * What is dropped is only what has no effect: the initial value which calls a function still runs when nothing
     * reads the global, and the counter which only dead assignments touch goes without a trace.
     */
    @Test
    public void aProgramWithGlobalsNothingReadsRunsOnBothBackends() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "int calls = 0",
            "function bump() returns int",
            "    calls++",
            "    return calls",
            "int unreadWithEffect = bump()",
            "int deadCounter = 0",
            "int array deadTable",
            "int base = 40",
            "int derived = base + 2",
            "function work()",
            "    deadCounter++",
            "    deadTable[2] = deadCounter + 1",
            "init",
            "    work()",
            "    work()",
            "    if calls == 1 and derived == 42",
            "        testSuccess()");
    }

    /** The class elimination builds the allocators from variables which no body mentions yet. */
    @Test
    public void theVariablesWhichManageTheInstancesOfAClassStayForTheClassElimination() {
        WurstCompilerJassImpl compiler = translateForCompiletimeFunctions(false,
            "package Test",
            "native testSuccess()",
            "class A",
            "    int value",
            "function compiletime(A a) returns A",
            "    return a",
            "let a = compiletime(new A)",
            "init",
            "    if a.value == 0",
            "        testSuccess()");
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        List<ImVar> pinned = compiler.getImTranslator().pinnedGlobals();
        assertTrue("the compile-time object created the management variables: " + pinned, pinned.size() >= 4);

        TreeShaker.removeUnreachableFunctions(compiler.getImTranslator());

        Set<ImVar> remaining = Collections.newSetFromMap(new IdentityHashMap<>());
        remaining.addAll(compiler.getImProg().getGlobals());
        for (ImVar variable : pinned) {
            assertTrue(variable.getName() + " is pinned", remaining.contains(variable));
        }
    }

    private static final String[] COMPILETIME_PROGRAM = {
        "package Test",
        "native testSuccess()",
        "function compiletime(int i) returns int",
        "    return i",
        "@compiletime int state = 0",
        "function viaTheFunction() returns int",
        "    return 20",
        "function viaTheExpression() returns int",
        "    return 22",
        "function nothingCallsThis() returns int",
        "    return 1",
        "function onlyThatOneCalls() returns int",
        "    return nothingCallsThis()",
        "@compiletime function fill()",
        "    state += viaTheFunction()",
        "function holdsAnExpression() returns int",
        "    return compiletime(viaTheExpression())",
        "function neverCalled() returns int",
        "    return onlyThatOneCalls()",
        "init",
        "    if state == 20",
        "        testSuccess()"};

    private WurstCompilerJassImpl translateForCompiletimeFunctions(boolean lua, String... lines) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        RunArgs runArgs = (lua ? new RunArgs().with("-lua") : new RunArgs()).with("-runcompiletimefunctions");
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        WurstModel model = parseFiles(Collections.emptyList(),
            Collections.singletonList(new CU("TreeShakerTests.wurst", String.join("\n", lines))), false, compiler);
        assertTrue("unexpected parse/type errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.checkProg(model);
        assertTrue("unexpected compile errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.translateProgToIm(model);
        return compiler;
    }

    /**
     * What the run starts from is no part of what main reaches: nothing calls a compile-time function, and the
     * expression in a function nothing calls is evaluated all the same.
     */
    @Test
    public void inFrontOfTheCompiletimeRunTheFunctionsItExecutesAndEvaluatesStay() {
        for (boolean lua : new boolean[]{true, false}) {
            WurstCompilerJassImpl compiler = translateForCompiletimeFunctions(lua, COMPILETIME_PROGRAM);
            assertTrue(functionNames(compiler).toString(), anyNameStartsWith(functionNames(compiler), "nothingCallsThis"));

            TreeShaker.removeUnreachableFunctionsBeforeCompiletime(compiler.getImTranslator(),
                CompiletimeFunctionRunner.functionsOfTheRun(compiler.getImProg(), CompiletimeFunctionRunner.FunctionFlagToRun.CompiletimeFunctions));

            Set<String> after = functionNames(compiler);
            assertTrue("the compile-time function stays: " + after, anyNameStartsWith(after, "fill"));
            assertTrue("what it calls stays: " + after, anyNameStartsWith(after, "viaTheFunction"));
            assertTrue("the function holding a compile-time expression stays: " + after, anyNameStartsWith(after, "holdsAnExpression"));
            assertTrue("what the expression calls stays: " + after, anyNameStartsWith(after, "viaTheExpression"));
            assertFalse("what nothing reaches goes: " + after, anyNameStartsWith(after, "neverCalled"));
            assertFalse("what only that calls goes: " + after, anyNameStartsWith(after, "onlyThatOneCalls"));
            assertFalse("and what only that calls: " + after, anyNameStartsWith(after, "nothingCallsThis"));
        }
    }

    /** {@code runCompiletime} is what drops them, on both targets. */
    @Test
    public void theCompiletimeRunDropsTheUnreachableFunctions() {
        for (boolean lua : new boolean[]{true, false}) {
            WurstCompilerJassImpl compiler = translateForCompiletimeFunctions(lua, COMPILETIME_PROGRAM);
            assertTrue(functionNames(compiler).toString(), anyNameStartsWith(functionNames(compiler), "neverCalled"));
            compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
            Set<String> after = functionNames(compiler);
            assertFalse((lua ? "Lua: " : "Jass: ") + after, anyNameStartsWith(after, "neverCalled"));
            assertTrue((lua ? "Lua: " : "Jass: ") + after, anyNameStartsWith(after, "fill"));
        }
    }

    @Test
    public void luaAndJassRunWithOverridesAndReferences() {
        String[] program = {
            "package Test",
            "native testSuccess()",
            "abstract class Animal",
            "    abstract function sound() returns int",
            "    function unusedOnAnimal() returns int",
            "        return -1",
            "class Dog extends Animal",
            "    override function sound() returns int",
            "        return 40",
            "    function unusedOnDog() returns int",
            "        return -2",
            "class Cat extends Animal",
            "    override function sound() returns int",
            "        return 2",
            "function total(Animal a, Animal b) returns int",
            "    return a.sound() + b.sound()",
            "function neverCalled() returns int",
            "    return new Dog().unusedOnDog()",
            "init",
            "    if total(new Dog(), new Cat()) == 42",
            "        testSuccess()"};
        test().testLua(true).executeProg().lines(program);
        test().executeProg().lines(program);
    }

    /**
     * The allocator which the class elimination builds after the shake calls the error function of the ErrorHandling
     * package when the instances run out, though nothing in the program calls it. A real project names it earlier,
     * in the package initialisers which the trigger natives of common.j run; this program has no common.j.
     */
    @Test
    public void theErrorFunctionWhichTheAllocatorCallsOutlivesTheShake() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package ErrorHandling",
            "    string lastError = \"\"",
            "    public function error(string msg)",
            "        lastError = msg",
            "endpackage",
            "package Test",
            "    native testSuccess()",
            "    class A",
            "        int x",
            "    init",
            "        let a = new A()",
            "        if a.x == 0",
            "            testSuccess()",
            "endpackage");
    }
}
