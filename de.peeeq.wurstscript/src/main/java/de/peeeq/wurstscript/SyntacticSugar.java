package de.peeeq.wurstscript;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.WPos;

import java.util.Map;
import java.util.Map.Entry;

/**
 * general rules for syntactic sugar:
 * <p>
 * 1. operations must be idempotent: syntacticSugar(syntacticSugar(program)) = syntacticSugar(program)
 * 2. operations must not depend on other compilation units.
 */
public class SyntacticSugar {

    public void removeSyntacticSugar(CompilationUnit root, boolean hasCommonJ) {
        if (hasCommonJ) {
            addDefaultImports(root);
        }
        rewriteNegatedInts(root);
        addDefaultConstructors(root);
        addEndFunctionStatements(root);
        replaceTypeIdUse(root);
    }


    private void replaceTypeIdUse(CompilationUnit root) {
        final Map<Expr, Expr> replacements = Maps.newLinkedHashMap();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprMemberVarDot e) {
                super.visit(e);
                if (e.getVarName().equals("typeId")) {
                    replacements.put(e, Ast.ExprTypeId(e.getSource(), (Expr) e.getLeft().copy()));
                }
            }
        });
        doReplacements(replacements, "Cannot use typeId here");
    }


    private void rewriteNegatedInts(CompilationUnit root) {
        final Map<Expr, Expr> replacements = Maps.newLinkedHashMap();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprUnary e) {
                super.visit(e);
                if (e.getOpU() == WurstOperator.UNARY_MINUS
                        && e.getRight() instanceof ExprIntVal) {
                    ExprIntVal iv = (ExprIntVal) e.getRight();
                    ExprIntVal newExpr = Ast.ExprIntVal(e.getSource(), "-" + iv.getValIraw());
                    replacements.put(e, newExpr);
                }
            }
        });
        doReplacements(replacements, "Cannot use typeId here");
    }


    private void doReplacements(Map<Expr, Expr> replacements, String msg) {
        for (Entry<Expr, Expr> e : replacements.entrySet()) {
            Expr oldE = e.getKey();
            Expr newE = e.getValue();
            try {
                doSingleReplacement(oldE, newE);
            } catch (ClassCastException ex) {
                oldE.addError(msg);
            }
        }

    }

    public void doSingleReplacement(Expr oldE, Expr newE) throws Error {
        Element parent = oldE.getParent();
        for (int i = 0; i < parent.size(); i++) {
            if (parent.get(i) == oldE) {
                parent.set(i, newE);
                return;
            }
        }
        throw new Error("could not replace " + oldE + " with " + newE);
    }

    private void addEndFunctionStatements(CompilationUnit root) {

        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExtensionFuncDef f) {
                super.visit(f);
                addEnd(f);
            }


            @Override
            public void visit(FuncDef f) {
                super.visit(f);
                addEnd(f);
            }

            @Override
            public void visit(ConstructorDef f) {
                super.visit(f);
                addEnd(f);
            }

            @Override
            public void visit(InitBlock f) {
                super.visit(f);
                addEnd(f);
            }


            @Override
            public void visit(OnDestroyDef f) {
                super.visit(f);
                addEnd(f);
            }

            @Override
            public void visit(ExprStatementsBlock f) {
                super.visit(f);
                addEnd(f);
            }

            private void addEnd(AstElementWithBody f) {
                WPos pos = f.attrSource();
                pos = pos.withRightPos(pos.getLeftPos() - 1);
                f.getBody().add(Ast.EndFunctionStatement(pos));
                f.getBody().add(0, Ast.StartFunctionStatement(pos));
            }

        });

    }


    private void addDefaultImports(CompilationUnit root) {
        nextPackage:
        for (WPackage p : root.attrGetByType().packageDefs) {
            // add 'import Wurst' if it does not exist
            for (WImport imp : p.getImports()) {
                if (imp.getPackagename().equals("Wurst")) {
                    // wurst package already imported
                    continue nextPackage;
                }
                if (imp.getPackagename().equals("NoWurst")) {
                    // NoWurst package imported --> no standard lib wanted
                    continue nextPackage;
                }
            }
            WPos source = p.getSource();
            source = source.withRightPos(source.getLeftPos() - 1);
            p.getImports().add(Ast.WImport(source, false, false, Ast.Identifier(source, "Wurst")));
        }
    }


    /**
     * add a empty default constructor to every class without any constructor
     */
    private void addDefaultConstructors(CompilationUnit root) {
        for (ClassDef c : root.attrGetByType().classes) {
            if (c.getConstructors().size() == 0) {
                // add default constructor if none exists:
                WPos source = c.getSource().withRightPos(c.getSource().getLeftPos() - 1);
                c.getConstructors().add(Ast.ConstructorDef(
                        source,
                        Ast.Modifiers(),
                        Ast.WParameters(),
                        Ast.NoSuperConstructorCall(),
                        Ast.WStatements()));
            }
        }
    }

}
