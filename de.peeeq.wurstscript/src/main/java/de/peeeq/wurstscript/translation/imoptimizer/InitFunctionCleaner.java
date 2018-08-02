package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.jassIm.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InitFunctionCleaner {

    public static void clean(ImProg prog) {
        List<ImFunction> initFuncs = prog.getFunctions().stream().filter(func -> func.getName().startsWith
                ("init_") && func.getBody().size() == 1).collect(Collectors.toList());

        initFuncs.forEach(func -> {
            ImVar globalBridge = null;
            for (ImVar global : prog.getGlobals()) {
                if (global.getName().equals("ref_function_" + func.getName())) {
                    globalBridge = global;
                    break;
                }
            }
            if (globalBridge != null) {
                Collection<ImVarRead> imVarReads = globalBridge.attrReads();
                if (imVarReads.size() == 1) {
                    ImVarRead initRead = imVarReads.iterator().next();
                    ImVar var = initRead.getVar();

                    Optional<ImFunction> mainFunc = prog.getFunctions().stream().filter(f -> f.getName().equals("main")).findFirst();
                    if (mainFunc.isPresent()) {
                        ImStmts body = mainFunc.get().getBody();
                        for (int i = 0; i < body.size(); i++) {
                            ImStmt imStmt = body.get(i);
                            if (imStmt instanceof ImFunctionCall) {
                                ImFunction calledFunc = ((ImFunctionCall) imStmt).getFunc();
                                if (calledFunc.getName().equals("TriggerAddCondition")) {
                                    int finalI = i;
                                    ((ImFunctionCall) imStmt).getArguments().forEach(arg -> {
                                        if ((arg instanceof ImFunctionCall) && ((ImFunctionCall) arg).getFunc().getName().equals("Condition")) {
                                            ImExpr next = ((ImFunctionCall) arg).getArguments().iterator().next();
                                            if ((next instanceof ImVarAccess) && ((ImVarAccess) next).getVar().structuralEquals(var)) {
                                                imStmt.replaceBy(JassIm.ImNull());
                                                if (body.get(finalI - 1) instanceof ImFunctionCall) {
                                                    body.get(finalI - 1).replaceBy(JassIm.ImNull());
                                                }
                                                body.get(finalI + 1).replaceBy(JassIm.ImNull());
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
