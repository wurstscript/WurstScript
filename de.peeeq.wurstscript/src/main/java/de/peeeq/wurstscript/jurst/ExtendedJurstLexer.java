package de.peeeq.wurstscript.jurst;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.jurst.antlr.JurstLexer;
import de.peeeq.wurstscript.jurst.antlr.JurstParser;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

public class ExtendedJurstLexer implements TokenSource {

    private final JurstLexer orig;
    private final Queue<Token> nextTokens = new ArrayDeque<>();
    private State state = State.INIT;
    private @Nullable Token eof = null;
    private @Nullable Token firstNewline;
    private final LineOffsets lineOffsets = new LineOffsets();
    public StringBuilder debugSb = new StringBuilder();
    private final boolean debug = false;
    private final Pair<TokenSource, CharStream> sourcePair;
    private boolean isWurst = false;


    enum State {
        INIT, WRAP_CHAR, NEWLINES, BEGIN_LINE
    }


    public ExtendedJurstLexer(CharStream input) {
        orig = new JurstLexer(input);
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
            return makeToken(JurstParser.EOF, "$EOF", eof_local.getStartIndex(), eof_local.getStopIndex());
        }


        for (; ; ) {
            Token token = orig.nextToken();

            if (debug) WLogger.info("orig token = " + token);

            if (token == null) {
                continue;
            }

            if (isWurst) {
                if (token.getType() == JurstParser.ENDPACKAGE
                        || token.getType() == JurstParser.ENDLIBRARY
                        || token.getType() == JurstParser.ENDSCOPE) {
                    isWurst = false;
                }
            } else {
                if (token.getType() == JurstParser.PACKAGE
                        || token.getType() == JurstParser.LIBRARY
                        || token.getType() == JurstParser.SCOPE) {
                    isWurst = true;
                } else if (isWurstOnlyKeyword(token)) {
                    token = makeToken(JurstParser.ID, token.getText(), token.getStartIndex(), token.getStopIndex());
                    assert token != null;
                } else if (token.getType() == JurstParser.HOTDOC_COMMENT) {
                    continue;
                }
            }


            if (token.getType() == JurstParser.NL) {
                lineOffsets.set(token.getLine(), token.getStopIndex());
            }

            if (token.getType() == JurstParser.EOF) {
                // at EOF close all blocks and return an extra newline
                eof = token;
                if (isWurst) {
                    // if inside wurst, add a closing 'endpackage' and a newline
                    nextTokens.add(makeToken(JurstParser.ENDPACKAGE, "endpackage", token.getStartIndex(), token.getStopIndex()));
                    nextTokens.add(makeToken(JurstParser.NL, "$NL", token.getStartIndex(), token.getStopIndex()));
                }
                // add a single newline
                return makeToken(JurstParser.NL, "$NL", token.getStartIndex(), token.getStopIndex());
            }

            if (token.getType() == JurstParser.ID
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
                    } else if (token.getType() == JurstParser.NL) {
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
                    } else if (token.getType() == JurstParser.NL) {
                        continue;
                    } else {
                        nextTokens.add(token);
                        state(State.INIT);
                        return firstNewline;
                    }
                case WRAP_CHAR:
                    if (isWrapCharEndLine(token.getType())) {
                        return token;
                    } else if (token.getType() == JurstParser.NL) {
                        // ignore newlines after wrap char
                        continue;
                    } else {
                        state(State.INIT);
                        return token;
                    }
                case BEGIN_LINE:
                    if (token.getType() == JurstParser.NL) {
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


    private boolean isWurstOnlyKeyword(Token token) {
        switch (token.getType()) {
            case JurstParser.VAR:
            case JurstParser.LET:
                // TODO other tokens
                return true;
            default:
                return false;
        }
    }


    private void state(State s) {
        if (debug) WLogger.info("state " + state + " -> " + s);
        state = s;
    }


    private boolean isWrapChar(int type) {
        switch (type) {
//		case JurstParser.PAREN_LEFT:
//		case JurstParser.BRACKET_LEFT:
            case JurstParser.COMMA:
            case JurstParser.PLUS:
            case JurstParser.MULT:
            case JurstParser.MINUS:
            case JurstParser.DIV:
            case JurstParser.DIV_REAL:
            case JurstParser.MOD:
            case JurstParser.MOD_REAL:
            case JurstParser.AND:
            case JurstParser.OR:

                return true;
        }
        return false;
    }


    private boolean isWrapCharEndLine(int type) {
        switch (type) {
            case JurstParser.PAREN_LEFT:
            case JurstParser.BRACKET_LEFT:
                return true;
            default:
                return isWrapChar(type);
        }
    }


    private boolean isWrapCharBeginLine(int type) {
        switch (type) {
            case JurstParser.PAREN_RIGHT:
            case JurstParser.BRACKET_RIGHT:
            case JurstParser.DOT:
            case JurstParser.DOTDOT:
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
