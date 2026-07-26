package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Conservative, flow-insensitive analysis for values and functions which may
 * depend on {@code GetLocalPlayer()}.
 *
 * Optimizers use this analysis as a barrier. False positives only cost an
 * optimization; false negatives could move synchronized work into a
 * client-local control-flow region.
 */
public final class LocalPlayerContextAnalyzer {

    private final Set<ImVar> localPlayerDependentVars =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> localPlayerDependentReturns =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> functionsUsingLocalPlayer =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> functionsDirectlyUsingLocalPlayer =
        Collections.newSetFromMap(new IdentityHashMap<>());

    public LocalPlayerContextAnalyzer(ImProg prog) {
        analyze(prog);
    }

    public boolean isLocalPlayerDependent(Element element) {
        if (element == null) {
            return false;
        }
        if (element instanceof ImVarAccess) {
            return localPlayerDependentVars.contains(((ImVarAccess) element).getVar());
        }
        if (element instanceof ImVarArrayAccess) {
            ImVarArrayAccess access = (ImVarArrayAccess) element;
            if (localPlayerDependentVars.contains(access.getVar())) {
                return true;
            }
        }
        if (element instanceof ImMemberAccess) {
            ImMemberAccess access = (ImMemberAccess) element;
            if (localPlayerDependentVars.contains(access.getVar())) {
                return true;
            }
        }
        if (element instanceof ImFunctionCall) {
            ImFunctionCall call = (ImFunctionCall) element;
            if (isGetLocalPlayer(call.getFunc())
                || localPlayerDependentReturns.contains(call.getFunc())) {
                return true;
            }
            // Conservatively assume a return value can depend on any argument.
            if (isLocalPlayerDependent(call.getArguments())) {
                return true;
            }
        }
        if (element instanceof ImMethodCall) {
            ImMethodCall call = (ImMethodCall) element;
            if (methodReturnsLocalPlayerDependentValue(call.getMethod())
                || isLocalPlayerDependent(call.getReceiver())
                || isLocalPlayerDependent(call.getArguments())) {
                return true;
            }
        }
        for (int i = 0; i < element.size(); i++) {
            if (isLocalPlayerDependent(element.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean functionUsesLocalPlayer(ImFunction function) {
        return function != null
            && (isGetLocalPlayer(function) || functionsUsingLocalPlayer.contains(function));
    }

    public boolean functionInliningIsLocalPlayerSensitive(ImFunction function) {
        return function != null
            && (isGetLocalPlayer(function)
            || functionsDirectlyUsingLocalPlayer.contains(function)
            || localPlayerDependentReturns.contains(function));
    }

    public boolean isLocalPlayerDependent(ImVar variable) {
        return variable != null && localPlayerDependentVars.contains(variable);
    }

    public boolean isLocalPlayerSource(ImFunction function) {
        return isGetLocalPlayer(function);
    }

    private void analyze(ImProg prog) {
        boolean changed;
        do {
            int varsBefore = localPlayerDependentVars.size();
            int returnsBefore = localPlayerDependentReturns.size();
            int functionsBefore = functionsUsingLocalPlayer.size();

            for (ImFunction function : ImHelper.calculateFunctionsOfProg(prog)) {
                analyzeFunction(function);
            }

            changed = varsBefore != localPlayerDependentVars.size()
                || returnsBefore != localPlayerDependentReturns.size()
                || functionsBefore != functionsUsingLocalPlayer.size();
        } while (changed);
    }

    private void analyzeFunction(ImFunction function) {
        if (isGetLocalPlayer(function)) {
            localPlayerDependentReturns.add(function);
            functionsUsingLocalPlayer.add(function);
            return;
        }
        if (function.isNative()) {
            return;
        }
        if (containsDirectGetLocalPlayerCall(function.getBody())) {
            functionsDirectlyUsingLocalPlayer.add(function);
        }

        function.getBody().accept(new ImStmt.DefaultVisitor() {
            @Override
            public void visit(ImSet set) {
                super.visit(set);
                if (isLocalPlayerDependent(set.getRight())) {
                    addAssignedVariables(set.getLeft());
                }
            }

            @Override
            public void visit(ImReturn returnStmt) {
                super.visit(returnStmt);
                if (returnStmt.getReturnValue() instanceof ImExpr
                    && isLocalPlayerDependent((ImExpr) returnStmt.getReturnValue())) {
                    localPlayerDependentReturns.add(function);
                }
            }

            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                int count = Math.min(call.getArguments().size(), call.getFunc().getParameters().size());
                for (int i = 0; i < count; i++) {
                    if (isLocalPlayerDependent(call.getArguments().get(i))) {
                        localPlayerDependentVars.add(call.getFunc().getParameters().get(i));
                    }
                }
            }

            @Override
            public void visit(ImMethodCall call) {
                super.visit(call);
                if (isLocalPlayerDependent(call.getReceiver())
                    || isLocalPlayerDependent(call.getArguments())) {
                    markMethodParametersLocalPlayerDependent(call.getMethod());
                }
            }
        });

        if (containsLocalPlayerUse(function.getBody())) {
            functionsUsingLocalPlayer.add(function);
        }
    }

    private boolean containsLocalPlayerUse(Element element) {
        if (element instanceof ImFunctionCall) {
            ImFunction called = ((ImFunctionCall) element).getFunc();
            if (isGetLocalPlayer(called) || functionsUsingLocalPlayer.contains(called)) {
                return true;
            }
        } else if (element instanceof ImMethodCall) {
            if (methodUsesLocalPlayer(((ImMethodCall) element).getMethod())) {
                return true;
            }
        } else if (element instanceof ImVarAccess
            && localPlayerDependentVars.contains(((ImVarAccess) element).getVar())) {
            return true;
        } else if (element instanceof ImVarArrayAccess
            && localPlayerDependentVars.contains(((ImVarArrayAccess) element).getVar())) {
            return true;
        } else if (element instanceof ImMemberAccess
            && localPlayerDependentVars.contains(((ImMemberAccess) element).getVar())) {
            return true;
        }
        for (int i = 0; i < element.size(); i++) {
            if (containsLocalPlayerUse(element.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean methodReturnsLocalPlayerDependentValue(ImMethod method) {
        if (method == null || method.getImplementation() == null) {
            return true;
        }
        if (localPlayerDependentReturns.contains(method.getImplementation())) {
            return true;
        }
        for (ImMethod subMethod : method.getSubMethods()) {
            if (methodReturnsLocalPlayerDependentValue(subMethod)) {
                return true;
            }
        }
        return false;
    }

    private boolean methodUsesLocalPlayer(ImMethod method) {
        if (method == null || method.getImplementation() == null) {
            return true;
        }
        if (functionsUsingLocalPlayer.contains(method.getImplementation())) {
            return true;
        }
        for (ImMethod subMethod : method.getSubMethods()) {
            if (methodUsesLocalPlayer(subMethod)) {
                return true;
            }
        }
        return false;
    }

    private void markMethodParametersLocalPlayerDependent(ImMethod method) {
        if (method == null || method.getImplementation() == null) {
            return;
        }
        localPlayerDependentVars.addAll(method.getImplementation().getParameters());
        for (ImMethod subMethod : method.getSubMethods()) {
            markMethodParametersLocalPlayerDependent(subMethod);
        }
    }

    private boolean containsDirectGetLocalPlayerCall(Element element) {
        if (element instanceof ImFunctionCall
            && isGetLocalPlayer(((ImFunctionCall) element).getFunc())) {
            return true;
        }
        for (int i = 0; i < element.size(); i++) {
            if (containsDirectGetLocalPlayerCall(element.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void addAssignedVariables(ImLExpr left) {
        if (left instanceof ImVarAccess) {
            localPlayerDependentVars.add(((ImVarAccess) left).getVar());
        } else if (left instanceof ImVarArrayAccess) {
            localPlayerDependentVars.add(((ImVarArrayAccess) left).getVar());
        } else if (left instanceof ImMemberAccess) {
            localPlayerDependentVars.add(((ImMemberAccess) left).getVar());
        } else if (left instanceof ImTupleSelection) {
            ImExpr tupleExpr = ((ImTupleSelection) left).getTupleExpr();
            if (tupleExpr instanceof ImLExpr) {
                addAssignedVariables((ImLExpr) tupleExpr);
            }
        } else if (left instanceof ImTupleExpr) {
            for (ImExpr expr : ((ImTupleExpr) left).getExprs()) {
                if (expr instanceof ImLExpr) {
                    addAssignedVariables((ImLExpr) expr);
                }
            }
        } else if (left instanceof ImStatementExpr) {
            ImExpr expr = ((ImStatementExpr) left).getExpr();
            if (expr instanceof ImLExpr) {
                addAssignedVariables((ImLExpr) expr);
            }
        }
    }

    private static boolean isGetLocalPlayer(ImFunction function) {
        return function != null
            && function.isNative()
            && "GetLocalPlayer".equals(function.getName());
    }
}
