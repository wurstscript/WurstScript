package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.Flatten;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.*;

/**
 * Removes a write of a constant to a field of an object allocated in the same statement list, which the list writes
 * again before anything can read it.
 * <p>
 * Between the two writes there must be no read of the field through any object (so an alias cannot see the first
 * value), no call (a callee can read anything, a native can run Wurst code through an event), nothing which can stop
 * the thread or leave the list (a division, a deallocation, which fails on a double free, {@code exitwhen},
 * {@code return}: the first value would then be what remains) and no statement with statement lists of its own
 * (whose paths are not followed). The object is told by value: a local copied from another names the same object
 * until either is assigned again. It must have been allocated in the list, so it is not null and the first write
 * cannot fail either. An allocation reads no field and runs no Wurst code, so it is not in the way.
 * <p>
 * This removes the defaults an allocation writes on Lua where the constructor sets the field
 * ({@code LuaFieldDefaults}). Fields are only still fields on Lua: on Jass the class elimination has turned them into
 * arrays before the local optimisations run.
 */
public class RedundantFieldStores implements OptimizerPass {

    private ImTranslator trans;
    private int removed;

    @Override
    public int optimize(ImTranslator trans) {
        this.trans = trans;
        removed = 0;
        for (ImFunction func : ImHelper.calculateFunctionsOfProg(trans.getImProg())) {
            if (!func.isNative() && !func.isBj()) {
                optimizeStatements(func.getBody());
            }
        }
        return removed;
    }

    @Override
    public String getName() {
        return "Redundant field stores removed";
    }

    /** A field, by its storage, of the object a value number stands for. */
    private record Slot(ImVar field, int object) {
    }

    private void optimizeStatements(ImStmts stmts) {
        Map<ImVar, Integer> objectOf = new IdentityHashMap<>();
        int[] nextObject = {0};
        Set<Integer> allocated = new HashSet<>();
        Map<Slot, ImSet> pending = new HashMap<>();
        Set<ImStmt> dead = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ImStmt s : stmts) {
            if (hasStatementLists(s)) {
                // Its paths can assign the locals, so their values are told apart afresh after it.
                pending.clear();
                objectOf.clear();
                allocated.clear();
                s.accept(new Element.DefaultVisitor() {
                    @Override
                    public void visit(ImStmts nested) {
                        optimizeStatements(nested);
                    }
                });
                continue;
            }
            if (isBarrier(s)) {
                pending.clear();
            }
            if (s instanceof ImSet set) {
                forgetReadFields(set.getRight(), pending);
                if (set.getLeft() instanceof ImMemberAccess target) {
                    forgetReadFields(target.getReceiver(), pending);
                    forgetReadFields(target.getIndexes(), pending);
                    Integer object = target.getIndexes().isEmpty()
                        && target.getReceiver() instanceof ImVarAccess receiver
                        && !receiver.getVar().isGlobal() ? objectOf.get(receiver.getVar()) : null;
                    if (object != null && allocated.contains(object)) {
                        Slot slot = new Slot(trans.canonical(target.getVar()), object);
                        ImSet earlier = pending.remove(slot);
                        if (earlier != null) {
                            dead.add(earlier);
                        }
                        if (isConstant(set.getRight())) {
                            pending.put(slot, set);
                        }
                    }
                } else {
                    forgetReadFields(set.getLeft(), pending);
                    if (set.getLeft() instanceof ImVarAccess assigned && !assigned.getVar().isGlobal()) {
                        if (set.getRight() instanceof ImVarAccess copied && !copied.getVar().isGlobal()) {
                            objectOf.put(assigned.getVar(),
                                objectOf.computeIfAbsent(copied.getVar(), v -> nextObject[0]++));
                        } else {
                            int object = nextObject[0]++;
                            objectOf.put(assigned.getVar(), object);
                            if (set.getRight() instanceof ImAlloc) {
                                allocated.add(object);
                            }
                        }
                    }
                }
            } else {
                forgetReadFields(s, pending);
            }
        }
        if (!dead.isEmpty()) {
            stmts.removeIf(dead::contains);
            removed += dead.size();
        }
    }

    private static boolean hasStatementLists(ImStmt s) {
        return s instanceof ImIf || s instanceof ImLoop || s instanceof ImVarargLoop;
    }

    /**
     * Whether running {@code s} can read any field or leave the list with the first value in place: a call, a
     * deallocation or a division, which can stop the thread, or an {@code exitwhen} or {@code return}.
     */
    private static boolean isBarrier(ImStmt s) {
        if (s instanceof ImExitwhen || s instanceof ImReturn) {
            return true;
        }
        boolean[] barrier = {false};
        s.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                barrier[0] = true;
            }

            @Override
            public void visit(ImMethodCall call) {
                barrier[0] = true;
            }

            @Override
            public void visit(ImDealloc dealloc) {
                barrier[0] = true;
            }

            @Override
            public void visit(ImOperatorCall call) {
                super.visit(call);
                if (Flatten.mayStopTheThread(call)) {
                    barrier[0] = true;
                }
            }
        });
        return barrier[0];
    }

    /** Forgets the pending writes of every field {@code e} reads, through whatever object. */
    private void forgetReadFields(Element e, Map<Slot, ImSet> pending) {
        if (pending.isEmpty()) {
            return;
        }
        e.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImMemberAccess access) {
                super.visit(access);
                ImVar field = trans.canonical(access.getVar());
                pending.keySet().removeIf(slot -> slot.field() == field);
            }
        });
    }

    private static boolean isConstant(ImExpr e) {
        return e instanceof ImIntVal || e instanceof ImRealVal || e instanceof ImBoolVal
            || e instanceof ImStringVal || e instanceof ImNull;
    }
}
