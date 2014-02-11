package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;

public class UsedPackages {

	public static Collection<WPackage> usedPackages(AstElement e) {
		if (e.size() == 0) {
			return Collections.emptyList();
		}
		
		Set<WPackage> result = null;
		return processChildren(e, result);
	}

	private static Collection<WPackage> processChildren(AstElement e,
			Set<WPackage> result) {
		for (int i=0; i<e.size(); i++) {
			Collection<WPackage> used = e.get(i).attrUsedPackages();
			if (!used.isEmpty()) {
				if (result == null) {
					result = Sets.newLinkedHashSet();
				}
				result.addAll(used);
			}
		}
		return result;
	}
	
	public static Collection<WPackage> usedPackages(NameRef e) {
		Set<WPackage> result = Sets.newLinkedHashSet();
		NameDef def = e.attrNameDef();
		if (def instanceof VarDef) {
			if (def.attrNearestPackage() instanceof WPackage) {
				result.add((WPackage) e.attrNearestPackage());
			}
		}
		return processChildren(e, result);
	}
	
	

}
