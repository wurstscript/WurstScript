package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.jassIm.ImFunction;

/**
 * What an intrinsic declaration has to be before a lowering may take it over by name: an annotated
 * function of a package without a vararg parameter, and for most lowerings without type parameters.
 *
 * <p>A lowering that matches on the annotation, the name and the parameter types replaces the
 * declaration's behaviour with its own. Anything else that keeps those parameter types would be
 * replaced too, and its calls redirected to a stub built for the plain shape: a generic function
 * loses its specialisation, a vararg function is called with the wrong arguments, a static class
 * function of the same name is taken for the library's. One place decides, so a shape is excluded
 * for every lowering that asks and not one report at a time.
 *
 * <p>Type parameters are the one shape lowerings differ on. The keyed tables and maps are declared
 * generic on purpose: an erased {@code K:} key is the table key itself, which is what makes them
 * native tables. They ask for {@link #packageIntrinsic}, the rest for {@link #plainIntrinsic}.
 */
final class IntrinsicDeclarations {

    private IntrinsicDeclarations() {
    }

    /** The source declaration of {@code f} if it is a plain intrinsic package function, else null. */
    static FuncDef plainIntrinsic(ImFunction f) {
        FuncDef fd = packageIntrinsic(f);
        return fd != null && fd.getTypeParameters().isEmpty() ? fd : null;
    }

    /**
     * The source declaration of {@code f} if it is an intrinsic package function without a vararg
     * parameter, whether or not it has type parameters, else null.
     */
    static FuncDef packageIntrinsic(ImFunction f) {
        if (f.attrTrace() instanceof FuncDef fd
            && fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION)
            && fd.attrNearestNamedScope() instanceof WPackage
            && !hasVarargParameter(fd)) {
            return fd;
        }
        return null;
    }

    /**
     * Read off the source declaration, not the IM function: the fixed-arity copies the vararg
     * eliminator makes keep their trace but drop IS_VARARG, so a vararg function called with one
     * argument would otherwise look like a plain one-parameter declaration.
     */
    private static boolean hasVarargParameter(FuncDef fd) {
        WParameters params = fd.getParameters();
        return params.size() >= 1 && params.get(params.size() - 1).attrIsVararg();
    }
}
