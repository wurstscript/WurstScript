package de.peeeq.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Partitions<T> {

	private class Partition {
		private Partition rep;
		private T item;

		public Partition(T item) {
			rep = this;
			this.item = item;
		}

		public void unionWith(Partition other) {
			getRep().rep = other.getRep();
		}

		private Partition getRep() {
			if (rep == this) {
				return this;
			} else {
				rep = rep.getRep(); // path compression
				return rep;
			}
		}
	}
	
	private Map<T, Partition> partitions = Maps.newLinkedHashMap();
	
	public Partitions() {
	}
	
	public Partitions(Collection<T> items) {
		for (T t : items) {
			partitions.put(t, new Partition(t));
		}
	}

	/**
	 * unions the partition of a and the partition of b.
	 */
	public void union(T a, T b) {
		getPartition(a).unionWith(getPartition(b));
	}


	/**
	 * add a new partition
	 */
	public void add(T t) {
		getPartition(t);
	}

	/**
	 * get the representing item for the partition containing t 
	 */
	public T getRep(T t) {
		return getPartition(t).getRep().item;
	}

	private Partition getPartition(T b) {
		Partition p = partitions.get(b);
		if (p == null) {
			p = new Partition(b);
			partitions.put(b, p);
		}
		return p;
	}

	public boolean contains(T t) {
		return partitions.containsKey(t);
	}
}
