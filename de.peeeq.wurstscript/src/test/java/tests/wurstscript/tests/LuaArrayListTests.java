package tests.wurstscript.tests;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/** Public ArrayList API on both backends, including its Lua storage and explicit object lifetime. */
public class LuaArrayListTests extends WurstScriptTest {
    @Test
    public void recycledGenericArrayBuffersRestoreTheirTypedDefaults() {
        test().testLua(true).luaOnly(false).optimize().inline().localOptimizations().executeProg().lines(
            "package Test", "native testSuccess()", "native testFail(string message)",
            "class Value",
            "class Buffer<T:>", "    static T array typedStore", "    T array[1] cells",
            "    function read() returns T", "        return cells[0]",
            "    function readStatic() returns T", "        return typedStore[0]",
            "init", "    let a = new Buffer<int>()",
            "    if a.read() != 0 or a.readStatic() != 0", "        testFail(\"integer default\")",
            "    destroy a", "    let b = new Buffer<string>()",
            "    if b.read() != null or b.readStatic() != null", "        testFail(\"string default on recycled ID\")",
            "    destroy b", "    let c = new Buffer<boolean>()",
            "    if c.read() != false or c.readStatic() != false", "        testFail(\"boolean default on recycled ID\")",
            "    destroy c", "    let d = new Buffer<Value>()",
            "    if d.read() != null or d.readStatic() != null", "        testFail(\"class default on recycled ID\")",
            "    destroy d", "    testSuccess()");
    }

    @Test
    public void manyArrayBuffersReuseOneAllocatorTemporary() throws IOException {
        java.util.List<String> source = new java.util.ArrayList<>(java.util.List.of(
            "package Test", "native testSuccess()", "native testFail(string message)", "class Wide"));
        for (int i = 0; i < 201; i++) source.add("    int array[1] cells" + i);
        source.addAll(java.util.List.of("init", "    let first = new Wide()"));
        for (int i = 0; i < 201; i++) source.add("    first.cells" + i + "[0] = " + (i + 1));
        source.addAll(java.util.List.of("    destroy first", "    let second = new Wide()"));
        for (int i = 0; i < 201; i++) {
            source.add("    if second.cells" + i + "[0] != 0");
            source.add("        testFail(\"buffer " + i + " was not reset\")");
        }
        source.addAll(java.util.List.of("    destroy second", "    testSuccess()"));
        test().testLua(true).inline().localOptimizations().executeProg().lines(source.toArray(String[]::new));
        assertFalse("array reset must not force a per-lifetime locals-table allocation",
            lua("manyArrayBuffersReuseOneAllocatorTemporary").contains("__wurst_locals"));
    }

    @Test
    public void luaCompiletimeStorageSurvivesSerialization() {
        test().withStdLib().testLua(true).inline().localOptimizations()
            .executeProgOnlyAfterTransforms().executeProg().lines(
                "package Test", "import ArrayList",
                "@compiletime function create() returns ArrayList<int>", "    let result = new ArrayList<int>(1)",
                "    for i = 0 to 127", "        result.add(i)", "    return result",
                "let values = compiletime(create())", "init",
                "    if values.size() != 128 or values.get(127) != 127",
                "        testFail(\"compiletime contents\")",
                "    values.add(128)", "    if values.get(128) != 128", "        testFail(\"runtime append\")",
                "    destroy values", "    testSuccess()");
    }

    @Test
    public void publicStorageAndLifetimeRegressions() {
        test().withStdLib().testLua(true).luaOnly(false).inline().localOptimizations().executeProg().lines(
            "package Test", "import ArrayListNativeStorageTests", "init",
            "    explicitElementLifetimeAndRecycledListId()", "    defaultsHolesAndTupleCopies()",
            "    updateAllPreservesJassWriteAddress()",
            "    testSuccess()");
    }

    @Test
    public void tupleNullClassFieldsKeepTheirType() {
        test().testLua(true).luaOnly(false).inline().localOptimizations().executeProg().lines(
            "package Test", "native testSuccess()", "native testFail(string message)",
            "class Item", "    int value = 7",
            "tuple Payload(int count, Item reference)",
            "class Buffer", "    Payload array[2] entries",
            "@noinline function emptyPayload() returns Payload", "    return Payload(99, null)",
            "init", "    let buffer = new Buffer()", "    buffer.entries[0] = emptyPayload()",
            "    if buffer.entries[0].count != 99 or buffer.entries[0].reference != null",
            "        testFail(\"null class in tuple return/array\")",
            "    let item = new Item()", "    buffer.entries[1] = Payload(7, item)",
            "    buffer.entries[1] = Payload(8, null)",
            "    if buffer.entries[1].reference != null", "        testFail(\"null class in tuple assignment\")",
            "    destroy item", "    destroy buffer", "    testSuccess()");
    }

