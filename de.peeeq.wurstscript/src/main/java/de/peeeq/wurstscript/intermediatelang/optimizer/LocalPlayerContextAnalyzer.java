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

/**
 * Conservative, flow-insensitive analysis for values and functions which may
 * depend on {@code GetLocalPlayer()}.
 *
 * Optimizers use this analysis as a barrier. False positives only cost an
 * optimization; false negatives could move synchronized work into a
 * client-local control-flow region.
 */
public final class LocalPlayerContextAnalyzer {

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
            if (isGetLocalPlayer(call.getFunc())
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
            && (isGetLocalPlayer(function) || functionsUsingLocalPlayer.contains(function));
    }

    public boolean functionInliningIsLocalPlayerSensitive(ImFunction function) {
        return function != null
            && (isGetLocalPlayer(function)
            || functionsDirectlyUsingLocalPlayer.contains(function)
            || localPlayerDependentReturns.contains(function));
    }

    public boolean isLocalPlayerDependent(ImVar variable) {
        return variable != null && localPlayerDependentVars.contains(variable);
    }

    public boolean isLocalPlayerSource(ImFunction function) {
        return isGetLocalPlayer(function);
    }

    private void analyze(ImProg prog) {
        sourceFacts.add(unknownDispatchSource);
        for (ImFunction function : ImHelper.calculateFunctionsOfProg(prog)) {
            returnFact(function);
            useFact(function);
            if (isGetLocalPlayer(function)) {
                addLocalPlayerSource(function);
            } else if (!function.isNative()) {
                indexElement(function.getBody(), function);
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

    private void indexElement(Element element, ImFunction owner) {
        indexedElements.add(element);
        for (int i = 0; i < element.size(); i++) {
            Element child = element.get(i);
            indexElement(child, owner);
            addDependency(child, element);
        }

        if (element instanceof ImVarAccess) {
            addDependency(variableFact(((ImVarAccess) element).getVar()), element);
        } else if (element instanceof ImVarArrayAccess) {
            addDependency(variableFact(((ImVarArrayAccess) element).getVar()), element);
        } else if (element instanceof ImMemberAccess) {
            addDependency(variableFact(((ImMemberAccess) element).getVar()), element);
        }

        if (element instanceof ImSet) {
            ImSet set = (ImSet) element;
            forEachAssignedVariable(set.getLeft(),
                variable -> addDependency(set.getRight(), variableFact(variable)));
        } else if (element instanceof ImReturn) {
            ImReturn returnStmt = (ImReturn) element;
            if (returnStmt.getReturnValue() instanceof ImExpr) {
                addDependency(returnStmt.getReturnValue(), returnFact(owner));
            }
        } else if (element instanceof ImFunctionCall) {
            indexFunctionCall((ImFunctionCall) element, owner);
        } else if (element instanceof ImMethodCall) {
            indexMethodCall((ImMethodCall) element, owner);
        }
    }

    private void indexFunctionCall(ImFunctionCall call, ImFunction owner) {
        ImFunction called = call.getFunc();
        addDependency(returnFact(called), call);
        addDependency(useFact(called), useFact(owner));
        if (isGetLocalPlayer(called)) {
            functionsDirectlyUsingLocalPlayer.add(owner);
            addLocalPlayerSource(called);
        }

        int count = Math.min(call.getArguments().size(), called.getParameters().size());
        for (int i = 0; i < count; i++) {
            addDependency(call.getArguments().get(i),
                variableFact(called.getParameters().get(i)));
        }
    }

    private void indexMethodCall(ImMethodCall call, ImFunction owner) {
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
            for (ImVar parameter : implementation.getParameters()) {
                addDependency(call.getReceiver(), variableFact(parameter));
                for (ImExpr argument : call.getArguments()) {
                    addDependency(argument, variableFact(parameter));
                }
            }
        }
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

    private enum FactKind {
        VARIABLE,
        RETURN,
        USE,
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

    private static boolean isGetLocalPlayer(ImFunction function) {
        return function != null
            && function.isNative()
            && "GetLocalPlayer".equals(function.getName());
    }
}
