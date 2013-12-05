package de.peeeq.parseq.grammars.ast;

import org.antlr.v4.runtime.Token;

import de.peeeq.parseq.asts.ast.AstEntityDefinition;

public class Rule extends AstElement {
	final public String name;
	final public String returnType;
	final public Production production;
	
	
	public Rule(String name, String returnType, Production production) {
		this.name = name;
		this.returnType = returnType;
		this.production = production;
		production.setParent(this);
	}


	public Rule(Token t, Token n, Production p) {
		name = n.getText();
		if (t == null) {
			returnType = name;
		} else {
			returnType = t.getText();
		}
		production = p;
		p.setParent(this);
	}


	public SimpleType getReturnType() {
		switch (returnType) {
		case "void":
			return new SimpleTypeVoid();
		case "String":
			return new SimpleTypeString();
		default:
			// lookup ast type
			AstEntityDefinition astType = lookupAstEntity(returnType);
			return new SimpleTypeAst(astType);
		}
	}
	
	
}
