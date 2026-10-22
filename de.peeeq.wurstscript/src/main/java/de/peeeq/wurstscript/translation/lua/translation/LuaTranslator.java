package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.CompileTimeInfo;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.LuaFieldDefaults;
import de.peeeq.wurstscript.translation.imtranslation.LuaHashtable;
import de.peeeq.wurstscript.translation.imtranslation.GenericTypes;
import de.peeeq.wurstscript.translation.imtranslation.LuaDispatchPreparation;
import de.peeeq.wurstscript.translation.imtranslation.LuaMultipleResults;
import de.peeeq.wurstscript.translation.imtranslation.LuaNativeLowering;
import de.peeeq.wurstscript.translation.imtranslation.TypeId;
import de.peeeq.wurstscript.translation.lua.printing.LuaPrinter;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.utils.Lazy;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.NamePreservation;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.peeeq.wurstscript.translation.lua.translation.ExprTranslation.WURST_SUPERTYPES;

public class LuaTranslator {
    private static final int LUA_LOCALS_LIMIT = 200;
    private static final int LUA_STORAGE_ALIAS_LOCAL_BUDGET = 190;
    private static final int LUA_STORAGE_ALIAS_LIMIT = 16;
    private static final List<String> HASHTABLE_HANDLE_SAVE_NAMES = Arrays.asList(
        "SavePlayerHandle", "SaveWidgetHandle", "SaveDestructableHandle", "SaveItemHandle", "SaveUnitHandle",
        "SaveAbilityHandle", "SaveTimerHandle", "SaveTriggerHandle", "SaveTriggerConditionHandle",
        "SaveTriggerActionHandle", "SaveTriggerEventHandle", "SaveForceHandle", "SaveGroupHandle",
        "SaveLocationHandle", "SaveRectHandle", "SaveBooleanExprHandle", "SaveSoundHandle", "SaveEffectHandle",
        "SaveUnitPoolHandle", "SaveItemPoolHandle", "SaveQuestHandle", "SaveQuestItemHandle",
        "SaveDefeatConditionHandle", "SaveTimerDialogHandle", "SaveLeaderboardHandle", "SaveMultiboardHandle",
        "SaveMultiboardItemHandle", "SaveTrackableHandle", "SaveDialogHandle", "SaveButtonHandle",
        "SaveTextTagHandle", "SaveLightningHandle", "SaveImageHandle", "SaveUbersplatHandle", "SaveRegionHandle",
        "SaveFogStateHandle", "SaveFogModifierHandle", "SaveAgentHandle", "SaveHashtableHandle", "SaveFrameHandle"
    );
    private static final List<String> HASHTABLE_HANDLE_LOAD_NAMES = Arrays.asList(
        "LoadPlayerHandle", "LoadWidgetHandle", "LoadDestructableHandle", "LoadItemHandle", "LoadUnitHandle",
        "LoadAbilityHandle", "LoadTimerHandle", "LoadTriggerHandle", "LoadTriggerConditionHandle",
        "LoadTriggerActionHandle", "LoadTriggerEventHandle", "LoadForceHandle", "LoadGroupHandle",
        "LoadLocationHandle", "LoadRectHandle", "LoadBooleanExprHandle", "LoadSoundHandle", "LoadEffectHandle",
        "LoadUnitPoolHandle", "LoadItemPoolHandle", "LoadQuestHandle", "LoadQuestItemHandle",
        "LoadDefeatConditionHandle", "LoadTimerDialogHandle", "LoadLeaderboardHandle", "LoadMultiboardHandle",
        "LoadMultiboardItemHandle", "LoadTrackableHandle", "LoadDialogHandle", "LoadButtonHandle",
        "LoadTextTagHandle", "LoadLightningHandle", "LoadImageHandle", "LoadUbersplatHandle", "LoadRegionHandle",
        "LoadFogStateHandle", "LoadFogModifierHandle", "LoadHashtableHandle", "LoadFrameHandle"
    );
    private static final List<String> HASHTABLE_NATIVE_NAMES_RAW = Arrays.asList(
        "InitHashtable",
        "SaveInteger", "SaveBoolean", "SaveReal", "SaveStr",
        "LoadInteger", "LoadBoolean", "LoadReal", "LoadStr",
        "HaveSavedInteger", "HaveSavedBoolean", "HaveSavedReal", "HaveSavedString", "HaveSavedHandle",
        "FlushChildHashtable", "FlushParentHashtable",
        "RemoveSavedInteger", "RemoveSavedBoolean", "RemoveSavedReal", "RemoveSavedString", "RemoveSavedHandle"
    );
    private static final Set<String> LUA_HANDLE_TO_INDEX = Set.of(
        "widgetToIndex", "unitToIndex", "destructableToIndex", "itemToIndex", "abilityToIndex",
        "forceToIndex", "groupToIndex", "triggerToIndex", "triggeractionToIndex", "triggerconditionToIndex",
        "timerToIndex", "locationToIndex", "regionToIndex", "rectToIndex", "soundToIndex",
        "effectToIndex", "dialogToIndex", "buttonToIndex", "questToIndex", "questitemToIndex",
        "leaderboardToIndex", "multiboardToIndex", "trackableToIndex", "lightningToIndex",
        "ubersplatToIndex", "framehandleToIndex", "oskeytypeToIndex"
    );
    private static final Set<String> LUA_HANDLE_FROM_INDEX = Set.of(
        "widgetFromIndex", "unitFromIndex", "destructableFromIndex", "itemFromIndex", "abilityFromIndex",
        "forceFromIndex", "groupFromIndex", "triggerFromIndex", "triggeractionFromIndex", "triggerconditionFromIndex",
        "timerFromIndex", "locationFromIndex", "regionFromIndex", "rectFromIndex", "soundFromIndex",
        "effectFromIndex", "dialogFromIndex", "buttonFromIndex", "questFromIndex", "questitemFromIndex",
        "leaderboardFromIndex", "multiboardFromIndex", "trackableFromIndex", "lightningFromIndex",
        "ubersplatFromIndex", "framehandleFromIndex", "oskeytypeFromIndex"
    );
    private static final boolean DEBUG_LUA_DISPATCH = "1".equals(System.getenv("WURST_DEBUG_LUA_DISPATCH"))
        || Boolean.getBoolean("wurst.debug.lua.dispatch");
    private static final boolean DEBUG_LUA_LOCALS = "1".equals(System.getenv("WURST_DEBUG_LUA_LOCALS"))
        || Boolean.getBoolean("wurst.debug.lua.locals");

    final ImProg prog;
    final LuaCompilationUnit luaModel;
    private final LuaStatements deferredMainInit = LuaAst.LuaStatements();
    private final Map<String, Integer> uniqueNameCounters = new HashMap<>();
    private final Set<String> usedNames = LuaReservedNames.all();
    /**
     * The names of the locals of every function in a hot-reload build, which {@link #uniqueName} avoids: a global named
     * after a local of the same name (a helper made when a body first needs it) would be shadowed in that body.
     */
    private final Set<String> localNames = new HashSet<>();
    private final Set<LuaVariable> localizableStorageTables = Collections.newSetFromMap(new IdentityHashMap<>());
    private LuaFunction bootstrapFunction;
    private final Set<String> emittedDispatchSlots = new HashSet<>();
    private final Map<ImClass, Set<String>> emittedDispatchSlotsByClass = new IdentityHashMap<>();
    private final Map<ImClass, Map<String, Set<DispatchGroupIdentity>>> emittedDispatchSlotGroupsByClass = new IdentityHashMap<>();
    /**
     * The receiver sets below are iterated to emit descriptor slots, and classes can share an IM name
     * (every closure of one interface method in one function does), so a name sort cannot order them:
     * they are insertion ordered, filled in program order, and never identity-hash ordered.
     */
    private final Map<DispatchGroupIdentity, Set<ImClass>> concreteReceiverClassesByGroup = new HashMap<>();
    private final Map<ImClass, Set<ImClass>> concreteReceiverClassesByNominalType = new IdentityHashMap<>();
    private final Map<ImMethod, DispatchGroupIdentity> dispatchGroups = new IdentityHashMap<>();
    private final Map<ImMethod, Set<ImClass>> concreteReceiverFamilyCache = new IdentityHashMap<>();
    private final Map<DispatchGroupIdentity, String> canonicalDispatchSlots = new HashMap<>();
    private boolean dispatchGroupsBuilt;
    private boolean dispatchReceiverIndexBuilt;
    private final List<PendingDispatch> pendingDispatches = new ArrayList<>();
    /** The class-table bindings written for a descriptor's slots, so unread ones can be dropped once call sites are resolved. */
    private final Map<LuaStatement, SlotBinding> slotBindings = new IdentityHashMap<>();
    /** The slot names which were dropped, for the check that nothing reads one of them after all. */
    private final Set<String> droppedSlots = new TreeSet<>();

    private record SlotBinding(ImClass receiver, String slot) {
    }

    private static final class PendingDispatch {
        final ImMethod method;
        final LuaExprFieldAccess target;

        PendingDispatch(ImMethod method, LuaExprFieldAccess target) {
            this.method = method;
            this.target = target;
        }
    }

