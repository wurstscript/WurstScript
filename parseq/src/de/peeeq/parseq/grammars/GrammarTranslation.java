package de.peeeq.parseq.grammars;

import de.peeeq.parseq.asts.ast.Program;
import de.peeeq.parseq.grammars.ast.GrammarFile;
import de.peeeq.parseq.grammars.ast.ProdAlternative;
import de.peeeq.parseq.grammars.ast.ProdId;
import de.peeeq.parseq.grammars.ast.ProdLex;
import de.peeeq.parseq.grammars.ast.ProdRepeat;
import de.peeeq.parseq.grammars.ast.ProdSequence;
import de.peeeq.parseq.grammars.ast.Production;
import de.peeeq.parseq.grammars.ast.Rule;

public class GrammarTranslation {

	private final GrammarFile grammar;
	private final StringBuilder sb = new StringBuilder();
	private final Program prog;
	
	public GrammarTranslation(GrammarFile g, Program prog) {
		this.grammar = g;
		this.prog = prog;
	}

	public void translate() {
		
		for (Rule r : grammar.rules) {
			translateRule(r);
		}
		System.out.println(sb.toString());
	}

	private void translateRule(Rule r) {
		sb.append(r.name + " returns [" + r.returnType + " result] :\n");
		r.production.print(this);
		sb.append(";\n\n");
	}

	public void print(String s) {
		sb.append(s);
	}
	
	

}
