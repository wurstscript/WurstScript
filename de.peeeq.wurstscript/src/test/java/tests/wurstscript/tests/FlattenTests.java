package tests.wurstscript.tests;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.Flatten;
import de.peeeq.wurstscript.translation.imtranslation.Flatten.MultiResult;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.*;

public class FlattenTests {

    @Test
    public void avoid_extra_temp_for_var_access() throws Exception {
        ImTranslator translator = new ImTranslator(Ast.WurstModel(), true, new RunArgs());
        de.peeeq.wurstscript.ast.Element trace = Ast.NoExpr();
        ImVar sourceVar = JassIm.ImVar(trace, JassIm.ImSimpleType("int"), "source", false);
        ImVar targetVar = JassIm.ImVar(trace, JassIm.ImSimpleType("int"), "target", false);

        ImExpr simpleAccess = JassIm.ImVarAccess(sourceVar);
        ImExpr expressionWithStatements = JassIm.ImStatementExpr(
            JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(targetVar), JassIm.ImIntVal(1))),
            JassIm.ImVarAccess(targetVar));

        ImFunction function = JassIm.ImFunction(trace, "test", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(sourceVar, targetVar), JassIm.ImStmts(), Collections.emptyList());

        Method flattenExprs = Flatten.class.getDeclaredMethod("flattenExprs", ImTranslator.class, ImFunction.class, java.util.List.class);
        flattenExprs.setAccessible(true);

        MultiResult result = (MultiResult) flattenExprs.invoke(null, translator, function, Arrays.asList(simpleAccess, expressionWithStatements));

        assertEquals(function.getLocals().size(), 2, "no extra locals should be introduced");
        assertSame(simpleAccess, result.expr(0), "the original access should be reused");
    }

    @Test
    public void add_temp_for_impure_expr_with_followup_statements() throws Exception {
        ImTranslator translator = new ImTranslator(Ast.WurstModel(), true, new RunArgs());
        de.peeeq.wurstscript.ast.Element trace = Ast.NoExpr();
        ImVar sourceVar = JassIm.ImVar(trace, JassIm.ImSimpleType("int"), "source", false);
        ImVar targetVar = JassIm.ImVar(trace, JassIm.ImSimpleType("int"), "target", false);

        ImFunction impureCallee = JassIm.ImFunction(trace, "impure", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImSimpleType("int"), JassIm.ImVars(), JassIm.ImStmts(), Collections.emptyList());
        ImExpr impureCall = JassIm.ImFunctionCall(trace, impureCallee, JassIm.ImTypeArguments(), JassIm.ImExprs(),
            false, CallType.NORMAL);

        ImExpr expressionWithStatements = JassIm.ImStatementExpr(
            JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(targetVar), JassIm.ImIntVal(1))),
            JassIm.ImVarAccess(targetVar));

        ImFunction function = JassIm.ImFunction(trace, "test", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(sourceVar, targetVar), JassIm.ImStmts(), Collections.emptyList());

        Method flattenExprs = Flatten.class.getDeclaredMethod("flattenExprs", ImTranslator.class, ImFunction.class, java.util.List.class);
        flattenExprs.setAccessible(true);

        MultiResult result = (MultiResult) flattenExprs.invoke(null, translator, function, Arrays.asList(impureCall, expressionWithStatements));

        assertEquals(function.getLocals().size(), 3, "a temporary should be added for the impure expression");
        assertEquals(result.stmts.size(), 2, "flattening should produce a temp assignment followed by the statement expr body");

        ImExpr flattenedImpure = result.expr(0);
        assertTrue(flattenedImpure instanceof ImVarAccess, "impure call should be replaced with temp access");
        ImVar tempVar = ((ImVarAccess) flattenedImpure).getVar();
        assertNotSame(tempVar, sourceVar);
        assertNotSame(tempVar, targetVar);

        ImStmt tempAssignment = result.stmts.get(0);
        assertTrue(tempAssignment instanceof ImSet, "first statement should assign the impure result to the temp var");
        ImLExpr left = ((ImSet) tempAssignment).getLeft();
        assertTrue(left instanceof ImVarAccess);
        assertSame(((ImVarAccess) left).getVar(), tempVar);
    }

    @Test
    public void add_temp_when_followup_writes_accessed_var() throws Exception {
        ImTranslator translator = new ImTranslator(Ast.WurstModel(), true, new RunArgs());
        de.peeeq.wurstscript.ast.Element trace = Ast.NoExpr();
        ImVar shared = JassIm.ImVar(trace, JassIm.ImSimpleType("int"), "shared", false);

        ImExpr initialRead = JassIm.ImVarAccess(shared);
        ImExpr statementWithWrite = JassIm.ImStatementExpr(
            JassIm.ImStmts(JassIm.ImSet(trace, JassIm.ImVarAccess(shared), JassIm.ImIntVal(4))),
            JassIm.ImIntVal(2));

        ImFunction function = JassIm.ImFunction(trace, "test", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(shared), JassIm.ImStmts(), Collections.emptyList());

        Method flattenExprs = Flatten.class.getDeclaredMethod("flattenExprs", ImTranslator.class, ImFunction.class, java.util.List.class);
        flattenExprs.setAccessible(true);

        MultiResult result = (MultiResult) flattenExprs.invoke(null, translator, function, Arrays.asList(initialRead, statementWithWrite));

        assertEquals(function.getLocals().size(), 2, "flattening should introduce a temp for the first argument");
        assertEquals(result.stmts.size(), 2, "flattening should store the first arg before executing later statements");

        ImExpr firstExpr = result.expr(0);
        assertTrue(firstExpr instanceof ImVarAccess, "first expression should be rewritten to a temp access");
        ImVar tempVar = ((ImVarAccess) firstExpr).getVar();
        assertNotSame(tempVar, shared);

        ImStmt firstStmt = result.stmts.get(0);
        assertTrue(firstStmt instanceof ImSet, "first statement should capture the original value");
        assertSame(((ImVarAccess) ((ImSet) firstStmt).getLeft()).getVar(), tempVar);
    }
}
