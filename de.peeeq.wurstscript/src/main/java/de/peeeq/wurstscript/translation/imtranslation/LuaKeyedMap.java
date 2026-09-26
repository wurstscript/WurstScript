package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImAnyType;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImTypeVarRef;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Recognises the compiler-owned {@code KeyedMap} operations: a key-to-value table which on Lua is
 * one native table per map, the key being the element itself.
 *
 * <p>The old generic {@code HashMap<K, V>} squeezes every key and value through {@code castTo int}
 * typecasting, which on Lua means an index map that boxes numbers, retains every key forever and
 * costs several calls per lookup on top of the hashtable emulation. {@code UnitIndexer} and
 * {@code RegisterEvents} sit on that path in every map. A new-generics {@code K:} key arrives
 * erased, so it can be the table key directly - the same reasoning as {@link LuaKeyedTable}.
 *
 * <p>Reads come typed: {@code keyedMapGetInt} and friends answer the Wurst default for a missing
 * key inside the stub, so a caller declared to return {@code int} needs no nil normalisation.
 * The untyped {@code keyedMapGet} is for values whose default is nil (class instances, handles).
 *
 * <p>Matching is by declaration: the function has to be annotated {@code @compilerintrinsic} and
 * its IM signature has to be the one the stub assumes. A user function of the same name and a
 * different shape is left alone. No iteration is offered, for the reason given on the keyed table:
 * {@code pairs()} order is not the same on every client.
 */
public final class LuaKeyedMap {

    private static final String CREATE = "keyedMapCreate";
    private static final String PUT = "keyedMapPut";
    private static final String GET = "keyedMapGet";
    private static final String GET_INT = "keyedMapGetInt";
    private static final String GET_REAL = "keyedMapGetReal";
    private static final String GET_BOOL = "keyedMapGetBool";
    private static final String GET_STR = "keyedMapGetStr";
    private static final String HAS = "keyedMapHas";
    private static final String REMOVE = "keyedMapRemove";
    private static final String DESTROY = "keyedMapDestroy";

    public static final String NATIVE_CREATE = "__wurst_keyedMapCreate";
    public static final String NATIVE_PUT = "__wurst_keyedMapPut";
    public static final String NATIVE_GET = "__wurst_keyedMapGet";
    public static final String NATIVE_GET_INT = "__wurst_keyedMapGetInt";
    public static final String NATIVE_GET_REAL = "__wurst_keyedMapGetReal";
    public static final String NATIVE_GET_BOOL = "__wurst_keyedMapGetBool";
    public static final String NATIVE_GET_STR = "__wurst_keyedMapGetStr";
    public static final String NATIVE_HAS = "__wurst_keyedMapHas";
    public static final String NATIVE_REMOVE = "__wurst_keyedMapRemove";

    private LuaKeyedMap() {
    }

    /**
     * The {@code __wurst_} stub {@code f} is lowered to on Lua, or null if {@code f} is not a
     * compiler-owned KeyedMap operation of the expected shape.
     */
    public static String nativeStubFor(ImFunction f) {
        if (!(f.attrTrace() instanceof FuncDef fd)
            || !fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION)) {
            return null;
        }
        ImType result = f.getReturnType();
        return switch (fd.getName()) {
            case CREATE -> f.getParameters().isEmpty() && TypesHelper.isIntType(result)
                ? NATIVE_CREATE : null;
            case PUT -> isKeyedPair(f, 3) && isValueType(f.getParameters().get(2).getType())
                && result instanceof ImVoid
                ? NATIVE_PUT : null;
            case GET -> isKeyedPair(f, 2) && isNilDefaultedType(result) ? NATIVE_GET : null;
            case GET_INT -> isKeyedPair(f, 2) && TypesHelper.isIntType(result) ? NATIVE_GET_INT : null;
            case GET_REAL -> isKeyedPair(f, 2) && TypesHelper.isRealType(result) ? NATIVE_GET_REAL : null;
            case GET_BOOL -> isKeyedPair(f, 2) && TypesHelper.isBoolType(result) ? NATIVE_GET_BOOL : null;
            case GET_STR -> isKeyedPair(f, 2) && TypesHelper.isStringType(result) ? NATIVE_GET_STR : null;
            case HAS -> isKeyedPair(f, 2) && TypesHelper.isBoolType(result) ? NATIVE_HAS : null;
            case REMOVE -> isKeyedPair(f, 2) && result instanceof ImVoid ? NATIVE_REMOVE : null;
            default -> null;
        };
    }

    /** Whether {@code f} frees a keyed map; see {@link LuaKeyedTable#isDestroy}. */
    public static boolean isDestroy(ImFunction f) {
        return f.attrTrace() instanceof FuncDef fd
            && DESTROY.equals(fd.getName())
            && fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION)
            && f.getParameters().size() == 1
            && TypesHelper.isIntType(f.getParameters().get(0).getType())
            && f.getReturnType() instanceof ImVoid;
    }

    /** A (map, key, ...) parameter list of exactly {@code count} entries. */
    private static boolean isKeyedPair(ImFunction f, int count) {
        return f.getParameters().size() == count
            && TypesHelper.isIntType(f.getParameters().get(0).getType())
            && isKeyType(f.getParameters().get(1).getType());
    }

    /**
     * Key types: an int, an erased type parameter which is the element itself, or a concrete
     * handle type. The last lets a library declare the operations with a {@code unit} key and a
     * Jass body of {@code Table} plus {@code GetHandleId}, which is correct in source on every
     * compiler, so a build which does not lower the operations degrades to the hashtable path
     * instead of keying every element the same.
     */
    private static boolean isKeyType(ImType t) {
        return TypesHelper.isIntType(t) || t instanceof ImTypeVarRef || t instanceof ImAnyType
            || LuaNativeLowering.isHandleType(t);
    }

    /** Anything storable: a primitive, an erased type parameter, a class instance or a handle. */
    private static boolean isValueType(ImType t) {
        return t instanceof ImSimpleType || t instanceof ImTypeVarRef || t instanceof ImAnyType
            || t instanceof ImClassType;
    }

    /** Result types whose Wurst default is nil, so a bare table read already answers correctly. */
    private static boolean isNilDefaultedType(ImType t) {
        if (t instanceof ImTypeVarRef || t instanceof ImAnyType || t instanceof ImClassType) {
            return true;
        }
        return t instanceof ImSimpleType simple
            && !TypesHelper.isIntType(simple) && !TypesHelper.isRealType(simple)
            && !TypesHelper.isBoolType(simple) && !TypesHelper.isStringType(simple);
    }
}
