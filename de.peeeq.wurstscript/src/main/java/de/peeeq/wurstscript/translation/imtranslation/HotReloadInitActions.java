package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.WurstTypeBool;

import java.util.*;

/** Owns complete, independent callback registration statements before lowering can split their temporaries. */
final class HotReloadInitActions {
    private HotReloadInitActions() { }

    private record Guard(Expr condition, boolean thenBranch) { }
    private record Unit(WStatement statement, List<Guard> guards) { }

    static void translate(InitBlock block, ImTranslator translator, ImFunction init) {
        Map<String, Integer> occurrences = new LinkedHashMap<>();
        Map<FunctionCall, String> identities = new IdentityHashMap<>();
        // Equal registrations in separate blocks or branches are ambiguous too. Keep them in ordinary init code.
        ((WPackage) block.attrNearestPackage()).accept(new de.peeeq.wurstscript.ast.Element.DefaultVisitor() {
            @Override public void visit(InitBlock candidate) {
                for (Unit unit : units(candidate)) {
                    if (!(unit.statement() instanceof FunctionCall call) || readsOutsideLocal(call)) continue;
                    String key = ClosureTranslator.registrationIdentity(call, translator);
                    if (key != null) { identities.put(call, key); occurrences.merge(key, 1, Integer::sum); }
                }
            }
        });
        for (Unit unit : units(block)) {
            String key = unit.statement() instanceof FunctionCall call ? identities.get(call) : null;
            if (key == null || occurrences.get(key) != 1) {
                init.getBody().addAll(translate(unit, translator, init));
                continue;
            }
            ImFunction action = JassIm.ImFunction(unit.statement(), "init_action_" + key, JassIm.ImTypeVars(), JassIm.ImVars(),
                JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), List.of(FunctionFlagEnum.HOT_RELOAD_INIT_ACTION));
            translator.imProg().getFunctions().add(action);
            action.getBody().addAll(translate(unit, translator, action));
            init.getBody().add(JassIm.ImFunctionCall(unit.statement(), action, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL));
        }
    }

    private static List<Unit> units(InitBlock block) {
        List<Unit> result = new ArrayList<>();
        collect(block.getBody(), List.of(), result);
        return result;
    }

    private static void collect(WStatements statements, List<Guard> guards, List<Unit> result) {
        for (WStatement statement : statements) {
            if (statement instanceof StmtIf conditional && immutableGuard(conditional.getCond())) {
                List<Guard> path = new ArrayList<>(guards);
                path.add(new Guard(conditional.getCond(), true));
                collect(conditional.getThenBlock(), List.copyOf(path), result);
                path.set(path.size() - 1, new Guard(conditional.getCond(), false));
                collect(conditional.getElseBlock(), List.copyOf(path), result);
            } else {
                result.add(new Unit(statement, guards));
            }
        }
    }

    private static List<ImStmt> translate(Unit unit, ImTranslator translator, ImFunction function) {
        ImStmts body = JassIm.ImStmts(translator.translateStatements(function, List.of(unit.statement())));
        // Every leaf keeps its original path. These guards have no effects and cannot change during init, so
        // splitting them preserves order and lets later payloads add a registration without repeating its siblings.
        for (int i = unit.guards().size() - 1; i >= 0; i--) {
            Guard guard = unit.guards().get(i);
            body = JassIm.ImStmts(JassIm.ImIf(guard.condition(), guard.condition().imTranslateExpr(translator, function),
                guard.thenBranch() ? body : JassIm.ImStmts(), guard.thenBranch() ? JassIm.ImStmts() : body));
        }
        return body.removeAll();
    }

    private static boolean immutableGuard(Expr expression) {
        if (expression instanceof ExprBoolVal) return true;
        if (expression instanceof ExprVarAccess access && access.attrNameDef() instanceof GlobalVarDef global) {
            return global.attrIsConstant() && !global.attrIsDynamicClassMember() && access.attrTyp() instanceof WurstTypeBool;
        }
        if (expression instanceof ExprUnary unary && unary.getOpU() == WurstOperator.NOT) return immutableGuard(unary.getRight());
        if (expression instanceof ExprBinary binary && (binary.getOp() == WurstOperator.AND || binary.getOp() == WurstOperator.OR)) {
            return immutableGuard(binary.getLeft()) && immutableGuard(binary.getRight());
        }
        return false;
    }

    private static boolean readsOutsideLocal(FunctionCall call) {
        boolean[] outside = {false};
        call.accept(new de.peeeq.wurstscript.ast.Element.DefaultVisitor() {
            private void check(NameDef declaration) {
                if (!(declaration instanceof LocalVarDef) && !(declaration instanceof WParameter)) return;
                for (de.peeeq.wurstscript.ast.Element scope = declaration; scope != null; scope = scope.getParent()) {
                    if (scope == call) return;
                }
                outside[0] = true;
            }
            @Override public void visit(ExprVarAccess access) { check(access.attrNameDef()); }
            @Override public void visit(ExprVarArrayAccess access) { super.visit(access); check(access.attrNameDef()); }
        });
        return outside[0];
    }
}
