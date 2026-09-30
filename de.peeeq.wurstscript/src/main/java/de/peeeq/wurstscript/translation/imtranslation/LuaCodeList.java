package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Recognises the compiler-owned {@code CodeList} operations: an ordered list of {@code code}
 * values which is run right here, in the current thread, the way a trigger's conditions are.
 *
 * <p>Jass cannot keep a {@code code} value in an array or a field, and can only run one through a
 * trigger or {@code ExecuteFunc}. The library's declarations do exactly that: a list is a trigger,
 * adding is {@code TriggerAddCondition} and running is {@code TriggerEvaluate}. On Lua a
 * {@code code} value is a function, a list is a table of them and running is one call each; a
 * trigger round trip through the engine costs about thirty times that, which is what an event with
 * a few listeners pays on every dispatch. The list is one table per list, and running walks its
 * array part, so the order is the order of adding and does not depend on any hash layout. A value
 * added while the list runs is reached in the same run, and the result of a value is ignored: both as
 * for a trigger's conditions, measured in the game for the second (it runs every condition after one
 * that returned false). The list is not a trigger a value can observe: GetTriggeringTrigger and the
 * trigger counters are not supported inside one.
 *
 * <p>Matching is by declaration, as for the keyed maps: the function has to be annotated
 * {@code @compilerintrinsic} and have the exact signature the stub assumes. The values run must
 * not wait, as a condition must not; run as direct calls they make no promise about what a wait
 * does. An error in one is reported by the callback adapter around every function reference, so
 * the next one still runs, as it does behind a trigger.
 */
public final class LuaCodeList {

    private static final String CREATE = "codeListCreate";
    private static final String ADD = "codeListAdd";
    private static final String RUN = "codeListRun";

    /** Stub names whose Lua bodies live in {@code LuaNatives}. */
    public static final String NATIVE_CREATE = "__wurst_codeListCreate";
    public static final String NATIVE_ADD = "__wurst_codeListAdd";
    public static final String NATIVE_RUN = "__wurst_codeListRun";

    private LuaCodeList() {
    }

    /** The {@code __wurst_} stub {@code f} is lowered to on Lua, or null if it is not a list operation. */
    public static String nativeStubFor(ImFunction f) {
        FuncDef fd = IntrinsicDeclarations.plainIntrinsic(f);
        if (fd == null) {
            return null;
        }
        int params = f.getParameters().size();
        return switch (fd.getName()) {
            case CREATE -> params == 0 && TypesHelper.isIntType(f.getReturnType()) ? NATIVE_CREATE : null;
            case ADD -> params == 2 && isListHandle(f, 0) && isCode(f.getParameters().get(1).getType())
                && f.getReturnType() instanceof ImVoid ? NATIVE_ADD : null;
            case RUN -> params == 1 && isListHandle(f, 0) && f.getReturnType() instanceof ImVoid
                ? NATIVE_RUN : null;
            default -> null;
        };
    }

    /** A list is an {@code int} in source: on Lua {@code castTo int} of a table is the table itself. */
    private static boolean isListHandle(ImFunction f, int index) {
        return TypesHelper.isIntType(f.getParameters().get(index).getType());
    }

    private static boolean isCode(ImType t) {
        return t instanceof ImSimpleType simple && "code".equals(simple.getTypename());
    }
}
