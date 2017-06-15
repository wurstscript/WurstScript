package tests.utils;

import com.google.common.collect.Lists;
import de.peeeq.datastructures.GraphInterpreter;
import de.peeeq.datastructures.GraphInterpreter.TopsortResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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
            for (Node n : next) {
                nextNodes.add(n);
            }
        }

        @Override
        public String toString() {
            return val;
        }
    }

}
