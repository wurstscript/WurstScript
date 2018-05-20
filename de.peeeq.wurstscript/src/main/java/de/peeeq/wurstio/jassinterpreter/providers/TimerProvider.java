package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.TimerMock;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class TimerProvider extends Provider {
    private static IlConstHandle lastExpiredMock = null;

    public TimerProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateTimer() {
        TimerMock mock = new TimerMock(interpreter);
        IlConstHandle timer = new IlConstHandle(NameProvider.getRandomName("timer"), mock);
        mock.setHandle(timer);
        return timer;
    }

    public void DestroyTimer(IlConstHandle timer) {
        TimerMock timerMock = (TimerMock) timer.getObj();
        timerMock.destroy();
    }

    public IlConstHandle GetExpiredTimer() {
        return lastExpiredMock;
    }

    public void TimerStart(IlConstHandle whichTimer, ILconstReal timeout, ILconstBool periodic, ILconstAbstract handlerFunc) {
        TimerMock timerMock = (TimerMock) whichTimer.getObj();
        if (handlerFunc instanceof ILconstFuncRef) {
            timerMock.start(timeout, periodic, (ILconstFuncRef) handlerFunc);
        }
    }

    public static void setLastExpiredMock(IlConstHandle lastExpiredMock) {
        TimerProvider.lastExpiredMock = lastExpiredMock;
    }
}
