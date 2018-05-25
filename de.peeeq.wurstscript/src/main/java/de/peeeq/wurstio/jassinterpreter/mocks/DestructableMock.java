package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;

public class DestructableMock {
    public ILconstInt objectId;
    public ILconstReal x;
    public ILconstReal y;
    public ILconstReal face;
    public ILconstReal scale;
    public ILconstInt variation;

    public DestructableMock(ILconstInt objectId, ILconstReal x, ILconstReal y, ILconstReal face, ILconstReal scale, ILconstInt variation) {
        this.objectId = objectId;
        this.x = x;
        this.y = y;
        this.face = face;
        this.scale = scale;
        this.variation = variation;
    }
}
