package de.peeeq.wurstscript.validation.controlflow;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;
import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.types.WurstTypeArray;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.Map.Entry;

//w(11)->{r(17), r(19)} & w(19)->{r(17), r(19)} & w(22)
class VarStates {
    final ImmutableMap<LocalVarDef, VState> states;
    final boolean thisDestroyed;

    public VarStates(ImmutableMap<LocalVarDef, VState> states, boolean thisDestroyed) {
        this.states = states;
        this.thisDestroyed = thisDestroyed;
    }

    VarStates merge(VarStates other) {
        ImmutableMap<LocalVarDef, VState> merged = Utils.mergeMaps(states, other.states, VState::merge);
        return new VarStates(merged, thisDestroyed || other.thisDestroyed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarStates varStates = (VarStates) o;
        return thisDestroyed == varStates.thisDestroyed &&
                Objects.equals(states, varStates.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(states, thisDestroyed);
    }

    public static VarStates initial(Set<LocalVarDef> r) {
        ImmutableMap.Builder<LocalVarDef, VState> s = ImmutableMap.builder();
        for (LocalVarDef v : r) {
            s.put(v, VState.initial);
        }
        return new VarStates(s.build(), false);
    }

    public boolean destroyed(NameDef v) {
        VState s = states.get(v);
        return s != null && s.mightBeDestroyed;
    }

    public boolean uninitialized(NameDef v) {
        VState s = states.get(v);
        return s != null && s.mightBeUninitialized;
    }

    public VarStates addRead(LocalVarDef v, Element r) {
        VState s = getVarState(v);
        if (s == null) {
            s = VState.initialDefined;
        }
        s = s.addRead(r);
        Builder<LocalVarDef, VState> builder = ImmutableMap.builder();
        for (Entry<LocalVarDef, VState> e : states.entrySet()) {
            if (e.getKey() != v) {
                builder.put(e);
            }
        }
        ImmutableMap<LocalVarDef, VState> rs = builder
                .put(v, s)
                .build();
        return new VarStates(rs, thisDestroyed);
    }

    public ImmutableSet<WStatement> getUnreadWrites(NameDef var) {
        ImmutableSet.Builder<WStatement> res = ImmutableSet.builder();
        VState vState = states.get(var);
        if (vState == null) return ImmutableSet.of();
        for (WStatement wr : vState.allWrites) {
            if (vState.writesAndReads.get(wr).isEmpty()) {
                res.add(wr);
            }
        }

        return res.build();
    }

    public VarStates addWrite(LocalVarDef var, WStatement s) {
        ImmutableMap.Builder<LocalVarDef, VState> res = ImmutableMap.builder();
        for (Entry<LocalVarDef, VState> e : states.entrySet()) {
            if (e.getKey() != var) {
                res.put(e);
            }
        }
        VState vState = getVarState(var);
        if (vState == null) {
            vState = VState.initialDefined;
        }
        vState = vState.addWrite(s);
        res.put(var, vState);
        return new VarStates(res.build(), thisDestroyed);
    }

    public VarStates addDestroy(LocalVarDef var) {
        ImmutableMap.Builder<LocalVarDef, VState> res = ImmutableMap.builder();
        for (Entry<LocalVarDef, VState> e : states.entrySet()) {
            if (e.getKey() != var) {
                res.put(e);
            }
        }
        res.put(var, VState.destroyed);
        return new VarStates(res.build(), thisDestroyed);
    }



    private @Nullable VState getVarState(LocalVarDef var) {
        return states.get(var);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("VarStates [");
        for (Entry<LocalVarDef, VState> e : this.states.entrySet()) {
            sb.append("\n\t");
            sb.append(e.getKey().getName()).append(" -> ").append(e.getValue());

        }
        sb.append("]");
        return sb.toString();
    }

    public boolean isThisDestroyed() {
        return thisDestroyed;
    }

    public VarStates withThisDestroyed(boolean thisDestroyed) {
        return new VarStates(states, thisDestroyed);
    }


}

class VState {

    public static final VState initialDefined = new VState(false, false, ImmutableSetMultimap.of()
            , ImmutableSet.of(), ImmutableSet.of());
    public static final VState initial = new VState(true, false, ImmutableSetMultimap.of()
            , ImmutableSet.of(), ImmutableSet.of());
    public static final VState destroyed = new VState(false, true, ImmutableSetMultimap.of()
            , ImmutableSet.of(), ImmutableSet.of());

    final boolean mightBeUninitialized;
    final boolean mightBeDestroyed;
    final ImmutableSetMultimap<WStatement, Element> writesAndReads;
    final ImmutableSet<WStatement> activeWrites;
    final ImmutableSet<WStatement> allWrites;

    public VState(boolean mightBeUninitialized, boolean mightBeDestroyed, ImmutableSetMultimap<WStatement, Element> writesAndReads, ImmutableSet<WStatement> activeWrites, ImmutableSet<WStatement> allWrites) {
        this.mightBeUninitialized = mightBeUninitialized;
        this.mightBeDestroyed = mightBeDestroyed;
        this.writesAndReads = writesAndReads;
        this.activeWrites = activeWrites;
        this.allWrites = allWrites;
    }

    public VState addWrite(WStatement s) {
        ImmutableSet.Builder<WStatement> wr = ImmutableSet.builder();
        wr.addAll(allWrites);
        wr.add(s);
        return new VState(false, false, writesAndReads, ImmutableSet.of(s), wr.build());
    }

    public VState addRead(Element r) {
        ImmutableSetMultimap.Builder<WStatement, Element> builder = ImmutableSetMultimap.builder();
        builder.putAll(writesAndReads);
        for (WStatement s : this.activeWrites) {
            builder.put(s, r);
        }
        return new VState(mightBeUninitialized, mightBeDestroyed, builder.build(), activeWrites, allWrites);
    }

    public VState merge(VState other) {
        return new VState(mightBeUninitialized || other.mightBeUninitialized,
                mightBeDestroyed || other.mightBeDestroyed,
                Utils.mergeMultiMaps(writesAndReads, other.writesAndReads),
                Utils.mergeSets(activeWrites, other.activeWrites),
                Utils.mergeSets(allWrites, other.allWrites));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + activeWrites.hashCode();
        result = prime * result + allWrites.hashCode();
        result = prime * result + (mightBeUninitialized ? 1231 : 1237);
        result = prime * result + (mightBeDestroyed ? 1231 : 1237);
        result = prime * result + writesAndReads.hashCode();
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
        if (!activeWrites.equals(other.activeWrites))
            return false;
        if (!allWrites.equals(other.allWrites))
            return false;
        if (mightBeUninitialized != other.mightBeUninitialized)
            return false;
        if (mightBeDestroyed != other.mightBeDestroyed)
            return false;
        if (!writesAndReads.equals(other.writesAndReads))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("VState [ " + this.mightBeUninitialized + ", " + this.mightBeDestroyed + ", ");
        for (Entry<WStatement, Element> e : this.writesAndReads.entries()) {
            sb.append("\n\t\t");
            sb.append(e.getKey().attrSource().getLine()).append(" -> ").append(e.getValue().attrSource().getLine());
        }
        sb.append("]");
        return sb.toString();
    }


}


public class DataflowAnomalyAnalysis extends ForwardMethod<VarStates, AstElementWithBody> {


    private final boolean jassCode;

    public DataflowAnomalyAnalysis(boolean jassCode) {
        this.jassCode = jassCode;
    }

    @Override
    VarStates calculate(WStatement s, VarStates incoming) {
        if (s instanceof StartFunctionStatement) {
            // initially all vars are uninitialized
            final Set<LocalVarDef> r = Sets.newHashSet();
            collectLocalVars(r, getFuncDef());
            return VarStates.initial(r);
        }


        if (s instanceof CompoundStatement) {
            // for a compound statement check only the expressions in the statement
            for (int i = 0; i < s.size(); i++) {
                if (s.get(i) instanceof Expr) {
                    Expr expr = (Expr) s.get(i);
                    incoming = handleExprInCompound(incoming, expr);
                }
            }
            if (s instanceof SwitchStmt) {
                SwitchStmt swi = (SwitchStmt) s;
                // switch statement must be handled separately, because expressions are not direct children:
                for (SwitchCase switchCase : swi.getCases()) {
                    for (Expr switchCaseExpr : switchCase.getExpressions()) {
                        incoming = handleExprInCompound(incoming, switchCaseExpr);
                    }
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

        if (s instanceof ExprDestroy) {
            ExprDestroy destr = (ExprDestroy) s;
            if (destr.getDestroyedObj() instanceof ExprVarAccess) {
                ExprVarAccess destroyed = (ExprVarAccess) destr.getDestroyedObj();
                NameDef destroyedVar = destroyed.attrNameDef();
                if (isLocalVarDef(destroyedVar)) {
                    return incoming.addDestroy((LocalVarDef) destroyedVar);
                }
            } else if (destr.getDestroyedObj() instanceof ExprThis) {
                return incoming.withThisDestroyed(true);
            }
        }

        NameDef n = getInitializedVar(s);
        if (n != null) {
//			ImmutableSet<WStatement> unreadWrites = incoming.getUnreadWrites(n);
//			for (WStatement wr : unreadWrites) {
//				Element parent = s.getParent();
//				if (isNested(wr, parent)) {				
////					s.addError("Assigning to " + Utils.printElement(n) + " overrides previous assignment in line " + wr.attrSource().getLine());
//				}
//			} 
            if (isLocalVarDef(n)) {
                LocalVarDef lv = (LocalVarDef) n;
                return incoming.addWrite(lv, s);
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
        } if (s instanceof FunctionCall) {
            if (((FunctionCall) s).attrImplicitParameter() instanceof ExprThis) {
                reportError(s, "Cannot access 'this' because it might already have been destroyed.");
                return true;
            }
        } else if (s instanceof NameRef) {
            if (((NameRef) s).attrImplicitParameter() instanceof ExprThis) {
                reportError(s, "Cannot access 'this' because it might already have been destroyed.");
                return true;
            }
        }
        return false;
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
        if (s instanceof StmtSet) {
            StmtSet s2 = (StmtSet) s;
            NameLink link = s2.getUpdatedExpr().attrNameLink();
            if (link != null) {
                n = link.getDef();
            }

        } else if (isLocalVarDef(s)) {
            LocalVarDef l = (LocalVarDef) s;
            if (l.getInitialExpr() instanceof Expr) {
                n = l;
            }
        } else if (s instanceof LoopStatementWithVarDef) {
            LoopStatementWithVarDef s2 = (LoopStatementWithVarDef) s;
            n = s2.getLoopVar();
        }
        return n;
    }

    private void collectLocalVars(Set<LocalVarDef> r, Element e) {
        if (isLocalVarDef(e)) {
            r.add((LocalVarDef) e);
        }

        for (int i = 0; i < e.size(); i++) {
            Element c = e.get(i);
            if (!(c instanceof ExprClosure) && !(c instanceof ExprStatementsBlock)) {
                collectLocalVars(r, c);
            }
        }
    }

    /**
     * checks if this is a local variable and not an array
     */
    private boolean isLocalVarDef(Element e) {
        if (e instanceof LocalVarDef) {
            LocalVarDef l = (LocalVarDef) e;
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
        if (e instanceof HasReadVariables) {
            HasReadVariables r = (HasReadVariables) e;
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
        if (parent instanceof StmtSet) {
            StmtSet stmtSet = (StmtSet) parent;
            if (stmtSet.getUpdatedExpr() == r) {
                return true;
            }
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
        for (LocalVarDef var : fin.states.keySet()) {
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
                if (ur instanceof StmtSet) {
                    errorPos = ((StmtSet) ur).getUpdatedExpr();
                }
                errorPos.addWarning("The assignment to " + Utils.printElement(var) + " is never read.");
            }
        }
    }

    @Override
    public VarStates startValue() {
        return VarStates.initial(Collections.emptySet());
    }


}
