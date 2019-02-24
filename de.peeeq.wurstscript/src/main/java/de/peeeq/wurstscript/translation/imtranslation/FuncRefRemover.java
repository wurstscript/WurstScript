package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.WurstTypeCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * removes function references with variables
 * in order to avoid cyclic dependencies
 */
public class FuncRefRemover {

    private ImProg prog;
    private ImTranslator tr;

    public FuncRefRemover(ImProg imProg, ImTranslator tr) {
        this.prog = imProg;
        this.tr = tr;
    }


    public void run() {
        final List<ImFuncRef> funcRefs = Lists.newArrayList();
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImFuncRef imFuncRef) {
                super.visit(imFuncRef);
                funcRefs.add(imFuncRef);
            }
        });

        Map<ImFunction, ImVar> refs = new HashMap<>();

        for (ImFuncRef fr : funcRefs) {
            ImFunction func = fr.getFunc();
            if (func.isBj()) {
                // do not handle bj functions
                continue;
            }
            ImVar g;
            if (refs.containsKey(func)) {
                // already created global function reference
                g = refs.get(func);
            } else {
                // create global variable containing a reference to the function:
                g = JassIm.ImVar(fr.attrTrace(), WurstTypeCode.instance().imTranslateType(tr),
                        "ref_function_" + func.getName(), false);
                refs.put(func, g);
                tr.addGlobalWithInitalizer(g, fr.copy());
            }
            fr.replaceBy(JassIm.ImVarAccess(g));
        }

    }
}
