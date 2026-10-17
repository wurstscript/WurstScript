package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

/**
 * Converts an integer stored where a real is declared: into a variable, as an argument for a real parameter, or as
 * the result of a function which returns real. Only Jass input stores one there ({@code set r = n}), and Jass
 * converts it at the store. Lua keeps the integer, which the game's Lua computes in 32 bits, so a product of the
 * stored value wraps where Jass multiplies reals: measured on the 3.0.0 client, 65536 stored into a real global,
 * squared and divided by 65536. gave 0 on Lua and 65536 on Jass.
 *
 * <p>A literal becomes the real it stands for. Anything else gets {@code + 0.}, the conversion Lua's own arithmetic
 * does for an integer operand. Wurst code stores no integer where a real is declared, so it gets neither. This runs
 * before the optimiser: an inlined function keeps the conversion of its result, and copy propagation sees a real.
 */
public final class LuaIntToReal {

    private LuaIntToReal() {
    }

    public static void transform(ImProg prog) {
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImSet s) {
                super.visit(s);
                convert(s.getRight(), s.getLeft().attrTyp());
            }

            @Override
            public void visit(ImReturn r) {
                super.visit(r);
                if (r.getReturnValue() instanceof ImExpr value) {
                    convert(value, r.getNearestFunc().getReturnType());
                }
            }

            @Override
            public void visit(ImFunctionCall c) {
                super.visit(c);
                convertArguments(c.getArguments(), c.getFunc().getParameters(), 0);
            }

            @Override
            public void visit(ImMethodCall c) {
                super.visit(c);
                // The implementation's first parameter is the receiver.
                convertArguments(c.getArguments(), c.getMethod().getImplementation().getParameters(), 1);
            }
        });
    }

    private static void convertArguments(ImExprs args, ImVars params, int firstParameter) {
        for (int i = 0; i < args.size() && firstParameter + i < params.size(); i++) {
            convert(args.get(i), params.get(firstParameter + i).getType());
        }
    }

    private static void convert(ImExpr e, ImType declared) {
        if (!TypesHelper.isRealType(declared) || !TypesHelper.isIntType(e.attrTyp())) {
            return;
        }
        if (e instanceof ImIntVal literal) {
            e.replaceBy(JassIm.ImRealVal(literal.getValI() + "."));
            return;
        }
        e.replaceBy(JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(e.copy(), JassIm.ImRealVal("0."))));
    }
}
