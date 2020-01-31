package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstio.jassinterpreter.providers.TimerProvider;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.TimerMockHandler;

public class TimerMock {
    private AbstractInterpreter interpreter;
    private TimerMockHandler timerMockHandler;
    private final TimerProvider timerProvider;
    private IlConstHandle timerHandle;
    private TimerMockHandler.RunTask runTask;
    private TimerMockHandler.PausedTask pausedTask;

    public class TimerMockRunnable implements Runnable {

        private ILconstFuncRef handlerFunc;
        private boolean periodic;
        private float timeout;
        private boolean cancelled;

        public TimerMockRunnable(ILconstFuncRef handlerFunc, boolean periodic, float timeout) {
            this.handlerFunc = handlerFunc;
            this.periodic = periodic;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            if (cancelled) {
                return;
            }
            timerProvider.setLastExpiredMock(timerHandle);
            interpreter.runFuncRef(handlerFunc, null);
            if (periodic) {
                // run again:
                timerMockHandler.registerTimedAction(timeout, this);
            }
        }

        public void cancel() {
            this.cancelled = true;
        }

        public void resume() {
            this.cancelled = false;
        }
    }

    public TimerMock(AbstractInterpreter interpreter, TimerProvider timerProvider) {
        this.interpreter = interpreter;
        this.timerProvider = timerProvider;
        this.timerMockHandler = interpreter.getTimerMockHandler();
    }

    public void start(ILconstReal timeout, ILconstBool periodic, ILconstFuncRef handlerFunc) {
        if (runTask != null) {
            timerMockHandler.cancelTask(runTask);
        }
        pausedTask = null;
        TimerMockRunnable toRun = new TimerMockRunnable(handlerFunc, periodic.getVal(), timeout.getVal());
        this.runTask = timerMockHandler.registerTimedAction(timeout.getVal(), toRun);
    }

    public void destroy() {
        if (runTask != null) {
            timerMockHandler.cancelTask(runTask);
        }
        runTask = null;
        pausedTask = null;
        interpreter = null;
        timerMockHandler = null;
        timerHandle = null;
    }

    public void setHandle(IlConstHandle handle) {
        this.timerHandle = handle;
    }

    public void pause() {
        if (runTask == null) {
            throw new InterpreterException("Trying to pause a timer that was not started.");
        }
        pausedTask = timerMockHandler.pauseTask(runTask);
        runTask = null;
    }

    public void resume() {
        if (pausedTask == null) {
            throw new InterpreterException("Trying to resume a timer that was not paused.");
        }
        runTask = timerMockHandler.resumeTask(pausedTask);
        pausedTask = null;
    }
}
