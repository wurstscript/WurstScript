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
        assertTrue("generic operations lower to the keyed-map stubs",
            compiled.contains("__wurst_keyedMapPut") && compiled.contains("__wurst_keyedMapGetInt"));
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
            "endpackage"));

        String lua = compiled("handleBoundWrapperSpecializesToOneLuaTableAccess");
        String init = getFunctionBody(lua, "init_Test");
        String put = getFunctionBody(lua, "KeyedMap_KeyedMap_put");
        String get = getFunctionBody(lua, "KeyedMap_KeyedMap_get");
        assertTrue("specialized wrapper calls the unchanged put intrinsic: " + put,
            put.contains("__wurst_keyedMapPut"));
        assertTrue("specialized wrapper calls the unchanged get intrinsic: " + get,
            get.contains("__wurst_keyedMapGetInt"));
        assertFalse("the native unit is used as the key without handle-id conversion: " + init + put + get,
            (init + put + get).contains("GetHandleId") || (init + put + get).contains("__wurst_objectToIndex")
                || (init + put + get).contains("__wurst_classToIndex"));
        assertTrue("put is one direct Lua table store",
            getFunctionBody(lua, "__wurst_keyedMapPut").contains("t[k] = v"));
        assertTrue("get is one direct Lua table read",
            getFunctionBody(lua, "__wurst_keyedMapGetInt").contains("return t[k] or 0"));
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
            "endpackage");

        String lua = compiled("handleBoundGenericValuesKeepTheirNativeLuaRepresentation");
        String init = getFunctionBody(lua, "init_Test");
        String put = getFunctionBody(lua, "KeyedMap_KeyedMap_put");
        String get = getFunctionBody(lua, "KeyedMap_KeyedMap_get");
        assertTrue("specialized generic wrapper calls the generic put intrinsic: " + put,
            put.contains("__wurst_keyedMapPut"));
        assertTrue("specialized generic wrapper calls the generic get intrinsic: " + get,
            get.contains("__wurst_keyedMapGet"));
        String wrapperCalls = init + put + get;
        assertFalse("generic values and handle keys need no index conversions: " + wrapperCalls,
            wrapperCalls.contains("GetHandleId") || wrapperCalls.contains("__wurst_objectToIndex")
                || wrapperCalls.contains("__wurst_classToIndex") || wrapperCalls.contains("__wurst_classFromIndex"));
        String putStub = getFunctionBody(lua, "__wurst_keyedMapPut");
        String getStub = getFunctionBody(lua, "__wurst_keyedMapGet");
        assertTrue("generic put has exactly one direct table store: " + putStub,
            putStub.contains("t[k] = v") && putStub.indexOf("t[k]") == putStub.lastIndexOf("t[k]"));
        assertTrue("generic get has exactly one direct table read: " + getStub,
            getStub.contains("return t[k]") && getStub.indexOf("t[k]") == getStub.lastIndexOf("t[k]"));
    }

    @Test
    public void nativeIntegerGetterThroughGenericWrapperUsesRawLuaStub() throws IOException {
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
            "endpackage");

        String lua = compiled("nativeIntegerGetterThroughGenericWrapperUsesRawLuaStub");
        String init = getFunctionBody(lua, "init_Test");
        assertTrue("the generic wrapper result is normalized to Wurst's int default: " + init,
            init.contains("__wurst_ensureInt(readNative(map, u))"));
        assertTrue("the generic getter still reaches the raw table stub", lua.contains("return t[k]"));
        assertFalse("integer get must not leave the failing source fallback body",
            lua.contains("function keyedMapGetNative_unit_int") && lua.contains("return nil"));
        String getStub = getFunctionBody(lua, "__wurst_keyedMapGet");
        assertTrue("generic get is one direct table read: " + getStub,
            getStub.contains("return t[k]") && getStub.indexOf("t[k]") == getStub.lastIndexOf("t[k]"));
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
            .contains("keyedMapPutNative requires an int map, a handle key, and an int-represented value"));
        assertFalse(failure.getMessage(), failure.getMessage().contains("ArrayIndexOutOfBounds"));
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
        test().expectError("keyedMapPutNative requires an int map, a handle key, and an int-represented value")
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
        assertTrue("the operations lower to the keyed-map stubs: " + init,
            init.contains("__wurst_keyedMapPut") && init.contains("__wurst_keyedMapGetInt")
                && init.contains("__wurst_keyedMapHas") && init.contains("__wurst_keyedMapRemove"));
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
        test().testLua(true).executeProg(true).withStdLib().lines(
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
}
