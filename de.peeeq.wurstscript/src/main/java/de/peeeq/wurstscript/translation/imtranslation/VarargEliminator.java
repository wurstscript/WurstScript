package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.jassIm.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;

/**
 * Takes a program and eliminates vararg functions, replacing them with
 * generated functions with the appropriate amount of parameters.
 */
public class VarargEliminator {

    private final ImProg prog;
    // original + number of args --> new function
    private final Table<ImFunction, Integer, ImFunction> varargFuncs = HashBasedTable.create();

    public VarargEliminator(ImProg prog) {
        this.prog = prog;
    }

    public void run() {
        // create new vararg functions
        for (ImFunctionCall c : collectVarargCalls()) {
            if (c.getFunc().hasFlag(IS_VARARG)) {
                generateVarargFunc(c.getFunc(), c.getArguments().size());
            }
        }

        // remove original vararg functions:
        prog.getFunctions().removeIf(f -> f.hasFlag(IS_VARARG));

        // rewrite calls to use new functions:
        // (need to collect vararg calls again, because first phase can create copies of calls)
        for (ImFunctionCall call : collectVarargCalls()) {
            redirectCall(call, varargFuncs.get(call.getFunc(), call.getArguments().size()));
        }
    }

    @NotNull
    private Collection<ImFunctionCall> collectVarargCalls() {
        // Collect all calls to vararg functions
        final Collection<ImFunctionCall> calls = new ArrayList<>();
        prog.accept(new ImProg.DefaultVisitor() {

            @Override
            public void visit(ImFunctionCall c) {
                super.visit(c);
                if (c.getFunc().hasFlag(IS_VARARG)) {
                    calls.add(c);
                }
            }

        });
        return calls;
    }

    /**
     * Generates a function based on the vararg function with the appropriate amount of parameters
     * for the function call.
     */
    private void generateVarargFunc(ImFunction func, int numberOfParams) {
        if (varargFuncs.contains(func, numberOfParams)) {
            // already generated
            return;
        }

        // how many vararg-parameters should we generate?
        // ==> number of parameters in call minus non-vararg parameters in the definition
        int argumentSize = 1 + numberOfParams - func.getParameters().size();

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
                unrollVarargLoop(imLoop, newParams);
            }


        });

        // Visit all remaining uses of the vararg-parameter
        // this must be calls to other vararg functions, so unfold the parameters
        List<ImVarAccess> varargParamUses = collectUsesOfVar(newFunc, varargParam);

        for (ImVarAccess va : varargParamUses) {
            ImExprs params = (ImExprs) va.getParent();
            ImFunctionCall call = (ImFunctionCall) params.getParent();

            params.remove(va);
            params.addAll(newParams.stream().map(JassIm::ImVarAccess).collect(Collectors.toList()));

            // generate function for this new call
            generateVarargFunc(call.getFunc(), call.getArguments().size());
        }


        // Remove vararg flag
        newFunc.setFlags(newFunc.getFlags().stream().filter(flag -> flag != IS_VARARG).collect(Collectors.toList()));
        // Add new function to prog
        prog.getFunctions().add(newFunc);
        varargFuncs.put(func, numberOfParams, newFunc);
    }

    @NotNull
    private List<ImVarAccess> collectUsesOfVar(ImFunction newFunc, ImVar varargParam) {
        List<ImVarAccess> varargParamUses = new ArrayList<>();
        newFunc.getBody().accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                if (va.getVar() == varargParam) {
                    varargParamUses.add(va);
                }
            }
        });
        return varargParamUses;
    }

    private void redirectCall(ImFunctionCall call, ImFunction newFunc) {
        // Redirect call to new function
        ImFunctionCall newCall = JassIm.ImFunctionCall(call.getTrace(), newFunc, JassIm.ImTypeArguments(), JassIm.ImExprs(call.getArguments().removeAll()), call.getTuplesEliminated(), call.getCallType());
        call.replaceBy(newCall);
    }

    private void unrollVarargLoop(ImVarargLoop imLoop, List<ImVar> newParams) {
        ImStatementExpr stmtExpr = ImHelper.statementExprVoid(JassIm.ImStmts());

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
