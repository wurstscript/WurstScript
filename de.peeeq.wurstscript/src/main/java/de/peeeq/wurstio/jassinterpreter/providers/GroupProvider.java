package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstFuncRef;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class GroupProvider extends Provider {

    private ArrayDeque<IlConstHandle> enumUnitStack = new ArrayDeque<>();

    public GroupProvider(ILInterpreter interpreter) {
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
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        groupList.forEach((IlConstHandle u) -> {
            enumUnitStack.push(u);
            interpreter.executeFunction(funcRef.getFuncName(), null);
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
    }

}
