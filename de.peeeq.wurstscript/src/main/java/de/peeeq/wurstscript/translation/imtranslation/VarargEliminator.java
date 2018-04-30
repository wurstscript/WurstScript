package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;

/**
 * Takes a program and eliminates vararg functions, replacing them with
 * generated functions with the appropriate amount of parameters.
 */
public class VarargEliminator {

    private ImProg prog;

    public VarargEliminator(ImProg prog, ImTranslator trans) {
        this.prog = prog;
    }

    public void run() {
        final Multimap<ImFunction, ImFunctionCall> calls = LinkedListMultimap.create();
        final List<Integer> generatedSizes = Lists.newArrayList();
        prog.accept(new ImProg.DefaultVisitor() {

            @Override
            public void visit(ImFunctionCall c) {
                super.visit(c);
                // Collect all calls to vararg functions
                if (c.getFunc().hasFlag(IS_VARARG)) {
                    calls.put(c.getFunc(), c);
                }
            }

        });

        calls.forEach((func, call) -> {
            // Generate functions with appropriate parameters
            if (!generatedSizes.contains(call.getArguments().size())) {
                generateVarargFunc(func, call);
                generatedSizes.add(call.getArguments().size());
            }
        });

        calls.forEach((func, call) -> {
            func.setParent(null);
            prog.getFunctions().remove(func);
        });

    }

    /**
     * Generates a function based on the vararg function with the appropriate amount of parameters
     * for the function call.
     */
    private void generateVarargFunc(ImFunction func, ImFunctionCall call) {
        // how many vararg-parameters should we generate?
        // ==> number of parameters in call minus non-vararg parameters in the definition
        int argumentSize = 1 + call.getArguments().size() - func.getParameters().size();

        // Create new function
        ImFunction newFunc = ReferenceRewritingCopy.copy(func);
        newFunc.setName(func.getName() + "_" + argumentSize);
        // replace vararg with special parameters:
        ImVar varargParam = newFunc.getParameters().remove(newFunc.getParameters().size() - 1);
        ImType type = varargParam.getType();
        List<ImVar> newParams = new ArrayList<>();
        for (int i = 0; i < argumentSize; i++) {
            ImVar param = JassIm.ImVar(func.getTrace(), type, varargParam.getName() + "_" + i, false);
            newParams.add(param);
            newFunc.getParameters().add(param);
        }



        // Visit all vararg loop statements inside the new function
        newFunc.getBody().accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVarargLoop imLoop) {
                super.visit(imLoop);
                unrollVarargLoop(func, newFunc, imLoop, newParams);
            }

        });

        // Remove vararg flag
        newFunc.getFlags().remove(IS_VARARG);
        // Add new function to prog
        prog.getFunctions().add(newFunc);

        // Redirect call to new function
        ImFunctionCall newCall = JassIm.ImFunctionCall(call.getTrace().copy(), newFunc, call.getArguments().copy(), call.getTuplesEliminated(),
                call.getCallType());
        call.replaceBy(newCall);
    }

    private void unrollVarargLoop(ImFunction func, ImFunction newFunc, ImVarargLoop imLoop, List<ImVar> newParams) {
        ImStatementExpr stmtExpr = JassIm.ImStatementExpr(JassIm.ImStmts(), JassIm.ImNull());

        for (int i = 0; i < newParams.size(); i++) {
            ImStmts bodyCopy = imLoop.getBody().copy();
            int finalI = i;
            bodyCopy.accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ImVarAccess access) {
                    super.visit(access);
                    if (access.getVar() == imLoop.getLoopVar()) {
                        access.setVar(newParams.get(finalI));
                    }
                }

            });
            stmtExpr.getStatements().addAll(bodyCopy.removeAll());
        }

        imLoop.replaceBy(stmtExpr);
    }


}
