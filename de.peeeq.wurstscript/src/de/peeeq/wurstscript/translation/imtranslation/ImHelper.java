package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.ast.WParameters;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;

public class ImHelper {

	static void translateParameters(WParameters params, ImVars result) {
		for (WParameter p : params) {
			result.add(translateParam(p));
		}
	}

	static ImVar translateParam(WParameter p) {
		return JassIm.ImVar(p.attrTyp().imTranslateType(), p.getName());
	}

}
