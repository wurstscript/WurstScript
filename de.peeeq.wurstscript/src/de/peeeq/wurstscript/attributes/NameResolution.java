package de.peeeq.wurstscript.attributes;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithName;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;
import de.peeeq.wurstscript.utils.Utils;

public class NameResolution {

	
	public static <T extends AstElement> T searchTypedNameGetOne(Class<T> t, String name, AstElement where) {
		List<T> names = searchTypedNameInScope(t, name, where.attrNearestScope());
		if (names.size() == 0) {
			attr.addError(where.attrSource(), "Could not resolve reference to " + name);
			return null;
		} else if (names.size() > 1) {
			attr.addError(where.attrSource(), "Reference to " + name + " is ambiguous. Alternatives are:\n" + printAlternatives(names));
		}
		return names.get(0);
	}
	
	private static String printAlternatives(List<? extends AstElement> alternatives) {
		List<String> result = Lists.newLinkedList();
		for (AstElement a : alternatives) {
			WPos source = a.attrSource();
			String s = a.getClass().getSimpleName() + " defined in line " + source.getLine() + " ("+source.getFile()+")" ;
			result.add(s);
		}
		return " * " + Utils.join(result, "\n * ") ;
	}
	
	public static <T extends AstElement> List<T> searchTypedName(Class<T> t, String name, AstElement where) {
		return searchTypedNameInScope(t, name, where.attrNearestScope());
	}
	
	private static <T extends AstElement> List<T> searchTypedNameInScope(Class<T> t, String name, WScope scope) {
		if (scope == null) {
			return Collections.emptyList();
		}
		List<T> result = Lists.newLinkedList();
		Multimap<String, NameDef> names = scope.attrVisibleNamesPrivate();
		for (NameDef n : names.get(name)) {
			if (t.isInstance(n)) {
				@SuppressWarnings("unchecked")
				T n2 = (T) n;
				result.add(n2);
			}
		}
		if (result.size() > 0) {
			return result;
		} else {
			// not found yet, search in next scope
			return searchTypedNameInScope(t, name, scope.attrNextScope());
		}
		
	}
	
	

	public static <T> T getTypedNameFromNamedScope(Class<T> t, Expr context, String name, PscriptTypeNamedScope sr) {
		Multimap<String, NameDef> names;
		if (isSameClass(context, sr)) {
			names = sr.getDef().attrVisibleNamesPrivate();
		} else if (isSamePackage(context, sr)) {
			names = sr.getDef().attrVisibleNamesProtected();
		} else {
			// different package
			names = sr.getDef().attrVisibleNamesPublic();
		}
		for (NameDef n : names.get(name)) {
			if (t.isInstance(n)) {
				@SuppressWarnings("unchecked")
				T n2 = (T) n;
				return n2;
			}
		}
		return null;
	}

	private static boolean isSamePackage(AstElement context, PscriptTypeNamedScope sr) {
		return sr.getDef().attrNearestPackage() == context.attrNearestPackage();
	}

	private static boolean isSameClass(AstElement context, PscriptTypeNamedScope sr) {
		return sr.getDef() == context.attrNearestNamedScope();
	}

}
