package de.peeeq.wurstio.jassinterpreter;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.intermediatelang.interpreter.ProgramState;
import de.peeeq.wurstscript.parser.WPos;

public class InterpreterException extends RuntimeException {
    private static final long serialVersionUID = 3387292080655779808L;

    private Element trace;

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

    public InterpreterException(Element trace, String msg, Throwable e) {
        super(msg, e);
        this.trace = trace;
    }

    @Override
    public String toString() {
        if (trace == null) {
            return getMessage();
        }
        WPos pos = trace.attrSource();
        return "at " + pos.print() + ":\n" + getMessage()
                + (stackTrace != null ? "\nStack trace:\n" + stackTrace : "");
    }


    public InterpreterException setStacktrace(String msg) {
        if (this.stackTrace == null) {
            this.stackTrace = msg;
        }
        return this;
    }

    public InterpreterException setTrace(Element trace) {
        if (this.trace == null) {
            this.trace = trace;
        }
        return null;
    }

    public Element getTrace() {
        return trace;
    }

    public String getWurstStackTrace() {
        return stackTrace;
    }
}
