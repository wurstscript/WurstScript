package de.peeeq.wurstscript.parser.antlr;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.antlr.WurstLexer;
import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.attributes.CompilationUnitInfo;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.jdt.annotation.Nullable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class ExtendedWurstLexer implements TokenSource {

    private final WurstLexer orig;
    private Queue<Token> nextTokens = new LinkedList<>();
    private State state = State.INIT;
    private Stack<Integer> indentationLevels = new Stack<>();
    private int spacesPerIndent = -1;
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
    // counts the number of open parentheses
    private int parenthesesLevel = 0;

    enum State {
        INIT, NEWLINES, BEGIN_LINE
    }

    enum TabChoice {
        Unknown,
        Spaces,
        Tabs;

        public static TabChoice from(Token token) {
            if (token.getType() == WurstParser.SPACETAB) {
                return Spaces;
            } else {
                return Tabs;
            }
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
            Token token1 = orig.nextToken();

            if (debug) WLogger.info("orig token = " + token1);

            if (token1 == null) {
                continue;
            }


            if (isWurst) {
                if (isJassOnlyKeyword(token1)) {
                    token1 = makeToken(WurstParser.ID, token1.getText(), token1.getStartIndex(), token1.getStopIndex());
                    assert token1 != null;
                } else if (token1.getType() == WurstParser.ENDPACKAGE) {
                    handleIndent(0, token1, token1.getStartIndex(), token1.getStopIndex(), token1);
                    isWurst = false;
                }
            } else {
                if (token1.getType() == WurstParser.PACKAGE) {
                    isWurst = true;
                } else if (isWurstOnlyKeyword(token1)) {
                    token1 = makeToken(WurstParser.ID, token1.getText(), token1.getStartIndex(), token1.getStopIndex());
                    assert token1 != null;
                } else if (token1.getType() == WurstParser.HOTDOC_COMMENT) {
                    continue;
                }
            }
            final Token token = token1;

            if (token.getType() == WurstParser.NL) {
                int line = 0;
                for (int i = 0; i < token.getText().length(); i++) {
                    char c = token.getText().charAt(i);
                    if (c == '\n') {
                        lineOffsets.set(token.getLine() + line, token.getStartIndex() + i);
                        line++;
                    }
                }
            } else if (token.getType() == WurstParser.PAREN_LEFT) {
                parenthesesLevel++;
            } else if (token.getType() == WurstParser.PAREN_RIGHT) {
                parenthesesLevel--;
            } else if (token.getType() == WurstParser.EOF) {
                // at EOF close all blocks and return an extra newline
                handleIndent(0, token, token.getStartIndex(), token.getStopIndex(), token);
                eof = token;
                if (isWurst) {
                    // if inside wurst, add a closing 'endpackage' and a newline
                    nextTokens.add(makeToken(WurstParser.ENDPACKAGE, "endpackage", token.getStartIndex(), token.getStopIndex()));
                    nextTokens.add(makeToken(WurstParser.NL, "$NL", token.getStartIndex(), token.getStopIndex()));
                }

                lineOffsets.set(token.getLine(), token.getStopIndex() + 1);
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
                        numberOfTabs = tabWidth(token);
                        continue;
                    } else {
                        // no tabs after newline
                        handleIndent(0, token, token.getStartIndex(), token.getStopIndex(), firstNewline);
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
                        numberOfTabs += tabWidth(token);
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
                            handleIndent(numberOfTabs, token, token.getStartIndex(), token.getStopIndex(), firstNewline);
                            state(State.INIT);
                            nextTokens.add(token);
                            return firstNewline;
                        }
                    }
            }
        }
    }

    private int tabWidth(Token token) {
        int len = 1 + token.getStopIndex() - token.getStartIndex();
        switch (token.getType()) {
            case WurstParser.TAB:
                return len * 4;
            case WurstParser.SPACETAB:
                return len;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void readTabChar(Token token) {
        if (tabChoice == TabChoice.Unknown) {
            tabChoice = TabChoice.from(token);
        } else if (tabWarning == null) {
            if (token.getType() == WurstParser.TAB) {
                if (tabChoice == TabChoice.Spaces) {
                    tabWarning = new CompileError(new WPos("", lineOffsets, token.getStartIndex(), token.getStopIndex()), "Mixing tabs and spaces for indentation.");
                }
            } else if (token.getType() == WurstParser.SPACETAB
                && tabChoice == TabChoice.Tabs) {
                if (tabWidth(token) > 3) {
                    // up to 3 spaces is allowed for alignment
                    tabWarning = new CompileError(new WPos("", lineOffsets, token.getStartIndex(), token.getStopIndex()), "Mixing tabs and spaces for indentation.");
                }
            }
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


    private void handleIndent(int n, Token token, int start, int stop, Token endBlockToken) {
        if (!isWurst) {
            return;
        }
        if (debug) WLogger.info("handleIndent " + n + "	 " + indentationLevels);
        if (n > indentationLevels.peek()) {
            if (spacesPerIndent < 0) {
                spacesPerIndent = n;
            } else if (parenthesesLevel == 0 && tabWarning == null && n != indentationLevels.peek() + spacesPerIndent) {
                String message = "Inconsistent indentation: Earlier in this file " + spacesPerIndent + " spaces were used for indentation and here it is " + (n - indentationLevels.peek()) + " spaces.";
                tabWarning = new CompileError(new WPos("", lineOffsets, lineOffsets.get(token.getLine()), token.getStopIndex()), message);
            }
            if (tabWarning == null && n % 2 == 1) {
                tabWarning = new CompileError(new WPos("", lineOffsets, lineOffsets.get(token.getLine()), token.getStopIndex()), "Use an even number of spaces for indentation.");
            }


            indentationLevels.push(n);
            nextTokens.add(makeToken(WurstParser.STARTBLOCK, "$begin", start, stop));
        } else {
            while (n < indentationLevels.peek()) {
                indentationLevels.pop();
                nextTokens.add(makeToken(WurstParser.ENDBLOCK, "$end", endBlockToken.getStartIndex(), endBlockToken.getStartIndex()));
            }
            Integer expectedIndentation = indentationLevels.peek();
            if (n != expectedIndentation) {
                // all lines must be indented on the same level,
                // with the exception of the 'end' keyword, which can be on a different line
                if (token.getType() != WurstParser.END) {
                    String msg = "Invalid indentation level. Current indentation is " + expectedIndentation + ", but this is indented by " + n + ".";
                    for (ANTLRErrorListener el : orig.getErrorListeners()) {
                        int line = lineOffsets.getLine(start);
                        el.syntaxError(orig, "", line, start - lineOffsets.get(line), msg, null);
                    }
                }
            }
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

    public CompilationUnitInfo.IndentationMode getIndentationMode() {
        if (tabChoice == TabChoice.Tabs) {
            return CompilationUnitInfo.IndentationMode.tabs();
        } else {
            int num = this.spacesPerIndent;
            if (num < 0) {
                num = 4;
            }
            return CompilationUnitInfo.IndentationMode.spaces(num);
        }
    }
}
