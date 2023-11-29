package de.peeeq.wurstio.jassinterpreter;

import com.google.common.collect.Maps;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstAbstract;
import de.peeeq.wurstscript.types.WurstType;

import java.util.Map;

public class JassArray extends ILconstAbstract {

    private final String type;
    private final Map<Integer, ILconst> values = Maps.newLinkedHashMap();

    public JassArray(String type) {
        this.type = type;
    }

    public WurstType getType() {
        throw new Error("Not implemented.");
    }


    @Override
    public String print() {
        StringBuilder res = new StringBuilder();
        int i = 0;
        while (true) {
            ILconst v = values.get(i);
            if (v == null) {
                break;
            }
            if (res.length() > 0) {
                res.append(", ");
            }
            res.append(v);
            i++;
        }

        int finalI = i;
        values.keySet().stream().sorted().filter(x -> x < 0 || x >= finalI).forEach(k -> {
            ILconst v = values.get(k);
            if (res.length() > 0) {
                res.append(", ");
            }
            res.append(k);
            res.append(" -> ");
            res.append(v);
        });

        return "[" + res + "]";
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
