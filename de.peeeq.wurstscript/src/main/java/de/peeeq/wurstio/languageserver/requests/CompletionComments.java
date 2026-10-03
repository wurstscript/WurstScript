package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstscript.antlr.JassLexer;
import de.peeeq.wurstscript.antlr.WurstLexer;
import de.peeeq.wurstscript.jurst.antlr.JurstLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import java.util.List;

/**
 * Decides whether the end of a text is inside a comment by running the language's own lexer over it.
 * <p>
 * What counts as a comment depends on string, escape and rawcode rules, and on how the lexer recovers from
 * a malformed literal (it reports the error and resumes after consuming one more character, which can be the
 * first slash of a comment opener). A hand-written scanner has to re-derive all of that and drifts from the
 * grammar one case at a time, so the lexer decides instead.
 */
public final class CompletionComments {
    public enum Syntax {
        WURST, JASS, JURST;

        public static Syntax ofFile(String fileName) {
            if (fileName.endsWith(".j")) {
                return JASS;
            }
            if (fileName.endsWith(".jurst")) {
                return JURST;
            }
            return WURST;
        }
    }

    private CompletionComments() {
    }

    /**
     * Whether the lexer, run over exactly {@code text}, is inside a comment at the end of it.
     * <ul>
     * <li>A line comment is one token running to the end of the text. Wurst emits it on a hidden channel; Jass
     * and Jurst skip it, so the skipped token is observed instead.</li>
     * <li>A block comment with no closing delimiter yields no token at all: the lexer falls back to a "/"
     * immediately followed by a token starting with "*".</li>
     * </ul>
     */
    public static boolean endsInsideComment(String text, Syntax syntax) {
        int length = text.length();
        boolean[] skippedLineComment = {false};
        Lexer lexer = newLexer(text, syntax, (tokenText, end) -> {
            if (tokenText.startsWith("//") && end == length) {
                skippedLineComment[0] = true;
            }
        });
        lexer.removeErrorListeners();
        List<? extends Token> tokens = lexer.getAllTokens();
        if (skippedLineComment[0]) {
            return true;
        }
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (syntax == Syntax.WURST && token.getType() == WurstLexer.LINE_COMMENT
                && token.getStopIndex() + 1 == length) {
                return true;
            }
            if (token.getText().equals("/") && i + 1 < tokens.size()) {
                Token next = tokens.get(i + 1);
                if (next.getText().startsWith("*") && next.getStartIndex() == token.getStopIndex() + 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private interface SkipObserver {
        void skipped(String tokenText, int endIndex);
    }

    private static Lexer newLexer(String text, Syntax syntax, SkipObserver observer) {
        switch (syntax) {
            case JASS:
                return new JassLexer(CharStreams.fromString(text)) {
                    @Override
                    public void skip() {
                        observer.skipped(getText(), _input.index());
                        super.skip();
                    }
                };
            case JURST:
                return new JurstLexer(CharStreams.fromString(text)) {
                    @Override
                    public void skip() {
                        observer.skipped(getText(), _input.index());
                        super.skip();
                    }
                };
            default:
                return new WurstLexer(CharStreams.fromString(text));
        }
    }
}
