package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.SortPos;
import de.peeeq.wurstscript.ast.WScope;


public class Scoping {

	public static WScope getNearestScope(SortPos node) {
		SortPos p = node.getParent();
		while (p != null) {
			if (p instanceof WScope) {
				return (WScope) p;
			}
			p = p.getParent();
		}
		return null;
	}

}
