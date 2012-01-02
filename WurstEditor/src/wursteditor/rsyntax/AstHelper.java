package wursteditor.rsyntax;

import de.peeeq.wurstscript.ast.AstElement;

public class AstHelper {
	public static AstElement getAstElementAtPos(AstElement elem, int caretPosition) {
		AstElement result = elem;
		for (int i=0; i < elem.size(); i++) {
			AstElement e = elem.get(i);
			if (elementContainsPos(e, caretPosition)) {
				result = e;
			}
		}
		if (result == elem) {
			return result;
		} else {
			return getAstElementAtPos(result, caretPosition);
		}
	}


	public static boolean elementContainsPos(AstElement e, int pos) {
		return e.attrSource().getLeftPos() <= pos && e.attrSource().getRightPos() >= pos;
	}
}
