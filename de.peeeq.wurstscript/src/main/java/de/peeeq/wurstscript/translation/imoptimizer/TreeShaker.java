package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.jassIm.ImBoolVal;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImIntVal;
import de.peeeq.wurstscript.jassIm.ImLExpr;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImMethodCall;
import de.peeeq.wurstscript.jassIm.ImNull;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImRealVal;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImStringVal;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTypeArgument;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVarArrayAccess;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.UsedVariables;
import de.peeeq.wurstscript.validation.NamePreservation;
import io.vavr.control.Either;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Drops the functions and the globals which nothing can reach, before the passes which would otherwise process them.
 * <p>
 * The translation of a project produces every function and every global of every package it imports, and most of
 * them are never used: on castle fight 43,000 functions go into the passes after the specialisation of generics and
 * 10,600 come out, and 26,000 globals of which 5,100 are read, and each of those passes walks all of them.
 * {@link ImOptimizer#removeGarbage} drops them too, but only after the tuples are eliminated.
 * <p>
 * This removes functions, the methods whose implementation goes with them, and the globals which no reachable code
 * mentions, with the assignments to them which have nothing else to do (a constant, a read, an operator on such: not
 * a call). It removes no class, no field and no other statement, so it is the part of garbage removal which is safe
 * where it is called, as long as no later pass uses a function or a global it did not find in a body: those are the
 * ones the translator keeps for such passes ({@link ImTranslator#pinnedFunctions}, {@link ImTranslator#pinnedGlobals}),
 * which count as roots here.
 * <p>
 * Both pipelines run it three times: in front of the compile-time functions (which walk the whole program and analyse
 * it once for each function they split), before the generics (which specialise all they are given) and after them.
 * Each later run drops what the passes in between made unreachable, such as the functions only a compile-time
 * expression called, which the run replaced by its value.
 * <p>
 * The passes of the Jass pipeline which look at the whole program are served here, each at its origin:
 * <ul>
 * <li>The keyed-map lowerings find the functions of the package which declares an intrinsic by name, whether anything
 * calls them or not: the declarations of the compiler's own intrinsics are roots on Jass. The interpreter finds them
 * the same way, so in front of the compile-time run they are roots on Lua too.</li>
 * <li>The class elimination builds a dispatch function over every override of every method, called or not: a method
 * which nothing calls is removed with its implementation.</li>
 * <li>The vararg lowering reports a call which does not fit Jass's 31 parameters in a function nothing calls, which is
 * not in the script either: it no longer does.</li>
 * </ul>
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
        return remove(trans, false, false, Collections.emptyList());
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
        return remove(trans, true, false, Collections.emptyList());
    }

    /**
     * The same in front of the compile-time functions, where the generics are still in place as well. The run
     * executes the compile-time functions, which nothing calls, and evaluates the compile-time expressions of the
     * program, in functions nothing calls too, so none of them is reachable from main and the caller passes them as
     * roots. Everything else the run needs it reaches by a call, a reference or a type argument from there: it
     * interprets the IM, which holds the functions it calls and the bindings of its type arguments as nodes. Except
     * for the Jass fallbacks of the keyed-map intrinsics, which it runs them through and finds by name, the way the
     * Jass lowering does: the declarations of the compiler's intrinsics are roots here on Lua too.
     *
     * @param compiletimeRoots the functions the run executes or holds an expression it evaluates
     *                         ({@code CompiletimeFunctionRunner#functionsOfTheRun})
     * @return the number of functions removed
     */
    public static int removeUnreachableFunctionsBeforeCompiletime(ImTranslator trans, Collection<ImFunction> compiletimeRoots) {
        return remove(trans, true, true, compiletimeRoots);
    }

    private static int remove(ImTranslator trans, boolean followTypeArguments, boolean beforeCompiletime,
                              Collection<ImFunction> extraRoots) {
        ImProg prog = trans.imProg();
        Reachability reachability = new Reachability(trans, prog, followTypeArguments);
        reachability.add(trans.getMainFunc());
        reachability.add(trans.getConfFunc());
        boolean keepIntrinsics = beforeCompiletime || !trans.isLuaTarget();
        for (ImFunction function : ImHelper.calculateFunctionsOfProg(prog)) {
            if (NamePreservation.isPreserved(function) || (keepIntrinsics && isCompilerIntrinsic(function))) {
                reachability.add(function);
            }
        }
        for (ImFunction pinned : trans.pinnedFunctions()) {
            reachability.add(pinned);
        }
        for (ImFunction root : extraRoots) {
            reachability.add(root);
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
        removeUnusedMethods(prog, reachability.usedMethods);
        reachability.removeUnreadGlobals(prog, trans.pinnedGlobals());
        return removed;
    }

    /**
     * A method which nothing calls has lost its implementation unless something else reaches it, and a method
     * without one is a dangling reference: the class elimination builds a dispatch function over every method of a
     * class, called or not, and so would call the functions removed above (the Lua translation drops such methods
     * with its own garbage removal, later). A method which is used is dispatched to every override it has, and
     * those are used too.
     */
    private static void removeUnusedMethods(ImProg prog, Set<ImMethod> usedMethods) {
        prog.getMethods().retainAll(usedMethods);
        for (ImClass c : prog.getClasses()) {
            c.getMethods().retainAll(usedMethods);
        }
    }

    /**
     * Whether {@code f} is a declaration of the compiler's own functions. The Jass lowerings of these find the
     * functions their lowered body calls (the projection of a key, the fallback of a keyed map) among the intrinsics
     * of the declaring package, by name, whether any code calls them or not, and so does the interpreter on both
     * targets.
     */
    private static boolean isCompilerIntrinsic(ImFunction f) {
        return f.attrTrace() instanceof FunctionDefinition definition && CompilerIntrinsics.isDeclaration(definition);
    }

    /**
     * What a function reaches: what it calls and refers to, and, before the generics, what its type arguments bind.
     * It also takes note of which globals the reachable code reads, and of the assignments to globals which have no
     * effect but the assignment, since those are the only thing a global which nothing reads is needed for.
     */
    private static final class Reachability extends ImFunction.DefaultVisitor {
        private final ImTranslator trans;
        private final boolean followTypeArguments;
        private final Set<ImFunction> reachable;
        private final Set<ImClass> constructedClasses = new ReferenceOpenHashSet<>();
        private final Set<ImMethod> usedMethods = new ReferenceOpenHashSet<>();
        private final ArrayDeque<ImFunction> work = new ArrayDeque<>();
        private final Set<ImVar> globals;
        /** Every variable which reachable code mentions, except as the target of an assignment which has no effect. */
        private final Set<ImVar> mentioned = new ReferenceOpenHashSet<>();
        /** The assignments of effect-free values to a global, which stay only while the global does. */
        private final Map<ImVar, List<ImSet>> assignments = new Reference2ObjectOpenHashMap<>();

        private Reachability(ImTranslator trans, ImProg prog, boolean followTypeArguments) {
            this.trans = trans;
            this.followTypeArguments = followTypeArguments;
            this.reachable = new ReferenceOpenHashSet<>(Math.max(16, prog.getFunctions().size() / 2));
            this.globals = new ReferenceOpenHashSet<>(prog.getGlobals());
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

        /**
         * The global which {@code set} assigns a value to which can be dropped with the assignment, or null: a
         * global of the program which is not preserved, assigned as a whole or by a constant index, a value without
         * effect.
         */
        private @Nullable ImVar globalAssignedWithoutEffect(ImSet set) {
            if (!(set.getParent() instanceof ImStmts) || !hasNoEffect(set.getRight())) {
                return null;
            }
            ImLExpr left = set.getLeft();
            ImVar var;
            if (left instanceof ImVarAccess access) {
                var = access.getVar();
            } else if (left instanceof ImVarArrayAccess access
                && access.getIndexes().stream().allMatch(index -> index instanceof ImIntVal)) {
                var = access.getVar();
            } else {
                return null;
            }
            return globals.contains(var) && !NamePreservation.isPreserved(var) ? var : null;
        }

        /**
         * Whether evaluating {@code e} does nothing but produce its value: a constant, a read of a variable or an
         * operator on such. A call may do anything, and an integer or real division or modulo is the deliberate way to
         * stop a thread when its divisor is zero, so these count only with a divisor which is a nonzero constant.
         */
        private static boolean hasNoEffect(ImExpr e) {
            if (e instanceof ImIntVal || e instanceof ImRealVal || e instanceof ImStringVal || e instanceof ImBoolVal
                || e instanceof ImNull || e instanceof ImVarAccess) {
                return true;
            }
            if (e instanceof ImTupleExpr tuple) {
                return tuple.getExprs().stream().allMatch(Reachability::hasNoEffect);
            }
            if (e instanceof ImOperatorCall call) {
                WurstOperator op = call.getOp();
                if (op == WurstOperator.DIV_INT || op == WurstOperator.MOD_INT || op == WurstOperator.JASS_MOD_INT
                    || op == WurstOperator.DIV_REAL || op == WurstOperator.MOD_REAL) {
                    if (call.getArguments().size() != 2 || !isNonZeroConstant(call.getArguments().get(1))) {
                        return false;
                    }
                }
                return call.getArguments().stream().allMatch(Reachability::hasNoEffect);
            }
            return false;
        }

        private static boolean isNonZeroConstant(ImExpr e) {
            if (e instanceof ImIntVal value) {
                return value.getValI() != 0;
            }
            if (e instanceof ImRealVal value) {
                try {
                    return Double.parseDouble(value.getValR()) != 0.0;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
            return false;
        }

        /**
         * Removes the globals which no reachable code reads, with the assignments of effect-free values to them. A
         * global is needed when reachable code mentions it in any other way (a read, an assignment which may have an
         * effect, a call argument), when it is preserved or pinned, and when the effect-free value assigned to a
         * needed global or the initialiser of a needed global of blizzard.j or common.j reads it. Those are in no
         * function body: the game initialises them, and the interpreters evaluate them when they are first read.
         */
        void removeUnreadGlobals(ImProg prog, Collection<ImVar> pinned) {
            Set<ImVar> needed = new ReferenceOpenHashSet<>();
            ArrayDeque<ImVar> pending = new ArrayDeque<>();
            for (ImVar var : mentioned) {
                markNeeded(var, needed, pending);
            }
            for (ImVar var : pinned) {
                markNeeded(var, needed, pending);
            }
            for (ImVar var : prog.getGlobals()) {
                if (NamePreservation.isPreserved(var)) {
                    markNeeded(var, needed, pending);
                }
            }
            while (!pending.isEmpty()) {
                ImVar var = pending.removeLast();
                for (ImSet assignment : assignments.getOrDefault(var, Collections.emptyList())) {
                    markReadNeeded(assignment.getRight(), needed, pending);
                }
                if (var.getIsBJ()) {
                    for (ImSet initialiser : prog.getGlobalInits().getOrDefault(var, Collections.emptyList())) {
                        markReadNeeded(initialiser.getRight(), needed, pending);
                    }
                }
            }

            Map<ImStmts, Set<ImStmt>> doomed = new IdentityHashMap<>();
            for (Map.Entry<ImVar, List<ImSet>> entry : assignments.entrySet()) {
                if (needed.contains(entry.getKey())) {
                    continue;
                }
                for (ImSet assignment : entry.getValue()) {
                    doomed.computeIfAbsent((ImStmts) assignment.getParent(), k -> new ReferenceOpenHashSet<>()).add(assignment);
                }
            }
            for (Map.Entry<ImStmts, Set<ImStmt>> entry : doomed.entrySet()) {
                Set<ImStmt> assignmentsToRemove = entry.getValue();
                entry.getKey().removeIf(assignmentsToRemove::contains);
            }
            prog.getGlobalInits().keySet().removeIf(var -> globals.contains(var) && !needed.contains(var));
            prog.getGlobals().retainAll(needed);
        }

        private void markNeeded(ImVar var, Set<ImVar> needed, ArrayDeque<ImVar> pending) {
            if (globals.contains(var) && needed.add(var)) {
                pending.add(var);
            }
        }

        private void markReadNeeded(ImExpr e, Set<ImVar> needed, ArrayDeque<ImVar> pending) {
            for (ImVar var : UsedVariables.calculateReadVars(e)) {
                markNeeded(var, needed, pending);
            }
        }

        /** A call of a method dispatches to the implementation of any override of it. */
        private void addMethod(ImMethod method) {
            if (!usedMethods.add(method)) {
                return;
            }
            add(method.getImplementation());
            for (ImMethod sub : method.getSubMethods()) {
                addMethod(sub);
            }
        }

        @Override
        public void visit(ImVarAccess e) {
            super.visit(e);
            mentioned.add(e.getVar());
        }

        @Override
        public void visit(ImVarArrayAccess e) {
            super.visit(e);
            mentioned.add(e.getVar());
        }

        @Override
        public void visit(ImSet set) {
            ImVar target = globalAssignedWithoutEffect(set);
            if (target == null) {
                super.visit(set);
                return;
            }
            // nothing in an effect-free value reaches a function or a type argument, and what it reads is only
            // needed if the global is (see removeUnreadGlobals)
            assignments.computeIfAbsent(target, v -> new ArrayList<>()).add(set);
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
            if (followTypeArguments) {
                followTypeArgument(argument);
            }
        }

        /**
         * What a type argument binds: the implementations its type class binding names, and the construction of each
         * class its type names. The type is a reference, which the visitor does not enter, so the type arguments
         * inside it are followed here: given {@code Loader<State>}, the specialised methods of {@code Loader<State>}
         * construct {@code State} and dispatch through the bindings of its type argument.
         */
        private void followTypeArgument(ImTypeArgument argument) {
            for (Either<ImMethod, ImFunction> implementation : argument.getTypeClassBinding().values()) {
                if (implementation.isLeft()) {
                    addMethod(implementation.getLeft());
                } else {
                    add(implementation.get());
                }
            }
            if (argument.getType() instanceof ImClassType classType) {
                addConstruction(classType.getClassDef());
                for (ImTypeArgument inner : classType.getTypeArguments()) {
                    followTypeArgument(inner);
                }
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
