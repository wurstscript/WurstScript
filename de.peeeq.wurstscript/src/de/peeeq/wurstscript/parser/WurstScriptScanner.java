package de.peeeq.wurstscript.parser;
import java.io.InputStream;
import java.io.Reader;

import java_cup.runtime.Symbol;

public class WurstScriptScanner extends WurstScriptScannerIntern {

	private int indent = 0;
	private Symbol eof = null;

	public WurstScriptScanner(InputStream in) {
		super(in);
	}
	public WurstScriptScanner(Reader in) {
		super(in);
	}
	
	@Override
	public java_cup.runtime.Symbol next_token() throws java.io.IOException {
		if (eof != null) {
			// finish unindents...
			if (indent > 0) {
				indent--;
				return new Symbol(TokenType.UNINDENT, eof.left, eof.right);
			} else {
				return new Symbol(TokenType.EOF, eof.left, eof.right);
			}
		}
		
		Symbol token;
		
		if (!returnStack.isEmpty()) {
			token = returnStack.pop();
		} else {
			do {
				 token = super.next_token();
			} while (token == null);

		}
		
		if (token.sym == TokenType.INDENT) {
			indent++;
		} else if (token.sym == TokenType.UNINDENT) {
			indent--;
		} else if (token.sym == TokenType.EOF) {
			if (indent > 0) {
				// when we encounter an end of file and still have indentation then we add
				// artificial newlines and unindents
				eof = token;
				return new Symbol(TokenType.NL, eof.left, eof.right);
			}
		}
		
		
		return token;
	}

}
