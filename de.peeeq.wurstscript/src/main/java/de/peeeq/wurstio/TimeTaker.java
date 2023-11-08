package de.peeeq.wurstio;

import de.peeeq.wurstscript.utils.Utils;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
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

    class Recording implements TimeTaker {
        private int nesting = 0;
        private String currentPhaseDescription;
        private long currentPhaseStart;
        private Map<String, Long> accumulatedTimes = new LinkedHashMap<>();

        private Long startTime = 0L;

        public <T> T measure(String name, Supplier<T> f) {
            name = withNesting(name);
            nesting++;
            accumulatedTimes.putIfAbsent(name, 0L);
            long time = System.currentTimeMillis();
            T result = f.get();
            long duration = System.currentTimeMillis() - time;
            reportDuration(name, duration);
            nesting--;
            return result;
        }

        private String withNesting(String name) {
            return Utils.repeat(' ', nesting) + name;
        }

        private void reportDuration(String name, long duration) {
            accumulatedTimes.put(name, accumulatedTimes.getOrDefault(name, 0L) + duration);
        }


        @Override
        public void beginPhase(String description) {
            if (accumulatedTimes.isEmpty()) {
                this.startTime = System.currentTimeMillis();
            }
            if (currentPhaseDescription != null) {
                endPhase();
            }
            description = withNesting(description);
            nesting++;
            accumulatedTimes.putIfAbsent(description, 0L);
            currentPhaseDescription = description;
            currentPhaseStart = System.currentTimeMillis();
        }

        @Override
        public void endPhase() {
            if (currentPhaseDescription == null) {
                return;
            }
            long duration = System.currentTimeMillis() - currentPhaseStart;
            reportDuration(currentPhaseDescription, duration);
            nesting--;
            currentPhaseDescription = null;
        }

        @Override
        public void printReport() {
            System.out.println("#############################");
            System.out.println("Run times:");

            for (Map.Entry<String, Long> e : accumulatedTimes.entrySet()) {
                System.out.println(e.getKey() + ": " + e.getValue() + "ms");
            }

            System.out.println("Total runtime: " + (System.currentTimeMillis() - startTime) + "ms");
            System.out.println("GC time: " + getGarbageCollectionTime() + "ms");
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
