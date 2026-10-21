package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * The key projection of the keyed-table intrinsics, {@code wurstKeyOf}.
 *
 * <p>On Lua a keyed table's element is its own table key, so its operations never project. Jass
 * has no hashing, so a keyed structure needs an integer, and a {@code T:} parameter cannot be
 * projected to one in source. JassKeyOfLowering fills that in after generic elimination, when each
 * specialisation's element type is concrete.
 *
 * <p>The positive tests deliberately run the interpreter both before and after that pass: before
 * it the intrinsic is still generic and ILInterpreter supplies the projection, after it the
 * lowered body runs. Both must agree, or compiletime state would disagree with the final Jass.
 *
 * <p>They run on Lua too, where a direct call is lowered where it is made (LuaNativeLowering): Lua
 * keeps generics erased, so the intrinsic never gets a body there, and a call from generic code,
 * which Jass specialises, has no element type to key by on Lua and is rejected.
 */
public class KeyedTableTests extends WurstScriptTest {

    /** The intrinsic and the projections it is rewritten to, declared as the library would. */
    private static String[] withKeyedTable(String... usage) {
        java.util.List<String> lines = new java.util.ArrayList<>(java.util.Arrays.asList(
            "package KeyedTable",
            "@compilerintrinsic public function wurstKeyOf<T:>(T value) returns int",
            "    return 0",
            "@compilerintrinsic public function keyOfInt(int v) returns int",
            "    return v",
            "@compilerintrinsic public function keyOfHandle(handle h) returns int",
            "    return GetHandleId(h)",
            "endpackage"));
        lines.addAll(java.util.Arrays.asList(usage));
        return lines.toArray(new String[0]);
    }

