package tests.wurstscript.tests;

import org.testng.annotations.Test;

/**
 * The standard library's own string handling, run against the interpreter.
 * <p>
 * {@code String.wurst} enables multibyte support by default and works it out at runtime rather than
 * being told: it slices a character in half to see how the engine represents a partial slice, and
 * slices a literal byte by byte to enumerate every continuation byte. All of that rests on a string
 * being a sequence of bytes, so these check the library reaches the answers it is meant to reach
 * rather than quietly falling back to its ascii-only path.
 * <p>
 * Nothing else runs the library's own tests, so a bump of the pinned version is otherwise only
 * checked for still compiling.
 */
public class StdLibStringTests extends WurstScriptTest {

    /** Two bytes for the character, and the library counts what the game counts. */
    @Test
    public void lengthOfAMultibyteStringIsInBytes() {
        test().withStdLib().executeProg().lines(
            "package test",
            "import String",
            "init",
            "    if \"ä\".length() == 2 and \"aä\".length() == 3",
            "        testSuccess()"
        );
    }

    /**
     * The position between the two bytes of a character is not a boundary, and both ends are. This is
     * the library's detection working end to end: it only answers this way if slicing produced the
     * partial bytes it expected to find.
     */
    @Test
    public void aPositionInsideACharacterIsNotABoundary() {
        test().withStdLib().executeProg().lines(
            "package test",
            "import String",
            "init",
            "    let s = \"ä\"",
            "    if s.isCharBoundary(0) and s.isCharBoundary(2) and not s.isCharBoundary(1)",
            "        testSuccess()"
        );
    }

    /** Ascii is unaffected: every position in it starts a character. */
    @Test
    public void everyPositionInAnAsciiStringIsABoundary() {
        test().withStdLib().executeProg().lines(
            "package test",
            "import String",
            "init",
            "    let s = \"abc\"",
            "    if s.isCharBoundary(0) and s.isCharBoundary(1) and s.isCharBoundary(2)",
            "        testSuccess()"
        );
    }
}
