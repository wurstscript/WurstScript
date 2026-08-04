package de.peeeq.wurstio.benchmark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

/** Writes the machine-readable result of one isolated benchmark compiler worker. */
public final class BenchmarkWorkerOutput {
    public static final String SCHEMA = "wurst-benchmark-worker-v2";

    @FunctionalInterface
    public interface TemporaryFileWriter {
        void write(Path temporary, String json) throws IOException;
    }

    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .create();

    private BenchmarkWorkerOutput() {
    }

    public static void writeDiscovery(Path output, List<String> benchmarkNames) throws IOException {
        Objects.requireNonNull(benchmarkNames, "benchmarkNames");
        JsonObject json = envelope("discovery");
        JsonArray benchmarks = new JsonArray();
        for (String benchmarkName : benchmarkNames) {
            benchmarks.add(Objects.requireNonNull(benchmarkName, "benchmarkName"));
        }
        json.add("benchmarks", benchmarks);
        writeAtomically(output, GSON.toJson(json));
    }

    public static void writeResult(Path output, BenchmarkResult result) throws IOException {
        Objects.requireNonNull(result, "result");
        JsonObject json = envelope("execution");
        json.addProperty("qualifiedName", result.qualifiedName());
        json.addProperty("checksum", result.checksum());
        json.addProperty("batchSize", result.batchSize());
        json.add("samplesNanos", GSON.toJsonTree(result.samplesNanos()));
        writeAtomically(output, GSON.toJson(json));
    }

    /**
     * Write a complete JSON document to a sibling temporary file, then rename it
     * over the destination. Serialization happens before touching the destination.
     */
    public static void writeAtomically(Path output, String json) throws IOException {
        writeAtomically(output, json, BenchmarkWorkerOutput::writeTemporaryFile);
    }

    public static void writeAtomically(
        Path output,
        String json,
        TemporaryFileWriter temporaryFileWriter
    ) throws IOException {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(temporaryFileWriter, "temporaryFileWriter");
        Path absoluteOutput = output.toAbsolutePath();
        Path parent = absoluteOutput.getParent();
        if (parent == null) {
            throw new IOException("benchmark output has no parent directory: " + output);
        }
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "wurst-benchmark-", ".tmp");
        try {
            temporaryFileWriter.write(temporary, json);
            try {
                Files.move(
                    temporary,
                    absoluteOutput,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, absoluteOutput, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void writeTemporaryFile(Path temporary, String json) throws IOException {
        Files.writeString(
            temporary,
            json,
            StandardCharsets.UTF_8,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static JsonObject envelope(String mode) {
        JsonObject json = new JsonObject();
        json.addProperty("schema", SCHEMA);
        json.addProperty("mode", mode);
        return json;
    }
}
