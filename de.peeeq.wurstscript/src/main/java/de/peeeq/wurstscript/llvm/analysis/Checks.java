package de.peeeq.wurstscript.llvm.analysis;

import de.peeeq.wurstscript.llvm.ast.*;
import de.peeeq.wurstscript.llvm.printer.PrettyPrinter;
import de.peeeq.wurstscript.parser.WPos;

import java.util.*;

public class Checks {

    private Map<Element, WPos> sourcePositions;
    private String progString;
    private Map<BasicBlock, List<BasicBlock>> predecessorMap;

    public void checkProgram(Prog prog) {
        StringBuilder sb = new StringBuilder();
        PrettyPrinter printer = new PrettyPrinter(sb);
        prog.match(printer);
        sourcePositions = printer.getSourcePositions();
        progString = sb.toString();


        for (Proc proc : prog.getProcedures()) {
            checkProcedure(proc);
        }
    }

    private void checkProcedure(Proc proc) {
        if (proc.getBasicBlocks().isEmpty()) {
            error(proc, "Procedure " + proc.getName() + " has no basic blocks.");
        }

        predecessorMap = buildPredecessors(proc.getBasicBlocks());

        for (BasicBlock block : proc.getBasicBlocks()) {
            checkBlock(block);
        }
    }

    private Map<BasicBlock, List<BasicBlock>> buildPredecessors(BasicBlockList basicBlocks) {
        Map<BasicBlock, List<BasicBlock>> result = new HashMap<>();

        for (BasicBlock block : basicBlocks) {
            result.put(block, new ArrayList<>());
        }

        for (BasicBlock block : basicBlocks) {
            block.getTerminatingInstruction().ifPresent(t -> {
                if (t instanceof Jump) {
                    Jump jump = (Jump) t;
                    if (result.containsKey(jump.getLabel())) {
                        result.get(jump.getLabel()).add(block);
                    } else {
                        error(jump, "Block with label " + jump.getLabel().getName() + " does not exist in procedure.");
                    }
                } else if (t instanceof Branch) {
                    Branch branch = ((Branch) t);
                    if (!result.containsKey(branch.getIfFalseLabel())) {
                        error(branch, "Block with label " + branch.getIfFalseLabel().getName() + " does not exist in procedure.");
                    } else if (!result.containsKey(branch.getIfTrueLabel())) {
                        error(branch, "Block with label " + branch.getIfTrueLabel().getName() + " does not exist in procedure.");
                    } else {
                        result.get(branch.getIfFalseLabel()).add(block);
                        result.get(branch.getIfTrueLabel()).add(block);
                    }
                }
            });
        }
        return result;
    }

    private void checkBlock(BasicBlock block) {
        boolean afterPhi = false;
        boolean afterTerminating = false;
        for (Instruction instr : block) {
            if (instr instanceof CommentInstr) {
                continue;
            }
            if (afterTerminating) {
                error(instr, "There must be no instruction after a terminating instruction.");
            }
            if (!(instr instanceof PhiNode)) {
                afterPhi = true;
            }
            if (afterPhi && instr instanceof PhiNode) {
                error(instr, "Phi node instruction not at the beginning of block.");
            }
            checkInstruction(instr);
            if (instr instanceof TerminatingInstruction) {
                afterTerminating = true;
            }
        }
        if (!block.getTerminatingInstruction().isPresent()) {
            error(block, "Block " + block.getName() + " does not have a terminating instruction at the end.");
        }
    }

