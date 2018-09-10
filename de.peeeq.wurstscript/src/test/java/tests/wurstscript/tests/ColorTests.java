package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.requests.Colors;
import org.eclipse.lsp4j.Color;
import org.eclipse.lsp4j.ColorInformation;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 *
 */
public class ColorTests {

    @Test
    public void testStringColors() {
        String s = "Blubber |cffFF3A29Blub";
        List<ColorInformation> result = new ArrayList<>();
        Colors.collectStringColors(s, result, 0, 0);
        assertEquals(result.size(), 1);
        ColorInformation ci = result.get(0);
        assertEquals(ci.getRange().getStart().getCharacter(), 8);
        assertEquals(ci.getRange().getEnd().getCharacter(), 18);
        assertEquals(Colors.toHex(ci.getColor()), "ffff3a29");
    }

    @Test
    public void testStringColors2() {
        String s = "x|cff000000x|cffffffffx";
        List<ColorInformation> result = new ArrayList<>();
        Colors.collectStringColors(s, result, 0, 0);
        assertEquals(result.size(), 2);
        {
            ColorInformation ci1 = result.get(0);
            assertEquals(Colors.toHex(ci1.getColor()), "ff000000");
            assertEquals(ci1.getRange().getStart().getCharacter(), 1);
            assertEquals(ci1.getRange().getEnd().getCharacter(), 11);
        }
        {
            ColorInformation ci2 = result.get(1);
            assertEquals(Colors.toHex(ci2.getColor()), "ffffffff");
            assertEquals(ci2.getRange().getStart().getCharacter(), 12);
            assertEquals(ci2.getRange().getEnd().getCharacter(), 22);
        }
    }
}
