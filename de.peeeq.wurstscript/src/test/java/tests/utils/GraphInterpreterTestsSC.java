package tests.utils;

import com.google.common.collect.Lists;
import de.peeeq.datastructures.GraphInterpreter;
import org.junit.Test;
import org.junit.runner.RunWith;
import smallcheck.SmallCheckRunner;
import smallcheck.annotations.From;
import smallcheck.annotations.Property;
import smallcheck.generators.SeriesGen;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;

@RunWith(SmallCheckRunner.class)
public class GraphInterpreterTestsSC {

    int count = 0;

    @Property(maxInvocations = 50000)
    public void test(@From(GraphGen.class) Graph g) {
        System.out.println("iteration " + ++count);
        System.out.println(g);

        Set<Set<Node>> components = g.findStronglyConnectedComponents(g.nodes);

        boolean componentCycle = (components.stream().anyMatch(c -> c.size() > 1));

        assertEquals(componentCycle, isCyclic(g));

    }

    @Test
    public void simpleGraph() {
        boolean[][] adj = {{true,true},{true, false}};
        Graph g = new Graph(adj);
        System.out.println(g.toString());
        Set<Set<Node>> components = g.findStronglyConnectedComponents(g.nodes);
        boolean componentCycle = (components.stream().anyMatch(c -> c.size() > 1));
        assertEquals(componentCycle, isCyclic(g));
    }

    private boolean isCyclicUtil(Graph g, Node v, boolean visited[], boolean[] recStack) {
        if (!visited[v.val]) {
            // Mark the current node as visited and part of recursion stack
            visited[v.val] = true;
            recStack[v.val] = true;

            // Recur for all the vertices adjacent to this vertex
            for (Node i : g.nodes) {
                if (i.val != v.val && g.adj[v.val][i.val]) {
                    if (!visited[i.val] && isCyclicUtil(g, i, visited, recStack))
                        return true;
                    else if (recStack[i.val])
                        return true;
                }
            }
        }
        recStack[v.val] = false;  // remove the vertex from recursion stack
        return false;
    }

    private boolean isCyclic(Graph g) {
        // Mark all the vertices as not visited and not part of recursion
        // stack
        boolean[] visited = new boolean[g.nodes.size()];
        boolean[] recStack = new boolean[g.nodes.size()];
        // Call the recursive helper function to detect cycle in different
        // DFS trees
        for (Node i : g.nodes) {
            if (isCyclicUtil(g, i, visited, recStack))
                return true;

        }
        return false;
    }

    public static class GraphGen extends SeriesGen<Graph> {

        @Override
        public Stream<Graph> generate(int depth) {
            return IntStream.range(1, Integer.MAX_VALUE)
                    .boxed()
                    .flatMap((Integer size) -> {
                        boolean[][] adj = new boolean[size][size];
                        Graph g = new Graph(adj);
                        return Stream.generate(() -> {
                            increment(adj);
                            return g;
                        }).limit((long) Math.pow(2, size * size) - 1);
                    });
        }

        private void increment(boolean[][] adj) {
            for (int i = 0; i < adj.length; i++) {
                if (incrementRow(adj[i])) {
                    break;
                }
            }
        }

        private boolean incrementRow(boolean[] a) {
            for (int i = 0; i < a.length; i++) {
                if (a[i]) {
                    a[i] = false;
                } else {
                    a[i] = true;
                    return true;
                }

            }
            return false;
        }

    }

    public static class Graph extends GraphInterpreter<Node> {
        private final List<Node> nodes;
        private final boolean[][] adj;

        public Graph(boolean[][] adj) {
            nodes = new ArrayList<>(adj.length);
            for (int i = 0; i < adj.length; i++) {
                nodes.add(new Node(i));
            }
            this.adj = adj;
        }

        @Override
        protected Collection<Node> getIncidentNodes(Node node) {
            Collection<Node> res = new ArrayList<>();
            for (int i = 0; i < adj[node.val].length; i++) {
                if (adj[node.val][i]) {
                    res.add(nodes.get(i));
                }
            }
            return res;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("\n");
            for (boolean[] bs : adj) {
                sb.append("|");
                for (boolean b : bs) {
                    sb.append(b ? 'X' : '.');
                }
                sb.append("|\n");
            }
            return sb.toString();
        }
    }


    public static class Node {
        final int val;
        final List<Node> nextNodes;

        public Node(int val) {
            this.val = val;
            this.nextNodes = Lists.newArrayList();
        }

        void add(Node... next) {
            Collections.addAll(nextNodes, next);
        }

        @Override
        public String toString() {
            return "N" + val;
        }
    }


    public static class GraphFactory {


    }

}
