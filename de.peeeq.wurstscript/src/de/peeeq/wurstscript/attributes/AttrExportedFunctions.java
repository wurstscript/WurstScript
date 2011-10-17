package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.FuncDefPos;
import de.peeeq.wurstscript.ast.FunctionDefinitionPos;
import de.peeeq.wurstscript.ast.VisibilityPublicPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WPackagePos;

public class AttrExportedFunctions extends Attribute<WPackagePos, Multimap<String, FunctionDefinitionPos>> {

	public AttrExportedFunctions(Attributes attr) {
		super(attr);
	}

	@Override
	protected Multimap<String, FunctionDefinitionPos> calculate(WPackagePos node) {
		final Multimap<String, FunctionDefinitionPos> result = ArrayListMultimap.create();
		for (WEntityPos x : node.elements()) {
			if (x instanceof FuncDefPos) {
				FuncDefPos f = (FuncDefPos) x;
				if (f.visibility() instanceof VisibilityPublicPos) {
					result.put(f.signature().name().term(), f);
				}
				
			}
		}
		return result;
	}


}
