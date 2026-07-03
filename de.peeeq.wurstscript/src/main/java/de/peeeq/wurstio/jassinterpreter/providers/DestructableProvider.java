package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.DestructableMock;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class DestructableProvider extends Provider {
    public DestructableProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateDestructable(ILconstInt objectId, ILconstReal x, ILconstReal y, ILconstReal face, ILconstReal scale, ILconstInt variation) {
        return new IlConstHandle(NameProvider.getRandomName("destructable"), new DestructableMock(objectId, x, y, face, scale, variation));
    }

    public void RemoveDestructable(IlConstHandle destructable) {
    }

    public void KillDestructable(IlConstHandle destructable) {
        DestructableMock destructableMock = destructableOrNull(destructable);
        if (destructableMock != null) {
            destructableMock.life = ILconstReal.create(0);
        }
    }

    public void SetDestructableLife(IlConstHandle destructable, ILconstReal life) {
        DestructableMock destructableMock = destructableOrNull(destructable);
        if (destructableMock != null) {
            destructableMock.life = life;
        }
    }

    public ILconstReal GetDestructableLife(IlConstHandle destructable) {
        DestructableMock destructableMock = destructableOrNull(destructable);
        return destructableMock == null ? ILconstReal.create(0) : destructableMock.life;
    }

    public ILconstReal GetDestructableX(IlConstHandle destructable) {
        return ((DestructableMock)destructable.getObj()).x;
    }

    public ILconstReal GetDestructableY(IlConstHandle destructable) {
        return ((DestructableMock)destructable.getObj()).y;
    }

    private DestructableMock destructableOrNull(IlConstHandle destructable) {
        if (destructable == null || !(destructable.getObj() instanceof DestructableMock)) {
            return null;
        }
        return (DestructableMock) destructable.getObj();
    }
}
