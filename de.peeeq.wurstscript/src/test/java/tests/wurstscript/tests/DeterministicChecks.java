package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;


/**
 * These tests are supposed to check, whether the compiler is determinisitic
 */
public class DeterministicChecks extends WurstScriptTest {


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
     * alone got {@code temp0} and the same program compiled after other work got {@code temp70} —
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
    }

    @Test
    public void closureTypeIdOrderIsDeterministic() {
        CompilationResult res = test()
            .setStopOnFirstError(false)
            .executeProg(false)
            .lines(
                "package test",
                "interface Callback",
                "    function call()",
                "function run(Callback cb)",
                "    cb.call()",
                "init",
                "    run(() -> begin",
                "        int x = 1",
                "    end)",
                "    run(() -> begin",
                "        int y = 2",
                "    end)"
            );

        de.peeeq.wurstscript.translation.imtranslation.ImTranslator tr =
            new de.peeeq.wurstscript.translation.imtranslation.ImTranslator(res.getModel(), false, new de.peeeq.wurstscript.RunArgs());
        de.peeeq.wurstscript.jassIm.ImProg prog = tr.translateProg();
        java.util.Map<de.peeeq.wurstscript.jassIm.ImClass, Integer> typeIds = de.peeeq.wurstscript.translation.imtranslation.TypeId.calculate(prog);
        java.util.List<Integer> ids = new java.util.ArrayList<>(typeIds.values());
        for (int i = 0; i < ids.size(); i++) {
            assertEquals((int) ids.get(i), i + 1);
        }
    }

    @Test
    public void packageFunctionAndClosureNamingIsDeterministic() throws IOException {
        test().testLua(true).compilationUnits(
            compilationUnit("PkgA.wurst",
                "package PkgA",
                "interface Callback",
                "    function run()",
                "public function update()",
                "    Callback cb = () -> begin",
                "        int x = 1",
                "    end",
                "    cb.run()",
                "init",
                "    update()"
            ),
            compilationUnit("PkgB.wurst",
                "package PkgB",
                "public function update()",
                "init",
                "    update()"
            )
        );

        String output = Files.toString(new File("test-output/lua/DeterministicChecks_packageFunctionAndClosureNamingIsDeterministic.lua"), Charsets.UTF_8);
        AssertJUnit.assertTrue(output.contains("PkgA__update"));
        AssertJUnit.assertTrue(output.contains("PkgB__update"));
        AssertJUnit.assertTrue(output.contains("Callback_L"));
    }

    @Test
    public void modularChunkEmissionAndRapidAssembly() throws IOException {
        File tempCacheDir = java.nio.file.Files.createTempDirectory("wurst_cache_test1").toFile();
        try {
            test().incremental().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(
                compilationUnit("PkgA.wurst",
                    "package PkgA",
                    "public function calc(int x) returns int",
                    "    return x * 2",
                    "init",
                    "    calc(5)"
                ),
                compilationUnit("PkgB.wurst",
                    "package PkgB",
                    "import PkgA",
                    "public function run() returns int",
                    "    return calc(10) + 1",
                    "init",
                    "    run()"
                )
            );

            File[] cachedFiles = tempCacheDir.listFiles((dir, name) -> name.endsWith(".lua"));
            AssertJUnit.assertNotNull(cachedFiles);
            AssertJUnit.assertTrue(cachedFiles.length >= 2);
            boolean hasPkgA = false;
            boolean hasPkgB = false;
            for (File f : cachedFiles) {
                if (f.getName().startsWith("PkgA_")) hasPkgA = true;
                if (f.getName().startsWith("PkgB_")) hasPkgB = true;
            }
            AssertJUnit.assertTrue(hasPkgA);
            AssertJUnit.assertTrue(hasPkgB);

            String output = Files.toString(new File("test-output/lua/DeterministicChecks_modularChunkEmissionAndRapidAssembly.lua"), Charsets.UTF_8);
            AssertJUnit.assertTrue(output.contains("PkgA__calc"));
            AssertJUnit.assertTrue(output.contains("PkgB__run"));
            AssertJUnit.assertTrue(output.contains("__wurst_init_bootstrap"));
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(tempCacheDir);
        }
    }

    @Test
    public void incrementalBuildAchievesFullCacheHitAndBitExactOutput() throws IOException {
        File tempCacheDir = java.nio.file.Files.createTempDirectory("wurst_cache_test2").toFile();
        try {
            CU cuA = compilationUnit("PkgA.wurst",
                "package PkgA",
                "public function calc(int x) returns int",
                "    return x * 2",
                "init",
                "    calc(5)"
            );
            CU cuB = compilationUnit("PkgB.wurst",
                "package PkgB",
                "import PkgA",
                "public function run() returns int",
                "    return calc(10) + 1",
                "init",
                "    run()"
            );

            // Run 1: Cold build - populate cache
            test().incremental().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(cuA, cuB);
            File outFile = new File("test-output/lua/DeterministicChecks_incrementalBuildAchievesFullCacheHitAndBitExactOutput.lua");
            String output1 = Files.toString(outFile, Charsets.UTF_8);

            // Record modification timestamps of cached chunks
            File[] cachedFiles = tempCacheDir.listFiles((dir, name) -> name.endsWith(".lua"));
            AssertJUnit.assertNotNull(cachedFiles);
            AssertJUnit.assertEquals(2, cachedFiles.length);
            Map<String, Long> timestamps = new HashMap<>();
            for (File f : cachedFiles) {
                timestamps.put(f.getName(), f.lastModified());
            }

            // Run 2: Warm build with identical source
            test().incremental().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(cuA, cuB);
            String output2 = Files.toString(outFile, Charsets.UTF_8);

            // Verify bit-exact reproducible output
            assertEquals(output1, output2);

            // Verify cache files were not overwritten (full cache hit)
            for (File f : cachedFiles) {
                assertEquals((long) timestamps.get(f.getName()), f.lastModified());
            }
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(tempCacheDir);
        }
    }

    @Test
    public void packageModificationCausesCacheMissOnlyForModifiedPackage() throws IOException {
        File tempCacheDir = java.nio.file.Files.createTempDirectory("wurst_cache_test3").toFile();
        try {
            CU cuA = compilationUnit("PkgA.wurst",
                "package PkgA",
                "public function funcA() returns int",
                "    return 1",
                "init",
                "    funcA()"
            );
            CU cuB1 = compilationUnit("PkgB.wurst",
                "package PkgB",
                "public function funcB() returns int",
                "    return 10",
                "init",
                "    funcB()"
            );
            CU cuC = compilationUnit("PkgC.wurst",
                "package PkgC",
                "public function funcC() returns int",
                "    return 100",
                "init",
                "    funcC()"
            );

            // Build 1: Cold build
            test().incremental().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(cuA, cuB1, cuC);

            File[] cachedFiles1 = tempCacheDir.listFiles((dir, name) -> name.endsWith(".lua"));
            AssertJUnit.assertNotNull(cachedFiles1);
            AssertJUnit.assertEquals(3, cachedFiles1.length);
            String pkgAFile = null;
            String pkgCFile = null;
            for (File f : cachedFiles1) {
                if (f.getName().startsWith("PkgA_")) pkgAFile = f.getName();
                if (f.getName().startsWith("PkgC_")) pkgCFile = f.getName();
            }
            AssertJUnit.assertNotNull(pkgAFile);
            AssertJUnit.assertNotNull(pkgCFile);

            // Build 2: Modify only PkgB
            CU cuB2 = compilationUnit("PkgB.wurst",
                "package PkgB",
                "public function funcB() returns int",
                "    return 20",
                "init",
                "    funcB()"
            );
            test().incremental().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(cuA, cuB2, cuC);

            // Verify PkgA and PkgC cache files are still intact
            AssertJUnit.assertTrue(new File(tempCacheDir, pkgAFile).exists());
            AssertJUnit.assertTrue(new File(tempCacheDir, pkgCFile).exists());

            String output = Files.toString(new File("test-output/lua/DeterministicChecks_packageModificationCausesCacheMissOnlyForModifiedPackage.lua"), Charsets.UTF_8);
            AssertJUnit.assertTrue(output.contains("PkgB__funcB"));
            AssertJUnit.assertTrue(output.contains("20"));
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(tempCacheDir);
        }
    }

    @Test
    public void incrementalDisablesCrossPackageInliningAndDce() throws IOException {
        File tempCacheDir = java.nio.file.Files.createTempDirectory("wurst_cache_test4").toFile();
        try {
            CU cuLib = compilationUnit("Lib.wurst",
                "package Lib",
                "public function leafCalc(int x) returns int",
                "    return x + 42",
                "public function unusedHelper() returns int",
                "    return 999",
                "init",
                "    leafCalc(1)"
            );
            CU cuMain = compilationUnit("Main.wurst",
                "package Main",
                "import Lib",
                "public function run() returns int",
                "    return leafCalc(10)",
                "init",
                "    run()"
            );

            // Incremental build with inlining enabled (-incremental -inline)
            test().incremental().inline().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(cuLib, cuMain);

            String output = Files.toString(new File("test-output/lua/DeterministicChecks_incrementalDisablesCrossPackageInliningAndDce.lua"), Charsets.UTF_8);
            // 1. Cross-package function call is NOT inlined
            AssertJUnit.assertTrue("Cross-package call to leafCalc should not be inlined",
                output.contains("Lib__leafCalc(10)"));
            // 2. Dead code elimination is disabled: unusedHelper is preserved
            AssertJUnit.assertTrue("Unused function should be retained in incremental mode",
                output.contains("Lib__unusedHelper"));
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(tempCacheDir);
        }
    }

    @Test
    public void modularLuaAssemblyExecutesCorrectly() throws IOException {
        File tempCacheDir = java.nio.file.Files.createTempDirectory("wurst_cache_test5").toFile();
        try {
            test().incremental().executeProg().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(
                compilationUnit("PkgA.wurst",
                    "package PkgA",
                    "public interface Greeter",
                    "    function greet() returns string",
                    "public class FriendlyGreeter implements Greeter",
                    "    override function greet() returns string",
                    "        return \"hello\"",
                    "init",
                    "    Greeter g = new FriendlyGreeter()",
                    "    destroy g"
                ),
                compilationUnit("PkgB.wurst",
                    "package PkgB",
                    "import PkgA",
                    "native testSuccess()",
                    "init",
                    "    Greeter g = new FriendlyGreeter()",
                    "    if g.greet() == \"hello\"",
                    "        testSuccess()",
                    "    destroy g"
                )
            );
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(tempCacheDir);
        }
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

        // 1. Standard Lua translation: Pass 1 in order [A, B, C, D, E]
        test().testLua(true).executeProg().compilationUnits(cuA, cuB, cuC, cuD, cuE);
        File outFile = new File("test-output/lua/DeterministicChecks_multiPackageShuffledCompilationUnitOrderIsBitExact.lua");
        String outputStd1 = Files.toString(outFile, Charsets.UTF_8);
        String hashStd1 = Hashing.sha256().hashString(outputStd1, Charsets.UTF_8).toString();

        // Standard Lua translation: Pass 2 in shuffled order [D, A, E, C, B]
        test().testLua(true).executeProg().compilationUnits(cuD, cuA, cuE, cuC, cuB);
        String outputStd2 = Files.toString(outFile, Charsets.UTF_8);
        String hashStd2 = Hashing.sha256().hashString(outputStd2, Charsets.UTF_8).toString();

        assertEquals(hashStd1, hashStd2, "SHA-256 hash must be identical across shuffled compilation unit order");
        assertEquals(outputStd1, outputStd2, "Output must be bit-for-bit identical across shuffled compilation unit order");

        // 2. Incremental Lua translation: Shuffled order with disk chunk cache
        File tempCacheDir = java.nio.file.Files.createTempDirectory("wurst_cache_shuffled").toFile();
        try {
            // Cold build with order [B, C, A, E, D]
            test().incremental().executeProg().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(cuB, cuC, cuA, cuE, cuD);
            String outputInc1 = Files.toString(outFile, Charsets.UTF_8);
            String hashInc1 = Hashing.sha256().hashString(outputInc1, Charsets.UTF_8).toString();

            // Re-run with reverse order [D, E, A, C, B]
            test().incremental().executeProg().cachePath(tempCacheDir.getAbsolutePath()).compilationUnits(cuD, cuE, cuA, cuC, cuB);
            String outputInc2 = Files.toString(outFile, Charsets.UTF_8);
            String hashInc2 = Hashing.sha256().hashString(outputInc2, Charsets.UTF_8).toString();

            assertEquals(hashInc1, hashInc2, "Incremental build SHA-256 hash must be identical across shuffled compilation unit order");
            assertEquals(outputInc1, outputInc2, "Incremental build output must be bit-for-bit identical across shuffled compilation unit order");
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(tempCacheDir);
        }
    }

    @Test
    public void incrementalRebuildBenchmarkingAndSelectiveInvalidation() throws IOException, InterruptedException {
        File tempCacheDir = java.nio.file.Files.createTempDirectory("wurst_bench_cache").toFile();
        try {
            CU cuMath = compilationUnit("LibMath.wurst",
                "package LibMath",
                "public function calcSquare(int x) returns int",
                "    return x * x",
                "public function calcCube(int x) returns int",
                "    return x * x * x"
            );
            CU cuString = compilationUnit("LibString.wurst",
                "package LibString",
                "public function wrapTag(string tag, string content) returns string",
                "    return \"<\" + tag + \">\" + content + \"</\" + tag + \">\""
            );
            CU cuEntity = compilationUnit("LibEntity.wurst",
                "package LibEntity",
                "public interface Entity",
                "    function getScore() returns int",
                "public class UnitEntity implements Entity",
                "    int kills = 5",
                "    override function getScore() returns int",
                "        return kills * 10"
            );
            CU cuLogicV1 = compilationUnit("GameLogic.wurst",
                "package GameLogic",
                "import LibMath",
                "import LibString",
                "import LibEntity",
                "public function computeBonus() returns int",
                "    return 42",
                "public function evaluateGame() returns int",
                "    Entity e = new UnitEntity()",
                "    int s = e.getScore() + calcSquare(3) + computeBonus()",
                "    destroy e",
                "    return s"
            );
            CU cuMain = compilationUnit("Main.wurst",
                "package Main",
                "import GameLogic",
                "import LibString",
                "native testSuccess()",
                "init",
                "    int total = evaluateGame()",
                "    string tag = wrapTag(\"score\", \"ok\")",
                "    if total > 0 and tag == \"<score>ok</score>\"",
                "        testSuccess()"
            );

            // 1. Cold build: populate chunk cache for all 5 packages
            test().incremental().executeProg().cachePath(tempCacheDir.getAbsolutePath())
                .compilationUnits(cuMath, cuString, cuEntity, cuLogicV1, cuMain);

            File[] cachedChunks1 = tempCacheDir.listFiles((dir, name) -> name.endsWith(".lua"));
            AssertJUnit.assertNotNull(cachedChunks1);
            AssertJUnit.assertEquals(5, cachedChunks1.length);

            Map<String, Long> timestamps1 = new HashMap<>();
            Map<String, String> hashes1 = new HashMap<>();
            String logicChunkName1 = null;
            for (File f : cachedChunks1) {
                timestamps1.put(f.getName(), f.lastModified());
                hashes1.put(f.getName(), Files.asByteSource(f).hash(Hashing.sha256()).toString());
                if (f.getName().startsWith("GameLogic_")) {
                    logicChunkName1 = f.getName();
                }
            }
            AssertJUnit.assertNotNull("GameLogic chunk must be present in cache", logicChunkName1);

            // Verify cold build output
            File outFile = new File("test-output/lua/DeterministicChecks_incrementalRebuildBenchmarkingAndSelectiveInvalidation.lua");
            String outputV1 = Files.toString(outFile, Charsets.UTF_8);
            AssertJUnit.assertTrue(outputV1.contains("42"));

            // Sleep briefly to ensure filesystem timestamp resolution ticks
            Thread.sleep(60);

            // 2. Warm rebuild: Modify ONLY the function body of computeBonus in GameLogic
            CU cuLogicV2 = compilationUnit("GameLogic.wurst",
                "package GameLogic",
                "import LibMath",
                "import LibString",
                "import LibEntity",
                "public function computeBonus() returns int",
                "    return 100",
                "public function evaluateGame() returns int",
                "    Entity e = new UnitEntity()",
                "    int s = e.getScore() + calcSquare(3) + computeBonus()",
                "    destroy e",
                "    return s"
            );

            long startRebuild = System.currentTimeMillis();
            test().incremental().executeProg().cachePath(tempCacheDir.getAbsolutePath())
                .compilationUnits(cuMath, cuString, cuEntity, cuLogicV2, cuMain);
            long elapsedRebuildMs = System.currentTimeMillis() - startRebuild;

            // 3. Performance assertion: incremental rebuild and assembly must complete in <500ms
            System.out.println("Incremental warm rebuild turnaround: " + elapsedRebuildMs + "ms");
            AssertJUnit.assertTrue("Incremental rebuild should be rapid (<500ms), but took: " + elapsedRebuildMs + "ms",
                elapsedRebuildMs < 500);

            // 4. Cache hit/miss validation
            File[] cachedChunks2 = tempCacheDir.listFiles((dir, name) -> name.endsWith(".lua"));
            AssertJUnit.assertNotNull(cachedChunks2);
            // Cache contains 6 files: 4 untouched packages + old GameLogic chunk + new GameLogic chunk
            AssertJUnit.assertEquals(6, cachedChunks2.length);

            // All untouched packages (LibMath, LibString, LibEntity, Main) must hit the cache exactly
            int touchedCount = 0;
            int untouchedHitCount = 0;
            for (File f : cachedChunks2) {
                String name = f.getName();
                if (name.startsWith("GameLogic_")) {
                    touchedCount++;
                } else {
                    untouchedHitCount++;
                    // Must be an exact cache hit (same timestamp and same content hash)
                    AssertJUnit.assertEquals("Untouched chunk " + name + " must maintain same timestamp (cache hit)",
                        timestamps1.get(name), (Long) f.lastModified());
                    AssertJUnit.assertEquals("Untouched chunk " + name + " must have identical hash (cache hit)",
                        hashes1.get(name), Files.asByteSource(f).hash(Hashing.sha256()).toString());
                }
            }
            AssertJUnit.assertEquals("Should have 2 chunks for modified package (old and new)", 2, touchedCount);
            AssertJUnit.assertEquals("Should have 4 exact chunk cache hits for untouched packages", 4, untouchedHitCount);

            // 5. Output lua must reflect the updated function body
            String outputV2 = Files.toString(outFile, Charsets.UTF_8);
            AssertJUnit.assertTrue("Rebuilt script should contain updated value 100", outputV2.contains("100"));
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(tempCacheDir);
        }
    }

}
