package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.lua.translation.LuaHotReload;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertTrue;


/**
 * These tests are supposed to check, whether the compiler is determinisitic
 */
public class DeterministicChecks extends WurstScriptTest {

    @Test
    public void hotReloadCompiletimeObjectSnapshotsAreDeterministic() {
        CU values = compilationUnit("Values.wurst", "package Values", "public class Node", "    public int value",
            "    construct(int v)", "        value = v", "public function make(int v) returns Node", "    return new Node(v)");
        CU main = compilationUnit("Main.wurst", "package Main", "import Values", "native observe(int v)",
            "function compiletime(Node n) returns Node", "    return n", "constant a = compiletime(make(1))",
            "constant b = compiletime(make(2))", "init", "    observe(a.value + b.value)");
        RunArgs args = new RunArgs().with("-lua", "-luaHotReload", "-stacktraces", "-runcompiletimefunctions");
        var first = compileLuaHotReloadManifest(false, args, values, main);
        assertEquals(compileLuaHotReloadManifest(false, args, main, values), first);
        assertEquals(compileLuaHotReloadManifest(false, args, values, main), first);
    }

    @Test
    public void hotReloadRemovalWarningsIgnoreHistoryMapOrder() throws Exception {
        Map<String, String> forward = new java.util.LinkedHashMap<>();
        forward.put("alpha", "markerA");
        forward.put("zulu", "markerZ");
        Map<String, String> reverse = new java.util.LinkedHashMap<>();
        reverse.put("zulu", "markerZ");
        reverse.put("alpha", "markerA");
        LuaHotReload.Manifest empty = new LuaHotReload.Manifest("abi", "v", Map.of(), List.of(), List.of(), List.of());
        var first = LuaHotReload.payload(empty, empty, new LuaHotReload.History(java.util.Set.of(), Map.of(), Map.of(), forward,
            new java.util.LinkedHashSet<>(List.of("alphaInit", "zuluInit"))), 1);
        var reordered = LuaHotReload.payload(empty, empty, new LuaHotReload.History(java.util.Set.of(), Map.of(), Map.of(), reverse,
            new java.util.LinkedHashSet<>(List.of("zuluInit", "alphaInit"))), 1);
        assertEquals(reordered.source(), first.source());
    }

    /** Initializer functions and their manifest revisions use source identities, independent of unit order. */
    @Test
    public void hotReloadInitializerUnitsAreDeterministic() {
        CU dependency = compilationUnit("Values.wurst", "package Values", "public int value = 3");
        CU program = compilationUnit("Initializer.wurst", "package Initializer", "import Values",
            "native observe(int value)", "tuple Pair(int a, int b)",
            "Pair pair = Pair(value, value + 1)",
            "int total = begin", "    var result = pair.a", "    if value > 0", "        result += pair.b",
            "    return result", "end", "init", "    observe(total)");
        RunArgs args = new RunArgs().with("-lua", "-luaHotReload");
        var first = compileLuaHotReloadManifest(false, args, dependency, program);
        var reversed = compileLuaHotReloadManifest(false, args, program, dependency);
        var repeated = compileLuaHotReloadManifest(false, args, dependency, program);
        assertEquals(reversed, first);
        assertEquals(repeated, first);
    }

    /** The constants a payload may run again, and what each reads, do not depend on unit order. */
    @Test
    public void hotReloadConstantsAreDeterministic() {
        CU dependency = compilationUnit("Values.wurst", "package Values", "public constant int base = 3",
            "public constant int scaled = base * 2", "public function twice(int x) returns int", "    return x * 2");
        CU program = compilationUnit("Constants.wurst", "package Constants", "import Values", "native observe(int value)",
            "constant int total = scaled + base", "constant int doubled = twice(total)", "int counter = total",
            "init", "    observe(doubled + counter)");
        RunArgs args = new RunArgs().with("-lua", "-luaHotReload");
        var first = compileLuaHotReloadManifest(false, args, dependency, program);
        var reversed = compileLuaHotReloadManifest(false, args, program, dependency);
        assertEquals(List.copyOf(reversed.constants().entrySet()), List.copyOf(first.constants().entrySet()));
        assertEquals(reversed, first);
        assertEquals(compileLuaHotReloadManifest(false, args, dependency, program), first);
        assertEquals(first.constants().size(), 4, first.constants().toString());
        assertTrue(first.constants().values().stream().allMatch(LuaHotReload.Constant::pure), first.constants().toString());
    }

    @Test
    public void hotReloadNamedClosureBindingsAreDeterministic() {
        CU closures = compilationUnit("NamedClosures.wurst", "package NamedClosures", "interface Op",
            "    function apply(int value) returns int", "public function run(int seed) returns int",
            "    Op a = (int value) -> seed + value", "    Op b = (int value) -> seed * value",
            "    let result = a.apply(2) + b.apply(3)", "    destroy a", "    destroy b", "    return result",
            "public function run(string text, int seed) returns int", "    Op a = (int value) -> seed - value",
            "    let result = a.apply(1)", "    destroy a", "    return result",
            "public class A", "    public static class N", "        int value",
            "    static function create() returns N", "        return new N()",
            "public class B", "    public static class N", "        int value",
            "    static function create() returns N", "        return new N()",
            "public function run(A.N item) returns int", "    Op a = (int value) -> item.value + value",
            "    let result = a.apply(2)", "    destroy a", "    return result",
            "public function run(B.N item) returns int", "    Op a = (int value) -> item.value - value",
            "    let result = a.apply(3)", "    destroy a", "    return result");
        CU main = compilationUnit("Main.wurst", "package Main", "import NamedClosures", "init", "    run(4)", "    run(\"x\", 5)",
            "    let a = A.create()", "    let b = B.create()", "    run(a)", "    run(b)", "    destroy a", "    destroy b");
        RunArgs args = new RunArgs().with("-lua", "-luaHotReload");
        var first = compileLuaHotReloadManifest(false, args, closures, main);
        assertEquals(compileLuaHotReloadManifest(false, args, main, closures), first);
        assertEquals(compileLuaHotReloadManifest(false, args, closures, main), first);
    }

    @Test
    public void branchNullFactsIgnoreCompilationUnitOrder() throws IOException {
        CU program = compilationUnit("NullFacts.wurst",
            "type unit extends handle",
            "package NullFacts",
            "@extern native GetUnitX(unit u) returns real",
            "native source() returns unit",
            "native consume(real value)",
            "function coordinate(unit u) returns real",
            "    return u == null ? 0. : GetUnitX(u)",
            "@noinline function update(unit u)",
            "    if u != null",
            "        consume(coordinate(u))",
            "init",
            "    update(source())");
        CU extra = compilationUnit("Extra.wurst", "package Extra", "native observe(int value)",
            "init", "    observe(7)");
        String name = "branchNullFacts";
        testNamed(name).testLua(true).inline().localOptimizations().compilationUnits(program, extra);
        String first = compiledLua(name);
        testNamed(name).testLua(true).inline().localOptimizations().compilationUnits(extra, program);
        assertEquals(compiledLua(name), first);
        testNamed(name).testLua(true).inline().localOptimizations().compilationUnits(program, extra);
        assertEquals(compiledLua(name), first);
    }

    @Test
    public void erasedDispatchConcreteStoresAreDeterministic() throws IOException {
        CU program = compilationUnit("Stores.wurst", GenericStoreGeneratedTests.program(7));
        CU extra = compilationUnit("Extra.wurst", "package Extra", "native consume(int value)",
            "init", "    consume(7)");
        String name = "erasedDispatchStores";
        testNamed(name).testLua(true).inline().localOptimizations().compilationUnits(program, extra);
        String forward = compiledLua(name);
        testNamed(name).testLua(true).inline().localOptimizations().compilationUnits(extra, program);
        assertEquals(compiledLua(name), forward);
    }

