package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.base.Preconditions;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.PRESERVE_NAME;

/**
 * Takes a program and eliminates vararg functions, replacing them with
 * generated functions with the appropriate amount of parameters.
 */
public class VarargEliminator {

    private static final int JASS_MAX_PARAMETERS = ImHelper.JASS_MAX_PARAMETERS;
    /**
     * Largest number of emitted parameters a fixed-arity copy may have on Lua, counted after tuple
     * flattening: one four-field tuple argument is four parameters, so an arity that looks modest in
     * source can exceed what the target accepts. Lua caps a function at 200 locals including its
     * parameters, and the locals-table fallback cannot spill parameters, so this leaves room for the
     * body's own locals. A call above it keeps the original `...` function, which is always still
     * present on that target.
     */
    public static final int LUA_MAX_SPECIALISED_VARARG_PARAMETERS = 64;
    private final ImProg prog;
    /**
     * On Lua classes are still present when this runs, so a vararg function can also be reached
     * through a method dispatch or a function reference. Originals are therefore kept, only direct
     * calls are redirected, and unreferenced originals are left to garbage removal.
     */
    private final boolean luaTarget;
    // original + number of args --> new function
    private final Table<ImFunction, Integer, ImFunction> varargFuncs = HashBasedTable.create();

    public VarargEliminator(ImProg prog) {
        this(prog, false);
    }

    public VarargEliminator(ImProg prog, boolean luaTarget) {
        this.prog = prog;
        this.luaTarget = luaTarget;
    }

    public void run() {
        // Create new vararg functions. Repeated to a fixpoint: a generated copy can contain a call
        // to a vararg function at an arity nothing has needed yet, which is what a recursive vararg
        // function calling itself with a different argument count produces.
        boolean generated = true;
        while (generated) {
            generated = false;
            for (ImFunctionCall c : collectVarargCalls()) {
                if (c.getFunc().hasFlag(IS_VARARG) && shouldSpecialise(c)
                    && !varargFuncs.contains(c.getFunc(), c.getArguments().size())) {
                    generateVarargFunc(c);
                    generated = true;
                }
            }
            if (luaTarget) {
                // The Lua backend already turns a method call with exactly one possible
                // implementation into a direct call of that implementation. Doing the same here for
                // vararg methods is what lets ArrayList.add and friends get a fixed-arity copy at
                // all: on this target the call is still an ImMethodCall when varargs are eliminated.
                for (ImMethodCall c : collectMonomorphicVarargMethodCalls()) {
                    ImFunction implementation = c.getMethod().getImplementation();
                    List<ImExpr> arguments = receiverAndArguments(c);
                    if (shouldSpecialise(arguments)
                        && !varargFuncs.contains(implementation, arguments.size())) {
                        generateVarargFunc(implementation, arguments, c);
                        generated = true;
                    }
                }
            }
        }

        if (!luaTarget) {
            // remove original vararg functions:
            prog.getFunctions().removeIf(f -> f.hasFlag(IS_VARARG));
        }

        // rewrite calls to use new functions:
        // (need to collect vararg calls again, because first phase can create copies of calls)
        for (ImFunctionCall call : collectVarargCalls()) {
            ImFunction newFunc = varargFuncs.get(call.getFunc(), call.getArguments().size());
            if (newFunc != null) {
                redirectCall(call, newFunc);
            }
        }
        if (luaTarget) {
            for (ImMethodCall call : collectMonomorphicVarargMethodCalls()) {
                ImFunction implementation = call.getMethod().getImplementation();
                ImFunction newFunc = varargFuncs.get(implementation, 1 + call.getArguments().size());
                if (newFunc != null) {
                    redirectMethodCall(call, newFunc);
                }
            }
        }
    }

