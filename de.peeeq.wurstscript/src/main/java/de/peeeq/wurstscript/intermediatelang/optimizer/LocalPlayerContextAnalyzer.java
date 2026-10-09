package de.peeeq.wurstscript.intermediatelang.optimizer;

import de.peeeq.wurstscript.jassIm.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_VARARG;

/**
 * Conservative, flow-insensitive analysis for values and functions which may
 * depend on client-local native values such as {@code GetLocalPlayer()} or
 * camera state.
 *
 * Optimizers use this analysis as a barrier. False positives only cost an
 * optimization; false negatives could move synchronized work into a
 * client-local control-flow region.
 *
 * <p>The analysis is a reachability problem over a graph whose nodes are the elements of the function
 * bodies and facts about variables, functions and control flow. Every node is made once, so the
 * nodes carry their own state: a fact holds its flags and its outgoing edges, and an element's flags
 * and edges sit in one open-addressing table sized from the number of elements, which is probed once
 * per element visit. An element which cannot become dependent, a leaf which is no variable access, is
 * neither indexed nor given edges. The edges are dropped when the propagation ends, because the
 * analysis is kept by the passes which use it and only the answers are needed then.
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
        "GetCameraFieldControlledByInput",
        "BlzCameraGetCameraType",

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
        "BlzGetLocale",

        // Current cinematic and renderer state belongs to the local client.
        "BlzGetModelCinematicGameShotCount",
        "BlzGetModelCinematicGameCurrentShot",
        "BlzGetModelCinematicGameRemainingTime",
        "BlzGetMinShadowCastingPointLightCount",

        // Polling input and converting viewport coordinates are client-local.
        "BlzIsMetaKeyPressed",
        "BlzIsKeyPressed",
        "BlzIsMouseButtonPressed",
        "BlzGetMouseScreenPosX",
        "BlzGetMouseScreenPosY",
        "BlzPixelToFrameX",
        "BlzPixelToFrameY",
        "BlzFrameToPixelX",
        "BlzFrameToPixelY"
    );

    /** Initial size of the stacks the construction walks the bodies with; they grow when a body is deeper. */
    private static final int INITIAL_STACK = 256;

    /** The flags and edges of the elements which are indexed, or which something refers to. */
    private final ElementTable elements;
    /** Whether a variable depends on a client-local value is the {@code active} flag of its fact. */
    private final Reference2ObjectOpenHashMap<ImVar, Fact> variableFacts;
    private final Reference2ObjectOpenHashMap<ImFunction, FunctionFacts> functionFacts;

    /** The state of the construction only; nothing below is kept once the analysis is made. */
    private final Fact unknownDispatchSource = new Fact();
    private Fact[] sources = new Fact[16];
    private int sourceCount;
    private Element[] returnMarks = new Element[256];
    private int returnMarkCount;
    private Object[] worklist = new Object[1024];
    private int worklistSize;
    private Element[] workElement = new Element[INITIAL_STACK];
    private Fact[] workControl = new Fact[INITIAL_STACK];
    private boolean[] workAfter = new boolean[INITIAL_STACK];
    private int workSize;
    private Element[] frameElement = new Element[INITIAL_STACK];
    private int[] frameIndex = new int[INITIAL_STACK];
    private boolean[] frameFound = new boolean[INITIAL_STACK];
    private Element[] countStack = new Element[INITIAL_STACK];
    private Reference2ObjectOpenHashMap<ImMethod, MethodImplementations> methodImplementations;
    private int bodyElementCount;
    private int variableCount;
    private int functionCount;

    public LocalPlayerContextAnalyzer(ImProg prog) {
        // Every body is walked once to find which statements contain a return, which is also the count the
        // table is sized for, rather than grown through some twenty rehashes of a million entries.
        markReturns(prog);
        elements = new ElementTable(bodyElementCount + returnMarkCount);
        for (int i = 0; i < returnMarkCount; i++) {
            elements.addFlag(elements.findOrInsert(returnMarks[i]), ElementTable.RETURN);
        }
        returnMarks = null;
        variableFacts = new Reference2ObjectOpenHashMap<>(Math.max(16, variableCount));
        functionFacts = new Reference2ObjectOpenHashMap<>(Math.max(16, functionCount));
        methodImplementations = new Reference2ObjectOpenHashMap<>();
        analyze(prog);
        releaseConstructionState();
    }

    public boolean isLocalPlayerDependent(Element element) {
        if (element == null) {
            return false;
        }
        int slot = elements.find(element);
        if (slot >= 0 && elements.hasFlag(slot, ElementTable.INDEXED)) {
            return elements.hasFlag(slot, ElementTable.ACTIVE);
        }
        if (element instanceof ImVarAccess imVarAccess) {
            return variableIsDependent(imVarAccess.getVar());
        }
        if (element instanceof ImVarArrayAccess access) {
            if (variableIsDependent(access.getVar())) {
                return true;
            }
        }
        if (element instanceof ImMemberAccess access) {
            if (variableIsDependent(access.getVar())) {
                return true;
            }
        }
        if (element instanceof ImFunctionCall call) {
            if (isClientLocalValueSource(call.getFunc())
                || returnIsDependent(call.getFunc())) {
                return true;
            }
            // Conservatively assume a return value can depend on any argument.
            return isLocalPlayerDependent(call.getArguments());
        }
        if (element instanceof ImMethodCall call) {
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
        if (function == null) {
            return false;
        }
        if (isClientLocalValueSource(function)) {
            return true;
        }
        FunctionFacts facts = functionFacts.get(function);
        return facts != null && facts.useFact.active;
    }

    /**
     * Whether inlining this function must be refused: it is a client-local native, calls one
     * directly, or returns a value derived from one by data flow. Control taint is deliberately not
     * consulted. A function reachable from a client-local branch has a tainted return fact, but
     * inlining substitutes its body at the call site, where it runs under exactly the control the
     * call already had, so nothing crosses a boundary. The passes which do move code
     * ({@link BranchMerger}, {@link TempMerger}, ...) run after inlining and re-analyse the inlined
     * program, where that control is explicit.
     */
    public boolean functionInliningIsLocalPlayerSensitive(ImFunction function) {
        if (function == null) {
            return false;
        }
        if (isClientLocalValueSource(function)) {
            return true;
        }
        FunctionFacts facts = functionFacts.get(function);
        return facts != null && (facts.directlyUsesLocalPlayer || facts.returnFact.reached);
    }

    public boolean isLocalPlayerDependent(ImVar variable) {
        return variable != null && variableIsDependent(variable);
    }

    public boolean isLocalPlayerSource(ImFunction function) {
        return isClientLocalValueSource(function);
    }

    private boolean variableIsDependent(ImVar variable) {
        Fact fact = variableFacts.get(variable);
        return fact != null && fact.active;
    }

    private boolean returnIsDependent(ImFunction function) {
        FunctionFacts facts = functionFacts.get(function);
        return facts != null && facts.returnFact.active;
    }

    private void analyze(ImProg prog) {
        sources[sourceCount++] = unknownDispatchSource;
        analyzeFunctions(prog.getFunctions());
        List<ImClass> classes = prog.getClasses();
        for (int i = 0; i < classes.size(); i++) {
            analyzeFunctions(classes.get(i).getFunctions());
        }
        propagateFacts();
        propagateDataFacts();
    }

    private void analyzeFunctions(List<ImFunction> functions) {
        for (int i = 0; i < functions.size(); i++) {
            ImFunction function = functions.get(i);
            FunctionFacts facts = functionFacts(function);
            if (facts.source) {
                addLocalPlayerSource(facts);
            } else if (!function.isNative()) {
                ImStmts body = function.getBody();
                indexElement(body, function, entryControlFact(facts));
                addElementEdge(body, facts.useFact, true);
            }
        }
    }

    private boolean methodReturnsLocalPlayerDependentValue(ImMethod method) {
        if (method == null || method.getImplementation() == null) {
            return true;
        }
        if (returnIsDependent(method.getImplementation())) {
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

    /**
     * An element which can never depend on a client-local value, and so needs neither flags nor edges: it
     * is activated by a fact (only variable accesses and calls are, and calls have operands) or by one
     * of its children.
     */
    private static boolean isInert(Element element) {
        return !(element instanceof ImVarAccess) && element.size() == 0;
    }

    // ---- the walk of the bodies ----

    private void pushWork(Element element, Fact control, boolean afterChildren) {
        if (workSize == workElement.length) {
            growWork(workSize + 1);
        }
        workElement[workSize] = element;
        workControl[workSize] = control;
        workAfter[workSize] = afterChildren;
        workSize++;
    }

    private void growWork(int minimum) {
        int capacity = Math.max(minimum, workElement.length * 2);
        workElement = Arrays.copyOf(workElement, capacity);
        workControl = Arrays.copyOf(workControl, capacity);
        workAfter = Arrays.copyOf(workAfter, capacity);
    }

    /** Reverses the tasks of one statement or operand sequence, pushed in order, so that they pop in order. */
    private void reverseWork(int from, int to) {
        for (int i = from, j = to - 1; i < j; i++, j--) {
            Element element = workElement[i];
            workElement[i] = workElement[j];
            workElement[j] = element;
            Fact control = workControl[i];
            workControl[i] = workControl[j];
            workControl[j] = control;
            // Tasks of a sequence are never after-children tasks.
        }
    }

    private void indexElement(Element root, ImFunction owner, Fact rootControl) {
        if (isInert(root)) {
            return;
        }
        pushWork(root, rootControl, false);
        while (workSize > 0) {
            int top = --workSize;
            Element element = workElement[top];
            Fact control = workControl[top];
            workElement[top] = null;
            workControl[top] = null;
            if (workAfter[top]) {
                indexElementAfterChildren(element, owner, control);
                continue;
            }

            elements.addFlag(elements.findOrInsert(element), ElementTable.INDEXED);
            ImIf ifStmt = element instanceof ImIf imIf ? imIf : null;
            Fact branchControl = null;
            if (ifStmt != null) {
                branchControl = new Fact();
                addElementEdge(ifStmt.getCondition(), branchControl, true);
                addEnclosingControlDependency(control, branchControl);
            }

            ImLoop loop = element instanceof ImLoop imLoop ? imLoop : null;
            Fact loopControl = null;
            if (loop != null) {
                loopControl = new Fact();
                addEnclosingControlDependency(control, loopControl);
                addLoopExitDependencies(loop.getBody(), loopControl);
            }

            if (element instanceof ImStmts statements) {
                scheduleStatementSequence(statements, control);
                continue;
            }

            int size = element.size();
            if (hasIndexingAfterChildren(element)) {
                if (size == 0) {
                    // Nothing is indexed between a leaf and its own after-children task.
                    indexElementAfterChildren(element, owner, control);
                    continue;
                }
                pushWork(element, control, true);
            }
            ImExprs shortCircuitArguments =
                element instanceof ImOperatorCall operator && operator.getOp().isLazy() ? operator.getArguments() : null;
            for (int i = size - 1; i >= 0; i--) {
                Element child = element.get(i);
                if (child == shortCircuitArguments) {
                    scheduleShortCircuitArguments(shortCircuitArguments, control);
                    continue;
                }
                if (isInert(child)) {
                    continue;
                }
                Fact childControl = control;
                if (ifStmt != null && (child == ifStmt.getThenBlock() || child == ifStmt.getElseBlock())) {
                    childControl = branchControl;
                } else if (loop != null && child == loop.getBody()) {
                    childControl = loopControl;
                }
                pushWork(child, childControl, false);
            }
        }
    }

    private static boolean hasIndexingAfterChildren(Element element) {
        return element instanceof ImVarAccess
            || element instanceof ImVarArrayAccess
            || element instanceof ImMemberAccess
            || element instanceof ImVarargLoop
            || element instanceof ImSet
            || element instanceof ImReturn
            || element instanceof ImFunctionCall
            || element instanceof ImMethodCall;
    }

    private void indexElementAfterChildren(Element element, ImFunction owner, Fact control) {
        if (element instanceof ImVarAccess imVarAccess) {
            addFactEdge(variableFact(imVarAccess.getVar()), element, true);
        } else if (element instanceof ImVarArrayAccess imVarArrayAccess) {
            addFactEdge(variableFact(imVarArrayAccess.getVar()), element, true);
        } else if (element instanceof ImMemberAccess imMemberAccess) {
            addFactEdge(variableFact(imMemberAccess.getVar()), element, true);
        } else if (element instanceof ImVarargLoop loop) {
            ImVar varargParameter = varargParameter(owner);
            if (varargParameter != null) {
                List<ImVarargLoopVar> loopVars = loop.getLoopVars();
                for (int i = 0; i < loopVars.size(); i++) {
                    ImVarargLoopVar loopVar = loopVars.get(i);
                    addFactEdge(variableFact(varargParameter), variableFact(loopVar.getVar()), true);
                }
            }
        }

        if (element instanceof ImSet set) {
            indexAssignment(set.getLeft(), set, control);
        } else if (element instanceof ImReturn returnStmt) {
            if (returnStmt.getReturnValue() instanceof ImExpr) {
                Fact returnFact = functionFacts(owner).returnFact;
                addElementEdge(returnStmt.getReturnValue(), returnFact, true);
                addEnclosingControlDependency(control, returnFact);
            }
        } else if (element instanceof ImFunctionCall imFunctionCall) {
            indexFunctionCall(imFunctionCall, owner, control);
        } else if (element instanceof ImMethodCall imMethodCall) {
            indexMethodCall(imMethodCall, owner, control);
        }
    }

    /** Every variable assigned through {@code left} depends on what is assigned and on the control of the assignment. */
    private void indexAssignment(ImLExpr left, ImSet set, Fact control) {
        if (left instanceof ImVarAccess imVarAccess) {
            indexAssignedVariable(imVarAccess.getVar(), set, control);
        } else if (left instanceof ImVarArrayAccess imVarArrayAccess) {
            indexAssignedVariable(imVarArrayAccess.getVar(), set, control);
        } else if (left instanceof ImMemberAccess imMemberAccess) {
            indexAssignedVariable(imMemberAccess.getVar(), set, control);
        } else if (left instanceof ImTupleSelection imTupleSelection) {
            ImExpr tupleExpr = imTupleSelection.getTupleExpr();
            if (tupleExpr instanceof ImLExpr imLExpr) {
                indexAssignment(imLExpr, set, control);
            }
        } else if (left instanceof ImTupleExpr imTupleExpr) {
            ImExprs exprs = imTupleExpr.getExprs();
            for (int i = 0; i < exprs.size(); i++) {
                ImExpr expr = exprs.get(i);
                if (expr instanceof ImLExpr lExpr) {
                    indexAssignment(lExpr, set, control);
                }
            }
        } else if (left instanceof ImStatementExpr imStatementExpr) {
            ImExpr expr = imStatementExpr.getExpr();
            if (expr instanceof ImLExpr imLExpr) {
                indexAssignment(imLExpr, set, control);
            }
        }
    }

    private void indexAssignedVariable(ImVar variable, ImSet set, Fact control) {
        Fact fact = variableFact(variable);
        addElementEdge(set.getLeft(), fact, true);
        addElementEdge(set.getRight(), fact, true);
        addEnclosingControlDependency(control, fact);
    }

    private void scheduleStatementSequence(ImStmts statements, Fact control) {
        int count = statements.size();
        if (count == 0) {
            return;
        }
        if (workSize + count > workElement.length) {
            growWork(workSize + count);
        }
        int base = workSize;
        Fact continuationControl = control;
        for (int i = 0; i < count; i++) {
            ImStmt statement = statements.get(i);
            if (!isInert(statement)) {
                workElement[workSize] = statement;
                workControl[workSize] = continuationControl;
                workAfter[workSize] = false;
                workSize++;
            }

            if (containsFunctionReturn(statement)) {
                Fact followingStatementControl = new Fact();
                addEnclosingControlDependency(continuationControl, followingStatementControl);
                addElementEdge(statement, followingStatementControl, true);
                continuationControl = followingStatementControl;
            }
        }
        reverseWork(base, workSize);
    }

    private boolean containsFunctionReturn(Element root) {
        int slot = elements.find(root);
        return slot >= 0 && elements.hasFlag(slot, ElementTable.RETURN);
    }

    private void scheduleShortCircuitArguments(ImExprs arguments, Fact control) {
        elements.addFlag(elements.findOrInsert(arguments), ElementTable.INDEXED);
        int count = arguments.size();
        if (workSize + count > workElement.length) {
            growWork(workSize + count);
        }
        int base = workSize;
        Fact operandControl = control;
        for (int i = 0; i < count; i++) {
            ImExpr argument = arguments.get(i);
            if (!isInert(argument)) {
                workElement[workSize] = argument;
                workControl[workSize] = operandControl;
                workAfter[workSize] = false;
                workSize++;
            }

            if (i + 1 < count) {
                Fact followingOperandControl = new Fact();
                addEnclosingControlDependency(operandControl, followingOperandControl);
                addElementEdge(argument, followingOperandControl, true);
                operandControl = followingOperandControl;
            }
        }
        reverseWork(base, workSize);
    }

    private void indexFunctionCall(ImFunctionCall call, ImFunction owner, Fact control) {
        ImFunction called = call.getFunc();
        FunctionFacts calledFacts = functionFacts(called);
        FunctionFacts ownerFacts = functionFacts(owner);
        addFactEdge(calledFacts.returnFact, call, true);
        addFactEdge(calledFacts.useFact, ownerFacts.useFact, true);
        List<ImExpr> arguments = call.getArguments();
        List<ImVar> calledParameters = called.getParameters();
        if (!called.isNative()) {
            addEnclosingControlDependency(control, entryControlFact(calledFacts));
        }
        if (calledFacts.source) {
            ownerFacts.directlyUsesLocalPlayer = true;
            addLocalPlayerSource(calledFacts);
        }

        int fixedParameterCount = calledParameters.size();
        if (called.hasFlag(IS_VARARG) && fixedParameterCount > 0) {
            fixedParameterCount--;
        }
        int argumentCount = arguments.size();
        int positionalCount = Math.min(argumentCount, fixedParameterCount);
        for (int i = 0; i < positionalCount; i++) {
            addElementEdge(arguments.get(i), variableFact(calledParameters.get(i)), false);
        }
        ImVar varargParameter = varargParameter(called);
        if (varargParameter != null) {
            Fact varargFact = variableFact(varargParameter);
            for (int i = fixedParameterCount; i < argumentCount; i++) {
                addElementEdge(arguments.get(i), varargFact, false);
            }
        }
    }

    private ImVar varargParameter(ImFunction function) {
        if (function.hasFlag(IS_VARARG) && !function.getParameters().isEmpty()) {
            return function.getParameters().get(function.getParameters().size() - 1);
        }
        return null;
    }

    /**
     * Every operand of a method call may flow into every parameter of every implementation which can
     * run. The operands go through one fact of their own when that makes fewer edges than connecting each
     * to each; a fact nothing else refers to adds no conclusion of its own.
     */
    private void indexMethodCall(ImMethodCall call, ImFunction owner, Fact control) {
        MethodImplementations resolved = implementationsOf(call.getMethod());
        FunctionFacts ownerFacts = functionFacts(owner);
        if (!resolved.allImplementationsKnown) {
            addFactEdge(unknownDispatchSource, call, true);
            addFactEdge(unknownDispatchSource, ownerFacts.useFact, true);
        }

        ImFunction[] implementations = resolved.implementations;
        int parameterTotal = 0;
        for (ImFunction implementation : implementations) {
            FunctionFacts facts = functionFacts(implementation);
            addFactEdge(facts.returnFact, call, true);
            addFactEdge(facts.useFact, ownerFacts.useFact, true);
            addEnclosingControlDependency(control, entryControlFact(facts));
            parameterTotal += implementation.getParameters().size();
        }
        if (parameterTotal == 0) {
            return;
        }

        List<ImExpr> arguments = call.getArguments();
        Element receiver = call.getReceiver();
        int operandTotal = isInert(receiver) ? 0 : 1;
        for (int j = 0; j < arguments.size(); j++) {
            if (!isInert(arguments.get(j))) {
                operandTotal++;
            }
        }
        if (operandTotal == 0) {
            return;
        }
        // Connecting each of n operands to each of m parameters takes n * m edges, through one fact n + m.
        Fact union = (operandTotal - 1) * (parameterTotal - 1) > 1 ? new Fact() : null;
        for (ImFunction implementation : implementations) {
            List<ImVar> parameters = implementation.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                Fact parameter = variableFact(parameters.get(i));
                if (union != null) {
                    addFactEdge(union, parameter, false);
                    continue;
                }
                addElementEdge(receiver, parameter, false);
                for (int j = 0; j < arguments.size(); j++) {
                    addElementEdge(arguments.get(j), parameter, false);
                }
            }
        }
        if (union != null) {
            addElementEdge(receiver, union, false);
            for (int j = 0; j < arguments.size(); j++) {
                addElementEdge(arguments.get(j), union, false);
            }
        }
    }

    /** The implementations a call of {@code method} can reach, resolved once for all the calls of it. */
    private MethodImplementations implementationsOf(ImMethod method) {
        if (method == null) {
            return MethodImplementations.UNKNOWN;
        }
        MethodImplementations cached = methodImplementations.get(method);
        if (cached == null) {
            Set<ImFunction> implementations = Collections.newSetFromMap(new IdentityHashMap<>());
            List<ImFunction> inOrder = new ArrayList<>();
            boolean allImplementationsKnown = collectMethodImplementations(
                method,
                implementations,
                inOrder,
                Collections.newSetFromMap(new IdentityHashMap<>()));
            cached = new MethodImplementations(inOrder.toArray(new ImFunction[0]), allImplementationsKnown);
            methodImplementations.put(method, cached);
        }
        return cached;
    }

    private boolean collectMethodImplementations(ImMethod method,
                                                 Set<ImFunction> implementations,
                                                 List<ImFunction> inOrder,
                                                 Set<ImMethod> visited) {
        if (method == null || !visited.add(method) || method.getImplementation() == null) {
            return method != null && method.getImplementation() != null;
        }
        if (implementations.add(method.getImplementation())) {
            inOrder.add(method.getImplementation());
        }
        List<ImMethod> subMethods = method.getSubMethods();
        for (int i = 0; i < subMethods.size(); i++) {
            if (!collectMethodImplementations(subMethods.get(i), implementations, inOrder, visited)) {
                return false;
            }
        }
        return true;
    }

    private void addLoopExitDependencies(Element root, Fact loopControl) {
        int depth = 0;
        depth = pushFrame(depth, root);
        while (depth > 0) {
            int top = depth - 1;
            Element element = frameElement[top];
            int next = frameIndex[top];
            if (next == 0) {
                if (element instanceof ImExitwhen exitwhen) {
                    addElementEdge(exitwhen.getCondition(), loopControl, true);
                    depth--;
                    if (depth > 0) {
                        frameFound[depth - 1] = true;
                    }
                    continue;
                }
                if (element instanceof ImLoop || element instanceof ImVarargLoop) {
                    // An exit inside a nested loop leaves that loop.
                    depth--;
                    continue;
                }
            }
            if (next < element.size()) {
                frameIndex[top] = next + 1;
                Element child = element.get(next);
                if (!isInert(child)) {
                    depth = pushFrame(depth, child);
                }
                continue;
            }
            boolean containsExit = frameFound[top];
            depth--;
            if (containsExit) {
                if (element instanceof ImIf ifStmt) {
                    addElementEdge(ifStmt.getCondition(), loopControl, true);
                }
                if (depth > 0) {
                    frameFound[depth - 1] = true;
                }
            }
        }
    }

    private int pushFrame(int depth, Element element) {
        if (depth == frameElement.length) {
            int capacity = depth * 2;
            frameElement = Arrays.copyOf(frameElement, capacity);
            frameIndex = Arrays.copyOf(frameIndex, capacity);
            frameFound = Arrays.copyOf(frameFound, capacity);
        }
        frameElement[depth] = element;
        frameIndex[depth] = 0;
        frameFound[depth] = false;
        return depth + 1;
    }

    /**
     * Walks every body once. Finds the statements which contain a return, which is the set of the
     * returns of the bodies and every element above one, up to the body: the statements after one run
     * under control the return depended on. Counts what the walk of the bodies will index, and the
     * variables whose facts it will make.
     */
    private void markReturns(ImProg prog) {
        variableCount = prog.getGlobals().size();
        markReturns(prog.getFunctions());
        List<ImClass> classes = prog.getClasses();
        for (int i = 0; i < classes.size(); i++) {
            variableCount += classes.get(i).getFields().size();
            markReturns(classes.get(i).getFunctions());
        }
    }

    private void markReturns(List<ImFunction> functions) {
        functionCount += functions.size();
        for (int f = 0; f < functions.size(); f++) {
            ImFunction function = functions.get(f);
            variableCount += function.getParameters().size() + function.getLocals().size();
            if (function.isNative()) {
                continue;
            }
            Element body = function.getBody();
            if (isInert(body)) {
                continue;
            }
            bodyElementCount++;
            int depth = pushFrame(0, body);
            while (depth > 0) {
                int top = depth - 1;
                Element element = frameElement[top];
                if (element instanceof ImReturn) {
                    // The returns are the leaves of this walk, whatever is below one.
                    bodyElementCount += countBelow(element);
                    addReturnMark(element);
                    depth--;
                    if (depth > 0) {
                        frameFound[depth - 1] = true;
                    }
                    continue;
                }
                int next = frameIndex[top];
                if (next < element.size()) {
                    frameIndex[top] = next + 1;
                    Element child = element.get(next);
                    if (!(child instanceof ImReturn) && isInert(child)) {
                        continue;
                    }
                    bodyElementCount++;
                    depth = pushFrame(depth, child);
                    continue;
                }
                boolean containsReturn = frameFound[top];
                depth--;
                if (containsReturn) {
                    addReturnMark(element);
                    if (depth > 0) {
                        frameFound[depth - 1] = true;
                    }
                }
            }
        }
    }

    /** The elements below one which will be indexed. */
    private int countBelow(Element root) {
        int count = 0;
        int top = 0;
        countStack[top++] = root;
        while (top > 0) {
            Element element = countStack[--top];
            for (int i = 0, n = element.size(); i < n; i++) {
                Element child = element.get(i);
                if (isInert(child)) {
                    continue;
                }
                count++;
                if (top == countStack.length) {
                    countStack = Arrays.copyOf(countStack, top * 2);
                }
                countStack[top++] = child;
            }
        }
        return count;
    }

    private void addReturnMark(Element element) {
        if (returnMarkCount == returnMarks.length) {
            returnMarks = Arrays.copyOf(returnMarks, returnMarkCount * 2);
        }
        returnMarks[returnMarkCount++] = element;
    }

    // ---- the graph ----

    private void addEnclosingControlDependency(Fact controlContext, Fact dependent) {
        if (controlContext != null) {
            // A control edge: present in the full graph only, never in the data graph.
            controlContext.other = addEdge(controlContext.other, dependent);
        }
    }

    /**
     * An edge from a fact. A data edge is in both graphs; any other edge is a control edge, or a
     * call-site argument flowing into a callee parameter. Those are in the full graph only: the data
     * graph answers what a function computes from its own body, so it must not merge the arguments
     * of every caller into the parameter. With that merge, one client-local argument to a shared
     * helper such as {@code max} would taint the helper and everything computed from its result.
     */
    private static void addFactEdge(Fact from, Object to, boolean data) {
        if (data) {
            from.data = addEdge(from.data, to);
        } else {
            from.other = addEdge(from.other, to);
        }
    }

    /** An edge from an element to a fact; an element which can never be dependent has none. */
    private void addElementEdge(Element from, Fact to, boolean data) {
        if (isInert(from)) {
            return;
        }
        elements.addEdge(elements.findOrInsert(from), to, data);
    }

    private static Object addEdge(Object edges, Object target) {
        if (edges == null) {
            return target;
        }
        if (edges instanceof EdgeList list) {
            list.add(target);
            return list;
        }
        EdgeList list = new EdgeList();
        list.add(edges);
        list.add(target);
        return list;
    }

    private void addLocalPlayerSource(FunctionFacts facts) {
        if (facts.registeredAsSource) {
            return;
        }
        facts.registeredAsSource = true;
        addSource(facts.returnFact);
        addSource(facts.useFact);
    }

    private void addSource(Fact fact) {
        if (sourceCount == sources.length) {
            sources = Arrays.copyOf(sources, sourceCount * 2);
        }
        sources[sourceCount++] = fact;
    }

    // ---- the propagation ----

    private void pushWorklist(Object node) {
        if (worklistSize == worklist.length) {
            worklist = Arrays.copyOf(worklist, worklistSize * 2);
        }
        worklist[worklistSize++] = node;
    }

    private void propagateFacts() {
        for (int i = 0; i < sourceCount; i++) {
            activateFact(sources[i]);
        }
        while (worklistSize > 0) {
            Object node = worklist[--worklistSize];
            worklist[worklistSize] = null;
            if (node instanceof Fact fact) {
                activateAll(fact.data);
                activateAll(fact.other);
                continue;
            }
            Element element = (Element) node;
            int slot = elements.find(element);
            // Read before activating anything: a table which grows moves the slots.
            Object data = elements.dataEdges[slot];
            Object other = elements.otherEdges[slot];
            activateIndexedParent(element);
            activateAll(data);
            activateAll(other);
        }
    }

    private void activateAll(Object edges) {
        if (edges == null) {
            return;
        }
        if (edges instanceof EdgeList list) {
            Object[] items = list.items;
            for (int i = 0, n = list.size; i < n; i++) {
                activate(items[i]);
            }
        } else {
            activate(edges);
        }
    }

    private void activate(Object node) {
        if (node instanceof Fact fact) {
            activateFact(fact);
            return;
        }
        Element element = (Element) node;
        int slot = elements.findOrInsert(element);
        if (!elements.hasFlag(slot, ElementTable.ACTIVE)) {
            elements.addFlag(slot, ElementTable.ACTIVE);
            pushWorklist(element);
        }
    }

    private void activateFact(Fact fact) {
        if (!fact.active) {
            fact.active = true;
            pushWorklist(fact);
        }
    }

    /**
     * What an element makes dependent through the tree itself: a value or statement is part of
     * its parent. The roots of the indexed bodies are not part of an indexed element.
     */
    private void activateIndexedParent(Element element) {
        Element parent = element.getParent();
        if (parent == null) {
            return;
        }
        int slot = elements.find(parent);
        if (slot >= 0
            && elements.hasFlag(slot, ElementTable.INDEXED)
            && !elements.hasFlag(slot, ElementTable.ACTIVE)) {
            elements.addFlag(slot, ElementTable.ACTIVE);
            pushWorklist(parent);
        }
    }

    /**
     * Second pass over the data-only graph. Marks just the RETURN facts, which is what the
     * inlining barrier needs: whether a return value is derived from a client-local value regardless
     * of where the function happens to be called from.
     */
    private void propagateDataFacts() {
        for (int i = 0; i < sourceCount; i++) {
            reachFact(sources[i]);
        }
        while (worklistSize > 0) {
            Object node = worklist[--worklistSize];
            worklist[worklistSize] = null;
            if (node instanceof Fact fact) {
                reachAll(fact.data);
                continue;
            }
            Element element = (Element) node;
            Object data = elements.dataEdges[elements.find(element)];
            Element parent = element.getParent();
            if (parent != null) {
                int parentSlot = elements.find(parent);
                if (parentSlot >= 0
                    && elements.hasFlag(parentSlot, ElementTable.INDEXED)
                    && !elements.hasFlag(parentSlot, ElementTable.REACHED)) {
                    elements.addFlag(parentSlot, ElementTable.REACHED);
                    pushWorklist(parent);
                }
            }
            reachAll(data);
        }
    }

    private void reachAll(Object edges) {
        if (edges == null) {
            return;
        }
        if (edges instanceof EdgeList list) {
            Object[] items = list.items;
            for (int i = 0, n = list.size; i < n; i++) {
                reach(items[i]);
            }
        } else {
            reach(edges);
        }
    }

    private void reach(Object node) {
        if (node instanceof Fact fact) {
            reachFact(fact);
            return;
        }
        Element element = (Element) node;
        int slot = elements.findOrInsert(element);
        if (!elements.hasFlag(slot, ElementTable.REACHED)) {
            elements.addFlag(slot, ElementTable.REACHED);
            pushWorklist(element);
        }
    }

    private void reachFact(Fact fact) {
        if (!fact.reached) {
            fact.reached = true;
            pushWorklist(fact);
        }
    }

    /** Drops the graph and the scratch space: the passes keep the analysis, and only need what it concluded. */
    private void releaseConstructionState() {
        elements.dropEdges();
        unknownDispatchSource.dropEdges();
        for (Fact fact : variableFacts.values()) {
            fact.dropEdges();
        }
        for (FunctionFacts facts : functionFacts.values()) {
            facts.returnFact.dropEdges();
            facts.useFact.dropEdges();
            if (facts.entryControlFact != null) {
                facts.entryControlFact.dropEdges();
            }
        }
        sources = null;
        worklist = null;
        workElement = null;
        workControl = null;
        workAfter = null;
        frameElement = null;
        frameIndex = null;
        frameFound = null;
        countStack = null;
        methodImplementations = null;
    }

    // ---- facts ----

    private Fact variableFact(ImVar variable) {
        Fact fact = variableFacts.get(variable);
        if (fact == null) {
            fact = new Fact();
            variableFacts.put(variable, fact);
        }
        return fact;
    }

    private FunctionFacts functionFacts(ImFunction function) {
        FunctionFacts facts = functionFacts.get(function);
        if (facts == null) {
            facts = new FunctionFacts(isClientLocalValueSource(function));
            functionFacts.put(function, facts);
        }
        return facts;
    }

    private static Fact entryControlFact(FunctionFacts facts) {
        if (facts.entryControlFact == null) {
            facts.entryControlFact = new Fact();
        }
        return facts.entryControlFact;
    }

    /**
     * A node of the graph which is not an element: whether a variable depends on a client-local value,
     * whether a function's return value or use of one does, or the control of a branch.
     */
    private static final class Fact {
        /** Depends on a client-local value, by data flow or by control. */
        private boolean active;
        /** Depends on a client-local value by data flow alone. */
        private boolean reached;
        /** Edges of both graphs: null, one target (a {@link Fact} or an {@link Element}), or an {@link EdgeList}. */
        private Object data;
        /** Edges of the full graph only. */
        private Object other;

        private void dropEdges() {
            data = null;
            other = null;
        }
    }

    /** The facts of one function. */
    private static final class FunctionFacts {
        private final Fact returnFact = new Fact();
        private final Fact useFact = new Fact();
        private Fact entryControlFact;
        /** The function is a client-local native. */
        private final boolean source;
        private boolean registeredAsSource;
        /** The function calls a client-local native. */
        private boolean directlyUsesLocalPlayer;

        private FunctionFacts(boolean source) {
            this.source = source;
        }
    }

    private static final class MethodImplementations {
        private static final MethodImplementations UNKNOWN = new MethodImplementations(new ImFunction[0], false);
        private final ImFunction[] implementations;
        private final boolean allImplementationsKnown;

        private MethodImplementations(ImFunction[] implementations, boolean allImplementationsKnown) {
            this.implementations = implementations;
            this.allImplementationsKnown = allImplementationsKnown;
        }
    }

    private static final class EdgeList {
        private Object[] items = new Object[4];
        private int size;

        private void add(Object target) {
            if (size == items.length) {
                items = Arrays.copyOf(items, size * 2);
            }
            items[size++] = target;
        }
    }

    /**
     * An identity-keyed open-addressing table of elements: a byte of flags and the edges out of each, in
     * parallel arrays, so that one probe finds all of it. Sized for the number of elements it will hold.
     */
    private static final class ElementTable {
        private static final byte INDEXED = 1;
        private static final byte ACTIVE = 2;
        private static final byte REACHED = 4;
        /** The element is a return of a body, or above one. */
        private static final byte RETURN = 8;

        private Element[] keys;
        private byte[] flags;
        private Object[] dataEdges;
        private Object[] otherEdges;
        private int mask;
        private int size;
        private int limit;

        private ElementTable(int expected) {
            int capacity = 64;
            while ((long) capacity * 3 < (long) expected * 5) {
                capacity <<= 1;
            }
            allocate(capacity);
        }

        private void allocate(int capacity) {
            keys = new Element[capacity];
            flags = new byte[capacity];
            dataEdges = new Object[capacity];
            otherEdges = new Object[capacity];
            mask = capacity - 1;
            limit = (int) ((long) capacity * 3 / 5);
        }

        private static int hash(Object element) {
            int h = System.identityHashCode(element) * 0x9E3779B9;
            return h ^ (h >>> 16);
        }

        /** The slot of the element, or -1. */
        private int find(Object element) {
            int pos = hash(element) & mask;
            Element key;
            while ((key = keys[pos]) != null) {
                if (key == element) {
                    return pos;
                }
                pos = (pos + 1) & mask;
            }
            return -1;
        }

        private int findOrInsert(Element element) {
            int pos = hash(element) & mask;
            Element key;
            while ((key = keys[pos]) != null) {
                if (key == element) {
                    return pos;
                }
                pos = (pos + 1) & mask;
            }
            if (size >= limit) {
                grow();
                pos = hash(element) & mask;
                while (keys[pos] != null) {
                    pos = (pos + 1) & mask;
                }
            }
            keys[pos] = element;
            size++;
            return pos;
        }

        private void grow() {
            Element[] oldKeys = keys;
            byte[] oldFlags = flags;
            Object[] oldData = dataEdges;
            Object[] oldOther = otherEdges;
            allocate(oldKeys.length * 2);
            for (int i = 0; i < oldKeys.length; i++) {
                Element key = oldKeys[i];
                if (key == null) {
                    continue;
                }
                int pos = hash(key) & mask;
                while (keys[pos] != null) {
                    pos = (pos + 1) & mask;
                }
                keys[pos] = key;
                flags[pos] = oldFlags[i];
                dataEdges[pos] = oldData[i];
                otherEdges[pos] = oldOther[i];
            }
        }

        private boolean hasFlag(int slot, byte flag) {
            return (flags[slot] & flag) != 0;
        }

        private void addFlag(int slot, byte flag) {
            flags[slot] |= flag;
        }

        private void addEdge(int slot, Fact to, boolean data) {
            if (data) {
                dataEdges[slot] = LocalPlayerContextAnalyzer.addEdge(dataEdges[slot], to);
            } else {
                otherEdges[slot] = LocalPlayerContextAnalyzer.addEdge(otherEdges[slot], to);
            }
        }

        private void dropEdges() {
            dataEdges = null;
            otherEdges = null;
        }
    }

    private static boolean isClientLocalValueSource(ImFunction function) {
        return function != null
            && function.isNative()
            && CLIENT_LOCAL_VALUE_SOURCES.contains(function.getName());
    }
}
