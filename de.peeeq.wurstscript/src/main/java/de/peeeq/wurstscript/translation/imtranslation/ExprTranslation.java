package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.OtherLink;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Utils;
import io.vavr.control.Either;
import io.vavr.control.Option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.peeeq.wurstscript.jassIm.JassIm.*;

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

    public static ImExpr translate(ExprInstanceOf e, ImTranslator t, ImFunction f) {
        return wrapTranslation(e, t, translateIntern(e, t, f));
    }

    private static ImExpr wrapTranslation(Expr e, ImTranslator t, ImExpr translated) {
        WurstType actualType = e.attrTypRaw();
        WurstType expectedTypRaw = e.attrExpectedTypRaw();
        return wrapTranslation(e, t, translated, actualType, expectedTypRaw);
    }

    static ImExpr wrapTranslation(Element trace, ImTranslator t, ImExpr translated, WurstType actualType, WurstType expectedTypRaw) {
        if (t.isLuaTarget()) {
            // for lua we do not need fromIndex/toIndex
            return translated;
        }


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
            return translated;
        } else if (fromIndex != null) {
//            System.out.println("  --> fromIndex");
            return ImFunctionCall(trace, fromIndex, ImTypeArguments(), JassIm.ImExprs(translated), false, CallType.NORMAL);
        } else if (toIndex != null) {
//            System.out.println("  --> toIndex");
            return ImFunctionCall(trace, toIndex, ImTypeArguments(), JassIm.ImExprs(translated), false, CallType.NORMAL);
        }
        return translated;
    }

    public static ImExpr translateIntern(ExprBinary e, ImTranslator t, ImFunction f) {
        ImExpr left = e.getLeft().imTranslateExpr(t, f);
        ImExpr right = e.getRight().imTranslateExpr(t, f);
        WurstOperator op = e.getOp();
        if (e.attrFuncLink() != null) {
            // overloaded operator
            ImFunction calledFunc = t.getFuncFor(e.attrFuncDef());
            return ImFunctionCall(e, calledFunc, ImTypeArguments(), ImExprs(left, right), false, CallType.NORMAL);
        }
        if (op == WurstOperator.DIV_REAL) {
            if (Utils.isJassCode(Optional.of(e))) {
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
        return translateNameDef(e, t, f);
    }

    private static ImExpr translateNameDef(NameRef e, ImTranslator t, ImFunction f) throws CompileError {
        NameLink link = e.attrNameLink();
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
        } else if (link instanceof OtherLink) {
            OtherLink otherLink = (OtherLink) link;
            return otherLink.translate(e, t, f);
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
            return ImTupleSelection((ImLExpr) left, tupleIndex);
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
            return translateFunctionCall(e, t, f, true);
        } else {
            return translateFunctionCall(e, t, f, false);
        }
    }

    private static ImExpr translateFunctionCall(FunctionCall e, ImTranslator t, ImFunction f, boolean returnReveiver) {

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
        boolean dynamicDispatch = false;

        FunctionDefinition calledFunc = e.attrFuncDef();

        if (e.attrImplicitParameter() instanceof Expr) {
            if (isCalledOnDynamicRef(e) && calledFunc instanceof FuncDef) {
                dynamicDispatch = true;
            }
            // add implicit parameter to front
            // TODO why would I add the implicit parameter here, if it is
            // not a dynamic dispatch?
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

        if (calledFunc == e.attrNearestFuncDef()) {
            // recursive self calls are bound statically
            // this is different to other objectoriented languages but it is
            // necessary
            // because jass does not allow mutually recursive calls
            // The only situation where this would make a difference is with
            // super-calls
            // (or other statically bound calls)
            dynamicDispatch = false;
        }

        ImExpr receiver = leftExpr == null ? null : leftExpr.imTranslateExpr(t, f);
        ImExprs imArgs = translateExprs(arguments, t, f);

        if (calledFunc instanceof TupleDef) {
            // creating a new tuple...
            return ImTupleExpr(imArgs);
        }

        ImStmts stmts = null;
        ImVar tempVar = null;
        if (returnReveiver) {
            if (leftExpr == null)
                throw new Error("impossible");
            tempVar = JassIm.ImVar(leftExpr, leftExpr.attrTyp().imTranslateType(t), "receiver", false);
            f.getLocals().add(tempVar);
            stmts = JassIm.ImStmts(ImSet(e, ImVarAccess(tempVar), receiver));
            receiver = JassIm.ImVarAccess(tempVar);
        }



        ImExpr call;
        if (dynamicDispatch) {
            ImMethod method = t.getMethodFor((FuncDef) calledFunc);
            ImTypeArguments typeArguments = getFunctionCallTypeArguments(t, e.attrFunctionSignature(), e, method.getImplementation().getTypeVariables());
            call = ImMethodCall(e, method, typeArguments, receiver, imArgs, false);
        } else {
            ImFunction calledImFunc = t.getFuncFor(calledFunc);
            if (receiver != null) {
                imArgs.add(0, receiver);
            }
            ImTypeArguments typeArguments = getFunctionCallTypeArguments(t, e.attrFunctionSignature(), e, calledImFunc.getTypeVariables());
            call = ImFunctionCall(e, calledImFunc, typeArguments, imArgs, false, CallType.NORMAL);
        }

        if (returnReveiver) {
            if (stmts == null)
                throw new Error("impossible");
            stmts.add(call);
            return JassIm.ImStatementExpr(stmts, JassIm.ImVarAccess(tempVar));
        } else {
            return call;
        }
    }

    private static ImTypeArguments getFunctionCallTypeArguments(ImTranslator tr, FunctionSignature sig, Element location, ImTypeVars typeVariables) {
        ImTypeArguments res = ImTypeArguments();
        VariableBinding mapping = sig.getMapping();
        for (ImTypeVar tv : typeVariables) {
            TypeParamDef tp = tr.getTypeParamDef(tv);
            Option<WurstTypeBoundTypeParam> to = mapping.get(tp);
            if (to.isEmpty()) {
                throw new CompileError(location, "Type variable " + tp.getName() + " not bound in mapping.");
            }
            WurstTypeBoundTypeParam t = to.get();
            if (!t.isTemplateTypeParameter()) {
                continue;
            }
            ImType type = t.imTranslateType(tr);
            // TODO handle constraints
            Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> typeClassBinding = new HashMap<>();
            res.add(ImTypeArgument(type, typeClassBinding));
        }
        return res;
    }

    private static boolean isCalledOnDynamicRef(FunctionCall e) {
        if (e instanceof ExprMemberMethod) {
            ExprMemberMethod mm = (ExprMemberMethod) e;
            return mm.getLeft().attrTyp().allowsDynamicDispatch();
        } else if (e.attrIsDynamicContext()) {
            return true;
        }
        return false;
    }

    private static ImExprs translateExprs(List<Expr> arguments, ImTranslator t, ImFunction f) {
        ImExprs result = ImExprs();
        for (Expr e : arguments) {
            result.add(e.imTranslateExpr(t, f));
        }
        return result;
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
        return ImFunctionCall(e, constructorImFunc, typeArgs, translateExprs(e.getArgs(), t, f), false, CallType.NORMAL);
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
            ImExpr expr = ((Expr) r.getReturnedObj()).imTranslateExpr(translator, f);
            return JassIm.ImStatementExpr(statements, expr);
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

    public static ImLExpr translateLvalue(LExpr e, ImTranslator t, ImFunction f) {
        NameDef decl = e.attrNameDef();
        if (decl == null) {
            // should only happen with gg_ variables
            throw new CompileError(e.getSource(), "Translation Error: Could not find definition of " + e.getVarName() + ".");
        }
        if (decl instanceof VarDef) {
            VarDef varDef = (VarDef) decl;

            ImVar v = t.getVarFor(varDef);

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

//    public static ImLExpr translateLvalue(ExprVarArrayAccess e, ImTranslator translator, ImFunction f) {
//        NameDef nameDef = e.tryGetNameDef();
//        if (nameDef instanceof VarDef) {
//            VarDef varDef = (VarDef) nameDef;
//            ImVar v = translator.getVarFor(varDef);
//            ImExprs indexes = e.getIndexes().stream()
//                    .map(ie -> ie.imTranslateExpr(translator, f))
//                    .collect(Collectors.toCollection(JassIm::ImExprs));
//            return JassIm.ImVarArrayAccess(v, indexes);
//        }
//        throw new RuntimeException("TODO");
//
//    }
//
//    public static ImLExpr translateLvalue(ExprMemberVar e, ImTranslator translator, ImFunction f) {
//        ImExpr receiver = e.getLeft().imTranslateExpr(translator, f);
//        NameDef nameDef = e.tryGetNameDef();
//        if (nameDef instanceof VarDef) {
//            VarDef v = (VarDef) nameDef;
//            ImVar imVar = translator.getVarFor(v);
//            return JassIm.ImMemberAccess(receiver, imVar);
//        }
//        throw new RuntimeException("TODO");
//    }
//
//    public static ImLExpr translateLvalue(ExprMemberArrayVar e, ImTranslator translator, ImFunction f) {
//        throw new RuntimeException("TODO");
//    }


}
