package tests.wurstscript.tests;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.intermediatelang.optimizer.LocalPlayerContextAnalyzer;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.types.TypesHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * What the local-player analysis concludes, on hand-built IM: the data flow, the control flow it
 * follows (a branch, an exit of a loop, the operands of a lazy operator, a statement after a return,
 * the entry of a callee), the call-site arguments which flow into parameters, and a method call.
 * Each case has an element or variable that must be dependent and one that must stay clean.
 */
public class LocalPlayerContextAnalyzerTests {
    private final Element trace = Ast.NoExpr();
    /** Part of the program of one test; made again for each, since an element belongs to one tree. */
    private ImFunction localPlayer;

    @BeforeMethod
    public void makeLocalPlayerNative() {
        localPlayer = nativeIntFunction("GetLocalPlayer");
    }

    private ImFunction nativeIntFunction(String name) {
        return JassIm.ImFunction(trace, name, JassIm.ImTypeVars(), JassIm.ImVars(), TypesHelper.imInt(),
            JassIm.ImVars(), JassIm.ImStmts(), Collections.singletonList(FunctionFlagEnum.IS_NATIVE));
    }

    private ImVar intVar(String name) {
        return JassIm.ImVar(trace, TypesHelper.imInt(), name, false);
    }

    private ImFunction function(String name, ImVars parameters, ImVars locals, ImStmts body, ImType returnType) {
        return JassIm.ImFunction(trace, name, JassIm.ImTypeVars(), parameters, returnType, locals, body,
            Collections.emptyList());
    }

    private ImFunction voidFunction(String name, ImVars locals, ImStmt... body) {
        return function(name, JassIm.ImVars(), locals, JassIm.ImStmts(body), JassIm.ImVoid());
    }

    private ImFunctionCall call(ImFunction function, ImExpr... arguments) {
        return JassIm.ImFunctionCall(trace, function, JassIm.ImTypeArguments(), JassIm.ImExprs(arguments), false,
            CallType.NORMAL);
    }

    private ImSet set(ImVar variable, ImExpr value) {
        return JassIm.ImSet(trace, JassIm.ImVarAccess(variable), value);
    }

    /** {@code GetLocalPlayer() == 0}. */
    private ImExpr localCondition() {
        return JassIm.ImOperatorCall(WurstOperator.EQ, JassIm.ImExprs(call(localPlayer), JassIm.ImIntVal(0)));
    }

    private ImProg prog(ImVars globals, ImFunction... functions) {
        ImFunctions all = JassIm.ImFunctions();
        all.add(localPlayer);
        for (ImFunction function : functions) {
            all.add(function);
        }
        return JassIm.ImProg(trace, globals, all, JassIm.ImMethods(), JassIm.ImClasses(), JassIm.ImTypeClassFuncs(),
            new java.util.HashMap<>());
    }

