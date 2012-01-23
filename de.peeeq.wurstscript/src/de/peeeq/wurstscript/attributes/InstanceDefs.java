package de.peeeq.wurstscript.attributes;

import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.InstanceDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.utils.Utils;

public class InstanceDefs {

	public static Multimap<InterfaceDef, InstanceDef> calculates(CompilationUnit cu) {
		Multimap<InterfaceDef, InstanceDef> result = HashMultimap.create();
		for (WPackage p  : cu.attrPackages().values()) {
			result.putAll(p.attrInstanceDefs());
		}
		return result;
	}

	public static Multimap<InterfaceDef, InstanceDef> calculates(WPackage p) {
		Multimap<InterfaceDef, InstanceDef> result = HashMultimap.create();
		Set<WPackage> included = Sets.newHashSet();
		addInstanceDefsFromPackage(included, p, result, true);
		return result;
	}

	private static void addInstanceDefsFromPackage(Set<WPackage> included, WPackage p, Multimap<InterfaceDef, InstanceDef> result, boolean useAll) {
		if (p == null || included.contains(p)) {
			return;
		}
		included.add(p);
		for (WEntity e : p.getElements()) {
			if (e instanceof InstanceDef) {
				InstanceDef instanceDef = (InstanceDef) e;
				if (!useAll && !instanceDef.attrIsPublic()) {
					continue;
				}
				TypeDef typeDef = instanceDef.getImplementedTyp().attrTypeDef();
				if (typeDef instanceof InterfaceDef) {
					InterfaceDef interfaceDef = (InterfaceDef) typeDef;
					
					result.put(interfaceDef, instanceDef);
				} else {
					attr.addError(instanceDef.getImplementedTyp().getSource(), "Implemented type " + Utils.printElement(typeDef)  + " is not a interface.");
				}
			}
		}
		// add instances from imported packages
		// because of possible cyclic dependencies, we cannot use the attribute here
		for (WImport imp : p.getImports()) {
			addInstanceDefsFromPackage(included, imp.attrImportedPackage(), result, false);
		}
	}

}
