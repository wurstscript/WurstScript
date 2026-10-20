package de.peeeq.wurstscript.validation.controlflow;

import com.google.common.collect.ImmutableSet;
import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.OtherLink;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.utils.Utils;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

/**
 * Numbers the locals, and the statements and expressions, which the states of one analysed body mention, in the
 * order in which the analysis meets them, so that a state can keep them in arrays.
 */
final class DataflowIds {
    private final Reference2IntOpenHashMap<LocalVarDef> varIds = new Reference2IntOpenHashMap<>();
    private final List<LocalVarDef> vars = new ArrayList<>();
    private final Reference2IntOpenHashMap<Element> elementIds = new Reference2IntOpenHashMap<>();
    private final List<Element> elements = new ArrayList<>();

    DataflowIds() {
        varIds.defaultReturnValue(-1);
        elementIds.defaultReturnValue(-1);
    }

    int varId(LocalVarDef v) {
        int id = varIds.getInt(v);
        if (id < 0) {
            id = vars.size();
            varIds.put(v, id);
            vars.add(v);
        }
        return id;
    }

    /** The id of {@code v}, or -1 when no state mentions it. */
    int existingVarId(NameDef v) {
        return varIds.getInt(v);
    }

    LocalVarDef var(int id) {
        return vars.get(id);
    }

    int elementId(Element e) {
        int id = elementIds.getInt(e);
        if (id < 0) {
            id = elements.size();
            elementIds.put(e, id);
            elements.add(e);
        }
        return id;
    }

    Element element(int id) {
        return elements.get(id);
    }
}

/**
 * The facts about the locals at one point of a body. A state is never changed: an operation returns a new state, or
 * this one when it changes nothing. Each local has the index of its {@link DataflowIds} id; a local with a null entry,
 * or past the end of the array, has no state.
 */
class VarStates {
    private final DataflowIds ids;
    private final @Nullable VState[] states;
    final ImmutableSet<NameDef> destroyedParameters;
    final boolean thisDestroyed;

    private VarStates(DataflowIds ids, @Nullable VState[] states, ImmutableSet<NameDef> destroyedParameters, boolean thisDestroyed) {
        this.ids = ids;
        this.states = states;
        this.destroyedParameters = destroyedParameters;
        this.thisDestroyed = thisDestroyed;
    }

    static VarStates empty(DataflowIds ids) {
        return new VarStates(ids, new VState[0], ImmutableSet.of(), false);
    }

    static VarStates initial(DataflowIds ids, List<LocalVarDef> vars) {
        int[] varIds = new int[vars.size()];
        int length = 0;
        for (int i = 0; i < varIds.length; i++) {
            varIds[i] = ids.varId(vars.get(i));
            length = Math.max(length, varIds[i] + 1);
        }
        @Nullable VState[] states = new VState[length];
        for (int id : varIds) {
            states[id] = VState.initial;
        }
        return new VarStates(ids, states, ImmutableSet.of(), false);
    }

    private @Nullable VState get(int id) {
        return id < states.length ? states[id] : null;
    }

    private @Nullable VState stateOf(NameDef v) {
        int id = ids.existingVarId(v);
        return id < 0 ? null : get(id);
    }

    private VarStates with(int id, VState s) {
        if (get(id) == s) {
            return this;
        }
        @Nullable VState[] changed = Arrays.copyOf(states, Math.max(states.length, id + 1));
        changed[id] = s;
        return new VarStates(ids, changed, destroyedParameters, thisDestroyed);
    }

    VarStates merge(VarStates other) {
        if (this == other) {
            return this;
        }
        int n = Math.max(states.length, other.states.length);
        @Nullable VState[] merged = new VState[n];
        boolean sameAsThis = true;
        boolean sameAsOther = true;
        for (int i = 0; i < n; i++) {
            VState a = get(i);
            VState b = other.get(i);
            VState m = a == null ? b : b == null ? a : a.merge(b);
            merged[i] = m;
            sameAsThis &= m == a;
            sameAsOther &= m == b;
        }
        ImmutableSet<NameDef> destroyed = union(destroyedParameters, other.destroyedParameters);
        boolean mergedThisDestroyed = thisDestroyed || other.thisDestroyed;
        if (sameAsThis && destroyed == destroyedParameters && mergedThisDestroyed == thisDestroyed) {
            return this;
        }
        if (sameAsOther && destroyed == other.destroyedParameters && mergedThisDestroyed == other.thisDestroyed) {
            return other;
        }
        return new VarStates(ids, merged, destroyed, mergedThisDestroyed);
    }

