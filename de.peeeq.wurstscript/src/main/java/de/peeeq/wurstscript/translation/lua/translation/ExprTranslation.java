package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.imtranslation.LuaMethodCallLowering;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.Optional;
import java.util.Set;

public class ExprTranslation {

    public static final String TYPE_ID = "__typeId__";
    public static final String WURST_SUPERTYPES = "__wurst_supertypes";
    static final String WURST_ABORT_THREAD_SENTINEL = "__wurst_abort_thread";
    private static final Set<String> LUA_HANDLE_TO_INDEX = Set.of(
        "widgetToIndex", "unitToIndex", "destructableToIndex", "itemToIndex", "abilityToIndex",
        "forceToIndex", "groupToIndex", "triggerToIndex", "triggeractionToIndex", "triggerconditionToIndex",
        "timerToIndex", "locationToIndex", "regionToIndex", "rectToIndex", "soundToIndex",
        "effectToIndex", "dialogToIndex", "buttonToIndex", "questToIndex", "questitemToIndex",
        "leaderboardToIndex", "multiboardToIndex", "trackableToIndex", "lightningToIndex",
        "ubersplatToIndex", "framehandleToIndex", "oskeytypeToIndex"
    );
    private static final Set<String> LUA_HANDLE_FROM_INDEX = Set.of(
        "widgetFromIndex", "unitFromIndex", "destructableFromIndex", "itemFromIndex", "abilityFromIndex",
        "forceFromIndex", "groupFromIndex", "triggerFromIndex", "triggeractionFromIndex", "triggerconditionFromIndex",
        "timerFromIndex", "locationFromIndex", "regionFromIndex", "rectFromIndex", "soundFromIndex",
        "effectFromIndex", "dialogFromIndex", "buttonFromIndex", "questFromIndex", "questitemFromIndex",
        "leaderboardFromIndex", "multiboardFromIndex", "trackableFromIndex", "lightningFromIndex",
        "ubersplatFromIndex", "framehandleFromIndex", "oskeytypeFromIndex"
    );

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
        return LuaAst.LuaExprFunctionCall(tr.objectDealloc,
            LuaAst.LuaExprlist(e.getObj().translateToLua(tr)));
    }

    public static LuaExpr translate(ImFuncRef e, LuaTranslator tr) {
        return LuaAst.LuaExprFuncRef(tr.callbackAdapterFor(e.getFunc()));
    }

    static String callErrorFunc(LuaTranslator tr, String msg) {
        return callErrorFunc(tr, msg, "<lua error>");
    }

    static String callErrorFunc(LuaTranslator tr, String msg, String stackPos) {
        LuaFunction ef = tr.getErrorFunc();
        if (ef != null) {
            if (ef.getParams().size() == 2) {
                return ef.getName() + "(" + msg + ", \"" + stackPos + "\")";
            }
            return ef.getName() + "(" + msg + ")";
        }
        return "BJDebugMsg(" + msg + ")";
    }

    public static LuaExpr translate(ImFunctionCall e, LuaTranslator tr) {
        // String concatenation is lowered to imTr.stringConcatFunc at the IM level
        // (see EliminateLocalTypes). It is a plain, portable, uniquely-named IM
        // function now (see LuaEnsureFunctions), so it needs no special-casing
        // here - the generic function-call translation below handles it, the same
        // way it handles any other Wurst-internal function.
        String tcFunc = tr.getTypeCastingFunctionName(e.getFunc());
        if (tcFunc != null && !e.getArguments().isEmpty()) {
            LuaExpr arg = e.getArguments().get(0).translateToLua(tr);
            ImType argumentType = e.getArguments().get(0).attrTyp();
            ImType resultType = e.attrTyp();
            if (argumentType instanceof ImClassType && TypesHelper.isIntType(resultType)) {
                return LuaAst.LuaExprFunctionCall(tr.classToIndex, LuaAst.LuaExprlist(arg));
            } else if (TypesHelper.isIntType(argumentType) && resultType instanceof ImClassType) {
                return LuaAst.LuaExprFunctionCall(tr.classFromIndex, LuaAst.LuaExprlist(arg));
            } else if (tcFunc.equals("objectToIndex")) {
                return LuaAst.LuaExprFunctionCall(tr.toIndexFunction, LuaAst.LuaExprlist(arg));
            } else if (tcFunc.equals("objectFromIndex")) {
                return LuaAst.LuaExprFunctionCall(tr.fromIndexFunction, LuaAst.LuaExprlist(arg));
            } else if (tcFunc.equals("stringToIndex")) {
                return LuaAst.LuaExprFunctionCall(tr.stringToIndexFunction, LuaAst.LuaExprlist(arg));
            } else if (tcFunc.equals("stringFromIndex")) {
                return LuaAst.LuaExprFunctionCall(tr.stringFromIndexFunction, LuaAst.LuaExprlist(arg));
            } else if (LUA_HANDLE_TO_INDEX.contains(tcFunc)) {
                return LuaAst.LuaExprFunctionCall(tr.toIndexFunction, LuaAst.LuaExprlist(arg));
            } else if (LUA_HANDLE_FROM_INDEX.contains(tcFunc)) {
                return LuaAst.LuaExprFunctionCall(tr.fromIndexFunction, LuaAst.LuaExprlist(arg));
            }
        }

        // Use the immutable ImFunction name rather than f.getName(), because f is a cached
        // LuaFunction object shared across all call sites of this native. The setName() calls
        // below mutate it, so f.getName() changes after the first translation and can no longer
        // be relied upon for sentinel checks.
        String imFuncName = e.getFunc().getName();
        if (isRawNumericIntrinsic(e.getFunc(), tr)) {
            if (e.getArguments().size() != 2) {
                throw new CompileError(e.attrTrace().attrSource(),
                    imFuncName + " expects exactly two arguments");
            }
            LuaExpr left = e.getArguments().get(0).translateToLua(tr);
            LuaExpr right = e.getArguments().get(1).translateToLua(tr);
            if (e.getFunc() == tr.imTr.luaRawFloorDivIntFunc) {
                return LuaAst.LuaExprBinary(left, LuaAst.LuaOpFloorDiv(), right);
            }
            return LuaAst.LuaExprFunctionCallByName("math.fmod", LuaAst.LuaExprlist(left, right));
        }
        LuaFunction f = tr.luaFunc.getFor(e.getFunc());
        if ("I2S".equals(imFuncName) && isIntentionalThreadAbortCall(e)) {
            return LuaAst.LuaExprFunctionCallByName("error", LuaAst.LuaExprlist(
                LuaAst.LuaExprStringVal(WURST_ABORT_THREAD_SENTINEL),
                LuaAst.LuaExprIntVal("0")
            ));
        }
        if (ImTranslator.$DEBUG_PRINT.equals(imFuncName)) {
            f.setName("BJDebugMsg");
        } else if ("I2S".equals(imFuncName)) {
            f.setName("tostring");
        }
        return LuaAst.LuaExprFunctionCall(f, tr.translateExprList(e.getArguments()));
    }

    static boolean isRawNumericIntrinsic(ImFunction function, LuaTranslator tr) {
        return function == tr.imTr.luaRawFloorDivIntFunc
            || function == tr.imTr.luaRawFmodIntFunc
            || function == tr.imTr.luaRawFmodRealFunc;
    }

    private static boolean isIntentionalThreadAbortCall(ImFunctionCall e) {
        if (e.getArguments().size() != 1) {
            return false;
        }
        ImExpr arg = e.getArguments().get(0);
        if (!(arg instanceof ImOperatorCall)) {
            return false;
        }
        ImOperatorCall op = (ImOperatorCall) arg;
        if (op.getOp() != WurstOperator.DIV_INT) {
            return false;
        }
        if (op.getArguments().size() != 2) {
            return false;
        }
        ImExpr left = op.getArguments().get(0);
        ImExpr right = op.getArguments().get(1);
        return (left instanceof ImIntVal && ((ImIntVal) left).getValI() == 1)
            && (right instanceof ImIntVal && ((ImIntVal) right).getValI() == 0);
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
        LuaExpr res = LuaAst.LuaExprArrayAccess(
            LuaAst.LuaExprVarAccess(tr.fieldStorage(e.getVar())),
            LuaAst.LuaExprlist(e.getReceiver().translateToLua(tr)));
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
        ImMethod method = e.getMethod();
        if (LuaMethodCallLowering.canLowerDirectly(method)) {
            LuaExprlist args = LuaAst.LuaExprlist();
            args.add(e.getReceiver().translateToLua(tr));
            for (ImExpr arg : e.getArguments()) {
                args.add(arg.translateToLua(tr));
            }
            return LuaAst.LuaExprFunctionCall(tr.luaFunc.getFor(method.getImplementation()), args);
        }
        LuaExprlist args = LuaAst.LuaExprlist(e.getReceiver().translateToLua(tr));
        args.addAll(tr.translateExprList(e.getArguments()).removeAll());
        return LuaAst.LuaExprFunctionCall(tr.luaDispatchFunc.getFor(e.getMethod()), args);
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
            if (e.getOp() == WurstOperator.MOD_INT || e.getOp() == WurstOperator.MOD_REAL
                || e.getOp() == WurstOperator.JASS_MOD_INT || e.getOp() == WurstOperator.DIV_INT) {
                // LuaNativeLowering.lowerDivMod rewrites every DIV_INT/MOD_INT/MOD_REAL/JASS_MOD_INT
                // into a call against a portable IM function before the optimizer runs
                // (so it can be inlined/constant-folded there). It should never survive
                // to here - falling through to the default binary-op path below would
                // silently use Lua's floored // and % instead of the truncating/
                // Blizzard.j semantics Jass requires.
                throw new Error("unexpected " + e.getOp() + " in Lua backend - should have been lowered by LuaNativeLowering.lowerDivMod");
            }
            LuaExpr leftExpr = left.translateToLua(tr);
            LuaExpr rightExpr = right.translateToLua(tr);
            LuaOpBinary op = e.getOp().luaTranslateBinary();
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
        Optional<TupleFunc> tfo = Optional.empty();
        for (TupleFunc f : tr.tupleEqualsFuncs) {
            if (f.tupleType.equalsType(t)) {
                tfo = Optional.of(f);
                break;
            }
        }
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
        Optional<TupleFunc> tfo = Optional.empty();
        for (TupleFunc f : tr.tupleCopyFuncs) {
            if (f.tupleType.equalsType(t)) {
                tfo = Optional.of(f);
                break;
            }
        }
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

    public static LuaExpr translate(ImRealVal e, LuaTranslator tr) {
        return LuaAst.LuaExprRealVal(e.getValR());
    }

    public static LuaExpr translate(ImStatementExpr e, LuaTranslator tr) {
        // The statement-expr becomes an immediately-invoked closure. An exitwhen
        // that targets a loop OUTSIDE the statement-expr would emit 'break' inside
        // the closure, which is invalid Lua — fail loudly instead of emitting it.
        // (flatten() normally hoists statement-exprs so this should not occur.)
        e.getStatements().accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {
            @Override
            public void visit(ImLoop loop) {
                // breaks inside a nested loop are fine — do not descend
            }

            @Override
            public void visit(ImVarargLoop loop) {
                // breaks inside a nested loop are fine — do not descend
            }

            @Override
            public void visit(ImExitwhen exitwhen) {
                throw new de.peeeq.wurstscript.attributes.CompileError(e.attrTrace().attrSource(),
                    "Lua backend: cannot translate a loop exit inside a statement-expression "
                        + "(it would produce a 'break' inside a closure).");
            }
        });
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
        return LuaAst.LuaExprFieldAccess(
            LuaAst.LuaExprArrayAccess(LuaAst.LuaExprVarAccess(tr.objectClass),
                LuaAst.LuaExprlist(e.getObj().translateToLua(tr))),
            TYPE_ID);
    }

    public static LuaExpr translate(ImVarAccess e, LuaTranslator tr) {
        return LuaAst.LuaExprVarAccess(tr.luaVar.getFor(e.getVar()));
    }

    /** Primitive-typed arrays carry their Wurst defaults through metatables, so every read is raw. */
    public static LuaExpr translate(ImVarArrayAccess e, LuaTranslator tr) {
        return translateArrayAccessRaw(e, tr);
    }

    public static LuaExpr translateArrayAccessRaw(ImVarArrayAccess e, LuaTranslator tr) {
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
        // Reaching the backend means specialization never supplied a concrete type for this
        // dispatch and the code is reachable, since unreachable functions have been removed by now.
        throw new CompileError(imTypeVarDispatch.attrTrace().attrSource(),
            "Type class dispatch of " + imTypeVarDispatch.getTypeClassFunc().getName()
                + " could not be resolved for the Lua target: the concrete type is not available"
                + " where it is used.");
    }

    public static LuaExpr translate(ImCast imCast, LuaTranslator tr) {
        LuaExpr translated = imCast.getExpr().translateToLua(tr);
        if (TypesHelper.isIntType(imCast.getToType())) {
            if (TypesHelper.isStringType(imCast.getExpr().attrTyp())) {
                return LuaAst.LuaExprFunctionCall(tr.stringToIndexFunction, LuaAst.LuaExprlist(translated));
            }
            if (imCast.getExpr().attrTyp() instanceof ImClassType) {
                return LuaAst.LuaExprFunctionCall(tr.classToIndex, LuaAst.LuaExprlist(translated));
            }
            return LuaAst.LuaExprFunctionCall(tr.toIndexFunction, LuaAst.LuaExprlist(translated));
        } else if (imCast.getToType() instanceof ImClassType) {
            return LuaAst.LuaExprFunctionCall(tr.classFromIndex, LuaAst.LuaExprlist(translated));
        } else if (imCast.getToType() instanceof ImAnyType) {
            return LuaAst.LuaExprFunctionCall(tr.fromIndexFunction, LuaAst.LuaExprlist(translated));
        } else {
            return translated;
        }
    }
}
