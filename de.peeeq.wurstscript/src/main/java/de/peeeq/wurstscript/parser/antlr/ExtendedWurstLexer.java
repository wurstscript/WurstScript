package de.peeeq.wurstscript.parser.antlr;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.antlr.WurstLexer;
import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

public class ExtendedWurstLexer implements TokenSource {

    private final WurstLexer orig;
    private Queue<Token> nextTokens = new LinkedList<>();
    private State state = State.INIT;
    private Stack<Integer> indentationLevels = new Stack<>();
    private @Nullable Token eof = null;
    private Token firstNewline;
    private int numberOfTabs;
    private LineOffsets lineOffsets = new LineOffsets();
    private final boolean debug = false;
    private Pair<TokenSource, CharStream> sourcePair;
    private boolean isWurst = false;
    private boolean lastCharWasWrap = false;
    private @Nullable Token lastToken = null;
    // which character is used for indentation
    private TabChoice tabChoice = TabChoice.Unknown;
    private CompileError tabWarning = null;
    private Deque<Token> commentTokens = new ArrayDeque<>();

    public Deque<Token> getCommentTokens() {
        return commentTokens;
    }


    enum State {
        INIT, NEWLINES, BEGIN_LINE
    }

    enum TabChoice {
        Unknown(0),
        Spaces(WurstParser.SPACETAB),
        Tabs(WurstParser.TAB);

        private int symbol;

        TabChoice(int symbol) {

            this.symbol = symbol;
        }

        public static TabChoice from(Token token) {
            if (token.getType() == WurstParser.SPACETAB) {
                return Spaces;
            } else {
                return Tabs;
            }
        }

        public boolean isConsistentWith(Token token) {
            return symbol == token.getType();
        }
    }


    public ExtendedWurstLexer(CharStream input) {
        orig = new WurstLexer(input);
        sourcePair = new Pair<>(orig, input);
        indentationLevels.push(0);
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
        lastToken = t;

        if (debug) WLogger.trace("		new token: " + t);
        return t;
    }


    private Token nextTokenIntern() {
        if (!nextTokens.isEmpty()) {
            return nextTokens.poll();
        }

        Token l_eof = eof;
        if (l_eof != null) {
            return makeToken(WurstParser.EOF, "$EOF", l_eof.getStartIndex(), l_eof.getStopIndex());
        }


        for (; ; ) {
            Token token = orig.nextToken();

            if (debug) WLogger.info("orig token = " + token);

            if (token == null) {
                continue;
            }

            if (token.getChannel() == 2) {
                commentTokens.addLast(token);
                continue;
            }

            if (isWurst) {
                if (isJassOnlyKeyword(token)) {
                    token = makeToken(WurstParser.ID, token.getText(), token.getStartIndex(), token.getStopIndex());
                    assert token != null;
                } else if (token.getType() == WurstParser.ENDPACKAGE) {
                    handleIndent(0, token, token.getStartIndex(), token.getStopIndex());
                    isWurst = false;
                }
            } else {
                if (token.getType() == WurstParser.PACKAGE) {
                    isWurst = true;
                } else if (isWurstOnlyKeyword(token)) {
                    token = makeToken(WurstParser.ID, token.getText(), token.getStartIndex(), token.getStopIndex());
                    assert token != null;
                } else if (token.getType() == WurstParser.HOTDOC_COMMENT) {
                    continue;
                }
            }


            if (token.getType() == WurstParser.NL) {
                int line = 0;
                for (int i = 0; i < token.getText().length(); i++) {
                    char c = token.getText().charAt(i);
                    if (c == '\n') {
                        lineOffsets.set(token.getLine() + line, token.getStartIndex()+i);
                        line++;
                    }
                }
            }

            if (token.getType() == WurstParser.EOF) {
                // at EOF close all blocks and return an extra newline
                handleIndent(0, token, token.getStartIndex(), token.getStopIndex());
                eof = token;
                if (isWurst) {
                    // if inside wurst, add a closing 'endpackage' and a newline
                    nextTokens.add(makeToken(WurstParser.ENDPACKAGE, "endpackage", token.getStartIndex(), token.getStopIndex()));
                    nextTokens.add(makeToken(WurstParser.NL, "$NL", token.getStartIndex(), token.getStopIndex()));
                }

                lineOffsets.set(token.getLine(), token.getStopIndex()+1);
                // add a single newline
                return makeToken(WurstParser.NL, "$NL", token.getStartIndex(), token.getStopIndex());
            }


            switch (state) {
                case INIT:
                    if (token.getType() == WurstParser.NL) {
                        if (lastCharWasWrap) {
                            // ignore the newline following a wrap-character
                            continue;
                        } else {
                            firstNewline = token;
                            state(State.NEWLINES);
                            continue;
                        }
                    } else if (isTab(token)) {
                        continue;
                    }
                    lastCharWasWrap = isWrapCharEndLine(token.getType());
                    return token;
                case NEWLINES:
                    if (isWrapCharBeginLine(token.getType())) {
                        // ignore all the newlines when a wrap char comes after newlines
                        lastCharWasWrap = isWrapChar(token.getType());
                        state(State.INIT);
                        return token;
                    } else if (token.getType() == WurstParser.NL) {
                        continue;
                    } else if (isTab(token)) {
                        state(State.BEGIN_LINE);
                        readTabChar(token);
                        numberOfTabs = 1;
                        continue;
                    } else {
                        // no tabs after newline
                        handleIndent(0, token, token.getStartIndex(), token.getStopIndex());
                        nextTokens.add(token);
                        state(State.INIT);
                        return firstNewline;
                    }
//			case WRAP_CHAR:
//				if (isWrapCharEndLine(token.getType())) {
//					return token;
//				} else if (token.getType() == WurstParser.NL) {
//					firstNewline = token;
//					numberOfTabs = 0;
//					continue;
//				} else if (token.getType() == WurstParser.TAB) {
//					numberOfTabs++;
//					continue;
//				} else {
//					state(State.INIT);
//					if (numberOfTabs <= indentationLevels.peek()) {
//						// when the number of tabs decreases we ignore wrap chars
//						handleIndent(numberOfTabs, token.getStartIndex(), token.getStopIndex());
//						nextTokens.add(token);
//						return firstNewline;
//					} else {
//						return token;
//					}
//				}
                case BEGIN_LINE:
                    if (isTab(token)) {
                        readTabChar(token);
                        numberOfTabs++;
                    } else if (token.getType() == WurstParser.NL) {
                        state(State.NEWLINES);
                    } else if (isWrapCharBeginLine(token.getType())) {
                        // ignore all the newlines when a wrap char comes after newlines
                        lastCharWasWrap = isWrapChar(token.getType());
                        state(State.INIT);
                        return token;
                    } else {
                        if (lastCharWasWrap && numberOfTabs > indentationLevels.peek()) {
                            // ignore the newline, only return the token
                            state(State.INIT);
                            return token;
                        } else {
                            handleIndent(numberOfTabs, token, token.getStartIndex(), token.getStopIndex());
                            state(State.INIT);
                            nextTokens.add(token);
                            return firstNewline;
                        }
                    }
            }
        }
    }

