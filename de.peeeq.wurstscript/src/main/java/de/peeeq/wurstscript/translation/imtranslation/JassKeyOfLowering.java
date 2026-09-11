package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.LinkedHashMap;
import java.util.Map;

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
    private static final String KEY_OF_STRING = "keyOfString";

    private JassKeyOfLowering() {
    }

    public static void transform(ImProg prog) {
        Map<String, ImFunction> projections = new LinkedHashMap<>();
        for (ImFunction f : prog.getFunctions()) {
            String name = annotatedName(f);
            if (KEY_OF_INT.equals(name) || KEY_OF_HANDLE.equals(name) || KEY_OF_STRING.equals(name)) {
                projections.put(name, f);
            }
        }

        for (ImFunction f : prog.getFunctions()) {
            if (!KEY_OF.equals(annotatedName(f)) || f.getParameters().size() != 1) {
                continue;
            }
            ImVar value = f.getParameters().get(0);
            ImFunction projection = projections.get(projectionFor(value.getType(), f));
            if (projection == null) {
                // The library is expected to declare all three next to the intrinsic; without them
                // there is nothing to call, and silently leaving the original body would ship a
                // keyed set that does not key on anything.
                throw new CompileError(f.attrTrace().attrErrorPos(),
                    "The KeyedTable package must declare keyOfInt, keyOfHandle and keyOfString "
                        + "alongside " + KEY_OF + ".");
            }
            f.getBody().clear();
            f.getLocals().clear();
            f.getBody().add(JassIm.ImReturn(f.attrTrace(), JassIm.ImFunctionCall(
                f.attrTrace(), projection, JassIm.ImTypeArguments(),
                JassIm.ImExprs(JassIm.ImVarAccess(value)),
                false, CallType.NORMAL)));
        }
    }

    /** Which projection a concrete element type needs. */
    private static String projectionFor(ImType t, ImFunction f) {
        if (TypesHelper.isRealType(t) || TypesHelper.isBoolType(t)) {
            throw new CompileError(f.attrTrace().attrErrorPos(),
                "A keyed set cannot use " + typeNameOf(t) + " as its element type on Jass: it has "
                    + "no stable integer key. Use int, string, a handle or a class, or restrict "
                    + "the set to Lua.");
        }
        if (isCodeType(t)) {
            throw new CompileError(f.attrTrace().attrErrorPos(),
                "A keyed set cannot use code as its element type: function references have no "
                    + "stable identity to key on.");
        }
        if (TypesHelper.isStringType(t)) {
            return KEY_OF_STRING;
        }
        // Class instances are integers by this point, and so is int itself.
        if (TypesHelper.isIntType(t) || t instanceof ImClassType) {
            return KEY_OF_INT;
        }
        // Everything left is a handle type, whose id is its key.
        return KEY_OF_HANDLE;
    }

    private static boolean isCodeType(ImType t) {
        return t instanceof ImSimpleType st && "code".equals(st.getTypename());
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
