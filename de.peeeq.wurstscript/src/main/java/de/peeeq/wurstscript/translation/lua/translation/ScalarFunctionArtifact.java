package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.EliminateLocalTypes;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.types.TypesHelper;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

import static de.peeeq.wurstscript.jassIm.JassIm.*;

/** Closed scalar IM: owned variable slots and control flow, with no external symbol operands or mutable traces. */
record ScalarFunctionArtifact(String imName, List<Variable> parameters, List<Variable> locals, List<Statement> body) {
    public ScalarFunctionArtifact {
        parameters = List.copyOf(parameters);
        locals = List.copyOf(locals);
        body = List.copyOf(body);
    }

    enum Type {
        INT, BOOL;
        ImType materialise() { return (this == INT ? TypesHelper.imInt() : TypesHelper.imBool()).copy(); }
        static @Nullable Type freeze(ImType type) {
            return EliminateLocalTypes.isIntegerOrLocalInteger(type) ? INT
                : EliminateLocalTypes.isBooleanOrLocalBoolean(type) ? BOOL : null;
        }
    }
    record Variable(String name, Type type) {
        ImVar materialise() { return ImVar(Ast.NoExpr(), type.materialise(), name, false); }
    }

    sealed interface Expression permits Literal, BoolLiteral, Slot, Operation {
        ImExpr materialise(List<ImVar> variables);
        boolean sameShape(Expression other);
    }
    record Literal(int value) implements Expression {
        public ImExpr materialise(List<ImVar> variables) { return ImIntVal(value); }
        public boolean sameShape(Expression other) { return other instanceof Literal; }
    }
    record BoolLiteral(boolean value) implements Expression {
        public ImExpr materialise(List<ImVar> variables) { return ImBoolVal(value); }
        public boolean sameShape(Expression other) { return other instanceof BoolLiteral; }
    }
    record Slot(int index) implements Expression {
        public ImExpr materialise(List<ImVar> variables) { return ImVarAccess(variables.get(index)); }
        public boolean sameShape(Expression other) { return other instanceof Slot s && index == s.index; }
    }
    record Operation(WurstOperator operator, List<Expression> arguments) implements Expression {
        public Operation { arguments = List.copyOf(arguments); }
        public ImExpr materialise(List<ImVar> variables) {
            return ImOperatorCall(operator, ImExprs(arguments.stream().map(a -> a.materialise(variables)).toList()));
        }
        public boolean sameShape(Expression other) {
            if (!(other instanceof Operation o) || operator != o.operator || arguments.size() != o.arguments.size()) return false;
            for (int i = 0; i < arguments.size(); i++) if (!arguments.get(i).sameShape(o.arguments.get(i))) return false;
            return true;
        }
    }

