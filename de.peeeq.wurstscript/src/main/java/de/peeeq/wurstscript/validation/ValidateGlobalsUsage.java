package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.*;

import java.util.*;

/**
 *
 */
public class ValidateGlobalsUsage {
    public static void checkGlobalsUsage(List<CompilationUnit> toCheck) {
        Map<NameDef, Element> usedGlobals = new HashMap<>();
        for (CompilationUnit cu : toCheck) {
            checkJassGlobals(cu.getJassDecls(), usedGlobals);
            for (WPackage p : cu.getPackages()) {
                checkGlobalsUsage(p);
            }
        }
    }

    private static void checkJassGlobals(JassToplevelDeclarations jassDecls, Map<NameDef, Element> usedGlobals) {
        for (JassToplevelDeclaration jassDecl : jassDecls) {
            if (jassDecl instanceof JassGlobalBlock) {
                JassGlobalBlock globals = (JassGlobalBlock) jassDecl;
                for (GlobalVarDef glob : globals) {
                    if (!glob.getSource().getFile().endsWith("common.j") && !glob.getSource().getFile().endsWith("blizzard.j")
                      && !glob.getSource().getFile().endsWith("war3map.j")) {
                        glob.getInitialExpr().accept(new Element.DefaultVisitor() {
                          @Override
                          public void visit(ExprVarAccess e) {
                            usedGlobals.put(e.attrNameDef(), e);
                          }
                        });
                        Element use = usedGlobals.get(glob);
                        if (use != null) {
                            use.addWarning("Global variable " + glob.getName() + " used before it is declared.");
                        }
                    }
                }
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
                NameDef nameDef = e.attrNameDef();
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
