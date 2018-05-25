package de.peeeq.wurstio.jassinterpreter.providers;

import java.util.Random;

public class NameProvider {
    private static final Random random = new Random();

    public static String getRandomName(String string) {
        return string + random.nextInt(1000);
    }
}
