package de.peeeq.immutablecollections;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ImmutableList<T> implements Iterable<T> {
	
	/**
	 * adds an element to the front of the list 
	 */
	abstract public ImmutableList<T> appFront(T elem);
	
	/**
	 * adds an other ImmutableList to the end 
	 */
	abstract public ImmutableList<T> cons(ImmutableList<T> other);
	
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
		return ImmutableListEmpty.<T>instance();
	}
	
	public static <T> ImmutableList<T> of(T ... elems) {
		ImmutableList<T> result = emptyList();
		for (int i=elems.length-1; i>=0; i--) {
			result = result.appFront(elems[i]);
		}
		return result;
	}
	
	public static <T> ImmutableList<T> of(Collection<T> elems) {
		T[] ar = (T[]) new Object[elems.size()];
		int i = 0;
		for (T t : elems) {
			ar[i] = t;
			i++;
		}
		return of(ar);
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

}




class ImmutableListEmpty<T> extends ImmutableList<T> {

	private static ImmutableList<Object> instance = new ImmutableListEmpty<Object>();

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
		return new ImmutableListEmpty.MyIterator<T>();
	}

	@Override
	public ImmutableList<T> appFront(T elem) {
		if (elem == null) throw new IllegalArgumentException("elem must not be null");
		return new ImmutableListImpl<T>(elem);
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public T head() {
		return null;
	}

	@Override
	public ImmutableList<T> tail() {
		return null;
	}

	@Override
	public ImmutableList<T> cons(ImmutableList<T> other) {
		// just return other since this list is empty
		return other;
	}

}


class ImmutableListIterator<T> implements Iterator<T> {
	private ImmutableList<T> pos;

	public ImmutableListIterator(ImmutableList<T> list) {
		this.pos = list;
	}

	@Override
	public boolean hasNext() {
		
		return pos != null; // && pos.size() > 0;
	}

	@Override
	public T next() {
		T res = pos.head();
		pos = pos.tail();
		return res;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

class ImmutableListImpl<T> extends ImmutableList<T> {

	

	private T head;
	private ImmutableList<T> tail;
	private int size;

	ImmutableListImpl(T elem) {
		this.head = elem;
		this.tail = null;
		this.size = 1;
	}

	ImmutableListImpl(T head, ImmutableList<T> tail) {
		this.head = head;
		this.tail = tail;
		this.size = 1 + tail.size();
	}
	
	
	@Override
	public Iterator<T> iterator() {
		return new ImmutableListIterator<T>(this);
	}

	@Override
	public ImmutableList<T> appFront(T elem) {
		if (elem == null) throw new IllegalArgumentException("elem must not be null");
		return new ImmutableListImpl<T>(elem, this);
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
	public ImmutableList<T> cons(ImmutableList<T> other) {
		if (other.isEmpty()) {
			return this;
		} else {
			return new ImmutableListImplCons<T>(this, other);
		}
	}

}

class ImmutableListImplCons<T> extends ImmutableList<T> {

	
	
	// invariant: size(left) > 0 && size(right) > 0
	
	private ImmutableList<T> left;
	private ImmutableList<T> right;

	ImmutableListImplCons(ImmutableList<T> left, ImmutableList<T> right) {
		if (left.size() == 0) throw new IllegalArgumentException("left list is empty");
		if (right.size() == 0) throw new IllegalArgumentException("right list is empty");
		this.left = left;
		this.right = right;
	}

	@Override
	public Iterator<T> iterator() {
		return new ImmutableListIterator<T>(this);
	}

	@Override
	public ImmutableList<T> appFront(T elem) {
		if (elem == null) throw new IllegalArgumentException("elem must not be null");
		return new ImmutableListImpl<T>(elem, this);
	}

	@Override
	public ImmutableList<T> cons(ImmutableList<T> other) {
		if (other.isEmpty()) {
			return this;
		} else {
			return new ImmutableListImplCons<T>(this, other);
		}
	}

	@Override
	public T head() {
		return left.head();
	}

	@Override
	public ImmutableList<T> tail() {
		ImmutableList<T> leftTail = left.tail();
		if (leftTail == null || leftTail.isEmpty()) {
			return right;
		}
		return new ImmutableListImplCons<T>(leftTail, right);
	}

	@Override
	public int size() {
		return left.size() + right.size();
	}
	
}
