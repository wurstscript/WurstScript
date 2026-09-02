package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.CompilerIntrinsics;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.AttrFuncDef;
import de.peeeq.wurstscript.attributes.AttrExprExpectedType;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.AttrImplicitParameter;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.OtherLink;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.validation.NamePreservation;
import de.peeeq.wurstscript.utils.Utils;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.peeeq.wurstscript.jassIm.JassIm.*;
import static de.peeeq.wurstscript.validation.WurstValidator.isTypeParamNewGeneric;

public class ExprTranslation {

    public static ImExpr translate(ExprBinary e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprUnary e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprBoolVal e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprFuncRef e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprIntVal e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprNull e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprRealVal e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprStringVal e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprThis e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprSuper e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(NameRef e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprCast e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(FunctionCall e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprIncomplete e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprNewObject e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    public static ImExpr translate(ExprTypeRef e, ImTranslator t, ImFunction f) {
        throw new CompileError(e, "Type reference " + Utils.printTypeExpr(e.getTyp()) + " cannot be used as a value.");
    }

    public static ImExpr translate(ExprInstanceOf e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    private static ImExpr wrapTranslation(Expr e, ImTranslator t, ImExpr translated) {
        WurstType actualType = e.attrTypRaw();
        WurstType expectedTypRaw = t.isLuaTarget()
            && actualType instanceof WurstTypeBoundTypeParam
            && e.getParent() instanceof Arguments
            ? AttrExprExpectedType.afterOverloading(e)
            : e.attrExpectedTypRaw();
        return wrapTranslation(e, t, translated, actualType, expectedTypRaw);
    }

    static ImExpr wrapLua(Element trace, ImTranslator t, ImExpr translated, WurstType actualType) {
        // Erased generic values are the one kind of Wurst value which can lose
        // its primitive default when represented in Lua.  Keep the
        // normalization available to callers which explicitly cross an
        // external boundary; ordinary Wurst expressions must not pay for it.
        if (t.isLuaTarget() && actualType instanceof WurstTypeBoundTypeParam) {
            WurstTypeBoundTypeParam wtb = (WurstTypeBoundTypeParam) actualType;

            @Nullable ImFunction ensureType = null;
            switch (wtb.getName()) {
                case "integer":
                    ensureType = t.ensureIntFunc;
                    break;
                case "string":
                    ensureType = t.ensureStrFunc;
                    break;
                case "boolean":
                    ensureType = t.ensureBoolFunc;
                    break;
                case "real":
                    ensureType = t.ensureRealFunc;
                    break;
            }
            if(ensureType != null) {
                // Lua already has the exact cheap operation needed for the
                // boolean case.  Equality with true preserves false while
                // mapping nil (and other non-true values) to false.
                if (ensureType == t.ensureBoolFunc) {
                    return ImOperatorCall(WurstOperator.EQ, ImExprs(
                        translated, ImBoolVal(true)));
                }
                return ImFunctionCall(trace, ensureType, ImTypeArguments(), JassIm.ImExprs(translated), false, CallType.NORMAL);
            }
        }
        return translated;
    }

    static ImExpr wrapTranslation(Expr e, ImTranslator t, ImExpr translated, WurstType actualType, WurstType expectedTypRaw) {
        return wrapTranslation(e, t, translated, actualType, expectedTypRaw,
            e.getParent() instanceof Indexes);
    }

    static ImExpr wrapTranslation(Element trace, ImTranslator t, ImExpr translated,
                                  WurstType actualType, WurstType expectedTypRaw) {
        return wrapTranslation(trace, t, translated, actualType, expectedTypRaw, false);
    }

    private static ImExpr wrapTranslation(Element trace, ImTranslator t, ImExpr translated,
                                          WurstType actualType, WurstType expectedTypRaw,
                                          boolean indexContext) {
        ImFunction toIndex = null;
        ImFunction fromIndex = null;
        if (actualType instanceof WurstTypeBoundTypeParam) {
            WurstTypeBoundTypeParam wtb = (WurstTypeBoundTypeParam) actualType;
            FuncDef fromIndexFunc = wtb.getFromIndex();
            if (fromIndexFunc != null) {
                fromIndex = t.getFuncFor(fromIndexFunc);
            }
        }
        if (expectedTypRaw instanceof WurstTypeBoundTypeParam) {
            WurstTypeBoundTypeParam wtb = (WurstTypeBoundTypeParam) expectedTypRaw;
            FuncDef toIndexFunc = wtb.getToIndex();
            if (toIndexFunc != null) {
                toIndex = t.getFuncFor(toIndexFunc);
            }
        }

//        System.out.println("CAll " + Utils.prettyPrintWithLine(trace));
//        System.out.println("  actualType = " + actualType.getFullName());
//        System.out.println("  expectedTypRaw = " + expectedTypRaw.getFullName());

        if (toIndex != null && fromIndex != null) {
//            System.out.println("  --> cancel");
            // the two conversions cancel each other out
            return wrapLua(trace, t, translated, actualType);
        } else if (fromIndex != null) {
//            System.out.println("  --> fromIndex");
            if(t.isLuaTarget()) {
                translated = ImFunctionCall(trace, t.ensureIntFunc, ImTypeArguments(), JassIm.ImExprs(translated), false, CallType.NORMAL);
            }
            // no ensure type necessary here, because the fromIndex function is already type safe
            return ImFunctionCall(trace, fromIndex, ImTypeArguments(), JassIm.ImExprs(translated), false, CallType.NORMAL);
        } else if (toIndex != null) {
//            System.out.println("  --> toIndex");
            return wrapLua(trace, t, ImFunctionCall(trace, toIndex, ImTypeArguments(), JassIm.ImExprs(translated), false, CallType.NORMAL), actualType);
        }
        // Preserve Wurst's primitive defaults when an erased generic value is
        // consumed by a concrete primitive expression. Generic-to-generic
        // propagation remains raw and is normalized only at its eventual
        // concrete/native boundary.
        if (actualType instanceof WurstTypeBoundTypeParam
            && !(expectedTypRaw instanceof WurstTypeBoundTypeParam)
            && !(expectedTypRaw instanceof WurstTypeTypeParam)
            && (isPrimitiveType(expectedTypRaw) || indexContext)) {
            return wrapLua(trace, t, translated, actualType);
        }
        return translated;
    }

    public static ImExpr translateIntern(ExprBinary e, ImTranslator t, ImFunction f) {
        WurstOperator op = e.getOp();
        FuncLink overloadedOperator = e.attrFuncLink();
        ImExpr left = translateConcatOperand(e, e.getLeft(), t, f, overloadedOperator);
        ImExpr right = translateConcatOperand(e, e.getRight(), t, f, overloadedOperator);
        if (overloadedOperator == null) {
            // A built-in operator can leave both operands with the same erased
            // generic type. In that case there is no concrete expected type to
            // trigger wrapTranslation, but Lua still needs each operand's
            // primitive default restored before applying the operator.
            left = normalizeBuiltinOperand(e.getLeft(), left, t);
            right = normalizeBuiltinOperand(e.getRight(), right, t);
        }
        if (op == WurstOperator.PLUS && overloadedOperator == null) {
            left = wrapImplicitToString(e, e.getLeft(), left, t);
            right = wrapImplicitToString(e, e.getRight(), right, t);
        }
        if (overloadedOperator != null) {
            // overloaded operator
            ImFunction calledFunc = t.getFuncFor(e.attrFuncDef());
            return ImFunctionCall(e, calledFunc, ImTypeArguments(), ImExprs(left, right), false, CallType.NORMAL);
        }
        if (op == WurstOperator.DIV_REAL) {
            if (Utils.isJassCode(e)) {
                if (e.getLeft().attrTyp().isSubtypeOf(WurstTypeInt.instance(), e)
                        && e.getRight().attrTyp().isSubtypeOf(WurstTypeInt.instance(), e)) {
                    // in jass when we have int1 / int2 this actually means int1
                    // div int2
                    op = WurstOperator.DIV_INT;
                }
            } else {
                if (e.getLeft().attrTyp().isSubtypeOf(WurstTypeInt.instance(), e)
                        && e.getRight().attrTyp().isSubtypeOf(WurstTypeInt.instance(), e)) {
                    // we want a real division but have 2 ints so we need to
                    // multiply with 1.0
                    // TODO is this really needed or handled in IM->Jass
                    // translation?
                    left = ImOperatorCall(WurstOperator.MULT, ImExprs(left, ImRealVal("1.")));
                }
            }
        }
        return ImOperatorCall(op, ImExprs(left, right));
    }

    private static ImExpr normalizeBuiltinOperand(Expr operand, ImExpr translated, ImTranslator t) {
        if (!t.isLuaTarget() || !(operand.attrTypRaw() instanceof WurstTypeBoundTypeParam)
            || isAlreadyTypeAssured(translated, t)) {
            return translated;
        }
        return wrapLua(operand, t, translated, operand.attrTypRaw());
    }

    private static ImExpr translateConcatOperand(ExprBinary concat, Expr operand, ImTranslator t, ImFunction f,
                                                 @Nullable FuncLink overloadedOperator) {
        if (concat.getOp() == WurstOperator.PLUS && overloadedOperator == null
            && isCompositeExpectedTypeExpression(operand)) {
            FuncLink toString = AttrFuncDef.implicitToStringForConcatOperand(concat, operand);
            if (toString != null) {
                FunctionSignature signature = FunctionSignature.fromNameLink(toString);
                return translateWithExpectedType(operand, t, f, signature.getReceiverType());
            }
        }
        return operand.imTranslateExpr(t, f);
    }

    private static ImExpr wrapImplicitToString(ExprBinary concat, Expr operand, ImExpr translated,
                                               ImTranslator t) {
        FuncLink toString = AttrFuncDef.implicitToStringForConcatOperand(concat, operand);
        if (toString == null) {
            return translated;
        }

        FunctionDefinition calledFunc = toString.getDef().attrRealFuncDef();
        FunctionSignature signature = FunctionSignature.fromNameLink(toString);
        translated = wrapTranslation(operand, t, translated, operand.attrTypRaw(), signature.getReceiverType());
        if (calledFunc instanceof FuncDef
                && !((FuncDef) calledFunc).attrIsStatic()
                && operand.attrTyp().allowsDynamicDispatch()) {
            ImMethod method = t.getMethodFor((FuncDef) calledFunc);
            ImTypeArguments typeArguments = getFunctionCallTypeArguments(
                t, signature, operand, method.getImplementation().getTypeVariables());
            return ImMethodCall(operand, method, typeArguments, translated, ImExprs(), false);
        }

        ImFunction calledImFunc = t.getFuncFor(calledFunc);
        ImTypeArguments typeArguments = getFunctionCallTypeArguments(
            t, signature, operand, calledImFunc.getTypeVariables());
        return ImFunctionCall(operand, calledImFunc, typeArguments, ImExprs(translated), false, CallType.NORMAL);
    }

    public static ImExpr translateIntern(ExprUnary e, ImTranslator t, ImFunction f) {
        return ImOperatorCall(e.getOpU(), ImExprs(e.getRight().imTranslateExpr(t, f)));
    }

    public static ImExpr translateIntern(ExprBoolVal e, ImTranslator t, ImFunction f) {
        return JassIm.ImBoolVal(e.getValB());
    }

    public static ImExpr translateIntern(ExprFuncRef e, ImTranslator t, ImFunction f) {
        ImFunction func = t.getFuncFor(e.attrFuncDef());
        return ImFuncRef(e, func);
    }

    public static ImExpr translateIntern(ExprIntVal e, ImTranslator t, ImFunction f) {
        if (e.attrExpectedTyp() instanceof WurstTypeReal) {
            // translate differently when real is expected
            return ImRealVal(e.getValI() + ".");
        }

        return ImIntVal(e.getValI());
    }

    public static ImExpr translateIntern(ExprNull e, ImTranslator t, ImFunction f) {
        WurstType expectedTypeRaw = e.attrExpectedTypRaw();
        if (expectedTypeRaw instanceof WurstTypeUnknown) {
            e.addError("Cannot use 'null' in this context.");
        }
        return ImNull(expectedTypeRaw.imTranslateType(t));
    }

    public static ImExpr translateIntern(ExprRealVal e, ImTranslator t, ImFunction f) {
        return ImRealVal(e.getValR());
    }

    public static ImExpr translateIntern(ExprStringVal e, ImTranslator t, ImFunction f) {
        return ImStringVal(e.getValS());
    }

    public static ImExpr translateIntern(ExprThis e, ImTranslator t, ImFunction f) {
        ImVar var = t.getThisVar(f, e);
        return ImVarAccess(var);
    }

    public static ImExpr translateIntern(ExprSuper e, ImTranslator t, ImFunction f) {
        ImVar var = t.getThisVar(f, e);
        return ImVarAccess(var);
    }

    public static ImExpr translateIntern(NameRef e, ImTranslator t, ImFunction f) {
        if (e instanceof ExprMemberVarQuestionDot) {
            return translateNullSafeMemberVar((ExprMemberVarQuestionDot) e, t, f);
        }
        return translateNameDef(e, t, f);
    }

    private static ImExpr translateNullSafeMemberVar(ExprMemberVarQuestionDot e, ImTranslator t, ImFunction f) {
        NameLink link = e.attrNameLink();
        if (link == null || link instanceof OtherLink || !(link.getDef() instanceof VarDef)) {
            throw new CompileError(e.getSource(),
                "The null-safe operator '?.' is not supported for this kind of member access.");
        }
        Expr left = e.getLeft();
        ImVar fieldVar = t.getVarFor((VarDef) link.getDef());

        ImVar tempVar = JassIm.ImVar(left, left.attrTyp().imTranslateType(t), "receiver", false);
        f.getLocals().add(tempVar);
        ImStmts stmts = JassIm.ImStmts(ImSet(e, ImVarAccess(tempVar), left.imTranslateExpr(t, f)));
        ImExpr access = JassIm.ImMemberAccess(e, ImVarAccess(tempVar), JassIm.ImTypeArguments(),
            fieldVar, JassIm.ImExprs());
        return nullSafeGuard(e, t, f, stmts, tempVar, left.attrTyp(), access);
    }

    private static ImExpr translateNameDef(NameRef e, ImTranslator t, ImFunction f) throws CompileError {
        NameLink link = e.attrNameLink();
        if (link instanceof OtherLink) {
            return ((OtherLink) link).translate(e, t, f);
        }
        NameDef decl = link == null ? null : link.getDef();
        if (decl == null) {
            // should only happen with gg_ variables
            if (!t.isEclipseMode()) {
                e.addError("Translation Error: Could not find definition of " + e.getVarName() + ".");
            }
            return ImHelper.nullExpr();
        }
        if (decl instanceof VarDef) {
            VarDef varDef = (VarDef) decl;

            ImVar v = t.getVarFor(varDef);
            @Nullable FuncLink indexGetOverload = getIndexGetOverload(e, link);

            if (e.attrImplicitParameter() instanceof Expr) {
                // we have implicit parameter
                // e.g. "someObject.someField"
                Expr implicitParam = (Expr) e.attrImplicitParameter();

                if (implicitParam.attrTyp() instanceof WurstTypeTuple) {
                    WurstTypeTuple tupleType = (WurstTypeTuple) implicitParam.attrTyp();
                    if (e instanceof ExprMemberVar) {
                        ExprMemberVar e2 = (ExprMemberVar) e;
                        return translateTupleSelection(t, f, e2);
                    } else {
                        throw new CompileError(e.getSource(), "Cannot create tuple access");
                    }
                }

                if (e instanceof AstElementWithIndexes) {
                    if (indexGetOverload != null) {
                        AstElementWithIndexes withIndexes = (AstElementWithIndexes) e;
                        ImExpr receiver = JassIm.ImMemberAccess(e, implicitParam.imTranslateExpr(t, f), JassIm.ImTypeArguments(), v, JassIm.ImExprs());
                        ImExpr index = withIndexes.getIndexes().get(0).imTranslateExpr(t, f);
                        ImFunction calledFunc = t.getFuncFor(indexGetOverload.getDef());
                        return ImFunctionCall(e, calledFunc, ImTypeArguments(), ImExprs(receiver, index), false, CallType.NORMAL);
                    }
                    ImExpr index1 = implicitParam.imTranslateExpr(t, f);
                    ImExpr index2 = ((AstElementWithIndexes) e).getIndexes().get(0).imTranslateExpr(t, f);
                    return JassIm.ImMemberAccess(e, index1, JassIm.ImTypeArguments(), v, JassIm.ImExprs(index2));
                } else {
                    ImExpr index = implicitParam.imTranslateExpr(t, f);
                    return JassIm.ImMemberAccess(e, index, JassIm.ImTypeArguments(), v, JassIm.ImExprs());
                }
            } else {
                // direct var access
                if (e instanceof AstElementWithIndexes) {
                    if (indexGetOverload != null) {
                        AstElementWithIndexes withIndexes = (AstElementWithIndexes) e;
                        ImExpr receiver = ImVarAccess(v);
                        ImExpr index = withIndexes.getIndexes().get(0).imTranslateExpr(t, f);
                        ImFunction calledFunc = t.getFuncFor(indexGetOverload.getDef());
                        return ImFunctionCall(e, calledFunc, ImTypeArguments(), ImExprs(receiver, index), false, CallType.NORMAL);
                    }
                    // direct access array var
                    AstElementWithIndexes withIndexes = (AstElementWithIndexes) e;
                    if (withIndexes.getIndexes().size() > 1) {
                        throw new CompileError(e.getSource(), "More than one index is not supported.");
                    }
                    ImExpr index = withIndexes.getIndexes().get(0).imTranslateExpr(t, f);
                    return ImVarArrayAccess(e, v, JassIm.ImExprs(index));
                } else {
                    // not an array var
                    return ImVarAccess(v);

                }
            }
        } else if (decl instanceof EnumMember) {
            EnumMember enumMember = (EnumMember) decl;
            int id = t.getEnumMemberId(enumMember);
            return ImIntVal(id);
        } else {
            throw new CompileError(e.getSource(), "Cannot translate reference to " + Utils.printElement(decl));
        }
    }

    private static ImExpr translateTupleSelection(ImTranslator t, ImFunction f, ExprMemberVar mv) {
        ImExpr left = mv.getLeft().imTranslateExpr(t, f);
        WParameter tupleParam = (WParameter) mv.attrNameDef();
        WParameters tupleParams = (WParameters) tupleParam.getParent();
        int tupleIndex = tupleParams.indexOf(tupleParam);
        if (left instanceof ImLExpr) {
            return ImTupleSelection(left, tupleIndex);
        } else {
            // if tupleExpr is not an l-value (e.g. foo().x)
            // store result in intermediate variable first:
            ImVar v = ImVar(left.attrTrace(), left.attrTyp(), "temp_tuple", false);
            f.getLocals().add(v);
            return JassIm.ImStatementExpr(
                    JassIm.ImStmts(
                            ImSet(left.attrTrace(), ImVarAccess(v), left)
                    ),
                    ImTupleSelection(ImVarAccess(v), tupleIndex)
            );
        }
    }

    /*
    private static ImExpr translateTupleSelection(ImTranslator t, ImFunction f, ExprMemberVar mv) {
        List<WParameter> indexes = new ArrayList<>();

        Expr expr = mv;
        while (true) {
            if (expr instanceof ExprMemberVar) {
                ExprMemberVar mv2 = (ExprMemberVar) expr;
                Expr left = mv2.getLeft();
                if (left.attrTyp() instanceof WurstTypeTuple) {
                    indexes.add(0, (WParameter) mv2.attrNameDef());
                    expr = left;
                    continue;
                }
            }
            break;
        }

        WurstTypeTuple tt = (WurstTypeTuple) expr.attrTyp();
        int tupleIndex = 0;
        WurstType resultTupleType = null;
        for (int i = 0; i < indexes.size(); i++) {
            WParameter param = indexes.get(i);
            TupleDef tdef = tt.getTupleDef();
            int pos = 0;
            while (tdef.getParameters().get(pos) != param) {
                tupleIndex += tupleSize(tdef.getParameters().get(pos).getTyp().attrTyp());
                pos++;
            }
            resultTupleType = tdef.getParameters().get(pos).getTyp().attrTyp();
            if (i < indexes.size() - 1) {
                tt = (WurstTypeTuple) tdef.getParameters().get(pos).getTyp().attrTyp();
            }
        }
        ImExpr exprTr = expr.imTranslateExpr(t, f);
        if (resultTupleType instanceof WurstTypeTuple) {
            // if the result is a tuple, create it:
            int tupleSize = tupleSize(resultTupleType);

            if (exprTr instanceof ImLExpr
                    && (exprTr.attrPurity() instanceof Pure || exprTr.attrPurity() instanceof ReadsGlobals)) {
                ImExprs exprs = JassIm.ImExprs();
                for (int i = 0; i < tupleSize; i++) {
                    exprs.add(ImTupleSelection((ImLExpr) exprTr.copy(), tupleIndex + i));
                }
                return ImTupleExpr(exprs);
            } else {
                ImVar temp = JassIm.ImVar(expr, exprTr.attrTyp(), "temp", false);
                // for impure expressions use a temporary:
                f.getLocals().add(temp);

                ImExprs exprs = JassIm.ImExprs();
                for (int i = 0; i < tupleSize; i++) {
                    // TODO use temporary var
                    exprs.add(ImTupleSelection(JassIm.ImVarAccess(temp), tupleIndex + i));
                }
                return JassIm.ImStatementExpr(JassIm.ImStmts(ImSet(expr, ImVarAccess(temp), exprTr)), ImTupleExpr(exprs));
            }
        } else {
            if (exprTr instanceof ImLExpr) {
                return ImTupleSelection((ImLExpr) exprTr, tupleIndex);
            } else {
                // if tupleExpr is not an l-value (e.g. foo().x)
                // store result in intermediate variable first:
                ImVar v = ImVar(exprTr.attrTrace(), exprTr.attrTyp(), "temp_tuple", false);
                f.getLocals().add(v);
                return JassIm.ImStatementExpr(
                        JassIm.ImStmts(
                                ImSet(exprTr.attrTrace(), ImVarAccess(v), exprTr)
                        ),
                        ImTupleSelection(ImVarAccess(v), tupleIndex)
                );
            }
        }
    }
    */

    /**
     * counts the components of a tuple (including nested)
     */
    private static int tupleSize(WurstType t) {
        if (t instanceof WurstTypeTuple) {
            WurstTypeTuple tt = (WurstTypeTuple) t;
            int sum = 0;
            for (WParameter p : tt.getTupleDef().getParameters()) {
                sum += tupleSize(p.getTyp().attrTyp());
            }
            return sum;
        }
        // all other types have size 1
        return 1;
    }

    public static ImExpr translateIntern(ExprCast e, ImTranslator t, ImFunction f) {
        ImExpr et = e.getExpr().imTranslateExpr(t, f);
        ImType toType = e.getTyp().attrTyp().imTranslateType(t);
        return JassIm.ImCast(et, toType);
    }

    public static ImExpr translateIntern(FunctionCall e, ImTranslator t, ImFunction f) {
        if (e instanceof ExprMemberMethodDotDot) {
            return translateFunctionCall(e, t, f, true, false);
        } else if (e instanceof ExprMemberMethodQuestionDot) {
            return translateFunctionCall(e, t, f, false, true);
        } else {
            return translateFunctionCall(e, t, f, false, false);
        }
    }

    /**
     * Translates {@code T.f(args)} into a dispatch through T's type class binding.
     * <p>
     * The concrete implementation is not known here, because T is still abstract inside the
     * generic. Generic elimination substitutes T and rewrites this node into a direct call to the
     * function supplied by the instance chosen for the substituted type.
     */
    private static ImExpr translateTypeClassDispatch(FunctionCall e, ImTranslator t, ImFunction f) {
        WurstType receiver = ((HasReceiver) e).getLeft().attrTyp();
        FunctionDefinition called = e.attrFuncDef();
        if (!(called instanceof FuncDef method)) {
            throw new CompileError(e.attrSource(),
                "Type class requirement " + e.getFuncName() + " must be a function of the bound interface.");
        }
        ImExprs args = JassIm.ImExprs();
        for (Expr arg : e.getArgs()) {
            args.add(arg.imTranslateExpr(t, f));
        }
        ImTypeClassFunc requirement = t.getTypeClassFunc(method);
        if (receiver instanceof WurstTypeBoundTypeParam bound) {
            return translateModuleParamDispatch(e, bound, requirement, args, t);
        }
        return JassIm.ImTypeVarDispatch(e, requirement, args,
            t.getTypeVar(((WurstTypeTypeParam) receiver).getDef()));
    }

    /**
     * Dispatches a requirement called on a module instantiation's type parameter, which stands for
     * the argument the using class supplied rather than for a variable of its own.
     * <p>
     * When that argument is itself a type parameter the dispatch is on the using class's variable,
     * exactly as if the call had been written there. Otherwise the instance is already determined:
     * a module used with a concrete argument leaves no variable for generic elimination to
     * substitute, so the implementation is chosen here.
     */
    private static ImExpr translateModuleParamDispatch(FunctionCall e, WurstTypeBoundTypeParam bound,
            ImTypeClassFunc requirement, ImExprs args, ImTranslator t) {
        WurstType argument = bound.getBaseType().normalize();
        if (argument instanceof WurstTypeTypeParam tp) {
            return JassIm.ImTypeVarDispatch(e, requirement, args, t.getTypeVar(tp.getDef()));
        }
        Either<ImMethod, ImFunction> impl = bound.imTypeClassBinding(t).get(requirement);
        if (impl == null) {
            throw new CompileError(e.attrSource(),
                "No type class instance supplies " + e.getFuncName() + " for " + argument + ".");
        }
        ImFunction target = impl.isRight() ? impl.get() : impl.getLeft().getImplementation();
        return ImFunctionCall(e, target, ImTypeArguments(), args, false, CallType.NORMAL);
    }

    private static ImExpr translateFunctionCall(FunctionCall e, ImTranslator t, ImFunction f, boolean returnReveiver, boolean nullSafe) {

        if (e instanceof ExprFunctionCall call && CompilerIntrinsics.isNew(call)) {
            ImType targetType = call.getTypeArgs().get(0).attrTyp().imTranslateType(t);
            ImTypeArguments typeArguments = JassIm.ImTypeArguments(
                JassIm.ImTypeArgument(targetType, new HashMap<>()));
            return ImFunctionCall(call, t.getGenericNewMarker(), typeArguments,
                JassIm.ImExprs(), false, CallType.NORMAL);
        }

        if (AttrImplicitParameter.isTypeClassDispatch(e)) {
            return translateTypeClassDispatch(e, t, f);
        }

        if (e.getFuncName().equals("getStackTraceString") && e.attrImplicitParameter() instanceof NoExpr
            && e.getArgs().size() == 0) {
            // special built-in error function
            return JassIm.ImGetStackTrace();
        }

        if (e.getFuncName().equals("ExecuteFunc")) {
            ExprStringVal s = (ExprStringVal) e.getArgs().get(0);
            String exFunc = s.getValS();
            NameLink func = Utils.getFirst(e.lookupFuncs(exFunc));
            ImFunction executedFunc = t.getFuncFor((TranslatedToImFunction) func.getDef());
            NamePreservation.preserve(executedFunc);
            return ImFunctionCall(e, executedFunc, ImTypeArguments(), JassIm.ImExprs(), true, CallType.EXECUTE);
        }

        if (e.getFuncName().equals("compiletime")
            && e.attrImplicitParameter() instanceof NoExpr
            && e.getArgs().size() == 1) {
            // special compiletime-expression
            return JassIm.ImCompiletimeExpr(e, e.getArgs().get(0).imTranslateExpr(t, f), t.getCompiletimeExpressionsOrder(e));
        }

        List<Expr> arguments = Lists.newArrayList(e.getArgs());
        Expr leftExpr = null;

        FunctionDefinition calledFunc = e.attrFuncDef();

        if (e.attrImplicitParameter() instanceof Expr) {
            // keep implicit parameter
            leftExpr = (Expr) e.attrImplicitParameter();
        }

        // get real func def (override of module function)
        boolean useRealFuncDef = true;
        if (e instanceof ExprMemberMethod) {
            ExprMemberMethod exprMemberMethod = (ExprMemberMethod) e;
            WurstType left = exprMemberMethod.getLeft().attrTyp();
            if (left instanceof WurstTypeModuleInstanciation) {
                // if we have a call like A.foo() and A is a module,
                // use this function
                useRealFuncDef = false;
            }
        }

        if (calledFunc == null) {
            // this must be an ignored function
            return ImHelper.nullExpr();
        }

        if (useRealFuncDef) {
            calledFunc = calledFunc.attrRealFuncDef();
        }

        boolean dynamicDispatch = false;

        if (leftExpr instanceof ExprThis && calledFunc == e.attrNearestFuncDef()) {
            // recursive self calls are bound statically
            // (necessary because jass does not allow mutually recursive calls)
            dynamicDispatch = false;
        } else if (leftExpr != null
            && isCalledOnDynamicRef(e)
            && calledFunc instanceof FuncDef
            && !((FuncDef) calledFunc).attrIsStatic()) {
            // only instance methods participate in dispatch
            dynamicDispatch = true;
        }

        // logging
        if (calledFunc instanceof FuncDef) {
            FuncDef fd = (FuncDef) calledFunc;
            if (WLogger.isTraceEnabled()) WLogger.trace("[DISPATCH] call " + fd.getName()
                + " isStatic=" + fd.attrIsStatic()
                + " dynCtx=" + isCalledOnDynamicRef(e)
                + " -> dynamicDispatch=" + dynamicDispatch);
        } else {
            if (WLogger.isTraceEnabled()) WLogger.trace("[DISPATCH] call " + calledFunc.getName()
                + " (non-FuncDef)"
                + " dynCtx=" + isCalledOnDynamicRef(e)
                + " -> dynamicDispatch=" + dynamicDispatch);
        }

        ImFunction directFunc = null;
        if (!dynamicDispatch && !(calledFunc instanceof TupleDef)) {
            directFunc = t.getFuncFor(calledFunc);
        }

        ImExpr receiver = leftExpr == null ? null : leftExpr.imTranslateExpr(t, f);
        boolean normalizeAtBoundary = directFunc != null && isLuaExternalBoundary(directFunc);
        FunctionSignature selectedSignature = t.isLuaTarget() ? e.attrFunctionSignature() : null;
        ImExprs imArgs = translateExprs(arguments, t, f, normalizeAtBoundary, selectedSignature);

        if (calledFunc instanceof TupleDef) {
            // creating a new tuple...
            return ImTupleExpr(imArgs);
        }

        ImStmts stmts = null;
        ImVar tempVar = null;
        if (returnReveiver || nullSafe) {
            if (leftExpr == null) {
                throw new Error("impossible");
            }
            tempVar = JassIm.ImVar(leftExpr, leftExpr.attrTyp().imTranslateType(t), "receiver", false);
            f.getLocals().add(tempVar);
            stmts = JassIm.ImStmts(ImSet(e, ImVarAccess(tempVar), receiver));
            receiver = JassIm.ImVarAccess(tempVar);
        }

        ImExpr call;
        if (dynamicDispatch) {
            ImMethod method = t.getMethodFor((FuncDef) calledFunc);
            ImTypeArguments typeArguments = getFunctionCallTypeArguments(
                t, e.attrFunctionSignature(), e, method.getImplementation().getTypeVariables());
            call = ImMethodCall(e, method, typeArguments, receiver, imArgs, false);
        } else {
            ImFunction calledImFunc = directFunc;
            if (receiver != null) {
                imArgs.add(0, receiver);
            }
            ImTypeArguments typeArguments = getFunctionCallTypeArguments(
                t, e.attrFunctionSignature(), e, calledImFunc.getTypeVariables());
            call = ImFunctionCall(e, calledImFunc, typeArguments, imArgs, false, CallType.NORMAL);
        }

        if (returnReveiver) {
            if (stmts == null) {
                throw new Error("impossible");
            }
            stmts.add(call);
            return JassIm.ImStatementExpr(stmts, JassIm.ImVarAccess(tempVar));
        } else if (nullSafe) {
            if (stmts == null || leftExpr == null) {
                throw new Error("impossible");
            }
            // guard the call so that it (including argument evaluation) only
            // happens when the receiver is not null
            return nullSafeGuard(e, t, f, stmts, tempVar, leftExpr.attrTyp(), call);
        } else {
            return call;
        }
    }

    /**
     * Completes the lowering of a null-safe access {@code a?.x} / {@code a?.foo()}:
     * {@code stmts} already assigns the receiver to {@code tempVar}; the guarded
     * {@code access} is only evaluated when the receiver is not null, otherwise
     * the result is null (or the access is skipped entirely in statement position).
     */
    private static ImExpr nullSafeGuard(Expr e, ImTranslator t, ImFunction f,
                                        ImStmts stmts, ImVar tempVar, WurstType receiverType, ImExpr access) {
        ImExpr notNull = JassIm.ImOperatorCall(WurstOperator.NOTEQ,
            JassIm.ImExprs(ImVarAccess(tempVar), JassIm.ImNull(receiverType.imTranslateType(t))));
        WurstType resultType = e.attrTyp();
        boolean resultUsed = !(resultType instanceof WurstTypeVoid)
            && !(e.getParent() instanceof WStatements);
        if (resultUsed) {
            ImVar resultVar = JassIm.ImVar(e, resultType.imTranslateType(t), "nullSafeResult", false);
            f.getLocals().add(resultVar);
            stmts.add(JassIm.ImIf(e, notNull,
                JassIm.ImStmts(ImSet(e, ImVarAccess(resultVar), access)),
                JassIm.ImStmts(ImSet(e, ImVarAccess(resultVar), JassIm.ImNull(resultType.imTranslateType(t))))));
            return JassIm.ImStatementExpr(stmts, ImVarAccess(resultVar));
        } else {
            stmts.add(JassIm.ImIf(e, notNull, JassIm.ImStmts(access), JassIm.ImStmts()));
            return JassIm.ImStatementExpr(stmts, ImHelper.nullExpr());
        }
    }

    private static ImTypeArguments getFunctionCallTypeArguments(ImTranslator tr, FunctionSignature sig, Element location, ImTypeVars typeVariables) {
        ImTypeArguments res = ImTypeArguments();
        VariableBinding mapping = sig.getMapping();

        for (ImTypeVar tv : typeVariables) {
            TypeParamDef tp = tr.getTypeParamDef(tv);
            if (tp == null) {
                // Should not happen, but be defensive: if we cannot map back, just pass through
                Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> typeClassBinding = new HashMap<>();
                res.add(ImTypeArgument(JassIm.ImTypeVarRef(tv), typeClassBinding));
                continue;
            }

            Option<WurstTypeBoundTypeParam> to = mapping.get(tp);

            if (to.isEmpty()) {
                if (isTypeParamNewGeneric(tp)) {
                    ImType tvType = JassIm.ImTypeVarRef(tr.getTypeVar(tp));
                    res.add(ImTypeArgument(tvType, new HashMap<>()));
                    continue;
                }
                throw new CompileError(location, "Type variable " + tp.getName() + " not bound in mapping.");
            }

            WurstTypeBoundTypeParam t = to.get();

            if (!t.isTemplateTypeParameter()) {
                continue;
            }

            res.add(t.imTranslateToTypeArgument(tr));
        }

        return res;
    }


    private static boolean isCalledOnDynamicRef(FunctionCall e) {
        if (e instanceof ExprMemberMethod) {
            ExprMemberMethod mm = (ExprMemberMethod) e;
            return mm.getLeft().attrTyp().allowsDynamicDispatch();
        } else return e.attrIsDynamicContext();
    }

    private static ImExprs translateExprs(List<Expr> arguments, ImTranslator t, ImFunction f) {
        return translateExprs(arguments, t, f, false);
    }

    private static ImExprs translateExprs(List<Expr> arguments, ImTranslator t, ImFunction f,
                                          boolean externalBoundary) {
        return translateExprs(arguments, t, f, externalBoundary, null);
    }

    private static ImExprs translateExprs(List<Expr> arguments, ImTranslator t, ImFunction f,
                                          boolean externalBoundary, @Nullable FunctionSignature selectedSignature) {
        ImExprs result = ImExprs();
        for (int i = 0; i < arguments.size(); i++) {
            Expr e = arguments.get(i);
            WurstType expectedType = selectedSignature != null && i < selectedSignature.getMaxNumParams()
                ? selectedSignature.getParamType(i)
                : null;
            ImExpr translated = expectedType != null && isCompositeExpectedTypeExpression(e)
                ? translateWithExpectedType(e, t, f, expectedType)
                : e.imTranslateExpr(t, f);
            if (externalBoundary) {
                translated = wrapLuaAtExternalBoundary(e, t, translated);
            }
            result.add(translated);
        }
        return result;
    }

    private static boolean isCompositeExpectedTypeExpression(Expr e) {
        return e instanceof ExprIfElse || e instanceof ExprUnary;
    }

    private static boolean isLuaExternalBoundary(ImFunction function) {
        return function.isNative() || function.isBj() || function.isExtern();
    }

    private static ImExpr wrapLuaAtExternalBoundary(Expr source, ImTranslator t, ImExpr translated) {
        WurstType actualType = source.attrTypRaw();
        // Ordinary Wurst locals and literals already have their normal Lua
        // representation. Only values which can lose their primitive default
        // in Lua need normalization: raw array reads crossing into untyped
        // code. Erased generic values are normalized by wrapTranslation when
        // a concrete primitive context consumes them.
        if (!(translated instanceof ImVarArrayAccess)) {
            return translated;
        }
        WurstType normalized = actualType.normalize();
        ImFunction ensureType = null;
        if (normalized instanceof WurstTypeInt) {
            ensureType = t.ensureIntFunc;
        } else if (normalized instanceof WurstTypeBool) {
            ensureType = t.ensureBoolFunc;
        } else if (normalized instanceof WurstTypeReal) {
            ensureType = t.ensureRealFunc;
        } else if (normalized instanceof WurstTypeString) {
            ensureType = t.ensureStrFunc;
        }
        if (ensureType == null) {
            return translated;
        }
        if (ensureType == t.ensureBoolFunc) {
            return ImOperatorCall(WurstOperator.EQ, ImExprs(
                translated, ImBoolVal(true)));
        }
        return ImFunctionCall(source, ensureType, ImTypeArguments(), ImExprs(translated), false, CallType.NORMAL);
    }

    private static boolean isPrimitiveType(WurstType type) {
        WurstType normalized = type.normalize();
        return normalized instanceof WurstTypeInt
            || normalized instanceof WurstTypeBool
            || normalized instanceof WurstTypeReal
            || normalized instanceof WurstTypeString;
    }

    public static ImExpr translateIntern(ExprIncomplete e, ImTranslator t, ImFunction f) {
        throw new CompileError(e.getSource(), "Incomplete expression.");
    }

    public static ImExpr translateIntern(ExprNewObject e, ImTranslator t, ImFunction f) {
        ConstructorDef constructorFunc = e.attrConstructorDef();
        ImFunction constructorImFunc = t.getConstructNewFunc(constructorFunc);
        FunctionSignature sig = e.attrFunctionSignature();
        WurstTypeClass wurstType = (WurstTypeClass) e.attrTyp();
        ImClass imClass = t.getClassFor(wurstType.getClassDef());
        ImTypeArguments typeArgs = getFunctionCallTypeArguments(t, sig, e, imClass.getTypeVariables());
        FunctionSignature selectedSignature = t.isLuaTarget() ? sig : null;
        return ImFunctionCall(e, constructorImFunc, typeArgs,
            translateExprs(e.getArgs(), t, f, false, selectedSignature), false, CallType.NORMAL);
    }

    public static ImExprOpt translate(NoExpr e, ImTranslator translator, ImFunction f) {
        return JassIm.ImNoExpr();
    }

    public static ImExpr translateIntern(ExprInstanceOf e, ImTranslator translator, ImFunction f) {
        WurstType targetType = e.getTyp().attrTyp();
        ImType imTargetType = targetType.imTranslateType(translator);
        if (imTargetType instanceof ImClassType) {
            return JassIm.ImInstanceof(e.getExpr().imTranslateExpr(translator, f), (ImClassType) imTargetType);
        }
        throw new Error("Cannot compile instanceof " + targetType);
    }

    public static ImExpr translate(ExprTypeId e, ImTranslator translator, ImFunction f) {
        WurstType leftType = e.getLeft().attrTyp();
        ImType imLeftType = leftType.imTranslateType(translator);
        if (imLeftType instanceof ImClassType) {
            ImClassType imLeftTypeC = (ImClassType) imLeftType;
            if (leftType instanceof WurstTypeClassOrInterface) {
                WurstTypeClassOrInterface wtc = (WurstTypeClassOrInterface) leftType;

                if (wtc.isStaticRef()) {
                    return JassIm.ImTypeIdOfClass(imLeftTypeC);
                } else {
                    return JassIm.ImTypeIdOfObj(e.getLeft().imTranslateExpr(translator, f), imLeftTypeC);
                }
            } else {
                throw new CompileError(e, "not implemented for " + leftType);
            }
        } else {
            throw new CompileError(e, "not implemented for " + leftType);
        }
    }

    public static ImExpr translate(ExprClosure e, ImTranslator tr, ImFunction f) {
        return new ClosureTranslator(e, tr, f).translate();
    }

    public static ImExpr translate(ExprStatementsBlock e, ImTranslator translator, ImFunction f) {

        ImStmts statements = JassIm.ImStmts();
        for (WStatement s : e.getBody()) {
            if (s instanceof StmtReturn) {
                continue;
            }
            ImStmt translated = s.imTranslateStmt(translator, f);
            statements.add(translated);
        }

        StmtReturn r = e.getReturnStmt();
        if (r != null && r.getReturnedObj() instanceof Expr) {
            Expr returnedExpr = (Expr) r.getReturnedObj();
            boolean propagatesExpectedType = returnedExpr instanceof ExprIfElse || returnedExpr instanceof ExprUnary;
            ImExpr expr = propagatesExpectedType
                ? translateWithExpectedType(returnedExpr, translator, f, e.attrExpectedTypRaw())
                : returnedExpr.imTranslateExpr(translator, f);
            if (!propagatesExpectedType) {
                expr = wrapTranslation(e, translator, expr, returnedExpr.attrTypRaw(), e.attrExpectedTypRaw());
            }
            ImExpr result = JassIm.ImStatementExpr(statements, expr);
            return propagatesExpectedType
                ? result
                : wrapTranslation(e, translator, result, e.attrTypRaw(), e.attrExpectedTypRaw());
        } else {
            return ImHelper.statementExprVoid(statements);
        }
    }

    public static ImExpr translate(ExprDestroy s, ImTranslator t, ImFunction f) {
        WurstType typ = s.getDestroyedObj().attrTyp();
        if (typ instanceof WurstTypeClass) {
            WurstTypeClass classType = (WurstTypeClass) typ;
            return destroyClass(s, t, f, classType.getClassDef());
        } else if (typ instanceof WurstTypeInterface) {
            WurstTypeInterface wti = (WurstTypeInterface) typ;
            return destroyClass(s, t, f, wti.getDef());
        } else if (typ instanceof WurstTypeModuleInstanciation) {
            WurstTypeModuleInstanciation minsType = (WurstTypeModuleInstanciation) typ;
            ClassDef classDef = minsType.getDef().attrNearestClassDef();
            return destroyClass(s, t, f, classDef);
        }
        // TODO destroy interfaces?
        throw new CompileError(s.getSource(), "cannot destroy object of type " + typ);
    }

    public static ImExpr destroyClass(ExprDestroy s, ImTranslator t, ImFunction f, StructureDef classDef) {
        ImMethod destroyFunc = t.destroyMethod.getFor(classDef);
        return ImMethodCall(s, destroyFunc, ImTypeArguments(), s.getDestroyedObj().imTranslateExpr(t, f), ImExprs(), false);

    }

    public static ImExpr translate(ExprEmpty s, ImTranslator translator, ImFunction f) {
        throw new CompileError(s.getSource(), "cannot translate empty expression");
    }

    public static ImExpr translate(ExprIfElse e, ImTranslator t, ImFunction f) {
        ImExpr ifTrue = e.getIfTrue().imTranslateExpr(t, f);
        ImExpr ifFalse = e.getIfFalse().imTranslateExpr(t, f);
        // TODO common super type of both
        ImVar res = JassIm.ImVar(e, ifTrue.attrTyp(), "cond_result", false);
        f.getLocals().add(res);
        return JassIm.ImStatementExpr(
                ImStmts(
                        ImIf(e, e.getCond().imTranslateExpr(t, f),
                                ImStmts(
                                        ImSet(e.getIfTrue(), ImVarAccess(res), ifTrue)
                                ),
                                ImStmts(
                                        ImSet(e.getIfFalse(), ImVarAccess(res), ifFalse)
                                ))
                ),
                JassIm.ImVarAccess(res)
        );
    }

    static ImExpr translateWithExpectedType(Expr e, ImTranslator t, ImFunction f, WurstType expectedType) {
        if (e instanceof ExprIfElse) {
            return translateWithExpectedType((ExprIfElse) e, t, f, expectedType);
        }
        if (e instanceof ExprUnary) {
            ExprUnary unary = (ExprUnary) e;
            ImExpr right = translateWithExpectedType(unary.getRight(), t, f, expectedType);
            ImExpr translated = ImOperatorCall(unary.getOpU(), ImExprs(right));
            return wrapTranslation(e, t, translated, e.attrTypRaw(), expectedType);
        }
        ImExpr translated = e.imTranslateExpr(t, f);
        if (isAlreadyTypeAssured(translated, t)) {
            return translated;
        }
        return wrapTranslation(e, t, translated, e.attrTypRaw(), expectedType);
    }

    private static boolean isAlreadyTypeAssured(ImExpr translated, ImTranslator t) {
        if (translated instanceof ImFunctionCall) {
            ImFunction function = ((ImFunctionCall) translated).getFunc();
            return function == t.ensureIntFunc || function == t.ensureRealFunc
                || function == t.ensureStrFunc || function == t.ensureBoolFunc;
        }
        if (translated instanceof ImOperatorCall) {
            ImOperatorCall operator = (ImOperatorCall) translated;
            return operator.getOp() == WurstOperator.EQ
                && operator.getArguments().size() == 2
                && operator.getArguments().get(1) instanceof ImBoolVal
                && ((ImBoolVal) operator.getArguments().get(1)).getValB();
        }
        return false;
    }

    private static ImExpr translateWithExpectedType(ExprIfElse e, ImTranslator t, ImFunction f,
                                                    WurstType expectedType) {
        ImExpr ifTrue = translateWithExpectedType(e.getIfTrue(), t, f, expectedType);
        ImExpr ifFalse = translateWithExpectedType(e.getIfFalse(), t, f, expectedType);
        ImVar res = JassIm.ImVar(e, ifTrue.attrTyp(), "cond_result", false);
        f.getLocals().add(res);
        return JassIm.ImStatementExpr(
            ImStmts(
                ImIf(e, e.getCond().imTranslateExpr(t, f),
                    ImStmts(ImSet(e.getIfTrue(), ImVarAccess(res), ifTrue)),
                    ImStmts(ImSet(e.getIfFalse(), ImVarAccess(res), ifFalse)))
            ),
            JassIm.ImVarAccess(res)
        );
    }

    public static ImLExpr translateLvalue(LExpr e, ImTranslator t, ImFunction f) {
        NameDef decl = e.attrNameDef();
        if (decl == null) {
            // should only happen with gg_ variables
            throw new CompileError(e.getSource(), "Translation Error: Could not find definition of " + e.getVarName() + ".");
        }
        if (decl instanceof VarDef) {
            VarDef varDef = (VarDef) decl;

            ImVar v = t.getVarFor(varDef);
            NameLink link = e.attrNameLink();
            @Nullable FuncLink indexGetOverload = (link == null || !(e instanceof NameRef))
                    ? null
                    : getIndexGetOverload((NameRef) e, link);

            if (e.attrImplicitParameter() instanceof Expr) {
                // we have implicit parameter
                // e.g. "someObject.someField"
                Expr implicitParam = (Expr) e.attrImplicitParameter();

                if (implicitParam.attrTyp() instanceof WurstTypeTuple) {
                    WurstTypeTuple tupleType = (WurstTypeTuple) implicitParam.attrTyp();
                    if (e instanceof ExprMemberVar && ((ExprMemberVar) e).getLeft() instanceof LExpr) {
                        ExprMemberVar emv = (ExprMemberVar) e;
                        LExpr left = (LExpr) emv.getLeft();
                        ImLExpr lt = left.imTranslateExprLvalue(t, f);
                        return JassIm.ImTupleSelection(lt, tupleType.getTupleIndex(varDef));
                    } else {
                        throw new CompileError(e.getSource(), "Cannot create tuple access");
                    }
                }

                if (e instanceof AstElementWithIndexes) {
                    if (indexGetOverload != null) {
                        throw new CompileError(e.getSource(), "Cannot assign to overloaded [] access without " + AttrFuncDef.overloadingIndexSet + ".");
                    }
                    ImExpr index1 = implicitParam.imTranslateExpr(t, f);
                    ImExpr index2 = ((AstElementWithIndexes) e).getIndexes().get(0).imTranslateExpr(t, f);
                    return JassIm.ImMemberAccess(e, index1, JassIm.ImTypeArguments(), v, JassIm.ImExprs(index2));

                } else {
                    ImExpr index = implicitParam.imTranslateExpr(t, f);
                    return JassIm.ImMemberAccess(e, index, JassIm.ImTypeArguments(), v, JassIm.ImExprs());
                }
            } else {
                // direct var access
                if (e instanceof AstElementWithIndexes) {
                    if (indexGetOverload != null) {
                        throw new CompileError(e.getSource(), "Cannot assign to overloaded [] access without " + AttrFuncDef.overloadingIndexSet + ".");
                    }
                    // direct access array var
                    AstElementWithIndexes withIndexes = (AstElementWithIndexes) e;
                    if (withIndexes.getIndexes().size() > 1) {
                        throw new CompileError(e.getSource(), "More than one index is not supported.");
                    }
                    ImExpr index = withIndexes.getIndexes().get(0).imTranslateExpr(t, f);
                    return ImVarArrayAccess(e, v, JassIm.ImExprs(index));
                } else {
                    // not an array var
                    return ImVarAccess(v);

                }
            }
        } else {
            throw new CompileError(e.getSource(), "Cannot translate reference to " + Utils.printElement(decl));
        }
    }

    public static ImExpr translate(ExprArrayLength exprArrayLength, ImTranslator translator, ImFunction f) {
        var t = exprArrayLength.getArray().attrTyp();
        if (t instanceof WurstTypeArray wta && wta.getDimensions() > 0) {
            return JassIm.ImIntVal(wta.getSize(0));
        }
        // if you ever support dynamic length, translate accordingly (otherwise error)
        exprArrayLength.addError("length is only available for arrays with known size.");
        return JassIm.ImIntVal(0);
    }

    private static @Nullable FuncLink getIndexGetOverload(NameRef e, NameLink link) {
        if (!(e instanceof AstElementWithIndexes)) {
            return null;
        }
        AstElementWithIndexes withIndexes = (AstElementWithIndexes) e;
        if (withIndexes.getIndexes().size() != 1) {
            return null;
        }
        WurstType receiverType = link.getTyp();
        if (receiverType instanceof WurstTypeArray) {
            return null;
        }
        return AttrFuncDef.getIndexGetOperator(e, receiverType, withIndexes.getIndexes().get(0).attrTyp());
    }

}
