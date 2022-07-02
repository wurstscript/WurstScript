package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.QuestMock;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class QuestProvider extends Provider {
    public QuestProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateQuest() {
        return new IlConstHandle(NameProvider.getRandomName("quest"), new QuestMock());
    }

    public void DestroyQuest(IlConstHandle quest) {
    }

    public void QuestSetTitle(IlConstHandle quest, ILconstString title) {
    }

    public void QuestSetIconPath(IlConstHandle quest, ILconstString title) {
    }

    public void QuestSetDescription(IlConstHandle quest, ILconstString description) {
    }

    public ILconstBool IsQuestCompleted(IlConstHandle quest) {
        return ((QuestMock) quest.getObj()).completed;
    }

    public ILconstBool IsQuestDiscovered(IlConstHandle quest) {
        return ((QuestMock) quest.getObj()).discovered;
    }

    public ILconstBool IsQuestEnabled(IlConstHandle quest) {
        return ((QuestMock) quest.getObj()).enabled;
    }

    public ILconstBool IsQuestFailed(IlConstHandle quest) {
        return ((QuestMock) quest.getObj()).failed;
    }

    public ILconstBool IsQuestRequired(IlConstHandle quest) {
        return ((QuestMock) quest.getObj()).required;
    }

    public void QuestSetCompleted(IlConstHandle quest, ILconstBool completed) {
        ((QuestMock) quest.getObj()).completed = completed;
    }

    public void QuestSetDiscovered(IlConstHandle quest, ILconstBool discovered) {
        ((QuestMock) quest.getObj()).discovered = discovered;
    }

    public void QuestSetEnabled(IlConstHandle quest, ILconstBool enabled) {
        ((QuestMock) quest.getObj()).enabled = enabled;
    }

    public void QuestSetFailed(IlConstHandle quest, ILconstBool failed) {
        ((QuestMock) quest.getObj()).failed = failed;
    }

    public void QuestSetRequired(IlConstHandle quest, ILconstBool required) {
        ((QuestMock) quest.getObj()).required = required;
    }
}
