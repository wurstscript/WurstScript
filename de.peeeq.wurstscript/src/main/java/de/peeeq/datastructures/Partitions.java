package de.peeeq.datastructures;

import com.google.common.collect.Maps;

import java.util.Map;

public class Partitions<T> {

    private class Partition {
        private Partition rep;
        private final T item;

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

    private final Map<T, Partition> partitions = Maps.newLinkedHashMap();

    public Partitions() {
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
        return partitions.computeIfAbsent(b, k -> new Partition(b));
    }

    public boolean contains(T t) { // NO_UCD (unused code)
        return partitions.containsKey(t);
    }
}
