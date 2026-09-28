package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Lowers new-generic KeyedMap value operations to the existing integer Jass fallback after
 * generic and class elimination. At that point int values and class references both have Jass's
 * integer representation, while Lua lowers the same intrinsic declarations to native tables.
 */
public final class JassKeyedMapLowering {

    private static final String PUT_NATIVE = "keyedMapPutNative";
    private static final String GET_NATIVE = "keyedMapGetNative";
    private static final String PUT = "keyedMapPut";
    private static final String GET_INT = "keyedMapGetInt";

    private JassKeyedMapLowering() {
    }

    public static void transform(ImProg prog) {
        for (ImFunction f : prog.getFunctions()) {
            String name = intrinsicName(f);
            if (name == null || !f.getTypeVariables().isEmpty()) {
                continue;
            }
            if (PUT_NATIVE.equals(name)) {
                lowerPut(prog, f);
            } else if (GET_NATIVE.equals(name)) {
                lowerGet(prog, f);
            }
        }
    }

    /**
     * Whether f is the generic put intrinsic as the interpreter meets it: still generic, so this pass
     * has not given it its body yet. The type-variable test comes first, because the interpreter asks
     * on every call and it rules out almost everything.
     */
    public static boolean isUnloweredPutNative(ImFunction f) {
        return !f.getTypeVariables().isEmpty() && PUT_NATIVE.equals(intrinsicName(f));
    }

    /** Whether f is the generic get intrinsic, not yet lowered. */
    public static boolean isUnloweredGetNative(ImFunction f) {
        return !f.getTypeVariables().isEmpty() && GET_NATIVE.equals(intrinsicName(f));
    }

    /**
     * The int fallback that the lowered put or get calls, found the way this pass finds it. Checks the
     * (map, key[, value]) shape first and raises this pass's diagnostic when it is wrong, so running the
     * program before the lowering cannot bypass it.
     */
    public static ImFunction fallbackOf(ImProg prog, ImFunction f) {
        boolean put = PUT_NATIVE.equals(intrinsicName(f));
        boolean shaped = f.getParameters().size() == (put ? 3 : 2)
            && TypesHelper.isIntType(f.getParameters().get(0).getType())
            && (!put || f.getReturnType() instanceof ImVoid);
        if (!shaped) {
            throw invalidSpecialization(f, put
                ? "keyedMapPutNative requires an int map, a handle key, and an int-represented value"
                : "keyedMapGetNative requires an int map, a handle key, and an int-represented result");
        }
        return findFallback(prog, packageOf(f), f, put ? PUT : GET_INT, put);
    }

    /**
     * Raises this pass's diagnostic unless a call's resolved types are the ones it lowers: a handle
     * key, and a value with Jass's integer representation, an int or a class reference. The
     * interpreter checks each call with it, so a specialization the Jass build rejects cannot pass
     * there.
     */
    public static void checkSpecialization(ImFunction f, ImType keyType, ImType valueType) {
        if (LuaNativeLowering.isHandleType(keyType)
            && (TypesHelper.isIntType(valueType) || valueType instanceof ImClassType)) {
            return;
        }
        throw invalidSpecialization(f, PUT_NATIVE.equals(intrinsicName(f))
            ? "keyedMapPutNative requires an int map, a handle key, and an int-represented value"
            : "keyedMapGetNative requires an int map, a handle key, and an int-represented result");
    }

    private static void lowerPut(ImProg prog, ImFunction f) {
        if (f.getParameters().size() != 3 || !(f.getReturnType() instanceof ImVoid)
            || !TypesHelper.isIntType(f.getParameters().get(0).getType())
            || !LuaNativeLowering.isHandleType(f.getParameters().get(1).getType())
            || !TypesHelper.isIntType(f.getParameters().get(2).getType())) {
            throw invalidSpecialization(f, "keyedMapPutNative requires an int map, a handle key, and an int-represented value");
        }
        ImFunction fallback = findFallback(prog, packageOf(f), f, PUT, true);
        ImVar map = f.getParameters().get(0);
        ImVar key = f.getParameters().get(1);
        ImVar value = f.getParameters().get(2);
        f.getBody().clear();
        f.getLocals().clear();
        f.getBody().add(JassIm.ImFunctionCall(f.attrTrace(), fallback, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(map), JassIm.ImVarAccess(key), JassIm.ImVarAccess(value)),
            false, CallType.NORMAL));
    }

    private static void lowerGet(ImProg prog, ImFunction f) {
        if (f.getParameters().size() != 2 || !TypesHelper.isIntType(f.getReturnType())
            || !TypesHelper.isIntType(f.getParameters().get(0).getType())
            || !LuaNativeLowering.isHandleType(f.getParameters().get(1).getType())) {
            throw invalidSpecialization(f, "keyedMapGetNative requires an int map, a handle key, and an int-represented result");
        }
        ImFunction fallback = findFallback(prog, packageOf(f), f, GET_INT, false);
        ImVar map = f.getParameters().get(0);
        ImVar key = f.getParameters().get(1);
        ImExpr call = JassIm.ImFunctionCall(f.attrTrace(), fallback, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImVarAccess(map), JassIm.ImVarAccess(key)), false, CallType.NORMAL);
        f.getBody().clear();
        f.getLocals().clear();
        f.getBody().add(JassIm.ImReturn(f.attrTrace(), call));
    }

    private static ImFunction findFallback(ImProg prog, WPackage owner, ImFunction source, String name, boolean put) {
        if (owner != null) {
            for (ImFunction candidate : prog.getFunctions()) {
                if (!name.equals(intrinsicName(candidate)) || packageOf(candidate) != owner) {
                    continue;
                }
                boolean valid = put
                    ? candidate.getParameters().size() == 3
                        && TypesHelper.isIntType(candidate.getParameters().get(0).getType())
                        && isHandle(candidate.getParameters().get(1).getType())
                        && TypesHelper.isIntType(candidate.getParameters().get(2).getType())
                        && candidate.getReturnType() instanceof ImVoid
                    : candidate.getParameters().size() == 2
                        && TypesHelper.isIntType(candidate.getParameters().get(0).getType())
                        && isHandle(candidate.getParameters().get(1).getType())
                        && TypesHelper.isIntType(candidate.getReturnType());
                if (!valid) {
                    throw new CompileError(candidate.attrTrace().attrErrorPos(),
                        name + " must keep the existing int-map/handle-key signature.");
                }
                return candidate;
            }
        }
        throw new CompileError(source.attrTrace().attrErrorPos(),
            "The package declaring " + (put ? PUT_NATIVE : GET_NATIVE)
                + " must also declare the existing " + name + " Jass fallback.");
    }

    private static boolean isHandle(ImType type) {
        return type instanceof ImSimpleType simple && "handle".equals(simple.getTypename());
    }

    private static String intrinsicName(ImFunction f) {
        return f.attrTrace() instanceof FuncDef fd
            && fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION)
            ? fd.getName()
            : null;
    }

    private static WPackage packageOf(ImFunction f) {
        return f.attrTrace() instanceof FuncDef fd && fd.attrNearestPackage() instanceof WPackage p
            ? p
            : null;
    }

    private static CompileError invalidSpecialization(ImFunction f, String message) {
        return new CompileError(f.attrTrace().attrErrorPos(), message + ".");
    }
}
