package tests.utils;

import de.peeeq.wurstscript.utils.LineOffsets;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class LineOffsetsTests {
    @Test
    public void resolvesPreviousOffsetsAndColumns() {
        LineOffsets offsets = new LineOffsets();
        offsets.set(1, 5);
        offsets.set(2, 10);

        assertEquals(offsets.get(0), -1);
        assertEquals(offsets.get(1), 5);
        assertEquals(offsets.get(3), 10);
        assertEquals(offsets.getLine(7), 2);
        assertEquals(offsets.getColumn(7), 2);
    }

    @Test
    public void growsForLargeLineNumbersAndClampsQueries() {
        LineOffsets offsets = new LineOffsets();
        offsets.set(256, 1000);

        assertEquals(offsets.get(256), 1000);
        assertEquals(offsets.get(10000), 1000);
        assertEquals(offsets.getLine(1000), 256);
    }
}
