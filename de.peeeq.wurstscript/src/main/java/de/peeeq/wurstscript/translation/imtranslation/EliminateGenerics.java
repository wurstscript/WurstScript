package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.*;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtojass.ImAttrType;
import de.peeeq.wurstscript.translation.imtojass.TypeRewriteMatcher;
import de.peeeq.wurstscript.translation.lua.translation.RemoveGarbage;
import org.eclipse.jdt.annotation.Nullable;
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

    // Track concrete generic arguments for specialized functions to simplify later lookups
    private final Map<ImFunction, GenericTypes> specializedFunctionGenerics = new IdentityHashMap<>();

    // NEW: Track specialized global variables for generic static fields
    // Key: (original generic global var, concrete type instantiation) -> specialized var
    private final Table<ImVar, String, ImVar> specializedGlobals = HashBasedTable.create();

    private static String gKey(GenericTypes g) {
        return g.makeName();
    }

    // NEW: Track which global vars belong to which generic class
    // This helps us know which globals need specialization
    private final Map<ImVar, ImClass> globalToClass = new HashMap<>();

    // NEW: which functions touch generic globals (identity-based)
    private final Map<ImFunction, Set<ImClass>> functionToGenericGlobalOwners = new IdentityHashMap<>();


    public EliminateGenerics(ImTranslator tr, ImProg prog) {
        translator = tr;
        this.prog = prog;
    }


    public void transform() {
        dbg(summary("start"));

        simplifyClasses();
        dbg(summary("after simplifyClasses"));

        addMemberTypeArguments();
        dbg(summary("after addMemberTypeArguments"));

        identifyGenericGlobals();
        dbg(summary("after identifyGenericGlobals"));

        collectGenericUsages();
        dbg(summary("after collectGenericUsages"));

        eliminateGenericUses();
        dbg(summary("after eliminateGenericUses"));

        dbgMethodsByName("after eliminateGenericUses");

        makeNullAssignmentsSafe();

        removeNonSpecializedGlobals();
        dbg(summary("after removeNonSpecializedGlobals"));

        removeGenericConstructs();
        dbg(summary("after removeGenericConstructs"));

        dbg(checkDanglingMethodRefs("end"));

        // TODO fix or remove this check
//        assertNoUnspecializedGenericGlobals();
    }

    private void assertNoUnspecializedGenericGlobals() {
        prog.accept(new Element.DefaultVisitor() {
            @Override public void visit(ImVarAccess va) {
                super.visit(va);
                if (globalToClass.containsKey(va.getVar())) {
                    throw new CompileError(va, "Unspecialized generic global still used: " + va.getVar().getName());
                }
            }
            @Override public void visit(ImVarArrayAccess vaa) {
                super.visit(vaa);
                if (globalToClass.containsKey(vaa.getVar())) {
                    throw new CompileError(vaa, "Unspecialized generic global array still used: " + vaa.getVar().getName());
                }
            }
        });
    }

    private void makeNullAssignmentsSafe() {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImSet s) {
                super.visit(s);

                ImExpr rhs = s.getRight();
                if (!(rhs instanceof ImNull)) return;

                // determine expected type from the LHS (already typechecked in IM)
                ImType lhsType = s.getLeft().attrTyp();
                if (lhsType == null) return;

                // after generic elimination, ensure we use the specialized concrete type
                ImType expected = specializeType(lhsType);

                ImExpr safe = specializeNullInitializer(rhs, expected);
                if (safe != rhs) {
                    s.setRight(safe);
                } else {
                    // keep IM consistent: null<T> should have the correct concrete type
                    ((ImNull) rhs).setType(expected);
                }
            }
        });
    }


    private @NotNull Set<ImClass> ownersOf(ImFunction f) {
        return functionToGenericGlobalOwners.computeIfAbsent(f, k -> Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private boolean needsGlobalSpecialization(ImFunction f) {
        Set<ImClass> o = functionToGenericGlobalOwners.get(f);
        return o != null && !o.isEmpty();
    }

    private ImFunction enclosingFunction(Element e) {
        Element cur = e;
        while (cur != null) {
            if (cur instanceof ImFunction) return (ImFunction) cur;
            cur = cur.getParent();
        }
        return null;
    }

    private void recordGenericGlobalUse(Element site, ImVar global) {
        ImClass owner = globalToClass.get(global);
        if (owner == null) return;
        ImFunction f = enclosingFunction(site);
        if (f == null) return;
        ownersOf(f).add(owner);
    }

    private void dbgMethodsByName(String phase) {
        Map<String, Integer> counts = new HashMap<>();
        for (ImMethod m : prog.getMethods()) {
            counts.merge(m.getName(), 1, Integer::sum);
        }
        dbg(phase + " methodsByName=" + counts);
    }

    private String checkDanglingMethodRefs(String phase) {
        IdentityHashMap<ImMethod, Boolean> inProg = new IdentityHashMap<>();
        for (ImMethod m : prog.getMethods()) inProg.put(m, Boolean.TRUE);

        final int[] dangling = {0};

        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImMethodCall mc) {
                super.visit(mc);

                ImMethod m = mc.getMethod();
                if (m == null) return;

                boolean methodIsGeneric = m.getImplementation() != null && !m.getImplementation().getTypeVariables().isEmpty();

                // If method is generic but call carries no type args, try to infer from receiver type and patch them in.
                if (methodIsGeneric && mc.getTypeArguments().isEmpty()) {
                    ImClass owning = m.attrClass();
                    ImType rt = mc.getReceiver().attrTyp();
                    if (owning != null && rt instanceof ImClassType) {
                        ImClassType adapted = adaptToSuperclass((ImClassType) rt, owning);
                        if (adapted != null && !adapted.getTypeArguments().isEmpty()) {
                            List<ImTypeArgument> copied = new ArrayList<>(adapted.getTypeArguments().size());
                            for (ImTypeArgument ta : adapted.getTypeArguments()) copied.add(ta.copy());
                            mc.getTypeArguments().addAll(0, copied);

                            dbg("Backfilled missing methodCall typeArgs: method=" + m.getName() + " " + id(m)
                                + " owning=" + owning.getName()
                                + " recvType=" + rt
                                + " inferredTA=" + shortTypeArgs(mc.getTypeArguments()));
                        } else {
                            dbg("MISSING methodCall typeArgs (cannot infer): method=" + m.getName() + " " + id(m)
                                + " owning=" + (owning == null ? "null" : owning.getName())
                                + " recvType=" + rt);
                        }
                    } else {
                        dbg("MISSING methodCall typeArgs (no owning/receiverClassType): method=" + m.getName() + " " + id(m)
                            + " owning=" + (owning == null ? "null" : owning.getName())
                            + " recvType=" + shortType(rt));
                    }
                }

                if (!mc.getTypeArguments().isEmpty()) {
                    dbg("COLLECT GenericMethodCall: method=" + m.getName() + " " + id(m)
                        + " impl=" + (m.getImplementation() == null ? "null" : (m.getImplementation().getName() + " " + id(m.getImplementation())))
                        + " owningClass=" + (m.attrClass() == null ? "null" : (m.attrClass().getName() + " " + id(m.attrClass())))
                        + " recvType=" + shortType(mc.getReceiver().attrTyp())
                        + " callTA=" + shortTypeArgs(mc.getTypeArguments()));
                    genericsUses.add(new GenericMethodCall(mc));
                }
            }
        });

        return phase + " danglingMethodCalls=" + dangling[0];
    }

    private String summary(String phase) {
        final int[] nGenericClasses = {0};
        final int[] nGenericMethods = {0};
        final int[] nGenericFunctions = {0};
        final int[] nMethodCallsWithTA = {0};
        final int[] nAllMethodCalls = {0};
        final int[] nAllAllocs = {0};
        final int[] nGenericAllocs = {0};
        final int[] nTypeVarRefs = {0};

        prog.accept(new Element.DefaultVisitor() {
            @Override public void visit(ImClass c) {
                if (!c.getTypeVariables().isEmpty()) nGenericClasses[0]++;
                super.visit(c);
            }
            @Override public void visit(ImMethod m) {
                if (!m.getImplementation().getTypeVariables().isEmpty()) nGenericMethods[0]++;
                super.visit(m);
            }
            @Override public void visit(ImFunction f) {
                if (!f.getTypeVariables().isEmpty()) nGenericFunctions[0]++;
                super.visit(f);
            }
            @Override public void visit(ImMethodCall mc) {
                nAllMethodCalls[0]++;
                if (!mc.getTypeArguments().isEmpty()) nMethodCallsWithTA[0]++;
                super.visit(mc);
            }
            @Override public void visit(ImAlloc a) {
                nAllAllocs[0]++;
                if (isGenericType(a.getClazz())) nGenericAllocs[0]++;
                super.visit(a);
            }
            @Override public void visit(ImTypeVarRef t) {
                nTypeVarRefs[0]++;
                super.visit(t);
            }
        });

        return phase
            + " classes=" + prog.getClasses().size()
            + " funcs=" + prog.getFunctions().size()
            + " methods=" + prog.getMethods().size()
            + " genericClasses=" + nGenericClasses[0]
            + " genericFuncs=" + nGenericFunctions[0]
            + " genericMethods=" + nGenericMethods[0]
            + " methodCalls=" + nAllMethodCalls[0]
            + " methodCallsWithTA=" + nMethodCallsWithTA[0]
            + " allocs=" + nAllAllocs[0]
            + " genericAllocs=" + nGenericAllocs[0]
            + " typeVarRefs=" + nTypeVarRefs[0];
    }


    private void removeNonSpecializedGlobals() {
        for (ImVar imVar : specializedGlobals.rowKeySet()) {
            prog.getGlobals().remove(imVar);
            List<ImSet> inits = prog.getGlobalInits().remove(imVar);
            if (inits != null) {
                for (ImSet init : inits) {
                    init.replaceBy(ImHelper.nullExpr());
                }
            }
        }
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
                if (!(receiverType instanceof ImClassType)) return;

                ImClassType rt = (ImClassType) receiverType;
                ImClassType ct = adaptToSuperclass(rt, owningClass);
                if (ct == null) {
//                    dbg("addMemberTA FAIL: owning=" + owningClass.getName() + " recv=" + rt + " in " + ma);
                    throw new CompileError(ma, "Could not adapt receiver " + rt + " to superclass " + owningClass + " in member access " + ma);
                }

//                dbg("addMemberTA: kind=" + ma.getClass().getSimpleName()
//                    + " owning=" + owningClass.getName() + " " + id(owningClass)
//                    + " recvType=" + rt
//                    + " adapted=" + ct
//                    + " beforeTA=" + shortTypeArgs(ma.getTypeArguments()));

                // existing code...
                List<ImTypeArgument> typeArgs = new ArrayList<>();
                for (ImTypeArgument imTypeArgument : ct.getTypeArguments()) {
                    typeArgs.add(imTypeArgument.copy());
                }
                ma.getTypeArguments().addAll(0, typeArgs);

//                dbg("addMemberTA: afterTA=" + shortTypeArgs(ma.getTypeArguments()));
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
        for (ImClass c : new ArrayList<>(prog.getClasses())) {
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
                typeArgs.add(JassIm.ImTypeArgument(JassIm.ImTypeVarRef(ta), Collections.emptyMap()));
            }
            rewriteGenerics(f, new GenericTypes(typeArgs), c.getTypeVariables());

            // NEW: fill implicit type args for captured generics (Inner -> Inner<T>)
            Map<String, ImTypeVar> scope = new HashMap<>();
            for (ImTypeVar tv : f.getTypeVariables()) {
                scope.put(tv.getName(), tv);
            }

            f.setReturnType(fillMissingTypeArgsFromScope(f.getReturnType(), scope));

            for (ImVar p : f.getParameters()) {
                p.setType(fillMissingTypeArgsFromScope(p.getType(), scope));
            }
            for (ImVar l : f.getLocals()) {
                l.setType(fillMissingTypeArgsFromScope(l.getType(), scope));
            }
        }
    }

    /**
     * NEW: Identify global variables that belong to generic classes
     * These are the "static" fields that need specialization
     */
    private void identifyGenericGlobals() {
        // Only include "relevant" classes: new-generic or subclass of new-generic.
        Map<String, ImClass> relevantClassMap = buildRelevantClassMap();

        for (ImVar global : prog.getGlobals()) {
            ImClass owner = resolveOwningClassFromTrace(global, relevantClassMap);
            if (owner == null) {
                continue; // not defined inside a class (package/global constant, etc.)
            }

            // This global belongs to a relevant (new-generic or inheriting) class:
            globalToClass.put(global, owner);
            WLogger.trace("Identified generic static-field global: " + global.getName()
                + " of type " + global.getType()
                + " belonging to class " + owner.getName());
        }
    }

    /**
     * Build a map of class-name -> ImClass, but only for "relevant" classes:
     * - the class is new-generic (has typeVariables)
     * - OR any of its superclasses is new-generic (transitively)
     */
    private Map<String, ImClass> buildRelevantClassMap() {
        Map<String, ImClass> m = new HashMap<>();
        IdentityHashMap<ImClass, Boolean> memo = new IdentityHashMap<>();

        for (ImClass c : prog.getClasses()) {
            if (!c.getTypeVariables().isEmpty()) {
                m.put(c.getName(), c);
            }
        }
        return m;
    }

    /**
     * Resolve owning class for a global via trace:
     * - if the global's trace source is inside a class, return the matching ImClass (if relevant)
     * - otherwise return null
     */
    private @Nullable ImClass resolveOwningClassFromTrace(ImVar global, Map<String, ImClass> relevantClassMap) {
        if (global.getTrace() == null) return null;

        // This is the only assumption you may need to adapt if your ImTrace API differs:
        de.peeeq.wurstscript.ast.Element srcObj = global.getTrace(); // expected to be a wurst AST Element
        if (srcObj == null) return null;

        @Nullable ClassDef classDef = srcObj.attrNearestClassDef();
        if (classDef == null) return null;

        // Get the class name from the AST (no global-name parsing).
        String className = classDef.getNameId().getName();

        // Only accept if it is one of the relevant classes (new-generic or inherits new-generic).
        return relevantClassMap.get(className);
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

    private void fixCalleesInSpecializedFunction(ImFunction newF, GenericTypes generics) {
        newF.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImFunctionCall fc) {
                super.visit(fc);

                ImFunction callee = fc.getFunc();
                if (callee == null) return;

                boolean calleeIsGeneric = !callee.getTypeVariables().isEmpty();
                boolean calleeNeedsGlobals = needsGlobalSpecialization(callee);

                // If it is neither generic nor touches generic globals, ignore it
                if (!calleeIsGeneric && !calleeNeedsGlobals) {
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
        if (specialized != null) return specialized;

        boolean isGeneric = !f.getTypeVariables().isEmpty();
        boolean needsGlobals = needsGlobalSpecialization(f);

        if (!isGeneric && !needsGlobals) {
            return f;
        }
        if (generics.containsTypeVariable()) {
            throw new CompileError(f, "Generics should not contain type variables");
        }

        ImFunction newF = f.copyWithRefs();
        specializedFunctions.put(f, generics, newF);
        specializedFunctionGenerics.put(newF, generics);
        prog.getFunctions().add(newF);

        // concrete clone => no type vars
        newF.getTypeVariables().removeAll();

        newF.setName(f.getName() + "⟪" + generics.makeName() + "⟫");

        // Only rewrite type variables if the function actually has them
        if (isGeneric) {
            List<ImTypeVar> typeVars = f.getTypeVariables();
            rewriteGenerics(newF, generics, typeVars);
        }

        // Fix calls inside this specialized function so they also point to specialized callees
        fixCalleesInSpecializedFunction(newF, generics);

        // Then collect further generic uses inside the now-specialized body (incl. generic globals)
        collectGenericUsages(newF);

        return newF;
    }

    /**
     * creates a specialized version of this method
     */
    private ImMethod specializeMethod(ImMethod m, GenericTypes generics) {

        dbg("specializeMethod ENTER: " + m.getName() + " " + id(m)
            + " impl=" + (m.getImplementation() == null ? "null" : (m.getImplementation().getName() + " " + id(m.getImplementation())))
            + " methodClass=" + m.getMethodClass()
            + " generics=" + generics);

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
                ImType newT = transformType(e.getType(), generics, typeVars);
                e.setType(newT);

                ImExpr safe = specializeNullInitializer(e, newT);
                if (safe != e) {
                    e.replaceBy(safe);
                    return;
                }
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
            throw new CompileError(c, "Generics should not contain type variables (" + c.getName() + " ⟪" + generics.makeName() + "⟫).");
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

    private ImExpr rewriteGenericGlobalsInExpr(ImExpr e, ImClass owningClass, GenericTypes generics) {
        e.accept(new Element.DefaultVisitor() {
            @Override public void visit(ImVarAccess va) {
                super.visit(va);
                ImVar v = va.getVar();
                ImClass owner = globalToClass.get(v);
                if (owner == null) return;

                GenericTypes g = normalizeToClassArity(generics, owner, "init-rhs");
                if (g == null || g.containsTypeVariable()) return;

                ImVar sg = ensureSpecializedGlobal(v, owner, g);
                if (sg != null) va.setVar(sg);
            }

            @Override public void visit(ImVarArrayAccess aa) {
                super.visit(aa);
                ImVar v = aa.getVar();
                ImClass owner = globalToClass.get(v);
                if (owner == null) return;

                GenericTypes g = normalizeToClassArity(generics, owner, "init-rhs");
                if (g == null || g.containsTypeVariable()) return;

                ImVar sg = ensureSpecializedGlobal(v, owner, g);
                if (sg != null) aa.setVar(sg);
            }
        });
        return e;
    }

    private void createSpecializedGlobals(ImClass originalClass, GenericTypes generics, List<ImTypeVar> typeVars) {
        String key = gKey(generics);

        // Collect "insert specialized init right after original init" operations per parent ImStmts
        // Using identity maps because IM nodes use identity semantics for parent/ownership.
        Map<ImStmts, IdentityHashMap<ImStmt, List<ImStmt>>> insertsByParent = new IdentityHashMap<>();

        for (Map.Entry<ImVar, ImClass> entry : globalToClass.entrySet()) {
            ImVar originalGlobal = entry.getKey();
            ImClass owningClass = entry.getValue();

            // be robust: sometimes class objects differ; name match is good enough here
            if (owningClass != originalClass && !owningClass.getName().equals(originalClass.getName())) continue;

            if (specializedGlobals.contains(originalGlobal, key)) continue;

            ImType specializedType = transformType(originalGlobal.getType(), generics, typeVars);

            String specializedName = originalGlobal.getName() + "⟪" + key + "⟫";
            ImVar specializedGlobal = JassIm.ImVar(
                originalGlobal.getTrace(),
                specializedType,
                specializedName,
                originalGlobal.getIsBJ()
            );

            // Create + register global
            translator.addGlobal(specializedGlobal);
            specializedGlobals.put(originalGlobal, key, specializedGlobal);
            dbg("Created specialized global: " + specializedName + " type=" + specializedType);

            // If original has init(s), create corresponding specialized init(s) and schedule insertion
            List<ImSet> originalInits = prog.getGlobalInits().get(originalGlobal);
            if (originalInits != null && !originalInits.isEmpty()) {

                ImSet firstOrig = originalInits.getFirst();
                if (!(firstOrig.getParent() instanceof ImStmts parentStmts)) {
                    throw new CompileError(originalGlobal,
                        "Initializer for global " + originalGlobal.getName() + " is not inside ImStmts.");
                }
                // ensure all original init sets share the same parent statement list
                for (ImSet s : originalInits) {
                    if (s.getParent() != parentStmts) {
                        throw new CompileError(originalGlobal,
                            "Initializer statements for global " + originalGlobal.getName() + " are not in the same ImStmts.");
                    }
                }

                // Helper: rebuild LHS as ImLExpr for specialized global
                java.util.function.Function<ImLExpr, ImLExpr> specializeLhs = (ImLExpr lhs) -> {
                    if (lhs instanceof ImVarAccess va) {
                        if (va.getVar() == originalGlobal) {
                            return JassIm.ImVarAccess(specializedGlobal);
                        }
                        return (ImLExpr) va.copy();
                    }
                    if (lhs instanceof ImVarArrayAccess aa) {
                        if (aa.getVar() == originalGlobal) {
                            return JassIm.ImVarArrayAccess(
                                aa.getTrace(),
                                specializedGlobal,
                                aa.getIndexes().copy()
                            );
                        }
                        return (ImLExpr) aa.copy();
                    }
                    throw new CompileError(originalGlobal,
                        "Unsupported initializer LHS for global " + originalGlobal.getName() + ": " + lhs.getClass().getSimpleName());
                };

                List<ImSet> specializedInitsForMap = new ArrayList<>(originalInits.size());

                // Create specialized init sets and schedule: insert each right after its corresponding original init set
                for (ImSet origSet : originalInits) {
                    ImExpr rhs = origSet.getRight().copy();
                    rhs = rewriteGenericGlobalsInExpr(rhs, originalClass, generics);
                    rhs = specializeNullInitializer(rhs, specializedType);

                    ImLExpr newLeft = specializeLhs.apply(origSet.getLeft());
                    ImSet specSet = JassIm.ImSet(originalGlobal.attrTrace(), newLeft, rhs);

                    // schedule insertion right after origSet in its parent ImStmts
                    IdentityHashMap<ImStmt, List<ImStmt>> byStmt =
                        insertsByParent.computeIfAbsent(parentStmts, k -> new IdentityHashMap<>());
                    byStmt.computeIfAbsent(origSet, k -> new ArrayList<>(1)).add(specSet);

                    // keep prog.getGlobalInits consistent, but do NOT reuse the tree-attached node elsewhere
                    specializedInitsForMap.add((ImSet) specSet.copy());
                }

                prog.getGlobalInits().put(specializedGlobal, specializedInitsForMap);
            }
        }

        // Perform insertions after the loop (so indices/state remain stable during collection)
        for (Map.Entry<ImStmts, IdentityHashMap<ImStmt, List<ImStmt>>> e : insertsByParent.entrySet()) {
            ImStmts parent = e.getKey();
            IdentityHashMap<ImStmt, List<ImStmt>> toInsertAfter = e.getValue();

            ListIterator<ImStmt> it = parent.listIterator();
            while (it.hasNext()) {
                ImStmt curr = it.next();
                List<ImStmt> ins = toInsertAfter.get(curr);
                if (ins != null) {
                    // add in order, immediately after the original init
                    for (ImStmt s : ins) {
                        it.add(s);
                    }
                }
            }
        }
    }

    private ImExpr specializeNullInitializer(ImExpr rhs, ImType specializedType) {
        if (!(rhs instanceof ImNull)) {
            return rhs;
        }

        // IMPORTANT: for concrete primitives, pjass forbids setting to null.
        if (specializedType instanceof ImSimpleType) {
            String n = ((ImSimpleType) specializedType).getTypename();
            switch (n) {
                case "integer":
                    return JassIm.ImIntVal(0);
                case "real":
                    // if your JassIm has a different overload, adjust accordingly:
                    return JassIm.ImRealVal("0.0");
                case "boolean":
                    return JassIm.ImBoolVal(false);
                default:
                    // string/handle-like types can stay null
                    ((ImNull) rhs).setType(specializedType);
                    return rhs;
            }
        }

        // For everything else, keep null but correct the type (so later passes are consistent).
        ((ImNull) rhs).setType(specializedType);
        return rhs;
    }

    /**
     * Collects all usages from non-generic functions
     */
    private void collectGenericUsages() {
        collectGenericUsages(prog);
    }

    private boolean isGlobalInitStmt(ImSet s, ImVar v) {
        List<ImSet> inits = prog.getGlobalInits().get(v);
        if (inits == null) return false;
        for (ImSet x : inits) {
            if (x == s) return true; // identity
        }
        return false;
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
                    dbg("COLLECT GenericMethodCall: method=" + mc.getMethod().getName() + " " + id(mc.getMethod())
                        + " impl=" + (mc.getMethod().getImplementation() == null ? "null" : (mc.getMethod().getImplementation().getName() + " " + id(mc.getMethod().getImplementation())))
                        + " owningClass=" + (mc.getMethod().attrClass() == null ? "null" : (mc.getMethod().attrClass().getName() + " " + id(mc.getMethod().attrClass())))
                        + " recvType=" + shortType(mc.getReceiver().attrTyp())
                        + " callTA=" + shortTypeArgs(mc.getTypeArguments()));
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
            public void visit(ImVarAccess va) {
                super.visit(va);
                if (globalToClass.containsKey(va.getVar())) {
                    recordGenericGlobalUse(va, va.getVar());
                    genericsUses.add(new GenericGlobalAccess(va));
                }
            }

            @Override
            public void visit(ImVarArrayAccess vaa) {
                super.visit(vaa);
                if (globalToClass.containsKey(vaa.getVar())) {
                    recordGenericGlobalUse(vaa, vaa.getVar());
                    genericsUses.add(new GenericGlobalArrayAccess(vaa));
                }
            }

            @Override
            public void visit(ImSet set) {
                super.visit(set);

                ImVar v = null;
                if (set.getLeft() instanceof ImVarAccess va) v = va.getVar();
                else if (set.getLeft() instanceof ImVarArrayAccess aa) v = aa.getVar();
                else return;

                if (!globalToClass.containsKey(v)) return;

                // IMPORTANT: do not treat global-init statements as “generic global accesses”
                if (isGlobalInitStmt(set, v)) {
                    return;
                }

                recordGenericGlobalUse(set, v);
                genericsUses.add(set.getLeft() instanceof ImVarAccess
                    ? new GenericGlobalAccess((ImVarAccess) set.getLeft())
                    : new GenericGlobalArrayAccess((ImVarArrayAccess) set.getLeft()));
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

            dbg("ELIM GenericMethodCall: method=" + f.getName() + " " + id(f)
                + " impl=" + (f.getImplementation() == null ? "null" : (f.getImplementation().getName() + " " + id(f.getImplementation())))
                + " owningClass=" + (f.attrClass() == null ? "null" : (f.attrClass().getName() + " " + id(f.attrClass())))
                + " callTA=" + shortTypeArgs(mc.getTypeArguments())
                + " concrete=" + generics);

            ImMethod specializedMethod = specializeMethod(f, generics);

            dbg("ELIM -> specializedMethod=" + specializedMethod.getName() + " " + id(specializedMethod)
                + " impl=" + (specializedMethod.getImplementation() == null ? "null" : (specializedMethod.getImplementation().getName() + " " + id(specializedMethod.getImplementation())))
                + " methodClass=" + specializedMethod.getMethodClass());

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
            // If the access still carries type variables, defer specialization until a concrete
            // instantiation is created (e.g. when the surrounding generic function/class is
            // specialized). If the receiver type is already concrete we can directly resolve the
            // target field using that type information.
            if (generics.containsTypeVariable()) {
                ImType receiverType = specializeType(ma.getReceiver().attrTyp());
                if (receiverType instanceof ImClassType) {
                    ImClass specializedClass = ((ImClassType) receiverType).getClassDef();
                    int fieldIndex = owningClass.getFields().indexOf(f);
                    ImVar newVar = specializedClass.getFields().get(fieldIndex);
                    ma.setVar(newVar);
                    ma.getTypeArguments().removeAll();
                    newVar.setType(specializeType(newVar.getType()));
                }
                return;
            }
            ImClass specializedClass = specializeClass(owningClass, generics);
            int fieldIndex = owningClass.getFields().indexOf(f);
            ImVar newVar = specializedClass.getFields().get(fieldIndex);
            ma.setVar(newVar);
            ma.getTypeArguments().removeAll();
            newVar.setType(specializeType(newVar.getType()));
        }
    }

    private ImVar ensureSpecializedGlobal(ImVar originalGlobal, ImClass owningClass, GenericTypes concreteGenerics) {
        concreteGenerics = normalizeToClassArity(concreteGenerics, owningClass, "ensureSpecializedGlobal:" + originalGlobal.getName());
        if (concreteGenerics == null) return null;

        String key = gKey(concreteGenerics);
        ImVar sg = specializedGlobals.get(originalGlobal, key);
        if (sg != null) return sg;

        // Ensure class specialization exists (this should also call createSpecializedGlobals)
        specializeClass(owningClass, concreteGenerics);

        sg = specializedGlobals.get(originalGlobal, key);
        if (sg != null) return sg;

        // Absolute fallback: force-create (in case specializeClass was short-circuited)
        createSpecializedGlobals(owningClass, concreteGenerics, owningClass.getTypeVariables());
        return specializedGlobals.get(originalGlobal, key);
    }

    /**
     * NEW: Handle accesses to generic global variables (static fields)
     */
    class GenericGlobalAccess implements GenericUse {
        private final ImVarAccess va;
        GenericGlobalAccess(ImVarAccess va) { this.va = va; }

        @Override public void eliminate() {
            ImVar originalGlobal = va.getVar();
            ImClass owningClass = globalToClass.get(originalGlobal);
            if (owningClass == null) return;

            GenericTypes concrete = inferGenericsFromFunction(va, owningClass);
            if (concrete == null || concrete.containsTypeVariable()) return;

            ImVar sg = ensureSpecializedGlobal(originalGlobal, owningClass, concrete);
            if (sg == null) {
                dbg("WARNING: could not specialize global " + originalGlobal.getName() + " for " + concrete);
                return;
            }
            va.setVar(sg);
        }
    }

    class GenericGlobalArrayAccess implements GenericUse {
        private final ImVarArrayAccess vaa;
        GenericGlobalArrayAccess(ImVarArrayAccess vaa) { this.vaa = vaa; }

        @Override public void eliminate() {
            ImVar originalGlobal = vaa.getVar();
            ImClass owningClass = globalToClass.get(originalGlobal);
            if (owningClass == null) return;

            GenericTypes concrete = inferGenericsFromFunction(vaa, owningClass);
            if (concrete == null || concrete.containsTypeVariable()) return;

            ImVar sg = ensureSpecializedGlobal(originalGlobal, owningClass, concrete);
            if (sg == null) {
                dbg("WARNING: could not specialize global array " + originalGlobal.getName() + " for " + concrete);
                return;
            }
            vaa.setVar(sg);
        }
    }

    private @Nullable GenericTypes normalizeToClassArity(GenericTypes g, ImClass owningClass, String why) {
        int need = owningClass.getTypeVariables().size();
        int have = g.getTypeArguments().size();

        if (have == need) return g;

        if (have < need) {
            dbg("GEN-ARITY FAIL (" + why + "): class=" + owningClass.getName()
                + " need=" + need + " have=" + have + " g=" + g);
            return null;
        }

        // have > need: take the prefix; this is the common case when the function-context has extra type args.
        List<ImTypeArgument> cut = new ArrayList<>(need);
        for (int i = 0; i < need; i++) {
            cut.add(g.getTypeArguments().get(i).copy());
        }
        GenericTypes r = new GenericTypes(cut);
        dbg("GEN-ARITY TRUNC (" + why + "): class=" + owningClass.getName()
            + " need=" + need + " have=" + have + " g=" + g + " -> " + r);
        return r;
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

                GenericTypes specialized = specializedFunctionGenerics.get(func);
                if (specialized != null) {
                    return normalizeToClassArity(specialized, owningClass, "specializedFunctionGenerics:" + func.getName());
                }

                if (!func.getTypeVariables().isEmpty()) {
                    return null;
                }

                if (!func.getParameters().isEmpty()) {
                    ImType rt = func.getParameters().get(0).getType();
                    if (rt instanceof ImClassType) {
                        ImClassType ct = (ImClassType) rt;
                        ImClass raw = ct.getClassDef();

                        boolean matches =
                            raw.getName().equals(owningClass.getName()) ||
                                raw.getName().startsWith(owningClass.getName() + "⟪") ||
                                raw.isSubclassOf(owningClass);

                        if (matches) {
                            if (!ct.getTypeArguments().isEmpty()) {
                                List<ImTypeArgument> copied = new ArrayList<>(ct.getTypeArguments().size());
                                for (ImTypeArgument ta : ct.getTypeArguments()) {
                                    copied.add(JassIm.ImTypeArgument(ta.getType().copy(), ta.getTypeClassBinding()));
                                }
                                return normalizeToClassArity(new GenericTypes(copied), owningClass, "receiverTypeArgs:" + func.getName());
                            }

                            GenericTypes fromName = extractGenericsFromClassName(raw.getName());
                            if (fromName != null && !fromName.getTypeArguments().isEmpty()) {
                                return normalizeToClassArity(fromName, owningClass, "className:" + raw.getName());
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
                GenericTypes generics = new GenericTypes(newTypeArgs);

                if (generics.containsTypeVariable()) {
                    dbg("specializeType VAR-CONTAINS: class=" + t.getClassDef().getName() + " " + id(t.getClassDef())
                        + " typeArgs=" + shortTypeArgs(t.getTypeArguments())
                        + " generics=" + generics
                        + " knownSpecializations=" + specializedClasses.row(t.getClassDef()).size());
                    Map<GenericTypes, ImClass> specialized = specializedClasses.row(t.getClassDef());

                    if (!specialized.isEmpty()) {
                        ImClass firstSpecialization = specialized.values().iterator().next();
                        return JassIm.ImClassType(firstSpecialization, JassIm.ImTypeArguments());
                    }

                    ImTypeArguments copiedArgs = JassIm.ImTypeArguments();
                    copiedArgs.addAll(newTypeArgs);
                    return JassIm.ImClassType(t.getClassDef(), copiedArgs);
                }

                ImClass specializedClass = specializeClass(t.getClassDef(), generics);
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
            ImType returnType = mc.getReturnType();

            if (containsTypeVariable(returnType) && returnType instanceof ImClassType && !mc.getParameters().isEmpty()) {
                ImClassType retClassType = (ImClassType) returnType;
                ImType receiverType = mc.getParameters().get(0).getType();

                if (receiverType instanceof ImClassType) {
                    ImClassType receiverClassType = (ImClassType) receiverType;
                    ImClassType adapted = adaptToSuperclass(receiverClassType, retClassType.getClassDef());

                    if (adapted != null) {
                        GenericTypes concrete = new GenericTypes(specializeTypeArgs(adapted.getTypeArguments()));
                        ImType specialized = ImAttrType.substituteType(returnType, concrete.getTypeArguments(), retClassType.getClassDef().getTypeVariables());

                        mc.setReturnType(specializeType(specialized));
                        return;
                    }
                }
            }

            mc.setReturnType(specializeType(returnType));
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

    private ImType fillMissingTypeArgsFromScope(ImType t, Map<String, ImTypeVar> scope) {
        return t.match(new ImType.Matcher<ImType>() {

            @Override
            public ImType case_ImClassType(ImClassType ct) {
                int need = ct.getClassDef().getTypeVariables().size();
                int have = ct.getTypeArguments().size();
                if (need == 0 || have >= need) {
                    return ct;
                }

                ImTypeArguments newArgs = JassIm.ImTypeArguments();
                // keep existing args
                for (ImTypeArgument a : ct.getTypeArguments()) {
                    newArgs.add(a.copy());
                }

                // fill missing args by name from scope
                for (int i = have; i < need; i++) {
                    ImTypeVar tv = ct.getClassDef().getTypeVariables().get(i);
                    ImTypeVar inScope = scope.get(tv.getName());
                    if (inScope == null) {
                        // no suitable type var in scope -> cannot fill
                        return ct;
                    }
                    newArgs.add(JassIm.ImTypeArgument(JassIm.ImTypeVarRef(inScope), Collections.emptyMap()));
                }

                return JassIm.ImClassType(ct.getClassDef(), newArgs);
            }

            @Override
            public ImType case_ImArrayType(ImArrayType at) {
                return JassIm.ImArrayType(fillMissingTypeArgsFromScope(at.getEntryType(), scope));
            }

            @Override
            public ImType case_ImArrayTypeMulti(ImArrayTypeMulti at) {
                return JassIm.ImArrayTypeMulti(fillMissingTypeArgsFromScope(at.getEntryType(), scope), at.getArraySize());
            }

            @Override
            public ImType case_ImTupleType(ImTupleType tt) {
                List<ImType> ts = new ArrayList<>();
                for (ImType x : tt.getTypes()) {
                    ts.add(fillMissingTypeArgsFromScope(x, scope));
                }
                return JassIm.ImTupleType(ts, tt.getNames());
            }

            @Override public ImType case_ImVoid(ImVoid v) { return v; }
            @Override public ImType case_ImAnyType(ImAnyType a) { return a; }
            @Override public ImType case_ImSimpleType(ImSimpleType s) { return s; }
            @Override public ImType case_ImTypeVarRef(ImTypeVarRef r) { return r; }
        });
    }

    private static String id(Object o) {
        return o == null ? "null" : (o.getClass().getSimpleName() + "@" + System.identityHashCode(o));
    }

    private static String shortType(ImType t) {
        return t == null ? "null" : t.toString();
    }

    private static String shortTypeArgs(ImTypeArguments tas) {
        if (tas == null) return "null";
        if (tas.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < tas.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(tas.get(i).getType());
        }
        sb.append("]");
        return sb.toString();
    }

    private void dbg(String msg) {
        WLogger.trace("[ELIMGEN] " + msg);
    }

}
