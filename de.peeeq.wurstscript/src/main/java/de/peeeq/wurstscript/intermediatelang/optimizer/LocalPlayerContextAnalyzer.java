package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.jassIm.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;

/**
 * Conservative, flow-insensitive analysis for values and functions which may
 * depend on client-local native values such as {@code GetLocalPlayer()} or
 * camera state.
 *
 * Optimizers use this analysis as a barrier. False positives only cost an
 * optimization; false negatives could move synchronized work into a
 * client-local control-flow region.
 */
public final class LocalPlayerContextAnalyzer {

    /**
     * Native return values which may differ between clients during the same
     * synchronized execution without requiring user code to mutate local state.
     * Event responses are synchronized, while handles and UI/audio/visual state
     * made local by user code remain the user's responsibility.
     */
    private static final Set<String> CLIENT_LOCAL_VALUE_SOURCES = Set.of(
        // Player identity and values explicitly documented as asynchronous.
        "GetLocalPlayer",
        "GetLocationZ",

        // Camera state belongs to the local client's camera.
        "GetCameraMargin",
        "GetCameraBoundMinX",
        "GetCameraBoundMinY",
        "GetCameraBoundMaxX",
        "GetCameraBoundMaxY",
        "GetCameraField",
        "GetCameraTargetPositionX",
        "GetCameraTargetPositionY",
        "GetCameraTargetPositionZ",
        "GetCameraTargetPositionLoc",
        "GetCameraEyePositionX",
        "GetCameraEyePositionY",
        "GetCameraEyePositionZ",
        "GetCameraEyePositionLoc",

        // Localized data may vary with the client's language.
        "GetLocalizedString",
        "GetLocalizedHotkey",
        "GetObjectName",

        // Reforged client-local world and client state.
        "BlzGetLocalUnitZ",
        "BlzGetUnitZ",
        "BlzGetLocalClientWidth",
        "BlzGetLocalClientHeight",
        "BlzIsLocalClientActive",
        "BlzGetMouseFocusUnit",
        "BlzGetLocale"
    );

    private final Set<ImVar> localPlayerDependentVars =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> localPlayerDependentReturns =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> functionsUsingLocalPlayer =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> functionsDirectlyUsingLocalPlayer =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Element> indexedElements =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Object> activeFacts =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Object, List<Object>> dependents = new IdentityHashMap<>();
    private final Map<ImVar, Fact> variableFacts = new IdentityHashMap<>();
    private final Map<ImFunction, Fact> returnFacts = new IdentityHashMap<>();
    private final Map<ImFunction, Fact> useFacts = new IdentityHashMap<>();
    private final Map<ImFunction, Fact> entryControlFacts = new IdentityHashMap<>();
    private final Map<Element, Boolean> containsReturnCache = new IdentityHashMap<>();
    private final Set<Object> sourceFacts =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private final Fact unknownDispatchSource = new Fact(FactKind.SOURCE, null);

    public LocalPlayerContextAnalyzer(ImProg prog) {
        analyze(prog);
    }

