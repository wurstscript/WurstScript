package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class GroupProvider extends Provider {

    private ArrayDeque<IlConstHandle> enumUnitStack = new ArrayDeque<>();

    public GroupProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateGroup() {
        return new IlConstHandle(NameProvider.getRandomName("group"), new LinkedHashSet<>());
    }

    public void GroupClear(IlConstHandle group) {
        ((LinkedHashSet<IlConstHandle>) group.getObj()).clear();
    }

    public void GroupAddUnit(IlConstHandle group, IlConstHandle unit) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        groupList.add(unit);
    }

    public void GroupRemoveUnit(IlConstHandle group, IlConstHandle unit) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        groupList.remove(unit);
    }

    public ILconst FirstOfGroup(IlConstHandle group) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        if (groupList.isEmpty()) {
            WLogger.warning("Trying to get FoG of empty group");
            return ILconstNull.instance();
        }
        Iterator<IlConstHandle> iterator = groupList.iterator();
        IlConstHandle next = iterator.next();
        iterator.remove();
        return next;
    }

    public void ForGroup(IlConstHandle group, ILconstFuncRef funcRef) {
        WLogger.trace("for group call");
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        groupList.forEach((IlConstHandle u) -> {
            enumUnitStack.push(u);
            WLogger.trace("for group call itr: " + funcRef.getFuncName());
            interpreter.runFuncRef(funcRef, null);
            enumUnitStack.pop();
        });
    }

    public IlConstHandle GetEnumUnit() {
        if (enumUnitStack.size() == 0 || enumUnitStack.peek() == null) {
            WLogger.warning("Calling GetEnumUnit() outside of callback");
        }
        return enumUnitStack.peek();
    }

    public void DestroyGroup(IlConstHandle group) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        groupList.clear();
    }

    public ILconstBool IsUnitInGroup(IlConstHandle unit, IlConstHandle group) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        return ILconstBool.instance(groupList.contains(unit));
    }

}
