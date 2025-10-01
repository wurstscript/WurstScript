package de.peeeq.wurstscript.validation;

import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.interpreter.LocalState;
import de.peeeq.wurstscript.types.WurstType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Arrays;

// Expose static fields only if you already have them there; otherwise, just clear via dedicated methods.
public final class GlobalCaches {
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

    public enum Mode { TEST_ISOLATED, DEV_PERSISTENT }
    private static volatile Mode mode = Mode.DEV_PERSISTENT;

    public static void setMode(Mode m) { mode = m; }
    public static Mode mode() { return mode; }

    private GlobalCaches() {}

    public static final Object2ObjectOpenHashMap<Object, Object2ObjectOpenHashMap<ArgumentKey, LocalState>> LOCAL_STATE_CACHE = new Object2ObjectOpenHashMap<>();
    public static final Reference2ObjectOpenHashMap<WurstType, Reference2BooleanOpenHashMap<WurstType>> SUBTYPE_MEMO = new Reference2ObjectOpenHashMap<>();

    /** Call this between tests (and after each compile) */
    public static void clearAll() {
        LOCAL_STATE_CACHE.clear();
        SUBTYPE_MEMO.clear();
    }
}
