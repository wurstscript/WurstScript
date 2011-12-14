package de.peeeq.wurstscript.jasstranslation;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.jassAst.JassVar;

public class GlobalInit {
	public final JassVar v;
	public final Expr initialExpr;
	
	
	public GlobalInit(JassVar v, Expr initialExpr) {
		this.v = v;
		this.initialExpr = initialExpr;
	}
	
	
	
}
