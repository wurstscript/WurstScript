package tests.wurstscript.tests;

import de.peeeq.wurstio.intermediateLang.interpreter.ProgramStateIO;
import de.peeeq.wurstio.objectreader.ObjectFileType;
import net.moonlightflower.wc3libs.bin.ObjMod;
import net.moonlightflower.wc3libs.bin.app.objMod.*;
import net.moonlightflower.wc3libs.dataTypes.app.War3Int;
import net.moonlightflower.wc3libs.dataTypes.app.War3Real;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Tests for {@link ProgramStateIO#exportToWurst} — verifies that the enriched
 * wrapper-class output is produced for known types and that the raw fallback is
 * used when the base ID has no mapping or a field has no wrapper method.
 */
public class ExportToWurstTest extends WurstScriptTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Adds a level-based (ExtendedMod) field to an object. */
    private static void addLvlMod(ObjMod.Obj obj, String fieldId, ObjMod.ValType type,
                                   int level, int dataPtr, Object value) {
        obj.addMod(new ObjMod.Obj.ExtendedMod(
            MetaFieldId.valueOf(fieldId), type, toDataType(type, value), level, dataPtr));
    }

    /** Adds a non-level (plain Mod) field to an object. */
    private static void addMod(ObjMod.Obj obj, String fieldId, ObjMod.ValType type, Object value) {
        obj.addMod(new ObjMod.Obj.Mod(MetaFieldId.valueOf(fieldId), type, toDataType(type, value)));
    }

    private static net.moonlightflower.wc3libs.dataTypes.DataType toDataType(ObjMod.ValType type, Object value) {
        return switch (type) {
            case INT    -> War3Int.valueOf((int) value);
            case REAL, UNREAL -> War3Real.valueOf((double) value);
            case STRING -> War3String.valueOf((String) value);
        };
    }

    private static String export(ObjMod.Obj obj, ObjectFileType fileType) throws IOException {
        StringBuilder sb = new StringBuilder();
        ProgramStateIO.exportToWurst(List.of(obj), fileType, sb);
        return sb.toString();
    }

    private void assertExportCompiles(String exported, String... imports) {
        String source = """
            package ExportedObjectsCompileTest
            import ObjEditingNatives
            import ObjEditingCommons

            """ + String.join("\n", imports) + "\n\n" + exported;
        testAssertOkLinesWithStdLib(false, source.split("\n"));
    }

    private void assertUnitExportCompilesWithFixedUnitObjEditing(String exported) {
        testAssertOk(false, false,
            compilationUnit("ObjEditingNatives.wurst",
                "package ObjEditingNatives",
                "public class ObjectDefinition",
                "\tfunction setInt(string modification, int value)",
                "\t\tskip",
                "public function createObjectDefinition(string fileType, int newId, int deriveFrom) returns ObjectDefinition",
                "\treturn new ObjectDefinition()"
            ),
            compilationUnit("ObjEditingCommons.wurst",
                "package ObjEditingCommons"
            ),
            compilationUnit("UnitObjEditing.wurst",
                "package UnitObjEditing",
                "import ObjEditingNatives",
                "import ObjEditingCommons",
                "public class UnitDefinition",
                "\tObjectDefinition def",
                "\tconstruct(int newId, int origUnitId)",
                "\t\tdef = createObjectDefinition(\"w3u\", newId, origUnitId)",
                "\tfunction setModelFileExtraVersions(int data)",
                "\t\tdef.setInt(\"uver\", data)"
            ),
            compilationUnit("ExportedObjectsCompileTest.wurst",
                ("package ExportedObjectsCompileTest\n"
                    + "import ObjEditingNatives\n"
                    + "import ObjEditingCommons\n"
                    + "import UnitObjEditing\n\n"
                    + exported).split("\n"))
        );
    }

    // -------------------------------------------------------------------------
    // Ability (w3a) — known base ID
    // -------------------------------------------------------------------------

    @Test
    public void abilityWithKnownBaseIdUsesWrapperClass() throws IOException {
        W3A w3a = new W3A();
        // 'Aslo' = Slow ability -> AbilityDefinitionSlow
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01M"), ObjId.valueOf("Aslo"));
        addLvlMod(obj, "acdn", ObjMod.ValType.UNREAL, 1, 0, 10.0);  // setCooldown

        String out = export(obj, ObjectFileType.ABILITIES);

        assertTrue(out.contains("new AbilityDefinitionSlow('A01M')"),
            "Expected AbilityDefinitionSlow constructor, got:\n" + out);
        assertTrue(out.contains("..setCooldown(1, 10.0)"),
            "Expected setCooldown wrapper call, got:\n" + out);
        assertFalse(out.contains("createObjectDefinition"),
            "Should not fall back to raw format for known base ID");
    }

    @Test
    public void abilityAliasBaseIdsUseAliasSpecificWrapperClasses() throws IOException {
        Object[][] cases = {
            {"A9C1", "ACce", "AbilityDefinitionCleavingAttackCreep", "AbilityDefinitionPitLordCleavingAttack",
                "nca1", ObjMod.ValType.UNREAL, 1, 1, 0.25, "..setDistributedDamageFactor(1, 0.25)"},
            {"A9C2", "ACds", "AbilityDefinitionDivineShieldCreep", "AbilityDefinitionPaladinDivineShield",
                "Hds1", ObjMod.ValType.INT, 1, 1, 1, "..setCanDeactivate(1, true)"},
            {"A9C3", "ANak", "AbilityDefinitionOrbOfAnnihilationQuillSpray", "AbilityDefinitionOrbofAnnihilation",
                "fak1", ObjMod.ValType.UNREAL, 1, 1, 15.0, "..setDamageBonus(1, 15.0)"},
            {"A9C4", "AIhm", "AbilityDefinitionShadowMeldItem", "AbilityDefinitionShadowMeld",
                "Shm1", ObjMod.ValType.UNREAL, 1, 1, 1.5, "..setFadeDuration(1, 1.5)"},
            {"A9C5", "ACah", "AbilityDefinitionThornsAuraCreep", "AbilityDefinitionKeeperoftheGroveThornsAura",
                "Eah1", ObjMod.ValType.UNREAL, 1, 1, 0.1, "..setDamageDealttoAttackers(1, 0.1)"}
        };

        for (Object[] c : cases) {
            W3A w3a = new W3A();
            String newId = (String) c[0];
            String baseId = (String) c[1];
            String expectedClass = (String) c[2];
            String implementationCodeClass = (String) c[3];
            W3A.Obj obj = w3a.addObj(ObjId.valueOf(newId), ObjId.valueOf(baseId));
            addLvlMod(obj, (String) c[4], (ObjMod.ValType) c[5], (int) c[6], (int) c[7], c[8]);

            String out = export(obj, ObjectFileType.ABILITIES);

            assertTrue(out.contains("new " + expectedClass + "('" + newId + "')"), baseId + " export:\n" + out);
            assertTrue(out.contains((String) c[9]), baseId + " export:\n" + out);
            assertFalse(out.contains("new " + implementationCodeClass + "("), baseId + " must not export via implementation-code class:\n" + out);
            assertFalse(out.contains("createObjectDefinition"), baseId + " must not fall back to raw export:\n" + out);
            assertExportCompiles(out, "import AbilityObjEditing");
        }
    }

    @Test
    public void abilityCoverageGapsFromPristineExportUseTypedWrappers() throws IOException {
        W3A w3a = new W3A();
        W3A.Obj resistantSkin = w3a.addObj(ObjId.valueOf("A07B"), ObjId.valueOf("Arsk"));
        addLvlMod(resistantSkin, "arac", ObjMod.ValType.STRING, 0, 0, "human");
        addLvlMod(resistantSkin, "areq", ObjMod.ValType.STRING, 0, 0, "");

        String resistantSkinOut = export(resistantSkin, ObjectFileType.ABILITIES);

        assertTrue(resistantSkinOut.contains("new AbilityDefinitionResistantSkin('A07B')"), resistantSkinOut);
        assertTrue(resistantSkinOut.contains("..setRace(Race.Human)"), resistantSkinOut);
        assertTrue(resistantSkinOut.contains("..setRequirements(\"\")"), resistantSkinOut);
        assertFalse(resistantSkinOut.contains("createObjectDefinition"), resistantSkinOut);

        W3A.Obj undeadBuild = w3a.addObj(ObjId.valueOf("AUbu"), null);
        addLvlMod(undeadBuild, "aart", ObjMod.ValType.STRING, 0, 0,
            "ReplaceableTextures\\CommandButtons\\BTNAdvStruct.blp");

        String undeadBuildOut = export(undeadBuild, ObjectFileType.ABILITIES);

        assertTrue(undeadBuildOut.contains("new AbilityDefinitionBuildUndead('AUbu')"), undeadBuildOut);
        assertTrue(undeadBuildOut.contains("..setIconNormal(\"ReplaceableTextures\\\\CommandButtons\\\\BTNAdvStruct.blp\")"), undeadBuildOut);
        assertFalse(undeadBuildOut.contains("createObjectDefinition"), undeadBuildOut);

        W3A.Obj rally = w3a.addObj(ObjId.valueOf("ARal"), null);
        addLvlMod(rally, "anam", ObjMod.ValType.STRING, 0, 0, "Sync");
        addLvlMod(rally, "atp1", ObjMod.ValType.STRING, 0, 0, "");

        String rallyOut = export(rally, ObjectFileType.ABILITIES);

        assertTrue(rallyOut.contains("new AbilityDefinitionRally('ARal')"), rallyOut);
        assertTrue(rallyOut.contains("..setName(\"Sync\")"), rallyOut);
        assertTrue(rallyOut.contains("..setTooltipNormal(0, \"\")"), rallyOut);
        assertFalse(rallyOut.contains("createObjectDefinition"), rallyOut);
    }

    @Test
    public void abilityDataBaseIdsHaveWrapperCoverageForPreviouslyMissingRows() throws IOException {
        Object[][] cases = {
            {"AOvd", "AbilityDefinitionShadowHunterVoodooo"},
            {"Aaha", "AbilityDefinitionAcolyteHarvest"},
            {"Aawa", "AbilityDefinitionAwaken"},
            {"ANbu", "AbilityDefinitionBuildNeutral"},
            {"AHbu", "AbilityDefinitionBuildHuman"},
            {"AObu", "AbilityDefinitionBuildOrc"},
            {"AEbu", "AbilityDefinitionBuildNightElf"},
            {"AGbu", "AbilityDefinitionBuildNaga"},
            {"ACsp", "AbilityDefinitionCreepSleep"},
            {"Adri", "AbilityDefinitionDropInstant"},
            {"Adro", "AbilityDefinitionDrop"},
            {"Amed", "AbilityDefinitionMeatDrop"},
            {"Amel", "AbilityDefinitionMeatLoad"},
            {"Amic", "AbilityDefinitionMilitiaConversion"},
            {"Apit", "AbilityDefinitionPurchaseItem"},
            {"Arev", "AbilityDefinitionRevive"},
            {"Asac", "AbilityDefinitionSacrificeSacrificialPit"},
            {"Alam", "AbilityDefinitionSacrificeAcolyte"},
            {"Asid", "AbilityDefinitionSellItem"},
            {"Asud", "AbilityDefinitionSellUnit"},
            {"Atol", "AbilityDefinitionTreeOfLifeForAttachingArt"},
            {"AIfl", "AbilityDefinitionFlag"},
            {"AIfm", "AbilityDefinitionFlagHuman"},
            {"AIfo", "AbilityDefinitionFlagOrc"},
            {"AIfn", "AbilityDefinitionFlagNightElf"},
            {"AIfe", "AbilityDefinitionFlagUndead"},
            {"AIso", "AbilityDefinitionSoulTrap"},
            {"Asou", "AbilityDefinitionSoulPossession"},
            {"AIdm", "AbilityDefinitionItemDamageAoe"},
            {"AIvu", "AbilityDefinitionItemInvulNormal"},
            {"AIdg", "AbilityDefinitionItemRitualDaggerInstant"},
            {"AIg2", "AbilityDefinitionItemRitualDaggerRegen"},
            {"AIno", "AbilityDefinitionSlow2"},
            {"AUa2", "AbilityDefinitionDeathKnightAnimateDead1"}
        };

        for (int i = 0; i < cases.length; i++) {
            W3A w3a = new W3A();
            String baseId = (String) cases[i][0];
            String expectedClass = (String) cases[i][1];
            String newId = String.format("Z%03d", i);
            W3A.Obj obj = w3a.addObj(ObjId.valueOf(newId), ObjId.valueOf(baseId));

            String out = export(obj, ObjectFileType.ABILITIES);

            assertTrue(out.contains("new " + expectedClass + "('" + newId + "')"), baseId + " export:\n" + out);
            assertFalse(out.contains("createObjectDefinition"), baseId + " must not fall back to raw export:\n" + out);
        }
    }

    @Test
    public void abilityDataBaseIdsWithSpecificFieldsUseTypedSetters() throws IOException {
        Object[][] cases = {
            {"AIvu", "AIvu", ObjMod.ValType.INT, 1, 1, 1, "..setData(1, true)"},
            {"AIdg", "Idg1", ObjMod.ValType.INT, 1, 2, 1, "..setData(1, true)"},
            {"AIg2", "Ihpg", ObjMod.ValType.INT, 1, 1, 50, "..setHitPointsGained(1, 50)"},
            {"AIno", "Slo1", ObjMod.ValType.UNREAL, 1, 1, 0.5, "..setMovementSpeedFactor(1, 0.5)"},
            {"AUa2", "Uan1", ObjMod.ValType.INT, 1, 1, 6, "..setNumberofCorpsesRaised(1, 6)"}
        };

        for (int i = 0; i < cases.length; i++) {
            W3A w3a = new W3A();
            String baseId = (String) cases[i][0];
            String newId = String.format("Y%03d", i);
            W3A.Obj obj = w3a.addObj(ObjId.valueOf(newId), ObjId.valueOf(baseId));
            addLvlMod(obj, (String) cases[i][1], (ObjMod.ValType) cases[i][2],
                (int) cases[i][3], (int) cases[i][4], cases[i][5]);

            String out = export(obj, ObjectFileType.ABILITIES);

            assertTrue(out.contains((String) cases[i][6]), baseId + " export:\n" + out);
            assertFalse(out.contains("createObjectDefinition"), baseId + " must not fall back to raw export:\n" + out);
        }
    }

    @Test
    public void abilityWrapperIncludesAllKnownFields() throws IOException {
        W3A w3a = new W3A();
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01M"), ObjId.valueOf("Aslo"));
        addLvlMod(obj, "acdn", ObjMod.ValType.UNREAL, 1, 0, 10.0);  // setCooldown
        addLvlMod(obj, "amcs", ObjMod.ValType.INT,   1, 0, 25);      // setManaCost
        addLvlMod(obj, "Slo1", ObjMod.ValType.UNREAL, 1, 1, 0.3);    // setMovementSpeedFactor
        addLvlMod(obj, "Slo2", ObjMod.ValType.UNREAL, 1, 2, 0.3);    // setAttackSpeedFactor

        String out = export(obj, ObjectFileType.ABILITIES);

        assertTrue(out.contains("..setCooldown(1, 10.0)"));
        assertTrue(out.contains("..setManaCost(1, 25)"));
        assertTrue(out.contains("..setMovementSpeedFactor(1, 0.3)"));
        assertTrue(out.contains("..setAttackSpeedFactor(1, 0.3)"));
    }

    @Test
    public void abilityWithUnknownBaseIdFallsBackToRaw() throws IOException {
        W3A w3a = new W3A();
        // 'Axxx' is not a known base ID in the stdlib
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01M"), ObjId.valueOf("Axxx"));
        addLvlMod(obj, "acdn", ObjMod.ValType.UNREAL, 1, 0, 10.0);

        String out = export(obj, ObjectFileType.ABILITIES);

        assertTrue(out.contains("createObjectDefinition(\"w3a\", 'A01M', 'Axxx')"),
            "Unknown base ID should fall back to raw format");
        assertFalse(out.contains("new Ability"), "Should not emit a wrapper class for unknown base ID");
    }

    @Test
    public void abilityWithUnmappedFieldEmitsCommentedRawCall() throws IOException {
        W3A w3a = new W3A();
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01M"), ObjId.valueOf("Aslo"));
        addLvlMod(obj, "acdn", ObjMod.ValType.UNREAL, 1, 0, 5.0);  // mapped
        // "zzzz" is not a real field — has no wrapper method
        addLvlMod(obj, "zzzz", ObjMod.ValType.INT, 1, 0, 42);

        String out = export(obj, ObjectFileType.ABILITIES);

        // Should still use the wrapper class (not fall back to raw entirely)
        assertTrue(out.contains("new AbilityDefinitionSlow('A01M')"),
            "Wrapper class should still be used even with one unmapped field");
        assertTrue(out.contains("..setCooldown(1, 5.0)"),
            "Mapped fields should still emit wrapper calls");
        // Unmapped field should appear as a comment, not a live call
        assertTrue(out.contains("// TODO no wrapper:"),
            "Unmapped field should be commented out");
        assertTrue(out.contains("zzzz"),
            "Unmapped field ID should appear in the comment");
        assertFalse(out.contains("createObjectDefinition"),
            "Should not fall back to full raw format");
    }

    @Test
    public void abilityWursMarkerFieldIsAlwaysExcluded() throws IOException {
        W3A w3a = new W3A();
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01M"), ObjId.valueOf("Aslo"));
        addLvlMod(obj, "wurs", ObjMod.ValType.INT, 0, 0, 1);  // internal marker
        addLvlMod(obj, "acdn", ObjMod.ValType.UNREAL, 1, 0, 5.0);

        String out = export(obj, ObjectFileType.ABILITIES);

        assertFalse(out.contains("wurs"), "Internal 'wurs' marker should never appear in output");
        // "wurs" excluded, acdn is known -> should still use wrapper
        assertTrue(out.contains("new AbilityDefinitionSlow"), "Wrapper should be used when only 'wurs' is unmapped");
    }

    @Test
    public void abilityMeleeOverrideUsesSameIdFormat() throws IOException {
        W3A w3a = new W3A();
        // Melee override: same base and new ID
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("Aslo"), null);
        addLvlMod(obj, "acdn", ObjMod.ValType.UNREAL, 1, 0, 3.0);

        String out = export(obj, ObjectFileType.ABILITIES);

        // For melee overrides old/new id are both the same - check either format is valid
        assertTrue(out.contains("Aslo"), "Melee override should reference the ability ID");
    }

    @Test
    public void abilityBoolFieldEmitsTrueFalse() throws IOException {
        W3A w3a = new W3A();
        // 'AHav' = Avatar -> AbilityDefinitionMountainKingAvatar (has bool fields)
        // Use 'Aslo' which has Slo3 (setAlwaysAutocast, bool field)
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01M"), ObjId.valueOf("Aslo"));
        addLvlMod(obj, "Slo3", ObjMod.ValType.INT, 1, 3, 1);  // setAlwaysAutocast(1, true)

        String out = export(obj, ObjectFileType.ABILITIES);

        assertTrue(out.contains("..setAlwaysAutocast(1, true)"),
            "Bool field stored as int 1 should emit 'true', got:\n" + out);
    }

    @Test
    public void abilityEnumFieldEmitsEnumConstantAndCompiles() throws IOException {
        W3A w3a = new W3A();
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01N"), ObjId.valueOf("Aslo"));
        addLvlMod(obj, "arac", ObjMod.ValType.STRING, 0, 0, "orc");

        String out = export(obj, ObjectFileType.ABILITIES);

        assertTrue(out.contains("..setRace(Race.Orc)"), out);
        assertExportCompiles(out, "import AbilityObjEditing");
    }

    @Test
    public void abilityUnknownEnumFieldFallsBackToRawObjectExport() throws IOException {
        W3A w3a = new W3A();
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01O"), ObjId.valueOf("Aslo"));
        addLvlMod(obj, "arac", ObjMod.ValType.STRING, 0, 0, "not-a-race");

        String out = export(obj, ObjectFileType.ABILITIES);

        assertTrue(out.contains("createObjectDefinition(\"w3a\", 'A01O', 'Aslo')"), out);
        assertTrue(out.contains("..setLvlDataString(\"arac\", 0, 0, \"not-a-race\")"), out);
        assertFalse(out.contains("new AbilityDefinitionSlow('A01O')"), out);
        assertFalse(out.contains("// TODO no wrapper:"), out);
    }

    @Test
    public void abilityUnsupportedMappedWrapperParameterFallsBackToRawExport() throws IOException {
        W3A w3a = new W3A();
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A0RJ"), ObjId.valueOf("Arej"));
        addLvlMod(obj, "acdn", ObjMod.ValType.UNREAL, 1, 0, 4.0);
        addLvlMod(obj, "Rej3", ObjMod.ValType.INT, 1, 3, 1);

        String out = export(obj, ObjectFileType.ABILITIES);

        assertTrue(out.contains("createObjectDefinition(\"w3a\", 'A0RJ', 'Arej')"), out);
        assertTrue(out.contains("..setLvlDataUnreal(\"acdn\", 1, 0, 4.0)"), out);
        assertTrue(out.contains("..setLvlDataInt(\"Rej3\", 1, 3, 1)"), out);
        assertFalse(out.contains("new AbilityDefinitionRejuvination('A0RJ')"), out);
        assertFalse(out.contains("// TODO no wrapper:"), out);
    }

    // -------------------------------------------------------------------------
    // Unit (w3u)
    // -------------------------------------------------------------------------

    @Test
    public void unitUsesUnitDefinitionWrapper() throws IOException {
        W3U w3u = new W3U();
        // Lowercase new ID → regular unit (uppercase would be hero by convention)
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("n001"), ObjId.valueOf("hfoo"));
        addMod(obj, "unam", ObjMod.ValType.STRING, "My Unit");
        addMod(obj, "uhpm", ObjMod.ValType.INT, 1500);  // setHitPointsMaximumBase

        String out = export(obj, ObjectFileType.UNITS);

        assertTrue(out.contains("new UnitDefinition('n001', 'hfoo')"),
            "Units with lowercase new ID should use UnitDefinition wrapper, got:\n" + out);
        assertTrue(out.contains("..setName(\"My Unit\")"));
        assertFalse(out.contains("createObjectDefinition"));
    }

    @Test
    public void unitWithUnmappedFieldEmitsCommentedRaw() throws IOException {
        W3U w3u = new W3U();
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("n001"), ObjId.valueOf("hfoo"));
        addMod(obj, "unam", ObjMod.ValType.STRING, "My Unit");
        addMod(obj, "zzzz", ObjMod.ValType.STRING, "unknown");

        String out = export(obj, ObjectFileType.UNITS);

        assertTrue(out.contains("new UnitDefinition('n001', 'hfoo')"),
            "UnitDefinition wrapper should still be used");
        assertTrue(out.contains("..setName(\"My Unit\")"));
        assertTrue(out.contains("// TODO no wrapper:"),
            "Unmapped field should be commented out");
        assertFalse(out.contains("createObjectDefinition"));
    }

    @Test
    public void unitEnumFieldsEmitEnumConstantsAndCompile() throws IOException {
        W3U w3u = new W3U();
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("n010"), ObjId.valueOf("hfoo"));
        addMod(obj, "unam", ObjMod.ValType.STRING, "Enum Unit");
        addMod(obj, "udty", ObjMod.ValType.STRING, "small");
        addMod(obj, "ua1t", ObjMod.ValType.STRING, "magic");
        addMod(obj, "ua1w", ObjMod.ValType.STRING, "instant");
        addMod(obj, "umvt", ObjMod.ValType.STRING, "foot");
        addMod(obj, "ucs1", ObjMod.ValType.STRING, "AxeMediumChop");
        addMod(obj, "uarm", ObjMod.ValType.STRING, "Metal");
        addMod(obj, "urac", ObjMod.ValType.STRING, "orc");
        addMod(obj, "uacq", ObjMod.ValType.UNREAL, 2.1474836E9);

        String out = export(obj, ObjectFileType.UNITS);

        assertTrue(out.contains("..setArmorType(ArmorType.Small)"), out);
        assertTrue(out.contains("..setAttack1AttackType(AttackType.Magic)"), out);
        assertTrue(out.contains("..setAttack1WeaponType(WeaponType.Instant)"), out);
        assertTrue(out.contains("..setMovementType(MovementType.Foot)"), out);
        assertTrue(out.contains("..setAttack1WeaponSound(WeaponSound.AxeMediumChop)"), out);
        assertTrue(out.contains("..setArmorSoundType(ArmorSoundType.Metal)"), out);
        assertTrue(out.contains("..setRace(Race.Orc)"), out);
        assertTrue(out.contains("..setAcquisitionRange(2147483600.0)"), out);
        assertFalse(out.contains("2.1474836E9"), out);
        assertExportCompiles(out, "import UnitObjEditing");
    }

    @Test
    public void emptyWeaponSoundEnumValueKeepsTypedExportAndCompiles() throws IOException {
        W3U w3u = new W3U();
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("n011"), ObjId.valueOf("hfoo"));
        addMod(obj, "ucs1", ObjMod.ValType.STRING, "");

        String out = export(obj, ObjectFileType.UNITS);

        assertTrue(out.contains("new UnitDefinition('n011', 'hfoo')"), out);
        assertTrue(out.contains("..setAttack1WeaponSound(WeaponSound.Nothing)"), out);
        assertFalse(out.contains("createObjectDefinition"), out);
        assertExportCompiles(out, "import UnitObjEditing");
    }

    @Test
    public void intFieldWithFixedWrapperEmitsWrapperCallAndCompiles() throws IOException {
        W3U w3u = new W3U();
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("n012"), ObjId.valueOf("hfoo"));
        addMod(obj, "uver", ObjMod.ValType.INT, 1);

        String out = export(obj, ObjectFileType.UNITS);

        assertFalse(out.contains("createObjectDefinition"), out);
        assertTrue(out.contains("..setModelFileExtraVersions(1)"), out);
        assertUnitExportCompilesWithFixedUnitObjEditing(out);
    }

    @Test
    public void buildingBaseIdUsesBuildingDefinition() throws IOException {
        W3U w3u = new W3U();
        // 'hhou' = Human Farm — a stock building (isbldg=1 in unitbalance.slk)
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("H002"), ObjId.valueOf("hhou"));
        addMod(obj, "unam", ObjMod.ValType.STRING, "My Farm");
        addMod(obj, "uubs", ObjMod.ValType.STRING, "HSMA");  // setGroundTexture — building-only

        String out = export(obj, ObjectFileType.UNITS);

        assertTrue(out.contains("new BuildingDefinition('H002', 'hhou')"),
            "Building base ID should use BuildingDefinition, got:\n" + out);
        assertTrue(out.contains("..setGroundTexture(\"HSMA\")"),
            "Building-only field uubs should map to setGroundTexture, got:\n" + out);
        assertFalse(out.contains("// TODO no wrapper"),
            "No unmapped fields expected for building with known fields, got:\n" + out);
    }

    @Test
    public void buildingFieldsResearchesPathingMap() throws IOException {
        W3U w3u = new W3U();
        // 'hgtw' = Guard Tower — building
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("H003"), ObjId.valueOf("hgtw"));
        addMod(obj, "ures", ObjMod.ValType.STRING, "");    // setResearchesAvailable
        addMod(obj, "upat", ObjMod.ValType.STRING, "PathTextures\\4x4SimpleSolid.tga");  // setPathingMap

        String out = export(obj, ObjectFileType.UNITS);

        assertTrue(out.contains("new BuildingDefinition('H003', 'hgtw')"),
            "hgtw should use BuildingDefinition, got:\n" + out);
        assertTrue(out.contains("..setResearchesAvailable(\"\")"),
            "ures should map to setResearchesAvailable, got:\n" + out);
        assertTrue(out.contains("..setPathingMap("),
            "upat should map to setPathingMap, got:\n" + out);
        assertFalse(out.contains("// TODO no wrapper"),
            "No unmapped fields expected, got:\n" + out);
    }

    @Test
    public void heroBaseIdUsesHeroDefinition() throws IOException {
        W3U w3u = new W3U();
        // 'Hamg' = Archmage — a stock hero
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("H004"), ObjId.valueOf("Hamg"));
        addMod(obj, "unam", ObjMod.ValType.STRING, "My Archmage");
        addMod(obj, "uint", ObjMod.ValType.INT, 20);   // setStartingIntelligence — hero-only
        addMod(obj, "upra", ObjMod.ValType.STRING, "INT"); // setPrimaryAttribute — hero-only

        String out = export(obj, ObjectFileType.UNITS);

        assertTrue(out.contains("new HeroDefinition('H004', 'Hamg')"),
            "Hero base ID should use HeroDefinition, got:\n" + out);
        assertTrue(out.contains("..setStartingIntelligence(20)"),
            "Hero-only uint field should map to setStartingIntelligence, got:\n" + out);
        assertFalse(out.contains("// TODO no wrapper"),
            "No unmapped fields expected for hero with known fields, got:\n" + out);
    }

    @Test
    public void uppercaseNewIdWithUnitBaseUsesHeroDefinition() throws IOException {
        W3U w3u = new W3U();
        // Custom hero ID starting with uppercase (e.g. 'X00O') based on a non-hero unit (hpea).
        // WC3 convention: uppercase new ID = hero unit.
        W3U.Obj obj = w3u.addObj(ObjId.valueOf("X00O"), ObjId.valueOf("hpea"));
        addMod(obj, "unam", ObjMod.ValType.STRING, "Custom Hero");
        addMod(obj, "upro", ObjMod.ValType.STRING, "Race Builder");  // setProperNames — hero-only
        addMod(obj, "upru", ObjMod.ValType.INT, 1);                  // setProperNamesUsed

        String out = export(obj, ObjectFileType.UNITS);

        assertTrue(out.contains("new HeroDefinition('X00O', 'hpea')"),
            "Uppercase new ID should use HeroDefinition even with unit base, got:\n" + out);
        assertFalse(out.contains("// TODO no wrapper"),
            "upro/upru should map via HeroDefinition, got:\n" + out);
    }

    // -------------------------------------------------------------------------
    // Buff (w3h)
    // -------------------------------------------------------------------------

    @Test
    public void buffUsesBuffDefinitionWrapper() throws IOException {
        W3H w3h = new W3H();
        W3H.Obj obj = w3h.addObj(ObjId.valueOf("B001"), ObjId.valueOf("Basl"));
        addLvlMod(obj, "fnam", ObjMod.ValType.STRING, 1, 0, "My Buff");  // setName

        String out = export(obj, ObjectFileType.BUFFS);

        assertTrue(out.contains("new BuffDefinition('B001', 'Basl')"),
            "Buffs should use BuffDefinition wrapper, got:\n" + out);
        assertTrue(out.contains("..setName(1, \"My Buff\")"));
        assertFalse(out.contains("createObjectDefinition"));
    }

    // -------------------------------------------------------------------------
    // Item (w3t)
    // -------------------------------------------------------------------------

    @Test
    public void itemUsesItemDefinitionWrapper() throws IOException {
        W3T w3t = new W3T();
        W3T.Obj obj = w3t.addObj(ObjId.valueOf("I001"), ObjId.valueOf("rat9"));
        addMod(obj, "unam", ObjMod.ValType.STRING, "My Item");

        String out = export(obj, ObjectFileType.ITEMS);

        assertTrue(out.contains("new ItemDefinition('I001', 'rat9')"),
            "Items should use ItemDefinition wrapper, got:\n" + out);
        assertTrue(out.contains("..setName(\"My Item\")"));
        assertFalse(out.contains("createObjectDefinition"));
    }

    @Test
    public void itemEnumFieldEmitsEnumConstantAndCompiles() throws IOException {
        W3T w3t = new W3T();
        W3T.Obj obj = w3t.addObj(ObjId.valueOf("I002"), ObjId.valueOf("rat9"));
        addMod(obj, "iarm", ObjMod.ValType.STRING, "small");

        String out = export(obj, ObjectFileType.ITEMS);

        assertTrue(out.contains("..setArmorType(ArmorType.Small)"), out);
        assertExportCompiles(out, "import ItemObjEditing");
    }

    // -------------------------------------------------------------------------
    // Inherited fields — child abilities that inherit specific fields from parent
    // -------------------------------------------------------------------------

    @Test
    public void feedbackArcaneTowerInheritsParentFields() throws IOException {
        W3A w3a = new W3A();
        // 'Afbt' (Feedback Arcane Tower) inherits fbk1-4 from parent 'Afbk' (Feedback)
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A075"), ObjId.valueOf("Afbt"));
        addLvlMod(obj, "fbk1", ObjMod.ValType.UNREAL, 1, 1, 60.0);  // setMaxManaDrainedUnits
        addLvlMod(obj, "fbk2", ObjMod.ValType.UNREAL, 1, 2, 2.2);   // setDamageRatioUnits
        addLvlMod(obj, "fbk3", ObjMod.ValType.UNREAL, 1, 3, 60.0);  // setMaxManaDrainedHeros
        addLvlMod(obj, "fbk4", ObjMod.ValType.UNREAL, 1, 4, 2.2);   // setDamageRatioHeros
        addLvlMod(obj, "fbk5", ObjMod.ValType.UNREAL, 1, 5, 10.0);  // setSummonedDamage

        String out = export(obj, ObjectFileType.ABILITIES);

        assertFalse(out.contains("createObjectDefinition"),
            "Afbt should use wrapper, not raw fallback");
        assertTrue(out.contains("new AbilityDefinitionFeedbackArcaneTower('A075')"),
            "Should use AbilityDefinitionFeedbackArcaneTower, got:\n" + out);
        assertFalse(out.contains("// TODO no wrapper"),
            "All fbk1-5 fields should be mapped, got:\n" + out);
        assertTrue(out.contains("..setMaxManaDrainedUnits(1, 60.0)"), "fbk1 should map to setMaxManaDrainedUnits");
        assertTrue(out.contains("..setDamageRatioUnits(1, 2.2)"),     "fbk2 should map to setDamageRatioUnits");
        assertTrue(out.contains("..setMaxManaDrainedHeros(1, 60.0)"), "fbk3 should map to setMaxManaDrainedHeros");
        assertTrue(out.contains("..setDamageRatioHeros(1, 2.2)"),     "fbk4 should map to setDamageRatioHeros");
        assertTrue(out.contains("..setSummonedDamage(1, 10.0)"),      "fbk5 should map to setSummonedDamage");
    }

    @Test
    public void parasiteEredarInheritsParentFields() throws IOException {
        W3A w3a = new W3A();
        // 'ACpa' (Parasite Eredar) inherits Npa6, Poi1, Poi4, ipmu from parent 'ANpa'
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A017"), ObjId.valueOf("ACpa"));
        addLvlMod(obj, "Npa6", ObjMod.ValType.UNREAL,  1, 0, 0.01);
        addLvlMod(obj, "Poi1", ObjMod.ValType.UNREAL,  1, 1, 0.0);
        addLvlMod(obj, "Poi4", ObjMod.ValType.INT,     1, 4, 0);
        addLvlMod(obj, "ipmu", ObjMod.ValType.STRING,  1, 0, "nfbr");

        String out = export(obj, ObjectFileType.ABILITIES);

        assertFalse(out.contains("createObjectDefinition"),
            "ACpa should use wrapper, not raw fallback");
        assertFalse(out.contains("// TODO no wrapper"),
            "All ACpa fields should be mapped via inheritance, got:\n" + out);
        assertTrue(out.contains("..setStackingType(1, 0)"), out);
        assertExportCompiles(out, "import AbilityObjEditing");
    }

    @Test
    public void abilityIntegerLevelFieldsWithWrapperMethodsCompile() throws IOException {
        Object[][] cases = {
            // baseId, fieldId, dataPtr, wrapper class, setter method, newId
            {"ACpa", "Poi4", 4, "AbilityDefinitionParasiteEredar", "setStackingType", "Zp04"},
            {"AIsz", "Spo4", 4, "AbilityDefinitionSlowPoisonItem", "setStackingType", "Zs04"},
            {"Abu5", "Eme2", 2, "AbilityDefinitionBurrowBarbedArachnathid", "setMorphingFlags", "Ze02"},
            {"Acdh", "Nsi1", 1, "AbilityDefinitionChenDrunkenHaze", "setAttacksPrevented", "Zn01"},
            {"AHca", "Hca4", 4, "AbilityDefinitionRangerColdArrows", "setStackFlags", "Zh04"},
        };

        for (Object[] c : cases) {
            W3A w3a = new W3A();
            W3A.Obj obj = w3a.addObj(ObjId.valueOf((String) c[5]), ObjId.valueOf((String) c[0]));
            addLvlMod(obj, (String) c[1], ObjMod.ValType.INT, 1, (int) c[2], 0);

            String out = export(obj, ObjectFileType.ABILITIES);

            assertTrue(out.contains("new " + c[3] + "('" + c[5] + "')"), out);
            assertTrue(out.contains(".." + c[4] + "(1, 0)"), out);
            assertExportCompiles(out, "import AbilityObjEditing");
        }
    }

    // -------------------------------------------------------------------------
    // 3-char field IDs (e.g. "Crs" for Curse chance-to-miss)
    // -------------------------------------------------------------------------

    @Test
    public void threeCharFieldIdIsMapped() throws IOException {
        W3A w3a = new W3A();
        // 'Acrs' = Curse -> has "Crs" (3-char) field for chance to miss
        W3A.Obj obj = w3a.addObj(ObjId.valueOf("A01G"), ObjId.valueOf("Acrs"));
        addLvlMod(obj, "Crs", ObjMod.ValType.UNREAL, 1, 1, 0.35);

        String out = export(obj, ObjectFileType.ABILITIES);

        assertFalse(out.contains("// TODO no wrapper"),
            "3-char field 'Crs' should be mapped, got:\n" + out);
        assertTrue(out.contains("..setChancetoMiss(1, 0.35)"),
            "Crs should map to setChancetoMiss, got:\n" + out);
    }

    // -------------------------------------------------------------------------
    // Unsupported types (w3b doodads, w3q upgrades) → always raw
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Destructable (w3b)
    // -------------------------------------------------------------------------

    @Test
    public void destructableUsesDestructableDefinitionWrapper() throws IOException {
        W3B w3b = new W3B();
        W3B.Obj obj = w3b.addObj(ObjId.valueOf("B001"), ObjId.valueOf("DTfr"));
        addMod(obj, "bnam", ObjMod.ValType.STRING, "My Wall");
        addMod(obj, "bhps", ObjMod.ValType.UNREAL, 20000.0);  // setHitPoints
        addMod(obj, "bvar", ObjMod.ValType.INT, 1);            // setNumVariations

        String out = export(obj, ObjectFileType.DESTRUCTABLES);

        assertTrue(out.contains("new DestructableDefinition('B001', 'DTfr')"),
            "Destructables should use DestructableDefinition wrapper, got:\n" + out);
        assertTrue(out.contains("..setName(\"My Wall\")"),
            "bnam should map to setName, got:\n" + out);
        assertTrue(out.contains("..setHitPoints(20000.0)"),
            "bhps should map to setHitPoints, got:\n" + out);
        assertTrue(out.contains("..setNumVariations(1)"),
            "bvar should map to setNumVariations, got:\n" + out);
        assertFalse(out.contains("createObjectDefinition"),
            "Should not fall back to raw format");
    }

    // -------------------------------------------------------------------------
    // Doodad (w3d)
    // -------------------------------------------------------------------------

    @Test
    public void doodadUsesDoodadDefinitionWrapper() throws IOException {
        W3D w3d = new W3D();
        W3D.Obj obj = w3d.addObj(ObjId.valueOf("D001"), ObjId.valueOf("YSw0"));
        addLvlMod(obj, "dnam", ObjMod.ValType.STRING, 0, 0, "My Wall");
        addLvlMod(obj, "dmas", ObjMod.ValType.UNREAL, 0, 0, 1.1);  // setMaximumScale
        addLvlMod(obj, "dptx", ObjMod.ValType.STRING, 0, 0, "PathTextures\\StoneWall3Path.tga");

        String out = export(obj, ObjectFileType.DOODADS);

        assertTrue(out.contains("new DoodadDefinition('D001', 'YSw0')"),
            "Doodads should use DoodadDefinition wrapper, got:\n" + out);
        assertTrue(out.contains("..setName(0, \"My Wall\")"),
            "dnam should map to setName with level, got:\n" + out);
        assertTrue(out.contains("..setMaximumScale(0, 1.1)"),
            "dmas should map to setMaximumScale with level, got:\n" + out);
        assertFalse(out.contains("createObjectDefinition"),
            "Should not fall back to raw format");
    }
}
