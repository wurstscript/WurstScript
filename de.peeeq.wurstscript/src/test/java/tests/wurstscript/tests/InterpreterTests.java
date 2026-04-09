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
