package de.peeeq.wurstio.jassinterpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.intermediateLang.ILconst;
import de.peeeq.wurstscript.intermediateLang.ILconstAbstract;
import de.peeeq.wurstscript.types.WurstType;

import java.util.Map;

public class JassArray extends ILconstAbstract {

    private String type;
    private Map<Integer, ILconst> values = Maps.newLinkedHashMap();

    public JassArray(String type) {
        this.type = type;
    }

    public WurstType getType() {
        throw new Error("Not implemented.");
    }


    @Override
    public String print() {
        throw new Error("Not implemented yet.");
    }

    public void set(int key, ILconst value) {
        values.put(key, value);
    }

    public ILconst get(int key) {
        if (values.containsKey(key)) {
            return values.get(key);
        } else {
            return JassInterpreter.getDefaultValue(type);
        }
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        throw new Error("Cannot compare arrays.");
    }

}
