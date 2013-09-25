package de.peeeq.parseq.grammars.ast;

import org.antlr.v4.runtime.Token;

import de.peeeq.parseq.grammars.GrammarTranslation;

public class ProdLex extends ProdNamed {

	public final String lex;

	public ProdLex(Token name, Token op, Token lex) {
		super(name, op);
		this.lex = lex.getText().substring(1, lex.getText().length() - 1);
	}
	
	@Override
	public void print(GrammarTranslation tr) {
		super.printName(tr);
		tr.print("'" + lex + "'");
	}

}