    sealed interface Statement permits Return, Assign, Branch, Loop, Exit, Sequence {
        ImStmt materialise(List<ImVar> variables);
        boolean sameShape(Statement other);
    }
    record Return(Expression value) implements Statement {
        public ImStmt materialise(List<ImVar> variables) { return ImReturn(Ast.NoExpr(), value.materialise(variables)); }
        public boolean sameShape(Statement other) { return other instanceof Return r && value.sameShape(r.value); }
    }
    record Assign(int target, Expression value) implements Statement {
        public ImStmt materialise(List<ImVar> variables) {
            return ImSet(Ast.NoExpr(), ImVarAccess(variables.get(target)), value.materialise(variables));
        }
        public boolean sameShape(Statement other) { return other instanceof Assign a && target == a.target && value.sameShape(a.value); }
    }
    record Branch(Expression condition, List<Statement> thenBlock, List<Statement> elseBlock) implements Statement {
        public Branch { thenBlock = List.copyOf(thenBlock); elseBlock = List.copyOf(elseBlock); }
        public ImStmt materialise(List<ImVar> variables) {
            return ImIf(Ast.NoExpr(), condition.materialise(variables), materialiseBlock(thenBlock, variables), materialiseBlock(elseBlock, variables));
        }
        public boolean sameShape(Statement other) {
            return other instanceof Branch b && condition.sameShape(b.condition)
                && sameBlock(thenBlock, b.thenBlock) && sameBlock(elseBlock, b.elseBlock);
        }
    }
    record Loop(List<Statement> body) implements Statement {
        public Loop { body = List.copyOf(body); }
        public ImStmt materialise(List<ImVar> variables) { return ImLoop(Ast.NoExpr(), materialiseBlock(body, variables)); }
        public boolean sameShape(Statement other) { return other instanceof Loop l && sameBlock(body, l.body); }
    }
    record Exit(Expression condition) implements Statement {
        public ImStmt materialise(List<ImVar> variables) { return ImExitwhen(Ast.NoExpr(), condition.materialise(variables)); }
        public boolean sameShape(Statement other) { return other instanceof Exit e && condition.sameShape(e.condition); }
    }
    record Sequence(List<Statement> body) implements Statement {
        public Sequence { body = List.copyOf(body); }
        public ImStmt materialise(List<ImVar> variables) { return ImHelper.statementExprVoid(materialiseBlock(body, variables)); }
        public boolean sameShape(Statement other) { return other instanceof Sequence s && sameBlock(body, s.body); }
    }

    public static @Nullable ScalarFunctionArtifact freeze(ImFunction function) {
        if (!(function.getTrace() instanceof FuncDef source)
            || !(source.getParent().getParent() instanceof WPackage) || !source.getTypeParameters().isEmpty()) return null;
        return freezeOwned(function);
    }

    private static @Nullable ScalarFunctionArtifact freezeOwned(ImFunction function) {
        if (!function.getTypeVariables().isEmpty() || !function.getFlags().isEmpty()
            || !TypesHelper.isIntType(function.getReturnType()) || function.getParameters().size() > 32) return null;
        Map<ImVar, Integer> slots = new IdentityHashMap<>();
        List<Variable> parameters = freezeVariables(function.getParameters(), slots);
        List<Variable> locals = freezeVariables(function.getLocals(), slots);
        if (parameters == null || locals == null) return null;
        List<Statement> body = freezeBlock(function.getBody(), slots);
        return body == null ? null : new ScalarFunctionArtifact(function.getName(), parameters, locals, body);
    }

    private static @Nullable List<Variable> freezeVariables(ImVars variables, Map<ImVar, Integer> slots) {
        List<Variable> result = new ArrayList<>();
        for (ImVar variable : variables) {
            Type type = Type.freeze(variable.getType());
            if (type == null || variable.getIsBJ()) return null;
            slots.put(variable, slots.size());
            result.add(new Variable(variable.getName(), type));
        }
        return result;
    }

    private static @Nullable List<Statement> freezeBlock(ImStmts statements, Map<ImVar, Integer> slots) {
        List<Statement> result = new ArrayList<>();
        for (ImStmt statement : statements) {
            if (statement instanceof ImNull) continue; // Start/end markers carry no runtime operation in this mode.
            Statement frozen = null;
            if (statement instanceof ImReturn ret && ret.getReturnValue() instanceof ImExpr value) {
                Expression expression = freezeExpression(value, slots);
                if (expression != null) frozen = new Return(expression);
            } else if (statement instanceof ImSet set && set.getLeft() instanceof ImVarAccess access) {
                Integer slot = slots.get(access.getVar());
                Expression value = freezeExpression(set.getRight(), slots);
                if (slot != null && value != null) frozen = new Assign(slot, value);
            } else if (statement instanceof ImIf branch) {
                Expression condition = freezeExpression(branch.getCondition(), slots);
                List<Statement> thenBlock = freezeBlock(branch.getThenBlock(), slots), elseBlock = freezeBlock(branch.getElseBlock(), slots);
                if (condition != null && thenBlock != null && elseBlock != null) frozen = new Branch(condition, thenBlock, elseBlock);
            } else if (statement instanceof ImLoop loop) {
                List<Statement> body = freezeBlock(loop.getBody(), slots);
                if (body != null) frozen = new Loop(body);
            } else if (statement instanceof ImExitwhen exit) {
                Expression condition = freezeExpression(exit.getCondition(), slots);
                if (condition != null) frozen = new Exit(condition);
            } else if (statement instanceof ImStatementExpr sequence && sequence.getExpr() instanceof ImNull) {
                List<Statement> body = freezeBlock(sequence.getStatements(), slots);
                if (body != null) frozen = new Sequence(body);
            }
            if (frozen == null) return null;
            result.add(frozen);
        }
        return result;
    }

