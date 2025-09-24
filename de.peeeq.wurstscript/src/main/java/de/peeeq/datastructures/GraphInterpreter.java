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
     * Like topsort, but will find bigger cycles.
     * This is an iterative implementation of the path-based strong component algorithm
     * to prevent StackOverflowErrors on deep graphs.
     * <p>
     * See https://en.wikipedia.org/wiki/Path-based_strong_component_algorithm
     */
    public Set<Set<T>> findStronglyConnectedComponents(List<T> nodes) {
        Deque<T> s = new ArrayDeque<>();
        Deque<T> p = new ArrayDeque<>();
        AtomicInteger c = new AtomicInteger();
        AtomicInteger componentCount = new AtomicInteger();
        Map<T, Integer> preorderNumber = new HashMap<>();
        Map<T, Integer> component = new HashMap<>();

        // This stack simulates the recursive calls
        Deque<T> traversalStack = new ArrayDeque<>();
        // This map holds iterators for the children of each node
        Map<T, Iterator<T>> iterators = new HashMap<>();

        for (T startNode : nodes) {
            if (!preorderNumber.containsKey(startNode)) {
                traversalStack.push(startNode);

                while (!traversalStack.isEmpty()) {
                    T v = traversalStack.peek();

                    // Pre-order processing (first time visiting node v)
                    if (!preorderNumber.containsKey(v)) {
                        preorderNumber.put(v, c.getAndIncrement());
                        s.push(v);
                        p.push(v);
                        iterators.put(v, getIncidentNodes(v).iterator());
                    }

                    boolean foundNewChild = false;
                    Iterator<T> children = iterators.get(v);

                    // Iterate over children to find the next one to visit
                    while (children.hasNext()) {
                        T w = children.next();
                        if (!preorderNumber.containsKey(w)) {
                            // Found an unvisited child, push to stack to simulate recursive call
                            traversalStack.push(w);
                            foundNewChild = true;
                            break;
                        } else if (!component.containsKey(w)) {
                            // Child w has been visited but is not yet in a component
                            while (!p.isEmpty() && preorderNumber.getOrDefault(p.peek(), -1) > preorderNumber.get(w)) {
                                p.pop();
                            }
                        }
                    }

                    if (foundNewChild) {
                        // Continue the loop to process the new child on top of the stack
                        continue;
                    }

                    // Post-order processing (all children of v have been visited)
                    traversalStack.pop(); // Finished with v, pop it
                    iterators.remove(v); // Clean up iterator

                    if (!p.isEmpty() && p.peek() == v) {
                        Integer newComponent = componentCount.incrementAndGet();
                        while (true) {
                            T popped = s.pop();
                            component.put(popped, newComponent);
                            if (popped == v) {
                                break;
                            }
                        }
                        p.pop();
                    }
                }
            }
        }
        return ImmutableSet.copyOf(Utils.inverseMapToSet(component).values());
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
            sb.append("  \"").append(node).append("\";\n");
            for (T n : getIncidentNodes(node)) {
                sb.append("  ");
                sb.append("\"").append(node).append("\" -> ");
                sb.append("\"").append(n.toString()).append("\";\n\n");
                todo.addFirst(n);
            }
        }
        sb.append("}\n");
        return sb.toString();
    }
}
