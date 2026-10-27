package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.*;

import java.util.*;

/** Owns complete, independent callback registration statements before lowering can split their temporaries. */
final class HotReloadInitActions {
    private HotReloadInitActions() { }

    static void translate(InitBlock block, ImTranslator translator, ImFunction init) {
        Map<String, Integer> occurrences = new LinkedHashMap<>();
        Map<ExprFunctionCall, String> identities = new IdentityHashMap<>();
        // Equal registrations in separate init blocks are ambiguous too. Keep such groups in the ordinary init.
        ((WPackage) block.attrNearestPackage()).accept(new de.peeeq.wurstscript.ast.Element.DefaultVisitor() {
            @Override public void visit(InitBlock candidate) {
                for (WStatement statement : candidate.getBody()) {
                    if (!(statement instanceof ExprFunctionCall call) || readsOutsideLocal(call)) continue;
                    String key = ClosureTranslator.registrationIdentity(call, translator);
                    if (key != null) { identities.put(call, key); occurrences.merge(key, 1, Integer::sum); }
                }
            }
        });
        for (WStatement statement : block.getBody()) {
            String key = statement instanceof ExprFunctionCall call ? identities.get(call) : null;
            if (key == null || occurrences.get(key) != 1) {
                init.getBody().addAll(translator.translateStatements(init, List.of(statement)));
                continue;
            }
            ImFunction action = JassIm.ImFunction(statement, "init_action_" + key, JassIm.ImTypeVars(), JassIm.ImVars(),
                JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), List.of(FunctionFlagEnum.HOT_RELOAD_INIT_ACTION));
            translator.imProg().getFunctions().add(action);
            action.getBody().addAll(translator.translateStatements(action, List.of(statement)));
            init.getBody().add(JassIm.ImFunctionCall(statement, action, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL));
        }
    }

    private static boolean readsOutsideLocal(ExprFunctionCall call) {
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
