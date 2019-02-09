package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Constants;

/**
 * Manages object ids in a queue. This way the time each object is
 * inactive is maximized and thus errors should be easier to detect
 */
public class RecycleCodeGeneratorQueue implements RecycleCodeGenerator {

    @Override
    public void createAllocFunc(ImTranslator translator, ImProg prog, ImClass c) {
        ImFunction f = translator.allocFunc.getFor(c);
        prog.getFunctions().add(f);
        ImVars locals = f.getLocals();
        ImStmts body = f.getBody();
        Element tr = c.getTrace();

        ImVar thisVar = JassIm.ImVar(tr, translator.selfType(c), "this", false); // TODO change type
        locals.add(thisVar);

        ClassManagementVars mVars = translator.getClassManagementVarsFor(c);

        int maxSize = Constants.MAX_ARRAY_SIZE;
        // if freeCount == 0 then
        ImStmts elseBlock = JassIm.ImStmts();
        ImStmts thenBlock = JassIm.ImStmts();
        body.add(JassIm.ImIf(tr,
                JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(JassIm.ImVarAccess(mVars.freeCount), JassIm.ImIntVal(0))),
                thenBlock, elseBlock));
        ImStmts ifEnoughMemory = JassIm.ImStmts();
        ImStmts ifNotEnoughMemory = JassIm.ImStmts();
        //     if maxIndex < 8191
        thenBlock.add(JassIm.ImIf(tr,
                JassIm.ImOperatorCall(WurstOperator.LESS, JassIm.ImExprs(JassIm.ImVarAccess(mVars.maxIndex), JassIm.ImIntVal(maxSize))),
                ifEnoughMemory, ifNotEnoughMemory));
        //         maxIndex = maxIndex + 1
        ifEnoughMemory.add(JassIm.ImSet(tr, JassIm.ImVarAccess(mVars.maxIndex), JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(mVars.maxIndex), JassIm.ImIntVal(1)))));
        // 		   this = maxIndex
        ifEnoughMemory.add(JassIm.ImSet(tr, JassIm.ImVarAccess(thisVar), JassIm.ImVarAccess(mVars.maxIndex)));
        //	       typeId[this] = ...
        ifEnoughMemory.add(JassIm.ImSet(tr, JassIm.ImVarArrayAccess(tr, mVars.typeId, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(thisVar))), JassIm.ImIntVal(c.attrTypeId())));
        //     else:
        //         error("out of memory")
        ifNotEnoughMemory.add(translator.imError(c.getTrace(), JassIm.ImStringVal("Out of memory: Could not create " + c.getName() + ".")));
        //         this = 0
        ifNotEnoughMemory.add(JassIm.ImSet(tr, JassIm.ImVarAccess(thisVar), JassIm.ImIntVal(0)));
        // else:
        //     freeCount = freeCount - 1
        elseBlock.add(JassIm.ImSet(tr, JassIm.ImVarAccess(mVars.freeCount), JassIm.ImOperatorCall(WurstOperator.MINUS, JassIm.ImExprs(JassIm.ImVarAccess(mVars.freeCount), JassIm
                .ImIntVal(1)))));
        //     this = free[freeCount]
        elseBlock.add(JassIm.ImSet(tr, JassIm.ImVarAccess(thisVar), JassIm.ImVarArrayAccess(tr, mVars.free, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(mVars.freeCount)))));
        //     typeId[this] = ...
        elseBlock.add(JassIm.ImSet(tr, JassIm.ImVarArrayAccess(tr, mVars.typeId, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(thisVar))), JassIm.ImIntVal(c.attrTypeId())));
        // endif


        // return this
        body.add(JassIm.ImReturn(tr, JassIm.ImVarAccess(thisVar)));
    }


    @Override
    public void createDeallocFunc(ImTranslator translator, ImProg prog, ImClass c) {
        Element tr = c.getTrace();
        ImFunction f = translator.deallocFunc.getFor(c);
        prog.getFunctions().add(f);
        ImStmts body = f.getBody();
        ImVar thisVar = f.getParameters().get(0);

        ClassManagementVars mVars = translator.getClassManagementVarsFor(c);

        // if typeId[this] == 0 then error
        body.add(JassIm.ImIf(tr,
                JassIm.ImOperatorCall(WurstOperator.EQ,
                        JassIm.ImExprs(JassIm.ImVarArrayAccess(tr, mVars.typeId, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(thisVar))), JassIm.ImIntVal(0))),
                // then
                // error
                JassIm.ImStmts(translator.imError(c.getTrace(), JassIm.ImStringVal("Double free: object of type " + c.getName()))),
                // else
                JassIm.ImStmts(
                        // free[freeCount] = this
                        JassIm.ImSet(tr, JassIm.ImVarArrayAccess(tr, mVars.free, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(mVars.freeCount))), JassIm.ImVarAccess(thisVar)),
                        // freeCount++
                        JassIm.ImSet(tr, JassIm.ImVarAccess(mVars.freeCount), JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(mVars.freeCount),
                                JassIm.ImIntVal(1)))),
                        // typeId[this] = 0
                        JassIm.ImSet(tr, JassIm.ImVarArrayAccess(tr, mVars.typeId, JassIm.ImExprs((ImExpr) JassIm.ImVarAccess(thisVar))), JassIm.ImIntVal(0))
                )));

    }
}
