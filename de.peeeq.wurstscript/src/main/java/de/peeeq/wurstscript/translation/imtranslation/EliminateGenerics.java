package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.*;
import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtojass.ImAttrType;
import de.peeeq.wurstscript.translation.imtojass.TypeRewriteMatcher;
import de.peeeq.wurstscript.translation.lua.translation.RemoveGarbage;
import de.peeeq.wurstscript.types.TypesHelper;
import io.vavr.control.Either;
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
    private boolean genericNewOnly;
    private boolean specializeTupleValueTypes;
    private final Deque<GenericUse> genericsUses = new ArrayDeque<>();
    /**
     * Call sites already rewritten to a specialisation.
     * <p>
     * Collection has to be repeatable, because specialising one call is what makes the next one
     * concrete. It is not naturally idempotent: a member call whose type arguments were consumed
     * has them re-derived from its receiver, which would collect and specialise it again forever.
     */
    private final Set<Element> specializedCallSites = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Element> recordedErasedStaticAllocations =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> scannedFixedStaticCallees =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Table<ImFunction, GenericTypes, ImFunction> specializedFunctions = HashBasedTable.create();
    /** The class each function was moved out of, for calls which name their target without a receiver. */
    private final Map<ImFunction, ImClass> functionOwners = new IdentityHashMap<>();
    private final Table<ImMethod, GenericTypes, ImMethod> specializedMethods = HashBasedTable.create();
    private final Table<ImClass, GenericTypes, ImClass> specializedClasses = HashBasedTable.create();
    /** Concrete generic identities named by runtime instanceof checks. */
    private final Table<ImClass, GenericTypes, Boolean> runtimeTypeSpecializations = HashBasedTable.create();
    private record RuntimeTypeUse(ImClass clazz, GenericTypes generics) {
    }
    private final Multimap<ImClass, BiConsumer<GenericTypes, ImClass>> onSpecializedClassTriggers = HashMultimap.create();

    // Track concrete generic arguments for specialized functions to simplify later lookups
    private final Map<ImFunction, GenericTypes> specializedFunctionGenerics = new IdentityHashMap<>();
    private final Set<ImFunction> unspecializedGenericClassMethods =
        Collections.newSetFromMap(new IdentityHashMap<>());

    // NEW: Track specialized global variables for generic static fields
    // Key: (original generic global var, concrete type instantiation) -> specialized var
    private final Table<ImVar, GenericTypes, ImVar> specializedGlobals = HashBasedTable.create();
    /** Last specialized initializer emitted for each original initializer, preserving discovery order. */
    private final Map<ImStmt, ImStmt> specializedInitializerTails = new IdentityHashMap<>();

    // NEW: Track which global vars belong to which generic class
    // This helps us know which globals need specialization
    /** Generic statics in source/program order; specialization emission must be deterministic. */
    private final Map<ImVar, ImClass> globalToClass = new LinkedHashMap<>();

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

        eliminateRemainingGenericNewCalls();
        eliminateGenericUses();

        dbgMethodsByName("after eliminateGenericUses");

        makeNullAssignmentsSafe();

        removeNonSpecializedGlobals();
        dbg(summary("after removeNonSpecializedGlobals"));

        removeGenericConstructs();
        dbg(summary("after removeGenericConstructs"));

        assertNoGenericNewMarkers();

        dbg(checkDanglingMethodRefs("end"));

        // TODO fix or remove this check
