package tests.wurstscript.tests;

import de.peeeq.wurstscript.WurstParser;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGuiCliImpl;
import org.testng.annotations.Test;

import java.io.StringReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class WurstParserTests {
    @Test
    public void parsesWurstJassAndJurstCompilationUnits() {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        ErrorHandler errors = new ErrorHandler(gui);
        WurstParser parser = new WurstParser(errors, gui);
        String jassFunction = "function foo takes nothing returns nothing\nendfunction\n";

        assertNotNull(parser.parse(new StringReader("package Demo\n"), "demo.wurst", false));
        assertNotNull(parser.parseJass(new StringReader(jassFunction), "demo.j", false));
        assertNotNull(parser.parseJurst(new StringReader(jassFunction), "demo.jurst", false));
        assertEquals(errors.getErrorCount(), 0);
    }

    @Test
    public void canLeaveSyntacticSugarForDownstreamInspection() {
        WurstGuiCliImpl gui = new WurstGuiCliImpl();
        ErrorHandler errors = new ErrorHandler(gui);
        WurstParser parser = new WurstParser(errors, gui);
        parser.setRemoveSugar(false);

        assertNotNull(parser.parse(new StringReader("package Demo\nfunction foo()\n"), "demo.wurst", false));
        assertEquals(errors.getErrorCount(), 0);
    }
}
