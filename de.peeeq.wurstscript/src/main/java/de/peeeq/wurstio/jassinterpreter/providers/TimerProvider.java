package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.TimerMock;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class TimerProvider extends Provider {
    private IlConstHandle lastExpiredMock = null;

    public TimerProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateTimer() {
        TimerMock mock = new TimerMock(interpreter, this);
        IlConstHandle timer = new IlConstHandle(NameProvider.getRandomName("timer"), mock);
        mock.setHandle(timer);
        return timer;
    }

    public ILconstReal TimerGetElapsed(IlConstHandle timer) {
        TimerMock timerMock = (TimerMock) timer.getObj();
        return new ILconstReal(timerMock.getElapsed());
    }

    public void DestroyTimer(IlConstHandle timer) {
        TimerMock timerMock = (TimerMock) timer.getObj();
        timerMock.destroy();
    }

    public void PauseTimer(IlConstHandle timer) {
        TimerMock timerMock = (TimerMock) timer.getObj();
        timerMock.pause();
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

    public void setLastExpiredMock(IlConstHandle lastExpiredMock) {
        this.lastExpiredMock = lastExpiredMock;
    }
}
