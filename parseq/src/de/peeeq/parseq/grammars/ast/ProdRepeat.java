package de.peeeq.parseq.grammars.ast;

import org.antlr.v4.runtime.Token;



public class ProdRepeat extends Production {

	public final Production prod;
	public final RepeatType repType;

	public ProdRepeat(Production prod, RepeatType repType) {
		this.prod = prod;
		this.repType = repType;
	}

	public static Production create(Production p, Token mod) {
		if (mod == null) {
			return p;
		}
		RepeatType repType;
		if (mod.getText() == "+") {
			repType = RepeatType.AT_LEAST_ONCE;
		} else if (mod.getText() == "*") {
			repType = RepeatType.ARBITRARY;
		} else if (mod.getText() == "?") {
			repType = RepeatType.ZERO_OR_ONCE;
		} else {
			throw new Error(mod.getText());
		}
		return new ProdRepeat(p, repType);
	}
	
	
	
}
