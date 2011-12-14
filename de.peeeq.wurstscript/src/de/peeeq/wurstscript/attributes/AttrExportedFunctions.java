package de.peeeq.wurstscript.attributes;

import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.NotExtensionFunction;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;

public class AttrExportedFunctions {

	public static Map<String, NotExtensionFunction> calculate(WPackage node) {
		final Map<String, NotExtensionFunction> result = Maps.newHashMap();
		for (WEntity x : node.getElements()) {
			if (x instanceof NotExtensionFunction) {
				NotExtensionFunction f = (NotExtensionFunction) x;
				if (f.attrIsPublic()) {
					result.put(f.getSignature().getName(), f);
				}
				
			}
		}
		return result;
	}

	public static Multimap<String, ExtensionFuncDef> calculateExportedExtensionFunctions(WPackage p) {
		Multimap<String, ExtensionFuncDef> result = HashMultimap.create();
		for (WEntity e : p.getElements()) {
			if (e instanceof ExtensionFuncDef) {
				ExtensionFuncDef ext = (ExtensionFuncDef) e;
				if (ext.attrIsPublic()) {
					result.put(ext.getSignature().getName(), ext);
				}
			}
		}
		return result;
	}


}
