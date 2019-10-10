package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Iterator;

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
        return groupList.iterator().next();
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
        groupList.addAll((LinkedHashSet<IlConstHandle>) group.getObj());
        return ILconstInt.create(groupList.size());
    }

    public ILconstInt BlzGroupRemoveGroupFast(IlConstHandle group, IlConstHandle groupRm) {
        LinkedHashSet<IlConstHandle> groupList = (LinkedHashSet<IlConstHandle>) groupRm.getObj();
        groupList.removeAll((LinkedHashSet<IlConstHandle>) group.getObj());
        return ILconstInt.create(groupList.size());
    }	
}
