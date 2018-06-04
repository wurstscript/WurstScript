package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ValidateGlobalsUsage {
    public static void checkGlobalsUsage(List<CompilationUnit> toCheck) {
        for (CompilationUnit cu : toCheck) {
            for (WPackage p : cu.getPackages()) {
                checkGlobalsUsage(p);
            }
        }
    }

    private static void checkGlobalsUsage(WPackage p) {
        Set<GlobalVarDef> definedVars = new HashSet<>();

        p.accept(new WPackage.DefaultVisitor() {

            @Override
            public void visit(GlobalVarDef g) {
                super.visit(g);
                definedVars.add(g);
            }

            @Override
            public void visit(ExprVarAccess e) {
                super.visit(e);
                NameDef nameDef = e.attrNameDef().getDef();
                if (nameDef instanceof GlobalVarDef) {
                    GlobalVarDef g = (GlobalVarDef) nameDef;
                    if (!definedVars.contains(g)
                            && !g.attrIsDynamicClassMember()
                            && g.attrNearestNamedScope() == p) {
                        e.addWarning("Global variable " + e.getVarName() + " must be declared before it is used. " +
                                "This will be an error in future Wurst versions.");
                        // add variable to defined vars to silence further warnings:
                        definedVars.add(g);
                    }
                }
            }
        });


    }
}
