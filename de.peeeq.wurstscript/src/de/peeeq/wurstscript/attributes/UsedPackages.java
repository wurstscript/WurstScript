package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;

public class UsedPackages {

	public static ImmutableCollection<WPackage> usedPackages(AstElement e) {
		if (e.size() == 0) {
			return ImmutableList.of();
		}
		
		ImmutableSet.Builder<WPackage> result = ImmutableSet.builder();
		processChildren(e, result);
		return result.build();
	}

	private static void processChildren(AstElement e,
			ImmutableSet.Builder<WPackage> result) {
		for (int i=0; i<e.size(); i++) {
			ImmutableCollection<WPackage> used = e.get(i).attrUsedPackages();
			if (!used.isEmpty()) {
				result.addAll(used);
			}
		}
	}
	
	public static ImmutableCollection<WPackage> usedPackages(NameRef e) {
		ImmutableSet.Builder<WPackage> result = ImmutableSet.builder();
		NameDef def = e.attrNameDef();
		if (def instanceof VarDef) {
			if (def.attrNearestPackage() instanceof WPackage) {
				result.add((WPackage) e.attrNearestPackage());
			}
		}
		processChildren(e, result);
		return result.build();
	}
	
	

}
