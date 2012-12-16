package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElementWithModifiers;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.WurstDoc;
import de.peeeq.wurstscript.utils.Utils;

public class AttrWurstDoc {

	public static String getComment(NameDef nameDef) {
		if (nameDef instanceof AstElementWithModifiers) {
			AstElementWithModifiers astElementWithModifiers = (AstElementWithModifiers) nameDef;
			Modifiers modifiers = astElementWithModifiers.getModifiers();
			for (Modifier m : modifiers) {
				if (m instanceof WurstDoc) {
					WurstDoc wurstDoc = (WurstDoc) m;
					return comment(wurstDoc);
				}
			}
		}
		return "";
	}

	private static String comment(WurstDoc wurstDoc) {
		String result = wurstDoc.getRawComment();
		if (result.length() <= 5) {
			return "";
		}
		// strip of "/**" and */" 
		result = result.replaceAll("^/\\*\\*+", "").replaceAll("\\*+/$", ""); 
		
		String[] lines = result.split("(\n|\r|\n\r|\r\n)[\t ]*(\\*)? ?");
		
		result = Utils.join(lines, "\n");
		
		return result;
	}

}
