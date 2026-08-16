package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * Warcraft III treats a string as a sequence of bytes: {@code StringLength} counts bytes and
 * {@code SubString} takes byte offsets, so a slice can cut a multibyte character in half. Lua agrees,
 * its strings being byte arrays. The interpreter holds a Java string and counts UTF-16 code units,
 * which is the same answer only for ascii.
 * <p>
 * The standard library depends on the difference rather than avoiding it: {@code String.wurst} cuts a
 * character in half on purpose to find out how the engine represents a partial slice, and slices a
 * 64 character literal byte by byte to enumerate every continuation byte. Under UTF-16 that detection
 * quietly concludes the engine has no multibyte characters, so anything computed at compiletime -
 * object editor text, chunked data - is built from lengths the game will not agree with.
 */
public class StringByteSemanticsTests extends WurstScriptTest {

    private static String[] program(String... body) {
        String[] head = {
            "package test",
            "native testSuccess()",
            "@extern native StringLength(string s) returns int",
            "@extern native SubString(string s, int start, int stop) returns string",
            "@extern native StringHash(string s) returns int",
        };
        String[] all = new String[head.length + body.length];
        System.arraycopy(head, 0, all, 0, head.length);
        System.arraycopy(body, 0, all, head.length, body.length);
        return all;
    }

    /**
     * Written as an escape rather than as itself, so what reaches the compiler does not depend on
     * the encoding javac happens to read this file with.
     */
    private static final String A_UMLAUT = "ä";

    /** Two bytes in UTF-8, and the game counts bytes. */
    private static final String[] LENGTH_OF_A_TWO_BYTE_CHARACTER = program(
        "init",
        "    if StringLength(\"" + A_UMLAUT + "\") == 2",
        "        testSuccess()"
    );

    @Test
    public void lengthCountsBytes() {
        testAssertOkLines(true, LENGTH_OF_A_TWO_BYTE_CHARACTER);
    }

    @Test
    public void lengthCountsBytesLua() {
        test().testLua(true).executeProg().lines(LENGTH_OF_A_TWO_BYTE_CHARACTER);
    }

    /** A slice may stop between the bytes of one character, which is how the stdlib probes. */
    private static final String[] SLICING_A_CHARACTER_IN_HALF = program(
        "init",
        "    let half = SubString(\"ä\", 0, 1)",
        "    if StringLength(half) == 1 and half != \"ä\"",
        "        testSuccess()"
    );

    @Test
    public void aSliceCanCutACharacterInHalf() {
        testAssertOkLines(true, SLICING_A_CHARACTER_IN_HALF);
    }

    @Test
    public void aSliceCanCutACharacterInHalfLua() {
        test().testLua(true).executeProg().lines(SLICING_A_CHARACTER_IN_HALF);
    }

    /** The halves are the bytes of the whole, so putting them back gives it back. */
    private static final String[] HALVES_REJOIN = program(
        "init",
        "    let s = \"äö\"",
        "    if SubString(s, 0, 2) + SubString(s, 2, 4) == s and StringLength(s) == 4",
        "        testSuccess()"
    );

    @Test
    public void slicesRejoinIntoTheOriginal() {
        testAssertOkLines(true, HALVES_REJOIN);
    }

    @Test
    public void slicesRejoinIntoTheOriginalLua() {
        test().testLua(true).executeProg().lines(HALVES_REJOIN);
    }

    /**
     * Whole characters cross from compiletime into the generated script and keep their length, so the
     * value the interpreter computed is the value the program runs with.
     */
    @Test
    public void aCompiletimeStringKeepsItsLengthAfterTransforms() {
        test().executeProg().runCompiletimeFunctions(true).lines(program(
            "function compiletime(string s) returns string",
            "    return s",
            "constant string GREETING = compiletime(\"h" + A_UMLAUT + "llo\")",
            "init",
            "    if StringLength(GREETING) == 6 and GREETING == \"h" + A_UMLAUT + "llo\"",
            "        testSuccess()"
        ));
    }

    /**
     * Half a character cannot. It has to become a literal in a script written as UTF-8, and neither
     * Jass nor the escaping has a way to write a byte down numerically, so it would go in as the
     * replacement character and come back out three bytes long rather than one. Refused instead, and
     * this pins that it is refused rather than silently carried across at a different length.
     */
    @Test
    public void aCompiletimeStringHoldingHalfACharacterIsRefused() {
        test().executeProg().runCompiletimeFunctions(true)
            .expectError("part of a multibyte character")
            .lines(program(
                "function compiletime(string s) returns string",
                "    return s",
                "constant string HALF = compiletime(SubString(\"" + A_UMLAUT + "\", 0, 1))",
                "init",
                "    if StringLength(HALF) == 1",
                "        testSuccess()"
            ));
    }
}
