package de.peeeq.wurstscript.llvm.analysis;

import de.peeeq.wurstscript.llvm.ast.*;

public class Typechecker {
    public static Type calculateType(ConstBool constBool) {
        return Ast.TypeBool();
    }

    public static Type calculateType(ConstInt constInt) {
        return Ast.TypeInt();
    }


    public static Type calculateType(VarRef o) {
        return o.getVariable().calculateType();
    }

    public static Type calculateType(GlobalRef o) {
        return Ast.TypePointer(o.getGlobal().getType());
    }

    public static Type calculateType(ProcedureRef o) {
        Proc proc = o.getProcedure();
        TypeRefList types = Ast.TypeRefList();

        for (Parameter v : proc.getParameters()) {
            types.add(v.getType());
        }

        return Ast.TypePointer(Ast.TypeProc(types,
                proc.getReturnType()));
    }

    public static Type calculateType(Nullpointer o) {
        return Ast.TypeNullpointer();
    }

    public static Type calculateType(Sizeof o) {
        return Ast.TypeInt();
    }

    public static Type calculateType(ConstStruct o) {
        return Ast.TypeRef(o.getStructType());
    }

    public static Type calculateType(Parameter parameter) {
        return parameter.getType();
    }

    public static Type calculateType(TemporaryVar t) {
        Element parent = t.getParent();
        if (parent == null) {
            // unknown
            return Ast.TypeByte();
        }
        if (parent instanceof Assign) {
            return ((Assign) parent).match(new Assign.Matcher<Type>() {
                @Override
                public Type case_Alloc(Alloc alloc) {
                    return Ast.TypePointer(Ast.TypeByte());
                }

                @Override
                public Type case_Call(Call call) {
                    Type funcType = call.getFunction().calculateType();
                    return returnTypeOfProcPointer(funcType);
                }

                @Override
                public Type case_Load(Load load) {
                    Type addressType = load.getAddress().calculateType();
                    if (addressType instanceof TypePointer) {
                        return ((TypePointer) addressType).getTo();
                    }
                    // error case:
                    return addressType;
                }

                @Override
                public Type case_Bitcast(Bitcast bitcast) {
                    return bitcast.getType();
                }

                @Override
                public Type case_BinaryOperation(BinaryOperation binOp) {
                    Operator op = binOp.getOperator();
                    if (isComparison(op)) {
                        return Ast.TypeBool();
                    }
                    // other operators return the same type as the arguments
                    return binOp.getLeft().calculateType();
                }

                @Override
                public Type case_Alloca(Alloca alloca) {
                    return Ast.TypePointer(alloca.getType());
                }

                @Override
                public Type case_GetElementPtr(GetElementPtr gep) {
                    Type ba = gep.getBaseAddress().calculateType();
                    if (ba instanceof TypePointer) {
                        Type t = ((TypePointer) ba).getTo();
                        for (int i = 1; i < gep.getIndices().size(); i++) {
                            Operand index = gep.getIndices().get(i);
                            if (t instanceof TypeArray) {
                                t = ((TypeArray) t).getOf();
                            } else if (t instanceof TypeStruct) {
                                TypeStruct struct = (TypeStruct) t;
                                if (index instanceof ConstInt) {
                                    int indexNr = ((ConstInt) index).getIntVal();
                                    if (indexNr >= 0 && indexNr < struct.getFields().size()) {
                                        t = struct.getFields().get(indexNr).getType();
                                    }
                                }
                            }
                        }
                        return Ast.TypePointer(t);
                    }
                    // unknown
                    return Ast.TypeByte();
                }

                @Override
                public Type case_PhiNode(PhiNode phiNode) {
                    return phiNode.getType();
                }
            });
        }
        throw new RuntimeException("unhandled case: " + parent.getClass().getSimpleName());
    }

    private static Type returnTypeOfProcPointer(Type funcPointerType) {
        if (funcPointerType instanceof TypePointer) {
            TypePointer funcType = (TypePointer) funcPointerType;
            if (funcType.getTo() instanceof TypeProc) {
                TypeProc procType = (TypeProc) funcType.getTo();
                return procType.getResultType();
            }
        }
        // unknown
        return Ast.TypeByte();
    }

    public static boolean isComparison(Operator operator) {
        return operator instanceof Eq
                || operator instanceof Slt;
    }

    public static boolean equalsType(TypeByte t, Type other) {
        return other instanceof TypeByte;
    }

    public static boolean equalsType(TypeVoid t, Type other) {
        return other instanceof TypeVoid;
    }

    public static boolean equalsType(TypeInt t, Type other) {
        return other instanceof TypeInt;
    }

    public static boolean equalsType(TypePointer t, Type other) {
        if (other instanceof TypePointer) {
            return t.getTo().equalsType(((TypePointer) other).getTo());
        }
        return false;
    }

    public static boolean equalsType(TypeArray t, Type other) {
        if (other instanceof TypeArray) {
            TypeArray ar = (TypeArray) other;
            return t.getOf().equalsType(ar.getOf())
                    && t.getSize() == ar.getSize();

        }
        return false;
    }

    public static boolean equalsType(TypeProc t, Type other) {
        if (other instanceof TypeProc) {
            TypeProc tp = (TypeProc) other;
            if (!t.getResultType().equalsType(tp.getResultType())) {
                return false;
            }
            if (t.getArgTypes().size() != tp.getArgTypes().size()) {
                return false;
            }
            for (int i = 0; i < t.getArgTypes().size(); i++) {
                if (!t.getArgTypes().get(i).equalsType(tp.getArgTypes().get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean equalsType(TypeNullpointer t, Type other) {
        return other instanceof TypeNullpointer;
    }

    public static boolean equalsType(TypeStruct t, Type other) {
        return t == other;
    }

    public static boolean equalsType(TypeBool t, Type other) {
        return other instanceof TypeBool;
    }

    public static boolean equalsType(TypeOpaque t, Type other) {
        return t == other;
    }

    public static Type calculateType(ConstString constString) {
        return Ast.TypeArray(Ast.TypeByte(), constString.getStringVal().getBytes().length);
    }

    public static boolean equalsType(TypeRef typeRef, Type other) {
        return other instanceof TypeRef
                && typeRef.getTypeDef() == ((TypeRef) other).getTypeDef();
    }
}

