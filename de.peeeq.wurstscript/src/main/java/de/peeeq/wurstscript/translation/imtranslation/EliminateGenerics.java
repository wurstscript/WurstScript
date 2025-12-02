package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.*;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtojass.ImAttrType;
import de.peeeq.wurstscript.translation.imtojass.TypeRewriteMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * eliminate classes and dynamic method invocations
 */
public class EliminateGenerics {

    private final ImTranslator translator;
    private final ImProg prog;
    private final Deque<GenericUse> genericsUses = new ArrayDeque<>();
    private final Table<ImFunction, GenericTypes, ImFunction> specializedFunctions = HashBasedTable.create();
    private final Table<ImMethod, GenericTypes, ImMethod> specializedMethods = HashBasedTable.create();
    private final Table<ImClass, GenericTypes, ImClass> specializedClasses = HashBasedTable.create();
    private final Multimap<ImClass, BiConsumer<GenericTypes, ImClass>> onSpecializedClassTriggers = HashMultimap.create();

    // NEW: Track specialized global variables for generic static fields
    // Key: (original generic global var, concrete type instantiation) -> specialized var
    private final Table<ImVar, GenericTypes, ImVar> specializedGlobals = HashBasedTable.create();

    // NEW: Track which global vars belong to which generic class
    // This helps us know which globals need specialization
    private final Map<ImVar, ImClass> globalToClass = new HashMap<>();

    public EliminateGenerics(ImTranslator tr, ImProg prog) {
        translator = tr;
        this.prog = prog;
    }


