package de.peeeq.wurstscript.translation.imtojass;


import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassAst.*;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.types.TypesHelper;

import java.math.BigDecimal;

import static de.peeeq.wurstscript.jassAst.JassAst.*;

public class ExprTranslation {

    public static JassExpr translate(ImBoolVal e, ImToJassTranslator translator) {
        return JassAst.JassExprBoolVal(e.getValB());
    }

    public static JassExpr translate(ImFuncRef e, ImToJassTranslator translator) {
        JassFunctionOrNative f = translator.getJassFuncFor(e.getFunc());
        return JassAst.JassExprFuncRef(f.getName());
    }

    public static JassExpr translate(ImFunctionCall e, ImToJassTranslator translator) {

        JassFunctionOrNative f = translator.getJassFuncFor(e.getFunc());
        JassExprlist arguments = JassExprlist();
        for (ImExpr arg : e.getArguments()) {
            arguments.add(arg.translate(translator));
        }
        String funcName = f.getName();
        if (funcName.equals(ImTranslator.$DEBUG_PRINT)) {
            funcName = "BJDebugMsg";
        }
        switch (e.getCallType()) {
            case NORMAL:
                return JassAst.JassExprFunctionCall(funcName, arguments);
            case EXECUTE:
                return JassAst.JassExprFunctionCall("ExecuteFunc", JassAst.JassExprlist(JassAst.JassExprStringVal(f.getName())));
            default:
                throw new Error("unhandled case");
        }
    }

    public static JassExpr translate(ImIntVal e, ImToJassTranslator translator) {
        return JassExprIntVal(String.valueOf(e.getValI()));
    }

    public static JassExpr translate(ImNull e, ImToJassTranslator translator) {
        ImType type = e.getType();

        // Handle simple types (after EliminateGenerics specialization)
        if (type instanceof ImSimpleType simpleType) {
            String typename = simpleType.getTypename();

            if (typename.equals("integer") || typename.equals("int")) {
                return JassExprIntVal("0");
            } else if (typename.equals("real")) {
                return JassExprRealVal("0.");
            } else if (typename.equals("boolean") || typename.equals("bool")) {
                return JassExprBoolVal(false);
            } else if (typename.equals("string")) {
                return JassExprNull();
            }
        }

        // Handle AnyType and int-like types
        if (type instanceof ImAnyType || TypesHelper.isIntType(type)) {
            return JassExprIntVal("0");
        }

        // Class types (reference types) can be null, represented as 0 in Jass
        if (type instanceof ImClassType) {
            return JassExprIntVal("0");
        }

        // Default: actual null (for handle types, etc.)
        return JassExprNull();
    }

    public static JassExpr translate(ImOperatorCall e, ImToJassTranslator translator) {
        WurstOperator op = e.getOp();
        if (op.isBinaryOp() && e.getArguments().size() == 2) {
            JassExpr left = e.getArguments().get(0).translate(translator);
            JassExpr right = e.getArguments().get(1).translate(translator);

            if (op == WurstOperator.PLUS) {
                // special cases for using 'null' as a string constant:
                // "a" + null gets translated to just "a"
                if (left instanceof JassExprNull) {
                    return right;
                }
                if (right instanceof JassExprNull) {
                    return left;
                }
            }

            if (op == WurstOperator.MOD_REAL) {
                return JassExprFunctionCall("ModuloReal", JassExprlist(left, right));
            } else if (op == WurstOperator.MOD_INT) {
                return JassExprFunctionCall("ModuloInteger", JassExprlist(left, right));
            }

            return JassExprBinary(left, op.jassTranslateBinary(), right);
        } else if (op.isUnaryOp() && e.getArguments().size() == 1) {
            return JassExprUnary(op.jassTranslateUnary(), e.getArguments().get(0).translate(translator));
        } else {
            throw new Error("not implemented: " + e);
        }
    }


    public static JassExpr translate(ImRealVal e, ImToJassTranslator translator) {
        return realLiteral(e);
    }

    private static final BigDecimal TWO_TO_24 = BigDecimal.valueOf(1 << 24);
    /** The largest power of two whose literal Jass reads exactly. */
    private static final int MAX_FACTOR_EXPONENT = 30;

