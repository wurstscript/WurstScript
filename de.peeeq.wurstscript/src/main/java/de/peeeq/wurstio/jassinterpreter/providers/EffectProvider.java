package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.EffectMock;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class EffectProvider extends Provider {
    public EffectProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle AddSpecialEffect(ILconstString modelName, ILconstReal x, ILconstReal y) {
        return new IlConstHandle(NameProvider.getRandomName("effect"), new EffectMock(modelName, x, y));
    }

    public void DestroyEffect(IlConstHandle effect) {
    }

    public ILconstReal BlzGetLocalSpecialEffectX(IlConstHandle effect) {
        return ((EffectMock)effect.getObj()).x;
    }

    public ILconstReal BlzGetLocalSpecialEffectY(IlConstHandle effect) {
        return ((EffectMock)effect.getObj()).y;
    }
    public ILconstReal BlzGetLocalSpecialEffectZ(IlConstHandle effect) {
        return ((EffectMock)effect.getObj()).z;
    }


}
