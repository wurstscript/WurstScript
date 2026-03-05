package de.peeeq.wurstscript.parser;

import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.attributes.AttrWurstDoc;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

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

    public static TriviaIndex fromTokens(List<Token> tokens, Deque<Token> hiddenCommentTokens) {
        if ((tokens == null || tokens.isEmpty()) && (hiddenCommentTokens == null || hiddenCommentTokens.isEmpty())) {
            return EMPTY;
        }
        List<Token> allTokens = new ArrayList<>();
        if (tokens != null) {
            allTokens.addAll(tokens);
        }
        if (hiddenCommentTokens != null) {
            allTokens.addAll(hiddenCommentTokens);
        }
        allTokens.sort((a, b) -> {
            int byStart = Integer.compare(a.getStartIndex(), b.getStartIndex());
            if (byStart != 0) {
                return byStart;
            }
            return Integer.compare(a.getStopIndex(), b.getStopIndex());
        });
        return new TriviaIndex(allTokens);
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

    public Optional<CommentTrivia> findCommentAtOffset(int offset) {
        for (Token token : tokens) {
            if (token.getStartIndex() <= offset && offset <= token.getStopIndex() && isCommentToken(token.getType())) {
                return Optional.of(new CommentTrivia(token.getType(), token.getText()));
            }
        }
        return Optional.empty();
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

    private boolean isCommentToken(int type) {
        return type == WurstParser.HOTDOC_COMMENT
            || type == WurstParser.LINE_COMMENT
            || type == WurstParser.ML_COMMENT;
    }

    public static final class CommentTrivia {
        private final int tokenType;
        private final String rawText;

        public CommentTrivia(int tokenType, String rawText) {
            this.tokenType = tokenType;
            this.rawText = rawText;
        }

        public int getTokenType() {
            return tokenType;
        }

        public String getRawText() {
            return rawText;
        }
    }
}
