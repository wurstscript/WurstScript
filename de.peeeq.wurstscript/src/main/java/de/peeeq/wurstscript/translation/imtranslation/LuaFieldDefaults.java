package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.*;

/**
 * Writes the default of each field of a new object on Lua, as IM statements after its allocation, so that the
 * optimiser sees the ones the constructor overwrites.
 * <p>
 * Lua keeps a field in a table indexed by the object id. A slot nothing wrote reads as nil, where Jass reads 0,
 * and an id is reused, so a new object's fields are set to their defaults. The backend's allocation used to write
 * them all, out of the optimiser's sight, so a construction wrote each field the constructor sets twice: a damage
 * event wrote the 11 fields of DamageInstance 22 times.
 * <p>
 * {@link #materialize} runs after the inlining, when an allocation is followed by the constructor's own writes, and
 * the local optimisations remove a default which is written again before anything can read it
 * ({@code RedundantFieldStores}). {@link #moveSurvivorsToAllocations} runs before the backend: a default which
 * survives at some allocation of a class goes back into that class's shared allocation function, and its copies
 * after the allocations are removed, so a field leaves the allocation only where every allocation overwrote it, and
 * no construction writes more than before nor is the script bigger. Without the local optimisations every default
 * goes back; only the allocation function of a class nothing allocates (an abstract base), which nothing calls, is
 * left without them.
 * <p>
 * An array field gets a table of its own per object, which IM cannot express; the backend's allocation always makes
 * those ({@link #isArrayField}).
 */
public final class LuaFieldDefaults {

    private LuaFieldDefaults() {
    }

