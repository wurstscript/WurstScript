package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.types.WurstTypeString;

import java.util.Collections;

/**
 * Refuses a destroy through a stale reference on Lua, and tells the map about it.
 *
 * <p>On Lua every class shares one id space and an allocation hands out the most recently freed id, so a reference
 * kept past its object's destroy can name an object of any class later. A destroy through it would run that object's
 * destructor and free its id while the object is still in use, and the next allocation would share the id. Each
 * {@code destroy} therefore checks, before it dispatches and before any {@code ondestroy} runs, that its object is
 * still an instance of the type the caller destroys it as. The receiver is evaluated once. When the check fails,
 * {@code __wurst_staleDestroy} records {@code count|id|class|destroyed as|file:line} and raises; nothing is destroyed.
 * An id that is free (a plain double destroy) passes on to the deallocator, which reports it as before.
 *
 * <p>Not covered: a stale reference whose id was reused by an object of the same class or a subclass passes, since
 * the check sees only the class. Jass is not checked either: it keeps a connected id pool per class and interface
 * group, so a stale reference there names an object of a related type, while Lua keeps one global allocator for all
 * classes. This check does not make the two allocators agree.
 *
 * <p>A map reads the record through {@code staleDestroyReport() returns string}, matched by declaration like the list
 * operations: the annotation, the name, no parameters and a string result, of a plain package function (see
 * {@link IntrinsicDeclarations}). The source body is what Jass and an older compiler run, and should answer "".
 */
public final class LuaStaleDestroy {

    private static final String REPORT = "staleDestroyReport";

    /** The stub name whose Lua body lives in {@code LuaNatives}. */
    public static final String NATIVE_REPORT = "__wurst_staleDestroyReport";

    /** The check's failure path, whose Lua body lives in {@code LuaNatives}. */
    public static final String NATIVE_CHECK = "__wurst_staleDestroy";

    /** The global the failure path writes the record to and the report reads. */
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

    /**
     * Adds to {@code stmts} the check of {@code object}, a local the destroyed object was evaluated into, against
     * {@code type}, the type the caller destroys it as.
     */
    static void addCheck(ImTranslator t, Expr trace, ImStmts stmts, ImVar object, ImClassType type,
                         StructureDef destroyedAs) {
        ImExpr notNull = JassIm.ImOperatorCall(WurstOperator.NOTEQ,
            JassIm.ImExprs(JassIm.ImVarAccess(object), JassIm.ImNull(object.getType())));
        // The class alone, without its type arguments: a parameterized instanceof observes the identity of an
        // instantiation and makes EliminateGenerics specialise for it, which a check must not change. Every
        // instantiation of an erased class is an instance of the class.
        ImClassType erased = JassIm.ImClassType(type.getClassDef(), JassIm.ImTypeArguments());
        ImExpr notInstance = JassIm.ImOperatorCall(WurstOperator.NOT,
            JassIm.ImExprs(JassIm.ImInstanceof(JassIm.ImVarAccess(object), erased)));
        ImExpr fails = JassIm.ImOperatorCall(WurstOperator.AND, JassIm.ImExprs(notNull, notInstance));
        // The site is added by the Lua translation, from the call's trace: a cached function keeps its IM
        // across an edit that moves it, while its traces are refreshed.
        stmts.add(JassIm.ImIf(trace, fails, JassIm.ImStmts(JassIm.ImFunctionCall(trace, checkStub(t, trace),
            JassIm.ImTypeArguments(), JassIm.ImExprs(JassIm.ImVarAccess(object),
                JassIm.ImStringVal(destroyedAs.getName())), false, CallType.NORMAL)),
            JassIm.ImStmts()));
    }

    /** {@code File.wurst:line} of {@code trace}, without the checkout path. */
    public static String site(de.peeeq.wurstscript.ast.Element trace) {
        WPos pos = trace.attrSource();
        String file = pos.getFile().replace('\\', '/');
        return file.substring(file.lastIndexOf('/') + 1) + ":" + pos.getLine();
    }

    private static ImFunction checkStub(ImTranslator t, Expr trace) {
        for (ImFunction f : t.getImProg().getFunctions()) {
            if (f.getName().equals(NATIVE_CHECK) && f.isNative()) {
                return f;
            }
        }
        ImFunction stub = JassIm.ImFunction(trace, NATIVE_CHECK, JassIm.ImTypeVars(),
            JassIm.ImVars(JassIm.ImVar(trace, JassIm.ImAnyType(), "object", false),
                JassIm.ImVar(trace, WurstTypeString.instance().imTranslateType(t), "expected", false)),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
        t.getImProg().getFunctions().add(stub);
        return stub;
    }
}
