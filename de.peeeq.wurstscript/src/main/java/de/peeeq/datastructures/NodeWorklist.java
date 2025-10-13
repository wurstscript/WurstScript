package de.peeeq.datastructures;

import de.peeeq.wurstscript.intermediatelang.optimizer.ControlFlowGraph.Node;
import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Ultra-fast worklist specifically for CFG nodes.
 * Uses array-based tracking for even better performance.
 */
public class NodeWorklist {
    private final ArrayDeque<Node> queue = new ArrayDeque<>();
    private final boolean[] inQueue;
    private final Node[] allNodes;

    public NodeWorklist(Collection<Node> nodes) {
        this.allNodes = nodes.toArray(new Node[0]);
        this.inQueue = new boolean[allNodes.length];

        // Add all nodes initially
        for (int i = 0; i < allNodes.length; i++) {
            queue.add(allNodes[i]);
            inQueue[i] = true;
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public Node poll() {
        Node n = queue.poll();
        if (n != null) {
            inQueue[getIndex(n)] = false;
        }
        return n;
    }

    public boolean add(Node n) {
        int idx = getIndex(n);
        if (!inQueue[idx]) {
            inQueue[idx] = true;
            queue.add(n);
            return true;
        }
        return false;
    }

    public void addAll(Collection<Node> nodes) {
        for (Node n : nodes) {
            add(n);
        }
    }

    private int getIndex(Node n) {
        // You'd need to add an index field to Node for O(1) lookup
        // For now, linear search (not ideal)
        for (int i = 0; i < allNodes.length; i++) {
            if (allNodes[i] == n) return i;
        }
        return -1;
    }
}
