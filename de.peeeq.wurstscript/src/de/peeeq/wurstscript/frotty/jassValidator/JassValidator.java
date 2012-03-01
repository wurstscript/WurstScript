package de.peeeq.wurstscript.frotty.jassValidator;

import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassProg;

public class JassValidator {
	
	public static void validate(JassProg p) {
		p.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassExprVarAccess e) {
				e.attrVariableDefinition();
			}
			
		});
	}
}