    private void checkInstruction(Instruction instr) {
        checkReferences(instr);
        instr.match(new Instruction.MatcherVoid() {
            @Override
            public void case_Store(Store store) {
                Type addrType = getType(store.getAddress());
                if (addrType instanceof TypePointer) {
                    Type to = ((TypePointer) addrType).getTo();
                    Type valueType = getType(store.getValue());
                    if (!valueType.equalsType(to)) {
                        error(instr, "Store expected value of type " + to + ", but found value of type " + valueType);
                    }
                } else {
                    error(instr, "The address of a store instruction should be a pointer type, but it was: " + addrType);
                }
            }

            @Override
            public void case_Branch(Branch branch) {
                expectType(branch.getCondition(), Ast.TypeBool());
                checkReference(branch, branch.getIfTrueLabel(), Proc.class);
                checkReference(branch, branch.getIfFalseLabel(), Proc.class);
            }

            @Override
            public void case_CommentInstr(CommentInstr commentInstr) {
                // nothing to check
            }

            @Override
            public void case_ReturnVoid(ReturnVoid returnVoid) {
                Proc proc = getParent(Proc.class, returnVoid);
                if (!(proc.getReturnType() instanceof TypeVoid)) {
                    error(returnVoid, "Return void can only be used in functions returning void");
                }
            }

            @Override
            public void case_ReturnExpr(ReturnExpr returnExpr) {
                Proc proc = getParent(Proc.class, returnExpr);
                expectType(returnExpr.getReturnValue(), proc.getReturnType());
            }


            @Override
            public void case_Jump(Jump jump) {
                checkReference(jump, jump.getLabel(), Proc.class);
            }

            @Override
            public void case_Print(Print print) {
                expectType(print.getE(), Ast.TypeInt());
            }


            @Override
            public void case_Assign(Assign assign) {
                assign.getValueInstruction().match(new ValueInstruction.MatcherVoid() {
                    @Override
                    public void case_GetElementPtr(GetElementPtr gep) {
                        Type ba = getType(gep.getBaseAddress());
                        if (ba instanceof TypePointer) {
                            Type t = ((TypePointer) ba).getTo();
                            for (int i = 1; i < gep.getIndices().size(); i++) {
                                Operand index = gep.getIndices().get(i);
                                if (t instanceof TypeArray) {
                                    t = ((TypeArray) t).getOf();
                                } else if (t instanceof TypeDef) {
                                    TypeDef struct = (TypeDef) t;
                                    if (index instanceof ConstInt) {
                                        int indexNr = ((ConstInt) index).getIntVal();
                                        if (indexNr >= 0 && indexNr < struct.getFields().size()) {
                                            t = struct.getFields().get(indexNr).getType();
                                        } else {
                                            error(index, "Struct " + struct.getName() + " does not have a field index " + indexNr);
                                        }
                                    }
                                } else {
                                    error(index, "Can only index into array or struct types, got " + t);
                                }
                            }
                        } else {
                            error(gep.getBaseAddress(), "Expected a pointer type, but found " + ba);
                        }
                    }

                    @Override
                    public void case_BinaryOperation(BinaryOperation bop) {
                        Type leftType = getType(bop.getLeft());
                        Type rightType = getType(bop.getRight());
                        if (!leftType.equalsType(rightType)) {
                            error(bop, "Both operands of binary operation must be of same type, got " + leftType + " and " + rightType);
                            return;
                        }
                        if (bop.getOperator() instanceof Eq) {
                            // nothing more to check
                        }
                        if (Typechecker.isComparison(bop.getOperator())) {
                            if (!isIntegerType(leftType) && !isPointerType(leftType)) {
                                error(bop, "Can only compare integer and pointer types, but got type " + leftType);
                            }
                        } else {
                            if (!isIntegerType(leftType)) {
                                error(bop, "Binary operation " + bop.getOperator() + " can only be applied to integer types, but got type " + leftType);
                            }
                        }
                    }

                    @Override
                    public void case_Alloca(Alloca alloca) {
                        // nothing to check
                    }

                    @Override
                    public void case_Load(Load load) {
                        Type addrType = getType(load.getAddress());
                        if (!(addrType instanceof TypePointer)) {
                            error(load, "Address of load instruction must be a pointer type, but found type " + addrType);
                        }
                    }

                    @Override
                    public void case_Bitcast(Bitcast bitcast) {
                        // no checks done (could do some sanity checks)
                    }

                    @Override
                    public void case_Call(Call call) {
                        checkCall(call, call.getFunction(), call.getArguments());
                    }

                    @Override
                    public void case_Alloc(Alloc alloc) {
                        // nothing to check
                    }

                    @Override
                    public void case_PhiNode(PhiNode phiNode) {
                        List<BasicBlock> predecessors = new ArrayList<BasicBlock>(predecessorMap.get(phiNode.getParent()));
                        for (PhiNodeChoice c : phiNode.getChoices()) {
                            expectType(c.getValue(), phiNode.getType());
                            if (!predecessors.remove(c.getLabel())) {
                                error(c, "Phi choice " + c.getLabel() + " is not a predecessor.");
                            }
                        }
                        for (BasicBlock predecessor : predecessors) {
                            error(phiNode, "Phi node is missing a choice for " + predecessor);
                        }
                    }
                });
            }

            @Override
            public void case_CallVoid(CallVoid call) {
                checkCall(call, call.getFunction(), call.getArguments());
            }


            private void checkCall(Element errorPos, Operand func, OperandList args) {
                Type funcType = getType(func);
                if (funcType instanceof TypePointer) {
                    TypePointer pointerType = (TypePointer) funcType;
                    if (pointerType.getTo() instanceof TypeProc) {
                        TypeProc pt = (TypeProc) pointerType.getTo();

                        if (args.size() != pt.getArgTypes().size()) {
                            error(errorPos, "Expected " + pt.getArgTypes().size() + " arguments, but has " + args.size() + " arguments.");
                            return;
                        }

                        // check parameter types
                        for (int i = 0; i < args.size(); i++) {
                            expectType(args.get(i),
                                    pt.getArgTypes().get(i));
                        }
                        return;
                    }

                }
                error(errorPos, "Procedure type must be a pointer to a procedure, but found: " + funcType);
            }


            @Override
            public void case_HaltWithError(HaltWithError haltWithError) {
                // nothing to check
            }


        });
    }

