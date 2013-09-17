package de.peeeq.parseq.grammars.ast;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.parseq.grammars.parser.GrammarsParserParser.GExprPartsContext;

public class ProdAlternative extends Production {
	public final List<Production> alternatives;


	public ProdAlternative(List<GExprPartsContext>  alts) {
		this.alternatives = Lists.newArrayList();
		for (GExprPartsContext g : alts) {
			alternatives.add(g.result);
		}
	}
	
	
}
