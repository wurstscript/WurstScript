package de.peeeq.wurstscript.parser.antlr;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Pair;

import de.peeeq.wurstscript.antlr.WurstLexer;
import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;

public class ExtendedWurstLexer implements TokenSource {

	private final WurstLexer orig;
	private Queue<Token> nextTokens = new LinkedList<>();
	private State state = State.INIT;
	private Stack<Integer> indentationLevels = new Stack<>();
	private Token eof = null;
	private Token firstNewline;
	private int numberOfTabs;
	private LineOffsets lineOffsets = new LineOffsets();
	public StringBuilder debugSb = new StringBuilder();
	private final boolean debug = true;
	private Pair<TokenSource, CharStream> sourcePair;
	private boolean isWurst = false;


	enum State {
		INIT, WRAP_CHAR, NEWLINES, BEGIN_LINE
	}


	public ExtendedWurstLexer(CharStream input) {
		orig = new WurstLexer(input);
		sourcePair = new Pair<TokenSource, CharStream>(orig, input);
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

		debugSb.append(t.getText() + " ");
		if (debug) System.out.println("		new token: " + t);
		return t;		
	}


	private Token nextTokenIntern() {
		if (!nextTokens.isEmpty()) {
			return nextTokens.poll();
		}

		if (eof != null) {
			return makeToken(WurstParser.EOF, "$EOF", eof.getStartIndex(), eof.getStopIndex());
		}




		for (;;) {
			Token token = orig.nextToken();

			if (debug) System.out.println("orig token = " + token);

			if (token == null) {
				continue;
			}

			if (isWurst) {
				if (isJassOnlyKeyword(token)) {
					token = makeToken(WurstParser.ID, token.getText(), token.getStartIndex(), token.getStopIndex());
				} else if (token.getType() == WurstParser.ENDPACKAGE) {
					handleIndent(0, token.getStartIndex(), token.getStopIndex());
					isWurst = false;
				}
			} else {
				if (token.getType() == WurstParser.PACKAGE) {
					isWurst = true;
				} else if (isWurstOnlyKeyword(token)) {
					token = makeToken(WurstParser.ID, token.getText(), token.getStartIndex(), token.getStopIndex());
				} else if (token.getType() == WurstParser.HOTDOC_COMMENT) {
					continue;
				}
			}


			if (token.getType() == WurstParser.NL) {
				lineOffsets.set(token.getLine(), token.getStartIndex());
			}

			if (token.getType() == WurstParser.EOF) {
				// at EOF close all blocks and return an extra newline
				handleIndent(0, token.getStartIndex(), token.getStopIndex());
				eof = token;
				return makeToken(WurstParser.NL, "$NL", token.getStartIndex(), token.getStopIndex());
			}
			
			
			

			switch (state) {
			case INIT:
				if (isWrapCharEndLine(token.getType())) {
					state(State.WRAP_CHAR);
					return token;
				} else if (token.getType() == WurstParser.NL) {
					firstNewline = token;
					state(State.NEWLINES);
					continue;
				} else if (token.getType() == WurstParser.TAB) {
					continue;
				}
				return token;
			case NEWLINES:
				if (isWrapCharBeginLine(token.getType())) {
					// ignore all the newlines when a wrap char comes after newlines
					state(State.WRAP_CHAR);
					return token;
				} else if (token.getType() == WurstParser.NL) {
					continue;
				} else if (token.getType() == WurstParser.TAB) {
					state(State.BEGIN_LINE);
					numberOfTabs = 1; 
					continue;
				} else {
					// no tabs after newline
					handleIndent(0, token.getStartIndex(), token.getStopIndex());
					nextTokens.add(token);
					state(State.INIT);
					return firstNewline;
				}
			case WRAP_CHAR:
				if (isWrapCharEndLine(token.getType())) {
					return token;
				} else if (token.getType() == WurstParser.NL
						|| token.getType() == WurstParser.TAB) {
					// ignore newlines and tabs after wrap char
					continue;
				} else {
					state(State.INIT);
					return token;
				}
			case BEGIN_LINE:
				if (token.getType() == WurstParser.TAB) {
					numberOfTabs++;
					continue;
				} else if (token.getType() == WurstParser.NL) {
					state(State.NEWLINES);
					continue;
				} else if (isWrapCharBeginLine(token.getType())) {
					// ignore all the newlines when a wrap char comes after newlines
					state(State.WRAP_CHAR);
					return token;
				} else {
					handleIndent(numberOfTabs, token.getStartIndex(), token.getStopIndex());
					state(State.INIT);
					nextTokens.add(token);
					return firstNewline;
				}
			}
		}
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
		if (debug) System.out.println("state " + state + " -> " + s);
		state = s;
	}


	private void handleIndent(int n, int start, int stop) {
		if (!isWurst) {
			return;
		}
		if (debug) System.out.println("handleIndent " + n + "	 " + indentationLevels);
		if (n > indentationLevels.peek()) {
			indentationLevels.push(n);
			nextTokens.add(makeToken(WurstParser.STARTBLOCK, "$begin", start, stop));
		} else {
			while (n < indentationLevels.peek()) {
				indentationLevels.pop();
				nextTokens.add(makeToken(WurstParser.ENDBLOCK, "$end", start, stop));
			}
			if (n != indentationLevels.peek()) {
				for (ANTLRErrorListener el : orig.getErrorListeners()) {
					int line = lineOffsets.getLine(start);
					el.syntaxError(orig, "", line, start - lineOffsets.get(line), "Invalid indentation level.", null);
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
			return true;
		default:
			return isWrapChar(type);
		}
	}


	private @NotNull Token makeToken(int type, String text, int start, int stop) {
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

	
	public void addErrorListener(ANTLRErrorListener listener) {
		orig.addErrorListener(listener);
	}
}
