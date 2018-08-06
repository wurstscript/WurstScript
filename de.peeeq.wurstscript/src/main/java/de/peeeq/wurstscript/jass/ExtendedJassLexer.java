package de.peeeq.wurstscript.jass;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.antlr.JassLexer;
import de.peeeq.wurstscript.antlr.JassParser;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.jdt.annotation.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class ExtendedJassLexer implements TokenSource {

    private final JassLexer orig;
    private Queue<Token> nextTokens = new LinkedList<>();
    private State state = State.INIT;
    private @Nullable Token eof = null;
    private @Nullable Token firstNewline;
    private LineOffsets lineOffsets = new LineOffsets();
    public StringBuilder debugSb = new StringBuilder();
    private final boolean debug = false;
    private Pair<TokenSource, CharStream> sourcePair;

    enum State {
        INIT, WRAP_CHAR, NEWLINES, BEGIN_LINE
    }


    public ExtendedJassLexer(CharStream input) {
        orig = new JassLexer(input);
        sourcePair = new Pair<>(orig, input);
    }


    @Override
    public int getCharPositionInLine() {
        return orig.getCharPositionInLine();
    }

    @Override
    public CharStream getInputStream() {
        return orig.getInputStream();
    }

    @Override
    public int getLine() {
        return orig.getLine();
    }

    @Override
    public String getSourceName() {
        return orig.getSourceName();
    }

    @Override
    public TokenFactory<?> getTokenFactory() {
        return orig.getTokenFactory();
    }

    @Override
    public Token nextToken() {
        Token t = nextTokenIntern();

        debugSb.append(t.getText()).append(" ");
        if (debug) WLogger.info("		new token: " + t);
        return t;
    }


    private Token nextTokenIntern() {
        if (!nextTokens.isEmpty()) {
            return nextTokens.poll();
        }

        Token eof_local = eof;
        if (eof_local != null) {
            return makeToken(JassParser.EOF, "$EOF", eof_local.getStartIndex(), eof_local.getStopIndex());
        }


        for (; ; ) {
            Token token = orig.nextToken();

            if (debug) WLogger.info("orig token = " + token);

            if (token == null) {
                continue;
            }

            if (token.getType() == JassParser.NL) {
                lineOffsets.set(token.getLine(), token.getStopIndex());
            }

            if (token.getType() == JassParser.EOF) {
                // at EOF close all blocks and return an extra newline
                eof = token;
                // add a single newline
                return makeToken(JassParser.NL, "$NL", token.getStartIndex(), token.getStopIndex());
            }

            if (token.getType() == JassParser.ID
                    && token.getText().equals("debug")
                    && (state == State.NEWLINES)) {
                // ignore 'debug' at beginning of line
                continue;
            }


            switch (state) {
                case INIT:
                    if (isWrapCharEndLine(token.getType())) {
                        state(State.WRAP_CHAR);
                        return token;
                    } else if (token.getType() == JassParser.NL) {
                        firstNewline = token;
                        state(State.NEWLINES);
                        continue;
                    }
                    return token;
                case NEWLINES:
                    if (isWrapCharBeginLine(token.getType())) {
                        // ignore all the newlines when a wrap char comes after newlines
                        state(State.WRAP_CHAR);
                        return token;
                    } else if (token.getType() == JassParser.NL) {
                        continue;
                    } else {
                        nextTokens.add(token);
                        state(State.INIT);
                        return firstNewline;
                    }
                case WRAP_CHAR:
                    if (isWrapCharEndLine(token.getType())) {
                        return token;
                    } else if (token.getType() == JassParser.NL) {
                        // ignore newlines after wrap char
                        continue;
                    } else {
                        state(State.INIT);
                        return token;
                    }
                case BEGIN_LINE:
                    if (token.getType() == JassParser.NL) {
                        state(State.NEWLINES);
                    } else if (isWrapCharBeginLine(token.getType())) {
                        // ignore all the newlines when a wrap char comes after newlines
                        state(State.WRAP_CHAR);
                        return token;
                    } else {
                        state(State.INIT);
                        nextTokens.add(token);
                        return firstNewline;
                    }
            }
        }
    }


    private void state(State s) {
        if (debug) WLogger.info("state " + state + " -> " + s);
        state = s;
    }


    private boolean isWrapChar(int type) {
        switch (type) {
            case JassParser.COMMA:
            case JassParser.PLUS:
            case JassParser.MULT:
            case JassParser.MINUS:
            case JassParser.DIV_REAL:
            case JassParser.MOD_REAL:
            case JassParser.AND:
            case JassParser.OR:
                return true;
        }
        return false;
    }


    private boolean isWrapCharEndLine(int type) {
        switch (type) {
            case JassParser.PAREN_LEFT:
            case JassParser.BRACKET_LEFT:
                return true;
            default:
                return isWrapChar(type);
        }
    }


    private boolean isWrapCharBeginLine(int type) {
        switch (type) {
            case JassParser.PAREN_RIGHT:
            case JassParser.BRACKET_RIGHT:
                return true;
            default:
                return isWrapChar(type);
        }
    }


    private Token makeToken(int type, String text, int start, int stop) {
        Pair<TokenSource, CharStream> source = sourcePair;
        int channel = 0;
        CommonToken t = new CommonToken(source, type, channel, start, stop);
        return t;
    }

    @Override
    public void setTokenFactory(@SuppressWarnings("null") TokenFactory<?> factory) {
        orig.setTokenFactory(factory);
    }


    public LineOffsets getLineOffsets() {
        return lineOffsets;
    }


    public void addErrorListener(ANTLRErrorListener listener) {
        orig.addErrorListener(listener);
    }
}
