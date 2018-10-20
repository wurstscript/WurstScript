package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstArray;
import de.peeeq.wurstscript.jassIm.ImVar;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class State {

    private Map<ImVar, ILconst> values = Maps.newLinkedHashMap();
    protected Map<ImVar, ILconstArray> arrayValues = Maps.newLinkedHashMap();


    public void setVal(ImVar v, ILconst val) {
        values.put(v, val);
    }

    public @Nullable ILconst getVal(ImVar v) {
        return values.get(v);
    }

    protected ILconstArray getArray(ImVar v) {
        return arrayValues.computeIfAbsent(v, k -> new ILconstArray(() -> v.getType().defaultValue()));
    }

    public void setArrayVal(ImVar v, List<Integer> indexes, ILconst val) {
        ILconstArray ar = getArray(v);
        for (int i = 0; i < indexes.size() - 1; i++) {
            ar = (ILconstArray) ar.get(indexes.get(i));
        }
        ar.set(indexes.get(indexes.size() - 1), val);
    }

    public @Nullable ILconst getArrayVal(ImVar v, List<Integer> indexes) {
        ILconstArray ar = getArray(v);
        for (int i = 0; i < indexes.size() - 1; i++) {
            ar = (ILconstArray) ar.get(indexes.get(i));
        }
        return ar.get(indexes.get(indexes.size() - 1));
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