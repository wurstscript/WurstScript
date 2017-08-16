package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.jassIm.ImVar;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;
import java.util.Map.Entry;

public abstract class State {

    private Map<ImVar, ILconst> values = Maps.newLinkedHashMap();
    private Map<ImVar, Map<Integer, ILconst>> arrayValues = Maps.newLinkedHashMap();


    public void setVal(ImVar v, ILconst val) {
        values.put(v, val);
    }

    public @Nullable ILconst getVal(ImVar v) {
        return values.get(v);
    }

    private Map<Integer, ILconst> getArray(ImVar v) {
        Map<Integer, ILconst> r = arrayValues.get(v);
        if (r == null) {
            r = Maps.newLinkedHashMap();
            arrayValues.put(v, r);
        }
        return r;
    }

    public void setArrayVal(ImVar v, int index, ILconst val) {
        getArray(v).put(index, val);
    }

    public @Nullable ILconst getArrayVal(ImVar v, int index) {
        return getArray(v).get(index);
    }

    public @Nullable ILconst getVarValue(String varName) {
        for (Entry<ImVar, ILconst> e : values.entrySet()) {
            if (e.getKey().getName().equals(varName)) {
                return e.getValue();
            }
        }
        return null;
    }


}