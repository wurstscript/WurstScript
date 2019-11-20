package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstio.jassinterpreter.mocks.TimerMock;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 */
public class TimerMockHandler {
    private float virtualTime = 0;
    private PriorityQueue<RunTask> nextRunnable = new PriorityQueue<>(Comparator.comparing(r -> r.time));

    public void cancelTask(RunTask runTask) {
        nextRunnable.remove(runTask);
    }

    public static class PausedTask {
        private final float remainingTime;
        private final Runnable runnable;

        private PausedTask(float remainingTime, Runnable runnable) {
            this.remainingTime = remainingTime;
            this.runnable = runnable;
        }
    }


    public static class RunTask {
        private final float time;
        private final Runnable runnable;

        private RunTask(float time, Runnable runnable) {
            this.time = time;
            this.runnable = runnable;
        }
    }

    public RunTask registerTimedAction(float timeOut, Runnable toRun) {
        RunTask t = new RunTask(virtualTime + timeOut, toRun);
        nextRunnable.add(t);
        return t;
    }

    public PausedTask pauseTask(RunTask t) {
        TimerMock.TimerMockRunnable runnable = (TimerMock.TimerMockRunnable) t.runnable;
        runnable.cancel();
        boolean removed = nextRunnable.remove(t);
        if (!removed) {
            return null;
        }
        return new PausedTask(t.time - virtualTime, t.runnable);
    }

    public RunTask resumeTask(PausedTask t) {
        TimerMock.TimerMockRunnable runnable = (TimerMock.TimerMockRunnable) t.runnable;
        runnable.resume();
        return registerTimedAction(virtualTime + t.remainingTime, t.runnable);
    }

    public void completeTimers() {
        while (!nextRunnable.isEmpty()) {
            RunTask r = nextRunnable.remove();
            virtualTime = Math.max(virtualTime, r.time);
            r.runnable.run();
        }
    }


}
