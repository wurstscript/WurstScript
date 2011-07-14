package de.peeeq.pscript.PUtil;

import java.util.List;

import de.peeeq.pscript.utils.NotNullList;


public class PUtil {

	/**
	 * creates an array from the given types
	 * @param args
	 * @return
	 */
	public static <T> T[] arrayOf(T ... args) {
		return args;
	}
	
	public static <T> List<T> listOf(T ... args) {
		List<T> result =  new NotNullList<T>();
		for (T t : args) {
			result.add(t);
		}
		return result;
	}

}
