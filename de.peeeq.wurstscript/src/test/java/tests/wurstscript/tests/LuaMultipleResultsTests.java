package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.luaAst.LuaCompilationUnit;
import org.testng.annotations.Test;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * A function returning a tuple returns its components as Lua multiple results, and a call takes them into locals of
 * its own ({@code local a, b = f()}), instead of passing every component but the first through a global of the
 * function. Each test runs the emitted Lua; most also check its shape.
 */
public class LuaMultipleResultsTests extends WurstScriptTest {

    private String compiledLua(String testName) throws IOException {
        return Files.toString(new File("test-output/lua/LuaMultipleResultsTests_" + testName + ".lua"), Charsets.UTF_8);
    }

    private String compileLua(RunArgs runArgs, String... lines) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, runArgs);
        WurstModel model = parseFiles(Collections.emptyList(),
            Collections.singletonList(new CU("test.wurst", String.join("\n", lines))), false, compiler);
        assertTrue("unexpected errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.checkProg(model);
        assertTrue("unexpected errors: " + gui.getErrorList(), gui.getErrorList().isEmpty());
        compiler.translateProgToIm(model);
        compiler.runCompiletime(WurstProjectConfigData.empty(), false, false);
        LuaCompilationUnit luaCode = compiler.transformProgToLua();
        StringBuilder result = new StringBuilder();
        luaCode.print(result, 0);
        return result.toString();
    }

    /** The body of the Lua function {@code name}. */
    private static String functionBody(String lua, String name) {
        Matcher m = Pattern.compile("(?m)^function " + Pattern.quote(name) + "\\(([^)]*)\\)\\s*\\n(.*?)\\nend$",
            Pattern.DOTALL).matcher(lua);
        assertTrue("no function " + name + " in\n" + lua, m.find());
        return m.group(2);
    }

    private static void assertNoReturnGlobals(String lua) {
        assertFalse("a tuple component must not travel through a return global:\n" + lua,
            Pattern.compile("\\w+_return_\\w+").matcher(lua).find());
    }

    @Test
    public void aTupleIsReturnedAsMultipleResults() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "@noinline function makePair(int a, int b) returns pair",
            "    return pair(a + 1, b + 2)",
            "init",
            "    let p = makePair(4, 10)",
            "    if p.x == 5 and p.y == 12",
            "        testSuccess()"
        );
        String lua = compiledLua("aTupleIsReturnedAsMultipleResults");
        assertNoReturnGlobals(lua);
        assertTrue("the components are returned together:\n" + lua,
            Pattern.compile("(?m)^\\s*return \\(?\\w+ \\+ 1\\)?, \\(?\\w+ \\+ 2\\)?\\s*$").matcher(functionBody(lua, "makePair")).find());
        assertTrue("a call takes both results:\n" + lua,
            Pattern.compile("\\w+, \\w+ = makePair\\(").matcher(lua).find());
    }

    @Test
    public void nestedTuplesAreReturnedAsTheirScalarComponents() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple vec(real x, real y)",
            "tuple seg(vec a, vec b)",
            "@noinline function makeSeg(real s) returns seg",
            "    return seg(vec(s, s + 1.), vec(s + 2., s + 3.))",
            "init",
            "    let g = makeSeg(10.)",
            "    if g.a.x == 10. and g.a.y == 11. and g.b.x == 12. and g.b.y == 13. and g.b == vec(12., 13.)",
            "        testSuccess()"
        );
        String lua = compiledLua("nestedTuplesAreReturnedAsTheirScalarComponents");
        assertNoReturnGlobals(lua);
        assertTrue("a call takes all four results:\n" + lua,
            Pattern.compile("\\w+, \\w+, \\w+, \\w+ = makeSeg\\(").matcher(lua).find());
    }

    @Test
    public void aMethodReturnsMultipleResultsThroughDispatch() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "abstract class Shape",
            "    abstract function size(int scale) returns pair",
            "class Square extends Shape",
            "    override function size(int scale) returns pair",
            "        return pair(scale, scale)",
            "class Strip extends Shape",
            "    override function size(int scale) returns pair",
            "        return pair(scale * 3, 1)",
            "@noinline function area(Shape s) returns int",
            "    let p = s.size(2)",
            "    return p.x * p.y",
            "init",
            "    if area(new Square()) == 4 and area(new Strip()) == 6",
            "        testSuccess()"
        );
        String lua = compiledLua("aMethodReturnsMultipleResultsThroughDispatch");
        assertNoReturnGlobals(lua);
        assertTrue("a dispatched call takes both results:\n" + lua,
            Pattern.compile("\\w+, \\w+ = __wurst_objectClass\\[[^\\]]+\\]\\.\\w+\\(").matcher(lua).find());
    }

    @Test
    public void aClosureReturnsMultipleResults() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "interface Maker",
            "    function make(int x) returns pair",
            "@noinline function useMaker(Maker m) returns int",
            "    let p = m.make(5)",
            "    return p.x + p.y * 10",
            "init",
            "    int offset = 2",
            "    Maker m = x -> pair(x + offset, x)",
            "    if useMaker(m) == 57",
            "        testSuccess()"
        );
        assertNoReturnGlobals(compiledLua("aClosureReturnsMultipleResults"));
    }

    @Test
    public void resultsPassThroughReturnsAndRecursion() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "@noinline function fib(int n) returns pair",
            "    if n == 0",
            "        return pair(0, 1)",
            "    let p = fib(n - 1)",
            "    return pair(p.y, p.x + p.y)",
            "@noinline function forward(int n) returns pair",
            "    return fib(n)",
            "@noinline function swapped(int n) returns pair",
            "    let p = forward(n)",
            "    return pair(p.y, p.x)",
            "init",
            "    let a = forward(10)",
            "    let b = swapped(10)",
            "    if a == pair(55, 89) and b == pair(89, 55)",
            "        testSuccess()"
        );
        assertNoReturnGlobals(compiledLua("resultsPassThroughReturnsAndRecursion"));
    }

    @Test
    public void callsWhoseResultsAreUnusedStillRunInOrder() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int trace = 0",
            "@noinline function advance(int v) returns pair",
            "    trace = trace * 10 + v",
            "    return pair(v, trace)",
            "@noinline function both(pair a, pair b) returns int",
            "    return a.x * 1000 + a.y * 100 + b.x * 10 + b.y",
            "init",
            "    advance(1)",
            "    let second = advance(2).y",
            "    let r = both(advance(3), advance(4))",
            "    if second == 12 and r == 3 * 1000 + 123 * 100 + 4 * 10 + 1234 and trace == 1234",
            "        testSuccess()"
        );
        assertNoReturnGlobals(compiledLua("callsWhoseResultsAreUnusedStillRunInOrder"));
    }

    @Test
    public void aResultNoCallerReadsIsNotReturned() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple triple(int x, int y, int z)",
            "@noinline function make(int a) returns triple",
            "    return triple(a + 1, a * 7, a + 3)",
            "@noinline function relay(int a) returns triple",
            "    return make(a)",
            "init",
            "    let t = relay(4)",
            "    if t.x == 5 and t.z == 7",
            "        testSuccess()"
        );
        String lua = compiledLua("aResultNoCallerReadsIsNotReturned");
        assertNoReturnGlobals(lua);
        assertFalse("the component nobody reads is not computed:\n" + lua, lua.contains("* 7"));
        assertTrue("make returns the two read components:\n" + lua,
            Pattern.compile("(?m)^\\s*return \\(?\\w+ \\+ 1\\)?, \\(?\\w+ \\+ 3\\)?\\s*$").matcher(functionBody(lua, "make")).find());
    }

    @Test
    public void aResultWithAnEffectWhichNoCallerReadsStillRunsOnce() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "int calls = 0",
            "int trace = 0",
            "@noinline function count() returns int",
            "    calls++",
            "    trace = trace * 10 + 2",
            "    return calls",
            "@noinline function first() returns int",
            "    trace = trace * 10 + 1",
            "    return 5",
            "@noinline function make() returns pair",
            "    return pair(first(), count())",
            "init",
            "    let x = make().x",
            "    if x == 5 and calls == 1 and trace == 12",
            "        testSuccess()"
        );
        assertNoReturnGlobals(compiledLua("aResultWithAnEffectWhichNoCallerReadsStillRunsOnce"));
    }

    @Test
    public void aResultReadThroughOneOverrideIsReturnedByEveryOverride() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple pair(int x, int y)",
            "abstract class Base",
            "    abstract function get() returns pair",
            "class A extends Base",
            "    override function get() returns pair",
            "        return pair(1, 2)",
            "class B extends Base",
            "    override function get() returns pair",
            "        return pair(3, 4)",
            "@noinline function onlyX(A a) returns int",
            "    return a.get().x",
            "@noinline function bothThroughBase(Base b) returns int",
            "    let p = b.get()",
            "    return p.x * 10 + p.y",
            "init",
            "    if onlyX(new A()) == 1 and bothThroughBase(new A()) == 12 and bothThroughBase(new B()) == 34",
            "        testSuccess()"
        );
        assertNoReturnGlobals(compiledLua("aResultReadThroughOneOverrideIsReturnedByEveryOverride"));
    }

    @Test
    public void aTupleOfOneComponentIsAnOrdinaryReturn() throws IOException {
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "tuple single(int v)",
            "@noinline function wrap(int a) returns single",
            "    return single(a * 2)",
            "init",
            "    if wrap(21).v == 42",
            "        testSuccess()"
        );
        String lua = compiledLua("aTupleOfOneComponentIsAnOrdinaryReturn");
        assertNoReturnGlobals(lua);
        assertTrue(lua, Pattern.compile("(?m)^\\s*return \\(?\\w+ \\* 2\\)?\\s*$").matcher(functionBody(lua, "wrap")).find());
    }

    @Test
    public void resultsAreStoredInTheLocalsTableOfAFunctionOverTheLocalLimit() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("package Test");
        lines.add("native testSuccess()");
        lines.add("tuple pair(int x, int y)");
        lines.add("@noinline function makePair(int a) returns pair");
        lines.add("    return pair(a, a + 1)");
        lines.add("@noinline function big(int seed) returns int");
        StringBuilder sum = new StringBuilder("p.x + p.y");
        for (int i = 0; i < 210; i++) {
            lines.add("    int v" + i + " = seed + " + i);
            sum.append(" + v").append(i);
        }
        lines.add("    let p = makePair(seed)");
        lines.add("    return " + sum);
        lines.add("init");
        // 2 * 1 + 1 + 210 * 1 + (0 + ... + 209)
        lines.add("    if big(1) == " + (1 + 2 + 210 + 209 * 210 / 2));
        lines.add("        testSuccess()");
        test().testLua(true).executeProg().lines(lines.toArray(new String[0]));
        String lua = compiledLua("resultsAreStoredInTheLocalsTableOfAFunctionOverTheLocalLimit");
        assertNoReturnGlobals(lua);
        assertTrue("both results go to slots of the locals table:\n" + functionBody(lua, "big"),
            Pattern.compile("__wurst_locals\\d*\\[\\d+\\], __wurst_locals\\d*\\[\\d+\\] = makePair\\(")
                .matcher(functionBody(lua, "big")).find());
    }

    @Test
    public void optimizedCallsTakeTheResultsIntoLocals() {
        String lua = compileLua(new RunArgs().with("-lua", "-inline", "-localOptimizations"),
            "package Test",
            "tuple vec2(real x, real y)",
            "native consume(real x, real y)",
            "@noinline function add(vec2 left, vec2 right) returns vec2",
            "    return vec2(left.x + right.x, left.y + right.y)",
            "init",
            "    let result = add(vec2(1., 2.), vec2(3., 4.))",
            "    consume(result.x, result.y)");
        assertNoReturnGlobals(lua);
        String add = functionBody(lua, "add");
        assertEquals("add returns once, both components:\n" + add, 1, add.split("\\breturn\\b", -1).length - 1);
        assertTrue(add, Pattern.compile("(?m)^\\s*return [^,\\n]+, [^,\\n]+$").matcher(add).find());
        assertTrue("the call takes both results:\n" + lua,
            Pattern.compile("(\\w+), (\\w+) = add\\(").matcher(lua).find());
    }

    @Test
    public void emissionIsDeterministic() {
        String[] source = {
            "package Test",
            "tuple pair(int x, int y)",
            "tuple triple(int x, int y, int z)",
            "native consume(int x)",
            "abstract class Base",
            "    abstract function get(int s) returns triple",
            "class A extends Base",
            "    override function get(int s) returns triple",
            "        return triple(s, s + 1, s + 2)",
            "class B extends Base",
            "    override function get(int s) returns triple",
            "        return triple(s * 2, s * 3, s * 4)",
            "@noinline function make(int a) returns pair",
            "    return pair(a, a + 1)",
            "init",
            "    Base b = new A()",
            "    let t = b.get(3)",
            "    let p = make(t.x)",
            "    consume(p.y + t.z)",
            "    b = new B()",
            "    consume(b.get(4).y)"
        };
        RunArgs args = new RunArgs().with("-lua", "-inline", "-localOptimizations");
        String first = compileLua(args, source);
        String second = compileLua(args, source);
        assertEquals(first, second);
        assertNoReturnGlobals(first);
    }
}
