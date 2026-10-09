package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.expectThrows;
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

    @Test
    public void malformedStringPutFallbackReportsStringSignature() {
        test().expectError("keyedMapPutString must keep the existing int-map/string-key signature.").lines(
            "package Test",
            "@compilerintrinsic function keyedMapPutString(int map, handle key, int value)",
            "    skip",
            "@compilerintrinsic function keyedMapPutNative<K:, V:>(int map, K key, V value)",
            "    skip",
            "init",
            "    keyedMapPutNative<string, int>(0, \"key\", 1)",
            "endpackage");
    }

    @Test
    public void malformedStringGetFallbackReportsStringSignature() {
        test().expectError("keyedMapGetStringInt must keep the existing int-map/string-key signature.").lines(
            "package Test",
            "@compilerintrinsic function keyedMapGetStringInt(int map, handle key) returns int",
            "    return 0",
            "@compilerintrinsic function keyedMapGetNative<K:, V:>(int map, K key) returns V",
            "    return null",
            "init",
            "    let value = keyedMapGetNative<string, int>(0, \"key\")",
            "endpackage");
    }

    @Test
    public void stringKeysInFastKeyedMap() throws IOException {
        test().withStdLib().testLua(true).luaOnly(false).inline().executeProg().lines(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = new FastKeyedMap<string, int>()",
            "    let other = new FastKeyedMap<string, int>()",
            "    m.put(\"alpha\", 42)",
            "    m.put(\"ALPHA\", 7)",
            "    m.put(\"\", 0)",
            "    m.put(\"alpha\", 43)",
            "    if m.get(\"alpha\") != 43 or m.get(\"ALPHA\") != 7 or not m.has(\"\")",
            "        testFail(\"string keys or stored zero\")",
            "    if other.has(\"alpha\") or m.has(\"missing\") or m.get(\"missing\") != 0",
            "        testFail(\"absence or isolation\")",
            "    m.remove(\"alpha\")",
            "    m.remove(\"missing\")",
            "    if m.has(\"alpha\") or m.get(\"alpha\") != 0 or m.get(\"ALPHA\") != 7",
            "        testFail(\"remove\")",
            "    destroy m",
            "    let reused = new FastKeyedMap<string, int>()",
            "    if reused.has(\"ALPHA\") or reused.has(\"\")",
            "        testFail(\"destroy and reuse\")",
            "    destroy reused",
            "    destroy other",
            "    testSuccess()",
            "endpackage");
        String lua = getFunctionBody(compiled("stringKeysInFastKeyedMap"), "init_Test");
        assertFalse(lua.contains("StringHash("));
        assertFalse(lua.contains("GetHandleId("));
        assertFalse(lua.contains("keyedMapPutString("));
        assertTrue(lua, lua.contains("[\"alpha\"]"));
    }

    @Test
    public void stringKeysWithClassValues() {
        test().withStdLib().testLua(true).luaOnly(false).executeProg().lines(
            "package Test",
            "import KeyedMap",
            "class Data",
            "    int value = 17",
            "init",
            "    let m = new FastKeyedMap<string, Data>()",
            "    let data = new Data()",
            "    m.put(\"data\", data)",
            "    if m.get(\"data\") != data or m.get(\"data\").value != 17 or m.get(\"absent\") != null",
            "        testFail(\"class values\")",
            "    m.remove(\"data\")",
            "    if m.has(\"data\") or m.get(\"data\") != null",
            "        testFail(\"class remove\")",
            "    destroy data",
            "    destroy m",
            "    testSuccess()",
            "endpackage");
    }

    @Test
    public void stdlibKeyedMapTests() {
        test().withStdLib().executeTests().lines(
            "package Test",
            "import KeyedMapTests",
            "endpackage");
    }

    @Test
    public void stringMapEmissionIsDeterministic() throws IOException {
        String[] source = {
            "package Test", "import KeyedMap", "init",
            "    let m = new FastKeyedMap<string, int>()",
            "    m.put(\"alpha\", 0)", "    m.remove(\"alpha\")", "    destroy m", "endpackage"
        };
        File jassFile = new File("test-output/LuaKeyedMapTests_stringMapEmissionIsDeterministic_no_opts.j");
        test().withStdLib().testLua(true).luaOnly(false).lines(source);
        String firstLua = compiled("stringMapEmissionIsDeterministic");
        String firstJass = Files.toString(jassFile, Charsets.UTF_8);
        test().withStdLib().testLua(true).luaOnly(false).lines(source);
        org.testng.Assert.assertEquals(compiled("stringMapEmissionIsDeterministic"), firstLua);
        org.testng.Assert.assertEquals(Files.toString(jassFile, Charsets.UTF_8), firstJass);
    }

    private String getFunctionBody(String output, String functionName) {
        // Up to the closing 'end' at column 0; nested blocks are indented.
        Pattern pattern = Pattern.compile("function\\s*" + functionName + "\\s*\\([^\\n]*\\n(.*?)\\nend", Pattern.DOTALL);
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
        assertTrue("remove is one store", getFunctionBody(compiled, "__wurst_keyedMapRemove").contains("t[k] = nil"));

        String init = getFunctionBody(compiled, "init_Test");
        // The reads are printed as the index they stand for, so there is no stub to call or define.
        assertTrue("an int read is one index that answers 0 for a missing key: " + init,
            init.contains("] or 0)"));
        assertTrue("has is one index: " + init, init.contains("] ~= nil)"));
        assertFalse("no read stub is called: " + init,
            init.contains("__wurst_keyedMapGetInt") || init.contains("__wurst_keyedMapHas"));
        assertFalse("no read stub is defined", compiled.contains("function __wurst_keyedMapGetInt")
            || compiled.contains("function __wurst_keyedMapHas"));
        assertFalse("the caller must not reach the hashtable natives: " + init,
            init.contains("SaveInteger") || init.contains("LoadInteger") || init.contains("HaveSavedInteger"));
        assertFalse("an int read needs no nil normalisation: " + init,
            init.contains("__wurst_ensureInt") || init.contains("__wurst_rawTo"));
    }

    @Test
    public void keyedMapAgreesOnBothBackends() {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(keyedMapSource(
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
        assertTrue("the caller must read the table itself and call the put stub directly: " + init,
            init.contains("] or 0)") && init.contains("__wurst_keyedMapPut"));
        assertFalse("the caller must not reach the hashtable natives: " + init,
            init.contains("SaveInteger") || init.contains("LoadInteger"));
    }

    /**
     * A typed read is printed as the index it stands for, where it is called: the stubs are natives
     * with Lua text bodies, which the inliner cannot expand. A read whose result nothing uses is
     * dropped, which a call to a native the optimiser knows nothing about never was.
     */
    @Test
    public void keyedMapLookupIsExpandedAndAnUnusedReadIsDropped() throws IOException {
        test().testLua(true).inline().localOptimizations().withStdLib().lines(keyedMapSource(
            "package Test",
            "import KeyedMap",
            "@noinline function lookup(int m, int k) returns int",
            "    return keyedMapGetInt(m, k)",
            "@noinline function discard(int m, int k)",
            "    let ignoredValue = keyedMapGetInt(m, k)",
            "    let ignoredPresence = keyedMapHas(m, k)",
            "init",
            "    let m = keyedMapCreate()",
            "    keyedMapPut(m, 7, 70)",
            "    discard(m, 7)",
            "    print(lookup(m, 7).toString())",
            "endpackage"));

        String compiled = compiled("keyedMapLookupIsExpandedAndAnUnusedReadIsDropped");
        String lookup = getFunctionBody(compiled, "lookup");
        assertTrue("the lookup is the table index with the int default: " + lookup, lookup.contains("] or 0)"));
        int discardStart = compiled.indexOf("function discard(");
        assertTrue("expected function discard", discardStart >= 0);
        String discard = compiled.substring(discardStart, compiled.indexOf("\nend", discardStart));
        assertFalse("the unused reads are dropped: " + discard,
            discard.contains("[") || discard.contains("wurstExpr") || discard.contains("__wurst_keyedMap"));
        assertFalse("no read stub is called or defined anywhere:\n" + compiled,
            compiled.contains("__wurst_keyedMapGetInt") || compiled.contains("__wurst_keyedMapHas"));
    }

    @Test
    public void keyedMapDestroyClearsLuaStore() throws IOException {
        test().testLua(true).stacktraces().withStdLib().lines(keyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    keyedMapPut(m, 7, 70)",
            "    keyedMapDestroy(m)",
            "endpackage"));

        String compiled = compiled("keyedMapDestroyClearsLuaStore");
        assertTrue("destroy must clear the native Lua table", compiled.contains("__wurst_keyedMapDestroy"));
        String init = getFunctionBody(compiled, "init_Test");
        assertTrue("the map destruction call should reach its clear stub: " + init,
            init.contains("__wurst_keyedMapDestroy"));
        String destroy = getFunctionBody(compiled, "__wurst_keyedMapDestroy");
        assertTrue("the clear stub must empty the existing table in place: " + destroy,
            destroy.contains("for k in pairs(t) do t[k] = nil end"));
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
        assertTrue("generic operations lower to the keyed-map put stub and the table read",
            compiled.contains("__wurst_keyedMapPut") && compiled.contains("] or 0)"));
        String init = getFunctionBody(compiled, "init_Test");
        assertFalse("the unit must be handed over as itself: " + init,
            init.contains("__wurst_objectToIndex") || init.contains("__wurst_classFromIndex")
                || init.contains("__wurst_classToIndex"));
    }

    private static String[] handleKeyedMapSource(String... usage) {
        java.util.List<String> lines = new java.util.ArrayList<>(java.util.Arrays.asList(
            "package KeyedMap",
            "import Table",
            "@compilerintrinsic public function keyedMapPut(int tbl, handle key, int value)",
            "    (tbl castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapGetInt(int tbl, handle key) returns int",
            "    return (tbl castTo Table).loadInt(GetHandleId(key))",
            "public class KeyedMap<K: handle>",
            "    function put(K key, int value)",
            "        keyedMapPut(0, key, value)",
            "    function get(K key) returns int",
            "        return keyedMapGetInt(0, key)",
            "public function forwardPut<K: handle>(KeyedMap<K> map, K key, int value)",
            "    map.put(key, value)",
            "endpackage"));
        lines.addAll(java.util.Arrays.asList(usage));
        return lines.toArray(new String[0]);
    }

    @Test
    public void handleBoundWrapperSpecializesToOneLuaTableAccess() throws IOException {
        test().testLua(true).inline().withStdLib().lines(handleKeyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let map = new KeyedMap<unit>()",
            "    let timerMap = new KeyedMap<timer>()",
            "    let playerMap = new KeyedMap<player>()",
            "    let itemMap = new KeyedMap<item>()",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    forwardPut(map, u, 5)",
            "    let value = map.get(u)",
            "    print(value.toString())",
            "endpackage"));

        // The small specialized wrappers are expanded into the caller, which is all that is left to read.
        String lua = compiled("handleBoundWrapperSpecializesToOneLuaTableAccess");
        String init = getFunctionBody(lua, "init_Test");
        assertTrue("the caller calls the unchanged put intrinsic: " + init, init.contains("__wurst_keyedMapPut"));
        assertTrue("and reads the table with the int default: " + init,
            init.contains("] or 0)") && !init.contains("__wurst_keyedMapGetInt"));
        assertFalse("the native unit is used as the key without handle-id conversion: " + init,
            init.contains("GetHandleId") || init.contains("__wurst_objectToIndex")
                || init.contains("__wurst_classToIndex"));
        assertTrue("put is one direct Lua table store",
            getFunctionBody(lua, "__wurst_keyedMapPut").contains("t[k] = v"));
        assertTrue("get is one direct Lua table read", init.indexOf("] or 0)") == init.lastIndexOf("] or 0)"));
    }

    @Test
    public void handleBoundWrapperKeepsJassHashtableFallback() throws IOException {
        ErrorHandler.outputTestSource = true;
        try {
            test().withStdLib().lines(handleKeyedMapSource(
                "package Test",
                "import KeyedMap",
                "init",
                "    let map = new KeyedMap<unit>()",
                "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
                "    map.put(u, 5)",
                "    let value = map.get(u)",
                "endpackage"));

            String jass = Files.toString(new File("test-output/LuaKeyedMapTests_handleBoundWrapperKeepsJassHashtableFallback_no_opts.j"), Charsets.UTF_8);
            assertTrue("the Jass fallback still uses handle ids", jass.contains("GetHandleId"));
            assertTrue("the Jass fallback still uses hashtable storage", jass.contains("SaveInteger")
                && jass.contains("LoadInteger"));
        } finally {
            ErrorHandler.outputTestSource = false;
        }
    }

    @Test
    public void handleBoundRejectsIntegerKeys() {
        testAssertErrorsLines(true, "Only handle types can be used here",
            "package KeyedMap",
            "public class KeyedMap<K: handle>",
            "endpackage",
            "package Test",
            "import KeyedMap",
            "class Data",
            "init",
            "    let map = new KeyedMap<int>()",
            "    let classMap = new KeyedMap<Data>()",
            "endpackage");
    }

    @Test
    public void handleBoundDoesNotInferNullAsATypeArgument() {
        test().expectError("Cannot infer a handle type argument from null").withStdLib().lines(
            "package Test",
            "function identity<T: handle>(T value) returns T",
            "    return value",
            "init",
            "    identity(null)",
            "endpackage");
    }

    @Test
    public void handleBoundAllowsNullForExplicitHandleType() {
        test().withStdLib().lines(
            "package Test",
            "function identity<T: handle>(T value) returns T",
            "    return value",
            "init",
            "    let value = identity<timer>(null)",
            "endpackage");
    }

    @Test
    public void handleBoundGenericValuesKeepTheirNativeLuaRepresentation() throws IOException {
        test().testLua(true).inline().withStdLib().lines(
            "package KeyedMap",
            "import Table",
            "import ErrorHandling",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int tbl, handle key, int value)",
            "    skip",
            "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int tbl, K key, V value)",
            "    if key == null",
            "        return",
            "    error(\"keyedMapPutNative requires compiler keyed-map intrinsic support\")",
            "@compilerintrinsic public function keyedMapGetNative<K: handle, V:>(int tbl, K key) returns V",
            "    error(\"keyedMapGetNative requires compiler keyed-map intrinsic support\")",
            "    return null",
            "public class KeyedMap<K: handle, V:>",
            "    private int map",
            "    construct()",
            "        map = keyedMapCreate()",
            "    function put(K key, V value)",
            "        keyedMapPutNative<K, V>(map, key, value)",
            "    function get(K key) returns V",
            "        return keyedMapGetNative<K, V>(map, key)",
            "endpackage",
            "package Test",
            "import KeyedMap",
            "class Data",
            "    int field",
            "init",
            "    let intMap = new KeyedMap<unit, int>()",
            "    let classMap = new KeyedMap<unit, Data>()",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    keyedMapPut(0, u, 7)",
            "    intMap.put(u, 5)",
            "    let number = intMap.get(u)",
            "    classMap.put(u, new Data())",
            "    let data = classMap.get(u)",
            "    data.field = number",
            "    print(data.field.toString())",
            "endpackage");

        // The small specialized wrappers are expanded into the caller, which is all that is left to read.
        String lua = compiled("handleBoundGenericValuesKeepTheirNativeLuaRepresentation");
        String init = getFunctionBody(lua, "init_Test");
        assertTrue("the caller calls the generic put intrinsic: " + init, init.contains("__wurst_keyedMapPut"));
        assertTrue("and reads the table directly: " + init,
            init.contains("] or 0)") && !init.contains("__wurst_keyedMapGet"));
        assertFalse("generic values and handle keys need no index conversions: " + init,
            init.contains("GetHandleId") || init.contains("__wurst_objectToIndex")
                || init.contains("__wurst_classToIndex") || init.contains("__wurst_classFromIndex"));
        String putStub = getFunctionBody(lua, "__wurst_keyedMapPut");
        assertTrue("generic put has exactly one direct table store: " + putStub,
            putStub.contains("t[k] = v") && putStub.indexOf("t[k]") == putStub.lastIndexOf("t[k]"));
        assertFalse("generic get is a table index, with no stub to define", lua.contains("function __wurst_keyedMapGet("));
    }

    @Test
    public void nativeIntegerGetterThroughGenericWrapperUsesRawLuaIndex() throws IOException {
        test().testLua(true).withStdLib().lines(
            "package KeyedMap",
            "import ErrorHandling",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return 0",
            "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int tbl, K key, V value)",
            "    error(\"keyedMapPutNative requires compiler keyed-map intrinsic support\")",
            "@compilerintrinsic public function keyedMapGetNative<K: handle, V:>(int tbl, K key) returns V",
            "    error(\"keyedMapGetNative requires compiler keyed-map intrinsic support\")",
            "    return null",
            "public function readNative<K: handle, V:>(int tbl, K key) returns V",
            "    return keyedMapGetNative<K, V>(tbl, key)",
            "endpackage",
            "package Test",
            "import KeyedMap",
            "init",
            "    let map = keyedMapCreate()",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    keyedMapPutNative<unit, int>(map, u, 42)",
            "    let value = readNative<unit, int>(map, u)",
            "    print(value.toString())",
            "endpackage");

        String lua = compiled("nativeIntegerGetterThroughGenericWrapperUsesRawLuaIndex");
        String init = getFunctionBody(lua, "init_Test");
        // Used as an int, the wrapper's read is the typed read (LuaTypedKeyedReads): a typed copy of
        // the wrapper reads the table with the int default, so no ensure is left around the call.
        assertTrue("the generic wrapper is read through its typed copy: " + init,
            init.contains("readNative_int(map, u)") && !init.contains("__wurst_ensureInt"));
        String typedCopy = getFunctionBody(lua, "readNative_int");
        assertTrue("the typed copy returns one direct table read with the int default: " + typedCopy,
            typedCopy.contains("] or 0)") && typedCopy.indexOf("] or 0)") == typedCopy.lastIndexOf("] or 0)"));
        assertFalse("integer get must not leave the failing source fallback body",
            lua.contains("function keyedMapGetNative_unit_int") && lua.contains("return nil"));
        assertFalse("the typed get has no stub to define", lua.contains("function __wurst_keyedMapGetInt"));
    }

    /**
     * The interpreter meets the generic value intrinsics before JassKeyedMapLowering gives them their
     * bodies, so their placeholder bodies must not run: compiletime code and -runTests would raise
     * their error. It stores values the way the lowering compiles them, through the int fallback.
     */
    @Test
    public void genericValuesRunInTheInterpreter() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package KeyedMap",
            "import Table",
            "import ErrorHandling",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int map, handle key, int value)",
            "    if key == null",
            "        return",
            "    (map castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapGetInt(int map, handle key) returns int",
            "    return (map castTo Table).loadInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int map, K key, V value)",
            "    if key == null",
            "        return",
            "    error(\"keyedMapPutNative requires compiler keyed-map intrinsic support\")",
            "@compilerintrinsic public function keyedMapGetNative<K: handle, V:>(int map, K key) returns V",
            "    error(\"keyedMapGetNative requires compiler keyed-map intrinsic support\")",
            "    return null",
            "@compilerintrinsic public function keyedMapHas(int map, handle key) returns boolean",
            "    return (map castTo Table).hasInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapRemove(int map, handle key)",
            "    if key == null",
            "        return",
            "    (map castTo Table).removeInt(GetHandleId(key))",
            "public class FastKeyedMap<K: handle, V:>",
            "    private int map",
            "    construct()",
            "        map = keyedMapCreate()",
            "    function put(K key, V value)",
            "        keyedMapPutNative<K, V>(map, key, value)",
            "    function get(K key) returns V",
            "        return keyedMapGetNative<K, V>(map, key)",
            "    function has(K key) returns boolean",
            "        return keyedMapHas(map, key)",
            "    function remove(K key)",
            "        keyedMapRemove(map, key)",
            "endpackage",
            "package Test",
            "import KeyedMap",
            "class Data",
            "    int value",
            "    construct(int value)",
            "        this.value = value",
            "init",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    let v = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    let ints = new FastKeyedMap<unit, int>()",
            "    ints.put(u, 42)",
            "    ints.put(v, 7)",
            "    ints.put(u, 43)",
            "    if ints.get(u) != 43 or ints.get(v) != 7",
            "        testFail(\"int values\")",
            "    ints.remove(u)",
            "    if ints.has(u) or ints.get(u) != 0 or not ints.has(v)",
            "        testFail(\"int remove\")",
            "    let datas = new FastKeyedMap<unit, Data>()",
            "    let data = new Data(17)",
            "    datas.put(u, data)",
            "    if datas.get(u) != data or datas.get(u).value != 17",
            "        testFail(\"class values\")",
            "    if datas.get(v) != null",
            "        testFail(\"absent class value\")",
            "    let raw = keyedMapCreate()",
            "    keyedMapPutNative<unit, int>(raw, v, 8)",
            "    if keyedMapGetNative<unit, int>(raw, v) != 8",
            "        testFail(\"direct call\")",
            "    testSuccess()",
            "endpackage");
    }

    /**
     * A compile-time expression runs the generic value intrinsics through their int fallback as well, which the
     * interpreter finds by name. Nothing calls the fallback at run time, so the tree shake in front of the
     * compile-time run must keep it on Lua too, where the backend does not need it.
     */
    @Test
    public void genericValuesRunInACompiletimeExpression() {
        test().withStdLib().testLua(true).luaOnly(false).executeProg(true).lines(
            "package KeyedMap",
            "import Table",
            "import ErrorHandling",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int map, handle key, int value)",
            "    if key == null",
            "        return",
            "    (map castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapGetInt(int map, handle key) returns int",
            "    return (map castTo Table).loadInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int map, K key, V value)",
            "    if key == null",
            "        return",
            "    error(\"keyedMapPutNative requires compiler keyed-map intrinsic support\")",
            "@compilerintrinsic public function keyedMapGetNative<K: handle, V:>(int map, K key) returns V",
            "    error(\"keyedMapGetNative requires compiler keyed-map intrinsic support\")",
            "    return null",
            "endpackage",
            "package Test",
            "import KeyedMap",
            "function roundTrip() returns int",
            "    let m = keyedMapCreate()",
            "    let p = Player(0)",
            "    keyedMapPutNative<player, int>(m, p, 42)",
            "    return keyedMapGetNative<player, int>(m, p)",
            "init",
            "    if compiletime(roundTrip()) == 42",
            "        testSuccess()",
            "endpackage");
    }

    /** A malformed intrinsic gets the lowering's diagnostic in the interpreter, not a crash on its arguments. */
    @Test
    public void malformedGenericPutIsReportedWhenInterpreted() {
        Error failure = expectThrows(Error.class, () -> test().withStdLib().executeTests().lines(
            "package KeyedMap",
            "import Table",
            "@compilerintrinsic public function keyedMapPut(int map, handle key, int value)",
            "    (map castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int map, K key)",
            "    skip",
            "@Test function putWithoutAValue()",
            "    keyedMapPutNative<unit, int>((new Table()) castTo int, CreateUnit(Player(0), 'hfoo', 0., 0., 0.))",
            "endpackage"));
        assertTrue(failure.getMessage(), failure.getMessage()
            .contains("keyedMapPutNative requires an int map, a handle or string key, and an int-represented value"));
        assertFalse(failure.getMessage(), failure.getMessage().contains("ArrayIndexOutOfBounds"));
    }

    /** A handle-valued specialization is rejected in the interpreter too, even with a null value. */
    @Test
    public void handleValuedGenericPutIsReportedWhenInterpreted() {
        Error failure = expectThrows(Error.class, () -> test().withStdLib().executeTests().lines(
            "package KeyedMap",
            "import Table",
            "@compilerintrinsic public function keyedMapPut(int map, handle key, int value)",
            "    (map castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int map, K key, V value)",
            "    skip",
            "@Test function putANullUnit()",
            "    keyedMapPutNative<unit, unit>((new Table()) castTo int, CreateUnit(Player(0), 'hfoo', 0., 0., 0.), null)",
            "endpackage"));
        assertTrue(failure.getMessage(), failure.getMessage()
            .contains("keyedMapPutNative requires an int map, a handle or string key, and an int-represented value"));
    }

    /** An unbounded key type called with an int key is rejected before the fallback's handle parameter. */
    @Test
    public void intKeyedGenericGetIsReportedWhenInterpreted() {
        Error failure = expectThrows(Error.class, () -> test().withStdLib().executeTests().lines(
            "package KeyedMap",
            "import Table",
            "@compilerintrinsic public function keyedMapGetInt(int map, handle key) returns int",
            "    return (map castTo Table).loadInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapGetNative<K, V>(int map, K key) returns V",
            "    return null",
            "@Test function getByAnInt()",
            "    let value = keyedMapGetNative<int, int>((new Table()) castTo int, 7)",
            "endpackage"));
        assertTrue(failure.getMessage(), failure.getMessage()
            .contains("keyedMapGetNative requires an int map, a handle or string key, and an int-represented result"));
    }

    /** The generic value intrinsics and a typed wrapper over them, as the standard library declares them. */
    private static String[] fastKeyedMapSource(String... usage) {
        java.util.List<String> lines = new java.util.ArrayList<>(java.util.Arrays.asList(
            "package KeyedMap",
            "import Table",
            "import ErrorHandling",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int map, handle key, int value)",
            "    if key == null",
            "        return",
            "    (map castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapGetInt(int map, handle key) returns int",
            "    return (map castTo Table).loadInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int map, K key, V value)",
            "    if key == null",
            "        return",
            "    error(\"keyedMapPutNative requires compiler keyed-map intrinsic support\")",
            "@compilerintrinsic public function keyedMapGetNative<K: handle, V:>(int map, K key) returns V",
            "    error(\"keyedMapGetNative requires compiler keyed-map intrinsic support\")",
            "    return null",
            "public class FastKeyedMap<K: handle, V:>",
            "    private int map",
            "    construct()",
            "        map = keyedMapCreate()",
            "    function put(K key, V value)",
            "        keyedMapPutNative<K, V>(map, key, value)",
            "    function get(K key) returns V",
            "        return keyedMapGetNative<K, V>(map, key)",
            "endpackage"));
        lines.addAll(java.util.Arrays.asList(usage));
        return lines.toArray(new String[0]);
    }

    /**
     * An erased read used as a primitive is the typed read, not the untyped read plus an ensure: the
     * hot int loop calls __wurst_keyedMapGetInt with no tonumber or math.tointeger, and every type
     * still reads back its values and its default for a missing key.
     */
    @Test
    public void fastKeyedMapReadsUsedAsPrimitivesAreTyped() throws IOException {
        test().testLua(true).inline().executeProg(true).withStdLib().lines(fastKeyedMapSource(
            "package Test",
            "import KeyedMap",
            "timer array keys",
            "int total = 0",
            "function readAll(FastKeyedMap<timer, int> m)",
            "    for i = 0 to 2",
            "        total += m.get(keys[i])",
            "init",
            "    for i = 0 to 2",
            "        keys[i] = CreateTimer()",
            "    let ints = new FastKeyedMap<timer, int>()",
            "    ints.put(keys[0], 40)",
            "    ints.put(keys[1], 2)",
            "    readAll(ints)",
            "    let reals = new FastKeyedMap<timer, real>()",
            "    reals.put(keys[0], 1.5)",
            "    let bools = new FastKeyedMap<timer, boolean>()",
            "    bools.put(keys[0], true)",
            "    bools.put(keys[1], false)",
            "    let strings = new FastKeyedMap<timer, string>()",
            "    strings.put(keys[0], \"a\")",
            "    if total != 42",
            "        testFail(\"ints \" + I2S(total))",
            "    if reals.get(keys[0]) != 1.5 or reals.get(keys[1]) != 0.",
            "        testFail(\"reals\")",
            "    if not bools.get(keys[0]) or bools.get(keys[1]) or bools.get(keys[2])",
            "        testFail(\"bools\")",
            "    if strings.get(keys[0]) != \"a\" or strings.get(keys[1]) != \"\"",
            "        testFail(\"strings\")",
            "    testSuccess()",
            "endpackage"));

        // readAll is inlined into init, so its loop is cut out of init by the global it updates.
        String init = getFunctionBody(compiled("fastKeyedMapReadsUsedAsPrimitivesAreTyped"), "init_Test");
        String[] lines = init.split("\n");
        int update = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("Test_total = (")) {
                update = i;
            }
        }
        assertTrue("the read loop is in init: " + init, update >= 0);
        int start = update;
        while (start > 0 && !lines[start].startsWith("\tfor ")) {
            start--;
        }
        int end = update;
        while (end < lines.length - 1 && !lines[end].equals("\tend")) {
            end++;
        }
        String loop = String.join("\n", java.util.Arrays.copyOfRange(lines, start, end + 1));
        assertTrue("the int read is the typed read: " + loop, loop.contains("] or 0)"));
        assertFalse("no ensure left on the int read: " + loop,
            loop.contains("tonumber") || loop.contains("math.tointeger") || loop.contains("__wurst_keyedMapGet("));
    }

    /**
     * Stack traces put a push, a pop and possibly a temporary around a wrapper's return; the wrapper
     * still returns its one untyped read, so its int read is still typed.
     */
    @Test
    public void fastKeyedMapIntReadsAreTypedWithStackTraces() throws IOException {
        test().testLua(true).stacktraces().inline().executeProg(true).withStdLib().lines(fastKeyedMapSource(
            "package Test",
            "import KeyedMap",
            "timer array keys",
            "int total = 0",
            "function readAll(FastKeyedMap<timer, int> m)",
            "    for i = 0 to 2",
            "        total += m.get(keys[i])",
            "init",
            "    for i = 0 to 2",
            "        keys[i] = CreateTimer()",
            "    let ints = new FastKeyedMap<timer, int>()",
            "    ints.put(keys[0], 40)",
            "    ints.put(keys[1], 2)",
            "    readAll(ints)",
            "    if total != 42",
            "        testFail(\"ints \" + I2S(total))",
            "    testSuccess()",
            "endpackage"));

        String lua = compiled("fastKeyedMapIntReadsAreTypedWithStackTraces");
        assertTrue("the typed read is emitted: " + lua.length(), lua.contains("] or 0)"));
        assertFalse("no int ensure is left anywhere a FastKeyedMap<timer, int> is read",
            lua.contains("__wurst_ensureInt(FastKeyedMap") || lua.contains("__wurst_ensureInt(readAll"));
        int stubs = lua.split("function __wurst_keyedMapGet\\(", -1).length - 1;
        assertTrue("one untyped get stub, not one per lowering pass (" + stubs + ")", stubs <= 1);
    }

    /** A read reached through a delegating index operator is typed too, once inlining exposes it. */
    @Test
    public void delegatedFastKeyedMapIntReadsAreTyped() throws IOException {
        test().testLua(true).inline().executeProg(true).withStdLib().lines(fastKeyedMapSource(
            "package Test",
            "import KeyedMap",
            "class Outer<K: handle, V:>",
            "    FastKeyedMap<K, V> inner = new FastKeyedMap<K, V>()",
            "    function op_index(K key) returns V",
            "        return inner.get(key)",
            "timer array keys",
            "int total = 0",
            "function readAll(Outer<timer, int> m)",
            "    for i = 0 to 2",
            "        total += m[keys[i]]",
            "init",
            "    for i = 0 to 2",
            "        keys[i] = CreateTimer()",
            "    let outer = new Outer<timer, int>()",
            "    outer.inner.put(keys[0], 40)",
            "    outer.inner.put(keys[1], 2)",
            "    readAll(outer)",
            "    if total != 42",
            "        testFail(\"ints \" + I2S(total))",
            "    testSuccess()",
            "endpackage"));

        String init = getFunctionBody(compiled("delegatedFastKeyedMapIntReadsAreTyped"), "init_Test");
        assertTrue("the delegated int read is the typed read: " + init, init.contains("] or 0)"));
        assertFalse("no ensure left on the delegated int read: " + init,
            init.contains("math.tointeger") || init.contains("__wurst_ensureInt("));
    }

    /**
     * A read outside any loop is typed too, like a library's lookup in a function that hot code calls:
     * the method call cannot dispatch anywhere else, so it becomes a direct call of the typed twin.
     */
    @Test
    public void fastKeyedMapReadsOutsideLoopsAreTyped() throws IOException {
        test().testLua(true).inline().executeProg(true).withStdLib().lines(fastKeyedMapSource(
            "package Test",
            "import KeyedMap",
            "let ids = new FastKeyedMap<timer, int>()",
            "let scales = new FastKeyedMap<timer, real>()",
            "@noinline function idOf(timer t) returns int",
            "    let id = ids.get(t)",
            "    if id == 0",
            "        return -1",
            "    return id",
            "@noinline function scaleOf(timer t) returns real",
            "    return scales.get(t) * 2.",
            "init",
            "    let a = CreateTimer()",
            "    let b = CreateTimer()",
            "    ids.put(a, 42)",
            "    scales.put(a, 1.5)",
            "    if idOf(a) != 42 or idOf(b) != -1",
            "        testFail(\"ids\")",
            "    if scaleOf(a) != 3. or scaleOf(b) != 0.",
            "        testFail(\"scales\")",
            "    testSuccess()",
            "endpackage"));

        String lua = compiled("fastKeyedMapReadsOutsideLoopsAreTyped");
        for (String function : new String[] {"idOf", "scaleOf"}) {
            String body = getFunctionBody(lua, function);
            assertFalse("no ensure left on the read in " + function + ": " + body,
                body.contains("tonumber") || body.contains("math.tointeger") || body.contains("__wurst_ensure"));
        }
    }

    @Test
    public void handleBoundGenericValuesKeepJassHashtableFallback() throws IOException {
        ErrorHandler.outputTestSource = true;
        try {
            test().withStdLib().lines(
            "package KeyedMap",
            "import Table",
            "import ErrorHandling",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int tbl, K key, V value)",
                "    if key == null",
                "        return",
                "    skip",
                "@compilerintrinsic public function keyedMapPut(int tbl, handle key, int value)",
                "    if key == null",
                "        return",
                "    error(\"keyedMapPutNative requires compiler keyed-map intrinsic support\")",
                "@compilerintrinsic public function keyedMapGetInt(int tbl, handle key) returns int",
                "    return (tbl castTo Table).loadInt(GetHandleId(key))",
                "@compilerintrinsic public function keyedMapGetNative<K: handle, V:>(int tbl, K key) returns V",
                "    error(\"keyedMapGetNative requires compiler keyed-map intrinsic support\")",
                "    return null",
                "public class KeyedMap<K: handle, V:>",
                "    private int map",
                "    construct()",
                "        map = keyedMapCreate()",
                "    function put(K key, V value)",
                "        keyedMapPutNative<K, V>(map, key, value)",
                "    function get(K key) returns V",
                "        return keyedMapGetNative<K, V>(map, key)",
                "endpackage",
                "package Test",
                "import KeyedMap",
                "class Data",
                "init",
                "    let intMap = new KeyedMap<unit, int>()",
                "    let classMap = new KeyedMap<unit, Data>()",
                "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
                "    intMap.put(u, 5)",
                "    let number = intMap.get(u)",
                "    classMap.put(u, new Data())",
                "    let data = classMap.get(u)",
                "    let keep = data",
                "endpackage");

            String jass = Files.toString(new File("test-output/LuaKeyedMapTests_handleBoundGenericValuesKeepJassHashtableFallback_no_opts.j"), Charsets.UTF_8);
            assertTrue("generic values still lower to the hashtable int representation", jass.contains("SaveInteger")
                && jass.contains("LoadInteger"));
            assertTrue("handle keys still use GetHandleId", jass.contains("GetHandleId"));
            assertTrue("class values route through the unchanged int fallback",
                jass.contains("function keyedMapPutNative_unit__Data_u")
                    && jass.contains("call keyedMapPut(tbl, key, value)"));
            assertTrue("class reads route through the unchanged int getter",
                jass.contains("function keyedMapGetNative_unit__Data_u")
                    && jass.contains("return keyedMapGetInt(tbl, key)"));
        } finally {
            ErrorHandler.outputTestSource = false;
        }
    }

    @Test
    public void jassRejectsValuesWithoutIntegerRepresentation() {
        test().expectError("keyedMapPutNative requires an int map, a handle or string key, and an int-represented value")
            .withStdLib().lines(
                "package KeyedMap",
                "import Table",
                "@compilerintrinsic public function keyedMapCreate() returns int",
                "    return (new Table()) castTo int",
                "@compilerintrinsic public function keyedMapPutNative<K: handle, V:>(int tbl, K key, V value)",
                "    skip",
                "@compilerintrinsic public function keyedMapPut(int tbl, handle key, int value)",
                "    (tbl castTo Table).saveInt(GetHandleId(key), value)",
                "@compilerintrinsic public function keyedMapGetNative<K: handle, V:>(int tbl, K key) returns V",
                "    return null",
                "@compilerintrinsic public function keyedMapGetInt(int tbl, handle key) returns int",
                "    return (tbl castTo Table).loadInt(GetHandleId(key))",
                "endpackage",
                "package Test",
                "import KeyedMap",
                "init",
                "    let map = keyedMapCreate()",
                "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
                "    keyedMapPutNative<unit, real>(map, u, 1.)",
                "endpackage");
    }

    /**
     * The shape the library ships for UnitIndexer: a concrete unit key with a Table + GetHandleId
     * body. That body is correct on any compiler, so nothing degrades silently; a compiler with
     * the lowering keys the native table by the unit itself.
     */
    private static String[] unitKeyedMapSource(String... usage) {
        java.util.List<String> lines = new java.util.ArrayList<>(java.util.Arrays.asList(
            "package KeyedMap",
            "import Table",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int tbl, unit key, int value)",
            "    (tbl castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapGetInt(int tbl, unit key) returns int",
            "    return (tbl castTo Table).loadInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapHas(int tbl, unit key) returns boolean",
            "    return (tbl castTo Table).hasInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapRemove(int tbl, unit key)",
            "    (tbl castTo Table).removeInt(GetHandleId(key))",
            "endpackage"));
        lines.addAll(java.util.Arrays.asList(usage));
        return lines.toArray(new String[0]);
    }

    @Test
    public void unitKeyedMapKeysTheUnitItselfOnLua() throws IOException {
        test().testLua(true).inline().withStdLib().lines(unitKeyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    keyedMapPut(m, u, 5)",
            "    if keyedMapHas(m, u) and keyedMapGetInt(m, u) == 5",
            "        print(\"present\")",
            "    keyedMapRemove(m, u)",
            "endpackage"));

        String compiled = compiled("unitKeyedMapKeysTheUnitItselfOnLua");
        String init = getFunctionBody(compiled, "init_Test");
        assertTrue("writes lower to the keyed-map stubs and reads to the table index: " + init,
            init.contains("__wurst_keyedMapPut") && init.contains("] or 0)")
                && init.contains("] ~= nil)") && init.contains("__wurst_keyedMapRemove"));
        assertFalse("the unit must not go through a handle id or an index map: " + init,
            init.contains("GetHandleId") || init.contains("__wurst_objectToIndex") || init.contains("SaveInteger"));
    }

    /**
     * Runtime parity for a handle key, on the interpreter through the Table body and on Lua
     * through the stubs. Timers, because the Lua test runtime creates those; the lowering is the
     * same for every handle type. A null key stores nothing and reads as absent on both.
     */
    @Test
    public void handleKeyedMapAgreesOnBothBackends() {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(
            "package KeyedMap",
            "import Table",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int tbl, timer key, int value)",
            "    (tbl castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapGetInt(int tbl, timer key) returns int",
            "    return (tbl castTo Table).loadInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapHas(int tbl, timer key) returns boolean",
            "    return (tbl castTo Table).hasInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapRemove(int tbl, timer key)",
            "    (tbl castTo Table).removeInt(GetHandleId(key))",
            "endpackage",
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    let a = CreateTimer()",
            "    let b = CreateTimer()",
            "    if keyedMapHas(m, a) or keyedMapGetInt(m, a) != 0",
            "        testFail(\"empty map reported a\")",
            "    keyedMapPut(m, a, 1)",
            "    keyedMapPut(m, b, 2)",
            "    if keyedMapGetInt(m, a) != 1 or keyedMapGetInt(m, b) != 2 or not keyedMapHas(m, b)",
            "        testFail(\"handles did not get distinct entries\")",
            "    keyedMapRemove(m, a)",
            "    if keyedMapHas(m, a) or keyedMapGetInt(m, a) != 0 or keyedMapGetInt(m, b) != 2",
            "        testFail(\"remove did not clear only its handle\")",
            "    timer none = null",
            "    keyedMapRemove(m, none)",
            "    if keyedMapHas(m, none) or keyedMapGetInt(m, none) != 0",
            "        testFail(\"a null key must read as absent\")",
            "    testSuccess()",
            "endpackage");
    }

    @Test
    public void keyedMapDestroyClearsLuaStoreWhileAliasRemains() {
        test().testLua(true).luaOnly(true).executeProg().withStdLib().lines(
            "package KeyedMap",
            "import Table",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int tbl, timer key, int value)",
            "    (tbl castTo Table).saveInt(GetHandleId(key), value)",
            "@compilerintrinsic public function keyedMapHas(int tbl, timer key) returns boolean",
            "    return (tbl castTo Table).hasInt(GetHandleId(key))",
            "@compilerintrinsic public function keyedMapDestroy(int tbl)",
            "    destroy (tbl castTo Table)",
            "endpackage",
            "package Test",
            "import KeyedMap",
            "init",
            "    let map = keyedMapCreate()",
            "    let alias = map",
            "    let key = CreateTimer()",
            "    keyedMapPut(map, key, 42)",
            "    keyedMapDestroy(map)",
            "    if keyedMapHas(alias, key)",
            "        testFail(\"destroy must clear entries through retained aliases\")",
            "    testSuccess()",
            "endpackage");
    }

    /**
     * A vararg declaration and a static function of a class keep their own bodies: they are not the
     * library's package functions, whatever their parameter types look like.
     */
    @Test
    public void varargAndClassFunctionsNamedLikeMapOperationsAreNotLowered() throws IOException {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(
            "package Test",
            "int total = 0",
            "@compilerintrinsic function keyedMapDestroy(vararg int maps)",
            "    for m in maps",
            "        total += m",
            "class Holder",
            "    @compilerintrinsic static function keyedMapCreate() returns int",
            "        total += 1000",
            "        return 7",
            "    @compilerintrinsic static function keyedMapHas(int map, int key) returns boolean",
            "        total += 10000",
            "        return true",
            "init",
            "    keyedMapDestroy(1, 2, 3)",
            "    keyedMapDestroy(4)",
            "    if Holder.keyedMapCreate() == 7 and Holder.keyedMapHas(0, 1) and total == 11010",
            "        testSuccess()",
            "endpackage");

        String compiled = compiled("varargAndClassFunctionsNamedLikeMapOperationsAreNotLowered");
        assertFalse("none of them is replaced by a stub", compiled.contains("__wurst_keyedMap"));
    }

    /**
     * An int key and a class-typed value: what a library keeping one object per ability id declares.
     * The Jass bodies go through the integer object id, so the source is correct on any compiler.
     */
    private static String[] classValuedIntKeyedMapSource(String... usage) {
        java.util.List<String> lines = new java.util.ArrayList<>(java.util.Arrays.asList(
            "package KeyedMap",
            "import Table",
            "public class Listeners",
            "    int id",
            "    construct(int id)",
            "        this.id = id",
            "@compilerintrinsic public function keyedMapCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedMapPut(int tbl, int key, Listeners value)",
            "    (tbl castTo Table).saveInt(key, value castTo int)",
            "@compilerintrinsic public function keyedMapGet(int tbl, int key) returns Listeners",
            "    return (tbl castTo Table).loadInt(key) castTo Listeners",
            "@compilerintrinsic public function keyedMapHas(int tbl, int key) returns boolean",
            "    return (tbl castTo Table).hasInt(key)",
            "@compilerintrinsic public function keyedMapRemove(int tbl, int key)",
            "    (tbl castTo Table).removeInt(key)",
            "endpackage"));
        lines.addAll(java.util.Arrays.asList(usage));
        return lines.toArray(new String[0]);
    }

    @Test
    public void intKeyedClassMapLowersToSingleLuaIndexes() throws IOException {
        test().testLua(true).inline().withStdLib().lines(classValuedIntKeyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    keyedMapPut(m, 'A000', new Listeners(3))",
            "    if keyedMapHas(m, 'A000')",
            "        print(keyedMapGet(m, 'A000').id.toString())",
            "    keyedMapRemove(m, 'A000')",
            "endpackage"));

        String compiled = compiled("intKeyedClassMapLowersToSingleLuaIndexes");
        String init = getFunctionBody(compiled, "init_Test");
        assertTrue("writes lower to the keyed-map stubs and reads to the table index: " + init,
            init.contains("__wurst_keyedMapPut") && init.contains("] ~= nil)")
                && !init.contains("__wurst_keyedMapGet") && init.contains("__wurst_keyedMapRemove"));
        assertFalse("the object must be stored as itself: " + init,
            init.contains("__wurst_objectToIndex") || init.contains("__wurst_classToIndex")
                || init.contains("__wurst_classFromIndex"));
    }

    /** Runtime parity: the interpreter runs the Table bodies, Lua runs the stubs. */
    @Test
    public void intKeyedClassMapAgreesOnBothBackends() {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(classValuedIntKeyedMapSource(
            "package Test",
            "import KeyedMap",
            "init",
            "    let m = keyedMapCreate()",
            "    let a = new Listeners(1)",
            "    let b = new Listeners(2)",
            "    if keyedMapHas(m, 'A000') or keyedMapGet(m, 'A000') != null",
            "        testFail(\"empty map reported an entry\")",
            "    keyedMapPut(m, 'A000', a)",
            "    keyedMapPut(m, 'A001', b)",
            "    if keyedMapGet(m, 'A000') != a or keyedMapGet(m, 'A001') != b or not keyedMapHas(m, 'A001')",
            "        testFail(\"keys did not get distinct entries\")",
            "    if keyedMapGet(m, 'A000').id != 1",
            "        testFail(\"the stored object is not the one that was put\")",
            "    keyedMapPut(m, 'A000', b)",
            "    if keyedMapGet(m, 'A000') != b",
            "        testFail(\"put must replace\")",
            "    keyedMapRemove(m, 'A000')",
            "    if keyedMapHas(m, 'A000') or keyedMapGet(m, 'A000') != null or keyedMapGet(m, 'A001') != b",
            "        testFail(\"remove did not clear only its key\")",
            "    testSuccess()",
            "endpackage"));
    }
}
