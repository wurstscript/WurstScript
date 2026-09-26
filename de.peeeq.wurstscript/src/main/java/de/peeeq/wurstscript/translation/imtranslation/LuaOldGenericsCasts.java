package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.intermediatelang.optimizer.LocalMerger;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerContextAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.types.TypesHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Gives every old-generics int cast a local variable operand, right before Lua translation.
 *
 * <p>The Lua backend prints a cast between {@code int} and an old-generics type parameter as a
 * short inline expression which reads its operand more than once (see
 * ExprTranslation.translate(ImCast)). That is only correct and cheap for a variable. Every other
 * operand - a call such as {@code loadInt(...)}, an array read - is stored in a fresh local first.
 * This runs after the last optimization, so nothing folds the temporary back into the cast, and
 * the translation never needs a runtime helper call.
 *
 * <p>Each temporary lives from its assignment to the cast right after it, so the functions that
 * got some are passed through the local merger once more: the temporaries share a slot with each
 * other and with dead locals of their type, and a function with many casts does not grow towards
 * Lua's limit of 200 locals. Merging only renames variables, so the operands stay variables.
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
        Set<ImFunction> touched = new LinkedHashSet<>();
        for (ImCast cast : casts) {
            ImFunction function = cast.getNearestFunc();
            touched.add(function);
            ImExpr operand = cast.getExpr();
            de.peeeq.wurstscript.ast.Element trace = operand.attrTrace();
            ImVar temp = JassIm.ImVar(trace, EliminateLocalTypes.localTypeFor(operand.attrTyp()),
                "oldGenericsValue", false);
            function.getLocals().add(temp);
            ImCast replacement = JassIm.ImCast(JassIm.ImVarAccess(temp), cast.getToType());
            operand.setParent(null);
            cast.replaceBy(JassIm.ImStatementExpr(
                JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(temp), operand)),
                replacement));
        }
        prog.flatten(translator);
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog);
        for (ImFunction function : touched) {
            new LocalMerger().optimizeFunc(function, analyzer, translator);
        }
    }
}
