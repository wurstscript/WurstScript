package de.peeeq.datastructures;

import java.util.List;
import java.util.function.Function;

/**
 *
 */
public class Graph<Node> {
    private final List<Node> nodes;
    private final Function<Node, List<Node>> edges;

    public Graph(List<Node> nodes, Function<Node, List<Node>> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Node> adjacentNodes(Node n) {
        return edges.apply(n);
    }

}
