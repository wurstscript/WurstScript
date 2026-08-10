package de.peeeq.wurstscript;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeTypeParam;

import java.util.*;

/**
 * general rules for syntactic sugar:
 * <p>
 * 1. operations must be idempotent: syntacticSugar(syntacticSugar(program)) = syntacticSugar(program)
 * 2. operations must not depend on other compilation units.
 */
public class SyntacticSugar {

    private int generatedTargetCounter;
    public static final class DeferredModuleCall {
        private final WStatements statements;
        private final int index;
        private final ExprFunctionCall call;
        private final List<WStatement> generatedStatements;

        private DeferredModuleCall(WStatements statements, int index, ExprFunctionCall call) {
            this(statements, index, call, List.of());
        }

        private DeferredModuleCall(WStatements statements, int index, ExprFunctionCall call,
                                   List<WStatement> generatedStatements) {
            this.statements = statements;
            this.index = index;
            this.call = call;
            this.generatedStatements = generatedStatements;
        }
    }

    public static final class DirectFieldIterationState {
        private final List<DeferredModuleCall> detached;

        private DirectFieldIterationState(List<DeferredModuleCall> detached) {
            this.detached = detached;
        }
    }

    private static final class FieldInfo {
        private final GlobalVarDef declaration;
        private final List<String> modulePath;

        private FieldInfo(GlobalVarDef declaration, List<String> modulePath) {
            this.declaration = declaration;
            this.modulePath = List.copyOf(modulePath);
        }

        private String key() {
            if (modulePath.isEmpty()) {
                return declaration.getName();
            }
            return String.join(".", modulePath) + "." + declaration.getName();
        }
    }

    public static boolean isFieldIterationIntrinsic(ExprFunctionCall call) {
        return CompilerIntrinsics.isFieldIteration(call);
    }

