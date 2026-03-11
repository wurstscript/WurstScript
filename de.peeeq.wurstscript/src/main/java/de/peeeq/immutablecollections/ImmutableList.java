package de.peeeq.immutablecollections;

import io.vavr.collection.List;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.Objects;

/**
 * Lightweight wrapper around {@link List} to keep the existing API surface
 * while using Vavr's efficient persistent list implementation.
 */
public final class ImmutableList<T> implements Iterable<T> {

    private static final ImmutableList<?> EMPTY = new ImmutableList<>(List.empty());

    private final List<T> delegate;

    private ImmutableList(List<T> delegate) {
        this.delegate = delegate;
    }

    /**
     * adds an element to the front of the list
     */
    public ImmutableList<T> appFront(T elem) {
        Objects.requireNonNull(elem, "elem");
        return wrap(delegate.prepend(elem));
    }

    /**
     * adds another ImmutableList to the end
     */
    public <R extends T> ImmutableList<T> cons(ImmutableList<R> other) {
        if (other.isEmpty()) {
            return this;
        }
        return wrap(delegate.appendAll(other.delegate));
    }

    public T head() {
        return delegate.head();
    }

    public ImmutableList<T> tail() {
        return wrap(delegate.tail());
    }

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean contains(T elem) {
        for (T t : this) {
            if (Objects.equals(t, elem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return an empty list
     */
    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> emptyList() {
        return (ImmutableList<T>) EMPTY;
    }

    @SafeVarargs
    public static <T> ImmutableList<T> of(T... elems) {
        List<T> list = List.empty();
        for (int i = elems.length - 1; i >= 0; i--) {
            T elem = Objects.requireNonNull(elems[i], "Elem " + i + " is null.");
            list = list.prepend(elem);
        }
        return wrap(list);
    }

    @Override
    public String toString() {
        return delegate.mkString("List(", ", ", ")");
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ImmutableList<?> other) {
            return delegate.equals(other.delegate);
        }
        return false;
    }

    public ImmutableList<T> appBack(T elem) {
        Objects.requireNonNull(elem, "elem");
        return wrap(delegate.append(elem));
    }

    public ImmutableList<T> removeAll(T t) {
        List<T> filtered = delegate.filter(e -> !Objects.equals(e, t));
        if (filtered == delegate) {
            return this;
        }
        return wrap(filtered);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    private static <T> ImmutableList<T> wrap(List<T> list) {
        if (list.isEmpty()) {
            return emptyList();
        }
        return new ImmutableList<>(list);
    }
}
