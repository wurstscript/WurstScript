import jdk.jfr.consumer.*;
import java.nio.file.Path;
import java.util.*;

public class RunMapProfileReport {
    static String method(RecordedFrame frame) {
        return frame.getMethod().getType().getName() + "." + frame.getMethod().getName();
    }
    static String phase(List<RecordedFrame> frames) {
        List<String> names = frames.stream().map(RunMapProfileReport::method).toList();
        for (String name : names) {
            if (name.endsWith("WurstValidator.lightValidation")) return "check-light";
            if (name.endsWith("WurstValidator.heavyValidation")) return "check-heavy";
            if (name.endsWith("WurstValidator.postChecks")) return "check-post";
        }
        for (String name : names) {
            if (name.contains("WurstChecker.checkProg")) return "check-setup";
            if (name.contains("CompiletimeFunctionRunner.")) return "compiletime";
            if (name.contains(".imoptimizer.")) return "optimizer";
            if (name.contains("EliminateGenerics.")) return "generics";
            if (name.contains("ImTranslator.translateProg")) return "ast-to-im";
            if (name.contains(".lua.translation.") || name.contains(".translation.lua.")) return "lua-backend";
            if (name.contains(".translation.imtranslation.")) return "im-lowering";
            if (name.contains(".antlr.") || name.contains("WurstParser.parse") || name.contains("AntlrTokenPipeline.run")) return "parse";
            if (name.contains("JMpqEditor.") || name.contains("Jmpq3MpqEditor.") || name.contains(".compression.")) return "map-io";
        }
        return "other";
    }
    static void top(String label, Map<String, Long> counts, int limit) {
        counts.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed())
            .limit(limit).forEach(e -> System.out.println(label + " " + e.getValue() + " " + e.getKey()));
    }
    public static void main(String[] args) throws Exception {
        Map<String,Long> phases = new TreeMap<>(), inclusive = new HashMap<>(), leaves = new HashMap<>();
        Map<String,Map<String,Long>> perPhase = new TreeMap<>();
        Map<String,Long> allocPhases = new TreeMap<>(), allocClasses = new HashMap<>(), allocFrames = new HashMap<>();
        Map<String,Long> nameLinkSites = new TreeMap<>(), resolutionSites = new TreeMap<>();
        Map<String,Long> allocationSites = new HashMap<>();
        long samples = 0, mainSamples = 0, gcNanos = 0, allocationSamples = 0;
        try (RecordingFile file = new RecordingFile(Path.of(args[0]))) {
            while (file.hasMoreEvents()) {
                RecordedEvent event = file.readEvent();
                String type = event.getEventType().getName();
                if (type.equals("jdk.GCPhasePause")) gcNanos += event.getDuration().toNanos();
                var trace = event.getStackTrace();
                if (trace == null || trace.getFrames().isEmpty()) continue;
                var frames = trace.getFrames();
                String phase = phase(frames);
                if (type.equals("jdk.ExecutionSample")) {
                    samples++;
                    if (event.getThread("sampledThread").getJavaName().equals("main")) mainSamples++;
                    phases.merge(phase, 1L, Long::sum);
                    leaves.merge(method(frames.getFirst()), 1L, Long::sum);
                    Set<String> seen = new HashSet<>();
                    for (RecordedFrame frame : frames) {
                        String name = method(frame);
                        if (seen.add(name)) {
                            inclusive.merge(name, 1L, Long::sum);
                            perPhase.computeIfAbsent(phase, ignored -> new HashMap<>()).merge(name, 1L, Long::sum);
                        }
                        if (name.endsWith("NameLinks.calculate")) nameLinkSites.merge(name + ":" + frame.getLineNumber(), 1L, Long::sum);
                        if (name.contains("NameResolution.")) resolutionSites.merge(name + ":" + frame.getLineNumber(), 1L, Long::sum);
                    }
                } else if (type.equals("jdk.ObjectAllocationSample")) {
                    allocationSamples++;
                    long weight = event.getLong("weight");
                    allocPhases.merge(phase, weight, Long::sum);
                    allocClasses.merge(event.getClass("objectClass").getName(), weight, Long::sum);
                    Set<String> seen = new HashSet<>();
                    boolean ownSite = false;
                    for (RecordedFrame frame : frames) {
                        String name = method(frame);
                        if (seen.add(name)) allocFrames.merge(name, weight, Long::sum);
                        if (!ownSite && name.startsWith("de.peeeq.")) {
                            ownSite = true;
                            allocationSites.merge(phase + " " + name + ":" + frame.getLineNumber(), weight, Long::sum);
                        }
                    }
                }
            }
        }
        System.out.println("SUMMARY samples=" + samples + " main=" + mainSamples + " gcPauseMs=" + gcNanos/1_000_000.0
            + " allocationSamples=" + allocationSamples);
        System.out.println("PHASES " + phases);
        top("INCLUSIVE", inclusive, 60);
        top("LEAF", leaves, 40);
        for (var entry : perPhase.entrySet()) top("PHASE " + entry.getKey(), entry.getValue(), 30);
        top("NAMELINK_SITE", nameLinkSites, 30);
        top("RESOLUTION_SITE", resolutionSites, 35);
        System.out.println("ALLOCATION_PHASE_BYTES " + allocPhases);
        top("ALLOC_CLASS_BYTES", allocClasses, 20);
        top("ALLOC_INCLUSIVE_BYTES", allocFrames, 35);
        top("ALLOC_SITE_BYTES", allocationSites, 50);
        for (var entry : perPhase.entrySet()) {
            Map<String,Long> focused = new HashMap<>();
            for (var count : entry.getValue().entrySet()) {
                String name = count.getKey();
                if (name.startsWith("de.peeeq.") && !name.contains(".languageserver.")
                    && !name.contains("WurstCompilerJassImpl.") && !name.contains("TimeTaker")
                    && !name.contains("CompilationCancellation.") && !name.contains("WurstChecker.")
                    && !name.endsWith("WurstValidator.validate") && !name.endsWith("WurstValidator.check")
                    && !name.endsWith("WurstValidator.walkTree") && !name.endsWith("WurstValidator.lightValidation")) {
                    focused.put(name, count.getValue());
                }
            }
            top("FOCUSED " + entry.getKey(), focused, 40);
        }
    }
}
