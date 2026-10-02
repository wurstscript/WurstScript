package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.Implements;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.*;

public class HashtableProvider extends Provider {
    public HashtableProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public static class KeyPair {
        private final int parentkey;
        private final int childkey;

        public KeyPair(int parentkey, int childkey) {
            this.parentkey = parentkey;
            this.childkey = childkey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyPair keyPair = (KeyPair) o;
            return parentkey == keyPair.parentkey && childkey == keyPair.childkey;
        }

        @Override
        public int hashCode() {
            return 31 * parentkey + childkey;
        }

        public int getParentkey() {
            return parentkey;
        }

        public int getChildkey() {
            return childkey;
        }
    }

    public static class WurstHashtable {
        private final Map<Integer, Map<Integer, Map<Class<?>, Object>>> data = new LinkedHashMap<>();

        public void save(int parentKey, int childKey, Object value) {
            data.computeIfAbsent(parentKey, k -> new LinkedHashMap<>(8))
                .computeIfAbsent(childKey, k -> new LinkedHashMap<>(2))
                .put(value.getClass(), value);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(int parentKey, int childKey, Class<T> clazz) {
            Map<Integer, Map<Class<?>, Object>> parent = data.get(parentKey);
            if (parent == null) return null;
            Map<Class<?>, Object> child = parent.get(childKey);
            if (child == null) return null;
            return (T) child.get(clazz);
        }

        public boolean has(int parentKey, int childKey, Class<?> clazz) {
            Map<Integer, Map<Class<?>, Object>> parent = data.get(parentKey);
            if (parent == null) return false;
            Map<Class<?>, Object> child = parent.get(childKey);
            return child != null && child.containsKey(clazz);
        }

        public void remove(int parentKey, int childKey, Class<?> clazz) {
            Map<Integer, Map<Class<?>, Object>> parent = data.get(parentKey);
            if (parent != null) {
                Map<Class<?>, Object> child = parent.get(childKey);
                if (child != null) {
                    child.remove(clazz);
                    if (child.isEmpty()) {
                        parent.remove(childKey);
                        if (parent.isEmpty()) {
                            data.remove(parentKey);
                        }
                    }
                }
            }
        }

        public void flushParent() {
            data.clear();
        }

        public void flushChild(int parentKey) {
            data.remove(parentKey);
        }

        public static class Entry {
            public final int parentKey;
            public final int childKey;
            public final Object value;
            public Entry(int p, int c, Object v) { this.parentKey = p; this.childKey = c; this.value = v; }
        }

        public List<Entry> entries() {
            List<Entry> result = new ArrayList<>();
            for (Map.Entry<Integer, Map<Integer, Map<Class<?>, Object>>> pe : data.entrySet()) {
                int p = pe.getKey();
                for (Map.Entry<Integer, Map<Class<?>, Object>> ce : pe.getValue().entrySet()) {
                    int c = ce.getKey();
                    for (Object v : ce.getValue().values()) {
                        result.add(new Entry(p, c, v));
                    }
                }
            }
            return result;
        }

        public int size() {
            int count = 0;
            for (Map<Integer, Map<Class<?>, Object>> parent : data.values()) {
                for (Map<Class<?>, Object> child : parent.values()) {
                    count += child.size();
                }
            }
            return count;
        }
    }

    public IlConstHandle InitHashtable() {
        return new IlConstHandle(NameProvider.getRandomName("ht"), new WurstHashtable());
    }

    @Implements(funcNames = {"SaveInteger", "SaveStr", "SaveReal", "SaveBoolean", "SavePlayerHandle", "SaveWidgetHandle", "SaveDestructableHandle",
            "SaveItemHandle", "SaveUnitHandle", "SaveAbilityHandle", "SaveTimerHandle", "SaveTriggerHandle", "SaveTriggerConditionHandle",
            "SaveTriggerActionHandle", "SaveTriggerEventHandle", "SaveForceHandle", "SaveGroupHandle", "SaveLocationHandle", "SaveRectHandle",
            "SaveBooleanExprHandle", "SaveSoundHandle", "SaveEffectHandle", "SaveUnitPoolHandle", "SaveItemPoolHandle", "SaveQuestHandle",
            "SaveQuestItemHandle", "SaveDefeatConditionHandle", "SaveTimerDialogHandle", "SaveLeaderboardHandle", "SaveMultiboardHandle",
            "SaveMultiboardItemHandle", "SaveTrackableHandle", "SaveDialogHandle", "SaveButtonHandle", "SaveTextTagHandle", "SaveLightningHandle",
            "SaveImageHandle", "SaveUbersplatHandle", "SaveRegionHandle", "SaveFogStateHandle", "SaveFogModifierHandle", "SaveAgentHandle",
            "SaveHashtableHandle",
    })
    public void Save(IlConstHandle ht, ILconstInt key1, ILconstInt key2, ILconst value) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        table.save(key1.getVal(), key2.getVal(), value);
    }

