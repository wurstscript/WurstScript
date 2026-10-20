package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.attributes.HasAnnotation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class GlobalCaches {
    public enum Mode {TEST_ISOLATED, DEV_PERSISTENT}

    private static volatile Mode mode = Mode.DEV_PERSISTENT;

    public static void setMode(Mode m) {
        mode = m;
    }

    public static Mode mode() {
        return mode;
    }

    private GlobalCaches() {
    }

    /**
     * Call this between tests (and after each compile)
     */
    public static void clearAll() {
        lookupCache.clear();
        HasAnnotation.clearCaches();
    }

    /**
     * Clears lookup entries whose query element belongs to one of the changed roots.
     * This keeps editor validation from throwing away hot lookup data for unchanged files.
     */
    public static void clearLookupCacheFor(Collection<? extends Element> roots) {
        if (roots.isEmpty()) {
            return;
        }
        Set<Element> affected = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<Element> todo = new ArrayDeque<>(roots);
        while (!todo.isEmpty()) {
            Element e = todo.removeLast();
            if (!affected.add(e)) {
                continue;
            }
            for (int i = 0; i < e.size(); i++) {
                todo.add(e.get(i));
            }
        }
        lookupCache.keySet().removeIf(key -> affected.contains(key.element));
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
            if (!(o instanceof CacheKey that)) return false;
            return element == that.element && name.equals(that.name) && type == that.type;
        }

        @Override
        public int hashCode() {
            return 31 * (31 * System.identityHashCode(element) + name.hashCode()) + type.hashCode();
        }
    }

    public static final Map<CacheKey, Object> lookupCache = new Object2ObjectOpenHashMap<>();
}
