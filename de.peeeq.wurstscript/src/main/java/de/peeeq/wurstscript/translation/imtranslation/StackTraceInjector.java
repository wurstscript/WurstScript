package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstTypeString;
import de.peeeq.wurstscript.utils.Utils;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Takes a program and inserts stack traces at error messages
 */
public class StackTraceInjector {

    private static final String WURST_STACK_TRACE = "wurstStackTrace";
    private ImProg prog;

    public StackTraceInjector(ImProg prog) {
        this.prog = prog;
    }

    public void transform() {
//		@Deprecated final Multimap<ImFunction, ImError> errorPrints = LinkedListMultimap.create();
        final Multimap<ImFunction, ImGetStackTrace> stackTraceGets = LinkedListMultimap.create();
        final Multimap<ImFunction, ImFunctionCall> calls = LinkedListMultimap.create();
        final Multimap<ImFunction, ImFunction> callRelation = LinkedListMultimap.create();
        final List<ImFuncRef> funcRefs = Lists.newArrayList();
        prog.accept(new ImProg.DefaultVisitor() {

            @Override
            public void visit(ImGetStackTrace e) {
                stackTraceGets.put(e.getNearestFunc(), e);
            }

            @Override
            public void visit(ImFunctionCall c) {
                calls.put(c.getFunc(), c);
                ImFunction caller = c.getNearestFunc();
                callRelation.put(caller, c.getFunc());
            }

            @Override
            public void visit(ImFuncRef imFuncRef) {
                funcRefs.add(imFuncRef);
            }
        });

        Multimap<ImFunction, ImFunction> callRelationTr = Utils.transientClosure(callRelation);

        // find affected functions
        Set<ImFunction> affectedFuncs = Sets.newHashSet(stackTraceGets.keySet());
        for (Entry<ImFunction, ImFunction> e : callRelationTr.entries()) {
            if (stackTraceGets.containsKey(e.getValue())) {
                affectedFuncs.add(e.getKey());
            }
        }

        addStackTraceParams(affectedFuncs);
        passStacktraceParams(calls, affectedFuncs);
        rewriteFuncRefs(funcRefs, affectedFuncs);
        rewriteErrorStatements(stackTraceGets);
    }

    private void addStackTraceParams(Set<ImFunction> affectedFuncs) {
        // add parameter to affected functions
        for (ImFunction f : affectedFuncs) {
            if (isMainOrConfig(f)) {
                continue;
            }
            f.getParameters().add(JassIm.ImVar(f.getTrace(), WurstTypeString.instance().imTranslateType(), WURST_STACK_TRACE, false));
        }
    }

    private boolean isMainOrConfig(ImFunction f) {
        return f.getName().equals("main") || f.getName().equals("config");
    }

    private void passStacktraceParams(
            final Multimap<ImFunction, ImFunctionCall> calls,
            Set<ImFunction> affectedFuncs) {
        // pass the stacktrace parameter at all cals
        for (ImFunction f : affectedFuncs) {
            for (ImFunctionCall call : calls.get(f)) {
                ImFunction caller = call.getNearestFunc();
                ImExpr stExpr;
                if (isMainOrConfig(caller)) {
                    stExpr = str("   " + f.getName());
                } else {
                    ImVar stackTraceVar = getStackTraceVar(caller);
                    WPos source = call.attrTrace().attrSource();
                    String callPos;
                    if (source.getFile().startsWith("<")) {
                        callPos = "";
                    } else {
                        callPos = "\n   " + source.printShort();
                    }
                    stExpr = JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(
                            str(callPos),
                            JassIm.ImVarAccess(stackTraceVar)
                    ));
                }
                call.getArguments().add(stExpr);
            }
        }
    }

    private void rewriteFuncRefs(final List<ImFuncRef> funcRefs, Set<ImFunction> affectedFuncs) {
        // rewrite funcrefs
        for (ImFuncRef fr : funcRefs) {
            ImFunction f = fr.getFunc();
            if (!affectedFuncs.contains(f)) {
                continue;
            }

            ImFunction bridgeFunc = JassIm.ImFunction(f.getTrace(), "bridge_" + f.getName(),
                    f.getParameters().copy(), (ImType) f.getReturnType().copy(), JassIm.ImVars(), JassIm.ImStmts(), f.getFlags());
            prog.getFunctions().add(bridgeFunc);

            //remove statcktrace var from params
            bridgeFunc.getParameters().remove(getStackTraceVar(bridgeFunc));

            ImStmt stmt;
            ImExprs args = JassIm.ImExprs(str("\n   " + fr.attrTrace().attrSource().printShort()));
            ImFunctionCall call = JassIm.ImFunctionCall(fr.attrTrace(), f, args, true, CallType.NORMAL);
            if (bridgeFunc.getReturnType() instanceof ImVoid) {
                stmt = call;
            } else {
                stmt = JassIm.ImReturn(fr.attrTrace(), call);
            }
            bridgeFunc.getBody().add(stmt);

            fr.setFunc(bridgeFunc);
        }
    }

    private void rewriteErrorStatements(
            final Multimap<ImFunction, ImGetStackTrace> stackTraceGets) {
        //  rewrite error statements
        for (Entry<ImFunction, ImGetStackTrace> e : stackTraceGets.entries()) {
            ImFunction f = e.getKey();
            ImGetStackTrace s = e.getValue();

            s.replaceBy(JassIm.ImVarAccess(getStackTraceVar(f)));
        }
    }

    private ImVar getStackTraceVar(ImFunction f) {
        if (!f.getParameters().isEmpty()) {
            ImVar v = f.getParameters().get(f.getParameters().size() - 1);
            if (v.getName().equals(WURST_STACK_TRACE)) {
                return v;
            }
        }
        throw new Error("no stacktrace var found in: " + f.getName());
    }

    private ImExpr str(String s) {
        return JassIm.ImStringVal(s);
    }

}