    @Test
    public void backendBranchesAreRemovedBeforeInlining() throws IOException {
        java.util.List<String> source = new java.util.ArrayList<>(java.util.List.of(
            "package Test", "import MagicFunctions",
            "@noinline function external(int value) returns int", "    return value",
            "function bookkeeping(int input) returns int", "    var value = input", "    if isLua", "        return value"));
        for (int i = 0; i < 40; i++) source.add("    value += external(1)");
        source.add("    return value");
        for (int i = 0; i < 4; i++) {
            source.add("@noinline function caller" + i + "(int value) returns int");
            source.add("    return bookkeeping(value)");
        }
        source.addAll(java.util.List.of("init",
            "    let sum = caller0(7) + caller1(8) + caller2(9) + caller3(10)",
            "    if sum != (isLua ? 34 : 194)", "        testFail(\"backend branch\")",
            "    testSuccess()"));
        test().withStdLib().testLua(true).luaOnly(false).inline().localOptimizations().executeProg()
            .lines(source.toArray(String[]::new));
        assertFalse("Jass-only work must not inflate the Lua inlining cost",
            lua("backendBranchesAreRemovedBeforeInlining").contains("bookkeeping("));
    }

    @Test
    public void recycledArrayFieldsReuseTheirTables() throws IOException {
        test().testLua(true).inline().localOptimizations().executeProg().lines(
            "package Test", "native testSuccess()", "native testFail(string message)",
            "class Grid", "    int array[8] cells", "    string array[8] labels",
            "init", "    let a = new Grid()", "    let id = a castTo int",
            "    a.cells[7] = 42", "    a.labels[3] = \"old\"", "    destroy a",
            "    let b = new Grid()",
            "    if b castTo int != id or b.cells[7] != 0 or b.labels[3] != \"\"",
            "        testFail(\"recycled array fields must be reset\")",
            "    b.cells[7] = 19",
            "    if (id castTo Grid).cells[7] != 19", "        testFail(\"stale ID must alias the replacement\")",
            "    destroy b", "    testSuccess()");
        assertTrue("recycle the backing table instead of generating garbage on ID reuse",
            lua("recycledArrayFieldsReuseTheirTables").contains("__wurst_resetArray"));
    }

    @Test
    public void nativeStorageHasNoArena() throws IOException {
        test().withStdLib().testLua(true).luaOnly(false).inline().localOptimizations().executeProg().lines(
            "package Test", "import ArrayList",
            "init",
            "    let a = new ArrayList<int>(1)",
            "    let b = new ArrayList<int>(1)",
            "    for i = 0 to 127",
            "        a.add(i)",
            "        b.add(1000 + i)",
            "    a.addAll(a)",
            "    if a.size() != 256 or a.get(128) != 0 or a.get(255) != 127 or b.get(127) != 1127",
            "        testFail(\"growth, self-copy or isolation\")",
            "    destroy a", "    destroy b", "    testSuccess()");
        String lua = lua("nativeStorageHasNoArena");
        assertFalse("Lua must not allocate arena sections", lua.contains("nextFreeIndex"));
        assertFalse("Lua must not keep a free-section pool", lua.contains("freeSection"));
        assertFalse("Lua must not offset element reads", lua.contains("startIndex"));
        assertTrue("Lua must use an array field per list", lua.contains("elements_storage"));
        String jass = Files.readString(Path.of("test-output/LuaArrayListTests_nativeStorageHasNoArena_inlopt.j"),
            StandardCharsets.UTF_8);
        assertTrue("Jass keeps its shared arena", jass.contains("nextFreeIndex"));
        assertFalse("optimized Jass must not retain the Lua array field", jass.contains("elements"));
    }

    private static String lua(String name) throws IOException {
        return Files.readString(Path.of("test-output/lua/LuaArrayListTests_" + name + ".lua"),
            StandardCharsets.UTF_8);
    }
}
