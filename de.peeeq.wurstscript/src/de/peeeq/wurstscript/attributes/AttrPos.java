package de.peeeq.wurstscript.attributes;

import java.util.List;
import java.util.Map;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.utils.LineOffsets;

public class AttrPos {

	/**
	 * makes a best effort to get a precise position for this element
	 * @param e
	 * @return
	 */
	public static WPos getPos(AstElement e) {
		if (e instanceof AstElementWithSource) {
			AstElementWithSource ws = (AstElementWithSource) e;
			return ws.getSource();
		}
		if (e.size() > 0) { // try to find the position by examining the childs
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (int i = 0; i < e.size(); i++) {
				AstElement child = e.get(i);
				WPos childSource = child.attrSource();
				min = Math.min(min, childSource.getLeftPos());
				max = Math.max(max, childSource.getRightPos());
			}
			return Ast.WPos(e.get(0).attrSource().getFile(), e.get(0).attrSource().getLineOffsets(), min, max);
		}
		// if no childs exist, search a parent element with a explicit position
		AstElement parent = e.getParent();
		while (parent != null) {
			if (parent instanceof AstElementWithSource) {
				WPos parentSource = ((AstElementWithSource) parent).getSource();
				// use parent position but with size -1, so we do not go into this
				return Ast.WPos(parentSource.getFile(), parentSource.getLineOffsets(), 
						parentSource.getLeftPos(), parentSource.getLeftPos()-1);
			}
		} 
		return Ast.WPos("<source not found>", null, 0, 0);
	}

	public static int getColumn(WPos p) {
		LineOffsets lineOffsets = (LineOffsets) p.getLineOffsets();
		return p.getLeftPos() - lineOffsets.get(p.getLine() - 1);
	}
	
	public static int getLine(WPos p) {
		LineOffsets lineOffsets = (LineOffsets) p.getLineOffsets();
		return lineOffsets.getLine(p.getLeftPos()) + 1;
	}
	

}
