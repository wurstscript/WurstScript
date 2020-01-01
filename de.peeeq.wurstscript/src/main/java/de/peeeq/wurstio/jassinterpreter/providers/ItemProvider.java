package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.ItemMock;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class ItemProvider extends Provider {
    public ItemProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateItem(ILconstInt itemid, ILconstReal x, ILconstReal y) {
        return new IlConstHandle(NameProvider.getRandomName("item"), new ItemMock(itemid, x, y));
    }

    public void RemoveItem(IlConstHandle item) {
    }

    public void SetItemVisible(IlConstHandle item, ILconstBool show) {
        ItemMock itemMock = (ItemMock) item.getObj();
        itemMock.setVisible(show);
    }
}
