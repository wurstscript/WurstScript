package de.peeeq.wurstio;

import de.peeeq.wurstscript.utils.CompilationCancellation;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 */
public interface TimeTaker {
    default void measure(String name, Runnable f) {
        measure(name, () -> {
            f.run();
            return null;
        });
    }

    <T> T measure(String name, Supplier<T> f);

    void beginPhase(String description);

    void endPhase();

    void printReport();


    class Default implements TimeTaker {

        @Override
        public <T> T measure(String name, Supplier<T> f) {
            CompilationCancellation.check();
            return f.get();
        }

        @Override
        public void beginPhase(String description) {
        }

        @Override
        public void endPhase() {
        }

        @Override
        public void printReport() {

        }
    }

    /**
     * Records the time of each measured step and phase, nested as they run. A phase begun inside another phase
     * or step is a part of it, and {@link #endPhase()} ends the innermost open phase; a step ends the phases left
     * open inside it. The report shows each step under the one it ran in, and what the steps of a level do not
     * account for, so that the parts add up to the total, which runs from the creation of the recorder.
     */
    class Recording implements TimeTaker {
        private final long startTime = System.nanoTime();
        /** Accumulated nanoseconds per step, by its path of enclosing steps; in the order the steps first began. */
        private final Map<List<String>, Long> accumulatedTimes = new LinkedHashMap<>();
        private final Deque<Open> open = new ArrayDeque<>();

        private record Open(List<String> path, long start, boolean phase) {
        }

        @Override
        public <T> T measure(String name, Supplier<T> f) {
            CompilationCancellation.check();
            Open step = begin(name, false);
            try {
                return f.get();
            } finally {
                while (!open.isEmpty() && open.peek() != step) {
                    end();
                }
                if (!open.isEmpty()) {
                    end();
                }
            }
        }

        @Override
        public void beginPhase(String description) {
            begin(description, true);
        }

        @Override
        public void endPhase() {
            // an end without a phase open in the current step is ignored: it must not end the step
            if (!open.isEmpty() && open.peek().phase()) {
                end();
            }
        }

        private Open begin(String name, boolean phase) {
            List<String> path = new ArrayList<>(open.isEmpty() ? List.of() : open.peek().path());
            path.add(name);
            List<String> key = List.copyOf(path);
            accumulatedTimes.putIfAbsent(key, 0L);
            Open step = new Open(key, System.nanoTime(), phase);
            open.push(step);
            return step;
        }

        private void end() {
            Open step = open.pop();
            accumulatedTimes.merge(step.path(), System.nanoTime() - step.start(), Long::sum);
        }

        /** The report, with the phases still open ended now. */
        public String report() {
            while (!open.isEmpty()) {
                end();
            }
            long total = System.nanoTime() - startTime;
            StringBuilder sb = new StringBuilder();
            sb.append("#############################\n");
            sb.append("Run times:\n");
            appendLevel(sb, List.of(), total);
            sb.append("Total runtime: ").append(ms(total)).append("ms\n");
            sb.append("GC time: ").append(getGarbageCollectionTime()).append("ms\n");
            return sb.toString();
        }

        private void appendLevel(StringBuilder sb, List<String> parent, long parentTime) {
            long accounted = 0;
            boolean any = false;
            for (Map.Entry<List<String>, Long> e : accumulatedTimes.entrySet()) {
                List<String> path = e.getKey();
                if (path.size() != parent.size() + 1 || !path.subList(0, parent.size()).equals(parent)) {
                    continue;
                }
                any = true;
                accounted += e.getValue();
                sb.append(" ".repeat(2 * parent.size())).append(path.get(path.size() - 1)).append(": ")
                    .append(ms(e.getValue())).append("ms\n");
                appendLevel(sb, path, e.getValue());
            }
            if (any && parentTime - accounted >= 1_000_000L) {
                sb.append(" ".repeat(2 * parent.size())).append("(not measured): ")
                    .append(ms(parentTime - accounted)).append("ms\n");
            }
        }

        private static long ms(long nanos) {
            return nanos / 1_000_000L;
        }

        @Override
        public void printReport() {
            System.out.print(report());
        }

        private static long getGarbageCollectionTime() {
            long collectionTime = 0;
            for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                collectionTime += garbageCollectorMXBean.getCollectionTime();
            }
            return collectionTime;
        }

    }
}