    public void transform() {

        simplifyClasses();

        addMemberTypeArguments();

        // NEW: Identify generic globals before collecting usages
        identifyGenericGlobals();

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
                List<ImTypeArgument> typeArgs = new ArrayList<>();
                for (ImTypeArgument imTypeArgument : ct.getTypeArguments()) {
                    ImTypeArgument copy = imTypeArgument.copy();
                    typeArgs.add(copy);
                }
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

            List<ImTypeVar> newTypeVars = new ArrayList<>();
            for (ImTypeVar imTypeVar : c.getTypeVariables()) {
                ImTypeVar copy = imTypeVar.copy();
                newTypeVars.add(copy);
            }
            f.getTypeVariables().addAll(0, newTypeVars);
            List<ImTypeArgument> typeArgs = new ArrayList<>();
            for (ImTypeVar ta : newTypeVars) {
                ImTypeArgument imTypeArgument = JassIm.ImTypeArgument(JassIm.ImTypeVarRef(ta), Collections.emptyMap());
                typeArgs.add(imTypeArgument);
            }
            rewriteGenerics(f, new GenericTypes(typeArgs), c.getTypeVariables());
        }
    }

    /**
     * NEW: Identify global variables that belong to generic classes
     * These are the "static" fields that need specialization
     */
    private void identifyGenericGlobals() {
        // Build a map of class name to class for quick lookup
        Map<String, ImClass> classMap = new HashMap<>();
        for (ImClass c : prog.getClasses()) {
            classMap.put(c.getName(), c);
        }

        // Check each global variable to see if it belongs to a generic class
        for (ImVar global : prog.getGlobals()) {
            // Global variable names for static fields follow the pattern: ClassName_fieldName
            String varName = global.getName();
            int underscoreIdx = varName.indexOf('_');
            if (underscoreIdx > 0) {
                String potentialClassName = varName.substring(0, underscoreIdx);
                ImClass owningClass = classMap.get(potentialClassName);

                if (owningClass != null && !owningClass.getTypeVariables().isEmpty()) {
                    // This global belongs to a generic class
                    if (containsTypeVariable(global.getType())) {
                        globalToClass.put(global, owningClass);
                        WLogger.info("Identified generic global: " + varName + " of type " + global.getType() +
                            " belonging to class " + owningClass.getName());
                    }
                }
            }
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

        // NEW: Remove original generic global variables
        prog.getGlobals().removeIf(v -> {
            if (globalToClass.containsKey(v)) {
                WLogger.info("Removing generic global variable: " + v.getName() + " with type " + v.getType());
                return true;
            }
            return false;
        });
    }

    private void eliminateGenericUses() {
        while (!genericsUses.isEmpty()) {
            GenericUse gu = genericsUses.removeFirst();
            gu.eliminate();
        }
    }

    private void fixCalleesInSpecializedFunction(ImFunction newF, GenericTypes generics) {
        newF.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImFunctionCall fc) {
                super.visit(fc);

                ImFunction callee = fc.getFunc();
                if (callee == null) return;

                // Only interesting if callee itself is generic
                if (callee.getTypeVariables().isEmpty()) {
                    return;
                }

                // Determine which generics to use for the callee
                GenericTypes calleeGenerics;

                if (!fc.getTypeArguments().isEmpty()) {
                    // Call carries explicit type args → honor them
                    calleeGenerics = new GenericTypes(specializeTypeArgs(fc.getTypeArguments()));
                } else {
                    // No explicit type args → use the same generics context as the enclosing function.
                    // This matches the pattern: destroyArrayList<T>(this: ArrayList<T>) calls ArrayList_onDestroy<T>(this)
                    calleeGenerics = generics;
                }

                if (calleeGenerics.containsTypeVariable()) {
                    // Still not concrete → let the normal pipeline handle it later or fail explicitly if needed
                    return;
                }

                ImFunction specializedCallee = specializedFunctions.get(callee, calleeGenerics);
                if (specializedCallee == null) {
                    specializedCallee = specializeFunction(callee, calleeGenerics);
                }

                fc.setFunc(specializedCallee);
                fc.getTypeArguments().removeAll();
            }
        });
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

        // fix calls inside this specialized function so they also point to specialized callees
        fixCalleesInSpecializedFunction(newF, generics);

        // Then collect further generic uses inside the now-specialized body
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
            }else {
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

        // NEW: Create specialized global variables for this class instantiation
        createSpecializedGlobals(c, generics, typeVars);

        onSpecializedClassTriggers.get(c).forEach(consumer ->
            consumer.accept(generics, newC));
        return newC;
    }

    /**
     * NEW: Create specialized global variables for each generic static field
     */
    private void createSpecializedGlobals(ImClass originalClass, GenericTypes generics, List<ImTypeVar> typeVars) {
        // Find all global variables that belong to this class
        for (Map.Entry<ImVar, ImClass> entry : globalToClass.entrySet()) {
            ImVar originalGlobal = entry.getKey();
            ImClass owningClass = entry.getValue();

            if (owningClass != originalClass) {
                continue;
            }

            // Check if we already created this specialized version
            if (specializedGlobals.contains(originalGlobal, generics)) {
                continue;
            }

            // Transform the type using the concrete generics
            ImType specializedType = transformType(originalGlobal.getType(), generics, typeVars);

            // Create new global variable with specialized type
            String specializedName = originalGlobal.getName() + "⟪" + generics.makeName() + "⟫";
            ImVar specializedGlobal = JassIm.ImVar(
                originalGlobal.getTrace(),
                specializedType,
                specializedName,
                originalGlobal.getIsBJ()
            );

            // Add to program globals
            prog.getGlobals().add(specializedGlobal);

            // Track the specialization
            specializedGlobals.put(originalGlobal, generics, specializedGlobal);

            WLogger.info("Created specialized global: " + specializedName +
                " with type " + specializedType +
                " for generics " + generics);
        }
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

            // NEW: Collect variable accesses to generic globals
            @Override
            public void visit(ImVarAccess va) {
                super.visit(va);
                if (globalToClass.containsKey(va.getVar())) {
                    genericsUses.add(new GenericGlobalAccess(va));
                }
            }

            // NEW: Collect array accesses to generic globals
            @Override
            public void visit(ImVarArrayAccess vaa) {
                super.visit(vaa);
                if (globalToClass.containsKey(vaa.getVar())) {
                    genericsUses.add(new GenericGlobalArrayAccess(vaa));
                }
            }

            // NEW: Collect assignments to generic globals
            @Override
            public void visit(ImSet set) {
                super.visit(set);
                if (set.getLeft() instanceof ImVarAccess) {
                    ImVarAccess va = (ImVarAccess) set.getLeft();
                    if (globalToClass.containsKey(va.getVar())) {
                        genericsUses.add(new GenericGlobalAccess(va));
                    }
                } else if (set.getLeft() instanceof ImVarArrayAccess) {
                    ImVarArrayAccess vaa = (ImVarArrayAccess) set.getLeft();
                    if (globalToClass.containsKey(vaa.getVar())) {
                        genericsUses.add(new GenericGlobalArrayAccess(vaa));
                    }
                }
            }
            @Override
            public void visit(ImVar v) {
                super.visit(v);

                // Skip globals - they're handled elsewhere
                if (v.isGlobal()) return;

                // Do NOT error on type variables here. The initializer/method calls may
                // still specialize this. We'll validate at the very end.
                // If it's generic-but-concrete, schedule specialization:
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
                genericsUses.add(() -> {
                    List<ImClassType> newSuperClasses = new ArrayList<>();
                    for (ImClassType imClassType : c.getSuperClasses()) {
                        ImClassType specializeType = EliminateGenerics.this.specializeType(imClassType);
                        newSuperClasses.add(specializeType);
                    }
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
                for (ImTypeArgument tt : t.getTypeArguments()) {
                    if (containsTypeVariable(tt.getType())) {
                        return true;
                    }
                }
                return false;
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

    /**
     * NEW: Handle accesses to generic global variables (static fields)
     */
    class GenericGlobalAccess implements GenericUse {
        private final ImVarAccess va;

        GenericGlobalAccess(ImVarAccess va) {
            this.va = va;
        }

        @Override
        public void eliminate() {
            ImVar originalGlobal = va.getVar();
            ImClass owningClass = globalToClass.get(originalGlobal);

            if (owningClass == null) {
                WLogger.info("Warning: No owning class found for global " + originalGlobal.getName());
                return;
            }

            // Infer the concrete type from the enclosing function
            GenericTypes concreteGenerics = inferGenericsFromFunction(va, owningClass);

            if (concreteGenerics == null) {
                WLogger.info("Warning: Could not infer generics for global access: " + originalGlobal.getName());
                return;
            }

            // Get the specialized global variable
            ImVar specializedGlobal = specializedGlobals.get(originalGlobal, concreteGenerics);

            if (specializedGlobal == null) {
                WLogger.info("Warning: No specialized global found for " + originalGlobal.getName() +
                    " with generics " + concreteGenerics);
                return;
            }

            WLogger.info("Redirecting access from " + originalGlobal.getName() +
                " to " + specializedGlobal.getName());

            // Redirect to the specialized variable
            va.setVar(specializedGlobal);
        }
    }

    /**
     * NEW: Handle array accesses to generic global variables (static arrays)
     */
    class GenericGlobalArrayAccess implements GenericUse {
        private final ImVarArrayAccess vaa;

        GenericGlobalArrayAccess(ImVarArrayAccess vaa) {
            this.vaa = vaa;
        }

        @Override
        public void eliminate() {
            ImVar originalGlobal = vaa.getVar();
            ImClass owningClass = globalToClass.get(originalGlobal);

            if (owningClass == null) {
                WLogger.info("Warning: No owning class found for global " + originalGlobal.getName());
                return;
            }

            // Infer the concrete type from the enclosing function
            GenericTypes concreteGenerics = inferGenericsFromFunction(vaa, owningClass);

            if (concreteGenerics == null) {
                WLogger.info("Warning: Could not infer generics for global array access: " + originalGlobal.getName());
                return;
            }

            // Get the specialized global variable
            ImVar specializedGlobal = specializedGlobals.get(originalGlobal, concreteGenerics);

            if (specializedGlobal == null) {
                WLogger.info("Warning: No specialized global found for " + originalGlobal.getName() +
                    " with generics " + concreteGenerics);
                return;
            }

            WLogger.info("Redirecting array access from " + originalGlobal.getName() +
                " to " + specializedGlobal.getName());

            // Redirect to the specialized variable
            vaa.setVar(specializedGlobal);
        }
    }

    /**
     * NEW: Infer generic types from the enclosing function context
     * For specialized functions, the name contains the type information
     */
    private GenericTypes inferGenericsFromFunction(Element element, ImClass owningClass) {
        Element current = element;
        while (current != null) {
            if (current instanceof ImFunction) {
                ImFunction func = (ImFunction) current;

                // If function is still generic, we can't decide yet.
                if (!func.getTypeVariables().isEmpty()) {
                    return null;
                }

                if (!func.getParameters().isEmpty()) {
                    ImVar receiver = func.getParameters().get(0);
                    ImType rt = receiver.getType();

                    if (rt instanceof ImClassType) {
                        ImClassType ct = (ImClassType) rt;
                        ImClass raw = ct.getClassDef();

                        boolean matches =
                            raw.getName().equals(owningClass.getName()) ||
                                raw.getName().startsWith(owningClass.getName() + "⟪") ||
                                raw.isSubclassOf(owningClass);

                        if (matches) {
                            // PRIMARY: use actual type args if present
                            if (!ct.getTypeArguments().isEmpty()) {
                                List<ImTypeArgument> copied = new ArrayList<>(ct.getTypeArguments().size());
                                for (ImTypeArgument ta : ct.getTypeArguments()) {
                                    copied.add(JassIm.ImTypeArgument(ta.getType().copy(), ta.getTypeClassBinding()));
                                }
                                return new GenericTypes(copied);
                            }

                            // FALLBACK: parse from specialized class name: Box⟪...⟫
                            GenericTypes fromName = extractGenericsFromClassName(raw.getName());
                            if (fromName != null && !fromName.getTypeArguments().isEmpty()) {
                                return fromName;
                            }
                        }
                    }
                }
                return null;
            }
            current = current.getParent();
        }
        return null;
    }


    /**
     * NEW: Extract generic types from a specialized class name like "Box⟪integer⟫"
     */
    private GenericTypes extractGenericsFromClassName(String className) {
        int start = className.indexOf('⟪');
        int end   = className.lastIndexOf('⟫');
        if (start < 0 || end < 0 || end <= start + 1) return null;

        String payload = className.substring(start + 1, end).trim();
        List<String> parts = splitTopLevel(payload); // comma-split with bracket depth
        List<ImTypeArgument> args = new ArrayList<>(parts.size());
        for (String p : parts) {
            ImType t = parseTypeAtom(p.trim());
            args.add(JassIm.ImTypeArgument(t, Collections.emptyMap()));
        }
        return new GenericTypes(args);
    }

    /** split by commas at top level, respecting both ⟪⟫ and ⦅⦆ */
    private List<String> splitTopLevel(String s) {
        List<String> res = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depthAngle = 0, depthTuple = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '⟪') depthAngle++;
            else if (ch == '⟫') depthAngle--;
            else if (ch == '⦅') depthTuple++;
            else if (ch == '⦆') depthTuple--;

            if (ch == ',' && depthAngle == 0 && depthTuple == 0) {
                res.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(ch);
        }
        if (cur.length() > 0) res.add(cur.toString());
        return res;
    }

    /** parse simple atoms and tuples like ⦅integer, integer⦆ (can nest) */
    private ImType parseTypeAtom(String s) {
        s = s.trim();
        // tuple
        if (s.startsWith("⦅") && s.endsWith("⦆")) {
            String inner = s.substring(1, s.length() - 1).trim();
            List<String> elems = splitTopLevel(inner);
            List<ImType> tt = new ArrayList<>();
            List<String> names = Lists.newArrayList();
            int i = 1;
            for (String e : elems) {
                tt.add(parseTypeAtom(e));
                names.add("" + i++);
            }
            return JassIm.ImTupleType(tt, names);
        }

        // common simples
        switch (s) {
            case "integer":
            case "int":    return JassIm.ImSimpleType("integer");
            case "real":   return JassIm.ImSimpleType("real");
            case "boolean":
            case "bool":   return JassIm.ImSimpleType("boolean");
            case "string": return JassIm.ImSimpleType("string");
        }

        // class type without visible args here
        for (ImClass c : prog.getClasses()) {
            if (c.getName().equals(s)) {
                return JassIm.ImClassType(c, JassIm.ImTypeArguments());
            }
        }
        // fallback: simple type with this name
        return JassIm.ImSimpleType(s);
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
        List<ImTypeArgument> list = new ArrayList<>();
        for (ImTypeArgument ta : typeArgs) {
            ImTypeArgument imTypeArgument = JassIm.ImTypeArgument(specializeType(ta.getType()), ta.getTypeClassBinding());
            list.add(imTypeArgument);
        }
        return list;
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
