package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;

public class ItemMock {
    public ILconstInt id;
    public ILconstReal x;
    public ILconstReal y;
    private ILconstBool visible;

    public ItemMock(ILconstInt id, ILconstReal x, ILconstReal y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public void setVisible(ILconstBool visible) {
        this.visible = visible;
    }
}
