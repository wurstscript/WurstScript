package de.peeeq.parseq.grammars.ast;

import de.peeeq.parseq.asts.ast.AstEntityDefinition;
import de.peeeq.parseq.asts.ast.Program;

public class AstElement {
	private AstElement parent;

	public AstElement getParent() {
		return parent;
	}
	
	void setParent(AstElement parent) {
		if (parent == null) throw new IllegalArgumentException();
		this.parent = parent;
	}
	
	
	GrammarFile getGrammarFile() {
		AstElement e = this;
		while (e != null) {
			if (e instanceof GrammarFile) {
				return (GrammarFile) e;
			}
			if (e.getParent() == null) {
				System.err.println(e);
			}
			e = e.getParent();
		}
		throw new Error("AstElement not attached to grammar file: " + this);
	}
	
	Rule lookupRule(String ruleName) {
		GrammarFile gf = getGrammarFile();
		for (Rule r : gf.rules) {
			if (r.name.equals(ruleName)) {
				return r;
			}
		}
		return null;
	}
	
	AstEntityDefinition lookupAstEntity(String name) {
		GrammarFile gf = getGrammarFile();
		Program p = gf.program;
		return p.definitions.get(name);
	}
}
