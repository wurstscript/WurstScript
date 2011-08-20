package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.ast.WScopePos;

public class Scoping {

	public static WScopePos getNearestScope(SortPos node) {
		SortPos p = node.parent();
		while (p != null) {
			if (p instanceof WScopePos) {
				return (WScopePos) p;
			}
			p = p.parent();
		}
		return null;
	}

}
