package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.utils.Utils;

public class PathDescription {
	public static String get(AstElement e) {
		String result = Utils.printElement(e);
		if (e.getParent() != null) {
			result = e.getParent().attrPathDescription() + "/" + result;
		}
		return result;
	}
}
