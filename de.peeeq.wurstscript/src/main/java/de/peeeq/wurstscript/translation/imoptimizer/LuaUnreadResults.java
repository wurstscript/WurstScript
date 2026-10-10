package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.intermediatelang.optimizer.SideEffectAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.LuaMultipleResults;
import de.peeeq.wurstscript.validation.NamePreservation;

import java.util.*;

/**
 * Drops a multiple result ({@link LuaMultipleResults}) which no call reads. On Jass such a component goes through a
 * global of its function, which the garbage removal drops, value and all, when nothing reads it; on Lua it is one of the
 * values the function returns, which only this removes.
 * <p>
 * The functions which have to agree on their results form a group: the implementations of the methods one call can
 * dispatch between, and the functions whose calls write the same results local (LocalMerger can give two of them one
 * variable). A component goes when no results local of the group reads it: every function of the group stops returning
 * it, and the selections of the later components move down. What a dropped value does besides being returned stays, at
 * its place in the order of evaluation; what it read becomes garbage for the removal which follows.
 */
final class LuaUnreadResults {

    private final ImOptimizer optimizer;
    private final ImTranslator trans;
    /** Union-find over the functions which return multiple results, the methods and the results locals. */
    private final Map<Object, Object> parent = new IdentityHashMap<>();
    /** For each results local, the components it reads. */
    private final Map<ImVar, BitSet> reads = new LinkedHashMap<>();
    /** The results locals used other than by a selection or as the target of a call (none, by the shape). */
    private final Set<ImVar> escaping = Collections.newSetFromMap(new IdentityHashMap<>());

    private LuaUnreadResults(ImOptimizer optimizer, ImTranslator trans) {
        this.optimizer = optimizer;
        this.trans = trans;
    }

    /** @return whether a result was dropped */
    static boolean drop(ImOptimizer optimizer, ImTranslator trans) {
        return new LuaUnreadResults(optimizer, trans).drop();
    }

    private boolean drop() {
        ImProg prog = trans.getImProg();
        Set<ImFunction> functions = ImHelper.calculateFunctionsOfProg(prog);
        List<ImFunction> producers = new ArrayList<>();
        for (ImFunction f : functions) {
            if (f.getReturnType() instanceof ImTupleType) {
                producers.add(f);
            }
        }
        if (producers.isEmpty()) {
            return false;
        }
        for (ImMethod method : allMethods(prog)) {
            for (ImMethod subMethod : method.getSubMethods()) {
                union(method, subMethod);
            }
            if (method.getImplementation() != null) {
                union(method, method.getImplementation());
            }
        }
        for (ImFunction f : functions) {
            collectResultsLocals(f);
        }

        Map<Object, List<ImFunction>> producersByGroup = new LinkedHashMap<>();
        for (ImFunction producer : producers) {
            producersByGroup.computeIfAbsent(find(producer), g -> new ArrayList<>()).add(producer);
        }
        Map<Object, List<ImVar>> localsByGroup = new LinkedHashMap<>();
        for (ImVar local : reads.keySet()) {
            localsByGroup.computeIfAbsent(find(local), g -> new ArrayList<>()).add(local);
        }

        boolean changed = false;
        SideEffectAnalyzer analyzer = new SideEffectAnalyzer(prog);
        for (Map.Entry<Object, List<ImFunction>> group : producersByGroup.entrySet()) {
            List<ImFunction> members = group.getValue();
            List<ImVar> locals = localsByGroup.getOrDefault(group.getKey(), Collections.emptyList());
            int arity = ((ImTupleType) members.get(0).getReturnType()).getTypes().size();
            BitSet read = new BitSet();
            boolean keepAll = false;
            for (ImFunction member : members) {
                keepAll |= NamePreservation.isPreserved(member)
                    || ((ImTupleType) member.getReturnType()).getTypes().size() != arity;
            }
            for (ImVar local : locals) {
                keepAll |= escaping.contains(local);
                read.or(reads.get(local));
            }
            if (keepAll || read.cardinality() == arity) {
                continue;
            }
            dropFrom(members, locals, read, analyzer);
            changed = true;
        }
        return changed;
    }

