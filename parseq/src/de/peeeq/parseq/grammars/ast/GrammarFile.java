package de.peeeq.parseq.grammars.ast;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.parseq.asts.ast.Program;
import de.peeeq.parseq.grammars.parser.GrammarsParserParser.GrammarRuleContext;

public class GrammarFile extends AstElement {

	public final List<Rule> rules;
	public Program program;
	
	public GrammarFile(List<GrammarRuleContext> rules) {
		this.rules = Lists.newArrayList();
		for (GrammarRuleContext g : rules) {
			this.rules.add(g.result);
			g.result.setParent(this);
		}
	}

}
