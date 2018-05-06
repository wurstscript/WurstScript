package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.EffectMock;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

public class EffectProvider extends Provider {
    public EffectProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle AddSpecialEffect(ILconstString modelName, ILconstReal x, ILconstReal y) {
        return new IlConstHandle(NameProvider.getRandomName("effect"), new EffectMock(modelName, x, y));
    }

    public void DestroyEffect(IlConstHandle effect) {
    }

}
