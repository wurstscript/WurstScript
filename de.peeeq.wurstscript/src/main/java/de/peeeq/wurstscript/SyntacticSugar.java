package de.peeeq.wurstscript;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.WPos;

import java.util.*;

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
        expandFieldIterations(root);
        addEndFunctionStatements(root);
        replaceTypeIdUse(root);
    }

    /**
     * Expands field-wise operations before name and overload resolution. This gives serializers a
     * reflection-like API while keeping the generated program equivalent to handwritten direct
     * field accesses.
     *
     * <pre>
     * forFields((name, value) -> writer.write(name, value))
     * mapFields((name, value) -> reader.read(name, value))
     * </pre>
     */
    private void expandFieldIterations(CompilationUnit root) {
        List<ExprFunctionCall> calls = new ArrayList<>();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprFunctionCall call) {
                super.visit(call);
                String name = call.getFuncName();
                if ("forFields".equals(name) || "mapFields".equals(name)) {
                    calls.add(call);
                }
            }
        });

        for (ExprFunctionCall call : calls) {
            expandFieldIteration(call, "mapFields".equals(call.getFuncName()));
        }
    }

    private void expandFieldIteration(ExprFunctionCall call, boolean assignsResult) {
        if (!(call.getParent() instanceof WStatements statements)) {
            call.addError(call.getFuncName() + " can only be used as a statement.");
            return;
        }
        ClassDef classDef = call.attrNearestClassDef();
        if (classDef == null || !call.attrIsDynamicContext()) {
            call.addError(call.getFuncName() + " can only be used in an instance method or constructor.");
            return;
        }
        if (call.getArgs().size() != 1 || !(call.getArgs().get(0) instanceof ExprClosure closure)
            || closure.getShortParameters().size() != 2) {
            call.addError(call.getFuncName() + " expects a closure with (fieldName, fieldValue) parameters.");
            return;
        }

        String nameParameter = closure.getShortParameters().get(0).getName();
        String valueParameter = closure.getShortParameters().get(1).getName();
        if (!assignsResult && !(closure.getImplementation() instanceof WStatement)) {
            call.addError("forFields closure must produce a statement expression.");
            return;
        }
        int statementIndex = statements.indexOf(call);
        statements.remove(statementIndex);

        for (GlobalVarDef field : classDef.getVars()) {
            if (field.attrIsStatic()) {
                continue;
            }
            Expr fieldAccess = fieldAccess(call.getSource(), field.getName());
            Expr implementation = substituteFieldParameters(
                closure.getImplementation().copy(), nameParameter, valueParameter, field.getName());
            WStatement expanded;
            if (assignsResult) {
                expanded = Ast.StmtSet(call.getSource(), (LExpr) fieldAccess, implementation);
            } else {
                expanded = (WStatement) implementation;
            }
            statements.add(statementIndex++, expanded);
        }
    }

    private Expr substituteFieldParameters(Expr expression, String nameParameter,
                                           String valueParameter, String fieldName) {
        if (expression instanceof ExprVarAccess access) {
            if (access.getVarName().equals(nameParameter)) {
                return Ast.ExprStringVal(access.getSource(), fieldName);
            }
            if (access.getVarName().equals(valueParameter)) {
                return fieldAccess(access.getSource(), fieldName);
            }
        }

        List<ExprVarAccess> accesses = new ArrayList<>();
        expression.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprVarAccess access) {
                super.visit(access);
                if (access.getVarName().equals(nameParameter) || access.getVarName().equals(valueParameter)) {
                    accesses.add(access);
                }
            }
        });
        for (ExprVarAccess access : accesses) {
            Expr replacement = access.getVarName().equals(nameParameter)
                ? Ast.ExprStringVal(access.getSource(), fieldName)
                : fieldAccess(access.getSource(), fieldName);
            access.replaceBy(replacement);
        }
        return expression;
    }

    private ExprMemberVarDot fieldAccess(WPos source, String fieldName) {
        return Ast.ExprMemberVarDot(source, Ast.ExprThis(source), Ast.Identifier(source, fieldName));
    }

    private void replaceTypeIdUse(CompilationUnit root) {
        final Map<Expr, Expr> replacements = Maps.newLinkedHashMap();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprMemberVarDot e) {
                super.visit(e);
                // OPTIMIZATION 1: Quick string comparison before creating replacement
                if ("typeId".equals(e.getVarName())) {
                    replacements.put(e, Ast.ExprTypeId(e.getSource(), e.getLeft().copy()));
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
                // OPTIMIZATION 2: Check operator first (cheapest check)
                if (e.getOpU() == WurstOperator.UNARY_MINUS) {
                    Expr right = e.getRight();
                    if (right instanceof ExprIntVal) {
                        ExprIntVal iv = (ExprIntVal) right;
                        ExprIntVal newExpr = Ast.ExprIntVal(e.getSource(), "-" + iv.getValIraw());
                        replacements.put(e, newExpr);
                    }
                }
            }
        });
        doReplacements(replacements, "Cannot use unary minus here");
    }

    private void doReplacements(Map<Expr, Expr> replacements, String msg) {
        for (Map.Entry<Expr, Expr> e : replacements.entrySet()) {
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
        // OPTIMIZATION 3: Use indexed loop for better performance
        for (int i = 0, size = parent.size(); i < size; i++) {
            if (parent.get(i) == oldE) {
                parent.set(i, newE);
                return;
            }
        }
        throw new Error("could not replace " + oldE + " with " + newE);
    }

    private void addEndFunctionStatements(CompilationUnit root) {
        // OPTIMIZATION 4: Single visitor handles all function-like elements
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
                // OPTIMIZATION 5: Reuse same WPos for both statements
                WPos pos = f.attrSource();
                pos = pos.withRightPos(pos.getLeftPos() - 1);

                // OPTIMIZATION 6: Add both at once to avoid list resizing
                WStatements body = f.getBody();
                body.add(0, Ast.StartFunctionStatement(pos));
                body.add(Ast.EndFunctionStatement(pos));
            }
        });
    }

    private void addDefaultImports(CompilationUnit root) {
        // OPTIMIZATION 7: Pre-collect packages to avoid nested iteration
        List<WPackage> packages = root.attrGetByType().packageDefs;
        if (packages.isEmpty()) {
            return;
        }

        nextPackage:
        for (WPackage p : packages) {
            // OPTIMIZATION 8: Check for imports before creating artificial source
            boolean hasWurst = false;
            boolean hasNoWurst = false;

            for (WImport imp : p.getImports()) {
                String pkgName = imp.getPackagename();
                if ("Wurst".equals(pkgName)) {
                    hasWurst = true;
                    continue nextPackage;
                }
                if ("NoWurst".equals(pkgName)) {
                    hasNoWurst = true;
                    continue nextPackage;
                }
            }

            // Only create artificial source if we need to add import
            if (!hasWurst && !hasNoWurst) {
                WPos source = p.getSource().artificial();
                p.getImports().add(Ast.WImport(source, false, false, Ast.Identifier(source, "Wurst")));
            }
        }
    }

    /**
     * add a empty default constructor to every class without any constructor
     */
    private void addDefaultConstructors(CompilationUnit root) {
        // OPTIMIZATION 9: Direct access to classes list
        List<ClassDef> classes = root.attrGetByType().classes;
        if (classes.isEmpty()) {
            return;
        }

        for (ClassDef c : classes) {
            // OPTIMIZATION 10: Use isEmpty() instead of size() == 0
            if (c.getConstructors().isEmpty()) {
                // OPTIMIZATION 11: Create source position only when needed
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
