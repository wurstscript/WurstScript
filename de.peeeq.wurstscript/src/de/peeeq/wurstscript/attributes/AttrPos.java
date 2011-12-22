package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.WPos;

public class AttrPos {

	public static WPos getPos(AstElement e) {
		if (e instanceof AstElementWithSource) {
			AstElementWithSource ws = (AstElementWithSource) e;
			return ws.getSource();
		} else if (e.getParent() != null) {
			return e.getParent().attrSource();
		} else {
			return Ast.WPos("<source not found>", 0, 0);
		}
	}

}
