package de.peeeq.wurstscript.attributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassOrModuleOrModuleInstanciation;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NotExtensionFunction;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.PscriptTypeNamedScope;

public class NameResolution {

	
	public static <T extends AstElement> T searchTypedNameGetOne(Class<T> t, String name, AstElement where) {
		List<T> names = searchTypedNameInScope(t, name, where.attrNearestScope());
		if (names.size() == 0) {
			attr.addError(where.attrSource(), "Could not resolve reference to " + name);
			return null;
		} else if (names.size() > 1) {
			attr.addError(where.attrSource(), "Reference to " + name + " is ambiguous.");
		}
		return names.get(0);
	}
	
	public static <T extends AstElement> List<T> searchTypedName(Class<T> t, String name, AstElement where) {
		return searchTypedNameInScope(t, name, where.attrNearestScope());
	}
	
	private static <T extends AstElement> List<T> searchTypedNameInScope(Class<T> t, String name, WScope scope) {
		System.out.println("	searching for "+ name + " in scope " + scope);
		if (scope == null) {
			return Collections.emptyList();
		}
		List<T> result = Lists.newLinkedList();
		Multimap<String, NameDef> names = scope.attrVisibleNamesPrivate();
		System.out.println("		" + names.size() + " names found in this scope");
		for (NameDef n : names.get(name)) {
			System.out.println("			=> " + n);
			if (t.isInstance(n)) {
				System.out.println("				adding");
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
