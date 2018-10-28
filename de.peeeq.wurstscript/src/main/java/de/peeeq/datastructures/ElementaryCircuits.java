package de.peeeq.datastructures;


import com.google.common.collect.ImmutableList;

import java.util.*;

/**
 * Find elementary cicuits in a graph using
 * Following the paper "Finding all the elementary circuits of a directed graph"
 * by Donald B. Johnson.
 */
public class ElementaryCircuits<Node> {

    private final Graph<Node> graph;

    private Map<Node, List<Node>> a = new LinkedHashMap<>();
    private Map<Node, List<Node>> b = new LinkedHashMap<>();
    private Set<Node> blocked = new LinkedHashSet<>();
    private Node s;
    private Deque<Node> stack = new ArrayDeque<>();
    private List<List<Node>> circuits = new ArrayList<>();

    public void findElementaryCircuit() {

    }

    private ElementaryCircuits(Graph<Node> graph) {
        this.graph = graph;
    }

    private boolean circuit(Node v) {
        boolean f = false;
        stack.push(v);
        blocked.add(v);
        // L1:
        for (Node w : a.get(v)) {
            if (w == s) {
                // output circuit composed of stack followed by s
                List<Node> circuit = ImmutableList.<Node>builder().addAll(stack).add(s).build();
                circuits.add(circuit);
                f = true;
            } else if (!blocked.contains(w)) {
                if (circuit(w)) {
                    f = true;
                }
            }
        }
        // L2:
        if (f) {
            unblock(v);
        } else {
            for (Node w : a.get(v)) {
                if (!b.get(w).contains(v)) {
                    b.computeIfAbsent(w, x -> new ArrayList<>()).add(w);
                }
            }
        }
        Node popped = stack.pop();
        assert popped == v;
        for (Node node : graph.getNodes()) {
            s = node;

            // adjacency structure of strong component K with least vertex in subgraph of G induced by s, s+1, ... n
            a = a;

            if (!a.isEmpty()) {
                s = null;
            }

        }


        return false;
    }

    private void f() {

    }

    private void unblock(Node u) {
        blocked.remove(u);
        for (Node w : b.get(u)) {
            if (blocked.contains(w)) {
                unblock(w);
            }
        }
        b.put(u, new ArrayList<>());
    }


}
