package de.peeeq.wurstio;

import de.peeeq.wurstscript.CompileTimeInfo;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MainTests {

    @Test
    public void noArgumentsReturnsWithoutStartingInteractiveComponents() {
        String output = captureStdout(() -> Main.main(new String[0]));
        assertTrue(output.contains("Usage:"));
    }

    @Test
    public void versionPrintsCompilerVersion() {
        String output = captureStdout(() -> Main.main(new String[]{"-version"}));
        assertEquals(output.trim(), CompileTimeInfo.version);
    }

    private String captureStdout(Runnable action) {
        PrintStream previousOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            action.run();
        } finally {
            System.setOut(previousOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
