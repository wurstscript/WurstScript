package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Recognises {@code staleDestroyReport() returns string}, which tells a map about the last destroy through a stale
 * reference.
 *
 * <p>On Lua every class shares one id space and an allocation hands out the most recently freed id, so a destroy
 * through a stale reference would free an object of any class. The deallocator refuses one whose object is not of
 * the class it is destroyed as: it raises, and before that records the destroy. The thread that raised has ended,
 * and an error reported to the map is only as reliable as the code that sees it, so a map reads the record through
 * this intrinsic instead, from code of its choosing: {@code count|class|destroyed as} of the last one, or the empty
 * string. The source body is what Jass and an older compiler run, and should answer the empty string.
 *
 * <p>Matching is by declaration, as for the list operations: the annotation, the name, no parameters and a string
 * result, of a plain package function (see {@link IntrinsicDeclarations}).
 */
public final class LuaStaleDestroy {

    private static final String REPORT = "staleDestroyReport";

    /** The stub name whose Lua body lives in {@code LuaNatives}. */
    public static final String NATIVE_REPORT = "__wurst_staleDestroyReport";

    /** The global the deallocator writes the record to and the stub reads. */
    public static final String RECORD = "__wurst_staleDestroyRecord";

    /** The global counting the stale destroys. */
    public static final String COUNT = "__wurst_staleDestroyCount";

    private LuaStaleDestroy() {
    }

    /** The {@code __wurst_} stub {@code f} is lowered to on Lua, or null if it is not the report. */
    public static String nativeStubFor(ImFunction f) {
        FuncDef fd = IntrinsicDeclarations.plainIntrinsic(f);
        if (fd == null || !REPORT.equals(fd.getName())) {
            return null;
        }
        return f.getParameters().isEmpty() && TypesHelper.isStringType(f.getReturnType()) ? NATIVE_REPORT : null;
    }
}
