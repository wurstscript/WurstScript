package de.peeeq.wurstscript.frotty.jassValidator;

import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassExprVarAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarArrayAccess;
import de.peeeq.wurstscript.jassAst.JassExprVarRef;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassStmtCall;
import de.peeeq.wurstscript.jassAst.JassStmtSet;
import de.peeeq.wurstscript.jassAst.JassStmtSetArray;

public class JassValidator {
	
	public static void validate(JassProg p) {
		p.accept(new JassProg.DefaultVisitor() {
			
			@Override
			public void visit(JassExprVarAccess e) {
				e.attrVariableDefinition();
			}
			
			@Override
			public void visit(JassExprVarArrayAccess e) {
				e.attrVariableDefinition();
			}
			
			
			@Override
			public void visit(JassExprFunctionCall e) {
				e.attrFunctionCall();
			}
			
			@Override
			public void visit(JassExprFuncRef e) {
				e.attrFuncRef();
			}
			
			@Override
			public void visit(JassStmtCall e) {
				e.attrFunctionCall();
			}
			
			@Override
			public void visit(JassStmtSet e) {
				e.attrVariableDefinition();
			}			
			
			@Override
			public void visit(JassStmtSetArray e) {
				e.attrVariableDefinition();
			}	
			
		});
	}
}
