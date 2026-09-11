package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Gives {@code wurstKeyOf} a body on Jass, so a keyed set works on both backends.
 *
 * <p>Jass has no hashing, so a keyed structure needs an integer key. A {@code T:} type parameter
 * cannot be projected to one in source - that is why SparseSet asks its caller for a
 * {@code SparseSetKey} - but after generic elimination each specialisation has a concrete
 * parameter type, and the projection follows from it.
 *
 * <p>The projections are ordinary Wurst functions in the intrinsic's own package rather than
 * natives synthesised here. A synthesised IS_NATIVE stub would be emitted as a {@code native}
 * declaration by ImToJassTranslator unless flagged BJ or extern, redeclaring a common.j native and
 * failing pjass. Functions the library already references are in the IM, declared correctly, and
 * emitted by the normal path - so this pass only has to pick one.
 *
 * <p>On Lua this never runs: {@link LuaKeyedTable} replaces the keyed-table operations wholesale
 * before the inliner, and the element is its own key there, so no projection exists to make.
 */
public final class JassKeyOfLowering {

    /** The intrinsic being given a body. */
    private static final String KEY_OF = "wurstKeyOf";

    /** Projections it can be rewritten to, each an ordinary function in the same package. */
    private static final String KEY_OF_INT = "keyOfInt";
    private static final String KEY_OF_HANDLE = "keyOfHandle";

    private JassKeyOfLowering() {
    }

    public static void transform(ImProg prog) {
        for (ImFunction f : prog.getFunctions()) {
            if (!isNamedKeyOf(f)) {
                continue;
            }
            if (!isKeyOf(f)) {
                // The rewrite below returns an integer projection, so a declaration of another
                // shape would leave the return type disagreeing with the body it now has.
                throw new CompileError(f.attrTrace().attrErrorPos(),
                    KEY_OF + " must take exactly one parameter and return int.");
            }
            ImVar value = f.getParameters().get(0);
            ImFunction projection =
                findProjection(prog, packageOf(f), projectionFor(value.getType(), f), f);
            f.getBody().clear();
            f.getLocals().clear();
            f.getBody().add(JassIm.ImReturn(f.attrTrace(), JassIm.ImFunctionCall(
                f.attrTrace(), projection, JassIm.ImTypeArguments(),
                JassIm.ImExprs(JassIm.ImVarAccess(value)),
                false, CallType.NORMAL)));
        }
    }

    /**
     * Whether {@code f} is the key projection intrinsic still carrying its placeholder body.
     *
     * <p>The interpreter asks: it runs on the IM before transformProgToJass, so compiletime
     * evaluation and {@code -runTests} would otherwise execute that placeholder and give every
     * element the same key. ILInterpreter supplies the meaning this pass compiles the call to.
     *
     * <p>Still generic is what makes it unlowered: generic elimination gives each specialisation a
     * concrete parameter type, and only then can this pass pick a projection. A monomorphised copy
     * therefore runs its real body, so the post-transform interpreter run still exercises it.
     */
    public static boolean isUnloweredKeyOf(ImFunction f) {
        // The interpreter asks this on every call it makes, so the list checks come before the
        // trace and annotation lookups. Having type variables rules out almost everything.
        return !f.getTypeVariables().isEmpty() && isKeyOf(f);
    }

    private static boolean isKeyOf(ImFunction f) {
        return isNamedKeyOf(f)
            && f.getParameters().size() == 1
            && TypesHelper.isIntType(f.getReturnType());
    }

