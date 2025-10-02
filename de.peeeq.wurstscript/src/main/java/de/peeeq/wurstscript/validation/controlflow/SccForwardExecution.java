package de.peeeq.wurstscript.validation.controlflow;

import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.wurstscript.ast.AstElementWithBody;
import de.peeeq.wurstscript.ast.WStatement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;

/**
 * An optimized forward execution driver for dataflow analysis that uses
 * Strongly Connected Components (SCCs) to structure the analysis.
 *
 * The algorithm works as follows:
 * 1.  The control flow graph (CFG) of the function is built.
 * 2.  The CFG is decomposed into its Strongly Connected Components.
 * 3.  The graph of the SCCs (a Directed Acyclic Graph) is topologically sorted.
 * 4.  The analysis is performed on each SCC in topological order.
 * 5.  For each SCC, a local worklist algorithm iterates until a fixed-point is reached for all statements *within that component*.
 *
 * This approach is highly efficient because it "contains" the iterative analysis within loops (which form SCCs).
 * State changes only propagate within the current SCC until it stabilizes, before moving to the next component.
 * This dramatically reduces the number of redundant merge operations that plague a simple worklist algorithm on large functions.
 */
public class SccForwardExecution<T, Target extends AstElementWithBody> {

    private final ForwardMethod<T, Target> method;
    private final Target f;
    private final Map<WStatement, T> finalValues;

    public SccForwardExecution(Target f, ForwardMethod<T, Target> method) {
        this.f = f;
        this.method = method;
        this.method.setFuncDef(f);
        // Use fastutil's Object2ObjectOpenHashMap for potentially better performance
        this.finalValues = new Object2ObjectOpenHashMap<>();
    }

    public void execute() {
        if (f.getBody().isEmpty()) {
            return;
        }

        // 1. Collect all statements (nodes) in the control flow graph
        List<WStatement> allNodes = getAllStatements();
        if (allNodes.isEmpty()) {
            return;
        }

        // 2. Decompose the CFG into Strongly Connected Components
        GraphInterpreter<WStatement> graphInterpreter = new GraphInterpreter<>() {
            @Override
            protected Collection<WStatement> getIncidentNodes(WStatement t) {
                return t.attrNextStatements();
            }
        };
        List<List<WStatement>> sccs = graphInterpreter.findStronglyConnectedComponents(allNodes);

        // 3. The SCC algorithm outputs components in reverse topological order, so we need to reverse them
        Collections.reverse(sccs);

        // 4. Analyze each SCC in topological order
        for (List<WStatement> scc : sccs) {
            analyzeComponent(scc);
        }


        // 5. Run the final check on the state of the last statement(s)
        List<WStatement> finalStmts = findFinalStatements(allNodes);
        T finalState = method.merge(get(finalStmts));
        method.checkFinal(finalState);
    }

    private void analyzeComponent(List<WStatement> scc) {
        Queue<WStatement> worklist = new ArrayDeque<>(scc);

        int iterations = 0;
        int maxIterations = scc.size() * scc.size() + 100; // Heuristic limit to prevent infinite loops

        while (!worklist.isEmpty()) {
            if (iterations++ > maxIterations) {
                // This should ideally not happen in a correct CFG with a monotonic transfer function
                throw new RuntimeException("Dataflow analysis did not converge. Possible infinite loop in component.");
            }

            WStatement s = worklist.poll();

            // Merge states from all predecessors
            Collection<T> predecessorStates = get(s.attrPreviousStatements());
            T incoming = method.merge(predecessorStates);

            T oldVal = finalValues.get(s);
            T newVal = method.calculate(s, incoming);

            if (oldVal == null || !method.equality(oldVal, newVal)) {
                finalValues.put(s, newVal);

                // If the value changed, add successors within the same SCC to the worklist
                for (WStatement succ : s.attrNextStatements()) {
                    if (scc.contains(succ)) {
                        worklist.add(succ);
                    }
                }
            }
        }
    }

    private List<WStatement> getAllStatements() {
        List<WStatement> allNodes = new ArrayList<>();
        Queue<WStatement> todo = new ArrayDeque<>();
        Set<WStatement> visited = new ObjectOpenHashSet<>();

        if (!f.getBody().isEmpty()) {
            todo.add(f.getBody().get(0));
            visited.add(f.getBody().get(0));
        }

        while (!todo.isEmpty()) {
            WStatement s = todo.poll();
            allNodes.add(s);
            for (WStatement next : s.attrNextStatements()) {
                if (visited.add(next)) {
                    todo.add(next);
                }
            }
        }
        return allNodes;
    }

    private List<WStatement> findFinalStatements(List<WStatement> allNodes) {
        List<WStatement> result = new ArrayList<>();
        for (WStatement s : allNodes) {
            if (s.attrNextStatements().isEmpty()) {
                result.add(s);
            }
        }
        return result;
    }

    private Collection<T> get(List<WStatement> statements) {
        if (statements.isEmpty()) {
            // If there are no predecessors (e.g., the first statement), start with the initial value.
            return Collections.singleton(method.startValue());
        }
        Collection<T> result = new ArrayList<>(statements.size());
        for (WStatement s : statements) {
            result.add(get(s));
        }
        return result;
    }

    private T get(WStatement s) {
        T t = finalValues.get(s);
        if (t == null) {
            // If a statement hasn't been processed yet, use the initial value
            return method.startValue();
        }
        return t;
    }
}
