package de.peeeq.wurstscript.translation.imtranslation;

/**
 * function with @compiletime
 */
public class FunctionFlagCompiletime implements FunctionFlag {
    /** index, which specifies order in which functions are executed
     * Compiletime functions with smaller index are executed first */
    private int index;

    public FunctionFlagCompiletime(int index) {
        this.index = index;
    }

    public int getOrderIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "@compiletime(" + index + ")";
    }
}
