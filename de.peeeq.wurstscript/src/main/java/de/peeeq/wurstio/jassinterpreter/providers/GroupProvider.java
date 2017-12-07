package de.peeeq.wurstio.jassinterpreter.providers;

import com.google.common.collect.Sets;
import de.peeeq.wurstscript.intermediatelang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

import java.util.LinkedHashSet;

public class GroupProvider extends Provider {

    public GroupProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateGroup() {
        return new IlConstHandle(NameProvider.getRandomName("group"), Sets.newLinkedHashSet());
    }

    public void GroupClear(IlConstHandle group) {
        ((LinkedHashSet<IlConstHandle>)group.getObj()).clear();
    }

    public void GroupAddUnit(IlConstHandle group, IlConstHandle unit) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>)group.getObj();
        groupList.add(unit);
    }

    public void GroupRemoveUnit(IlConstHandle group, IlConstHandle unit) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>)group.getObj();
        groupList.remove(unit);
    }

    public IlConstHandle FirstOfGroup(IlConstHandle group) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>)group.getObj();
        IlConstHandle next = groupList.iterator().next();
        groupList.iterator().remove();
        return next;
    }

    public void ForGroup(IlConstHandle group, ILconstFuncRef funcRef) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>)group.getObj();
        groupList.forEach((IlConstHandle u) -> interpreter.executeFunction(funcRef.getFuncName(), null));
    }

}
