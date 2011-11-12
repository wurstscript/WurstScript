package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.WScope;


public class Scoping {

	public static WScope getNearestScope(AstElement node) {
		AstElement p = node.getParent();
		while (p != null) {
			if (p instanceof WScope) {
				return (WScope) p;
			}
			p = p.getParent();
		}
		return null;
	}

}
