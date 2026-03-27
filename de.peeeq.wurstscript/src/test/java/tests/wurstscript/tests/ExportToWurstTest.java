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
public class ExportToWurstTest {

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
