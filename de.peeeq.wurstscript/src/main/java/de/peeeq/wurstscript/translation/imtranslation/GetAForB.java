package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Maps;

import java.util.Map;

public abstract class GetAForB<B, A> {

    private final Map<B, A> thing = Maps.newLinkedHashMap();

    public abstract A initFor(B a);

    public A getFor(B a) {
        A existing = thing.get(a);
        if (existing != null) {
            return existing;
        }
        A created = initFor(a);
        thing.put(a, created);
        return created;
    }

    /** Number of memoized entries; used to detect unexpected creations during cached phases. */
    public int cachedCount() {
        return thing.size();
    }

}
