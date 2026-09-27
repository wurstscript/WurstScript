package de.peeeq.wurstio.gui;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class WurstErrorWindowTests {

    @Test
    public void syntheticSourceIsNotTreatedAsAFile() {
        assertTrue(WurstErrorWindow.isSyntheticSource("<source of NoExpr not found>"));
        assertTrue(WurstErrorWindow.isSyntheticSource(null));
        assertFalse(WurstErrorWindow.isSyntheticSource("wurst/Package.wurst"));
    }
}