//        assertNoUnspecializedGenericGlobals();
    }

    /**
     * Lua normally erases generics. Generic construction and scalar storage for tuple type arguments,
     * bounded dispatch, and parameterized runtime identity are the operations which need the concrete
     * type, so only specialize paths leading to those operations. All other generic calls and classes
     * keep the Lua backend's erased representation.
     */
    public void transformGenericNewOnly() {
        transformGenericNewOnly(false);
    }

    public void transformGenericNewOnly(boolean specializeTupleValueTypes) {
        genericNewOnly = true;
        this.specializeTupleValueTypes = specializeTupleValueTypes;
        identifyGenericGlobals();
        if (specializeTupleValueTypes || !globalToClass.isEmpty()) {
            addMemberTypeArguments();
        }
        indexGenericGlobalUses();
        collectUnspecializedGenericClassMethods();
        // Specialising a constructor makes its result type concrete, which is what lets a method
        // call on that result resolve. Repeat until a pass finds nothing new; collection is
        // idempotent, so this terminates once every reachable site has been rewritten.
        while (true) {
            if (specializeTupleValueTypes) {
                collectRuntimeTypeSpecializations();
            }
            collectGenericNewRoots();
            if (genericsUses.isEmpty()) {
                break;
            }
            eliminateGenericUses();
        }
        eliminateRemainingGenericNewCalls();
        assertNoReachableGenericNewMarkers();
        bindSpecialisedMethodsToTheAllocatedClass();
        settleRemainingDispatches();
    }

    public boolean hasGenericStatics() {
        identifyGenericGlobals();
        return !globalToClass.isEmpty();
    }

    /**
     * Moves a specialisation's methods to the class its objects are actually allocated from.
     * <p>
     * Specialising a method leaves the copy on the specialised class. That is where the object comes
     * from when a construction on this path was redirected there, and a virtual call finds the slot
     * through the object as usual. Otherwise the object stays erased — which is this target's normal
     * representation — and is allocated from the class the method was declared on, so the slot the
     * call names resolves to nothing and the call fails at runtime rather than at compile time.
     * <p>
     * Decided from what the program allocates rather than from the shape of the class, because one
     * class can be reached both ways: a container whose constructor was specialised is allocated from
     * the copy, while an ordinary generic object beside it is not. A specialised name carries its
     * instantiation, so two specialisations of one method stay distinct on the erased class.
     * <p>
     * When both are allocated the methods stay where they are. Giving the erased class a copy of its
     * own looks safer and is not: a copy is a dispatch group of its own, so it is named separately and
     * the binding lands under a name no call site asks for, and joining it to the method it came from
     * to share the name merges two groups which are deliberately distinct. The one shape reaching this
     * is a closure, where each class binds its own implementation under the same slot names already
     * and the erased allocation is dead. Left alone rather than fixed blind.
     */
    private void bindSpecialisedMethodsToTheAllocatedClass() {
        Map<ImClass, ImClass> erasedOf = new IdentityHashMap<>();
        for (Table.Cell<ImClass, GenericTypes, ImClass> cell : specializedClasses.cellSet()) {
            erasedOf.put(cell.getValue(), cell.getRowKey());
        }
        Set<ImClass> allocated = allocatedClasses();
        // Walked in program order rather than over the specialisation table, whose iteration is
        // hash-ordered: what ends up on a class, and in which order, decides its emitted slot names.
        for (ImClass specialized : new ArrayList<>(prog.getClasses())) {
            ImClass erased = erasedOf.get(specialized);
            if (erased == null || erased == specialized || !allocated.contains(erased)) {
                continue;
            }
            if (allocated.contains(specialized)) {
                continue;
            }
            for (ImMethod method : specialized.getMethods().removeAll()) {
                method.setMethodClass(JassIm.ImClassType(erased, JassIm.ImTypeArguments()));
                erased.getMethods().add(method);
            }
        }
    }

    /** Every class the program allocates an instance of. */
    private Set<ImClass> allocatedClasses() {
        Set<ImClass> result = Collections.newSetFromMap(new IdentityHashMap<>());
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImAlloc alloc) {
                super.visit(alloc);
                result.add(alloc.getClazz().getClassDef());
            }
        });
        return result;
    }

    /**
     * Neutralises the dispatches left in functions that were specialized.
     * <p>
     * Such a function is dead: every reachable call to it was rewritten to its specialization, so a
     * dispatch still sitting in the original can never run. This backend keeps generics rather than
     * removing them wholesale, so those originals are still translated and the dispatch would reach
     * a backend with no way to express it.
     * <p>
     * Anything else is left alone. A dispatch may legitimately remain in a bounded generic that is
     * merely declared and never called, and garbage removal deletes those later; deciding here
     * would mean duplicating reachability. One which survives that far and still reaches the
     * backend is reported there, where it is known to be both reachable and unresolvable.
     */
    private void settleRemainingDispatches() {
        List<ImTypeVarDispatch> remaining = new ArrayList<>();
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImTypeVarDispatch dispatch) {
                super.visit(dispatch);
                remaining.add(dispatch);
            }
        });
        for (ImTypeVarDispatch dispatch : remaining) {
            ImFunction owner = dispatch.getNearestFunc();
            if (owner != null && specializedFunctions.containsRow(owner)) {
                dispatch.replaceBy(defaultValueFor(dispatch.getTypeClassFunc().getReturnType()));
            }
        }
    }

    private static ImExpr defaultValueFor(ImType type) {
        return JassIm.ImNull(type.copy());
    }


    private void collectGenericNewRoots() {
        classByFunction = null;
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunction function) {
                if (!function.getTypeVariables().isEmpty()
                    || unspecializedGenericClassMethods.contains(function)) {
                    return;
                }
                super.visit(function);
            }

            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                collectGenericNewUse(call);
            }

            @Override
            public void visit(ImMethodCall call) {
                super.visit(call);
                collectGenericNewUse(call);
            }

            @Override
            public void visit(ImAlloc alloc) {
                super.visit(alloc);
                collectGenericNewUse(alloc);
            }

            @Override
            public void visit(ImMemberAccess memberAccess) {
                super.visit(memberAccess);
                collectGenericNewUse(memberAccess);
            }

            @Override
            public void visit(ImDealloc dealloc) {
                super.visit(dealloc);
                collectGenericNewUse(dealloc);
            }

            @Override
            public void visit(ImInstanceof instanceOf) {
                super.visit(instanceOf);
                collectGenericNewUse(instanceOf);
            }

            @Override
            public void visit(ImTypeIdOfObj typeId) {
                super.visit(typeId);
                collectGenericNewUse(typeId);
            }

            @Override
            public void visit(ImTypeIdOfClass typeId) {
                super.visit(typeId);
                collectGenericNewUse(typeId);
            }
        });
    }

    /**
     * Records parameterized classes whose runtime identity is observed. Lua normally erases
     * generics, but an instanceof target must denote the same concrete class as allocations of that
     * instantiation; otherwise a tuple-specialized object is also an instance of every erased
     * non-tuple instantiation.
     */
    private void collectRuntimeTypeSpecializations() {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImInstanceof instanceOf) {
                super.visit(instanceOf);
                ImClassType clazz = instanceOf.getClazz();
                if (!clazz.getTypeArguments().isEmpty()
                    && !typeArgumentsContainTypeVariable(clazz.getTypeArguments())) {
                    GenericTypes generics = new GenericTypes(clazz.getTypeArguments());
                    ImClass original = clazz.getClassDef();
                    if (runtimeTypeSpecializations.put(original, generics, true) == null) {
                        rewriteExistingRuntimeTypeSuperEdges();
                    }
                }
            }
        });
    }

    private void rewriteExistingRuntimeTypeSuperEdges() {
        for (Table.Cell<ImClass, GenericTypes, ImClass> cell
            : new ArrayList<>(specializedClasses.cellSet())) {
            if (needsRuntimeTypeSpecialization(cell.getRowKey(), cell.getColumnKey(),
                new HashSet<>())) {
                rewriteRuntimeTypeSuperEdges(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
            }
        }
    }

    /** Redirects concrete inheritance edges to the same class identity used by instanceof. */
    private void rewriteRuntimeTypeSuperEdges(ImClass original, GenericTypes generics,
                                              ImClass specialized) {
        for (ImClass clazz : prog.getClasses()) {
            clazz.getSuperClasses().replaceAll(superType -> {
                if (superType.getClassDef() != original
                    || typeArgumentsContainTypeVariable(superType.getTypeArguments())
                    || !new GenericTypes(superType.getTypeArguments()).equals(generics)) {
                    return superType;
                }
                return JassIm.ImClassType(specialized, JassIm.ImTypeArguments());
            });
        }
    }

    private boolean needsRuntimeTypeSpecialization(ImClassType clazz) {
        if (typeArgumentsContainTypeVariable(clazz.getTypeArguments())) {
            return false;
        }
        return needsRuntimeTypeSpecialization(clazz.getClassDef(),
            new GenericTypes(clazz.getTypeArguments()),
            new HashSet<>());
    }

    private boolean needsRuntimeTypeSpecialization(ImClass clazz, GenericTypes generics,
                                                   Set<RuntimeTypeUse> visited) {
        if (!visited.add(new RuntimeTypeUse(clazz, generics))) {
            return false;
        }
        if (runtimeTypeSpecializations.contains(clazz, generics)) {
            return true;
        }
        if (generics.getTypeArguments().size() != clazz.getTypeVariables().size()) {
            return false;
        }
        for (ImClassType superType : clazz.getSuperClasses()) {
            ImClassType concreteSuper = (ImClassType) transformType(superType, generics,
                clazz.getTypeVariables());
            if (!typeArgumentsContainTypeVariable(concreteSuper.getTypeArguments())
                && needsRuntimeTypeSpecialization(concreteSuper.getClassDef(),
                    new GenericTypes(concreteSuper.getTypeArguments()), visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean needsRuntimeTypeSpecialization(ImClass clazz,
                                                   ImTypeArguments typeArguments) {
        int classArgumentCount = clazz.getTypeVariables().size();
        if (classArgumentCount == 0 || typeArguments.size() < classArgumentCount) {
            return false;
        }
        List<ImTypeArgument> classArguments = new ArrayList<>(classArgumentCount);
        for (int i = 0; i < classArgumentCount; i++) {
            ImTypeArgument argument = typeArguments.get(i);
            if (containsTypeVariable(argument.getType())) {
                return false;
            }
            classArguments.add(argument);
        }
        return needsRuntimeTypeSpecialization(clazz, new GenericTypes(classArguments),
            new HashSet<>());
    }

    private boolean needsRuntimeTypeSpecialization(ImFunctionCall call) {
        ImClass owner = classOwning(call.getFunc());
        return owner != null
            && needsRuntimeTypeSpecialization(owner, call.getTypeArguments());
    }

    private void collectGenericNewUse(ImClassRelatedExprWithClass expression) {
        ImClassType clazz = expression.getClazz();
        if (clazz.getTypeArguments().isEmpty()
            || typeArgumentsContainTypeVariable(clazz.getTypeArguments())
            || (!shouldSpecializeTupleArguments(clazz.getTypeArguments())
                && !needsRuntimeTypeSpecialization(clazz))) {
            return;
        }
        genericsUses.add(new GenericClazzUse(expression));
    }

    private void collectGenericNewUses(Element element) {
        element.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                collectGenericNewUse(call);
            }

            @Override
            public void visit(ImMethodCall call) {
                super.visit(call);
                collectGenericNewUse(call);
            }

            @Override
            public void visit(ImAlloc alloc) {
                super.visit(alloc);
                collectGenericNewUse(alloc);
            }

            @Override
            public void visit(ImMemberAccess memberAccess) {
                super.visit(memberAccess);
                collectGenericNewUse(memberAccess);
            }
        });
    }

    private void collectGenericNewUse(ImFunctionCall call) {
        if (specializedCallSites.contains(call)) {
            return;
        }
        if (translator.isGenericNewMarker(call.getFunc())) {
            if (!typeArgumentsContainTypeVariable(call.getTypeArguments())) {
                genericsUses.add(new GenericNewCall(call));
            }
            return;
        }
        recordErasedConstructorAllocation(call);
        if (!call.getTypeArguments().isEmpty()
            && (shouldSpecializeTupleArguments(call.getTypeArguments())
                || needsRuntimeTypeSpecialization(call)
                || functionNeedsSpecialization(call.getFunc(), Collections.newSetFromMap(new IdentityHashMap<>())))) {
            if (!typeArgumentsContainTypeVariable(call.getTypeArguments())) {
                genericsUses.add(new GenericImFunctionCall(call));
            }
            return;
        }
        if (call.getTypeArguments().isEmpty()) {
            collectCallThroughGenericReceiver(call);
        } else if (!typeArgumentsContainTypeVariable(call.getTypeArguments())
            && !(call.getFunc().getTrace() instanceof ConstructorDef)) {
            // The generic callee remains erased, so its body is skipped by collectGenericNewRoots.
            // Fixed concrete allocations inside it still name real per-instantiation statics and
            // must be registered without cloning the caller for unrelated type arguments.
            recordFixedErasedStaticAllocations(call.getFunc());
        }
    }

    private void recordFixedErasedStaticAllocations(ImFunction function) {
        if (!scannedFixedStaticCallees.add(function)) {
            return;
        }
        function.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall nestedCall) {
                super.visit(nestedCall);
                collectGenericNewUse(nestedCall);
            }

            @Override
            public void visit(ImMethodCall nestedCall) {
                super.visit(nestedCall);
                collectGenericNewUse(nestedCall);
            }
        });
    }

    private void recordErasedConstructorAllocation(ImFunctionCall call) {
        if (call.getTypeArguments().isEmpty()
            || typeArgumentsContainTypeVariable(call.getTypeArguments())
            || !(call.getFunc().getTrace() instanceof ConstructorDef)
            || !(call.getFunc().getReturnType() instanceof ImClassType)
            || shouldSpecializeTupleArguments(call.getTypeArguments())
            || needsRuntimeTypeSpecialization(call)) {
            return;
        }
        ImClass owner = classOwning(call.getFunc());
        if (owner != null && classOwnsGenericGlobals(owner)
            && !functionNeedsSpecialization(call.getFunc(),
            Collections.newSetFromMap(new IdentityHashMap<>()))) {
            recordErasedStaticInstantiation(call, owner, call.getTypeArguments());
        }
    }

    private void recordErasedStaticInstantiation(Element site, ImClass owner,
                                                  List<ImTypeArgument> typeArguments) {
        if (!recordedErasedStaticAllocations.add(site)) {
            return;
        }
        GenericTypes generics = new GenericTypes(typeArguments);
        translator.recordErasedGenericAllocation(owner, typeArguments);
        genericsUses.add(() -> specializeClass(owner, generics));
    }

    /**
     * Collects a call which names a function of a generic class outright, taking the instantiation
     * from the receiver it was handed.
     * <p>
     * {@code super.m()} is the case that shows why: the call names its target, so there is no receiver
     * to read type arguments from, and this target never lifts the class's type variables onto the
     * function, so nothing on the call says which instantiation to specialise for and the erased
     * original is reached instead — where the dispatch it contains is dead. The receiver is still the
     * first argument, and the class it is used as gives the same answer the lift gives elsewhere. A
     * constructor's own body is reached the same way, its {@code this} being that first argument.
     * <p>
     * Only for a target which reaches one of the operations needing a concrete type. Being able to
     * read an instantiation off a receiver says nothing about whether anything wants it, and this
     * target keeps generics erased: specialising every call into a generic superclass would make a
     * copy per instantiation of functions with no dispatch and no construction in them.
     */
    private void collectCallThroughGenericReceiver(ImFunctionCall call) {
        ImClass owningClass = classOwning(call.getFunc());
        if (owningClass == null || owningClass.getTypeVariables().isEmpty()
            || !call.getFunc().getTypeVariables().isEmpty()) {
            return;
        }
        if (call.getArguments().isEmpty()
            || !(call.getArguments().get(0).attrTyp() instanceof ImClassType receiverType)) {
            return;
        }
        ImClassType classType = adaptToSuperclass(receiverType, owningClass);
        if (classType == null
            || classType.getTypeArguments().size() != owningClass.getTypeVariables().size()
            || typeArgumentsContainTypeVariable(classType.getTypeArguments())) {
            return;
        }
        if (!shouldSpecializeTupleArguments(classType.getTypeArguments())
            && !functionNeedsSpecialization(call.getFunc(),
                Collections.newSetFromMap(new IdentityHashMap<>()))) {
            return;
        }
        genericsUses.add(new GenericClassFunctionCall(call, owningClass,
            new GenericTypes(classType.getTypeArguments())));
    }

    /**
     * The class a function belongs to, whether as a method's implementation or as a function of its
     * own, and null when it belongs to none.
     * <p>
     * Rebuilt per collection pass rather than kept, because specialising adds more. This target leaves
     * both on their classes, so there is no owner map of the kind moving them out builds.
     */
    private @Nullable ImClass classOwning(ImFunction function) {
        if (classByFunction == null) {
            classByFunction = new IdentityHashMap<>();
            Map<ClassDef, ImClass> classBySource = new IdentityHashMap<>();
            for (ImClass imClass : prog.getClasses()) {
                if (imClass.getTrace() instanceof ClassDef sourceClass) {
                    classBySource.put(sourceClass, imClass);
                }
                for (ImFunction f : imClass.getFunctions()) {
                    classByFunction.putIfAbsent(f, imClass);
                }
                for (ImMethod method : imClass.getMethods()) {
                    if (method.getImplementation() != null) {
                        classByFunction.putIfAbsent(method.getImplementation(),
                            method.getMethodClass().getClassDef());
                    }
                }
            }
            for (ImFunction f : prog.getFunctions()) {
                ClassDef sourceClass = f.attrTrace() == null
                    ? null : f.attrTrace().attrNearestClassDef();
                if (sourceClass != null) {
                    ImClass owner = classBySource.get(sourceClass);
                    if (owner != null) {
                        classByFunction.putIfAbsent(f, owner);
                    }
                }
            }
        }
        return classByFunction.get(function);
    }

    private @Nullable Map<ImFunction, ImClass> classByFunction;

    /**
     * A construction states an instantiation that no call site has to mention. A closure is the case
     * that needs it: its class is built from the enclosing type variables and reached through its
     * interface, so the call carries no type arguments at all and only the allocation knows what the
     * body dispatches on. Restricted to classes that actually dispatch on a bound, so this stays a
     * targeted specialisation rather than general monomorphisation on Lua.
     */
    private void collectGenericNewUse(ImAlloc alloc) {
        ImClassType clazz = alloc.getClazz();
        if (clazz.getTypeArguments().isEmpty()
            || typeArgumentsContainTypeVariable(clazz.getTypeArguments())) {
            return;
        }
        if (!shouldSpecializeTupleArguments(clazz.getTypeArguments())
            && !needsRuntimeTypeSpecialization(clazz)
            && !isConstructionOnlyInstantiation(clazz.getClassDef())) {
            if (classOwnsGenericGlobals(clazz.getClassDef())) {
                recordErasedStaticInstantiation(alloc, clazz.getClassDef(), clazz.getTypeArguments());
            }
            return;
        }
        genericsUses.add(new GenericClazzUse(alloc));
    }

    /**
     * Whether the construction is the only place a class's instantiation is stated.
     * <p>
     * A class the user writes is used through calls that carry its type arguments, and those already
     * specialise what they need onto the erased class. A closure has no such call: it is reached
     * through the interface it implements, which is not generic, so the allocation is the only thing
     * that knows what the body dispatches on. Widening this beyond that case makes the two
     * mechanisms disagree — the object comes from the specialised class while its methods were bound
     * to the erased one.
     */
    private boolean isConstructionOnlyInstantiation(ImClass classDef) {
        return classDef.attrTrace() instanceof ExprClosure closure
            && !isInsideAnotherClosure(closure)
            && classReachesDispatch(classDef);
    }

    /**
     * A closure written inside another one is left alone.
     * <p>
     * Its captured environment is reached through a receiver belonging to the enclosing closure,
     * which by then has been specialised itself, and specialising the owner again with what is
     * left over fails inside the rewrite. Supporting that is a further step; until it is taken,
     * saying the bound could not be resolved - which is what happens without any of this - is
     * better than an error about generics of the wrong size.
     */
    private static boolean isInsideAnotherClosure(ExprClosure closure) {
        de.peeeq.wurstscript.ast.Element parent = closure.getParent();
        return parent != null && parent.attrNearestExprClosure() != null;
    }

    /**
     * Whether anything the class does ends in a dispatch on a bound, including through the
     * functions it calls. `classNeedsSpecialization` asks only whether a dispatch sits in the class
     * itself, which is the wrong question here: a closure whose body is `() -> helper(x)` has no
     * dispatch of its own, and the instantiation it needs is still only known at its construction.
     * That question is kept as it is, because widening it would change what gets specialised on
     * paths that have nothing to do with closures.
     */
    private boolean classReachesDispatch(ImClass classDef) {
        for (ImFunction f : classDef.getFunctions()) {
            if (functionNeedsSpecialization(f, Collections.newSetFromMap(new IdentityHashMap<>()),
                Collections.newSetFromMap(new IdentityHashMap<>()))) {
                return true;
            }
        }
        for (ImMethod m : classDef.getMethods()) {
            if (m.getImplementation() != null
                && functionNeedsSpecialization(m.getImplementation(),
                    Collections.newSetFromMap(new IdentityHashMap<>()),
                    Collections.newSetFromMap(new IdentityHashMap<>()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * A field of a class specialised from a construction has to be reached on the copy. The write
     * that captures a closure's environment is the case that needs it: it names the field of the
     * generic class, which nothing allocates any more once the construction was redirected.
     */
    private void collectGenericNewUse(ImMemberAccess memberAccess) {
        ImVar field = memberAccess.getVar();
        if (field.getParent() == null || !(field.getParent().getParent() instanceof ImClass owningClass)) {
            return;
        }
        // A class that has already been specialised has nothing left to select, and asking the
        // receiver to adapt to it fails outright: the receiver is still typed by the generic class
        // the specialised one was copied from, which is not a superclass of it.
        if (owningClass.getTypeVariables().isEmpty()) {
            return;
        }
        if (memberAccess.getTypeArguments().isEmpty()) {
            // The access names a field, not an instantiation; the receiver is what knows which one.
            addMemberTypeArguments(memberAccess, owningClass);
        }
        if (memberAccess.getTypeArguments().isEmpty()
            || typeArgumentsContainTypeVariable(memberAccess.getTypeArguments())) {
            return;
        }
        if (!shouldSpecializeTupleArguments(memberAccess.getTypeArguments())
            && !isConstructionOnlyInstantiation(owningClass)) {
            return;
        }
        genericsUses.add(new GenericMemberAccess(memberAccess));
    }

    private void collectGenericNewUse(ImMethodCall call) {
        if (specializedCallSites.contains(call)) {
            return;
        }
        ImMethod method = call.getMethod();
        ImTranslator.Specialisation existing = translator.specialisationOf(method);
        if (existing != null && existing.original() instanceof ImMethod) {
            // The owning class created this concrete method. Its type arguments were consumed by
            // that structural class specialisation; collecting it again invents a second generic
            // boundary with no type variables and loses the phase invariant.
            call.getTypeArguments().removeAll();
            specializedCallSites.add(call);
            return;
        }
        if (isMissingClassTypeArguments(call, method)) {
            addMemberTypeArguments(call, method.attrClass());
        }
        if (typeArgumentsContainTypeVariable(call.getTypeArguments())) {
            // A call directly on a fresh generic construction gets its concrete class arguments
            // from that construction before deciding between specialization and fixed-body scan.
            useConstructionTypeArguments(call);
        }
        boolean needsSpecialization = methodNeedsSpecialization(method,
            Collections.newSetFromMap(new IdentityHashMap<>()),
            Collections.newSetFromMap(new IdentityHashMap<>()));
        if (!shouldSpecializeTupleArguments(call.getTypeArguments()) && !needsSpecialization) {
            if (!call.getTypeArguments().isEmpty()
                && !typeArgumentsContainTypeVariable(call.getTypeArguments())
                && method.getImplementation() != null) {
                recordFixedErasedStaticAllocations(method.getImplementation());
            }
            return;
        }
        if (!call.getTypeArguments().isEmpty()
            && !typeArgumentsContainTypeVariable(call.getTypeArguments())) {
            genericsUses.add(new GenericMethodCall(call));
        }
    }

    /**
     * Whether a call is short of the type arguments belonging to the receiver's class.
     * <p>
     * A specialisation is matched against the class's type variables followed by the method's own, so
     * a method declaring parameters of its own leaves the call supplying the shorter list rather than
     * an empty one. Reading a non-empty list as "already has them" is what rejected a bounded type
     * parameter on a method of a generic class here, while the same program compiles on Jass, where
     * lifting the class's variables onto the method gives the call both at once.
     */
    private static boolean isMissingClassTypeArguments(ImMethodCall call, ImMethod method) {
        ImFunction implementation = method.getImplementation();
        int own = implementation == null ? 0 : implementation.getTypeVariables().size();
        return call.getTypeArguments().size() == own
            && !method.getMethodClass().getClassDef().getTypeVariables().isEmpty();
    }

    /**
     * Replaces a member call's still-generic type arguments with those of the constructor call that
     * produced its receiver, as in {@code new Box<int>().render(x)}.
     */
    private void useConstructionTypeArguments(ImMethodCall call) {
        if (!(call.getReceiver() instanceof ImFunctionCall construction)
            || construction.getTypeArguments().isEmpty()
            || typeArgumentsContainTypeVariable(construction.getTypeArguments())) {
            return;
        }
        List<ImTypeArgument> fromConstruction = new ArrayList<>();
        for (ImTypeArgument ta : construction.getTypeArguments()) {
            fromConstruction.add(ta.copy());
        }
        call.getTypeArguments().removeAll();
        call.getTypeArguments().addAll(fromConstruction);
    }

    private boolean typeArgumentsContainTypeVariable(ImTypeArguments typeArguments) {
        for (ImTypeArgument typeArgument : typeArguments) {
            if (containsTypeVariable(typeArgument.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean typeArgumentsContainTuple(Iterable<ImTypeArgument> typeArguments) {
        for (ImTypeArgument typeArgument : typeArguments) {
            if (TypesHelper.typeContainsTuples(typeArgument.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldSpecializeTupleArguments(ImTypeArguments typeArguments) {
        return specializeTupleValueTypes && typeArgumentsContainTuple(typeArguments);
    }

    private boolean genericTypesContainTuple(GenericTypes generics) {
        return typeArgumentsContainTuple(generics.getTypeArguments());
    }

    private boolean functionNeedsSpecialization(ImFunction function, Set<ImFunction> visited) {
        return functionNeedsSpecialization(function, visited,
            Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    /**
     * Whether a function must be specialised even on Lua, which otherwise keeps generics erased.
     * <p>
     * Concrete type arguments are needed when constructing a value of them, dispatching on a type
     * class bound, or constructing a generic class whose static storage is per instantiation.
     * Specialising these paths keeps a bounded generic as cheap on Lua as it is on Jass, at the cost
     * of one copy per instantiation actually used.
     */
    private boolean functionNeedsSpecialization(ImFunction function, Set<ImFunction> visitedFunctions,
                                               Set<ImMethod> visitedMethods) {
        if (needsGlobalSpecialization(function)) {
            return true;
        }
        if (!visitedFunctions.add(function)) {
            return false;
        }
        boolean[] found = {false};
        function.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImTypeVarDispatch dispatch) {
                found[0] = true;
            }

            @Override
            public void visit(ImInstanceof instanceOf) {
                if (typeArgumentsContainTypeVariable(instanceOf.getClazz().getTypeArguments())) {
                    found[0] = true;
                    return;
                }
                super.visit(instanceOf);
            }

            @Override
            public void visit(ImAlloc alloc) {
                // Constructing a class whose methods dispatch has to be specialised as well:
                // otherwise the constructor keeps a generic result type, and a method call on that
                // result never becomes concrete enough to resolve.
                if (classNeedsSpecialization(alloc.getClazz().getClassDef())) {
                    found[0] = true;
                    return;
                }
                super.visit(alloc);
            }

            @Override
            public void visit(ImFunctionCall call) {
                // Empty arguments may be supplied implicitly by the enclosing generic receiver.
                // Only an explicit, already-concrete call is independent of the caller context.
                boolean dependsOnCaller = call.getTypeArguments().isEmpty()
                    || typeArgumentsContainTypeVariable(call.getTypeArguments());
                if (constructsClassOwningGenericGlobals(function, call)
                    || translator.isGenericNewMarker(call.getFunc())
                    || (dependsOnCaller
                    && functionNeedsSpecialization(call.getFunc(), visitedFunctions, visitedMethods))) {
                    found[0] = true;
                    return;
                }
                super.visit(call);
            }

            @Override
            public void visit(ImMethodCall call) {
                boolean dependsOnCaller = call.getTypeArguments().isEmpty()
                    || typeArgumentsContainTypeVariable(call.getTypeArguments());
                if (dependsOnCaller
                    && methodNeedsSpecialization(call.getMethod(), visitedFunctions, visitedMethods)) {
                    found[0] = true;
                    return;
                }
                super.visit(call);
            }
        });
        return found[0];
    }

    /**
     * A generic caller containing {@code new Box<T>()} must be revisited after {@code T} becomes
     * concrete so each constructed instantiation can register its own static storage. Detect the
     * constructor call at the caller boundary; marking the constructor implementation itself would
     * unnecessarily redirect ordinary objects away from Lua's erased representation.
     */
    private boolean constructsClassOwningGenericGlobals(ImFunction enclosingFunction,
                                                         ImFunctionCall call) {
        if (!(call.getFunc().getTrace() instanceof ConstructorDef)
            || !typeArgumentsContainTypeVariable(call.getTypeArguments())) {
            return false;
        }
        // A lowered constructor wrapper calls the class initializer carrying the same source
        // ConstructorDef. That call implements the current allocation; it is not another generic
        // allocation hidden inside this function and direct callers register it themselves.
        if (enclosingFunction.getTrace() == call.getFunc().getTrace()) {
            return false;
        }
        ImClass owner = classOwning(call.getFunc());
        return owner != null && classOwnsGenericGlobals(owner);
    }

    private boolean methodNeedsSpecialization(ImMethod method, Set<ImFunction> visitedFunctions,
                                             Set<ImMethod> visitedMethods) {
        if (!visitedMethods.add(method)) {
            return false;
        }
        if (method.getImplementation() != null
            && functionNeedsSpecialization(method.getImplementation(), visitedFunctions, visitedMethods)) {
            return true;
        }
        for (ImMethod subMethod : method.getSubMethods()) {
            if (methodNeedsSpecialization(subMethod, visitedFunctions, visitedMethods)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether constructing this class requires the concrete type argument, because one of its own
     * or inherited members dispatches on a type class bound.
     * <p>
     * Deliberately a property of the class alone, not of the path that asked. An earlier version
     * threaded the caller's visited set through here and memoised the answer, so a query made while
     * one of the class's own functions was already being visited recorded a negative result that
     * then stood for every later query.
     */
    private boolean classNeedsSpecialization(ImClass classDef) {
        Boolean cached = classNeedsSpecializationCache.get(classDef);
        if (cached != null) {
            return cached;
        }
        classNeedsSpecializationCache.put(classDef, false);
        boolean result = classDispatchesOnBound(classDef,
            Collections.newSetFromMap(new IdentityHashMap<>()));
        classNeedsSpecializationCache.put(classDef, result);
        return result;
    }

    private boolean classDispatchesOnBound(ImClass classDef, Set<ImClass> visited) {
        if (!visited.add(classDef)) {
            return false;
        }
        for (ImFunction f : classDef.getFunctions()) {
            if (containsDispatch(f)) {
                return true;
            }
        }
        for (ImMethod m : classDef.getMethods()) {
            if (m.getImplementation() != null && containsDispatch(m.getImplementation())) {
                return true;
            }
        }
        for (ImClassType superType : classDef.getSuperClasses()) {
            if (classDispatchesOnBound(superType.getClassDef(), visited)) {
                return true;
            }
        }
        return false;
    }

    /** Whether this function body dispatches on a bound, without following calls out of it. */
    private static boolean containsDispatch(ImFunction f) {
        boolean[] found = {false};
        f.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImTypeVarDispatch dispatch) {
                found[0] = true;
            }
        });
        return found[0];
    }

    private final Map<ImClass, Boolean> classNeedsSpecializationCache = new IdentityHashMap<>();

    private void assertNoReachableGenericNewMarkers() {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunction function) {
                if (!function.getTypeVariables().isEmpty()
                    || unspecializedGenericClassMethods.contains(function)) {
                    return;
                }
                super.visit(function);
            }

            @Override
            public void visit(ImFunctionCall call) {
                if (translator.isGenericNewMarker(call.getFunc())) {
                    throw new CompileError(call, CompilerIntrinsics.NEW
                        + " requires its type argument to resolve to a concrete class.");
                }
                super.visit(call);
            }
        });
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

    private void assertNoGenericNewMarkers() {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                if (translator.isGenericNewMarker(call.getFunc())) {
                    ImFunction owner = enclosingFunction(call);
                    throw new CompileError(call, "Internal error: " + CompilerIntrinsics.NEW
                        + " was not lowered in " + (owner == null ? "<unknown>" : owner.getName())
                        + ".");
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

    private boolean classOwnsGenericGlobals(ImClass clazz) {
        return classOwnsGenericGlobals(clazz,
            Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private boolean classOwnsGenericGlobals(ImClass clazz, Set<ImClass> visited) {
        if (!visited.add(clazz)) {
            return false;
        }
        ImClass canonical = translator.canonical(clazz);
        for (ImClass owner : globalToClass.values()) {
            if (translator.canonical(owner) == canonical) {
                return true;
            }
        }
        for (ImClassType superClass : clazz.getSuperClasses()) {
            if (classOwnsGenericGlobals(superClass.getClassDef(), visited)) {
                return true;
            }
        }
        return false;
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

    private void indexGenericGlobalUses() {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess access) {
                recordGenericGlobalUse(access, access.getVar());
                super.visit(access);
            }

            @Override
            public void visit(ImVarArrayAccess access) {
                recordGenericGlobalUse(access, access.getVar());
                super.visit(access);
            }
        });
    }

    private void dbgMethodsByName(String phase) {
        Map<String, Integer> counts = new HashMap<>();
        for (ImMethod m : prog.getMethods()) {
            counts.merge(m.getName(), 1, Integer::sum);
        }
        dbg(phase + " methodsByName=" + counts);
    }

    private String checkDanglingMethodRefs(String phase) {
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
                    if (init.getParent() != null) {
                        init.replaceBy(ImHelper.nullExpr());
                    }
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
                addMemberTypeArguments(mc, mc.getMethod().attrClass());
            }

            @Override
            public void visit(ImMemberAccess ma) {
                super.visit(ma);
                addMemberTypeArguments(ma, (ImClass) ma.getVar().getParent().getParent());
            }

            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                addReceiverTypeArguments(call);
            }
        });
    }

    /**
     * Gives a call which reaches a class function directly the type arguments of the class.
     * <p>
     * Moving a function out of its class lifts the class's type variables onto the function, and a
     * call through a receiver gets them back from the receiver's type. A call which names its target
     * outright has no receiver to read - {@code super.m()} and {@code super()} are both of this kind -
     * so it is left asking for a function with type variables while supplying none, and nothing
     * specialises it. The receiver is still there as the first argument, so the class it is used as
     * gives the same type arguments the receiver would have.
     */
    private void addReceiverTypeArguments(ImFunctionCall call) {
        ImClass owningClass = functionOwners.get(call.getFunc());
        if (owningClass == null || call.getArguments().isEmpty()) {
            return;
        }
        // The class's variables are lifted onto the front of the function's own, so what a call is
        // short of is that prefix. A method with type parameters of its own already supplies theirs,
        // which is a shorter list rather than an empty one.
        int missing = call.getFunc().getTypeVariables().size() - call.getTypeArguments().size();
        if (missing != owningClass.getTypeVariables().size()) {
            return;
        }
        if (!(call.getArguments().get(0).attrTyp() instanceof ImClassType receiverType)) {
            return;
        }
        ImClassType classType = adaptToSuperclass(receiverType, owningClass);
        if (classType == null || classType.getTypeArguments().size() != missing) {
            return;
        }
        List<ImTypeArgument> typeArgs = new ArrayList<>();
        for (ImTypeArgument typeArgument : classType.getTypeArguments()) {
            typeArgs.add(typeArgument.copy());
        }
        call.getTypeArguments().addAll(0, typeArgs);
    }

    private void addMemberTypeArguments(ImMemberOrMethodAccess access, ImClass owningClass) {
        ImType receiverType = access.getReceiver().attrTyp();
        if (!(receiverType instanceof ImClassType rt)) {
            return;
        }
        ImClassType classType = adaptToSuperclass(rt, owningClass);
        if (classType == null) {
            throw new CompileError(access, "Could not adapt receiver " + rt + " to superclass "
                + owningClass + " in member access " + access);
        }
        List<ImTypeArgument> typeArgs = new ArrayList<>();
        for (ImTypeArgument typeArgument : classType.getTypeArguments()) {
            typeArgs.add(typeArgument.copy());
        }
        access.getTypeArguments().addAll(0, typeArgs);
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
            functionOwners.put(f, c);

            List<ImTypeVar> newTypeVars = new ArrayList<>();
            for (ImTypeVar imTypeVar : c.getTypeVariables()) {
                ImTypeVar copy = imTypeVar.copy();
                // One source parameter becomes several nodes here. Recorded so the two can be
                // recognised as the same parameter without falling back to comparing names.
                translator.recordSpecialisation(copy, imTypeVar);
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
        Map<ClassDef, ImClass> genericClassesBySource = new IdentityHashMap<>();
        for (ImClass imClass : prog.getClasses()) {
            if (!imClass.getTypeVariables().isEmpty() && imClass.getTrace() instanceof ClassDef sourceClass) {
                genericClassesBySource.put(sourceClass, imClass);
            }
        }

        for (ImVar global : prog.getGlobals()) {
            ImClass owner = resolveOwningClassFromTrace(global, genericClassesBySource);
            if (owner == null) {
                continue; // not defined inside a class (package/global constant, etc.)
            }

            // This global belongs to a relevant (new-generic or inheriting) class:
            globalToClass.put(global, owner);
            WLogger.trace(() -> "Identified generic static-field global: " + global.getName()
                + " of type " + global.getType()
                + " belonging to class " + owner.getName());
        }
    }

    /** Resolve a generic static's owner through its source class identity. */
    private @Nullable ImClass resolveOwningClassFromTrace(
            ImVar global, Map<ClassDef, ImClass> genericClassesBySource) {
        if (global.getTrace() == null) return null;
        @Nullable ClassDef classDef = global.getTrace().attrNearestClassDef();
        if (classDef == null) return null;
        return genericClassesBySource.get(classDef);
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

    private void eliminateRemainingGenericNewCalls() {
        List<ImFunctionCall> calls = new ArrayList<>();
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunction function) {
                if (!function.getTypeVariables().isEmpty()
                    || unspecializedGenericClassMethods.contains(function)) {
                    return;
                }
                super.visit(function);
            }

            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                if (translator.isGenericNewMarker(call.getFunc())) {
                    calls.add(call);
                }
            }
        });
        for (ImFunctionCall call : calls) {
            new GenericNewCall(call).eliminate();
        }
    }

    private void collectUnspecializedGenericClassMethods() {
        for (ImMethod method : prog.getMethods()) {
            collectUnspecializedGenericClassMethod(method);
        }
        for (ImClass imClass : prog.getClasses()) {
            if (!imClass.getTypeVariables().isEmpty()) {
                for (ImMethod method : imClass.getMethods()) {
                    collectUnspecializedGenericClassMethod(method);
                }
            }
        }
    }

    private void collectUnspecializedGenericClassMethod(ImMethod method) {
        if (method.getImplementation() != null
            && !method.getMethodClass().getClassDef().getTypeVariables().isEmpty()) {
            unspecializedGenericClassMethods.add(method.getImplementation());
        }
    }

    private void fixCalleesInSpecializedFunction(ImFunction newF, GenericTypes generics) {
        newF.accept(new Element.DefaultVisitor() {

            @Override
            public void visit(ImFunctionCall fc) {
                super.visit(fc);

                ImFunction callee = fc.getFunc();
                if (callee == null) return;
                if (translator.isGenericNewMarker(callee)) return;

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
            // A function of a generic class declares no type variables of its own: it uses the
            // class's, which this target does not lift onto it. The call already says which
            // instantiation it is for, so match against the class's variables rather than treating
            // the function as nothing to specialise, strip the arguments and leave the dispatch
            // inside it with no concrete type. A constructor is the case that needs this - the call
            // running it is the only place its instantiation is stated, there being no receiver yet.
            ImClass owner = genericNewOnly ? classOwning(f) : null;
            if (owner != null && !owner.getTypeVariables().isEmpty()
                && owner.getTypeVariables().size() == generics.getTypeArguments().size()) {
                return specializeClassFunction(f, owner, f, generics);
            }
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
        translator.recordSpecialisation(newF, f, generics.getTypeArguments());
        recordCopiedTypeVars(f.getTypeVariables(), newF.getTypeVariables());
        newF.getTypeVariables().removeAll();

        newF.setName(genericNewOnly
            ? f.getName() + "_specialized"
            : f.getName() + "⟪" + generics.makeName() + "⟫");

        // Only rewrite type variables if the function actually has them
        if (isGeneric) {
            List<ImTypeVar> typeVars = f.getTypeVariables();
            rewriteGenerics(newF, generics, typeVars);
        }

        if (genericNewOnly && (needsGlobalSpecialization(f)
            || (specializeTupleValueTypes && genericTypesContainTuple(generics)))) {
            ImClass owner = classOwning(f);
            if (owner != null && !owner.getTypeVariables().isEmpty()) {
                GenericTypes ownerGenerics = generics.take(owner.getTypeVariables().size());
                specializeClass(owner, ownerGenerics);
                rewriteOwnedGenericGlobals(newF, owner, ownerGenerics);
            }
        }

        // Fix calls inside this specialized function so they also point to specialized callees
        if (genericNewOnly) {
            collectGenericNewUses(newF);
        } else {
            fixCalleesInSpecializedFunction(newF, generics);

            // Then collect further generic uses inside the now-specialized body (incl. generic globals)
            collectGenericUsages(newF);
        }

        return newF;
    }

    private void rewriteOwnedGenericGlobals(Element copy, ImClass owner, GenericTypes ownerGenerics) {
        copy.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImVarAccess access) {
                super.visit(access);
                access.setVar(specializedGlobal(access.getVar()));
            }

            @Override
            public void visit(ImVarArrayAccess access) {
                super.visit(access);
                access.setVar(specializedGlobal(access.getVar()));
            }

            private ImVar specializedGlobal(ImVar original) {
                ImClass globalOwner = globalToClass.get(original);
                if (globalOwner == null) {
                    return original;
                }
                GenericTypes globalGenerics = adaptGenericsToOwner(owner, ownerGenerics, globalOwner);
                if (globalGenerics == null) {
                    return original;
                }
                ImVar result = ensureSpecializedGlobal(original, globalOwner, globalGenerics);
                return result == null ? original : result;
            }
        });
    }

    /** Maps a concrete subclass instantiation onto the type arguments of a static's declaring class. */
    private @Nullable GenericTypes adaptGenericsToOwner(ImClass concreteOwner,
                                                        GenericTypes concreteGenerics,
                                                        ImClass declaringOwner) {
        if (translator.canonical(concreteOwner) == translator.canonical(declaringOwner)) {
            return concreteGenerics;
        }
        ImTypeArguments arguments = JassIm.ImTypeArguments();
        for (ImTypeArgument argument : concreteGenerics.getTypeArguments()) {
            arguments.add(argument.copy());
        }
        ImClassType adapted = adaptToSuperclass(
            JassIm.ImClassType(concreteOwner, arguments), declaringOwner);
        if (adapted == null || adapted.getTypeArguments().size() != declaringOwner.getTypeVariables().size()
            || typeArgumentsContainTypeVariable(adapted.getTypeArguments())) {
            return null;
        }
        return new GenericTypes(adapted.getTypeArguments());
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
        if (!genericNewOnly) {
            prog.getMethods().add(newM);
        }

        ImClassType newClassType = newM.getMethodClass().copy();
        for (int i = 0; i < newClassType.getTypeArguments().size(); i++) {
            newClassType.getTypeArguments().set(i, generics.getTypeArguments().get(i).copy());
        }
        newM.setMethodClass(specializeType(newClassType));
        if (genericNewOnly) {
            newM.getMethodClass().getClassDef().getMethods().add(newM);
        }

        newM.setName(genericNewOnly
            ? m.getName() + "_specialized_" + generics.makeName()
            : m.getName() + "⟪" + generics.makeName() + "⟫");
        newM.setImplementation(genericNewOnly
            ? specializeMethodImplementation(m, generics)
            : specializeFunction(newM.getImplementation(), generics));
        adaptSubmethods(m.getSubMethods(), newM, generics);
        return newM;
    }

    private ImFunction specializeMethodImplementation(ImMethod method, GenericTypes generics) {
        return specializeClassFunction(method.getImplementation(),
            method.getMethodClass().getClassDef(), method, generics);
    }

    /**
     * Specialises a function belonging to a generic class, whether it implements a method or is a
     * function of the class in its own right - a constructor and the body it runs are the latter.
     * <p>
     * This target does not lift a class's type variables onto its functions, so such a function uses
     * them where they are declared and the arguments to match are the class's followed by any the
     * function declares itself.
     */
    private ImFunction specializeClassFunction(ImFunction function, ImClass owningClass,
                                               Element blameFor, GenericTypes generics) {
        ImFunction specialized = specializedFunctions.get(function, generics);
        if (specialized != null) {
            return specialized;
        }

        List<ImTypeVar> typeVariables = new ArrayList<>(owningClass.getTypeVariables());
        typeVariables.addAll(function.getTypeVariables());
        if (typeVariables.size() != generics.getTypeArguments().size()) {
            throw new CompileError(blameFor, "Generics should match class method type variables for "
                + function.getName() + ": expected " + typeVariables.size() + " but found "
                + generics.getTypeArguments().size() + ".");
        }

        ImFunction newImplementation = function.copyWithRefs();
        specializedFunctions.put(function, generics, newImplementation);
        specializedFunctionGenerics.put(newImplementation, generics);
        prog.getFunctions().add(newImplementation);
        translator.recordSpecialisation(newImplementation, function, generics.getTypeArguments());
        recordCopiedTypeVars(function.getTypeVariables(), newImplementation.getTypeVariables());
        newImplementation.getTypeVariables().removeAll();
        newImplementation.setName(function.getName() + "_specialized");
        rewriteGenerics(newImplementation, generics, typeVariables);
        if (needsGlobalSpecialization(function)
            || (specializeTupleValueTypes && genericTypesContainTuple(generics))) {
            GenericTypes ownerGenerics = generics.take(owningClass.getTypeVariables().size());
            specializeClass(owningClass, ownerGenerics);
            rewriteOwnedGenericGlobals(newImplementation, owningClass, ownerGenerics);
        }
        collectGenericNewUses(newImplementation);
        return newImplementation;
    }

    private void adaptSubmethods(List<ImMethod> oldSubMethods, ImMethod newM, GenericTypes generics) {
        newM.setSubMethods(new ArrayList<>());
        ImClassType newClassT = newM.getMethodClass();
        ImClass newMClass = newClassT.getClassDef();
        for (ImMethod subMethod : oldSubMethods) {
            ImClassType subClassT = subMethod.getMethodClass();
            ImClass subClass = subClassT.getClassDef();
            if (isGenericType(subClassT)) {
                if (genericNewOnly
                    && subClass.getTypeVariables().size() == generics.getTypeArguments().size()) {
                    ImMethod specializedSubMethod = specializeMethod(subMethod, generics);
                    // Lua keeps ordinary generic objects erased. Bind the concrete implementation
                    // to that erased class so interface dispatch on an existing object can reach it.
                    specializedSubMethod.getMethodClass().getClassDef().getMethods()
                        .remove(specializedSubMethod);
                    specializedSubMethod.setMethodClass(
                        JassIm.ImClassType(subClass, JassIm.ImTypeArguments()));
                    subClass.getMethods().add(specializedSubMethod);
                    newM.getSubMethods().add(specializedSubMethod);
                    continue;
                }
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
                ImType original = ta.getType();
                ta.setType(transformType(original, generics, typeVars));
                inheritTypeClassBinding(ta, original, generics, typeVars);
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

            @Override
            public void visit(ImTypeVarDispatch e) {
                super.visit(e);
                resolveTypeClassDispatch(e, generics, typeVars);
            }

        });
    }

    /**
     * Replaces a type class dispatch by a direct call once the type variable it dispatches on has
     * been substituted by a concrete type argument.
     * <p>
     * This is what keeps bounded generics free of runtime cost: after specialisation the call is an
     * ordinary static call to the instance function, with no lookup and no indirection left.
     */
    /**
     * Carries a type class binding down into a nested call.
     * <p>
     * When a bounded generic passes its own type parameter on to another bounded generic, the inner
     * call site cannot know the instance: the parameter is still abstract there. Substituting the
     * outer parameter also supplies the instance it was specialised with, which is what makes a
     * chain of bounded generics resolve without any runtime dictionary.
     */
    private void inheritTypeClassBinding(ImTypeArgument ta, ImType original,
                                         GenericTypes generics, List<ImTypeVar> typeVars) {
        if (!ta.getTypeClassBinding().isEmpty() || !(original instanceof ImTypeVarRef ref)) {
            return;
        }
        int index = indexOfTypeVar(typeVars, ref.getTypeVariable());
        if (index < 0 || index >= generics.getTypeArguments().size()) {
            return;
        }
        Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> outer =
            generics.getTypeArguments().get(index).getTypeClassBinding();
        if (!outer.isEmpty()) {
            ta.setTypeClassBinding(new LinkedHashMap<>(outer));
        }
    }

    /**
     * A type variable can be represented by more than one node for the same source type parameter,
     * so match on the name as the rest of this pass does.
     */
    private static String enclosingFunctionName(Element e) {
        Element cur = e;
        while (cur != null && !(cur instanceof ImFunction)) {
            cur = cur.getParent();
        }
        return cur == null ? "?" : ((ImFunction) cur).getName();
    }

    /**
     * Where {@code target} sits in {@code typeVars}, comparing what each was copied from rather than
     * the nodes themselves: moving a function out of its class copies the class's type variables onto
     * it, so one source parameter is several nodes. Comparing names instead would make two parameters
     * which merely share a name look like one.
     */
    /**
     * Pairs a copy's type variables with the ones they were copied from, index by index.
     * <p>
     * Copying a function or a class copies its type variables with it, and the references inside the
     * copy point at the new nodes. The copy's list is then emptied, which leaves those nodes with no
     * owner at all - so a reference reached later has nothing to compare against but a name. Recorded
     * before that happens, the copy still leads back to the parameter it stands for.
     */
    private void recordCopiedTypeVars(List<ImTypeVar> originals, List<ImTypeVar> copies) {
        for (int i = 0; i < originals.size() && i < copies.size(); i++) {
            translator.recordSpecialisation(copies.get(i), originals.get(i));
        }
    }

    private int indexOfTypeVar(List<ImTypeVar> typeVars, ImTypeVar target) {
        ImTypeVar wanted = translator.canonical(target);
        for (int i = 0; i < typeVars.size(); i++) {
            if (translator.canonical(typeVars.get(i)) == wanted) {
                return i;
            }
        }
        return -1;
    }

    private void resolveTypeClassDispatch(ImTypeVarDispatch e, GenericTypes generics, List<ImTypeVar> typeVars) {
        int index = indexOfTypeVar(typeVars, e.getTypeVariable());
        if (index >= 0 && index >= generics.getTypeArguments().size()) {
            // Fewer arguments than variables: the variables and the arguments are not in
            // correspondence here, so position says nothing. Reading one anyway would dispatch
            // through whichever type happened to sit at that index.
            return;
        }
        if (index < 0) {
            // dispatching on a variable of some enclosing generic; it is resolved when that one is
            // specialised.
            return;
        }
        ImTypeArgument typeArgument = generics.getTypeArguments().get(index);
        Either<ImMethod, ImFunction> impl = typeArgument.getTypeClassBinding().get(e.getTypeClassFunc());
        if (impl == null) {
            ImFunction fromRegistry = translator.lookupTypeClassImpl(e.getTypeClassFunc(), typeArgument.getType());
            if (fromRegistry != null) {
                impl = Either.right(fromRegistry);
            }
        }
        if (impl == null) {
            if (containsTypeVariable(typeArgument.getType())) {
                // Not an instantiation: passes which only rename or move type variables, such as
                // lifting a class's variables onto its functions, substitute one variable for
                // another. The dispatch is resolved once a concrete type argument arrives.
                return;
            }
            throw new CompileError(e.attrTrace().attrSource(),
                "No type class instance bound for " + e.getTypeClassFunc().getName()
                    + " on type argument " + typeArgument.getType()
                    + " (type variable " + e.getTypeVariable().getName()
                    + ", index " + index + " of " + typeVars.size()
                    + ", in " + enclosingFunctionName(e) + ").");
        }
        ImExprs args = e.getArguments();
        args.setParent(null);
        if (impl.isRight()) {
            e.replaceBy(JassIm.ImFunctionCall(e.getTrace(), impl.get(), JassIm.ImTypeArguments(), args,
                false, CallType.NORMAL));
        } else {
            ImMethod method = impl.getLeft();
            e.replaceBy(JassIm.ImFunctionCall(e.getTrace(), method.getImplementation(), JassIm.ImTypeArguments(),
                args, false, CallType.NORMAL));
        }
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
        // The copy is structural, so field i of the copy is field i of the original. Nothing will
        // refer to the copies, so this is the only record that they are the same fields.
        for (int i = 0; i < c.getFields().size() && i < newC.getFields().size(); i++) {
            translator.recordSpecialisation(newC.getFields().get(i), c.getFields().get(i), generics.getTypeArguments());
        }
        for (int i = 0; i < c.getMethods().size() && i < newC.getMethods().size(); i++) {
            ImMethod originalMethod = c.getMethods().get(i);
            ImMethod copiedMethod = newC.getMethods().get(i);
            translator.recordSpecialisation(copiedMethod, originalMethod, generics.getTypeArguments());
            if (originalMethod.getImplementation() != null && copiedMethod.getImplementation() != null) {
                translator.recordSpecialisation(copiedMethod.getImplementation(),
                    originalMethod.getImplementation(), generics.getTypeArguments());
            }
        }
        for (int i = 0; i < c.getFunctions().size() && i < newC.getFunctions().size(); i++) {
            translator.recordSpecialisation(newC.getFunctions().get(i), c.getFunctions().get(i),
                generics.getTypeArguments());
        }
        translator.recordSpecialisation(newC, c, generics.getTypeArguments());
        specializedClasses.put(c, generics, newC);
        prog.getClasses().add(newC);
        recordCopiedTypeVars(c.getTypeVariables(), newC.getTypeVariables());
        newC.getTypeVariables().removeAll();

        newC.setName(genericNewOnly
            ? c.getName() + "_specialized_" + generics.makeName()
            : c.getName() + "⟪" + generics.makeName() + "⟫");
        List<ImTypeVar> typeVars = c.getTypeVariables();
        rewriteGenerics(newC, generics, typeVars);
        newC.getSuperClasses().replaceAll(this::specializeType);
        if (needsRuntimeTypeSpecialization(c, generics, new HashSet<>())) {
            rewriteRuntimeTypeSuperEdges(c, generics, newC);
        }

        // NEW: Create specialized global variables for this class instantiation
        createSpecializedGlobals(c, generics, typeVars);
        if (genericNewOnly && (classOwnsGenericGlobals(c)
            || (specializeTupleValueTypes && genericTypesContainTuple(generics)))) {
            rewriteOwnedGenericGlobals(newC, c, generics);
        }

        if (genericNewOnly && (isConstructionOnlyInstantiation(c)
            || (specializeTupleValueTypes && genericTypesContainTuple(generics)))) {
            attachSpecializedClassMethods(c, newC, generics);
        }

        onSpecializedClassTriggers.get(c).forEach(consumer ->
            consumer.accept(generics, newC));
        return newC;
    }

    /**
     * Makes the methods of a class specialised from a construction reachable.
     * <p>
     * A class specialised because a call named its instantiation is reached through that call.
     * One specialised because it was constructed is not: the receiver is held as its interface, so
     * dispatch goes through the root method, whose submethods still list only the generic original.
     * Each copy is bound to the same roots, and the original's implementation is recorded as having
     * a specialisation so the dispatch left behind in it settles instead of reaching the backend.
     */
    private void attachSpecializedClassMethods(ImClass original, ImClass specialized, GenericTypes generics) {
        List<ImMethod> originalMethods = original.getMethods();
        List<ImMethod> specializedMethods = specialized.getMethods();
        if (originalMethods.size() != specializedMethods.size()) {
            // The copy is structural, so this cannot happen; bail rather than pair the wrong ones.
            return;
        }
        Map<ImMethod, ImMethod> specializationOf = new IdentityHashMap<>();
        for (int i = 0; i < originalMethods.size(); i++) {
            ImMethod copy = specializedMethods.get(i);
            copy.setMethodClass(JassIm.ImClassType(specialized, JassIm.ImTypeArguments()));
            specializationOf.put(originalMethods.get(i), copy);

            ImFunction implementation = originalMethods.get(i).getImplementation();
            ImFunction copyImplementation = copy.getImplementation();
            if (implementation != null && copyImplementation != null && implementation != copyImplementation
                && specializedFunctions.get(implementation, generics) == null) {
                specializedFunctions.put(implementation, generics, copyImplementation);
            }
        }
        for (ImClass c : new ArrayList<>(prog.getClasses())) {
            for (ImMethod root : c.getMethods()) {
                for (ImMethod sub : new ArrayList<>(root.getSubMethods())) {
                    ImMethod copy = specializationOf.get(sub);
                    if (copy != null && !root.getSubMethods().contains(copy)) {
                        root.getSubMethods().add(copy);
                    }
                }
            }
        }
    }

    private ImExpr rewriteGenericGlobalsInExpr(ImExpr e, ImClass owningClass, GenericTypes generics) {
        e.accept(new Element.DefaultVisitor() {
            @Override public void visit(ImVarAccess va) {
                super.visit(va);
                ImVar v = va.getVar();
                ImClass owner = globalToClass.get(v);
                if (owner == null) return;

                GenericTypes g = adaptGenericsToOwner(owningClass, generics, owner);
                if (g == null || g.containsTypeVariable()) return;

                ImVar sg = ensureSpecializedGlobal(v, owner, g);
                if (sg != null) va.setVar(sg);
            }

            @Override public void visit(ImVarArrayAccess aa) {
                super.visit(aa);
                ImVar v = aa.getVar();
                ImClass owner = globalToClass.get(v);
                if (owner == null) return;

                GenericTypes g = adaptGenericsToOwner(owningClass, generics, owner);
                if (g == null || g.containsTypeVariable()) return;

                ImVar sg = ensureSpecializedGlobal(v, owner, g);
                if (sg != null) aa.setVar(sg);
            }
        });
        return e;
    }

    private void createSpecializedGlobals(ImClass originalClass, GenericTypes generics, List<ImTypeVar> typeVars) {
        // Collect "insert specialized init right after original init" operations per parent ImStmts
        // Using identity maps because IM nodes use identity semantics for parent/ownership.
        Map<ImStmts, IdentityHashMap<ImStmt, List<ImStmt>>> insertsByParent = new IdentityHashMap<>();
        List<Map.Entry<ImVar, ImVar>> newlyCreated = new ArrayList<>();

        // Establish the complete declaration environment before translating any initializer. An
        // initializer may refer to a later static of the same class; interleaving declaration and
        // initializer lowering would make correctness depend on global iteration order.
        for (Map.Entry<ImVar, ImClass> entry : globalToClass.entrySet()) {
            ImVar originalGlobal = entry.getKey();
            ImClass owningClass = entry.getValue();

            if (translator.canonical(owningClass) != translator.canonical(originalClass)) continue;

            if (specializedGlobals.contains(originalGlobal, generics)) continue;

            ImType specializedType = transformType(originalGlobal.getType(), generics, typeVars);

            String specializedName = originalGlobal.getName() + "⟪" + generics.makeName() + "⟫";
            ImVar specializedGlobal = JassIm.ImVar(
                originalGlobal.getTrace(),
                specializedType,
                specializedName,
                originalGlobal.getIsBJ()
            );

            // Create + register global
            translator.addGlobal(specializedGlobal);
            // Both halves of what the interpreter used to read out of the name: what this was copied
            // from, and which class it belongs to.
            translator.recordSpecialisation(specializedGlobal, originalGlobal, generics.getTypeArguments());
            translator.recordGenericStaticOwner(specializedGlobal, originalClass);
            translator.recordGenericStaticOwner(originalGlobal, originalClass);
            specializedGlobals.put(originalGlobal, generics, specializedGlobal);
            dbg("Created specialized global: " + specializedName + " type=" + specializedType);
            newlyCreated.add(Map.entry(originalGlobal, specializedGlobal));
        }

        for (Map.Entry<ImVar, ImVar> specialization : newlyCreated) {
            ImVar originalGlobal = specialization.getKey();
            ImVar specializedGlobal = specialization.getValue();
            ImType specializedType = specializedGlobal.getType();

            // If original has init(s), create corresponding specialized init(s) and schedule insertion
            List<ImSet> originalInits = prog.getGlobalInits().get(originalGlobal);
            if (originalInits != null && !originalInits.isEmpty()) {

                ImStmts parentStmts = null;
                boolean hasDetachedInits = false;
                for (ImSet s : originalInits) {
                    if (s.getParent() == null) {
                        hasDetachedInits = true;
                        continue;
                    }
                    if (!(s.getParent() instanceof ImStmts)) {
                        throw new CompileError(originalGlobal,
                            "Initializer for global " + originalGlobal.getName() + " is not inside ImStmts.");
                    }
                    ImStmts currParent = (ImStmts) s.getParent();
                    if (parentStmts == null) {
                        parentStmts = currParent;
                    } else if (parentStmts != currParent) {
                        throw new CompileError(originalGlobal,
                            "Initializer statements for global " + originalGlobal.getName() + " are not in the same ImStmts.");
                    }
                }
                if (hasDetachedInits && parentStmts != null) {
                    throw new CompileError(originalGlobal,
                        "Initializer statements for global " + originalGlobal.getName() + " are inconsistently attached.");
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

                    // Append after earlier specializations of this initializer. Each invocation of
                    // createSpecializedGlobals has its own insertion batch; always inserting after
                    // origSet would therefore reverse specialization discovery/initializer order.
                    if (parentStmts != null) {
                        ImStmt insertionPoint = specializedInitializerTails.getOrDefault(origSet, origSet);
                        IdentityHashMap<ImStmt, List<ImStmt>> byStmt =
                            insertsByParent.computeIfAbsent(parentStmts, k -> new IdentityHashMap<>());
                        byStmt.computeIfAbsent(insertionPoint, k -> new ArrayList<>(1)).add(specSet);
                        specializedInitializerTails.put(origSet, specSet);
                    }

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
        // Cache expensive recursive submethod checks within this traversal.
        Map<ImMethod, Boolean> hasGenericSubmethodCache = new IdentityHashMap<>();
        element.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall f) {
                super.visit(f);
                if (translator.isGenericNewMarker(f.getFunc())) {
                    genericsUses.add(new GenericNewCall(f));
                    return;
                }
                if (!f.getTypeArguments().isEmpty()) {
                    genericsUses.add(new GenericImFunctionCall(f));
                }
            }

            @Override
            public void visit(ImMethodCall mc) {
                super.visit(mc);
                ImMethod method = mc.getMethod();
                boolean hasTypeArgs = !mc.getTypeArguments().isEmpty();
                boolean needsDispatchSpecialization = false;
                // If type args are present, specialization is unconditional, so avoid extra checks.
                if (!hasTypeArgs) {
                    // Interface/base dispatch methods can be non-generic but still require specialization
                    // when they dispatch to generic implementors.
                    needsDispatchSpecialization = methodImplementationIsGeneric(method);
                    if (!needsDispatchSpecialization) {
                        needsDispatchSpecialization = hasGenericSubmethodCache.computeIfAbsent(
                            method,
                            EliminateGenerics.this::hasGenericSubmethodImplementation
                        );
                    }
                }
                if (hasTypeArgs || needsDispatchSpecialization) {
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

    private boolean methodImplementationIsGeneric(ImMethod method) {
        ImFunction implementation = method.getImplementation();
        return implementation != null && !implementation.getTypeVariables().isEmpty();
    }

    private boolean hasGenericSubmethodImplementation(ImMethod method) {
        return hasGenericSubmethodImplementation(method, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private boolean hasGenericSubmethodImplementation(ImMethod method, Set<ImMethod> visited) {
        if (!visited.add(method)) {
            return false;
        }
        for (ImMethod subMethod : method.getSubMethods()) {
            if (methodImplementationIsGeneric(subMethod)) {
                return true;
            }
            if (hasGenericSubmethodImplementation(subMethod, visited)) {
                return true;
            }
        }
        return false;
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
            specializedCallSites.add(fc);
        }
    }

    /**
     * A call naming a function of a generic class, rewritten to the copy specialised for the
     * instantiation the call is for. That instantiation came from the receiver it was handed or from
     * the type the result is stored in, rather than from the call, so there are no type arguments on
     * the call to clear.
     */
    class GenericClassFunctionCall implements GenericUse {
        private final ImFunctionCall call;
        private final ImClass owningClass;
        private final GenericTypes generics;

        GenericClassFunctionCall(ImFunctionCall call, ImClass owningClass, GenericTypes generics) {
            this.call = call;
            this.owningClass = owningClass;
            this.generics = generics;
        }

        @Override
        public void eliminate() {
            call.setFunc(specializeClassFunction(call.getFunc(), owningClass, call, generics));
            specializedCallSites.add(call);
        }
    }

    class GenericNewCall implements GenericUse {
        private final ImFunctionCall call;

        GenericNewCall(ImFunctionCall call) {
            this.call = call;
        }

        @Override
        public void eliminate() {
            if (call.getTypeArguments().size() != 1) {
                throw new CompileError(call, CompilerIntrinsics.NEW
                    + " expects exactly one type argument and no value arguments.");
            }

            ImType targetType = call.getTypeArguments().get(0).getType();
            if (containsTypeVariable(targetType)) {
                throw new CompileError(call, CompilerIntrinsics.NEW
                    + " requires its type argument to resolve to a concrete class.");
            }
            if (!(targetType instanceof ImClassType classType)) {
                throw new CompileError(call, CompilerIntrinsics.NEW
                    + " requires a concrete, non-abstract class type, but found " + targetType + ".");
            }

            ImClass imClass = classType.getClassDef();
            if (!(imClass.getTrace() instanceof ClassDef classDef)) {
                String kind = imClass.getTrace() instanceof InterfaceDef ? "interface" : "type";
                throw new CompileError(call, CompilerIntrinsics.NEW + " cannot construct " + kind + " "
                    + imClass.getName() + ".");
            }
            if (classDef.attrIsAbstract()) {
                throw new CompileError(call, CompilerIntrinsics.NEW + " cannot construct abstract class "
                    + classDef.getName() + ".");
            }
            ConstructorDef constructor = zeroArgumentConstructor(classDef);
            if (constructor == null) {
                throw new CompileError(call, CompilerIntrinsics.NEW + " requires class " + classDef.getName()
                    + " to have a zero-argument constructor.");
            }
            de.peeeq.wurstscript.ast.Element source = call.getTrace();
            if (constructor.attrIsPrivate() && (source == null || !source.isSubtreeOf(classDef))) {
                throw new CompileError(call, CompilerIntrinsics.NEW
                    + " cannot access the zero-argument constructor of class " + classDef.getName() + ".");
            }

            ImFunction constructorFunction = translator.getConstructNewFunc(constructor);
            ImTypeArguments constructorTypeArguments = JassIm.ImTypeArguments();
            for (ImTypeArgument argument : classType.getTypeArguments()) {
                constructorTypeArguments.add(argument.copy());
            }
            ImFunctionCall replacement = JassIm.ImFunctionCall(call.getTrace(), constructorFunction,
                constructorTypeArguments, JassIm.ImExprs(), false, CallType.NORMAL);
            call.replaceBy(replacement);
            if (!genericNewOnly && !constructorTypeArguments.isEmpty()) {
                genericsUses.addFirst(new GenericImFunctionCall(replacement));
            }
        }

        private ConstructorDef zeroArgumentConstructor(ClassDef classDef) {
            for (ConstructorDef constructor : classDef.getConstructors()) {
                if (constructor.getParameters().isEmpty()) {
                    return constructor;
                }
            }
            return null;
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
            specializedCallSites.add(mc);
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

        ImVar sg = specializedGlobals.get(originalGlobal, concreteGenerics);
        if (sg != null) return sg;

        // Ensure class specialization exists (this should also call createSpecializedGlobals)
        specializeClass(owningClass, concreteGenerics);

        sg = specializedGlobals.get(originalGlobal, concreteGenerics);
        if (sg != null) return sg;

        throw new CompileError(originalGlobal,
            "Generic static specialization was not created for " + originalGlobal.getName()
                + " in " + owningClass.getName() + " with " + concreteGenerics + ".");
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

    /** Infer class type arguments from the enclosing function's structural specialization context. */
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

                        ImClass canonicalRaw = translator.canonical(raw);
                        ImClass canonicalOwner = translator.canonical(owningClass);
                        boolean matches = canonicalRaw == canonicalOwner
                            || canonicalRaw.isSubclassOf(canonicalOwner);

                        if (matches) {
                            if (!ct.getTypeArguments().isEmpty()) {
                                List<ImTypeArgument> copied = new ArrayList<>(ct.getTypeArguments().size());
                                for (ImTypeArgument ta : ct.getTypeArguments()) {
                                    copied.add(JassIm.ImTypeArgument(ta.getType().copy(), ta.getTypeClassBinding()));
                                }
                                return normalizeToClassArity(new GenericTypes(copied), owningClass, "receiverTypeArgs:" + func.getName());
                            }

                            ImTranslator.Specialisation classSpecialisation = translator.specialisationOf(raw);
                            if (classSpecialisation != null
                                && !classSpecialisation.typeArguments().isEmpty()) {
                                return normalizeToClassArity(
                                    new GenericTypes(classSpecialisation.typeArguments()), owningClass,
                                    "receiverSpecialisation:" + func.getName());
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
        WLogger.trace(() -> "[ELIMGEN] " + msg);
    }

}
