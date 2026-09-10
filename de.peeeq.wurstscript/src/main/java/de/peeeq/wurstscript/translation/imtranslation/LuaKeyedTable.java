package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImAnyType;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImTypeVarRef;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Recognises the compiler-owned {@code KeyedTable} membership operations.
 *
 * <p>Jass has no hashing, so every keyed structure in the library bottoms out in the hashtable
 * natives. Those take a (parent, child) pair and are emitted on Lua as a nested table plus a nil
 * check, so a membership test costs a call and two indexes on a runtime that is already a hash
 * table - a Jass compromise carried into Lua, which AGENTS.md section 7 asks us not to do. On Lua
 * each keyed table is its own table and the element is the key, so each operation is one index.
 *
 * <p>Matching is by <b>declaration</b>, not by name: a function qualifies only if it is annotated
 * {@code @compilerintrinsic} and its IM signature is exactly the one the lowering assumes.
 * Name-only matching would silently replace the body of any user function that happened to share
 * the name, including one with different types, which section 7 rules out - semantic identity must
 * come from the declaration and its structural signature, never from string comparison.
 *
 * <p><b>Iteration is deliberately absent.</b> Enumerating a Lua table needs {@code pairs()}, whose
 * order follows internal hash layout and so differs between clients, which desyncs a lockstep
 * game. Anything that must be iterated needs a separately maintained insertion-ordered array -
 * what SparseSet's dense half provides. This primitive is membership only.
 */
public final class LuaKeyedTable {

    /** Source-level names of the operations. Necessary to identify them, never sufficient. */
    private static final String CREATE = "keyedTableCreate";
    private static final String ADD = "keyedTableAdd";
    private static final String CONTAINS = "keyedTableContains";
    private static final String REMOVE = "keyedTableRemove";

    /** Stub names whose Lua bodies live in {@code LuaNatives}. */
    public static final String NATIVE_CREATE = "__wurst_keyedTableCreate";
    public static final String NATIVE_ADD = "__wurst_keyedTableAdd";
    public static final String NATIVE_CONTAINS = "__wurst_keyedTableContains";
    public static final String NATIVE_REMOVE = "__wurst_keyedTableRemove";

    private LuaKeyedTable() {
    }

    /**
     * The {@code __wurst_} stub {@code f} should be lowered to on Lua, or null if {@code f} is not
     * a compiler-owned KeyedTable operation.
     */
    public static String nativeStubFor(ImFunction f) {
        if (!(f.attrTrace() instanceof FuncDef fd)
            || !fd.attrHasAnnotation(CompilerIntrinsics.ANNOTATION)) {
            return null;
        }
        int params = f.getParameters().size();
        return switch (fd.getName()) {
            case CREATE -> params == 0 && TypesHelper.isIntType(f.getReturnType())
                ? NATIVE_CREATE : null;
            case ADD -> isKeyedPair(f) && f.getReturnType() instanceof ImVoid
                ? NATIVE_ADD : null;
            case REMOVE -> isKeyedPair(f) && f.getReturnType() instanceof ImVoid
                ? NATIVE_REMOVE : null;
            case CONTAINS -> isKeyedPair(f) && TypesHelper.isBoolType(f.getReturnType())
                ? NATIVE_CONTAINS : null;
            default -> null;
        };
    }

    /**
     * A (table, key) parameter pair. Both are {@code int} at source level: on Jass everything is an
     * integer anyway, and on Lua {@code castTo int} is the identity for class types, so the value
     * arriving here is already a usable table key with reference identity.
     */
    private static boolean isKeyedPair(ImFunction f) {
        return f.getParameters().size() == 2
            && TypesHelper.isIntType(f.getParameters().get(0).getType())
            && isKeyType(f.getParameters().get(1).getType());
    }

    /**
     * Types a key may have.
     *
     * <p>A {@code T:} type parameter is the point of this: new generics are erased on Lua rather
     * than squeezed through {@code castTo int} the way the old {@code <T>} containers are, so the
     * value arriving here is the element itself and becomes the table key directly - which is what
     * makes native Lua hashing possible at all. {@code int} stays accepted for keys that are
     * already integers, and an erased type parameter can also present as ImAnyType by this point.
     */
    private static boolean isKeyType(ImType t) {
        return TypesHelper.isIntType(t) || t instanceof ImTypeVarRef || t instanceof ImAnyType;
    }
}
