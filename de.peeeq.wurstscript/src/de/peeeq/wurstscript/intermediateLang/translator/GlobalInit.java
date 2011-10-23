package de.peeeq.wurstscript.intermediateLang.translator;

import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.intermediateLang.ILvar;

public class GlobalInit {
	ILvar v;
	Expr init;
	
	public GlobalInit(ILvar v, Expr init) {
		if (v == null) throw new IllegalArgumentException();
		if (init == null) throw new IllegalArgumentException();
		this.v = v;
		this.init = init;
	}
	
	@Override
	public String toString() {
		return v.getName();
	}
	
}