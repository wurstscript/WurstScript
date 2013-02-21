package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;

public class ClassManagementVars {
	public final ImVar nextFree;
	public final ImVar firstFree;
	public final ImVar maxIndex;
	public final ImVar typeId;

	public ClassManagementVars(StructureDef repClass, ImTranslator translator) {
		nextFree = JassIm.ImVar(JassIm.ImArrayType("integer"), repClass.getName() + "_nextFree", false);
		translator.addGlobal(nextFree);
		firstFree = JassIm.ImVar(TypesHelper.imInt(), repClass.getName() + "_firstFree", false);
		translator.addGlobal(firstFree);
		translator.addGlobalInitalizer(firstFree, null, Ast.ExprIntVal(repClass.getSource(), "0"));
		maxIndex = JassIm.ImVar(TypesHelper.imInt(), repClass.getName() + "_maxIndex", false);
		translator.addGlobal(maxIndex);
		translator.addGlobalInitalizer(maxIndex, null, Ast.ExprIntVal(repClass.getSource(), "0"));
		typeId = JassIm.ImVar(JassIm.ImArrayType("integer"), repClass.getName() + "_typeId", false);
		translator.addGlobal(typeId);
	}
}
