package tests.wurstscript.tests;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Opt-in: compile the real old/new library sources, never a hand-written Lua storage model. */
public class LuaArrayListBenchmark extends WurstScriptTest {
    private CU source(String property, String name) throws IOException {
        String path = System.getProperty("wurst." + property);
        if (path == null) throw new IllegalArgumentException("Supply -P" + property + "=<Wurst source path>");
        return compilationUnit(name, Files.readString(Path.of(path), StandardCharsets.UTF_8));
    }

    @Test
    public void workingCopyRegressionTests() throws IOException {
        test().withStdLib().testLua(true).luaOnly(false).inline().localOptimizations()
            .executeTests().expectAtLeastTests(50).compilationUnits(
                source("arrayListSource", "ArrayList.wurst"),
                source("arrayListTests", "ArrayListNativeStorageTests.wurst"),
                compilationUnit("Test.wurst", "package Test", "import ArrayListTests",
                    "import ArrayListMemoryTests", "import ArrayListNativeStorageTests"));
        testNamed("workingCopyLuaExecution").withStdLib().testLua(true).inline().localOptimizations()
            .executeProg().compilationUnits(
                source("arrayListSource", "ArrayList.wurst"),
                source("arrayListTests", "ArrayListNativeStorageTests.wurst"),
                compilationUnit("Test.wurst", "package Test", "import ArrayListNativeStorageTests", "init",
                    "    explicitElementLifetimeAndRecycledListId()", "    defaultsHolesAndTupleCopies()",
                    "    updateAllPreservesJassWriteAddress()",
                    "    testSuccess()"));
    }

    @Test public void arena() throws IOException { compile("arrayListBaseline", "arena"); }
    @Test public void nativeTables() throws IOException { compile("arrayListSource", "nativeTables"); }

    private void compile(String property, String name) throws IOException {
        testNamed(name).withStdLib().testLua(true).inline().localOptimizations().compilationUnits(
            source(property, "ArrayList.wurst"), compilationUnit("Probe.wurst",
                "package Probe", "import ArrayList",
                "native benchmarkRegister(code callback)",
                "native benchmarkMode() returns int",
                "native benchmarkIterations() returns int",
                "native benchmarkWidth() returns int",
                "native benchmarkResult(int result)",
                "@noinline function run(int mode, int iterations, int width) returns int",
                "    var sum = 0",
                "    if mode == 0",
                "        let list = new ArrayList<int>(width)",
                "        for i = 0 to width - 1", "            list.add(i)",
                "        for round = 1 to iterations",
                "            for i = 0 to list.size() - 1", "                sum += list.get(i)",
                "        destroy list",
                "    else if mode == 1",
                "        for round = 1 to iterations",
                "            let a = new ArrayList<int>(1)", "            let b = new ArrayList<int>(1)",
                "            for i = 0 to width - 1", "                a.add(i)", "                b.add(i + 1)",
                "            sum += a.getLast() + b.getLast()", "            destroy b", "            destroy a",
                "    else if mode == 2",
                "        for round = 1 to iterations",
                "            let list = new ArrayList<int>(width)",
                "            for i = 0 to width - 1", "                list.add(i)",
                "            sum += list.getLast()", "            destroy list",
                "    else",
                "        let list = new ArrayList<int>(width)",
                "        for round = 1 to iterations",
                "            for i = 0 to width - 1", "                list.add(i)",
                "            sum += list.getLast()",
                "            if mode == 3", "                list.reset()", "            else", "                list.clear()",
                "        destroy list",
                "    return sum",
                "function benchmark()",
                "    benchmarkResult(run(benchmarkMode(), benchmarkIterations(), benchmarkWidth()))",
                "init", "    benchmarkRegister(function benchmark)"));
    }
}
