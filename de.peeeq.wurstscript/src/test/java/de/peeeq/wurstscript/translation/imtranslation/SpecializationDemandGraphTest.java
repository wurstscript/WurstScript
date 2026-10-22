package de.peeeq.wurstscript.translation.imtranslation;

import org.testng.annotations.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.testng.Assert.*;

public class SpecializationDemandGraphTest {
    private static final class Key {
        boolean intrinsic;
        List<Key> dependencies = List.of();
        SpecializationDemandGraph.Facts<Key> facts() {
            return new SpecializationDemandGraph.Facts<>(intrinsic, dependencies);
        }
    }

    @Test
    public void cycleNeedsAnExternalSeedAndLosesDemandWhenThatEdgeGoes() {
        Key a = new Key(), b = new Key(), seed = new Key(), caller = new Key();
        a.dependencies = List.of(b);
        b.dependencies = List.of(a, seed);
        caller.dependencies = List.of(a);
        var graph = new SpecializationDemandGraph<Key>(Key::facts);
        assertFalse(graph.query(caller));
        seed.intrinsic = true;
        graph.changed(seed);
        assertTrue(graph.query(caller));
        assertTrue(graph.query(b));
        b.dependencies = List.of(a);
        graph.changed(b);
        assertFalse(graph.query(a));
        assertFalse(graph.query(b));
        assertFalse(graph.query(caller));
        assertTrue(graph.query(seed));
        b.dependencies = List.of(a, seed);
        graph.changed(b);
        assertTrue(graph.query(caller));
    }

    @Test
    public void diamondKeepsIndependentDemandAndRebuildsOnlyTheChangedNode() {
        Key root = new Key(), left = new Key(), right = new Key(), seed = new Key();
        seed.intrinsic = true;
        root.dependencies = List.of(left, right);
        left.dependencies = right.dependencies = List.of(seed);
        AtomicInteger builds = new AtomicInteger();
        var graph = new SpecializationDemandGraph<Key>(key -> { builds.incrementAndGet(); return key.facts(); });
        assertTrue(graph.query(root));
        assertEquals(builds.get(), 4);
        for (int i = 0; i < 100; i++) assertTrue(graph.query(root));
        assertEquals(builds.get(), 4);
        left.dependencies = List.of();
        graph.changed(left);
        assertTrue(graph.query(root));
        assertEquals(builds.get(), 5);
        right.dependencies = List.of();
        graph.changed(right);
        assertFalse(graph.query(root));
        assertEquals(builds.get(), 6);
    }

    @Test
    public void deepChainIsIterativeAndQueriesDoNotRescanIt() {
        List<Key> chain = new ArrayList<>();
        for (int i = 0; i < 20_000; i++) chain.add(new Key());
        chain.getLast().intrinsic = true;
        for (int i = 1; i < chain.size(); i++) chain.get(i - 1).dependencies = List.of(chain.get(i));
        AtomicInteger builds = new AtomicInteger();
        var graph = new SpecializationDemandGraph<Key>(key -> { builds.incrementAndGet(); return key.facts(); });
        for (Key key : chain) assertTrue(graph.query(key));
        assertEquals(builds.get(), chain.size());
        chain.getLast().intrinsic = false;
        graph.changed(chain.getLast());
        assertFalse(graph.query(chain.getFirst()));
        assertEquals(builds.get(), chain.size() + 1);
    }

    @Test
    public void changingCyclicGraphsAgreeWithIndependentSearches() {
        Random random = new Random(17);
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < 64; i++) keys.add(new Key());
        for (Key key : keys) {
            key.intrinsic = random.nextInt(8) == 0;
            key.dependencies = List.of(keys.get(random.nextInt(keys.size())), keys.get(random.nextInt(keys.size())));
        }
        AtomicInteger builds = new AtomicInteger();
        var graph = new SpecializationDemandGraph<Key>(key -> { builds.incrementAndGet(); return key.facts(); });
        for (int step = 0; step <= 200; step++) {
            if (step > 0) {
                Key changed = keys.get(random.nextInt(keys.size()));
                if (random.nextBoolean()) changed.intrinsic = !changed.intrinsic;
                else changed.dependencies = List.of(keys.get(random.nextInt(keys.size())), keys.get(random.nextInt(keys.size())));
                graph.changed(changed);
            }
            for (Key root : keys) {
                Set<Key> visited = Collections.newSetFromMap(new IdentityHashMap<>());
                ArrayDeque<Key> work = new ArrayDeque<>();
                work.add(root);
                boolean expected = false;
                while (!work.isEmpty()) {
                    Key current = work.removeFirst();
                    if (!visited.add(current)) continue;
                    if (current.intrinsic) { expected = true; break; }
                    work.addAll(current.dependencies);
                }
                assertEquals(graph.query(root), expected, "after dependency update " + step);
            }
        }
        assertEquals(builds.get(), keys.size() + 200);
    }
}
