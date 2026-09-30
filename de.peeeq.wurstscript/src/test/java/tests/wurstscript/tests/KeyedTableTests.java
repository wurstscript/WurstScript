package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.assertFalse;

/**
 * The Jass side of the keyed-table intrinsics.
 *
 * <p>On Lua the element is its own table key and there is nothing to project. Jass has no hashing,
 * so a keyed structure needs an integer, and a {@code T:} parameter cannot be projected to one in
 * source. JassKeyOfLowering fills that in after generic elimination, when each specialisation's
 * element type is concrete.
 *
 * <p>The positive tests deliberately run the interpreter both before and after that pass: before
 * it the intrinsic is still generic and ILInterpreter supplies the projection, after it the
 * lowered body runs. Both must agree, or compiletime state would disagree with the final Jass.
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
    public void intKeyIsIdentity() {
        test().executeProg(true).withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    wurstKeyOf(7).assertEquals(7)",
            "    wurstKeyOf(-3).assertEquals(-3)",
            "    testSuccess()",
            "endpackage"));
    }

    /** A handle is keyed by its id, which is what makes unit membership work on Jass. */
    @Test
    public void handleKeyIsItsHandleId() {
        test().executeProg(true).withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    let u = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    wurstKeyOf(u).assertEquals(GetHandleId(u))",
            "    let v = CreateUnit(Player(0), 'hfoo', 0., 0., 0.)",
            "    (wurstKeyOf(u) != wurstKeyOf(v)).assertTrue()",
            "    testSuccess()",
            "endpackage"));
    }

    /** A class instance is already an integer by the time the projection is chosen. */
    @Test
    public void classInstanceKeyIsStable() {
        test().executeProg(true).withStdLib().lines(withKeyedTable(
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
    }

    /**
     * A null element keys to 0, the id the game gives a null handle and the value of a null class
     * instance. No live handle may share it, which is why interpreter ids start at 1.
     */
    @Test
    public void nullElementKeysToZeroAndDoesNotCollide() {
        test().executeProg(true).withStdLib().lines(withKeyedTable(
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
     * StringHash is not identity - this repo's MultibyteDiagnostics records that it collapses
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
            "    if Holder.keyedTableCreate() == 7 and Holder.keyedTableContains(0, 1) and total == 11006",
            "        testSuccess()",
            "endpackage");

        String compiled = Files.toString(new File(
            "test-output/lua/KeyedTableTests_varargAndClassFunctionsNamedLikeTableOperationsAreNotLowered.lua"),
            Charsets.UTF_8);
        assertFalse("none of them is replaced by a stub", compiled.contains("__wurst_keyedTable"));
    }
}
