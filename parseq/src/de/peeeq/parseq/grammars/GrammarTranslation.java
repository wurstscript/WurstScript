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
		sb.append("grammar " + prog.getLastPackagePart() + ";\n\n");
		
		for (Rule r : grammar.rules) {
			translateRule(r);
		}
		fileGenerator.createFile("grammar.g4", sb);
		System.out.println(sb.toString());
	}

	private void translateRule(Rule r) {
		if (r.production.getParent() == null) {
			throw new Error("wtf " + r);
		}
		sb.append(r.name);
		if (!r.returnType.equals("void")) {
			sb.append(" returns [" + r.returnType + " result] ");
		}
		sb.append(":\n");
		r.production.print(sb);
		sb.append(r.production.getType());
		sb.append(";\n\n");
	}

	public void print(String s) {
		sb.append(s);
	}
	
	

}
