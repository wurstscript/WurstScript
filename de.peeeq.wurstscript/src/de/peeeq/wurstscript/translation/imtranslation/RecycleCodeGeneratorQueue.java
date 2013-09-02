package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Manages object ids in a queue. This way the time each object is
 * inactive is maximized and thus errors should be easier to detect  
 *
 */
public class RecycleCodeGeneratorQueue implements RecycleCodeGenerator {
	
	@Override 
	public void createAllocFunc(ImTranslator translator, ImProg prog, ImClass c) {
		ImFunction f = translator.allocFunc.getFor(c);
		prog.getFunctions().add(f);
		ImVars locals = f.getLocals();
		ImStmts body = f.getBody();
		AstElement tr = c.getTrace();
		
		ImVar thisVar = JassIm.ImVar(TypesHelper.imInt(), "this", false);
		locals.add(thisVar);
		
		ClassManagementVars mVars = translator.getClassManagementVarsFor(c);
		// if firstFree == null then
		ImStmts elseBlock = JassIm.ImStmts();
		ImStmts thenBlock = JassIm.ImStmts();
		body.add(JassIm.ImIf(tr, 
				JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(JassIm.ImVarAccess(mVars.firstFree), JassIm.ImIntVal(0))), 
				thenBlock, elseBlock));
		// maxIndex = maxIndex + 1
		thenBlock.add(JassIm.ImSet(tr, mVars.maxIndex, 
				JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(mVars.maxIndex), JassIm.ImIntVal(1)))));
		// this = maxIndex
		thenBlock.add(JassIm.ImSet(tr, thisVar, JassIm.ImVarAccess(mVars.maxIndex)));
		// else:
		// this = firstFree
		elseBlock.add(JassIm.ImSet(tr, thisVar, JassIm.ImVarAccess(mVars.firstFree)));
		// firstFree = nextFree[firstFree]
		elseBlock.add(JassIm.ImSet(tr, mVars.firstFree, JassIm.ImVarArrayAccess(mVars.nextFree, JassIm.ImVarAccess(mVars.firstFree))));
		// nextFree[this] = 0
		elseBlock.add(JassIm.ImSetArray(tr, mVars.nextFree, JassIm.ImVarAccess(thisVar), JassIm.ImIntVal(0)));
		// endif
		// typeId[this] = ...
		body.add(JassIm.ImSetArray(tr, mVars.typeId, JassIm.ImVarAccess(thisVar), JassIm.ImIntVal(c.attrTypeId())));
		
		// return this
		body.add(JassIm.ImReturn(tr, JassIm.ImVarAccess(thisVar)));
	}
	
	
	@Override 
	public void createDeallocFunc(ImTranslator translator, ImProg prog, ImClass c) {
		AstElement tr = c.getTrace();
		ImFunction f = translator.deallocFunc.getFor(c);
		prog.getFunctions().add(f);
		ImStmts body = f.getBody();
		ImVar thisVar = f.getParameters().get(0);
		
		ClassManagementVars mVars = translator.getClassManagementVarsFor(c);
		
		// if typeId[this] == 0 then error
		body.add(JassIm.ImIf(tr, 
				JassIm.ImOperatorCall(WurstOperator.EQ, 
						JassIm.ImExprs(JassIm.ImVarArrayAccess(mVars.typeId, JassIm.ImVarAccess(thisVar)), JassIm.ImIntVal(0))), 
				// then
				// error
				JassIm.ImStmts(JassIm.ImError(JassIm.ImStringVal("Double free: object of type " + c.getName()))),
				// else
				JassIm.ImStmts(
						// nextFree[lastFree] = this
						JassIm.ImSetArray(tr, mVars.nextFree, JassIm.ImVarAccess(mVars.lastFree), JassIm.ImVarAccess(thisVar)),
						// lastFree = this 
						JassIm.ImSet(tr, mVars.lastFree, JassIm.ImVarAccess(thisVar)),
						// if firstFree = 0
						JassIm.ImIf(tr, JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(JassIm.ImVarAccess(mVars.firstFree), JassIm.ImIntVal(0))), 
						// 		then firstFree = this
								JassIm.ImStmts(
										JassIm.ImSet(tr, mVars.firstFree, JassIm.ImVarAccess(thisVar)),
								// nextFree[this] = 0
										JassIm.ImSetArray(tr, mVars.nextFree, JassIm.ImVarAccess(thisVar), JassIm.ImIntVal(0))
								), JassIm.ImStmts()),
						// typeId[this] = 0
						JassIm.ImSetArray(tr, mVars.typeId, JassIm.ImVarAccess(thisVar), JassIm.ImIntVal(0))
						)));
		
	}
}
