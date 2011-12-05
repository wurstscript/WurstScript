package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;

public class AttrExportedFunctions {

	public static Multimap<String, FunctionDefinition> calculate(WPackage node) {
		final Multimap<String, FunctionDefinition> result = ArrayListMultimap.create();
		for (WEntity x : node.getElements()) {
			if (x instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) x;
				if (f.attrIsPublic()) {
					result.put(f.getSignature().getName(), f);
				}
				
			}
		}
		return result;
	}


}
