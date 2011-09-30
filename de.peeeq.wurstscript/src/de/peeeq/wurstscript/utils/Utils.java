package de.peeeq.wurstscript.utils;

import java.util.List;

import javax.xml.bind.helpers.DefaultValidationEventHandler;

import com.google.common.base.Function;

import de.peeeq.wurstscript.ast.CompilationUnitPos;
import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.ast.OpDivRealPos;
import de.peeeq.wurstscript.ast.WPackagePos;

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
	public static boolean isJassCode(SortPos pos) {
		while (pos != null) {
			if (pos instanceof WPackagePos) {
				return false; // code is inside package -> pscript code 
			}
			pos = pos.parent();
		}
		return true; // no package found -> jass code
	}


}
