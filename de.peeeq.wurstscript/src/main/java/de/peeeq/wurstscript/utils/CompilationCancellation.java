package de.peeeq.wurstscript.utils;

import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

/** Cooperative cancellation scoped to one compilation, without interrupting the language worker. */
public final class CompilationCancellation {
    private static final ThreadLocal<BooleanSupplier> CURRENT = new ThreadLocal<>();

    private CompilationCancellation() {}

    public static void run(BooleanSupplier canceled, Runnable action) {
        BooleanSupplier previous = CURRENT.get();
        CURRENT.set(canceled);
        try {
            action.run();
        } finally {
            if (previous == null) CURRENT.remove();
            else CURRENT.set(previous);
        }
    }

    public static void check() {
        BooleanSupplier canceled = CURRENT.get();
        if (canceled != null && canceled.getAsBoolean()) {
            throw new CancellationException("Map command canceled");
        }
    }

    public static boolean isScoped() {
        return CURRENT.get() != null;
    }
}
