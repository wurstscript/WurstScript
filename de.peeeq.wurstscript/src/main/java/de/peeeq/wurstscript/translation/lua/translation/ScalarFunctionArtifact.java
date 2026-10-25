package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

import static de.peeeq.wurstscript.jassIm.JassIm.*;

/** Immutable, closed scalar IM: owned parameter slots and integer operations, with no external symbol operands. */
record ScalarFunctionArtifact(String imName, List<String> parameters, Expression result) {
    public ScalarFunctionArtifact { parameters = List.copyOf(parameters); }

    public sealed interface Expression permits Literal, Parameter, Binary {
        ImExpr materialise(ImVars parameters);
        boolean sameShape(Expression other);
    }
    public record Literal(int value) implements Expression {
        public ImExpr materialise(ImVars parameters) { return ImIntVal(value); }
        public boolean sameShape(Expression other) { return other instanceof Literal; }
    }
    public record Parameter(int slot) implements Expression {
        public ImExpr materialise(ImVars parameters) { return ImVarAccess(parameters.get(slot)); }
        public boolean sameShape(Expression other) { return other instanceof Parameter p && slot == p.slot; }
    }
    public record Binary(WurstOperator operator, Expression left, Expression right) implements Expression {
        public ImExpr materialise(ImVars parameters) {
            return ImOperatorCall(operator, ImExprs(left.materialise(parameters), right.materialise(parameters)));
        }
        public boolean sameShape(Expression other) {
            return other instanceof Binary b && operator == b.operator && left.sameShape(b.left) && right.sameShape(b.right);
        }
    }

    public static @Nullable ScalarFunctionArtifact freeze(ImFunction function) {
        if (!(function.getTrace() instanceof FuncDef source)
            || !(source.getParent().getParent() instanceof WPackage)
            || !source.getTypeParameters().isEmpty()
            || !function.getTypeVariables().isEmpty() || !function.getFlags().isEmpty()
            || !TypesHelper.isIntType(function.getReturnType()) || !function.getLocals().isEmpty()
            || function.getParameters().size() > 32
            || function.getParameters().stream().anyMatch(p -> !TypesHelper.isIntType(p.getType()))) return null;
        ImReturn ret = null;
        for (ImStmt statement : function.getBody()) {
            if (statement instanceof ImReturn value && ret == null) ret = value;
            else if (!(statement instanceof ImNull)) return null;
        }
        if (ret == null || !(ret.getReturnValue() instanceof ImExpr expression)) return null;
        Expression frozen = freezeExpression(expression, function.getParameters());
        return frozen == null ? null : new ScalarFunctionArtifact(function.getName(),
            function.getParameters().stream().map(ImVar::getName).toList(), frozen);
    }

    private static @Nullable Expression freezeExpression(ImExpr expression, ImVars parameters) {
        if (expression instanceof ImIntVal literal) return new Literal(literal.getValI());
        if (expression instanceof ImVarAccess access) {
            for (int i = 0; i < parameters.size(); i++) if (access.getVar() == parameters.get(i)) return new Parameter(i);
        } else if (expression instanceof ImOperatorCall call && call.getArguments().size() == 2
            && (call.getOp() == WurstOperator.PLUS || call.getOp() == WurstOperator.MINUS || call.getOp() == WurstOperator.MULT)) {
            Expression left = freezeExpression(call.getArguments().get(0), parameters);
            Expression right = freezeExpression(call.getArguments().get(1), parameters);
            if (left != null && right != null) return new Binary(call.getOp(), left, right);
        }
        return null;
    }

    public boolean sameShape(ScalarFunctionArtifact other) {
        return imName.equals(other.imName) && parameters.equals(other.parameters) && result.sameShape(other.result);
    }

    /** All other lowering stages are identity operations on this closed subset; flatten and the emitter are shared. */
    public String emit(LuaTranslator.FunctionFrame frame, RunArgs args) {
        var trace = Ast.NoExpr();
        ImVars ownedParameters = ImVars();
        for (String name : parameters) ownedParameters.add(ImVar(trace, TypesHelper.imInt().copy(), name, false));
        ImFunction function = ImFunction(trace, imName, ImTypeVars(), ownedParameters, TypesHelper.imInt().copy(), ImVars(),
            ImStmts(ImReturn(trace, result.materialise(ownedParameters))), new ArrayList<>());
        ImTranslator context = new ImTranslator(Ast.WurstModel(), false, args);
        context.imProg().getFunctions().add(function);
        function.flatten(context);
        return LuaTranslator.emitOwnedScalar(function, frame, context);
    }
}
