package de.peeeq.immutablecollections;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ImmutableList<T> implements Iterable<T> {
	
	/**
	 * adds an element to the front of the list 
	 */
	abstract public ImmutableList<T> appFront(T elem);
	
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
	
	public static <T> ImmutableList<T> emptyList() {
		return ImmutableListEmpty.<T>instance();
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

}


class ImmutableListImpl<T> extends ImmutableList<T> {

	private static class MyIterator<T> implements Iterator<T> {
		private ImmutableList<T> pos;

		public MyIterator(ImmutableListImpl<T> list) {
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
		return new ImmutableListImpl.MyIterator<T>(this);
	}

	@Override
	public ImmutableList<T> appFront(T elem) {
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

}
