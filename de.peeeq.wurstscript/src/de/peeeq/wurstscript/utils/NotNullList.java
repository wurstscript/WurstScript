package de.peeeq.wurstscript.utils;

import java.util.Collection;
import java.util.LinkedList;

/**
 * a simple linked list which cannot have null elements in it 
 */
public class NotNullList<T> extends LinkedList<T> {
	private static final long serialVersionUID = -2435064420633050305L;

	@Override
	public boolean add(T e) {
		if (e==null) throw new IllegalArgumentException("E must not be null");
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
	public void addFirst(T e) {
		if (e == null) throw new IllegalArgumentException("Tried to insert null element"); 
		super.addFirst(e);
	}

	@Override
	public void addLast(T e) {
		if (e == null) throw new IllegalArgumentException("Tried to insert null element"); 
		super.addLast(e);
	}

	@Override
	public T set(int index, T element) {
		if (element == null) throw new IllegalArgumentException("Tried to insert null element"); 
		return super.set(index, element);
	}
	
}
