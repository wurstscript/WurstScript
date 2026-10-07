package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * A chunk written by an earlier build names its functions, globals and classes. Those names must be the
 * ones a later build gives them, also when only some packages are translated again: each test builds with a
 * warm chunk cache, runs the program, and compares the script with the one of a build from an empty cache.
 */
public class IncrementalNamingTests extends WurstScriptTest {

    private String readEmittedLua(String testMethodName) throws IOException {
        File lua = new File("test-output/lua/IncrementalNamingTests_" + testMethodName + ".lua");
        AssertJUnit.assertTrue("Expected emitted Lua at " + lua.getPath(), lua.isFile());
        return Files.toString(lua, Charsets.UTF_8);
    }

    /** Fails with the first line which differs, as two scripts of thousands of lines are not readable. */
    private static void assertSameScript(String clean, String warm) {
        if (clean.equals(warm)) {
            return;
        }
        String[] a = clean.split("\n");
        String[] b = warm.split("\n");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            String x = i < a.length ? a[i] : "<end>";
            String y = i < b.length ? b[i] : "<end>";
            if (!x.equals(y)) {
                AssertJUnit.fail("Warm build differs from a clean build at line " + (i + 1)
                    + "\n  clean: " + x + "\n  warm:  " + y);
            }
        }
    }

    private static File tempDir(String prefix) throws IOException {
        return java.nio.file.Files.createTempDirectory(prefix).toFile();
    }

    @Test
    public void overloadsKeepTheirNamesWhenOnlyAnImporterChanges() throws IOException {
        File cacheDir = tempDir("wurst_naming_overload");
        File cleanCacheDir = tempDir("wurst_naming_overload_clean");
        try {
            CU cuA = compilationUnit("A.wurst",
                "package A",
                "public function pick(int x) returns int",
                "    return 1",
                "public function pick(real x) returns int",
                "    return 2"
            );
            CU cuB1 = compilationUnit("B.wurst",
                "package B",
                "import A",
                "public function useReal() returns int",
                "    return pick(1.5)",
                "public function bExtra() returns int",
                "    return 5"
            );
            CU cuB2 = compilationUnit("B.wurst",
                "package B",
                "import A",
                "public function useReal() returns int",
                "    return pick(1.5)",
                "public function bExtra() returns int",
                "    return 6"
            );
            CU cuMain = compilationUnit("Main.wurst",
                "package Main",
                "import B",
                "native testSuccess()",
                "init",
                "    if useReal() == 2",
                "        testSuccess()"
            );

            test().incremental().executeProg().cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA, cuB1, cuMain);
            // A is reused, B is translated again: B has to call the same name for pick(real) as before.
            test().incremental().executeProg().cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA, cuB2, cuMain);
            String warm = readEmittedLua("overloadsKeepTheirNamesWhenOnlyAnImporterChanges");

            test().incremental().executeProg().cachePath(cleanCacheDir.getAbsolutePath())
                .compilationUnits(cuA, cuB2, cuMain);
            String clean = readEmittedLua("overloadsKeepTheirNamesWhenOnlyAnImporterChanges");
            assertSameScript(clean, warm);
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cleanCacheDir);
        }
    }

    @Test
    public void classesOfTheSameNameInTwoPackagesKeepTheirNames() throws IOException {
        File cacheDir = tempDir("wurst_naming_class");
        File cleanCacheDir = tempDir("wurst_naming_class_clean");
        try {
            CU cuA = compilationUnit("A.wurst",
                "package A",
                "class Foo",
                "    function val() returns int",
                "        return 1",
                "public function aVal() returns int",
                "    let f = new Foo()",
                "    let r = f.val()",
                "    destroy f",
                "    return r"
            );
            CU cuB1 = compilationUnit("B.wurst",
                "package B",
                "class Foo",
                "    function val() returns int",
                "        return 2",
                "public function bVal() returns int",
                "    let f = new Foo()",
                "    let r = f.val()",
                "    destroy f",
                "    return r",
                "public function bExtra() returns int",
                "    return 5"
            );
            CU cuB2 = compilationUnit("B.wurst",
                "package B",
                "class Foo",
                "    function val() returns int",
                "        return 2",
                "public function bVal() returns int",
                "    let f = new Foo()",
                "    let r = f.val()",
                "    destroy f",
                "    return r",
                "public function bExtra() returns int",
                "    return 6"
            );
            CU cuMain = compilationUnit("Main.wurst",
                "package Main",
                "import A",
                "import B",
                "native testSuccess()",
                "init",
                "    if aVal() == 1 and bVal() == 2",
                "        testSuccess()"
            );

            test().incremental().executeProg().cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA, cuB1, cuMain);
            test().incremental().executeProg().cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA, cuB2, cuMain);
            String warm = readEmittedLua("classesOfTheSameNameInTwoPackagesKeepTheirNames");

            test().incremental().executeProg().cachePath(cleanCacheDir.getAbsolutePath())
                .compilationUnits(cuA, cuB2, cuMain);
            String clean = readEmittedLua("classesOfTheSameNameInTwoPackagesKeepTheirNames");
            assertSameScript(clean, warm);
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cleanCacheDir);
        }
    }

    /** Adding a private overload renumbers the public ones without touching the package's public interface. */
    @Test
    public void aPrivateOverloadRenumbersPublicOnesForCachedImporters() throws IOException {
        File cacheDir = tempDir("wurst_naming_private");
        File cleanCacheDir = tempDir("wurst_naming_private_clean");
        try {
            CU cuA1 = compilationUnit("A.wurst",
                "package A",
                "public function pick(int x) returns int",
                "    return 1",
                "public function pick(real x) returns int",
                "    return 2"
            );
            CU cuA2 = compilationUnit("A.wurst",
                "package A",
                "function pick(bool b) returns int",
                "    return 3",
                "public function pick(int x) returns int",
                "    return 1",
                "public function pick(real x) returns int",
                "    return 2"
            );
            CU cuB = compilationUnit("B.wurst",
                "package B",
                "import A",
                "public function useReal() returns int",
                "    return pick(1.5)"
            );
            CU cuMain = compilationUnit("Main.wurst",
                "package Main",
                "import B",
                "native testSuccess()",
                "init",
                "    if useReal() == 2",
                "        testSuccess()"
            );

            test().incremental().executeProg().cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA1, cuB, cuMain);
            // B is byte-identical and A's public interface is unchanged, but A now numbers its overloads differently.
            test().incremental().executeProg().cachePath(cacheDir.getAbsolutePath())
                .compilationUnits(cuA2, cuB, cuMain);
            String warm = readEmittedLua("aPrivateOverloadRenumbersPublicOnesForCachedImporters");

            test().incremental().executeProg().cachePath(cleanCacheDir.getAbsolutePath())
                .compilationUnits(cuA2, cuB, cuMain);
            String clean = readEmittedLua("aPrivateOverloadRenumbersPublicOnesForCachedImporters");
            assertSameScript(clean, warm);
        } finally {
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cacheDir);
            de.peeeq.wurstio.utils.FileUtils.deleteRecursively(cleanCacheDir);
        }
    }
}
