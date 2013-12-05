package de.peeeq.parseq.grammars.ast;

import org.antlr.v4.runtime.Token;

import de.peeeq.parseq.grammars.GrammarTranslation;

public abstract class ProdNamed extends Production {

	public final String name;
	public final boolean isList;
	
	public ProdNamed(Token name, Token op) {
		if (name == null) {
			this.name = null;
			isList = false;
		} else {
			this.name = name.getText();
			isList = op.getText().equals("+=");
		}
	}

	public void printName(StringBuilder tr) {
		if (name != null) {
			tr.append(name);
			if (isList) {
				tr.append("+=");
			} else {
				tr.append("=");
			}
		}
		
	}

}