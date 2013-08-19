package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImProg;

public interface RecycleCodeGenerator {

	void createAllocFunc(ImTranslator translator, ImProg prog, ImClass c);

	void createDeallocFunc(ImTranslator translator, ImProg prog, ImClass c);
	
}
