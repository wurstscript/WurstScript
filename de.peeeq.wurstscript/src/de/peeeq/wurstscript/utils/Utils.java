package de.peeeq.wurstscript.utils;

import java.util.List;

import com.google.common.base.Function;

import de.peeeq.wurstscript.ast.AST.SortPos;

public class Utils {

	public static <T,R> List<R> map(Iterable<T> list, Function<T, R> function) {
		List<R> result = new NotNullList<R>();
		for (T t : list) {
			result.add(function.apply(t));
		}
		return result;
	}

	
	public static <T> List<T> list(T ... args) {
		List<T> result = new NotNullList<T>();
		for (T t : args) {
			result.add(t);
		}
		return result;
	}


	public static <T> List<T> filter(List<T> list,	Function<T, Boolean> filter) {
		if (list == null) throw new IllegalArgumentException("list must not be null");
		if (filter == null) throw new IllegalArgumentException("filter must not be null");
		List<T> result = new NotNullList<T>();
		for (T t : list) {
			if (filter.apply(t)) {
				result.add(t);
			}
		}
		return result;
	}

	public static <T> List<T> removeDuplicates(List<T> list) {
		List<T> result = new NotNullList<T>();
		for (T t : list) {
			if (!result.contains(t)) {
				result.add(t);
			}
		}
		return result;
		
	}
	
	public static void visitPostOrder(SortPos p, Function<SortPos, Void> func) {
		p = p.postOrderStart();
		while (p != null) {
			func.apply(p);
			p = p.postOrder();
		}
	}

}
