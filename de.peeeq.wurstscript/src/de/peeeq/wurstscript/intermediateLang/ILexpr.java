package de.peeeq.wurstscript.intermediateLang;

import de.peeeq.wurstscript.types.PscriptType;

/**
 * an arbitrary expression like "(2 + y) * sqrt(9) >= 7"
 *
 */
public interface ILexpr extends CodePrinting {

	void printJassExpr(StringBuilder sb, int indent);

	PscriptType getType();

}
