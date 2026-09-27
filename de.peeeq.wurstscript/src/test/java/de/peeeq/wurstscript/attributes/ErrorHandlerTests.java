package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import de.peeeq.wurstscript.parser.WPos;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class ErrorHandlerTests {
    @Test
    public void tracksErrorsWarningsAndPerFileBuckets() {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        ErrorHandler handler = new ErrorHandler(gui);
        CompileError error = new CompileError(new WPos("one.wurst", null, 1, 1), "bad");
        CompileError warning = new CompileError(new WPos("one.wurst", null, 2, 1), "careful",
            CompileError.ErrorType.WARNING);

        handler.sendError(error);
        handler.sendError(warning);

        assertEquals(handler.getErrorCount(), 1);
        assertEquals(handler.getErrors(), java.util.List.of(error));
        assertEquals(handler.getWarnings(), java.util.List.of(warning));
        assertEquals(handler.getBucketForFile("one.wurst", CompileError.ErrorType.ERROR), java.util.List.of(error));
        assertEquals(handler.getBucketForFile("one.wurst", CompileError.ErrorType.WARNING), java.util.List.of(warning));
        assertEquals(gui.getErrorList().size(), 1);

        handler.removeFromGlobal(error);
        handler.removeFromGlobal(warning);
        assertTrue(handler.getErrors().isEmpty());
        assertTrue(handler.getWarnings().isEmpty());
        assertEquals(handler.getBucketForFile("one.wurst", CompileError.ErrorType.ERROR), null);
        assertEquals(handler.getBucketForFile("one.wurst", CompileError.ErrorType.WARNING), null);
    }

    @Test
    public void exposesGuiAndUnitTestMode() {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        ErrorHandler handler = new ErrorHandler(gui);

        assertSame(handler.getGui(), gui);
        assertFalse(handler.isUnitTestMode());
        handler.enableUnitTestMode();
        assertTrue(handler.isUnitTestMode());
        assertFalse(handler.isOutputTestSource());
    }
}
