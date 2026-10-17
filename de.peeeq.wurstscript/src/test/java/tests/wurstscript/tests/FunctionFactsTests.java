package tests.wurstscript.tests;

import de.peeeq.wurstio.TimeTaker;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imoptimizer.ImOptimizer;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.RunArgs;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class FunctionFactsTests {
    private final WurstModel trace = Ast.WurstModel();

    private ImFunction function(String name) {
        return JassIm.ImFunction(trace, name, JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImVoid(),
            JassIm.ImVars(), JassIm.ImStmts(), List.of());
    }

    private ImTranslator translator(ImFunction main, ImFunction... others) {
        ImTranslator translator = new ImTranslator(trace, true, new RunArgs());
        translator.getImProg().getFunctions().add(main);
        translator.getImProg().getFunctions().addAll(List.of(others));
        translator.setMainFunc(main);
        return translator;
    }

    private ImFunctionCall call(ImFunction function) {
        return JassIm.ImFunctionCall(trace, function, JassIm.ImTypeArguments(), JassIm.ImExprs(), false, CallType.NORMAL);
    }

    @Test
    public void unchangedFactsSurviveSeparateGarbageRemovals() {
        ImFunction main = function("main");
        ImTranslator translator = translator(main);
        ImOptimizer optimizer = new ImOptimizer(new TimeTaker.Default(), translator);
        optimizer.removeGarbage();
        translator.rememberFunctionFacts();
        List<ImSet> sets = translator.setStatementsOf(main);
        translator.forgetFunctionFacts();
        optimizer.removeGarbage();
        translator.rememberFunctionFacts();
        assertSame(translator.setStatementsOf(main), sets, "an unchanged body must not be walked again");
        translator.forgetFunctionFacts();
    }

    @Test
    public void deepMutationRefreshesReadsAndAssignmentsWithoutNotification() {
        ImFunction main = function("main");
        ImVar before = JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), "before", false);
        ImVar after = JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), "after", false);
        ImSet assignment = JassIm.ImSet(trace, JassIm.ImVarAccess(before), JassIm.ImVarAccess(before));
        main.getBody().add(JassIm.ImIf(trace, JassIm.ImBoolVal(true), JassIm.ImStmts(assignment), JassIm.ImStmts()));
        ImTranslator translator = translator(main);
        translator.getImProg().getGlobals().addAll(List.of(before, after));
        translator.rememberFunctionFacts();
        translator.calculateCallRelationsAndReadVariables();
        List<ImSet> sets = translator.setStatementsOf(main);
        assignment.setRight(JassIm.ImVarAccess(after));
        translator.calculateCallRelationsAndReadVariables();
        assertEquals(translator.getReadVariables(), java.util.Set.of(after));
        assertNotSame(translator.setStatementsOf(main), sets);
        assertEquals(translator.setStatementsOf(main), List.of(assignment));
    }

    @Test
    public void changedCallAndFunctionReferenceRefreshReachability() {
        ImFunction main = function("main"), before = function("before"), after = function("after");
        ImFunctionCall call = call(before);
        main.getBody().add(call);
        ImTranslator translator = translator(main, before, after);
        translator.rememberFunctionFacts();
        translator.calculateCallRelationsAndReadVariables();
        assertTrue(translator.getUsedFunctions().contains(before));
        call.setFunc(after);
        translator.calculateCallRelationsAndReadVariables();
        assertFalse(translator.getUsedFunctions().contains(before));
        assertTrue(translator.getUsedFunctions().contains(after));
        main.getBody().add(JassIm.ImFuncRef(trace, before));
        translator.calculateCallRelationsAndReadVariables();
        assertTrue(translator.getUsedFunctions().contains(before));
    }

    @Test
    public void successiveReadRefreshesUseThePreviousRound() {
        ImFunction main = function("main");
        ImVar a = JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), "a", false);
        ImVar b = JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), "b", false);
        ImVar c = JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), "c", false);
        ImVarAccess read = JassIm.ImVarAccess(a);
        main.getBody().add(JassIm.ImReturn(trace, read));
        ImTranslator translator = translator(main);
        translator.getImProg().getGlobals().addAll(List.of(a, b, c));
        translator.rememberFunctionFacts();
        translator.calculateCallRelationsAndReadVariables();
        read.setVar(b);
        assertEquals(translator.refreshReadVariables(List.of(main)), List.of(a));
        assertEquals(translator.getReadVariables(), java.util.Set.of(b));
        read.setVar(c);
        assertEquals(translator.refreshReadVariables(List.of(main)), List.of(b));
        assertEquals(translator.getReadVariables(), java.util.Set.of(c));
        translator.forgetFunctionFacts();
    }

    @Test
    public void movedStatementsInvalidateBothFunctions() {
        ImFunction main = function("main"), other = function("other");
        ImVar a = JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), "a", false);
        ImVar b = JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), "b", false);
        ImSet assignment = JassIm.ImSet(trace, JassIm.ImVarAccess(a), JassIm.ImVarAccess(b));
        other.getBody().add(assignment);
        main.getBody().add(call(other));
        ImTranslator translator = translator(main, other);
        translator.rememberFunctionFacts();
        translator.calculateCallRelationsAndUsedVariables();
        assertEquals(translator.setStatementsOf(other), List.of(assignment));
        main.getBody().addAllMoved(other.getBody());
        translator.calculateCallRelationsAndUsedVariables();
        assertTrue(translator.setStatementsOf(other).isEmpty());
        assertEquals(translator.setStatementsOf(main), List.of(assignment));
        assertTrue(translator.getUsedVariables().containsAll(List.of(a, b)));
        assertTrue(translator.getReadVariables().contains(b));
        translator.forgetFunctionFacts();
    }

    @Test
    public void externalDispatchChangesRefreshAnUnmodifiedCaller() {
        ImFunction main = function("main"), base = function("base"), old = function("old"), replacement = function("replacement");
        ImTranslator translator = translator(main, base, old, replacement);
        ImClass clazz = JassIm.ImClass(trace, "C", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(),
            JassIm.ImFunctions(), new ArrayList<>());
        translator.getImProg().getClasses().add(clazz);
        ImClassType type = JassIm.ImClassType(clazz, JassIm.ImTypeArguments());
        ImMethod root = JassIm.ImMethod(trace, type, "root", base, new ArrayList<>(), new ArrayList<>(), "", false);
        ImMethod child = JassIm.ImMethod(trace, type.copy(), "child", old, new ArrayList<>(), new ArrayList<>(), "", false);
        clazz.getMethods().addAll(List.of(root, child));
        main.getBody().add(JassIm.ImMethodCall(trace, root, JassIm.ImTypeArguments(), JassIm.ImIntVal(1), JassIm.ImExprs(), false));
        translator.rememberFunctionFacts();
        translator.calculateCallRelationsAndReadVariables();
        int callerVersion = main.modificationCount();
        root.getSubMethods().add(child);
        translator.calculateCallRelationsAndReadVariables();
        assertTrue(translator.getUsedFunctions().contains(old));
        child.setImplementation(replacement);
        translator.calculateCallRelationsAndReadVariables();
        assertFalse(translator.getUsedFunctions().contains(old));
        assertTrue(translator.getUsedFunctions().contains(replacement));
        root.getSubMethods().clear();
        root.setImplementation(old);
        translator.calculateCallRelationsAndReadVariables();
        assertTrue(translator.getUsedFunctions().contains(old));
        assertFalse(translator.getUsedFunctions().contains(base));
        assertFalse(translator.getUsedFunctions().contains(replacement));
        assertEquals(main.modificationCount(), callerVersion, "dispatch changes occur outside the caller's tree");
    }
}
