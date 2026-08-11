package de.peeeq.wurstscript;

import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.attributes.AttrFuncDef;

/** Source-level compiler intrinsics which must be eliminated before backend emission. */
public final class CompilerIntrinsics {

    public static final String FOR_FIELDS = "wurstForFields";
    public static final String MAP_FIELDS = "wurstMapFields";
    public static final String NEW = "wurstNewInstance";
    private static final String LEGACY_FOR_FIELDS = "forFields";
    private static final String LEGACY_MAP_FIELDS = "mapFields";
    private static final String LEGACY_NEW = "newInstance";
    public static final String NEW_MARKER = "wurstNewMarker";
    public static final String ANNOTATION = "compilerintrinsic";

    private CompilerIntrinsics() {
    }

    public static boolean isForFields(ExprFunctionCall call) {
        return hasName(call, FOR_FIELDS, LEGACY_FOR_FIELDS)
            && hasClosureArgument(call)
            && !AttrFuncDef.hasApplicableUserFunction(call);
    }

    public static boolean isMapFields(ExprFunctionCall call) {
        return hasName(call, MAP_FIELDS, LEGACY_MAP_FIELDS)
            && hasClosureArgument(call)
            && !AttrFuncDef.hasApplicableUserFunction(call);
    }

    public static boolean isFieldIteration(ExprFunctionCall call) {
        return isForFields(call) || isMapFields(call);
    }

    public static boolean isNew(ExprFunctionCall call) {
        return hasName(call, NEW, LEGACY_NEW) && !AttrFuncDef.hasApplicableUserFunction(call);
    }

    private static boolean hasClosureArgument(ExprFunctionCall call) {
        return call.getArgs().stream().anyMatch(arg -> arg instanceof ExprClosure);
    }

    public static boolean isDeclaration(de.peeeq.wurstscript.ast.FunctionDefinition definition) {
        return definition.attrHasAnnotation(ANNOTATION);
    }

    private static boolean hasName(ExprFunctionCall call, String name, String legacyName) {
        return name.equals(call.getFuncName()) || legacyName.equals(call.getFuncName());
    }
}
