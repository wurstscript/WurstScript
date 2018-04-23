package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.peeeq.wurstscript.jassIm.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;

/**
 * Takes a program and eliminates vararg functions, replacing them with
 * generated functions with the appropriate amount of parameters.
 */
public class VarargEliminator {

    private static final int MAX_STACKTRACE_SIZE = 20;
    private static final String WURST_STACK_TRACE = "wurstStackTrace";
    private ImProg prog;
    private ImVar stackSize;
    private ImVar stack;
    private Map<Element, Element> replacements = new LinkedHashMap<>();

    public VarargEliminator(ImProg prog, ImTranslator trans) {
        this.prog = prog;
    }

    public void run() {
        final Multimap<ImFunction, ImFunctionCall> calls = LinkedListMultimap.create();
        final List<ImFunction> varargFuncs = Lists.newArrayList();
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

    }

    private void generateVarargFunc(ImFunction func, ImFunctionCall call) {
        int argumentSize = call.getArguments().size();
        ImVar vararg = func.getParameters().get(0);
        ImVars params = JassIm.ImVars();
        for (int i = 0; i < argumentSize; i++) {
            params.add(JassIm.ImVar(func.getTrace(), vararg.getType(), vararg.getName() + "_" + i, false));
        }
        ImFunction newFunc = JassIm.ImFunction(func.getTrace(), func.getName() + "_" + argumentSize, params, func.getReturnType(), func.getLocals().copy()
                , func.getBody().copy(), func.getFlags());


        ImFunctionCall newCall = JassIm.ImFunctionCall(call.getTrace().copy(), newFunc, call.getArguments().copy(), call.getTuplesEliminated(),
                call.getCallType());

        call.replaceBy(newCall);
        prog.getFunctions().add(newFunc);

    }


}
