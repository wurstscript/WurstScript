package de.peeeq.wurstscript.utils;

import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

/** Cooperative cancellation scoped to one compilation, without interrupting the language worker. */
public final class CompilationCancellation {
    private static final ThreadLocal<BooleanSupplier> CURRENT = new ThreadLocal<>();

    private CompilationCancellation() {}

    @FunctionalInterface
    public interface Action<E extends Exception> {
        void run() throws E;
    }

    /** Capture the signal, not its current value, before handing work to an executor. */
    public static BooleanSupplier capture() {
        return CURRENT.get();
    }

    public static <E extends Exception> void run(BooleanSupplier canceled, Action<E> action) throws E {
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
}
