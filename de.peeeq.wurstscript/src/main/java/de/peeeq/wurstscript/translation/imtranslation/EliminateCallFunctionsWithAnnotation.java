package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EliminateCallFunctionsWithAnnotation {

    private static final String CALL_FUNCTIONS_WITH_ANNOTATION = "callFunctionsWithAnnotation";

    public static void process(ImProg prog) {
        List<ImFunctionCall> specialCalls = collectSpecialCalls(prog);
        Set<String> calledAnnotations = getAnnotations(specialCalls);
        Multimap<String, ImFunction> annotatedFunctions = collectAnnotatedFunctions(prog, calledAnnotations);
        rewriteSpecialCalls(specialCalls, annotatedFunctions);
    }

    private static void rewriteSpecialCalls(List<ImFunctionCall> specialCalls,
                                            Multimap<String, ImFunction> annotatedFunctions) {
        for (ImFunctionCall fc : specialCalls) {
            rewriteSpecialCall(fc, annotatedFunctions);
        }
    }

    private static void rewriteSpecialCall(ImFunctionCall fc,
                                           Multimap<String, ImFunction> annotatedFunctions) {
        ImStmts statements = JassIm.ImStmts();
        for (ImFunction f : annotatedFunctions.get(calledAnnotation(fc))) {
            statements.add(JassIm.ImFunctionCall(fc.getTrace(), f, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL));
        }
        fc.replaceBy(ImHelper.statementExprVoid(statements));
    }

    private static Multimap<String, ImFunction> collectAnnotatedFunctions(
            ImProg prog, Set<String> calledAnnotations) {
        Multimap<String, ImFunction> res = LinkedHashMultimap.create();
        for (ImFunction f : prog.getFunctions()) {
            for (String a : calledAnnotations) {
                if (f.hasFlag(new FunctionFlagAnnotation(a))) {
                    res.put(a, f);
                }
            }
        }
        return res;
    }

    private static Set<String> getAnnotations(List<ImFunctionCall> specialCalls) {
        Set<String> res = new LinkedHashSet<>();
        for (ImFunctionCall fc : specialCalls) {
            res.add(calledAnnotation(fc));
        }
        return res;
    }

    private static String calledAnnotation(ImFunctionCall fc)
            throws CompileError {
        if (fc.getArguments().size() != 1) {
            throw new CompileError(fc.attrTrace().attrSource(), "wrong number of arguments");
        }
        ImExpr arg = fc.getArguments().get(0);
        if (arg instanceof ImStringVal) {
            ImStringVal sArg = (ImStringVal) arg;
            return sArg.getValS();
        } else {
            throw new CompileError(fc.attrTrace().attrSource(), "argument must be a constant string");
        }
    }

    private static List<ImFunctionCall> collectSpecialCalls(ImProg prog) {
        final List<ImFunctionCall> specialCalls = new ArrayList<>();
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall fc) {
                super.visit(fc);
                if (fc.getFunc().getName().equals(CALL_FUNCTIONS_WITH_ANNOTATION)) {
                    specialCalls.add(fc);
                }
            }
        });
        return specialCalls;
    }

}
