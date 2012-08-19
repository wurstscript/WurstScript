package de.peeeq.wurstscript.validation.controlflow;

import java.util.Collection;

import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.WStatement;

public abstract class ForwardMethod<T> {

	private FunctionImplementation f;
	
	
	abstract T calculate(WStatement s, T incoming);
	abstract T merge(Collection<T> values);
	abstract boolean equality(T a, T b);
	abstract void checkFinal(T fin);
	public abstract T inital();

	public FunctionImplementation getFuncDef() {
		return f;
	}

	public void setFuncDef(FunctionImplementation f) {
		this.f = f;
	}
	
}