    public ILconstInt LoadInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        ILconstInt res = table.get(key1.getVal(), key2.getVal(), ILconstInt.class);
        return res != null ? res : ILconstInt.create(0);
    }

    public ILconstReal LoadReal(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        ILconstReal res = table.get(key1.getVal(), key2.getVal(), ILconstReal.class);
        return res != null ? res : new ILconstReal(0);
    }

    public ILconstString LoadStr(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        ILconstString res = table.get(key1.getVal(), key2.getVal(), ILconstString.class);
        return res != null ? res : ILconstString.fromText("");
    }

    public ILconstBool LoadBoolean(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        ILconstBool res = table.get(key1.getVal(), key2.getVal(), ILconstBool.class);
        return res != null ? res : ILconstBool.FALSE;
    }

    @Implements(funcNames = {"LoadPlayerHandle", "LoadWidgetHandle", "LoadDestructableHandle", "LoadItemHandle", "LoadUnitHandle", "LoadAbilityHandle",
            "LoadTimerHandle", "LoadTriggerHandle", "LoadTriggerConditionHandle", "LoadTriggerActionHandle", "LoadTriggerEventHandle", "LoadForceHandle",
            "LoadGroupHandle", "LoadLocationHandle", "LoadRectHandle", "LoadBooleanExprHandle", "LoadSoundHandle", "LoadEffectHandle", "LoadUnitPoolHandle",
            "LoadItemPoolHandle", "LoadQuestHandle", "LoadQuestItemHandle", "LoadDefeatConditionHandle", "LoadTimerDialogHandle", "LoadLeaderboardHandle",
            "LoadMultiboardHandle", "LoadMultiboardItemHandle", "LoadTrackableHandle", "LoadDialogHandle", "LoadButtonHandle", "LoadTextTagHandle",
            "LoadLightningHandle", "LoadImageHandle", "LoadUbersplatHandle", "LoadRegionHandle", "LoadFogStateHandle", "LoadFogModifierHandle",
            "LoadHashtableHandle"})
    public IlConstHandle LoadHandle(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        return table.get(key1.getVal(), key2.getVal(), IlConstHandle.class);
    }

    public void FlushParentHashtable(IlConstHandle ht) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        table.flushParent();
    }

    public void FlushChildHashtable(IlConstHandle ht, ILconstInt parentKey) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        table.flushChild(parentKey.getVal());
    }

    public void RemoveSavedInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        removeSaved(ht, key1, key2, ILconstInt.class);
    }

    public void RemoveSavedReal(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        removeSaved(ht, key1, key2, ILconstReal.class);
    }

    public void RemoveSavedBoolean(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        removeSaved(ht, key1, key2, ILconstBool.class);
    }

    public void RemoveSavedString(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        removeSaved(ht, key1, key2, ILconstString.class);
    }

    public void RemoveSavedHandle(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        removeSaved(ht, key1, key2, IlConstHandle.class);
    }

    public ILconstBool HaveSavedString(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, ILconstString.class));
    }

    public ILconstBool HaveSavedInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, ILconstInt.class));
    }

    public ILconstBool HaveSavedReal(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, ILconstReal.class));
    }

    public ILconstBool HaveSavedBoolean(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, ILconstBool.class));
    }

    public ILconstBool HaveSavedHandle(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, IlConstHandle.class));
    }

    private <T> void removeSaved(IlConstHandle ht, ILconstInt key1, ILconstInt key2, Class<T> clazz) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        table.remove(key1.getVal(), key2.getVal(), clazz);
    }

    private <T> boolean haveSaved(IlConstHandle ht, ILconstInt key1, ILconstInt key2, Class<T> clazz) {
        WurstHashtable table = (WurstHashtable) ht.getObj();
        return table.has(key1.getVal(), key2.getVal(), clazz);
    }
}