    @Test
    public void dataFlowReachesTheVariableTheFunctionAndItsCallers() {
        ImVar value = intVar("value");
        ImVar copy = intVar("copy");
        ImVar unrelated = intVar("unrelated");
        ImFunction source = function("source", JassIm.ImVars(), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(trace, call(localPlayer))), TypesHelper.imInt());
        ImFunction user = voidFunction("user", JassIm.ImVars(value, copy, unrelated),
            set(value, call(source)),
            set(copy, JassIm.ImVarAccess(value)),
            set(unrelated, JassIm.ImIntVal(3)));
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog(JassIm.ImVars(), source, user));

        assertTrue(analyzer.isLocalPlayerDependent(value));
        assertTrue(analyzer.isLocalPlayerDependent(copy), "a copy of a dependent value is dependent");
        assertFalse(analyzer.isLocalPlayerDependent(unrelated));
        assertTrue(analyzer.functionInliningIsLocalPlayerSensitive(source),
            "a function which returns a client-local value must stay a call");
        assertTrue(analyzer.functionUsesLocalPlayer(user), "the use of the local player reaches the caller");
        assertFalse(analyzer.functionInliningIsLocalPlayerSensitive(user),
            "a function which only reads a dependent value from a callee returns no client-local value itself");
    }

    @Test
    public void statementsAfterAReturnUnderLocalControlDependOnIt() {
        ImVar before = intVar("before");
        ImVar inside = intVar("inside");
        ImVar after = intVar("after");
        ImIf guardedReturn = JassIm.ImIf(trace, localCondition(),
            JassIm.ImStmts(set(inside, JassIm.ImIntVal(1)), JassIm.ImReturn(trace, JassIm.ImNoExpr())),
            JassIm.ImStmts());
        ImFunction function = voidFunction("function", JassIm.ImVars(before, inside, after),
            set(before, JassIm.ImIntVal(1)),
            guardedReturn,
            set(after, JassIm.ImIntVal(1)));
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog(JassIm.ImVars(), function));

        assertFalse(analyzer.isLocalPlayerDependent(before), "runs before the local branch");
        assertTrue(analyzer.isLocalPlayerDependent(inside), "runs under the local branch");
        assertTrue(analyzer.isLocalPlayerDependent(after), "runs only if the local branch did not return");
        assertTrue(analyzer.isLocalPlayerDependent(guardedReturn));
    }

    @Test
    public void whatRunsInALoopWhichLeavesOnALocalConditionDependsOnIt() {
        ImVar inLoop = intVar("inLoop");
        ImVar afterLoop = intVar("afterLoop");
        ImVar otherLoop = intVar("otherLoop");
        ImLoop leavingOnLocal = JassIm.ImLoop(trace, JassIm.ImStmts(
            JassIm.ImExitwhen(trace, localCondition()),
            set(inLoop, JassIm.ImIntVal(1))));
        ImLoop leavingOnConstant = JassIm.ImLoop(trace, JassIm.ImStmts(
            JassIm.ImExitwhen(trace, JassIm.ImBoolVal(true)),
            set(otherLoop, JassIm.ImIntVal(1))));
        ImFunction function = voidFunction("function", JassIm.ImVars(inLoop, afterLoop, otherLoop),
            leavingOnLocal,
            set(afterLoop, JassIm.ImIntVal(1)),
            leavingOnConstant);
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog(JassIm.ImVars(), function));

        assertTrue(analyzer.isLocalPlayerDependent(inLoop), "how often it runs depends on the exit");
        assertFalse(analyzer.isLocalPlayerDependent(afterLoop), "a loop without a return does not guard what follows it");
        assertFalse(analyzer.isLocalPlayerDependent(otherLoop));
    }

    @Test
    public void onlyAnOperandOfALazyOperatorIsGuardedByTheOnesBeforeIt() {
        ImVar guarded = intVar("guarded");
        ImVar unguarded = intVar("unguarded");
        ImVar lazyResult = intVar("lazyResult");
        ImVar eagerResult = intVar("eagerResult");
        ImExpr setsGuarded = JassIm.ImStatementExpr(JassIm.ImStmts(set(guarded, JassIm.ImIntVal(1))),
            JassIm.ImBoolVal(true));
        ImExpr setsUnguarded = JassIm.ImStatementExpr(JassIm.ImStmts(set(unguarded, JassIm.ImIntVal(1))),
            JassIm.ImIntVal(1));
        ImFunction function = voidFunction("function", JassIm.ImVars(guarded, unguarded, lazyResult, eagerResult),
            set(lazyResult, JassIm.ImOperatorCall(WurstOperator.AND, JassIm.ImExprs(localCondition(), setsGuarded))),
            set(eagerResult, JassIm.ImOperatorCall(WurstOperator.PLUS,
                JassIm.ImExprs(call(localPlayer), setsUnguarded))));
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog(JassIm.ImVars(), function));

        assertTrue(analyzer.isLocalPlayerDependent(guarded), "only evaluated if the local operand before it holds");
        assertFalse(analyzer.isLocalPlayerDependent(unguarded), "every operand of a plain operator is evaluated");
        assertTrue(analyzer.isLocalPlayerDependent(lazyResult));
        assertTrue(analyzer.isLocalPlayerDependent(eagerResult));
    }

    @Test
    public void aCalleeEnteredUnderLocalControlIsControlledButNotDataDependent() {
        ImVar global = intVar("global");
        ImVar local = intVar("local");
        ImVar bystanderLocal = intVar("bystanderLocal");
        ImFunction callee = voidFunction("callee", JassIm.ImVars(local),
            JassIm.ImSet(trace, JassIm.ImVarAccess(global), JassIm.ImIntVal(1)),
            set(local, JassIm.ImIntVal(1)));
        ImFunction bystander = voidFunction("bystander", JassIm.ImVars(bystanderLocal),
            set(bystanderLocal, JassIm.ImIntVal(1)));
        ImFunction caller = voidFunction("caller", JassIm.ImVars(),
            call(bystander),
            JassIm.ImIf(trace, localCondition(), JassIm.ImStmts(call(callee)), JassIm.ImStmts()));
        LocalPlayerContextAnalyzer analyzer =
            new LocalPlayerContextAnalyzer(prog(JassIm.ImVars(global), callee, bystander, caller));

        assertTrue(analyzer.isLocalPlayerDependent(global), "assigned by a function the local branch calls");
        assertTrue(analyzer.isLocalPlayerDependent(local));
        assertFalse(analyzer.isLocalPlayerDependent(bystanderLocal), "called outside the local branch");
        assertTrue(analyzer.functionUsesLocalPlayer(caller));
        assertTrue(analyzer.functionInliningIsLocalPlayerSensitive(caller));
        assertTrue(analyzer.functionUsesLocalPlayer(callee), "it assigns variables which are dependent");
        assertFalse(analyzer.functionUsesLocalPlayer(bystander));
        assertFalse(analyzer.functionInliningIsLocalPlayerSensitive(callee),
            "inlining the callee keeps it under the control it already had");
    }

    @Test
    public void anArgumentTaintsTheParameterButNotTheCalleeForInlining() {
        ImVar parameter = intVar("parameter");
        ImVar result = intVar("result");
        ImFunction passthrough = function("passthrough", JassIm.ImVars(parameter), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(trace, JassIm.ImVarAccess(parameter))), TypesHelper.imInt());
        ImFunction user = voidFunction("user", JassIm.ImVars(result),
            set(result, call(passthrough, call(localPlayer))));
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog(JassIm.ImVars(), passthrough, user));

        assertTrue(analyzer.isLocalPlayerDependent(parameter));
        assertTrue(analyzer.isLocalPlayerDependent(result), "the result of a call may depend on any argument");
        assertFalse(analyzer.functionInliningIsLocalPlayerSensitive(passthrough),
            "one caller's argument must not make a shared helper a barrier for every other caller");
    }

    @Test
    public void everyOperandOfAMethodCallFlowsIntoEveryParameterOfEveryImplementation() {
        ImVar baseThis = intVar("baseThis");
        ImVar baseValue = intVar("baseValue");
        ImVar subThis = intVar("subThis");
        ImVar subValue = intVar("subValue");
        ImVar receiver = intVar("receiver");
        ImVar result = intVar("result");
        ImFunction baseImplementation = function("C_m", JassIm.ImVars(baseThis, baseValue), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(trace, JassIm.ImIntVal(0))), TypesHelper.imInt());
        ImFunction subImplementation = function("D_m", JassIm.ImVars(subThis, subValue), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(trace, JassIm.ImIntVal(1))), TypesHelper.imInt());
        ImClass base = JassIm.ImClass(trace, "C", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(),
            JassIm.ImFunctions(baseImplementation), Collections.emptyList());
        ImClassType baseType = JassIm.ImClassType(base, JassIm.ImTypeArguments());
        ImMethod subMethod = JassIm.ImMethod(trace, baseType, "m", subImplementation, Collections.emptyList(),
            Collections.emptyList(), "m", false);
        ImMethod method = JassIm.ImMethod(trace, baseType, "m", baseImplementation, List.of(subMethod),
            Collections.emptyList(), "m", false);
        ImFunction user = voidFunction("user", JassIm.ImVars(receiver, result),
            set(result, JassIm.ImMethodCall(trace, method, JassIm.ImTypeArguments(), JassIm.ImVarAccess(receiver),
                JassIm.ImExprs(call(localPlayer)), false)));
        ImProg prog = prog(JassIm.ImVars(), user);
        prog.getClasses().add(base);
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog);

        for (ImVar parameter : List.of(baseThis, baseValue, subThis, subValue)) {
            assertTrue(analyzer.isLocalPlayerDependent(parameter),
                parameter.getName() + " may receive the client-local argument");
        }
        assertTrue(analyzer.isLocalPlayerDependent(result));
        assertFalse(analyzer.isLocalPlayerDependent(receiver), "an operand is not changed by being passed on");
        assertFalse(analyzer.functionInliningIsLocalPlayerSensitive(baseImplementation));
    }

    @Test
    public void aCallOfAMethodWithoutKnownImplementationsMayDependOnAnything() {
        ImVar result = intVar("result");
        ImVar receiver = intVar("receiver");
        ImClass base = JassIm.ImClass(trace, "C", JassIm.ImTypeVars(), JassIm.ImVars(), JassIm.ImMethods(),
            JassIm.ImFunctions(), Collections.emptyList());
        ImMethod abstractMethod = JassIm.ImMethod(trace, JassIm.ImClassType(base, JassIm.ImTypeArguments()), "m",
            null, Collections.emptyList(), Collections.emptyList(), "m", true);
        ImMethodCall methodCall = JassIm.ImMethodCall(trace, abstractMethod, JassIm.ImTypeArguments(),
            JassIm.ImVarAccess(receiver), JassIm.ImExprs(), false);
        ImFunction user = voidFunction("user", JassIm.ImVars(result, receiver), set(result, methodCall));
        ImProg prog = prog(JassIm.ImVars(), user);
        prog.getClasses().add(base);
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog);

        assertTrue(analyzer.isLocalPlayerDependent(methodCall));
        assertTrue(analyzer.isLocalPlayerDependent(result));
        assertTrue(analyzer.functionUsesLocalPlayer(user));
    }

    @Test
    public void anElementMadeAfterTheAnalysisIsJudgedFromItsParts() {
        ImVar tainted = intVar("tainted");
        ImVar clean = intVar("clean");
        ImFunction function = voidFunction("function", JassIm.ImVars(tainted, clean),
            set(tainted, call(localPlayer)),
            set(clean, JassIm.ImIntVal(1)));
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog(JassIm.ImVars(), function));

        // The passes rewrite the program while they keep the analysis, so what they build is not in it.
        assertTrue(analyzer.isLocalPlayerDependent(JassIm.ImVarAccess(tainted)));
        assertFalse(analyzer.isLocalPlayerDependent(JassIm.ImVarAccess(clean)));
        assertFalse(analyzer.isLocalPlayerDependent(JassIm.ImIntVal(5)));
        assertTrue(analyzer.isLocalPlayerDependent(call(localPlayer)));
        assertTrue(analyzer.isLocalPlayerDependent(JassIm.ImOperatorCall(WurstOperator.PLUS,
            JassIm.ImExprs(JassIm.ImIntVal(1), JassIm.ImVarAccess(tainted)))));
        assertFalse(analyzer.isLocalPlayerDependent(JassIm.ImOperatorCall(WurstOperator.PLUS,
            JassIm.ImExprs(JassIm.ImIntVal(1), JassIm.ImVarAccess(clean)))));
    }

    @Test
    public void analysisOfAConstantOnlyFunctionBodyIsClean() {
        ImVar variable = intVar("variable");
        ImSet assignment = set(variable, JassIm.ImIntVal(2));
        ImFunction function = voidFunction("function", JassIm.ImVars(variable), assignment);
        ImFunction empty = voidFunction("empty", JassIm.ImVars());
        LocalPlayerContextAnalyzer analyzer = new LocalPlayerContextAnalyzer(prog(JassIm.ImVars(), function, empty));

        assertFalse(analyzer.isLocalPlayerDependent(assignment));
        assertFalse(analyzer.isLocalPlayerDependent(function.getBody()));
        assertFalse(analyzer.isLocalPlayerDependent(empty.getBody()));
        assertFalse(analyzer.isLocalPlayerDependent(variable));
        assertFalse(analyzer.functionUsesLocalPlayer(function));
    }
}
