package de.peeeq.wurstscript.llvm.tollvm;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.llvm.ast.*;

/**
 *
 */
public class ExprLTranslator implements ImLExpr.Matcher<Operand> {
    private final LlvmTranslator tr;

    public ExprLTranslator(LlvmTranslator tr) {
        this.tr = tr;
    }

    @Override
    public Operand case_ImVarAccess(ImVarAccess e) {
        return tr.getVarLocation(e.getVar());
    }

    @Override
    public Operand case_ImStatementExpr(ImStatementExpr se) {
        tr.translateStmts(se.getStatements());
        return tr.translateExprL((ImLExpr) se.getExpr());
    }

    @Override
    public Operand case_ImTupleSelection(ImTupleSelection imTupleSelection) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImTupleExpr(ImTupleExpr imTupleExpr) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImVarArrayAccess(ImVarArrayAccess e) {
        Operand loc = tr.getVarLocation(e.getVar());
        OperandList indices = Ast.OperandList(Ast.ConstInt(0));
        for (ImExpr ie : e.getIndexes()) {
            indices.add(tr.translateExpr(ie));
        }
        TemporaryVar addr = Ast.TemporaryVar(e.getVar().getName());
        Ast.Assign(addr, Ast.GetElementPtr(loc, indices));
        return Ast.VarRef(addr);
    }

    @Override
    public Operand case_ImMemberAccess(ImMemberAccess imMemberAccess) {
        throw new RuntimeException("TODO");
    }
}
