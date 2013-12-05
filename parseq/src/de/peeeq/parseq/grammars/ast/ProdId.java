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
	public void print(StringBuilder tr) {
		super.printName(tr);
		tr.append(text);
	}

	@Override
	public ProdType getType() {
		Rule r = lookupRule(text);
		if (r == null) {
			throw new Error("Could not find rule " + text);
		}
		SimpleType t = r.getReturnType();
		if (t instanceof SimpleTypeVoid) {
			return ProdType.empty();
		}
		String name2 = name;
		if (name2 == null) {
			name2 = "anon";
		}
		return new ProdType(name2, t);
	}

}
