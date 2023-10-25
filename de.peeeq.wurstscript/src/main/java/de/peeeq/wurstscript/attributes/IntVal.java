package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.utils.Utils;

import java.util.regex.Pattern;

public class IntVal {

    private static final Pattern INT_PATTERN =Pattern.compile("-?(0|[1-9][0-9]*)");
    private static final Pattern OINT_PATTERN =Pattern.compile("-?0[0-9]+");
    private static final Pattern HINT_PATTERN =Pattern.compile("-?0x[0-9a-fA-F]+");
    private static final Pattern HINT_PATTERN2 =Pattern.compile("-?\\$[0-9a-fA-F]+");
    public static int getValI(ExprIntVal i) {
        String raw = i.getValIraw();
        try {
            if (INT_PATTERN.matcher(raw).matches()) {
                return Utils.parseInt(raw);
            } else if (OINT_PATTERN.matcher(raw).matches()) {
                return Utils.parseOctalInt(raw);
            } else if (HINT_PATTERN.matcher(raw).matches()) {
                return Utils.parseHexInt(raw, 2);
            } else if (HINT_PATTERN2.matcher(raw).matches()) {
                return Utils.parseHexInt(raw, 1);
            } else if (raw.startsWith("'")) {
                try {
                    return Utils.parseAsciiInt(raw);
                } catch (NumberFormatException e) {
                    i.addError(e.getMessage());
                    return 0;
                }
            }
        } catch (NumberFormatException e) {
            // fall through...
        }
        i.addError("Invalid number: " + raw);
        return 0;
    }

}
