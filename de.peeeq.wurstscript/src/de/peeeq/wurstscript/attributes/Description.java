package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.utils.Utils;

public class Description {

	public static String description(AstElement e) {
		
		return Utils.stripHtml(e.descriptionHtml());
	}

}
