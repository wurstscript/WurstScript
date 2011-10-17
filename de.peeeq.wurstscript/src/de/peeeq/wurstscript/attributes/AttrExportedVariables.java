package de.peeeq.wurstscript.attributes;

import java.util.HashMap;
import java.util.Map;

import de.peeeq.wurstscript.ast.GlobalVarDefPos;
import de.peeeq.wurstscript.ast.VarDefPos;
import de.peeeq.wurstscript.ast.VisibilityPublicPos;
import de.peeeq.wurstscript.ast.WEntityPos;
import de.peeeq.wurstscript.ast.WPackagePos;

public class AttrExportedVariables extends Attribute<WPackagePos, Map<String, VarDefPos>> {

	public AttrExportedVariables(Attributes attr) {
		super(attr);
	}

	@Override
	protected Map<String, VarDefPos> calculate(WPackagePos node) {
		final Map<String, VarDefPos> result = new HashMap<String, VarDefPos>();
		for (WEntityPos x : node.elements()) {
			if (x instanceof GlobalVarDefPos) {
				GlobalVarDefPos v = (GlobalVarDefPos) x;
				if (v.visibility() instanceof VisibilityPublicPos) {
					result.put(v.name().term(), v);
				}
				
			}
		}
		return result;
	}


}
