package de.peeeq.parseq.grammars;

import de.peeeq.parseq.asts.FileGenerator;
import de.peeeq.parseq.asts.ast.Program;
import de.peeeq.parseq.grammars.ast.GrammarFile;
import de.peeeq.parseq.grammars.ast.Rule;

public class GrammarTranslation {

	private final GrammarFile grammar;
	private final StringBuilder sb = new StringBuilder();
	private final Program prog;
	private FileGenerator fileGenerator;
	
	public GrammarTranslation(FileGenerator fileGenerator, GrammarFile g, Program prog) {
		this.fileGenerator = fileGenerator;
		this.grammar = g;
		this.prog = prog;
	}

	public void translate() {
		sb.append(FileGenerator.PARSEQ_COMMENT + "\n\n");
		for (Rule r : grammar.rules) {
			translateRule(r);
		}
		fileGenerator.createFile("grammar.g4", sb);
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
