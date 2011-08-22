package de.peeeq.wurstscript.intermediateLang.translator;

import de.peeeq.wurstscript.intermediateLang.ILprog;

public interface IntermediateCodeCompressor {

	/**
	 * Tries to compress the program by using the extended statements like ILsetExpr
	 * @param prog Program which does not ILsetExpr and 
	 */
	void compress(ILprog prog);
	
}
