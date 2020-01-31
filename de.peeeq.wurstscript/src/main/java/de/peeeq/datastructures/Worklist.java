package de.peeeq.datastructures;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A queue with efficient lookup using a hashset.
 * <p>
 * No element is added to the queue more than once.
 */
public class Worklist<T> {
    private ArrayDeque<T> queue = new ArrayDeque<>();
    private HashSet<T> set = new HashSet<>();

    public Worklist() {
    }

    public Worklist(Iterable<? extends T> nodes) {
        for (T node : nodes) {
            addLast(node);
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }


    public void addFirst(T node) {
        if (set.add(node)) {
            queue.addFirst(node);
        }
    }

    public void addLast(T node) {
        if (set.add(node)) {
            queue.addLast(node);
        }
    }

    public T poll() {
        T result = queue.poll();
        if (result != null) {
            set.remove(result);
        }
        return result;
    }

    public int size() {
        return queue.size();
    }

    public void addAll(Collection<? extends T> elems) {
        for (T elem : elems) {
            addLast(elem);
        }
    }
}
