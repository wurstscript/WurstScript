package de.peeeq.wurstscript.jassinterpreter;


public class ExitwhenException extends Error {
    private static final long serialVersionUID = -7104108904090494021L;
    private static final ExitwhenException instance = new ExitwhenException();

    private ExitwhenException() {
    }

    public static ExitwhenException instance() {
        return instance;
    }

}