    private boolean isPointerType(Type t) {
        return t instanceof TypePointer
                || t instanceof TypeNullpointer;
    }

    private boolean isIntegerType(Type t) {
        return t instanceof TypeInt
                || t instanceof TypeByte
                || t instanceof TypeBool;
    }

    private void expectType(Operand operand, Type expected) {
        Type t = getType(operand);
        if (!t.equalsType(expected)) {
            error(operand, "Expected operand of type " + expected + ", but found " + operand + " of type " + t + ".");
        }
    }

    private Type getType(Operand operand) {
        Type t = operand.calculateType();
        if (t instanceof TypeNullpointer) {
            Type expected = ExpectedType.expectedType(operand);
            if (expected instanceof TypePointer) {
                return expected;
            }
        }
        return t;
    }

    private <T extends Element> void checkReference(Element e, Element referenced, Class<T> commonTarget) {
        T refParent = getParent(commonTarget, referenced);
        T eParent = getParent(commonTarget, e);
        if (refParent == null) {
            error(e, "Referenced element in " + referenced + " is not part of a " + commonTarget.getSimpleName());
        } else if (refParent != eParent) {
            error(e, "Referenced element in " + referenced + " is not part of the same " + commonTarget.getSimpleName());
        }
    }

    private void checkReferences(Element e) {
        e.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(VarRef varRef) {
                super.visit(varRef);
                checkReference(varRef, varRef.getVariable(), Proc.class);
            }

            @Override
            public void visit(GlobalRef varRef) {
                super.visit(varRef);
                checkReference(varRef, varRef.getGlobal(), Prog.class);
            }

            @Override
            public void visit(ProcedureRef p) {
                super.visit(p);
                checkReference(p, p.getProcedure(), Prog.class);
            }

            @Override
            public void visit(ConstStruct p) {
                super.visit(p);
                checkReference(p, p.getStructType(), Prog.class);
            }

            @Override
            public void visit(Sizeof p) {
                super.visit(p);
                checkReference(p, p.getStructType(), Prog.class);
            }

            @Override
            public void visit(PhiNodeChoice p) {
                super.visit(p);
                checkReference(p, p.getLabel(), Proc.class);
            }
        });
    }

    private <T extends Element> T getParent(Class<T> clazz, Element e) {
        while (e != null) {
            if (clazz.isAssignableFrom(e.getClass())) {
                @SuppressWarnings("unchecked")
                T res = (T) e;
                return res;
            }
            e = e.getParent();
        }
        return null;
    }

    private void error(Element e, String s) {
        WPos pos = getPos(e);
        String line = progString.split("\n")[pos.getLine() - 1];

        int endColumn;
        if (pos.getLine() == pos.getEndLine()) {
            endColumn = pos.getEndColumn();
        } else {
            endColumn = line.length();
        }

        String message = s;
        message += "\n\n";
        message += line + "\n";
        message += repeat(' ', pos.getStartColumn());
        message += repeat('^', endColumn - pos.getStartColumn());

        throw new LlvmTypeError(pos, message);
    }

    private String repeat(char c, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    private WPos getPos(Element e) {
        while (e != null) {
            WPos sourcePosition = sourcePositions.get(e);
            if (sourcePosition != null) {
                return sourcePosition;
            }
            e = e.getParent();
        }
        return new WPos("", null, 1, 0);
    }

}
