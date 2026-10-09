package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.collect.ImmutableList.Builder;
import de.peeeq.datastructures.Partitions;
import de.peeeq.datastructures.TransitiveClosure;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.PackageLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.NamePreservation;
import de.peeeq.wurstscript.validation.WurstValidator;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.peeeq.wurstscript.jassIm.JassIm.*;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.*;
import static de.peeeq.wurstscript.utils.Utils.elementNameWithPath;

public class ImTranslator implements SpecialisationLookup {


    public static final String $DEBUG_PRINT = "$debugPrint";

    /**
     * What each specialised node was copied from, and under which type arguments.
     * <p>
     * Specialising a generic entity makes a new node rather than recording a relation, so the copy has
     * no way to say what it stands for. Passes then recover that by other means, and each of those
     * means has been wrong: a pass which drops what nothing reads dropped every copied field, because
     * an access made before specialisation still names the original's variable; a lookup which matched
     * type variables by name took two parameters sharing a name for one, which dispatched a value
     * through the wrong instance; and a name composed from a copy's mangled name reads the type
     * argument as though it were a method name.
     * <p>
     * This is that relation, in one place: a copy, what it was copied from, and the type arguments the
     * copy was made for. Identity, liveness and naming can all be answered from it rather than from a
     * name, which is what those three passes were doing by hand.
     */
    public record Specialisation(Element original, List<ImTypeArgument> typeArguments) {
    }

    private final Map<Element, Specialisation> specialisations = new IdentityHashMap<>();
    private final Map<ImClass, Set<GenericTypes>> erasedGenericAllocations = new IdentityHashMap<>();

    /**
     * @param typeArguments the arguments the copy was made for, empty when a copy carries none of its
     *                      own - moving a function out of its class copies the class's type variables
     *                      onto it without specialising anything
     */
    public void recordSpecialisation(Element copy, Element original, List<ImTypeArgument> typeArguments) {
        specialisations.put(copy, new Specialisation(original, List.copyOf(typeArguments)));
    }

    public void recordSpecialisation(Element copy, Element original) {
        recordSpecialisation(copy, original, List.of());
    }

    /**
     * The generic class a static field belongs to.
     * <p>
     * A static field of a generic class becomes a global named after the class, and the interpreter
     * used to recover the owner by taking the longest prefix of that name ending at an underscore
     * which matches a class name. A class whose name contains an underscore, or a field whose name
     * begins like a class, answers that wrongly and silently. Recorded here instead, where it is
     * known.
     */
    private final Map<ImVar, ImClass> genericStaticOwners = new IdentityHashMap<>();

    public void recordGenericStaticOwner(ImVar global, ImClass owner) {
        genericStaticOwners.put(global, owner);
    }

    public @Nullable ImClass genericStaticOwnerOf(ImVar global) {
        return genericStaticOwners.get(global);
    }

    /** What {@code copy} was made from and for, or null when it is not a copy. */
    public @Nullable Specialisation specialisationOf(Element copy) {
        return specialisations.get(copy);
    }

    public void recordErasedGenericAllocation(ImClass clazz, List<ImTypeArgument> typeArguments) {
        erasedGenericAllocations.computeIfAbsent(canonical(clazz), ignored -> new HashSet<>())
            .add(new GenericTypes(typeArguments));
    }

