package de.peeeq.datastructures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

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
    public List<List<T>> findStronglyConnectedComponents(List<T> nodes) {
        Deque<T> s = new ArrayDeque<>(); // S stack
        Deque<T> p = new ArrayDeque<>(); // P stack

        int preorderCounter = 0;
        int componentCounter = 0;

        Object2IntLinkedOpenHashMap<T> preorder = new Object2IntLinkedOpenHashMap<>();
        preorder.defaultReturnValue(-1);
        Object2IntLinkedOpenHashMap<T> compId = new Object2IntLinkedOpenHashMap<>();
        compId.defaultReturnValue(-1);

        Deque<T> frameStack = new ArrayDeque<>();         // simulated recursion
        Map<T, Iterator<T>> childIters = new HashMap<>(); // per-node child iterators

        List<List<T>> sccs = new ArrayList<>();

        for (T start : nodes) {
            if (preorder.getInt(start) != -1) continue;

            frameStack.push(start);
            while (!frameStack.isEmpty()) {
                T v = frameStack.peek();

                // First time at v
                if (preorder.getInt(v) == -1) {
                    preorder.put(v, preorderCounter++);
                    s.push(v);
                    p.push(v);
                    childIters.put(v, getIncidentNodes(v).iterator());
                }

                boolean descended = false;
                Iterator<T> it = childIters.get(v);

                while (it.hasNext()) {
                    T w = it.next();
                    int preW = preorder.getInt(w);
                    if (preW == -1) {
                        frameStack.push(w);
                        descended = true;
                        break;
                    } else if (compId.getInt(w) == -1) {
                        // w discovered but not assigned; shrink P
                        while (!p.isEmpty() && preorder.getInt(p.peek()) > preW) {
                            p.pop();
                        }
                    }
                }

                if (descended) continue;

                // Post-order for v
                frameStack.pop();
                childIters.remove(v);

                if (!p.isEmpty() && p.peek() == v) {
                    int newCid = componentCounter++;
                    ArrayList<T> cur = new ArrayList<>();
                    while (true) {
                        T x = s.pop();
                        compId.put(x, newCid);
                        cur.add(x);
                        if (x == v) break;
                    }
                    p.pop();

                    // Gabow emits SCCs in reverse-topological order
                    sccs.add(cur);
                }
            }
        }

        return sccs;
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
