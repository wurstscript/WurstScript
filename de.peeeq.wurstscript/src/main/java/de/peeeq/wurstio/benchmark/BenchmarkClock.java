package de.peeeq.wurstio.benchmark;

@FunctionalInterface
public interface BenchmarkClock {
    long nanoTime();
}
