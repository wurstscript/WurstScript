package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Sets;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;

import java.util.Set;

public class GlobalsInliner implements OptimizerPass {

    public int optimize(ImTranslator trans) {
        int obsoleteCount = 0;
        ImProg prog = trans.getImProg();
        prog.clearAttributes(); // TODO only clear read/write attributes

        Set<ImVar> obsoleteVars = Sets.newLinkedHashSet();
        for (final ImVar v : prog.getGlobals()) {
            if (v.attrWrites().size() == 1) {
                ImExpr right = null;
                ImVarWrite obs = null;
                for (ImVarWrite v2 : v.attrWrites()) {
                    ImFunction func = v2.getNearestFunc();
                    if (func.getName().startsWith("init_") || func.getName().equals("main") || func.getName().startsWith("InitTrig_")
                            || func.getName().equals("initGlobals")) {
                        right = v2.getRight();
                        obs = v2;
                        break;
                    }
                }
                if (obs == null) {
                    continue;
                }
                ImExpr replacement;
                if (right instanceof ImIntVal) {
                    ImIntVal val = (ImIntVal) right;
                    replacement = (JassIm.ImIntVal(val.getValI()));
                    if (obs.getParent() != null)
                        obs.replaceBy(JassIm.ImNull());
                } else if (right instanceof ImRealVal) {
                    ImRealVal val = (ImRealVal) right;
                    replacement = (JassIm.ImRealVal(val.getValR()));
                    if (obs.getParent() != null)
                        obs.replaceBy(JassIm.ImNull());
                } else if (right instanceof ImStringVal) {
                    ImStringVal val = (ImStringVal) right;
                    replacement = (JassIm.ImStringVal(val.getValS()));
                    if (obs.getParent() != null)
                        obs.replaceBy(JassIm.ImNull());
                } else if (right instanceof ImBoolVal) {
                    ImBoolVal val = (ImBoolVal) right;
                    replacement = (JassIm.ImBoolVal(val.getValB()));
                    if (obs.getParent() != null)
                        obs.replaceBy(JassIm.ImNull());
                } else {
                    replacement = null;
                }
                if (replacement != null) {
                    for (ImVarRead v3 : v.attrReads()) {
                        v3.replaceBy(replacement.copy());
                    }
                }
                if (replacement != null || v.attrReads().size() == 0) {
                    obsoleteVars.add(v);
                }
            }
        }
        obsoleteCount += obsoleteVars.size();
        for (ImVar i : obsoleteVars) {
            // remove the write
            if (i.attrWrites().size() > 0) {
                ImVarWrite write = Utils.getFirstAndOnly(i.attrWrites());
                if (write.getParent() != null) {
                    write.replaceBy(write.getRight().copy());
                }
            }
        }
        prog.getGlobals().removeAll(obsoleteVars);
        return obsoleteCount;
    }

    @Override
    public String getName() {
        return "Globals Inlined";
    }

}