    /** A method call which can only ever reach one implementation, and that implementation is vararg. */
    private Collection<ImMethodCall> collectMonomorphicVarargMethodCalls() {
        final Collection<ImMethodCall> calls = new ArrayList<>();
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImMethodCall c) {
                super.visit(c);
                ImMethod method = c.getMethod();
                if (method != null && !method.getIsAbstract() && method.getImplementation() != null
                    && method.getSubMethods().isEmpty() && method.getImplementation().hasFlag(IS_VARARG)) {
                    calls.add(c);
                }
            }
        });
        return calls;
    }

    /** The implementation's argument list: the receiver is its first parameter. */
    private static List<ImExpr> receiverAndArguments(ImMethodCall call) {
        List<ImExpr> arguments = new ArrayList<>(1 + call.getArguments().size());
        arguments.add(call.getReceiver());
        arguments.addAll(call.getArguments());
        return arguments;
    }

    private void redirectMethodCall(ImMethodCall call, ImFunction newFunc) {
        ImExprs args = JassIm.ImExprs(call.getReceiver().copy());
        args.addAll(call.getArguments().removeAll());
        call.replaceBy(JassIm.ImFunctionCall(call.getTrace(), newFunc, JassIm.ImTypeArguments(), args,
            call.getTuplesEliminated(), CallType.NORMAL));
    }

    /** Whether a call gets a fixed-arity copy. Always on Jass; on Lua only within the parameter bound. */
    private boolean shouldSpecialise(ImFunctionCall call) {
        return shouldSpecialise(call.getArguments());
    }

    /**
     * Counted after tuple flattening, because that is what the emitted parameter list costs: twenty
     * four-field tuples are eighty parameters, not twenty.
     */
    private boolean shouldSpecialise(List<ImExpr> arguments) {
        if (!luaTarget) {
            return true;
        }
        int parameters = 0;
        for (ImExpr argument : arguments) {
            parameters += ImHelper.flattenedJassArity(argument.attrTyp());
            if (parameters > LUA_MAX_SPECIALISED_VARARG_PARAMETERS) {
                return false;
            }
        }
        return true;
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
    private void generateVarargFunc(ImFunctionCall sourceCall) {
        generateVarargFunc(sourceCall.getFunc(), sourceCall.getArguments(), sourceCall);
    }

    /** {@code arguments} are in the callee's parameter order, so for a method they start with the receiver. */
    private void generateVarargFunc(ImFunction func, List<ImExpr> arguments, Element trace) {
        int numberOfParams = arguments.size();
        int jassParameterCount = arguments.stream()
            .mapToInt(argument -> ImHelper.flattenedJassArity(argument.attrTyp()))
            .sum();
        if (!luaTarget && jassParameterCount > JASS_MAX_PARAMETERS) {
            throw new CompileError(trace, "Vararg call would generate " + jassParameterCount
                + " Jass parameters; the maximum is " + JASS_MAX_PARAMETERS
                + ". Use multiple calls (for example with the cascade operator) or pass a collection instead.");
        }
        if (varargFuncs.contains(func, numberOfParams)) {
            // already generated
            return;
        }

        // how many vararg-parameters should we generate?
        // ==> number of parameters in call minus non-vararg parameters in the definition
        int argumentSize = 1 + numberOfParams - func.getParameters().size();

        // Create new function
        ImFunction newFunc = ReferenceRewritingCopy.copy(func);
        // ReferenceRewritingCopy retargets the function's own references - both call and reference
        // nodes - so inside the copy they now name the copy. That is wrong for either kind. A
        // recursive call must go back to naming the vararg original, so the rewrite below maps it to
        // a copy of its own arity like any other call; a self reference must name the original too,
        // because it is invoked at an arity this pass never sees.
        newFunc.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                if (call.getFunc() == newFunc) {
                    call.setFunc(func);
                }
            }

            @Override
            public void visit(ImFuncRef ref) {
                super.visit(ref);
                // Lua only: nothing redirects a reference afterwards, so it keeps naming whatever it
                // is set to here, and only this target retains the original. On Jass the original is
                // removed below and pointing at it would leave the reference dangling.
                if (luaTarget && ref.getFunc() == newFunc) {
                    ref.setFunc(func);
                }
            }
        });
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
            List<ImVarAccess> list = new ArrayList<>();
            for (ImVar newParam : newParams) {
                ImVarAccess imVarAccess = JassIm.ImVarAccess(newParam);
                list.add(imVarAccess);
            }
            params.addAll(list);

            // generate function for this new call
            if (shouldSpecialise(call)) {
                generateVarargFunc(call);
            }
        }


        // Drop the vararg flag, and on Lua the name preservation with it. A preserved name is part
        // of the map's Warcraft-facing API and belongs to the retained original, which is what
        // external code calls at an arity this pass never sees. Since a copy shares the original's
        // trace, and LuaTranslator.collectPredefinedNames() resets every preserved function to its
        // trace's source name, an inherited flag would emit both under one name.
        List<FunctionFlag> list = new ArrayList<>();
        for (FunctionFlag flag : newFunc.getFlags()) {
            if (flag == IS_VARARG || (luaTarget && flag == PRESERVE_NAME)) {
                continue;
            }
            list.add(flag);
        }
        newFunc.setFlags(list);
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
        Preconditions.checkState(imLoop.getLoopVars().size() == 1,
            "Expected one vararg loop variable before vararg elimination.");
        ImVar loopVar = imLoop.getLoopVars().get(0).getVar();
        ImStatementExpr stmtExpr = ImHelper.statementExprVoid(JassIm.ImStmts());

        for (int i = 0; i < newParams.size(); i++) {
            ImStmts bodyCopy = imLoop.getBody().copy();
            int finalI = i;
            bodyCopy.accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ImVarAccess access) {
                    super.visit(access);
                    if (access.getVar() == loopVar) {
                        access.setVar(newParams.get(finalI));
                    }
                }

            });
            stmtExpr.getStatements().addAll(bodyCopy.removeAll());
        }

        imLoop.replaceBy(stmtExpr);
    }


}
