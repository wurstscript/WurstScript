package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;

import java.util.*;
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

//        recalculateTypeIds();
    }

    private void addMemberTypeArguments() {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImMethodCall mc) {
                super.visit(mc);
                ImClassType ct = (ImClassType) mc.getReceiver().attrTyp();
                List<ImTypeArgument> typeArgs = ct.getTypeArguments().stream().map(ImTypeArgument::copy).collect(Collectors.toList());
                mc.getTypeArguments().addAll(0, typeArgs);
            }

            @Override
            public void visit(ImMemberAccess ma) {
                super.visit(ma);
                ImClassType ct = (ImClassType) ma.getReceiver().attrTyp();
                List<ImTypeArgument> typeArgs = ct.getTypeArguments().stream().map(ImTypeArgument::copy).collect(Collectors.toList());
                ma.getTypeArguments().addAll(0, typeArgs);
            }
        });
    }

//    private void recalculateTypeIds() {
//        Map<ImClass, Integer> typeIds = prog.attrTypeId();
//        typeIds.clear();
//        typeIds.putAll(TypeId.calculate(prog));
//        System.out.println("new type ids: " + typeIds.size());
//        typeIds.forEach((k,v) -> {
//            System.out.println(k.getName() + ImPrinter.smallHash(k) + " -> " + v);
//        });
//    }

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
        ImFunction newF = f.copyWithRefs();
        specializedFunctions.put(f, generics, newF);
        prog.getFunctions().add(newF);
        newF.getTypeVariables().removeAll();
        List<ImTypeVar> typeVars = f.getTypeVariables();

        newF.setName(f.getName() + "_specialized_" + generics.makeName());
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
        ImMethod newM = m.copyWithRefs();
        specializedMethods.put(m, generics, newM);
        prog.getMethods().add(newM);

        ImClassType newClassType = newM.getMethodClass().copy();
        for (int i = 0; i < newClassType.getTypeArguments().size(); i++) {
            newClassType.getTypeArguments().set(i, generics.getTypeArguments().get(i).copy());
        }
        newM.setMethodClass(specializeType(newClassType));

        newM.setName(m.getName() + "_specialized_" + generics.makeName());
        newM.setImplementation(specializeFunction(newM.getImplementation(), generics));
        newM.getSubMethods().replaceAll(subMethod -> specializeMethod(subMethod, generics));

        return newM;
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
                c.getSuperClasses().replaceAll(t -> (ImClassType) transformType(t));
                super.visit(c);
            }

            @Override
            public void visit(ImTypeArgument ta) {
                ta.setType(transformType(ta.getType()));
            }

            @Override
            public void visit(ImNull e) {
                e.setType(transformType(e.getType()));
                super.visit(e);
            }

            @Override
            public void visit(ImFunction e) {
                e.setReturnType(transformType(e.getReturnType()));
                super.visit(e);
            }

            @Override
            public void visit(ImVar e) {
                e.setType(transformType(e.getType()));
                super.visit(e);
            }

            private ImType transformType(ImType type) {
                return type.match(new ImType.Matcher<ImType>() {

                    @Override
                    public ImType case_ImVoid(ImVoid t) {
                        return t;
                    }

                    @Override
                    public ImType case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                        return JassIm.ImArrayTypeMulti(transformType(t.getEntryType()), t.getArraySize());
                    }

                    @Override
                    public ImType case_ImTupleType(ImTupleType t) {
                        return JassIm.ImTupleType(t.getTypes().stream().map(tt -> transformType(tt)).collect(Collectors.toList()), t.getNames());
                    }

                    @Override
                    public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                        int index = typeVars.indexOf(t.getTypeVariable());
                        if (index < 0) {
                            throw new CompileError(t, "Could not find type var " + t + " in " + typeVars);
                        }
                        return generics.getTypeArguments().get(index).getType();
                    }

                    @Override
                    public ImType case_ImSimpleType(ImSimpleType t) {
                        return t;
                    }

                    @Override
                    public ImType case_ImArrayType(ImArrayType t) {
                        return JassIm.ImArrayType(transformType(t.getEntryType()));
                    }

                    @Override
                    public ImType case_ImClassType(ImClassType t) {
                        ImTypeArguments args = t.getTypeArguments().stream().map(ta -> JassIm.ImTypeArgument(transformType(ta.getType()), ta.getTypeClassBinding())).collect(Collectors.toCollection(JassIm::ImTypeArguments));
                        return JassIm.ImClassType(t.getClassDef(), args);
                    }
                });
            }
        });
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
        ImClass newC = c.copyWithRefs();
        specializedClasses.put(c, generics, newC);
        prog.getClasses().add(newC);
        newC.getTypeVariables().removeAll();

        newC.setName(c.getName() + "_specialized_" + generics.makeName());
        List<ImTypeVar> typeVars = c.getTypeVariables();
        rewriteGenerics(newC, generics, typeVars);
        newC.getSuperClasses().replaceAll(this::specializeType);
        collectGenericUsages(newC);
        return newC;
    }


    /**
     * Collects all usages from non-generic functions
     */
    private void collectGenericUsages() {
        collectGenericUsages(prog);
    }

    /** checks that all the type arguments only have concrete types */
    private boolean onlyConcreteTypes(ImTypeArguments ta) {
        return ta.stream()
                .noneMatch(t -> isGenericType(t.getType()));
    }

    private void collectGenericUsages(Element element) {
        element.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall f) {
                super.visit(f);
                if (!f.getTypeArguments().isEmpty()
                        && onlyConcreteTypes(f.getTypeArguments())) {
                    genericsUses.add(new GenericImFunctionCall(f));
                }
            }

            @Override
            public void visit(ImMethodCall mc) {
                super.visit(mc);
                if (!mc.getTypeArguments().isEmpty()
                        && onlyConcreteTypes(mc.getTypeArguments())) {
                    genericsUses.add(new GenericMethodCall(mc));
                }
            }

            @Override
            public void visit(ImMemberAccess ma) {
                super.visit(ma);
                if (!ma.getTypeArguments().isEmpty()
                        && onlyConcreteTypes(ma.getTypeArguments())) {
                    genericsUses.add(new GenericMemberAccess(ma));
                }

            }

            @Override
            public void visit(ImVar v) {
                super.visit(v);
                if (isGenericType(v.getType())) {
                    genericsUses.add(new GenericVar(v));
                }
            }

            @Override
            public void visit(ImClass c) {
                if (!c.getTypeVariables().isEmpty()) {
                    // handle generic classes after they are specialized
                    return;
                }
                genericsUses.add(() ->
                        c.getSuperClasses().replaceAll(EliminateGenerics.this::specializeType));

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

            GenericTypes generics = new GenericTypes(fc.getTypeArguments());
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

            List<ImTypeArgument> typeArguments = new ArrayList<>();
            GenericTypes generics = new GenericTypes(mc.getTypeArguments());
            ImMethod specializedMethod = specializedMethods.get(f, generics);
            if (specializedMethod == null) {
                specializedMethod = specializeMethod(f, generics);
            }
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
            ImClassType t = (ImClassType) specializeType(ma.getReceiver().attrTyp());
            ImVar oldVar = ma.getVar();
            ImVars parent = (ImVars) oldVar.getParent();
            int index = parent.indexOf(oldVar);
            ImVar newVar = t.getClassDef().getFields().get(index);
            if (oldVar != newVar) {
                ma.setVar(newVar);
            }
        }
    }


    class GenericClass implements GenericUse {

        private final ImClassType ct;

        public GenericClass(ImClassType ct) {
            this.ct = ct;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
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
        return type.match(new ImType.Matcher<ImType>() {
            @Override
            public ImType case_ImArrayTypeMulti(ImArrayTypeMulti t) {
                return JassIm.ImArrayTypeMulti(specializeType(t.getEntryType()), t.getArraySize());
            }

            @Override
            public ImType case_ImArrayType(ImArrayType t) {
                return JassIm.ImArrayType(specializeType(t.getEntryType()));
            }

            @Override
            public ImType case_ImClassType(ImClassType t) {
                if (t.getTypeArguments().isEmpty()) {
                    return t;
                }
                ImClass specializedClass = specializeClass(t.getClassDef(), new GenericTypes(t.getTypeArguments()));
                return JassIm.ImClassType(specializedClass, JassIm.ImTypeArguments());
            }

            @Override
            public ImType case_ImVoid(ImVoid t) {
                return t;
            }

            @Override
            public ImType case_ImTupleType(ImTupleType t) {
                return null;
            }

            @Override
            public ImType case_ImSimpleType(ImSimpleType t) {
                return t;
            }

            @Override
            public ImType case_ImTypeVarRef(ImTypeVarRef t) {
                return t;
            }
        });
    }

    class GenericReturnTypeFunc implements GenericUse {
        private final ImFunction mc;

        GenericReturnTypeFunc(ImFunction mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
        }
    }

    class GenericReturnTypeMethod implements GenericUse {
        private final ImMethod mc;

        GenericReturnTypeMethod(ImMethod mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
        }
    }

    class GenericNull implements GenericUse {
        private final ImNull mc;

        GenericNull(ImNull mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
        }
    }

    class GenericTypeArgument implements GenericUse {
        private final ImTypeArgument mc;

        GenericTypeArgument(ImTypeArgument mc) {
            this.mc = mc;
        }

        @Override
        public void eliminate() {
            throw new RuntimeException("TODO");
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