package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.jassIm.*;

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
        int argumentSize = call.getArguments().size();
        ImVar vararg = func.getParameters().get(0);
        ImType type = vararg.getType();
        // Generate parameters
        ImVars params = JassIm.ImVars();
        for (int i = 0; i < argumentSize; i++) {
            params.add(JassIm.ImVar(func.getTrace(), type, vararg.getName() + "_" + i, false));
        }
        // Create new function
        ImFunction newFunc = JassIm.ImFunction(func.getTrace(), func.getName() + "_" + argumentSize, params, func.getReturnType(), func.getLocals().copy()
                , func.getBody().copy(), func.getFlags());

        // Visit all vararg loop statements inside the new function
        newFunc.getBody().accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVarargLoop imLoop) {
                super.visit(imLoop);
                System.out.println("visit vararg loop");
                unrollVarargLoop(func, newFunc, imLoop, params);
            }

            @Override
            public void visit(ImVarAccess access) {
                super.visit(access);
                AtomicInteger i = new AtomicInteger();
                func.getLocals().forEach(local -> {
                    if (access.getVar().getName().equals(local.getName())) {
                        access.setVar(newFunc.getLocals().get(i.get()));
                    }
                    i.getAndIncrement();
                });
            }

            @Override
            public void visit(ImSet set) {
                super.visit(set);
                AtomicInteger i = new AtomicInteger();
                func.getLocals().forEach(local -> {
                    if (set.getLeft().getName().equals(local.getName())) {
                        set.setLeft(newFunc.getLocals().get(i.get()));
                    }
                    i.getAndIncrement();
                });
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

    private void unrollVarargLoop(ImFunction func, ImFunction newFunc, ImVarargLoop imLoop, ImVars params) {
        ImStatementExpr stmtExpr = JassIm.ImStatementExpr(JassIm.ImStmts(), JassIm.ImNull());

        for (int i = 0; i < params.size(); i++) {
            ImStmts bodyCopy = imLoop.getBody().copy();
            bodyCopy.forEach(elem -> elem.setParent(null));
            int finalI = i;
            bodyCopy.accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ImVarAccess access) {
                    super.visit(access);
                    if (access.getVar().getName().equals(imLoop.getLoopVar().getName())) {
                        access.setVar(params.get(finalI));
                    }
                }

            });
            bodyCopy.setParent(null);
            stmtExpr.getStatements().addAll(bodyCopy);
        }

        imLoop.replaceBy(stmtExpr);
    }


}
