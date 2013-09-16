package de.peeeq.parseq.grammars;

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
	
	public GrammarTranslation(GrammarFile g) {
		this.grammar = g;
	}

	public void translate() {
		
		for (Rule r : grammar.rules) {
			translateRule(r);
		}
		System.out.println(sb.toString());
	}

	private void translateRule(Rule r) {
		sb.append(r.name + " returns [" + r.returnType + " result] :\n");
		translateProduction(r.production);
		sb.append(";\n\n");
		
	}

	private void translateProduction(Production prod) {
		if (prod instanceof ProdAlternative) {
			ProdAlternative p = (ProdAlternative) prod;
			
			boolean first = true;
			
			sb.append("(");
			for (Production a : p.alternatives) {
				if (!first) {
					sb.append(") | (");
				}
				translateProduction(a);
				first = false;
			}
			sb.append(")");
			
		} else if (prod instanceof ProdId) {
			ProdId p = (ProdId) prod;
			sb.append(p.text);
			
		} else if (prod instanceof ProdLex) {
			ProdLex p = (ProdLex) prod;
			sb.append("'" + p.lex + "'");
			
		} else if (prod instanceof ProdRepeat) {
			ProdRepeat p = (ProdRepeat) prod;
			sb.append("(");
			translateProduction(p.prod);
			sb.append(")");
			switch (p.repType) {
			case ARBITRARY:
				sb.append("*");
				break;
			case AT_LEAST_ONCE:
				sb.append("+");
				break;
			case ZERO_OR_ONCE:
				sb.append("+");
				break;
			
			}
		} else if (prod instanceof ProdSequence) {
			ProdSequence p = (ProdSequence) prod;
			for (Production c : p.prods) {
				sb.append(" ");
				translateProduction(c);
			}
		}
		
	}
	
	

}
