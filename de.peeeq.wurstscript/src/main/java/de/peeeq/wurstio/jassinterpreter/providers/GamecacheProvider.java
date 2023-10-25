package de.peeeq.wurstio.jassinterpreter.providers;

import com.google.common.collect.ArrayListMultimap;
import de.peeeq.wurstio.jassinterpreter.Implements;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.Objects;

public class GamecacheProvider extends Provider {
    public GamecacheProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    static class KeyPair {
        String missionKey;
        String key;

        KeyPair(String missionKey, String key) {
            this.missionKey = missionKey;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyPair keyPair = (KeyPair) o;
            return Objects.equals(missionKey, keyPair.missionKey) &&
                    Objects.equals(key, keyPair.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(missionKey, key);
        }
    }

    public IlConstHandle InitGameCache(ILconstString name) {
        return new IlConstHandle(name.getVal(), ArrayListMultimap.create());
    }

    @Implements(funcNames = {"StoreInteger", "StoreReal", "StoreBoolean", "StoreUnit", "StoreString" })
    public void Store(IlConstHandle ht, ILconstString key1, ILconstString key2, ILconst value) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        KeyPair keyPair = new KeyPair(key1.getVal(), key2.getVal());
        deleteIfPresent(map, keyPair, value.getClass());
        map.put(keyPair, value);
    }

    @Implements(funcNames = {"SyncStoredInteger", "SyncStoredReal", "SyncStoredBoolean", "SyncStoredUnit", "SyncStoredString" })
    public void Sync(IlConstHandle ht, ILconstString key1, ILconstString key2, ILconst value) {
        // TODO not implemented
    }

    public ILconstInt GetStoredInteger(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return haveSaved(ht, key1, key2, ILconstInt.class) ? load(ht, key1, key2, ILconstInt.class) : ILconstInt.create(0);
    }

    public ILconstReal GetStoredReal(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return haveSaved(ht, key1, key2, ILconstReal.class) ? load(ht, key1, key2, ILconstReal.class) : new ILconstReal(0);
    }

    public ILconstString GetStoredString(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return haveSaved(ht, key1, key2, ILconstString.class) ? load(ht, key1, key2, ILconstString.class) : new ILconstString("");
    }

    public ILconstBool GetStoredBoolean(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return haveSaved(ht, key1, key2, ILconstBool.class) ? load(ht, key1, key2, ILconstBool.class) : ILconstBool.FALSE;
    }

    public IlConstHandle RestoreUnit(IlConstHandle ht, ILconstString key1, ILconstString key2, IlConstHandle player, ILconstReal x, ILconstReal y, ILconstReal facing) {
        return load(ht, key1, key2, IlConstHandle.class);
    }

    public void FlushGameCache(IlConstHandle ht) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        map.clear();
    }

    public void FlushStoredMission(IlConstHandle ht, ILconstString missionKey) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        map.entries().removeIf(entry -> entry.getKey().missionKey.equalsIgnoreCase(missionKey.getVal()));
    }

    public void FlushStoredInteger(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        removeSaved(ht, key1, key2, ILconstInt.class);
    }

    public void FlushStoredReal(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        removeSaved(ht, key1, key2, ILconstReal.class);
    }

    public void FlushStoredBoolean(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        removeSaved(ht, key1, key2, ILconstBool.class);
    }

    public void FlushStoredString(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        removeSaved(ht, key1, key2, ILconstString.class);
    }

    public void FlushStoredUnit(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        removeSaved(ht, key1, key2, IlConstHandle.class);
    }

    public ILconstBool HaveStoredString(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, ILconstInt.class));
    }

    public ILconstBool HaveStoredInteger(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, ILconstInt.class));
    }

    public ILconstBool HaveStoredReal(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, ILconstReal.class));
    }

    public ILconstBool HaveStoredBoolean(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, ILconstBool.class));
    }

    public ILconstBool HaveStoredUnit(IlConstHandle ht, ILconstString key1, ILconstString key2) {
        return ILconstBool.instance(haveSaved(ht, key1, key2, IlConstHandle.class));
    }

    private <T> T load(IlConstHandle ht, ILconstString key1, ILconstString key2, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        KeyPair keyPair = new KeyPair(key1.getVal(), key2.getVal());
        if (hasValueOfType(map, keyPair, clazz)) {
            return getValueOfType(map, keyPair, clazz);
        }
        return null;
    }

    private <T> void removeSaved(IlConstHandle ht, ILconstString key1, ILconstString key2, T type) {
        @SuppressWarnings("unchecked")
        ArrayListMultimap<KeyPair, Object> map = (ArrayListMultimap<KeyPair, Object>) ht.getObj();
        KeyPair keyPair = new KeyPair(key1.getVal(), key2.getVal());
        deleteIfPresent(map, keyPair, type);
    }

    private <T> boolean haveSaved(IlConstHandle ht, ILconstString key1, ILconstString key2, Class<T> clazz) {
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
            if (!map.remove(key, toRemove)) {
                throw new Error("object was found but not deleted");
            }
        }
    }
}
