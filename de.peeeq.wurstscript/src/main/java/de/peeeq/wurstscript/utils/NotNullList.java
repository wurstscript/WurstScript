package de.peeeq.wurstscript.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An array list that rejects null elements on insertion and replacement.
 */
public class NotNullList<T> extends ArrayList<T> {
    private static final long serialVersionUID = -2435064420633050305L;

    @Override
    public boolean add(T e) {
        if (e == null) throw new IllegalArgumentException("E must not be null");
        return super.add(e);
    }

    @Override
    public void add(int index, T element) {
        if (element == null) throw new IllegalArgumentException("Tried to insert null element");
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            if (t == null) throw new IllegalArgumentException("Tried to insert null element");
        }
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        for (T t : c) {
            if (t == null) throw new IllegalArgumentException("Tried to insert null element");
        }
        return super.addAll(index, c);
    }


    @Override
    public T set(int index, T element) {
        if (element == null) throw new IllegalArgumentException("Tried to insert null element");
        return super.set(index, element);
    }

}
