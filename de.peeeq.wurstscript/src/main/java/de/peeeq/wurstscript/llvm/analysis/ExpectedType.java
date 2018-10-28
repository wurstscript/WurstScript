package de.peeeq.wurstscript.llvm.analysis;

import de.peeeq.wurstscript.llvm.ast.*;

public class ExpectedType {


    public static Type expectedType(Operand operand) {
        Element parent = operand.getParent();
        if (parent instanceof PhiNodeChoice) {
            PhiNode phiNode = getParent(PhiNode.class, parent);
            return phiNode.getType();
        } else if (parent instanceof OperandList) {
            if (parent.getParent() instanceof Call) {
                Call call = (Call) parent.getParent();
                int index = ((OperandList) parent).indexOf(operand);
                Type funcPointerType = call.getFunction().calculateType();
                if (funcPointerType instanceof TypePointer) {
                    TypePointer funcType = (TypePointer) funcPointerType;
                    if (funcType.getTo() instanceof TypeProc) {
                        TypeProc procType = (TypeProc) funcType.getTo();
                        if (index < procType.getArgTypes().size()) {
                            return procType.getArgTypes().get(index);
                        }
                    }
                }
                // unknown
                return Ast.TypePointer(Ast.TypeByte());
            } else if (parent.getParent() instanceof GetElementPtr) {
                return Ast.TypeInt();
            }
            throw new RuntimeException();
        } else if (parent instanceof Instruction) {
            return ((Instruction) parent).match(new Instruction.Matcher<Type>() {

                @Override
                public Type case_Store(Store store) {
                    Type storeType = store.getAddress().calculateType();
                    if (storeType instanceof TypePointer) {
                        Type t = ((TypePointer) storeType).getTo();
                        if (operand == store.getValue()) {
                            return t;
                        } else {
                            return storeType;
                        }
                    }
                    // unknown
                    return Ast.TypePointer(Ast.TypeByte());
                }

                @Override
                public Type case_Branch(Branch branch) {
                    return Ast.TypeBool();
                }



                @Override
                public Type case_HaltWithError(HaltWithError haltWithError) {
                    throw new RuntimeException();
                }

                @Override
                public Type case_Print(Print print) {
                    return Ast.TypeInt();
                }



                @Override
                public Type case_ReturnExpr(ReturnExpr returnExpr) {
                    return getParent(Proc.class, parent).getReturnType();
                }

                @Override
                public Type case_Jump(Jump jump) {
                    throw new RuntimeException();
                }


                @Override
                public Type case_CommentInstr(CommentInstr commentInstr) {
                    throw new RuntimeException();
                }

                @Override
                public Type case_ReturnVoid(ReturnVoid returnVoid) {
                    throw new RuntimeException();
                }

                @Override
                public Type case_Assign(Assign assign) {
                    return assign.getValueInstruction().match(new ValueInstruction.Matcher<Type>() {

                        @Override
                        public Type case_Alloca(Alloca alloca) {
                            throw new RuntimeException();
                        }

                        @Override
                        public Type case_Load(Load load) {
                            // unknown
                            return Ast.TypePointer(Ast.TypeByte());
                        }

                        @Override
                        public Type case_Select(Select select) {
                            if (operand == select.getCond()) {
                                return Ast.TypeBool();
                            } else if (operand == select.getIfFalse()){
                                return select.getIfTrue().calculateType();
                            }
                            // otherwise: unknown:
                            return Ast.TypePointer(Ast.TypeByte());
                        }

                        @Override
                        public Type case_Alloc(Alloc alloc) {
                            return Ast.TypeInt();
                        }

                        @Override
                        public Type case_PhiNode(PhiNode phiNode) {
                            throw new RuntimeException();
                        }

                        @Override
                        public Type case_BinaryOperation(BinaryOperation binaryOperation) {
                            // approximation: expects same type as other operand
                            Type t;
                            if (operand == binaryOperation.getLeft()) {
                                t = binaryOperation.getRight().calculateType();
                            } else {
                                t = binaryOperation.getLeft().calculateType();
                            }
                            if (t instanceof TypeNullpointer) {
                                return Ast.TypePointer(Ast.TypeByte());
                            }
                            return t;
                        }

                        @Override
                        public Type case_Bitcast(Bitcast bitcast) {
                            // unknown
                            return Ast.TypePointer(Ast.TypeByte());
                        }

                        @Override
                        public Type case_GetElementPtr(GetElementPtr gep) {
                            // unknown
                            return Ast.TypePointer(Ast.TypeByte());
                        }

                        @Override
                        public Type case_Call(Call call) {
                            // unknown
                            return Ast.TypePointer(Ast.TypeByte());
                        }

                    });
                }

                @Override
                public Type case_CallVoid(CallVoid callVoid) {
                    // unknown
                    return Ast.TypePointer(Ast.TypeByte());
                }
            });
        }
        throw new RuntimeException("Unhandled case: " + parent.getClass().getSimpleName());
    }

    private static <T> T getParent(Class<T> parentClass, Element e) {
        while (e != null) {
            if (parentClass.isAssignableFrom(e.getClass())) {
                @SuppressWarnings("unchecked")
                T r = (T) e;
                return r;
            }
            e = e.getParent();
        }
        throw new RuntimeException("No parent found.");
    }

}
