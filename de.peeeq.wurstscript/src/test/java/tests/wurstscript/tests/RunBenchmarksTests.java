package tests.wurstscript.tests;

import de.peeeq.wurstio.WurstCompilerJassImpl;
import de.peeeq.wurstio.benchmark.BenchmarkResult;
import de.peeeq.wurstio.benchmark.RunBenchmarks;
import de.peeeq.wurstio.benchmark.BenchmarkOptions;
import de.peeeq.wurstio.benchmark.BenchmarkSessionTestSupport;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.io.PrintStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class RunBenchmarksTests extends WurstScriptTest {

    @Test
    public void discoversPublicBenchmarkNamesAndExcludesTests() {
        Compilation compilation = compile("""
            package Bench
            native testSuccess()
            @benchmark function work() returns int
                return 4950
            @test function notABenchmark()
                testSuccess()
            """);

        RunBenchmarks runner = new RunBenchmarks(() -> 0L);

        assertEquals(runner.discover(compilation.program(), Optional.of("bench.work")), List.of("Bench.work"));
    }

    @Test
    public void dualAnnotatedFunctionRunsAsBenchmark() {
        Compilation compilation = compile("""
            package Bench
            @test @benchmark function checkedWork() returns int
                return 4950
            """);
        RunBenchmarks runner = new RunBenchmarks(new FakeClock(0L, 100L));

        assertEquals(runner.discover(compilation.program(), Optional.empty()), List.of("Bench.checkedWork"));
        BenchmarkResult result = runner.run(
            compilation.translator(), compilation.program(), "Bench.checkedWork",
            new BenchmarkOptions(0, 1, 0));
        assertEquals(result.checksum(), 4950);
    }

    @Test
    public void measuresIntegerNanosecondsAndCalculatesPercentiles() {
        Compilation compilation = compile("""
            package Bench
            @benchmark function work() returns int
                return 4950
            """);
        FakeClock clock = new FakeClock(0L, 100L, 100L, 220L, 220L, 330L);
        RunBenchmarks runner = new RunBenchmarks(clock);

        BenchmarkResult result = runner.run(
            compilation.translator(), compilation.program(), "Bench.work",
            new BenchmarkOptions(0, 3, 0));

        assertEquals(result.checksum(), 4950);
        assertEquals(result.batchSize(), 1);
        assertEquals(result.samplesNanos(), List.of(100L, 120L, 110L));
    }

    @Test
    public void calibrationSamplesAreNotReported() {
        Compilation compilation = compile("""
            package Bench
            @benchmark function work() returns int
                return 4950
            """);
        FakeClock clock = new FakeClock(0L, 10L, 10L, 110L);
        RunBenchmarks runner = new RunBenchmarks(clock);

        BenchmarkResult result = runner.run(
            compilation.translator(), compilation.program(), "Bench.work",
            new BenchmarkOptions(0, 1, 10));

        assertEquals(result.batchSize(), 1);
        assertEquals(result.samplesNanos(), List.of(100L));
    }

    @Test
    public void adaptiveCalibrationNormalizesBatchesAndExcludesCalibrationSamples() {
        Compilation compilation = compile("""
            package Bench
            @benchmark function work() returns int
                return 4950
            """);
        TrackingSession session = new TrackingSession(false, false);
        FakeClock clock = new FakeClock(0L, 4L, 4L, 14L, 14L, 25L);

        BenchmarkResult result = new RunBenchmarks(
            clock,
            (translator, program) -> session).run(
                compilation.translator(), compilation.program(), "Bench.work",
                new BenchmarkOptions(0, 1, 10));

        assertEquals(result.batchSize(), 2);
        assertEquals(result.samplesNanos(), List.of(5L));
        assertEquals(session.invokeCalls, 5);
    }

    @Test
    public void warmupsPrecedeCalibrationAndMeasurement() {
        Compilation compilation = compile("""
            package Bench
            @benchmark function work() returns int
                return 4950
            """);
        TrackingSession session = new TrackingSession(false, false);
        EventClock clock = new EventClock(List.of(0L, 4L, 4L, 14L, 14L, 25L), session.events);

        BenchmarkResult result = new RunBenchmarks(
            clock,
            (translator, program) -> session).run(
                compilation.translator(), compilation.program(), "Bench.work",
                new BenchmarkOptions(2, 1, 10));

        assertEquals(result.checksum(), 4950);
        assertEquals(result.batchSize(), 2);
        assertEquals(result.samplesNanos(), List.of(5L));
        assertEquals(session.events, List.of(
            "initialize", "invoke", "invoke", "clock", "invoke", "clock", "clock", "invoke", "invoke",
            "clock", "clock", "invoke", "invoke", "clock", "close"));
    }

    @Test
    public void changingChecksumsFailDuringWarmup() {
        Compilation compilation = compile("""
            package Bench
            @benchmark function changing() returns int
                return 42
            """);
        TrackingSession session = new TrackingSession(false, false, List.of(42, 43));

        IllegalStateException failure;
        try {
            new RunBenchmarks(
                new EventClock(List.of()),
                (translator, program) -> session).run(
                    compilation.translator(), compilation.program(), "Bench.changing",
                    new BenchmarkOptions(2, 1, 0));
            fail("changing checksum should fail during warmup");
            return;
        } catch (IllegalStateException e) {
            failure = e;
        }

        assertTrue(failure.getMessage().contains("changing checksums"), failure.getMessage());
        assertEquals(session.events, List.of("initialize", "invoke", "invoke", "close"));
    }

    @Test
    public void rawBenchmarkResultsRejectInvalidSamples() {
        assertThrows(IllegalArgumentException.class, () ->
            new BenchmarkResult("Bench.work", 1, 1, List.of()));
        assertThrows(IllegalArgumentException.class, () ->
            new BenchmarkResult("Bench.work", 1, 1, List.of(-1L)));

        BenchmarkResult result = new BenchmarkResult("Bench.work", 1, 1, List.of(1L));
        assertEquals(result.samplesNanos(), List.of(1L));
    }

    @Test
    public void changingChecksumsFailDuringMeasurement() {
        Compilation compilation = compile("""
            package Bench
            int counter = 0
            @benchmark function changing() returns int
                counter += 1
                return counter
            """);

        IllegalStateException failure;
        try {
            new RunBenchmarks(new FakeClock(0L, 1L, 1L, 2L)).run(
                compilation.translator(), compilation.program(), "Bench.changing",
                new BenchmarkOptions(0, 2, 0));
            fail("changing checksum should fail");
            return;
        } catch (IllegalStateException e) {
            failure = e;
        }
        assertTrue(failure.getMessage().contains("changing checksums"), failure.getMessage());
    }

    @Test
    public void interpreterExceptionsEscapeTheBenchmarkRun() {
        Compilation compilation = compile("""
            package Bench
            int denominator = 0
            @benchmark function failing() returns int
                return 1 div denominator
            """);

        assertThrows(RuntimeException.class, () ->
            new RunBenchmarks(new FakeClock(0L, 1L)).run(
                compilation.translator(), compilation.program(), "Bench.failing",
                new BenchmarkOptions(0, 1, 0)));
    }

    @Test
    public void benchmarkSessionClosesOnSuccessAndFailure() {
        Compilation compilation = compile("""
            package Bench
            @benchmark function work() returns int
                return 4950
            """);

        TrackingSession success = new TrackingSession(false, false);
        BenchmarkResult result = new RunBenchmarks(
            new FakeClock(0L, 100L),
            (translator, program) -> success).run(
                compilation.translator(), compilation.program(), "Bench.work",
                new BenchmarkOptions(0, 1, 0));
        assertEquals(result.checksum(), 4950);
        assertEquals(success.initializeCalls, 1);
        assertEquals(success.invokeCalls, 1);
        assertEquals(success.events, List.of("initialize", "invoke", "close"));

        TrackingSession initializationFailure = new TrackingSession(true, false);
        assertThrows(IllegalStateException.class, () -> new RunBenchmarks(
            new FakeClock(),
            (translator, program) -> initializationFailure).run(
                compilation.translator(), compilation.program(), "Bench.work",
                new BenchmarkOptions(0, 1, 0)));
        assertEquals(initializationFailure.initializeCalls, 1);
        assertEquals(initializationFailure.invokeCalls, 0);
        assertEquals(initializationFailure.events, List.of("initialize", "close"));

        TrackingSession invocationFailure = new TrackingSession(false, true);
        assertThrows(IllegalStateException.class, () -> new RunBenchmarks(
            new FakeClock(0L, 100L),
            (translator, program) -> invocationFailure).run(
                compilation.translator(), compilation.program(), "Bench.work",
                new BenchmarkOptions(0, 1, 0)));
        assertEquals(invocationFailure.initializeCalls, 1);
        assertEquals(invocationFailure.invokeCalls, 1);
        assertEquals(invocationFailure.events, List.of("initialize", "invoke", "close"));
    }

    @Test
    public void realInterpreterProviderClosesAfterSuccessAndBenchmarkFailure() {
        Compilation successCompilation = compile("""
            package Bench
            @compiletimenative function trackedNative() returns int
                return 7
            @benchmark function work() returns int
                return trackedNative()
            """);
        TrackingProvider successProvider = new TrackingProvider(false);
        BenchmarkResult success = new RunBenchmarks(
            new EventClock(List.of(0L, 100L)),
            BenchmarkSessionTestSupport.defaultSessionFactory(successProvider)).run(
                successCompilation.translator(), successCompilation.program(), "Bench.work",
                new BenchmarkOptions(0, 1, 0));

        assertEquals(success.checksum(), 7);
        assertTrue(successProvider.invoked);
        assertTrue(successProvider.closed);

        Compilation failureCompilation = compile("""
            package Bench
            @compiletimenative function trackedNative() returns int
                return 7
            @benchmark function work() returns int
                return trackedNative()
            """);
        TrackingProvider failureProvider = new TrackingProvider(true);

        assertThrows(RuntimeException.class, () -> new RunBenchmarks(
            new EventClock(List.of(0L)),
            BenchmarkSessionTestSupport.defaultSessionFactory(failureProvider)).run(
                failureCompilation.translator(), failureCompilation.program(), "Bench.work",
                new BenchmarkOptions(0, 1, 0)));
        assertTrue(failureProvider.invoked);
        assertTrue(failureProvider.closed);
    }

    private Compilation compile(String source) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        WurstCompilerJassImpl compiler = new WurstCompilerJassImpl(null, gui, null, new RunArgs());
        WurstModel model = parseFiles(null,
            Collections.singletonList(new CU("benchmark", Utils.join(source.lines().toList(), "\n") + "\n")),
            false,
            compiler);
        compiler.checkProg(model);
        assertTrue(gui.getErrorList().isEmpty(), gui.getErrorsAndWarnings().toString());
        ImProg program = compiler.translateProgToIm(model);
        assertTrue(program != null, gui.getErrorsAndWarnings().toString());
        return new Compilation(compiler.getImTranslator(), program);
    }

    private record Compilation(ImTranslator translator, ImProg program) {
    }

    private static final class FakeClock implements de.peeeq.wurstio.benchmark.BenchmarkClock {
        private final ArrayDeque<Long> values = new ArrayDeque<>();

        private FakeClock(long... values) {
            for (long value : values) {
                this.values.add(value);
            }
        }

        @Override
        public long nanoTime() {
            if (values.isEmpty()) {
                throw new AssertionError("fake clock exhausted");
            }
            return values.removeFirst();
        }
    }

    private static final class EventClock implements de.peeeq.wurstio.benchmark.BenchmarkClock {
        private final ArrayDeque<Long> values = new ArrayDeque<>();
        private final List<String> events;

        private EventClock(List<Long> values) {
            this(values, new java.util.ArrayList<>());
        }

        private EventClock(List<Long> values, List<String> events) {
            this.values.addAll(values);
            this.events = events;
        }

        @Override
        public long nanoTime() {
            events.add("clock");
            if (values.isEmpty()) {
                throw new AssertionError("fake clock exhausted");
            }
            return values.removeFirst();
        }
    }

    private static final class TrackingSession implements RunBenchmarks.BenchmarkSession {
        private final boolean failInitialization;
        private final boolean failInvocation;
        private final List<String> events = new java.util.ArrayList<>();
        private final ArrayDeque<Integer> returnValues;
        private int initializeCalls;
        private int invokeCalls;

        private TrackingSession(boolean failInitialization, boolean failInvocation) {
            this(failInitialization, failInvocation, List.of(4950));
        }

        private TrackingSession(boolean failInitialization, boolean failInvocation, List<Integer> returnValues) {
            this.failInitialization = failInitialization;
            this.failInvocation = failInvocation;
            this.returnValues = new ArrayDeque<>(returnValues);
        }

        @Override
        public void runCompiletime() {
            events.add("initialize");
            initializeCalls++;
            if (failInitialization) {
                throw new IllegalStateException("initialization failed");
            }
        }

        @Override
        public ILconst invoke(de.peeeq.wurstscript.jassIm.ImFunction function) {
            events.add("invoke");
            invokeCalls++;
            if (failInvocation) {
                throw new IllegalStateException("invocation failed");
            }
            return ILconstInt.create(returnValues.isEmpty() ? 4950 : returnValues.removeFirst());
        }

        @Override
        public void close() {
            events.add("close");
        }
    }

    private static final class TrackingProvider implements NativesProvider {
        private final boolean fail;
        private boolean invoked;
        private boolean closed;

        private TrackingProvider(boolean fail) {
            this.fail = fail;
        }

        @Override
        public ILconst invoke(String funcname, ILconst[] args)
            throws de.peeeq.wurstscript.intermediatelang.interpreter.NoSuchNativeException {
            if (!funcname.equals("trackedNative")) {
                throw new de.peeeq.wurstscript.intermediatelang.interpreter.NoSuchNativeException(funcname);
            }
            invoked = true;
            if (fail) {
                throw new IllegalStateException("tracked provider failure");
            }
            return ILconstInt.create(7);
        }

        @Override
        public void setOutStream(PrintStream outStream) {
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