    /** The methods of the program and of its classes, and their sub-methods, however deep. */
    private static List<ImMethod> allMethods(ImProg prog) {
        Set<ImMethod> known = Collections.newSetFromMap(new IdentityHashMap<>());
        List<ImMethod> result = new ArrayList<>();
        for (ImMethod method : prog.getMethods()) {
            if (known.add(method)) {
                result.add(method);
            }
        }
        for (ImClass c : prog.getClasses()) {
            for (ImMethod method : c.getMethods()) {
                if (known.add(method)) {
                    result.add(method);
                }
            }
        }
        for (int i = 0; i < result.size(); i++) {
            for (ImMethod subMethod : result.get(i).getSubMethods()) {
                if (known.add(subMethod)) {
                    result.add(subMethod);
                }
            }
        }
        return result;
    }

    private void collectResultsLocals(ImFunction f) {
        boolean any = false;
        for (ImVar local : f.getLocals()) {
            if (LuaMultipleResults.isResultsLocal(local)) {
                reads.put(local, new BitSet());
                any = true;
            }
        }
        if (!any) {
            return;
        }
        f.getBody().accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImSet set) {
                super.visit(set);
                if (set.getLeft() instanceof ImVarAccess target && reads.containsKey(target.getVar())) {
                    ImExpr value = LuaMultipleResults.valueOf(set.getRight());
                    if (value instanceof ImFunctionCall call) {
                        union(target.getVar(), call.getFunc());
                    } else if (value instanceof ImMethodCall call) {
                        union(target.getVar(), call.getMethod());
                    } else {
                        escaping.add(target.getVar());
                    }
                }
            }

