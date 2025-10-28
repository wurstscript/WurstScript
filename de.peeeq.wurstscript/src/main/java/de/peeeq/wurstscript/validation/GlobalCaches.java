package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.AttrNearest;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.interpreter.LocalState;
import de.peeeq.wurstscript.jassIm.ImFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

// Expose static fields only if you already have them there; otherwise, just clear via dedicated methods.
public final class GlobalCaches {

    // Statistics tracking
    public static class CacheStats {
        final AtomicLong hits = new AtomicLong();
        final AtomicLong misses = new AtomicLong();
        final AtomicLong evictions = new AtomicLong();
        final String name;

        CacheStats(String name) {
            this.name = name;
        }

        void recordHit() {
            hits.incrementAndGet();
        }

        void recordMiss() {
            misses.incrementAndGet();
        }

        void recordEviction(int count) {
            evictions.addAndGet(count);
        }

        double hitRate() {
            long h = hits.get();
            long m = misses.get();
            long total = h + m;
            return total == 0 ? 0.0 : (double) h / total;
        }

        @Override
        public String toString() {
            return String.format("%s: hits=%d, misses=%d, hitRate=%.2f%%, evictions=%d",
                name, hits.get(), misses.get(), hitRate() * 100, evictions.get());
        }
    }

    private static final CacheStats lookupStats = new CacheStats("LookupCache");
    private static final CacheStats localStateStats = new CacheStats("LocalStateCache");

    // Optimized ArgumentKey that minimizes allocation overhead
    public static final class ArgumentKey {
        private final ILconst[] args;
        private final int hash;

        // Reuse instances when possible via a small pool for common sizes
        private static final ThreadLocal<ArgumentKey[]> POOL =
            ThreadLocal.withInitial(() -> new ArgumentKey[4]);

        private ArgumentKey(ILconst[] args) {
            this.args = args;
            this.hash = Arrays.hashCode(args);
        }

