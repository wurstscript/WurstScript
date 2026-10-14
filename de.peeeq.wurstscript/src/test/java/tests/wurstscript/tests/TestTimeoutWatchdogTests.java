package tests.wurstscript.tests;

import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wurstscript.testing.TestTimeoutWatchdog;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class TestTimeoutWatchdogTests {
    @DataProvider
    public Object[][] hangs() {
        return new Object[][] {{"test", "StuckTest.hang"}, {"configuration", "StuckSetup.setup"},
            {"blocked-output", null}};
    }

    @Test(dataProvider = "hangs")
    public void killsUninterruptibleWork(String mode, String method) throws Exception {
        Result result = probe(mode);
        assertEquals(result.exit, 124, result.output);
        if (method != null) {
            assertTrue(result.output.contains("TEST TIMEOUT:"), result.output);
            assertTrue(result.output.contains(method), result.output);
            assertTrue(result.output.contains("Thread dump:"), result.output);
        }
        assertFalse(result.output.contains("SHUTDOWN HOOK RAN"), result.output);
    }

    @Test
    public void completedInvocationsCancelTheirDeadline() throws Exception {
        Result result = probe("completed");
        assertEquals(result.exit, 0, result.output);
        assertFalse(result.output.contains("TEST TIMEOUT:"), result.output);
    }

    private static Result probe(String mode) throws Exception {
        Set<String> classpath = new LinkedHashSet<>();
        for (Class<?> dependency : new Class<?>[] {TestTimeoutWatchdogTests.class,
            TestTimeoutWatchdog.class, TestNG.class, org.slf4j.LoggerFactory.class,
            com.beust.jcommander.JCommander.class}) {
            classpath.add(Path.of(dependency.getProtectionDomain().getCodeSource().getLocation().toURI()).toString());
        }
        Path output = Files.createTempFile("test-timeout-probe-", ".log");
        Process process = new ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-Dwurst.test.timeoutMillis=250", "-cp", String.join(File.pathSeparator, classpath),
            TimeoutWatchdogProbe.class.getName(), mode)
            .redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "timeout probe did not exit: " + mode);
            return new Result(process.exitValue(), Files.readString(output));
        } finally {
            process.destroyForcibly();
            Files.deleteIfExists(output);
        }
    }

    private record Result(int exit, String output) {}

}
