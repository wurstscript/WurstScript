package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringProvider extends Provider {

    public StringProvider(ILInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstString I2S(ILconstInt i) {
        return new ILconstString("" + i.getVal());
    }

    public ILconstInt S2I(ILconstString s) {
        String str = s.getVal();
        Pattern pattern = Pattern.compile("((\\+|-)?[0-9]+).*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            str = matcher.group(1);
            return new ILconstInt(Integer.parseInt(str));
        } else {
            return new ILconstInt(-1);
        }
    }

    public ILconstString R2S(ILconstReal r) {
        return new ILconstString("" + r.getVal());
    }

    public ILconstInt R2I(ILconstReal i) {
        return new ILconstInt((int) i.getVal());
    }

    public ILconstReal I2R(ILconstInt i) {
        return new ILconstReal(i.getVal());
    }

    public ILconstInt StringHash(ILconstString s) {
        // TODO use wc3's hash func
        WLogger.info("stringhash of <" + s.getVal() + "> = " + s.getVal().hashCode());
        return new ILconstInt(s.getVal().hashCode());
    }

    public ILconstInt StringLength(ILconstString string) {
        return new ILconstInt(string.getVal().length());
    }

    public ILconstString SubString(ILconstString s, ILconstInt start, ILconstInt end) {
        String str = s.getVal();
        if (start.getVal() < 0) {
            // I am not gonna emulate the WC3 bug for negative indexes here ...
            throw new RuntimeException("SubString called with negative start index: " + start) ;
        }
        int e = end.getVal();
        if (e >= str.length()) {
            // Warcraft does no bound checking here:
            e = str.length();
        }
        return new ILconstString(str.substring(start.getVal(), e));
    }

    public ILconstString StringCase(ILconstString string, ILconstBool upperCase) {
        return new ILconstString(
                upperCase.getVal() ?
                        string.getVal().toUpperCase()
                        : string.getVal().toLowerCase());
    }
}
