package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Map;

import com.google.common.collect.Maps;

public abstract class GetAForB<A,B> {

	private final Map<A, B> thing = Maps.newHashMap();
	
	abstract B initFor(A a);
	
	public B getFor(A a) {
		if (thing.containsKey(a)) {
			return thing.get(a);
		}
		B b = initFor(a);
		thing.put(a, b);
		return b;
	}
	
}
