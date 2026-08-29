package tests.wurstscript.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.eclipse.jdt.annotation.Nullable;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            // A removed slot cannot go back to empty: a probe that stopped there would miss keys
            // put down beyond it. It becomes a tombstone instead - passed over when searching,
            // reused when putting.
            "    private static boolean array dead",
            // Never written, so a read yields V's default. That is the only way to say "no value"
            // for a type parameter, and it costs an array read rather than a branch.
            "    private static V array none",
            "    private static int nextFree = 0",
            "    private int base",
            "    private int count = 0",
            "    construct()",
            "        base = nextFree",
            "        nextFree += CAPACITY",
            // The slot holding key, or the one it belongs in: the first tombstone passed over,
            // else the empty slot the probe stopped at. Capacity is fixed, so a full table
            // returns -1 rather than probing forever.
            "    private function slotFor(K key) returns int",
            "        var i = K.hash(key) mod CAPACITY",
            "        if i < 0",
            "            i += CAPACITY",
            "        var firstDead = -1",
            "        var probes = 0",
            "        while probes < CAPACITY",
            "            let s = base + i",
            "            if used[s] and K.equals(keys[s], key)",
            "                return s",
            "            if not used[s] and not dead[s]",
            "                if firstDead >= 0",
            "                    return firstDead",
            "                return s",
            "            if dead[s] and firstDead < 0",
            "                firstDead = s",
            "            i = (i + 1) mod CAPACITY",
            "            probes++",
            "        return firstDead",
            "    function put(K key, V value)",
            "        let s = slotFor(key)",
            // Capacity is fixed, so a new key can arrive with nowhere to go. The other three
            // guard the same way; without it this writes through the sentinel and counts it.
            "        if s < base",
            "            return",
            "        if not used[s]",
            "            used[s] = true",
            "            dead[s] = false",
            "            keys[s] = key",
            "            count++",
            "        values[s] = value",
            "    function get(K key) returns V",
            "        let s = slotFor(key)",
            "        if s < base or not used[s]",
            "            return none[0]",
            "        return values[s]",
            "    function has(K key) returns boolean",
            "        let s = slotFor(key)",
            "        return s >= base and used[s]",
            "    function remove(K key) returns boolean",
            "        let s = slotFor(key)",
            "        if s < base or not used[s]",
            "            return false",
            "        used[s] = false",
            "        dead[s] = true",
            "        count--",
            "        return true",
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
    public void fastHashMapRuntimeLua() throws IOException {
        test().testLua(true).executeProg().lines(program(fastHashMap(), INT_INSTANCE, USE_WITH_COLLISION));
        assertEachSlotBindsItsOwnMethod(compiledLua("fastHashMapRuntimeLua"));
    }

    private static final String[] METHOD_NAMES = {"slotFor", "put", "get", "has", "size", "remove"};

    private String compiledLua(String testName) throws IOException {
        return Files.toString(new File("test-output/lua/FastHashMapTests_" + testName + ".lua"), Charsets.UTF_8);
    }

    /**
     * A dispatch slot is named after the method it belongs to, and nothing else in the emitted Lua
     * records which implementation belongs there — so this is the only place the two can be checked
     * against each other. Specialisation names every method of one instantiation after the same type
     * argument, which is exactly when they are easiest to confuse.
     */
    private void assertEachSlotBindsItsOwnMethod(String lua) {
        Matcher assignment = Pattern.compile("(?m)^\\s*\\w+\\.(\\w+) = (\\w+)\\s*$").matcher(lua);
        while (assignment.find()) {
            String slot = assignment.group(1);
            String implementation = assignment.group(2);
            for (String method : METHOD_NAMES) {
                if (namesMethod(slot, method) && !namesMethod(implementation, method)) {
                    throw new AssertionError("slot '" + slot + "' is named after " + method
                        + " but binds '" + implementation + "'");
                }
            }
        }
    }

    /** Whether {@code name} carries {@code method} as one of its underscore-separated segments. */
    private static boolean namesMethod(String name, String method) {
        return name.equals(method)
            || name.startsWith(method + "_")
            || name.endsWith("_" + method)
            || name.contains("_" + method + "_");
    }

    /**
     * Keys 1, 9 and 2 form one probe run at capacity 8. Removing the middle of it is the case
     * tombstones exist for: with the slot marked empty instead, the probe for 2 would stop at it
     * and report the key missing.
     */
    private static final String[] USE_WITH_REMOVE = {
        "init",
        "    let m = new FastHashMap<int, int>()",
        "    m.put(1, 10)",
        "    m.put(9, 90)",
        "    m.put(2, 20)",
        "    if m.remove(9) and not m.remove(9)",
        "        if m.get(2) == 20 and m.has(2) and not m.has(9) and m.size() == 2",
        // The tombstone is reused rather than leaked, so the run stays the same length.
        "            m.put(9, 91)",
        "            if m.get(9) == 91 and m.size() == 3 and m.get(1) == 10 and m.get(2) == 20",
        "                testSuccess()",
    };

    /**
     * Capacity is fixed, so the ninth distinct key has nowhere to go: keys 1 to 8 hash to the eight
     * slots exactly. `slotFor` says so by returning a slot below this instance's section, and every
     * entry point has to check that before indexing — otherwise the write goes through the sentinel,
     * the entry is unreachable, and the count says nine.
     */
    @Test
    public void puttingIntoAFullMapChangesNothing() {
        testAssertOkLines(true, program(fastHashMap(), INT_INSTANCE, new String[]{
            "init",
            "    let m = new FastHashMap<int, int>()",
            "    for i = 1 to 8",
            "        m.put(i, i * 10)",
            "    m.put(9, 90)",
            "    if m.size() == 8 and not m.has(9)",
            // Not m.get(9) as well: reading an absent key of a type parameter returns a stand-in
            // the interpreter cannot compare, which is a separate bug fixed later in this stack.
            "        if m.get(1) == 10 and m.get(8) == 80",
            "            testSuccess()"
        }));
    }

    @Test
    public void removeLeavesATombstone() {
        testAssertOkLines(true, program(fastHashMap(), INT_INSTANCE, USE_WITH_REMOVE));
    }

    @Test
    public void removeLeavesATombstoneLua() {
        test().testLua(true).executeProg().lines(program(fastHashMap(), INT_INSTANCE, USE_WITH_REMOVE));
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

    /**
     * The point of bounds over {@code HashMap extends Table}: the requirements are resolved when the
     * map is specialised, not carried to runtime. This asserts on the least optimised configuration
     * on purpose — the cost has to be absent by construction, not removed afterwards by the inliner.
     */
    @Test
    public void emittedCodeCostsNothingExtra() throws IOException {
        testAssertOkLines(true, program(fastHashMap(), INT_INSTANCE, USE_WITH_COLLISION));
        String jass = compiledJass("emittedCodeCostsNothingExtra_no_opts");

        // Storage is plain Jass arrays, so a lookup is an array read, not a native call.
        for (String[] storage : new String[][]{{"integer", "keys"}, {"integer", "values"},
            {"boolean", "used"}, {"boolean", "dead"}}) {
            assertMatches(jass, "(?m)^" + storage[0] + " array FastHashMap_" + storage[1] + "\\w*$",
                "storage array " + storage[1]);
        }
        for (String hashtableNative : new String[]{"InitHashtable", "SaveInteger", "LoadInteger",
            "SaveStr", "LoadStr", "FlushChildHashtable", "GetHandleId"}) {
            if (jass.contains(hashtableNative)) {
                throw new AssertionError("the map reached for a WC3 hashtable native: " + hashtableNative);
            }
        }

        // The bound resolves to a direct call to the instance's own function. An instance passed at
        // runtime would show up as an extra parameter here, and a dispatched one as an indirection.
        Matcher slotFor = Pattern
            // Not the dispatch_ wrapper of the same name - that one is the nullpointer check every
            // class method gets, and it is the real function underneath that has to be free of cost.
            .compile("(?m)^function (?!dispatch_)\\w*slotFor\\w* takes ([^\\n]*?) returns [^\\n]*\\n(.*?)\\nendfunction",
                Pattern.DOTALL)
            .matcher(jass);
        if (!slotFor.find()) {
            throw new AssertionError("no slotFor in the emitted Jass:\n" + jass);
        }
        if (!slotFor.group(1).equals("integer this, integer key")) {
            throw new AssertionError("slotFor carries something beyond the receiver and the key: "
                + slotFor.group(1));
        }
        String body = slotFor.group(2);
        assertMatches(body, "(?<![\\w_])hash\\w*\\(", "direct call to the hash requirement");
        assertMatches(body, "(?<![\\w_])equals\\w*\\(", "direct call to the equals requirement");
        for (String indirection : new String[]{"ExecuteFunc", "TriggerEvaluate", "dispatch_"}) {
            if (body.contains(indirection)) {
                throw new AssertionError("slotFor reaches the requirement through " + indirection
                    + ":\n" + body);
            }
        }
    }

    /**
     * Everything above compiles a package on its own. The container is meant to live in the standard
     * library, and that is a different question: the bound has to keep dispatching with everything the
     * library defines in scope, {@code int} has to keep taking the instance declared beside the map
     * rather than anything the library brings, and the specialised copies have to survive a program of
     * that size being optimised around them.
     */
    private static String[] withStandardLibrary(String[] lines) {
        return java.util.Arrays.stream(lines)
            .filter(line -> !line.equals("native testSuccess()"))
            .toArray(String[]::new);
    }

    @Test
    public void fastHashMapAgainstTheStandardLibrary() {
        test().withStdLib().executeProg()
            .lines(withStandardLibrary(program(fastHashMap(), INT_INSTANCE, USE_WITH_COLLISION)));
    }

    /**
     * Compiled rather than run, as every other test which puts the standard library on Lua is: the
     * runtime shim cannot initialise the library's own packages, so no standard library program has
     * ever executed on that target here. What this covers is that the container survives translation
     * with the library in scope, which is where the specialised copies and the erased ones meet.
     */
    @Test
    public void fastHashMapAgainstTheStandardLibraryLua() throws IOException {
        test().withStdLib().testLua(true).executeProg()
            .lines(withStandardLibrary(program(fastHashMap(), INT_INSTANCE, USE_WITH_COLLISION)));
        assertSpecialisedClassesAllocateTheirFields(
            Files.toString(new File("test-output/lua/FastHashMapTests_fastHashMapAgainstTheStandardLibraryLua.lua"),
                Charsets.UTF_8));
    }

    /**
     * The erased class and any specialised copy of it agree on the fields they allocate.
     * <p>
     * Nothing refers to the copies a specialised class holds - an access made before specialisation
     * still names the original's variable - so a pass which drops unread fields drops all of them,
     * and an instance allocated from the specialised class comes out with no fields at all while the
     * emitted code goes on reading them by name.
     * <p>
     * A specialisation whose methods are bound to the erased class the objects come from has nothing
     * left to allocate and is not emitted, which is the shape this target aims for and leaves no field
     * set to disagree. One emitted without being allocated is neither: dead weight, and the two class
     * shapes coexisting is what produced the bug above. Both accepted states are named, so this cannot
     * pass by finding nothing.
     */
    private static void assertSpecialisedClassesAllocateTheirFields(String compiled) {
        String erasedFields = allocatedFields(compiled, "FastHashMap");
        String specialisedFields = allocatedFieldsOrNull(compiled, "FastHashMap_specialized\\w*");
        if (specialisedFields == null) {
            // The class table, not any name containing it: a specialised function is named after the
            // one it was copied from, so matching the bare name would read those as a class.
            if (Pattern.compile("(?m)^\\s*FastHashMap_specialized\\w*\\s*=\\s*\\(\\{\\s*\\}\\)")
                    .matcher(compiled).find()) {
                throw new AssertionError("a specialised class is emitted but never allocated;"
                    + " its methods should be bound to the class the objects come from, leaving nothing"
                    + " of it behind, in:\n" + compiled);
            }
            return;
        }
        if (!erasedFields.equals(specialisedFields)) {
            throw new AssertionError("the specialised class should allocate the same fields as the erased one."
                + "\n  erased:      " + erasedFields
                + "\n  specialised: " + specialisedFields);
        }
    }

    private static String allocatedFields(String compiled, String classPattern) {
        String fields = allocatedFieldsOrNull(compiled, classPattern);
        if (fields == null || fields.isEmpty()) {
            throw new AssertionError("expected an allocation for " + classPattern + " in:\n" + compiled);
        }
        return fields;
    }

    private static @Nullable String allocatedFieldsOrNull(String compiled, String classPattern) {
        Matcher allocation = Pattern.compile("function " + classPattern + ":create\\d*\\(\\)\\s*\\R"
            + "(.*?)\\R\\s*return new_inst\\s*\\Rend", Pattern.DOTALL).matcher(compiled);
        if (!allocation.find()) {
            return null;
        }
        Matcher field = Pattern.compile("(?m)^\\s*(\\w+_storage)\\[new_inst\\]\\s*=")
            .matcher(allocation.group(1));
        java.util.List<String> fields = new java.util.ArrayList<>();
        while (field.find()) {
            fields.add(field.group(1));
        }
        java.util.Collections.sort(fields);
        return String.join(",", fields);
    }

    /**
     * A field may share its name with a method, and Lua puts both in one table, so the field is
     * renamed around the method. That renaming is decided per class from that class's own methods,
     * and pruning can leave a specialised class holding a different set than the class it was copied
     * from - so the two can be renamed differently. The accesses still name the original's field, so
     * the copy has to end up with the same key or the allocation writes one nothing reads.
     */
    private static String[] fieldNamedLikeAMethod() {
        String[] lines = fastHashMap();
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i]
                .replace("private int count = 0", "private int size = 0")
                .replace("count++", "size++")
                .replace("count--", "size--")
                .replace("function size() returns int", "function size() returns int")
                .replace("        return count", "        return size");
        }
        return lines;
    }

    @Test
    public void aFieldNamedLikeAMethodKeepsOneKeyAcrossSpecialisation() throws IOException {
        test().testLua(true).executeProg()
            .lines(program(fieldNamedLikeAMethod(), INT_INSTANCE, USE_WITH_COLLISION));
        assertSpecialisedClassesAllocateTheirFields(
            compiledLua("aFieldNamedLikeAMethodKeepsOneKeyAcrossSpecialisation"));
    }

    /**
     * Every dispatch slot on a specialised class names one of its methods.
     * <p>
     * A slot's name is composed from the owner's and the segment after the last underscore of the
     * method's, and for a specialised method that segment is the type argument - so every method of
     * one specialisation used to compose the same name and the first bound claimed it. Nothing called
     * it, which is why it went unnoticed; it is not emitted now, and this says so rather than leaving
     * the next reader to wonder what it was.
     * <p>
     * A method and its overrides do share a slot, which is dispatch rather than a collision. They
     * share a declared name, which is what tells the two cases apart.
     */
    @Test
    public void everySlotOnASpecialisedClassNamesAMethod() throws IOException {
        test().testLua(true).executeProg().lines(program(fastHashMap(), INT_INSTANCE, USE_WITH_COLLISION));
        String lua = compiledLua("everySlotOnASpecialisedClassNamesAMethod");

        Matcher table = Pattern.compile("(FastHashMap_specialized\\w*)\\.(\\w+)\\s*=").matcher(lua);
        java.util.List<String> unnamed = new java.util.ArrayList<>();
        while (table.find()) {
            String slot = table.group(2);
            if (slot.startsWith("__")) {
                continue;
            }
            // A real slot carries a method name; the junk one was the class name and the type
            // argument with no method name anywhere in it.
            boolean namesAMethod = false;
            for (String method : METHOD_NAMES) {
                if (slot.contains(method)) {
                    namesAMethod = true;
                    break;
                }
            }
            if (!namesAMethod) {
                unnamed.add(slot);
            }
        }
        if (!unnamed.isEmpty()) {
            throw new AssertionError("these slots name no method: " + unnamed + "\n" + lua);
        }
    }

    private String compiledJass(String testName) throws IOException {
        return Files.toString(new File(TEST_OUTPUT_PATH, "FastHashMapTests_" + testName + ".j"), Charsets.UTF_8);
    }

    private static void assertMatches(String text, String regex, String what) {
        if (!Pattern.compile(regex).matcher(text).find()) {
            throw new AssertionError("expected " + what + " (/" + regex + "/) in:\n" + text);
        }
    }
}
