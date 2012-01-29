package de.peeeq.wurstscript.jasstranslation;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.VarDef;

public class GlobalInit {
	public final VarDef v;
	public final Expr initialExpr;
	
	
	public GlobalInit(VarDef v, Expr initialExpr) {
		this.v = v;
		this.initialExpr = initialExpr;
	}
	
	
	
}