    private static boolean isNamedKeyOf(ImFunction f) {
        return f.attrTrace() instanceof FuncDef fd
            && KEY_OF.equals(fd.getName())
            && fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION);
    }

    /**
     * The projection of that name declared beside the intrinsic itself.
     *
     * <p>Scoped to the declaring package rather than matched by name across the program: the name
     * is not identity, and a same-named annotated function in another package would otherwise win
     * or lose by traversal order and silently key every set on something else. The whole signature
     * is checked for the same reason - the call emitted below assumes the parameter type, so a
     * helper of the wrong shape produces malformed IM and a pjass failure rather than an error
     * anyone can read.
     */
    private static ImFunction findProjection(ImProg prog, WPackage owner, String name, ImFunction f) {
        if (owner != null) {
            for (ImFunction candidate : prog.getFunctions()) {
                if (!name.equals(annotatedName(candidate)) || packageOf(candidate) != owner) {
                    continue;
                }
                if (candidate.getParameters().size() != 1
                    || !TypesHelper.isIntType(candidate.getReturnType())
                    || !takesExpectedParam(name, candidate.getParameters().get(0).getType())) {
                    throw new CompileError(candidate.attrTrace().attrErrorPos(),
                        name + " must take exactly one " + expectedParam(name)
                            + " parameter and return int.");
                }
                return candidate;
            }
        }
        throw new CompileError(f.attrTrace().attrErrorPos(),
            "The package declaring " + KEY_OF + " must also declare " + name + ".");
    }

    /** The package a compiler-intrinsic declaration belongs to, or null if it has no trace. */
    private static WPackage packageOf(ImFunction f) {
        return f.attrTrace() instanceof FuncDef fd && fd.attrNearestPackage() instanceof WPackage p
            ? p
            : null;
    }

    /** Which projection a concrete element type needs. */
    private static String projectionFor(ImType t, ImFunction f) {
        if (TypesHelper.isRealType(t) || TypesHelper.isBoolType(t)) {
            throw new CompileError(f.attrTrace().attrErrorPos(),
                "A keyed set cannot use " + typeNameOf(t) + " as its element type on Jass: it has "
                    + "no stable integer key. Use int, a handle or a class, or restrict the set "
                    + "to Lua.");
        }
        if (isSimpleType(t, "code")) {
            throw new CompileError(f.attrTrace().attrErrorPos(),
                "A keyed set cannot use code as its element type: function references have no "
                    + "stable identity to key on.");
        }
        if (TypesHelper.isStringType(t)) {
            // StringHash is not identity. This repository's own MultibyteDiagnostics records that
            // it collapses whole classes of strings to one marker hash and that its behaviour has
            // changed between game versions - so membership would be wrong for ordinary inputs,
            // and wrong differently per patch, while Lua keyed on the string itself would be
            // right. A set that disagrees with itself across backends is worse than one that says
            // no.
            throw new CompileError(f.attrTrace().attrErrorPos(),
                "A keyed set cannot use string as its element type on Jass: StringHash is lossy "
                    + "and patch-dependent, so membership would not match the Lua backend. "
                    + "Restrict the set to Lua, or key on an int derived from the string.");
        }
        if (t instanceof ImTupleType) {
            throw new CompileError(f.attrTrace().attrErrorPos(),
                "A keyed set cannot use a tuple as its element type: tuple elimination expands the "
                    + "argument, so there is no single value to key on.");
        }
        // Class instances are integers by this point, and so is int itself.
        if (TypesHelper.isIntType(t) || t instanceof ImClassType) {
            return KEY_OF_INT;
        }
        // Everything left is a handle type, whose id is its key.
        return KEY_OF_HANDLE;
    }

    /** The parameter type a projection must take, since the emitted call assumes it. */
    private static boolean takesExpectedParam(String name, ImType t) {
        return KEY_OF_INT.equals(name) ? TypesHelper.isIntType(t) : isSimpleType(t, "handle");
    }

    private static String expectedParam(String name) {
        return KEY_OF_INT.equals(name) ? "int" : "handle";
    }

    private static boolean isSimpleType(ImType t, String name) {
        return t instanceof ImSimpleType st && name.equals(st.getTypename());
    }

    private static String typeNameOf(ImType t) {
        return t instanceof ImSimpleType st ? st.getTypename() : t.toString();
    }

    /** The source name of {@code f} if it is a compiler intrinsic declaration, else null. */
    private static String annotatedName(ImFunction f) {
        return f.attrTrace() instanceof FuncDef fd
            && fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION)
            ? fd.getName()
            : null;
    }
}
