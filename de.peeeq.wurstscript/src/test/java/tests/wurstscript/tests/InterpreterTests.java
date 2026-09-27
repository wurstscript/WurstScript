package tests.wurstscript.tests;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import org.testng.annotations.Test;

public class InterpreterTests extends WurstScriptTest {




    @Test
    public void testR2SW() {
        test().executeProg(true).testLua(false).lines(
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "@extern native R2SW(real r, integer width, integer precision) returns string",
            "native println(string s)",
            "init",
            "    if R2SW(1116.0, 2, 2) != \"1116.00\"",
            "        testFail(\"failed A \" + R2SW(1116.0, 2, 2))",
            "    if R2SW(1116.123, 10, 1) != \"1116.1    \"",
            "        testFail(\"failed B \" + R2SW(1116.123, 10, 1))",
            "    testSuccess()"
        );
    }

    @Test(expectedExceptions = {InterpreterException.class})
    public void arrayDefaultTestFail() {
        test().executeProg(true).testLua(false).lines(
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "let ar = [42]",
            "init",
            "    if ar[1] == 0", // Note: interpreter checks array bounds here, even though Jass code does not
            "        testFail(\"should fail\")"
        );
    }

    @Test
    public void arrayDefault() {
        test().executeProg(true).testLua(false).lines(
            "package Test",
            "native testSuccess()",
            "native testFail(string msg)",
            "int array ar",
            "init",
            "    if ar[1] == 0",
            "        testSuccess()"
        );
    }

