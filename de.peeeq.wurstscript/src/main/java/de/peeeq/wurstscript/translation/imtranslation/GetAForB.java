package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Maps;

import java.util.Map;

public abstract class GetAForB<B, A> {

    private final Map<B, A> thing = Maps.newLinkedHashMap();

    public abstract A initFor(B a);

    public A getFor(B a) {
        if (thing.containsKey(a)) {
            return thing.get(a);
        }
        A b = initFor(a);
        thing.put(a, b);
        return b;
    }

}
