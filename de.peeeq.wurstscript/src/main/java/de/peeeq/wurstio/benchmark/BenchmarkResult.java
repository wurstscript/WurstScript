package de.peeeq.wurstio.benchmark;

import java.util.List;
import java.util.Objects;

public record BenchmarkResult(
    String qualifiedName,
    int checksum,
    int batchSize,
    List<Long> samplesNanos
) {
    public BenchmarkResult {
        Objects.requireNonNull(qualifiedName, "qualifiedName");
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        samplesNanos = List.copyOf(Objects.requireNonNull(samplesNanos, "samplesNanos"));
        if (samplesNanos.isEmpty()) {
            throw new IllegalArgumentException("at least one benchmark sample is required");
        }
        for (Long sample : samplesNanos) {
            if (sample < 0) {
                throw new IllegalArgumentException("benchmark samples must be non-negative");
            }
        }
    }
}
