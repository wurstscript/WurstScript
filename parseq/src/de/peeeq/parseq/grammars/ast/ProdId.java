package de.peeeq.parseq.grammars.ast;

import org.antlr.v4.runtime.Token;

import de.peeeq.parseq.grammars.GrammarTranslation;

public class ProdId extends ProdNamed {

	public final String text;
	public ProdId(Token name, Token op, Token i) {
		super(name, op);
		this.text = i.getText();
	}
	
	@Override
	public void print(GrammarTranslation tr) {
		super.printName(tr);
		tr.print(text);
	}

}
