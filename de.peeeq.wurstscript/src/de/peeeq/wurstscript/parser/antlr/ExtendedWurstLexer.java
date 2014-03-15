package de.peeeq.wurstscript.parser.antlr;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;

import de.peeeq.wurstscript.antlr.WurstLexer;
import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.parser.JurstTokenType;

public class ExtendedWurstLexer implements TokenSource {

	private final WurstLexer orig;
	private Queue<Token> nextTokens = new LinkedList<>();
	private State state = State.INIT;
	private int currentLineTabs = 0;
	private Stack<Integer> indentationLevels = new Stack<>();
	private Token eof = null;
	private Token firstNewline;
	private int numberOfTabs;
	
	enum State {
		INIT, WRAP_CHAR, NEWLINES, BEGIN_LINE
	}
	
	
	public ExtendedWurstLexer(CharStream input) {
		orig = new WurstLexer(input);
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
		System.out.println("		new token: " + t);
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
			System.out.println("orig token = " + token);
			
			if (token == null) {
				continue;
			}
			
			
			if (token.getType() == WurstParser.EOF) {
				// at EOF close all blocks and return an extra newline
				handleIndent(0, token.getStartIndex(), token.getStopIndex());
				eof = token;
				return makeToken(WurstParser.NL, "$NL", token.getStartIndex(), token.getStopIndex());
			}
			
			switch (state) {
			case INIT:
				if (isWrapChar(token.getType())) {
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
				if (isWrapChar(token.getType())) {
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
					nextTokens.add(token);
					state(State.INIT);
					return firstNewline;
				}
			case WRAP_CHAR:
				if (isWrapChar(token.getType())) {
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
				} else {
					handleIndent(numberOfTabs, token.getStartIndex(), token.getStopIndex());
					state(State.INIT);
					nextTokens.add(token);
					return firstNewline;
				}
			}
		}
	}


	private void state(State s) {
		System.out.println("state " + state + " -> " + s);
		state = s;
	}


	private void handleIndent(int n, int start, int stop) {
		if (n > indentationLevels.peek()) {
			indentationLevels.push(n);
			nextTokens.add(makeToken(WurstParser.STARTBLOCK, "$begin", start, stop));
		} else {
			while (n < indentationLevels.peek()) {
				indentationLevels.pop();
				nextTokens.add(makeToken(WurstParser.ENDBLOCK, "$end", start, stop));
			}
		}
	}


	private boolean isWrapChar(int type) {
		return false; //type == WurstParser.
	}



	private Token makeToken(int type, String text, int start, int stop) {
		CommonToken t = new CommonToken(type);
		t.setStartIndex(start);
		t.setStopIndex(stop);
		t.setText(text);
		return t;
	}

	@Override
	public void setTokenFactory(TokenFactory<?> factory) {
		orig.setTokenFactory(factory);
	}

}
