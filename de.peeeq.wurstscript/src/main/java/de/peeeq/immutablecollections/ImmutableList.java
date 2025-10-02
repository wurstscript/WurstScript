package de.peeeq.immutablecollections;

import com.google.common.base.Preconditions;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ImmutableList<T> implements Iterable<T> {

    private int hashCode = 0;

    /**
     * adds an element to the front of the list
     */
    abstract public ImmutableList<T> appFront(T elem);

    /**
     * adds another ImmutableList to the end
     */
    abstract public <R extends T> ImmutableList<T> cons(ImmutableList<R> other);

    abstract public T head();

    abstract public ImmutableList<T> tail();


    abstract public int size();

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(T elem) {
        for (T t : this) {
            if (t.equals(elem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return an empty list
     */
    public static <T> ImmutableList<T> emptyList() {
        return ImmutableListEmpty.instance();
    }

    @SafeVarargs
    public static <T> ImmutableList<T> of(T... elems) {
        ImmutableList<T> result = emptyList();
        for (int i = elems.length - 1; i >= 0; i--) {
            T elem = elems[i];
            if (elem == null) {
                throw new IllegalArgumentException("Elem " + i + " is null.");
            }
            result = result.appFront(elem);
        }
        return result;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("List(");
        boolean first = true;
        for (T t : this) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(t);
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            Object[] elements = new Object[size()];
            int i = 0;
            for (T t : this) {
                elements[i] = t;
                i++;
            }
            hashCode = Arrays.hashCode(elements);
        }
        return hashCode;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ImmutableList) {
            ImmutableList<?> other = (ImmutableList<?>) obj;
            if (other.size() != size() || other.hashCode() != hashCode()) {
                return false;
            }
            if (size() == 0) {
                return true;
            }
            if (!other.head().equals(head())) {
                return false;
            }
            if (size() > 1) {
                return other.tail().equals(tail());
            } else {
                return true;
            }
        }
        return false;
    }

    public ImmutableList<T> appBack(T elem) {
        return this.cons(ImmutableList.<T>emptyList().appFront(elem));
    }

    public abstract ImmutableList<T> removeAll(T t);
}


class ImmutableListEmpty<T> extends ImmutableList<T> {

    private final static ImmutableList<Object> instance = new ImmutableListEmpty<>();

    @SuppressWarnings("unchecked")
    static <T> ImmutableList<T> instance() {
        return (ImmutableList<T>) instance;
    }

    private ImmutableListEmpty() {

    }

    private static class MyIterator<T> implements Iterator<T> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableListEmpty.MyIterator<>();
    }

    @Override
    public ImmutableList<T> appFront(T elem) {
        Preconditions.checkNotNull(elem);
        return new ImmutableListImpl<>(elem);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public T head() {
        throw new Error("Tried to get head of empty list");
    }

    @Override
    public ImmutableList<T> tail() {
        throw new Error("Tried to get tail of empty list");
    }


    @Override
    public <R extends T> ImmutableList<T> cons(ImmutableList<R> other) {
        // just return other since this list is empty
        @SuppressWarnings("unchecked") // this is safe because the list is immutable
                ImmutableList<T> result = (ImmutableList<T>) other;
        return result;
    }

    @Override
    public ImmutableList<T> removeAll(T t) {
        return this;
    }

}


class ImmutableListIterator<T> implements Iterator<T> {
    private @Nullable ImmutableList<T> pos;

    public ImmutableListIterator(ImmutableList<T> list) {
        this.pos = list;
    }

    @Override
    public boolean hasNext() {
        ImmutableList<T> p = pos;
        return p != null && !p.isEmpty();
    }

    @Override
    public T next() {
        ImmutableList<T> p = pos;
        if (p == null || p.isEmpty()) {
            pos = null;
            throw new RuntimeException("Calling next on finished iterator.");
        } else {
            T res = p.head();
            pos = p.tail();
            return res;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}

class ImmutableListImpl<T> extends ImmutableList<T> {


    private final T head;
    private final ImmutableList<T> tail;
    private final int size;

    ImmutableListImpl(T elem) {
        this.head = elem;
        this.tail = ImmutableList.emptyList();
        this.size = 1;
    }

    ImmutableListImpl(T head, ImmutableList<T> tail) {
        this.head = head;
        this.tail = tail;
        this.size = 1 + tail.size();
    }


    @Override
    public Iterator<T> iterator() {
        return new ImmutableListIterator<>(this);
    }

    @Override
    public ImmutableList<T> appFront(T elem) {
        Preconditions.checkNotNull(elem);
        return new ImmutableListImpl<>(elem, this);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T head() {
        return head;
    }

    @Override
    public ImmutableList<T> tail() {
        return tail;
    }

    @Override
    public <R extends T> ImmutableList<T> cons(ImmutableList<R> other) {
        if (other.isEmpty()) {
            return this;
        } else {
            return new ImmutableListImplCons<>(this, other);
        }
    }

    @Override
    public ImmutableList<T> removeAll(T t) {
        if (head.equals(t)) {
            return tail.removeAll(t);
        } else {
            return tail.removeAll(t).appFront(head);
        }
    }

}

class ImmutableListImplCons<T, L extends T, R extends T> extends ImmutableList<T> {


    // invariant: size(left) > 0 && size(right) > 0

    private final ImmutableList<L> left;
    private final ImmutableList<R> right;
    private final int size;

    ImmutableListImplCons(ImmutableList<L> left, ImmutableList<R> right) {
        if (left.size() == 0) throw new IllegalArgumentException("left list is empty");
        if (right.size() == 0) throw new IllegalArgumentException("right list is empty");
        this.left = left;
        this.right = right;
        this.size = left.size() + right.size();
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableListIterator<>(this);
    }

    @Override
    public ImmutableList<T> appFront(T elem) {
        Preconditions.checkNotNull(elem);
        return new ImmutableListImpl<>(elem, this);
    }

    @Override
    public <R2 extends T> ImmutableList<T> cons(ImmutableList<R2> other) {
        if (other.isEmpty()) {
            return this;
        } else {
            return new ImmutableListImplCons<>(this, other);
        }
    }

    @Override
    public T head() {
        return left.head();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<T> tail() {
        ImmutableList<T> leftTail = (ImmutableList<T>) left.tail();
        if (leftTail.isEmpty()) {
            return (ImmutableList<T>) right;
        }
        return new ImmutableListImplCons<>(leftTail, right);
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableList<T> removeAll(T t) {
        ImmutableList<L> l = left.removeAll((L) t);
        ImmutableList<R> r = right.removeAll((R) t);
        if (left == l && right == r) {
            return this;
        }
        return ((ImmutableList<T>) l).cons(r);
    }

}
