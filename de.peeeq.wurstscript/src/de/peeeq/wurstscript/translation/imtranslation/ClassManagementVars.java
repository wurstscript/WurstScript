package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;

public class ClassManagementVars {
	public final ImVar nextFree;
	public final ImVar firstFree;
	public final ImVar maxIndex;
	public final ImVar typeId;

	public ClassManagementVars(ClassDef classDef, ImTranslator translator) {
		nextFree = JassIm.ImVar(JassIm.ImArrayType("integer"), classDef.getName() + "_nextFree", false);
		translator.addGlobal(nextFree);
		firstFree = JassIm.ImVar(TypesHelper.imInt(), classDef.getName() + "_firstFree", false);
		translator.addGlobal(firstFree);
		maxIndex = JassIm.ImVar(TypesHelper.imInt(), classDef.getName() + "_maxIndex", false);
		translator.addGlobal(maxIndex);
		typeId = JassIm.ImVar(JassIm.ImArrayType("integer"), classDef.getName() + "_typeId", false);
		translator.addGlobal(typeId);
		
	}
}
