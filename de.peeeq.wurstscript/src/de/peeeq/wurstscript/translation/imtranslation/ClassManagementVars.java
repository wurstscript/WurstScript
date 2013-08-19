package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;

public class ClassManagementVars {
	/** array, nextFree[x] is the element which comes next in the queue */
	public final ImVar nextFree;
	
	/** first element of the queue, from here we take new objects */
	public final ImVar firstFree;
	
	/** last element of the queue, here we put destroyed objects */
	public final ImVar lastFree;
	
	/** the maximal index of current objects*/
	public final ImVar maxIndex;
	
	/** array, typeId of each object. used for dispatch */
	public final ImVar typeId;

	public ClassManagementVars(ImClass repClass, ImTranslator translator) {
		ImProg prog = translator.getImProg();
		nextFree = JassIm.ImVar(JassIm.ImArrayType("integer"), repClass.getName() + "_nextFree", false);
		prog.getGlobals().add(nextFree);
		
		firstFree = JassIm.ImVar(TypesHelper.imInt(), repClass.getName() + "_firstFree", false);
		translator.addGlobalWithInitalizer(firstFree, JassIm.ImIntVal(0));
		
		lastFree = JassIm.ImVar(TypesHelper.imInt(), repClass.getName() + "_lastFree", false);
		translator.addGlobalWithInitalizer(lastFree, JassIm.ImIntVal(0));
		
		maxIndex = JassIm.ImVar(TypesHelper.imInt(), repClass.getName() + "_maxIndex", false);
		translator.addGlobalWithInitalizer(maxIndex, JassIm.ImIntVal(0));
		
		typeId = JassIm.ImVar(JassIm.ImArrayType("integer"), repClass.getName() + "_typeId", false);
		prog.getGlobals().add(typeId);
	}
	
	
}
