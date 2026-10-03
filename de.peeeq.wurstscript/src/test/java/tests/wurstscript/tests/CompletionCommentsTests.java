package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.requests.CompletionComments;
import de.peeeq.wurstio.languageserver.requests.CompletionComments.Syntax;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Whether completion is inside a comment is decided by the language's lexers, including the way they recover
 * from a malformed literal, so these cases state what the lexers do rather than what looks plausible.
 */
public class CompletionCommentsTests {
    private static void assertInside(Syntax syntax, String text) {
        assertEquals(CompletionComments.endsInsideComment(text, syntax), true, syntax + ": " + escape(text));
    }

    private static void assertOutside(Syntax syntax, String text) {
        assertEquals(CompletionComments.endsInsideComment(text, syntax), false, syntax + ": " + escape(text));
    }

    private static String escape(String text) {
        return text.replace("\n", "\\n").replace("\r", "\\r");
    }

    @Test
    public void lineAndBlockComments() {
        for (Syntax syntax : Syntax.values()) {
            assertInside(syntax, "x = 1 // note");
            assertInside(syntax, "/* open");
            assertInside(syntax, "/** open doc");
            assertInside(syntax, "/* a */ /* open");
            assertOutside(syntax, "/* closed */");
            assertOutside(syntax, "/* closed */ x");
            assertOutside(syntax, "x // note\ny");
            assertOutside(syntax, "x // note\r\ny");
        }
    }

    @Test
    public void commentMarkersInsideLiteralsAreNotComments() {
        for (Syntax syntax : Syntax.values()) {
            assertOutside(syntax, "x = \"a // b\" + 1");
            assertOutside(syntax, "x = \"a /* b\" + 1");
            assertOutside(syntax, "x = '//' + 1");
            assertOutside(syntax, "x = \"a \\\" // b\" + 1");
        }
    }

    @Test
    public void wurstStringsEndAtTheLineEvenWhenUnterminated() {
        assertInside(Syntax.WURST, "x = \"abc\n// note");
        assertInside(Syntax.WURST, "x = \"abc\r\n// note");
        assertInside(Syntax.WURST, "x = \"abc\\\n// note");
        assertInside(Syntax.WURST, "x = \"abc\\\r\n/* note");
    }

    @Test
    public void jassAndJurstStringsMaySpanLines() {
        for (Syntax syntax : new Syntax[] {Syntax.JASS, Syntax.JURST}) {
            assertOutside(syntax, "x = \"abc\n// still the string");
            assertInside(syntax, "x = \"abc\n\" // note");
        }
    }

    @Test
    public void recoveryFromAnInvalidEscapeConsumesOneCharacter() {
        // The lexer reports the bad escape and resumes after one more character. That character is the first
        // slash here, so only a third slash (or "/*") after it still opens a comment.
        assertInside(Syntax.WURST, "x = \"\\/// note");
        assertInside(Syntax.WURST, "x = \"\\//* note");
        assertInside(Syntax.WURST, "x = \"\\q// note");
        assertOutside(Syntax.WURST, "x = \"\\// not a comment");
    }

    /** The lexers must never throw on the unfinished, half-typed text a completion request sees. */
    @Test
    public void neverThrowsOnMalformedText() {
        for (Syntax syntax : Syntax.values()) {
            for (int length = 0; length <= 5; length++) {
                forEachText(syntax, length, new char[length], 0);
            }
        }
    }

    private static final char[] TEXT_ALPHABET = {'"', '\'', '\\', '/', '*', '\n', '\r', 'n'};

    private static void forEachText(Syntax syntax, int length, char[] text, int index) {
        if (index == length) {
            CompletionComments.endsInsideComment(new String(text), syntax);
            return;
        }
        for (char c : TEXT_ALPHABET) {
            text[index] = c;
            forEachText(syntax, length, text, index + 1);
        }
    }
}
