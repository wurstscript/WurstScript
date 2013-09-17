package de.peeeq.parseq.grammars.ast;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.parseq.grammars.parser.GrammarsParserParser.GrammarRuleContext;

public class GrammarFile {

	public final List<Rule> rules;
	
	public GrammarFile(List<GrammarRuleContext> rules) {
		this.rules = Lists.newArrayList();
		for (GrammarRuleContext g : rules) {
			this.rules.add(g.result);
		}
	}

}
