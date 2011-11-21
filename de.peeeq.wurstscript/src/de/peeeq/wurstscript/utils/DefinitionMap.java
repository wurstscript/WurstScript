package de.peeeq.wurstscript.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Map which calls the method onElementRedefined if you try to add two elements with the same key
 * everything else works just like a normal HashMap 
 */
public abstract class DefinitionMap<K,V> extends HashMap<K, V> {
	private static final long serialVersionUID = -2053400305717662756L;

	
		
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (java.util.Map.Entry<? extends K, ? extends V> k : m.entrySet()) {
			put(k.getKey(), k.getValue());
		}
//		super.putAll(m);
	}
	
	public V put(K key, V value) {
		if (containsKey(key)) {
			onElementRedefined(get(key), value, key);
		}
		return super.put(key, value);
	}

	public abstract void onElementRedefined(V firstDefinition, V secondDefinition, K name);


	
}
