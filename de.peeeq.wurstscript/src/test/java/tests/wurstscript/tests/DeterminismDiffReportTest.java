package tests.wurstscript.tests;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Covers the summary the determinism test prints when it fails.
 * <p>
 * The failure it describes has happened once and is not reproducible on demand, so the summary is
 * the only thing that will be read when it happens again. That makes it worth testing on its own:
 * a description which misreports what changed is worse than none, because it sends the next person
 * looking in the wrong place.
 */
public class DeterminismDiffReportTest {

    /**
     * A block appearing in one script and not the other reads as one addition. Comparing equal
     * indexes instead would report every line after the insertion as differing, which is the case
     * emission-order nondeterminism actually produces.
     */
    @Test
    public void anInsertedBlockIsOneAdditionRatherThanEverythingAfterIt() {
        String first = "a\nb\nc\nd\ne\nf\ng\nh\n";
        String second = "a\nb\nINSERTED\nc\nd\ne\nf\ng\nh\n";

        String report = LuaTranslationTests.describeFirstDifferences(first, second);

        assertTrue(report.contains("0 line(s) only in the first, 1 only in the second"),
            "one inserted line should read as one addition:\n" + report);
        assertTrue(report.contains("INSERTED"), "the added line should be named:\n" + report);
        assertTrue(report.contains("line 3"), "the added line's number should be given:\n" + report);
    }

    /** A block moved rather than inserted reads as one removal and one addition, not a cascade. */
    @Test
    public void aMovedLineIsOneRemovalAndOneAddition() {
        String first = "one\nmoved\ntwo\nthree\nfour\n";
        String second = "one\ntwo\nthree\nmoved\nfour\n";

        String report = LuaTranslationTests.describeFirstDifferences(first, second);

        assertTrue(report.contains("1 line(s) only in the first, 1 only in the second"),
            "a moved line should read as one removal and one addition:\n" + report);
    }

    /** Replacing a line in place is a removal and an addition at the same position. */
    @Test
    public void aReplacedLineNamesBothVersions() {
        String first = "x\nbefore\nz\n";
        String second = "x\nafter\nz\n";

        String report = LuaTranslationTests.describeFirstDifferences(first, second);

        assertTrue(report.contains("before"), "the first version should be named:\n" + report);
        assertTrue(report.contains("after"), "the second version should be named:\n" + report);
    }

    /** Identical scripts differing only in trailing bytes say so rather than listing nothing. */
    @Test
    public void identicalLinesReportTheTrailingByteCase() {
        String report = LuaTranslationTests.describeFirstDifferences("a\nb\n", "a\nb\n");

        assertTrue(report.contains("no line does"), "expected the trailing byte wording:\n" + report);
    }
}