    /** Inserts the defaults after every allocation and returns how many allocations there were. */
    public static int materialize(ImProg prog, ImTranslator tr) {
        List<ImSet> allocations = new ArrayList<>();
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImAlloc alloc) {
                super.visit(alloc);
                if (!(alloc.getParent() instanceof ImSet set)
                    || set.getRight() != alloc
                    || !(set.getLeft() instanceof ImVarAccess)
                    || !(set.getParent() instanceof ImStmts)) {
                    throw new IllegalStateException("An allocation of " + alloc.getClazz()
                        + " which is not assigned to a variable as a statement: " + alloc.getParent());
                }
                allocations.add(set);
            }
        });
        for (ImSet allocation : allocations) {
            ImAlloc alloc = (ImAlloc) allocation.getRight();
            ImVar object = ((ImVarAccess) allocation.getLeft()).getVar();
            ImStmts stmts = (ImStmts) allocation.getParent();
            int index = stmts.indexOf(allocation);
            List<ImStmt> defaults = new ArrayList<>();
            for (ImVar field : fieldsOf(alloc.getClazz().getClassDef(), tr)) {
                if (isArrayField(field)) {
                    continue;
                }
                ImTypeArguments typeArguments = alloc.getClazz().getTypeArguments().copy();
                defaults.add(JassIm.ImSet(allocation.getTrace(),
                    JassIm.ImMemberAccess(allocation.getTrace(), JassIm.ImVarAccess(object), typeArguments, field,
                        JassIm.ImExprs()),
                    defaultValue(field.getType())));
            }
            stmts.addAll(index + 1, defaults);
        }
        tr.setLuaFieldDefaultsMaterialized();
        return allocations.size();
    }

    /**
     * Removes the defaults which still follow an allocation and returns, for each class allocated, the fields (by
     * storage) whose default survived at one of its allocations, which its allocation function writes instead.
     * <p>
     * A surviving default is found where {@link #materialize} put it: in the run of writes right after the
     * allocation, each setting a field of the object to exactly its default. Moving such a write into the
     * allocation, which runs where the allocation statement is, changes nothing: nothing runs between them,
     * whoever wrote it. At another allocation of the class, where the optimiser removed that default because the
     * field is set before anything reads it, the allocation's write is dead, which is what every allocation did
     * before.
     */
    public static Map<ImClass, Set<ImVar>> moveSurvivorsToAllocations(ImProg prog, ImTranslator tr) {
        if (!tr.luaFieldDefaultsMaterialized()) {
            throw new IllegalStateException("The field defaults were not written after the allocations "
                + "(LuaFieldDefaults.materialize), so the allocations would leave fields nil.");
        }
        Map<ImClass, Set<ImVar>> written = new LinkedHashMap<>();
        List<ImStmt> survivors = new ArrayList<>();
        prog.accept(new ImProg.DefaultVisitor() {
            @Override
            public void visit(ImStmts stmts) {
                super.visit(stmts);
                for (int i = 0; i < stmts.size(); i++) {
                    if (!(stmts.get(i) instanceof ImSet set) || !(set.getRight() instanceof ImAlloc alloc)
                        || !(set.getLeft() instanceof ImVarAccess object)) {
                        continue;
                    }
                    ImClass c = alloc.getClazz().getClassDef();
                    Map<ImVar, ImVar> fields = new IdentityHashMap<>();
                    for (ImVar field : fieldsOf(c, tr)) {
                        if (!isArrayField(field)) {
                            fields.put(tr.canonical(field), field);
                        }
                    }
                    for (int j = i + 1; j < stmts.size(); j++) {
                        ImVar field = defaultWrittenTo(stmts.get(j), object.getVar(), fields, tr);
                        if (field == null) {
                            break;
                        }
                        written.computeIfAbsent(c, k -> Collections.newSetFromMap(new IdentityHashMap<>()))
                            .add(field);
                        survivors.add(stmts.get(j));
                    }
                }
            }
        });
        for (ImStmt survivor : survivors) {
            ((ImStmts) survivor.getParent()).remove(survivor);
        }
        return written;
    }

    /** The field (by storage) which {@code s} sets to exactly its default on {@code object}, or null. */
    private static @org.eclipse.jdt.annotation.Nullable ImVar defaultWrittenTo(ImStmt s, ImVar object,
                                                                              Map<ImVar, ImVar> fields,
                                                                              ImTranslator tr) {
        if (!(s instanceof ImSet set) || !(set.getLeft() instanceof ImMemberAccess target)
            || !target.getIndexes().isEmpty()
            || !(target.getReceiver() instanceof ImVarAccess receiver) || receiver.getVar() != object) {
            return null;
        }
        ImVar storage = tr.canonical(target.getVar());
        ImVar field = fields.get(storage);
        if (field == null || !isDefault(set.getRight(), field.getType())) {
            return null;
        }
        return storage;
    }

    private static boolean isDefault(ImExpr e, ImType type) {
        if (type instanceof ImSimpleType simpleType) {
            if (TypesHelper.isIntType(simpleType)) {
                return e instanceof ImIntVal v && v.getValI() == 0;
            } else if (TypesHelper.isBoolType(simpleType)) {
                return e instanceof ImBoolVal v && !v.getValB();
            } else if (TypesHelper.isRealType(simpleType)) {
                if (!(e instanceof ImRealVal v)) {
                    return false;
                }
                double value = Double.parseDouble(v.getValR());
                return value == 0 && Double.doubleToRawLongBits(value) == 0;
            } else if (TypesHelper.isStringType(simpleType)) {
                return e instanceof ImStringVal v && v.getValS().isEmpty();
            }
        }
        return e instanceof ImNull;
    }

    /**
     * The fields an object of {@code c} has: those of its superclasses, in the order of their names, then its own,
     * each storage once ({@link ImTranslator#canonical}).
     */
    public static List<ImVar> fieldsOf(ImClass c, ImTranslator tr) {
        List<ImVar> result = new ArrayList<>();
        collectFields(c, tr, result, Collections.newSetFromMap(new IdentityHashMap<>()),
            Collections.newSetFromMap(new IdentityHashMap<>()));
        return result;
    }

    private static void collectFields(ImClass c, ImTranslator tr, List<ImVar> out, Set<ImClass> visitedClasses,
                                      Set<ImVar> visitedFields) {
        if (!visitedClasses.add(c)) {
            return;
        }
        List<ImClassType> superClasses = new ArrayList<>(c.getSuperClasses());
        superClasses.sort(Comparator.comparing(sc -> sc.getClassDef().getName()));
        for (ImClassType sc : superClasses) {
            collectFields(sc.getClassDef(), tr, out, visitedClasses, visitedFields);
        }
        for (ImVar field : c.getFields()) {
            if (visitedFields.add(tr.canonical(field))) {
                out.add(field);
            }
        }
    }

    /** An array field, whose default is a table of its own per object, which the backend makes. */
    public static boolean isArrayField(ImVar field) {
        return field.getType() instanceof ImArrayType || field.getType() instanceof ImArrayTypeMulti;
    }

    /** The value a field of {@code type} holds before anything writes it, as the Lua backend prints defaults. */
    private static ImExpr defaultValue(ImType type) {
        if (type instanceof ImSimpleType simpleType) {
            if (TypesHelper.isIntType(simpleType)) {
                return JassIm.ImIntVal(0);
            } else if (TypesHelper.isBoolType(simpleType)) {
                return JassIm.ImBoolVal(false);
            } else if (TypesHelper.isRealType(simpleType)) {
                return JassIm.ImRealVal("0.");
            } else if (TypesHelper.isStringType(simpleType)) {
                return JassIm.ImStringVal("");
            }
        } else if (type instanceof ImTupleType) {
            throw new IllegalStateException("A tuple field after the tuples were eliminated: " + type);
        }
        return JassIm.ImNull(type);
    }
}
