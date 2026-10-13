package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.jassIm.ImTypeArgument;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImTypeVar;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

/**
 * Unchanged API. No eager map allocations unless you actually set/get vars/arrays.
 */
public class LocalState extends State {

    private @Nullable ILconst returnVal;
    /**
     * Set by a return statement: the statements around it stop running and the call ends. A Java exception
     * did this before, and unwinding one through the interpreter's frames on every return was a large part
     * of the cost of a call.
     */
    private boolean returned;
    private @Nullable ILconst returnedValue;
    /** Set by an exitwhen whose condition holds: the statements around it stop, and the loop ends. */
    private boolean exiting;

    /**
     * The local variables and their values, side by side. A call has a few locals, and looking them up by
     * identity in an array is cheaper than allocating and filling a hash map for every call. A function with
     * many locals goes over to the map of {@link State}.
     */
    private static final int MAX_ARRAY_LOCALS = 16;
    private static final ImVar[] NO_VARS = new ImVar[0];
    private static final ILconst[] NO_VALUES = new ILconst[0];
    private ImVar[] localVars = NO_VARS;
    private ILconst[] localValues = NO_VALUES;
    private int localCount;
    private boolean localsInMap;

    @Override
    public void setVal(ImVar v, ILconst val) {
        if (localsInMap) {
            super.setVal(v, val);
            return;
        }
        ImVar[] vars = localVars;
        for (int i = 0; i < localCount; i++) {
            if (vars[i] == v) {
                localValues[i] = val;
                return;
            }
        }
        if (localCount == vars.length) {
            if (localCount == MAX_ARRAY_LOCALS) {
                for (int i = 0; i < localCount; i++) {
                    super.setVal(vars[i], localValues[i]);
                }
                localsInMap = true;
                localVars = NO_VARS;
                localValues = NO_VALUES;
                super.setVal(v, val);
                return;
            }
            localVars = vars = java.util.Arrays.copyOf(vars, Math.max(4, vars.length * 2));
            localValues = java.util.Arrays.copyOf(localValues, vars.length);
        }
        vars[localCount] = v;
        localValues[localCount] = val;
        localCount++;
    }

    @Override
    public @Nullable ILconst getVal(ImVar v) {
        if (localsInMap) {
            return super.getVal(v);
        }
        ImVar[] vars = localVars;
        for (int i = 0; i < localCount; i++) {
            if (vars[i] == v) {
                return localValues[i];
            }
        }
        return null;
    }
    /**
     * The type arguments this invocation was called with, kept only when the program still
     * contains generics, i.e. when running before generic elimination. Type class dispatch reads
     * the instance bound to a type argument from here.
     */
    private @Nullable Map<ImTypeVar, ImTypeArgument> typeArguments;

    public void setTypeArguments(Map<ImTypeVar, ImTypeArgument> typeArguments) {
        this.typeArguments = typeArguments;
    }

    /**
     * The argument bound to this type variable.
     * <p>
     * One source type parameter can be represented by several {@code ImTypeVar} nodes — a class and
     * its constructor hold separate ones — so fall back to matching by name, as generic elimination
     * does.
     */
    public @Nullable ImTypeArgument getTypeArgument(ImTypeVar typeVar) {
        if (typeArguments == null) {
            return null;
        }
        ImTypeArgument exact = typeArguments.get(typeVar);
        if (exact != null) {
            return exact;
        }
        for (Map.Entry<ImTypeVar, ImTypeArgument> e : typeArguments.entrySet()) {
            if (e.getKey().getName().equals(typeVar.getName())) {
                return e.getValue();
            }
        }
        return null;
    }

    public Map<ImTypeVar, ImTypeArgument> getTypeArguments() {
        return typeArguments == null ? java.util.Collections.emptyMap() : typeArguments;
    }

    public LocalState() {
        // no eager allocations
    }

    public void returnWith(@Nullable ILconst value) {
        returned = true;
        returnedValue = value;
    }

    public boolean hasReturned() {
        return returned;
    }

    /** The value of the return, which is consumed. */
    public @Nullable ILconst takeReturn() {
        ILconst value = returnedValue;
        returned = false;
        returnedValue = null;
        return value;
    }

    public void exitLoop() {
        exiting = true;
    }

    /** Whether an exitwhen ended the loop, which is consumed. */
    public boolean consumeExit() {
        boolean result = exiting;
        exiting = false;
        return result;
    }

    /** Whether the statements being run have to stop: after a return, or an exitwhen. */
    public boolean isLeaving() {
        return returned || exiting;
    }

    public LocalState(ILconst returnVal) {
        this.returnVal = returnVal;
    }

    public @Nullable ILconst getReturnVal() {
        return returnVal;
    }

    public LocalState setReturnVal(@Nullable ILconst returnVal) {
        this.returnVal = returnVal;
        return this;
    }
}
