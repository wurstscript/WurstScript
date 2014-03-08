package de.peeeq.wurstscript.parser;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import java_cup.runtime.Symbol;

public class JurstScanner extends JurstScannerIntern {

	enum State {
		INIT, WRAP_CHAR, NEWLINES
	}
	
	private State state = State.INIT;
	private Symbol returnNext;
	private Symbol firstNewline;
	private Symbol eof;

	public JurstScanner(InputStream in) {
		super(in);
	}
	public JurstScanner(Reader in) {
		super(in);
	}
	
	@Override
	public Symbol next_token() throws java.io.IOException {
		Symbol nextToken = getNextToken();
		return nextToken;
	}
	private Symbol getNextToken() throws IOException {
		if (eof != null) {
			return new Symbol(TokenType.EOF, eof.left, eof.right);
		}
		
		if (returnNext != null) {
			Symbol temp = returnNext;
			returnNext = null;
			return temp;
		}
		
		for (;;) {
			Symbol token = super.next_token();
			if (token == null) {
				continue;
			}
			
			if (token.sym == JurstTokenType.EOF) {
				// at EOF return an extra newline
				eof = token;
				return new Symbol(TokenType.NL, token.left, token.right);
//				return token;
			}
			
			switch (state) {
			case INIT:
				if (isWrapChar(token.sym)) {
					state = State.WRAP_CHAR;
					return token;
				} else if (token.sym == JurstTokenType.NL) {
					firstNewline = token;
					state = State.NEWLINES;
					continue;
				}
				return token;
			case NEWLINES:
				if (isWrapChar(token.sym)) {
					// ignore all the newlines when a wrap char comes after newlines
					state = State.WRAP_CHAR;
					return token;
				} else if (token.sym == JurstTokenType.NL) {
					continue;
				} else {
					returnNext = token;
					state = State.INIT;
					return firstNewline;
				}
			case WRAP_CHAR:
				if (isWrapChar(token.sym)) {
					return token;
				} else if (token.sym == JurstTokenType.NL) {
					// ignore newlines after wrap char
					continue;
				} else {
					state = State.INIT;
					return token;
				}
			}
		}
	}

	private boolean isWrapChar(int sym) {
		switch (sym) {
			case JurstTokenType.COMMA:
			case JurstTokenType.DOT:
			case JurstTokenType.DOTDOT:
				return true;
		}
		return false;
	}
	public List<ScannerError> getErrors() {
		return errors;
	}
}
