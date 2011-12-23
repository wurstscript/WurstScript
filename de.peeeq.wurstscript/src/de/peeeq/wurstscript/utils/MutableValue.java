package de.peeeq.wurstscript.utils;

public class MutableValue<T> {
	T t = null;
	
	public MutableValue() {
		
	}
	
	public MutableValue(T t) {
		this.t = t;
	}
	
	
	public void set(T t) {
		this.t = t;
	}
	
	public T get() {
		return t;
	}
	
	public boolean isNull() {
		return t == null;
	}
}
