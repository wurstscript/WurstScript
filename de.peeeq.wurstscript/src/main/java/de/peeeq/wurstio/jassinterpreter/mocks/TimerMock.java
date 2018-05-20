package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstio.jassinterpreter.providers.TimerProvider;
import de.peeeq.wurstio.languageserver.requests.RunTests;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimerMock {
    private AbstractInterpreter interpreter;
    private ScheduledFuture<?> thread;
    private IlConstHandle timerHandle;

    public TimerMock(AbstractInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void start(ILconstReal timeout, ILconstBool periodic, ILconstFuncRef handlerFunc) {
        Runnable runnable = () -> {
            if (interpreter != null) {
                TimerProvider.setLastExpiredMock(timerHandle);
                interpreter.runFuncRef(handlerFunc, null);
                TimerProvider.setLastExpiredMock(timerHandle);
            }
        };
        if (periodic.getVal()) {
            thread = RunTests.getService().scheduleAtFixedRate(runnable, (long) timeout.getVal(), (long) timeout.getVal(), TimeUnit.SECONDS);
        } else {
            thread = RunTests.getService().schedule(runnable, (long) timeout.getVal(), TimeUnit.SECONDS);
        }
    }

    public void destroy() {
        if (thread != null) {
            thread.cancel(true);
            thread = null;
        }
        interpreter = null;
        timerHandle = null;
    }

    public void setHandle(IlConstHandle handle) {
        this.timerHandle = handle;
    }
}