    /**
     * Identity of a dispatch group. The root is an IM method, not a generated Lua name: two
     * unrelated groups are allowed to have equal names after generic elimination. Specializations
     * moved onto one erased class retain their structural type arguments so distinct lowered
     * signatures cannot overwrite one another's descriptor slot.
     */
    private static final class DispatchGroupIdentity {
        final ImMethod root;
        final GenericTypes specialization;
        DispatchGroupIdentity(ImMethod root, GenericTypes specialization) {
            this.root = root;
            this.specialization = specialization;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof DispatchGroupIdentity that)) {
                return false;
            }
            return root == that.root
                && Objects.equals(specialization, that.specialization);
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(root) + Objects.hashCode(specialization);
        }
    }

    private ImProg getProg() {
        return prog;
    }

    List<ExprTranslation.TupleFunc> tupleEqualsFuncs = new ArrayList<>();
    private final Map<ImFunction, LuaFunction> callbackAdapters = new IdentityHashMap<>();
    private LuaFunction callbackErrorHandler;
    private LuaFunction initErrorHandler;

    // Array-default infrastructure (metatables/helper functions) shared across
    // every array of a given entry type, instead of allocated per array
    // instance - see newDefaultArray().
    private final Map<String, LuaVariable> primitiveArrayMetatables = new HashMap<>();
    private final List<LazyArrayDefault> lazyArrayDefaults = new ArrayList<>();

    private static final class LazyArrayDefault {
        final ImType entryType;
        final LuaVariable metatableVar;

        LazyArrayDefault(ImType entryType, LuaVariable metatableVar) {
            this.entryType = entryType;
            this.metatableVar = metatableVar;
        }
    }
    GetAForB<ImVar, LuaVariable> luaVar = new GetAForB<ImVar, LuaVariable>() {
        @Override
        public LuaVariable initFor(ImVar a) {
            String name = a.getName();
            if (!a.getIsBJ() && !NamePreservation.isPreserved(a)) {
                name = a.isGlobal() ? declarationName(name, LuaHotReload.variableId(a)) : localName(ownerFunction(a), name);
            } else {
                usedNames.add(name);
            }
            return LuaAst.LuaVariable(name, LuaAst.LuaNoExpr());
        }
    };

    /** The Lua locals of each results local, one per component ({@link LuaMultipleResults}). Only looked up. */
    private final Map<ImVar, List<LuaVariable>> luaResultVars = new IdentityHashMap<>();

    List<LuaVariable> resultVars(ImVar local) {
        return luaResultVars.computeIfAbsent(local, v -> {
            ImTupleType type = (ImTupleType) v.getType();
            List<LuaVariable> vars = new ArrayList<>();
            for (String component : type.getNames()) {
                vars.add(LuaAst.LuaVariable(localName(ownerFunction(v), v.getName() + "_" + component), LuaAst.LuaNoExpr()));
            }
            return vars;
        });
    }

    GetAForB<ImFunction, LuaFunction> luaFunc = new GetAForB<ImFunction, LuaFunction>() {

        @Override
        public LuaFunction initFor(ImFunction a) {
            String name = a.getName();
            if (!a.isExtern() && !a.isBj() && !a.isNative()
                && !isFixedEntryPoint(a) && !NamePreservation.isPreserved(a)) {
                name = declarationName(name, LuaHotReload.functionId(a));
            } else if (isFixedEntryPoint(a) || NamePreservation.isPreserved(a)) {
                usedNames.add(name);
            }

            LuaFunction lf = LuaAst.LuaFunction(name, LuaAst.LuaParams(), LuaAst.LuaStatements());
            imFunctionOf.put(lf, a);
            // translate parameters
            for (ImVar p : a.getParameters()) {
                LuaVariable pv = luaVar.getFor(p);
                lf.getParams().add(pv);
            }
            return lf;
        }
    };
    public GetAForB<ImMethod, LuaMethod> luaMethod = new GetAForB<ImMethod, LuaMethod>() {

        @Override
        public LuaMethod initFor(ImMethod a) {
            LuaExpr receiver = LuaAst.LuaExprVarAccess(luaClassVar.getFor(a.attrClass()));
            // A method name is a table key, so it must be an identifier - but unlike a variable
            // it must not be uniqued: every override has to keep landing in the same slot.
            return LuaAst.LuaMethod(receiver, dispatchSlotName(a.getName()), LuaAst.LuaParams(), LuaAst.LuaStatements());
        }
    };


    GetAForB<ImClass, LuaVariable> luaClassVar = new GetAForB<ImClass, LuaVariable>() {
        @Override
        public LuaVariable initFor(ImClass a) {
            return LuaAst.LuaVariable(declarationName(a.getName(), LuaHotReload.classId(a)),
                LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));
        }
    };

    /**
     * Runtime class instances are positive integer ids. Field values live in one static Lua table
     * per canonical IM field, indexed by that id; class descriptors remain static tables and are
     * reached through {@link #objectClass}. Allocation therefore creates no per-instance table.
     *
     * <p>Destroy only removes the live-object descriptor before putting the id on the free stack.
     * Field storage intentionally retains its value, matching the Jass backend's array-backed
     * fields. A stale reference aliases a later object after that id is recycled; before reuse its
     * descriptor is absent, so virtual dispatch fails and {@code instanceof} is false. Capturing
     * closures use the same representation and, like Jass closures, retain their id until destroyed.
     */
    GetAForB<ImVar, LuaVariable> luaFieldStorage = new GetAForB<ImVar, LuaVariable>() {
        @Override
        public LuaVariable initFor(ImVar field) {
            LuaVariable storage = LuaAst.LuaVariable(declarationName(field.getName() + "_storage", LuaHotReload.variableId(field)),
                LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));
            localizableStorageTables.add(storage);
            return storage;
        }
    };

    final LuaVariable objectClass = LuaAst.LuaVariable("__wurst_objectClass",
        LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));
    final LuaVariable objectFree = LuaAst.LuaVariable("__wurst_objectFree",
        LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));
    final LuaVariable objectMax = LuaAst.LuaVariable("__wurst_objectMax", LuaAst.LuaExprIntVal("0"));
    final LuaVariable objectFreeCount = LuaAst.LuaVariable("__wurst_objectFreeCount", LuaAst.LuaExprIntVal("0"));
    final LuaFunction objectDealloc = LuaAst.LuaFunction("__wurst_deallocObject", LuaAst.LuaParams(), LuaAst.LuaStatements());
    final LuaFunction classToIndex = LuaAst.LuaFunction("__wurst_classToIndex", LuaAst.LuaParams(), LuaAst.LuaStatements());
    private final Map<String, LuaVariable> luaLibraries = new HashMap<>();

    /** A reference to a Lua standard library table such as {@code math}; never declared. */
    LuaVariable luaLibrary(String name) {
        return luaLibraries.computeIfAbsent(name, n -> LuaAst.LuaVariable(n, LuaAst.LuaNoExpr()));
    }
    final LuaFunction classFromIndex = LuaAst.LuaFunction("__wurst_classFromIndex", LuaAst.LuaParams(), LuaAst.LuaStatements());

    GetAForB<ImMethod, LuaFunction> luaDispatchFunc = new GetAForB<ImMethod, LuaFunction>() {
        @Override
        public LuaFunction initFor(ImMethod method) {
            LuaVariable receiver = LuaAst.LuaVariable("receiver", LuaAst.LuaNoExpr());
            LuaVariable dots = LuaAst.LuaVariable("...", LuaAst.LuaNoExpr());
            LuaFunction result = LuaAst.LuaFunction(declarationName("dispatch_" + method.getName(), LuaHotReload.methodId(method)),
                LuaAst.LuaParams(receiver, dots), LuaAst.LuaStatements());
            LuaExpr descriptor = LuaAst.LuaExprArrayAccess(
                LuaAst.LuaExprVarAccess(objectClass),
                LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(receiver)));
            // The final slot is resolved after all class descriptors have been emitted. Generic
            // lowering can leave an unspecialized call with a mangled method name while the
            // implementing closure classes register the normalized alias (e.g. Predicate_test
            // vs. test); the descriptor table is the authoritative source for that choice.
            LuaExprFieldAccess target = LuaAst.LuaExprFieldAccess(
                descriptor, isDestroyDispatchMethod(method)
                    ? "__wurst_destroy"
                    : dispatchSlotName(imTr.dispatchSegmentOf(method)));
            LuaExprFunctionCallE call = LuaAst.LuaExprFunctionCallE(target,
                LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(receiver), LuaAst.LuaExprVarAccess(dots)));
            pendingDispatches.add(new PendingDispatch(method, target));
            result.getBody().add(LuaAst.LuaReturn(call));
            luaModel.add(result);
            return result;
        }
    };

    /**
     * A virtual call whose receiver is a plain variable: look the implementation up in the
     * receiver's descriptor right here instead of calling a stub which forwards varargs. The slot
     * is resolved later, together with the stubs, through the same pending-dispatch list.
     */
    LuaExpr inlineDispatchCall(ImMethod method, LuaExpr receiver, LuaExprlist args) {
        LuaExpr descriptor = LuaAst.LuaExprArrayAccess(
            LuaAst.LuaExprVarAccess(objectClass),
            LuaAst.LuaExprlist(receiver.copy()));
        LuaExprFieldAccess target = LuaAst.LuaExprFieldAccess(
            descriptor, isDestroyDispatchMethod(method)
                ? "__wurst_destroy"
                : dispatchSlotName(imTr.dispatchSegmentOf(method)));
        pendingDispatches.add(new PendingDispatch(method, target));
        return LuaAst.LuaExprFunctionCallE(target, args);
    }

    /**
     * The allocator of a class with one of its own ({@link #allocatesOnly}): a plain function, not a method of the
     * class table. An allocation then calls it directly, while a method would cost each allocation a lookup in the
     * class table and pass the table as an argument nothing reads.
     */
    GetAForB<ImClass, LuaFunction> luaClassAllocator = new GetAForB<ImClass, LuaFunction>() {
        @Override
        public LuaFunction initFor(ImClass a) {
            return LuaAst.LuaFunction(declarationName(a.getName() + "_create", LuaHotReload.classId(a)),
                LuaAst.LuaParams(), LuaAst.LuaStatements());
        }
    };

    LuaFunction toIndexFunction = LuaAst.LuaFunction(uniqueName("__wurst_objectToIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());
    /** The int an old-generics int value 0 is stored as; see ExprTranslation.translate(ImCast). */
    private final Lazy<LuaVariable> oldGenericsZero = Lazy.create(() -> LuaPolyfillSetup.createOldGenericsZero(this));
    private final Lazy<LuaPolyfillSetup.OldGenericsHelpers> oldGenericsHelpers =
        Lazy.create(() -> LuaPolyfillSetup.createOldGenericsCastFunctions(this));

    /** Emitted on first use, so a map without old-generics casts carries none of it. */
    LuaVariable oldGenericsZero() {
        return oldGenericsZero.get();
    }

    LuaPolyfillSetup.OldGenericsHelpers oldGenericsHelpers() {
        return oldGenericsHelpers.get();
    }

    /** The hashtable stubs whose helper is defined. Membership only, never iterated. */
    private final Set<ImFunction> definedHashtableHelpers = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Lazy<LuaVariable> hashtableEmpty = Lazy.create(() -> LuaHashtableTranslation.createEmpty(this));
    private final Lazy<LuaFunction> hashtableNewChild = Lazy.create(() -> LuaHashtableTranslation.createNewChild(this));
    private final Lazy<LuaVariable> hashtableFlushed = Lazy.create(() -> LuaHashtableTranslation.createFlushed(this));
    /** Looked up by type, never iterated: the declaration order is the order of first use. */
    private final Map<String, LuaVariable> hashtableAccepts = new HashMap<>();

    /**
     * The helper a call of a hashtable stub calls where the operation cannot be printed in place
     * ({@link LuaHashtableTranslation}), defined with the first such call: a stub which every call prints
     * in place has no definition.
     */
    LuaFunction hashtableHelper(ImFunction stub) {
        LuaFunction helper = luaFunc.getFor(stub);
        if (definedHashtableHelpers.add(stub)) {
            LuaNatives.get(helper);
            luaModel.add(helper);
        }
        return helper;
    }

    /** Main-chunk locals of the in-place hashtable operations, declared on first use. */
    LuaVariable hashtableEmpty() {
        return hashtableEmpty.get();
    }

    LuaFunction hashtableNewChild() {
        return hashtableNewChild.get();
    }

    LuaVariable hashtableFlushed() {
        return hashtableFlushed.get();
    }

    /** The set of tags a handle load of {@code type} accepts, one main-chunk local per type, declared on first use. */
    LuaVariable hashtableAccepts(String type) {
        return hashtableAccepts.computeIfAbsent(type, t -> LuaHashtableTranslation.createAccepts(t, this));
    }

    LuaFunction fromIndexFunction = LuaAst.LuaFunction(uniqueName("__wurst_objectFromIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());
    LuaFunction stringToIndexFunction = LuaAst.LuaFunction(uniqueName("__wurst_stringToIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());
    LuaFunction stringFromIndexFunction = LuaAst.LuaFunction(uniqueName("__wurst_stringFromIndex"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    LuaFunction instanceOfFunction = LuaAst.LuaFunction(uniqueName("isInstanceOf"), LuaAst.LuaParams(), LuaAst.LuaStatements());

    private final Lazy<LuaFunction> errorFunc = Lazy.create(() ->
        this.getProg().getFunctions().stream()
            .flatMap(f -> {
                de.peeeq.wurstscript.ast.Element trace = f.attrTrace();
                if (trace instanceof FuncDef fd) {
                    if (fd.getName().equals("error")
                        && fd.attrNearestPackage() instanceof WPackage) {
                        WPackage p = (WPackage) fd.attrNearestPackage();
                        if (p.getName().equals("ErrorHandling")) {
                            return Stream.of(luaFunc.getFor(f));
                        }
                    }
                }
                return Stream.empty();
            })
            .findFirst().orElse(null));
    final ImTranslator imTr;


    /** Whether this is a {@code -luaHotReload} build ({@link LuaHotReload}). */
    private final boolean hotReload;
    /** In a hot-reload build, the functions translated from the program, whose bodies a payload can replace. */
    private final Map<LuaFunction, ImFunction> hotReloadFunctions = new LinkedHashMap<>();
    private LuaHotReload.@Nullable Manifest hotReloadManifest;

    public LuaTranslator(ImProg prog, ImTranslator imTr) {
        this.prog = prog;
        this.imTr = imTr;
        this.hotReload = imTr.getRunArgs().isLuaHotReload();
        luaModel = LuaAst.LuaCompilationUnit();
    }

    /**
     * The name of a declaration of the program. A hot-reload build adds a hash of the declaration's identity
     * ({@link LuaHotReload#identityHash}), so that a name depends on its declaration alone: numbered names change when a
     * declaration of the same name is added or removed before them, which a payload cannot carry.
     */
    String declarationName(String rawName, String identity) {
        return uniqueName(hotReload ? rawName + "_" + LuaHotReload.identityHash(identity) : rawName);
    }

    /**
     * In a hot-reload build, the names given to the locals of each function, kept per function: numbered across the
     * program, a local added to one function would rename the locals of every function translated after it.
     */
    private final Map<Object, Set<String>> localNamesByScope = new IdentityHashMap<>();
    /** The IM function each Lua function was translated from. */
    private final Map<LuaFunction, ImFunction> imFunctionOf = new IdentityHashMap<>();

    /**
     * The name of a local of {@code scope} (an IM function, or a Lua function the backend made). A hot-reload build
     * numbers it within its function alone, avoiding the names of every global; any other build numbers it across the
     * program, as {@link #uniqueName} does.
     */
    String localName(@Nullable Object scope, String rawName) {
        if (!hotReload || scope == null) {
            return uniqueName(rawName);
        }
        String name = LuaIdentifiers.toIdentifier(rawName);
        Set<String> used = localNamesByScope.computeIfAbsent(scope, k -> new HashSet<>());
        String candidate = name;
        for (int i = 1; usedNames.contains(candidate) || used.contains(candidate); i++) {
            candidate = name + i;
        }
        used.add(candidate);
        localNames.add(candidate);
        return candidate;
    }

    private static @Nullable ImFunction ownerFunction(ImVar v) {
        de.peeeq.wurstscript.jassIm.Element e = v.getParent();
        while (e != null && !(e instanceof ImFunction)) {
            e = e.getParent();
        }
        return (ImFunction) e;
    }

    /** The scope of the locals of a Lua function: the IM function it was translated from, when there is one. */
    private Object scopeOf(Object luaFunction) {
        ImFunction im = luaFunction instanceof LuaFunction lf ? imFunctionOf.get(lf) : null;
        return im != null ? im : luaFunction;
    }

    protected String uniqueName(String rawName) {
        String name = LuaIdentifiers.toIdentifier(rawName);
        Integer nextIndex = uniqueNameCounters.get(name);
        if (nextIndex == null) {
            uniqueNameCounters.put(name, 1);
            if (!localNames.contains(name) && usedNames.add(name)) {
                return name;
            }
            nextIndex = 1;
        }
        String candidate;
        do {
            candidate = name + nextIndex;
            nextIndex++;
        } while (localNames.contains(candidate) || !usedNames.add(candidate));
        uniqueNameCounters.put(name, nextIndex);
        return candidate;
    }

    public LuaCompilationUnit translate() {
        collectPredefinedNames();
        assertNoDanglingFunctionReferences(prog);
        fieldDefaultsOnAllocation = LuaFieldDefaults.moveSurvivorsToAllocations(prog, imTr);

        normalizeFieldNames();


        findClassMetadataReads();
        createObjectManagement();
        if (reflection != null) {
            reflection.declare();
        }
        if (readsSupertypes) {
            createInstanceOfFunction();
        }
        createObjectIndexFunctions();
        createStringIndexFunctions();

        for (ImVar v : prog.getGlobals()) {
            translateGlobal(v);
        }

        Set<LuaVariable> emittedFieldStorage = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ImClass c : prog.getClasses()) {
            for (ImVar field : c.getFields()) {
                LuaVariable storage = fieldStorage(field);
                if (emittedFieldStorage.add(storage)) {
                    luaModel.add(storage);
                }
            }
        }

        // first add class variables
        for (ImClass c : prog.getClasses()) {
            LuaVariable classVar = luaClassVar.getFor(c);
            luaModel.add(classVar);
        }

        if (hotReload) {
            nameFunctionsBeforeTheirBodies();
        }

        for (ImClass c : prog.getClasses()) {
            translateClass(c);
        }

        for (ImFunction f : prog.getFunctions()) {
            translateFunc(f);
        }

        for (ImClass c : prog.getClasses()) {
            initClassTables(c);
        }

        resolveDispatchSlots();
        assertResolvedDispatchSlots();
        dropUnreadDispatchSlots();
        // The tables as they will be printed: every slot a call site reads must still be bound
        // in every descriptor which can receive it.
        assertResolvedDispatchSlots();

        createBootstrapFunction();
        cleanStatements();
        demoteForLoopsOverLocalLimit();
        localizeHotStorageTables();
        enforceLuaLocalLimits();
        LuaAssertions.assertNoDroppedSlotIsRead(luaModel, objectClass, droppedSlots);

        if (imTr.getRunArgs().isOptimize()) {
            Set<String> preserved = new HashSet<>();
            for (ImFunction function : ImHelper.calculateFunctionsOfProg(prog)) {
                if (function.isNative() || function.isBj() || function.isExtern()
                    || NamePreservation.isPreserved(function)) {
                    preserved.add(luaFunc.getFor(function).getName());
                }
            }
            for (ImVar global : prog.getGlobals()) {
                if (global.getIsBJ() || NamePreservation.isPreserved(global)) {
                    preserved.add(luaVar.getFor(global).getName());
                }
            }
            Set<LuaVariable> descriptors = Collections.newSetFromMap(new IdentityHashMap<>());
            for (ImClass c : prog.getClasses()) descriptors.add(luaClassVar.getFor(c));
            Set<LuaExprFieldAccess> targets = Collections.newSetFromMap(new IdentityHashMap<>());
            for (PendingDispatch pending : pendingDispatches) targets.add(pending.target);
            LuaNameCompressor.compress(luaModel, preserved, descriptors, emittedDispatchSlots, targets);
        }

        if (hotReload) {
            startHotReloadPoll();
            Set<ImFunction> runOnce = Collections.newSetFromMap(new IdentityHashMap<>());
            runOnce.addAll(imTr.luaInitFunctions.keySet());
            for (ImFunction f : new ImFunction[] {imTr.getMainFunc(), imTr.getConfFunc(), imTr.getGlobalInitFunc()}) {
                if (f != null) {
                    runOnce.add(f);
                }
            }
            hotReloadManifest = LuaHotReload.install(this, hotReloadFunctions, runOnce, CompileTimeInfo.version);
        }
        return luaModel;
    }

    /** The class functions a hot-reload build named before translating any body. */
    private final Set<ImFunction> classFunctionsNamed = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Locals and functions share one namespace of numbered names, and a function is otherwise named when it is first
     * called or after its class's bodies are translated. So a local added to one body could take the name of a function
     * named later, which would change the script outside the bodies. A hot-reload build names every function first.
     */
    private void nameFunctionsBeforeTheirBodies() {
        for (ImClass c : prog.getClasses()) {
            for (ImFunction f : c.getFunctions()) {
                luaFunc.getFor(f).setName(declarationName(c.getName() + "_" + classFunctionName(f), LuaHotReload.functionId(f)));
                classFunctionsNamed.add(f);
            }
        }
        for (ImFunction f : prog.getFunctions()) {
            luaFunc.getFor(f);
        }
    }

    /** What a {@code -luaHotReload} build emitted, null in any other build. */
    public LuaHotReload.@Nullable Manifest hotReloadManifest() {
        return hotReloadManifest;
    }

    /** Main polls for a payload once a second, after the bootstrap, which the poll's natives need. */
    private void startHotReloadPoll() {
        ImFunction mainIm = imTr.getMainFunc();
        if (mainIm == null) {
            return;
        }
        LuaStatements body = luaFunc.getFor(mainIm).getBody();
        body.add(bootstrapFunction == null ? 0 : 1,
            LuaAst.LuaLiteral("TimerStart(CreateTimer(), 1.0, true, " + LuaHotReload.POLL_FUNCTION + ")"));
    }

    /**
     * Function references need an xpcall boundary, but the boundary is a property of the referenced
     * function rather than of each expression which names it. Emit one reusable adapter per target
     * so evaluating a function reference performs no closure allocation.
     */
    LuaFunction callbackAdapterFor(ImFunction target) {
        LuaFunction existing = callbackAdapters.get(target);
        if (existing != null) {
            return existing;
        }

        LuaFunction targetLua = luaFunc.getFor(target);
        LuaFunction adapter = LuaAst.LuaFunction(
            uniqueName("__wurst_callback_" + targetLua.getName()),
            LuaAst.LuaParams(), LuaAst.LuaStatements());
        callbackAdapters.put(target, adapter);

        LuaFunction errorHandler = callbackErrorHandler();
        LuaExprlist xpcallArgs = LuaAst.LuaExprlist(
            LuaAst.LuaExprFuncRef(targetLua),
            LuaAst.LuaExprFuncRef(errorHandler));
        // A Wurst code value takes no parameters, so whatever the caller passes is dropped by the
        // target anyway: such an adapter takes no varargs. Only a target with parameters needs them.
        if (!targetLua.getParams().isEmpty()) {
            LuaVariable dots = LuaAst.LuaVariable("...", LuaAst.LuaNoExpr());
            adapter.getParams().add(dots);
            xpcallArgs.add(LuaAst.LuaExprVarAccess(dots));
        }
        LuaExprFunctionCallByName xpcall = LuaAst.LuaExprFunctionCallByName("xpcall", xpcallArgs);
        if (target.getReturnType() instanceof ImVoid) {
            adapter.getBody().add(xpcall);
        } else {
            // Keep exactly the first callback result. Returning select(2, xpcall(...)) directly
            // could leak additional Lua return values into a surrounding argument list.
            LuaVariable ignored = LuaAst.LuaVariable("_", LuaAst.LuaNoExpr());
            LuaVariable result = LuaAst.LuaVariable("result", LuaAst.LuaNoExpr());
            adapter.getBody().add(ignored);
            adapter.getBody().add(result);
            adapter.getBody().add(LuaAst.LuaAssignment(
                LuaAst.LuaLiteral("_, result"), xpcall));
            adapter.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(result)));
        }
        luaModel.add(adapter);
        return adapter;
    }

    private LuaFunction callbackErrorHandler() {
        if (callbackErrorHandler != null) {
            return callbackErrorHandler;
        }
        LuaVariable err = LuaAst.LuaVariable("err", LuaAst.LuaNoExpr());
        callbackErrorHandler = LuaAst.LuaFunction(uniqueName("__wurst_callback_error"),
            LuaAst.LuaParams(err), LuaAst.LuaStatements());
        callbackErrorHandler.getBody().add(LuaAst.LuaLiteral(
            "if err == \"" + ExprTranslation.WURST_ABORT_THREAD_SENTINEL + "\" then return end"));
        callbackErrorHandler.getBody().add(LuaAst.LuaLiteral(
            "BJDebugMsg(\"lua callback error: \" .. tostring(err))"));
        callbackErrorHandler.getBody().add(LuaAst.LuaLiteral(
            "xpcall(function() " + ExprTranslation.callErrorFunc(this, "tostring(err)",
                "in lua callback error handler")
                + " end, function(err2) if err2 == \"" + ExprTranslation.WURST_ABORT_THREAD_SENTINEL
                + "\" then return end BJDebugMsg(\"error reporting error: \" .. tostring(err2))"
                + " BJDebugMsg(\"while reporting: \" .. tostring(err)) end)"));
        luaModel.add(callbackErrorHandler);
        return callbackErrorHandler;
    }

    /** The handler every package init is called with ({@code StmtTranslation.emitLuaInitXpcall}), defined once. */
    LuaFunction initErrorHandler() {
        if (initErrorHandler != null) {
            return initErrorHandler;
        }
        LuaVariable err = LuaAst.LuaVariable("err", LuaAst.LuaNoExpr());
        initErrorHandler = LuaAst.LuaFunction(uniqueName("__wurst_init_error"),
            LuaAst.LuaParams(err), LuaAst.LuaStatements());
        initErrorHandler.getBody().add(LuaAst.LuaLiteral(
            "if err == \"" + ExprTranslation.WURST_ABORT_THREAD_SENTINEL + "\" then return end"));
        initErrorHandler.getBody().add(LuaAst.LuaLiteral(
            "BJDebugMsg(\"lua init error: \" .. tostring(err))"));
        initErrorHandler.getBody().add(LuaAst.LuaLiteral(
            "xpcall(function() " + ExprTranslation.callErrorFunc(this, "tostring(err)")
                + " end, function(err2) if err2 == \"" + ExprTranslation.WURST_ABORT_THREAD_SENTINEL
                + "\" then return end BJDebugMsg(\"error reporting error: \" .. tostring(err2)) end)"));
        luaModel.add(initErrorHandler);
        return initErrorHandler;
    }

    /**
     * Rejects calls/references to functions that an earlier optimizer pass
     * detached from the IM program. Without this invariant the Lua printer
     * can emit a valid-looking call with no corresponding definition, which
     * only fails once Warcraft executes that path.
     */
    static void assertNoDanglingFunctionReferences(ImProg prog) {
        Set<ImFunction> rootedFunctions = Collections.newSetFromMap(new IdentityHashMap<>());
        rootedFunctions.addAll(ImHelper.calculateFunctionsOfProg(prog));
        prog.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
            private void requireRooted(de.peeeq.wurstscript.jassIm.Element reference, ImFunction target) {
                if (!rootedFunctions.contains(target)) {
                    throw new Error("Lua IM contains a dangling reference to removed function '"
                        + target.getName() + "' in " + reference);
                }
            }

            @Override
            public void visit(ImFunctionCall call) {
                requireRooted(call, call.getFunc());
                super.visit(call);
            }

            @Override
            public void visit(ImFuncRef ref) {
                requireRooted(ref, ref.getFunc());
                super.visit(ref);
            }

            @Override
            public void visit(ImMethod method) {
                requireRooted(method, method.getImplementation());
                super.visit(method);
            }
        });
    }

    void deferMainInit(LuaStatement statement) {
        deferredMainInit.add(statement);
    }

    /**
     * Moves all deferred initialization (global defaults, class dispatch tables,
     * typecasting maps) into a guarded bootstrap function that is called at the
     * top of BOTH entry points. WC3 calls config() before main(), so config must
     * see initialized globals too. The statements cannot run at the root of the
     * script because WC3 natives are not available there yet.
     */
    private void createBootstrapFunction() {
        if (deferredMainInit.isEmpty()) {
            return;
        }
        ImFunction mainIm = imTr.getMainFunc();
        ImFunction confIm = imTr.getConfFunc();
        if (mainIm == null && confIm == null) {
            return;
        }
        // nil until the bootstrap runs, which is how a global Lua has not seen reads: it needs no declaration
        LuaVariable doneFlag = LuaAst.LuaVariable(uniqueName("__wurst_bootstrap_done"), LuaAst.LuaNoExpr());
        LuaFunction boot = LuaAst.LuaFunction(uniqueName("__wurst_init_bootstrap"), LuaAst.LuaParams(), LuaAst.LuaStatements());
        bootstrapFunction = boot;
        boot.getBody().add(LuaAst.LuaLiteral("if " + doneFlag.getName() + " then return end"));
        boot.getBody().add(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(doneFlag), LuaAst.LuaExprBoolVal(true)));
        List<LuaStatement> stmts = new ArrayList<>();
        while (!deferredMainInit.isEmpty()) {
            stmts.add(deferredMainInit.remove(deferredMainInit.size() - 1));
        }
        Collections.reverse(stmts);
        for (LuaStatement stmt : stmts) {
            boot.getBody().add(stmt);
        }
        luaModel.add(boot);

        insertBootstrapCall(mainIm, boot);
        insertBootstrapCall(confIm, boot);
    }

    private void insertBootstrapCall(ImFunction entryPoint, LuaFunction boot) {
        if (entryPoint == null) {
            return;
        }
        LuaFunction lf = luaFunc.getFor(entryPoint);
        lf.getBody().add(0, LuaAst.LuaExprFunctionCall(boot, LuaAst.LuaExprlist()));
    }

    // Assertion helpers are implemented in LuaAssertions; kept here as public entry points
    // for callers that reference LuaTranslator directly.
    public static void assertNoLeakedGetHandleIdCalls(String luaCode) {
        LuaAssertions.assertNoLeakedGetHandleIdCalls(luaCode);
    }

    public static void assertNoLeakedHashtableNativeCalls(String luaCode) {
        LuaAssertions.assertNoLeakedHashtableNativeCalls(luaCode);
    }

    static List<String> allHashtableNativeNames() {
        List<String> result = new ArrayList<>(HASHTABLE_NATIVE_NAMES_RAW);
        result.addAll(HASHTABLE_HANDLE_SAVE_NAMES);
        result.addAll(HASHTABLE_HANDLE_LOAD_NAMES);
        return result;
    }

    private boolean isFixedEntryPoint(ImFunction function) {
        return function == imTr.getMainFunc() || function == imTr.getConfFunc();
    }

    private void collectPredefinedNames() {
        for (ImFunction function : prog.getFunctions()) {
            if (function.isBj() || function.isExtern() || function.isNative()
                || NamePreservation.isPreserved(function)) {
                // Don't rename Wurst-internal stubs (names starting with __wurst_)
                // since their names are intentionally different from their trace's source name.
                if (!function.getName().startsWith("__wurst_")) {
                    setNameFromTrace(function);
                }
                usedNames.add(function.getName());
            }
        }

        for (ImVar global : prog.getGlobals()) {
            if (global.getIsBJ()) {
                setNameFromTrace(global);
                usedNames.add(global.getName());
            } else if (NamePreservation.isPreserved(global)) {
                usedNames.add(global.getName());
            }
        }
    }

    private void setNameFromTrace(JassImElementWithName named) {
        de.peeeq.wurstscript.ast.Element trace = named.attrTrace();
        if (trace instanceof NameDef nameDef) {
            named.setName(nameDef.getName());
        }
    }

    private void normalizeFieldNames() {
        Map<ImVar, Set<String>> namesToAvoid = collectNamesEachFieldMustAvoid();
        Map<ImVar, String> chosenNames = new IdentityHashMap<>();
        Set<ImClass> processed = new HashSet<>();
        for (ImClass c : prog.getClasses()) {
            normalizeFieldNames(c, processed, namesToAvoid, chosenNames);
        }
    }

    /**
     * The method names a field has to keep clear of, gathered per original field rather than per
     * class.
     * <p>
     * A specialised class holds a copy of each field, and the accesses reaching either still name the
     * original's variable, so the two must end up as one table key. They cannot be normalised
     * independently: the classes need not hold the same methods once unused ones are dropped, and a
     * specialisation has slots of its own that the original never had. Naming each side around only
     * its own methods leaves them different; restoring the original's name afterwards puts back
     * whatever collision the specialisation had escaped, and an instance field which shadows a method
     * slot is found first by a virtual call, which then tries to call a field.
     * <p>
     * One name chosen against the methods of the original and of every specialisation is safe on all
     * of them.
     */
    private Map<ImVar, Set<String>> collectNamesEachFieldMustAvoid() {
        Map<ImVar, Set<String>> namesToAvoid = new IdentityHashMap<>();
        for (ImClass c : prog.getClasses()) {
            Set<String> reserved = new HashSet<>(LuaReservedNames.LUA_KEYWORDS);
            collectMethodNames(c, reserved, new HashSet<>());
            for (ImVar field : c.getFields()) {
                namesToAvoid
                    .computeIfAbsent(imTr.canonical(field), origin -> new HashSet<>())
                    .addAll(reserved);
            }
        }
        return namesToAvoid;
    }

    private void normalizeFieldNames(ImClass c, Set<ImClass> processed,
                                     Map<ImVar, Set<String>> namesToAvoid, Map<ImVar, String> chosenNames) {
        if (!processed.add(c)) {
            return;
        }
        // Superclasses first: all fields of a hierarchy share one instance table,
        // so a subclass field must be renamed around already-final ancestor names.
        for (ImClassType sc : c.getSuperClasses()) {
            normalizeFieldNames(sc.getClassDef(), processed, namesToAvoid, chosenNames);
        }
        // Field names become raw Lua table keys / field accesses, so they must not
        // collide with Lua keywords, method dispatch slots, or inherited fields.
        Set<String> reserved = new HashSet<>(LuaReservedNames.LUA_KEYWORDS);
        collectMethodNames(c, reserved, new HashSet<>());
        collectSuperFieldNames(c, reserved, new HashSet<>());
        for (ImVar field : c.getFields()) {
            ImVar origin = imTr.canonical(field);
            String settled = chosenNames.get(origin);
            if (settled != null) {
                // The original and its copies are one key, decided the first time any of them is met.
                field.setName(settled);
                reserved.add(settled);
                continue;
            }
            Set<String> avoid = new HashSet<>(reserved);
            avoid.addAll(namesToAvoid.getOrDefault(origin, Collections.emptySet()));
            if (avoid.contains(field.getName())) {
                String base = field.getName() + "_field";
                String candidate = base;
                int i = 1;
                while (avoid.contains(candidate)) {
                    candidate = base + i++;
                }
                field.setName(candidate);
            }
            chosenNames.put(origin, field.getName());
            reserved.add(field.getName());
        }
    }

    private void collectSuperFieldNames(ImClass c, Set<String> out, Set<ImClass> visited) {
        if (!visited.add(c)) {
            return;
        }
        for (ImClassType sc : c.getSuperClasses()) {
            ImClass superClass = sc.getClassDef();
            for (ImVar field : superClass.getFields()) {
                out.add(field.getName());
            }
            collectSuperFieldNames(superClass, out, visited);
        }
    }

    private void collectMethodNames(ImClass c, Set<String> methodNames, Set<ImClass> visited) {
        if (visited.contains(c)) {
            return;
        }
        visited.add(c);
        for (ImMethod method : c.getMethods()) {
            methodNames.add(dispatchSlotName(method.getName()));
        }
        for (ImClassType sc : c.getSuperClasses()) {
            collectMethodNames(sc.getClassDef(), methodNames, visited);
        }
    }

    /**
     * The class tables carry metadata for three reads alone, {@code instanceof}, the type id of an object and the
     * natives of {@code Reflection}; the program as it is translated says whether it has any.
     */
    private void findClassMetadataReads() {
        List<ImFunctionCall> reflectionCalls = new ArrayList<>();
        prog.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
            @Override
            public void visit(ImInstanceof e) {
                super.visit(e);
                readsSupertypes = true;
            }

            @Override
            public void visit(ImTypeIdOfObj e) {
                super.visit(e);
                readsObjectTypeId = true;
            }

            @Override
            public void visit(ImFunctionCall e) {
                super.visit(e);
                if (LuaReflection.isReflectionNative(e.getFunc())) {
                    reflectionCalls.add(e);
                }
            }
        });
        if (reflectionCalls.isEmpty()) {
            return;
        }
        LuaReflection r = new LuaReflection(this);
        for (ImFunctionCall call : reflectionCalls) {
            r.noteCall(call);
        }
        prog.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
            @Override
            public void visit(ImAlloc e) {
                super.visit(e);
                r.noteAllocation(e.getClazz().getClassDef());
            }
        });
        reflection = r;
    }

    private void createInstanceOfFunction() {
        LuaPolyfillSetup.createInstanceOfFunction(this);
    }

    /** For each class, the fields whose default its allocation writes ({@link LuaFieldDefaults}). */
    private Map<ImClass, Set<ImVar>> fieldDefaultsOnAllocation = Collections.emptyMap();
    /** Whether the program has an {@code instanceof}, the only reader of a class table's supertypes. */
    private boolean readsSupertypes;
    /** Whether the program reads the type id of an object, the only reader of a class table's type id. */
    private boolean readsObjectTypeId;
    /** The natives of {@code Reflection}, null when the program calls none. */
    private @Nullable LuaReflection reflection;
    private final Map<ImClass, Boolean> allocatesOnly = new IdentityHashMap<>();
    private final Lazy<LuaFunction> objectAlloc = Lazy.create(this::createObjectAlloc);

    LuaVariable fieldStorage(ImVar field) {
        return luaFieldStorage.getFor(imTr.canonical(field));
    }

    /** How many main-chunk locals lead the model; see {@link #declareChunkLocal}. */
    private int chunkLocalCount;

    /**
     * Declares a local of the main chunk at the top of the script, before every function, so that
     * each function reaches it as an upvalue instead of looking it up among the thousands of globals.
     * Only the runtime state read on hot paths belongs here: the main chunk has room for 200 locals
     * and a function for 255 upvalues. Declared now: the four object-allocator variables, the
     * deallocator and, when used, the old-generics zero sentinel.
     */
    void declareChunkLocal(LuaDefinition definition) {
        // A payload is compiled as a chunk of its own, which cannot see the locals of the map's chunk: a hot-reload
        // build keeps this state in globals, which a replaced body reaches by the same name.
        luaModel.add(chunkLocalCount++, hotReload ? definition : LuaAst.LuaChunkLocal(definition));
    }

    private void createObjectManagement() {
        // Every allocation and destroy reads these. The descriptor map is not aliased into function
        // locals like the field stores (localizeHotStorageTables): Lua indexes an upvalue table in
        // one instruction (GETTABUP), as it does a local one, so an alias would only add a copy.
        declareChunkLocal(objectClass);
        declareChunkLocal(objectFree);
        declareChunkLocal(objectMax);
        declareChunkLocal(objectFreeCount);

        addDeallocation(objectDealloc);
        // Called by every destroy, so a local function like the state it pushes onto.
        declareChunkLocal(objectDealloc);

        LuaVariable toIndexObject = LuaAst.LuaVariable("object", LuaAst.LuaNoExpr());
        classToIndex.getParams().add(toIndexObject);
        classToIndex.getBody().add(LuaAst.LuaIf(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(toIndexObject), LuaAst.LuaOpEquals(), LuaAst.LuaExprNull()),
            LuaAst.LuaStatements(LuaAst.LuaReturn(LuaAst.LuaExprIntVal("0"))),
            LuaAst.LuaStatements()));
        classToIndex.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(toIndexObject)));
        luaModel.add(classToIndex);

        LuaVariable fromIndexValue = LuaAst.LuaVariable("index", LuaAst.LuaNoExpr());
        classFromIndex.getParams().add(fromIndexValue);
        classFromIndex.getBody().add(LuaAst.LuaIf(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(fromIndexValue), LuaAst.LuaOpEquals(), LuaAst.LuaExprIntVal("0")),
            LuaAst.LuaStatements(LuaAst.LuaReturn(LuaAst.LuaExprNull())),
            LuaAst.LuaStatements()));
        classFromIndex.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(fromIndexValue)));
        luaModel.add(classFromIndex);
    }

    /**
     * Gives {@code dealloc} the parameter {@code object} and a body which destroys it: the object's descriptor is
     * removed and its id pushed onto the free stack, or a double free is an error. Returns the local which holds the
     * descriptor.
     */
    LuaVariable addDeallocation(LuaFunction dealloc) {
        LuaVariable object = LuaAst.LuaVariable("object", LuaAst.LuaNoExpr());
        dealloc.getParams().add(object);
        LuaVariable descriptor = LuaAst.LuaVariable("descriptor",
            LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(objectClass),
                LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(object))));
        dealloc.getBody().add(descriptor);
        dealloc.getBody().add(LuaAst.LuaIf(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(descriptor), LuaAst.LuaOpEquals(), LuaAst.LuaExprNull()),
            LuaAst.LuaStatements(LuaAst.LuaExprFunctionCallByName("error",
                LuaAst.LuaExprlist(LuaAst.LuaExprStringVal("Double free or invalid Wurst object.")))),
            LuaAst.LuaStatements()));
        dealloc.getBody().add(LuaAst.LuaAssignment(
            LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(objectClass),
                LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(object))),
            LuaAst.LuaExprNull()));
        dealloc.getBody().add(LuaAst.LuaAssignment(
            LuaAst.LuaExprVarAccess(objectFreeCount),
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(objectFreeCount), LuaAst.LuaOpPlus(), LuaAst.LuaExprIntVal("1"))));
        dealloc.getBody().add(LuaAst.LuaAssignment(
            LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(objectFree),
                LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(objectFreeCount))),
            LuaAst.LuaExprVarAccess(object)));
        return descriptor;
    }

    /** What destroys an object whose class is {@code c} or a subclass of it. */
    LuaFunction deallocFor(ImClass c) {
        return reflection == null ? objectDealloc : reflection.deallocFor(c);
    }

    /** The natives of {@code Reflection}, null when the program calls none. */
    @Nullable LuaReflection reflection() {
        return reflection;
    }

    private void createObjectIndexFunctions() {
        LuaPolyfillSetup.createObjectIndexFunctions(this);
    }

    private void createStringIndexFunctions() {
        LuaPolyfillSetup.createStringIndexFunctions(this);
    }

    private void cleanStatements() {
        luaModel.accept(new LuaModel.DefaultVisitor() {
            @Override
            public void visit(LuaStatements stmts) {
                super.visit(stmts);
                cleanStatements(stmts);
            }

        });
    }

    private void cleanStatements(LuaStatements stmts) {
        ListIterator<LuaStatement> it = stmts.listIterator();
        while (it.hasNext()) {
            LuaStatement s = it.next();
            if (s instanceof LuaExprNull) {
                it.remove();
            } else if (s instanceof LuaExpr e) {
                boolean parenthesisedCall = e instanceof LuaExprFunctionCallE luaExprFunctionCallE
                    && !LuaPrinter.startsWithName(luaExprFunctionCallE.getFuncExpr());
                if (!(e instanceof LuaCallExpr || e instanceof LuaLiteral) || parenthesisedCall) {
                    e.setParent(null);
                    LuaVariable exprTemp = LuaAst.LuaVariable("wurstExpr", e);
                    it.set(exprTemp);
                }
            }
        }
    }

    private void translateFunc(ImFunction f) {
        if (f.isBj()) {
            // do not translate blizzard functions
            return;
        }
        if (f.isNative() && ExprTranslation.isBackendIntrinsic(f, this)) {
            return;
        }
        if (LuaReflection.isReflectionNative(f)) {
            // Printed where it is called (LuaReflection).
            return;
        }
        if (LuaHashtable.op(imTr, f) != null) {
            // Printed where it is called; a call which cannot be printed so defines it (hashtableHelper).
            return;
        }
        LuaFunction lf = luaFunc.getFor(f);
        if (f.isNative()) {
            LuaNatives.get(lf);
        } else {
            if (rewriteGetHandleIdCompatFunction(f, lf)) {
                luaModel.add(lf);
                return;
            }
            if (rewriteTypeCastingCompatFunction(f, lf)) {
                luaModel.add(lf);
                return;
            }


            if (f.hasFlag(FunctionFlagEnum.IS_VARARG)) {
                LuaVariable lastParam = luaVar.getFor(Utils.getLast(f.getParameters()));
                lastParam.setName("...");
            }

            // translate local variables
            for (ImVar local : f.getLocals()) {
                if (LuaMultipleResults.isResultsLocal(local)) {
                    List<ImType> types = ((ImTupleType) local.getType()).getTypes();
                    List<LuaVariable> components = resultVars(local);
                    for (int i = 0; i < types.size(); i++) {
                        components.get(i).setInitialValue(defaultValue(types.get(i)));
                        lf.getBody().add(components.get(i));
                    }
                    continue;
                }
                LuaVariable luaLocal = luaVar.getFor(local);
                luaLocal.setInitialValue(defaultValue(local.getType()));
                lf.getBody().add(luaLocal);
            }

            // translate body:
            translateStatements(lf.getBody(), f.getBody());
            // local-limit enforcement is done after final statement cleanup,
            // because cleanup and later rewrites can still introduce locals.
        }

        if (f.isExtern() || f.isNative()) {
            String name = lf.getName();
            if (name.startsWith("__wurst_")) {
                // Wurst-internal natives are never pre-defined by the WC3 runtime; emit directly.
                luaModel.add(lf);
            } else {
                // only add the function if it is not yet defined by the WC3 runtime:
                luaModel.add(LuaAst.LuaIf(
                    LuaAst.LuaExprFuncRef(lf),
                    LuaAst.LuaStatements(),
                    LuaAst.LuaStatements(
                        LuaAst.LuaAssignment(LuaAst.LuaLiteral(name), LuaAst.LuaExprFunctionAbstraction(
                            lf.getParams().copy(),
                            lf.getBody().copy()
                        ))
                    )
                ));
            }
        } else {
            luaModel.add(lf);
            if (hotReload) {
                hotReloadFunctions.put(lf, f);
            }
        }
    }

    private boolean rewriteGetHandleIdCompatFunction(ImFunction f, LuaFunction lf) {
        if (f.getParameters().size() != 1 || !f.getName().endsWith("_getHandleId") || f.getName().endsWith("_getTCHandleId")) {
            return false;
        }
        ImVar firstParam = f.getParameters().get(0);
        // Restrict to WC3 simple handle types. User-defined Wurst classes use ImClassType
        // and must not have their function body replaced.
        if (!LuaNativeLowering.isHandleType(firstParam.getType())) {
            return false;
        }
        LuaExpr arg = LuaAst.LuaExprVarAccess(luaVar.getFor(firstParam));
        // Shim opaque runtime handles; keep native GetHandleId for enum-like handles.
        String targetFunction = LuaNativeLowering.usesLuaObjectIdentityHandleId(firstParam.getType())
            ? "__wurst_GetHandleId" : "GetHandleId";
        lf.getBody().clear();
        lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCallByName(targetFunction, LuaAst.LuaExprlist(arg))));
        return true;
    }

    private boolean rewriteTypeCastingCompatFunction(ImFunction f, LuaFunction lf) {
        if (f.getParameters().isEmpty()) {
            return false;
        }
        String tcFunc = getTypeCastingFunctionName(f);
        if (tcFunc == null) {
            return false;
        }
        ImVar firstParam = f.getParameters().get(0);
        LuaExpr arg = LuaAst.LuaExprVarAccess(luaVar.getFor(firstParam));

        if (firstParam.getType() instanceof ImClassType && TypesHelper.isIntType(f.getReturnType())) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaIf(
                LuaAst.LuaExprBinary(arg.copy(), LuaAst.LuaOpEquals(), LuaAst.LuaExprNull()),
                LuaAst.LuaStatements(LuaAst.LuaReturn(LuaAst.LuaExprIntVal("0"))),
                LuaAst.LuaStatements()));
            lf.getBody().add(LuaAst.LuaReturn(arg));
            return true;
        }
        if (TypesHelper.isIntType(firstParam.getType()) && f.getReturnType() instanceof ImClassType) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaIf(
                LuaAst.LuaExprBinary(arg.copy(), LuaAst.LuaOpEquals(), LuaAst.LuaExprIntVal("0")),
                LuaAst.LuaStatements(LuaAst.LuaReturn(LuaAst.LuaExprNull())),
                LuaAst.LuaStatements()));
            lf.getBody().add(LuaAst.LuaReturn(arg));
            return true;
        }

        if ("objectToIndex".equals(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(
                LuaAst.LuaExprFunctionCall(toIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        if ("objectFromIndex".equals(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(
                LuaAst.LuaExprFunctionCall(fromIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }

        if ("stringToIndex".equals(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(stringToIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        if ("stringFromIndex".equals(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(stringFromIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        // Keep semantic conversions for primitive/index-domain helpers intact.
        if ("realToIndex".equals(tcFunc) || "realFromIndex".equals(tcFunc)
            || "playerToIndex".equals(tcFunc) || "playerFromIndex".equals(tcFunc)
            || "booleanToIndex".equals(tcFunc) || "booleanFromIndex".equals(tcFunc)) {
            return false;
        }
        if (LUA_HANDLE_TO_INDEX.contains(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(toIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        if (LUA_HANDLE_FROM_INDEX.contains(tcFunc)) {
            lf.getBody().clear();
            lf.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprFunctionCall(fromIndexFunction, LuaAst.LuaExprlist(arg))));
            return true;
        }
        return false;
    }

    private void enforceLuaLocalLimits() {
        luaModel.accept(new LuaModel.DefaultVisitor() {
            @Override
            public void visit(LuaFunction f) {
                super.visit(f);
                spillLocalsIntoTableIfNeeded(f, f.getName(), f.getParams(), f.getBody());
            }

            @Override
            public void visit(LuaMethod m) {
                super.visit(m);
                spillLocalsIntoTableIfNeeded(m, m.getName(), m.getParams(), m.getBody());
            }
        });
    }

    /** Registers a numeric for loop occupies while it runs: three hidden control values and the variable. */
    private static final int LUA_FOR_LOOP_REGISTERS = 4;

    private static int forLoopRegisters(de.peeeq.wurstscript.luaAst.Element e) {
        if (e instanceof LuaExprFunctionAbstraction || e instanceof LuaFunction || e instanceof LuaMethod) {
            return 0;
        }
        int[] count = {e instanceof LuaFor ? LUA_FOR_LOOP_REGISTERS : 0};
        e.forEachElement(child -> count[0] += forLoopRegisters(child));
        return count[0];
    }

    /**
     * A numeric for loop needs registers which the counted locals do not show. Where they would push
     * a function over the Lua limit, the loop goes back to the while form it was recognised from, so
     * the locals-table fallback never has to spill a for-loop variable (which it cannot rewrite).
     */
    private void demoteForLoopsOverLocalLimit() {
        luaModel.accept(new LuaModel.DefaultVisitor() {
            @Override
            public void visit(LuaFunction f) {
                super.visit(f);
                demoteForLoopsOverLocalLimit(f.getParams(), f.getBody());
            }

            @Override
            public void visit(LuaMethod m) {
                super.visit(m);
                demoteForLoopsOverLocalLimit(m.getParams(), m.getBody());
            }
        });
    }

    private void demoteForLoopsOverLocalLimit(LuaParams params, LuaStatements body) {
        int registers = forLoopRegisters(body);
        if (registers == 0) {
            return;
        }
        int localCount = params.size() + collectFunctionScopeLocals(body).size() + registers;
        if (localCount < LUA_LOCALS_LIMIT) {
            return;
        }
        List<LuaFor> loops = new ArrayList<>();
        collectForLoops(body, loops);
        for (LuaFor loop : loops) {
            LuaNumericFor.demote(loop);
        }
    }

    private static void collectForLoops(de.peeeq.wurstscript.luaAst.Element e, List<LuaFor> out) {
        if (e instanceof LuaExprFunctionAbstraction || e instanceof LuaFunction || e instanceof LuaMethod) {
            return;
        }
        if (e instanceof LuaFor luaFor) {
            out.add(luaFor);
        }
        e.forEachElement(child -> collectForLoops(child, out));
    }

    /**
     * Cache compiler-owned array and field-storage tables in function locals when they are indexed
     * from a loop. This removes a global lookup from every dynamic iteration without changing cold
     * paths or making assumptions about user-authored Lua tables. The cap and headroom avoid turning
     * the optimization into register pressure or immediately triggering the locals-table fallback.
     */
    private void localizeHotStorageTables() {
        luaModel.accept(new LuaModel.DefaultVisitor() {
            @Override
            public void visit(LuaFunction f) {
                super.visit(f);
                localizeHotStorageTables(f, f.getParams(), f.getBody());
            }

            @Override
            public void visit(LuaMethod m) {
                super.visit(m);
                localizeHotStorageTables(m, m.getParams(), m.getBody());
            }
        });
    }

    private void localizeHotStorageTables(Object owner, LuaParams params, LuaStatements body) {
        Map<LuaVariable, Integer> loopAccessCounts = new IdentityHashMap<>();
        collectLoopStorageAccesses(body, false, loopAccessCounts);
        if (loopAccessCounts.isEmpty()) {
            return;
        }

        int existingLocals = params.size() + collectFunctionScopeLocals(body).size() + forLoopRegisters(body);
        int aliasCount = Math.min(LUA_STORAGE_ALIAS_LIMIT,
            LUA_STORAGE_ALIAS_LOCAL_BUDGET - existingLocals);
        if (aliasCount <= 0) {
            return;
        }

        List<LuaVariable> selected = new ArrayList<>(loopAccessCounts.keySet());
        selected.sort(Comparator.<LuaVariable>comparingInt(loopAccessCounts::get).reversed()
            .thenComparing(LuaVariable::getName));
        if (selected.size() > aliasCount) {
            selected = new ArrayList<>(selected.subList(0, aliasCount));
        }

        Map<LuaVariable, LuaVariable> aliases = new IdentityHashMap<>();
        for (LuaVariable storage : selected) {
            aliases.put(storage, LuaAst.LuaVariable(localName(scopeOf(owner), storage.getName() + "_local"),
                LuaAst.LuaExprVarAccess(storage)));
        }
        rewriteStorageAccesses(body, aliases);

        int insertionIndex = bootstrapCallInsertionIndex(body);
        for (LuaVariable storage : selected) {
            body.add(insertionIndex++, aliases.get(storage));
        }
    }

    private void collectLoopStorageAccesses(de.peeeq.wurstscript.luaAst.Element element, boolean insideLoop,
                                            Map<LuaVariable, Integer> counts) {
        if (element instanceof LuaExprFunctionAbstraction
            || element instanceof LuaFunction || element instanceof LuaMethod) {
            return;
        }
        boolean childInsideLoop = insideLoop || element instanceof LuaWhile || element instanceof LuaFor;
        if (childInsideLoop && element instanceof LuaExprArrayAccess luaExprArrayAccess) {
            LuaExpr left = luaExprArrayAccess.getLeft();
            if (left instanceof LuaExprVarAccess luaExprVarAccess) {
                LuaVariable storage = luaExprVarAccess.getVar();
                if (localizableStorageTables.contains(storage)) {
                    counts.merge(storage, 1, Integer::sum);
                }
            }
        }
        element.forEachElement(child -> collectLoopStorageAccesses(child, childInsideLoop, counts));
    }

    private void rewriteStorageAccesses(de.peeeq.wurstscript.luaAst.Element element,
                                        Map<LuaVariable, LuaVariable> aliases) {
        if (element instanceof LuaExprFunctionAbstraction
            || element instanceof LuaFunction || element instanceof LuaMethod) {
            return;
        }
        if (element instanceof LuaExprArrayAccess luaExprArrayAccess) {
            LuaExpr left = luaExprArrayAccess.getLeft();
            if (left instanceof LuaExprVarAccess luaExprVarAccess) {
                LuaVariable alias = aliases.get(luaExprVarAccess.getVar());
                if (alias != null) {
                    left.replaceBy(LuaAst.LuaExprVarAccess(alias));
                }
            }
        }
        element.forEachElement(child -> rewriteStorageAccesses(child, aliases));
    }

    private int bootstrapCallInsertionIndex(LuaStatements body) {
        if (bootstrapFunction != null && !body.isEmpty() && body.get(0) instanceof LuaExprFunctionCall
            && ((LuaExprFunctionCall) body.get(0)).getFunc() == bootstrapFunction) {
            return 1;
        }
        return 0;
    }

    private void spillLocalsIntoTableIfNeeded(Object owner, String functionName, LuaParams params, LuaStatements body) {
        List<LuaVariable> scopeLocals = collectFunctionScopeLocals(body);
        int localCount = params.size() + scopeLocals.size() + forLoopRegisters(body);
        if (DEBUG_LUA_LOCALS) {
            WLogger.info("[LUA_LOCALS] function=" + functionName + " params=" + params.size()
                + " locals=" + scopeLocals.size() + " total=" + localCount);
        }
        if (localCount < LUA_LOCALS_LIMIT || scopeLocals.isEmpty()) {
            return;
        }

        LuaVariable localsTable = findTopLevelLocalsTable(body);
        if (localsTable == null) {
            localsTable = LuaAst.LuaVariable(localName(scopeOf(owner), "__wurst_locals"),
                LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()));
        }
        // Must be declared before any rewritten uses; otherwise accesses become global lookups.
        if (!body.isEmpty() && body.get(0) != localsTable) {
            body.remove(localsTable);
            body.add(0, localsTable);
        }

        Set<LuaVariable> localSet = new LinkedHashSet<>();
        for (LuaVariable v : scopeLocals) {
            if (v != localsTable) {
                localSet.add(v);
            }
        }
        if (localSet.isEmpty()) {
            return;
        }

        if (DEBUG_LUA_LOCALS) {
            WLogger.info("[LUA_LOCALS] spill function=" + functionName + " total=" + localCount
                + " spilledLocals=" + localSet.size());
        }

        final LuaVariable tableVar = localsTable;
        final Map<LuaVariable, Integer> localSlots = createLocalSlots(localSet);

        // Rewrite accesses first, then replace declarations with table init assignments.
        forEachElementRec(body, e -> {
            if (e instanceof LuaExprVarAccess va) {
                LuaVariable var = va.getVar();
                Integer slot = localSlots.get(var);
                if (slot != null) {
                    LuaExpr tableRef = LuaAst.LuaExprVarAccess(tableVar);
                    LuaExpr key = LuaAst.LuaExprIntVal("" + slot);
                    va.replaceBy(LuaAst.LuaExprArrayAccess(tableRef, LuaAst.LuaExprlist(key)));
                }
            }
        });

        rewriteLocalDeclarationsToTableAssignments(body, localSet, localSlots, tableVar);
    }

    private Map<LuaVariable, Integer> createLocalSlots(Set<LuaVariable> localSet) {
        Map<LuaVariable, Integer> r = new LinkedHashMap<>();
        int i = 1;
        for (LuaVariable v : localSet) {
            r.put(v, i++);
        }
        return r;
    }

    private LuaVariable findTopLevelLocalsTable(LuaStatements body) {
        for (LuaStatement stmt : body) {
            if (stmt instanceof LuaVariable v) {
                if (v.getName().startsWith("__wurst_locals") && v.getInitialValue() instanceof LuaTableConstructor) {
                    return v;
                }
            }
        }
        return null;
    }

    private List<LuaVariable> collectFunctionScopeLocals(LuaStatements body) {
        List<LuaVariable> result = new ArrayList<>();
        collectFunctionScopeLocalsRec(body, result);
        return result;
    }

    private void rewriteLocalDeclarationsToTableAssignments(LuaStatements stmts, Set<LuaVariable> localSet, Map<LuaVariable, Integer> localSlots, LuaVariable tableVar) {
        ListIterator<LuaStatement> it = stmts.listIterator();
        while (it.hasNext()) {
            LuaStatement stmt = it.next();
            if (stmt instanceof LuaVariable localDecl && localSet.contains(stmt)) {
                Integer slot = localSlots.get(localDecl);
                if (slot == null) {
                    continue;
                }
                LuaExpr key = LuaAst.LuaExprIntVal("" + slot);
                LuaExpr left = LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tableVar), LuaAst.LuaExprlist(key));
                LuaExprOpt initVal = localDecl.getInitialValue();
                LuaExpr right = initVal instanceof LuaExpr ? (LuaExpr) initVal.copy() : LuaAst.LuaExprNull();
                it.set(LuaAst.LuaAssignment(left, right));
            } else if (stmt instanceof LuaIf luaIf) {
                rewriteLocalDeclarationsToTableAssignments(luaIf.getThenStmts(), localSet, localSlots, tableVar);
                rewriteLocalDeclarationsToTableAssignments(luaIf.getElseStmts(), localSet, localSlots, tableVar);
            } else if (stmt instanceof LuaWhile luaWhile) {
                rewriteLocalDeclarationsToTableAssignments(luaWhile.getBody(), localSet, localSlots, tableVar);
            } else if (stmt instanceof LuaFor luaFor) {
                rewriteLocalDeclarationsToTableAssignments(luaFor.getBody(), localSet, localSlots, tableVar);
            }
        }
    }

    private void collectFunctionScopeLocalsRec(de.peeeq.wurstscript.luaAst.Element e, List<LuaVariable> out) {
        if (e instanceof LuaExprFunctionAbstraction || e instanceof LuaFunction || e instanceof LuaMethod) {
            return;
        }
        if (e instanceof LuaVariable luaVariable) {
            out.add(luaVariable);
        }
        e.forEachElement(child -> collectFunctionScopeLocalsRec(child, out));
    }

    private void forEachElementRec(de.peeeq.wurstscript.luaAst.Element root, java.util.function.Consumer<de.peeeq.wurstscript.luaAst.Element> action) {
        action.accept(root);
        root.forEachElement(child -> forEachElementRec(child, action));
    }

    void translateStatements(List<LuaStatement> res, ImStmts stmts) {
        for (ImStmt s : stmts) {
            s.translateStmtToLua(res, this);
        }
    }

    public LuaStatements translateStatements(ImStmts stmts) {
        LuaStatements r = LuaAst.LuaStatements();
        translateStatements(r, stmts);
        return r;
    }


    private void translateClass(ImClass c) {

        // following the code at http://lua-users.org/wiki/InheritanceTutorial
        LuaVariable classVar = luaClassVar.getFor(c);
        // a class which only allocates has no allocator of its own (allocatesOnly)
        LuaFunction allocator = allocatesOnly(c) ? null : luaClassAllocator.getFor(c);
        if (allocator != null) {
            luaModel.add(allocator);
        }

        // translate functions
        for (ImFunction f : c.getFunctions()) {
            translateFunc(f);
            if (!classFunctionsNamed.contains(f)) {
                luaFunc.getFor(f).setName(uniqueName(c.getName() + "_" + classFunctionName(f)));
            }
        }

        if (allocator != null) {
            createClassInitFunction(c, classVar, allocator);
        }
    }

    /**
     * Whether allocating an object of {@code c} takes an id and records its class and nothing else: no array field
     * needs its table, no default survived at an allocation ({@link LuaFieldDefaults}) and {@link LuaReflection}
     * does not count its objects. Such a class has no allocator of its own; its allocations call the shared
     * {@link #objectAlloc()}.
     */
    boolean allocatesOnly(ImClass c) {
        return allocatesOnly.computeIfAbsent(c, k -> {
            if (reflection != null && reflection.countsAllocationsOf(k)) {
                return false;
            }
            Set<ImVar> survivingDefaults = fieldDefaultsOnAllocation.getOrDefault(k, Collections.emptySet());
            for (ImVar field : LuaFieldDefaults.fieldsOf(k, imTr)) {
                if (LuaFieldDefaults.isArrayField(field) || survivingDefaults.contains(imTr.canonical(field))) {
                    return false;
                }
            }
            return true;
        });
    }

    /** {@code __wurst_allocObject(class)}: a fresh or recycled id, recorded as an object of the class given. */
    LuaFunction objectAlloc() {
        return objectAlloc.get();
    }

    private LuaFunction createObjectAlloc() {
        LuaVariable classParam = LuaAst.LuaVariable("class", LuaAst.LuaNoExpr());
        LuaFunction alloc = LuaAst.LuaFunction(uniqueName("__wurst_allocObject"), LuaAst.LuaParams(classParam),
            LuaAst.LuaStatements());
        LuaVariable newInst = addAllocation(alloc.getBody(), LuaAst.LuaExprVarAccess(classParam));
        alloc.getBody().add(LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(newInst)));
        // Called by every allocation of most classes, so a local function like the deallocator.
        declareChunkLocal(alloc);
        return alloc;
    }

    /**
     * What a class function is called after its class's name. A closure's implementation is named after
     * the method and the scopes around the closure, which its class is named after as well, so there the
     * method alone is left.
     */
    private String classFunctionName(ImFunction f) {
        if (f.attrTrace() instanceof ExprClosure closure) {
            return closure.attrClosureAbstractMethod().getDef().getName();
        }
        return f.getName();
    }

    private void createClassInitFunction(ImClass c, LuaVariable classVar, LuaFunction allocator) {
        LuaStatements body = allocator.getBody();
        LuaVariable newInst = addAllocation(body, LuaAst.LuaExprVarAccess(classVar));
        // The defaults the optimiser did not remove at some allocation of this class (LuaFieldDefaults), and the
        // table an array field needs per object, which only the backend can make.
        Set<ImVar> survivingDefaults = fieldDefaultsOnAllocation.getOrDefault(c, Collections.emptySet());
        for (ImVar field : LuaFieldDefaults.fieldsOf(c, imTr)) {
            if (LuaFieldDefaults.isArrayField(field) || survivingDefaults.contains(imTr.canonical(field))) {
                body.add(LuaAst.LuaAssignment(
                    LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(fieldStorage(field)),
                        LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(newInst))),
                    defaultValue(field.getType())));
            }
        }
        if (reflection != null) {
            reflection.addAllocationCount(body, c);
        }
        body.add(LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(newInst)));
    }

    /** Adds to {@code body} the local {@code new_inst}, a fresh or recycled id recorded as an object of {@code classExpr}. */
    private LuaVariable addAllocation(LuaStatements body, LuaExpr classExpr) {
        LuaVariable newInst = LuaAst.LuaVariable("new_inst", LuaAst.LuaNoExpr());
        body.add(newInst);
        LuaStatements fresh = LuaAst.LuaStatements(
            LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(objectMax),
                LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(objectMax), LuaAst.LuaOpPlus(), LuaAst.LuaExprIntVal("1"))),
            LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(newInst), LuaAst.LuaExprVarAccess(objectMax)));
        // The popped slot is not cleared: a slot above the count is never read before a destroy
        // writes it again, and it holds an integer id, so it retains nothing. The free stack stays
        // as large as its peak.
        LuaStatements recycled = LuaAst.LuaStatements(
            LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(newInst),
                LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(objectFree),
                    LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(objectFreeCount)))),
            LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(objectFreeCount),
                LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(objectFreeCount), LuaAst.LuaOpMinus(), LuaAst.LuaExprIntVal("1"))));
        body.add(LuaAst.LuaIf(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(objectFreeCount), LuaAst.LuaOpEquals(), LuaAst.LuaExprIntVal("0")),
            fresh, recycled));
        body.add(LuaAst.LuaAssignment(
            LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(objectClass),
                LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(newInst))),
            classExpr));
        return newInst;
    }

    private void initClassTables(ImClass c) {
        LuaVariable classVar = luaClassVar.getFor(c);
        // create methods:
        createMethods(c, classVar);

        // set supertype metadata:
        if (readsSupertypes) {
            LuaTableFields superClasses = LuaAst.LuaTableFields();
            collectSuperClasses(superClasses, c, new HashSet<>());
            deferMainInit(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
                LuaAst.LuaExprVarAccess(classVar),
                WURST_SUPERTYPES),
                LuaAst.LuaTableConstructor(superClasses)
            ));
        }

        // set typeid metadata:
        if (readsObjectTypeId) {
            // Targeted Lua specialization changes storage, not nominal identity (getTypeId). Garbage
            // reachability retains this canonical metadata dependency before emission.
            deferMainInit(LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
                LuaAst.LuaExprVarAccess(classVar),
                ExprTranslation.TYPE_ID),
                LuaAst.LuaExprIntVal("" + getTypeId(c))
            ));
        }

        // the counted group, which the destroy of an object reads:
        if (reflection != null) {
            LuaStatement groupField = reflection.groupField(c, classVar);
            if (groupField != null) {
                deferMainInit(groupField);
            }
        }

    }

    private void createMethods(ImClass c, LuaVariable classVar) {
        List<ImMethod> allMethods = collectMethodsInHierarchy(c);
        Map<String, List<ImMethod>> groupedMethods = new TreeMap<>();
        for (ImMethod method : allMethods) {
            groupedMethods.computeIfAbsent(dispatchGroupKey(method), ignored -> new ArrayList<>()).add(method);
        }

        List<List<ImMethod>> groups = new ArrayList<>(groupedMethods.values());
        groups.sort(Comparator.comparing(group -> group.isEmpty() ? "" : methodSortKey(group.get(0))));
        List<List<ImMethod>> preparedGroups = new ArrayList<>();
        Map<List<ImMethod>, ImMethod> chosenByGroup = new LinkedHashMap<>();
        // Both passes below read the slot names of every group and the names of its members; they are made once.
        Map<List<ImMethod>, Set<String>> slotNamesByGroup = new IdentityHashMap<>();
        Map<List<ImMethod>, Set<String>> memberNamesByGroup = new IdentityHashMap<>();
        for (List<ImMethod> groupMethods : groups) {
            if (groupMethods == null || groupMethods.isEmpty()) {
                continue;
            }
            groupMethods.sort(Comparator.comparing(this::methodSortKey));
            ImMethod chosen = chooseBestImplementationForClass(c, groupMethods);
            if (chosen == null || chosen.getIsAbstract() || chosen.getImplementation() == null) {
                continue;
            }
            preparedGroups.add(groupMethods);
            chosenByGroup.put(groupMethods, chosen);
            Set<String> memberNames = new HashSet<>();
            for (ImMethod m : groupMethods) {
                memberNames.add(dispatchSlotName(m.getName()));
            }
            memberNamesByGroup.put(groupMethods, memberNames);
            slotNamesByGroup.put(groupMethods, collectDispatchSlotNames(c, groupMethods));
        }

        // Slots are assigned in two passes: a slot name that is the (normalized)
        // name of a method in a group is a DIRECT claim of that group; call sites
        // dispatch through exactly these names. All other slot names (semantic
        // aliases like ClassName_x, closure/generics bridging aliases) are DERIVED.
        // A derived claim may take over a direct slot only when the two methods
        // have the same runtime arity (that is how overrides of erased generic
        // groups are bridged; their full signatures differ in the erased type
        // parameter positions); otherwise an unrelated method whose name merely
        // shares an underscore-suffix (my_x(int) vs x()) could steal a real slot.
        Map<String, ImMethod> slotToImpl = new TreeMap<>();
        Set<String> directSlots = new HashSet<>();
        for (List<ImMethod> groupMethods : preparedGroups) {
            ImMethod chosen = chosenByGroup.get(groupMethods);
            Set<String> memberNames = memberNamesByGroup.get(groupMethods);
            Set<String> slotNames = slotNamesByGroup.get(groupMethods);
            for (String slotName : slotNames) {
                if (!memberNames.contains(slotName)) {
                    continue;
                }
                ImMethod current = slotToImpl.get(slotName);
                if (current == null || !directSlots.contains(slotName)
                    || compareDispatchCandidates(c, chosen, current) < 0) {
                    slotToImpl.put(slotName, chosen);
                }
                directSlots.add(slotName);
            }
            debugDispatchGroup(c, groupMethods.get(0).getName(), slotNames, groupMethods, chosen);
        }
        for (List<ImMethod> groupMethods : preparedGroups) {
            ImMethod chosen = chosenByGroup.get(groupMethods);
            Set<String> memberNames = memberNamesByGroup.get(groupMethods);
            for (String slotName : slotNamesByGroup.get(groupMethods)) {
                if (memberNames.contains(slotName)) {
                    continue;
                }
                ImMethod current = slotToImpl.get(slotName);
                if (current != null && directSlots.contains(slotName)
                    && (implArity(chosen) != implArity(current)
                        || !LuaDispatchPreparation.compatibleReturnTypes(chosen, current))) {
                    continue;
                }
                if (current == null || compareDispatchCandidates(c, chosen, current) < 0) {
                    slotToImpl.put(slotName, chosen);
                }
            }
        }

        List<ImMethod> destroyMethods = allMethods.stream()
            .filter(this::isDestroyDispatchMethod)
            .toList();
        ImMethod destroyImplementation = chooseBestImplementationForClass(c, destroyMethods);
        if (destroyImplementation != null) {
            slotToImpl.put("__wurst_destroy", destroyImplementation);
        }

        for (Map.Entry<String, ImMethod> e : slotToImpl.entrySet()) {
            ImMethod impl = e.getValue();
            if (impl == null || impl.getImplementation() == null) {
                continue;
            }
            registerDispatchSlot(c, e.getKey(), dispatchGroupOf(impl));
            LuaStatement binding = LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(
                LuaAst.LuaExprVarAccess(classVar),
                e.getKey()),
                LuaAst.LuaExprFuncRef(luaFunc.getFor(impl.getImplementation()))
            );
            slotBindings.put(binding, new SlotBinding(c, e.getKey()));
            deferMainInit(binding);
        }

    }

    /**
     * Class tables bind every name dispatch preparation collected for a method: the names of its
     * overrides, of the closures which share its interface, the class-prefixed forms and the numbered
     * overloads. That keeps the resolution above free to land on whichever name a family has in common,
     * but a table is a closed world: once every call site has its slot, a binding no call site reads is
     * never looked up. Dropping those keeps a table at the slots its callers use, instead of one per
     * sibling, which for a family of closures is quadratic in its size.
     *
     * <p>Slots the runtime itself reads ({@code __wurst_...}) always stay.
     */
    private void dropUnreadDispatchSlots() {
        Set<String> read = new HashSet<>();
        for (PendingDispatch pending : pendingDispatches) {
            read.add(pending.target.getFieldName());
        }
        List<LuaStatement> drained = new ArrayList<>(deferredMainInit.size());
        while (!deferredMainInit.isEmpty()) {
            drained.add(deferredMainInit.remove(deferredMainInit.size() - 1));
        }
        Collections.reverse(drained);
        for (LuaStatement statement : drained) {
            SlotBinding binding = slotBindings.get(statement);
            if (binding == null || binding.slot().startsWith("__wurst_") || read.contains(binding.slot())) {
                deferredMainInit.add(statement);
                continue;
            }
            droppedSlots.add(binding.slot());
            Set<String> slots = emittedDispatchSlotsByClass.get(binding.receiver());
            if (slots != null) {
                slots.remove(binding.slot());
            }
            Map<String, Set<DispatchGroupIdentity>> groups = emittedDispatchSlotGroupsByClass.get(binding.receiver());
            if (groups != null) {
                groups.remove(binding.slot());
            }
        }
    }

    private void registerDispatchSlot(ImClass receiver, String slot, DispatchGroupIdentity group) {
        emittedDispatchSlots.add(slot);
        emittedDispatchSlotsByClass.computeIfAbsent(receiver, ignored -> new HashSet<>()).add(slot);
        if (group != null) {
            emittedDispatchSlotGroupsByClass
                .computeIfAbsent(receiver, ignored -> new HashMap<>())
                .computeIfAbsent(slot, ignored -> new HashSet<>())
                .add(group);
        }
    }

    /** Resolve dispatch helper targets against the slots actually registered by class descriptors. */
    private void resolveDispatchSlots() {
        buildDispatchReceiverIndex();
        ensureDestroyFallbackSlots();
        for (PendingDispatch pending : pendingDispatches) {
            String current = pending.target.getFieldName();
            Set<String> candidates = new TreeSet<>();
            String methodName = dispatchSlotName(pending.method.getName());
            String segment = dispatchSlotName(imTr.dispatchSegmentOf(pending.method));
            Set<ImClass> receivers = concreteReceiversFor(pending.method);
            if (isDestroyDispatchMethod(pending.method)
                && emittedDispatchSlots.contains("__wurst_destroy")) {
                setResolvedDispatchSlot(pending, "__wurst_destroy");
                continue;
            }
            if (needsOwnDispatchSlot(pending.method)) {
                ensureOwnDispatchSlot(pending);
                continue;
            }
            Set<String> commonSlots = receivers.isEmpty()
                ? Collections.emptySet()
                : commonConcreteReceiverSlots(pending.method);
            // A concrete receiver family with no common emitted slot must be canonicalized. Never
            // fall back to the program-wide slot union: an unrelated dispatch group can otherwise
            // satisfy the name check and silently bind the wrong implementation. Opaque native
            // callbacks are the only case where there is no compiler-known receiver descriptor.
            Set<String> resolutionSlots = receivers.isEmpty() ? emittedDispatchSlots : commonSlots;
            if (!receivers.isEmpty() && commonSlots.isEmpty()) {
                ensureCanonicalDispatchSlot(pending);
                continue;
            }
            if (resolutionSlots.contains(methodName)) {
                setResolvedDispatchSlot(pending, methodName);
                continue;
            }
            if (resolutionSlots.contains(segment)) {
                setResolvedDispatchSlot(pending, segment);
                continue;
            }
            if (resolutionSlots.contains(current)) {
                setResolvedDispatchSlot(pending, current);
                continue;
            }
            candidates.addAll(dispatchCandidateSlots(pending.method));
            String resolved = candidates.stream()
                .filter(resolutionSlots::contains)
                .sorted(Comparator.comparingInt(String::length).thenComparing(String::compareTo))
                .findFirst().orElse(null);
            if (resolved != null) {
                setResolvedDispatchSlot(pending, resolved);
                continue;
            }
            if (!concreteReceiversFor(pending.method).isEmpty()) {
                ensureCanonicalDispatchSlot(pending);
            }
        }
    }

    private void setResolvedDispatchSlot(PendingDispatch pending, String slot) {
        Set<ImClass> receivers = concreteReceiversFor(pending.method);
        if (!receivers.isEmpty() && receivers.stream().anyMatch(c ->
            !emittedDispatchSlotsByClass.getOrDefault(c, Collections.emptySet()).contains(slot))) {
            ensureCanonicalDispatchSlot(pending);
        } else {
            pending.target.setFieldName(slot);
        }
    }

    /** Give a heterogeneous specialization family one private, consistently registered slot. */
    private void ensureCanonicalDispatchSlot(PendingDispatch pending) {
        DispatchGroupIdentity group = dispatchGroupOf(pending.method);
        if (group == null) {
            return;
        }
        String semantic = dispatchSlotName(imTr.dispatchSegmentOf(pending.method));
        if (semantic.isEmpty()) {
            semantic = "method";
        }
        String canonicalName = semantic;
        String slot = canonicalDispatchSlots.computeIfAbsent(group,
            ignored -> uniqueName("__wurst_dispatch_" + canonicalName));
        Set<String> semanticNames = dispatchSemanticNames(pending.method);
        Set<ImClass> receivers = concreteReceiversFor(pending.method);
        List<ImClass> sortedReceivers = new ArrayList<>(receivers);
        sortedReceivers.sort(Comparator.comparing(this::classSortKey));
        for (ImClass receiver : sortedReceivers) {
            List<ImMethod> candidates = dispatchCandidatesOf(receiver, pending.method, semanticNames);
            ImMethod implementation = chooseBestImplementationForClass(receiver, candidates);
            if (implementation == null) {
                throw new RuntimeException("Wurst Lua backend assertion failed: no implementation for dispatch slot '"
                    + slot + "' in descriptor for " + receiver.getName() + ".");
            }
            Set<String> registered = emittedDispatchSlotsByClass.computeIfAbsent(receiver, ignored -> new HashSet<>());
            if (registered.add(slot)) {
                registerDispatchSlot(receiver, slot, group);
                deferMainInit(LuaAst.LuaAssignment(
                    LuaAst.LuaExprFieldAccess(LuaAst.LuaExprVarAccess(luaClassVar.getFor(receiver)), slot),
                    LuaAst.LuaExprFuncRef(luaFunc.getFor(implementation.getImplementation()))));
            }
        }
        pending.target.setFieldName(slot);
    }

    /** The names under which a method's family can be found in a class's hierarchy: its segment and its source name. */
    private Set<String> dispatchSemanticNames(ImMethod method) {
        Set<String> semanticNames = new HashSet<>();
        if (!imTr.dispatchSegmentOf(method).isEmpty()) {
            semanticNames.add(imTr.dispatchSegmentOf(method));
        }
        String sourceName = sourceSemanticName(method);
        if (!sourceName.isEmpty()) {
            semanticNames.add(sourceName);
        }
        return semanticNames;
    }

    /** The methods of {@code receiver}'s hierarchy which can implement a call through {@code method}. */
    private List<ImMethod> dispatchCandidatesOf(ImClass receiver, ImMethod method, Set<String> semanticNames) {
        List<ImMethod> candidates = new ArrayList<>();
        for (ImMethod candidate : collectMethodsInHierarchy(receiver)) {
            if (sameDispatchFamily(method, candidate)
                && sharesDispatchSemanticName(candidate, semanticNames)) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    private Set<String> dispatchCandidateSlots(ImMethod method) {
        Set<String> candidates = new TreeSet<>();
        candidates.add(dispatchSlotName(method.getName()));
        candidates.add(dispatchSlotName(imTr.dispatchSegmentOf(method)));
        for (String alias : method.getLuaMethodDispatchAliases()) {
            if (alias != null && !alias.isEmpty()) {
                candidates.add(dispatchSlotName(alias));
            }
        }
        Set<ImClass> receivers = concreteReceiversFor(method);
        Set<String> semanticNames = new HashSet<>();
        String segment = imTr.dispatchSegmentOf(method);
        if (!segment.isEmpty()) {
            semanticNames.add(segment);
        }
        String sourceName = sourceSemanticName(method);
        if (!sourceName.isEmpty()) {
            semanticNames.add(sourceName);
        }
        for (ImClass receiver : receivers) {
            for (ImMethod candidate : collectMethodsInHierarchy(receiver)) {
                if (!sameDispatchFamily(method, candidate)
                    || !sharesDispatchSemanticName(candidate, semanticNames)) {
                    continue;
                }
                candidates.add(dispatchSlotName(candidate.getName()));
                candidates.add(dispatchSlotName(imTr.dispatchSegmentOf(candidate)));
                for (String alias : candidate.getLuaMethodDispatchAliases()) {
                    if (alias != null && !alias.isEmpty()) {
                        candidates.add(dispatchSlotName(alias));
                    }
                }
            }
        }
        candidates.remove("");
        return candidates;
    }

    private boolean sharesDispatchSemanticName(ImMethod method, Set<String> semanticNames) {
        if (isDestroyDispatchMethod(method) && semanticNames.stream().anyMatch(name -> name.startsWith("destroy"))) {
            return true;
        }
        return semanticNames.contains(imTr.dispatchSegmentOf(method))
            || semanticNames.contains(sourceSemanticName(method));
    }

    private boolean isDestroyDispatchMethod(ImMethod method) {
        ImFunction implementation = method.getImplementation();
        return implementation != null && implementation.attrTrace() instanceof OnDestroyDef;
    }

    /** Verify every descriptor slot used by an emitted dispatch helper is registered by its receivers. */
    private void assertResolvedDispatchSlots() {
        for (PendingDispatch pending : pendingDispatches) {
            String slot = pending.target.getFieldName();
            Set<ImClass> receivers = concreteReceiversFor(pending.method);
            if (receivers.isEmpty()) {
                // Native/opaque callback interfaces can have no concrete IM receiver descriptor;
                // their slot is supplied by the runtime rather than by this program.
                continue;
            }
            for (ImClass receiver : receivers) {
                Set<String> slots = emittedDispatchSlotsByClass.getOrDefault(receiver, Collections.emptySet());
                if (!slots.contains(slot)) {
                    throw new RuntimeException("Wurst Lua backend assertion failed: dispatch slot '"
                        + slot + "' is missing from descriptor for " + receiver.getName() + ".");
                }
            }
        }
    }

    /**
     * Return only slots shared by every concrete descriptor which can receive this dispatch
     * group. A global slot union is insufficient: an unrelated specialization may register the
     * mangled method name while closure descriptors for the same helper register only its alias.
     */
    private Set<String> commonConcreteReceiverSlots(ImMethod method) {
        Set<String> common = null;
        Set<ImClass> receivers = concreteReceiversFor(method);
        DispatchGroupIdentity group = dispatchGroupOf(method);
        for (ImClass c : receivers) {
            Map<String, Set<DispatchGroupIdentity>> slotGroups = emittedDispatchSlotGroupsByClass.get(c);
            Set<String> slots = slotGroups == null ? Collections.emptySet() : slotGroups.entrySet().stream()
                .filter(entry -> group != null && entry.getValue().contains(group))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
            if (slots == null || slots.isEmpty()) {
                return Collections.emptySet();
            }
            if (common == null) {
                common = new HashSet<>(slots);
            } else {
                common.retainAll(slots);
            }
        }
        return common == null ? Collections.emptySet() : common;
    }

    private void buildDispatchReceiverIndex() {
        if (dispatchReceiverIndexBuilt) {
            return;
        }
        dispatchReceiverIndexBuilt = true;
        buildDispatchGroupIndex();
        for (ImClass receiver : prog.getClasses()) {
            boolean hasConcreteMethod = false;
            for (ImMethod candidate : collectMethodsInHierarchy(receiver)) {
                if (candidate.getIsAbstract() || candidate.getImplementation() == null) {
                    continue;
                }
                hasConcreteMethod = true;
                DispatchGroupIdentity group = dispatchGroups.get(candidate);
                if (group != null) {
                    concreteReceiverClassesByGroup.computeIfAbsent(group, ignored -> new LinkedHashSet<>()).add(receiver);
                }
            }
            if (hasConcreteMethod && !isInterfaceClass(receiver)) {
                for (ImClass nominalType : collectClassesInHierarchy(receiver)) {
                    concreteReceiverClassesByNominalType.computeIfAbsent(nominalType, ignored -> new LinkedHashSet<>()).add(receiver);
                }
            }
        }
    }

    private DispatchGroupIdentity dispatchGroupOf(ImMethod method) {
        buildDispatchGroupIndex();
        return dispatchGroups.get(method);
    }

    private Set<ImClass> concreteReceiversFor(ImMethod method) {
        Set<ImClass> cached = concreteReceiverFamilyCache.get(method);
        if (cached != null) {
            return cached;
        }
        Set<ImClass> receivers = new LinkedHashSet<>(concreteReceiverClassesByGroup.getOrDefault(
            dispatchGroupOf(method), Collections.emptySet()));
        ImClass owner = method.attrClass();
        if (owner != null) {
            receivers.addAll(concreteReceiverClassesByNominalType.getOrDefault(owner, Collections.emptySet()));
        }
        Set<String> semanticNames = new HashSet<>();
        if (!imTr.dispatchSegmentOf(method).isEmpty()) {
            semanticNames.add(imTr.dispatchSegmentOf(method));
        }
        String sourceName = sourceSemanticName(method);
        if (!sourceName.isEmpty()) {
            semanticNames.add(sourceName);
        }
        if (isDestroyDispatchMethod(method)) {
            // A closure's specialized interface class can omit the generated destroy method from
            // its IM hierarchy even though it is a valid receiver for the interface's lifecycle
            // call.  Any concrete receiver of another method declared by that owner is also a
            // concrete receiver of its destroy slot.
            if (owner != null) {
                for (ImMethod ownerMethod : owner.getMethods()) {
                    receivers.addAll(concreteReceiverClassesByGroup.getOrDefault(
                        dispatchGroupOf(ownerMethod), Collections.emptySet()));
                }
            }
            receivers.removeIf(this::isInterfaceClass);
            Set<ImClass> result = Collections.unmodifiableSet(receivers);
            concreteReceiverFamilyCache.put(method, result);
            return result;
        }
        receivers.removeIf(receiver -> collectMethodsInHierarchy(receiver).stream()
            .noneMatch(candidate -> !candidate.getIsAbstract()
                && candidate.getImplementation() != null
                && sameDispatchFamily(method, candidate)
                && sharesDispatchSemanticName(candidate, semanticNames)));
        Set<ImClass> result = Collections.unmodifiableSet(receivers);
        concreteReceiverFamilyCache.put(method, result);
        return result;
    }

    /** Install the fallback only for descriptors that can actually receive a dynamic destroy call. */
    private void ensureDestroyFallbackSlots() {
        Set<ImClass> fallbackReceivers = new LinkedHashSet<>();
        for (PendingDispatch pending : pendingDispatches) {
            if (!isDestroyDispatchMethod(pending.method)) {
                continue;
            }
            fallbackReceivers.addAll(concreteReceiversFor(pending.method));
        }
        List<ImClass> sortedReceivers = new ArrayList<>(fallbackReceivers);
        sortedReceivers.sort(Comparator.comparing(this::classSortKey));
        for (ImClass receiver : sortedReceivers) {
            if (isInterfaceClass(receiver)) {
                continue;
            }
            Set<String> slots = emittedDispatchSlotsByClass.computeIfAbsent(receiver,
                ignored -> new HashSet<>());
            if (!slots.contains("__wurst_destroy")) {
                registerDispatchSlot(receiver, "__wurst_destroy", null);
                deferMainInit(LuaAst.LuaAssignment(
                    LuaAst.LuaExprFieldAccess(LuaAst.LuaExprVarAccess(luaClassVar.getFor(receiver)), "__wurst_destroy"),
                    LuaAst.LuaExprFuncRef(deallocFor(receiver))));
            }
        }
    }

    private boolean sameDispatchFamily(ImMethod reference, ImMethod candidate) {
        return dispatchGroupOf(reference) == dispatchGroupOf(candidate);
    }

    /** Reconstruct dispatch-group identity from the IM override graph without using names. */
    private void buildDispatchGroupIndex() {
        if (dispatchGroupsBuilt) {
            return;
        }
        dispatchGroupsBuilt = true;
        List<ImMethod> methods = new ArrayList<>();
        for (ImClass c : prog.getClasses()) {
            methods.addAll(c.getMethods());
        }
        Set<ImMethod> knownMethods = Collections.newSetFromMap(new IdentityHashMap<>());
        knownMethods.addAll(methods);
        Map<ImMethod, ImMethod> parent = new IdentityHashMap<>();
        for (ImMethod method : methods) {
            parent.put(method, method);
        }
        for (ImMethod method : methods) {
            for (ImMethod subMethod : method.getSubMethods()) {
                if (knownMethods.contains(subMethod)) {
                    unionDispatchGroups(parent, method, subMethod);
                }
            }
        }
        Map<DispatchGroupIdentity, DispatchGroupIdentity> identities = new HashMap<>();
        for (ImMethod method : methods) {
            ImMethod root = findDispatchGroupRoot(parent, method);
            DispatchGroupIdentity candidate = new DispatchGroupIdentity(root, dispatchSpecializationOf(method));
            DispatchGroupIdentity identity = identities.computeIfAbsent(candidate, ignored -> candidate);
            dispatchGroups.put(method, identity);
        }
    }

    /**
     * Specialised methods which were moved back onto their erased allocation class can coexist
     * with another specialization of the same virtual root. Keep their structural arguments in
     * the dispatch identity; specialised methods still living on a specialised class are already
     * reached through that class and must continue sharing the root family with its overrides.
     */
    private GenericTypes dispatchSpecializationOf(ImMethod method) {
        ImTranslator.Specialisation specialization = imTr.specialisationOf(method);
        ImClass owner = method.attrClass();
        if (specialization == null || owner == null || imTr.canonical(owner) != owner) {
            return null;
        }
        return new GenericTypes(specialization.typeArguments());
    }

    private static ImMethod findDispatchGroupRoot(Map<ImMethod, ImMethod> parent, ImMethod method) {
        ImMethod root = method;
        ImMethod next;
        while ((next = parent.get(root)) != root) {
            root = next;
        }
        while ((next = parent.get(method)) != method) {
            parent.put(method, root);
            method = next;
        }
        return root;
    }

    private static void unionDispatchGroups(Map<ImMethod, ImMethod> parent, ImMethod left, ImMethod right) {
        ImMethod leftRoot = findDispatchGroupRoot(parent, left);
        ImMethod rightRoot = findDispatchGroupRoot(parent, right);
        if (leftRoot != rightRoot) {
            parent.put(rightRoot, leftRoot);
        }
    }

    /**
     * The Lua table key a dispatch slot is emitted under. Aliases and class-qualified names are
     * built from IM names, which may contain characters Lua has no place for; the mapping has to
     * be the same one call sites go through, so that a slot is still found under its new name.
     */
    private String dispatchSlotName(String rawName) {
        return LuaIdentifiers.toIdentifier(rawName);
    }

    private Set<String> collectDispatchSlotNames(ImClass receiverClass, List<ImMethod> groupMethods) {
        Set<String> slotNames = new TreeSet<>();
        Set<String> semanticNames = new TreeSet<>();
        for (ImMethod m : groupMethods) {
            if (m == null) {
                continue;
            }
            for (String alias : m.getLuaMethodDispatchAliases()) {
                if (alias != null && !alias.isEmpty()) {
                    slotNames.add(dispatchSlotName(alias));
                }
            }
            String semanticName = imTr.dispatchSegmentOf(m);
            if (!semanticName.isEmpty()) {
                semanticNames.add(semanticName);
            }
            String sourceSemanticName = sourceSemanticName(m);
            if (m.attrClass() != null && m.attrClass().attrTrace() instanceof ExprClosure && !sourceSemanticName.isEmpty()) {
                semanticNames.add(sourceSemanticName);
            }
        }
        if (receiverClass != null && !semanticNames.isEmpty()) {
            // A semantic name which several of the class's methods share names none of them, so a
            // slot composed from it would be claimed by whichever is bound first. For a specialised
            // class every method's trailing segment is the type argument, which is exactly that
            // case, and the resulting slot is never called. Left uncomposed rather than bound
            // arbitrarily; LuaDispatchPreparation drops the matching alias for the same reason.
            Set<String> ambiguous = ambiguousSemanticNames(receiverClass);
            List<ImClass> classes = collectClassesInHierarchy(receiverClass);
            for (ImClass targetClass : classes) {
                String className = targetClass.getName();
                for (String semanticName : semanticNames) {
                    if (ambiguous.contains(semanticName)
                        || (isInterfaceClass(targetClass)
                            && !hasCompatibleSemanticMethod(targetClass, groupMethods, semanticName))) {
                        continue;
                    }
                    slotNames.add(dispatchSlotName(className + "_" + semanticName));
                }
            }
        }
        return slotNames;
    }

    /**
     * The semantic names which name no method in particular, cached per class.
     * <p>
     * A method and its overrides share a semantic name and must share a slot: that is dispatch, and
     * they all declare the same name in the source. The siblings of one specialisation declare
     * different names and still compose the same segment, because for a specialised method that
     * segment is the type argument - and the slot composed from it is claimed by whichever is bound
     * first, then never called.
     * <p>
     * The dispatch group key would be a sharper identity but cannot be used: it embeds the signature,
     * and a generic override chain's signatures differ by the type variable of each class in it
     * ({@code void|T192,real} against {@code void|T636,real}), so overrides would read as unrelated
     * and their shared slot would be dropped. What that leaves uncovered is recorded in backlog
     * item 15: overloads of one source method inside a specialised class share a declared name, so
     * their composed name is not seen as ambiguous and one dead key survives there.
     * <p>
     * Cached because {@code createMethods} asks twice per dispatch group and each ask would otherwise
     * rebuild and sort the whole inherited method list.
     */
    private final Map<ImClass, Set<String>> ambiguousSemanticNamesByClass = new LinkedHashMap<>();

    private Set<String> ambiguousSemanticNames(ImClass c) {
        return ambiguousSemanticNamesByClass.computeIfAbsent(c, owner -> {
            Map<String, Set<String>> claimants = new TreeMap<>();
            for (ImMethod m : collectMethodsInHierarchy(owner)) {
                if (m == null) {
                    continue;
                }
                String semanticName = imTr.dispatchSegmentOf(m);
                if (semanticName.isEmpty()) {
                    continue;
                }
                claimants.computeIfAbsent(semanticName, name -> new TreeSet<>())
                    .add(LuaDispatchPreparation.declaredName(m));
            }
            Set<String> ambiguous = new TreeSet<>();
            claimants.forEach((name, keys) -> {
                if (keys.size() > 1) {
                    ambiguous.add(name);
                }
            });
            return ambiguous;
        });
    }

    private List<ImClass> collectClassesInHierarchy(ImClass c) {
        List<ImClass> result = new ArrayList<>();
        collectClassesInHierarchy(c, result, new HashSet<>());
        result.sort(Comparator.comparing(this::classSortKey));
        return result;
    }

    private void collectClassesInHierarchy(ImClass c, List<ImClass> out, Set<ImClass> visited) {
        if (c == null || !visited.add(c)) {
            return;
        }
        out.add(c);
        for (ImClassType sc : c.getSuperClasses()) {
            collectClassesInHierarchy(sc.getClassDef(), out, visited);
        }
    }

    private boolean hasCompatibleSemanticMethod(ImClass targetClass, List<ImMethod> groupMethods, String semanticName) {
        for (ImMethod candidate : collectMethodsInHierarchy(targetClass)) {
            String candidateSemanticName = imTr.dispatchSegmentOf(candidate);
            if (!semanticName.equals(candidateSemanticName)
                && !semanticName.equals(sourceSemanticName(candidate))) {
                continue;
            }
            for (ImMethod groupMethod : groupMethods) {
                // The semantic alias is deliberately broader than the exact dispatch-group key:
                // generic overrides may have different erased parameter types while still sharing
                // the same runtime slot. Return compatibility is the part that must remain strict;
                // otherwise an unrelated int value() and string value() can claim one another's
                // class-qualified alias.
                if (LuaDispatchPreparation.compatibleReturnTypes(groupMethod, candidate, imTr)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInterfaceClass(ImClass c) {
        return c != null && c.attrTrace() instanceof InterfaceDef;
    }

    private List<ImMethod> collectMethodsInHierarchy(ImClass c) {
        List<ImMethod> result = new ArrayList<>();
        collectMethodsInHierarchy(c, result, new HashSet<>());
        result.sort(Comparator.comparing(this::methodSortKey));
        return result;
    }

    private void collectMethodsInHierarchy(ImClass c, List<ImMethod> out, Set<ImClass> visited) {
        if (c == null || !visited.add(c)) {
            return;
        }
        out.addAll(c.getMethods());
        List<ImClassType> superClasses = new ArrayList<>(c.getSuperClasses());
        superClasses.sort(Comparator.comparing(t -> classSortKey(t.getClassDef())));
        for (ImClassType sc : superClasses) {
            collectMethodsInHierarchy(sc.getClassDef(), out, visited);
        }
    }

    private int implArity(ImMethod method) {
        return method.getImplementation().getParameters().size();
    }

    private ImMethod chooseBestImplementationForClass(ImClass receiverClass, List<ImMethod> candidates) {
        List<ImMethod> concrete = new ArrayList<>();
        for (ImMethod m : candidates) {
            if (!m.getIsAbstract() && m.getImplementation() != null) {
                concrete.add(m);
            }
        }
        if (concrete.isEmpty()) {
            return null;
        }
        Set<ImMethod> passedOver = passedOverByAnInterfaceDefault(concrete);
        concrete.sort((a, b) -> compareDispatchCandidates(receiverClass, a, b, passedOver));
        return concrete.get(0);
    }

    private int compareDispatchCandidates(ImClass receiverClass, ImMethod a, ImMethod b) {
        return compareDispatchCandidates(receiverClass, a, b, passedOverByAnInterfaceDefault(List.of(a, b)));
    }

    /**
     * Orders two implementations a class could bind for one slot, the better first: the class's own, then one no
     * interface default among the candidates passes over ({@link #implementationsOutsideItsInterface}), then the
     * nearer class. Two which are still tied come from different branches of the hierarchy at the same distance,
     * where Jass and the interpreter take the one on the side of the method called through; the name order only
     * makes that choice deterministic.
     */
    private int compareDispatchCandidates(ImClass receiverClass, ImMethod a, ImMethod b, Set<ImMethod> passedOver) {
        boolean aLocal = isImplementationFromClass(a, receiverClass);
        boolean bLocal = isImplementationFromClass(b, receiverClass);
        if (aLocal != bLocal) {
            return aLocal ? -1 : 1;
        }
        boolean aPassedOver = passedOver.contains(a);
        boolean bPassedOver = passedOver.contains(b);
        if (aPassedOver != bPassedOver) {
            return aPassedOver ? 1 : -1;
        }
        int aDist = classDistance(receiverClass, a.attrClass());
        int bDist = classDistance(receiverClass, b.attrClass());
        if (aDist != bDist) {
            return Integer.compare(aDist, bDist);
        }
        return methodSortKey(a).compareTo(methodSortKey(b));
    }

    /**
     * Whether {@code method} is declared in {@code ownerClass} itself. Asked of the class, not of the implementation's
     * name: {@code Foo_Bar_m}, the implementation of a superclass {@code Foo_Bar}, starts with {@code Foo_}, and so
     * does the implementation of a class {@code Foo} in another package.
     */
    private boolean isImplementationFromClass(ImMethod method, ImClass ownerClass) {
        if (method == null || ownerClass == null || method.getImplementation() == null) {
            return false;
        }
        return method.attrClass() == ownerClass;
    }

    /** The candidates which an interface default among them passes over, see {@link #implementationsOutsideItsInterface}. */
    private Set<ImMethod> passedOverByAnInterfaceDefault(List<ImMethod> candidates) {
        Set<ImMethod> result = null;
        for (ImMethod interfaceDefault : candidates) {
            Set<ImMethod> outside = implementationsOutsideItsInterface(interfaceDefault);
            if (outside.isEmpty()) {
                continue;
            }
            for (ImMethod candidate : candidates) {
                if (candidate != interfaceDefault && outside.contains(candidate)) {
                    if (result == null) {
                        result = Collections.newSetFromMap(new IdentityHashMap<>());
                    }
                    result.add(candidate);
                }
            }
        }
        return result == null ? Collections.emptySet() : result;
    }

    private final Map<ImMethod, Set<ImMethod>> implementationsOutsideInterface = new IdentityHashMap<>();

    /**
     * The implementations which the sub-methods of an interface's default method list from classes outside that
     * interface. A class which implements the interface and inherits the method from a superclass which does not
     * ({@code C extends Base implements Omega}) has the superclass's method recorded as its implementation of the
     * interface's. Jass and the interpreter never call it through the interface: the Jass dispatch of
     * {@code Omega.m} only takes methods of the classes below Omega, and the interpreter only sub-methods of
     * subclasses, so through the interface the default stands. Through the superclass it is the other way round
     * (see {@link #needsOwnDispatchSlot}).
     *
     * <p>Asked of the IM classes, which the Jass dispatch walks, and of the source as well, because Lua's targeted
     * specialisation moves implementations between erased and specialised classes, whose IM relation then no
     * longer says whether the source class implements the interface.
     */
    private Set<ImMethod> implementationsOutsideItsInterface(ImMethod method) {
        return implementationsOutsideInterface.computeIfAbsent(method, interfaceDefault -> {
            ImClass owner = interfaceDefault.attrClass();
            if (interfaceDefault.getIsAbstract() || interfaceDefault.getImplementation() == null || owner == null
                || !(owner.attrTrace() instanceof InterfaceDef interfaceDef)) {
                return Collections.emptySet();
            }
            Set<ImMethod> outside = Collections.newSetFromMap(new IdentityHashMap<>());
            Set<ImMethod> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            ArrayDeque<ImMethod> queue = new ArrayDeque<>(interfaceDefault.getSubMethods());
            while (!queue.isEmpty()) {
                ImMethod sub = queue.removeFirst();
                if (!visited.add(sub)) {
                    continue;
                }
                queue.addAll(sub.getSubMethods());
                ImClass subClass = sub.attrClass();
                if (subClass != null && !subClass.isSubclassOf(owner) && !declaredInAnImplementationOf(sub, interfaceDef)) {
                    outside.add(sub);
                }
            }
            return outside.isEmpty() ? Collections.emptySet() : outside;
        });
    }

    /** Whether the source declares {@code method} in a class which implements {@code interfaceDef}; unknown counts as yes. */
    private static boolean declaredInAnImplementationOf(ImMethod method, InterfaceDef interfaceDef) {
        if (!(method.attrTrace() instanceof FuncDef funcDef)) {
            return true;
        }
        ClassDef classDef = funcDef.attrNearestClassDef();
        if (classDef == null) {
            return true;
        }
        for (WurstTypeInterface implemented : classDef.attrTypC().transitiveSuperInterfaces()) {
            if (implemented.getDef() == interfaceDef) {
                return true;
            }
        }
        return false;
    }

    private Set<ImMethod> passedOverAnywhere;

    /** Every implementation some interface default passes over; almost always none. */
    private Set<ImMethod> passedOverAnywhere() {
        if (passedOverAnywhere == null) {
            passedOverAnywhere = Collections.newSetFromMap(new IdentityHashMap<>());
            for (ImClass c : prog.getClasses()) {
                for (ImMethod m : c.getMethods()) {
                    passedOverAnywhere.addAll(implementationsOutsideItsInterface(m));
                }
            }
        }
        return passedOverAnywhere;
    }

    private final Map<ImMethod, Set<ImMethod>> reachedThroughCache = new IdentityHashMap<>();

    /** The implementations a call through {@code method} can run on Jass: the method and its sub-methods. */
    private Set<ImMethod> reachedThrough(ImMethod method) {
        return reachedThroughCache.computeIfAbsent(method, root -> {
            Set<ImMethod> reached = Collections.newSetFromMap(new IdentityHashMap<>());
            ArrayDeque<ImMethod> queue = new ArrayDeque<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                ImMethod current = queue.removeFirst();
                if (reached.add(current)) {
                    queue.addAll(current.getSubMethods());
                }
            }
            return reached;
        });
    }

    /**
     * The interface defaults among {@code candidates} which a call reaching {@code reached} must not run: they are no
     * implementation it reaches, but pass over one which is.
     */
    private Set<ImMethod> defaultsBesideTheCall(List<ImMethod> candidates, Set<ImMethod> reached) {
        Set<ImMethod> result = null;
        for (ImMethod interfaceDefault : candidates) {
            if (reached.contains(interfaceDefault)) {
                continue;
            }
            Set<ImMethod> outside = implementationsOutsideItsInterface(interfaceDefault);
            for (ImMethod candidate : candidates) {
                if (outside.contains(candidate) && reached.contains(candidate)) {
                    if (result == null) {
                        result = Collections.newSetFromMap(new IdentityHashMap<>());
                    }
                    result.add(interfaceDefault);
                    break;
                }
            }
        }
        return result == null ? Collections.emptySet() : result;
    }

    /**
     * Whether calls through {@code method} need a slot of their own. A group has one slot, and a class binds one
     * implementation to it. C extends Base implements Omega binds Omega's default, which is right through Omega; but
     * through Base, Jass and the interpreter run Base.m on C, so where Base.m is dispatched (it has overrides), its
     * calls cannot read the group's slot.
     */
    private boolean needsOwnDispatchSlot(ImMethod method) {
        Set<ImMethod> passedOver = passedOverAnywhere();
        if (passedOver.isEmpty()) {
            return false;
        }
        return needsOwnDispatchSlotCache.computeIfAbsent(method, m -> {
            Set<ImMethod> reached = reachedThrough(m);
            if (reached.stream().noneMatch(passedOver::contains)) {
                return false;
            }
            Set<String> semanticNames = dispatchSemanticNames(m);
            for (ImClass receiver : concreteReceiversFor(m)) {
                List<ImMethod> candidates = dispatchCandidatesOf(receiver, m, semanticNames);
                ImMethod bound = chooseBestImplementationForClass(receiver, candidates);
                if (bound != null && defaultsBesideTheCall(candidates, reached).contains(bound)) {
                    return true;
                }
            }
            return false;
        });
    }

    private final Map<ImMethod, Boolean> needsOwnDispatchSlotCache = new IdentityHashMap<>();

    private final Map<ImMethod, String> ownDispatchSlots = new IdentityHashMap<>();

    /**
     * Give calls through {@code pending.method} a slot of their own, which every receiver binds to the implementation
     * the call reaches: the one the group binds, unless that is an interface default beside the call.
     */
    private void ensureOwnDispatchSlot(PendingDispatch pending) {
        ImMethod method = pending.method;
        String slot = ownDispatchSlots.get(method);
        if (slot == null) {
            String semantic = dispatchSlotName(imTr.dispatchSegmentOf(method));
            slot = uniqueName("__wurst_dispatch_" + (semantic.isEmpty() ? "method" : semantic));
            ownDispatchSlots.put(method, slot);
            Set<ImMethod> reached = reachedThrough(method);
            Set<String> semanticNames = dispatchSemanticNames(method);
            List<ImClass> sortedReceivers = new ArrayList<>(concreteReceiversFor(method));
            sortedReceivers.sort(Comparator.comparing(this::classSortKey));
            for (ImClass receiver : sortedReceivers) {
                List<ImMethod> candidates = dispatchCandidatesOf(receiver, method, semanticNames);
                Set<ImMethod> beside = defaultsBesideTheCall(candidates, reached);
                candidates.removeIf(beside::contains);
                ImMethod implementation = chooseBestImplementationForClass(receiver, candidates);
                if (implementation == null) {
                    throw new RuntimeException("Wurst Lua backend assertion failed: no implementation for dispatch slot '"
                        + slot + "' in descriptor for " + receiver.getName() + ".");
                }
                registerDispatchSlot(receiver, slot, dispatchGroupOf(method));
                deferMainInit(LuaAst.LuaAssignment(
                    LuaAst.LuaExprFieldAccess(LuaAst.LuaExprVarAccess(luaClassVar.getFor(receiver)), slot),
                    LuaAst.LuaExprFuncRef(luaFunc.getFor(implementation.getImplementation()))));
            }
        }
        pending.target.setFieldName(slot);
    }

    private int classDistance(ImClass from, ImClass to) {
        if (from == null || to == null) {
            return Integer.MAX_VALUE;
        }
        if (from == to) {
            return 0;
        }
        ArrayDeque<ImClass> queue = new ArrayDeque<>();
        Map<ImClass, Integer> dist = new HashMap<>();
        queue.add(from);
        dist.put(from, 0);
        while (!queue.isEmpty()) {
            ImClass current = queue.removeFirst();
            int currentDist = dist.get(current);
            List<ImClassType> superClasses = new ArrayList<>(current.getSuperClasses());
            superClasses.sort(Comparator.comparing(t -> classSortKey(t.getClassDef())));
            for (ImClassType sc : superClasses) {
                ImClass next = sc.getClassDef();
                if (next == null || dist.containsKey(next)) {
                    continue;
                }
                int nextDist = currentDist + 1;
                if (next == to) {
                    return nextDist;
                }
                dist.put(next, nextDist);
                queue.add(next);
            }
        }
        return Integer.MAX_VALUE;
    }

    private void debugDispatchGroup(ImClass receiverClass, String key, Set<String> slotNames, List<ImMethod> groupMethods, ImMethod chosen) {
        if (!DEBUG_LUA_DISPATCH) {
            return;
        }
        String chosenImpl = chosen != null && chosen.getImplementation() != null ? chosen.getImplementation().getName() : "null";
        StringBuilder candidates = new StringBuilder();
        List<ImMethod> sorted = new ArrayList<>(groupMethods);
        sorted.sort(Comparator.comparing(this::methodSortKey));
        for (ImMethod m : sorted) {
            String impl = m.getImplementation() != null ? m.getImplementation().getName() : "null";
            if (candidates.length() > 0) {
                candidates.append("; ");
            }
            candidates.append(m.getName()).append("->").append(impl).append("@").append(classSortKey(m.attrClass()));
        }
        String line = "[LuaDispatch] class=" + classSortKey(receiverClass)
            + " key=" + key
            + " slots=" + slotNames
            + " chosen=" + chosenImpl
            + " candidates=[" + candidates + "]";
        WLogger.trace(line);
    }

    private String methodSortKey(ImMethod m) {
        String owner = classSortKey(m.attrClass());
        String impl = m.getImplementation() != null ? m.getImplementation().getName() : "";
        return owner + "|" + m.getName() + "|" + impl;
    }

    private String dispatchGroupKey(ImMethod method) {
        String key = method.getLuaDispatchGroupKey();
        return key == null || key.isEmpty() ? methodSortKey(method) : key;
    }

    private String sourceSemanticName(ImMethod method) {
        if (method == null) {
            return "";
        }
        de.peeeq.wurstscript.ast.Element trace = method.attrTrace();
        if (trace instanceof FuncDef funcDef) {
            return funcDef.getName();
        }
        if (trace instanceof AstElementWithFuncName withFuncName) {
            return withFuncName.getFuncNameId().getName();
        }
        if (method.getImplementation() != null) {
            String implementationName = method.getImplementation().getName();
            int firstUnderscore = implementationName.indexOf('_');
            if (firstUnderscore > 0) {
                return implementationName.substring(0, firstUnderscore);
            }
            return implementationName;
        }
        return "";
    }

    private String classSortKey(ImClass c) {
        if (c == null) {
            return "";
        }
        return c.getName();
    }

    private void collectSuperClasses(LuaTableFields superClasses, ImClass c, Set<ImClass> visited) {
        if (visited.contains(c)) {
            return;
        }
        superClasses.add(LuaAst.LuaTableExprField(LuaAst.LuaExprVarAccess(luaClassVar.getFor(c)), LuaAst.LuaExprBoolVal(true)));
        visited.add(c);
        ImClass nominalClass = imTr.canonical(c);
        if (nominalClass != c) {
            // A targeted Lua specialization is a representation detail, not a new nominal type.
            // Keep erased-class runtime checks true without inheriting its fields a second time.
            collectSuperClasses(superClasses, nominalClass, visited);
        }
        for (ImClassType sc : c.getSuperClasses()) {
            collectSuperClasses(superClasses, sc.getClassDef(), visited);
        }
    }


    private void translateGlobal(ImVar v) {
        if (v.getIsBJ()) {
            // do not translate blizzard variables
            return;
        }
        LuaVariable lv = luaVar.getFor(v);
        if (v.getType() instanceof ImArrayType || v.getType() instanceof ImArrayTypeMulti) {
            localizableStorageTables.add(lv);
        }
        // Not declared at the top of the script: a global Lua has not seen reads as nil, and the bootstrap, which
        // main runs first, gives it its default.
        deferMainInit(LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(lv), defaultValue(v.getType())));
    }

    private LuaExpr defaultValue(ImType type) {
        return type.match(new ImType.Matcher<LuaExpr>() {
            @Override
            public LuaExpr case_ImAnyType(ImAnyType imAnyType) {
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImTupleType(ImTupleType tt) {
                LuaTableFields tableFields = LuaAst.LuaTableFields();
                for (int i = 0; i < tt.getNames().size(); i++) {
                    tableFields.add(LuaAst.LuaTableSingleField(defaultValue(tt.getTypes().get(i))));
                }
                return LuaAst.LuaTableConstructor(
                    tableFields
                );
            }

            @Override
            public LuaExpr case_ImVoid(ImVoid imVoid) {
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImClassType(ImClassType imClassType) {
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImArrayTypeMulti(ImArrayTypeMulti at) {
                ImType baseType;
                if (at.getArraySize().size() <= 1) {
                    baseType = at.getEntryType();
                } else {
                    List<Integer> arraySizes = new ArrayList<>(at.getArraySize());
                    arraySizes.remove(0);
                    baseType = JassIm.ImArrayTypeMulti(at.getEntryType(), arraySizes);
                }
                return newDefaultArray(baseType);
            }

            @Override
            public LuaExpr case_ImSimpleType(ImSimpleType st) {
                if (TypesHelper.isIntType(st)) {
                    return LuaAst.LuaExprIntVal("0");
                } else if (TypesHelper.isBoolType(st)) {
                    return LuaAst.LuaExprBoolVal(false);
                } else if (TypesHelper.isRealType(st)) {
                    return LuaAst.LuaExprRealVal("0.");
                } else if (TypesHelper.isStringType(st)) {
                    return LuaAst.LuaExprStringVal("");
                }
                return LuaAst.LuaExprNull();
            }

            @Override
            public LuaExpr case_ImArrayType(ImArrayType imArrayType) {
                ImType baseType = imArrayType.getEntryType();
                return newDefaultArray(baseType);
            }

            @Override
            public LuaExpr case_ImTypeVarRef(ImTypeVarRef imTypeVarRef) {
                return LuaAst.LuaExprNull();
            }
        });
    }

    /**
     * Builds a fresh array table whose reads of never-written slots yield the
     * default value for {@code entryType}, matching Jass's fully-initialized
     * arrays. The metatable/helper-function infrastructure that makes this
     * work is shared across every array with the same entry type - only the
     * per-array table itself (and, for table-typed defaults, each lazily
     * materialized slot) is allocated per array instance. Mirrors the shared
     * per-class instance metatable in translateClass().
     */
    private LuaExpr newDefaultArray(ImType entryType) {
        if (isAlwaysNilDefault(entryType)) {
            // An untouched Lua table key already reads as nil - no metatable needed.
            return LuaAst.LuaTableConstructor(LuaAst.LuaTableFields());
        }
        if (entryType instanceof ImSimpleType imSimpleType) {
            return setmetatableCall(getOrCreatePrimitiveArrayMetatable(imSimpleType));
        }
        // Table-typed default (tuple / nested array): each slot needs its own,
        // separately mutable default value, materialized lazily on first read.
        return setmetatableCall(getOrCreateLazyArrayMetatable(entryType));
    }

    private LuaExpr setmetatableCall(LuaVariable metatableVar) {
        return LuaAst.LuaExprFunctionCallByName("setmetatable", LuaAst.LuaExprlist(
            LuaAst.LuaTableConstructor(LuaAst.LuaTableFields()),
            LuaAst.LuaExprVarAccess(metatableVar)
        ));
    }

    /** True for entry types whose Wurst default value is nil / not yet allocated. */
    private boolean isAlwaysNilDefault(ImType t) {
        if (t instanceof ImClassType || t instanceof ImAnyType || t instanceof ImTypeVarRef || t instanceof ImVoid) {
            return true;
        }
        if (t instanceof ImSimpleType st) {
            // WC3 handle types (unit, player, timer, ...) are ImSimpleType too,
            // and default to nil just like user classes - only the four true
            // primitives below have a non-nil default.
            return !TypesHelper.isIntType(st) && !TypesHelper.isBoolType(st)
                && !TypesHelper.isRealType(st) && !TypesHelper.isStringType(st);
        }
        return false;
    }

    /**
     * One shared metatable per primitive kind (int/real/bool/string), for the
     * whole program. The default is immutable, so reads never need to store
     * anything back into the array table.
     */
    private LuaVariable getOrCreatePrimitiveArrayMetatable(ImSimpleType st) {
        String key = TypesHelper.isIntType(st) ? "integer"
            : TypesHelper.isBoolType(st) ? "boolean"
            : TypesHelper.isRealType(st) ? "real"
            : TypesHelper.isStringType(st) ? "string"
            : null;
        if (key == null) {
            // Defensive fallback; isAlwaysNilDefault() should have routed anything
            // else away from this method.
            return getOrCreateLazyArrayMetatable(st);
        }
        LuaVariable existing = primitiveArrayMetatables.get(key);
        if (existing != null) {
            return existing;
        }
        LuaFunction indexFn = LuaAst.LuaFunction(uniqueName("__wurst_arrIndex_" + key),
            LuaAst.LuaParams(LuaAst.LuaVariable("t", LuaAst.LuaNoExpr()), LuaAst.LuaVariable("k", LuaAst.LuaNoExpr())),
            LuaAst.LuaStatements(LuaAst.LuaReturn(defaultValue(st))));
        luaModel.add(indexFn);

        LuaVariable mt = LuaAst.LuaVariable(uniqueName("__wurst_arrMt_" + key), LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
            LuaAst.LuaTableNamedField("__index", LuaAst.LuaExprFuncRef(indexFn))
        )));
        luaModel.add(mt);
        primitiveArrayMetatables.put(key, mt);
        return mt;
    }

    /**
     * One shared metatable (and default-value factory) per distinct entry
     * type that needs a fresh, independently mutable default per slot
     * (tuples, nested arrays).
     */
    private LuaVariable getOrCreateLazyArrayMetatable(ImType entryType) {
        for (LazyArrayDefault info : lazyArrayDefaults) {
            if (info.entryType.equalsType(entryType)) {
                return info.metatableVar;
            }
        }
        LuaVariable mt = LuaAst.LuaVariable(uniqueName("__wurst_arrMt"), LuaAst.LuaNoExpr());
        // Register before building the (possibly recursive, e.g. nested arrays) body.
        lazyArrayDefaults.add(new LazyArrayDefault(entryType, mt));

        LuaFunction thunk = LuaAst.LuaFunction(uniqueName("__wurst_arrDefault"), LuaAst.LuaParams(), LuaAst.LuaStatements());
        thunk.getBody().add(LuaAst.LuaReturn(defaultValue(entryType)));
        luaModel.add(thunk);

        LuaVariable tParam = LuaAst.LuaVariable("t", LuaAst.LuaNoExpr());
        LuaVariable kParam = LuaAst.LuaVariable("k", LuaAst.LuaNoExpr());
        LuaVariable vLocal = LuaAst.LuaVariable("v", LuaAst.LuaExprFunctionCall(thunk, LuaAst.LuaExprlist()));
        LuaFunction indexFn = LuaAst.LuaFunction(uniqueName("__wurst_arrIndex"), LuaAst.LuaParams(tParam, kParam),
            LuaAst.LuaStatements(
                vLocal,
                LuaAst.LuaAssignment(
                    LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tParam), LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(kParam))),
                    LuaAst.LuaExprVarAccess(vLocal)),
                LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(vLocal))
            ));
        luaModel.add(indexFn);

        mt.setInitialValue(LuaAst.LuaTableConstructor(LuaAst.LuaTableFields(
            LuaAst.LuaTableNamedField("__index", LuaAst.LuaExprFuncRef(indexFn))
        )));
        luaModel.add(mt);
        return mt;
    }

    public LuaExprOpt translateOptional(ImExprOpt e) {
        if (e instanceof ImExpr imExpr) {
            return imExpr.translateToLua(this);
        }
        return LuaAst.LuaNoExpr();
    }

    public LuaExprlist translateExprList(ImExprs exprs) {
        LuaExprlist r = LuaAst.LuaExprlist();
        for (ImExpr e : exprs) {
            r.add(e.translateToLua(this));
        }
        return r;
    }


    /**
     * The type ids of the Lua program. A targeted specialisation keeps the nominal identity of the class it was
     * copied from, so only those classes are numbered, and a copy has the type id of its class. When compile-time
     * code read type ids, the classes keep the ids they had then ({@link ImTranslator#typeIdsBeforeGenerics}).
     */
    private @Nullable Map<ImClass, Integer> typeIds;

    /** The type id of each class which has one of its own; see {@link #getTypeId}. */
    Map<ImClass, Integer> typeIds() {
        Map<ImClass, Integer> result = typeIds;
        if (result == null) {
            List<ImClass> nominal = new ArrayList<>();
            for (ImClass c : prog.getClasses()) {
                if (imTr.canonical(c) == c) {
                    nominal.add(c);
                }
            }
            // Compile-time code which read a type id may have put it into the script, so those ids stay.
            Map<ImClass, Integer> before = imTr.typeIdsBeforeGenerics();
            result = before == null
                ? TypeId.calculate(nominal, this::nominalSuperClasses)
                : TypeId.keeping(before, nominal, this::nominalSuperClasses);
            typeIds = result;
        }
        return result;
    }

    /**
     * The superclasses of {@code c} as classes with a type id: a class can extend a specialisation, which stands for
     * the class it was copied from.
     */
    List<ImClass> nominalSuperClasses(ImClass c) {
        List<ImClass> result = new ArrayList<>();
        for (ImClassType sc : c.getSuperClasses()) {
            ImClass nominal = imTr.canonical(sc.getClassDef());
            if (!result.contains(nominal)) {
                result.add(nominal);
            }
        }
        return result;
    }

    public int getTypeId(ImClass classDef) {
        ImClass nominal = imTr.canonical(classDef);
        Integer id = typeIds().get(nominal);
        if (id == null) {
            throw new IllegalStateException("Lua backend: no type id for " + classDef.getName()
                + ", whose class " + nominal.getName() + " is not in the program");
        }
        return id;
    }


    public LuaFunction getErrorFunc() {
        return errorFunc.get();
    }

    /**
     * The Lua names of the package init functions which are still in the program, in the order of
     * {@code ImTranslator.luaInitFunctions}. The generated script calls each in an xpcall
     * ({@code StmtTranslation.emitLuaInitXpcall}); the test harness watches them.
     */
    /** The Lua names of the program's functions called {@code imName}, for a test harness which replaces one. */
    public List<String> functionNames(String imName) {
        List<String> names = new ArrayList<>();
        for (ImFunction f : prog.getFunctions()) {
            if (f.getName().equals(imName)) {
                names.add(luaFunc.getFor(f).getName());
            }
        }
        return names;
    }

    public List<String> initFunctionNames() {
        List<String> names = new ArrayList<>();
        for (ImFunction f : imTr.luaInitFunctions.keySet()) {
            if (f.getParent() != null) {
                names.add(luaFunc.getFor(f).getName());
            }
        }
        return names;
    }

    public String getTypeCastingFunctionName(ImFunction f) {
        de.peeeq.wurstscript.ast.Element trace = f.attrTrace();
        if (trace instanceof FuncDef fd && fd.attrNearestPackage() instanceof WPackage p) {
            if ("TypeCasting".equals(p.getName())) {
                return fd.getName();
            }
        }
        return null;
    }
}
