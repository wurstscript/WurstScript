package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;

public class ClassManagementVars {
	/** array, nextFree[x] is the element which comes next in the queue */
	public final ImVar free;
	
	/** first element of the queue, from here we take new objects */
	public final ImVar freeCount;
	
	/** the maximal index of current objects*/
	public final ImVar maxIndex;
	
	/** array, typeId of each object. used for dispatch */
	public final ImVar typeId;

	public ClassManagementVars(ImClass repClass, ImTranslator translator) {
		AstElement tr = repClass.getTrace();
		ImProg prog = translator.getImProg();
		free = JassIm.ImVar(tr, JassIm.ImArrayType("integer"), repClass.getName() + "_nextFree", false);
		prog.getGlobals().add(free);
		
		freeCount = JassIm.ImVar(tr, TypesHelper.imInt(), repClass.getName() + "_firstFree", false);
		translator.addGlobalWithInitalizer(freeCount, JassIm.ImIntVal(0));
		
		maxIndex = JassIm.ImVar(tr, TypesHelper.imInt(), repClass.getName() + "_maxIndex", false);
		translator.addGlobalWithInitalizer(maxIndex, JassIm.ImIntVal(0));
		
		typeId = JassIm.ImVar(tr, JassIm.ImArrayType("integer"), repClass.getName() + "_typeId", false);
		prog.getGlobals().add(typeId);
	}
	
	
}
