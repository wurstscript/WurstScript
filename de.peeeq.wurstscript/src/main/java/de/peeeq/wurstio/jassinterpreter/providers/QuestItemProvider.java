package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.QuestItemMock;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class QuestItemProvider extends Provider {
    public QuestItemProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle QuestCreateItem(IlConstHandle quest) {
        return new IlConstHandle(NameProvider.getRandomName("questitem"), new QuestItemMock());
    }

    public void QuestItemSetDescription(IlConstHandle questItem, ILconstString description) {
    }

    public ILconstBool IsQuestItemCompleted(IlConstHandle questItem) {
        return ((QuestItemMock) questItem.getObj()).completed;
    }

    public void QuestItemSetCompleted(IlConstHandle questItem, ILconstBool completed) {
        ((QuestItemMock) questItem.getObj()).completed = completed;
    }
}
