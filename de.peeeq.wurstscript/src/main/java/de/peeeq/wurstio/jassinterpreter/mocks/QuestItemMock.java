package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstBool;

public class QuestItemMock {
    public ILconstBool completed;

    public QuestItemMock() {
        this.completed = ILconstBool.FALSE;
    }
}
