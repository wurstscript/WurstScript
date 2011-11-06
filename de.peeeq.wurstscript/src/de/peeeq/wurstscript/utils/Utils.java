package de.peeeq.wurstscript.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.WPackage;

public class Utils {

	public static void printIndent(StringBuilder sb, int indent) {
		for (int i=0; i<indent; i++) {
			sb.append("\t");
		}
	}

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

	//	public static void visitPostOrder(SortPos p, Function<SortPos, Void> func) {
	//		p = p.postOrderStart();
	//		while (p != null) {
	//			func.apply(p);
	//			p = p.postOrder();
	//		}
	//	}



	public static <T> void printSep(StringBuilder sb, String seperator, T[] args, Function<T, String> function) {
		for (int i=0; i < args.length; i++) {
			if (i > 0) {
				sb.append(seperator);
			}
			sb.append(function.apply(args[i]));
		}
	}


	public static <T> void printSep(StringBuilder sb, String seperator, T[] args) {
		for (int i=0; i < args.length; i++) {
			if (i > 0) {
				sb.append(seperator);
			}
			sb.append(args[i].toString());
		}
	}

	public static <T> void printSep(StringBuilder sb, String seperator, Iterable<T> params, Function<T, String> function) {
		boolean first = true;
		for (T t : params) {
			if (!first) {
				sb.append(seperator);
			}
			sb.append(function.apply(t));
			first = false;
		}
	}

	public static int parseInt(String yytext) {
		if (yytext.startsWith("0")) {
			return Integer.parseInt(yytext, 8);
		} else {
			return Integer.parseInt(yytext);
		}
	}

	public static int parseAsciiInt1(String yytext) {
		return yytext.charAt(1);
	}

	/**
	 * parse an integer like 'Hfoo'
	 */
	public static int parseAsciiInt4(String yytext) {
		int result = 0;
		int power = 1;
		for (int i=4; i>0; i--) {
			result += yytext.charAt(i)*power;
			power*=256;
		}
		return result;		
	}

	public static int parseHexInt(String yytext) {
		return Integer.parseInt(yytext.substring(2), 16);
	}

	public static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
		}
	}

	public static String printSep(String sep, String[] args) {
		StringBuilder sb = new StringBuilder();
		printSep(sb , sep, args);
		return sb.toString();		
	}

	/**
	 *	is a piece of code jass code? 
	 */
	public static boolean isJassCode(AstElement pos) {
		while (pos != null) {
			if (pos instanceof WPackage) {
				return false; // code is inside package -> pscript code 
			}
			pos = pos.getParent();
		}
		return true; // no package found -> jass code
	}

	
	public static <T extends AstElement> List<T> collect(Class<T> t, AstElement pos) {
		List<T> result = new LinkedList<T>();
		collectRec(t, pos, result);
		return result ;
	}
	
	@SuppressWarnings("unchecked")
	static <T extends AstElement> void collectRec(Class<T> t, AstElement pos, List<T> result) {
		if (t.isInstance(pos)) {
			result.add((T) pos);
		}
		for (int i = 0; i < pos.size(); i++) {
			collectRec(t, pos.get(i), result);
		}
	}

	public static <T>  T[] array(T ... ar) {
		return ar;
	}

	public static int[] array(int ... ar) {
		return ar;
	}

	public static <T> String join(Iterable<T> hints, String seperator) {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (T s : hints) {
			if (!first) {
				result.append(seperator);
			}
			result.append(s);
			first = false;
		}
		return result.toString();
	}

	public static <T> String join(T[] arguments, String seperator) {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (T s : arguments) {
			result.append(s);
			if (!first) {
				result.append(seperator);
			}
			first = false;
		}
		return result.toString();
	}


	/**
	 * sorts a list with partitial ordering topologically.
	 * If a > b then a will appear before b in the result list
	 * @param items items to sort
	 * @param biggerItems a function to get all the bigger items for a given item
	 * @return a sorted list
	 * @throws TopsortCycleException if there exist items a,b so that a > b and b > a 
	 */
	public static <T> List<T> topSort(Collection<T> items, Function<T, Collection<T>> biggerItems) throws TopsortCycleException {
		Set<T> visitedItems = new HashSet<T>();
		List<T> result = new ArrayList<T>(items.size());
		LinkedList<T> activeItems = new LinkedList<T>();
		for (T t : items) {
			if (t == null) throw new IllegalArgumentException();
			topSortHelper(result, visitedItems, activeItems, biggerItems, t);
		}
		return result;
	}

	private static <T> void topSortHelper(List<T> result, Set<T> visitedItems, LinkedList<T> activeItems, Function<T, Collection<T>> biggerItems, T item) throws TopsortCycleException {
		if (visitedItems.contains(item)) {
			return;
		}
		if (activeItems.contains(item)) { // This is not constant time, could be more efficient
			while (activeItems.get(0) != item) {
				activeItems.remove(0);
			}
			throw new TopsortCycleException(activeItems);
		}
		activeItems.add(item);
		visitedItems.add(item);
		System.out.println("visit " + item);
		for (T t : biggerItems.apply(item)) {
			System.out.println("	-> " + t);
			if (t == null) throw new IllegalArgumentException();
			topSortHelper(result, visitedItems, activeItems, biggerItems, t);
		}
		System.out.println("add " + item);
		result.add(item);
		activeItems.removeLast();
	}

	public static <T> boolean oneOf(T obj, T ... ts ) {
		for (T t : ts) {
			if (t.equals(obj)) {
				return true;
			}
		}
		return false;
	}

	public static AstElement getRoot(AstElement i) {
		AstElement s = i;
		while (s.getParent() != null) {
			s = s.getParent();
		}
		return s;
	}





}
