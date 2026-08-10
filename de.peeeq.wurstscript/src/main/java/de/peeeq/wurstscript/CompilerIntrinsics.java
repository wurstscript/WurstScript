package de.peeeq.wurstscript;

import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.attributes.AttrFuncDef;

/** Source-level compiler intrinsics which must be eliminated before backend emission. */
public final class CompilerIntrinsics {

    public static final String FOR_FIELDS = "forFields";
    public static final String MAP_FIELDS = "mapFields";
    public static final String NEW = "newInstance";
    public static final String NEW_MARKER = "wurstNewMarker";

    private CompilerIntrinsics() {
    }

    public static boolean isForFields(ExprFunctionCall call) {
        return FOR_FIELDS.equals(call.getFuncName())
            && hasClosureArgument(call)
            && !AttrFuncDef.hasApplicableUserFunction(call);
    }

    public static boolean isMapFields(ExprFunctionCall call) {
        return MAP_FIELDS.equals(call.getFuncName())
            && hasClosureArgument(call)
            && !AttrFuncDef.hasApplicableUserFunction(call);
    }

    public static boolean isFieldIteration(ExprFunctionCall call) {
        return isForFields(call) || isMapFields(call);
    }

    public static boolean isNew(ExprFunctionCall call) {
        return NEW.equals(call.getFuncName()) && !AttrFuncDef.hasApplicableUserFunction(call);
    }

    private static boolean hasClosureArgument(ExprFunctionCall call) {
        return call.getArgs().stream().anyMatch(arg -> arg instanceof ExprClosure);
    }
}
