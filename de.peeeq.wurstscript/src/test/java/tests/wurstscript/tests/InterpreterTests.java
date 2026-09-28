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
            // As in game (measured on the 3.0.0 client): the integer part is padded on the left to width - precision.
            "    if R2SW(1116.123, 10, 1) != \"     1116.1\"",
            "        testFail(\"failed B \" + R2SW(1116.123, 10, 1))",
            "    testSuccess()"
        );
    }

    /** What the game prints for a precision this large was not measured; the interpreter says so. */
    @Test(expectedExceptions = {InterpreterException.class})
    public void r2swBeyondTheModelledRange() {
        test().executeProg(true).testLua(false).lines(
            "package Test",
            "native testSuccess()",
            "@extern native R2SW(real r, integer width, integer precision) returns string",
            "init",
            "    if R2SW(1.0, 1, 100) != \"\"",
            "        testSuccess()"
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

    /** Mirrors what the 3.0.0 client does: Locust keeps a unit unselectable after the ability is removed,
     *  until the unit is hidden and shown again, with or without it; hidden, dead and removed units are
     *  unselectable. */
    @Test
    public void unitSelectableAndAliveNatives() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    let u = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    if not BlzIsUnitSelectable(u) or not UnitAlive(u)",
            "        testFail(\"new unit\")",
            "    UnitAddAbility(u, 'Aloc')",
            "    if BlzIsUnitSelectable(u)",
            "        testFail(\"locust\")",
            "    UnitRemoveAbility(u, 'Aloc')",
            "    if BlzIsUnitSelectable(u)",
            "        testFail(\"locust removed\")",
            "    ShowUnit(u, true)",
            "    if BlzIsUnitSelectable(u)",
            "        testFail(\"shown without being hidden\")",
            "    ShowUnit(u, false)",
            "    if BlzIsUnitSelectable(u)",
            "        testFail(\"hidden\")",
            "    ShowUnit(u, true)",
            "    if not BlzIsUnitSelectable(u)",
            "        testFail(\"hidden and shown after locust\")",
            "    let v = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    UnitAddAbility(v, 'Aloc')",
            "    ShowUnit(v, false)",
            "    ShowUnit(v, true)",
            "    if not BlzIsUnitSelectable(v)",
            "        testFail(\"hidden and shown while still locust\")",
            "    SetWidgetLife(u, 0.41)",
            "    if not UnitAlive(u) or IsUnitType(u, UNIT_TYPE_DEAD)",
            "        testFail(\"life just above the death threshold\")",
            "    SetWidgetLife(u, 0.3)",
            "    if UnitAlive(u)",
            "        testFail(\"alive below the death threshold\")",
            "    if BlzIsUnitSelectable(u)",
            "        testFail(\"selectable below the death threshold\")",
            "    if not IsUnitType(u, UNIT_TYPE_DEAD)",
            "        testFail(\"not dead below the death threshold\")",
            "    SetWidgetLife(u, 100.0)",
            "    KillUnit(u)",
            "    if BlzIsUnitSelectable(u) or UnitAlive(u)",
            "        testFail(\"dead\")",
            "    let w = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    RemoveUnit(w)",
            "    if BlzIsUnitSelectable(w)",
            "        testFail(\"removed\")",
            // As in game, where a removed unit reads alive until the removal is reported.
            "    if not UnitAlive(w)",
            "        testFail(\"removed unit alive in the instant it is removed\")",
            "    if BlzIsUnitSelectable(null) or UnitAlive(null)",
            "        testFail(\"null\")",
            "    testSuccess()"
        );
    }

    /** common.j constants are initialised by the game, not by the map script, so the interpreter must
     *  evaluate their ConvertX initialisers itself. */
    @Test
    public void commonJConstantsCarryTheirHandles() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    if UNIT_TYPE_DEAD == null",
            "        testFail(\"dead type null\")",
            "    if UNIT_STATE_MANA == null",
            "        testFail(\"mana state null\")",
            "    let u = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    if IsUnitType(u, UNIT_TYPE_DEAD)",
            "        testFail(\"new unit dead\")",
            // Mana is clamped to the maximum, which a new unit in the mock does not have.
            "    BlzSetUnitMaxMana(u, 50)",
            "    SetUnitState(u, UNIT_STATE_MANA, 42.0)",
            "    if GetUnitState(u, UNIT_STATE_MANA) != 42.0",
            "        testFail(\"mana\")",
            "    if GetUnitState(u, UNIT_STATE_LIFE) != 100.0 or GetWidgetLife(u) != 100.0",
            "        testFail(\"setting mana changed life\")",
            "    SetUnitState(u, UNIT_STATE_LIFE, 70.0)",
            "    if GetUnitState(u, UNIT_STATE_MANA) != 42.0",
            "        testFail(\"setting life changed mana\")",
            // As in game, where a null state is id 0, UNIT_STATE_LIFE.
            "    SetUnitState(u, null, 55.0)",
            "    if GetUnitState(u, UNIT_STATE_LIFE) != 55.0 or GetUnitState(u, null) != 55.0",
            "        testFail(\"a null state is not life\")",
            "    KillUnit(u)",
            "    if not IsUnitType(u, UNIT_TYPE_DEAD)",
            "        testFail(\"killed unit not dead\")",
            "    testSuccess()"
        );
    }

    /** A blizzard.j constant defined from another one, and constants whose ConvertX had no mock. */
    @Test
    public void derivedAndLessCommonConstants() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    if bj_DEGTORAD < 0.0174 or bj_DEGTORAD > 0.0175",
            "        testFail(\"bj_DEGTORAD\")",
            "    if ANIM_TYPE_BIRTH == null or FRAMEPOINT_CENTER == null or ABILITY_BF_HERO_ABILITY == null",
            "        testFail(\"constant null\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client. */
    @Test
    public void stringNativesAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "function measured(string name, string actual, string expected)",
            "    if actual != expected",
            "        testFail(name + \" gave \" + actual)",
            "init",
            "    measured(\"S2I whitespace\", I2S(S2I(\" \\t12\")), \"12\")",
            "    measured(\"S2I sign after whitespace\", I2S(S2I(\"  -4x\")), \"-4\")",
            "    measured(\"S2I above the limit\", I2S(S2I(\"2147483648\")), \"2147483647\")",
            "    measured(\"S2I far above the limit\", I2S(S2I(\"99999999999\")), \"2147483647\")",
            "    measured(\"S2I below the limit\", I2S(S2I(\"-2147483649\")), \"-2147483648\")",
            "    measured(\"S2I no digits\", I2S(S2I(\"abc\")), \"0\")",
            "    measured(\"S2I null\", I2S(S2I(null)), \"0\")",
            "    measured(\"S2R leading point\", R2S(S2R(\".5\")), \"0.500\")",
            "    measured(\"S2R signed leading point\", R2S(S2R(\"-.5\")), \"-0.500\")",
            "    measured(\"S2R whitespace\", R2S(S2R(\" 2.5\")), \"0.000\")",
            "    measured(\"S2R exponent\", R2S(S2R(\"1e3\")), \"1.000\")",
            "    measured(\"S2R too large\", R2S(S2R(\"99999999999999999999999999999999999999999\")), \"0.000\")",
            "    measured(\"R2S one\", R2S(1.), \"1.000\")",
            "    measured(\"R2S third\", R2S(1. / 3.), \"0.333\")",
            "    measured(\"R2S two thirds\", R2S(2. / 3.), \"0.667\")",
            "    measured(\"R2S carry\", R2S(0.9996), \"1.000\")",
            "    measured(\"R2S negative\", R2S(-1.5), \"-1.500\")",
            "    measured(\"R2S negative to zero\", R2S(-0.0004), \"-0.000\")",
            "    measured(\"R2S wraps\", R2S(3000000000.), \"-1294967296.000\")",
            "    measured(\"R2S negative wraps\", R2S(-3000000000.), \"1294967296.000\")",
            "    measured(\"R2S 1e10\", R2S(10000000000.), \"1410065408.000\")",
            // Past 2^56 a real is a multiple of 2^32, so its integer part wraps to 0.
            "    measured(\"R2S and R2SW beyond the long range\", R2S(100000000000000000000.) + \",\" + R2SW(100000000000000000000., 1, 2), \"0.000,0.00\")",
            "    measured(\"R2SW padded\", R2SW(1.5, 8, 2), \"     1.50\")",
            "    measured(\"R2SW negative padded\", R2SW(-1.5, 8, 3), \"-    1.500\")",
            "    measured(\"R2SW precision 0\", R2SW(1.5, 2, 0), \" 2.0\")",
            "    measured(\"R2SW half up\", R2SW(2.5, 1, 0), \"3.0\")",
            "    measured(\"R2SW half up fraction\", R2SW(0.125, 5, 2), \"  0.13\")",
            "    measured(\"R2SW negative to zero\", R2SW(-0.4, 1, 0), \"-0.0\")",
            "    measured(\"R2SW integer\", R2SW(7., 4, 0), \"   7.0\")",
            "    measured(\"R2SW carry\", R2SW(0.9996, 1, 3), \"1.000\")",
            "    measured(\"R2SW smallest width\", R2SW(1., -2147483647 - 1, 1), \"1.0\")",
            "    measured(\"R2I truncates\", I2S(R2I(1.9)) + \",\" + I2S(R2I(-1.9)), \"1,-1\")",
            "    measured(\"R2I wraps\", I2S(R2I(10000000000.)) + \",\" + I2S(R2I(-10000000000.)), \"1410065408,-1410065408\")",
            "    measured(\"SubString end before start\", SubString(\"abcdef\", 3, 1), \"def\")",
            "    measured(\"SubString empty range\", SubString(\"abcdef\", 3, 3), \"\")",
            "    measured(\"SubString at the end\", SubString(\"abcdef\", 6, 8), \"\")",
            "    measured(\"SubString of empty\", SubString(\"\", 0, 0), null)",
            "    measured(\"SubString empty of one\", SubString(\"a\", 0, 0), \"\")",
            "    measured(\"SubString of null\", SubString(null, 0, 1), null)",
            "    measured(\"StringLength null\", I2S(StringLength(null)), \"0\")",
            "    measured(\"StringCase null\", StringCase(null, true), null)",
            "    let hashes = I2S(StringHash(\"hello\")) + \",\" + I2S(StringHash(\"HELLO\")) + \",\" + I2S(StringHash(\"a/b\")) + \",\" + I2S(StringHash(\"a\\\\b\")) + \",\" + I2S(StringHash(\"Wurst\"))",
            "    measured(\"StringHash\", hashes, \"-1801350911,-1801350911,-2092314359,-2092314359,-1436777953\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client. */
    @Test
    public void mathNativesAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "function measured(string name, string actual, string expected)",
            "    if actual != expected",
            "        testFail(name + \" gave \" + actual)",
            "init",
            "    measured(\"SquareRoot negative\", R2S(SquareRoot(-1.)), \"0.000\")",
            "    measured(\"Asin outside\", R2S(Asin(2.)), \"0.000\")",
            "    measured(\"Acos outside\", R2S(Acos(-2.)), \"0.000\")",
            "    measured(\"Pow negative base, fractional exponent\", R2S(Pow(-8., 1. / 3.)) + \",\" + R2S(Pow(-2., 0.5)), \"2.000,1.414\")",
            "    measured(\"Pow negative base, integer exponent\", R2S(Pow(-2., 3.)) + \",\" + R2S(Pow(-2., 2.)), \"-8.000,4.000\")",
            "    measured(\"Pow zero base\", R2S(Pow(0., -1.)) + \",\" + R2S(Pow(0., 0.)), \"0.000,1.000\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client. */
    @Test
    public void hashtableNativesAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "function measured(string name, string actual, string expected)",
            "    if actual != expected",
            "        testFail(name + \" gave \" + actual)",
            "function bs(boolean b) returns string",
            "    return b ? \"true\" : \"false\"",
            "init",
            "    let ht = InitHashtable()",
            "    measured(\"LoadStr missing\", LoadStr(ht, 1, 1), null)",
            "    SaveStr(ht, 1, 1, \"x\")",
            "    SaveStr(ht, 1, 1, null)",
            "    measured(\"null string saved over x\", bs(HaveSavedString(ht, 1, 1)), \"true\")",
            "    measured(\"null string loads\", LoadStr(ht, 1, 1), null)",
            "    RemoveSavedString(ht, 1, 1)",
            "    measured(\"null string removed\", bs(HaveSavedString(ht, 1, 1)), \"false\")",
            "    let u = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    let itm = CreateItem('ratf', 0.0, 0.0)",
            "    SaveUnitHandle(ht, 1, 2, u)",
            "    measured(\"unit through unit, item, widget\", bs(LoadUnitHandle(ht, 1, 2) == u) + \",\" + bs(LoadItemHandle(ht, 1, 2) == null) + \",\" + bs(LoadWidgetHandle(ht, 1, 2) == u), \"true,true,true\")",
            "    SaveItemHandle(ht, 1, 2, itm)",
            "    measured(\"item over unit\", bs(LoadUnitHandle(ht, 1, 2) == null) + \",\" + bs(LoadItemHandle(ht, 1, 2) == itm), \"true,true\")",
            "    SaveUnitHandle(ht, 1, 3, null)",
            "    measured(\"null handle on an empty key\", bs(HaveSavedHandle(ht, 1, 3)), \"false\")",
            "    SaveUnitHandle(ht, 1, 2, null)",
            "    measured(\"null handle over an item\", bs(HaveSavedHandle(ht, 1, 2)) + \",\" + bs(LoadItemHandle(ht, 1, 2) == itm), \"true,true\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client. */
    @Test
    public void conversionNativesAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "function measured(string name, string actual, string expected)",
            "    if actual != expected",
            "        testFail(name + \" gave \" + actual)",
            "function bs(boolean b) returns string",
            "    return b ? \"true\" : \"false\"",
            "init",
            "    measured(\"same value, same handle\", bs(ConvertUnitType(1) == UNIT_TYPE_DEAD) + \",\" + bs(ConvertUnitEvent(5) == ConvertUnitEvent(5)) + \",\" + bs(ConvertUnitType(999) != null), \"true,true,true\")",
            "    let ids = I2S(GetHandleId(UNIT_TYPE_DEAD)) + \",\" + I2S(GetHandleId(UNIT_STATE_MANA)) + \",\" + I2S(GetHandleId(FRAMEPOINT_CENTER)) + \",\" + I2S(GetHandleId(PLAYER_STATE_RESOURCE_GOLD)) + \",\" + I2S(GetHandleId(UNIT_RF_HP)) + \",\" + I2S(GetHandleId(ConvertUnitType(999)))",
            "    measured(\"GetHandleId of conversions\", ids, \"1,2,4,1,1969778787,999\")",
            "    measured(\"GetHandleId of a unit\", bs(GetHandleId(CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)) >= 0x100000), \"true\")",
            "    measured(\"GetHandleId null\", I2S(GetHandleId(null)), \"0\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client. */
    @Test
    public void playerResourcesAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "init",
            "    let p = Player(1)",
            "    SetPlayerState(p, PLAYER_STATE_RESOURCE_GOLD, -5)",
            "    SetPlayerState(p, PLAYER_STATE_RESOURCE_LUMBER, -3)",
            "    if GetPlayerState(p, PLAYER_STATE_RESOURCE_GOLD) != 0 or GetPlayerState(p, PLAYER_STATE_RESOURCE_LUMBER) != 0",
            "        testFail(\"negative resources\")",
            "    SetPlayerState(p, PLAYER_STATE_RESOURCE_GOLD, 5000000)",
            "    if GetPlayerState(p, PLAYER_STATE_RESOURCE_GOLD) != 5000000",
            "        testFail(\"five million gold\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client, except where the mock has no object
     *  data: a new unit has 100 life and no mana. */
    @Test
    public void unitLifeAndManaAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "function measured(string name, string actual, string expected)",
            "    if actual != expected",
            "        testFail(name + \" gave \" + actual)",
            "function bs(boolean b) returns string",
            "    return b ? \"true\" : \"false\"",
            "function states(unit u) returns string",
            "    return R2S(GetUnitState(u, UNIT_STATE_LIFE)) + \"/\" + R2S(GetUnitState(u, UNIT_STATE_MAX_LIFE)) + \" \" + R2S(GetUnitState(u, UNIT_STATE_MANA)) + \"/\" + R2S(GetUnitState(u, UNIT_STATE_MAX_MANA))",
            "function death(unit u) returns string",
            "    return bs(UnitAlive(u)) + \",\" + bs(IsUnitType(u, UNIT_TYPE_DEAD)) + \",\" + R2S(GetWidgetLife(u))",
            "init",
            "    let u = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    measured(\"new\", states(u), \"100.000/100.000 0.000/0.000\")",
            "    SetUnitState(u, UNIT_STATE_LIFE, 1000.0)",
            "    SetWidgetLife(u, 5000.0)",
            "    SetUnitState(u, UNIT_STATE_MANA, 42.0)",
            "    SetUnitState(u, UNIT_STATE_MAX_LIFE, 500.0)",
            "    SetUnitState(u, UNIT_STATE_MAX_MANA, 50.0)",
            "    measured(\"clamped, maximum states not settable\", states(u), \"100.000/100.000 0.000/0.000\")",
            "    SetUnitState(u, UNIT_STATE_LIFE, 90.0)",
            "    BlzSetUnitMaxHP(u, 80)",
            "    measured(\"lower max hp\", states(u), \"80.000/80.000 0.000/0.000\")",
            "    BlzSetUnitMaxHP(u, 200)",
            "    measured(\"higher max hp\", states(u), \"80.000/200.000 0.000/0.000\")",
            "    BlzSetUnitMaxMana(u, 50)",
            "    SetUnitState(u, UNIT_STATE_MANA, 42.0)",
            "    measured(\"mana\", states(u), \"80.000/200.000 42.000/50.000\")",
            "    SetUnitState(u, UNIT_STATE_MANA, 100.0)",
            "    measured(\"mana above max\", states(u), \"80.000/200.000 50.000/50.000\")",
            "    SetUnitState(u, UNIT_STATE_MANA, -5.0)",
            "    measured(\"negative mana\", states(u), \"80.000/200.000 0.000/50.000\")",
            "    SetUnitState(u, UNIT_STATE_MANA, 20.0)",
            "    BlzSetUnitMaxMana(u, 10)",
            "    measured(\"lower max mana\", states(u), \"80.000/200.000 10.000/10.000\")",
            "    measured(\"max getters\", I2S(BlzGetUnitMaxHP(u)) + \",\" + I2S(BlzGetUnitMaxMana(u)), \"200,10\")",
            "    let d = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    SetWidgetLife(d, 0.41)",
            "    measured(\"just above the threshold\", death(d), \"true,false,0.410\")",
            "    SetWidgetLife(d, 0.3)",
            "    measured(\"below the threshold\", death(d), \"false,true,0.000\")",
            "    SetWidgetLife(d, 50.0)",
            "    measured(\"life set after death\", death(d), \"false,true,50.000\")",
            "    let z = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    SetUnitState(z, UNIT_STATE_LIFE, -5.0)",
            "    measured(\"negative life\", death(z), \"false,true,0.000\")",
            "    let k = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    KillUnit(k)",
            "    SetWidgetLife(k, 100.0)",
            "    measured(\"killed, then life set\", death(k), \"false,true,100.000\")",
            // The death rule holds for every life change, also a maximum lowered to 0.
            "    let m = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    BlzSetUnitMaxHP(m, 0)",
            "    measured(\"max hp 0\", death(m), \"false,true,0.000\")",
            "    let rh = CreateUnit(Player(0), 'Hpal', 0.0, 0.0, 0.0)",
            "    BlzSetUnitMaxHP(rh, 0)",
            "    ReviveHero(rh, 0.0, 0.0, false)",
            "    measured(\"revived with max hp 0\", death(rh), \"false,true,0.000\")",
            "    let mm = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    BlzSetUnitMaxMana(mm, 50)",
            "    SetUnitState(mm, UNIT_STATE_MANA, 20.0)",
            "    BlzSetUnitMaxMana(mm, -1)",
            "    measured(\"negative max mana\", R2S(GetUnitState(mm, UNIT_STATE_MANA)), \"0.000\")",
            "    let h = CreateUnit(Player(0), 'Hpal', 0.0, 0.0, 0.0)",
            "    measured(\"hero type and null type\", bs(IsUnitType(h, UNIT_TYPE_HERO)) + \",\" + bs(IsUnitType(h, null)) + \",\" + bs(IsUnitType(u, UNIT_TYPE_HERO)), \"true,true,false\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client. */
    @Test
    public void abilityNativesAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "function measured(string name, string actual, string expected)",
            "    if actual != expected",
            "        testFail(name + \" gave \" + actual)",
            "function bs(boolean b) returns string",
            "    return b ? \"true\" : \"false\"",
            "init",
            "    let u = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    measured(\"add twice\", bs(UnitAddAbility(u, 'AHhb')) + \",\" + bs(UnitAddAbility(u, 'AHhb')), \"true,false\")",
            "    measured(\"set level\", I2S(SetUnitAbilityLevel(u, 'AHhb', 2)) + \",\" + I2S(GetUnitAbilityLevel(u, 'AHhb')), \"2,2\")",
            "    measured(\"set level 0\", I2S(SetUnitAbilityLevel(u, 'AHhb', 0)) + \",\" + I2S(GetUnitAbilityLevel(u, 'AHhb')), \"1,1\")",
            "    measured(\"dec at 1\", I2S(DecUnitAbilityLevel(u, 'AHhb')) + \",\" + I2S(GetUnitAbilityLevel(u, 'AHhb')), \"1,1\")",
            "    measured(\"inc\", I2S(IncUnitAbilityLevel(u, 'AHhb')), \"2\")",
            "    let missing = I2S(SetUnitAbilityLevel(u, 'AHtb', 2)) + \",\" + I2S(IncUnitAbilityLevel(u, 'AHtc')) + \",\" + I2S(DecUnitAbilityLevel(u, 'AHtc'))",
            "    measured(\"missing ability\", missing + \",\" + I2S(GetUnitAbilityLevel(u, 'AHtb')) + \",\" + I2S(GetUnitAbilityLevel(u, 'AHtc')), \"0,0,0,0,0\")",
            "    measured(\"remove twice\", bs(UnitRemoveAbility(u, 'AHhb')) + \",\" + bs(UnitRemoveAbility(u, 'AHhb')), \"true,false\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client, with the default experience table. */
    @Test
    public void heroLevelsAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "function measured(string name, string actual, string expected)",
            "    if actual != expected",
            "        testFail(name + \" gave \" + actual)",
            "function hero(unit h) returns string",
            "    return I2S(GetHeroLevel(h)) + \",\" + I2S(GetHeroXP(h)) + \",\" + I2S(GetHeroSkillPoints(h))",
            "init",
            "    let h = CreateUnit(Player(0), 'Hpal', 0.0, 0.0, 0.0)",
            "    measured(\"new\", hero(h), \"1,0,1\")",
            "    SetHeroXP(h, 150, false)",
            "    measured(\"150 XP\", hero(h), \"1,150,1\")",
            "    SetHeroXP(h, 1000, false)",
            "    measured(\"1000 XP\", hero(h), \"4,1000,4\")",
            "    SetHeroXP(h, 300, false)",
            "    measured(\"lower XP\", hero(h), \"4,1000,4\")",
            "    SetHeroLevel(h, 6, false)",
            "    measured(\"level 6\", hero(h), \"6,2000,6\")",
            "    SetHeroLevel(h, 2, false)",
            "    measured(\"lower level\", hero(h), \"6,2000,6\")",
            "    measured(\"not a hero\", I2S(GetHeroLevel(CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0))), \"0\")",
            "    let big = CreateUnit(Player(0), 'Hpal', 0.0, 0.0, 0.0)",
            "    SetHeroXP(big, 2147483647, false)",
            "    measured(\"largest XP\", I2S(GetHeroXP(big)), \"2147483647\")",
            "    let top = CreateUnit(Player(0), 'Hpal', 0.0, 0.0, 0.0)",
            "    SetHeroLevel(top, 2147483647, false)",
            "    SetHeroXP(top, 1, false)",
            "    SetHeroXP(top, 2147483647, false)",
            "    if GetHeroLevel(top) >= 2147483647 or GetHeroXP(top) <= 0",
            "        testFail(\"largest level\")",
            "    testSuccess()"
        );
    }

    /** Every expected value here was measured on the 3.0.0 client. */
    @Test
    public void groupOrderAsMeasured() {
        test().withStdLib().executeProg(true).testLua(false).lines(
            "package Test",
            "function measured(string name, string actual, string expected)",
            "    if actual != expected",
            "        testFail(name + \" gave \" + actual)",
            "function bs(boolean b) returns string",
            "    return b ? \"true\" : \"false\"",
            "init",
            "    let a = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    let b = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    let c = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    let g = CreateGroup()",
            "    GroupAddUnit(g, c)",
            "    GroupAddUnit(g, a)",
            "    GroupAddUnit(g, b)",
            "    measured(\"creation order\", bs(BlzGroupUnitAt(g, 0) == a) + \",\" + bs(BlzGroupUnitAt(g, 1) == b) + \",\" + bs(BlzGroupUnitAt(g, 2) == c) + \",\" + bs(FirstOfGroup(g) == a), \"true,true,true,true\")",
            "    GroupRemoveUnit(g, a)",
            "    GroupAddUnit(g, a)",
            "    measured(\"removed and added again\", bs(BlzGroupUnitAt(g, 0) == a), \"true\")",
            "    measured(\"add twice, add null\", bs(GroupAddUnit(g, a)) + \",\" + bs(GroupAddUnit(g, null)) + \",\" + I2S(BlzGroupGetSize(g)), \"false,false,3\")",
            "    KillUnit(b)",
            "    measured(\"killed unit stays\", bs(IsUnitInGroup(b, g)), \"true\")",
            "    testSuccess()"
        );
    }

    /** `grill test` and compiletime evaluation run the IM interpreter, which reads the constants too. */
    @Test
    public void commonJConstantsInTestsAndCompiletime() {
        test().withStdLib().executeTests(true).executeProg(true).runCompiletimeFunctions(true).testLua(false).lines(
            "package Test",
            "@compiletime function checkConstants()",
            "    if UNIT_TYPE_DEAD == null or UNIT_STATE_MANA == null",
            "        compileError(\"common.j constant null in a compiletime function\")",
            "constant deadTypeAtCompiletime = compiletime(UNIT_TYPE_DEAD != null)",
            "@test function deadTypeInTest()",
            "    let u = CreateUnit(Player(0), 'hfoo', 0.0, 0.0, 0.0)",
            "    KillUnit(u)",
            "    if not IsUnitType(u, UNIT_TYPE_DEAD)",
            "        testFail(\"killed unit not dead in a test\")",
            "init",
            "    if not deadTypeAtCompiletime",
            "        testFail(\"dead type null in a compiletime expression\")",
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
