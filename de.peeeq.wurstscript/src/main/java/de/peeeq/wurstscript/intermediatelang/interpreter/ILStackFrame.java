package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.parser.WPos;

public class ILStackFrame {

    public final ImFunction f;
    public final ILconst[] args;
    public final WPos trace;

    public ILStackFrame(ImFunction f, ILconst[] args2, WPos trace) {
        this.f = f;
        this.args = args2;
        this.trace = trace;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("... when calling " + f.getName() + "(");
        boolean first = true;
        for (ILconst arg : args) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(arg);
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public CompileError makeCompileError() {
        return new CompileError(trace, getMessage());
    }

}