            @Override
            public void visit(ImVarAccess access) {
                super.visit(access);
                ImVar v = access.getVar();
                if (!reads.containsKey(v)) {
                    return;
                }
                if (access.getParent() instanceof ImTupleSelection selection) {
                    reads.get(v).set(selection.getTupleIndex());
                } else if (!(access.getParent() instanceof ImSet set && set.getLeft() == access)) {
                    escaping.add(v);
                }
            }
        });
    }

    private void dropFrom(List<ImFunction> members, List<ImVar> locals, BitSet read, SideEffectAnalyzer analyzer) {
        ImTupleType results = (ImTupleType) members.get(0).getReturnType();
        int arity = results.getTypes().size();
        // the new place of each kept component
        int[] newIndex = new int[arity];
        List<ImType> keptTypes = new ArrayList<>();
        List<String> keptNames = new ArrayList<>();
        for (int k = 0; k < arity; k++) {
            newIndex[k] = keptTypes.size();
            if (read.get(k)) {
                keptTypes.add(results.getTypes().get(k).copy());
                keptNames.add(results.getNames().get(k));
            }
        }
        ImType newType = keptTypes.size() >= 2 ? JassIm.ImTupleType(keptTypes, keptNames)
            : keptTypes.size() == 1 ? keptTypes.get(0) : JassIm.ImVoid();

        for (ImFunction member : members) {
            List<ImReturn> returns = new ArrayList<>();
            member.getBody().accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ImReturn ret) {
                    super.visit(ret);
                    returns.add(ret);
                }
            });
            for (ImReturn ret : returns) {
                dropFromReturn(member, ret, read, analyzer);
            }
            member.setReturnType(newType.copy());
            trans.functionChanged(member);
        }

        for (ImVar local : locals) {
            ImFunction owner = (ImFunction) local.getParent().getParent();
            List<ImVarAccess> accesses = new ArrayList<>();
            owner.getBody().accept(new Element.DefaultVisitor() {
                @Override
                public void visit(ImVarAccess access) {
                    super.visit(access);
                    if (access.getVar() == local) {
                        accesses.add(access);
                    }
                }
            });
            for (ImVarAccess access : accesses) {
                if (access.getParent() instanceof ImTupleSelection selection) {
                    if (keptTypes.size() >= 2) {
                        selection.setTupleIndex(newIndex[selection.getTupleIndex()]);
                    } else {
                        access.setParent(null);
                        selection.replaceBy(access);
                    }
                } else if (keptTypes.isEmpty() && access.getParent() instanceof ImSet set) {
                    // nothing is read: the call stays for what it does (with the statements in front of it)
                    ImExpr call = set.getRight();
                    call.setParent(null);
                    set.replaceBy(call);
                }
            }
            if (keptTypes.isEmpty()) {
                owner.getLocals().remove(local);
            } else {
                local.setType(newType.copy());
            }
            trans.functionChanged(owner);
        }
    }

    /**
     * Drops the unread components from what {@code ret} returns. A dropped component which does something (a call, a
     * division which may stop the thread) keeps doing it where it was evaluated: before it go the statements of what it
     * does, and the kept components before it which a call could change (or which could fail) are first saved in
     * locals, so they are still evaluated before it.
     */
    private void dropFromReturn(ImFunction f, ImReturn ret, BitSet read, SideEffectAnalyzer analyzer) {
        if (!(ret.getReturnValue() instanceof ImTupleExpr tuple) || !(ret.getParent() instanceof ImStmts statements)) {
            throw new IllegalStateException("Lua multiple results: a return which is not of a tuple expression in a "
                + "statement list: " + ret + "\nin " + f);
        }
        List<ImExpr> components = new ArrayList<>(tuple.getExprs());
        List<List<ImExpr>> effects = new ArrayList<>();
        int lastEffect = -1;
        for (int k = 0; k < components.size(); k++) {
            List<ImExpr> componentEffects = read.get(k)
                ? Collections.emptyList()
                : optimizer.collectSideEffects(components.get(k), analyzer);
            effects.add(componentEffects);
            if (!componentEffects.isEmpty()) {
                lastEffect = k;
            }
        }
        List<ImStmt> before = new ArrayList<>();
        for (int k = 0; k <= lastEffect; k++) {
            ImExpr component = components.get(k);
            if (read.get(k) && !isStable(component)) {
                ImVar saved = JassIm.ImVar(component.attrTrace(), component.attrTyp(), "tuple_result", false);
                f.getLocals().add(saved);
                component.setParent(null);
                before.add(JassIm.ImSet(ret.getTrace(), JassIm.ImVarAccess(saved), component));
                components.set(k, JassIm.ImVarAccess(saved));
            }
            for (ImExpr effect : effects.get(k)) {
                effect.setParent(null);
                effect.flatten(trans, f).intoStatements(before, trans, f);
            }
        }
        ImExprs kept = JassIm.ImExprs();
        for (int k = 0; k < components.size(); k++) {
            if (read.get(k)) {
                ImExpr component = components.get(k);
                component.setParent(null);
                kept.add(component);
            }
        }
        ret.setReturnValue(kept.size() >= 2 ? JassIm.ImTupleExpr(kept)
            : kept.size() == 1 ? kept.remove(0) : JassIm.ImNoExpr());
        if (!before.isEmpty()) {
            int position = 0;
            while (statements.get(position) != ret) {
                position++;
            }
            statements.addAll(position, before);
        }
    }

    /** A value no call evaluated before it can change, and which cannot fail: a literal, a local, a call's result. */
    private static boolean isStable(ImExpr e) {
        return e instanceof ImConst
            || e instanceof ImVarAccess access && !access.getVar().isGlobal()
            || e instanceof ImTupleSelection selection && selection.getTupleExpr() instanceof ImVarAccess access
            && LuaMultipleResults.isResultsLocal(access.getVar());
    }

    private Object find(Object x) {
        Object p = parent.get(x);
        if (p == null) {
            parent.put(x, x);
            return x;
        }
        if (p != x) {
            p = find(p);
            parent.put(x, p);
        }
        return p;
    }

    private void union(Object a, Object b) {
        Object rootA = find(a);
        Object rootB = find(b);
        if (rootA != rootB) {
            parent.put(rootB, rootA);
        }
    }
}