    public boolean hasErasedAllocationWithoutStaticSpecialization(ImClass clazz, ImVar originalStatic) {
        Set<GenericTypes> allocations = erasedGenericAllocations.get(canonical(clazz));
        if (allocations == null || allocations.isEmpty()) {
            return false;
        }
        Set<GenericTypes> specializedStatics = new HashSet<>();
        for (Map.Entry<Element, Specialisation> entry : specialisations.entrySet()) {
            Specialisation specialization = entry.getValue();
            if (specialization.original() == originalStatic) {
                specializedStatics.add(new GenericTypes(specialization.typeArguments()));
            }
        }
        for (GenericTypes allocation : allocations) {
            if (!specializedStatics.contains(allocation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The node {@code copy} was ultimately copied from, or {@code copy} itself.
     * <p>
     * A copy of a copy is possible, so this follows to the root. The relation is acyclic by
     * construction because a copy is always newer than what it was made from; the bound is there so a
     * mistake elsewhere fails loudly rather than hanging.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Element> T canonical(T copy) {
        Element current = copy;
        for (int steps = 0; steps < 1000; steps++) {
            Specialisation specialisation = specialisations.get(current);
            if (specialisation == null) {
                return (T) current;
            }
            current = specialisation.original();
        }
        throw new IllegalStateException("specialisation chain does not terminate at " + copy);
    }

    private static final de.peeeq.wurstscript.ast.Element emptyTrace = Ast.NoExpr();

    // existing fields (keep callRelations as Guava Multimap to avoid ripple effects)
    private Multimap<ImFunction, ImFunction> callRelations;

    // swap these Sets to fastutil; keep accessors returning Set if you have them
    private ReferenceOpenHashSet<ImFunction> usedFunctions;
    private ObjectOpenHashSet<ImVar> usedVariables;  // or ReferenceOpenHashSet<ImVar> if identity semantics are intended
    private ObjectOpenHashSet<ImVar> readVariables;  // ditto

    private @Nullable ImFunction debugPrintFunction;

    private final Map<TranslatedToImFunction, ImFunction> functionMap = new Object2ObjectLinkedOpenHashMap<>();
    private @Nullable ImFunction globalInitFunc;

    private final ImProg imProg;

    public final Map<WPackage, ImFunction> initFuncMap = new Object2ObjectLinkedOpenHashMap<>();

    /** Initializer functions in the exact order emitted by {@link #finishInitFunctions()}. */
    private final List<ImFunction> initializationOrder = new ArrayList<>();

    /**
     * When targeting Lua, package init functions that should be called directly via xpcall
     * rather than through the JASS TriggerEvaluate thread-isolation pattern.
     * Populated during translateProg() when isLuaTarget() is true.
     */
    public final Map<ImFunction, String> luaInitFunctions = new Object2ObjectLinkedOpenHashMap<>();

    private final Map<TranslatedToImFunction, ImVar> thisVarMap = new Object2ObjectLinkedOpenHashMap<>();

    private final Set<WPackage> translatedPackages = new ObjectLinkedOpenHashSet<>();
    private final Set<ClassDef> translatedClasses = new ObjectLinkedOpenHashSet<>();


    private final Map<VarDef, ImVar> varMap = new Object2ObjectLinkedOpenHashMap<>();

    private final WurstModel wurstProg;

    private @Nullable ImFunction mainFunc = null;

    private @Nullable ImFunction configFunc = null;

    @Nullable public ImFunction ensureIntFunc = null;
    @Nullable public ImFunction ensureBoolFunc = null;
    @Nullable public ImFunction ensureRealFunc = null;
    @Nullable public ImFunction ensureStrFunc = null;
    @Nullable public ImFunction stringConcatFunc = null;
    // Exact synthetic nodes owned by LuaNativeLowering; backend intrinsic recognition must use identity.
    @Nullable public ImFunction luaRawFloorDivIntFunc = null;
    @Nullable public ImFunction luaRawFmodIntFunc = null;
    @Nullable public ImFunction luaRawFmodRealFunc = null;
    @Nullable public ImFunction luaRawFloorModIntFunc = null;
    @Nullable public ImFunction luaRawConcatFunc = null;
    /** {@code x or ""}: a string operand which may be nil, made safe for {@link #luaRawConcatFunc}. */
    @Nullable public ImFunction luaRawOrEmptyFunc = null;
    /** {@code R2I} in Lua arithmetic, falling back to the native outside the 32-bit range; see ExprTranslation. */
    @Nullable public ImFunction luaRawR2IFunc = null;
    /** The one-argument conversions the ensure helpers use; printed as direct Lua calls. */
    @Nullable public ImFunction luaRawToNumberIntFunc = null;
    @Nullable public ImFunction luaRawToNumberRealFunc = null;
    @Nullable public ImFunction luaRawToIntegerFunc = null;
    @Nullable public ImFunction luaRawToStringFunc = null;
    /**
     * The Lua keyed-table and keyed-map native stubs, by stub name. {@link LuaNativeLowering#lowerKeyedTables}
     * creates each one once and a second lowering reuses it, so later passes can match the very node a
     * call was lowered to instead of a name.
     */
    public final Map<String, ImFunction> luaKeyedStubs = new LinkedHashMap<>();

    /**
     * A call to one of the Lua backend's operator intrinsics which cannot fail at runtime: a
     * concatenation, or a division or remainder whose divisor is a non-zero literal. Such a call
     * is as pure as the operator it prints as, so an unused one may be dropped.
     */
    public boolean isTrapFreeLuaIntrinsicCall(ImFunctionCall call) {
        ImFunction target = call.getFunc();
        if (target == luaRawOrEmptyFunc && call.getArguments().size() == 1) {
            return true;
        }
        if (call.getArguments().size() != 2) {
            return false;
        }
        if (target == luaRawConcatFunc) {
            return true;
        }
        ImExpr divisor = call.getArguments().get(1);
        boolean nonZeroDivisor = (divisor instanceof ImIntVal intVal && intVal.getValI() != 0)
            || (divisor instanceof ImRealVal realVal && Double.parseDouble(realVal.getValR()) != 0.0);
        return nonZeroDivisor
            && (target == luaRawFloorDivIntFunc || target == luaRawFmodIntFunc
                || target == luaRawFloorModIntFunc || target == luaRawFmodRealFunc);
    }
    /**
     * Whether {@code f} is one of the Lua keyed-map read stubs, which the backend prints as a table
     * index. A call whose result nothing uses may be dropped, as for the Jass natives which only read.
     */
    public boolean isLuaKeyedMapRead(ImFunction f) {
        return LuaKeyedMap.readStubName(this, f) != null;
    }
    @Nullable public ImFunction luaIntDivFunc = null;
    @Nullable public ImFunction luaModIntFunc = null;
    @Nullable public ImFunction luaModRealFunc = null;

    /**
     * The functions this translator created for passes which run later and call them through the handle it keeps
     * (the Lua helpers above, the error and debug-print functions), not through a call that already is in the program:
     * no body calls them yet, and a function which nothing reaches is removed. A new field of this kind has to be
     * listed here ({@code ImTranslatorPinnedFunctionsTests} fails until it is).
     */
    public List<ImFunction> pinnedFunctions() {
        List<ImFunction> result = new ArrayList<>();
        for (ImFunction f : new ImFunction[]{
            ensureIntFunc, ensureBoolFunc, ensureRealFunc, ensureStrFunc, stringConcatFunc,
            luaRawFloorDivIntFunc, luaRawFmodIntFunc, luaRawFmodRealFunc, luaRawFloorModIntFunc, luaRawConcatFunc,
            luaRawOrEmptyFunc, luaRawR2IFunc, luaRawToNumberIntFunc, luaRawToNumberRealFunc, luaRawToIntegerFunc,
            luaRawToStringFunc, luaIntDivFunc, luaModIntFunc, luaModRealFunc,
            debugPrintFunction, errorFunc, genericNewMarker, globalInitFunc}) {
            if (f != null) {
                result.add(f);
            }
        }
        result.addAll(luaKeyedStubs.values());
        return result;
    }

    /**
     * The globals this translator created for passes which run later and reach them through its handle, not through
     * an access in a body: the variables which manage the instances of a class, which the class elimination builds
     * the allocators and the dispatch from. A new field of this kind has to be listed here.
     */
    public List<ImVar> pinnedGlobals() {
        List<ImVar> result = new ArrayList<>();
        if (classManagementVars != null) {
            Set<ClassManagementVars> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            for (ClassManagementVars vars : classManagementVars.values()) {
                if (seen.add(vars)) {
                    result.add(vars.free);
                    result.add(vars.freeCount);
                    result.add(vars.maxIndex);
                    result.add(vars.typeId);
                }
            }
        }
        return result;
    }

    private final Map<ImVar, VarsForTupleResult> varsForTupleVar = new Object2ObjectLinkedOpenHashMap<>();

    private final boolean isUnitTestMode;

    private final ImVar lastInitFunc = JassIm.ImVar(emptyTrace, WurstTypeString.instance().imTranslateType(this), "lastInitFunc", false);

    private int compiletimeOrderCounter = 1;
    private final Map<TranslatedToImFunction, FunctionFlagCompiletime> compiletimeFlags = new HashMap<>();
    private final Map<ExprFunctionCall, Integer> compiletimeExpressionsOrder = new HashMap<>();

    de.peeeq.wurstscript.ast.Element lasttranslatedThing;
    private final boolean debug = false;
    private final RunArgs runArgs;

    private final Map<ClassDef, Map<TypeParamDef, ImTypeVar>> capturedOwnerTypeVarsByStaticClass = new IdentityHashMap<>();
    private final Deque<Map<TypeParamDef, ImTypeVar>> typeVarOverrideStack = new ArrayDeque<>();
    private final Deque<ImVar> continueFlagStack = new ArrayDeque<>();

    private static boolean hasTypeVarNamed(ImTypeVars vars, String name) {
        for (ImTypeVar v : vars) {
            if (v.getName().equals(name)) return true;
        }
        return false;
    }

    private static String uniqueTypeVarName(ImTypeVars vars, TypeParamDef tp) {
        String name = tp.getName();
        if (!hasTypeVarNamed(vars, name)) {
            return name;
        }

        String baseName = name + "$" + typeParamOwnerName(tp);
        String candidate = baseName;
        int suffix = 2;
        while (hasTypeVarNamed(vars, candidate)) {
            candidate = baseName + suffix;
            suffix++;
        }
        return candidate;
    }

    private static String typeParamOwnerName(TypeParamDef tp) {
        de.peeeq.wurstscript.ast.Element e = tp.getParent();
        while (e != null) {
            if (e instanceof FuncDef) {
                return ((FuncDef) e).getName();
            }
            if (e instanceof NamedScope) {
                return ((NamedScope) e).getName();
            }
            e = e.getParent();
        }
        return "type";
    }

    public Map<TypeParamDef, ImTypeVar> getTypeVarOverridesForClass(ClassDef cd) {
        Map<TypeParamDef, ImTypeVar> m = capturedOwnerTypeVarsByStaticClass.get(cd);
        return (m == null) ? Collections.emptyMap() : m;
    }

    public void pushTypeVarOverrides(Map<TypeParamDef, ImTypeVar> m) {
        if (m != null && !m.isEmpty()) typeVarOverrideStack.push(m);
    }

    public void popTypeVarOverrides(Map<TypeParamDef, ImTypeVar> m) {
        if (m != null && !m.isEmpty()) typeVarOverrideStack.pop();
    }

    public void pushContinueFlag(ImVar continueFlag) {
        continueFlagStack.push(continueFlag);
    }

    public void popContinueFlag() {
        continueFlagStack.pop();
    }

    public @Nullable ImVar currentContinueFlag() {
        return continueFlagStack.peek();
    }

    public Map<TypeParamDef, ImTypeVar> getTypeVarOverridesForFunction(ImFunction f) {
        Map<TypeParamDef, ImTypeVar> result = new IdentityHashMap<>();
        for (ImTypeVar tv : f.getTypeVariables()) {
            TypeParamDef tp = typeVariableReverse.get(tv);
            if (tp != null) {
                result.put(tp, tv);
            }
        }
        return result;
    }


    public ImTranslator(WurstModel wurstProg, boolean isUnitTestMode, RunArgs runArgs) {
        this.wurstProg = wurstProg;
        this.lasttranslatedThing = wurstProg;
        this.isUnitTestMode = isUnitTestMode;
        imProg = ImProg(wurstProg, ImVars(), ImFunctions(), ImMethods(), JassIm.ImClasses(), JassIm.ImTypeClassFuncs(), new Object2ObjectLinkedOpenHashMap<>());
        this.runArgs = runArgs;
    }


    /**
     * translates a program
     */
    public ImProg translateProg() {
        try {
            globalInitFunc = ImFunction(emptyTrace, "initGlobals", ImTypeVars(), ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
            addFunction(getGlobalInitFunc());
            debugPrintFunction = ImFunction(emptyTrace, $DEBUG_PRINT, ImTypeVars(), ImVars(JassIm.ImVar(wurstProg, WurstTypeString.instance().imTranslateType(this), "msg",
                    false)), ImVoid(), ImVars(), ImStmts(), flags(IS_NATIVE, IS_BJ));

            if(isLuaTarget()) {
                // Portable IM bodies (not IS_NATIVE stubs) - see LuaEnsureFunctions for why.
                List<ImFunction> luaHelperFunctions = new ArrayList<>();
                ensureIntFunc = LuaEnsureFunctions.buildEnsureInt(luaHelperFunctions, this);
                ensureBoolFunc = LuaEnsureFunctions.buildEnsureBool(luaHelperFunctions);
                ensureRealFunc = LuaEnsureFunctions.buildEnsureReal(luaHelperFunctions, this);
                ensureStrFunc = LuaEnsureFunctions.buildEnsureStr(luaHelperFunctions, this);
                stringConcatFunc = LuaEnsureFunctions.buildStringConcat(luaHelperFunctions, this);
                luaHelperFunctions.forEach(this::addFunction);
            }

            calculateCompiletimeOrder();

            for (CompilationUnit cu : wurstProg) {
                translateCompilationUnit(cu);
            }
            linkBridgedOverrides();
            addBridgeFunctions();

            if (mainFunc == null) {
                mainFunc = ImFunction(emptyTrace, "main", ImTypeVars(), ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
                addFunction(mainFunc);
            }
            if (configFunc == null) {
                configFunc = ImFunction(emptyTrace, "config", ImTypeVars(), ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
                addFunction(configFunc);
            }

            finishInitFunctions();
            // Passes after the tree shake make calls of the error function (the allocators of the class
            // elimination), so the one of the ErrorHandling package is named here, where the shake finds it
            // pinned. The default one is made when it is first needed (imError).
            if (errorFunc == null) {
                errorFunc = findErrorFunc().map(this::getFuncFor).orElse(null);
            }
            EliminateCallFunctionsWithAnnotation.process(imProg);
            removeDuplicateNatives(imProg);
            sortEverything();
            return imProg;
        } catch (CompileError t) {
            throw t;
        } catch (Throwable t) {
            WLogger.severe(t);
            throw new RuntimeException("There was a Wurst bug in the translation of "
                    + Utils.printElementWithSource(Optional.of(lasttranslatedThing))
                    + ": "
                    + t.getMessage()
                    + "\nPlease open a ticket with source code and the error log.", t);
        }
    }

    /** A method a class has of its own for a method it inherits ({@code InterfaceTranslator}), and that method. */
    private record Bridge(ImMethod bridge, ImMethod inherited) {
    }

    private final List<Bridge> bridges = new ArrayList<>();

    /**
     * Gives {@code bridge} the overrides of {@code inherited} which are below its class as sub-methods, once every
     * class is translated: the sub-methods of {@code inherited} come from the translation of its class, which may come
     * later. Then a deeper override stays reachable from the bridge, as AGENTS.md section 8 asks of a bridge.
     */
    public void linkOverridesBelow(ImMethod bridge, ImMethod inherited) {
        bridges.add(new Bridge(bridge, inherited));
    }

    private void linkBridgedOverrides() {
        for (Bridge b : bridges) {
            ImClass bridgeClass = b.bridge().attrClass();
            for (ImMethod sub : b.inherited().getSubMethods()) {
                ImClass subClass = sub.attrClass();
                if (subClass != bridgeClass && subClass.isSubclassOf(bridgeClass)) {
                    b.bridge().getSubMethods().add(sub);
                }
            }
        }
    }

    /** The functions {@link #bridgeFunction} made, by class and by the implementation each one runs. */
    private final Map<ImClass, Map<FuncDef, ImFunction>> bridgeFunctions = new IdentityHashMap<>();

    /**
     * The function {@code c} has of its own to run {@code implementation}, an implementation it inherits
     * ({@code InterfaceTranslator}), made by {@code make} the first time: a class implementing several interfaces with
     * one inherited method gets one function for it.
     */
    public ImFunction bridgeFunction(ImClass c, FuncDef implementation, java.util.function.Supplier<ImFunction> make) {
        return bridgeFunctions.computeIfAbsent(c, k -> new IdentityHashMap<>())
            .computeIfAbsent(implementation, k -> make.get());
    }

    /**
     * Adds the functions of {@link #bridgeFunction} to their classes once every unit is translated. They are made as
     * the interfaces are translated, in the order of the compilation units, and two of them can have one name (the
     * overloads of a method), which the backends tell apart by their order. So they go in by their sort key, which the
     * order of the units does not change.
     */
    private void addBridgeFunctions() {
        for (ImClass c : imProg.getClasses()) {
            Map<FuncDef, ImFunction> functions = bridgeFunctions.get(c);
            if (functions != null) {
                List<ImFunction> sorted = new ArrayList<>(functions.values());
                sortList(sorted);
                c.getFunctions().addAll(sorted);
            }
        }
    }

    public void removeEmptyPackageInits() {
        Set<ImFunction> emptyInitFunctions = new HashSet<>();
        for (ImFunction initFunc : new LinkedHashSet<>(initFuncMap.values())) {
            if (isTrivialInitFunction(initFunc)) {
                emptyInitFunctions.add(initFunc);
            }
        }
        if (emptyInitFunctions.isEmpty()) {
            return;
        }

        Map<ImVar, ImFunction> initFuncRefs = collectInitFuncRefs();
        removeInitCallsFromMain(emptyInitFunctions, initFuncRefs);
        removeInitFuncRefsFromGlobals(emptyInitFunctions);
        imProg.getFunctions().removeIf(emptyInitFunctions::contains);
        initFuncMap.values().removeIf(emptyInitFunctions::contains);
    }

    private boolean isTrivialInitFunction(ImFunction initFunc) {
        if (initFunc.getBody().isEmpty()) {
            return true;
        }
        if (initFunc.getBody().size() != 1) {
            return false;
        }
        ImStmt stmt = initFunc.getBody().get(0);
        if (!(stmt instanceof ImReturn imReturn)) {
            return false;
        }
        ImExprOpt returnValue = imReturn.getReturnValue();
        if (returnValue instanceof ImNoExpr) {
            return true;
        }
        return returnValue instanceof ImBoolVal imBoolVal && imBoolVal.getValB();
    }

    private void removeInitCallsFromMain(Set<ImFunction> emptyInitFunctions, Map<ImVar, ImFunction> initFuncRefs) {
        ImFunction main = getMainFunc();
        if (main == null) {
            return;
        }

        ImFunction native_TriggerAddCondition = getNativeFunc("TriggerAddCondition");
        ImFunction native_Condition = getNativeFunc("Condition");
        ImFunction native_ClearTrigger = getNativeFunc("TriggerClearConditions");

        ImStmts mainBody = main.getBody();
        for (int i = 0; i < mainBody.size(); i++) {
            ImStmt stmt = mainBody.get(i);
            if (stmt instanceof ImFunctionCall call) {
                if (emptyInitFunctions.contains(call.getFunc())) {
                    mainBody.remove(i--);
                    continue;
                }
                if (native_TriggerAddCondition != null && native_Condition != null
                        && call.getFunc() == native_TriggerAddCondition
                        && hasInitCondition(call, native_Condition, emptyInitFunctions, initFuncRefs)) {
                    if (i + 2 < mainBody.size()
                            && mainBody.get(i + 1) instanceof ImIf
                            && isTriggerClear(mainBody.get(i + 2), native_ClearTrigger)) {
                        mainBody.remove(i + 2);
                        mainBody.remove(i + 1);
                        mainBody.remove(i--);
                    }
                }
            }
        }
    }

    private void removeInitFuncRefsFromGlobals(Set<ImFunction> emptyInitFunctions) {
        ImFunction globalInit = getGlobalInitFunc();
        if (globalInit == null) {
            return;
        }
        ImStmts body = globalInit.getBody();
        for (int i = 0; i < body.size(); i++) {
            ImStmt stmt = body.get(i);
            if (!(stmt instanceof ImSet imSet)) {
                continue;
            }
            ImExpr right = imSet.getRight();
            if (right instanceof ImFuncRef imFuncRef && emptyInitFunctions.contains(imFuncRef.getFunc())) {
                body.remove(i--);
            }
        }
    }

    private Map<ImVar, ImFunction> collectInitFuncRefs() {
        ImFunction globalInit = getGlobalInitFunc();
        if (globalInit == null) {
            return Collections.emptyMap();
        }
        Map<ImVar, ImFunction> refs = new HashMap<>();
        ImStmts body = globalInit.getBody();
        for (int i = 0; i < body.size(); i++) {
            ImStmt stmt = body.get(i);
            if (!(stmt instanceof ImSet set)) {
                continue;
            }
            if (!(set.getLeft() instanceof ImVarAccess)) {
                continue;
            }
            if (!(set.getRight() instanceof ImFuncRef)) {
                continue;
            }
            refs.put(((ImVarAccess) set.getLeft()).getVar(), ((ImFuncRef) set.getRight()).getFunc());
        }
        return refs;
    }

    private boolean hasInitCondition(ImFunctionCall call, ImFunction nativeCondition, Set<ImFunction> emptyInitFunctions,
                                     Map<ImVar, ImFunction> initFuncRefs) {
        if (call.getArguments().size() < 2) {
            return false;
        }
        ImExpr conditionExpr = call.getArguments().get(1);
        if (!(conditionExpr instanceof ImFunctionCall conditionCall)) {
            return false;
        }
        if (conditionCall.getFunc() != nativeCondition) {
            return false;
        }
        if (conditionCall.getArguments().size() != 1) {
            return false;
        }
        ImExpr argument = conditionCall.getArguments().get(0);
        if (argument instanceof ImFuncRef funcRef) {
            return emptyInitFunctions.contains(funcRef.getFunc());
        }
        if (argument instanceof ImVarAccess imVarAccess) {
            ImVar var = imVarAccess.getVar();
            ImFunction target = initFuncRefs.get(var);
            return target != null && emptyInitFunctions.contains(target);
        }
        return false;
    }

    private boolean isTriggerClear(ImStmt stmt, ImFunction nativeClearTrigger) {
        if (nativeClearTrigger == null) {
            return false;
        }
        return stmt instanceof ImFunctionCall imFunctionCall && imFunctionCall.getFunc() == nativeClearTrigger;
    }

    /**
     * Number all the compiletime functions and expressions,
     * so that the one with the lowest number can be executed first.
     * <p>
     * Dependendend packages are executed first and inside a package
     * it goes from top to bottom.
     */
    private void calculateCompiletimeOrder() {
        Set<WPackage> visited = new HashSet<>();
        ImmutableCollection<WPackage> packages = wurstProg.attrPackages().values();

        for (WPackage p : packages) {
            calculateCompiletimeOrder_walk(p, visited);
        }
    }

    private void calculateCompiletimeOrder_walk(WPackage p, Set<WPackage> visited) {
        if (!visited.add(p)) {
            return;
        }
        for (WPackage dep : p.attrInitDependencies()) {
            calculateCompiletimeOrder_walk(dep, visited);
        }
        p.accept(new de.peeeq.wurstscript.ast.Element.DefaultVisitor() {
            @Override
            public void visit(FuncDef funcDef) {
                super.visit(funcDef);
                if (funcDef.attrIsCompiletime()) {
                    compiletimeFlags.put(funcDef, new FunctionFlagCompiletime(compiletimeOrderCounter++));
                }
            }

            @Override
            public void visit(ExprFunctionCall fc) {
                super.visit(fc);
                if (fc.getFuncName().equals("compiletime")) {
                    compiletimeExpressionsOrder.put(fc, compiletimeOrderCounter++);
                }
            }
        });
    }


    /**
     * sorting everything is supposed to make the translation deterministic
     */
    private void sortEverything() {
        sortList(imProg.getClasses());
        sortList(imProg.getGlobals());
        sortList(imProg.getFunctions());
        for (ImClass c : imProg.getClasses()) {
            sortList(c.getFields());
            sortList(c.getMethods());
            // The sub-methods of a method are appended by whichever translator reaches an override: the
            // subclasses of a class, the classes implementing an interface and each closure, the last
            // in the order the compilation units are translated in. The backends bind dispatch slots
            // in the order of this list, so it is fixed here rather than by the order of its producers.
            for (ImMethod m : c.getMethods()) {
                sortList(m.getSubMethods());
            }
        }
    }


    private static class ElementWithKey<T> implements Comparable<ElementWithKey<T>> {
        final T element;
        final String key;

        ElementWithKey(T element, String key) {
            this.element = element;
            this.key = key;
        }

        @Override
        public int compareTo(ElementWithKey<T> o) {
            return this.key.compareTo(o.key);
        }
    }

    private final Map<de.peeeq.wurstscript.ast.Element, String> scopePrefixCache = new IdentityHashMap<>();

    private <T extends Element> void sortList(List<T> list) {
        if (list.size() <= 1) {
            return;
        }
        @SuppressWarnings("unchecked")
        ElementWithKey<T>[] elements = new ElementWithKey[list.size()];
        for (int i = 0; i < list.size(); i++) {
            T elem = list.get(i);
            elements[i] = new ElementWithKey<>(elem, getSortKey(elem));
        }
        Arrays.sort(elements);
        list.clear();
        for (ElementWithKey<T> e : elements) {
            list.add(e.element);
        }
    }

    private String getSortKey(Element c) {
        StringBuilder sb = new StringBuilder();
        de.peeeq.wurstscript.ast.Element trace = c.attrTrace();
        String scope = getScopePrefix(trace);
        if (!scope.isEmpty()) {
            sb.append(scope).append("_");
        }
        if (c instanceof ImFunction f) {
            sb.append("fn::").append(f.getName()).append("(");
            for (int i = 0; i < f.getParameters().size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(f.getParameters().get(i).getType());
            }
            sb.append(")->").append(f.getReturnType());
        } else if (c instanceof ImVar v) {
            sb.append("var::").append(v.getName()).append(":").append(v.getType());
        } else if (c instanceof ImClass cls) {
            sb.append("class::").append(cls.getName());
        } else if (c instanceof ImMethod m) {
            sb.append("method::").append(m.getName());
        } else {
            sb.append(c.getClass().getSimpleName());
        }
        if (trace != null) {
            de.peeeq.wurstscript.parser.WPos pos = trace.attrSource();
            if (pos != null) {
                sb.append("@L").append(pos.getLine()).append("C").append(pos.getStartColumn());
            }
        }
        return sb.toString();
    }

    private String getScopePrefix(de.peeeq.wurstscript.ast.Element e) {
        if (e == null) {
            return "";
        }
        String cached = scopePrefixCache.get(e);
        if (cached != null) {
            return cached;
        }
        NamedScope ns = e instanceof NamedScope namedScope ? namedScope : e.attrNearestNamedScope();
        if (ns == null) {
            scopePrefixCache.put(e, "");
            return "";
        }
        de.peeeq.wurstscript.ast.Element parent = ns.getParent();
        String parentScope = parent != null ? getScopePrefix(parent) : "";
        String result = parentScope.isEmpty() ? ns.getName() : parentScope + "_" + ns.getName();
        scopePrefixCache.put(e, result);
        return result;
    }


    /***
     * this phase removes duplicate native declarations
     */
    private void removeDuplicateNatives(ImProg imProg) {
        Map<String, ImFunction> natives = new HashMap<>();
        Map<ImFunction, ImFunction> removed = new HashMap<>();
        ListIterator<ImFunction> it = imProg.getFunctions().listIterator();
        while (it.hasNext()) {
            ImFunction f = it.next();
            if (f.isNative() && natives.containsKey(f.getName())) {
                ImFunction existing = natives.get(f.getName());
                if (!compatibleTypes(f, existing)) {
                    throw new CompileError(f, "Native function definition conflicts with other native function defined in " +
                            existing.attrTrace().attrErrorPos());
                }
                // remove duplicate
                it.remove();
                removed.put(f, existing);
            } else {
                natives.put(f.getName(), f);
            }
        }
        // rewrite removed links
        imProg.accept(new ImProg.DefaultVisitor() {
            public void visit(ImFunctionCall e) {
                super.visit(e);
                if (removed.containsKey(e.getFunc())) {
                    e.setFunc(removed.get(e.getFunc()));
                }
            }

            public void visit(ImFuncRef e) {
                super.visit(e);
                if (removed.containsKey(e.getFunc())) {
                    e.setFunc(removed.get(e.getFunc()));
                }
            }
        });
    }


    /**
     * checks if two functions f and g have compatible types
     */
    private boolean compatibleTypes(ImFunction f, ImFunction g) {
        if (!f.getReturnType().equalsType(g.getReturnType())) {
            return false;
        }
        if (f.getParameters().size() != g.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < f.getParameters().size(); i++) {
            if (!f.getParameters().get(i).getType().equalsType(g.getParameters().get(i).getType())) {
                return false;
            }
        }
        return true;
    }


    private ArrayList<FunctionFlag> flags(FunctionFlag... flags) {
        return Lists.newArrayList(flags);
    }


    private void translateCompilationUnit(CompilationUnit cu) {
        lasttranslatedThing = cu;
        // TODO can we make this smarter? Only translate functions which are actually called...
        for (WPackage p : cu.getPackages()) {
            lasttranslatedThing = p;
            p.imTranslateTLD(this);
        }
        for (JassToplevelDeclaration tld : cu.getJassDecls()) {
            lasttranslatedThing = tld;
            tld.imTranslateTLD(this);
        }
    }


    private void finishInitFunctions() {
        initializationOrder.clear();
        initializationOrder.add(globalInitFunc);
        // init globals, at beginning of main func:
        getMainFunc().getBody().add(0, ImFunctionCall(emptyTrace, globalInitFunc, ImTypeArguments(), ImExprs(), false, CallType.NORMAL));


        for (ImFunction initFunc : initFuncMap.values()) {
            addFunction(initFunc);
        }
        Set<WPackage> calledInitializers = Sets.newLinkedHashSet();

        if (isLuaTarget()) {
            // In Lua mode, xpcall handles error isolation; no trigger handle is needed.
            for (WPackage p : Utils.sortByName(initFuncMap.keySet())) {
                callInitFunc(calledInitializers, p, null);
            }
        } else {
            ImVar initTrigVar = prepareTrigger();

            for (WPackage p : Utils.sortByName(initFuncMap.keySet())) {
                callInitFunc(calledInitializers, p, initTrigVar);
            }

            ImFunction native_DestroyTrigger = getNativeFunc("DestroyTrigger");
            if (native_DestroyTrigger != null) {
                getMainFunc().getBody().add(JassIm.ImFunctionCall(emptyTrace, native_DestroyTrigger, ImTypeArguments(),
                        JassIm.ImExprs(JassIm.ImVarAccess(initTrigVar)), false, CallType.NORMAL));
            }
        }
    }

    @NotNull
    private ImVar prepareTrigger() {
        ImVar initTrigVar = JassIm.ImVar(emptyTrace, JassIm.ImSimpleType("trigger"), "initTrig", false);
        getMainFunc().getLocals().add(initTrigVar);

        // initTrigVar = CreateTrigger()
        ImFunction createTrigger = getNativeFunc("CreateTrigger");
        if (createTrigger != null) {
            getMainFunc().getBody().add(ImSet(getMainFunc().getTrace(), ImVarAccess(initTrigVar), JassIm.ImFunctionCall(getMainFunc().getTrace(), getNativeFunc("CreateTrigger"), ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL)));
        }
        return initTrigVar;
    }


    private ImFunction getNativeFunc(String funcName) {
        ImmutableCollection<FuncLink> wurstFunc = wurstProg.lookupFuncs(funcName);
        if (wurstFunc.isEmpty()) {
            return null;
        }
        return getFuncFor(Utils.getFirst(wurstFunc).getDef());
    }

private void callInitFunc(Set<WPackage> calledInitializers, WPackage p, @Nullable ImVar initTrigVar) {
        Preconditions.checkNotNull(p);
        if (calledInitializers.contains(p)) {
            return;
        }
        calledInitializers.add(p);
        // first initialize all packages imported by this package:
        for (WPackage dep : p.attrInitDependencies()) {
            callInitFunc(calledInitializers, dep, initTrigVar);
        }
        ImFunction initFunc = initFuncMap.get(p);
        if (initFunc == null) {
            return;
        }
        if (initFunc.getBody().size() == 0) {
            return;
        }
        initializationOrder.add(initFunc);
        if (isLuaTarget()) {
            // In Lua mode, xpcall replaces TriggerEvaluate for error isolation without WC3 handle overhead.
            // Record the init function so the Lua translator can wrap it with xpcall.
            luaInitFunctions.put(initFunc, p.getName());
            getMainFunc().getBody().add(ImFunctionCall(initFunc.getTrace(), initFunc, ImTypeArguments(), ImExprs(), false, CallType.NORMAL));
            return;
        }

        boolean successful = createInitFuncCall(p, initTrigVar, initFunc);

        if (!successful) {
            getMainFunc().getBody().add(ImFunctionCall(initFunc.getTrace(), initFunc, ImTypeArguments(), ImExprs(), false, CallType.NORMAL));
        }
    }


    private boolean createInitFuncCall(WPackage p, ImVar initTrigVar, ImFunction initFunc) {
        ImStmts mainBody = getMainFunc().getBody();

        ImFunction native_ClearTrigger = getNativeFunc("TriggerClearConditions");
        ImFunction native_TriggerAddCondition = getNativeFunc("TriggerAddCondition");
        ImFunction native_Condition = getNativeFunc("Condition");
        ImFunction native_TriggerEvaluate = getNativeFunc("TriggerEvaluate");
        ImFunction native_DisplayTimedTextToPlayer = getNativeFunc("DisplayTimedTextToPlayer");
        ImFunction native_GetLocalPlayer = getNativeFunc("GetLocalPlayer");

        if (native_ClearTrigger == null
                || native_TriggerAddCondition == null
                || native_Condition == null
                || native_TriggerEvaluate == null
                || native_DisplayTimedTextToPlayer == null
                || native_GetLocalPlayer == null
        ) {
            return false;
        }


        // rewrite init func to return boolean true:
        initFunc.setReturnType(WurstTypeBool.instance().imTranslateType(this));
        initFunc.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImReturn imReturn) {
                super.visit(imReturn);
                imReturn.setReturnValue(JassIm.ImBoolVal(true));
            }
        });
        de.peeeq.wurstscript.ast.Element trace = initFunc.getTrace();
        initFunc.getBody().add(JassIm.ImReturn(trace, JassIm.ImBoolVal(true)));


        // TriggerAddCondition(initTrigVar, Condition(function myInit))
        mainBody.add(ImFunctionCall(trace, native_TriggerAddCondition, ImTypeArguments(), JassIm.ImExprs(
                JassIm.ImVarAccess(initTrigVar),
                ImFunctionCall(trace, native_Condition, ImTypeArguments(), JassIm.ImExprs(
                        JassIm.ImFuncRef(trace, initFunc)), false, CallType.NORMAL)
        ), true, CallType.NORMAL));
        // if not TriggerEvaluate(initTrigVar) ...
        mainBody.add(JassIm.ImIf(trace,
                JassIm.ImOperatorCall(WurstOperator.NOT, JassIm.ImExprs(
                        ImFunctionCall(trace, native_TriggerEvaluate, ImTypeArguments(), JassIm.ImExprs(JassIm.ImVarAccess(initTrigVar)), false, CallType.NORMAL)
                )),
                // then: DisplayTimedTextToPlayer(GetLocalPlayer(), 0., 0., 45., "Could not initialize package")
                JassIm.ImStmts(
                    imError(trace, JassIm.ImStringVal("Could not initialize package " + p.getName() + "."))
                ),
                // else:
                JassIm.ImStmts()));
        mainBody.add(ImFunctionCall(trace, native_ClearTrigger, ImTypeArguments(), JassIm.ImExprs(JassIm.ImVarAccess(initTrigVar)), false, CallType.NORMAL));
        return true;
    }

    private void addFunction(ImFunction f, StructureDef s) {
        ImClass c = getClassFor(s.attrNearestClassOrInterface());
        c.getFunctions().add(f);
    }

    private void addFunction(ImFunction f, TranslatedToImFunction funcDef) {
        ImClass classForFunc = getClassForFunc(funcDef);
        if (classForFunc != null) {
            classForFunc.getFunctions().add(f);
        } else {
            addFunction(f);
        }
    }

    private void addFunction(ImFunction f) {
        imProg.getFunctions().add(f);
    }

    public void addGlobal(ImVar v) {
        imProg.getGlobals().add(v);
    }


    public void addGlobalInitalizer(ImVar v, PackageOrGlobal packageOrGlobal, VarInitialization initialExpr) {
        if (initialExpr instanceof NoExpr) {
            // nothing to initialize
            return;
        }


        ImFunction f;
        if (packageOrGlobal instanceof WPackage p) {
            f = getInitFuncFor(p);
        } else {
            f = globalInitFunc;
        }
        de.peeeq.wurstscript.ast.Element trace = packageOrGlobal == null ? emptyTrace : packageOrGlobal;
        if (initialExpr instanceof Expr expr) {
            ImExpr translated = expr.imTranslateExpr(this, f);
            ImSet imSet = ImSet(trace, ImVarAccess(v), translated);
            if (!v.getIsBJ()) {
                // add init statement for non-bj vars
                // bj-vars are already initalized by blizzard
                f.getBody().add(imSet);
            }
            imProg.getGlobalInits().put(v, Collections.singletonList(imSet));
        } else if (initialExpr instanceof ArrayInitializer arInit) {
            List<ImExpr> translatedExprs = new ArrayList<>();
            for (Expr expr : arInit.getValues()) {
                ImExpr imExpr = expr.imTranslateExpr(this, f);
                translatedExprs.add(imExpr);
            }
            List<ImSet> imSets = new ArrayList<>();
            for (int i = 0; i < arInit.getValues().size(); i++) {
                ImExpr translated = translatedExprs.get(i);
                ImSet imSet = ImSet(trace, ImVarArrayAccess(trace, v, ImExprs(JassIm.ImIntVal(i))), translated);
                imSets.add(imSet);
            }
            f.getBody().addAll(imSets);
            // add list of init-values to translatedExprs
            imProg.getGlobalInits().put(v, imSets);
        }
    }

    public void addGlobalWithInitalizer(ImVar g, ImExpr initial) {
        imProg.getGlobals().add(g);
        ImSet imSet = ImSet(g.getTrace(), ImVarAccess(g), initial);
        getGlobalInitFunc().getBody().add(imSet);
        imProg.getGlobalInits().put(g, Collections.singletonList(imSet));
    }


    public ImExpr getDefaultValueForJassType(ImType type) {
        if (type instanceof ImSimpleType imSimpleType) {
            return ImHelper.defaultValueForType(imSimpleType);
        } else if (type instanceof ImAnyType) {
            return JassIm.ImIntVal(0);
        } else if (type instanceof ImTupleType imTupleType) {
            return getDefaultValueForJassType(imTupleType.getTypes().get(0));
        } else {
            throw new IllegalArgumentException("could not get default value for type " + type);
        }
    }

    public GetAForB<StructureDef, ImFunction> destroyFunc = new GetAForB<StructureDef, ImFunction>() {

        @Override
        public ImFunction initFor(StructureDef classDef) {
            ImVars params = ImVars(JassIm.ImVar(classDef, selfType(classDef), "this", false));

            ImFunction f = ImFunction(classDef.getOnDestroy(), "destroy" + classDef.getName(), ImTypeVars(), params, TypesHelper.imVoid(), ImVars(), ImStmts(), flags());
            addFunction(f, classDef);
            return f;
        }
    };

    public GetAForB<StructureDef, ImMethod> destroyMethod = new GetAForB<StructureDef, ImMethod>() {

        @Override
        public ImMethod initFor(StructureDef classDef) {
            ImFunction impl = destroyFunc.getFor(classDef);
            ImMethod m = JassIm.ImMethod(classDef, selfType(classDef), "destroy" + classDef.getName(),
                    impl, Lists.newArrayList(), Lists.newArrayList(), "", false);
            return m;
        }
    };

    private ImType selfType(TranslatedToImFunction f) {
        return f.match(new TranslatedToImFunction.Matcher<ImType>() {
            @Override
            public ImType case_FuncDef(FuncDef f) {
                return selfType(f);
            }

            @Override
            public ImType case_ConstructorDef(ConstructorDef f) {
                return selfType(f.attrNearestClassOrInterface());
            }

            @Override
            public ImType case_NativeFunc(NativeFunc f) {
                throw new CompileError(f, "Cannot use 'this' here.");
            }

            @Override
            public ImType case_OnDestroyDef(OnDestroyDef f) {
                return selfType(f.attrNearestClassOrInterface());
            }

            @Override
            public ImType case_TupleDef(TupleDef f) {
                throw new CompileError(f, "Cannot use 'this' here.");
            }

            @Override
            public ImType case_ExprClosure(ExprClosure f) {
                return selfType(getClassForClosure(f));
            }

            @Override
            public ImType case_InitBlock(InitBlock f) {
                throw new CompileError(f, "Cannot use 'this' here.");
            }

            @Override
            public ImType case_ExtensionFuncDef(ExtensionFuncDef f) {
                return f.getExtendedType().attrTyp().imTranslateType(ImTranslator.this);
            }
        });
    }

    private ImClassType selfType(FuncDef f) {
        return selfType(f.attrNearestClassOrInterface());
    }

    public ImClassType selfType(StructureDef classDef) {
        ImClass imClass = getClassFor(classDef.attrNearestClassOrInterface());
        return selfType(imClass);
    }

    public ImClassType selfType(ImClass imClass) {
        ImTypeArguments typeArgs = JassIm.ImTypeArguments();
        for (ImTypeVar tv : imClass.getTypeVariables()) {
            TypeParamDef tpd = typeVariableReverse.get(tv);

            // If this ImTypeVar corresponds to an owner TypeParamDef (captured case),
            // resolve it through context so owner context uses owner vars, Iterator context uses captured vars.
            ImTypeVar tvForContext = (tpd != null) ? getTypeVar(tpd) : tv;

            typeArgs.add(JassIm.ImTypeArgument(JassIm.ImTypeVarRef(tvForContext), Collections.emptyMap()));
        }
        return JassIm.ImClassType(imClass, typeArgs);
    }

    public GetAForB<ImClass, ImFunction> allocFunc = new GetAForB<ImClass, ImFunction>() {

        @Override
        public ImFunction initFor(ImClass c) {

            return ImFunction(c.getTrace(), "alloc_" + c.getName(), ImTypeVars(), JassIm.ImVars(), TypesHelper.imInt(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        }

    };

    public GetAForB<ImClass, ImFunction> deallocFunc = new GetAForB<ImClass, ImFunction>() {

        @Override
        public ImFunction initFor(ImClass c) {

            return ImFunction(c.getTrace(), "dealloc_" + c.getName(), ImTypeVars(), JassIm.ImVars(JassIm.ImVar(c.getTrace(), TypesHelper.imInt(), "obj", false)), TypesHelper.imVoid(), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        }

    };

    private final Map<ImTypeVar, TypeParamDef> typeVariableReverse = new HashMap<>();

    private final GetAForB<TypeParamDef, ImTypeVar> typeVariable = new GetAForB<TypeParamDef, ImTypeVar>() {

        @Override
        public ImTypeVar initFor(TypeParamDef a) {
            ImTypeVar v = JassIm.ImTypeVar(a.getName());
            typeVariableReverse.put(v, a);
            return v;
        }
    };


    public ImFunction getFuncFor(TranslatedToImFunction funcDef) {
        if (functionMap.containsKey(funcDef)) {
            return functionMap.get(funcDef);
        }
        String name = getNameFor(funcDef);
        List<FunctionFlag> flags = flags();
        if (funcDef instanceof NativeFunc) {
            flags.add(IS_NATIVE);
        }
        if (isBJ(funcDef.getSource())) {
            flags.add(IS_BJ);
        }
        if (isExtern(funcDef)) {
            flags.add(FunctionFlagEnum.IS_EXTERN);
        }
        if (funcDef instanceof FuncDef funcDef2) {
            if (funcDef2.attrIsCompiletime()) {
                FunctionFlagCompiletime flag = compiletimeFlags.get(funcDef);
                if (flag == null) {
                    throw new CompileError(funcDef.getSource(), "Compiletime flag not supported here.");
                }
                flags.add(flag);
            }
            if (funcDef2.attrHasAnnotation("compiletimenative")) {
                flags.add(FunctionFlagEnum.IS_COMPILETIME_NATIVE);
            }
            if (funcDef2.attrHasAnnotation("test")) {
                flags.add(IS_TEST);
            }
        }

        // Check if last parameter is vararg
        if (funcDef instanceof AstElementWithParameters astElementWithParameters) {
            WParameters params = astElementWithParameters.getParameters();
            if (params.size() >= 1 && params.get(params.size() - 1).attrIsVararg()) {
                flags.add(IS_VARARG);
            }
        }


        if (funcDef instanceof HasModifier awm) {
            for (Modifier m : awm.getModifiers()) {
                if (m instanceof Annotation annotation) {
                    flags.add(new FunctionFlagAnnotation(annotation.getAnnotationType()));
                    if (NamePreservation.isPreserveAnnotation(annotation.getAnnotationType())) {
                        flags.add(PRESERVE_NAME);
                    }
                }
            }
        }

        ImTypeVars typeVars = collectTypeVarsForFunction(funcDef);
        ImFunction f = ImFunction(funcDef, name, typeVars, ImVars(), ImVoid(), ImVars(), ImStmts(), flags);
        Map<TypeParamDef, ImTypeVar> ov = getTypeVarOverridesForFunction(f);
        pushTypeVarOverrides(ov);
        try {
            funcDef.imCreateFuncSkeleton(this, f);
        } finally {
            popTypeVarOverrides(ov);
        }

        addFunction(f, funcDef);
        functionMap.put(funcDef, f);
        return f;
    }

    private ImClass getClassForFunc(TranslatedToImFunction funcDef) {
        if (funcDef == null) {
            return null;
        }
        return funcDef.match(new TranslatedToImFunction.Matcher<ImClass>() {
            @Override
            public ImClass case_TupleDef(TupleDef tupleDef) {
                return null;
            }

            @Override
            public ImClass case_FuncDef(FuncDef funcDef) {
                if (funcDef.attrIsDynamicClassMember()) {
                    return getClassFor(funcDef.attrNearestClassOrInterface());
                }
                return null;
            }

            @Override
            public ImClass case_NativeFunc(NativeFunc nativeFunc) {
                return null;
            }

            @Override
            public ImClass case_OnDestroyDef(OnDestroyDef funcDef) {
                return getClassFor(funcDef.attrNearestClassOrInterface());
            }

            @Override
            public ImClass case_InitBlock(InitBlock initBlock) {
                return null;
            }

            @Override
            public ImClass case_ExtensionFuncDef(ExtensionFuncDef extensionFuncDef) {
                return null;
            }

            @Override
            public ImClass case_ConstructorDef(ConstructorDef funcDef) {
                return getClassFor(funcDef.attrNearestClassOrInterface());
            }

            @Override
            public ImClass case_ExprClosure(ExprClosure exprClosure) {
                return null;
            }
        });
    }

    private ImTypeVars collectTypeVarsForFunction(TranslatedToImFunction funcDef) {
        ImTypeVars typeVars = ImTypeVars();
        funcDef.match(new TranslatedToImFunction.MatcherVoid() {
            @Override
            public void case_FuncDef(FuncDef funcDef) {
                if (funcDef.attrIsStatic()) {
                    ClassOrInterface owner = funcDef.attrNearestClassOrInterface();
                    if (owner != null) {
                        handleTypeParameters(owner.getTypeParameters());
                    }
                }
                handleTypeParameters(funcDef.getTypeParameters());
            }


            private void handleTypeParameters(TypeParamDefs tps) {
                for (TypeParamDef tp : tps) {
                    handleTypeParameter(tp);
                }
            }

            private void handleTypeParameter(TypeParamDef tp) {
                if (tp.getTypeParamConstraints() instanceof TypeExprList) {
                    ImTypeVar v = JassIm.ImTypeVar(uniqueTypeVarName(typeVars, tp));
                    typeVariableReverse.put(v, tp);
                    typeVars.add(v);
                }
            }

            @Override
            public void case_ConstructorDef(ConstructorDef constructorDef) {
            }

            @Override
            public void case_NativeFunc(NativeFunc nativeFunc) {
            }

            @Override
            public void case_OnDestroyDef(OnDestroyDef onDestroyDef) {
            }

            @Override
            public void case_TupleDef(TupleDef tupleDef) {
            }

            @Override
            public void case_ExprClosure(ExprClosure exprClosure) {
                // TODO where to set closure parameters?
            }

            @Override
            public void case_InitBlock(InitBlock initBlock) {

            }

            @Override
            public void case_ExtensionFuncDef(ExtensionFuncDef funcDef) {
                handleTypeParameters(funcDef.getTypeParameters());
            }
        });
        return typeVars;
    }


    private boolean isExtern(TranslatedToImFunction funcDef) {
        if (funcDef instanceof HasModifier f) {
            for (Modifier m : f.getModifiers()) {
                if (m instanceof Annotation a) {
                    if (a.getAnnotationType().equals("@extern")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private static final String BJ1 = "blizzard.j";
    private static final String BJ2 = "common.j";

    private boolean isBJ(WPos source) {
        String path = source.getFile(); // no lowercasing
        int n = path.length();

        return (n >= BJ1.length() && path.regionMatches(true, n - BJ1.length(), BJ1, 0, BJ1.length()))
            || (n >= BJ2.length() && path.regionMatches(true, n - BJ2.length(), BJ2, 0, BJ2.length()));
    }

    public ImFunction getInitFuncFor(WPackage p) {
        // TODO more precise trace
        return initFuncMap.computeIfAbsent(p, p1 -> ImFunction(p1, "init_" + p1.getName(), ImTypeVars(), ImVars(), ImVoid(), ImVars(), ImStmts(), flags()));
    }

    /**
     * returns a suitable name for the given element
     * the returned name is a valid jass identifier
     */
    public String getNameFor(de.peeeq.wurstscript.ast.Element e) {
        if (e instanceof FuncDef f) {
            if (f.attrNearestStructureDef() != null) {
                return getNameFor(f.attrNearestStructureDef()) + "_" + f.getName();
            }
        } else if (e instanceof ExtensionFuncDef f) {
            return getNameFor(f.getExtendedType()) + "_" + f.getName();
        } else if (e instanceof TypeExprSimple t) {
            return t.getTypeName();
        } else if (e instanceof TypeExprThis) {
            return "thistype";
        } else if (e instanceof TypeExprArray t) {
            return getNameFor(t.getBase()) + "Array";
        } else if (e instanceof ModuleInstanciation mi) {
            return getNameFor(mi.getParent().attrNearestNamedScope()) + "_" + mi.getName();
        }


        if (e instanceof AstElementWithNameId wn) {
            return wn.getNameId().getName();
        } else if (e instanceof ConstructorDef) {
            return "new_" + e.attrNearestClassDef().getName();
        } else if (e instanceof OnDestroyDef) {
            return "ondestroy_" + e.attrNearestClassDef().getName();
        } else if (e instanceof ExprClosure) {
            return e.attrNearestNamedScope().getName() + "_closure";
        }
        throw new RuntimeException("unhandled case: " + e.getClass().getName());
    }

    public ImVar getThisVar(TranslatedToImFunction f) {
        if (f instanceof OnDestroyDef) {
            // special case for onDestroy defs
            // TODO also special case for constructors needed?
            OnDestroyDef od = (OnDestroyDef) f;
            if (od.getParent() instanceof ModuleInstanciation) {
                ModuleInstanciation mi = (ModuleInstanciation) od.getParent();
                ClassDef c = mi.attrNearestClassDef();
                f = c.getOnDestroy();
            }
        }
        if (thisVarMap.containsKey(f)) {
            return thisVarMap.get(f);
        }
        ImVar v = JassIm.ImVar(f, selfType(f), "this", false);
        thisVarMap.put(f, v);
        return v;
    }

    public ImVar getThisVar(ImFunction f, ExprThis e) {
        return getThisVarForNode(f, e);
    }

    public ImVar getThisVar(ImFunction f, ExprSuper e) {
        return getThisVarForNode(f, e);
    }

    private ImVar getThisVarForNode(ImFunction f, de.peeeq.wurstscript.ast.Element node1) {
        de.peeeq.wurstscript.ast.Element node = node1;
        while (node != null) {
            if (node instanceof TranslatedToImFunction && !(node instanceof ExprClosure)) {
                return getThisVar((TranslatedToImFunction) node);
            }
            node = node.getParent();
        }
        if (f.getParameters().isEmpty()) {
            throw new CompileError(node1.attrSource(), "Could not get 'this'.");
        }
        return f.getParameters().get(0);
    }


    public int getTupleIndex(TupleDef tupleDef, VarDef parameter) {
        int i = 0;
        for (WParameter p : tupleDef.getParameters()) {
            if (p == parameter) {
                return i;
            }
            i++;
        }
        throw new Error("");
    }


    public ImVar getVarFor(VarDef varDef) {
        ImVar v = varMap.get(varDef);
        if (v == null) {
            Map<TypeParamDef, ImTypeVar> ov = getOwnerTypeVarOverridesForStaticClassVar(varDef);
            pushTypeVarOverrides(ov);
            ImType type;
            try {
                type = varDef.attrTyp().imTranslateType(this);
            } finally {
                popTypeVarOverrides(ov);
            }
            String name = varDef.getName();
            if (isNamedScopeVar(varDef)) {
                name = getNameFor(varDef.attrNearestNamedScope()) + "_" + name;
            }
            boolean isBj = isBJ(varDef.getSource());
            v = JassIm.ImVar(varDef, type, name, isBj);
            varMap.put(varDef, v);
        }
        return v;
    }

    private Map<TypeParamDef, ImTypeVar> getOwnerTypeVarOverridesForStaticClassVar(VarDef varDef) {
        if (!(varDef instanceof GlobalVarDef) || !varDef.attrIsStatic()) {
            return Collections.emptyMap();
        }
        ClassOrInterface owner = varDef.attrNearestClassOrInterface();
        if (owner == null) {
            return Collections.emptyMap();
        }
        Map<TypeParamDef, ImTypeVar> result = new IdentityHashMap<>();
        if (owner instanceof AstElementWithTypeParameters astElementWithTypeParameters) {
            for (TypeParamDef tp : astElementWithTypeParameters.getTypeParameters()) {
                if (tp.getTypeParamConstraints() instanceof TypeExprList) {
                    result.put(tp, typeVariable.getFor(tp));
                }
            }
        }
        return result;
    }

    private boolean isNamedScopeVar(VarDef varDef) {
        if (varDef.getParent() == null) {
            return false;
        }
        return varDef.getParent().getParent() instanceof NamedScope;
    }

    public WurstModel getWurstProg() {
        return wurstProg;
    }

    public ImProg imProg() {
        return imProg;
    }

    public boolean isTranslated(WPackage pack) {
        return translatedPackages.contains(pack);
    }

    public void setTranslated(WPackage pack) {
        translatedPackages.add(pack);
    }

    public boolean isTranslated(ClassDef c) {
        return translatedClasses.contains(c);
    }

    public void setTranslated(ClassDef c) {
        translatedClasses.add(c);
    }

    public List<ImStmt> translateStatements(ImFunction f, List<WStatement> statements) {
        List<ImStmt> result = Lists.newArrayList();
        Map<TypeParamDef, ImTypeVar> ov = getTypeVarOverridesForFunction(f);
        pushTypeVarOverrides(ov);
        try {
            for (WStatement s : statements) {
                lasttranslatedThing = s;
                ImStmt translated = s.imTranslateStmt(this, f);
                result.add(translated);
            }
        } finally {
            popTypeVarOverrides(ov);
        }
        return result;
    }

    public void setMainFunc(ImFunction f) {
        if (mainFunc != null) {
            throw new Error("mainFunction already set");
        }
        mainFunc = f;
    }

    public void setConfigFunc(ImFunction f) {
        if (configFunc != null) {
            throw new Error("configFunction already set");
        }
        configFunc = f;
    }

    public Multimap<ImFunction, ImFunction> getCalledFunctions() {
        if (callRelations == null) {
            calculateCallRelationsAndReadVariables();
        }
        return callRelations;
    }



    public void calculateCallRelationsAndUsedVariables() {
        calculateCallRelationsAndVariables(true);
    }

    public void calculateCallRelationsAndReadVariables() {
        calculateCallRelationsAndVariables(false);
    }

    private void calculateCallRelationsAndVariables(boolean includeUsedVariables) {
        // estimate sizes to reduce rehashing
        final int funcEstimate = Math.max(16, imProg.getFunctions().size());
        final int varEstimate  = Math.max(32, imProg.getGlobals().size());

        callRelations = com.google.common.collect.LinkedHashMultimap.create(); // keep Guava type externally

        usedFunctions = new ReferenceOpenHashSet<>(funcEstimate);
        usedVariables = includeUsedVariables ? new ObjectOpenHashSet<>(varEstimate) : null;
        readVariables = new ObjectOpenHashSet<>(varEstimate);
        readCounts = null;
        readByBjInitialisers = new ObjectOpenHashSet<>();
        retiredFacts = functionFacts == null ? null : new IdentityHashMap<>();

        final ImFunction main = getMainFunc();
        if (main != null) calculateCallRelations(main, includeUsedVariables);

        final ImFunction conf = getConfFunc();
        if (conf != null && conf != main) calculateCallRelations(conf, includeUsedVariables);

        // Preserved functions are externally visible entry points even when no Wurst code calls
        // them. Keep their bodies and everything they call reachable for both backends.
        for (ImFunction function : ImHelper.calculateFunctionsOfProg(imProg)) {
            if (NamePreservation.isPreserved(function)) {
                calculateCallRelations(function, includeUsedVariables);
            }
        }

        // Mark externally visible globals as read so they survive garbage collection.
        for (ImVar global : imProg.getGlobals()) {
            if (NamePreservation.isPreserved(global)) readVariables.add(global);
        }

        // The game initialises common.j and blizzard.j globals itself, so their initialisers are in no
        // function body, and the interpreters evaluate them when the global is first read. A read one
        // keeps what its initialiser reads: bj_DEGTORAD keeps bj_PI.
        ArrayDeque<ImVar> bjGlobals = new ArrayDeque<>(readVariables);
        while (!bjGlobals.isEmpty()) {
            ImVar global = bjGlobals.removeLast();
            if (!global.getIsBJ()) {
                continue;
            }
            for (ImSet init : imProg.getGlobalInits().getOrDefault(global, Collections.emptyList())) {
                for (ImVar read : UsedVariables.calculateReadVars(init.getRight())) {
                    if (includeUsedVariables) {
                        usedVariables.add(read);
                    }
                    readByBjInitialisers.add(read);
                    if (readVariables.add(read)) {
                        bjGlobals.add(read);
                    }
                }
            }
        }
    }

    private void calculateCallRelations(ImFunction rootFunction, boolean includeUsedVariables) {
        // nothing to do
        if (rootFunction == null) return;

        // if already processed, skip entirely
        if (usedFunctions.contains(rootFunction)) return;

        final ArrayDeque<ImFunction> work = new ArrayDeque<>();
        work.add(rootFunction);

        while (!work.isEmpty()) {
            final ImFunction f = work.removeLast(); // LIFO (DFS); change to removeFirst() for BFS

            if (!usedFunctions.add(f)) {
                // was already processed; skip
                continue;
            }

            // Only computed once per function thanks to usedFunctions.add() gate
            if (includeUsedVariables) {
                usedVariables.addAll(usedVariablesOf(f));
            }
            final FunctionFacts facts = relationFactsOf(f);
            readVariables.addAll(facts.readVariables);

            final Set<ImFunction> called = facts.usedFunctions;
            // Avoid streams/alloc; avoid pushing functions we've already seen
            for (ImFunction g : called) {
                if (g == null) continue;
                if (g != f) { // ignore self-calls in relation
                    callRelations.put(f, g);
                }
                if (!usedFunctions.contains(g)) {
                    work.add(g);
                }
            }
        }
    }

    /**
     * What the analysis read out of the body of a function, and its assignments, kept while garbage is removed.
     * <p>
     * The removal analyses the program once per round, and each of the three questions about a function is a walk
     * over its whole body, which is not cached. A round changes a few functions only, and says which
     * ({@link #functionChanged}); the rest keep their answers until {@link #forgetFunctionFacts}.
     */
    private @Nullable Map<ImFunction, FunctionFacts> functionFacts;

    private static final class FunctionFacts {
        @Nullable Set<ImVar> usedVariables;
        @Nullable Set<ImVar> readVariables;
        @Nullable Set<ImFunction> usedFunctions;
        @Nullable List<ImSet> sets;
    }

    /** From now on the analysis keeps what it learns of a function, until it is told that the function changed. */
    public void rememberFunctionFacts() {
        functionFacts = new IdentityHashMap<>();
    }

    public void forgetFunctionFacts() {
        functionFacts = null;
        retiredFacts = null;
        readCounts = null;
    }

    /**
     * The facts of the functions which changed since the last call relation analysis, as that analysis saw them.
     * {@link #refreshReadVariables} compares them with what the functions read now.
     */
    private @Nullable Map<ImFunction, FunctionFacts> retiredFacts;
    /** For each variable, the number of reachable functions which read it, once {@link #refreshReadVariables} needs it. */
    private @Nullable Object2IntOpenHashMap<ImVar> readCounts;
    /** The variables which the initialisers of the common.j and blizzard.j globals which are read, read. */
    private @Nullable Set<ImVar> readByBjInitialisers;

    /** The body of the function was changed: the analysis has to look at it again. */
    public void functionChanged(ImFunction function) {
        if (functionFacts != null) {
            FunctionFacts old = functionFacts.remove(function);
            if (old != null && retiredFacts != null) {
                retiredFacts.putIfAbsent(function, old);
            }
        }
    }

    /**
     * Brings the read variables of the last call relation analysis in line with functions which changed, without
     * analysing the program again. A function which still calls the same functions leaves the reachable functions as
     * they are, and what the program reads is what the functions read: a variable is
     * read while one reachable function reads it, the preserved globals are read, and so are those which the
     * initialisers of the common.j and blizzard.j globals read.
     *
     * @return the variables which nothing reads now, or null when the program has to be analysed again: a function
     * lost a call (functions it called may be unreachable now), a variable of common.j or blizzard.j is not read any
     * more (so what its initialiser read may not be read any more), or the analysis does not know what a function
     * read before. The call relation is not kept up to date.
     */
    public @Nullable List<ImVar> refreshReadVariables(Collection<ImFunction> changed) {
        if (functionFacts == null || retiredFacts == null || usedFunctions == null || readVariables == null
            || readByBjInitialisers == null) {
            return null;
        }
        if (readCounts == null) {
            Object2IntOpenHashMap<ImVar> counts = new Object2IntOpenHashMap<>(readVariables.size());
            for (ImFunction function : usedFunctions) {
                FunctionFacts facts = retiredFacts.get(function);
                if (facts == null) {
                    facts = functionFacts.get(function);
                }
                if (facts == null || facts.readVariables == null) {
                    return null;
                }
                for (ImVar v : facts.readVariables) {
                    counts.addTo(v, 1);
                }
            }
            readCounts = counts;
        }
        // A function reads fewer variables than before, and may read some it did not: the effects an assignment leaves
        // are flattened, which saves arguments in variables of its own.
        List<ImVar> lostReads = new ArrayList<>();
        for (ImFunction function : changed) {
            FunctionFacts before = retiredFacts.remove(function);
            if (before == null || before.usedFunctions == null || before.readVariables == null) {
                return null;
            }
            FunctionFacts now = relationFactsOf(function);
            if (!now.usedFunctions.equals(before.usedFunctions)) {
                return null;
            }
            for (ImVar v : before.readVariables) {
                if (!now.readVariables.contains(v)) {
                    readCounts.addTo(v, -1);
                    lostReads.add(v);
                }
            }
            for (ImVar v : now.readVariables) {
                if (!before.readVariables.contains(v)) {
                    readCounts.addTo(v, 1);
                    readVariables.add(v);
                }
            }
        }
        // what no function reads now, once every function is accounted for
        List<ImVar> candidates = new ArrayList<>();
        for (ImVar v : lostReads) {
            if (readCounts.getInt(v) == 0) {
                candidates.add(v);
            }
        }
        List<ImVar> unread = new ArrayList<>();
        for (ImVar v : candidates) {
            if (isGlobalOf(v) && NamePreservation.isPreserved(v)) {
                continue;
            }
            if (v.getIsBJ()) {
                return null;
            }
            if (readByBjInitialisers.contains(v)) {
                continue;
            }
            if (readVariables.remove(v)) {
                unread.add(v);
            }
        }
        return unread;
    }

    private boolean isGlobalOf(ImVar v) {
        Element parent = v.getParent();
        return parent != null && parent.getParent() instanceof ImProg;
    }

    private @Nullable FunctionFacts factsOf(ImFunction function) {
        return functionFacts == null ? null : functionFacts.computeIfAbsent(function, f -> new FunctionFacts());
    }

    private Set<ImVar> usedVariablesOf(ImFunction function) {
        FunctionFacts facts = factsOf(function);
        if (facts == null) {
            return function.calcUsedVariables();
        }
        if (facts.usedVariables == null) {
            facts.usedVariables = function.calcUsedVariables();
        }
        return facts.usedVariables;
    }

    /**
     * The functions a function uses and the variables it reads, from one walk over its body. While facts are kept
     * the assignments of the function come out of the same walk, which the removal asks for next.
     */
    private FunctionFacts relationFactsOf(ImFunction function) {
        FunctionFacts facts = factsOf(function);
        boolean kept = facts != null;
        if (facts == null) {
            facts = new FunctionFacts();
        }
        if (facts.usedFunctions == null || facts.readVariables == null) {
            FunctionFactsCollector collector = FunctionFactsCollector.collect(function, kept && facts.sets == null);
            if (isUnitTestMode) {
                assertSameAsSeparateWalks(function, collector);
            }
            facts.usedFunctions = collector.usedFunctions;
            facts.readVariables = collector.readVariables;
            if (collector.sets != null) {
                facts.sets = collector.sets;
            }
        }
        return facts;
    }

    /** The assignments in a function, the ones inside another assignment before it. */
    public List<ImSet> setStatementsOf(ImFunction function) {
        FunctionFacts facts = factsOf(function);
        if (facts != null && facts.sets != null) {
            return facts.sets;
        }
        List<ImSet> sets = referenceSetStatementsOf(function);
        if (facts != null) {
            facts.sets = sets;
        }
        return sets;
    }

    private static List<ImSet> referenceSetStatementsOf(ImFunction function) {
        List<ImSet> sets = new ArrayList<>();
        function.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImSet e) {
                super.visit(e);
                sets.add(e);
            }
        });
        return sets;
    }

    /**
     * What the single walk answers has to be what the separate walks answer, in the same order: unit tests compare
     * every function the analysis meets, with the walks which {@link FunctionFactsCollector} replaces.
     */
    private static void assertSameAsSeparateWalks(ImFunction function, FunctionFactsCollector collector) {
        List<ImFunction> expectedUsed = new ArrayList<>(function.calcUsedFunctions());
        List<ImFunction> actualUsed = new ArrayList<>(collector.usedFunctions);
        assertSameElements("functions used by " + function.getName(), expectedUsed, actualUsed);
        assertSameElements("variables read by " + function.getName(),
            new ArrayList<>(function.calcReadVariables()), new ArrayList<>(collector.readVariables));
        if (collector.sets != null) {
            assertSameElements("assignments in " + function.getName(), referenceSetStatementsOf(function), collector.sets);
        }
    }

    private static void assertSameElements(String what, List<?> expected, List<?> actual) {
        boolean same = expected.size() == actual.size();
        for (int i = 0; same && i < expected.size(); i++) {
            same = expected.get(i) == actual.get(i);
        }
        if (!same) {
            throw new AssertionError("The walk over the body found other " + what + " than the separate walks: expected "
                + expected + ", found " + actual);
        }
    }

    private Multimap<ImFunction, ImFunction> getCallRelations() {
        return callRelations;
    }

    public ImFunction getMainFunc() { return mainFunc; }
    public ImFunction getConfFunc() { return configFunc; }

    public List<ImFunction> getInitializationOrder() {
        return Collections.unmodifiableList(initializationOrder);
    }

    /**
     * returns a list of classes and functions implementing funcDef
     */
    public Map<ClassDef, FuncDef> getClassesWithImplementation(Collection<ClassDef> instances, FuncDef func) {
        if (func.attrIsPrivate()) {
            // private functions cannot be overridden
            return Collections.emptyMap();
        }
        Map<ClassDef, FuncDef> result = Maps.newLinkedHashMap();
        for (ClassDef c : instances) {
            FuncLink funcNameLink = null;
            WurstTypeClass cType = c.attrTypC();
            for (FuncLink nameLink : func.lookupMemberFuncs(cType, func.getName())) {
                if (nameLink.getDef() == func) {
                    funcNameLink = nameLink;
                }
            }
            if (funcNameLink == null) {
                throw new Error("Could not find "
                    + Utils.printElementWithSource(Optional.of(func))
                    + " in "
                    + Utils.printElementWithSource(Optional.of(c)));
            }
            FuncLink implementationLink = findImplementationLink(c, funcNameLink);
            if (implementationLink != null && implementationLink.getDef() instanceof FuncDef) {
                result.put(c, (FuncDef) implementationLink.getDef());
            }
        }
        return result;
    }

    private @Nullable FuncLink findImplementationLink(ClassDef c, FuncLink target) {
        FuncLink best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (NameLink candidateLink : c.attrNameLinks().get(target.getName())) {
            if (!(candidateLink instanceof FuncLink candidate)) {
                continue;
            }
            if (!WurstValidator.canOverride(candidate, target, false)) {
                continue;
            }
            FunctionDefinition candidateDef = candidate.getDef();
            if (!(candidateDef instanceof FuncDef)) {
                continue;
            }
            if (candidateDef.attrIsAbstract()) {
                continue;
            }
            ClassDef owner = candidateDef.attrNearestClassDef();
            if (owner == null) {
                continue;
            }
            int distance = distanceToOwner(c, owner);
            if (distance == Integer.MAX_VALUE) {
                continue;
            }
            if (best == null || distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private int distanceToOwner(ClassDef start, ClassDef owner) {
        int distance = 0;
        ClassDef current = start;
        Set<ClassDef> visited = new HashSet<>();
        while (current != null && visited.add(current)) {
            if (current == owner) {
                return distance;
            }
            WurstTypeClass type = current.attrTypC();
            if (type == null) {
                break;
            }
            WurstTypeClass superType = type.extendedClass();
            if (superType == null) {
                break;
            }
            current = superType.getClassDef();
            distance++;
        }
        return Integer.MAX_VALUE;
    }


    private final Map<ClassDef, List<Pair<ImVar, VarInitialization>>> classDynamicInitMap = Maps.newLinkedHashMap();
    private final Map<ClassDef, List<WStatement>> classInitStatements = Maps.newLinkedHashMap();

    public List<Pair<ImVar, VarInitialization>> getDynamicInits(ClassDef c) {
        return classDynamicInitMap.computeIfAbsent(c, k -> Lists.newArrayList());
    }


    private final Map<ConstructorDef, ImFunction> constructorFuncs = Maps.newLinkedHashMap();

    public ImFunction getConstructFunc(ConstructorDef constr) {
        ImFunction f = constructorFuncs.get(constr);
        if (f == null) {
            String name = constructorName(constr);
            ImVars params = ImVars(getThisVar(constr));
            for (WParameter p : constr.getParameters()) {
                params.add(getVarFor(p));
            }
            List<FunctionFlag> constructorFlags = flags();
            if (!constr.getParameters().isEmpty()
                && constr.getParameters().get(constr.getParameters().size() - 1).attrIsVararg()) {
                constructorFlags.add(IS_VARARG);
            }
            f = ImFunction(constr, name, ImTypeVars(), params, ImVoid(), ImVars(), ImStmts(), constructorFlags);
            addFunction(f, constr);
            constructorFuncs.put(constr, f);
        }
        return f;
    }


    private String constructorName(ConstructorDef constr) {
        ArrayDeque<String> names = new ArrayDeque<>();
        de.peeeq.wurstscript.ast.Element e = constr;
        while (e != null) {
            if (e instanceof ClassOrModuleInstanciation) {
                ClassOrModuleInstanciation mi = (ClassOrModuleInstanciation) e;
                int index = mi.getConstructors().indexOf(constr);
                names.addFirst(mi.getName() + (index > 0 ? 1 + index : ""));
                if (e instanceof ClassDef) {
                    break;
                }
            }
            e = e.getParent();
        }
        return "construct_" + String.join("_", names);
    }


    Map<ConstructorDef, ImFunction> constrNewFuncs = Maps.newLinkedHashMap();

    private ImFunction genericNewMarker;

    public ImFunction getGenericNewMarker() {
        if (genericNewMarker == null) {
            ImTypeVar typeVar = JassIm.ImTypeVar("T");
            genericNewMarker = ImFunction(emptyTrace, de.peeeq.wurstscript.CompilerIntrinsics.NEW_MARKER,
                ImTypeVars(typeVar), ImVars(), JassIm.ImTypeVarRef(typeVar), ImVars(), ImStmts(), flags());
        }
        return genericNewMarker;
    }

    public boolean isGenericNewMarker(ImFunction function) {
        return function == genericNewMarker;
    }

    /** The function which constructs with this constructor, if the program has one already: nothing is created. */
    public @Nullable ImFunction constructNewFuncIfTranslated(ConstructorDef constr) {
        return constrNewFuncs.get(constr);
    }

    public ImFunction getConstructNewFunc(ConstructorDef constr) {
        ImFunction f = constrNewFuncs.get(constr);
        if (f == null) {
            String name = "new_" + constr.attrNearestClassDef().getName();
            List<FunctionFlag> constructorFlags = flags();
            if (!constr.getParameters().isEmpty()
                && constr.getParameters().get(constr.getParameters().size() - 1).attrIsVararg()) {
                constructorFlags.add(IS_VARARG);
            }
            f = ImFunction(constr, name, ImTypeVars(), ImVars(), selfType(constr.attrNearestClassOrInterface()), ImVars(), ImStmts(), constructorFlags);
            addFunction(f, constr);
            constrNewFuncs.put(constr, f);
        }
        return f;
    }

    public ImProg getImProg() {
        return imProg;
    }


    private Multimap<InterfaceDef, ClassDef> interfaceInstances = null;

    public Collection<ClassDef> getInterfaceInstances(InterfaceDef interfaceDef) {
        if (interfaceInstances == null) {
            calculateInterfaceInstances();
        }
        return interfaceInstances.get(interfaceDef);
    }

    private void calculateInterfaceInstances() {
        // Insertion order, not hash order. The order of the compilation units still shows in it;
        // sortEverything fixes the order of the sub-methods which are made from it.
        interfaceInstances = LinkedHashMultimap.create();
        for (CompilationUnit cu : wurstProg) {
            for (ClassDef c : cu.attrGetByType().classes) {
                for (WurstTypeInterface i : c.attrTypC().transitiveSuperInterfaces()) {
                    interfaceInstances.put(i.getDef(), c);
                }
            }
        }
    }


    private TransitiveClosure<ClassDef> subclasses = null;
    private Multimap<ClassDef, ClassDef> directSubclasses = null;

    private boolean isEclipseMode = false;

    public List<ClassDef> getSubClasses(ClassDef classDef) {
        calculateSubclasses();
        return subclasses.getAsList(classDef);
    }

    private void calculateSubclasses() {
        if (subclasses != null) {
            return;
        }
        calculateDirectSubclasses();
        subclasses = new TransitiveClosure<>(directSubclasses);
    }


    private void calculateDirectSubclasses() {
        if (directSubclasses != null) {
            return;
        }
        // Insertion order, not hash order. The order of the compilation units still shows in it;
        // sortEverything fixes the order of the sub-methods which are made from it.
        directSubclasses = LinkedHashMultimap.create();
        for (ClassDef c : classes()) {
            WurstTypeClass extendedClass = c.attrTypC().extendedClass();
            if (extendedClass != null) {
                directSubclasses.put(extendedClass.getDef(), c);
            }
        }
    }

    /**
     * calculates list of all classes
     * ignoring the ones in modules, only module instantiations
     */
    private List<ClassDef> classes() {
        List<ClassDef> result = new ArrayList<>();
        for (CompilationUnit cu : wurstProg) {
            for (WPackage p : cu.getPackages()) {
                for (WEntity e : p.getElements()) {
                    if (e instanceof ClassDef c) {
                        classesAdd(result, c);
                    }
                }
            }
        }
        return result;

    }

    private void classesAdd(List<ClassDef> result, ClassOrModuleInstanciation c) {
        if (c instanceof ClassDef classDef) {
            result.add(classDef);
        }
        for (ClassDef ic : c.getInnerClasses()) {
            classesAdd(result, ic);
        }
        for (ModuleInstanciation mi : c.getModuleInstanciations()) {
            classesAdd(result, mi);
        }
    }

    public int getEnumMemberId(EnumMember enumMember) {
        return ((EnumMembers) enumMember.getParent()).indexOf(enumMember);
    }

    private ImFunction getDebugPrintFunction() {
        return debugPrintFunction;
    }

    public boolean isEclipseMode() {
        return isEclipseMode;
    }

    public void setEclipseMode(boolean enabled) {
        isEclipseMode = enabled;
    }

    public TypeParamDef getTypeParamDef(ImTypeVar tv) {
        return typeVariableReverse.get(tv);
    }

    /**
     * The signature node standing for one type class requirement.
     * <p>
     * Requirements are shared across every use of the bound, so each interface method maps to
     * exactly one node. Dispatch sites reference it, and each type argument carries the concrete
     * implementation bound to it, which is what lets generic elimination turn a dispatch into a
     * direct call.
     */
    public ImTypeClassFunc getTypeClassFunc(FuncDef method) {
        return typeClassFuncs.computeIfAbsent(method, m -> {
            ImTypeClassFunc result = JassIm.ImTypeClassFunc(m, m.getName(), JassIm.ImTypeVars(),
                    JassIm.ImVars(), m.attrReturnTyp().imTranslateType(this));
            imProg.getTypeClassFunctions().add(result);
            return result;
        });
    }

    private final Map<FuncDef, ImTypeClassFunc> typeClassFuncs = new LinkedHashMap<>();

    /**
     * Every type class implementation in the program, held per requirement against the type it is
     * for.
     * <p>
     * A type argument can carry its instances directly, but that binding lives on the argument
     * position and is lost as soon as a type variable is substituted into a plain type, which is
     * what happens when a generic class type travels through a return type or a receiver. Instance
     * selection is static, so recording the type is enough to recover it.
     * <p>
     * Matched by structural type equality rather than by printed name: a class prints as its simple
     * name, so two classes of the same name in different packages would otherwise collide and the
     * second would silently dispatch through the first. The lists hold one entry per instance of a
     * requirement, so scanning them is cheaper than the printing it replaces.
     */
    private final Map<ImTypeClassFunc, List<TypeClassImpl>> typeClassImpls = new LinkedHashMap<>();

    private record TypeClassImpl(ImType instanceType, ImFunction impl) {
    }

    public void registerTypeClassImpl(ImTypeClassFunc requirement, ImType instanceType, ImFunction impl) {
        List<TypeClassImpl> impls = typeClassImpls.computeIfAbsent(requirement, r -> new ArrayList<>());
        for (TypeClassImpl existing : impls) {
            if (existing.instanceType().equalsType(instanceType)) {
                return;
            }
        }
        impls.add(new TypeClassImpl(instanceType, impl));
    }

    public @Nullable ImFunction lookupTypeClassImpl(ImTypeClassFunc requirement, ImType instanceType) {
        List<TypeClassImpl> impls = typeClassImpls.get(requirement);
        if (impls == null) {
            return null;
        }
        for (TypeClassImpl candidate : impls) {
            if (candidate.instanceType().equalsType(instanceType)) {
                return candidate.impl();
            }
        }
        return null;
    }

    public ImTypeVar getTypeVar(TypeParamDef tp) {
        // If we're translating inside a captured class (Iterator), prefer its override
        for (Map<TypeParamDef, ImTypeVar> m : typeVarOverrideStack) {
            ImTypeVar v = m.get(tp);
            if (v != null) return v;
        }
        // Fallback: canonical per-TypeParamDef var (used elsewhere)
        return typeVariable.getFor(tp);
    }
    public boolean isLuaTarget() {
        return runArgs.isLua();
    }

    interface VarsForTupleResult {

        default Iterable<ImVar> allValues() {
            return allValuesStream()::iterator;
        }

        Stream<ImVar> allValuesStream();

        <T> T map(Function<Stream<T>, T> nodeBuilder, Function<ImVar, T> leafBuilder);
    }

    static class SingleVarResult implements VarsForTupleResult {
        private final ImVar var;

        public SingleVarResult(ImVar var) {
            this.var = var;
        }

        public ImVar getVar() {
            return var;
        }

        @Override
        public Stream<ImVar> allValuesStream() {
            return Stream.of(var);
        }

        @Override
        public <T> T map(Function<Stream<T>, T> nodeBuilder, Function<ImVar, T> leafBuilder) {
            return leafBuilder.apply(var);
        }

        @Override
        public String toString() {
            return var.toString();
        }
    }

    static class TupleResult implements VarsForTupleResult {
        private final List<VarsForTupleResult> items;

        public TupleResult(List<VarsForTupleResult> items) {
            this.items = items;
        }

        public List<VarsForTupleResult> getItems() {
            return items;
        }

        @Override
        public Stream<ImVar> allValuesStream() {
            return items.stream().flatMap(VarsForTupleResult::allValuesStream);
        }

        @Override
        public <T> T map(Function<Stream<T>, T> nodeBuilder, Function<ImVar, T> leafBuilder) {
            return nodeBuilder.apply(items.stream().map(e -> e.map(nodeBuilder, leafBuilder)));
        }

        @Override
        public String toString() {
            return "<" + Utils.printSep(", ", items) + ">";
        }
    }

    public VarsForTupleResult getVarsForTuple(ImVar v) {
        // TODO use list instead of tree
        VarsForTupleResult result = varsForTupleVar.get(v);
        if (result == null) {
            if (TypesHelper.typeContainsTuples(v.getType())) {
                result = createVarsForType(v.getName(), v.getType(), Function.identity(), v.getTrace());
            } else {
                result = new SingleVarResult(v);
            }
            varsForTupleVar.put(v, result);
        }
        return result;
    }

    /** Scalar leaves assigned for one source-level tuple variable, in source order. */
    public List<ImVar> getTupleScalarVars(ImVar v) {
        return getVarsForTuple(v).allValuesStream().toList();
    }

    VarsForTupleResult getVarsForTuple(ImVar v, ImType concreteStorageType) {
        if (!TypesHelper.typeContainsTuples(v.getType())
            && TypesHelper.typeContainsTuples(concreteStorageType)) {
            VarsForTupleResult result = varsForTupleVar.get(v);
            if (result != null) {
                return result;
            }
            result = createVarsForType(v.getName(), concreteStorageType, Function.identity(), v.getTrace());
            varsForTupleVar.put(v, result);
            if (v.getParent() instanceof ImVars owner) {
                int position = owner.indexOf(v) + 1;
                for (ImVar scalar : result.allValues()) {
                    if (!owner.contains(scalar)) {
                        owner.add(position++, scalar);
                    }
                }
            }
            return result;
        }
        return getVarsForTuple(v);
    }


    /**
     * Creates variables for the given type, eliminating tuple types
     *
     * @param name            base name for the variables
     * @param type            the type for which to create variables
     * @param typeConstructor how the types are constructed (creating an array or multi-array, just returning the type)
     * @param tr              trace for the variables
     * @return
     */
    private VarsForTupleResult createVarsForType(String name, final ImType type, Function<ImType, ImType> typeConstructor, de.peeeq.wurstscript.ast.Element tr) {
        return type.match(new ImType.Matcher<VarsForTupleResult>() {
            @Override
            public VarsForTupleResult case_ImArrayType(ImArrayType at) {
                if (at.getEntryType() instanceof ImTupleType) {
                    // if it is an array of tuples, create multiple array variables:
                    ImTupleType tt = (ImTupleType) at.getEntryType();
                    Builder<VarsForTupleResult> ts = ImmutableList.builder();
                    int i = 0;
                    for (ImType t : tt.getTypes()) {
                        ts.add(createVarsForType(name + "_" + tt.getNames().get(i), t, JassIm::ImArrayType, tr));
                        i++;
                    }
                    return new TupleResult(ts.build());
                }
                // otherwise just create the array variable
                return new SingleVarResult(JassIm.ImVar(tr, type, name, false));
            }

            @Override
            public VarsForTupleResult case_ImTypeVarRef(ImTypeVarRef imTypeVarRef) {
                throw new RuntimeException("Should be called after eliminating generics.");
            }

            @Override
            public VarsForTupleResult case_ImArrayTypeMulti(ImArrayTypeMulti at) {
                if (at.getEntryType() instanceof ImTupleType) {
                    // if it is an array of tuples, create multiple array variables:
                    ImTupleType tt = (ImTupleType) at.getEntryType();
                    Builder<VarsForTupleResult> ts = ImmutableList.builder();
                    int i = 0;
                    for (ImType t : tt.getTypes()) {
                        ts.add(createVarsForType(name + "_" + tt.getNames().get(i), t, et -> JassIm.ImArrayTypeMulti(et, new ArrayList<>(at.getArraySize())), tr));
                        i++;
                    }
                    return new TupleResult(ts.build());
                }
                // otherwise just create the array variable
                return new SingleVarResult(JassIm.ImVar(tr, type, name, false));
            }

            @Override
            public VarsForTupleResult case_ImVoid(ImVoid imVoid) {
                return new TupleResult(Collections.emptyList());
            }

            @Override
            public VarsForTupleResult case_ImClassType(ImClassType st) {
                ImType type = typeConstructor.apply(st);
                return new SingleVarResult(JassIm.ImVar(tr, type, name, false));
            }

            @Override
            public VarsForTupleResult case_ImSimpleType(ImSimpleType st) {
                ImType type = typeConstructor.apply(st);
                return new SingleVarResult(JassIm.ImVar(tr, type, name, false));
            }

            @Override
            public VarsForTupleResult case_ImAnyType(ImAnyType at) {
                ImType type = typeConstructor.apply(at);
                return new SingleVarResult(JassIm.ImVar(tr, type, name, false));
            }

            @Override
            public VarsForTupleResult case_ImTupleType(ImTupleType tt) {
                int i = 0;
                Builder<VarsForTupleResult> ts = ImmutableList.builder();
                for (ImType t : tt.getTypes()) {
                    ts.add(createVarsForType(name + "_" + tt.getNames().get(i), t, typeConstructor, tr));
                    i++;
                }
                return new TupleResult(ts.build());
            }
        });
    }


    private void addVarsForType(List<ImVar> result, String name, ImType type, de.peeeq.wurstscript.ast.Element tr) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(result);
        // TODO handle names
        if (type instanceof ImTupleType tt) {
            int i = 0;
            for (ImType t : tt.getTypes()) {
                addVarsForType(result, name + "_" + tt.getNames().get(i), t, tr);
                i++;
            }
        } else if (type instanceof ImVoid) {
            // nothing to add
        } else {
            result.add(JassIm.ImVar(tr, type, name, false));
        }

    }

    private final Map<ImFunction, VarsForTupleResult> tempReturnVars = Maps.newLinkedHashMap();

    public VarsForTupleResult getTupleTempReturnVarsFor(ImFunction f) {
        VarsForTupleResult result = tempReturnVars.get(f);
        if (result == null) {
            result = createVarsForType(f.getName() + "_return", getOriginalReturnValue(f), Function.identity(), f.getTrace());
            for (ImVar value : result.allValues()) {
                imProg.getGlobals().add(value);
            }
            tempReturnVars.put(f, result);
        }
        return result;
    }

    void setTupleTempReturnVarsFor(ImFunction f, VarsForTupleResult vars) {
        tempReturnVars.put(f, vars);
    }

    private final Map<ImFunction, ImType> originalReturnValues = Maps.newLinkedHashMap();


    public void setOriginalReturnValue(ImFunction f, ImType t) {
        originalReturnValues.put(f, t);
    }

    public ImType getOriginalReturnValue(ImFunction f) {
        return originalReturnValues.computeIfAbsent(f, ImFunction::getReturnType);
    }

    final Set<AssertProperty> properties = Sets.newHashSet();

    public void assertProperties(AssertProperty... properties1) {
        if (!debug) {
            return;
        }
        properties.clear();
        Collections.addAll(properties, properties1);

        assertProperties(properties, imProg);
    }

    public void assertProperties(Set<AssertProperty> properties, Element e) {
        if (e instanceof ElementWithVar elementWithVar) {
            checkVar(elementWithVar.getVar(), properties);
        }
        for (AssertProperty p : properties) {
            p.check(e);
        }
        if (properties.contains(AssertProperty.NOTUPLES)) {
            // TODO ?
        }
        if (properties.contains(AssertProperty.FLAT)) {
            // TODO ?
        }
        for (int i = 0; i < e.size(); i++) {
            Element child = e.get(i);
            if (child.getParent() == null) {
                throw new Error("Child " + i + " (" + child + ") of " + e + " not attached to tree");
            } else if (child.getParent() != e) {
                throw new Error("Child " + i + " (" + child + ") of " + e + " attached to wrong tree");
            }
            assertProperties(properties, child);
        }
    }

    private void checkVar(ImVar left, Set<AssertProperty> properties) {
        if (left.getParent() == null) {
            throw new Error("var not attached: " + left);
        }
        if (properties.contains(AssertProperty.NOTUPLES)) {
            if (TypesHelper.typeContainsTuples(left.getType())) {
                throw new Error("program contains tuple var " + left);
            }
        }
    }

    public Set<ImVar> getUsedVariables() {
        if (usedVariables == null) {
            calculateCallRelationsAndUsedVariables();
        }
        return usedVariables;
    }

    public Set<ImVar> getReadVariables() {
        if (readVariables == null) {
            calculateCallRelationsAndReadVariables();
        }
        return readVariables;
    }

    public Set<ImFunction> getUsedFunctions() {
        if (usedFunctions == null) {
            calculateCallRelationsAndReadVariables();
        }
        return usedFunctions;
    }

    public boolean isUnitTestMode() {
        return isUnitTestMode;
    }

    /** What the modification count of each function was when a flatten left it, which is flat. */
    private final Reference2IntOpenHashMap<ImFunction> flattenedAt = new Reference2IntOpenHashMap<>();

    /** Whether the function is as a flatten left it: nothing in it was modified since. */
    public synchronized boolean isUnmodifiedSinceFlatten(ImFunction function) {
        return flattenedAt.containsKey(function) && flattenedAt.getInt(function) == function.modificationCount();
    }

    /** A flatten has left the function flat as it is now. */
    public synchronized void flattened(ImFunction function) {
        flattenedAt.put(function, function.modificationCount());
    }

    /**
     * Whether the program holds no statement expression, which is what a flatten leaves and what the backends need: Jass
     * has no such expression, and Lua would make a closure of each. A compile-time expression is not looked into, the
     * interpreter evaluates it as it is.
     */
    public boolean isFlat() {
        return isFlat(imProg);
    }

    /** Whether the element holds no statement expression (see {@link #isFlat()}). */
    public static boolean isFlat(Element element) {
        boolean[] flat = {true};
        element.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImStatementExpr e) {
                flat[0] = false;
            }

            @Override
            public void visit(ImCompiletimeExpr e) {
            }
        });
        return flat[0];
    }

    /** In a unit test: fails when the program is not flat, which names where. */
    public void assertFlat(String where) {
        if (isUnitTestMode && !isFlat()) {
            throw new AssertionError("The program is not flat " + where);
        }
    }

    private final Map<ExprClosure, ImClass> classForClosure = Maps.newLinkedHashMap();

    public ImClass getClassForClosure(ExprClosure s) {
        Preconditions.checkNotNull(s);
        return classForClosure.computeIfAbsent(s, s1 -> JassIm.ImClass(s1, "Closure", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Lists.newArrayList()));
    }

    // inside ImTranslator

    private static boolean isIteratorLike(ClassOrInterface s) {
        return (s instanceof NamedScope namedScope) && namedScope.getName().contains("Iterator");
    }

    private void addCapturedTypeVarsFromOwningGeneric(ImTypeVars typeVariables, ClassOrInterface s) {
        if (isIteratorLike(s)) {
            WLogger.trace(() -> "[GENCAP] addCaptured enter: " + s.getClass().getSimpleName()
                + " name=" + ((NamedScope) s).getName()
                + " parent=" + (s.getParent() == null ? "null" : s.getParent().getClass().getSimpleName()));
        }

        if (!(s instanceof ClassDef cd)) {
            if (isIteratorLike(s)) WLogger.trace(() -> "[GENCAP] not a ClassDef -> skip");
            return;
        }

        boolean isStatic = cd.attrIsStatic();
        if (isIteratorLike(s)) WLogger.trace(() -> "[GENCAP] isStatic=" + isStatic);
        if (!isStatic) return;

        de.peeeq.wurstscript.ast.Element parent = cd.getParent();
        de.peeeq.wurstscript.ast.Element parent2 = parent == null ? null : parent.getParent();

        AstElementWithTypeParameters owner = null;
        String ownerInfo = null;

        if (parent2 instanceof ModuleInstanciation mi) {
            ClassDef o = mi.attrNearestClassDef();
            owner = o;
            ownerInfo = "moduleInst=" + mi.getName() + " owner=" + (o == null ? "null" : o.getName());
        } else if (parent2 instanceof ClassDef o) {
            owner = o;
            ownerInfo = "outerClass=" + o.getName();
        } else if (parent2 instanceof InterfaceDef o) {
            owner = o;
            ownerInfo = "outerInterface=" + o.getName();
        } else {
            if (isIteratorLike(s)) WLogger.trace(() -> "[GENCAP] parent2 not ModuleInstanciation/ClassDef/InterfaceDef -> skip");
            return;
        }

        if (isIteratorLike(s) && WLogger.isTraceEnabled()) WLogger.trace("[GENCAP] " + ownerInfo);

        if (owner == null) return;
        if (owner == cd) return;

        // override map for this static inner class: owner TypeParamDef -> captured ImTypeVar (fresh node)
        Map<TypeParamDef, ImTypeVar> override =
            capturedOwnerTypeVarsByStaticClass.computeIfAbsent(cd, k -> new IdentityHashMap<>());

        for (TypeParamDef tp : owner.getTypeParameters()) {
            if (!(tp.getTypeParamConstraints() instanceof TypeExprList)) {
                continue;
            }

            ImTypeVar captured = override.get(tp);
            if (captured == null) {
                // Create a *fresh* ImTypeVar node (do NOT reuse getTypeVar(tp))
                String baseName = tp.getName();
                String name = baseName;

                if (hasTypeVarNamed(typeVariables, name)) {
                    String ownerName = (owner instanceof NamedScope) ? ((NamedScope) owner).getName() : "Owner";
                    name = baseName + "$" + ownerName;
                }
                if (hasTypeVarNamed(typeVariables, name)) {
                    name = baseName + "$cap" + typeVariables.size();
                }

                captured = JassIm.ImTypeVar(name);

                // keep reverse mapping working for captured vars too
                typeVariableReverse.put(captured, tp);

                override.put(tp, captured);

                if (isIteratorLike(s) && WLogger.isTraceEnabled()) WLogger.trace("[GENCAP] created captured owner tvar: " + captured.getName());
            }

            // Add to inner class' ImTypeVars if not already present by name
            if (!hasTypeVarNamed(typeVariables, captured.getName())) {
                typeVariables.add(captured);
                if (isIteratorLike(s) && WLogger.isTraceEnabled()) WLogger.trace("[GENCAP] captured owner tvar added: " + captured.getName());
            }
        }
    }


    private final Map<ClassOrInterface, @Nullable ImClass> classForStructureDef = Maps.newLinkedHashMap();

    public ImClass getClassFor(ClassOrInterface s) {
        Preconditions.checkNotNull(s);
        return classForStructureDef.computeIfAbsent(s, s1 -> {
            ImTypeVars typeVariables = JassIm.ImTypeVars();

            // 1) class' own type parameters (unchanged idea, but use name-based uniqueness)
            if (s1 instanceof AstElementWithTypeParameters astElementWithTypeParameters) {
                for (TypeParamDef tp : astElementWithTypeParameters.getTypeParameters()) {
                    if (tp.getTypeParamConstraints() instanceof TypeExprList) {
                        ImTypeVar tv = getTypeVar(tp); // now context-aware (override stack)
                        if (!hasTypeVarNamed(typeVariables, tv.getName())) {
                            typeVariables.add(tv);
                        }
                    }
                }
            }

            // 2) NEW: capture owner generics for static classes inside module instantiations
            // (keeps your logging + parent logic inside that method)
            addCapturedTypeVarsFromOwningGeneric(typeVariables, s1);

            return JassIm.ImClass(
                s1,
                s1.getName(),
                typeVariables,
                JassIm.ImVars(),
                JassIm.ImMethods(),
                JassIm.ImFunctions(),
                Lists.newArrayList()
            );
        });
    }


    Map<FuncDef, ImMethod> methodForFuncDef = Maps.newLinkedHashMap();

    public ImMethod getMethodFor(FuncDef f) {

        ImMethod m = methodForFuncDef.get(f);
        if (m == null) {
            ImFunction imFunc = getFuncFor(f);

            // IMPORTANT: method name must match implementation function name,
            // otherwise EliminateClasses dispatch lookup can fail.
            String methodName = imFunc.getName();
            WLogger.trace(() -> "[GENCAP] getMethodFor " + elementNameWithPath(f) + " -> methodName=" + methodName);
            m = JassIm.ImMethod(f, selfType(f), methodName, imFunc, Lists.newArrayList(), Lists.newArrayList(), "", false);
            methodForFuncDef.put(f, m);
        }
        return m;
    }

    public ClassManagementVars getClassManagementVarsFor(ImClass c) {
        Map<ImClass, ClassManagementVars> vars = getClassManagementVars();
        ClassManagementVars res = vars.get(c);
        if (res != null) {
            return res;
        }
        // Try to recover by aliasing to an already existing entry without rebuilding.
        ClassManagementVars alias = findClassManagementAlias(c, vars);
        if (alias != null) {
            vars.put(c, alias);
            return alias;
        }
        // Extend mapping for current class graph only; do not clear and rebuild map,
        // as rebuilding creates duplicate globals/initializers.
        Partitions<ImClass> p = buildClassPartitions();
        p.add(c);
        for (ImClassType sc : c.getSuperClasses()) {
            p.union(c, sc.getClassDef());
        }
        ImClass rep = p.getRep(c);
        ClassManagementVars repVars = vars.get(rep);
        if (repVars == null) {
            repVars = findClassManagementAlias(rep, vars);
            if (repVars == null) {
                repVars = new ClassManagementVars(rep, this);
            }
            vars.put(rep, repVars);
        }
        for (ImClass cls : imProg.getClasses()) {
            if (p.getRep(cls) == rep) {
                vars.putIfAbsent(cls, repVars);
            }
        }
        vars.put(c, repVars);
        return repVars;
    }


    private Map<ImClass, ClassManagementVars> classManagementVars = null;

    private @Nullable ImFunction errorFunc;

    public Map<ImClass, ClassManagementVars> getClassManagementVars() {
        if (classManagementVars != null) {
            return classManagementVars;
        }
        Partitions<ImClass> p = buildClassPartitions();
        // generate typeId variables
        classManagementVars = new IdentityHashMap<>();
        for (ImClass c : imProg.getClasses()) {
            ImClass rep = p.getRep(c);
            ClassManagementVars v = classManagementVars.computeIfAbsent(rep, r -> new ClassManagementVars(r, this));
            classManagementVars.put(c, v);
        }
        return classManagementVars;
    }

    public void clearStaleClassManagementVars() {
        if (classManagementVars == null) {
            return;
        }
        if (classManagementVars.keySet().stream().anyMatch(c -> !imProg.getClasses().contains(c))
                || classManagementVars.values().stream().anyMatch(this::hasStaleClassManagementVars)) {
            classManagementVars = null;
        }
    }

    private boolean hasStaleClassManagementVars(ClassManagementVars vars) {
        return !imProg.getGlobals().contains(vars.free)
            || !imProg.getGlobals().contains(vars.freeCount)
            || !imProg.getGlobals().contains(vars.maxIndex)
            || !imProg.getGlobals().contains(vars.typeId);
    }

    private Partitions<ImClass> buildClassPartitions() {
        Partitions<ImClass> p = new Partitions<>();
        for (ImClass c : imProg.getClasses()) {
            p.add(c);
            for (ImClassType sc : c.getSuperClasses()) {
                p.union(c, sc.getClassDef());
            }
        }
        return p;
    }

    private @Nullable ClassManagementVars findClassManagementAlias(ImClass target, Map<ImClass, ClassManagementVars> vars) {
        for (Map.Entry<ImClass, ClassManagementVars> e : vars.entrySet()) {
            ImClass k = e.getKey();
            if (k == target || k.attrTrace() == target.attrTrace()) {
                return e.getValue();
            }
        }
        return null;
    }


    public ImFunction getGlobalInitFunc() {
        return globalInitFunc;
    }


    public ImFunctionCall imError(de.peeeq.wurstscript.ast.Element trace, ImExpr message) {
        ImFunction ef = errorFunc;
        if (ef == null) {
            Optional<ImFunction> f = findErrorFunc().map(this::getFuncFor);
            ef = errorFunc = f.orElseGet(this::makeDefaultErrorFunc);
        }
        ImExprs arguments = JassIm.ImExprs(message);
        return ImFunctionCall(trace, ef, ImTypeArguments(), arguments, false, CallType.NORMAL);
    }

    private ImFunction makeDefaultErrorFunc() {
        ImVar msgVar = JassIm.ImVar(emptyTrace, TypesHelper.imString(), "msg", false);
        ImVars parameters = JassIm.ImVars(msgVar);
        ImType returnType = JassIm.ImVoid();
        ImVars locals = JassIm.ImVars();
        ImStmts body = JassIm.ImStmts();

        // print message:
        // msg = msg + stacktrace
        ImExpr msg = JassIm.ImOperatorCall(WurstOperator.PLUS, ImExprs(JassIm.ImVarAccess(msgVar),
            JassIm.ImOperatorCall(WurstOperator.PLUS,
                ImExprs(
                    JassIm.ImStringVal("\n"),
                    JassIm.ImGetStackTrace()))));

        body.add(ImFunctionCall(emptyTrace, getDebugPrintFunction(), ImTypeArguments(), JassIm.ImExprs(msg), false, CallType.NORMAL));
        // TODO divide by zero to crash thread:


//		stmts.add(JassAst.JassStmtCall("BJDebugMsg",
//				JassAst.JassExprlist(JassAst.JassExprBinary(
//						JassAst.JassExprStringVal("|cffFF3A29Wurst Error:|r" + nl),
//						JassAst.JassOpPlus(),
//						s.getMessage().translate(translator)))));
//		// crash thread (divide by zero)
//		stmts.add(JassAst.JassStmtCall("I2S", JassAst.JassExprlist(JassAst.JassExprBinary(JassAst.JassExprIntVal("1"), JassAst.JassOpDiv(), Jas
//

        List<FunctionFlag> flags = Lists.newArrayList();

        ImFunction errorFunc = ImFunction(emptyTrace, "error", ImTypeVars(), parameters, returnType, locals, body, flags);
        imProg.getFunctions().add(errorFunc);
        return errorFunc;
    }


    /** The function {@code error} of the ErrorHandling package, a native or one with a body. */
    private Optional<FunctionDefinition> findErrorFunc() throws CompileError {
        PackageLink p = wurstProg.lookupPackage("ErrorHandling");
        if (p == null) {
            return Optional.empty();
        }
        ImmutableCollection<FuncLink> funcs = p.getDef().getElements().lookupFuncs("error");
        if (funcs.isEmpty()) {
            return Optional.empty();
        } else if (funcs.size() > 1) {
            return Optional.empty();
        }
        return Optional.of(funcs.stream().findAny().get().getDef());
    }

    int getCompiletimeExpressionsOrder(FunctionCall fc) {
        return compiletimeExpressionsOrder.getOrDefault(fc, 0);
    }

    public RunArgs getRunArgs() {
        return runArgs;
    }

    private final Map<ImMethod, String> dispatchSegments = new IdentityHashMap<>();

    /**
     * The part of a dispatch group's assigned name which identifies the method rather than a class.
     * <p>
     * Recorded by {@code LuaDispatchPreparation.normalizeMethodNames}, the only place which knows it: it
     * names a whole group after one member's already class-prefixed name, sanitises that into a Lua
     * identifier, and uniques it against every name taken. Reconstructing the segment afterwards means
     * cutting the result at a boundary nobody recorded, which is where four dispatch bugs came from -
     * most memorably a method declared {@code get_it} composing a slot called {@code it}.
     */
    public void recordDispatchSegment(ImMethod method, String segment) {
        dispatchSegments.put(method, segment);
    }

    /** The recorded segment, or the method's whole name when nothing recorded one - never a cut. */
    public String dispatchSegmentOf(ImMethod method) {
        if (method == null) {
            return "";
        }
        String segment = dispatchSegments.get(method);
        return segment != null ? segment : method.getName();
    }
}
