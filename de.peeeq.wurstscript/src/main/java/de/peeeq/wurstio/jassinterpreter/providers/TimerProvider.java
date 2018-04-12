package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.TimerMock;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

public class TimerProvider extends Provider {
    private static IlConstHandle lastExpiredMock = null;

    public TimerProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateTimer() {
        return new IlConstHandle(NameProvider.getRandomName("timer"), new TimerMock());
    }

    public void DestroyTimer(IlConstHandle trigger) {
    }

    public IlConstHandle GetExpiredTimer() {
        return lastExpiredMock;
    }

    public void TimerStart(IlConstHandle whichTimer, ILconstReal timeout, ILconstBool periodic, ILconstAbstract handlerFunc) {
        TimerMock timerMock = (TimerMock) whichTimer.getObj();
        timerMock.start(timeout, periodic, handlerFunc);
    }
}
