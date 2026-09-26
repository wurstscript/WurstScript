package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * The Lua side of the compiler-owned KeyedMap operations: one native table per map, keyed by the
 * element itself, with typed reads answering the Wurst default for a missing key.
 *
 * <p>Follows the KeyedTable tests: a Jass-runnable source built on the library's Table for the
 * parity checks, and a Lua-only generic-key source for the shape the standard library uses
 * behind its isLua guard.
 */
public class LuaKeyedMapTests extends WurstScriptTest {

    private String getFunctionBody(String output, String functionName) {
        Pattern pattern = Pattern.compile("function\\s*" + functionName + "\\s*\\(.*\\).*\\n" + "((?:\\n|.)*?)end");
        Matcher matcher = pattern.matcher(output);
        if (!matcher.find()) {
            fail("Function " + functionName + " was not found.");
        }
        return matcher.group(1);
    }

    private String compiled(String testName) throws IOException {
        return Files.toString(new File("test-output/lua/LuaKeyedMapTests_" + testName + ".lua"), Charsets.UTF_8);
    }

    /** Integer-keyed source with Jass bodies, so it runs on both backends. */
    private static String[] keyedMapSource(String... usage) {
        java.util.List<String> lines = new java.util.ArrayList<>(java.util.Arrays.asList(
            "package KeyedMap",
            "import Table",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int tbl, int key, int value)",
            "    (tbl castTo Table).saveInt(key, value)",
            "@compilerintrinsic public function keyedMapGetInt(int tbl, int key) returns int",
            "    return (tbl castTo Table).loadInt(key)",
            "@compilerintrinsic public function keyedMapHas(int tbl, int key) returns boolean",
            "    return (tbl castTo Table).hasInt(key)",
            "@compilerintrinsic public function keyedMapRemove(int tbl, int key)",
            "    (tbl castTo Table).removeInt(key)",
            "@compilerintrinsic public function keyedMapDestroy(int tbl)",
            "    destroy (tbl castTo Table)",
            "endpackage"));
        lines.addAll(java.util.Arrays.asList(usage));
        return lines.toArray(new String[0]);
    }

    @Test
    public void keyedMapLowersToSingleLuaIndexes() throws IOException {
        test().testLua(true).withStdLib().lines(keyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    keyedMapPut(m, 7, 70)",
            "    print(keyedMapGetInt(m, 7).toString())",
            "    print(keyedMapHas(m, 7).toString())",
            "    keyedMapRemove(m, 7)",
            "endpackage"));

        String compiled = compiled("keyedMapLowersToSingleLuaIndexes");
        assertTrue("create allocates a bare table", getFunctionBody(compiled, "__wurst_keyedMapCreate").contains("return {}"));
        assertTrue("put is one store", getFunctionBody(compiled, "__wurst_keyedMapPut").contains("t[k] = v"));
        assertTrue("an int read answers 0 for a missing key",
            getFunctionBody(compiled, "__wurst_keyedMapGetInt").contains("return t[k] or 0"));
        assertTrue("has is one index", getFunctionBody(compiled, "__wurst_keyedMapHas").contains("] ~= nil"));
        assertTrue("remove is one store", getFunctionBody(compiled, "__wurst_keyedMapRemove").contains("t[k] = nil"));

        String init = getFunctionBody(compiled, "init_Test");
        assertFalse("the caller must not reach the hashtable natives: " + init,
            init.contains("SaveInteger") || init.contains("LoadInteger") || init.contains("HaveSavedInteger"));
        assertFalse("an int read needs no nil normalisation: " + init,
            init.contains("__wurst_ensureInt") || init.contains("__wurst_rawTo"));
    }

    @Test
    public void keyedMapAgreesOnBothBackends() {
        test().testLua(true).executeProg(true).withStdLib().lines(keyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    if keyedMapHas(m, 7) or keyedMapGetInt(m, 7) != 0",
            "        testFail(\"empty map reported 7\")",
            "    keyedMapPut(m, 7, 70)",
            "    keyedMapPut(m, -2, 5)",
            "    if not keyedMapHas(m, 7) or keyedMapGetInt(m, 7) != 70 or keyedMapGetInt(m, -2) != 5",
            "        testFail(\"stored values not read back\")",
            "    keyedMapPut(m, 7, 71)",
            "    if keyedMapGetInt(m, 7) != 71",
            "        testFail(\"overwrite not visible\")",
            "    keyedMapRemove(m, 7)",
            "    if keyedMapHas(m, 7) or keyedMapGetInt(m, 7) != 0 or keyedMapGetInt(m, -2) != 5",
            "        testFail(\"remove did not clear only its key\")",
            "    keyedMapDestroy(m)",
            "    testSuccess()",
            "endpackage"));
    }

    @Test
    public void keyedMapStaysNativeWithStackTraces() throws IOException {
        test().testLua(true).stacktraces().inline().withStdLib().lines(keyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    keyedMapPut(m, 7, 70)",
            "    if keyedMapGetInt(m, 7) == 70",
            "        print(\"stored\")",
            "endpackage"));

        String compiled = compiled("keyedMapStaysNativeWithStackTraces");
        String init = getFunctionBody(compiled, "init_Test");
        assertTrue("the caller must call the stubs directly: " + init,
            init.contains("__wurst_keyedMapGetInt") && init.contains("__wurst_keyedMapPut"));
        assertFalse("the caller must not reach the hashtable natives: " + init,
            init.contains("SaveInteger") || init.contains("LoadInteger"));
    }

    @Test
    public void keyedMapDestroyCostsNothingOnLua() throws IOException {
        test().testLua(true).stacktraces().withStdLib().lines(keyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    keyedMapPut(m, 7, 70)",
            "    keyedMapDestroy(m)",
            "endpackage"));

        String compiled = compiled("keyedMapDestroyCostsNothingOnLua");
        assertFalse("destroy must not become a native stub", compiled.contains("__wurst_keyedMapDestroy"));
        String init = getFunctionBody(compiled, "init_Test");
        assertFalse("no call should remain to free a keyed map on Lua: " + init, init.contains("keyedMapDestroy"));
        assertFalse("the Table machinery must not reach Lua: " + init,
            init.contains("FlushChildHashtable") || init.contains("Table_destroy"));
    }

    /**
     * The shape the library uses: generic keys and values, Lua-only bodies behind an isLua guard.
     * A unit key is handed over as itself, and a class value comes back without index round trips.
     */
    @Test
    public void keyedMapGenericKeyAndValueReachLuaUncast() throws IOException {
        test().testLua(true).inline().withStdLib().lines(
            "package KeyedMap",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return 0",
            "@compilerintrinsic public function keyedMapPut<K:, V:>(int tbl, K key, V value)",
            "    skip",
            "@compilerintrinsic public function keyedMapGet<K:, V:>(int tbl, K key) returns V",
            "    return null",
            "@compilerintrinsic public function keyedMapGetInt<K:>(int tbl, K key) returns int",
            "    return 0",
            "@compilerintrinsic public function keyedMapHas<K:>(int tbl, K key) returns boolean",
            "    return false",
            "endpackage",
            "package Test",
            "import KeyedMap",
            "class Data",
            "    int value = 3",
            "init",
            "    let m = keyedMapCreate()",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    keyedMapPut(m, u, 5)",
            "    keyedMapPut(m, 9, new Data())",
            "    if keyedMapHas(m, u) and keyedMapGetInt(m, u) == 5",
            "        print(\"present\")",
            "    Data d = keyedMapGet<int, Data>(m, 9)",
            "    print(d.value.toString())",
            "endpackage");

        String compiled = compiled("keyedMapGenericKeyAndValueReachLuaUncast");
        assertTrue("generic operations lower to the keyed-map stubs",
            compiled.contains("__wurst_keyedMapPut") && compiled.contains("__wurst_keyedMapGetInt"));
        String init = getFunctionBody(compiled, "init_Test");
        assertFalse("the unit must be handed over as itself: " + init,
            init.contains("__wurst_objectToIndex") || init.contains("__wurst_classFromIndex")
                || init.contains("__wurst_classToIndex"));
    }
}
