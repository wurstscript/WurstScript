package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ValidateMemberVarUsage {
    public static void checkMemberVarUsage(List<CompilationUnit> toCheck) {
        for (CompilationUnit cu : toCheck) {
            for (WPackage p : cu.getPackages()) {
                checkMemberVarUsage(p);
            }
        }
    }

    private static void checkMemberVarUsage(WPackage p) {
        Set<GlobalVarDef> definedVars = new HashSet<>();

        p.accept(new WPackage.DefaultVisitor() {

            @Override
            public void visit(ClassDef c) {
                super.visit(c);
                definedVars.addAll(c.getVars());
            }

        });

        p.accept(new WPackage.DefaultVisitor() {

            @Override
            public void visit(ExprVarAccess e) {
                super.visit(e);
                NameDef nameDef = e.attrNameDef();
                if (nameDef instanceof GlobalVarDef) {
                    GlobalVarDef g = (GlobalVarDef) nameDef;
                    definedVars.remove(g);
                }
            }

            @Override
            public void visit(ExprVarArrayAccess e) {
                super.visit(e);
                NameDef nameDef = e.attrNameDef();
                if (nameDef instanceof GlobalVarDef) {
                    GlobalVarDef g = (GlobalVarDef) nameDef;
                    definedVars.remove(g);
                }
            }

        });

        definedVars.forEach(var -> {
            if (var.attrIsPrivate()) {
                var.addWarning("Private variable <" + var.getName() + "> is never read.");
            }
        });

    }
}
