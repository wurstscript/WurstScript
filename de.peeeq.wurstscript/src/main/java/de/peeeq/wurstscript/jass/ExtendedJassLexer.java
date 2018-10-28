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
    private final boolean debug = false;
    private Pair<TokenSource, CharStream> sourcePair;

    enum State {
        INIT, NEWLINES, BEGIN_LINE
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

        if (debug) WLogger.info("		new token: " + t);
        return t;
    }


    private Token nextTokenIntern() {
        if (!nextTokens.isEmpty()) {
            return nextTokens.poll();
        }

        Token eof_local = eof;
        if (eof_local != null) {
            return makeToken(JassParser.EOF, eof_local.getStartIndex(), eof_local.getStopIndex());
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
                return makeToken(JassParser.NL, token.getStartIndex(), token.getStopIndex());
            }

            if (token.getType() == JassParser.ID
                    && token.getText().equals("debug")
                    && (state == State.NEWLINES)) {
                // ignore 'debug' at beginning of line
                continue;
            }


            switch (state) {
                case INIT:
                    if (token.getType() == JassParser.NL) {
                        firstNewline = token;
                        state(State.NEWLINES);
                        continue;
                    }
                    return token;
                case NEWLINES:
                    if (token.getType() == JassParser.NL) {
                        continue;
                    } else {
                        nextTokens.add(token);
                        state(State.INIT);
                        return firstNewline;
                    }
                case BEGIN_LINE:
                    if (token.getType() == JassParser.NL) {
                        state(State.NEWLINES);
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

    private Token makeToken(int type, int start, int stop) {
        Pair<TokenSource, CharStream> source = sourcePair;
        int channel = 0;
        return new CommonToken(source, type, channel, start, stop);
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
