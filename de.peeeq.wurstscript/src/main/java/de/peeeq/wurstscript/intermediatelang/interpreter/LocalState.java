package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.jassIm.ImVar;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/** Local interpreter values plus their runtime counterparts during compiletime evaluation. */
public class LocalState extends State {

    private @Nullable ILconst returnVal;
    private final Map<ImVar, ILconst> runtimeValues = new IdentityHashMap<>();
    private final Set<ImVar> unknownRuntimeValues = Collections.newSetFromMap(new IdentityHashMap<>());

    public LocalState() {
        // no eager allocations
    }

    public LocalState(ILconst returnVal) {
        this.returnVal = returnVal;
    }

    @Override
    public void setVal(ImVar v, ILconst val) {
        super.setVal(v, val);
        if (val instanceof ILconstBool) {
            ILconstBool boolVal = (ILconstBool) val;
            if (!boolVal.isRuntimeValKnown()) {
                runtimeValues.remove(v);
                unknownRuntimeValues.add(v);
                return;
            }
            runtimeValues.put(v, ILconstBool.instance(boolVal.getRuntimeVal()));
        } else {
            runtimeValues.put(v, val);
        }
        unknownRuntimeValues.remove(v);
    }

    public void setValCompiletimeOnly(ImVar v, ILconst val) {
        super.setVal(v, val);
        if (!runtimeValues.containsKey(v)) {
            unknownRuntimeValues.add(v);
        }
    }

    @Override
    public @Nullable ILconst getVal(ImVar v) {
        ILconst val = super.getVal(v);
        if (!(val instanceof ILconstBool)) {
            return val;
        }
        if (unknownRuntimeValues.contains(v)) {
            return ILconstBool.withUnknownRuntimeValue(((ILconstBool) val).getVal());
        }
        ILconst runtimeVal = runtimeValues.get(v);
        if (runtimeVal instanceof ILconstBool) {
            return ILconstBool.withRuntimeValue(
                ((ILconstBool) val).getVal(), ((ILconstBool) runtimeVal).getVal());
        }
        return val;
    }

    public @Nullable ILconst getRuntimeVal(ImVar v) {
        return unknownRuntimeValues.contains(v) ? null : runtimeValues.get(v);
    }

    public @Nullable ILconst getReturnVal() {
        return returnVal;
    }

    public LocalState setReturnVal(@Nullable ILconst returnVal) {
        this.returnVal = returnVal;
        return this;
    }
}
