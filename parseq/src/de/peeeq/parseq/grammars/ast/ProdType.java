package de.peeeq.parseq.grammars.ast;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class ProdType {
	private static final ProdType empty = new ProdType();
	private final Map<String, SimpleType> data = Maps.newLinkedHashMap();
	
	public ProdType() {
	}
	
	public ProdType(String key, SimpleType type) {
		data.put(key, type);
	}
	
	public ProdType sequence(ProdType other) {
		ProdType result = new ProdType();
		result.data.putAll(data);
		for (Entry<String, SimpleType> e : other.data.entrySet()) {
			String key = e.getKey().replaceAll("[0-9]+", "");
			int i = 0;
			while (result.data.containsKey(key)) {
				i++;
				key = e.getKey() + i;
			}
			result.data.put(key, e.getValue());
		}
		// TODO check if key sets are disjoint?
		return result;
	}
	
	public ProdType alternative(ProdType other) {
		ProdType result = new ProdType();
		result.data.putAll(data);
		result.data.putAll(other.data);
		// TODO check if key sets are disjoint?
		return result;
	}

	public static ProdType empty() {
		return empty;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ProdType[");
		for (Entry<String, SimpleType> e : data.entrySet()) {
			sb.append(e.getKey() + " -> " + e.getValue() + ", ");
		}
		sb.append("]");
		return sb.toString();
	}
}
