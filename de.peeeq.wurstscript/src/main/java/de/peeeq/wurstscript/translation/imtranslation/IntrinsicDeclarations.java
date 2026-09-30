package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImFunction;

/**
 * What an intrinsic declaration has to be before a lowering may take it over by name: an annotated
 * function of a package, without type parameters and without a vararg parameter.
 *
 * <p>A lowering that matches on the annotation, the name and the parameter types replaces the
 * declaration's behaviour with its own. Anything else that keeps those parameter types would be
 * replaced too, and its calls redirected to a stub built for the plain shape: a generic function
 * loses its specialisation, a vararg function is called with the wrong arguments, a static class
 * function of the same name is taken for the library's. One place decides, so a shape is excluded
 * for every lowering that asks and not one report at a time.
 */
final class IntrinsicDeclarations {

    private IntrinsicDeclarations() {
    }

    /** The source declaration of {@code f} if it is a plain intrinsic package function, else null. */
    static FuncDef plainIntrinsic(ImFunction f) {
        if (f.attrTrace() instanceof FuncDef fd
            && fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION)
            && fd.getTypeParameters().isEmpty()
            && fd.attrNearestNamedScope() instanceof WPackage
            && !f.hasFlag(FunctionFlagEnum.IS_VARARG)) {
            return fd;
        }
        return null;
    }
}