    private static ImmutableSet<NameDef> union(ImmutableSet<NameDef> a, ImmutableSet<NameDef> b) {
        if (a.containsAll(b)) {
            return a;
        }
        if (b.containsAll(a)) {
            return b;
        }
        return ImmutableSet.<NameDef>builder().addAll(a).addAll(b).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarStates other = (VarStates) o;
        if (thisDestroyed != other.thisDestroyed || !destroyedParameters.equals(other.destroyedParameters)) {
            return false;
        }
        int n = Math.max(states.length, other.states.length);
        for (int i = 0; i < n; i++) {
            if (!Objects.equals(get(i), other.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = Objects.hash(destroyedParameters, thisDestroyed);
        for (int i = 0; i < states.length; i++) {
            VState s = states[i];
            if (s != null) {
                h += 31 * i ^ s.hashCode();
            }
        }
        return h;
    }

    public boolean destroyed(NameDef v) {
        VState s = stateOf(v);
        return (s != null && s.mightBeDestroyed) || destroyedParameters.contains(v);
    }

    public boolean uninitialized(NameDef v) {
        VState s = stateOf(v);
        return s != null && s.mightBeUninitialized;
    }

    public VarStates addRead(LocalVarDef v, Element r) {
        return withRead(v, r, null);
    }

    /**
     * Records a read that cannot observe writes made inside {@code excluded}.
     *
     * <p>A for-range start expression is evaluated once before the loop is entered, but the CFG
     * back edge revisits the loop statement, so a plain read would also mark writes made in the
     * loop body - suppressing a genuine never-read warning for them.
     */
    public VarStates addReadOutside(LocalVarDef v, Element r, Element excluded) {
        return withRead(v, r, excluded);
    }

    private VarStates withRead(LocalVarDef v, Element r, @Nullable Element excluded) {
        int id = ids.varId(v);
        VState s = get(id);
        if (s == null) {
            s = VState.initialDefined;
        }
        return with(id, s.addRead(ids, ids.elementId(r), excluded));
    }

    /** The locals which have a state, in the order of their ids. */
    public List<LocalVarDef> locals() {
        List<LocalVarDef> result = new ArrayList<>();
        for (int i = 0; i < states.length; i++) {
            if (states[i] != null) {
                result.add(ids.var(i));
            }
        }
        return result;
    }

    /** The writes to {@code var} which reach this point and which nothing read, in source order. */
    public List<WStatement> getUnreadWrites(LocalVarDef var) {
        VState vState = stateOf(var);
        if (vState == null) {
            return Collections.emptyList();
        }
        List<WStatement> res = new ArrayList<>();
        for (int write : vState.allWrites()) {
            if (!vState.isRead(write)) {
                res.add((WStatement) ids.element(write));
            }
        }
        res.sort(Comparator.comparingInt(w -> w.attrSource().getLeftPos()));
        return res;
    }

    public VarStates addWrite(LocalVarDef var, WStatement s) {
        int id = ids.varId(var);
        VState vState = get(id);
        if (vState == null) {
            vState = VState.initialDefined;
        }
        return with(id, vState.addWrite(ids.elementId(s)));
    }

    public VarStates addDestroy(LocalVarDef var) {
        return with(ids.varId(var), VState.destroyed);
    }

    public VarStates addDestroyParameter(NameDef var) {
        if (destroyedParameters.contains(var)) {
            return this;
        }
        ImmutableSet<NameDef> destroyed = ImmutableSet.<NameDef>builder().addAll(destroyedParameters).add(var).build();
        return new VarStates(ids, states, destroyed, thisDestroyed);
    }

    public VarStates clearDestroyParameter(NameDef var) {
        if (!destroyedParameters.contains(var)) {
            return this;
        }
        ImmutableSet.Builder<NameDef> remaining = ImmutableSet.builder();
        for (NameDef destroyed : destroyedParameters) {
            if (destroyed != var) {
                remaining.add(destroyed);
            }
        }
        return new VarStates(ids, states, remaining.build(), thisDestroyed);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("VarStates [");
        for (int i = 0; i < states.length; i++) {
            VState s = states[i];
            if (s != null) {
                sb.append("\n\t");
                sb.append(ids.var(i).getName()).append(" -> ").append(s.toString(ids));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public VarStates withThisDestroyed(boolean thisDestroyed) {
        if (thisDestroyed == this.thisDestroyed) {
            return this;
        }
        return new VarStates(ids, states, destroyedParameters, thisDestroyed);
    }


}

/**
 * The facts about one local. Writes and reads are {@link DataflowIds} element ids: a write is a statement, a read a
 * statement or an expression. Each array is sorted and free of duplicates, and is never changed.
 */
final class VState {
    private static final int[] NO_WRITES = new int[0];
    private static final long[] NO_READS = new long[0];

    public static final VState initialDefined = new VState(false, false, NO_READS, NO_WRITES, NO_WRITES);
    public static final VState initial = new VState(true, false, NO_READS, NO_WRITES, NO_WRITES);
    public static final VState destroyed = new VState(false, true, NO_READS, NO_WRITES, NO_WRITES);

    final boolean mightBeUninitialized;
    final boolean mightBeDestroyed;
    /** Each read of a write, as {@code write << 32 | read}. */
    private final long[] writesAndReads;
    private final int[] activeWrites;
    private final int[] allWrites;

    private VState(boolean mightBeUninitialized, boolean mightBeDestroyed, long[] writesAndReads, int[] activeWrites, int[] allWrites) {
        this.mightBeUninitialized = mightBeUninitialized;
        this.mightBeDestroyed = mightBeDestroyed;
        this.writesAndReads = writesAndReads;
        this.activeWrites = activeWrites;
        this.allWrites = allWrites;
    }

    int[] allWrites() {
        return allWrites;
    }

    boolean isRead(int write) {
        int i = Arrays.binarySearch(writesAndReads, (long) write << 32);
        if (i < 0) {
            i = -i - 1;
        }
        return i < writesAndReads.length && (int) (writesAndReads[i] >>> 32) == write;
    }

    public VState addWrite(int write) {
        return new VState(false, false, writesAndReads, new int[]{write}, insert(allWrites, write));
    }

    public VState addRead(DataflowIds ids, int read, @Nullable Element excluded) {
        if (activeWrites.length == 0) {
            return this;
        }
        long[] reads = new long[activeWrites.length];
        int n = 0;
        for (int write : activeWrites) {
            if (excluded == null || !isInside(ids.element(write), excluded)) {
                reads[n++] = (long) write << 32 | read;
            }
        }
        long[] merged = union(writesAndReads, n == reads.length ? reads : Arrays.copyOf(reads, n));
        if (merged == writesAndReads) {
            return this;
        }
        return new VState(mightBeUninitialized, mightBeDestroyed, merged, activeWrites, allWrites);
    }

    /** Whether {@code node} lies within the subtree rooted at {@code ancestor}. */
    private static boolean isInside(Element node, Element ancestor) {
        for (Element e = node; e != null; e = e.getParent()) {
            if (e == ancestor) {
                return true;
            }
        }
        return false;
    }

    public VState merge(VState other) {
        if (this == other) {
            return this;
        }
        boolean uninitialized = mightBeUninitialized || other.mightBeUninitialized;
        boolean destroyed = mightBeDestroyed || other.mightBeDestroyed;
        long[] reads = union(writesAndReads, other.writesAndReads);
        int[] active = union(activeWrites, other.activeWrites);
        int[] all = union(allWrites, other.allWrites);
        if (uninitialized == mightBeUninitialized && destroyed == mightBeDestroyed
                && reads == writesAndReads && active == activeWrites && all == allWrites) {
            return this;
        }
        if (uninitialized == other.mightBeUninitialized && destroyed == other.mightBeDestroyed
                && reads == other.writesAndReads && active == other.activeWrites && all == other.allWrites) {
            return other;
        }
        return new VState(uninitialized, destroyed, reads, active, all);
    }

    private static int[] insert(int[] a, int x) {
        int i = Arrays.binarySearch(a, x);
        if (i >= 0) {
            return a;
        }
        i = -i - 1;
        int[] result = new int[a.length + 1];
        System.arraycopy(a, 0, result, 0, i);
        result[i] = x;
        System.arraycopy(a, i, result, i + 1, a.length - i);
        return result;
    }

    /** The union of two sorted arrays; one of them when it contains the other. */
    private static int[] union(int[] a, int[] b) {
        if (a == b || b.length == 0) return a;
        if (a.length == 0) return b;
        int[] result = new int[a.length + b.length];
        int i = 0, j = 0, n = 0;
        while (i < a.length && j < b.length) {
            int x = a[i];
            int y = b[j];
            if (x < y) {
                result[n++] = x;
                i++;
            } else if (y < x) {
                result[n++] = y;
                j++;
            } else {
                result[n++] = x;
                i++;
                j++;
            }
        }
        while (i < a.length) result[n++] = a[i++];
        while (j < b.length) result[n++] = b[j++];
        if (n == a.length) return a;
        if (n == b.length) return b;
        return Arrays.copyOf(result, n);
    }

    /** The union of two sorted arrays; one of them when it contains the other. */
    private static long[] union(long[] a, long[] b) {
        if (a == b || b.length == 0) return a;
        if (a.length == 0) return b;
        long[] result = new long[a.length + b.length];
        int i = 0, j = 0, n = 0;
        while (i < a.length && j < b.length) {
            long x = a[i];
            long y = b[j];
            if (x < y) {
                result[n++] = x;
                i++;
            } else if (y < x) {
                result[n++] = y;
                j++;
            } else {
                result[n++] = x;
                i++;
                j++;
            }
        }
        while (i < a.length) result[n++] = a[i++];
        while (j < b.length) result[n++] = b[j++];
        if (n == a.length) return a;
        if (n == b.length) return b;
        return Arrays.copyOf(result, n);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(activeWrites);
        result = prime * result + Arrays.hashCode(allWrites);
        result = prime * result + (mightBeUninitialized ? 1231 : 1237);
        result = prime * result + (mightBeDestroyed ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(writesAndReads);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VState other = (VState) obj;
        return mightBeUninitialized == other.mightBeUninitialized
                && mightBeDestroyed == other.mightBeDestroyed
                && Arrays.equals(activeWrites, other.activeWrites)
                && Arrays.equals(allWrites, other.allWrites)
                && Arrays.equals(writesAndReads, other.writesAndReads);
    }

    String toString(DataflowIds ids) {
        StringBuilder sb = new StringBuilder("VState [ " + this.mightBeUninitialized + ", " + this.mightBeDestroyed + ", ");
        for (long e : writesAndReads) {
            sb.append("\n\t\t");
            sb.append(ids.element((int) (e >>> 32)).attrSource().getLine()).append(" -> ").append(ids.element((int) e).attrSource().getLine());
        }
        sb.append("]");
        return sb.toString();
    }


}


public class DataflowAnomalyAnalysis extends ForwardMethod<VarStates, AstElementWithBody> {


    private final boolean jassCode;
    private final DataflowIds ids = new DataflowIds();
    private final VarStates startValue = VarStates.empty(ids);

    public DataflowAnomalyAnalysis(boolean jassCode) {
        this.jassCode = jassCode;
    }

    @Override
    VarStates calculate(WStatement s, VarStates incoming) {
        if (s instanceof StartFunctionStatement) {
            // initially all vars are uninitialized
            List<LocalVarDef> r = new ArrayList<>();
            collectLocalVars(r, getFuncDef());
            return VarStates.initial(ids, r);
        }


        if (s instanceof CompoundStatement) {
            // A loop that declares its own variable evaluates its whole header exactly once before
            // the loop is entered - StmtTranslation assigns the start expression and hoists "to",
            // "step" and "in" into temporaries ahead of the ImLoop. A while condition, by contrast,
            // is re-evaluated per iteration, so only this family gets the once-only treatment.
            boolean headerEvaluatedOnce = s instanceof LoopStatementWithVarDef;
            // for a compound statement check only the expressions in the statement
            for (int i = 0; i < s.size(); i++) {
                if (s.get(i) instanceof Expr) {
                    Expr expr = (Expr) s.get(i);
                    incoming = headerEvaluatedOnce
                        ? handleLoopHeaderExpr(incoming, expr, s)
                        : handleExprInCompound(incoming, expr);
                }
            }
            if (s instanceof SwitchStmt swi) {
                // switch statement must be handled separately, because expressions are not direct children:
                for (SwitchCase switchCase : swi.getCases()) {
                    for (Expr switchCaseExpr : switchCase.getExpressions()) {
                        incoming = handleExprInCompound(incoming, switchCaseExpr);
                    }
                }
            } else if (s instanceof LoopStatementWithVarDef loopStatementWithVarDef) {
                // Same reason: for "for i = a downto 0" the start expression belongs to the loop
                // variable's LocalVarDef, not to the loop statement, so the loop above never sees
                // it and a local read only appearing there looked like a dead assignment.
                // StmtForFrom binds its loop variable from the "in" expression and has no initial
                // expression of its own, which the instanceof guard covers.
                LocalVarDef loopVar = loopStatementWithVarDef.getLoopVar();
                if (loopVar.getInitialExpr() instanceof Expr) {
                    incoming = handleLoopHeaderExpr(incoming, (Expr) loopVar.getInitialExpr(), s);
                }
            }
        } else {
            checkIfVarsInitialized(s, incoming);
            for (NameDef v : s.attrReadVariables()) {
                if (isLocalVarDef(v)) {
                    incoming = incoming.addRead((LocalVarDef) v, s);
                }
            }
            if (incoming.thisDestroyed) {
                checkNoAccessToThis(s);
            }
        }

        if (s instanceof ExprDestroy destr) {
            if (destr.getDestroyedObj() instanceof ExprVarAccess) {
                ExprVarAccess destroyed = (ExprVarAccess) destr.getDestroyedObj();
                NameDef destroyedVar = destroyed.attrNameDef();
                if (isLocalVarDef(destroyedVar)) {
                    return incoming.addDestroy((LocalVarDef) destroyedVar);
                }
                if (destroyedVar instanceof WParameter || destroyedVar instanceof WShortParameter) {
                    return incoming.addDestroyParameter(destroyedVar);
                }
            } else if (destr.getDestroyedObj() instanceof ExprThis) {
                return incoming.withThisDestroyed(true);
            }
        }

        NameDef n = getInitializedVar(s);
        if (n != null) {
            if (isLocalVarDef(n)) {
                LocalVarDef lv = (LocalVarDef) n;
                return incoming.addWrite(lv, s);
            }
            if (n instanceof WParameter || n instanceof WShortParameter) {
                return incoming.clearDestroyParameter(n);
            }
        }
        return incoming;
    }

    /** checks that no expression in s uses 'this'; adds an error and returns true if it finds someting*/
    private boolean checkNoAccessToThis(Element s) {
        for (int i = 0; i < s.size(); i++) {
            if (checkNoAccessToThis(s.get(i))) {
                return true;
            }
        }
        if (s instanceof ExprThis) {
            reportError(s, "Cannot access 'this' because it might already have been destroyed.");
            return true;
        } if (s instanceof FunctionCall functionCall) {
            if (functionCall.attrImplicitParameter() instanceof ExprThis) {
                reportError(s, "Cannot access 'this' because it might already have been destroyed.");
                return true;
            }
        } else if (s instanceof NameRef nameRef) {
            if (nameRef.attrImplicitParameter() instanceof ExprThis) {
                reportError(s, "Cannot access 'this' because it might already have been destroyed.");
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a loop header expression - the start value, "to", "step" or "in" - each of which
     * runs exactly once before the loop is entered.
     *
     * <p>The CFG has a back edge to the loop statement, so the fixpoint evaluates this again on
     * later iterations. Reads are therefore recorded only against writes outside the loop: a write
     * in the loop body happens after the header has already been evaluated and cannot be observed
     * by it, so counting it as read would hide a genuine dead assignment.
     */
    private VarStates handleLoopHeaderExpr(VarStates incoming, Expr headerExpr, WStatement loop) {
        checkIfVarsInitialized(headerExpr, incoming);
        for (NameDef v : headerExpr.attrReadVariables()) {
            if (isLocalVarDef(v)) {
                incoming = incoming.addReadOutside((LocalVarDef) v, headerExpr, loop);
            }
        }
        return incoming;
    }

    private VarStates handleExprInCompound(VarStates incoming, Expr expr) {
        checkIfVarsInitialized(expr, incoming);
        for (NameDef v : expr.attrReadVariables()) {
            if (isLocalVarDef(v)) {
                incoming = incoming.addRead((LocalVarDef) v, expr);
            }
        }
        return incoming;
    }

    private @Nullable NameDef getInitializedVar(WStatement s) {
        NameDef n = null;
        if (s instanceof StmtSet s2) {
            NameLink link = s2.getUpdatedExpr().attrNameLink();
            if (link != null && !(link instanceof OtherLink)) {
                n = link.getDef();
            }

        } else if (isLocalVarDef(s)) {
            LocalVarDef l = (LocalVarDef) s;
            if (l.getInitialExpr() instanceof Expr) {
                n = l;
            }
        } else if (s instanceof LoopStatementWithVarDef s2) {
            n = s2.getLoopVar();
        }
        return n;
    }

    private void collectLocalVars(List<LocalVarDef> out, Element root) {
        ArrayDeque<Element> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Element e = stack.pop();

            if (isLocalVarDef(e)) {
                out.add((LocalVarDef) e);
            }

            // visit children left→right: push in reverse
            for (int i = e.size() - 1; i >= 0; i--) {
                Element c = e.get(i);
                if (!(c instanceof ExprClosure) && !(c instanceof ExprStatementsBlock)) {
                    stack.push(c);
                }
            }
        }
    }

    /**
     * checks if this is a local variable and not an array
     */
    private boolean isLocalVarDef(Element e) {
        if (e instanceof LocalVarDef l) {
            return !l.attrTyp().isArray();
        }
        return false;
    }

    private void checkIfVarsInitialized(HasReadVariables s, VarStates incoming) {
        ImmutableList<NameDef> readVars = s.attrReadVariables();
        for (NameDef v : readVars) {
            if (v.attrTyp() instanceof WurstTypeArray) {
                continue;
            }

            if (incoming.uninitialized(v) || incoming.destroyed(v)) {
                Element readingExpr = findRead(s, v);
                if (readingExpr == null) {
                    readingExpr = s;
                }
                String error = "Variable " + v.getName();
                if (incoming.destroyed(v)) {
                    error += " may have been destroyed already";
                } else {
                    error += " may not have been initialized";
                }
                reportError(readingExpr, error);
            }
        }
    }

    private void reportError(Element location, String error) {
        if (jassCode) {
            location.addWarning(error);
        } else {
            location.addError(error);
        }
    }

    private @Nullable HasReadVariables findRead(Element e, NameDef v) {
        HasReadVariables result = null;
        if (e instanceof HasReadVariables r) {
            if (!r.attrReadVariables().contains(v)) {
                return null;
            }
            result = r;
        }
        for (int i = 0; i < e.size(); i++) {
            HasReadVariables r = findRead(e.get(i), v);
            if (r != null) {
                if (isLeftOfStmtSet(r)) {
                    continue;
                }

                return r;
            }
        }
        return result;
    }

    private boolean isLeftOfStmtSet(HasReadVariables r) {
        Element parent = r.getParent();
        if (parent instanceof StmtSet stmtSet) {
            return stmtSet.getUpdatedExpr() == r;
        }
        return false;
    }

    @Override
    VarStates merge(Collection<VarStates> values) {
        Iterator<VarStates> it = values.iterator();
        VarStates r = it.next();
        while (it.hasNext()) {
            r = r.merge(it.next());
        }
        return r;
    }

    @Override
    String print(VarStates t) {
        if (t == null) {
            return "null";
        }
        return t.toString();
    }

    @Override
    void checkFinal(VarStates fin) {
        for (LocalVarDef var : fin.locals()) {
            if (var.getName().startsWith("_")) {
                // ignore warning, if name starts with "_"
                continue;
            }
            for (WStatement ur : fin.getUnreadWrites(var)) {
                if (ur instanceof LoopStatement) {
                    // no warnings for loop variables
                    continue;
                }

                Element errorPos = ur;
                if (ur instanceof StmtSet stmtSet) {
                    errorPos = stmtSet.getUpdatedExpr();
                }
                @Nullable ExprClosure exprClosure = errorPos.attrNearestExprClosure();
                @Nullable ExprClosure exprClosure1 = var.attrNearestExprClosure();
                if (exprClosure != null && exprClosure != exprClosure1) {
                    errorPos.addWarning("This assignment to the closure-captured variable " + Utils.printElement(var)
                        + " does not propagate outside the closure because closures capture locals by value. "
                        + "If you want to update the outer value, use reference(" + var.getName() + ") deliberately.");
                } else {
                    errorPos.addWarning("The assignment to " + Utils.printElement(var) + " is never read.");
                }
            }
        }
    }

    @Override
    public VarStates startValue() {
        return startValue;
    }


}
