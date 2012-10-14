package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.FunctionLike;

public class SmallHelpers {

	public static boolean hasEmptyBody(FunctionLike f) {
		return f.getBody().size() <= 2;
	}

}
