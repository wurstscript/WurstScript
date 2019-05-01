package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtojass.ImAttrType;
import de.peeeq.wurstscript.translation.imtojass.TypeRewriteMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * eliminate classes and dynamic method invocations
 */
public class EliminateGenerics {

    private final ImTranslator translator;
    private final ImProg prog;
    // TODO only use one queue here with the different cases (add: generic class type, member access)
    private Deque<GenericUse> genericsUses = new ArrayDeque<>();
    private Table<ImFunction, GenericTypes, ImFunction> specializedFunctions = HashBasedTable.create();
    private Table<ImMethod, GenericTypes, ImMethod> specializedMethods = HashBasedTable.create();
    private Table<ImClass, GenericTypes, ImClass> specializedClasses = HashBasedTable.create();
    private Multimap<ImClass, BiConsumer<GenericTypes, ImClass>> onSpecializedClassTriggers = HashMultimap.create();

    public EliminateGenerics(ImTranslator tr, ImProg prog) {
        translator = tr;
        this.prog = prog;
    }


    public void transform() {

        simplifyClasses();

        addMemberTypeArguments();

        collectGenericUsages();

        eliminateGenericUses();

        removeGenericConstructs();

    }

    private void onSpecializeClass(ImClass orig, BiConsumer<GenericTypes, ImClass> action) {
        onSpecializedClassTriggers.put(orig, action);
        specializedClasses.row(orig).forEach(action);
    }

    private void addMemberTypeArguments() {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImMethodCall mc) {
                super.visit(mc);
                handle(mc, mc.getMethod().attrClass());
            }

            @Override
            public void visit(ImMemberAccess ma) {
                super.visit(ma);
                handle(ma, (ImClass) ma.getVar().getParent().getParent());
            }

