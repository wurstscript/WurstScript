package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.UnitMock;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class GroupProvider extends Provider {

    private final ArrayDeque<IlConstHandle> enumUnitStack = new ArrayDeque<>();

    public GroupProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateGroup() {
        return new IlConstHandle(NameProvider.getRandomName("group"), new LinkedHashSet<>());
    }

    public void GroupClear(IlConstHandle group) {
        ((LinkedHashSet<IlConstHandle>) group.getObj()).clear();
    }

    public ILconstBool GroupAddUnit(IlConstHandle group, IlConstHandle unit) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        // Measured on the 3.0.0 client: adding null returns false.
        if (unit == null || groupList.contains(unit)) {
            return ILconstBool.FALSE;
        }
        addInCreationOrder(groupList, unit);
        return ILconstBool.TRUE;
    }

    /** Measured on the 3.0.0 client: a group keeps its units in creation order, not in the order they
     *  were added and not by handle id. */
    private static void addInCreationOrder(LinkedHashSet<IlConstHandle> groupList, IlConstHandle unit) {
        if (groupList.isEmpty() || serial(groupList.getLast()) < serial(unit)) {
            groupList.add(unit);
            return;
        }
        List<IlConstHandle> units = new ArrayList<>(groupList);
        units.add(unit);
        units.sort(Comparator.comparingLong(GroupProvider::serial));
        groupList.clear();
        groupList.addAll(units);
    }

    private static long serial(IlConstHandle unit) {
        return unit.getObj() instanceof UnitMock unitMock ? unitMock.serial : Long.MAX_VALUE;
    }

    public ILconstBool GroupRemoveUnit(IlConstHandle group, IlConstHandle unit) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        if (groupList.contains(unit)) {
            groupList.remove(unit);
            return ILconstBool.TRUE;
        }
        return ILconstBool.FALSE;
    }

    public ILconst FirstOfGroup(IlConstHandle group) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        if (groupList.isEmpty()) {
            WLogger.warning("Trying to get FoG of empty group");
            return ILconstNull.instance();
        }
        return groupList.iterator().next();
    }

    public void ForGroup(IlConstHandle group, ILconstFuncRef funcRef) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        groupList.forEach((IlConstHandle u) -> {
            enumUnitStack.push(u);
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

    public ILconstInt BlzGroupGetSize(IlConstHandle group) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        return ILconstInt.create(groupList.size());
    }

    public ILconst BlzGroupUnitAt(IlConstHandle group, ILconstInt index) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) group.getObj();
        ILconst elem = ILconstNull.instance();
        if(index.getVal() > 0 && index.getVal() < groupList.size()) {
            Iterator<IlConstHandle> it = groupList.iterator();
            elem = it.next();
            for(int i = 1; i <= index.getVal() && it.hasNext(); i++)
                elem = it.next();
            return elem;
        }
        else if(index.getVal() == 0)
            return FirstOfGroup(group);
        return elem;
    }

    public ILconstInt BlzGroupAddGroupFast(IlConstHandle group, IlConstHandle groupAdd) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) groupAdd.getObj();
        LinkedHashSet<IlConstHandle> addList = (LinkedHashSet<IlConstHandle>) group.getObj();
        int addCount = 0;
        for (IlConstHandle unit : addList) {
            if (!groupList.contains(unit)) {
                addInCreationOrder(groupList, unit);
                addCount++;
            }
        }
        return ILconstInt.create(addCount);
    }

    public ILconstInt BlzGroupRemoveGroupFast(IlConstHandle group, IlConstHandle groupRm) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) groupRm.getObj();
        int sizeBefore = groupList.size();
        groupList.removeAll((LinkedHashSet<IlConstHandle>) group.getObj());
        return ILconstInt.create(sizeBefore - groupList.size());
    }
}
