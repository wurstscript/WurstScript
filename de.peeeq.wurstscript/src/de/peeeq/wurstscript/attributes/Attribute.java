package de.peeeq.wurstscript.attributes;

import java.util.Map;
import java.util.WeakHashMap;

import de.peeeq.wurstscript.ast.AST.SortPos;

/**
 *
 * An attribute which is defined on some nodes
 * 
 * this class implements the caching so each attribute is only evaluated once
 * 
 * @param <N> the nodes on which the attribute is defined
 * @param <R> the value computed by the attribute
 */
abstract class Attribute<N extends SortPos,R> {

	public Attribute(Attributes attr) {
		this.attr = attr;
	}
	
	protected Attributes attr;
	
	// using a weak hashmap here to avoid memory leaks caused by caching
	private Map<N,R> cache = new WeakHashMap<N, R>();
	
	/**
	 * get the value of the attribute 
	 */
	public R get(N node) {
		if (node == null) {
			throw new IllegalArgumentException("node must not be null (" + (this.getClass()) + " )");
		}
		if (cache.containsKey(node)) {
			return cache.get(node); 
		}
		R r = calculate(node);
		cache.put(node, r);
		return r;
	}

	/** 
	 * calculate the value of the attribute 
	 */
	abstract protected R calculate(N node);
	
}