    private void readTabChar(Token token) {
        if (tabChoice == TabChoice.Unknown) {
            tabChoice = TabChoice.from(token);
        } else if (tabWarning == null && !tabChoice.isConsistentWith(token)) {
            tabWarning = new CompileError(new WPos("", lineOffsets, token.getStartIndex(), token.getStopIndex()), "Mixing tabs and spaces for indentation.");
        }
    }

    private boolean isTab(Token token) {
        return token.getType() == WurstParser.TAB
                || token.getType() == WurstParser.SPACETAB;
    }


    private boolean isWurstOnlyKeyword(Token token) {
        switch (token.getType()) {
            case WurstParser.VAR:
            case WurstParser.LET:
                // TODO other tokens
                return true;
            default:
                return false;
        }
    }


    private boolean isJassOnlyKeyword(Token token) {
        switch (token.getType()) {
            case WurstParser.CALL:
            case WurstParser.SET:
            case WurstParser.JASS_LOCAL:
            case WurstParser.TAKES:
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


    private void handleIndent(int n, Token token, int start, int stop) {
        if (!isWurst) {
            return;
        }
//        Token t = lastToken;
//        if (t != null) {
//            start = t.getStopIndex();
//            stop = t.getStopIndex();
//        }
        if (debug) WLogger.info("handleIndent " + n + "	 " + indentationLevels);
        if (n > indentationLevels.peek()) {
            indentationLevels.push(n);
            nextTokens.add(makeToken(WurstParser.STARTBLOCK, "$begin", start, stop));
        } else {
            while (n < indentationLevels.peek()) {
                indentationLevels.pop();
                nextTokens.add(makeToken(WurstParser.ENDBLOCK, "$end", start, stop));
            }
            Integer expectedIndentation = indentationLevels.peek();
            if (n != expectedIndentation) {
                // all lines must be indented on the same level,
                // with the exception of the 'end' keyword, which can be on a different line
                if (token.getType() != WurstParser.END) {
                    int line = lineOffsets.getLine(start);
                    int charPositionInLine = start - lineOffsets.get(line);
                    String msg = "Invalid indentation level. Current indentation is " + expectedIndentation + ", but this is indented by " + n + ".";
                    reportError(line, charPositionInLine, msg);
                }
            }
        }
    }

    private void reportError(int line, int charPositionInLine, String msg) {
        for (ANTLRErrorListener el : orig.getErrorListeners()) {
            el.syntaxError(orig, "", line, charPositionInLine, msg, null);
        }
    }


    private boolean isWrapChar(int type) {
        switch (type) {
//		case WurstParser.PAREN_LEFT: 
//		case WurstParser.BRACKET_LEFT:
            case WurstParser.COMMA:
            case WurstParser.PLUS:
            case WurstParser.MULT:
            case WurstParser.MINUS:
            case WurstParser.DIV:
            case WurstParser.DIV_REAL:
            case WurstParser.MOD:
            case WurstParser.MOD_REAL:
            case WurstParser.AND:
            case WurstParser.OR:
//            case WurstParser.ARROW:
            case WurstParser.QUESTION:
            case WurstParser.COLON:
                return true;
        }
        return false;
    }


    private boolean isWrapCharEndLine(int type) {
        switch (type) {
            case WurstParser.PAREN_LEFT:
            case WurstParser.BRACKET_LEFT:
                return true;
            default:
                return isWrapChar(type);
        }
    }


    private boolean isWrapCharBeginLine(int type) {
        switch (type) {
            case WurstParser.PAREN_RIGHT:
            case WurstParser.BRACKET_RIGHT:
            case WurstParser.DOT:
            case WurstParser.DOTDOT:
            case WurstParser.BEGIN:
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
    public void setTokenFactory(TokenFactory<?> factory) {
        orig.setTokenFactory(factory);
    }


    public LineOffsets getLineOffsets() {
        return lineOffsets;
    }


    public void setErrorListener(ANTLRErrorListener listener) {
        orig.removeErrorListeners();
        orig.addErrorListener(listener);
    }

    public CompileError getTabWarning() {
        return tabWarning;
    }
}
