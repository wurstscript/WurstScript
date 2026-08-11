package tests.wurstscript.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.peeeq.wurstio.Main;
import de.peeeq.wurstio.benchmark.BenchmarkResult;
import de.peeeq.wurstio.benchmark.BenchmarkWorkerOutput;
import de.peeeq.wurstscript.RunArgs;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class BenchmarkCliTests {

    @Test
    public void parsesDiscoveryWorkerArguments() throws Exception {
        Path output = Files.createTempFile("benchmark-worker", ".json");
        RunArgs args = new RunArgs(
            "-runbenchmarks",
            "-benchmarkList",
            "-benchmarkFilter", "Polygon",
            "-benchmarkOutput", output.toString());

        assertTrue(args.isRunBenchmarks());
        assertTrue(args.isBenchmarkList());
        assertEquals(args.getBenchmarkFilter(), "Polygon");
        assertEquals(args.getBenchmarkOutput(), output.toString());
    }

    @Test
    public void parsesExecutionWorkerArguments() throws Exception {
        Path output = Files.createTempFile("benchmark-worker", ".json");
        RunArgs args = new RunArgs(
            "-runbenchmarks",
            "-benchmarkName", "Bench.work",
            "-benchmarkWarmup", "4",
            "-benchmarkIterations", "8",
            "-benchmarkOutput", output.toString());

        assertTrue(args.isRunBenchmarks());
        assertFalse(args.isBenchmarkList());
        assertEquals(args.getBenchmarkName(), "Bench.work");
        assertEquals(args.getBenchmarkWarmup(), 4);
        assertEquals(args.getBenchmarkIterations(), 8);
        assertEquals(args.getBenchmarkOutput(), output.toString());
    }

    @Test
    public void rejectsInvalidBenchmarkArgumentCombinations() {
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkList", "-benchmarkName", "Bench.work",
            "-benchmarkOutput", "out.json"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkList"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work",
            "-benchmarkWarmup", "-1", "-benchmarkIterations", "1",
            "-benchmarkOutput", "out.json"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work",
            "-benchmarkWarmup", "0", "-benchmarkIterations", "0",
            "-benchmarkOutput", "out.json"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work",
            "-benchmarkWarmup", "nope", "-benchmarkIterations", "1",
            "-benchmarkOutput", "out.json"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkList",
            "-benchmarkWarmup", "0", "-benchmarkIterations", "1",
            "-benchmarkOutput", "out.json"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work", "-benchmarkOutput", "out.json",
            "-runtests"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work", "-benchmarkOutput", "out.json",
            "-runcompiletimefunctions"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work", "-benchmarkOutput", "out.json",
            "-build"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work", "-benchmarkOutput", "out.json",
            "-dev"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work", "-benchmarkOutput", "out.json",
            "-out", "compiled.j.txt"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work", "-benchmarkOutput", "out.json",
            "-testFilter", "Bench"));
        assertThrows(RuntimeException.class, () -> new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work", "-benchmarkOutput", "out.json",
            "-testTimeout", "30"));
    }

    @Test
    public void preservesCompilerProjectOptionsInBenchmarkMode() {
        RunArgs args = new RunArgs(
            "-runbenchmarks", "-benchmarkName", "Bench.work", "-benchmarkOutput", "out.json",
            "-lua", "-lib", "dependency", "-noPJass", "-legacyJassChecks");

        assertTrue(args.isRunBenchmarks());
        assertTrue(args.isLua());
        assertTrue(args.isDisablePjass());
        assertTrue(args.isLegacyJassTypeChecks());
        assertEquals(args.getAdditionalLibDirs().size(), 1);
    }

    @Test
    public void writesDiscoveryJsonWithWorkerSchema() throws Exception {
        Path output = Files.createTempFile("benchmark-worker", ".json");
        BenchmarkWorkerOutput.writeDiscovery(output, List.of("Bench.first", "Bench.second"));

        JsonObject json = JsonParser.parseString(Files.readString(output)).getAsJsonObject();
        assertEquals(json.get("schema").getAsString(), "wurst-benchmark-worker-v2");
        assertEquals(json.get("mode").getAsString(), "discovery");
        assertEquals(json.getAsJsonArray("benchmarks").size(), 2);
        assertEquals(json.getAsJsonArray("benchmarks").get(0).getAsString(), "Bench.first");
    }

    @Test
    public void writesExecutionJsonWithRawSamplesWithoutDerivedStatistics() throws Exception {
        Path output = Files.createTempFile("benchmark-worker", ".json");
        List<Long> samples = List.of(100L, 120L, 110L);
        BenchmarkResult result = new BenchmarkResult("Bench.work", 4950, 2, samples);

        BenchmarkWorkerOutput.writeResult(output, result);

        JsonObject json = JsonParser.parseString(Files.readString(output)).getAsJsonObject();
        assertEquals(json.get("schema").getAsString(), "wurst-benchmark-worker-v2");
        assertEquals(json.get("mode").getAsString(), "execution");
        assertEquals(json.get("qualifiedName").getAsString(), "Bench.work");
        assertEquals(json.get("checksum").getAsInt(), 4950);
        assertEquals(json.get("batchSize").getAsInt(), 2);
        assertEquals(json.getAsJsonArray("samplesNanos").get(1).getAsLong(), 120L);
        assertFalse(json.has("statistics"));
    }

    @Test
    public void compilerWorkerDiscoversWithoutGeneratingScript() throws Exception {
        Path directory = Files.createTempDirectory("benchmark-cli");
        Path source = writeBenchmarkSource(directory);
        Path output = directory.resolve("discovery.json");

        ProcessResult process = runCompiler(directory,
            source, "-lua", "-runbenchmarks", "-benchmarkList", "-benchmarkFilter", "Bench",
            "-benchmarkOutput", output.toString());

        assertEquals(process.exitCode(), 0, process.output());
        assertTrue(Files.exists(output));
        assertFalse(Files.exists(directory.resolve("compiled.j.txt")));
        assertFalse(Files.exists(directory.resolve("temp/output.j")));
        JsonObject json = JsonParser.parseString(Files.readString(output)).getAsJsonObject();
        assertEquals(json.getAsJsonArray("benchmarks").get(0).getAsString(), "Bench.work");
    }

    @Test
    public void compilerWorkerRejectsNonExecutableBenchmarkBeforeDiscovery() throws Exception {
        Path directory = Files.createTempDirectory("benchmark-cli-invalid");
        Path source = directory.resolve("Bench.wurst");
        Files.writeString(source, """
            package Bench
            @benchmark native nativeBenchmark() returns int
            """);
        Path output = directory.resolve("discovery.json");

        ProcessResult process = runCompiler(directory,
            source, "-runbenchmarks", "-benchmarkList", "-benchmarkFilter", "Bench",
            "-benchmarkOutput", output.toString());

        assertTrue(process.exitCode() != 0, process.output());
        assertTrue(process.output().contains(
            "@benchmark functions must be executable by the compiletime IL interpreter; native, extern, and compiletimenative declarations are not supported."), process.output());
        assertFalse(Files.exists(output));
    }

    @Test
    public void compilerWorkerExecutesOneBenchmarkAndPreservesOutputOnFailure() throws Exception {
        Path directory = Files.createTempDirectory("benchmark-cli");
        Path source = writeBenchmarkSource(directory);
        Path output = directory.resolve("execution.json");

        ProcessResult success = runCompiler(directory,
            source, "-runbenchmarks", "-benchmarkName", "Bench.work",
            "-benchmarkWarmup", "0", "-benchmarkIterations", "1",
            "-benchmarkOutput", output.toString());

        assertEquals(success.exitCode(), 0, success.output());
        JsonObject json = JsonParser.parseString(Files.readString(output)).getAsJsonObject();
        assertEquals(json.get("checksum").getAsInt(), 42);
        assertTrue(json.getAsJsonArray("samplesNanos").size() == 1);
        assertFalse(Files.exists(directory.resolve("compiled.j.txt")));
        assertFalse(Files.exists(directory.resolve("temp/output.j")));

        Files.writeString(output, "existing valid output");
        ProcessResult failure = runCompiler(directory,
            source, "-runbenchmarks", "-benchmarkName", "Bench.missing",
            "-benchmarkWarmup", "0", "-benchmarkIterations", "1",
            "-benchmarkOutput", output.toString());

        assertTrue(failure.exitCode() != 0, failure.output());
        assertEquals(Files.readString(output), "existing valid output");
        assertFalse(Files.exists(directory.resolve("compiled.j.txt")));
        assertFalse(Files.exists(directory.resolve("temp/output.j")));
    }

    @Test
    public void compilerWorkerExecutesExactBenchmarkForLuaWithoutGeneratingScript() throws Exception {
        Path directory = Files.createTempDirectory("benchmark-cli-lua");
        Path source = writeBenchmarkSource(directory);
        Path output = directory.resolve("execution.json");

        ProcessResult process = runCompiler(directory,
            source, "-lua", "-runbenchmarks", "-benchmarkName", "Bench.work",
            "-benchmarkWarmup", "0", "-benchmarkIterations", "1",
            "-benchmarkOutput", output.toString());

        assertEquals(process.exitCode(), 0, process.output());
        JsonObject json = JsonParser.parseString(Files.readString(output)).getAsJsonObject();
        assertEquals(json.get("checksum").getAsInt(), 42);
        assertFalse(Files.exists(directory.resolve("compiled.j.txt")));
        assertFalse(Files.exists(directory.resolve("temp/output.j")));
    }

    @Test
    public void exactNameWorkerExecutesOnlyRequestedBenchmark() throws Exception {
        Path directory = Files.createTempDirectory("benchmark-cli-multiple");
        Path source = writeMultipleBenchmarkSource(directory);
        Path output = directory.resolve("execution.json");

        ProcessResult process = runCompiler(directory,
            source, "-runbenchmarks", "-benchmarkName", "Bench.second",
            "-benchmarkWarmup", "0", "-benchmarkIterations", "1",
            "-benchmarkOutput", output.toString());

        assertEquals(process.exitCode(), 0, process.output());
        JsonObject json = JsonParser.parseString(Files.readString(output)).getAsJsonObject();
        assertEquals(json.get("qualifiedName").getAsString(), "Bench.second");
        assertEquals(json.get("checksum").getAsInt(), 22,
            "first benchmark would have changed the second benchmark checksum");
    }

    @Test
    public void atomicWriterPreservesDestinationAndCleansTempAfterInjectedFailure() throws Exception {
        Path directory = Files.createTempDirectory("benchmark-atomic");
        Path output = directory.resolve("worker.json");
        Files.writeString(output, "old output");

        assertThrows(IOException.class, () -> BenchmarkWorkerOutput.writeAtomically(
            output,
            "new output",
            (temporary, json) -> {
                Files.writeString(temporary, json);
                throw new IOException("forced temporary writer failure");
            }));

        assertEquals(Files.readString(output), "old output");
        try (Stream<Path> files = Files.list(directory)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    public void atomicWriterSupportsShortDestinationBasenames() throws Exception {
        Path directory = Files.createTempDirectory("benchmark-atomic-short");
        Path output = directory.resolve("x");

        BenchmarkWorkerOutput.writeDiscovery(output, List.of("Bench.work"));

        assertTrue(Files.exists(output));
        assertTrue(Files.readString(output).contains("Bench.work"));
    }

    private static Path writeBenchmarkSource(Path directory) throws Exception {
        Path source = directory.resolve("Bench.wurst");
        Files.writeString(source, """
            package Bench
            @benchmark function work() returns int
                return 42
            """);
        return source;
    }

    private static Path writeMultipleBenchmarkSource(Path directory) throws Exception {
        Path source = directory.resolve("MultipleBench.wurst");
        Files.writeString(source, """
            package Bench
            int firstCalls = 0
            int secondCalls = 0
            @benchmark function first() returns int
                firstCalls += 1
                return 11
            @benchmark function second() returns int
                secondCalls += 1
                return firstCalls * 1000 + 22
            """);
        return source;
    }

    private static ProcessResult runCompiler(Path directory, Path source, String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(Main.class.getName());
        command.add(source.toString());
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command)
            .directory(directory.toFile())
            .redirectErrorStream(true)
            .start();
        String output = new String(process.getInputStream().readAllBytes());
        return new ProcessResult(process.waitFor(), output);
    }

    private record ProcessResult(int exitCode, String output) {
    }
}
