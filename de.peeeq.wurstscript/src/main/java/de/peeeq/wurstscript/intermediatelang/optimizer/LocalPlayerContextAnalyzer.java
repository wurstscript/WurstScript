package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;

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
        for (ImFunction function : ImHelper.calculateFunctionsOfProg(prog)) {
            returnFact(function);
            useFact(function);
            if (isClientLocalValueSource(function)) {
                addLocalPlayerSource(function);
            } else if (!function.isNative()) {
                indexElement(function.getBody(), function, entryControlFact(function));
                addDependency(function.getBody(), useFact(function));
            }
        }
        propagateFacts();
    }

    private boolean methodReturnsLocalPlayerDependentValue(ImMethod method) {
        if (method == null || method.getImplementation() == null) {
            return true;
        }
        if (localPlayerDependentReturns.contains(method.getImplementation())) {
            return true;
        }
        for (ImMethod subMethod : method.getSubMethods()) {
            if (methodReturnsLocalPlayerDependentValue(subMethod)) {
                return true;
            }
        }
        return false;
    }

    private void indexElement(Element element, ImFunction owner, Object controlContext) {
        indexedElements.add(element);

        Object branchControl = null;
        if (element instanceof ImIf) {
            ImIf ifStmt = (ImIf) element;
            branchControl = new Fact(FactKind.CONTROL, ifStmt);
            addDependency(ifStmt.getCondition(), branchControl);
            addEnclosingControlDependency(controlContext, branchControl);
        }

        Object loopControl = null;
        if (element instanceof ImLoop) {
            ImLoop loop = (ImLoop) element;
            loopControl = new Fact(FactKind.CONTROL, loop);
            addEnclosingControlDependency(controlContext, loopControl);
            addLoopExitDependencies(loop.getBody(), loopControl);
        }

        if (element instanceof ImStmts) {
            indexStatementSequence((ImStmts) element, owner, controlContext);
            return;
        }

        for (int i = 0; i < element.size(); i++) {
            Element child = element.get(i);
            if (element instanceof ImOperatorCall
                && ((ImOperatorCall) element).getOp().isLazy()
                && child == ((ImOperatorCall) element).getArguments()) {
                indexShortCircuitArguments(
                    ((ImOperatorCall) element).getArguments(),
                    owner,
                    controlContext);
                addDependency(child, element);
                continue;
            }
            Object childControl = controlContext;
            if (element instanceof ImIf
                && (child == ((ImIf) element).getThenBlock()
                || child == ((ImIf) element).getElseBlock())) {
                childControl = branchControl;
            } else if (element instanceof ImLoop
                && child == ((ImLoop) element).getBody()) {
                childControl = loopControl;
            }
            indexElement(child, owner, childControl);
            addDependency(child, element);
        }

        if (element instanceof ImVarAccess) {
            addDependency(variableFact(((ImVarAccess) element).getVar()), element);
        } else if (element instanceof ImVarArrayAccess) {
            addDependency(variableFact(((ImVarArrayAccess) element).getVar()), element);
        } else if (element instanceof ImMemberAccess) {
            addDependency(variableFact(((ImMemberAccess) element).getVar()), element);
        } else if (element instanceof ImVarargLoop) {
            ImVar varargParameter = varargParameter(owner);
            if (varargParameter != null) {
                addDependency(
                    variableFact(varargParameter),
                    variableFact(((ImVarargLoop) element).getLoopVar()));
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

    private void indexStatementSequence(ImStmts statements,
                                        ImFunction owner,
                                        Object controlContext) {
        Object continuationControl = controlContext;
        for (ImStmt statement : statements) {
            indexElement(statement, owner, continuationControl);
            addDependency(statement, statements);

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
    }

    private boolean containsFunctionReturn(Element element) {
        Boolean cached = containsReturnCache.get(element);
        if (cached != null) {
            return cached;
        }
        if (element instanceof ImReturn) {
            containsReturnCache.put(element, true);
            return true;
        }
        for (int i = 0; i < element.size(); i++) {
            if (containsFunctionReturn(element.get(i))) {
                containsReturnCache.put(element, true);
                return true;
            }
        }
        containsReturnCache.put(element, false);
        return false;
    }

    private void indexShortCircuitArguments(ImExprs arguments,
                                            ImFunction owner,
                                            Object controlContext) {
        indexedElements.add(arguments);
        Object operandControl = controlContext;
        for (int i = 0; i < arguments.size(); i++) {
            ImExpr argument = arguments.get(i);
            indexElement(argument, owner, operandControl);
            addDependency(argument, arguments);

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
    }

    private void indexFunctionCall(ImFunctionCall call, ImFunction owner, Object controlContext) {
        ImFunction called = call.getFunc();
        addDependency(returnFact(called), call);
        addDependency(useFact(called), useFact(owner));
        if (!called.isNative()) {
            addEnclosingControlDependency(controlContext, entryControlFact(called));
        }
        if (isClientLocalValueSource(called)) {
            functionsDirectlyUsingLocalPlayer.add(owner);
            addLocalPlayerSource(called);
        }

        int fixedParameterCount = called.getParameters().size();
        if (called.hasFlag(IS_VARARG) && fixedParameterCount > 0) {
            fixedParameterCount--;
        }
        int positionalCount = Math.min(call.getArguments().size(), fixedParameterCount);
        for (int i = 0; i < positionalCount; i++) {
            addDependency(call.getArguments().get(i),
                variableFact(called.getParameters().get(i)));
        }
        ImVar varargParameter = varargParameter(called);
        if (varargParameter != null) {
            for (int i = fixedParameterCount; i < call.getArguments().size(); i++) {
                addDependency(call.getArguments().get(i),
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

        for (ImFunction implementation : implementations) {
            addDependency(returnFact(implementation), call);
            addDependency(useFact(implementation), useFact(owner));
            addEnclosingControlDependency(controlContext, entryControlFact(implementation));
            for (ImVar parameter : implementation.getParameters()) {
                addDependency(call.getReceiver(), variableFact(parameter));
                for (ImExpr argument : call.getArguments()) {
                    addDependency(argument, variableFact(parameter));
                }
            }
        }
    }

    private void addEnclosingControlDependency(Object controlContext, Object dependent) {
        if (controlContext != null) {
            addDependency(controlContext, dependent);
        }
    }

    private boolean addLoopExitDependencies(Element element, Object loopControl) {
        if (element instanceof ImExitwhen) {
            addDependency(((ImExitwhen) element).getCondition(), loopControl);
            return true;
        } else if (element instanceof ImLoop || element instanceof ImVarargLoop) {
            return false;
        }

        boolean containsExit = false;
        for (int i = 0; i < element.size(); i++) {
            containsExit |= addLoopExitDependencies(element.get(i), loopControl);
        }
        if (containsExit && element instanceof ImIf) {
            addDependency(((ImIf) element).getCondition(), loopControl);
        }
        return containsExit;
    }

    private boolean collectMethodImplementations(ImMethod method,
                                                 Set<ImFunction> implementations,
                                                 Set<ImMethod> visited) {
        if (method == null || !visited.add(method) || method.getImplementation() == null) {
            return method != null && method.getImplementation() != null;
        }
        implementations.add(method.getImplementation());
        for (ImMethod subMethod : method.getSubMethods()) {
            if (!collectMethodImplementations(subMethod, implementations, visited)) {
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
            for (ImExpr expr : ((ImTupleExpr) left).getExprs()) {
                if (expr instanceof ImLExpr) {
                    forEachAssignedVariable((ImLExpr) expr, consumer);
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
            for (Object dependent : dependents.getOrDefault(fact, Collections.emptyList())) {
                activateFact(dependent, worklist);
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
