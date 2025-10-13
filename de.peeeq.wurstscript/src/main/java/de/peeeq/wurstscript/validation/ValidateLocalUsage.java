package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.NameLink;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 */
public class ValidateLocalUsage {
    public static void checkLocalsUsage(Collection<CompilationUnit> toCheck) {
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
                    if (!(varDef.getInitialExpr() instanceof ExprClosure)) {
                        locals.add(varDef);
                    }
                }
            }
        });

        p.accept(new WPackage.DefaultVisitor() {

            @Override
            public void visit(StmtSet set) {
                super.visit(set);
                LExpr updatedExpr = set.getUpdatedExpr();
                if (updatedExpr != null) {
                    NameLink nameLink = updatedExpr.attrNameLink();
                    if (nameLink != null) {
                        locals.remove(nameLink.getDef());
                    }

                    if (updatedExpr instanceof ExprMemberVar) {
                        checkLeftExpr((ExprMemberVar) updatedExpr);
                    }
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

        for (NameDef local : locals) {
            local.addWarning("Constant local variables should be defined using 'let'.");
        }

    }


}
