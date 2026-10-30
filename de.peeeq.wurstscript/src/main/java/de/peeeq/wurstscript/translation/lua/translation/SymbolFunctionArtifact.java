package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.*;

import java.util.*;

/** Generated, detached body graph. External operands are request-local symbols, never retained mutable IM. */
record SymbolFunctionArtifact(NodeGraphs.Snapshot body, String version, String shape, CallLoweringProducts products) {
    static boolean supports(ImFunction function) {
        if (function.isBj() || function.isNative() || function.isExtern() || !function.getTypeVariables().isEmpty()
            || function.getFlags().stream().anyMatch(flag -> !(flag instanceof FunctionFlagAnnotation)
                && flag != FunctionFlagEnum.PRESERVE_NAME) || !simple(function.getReturnType())) return false;
        for (ImVar variable : function.getParameters()) if (!simple(variable.getType())) return false;
        for (ImVar variable : function.getLocals()) if (!simple(variable.getType())) return false;
        ArrayDeque<Element> pending = new ArrayDeque<>(); pending.push(function.getBody());
        while (!pending.isEmpty()) {
            Element node = pending.pop();
            boolean supported = node instanceof ImStmts || node instanceof ImExprs || node instanceof ImIf
                || node instanceof ImLoop || node instanceof ImExitwhen || node instanceof ImReturn || node instanceof ImSet
                || node instanceof ImNoExpr || node instanceof ImStatementExpr || node instanceof ImVarAccess
                || node instanceof ImVarArrayAccess || node instanceof ImIntVal || node instanceof ImRealVal
                || node instanceof ImBoolVal || node instanceof ImStringVal || node instanceof ImNull
                || node instanceof ImFunctionCall || node instanceof ImOperatorCall || node instanceof ImTypeArguments;
            if (!supported) return false;
            if (node instanceof ImNull value && !simple(value.getType())) return false;
            if (node instanceof ElementWithVar access && !simple(access.getVar().getType())) return false;
            if (node instanceof ImFunctionCall call && (!call.getTypeArguments().isEmpty()
                || !call.getFunc().getTypeVariables().isEmpty() || !simple(call.getFunc().getReturnType())
                || call.getFunc().getParameters().stream().anyMatch(v -> !simple(v.getType()))
                || call.getCallType() != CallType.NORMAL)) return false;
            if (node instanceof ImOperatorCall call && (call.getOp() == WurstOperator.DIV_INT
                || call.getOp() == WurstOperator.MOD_INT || call.getOp() == WurstOperator.MOD_REAL
                || call.getOp() == WurstOperator.JASS_MOD_INT)) return false;
            for (int i = 0; i < node.size(); i++) pending.push(node.get(i));
        }
        return true;
    }
    private static boolean simple(ImType type) {
        return type instanceof ImSimpleType || type instanceof ImVoid
            || type instanceof ImArrayType array && array.getEntryType() instanceof ImSimpleType;
    }
    record Lowered(ImFunction function, ImTranslator context) { }
    Lowered lower(ImBackendInput input, RunArgs args) {
        ImFunction function = (ImFunction) body.materialize(input);
        ImTranslator context = new ImTranslator(Ast.WurstModel(), false, args);
        context.imProg().getFunctions().add(function);
        try (var scope = NodeOwnership.CHECKS ? NodeOwnership.scope(function) : null) {
            products.lower(function, input, context);
            function.flatten(context);
            EliminateLocalTypes.eliminateLocalTypesFunc(function);
        }
        return new Lowered(function, context);
    }
}
