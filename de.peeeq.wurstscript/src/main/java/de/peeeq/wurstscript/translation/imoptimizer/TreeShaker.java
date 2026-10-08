package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.validation.NamePreservation;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.ArrayDeque;
import java.util.Set;

/**
 * Drops the functions which nothing can reach, before the passes which would otherwise process them.
 * <p>
 * The translation of a project produces every function of every package it imports, and most of them are never
 * called: on castle fight 43,000 functions go into the passes after the specialisation of generics and 10,600 come
 * out, and each of those passes walks all of them. {@link ImOptimizer#removeGarbage} drops them too, but only after
 * the tuples are eliminated.
 * <p>
 * This removes functions and nothing else (no variable, no statement, no class member), so it is exactly the part of
 * garbage removal which is safe where it is called as long as no later pass calls a function it did not find in a
 * body: those are the functions the translator keeps for such passes ({@link ImTranslator#pinnedFunctions}), which
 * count as roots here.
 * <p>
 * The Lua pipeline only. The Jass pipeline's passes after the generics look at the whole program: the keyed-map
 * lowerings find the functions of the package which declares an intrinsic by name, whether anything calls them or not,
 * the class elimination builds a dispatch function over every override of a method, and the vararg lowering reports
 * a call in an unused function which does not fit Jass's 31 parameters.
 */
public final class TreeShaker {
    private TreeShaker() {
    }

    /**
     * Removes the functions of the program, and of its classes, which are not reachable from main, config, a preserved
     * function or a function the translator pinned. Call it where every call is concrete: after the specialisation
     * of generics, since a generic function calls what the type arguments it is given bind.
     *
     * @return the number of functions removed
     */
    public static int removeUnreachableFunctions(ImTranslator trans) {
        ImProg prog = trans.imProg();
        Set<ImFunction> reachable = new ReferenceOpenHashSet<>(Math.max(16, prog.getFunctions().size() / 2));
        ArrayDeque<ImFunction> work = new ArrayDeque<>();
        addRoot(work, trans.getMainFunc());
        addRoot(work, trans.getConfFunc());
        for (ImFunction function : ImHelper.calculateFunctionsOfProg(prog)) {
            if (NamePreservation.isPreserved(function)) {
                work.add(function);
            }
        }
        for (ImFunction pinned : trans.pinnedFunctions()) {
            work.add(pinned);
        }
        while (!work.isEmpty()) {
            ImFunction f = work.removeLast();
            if (!reachable.add(f)) {
                continue;
            }
            for (ImFunction called : f.calcUsedFunctions()) {
                if (called != null && !reachable.contains(called)) {
                    work.add(called);
                }
            }
        }

        int before = prog.getFunctions().size();
        prog.getFunctions().retainAll(reachable);
        int removed = before - prog.getFunctions().size();
        for (ImClass c : prog.getClasses()) {
            int classBefore = c.getFunctions().size();
            c.getFunctions().retainAll(reachable);
            removed += classBefore - c.getFunctions().size();
        }
        return removed;
    }

    private static void addRoot(ArrayDeque<ImFunction> work, ImFunction root) {
        if (root != null) {
            work.add(root);
        }
    }
}
