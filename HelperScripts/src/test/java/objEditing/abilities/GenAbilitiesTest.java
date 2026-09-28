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
}
