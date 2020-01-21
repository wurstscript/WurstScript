package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import de.peeeq.datastructures.TransitiveClosure;
import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Takes a program and inserts stack traces at error messages
 */
public class StackTraceInjector2 {

    private static final int MAX_STACKTRACE_SIZE = 20;
    public static final String STACK_POS_PARAM = "__wurst_stackPos";
    private ImProg prog;
    private ImTranslator tr;
    private ImVar stackSize;
    private ImVar stack;
    private ImGetStackTrace dummyGetStackTrace = JassIm.ImGetStackTrace();

    public StackTraceInjector2(ImProg prog, ImTranslator tr) {
        this.prog = prog;
        this.tr = tr;
    }

    public void transform(TimeTaker timeTaker) {
        final Multimap<ImFunction, ImGetStackTrace> stackTraceGets = LinkedListMultimap.create();
        final Multimap<ImFunction, ImFunctionCall> calls = LinkedListMultimap.create();
        // called function -> calling function
        final Multimap<ImFunction, ImFunction> callRelation = LinkedListMultimap.create();
        final List<ImFuncRefOrCall> funcRefs = Lists.newArrayList();
        prog.accept(new ImProg.DefaultVisitor() {

            @Override
            public void visit(ImGetStackTrace e) {
                super.visit(e);
                stackTraceGets.put(e.getNearestFunc(), e);
            }

            @Override
            public void visit(ImVarArrayAccess va) {
                super.visit(va);
                if (va.getIndexes().size() > 1) {
                    stackTraceGets.put(va.getNearestFunc(), dummyGetStackTrace);
                }
            }

            @Override
            public void visit(ImFunctionCall c) {
                super.visit(c);
                if (c.getCallType() == CallType.EXECUTE) {
                    // ExecuteFunc can be handled like a funcref
                    funcRefs.add(c);
                } else {
                    calls.put(c.getFunc(), c);
                    ImFunction caller = c.getNearestFunc();
                    callRelation.put(c.getFunc(), caller);
                }
            }

            @Override
            public void visit(ImFuncRef imFuncRef) {
                super.visit(imFuncRef);
                funcRefs.add(imFuncRef);
            }
        });

        de.peeeq.wurstscript.ast.Element trace = prog.attrTrace();
        stackSize = JassIm.ImVar(trace, TypesHelper.imInt(), "wurst_stack_depth", false);
        prog.getGlobals().add(stackSize);
        stack = JassIm.ImVar(trace, TypesHelper.imStringArray(), "wurst_stack", false);
        prog.getGlobals().add(stack);
        prog.getGlobalInits().put(stackSize, Collections.singletonList(JassIm.ImIntVal(0)));


        TransitiveClosure<ImFunction> callRelationTr = new TransitiveClosure<>(callRelation);

        // find affected functions
        Set<ImFunction> affectedFuncs = Sets.newHashSet(stackTraceGets.keySet());

        if (tr.isLuaTarget()) {
            // in Lua all functions are potentially affected, because we don't know where Lua might crash
            Stream.concat(
                prog.getFunctions().stream(),
                prog.getClasses().stream().flatMap(c -> c.getFunctions().stream()))
                .filter((ImFunction f) ->
                    !f.hasFlag(FunctionFlagEnum.IS_NATIVE)
                        && !f.hasFlag(FunctionFlagEnum.IS_BJ)
                        && !f.hasFlag(FunctionFlagEnum.IS_EXTERN))
                .collect(Collectors.toCollection(() -> affectedFuncs));

        } else {
            for (ImFunction stackTraceUse : stackTraceGets.keys()) {
                callRelationTr.get(stackTraceUse).forEach(affectedFuncs::add);
            }
        }


        passStacktraceParams(calls, affectedFuncs);
        addStackTracePush(calls, affectedFuncs);
        addStackTracePop(affectedFuncs);
        rewriteFuncRefs(funcRefs, affectedFuncs);
        rewriteErrorStatements(stackTraceGets);
        rewriteMethodCalls(affectedFuncs);

    }