    @Test
    public void displayNativesAcceptNullForceAndLocalPlayer() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    DisplayTextToForce(null, \"force\")",
            "    DisplayTimedTextToForce(null, 1.0, \"timed force\")",
            "    DisplayTextToPlayer(GetLocalPlayer(), 0.0, 0.0, \"player\")",
            "    DisplayTimedTextToPlayer(GetLocalPlayer(), 0.0, 0.0, 1.0, \"timed player\")",
            "    if GetPlayerId(GetLocalPlayer()) == 0",
            "        testSuccess()"
        );
    }

    @Test
    public void setPlayerTechMaxAllowed() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    SetPlayerTechMaxAllowed(Player(0), 'hfoo', 3)",
            "    if GetPlayerTechMaxAllowed(Player(0), 'hfoo') == 3",
            "        testSuccess()"
        );
    }

    @Test
    public void playerStateAndSlotNatives() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    let gold = ConvertPlayerState(1)",
            "    let lumber = ConvertPlayerState(2)",
            "    let foodCap = ConvertPlayerState(4)",
            "    let foodUsed = ConvertPlayerState(5)",
            "    SetPlayerState(Player(1), gold, 250)",
            "    SetPlayerState(Player(1), lumber, 125)",
            "    SetPlayerState(Player(1), foodCap, 12)",
            "    SetPlayerState(Player(1), foodUsed, 7)",
            "    if GetPlayerState(Player(1), gold) != 250",
            "        testFail(\"gold\")",
            "    if GetPlayerState(Player(1), lumber) != 125",
            "        testFail(\"lumber\")",
            "    if GetPlayerState(Player(1), foodCap) != 12",
            "        testFail(\"food cap\")",
            "    if GetPlayerState(Player(1), foodUsed) != 7",
            "        testFail(\"food used\")",
            "    if GetPlayerSlotState(Player(1)) != ConvertPlayerSlotState(1)",
            "        testFail(\"slot\")",
            "    if GetPlayerController(Player(1)) != ConvertMapControl(0)",
            "        testFail(\"controller\")",
            "    testSuccess()"
        );
    }

    @Test
    public void unitStateAbilityAndOrderNatives() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    let u = CreateUnit(Player(2), 'hfoo', 12.5, -3.25, 90.0)",
            "    let target = CreateUnit(Player(3), 'hbar', 0.0, 0.0, 0.0)",
            "    if GetOwningPlayer(u) != Player(2)",
            "        testFail(\"owner\")",
            "    if GetUnitTypeId(u) != 'hfoo'",
            "        testFail(\"type\")",
            "    if not IsHeroUnitId('Hpal') or IsHeroUnitId('hfoo')",
            "        testFail(\"hero id\")",
            "    if not IsUnitIdType('Hpal', UNIT_TYPE_HERO)",
            "        testFail(\"hero type\")",
            "    if GetUnitX(u) != 12.5",
            "        testFail(\"x\")",
            "    if GetUnitY(u) != -3.25",
            "        testFail(\"y\")",
            "    SetUnitX(u, 7.0)",
            "    SetUnitY(u, 8.0)",
            "    if GetUnitX(u) != 7.0 or GetUnitY(u) != 8.0",
            "        testFail(\"move\")",
            "    SetUnitState(u, UNIT_STATE_LIFE, 33.0)",
            "    if GetUnitState(u, UNIT_STATE_LIFE) != 33.0",
            "        testFail(\"life\")",
            "    if GetWidgetLife(u) != 33.0",
            "        testFail(\"widget life\")",
            "    SetWidgetLife(u, 44.0)",
            "    if GetUnitState(u, UNIT_STATE_LIFE) != 44.0",
            "        testFail(\"set widget life\")",
            "    KillUnit(u)",
            "    if GetWidgetLife(u) != 0.0",
            "        testFail(\"kill\")",
            "    if not UnitAddAbility(u, 'Afoo')",
            "        testFail(\"add ability\")",
            "    if not UnitMakeAbilityPermanent(u, true, 'Afoo')",
            "        testFail(\"make ability permanent\")",
            "    if GetUnitAbilityLevel(u, 'Afoo') != 1",
            "        testFail(\"ability default\")",
            "    if SetUnitAbilityLevel(u, 'Afoo', 3) != 3",
            "        testFail(\"set ability return\")",
            "    if GetUnitAbilityLevel(u, 'Afoo') != 3",
            "        testFail(\"ability level\")",
            "    if not UnitRemoveAbility(u, 'Afoo')",
            "        testFail(\"remove ability\")",
            "    if GetUnitAbilityLevel(u, 'Afoo') != 0",
            "        testFail(\"ability removed\")",
            "    UnitModifySkillPoints(u, 2)",
            "    SelectHeroSkill(u, 'Afoo')",
            "    SelectHeroSkill(u, 'Afoo')",
            "    if GetUnitAbilityLevel(u, 'Afoo') != 2 or GetHeroSkillPoints(u) != 0",
            "        testFail(\"hero skill\")",
            "    if not IssueImmediateOrderById(u, 851971)",
            "        testFail(\"immediate order\")",
            "    if GetUnitCurrentOrder(u) != 851971",
            "        testFail(\"immediate current\")",
            "    if not IssuePointOrderById(u, 851986, 1.0, 2.0)",
            "        testFail(\"point order\")",
            "    if GetUnitCurrentOrder(u) != 851986",
            "        testFail(\"point current\")",
            "    if not IssueTargetOrderById(u, 852000, target)",
            "        testFail(\"target order\")",
            "    if GetUnitCurrentOrder(u) != 852000",
            "        testFail(\"target current\")",
            "    if not IssueImmediateOrder(u, \"move\")",
            "        testFail(\"string order\")",
            "    if GetUnitCurrentOrder(u) != 851986",
            "        testFail(\"string current\")",
            "    if not IssueInstantPointOrder(u, \"thunderbolt\", 1.0, 2.0, target) or GetUnitCurrentOrder(u) != 852095",
            "        testFail(\"instant string order\")",
            "    if IssueInstantTargetOrder(u, \"unknown-order\", target, target)",
            "        testFail(\"unknown instant order\")",
            "    if OrderId(\"move\") != 851986 or OrderId(\"attack\") != 851983 or OrderId(\"thunderbolt\") != 852095 or OrderId(\"unknown-order\") != 0 or OrderId2String(851986) != \"move\"",
            "        testFail(\"order conversion\")",
            "    SetUnitState(target, UNIT_STATE_LIFE, 20.0)",
            "    if not UnitDamagePoint(u, 0.0, 1.0, 0.0, 0.0, 5.0, false, false, ATTACK_TYPE_NORMAL, DAMAGE_TYPE_NORMAL, WEAPON_TYPE_WHOKNOWS)",
            "        testFail(\"point damage\")",
            "    if GetUnitState(target, UNIT_STATE_LIFE) != 15.0",
            "        testFail(\"point damage life\")",
            "    let d = CreateDestructable('B000', 0.0, 0.0, 0.0, 1.0, 0)",
            "    SetDestructableLife(d, 20.0)",
            "    if not UnitDamageTarget(u, d, 5.0, false, false, ATTACK_TYPE_NORMAL, DAMAGE_TYPE_NORMAL, WEAPON_TYPE_WHOKNOWS)",
            "        testFail(\"destructable damage\")",
            "    if GetDestructableLife(d) != 15.0",
            "        testFail(\"destructable damage life\")",
            "    let damageItem = CreateItem('Idmg', 0.0, 0.0)",
            "    SetWidgetLife(damageItem, 20.0)",
            "    if not UnitDamageTarget(u, damageItem, 5.0, false, false, ATTACK_TYPE_NORMAL, DAMAGE_TYPE_NORMAL, WEAPON_TYPE_WHOKNOWS) or GetWidgetLife(damageItem) != 15.0",
            "        testFail(\"item damage\")",
            "    if IsUnitHidden(u)",
            "        testFail(\"hidden default\")",
            "    ShowUnit(u, false)",
            "    if not IsUnitHidden(u)",
            "        testFail(\"hide unit\")",
            "    ShowUnit(u, true)",
            "    if IsUnitHidden(u)",
            "        testFail(\"show unit\")",
            "    if not IsUnitRace(u, GetUnitRace(u))",
            "        testFail(\"race\")",
            "    let sameTypeUnit = CreateUnit(Player(2), 'hfoo', 0.0, 0.0, 0.0)",
            "    if not IsUnitRace(sameTypeUnit, GetUnitRace(u))",
            "        testFail(\"canonical race\")",
            "    SetUnitExploded(u, true)",
            "    if GetUnitTypeId(u) != 'hfoo'",
            "        testFail(\"exploded unit removed\")",
            "    SetUnitPosition(u, 9.0, 10.0)",
            "    if not IsUnitInRangeXY(u, 9.0, 10.0, 0.0)",
            "        testFail(\"range\")",
            "    if not IsUnitOwnedByPlayer(u, Player(2))",
            "        testFail(\"owner predicate\")",
            "    SetUnitFacing(u, 180.0)",
            "    if GetUnitFacing(u) != 180.0",
            "        testFail(\"facing\")",
            "    SetUnitMoveSpeed(u, 280.0)",
            "    if GetUnitMoveSpeed(u) != 280.0",
            "        testFail(\"move speed\")",
            "    if GetUnitDefaultMoveSpeed(u) != 0.0",
            "        testFail(\"default move speed\")",
            "    PauseUnit(u, true)",
            "    if not IsUnitPaused(u)",
            "        testFail(\"pause\")",
            "    SetUnitInvulnerable(u, true)",
            "    if not BlzIsUnitInvulnerable(u)",
            "        testFail(\"invulnerable\")",
            "    SelectUnit(u, true)",
            "    if not IsUnitSelected(u, GetLocalPlayer()) or IsUnitSelected(u, Player(1))",
            "        testFail(\"selection player\")",
            "    ClearSelection()",
            "    if IsUnitSelected(u, GetLocalPlayer())",
            "        testFail(\"clear selection\")",
            "    if not UnitCanSleep(u) or not UnitCanSleepPerm(u)",
            "        testFail(\"sleep default\")",
            "    UnitAddSleep(u, false)",
            "    UnitAddSleepPerm(u, false)",
            "    if UnitCanSleep(u) or UnitCanSleepPerm(u) or UnitIsSleeping(u)",
            "        testFail(\"sleep capability\")",
            "    UnitAddSleep(u, true)",
            "    UnitAddSleepPerm(u, true)",
            "    UnitWakeUp(u)",
            "    if not UnitCanSleep(u) or not UnitCanSleepPerm(u) or UnitIsSleeping(u)",
            "        testFail(\"wake up\")",
            "    let emptyInventoryUnit = CreateUnit(GetLocalPlayer(), 'hfoo', 1.0, 1.0, 0.0)",
            "    let emptyInventoryItem = CreateItem('Iemp', 0.0, 0.0)",
            "    if not UnitAddItem(emptyInventoryUnit, emptyInventoryItem)",
            "        testFail(\"empty inventory add\")",
            "    if not UnitAddItemToSlotById(u, 'Ihi0', 5)",
            "        testFail(\"inventory high slot\")",
            "    let testItem = CreateItem('Ifoo', 0.0, 0.0)",
            "    if not UnitAddItem(u, testItem) or not UnitHasItem(u, testItem)",
            "        testFail(\"inventory add\")",
            "    if UnitItemInSlot(u, 0) != testItem",
            "        testFail(\"inventory slot\")",
            "    UnitRemoveItem(u, testItem)",
            "    if UnitHasItem(u, testItem)",
            "        testFail(\"inventory remove\")",
            "    if not UnitAddItem(u, testItem)",
            "        testFail(\"inventory refill\")",
            "    if not UnitDropItemSlot(u, testItem, 1) or UnitItemInSlot(u, 1) != testItem",
            "        testFail(\"inventory move\")",
            "    if not UnitDropItemTarget(u, testItem, target) or UnitHasItem(u, testItem) or not UnitHasItem(target, testItem)",
            "        testFail(\"inventory transfer\")",
            "    if UnitAddItem(emptyInventoryUnit, testItem)",
            "        testFail(\"duplicate item\")",
            "    if not UnitUseItem(target, testItem) or not UnitHasItem(target, testItem)",
            "        testFail(\"inventory use\")",
            "    UnitRemoveItem(target, testItem)",
            "    UnitRemoveItemFromSlot(u, 5)",
            "    SetResourceAmount(u, 10)",
            "    AddResourceAmount(u, 5)",
            "    if GetResourceAmount(u) != 15",
            "        testFail(\"resource amount\")",
            "    RemoveUnit(u)",
            "    if GetUnitTypeId(u) != 'hfoo'",
            "        testFail(\"removed handle\")",
            "    testSuccess()"
        );
    }

    @Test
    public void getOwningPlayerNullUnitReturnsWurstNull() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    if GetOwningPlayer(null) != null",
            "        testFail(\"owning player null\")",
            "    testSuccess()"
        );
    }

    @Test
    public void destructableWidgetLifeNatives() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    let d = CreateDestructable('LTlt', 1.0, 2.0, 0.0, 1.0, 0)",
            "    if GetWidgetLife(d) != 100.0",
            "        testFail(\"default widget life\")",
            "    SetWidgetLife(d, 72.0)",
            "    if GetWidgetLife(d) != 72.0",
            "        testFail(\"widget life\")",
            "    if GetDestructableLife(d) != 72.0",
            "        testFail(\"destructable life after widget set\")",
            "    SetDestructableLife(d, 55.0)",
            "    if GetWidgetLife(d) != 55.0",
            "        testFail(\"widget life after destructable set\")",
            "    KillDestructable(d)",
            "    if GetWidgetLife(d) != 0.0",
            "        testFail(\"kill destructable\")",
            "    testSuccess()"
        );
    }

    @Test
    public void unitAndAbilityInfoNatives() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "native GetUnitBuildTime(integer unitid) returns integer",
            "init",
            "    let u = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    RemoveUnit(u)",
            "    if GetUnitName(u) == \"hfoo\"",
            "        if GetUnitUserData(u) == 0",
            "            if GetUnitUserData(null) == 0",
            "                if GetUnitGoldCost('hfoo') == 0",
            "                    if GetUnitWoodCost('hfoo') == 0",
            "                        if GetUnitPointValueByType('hfoo') == 0",
            "                            if GetFoodUsed('hfoo') == 0",
            "                                if GetUnitBuildTime('hfoo') == 0",
            "                                    if BlzGetAbilityIcon('AHbz') == \"\"",
            "                                        if BlzGetAbilityExtendedTooltip('AHbz', 1) == \"\"",
            "                                            if BlzGetUnitIntegerField(u, ConvertUnitIntegerField('ubui')) == 0",
            "                                                if BlzGetUnitWeaponIntegerField(u, ConvertUnitWeaponIntegerField('ua1b'), 0) == 0",
            "                                                    if not IsUnitType(u, ConvertUnitType(3))",
            "                                                        testSuccess()"
        );
    }

}
