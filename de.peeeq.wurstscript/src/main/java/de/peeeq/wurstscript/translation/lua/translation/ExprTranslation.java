package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.Optional;

public class ExprTranslation {

    public static final String TYPE_ID = "__typeId__";
    public static final String WURST_SUPERTYPES = "__wurst_supertypes";

    public static LuaExpr translate(ImAlloc e, LuaTranslator tr) {
        ImClass c = e.getClazz().getClassDef();
        LuaMethod m = tr.luaClassInitMethod.getFor(c);
        LuaVariable classVar = tr.luaClassVar.getFor(c);
        return LuaAst.LuaExprMethodCall(
            LuaAst.LuaExprVarAccess(classVar),
            m,
            LuaAst.LuaExprlist()
        );
    }

    public static LuaExpr translate(ImBoolVal e, LuaTranslator tr) {
        return LuaAst.LuaExprBoolVal(e.getValB());
    }

    public static LuaExpr translate(ImDealloc e, LuaTranslator tr) {
        return LuaAst.LuaExprNull(); // TODO maybe call some finalizer?
    }

    public static LuaExpr translate(ImFuncRef e, LuaTranslator tr) {
//        return LuaAst.LuaExprFuncRef(tr.luaFunc.getFor(e.getFunc()));
//         alternative: use xpcall to get stacktraces (did not work)
        LuaVariable dots = LuaAst.LuaVariable("...", LuaAst.LuaNoExpr());
        LuaVariable tempDots = LuaAst.LuaVariable("temp", LuaAst.LuaExprVarAccess(dots));
        LuaVariable tempRes = LuaAst.LuaVariable("tempRes", LuaAst.LuaExprNull());
        return LuaAst.LuaExprFunctionAbstraction(LuaAst.LuaParams(dots),
            LuaAst.LuaStatements(
                tempDots,
                tempRes,
                LuaAst.LuaExprFunctionCallByName("xpcall",
                    LuaAst.LuaExprlist(
                        LuaAst.LuaExprFunctionAbstraction(
                            LuaAst.LuaParams(),
                            LuaAst.LuaStatements(
                                LuaAst.LuaAssignment(LuaAst.LuaExprVarAccess(tempRes),
                                    LuaAst.LuaExprFunctionCall(tr.luaFunc.getFor(e.getFunc()), LuaAst.LuaExprlist(LuaAst.LuaExprVarAccess(tempDots)))))
                        ),
//                        LuaAst.LuaLiteral("function(err) " + errorFuncName(tr) + "(tostring(err)) end")
                        LuaAst.LuaLiteral("function(err) xpcall(function() " + callErrorFunc(tr, "tostring(err)") + " end, function(err2) BJDebugMsg(\"error reporting error: \" .. tostring(err2)) BJDebugMsg(\"while reporting: \" .. tostring(err))  end) end")
                        // unfortunately  BJDebugMsg(debug.traceback()) is not working
                    )
                ),
                LuaAst.LuaReturn(LuaAst.LuaExprVarAccess(tempRes))
            )
        );
    }

    private static String callErrorFunc(LuaTranslator tr, String msg) {
        LuaFunction ef = tr.getErrorFunc();
        if (ef != null) {
            if (ef.getParams().size() == 2) {
                return ef.getName() + "(" + msg + ", \"<lua error>\")";
            }
            return ef.getName() + "(" + msg + ")";
        }
        return "BJDebugMsg(" + msg + ")";
    }

    public static LuaExpr translate(ImFunctionCall e, LuaTranslator tr) {
        return LuaAst.LuaExprFunctionCall(tr.luaFunc.getFor(e.getFunc()), tr.translateExprList(e.getArguments()));
    }

    public static LuaExpr translate(ImInstanceof e, LuaTranslator tr) {
        return
            LuaAst.LuaExprFunctionCall(tr.instanceOfFunction, LuaAst.LuaExprlist(
                e.getObj().translateToLua(tr),
                LuaAst.LuaExprVarAccess(tr.luaClassVar.getFor(e.getClazz().getClassDef()))));
    }

