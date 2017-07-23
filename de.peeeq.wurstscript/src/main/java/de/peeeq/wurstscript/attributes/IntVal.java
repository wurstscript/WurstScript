package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.utils.Utils;

public class IntVal {

    public static int getValI(ExprIntVal i) {
        String raw = i.getValIraw();
        try {
            if (raw.matches("\\-?[0-9]+")) {
                return Utils.parseInt(raw);
            } else if (raw.toLowerCase().startsWith("0x")) {
                return Utils.parseHexInt(raw, 2);
            } else if (raw.startsWith("$")) {
                return Utils.parseHexInt(raw, 1);
            } else if (raw.startsWith("'")) {
                if (raw.length() == 1 + 2) {
                    return Utils.parseAsciiInt1(raw);
                } else if (raw.length() == 4 + 2) {
                    return Utils.parseAsciiInt4(raw);
                } else {
                    i.addError("Asii ints must have 4 or 1 characters but fount " + (raw.length() - 2) + " characters.");
                }
            }
        } catch (NumberFormatException e) {
            // fall through...
        }
        i.addError("Invalid number: " + raw);
        return 0;
    }

}
