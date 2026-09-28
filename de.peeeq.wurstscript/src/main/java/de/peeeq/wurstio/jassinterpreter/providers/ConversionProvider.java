package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConversionProvider extends Provider {
    /**
     * Measured on the 3.0.0 client: ConvertX(n) gives the same handle for the same n, also for values
     * without a common.j constant. So every conversion is one cached handle per type and value.
     */
    private static final Map<String, IlConstHandle> enumHandles = new ConcurrentHashMap<>();

    public ConversionProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public static IlConstHandle enumHandle(String typeName, int value) {
        return enumHandles.computeIfAbsent(typeName + value, key -> new IlConstHandle(key, value));
    }


    public IlConstHandle ConvertRace(ILconstInt i) {
        return enumHandle("race", i.getVal());
    }

    public IlConstHandle ConvertAllianceType(ILconstInt i) {
        return enumHandle("alliancetype", i.getVal());
    }

    public IlConstHandle ConvertRacePref(ILconstInt i) {
        return enumHandle("racepreference", i.getVal());
    }

    public IlConstHandle ConvertIGameState(ILconstInt i) {
        return enumHandle("igamestate", i.getVal());
    }

    public IlConstHandle ConvertFGameState(ILconstInt i) {
        return enumHandle("fgamestate", i.getVal());
    }

    public IlConstHandle ConvertPlayerState(ILconstInt i) {
        return enumHandle("playerstate", i.getVal());
    }

    public IlConstHandle ConvertPlayerScore(ILconstInt i) {
        return enumHandle("playerscore", i.getVal());
    }

    public IlConstHandle ConvertPlayerGameResult(ILconstInt i) {
        return enumHandle("playergameresult", i.getVal());
    }

    public IlConstHandle ConvertUnitState(ILconstInt i) {
        return enumHandle("unitstate", i.getVal());
    }

    public IlConstHandle ConvertUnitIntegerField(ILconstInt i) {
        return enumHandle("unitintegerfield", i.getVal());
    }

    public IlConstHandle ConvertUnitWeaponIntegerField(ILconstInt i) {
        return enumHandle("unitweaponintegerfield", i.getVal());
    }

    public IlConstHandle ConvertAIDifficulty(ILconstInt i) {
        return enumHandle("aidifficulty", i.getVal());
    }

    public IlConstHandle ConvertGameEvent(ILconstInt i) {
        return enumHandle("gameevent", i.getVal());
    }

    public IlConstHandle ConvertPlayerEvent(ILconstInt i) {
        return enumHandle("playerevent", i.getVal());
    }

    public IlConstHandle ConvertPlayerUnitEvent(ILconstInt i) {
        return enumHandle("playerunitevent", i.getVal());
    }

    public IlConstHandle ConvertWidgetEvent(ILconstInt i) {
        return enumHandle("widgetevent", i.getVal());
    }

    public IlConstHandle ConvertDialogEvent(ILconstInt i) {
        return enumHandle("dialogevent", i.getVal());
    }

    public IlConstHandle ConvertUnitEvent(ILconstInt i) {
        return enumHandle("unitevent", i.getVal());
    }

    public IlConstHandle ConvertLimitOp(ILconstInt i) {
        return enumHandle("limitop", i.getVal());
    }

    public IlConstHandle ConvertUnitType(ILconstInt i) {
        return enumHandle("unittype", i.getVal());
    }

    public IlConstHandle ConvertGameSpeed(ILconstInt i) {
        return enumHandle("gamespeed", i.getVal());
    }

    public IlConstHandle ConvertPlacement(ILconstInt i) {
        return enumHandle("placement", i.getVal());
    }

    public IlConstHandle ConvertStartLocPrio(ILconstInt i) {
        return enumHandle("startlocprio", i.getVal());
    }

    public IlConstHandle ConvertGameDifficulty(ILconstInt i) {
        return enumHandle("gamedifficulty", i.getVal());
    }

    public IlConstHandle ConvertGameType(ILconstInt i) {
        return enumHandle("gametype", i.getVal());
    }

    public IlConstHandle ConvertMapFlag(ILconstInt i) {
        return enumHandle("mapflag", i.getVal());
    }

    public IlConstHandle ConvertMapVisibility(ILconstInt i) {
        return enumHandle("mapvisibility", i.getVal());
    }

    public IlConstHandle ConvertMapSetting(ILconstInt i) {
        return enumHandle("mapsetting", i.getVal());
    }

    public IlConstHandle ConvertMapDensity(ILconstInt i) {
        return enumHandle("mapdensity", i.getVal());
    }

    public IlConstHandle ConvertMapControl(ILconstInt i) {
        return enumHandle("mapcontrol", i.getVal());
    }

    public IlConstHandle ConvertPlayerColor(ILconstInt i) {
        return enumHandle("playercolor", i.getVal());
    }

    public IlConstHandle ConvertPlayerSlotState(ILconstInt i) {
        return enumHandle("playerslotstate", i.getVal());
    }

    public IlConstHandle ConvertVolumeGroup(ILconstInt i) {
        return enumHandle("volumegroup", i.getVal());
    }

    public IlConstHandle ConvertCameraField(ILconstInt i) {
        return enumHandle("camerafield", i.getVal());
    }

    public IlConstHandle ConvertBlendMode(ILconstInt i) {
        return enumHandle("blendmode", i.getVal());
    }

    public IlConstHandle ConvertRarityControl(ILconstInt i) {
        return enumHandle("raritycontrol", i.getVal());
    }

    public IlConstHandle ConvertTexMapFlags(ILconstInt i) {
        return enumHandle("texmapflags", i.getVal());
    }

    public IlConstHandle ConvertFogState(ILconstInt i) {
        return enumHandle("fogstate", i.getVal());
    }

    public IlConstHandle ConvertEffectType(ILconstInt i) {
        return enumHandle("effecttype", i.getVal());
    }

    public IlConstHandle ConvertVersion(ILconstInt i) {
        return enumHandle("version", i.getVal());
    }

    public IlConstHandle ConvertItemType(ILconstInt i) {
        return enumHandle("itemtype", i.getVal());
    }

    public IlConstHandle ConvertAttackType(ILconstInt i) {
        return enumHandle("attacktype", i.getVal());
    }

    public IlConstHandle ConvertDamageType(ILconstInt i) {
        return enumHandle("damagetype", i.getVal());
    }

    public IlConstHandle ConvertWeaponType(ILconstInt i) {
        return enumHandle("weapontype", i.getVal());
    }

    public IlConstHandle ConvertSoundType(ILconstInt i) {
        return enumHandle("soundtype", i.getVal());
    }

    public IlConstHandle ConvertPathingType(ILconstInt i) {
        return enumHandle("pathingtype", i.getVal());
    }

    public IlConstHandle ConvertMouseButtonType(ILconstInt i) {
        return enumHandle("mousebuttontype", i.getVal());
    }

    public IlConstHandle ConvertDefenseType(ILconstInt i) {
        return enumHandle("defensetype", i.getVal());
    }

    public IlConstHandle ConvertAnimType(ILconstInt i) {
        return enumHandle("animtype", i.getVal());
    }

    public IlConstHandle ConvertSubAnimType(ILconstInt i) {
        return enumHandle("subanimtype", i.getVal());
    }

    public IlConstHandle ConvertOriginFrameType(ILconstInt i) {
        return enumHandle("originframetype", i.getVal());
    }

    public IlConstHandle ConvertFramePointType(ILconstInt i) {
        return enumHandle("framepointtype", i.getVal());
    }

    public IlConstHandle ConvertTextAlignType(ILconstInt i) {
        return enumHandle("textaligntype", i.getVal());
    }

    public IlConstHandle ConvertFrameEventType(ILconstInt i) {
        return enumHandle("frameeventtype", i.getVal());
    }

    public IlConstHandle ConvertOsKeyType(ILconstInt i) {
        return enumHandle("oskeytype", i.getVal());
    }

    public IlConstHandle ConvertAbilityIntegerField(ILconstInt i) {
        return enumHandle("abilityintegerfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityRealField(ILconstInt i) {
        return enumHandle("abilityrealfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityBooleanField(ILconstInt i) {
        return enumHandle("abilitybooleanfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityStringField(ILconstInt i) {
        return enumHandle("abilitystringfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityIntegerLevelField(ILconstInt i) {
        return enumHandle("abilityintegerlevelfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityRealLevelField(ILconstInt i) {
        return enumHandle("abilityreallevelfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityBooleanLevelField(ILconstInt i) {
        return enumHandle("abilitybooleanlevelfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityStringLevelField(ILconstInt i) {
        return enumHandle("abilitystringlevelfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityIntegerLevelArrayField(ILconstInt i) {
        return enumHandle("abilityintegerlevelarrayfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityRealLevelArrayField(ILconstInt i) {
        return enumHandle("abilityreallevelarrayfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityBooleanLevelArrayField(ILconstInt i) {
        return enumHandle("abilitybooleanlevelarrayfield", i.getVal());
    }

    public IlConstHandle ConvertAbilityStringLevelArrayField(ILconstInt i) {
        return enumHandle("abilitystringlevelarrayfield", i.getVal());
    }

    public IlConstHandle ConvertUnitRealField(ILconstInt i) {
        return enumHandle("unitrealfield", i.getVal());
    }

    public IlConstHandle ConvertUnitBooleanField(ILconstInt i) {
        return enumHandle("unitbooleanfield", i.getVal());
    }

    public IlConstHandle ConvertUnitStringField(ILconstInt i) {
        return enumHandle("unitstringfield", i.getVal());
    }

    public IlConstHandle ConvertUnitWeaponRealField(ILconstInt i) {
        return enumHandle("unitweaponrealfield", i.getVal());
    }

    public IlConstHandle ConvertUnitWeaponBooleanField(ILconstInt i) {
        return enumHandle("unitweaponbooleanfield", i.getVal());
    }

    public IlConstHandle ConvertUnitWeaponStringField(ILconstInt i) {
        return enumHandle("unitweaponstringfield", i.getVal());
    }

    public IlConstHandle ConvertItemIntegerField(ILconstInt i) {
        return enumHandle("itemintegerfield", i.getVal());
    }

    public IlConstHandle ConvertItemRealField(ILconstInt i) {
        return enumHandle("itemrealfield", i.getVal());
    }

    public IlConstHandle ConvertItemBooleanField(ILconstInt i) {
        return enumHandle("itembooleanfield", i.getVal());
    }

    public IlConstHandle ConvertItemStringField(ILconstInt i) {
        return enumHandle("itemstringfield", i.getVal());
    }

    public IlConstHandle ConvertMoveType(ILconstInt i) {
        return enumHandle("movetype", i.getVal());
    }

    public IlConstHandle ConvertTargetFlag(ILconstInt i) {
        return enumHandle("targetflag", i.getVal());
    }

    public IlConstHandle ConvertArmorType(ILconstInt i) {
        return enumHandle("armortype", i.getVal());
    }

    public IlConstHandle ConvertHeroAttribute(ILconstInt i) {
        return enumHandle("heroattribute", i.getVal());
    }

    public IlConstHandle ConvertRegenType(ILconstInt i) {
        return enumHandle("regentype", i.getVal());
    }

    public IlConstHandle ConvertUnitCategory(ILconstInt i) {
        return enumHandle("unitcategory", i.getVal());
    }

    public IlConstHandle ConvertPathingFlag(ILconstInt i) {
        return enumHandle("pathingflag", i.getVal());
    }

    public IlConstHandle ConvertFogStyle(ILconstInt i) {
        return enumHandle("fogstyle", i.getVal());
    }

    public IlConstHandle ConvertEquipmentType(ILconstInt i) {
        return enumHandle("equipmentType", i.getVal());
    }

    public IlConstHandle ConvertItemTag(ILconstInt i) {
        return enumHandle("itemTag", i.getVal());
    }

    public IlConstHandle ConvertLoadoutSlot(ILconstInt i) {
        return enumHandle("loadoutslot", i.getVal());
    }
}
