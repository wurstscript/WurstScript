package de.peeeq.wurstscript.llvm.tollvm;


import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.llvm.ast.*;

import java.util.ArrayList;
import java.util.List;
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
        Operand obj = tr.translateExpr(imDealloc.getObj());
        TemporaryVar resVar = Ast.TemporaryVar("void");
        Operand func = Ast.ProcedureRef(tr.builtinProc(LlvmTranslator.BuiltinProc.free));
        tr.addInstruction(Ast.Call(resVar, func, Ast.OperandList(obj)));
        return Ast.VarRef(resVar);
    }

    @Override
    public Operand case_ImFuncRef(ImFuncRef imFuncRef) {
        return Ast.ProcedureRef(tr.getProcFor(imFuncRef.getFunc()));
    }

    @Override
    public Operand case_ImAlloc(ImAlloc imAlloc) {
        Operand obj = Ast.Sizeof(tr.getStructFor(imAlloc.getClazz()));
        TemporaryVar resVar = Ast.TemporaryVar("void");
        Operand func = Ast.ProcedureRef(tr.builtinProc(LlvmTranslator.BuiltinProc.free));
        tr.addInstruction(Ast.Call(resVar, func, Ast.OperandList(obj)));
        return Ast.VarRef(resVar);
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
        throw new RuntimeException("TODO"); // TODO
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
        throw new RuntimeException("TODO"); // TODO
    }

    @Override
    public Operand case_ImOperatorCall(ImOperatorCall e) {
        List<Operand> args = new ArrayList<>();
        for (ImExpr arg : e.getArguments()) {
            args.add(tr.translateExpr(arg));
        }
        TemporaryVar res = Ast.TemporaryVar(e.getOp() + "_res");
        // TODO and/or lazy evaluation

        switch (e.getOp()) {
            case OR:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Or(), args.get(1)));
                break;
            case AND:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.And(), args.get(1)));
                break;
            case EQ:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Eq(), args.get(1)));
                break;
            case NOTEQ:
                TemporaryVar eq_res = Ast.TemporaryVar("eq_res");
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.NotEq(), args.get(1)));
                break;
            case LESS_EQ:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Sle(), args.get(1)));
                break;
            case LESS:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Slt(), args.get(1)));
                break;
            case GREATER_EQ:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Sge(), args.get(1)));
                break;
            case GREATER:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Sgt(), args.get(1)));
                break;
            case PLUS:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Add(), args.get(1)));
                break;
            case MINUS:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Sub(), args.get(1)));
                break;
            case MULT:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Mul(), args.get(1)));
                break;
            case DIV_REAL:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Fdiv(), args.get(1)));
                break;
            case DIV_INT:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Sdiv(), args.get(1)));
                break;
            case MOD_REAL:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Frem(), args.get(1)));
                break;
            case MOD_INT:
                tr.addInstruction(Ast.BinaryOperation(res, args.get(0), Ast.Srem(), args.get(1)));
                break;
            case NOT:
                tr.addInstruction(Ast.BinaryOperation(res, Ast.ConstInt(1), Ast.Xor(), args.get(0)));
                break;
            case UNARY_MINUS:
                tr.addInstruction(Ast.BinaryOperation(res, Ast.ConstInt(0), Ast.Sub(), args.get(0)));
                break;
            default:
                throw new RuntimeException("unhandled operator " + e.getOp());
        }

        return Ast.VarRef(res);
    }

    @Override
    public Operand case_ImTypeIdOfClass(ImTypeIdOfClass e) {
        return Ast.ConstInt(e.getClazz().attrTypeId());
    }

    @Override
    public Operand case_ImStatementExpr(ImStatementExpr se) {
        tr.translateStmts(se.getStatements());
        return tr.translateExpr(se.getExpr());
    }

    @Override
    public Operand case_ImVarArrayMultiAccess(ImVarArrayMultiAccess imVarArrayMultiAccess) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Operand case_ImIntVal(ImIntVal e) {
        return Ast.ConstInt(e.getValI());
    }

    @Override
    public Operand case_ImTypeIdOfObj(ImTypeIdOfObj e) {
        return Ast.ConstInt(e.getClazz().attrTypeId());
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
    public Operand case_ImCompiletimeExpr(ImCompiletimeExpr imCompiletimeExpr) {
        throw new RuntimeException("Compiletime expression not implemented");
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
