package de.peeeq.wurstscript.llvm.tollvm;


import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.llvm.ast.Ast;
import de.peeeq.wurstscript.llvm.ast.BasicBlock;
import de.peeeq.wurstscript.llvm.ast.Operand;

/**
 *
 */
public class StmtTranslator implements ImStmt.MatcherVoid {
    private LlvmTranslator tr;
    private BasicBlock exitWhenTarget;

    public StmtTranslator(LlvmTranslator tr) {
        this.tr = tr;
    }

    @Override
    public void case_ImIf(ImIf s) {
        Operand cond = tr.translateExpr(s.getCondition());
        BasicBlock ifTrue = block("if_true", s);
        BasicBlock ifFalse = block("if_false", s);
        BasicBlock ifEnd = block("if_end", s);
        tr.addInstruction(Ast.Branch(cond, ifTrue, ifFalse));
        tr.startBlock(ifTrue);
        tr.translateStmts(s.getThenBlock());
        tr.addInstruction(Ast.Jump(ifEnd));
        tr.startBlock(ifFalse);
        tr.translateStmts(s.getElseBlock());
        tr.addInstruction(Ast.Jump(ifEnd));
        tr.startBlock(ifEnd);
    }

    private BasicBlock block(String name, Element trace) {
        BasicBlock res = Ast.BasicBlock();
        res.setName(name + "_line_" + trace.attrTrace().attrSource().getLine());
        return res;
    }

    @Override
    public void case_ImLoop(ImLoop s) {
        BasicBlock loopStart = block("loop_start", s);
        BasicBlock loopEnd = block("loop_end", s);
        this.exitWhenTarget = loopEnd;
        tr.addInstruction(Ast.Jump(loopStart));
        tr.startBlock(loopStart);
        tr.translateStmts(s.getBody());
        tr.addInstruction(Ast.Jump(loopStart));
        tr.startBlock(loopEnd);
    }

    @Override
    public void case_ImExitwhen(ImExitwhen s) {
        BasicBlock exitwhenFalse = block("exitwhen_false", s);
        Operand cond = tr.translateExpr(s.getCondition());
        tr.addInstruction(Ast.Branch(cond, exitWhenTarget, exitwhenFalse));
        tr.startBlock(exitwhenFalse);
    }


    @Override
    public void case_ImSetTuple(ImSetTuple e) {
        throw new RuntimeException("TODO");
    }

    @Override
    public void case_ImReturn(ImReturn e) {
        if (e.getReturnValue() instanceof ImExpr) {
            ImExpr re = (ImExpr) e.getReturnValue();
            Operand ret = tr.translateExpr(re);
            tr.addInstruction(Ast.ReturnExpr(ret));
        } else {
            tr.addInstruction(Ast.ReturnVoid());
        }
        tr.finishCurrentBlock();
    }

    @Override
    public void case_ImSet(ImSet imSet) {
        Operand vl = tr.getVarLocation(imSet.getLeft());
        Operand val = tr.translateExpr(imSet.getRight());
        tr.addInstruction(Ast.Store(vl, val));
    }


    @Override
    public void case_ImSetArrayMulti(ImSetArrayMulti e) {
        throw new RuntimeException("TODO");
    }

    @Override
    public void case_ImSetArrayTuple(ImSetArrayTuple e) {
        throw new RuntimeException("TODO");
    }

    @Override
    public void case_ImSetArray(ImSetArray e) {
        throw new RuntimeException("TODO");
    }


    @Override
    public void case_ImGetStackTrace(ImGetStackTrace e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImFuncRef(ImFuncRef e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImFunctionCall(ImFunctionCall e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImTupleSelection(ImTupleSelection e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImStringVal(ImStringVal e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImOperatorCall(ImOperatorCall e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImStatementExpr(ImStatementExpr e) {
        tr.translateExpr(e);
    }


    @Override
    public void case_ImTypeIdOfObj(ImTypeIdOfObj e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImVarArrayAccess(ImVarArrayAccess e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImVarAccess(ImVarAccess e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImBoolVal(ImBoolVal e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImDealloc(ImDealloc e) {
        tr.translateExpr(e);
    }
    @Override
    public void case_ImAlloc(ImAlloc e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImNull(ImNull e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImMethodCall(ImMethodCall e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImTypeIdOfClass(ImTypeIdOfClass e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImVarArrayMultiAccess(ImVarArrayMultiAccess e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImIntVal(ImIntVal e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImMemberAccess(ImMemberAccess e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImTupleExpr(ImTupleExpr e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImRealVal(ImRealVal e) {
        tr.translateExpr(e);
    }

    @Override
    public void case_ImInstanceof(ImInstanceof e) {
        tr.translateExpr(e);
    }
}
