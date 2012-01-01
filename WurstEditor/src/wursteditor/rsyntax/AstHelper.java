package wursteditor.rsyntax;

import de.peeeq.wurstscript.ast.AstElement;

public class AstHelper {
	public static AstElement getAstElementAtPos(AstElement elem, int caretPosition) {
		System.out.println("searching in " + elem.getClass().getName());
		AstElement result = elem;
		for (int i=0; i < elem.size(); i++) {
			AstElement e = elem.get(i);
			System.out.println("	child " + e.getClass().getName());
			if (elementContainsPos(e, caretPosition)) {
				System.out.println("	check, ");
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
		System.out.println("		" + e.getClass().getName() + " " +  e.attrSource().getLeftPos() + " <= " + pos + " <=" + e.attrSource().getRightPos());
		return e.attrSource().getLeftPos() <= pos && e.attrSource().getRightPos() >= pos;
	}
}
