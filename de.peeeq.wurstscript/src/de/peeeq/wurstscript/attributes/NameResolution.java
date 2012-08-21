package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WScope;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeNamedScope;
import de.peeeq.wurstscript.utils.Utils;

public class NameResolution {

	
	public static <T extends AstElement> T searchTypedNameGetOne(Class<T> t, String name, AstElement where, boolean withInheritance) {
		List<T> names = searchTypedName(t, name, where, withInheritance);
		if (names.size() == 0) {
			where.addError("Could not resolve reference to " + name);
			return null;
		} else if (names.size() > 1) {
			PackageOrGlobal p = where.attrNearestPackage();
			for (T n : names) {
				// check if there is a name from the current package, we always prefer this.
				if (n.attrNearestPackage() == p) {
					return n;
				}
			}
			where.addError("Reference to " + name + " is ambiguous. Alternatives are:\n" + printAlternatives(names));
		}
		return names.get(0);
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
	
	public static <T extends AstElement> List<T> searchTypedName(Class<T> t, String name, AstElement where, boolean withInheritance) {
		if (where == null) throw new IllegalArgumentException();
		return searchTypedNameInScope(t, name, where.attrNearestScope(), withInheritance, Collections.<WScope>emptySet());
	}
	
	
	public static <T extends AstElement> List<T> searchTypedName(Class<T> t, String name, AstElement where, boolean withInheritance, Set<WScope> ignoredScopes) {
		if (where == null) throw new IllegalArgumentException();
		return searchTypedNameInScope(t, name, where.attrNearestScope(), withInheritance, ignoredScopes);
	}
	
	private static <T extends AstElement> List<T> searchTypedNameInScope(Class<T> t, String name, WScope scope, boolean withInheritance, Set<WScope> ignoredScopes) {
		if (scope == null) {
			return Collections.emptyList();
		}
		if (ignoredScopes.contains(scope)) {
			if (scope instanceof ClassDef) {
				ClassDef c = (ClassDef) scope;
				for (TypeParamDef tp : c.getTypeParameters()) {
					if (tp.getName().equals(name)) {
						return Collections.singletonList((T)tp);
					}
				}
				
			}
			
			
			return searchTypedNameInScope(t, name, scope.attrNextScope(), withInheritance, ignoredScopes);
		}
		List<T> result = Lists.newArrayList();
		if (withInheritance && scope instanceof NamedScope) {
			NamedScope namedScope = (NamedScope) scope;
			Collection<T> rs = getTypedNameFromNamedScope(t, scope, name, namedScope);
			result.addAll(rs);
		} else {
			Multimap<String, NameDef> names = scope.attrVisibleNamesPrivate();
			for (NameDef n : names.get(name)) {
				if (t.isInstance(n)) {
					@SuppressWarnings("unchecked")
					T n2 = (T) n;
					result.add(n2);
				}
			}
		}
		if (result.size() > 0) {
			return result;
		} else {
			// not found yet, search in next scope
			return searchTypedNameInScope(t, name, scope.attrNextScope(), withInheritance, ignoredScopes);
		}
		
	}
	
	

	public static <T> Collection<T> getTypedNameFromNamedScope(Class<T> t, Expr context, String name, WurstTypeNamedScope sr) {
		return getTypedNameFromNamedScope(t, context, name, sr.getDef());
	}
	
	private static <T> Collection<T> getTypedNameFromNamedScope(Class<T> t, AstElement context, String name, NamedScope ns) {
		Multimap<String, NameDef> names;
		if (isSameClass(context, ns)) {
			names = ns.attrVisibleNamesPrivate();
		} else if (isSamePackage(context, ns)) {
			names = ns.attrVisibleNamesProtected();
		} else {
			// different package
			names = ns.attrVisibleNamesPublic();
		}
		List<T> result = Lists.newArrayList();
		for (NameDef n : names.get(name)) {
			if (t.isInstance(n)) {
				@SuppressWarnings("unchecked")
				T n2 = (T) n;
				result.add(n2);
			}
		}		
		if (result.size() > 0) {
			return result;
		}
//		// if not found yet, try to find implementation in supertypes:
//		if (ns instanceof ClassDef) {
//			ClassDef c = (ClassDef) ns;
//			// search for impl in extended class
//			if (c.getExtendedClass() instanceof TypeExpr) {
//				TypeExpr typeExpr = (TypeExpr) c.getExtendedClass();
//				WurstType superType = typeExpr.attrTyp();
//				if (superType instanceof WurstTypeNamedScope) {
//					WurstTypeNamedScope superTypeNs = (WurstTypeNamedScope) superType;
//					Collection<T> r = getTypedNameFromNamedScope(t, context, name, superTypeNs.getDef());
//					if (r.size() > 0) {
//						return r;
//					}
//				}
//				
//			}
//			
//			// search for default implementation in interfaces
//			for (WurstTypeInterface i : c.attrImplementedInterfaces()) {
//				Collection<T> r = getTypedNameFromNamedScope(t, context, name, i.getDef());
//				if (r.size() > 0) {
//					return r;
//				}
//			}
//		}
		return Collections.emptyList();
	}

	private static boolean isSamePackage(AstElement context, NamedScope ns) {
		return ns.attrNearestPackage() == context.attrNearestPackage();
	}

	private static boolean isSameClass(AstElement context, NamedScope ns) {
		return ns == context.attrNearestNamedScope();
	}

}
