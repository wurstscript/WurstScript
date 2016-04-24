package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.utils.Utils;

public class Description {

	public static String description(AstElement e) {
		String html = e.descriptionHtml();
		if (html == null) {
			return null;
		}
		return Utils.stripHtml(html);
	}

}
