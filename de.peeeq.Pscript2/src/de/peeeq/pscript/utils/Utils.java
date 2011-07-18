package de.peeeq.pscript.utils;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Function;

import de.peeeq.immutablecollections.ImmutableList;

public class Utils {

	public static <T,R> List<R> map(Iterable<T> list, Function<T, R> function) {
		List<R> result = new NotNullList<R>();
		for (T t : list) {
			result.add(function.apply(t));
		}
		return result;
	}

	
	@SuppressWarnings("unchecked")
	public static <T> ImmutableList<T> collectRec(EObject target, Class<T> search) {
		if (target == null) throw new IllegalArgumentException("target must not be null");
		
		ImmutableList<T> result = ImmutableList.emptyList();
		if (search.isInstance(target)) {
			result = result.appFront((T) target);
		}
		if (target.eContents() != null) {
			for (EObject child :target.eContents()) {
				result = result.cons(collectRec(child, search));
			}		
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

	@SuppressWarnings("unchecked")
	public static <T> T getParent(EObject obj, Class<? extends T> search) {
		if (obj == null) throw new IllegalArgumentException("obj must not be null");
		
		while (!search.isInstance(obj)) {
			obj = obj.eContainer();
			if (obj == null) return null;
		}
		return (T) obj;
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

}
