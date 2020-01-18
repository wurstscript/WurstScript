package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class AttrImplicitParameter {

    public static OptExpr getImplicitParameter(ExprMemberVar e) {
        Expr result = getImplicitParameterUsingLeft(e);
        if (result == null) {
            return getImplicitParamterCaseNormalVar(e);
        } else {
            return result;
        }
    }

    public static OptExpr getImplicitParameter(ExprMemberArrayVar e) {
        Expr result = getImplicitParameterUsingLeft(e);
        if (result == null) {
            return getImplicitParamterCaseNormalVar(e);
        } else {
            return result;
        }
    }

    public static OptExpr getImplicitParameter(ExprVarAccess e) {
        return getImplicitParamterCaseNormalVar(e);
    }

    public static OptExpr getImplicitParameter(ExprVarArrayAccess e) {
        return getImplicitParamterCaseNormalVar(e);
    }


    public static OptExpr getImplicitParameter(ExprFunctionCall e) {
        return getImplicitParameterCaseNormalFunctionCall(e);
    }

    public static OptExpr getImplicitParameter(ExprMemberMethod e) {
        Expr result = getImplicitParameterUsingLeft(e);
        if (result == null) {
            return getImplicitParameterCaseNormalFunctionCall(e);
        } else {
            FuncLink calledFunc = e.attrFuncLink();
            if (calledFunc != null
                    && !calledFunc.getDef().attrIsDynamicClassMember()
                    && !(calledFunc.getDef() instanceof ExtensionFuncDef)) {
                e.addError("Cannot call static method " + e.getFuncName() + " on an object.");
            }

            return result;
        }
    }

    private static @Nullable Expr getImplicitParameterUsingLeft(HasReceiver e) {
        if (e.getLeft().attrTyp().isStaticRef()) {
            // we have a static ref like Math.sqrt()
            // this will be handled like if we just have sqrt()
            // if we have an implicit parameter depends on whether sqrt is static or not
            return null;
        }
        return e.getLeft();
    }

    private static OptExpr getImplicitParameterCaseNormalFunctionCall(FunctionCall e) {
        FuncLink calledFunc = e.attrFuncLink();
        return getFunctionCallImplicitParameter(e, calledFunc, true);
    }

    static OptExpr getFunctionCallImplicitParameter(FunctionCall e, FuncLink calledFunc, boolean showError) {
        if (e instanceof HasReceiver) {
            HasReceiver hasReceiver = (HasReceiver) e;
            Expr res = getImplicitParameterUsingLeft(hasReceiver);
            if (res != null) {
                return res;
            }
        }
        if (calledFunc == null) {
            return Ast.NoExpr();
        }
        if (calledFunc.getDef().attrIsDynamicClassMember()) {
            // dynamic function call
            if (e.attrIsDynamicContext()) {
                // dynamic context means we have a 'this':
                ExprThis t = Ast.ExprThis(e.getSource());
                t.setParent(e);
                // check if 'this' has correct type
                if (showError && !t.attrTyp().isSubtypeOf(calledFunc.getReceiverType(), e)) {
                    e.addError("Cannot access dynamic function " + e.getFuncName() + " from context of type " +
                            t.attrTyp() + ".");
                }
                return t;
            } else {
                if (showError) {
                    e.addError("Cannot call dynamic function " + e.getFuncName() + " from static context.");
                }
                return Ast.NoExpr();
            }
        } else {

            // static function:
            return Ast.NoExpr();
        }
    }

    private static OptExpr getImplicitParamterCaseNormalVar(NameRef e) {
        NameLink def = e.attrNameLink();
        if (def != null && def.getDef() instanceof VarDef) {
            VarDef varDef = (VarDef) def.getDef();
            if (varDef.attrIsDynamicClassMember()) {
                // dynamic var access
                if (e.attrIsDynamicContext()) {
                    // dynamic context means we have a 'this':
                    ExprThis t = Ast.ExprThis(e.getSource());
                    t.setParent(e);
                    // check if 'this' has correct type
                    WurstType thisType = t.attrTyp();
                    if (!def.receiverCompatibleWith(thisType, e)) {
                        e.addError("Cannot access dynamic variable " + varDef.getName() + " from context of type " +
                                thisType + ".");
                    }
                    return t;
                } else {
                    e.addError("Cannot access dynamic variable " + varDef.getName() + " from static context.");
                    return Ast.NoExpr();
                }
            }
        }
        return Ast.NoExpr();
    }


}
