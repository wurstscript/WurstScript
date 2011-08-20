package de.peeeq.wurstscript.attributes;

import java.util.HashMap;
import java.util.Map;

import de.peeeq.wurstscript.ast.TypeDefPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WPackagePos;


/**
 * this attribute calculates the available types for a given WPackage 
 */
public class AttrTypeScope extends Attribute<WPackagePos, Map<String, TypeDefPos>> {


	public AttrTypeScope(Attributes attr) {
		super(attr);
	}

	@Override
	protected Map<String, TypeDefPos> calculate(WPackagePos node) {
		final Map<String, TypeDefPos> result = new HashMap<String, TypeDefPos>();
		for (WEntityPos e : node.elements()) {
			if (e instanceof TypeDefPos) {
				TypeDefPos t = (TypeDefPos) e;
				result.put(t.name().term(), t);
			}
		}
		return result;
	}


}
