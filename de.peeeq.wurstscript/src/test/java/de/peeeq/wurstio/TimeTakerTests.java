package de.peeeq.wurstio;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TimeTakerTests {

    /** The report's step lines, without their times, as "indent + name". */
    private static List<String> steps(TimeTaker.Recording timeTaker) {
        return Arrays.stream(timeTaker.report().split("\n"))
            .filter(line -> line.endsWith("ms") && !line.startsWith("Total") && !line.startsWith("GC")
                && !line.contains("(not measured)"))
            .map(line -> line.substring(0, line.lastIndexOf(':')))
            .collect(Collectors.toList());
    }

    /**
     * A phase inside a phase is a part of it: beginning the inner one used to end the outer one, whose later end was
     * then ignored, so the time after the inner phase was counted nowhere.
     */
    @Test
    public void aPhaseInsideAPhaseIsPartOfIt() {
        TimeTaker.Recording timeTaker = new TimeTaker.Recording();
        timeTaker.beginPhase("eliminate tuples");
        timeTaker.beginPhase("flatten");
        timeTaker.endPhase();
        timeTaker.beginPhase("kill tuples");
        timeTaker.endPhase();
        timeTaker.endPhase();
        timeTaker.beginPhase("translate");
        timeTaker.endPhase();

        assertEquals(steps(timeTaker), List.of("eliminate tuples", "  flatten", "  kill tuples", "translate"));
    }

    @Test
    public void aStepEndsThePhasesLeftOpenInItAndAStrayEndDoesNotEndTheStep() {
        TimeTaker.Recording timeTaker = new TimeTaker.Recording();
        timeTaker.measure("compile", () -> {
            timeTaker.beginPhase("left open");
            timeTaker.endPhase();
            timeTaker.endPhase(); // nothing open in the step: ignored
            timeTaker.beginPhase("left open too");
        });
        timeTaker.measure("print", () -> {
        });

        assertEquals(steps(timeTaker), List.of("compile", "  left open", "  left open too", "print"));
    }

    /** The parts of a level add up to the whole: what no step covers is shown as not measured. */
    @Test
    public void timeNoStepCoversIsShown() throws InterruptedException {
        TimeTaker.Recording timeTaker = new TimeTaker.Recording();
        timeTaker.measure("step", () -> {
        });
        Thread.sleep(20);

        String report = timeTaker.report();
        assertTrue(report.contains("(not measured): "), report);
        assertTrue(report.contains("Total runtime: "), report);
    }
}