    public static LuaExpr translate(ImIntVal e, LuaTranslator tr) {
        return LuaAst.LuaExprIntVal("" + e.getValI());
    }

    public static LuaExpr translate(ImMemberAccess e, LuaTranslator tr) {
        LuaExpr res = LuaAst.LuaExprFieldAccess(e.getReceiver().translateToLua(tr), e.getVar().getName());
        if (!e.getIndexes().isEmpty()) {
            LuaExprlist indexes = LuaAst.LuaExprlist();
            for (ImExpr index : e.getIndexes()) {
                indexes.add(index.translateToLua(tr));
            }
            res = LuaAst.LuaExprArrayAccess(res, indexes);
        }
        return res;
    }

    public static LuaExpr translate(ImMethodCall e, LuaTranslator tr) {
        return LuaAst.LuaExprMethodCall(e.getReceiver().translateToLua(tr), tr.luaMethod.getFor(e.getMethod()), tr.translateExprList(e.getArguments()));
    }

    public static LuaExpr translate(ImNull e, LuaTranslator tr) {
        return LuaAst.LuaExprNull();
    }

    public static LuaExpr translate(ImOperatorCall e, LuaTranslator tr) {
        if (e.getArguments().size() == 2) {
            ImExpr left = e.getArguments().get(0);
            ImExpr right = e.getArguments().get(1);
            if (e.getOp() == WurstOperator.EQ) {
                return translateEquals(left, right, tr);
            } else if (e.getOp() == WurstOperator.NOTEQ) {
                return LuaAst.LuaExprUnary(LuaAst.LuaOpNot(), translateEquals(left, right, tr));
            }
            LuaExpr leftExpr = left.translateToLua(tr);
            LuaExpr rightExpr = right.translateToLua(tr);
            LuaOpBinary op;
            if (e.getOp() == WurstOperator.PLUS
                && isStringType(left.attrTyp())
                && isStringType(right.attrTyp())) {
                // special case for string concatenation
                return LuaAst.LuaExprFunctionCall(tr.stringConcatFunction,
                    LuaAst.LuaExprlist(leftExpr, rightExpr));
            } else if (e.getOp() == WurstOperator.MOD_INT) {
                op = LuaAst.LuaOpMod();

                return LuaAst.LuaExprFunctionCallE(LuaAst.LuaLiteral("math.floor"),
                    LuaAst.LuaExprlist(LuaAst.LuaExprBinary(leftExpr, op, rightExpr)));
            } else if (e.getOp() == WurstOperator.DIV_INT) {
                op = LuaAst.LuaOpDiv();
                return LuaAst.LuaExprFunctionCallE(LuaAst.LuaLiteral("math.floor"),
                    LuaAst.LuaExprlist(LuaAst.LuaExprBinary(leftExpr, op, rightExpr)));
            } else {
                // TODO special cases for integer division and modulo
                op = e.getOp().luaTranslateBinary();
            }

            return LuaAst.LuaExprBinary(leftExpr, op, rightExpr);
        } else if (e.getArguments().size() == 1) {
            ImExpr arg = e.getArguments().get(0);
            LuaExpr argT = arg.translateToLua(tr);
            LuaOpUnary op;
            switch (e.getOp()) {
                case NOT:
                    op = LuaAst.LuaOpNot();
                    break;
                case UNARY_MINUS:
                    op = LuaAst.LuaOpMinus();
                    break;
                default:
                    throw new Error("not implemented: unary operator " + e.getOp());
            }
            return LuaAst.LuaExprUnary(op, argT);

        }
        throw new Error("not implemented: " + e);
    }

    static class TupleFunc {
        final ImTupleType tupleType;
        final LuaFunction func;

        public TupleFunc(ImTupleType tupleType, LuaFunction func) {
            this.tupleType = tupleType;
            this.func = func;
        }
    }


