package objEditing.abilities;

import org.testng.annotations.Test;
import org.testng.SkipException;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GenAbilitiesTest {
    @Test
    public void classHeaderDocumentsRawcodeAndUsesNamedId() {
        assertEquals(GenAbilities.abilityClassHeader("AbilityDefinitionArchMageBlizzard", "blizzard", "AHbz"),
                "\n\n\n/** WC3 ability ID: 'AHbz'. Base ability: AbilityIds.blizzard. */\n"
                        + "public class AbilityDefinitionArchMageBlizzard extends AbilityDefinition\n"
                        + "\tconstruct(int newAbilityId)\n"
                        + "\t\tsuper(newAbilityId, AbilityIds.blizzard)\n");
    }

    @Test
    public void generatedDefinitionsUseDocumentedAbilityIds() throws Exception {
        for (String input : new String[]{"gamedata/abilitydata.slk", "gamedata/abilitymetadata.slk",
                "gamedata/WorldEditStrings.txt", "../../WurstStdlib2/wurst/_wurst/assets/AbilityIds.wurst",
                "../../WurstStdlib2/wurst/objediting/AbilityObjEditing.wurst"}) {
            if (!Files.isRegularFile(Path.of(input))) {
                throw new SkipException("Full generation requires the game snapshot and sibling stdlib: " + input);
            }
        }
        PrintStream originalOut = System.out;
        try (PrintStream quiet = new PrintStream(OutputStream.nullOutputStream())) {
            System.setOut(quiet);
            GenAbilities.main(new String[0]);
            String first = Files.readString(Path.of("AbilityObjEditing.wurst"));
            GenAbilities.main(new String[0]);
            assertEquals(Files.readString(Path.of("AbilityObjEditing.wurst")), first,
                    "Repeated generation must preserve field ordering and names");
        } finally {
            System.setOut(originalOut);
        }

        String standalone = Files.readString(Path.of("AbilityObjEditing.wurst"));
        assertTrue(standalone.contains("import public AbilityIds\n"));
        String idsSource = Files.readString(Path.of("AbilityIds.wurst"));
        assertTrue(idsSource.startsWith("package AbilityIds\n"));
        Map<String, String> ids = new HashMap<>();
        Matcher constants = Pattern.compile("static constant (\\w+)\\s*= '([^']{4})'").matcher(idsSource);
        while (constants.find()) {
            assertEquals(ids.put(constants.group(1), constants.group(2)), null, "Duplicate constant name");
        }
        assertTrue(ids.size() > 1000, "Expected the complete game-data mapping");

        Pattern definitions = Pattern.compile("public class (\\w+) extends AbilityDefinition\\n"
                + "\\tconstruct\\(int newAbilityId\\)\\n\\t\\tsuper\\(newAbilityId, AbilityIds\\.(\\w+)\\)");
        for (String source : new String[]{standalone, Files.readString(Path.of("AbilityObjEditing_additions.wurst"))}) {
            Matcher classes = definitions.matcher(source);
            int count = 0;
            while (classes.find()) {
                String constant = classes.group(2);
                assertTrue(ids.containsKey(constant), "Missing AbilityIds." + constant);
                String documentation = "/** WC3 ability ID: '" + ids.get(constant)
                        + "'. Base ability: AbilityIds." + constant + ". */\n";
                assertTrue(source.regionMatches(classes.start() - documentation.length(),
                                documentation, 0, documentation.length()),
                        "Missing rawcode documentation for " + classes.group(1));
                count++;
            }
            assertEquals(count, ids.size(), "Every ability must have a documented wrapper using its named ID");
            assertTrue(!source.contains("super(newAbilityId, '"), "Raw ability ID in constructor");
        }
    }

    @Test
    public void trimsAbilityDisplayNameFromGeneratedTooltipLabel() {
        GenAbilities.FieldData field = new GenAbilities.FieldData(
                "Hea1", "Hit Points Gained ", "unreal", 1, true);

        assertEquals(field.presetFunction("HitPointsGained", false),
                "\n\tfunction presetHitPointsGained(RealLevelClosure lc)\n"
                        + "\t\tdef.setLevelsDataUnreal(\"Hea1\", lvls, 1, lc)\n"
                        + "\t\taddTooltipProperty(\"Hit Points Gained\", lc)\n");
    }

    @Test
    public void usesWorldEditorLabelForTau2() {
        String displayName = GenAbilities.resolveDisplayName(
                "Tau2", "WESTRING_ABILITYILF_PREFERHOSTILES", "Prefer Hostiles");
        GenAbilities.FieldData field = new GenAbilities.FieldData("Tau2", displayName, "int", 2, true);

        assertEquals(displayName, "Prefer Friendlies");
        assertEquals(field.presetFunction("PreferFriendlies", false),
                "\n\tfunction presetPreferFriendlies(IntLevelClosure lc)\n"
                        + "\t\tdef.setLevelsDataInt(\"Tau2\", lvls, 2, lc)\n"
                        + "\t\taddTooltipProperty(\"Prefer Friendlies\", lc)\n");
    }
}
