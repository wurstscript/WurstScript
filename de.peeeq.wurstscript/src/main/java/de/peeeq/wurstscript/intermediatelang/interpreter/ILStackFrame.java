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
    /** Where the frame was entered; its position is worked out only when a message needs it. */
    private final @Nullable Element entry;
    private @Nullable WPos trace;
    public final @Nullable ILconstObject receiver;
    public final Map<ImTypeVar, ImType> typeSubstitutions;

    // last executed element *within this frame*
    public @Nullable de.peeeq.wurstscript.jassIm.Element currentElement;

    public ILStackFrame(ImFunction f, @Nullable ILconstObject receiver, ILconst[] args2,
                        Element entry, Map<ImTypeVar, ImType> typeSubstitutions) {
        this.f = Either.left(f);
        this.receiver = receiver;
        this.args = args2;
        this.entry = entry;
        this.typeSubstitutions = typeSubstitutions;
    }

    public ILStackFrame(ImCompiletimeExpr f, WPos trace) {
        this.f = Either.right(f);
        this.args = new ILconst[0];
        this.entry = null;
        this.trace = trace;
        this.receiver = null;
        this.typeSubstitutions = Collections.emptyMap();
    }

    public @Nullable WPos getCurrentSourcePos() {
        if (currentElement != null) {
            try {
                return currentElement.attrTrace().attrSource();
            } catch (Exception ignored) {}
        }
        return getTrace();
    }

    private WPos getTrace() {
        WPos result = trace;
        if (result == null) {
            result = entry.attrTrace().attrErrorPos();
            trace = result;
        }
        return result;
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        WPos pos = getCurrentSourcePos();
        if (pos != null && !pos.isArtificial()) {
            String file = new File(pos.getFile()).getName();
            sb.append("    ╚ ").append(file).append(":").append(pos.getLine());
        }

        if (f.isLeft()) {
            sb.append(" inside call ").append(f.getLeft().getName()).append("(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(args[i]);
            }
            sb.append(")");
        } else {
            sb.append("... when executing compiletime expression ");
        }
        return sb.toString();
    }

    public CompileError makeCompileError() {
        // Use current element position if available:
        WPos pos = getCurrentSourcePos();
        return new CompileError(pos != null ? pos : getTrace(), getMessage());
    }
}
