package tests.wurstscript.tests;

import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wurstscript.testing.TestTimeoutWatchdog;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.testng.Assert.fail;

/** Subprocess-only fixtures; never include this class in the Gradle/TestNG suite. */
public class TimeoutWatchdogProbe {
    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.err.println("SHUTDOWN HOOK RAN")));
        TestNG testng = new TestNG();
        testng.setUseDefaultListeners(false);
        testng.addListener(new TestTimeoutWatchdog());
        testng.setTestClasses(new Class<?>[] {switch (args[0]) {
            case "test" -> StuckTest.class;
            case "configuration" -> StuckSetup.class;
            case "blocked-output" -> BlockedOutput.class;
            default -> Completed.class;
        }});
        testng.run();
        Thread.sleep(500); // A canceled deadline must remain canceled after its original expiry.
        System.exit(testng.hasFailure() ? 1 : 0);
    }

    private static void hangForever() {
        for (;;) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
            Thread.interrupted(); // Deliberately ignore interruption, as a modal dialog can.
        }
    }

    public static class StuckTest {
        @Test public void hang() { hangForever(); }
    }

    public static class StuckSetup {
        @BeforeMethod public void setup() { hangForever(); }
        @Test public void unreachable() { fail("stuck setup unexpectedly returned"); }
    }

    public static class BlockedOutput {
        @Test public void hang() throws Exception {
            var stream = TestTimeoutWatchdog.class.getDeclaredField("diagnostics");
            stream.setAccessible(true);
            synchronized (stream.get(null)) { hangForever(); }
        }
    }

    public static class Completed {
        @BeforeMethod public void setup() {}
        @Test public void first() {}
        @Test public void second() {}
    }
}