    @Test
    public void compressedLuaDescriptorsAreIdenticalAcrossCompilations() throws IOException {
        String name = "compressedLuaDescriptors";
        File lua = new File("test-output/lua/DeterministicChecks_" + name + ".lua");
        testNamed(name).testLua(true).optimize().inline().localOptimizations()
            .executeProg().lines(LuaNameCompressionTests.source());
        String first = Files.toString(lua, Charsets.UTF_8);
        testNamed(name).testLua(true).optimize().inline().localOptimizations()
            .executeProg().lines(LuaNameCompressionTests.source());
        assertEquals(Files.toString(lua, Charsets.UTF_8), first);
    }

    @Test
    public void moduleGenericRootDispatchIsIdenticalAcrossCompilations() throws IOException {
        String name = "moduleGenericRootDispatch";
        String[] source = DispatchImplementationTests.moduleStateSource(false);
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(source);
        File jass = new File("test-output/DeterministicChecks_" + name + "_no_opts.j");
        File lua = new File("test-output/lua/DeterministicChecks_" + name + ".lua");
        String firstJass = Files.toString(jass, Charsets.UTF_8);
        String firstLua = Files.toString(lua, Charsets.UTF_8);
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(source);
        assertEquals(Files.toString(jass, Charsets.UTF_8), firstJass);
        assertEquals(Files.toString(lua, Charsets.UTF_8), firstLua);
    }

    /** A bounded interface default, inherited by some implementing classes and overridden by others, emits one script. */
    @Test
    public void interfaceDefaultWithBoundIsIdenticalAcrossCompilations() throws IOException {
        String name = "interfaceDefaultWithBound";
        String[] source = GenericsTests.overriddenBoundedDefaultSource();
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(source);
        File jass = new File("test-output/DeterministicChecks_" + name + "_no_opts.j");
        File lua = new File("test-output/lua/DeterministicChecks_" + name + ".lua");
        String firstJass = Files.toString(jass, Charsets.UTF_8);
        String firstLua = Files.toString(lua, Charsets.UTF_8);
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(source);
        assertEquals(Files.toString(jass, Charsets.UTF_8), firstJass);
        assertEquals(Files.toString(lua, Charsets.UTF_8), firstLua);
    }

