package de.peeeq.wurstscript.translation.imtranslation;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/** Pass-local existential dependency facts. Cycles require a real intrinsic demand, not their own cached truth. */
final class SpecializationDemandGraph<T> {
    record Facts<T>(boolean intrinsic, List<T> dependencies, BooleanSupplier valid) {
    }

    private final Function<T, Facts<T>> facts;
    private final Map<T, Node> nodes = new IdentityHashMap<>();
    private int validationRound;

    private final class Node {
        final T key;
        final List<Node> dependencies = new ArrayList<>();
        final List<Node> users = new ArrayList<>();
        boolean built;
        boolean intrinsic;
        boolean demanded;
        BooleanSupplier valid;
        int validatedAt;

        Node(T key) { this.key = key; }
    }

    SpecializationDemandGraph(Function<T, Facts<T>> facts) {
        this.facts = facts;
    }

    boolean query(T key) {
        Node root = node(key);
        if (!root.built) materialize(root);
        while (true) {
            List<Node> changed = null;
            int round = ++validationRound;
            ArrayDeque<Node> work = new ArrayDeque<>();
            work.add(root);
            while (!work.isEmpty()) {
                Node current = work.removeFirst();
                if (current.validatedAt == round) continue;
                current.validatedAt = round;
                if (!current.valid.getAsBoolean()) {
                    if (changed == null) changed = new ArrayList<>();
                    changed.add(current);
                }
                work.addAll(current.dependencies);
            }
            if (changed == null) return root.demanded;
            for (Node node : changed) rebuild(node);
            // Rebuilding can introduce dependencies that were not part of the previous validation.
        }
    }

    /** Rebuild this node's outgoing edges; its unchanged dependencies keep their already computed facts. */
    private void rebuild(Node root) {
        List<Node> previous = new ArrayList<>(root.dependencies);
        boolean intrinsicBefore = root.intrinsic;
        for (Node dependency : previous) dependency.users.remove(root);
        root.dependencies.clear();
        root.built = false;
        materialize(root);
        if (intrinsicBefore == root.intrinsic && previous.equals(root.dependencies)) return;

        // A removed edge can leave a cycle incorrectly supporting its old true answer. Clear the complete
        // reverse region, then derive its least fixed point from intrinsic and unaffected outside demands.
        Set<Node> affected = new LinkedHashSet<>();
        ArrayDeque<Node> work = new ArrayDeque<>();
        work.add(root);
        while (!work.isEmpty()) {
            Node current = work.removeFirst();
            if (affected.add(current)) work.addAll(current.users);
        }
        for (Node current : affected) current.demanded = false;
        ArrayDeque<Node> demanded = new ArrayDeque<>();
        for (Node current : affected) seed(current, demanded);
        propagate(demanded);
    }

    private Node node(T key) {
        Node result = nodes.get(key);
        if (result == null) {
            result = new Node(key);
            nodes.put(key, result);
        }
        return result;
    }

    private void materialize(Node root) {
        List<Node> added = new ArrayList<>();
        ArrayDeque<Node> work = new ArrayDeque<>();
        work.add(root);
        while (!work.isEmpty()) {
            Node current = work.removeFirst();
            if (current.built) continue;
            Facts<T> currentFacts = facts.apply(current.key);
            current.valid = currentFacts.valid();
            current.built = true;
            current.intrinsic = currentFacts.intrinsic();
            added.add(current);
            if (current.intrinsic) continue;
            Set<Node> seen = currentFacts.dependencies().size() > 1
                ? Collections.newSetFromMap(new IdentityHashMap<>()) : null;
            for (T dependencyKey : currentFacts.dependencies()) {
                Node dependency = node(dependencyKey);
                if (seen != null && !seen.add(dependency)) continue;
                current.dependencies.add(dependency);
                dependency.users.add(current);
                if (!dependency.built) work.add(dependency);
            }
        }
        ArrayDeque<Node> demanded = new ArrayDeque<>();
        for (Node current : added) seed(current, demanded);
        propagate(demanded);
    }

    private void seed(Node node, ArrayDeque<Node> work) {
        if (node.intrinsic) {
            mark(node, work);
            return;
        }
        for (Node dependency : node.dependencies) {
            if (dependency.demanded) {
                mark(node, work);
                return;
            }
        }
    }

    private void mark(Node node, ArrayDeque<Node> work) {
        if (!node.demanded) {
            node.demanded = true;
            work.add(node);
        }
    }

    private void propagate(ArrayDeque<Node> work) {
        while (!work.isEmpty()) {
            for (Node user : work.removeFirst().users) mark(user, work);
        }
    }
}
