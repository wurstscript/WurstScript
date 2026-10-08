package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImMethodCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImTypeArgument;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.validation.NamePreservation;
import io.vavr.control.Either;
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
     * of generics; before it, use {@link #removeUnreachableFunctionsBeforeGenerics}, since a generic function calls
     * what the type arguments it is given bind.
     *
     * @return the number of functions removed
     */
    public static int removeUnreachableFunctions(ImTranslator trans) {
        return remove(trans, false);
    }

    /**
     * The same before the generics are specialised. A generic function calls through its type arguments, so what the
     * type arguments in reachable code bind is reachable too: the implementations a type class binding names, and
     * the function which constructs a class given as a type argument (the specialisation of {@code wurstNewInstance}
     * calls it).
     *
     * @return the number of functions removed
     */
    public static int removeUnreachableFunctionsBeforeGenerics(ImTranslator trans) {
        return remove(trans, true);
    }

    private static int remove(ImTranslator trans, boolean followTypeArguments) {
        ImProg prog = trans.imProg();
        Reachability reachability = new Reachability(trans, Math.max(16, prog.getFunctions().size() / 2), followTypeArguments);
        reachability.add(trans.getMainFunc());
        reachability.add(trans.getConfFunc());
        for (ImFunction function : ImHelper.calculateFunctionsOfProg(prog)) {
            if (NamePreservation.isPreserved(function)) {
                reachability.add(function);
            }
        }
        for (ImFunction pinned : trans.pinnedFunctions()) {
            reachability.add(pinned);
        }
        reachability.run();

        Set<ImFunction> reachable = reachability.reachable;
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

    /** What a function reaches: what it calls and refers to, and, before the generics, what its type arguments bind. */
    private static final class Reachability extends ImFunction.DefaultVisitor {
        private final ImTranslator trans;
        private final boolean followTypeArguments;
        private final Set<ImFunction> reachable;
        private final Set<ImClass> constructedClasses = new ReferenceOpenHashSet<>();
        private final ArrayDeque<ImFunction> work = new ArrayDeque<>();

        private Reachability(ImTranslator trans, int expectedFunctions, boolean followTypeArguments) {
            this.trans = trans;
            this.followTypeArguments = followTypeArguments;
            this.reachable = new ReferenceOpenHashSet<>(expectedFunctions);
        }

        void add(ImFunction function) {
            if (function != null && !reachable.contains(function)) {
                work.add(function);
            }
        }

        void run() {
            while (!work.isEmpty()) {
                ImFunction f = work.removeLast();
                if (reachable.add(f)) {
                    f.accept(this);
                }
            }
        }

        private void addMethod(ImMethod method) {
            for (ImMethod sub : method.getSubMethods()) {
                add(sub.getImplementation());
            }
            add(method.getImplementation());
        }

        @Override
        public void visit(ImFunctionCall e) {
            super.visit(e);
            add(e.getFunc());
        }

        @Override
        public void visit(ImFuncRef e) {
            super.visit(e);
            add(e.getFunc());
        }

        @Override
        public void visit(ImMethodCall e) {
            super.visit(e);
            addMethod(e.getMethod());
        }

        @Override
        public void visit(ImTypeArgument argument) {
            super.visit(argument);
            if (!followTypeArguments) {
                return;
            }
            for (Either<ImMethod, ImFunction> implementation : argument.getTypeClassBinding().values()) {
                if (implementation.isLeft()) {
                    addMethod(implementation.getLeft());
                } else {
                    add(implementation.get());
                }
            }
            if (argument.getType() instanceof ImClassType classType) {
                addConstruction(classType.getClassDef());
            }
        }

        /** {@code wurstNewInstance<C>()} becomes a call of the function which constructs C with its zero-argument constructor. */
        private void addConstruction(ImClass imClass) {
            if (!constructedClasses.add(imClass) || !(imClass.getTrace() instanceof ClassDef classDef)) {
                return;
            }
            for (ConstructorDef constructor : classDef.getConstructors()) {
                if (constructor.getParameters().isEmpty()) {
                    add(trans.constructNewFuncIfTranslated(constructor));
                }
            }
        }
    }
}
