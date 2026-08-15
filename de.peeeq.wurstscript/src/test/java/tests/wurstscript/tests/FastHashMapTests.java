package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * Exercises type class bounds through the container they were added for: a hash map whose key type
 * is bounded by {@code Hashable}, so it can hash and compare keys without erasing them to int.
 * <p>
 * This is deliberately a whole working container rather than a minimal repro. It combines things
 * nothing else tests together: two type parameters where only the first is bounded, static arrays of
 * a bounded type parameter, and a bound used from a private method reached through a public one.
 */
public class FastHashMapTests extends WurstScriptTest {

    /**
     * Storage follows ArrayList: one array per specialisation, carved into a section per instance.
     * Collision handling is linear probing, so a lookup calls both requirements of the bound.
     */
    private static String[] fastHashMap(String... extra) {
        String[] head = {
            "package test",
            "native testSuccess()",
            "constant int CAPACITY = 8",
            "interface Hashable<T:>",
            "    function hash(T x) returns int",
            "    function equals(T a, T b) returns boolean",
            "class FastHashMap<K: Hashable, V:>",
            "    private static K array keys",
            "    private static V array values",
            "    private static boolean array used",
            "    private static int nextFree = 0",
            "    private int base",
            "    private int count = 0",
            "    construct()",
            "        base = nextFree",
            "        nextFree += CAPACITY",
            "    private function slotFor(K key) returns int",
            "        var i = K.hash(key) mod CAPACITY",
            "        if i < 0",
            "            i += CAPACITY",
            "        while used[base + i] and not K.equals(keys[base + i], key)",
            "            i = (i + 1) mod CAPACITY",
            "        return base + i",
            "    function put(K key, V value)",
            "        let s = slotFor(key)",
            "        if not used[s]",
            "            used[s] = true",
            "            keys[s] = key",
            "            count++",
            "        values[s] = value",
            "    function get(K key) returns V",
            "        return values[slotFor(key)]",
            "    function has(K key) returns boolean",
            "        return used[slotFor(key)]",
            "    function size() returns int",
            "        return count",
        };
        String[] all = new String[head.length + extra.length];
        System.arraycopy(head, 0, all, 0, head.length);
        System.arraycopy(extra, 0, all, head.length, extra.length);
        return all;
    }

    private static final String[] INT_INSTANCE = {
        "implements Hashable<int>",
        "    function hash(int x) returns int",
        "        return x",
        "    function equals(int a, int b) returns boolean",
        "        return a == b",
    };

    private static String[] program(String[]... parts) {
        int size = 0;
        for (String[] part : parts) {
            size += part.length;
        }
        String[] all = new String[size];
        int at = 0;
        for (String[] part : parts) {
            System.arraycopy(part, 0, all, at, part.length);
            at += part.length;
        }
        return all;
    }

    /** Keys 1 and 9 land in the same slot at capacity 8, so the probe path is taken. */
    private static final String[] USE_WITH_COLLISION = {
        "init",
        "    let m = new FastHashMap<int, int>()",
        "    m.put(1, 10)",
        "    m.put(9, 90)",
        "    m.put(2, 20)",
        "    if m.get(1) == 10 and m.get(9) == 90 and m.get(2) == 20",
        "        if m.size() == 3 and not m.has(3)",
        "            testSuccess()",
    };

    @Test
    public void fastHashMapRuntime() {
        testAssertOkLines(true, program(fastHashMap(), INT_INSTANCE, USE_WITH_COLLISION));
    }

    @Test
    public void fastHashMapRuntimeLua() {
        test().testLua(true).executeProg().lines(program(fastHashMap(), INT_INSTANCE, USE_WITH_COLLISION));
    }

    /**
     * A tuple key, which is the case old generics cannot serve at all: a tuple has no int
     * representation to cast to, so the only way to key a map by one is to say how it hashes.
     */
    private static final String[] TUPLE_INSTANCE = {
        "tuple pos(int x, int y)",
        "implements Hashable<pos>",
        "    function hash(pos p) returns int",
        "        return p.x * 31 + p.y",
        "    function equals(pos a, pos b) returns boolean",
        "        return a.x == b.x and a.y == b.y",
    };

    @Test
    public void tupleKey() {
        testAssertOkLines(true, program(fastHashMap(), TUPLE_INSTANCE, new String[]{
            "init",
            "    let m = new FastHashMap<pos, int>()",
            "    m.put(pos(1, 2), 12)",
            "    m.put(pos(2, 1), 21)",
            "    if m.get(pos(1, 2)) == 12 and m.get(pos(2, 1)) == 21",
            "        if m.has(pos(1, 2)) and not m.has(pos(9, 9))",
            "            testSuccess()"
        }));
    }

    @Test
    public void tupleKeyLua() {
        test().testLua(true).executeProg().lines(program(fastHashMap(), TUPLE_INSTANCE, new String[]{
            "init",
            "    let m = new FastHashMap<pos, int>()",
            "    m.put(pos(1, 2), 12)",
            "    m.put(pos(2, 1), 21)",
            "    if m.get(pos(1, 2)) == 12 and m.get(pos(2, 1)) == 21",
            "        testSuccess()"
        }));
    }

    /** A key type of the user's own, with the instance declared in the same package as the type. */
    @Test
    public void classKeyWithUserInstance() {
        testAssertOkLines(true, program(fastHashMap(), new String[]{
            "class Item",
            "    int id",
            "    construct(int id)",
            "        this.id = id",
            "implements Hashable<Item>",
            "    function hash(Item i) returns int",
            "        return i.id",
            "    function equals(Item a, Item b) returns boolean",
            "        return a.id == b.id",
            "init",
            "    let m = new FastHashMap<Item, string>()",
            "    let a = new Item(1)",
            "    let b = new Item(2)",
            "    m.put(a, \"a\")",
            "    m.put(b, \"b\")",
            "    if m.get(a) == \"a\" and m.get(b) == \"b\" and m.size() == 2",
            "        testSuccess()"
        }));
    }

    /**
     * Two specialisations live at once. Storage is static per specialisation, so this is what would
     * break if the arrays of one instantiation were shared with another.
     */
    @Test
    public void twoSpecialisationsCoexist() {
        testAssertOkLines(true, program(fastHashMap(), INT_INSTANCE, new String[]{
            // Every string hashes alike, so this also exercises the probe path on every lookup.
            "implements Hashable<string>",
            "    function hash(string s) returns int",
            "        return 0",
            "    function equals(string a, string b) returns boolean",
            "        return a == b",
            "init",
            "    let ints = new FastHashMap<int, int>()",
            "    let strs = new FastHashMap<string, int>()",
            "    ints.put(1, 100)",
            "    strs.put(\"a\", 200)",
            "    if ints.get(1) == 100 and strs.get(\"a\") == 200",
            "        if ints.size() == 1 and strs.size() == 1",
            "            testSuccess()"
        }));
    }

    /** Two maps of the same instantiation must not share storage either. */
    @Test
    public void twoInstancesOfOneSpecialisation() {
        testAssertOkLines(true, program(fastHashMap(), INT_INSTANCE, new String[]{
            "init",
            "    let a = new FastHashMap<int, int>()",
            "    let b = new FastHashMap<int, int>()",
            "    a.put(1, 10)",
            "    b.put(1, 20)",
            "    if a.get(1) == 10 and b.get(1) == 20 and a.size() == 1 and b.size() == 1",
            "        testSuccess()"
        }));
    }
}
