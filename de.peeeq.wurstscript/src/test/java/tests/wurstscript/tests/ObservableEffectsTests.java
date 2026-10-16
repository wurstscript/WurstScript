package tests.wurstscript.tests;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.purity.ChangesTheWorld;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class ObservableEffectsTests extends WurstScriptTest {
    @DataProvider
    public Object[][] luaModes() {
        return new Object[][] {{false, false}, {true, false}, {true, true}};
    }

    @DataProvider
    public Object[][] genericLuaModes() {
        List<Object[]> modes = new ArrayList<>();
        for (Object[] mode : luaModes()) {
            for (boolean oldGenerics : new boolean[] {false, true}) {
                modes.add(new Object[] {mode[0], mode[1], oldGenerics});
            }
        }
        return modes.toArray(Object[][]::new);
    }

    private TestConfig bothBackends(String name, boolean optimized, boolean stacktraces) {
        TestConfig config = testNamed(name + "_" + optimized + "_" + stacktraces)
            .testLua(true).luaOnly(false).executeProg();
        if (optimized) config.inline().localOptimizations();
        if (stacktraces) config.stacktraces();
        return config;
    }

    @Test(dataProvider = "luaModes")
    public void unreadVirtualResultPreservesOverrideWrite(boolean optimized, boolean stacktraces) throws IOException {
        bothBackends("unreadVirtualResultPreservesOverrideWrite", optimized, stacktraces).lines(
            "package Test", "native testSuccess()", "native testFail(string message)",
            "int hits = 0",
            "class Base",
            "    function value() returns int", "        return 1",
            "class Child extends Base",
            "    override function value() returns int", "        hits++", "        return 2",
            "@noinline function choose(boolean child) returns Base",
            "    return child ? new Child() : new Base()",
            "init", "    Base m = choose(true)", "    let unused = m.value()",
            "    if hits != 1", "        testFail(\"virtual override write was removed\")",
            "    testSuccess()");
        assertTrue(luaInitializer("unreadVirtualResultPreservesOverrideWrite", optimized, stacktraces)
            .contains("__wurst_objectClass["), "the effectful virtual call must survive");
    }

    @Test(dataProvider = "luaModes")
    public void unreadAllocationPreservesObjectIds(boolean optimized, boolean stacktraces) throws IOException {
        bothBackends("unreadAllocationPreservesObjectIds", optimized, stacktraces)
            .executeProgOnlyAfterTransforms().lines(
            "package Test", "native testSuccess()", "native testFail(string message)",
            "class Empty",
            "init", "    let first = new Empty()", "    new Empty()", "    let last = new Empty()",
            "    if (last castTo int) != (first castTo int) + 2",
            "        testFail(\"unused allocation must still consume an id\")",
            "    testSuccess()");
        if (optimized && !stacktraces) {
            String body = luaInitializer("unreadAllocationPreservesObjectIds", true, false);
            assertEquals(body.split("Empty:create[0-9]*\\(", -1).length - 1, 3,
                "all three allocations must survive inlined constructors");
        }
    }

    @Test(dataProvider = "luaModes")
    public void unreadDeepOverridePreservesMemberWrite(boolean optimized, boolean stacktraces) {
        bothBackends("unreadDeepOverridePreservesMemberWrite", optimized, stacktraces).lines(
            "package Test", "native testSuccess()", "native testFail(string message)",
            "class Box", "    int hits = 0", "Box box = new Box()",
            "class Base", "    function value() returns int", "        return 0",
            "class Middle extends Base", "    override function value() returns int", "        return 1",
            "class Child extends Middle", "    override function value() returns int",
            "        return mutate()",
            "@noinline function mutate() returns int", "    box.hits++", "    return 2",
            "@noinline function choose(boolean child) returns Base",
            "    return child ? new Child() : new Middle()",
            "@noinline function discard(Base m)", "    let unused = m.value()",
            "init", "    discard(choose(true))", "    if box.hits != 1",
            "        testFail(\"transitive deep override member write was removed\")", "    testSuccess()");
    }

    @Test(dataProvider = "luaModes")
    public void unreadOverridePreservesNativeCall(boolean optimized, boolean stacktraces) {
        bothBackends("unreadOverridePreservesNativeCall", optimized, stacktraces).lines(
            "package Test", "native testSuccess()",
            "interface Base", "    function value() returns int",
            "class Child implements Base", "    override function value() returns int",
            "        reached()", "        return 1",
            "class Other implements Base", "    override function value() returns int", "        return 0",
            "@noinline function reached()", "    testSuccess()",
            "@noinline function choose(boolean child) returns Base",
            "    return child ? new Child() : new Other()",
            "init", "    Base m = choose(true)", "    let unused = m.value()");
    }

    @Test(dataProvider = "genericLuaModes")
    public void unreadGenericModuleOverridePreservesWrite(boolean optimized, boolean stacktraces, boolean oldGenerics) {
        bothBackends("unreadGenericModuleOverridePreservesWrite_" + oldGenerics, optimized, stacktraces).lines(
            "package Test", "native testSuccess()", "native testFail(string message)", "int hits = 0",
            "interface Root<T" + (oldGenerics ? "" : ":") + ">", "    function value(T x) returns int",
            "class Parent<U" + (oldGenerics ? "" : ":") + "> implements Root<U>",
            "    override function value(U x) returns int", "        return 0",
            "module Impl", "    function value(int x) returns int", "        hits += x", "        return 1",
            "class Child extends Parent<int>", "    use Impl",
            "@noinline function choose(boolean child) returns Root<int>",
            "    return child ? new Child() : new Parent<int>()",
            "init", "    Root<int> m = choose(true)", "    let unused = m.value(7)",
            "    if hits != 7", "        testFail(\"generic module override effect was removed\")", "    testSuccess()");
    }

    @Test(dataProvider = "luaModes")
    public void unreadAllocationsPreserveCallsAssignmentsAndReuse(boolean optimized, boolean stacktraces) {
        bothBackends("unreadAllocationsPreserveCallsAssignmentsAndReuse", optimized, stacktraces)
            .executeProgOnlyAfterTransforms().lines(
            "package Test", "native testSuccess()", "native testFail(string message)",
            "class Empty", "Empty unreadGlobal = null",
            "@noinline function leak() returns int", "    new Empty()", "    return 1",
            "@noinline function release(Empty value) returns int", "    destroy value", "    return 0",
            "function allocate() returns Empty", "    return new Empty()",
            "init", "    let first = new Empty()", "    unreadGlobal = new Empty()",
            "    var unused = allocate()", "    unused = allocate()", "    let ignored = leak()",
            "    let last = new Empty()",
            "    if (last castTo int) != (first castTo int) + 5",
            "        testFail(\"unread allocation assignment or call was removed\")",
            "    let lastId = last castTo int", "    destroy last", "    new Empty()", "    let after = new Empty()",
            "    if (after castTo int) != lastId + 1",
            "        testFail(\"unused allocation must consume the recycled id\")",
            "    let afterId = after castTo int", "    let released = release(after)", "    let recycled = new Empty()",
            "    if (recycled castTo int) != afterId", "        testFail(\"unread deallocation call was removed\")",
            "    testSuccess()");
    }

    @Test(dataProvider = "luaModes")
    public void allocationsStayInArgumentEvaluationOrder(boolean optimized, boolean stacktraces) {
        bothBackends("allocationsStayInArgumentEvaluationOrder", optimized, stacktraces)
            .executeProgOnlyAfterTransforms().lines(
            "package Test", "native testSuccess()", "native testFail(string message)", "class Empty",
            "function later() returns Empty", "    new Empty()", "    return new Empty()",
            "@noinline function verify(Empty first, Empty last)",
            "    if (last castTo int) != (first castTo int) + 2",
            "        testFail(\"allocations moved across argument statements\")",
            "init", "    verify(new Empty(), later())", "    testSuccess()");
    }

    @Test
    public void lifetimeEffectsRemainObservableAndOrderedInIm() {
        WurstModel model = Ast.WurstModel();
        ImTranslator translator = new ImTranslator(model, true, new RunArgs().with("-lua"));
        ImProg prog = translator.getImProg();
        ImClass clazz = JassIm.ImClass(model, "Empty", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(),
            JassIm.ImFunctions(), new ArrayList<>());
        prog.getClasses().add(clazz);
        ImClassType type = JassIm.ImClassType(clazz, JassIm.ImTypeArguments());
        ImAlloc alloc = JassIm.ImAlloc(model, type);
        ImDealloc dealloc = JassIm.ImDealloc(model, type, JassIm.ImIntVal(1));
        SideEffectAnalyzer analyzer = new SideEffectAnalyzer(prog);
        for (ImExpr expr : List.of(alloc, dealloc)) {
            assertTrue(analyzer.hasSideEffects(expr));
            assertTrue(analyzer.hasObservableSideEffects(expr, f -> true));
            assertTrue(expr.attrPurity() instanceof ChangesTheWorld);
        }

        ImFunction function = function(model, "allocate");
        function.setReturnType(type);
        function.getBody().add(JassIm.ImReturn(model, alloc));
        prog.getFunctions().add(function);
        assertTrue(analyzer.hasObservableSideEffects(JassIm.ImFunctionCall(model, function,
            JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL), f -> true));
    }

    @Test
    public void overrideCallGraphIncludesDeepAndSharedImplementationsOnce() {
        WurstModel model = Ast.WurstModel();
        ImProg prog = new ImTranslator(model, true, new RunArgs().with("-lua")).getImProg();
        ImClass clazz = JassIm.ImClass(model, "Base", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(),
            JassIm.ImFunctions(), new ArrayList<>());
        prog.getClasses().add(clazz);
        ImClassType type = JassIm.ImClassType(clazz, JassIm.ImTypeArguments());
        ImFunction base = function(model, "base"), left = function(model, "left"),
            right = function(model, "right"), leaf = function(model, "leaf");
        prog.getFunctions().addAll(List.of(base, left, right, leaf));
        ImMethod root = method(model, type, base), a = method(model, type, left),
            b = method(model, type, right), deep = method(model, type, leaf);
        root.getSubMethods().addAll(List.of(a, b));
        a.getSubMethods().add(deep);
        b.getSubMethods().add(deep);
        ImMethodCall call = JassIm.ImMethodCall(model, root, JassIm.ImTypeArguments(), JassIm.ImIntVal(1),
            JassIm.ImExprs(), false);
        SideEffectAnalyzer analyzer = new SideEffectAnalyzer(prog);
        assertEquals(new ArrayList<>(analyzer.directlyCalledFunctions(call)), List.of(base, left, leaf, right));
        assertFalse(analyzer.hasObservableSideEffects(call, f -> false), "inert overrides remain inert");
        leaf.getBody().add(JassIm.ImDealloc(model, type, JassIm.ImIntVal(1)));
        assertTrue(new SideEffectAnalyzer(prog).hasObservableSideEffects(call, f -> false));
    }

    private ImFunction function(WurstModel trace, String name) {
        return JassIm.ImFunction(trace, name, JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), List.of());
    }

    private ImMethod method(WurstModel trace, ImClassType type, ImFunction implementation) {
        return JassIm.ImMethod(trace, type, implementation.getName(), implementation,
            new ArrayList<>(), new ArrayList<>(), "", false);
    }

    @Test
    public void inertVirtualResultsAreStillRemoved() throws IOException {
        bothBackends("inertVirtualResultsAreStillRemoved", true, false).lines(
            "package Test", "native testSuccess()",
            "class Base", "    function value() returns int", "        return 1",
            "class Child extends Base", "    override function value() returns int", "        return 2",
            "@noinline function choose(boolean child) returns Base",
            "    return child ? new Child() : new Base()",
            "init", "    Base m = choose(true)", "    let unused = m.value()", "    testSuccess()");
        assertFalse(luaInitializer("inertVirtualResultsAreStillRemoved", true, false)
            .contains("__wurst_objectClass["), "effect-free virtual calls need not be retained");
    }

    private String luaInitializer(String name, boolean optimized, boolean stacktraces) throws IOException {
        String code = Files.readString(Path.of("test-output", "lua", "ObservableEffectsTests_" + name
            + "_" + optimized + "_" + stacktraces + ".lua"));
        int start = code.indexOf("function init_Test(");
        assertTrue(start >= 0, "expected package initializer");
        return code.substring(start, code.indexOf("\nend", start));
    }
}
