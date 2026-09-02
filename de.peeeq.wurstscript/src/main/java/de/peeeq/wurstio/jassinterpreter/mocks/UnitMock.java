package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UnitMock {
    public IlConstHandle owner;
    public ILconstInt unitid;
    public ILconstReal x;
    public ILconstReal y;
    public ILconstReal face;
    public boolean removed;
    public boolean hidden;
    public boolean paused;
    public boolean invulnerable;
    public boolean pathing = true;
    public boolean useFood = true;
    public boolean selected;
    public boolean suspendedXp;
    public boolean sleeping;
    public boolean canSleep = true;
    public boolean canSleepPerm = true;
    public ILconstReal acquireRange = ILconstReal.create(0);
    public ILconstReal defaultAcquireRange = ILconstReal.create(0);
    public ILconstReal waygateX = ILconstReal.create(0);
    public ILconstReal waygateY = ILconstReal.create(0);
    public boolean waygateActive;
    public ILconstReal moveSpeed = ILconstReal.create(0);
    public ILconstReal defaultMoveSpeed = ILconstReal.create(0);
    public ILconstReal flyHeight = ILconstReal.create(0);
    public ILconstReal defaultFlyHeight = ILconstReal.create(0);
    public ILconstReal turnSpeed = ILconstReal.create(0);
    public ILconstReal defaultTurnSpeed = ILconstReal.create(0);
    public ILconstReal propWindow = ILconstReal.create(0);
    public ILconstReal defaultPropWindow = ILconstReal.create(0);
    public ILconstInt level = ILconstInt.create(1);
    public ILconstInt heroXp = ILconstInt.create(0);
    public ILconstInt heroStr = ILconstInt.create(0);
    public ILconstInt heroAgi = ILconstInt.create(0);
    public ILconstInt heroInt = ILconstInt.create(0);
    public ILconstInt skillPoints = ILconstInt.create(0);
    public ILconstInt resourceAmount = ILconstInt.create(0);
    public final HashMap<String, ILconstReal> states = new HashMap<>();
    public final HashMap<Integer, ILconstInt> abilityLevels = new HashMap<>();
    public final Set<Integer> permanentAbilities = new HashSet<>();
    public final Set<String> unitTypes = new HashSet<>();
    public final java.util.ArrayList<IlConstHandle> inventory = new java.util.ArrayList<>();
    public ILconstInt currentOrder = ILconstInt.create(0);

    public UnitMock(IlConstHandle owner, ILconstInt unitid, ILconstReal x, ILconstReal y, ILconstReal face) {
        this.owner = owner;
        this.unitid = unitid;
        this.x = x;
        this.y = y;
        this.face = face;
        states.put("unitstate0", ILconstReal.create(100));
        states.put("unitstate1", ILconstReal.create(100));
        states.put("unitstate2", ILconstReal.create(0));
        states.put("unitstate3", ILconstReal.create(0));
    }

}
