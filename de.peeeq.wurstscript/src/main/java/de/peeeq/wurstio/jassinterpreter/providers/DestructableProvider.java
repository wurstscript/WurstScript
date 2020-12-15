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
    }

    public ILconstReal GetDestructableX(IlConstHandle destructable) {
        return ((DestructableMock)destructable.getObj()).x;
    }

    public ILconstReal GetDestructableY(IlConstHandle destructable) {
        return ((DestructableMock)destructable.getObj()).y;
    }
}
