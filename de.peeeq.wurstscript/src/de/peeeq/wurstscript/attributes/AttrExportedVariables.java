package de.peeeq.wurstscript.attributes;

import java.util.HashMap;
import java.util.Map;

import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;

public class AttrExportedVariables {
	
	public static  Map<String, VarDef> calculate(WPackage node) {
		final Map<String, VarDef> result = new HashMap<String, VarDef>();
		for (WEntity x : node.getElements()) {
			if (x instanceof GlobalVarDef) {
				GlobalVarDef v = (GlobalVarDef) x;
				if (v.getVisibility() instanceof VisibilityPublic) {
					result.put(v.getName(), v);
				}
				
			}
		}
		return result;
	}


}
