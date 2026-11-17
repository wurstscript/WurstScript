package de.peeeq.wurstio.languageserver.requests;

import net.moonlightflower.wc3libs.port.Orient;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BattleNetLaunchWindowsTest {
    @Test
    public void ignoresAnUnrelatedProcessBeforeTouchingWindows() throws Exception {
        if (!Orient.isWindowsSystem()) throw new SkipException("Window handoff uses Win32");
        Process helper = new ProcessBuilder(BattleNetLaunchWindows.command(ProcessHandle.current().pid()))
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();
        try {
            assertTrue(helper.waitFor(15, TimeUnit.SECONDS), "The helper should exit immediately for an unrelated process");
            assertEquals(helper.exitValue(), BattleNetLaunchWindows.GAME_EXITED);
        } finally {
            helper.destroyForcibly();
        }
    }
}
