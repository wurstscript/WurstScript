package objEditing.abilities;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class GenAbilitiesTest {
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
