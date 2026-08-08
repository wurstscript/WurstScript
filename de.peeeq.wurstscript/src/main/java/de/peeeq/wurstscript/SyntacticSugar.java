package de.peeeq.wurstscript;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstTypeClass;

import java.util.*;

/**
 * general rules for syntactic sugar:
 * <p>
 * 1. operations must be idempotent: syntacticSugar(syntacticSugar(program)) = syntacticSugar(program)
 * 2. operations must not depend on other compilation units.
 */
public class SyntacticSugar {

    /** Compiler-reserved spelling keeps ordinary user functions named forFields/mapFields valid. */
    private static final String FOR_FIELDS = "__wurst_forFields";
    private static final String MAP_FIELDS = "__wurst_mapFields";

    public void removeSyntacticSugar(CompilationUnit root, boolean hasCommonJ) {
        if (hasCommonJ) {
            addDefaultImports(root);
        }
        rewriteNegatedInts(root);
        addDefaultConstructors(root);
        addEndFunctionStatements(root);
        replaceTypeIdUse(root);
    }

    /**
     * Expands field iteration after module methods have been copied into their consuming classes.
     * This must run after {@link ModuleExpander#expandModules(CompilationUnit)} so a module callback can
     * see all fields of the concrete class using it.
     */
    public void expandFieldIterations(CompilationUnit root) {
        expandFieldIterationsInTree(root);
    }

