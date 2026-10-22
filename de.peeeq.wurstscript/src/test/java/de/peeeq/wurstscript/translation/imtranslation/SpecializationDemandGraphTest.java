package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.JassIm;
import org.testng.annotations.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.testng.Assert.*;

public class SpecializationDemandGraphTest {
    private static final class Key {
        boolean intrinsic;
        List<Key> dependencies = List.of();
        int version;
        void setIntrinsic(boolean value) { intrinsic = value; version++; }
        void setDependencies(Key... values) { dependencies = List.of(values); version++; }
        SpecializationDemandGraph.Facts<Key> facts() {
            int seen = version;
            return new SpecializationDemandGraph.Facts<>(intrinsic, dependencies, () -> version == seen);
        }
    }

    @Test
    public void cycleNeedsAnExternalSeedAndLosesDemandWhenThatEdgeGoes() {
        Key a = new Key(), b = new Key(), seed = new Key(), caller = new Key();
        a.setDependencies(b);
        b.setDependencies(a, seed);
        caller.setDependencies(a);
        var graph = new SpecializationDemandGraph<Key>(Key::facts);
        assertFalse(graph.query(caller));
        seed.setIntrinsic(true);

        assertTrue(graph.query(caller));
        assertTrue(graph.query(b));
        b.setDependencies(a);

        assertFalse(graph.query(a));
        assertFalse(graph.query(b));
        assertFalse(graph.query(caller));
        assertTrue(graph.query(seed));
        b.setDependencies(a, seed);

        assertTrue(graph.query(caller));
    }

    @Test
    public void diamondKeepsIndependentDemandAndRebuildsOnlyTheChangedNode() {
        Key root = new Key(), left = new Key(), right = new Key(), seed = new Key();
        seed.setIntrinsic(true);
        root.setDependencies(left, right);
        left.setDependencies(seed);
        right.setDependencies(seed);
        AtomicInteger builds = new AtomicInteger();
        var graph = new SpecializationDemandGraph<Key>(key -> { builds.incrementAndGet(); return key.facts(); });
        assertTrue(graph.query(root));
        assertEquals(builds.get(), 4);
        for (int i = 0; i < 100; i++) assertTrue(graph.query(root));
        assertEquals(builds.get(), 4);
        left.setDependencies();

        assertTrue(graph.query(root));
        assertEquals(builds.get(), 5);
        right.setDependencies();

        assertFalse(graph.query(root));
        assertEquals(builds.get(), 6);
    }

    @Test
    public void deepChainIsIterativeAndQueriesDoNotRebuildFacts() {
        List<Key> chain = new ArrayList<>();
        for (int i = 0; i < 20_000; i++) chain.add(new Key());
        chain.getLast().setIntrinsic(true);
        for (int i = 1; i < chain.size(); i++) chain.get(i - 1).setDependencies(chain.get(i));
        AtomicInteger builds = new AtomicInteger();
        var graph = new SpecializationDemandGraph<Key>(key -> { builds.incrementAndGet(); return key.facts(); });
        for (Key key : chain) assertTrue(graph.query(key));
        assertEquals(builds.get(), chain.size());
        chain.getLast().setIntrinsic(false);
        assertFalse(graph.query(chain.getFirst()));
        assertEquals(builds.get(), chain.size() + 1);
    }

    @Test
    public void aReferencedFunctionMutationIsDetectedWithoutChangingTheCaller() {
        ImFunction caller = function("caller"), callee = function("callee");
        var graph = new SpecializationDemandGraph<ImFunction>(function -> {
            int version = function.modificationCount();
            return new SpecializationDemandGraph.Facts<>(!function.getBody().isEmpty(),
                function == caller ? List.of(callee) : List.of(), () -> function.modificationCount() == version);
        });
        assertFalse(graph.query(caller));
        int callerVersion = caller.modificationCount();
        callee.getBody().add(JassIm.ImReturn(Ast.NoExpr(), JassIm.ImIntVal(1)));
        assertEquals(caller.modificationCount(), callerVersion);
        assertTrue(graph.query(caller));
        callee.getBody().removeAll();
        assertFalse(graph.query(caller));
    }

    private static ImFunction function(String name) {
        return JassIm.ImFunction(Ast.NoExpr(), name, JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImSimpleType("integer"), JassIm.ImVars(), JassIm.ImStmts(), List.of());
    }

    @Test
    public void generatedClassListsHaveStableIdentitySnapshots() {
        ImClass clazz = clazz("C"), base = clazz("Base");
        assertTrue(EliminateGenerics.classMembersUnchanged(clazz, List.of(), List.of(), List.of()));
        clazz.getSuperClasses().add(JassIm.ImClassType(base, JassIm.ImTypeArguments()));
        assertFalse(EliminateGenerics.classMembersUnchanged(clazz, List.of(), List.of(), List.of()));
        assertTrue(EliminateGenerics.classMembersUnchanged(clazz, List.of(), List.of(), List.of(base)));
        ImFunction implementation = function("implementation");
        var method = JassIm.ImMethod(Ast.NoExpr(), JassIm.ImClassType(clazz, JassIm.ImTypeArguments()),
            "method", implementation, new ArrayList<>(), new ArrayList<>(), "", false);
        clazz.getMethods().add(method);
        assertFalse(EliminateGenerics.classMembersUnchanged(clazz, List.of(), List.of(), List.of(base)));
        assertTrue(EliminateGenerics.classMembersUnchanged(clazz, List.of(method), List.of(implementation), List.of(base)));
        method.setImplementation(function("replacement"));
        assertFalse(EliminateGenerics.classMembersUnchanged(clazz, List.of(method), List.of(implementation), List.of(base)));
    }

    private static ImClass clazz(String name) {
        return JassIm.ImClass(Ast.NoExpr(), name, JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImMethods(), JassIm.ImFunctions(), new ArrayList<>());
    }

    @Test
    public void aNewDependencyIsValidatedEvenIfItWasOutsideTheOldClosure() {
        Key root = new Key(), dependency = new Key();
        dependency.setIntrinsic(true);
        var graph = new SpecializationDemandGraph<Key>(Key::facts);
        assertFalse(graph.query(root));
        assertTrue(graph.query(dependency));
        dependency.setIntrinsic(false);
        root.setDependencies(dependency);
        assertFalse(graph.query(root));
    }

    @Test
    public void changingCyclicGraphsAgreeWithIndependentSearches() {
        Random random = new Random(17);
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < 64; i++) keys.add(new Key());
        for (Key key : keys) {
            key.setIntrinsic(random.nextInt(8) == 0);
            key.setDependencies(keys.get(random.nextInt(keys.size())), keys.get(random.nextInt(keys.size())));
        }
        AtomicInteger builds = new AtomicInteger();
        var graph = new SpecializationDemandGraph<Key>(key -> { builds.incrementAndGet(); return key.facts(); });
        for (int step = 0; step <= 200; step++) {
            if (step > 0) {
                Key changed = keys.get(random.nextInt(keys.size()));
                if (random.nextBoolean()) changed.setIntrinsic(!changed.intrinsic);
                else changed.setDependencies(keys.get(random.nextInt(keys.size())), keys.get(random.nextInt(keys.size())));

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
