package tests.wurstscript.tests;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
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

    /**
     * Two scripts whose lines all match but which are not equal differ in how the lines end. The
     * caller only asks after finding them unequal, so passing identical strings would test a state
     * production never reaches — this passes CRLF against LF, which it can.
     */
    @Test
    public void differingOnlyInLineEndingsSaysSoRatherThanListingEveryLine() {
        String crlf = "alpha\r\nbeta\r\ngamma\r\n";
        String lf = "alpha\nbeta\ngamma\n";
        assertNotEquals(crlf, lf, "the two inputs must be unequal for this to mean anything");

        String report = LuaTranslationTests.describeFirstDifferences(crlf, lf);

        assertTrue(report.contains("line terminators"),
            "line endings should be named rather than every line reported as changed:\n" + report);
        assertFalse(report.contains("only in the first"),
            "no line should be reported as removed:\n" + report);
    }

    /** Bytes after the last line reach the same branch: every line matches, the scripts do not. */
    @Test
    public void trailingBytesReachTheSameCase() {
        String report = LuaTranslationTests.describeFirstDifferences("a\nb\n", "a\nb");

        assertTrue(report.contains("line terminators") || report.contains("only in the"),
            "a trailing difference should be described one way or the other:\n" + report);
    }
}
