package de.peeeq.wurstio.jassinterpreter.providers;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JassPrngTest {

    @Test
    public void seed12345MatchesWarcraftIntegerSequence() {
        JassPrng random = new JassPrng();
        random.setRandomSeed(12345);

        assertEquals(random.getRandomInt(0, 1_000_000), 189832);
        assertEquals(random.getRandomInt(0, 1_000_000), 638801);
        assertEquals(random.getRandomInt(0, 1_000_000), 925099);
    }

    @Test
    public void seed12345MatchesWarcraftRealSequence() {
        JassPrng random = new JassPrng();
        random.setRandomSeed(12345);
        random.getRandomInt(0, 1_000_000);
        random.getRandomInt(0, 1_000_000);
        random.getRandomInt(0, 1_000_000);

        assertEquals(Float.floatToRawIntBits(random.getRandomReal(2.245f, 6.532f)), 0x4024339c);
        assertEquals(Float.floatToRawIntBits(random.getRandomReal(2.245f, 6.532f)), 0x408cf668);
        assertEquals(Float.floatToRawIntBits(random.getRandomReal(1.1f, 2.5f)), 0x3fc8be2c);
        assertEquals(Float.floatToRawIntBits(random.getRandomReal(1.1f, 2.5f)), 0x3fa33ff6);
        assertEquals(Float.floatToRawIntBits(random.getRandomReal(-2.1f, 3.14f)), 0xbf7f47cc);
        assertEquals(Float.floatToRawIntBits(random.getRandomReal(-2.1f, 3.14f)), 0x3d12a180);
    }

    @Test
    public void equalBoundsDoNotAdvanceTheGenerator() {
        JassPrng random = new JassPrng();
        random.setRandomSeed(12345);

        assertEquals(random.getRandomInt(7, 7), 7);
        assertEquals(random.getRandomInt(0, 1_000_000), 189832);
    }

    @Test
    public void reversedIntegerBoundsUseTheSameInclusiveRange() {
        JassPrng random = new JassPrng();
        random.setRandomSeed(12345);

        assertEquals(random.getRandomInt(10, 0), 12);
    }
}
