package de.peeeq.datastructures;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GraphInterpreter<T> {

    protected abstract Collection<T> getIncidentNodes(T t);

    public TopsortResult<T> topSort(List<T> nodes) {
        Set<T> seen = Sets.newHashSet();
        List<T> seenStack = Lists.newArrayList();
        List<T> result = Lists.newArrayList();

        for (T t : nodes) {
            if (!seen.contains(t)) {
                @Nullable TopsortResult<T> r = topSort_add(result, seen, seenStack, t);
                if (r != null) {
                    return r;
                }
            }
        }
        return new TopsortResult<>(false, result);
    }


    private @Nullable TopsortResult<T> topSort_add(List<T> result, Set<T> seen, List<T> seenStack, T n) {
        for (int i = seenStack.size() - 1; i >= 0; i--) {
            if (seenStack.get(i) == n) {
                // cycle
                return new TopsortResult<>(true, Utils.subList(seenStack, i));
            }
        }
        if (!seen.contains(n)) {
            seenStack.add(n);
            for (T m : getIncidentNodes(n)) {
                TopsortResult<T> r = topSort_add(result, seen, seenStack, m);
                if (r != null) {
                    return r;
                }
            }
            seenStack.remove(seenStack.size() - 1);
            seen.add(n);
            result.add(n);
        }
        return null;
    }


    public static class TopsortResult<T> {
        private final boolean isCycle;
        private final List<T> result;

        public TopsortResult(boolean isCycle, List<T> result) {
            this.isCycle = isCycle;
            this.result = result;
        }

        public boolean isCycle() {
            return isCycle;
        }

        public List<T> getResult() {
            return result;
        }


    }


    /**
     * Like topsort, but will find bigger cycles
     * <p>
     * See https://en.wikipedia.org/wiki/Path-based_strong_component_algorithm
     */
    public Set<Set<T>> findStronglyConnectedComponents(List<T> nodes) {
        // Stack S contains all the vertices that have not yet been assigned to a strongly connected component, in the order in which the depth-first search reaches the vertices.
        Deque<T> s = new ArrayDeque<>();
        // Stack P contains vertices that have not yet been determined to belong to different strongly connected components from each other
        Deque<T> p = new ArrayDeque<>();
        // It also uses a counter C of the number of vertices reached so far, which it uses to compute the preorder numbers of the vertices.
        AtomicInteger c = new AtomicInteger();
        AtomicInteger componentCount = new AtomicInteger();
        Map<T, Integer> preorderNumber = new LinkedHashMap<>();
        Map<T, Integer> component = new LinkedHashMap<>();

        for (T v : nodes) {
            if (!preorderNumber.containsKey(v)) {
                findStronglyConnectedComponentsRec(v, s, p, c, preorderNumber, component, componentCount);
            }
        }
        return ImmutableSet.copyOf(Utils.inverseMapToSet(component).values());
    }


    private void findStronglyConnectedComponentsRec(T v, Deque<T> s, Deque<T> p, AtomicInteger c, Map<T, Integer> preorderNumber, Map<T, Integer> component, AtomicInteger componentCount) {


        // When the depth-first search reaches a vertex v, the algorithm performs the following steps:
        // 1. Set the preorder number of v to C, and increment C.
        preorderNumber.put(v, c.getAndIncrement());

        // 2. Push v onto S and also onto P.
        s.push(v);
        p.push(v);

        // 3. For each edge from v to a neighboring vertex w:
        for (T w : getIncidentNodes(v)) {
            if (!preorderNumber.containsKey(w)) {
                // If the preorder number of w has not yet been assigned, recursively search w;
                findStronglyConnectedComponentsRec(w, s, p, c, preorderNumber, component, componentCount);
            } else {
                // Otherwise, if w has not yet been assigned to a strongly connected component:
                if (!component.containsKey(w)) {
                    // Repeatedly pop vertices from P until the top element of P has a preorder number less than or equal to the preorder number of w.
                    while (!p.isEmpty()
                            && preorderNumber.getOrDefault(p.peek(), -1) > preorderNumber.get(w)) {
                        p.pop();
                    }
                }
            }
        }
        // 4. If v is the top element of P:
        if (!p.isEmpty() && p.peek() == v) {
            // Pop vertices from S until v has been popped, and assign the popped vertices to a new component.
            Integer newComponent = componentCount.incrementAndGet();
            while (true) {
                T popped = s.pop();
                component.put(popped, newComponent);
                if (popped == v) {
                    break;
                }
            }
            // Pop v from P.
            T popped = p.pop();
            assert popped == v;
        }


    }

    public String generateDotFile(List<T> nodes) {
        StringBuilder sb = new StringBuilder();
        Set<T> visited = new HashSet<>();
        Deque<T> todo = new ArrayDeque<>(nodes);
        sb.append("digraph G{\n");
        while (!todo.isEmpty()) {
            T node = todo.removeFirst();
            if (!visited.add(node)) {
                continue;
            }
            sb.append("  \"").append(node.toString()).append("\";\n");
            for (T n : getIncidentNodes(node)) {
                sb.append("  ");
                sb.append("\"").append(node.toString()).append("\" -> ");
                sb.append("\"").append(n.toString()).append("\";\n\n");
                todo.addFirst(n);
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

}
