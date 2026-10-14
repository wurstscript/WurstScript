package org.wurstscript.testing;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.Comparator;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Bounds test and configuration methods without moving them to a different thread. */
public final class TestTimeoutWatchdog implements IInvokedMethodListener {
    // Gradle captures System.err per test and can lose it when a worker is forcibly terminated.
    // The process pipe remains visible even with showStandardStreams=false / --quiet.
    private static final PrintStream diagnostics =
        new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
    private final long timeoutMillis = readTimeoutMillis();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "test-timeout-watchdog");
        thread.setDaemon(true);
        return thread;
    });
    private ScheduledFuture<?> pending;
    private Object active;

    private static long readTimeoutMillis() {
        String value = System.getProperty("wurst.test.timeoutMillis", "180000");
        String message = "wurst.test.timeoutMillis must be a positive whole number of milliseconds: " + value;
        try {
            long timeout = Long.parseLong(value);
            if (timeout <= 0) throw new IllegalArgumentException(message);
            return timeout;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(message, error);
        }
    }

    @Override
    public synchronized void beforeInvocation(IInvokedMethod method, ITestResult result) {
        cancel();
        Object invocation = new Object();
        active = invocation;
        String name = method.getTestMethod().getQualifiedName();
        pending = timer.schedule(() -> expire(invocation, name), timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void afterInvocation(IInvokedMethod method, ITestResult result) {
        cancel();
    }

    private void cancel() {
        active = null;
        if (pending != null) pending.cancel(false);
        pending = null;
    }

    private synchronized void expire(Object invocation, String name) {
        if (active != invocation) return;
        // Printing can itself block behind the stuck test's stream lock. Keep the hard exit
        // independent of diagnostics, allowing at most two seconds to print the stacks.
        Thread hardExit = new Thread(() -> {
            java.util.concurrent.locks.LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(2));
            Runtime.getRuntime().halt(124);
        }, "test-timeout-hard-exit");
        hardExit.setDaemon(true);
        hardExit.start();
        diagnostics.println("TEST TIMEOUT: " + name + " exceeded " + timeoutMillis
            + "ms; terminating this test worker (exit 124). Thread dump:");
        Thread.getAllStackTraces().entrySet().stream()
            .sorted(Comparator.comparing(entry -> entry.getKey().getName()))
            .forEach(entry -> {
                diagnostics.println("\"" + entry.getKey().getName() + "\" " + entry.getKey().getState());
                for (StackTraceElement frame : entry.getValue()) diagnostics.println("    at " + frame);
            });
        diagnostics.flush();
        // Shutdown hooks and an interrupted modal dialog can also hang. Gradle reports the
        // nonzero worker exit as a failure; the enclosing suite/job have independent deadlines.
        Runtime.getRuntime().halt(124);
    }
}
