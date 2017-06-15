package de.peeeq.wurstio.objectreader;

public class ObjectHelper {

    public static int objectIdStringToInt(String id) {
        if (id.length() != 4) {
            throw new IllegalArgumentException("id must have length 4");
        }

        int result = 0;
        int factor = 1;
        for (int i = 0; i < 4; i++) {
            int pos = id.charAt(3 - i);
            result += factor * pos;
            factor *= 256;
        }
        return result;
    }

    public static String objectIdIntToString(int value) {
        String result = "";
        int remainingValue = value;
        int charValue;

        for (int byteno = 0; byteno <= 3; byteno++) {
            charValue = remainingValue % 256;
            remainingValue = remainingValue / 256;
            result = ((char) charValue) + result;
        }
        return result;
    }


}
