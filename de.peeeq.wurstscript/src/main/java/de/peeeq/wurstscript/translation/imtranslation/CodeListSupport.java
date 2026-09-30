package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Answers the library's compile-time check that the compiler knows the {@code CodeList} lowering.
 *
 * <p>A library that lists code values through the {@code codeList} intrinsics runs without the Lua
 * lowering too, through a trigger per list, only slowly and without saying so. The library declares
 * {@code codeListLowered() returns boolean} as an intrinsic whose source body answers false and
 * checks it in a compile-time function, which fails the build with {@code compileError}. This
 * compiler replaces every call of that declaration with true, on both targets and before any
 * compile-time function runs, so the check passes only here. An older compiler runs the source body
 * and stops the build.
 *
 * <p>Matching is by declaration, as for the list operations: the annotation, the name, no
 * parameters, no type parameters and a boolean result.
 */
public final class CodeListSupport {

    private static final String PROBE = "codeListLowered";

    private CodeListSupport() {
    }

    /** Replaces the calls of the support probe with true. */
    public static void markSupported(ImProg prog) {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                if (isProbe(call.getFunc())) {
                    call.replaceBy(JassIm.ImBoolVal(true));
                }
            }
        });
    }

    static boolean isProbe(ImFunction f) {
        return f.attrTrace() instanceof FuncDef fd
            && PROBE.equals(fd.getName())
            && fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION)
            && fd.getTypeParameters().isEmpty()
            && f.getParameters().isEmpty()
            && TypesHelper.isBoolType(f.getReturnType());
    }
}
