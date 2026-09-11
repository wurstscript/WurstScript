package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * The Jass side of the keyed-table intrinsics.
 *
 * <p>On Lua the element is its own table key and there is nothing to project. Jass has no hashing,
 * so a keyed structure needs an integer, and a {@code T:} parameter cannot be projected to one in
 * source. JassKeyOfLowering fills that in after generic elimination, when each specialisation's
 * element type is concrete.
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
            "@compilerintrinsic public function keyOfString(string s) returns int",
            "    return StringHash(s)",
            "endpackage"));
        lines.addAll(java.util.Arrays.asList(usage));
        return lines.toArray(new String[0]);
    }

    /** An int is its own key. */
    @Test
    public void intKeyIsIdentity() {
        test().executeProg(true).executeProgOnlyAfterTransforms().withStdLib().lines(withKeyedTable(
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
        test().executeProg(true).executeProgOnlyAfterTransforms().withStdLib().lines(withKeyedTable(
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
        test().executeProg(true).executeProgOnlyAfterTransforms().withStdLib().lines(withKeyedTable(
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

    @Test
    public void stringKeyIsItsHash() {
        test().executeProg(true).executeProgOnlyAfterTransforms().withStdLib().lines(withKeyedTable(
            "package Test",
            "import KeyedTable",
            "init",
            "    wurstKeyOf(\"abc\").assertEquals(StringHash(\"abc\"))",
            "    testSuccess()",
            "endpackage"));
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
}