    /**
     * The Jass for the float a real literal means. Jass reads a literal's integer part as a 32-bit
     * integer and truncates it past 2^24 (measured on the 3.0.0 client: the literal 3000000000. is
     * negative, 10000000000. reads as 1410065408, 16777219. as 16777218), while it reads an exact
     * float below 2^31 exactly. So past 2^24, where every float is an integer, the backend prints
     * the float's own digits, and from 2^31 on a product of exact factors, its odd part and powers
     * of two, which Jass multiplies exactly. A literal past the largest float, which the interpreter
     * reads as infinity, is the largest float: Jass has no product which gives infinity (measured
     * too: 2^150 compares below 3e38 and prints as 0.000, while the largest float compares above it).
     */
    private static JassExpr realLiteral(ImRealVal e) {
        String literal = e.getValR();
        if (new BigDecimal(literal).abs().compareTo(TWO_TO_24) <= 0) {
            return JassExprRealVal(literal);
        }
        float value = Float.parseFloat(literal);
        float magnitude = Math.min(Math.abs(value), Float.MAX_VALUE);
        JassExpr result;
        if (magnitude < 2147483648f) {
            result = JassExprRealVal(new BigDecimal(magnitude).toBigInteger() + ".");
        } else {
            // magnitude = odd * 2^exponent, with odd below 2^24
            int exponent = Math.getExponent(magnitude) - 23;
            long odd = (long) Math.scalb(magnitude, -exponent);
            int zeros = Long.numberOfTrailingZeros(odd);
            odd >>= zeros;
            exponent += zeros;
            if (odd == 1) {
                int factor = Math.min(exponent, MAX_FACTOR_EXPONENT);
                result = JassExprRealVal((1L << factor) + ".");
                exponent -= factor;
            } else {
                result = JassExprRealVal(odd + ".");
            }
            while (exponent > 0) {
                int factor = Math.min(exponent, MAX_FACTOR_EXPONENT);
                result = JassExprBinary(result, JassOpMult(), JassExprRealVal((1L << factor) + "."));
                exponent -= factor;
            }
        }
        return value < 0 ? JassExprUnary(JassOpMinus(), result) : result;
    }

    public static JassExpr translate(ImStatementExpr e, ImToJassTranslator translator) {
        throw new Error("this expr should have been flattened: " + e + "\n\n" + e.getNearestFunc());
    }

    public static JassExpr translate(ImStringVal e, ImToJassTranslator translator) {
        return JassExprStringVal(e.getValS());
    }

    public static JassExpr translate(ImTupleExpr e, ImToJassTranslator translator) {
        throw new Error("tuples should be eliminated in this phase");
    }

    public static JassExpr translate(ImTupleSelection e, ImToJassTranslator translator) {
        throw new Error("tuples should be eliminated in this phase");
    }

    public static JassExprVarAccess translate(ImVarAccess e, ImToJassTranslator translator) {
        JassVar v = translator.getJassVarFor(e.getVar());
        return JassExprVarAccess(v.getName());
    }

    public static JassExprVarArrayAccess translate(ImVarArrayAccess e, ImToJassTranslator translator) {
        JassVar v = translator.getJassVarFor(e.getVar());
        if (e.getIndexes().size() != 1) {
            throw new CompileError(e.attrTrace().attrSource(), "Only one array index allowed.");
        }
        return JassExprVarArrayAccess(v.getName(), e.getIndexes().get(0).translate(translator));
    }

    public static JassExpr translate(ImClassRelatedExpr e,
                                     ImToJassTranslator translator) {
        throw new RuntimeException("Eliminate method calls before translating to jass:\n" + e);
    }


    public static JassExpr translate(ImGetStackTrace imGetStackTrace, ImToJassTranslator translator) {
        return JassAst.JassExprStringVal("");
    }


    public static JassExpr translate(ImCompiletimeExpr e, ImToJassTranslator translator) {
        throw new CompileError(e.attrTrace().attrSource(),
                "Compiletime expression must be evaluated before translation. " +
                "Enable '-runcompiletimefunctions' to evaluate compiletime expressions.");
    }

    public static JassExpr translate(ImTypeVarDispatch e, ImToJassTranslator translator) {
        throw new CompileError(e, "Typevar dispatch not eliminated.");
    }

    public static JassExpr translate(ImCast imCast, ImToJassTranslator translator) {
        return imCast.getExpr().translate(translator);
    }
}
