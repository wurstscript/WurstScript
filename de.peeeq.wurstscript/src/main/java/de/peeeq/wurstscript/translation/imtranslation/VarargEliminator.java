package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.utils.Utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;

/**
 * Takes a program and inserts stack traces at error messages
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


        final Multimap<ImFunction, ImGetStackTrace> stackTraceGets = LinkedListMultimap.create();
        final Multimap<ImFunction, ImFunctionCall> calls = LinkedListMultimap.create();
        final Multimap<ImFunction, ImFunction> callRelation = LinkedListMultimap.create();
        final List<ImFuncRef> funcRefs = Lists.newArrayList();
        prog.accept(new ImProg.DefaultVisitor() {

            @Override
            public void visit(ImGetStackTrace e) {
                super.visit(e);
                stackTraceGets.put(e.getNearestFunc(), e);
            }

            @Override
            public void visit(ImFunctionCall c) {
                super.visit(c);
                // Collect all calls to vararg functions
                if(c.getFunc().hasFlag(IS_VARARG)) {
                    calls.put(c.getFunc(), c);
                }
            }

            @Override
            public void visit(ImFuncRef imFuncRef) {
                super.visit(imFuncRef);
                funcRefs.add(imFuncRef);
            }
        });

        de.peeeq.wurstscript.ast.Element trace = prog.attrTrace();
        stackSize = JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), "wurst_stack_depth", false);
        prog.getGlobals().add(stackSize);
        stack = JassIm.ImVar(trace, JassIm.ImArrayType("string"), "wurst_stack", false);
        prog.getGlobals().add(stack);
        prog.getGlobalInits().put(stackSize, JassIm.ImIntVal(0));

        Multimap<ImFunction, ImFunction> callRelationTr = Utils.transientClosure(callRelation);

        // find affected functions
        Set<ImFunction> affectedFuncs = Sets.newHashSet(stackTraceGets.keySet());
        for (Entry<ImFunction, ImFunction> e : callRelationTr.entries()) {
            if (stackTraceGets.containsKey(e.getValue())) {
                affectedFuncs.add(e.getKey());
            }
        }

        for (Entry<Element, Element> e : replacements.entrySet()) {
            e.getKey().replaceBy(e.getValue());
        }
    }


}
