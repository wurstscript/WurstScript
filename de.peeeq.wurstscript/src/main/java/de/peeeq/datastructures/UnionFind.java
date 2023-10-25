package de.peeeq.datastructures;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of https://en.wikipedia.org/wiki/Disjoint-set_data_structure
 */
public class UnionFind<T> {
    private Map<T, T> parent = new LinkedHashMap<>();
    private Map<T, Integer> rank = new LinkedHashMap<>();

    public T find(T x) {
        if (!parent.containsKey(x)) {
            parent.put(x, x);
            rank.put(x, 0);
        }

        if (!parent.get(x).equals(x)) {
            parent.put(x, find(parent.get(x)));
        }
        return parent.get(x);
    }

    public void union(T x, T y) {
        T xRoot = find(x);
        T yRoot = find(y);

        // x and y are already in the same set
        if (xRoot.equals(yRoot)) {
            return;
        }

        // x and y are not in same set, so we merge them
        if (rank.get(xRoot) < rank.get(yRoot)) {
            // swap xRoot and yRoot
            T temp = xRoot;
            xRoot = yRoot;
            yRoot = temp;
        }

        // merge yRoot into xRoot
        parent.put(yRoot, xRoot);
        if (rank.get(xRoot).equals(rank.get(yRoot))) {
            rank.put(xRoot, rank.get(xRoot));
        }
    }

    public Map<T, Set<T>> groups() {
        Map<T, Set<T>> result = new LinkedHashMap<>();
        for (T t : parent.keySet()) {
            result.put(t, new LinkedHashSet<>());
        }
        for (Map.Entry<T, T> e : parent.entrySet()) {
            result.get(e.getValue()).add(e.getKey());
        }
        return result;
    }

}
