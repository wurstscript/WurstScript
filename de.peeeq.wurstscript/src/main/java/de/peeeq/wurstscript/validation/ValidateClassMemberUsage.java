package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ValidateClassMemberUsage {
    public static void checkClassMembers(List<CompilationUnit> toCheck) {
        for (CompilationUnit cu : toCheck) {
            for (WPackage p : cu.getPackages()) {
                checkVarUsage(p);
                checkFuncUsage(p);
            }
        }
    }

    private static void checkVarUsage(WPackage p) {
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
                    definedVars.remove(nameDef);
                }
            }

            @Override
            public void visit(ExprVarArrayAccess e) {
                super.visit(e);
                NameDef nameDef = e.attrNameDef();
                if (nameDef instanceof GlobalVarDef) {
                    definedVars.remove(nameDef);
                }
            }

        });

        definedVars.forEach(var -> {
            if (var.attrIsPrivate()) {
                var.addWarning("Private variable <" + var.getName() + "> is never read.");
            }
        });

    }

    private static void checkFuncUsage(WPackage p) {
        Set<FuncDef> definedFuncs = new HashSet<>();

        p.accept(new WPackage.DefaultVisitor() {

            @Override
            public void visit(ClassDef c) {
                super.visit(c);
                definedFuncs.addAll(c.getMethods());
            }

        });

        p.accept(new WPackage.DefaultVisitor() {

            @Override
            public void visit(ExprFunctionCall e) {
                super.visit(e);
                definedFuncs.removeIf(funcDef -> funcDef.getName().equals(e.getFuncName()));
            }

            @Override
            public void visit(ExprFuncRef e) {
                super.visit(e);
                definedFuncs.removeIf(funcDef -> funcDef.getName().equals(e.getFuncName()));
            }

        });

        definedFuncs.forEach(funcDef -> {
            if (funcDef.attrIsPrivate()) {
                funcDef.addWarning("Private function <" + funcDef.getName() + "> is never used.");
            }
        });

    }
}