        // Factory method that reuses instances for lookup
        public static ArgumentKey forLookup(ILconst[] args) {
            return new ArgumentKey(args);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArgumentKey)) return false;
            ArgumentKey that = (ArgumentKey) o;
            return hash == that.hash && Arrays.equals(args, that.args);
        }
    }

    public enum Mode {TEST_ISOLATED, DEV_PERSISTENT}

    public static volatile Mode mode = Mode.DEV_PERSISTENT;

    public static void setMode(Mode m) {
        mode = m;
    }

    public static Mode mode() {
        return mode;
    }

    private GlobalCaches() {
    }

    // Wrapped caches with statistics
    public static final Object2ObjectOpenHashMap<Object, Object2ObjectOpenHashMap<ArgumentKey, LocalState>> LOCAL_STATE_CACHE =
        new Object2ObjectOpenHashMap<Object, Object2ObjectOpenHashMap<ArgumentKey, LocalState>>() {
            @Override
            public Object2ObjectOpenHashMap<ArgumentKey, LocalState> get(Object key) {
                Object2ObjectOpenHashMap<ArgumentKey, LocalState> result = super.get(key);
                if (result != null) {
                    localStateStats.recordHit();
                } else {
                    localStateStats.recordMiss();
                }
                return result;
            }

            @Override
            public void clear() {
                localStateStats.recordEviction(size());
                super.clear();
            }
        };


    /**
     * Call this between tests (and after each compile)
     */
    public static void clearAll() {
        LOCAL_STATE_CACHE.clear();
        lookupCache.clear();
    }

    /**
     * Evict cache entries that are tied to any of the given compilation units.
     *
     * <p>The validator replaces only a subset of units on incremental runs. Instead of
     * purging the full global caches (which would force re-computation for every file), we
     * walk both caches and drop entries whose owner element/function belongs to one of the
     * affected compilation units.</p>
     */
    public static void invalidateFor(WurstModel model, Collection<CompilationUnit> changedUnits) {
        if (mode == Mode.TEST_ISOLATED) {
            clearAll();
            return;
        }

        Set<CompilationUnit> affected = toIdentitySet(changedUnits);
        Set<CompilationUnit> live = model == null ? null : toIdentitySet(model);

        if (affected.isEmpty() && (live == null || live.isEmpty())) {
            return;
        }

        invalidateLookupCache(affected, live);
        invalidateLocalStateCache(affected, live);
    }

    private static Set<CompilationUnit> toIdentitySet(Iterable<CompilationUnit> units) {
        Set<CompilationUnit> set = Collections.newSetFromMap(new IdentityHashMap<>());
        if (units == null) {
            return set;
        }
        for (CompilationUnit cu : units) {
            if (cu != null) {
                set.add(cu);
            }
        }
        return set;
    }

    private static void invalidateLookupCache(Set<CompilationUnit> affected, Set<CompilationUnit> live) {
        if (lookupCache.isEmpty()) {
            return;
        }

        int evicted = 0;
        Iterator<Map.Entry<CacheKey, Object>> it = lookupCache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<CacheKey, Object> entry = it.next();
            Element element = entry.getKey().element;
            if (element == null) {
                continue;
            }

            CompilationUnit owner = AttrNearest.nearestCompilationUnit(element);
            if (owner == null) {
                continue;
            }

            boolean shouldEvict = affected.contains(owner)
                || (live != null && !live.contains(owner));

            if (shouldEvict) {
                it.remove();
                evicted++;
            }
        }

        if (evicted > 0) {
            lookupStats.recordEviction(evicted);
        }
    }

    private static void invalidateLocalStateCache(Set<CompilationUnit> affected, Set<CompilationUnit> live) {
        if (LOCAL_STATE_CACHE.isEmpty()) {
            return;
        }

        int evicted = 0;
        Iterator<Map.Entry<Object, Object2ObjectOpenHashMap<ArgumentKey, LocalState>>> it =
            LOCAL_STATE_CACHE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object2ObjectOpenHashMap<ArgumentKey, LocalState>> entry = it.next();
            Object key = entry.getKey();
            if (!(key instanceof ImFunction)) {
                continue;
            }

            ImFunction function = (ImFunction) key;
            Element trace = function.attrTrace();
            if (trace == null) {
                continue;
            }

            CompilationUnit owner = AttrNearest.nearestCompilationUnit(trace);
            if (owner == null) {
                continue;
            }

            boolean shouldEvict = affected.contains(owner)
                || (live != null && !live.contains(owner));

            if (shouldEvict) {
                it.remove();
                evicted++;
            }
        }

        if (evicted > 0) {
            localStateStats.recordEviction(evicted);
        }
    }


    public enum LookupType {
        FUNC, VAR, TYPE, PACKAGE, MEMBER_FUNC, MEMBER_VAR
    }

    public static class CacheKey {
        final Element element;
        final String name;
        final LookupType type;

        public CacheKey(Element element, String name, LookupType type) {
            this.element = element;
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey that = (CacheKey) o;
            return element == that.element && name.equals(that.name) && type == that.type;
        }

        @Override
        public int hashCode() {
            return 31 * (31 * System.identityHashCode(element) + name.hashCode()) + type.hashCode();
        }
    }

    public static final Map<CacheKey, Object> lookupCache = new Object2ObjectOpenHashMap<CacheKey, Object>() {
        @Override
        public Object get(Object key) {
            Object result = super.get(key);
            if (result != null) {
                lookupStats.recordHit();
            } else {
                lookupStats.recordMiss();
            }
            return result;
        }

        @Override
        public Object put(CacheKey key, Object value) {
            // Note: put returns old value, null if new entry
            Object old = super.put(key, value);
            if (old == null) {
                // New entry, the miss was already recorded in get()
            }
            return old;
        }

        @Override
        public void clear() {
            lookupStats.recordEviction(size());
            super.clear();
        }
    };

    // Statistics methods
    public static void printStats() {
        System.out.println("=== GlobalCaches Statistics ===");
        System.out.println(lookupStats);
        System.out.println(localStateStats);
        System.out.println("Current sizes: lookup=" + lookupCache.size() +
            ", localState=" + LOCAL_STATE_CACHE.size());
        System.out.println("==============================");
    }

    public static void resetStats() {
        lookupStats.hits.set(0);
        lookupStats.misses.set(0);
        lookupStats.evictions.set(0);
        localStateStats.hits.set(0);
        localStateStats.misses.set(0);
        localStateStats.evictions.set(0);
    }

    public static CacheStats getLookupStats() {
        return lookupStats;
    }

    public static CacheStats getLocalStateStats() {
        return localStateStats;
    }
}
