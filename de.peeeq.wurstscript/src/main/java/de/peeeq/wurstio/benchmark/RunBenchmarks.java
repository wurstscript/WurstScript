package de.peeeq.wurstio.benchmark;

import de.peeeq.wurstio.CompiletimeFunctionRunner;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.interpreter.NativesProvider;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.wurstscript.projectconfig.WurstProjectConfigData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static de.peeeq.wurstio.CompiletimeFunctionRunner.FunctionFlagToRun.CompiletimeFunctions;

public final class RunBenchmarks {
    private final BenchmarkClock clock;
    private final BenchmarkSessionFactory sessionFactory;

    @FunctionalInterface
    public interface BenchmarkSessionFactory {
        BenchmarkSession open(ImTranslator translator, ImProg program);
    }

    public interface BenchmarkSession extends AutoCloseable {
        void runCompiletime();

        ILconst invoke(ImFunction function);

        @Override
        void close();
    }

    public RunBenchmarks() {
        this(System::nanoTime, RunBenchmarks::openDefaultSession);
    }

    public RunBenchmarks(BenchmarkClock clock) {
        this(clock, RunBenchmarks::openDefaultSession);
    }

    public RunBenchmarks(BenchmarkClock clock, BenchmarkSessionFactory sessionFactory) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.sessionFactory = Objects.requireNonNull(sessionFactory, "sessionFactory");
    }

    static BenchmarkSessionFactory defaultSessionFactory(NativesProvider additionalProvider) {
        Objects.requireNonNull(additionalProvider, "additionalProvider");
        return (translator, program) -> openDefaultSession(translator, program, additionalProvider);
    }

    public List<String> discover(ImProg program, Optional<String> filter) {
        Objects.requireNonNull(program, "program");
        Objects.requireNonNull(filter, "filter");
        String normalizedFilter = filter.map(value -> value.toLowerCase(Locale.ROOT)).orElse("");
        List<String> result = new ArrayList<>();
        for (ImFunction function : program.getFunctions()) {
            if (!function.hasFlag(FunctionFlagEnum.IS_BENCHMARK)) {
                continue;
            }
            Optional<String> publicName = publicName(function);
            if (publicName.isPresent()
                && publicName.get().toLowerCase(Locale.ROOT).contains(normalizedFilter)) {
                result.add(publicName.get());
            }
        }
        result.sort(Comparator.naturalOrder());
        return List.copyOf(result);
    }

    public BenchmarkResult run(
        ImTranslator translator,
        ImProg program,
        String qualifiedName,
        BenchmarkOptions options
    ) {
        Objects.requireNonNull(translator, "translator");
        Objects.requireNonNull(program, "program");
        Objects.requireNonNull(qualifiedName, "qualifiedName");
        Objects.requireNonNull(options, "options");

        ImFunction function = findBenchmark(program, qualifiedName);

        // Every run owns its interpreter and native providers. This keeps a
        // benchmark isolated from any preceding benchmark and closes native
        // resources on successful and exceptional exits alike.
        try (BenchmarkSession session = sessionFactory.open(translator, program)) {
            session.runCompiletime();
            Checksum checksum = new Checksum();

            for (int i = 0; i < options.warmupIterations(); i++) {
                runBatch(session, function, 1, checksum);
            }

            int batchSize = calibrate(session, function, checksum, options.minimumSampleNanos());

            List<Long> samples = new ArrayList<>(options.measurementIterations());
            for (int i = 0; i < options.measurementIterations(); i++) {
                long start = clock.nanoTime();
                runBatch(session, function, batchSize, checksum);
                long elapsed = clock.nanoTime() - start;
                if (elapsed < 0) {
                    throw new IllegalStateException("benchmark clock moved backwards");
                }
                samples.add(elapsed / batchSize);
            }

            return new BenchmarkResult(
                qualifiedName,
                checksum.value(),
                batchSize,
                samples);
        }
    }

    private int calibrate(
        BenchmarkSession session,
        ImFunction function,
        Checksum checksum,
        long minimumSampleNanos
    ) {
        if (minimumSampleNanos == 0) {
            return 1;
        }

        int batchSize = 1;
        while (true) {
            long start = clock.nanoTime();
            runBatch(session, function, batchSize, checksum);
            long elapsed = clock.nanoTime() - start;
            if (elapsed < 0) {
                throw new IllegalStateException("benchmark clock moved backwards during calibration");
            }
            if (elapsed >= minimumSampleNanos) {
                return batchSize;
            }
            if (batchSize > Integer.MAX_VALUE / 2) {
                throw new IllegalStateException("benchmark batch size overflow during calibration");
            }
            batchSize *= 2;
        }
    }

    private static void runBatch(
        BenchmarkSession session,
        ImFunction function,
        int batchSize,
        Checksum checksum
    ) {
        for (int i = 0; i < batchSize; i++) {
            ILconst value = session.invoke(function);
            if (!(value instanceof ILconstInt intValue)) {
                throw new IllegalStateException(
                    "benchmark " + function.getName() + " returned "
                        + value.getClass().getSimpleName() + " instead of int");
            }
            checksum.accept(intValue.getVal());
        }
    }

    private static BenchmarkSession openDefaultSession(ImTranslator translator, ImProg program) {
        return openDefaultSession(translator, program, null);
    }

    private static BenchmarkSession openDefaultSession(
        ImTranslator translator,
        ImProg program,
        NativesProvider additionalProvider
    ) {
        WurstGuiCliImpl gui = new WurstGuiCliImpl(true);
        CompiletimeFunctionRunner compiletime = new CompiletimeFunctionRunner(
            translator,
            program,
            Optional.empty(),
            null,
            gui,
            CompiletimeFunctions,
            WurstProjectConfigData.empty(),
            false,
            false);
        if (additionalProvider != null) {
            compiletime.getInterpreter().addNativeProvider(additionalProvider);
        }
        return new DefaultBenchmarkSession(compiletime, gui);
    }

    private static final class DefaultBenchmarkSession implements BenchmarkSession {
        private final CompiletimeFunctionRunner compiletime;
        private final WurstGuiCliImpl gui;

        private DefaultBenchmarkSession(CompiletimeFunctionRunner compiletime, WurstGuiCliImpl gui) {
            this.compiletime = compiletime;
            this.gui = gui;
        }

        @Override
        public void runCompiletime() {
            compiletime.run();
            if (gui.getErrorCount() > 0) {
                throw new IllegalStateException("compiletime initialization failed: " + gui.getErrors());
            }
        }

        @Override
        public ILconst invoke(ImFunction function) {
            return compiletime.getInterpreter().runFunc(function, null);
        }

        @Override
        public void close() {
            compiletime.close();
        }
    }

    private static ImFunction findBenchmark(ImProg program, String qualifiedName) {
        List<ImFunction> matches = new ArrayList<>();
        for (ImFunction function : program.getFunctions()) {
            if (function.hasFlag(FunctionFlagEnum.IS_BENCHMARK)
                && publicName(function).filter(qualifiedName::equals).isPresent()) {
                matches.add(function);
            }
        }
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("no benchmark named " + qualifiedName);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("multiple benchmarks named " + qualifiedName);
        }
        return matches.get(0);
    }

    private static Optional<String> publicName(ImFunction function) {
        if (!(function.getTrace() instanceof FuncDef funcDef)) {
            return Optional.empty();
        }
        if (funcDef.attrNearestPackage() == null
            || funcDef.attrNearestPackage().tryGetNameDef() == null) {
            return Optional.empty();
        }
        return Optional.of(
            funcDef.attrNearestPackage().tryGetNameDef().getName() + "." + funcDef.getName());
    }

    private static final class Checksum {
        private boolean initialized;
        private int value;

        private void accept(int next) {
            if (!initialized) {
                value = next;
                initialized = true;
            } else if (value != next) {
                throw new IllegalStateException(
                    "benchmark returned changing checksums: expected " + value + ", got " + next);
            }
        }

        private int value() {
            if (!initialized) {
                throw new IllegalStateException("benchmark produced no samples");
            }
            return value;
        }
    }
}