    private static LuaFunction getTupleEqualsFunc(ImTupleType t, LuaTranslator tr) {
        Optional<TupleFunc> tfo = tr.tupleEqualsFuncs.stream()
            .filter(f -> f.tupleType.equalsType(t))
            .findFirst();
        TupleFunc tf;
        if (tfo.isPresent()) {
            tf = tfo.get();
        } else {
            LuaVariable t1 = LuaAst.LuaVariable("t1", LuaAst.LuaNoExpr());
            LuaVariable t2 = LuaAst.LuaVariable("t2", LuaAst.LuaNoExpr());
            LuaStatements body = LuaAst.LuaStatements();
            LuaFunction func = LuaAst.LuaFunction(tr.uniqueName("tupleEquals"), LuaAst.LuaParams(t1, t2), body);
            LuaExpr result = LuaAst.LuaExprBoolVal(true);

            for (int i = 0; i < t.getNames().size(); i++) {
                result = conjunction(result, translateEquals(
                    LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(t1),
                        LuaAst.LuaExprlist(LuaAst.LuaExprIntVal("" + i))),
                    LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(t2),
                        LuaAst.LuaExprlist(LuaAst.LuaExprIntVal("" + i))),
                    t.getTypes().get(i),
                    tr));
            }

            body.add(LuaAst.LuaReturn(result));
            tr.luaModel.add(func);
            tf = new TupleFunc(t, func);
            tr.tupleEqualsFuncs.add(tf);
        }
        return tf.func;
    }


    public static LuaFunction getTupleCopyFunc(ImTupleType t, LuaTranslator tr) {
        Optional<TupleFunc> tfo = tr.tupleCopyFuncs.stream()
            .filter(f -> f.tupleType.equalsType(t))
            .findFirst();
        TupleFunc tf;
        if (tfo.isPresent()) {
            tf = tfo.get();
        } else {
            LuaVariable t1 = LuaAst.LuaVariable("t", LuaAst.LuaNoExpr());
            LuaStatements body = LuaAst.LuaStatements();
            LuaFunction func = LuaAst.LuaFunction(tr.uniqueName("tupleCopy"), LuaAst.LuaParams(t1), body);
            LuaTableFields fields = LuaAst.LuaTableFields();
            LuaExpr result = LuaAst.LuaTableConstructor(fields);

            int i = 0;
            for (ImType type : t.getTypes()) {
                i++;
                LuaExpr v = LuaAst.LuaExprArrayAccess(
                    LuaAst.LuaExprVarAccess(t1),
                    LuaAst.LuaExprlist(LuaAst.LuaExprIntVal("" + i))
                );
                if (type instanceof ImTupleType) {
                    ImTupleType tt = (ImTupleType) type;
                    v = LuaAst.LuaExprFunctionCall(getTupleCopyFunc(tt, tr), LuaAst.LuaExprlist(v));
                }
                fields.add(LuaAst.LuaTableSingleField(v));
            }

            body.add(LuaAst.LuaReturn(result));
            tr.luaModel.add(func);
            tf = new TupleFunc(t, func);
            tr.tupleCopyFuncs.add(tf);
        }
        return tf.func;
    }

    private static LuaExpr conjunction(LuaExpr left, LuaExpr right) {
        if (left instanceof LuaExprBoolVal && ((LuaExprBoolVal) left).getValB()) {
            return right;
        } else if (right instanceof LuaExprBoolVal && ((LuaExprBoolVal) right).getValB()) {
            return left;
        }
        return LuaAst.LuaExprBinary(left, LuaAst.LuaOpAnd(), right);
    }

    private static LuaExpr translateEquals(ImExpr left, ImExpr right, LuaTranslator tr) {
        LuaExpr leftExpr = left.translateToLua(tr);
        LuaExpr rightExpr = right.translateToLua(tr);
        ImType t = left.attrTyp();
        return translateEquals(leftExpr, rightExpr, t, tr);
    }

    private static LuaExpr translateEquals(LuaExpr leftExpr, LuaExpr rightExpr, ImType t, LuaTranslator tr) {
        if (t instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) t;
            LuaFunction ef = getTupleEqualsFunc(tt, tr);
            return LuaAst.LuaExprFunctionCall(ef, LuaAst.LuaExprlist(leftExpr, rightExpr));
        }
        return LuaAst.LuaExprBinary(leftExpr, LuaAst.LuaOpEquals(), rightExpr);
    }


    private static boolean isStringType(ImType t) {
        if (t instanceof ImSimpleType) {
            ImSimpleType st = (ImSimpleType) t;
            return st.getTypename().equals("string");
        }
        return false;
    }

    public static LuaExpr translate(ImRealVal e, LuaTranslator tr) {
        return LuaAst.LuaExprRealVal(e.getValR());
    }

    public static LuaExpr translate(ImStatementExpr e, LuaTranslator tr) {
        LuaStatements body = tr.translateStatements(e.getStatements());
        body.add(LuaAst.LuaReturn(e.getExpr().translateToLua(tr)));
        return LuaAst.LuaExprFunctionCallE(
            LuaAst.LuaExprFunctionAbstraction(LuaAst.LuaParams(), body),
            LuaAst.LuaExprlist());
    }

    public static LuaExpr translate(ImStringVal e, LuaTranslator tr) {
        return LuaAst.LuaExprStringVal(e.getValS());
    }

    public static LuaExpr translate(ImTupleExpr e, LuaTranslator tr) {
        LuaTableFields tableFields = LuaAst.LuaTableFields();
        for (ImExpr te : e.getExprs()) {
            tableFields.add(LuaAst.LuaTableSingleField(te.translateToLua(tr)));
        }
        return LuaAst.LuaTableConstructor(tableFields);
    }

    public static LuaExpr translate(ImTupleSelection e, LuaTranslator tr) {
        return LuaAst.LuaExprArrayAccess(e.getTupleExpr().translateToLua(tr), LuaAst.LuaExprlist(LuaAst.LuaExprIntVal("" + (1 + e.getTupleIndex()))));
    }

    public static LuaExpr translate(ImTypeIdOfClass e, LuaTranslator tr) {
        int i = tr.getTypeId(e.getClazz().getClassDef());
        return LuaAst.LuaExprIntVal("" + i);
    }

    public static LuaExpr translate(ImTypeIdOfObj e, LuaTranslator tr) {
        return LuaAst.LuaExprFieldAccess(e.getObj().translateToLua(tr), TYPE_ID);
    }

    public static LuaExpr translate(ImVarAccess e, LuaTranslator tr) {
        return LuaAst.LuaExprVarAccess(tr.luaVar.getFor(e.getVar()));
    }

    public static LuaExpr translate(ImVarArrayAccess e, LuaTranslator tr) {
        LuaExprlist indexes = LuaAst.LuaExprlist();
        for (ImExpr ie : e.getIndexes()) {
            indexes.add(ie.translateToLua(tr));
        }
        return LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.luaVar.getFor(e.getVar())), indexes);
    }

    public static LuaExpr translate(ImGetStackTrace e, LuaTranslator tr) {
//        return LuaAst.LuaLiteral("debug.traceback()");
        return LuaAst.LuaLiteral("\"$Stacktrace$\"");
    }

    public static LuaExpr translate(ImCompiletimeExpr imCompiletimeExpr, LuaTranslator tr) {
        throw new Error("not implemented");
    }

    public static LuaExpr translate(ImTypeVarDispatch imTypeVarDispatch, LuaTranslator tr) {
        throw new Error("not implemented");
    }

    public static LuaExpr translate(ImCast imCast, LuaTranslator tr) {
        LuaExpr translated = imCast.getExpr().translateToLua(tr);
        if (TypesHelper.isIntType(imCast.getToType()) && !TypesHelper.isIntType(imCast.getExpr().attrTyp())) {
            return LuaAst.LuaExprFunctionCall(tr.toIndexFunction, LuaAst.LuaExprlist(translated));
        } else if (imCast.getToType() instanceof ImClassType
            || imCast.getToType() instanceof ImAnyType) {
            return LuaAst.LuaExprFunctionCall(tr.fromIndexFunction, LuaAst.LuaExprlist(translated));
        } else {
            return translated;
        }
    }
}