            private void handle(ImMemberOrMethodAccess ma, ImClass owningClass) {
                ImType receiverType = ma.getReceiver().attrTyp();
                if (!(receiverType instanceof ImClassType)) {
                    // using old generics
                    return;
                }
                ImClassType rt = (ImClassType) receiverType;
                ImClassType ct = adaptToSuperclass(rt, owningClass);
                if (ct == null) {
                    throw new CompileError(ma, "Could not adapt receiver " + rt + " to superclass " + owningClass + " in member access " + ma);
                }
                List<ImTypeArgument> typeArgs = ct.getTypeArguments().stream().map(ImTypeArgument::copy).collect(Collectors.toList());
                ma.getTypeArguments().addAll(0, typeArgs);
            }

        });
    }

    private static ImClassType adaptToSuperclass(ImClassType ct, ImClass owningClass) {
        if (ct.getClassDef() == owningClass) {
            return ct;
        }
        for (ImClassType sc : superTypes(ct)) {
            ImClassType r = adaptToSuperclass(sc, owningClass);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    private static Iterable<ImClassType> superTypes(ImClassType ct) {
        GenericTypes generics = new GenericTypes(ct.getTypeArguments());
        List<ImTypeVar> typeVars = ct.getClassDef().getTypeVariables();
        return () ->
                ct.getClassDef()
                        .getSuperClasses()
                        .stream()
                        .map(sc -> (ImClassType) transformType(sc, generics, typeVars))
                        .iterator();
    }


    /**
     * Removed methods and functions from classes and adds them to the
     * main program.
     */
    private void simplifyClasses() {
        for (ImClass c : prog.getClasses()) {
            simplifyClass(c);
        }
    }


    private void simplifyClass(ImClass c) {
        moveMethodsOutOfClass(c);
        moveFunctionsOutOfClass(c);

    }

    private void moveMethodsOutOfClass(ImClass c) {
        List<ImMethod> methods = c.getMethods().removeAll();
        prog.getMethods().addAll(methods);
    }

    private void moveFunctionsOutOfClass(ImClass c) {
        List<ImFunction> functions = c.getFunctions().removeAll();
        for (ImFunction f : functions) {
            prog.getFunctions().add(f);

            List<ImTypeVar> newTypeVars = c.getTypeVariables()
                    .stream()
                    .map(ImTypeVar::copy)
                    .collect(Collectors.toList());
            f.getTypeVariables().addAll(0, newTypeVars);
            List<ImTypeArgument> typeArgs = newTypeVars
                    .stream()
                    .map(ta -> JassIm.ImTypeArgument(JassIm.ImTypeVarRef(ta), Collections.emptyMap()))
                    .collect(Collectors.toList());
            rewriteGenerics(f, new GenericTypes(typeArgs), c.getTypeVariables());
        }
    }


    /**
     * When everything is specialized, we can remove generic functions and classes
     */
    private void removeGenericConstructs() {
        prog.getFunctions().removeIf(f -> !f.getTypeVariables().isEmpty());
        prog.getMethods().removeIf(m -> !m.getImplementation().getTypeVariables().isEmpty());
        prog.getClasses().removeIf(c -> !c.getTypeVariables().isEmpty());
        for (ImClass c : prog.getClasses()) {
            c.getFields().removeIf(f -> isGenericType(f.getType()));
        }
    }

    private void eliminateGenericUses() {
        while (!genericsUses.isEmpty()) {
            GenericUse gu = genericsUses.removeFirst();
            gu.eliminate();
        }
    }

    /**
     * creates a specialized version of this function
     */
    private ImFunction specializeFunction(ImFunction f, GenericTypes generics) {
        ImFunction specialized = specializedFunctions.get(f, generics);
        if (specialized != null) {
            return specialized;
        }
        if (f.getTypeVariables().isEmpty()) {
            return f;
        }
        if (generics.containsTypeVariable()) {
            throw new CompileError(f, "Generics should not contain type variables");
        }

        ImFunction newF = f.copyWithRefs();
        specializedFunctions.put(f, generics, newF);
        prog.getFunctions().add(newF);
        newF.getTypeVariables().removeAll();
        List<ImTypeVar> typeVars = f.getTypeVariables();

        newF.setName(f.getName() + "⟪" + generics.makeName() + "⟫");
        rewriteGenerics(newF, generics, typeVars);
        collectGenericUsages(newF);
        return newF;
    }

    /**
     * creates a specialized version of this method
     */
    private ImMethod specializeMethod(ImMethod m, GenericTypes generics) {

        ImMethod specialized = specializedMethods.get(m, generics);
        if (specialized != null) {
            return specialized;
        }
        if (generics.containsTypeVariable()) {
            throw new CompileError(m, "Generics should not contain type variables.");
        }

        ImMethod newM = m.copyWithRefs();
        specializedMethods.put(m, generics, newM);
        prog.getMethods().add(newM);

        ImClassType newClassType = newM.getMethodClass().copy();
        for (int i = 0; i < newClassType.getTypeArguments().size(); i++) {
            newClassType.getTypeArguments().set(i, generics.getTypeArguments().get(i).copy());
        }
        newM.setMethodClass(specializeType(newClassType));

        newM.setName(m.getName() + "⟪" + generics.makeName() + "⟫");
        newM.setImplementation(specializeFunction(newM.getImplementation(), generics));
        adaptSubmethods(m.getSubMethods(), newM);
        return newM;
    }

    private void adaptSubmethods(List<ImMethod> oldSubMethods, ImMethod newM) {
        newM.setSubMethods(new ArrayList<>());
        ImClassType newClassT = newM.getMethodClass();
        ImClass newMClass = newClassT.getClassDef();
        for (ImMethod subMethod : oldSubMethods) {
            ImClassType subClassT = subMethod.getMethodClass();
            ImClass subClass = subClassT.getClassDef();
            if (isGenericType(subClassT)) {
                onSpecializeClass(subClass, (subGenerics, specializedSubClass) -> {
                    if (specializedSubClass.isSubclassOf(newMClass)) {
                        ImMethod specializedSubMethod = specializeMethod(subMethod, subGenerics);
                        newM.getSubMethods().add(specializedSubMethod);
                    }
                });
            } else {
                subClass.getSuperClasses().replaceAll(this::specializeType);
                ImClassType newClassTspecialized = specializeType(newClassT);
                if (subClass.isSubclassOf(newClassTspecialized.getClassDef())) {
                    newM.getSubMethods().add(subMethod);
                }
            }
        }
    }

    /**
     * Replaces all uses of the given typeVars with the type arguments given in parameter generics.
     */
    private void rewriteGenerics(Element element, GenericTypes generics, List<ImTypeVar> typeVars) {
        if (generics.getTypeArguments().size() != typeVars.size()) {
            throw new RuntimeException("Rewrite generics with wrong sizes\n" +
                    "generics: " + generics + "\n" +
                    "typevars: " + typeVars + "\n" +
                    "in\n: " + element);
        }
        element.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImClass c) {
                c.getSuperClasses().replaceAll(t -> (ImClassType) transformType(t, generics, typeVars));
                super.visit(c);
            }

            @Override
            public void visit(ImTypeArgument ta) {
                ta.setType(transformType(ta.getType(), generics, typeVars));
            }

            @Override
            public void visit(ImNull e) {
                e.setType(transformType(e.getType(), generics, typeVars));
                super.visit(e);
            }

            @Override
            public void visit(ImFunction e) {
                e.setReturnType(transformType(e.getReturnType(), generics, typeVars));
                super.visit(e);
            }

            @Override
            public void visit(ImVar e) {
                e.setType(transformType(e.getType(), generics, typeVars));
                super.visit(e);
            }

            @Override
            public void visit(ImAlloc e) {
                e.setClazz((ImClassType) transformType(e.getClazz(), generics, typeVars));
                super.visit(e);
            }

            @Override
            public void visit(ImInstanceof e) {
                e.setClazz((ImClassType) transformType(e.getClazz(), generics, typeVars));
                super.visit(e);
            }

            @Override
            public void visit(ImTypeIdOfClass e) {
                e.setClazz((ImClassType) transformType(e.getClazz(), generics, typeVars));
                super.visit(e);
            }

            @Override
            public void visit(ImTypeIdOfObj e) {
                e.setClazz((ImClassType) transformType(e.getClazz(), generics, typeVars));
                super.visit(e);
            }

            @Override
            public void visit(ImDealloc e) {
                e.setClazz((ImClassType) transformType(e.getClazz(), generics, typeVars));
                super.visit(e);
            }

        });
    }

    private static ImType transformType(ImType type, GenericTypes generics, List<ImTypeVar> typeVars) {
        return ImAttrType.substituteType(type, generics.getTypeArguments(), typeVars);
    }

    /**
     * creates a specialized version of this class
     */
    private ImClass specializeClass(ImClass c, GenericTypes generics) {
        if (c.getTypeVariables().isEmpty()) {
            return c;
        }
        ImClass specialized = specializedClasses.get(c, generics);
        if (specialized != null) {
            return specialized;
        }
        if (generics.containsTypeVariable()) {
            throw new CompileError(c, "Generics should not contain type variables.");
        }
        ImClass newC = c.copyWithRefs();
        newC.setSuperClasses(new ArrayList<>(newC.getSuperClasses()));
        specializedClasses.put(c, generics, newC);
        prog.getClasses().add(newC);
        newC.getTypeVariables().removeAll();

        newC.setName(c.getName() + "⟪" + generics.makeName() + "⟫");
        List<ImTypeVar> typeVars = c.getTypeVariables();
        rewriteGenerics(newC, generics, typeVars);
        newC.getSuperClasses().replaceAll(this::specializeType);
        // we don't collect generic usages to avoid infinite loops
        // in cases like class C<T> { C<C<T>> x; }
        onSpecializedClassTriggers.get(c).forEach(consumer ->
                consumer.accept(generics, newC));
        return newC;
    }


    /**
     * Collects all usages from non-generic functions
     */
    private void collectGenericUsages() {
        collectGenericUsages(prog);
    }

    private void collectGenericUsages(Element element) {
        element.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall f) {
                super.visit(f);
                if (!f.getTypeArguments().isEmpty()) {
                    genericsUses.add(new GenericImFunctionCall(f));
                }
            }

            @Override
            public void visit(ImMethodCall mc) {
                super.visit(mc);
                if (!mc.getTypeArguments().isEmpty()) {
                    genericsUses.add(new GenericMethodCall(mc));
                }
            }

            @Override
            public void visit(ImMemberAccess ma) {
                super.visit(ma);
                if (!ma.getTypeArguments().isEmpty()) {
                    genericsUses.add(new GenericMemberAccess(ma));
                }

            }

            @Override
            public void visit(ImVar v) {
                super.visit(v);
                if (isGenericType(v.getType())) {
                    if (containsTypeVariable(v.getType())) {
                        throw new CompileError(v, "Var should not have type variables.");
                    }
                    genericsUses.add(new GenericVar(v));
                }
            }

            @Override
            public void visit(ImClass c) {
                if (!c.getTypeVariables().isEmpty()) {
                    // handle generic classes after they are specialized
                    return;
                }
                genericsUses.add(() -> {
                    List<ImClassType> newSuperClasses = c.getSuperClasses().stream().map(EliminateGenerics.this::specializeType).collect(Collectors.toList());
                    c.setSuperClasses(newSuperClasses);
                });

                super.visit(c);
            }

            @Override
            public void visit(ImFunction f) {
                if (!f.getTypeVariables().isEmpty()) {
                    // handle generic functions after they are specialized
                    return;
                }

                super.visit(f);
                if (isGenericType(f.getReturnType())) {
                    genericsUses.add(new GenericReturnTypeFunc(f));
                }
            }

            @Override
            public void visit(ImAlloc f) {
                if (isGenericType(f.getClazz())) {
                    genericsUses.add(new GenericClazzUse(f));
                }
            }

            @Override
            public void visit(ImDealloc f) {
                if (isGenericType(f.getClazz())) {
                    genericsUses.add(new GenericClazzUse(f));
                }
            }

            @Override
            public void visit(ImInstanceof f) {
                if (isGenericType(f.getClazz())) {
                    genericsUses.add(new GenericClazzUse(f));
                }
            }

            @Override
            public void visit(ImTypeIdOfObj f) {
                if (isGenericType(f.getClazz())) {
                    genericsUses.add(new GenericClazzUse(f));
                }
            }

            @Override
            public void visit(ImTypeIdOfClass f) {
                if (isGenericType(f.getClazz())) {
                    genericsUses.add(new GenericClazzUse(f));
                }
            }

        });
    }

    static boolean isGenericType(ImType type) {
        return type.match(new ImType.Matcher<Boolean>() {
            @Override
            public Boolean case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                return isGenericType(t.getEntryType());
            }

            @Override
            public Boolean case_ImArrayType(ImArrayType t) {
                return isGenericType(t.getEntryType());
            }

            @Override
            public Boolean case_ImClassType(ImClassType t) {
                return !t.getTypeArguments().isEmpty();
            }

            @Override
            public Boolean case_ImVoid(ImVoid t) {
                return false;
            }

            @Override
            public Boolean case_ImAnyType(ImAnyType imAnyType) {
                return false;
            }

            @Override
            public Boolean case_ImTupleType(ImTupleType t) {
                for (ImType tt : t.getTypes()) {
                    if (isGenericType(tt)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Boolean case_ImSimpleType(ImSimpleType t) {
                return false;
            }

            @Override
            public Boolean case_ImTypeVarRef(ImTypeVarRef t) {
                return false;
            }
        });
    }

    static boolean containsTypeVariable(ImType type) {
        return type.match(new ImType.Matcher<Boolean>() {
            @Override
            public Boolean case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                return containsTypeVariable(t.getEntryType());
            }

            @Override
            public Boolean case_ImArrayType(ImArrayType t) {
                return containsTypeVariable(t.getEntryType());
            }

            @Override
            public Boolean case_ImClassType(ImClassType t) {
                return t.getTypeArguments().stream()
                        .anyMatch(tt -> containsTypeVariable(tt.getType()));
            }

            @Override
            public Boolean case_ImVoid(ImVoid t) {
                return false;
            }

            @Override
            public Boolean case_ImAnyType(ImAnyType imAnyType) {
                return false;
            }

            @Override
            public Boolean case_ImTupleType(ImTupleType t) {
                for (ImType tt : t.getTypes()) {
                    if (containsTypeVariable(tt)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Boolean case_ImSimpleType(ImSimpleType t) {
                return false;
            }

            @Override
            public Boolean case_ImTypeVarRef(ImTypeVarRef t) {
                return true;
            }
        });
    }

    interface GenericUse {

        void eliminate();
    }

    class GenericImFunctionCall implements GenericUse {
        private final ImFunctionCall fc;

        GenericImFunctionCall(ImFunctionCall fc) {
            this.fc = fc;
        }

        @Override
        public void eliminate() {
            ImFunction f = fc.getFunc();

            GenericTypes generics = new GenericTypes(specializeTypeArgs(fc.getTypeArguments()));
            ImFunction specializedFunc = specializedFunctions.get(f, generics);
            if (specializedFunc == null) {
                specializedFunc = specializeFunction(f, generics);
            }
            fc.setFunc(specializedFunc);
            fc.getTypeArguments().removeAll();
        }
    }

    class GenericMethodCall implements GenericUse {
        private final ImMethodCall mc;

        GenericMethodCall(ImMethodCall mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            ImMethod f = mc.getMethod();

            GenericTypes generics = new GenericTypes(specializeTypeArgs(mc.getTypeArguments()));
            ImMethod specializedMethod = specializeMethod(f, generics);
            mc.setMethod(specializedMethod);
            mc.getTypeArguments().removeAll();
        }
    }

    class GenericMemberAccess implements GenericUse {
        private final ImMemberAccess ma;

        GenericMemberAccess(ImMemberAccess ma) {
            this.ma = ma;
        }

        @Override
        public void eliminate() {
            ImVar f = ma.getVar();
            ImClass owningClass = (ImClass) f.getParent().getParent();
            GenericTypes generics = new GenericTypes(specializeTypeArgs(ma.getTypeArguments()));
            ImClass specializedClass = specializeClass(owningClass, generics);
            int fieldIndex = owningClass.getFields().indexOf(f);
            ImVar newVar = specializedClass.getFields().get(fieldIndex);
            ma.setVar(newVar);
            ma.getTypeArguments().removeAll();
            newVar.setType(specializeType(newVar.getType()));
        }
    }


    class GenericVar implements GenericUse {
        private final ImVar mc;

        GenericVar(ImVar mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            mc.setType(specializeType(mc.getType()));
        }
    }

    private ImClassType specializeType(ImClassType type) {
        return (ImClassType) specializeType((ImType) type);
    }

    private ImType specializeType(ImType type) {
        return type.match(new TypeRewriteMatcher() {

            @Override
            public ImType case_ImClassType(ImClassType t) {
                ImTypeArguments typeArgs = t.getTypeArguments();
                List<ImTypeArgument> newTypeArgs = specializeTypeArgs(typeArgs);
                ImClass specializedClass = specializeClass(t.getClassDef(), new GenericTypes(newTypeArgs));
                return JassIm.ImClassType(specializedClass, JassIm.ImTypeArguments());
            }

        });
    }

    @NotNull
    private List<ImTypeArgument> specializeTypeArgs(ImTypeArguments typeArgs) {
        return typeArgs
                .stream()
                .map(ta -> JassIm.ImTypeArgument(specializeType(ta.getType()), ta.getTypeClassBinding()))
                .collect(Collectors.toList());
    }

    class GenericReturnTypeFunc implements GenericUse {
        private final ImFunction mc;

        GenericReturnTypeFunc(ImFunction mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            mc.setReturnType(specializeType(mc.getReturnType()));
        }
    }

    private class GenericClazzUse implements GenericUse {
        private final ImClassRelatedExprWithClass f;

        public GenericClazzUse(ImClassRelatedExprWithClass f) {
            super();
            this.f = f;
        }

        @Override
        public void eliminate() {
            f.setClazz(specializeType(f.getClazz()));
        }
    }

}
