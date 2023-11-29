package de.peeeq.wurstio.languageserver;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DebouncingTimer {
    final private Runnable action;
    final private ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
    private boolean isReady = false;
    private ScheduledFuture<?> fut;


    public DebouncingTimer(Runnable action) {
        this.action = action;
    }


    /** checks whether the timer is ready */
    public synchronized boolean isReady() {
        return isReady;
    }

    public synchronized void stop() {
        if (fut != null) {
            fut.cancel(true);
            fut = null;
        }
        isReady = false;
    }

    public synchronized void start(Duration d) {
        stop();
        fut = es.schedule(() -> {
            synchronized (DebouncingTimer.this) {
                isReady = true;
            }
            action.run();
        }, d.toMillis(), TimeUnit.MILLISECONDS);
    }





}