    private static @Nullable Expression freezeExpression(ImExpr expression, Map<ImVar, Integer> slots) {
        if (expression instanceof ImIntVal literal) return new Literal(literal.getValI());
        if (expression instanceof ImBoolVal literal) return new BoolLiteral(literal.getValB());
        if (expression instanceof ImVarAccess access) {
            Integer slot = slots.get(access.getVar());
            if (slot != null) return new Slot(slot);
        } else if (expression instanceof ImOperatorCall call) {
            boolean supported = switch (call.getOp()) {
                case PLUS, MINUS, MULT, EQ, NOTEQ, LESS, LESS_EQ, GREATER, GREATER_EQ, AND, OR -> call.getArguments().size() == 2;
                case NOT, UNARY_MINUS -> call.getArguments().size() == 1;
                default -> false;
            };
            if (!supported) return null;
            List<Expression> arguments = new ArrayList<>();
            for (ImExpr argument : call.getArguments()) {
                Expression frozen = freezeExpression(argument, slots);
                if (frozen == null) return null;
                arguments.add(frozen);
            }
            return new Operation(call.getOp(), arguments);
        }
        return null;
    }

    private static boolean sameBlock(List<Statement> first, List<Statement> second) {
        if (first.size() != second.size()) return false;
        for (int i = 0; i < first.size(); i++) if (!first.get(i).sameShape(second.get(i))) return false;
        return true;
    }
    public boolean sameShape(ScalarFunctionArtifact other) {
        return imName.equals(other.imName) && parameters.equals(other.parameters) && locals.equals(other.locals) && sameBlock(body, other.body);
    }
    private static ImStmts materialiseBlock(List<Statement> body, List<ImVar> variables) {
        return ImStmts(body.stream().map(s -> s.materialise(variables)).toList());
    }

    private record Lowered(ImFunction function, ImTranslator context) { }

    /** Only owned scratch IM reaches these shared body-local passes. */
    private Lowered lower(RunArgs args) {
        ImVars ownedParameters = ImVars(parameters.stream().map(Variable::materialise).toList());
        ImVars ownedLocals = ImVars(locals.stream().map(Variable::materialise).toList());
        List<ImVar> variables = new ArrayList<>(ownedParameters);
        variables.addAll(ownedLocals);
        ImFunction function = ImFunction(Ast.NoExpr(), imName, ImTypeVars(), ownedParameters, TypesHelper.imInt().copy(), ownedLocals,
            materialiseBlock(body, variables), new ArrayList<>());
        ImTranslator context = new ImTranslator(Ast.WurstModel(), false, args);
        context.imProg().getFunctions().add(function);
        function.flatten(context);
        EliminateLocalTypes.eliminateLocalTypesFunc(function);
        return new Lowered(function, context);
    }

    /** Cold-only admission oracle: passes not ported here must leave exactly this lowered body. */
    public @Nullable ScalarFunctionArtifact loweredArtifact(RunArgs args) { return freezeOwned(lower(args).function); }

    public String emit(LuaTranslator.FunctionFrame frame, RunArgs args) {
        Lowered lowered = lower(args);
        return LuaTranslator.emitOwnedScalar(lowered.function, frame, lowered.context);
    }
}
