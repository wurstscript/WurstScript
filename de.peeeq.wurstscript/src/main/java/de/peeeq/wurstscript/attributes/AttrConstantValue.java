package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;

public class AttrConstantValue {


    public static class ConstantValueCalculationException extends RuntimeException {

        public ConstantValueCalculationException(String msg) {
            super(msg);
        }

    }

    public static ILconst calculate(Expr e) {
        // by default, throw an exception
        // special cases for certain expressions
        throw new ConstantValueCalculationException(e.toString());
    }


    public static ILconst calculate(ExprIntVal e) {
        // for an integer, just return the int
        return new ILconstInt(e.getValI());
    }

    public static ILconst calculate(ExprVarAccess e) {
        NameLink v = e.attrNameDef();
        if (v != null && v.getDef() instanceof GlobalVarDef) {
            GlobalVarDef g = (GlobalVarDef) v.getDef();
            if (g.attrIsConstant() && g.getInitialExpr() instanceof Expr) {
                // when this is a global constant:
                Expr initial = (Expr) g.getInitialExpr();
                // return the value of the initial expr
                return initial.attrConstantValue();
            }
        }
        // otherwise we cannot calculate the value
        throw new ConstantValueCalculationException(e.toString());
    }

    public static ILconst calculate(ExprBinary e) {
        WurstOperator op = e.getOp();
        return op.evaluateBinaryOperator(e.getLeft().attrConstantValue(), () -> e.getRight().attrConstantValue());
    }

    public static ILconst calculate(ExprUnary e) {
        WurstOperator op = e.getOpU();
        return op.evaluateUnaryOperator(e.getRight().attrConstantValue());
    }


}
