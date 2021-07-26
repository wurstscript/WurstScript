package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class DialogProvider extends Provider {
    public DialogProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle DialogCreate() {
        return new IlConstHandle(NameProvider.getRandomName("dialog"), null);
    }

    public void DialogDestroy(IlConstHandle dialog) {
    }

    public void DialogClear(IlConstHandle dialog) {
    }

    public void DialogSetMessage(IlConstHandle dialog, ILconstString text) {
    }

    public void DialogAddButton(IlConstHandle dialog, ILconstString buttonText, ILconstInt hotkey) {
    }

    public void DialogAddQuitButton(IlConstHandle dialog, ILconstBool doScoreScreen, ILconstString buttonText, ILconstInt hotkey) {
    }

    public void DialogDisplay(IlConstHandle player, IlConstHandle dialog, ILconstBool flag) {
    }

}
