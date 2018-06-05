package de.peeeq.datastructures;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Compute something lazily.
 *
 * At the moment, this is used to avoid cyclic dependencies.
 * This should be improved with a better design.
 */
public class Deferred<T> {
    private Supplier<T> supplier;
    private T value;

    public Deferred(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    public Deferred(T value) {
        this.value = value;
    }

    public T get() {
        if (supplier != null) {
            value = supplier.get();
            supplier = null;
        }
        return value;
    }
}
