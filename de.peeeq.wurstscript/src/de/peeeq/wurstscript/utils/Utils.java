package de.peeeq.wurstscript.utils;

import java.awt.Rectangle;
import java.awt.Window;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithName;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.LocalVarDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.OnDestroyDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeExprSimple;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.attributes.names.NameLink;

public class Utils {

	@SuppressWarnings("rawtypes")
	public static int size(Iterable<?> i) {
		if (i instanceof Collection) {
			return ((Collection) i).size();
		}
		int size = 0;
		for (@SuppressWarnings("unused") Object o : i) {
			size++;
		}
		return size;
	}
	
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

	public static <T> List<T> removedDuplicates(List<T> list) {
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
		System.out.println(yytext);
		System.out.println(yytext.substring(2));
		return (int) Long.parseLong(yytext.substring(2), 16);
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
		List<T> result = Lists.newArrayList();
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
			if (!first) {
				result.append(seperator);
			}
			result.append(s);
			first = false;
		}
		return result.toString();
	}
	
	
	public static <S, T> List<T> map(List<S> items, Function<S, T> function) {
		List<T> result = new ArrayList<T>(items.size());
		for (S s : items) {
			result.add(function.apply(s));
		}
		return result;
	}

	/**
	 * sorts a list with partitial ordering topologically.
	 * If a > b then a will appear before b in the result list
	 * @param items items to sort
	 * @param biggerItems a function to get all the bigger items for a given item
	 * @return a sorted list
	 * @throws TopsortCycleException if there exist items a,b so that a > b and b > a 
	 */
	public static <T> List<T> topSort(Collection<T> items, Function<T, ? extends Collection<T>> biggerItems) throws TopsortCycleException {
		Set<T> visitedItems = new HashSet<T>();
		List<T> result = new ArrayList<T>(items.size());
		LinkedList<T> activeItems = Lists.newLinkedList();
		for (T t : items) {
			if (t == null) throw new IllegalArgumentException();
			topSortHelper(result, visitedItems, activeItems, biggerItems, t);
		}
		return result;
	}
	
	/**
	 * sorts a list with partitial ordering topologically.
	 * If a > b then a will appear before b in the result list
	 * @param items items to sort
	 * @param biggerItems a multimap to get all the bigger items for a given item
	 * @return a sorted list
	 * @throws TopsortCycleException if there exist items a,b so that a > b and b > a 
	 */
	public static <T> List<T> topSort(Collection<T> items, final Multimap<T, T> biggerItems) throws TopsortCycleException {
		return topSort(items, new Function<T, Collection<T>>() {

			@Override
			public Collection<T> apply(T input) {
				return biggerItems.get(input);
			}
			
		});
	}
	

	private static <T> void topSortHelper(List<T> result, Set<T> visitedItems, LinkedList<T> activeItems, Function<T, ? extends Collection<T>> biggerItems, T item) throws TopsortCycleException {
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
		for (T t : biggerItems.apply(item)) {
			if (t == null) throw new IllegalArgumentException();
			topSortHelper(result, visitedItems, activeItems, biggerItems, t);
		}
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

	public static String printContext(ImmutableList<ClassOrModule> context) {
		return join(map(context, new Function<ClassOrModule, String>() {

			@Override
			public String apply(ClassOrModule c) {
				return c.getName();
			}
			
		}), "->");
	}

	public static <T> T getFirst(Iterable<T> ts) {
		for (T t : ts) {
			return t;
		}
		throw new Error("collection has no first element");
	}

	public static int getCommonPrefixLength(ImmutableList<?> list1, ImmutableList<?> list2) {
		if (list1.isEmpty() || list2.isEmpty()) {
			return 0;
		}
		if (list1.head().equals(list2.head())) {
			return 1 + getCommonPrefixLength(list1.tail(), list2.tail());
		} else {
			return 0;
		}
	}

	  /**
	   * Get the method name for a depth in call stack. <br />
	   * Utility function
	   * @param depth depth in the call stack (0 means current method, 1 means call method, ...)
	   * @return method name
	   */
	  public static String getMethodName(final int depth)
	  {
	    StackTraceElement[] ste = Thread.currentThread().getStackTrace();
	    int i = 0;
	    for (StackTraceElement s : ste) {
//	    	System.out.println("Trace " +i+++ " = " + s.getMethodName());
	    }
	    
	    return ste[depth+2].getMethodName(); 
	  }

	public static <T> List<T> topSortIgnoreCycles(Collection<T> input,
			final Multimap<T, T> biggerItems) {
		return topSortIgnoreCycles(input, new Function<T, Collection<T>>() {

			@Override
			public Collection<T> apply(T t) {
				return biggerItems.get(t);
			}
		});
	}

	public static <T> List<T> topSortIgnoreCycles(Collection<T> items,
			Function<T, Collection<T>> biggerItems) {
		Set<T> visitedItems = new HashSet<T>();
		List<T> result = new ArrayList<T>(items.size());
		for (T t : items) {
			if (t == null) throw new IllegalArgumentException();
			topSortHelperIgnoreCycles(result, visitedItems, biggerItems, t);
		}
		return result;
	}
	

	private static <T> void topSortHelperIgnoreCycles(List<T> result, Set<T> visitedItems, Function<T, ? extends Collection<T>> biggerItems, T item) {
		if (visitedItems.contains(item)) {
			return;
		}
		visitedItems.add(item);
		for (T t : biggerItems.apply(item)) {
			if (t == null) throw new IllegalArgumentException();
			topSortHelperIgnoreCycles(result, visitedItems, biggerItems, t);
		}
		result.add(item);
	}

	public static void saveToFile(Object object, String filename) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(object);
			out.close();
		} catch (IOException e) {
			WLogger.info(e);
		}

	}

	
	public static Object loadFromFile(String filename) {
		FileInputStream fos = null;
		ObjectInputStream out = null;
		Object obj = null;
		try {
			fos = new FileInputStream(filename);
			out = new ObjectInputStream(fos);
			obj = out.readObject();
			out.close();
		} catch (IOException e) {
			WLogger.info(e);
		} catch (ClassNotFoundException e) {
			WLogger.info(e);
		}
		return obj;
	}

	public static void setWindowToCenterOfScreen(Window frm) {
        Rectangle screenBounds = frm.getGraphicsConfiguration().getBounds();
        
        int center_x = screenBounds.x + screenBounds.width / 2;
        int center_y = screenBounds.y + screenBounds.height / 2;
        
        frm.setLocation(center_x - frm.getWidth()/2, center_y - frm.getHeight()/2);
	}

	public static<K,V> Map<V, K> filterByType(
			Class<? extends K> type, Map<V, ?> map) {
		Map<V, K> result = Maps.newHashMap();
		for (Entry<V, ?> e : map.entrySet()) {
			if (type.isInstance(e.getValue())) {
				result.put(e.getKey(), (K) e.getValue());
			}
		}
		return result;
	}


	public static <T extends AstElement> T findParentOfType(Class<T> class1, AstElement node) {
		while (node != null) {
			if (class1.isInstance(node)) {
				return (T) node;
			}
			node = node.getParent();
		}
		return null;
	}

	public static String printScope(WScope scope) {
		if (scope == null) {
			return "null-scope";
		} else if (scope instanceof AstElementWithName) {
			AstElementWithName wn = (AstElementWithName) scope;
			return wn.getName() + " (" + scope.getClass().getName() +")";
		} else {
			return "scope (" + scope.getClass().getName() +")";
		}
	}

	public static String printElement(AstElement e) {
		if (e == null) {
			return "null";
		}
		String type = makeReadableTypeName(e);
		String name = "";
		if (e instanceof ExprFunctionCall) {
			ExprFunctionCall fc = (ExprFunctionCall) e;
			return "function call " +fc.getFuncName() + "()";
		} else if (e instanceof FuncDef) {
			FuncDef fd = (FuncDef) e;
			return "function " + fd.getName();
		} else if (e instanceof OnDestroyDef) {
			return "destroy function for " + e.attrNearestStructureDef().getName();
		} else if (e instanceof ConstructorDef) {
			return "constructor for " + e.attrNearestStructureDef().getName();
		} else if (e instanceof LocalVarDef) {
			LocalVarDef l = (LocalVarDef) e;
			return "local variable " + l.getName();
		} else if (e instanceof VarDef) {
			VarDef l = (VarDef) e;
			return "variable " + l.getName();
		} else if (e instanceof AstElementWithName) {
			name = ((AstElementWithName) e).getName();
		} else if (e instanceof TypeExprSimple) {
			TypeExprSimple t = (TypeExprSimple) e;
			name = t.getTypeName();
			if (t.getTypeArgs().size() > 0) {
				name += "{";
				boolean first = true;
				for (TypeExpr ta : t.getTypeArgs()) {
					if (!first) {
						name += ", ";
					}
					name += printElement(ta);
					first = false;
				}
				name += "}";
			}
		}
		return type + " " + name;
	}

	private static String makeReadableTypeName(AstElement e) {
		String type = e.getClass().getSimpleName()
				.replaceAll("Impl$", "")
				.replaceAll("Def$", "")
				.toLowerCase();
		if (type.equals("wpackage")) {
			type = "package";
		}
		return type;
	}

	public static int inBorders(int min, int x, int max) {
		return Math.max(min, Math.min(max, x));
	}

	public static String printStackTrace(StackTraceElement[] stackTrace) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement s : stackTrace) {
			sb.append(s.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public static <T> T[] conc(List<T> ts, T t) {
		ArrayList<T> temp = Lists.newArrayList(ts);
		temp.add(t);
		return (T[]) temp.toArray();
	}

	public static <T> void addAll(List<T> result, T[] elements) {
		for (T t: elements) {
			result.add(t);
		}
	}

	public static <T> List<T> slice(List<T> ts, int firstIndex, int count) {
		List<T> result = Lists.newArrayListWithCapacity(count);
		for (int i=0; i<count; i++) {
			result.add(ts.get(firstIndex+i));
		}
		return result ;
	}

	public static String printElementQualified(AstElementWithName e) {
		String name = e.getName();
		AstElement node = e.getParent();
		while (node != null) {
			if (node instanceof AstElementWithName) {
				AstElementWithName n = (AstElementWithName) node;
				name = n.getName() + "." + name;
			}
			node = node.getParent();
		}
		return name;
	}
	
	/**
	 * calculates the transient closure of a multimap 
	 */
	public static <T> Multimap<T, T> transientClosure(Multimap<T, T> start) {
		Multimap<T, T> result = HashMultimap.create();
		result.putAll(start);
		
		boolean changed;
		do {
			Multimap<T, T> changes = HashMultimap.create();
			
			for (Entry<T, T> e1 : result.entries()) {
				for (T t : result.get(e1.getValue())) {
					changes.put(e1.getKey(), t);
				}
			}
			changed = result.putAll(changes);
			
		} while (changed);
		
		return result;
	}

	public static AstElement getAstElementAtPos(AstElement elem, int caretPosition) {
		List<AstElement> betterResults = Lists.newArrayList();
		for (int i=0; i < elem.size(); i++) {
			AstElement e = elem.get(i);
			if (elementContainsPos(e, caretPosition)) {
				betterResults.add(getAstElementAtPos(e, caretPosition));
			}
		}
		if (betterResults.size() == 0) {
			return elem;
		} else {
			return bestResult(betterResults);
		}
	}

	/**
	 * return the element with the smallest size 
	 */
	private static AstElement bestResult(List<AstElement> betterResults) {
		int minSize = Integer.MAX_VALUE;
		AstElement min = null;
		for (AstElement e : betterResults) {
			int size = e.attrSource().getRightPos() - e.attrSource().getLeftPos();
			if (size < minSize) {
				minSize = size;
				min = e;
			}
		}
		return min;
	}


	public static boolean elementContainsPos(AstElement e, int pos) {
		return e.attrSource().getLeftPos() <= pos && e.attrSource().getRightPos() >= pos;
	}

	public static <T extends AstElementWithName> List<T> sortByName(Collection<T> c) {
		List<T> r = Lists.newArrayList(c);
		Collections.sort(r, new Comparator<T>() {

			@Override
			public int compare(T o1, T o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return r;
	}

	public static String printPos(WPos source) {
		return source.getFile() + ", line " + source.getLine();
	}

	public static boolean isEmptyCU(CompilationUnit cu) {
		return (cu == null) 
		  || (cu.getJassDecls().size() + cu.getPackages().size() == 0);
	}

	public static <T> T[] copyArray(T[] ar) {
		@SuppressWarnings("unchecked")
		T[] r = (T[]) Array.newInstance(ar.getClass(), ar.length);
		System.arraycopy(ar, 0, r, 0, ar.length);
		return r;
	}

	public static String printElementWithSource(AstElement e) {
		WPos src = e.attrSource();
		return printElement(e) + " (" + src.getFile() + ", line " + src.getLine() +")";
	}

	public static int[] copyArray(int[] ar) {
		int[] r = new int[ar.length];
		System.arraycopy(ar, 0, r, 0, ar.length);
		return r;
	}

	public static String toFirstUpper(String s) {
		if (s.isEmpty()) {
			return s;
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	@SuppressWarnings("unchecked")
	public static <T extends AstElement> T getParentOfType(Class<T> class1, AstElement node) {
		while (node != null) {
			if (class1.isAssignableFrom(node.getClass())) {
				return (T) node;
			}
			node = node.getParent();
		}
		return null;
	}
	
	public static String printAlternatives(Iterable<? extends AstElement> alternatives) {
		List<String> result = Lists.newArrayList();
		for (AstElement a : alternatives) {
			WPos source = a.attrSource();
			String s = Utils.printElement(a) + " defined in line " + source.getLine() + " ("+source.getFile()+")" ;
			result.add(s);
		}
		return " * " + Utils.join(result, "\n * ") ;
	}

	public static String printAlternatives(Collection<NameLink> alternatives) {
		List<String> result = Lists.newArrayList();
		for (NameLink a : alternatives) {
			WPos source = a.getNameDef().attrSource();
			String s = Utils.printElement(a.getNameDef()) + " defined in line " + source.getLine() + " ("+source.getFile()+")" ;
			result.add(s);
		}
		return " * " + Utils.join(result, "\n * ") ;
	}

	public static <T, S> Multimap<T, S> inverse(Multimap<S, T> orig) {
		Multimap<T, S> result = HashMultimap.create();
		for (Entry<S, T> e : orig.entries()) {
			result.put(e.getValue(), e.getKey());
		}
		return result;
	}

	public static boolean visitRec(AstElement e, Predicate<AstElement> f) {
		if (!f.apply(e)) {
			return false;
		}
		for (int i=0; i<e.size(); i++) {
			boolean r = visitRec(e.get(i), f);
			if (!r) {
				return false;
			}
		}
		return true;
	}

}
