package de.peeeq.wurstio.jassinterpreter.providers;

import com.google.common.collect.Maps;
import de.peeeq.wurstio.jassinterpreter.Implements;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

import java.util.Map;

public class HashtableProvider extends Provider {
    public HashtableProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle InitHashtable() {
        return new IlConstHandle(NameProvider.getRandomName("ht"), Maps.newLinkedHashMap());
    }

    public void SaveInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2, ILconstInt value) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.computeIfAbsent(key1.getVal(), k -> Maps.newLinkedHashMap());
        map2.put(key2.getVal(), value);
    }

    public ILconstInt LoadInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            Object value = map2.get(key2.getVal());
            if (value instanceof ILconstInt) {
                return (ILconstInt) value;
            }
        }
        return ILconstInt.create(0);
    }

    public void SaveStr(IlConstHandle ht, ILconstInt key1, ILconstInt key2, ILconstString value) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.computeIfAbsent(key1.getVal(), k -> Maps.newLinkedHashMap());
        map2.put(key2.getVal(), value);
        WLogger.info("savestr of key1: " + key1.getVal() + ", key2: " + key2.getVal() + ", val: " + value.getVal());
    }

    public ILconstString LoadStr(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            Object value = map2.get(key2.getVal());
            if (value instanceof ILconstString) {
                return (ILconstString) value;
            }
        }
        return new ILconstString("");
    }

    public void FlushChildHashtable(IlConstHandle ht, ILconstInt parentKey) {
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        map.remove(parentKey.getVal());
    }

    @Implements(funcNames = {"RemoveSavedInteger", "RemoveSavedReal", "RemoveSavedBoolean", "RemoveSavedString", "RemoveSavedHandle"})
    public void removeSaved(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        @SuppressWarnings("unchecked")
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            map2.remove(key2.getVal());
        }
    }

    @Implements(funcNames = {"HaveSavedInteger", "HaveSavedReal", "HaveSavedBoolean", "HaveSavedString", "HaveSavedHandle"})
    private ILconstBool haveSaved(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        Map<Integer, Map<Integer, Object>> map = (Map<Integer, Map<Integer, Object>>) ht.getObj();
        Map<Integer, Object> map2 = map.get(key1.getVal());
        if (map2 != null) {
            WLogger.info("HaveSavedString of key1: " + key1.getVal() + ", key2: " + key2.getVal() + ", is: " + map2.containsKey(key2.getVal()));
            return ILconstBool.instance(map2.containsKey(key2.getVal()));
        }
        WLogger.info("HaveSavedString false");
        return ILconstBool.FALSE;
    }
}
