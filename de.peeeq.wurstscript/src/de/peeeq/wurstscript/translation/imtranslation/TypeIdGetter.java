package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImVar;

public interface TypeIdGetter {
	ImExpr get(ImVar thisVar);
}
