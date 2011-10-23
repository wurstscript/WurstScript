package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;

public class AttrExportedFunctions extends Attribute<WPackage, Multimap<String, FunctionDefinition>> {

	public AttrExportedFunctions(Attributes attr) {
		super(attr);
	}

	@Override
	protected Multimap<String, FunctionDefinition> calculate(WPackage node) {
		final Multimap<String, FunctionDefinition> result = ArrayListMultimap.create();
		for (WEntity x : node.getElements()) {
			if (x instanceof FuncDef) {
				FuncDef f = (FuncDef) x;
				if (f.getVisibility() instanceof VisibilityPublic) {
					result.put(f.getSignature().getName(), f);
				}
				
			}
		}
		return result;
	}


}
