package de.peeeq.wurstio.benchmark;

import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;

/** Test-only bridge for the package-private default-session seam. */
public final class BenchmarkSessionTestSupport {
    private BenchmarkSessionTestSupport() {
    }

    public static RunBenchmarks.BenchmarkSessionFactory defaultSessionFactory(
        NativesProvider additionalProvider
    ) {
        return RunBenchmarks.defaultSessionFactory(additionalProvider);
    }
}
