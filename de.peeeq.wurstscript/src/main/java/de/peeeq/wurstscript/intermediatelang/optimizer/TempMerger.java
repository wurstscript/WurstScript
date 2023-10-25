package de.peeeq.wurstscript.intermediatelang.optimizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.OptimizerPass;
import de.peeeq.wurstscript.translation.imtranslation.AssertProperty;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.MapWithIndexes;
import de.peeeq.wurstscript.utils.MapWithIndexes.Index;
import de.peeeq.wurstscript.utils.MapWithIndexes.PredIndex;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class TempMerger implements OptimizerPass {
    private int totalMerged = 0;


    @Override
    public String getName() {
        return "Temp variables merged";
    }

    /**
     * @return The amount of merged temp variables
     */
    @Override
    public int optimize(ImTranslator trans) {
        ImProg prog = trans.getImProg();
        totalMerged = 0;
        trans.assertProperties(AssertProperty.FLAT, AssertProperty.NOTUPLES);
        prog.clearAttributes();
        for (ImFunction f : ImHelper.calculateFunctionsOfProg(prog)) {
            optimizeFunc(f);
        }
        // flatten the program because we introduced null-statements
        prog.flatten(trans);
        return totalMerged;
    }

    void optimizeFunc(ImFunction f) {
        optimizeStatements(f.getBody());
    }

    private void optimizeStatements(ImStmts stmts) {
        Knowledge kn = new Knowledge();

        Replacement replacement = null;
        do { // repeat while there are changes found
            kn.clear();
            // this terminates, because each replacement eliminates one set-statement
            // FIXME this is no longer true, because assignments which are used more than once are not removed
            for (ImStmt s : stmts) {
                if (s instanceof ImSet) {
                    ImSet imSet = (ImSet) s;
                    if (imSet.getRight() instanceof ImVarAccess
                            && imSet.getLeft() instanceof ImVarAccess) {
                        ImVarAccess right = (ImVarAccess) imSet.getRight();
                        ImVarAccess left = (ImVarAccess) imSet.getLeft();
                        if (left.getVar() == right.getVar()) {
                            // statement has the form 'x = x' so remove it
                            totalMerged++;
                            imSet.replaceBy(ImHelper.nullExpr());
                            continue;
                        }
                    }
                }

                replacement = processStatement(s, kn);
                if (replacement != null) {
                    // do the replacement
                    totalMerged++;
                    replacement.apply();
                    break;
                }
            }
        } while (replacement != null);

        // process nested statements:
        for (ImStmt s : stmts) {
            if (s instanceof ImIf) {
                ImIf imIf = (ImIf) s;
                optimizeStatements(imIf.getThenBlock());
                optimizeStatements(imIf.getElseBlock());
            } else if (s instanceof ImLoop) {
                ImLoop imLoop = (ImLoop) s;
                optimizeStatements(imLoop.getBody());
            } else if (s instanceof ImVarargLoop) {
                ImVarargLoop imVarargLoop = (ImVarargLoop) s;
                optimizeStatements(imVarargLoop.getBody());
            }
        }
    }

    private @Nullable Replacement processStatement(ImStmt s, Knowledge kn) {
        Replacement rep = getPossibleReplacement(s, kn);
        if (rep != null) {
            return rep;
        }
        if (containsFuncCall(s)) {
            kn.invalidateGlobals();
        }
        if (readsGlobal(s)) {
            kn.invalidateMutatingExpressions();
        }
        if (s instanceof ImSet) {
            ImSet imSet = (ImSet) s;
            if (imSet.getLeft() instanceof ImVarAccess) {
                ImVarAccess va = (ImVarAccess) imSet.getLeft();
                // update the knowledge with the new set statement
                kn.update(va.getVar(), imSet);
            } else if (imSet.getLeft() instanceof ImVarArrayAccess) {
                ImVarArrayAccess va = (ImVarArrayAccess) imSet.getLeft();
                kn.invalidateVar(va.getVar());
            } else if (imSet.getLeft() instanceof ImMemberAccess) {
                ImMemberAccess ma = (ImMemberAccess) imSet.getLeft();
                kn.invalidateVar(ma.getVar());
            } else if (imSet.getLeft() instanceof ImTupleSelection) {
                kn.invalidateVar(TypesHelper.getTupleVar(((ImTupleSelection) imSet.getLeft())));
            }
        } else if (s instanceof ImExitwhen || s instanceof ImIf || s instanceof ImLoop || s instanceof ImVarargLoop) {
            kn.clear();
            // TODO this could be more precise for local variables,
            // but for now we just forget everything if we see a loop or if statement
        }
        return null;
    }

    private @Nullable Replacement getPossibleReplacement(Element elem, Knowledge kn) {
        if (kn.isEmpty()) {
            return null;
        }
        if (elem instanceof ImVarAccess) {
            ImVarAccess va = (ImVarAccess) elem;
            if (!va.isUsedAsLValue()) {
                return kn.getReplacementIfPossible(va);
            }
        } else if (elem instanceof ImLoop) {
            return null;
        } else if (elem instanceof ImVarargLoop) {
            return null;
        } else if (elem instanceof ImIf) {
            ImIf imIf = (ImIf) elem;
            return getPossibleReplacement(imIf.getCondition(), kn);
        } else if (elem instanceof ImOperatorCall) {
            ImOperatorCall opCall = (ImOperatorCall) elem;
            if (opCall.getOp().isLazy()) {
                // for lazy operators (and, or) we only search the left expression for possible replacements
                return getPossibleReplacement(opCall.getArguments().get(0), kn);
            }
        }
        // process children
        for (int i = 0; i < elem.size(); i++) {
            Replacement r = getPossibleReplacement(elem.get(i), kn);
            if (r != null) {
                return r;
            }
        }
        if (elem instanceof ImFunctionCall) {
            // function call invalidates globals
            kn.invalidateGlobals();
        } else if (elem instanceof ImMethodCall) {
            // method call invalidates globals
            kn.invalidateGlobals();
        } else if (elem instanceof ImVarRead) { // this already covers member access as well
            ImVarRead va = (ImVarRead) elem;
            if (va.getVar().isGlobal()) {
                // in case we read a global variable
                kn.invalidateMutatingExpressions();
            }
        }
        return null;
    }


    private boolean containsFuncCall(Element elem) {
        if (elem instanceof ImFunctionCall || elem instanceof ImMethodCall) {
            return true;
        }
        // process children
        boolean r = false;
        for (int i = 0; i < elem.size(); i++) {
            r = containsFuncCall(elem.get(i));
            if (r) {
                return true;
            }
        }
        return false;
    }


    private boolean readsVar(Element elem, ImVar left) {
        if (elem instanceof ImVarRead) {
            ImVarRead va = (ImVarRead) elem;
            if (va.getVar() == left) {
                return true;
            }
        }
        if (elem instanceof ImMemberAccess) {
            if(((ImMemberAccess) elem).getVar() == left) {
                return true;
            }
        }
        // process children
        for (int i = 0; i < elem.size(); i++) {
            if (readsVar(elem.get(i), left)) {
                return true;
            }
        }
        return false;
    }


    private boolean readsGlobal(Element elem) {
        if (elem instanceof ImVarRead) {
            ImVarRead va = (ImVarRead) elem;
            if (va.getVar().isGlobal()) {
                return true;
            }
        }
        if (elem instanceof ImMemberAccess) {
            return true;
        }
        // process children
        for (int i = 0; i < elem.size(); i++) {
            if (readsGlobal(elem.get(i))) {
                return true;
            }
        }

        return false;
    }

    class Replacement {
        public final ImSet set;
        public final ImVarAccess read;

        public Replacement(ImSet set, ImVarAccess read) {
            Preconditions.checkArgument(set.getLeft() instanceof ImVarAccess);
            this.set = set;
            this.read = read;
        }

        @Override
        public String toString() {
            return "replace " + read + ", using " + set;
        }

        public void apply() {
            ImExpr e = set.getRight();
            if (getAssignedVar().attrReads().size() <= 1) {
                // make sure that an impure expression is only evaluated once
                // by removing the assignment
                set.replaceBy(ImHelper.nullExpr());

                // remove variables which are no longer read
                for (ImVarRead r : readVariables(set)) {
                    r.getVar().attrReads().remove(r);
                }
            }

            ImExpr newE = (ImExpr) e.copy();
            read.replaceBy(newE);
            // update attrReads:
            getAssignedVar().attrReads().remove(read);

            // for all the variables in e: add to read
            for (ImVarRead r : readVariables(newE)) {
                r.getVar().attrReads().add(r);
            }

        }

        private ImVar getAssignedVar() {
            return ((ImVarAccess) set.getLeft()).getVar();
        }

    }

    private Collection<ImVarRead> readVariables(Element e) {
        Collection<ImVarRead> result = Lists.newArrayList();
        collectReadVariables(result, e);
        return result;
    }


    private void collectReadVariables(Collection<ImVarRead> result, Element e) {
        if (e instanceof ImVarRead) {
            result.add((ImVarRead) e);
        }
        for (int i = 0; i < e.size(); i++) {
            collectReadVariables(result, e.get(i));
        }
    }

    class Knowledge {

        class VarKnowledge {
            private final ImSet imSet;
            private final Set<ImVar> dependsOn;
            private final boolean dependsOnGlobals;
            private final boolean isMutating;

            public VarKnowledge(ImSet imSet, Set<ImVar> dependsOn, boolean dependsOnGlobals, boolean isMutating) {
                this.imSet = imSet;
                this.dependsOn = dependsOn;
                this.dependsOnGlobals = dependsOnGlobals;
                this.isMutating = isMutating;
            }

            public VarKnowledge(ImSet set) {
                this.imSet = set;
                this.dependsOn = new LinkedHashSet<>();
                collectReadVariables(this.dependsOn, set.getRight());
                boolean containsFuncCall = containsFuncCall(set);
                this.dependsOnGlobals = containsFuncCall || readsGlobal(set.getRight());
                this.isMutating = containsFuncCall;
            }

            private void collectReadVariables(Collection<ImVar> result, Element e) {
                if (e instanceof ImVarRead) {
                    result.add(((ImVarRead) e).getVar());
                }
                if (e instanceof ImMemberAccess) {
                    result.add(((ImMemberAccess) e).getVar());
                }
                for (int i = 0; i < e.size(); i++) {
                    collectReadVariables(result, e.get(i));
                }
            }
        }


        private final MapWithIndexes<ImVar, VarKnowledge> currentValues = new MapWithIndexes<>();
        // map from a variable to the keys in currentValues that read it
        private final Index<ImVar, ImVar> readBy = currentValues.createMultiIndex((v) -> v.dependsOn);
        // set of keys in currentValues that depend on global state
        private final PredIndex<ImVar> globalState = currentValues.createPredicateIndex(v -> v.dependsOnGlobals);
        // set of keys in currentValues that can change global state
        private final PredIndex<ImVar> mutating = currentValues.createPredicateIndex(v -> v.isMutating);

        public void invalidateGlobals() {
            // invalidate all knowledge which might be based on global state
            // i.e. using a global var or calling a function
            currentValues.removeAll(globalState.lookup());
        }

        public void invalidateMutatingExpressions() {
            // invalidate all knowledge which can change global state
            // i.e. calling a function
            currentValues.removeAll(mutating.lookup());
        }

        public void clear() {
            currentValues.clear();
        }

        public @Nullable Replacement getReplacementIfPossible(ImVarAccess va) {
            for (Entry<ImVar, VarKnowledge> e : currentValues.entrySet()) {
                if (e.getKey() == va.getVar()) {
                    return new Replacement(e.getValue().imSet, va);
                }
            }
            return null;
        }

        public boolean isEmpty() {
            return currentValues.isEmpty();
        }

        public void update(ImVar left, ImSet set) {
            invalidateVar(left);

            if (isMergable(left, set.getRight())) {
                // only store local vars which are read exactly once
                VarKnowledge k = new VarKnowledge(set);
                currentValues.put(left, k);
            }
        }

        private boolean isMergable(ImVar left, ImExpr e) {
            if (left.isGlobal()) {
                // never merge globals
                return false;
            }
            if (e instanceof ImVarAccess) {
                ImVarAccess va = (ImVarAccess) e;
                if (va.getVar() == left) {
                    // this is a stupid assignment, ignore it
                    return false;
                }
            }
            if (left.attrReads().size() == 1) {
                // variable read exactly once can be replaced
                return true;
            }
            if (isSimplePureExpr(e)) {
                // simple and pure expressions can always be merged
                return true;
            }
            return false;
        }

        /**
         * invalidates all expression depending on 'left'
         */
        private void invalidateVar(ImVar left) {
            currentValues.remove(left);
            if (left.isGlobal()) {
                invalidateGlobals();
            } else {
                currentValues.removeAll(readBy.lookup(left));
            }
        }

        @Override
        public String toString() {
            ArrayList<ImVar> keys = Lists.newArrayList(currentValues.keySet());
            keys.sort(Utils.<ImVar>compareByNameIm());
            StringBuilder sb = new StringBuilder();
            for (ImVar v : keys) {
                ImSet s = currentValues.get(v).imSet;
                sb.append(v.getName()).append(" -> ").append(s).append(", ");
            }
            return sb.toString();
        }

    }

    private boolean isSimplePureExpr(ImExpr e) {
        if (e instanceof ImConst) {
            // constants are ok
            return true;
        } else if (e instanceof ImVarAccess) {
            // local variables are ok
            ImVarAccess va = (ImVarAccess) e;
            return !va.getVar().isGlobal();
        }
        return false;
    }

}
