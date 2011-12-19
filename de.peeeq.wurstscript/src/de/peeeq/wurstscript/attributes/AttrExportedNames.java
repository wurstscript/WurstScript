package de.peeeq.wurstscript.attributes;

import java.util.Map;

import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.AstElementWithModifiers;
import de.peeeq.wurstscript.ast.HasModifier;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;

public class AttrExportedNames {

	public static  Map<String, NameDef> calculate(WPackage node) {
		final Map<String, NameDef> result = Maps.newHashMap();
		for (WEntity x : node.getElements()) {
			if (x instanceof NameDef && x instanceof HasModifier) {
				NameDef v = (NameDef) x;
				HasModifier withMod = (HasModifier) v;
				if (withMod.attrIsPublic()) {
					result.put(v.getName(), v);
				}
			}
		}
		return result;
	}


}
