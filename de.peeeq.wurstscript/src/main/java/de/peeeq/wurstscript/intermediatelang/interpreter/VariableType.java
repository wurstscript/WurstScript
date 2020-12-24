package de.peeeq.wurstscript.intermediatelang.interpreter;

public class VariableType<T> {

    public static final VariableType<Integer> INTEGER = new VariableType<>(0);
    public static final VariableType<Float> REAL = new VariableType<>(1);
    public static final VariableType<Float> UNREAL = new VariableType<>(2);
    public static final VariableType<String> STRING = new VariableType<>(3);
    private int encoding;


    private VariableType(int encoding) {
        this.encoding = encoding;
    }


}
