package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives every old-generics int cast a local variable operand, right before Lua translation.
 *
 * <p>The Lua backend prints a cast between {@code int} and an old-generics type parameter as a
 * short inline expression which reads its operand more than once (see
 * ExprTranslation.translate(ImCast)). That is only correct and cheap for a variable. Every other
 * operand - a call such as {@code loadInt(...)}, an array read - is stored in a fresh local first.
 * This runs after the last optimization, so nothing folds the temporary back into the cast, and
 * the translation never needs a runtime helper call.
 */
public final class LuaOldGenericsCasts {

    private LuaOldGenericsCasts() {
    }

    /** Whether the Lua backend translates {@code cast} with the old-generics int encoding. */
    public static boolean isOldGenericsIntCast(ImCast cast) {
        ImType from = cast.getExpr().attrTyp();
        ImType to = cast.getToType();
        if (TypesHelper.isIntType(to)) {
            return from instanceof ImAnyType;
        }
        return to instanceof ImAnyType
            && (EliminateLocalTypes.isIntegerOrLocalInteger(from) || from instanceof ImClassType);
    }

    public static void transform(ImProg prog, ImTranslator translator) {
        List<ImCast> casts = new ArrayList<>();
        prog.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(ImCast cast) {
                super.visit(cast);
                if (!(cast.getExpr() instanceof ImVarAccess) && isOldGenericsIntCast(cast)
                    && cast.getNearestFunc() != null) {
                    casts.add(cast);
                }
            }
        });
        if (casts.isEmpty()) {
            return;
        }
        for (ImCast cast : casts) {
            ImFunction function = cast.getNearestFunc();
            ImExpr operand = cast.getExpr();
            de.peeeq.wurstscript.ast.Element trace = operand.attrTrace();
            ImVar temp = JassIm.ImVar(trace, operand.attrTyp().copy(), "oldGenericsValue", false);
            function.getLocals().add(temp);
            ImCast replacement = JassIm.ImCast(JassIm.ImVarAccess(temp), cast.getToType());
            operand.setParent(null);
            cast.replaceBy(JassIm.ImStatementExpr(
                JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(temp), operand)),
                replacement));
        }
        prog.flatten(translator);
    }
}