    public boolean isLocalPlayerDependent(Element element) {
        if (element == null) {
            return false;
        }
        if (indexedElements.contains(element)) {
            return activeFacts.contains(element);
        }
        if (element instanceof ImVarAccess) {
            return localPlayerDependentVars.contains(((ImVarAccess) element).getVar());
        }
        if (element instanceof ImVarArrayAccess) {
            ImVarArrayAccess access = (ImVarArrayAccess) element;
            if (localPlayerDependentVars.contains(access.getVar())) {
                return true;
            }
        }
        if (element instanceof ImMemberAccess) {
            ImMemberAccess access = (ImMemberAccess) element;
            if (localPlayerDependentVars.contains(access.getVar())) {
                return true;
            }
        }
        if (element instanceof ImFunctionCall) {
            ImFunctionCall call = (ImFunctionCall) element;
            if (isClientLocalValueSource(call.getFunc())
                || localPlayerDependentReturns.contains(call.getFunc())) {
                return true;
            }
            // Conservatively assume a return value can depend on any argument.
            return isLocalPlayerDependent(call.getArguments());
        }
        if (element instanceof ImMethodCall) {
            ImMethodCall call = (ImMethodCall) element;
            return methodReturnsLocalPlayerDependentValue(call.getMethod())
                || isLocalPlayerDependent(call.getReceiver())
                || isLocalPlayerDependent(call.getArguments());
        }
        for (int i = 0; i < element.size(); i++) {
            if (isLocalPlayerDependent(element.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean functionUsesLocalPlayer(ImFunction function) {
        return function != null
            && (isClientLocalValueSource(function) || functionsUsingLocalPlayer.contains(function));
    }

    public boolean functionInliningIsLocalPlayerSensitive(ImFunction function) {
        return function != null
            && (isClientLocalValueSource(function)
            || functionsDirectlyUsingLocalPlayer.contains(function)
            || localPlayerDependentReturns.contains(function));
    }

    public boolean isLocalPlayerDependent(ImVar variable) {
        return variable != null && localPlayerDependentVars.contains(variable);
    }

    public boolean isLocalPlayerSource(ImFunction function) {
        return isClientLocalValueSource(function);
    }

    private void analyze(ImProg prog) {
        sourceFacts.add(unknownDispatchSource);
        analyzeFunctions(prog.getFunctions());
        List<ImClass> classes = prog.getClasses();
        for (int i = 0; i < classes.size(); i++) {
            analyzeFunctions(classes.get(i).getFunctions());
        }
        propagateFacts();
    }

    private void analyzeFunctions(List<ImFunction> functions) {
        for (int i = 0; i < functions.size(); i++) {
            ImFunction function = functions.get(i);
            returnFact(function);
            useFact(function);
            if (isClientLocalValueSource(function)) {
                addLocalPlayerSource(function);
            } else if (!function.isNative()) {
                indexElement(function.getBody(), function, entryControlFact(function));
                addDependency(function.getBody(), useFact(function));
            }
        }
    }

    private boolean methodReturnsLocalPlayerDependentValue(ImMethod method) {
        if (method == null || method.getImplementation() == null) {
            return true;
        }
        if (localPlayerDependentReturns.contains(method.getImplementation())) {
            return true;
        }
        List<ImMethod> subMethods = method.getSubMethods();
        for (int i = 0; i < subMethods.size(); i++) {
            if (methodReturnsLocalPlayerDependentValue(subMethods.get(i))) {
                return true;
            }
        }
        return false;
    }

    private record IndexTask(Element element, Object controlContext, boolean afterChildren) {
    }

    private record ReturnTask(Element element, boolean afterChildren) {
    }

    private record LoopExitTask(Element element, boolean afterChildren) {
    }

    private void indexElement(Element root, ImFunction owner, Object controlContext) {
        Deque<IndexTask> work = new ArrayDeque<>();
        work.addFirst(new IndexTask(root, controlContext, false));
        while (!work.isEmpty()) {
            IndexTask task = work.removeFirst();
            Element element = task.element();
            if (task.afterChildren()) {
                indexElementAfterChildren(element, owner, task.controlContext());
                continue;
            }

            indexedElements.add(element);
            Object branchControl = null;
            if (element instanceof ImIf ifStmt) {
                branchControl = new Fact(FactKind.CONTROL, ifStmt);
                addDependency(ifStmt.getCondition(), branchControl);
                addEnclosingControlDependency(task.controlContext(), branchControl);
            }

            Object loopControl = null;
            if (element instanceof ImLoop loop) {
                loopControl = new Fact(FactKind.CONTROL, loop);
                addEnclosingControlDependency(task.controlContext(), loopControl);
                addLoopExitDependencies(loop.getBody(), loopControl);
            }

            if (element instanceof ImStmts statements) {
                scheduleStatementSequence(statements, task.controlContext(), work);
                continue;
            }

            work.addFirst(new IndexTask(element, task.controlContext(), true));
            for (int i = element.size() - 1; i >= 0; i--) {
                Element child = element.get(i);
                addDependency(child, element);
                if (element instanceof ImOperatorCall operator
                    && operator.getOp().isLazy()
                    && child == operator.getArguments()) {
                    scheduleShortCircuitArguments(operator.getArguments(), task.controlContext(), work);
                    continue;
                }
                Object childControl = task.controlContext();
                if (element instanceof ImIf ifStmt
                    && (child == ifStmt.getThenBlock() || child == ifStmt.getElseBlock())) {
                    childControl = branchControl;
                } else if (element instanceof ImLoop loop && child == loop.getBody()) {
                    childControl = loopControl;
                }
                work.addFirst(new IndexTask(child, childControl, false));
            }
        }
    }

    private void indexElementAfterChildren(Element element, ImFunction owner, Object controlContext) {
        if (element instanceof ImVarAccess) {
            addDependency(variableFact(((ImVarAccess) element).getVar()), element);
        } else if (element instanceof ImVarArrayAccess) {
            addDependency(variableFact(((ImVarArrayAccess) element).getVar()), element);
        } else if (element instanceof ImMemberAccess) {
            addDependency(variableFact(((ImMemberAccess) element).getVar()), element);
        } else if (element instanceof ImVarargLoop) {
            ImVarargLoop loop = (ImVarargLoop) element;
            ImVar varargParameter = varargParameter(owner);
            if (varargParameter != null) {
                List<ImVarargLoopVar> loopVars = loop.getLoopVars();
                for (int i = 0; i < loopVars.size(); i++) {
                    ImVarargLoopVar loopVar = loopVars.get(i);
                    addDependency(variableFact(varargParameter), variableFact(loopVar.getVar()));
                }
            }
        }

        if (element instanceof ImSet) {
            ImSet set = (ImSet) element;
            forEachAssignedVariable(set.getLeft(), variable -> {
                addDependency(set.getLeft(), variableFact(variable));
                addDependency(set.getRight(), variableFact(variable));
                addEnclosingControlDependency(controlContext, variableFact(variable));
            });
        } else if (element instanceof ImReturn) {
            ImReturn returnStmt = (ImReturn) element;
            if (returnStmt.getReturnValue() instanceof ImExpr) {
                addDependency(returnStmt.getReturnValue(), returnFact(owner));
                addEnclosingControlDependency(controlContext, returnFact(owner));
            }
        } else if (element instanceof ImFunctionCall) {
            indexFunctionCall((ImFunctionCall) element, owner, controlContext);
        } else if (element instanceof ImMethodCall) {
            indexMethodCall((ImMethodCall) element, owner, controlContext);
        }
    }

    private void scheduleStatementSequence(ImStmts statements,
                                           Object controlContext,
                                           Deque<IndexTask> work) {
        List<IndexTask> tasks = new ArrayList<>(statements.size());
        Object continuationControl = controlContext;
        for (int i = 0; i < statements.size(); i++) {
            ImStmt statement = statements.get(i);
            addDependency(statement, statements);
            tasks.add(new IndexTask(statement, continuationControl, false));

            if (containsFunctionReturn(statement)) {
                Fact followingStatementControl =
                    new Fact(FactKind.CONTROL, statement);
                addEnclosingControlDependency(
                    continuationControl,
                    followingStatementControl);
                addDependency(statement, followingStatementControl);
                continuationControl = followingStatementControl;
            }
        }
        for (int i = tasks.size() - 1; i >= 0; i--) {
            work.addFirst(tasks.get(i));
        }
    }

    private boolean containsFunctionReturn(Element root) {
        Boolean cached = containsReturnCache.get(root);
        if (cached != null) {
            return cached;
        }
        Deque<ReturnTask> work = new ArrayDeque<>();
        work.addFirst(new ReturnTask(root, false));
        while (!work.isEmpty()) {
            ReturnTask task = work.removeFirst();
            Element element = task.element();
            if (containsReturnCache.containsKey(element)) {
                continue;
            }
            if (element instanceof ImReturn) {
                containsReturnCache.put(element, true);
                continue;
            }
            if (!task.afterChildren()) {
                work.addFirst(new ReturnTask(element, true));
                for (int i = element.size() - 1; i >= 0; i--) {
                    Element child = element.get(i);
                    if (!containsReturnCache.containsKey(child)) {
                        work.addFirst(new ReturnTask(child, false));
                    }
                }
                continue;
            }
            boolean containsReturn = false;
            for (int i = 0; i < element.size(); i++) {
                if (Boolean.TRUE.equals(containsReturnCache.get(element.get(i)))) {
                    containsReturn = true;
                    break;
                }
            }
            containsReturnCache.put(element, containsReturn);
        }
        return Boolean.TRUE.equals(containsReturnCache.get(root));
    }

    private void scheduleShortCircuitArguments(ImExprs arguments,
                                               Object controlContext,
                                               Deque<IndexTask> work) {
        indexedElements.add(arguments);
        List<IndexTask> tasks = new ArrayList<>(arguments.size());
        Object operandControl = controlContext;
        for (int i = 0; i < arguments.size(); i++) {
            ImExpr argument = arguments.get(i);
            addDependency(argument, arguments);
            tasks.add(new IndexTask(argument, operandControl, false));

            if (i + 1 < arguments.size()) {
                Fact followingOperandControl =
                    new Fact(FactKind.CONTROL, argument);
                addEnclosingControlDependency(
                    operandControl,
                    followingOperandControl);
                addDependency(argument, followingOperandControl);
                operandControl = followingOperandControl;
            }
        }
        for (int i = tasks.size() - 1; i >= 0; i--) {
            work.addFirst(tasks.get(i));
        }
    }

    private void indexFunctionCall(ImFunctionCall call, ImFunction owner, Object controlContext) {
        ImFunction called = call.getFunc();
        addDependency(returnFact(called), call);
        addDependency(useFact(called), useFact(owner));
        List<ImExpr> arguments = call.getArguments();
        List<ImVar> calledParameters = called.getParameters();
        if (!called.isNative()) {
            addEnclosingControlDependency(controlContext, entryControlFact(called));
        }
        if (isClientLocalValueSource(called)) {
            functionsDirectlyUsingLocalPlayer.add(owner);
            addLocalPlayerSource(called);
        }

        int fixedParameterCount = calledParameters.size();
        if (called.hasFlag(IS_VARARG) && fixedParameterCount > 0) {
            fixedParameterCount--;
        }
        int argumentCount = arguments.size();
        int positionalCount = Math.min(argumentCount, fixedParameterCount);
        for (int i = 0; i < positionalCount; i++) {
            addDependency(arguments.get(i),
                variableFact(calledParameters.get(i)));
        }
        ImVar varargParameter = varargParameter(called);
        if (varargParameter != null) {
            for (int i = fixedParameterCount; i < argumentCount; i++) {
                addDependency(arguments.get(i),
                    variableFact(varargParameter));
            }
        }
    }

    private ImVar varargParameter(ImFunction function) {
        if (function.hasFlag(IS_VARARG) && !function.getParameters().isEmpty()) {
            return function.getParameters().get(function.getParameters().size() - 1);
        }
        return null;
    }

    private void indexMethodCall(ImMethodCall call, ImFunction owner, Object controlContext) {
        Set<ImFunction> implementations =
            Collections.newSetFromMap(new IdentityHashMap<>());
        boolean allImplementationsKnown = collectMethodImplementations(
            call.getMethod(),
            implementations,
            Collections.newSetFromMap(new IdentityHashMap<>()));
        if (!allImplementationsKnown) {
            addDependency(unknownDispatchSource, call);
            addDependency(unknownDispatchSource, useFact(owner));
        }

        List<ImExpr> arguments = call.getArguments();
        for (ImFunction implementation : implementations) {
            addDependency(returnFact(implementation), call);
            addDependency(useFact(implementation), useFact(owner));
            addEnclosingControlDependency(controlContext, entryControlFact(implementation));
            Element receiver = call.getReceiver();
            List<ImVar> parameters = implementation.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                ImVar parameter = parameters.get(i);
                addDependency(receiver, variableFact(parameter));
                for (int j = 0; j < arguments.size(); j++) {
                    addDependency(arguments.get(j), variableFact(parameter));
                }
            }
        }
    }

    private void addEnclosingControlDependency(Object controlContext, Object dependent) {
        if (controlContext != null) {
            addDependency(controlContext, dependent);
        }
    }

    private boolean addLoopExitDependencies(Element root, Object loopControl) {
        Map<Element, Boolean> containsExit = new IdentityHashMap<>();
        Deque<LoopExitTask> work = new ArrayDeque<>();
        work.addFirst(new LoopExitTask(root, false));
        while (!work.isEmpty()) {
            LoopExitTask task = work.removeFirst();
            Element element = task.element();
            if (element instanceof ImExitwhen exitwhen) {
                addDependency(exitwhen.getCondition(), loopControl);
                containsExit.put(element, true);
                continue;
            }
            if (element instanceof ImLoop || element instanceof ImVarargLoop) {
                containsExit.put(element, false);
                continue;
            }
            if (!task.afterChildren()) {
                work.addFirst(new LoopExitTask(element, true));
                for (int i = element.size() - 1; i >= 0; i--) {
                    work.addFirst(new LoopExitTask(element.get(i), false));
                }
                continue;
            }

            boolean elementContainsExit = false;
            for (int i = 0; i < element.size(); i++) {
                elementContainsExit |= Boolean.TRUE.equals(containsExit.get(element.get(i)));
            }
            if (elementContainsExit && element instanceof ImIf ifStmt) {
                addDependency(ifStmt.getCondition(), loopControl);
            }
            containsExit.put(element, elementContainsExit);
        }
        return Boolean.TRUE.equals(containsExit.get(root));
    }

    private boolean collectMethodImplementations(ImMethod method,
                                                 Set<ImFunction> implementations,
                                                 Set<ImMethod> visited) {
        if (method == null || !visited.add(method) || method.getImplementation() == null) {
            return method != null && method.getImplementation() != null;
        }
        implementations.add(method.getImplementation());
        List<ImMethod> subMethods = method.getSubMethods();
        for (int i = 0; i < subMethods.size(); i++) {
            if (!collectMethodImplementations(subMethods.get(i), implementations, visited)) {
                return false;
            }
        }
        return true;
    }

    private void forEachAssignedVariable(ImLExpr left, Consumer<ImVar> consumer) {
        if (left instanceof ImVarAccess) {
            consumer.accept(((ImVarAccess) left).getVar());
        } else if (left instanceof ImVarArrayAccess) {
            consumer.accept(((ImVarArrayAccess) left).getVar());
        } else if (left instanceof ImMemberAccess) {
            consumer.accept(((ImMemberAccess) left).getVar());
        } else if (left instanceof ImTupleSelection) {
            ImExpr tupleExpr = ((ImTupleSelection) left).getTupleExpr();
            if (tupleExpr instanceof ImLExpr) {
                forEachAssignedVariable((ImLExpr) tupleExpr, consumer);
            }
        } else if (left instanceof ImTupleExpr) {
            ImExprs exprs = ((ImTupleExpr) left).getExprs();
            for (int i = 0; i < exprs.size(); i++) {
                ImExpr expr = exprs.get(i);
                if (expr instanceof ImLExpr lExpr) {
                    forEachAssignedVariable(lExpr, consumer);
                }
            }
        } else if (left instanceof ImStatementExpr) {
            ImExpr expr = ((ImStatementExpr) left).getExpr();
            if (expr instanceof ImLExpr) {
                forEachAssignedVariable((ImLExpr) expr, consumer);
            }
        }
    }

    private void addLocalPlayerSource(ImFunction function) {
        sourceFacts.add(returnFact(function));
        sourceFacts.add(useFact(function));
    }

    private void addDependency(Object dependency, Object dependent) {
        dependents.computeIfAbsent(dependency,
            ignored -> new ArrayList<>())
            .add(dependent);
    }

    private void propagateFacts() {
        Deque<Object> worklist = new ArrayDeque<>();
        for (Object source : sourceFacts) {
            activateFact(source, worklist);
        }
        while (!worklist.isEmpty()) {
            Object fact = worklist.removeFirst();
            List<Object> factDependents = dependents.get(fact);
            if (factDependents != null) {
                for (int i = 0; i < factDependents.size(); i++) {
                    activateFact(factDependents.get(i), worklist);
                }
            }
        }
    }

    private void activateFact(Object fact, Deque<Object> worklist) {
        if (activeFacts.add(fact)) {
            publishFact(fact);
            worklist.addLast(fact);
        }
    }

    private void publishFact(Object fact) {
        if (!(fact instanceof Fact)) {
            return;
        }
        Fact typedFact = (Fact) fact;
        switch (typedFact.kind) {
            case VARIABLE:
                localPlayerDependentVars.add((ImVar) typedFact.subject);
                break;
            case RETURN:
                localPlayerDependentReturns.add((ImFunction) typedFact.subject);
                break;
            case USE:
                functionsUsingLocalPlayer.add((ImFunction) typedFact.subject);
                break;
            case CONTROL:
            case SOURCE:
                break;
        }
    }

    private Fact variableFact(ImVar variable) {
        return variableFacts.computeIfAbsent(variable,
            ignored -> new Fact(FactKind.VARIABLE, variable));
    }

    private Fact returnFact(ImFunction function) {
        return returnFacts.computeIfAbsent(function,
            ignored -> new Fact(FactKind.RETURN, function));
    }

    private Fact useFact(ImFunction function) {
        return useFacts.computeIfAbsent(function,
            ignored -> new Fact(FactKind.USE, function));
    }

    private Fact entryControlFact(ImFunction function) {
        return entryControlFacts.computeIfAbsent(function,
            ignored -> new Fact(FactKind.CONTROL, function));
    }

    private enum FactKind {
        VARIABLE,
        RETURN,
        USE,
        CONTROL,
        SOURCE
    }

    private static final class Fact {
        private final FactKind kind;
        private final Object subject;

        private Fact(FactKind kind, Object subject) {
            this.kind = kind;
            this.subject = subject;
        }
    }

    private static boolean isClientLocalValueSource(ImFunction function) {
        return function != null
            && function.isNative()
            && CLIENT_LOCAL_VALUE_SOURCES.contains(function.getName());
    }
}