    public static boolean isUninstantiatedModuleFieldIteration(ExprFunctionCall call) {
        return isFieldIterationIntrinsic(call)
            && call.getArgs().size() == 1
            && call.attrNearestClassDef() == null
            && call.attrNearestClassOrModule() instanceof ModuleDef;
    }

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
        List<DeferredModuleCall> detached = expandFieldIterationsInTree(root);
        if (!detached.isEmpty() && root.getCuInfo() != null) {
            root.getCuInfo().setDirectFieldIterationState(new DirectFieldIterationState(detached));
        }
    }

    /** Restores source intrinsics before an incremental compilation-unit recheck. */
    public static void restoreDirectFieldIterations(CompilationUnit root) {
        if (root.getCuInfo() != null) {
            DirectFieldIterationState state = root.getCuInfo().getDirectFieldIterationState();
            root.getCuInfo().setDirectFieldIterationState(null);
            if (state != null) {
                new SyntacticSugar().restoreModuleTemplateFieldIterations(state.detached);
            }
        }
    }

    /** Temporarily removes template intrinsics while validation runs; callers must restore them. */
    public List<DeferredModuleCall> detachModuleTemplateFieldIterations(CompilationUnit root) {
        List<DeferredModuleCall> detached = new ArrayList<>();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprFunctionCall call) {
                super.visit(call);
                if (isUninstantiatedModuleFieldIteration(call)
                    && call.getParent() instanceof WStatements statements) {
                    int index = statements.indexOf(call);
                    detached.add(new DeferredModuleCall(statements, index, call));
                }
            }
        });
        for (int i = detached.size() - 1; i >= 0; i--) {
            DeferredModuleCall state = detached.get(i);
            state.statements.remove(state.index);
        }
        return detached;
    }

    public void restoreModuleTemplateFieldIterations(List<DeferredModuleCall> detached) {
        for (int i = detached.size() - 1; i >= 0; i--) {
            DeferredModuleCall state = detached.get(i);
            for (WStatement generated : state.generatedStatements) {
                state.statements.remove(generated);
            }
            int index = Math.min(state.index, state.statements.size());
            state.statements.add(index, state.call);
        }
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
    private List<DeferredModuleCall> expandFieldIterationsInTree(CompilationUnit root) {
        List<ExprFunctionCall> calls = new ArrayList<>();
        root.accept(new WurstModel.DefaultVisitor() {
            @Override
            public void visit(ExprFunctionCall call) {
                super.visit(call);
                if (isFieldIterationIntrinsic(call)) {
                    calls.add(call);
                }
            }
        });

        List<DeferredModuleCall> detached = new ArrayList<>();
        for (ExprFunctionCall call : calls) {
            expandFieldIteration(call, CompilerIntrinsics.isMapFields(call), detached);
        }
        return detached;
    }

    private void expandFieldIteration(ExprFunctionCall call,
                                      boolean assignsResult,
                                      List<DeferredModuleCall> detached) {
        if (!(call.getParent() instanceof WStatements statements)) {
            call.addError(call.getFuncName() + " can only be used as a statement.");
            return;
        }
        boolean explicitTarget = call.getArgs().size() == 2;
        int closureIndex = explicitTarget ? 1 : 0;
        if ((!explicitTarget && call.getArgs().size() != 1)
            || !(call.getArgs().get(closureIndex) instanceof ExprClosure closure)
            || closure.getShortParameters().size() != 2) {
            call.addError(call.getFuncName()
                + " expects a closure with (fieldName, fieldValue) parameters, optionally preceded by a target.");
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
        ClassDef classDef;
        ClassOrModule owner;
        Expr target = null;
        String targetName = null;
        LocalVarDef targetVariable = null;
        int originalStatementIndex = statements.indexOf(call);
        if (explicitTarget) {
            target = call.getArgs().get(0);
            do {
                targetName = "__wurstFieldTarget" + generatedTargetCounter++;
            } while (call.lookupVar(targetName, false) != null);
            targetVariable = Ast.LocalVarDef(call.getSource(), Ast.Modifiers(), Ast.NoTypeExpr(),
                Ast.Identifier(call.getSource(), targetName), target.copy());
            statements.add(originalStatementIndex, targetVariable);
            statements.clearAttributes();
            WurstType targetType = target.attrTyp();
            if (targetType instanceof WurstTypeTypeParam) {
                statements.remove(targetVariable);
                statements.clearAttributes();
                call.addError(call.getFuncName() + " target type " + targetType
                    + " is not concrete here. Move field mapping into a callback with a concrete target type.");
                return;
            }
            if (!(targetType instanceof WurstTypeClass targetClass) || targetClass.isStaticRef()) {
                statements.remove(targetVariable);
                statements.clearAttributes();
                call.addError(call.getFuncName() + " target must have a concrete class type, but found "
                    + targetType + ".");
                return;
            }
            classDef = targetClass.getClassDef();
            owner = classDef;
        } else {
            classDef = call.attrNearestClassDef();
            owner = call.attrNearestClassOrModule();
            if (!call.attrIsDynamicContext()) {
                call.addError(call.getFuncName() + " can only be used in an instance method or constructor.");
                return;
            }
            if (owner instanceof ModuleDef) {
                // Module bodies are templates. Their copies were made by ModuleExpander; validate and
                // expand those concrete copies instead of type-checking this uninstantiated template.
                return;
            }
            if (classDef == null) {
                call.addError(call.getFuncName() + " can only be used in an instance method or constructor.");
                return;
            }
        }
        List<FieldInfo> fields = collectInstanceFields(classDef, owner, call, explicitTarget, assignsResult);
        if (fields.isEmpty()) {
            if (targetVariable != null) {
                statements.remove(targetVariable);
                statements.clearAttributes();
            }
            call.addError(call.getFuncName()
                + " requires at least one instance field; no accessible mutable instance fields were found.");
            return;
        }
        int statementIndex = statements.indexOf(call);
        detached.add(new DeferredModuleCall(statements, originalStatementIndex, call));
        statements.remove(statementIndex);

        List<WStatement> generatedStatements = new ArrayList<>(fields.size() + (explicitTarget ? 1 : 0));
        if (targetVariable != null) {
            generatedStatements.add(targetVariable);
        }
        for (FieldInfo field : fields) {
            String fieldKey = field.key();
            Expr fieldAccess = fieldAccess(call.getSource(), field, targetName);
            Expr implementation = substituteFieldParameters(
                closure.getImplementation().copy(), nameParameter, valueParameter, fieldKey, field, targetName);
            WStatement expanded;
            if (assignsResult) {
                expanded = Ast.StmtSet(call.getSource(), (LExpr) fieldAccess, implementation);
            } else {
                expanded = (WStatement) implementation;
            }
            generatedStatements.add(expanded);
            statements.add(statementIndex++, expanded);
        }
        detached.set(detached.size() - 1,
            new DeferredModuleCall(statements, originalStatementIndex, call, generatedStatements));
    }

    private List<FieldInfo> collectInstanceFields(ClassDef classDef, ClassOrModule owner,
                                                  Element accessSite, boolean explicitTarget,
                                                  boolean requireMutable) {
        List<FieldInfo> fields = new ArrayList<>();
        if (classDef != null) {
            collectInheritedFields(classDef.attrTypC(), fields, classDef,
                Collections.newSetFromMap(new IdentityHashMap<>()),
                Collections.newSetFromMap(new IdentityHashMap<>()), accessSite, explicitTarget,
                requireMutable);
        } else if (owner instanceof ModuleDef moduleDef) {
            addInstanceFields(moduleDef.getVars(), fields, null, List.of(), moduleDef,
                accessSite, explicitTarget, requireMutable);
        }
        return fields;
    }

    private void collectInheritedFields(WurstTypeClass type,
                                        List<FieldInfo> fields,
                                        ClassDef concreteClass,
                                        Set<ClassDef> visitedClasses,
                                        Set<ModuleInstanciation> visitedModules,
                                        Element accessSite,
                                        boolean explicitTarget,
                                        boolean requireMutable) {
        if (!visitedClasses.add(type.getClassDef())) {
            return;
        }
        WurstTypeClass superType = type.extendedClass();
        if (superType != null) {
            collectInheritedFields(superType, fields, concreteClass, visitedClasses, visitedModules,
                accessSite, explicitTarget, requireMutable);
        }
        addModuleFields(type.getClassDef().getModuleInstanciations(), fields, concreteClass,
            visitedModules, List.of(), accessSite, explicitTarget, requireMutable);
        addInstanceFields(type.getClassDef().getVars(), fields, concreteClass, List.of(), null,
            accessSite, explicitTarget, requireMutable);
    }

    private void addModuleFields(Iterable<ModuleInstanciation> modules,
                                 List<FieldInfo> fields,
                                 ClassDef concreteClass,
                                 Set<ModuleInstanciation> visited,
                                 List<String> parentPath,
                                 Element accessSite,
                                 boolean explicitTarget,
                                 boolean requireMutable) {
        for (ModuleInstanciation module : modules) {
            if (!visited.add(module)) {
                continue;
            }
            // A nested module's fields are exposed through the outer module instance (the
            // inner instance is not a member receiver in the consuming class).
            List<String> modulePath = parentPath.isEmpty()
                ? List.of(module.getName())
                : parentPath;
            addModuleFields(module.getModuleInstanciations(), fields, concreteClass, visited, modulePath,
                accessSite, explicitTarget, requireMutable);
            addInstanceFields(module.getVars(), fields, concreteClass, modulePath, module.attrModuleOrigin(),
                accessSite, explicitTarget, requireMutable);
        }
    }

    private void addInstanceFields(Iterable<GlobalVarDef> declarations,
                                   List<FieldInfo> fields,
                                   ClassDef concreteClass,
                                   List<String> modulePath,
                                   ModuleDef declaringModule,
                                   Element accessSite,
                                   boolean explicitTarget,
                                   boolean requireMutable) {
        for (GlobalVarDef field : declarations) {
            ClassDef declaringClass = field.attrNearestClassDef();
            boolean privateFromAnotherClass = field.attrIsPrivate() && concreteClass != null
                && (explicitTarget
                    ? declaringClass == null || !accessSite.isSubtreeOf(declaringClass)
                    : declaringClass != concreteClass);
            boolean privateFromAnotherModule = field.attrIsPrivate() && declaringModule != null;
            boolean mutableEnough = !requireMutable || (!field.attrIsReadonly() && !field.attrIsConstant());
            if (!field.attrIsStatic() && mutableEnough
                && !privateFromAnotherClass && !privateFromAnotherModule) {
                fields.add(new FieldInfo(field, modulePath));
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
                                           String valueParameter, String fieldName, FieldInfo field,
                                           String targetName) {
        if (expression instanceof ExprVarAccess access) {
            if (access.getVarName().equals(nameParameter)) {
                return Ast.ExprStringVal(access.getSource(), fieldName);
            }
            if (access.getVarName().equals(valueParameter)) {
                return fieldAccess(access.getSource(), field, targetName);
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
                : fieldAccess(access.getSource(), field, targetName);
            access.replaceBy(replacement);
        }
        return expression;
    }

    private ExprMemberVarDot fieldAccess(WPos source, FieldInfo field, String targetName) {
        Expr left;
        if (targetName != null) {
            left = Ast.ExprVarAccess(source, Ast.Identifier(source, targetName));
            for (String module : field.modulePath) {
                left = Ast.ExprMemberVarDot(source, left, Ast.Identifier(source, module));
            }
        } else if (field.modulePath.isEmpty()) {
            left = Ast.ExprThis(source);
        } else {
            left = Ast.ExprVarAccess(source, Ast.Identifier(source, field.modulePath.get(0)));
            for (int i = 1; i < field.modulePath.size(); i++) {
                left = Ast.ExprMemberVarDot(source, left,
                    Ast.Identifier(source, field.modulePath.get(i)));
            }
        }
        return Ast.ExprMemberVarDot(source, left,
            Ast.Identifier(source, field.declaration.getName()));
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