    /** An int is its own key. */
    @Test
    public void intKeyIsIdentity() throws IOException {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    wurstKeyOf(7).assertEquals(7)",
            "    wurstKeyOf(-3).assertEquals(-3)",
            "    testSuccess()",
            "endpackage"));

        // The intrinsic's own body is the placeholder `return 0`: no call may reach it, and the
        // key is the value itself, with no call left in its place.
        String lua = compiledLua("intKeyIsIdentity");
        assertFalse("nothing may call the placeholder: " + lua, lua.contains("wurstKeyOf"));
        String init = luaFunctionBody(lua, "init_Test");
        assertTrue("an int key is the int itself: " + init,
            init.contains("assertEquals(7, 7)") && init.contains("assertEquals(-3, -3)"));
    }

    /** A handle is keyed by its id, which is what makes unit membership work on Jass. */
    @Test
    public void handleKeyIsItsHandleId() throws IOException {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    wurstKeyOf(u).assertEquals(GetHandleId(u))",
            "    let v = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    (wurstKeyOf(u) != wurstKeyOf(v)).assertTrue()",
            "    testSuccess()",
            "endpackage"));

        // The id Lua gives GetHandleId of a unit, called directly rather than through a helper.
        String lua = compiledLua("handleKeyIsItsHandleId");
        assertFalse("nothing may call the placeholder: " + lua, lua.contains("wurstKeyOf"));
        String init = luaFunctionBody(lua, "init_Test");
        assertTrue("a handle key is its id: " + init,
            init.split("__wurst_GetHandleId\\(", -1).length - 1 == 4);
    }

    /** A class instance is already an integer by the time the projection is chosen. */
    @Test
    public void classInstanceKeyIsStable() throws IOException {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "class Marker",
            "init",
            "    let a = new Marker()",
            "    let b = new Marker()",
            "    wurstKeyOf(a).assertEquals(wurstKeyOf(a))",
            "    (wurstKeyOf(a) != wurstKeyOf(b)).assertTrue()",
            "    testSuccess()",
            "endpackage"));

        // An instance is an integer id on Lua as well, so it is its own key.
        String lua = compiledLua("classInstanceKeyIsStable");
        assertFalse("nothing may call the placeholder: " + lua, lua.contains("wurstKeyOf"));
    }

    /**
     * A null element keys to 0, the id the game gives a null handle and the value of a null class
     * instance. No live handle may share it, which is why interpreter ids start at 1.
     */
    @Test
    public void nullElementKeysToZeroAndDoesNotCollide() {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    unit noUnit = null",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    wurstKeyOf(noUnit).assertEquals(0)",
            "    (wurstKeyOf(u) != wurstKeyOf(noUnit)).assertTrue()",
            "    testSuccess()",
            "endpackage"));
    }

    /** A null class instance keys to 0 on every target, as its castTo int does, and as a null unit does above. */
    @Test
    public void nullClassInstanceKeysToZeroOnLuaToo() {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "class Marker",
            "Marker none = null",
            "init",
            "    let k = wurstKeyOf(none)",
            "    let some = new Marker",
            "    if k + 1 == 1 and k == none castTo int and wurstKeyOf(some) == some castTo int and wurstKeyOf(some) != 0",
            "        testSuccess()",
            "endpackage"));
    }

    /**
     * A key handed to a native the Lua lowering replaces: the replacement is built from a copy of
     * the arguments, so the key in the copy has to be lowered as well.
     */
    @Test
    public void keyPassedToAReplacedNativeIsLowered() throws IOException {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    let ht = InitHashtable()",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    SaveInteger(ht, 0, wurstKeyOf(u), 5)",
            "    SaveInteger(ht, 0, wurstKeyOf(7), 6)",
            "    LoadInteger(ht, 0, wurstKeyOf(u)).assertEquals(5)",
            "    LoadInteger(ht, 0, wurstKeyOf(7)).assertEquals(6)",
            "    testSuccess()",
            "endpackage"));

        String lua = compiledLua("keyPassedToAReplacedNativeIsLowered");
        assertFalse("nothing may call the placeholder: " + lua, lua.contains("wurstKeyOf"));
    }

    /**
     * The keyed-table operations as the library declares them: generic in the key, with Jass bodies
     * which key through the intrinsic. Lua replaces every call of them and keys on the element, so
     * those bodies, whose calls of the intrinsic have no concrete element type there, must go with
     * the tree shake rather than be lowered or rejected.
     */
    @Test
    public void keyedTableBodiesKeyingThroughTheIntrinsicWorkOnBothBackends() throws IOException {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(
            "package KeyedTable",
            "import Table",
            "@compilerintrinsic public function keyedTableCreate() returns int",
            "    return (new Table()) castTo int",
            "@compilerintrinsic public function keyedTableAdd<T:>(int tbl, T key)",
            "    (tbl castTo Table).saveBoolean(wurstKeyOf(key), true)",
            "@compilerintrinsic public function keyedTableContains<T:>(int tbl, T key) returns boolean",
            "    return (tbl castTo Table).loadBoolean(wurstKeyOf(key))",
            "@compilerintrinsic public function keyedTableRemove<T:>(int tbl, T key)",
            "    (tbl castTo Table).removeBoolean(wurstKeyOf(key))",
            "@compilerintrinsic public function keyedTableDestroy(int tbl)",
            "    destroy (tbl castTo Table)",
            "@compilerintrinsic public function wurstKeyOf<T:>(T value) returns int",
            "    return 0",
            "@compilerintrinsic public function keyOfInt(int v) returns int",
            "    return v",
            "@compilerintrinsic public function keyOfHandle(handle h) returns int",
            "    return GetHandleId(h)",
            "endpackage",
            "package Test",
            "import KeyedTable",
            "class Marker",
            "init",
            "    let ints = keyedTableCreate()",
            "    let markers = keyedTableCreate()",
            "    let units = keyedTableCreate()",
            "    let a = new Marker()",
            "    let b = new Marker()",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    let v = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    keyedTableAdd(ints, 7)",
            "    keyedTableAdd(markers, a)",
            "    keyedTableAdd(units, u)",
            "    keyedTableContains(ints, 7).assertTrue()",
            "    keyedTableContains(ints, 8).assertFalse()",
            "    keyedTableContains(markers, a).assertTrue()",
            "    keyedTableContains(markers, b).assertFalse()",
            "    keyedTableContains(units, u).assertTrue()",
            "    keyedTableContains(units, v).assertFalse()",
            "    keyedTableRemove(units, u)",
            "    keyedTableContains(units, u).assertFalse()",
            "    keyedTableDestroy(ints)",
            "    testSuccess()",
            "endpackage");

        String lua = compiledLua("keyedTableBodiesKeyingThroughTheIntrinsicWorkOnBothBackends");
        assertTrue("membership must lower to the keyed-table stubs: " + lua,
            lua.contains("__wurst_keyedTableContains") && lua.contains("__wurst_keyedTableAdd"));
        assertFalse("the Jass bodies, and the intrinsic they call, must not reach Lua: " + lua,
            lua.contains("wurstKeyOf"));
    }

    /**
     * A direct call from generic code. Jass specialises the function, so the call keys the int it
     * is given. Lua keeps the function erased, so the element type is unknown where the call is
     * lowered: answering anything would be the placeholder again, so the call is rejected.
     */
    @Test
    public void callFromGenericCodeIsRejectedOnLuaOnly() {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib()
            .expectError("wurstKeyOf needs a concrete element type on Lua")
            .lines(withKeyedTable(
                "package Test",
                "import KeyedTable",
                "function keyOfAny<T:>(T value) returns int",
                "    return wurstKeyOf(value)",
                "init",
                "    keyOfAny(7).assertEquals(7)",
                "    testSuccess()",
                "endpackage"));
    }

    /** Lua cannot key a type Jass rejects either: there is no integer key to answer with. */
    @Test
    public void elementTypeWithoutStableKeyIsRejectedOnLua() {
        test().testLua(true).withStdLib().expectError("wurstKeyOf cannot key string on Lua").lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    let k = wurstKeyOf(\"abc\")",
            "    if k > 0",
            "        skip",
            "endpackage"));
    }

    /** The Lua lowering answers an integer too, so the declaration has to return one there as well. */
    @Test
    public void intrinsicWithWrongReturnTypeIsRejectedOnLua() {
        test().testLua(true).withStdLib().expectError("wurstKeyOf must take exactly one parameter and return int")
            .lines(
                "package KeyedTable",
                "@compilerintrinsic public function wurstKeyOf<T:>(T value) returns real",
                "    return 0.",
                "endpackage",
                "package Test",
                "import KeyedTable",
                "init",
                "    let k = wurstKeyOf(7)",
                "    if k > 0.",
                "        skip",
                "endpackage");
    }

    /** The rewrite returns an integer projection, so the intrinsic itself must return int. */
    @Test
    public void intrinsicWithWrongReturnTypeIsRejected() {
        testAssertErrorsLinesWithStdLib(false, "wurstKeyOf must take exactly one parameter and return int",
            "package KeyedTable",
            "@compilerintrinsic public function wurstKeyOf<T:>(T value) returns real",
            "    return 0.",
            "@compilerintrinsic public function keyOfInt(int v) returns int",
            "    return v",
            "@compilerintrinsic public function keyOfHandle(handle h) returns int",
            "    return GetHandleId(h)",
            "endpackage",
            "package Test",
            "import KeyedTable",
            "init",
            "    let k = wurstKeyOf(7)",
            "    if k > 0.",
            "        skip",
            "endpackage");
    }

    /**
     * StringHash is not identity: the standard library's MultibyteDiagnostics records that it collapses
     * whole classes of strings and has changed between patches - so Jass membership would
     * disagree with Lua, which keys on the string itself.
     */
    @Test
    public void stringElementIsRejected() {
        testAssertErrorsLinesWithStdLib(false, "cannot use string as its element type", withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    let k = wurstKeyOf(\"abc\")",
            "    if k > 0",
            "        skip",
            "endpackage"));
    }

    /** Tuple elimination expands the argument, so there is no single value to key on. */
    @Test
    public void tupleElementIsRejected() {
        testAssertErrorsLinesWithStdLib(false, "cannot use a tuple as its element type", withKeyedTable(
            "package Test",
            "import KeyedTable",
            "tuple pair(int a, int b)",
            "init",
            "    let k = wurstKeyOf(pair(1, 2))",
            "    if k > 0",
            "        skip",
            "endpackage"));
    }

    /** A same-named helper in another package must not be picked up. */
    @Test
    public void helperFromAnotherPackageIsNotUsed() {
        testAssertErrorsLinesWithStdLib(false, "must also declare keyOfInt",
            "package KeyedTable",
            "@compilerintrinsic public function wurstKeyOf<T:>(T value) returns int",
            "    return 0",
            "@compilerintrinsic public function keyOfHandle(handle h) returns int",
            "    return GetHandleId(h)",
            "endpackage",
            "package Impostor",
            "@compilerintrinsic public function keyOfInt(int v) returns int",
            "    return v + 1",
            "endpackage",
            "package Test",
            "import KeyedTable",
            "import Impostor",
            "init",
            "    let k = wurstKeyOf(7)",
            "    if k > 0",
            "        skip",
            "endpackage");
    }

    /** The emitted call assumes the projection's parameter type, so a wrong one is a contract error. */
    @Test
    public void projectionWithWrongParameterTypeIsRejected() {
        testAssertErrorsLinesWithStdLib(false, "keyOfInt must take exactly one int parameter",
            "package KeyedTable",
            "@compilerintrinsic public function wurstKeyOf<T:>(T value) returns int",
            "    return 0",
            "@compilerintrinsic public function keyOfInt(string v) returns int",
            "    return 1",
            "@compilerintrinsic public function keyOfHandle(handle h) returns int",
            "    return GetHandleId(h)",
            "endpackage",
            "package Test",
            "import KeyedTable",
            "init",
            "    let k = wurstKeyOf(7)",
            "    if k > 0",
            "        skip",
            "endpackage");
    }

    /** real has no stable integer key, and saying so beats keying on a truncation. */
    @Test
    public void realElementIsRejected() {
        testAssertErrorsLinesWithStdLib(false, "cannot use real as its element type", withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    let k = wurstKeyOf(1.5)",
            "    if k > 0",
            "        skip",
            "endpackage"));
    }

    @Test
    public void booleanElementIsRejected() {
        testAssertErrorsLinesWithStdLib(false, "cannot use boolean as its element type", withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    let k = wurstKeyOf(true)",
            "    if k > 0",
            "        skip",
            "endpackage"));
    }

    /**
     * A vararg declaration and a static function of a class keep their own bodies on Lua: they are
     * not the library's package functions, whatever their parameter types look like.
     */
    @Test
    public void varargAndClassFunctionsNamedLikeTableOperationsAreNotLowered() throws IOException {
        test().testLua(true).luaOnly(false).executeProg(true).withStdLib().lines(
            "package Test",
            "int total = 0",
            "@compilerintrinsic function keyedTableDestroy(vararg int tables)",
            "    for t in tables",
            "        total += t",
            "class Holder",
            "    @compilerintrinsic static function keyedTableCreate() returns int",
            "        total += 1000",
            "        return 7",
            "    @compilerintrinsic static function keyedTableContains(int table, int key) returns boolean",
            "        total += 10000",
            "        return true",
            "init",
            "    keyedTableDestroy(1, 2, 3)",
            "    keyedTableDestroy(4)",
            "    if Holder.keyedTableCreate() == 7 and Holder.keyedTableContains(0, 1) and total == 11010",
            "        testSuccess()",
            "endpackage");

        String compiled = Files.toString(new File(
            "test-output/lua/KeyedTableTests_varargAndClassFunctionsNamedLikeTableOperationsAreNotLowered.lua"),
            Charsets.UTF_8);
        assertFalse("none of them is replaced by a stub", compiled.contains("__wurst_keyedTable"));
    }

    /** The body of a top-level Lua function, up to its closing {@code end} at column 0. */
    private static String luaFunctionBody(String lua, String functionName) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
            "function\\s*" + functionName + "\\s*\\([^\\n]*\\n(.*?)\\nend", java.util.regex.Pattern.DOTALL)
            .matcher(lua);
        assertTrue("function " + functionName + " was not found: " + lua, matcher.find());
        return matcher.group(1);
    }
}
