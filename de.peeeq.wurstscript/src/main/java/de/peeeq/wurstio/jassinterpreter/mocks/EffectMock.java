package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;

public class EffectMock {
    public ILconstString modelPath;
    public ILconstReal x;
    public ILconstReal y;

    public EffectMock(ILconstString modelPath, ILconstReal x, ILconstReal y) {
        this.modelPath = modelPath;
        this.x = x;
        this.y = y;
    }
}
