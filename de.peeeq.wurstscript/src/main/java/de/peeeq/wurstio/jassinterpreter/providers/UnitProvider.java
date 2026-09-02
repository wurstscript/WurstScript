package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.objectreader.ObjectHelper;
import de.peeeq.wurstio.jassinterpreter.mocks.DestructableMock;
import de.peeeq.wurstio.jassinterpreter.mocks.ItemMock;
import de.peeeq.wurstio.jassinterpreter.mocks.LocationMock;
import de.peeeq.wurstio.jassinterpreter.mocks.PlayerMock;
import de.peeeq.wurstio.jassinterpreter.mocks.UnitMock;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class UnitProvider extends Provider {
    private static final Map<String, Integer> ORDER_IDS = new LinkedHashMap<>();
    private static final Map<AbstractInterpreter, Set<UnitMock>> unitsByInterpreter = new WeakHashMap<>();
    private final LinkedHashMap<IlConstHandle, ILconstInt> userDataMap = new LinkedHashMap<>();
    private final Set<UnitMock> units;

    static {
        ORDER_IDS.put("smart", 851971);
        ORDER_IDS.put("stop", 851972);
        ORDER_IDS.put("setrally", 851980);
        ORDER_IDS.put("cancel", 851976);
        ORDER_IDS.put("resumeharvesting", 852017);
        ORDER_IDS.put("harvest", 852018);
        ORDER_IDS.put("returnresources", 852020);
        ORDER_IDS.put("repair", 852024);
        ORDER_IDS.put("repairon", 852025);
        ORDER_IDS.put("repairoff", 852026);
        ORDER_IDS.put("load", 852046);
        ORDER_IDS.put("unload", 852048);
        ORDER_IDS.put("unloadall", 852049);
        ORDER_IDS.put("unloadallcorpses", 852051);
        ORDER_IDS.put("defend", 852055);
        ORDER_IDS.put("undefend", 852056);
        ORDER_IDS.put("heal", 852063);
        ORDER_IDS.put("healon", 852064);
        ORDER_IDS.put("healoff", 852065);
        ORDER_IDS.put("innerfire", 852066);
        ORDER_IDS.put("innerfireon", 852067);
        ORDER_IDS.put("innerfireoff", 852068);
        ORDER_IDS.put("invisibility", 852069);
        ORDER_IDS.put("holybolt", 852092);
        ORDER_IDS.put("resurrection", 852094);
        ORDER_IDS.put("thunderbolt", 852095);
        ORDER_IDS.put("thunderclap", 852096);
        ORDER_IDS.put("healingward", 852109);
        ORDER_IDS.put("lightningshield", 852110);
        ORDER_IDS.put("chainlightning", 852119);
        ORDER_IDS.put("cyclone", 852144);
        ORDER_IDS.put("detonate", 852145);
        ORDER_IDS.put("flamingarrows", 852174);
        ORDER_IDS.put("immolation", 852177);
        ORDER_IDS.put("manaburn", 852179);
        ORDER_IDS.put("firebolt", 852231);
        ORDER_IDS.put("inferno", 852232);
        ORDER_IDS.put("poisonarrows", 852255);
        ORDER_IDS.put("blizzard", 852089);
        ORDER_IDS.put("blink", 852525);
        ORDER_IDS.put("flamestrike", 852488);
        ORDER_IDS.put("entangleinstant", 852171);
        ORDER_IDS.put("attack", 851983);
        ORDER_IDS.put("attackground", 851984);
        ORDER_IDS.put("attackonce", 851985);
        ORDER_IDS.put("move", 851986);
        ORDER_IDS.put("moveitem", 851987);
        ORDER_IDS.put("patrol", 851990);
        ORDER_IDS.put("holdposition", 851993);
        ORDER_IDS.put("build", 851994);
        ORDER_IDS.put("humanbuild", 851995);
        ORDER_IDS.put("upgrade", 851997);
        ORDER_IDS.put("magicdefense", 852478);
        ORDER_IDS.put("magicleash", 852480);
        ORDER_IDS.put("magicundefense", 852479);
        ORDER_IDS.put("healingwave", 852501);
        ORDER_IDS.put("hex", 852502);
        ORDER_IDS.put("devourmagic", 852536);
        ORDER_IDS.put("impale", 852555);
        ORDER_IDS.put("locustswarm", 852556);
        ORDER_IDS.put("howlofterror", 852588);
        ORDER_IDS.put("channel", 852600);
        ORDER_IDS.put("neutralspell", 852630);
        ORDER_IDS.put("clusterrockets", 852652);
        ORDER_IDS.put("chemicalrage", 852663);
        ORDER_IDS.put("healingspray", 852664);
    }

    public UnitProvider(AbstractInterpreter interpreter) {
        super(interpreter);
        synchronized (unitsByInterpreter) {
            units = unitsByInterpreter.computeIfAbsent(interpreter, ignored ->
                    Collections.synchronizedSet(new LinkedHashSet<>()));
        }
    }

    public IlConstHandle CreateUnit(IlConstHandle owner, ILconstInt unitid, ILconstReal x, ILconstReal y, ILconstReal face) {
        UnitMock unitMock = new UnitMock(owner, unitid, x, y, face);
        unitMock.race = ConversionProvider.enumHandle("race", unitRace(unitid));
        units.add(unitMock);
        return new IlConstHandle(NameProvider.getRandomName("unit"), unitMock);
    }

    public IlConstHandle CreateUnitByName(IlConstHandle owner, ILconstString unitname, ILconstReal x, ILconstReal y, ILconstReal face) {
        return CreateUnit(owner, ILconstInt.create(ObjectHelper.objectIdStringToInt(unitname.getVal())), x, y, face);
    }

    public IlConstHandle CreateUnitAtLoc(IlConstHandle owner, ILconstInt unitid, IlConstHandle location, ILconstReal face) {
        LocationMock locationMock = locationOrNull(location);
        return CreateUnit(owner, unitid, locationMock == null ? ILconstReal.create(0) : locationMock.x,
                locationMock == null ? ILconstReal.create(0) : locationMock.y, face);
    }

    public IlConstHandle CreateUnitAtLocByName(IlConstHandle owner, ILconstString unitname, IlConstHandle location, ILconstReal face) {
        LocationMock locationMock = locationOrNull(location);
        return CreateUnitByName(owner, unitname, locationMock == null ? ILconstReal.create(0) : locationMock.x,
                locationMock == null ? ILconstReal.create(0) : locationMock.y, face);
    }

    public IlConstHandle CreateCorpse(IlConstHandle owner, ILconstInt unitid, ILconstReal x, ILconstReal y, ILconstReal face) {
        IlConstHandle unit = CreateUnit(owner, unitid, x, y, face);
        unitOrNull(unit).states.put("unitstate0", ILconstReal.create(0));
        return unit;
    }

    public ILconst GetOwningPlayer(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstNull.instance() : unitMock.owner;
    }

    public ILconstInt GetUnitTypeId(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstInt.create(0) : unitMock.unitid;
    }

    public ILconstReal GetUnitX(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.x;
    }

    public ILconstReal GetUnitY(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.y;
    }

    public ILconst GetUnitLoc(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstNull.instance()
                : new IlConstHandle(NameProvider.getRandomName("location"), new LocationMock(unitMock.x, unitMock.y));
    }

    public void SetUnitX(IlConstHandle unit, ILconstReal x) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.x = x;
        }
    }

    public void SetUnitY(IlConstHandle unit, ILconstReal y) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.y = y;
        }
    }

    public void SetUnitPosition(IlConstHandle unit, ILconstReal x, ILconstReal y) {
        SetUnitX(unit, x);
        SetUnitY(unit, y);
    }

    public void SetUnitPositionLoc(IlConstHandle unit, IlConstHandle location) {
        LocationMock locationMock = locationOrNull(location);
        if (locationMock != null) SetUnitPosition(unit, locationMock.x, locationMock.y);
    }

    public ILconstReal GetUnitFacing(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.face;
    }

    public void SetUnitFacing(IlConstHandle unit, ILconstReal facingAngle) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.face = facingAngle;
    }

    public void SetUnitFacingTimed(IlConstHandle unit, ILconstReal facingAngle, ILconstReal duration) {
        SetUnitFacing(unit, facingAngle);
    }

    public void ShowUnit(IlConstHandle unit, ILconstBool show) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.hidden = !show.getVal();
    }

    public ILconstBool IsUnitHidden(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && unitMock.hidden);
    }

    public ILconstString GetUnitName(IlConstHandle unit) {
        if (unit == null) {
            return ILconstString.fromText("");
        }
        UnitMock unitMock = (UnitMock) unit.getObj();
        return ILconstString.fromText(ObjectHelper.objectIdIntToString(unitMock.unitid.getVal()));
    }

    public ILconstInt GetUnitGoldCost(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt GetUnitWoodCost(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt GetUnitPointValueByType(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt GetFoodUsed(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstInt GetUnitBuildTime(ILconstInt unitid) {
        return ILconstInt.create(0);
    }

    public ILconstReal GetUnitMoveSpeed(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.moveSpeed;
    }

    public ILconstReal GetUnitDefaultMoveSpeed(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.defaultMoveSpeed;
    }

    public void SetUnitMoveSpeed(IlConstHandle unit, ILconstReal newSpeed) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.moveSpeed = newSpeed;
    }

    public ILconstReal GetUnitFlyHeight(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.flyHeight;
    }

    public ILconstReal GetUnitDefaultFlyHeight(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.defaultFlyHeight;
    }

    public void SetUnitFlyHeight(IlConstHandle unit, ILconstReal newHeight, ILconstReal rate) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.flyHeight = newHeight;
    }

    public ILconstReal GetUnitTurnSpeed(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.turnSpeed;
    }

    public ILconstReal GetUnitDefaultTurnSpeed(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.defaultTurnSpeed;
    }

    public void SetUnitTurnSpeed(IlConstHandle unit, ILconstReal newTurnSpeed) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.turnSpeed = newTurnSpeed;
    }

    public ILconstReal GetUnitPropWindow(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.propWindow;
    }

    public ILconstReal GetUnitDefaultPropWindow(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.defaultPropWindow;
    }

    public void SetUnitPropWindow(IlConstHandle unit, ILconstReal newPropWindowAngle) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.propWindow = newPropWindowAngle;
    }

    public ILconstReal GetUnitAcquireRange(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.acquireRange;
    }

    public ILconstReal GetUnitDefaultAcquireRange(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstReal.create(0) : unitMock.defaultAcquireRange;
    }

    public void SetUnitAcquireRange(IlConstHandle unit, ILconstReal newAcquireRange) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.acquireRange = newAcquireRange;
    }

    public ILconstInt GetUnitFoodMade(IlConstHandle unit) { return ILconstInt.create(0); }

    public IlConstHandle GetUnitRace(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? new IlConstHandle("race0", new Object()) : unitMock.race;
    }

    public ILconstInt BlzGetUnitIntegerField(IlConstHandle whichUnit, IlConstHandle whichField) {
        return ILconstInt.create(0);
    }

    public ILconstInt BlzGetUnitWeaponIntegerField(IlConstHandle whichUnit, IlConstHandle whichField, ILconstInt index) {
        return ILconstInt.create(0);
    }

    public ILconstBool IsUnitType(IlConstHandle whichUnit, IlConstHandle whichUnitType) {
        UnitMock unitMock = unitOrNull(whichUnit);
        if (unitMock == null || whichUnitType == null) return ILconstBool.FALSE;
        if ("unittype1".equals(whichUnitType.print())) {
            return ILconstBool.instance(unitMock.states.get("unitstate0").getVal() <= 0);
        }
        return ILconstBool.instance(unitMock.unitTypes.contains(whichUnitType.print()));
    }

    public ILconstBool IsUnit(IlConstHandle unit, IlConstHandle specifiedUnit) {
        return ILconstBool.instance(unit != null && unit == specifiedUnit);
    }

    public ILconstBool IsUnitOwnedByPlayer(IlConstHandle unit, IlConstHandle player) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && unitMock.owner == player);
    }

    public ILconstBool IsUnitInForce(IlConstHandle unit, IlConstHandle force) { return ILconstBool.FALSE; }

    public ILconstBool IsUnitAlly(IlConstHandle unit, IlConstHandle player) { return IsUnitOwnedByPlayer(unit, player); }

    public ILconstBool IsUnitEnemy(IlConstHandle unit, IlConstHandle player) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && unitMock.owner != null && unitMock.owner != player);
    }

    public ILconstBool IsUnitVisible(IlConstHandle unit, IlConstHandle player) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && !unitMock.hidden);
    }

    public ILconstBool IsUnitDetected(IlConstHandle unit, IlConstHandle player) { return IsUnitVisible(unit, player); }
    public ILconstBool IsUnitInvisible(IlConstHandle unit, IlConstHandle player) { return ILconstBool.FALSE; }
    public ILconstBool IsUnitFogged(IlConstHandle unit, IlConstHandle player) { return ILconstBool.FALSE; }
    public ILconstBool IsUnitMasked(IlConstHandle unit, IlConstHandle player) { return ILconstBool.FALSE; }
    public ILconstBool IsUnitRace(IlConstHandle unit, IlConstHandle race) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && race != null
                && (unitMock.race == race || unitMock.race.getObj().equals(race.getObj())));
    }

    public ILconstBool IsUnitInRange(IlConstHandle unit, IlConstHandle otherUnit, ILconstReal distance) {
        UnitMock first = unitOrNull(unit), second = unitOrNull(otherUnit);
        if (first == null || second == null) return ILconstBool.FALSE;
        return ILconstBool.instance(Math.hypot(first.x.getVal() - second.x.getVal(), first.y.getVal() - second.y.getVal()) <= distance.getVal());
    }

    public ILconstBool IsUnitInRangeXY(IlConstHandle unit, ILconstReal x, ILconstReal y, ILconstReal distance) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) return ILconstBool.FALSE;
        return ILconstBool.instance(Math.hypot(unitMock.x.getVal() - x.getVal(), unitMock.y.getVal() - y.getVal()) <= distance.getVal());
    }

    public ILconstBool IsUnitInRangeLoc(IlConstHandle unit, IlConstHandle location, ILconstReal distance) {
        LocationMock locationMock = locationOrNull(location);
        return locationMock == null ? ILconstBool.FALSE : IsUnitInRangeXY(unit, locationMock.x, locationMock.y, distance);
    }

    public ILconstBool IsUnitIllusion(IlConstHandle unit) { return ILconstBool.FALSE; }
    public ILconstBool IsUnitInTransport(IlConstHandle unit, IlConstHandle transport) { return ILconstBool.FALSE; }
    public ILconstBool IsUnitLoaded(IlConstHandle unit) { return ILconstBool.FALSE; }
    public ILconstBool IsHeroUnitId(ILconstInt unitId) {
        String rawcode = ObjectHelper.objectIdIntToString(unitId.getVal());
        return ILconstBool.instance(!rawcode.isEmpty() && Character.isUpperCase(rawcode.charAt(0)));
    }
    public ILconstBool IsUnitIdType(ILconstInt unitId, IlConstHandle unitType) { return ILconstBool.FALSE; }

    public ILconstBool UnitAddType(IlConstHandle unit, IlConstHandle unitType) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && unitType != null && unitMock.unitTypes.add(unitType.print()));
    }

    public ILconstBool UnitRemoveType(IlConstHandle unit, IlConstHandle unitType) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && unitType != null && unitMock.unitTypes.remove(unitType.print()));
    }

    public void PauseUnit(IlConstHandle unit, ILconstBool flag) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.paused = flag.getVal();
    }

    public ILconstBool IsUnitPaused(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && unitMock.paused);
    }

    public void SetUnitInvulnerable(IlConstHandle unit, ILconstBool flag) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.invulnerable = flag.getVal();
    }

    public ILconstBool BlzIsUnitInvulnerable(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return ILconstBool.instance(unitMock != null && unitMock.invulnerable);
    }

    public void SetUnitPathing(IlConstHandle unit, ILconstBool flag) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.pathing = flag.getVal();
    }

    public void SetUnitOwner(IlConstHandle unit, IlConstHandle owner, ILconstBool changeColor) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.owner = owner;
    }

    public void RemoveUnit(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.removed = true;
        }
        userDataMap.remove(unit);
    }

    public void KillUnit(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.states.put("unitstate0", ILconstReal.create(0));
        }
    }

    public ILconstReal GetUnitState(IlConstHandle unit, IlConstHandle unitstate) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstReal.create(0);
        }
        return unitMock.states.getOrDefault(unitStateKey(unitstate), ILconstReal.create(0));
    }

    public void SetUnitState(IlConstHandle unit, IlConstHandle unitstate, ILconstReal value) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) {
            unitMock.states.put(unitStateKey(unitstate), value);
        }
    }

    public ILconstReal GetWidgetLife(IlConstHandle widget) {
        UnitMock unitMock = unitOrNull(widget);
        if (unitMock != null) {
            return unitMock.states.getOrDefault("unitstate0", ILconstReal.create(0));
        }
        DestructableMock destructableMock = destructableOrNull(widget);
        if (destructableMock != null) return destructableMock.life;
        ItemMock itemMock = itemOrNull(widget);
        return itemMock == null ? ILconstReal.create(0) : itemMock.life;
    }

    public void SetWidgetLife(IlConstHandle widget, ILconstReal newLife) {
        UnitMock unitMock = unitOrNull(widget);
        if (unitMock != null) {
            unitMock.states.put("unitstate0", newLife);
            return;
        }
        DestructableMock destructableMock = destructableOrNull(widget);
        if (destructableMock != null) {
            destructableMock.life = newLife;
            return;
        }
        ItemMock itemMock = itemOrNull(widget);
        if (itemMock != null) itemMock.life = newLife;
    }

    public ILconstBool UnitAddAbility(IlConstHandle unit, ILconstInt abilityId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstBool.FALSE;
        }
        unitMock.abilityLevels.putIfAbsent(abilityId.getVal(), ILconstInt.create(1));
        return ILconstBool.TRUE;
    }

    public ILconstBool UnitRemoveAbility(IlConstHandle unit, ILconstInt abilityId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstBool.FALSE;
        }
        return ILconstBool.instance(unitMock.abilityLevels.remove(abilityId.getVal()) != null);
    }

    public ILconstInt GetUnitAbilityLevel(IlConstHandle unit, ILconstInt abilityId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstInt.create(0);
        }
        return unitMock.abilityLevels.getOrDefault(abilityId.getVal(), ILconstInt.create(0));
    }

    public ILconstInt SetUnitAbilityLevel(IlConstHandle unit, ILconstInt abilityId, ILconstInt level) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null) {
            return ILconstInt.create(0);
        }
        unitMock.abilityLevels.put(abilityId.getVal(), level);
        return level;
    }

    public ILconstBool UnitMakeAbilityPermanent(IlConstHandle unit, ILconstBool permanent, ILconstInt abilityId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null || !unitMock.abilityLevels.containsKey(abilityId.getVal())) return ILconstBool.FALSE;
        if (permanent.getVal()) unitMock.permanentAbilities.add(abilityId.getVal());
        else unitMock.permanentAbilities.remove(abilityId.getVal());
        return ILconstBool.TRUE;
    }

    public ILconstInt IncUnitAbilityLevel(IlConstHandle unit, ILconstInt abilityId) {
        return SetUnitAbilityLevel(unit, abilityId, ILconstInt.create(GetUnitAbilityLevel(unit, abilityId).getVal() + 1));
    }

    public ILconstInt DecUnitAbilityLevel(IlConstHandle unit, ILconstInt abilityId) {
        return SetUnitAbilityLevel(unit, abilityId, ILconstInt.create(Math.max(0, GetUnitAbilityLevel(unit, abilityId).getVal() - 1)));
    }

    public ILconstInt GetUnitLevel(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstInt.create(0) : unitMock.level;
    }

    public ILconstInt GetHeroLevel(IlConstHandle unit) { return GetUnitLevel(unit); }

    public void SetHeroLevel(IlConstHandle unit, ILconstInt level, ILconstBool showEyeCandy) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.level = level;
    }

    public ILconstInt GetHeroXP(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstInt.create(0) : unitMock.heroXp;
    }

    public void SetHeroXP(IlConstHandle unit, ILconstInt xp, ILconstBool showEyeCandy) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.heroXp = xp;
    }

    public void AddHeroXP(IlConstHandle unit, ILconstInt xp, ILconstBool showEyeCandy) {
        SetHeroXP(unit, ILconstInt.create(GetHeroXP(unit).getVal() + xp.getVal()), showEyeCandy);
    }

    public ILconstInt GetHeroStr(IlConstHandle unit, ILconstBool includeBonuses) { UnitMock m = unitOrNull(unit); return m == null ? ILconstInt.create(0) : m.heroStr; }
    public ILconstInt GetHeroAgi(IlConstHandle unit, ILconstBool includeBonuses) { UnitMock m = unitOrNull(unit); return m == null ? ILconstInt.create(0) : m.heroAgi; }
    public ILconstInt GetHeroInt(IlConstHandle unit, ILconstBool includeBonuses) { UnitMock m = unitOrNull(unit); return m == null ? ILconstInt.create(0) : m.heroInt; }
    public void SetHeroStr(IlConstHandle unit, ILconstInt value, ILconstBool permanent) { UnitMock m = unitOrNull(unit); if (m != null) m.heroStr = value; }
    public void SetHeroAgi(IlConstHandle unit, ILconstInt value, ILconstBool permanent) { UnitMock m = unitOrNull(unit); if (m != null) m.heroAgi = value; }
    public void SetHeroInt(IlConstHandle unit, ILconstInt value, ILconstBool permanent) { UnitMock m = unitOrNull(unit); if (m != null) m.heroInt = value; }

    public ILconstInt GetHeroSkillPoints(IlConstHandle unit) { UnitMock m = unitOrNull(unit); return m == null ? ILconstInt.create(0) : m.skillPoints; }

    public ILconstBool UnitModifySkillPoints(IlConstHandle unit, ILconstInt delta) {
        UnitMock m = unitOrNull(unit);
        if (m == null || m.skillPoints.getVal() + delta.getVal() < 0) return ILconstBool.FALSE;
        m.skillPoints = ILconstInt.create(m.skillPoints.getVal() + delta.getVal());
        return ILconstBool.TRUE;
    }

    public ILconstBool UnitStripHeroLevel(IlConstHandle unit, ILconstInt levels) {
        UnitMock m = unitOrNull(unit);
        if (m == null) return ILconstBool.FALSE;
        m.level = ILconstInt.create(Math.max(1, m.level.getVal() - levels.getVal()));
        return ILconstBool.TRUE;
    }

    public ILconstString GetHeroProperName(IlConstHandle unit) { return GetUnitName(unit); }
    public void SuspendHeroXP(IlConstHandle unit, ILconstBool flag) { UnitMock m = unitOrNull(unit); if (m != null) m.suspendedXp = flag.getVal(); }
    public ILconstBool IsSuspendedXP(IlConstHandle unit) { UnitMock m = unitOrNull(unit); return ILconstBool.instance(m != null && m.suspendedXp); }
    public void SelectHeroSkill(IlConstHandle unit, ILconstInt abilityId) { UnitAddAbility(unit, abilityId); }

    public ILconstBool ReviveHero(IlConstHandle unit, ILconstReal x, ILconstReal y, ILconstBool doEyeCandy) {
        UnitMock m = unitOrNull(unit);
        if (m == null) return ILconstBool.FALSE;
        m.x = x; m.y = y; m.states.put("unitstate0", ILconstReal.create(100));
        return ILconstBool.TRUE;
    }

    public ILconstBool ReviveHeroLoc(IlConstHandle unit, IlConstHandle location, ILconstBool doEyeCandy) {
        LocationMock m = locationOrNull(location);
        return m == null ? ILconstBool.FALSE : ReviveHero(unit, m.x, m.y, doEyeCandy);
    }

    public void SelectUnit(IlConstHandle unit, ILconstBool flag) {
        UnitMock m = unitOrNull(unit);
        if (m == null) return;
        if (flag.getVal()) m.selectedPlayers.add(0);
        else m.selectedPlayers.remove(0);
    }
    public void ClearSelection() {
        synchronized (units) {
            for (UnitMock unit : units) unit.selectedPlayers.remove(0);
        }
    }
    public ILconstBool IsUnitSelected(IlConstHandle unit, IlConstHandle player) {
        UnitMock m = unitOrNull(unit);
        return ILconstBool.instance(m != null && m.selectedPlayers.contains(playerId(player)));
    }
    public void SetUnitColor(IlConstHandle unit, IlConstHandle color) { }
    public void SetUnitScale(IlConstHandle unit, ILconstReal x, ILconstReal y, ILconstReal z) { }
    public void SetUnitTimeScale(IlConstHandle unit, ILconstReal scale) { }
    public void SetUnitBlendTime(IlConstHandle unit, ILconstReal time) { }
    public void SetUnitVertexColor(IlConstHandle unit, ILconstInt red, ILconstInt green, ILconstInt blue, ILconstInt alpha) { }
    public void QueueUnitAnimation(IlConstHandle unit, ILconstString animation) { }
    public void SetUnitAnimation(IlConstHandle unit, ILconstString animation) { }
    public void SetUnitAnimationByIndex(IlConstHandle unit, ILconstInt animation) { }
    public void SetUnitAnimationWithRarity(IlConstHandle unit, ILconstString animation, IlConstHandle rarity) { }
    public void AddUnitAnimationProperties(IlConstHandle unit, ILconstString properties, ILconstBool add) { }
    public void SetUnitLookAt(IlConstHandle unit, ILconstString bone, IlConstHandle target, ILconstReal x, ILconstReal y, ILconstReal z) { }
    public void ResetUnitLookAt(IlConstHandle unit) { }
    public void SetUnitRescuable(IlConstHandle unit, IlConstHandle player, ILconstBool flag) { }
    public void SetUnitRescueRange(IlConstHandle unit, ILconstReal range) { }
    public void SetUnitCreepGuard(IlConstHandle unit, ILconstBool creepGuard) { }
    public void SetUnitExploded(IlConstHandle unit, ILconstBool exploded) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock != null) unitMock.exploded = exploded.getVal();
    }
    public void UnitShareVision(IlConstHandle unit, IlConstHandle player, ILconstBool share) { }
    public void UnitSuspendDecay(IlConstHandle unit, ILconstBool suspend) { }
    public void UnitSetConstructionProgress(IlConstHandle unit, ILconstInt percentage) { }
    public void UnitSetUpgradeProgress(IlConstHandle unit, ILconstInt percentage) { }
    public void UnitSetUsesAltIcon(IlConstHandle unit, ILconstBool flag) { }

    public ILconstBool IssueImmediateOrderById(IlConstHandle unit, ILconstInt orderId) {
        return issueOrder(unit, orderId);
    }

    public ILconstBool IssuePointOrderById(IlConstHandle unit, ILconstInt orderId, ILconstReal x, ILconstReal y) {
        return issueOrder(unit, orderId);
    }

    public ILconstBool IssueTargetOrderById(IlConstHandle unit, ILconstInt orderId, IlConstHandle target) {
        return issueOrder(unit, orderId);
    }

    public ILconstBool IssueImmediateOrder(IlConstHandle unit, ILconstString order) { return issueOrder(unit, orderId(order)); }
    public ILconstBool IssuePointOrder(IlConstHandle unit, ILconstString order, ILconstReal x, ILconstReal y) { return issueOrder(unit, orderId(order)); }
    public ILconstBool IssuePointOrderLoc(IlConstHandle unit, ILconstString order, IlConstHandle location) { return issueOrder(unit, orderId(order)); }
    public ILconstBool IssueTargetOrder(IlConstHandle unit, ILconstString order, IlConstHandle target) { return issueOrder(unit, orderId(order)); }
    public ILconstBool IssueBuildOrder(IlConstHandle unit, ILconstString unitToBuild, ILconstReal x, ILconstReal y) { return validUnit(unit); }
    public ILconstBool IssueBuildOrderById(IlConstHandle unit, ILconstInt unitId, ILconstReal x, ILconstReal y) { return validUnit(unit); }
    public ILconstBool IssuePointOrderByIdLoc(IlConstHandle unit, ILconstInt orderId, IlConstHandle location) { return issueOrder(unit, orderId); }
    public ILconstBool IssueInstantPointOrder(IlConstHandle unit, ILconstString order, ILconstReal x, ILconstReal y, IlConstHandle target) { return issueOrder(unit, orderId(order)); }
    public ILconstBool IssueInstantPointOrderById(IlConstHandle unit, ILconstInt orderId, ILconstReal x, ILconstReal y, IlConstHandle target) { return issueOrder(unit, orderId); }
    public ILconstBool IssueInstantTargetOrder(IlConstHandle unit, ILconstString order, IlConstHandle target, IlConstHandle instantTarget) { return issueOrder(unit, orderId(order)); }
    public ILconstBool IssueInstantTargetOrderById(IlConstHandle unit, ILconstInt orderId, IlConstHandle target, IlConstHandle instantTarget) { return issueOrder(unit, orderId); }
    public ILconstBool IssueNeutralImmediateOrder(IlConstHandle player, IlConstHandle structure, ILconstString unitToBuild) { return validUnit(structure); }
    public ILconstBool IssueNeutralImmediateOrderById(IlConstHandle player, IlConstHandle structure, ILconstInt unitId) { return validUnit(structure); }
    public ILconstBool IssueNeutralPointOrder(IlConstHandle player, IlConstHandle structure, ILconstString unitToBuild, ILconstReal x, ILconstReal y) { return validUnit(structure); }
    public ILconstBool IssueNeutralPointOrderById(IlConstHandle player, IlConstHandle structure, ILconstInt unitId, ILconstReal x, ILconstReal y) { return validUnit(structure); }
    public ILconstBool IssueNeutralTargetOrder(IlConstHandle player, IlConstHandle structure, ILconstString unitToBuild, IlConstHandle target) { return validUnit(structure); }
    public ILconstBool IssueNeutralTargetOrderById(IlConstHandle player, IlConstHandle structure, ILconstInt unitId, IlConstHandle target) { return validUnit(structure); }

    public ILconstInt OrderId(ILconstString order) { return orderId(order); }
    public ILconstString OrderId2String(ILconstInt orderId) {
        for (Map.Entry<String, Integer> entry : ORDER_IDS.entrySet()) {
            if (entry.getValue().equals(orderId.getVal())) return ILconstString.fromText(entry.getKey());
        }
        return ILconstString.fromText("");
    }

    public ILconstInt GetUnitCurrentOrder(IlConstHandle unit) {
        UnitMock unitMock = unitOrNull(unit);
        return unitMock == null ? ILconstInt.create(0) : unitMock.currentOrder;
    }

    public void SetResourceAmount(IlConstHandle unit, ILconstInt amount) { UnitMock m = unitOrNull(unit); if (m != null) m.resourceAmount = amount; }
    public void AddResourceAmount(IlConstHandle unit, ILconstInt amount) { SetResourceAmount(unit, ILconstInt.create(GetResourceAmount(unit).getVal() + amount.getVal())); }
    public ILconstInt GetResourceAmount(IlConstHandle unit) { UnitMock m = unitOrNull(unit); return m == null ? ILconstInt.create(0) : m.resourceAmount; }
    public ILconstInt GetUnitFoodUsed(IlConstHandle unit) { return ILconstInt.create(0); }
    public ILconstInt GetFoodMade(ILconstInt unitId) { return ILconstInt.create(0); }
    public void SetUnitUseFood(IlConstHandle unit, ILconstBool useFood) { UnitMock m = unitOrNull(unit); if (m != null) m.useFood = useFood.getVal(); }
    public ILconstInt GetUnitPointValue(IlConstHandle unit) { return ILconstInt.create(0); }
    public ILconst GetUnitRallyPoint(IlConstHandle unit) { return ILconstNull.instance(); }
    public ILconst GetUnitRallyUnit(IlConstHandle unit) { return ILconstNull.instance(); }
    public ILconst GetUnitRallyDestructable(IlConstHandle unit) { return ILconstNull.instance(); }

    public ILconstBool UnitAddItem(IlConstHandle unit, IlConstHandle item) {
        UnitMock m = unitOrNull(unit);
        if (m == null || item == null || inventoryFull(m) || m.inventory.contains(item) || itemHeldByOtherUnit(m, item)) return ILconstBool.FALSE;
        putInFirstFreeSlot(m, item);
        return ILconstBool.TRUE;
    }

    public ILconst UnitAddItemById(IlConstHandle unit, ILconstInt itemId) {
        UnitMock m = unitOrNull(unit);
        if (m == null || inventoryFull(m)) return ILconstNull.instance();
        IlConstHandle item = new IlConstHandle(NameProvider.getRandomName("item"), new ItemMock(itemId, m.x, m.y));
        putInFirstFreeSlot(m, item);
        return item;
    }

    public ILconstBool UnitAddItemToSlotById(IlConstHandle unit, ILconstInt itemId, ILconstInt slot) {
        UnitMock m = unitOrNull(unit);
        if (m == null || slot.getVal() < 0 || slot.getVal() >= 6 || inventoryFull(m)) return ILconstBool.FALSE;
        while (m.inventory.size() <= slot.getVal()) m.inventory.add(null);
        if (m.inventory.get(slot.getVal()) != null) return ILconstBool.FALSE;
        m.inventory.set(slot.getVal(), new IlConstHandle(NameProvider.getRandomName("item"), new ItemMock(itemId, m.x, m.y)));
        return ILconstBool.TRUE;
    }

    public void UnitRemoveItem(IlConstHandle unit, IlConstHandle item) {
        UnitMock m = unitOrNull(unit);
        if (m != null) {
            int index = m.inventory.indexOf(item);
            if (index >= 0) m.inventory.set(index, null);
        }
    }
    public ILconst UnitRemoveItemFromSlot(IlConstHandle unit, ILconstInt slot) {
        UnitMock m = unitOrNull(unit);
        return m == null || slot.getVal() < 0 || slot.getVal() >= m.inventory.size() || m.inventory.get(slot.getVal()) == null
                ? ILconstNull.instance() : m.inventory.set(slot.getVal(), null);
    }
    public ILconstBool UnitHasItem(IlConstHandle unit, IlConstHandle item) { UnitMock m = unitOrNull(unit); return ILconstBool.instance(m != null && m.inventory.contains(item)); }
    public ILconst UnitItemInSlot(IlConstHandle unit, ILconstInt slot) {
        UnitMock m = unitOrNull(unit);
        if (m == null || slot.getVal() < 0 || slot.getVal() >= m.inventory.size() || m.inventory.get(slot.getVal()) == null) return ILconstNull.instance();
        return m.inventory.get(slot.getVal());
    }
    public ILconstInt UnitInventorySize(IlConstHandle unit) { return ILconstInt.create(6); }
    public ILconstBool UnitDropItemPoint(IlConstHandle unit, IlConstHandle item, ILconstReal x, ILconstReal y) { return dropItem(unit, item); }
    public ILconstBool UnitDropItemSlot(IlConstHandle unit, IlConstHandle item, ILconstInt slot) { return moveItemToSlot(unit, item, slot.getVal()); }
    public ILconstBool UnitDropItemTarget(IlConstHandle unit, IlConstHandle item, IlConstHandle target) { return transferItem(unit, item, target); }
    public ILconstBool UnitUseItem(IlConstHandle unit, IlConstHandle item) { return useItem(unit, item); }
    public ILconstBool UnitUseItemPoint(IlConstHandle unit, IlConstHandle item, ILconstReal x, ILconstReal y) { return useItem(unit, item); }
    public ILconstBool UnitUseItemTarget(IlConstHandle unit, IlConstHandle item, IlConstHandle target) { return useItem(unit, item); }

    public void UnitAddSleep(IlConstHandle unit, ILconstBool add) { UnitMock m = unitOrNull(unit); if (m != null) m.canSleep = add.getVal(); }
    public ILconstBool UnitCanSleep(IlConstHandle unit) { UnitMock m = unitOrNull(unit); return ILconstBool.instance(m != null && m.canSleep); }
    public void UnitAddSleepPerm(IlConstHandle unit, ILconstBool add) { UnitMock m = unitOrNull(unit); if (m != null) m.canSleepPerm = add.getVal(); }
    public ILconstBool UnitCanSleepPerm(IlConstHandle unit) { UnitMock m = unitOrNull(unit); return ILconstBool.instance(m != null && m.canSleepPerm); }
    public ILconstBool UnitIsSleeping(IlConstHandle unit) { UnitMock m = unitOrNull(unit); return ILconstBool.instance(m != null && m.sleeping); }
    public void UnitWakeUp(IlConstHandle unit) { UnitMock m = unitOrNull(unit); if (m != null) m.sleeping = false; }
    public void UnitApplyTimedLife(IlConstHandle unit, ILconstInt buffId, ILconstReal duration) { }
    public ILconstBool UnitIgnoreAlarm(IlConstHandle unit, ILconstBool flag) { return validUnit(unit); }
    public ILconstBool UnitIgnoreAlarmToggled(IlConstHandle unit) { return ILconstBool.FALSE; }
    public void UnitResetCooldown(IlConstHandle unit) { }
    public void UnitPauseTimedLife(IlConstHandle unit, ILconstBool flag) { }
    public void UnitRemoveBuffs(IlConstHandle unit, ILconstBool positive, ILconstBool negative) { }
    public void UnitRemoveBuffsEx(IlConstHandle unit, ILconstBool positive, ILconstBool negative, ILconstBool magic, ILconstBool physical, ILconstBool timedLife, ILconstBool aura, ILconstBool autoDispel) { }
    public ILconstBool UnitHasBuffsEx(IlConstHandle unit, ILconstBool positive, ILconstBool negative, ILconstBool magic, ILconstBool physical, ILconstBool timedLife, ILconstBool aura, ILconstBool autoDispel) { return ILconstBool.FALSE; }
    public ILconstInt UnitCountBuffsEx(IlConstHandle unit, ILconstBool positive, ILconstBool negative, ILconstBool magic, ILconstBool physical, ILconstBool timedLife, ILconstBool aura, ILconstBool autoDispel) { return ILconstInt.create(0); }
    public ILconstBool UnitDamagePoint(IlConstHandle unit, ILconstReal delay, ILconstReal radius, ILconstReal x, ILconstReal y, ILconstReal amount, ILconstBool attack, ILconstBool ranged, IlConstHandle attackType, IlConstHandle damageType, IlConstHandle weaponType) {
        if (unitOrNull(unit) == null) return ILconstBool.FALSE;
        ArrayList<UnitMock> targets;
        synchronized (units) {
            targets = new ArrayList<>(units);
        }
        double radiusValue = radius.getVal();
        for (UnitMock target : targets) {
            double dx = target.x.getVal() - x.getVal();
            double dy = target.y.getVal() - y.getVal();
            if (dx * dx + dy * dy <= radiusValue * radiusValue) applyDamage(target, amount.getVal());
        }
        return ILconstBool.TRUE;
    }
    public ILconstBool UnitDamageTarget(IlConstHandle unit, IlConstHandle target, ILconstReal amount, ILconstBool attack, ILconstBool ranged, IlConstHandle attackType, IlConstHandle damageType, IlConstHandle weaponType) {
        if (unitOrNull(unit) == null) return ILconstBool.FALSE;
        UnitMock m = unitOrNull(target);
        if (m != null) {
            applyDamage(m, amount.getVal());
            return ILconstBool.TRUE;
        }
        DestructableMock destructable = destructableOrNull(target);
        if (destructable != null) {
            destructable.life = ILconstReal.create((float) Math.max(0, destructable.life.getVal() - amount.getVal()));
            return ILconstBool.TRUE;
        }
        ItemMock item = itemOrNull(target);
        if (item == null) return ILconstBool.FALSE;
        item.life = ILconstReal.create((float) Math.max(0, item.life.getVal() - amount.getVal()));
        return ILconstBool.TRUE;
    }

    public ILconstInt GetUnitUserData(IlConstHandle unit) {
        return unit == null ? ILconstInt.create(0) : userDataMap.getOrDefault(unit, ILconstInt.create(0));
    }

    public void AddItemToAllStock(ILconstInt itemId, ILconstInt currentStock, ILconstInt stockMax) { }
    public void AddItemToStock(IlConstHandle unit, ILconstInt itemId, ILconstInt currentStock, ILconstInt stockMax) { }
    public void AddUnitToAllStock(ILconstInt unitId, ILconstInt currentStock, ILconstInt stockMax) { }
    public void AddUnitToStock(IlConstHandle unit, ILconstInt unitId, ILconstInt currentStock, ILconstInt stockMax) { }
    public void RemoveItemFromAllStock(ILconstInt itemId) { }
    public void RemoveItemFromStock(IlConstHandle unit, ILconstInt itemId) { }
    public void RemoveUnitFromAllStock(ILconstInt unitId) { }
    public void RemoveUnitFromStock(IlConstHandle unit, ILconstInt unitId) { }
    public void SetAllItemTypeSlots(ILconstInt slots) { }
    public void SetAllUnitTypeSlots(ILconstInt slots) { }
    public void SetItemTypeSlots(IlConstHandle unit, ILconstInt slots) { }
    public void SetUnitTypeSlots(IlConstHandle unit, ILconstInt slots) { }

    public ILconstReal WaygateGetDestinationX(IlConstHandle waygate) { UnitMock m = unitOrNull(waygate); return m == null ? ILconstReal.create(0) : m.waygateX; }
    public ILconstReal WaygateGetDestinationY(IlConstHandle waygate) { UnitMock m = unitOrNull(waygate); return m == null ? ILconstReal.create(0) : m.waygateY; }
    public void WaygateSetDestination(IlConstHandle waygate, ILconstReal x, ILconstReal y) { UnitMock m = unitOrNull(waygate); if (m != null) { m.waygateX = x; m.waygateY = y; } }
    public void WaygateActivate(IlConstHandle waygate, ILconstBool activate) { UnitMock m = unitOrNull(waygate); if (m != null) m.waygateActive = activate.getVal(); }
    public ILconstBool WaygateIsActive(IlConstHandle waygate) { UnitMock m = unitOrNull(waygate); return ILconstBool.instance(m != null && m.waygateActive); }

    public void SetUnitUserData(IlConstHandle unit, ILconstInt userData) {
        if (unit == null) {
            return;
        }
        userDataMap.put(unit, userData);
    }

    private ILconstBool issueOrder(IlConstHandle unit, ILconstInt orderId) {
        UnitMock unitMock = unitOrNull(unit);
        if (unitMock == null || orderId.getVal() == 0) {
            return ILconstBool.FALSE;
        }
        unitMock.currentOrder = orderId;
        return ILconstBool.TRUE;
    }

    private ILconstBool validUnit(IlConstHandle unit) { return ILconstBool.instance(unitOrNull(unit) != null); }
    private ILconstInt orderId(ILconstString order) {
        Integer id = ORDER_IDS.get(order.getVal().toLowerCase(Locale.ROOT));
        return ILconstInt.create(id == null ? 0 : id);
    }
    private int playerId(IlConstHandle player) {
        return player != null && player.getObj() instanceof PlayerMock ? ((PlayerMock) player.getObj()).id.getVal() : -1;
    }
    private int unitRace(ILconstInt unitid) {
        String rawcode = ObjectHelper.objectIdIntToString(unitid.getVal()).toLowerCase(Locale.ROOT);
        return switch (rawcode.charAt(0)) {
            case 'h' -> 1;
            case 'o' -> 2;
            case 'u' -> 3;
            case 'e' -> 4;
            default -> 7;
        };
    }
    private boolean itemHeldByOtherUnit(UnitMock destination, IlConstHandle item) {
        synchronized (units) {
            for (UnitMock unit : units) {
                if (unit != destination && unit.inventory.contains(item)) return true;
            }
        }
        return false;
    }
    private boolean inventoryFull(UnitMock unit) { return firstFreeSlot(unit) >= 6; }
    private int firstFreeSlot(UnitMock unit) {
        for (int i = 0; i < 6; i++) {
            if (i >= unit.inventory.size() || unit.inventory.get(i) == null) return i;
        }
        return 6;
    }
    private void putInFirstFreeSlot(UnitMock unit, IlConstHandle item) {
        int slot = firstFreeSlot(unit);
        while (unit.inventory.size() <= slot) unit.inventory.add(null);
        unit.inventory.set(slot, item);
    }
    private ILconstBool moveItemToSlot(IlConstHandle unit, IlConstHandle item, int slot) {
        UnitMock source = unitOrNull(unit);
        if (source == null || slot < 0 || slot >= 6) return ILconstBool.FALSE;
        int sourceSlot = source.inventory.indexOf(item);
        if (sourceSlot < 0 || sourceSlot == slot) return ILconstBool.instance(sourceSlot == slot);
        while (source.inventory.size() <= slot) source.inventory.add(null);
        if (source.inventory.get(slot) != null) return ILconstBool.FALSE;
        source.inventory.set(sourceSlot, null);
        source.inventory.set(slot, item);
        return ILconstBool.TRUE;
    }
    private ILconstBool transferItem(IlConstHandle unit, IlConstHandle item, IlConstHandle target) {
        UnitMock source = unitOrNull(unit);
        UnitMock destination = unitOrNull(target);
        if (source == null || destination == null || !source.inventory.contains(item)) return ILconstBool.FALSE;
        if (source == destination) return ILconstBool.TRUE;
        if (inventoryFull(destination) || destination.inventory.contains(item)) return ILconstBool.FALSE;
        int sourceSlot = source.inventory.indexOf(item);
        source.inventory.set(sourceSlot, null);
        putInFirstFreeSlot(destination, item);
        return ILconstBool.TRUE;
    }
    private ILconstBool useItem(IlConstHandle unit, IlConstHandle item) {
        UnitMock m = unitOrNull(unit);
        return ILconstBool.instance(m != null && m.inventory.contains(item));
    }
    private void applyDamage(UnitMock target, double amount) {
        if (!target.invulnerable) {
            target.states.put("unitstate0", ILconstReal.create((float) Math.max(0, target.states.get("unitstate0").getVal() - amount)));
        }
    }
    private ILconstBool dropItem(IlConstHandle unit, IlConstHandle item) {
        UnitMock m = unitOrNull(unit);
        if (m == null) return ILconstBool.FALSE;
        int index = m.inventory.indexOf(item);
        if (index < 0) return ILconstBool.FALSE;
        m.inventory.set(index, null);
        return ILconstBool.TRUE;
    }

    private UnitMock unitOrNull(IlConstHandle unit) {
        if (unit == null || !(unit.getObj() instanceof UnitMock)) {
            return null;
        }
        return (UnitMock) unit.getObj();
    }

    private LocationMock locationOrNull(IlConstHandle location) {
        if (location == null || !(location.getObj() instanceof LocationMock)) return null;
        return (LocationMock) location.getObj();
    }

    private DestructableMock destructableOrNull(IlConstHandle destructable) {
        if (destructable == null || !(destructable.getObj() instanceof DestructableMock)) {
            return null;
        }
        return (DestructableMock) destructable.getObj();
    }

    private ItemMock itemOrNull(IlConstHandle item) {
        if (item == null || !(item.getObj() instanceof ItemMock)) return null;
        return (ItemMock) item.getObj();
    }

    private String unitStateKey(IlConstHandle unitstate) {
        return unitstate == null ? "unitstate0" : unitstate.print();
    }
}
