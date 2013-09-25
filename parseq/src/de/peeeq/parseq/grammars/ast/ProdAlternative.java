package de.peeeq.parseq.grammars.ast;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.parseq.grammars.GrammarTranslation;
import de.peeeq.parseq.grammars.parser.GrammarsParserParser.GExprPartsContext;

public class ProdAlternative extends Production {
	public final List<Production> alternatives;


	public ProdAlternative(List<GExprPartsContext>  alts) {
		this.alternatives = Lists.newArrayList();
		for (GExprPartsContext g : alts) {
			alternatives.add(g.result);
			g.result.setParent(this);
		}
	}


	@Override
	public void print(GrammarTranslation tr) {
		boolean first = true;
		
		tr.print("(");
		for (Production a : alternatives) {
			if (!first) {
				tr.print(") | (");
			}
			a.print(tr);;
			first = false;
		}
		tr.print(")");
		
	}


	
}
