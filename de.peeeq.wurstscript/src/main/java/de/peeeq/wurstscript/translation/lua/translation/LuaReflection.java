package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.datastructures.Partitions;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.EliminateClasses;
import de.peeeq.wurstscript.utils.Lazy;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

import static de.peeeq.wurstscript.translation.imtranslation.EliminateClasses.INSTANCE_COUNT;
import static de.peeeq.wurstscript.translation.imtranslation.EliminateClasses.MAX_INSTANCE_COUNT;
import static de.peeeq.wurstscript.translation.imtranslation.EliminateClasses.MAX_TYPE_ID;
import static de.peeeq.wurstscript.translation.imtranslation.EliminateClasses.TYPE_ID_TO_TYPE_NAME;

/**
 * The natives of the standard library's package {@code Reflection} on Lua. They answer what the class elimination
 * answers on Jass ({@link EliminateClasses}):
 * <ul>
 * <li>a type id numbers a class of the program, from 1 ({@code TypeId}); {@code maxTypeId} is the largest, -1 when
 * there is no class;</li>
 * <li>{@code typeIdToTypeName} is the class's qualified name, {@code "unknown"} for any other number;</li>
 * <li>{@code instanceCount} is the number of live objects of the class's allocation group, every class joined to it
 * by inheritance (on Jass one allocator serves the group, {@code ImTranslator.buildClassPartitions}), and
 * {@code maxInstanceCount} the most objects the group had alive at once, which on Jass is its highest id; both are 0
 * for any other number.</li>
 * </ul>
 * A call is printed in place, so the natives have no definition. A targeted specialisation of a class keeps the
 * nominal identity of the class it was copied from, so the two are one group. Lua has one allocator for every class
 * (docs/WC3_RUNTIME.md "Objects and memory"), so a group's live objects and its peak are counters which the
 * allocation and the destroy of its classes update: only in a program which asks for them, only for the groups it can
 * ask about, and the peak only for those {@code maxInstanceCount} asks about.
 */
final class LuaReflection {
    /** The field of a class table which holds the number of the class's counted group, read on destroy. */
    static final String INSTANCE_GROUP = "__wurst_instanceGroup";
    private static final String UNKNOWN_TYPE_NAME = "unknown";
    private static final Set<String> NATIVES = Set.of(TYPE_ID_TO_TYPE_NAME, MAX_TYPE_ID, INSTANCE_COUNT,
        MAX_INSTANCE_COUNT);

    private final LuaTranslator tr;
    private final int maxTypeId;
    /** The class of each type id, null where there is none. */
    private final @Nullable ImClass[] classOfTypeId;
    private final Partitions<ImClass> groups = new Partitions<>();
    private final Set<ImClass> askedGroups = new HashSet<>();
    private final Set<ImClass> askedPeakGroups = new HashSet<>();
    private final Set<ImClass> allocatedClasses = new HashSet<>();
    private boolean askedAllGroups;
    private boolean askedAllPeaks;
    private boolean namesByTypeId;
    /** The number of each counted group, by its representative, numbered in the order of the type ids. */
    private final Map<ImClass, Integer> countedGroups = new LinkedHashMap<>();
    private final Set<ImClass> peakGroups = new HashSet<>();
    private @Nullable LuaVariable live;
    private @Nullable LuaVariable peak;
    private @Nullable LuaVariable typeGroups;
    private @Nullable LuaVariable typeNames;
    private final Lazy<LuaFunction> countedDealloc = Lazy.create(this::createCountedDealloc);

    LuaReflection(LuaTranslator tr) {
        this.tr = tr;
        // Only the classes with a type id of their own: a targeted specialisation is its class (getTypeId).
        Map<ImClass, Integer> typeIds = tr.typeIds();
        maxTypeId = EliminateClasses.calculateMaxTypeId(typeIds);
        classOfTypeId = new ImClass[Math.max(maxTypeId, 0) + 1];
        for (Map.Entry<ImClass, Integer> e : typeIds.entrySet()) {
            classOfTypeId[e.getValue()] = e.getKey();
        }
        for (ImClass c : typeIds.keySet()) {
            groups.add(c);
            for (ImClass sc : tr.nominalSuperClasses(c)) {
                groups.union(c, sc);
            }
        }
    }

