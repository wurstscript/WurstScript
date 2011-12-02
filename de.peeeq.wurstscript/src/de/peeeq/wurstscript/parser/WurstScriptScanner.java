package de.peeeq.wurstscript.parser;
import java.io.InputStream;
import java.io.Reader;

import java_cup.runtime.Symbol;

public class WurstScriptScanner extends WurstScriptScannerIntern {

	public WurstScriptScanner(InputStream in) {
		super(in);
	}
	public WurstScriptScanner(Reader in) {
		super(in);
	}
	
	@Override
	public java_cup.runtime.Symbol next_token() throws java.io.IOException {
		Symbol token;
		if (!returnStack.isEmpty()) {
			token = returnStack.pop();
		} else {
			do {
				 token = super.next_token();
			} while (token == null);
		}
		System.out.println("TOKEN = " + ExtendedParser.symbolToString(token));
		return token;
	}

}
