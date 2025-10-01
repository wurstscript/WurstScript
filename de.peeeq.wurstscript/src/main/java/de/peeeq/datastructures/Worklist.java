package de.peeeq.datastructures;

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Collection;

/**
 * An optimized worklist that uses fastutil collections to reduce overhead and allocations.
 */
public class Worklist<T> {
    private final ObjectArrayFIFOQueue<T> queue = new ObjectArrayFIFOQueue<>();
    private final ObjectOpenHashSet<T> set = new ObjectOpenHashSet<>();

    public Worklist() {
    }

    public Worklist(Iterable<? extends T> nodes) {
        for (T node : nodes) {
            add(node);
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void add(T node) {
        if (set.add(node)) {
            queue.enqueue(node);
        }
    }

    public T poll() {
        T result = queue.dequeue();
        // The element is removed from the queue but must also be removed from the set
        // so it can be added again later in the process.
        set.remove(result);
        return result;
    }

    public int size() {
        return queue.size();
    }

    public void addAll(Collection<? extends T> elems) {
        for (T elem : elems) {
            add(elem);
        }
    }
}
