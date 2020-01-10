package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.VarLink;
import de.peeeq.wurstscript.jassIm.ImVarWrite;

import java.util.*;

/**
 *
 */
public class ValidateLocalUsage {
    public static void checkLocalsUsage(List<CompilationUnit> toCheck) {
        Map<NameDef, Element> usedGlobals = new HashMap<>();
        for (CompilationUnit cu : toCheck) {
            for (WPackage p : cu.getPackages()) {
                checkGlobalsUsage(p);
            }
        }
    }

    private static void checkGlobalsUsage(WPackage p) {
        HashSet<NameDef> locals = new HashSet<>();

        p.accept(new WPackage.DefaultVisitor() {

            @Override
            public void visit(LocalVarDef varDef) {
                super.visit(varDef);
                if (!varDef.attrIsConstant() && !(varDef.getParent() instanceof LoopStatement)) {
                    locals.add(varDef);
                }
            }
        });

        p.accept(new WPackage.DefaultVisitor() {

            @Override
            public void visit(StmtSet set) {
                super.visit(set);
                NameLink nameLink = set.getUpdatedExpr().attrNameLink();
                locals.remove(nameLink.getDef());

            }
        });

        locals.forEach(local -> local.addWarning("Constant local variables should be defined using 'let'."));

    }
}
