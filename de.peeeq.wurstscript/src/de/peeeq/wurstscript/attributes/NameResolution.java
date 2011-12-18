package de.peeeq.wurstscript.attributes;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ExprFuncRef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WScope;

public class NameResolution {

	public static <T extends AstElement> List<T> searchTypedName(Class<T> t, String name, AstElement where) {
		return searchTypedNameInScope(t, name, where.attrNearestScope());
	}
	
	public static <T extends AstElement> List<T> searchTypedNameInScope(Class<T> t, String name, WScope scope) {
		if (scope == null) {
			return Collections.emptyList();
		}
		List<T> result = Lists.newLinkedList();
		Multimap<String, NameDef> names = scope.attrVisibleNamesPrivate();
		for (NameDef n : names.get(name)) {
			if (t.isInstance(t)) {
				@SuppressWarnings("unchecked")
				T n2 = (T) n;
				result.add(n2);
			}
		}
		if (result.size() == 0) {
			return searchTypedNameInScope(t, name, scope.attrNextScope());
		}
		return result;
	}

}
