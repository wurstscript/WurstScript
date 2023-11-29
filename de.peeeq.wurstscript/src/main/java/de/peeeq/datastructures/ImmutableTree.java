package de.peeeq.datastructures;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collections;
import java.util.Iterator;

public class ImmutableTree<T> implements Iterable<ImmutableTree<T>> {

    // invariant: children == null <--> elem != null
    private final @Nullable ImmutableList<ImmutableTree<T>> children;
    private final @Nullable T elem;

    private ImmutableTree(@Nullable ImmutableList<ImmutableTree<T>> children, @Nullable T elem) {
        Preconditions.checkArgument((children == null) != (elem == null));
        this.children = children;
        this.elem = elem;
    }

    public static <T> ImmutableTree<T> node(ImmutableList<ImmutableTree<T>> children) {
        return new ImmutableTree<>(children, null);
    }

    public static <T> ImmutableTree<T> leaf(T t) {
        return new ImmutableTree<>(null, t);
    }

    public int size() {
        ImmutableList<ImmutableTree<T>> ch = children;
        if (ch == null) {
            return 1;
        } else {
            int size = 0;
            for (ImmutableTree<T> c : ch) {
                size += c.size();
            }
            return size;
        }
    }


    @Override
    public Iterator<ImmutableTree<T>> iterator() {
        final ImmutableList<ImmutableTree<T>> children2 = children;
        if (children2 != null) {
            return children2.iterator();
        } else {
            return Collections.emptyIterator();
        }
    }

    public ImmutableList<T> allValues() {
        Builder<T> b = ImmutableList.builder();
        addValues(b);
        return b.build();
    }

    private void addValues(Builder<@Nullable T> b) {
        ImmutableList<ImmutableTree<T>> ch = children;
        if (ch == null) {
            b.add(elem);
        } else {
            for (ImmutableTree<T> c : ch) {
                c.addValues(b);
            }
        }
    }

    public static <T> ImmutableTree<T> empty() {
        return node(ImmutableList.of());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        ImmutableList<ImmutableTree<T>> c = children;
        @Nullable T e = elem;
        result = prime * result + ((c == null) ? 0 : c.hashCode());
        result = prime * result + ((e == null) ? 0 : e.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImmutableTree<?> other = (ImmutableTree<?>) obj;
        ImmutableList<ImmutableTree<T>> c = children;
        if (c == null) {
            if (other.children != null)
                return false;
        } else if (!c.equals(other.children))
            return false;
        @Nullable T e = elem;
        if (e == null) {
            return other.elem == null;
        } else return e.equals(other.elem);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ImmutableList<ImmutableTree<T>> c = children;
        if (c == null) {
            sb.append("[");
            sb.append(elem);
            sb.append("]");
        } else {
            sb.append("[");
            boolean first = true;
            for (ImmutableTree<T> t : c) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(t);
                first = false;
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public T getOnlyEment() {
        ImmutableList<ImmutableTree<T>> ch = children;
        @Nullable T el = elem;
        if (el != null) {
            return el;
        } else if (ch != null && !ch.isEmpty()) {
            return ch.get(0).getOnlyEment();
        }
        throw new RuntimeException("There are " + size() + " elements in this tree.");
    }

    public boolean isLeaf() {
        return children == null;
    }


}
