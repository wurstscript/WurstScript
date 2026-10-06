package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * The state of a hashtable built by compiletime functions is replayed into the runtime program.
 * These tests pin the interpreter-side semantics which that replay depends on.
 */
public class CompiletimeHashtableTests extends WurstScriptTest {

    private static final String[] PRELUDE = {
        "type agent extends handle",
        "type hashtable extends agent",
        "package Test",
        "native testSuccess()",
        "@extern native InitHashtable() returns hashtable",
        "@extern native SaveInteger(hashtable h, int p, int c, int i)",
        "@extern native LoadInteger(hashtable h, int p, int c) returns int",
        "@extern native SaveReal(hashtable h, int p, int c, real r)",
        "@extern native LoadReal(hashtable h, int p, int c) returns real",
        "@extern native SaveStr(hashtable h, int p, int c, string s)",
        "@extern native LoadStr(hashtable h, int p, int c) returns string",
        "@extern native FlushChildHashtable(hashtable h, int p)",
        "@extern native FlushParentHashtable(hashtable h)",
        "@extern native RemoveSavedInteger(hashtable h, int p, int c)",
        "@extern native RemoveSavedReal(hashtable h, int p, int c)",
        "@extern native RemoveSavedString(hashtable h, int p, int c)",
        "@extern native HaveSavedInteger(hashtable h, int p, int c) returns bool",
        "function compiletime(hashtable h) returns hashtable",
        "    return h",
        "let h = compiletime(InitHashtable())",
    };

    private static String[] program(String... rest) {
        String[] result = new String[PRELUDE.length + rest.length];
        System.arraycopy(PRELUDE, 0, result, 0, PRELUDE.length);
        System.arraycopy(rest, 0, result, PRELUDE.length, rest.length);
        return result;
    }

    @Test
    public void valuesOfDifferentTypesShareAKeyAndAreReplayed() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true).lines(program(
            "@compiletime",
            "function fill()",
            "    SaveInteger(h, 1, 1, 10)",
            "    SaveStr(h, 1, 1, \"s\")",
            "    SaveReal(h, 1, 1, 2.5)",
            "    SaveInteger(h, 1, 1, 11)",
            "init",
            "    if LoadInteger(h, 1, 1) == 11 and LoadStr(h, 1, 1) == \"s\" and LoadReal(h, 1, 1) == 2.5",
            "        testSuccess()"));
    }

    @Test
    public void flushedAndRemovedEntriesAreNotReplayed() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true).lines(program(
            "@compiletime",
            "function fill()",
            "    SaveInteger(h, 1, 1, 10)",
            "    SaveInteger(h, 2, 5, 20)",
            "    SaveInteger(h, 2, 6, 21)",
            "    SaveInteger(h, 3, 5, 30)",
            "    SaveInteger(h, 4, 4, 40)",
            "    FlushChildHashtable(h, 2)",
            "    RemoveSavedInteger(h, 3, 5)",
            "init",
            "    if LoadInteger(h, 1, 1) == 10 and LoadInteger(h, 4, 4) == 40",
            "        if LoadInteger(h, 2, 5) == 0 and LoadInteger(h, 2, 6) == 0 and LoadInteger(h, 3, 5) == 0",
            "            if not HaveSavedInteger(h, 2, 5) and not HaveSavedInteger(h, 3, 5) and HaveSavedInteger(h, 1, 1)",
            "                testSuccess()"));
    }

    @Test
    public void flushingTheWholeTableLeavesNothingToReplay() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true).lines(program(
            "@compiletime",
            "function fill()",
            "    SaveInteger(h, 1, 1, 10)",
            "    SaveInteger(h, 2, 2, 20)",
            "    FlushParentHashtable(h)",
            "    SaveInteger(h, 5, 5, 50)",
            "init",
            "    if LoadInteger(h, 1, 1) == 0 and LoadInteger(h, 2, 2) == 0 and LoadInteger(h, 5, 5) == 50",
            "        testSuccess()"));
    }

    @Test
    public void savingAnotherTypeKeepsTheOtherValuesOfTheSlot() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true).lines(program(
            "@compiletime",
            "function fill()",
            "    SaveInteger(h, 1, 1, 5)",
            "    SaveStr(h, 1, 1, \"s\")",
            "init",
            "    if LoadInteger(h, 1, 1) == 5 and LoadStr(h, 1, 1) == \"s\"",
            "        testSuccess()"));
    }

    @Test
    public void removingOneChildKeepsItsSiblings() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true).lines(program(
            "@compiletime",
            "function fill()",
            "    SaveInteger(h, 1, 1, 1)",
            "    SaveInteger(h, 1, 2, 2)",
            "    RemoveSavedInteger(h, 1, 1)",
            "init",
            "    if LoadInteger(h, 1, 1) == 0 and LoadInteger(h, 1, 2) == 2",
            "        testSuccess()"));
    }

    @Test
    public void removingTheStringLeavesTheIntegerOfTheSlot() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true).lines(program(
            "@compiletime",
            "function fill()",
            "    SaveInteger(h, 1, 1, 5)",
            "    SaveStr(h, 1, 1, \"s\")",
            "    RemoveSavedString(h, 1, 1)",
            "init",
            "    if LoadInteger(h, 1, 1) == 5 and LoadStr(h, 1, 1) == null",
            "        testSuccess()"));
    }

    /** An integer and a real of equal value are different slot entries and must not replace each other. */
    @Test
    public void numericallyEqualIntegerAndRealAreSeparateEntries() {
        test().executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true).lines(program(
            "@compiletime",
            "function fill()",
            "    SaveInteger(h, 1, 1, 3)",
            "    SaveReal(h, 1, 1, 3.0)",
            "    SaveReal(h, 1, 1, 5.0)",
            "    SaveInteger(h, 2, 2, 7)",
            "    SaveReal(h, 2, 2, 7.0)",
            "    RemoveSavedReal(h, 2, 2)",
            "init",
            "    if LoadInteger(h, 1, 1) == 3 and LoadReal(h, 1, 1) == 5.0",
            "        if LoadInteger(h, 2, 2) == 7 and LoadReal(h, 2, 2) == 0.0",
            "            testSuccess()"));
    }

    @Test
    public void valuesOfDifferentTypesShareAKeyAndAreReplayedInLua() {
        test().testLua(true).executeProg(true).executeProgOnlyAfterTransforms().runCompiletimeFunctions(true).lines(program(
            "@compiletime",
            "function fill()",
            "    SaveInteger(h, 1, 1, 10)",
            "    SaveStr(h, 1, 1, \"s\")",
            "    SaveReal(h, 1, 1, 2.5)",
            "    SaveInteger(h, 2, 5, 20)",
            "    FlushChildHashtable(h, 2)",
            "init",
            "    if LoadInteger(h, 1, 1) == 10 and LoadStr(h, 1, 1) == \"s\" and LoadReal(h, 1, 1) == 2.5",
            "        if LoadInteger(h, 2, 5) == 0",
            "            testSuccess()"));
    }
}
