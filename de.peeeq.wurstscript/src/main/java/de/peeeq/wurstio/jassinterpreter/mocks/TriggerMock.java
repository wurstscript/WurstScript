package de.peeeq.wurstio.jassinterpreter.mocks;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

import java.util.ArrayList;

public class TriggerMock {

    private ArrayList<IlConstHandle> conditions = Lists.newArrayList();
    private ArrayList<ILconstFuncRef> actions = Lists.newArrayList();

    public TriggerMock() {
    }

    public void addCondition(IlConstHandle boolexpr) {
        conditions.add(boolexpr);
    }

    public ILconstBool evaluate(ILInterpreter interpreter) {
        if (interpreter != null) {
            conditions.forEach(cond -> {
                interpreter.runVoidFunc(((ILconstFuncRef) cond.getObj()).getFunc(), null);
            });
            actions.forEach(funcRef -> {
                interpreter.runVoidFunc(funcRef.getFunc(), null);
            });
        }

        return ILconstBool.instance(true);
    }

    public void addAction(ILconstFuncRef code) {
        actions.add(code);
    }
}
