package de.peeeq.wurstscript.intermediateLang.interpreter;

public class VariableType<T> {
	
	private static VariableType<?>[] bw = new VariableType[4];
	
	public static final VariableType<Integer> INTEGER = new VariableType<Integer>(0);
	public static final VariableType<Float> REAL = new VariableType<Float>(1);
	public static final VariableType<Float> UNREAL = new VariableType<Float>(2);
	public static final VariableType<String> STRING = new VariableType<String>(3);
	private int encoding;
	
	
	
	
	
	private VariableType(int encoding) {
		this.encoding = encoding;
		bw[encoding] = this;
	}
	
	

}
