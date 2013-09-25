package de.peeeq.parseq.grammars.ast;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.parseq.grammars.GrammarTranslation;
import de.peeeq.parseq.grammars.parser.GrammarsParserParser.GExprPartContext;

public class ProdSequence extends Production {
	public final List<Production> prods;


	public ProdSequence(List<GExprPartContext> parts) {
		this.prods = Lists.newArrayList();
		for (GExprPartContext g : parts) {
			prods.add(g.result);
			g.result.setParent(this);
		}
	}


	@Override
	public void print(GrammarTranslation tr) {
		for (Production c : prods) {
			tr.print(" ");
			c.print(tr);
		}
	}
	
	
}