    /**
     * Expands field-wise operations before name and overload resolution. This gives serializers a
     * reflection-like API while keeping the generated program equivalent to handwritten direct
     * field accesses.
     *
     * <pre>
     * __wurst_forFields((name, value) -> writer.write(name, value))
     * __wurst_mapFields((name, value) -> reader.read(name, value))
     * </pre>
     */
    private void expandFieldIterationsInTree(CompilationUnit root) {
        List<ExprFunctionCall> calls = new ArrayList<>();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprFunctionCall call) {
                super.visit(call);
                String name = call.getFuncName();
                if (FOR_FIELDS.equals(name) || MAP_FIELDS.equals(name)) {
                    calls.add(call);
                }
            }
        });

        for (ExprFunctionCall call : calls) {
            expandFieldIteration(call, MAP_FIELDS.equals(call.getFuncName()));
        }
    }

    private void expandFieldIteration(ExprFunctionCall call, boolean assignsResult) {
        if (!(call.getParent() instanceof WStatements statements)) {
            call.addError(call.getFuncName() + " can only be used as a statement.");
            return;
        }
        ClassDef classDef = call.attrNearestClassDef();
        ClassOrModule owner = call.attrNearestClassOrModule();
        if (!call.attrIsDynamicContext()) {
            call.addError(call.getFuncName() + " can only be used in an instance method or constructor.");
            return;
        }
        if (owner instanceof ModuleDef) {
            // Module bodies are templates. Their copies were made by ModuleExpander; validate and
            // expand those concrete copies instead of type-checking this uninstantiated template.
            statements.remove(call);
            return;
        }
        if (classDef == null) {
            call.addError(call.getFuncName() + " can only be used in an instance method or constructor.");
            return;
        }
        if (call.getArgs().size() != 1 || !(call.getArgs().get(0) instanceof ExprClosure closure)
            || closure.getShortParameters().size() != 2) {
            call.addError(call.getFuncName() + " expects a closure with (fieldName, fieldValue) parameters.");
            return;
        }
        for (WShortParameter parameter : closure.getShortParameters()) {
            if (!(parameter.getTypOpt() instanceof NoTypeExpr)) {
                parameter.addError("Field iteration closure parameters must use inferred types.");
                return;
            }
        }

        String nameParameter = closure.getShortParameters().get(0).getName();
        String valueParameter = closure.getShortParameters().get(1).getName();
        if (nameParameter.equals(valueParameter)) {
            closure.getShortParameters().get(1).addError(
                "Field iteration closure parameters must have distinct names.");
            return;
        }
        if (hasShadowingLocal(closure, nameParameter, valueParameter)) {
            call.addError("Field iteration callbacks cannot declare locals or loop variables named "
                + nameParameter + " or " + valueParameter + ".");
            return;
        }
        if (!assignsResult && !(closure.getImplementation() instanceof WStatement)) {
            call.addError("forFields closure must produce a statement expression.");
            return;
        }
        List<GlobalVarDef> fields = collectInstanceFields(classDef, owner);
        if (fields.isEmpty()) {
            call.addError(call.getFuncName() + " requires at least one instance field.");
            return;
        }
        int statementIndex = statements.indexOf(call);
        statements.remove(statementIndex);

        for (GlobalVarDef field : fields) {
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

    private List<GlobalVarDef> collectInstanceFields(ClassDef classDef, ClassOrModule owner) {
        List<GlobalVarDef> fields = new ArrayList<>();
        if (classDef != null) {
            collectInheritedFields(classDef.attrTypC(), fields, Collections.newSetFromMap(new IdentityHashMap<>()));
        } else if (owner instanceof ModuleDef moduleDef) {
            addInstanceFields(moduleDef.getVars(), fields);
        }
        return fields;
    }

    private void collectInheritedFields(WurstTypeClass type,
                                        List<GlobalVarDef> fields, Set<ClassDef> visited) {
        if (!visited.add(type.getClassDef())) {
            return;
        }
        WurstTypeClass superType = type.extendedClass();
        if (superType != null) {
            collectInheritedFields(superType, fields, visited);
        }
        addInstanceFields(type.getClassDef().getVars(), fields);
    }

    private void addInstanceFields(Iterable<GlobalVarDef> declarations, List<GlobalVarDef> fields) {
        for (GlobalVarDef field : declarations) {
            if (!field.attrIsStatic()) {
                fields.add(field);
            }
        }
    }

    private boolean hasShadowingLocal(ExprClosure closure, String nameParameter, String valueParameter) {
        final boolean[] result = {false};
        closure.getImplementation().accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprClosure nestedClosure) {
                // Nested closures have independent parameter scopes.
            }

            @Override
            public void visit(LocalVarDef localVarDef) {
                super.visit(localVarDef);
                if (localVarDef.getName().equals(nameParameter) || localVarDef.getName().equals(valueParameter)) {
                    result[0] = true;
                }
            }
        });
        return result[0];
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
            private final Deque<Set<String>> shadowedScopes = new ArrayDeque<>();

            private boolean isShadowed(String name) {
                return shadowedScopes.stream().anyMatch(scope -> scope.contains(name));
            }

            @Override
            public void visit(WStatements statements) {
                Set<String> blockBindings = new HashSet<>();
                for (WStatement statement : statements) {
                    if (statement instanceof LocalVarDef localVarDef
                        && (localVarDef.getName().equals(nameParameter)
                            || localVarDef.getName().equals(valueParameter))) {
                        blockBindings.add(localVarDef.getName());
                    }
                }
                shadowedScopes.push(blockBindings);
                super.visit(statements);
                shadowedScopes.pop();
            }

            private void visitLoopVariable(LocalVarDef loopVariable) {
                loopVariable.getModifiers().accept(this);
                loopVariable.getOptTyp().accept(this);
                loopVariable.getInitialExpr().accept(this);
            }

            private void visitLoopBody(LocalVarDef loopVariable, WStatements body) {
                shadowedScopes.push(Set.of(loopVariable.getName()));
                body.accept(this);
                shadowedScopes.pop();
            }

            @Override
            public void visit(StmtForRangeUp loop) {
                visitLoopVariable(loop.getLoopVar());
                loop.getTo().accept(this);
                loop.getStep().accept(this);
                visitLoopBody(loop.getLoopVar(), loop.getBody());
            }

            @Override
            public void visit(StmtForRangeDown loop) {
                visitLoopVariable(loop.getLoopVar());
                loop.getTo().accept(this);
                loop.getStep().accept(this);
                visitLoopBody(loop.getLoopVar(), loop.getBody());
            }

            @Override
            public void visit(StmtForIn loop) {
                visitLoopVariable(loop.getLoopVar());
                loop.getIn().accept(this);
                visitLoopBody(loop.getLoopVar(), loop.getBody());
            }

            @Override
            public void visit(StmtForFrom loop) {
                visitLoopVariable(loop.getLoopVar());
                loop.getIn().accept(this);
                visitLoopBody(loop.getLoopVar(), loop.getBody());
            }

            @Override
            public void visit(ExprClosure nestedClosure) {
                Set<String> shadowed = new HashSet<>();
                for (WShortParameter parameter : nestedClosure.getShortParameters()) {
                    shadowed.add(parameter.getName());
                }
                shadowedScopes.push(shadowed);
                super.visit(nestedClosure);
                shadowedScopes.pop();
            }

            @Override
            public void visit(LocalVarDef localVarDef) {
                super.visit(localVarDef);
                if (!shadowedScopes.isEmpty()
                    && (localVarDef.getName().equals(nameParameter)
                        || localVarDef.getName().equals(valueParameter))) {
                    shadowedScopes.peek().add(localVarDef.getName());
                }
            }

            @Override
            public void visit(ExprVarAccess access) {
                super.visit(access);
                if (!isShadowed(access.getVarName())
                    && (access.getVarName().equals(nameParameter) || access.getVarName().equals(valueParameter))) {
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
