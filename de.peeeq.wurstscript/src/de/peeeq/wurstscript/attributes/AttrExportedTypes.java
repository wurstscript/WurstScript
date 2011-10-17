package de.peeeq.wurstscript.attributes;

import java.util.HashMap;
import java.util.Map;

import de.peeeq.wurstscript.ast.TypeDefPos;
import de.peeeq.wurstscript.ast.VisibilityPublicPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WPackagePos;

public class AttrExportedTypes extends Attribute<WPackagePos, Map<String, TypeDefPos>> {

	public AttrExportedTypes(Attributes attr) {
		super(attr);
	}

	@Override
	protected Map<String, TypeDefPos> calculate(WPackagePos node) {
		final Map<String, TypeDefPos> result = new HashMap<String, TypeDefPos>();
		for (WEntityPos x : node.elements()) {
			if (x instanceof TypeDefPos) {
				TypeDefPos v = (TypeDefPos) x;
				if (v.visibility() instanceof VisibilityPublicPos) {
					result.put(v.name().term(), v);
				}
				
			}
		}
		return result;
	}


}