    /**
     * Sibling classes which take Showable's show from Shower's default, and one with a show of its own, emit the
     * same Jass and Lua in two compilations, and their sub-methods of Showable's show come out alike whatever order
     * the units are given in.
     */
    @Test
    public void subInterfaceDefaultDispatchIsIdenticalAcrossCompilations() throws IOException {
        String name = "subInterfaceDefaultDispatch";
        List<String> source = new ArrayList<>(List.of(
            "package test",
            "native testSuccess()",
            "interface Showable",
            "    function show() returns int",
            "interface Shower extends Showable",
            "    function show() returns int",
            "        return 7",
            "class Own implements Shower",
            "    override function show() returns int",
            "        return 1",
            "function viaShowable(Showable s) returns int",
            "    return s.show()"));
        for (int i = 0; i < 5; i++) {
            source.add("class C" + i + " implements Shower");
        }
        source.add("init");
        source.add("    int sum = viaShowable(new Own())");
        for (int i = 0; i < 5; i++) {
            source.add("    sum += viaShowable(new C" + i + "())");
        }
        source.add("    if sum == 36");
        source.add("        testSuccess()");
        String[] lines = source.toArray(new String[0]);
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(lines);
        File jass = new File("test-output/DeterministicChecks_" + name + "_no_opts.j");
        File lua = new File("test-output/lua/DeterministicChecks_" + name + ".lua");
        String firstJass = Files.toString(jass, Charsets.UTF_8);
        String firstLua = Files.toString(lua, Charsets.UTF_8);
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(lines);
        assertEquals(Files.toString(jass, Charsets.UTF_8), firstJass);
        assertEquals(Files.toString(lua, Charsets.UTF_8), firstLua);

        CU interfaces = compilationUnit("PkgShow.wurst",
            "package PkgShow",
            "public interface Showable",
            "    function show() returns int",
            "public interface Shower extends Showable",
            "    function show() returns int",
            "        return 7");
        List<CU> siblings = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            siblings.add(compilationUnit("PkgC" + i + ".wurst",
                "package PkgC" + i,
                "import PkgShow",
                "public class C" + i + " implements Shower"));
        }
        List<CU> forward = new ArrayList<>();
        forward.add(interfaces);
        forward.addAll(siblings);
        List<CU> shuffled = List.of(siblings.get(2), siblings.get(0), interfaces, siblings.get(3), siblings.get(1));
        assertSubMethodOrderIgnoresUnitOrder(forward, shuffled, 1);
    }

    /**
     * Interface methods with type parameters of their own, implemented by inherited methods (through the function a
     * generic class gets for them) and overridden below, emit one script.
     */
    @Test
    public void ownTypeParameterDispatchIsIdenticalAcrossCompilations() throws IOException {
        String name = "ownTypeParameterDispatch";
        String[] source = InterfaceTests.ownTypeParameterDispatchSource();
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(source);
        File jass = new File("test-output/DeterministicChecks_" + name + "_no_opts.j");
        File lua = new File("test-output/lua/DeterministicChecks_" + name + ".lua");
        String firstJass = Files.toString(jass, Charsets.UTF_8);
        String firstLua = Files.toString(lua, Charsets.UTF_8);
        testNamed(name).testLua(true).luaOnly(false).executeProg().lines(source);
        assertEquals(Files.toString(jass, Charsets.UTF_8), firstJass);
        assertEquals(Files.toString(lua, Charsets.UTF_8), firstLua);

        CU program = compilationUnit("OwnTypeParameters.wurst", source);
        CU extra = compilationUnit("Extra.wurst", "package Extra", "native consume(int value)",
            "init", "    consume(7)");
        testNamed(name).testLua(true).inline().localOptimizations().compilationUnits(program, extra);
        String forward = compiledLua(name);
        testNamed(name).testLua(true).inline().localOptimizations().compilationUnits(extra, program);
        assertEquals(compiledLua(name), forward);
    }

    @Test
    public void simple() throws IOException {
        ErrorHandler.outputTestSource = true;
        run(this::exampleCode, "exampleCode_no_opts");
        ErrorHandler.outputTestSource = false;
    }

    private void run(Runnable example, String name) throws IOException {
        example.run();
        File exampleFile1 = new File("test-output/DeterministicChecks_"+ name + ".j");
        String script1 = Files.toString(exampleFile1, Charsets.UTF_8);
        Files.move(exampleFile1, new File("test-output/det1.j"));
        Files.move(new File("test-output/im 1.im"), new File("test-output/im1.j"));
        example.run();
        String script2 = Files.toString(exampleFile1, Charsets.UTF_8);
        Files.move(exampleFile1, new File("test-output/det2.j"));
        Files.move(new File("test-output/im 1.im"), new File("test-output/im2.j"));
        assertEquals(script1, script2);
    }

    /**
     * The same source has to emit the same script whatever was compiled before it. Names of
     * generated temporaries used to be counted per thread and never reset, so a program compiled
     * alone got {@code temp0} and the same program compiled after other work got {@code temp70},
     * which is what made generated Jass impossible to compare across runs. Compiling something
     * else in between is the part that matters here; two runs on their own would agree either way.
     * The inlining configuration is the one that emits a temporary for this source.
     */
    @Test
    public void temporaryNamesDoNotDependOnEarlierCompilations() throws IOException {
        ErrorHandler.outputTestSource = true;
        try {
            usesTemporaries();
            String first = Files.toString(
                new File("test-output/DeterministicChecks_usesTemporaries_inl.j"), Charsets.UTF_8);

            exampleCode();
            cycleExample();

            usesTemporaries();
            String afterOtherWork = Files.toString(
                new File("test-output/DeterministicChecks_usesTemporaries_inl.j"), Charsets.UTF_8);

            assertEquals(first, afterOtherWork);
        } finally {
            ErrorHandler.outputTestSource = false;
        }
    }

    /**
     * A temporary added to one function renames none of another function's temporaries, so an edit changes the text of
     * the edited function alone. The Lua side is {@code LuaHotReloadTests.addedTemporaryLeavesOtherFunctionsAlone}.
     */
    @Test
    public void addedTemporaryRenamesNoOtherFunctionsTemporaries() throws IOException {
        String before = jassFunction(compileTemporaries("x + 1"), "second");
        assertTrue(before.contains("andLeft") && before.contains("temp"),
            "the other function has temporaries of both kinds:\n" + before);
        // The nested call is evaluated into a temporary, because the conditional after it has statements.
        String after = jassFunction(compileTemporaries("add(add(x, 1), x > 0 ? 1 : 2)"), "second");
        assertEquals(after, before, "a temporary added to first must not rename those of second");
    }

    private String compileTemporaries(String firstBody) throws IOException {
        testNamed("addedTemporary").lines(
            "package test",
            "function add(int a, int b) returns int",
            "    return a + b",
            "function first(int x) returns int",
            "    return " + firstBody,
            "function second(int x) returns int",
            "    if x > 0 and add(add(x, 1), x > 1 ? 1 : 2) > 2",
            "        return 1",
            "    return 0",
            "init",
            "    first(1)",
            "    second(2)");
        return Files.toString(new File("test-output/DeterministicChecks_addedTemporary_no_opts.j"), Charsets.UTF_8);
    }

    private static String jassFunction(String jass, String name) {
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("(?ms)^function \\w*" + name + "\\w* takes .*?^endfunction").matcher(jass);
        assertTrue(m.find(), "no function " + name + " in\n" + jass);
        return m.group();
    }

    /** Nested calls in one expression are what makes the flattener allocate temporaries. */
    private void usesTemporaries() {
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "function f(int x) returns int",
            "    return x + 1",
            "init",
            "    if f(f(1)) + f(f(2)) == 8",
            "        testSuccess()"
        );
    }

    private void exampleCode() {
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "interface I",
            "    function foo() returns int",
            "class B implements I",
            "    function foo() returns int",
            "        return 2",
            "class C implements I",
            "    function foo() returns int",
            "        return 3",
            "init",
            "    I i1 = new B()",
            "    I i2 = new C()",
            "    if i1.foo() == 2 and i2.foo() == 3",
            "        testSuccess()"
        );
    }

    @Test
    public void cyclicFunctionCall() throws IOException {
        ErrorHandler.outputTestSource = true;
        run(this::cycleExample, "cycleExample_no_opts");
        ErrorHandler.outputTestSource = false;
    }

    private void cycleExample() {
        testAssertOkLines(false,
            "package test",
            "native testSuccess()",
            "function a(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return b(i div 2)",
            "function b(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return c(i div 2)",
            "function c(int i) returns int",
            "    if i == 0",
            "       return 0",
            "    return a(i div 2)",
            "init",
            "    if a(42) == 0",
            "        testSuccess()"
        );
    }

    @Test
    public void test_var_merge() throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            test().executeProg(true).lines(
                "package test",
                "native testSuccess()",
                "native println(string s)",
                "function foo(string p_msg, string p_pos) returns string",
                "    var msg = p_msg",
                "    var pos = p_pos",
                "    pos = msg",
                "    msg = \"\"",
                "    for i = 1 to 3",
                "        msg += \"x\"",
                "    return pos + msg",
                "init",
                "    let s = foo(\"a\", \"b\")",
                "    if s == \"axxx\"",
                "        testSuccess()",
                "");

            String output = Files.toString(new File("./test-output/DeterministicChecks_test_var_merge_opt.j"), Charsets.UTF_8);
            counts.put(output, counts.getOrDefault(output, 0) + 1);
        }
        //System.out.println("counts = " + counts.values());
        AssertJUnit.assertEquals(1, counts.size());
        //System.out.println(counts.keySet());
        // Interesting note: LocalMerger seems to switch the order in the return line and sometimes rewrites the return to
        // return p_msg + p_pos
    }

    @Test
    public void functionAndGlobalSortOrderIsDeterministic() {
        CompilationResult res1 = test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .testLua(true)
            .lines(
                "package test",
                "int zVar = 1",
                "int aVar = 2",
                "function zFunc() returns int",
                "    return zVar",
                "function aFunc() returns int",
                "    return aVar",
                "init",
                "    aFunc()",
                "    zFunc()"
            );

        CompilationResult res2 = test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .testLua(true)
            .lines(
                "package test",
                "function aFunc() returns int",
                "    return aVar",
                "int aVar = 2",
                "function zFunc() returns int",
                "    return zVar",
                "int zVar = 1",
                "init",
                "    aFunc()",
                "    zFunc()"
            );

        de.peeeq.wurstscript.translation.imtranslation.ImTranslator tr1 =
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator(res1.getModel(), false, new de.peeeq.wurstscript.RunArgs());
        de.peeeq.wurstscript.jassIm.ImProg prog1 = tr1.translateProg();

        de.peeeq.wurstscript.translation.imtranslation.ImTranslator tr2 =
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator(res2.getModel(), false, new de.peeeq.wurstscript.RunArgs());
        de.peeeq.wurstscript.jassIm.ImProg prog2 = tr2.translateProg();

        java.util.List<String> funcs1 = prog1.getFunctions().stream()
            .map(de.peeeq.wurstscript.jassIm.ImFunction::getName)
            .filter(name -> name.contains("Func"))
            .collect(java.util.stream.Collectors.toList());
        java.util.List<String> funcs2 = prog2.getFunctions().stream()
            .map(de.peeeq.wurstscript.jassIm.ImFunction::getName)
            .filter(name -> name.contains("Func"))
            .collect(java.util.stream.Collectors.toList());
        assertEquals(funcs1, funcs2);

        java.util.List<String> globals1 = prog1.getGlobals().stream()
            .map(de.peeeq.wurstscript.jassIm.ImVar::getName)
            .filter(name -> name.endsWith("Var"))
            .collect(java.util.stream.Collectors.toList());
        java.util.List<String> globals2 = prog2.getGlobals().stream()
            .map(de.peeeq.wurstscript.jassIm.ImVar::getName)
            .filter(name -> name.endsWith("Var"))
            .collect(java.util.stream.Collectors.toList());
        assertEquals(2, globals1.size());
        assertEquals(globals1, globals2);
    }

    /**
     * A class which implements an interface through a module resolves {@code v.write(x)} to the same
     * function whatever order the compiler happens to see its candidates in. The candidates are the
     * module's implementation and the interface's declaration, and they live in hash sets keyed by
     * identity, so the order differs between compilations of the same source.
     */
    @Test
    public void interfaceImplementedByModuleResolvesTheSameCallEveryCompilation() throws IOException {
        File outFile = new File("test-output/lua/DeterministicChecks_moduleImplementedInterface.lua");
        String first = null;
        for (int run = 0; run < 3; run++) {
            moduleImplementedInterface();
            String output = Files.toString(outFile, Charsets.UTF_8);
            if (first == null) {
                first = output;
            } else {
                assertEquals(output, first, "compilation " + run + " of the same source must emit the same Lua");
            }
        }
    }

    private void moduleImplementedInterface() {
        int classes = 14;
        List<String> lines = new ArrayList<>(List.of(
            "package test",
            "native testSuccess()",
            "interface Codec",
            "    function write(int x) returns int",
            "    function read(int x) returns int",
            "module Lifecycle",
            "    abstract function write(int x) returns int",
            "    abstract function read(int x) returns int",
            "    function roundTrip(int x) returns int",
            "        return read(write(x))",
            "module Fields",
            "    use Lifecycle",
            "    int stored = 0",
            "    override function write(int x) returns int",
            "        stored = x",
            "        return x + 1",
            "    override function read(int x) returns int",
            "        return x - 1 + stored"));
        for (int i = 0; i < classes; i++) {
            lines.add("class C" + i + " implements Codec");
            lines.add("    use Fields");
            lines.add("function call" + i + "(C" + i + " v) returns int");
            lines.add("    return v.write(" + i + ") + v.read(" + i + ")");
        }
        lines.add("init");
        lines.add("    int sum = 0");
        for (int i = 0; i < classes; i++) {
            lines.add("    sum += call" + i + "(new C" + i + "())");
        }
        lines.add("    if sum != 0");
        lines.add("        testSuccess()");
        test().testLua(true).executeProg().lines(lines.toArray(new String[0]));
    }

    /**
     * A call on a class type binds to the module's implementation rather than to the interface
     * function, which is what the type checker chooses. The call must still reach an override in a
     * subclass, through the class type as well as through the interface, on both targets.
     */
    @Test
    public void callBoundToModuleImplementationStillReachesSubclassOverride() {
        test().testLua(true).luaOnly(false).executeProg().lines(
            "package test",
            "native testSuccess()",
            "interface Codec",
            "    function write(int x) returns int",
            "module Lifecycle",
            "    abstract function write(int x) returns int",
            "module Fields",
            "    use Lifecycle",
            "    override function write(int x) returns int",
            "        return x + 1",
            "class Base implements Codec",
            "    use Fields",
            "class Derived extends Base",
            "    override function write(int x) returns int",
            "        return x + 100",
            "function viaBase(Base b) returns int",
            "    return b.write(1)",
            "function viaInterface(Codec c) returns int",
            "    return c.write(1)",
            "init",
            "    if viaBase(new Base()) == 2 and viaBase(new Derived()) == 101",
            "        if viaInterface(new Base()) == 2 and viaInterface(new Derived()) == 101",
            "            testSuccess()"
        );
    }

    /**
     * The sub-methods of a method are the overrides in the classes which extend or implement its
     * owner, and the backends bind dispatch slots in their order. The classes of one owner were kept
     * in hash sets keyed by identity, so the same source listed them in a different order every time
     * it was compiled.
     */
    @Test
    public void subMethodOrderIsTheSameEveryCompilation() {
        List<String> first = subMethodOrder();
        for (int run = 1; run < 3; run++) {
            assertEquals(subMethodOrder(), first, "compilation " + run + " of the same source must order sub-methods alike");
        }
    }

    private List<String> subMethodOrder() {
        int classes = 14;
        List<String> lines = new ArrayList<>(List.of(
            "package test",
            "interface Shape",
            "    function area() returns int",
            "class Base",
            "    function size() returns int",
            "        return 0"));
        for (int i = 0; i < classes; i++) {
            lines.add("class Shape" + i + " implements Shape");
            lines.add("    override function area() returns int");
            lines.add("        return " + i);
            lines.add("class Derived" + i + " extends Base");
            lines.add("    override function size() returns int");
            lines.add("        return " + i);
        }
        CompilationResult res = test().setStopOnFirstError(false).executeProg(false).lines(lines.toArray(new String[0]));
        List<String> order = subMethodOrder(res);
        assertTrue(order.size() >= 2, "expected sub-methods for Shape.area and Base.size: " + order);
        return order;
    }

    private List<String> subMethodOrder(CompilationResult res) {
        ImProg prog = new ImTranslator(res.getModel(), false, new RunArgs()).translateProg();
        List<String> order = new ArrayList<>();
        for (ImClass c : prog.getClasses()) {
            for (ImMethod m : c.getMethods()) {
                if (!m.getSubMethods().isEmpty()) {
                    order.add(c.getName() + "." + m.getName() + " -> "
                        + m.getSubMethods().stream().map(sm -> sm.getImplementation().getName()).collect(Collectors.joining(", ")));
                }
            }
        }
        return order;
    }

    /**
     * The sub-methods which the units listed in {@code forward} give each method, compared with the
     * ones the same units give in {@code shuffled}, which holds them in another order.
     */
    private void assertSubMethodOrderIgnoresUnitOrder(List<CU> forward, List<CU> shuffled, int expectedMethods) {
        List<String> inOrder = subMethodOrder(test().setStopOnFirstError(false).executeProg(false)
            .compilationUnits(forward.toArray(new CU[0])));
        assertTrue(inOrder.size() >= expectedMethods, "expected " + expectedMethods + " methods with sub-methods: " + inOrder);
        List<String> reordered = subMethodOrder(test().setStopOnFirstError(false).executeProg(false)
            .compilationUnits(shuffled.toArray(new CU[0])));
        assertEquals(reordered, inOrder, "sub-method order must not depend on the order of the compilation units");
    }

    /**
     * Sibling implementors and subclasses which live in different compilation units are ordered by
     * their package, name and position, not by the order the units were handed to the compiler: the
     * sub-methods of a method, which the backends bind dispatch slots in, come out the same.
     */
    @Test
    public void subMethodOrderDoesNotDependOnCompilationUnitOrder() {
        CU shapes = compilationUnit("PkgShape.wurst",
            "package PkgShape",
            "public interface Shape",
            "    function area() returns int",
            "public class Base",
            "    function size() returns int",
            "        return 0");
        List<CU> siblings = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            siblings.add(compilationUnit("PkgS" + i + ".wurst",
                "package PkgS" + i,
                "import PkgShape",
                "public class Shape" + i + " implements Shape",
                "    override function area() returns int",
                "        return " + i,
                "public class Derived" + i + " extends Base",
                "    override function size() returns int",
                "        return " + i));
        }
        List<CU> forward = new ArrayList<>();
        forward.add(shapes);
        forward.addAll(siblings);
        List<CU> shuffled = List.of(siblings.get(3), siblings.get(1), shapes, siblings.get(2), siblings.get(0));
        assertSubMethodOrderIgnoresUnitOrder(forward, shuffled, 2);
    }

    /**
     * A closure is a class which implements the interface it is used as, and each closure adds itself
     * to the sub-methods of the interface's function while its unit is translated. Closures in
     * different units must not be listed in the order the units were handed to the compiler.
     */
    @Test
    public void closureSubMethodOrderDoesNotDependOnCompilationUnitOrder() {
        CU transformer = compilationUnit("PkgTransformer.wurst",
            "package PkgTransformer",
            "public interface Transformer",
            "    function transform(int x) returns int",
            "public function applyTransformer(int val, Transformer t) returns int",
            "    return t.transform(val)");
        List<CU> users = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            users.add(compilationUnit("PkgUser" + i + ".wurst",
                "package PkgUser" + i,
                "import PkgTransformer",
                "public function compute" + i + "(int val) returns int",
                "    Transformer t = (int x) -> begin",
                "        return x * " + i,
                "    end",
                "    return applyTransformer(val, t)"));
        }
        List<CU> forward = new ArrayList<>();
        forward.add(transformer);
        forward.addAll(users);
        List<CU> shuffled = List.of(users.get(3), users.get(1), transformer, users.get(2), users.get(0));
        assertSubMethodOrderIgnoresUnitOrder(forward, shuffled, 1);
    }

    @Test
    public void multiPackageShuffledCompilationUnitOrderIsBitExact() throws IOException {
        CU cuA = compilationUnit("PkgA.wurst",
            "package PkgA",
            "public interface Formatter",
            "    function format(string s) returns string",
            "public class UpperFormatter implements Formatter",
            "    override function format(string s) returns string",
            "        return s",
            "public int counter = 0",
            "public function inc()",
            "    counter++"
        );
        CU cuB = compilationUnit("PkgB.wurst",
            "package PkgB",
            "import PkgA",
            "public class FancyFormatter extends UpperFormatter",
            "    override function format(string s) returns string",
            "        return super.format(s) + \"!\"",
            "public function runAction(Formatter f, string text) returns string",
            "    inc()",
            "    return f.format(text)"
        );
        CU cuC = compilationUnit("PkgC.wurst",
            "package PkgC",
            "public interface Transformer",
            "    function transform(int x) returns int",
            "public function applyTransformer(int val, Transformer t) returns int",
            "    return t.transform(val)"
        );
        CU cuD = compilationUnit("PkgD.wurst",
            "package PkgD",
            "import PkgB",
            "import PkgC",
            "public function compute(int val) returns int",
            "    Transformer t = (int x) -> begin",
            "        return x * 2 + 1",
            "    end",
            "    return applyTransformer(val, t)"
        );
        CU cuE = compilationUnit("PkgE.wurst",
            "package PkgE",
            "import PkgA",
            "import PkgB",
            "import PkgD",
            "native testSuccess()",
            "init",
            "    FancyFormatter ff = new FancyFormatter()",
            "    let resStr = runAction(ff, \"test\")",
            "    let resNum = compute(10)",
            "    if resStr == \"test!\" and resNum == 21 and counter == 1",
            "        testSuccess()",
            "    destroy ff"
        );

        // Pass 1 in order [A, B, C, D, E]
        test().testLua(true).executeProg().compilationUnits(cuA, cuB, cuC, cuD, cuE);
        File outFile = new File("test-output/lua/DeterministicChecks_multiPackageShuffledCompilationUnitOrderIsBitExact.lua");
        String outputStd1 = Files.toString(outFile, Charsets.UTF_8);
        String hashStd1 = Hashing.sha256().hashString(outputStd1, Charsets.UTF_8).toString();

        // Pass 2 in shuffled order [D, A, E, C, B]
        test().testLua(true).executeProg().compilationUnits(cuD, cuA, cuE, cuC, cuB);
        String outputStd2 = Files.toString(outFile, Charsets.UTF_8);
        String hashStd2 = Hashing.sha256().hashString(outputStd2, Charsets.UTF_8).toString();

        assertEquals(hashStd1, hashStd2, "SHA-256 hash must be identical across shuffled compilation unit order");
        assertEquals(outputStd1, outputStd2, "Output must be bit-for-bit identical across shuffled compilation unit order");
    }

    /**
     * Every closure of one interface method written in one function becomes a class with the same IM
     * name, so no name-based sort key tells those classes apart. The Lua backend binds one dispatch
     * slot on each of them; the order of those assignments used to be the iteration order of a
     * {@code HashSet} of identity-hashed classes, which differs between compilations (and JVMs), while
     * every name and number in the script stayed the same. Compiling the same program repeatedly, with
     * the compilation units in a different order each time, must give one script.
     */
    @Test
    public void classesSharingAnImNameBindTheirDispatchSlotInProgramOrder() throws IOException {
        String[] types = {"int", "real", "string", "boolean"};
        String[] literals = {"1", "1.5", "\"s\"", "true"};
        List<String> library = new ArrayList<>(List.of(
            "package Shapes",
            "public interface Predicate<T:>",
            "    function test(T t) returns boolean",
            "public class Box<T:>",
            "    T value",
            "    construct(T value)",
            "        this.value = value",
            "    function matches(Predicate<T> p) returns boolean",
            "        let result = p.test(value)",
            "        destroy p",
            "        return result",
            "public class Foo",
            "public class Impl implements Predicate<Foo>",
            "    function test(Foo value) returns boolean",
            "        return value != null"));
        // A class beside the closures makes the family heterogeneous, which is what gives the
        // closures a canonical slot of their own.
        List<String> main = new ArrayList<>(List.of(
            "package Main",
            "import Shapes",
            "native testSuccess()",
            "init",
            "    int successes = 0",
            "    let ordinary = new Box<Foo>(new Foo())",
            "    if ordinary.matches(x -> x != null)",
            "        successes++",
            "    let ordinaryImpl = new Box<Foo>(new Foo())",
            "    if ordinaryImpl.matches(new Impl())",
            "        successes++"));
        int count = 0;
        for (int a = 0; a < types.length; a++) {
            for (int b = 0; b < 2; b++) {
                String tuple = "shape" + count;
                library.add("public tuple " + tuple + "(" + types[a] + " a, " + types[b] + " b)");
                main.add("    let box" + count + " = new Box<" + tuple + ">(" + tuple + "("
                    + literals[a] + ", " + literals[b] + "))");
                main.add("    if box" + count + ".matches(x -> x.a == " + literals[a] + ")");
                main.add("        successes++");
                count++;
            }
        }
        main.add("    if successes == " + (count + 2));
        main.add("        testSuccess()");
        CU shapesUnit = new CU("Shapes.wurst", String.join("\n", library));
        CU mainUnit = new CU("Main.wurst", String.join("\n", main));

        compileClosureReceivers(shapesUnit, mainUnit);
        String first = readClosureReceiversLua();
        // the premise: the closures (one per tuple, one over Foo) and Impl are bound to one slot
        assertEquals(countOccurrences(first, ".__wurst_dispatch_test = "), count + 2, first);

        for (int pass = 0; pass < 5; pass++) {
            if (pass % 2 == 0) {
                compileClosureReceivers(mainUnit, shapesUnit);
            } else {
                compileClosureReceivers(shapesUnit, mainUnit);
            }
            assertEquals(readClosureReceiversLua(), first,
                "Lua output must not depend on the identity hashes of the receiver classes (pass " + pass + ")");
        }
    }

    private void compileClosureReceivers(CU... units) {
        test().testLua(true).executeProg().compilationUnits(units);
    }

    private String readClosureReceiversLua() throws IOException {
        return Files.toString(new File("test-output/lua/DeterministicChecks_compileClosureReceivers.lua"),
            Charsets.UTF_8);
    }

    /**
     * A generic class implements two interfaces, each with one of two overloads it inherits, so it gets a function of
     * its own for each, both named after the method. The interfaces are in packages of their own: the Jass and the
     * Lua must not depend on which of them is translated first. Base comes first either way, so its own functions are
     * made in its order: a class's functions are made when they are first asked for, which an interface translated
     * before the class does in the order of the units. C comes after both interfaces.
     */
    @Test
    public void bridgesOfAGenericClassAreTheSameInAnyUnitOrder() throws IOException {
        List<CU> units = List.of(
            compilationUnit("IntM.wurst",
                "package IntM",
                "public interface IntM",
                "    function m(int x) returns int"),
            compilationUnit("StrM.wurst",
                "package StrM",
                "public interface StrM",
                "    function m(string s) returns int"),
            compilationUnit("BaseLib.wurst",
                "package BaseLib",
                "public class Base",
                "    function m(int x) returns int",
                "        return x",
                "    function m(string s) returns int",
                "        return 7"),
            compilationUnit("Lib.wurst",
                "package Lib",
                "import BaseLib",
                "import IntM",
                "import StrM",
                "public class C<T:> extends Base implements IntM, StrM"),
            compilationUnit("Main.wurst",
                "package Main",
                "import IntM",
                "import StrM",
                "import Lib",
                "native testSuccess()",
                "function viaInt(IntM i) returns int",
                "    return i.m(5)",
                "function viaStr(StrM s) returns int",
                "    return s.m(\"a\")",
                "init",
                "    if viaInt(new C<int>()) == 5 and viaStr(new C<int>()) == 7",
                "        testSuccess()"));
        String name = "bridgesOfAGenericClass";
        File jass = new File("test-output/DeterministicChecks_" + name + "_no_opts.j");
        File lua = new File("test-output/lua/DeterministicChecks_" + name + ".lua");
        testNamed(name).testLua(true).luaOnly(false).executeProg()
            .compilationUnits(units.get(2), units.get(0), units.get(1), units.get(3), units.get(4));
        String firstJass = Files.toString(jass, Charsets.UTF_8);
        String firstLua = Files.toString(lua, Charsets.UTF_8);
        testNamed(name).testLua(true).luaOnly(false).executeProg()
            .compilationUnits(units.get(2), units.get(1), units.get(0), units.get(3), units.get(4));
        assertEquals(Files.toString(jass, Charsets.UTF_8), firstJass, "Jass must not depend on the unit order");
        assertEquals(Files.toString(lua, Charsets.UTF_8), firstLua, "Lua must not depend on the unit order");
    }

    /**
     * A static inner class of a generic class takes the owner's instantiation from the type it is handed out as, and
     * Lua specialises its reader of the owner's static for that instantiation. Two such owners in packages of their
     * own: what is specialised, and its names, must not depend on which is translated first.
     */
    @Test
    public void staticInnerClassesOfGenericOwnersAreTheSameInAnyUnitOrder() throws IOException {
        List<CU> units = List.of(
            compilationUnit("OuterLib.wurst",
                "package OuterLib",
                "public class Outer<T:>",
                "    static T last",
                "    construct(T value)",
                "        last = value",
                "    function reader() returns Reader",
                "        return new Reader()",
                "    static class Reader",
                "        function read() returns T",
                "            return last"),
            compilationUnit("BoxLib.wurst",
                "package BoxLib",
                "public class Box<T:>",
                "    static T held",
                "    construct(T value)",
                "        held = value",
                "    function reader() returns Reader",
                "        return new Reader()",
                "    static class Reader",
                "        function read() returns T",
                "            return held"),
            compilationUnit("Main.wurst",
                "package Main",
                "import OuterLib",
                "import BoxLib",
                "native testSuccess()",
                "init",
                "    let ints = new Outer<int>(7)",
                "    let strings = new Outer<string>(\"ok\")",
                "    let box = new Box<int>(3)",
                "    if ints.reader().read() == 7 and strings.reader().read() == \"ok\" and box.reader().read() == 3",
                "        testSuccess()"));
        String name = "staticInnerClassesOfGenericOwners";
        File jass = new File("test-output/DeterministicChecks_" + name + "_no_opts.j");
        File lua = new File("test-output/lua/DeterministicChecks_" + name + ".lua");
        testNamed(name).testLua(true).luaOnly(false).executeProg()
            .compilationUnits(units.get(0), units.get(1), units.get(2));
        String firstJass = Files.toString(jass, Charsets.UTF_8);
        String firstLua = Files.toString(lua, Charsets.UTF_8);
        testNamed(name).testLua(true).luaOnly(false).executeProg()
            .compilationUnits(units.get(1), units.get(0), units.get(2));
        assertEquals(Files.toString(jass, Charsets.UTF_8), firstJass, "Jass must not depend on the unit order");
        assertEquals(Files.toString(lua, Charsets.UTF_8), firstLua, "Lua must not depend on the unit order");
    }

    /** Compiles {@code units} to Lua, runs them, and returns the script, which is written under {@code name}. */
    private String compileToLua(String name, List<CU> units) throws IOException {
        testNamed(name).testLua(true).executeProg().compilationUnits(units.toArray(new CU[0]));
        return Files.toString(new File("test-output/lua/DeterministicChecks_" + name + ".lua"), Charsets.UTF_8);
    }

    /**
     * The Lua dispatch groups the methods of an override family into slots by the key of their signature. A type
     * variable used to print with an identity hash there, so the parameter T of State and the T of NoOpState shared a
     * key in some runs and not in others, and the FSM below got its slot named after either class: two copies of a
     * program must give a type variable the same key, and two variables of different owners different ones.
     */
    @Test
    public void dispatchSignatureKeysDoNotDependOnIdentityHashes() {
        String[] first = typeVarKeys();
        String[] second = typeVarKeys();
        assertEquals(second, first);
        assertTrue(!first[0].equals(first[1]), first[0] + " / " + first[1]);
    }

    private static String[] typeVarKeys() {
        String[] keys = new String[2];
        String[] owners = {"State", "NoOpState"};
        for (int i = 0; i < 2; i++) {
            de.peeeq.wurstscript.jassIm.ImTypeVar t = de.peeeq.wurstscript.jassIm.JassIm.ImTypeVar("T");
            de.peeeq.wurstscript.jassIm.JassIm.ImClass(de.peeeq.wurstscript.ast.Ast.NoExpr(), owners[i], de.peeeq.wurstscript.jassIm.JassIm.ImTypeVars(t),
                de.peeeq.wurstscript.jassIm.JassIm.ImVars(), de.peeeq.wurstscript.jassIm.JassIm.ImMethods(),
                de.peeeq.wurstscript.jassIm.JassIm.ImFunctions(), new ArrayList<>());
            keys[i] = de.peeeq.wurstscript.translation.imtranslation.LuaDispatchPreparation.typeKey(
                de.peeeq.wurstscript.jassIm.JassIm.ImTypeVarRef(t));
        }
        return keys;
    }

    /**
     * The FSM of AGENTS.md section 8 with each sibling state in a package of its own: every sibling binds the root
     * slot FSM.update calls to its own update, never to NoOpState's, and the script is the same with the
     * compilation units in the reverse order.
     */
    @Test
    public void fsmSiblingsInSeparatePackagesBindRootSlotInAnyUnitOrder() throws IOException {
        List<CU> units = new ArrayList<>();
        units.add(compilationUnit("FsmLib.wurst",
            "package FsmLib",
            "public abstract class State<T:>",
            "    function enter(T owner)",
            "    function update(T owner, real dt)",
            "    function exit(T owner)",
            "public class NoOpState<T:> extends State<T>",
            "    override function enter(T owner)",
            "    override function update(T owner, real dt)",
            "    override function exit(T owner)",
            "public class FSM<T:>",
            "    T owner",
            "    State<T> currentState = null",
            "    construct(T owner)",
            "        this.owner = owner",
            "    function setInitialState(State<T> st)",
            "        currentState = st",
            "        if currentState != null",
            "            currentState.enter(owner)",
            "    function update(real dt)",
            "        if currentState != null",
            "            currentState.update(owner, dt)"));
        units.add(compilationUnit("FsmOwner.wurst",
            "package FsmOwner",
            "import FsmLib",
            "public class Owner",
            "    FSM<Owner> fsm = new FSM<Owner>(this)",
            "    int ticks = 0"));
        StringBuilder check = new StringBuilder("    if runOne(idle) == 0");
        List<String> imports = new ArrayList<>();
        for (int n = 1; n <= 5; n++) {
            units.add(compilationUnit("FsmS" + n + ".wurst",
                "package FsmS" + n,
                "import FsmLib",
                "import FsmOwner",
                "public class St" + n + " extends NoOpState<Owner>",
                "    override function update(Owner o, real dt)",
                "        o.ticks += " + n,
                "public constant st" + n + "State = new St" + n + "()"));
            imports.add("import FsmS" + n);
            check.append(" and runOne(st").append(n).append("State) == ").append(5 * n);
        }
        List<String> main = new ArrayList<>(List.of("package FsmMain", "import FsmLib", "import FsmOwner"));
        main.addAll(imports);
        main.addAll(List.of(
            "native testSuccess()",
            "public constant idle = new NoOpState<Owner>()",
            "function runOne(State<Owner> st) returns int",
            "    let o = new Owner()",
            "    o.fsm.setInitialState(st)",
            "    for i = 0 to 4",
            "        o.fsm.update(0.1)",
            "    return o.ticks",
            "init",
            check.toString(),
            "        testSuccess()"));
        units.add(compilationUnit("FsmMain.wurst", main.toArray(new String[0])));

        String first = compileToLua("fsmSiblingsInSeparatePackages", units);
        // the slot FSM.update calls, as in __wurst_objectClass[FSM_currentState_storage[this1]].State_update(..., dt)
        java.util.regex.Matcher call = java.util.regex.Pattern
            .compile("\\]\\.(\\w+)\\([^()\\n]*,\\s*dt\\w*\\)").matcher(first);
        assertTrue(call.find(), first);
        String slot = call.group(1);
        for (int n = 1; n <= 5; n++) {
            assertTrue(first.matches("(?s).*\\bSt" + n + "\\." + slot + " = St" + n + "_\\w*update\\b.*"),
                "St" + n + " binds " + slot + " to its own update:\n" + first);
        }
        List<CU> reversed = new ArrayList<>(units);
        java.util.Collections.reverse(reversed);
        assertEquals(compileToLua("fsmSiblingsInSeparatePackages", reversed), first,
            "Lua must not depend on the order of the compilation units");
    }

    /**
     * Three packages each declare a class Node, extending a shared abstract class, and an implementor of a shared
     * interface, with closures of it: receivers whose names are equal must bind their slots in an order which does
     * not depend on the order of the compilation units.
     */
    @Test
    public void sameNamedClassesInSeveralPackagesEmitTheSameLuaInAnyUnitOrder() throws IOException {
        List<CU> units = new ArrayList<>();
        units.add(compilationUnit("Lib.wurst",
            "package Lib",
            "public interface Visitor",
            "    function visit(int x) returns int",
            "public abstract class Shape",
            "    abstract function area() returns int",
            "public function apply(Visitor v, int x) returns int",
            "    return v.visit(x)",
            "public function areaOf(Shape s) returns int",
            "    return s.area()"));
        for (String p : List.of("P1", "P2", "P3")) {
            units.add(compilationUnit(p + ".wurst",
                "package " + p,
                "import Lib",
                "public class Node extends Shape",
                "    override function area() returns int",
                "        return " + p.charAt(1) + "0",
                "public class Impl implements Visitor",
                "    override function visit(int x) returns int",
                "        return x + 1",
                "public function run" + p + "() returns int",
                "    return areaOf(new Node()) + apply(new Impl(), 1) + apply((int x) -> x * 2, 3) + apply(x -> x * 3, 4)"));
        }
        units.add(compilationUnit("Main.wurst",
            "package Main",
            "import P1",
            "import P2",
            "import P3",
            "native testSuccess()",
            "init",
            // 10+2+6+12 + 20+2+6+12 + 30+2+6+12
            "    if runP1() + runP2() + runP3() == 120",
            "        testSuccess()"));
        String first = compileToLua("sameNamedClassesInSeveralPackages", units);
        List<CU> shuffled = List.of(units.get(4), units.get(2), units.get(0), units.get(3), units.get(1));
        assertEquals(compileToLua("sameNamedClassesInSeveralPackages", shuffled), first,
            "same-named receivers must bind in an order independent of the compilation units");
    }

    /**
     * On Lua the natives of Reflection number the counted allocation groups and print the names and the groups of
     * the type ids in tables, in the order of the type ids, which the order of the units must not change.
     */
    @Test
    public void reflectionTablesAreTheSameInAnyUnitOrder() throws IOException {
        List<CU> units = new ArrayList<>();
        for (String p : List.of("P1", "P2", "P3")) {
            units.add(compilationUnit(p + ".wurst",
                "package " + p,
                "public class Node",
                "public class Leaf extends Node",
                "public function make" + p + "() returns Node",
                "    return new Leaf()"));
        }
        units.add(compilationUnit("Main.wurst",
            "package Main",
            "import P1",
            "import P2",
            "import P3",
            "native testSuccess()",
            "native typeIdToTypeName(int typeId) returns string",
            "native maxTypeId() returns int",
            "native instanceCount(int typeId) returns int",
            "native maxInstanceCount(int typeId) returns int",
            "init",
            "    let nodes = makeP1().typeId + makeP2().typeId + makeP3().typeId",
            "    var total = 0",
            "    for i = 1 to maxTypeId()",
            "        if typeIdToTypeName(i) != \"unknown\"",
            "            total += instanceCount(i) + maxInstanceCount(i)",
            // each package: Node and Leaf are one group, with one live Leaf and a peak of one
            "    if nodes > 0 and maxTypeId() == 6 and total == 12",
            "        testSuccess()"));
        String first = compileToLua("reflectionTables", units);
        assertTrue(first.contains("__wurst_typeNames") && first.contains("__wurst_typeGroups"),
            "the type ids are known only at run time, so they are looked up in tables");
        List<CU> shuffled = List.of(units.get(3), units.get(2), units.get(0), units.get(1));
        assertEquals(compileToLua("reflectionTables", shuffled), first,
            "the tables and the group numbers must not depend on the order of the compilation units");
    }

    /**
     * Two packages each declare the generic hierarchy Box, First and Second. The key of a dispatch group named its class,
     * its method and the types of its signature by name only, so both families of pick had the same key and shared
     * one group: the packages of the class and of what the signature names keep them apart, in any order of the
     * compilation units.
     */
    @Test
    public void sameNamedGenericHierarchiesInTwoPackagesKeepTheirOwnDispatchGroups() throws IOException {
        assertOwnDispatchGroups("sameNamedOldGenericHierarchies", "T");
    }

    /** The same with new generics, whose signatures name the type variable and so its owner. */
    @Test
    public void sameNamedNewGenericHierarchiesInTwoPackagesKeepTheirOwnDispatchGroups() throws IOException {
        assertOwnDispatchGroups("sameNamedNewGenericHierarchies", "T:");
    }

    /**
     * One family across packages: Box in a shared package, and a class First overriding pick in each of two others.
     * Both overrides belong to Box's group and must bind the slot a call through Box uses.
     */
    @Test
    public void sameNamedOverridesInTwoPackagesShareTheirRootSlot() throws IOException {
        assertSharedRootSlot("sameNamedOldGenericOverrides", "T");
    }

    @Test
    public void sameNamedNewGenericOverridesInTwoPackagesShareTheirRootSlot() throws IOException {
        assertSharedRootSlot("sameNamedNewGenericOverrides", "T:");
    }

    private void assertSharedRootSlot(String name, String typeParameter) throws IOException {
        List<CU> units = new ArrayList<>();
        units.add(compilationUnit("Common.wurst",
            "package Common",
            "public abstract class Box<" + typeParameter + ">",
            "    abstract function pick(T a, T b) returns T",
            "public function pickOf(Box<int> box) returns int",
            "    return box.pick(10, 20)"));
        for (String p : List.of("PA", "PB")) {
            boolean first = p.equals("PA");
            units.add(compilationUnit(p + ".wurst",
                "package " + p,
                "import Common",
                "public class First<" + typeParameter + "> extends Box<T>",
                "    override function pick(T a, T b) returns T",
                "        return " + (first ? "a" : "b"),
                "public function run" + p + "() returns int",
                "    return pickOf(new First<int>())"));
        }
        units.add(compilationUnit("Main.wurst",
            "package Main",
            "import PA",
            "import PB",
            "native testSuccess()",
            "init",
            "    if runPA() == 10 and runPB() == 20",
            "        testSuccess()"));
        String lua = compileAndRunToLua(name, units.toArray(new CU[0]));
        List<CU> shuffled = List.of(units.get(3), units.get(2), units.get(0), units.get(1));
        assertEquals(compileToLua(name, shuffled), lua, "the same in any order of the compilation units");
    }

    private void assertOwnDispatchGroups(String name, String typeParameter) throws IOException {
        List<CU> units = new ArrayList<>();
        for (String p : List.of("PA", "PB")) {
            boolean first = p.equals("PA");
            units.add(compilationUnit(p + ".wurst",
                "package " + p,
                "public abstract class Box<" + typeParameter + ">",
                "    abstract function pick(T a, T b) returns T",
                "public class First<" + typeParameter + "> extends Box<T>",
                "    override function pick(T a, T b) returns T",
                "        return " + (first ? "a" : "b"),
                "public class Second<" + typeParameter + "> extends Box<T>",
                "    override function pick(T a, T b) returns T",
                "        return " + (first ? "b" : "a"),
                "public function run" + p + "() returns int",
                "    Box<int> x = new First<int>()",
                "    Box<int> y = new Second<int>()",
                "    return x.pick(10, 20) * 100 + y.pick(10, 20)"));
        }
        units.add(compilationUnit("Main.wurst",
            "package Main",
            "import PA",
            "import PB",
            "native testSuccess()",
            "init",
            "    if runPA() == 1020 and runPB() == 2010",
            "        testSuccess()"));
        String lua = compileAndRunToLua(name, units.toArray(new CU[0]));
        java.util.Set<String> slots = new java.util.TreeSet<>();
        java.util.regex.Matcher binding = java.util.regex.Pattern.compile("\\n\\t\\w+\\.(\\w+) = First_First_pick\\w*\\n")
            .matcher(lua);
        while (binding.find()) {
            slots.add(binding.group(1));
        }
        assertEquals(slots.size(), 2, "each package's First binds pick to a slot of its own:\n" + lua);
        List<CU> shuffled = List.of(units.get(2), units.get(1), units.get(0));
        assertEquals(compileToLua(name, shuffled), lua,
            "the slots are the same in any order of the compilation units");
    }

    /** Compiles {@code units} for every backend, runs them, and returns the Lua script written under {@code name}. */
    private String compileAndRunToLua(String name, CU... units) throws IOException {
        testNamed(name).testLua(true).luaOnly(false).executeProg().compilationUnits(units);
        return Files.toString(new File("test-output/lua/DeterministicChecks_" + name + ".lua"), Charsets.UTF_8);
    }

    /**
     * The translation of an interface asks for the methods implementing it, which makes them before their class is
     * translated. A class's overloads share a name, and the Lua backend numbers them in the order of the class's
     * functions, so that order must not follow the order in which the interfaces were translated.
     */
    @Test
    public void overloadsImplementingInterfacesOfOtherPackagesEmitTheSameLuaInAnyUnitOrder() throws IOException {
        CU intM = compilationUnit("IntM.wurst",
            "package IntM",
            "public interface IntM",
            "    function m(int x) returns int");
        CU strM = compilationUnit("StrM.wurst",
            "package StrM",
            "public interface StrM",
            "    function m(string s) returns int");
        CU lib = compilationUnit("Lib.wurst",
            "package Lib",
            "import IntM",
            "import StrM",
            "public class Base implements IntM, StrM",
            "    function m(int x) returns int",
            "        return x",
            "    function m(string s) returns int",
            "        return 7",
            "public class C<T:> extends Base");
        CU main = compilationUnit("Main.wurst",
            "package Main",
            "import IntM",
            "import StrM",
            "import Lib",
            "native testSuccess()",
            "function viaInt(IntM i) returns int",
            "    return i.m(5)",
            "function viaStr(StrM s) returns int",
            "    return s.m(\"a\")",
            "init",
            "    if viaInt(new C<int>()) == 5 and viaStr(new C<int>()) == 7",
            "        testSuccess()");
        String name = "overloadsImplementingInterfacesOfOtherPackages";
        String first = compileAndRunToLua(name, intM, strM, lib, main);
        assertEquals(compileAndRunToLua(name, strM, intM, main, lib), first,
            "the overloads of a class must be numbered in an order independent of the compilation units");
    }

    /**
     * A superclass asks for the overrides in its subclasses, and an interface for the methods implementing it, each
     * with the destroy function, when it is translated. Here they are in packages which do not import each other, so
     * which of them asks first follows the order of the compilation units, and the Lua backend emits a class's
     * functions in the order of the class's function list.
     */
    @Test
    public void functionsAskedForByUnrelatedPackagesEmitTheSameLuaInAnyUnitOrder() throws IOException {
        CU supA = compilationUnit("SupA.wurst",
            "package SupA",
            "public abstract class Base",
            "    abstract function b() returns int");
        CU supB = compilationUnit("SupB.wurst",
            "package SupB",
            "public interface HasC",
            "    function c() returns int");
        CU sub = compilationUnit("Sub.wurst",
            "package Sub",
            "import SupA",
            "import SupB",
            "public class Sub extends Base implements HasC",
            "    override function c() returns int",
            "        return 1",
            "    override function b() returns int",
            "        return 2");
        CU main = compilationUnit("Main.wurst",
            "package Main",
            "import SupA",
            "import SupB",
            "import Sub",
            "native testSuccess()",
            "init",
            "    let s = new Sub()",
            "    Base b = s",
            "    HasC h = s",
            "    if b.b() * 10 + h.c() == 21",
            "        testSuccess()",
            "    destroy s");
        String name = "functionsAskedForByUnrelatedPackages";
        String first = compileAndRunToLua(name, supA, supB, sub, main);
        assertEquals(compileAndRunToLua(name, supB, supA, main, sub), first,
            "a class's functions must be emitted in an order independent of the compilation units");
    }

    /**
     * Of two packages which import each other, either can be translated first, depending on the order of the
     * compilation units, so a call in one asks for a method of a class in the other before or after the class, and a
     * subclass in one translates its superclass in the other first or finds it translated.
     */
    @Test
    public void functionsAskedForAcrossAnImportCycleEmitTheSameLuaInAnyUnitOrder() throws IOException {
        CU cycA = compilationUnit("CycA.wurst",
            "package CycA",
            "import CycB",
            "public class C",
            "    function x() returns int",
            "        return 1",
            "    function y() returns int",
            "        return twice(2)");
        CU cycB = compilationUnit("CycB.wurst",
            "package CycB",
            "import initlater CycA",
            "public function twice(int v) returns int",
            "    return v * 2",
            "public function useY(C c) returns int",
            "    return c.y()",
            "public class D extends C",
            "    override function y() returns int",
            "        return 3");
        CU main = compilationUnit("Main.wurst",
            "package Main",
            "import CycA",
            "import CycB",
            "native testSuccess()",
            "init",
            "    let c = new C()",
            "    if c.x() + useY(c) == 5 and useY(new D()) == 3",
            "        testSuccess()");
        String name = "functionsAskedForAcrossAnImportCycle";
        String first = compileAndRunToLua(name, cycA, cycB, main);
        assertEquals(compileAndRunToLua(name, cycB, cycA, main), first,
            "a class's functions must be emitted in an order independent of the compilation units");
    }

    /**
     * The Lua backend declares the locals of the hashtable operations it prints in place, and defines the helper of
     * one it cannot, where it first needs them. Here the packages which first need each do not import each other.
     */
    @Test
    public void hashtableHelpersNeededByUnrelatedPackagesEmitTheSameLuaInAnyUnitOrder() throws IOException {
        CU first = compilationUnit("HtA.wurst",
            "package HtA",
            "@noinline function keyA(int k) returns int",
            "    return k",
            "public function useA(hashtable h) returns int",
            "    SaveInteger(h, 1, 1, 7)",
            "    return LoadInteger(h, keyA(1), 1)");
        CU second = compilationUnit("HtB.wurst",
            "package HtB",
            "@noinline function keyB(int k) returns int",
            "    return k",
            "public function useB(hashtable h) returns int",
            "    SaveReal(h, keyB(2), 1, 8.)",
            "    return R2I(LoadReal(h, 2, 1))");
        CU main = compilationUnit("Main.wurst",
            "package Main",
            "import HtA",
            "import HtB",
            "init",
            "    let h = InitHashtable()",
            "    if useA(h) + useB(h) == 15",
            "        testSuccess()");
        String name = "hashtableHelpersNeededByUnrelatedPackages";
        testNamed(name).testLua(true).withStdLib().executeProg().compilationUnits(first, second, main);
        File output = new File("test-output/lua/DeterministicChecks_" + name + ".lua");
        String forward = Files.toString(output, Charsets.UTF_8);
        testNamed(name).testLua(true).withStdLib().executeProg().compilationUnits(second, first, main);
        assertEquals(Files.toString(output, Charsets.UTF_8), forward,
            "the hashtable locals and helpers must be emitted in an order independent of the compilation units");
    }

}
