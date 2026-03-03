package de.peeeq.wurstscript.parser;

import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.attributes.AttrWurstDoc;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TriviaIndex {

    private static final TriviaIndex EMPTY = new TriviaIndex(Collections.emptyList());

    private final List<Token> tokens;

    private TriviaIndex(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static TriviaIndex empty() {
        return EMPTY;
    }

    public static TriviaIndex fromTokens(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return EMPTY;
        }
        return new TriviaIndex(new ArrayList<>(tokens));
    }

    public String findLeadingHotdoc(int declarationStartOffset) {
        int targetTokenIndex = firstNonTriviaTokenAtOrAfter(declarationStartOffset);
        if (targetTokenIndex < 0) {
            return "";
        }
        for (int i = targetTokenIndex - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            int type = token.getType();
            if (type == WurstParser.HOTDOC_COMMENT) {
                return AttrWurstDoc.normalizeHotdocComment(token.getText());
            }
            if (!isTriviaBetweenDocAndDeclaration(token)) {
                return "";
            }
        }
        return "";
    }

    private int firstNonTriviaTokenAtOrAfter(int offset) {
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getStartIndex() < offset) {
                continue;
            }
            if (!isTriviaBetweenDocAndDeclaration(token)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isTriviaBetweenDocAndDeclaration(Token token) {
        int type = token.getType();
        if (type == WurstParser.HOTDOC_COMMENT
            || type == WurstParser.LINE_COMMENT
            || type == WurstParser.ML_COMMENT
            || type == WurstParser.NL
            || type == WurstParser.TAB
            || type == WurstParser.SPACETAB) {
            return true;
        }
        return token.getChannel() != Token.DEFAULT_CHANNEL;
    }
}
