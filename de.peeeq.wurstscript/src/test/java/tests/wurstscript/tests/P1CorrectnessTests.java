package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Repros for the six remaining P1 correctness bugs on PR #1353 (Frotty 2026-10-04).
 * Each test mirrors the inline review comment's program as closely as practical.
 */
public class P1CorrectnessTests extends WurstScriptTest {

    @Test
    public void mutualRecursiveGenericsReachingWurstNewSpecializeCleanly() throws IOException {
        // Bug 1 / EliminateGenerics: a->b->c->a reaching wurstNewInstance must eliminate the marker
        // during specialization (ordinary Lua, no -incremental).
        test().testLua(true).executeProg().lines(
            "package Test",
            "native testSuccess()",
            "class Box",
            "    int value = 7",
            "function a<T:>(bool recurse) returns T",
            "    if recurse",
            "        return b<T>(false)",
            "    return wurstNewInstance<T>()",
            "function b<T:>(bool recurse) returns T",
            "    return c<T>(recurse)",
            "function c<T:>(bool recurse) returns T",
            "    return a<T>(recurse)",
            "init",
            "    let first = a<Box>(false)",
            "    let second = b<Box>(false)",
            "    if first.value == 7 and second.value == 7",
            "        testSuccess()"
        );

        String lua = Files.toString(new File(
            "test-output/lua/P1CorrectnessTests_mutualRecursiveGenericsReachingWurstNewSpecializeCleanly.lua"),
            Charsets.UTF_8);
        AssertJUnit.assertFalse(
            "Specialization must eliminate wurstNewMarker; later recovery must not be required",
            lua.contains("wurstNewMarker"));
        AssertJUnit.assertFalse(
            "Specialization must eliminate wurstNewInstance calls",
            lua.contains("wurstNewInstance"));
    }

    @Test
    public void incrementalModeAcceptsValidReturningClosure() {
        // Bug 2 / WurstChecker: cold incremental must prepare flow attributes before reachability.
        test().incremental().lines(
            "package Test",
            "interface Transformer",
            "    function transform(int x) returns int",
            "public function compute(int val) returns int",
            "    Transformer t = (int x) -> begin",
            "        return x * 2 + 1",
            "    end",
            "    return t.transform(val)"
        );
    }

    @Test
    public void chunkCacheHashesCompilerSourceSnapshotNotDisk() throws IOException {
        // Bug 3 / PackageChunkCache: warm build must use the in-memory buffer, not the disk file.
        File tempDir = java.nio.file.Files.createTempDirectory("wurst_buffer_hash").toFile();
        File cacheDir = java.nio.file.Files.createTempDirectory("wurst_buffer_cache").toFile();
        try {
            File pkgFile = new File(tempDir, "A.wurst");
            String diskSource = String.join("\n",
                "package A",
                "public function value() returns int",
                "    return 11",
                "init",
                "    value()",
                ""
            );
            Files.write(diskSource, pkgFile, Charsets.UTF_8);

            String path = pkgFile.getAbsolutePath();
            CU cuDisk = compilationUnit(path,
                "package A",
                "public function value() returns int",
                "    return 11",
                "init",
                "    value()"
            );
            test().incremental().cachePath(cacheDir.getAbsolutePath()).compilationUnits(cuDisk);

            // Disk still has 11; compile the unsaved buffer returning 22 for the same path.
            AssertJUnit.assertTrue(Files.toString(pkgFile, Charsets.UTF_8).contains("return 11"));

            CU cuBuffer = compilationUnit(path,
                "package A",
                "public function value() returns int",
                "    return 22",
                "init",
                "    value()"
            );
            test().incremental().cachePath(cacheDir.getAbsolutePath()).compilationUnits(cuBuffer);

            String lua = Files.toString(new File(
                "test-output/lua/P1CorrectnessTests_chunkCacheHashesCompilerSourceSnapshotNotDisk.lua"),
                Charsets.UTF_8);
            int valueFn = lua.indexOf("function A__value");
            AssertJUnit.assertTrue("Expected A__value in output", valueFn >= 0);
            String valueBody = lua.substring(valueFn, Math.min(lua.length(), valueFn + 160));
            AssertJUnit.assertTrue("value() must return 22 from the unsaved buffer:\n" + valueBody,
                valueBody.contains("return 22"));
            AssertJUnit.assertFalse("value() must not still return the disk value 11:\n" + valueBody,
                valueBody.contains("return 11"));
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(tempDir);
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
        }
    }

