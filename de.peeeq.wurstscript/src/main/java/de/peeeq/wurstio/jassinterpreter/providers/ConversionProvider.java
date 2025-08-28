package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.IlConstHandle;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

import java.util.LinkedHashSet;

public class ConversionProvider extends Provider {
    public ConversionProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle ConvertRace(ILconstInt i) {
        return new IlConstHandle("race" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertAllianceType(ILconstInt i) {
        return new IlConstHandle("alliancetype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertRacePref(ILconstInt i) {
        return new IlConstHandle("racepreference" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertIGameState(ILconstInt i) {
        return new IlConstHandle("igamestate" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertFGameState(ILconstInt i) {
        return new IlConstHandle("fgamestate" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPlayerState(ILconstInt i) {
        return new IlConstHandle("playerstate" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPlayerScore(ILconstInt i) {
        return new IlConstHandle("playerscore" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPlayerGameResult(ILconstInt i) {
        return new IlConstHandle("playergameresult" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertUnitState(ILconstInt i) {
        return new IlConstHandle("unitstate" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertAIDifficulty(ILconstInt i) {
        return new IlConstHandle("aidifficulty" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertGameEvent(ILconstInt i) {
        return new IlConstHandle("gameevent" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPlayerEvent(ILconstInt i) {
        return new IlConstHandle("playerevent" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPlayerUnitEvent(ILconstInt i) {
        return new IlConstHandle("playerunitevent" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertWidgetEvent(ILconstInt i) {
        return new IlConstHandle("widgetevent" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertDialogEvent(ILconstInt i) {
        return new IlConstHandle("dialogevent" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertUnitEvent(ILconstInt i) {
        return new IlConstHandle("unitevent" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertLimitOp(ILconstInt i) {
        return new IlConstHandle("limitop" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertUnitType(ILconstInt i) {
        return new IlConstHandle("unittype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertGameSpeed(ILconstInt i) {
        return new IlConstHandle("gamespeed" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPlacement(ILconstInt i) {
        return new IlConstHandle("placement" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertStartLocPrio(ILconstInt i) {
        return new IlConstHandle("startlocprio" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertGameDifficulty(ILconstInt i) {
        return new IlConstHandle("gamedifficulty" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertGameType(ILconstInt i) {
        return new IlConstHandle("gametype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertMapFlag(ILconstInt i) {
        return new IlConstHandle("mapflag" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertMapVisibility(ILconstInt i) {
        return new IlConstHandle("mapvisibility" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertMapSetting(ILconstInt i) {
        return new IlConstHandle("mapsetting" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertMapDensity(ILconstInt i) {
        return new IlConstHandle("mapdensity" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertMapControl(ILconstInt i) {
        return new IlConstHandle("mapcontrol" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPlayerColor(ILconstInt i) {
        return new IlConstHandle("playercolor" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPlayerSlotState(ILconstInt i) {
        return new IlConstHandle("playerslotstate" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertVolumeGroup(ILconstInt i) {
        return new IlConstHandle("volumegroup" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertCameraField(ILconstInt i) {
        return new IlConstHandle("camerafield" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertBlendMode(ILconstInt i) {
        return new IlConstHandle("blendmode" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertRarityControl(ILconstInt i) {
        return new IlConstHandle("raritycontrol" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertTexMapFlags(ILconstInt i) {
        return new IlConstHandle("texmapflags" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertFogState(ILconstInt i) {
        return new IlConstHandle("fogstate" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertEffectType(ILconstInt i) {
        return new IlConstHandle("effecttype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertVersion(ILconstInt i) {
        return new IlConstHandle("version" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertItemType(ILconstInt i) {
        return new IlConstHandle("itemtype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertAttackType(ILconstInt i) {
        return new IlConstHandle("attacktype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertDamageType(ILconstInt i) {
        return new IlConstHandle("damagetype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertWeaponType(ILconstInt i) {
        return new IlConstHandle("weapontype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertSoundType(ILconstInt i) {
        return new IlConstHandle("soundtype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertPathingType(ILconstInt i) {
        return new IlConstHandle("pathingtype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertMouseButtonType(ILconstInt i) {
        return new IlConstHandle("mousebuttontype" + i, new LinkedHashSet<>());
    }

    public IlConstHandle ConvertDefenseType(ILconstInt i) {
        return new IlConstHandle("defensetype" + i, new LinkedHashSet<>());
    }
}
