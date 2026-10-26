package de.peeeq.wurstscript.translation;

import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.ImFunction;

import java.util.*;

/** Optional request-local work measurement; counts actual copy, lowering and emission entry points. */
public final class BackendWork implements AutoCloseable {
    private static final ThreadLocal<BackendWork> ACTIVE = new ThreadLocal<>();
    private final Set<ImFunction> copied = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> lowered = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> emitted = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> compiletimeLowered = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<ImFunction> runtimeLowered = Collections.newSetFromMap(new IdentityHashMap<>());
    private int compiletimeDepth;
    private long copiedNodes;

    public record Snapshot(int copiedFunctions, long copiedNodes, int loweredFunctions, int emittedFunctions,
                           int compiletimeLoweredFunctions, int runtimeLoweredFunctions) {
        public Snapshot(int copiedFunctions, long copiedNodes, int loweredFunctions, int emittedFunctions) {
            this(copiedFunctions, copiedNodes, loweredFunctions, emittedFunctions, 0, loweredFunctions);
        }
    }
    public static final class Phase implements AutoCloseable {
        private final BackendWork work = ACTIVE.get();
        private Phase() { if (work != null) work.compiletimeDepth++; }
        public void close() { if (work != null) work.compiletimeDepth--; }
    }
    public static Phase compiletime() { return new Phase(); }

    private BackendWork() { }

    public static BackendWork measure() {
        if (ACTIVE.get() != null) throw new IllegalStateException("Backend work measurement is already active");
        BackendWork work = new BackendWork();
        ACTIVE.set(work);
        return work;
    }

    public Snapshot snapshot() {
        return new Snapshot(copied.size(), copiedNodes, lowered.size(), emitted.size(), compiletimeLowered.size(), runtimeLowered.size());
    }

    public static void copied(Element node) {
        BackendWork work = ACTIVE.get();
        if (work != null) {
            work.copiedNodes++;
            if (node instanceof ImFunction function) work.copied.add(function);
        }
    }

    public static void lowered(ImFunction function) {
        BackendWork work = ACTIVE.get();
        if (work != null) {
            work.lowered.add(function);
            (work.compiletimeDepth > 0 ? work.compiletimeLowered : work.runtimeLowered).add(function);
        }
    }

    public static void emitted(ImFunction function) {
        BackendWork work = ACTIVE.get();
        if (work != null) work.emitted.add(function);
    }

    @Override public void close() {
        if (ACTIVE.get() != this) throw new IllegalStateException("Backend work measurement closed out of order");
        ACTIVE.remove();
    }
}
