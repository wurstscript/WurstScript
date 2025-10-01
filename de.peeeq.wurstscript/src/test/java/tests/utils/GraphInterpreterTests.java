package tests.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.datastructures.GraphInterpreter.TopsortResult;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public class GraphInterpreterTests {

    @Test
    public void testSimpleGraph() {
        GraphInterpreter<Node> gi = new GraphInterpreter<Node>() {
            @Override
            protected List<Node> getIncidentNodes(Node n) {
                return n.nextNodes;
            }
        };

        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        Node d = new Node("D");
        List<Node> nodes = Lists.newArrayList(b, d, c, a);

        a.add(b, c);
        b.add(c, d);


        TopsortResult<Node> sorted = gi.topSort(nodes);

        assertFalse(sorted.isCycle());

        assertEquals(Lists.newArrayList(c, d, b, a), sorted.getResult());

    }


    @Test
    public void testCycle() {
        GraphInterpreter<Node> gi = new GraphInterpreter<Node>() {
            @Override
            protected List<Node> getIncidentNodes(Node n) {
                return n.nextNodes;
            }
        };

        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        Node d = new Node("D");
        List<Node> nodes = Lists.newArrayList(b, d, c, a);

        a.add(b, c);
        b.add(c, d);
        d.add(a);

        TopsortResult<Node> sorted = gi.topSort(nodes);

        assertTrue(sorted.isCycle());

        assertEquals(Lists.newArrayList(b, d, a), sorted.getResult());

    }


    @Test
    public void testStronglyConnectedComponents() {
        // example from https://en.wikipedia.org/wiki/Strongly_connected_component
        // https://en.wikipedia.org/wiki/File:Scc.png

        GraphInterpreter<Node> gi = new GraphInterpreter<Node>() {
            @Override
            protected List<Node> getIncidentNodes(Node n) {
                return n.nextNodes;
            }
        };

        Node a = new Node("a");
        Node b = new Node("b");
        Node c = new Node("c");
        Node d = new Node("d");
        Node e = new Node("e");
        Node f = new Node("f");
        Node g = new Node("g");
        Node h = new Node("h");

        List<Node> nodes = Lists.newArrayList(a, b, c, d, e, f, g, h);

        a.add(b);
        b.add(e, f);
        c.add(d, g);
        d.add(c, h);
        e.add(a, f);
        f.add(g);
        g.add(f);
        h.add(g, d);


        List<List<Node>> components = gi.findStronglyConnectedComponents(nodes);

        List<List<Node>> expected = ImmutableList.of(
            ImmutableList.of(g, f),
            ImmutableList.of(e, b, a),
            ImmutableList.of(h, d, c)
        );

        assertEquals(components, expected);
    }


    class Node {
        final String val;
        final List<Node> nextNodes;

        public Node(String val) {
            this.val = val;
            this.nextNodes = Lists.newArrayList();
        }

        public Node(String val, List<Node> nextNodes) {
            this.val = val;
            this.nextNodes = nextNodes;
        }

        void add(Node... next) {
            Collections.addAll(nextNodes, next);
        }

        @Override
        public String toString() {
            return val;
        }
    }

}
