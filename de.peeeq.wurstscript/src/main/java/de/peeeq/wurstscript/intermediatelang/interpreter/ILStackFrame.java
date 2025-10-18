package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstObject;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.parser.WPos;
import io.vavr.control.Either;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Map;

public class ILStackFrame {

    public final Either<ImFunction, ImCompiletimeExpr> f;
    public final ILconst[] args;
    public final WPos trace;
    public final @Nullable ILconstObject receiver;
    public final Map<ImTypeVar, ImType> typeSubstitutions;

    public ILStackFrame(ImFunction f, @Nullable ILconstObject receiver, ILconst[] args2, WPos trace, Map<ImTypeVar, ImType> typeSubstitutions) {
        this.f = Either.left(f);
        this.receiver = receiver;
        this.args = args2;
        this.trace = trace;
        this.typeSubstitutions = typeSubstitutions;
    }

    public ILStackFrame(ImCompiletimeExpr f, WPos trace) {
        this.f = Either.right(f);
        this.args = new ILconst[0];
        this.trace = trace;
        this.receiver = null;
        this.typeSubstitutions = Collections.emptyMap();
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (f.isLeft()) {
            sb.append("... when calling ").append(f.getLeft().getName()).append("(");
            boolean first = true;
            for (ILconst arg : args) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(arg);
                first = false;
            }
            sb.append(")");
        } else {
            sb.append("... when executing compiletime expression ");
        }

        if (trace != null && !trace.isArtificial()) {
            String file = new File(trace.getFile()).getName();
            sb.append(" in ").append(file).append(":").append(trace.getLine());
        }

        return sb.toString();
    }

    public CompileError makeCompileError() {
        return new CompileError(trace, getMessage());
    }

    public WPos getTrace() {
        return trace;
    }
}
