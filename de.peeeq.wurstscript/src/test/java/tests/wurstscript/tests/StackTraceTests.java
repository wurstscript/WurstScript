package tests.wurstscript.tests;

import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imtranslation.StackTraceInjector2;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class StackTraceTests extends WurstScriptTest {
    @Test
    public void compactSourcePositions() {
        for (String file : new String[]{"UnitIndexer.wurst", "/project/UnitIndexer.wurst",
            "C:\\project\\UnitIndexer.wurst"}) {
            assertEquals(StackTraceInjector2.getCallPos(new WPos(file, null, 0, 0)),
                " (UnitIndexer:L0)");
        }
        assertEquals(StackTraceInjector2.getCallPos(new WPos("<generated>", null, 0, 0)), "");
    }

    @Test
    public void compactFramesOnBothBackends() throws IOException {
        test().testLua(true).luaOnly(false).stacktraces().compilationUnits(
            compilationUnit("Trace.wurst",
                "package Trace",
                "native println(string s)",
                "function getStackTraceString() returns string",
                "    return \"\"",
                "class Probe",
                "    @noinline function snapshot() returns string",
                "        return getStackTraceString()",
                "@noinline function caller(Probe p)",
                "    println(p.snapshot())",
                "init",
                "    caller(new Probe)"));
        for (String file : new String[]{"StackTraceTests_compactFramesOnBothBackends_stacktraceinlopt.j",
            "lua/StackTraceTests_compactFramesOnBothBackends.lua"}) {
            String output = Files.readString(Path.of(TEST_OUTPUT_PATH, file), StandardCharsets.UTF_8);
            assertTrue(output.contains("snapshot (Trace:L9)"), file);
            assertTrue(output.contains("caller (Trace:L11)"), file);
            assertFalse(output.contains("\"when calling "), file);
        }
    }
}
