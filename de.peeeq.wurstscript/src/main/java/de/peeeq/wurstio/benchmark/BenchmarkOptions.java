package de.peeeq.wurstio.benchmark;

public record BenchmarkOptions(int warmupIterations, int measurementIterations, long minimumSampleNanos) {
    public BenchmarkOptions {
        if (warmupIterations < 0) {
            throw new IllegalArgumentException("warmupIterations must be non-negative");
        }
        if (measurementIterations <= 0) {
            throw new IllegalArgumentException("measurementIterations must be positive");
        }
        if (minimumSampleNanos < 0) {
            throw new IllegalArgumentException("minimumSampleNanos must be non-negative");
        }
    }
}