    /** The counted group of {@code c}, by its representative: that of its class, for a specialisation. */
    private ImClass groupOf(ImClass c) {
        return groups.getRep(tr.imTr.canonical(c));
    }

    static boolean isReflectionNative(ImFunction f) {
        return f.isNative() && NATIVES.contains(f.getName());
    }

    /**
     * Whether {@code call} can tell how the classes are numbered: {@code maxTypeId}, or a type id which is not
     * {@code C.typeId} or {@code o.typeId}. The garbage removal then keeps every class, since Jass numbers the classes
     * nothing uses as well.
     */
    static boolean observesTypeIdNumbering(ImFunctionCall call) {
        if (!isReflectionNative(call.getFunc())) {
            return false;
        }
        if (call.getFunc().getName().equals(MAX_TYPE_ID)) {
            return true;
        }
        if (call.getArguments().size() != 1) {
            return true;
        }
        ImExpr typeId = call.getArguments().get(0);
        return !(typeId instanceof ImTypeIdOfClass) && !(typeId instanceof ImTypeIdOfObj);
    }

    /** Records a call of a native of {@code Reflection}, before anything is translated. */
    void noteCall(ImFunctionCall call) {
        String name = call.getFunc().getName();
        if (name.equals(MAX_TYPE_ID)) {
            return;
        }
        ImExpr typeId = typeIdArgument(call);
        if (name.equals(TYPE_ID_TO_TYPE_NAME)) {
            namesByTypeId |= !isKnown(typeId);
            return;
        }
        boolean peakToo = name.equals(MAX_INSTANCE_COUNT);
        if (!isKnown(typeId)) {
            askedAllGroups = true;
            askedAllPeaks |= peakToo;
            return;
        }
        ImClass c = knownClass(typeId);
        if (c != null) {
            askedGroups.add(groupOf(c));
            if (peakToo) {
                askedPeakGroups.add(groupOf(c));
            }
        }
    }

    void noteAllocation(ImClass c) {
        allocatedClasses.add(c);
    }

    /** Numbers the counted groups and declares the tables the calls read, after every call was noted. */
    void declare() {
        for (int id = 1; id <= maxTypeId; id++) {
            ImClass c = classOfTypeId[id];
            if (c == null) {
                continue;
            }
            ImClass group = groupOf(c);
            if (askedAllGroups || askedGroups.contains(group)) {
                countedGroups.putIfAbsent(group, countedGroups.size() + 1);
            }
            if (askedAllPeaks || askedPeakGroups.contains(group)) {
                peakGroups.add(group);
            }
        }
        // Every allocation and destroy of a counted class reads these, so they are main-chunk locals. A type id known
        // only at run time reads them even when there is no class to count.
        if (!countedGroups.isEmpty() || askedAllGroups) {
            live = LuaAst.LuaVariable(tr.uniqueName("__wurst_instanceLive"), zeros(countedGroups.size()));
            tr.declareChunkLocal(live);
        }
        if (!peakGroups.isEmpty() || askedAllPeaks) {
            peak = LuaAst.LuaVariable(tr.uniqueName("__wurst_instancePeak"), zeros(countedGroups.size()));
            tr.declareChunkLocal(peak);
        }
        if (askedAllGroups) {
            LuaTableFields groupOfTypeId = LuaAst.LuaTableFields();
            for (int id = 1; id <= maxTypeId; id++) {
                ImClass c = classOfTypeId[id];
                groupOfTypeId.add(LuaAst.LuaTableSingleField(c == null
                    ? LuaAst.LuaExprNull()
                    : LuaAst.LuaExprIntVal("" + countedGroups.get(groupOf(c)))));
            }
            typeGroups = LuaAst.LuaVariable(tr.uniqueName("__wurst_typeGroups"),
                LuaAst.LuaTableConstructor(groupOfTypeId));
            tr.luaModel.add(typeGroups);
        }
        if (namesByTypeId) {
            LuaTableFields nameOfTypeId = LuaAst.LuaTableFields();
            for (int id = 1; id <= maxTypeId; id++) {
                ImClass c = classOfTypeId[id];
                nameOfTypeId.add(LuaAst.LuaTableSingleField(c == null
                    ? LuaAst.LuaExprNull()
                    : LuaAst.LuaExprStringVal(EliminateClasses.calculateClassName(c))));
            }
            typeNames = LuaAst.LuaVariable(tr.uniqueName("__wurst_typeNames"),
                LuaAst.LuaTableConstructor(nameOfTypeId));
            tr.luaModel.add(typeNames);
        }
    }

