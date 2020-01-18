package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.NameLink;

import java.util.*;

/**
 *
 */
public class ValidateLocalUsage {
    public static void checkLocalsUsage(List<CompilationUnit> toCheck) {
        Map<NameDef, Element> usedGlobals = new HashMap<>();
        for (CompilationUnit cu : toCheck) {
            for (WPackage p : cu.getPackages()) {
                checkLocalsUsage(p);
            }
        }
    }

    private static void checkLocalsUsage(WPackage p) {
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

                if (set.getUpdatedExpr() != null && set.getUpdatedExpr() instanceof ExprMemberVar) {
                    checkLeftExpr((ExprMemberVar) set.getUpdatedExpr());
                }

            }

            private void checkLeftExpr(ExprMemberVar updatedExpr) {
                if (updatedExpr.getLeft() != null) {
                    if (updatedExpr.getLeft() instanceof ExprMemberVar) {
                        checkLeftExpr((ExprMemberVar) updatedExpr.getLeft());
                    } else {
                        locals.remove(updatedExpr.getLeft().tryGetNameDef());
                    }
                }
            }
        });

        locals.forEach(local -> local.addWarning("Constant local variables should be defined using 'let'."));

    }


}
