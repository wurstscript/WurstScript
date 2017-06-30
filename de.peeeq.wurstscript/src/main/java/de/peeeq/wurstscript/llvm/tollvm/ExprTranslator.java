package de.peeeq.wurstscript.llvm.tollvm;


import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.llvm.ast.*;

import java.util.stream.Collectors;

/**
 *
 */
public class ExprTranslator implements ImExpr.Matcher<Operand> {
    private LlvmTranslator tr;

    public ExprTranslator(LlvmTranslator tr) {
        this.tr = tr;
    }

    @Override
    public Operand case_ImGetStackTrace(ImGetStackTrace imGetStackTrace) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImDealloc(ImDealloc imDealloc) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImFuncRef(ImFuncRef imFuncRef) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImAlloc(ImAlloc imAlloc) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImFunctionCall(ImFunctionCall fc) {
        TemporaryVar v = Ast.TemporaryVar("call_result");
        Proc func = tr.getProcFor(fc.getFunc());
        OperandList args = fc.getArguments().stream()
                .map(tr::translateExpr)
                .collect(Collectors.toCollection(Ast::OperandList));

        tr.addInstruction(Ast.Call(v, Ast.ProcedureRef(func), args));
        return Ast.VarRef(v);
    }

    @Override
    public Operand case_ImTupleSelection(ImTupleSelection imTupleSelection) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImNull(ImNull imNull) {
        return Ast.Nullpointer();
    }

    @Override
    public Operand case_ImStringVal(ImStringVal imStringVal) {
        return Ast.ConstString(imStringVal.getValS());
    }

    @Override
    public Operand case_ImMethodCall(ImMethodCall imMethodCall) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImOperatorCall(ImOperatorCall imOperatorCall) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImTypeIdOfClass(ImTypeIdOfClass imTypeIdOfClass) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImStatementExpr(ImStatementExpr imStatementExpr) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImVarArrayMultiAccess(ImVarArrayMultiAccess imVarArrayMultiAccess) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImIntVal(ImIntVal imIntVal) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImTypeIdOfObj(ImTypeIdOfObj imTypeIdOfObj) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImVarArrayAccess(ImVarArrayAccess imVarArrayAccess) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImMemberAccess(ImMemberAccess imMemberAccess) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImTupleExpr(ImTupleExpr imTupleExpr) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImVarAccess(ImVarAccess imVarAccess) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImRealVal(ImRealVal imRealVal) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImInstanceof(ImInstanceof imInstanceof) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImBoolVal(ImBoolVal imBoolVal) {
        throw new RuntimeException("TODO");
    }
}
