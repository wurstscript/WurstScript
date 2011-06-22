package de.peeeq.immutablecollections;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.peeeq.pscript.attributes.Declaration;


public abstract class ImmutableMap<K,V> {

	abstract public ImmutableMap<K,V> put(K key, V val);
	abstract public V get(K key);	
	abstract public boolean containsKey(K key);
	
	/**
	 * returns all entries of the list in some order with no duplicate keys
	 */
	abstract Iterable<Entry<K,V>> entries();
	public static ImmutableMap<String, ImmutableList<Declaration>> emptyMap() {
		return ImmutableMapEmpty.instance;
	}
	
	/**
	 * @return all the values in the map
	 */
	public Iterable<V> values() {
		Iterable<Entry<K, V>> entries = entries();
		LinkedList<V> s = new LinkedList<V>();
		for (Entry<K, V> e : entries) {
			s.add(e.val);
		}
		return s;
	}
}


class Entry<K,V> {
	final K key;
	final V val;
	final int keyHash;
	
	public Entry(K key, V val) {
		this.key = key;
		this.val = val;
		this.keyHash = key.hashCode();
	}
}

class ImmutableMapEmpty<K,V> extends ImmutableMap<K, V> {

	static final ImmutableMapEmpty instance = new ImmutableMapEmpty();
	
	@Override
	public ImmutableMap<K, V> put(K key, V val) {
		return new ImmutableHashMap<K, V>(key, val, this);
	}

	@Override
	public V get(K key) {
		return null;
	}

	@Override
	public boolean containsKey(K key) {
		return false;
	}

	@Override
	public Iterable<Entry<K, V>> entries() {
		return ImmutableList.emptyList();
	}

	
}

class ImmutableHashMap<K,V> extends ImmutableMap<K, V> {

	
	
	private ImmutableList<Entry<K,V>>[] table;
	
	public ImmutableHashMap(K key, V val,
			ImmutableMap<K, V> map) {
		
		if (map instanceof ImmutableHashMap) {
			ImmutableHashMap<K, V> hmap = (ImmutableHashMap<K, V>) map;
			int size = hmap.table.length;
			this.table = new ImmutableList[size];
			
			int pos = pos(key);
			// copy array
			// TODO might be faster with arraycopy 
			for (int i=0; i<size; i++) {
				if (pos == i) {
					table[i] = hmap.table[i].appFront(new Entry<K, V>(key, val));
				} else {
					table[i] = hmap.table[i];
				}
			}			
		} else {
			int size = 31;
			this.table = new ImmutableList[size];
			for (int i=0; i<size; i++) {
				table[i] = ImmutableList.emptyList();
			}	
			// add all elements
			for (Entry<K, V> e : map.entries()) {
				int pos = pos(e);
				table[pos] = table[pos].appFront(e);
			}
		}
		
	}

	private int pos(Entry<K, V> e) {
		return pos(e.key);
	}

	private int pos(K key) {
		return pos(key.hashCode()); 
	}

	private int pos(int keyHash) {
		return hash(keyHash) % table.length;
	}
	
	/**
	 * stolen from java.util.HashMap
	 * 
	 * Applies a supplemental hash function to a given hashCode, which defends
	 * against poor quality hash functions. This is critical because HashMap
	 * uses power-of-two length hash tables, that otherwise encounter collisions
	 * for hashCodes that do not differ in lower bits. Note: Null keys always
	 * map to hash 0, thus index 0.
	 */
	static int hash(int h) {
		// This function ensures that hashCodes that differ only by
		// constant multiples at each bit position have a bounded
		// number of collisions (approximately 8 at default load factor).
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}

	@Override
	public ImmutableMap<K, V> put(K key, V val) {
		return new ImmutableHashMap<K,V>(key, val, this);
	}

	@Override
	public V get(K key) {
		int keyHash = key.hashCode();
		int pos = pos(keyHash);
		for (Entry<K, V> x : table[pos]) {
			if (x.keyHash == keyHash  && x.key.equals(key)) {
				return x.val;
			}
		}
		return null;
	}

	

	@Override
	public boolean containsKey(K key) {
		int keyHash = key.hashCode();
		int pos = pos(keyHash);
		for (Entry<K, V> x : table[pos]) {
			if (x.keyHash == keyHash  && x.key.equals(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterable<Entry<K, V>> entries() {
		ImmutableList<Entry<K,V>> result = ImmutableList.emptyList();
		Set<K> keys = new HashSet<K>();
		for (int i = 0; i<table.length; i++) {
			for (Entry<K, V> x : table[i]) {
				if (!keys.contains(x.key)) {
					keys.add(x.key);
					result = result.appFront(x);
				}
			}
		}
		return result;
	}

	
}