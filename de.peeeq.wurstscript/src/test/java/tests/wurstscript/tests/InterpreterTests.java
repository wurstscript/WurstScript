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
            "    RemoveUnit(u)",
            "    if GetUnitTypeId(u) != 'hfoo'",
            "        testFail(\"removed handle\")",
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