    private String readEmittedLua(String testMethodName) throws IOException {
        File lua = new File(
            "test-output/lua/P1CorrectnessTests_" + testMethodName + ".lua");
        AssertJUnit.assertTrue("Expected emitted Lua at " + lua.getPath(), lua.isFile());
        return Files.toString(lua, Charsets.UTF_8);
    }

    @Test
    public void importedFunctionBodyChangeInvalidatesCompiletimeConstant() throws IOException {
        // Bug 4 / PackageChunkCache: implementation deps of compiletime must invalidate consumers.
        // The consumer B is byte-identical across the second and third builds: only A's body
        // changes. A stale B chunk would keep `answer = 11`; warm must equal a clean rebuild.
        File cacheDir = java.nio.file.Files.createTempDirectory("wurst_cte_cache").toFile();
        File cleanCacheDir = java.nio.file.Files.createTempDirectory("wurst_cte_clean").toFile();
        try {
            CU cuA11 = compilationUnit("A.wurst",
                "package A",
                "public function value() returns int",
                "    return 11"
            );
            // NOTE: shared instance, reused verbatim for the warm and clean rebuilds below.
            CU cuB = compilationUnit("B.wurst",
                "package B",
                "import A",
                "native testSuccess()",
                "function compiletime(int i) returns int",
                "    return i",
                "constant answer = compiletime(value())",
                "init",
                "    if answer == 11",
                "        testSuccess()"
            );

            // Cold baseline: executes, proving answer folds to 11.
            test().incremental().executeProg().runCompiletimeFunctions(true)
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA11, cuB);
            String coldLua = readEmittedLua("importedFunctionBodyChangeInvalidatesCompiletimeConstant");

            CU cuA22 = compilationUnit("A.wurst",
                "package A",
                "public function value() returns int",
                "    return 22"
            );

            // Warm rebuild with the same cache, B untouched (translate only: B still expects 11).
            test().incremental().runCompiletimeFunctions(true)
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA22, cuB);
            String warmLua = readEmittedLua("importedFunctionBodyChangeInvalidatesCompiletimeConstant");

            // Clean rebuild of the same new sources with a fresh cache.
            test().incremental().runCompiletimeFunctions(true)
                .cachePath(cleanCacheDir.getAbsolutePath())
                .compilationUnits(cuA22, cuB);
            String cleanLua = readEmittedLua("importedFunctionBodyChangeInvalidatesCompiletimeConstant");

