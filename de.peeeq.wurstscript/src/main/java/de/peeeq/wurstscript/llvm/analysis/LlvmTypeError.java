package de.peeeq.wurstscript.llvm.analysis;


import de.peeeq.wurstscript.parser.WPos;

public class LlvmTypeError extends RuntimeException {
    private WPos source;

    public LlvmTypeError(WPos source, String message) {
        super(message);
        this.source = source;
    }


    public int getLine() {
        return source.getLine();
    }

    public int getColumn() {
        return source.getStartColumn();
    }

    @Override
    public String toString() {
        return "Error in line " + getLine() + ":" + getColumn() + ": " + getMessage();
    }


}
