package de.peeeq.wurstscript.validation.controlflow;

import java.util.Collection;

import de.peeeq.wurstscript.ast.FunctionImplementation;
import de.peeeq.wurstscript.ast.FunctionLike;
import de.peeeq.wurstscript.ast.WStatement;

public abstract class ForwardMethod<T> {

	private FunctionLike f;
	boolean debug = false;
	
	abstract T calculate(WStatement s, T incoming);
	abstract T merge(Collection<T> values);
	boolean equality(T a, T b) {
		return a.equals(b);
	}
	
	String print(T t) {
		if (t == null) return "null";
		return t.toString();
	}
	abstract void checkFinal(T fin);
	public abstract T startValue();

	public FunctionLike getFuncDef() {
		return f;
	}

	public void setFuncDef(FunctionLike f) {
		this.f = f;
	}
	
	public void execute(FunctionLike f) {
		this.f = f;
		ForwardExecution<T> ex = new ForwardExecution<T>(f, (ForwardMethod<T>) this);
		ex.execute();
	}
	
}