            AssertJUnit.assertFalse("Rebuild must reflect the changed import body",
                warmLua.equals(coldLua));
            AssertJUnit.assertEquals("Warm build must agree with a clean build",
                cleanLua, warmLua);
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cleanCacheDir);
        }
    }

    @Test
    public void initlaterAbiChangeInvalidatesConsumerChunk() throws IOException {
        // Bug 5 / PackageChunkCache: import initlater still supplies types used to compile.
        // B is byte-identical across rebuilds; only A's tuple layout changes from
        // Pair(x, y) to Pair(y, x), so p.x flips from 11 to 22.
        File cacheDir = java.nio.file.Files.createTempDirectory("wurst_initlater_cache").toFile();
        File cleanCacheDir = java.nio.file.Files.createTempDirectory("wurst_initlater_clean").toFile();
        try {
            CU cuA1 = compilationUnit("A.wurst",
                "package A",
                "public tuple Pair(int x, int y)"
            );
            // NOTE: shared instance, reused verbatim for the warm and clean rebuilds below.
            CU cuB = compilationUnit("B.wurst",
                "package B",
                "import initlater A",
                "native testSuccess()",
                "function sink(int v)",
                "    if v == 11",
                "        testSuccess()",
                "init",
                "    let p = Pair(11, 22)",
                "    sink(p.x)"
            );

            // Cold baseline: executes, proving p.x is 11 under the old layout.
            test().incremental().executeProg()
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA1, cuB);
            String coldLua = readEmittedLua("initlaterAbiChangeInvalidatesConsumerChunk");

            CU cuA2 = compilationUnit("A.wurst",
                "package A",
                "public tuple Pair(int y, int x)"
            );

            // Warm rebuild, B untouched (translate only: B still expects 11).
            test().incremental()
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA2, cuB);
            String warmLua = readEmittedLua("initlaterAbiChangeInvalidatesConsumerChunk");

            // Clean rebuild of the same new sources with a fresh cache.
            test().incremental()
                .cachePath(cleanCacheDir.getAbsolutePath())
                .compilationUnits(cuA2, cuB);
            String cleanLua = readEmittedLua("initlaterAbiChangeInvalidatesConsumerChunk");

            AssertJUnit.assertFalse("Rebuild must reflect the changed tuple layout",
                warmLua.equals(coldLua));
            AssertJUnit.assertEquals("Warm build must agree with a clean build",
                cleanLua, warmLua);
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cleanCacheDir);
        }
    }

    @Test
    public void addingUnrelatedPackageDoesNotDuplicateTypeIds() throws IOException {
        // Bug 6 / LuaTranslator: cached chunks must not keep stale whole-program typeIds.
        // ZPkg is byte-identical across rebuilds; only the unrelated APkg is added. Clean
        // numbering is A=1, Z=2 (APkg sorts first); a stale Z chunk would keep Z=1 and
        // duplicate A's id, including in baked ImTypeIdOfClass literals that the postamble
        // overwrite cannot repair.
        File cacheDir = java.nio.file.Files.createTempDirectory("wurst_typeid_cache").toFile();
        File cleanCacheDir = java.nio.file.Files.createTempDirectory("wurst_typeid_clean").toFile();
        try {
            // NOTE: shared instance, reused verbatim for the warm and clean rebuilds below.
            CU cuZ = compilationUnit("ZPkg.wurst",
                "package ZPkg",
                "public class Z",
                "    int marker = 1",
                "native testSuccess()",
                "init",
                "    if Z.typeId > 0",
                "        testSuccess()"
            );

            test().incremental().executeProg()
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuZ);
            String coldLua = readEmittedLua("addingUnrelatedPackageDoesNotDuplicateTypeIds");

            CU cuA = compilationUnit("APkg.wurst",
                "package APkg",
                "public class A",
                "    int marker = 2"
            );

            // Warm rebuild with the same cache, Z untouched.
            test().incremental().executeProg()
                .cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA, cuZ);
            String warmLua = readEmittedLua("addingUnrelatedPackageDoesNotDuplicateTypeIds");

            // Clean rebuild of the same sources with a fresh cache.
            test().incremental().executeProg()
                .cachePath(cleanCacheDir.getAbsolutePath())
                .compilationUnits(cuA, cuZ);
            String cleanLua = readEmittedLua("addingUnrelatedPackageDoesNotDuplicateTypeIds");

            AssertJUnit.assertTrue("Clean build must emit both packages",
                cleanLua.contains("__wurst_bootstrap_APkg") && cleanLua.contains("__wurst_bootstrap_ZPkg"));
            AssertJUnit.assertFalse("Adding a package must change whole-program numbering",
                warmLua.equals(coldLua));
            AssertJUnit.assertEquals("Warm build must agree with a clean build",
                cleanLua, warmLua);
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cleanCacheDir);
        }
    }
}
