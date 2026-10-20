package tests.wurstscript.tests;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.NoExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImOperatorCall;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * An IM element without a trace of its own reports the trace of the nearest enclosing element which
 * has one: that source contains the element. Error positions, the Jass source map and the traces of
 * elements the optimiser makes from an expression all read it.
 */
public class ImTraceTests {

    private static ExprIntVal source(String value) {
        return Ast.ExprIntVal(new WPos("test.wurst", null, 0, 0), value);
    }

    private static ImVar var(String name) {
        return JassIm.ImVar(source(name), JassIm.ImSimpleType("integer"), name, false);
    }

    private static ImFunction function(String name, ImVar local, ImSet body) {
        return JassIm.ImFunction(source(name), name, JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(local), JassIm.ImStmts(body), Collections.emptyList());
    }

    @Test
    public void anUntracedExpressionTakesTheTraceOfItsNearestTracedAncestor() {
        ImVar x = var("x");
        ImVarAccess read = JassIm.ImVarAccess(x);
        ImOperatorCall sum = JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(read, JassIm.ImIntVal(1)));
        ExprIntVal setTrace = source("set");
        ImSet set = JassIm.ImSet(setTrace, JassIm.ImVarAccess(x), sum);
        function("f", x, set);

        assertSame(sum.attrTrace(), setTrace);
        assertSame(read.attrTrace(), setTrace);
        assertSame(sum.getArguments().attrTrace(), setTrace);
    }

    @Test
    public void anArgumentTakesTheTraceOfItsCallNotOfASiblingArgument() {
        ImVar x = var("x");
        ImFunction g = JassIm.ImFunction(source("g"), "g", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        ImVarAccess argument = JassIm.ImVarAccess(x);
        ExprIntVal siblingTrace = source("sibling");
        ImFunctionCall sibling = JassIm.ImFunctionCall(siblingTrace, g, JassIm.ImTypeArguments(), JassIm.ImExprs(),
            false, CallType.NORMAL);
        ExprIntVal callTrace = source("call");
        ImFunctionCall call = JassIm.ImFunctionCall(callTrace, g, JassIm.ImTypeArguments(),
            JassIm.ImExprs(JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(argument, sibling))),
            false, CallType.NORMAL);
        function("f", x, JassIm.ImSet(source("set"), JassIm.ImVarAccess(x), call));

        assertSame(argument.attrTrace(), callTrace);
        assertSame(sibling.attrTrace(), siblingTrace);
    }

    @Test
    public void aDetachedUntracedElementHasNoTrace() {
        ImVar x = var("x");
        ImOperatorCall sum = JassIm.ImOperatorCall(WurstOperator.PLUS, JassIm.ImExprs(JassIm.ImVarAccess(x), JassIm.ImIntVal(1)));

        assertTrue(sum.attrTrace() instanceof NoExpr, String.valueOf(sum.attrTrace()));
    }
}
