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

    void copyKnownTo(GetAForB<B, A> target, java.util.function.Function<B, B> keys,
                     java.util.function.Function<A, A> values) {
        thing.forEach((key, value) -> target.thing.put(keys.apply(key), values.apply(value)));
    }

}
