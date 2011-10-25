package de.peeeq.wurstscript.attributes;

import java.util.HashMap;
import java.util.Map;

import de.peeeq.wurstscript.ast.TypeDef;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;

public class AttrExportedTypes {

	public static Map<String, TypeDef> calculate(WPackage node) {
		final Map<String, TypeDef> result = new HashMap<String, TypeDef>();
		for (WEntity x : node.getElements()) {
			if (x instanceof TypeDef) {
				TypeDef v = (TypeDef) x;
				if (v.getVisibility() instanceof VisibilityPublic) {
					result.put(v.getName(), v);
				}
				
			}
		}
		return result;
	}


}
