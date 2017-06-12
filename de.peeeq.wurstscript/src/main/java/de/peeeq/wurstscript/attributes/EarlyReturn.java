package de.peeeq.wurstscript.attributes;

import com.google.common.base.Preconditions;

import de.peeeq.wurstscript.ast.FunctionDefinition;

public class EarlyReturn extends Exception {
	private static final long serialVersionUID = 996637533377651375L;
	
	private FunctionDefinition func;

	public EarlyReturn(FunctionDefinition f) {
		Preconditions.checkNotNull(f);
		this.func = f;
	}
	
	public FunctionDefinition getFunc() {
		return func;
	}
	
	
}
