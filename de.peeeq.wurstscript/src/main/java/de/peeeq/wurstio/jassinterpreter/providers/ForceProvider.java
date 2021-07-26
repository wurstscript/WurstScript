package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.LinkedHashSet;

public class ForceProvider extends Provider {
    public ForceProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateForce() {
        return new IlConstHandle(NameProvider.getRandomName("force"), new LinkedHashSet<>());
    }

    public void ForceAddPlayer(IlConstHandle force, IlConstHandle player) {
        LinkedHashSet<IlConstHandle> forceList = (LinkedHashSet<IlConstHandle>) force.getObj();
        forceList.add(player);
    }

    public void ForceRemovePlayer(IlConstHandle force, IlConstHandle player) {
        LinkedHashSet<IlConstHandle> forceList = (LinkedHashSet<IlConstHandle>) force.getObj();
        forceList.remove(player);
    }

    public void ForceClear(IlConstHandle force) {
        LinkedHashSet<IlConstHandle> forceList = (LinkedHashSet<IlConstHandle>) force.getObj();
        forceList.clear();
    }

    public ILconstBool IsPlayerInForce(IlConstHandle player, IlConstHandle force) {
        LinkedHashSet<IlConstHandle> forceList = (LinkedHashSet<IlConstHandle>) force.getObj();
        return ILconstBool.instance(forceList.contains(player));
    }

    public void DestroyForce(IlConstHandle force) {
        ForceClear(force);
    }

    public void ForForce(IlConstHandle force, ILconstFuncRef funcRef) {
        // TODO take force param into account
        // For now this simply executes the supplied function.
        WLogger.trace("for force call");
        interpreter.runFuncRef(funcRef, null);
    }
}
