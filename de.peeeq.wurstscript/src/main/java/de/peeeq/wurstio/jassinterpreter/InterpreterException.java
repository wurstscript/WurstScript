package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.intermediateLang.interpreter.ProgramState;
import de.peeeq.wurstscript.parser.WPos;

public class InterpreterException extends RuntimeException {
    private static final long serialVersionUID = 3387292080655779808L;

    private final Element trace;

    private String stackTrace;

    public InterpreterException(ProgramState g, String msg) {
        super(msg);
        this.trace = g.getLastStatement().attrTrace();
    }

    public InterpreterException(String msg) {
        super(msg);
        this.trace = null;
    }

    public InterpreterException(Element trace, String msg) {
        super(msg);
        this.trace = trace;
    }

    public InterpreterException(Element trace, String msg, Exception e) {
        super(msg, e);
        this.trace = trace;
    }

    @Override
    public String toString() {
        if (trace == null) {
            return getMessage();
        }
        System.err.println(trace);
        System.err.println(trace.attrSource());
        WPos pos = trace.attrSource();
        return "at " + pos.print() + ":\n" + getMessage()
                + (stackTrace != null ? "\nStack trace:\n" + stackTrace : "");
    }


    public InterpreterException withStacktrace(String msg) {
        this.stackTrace = msg;
        return this;
    }

}