    private static LuaExpr zeros(int n) {
        LuaTableFields fields = LuaAst.LuaTableFields();
        for (int i = 0; i < n; i++) {
            fields.add(LuaAst.LuaTableSingleField(LuaAst.LuaExprIntVal("0")));
        }
        return LuaAst.LuaTableConstructor(fields);
    }

    /** Whether the program allocates objects of {@code c} and counts them, which needs an allocator of its own. */
    boolean countsAllocationsOf(ImClass c) {
        return allocatedClasses.contains(c) && countedGroup(c) != null;
    }

    private @Nullable Integer countedGroup(ImClass c) {
        return countedGroups.isEmpty() ? null : countedGroups.get(groupOf(c));
    }

    /** The call in place, which evaluates its argument once. */
    LuaExpr translateCall(ImFunctionCall call) {
        String name = call.getFunc().getName();
        if (name.equals(MAX_TYPE_ID)) {
            if (!call.getArguments().isEmpty()) {
                throw new CompileError(call.attrTrace().attrSource(), name + " expects no arguments");
            }
            return LuaAst.LuaExprIntVal("" + maxTypeId);
        }
        ImExpr typeId = typeIdArgument(call);
        if (name.equals(TYPE_ID_TO_TYPE_NAME)) {
            if (isKnown(typeId)) {
                ImClass c = knownClass(typeId);
                return LuaAst.LuaExprStringVal(c == null ? UNKNOWN_TYPE_NAME : EliminateClasses.calculateClassName(c));
            }
            return LuaAst.LuaExprBinary(
                LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(Objects.requireNonNull(typeNames)),
                    LuaAst.LuaExprlist(typeId.translateToLua(tr))),
                LuaAst.LuaOpOr(), LuaAst.LuaExprStringVal(UNKNOWN_TYPE_NAME));
        }
        LuaVariable counters = name.equals(MAX_INSTANCE_COUNT) ? peak : live;
        if (isKnown(typeId)) {
            ImClass c = knownClass(typeId);
            if (c == null) {
                return LuaAst.LuaExprIntVal("0");
            }
            return counter(Objects.requireNonNull(counters), Objects.requireNonNull(countedGroup(c)));
        }
        LuaExpr group = LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(Objects.requireNonNull(typeGroups)),
            LuaAst.LuaExprlist(typeId.translateToLua(tr)));
        return LuaAst.LuaExprBinary(
            LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(Objects.requireNonNull(counters)),
                LuaAst.LuaExprlist(group)),
            LuaAst.LuaOpOr(), LuaAst.LuaExprIntVal("0"));
    }

    private static ImExpr typeIdArgument(ImFunctionCall call) {
        if (call.getArguments().size() != 1) {
            throw new CompileError(call.attrTrace().attrSource(),
                call.getFunc().getName() + " expects exactly one argument, a type id");
        }
        return call.getArguments().get(0);
    }

    /** Whether the class a type id names, or that it names none, is known before the program runs. */
    private static boolean isKnown(ImExpr typeId) {
        return typeId instanceof ImTypeIdOfClass || typeId instanceof ImIntVal;
    }

    private @Nullable ImClass knownClass(ImExpr typeId) {
        if (typeId instanceof ImTypeIdOfClass t) {
            return t.getClazz().getClassDef();
        }
        int id = ((ImIntVal) typeId).getValI();
        return id >= 1 && id <= maxTypeId ? classOfTypeId[id] : null;
    }

    private static LuaExpr counter(LuaVariable counters, int group) {
        return LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(counters),
            LuaAst.LuaExprlist(LuaAst.LuaExprIntVal("" + group)));
    }

    /**
     * Counts an allocation of {@code c} at the end of its allocator, {@code body}: the group's live objects, and its
     * peak where that is asked for.
     */
    void addAllocationCount(LuaStatements body, ImClass c) {
        Integer group = countedGroup(c);
        if (group == null || !allocatedClasses.contains(c)) {
            return;
        }
        LuaVariable liveCounters = Objects.requireNonNull(live);
        LuaExpr incremented = LuaAst.LuaExprBinary(counter(liveCounters, group), LuaAst.LuaOpPlus(),
            LuaAst.LuaExprIntVal("1"));
        if (!peakGroups.contains(groupOf(c))) {
            body.add(LuaAst.LuaAssignment(counter(liveCounters, group), incremented));
            return;
        }
        LuaVariable count = LuaAst.LuaVariable("live", incremented);
        body.add(count);
        body.add(LuaAst.LuaAssignment(counter(liveCounters, group), LuaAst.LuaExprVarAccess(count)));
        LuaVariable peakCounters = Objects.requireNonNull(peak);
        body.add(LuaAst.LuaIf(
            LuaAst.LuaExprBinary(LuaAst.LuaExprVarAccess(count), LuaAst.LuaOpGreater(), counter(peakCounters, group)),
            LuaAst.LuaStatements(LuaAst.LuaAssignment(counter(peakCounters, group), LuaAst.LuaExprVarAccess(count))),
            LuaAst.LuaStatements()));
    }

    /**
     * The class table field which tells the destroy of an object of {@code c} its counted group, or null. Only an
     * allocated class has objects to destroy.
     */
    @Nullable LuaStatement groupField(ImClass c, LuaVariable classVar) {
        Integer group = countedGroup(c);
        if (group == null || !allocatedClasses.contains(c)) {
            return null;
        }
        return LuaAst.LuaAssignment(LuaAst.LuaExprFieldAccess(LuaAst.LuaExprVarAccess(classVar), INSTANCE_GROUP),
            LuaAst.LuaExprIntVal("" + group));
    }

    /**
     * What destroys an object whose class is {@code c}, or a subclass of it: the shared deallocator, or for a counted
     * group one which counts the destroy as well. An object is always of the group of the class it is destroyed as.
     */
    LuaFunction deallocFor(ImClass c) {
        return countedGroup(c) != null ? countedDealloc.get() : tr.objectDealloc;
    }

    private LuaFunction createCountedDealloc() {
        LuaFunction dealloc = LuaAst.LuaFunction(tr.uniqueName("__wurst_deallocCounted"), LuaAst.LuaParams(),
            LuaAst.LuaStatements());
        LuaVariable descriptor = tr.addDeallocation(dealloc);
        LuaVariable group = LuaAst.LuaVariable("group",
            LuaAst.LuaExprFieldAccess(LuaAst.LuaExprVarAccess(descriptor), INSTANCE_GROUP));
        dealloc.getBody().add(group);
        LuaVariable liveCounters = Objects.requireNonNull(live);
        LuaExpr groupCounter = LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(liveCounters),
            LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(group)));
        dealloc.getBody().add(LuaAst.LuaAssignment(groupCounter,
            LuaAst.LuaExprBinary(groupCounter.copy(), LuaAst.LuaOpMinus(), LuaAst.LuaExprIntVal("1"))));
        // Called by every destroy of a counted class, so a local function like the shared deallocator.
        tr.declareChunkLocal(dealloc);
        return dealloc;
    }
}
