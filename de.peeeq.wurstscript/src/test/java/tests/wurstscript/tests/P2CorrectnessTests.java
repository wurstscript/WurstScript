package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstscript.attributes.CompileError;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Repros for Frotty's second-round findings on PR #1353: shared-helper ownership and
 * incremental validation gaps. Each test pairs a warm incremental build against a clean
 * rebuild of the same sources (or asserts an error the buggy version accepts).
 */
public class P2CorrectnessTests extends WurstScriptTest {

    private String readEmittedLua(String testMethodName) throws IOException {
        File lua = new File(
            "test-output/lua/P2CorrectnessTests_" + testMethodName + ".lua");
        AssertJUnit.assertTrue("Expected emitted Lua at " + lua.getPath(), lua.isFile());
        return Files.toString(lua, Charsets.UTF_8);
    }


    @Test
    public void coldIncrementalEmitsInstanceofHelper() throws IOException {
        // Shared runtime helpers must be owned by the preamble: a cold incremental build
        // emitted the instanceof call while dropping isInstanceOf into luaModel, which the
        // modular assembler never includes.
        test().incremental().executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class A",
            "class B extends A",
            "init",
            "    A a = new B()",
            "    if a instanceof B",
            "        testSuccess()"
        );
        String lua = readEmittedLua("coldIncrementalEmitsInstanceofHelper");
        AssertJUnit.assertTrue("isInstanceOf helper must be emitted", lua.contains("isInstanceOf"));
    }

    @Test
    public void warmHitKeepsSharedArrayDefaultHelper() throws IOException {
        // The int-array metatable used to live in whichever chunk triggered it first. When
        // package A (topologically first) removed its array, the cached package B chunk still
        // referencing it read nil instead of 0. Helpers are preamble-owned now.
        File cacheDir = java.nio.file.Files.createTempDirectory("wurst_arr_cache").toFile();
        File cleanCacheDir = java.nio.file.Files.createTempDirectory("wurst_arr_clean").toFile();
        try {
            CU cuA1 = compilationUnit("A.wurst",
                "package A",
                "int array aarr",
                "public function aval(int i) returns int",
                "    return aarr[i] + 1"
            );
            // NOTE: shared instance, reused verbatim for the warm and clean rebuilds below.
            CU cuB = compilationUnit("B.wurst",
                "package B",
                "import A",
                "native testSuccess()",
                "int array barr",
                "init",
                "    int x = aval(3)",
                "    if barr[7] == 0 and x == 1",
                "        testSuccess()"
            );

            test().incremental().executeProg()
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA1, cuB);

            CU cuA2 = compilationUnit("A.wurst",
                "package A",
                "int plain = 4",
                "public function aval(int i) returns int",
                "    return plain - i"
            );

            test().incremental().executeProg()
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA2, cuB);
            String warmLua = readEmittedLua("warmHitKeepsSharedArrayDefaultHelper");

            test().incremental().executeProg()
                .cachePath(cleanCacheDir.getAbsolutePath())
                .compilationUnits(cuA2, cuB);
            String cleanLua = readEmittedLua("warmHitKeepsSharedArrayDefaultHelper");

            AssertJUnit.assertEquals("Warm build must agree with a clean build",
                cleanLua, warmLua);
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cleanCacheDir);
        }
    }

    @Test
    public void coldIncrementalChecksImportedLibrary() throws IOException {
        // A return-type error inside an imported library was accepted even on a cold
        // incremental build: libraries were excluded from the checked units.
        File libDir = java.nio.file.Files.createTempDirectory("wurst_lib_err").toFile();
        try {
            Files.write(
                "package Lib\n"
                    + "public function get() returns int\n"
                    + "    return \"nope\"\n",
                new File(libDir, "Lib.wurst"), Charsets.UTF_8);
            try {
                test().incremental().libDir(libDir.getAbsolutePath()).compilationUnits(
                    compilationUnit("Main.wurst",
                        "package Main",
                        "import Lib",
                        "init",
                        "    get()")
                );
                AssertJUnit.fail("expected a return-type error inside the imported library");
            } catch (CompileError e) {
                // expected
            }
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(libDir);
        }
    }

    @Test
    public void transitiveValidationReachesUnchangedImporter() throws IOException {
        // A changed package must invalidate its importers transitively, even through an
        // already-dirty intermediate unit: dropping A.x breaks C, while B (dirty via a
        // comment, ABI unchanged) must still propagate to C.
        // Asserted directly on the filter: end-to-end the downstream pipeline reports C's
        // member error with identical text either way, so only the walk set discriminates.
        File cacheDir = java.nio.file.Files.createTempDirectory("wurst_transval_cache").toFile();
        try {
            java.util.Map<String, String> cold = new java.util.LinkedHashMap<>();
            cold.put("A.wurst", String.join("\n",
                "package A",
                "public class Base",
                "    int x = 0"));
            cold.put("B.wurst", String.join("\n",
                "package B",
                "import A",
                "public class Mid extends Base",
                "public function mid() returns int",
                "    return 1"));
            cold.put("C.wurst", String.join("\n",
                "package C",
                "import B",
                "native testSuccess()",
                "init",
                "    Mid m = new Mid()",
                "    if m.x == 0",
                "        testSuccess()"));

            test().incremental().executeProg()
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(unitsOf(cold));

            java.util.Map<String, String> warm = new java.util.LinkedHashMap<>(cold);
            warm.put("A.wurst", String.join("\n",
                "package A",
                "public class Base",
                "    int y = 0"));
            warm.put("B.wurst", String.join("\n",
                "package B",
                "import A",
                "// touch: body-only change, same ABI",
                "public class Mid extends Base",
                "public function mid() returns int",
                "    return 1"));

            de.peeeq.wurstscript.ast.WurstModel warmModel = parseOnly(warm);
            java.util.Set<String> walked = new java.util.HashSet<>();
            for (de.peeeq.wurstscript.ast.CompilationUnit cu
                : new de.peeeq.wurstscript.validation.ValidationCache(cacheDir)
                    .filterUnitsToWalk(warmModel)) {
                for (de.peeeq.wurstscript.ast.WPackage p : cu.getPackages()) {
                    walked.add(p.getName());
                }
            }
            AssertJUnit.assertEquals("walk set must be exactly the changed package, its dirty "
                + "dependent and the transitively reached importer",
                new java.util.HashSet<>(java.util.Arrays.asList("A", "B", "C")),
                walked);
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
        }
    }

    private CU[] unitsOf(java.util.Map<String, String> sources) {
        java.util.List<CU> out = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, String> e : sources.entrySet()) {
            out.add(compilationUnit(e.getKey(), e.getValue()));
        }
        return out.toArray(new CU[0]);
    }

    private de.peeeq.wurstscript.ast.WurstModel parseOnly(java.util.Map<String, String> sources) {
        de.peeeq.wurstio.WurstCompilerJassImpl compiler = new de.peeeq.wurstio.WurstCompilerJassImpl(
            null, new de.peeeq.wurstscript.gui.WurstGuiCliImpl(), null, new de.peeeq.wurstscript.RunArgs());
        for (java.util.Map.Entry<String, String> e : sources.entrySet()) {
            compiler.loadReader(e.getKey(), new java.io.StringReader(e.getValue()));
        }
        de.peeeq.wurstscript.ast.WurstModel model = compiler.parseFiles();
        AssertJUnit.assertNotNull("parse failed", model);
        return model;
    }
}
