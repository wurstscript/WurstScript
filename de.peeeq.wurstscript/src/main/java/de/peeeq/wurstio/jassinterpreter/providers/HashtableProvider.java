package de.peeeq.wurstio.jassinterpreter.providers;

import com.google.common.collect.ArrayListMultimap;
import de.peeeq.wurstio.jassinterpreter.Implements;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

import java.util.Objects;

public class HashtableProvider extends Provider {
    public HashtableProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public static class KeyPair {
        private final int parentkey;
        private final int childkey;

        KeyPair(int parentkey, int childkey) {
            this.parentkey = parentkey;
            this.childkey = childkey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyPair keyPair = (KeyPair) o;
            return parentkey == keyPair.parentkey &&
                    childkey == keyPair.childkey;
        }

        @Override
        public int hashCode() {
            return Objects.hash(parentkey, childkey);
        }

        public int getParentkey() {
            return parentkey;
        }

        public int getChildkey() {
            return childkey;
        }
    }

    public IlConstHandle InitHashtable() {
        return new IlConstHandle(NameProvider.getRandomName("ht"), ArrayListMultimap.create());
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
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        KeyPair keyPair = new KeyPair(key1.getVal(), key2.getVal());
        deleteIfPresent(map, keyPair, value.getClass());
        map.put(keyPair, value);
    }

    public ILconstInt LoadInteger(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return haveSaved(ht, key1, key2, ILconstInt.class) ? load(ht, key1, key2, ILconstInt.class) : ILconstInt.create(0);
    }

    public ILconstReal LoadReal(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return haveSaved(ht, key1, key2, ILconstReal.class) ? load(ht, key1, key2, ILconstReal.class) : new ILconstReal(0);
    }

    public ILconstString LoadStr(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return  haveSaved(ht, key1, key2, ILconstString.class) ? load(ht, key1, key2, ILconstString.class) : new ILconstString("");
    }

    public ILconstBool LoadBoolean(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return haveSaved(ht, key1, key2, ILconstBool.class) ? load(ht, key1, key2, ILconstBool.class) : ILconstBool.FALSE;
    }

    @Implements(funcNames = {"LoadPlayerHandle", "LoadWidgetHandle", "LoadDestructableHandle", "LoadItemHandle", "LoadUnitHandle", "LoadAbilityHandle",
            "LoadTimerHandle", "LoadTriggerHandle", "LoadTriggerConditionHandle", "LoadTriggerActionHandle", "LoadTriggerEventHandle", "LoadForceHandle",
            "LoadGroupHandle", "LoadLocationHandle", "LoadRectHandle", "LoadBooleanExprHandle", "LoadSoundHandle", "LoadEffectHandle", "LoadUnitPoolHandle",
            "LoadItemPoolHandle", "LoadQuestHandle", "LoadQuestItemHandle", "LoadDefeatConditionHandle", "LoadTimerDialogHandle", "LoadLeaderboardHandle",
            "LoadMultiboardHandle", "LoadMultiboardItemHandle", "LoadTrackableHandle", "LoadDialogHandle", "LoadButtonHandle", "LoadTextTagHandle",
            "LoadLightningHandle", "LoadImageHandle", "LoadUbersplatHandle", "LoadRegionHandle", "LoadFogStateHandle", "LoadFogModifierHandle",
            "LoadHashtableHandle"})
    public IlConstHandle LoadHandle(IlConstHandle ht, ILconstInt key1, ILconstInt key2) {
        return load(ht, key1, key2, IlConstHandle.class);
    }

    public void FlushParentHashtable(IlConstHandle ht) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        map.clear();
    }

    public void FlushChildHashtable(IlConstHandle ht, ILconstInt parentKey) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        map.entries().removeIf(entry -> entry.getKey().parentkey == parentKey.getVal());
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

    private <T> T load(IlConstHandle ht, ILconstInt key1, ILconstInt key2, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        KeyPair keyPair = new KeyPair(key1.getVal(), key2.getVal());
        if (hasValueOfType(map, keyPair, clazz)) {
            return getValueOfType(map, keyPair, clazz);
        }
        return null;
    }

    private <T> void removeSaved(IlConstHandle ht, ILconstInt key1, ILconstInt key2, T type) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        KeyPair keyPair = new KeyPair(key1.getVal(), key2.getVal());
        deleteIfPresent(map, keyPair, type);
    }

    private <T> boolean haveSaved(IlConstHandle ht, ILconstInt key1, ILconstInt key2, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        KeyPair keyPair = new KeyPair(key1.getVal(), key2.getVal());
        return hasValueOfType(map, keyPair, clazz);
    }

    private static <T> T getValueOfType(ArrayListMultimap<KeyPair, Object> map, KeyPair key, Class<T> clazz) {
        for (Object o : map.get(key)) {
            if (o.getClass() == clazz) {
                return (T) o;
            }
        }
        return null;
    }

    private static <T> boolean hasValueOfType(ArrayListMultimap<KeyPair, Object> map, KeyPair key, Class<T> type) {
        for (Object o : map.get(key)) {
            if (o.getClass() == type) {
                return true;
            }
        }
        return false;
    }

    private static <T> void deleteIfPresent(ArrayListMultimap<KeyPair, Object> map, KeyPair key, T type) {
        Object toRemove = null;
        for (Object o : map.get(key)) {
            if (o.getClass() == type) {
                toRemove = o;
                break;
            }
        }
        if (toRemove != null) {
            if(!map.remove(key, toRemove)) {
                throw new Error("object was found but not deleted");
            }
        }
    }
}