    private void rewriteMethodCalls(Set<ImFunction> affectedFuncs) {
        if (!tr.isLuaTarget()) {
            return;
        }
        // for lua, also add stack traces to method calls
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImMethodCall call) {
                super.visit(call);
                if (affectedFuncs.contains(call.getMethod().getImplementation())) {
                    String callPos = getCallPos(call.attrTrace().attrErrorPos());
                    call.getArguments().add(getStacktraceIndex(call), str("when calling " + name(call.getMethod()) + "" + callPos));
                }
            }
        });

    }

    /**
     * push a new stackframe when entering an affected function
     */
    private void addStackTracePush(Multimap<ImFunction, ImFunctionCall> calls, Set<ImFunction> affectedFuncs) {
        for (ImFunction f : affectedFuncs) {
            if (isMainOrConfig(f)) {
                continue;
            }
            ImStmts stmts = f.getBody();
            de.peeeq.wurstscript.ast.Element trace = f.getTrace();
            stmts.add(0, increment(trace, stackSize));
            stmts.add(0, JassIm.ImSet(trace, JassIm.ImVarArrayAccess(trace, stack, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(stackSize))), getStackPosVar(f)));
        }
    }

    private ImExpr getStackPosVar(ImFunction f) {
        return JassIm.ImVarAccess(f.getParameters().stream()
            .filter(this::isStackTraceParam)
            .findFirst().orElseGet(() -> {
                throw new CompileError(f, "Function " + f.getName() + " has no stacktrace parameter.");
            }));
    }

    /**
     * pops a stackframe when returning from an affected function
     */
    private void addStackTracePop(Set<ImFunction> affectedFuncs) {
        // add parameter to affected functions
        for (ImFunction f : affectedFuncs) {
            if (isMainOrConfig(f)) {
                continue;
            }
            Set<ImReturn> returns = new HashSet<>();
            f.getBody().accept(new ImStmts.DefaultVisitor() {
                @Override
                public void visit(ImReturn imReturn) {
                    super.visit(imReturn);
                    returns.add(imReturn);
                }
            });

            int count = 0;
            for (ImReturn ret : returns) {
                ImStmts stmts = JassIm.ImStmts();
                ImReturn newReturn;
                ImExprOpt returnedValue = ret.getReturnValue();
                returnedValue.setParent(null);
                de.peeeq.wurstscript.ast.Element trace = ret.getTrace();
                if (returnedValue instanceof ImNoExpr || !containsAffectedFunctioncall(returnedValue)) {
                    newReturn = JassIm.ImReturn(trace, returnedValue);
                } else {
                    // temp = result
                    String tempReturnName = "stackTrace_tempReturn";
                    count++;
                    if (count > 1) {
                        tempReturnName += "_" + count;
                    }
                    ImVar temp = JassIm.ImVar(trace, f.getReturnType(), tempReturnName, false);
                    f.getLocals().add(temp);
                    stmts.add(JassIm.ImSet(trace, JassIm.ImVarAccess(temp), (ImExpr) returnedValue));
                    newReturn = JassIm.ImReturn(trace, JassIm.ImVarAccess(temp));
                }
                // stackSize = stackSize - 1
                stmts.add(decrement(trace, stackSize));
                stmts.add(newReturn);

                ret.replaceBy(ImHelper.statementExprVoid(stmts));
            }

            // also decrement at end of function:
            if (!returnsOnAllPaths(f.getBody())) {
                f.getBody().add(decrement(f.getTrace(), stackSize));
            }

        }
    }

    private boolean returnsOnAllPaths(ImStmts body) {
        for (ImStmt v : body) {
            if (v instanceof ImReturn) {
                return true;
            } else if (v instanceof ImIf) {
                ImIf imIf = (ImIf) v;
                if (returnsOnAllPaths(imIf.getThenBlock())
                    && returnsOnAllPaths(imIf.getElseBlock())) {
                    return true;
                }
            } else if (v instanceof ImStatementExpr) {
                if (returnsOnAllPaths(((ImStatementExpr) v).getStatements())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsAffectedFunctioncall(ImExprOpt ret) {
        boolean[] res = {false};
        ret.accept(new ImExprOpt.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall imFunctionCall) {
                super.visit(imFunctionCall);
                res[0] = true;
            }

            @Override
            public void visit(ImGetStackTrace imGetStackTrace) {
                super.visit(imGetStackTrace);
                res[0] = true;
            }

        });
        return res[0];
    }

    private boolean isMainOrConfig(ImFunction f) {
        Preconditions.checkNotNull(f);
        return f.getName().equals("main") || f.getName().equals("config");
    }

    private void passStacktraceParams(final Multimap<ImFunction, ImFunctionCall> calls, Set<ImFunction> affectedFuncs) {

        for (ImFunction f : affectedFuncs) {
            if (isMainOrConfig(f)) {
                continue;
            }
            Collection<ImFunctionCall> callsForF = calls.get(f);
            // add stackPos parameter

            f.getParameters().add(getStacktraceIndex(f), JassIm.ImVar(f.getTrace(), TypesHelper.imString(), STACK_POS_PARAM, false));
            // pass the stacktrace parameter at all calls
            for (ImFunctionCall call : callsForF) {
                String callPos = getCallPos(call.attrTrace().attrErrorPos());
                call.getArguments().add(getStacktraceIndex(call), str("when calling " + name(f) + "" + callPos));
            }
        }
    }

    /**
     * insert stacktrace parameter at the end, for vararg functions at the second last position
     */
    private int getStacktraceIndex(ImFunction f) {
        if (f.hasFlag(FunctionFlagEnum.IS_VARARG)) {
            return f.getParameters().size() - 1;
        }
        return f.getParameters().size();
    }

    /**
     * insert stacktrace parameter at the end, for vararg functions at the second last position
     */
    private int getStacktraceIndex(ImFunctionCall c) {
        ImFunction f = c.getFunc();
        int res = f.getParameters().size() - 1;
        if (f.hasFlag(FunctionFlagEnum.IS_VARARG)) {
            res--;
        }
        if (res < 0 || res > c.getArguments().size() + 1) {
            throw new CompileError(c, "Call " + c + " invalid index " + res + " for parameters " + f.getParameters() + " and isVararg = " + f.hasFlag(FunctionFlagEnum.IS_VARARG));
        }
        return res;
    }

    private int getStacktraceIndex(ImMethodCall c) {
        ImFunction f = c.getMethod().getImplementation();
        int res = f.getParameters().size() - 2; // subtract one for implicit parameter
        if (f.hasFlag(FunctionFlagEnum.IS_VARARG)) {
            res--;
        }
        if (res < 0 || res > c.getArguments().size() + 1) {
            throw new CompileError(c, "Call " + c + " invalid index " + res + " for parameters " + f.getParameters() + " and isVararg = " + f.hasFlag(FunctionFlagEnum.IS_VARARG));
        }
        return res;
    }


    private String name(JassImElementWithName f) {
        @Nullable NameDef nameDef = f.attrTrace().tryGetNameDef();
        if (nameDef instanceof FunctionDefinition) {
            return nameDef.getName();
        }
        return f.getName();
    }

    public static String getCallPos(WPos source) {
        String callPos;
        if (source.getFile().startsWith("<")) {
            callPos = "";
        } else {
            callPos = " in " + source.printShort();
        }
        return callPos;
    }

    private ImStmt increment(de.peeeq.wurstscript.ast.Element trace, ImVar v) {
        return JassIm.ImSet(trace, JassIm.ImVarAccess(v), JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(v), JassIm.ImIntVal(1))));
    }

    private ImStmt decrement(de.peeeq.wurstscript.ast.Element trace, ImVar v) {
        return JassIm.ImSet(trace, JassIm.ImVarAccess(v), JassIm.ImOperatorCall(WurstOperator.MINUS, JassIm.ImExprs(JassIm.ImVarAccess(v), JassIm.ImIntVal(1))));
    }

    private void rewriteFuncRefs(final List<ImFuncRefOrCall> funcRefs, Set<ImFunction> affectedFuncs) {
        // rewrite funcrefs
        for (ImFuncRefOrCall fr : funcRefs) {
            ImFunction f = fr.getFunc();
            if (!affectedFuncs.contains(f)) {
                continue;
            }

            ImVars params = f.getParameters().copy();
            // remove stacktrace param
            params.removeIf(this::isStackTraceParam);

            ImFunction bridgeFunc = JassIm.ImFunction(f.getTrace(), "bridge_" + f.getName(), JassIm.ImTypeVars(), params, f.getReturnType().copy(), JassIm.ImVars(), JassIm.ImStmts(), f.getFlags());
            prog.getFunctions().add(bridgeFunc);

            ImStmt stmt;
            de.peeeq.wurstscript.ast.Element frTrace = fr.attrTrace();

            String msg = "";
            if (fr instanceof ImFuncRef) {
                msg = "via function reference";
            } else {
                msg = "via ExecuteFunc";
            }
            if (frTrace instanceof WurstModel) {
                ImFunction frEnclosingFunc = getEnclosingFunc(fr);
                if (frEnclosingFunc != null) {
                    msg += " in function " + frEnclosingFunc.getName();
                }
            } else {
                msg += " " +frTrace.attrSource().printShort();
            }
            ImExpr str = str(msg);
            ImExprs args = JassIm.ImExprs(str);
            ImStmts body = bridgeFunc.getBody();
            de.peeeq.wurstscript.ast.Element trace = frTrace;
            // reset stack and add information for callback:
            body.add(JassIm.ImSet(trace, JassIm.ImVarAccess(stackSize), JassIm.ImIntVal(0)));

            ImFunctionCall call = JassIm.ImFunctionCall(frTrace, f, JassIm.ImTypeArguments(), args, true, CallType.NORMAL);
            if (bridgeFunc.getReturnType() instanceof ImVoid) {
                stmt = call;
            } else {
                stmt = JassIm.ImReturn(frTrace, call);
            }
            body.add(stmt);

            fr.setFunc(bridgeFunc);
        }
    }

    private ImFunction getEnclosingFunc(Element e) {
        while (e != null) {
            if (e instanceof ImFunction) {
                return (ImFunction) e;
            }
            e = e.getParent();
        }
        return null;
    }

    private boolean isStackTraceParam(ImVar p) {
        return p.getName().equals(STACK_POS_PARAM);
    }

    private void rewriteErrorStatements(final Multimap<ImFunction, ImGetStackTrace> stackTraceGets) {
        // rewrite error statements
        for (Entry<ImFunction, ImGetStackTrace> e : stackTraceGets.entries()) {
            ImFunction f = e.getKey();
            ImGetStackTrace s = e.getValue();
            if (s == dummyGetStackTrace) {
                continue;
            }

            de.peeeq.wurstscript.ast.Element trace = s.attrTrace();
            ImVar traceStr = JassIm.ImVar(trace, TypesHelper.imString(), "stacktraceStr", false);
            f.getLocals().add(traceStr);
            ImVar traceI = JassIm.ImVar(trace, TypesHelper.imInt(), "stacktraceIndex", false);
            f.getLocals().add(traceI);
            ImVar traceLimit = JassIm.ImVar(trace, TypesHelper.imInt(), "stacktraceLimit", false);
            f.getLocals().add(traceLimit);
            ImStmts stmts = JassIm.ImStmts();
            stmts.add(JassIm.ImSet(trace, JassIm.ImVarAccess(traceStr), JassIm.ImStringVal("")));
            stmts.add(JassIm.ImSet(trace, JassIm.ImVarAccess(traceI), JassIm.ImVarAccess(stackSize)));
            stmts.add(JassIm.ImSet(trace, JassIm.ImVarAccess(traceLimit), JassIm.ImIntVal(0)));
            ImStmts loopBody = JassIm.ImStmts();
            stmts.add(JassIm.ImLoop(trace, loopBody));
            // i = i - 1
            loopBody.add(decrement(trace, traceI));
            // limit = limit + 1
            loopBody.add(increment(trace, traceLimit));
            // exitwhen limit > 20
            loopBody.add(JassIm.ImExitwhen(trace, JassIm.ImOperatorCall(WurstOperator.GREATER,
                JassIm.ImExprs(JassIm.ImVarAccess(traceLimit), JassIm.ImIntVal(MAX_STACKTRACE_SIZE)))));
            // exitwhen i < 0
            loopBody.add(JassIm.ImExitwhen(trace, JassIm.ImOperatorCall(WurstOperator.LESS,
                JassIm.ImExprs(JassIm.ImVarAccess(traceI), JassIm.ImIntVal(0)))));
            // s = s + "\n " + stack[i]
            loopBody.add(JassIm.ImSet(trace, JassIm.ImVarAccess(traceStr), JassIm.ImOperatorCall(WurstOperator.PLUS,
                JassIm.ImExprs(JassIm.ImVarAccess(traceStr),
                    JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImStringVal("\n   "),
                        JassIm.ImVarArrayAccess(trace, stack, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(traceI)))))))));

            s.replaceBy(JassIm.ImStatementExpr(stmts, JassIm.ImVarAccess(traceStr)));
        }
    }

    private ImExpr str(String s) {
        return JassIm.ImStringVal(s);
    }

}
