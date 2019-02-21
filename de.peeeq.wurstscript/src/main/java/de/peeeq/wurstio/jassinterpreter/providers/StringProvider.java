package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import de.peeeq.wurstscript.intermediatelang.interpreter.ILInterpreter;
import net.moonlightflower.wc3libs.misc.StringHash;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringProvider extends Provider {

    public StringProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstString I2S(ILconstInt i) {
        return new ILconstString("" + i.getVal());
    }

    public ILconstInt S2I(ILconstString s) {
        String str = s.getVal();
        Pattern pattern = Pattern.compile("([+\\-]?[0-9]+).*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            str = matcher.group(1);
            return new ILconstInt(Integer.parseInt(str));
        } else {
            return new ILconstInt(0);
        }
    }

    public ILconstReal S2R(ILconstString s) {
        String str = s.getVal();
        Pattern pattern = Pattern.compile("([+\\-]?[0-9]+(\\.[0-9]*)?).*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            str = matcher.group(1);
            return new ILconstReal(Float.parseFloat(str));
        } else {
            return new ILconstReal(0);
        }
    }

    public ILconstString R2S(ILconstReal r) {
        return new ILconstString("" + r.getVal());
    }

    public ILconstString R2SW(ILconstReal r, ILconstInt width, ILconstInt precision) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(precision.getVal());
        formatter.setMinimumFractionDigits(precision.getVal());
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setGroupingUsed(false);
        String s = formatter.format(r.getVal());
        // pad to desired width
        s = StringUtils.rightPad(s, width.getVal());
        return new ILconstString(s);
    }

    public ILconstInt R2I(ILconstReal i) {
        return new ILconstInt((int) i.getVal());
    }

    public ILconstReal I2R(ILconstInt i) {
        return new ILconstReal(i.getVal());
    }


    public ILconstInt StringHash(ILconstString s) {
        if (s == null) {
            return new ILconstInt(0);
        }
        try {
            return new ILconstInt(StringHash.hash(s.getVal()));
        } catch (UnsupportedEncodingException e) {
            WLogger.severe(e);
        }
        return new ILconstInt(0);
    }

    public ILconstInt StringLength(ILconstString string) {
        return new ILconstInt(string.getVal().length());
    }

    public ILconstString SubString(ILconstString istr, ILconstInt start, ILconstInt end) {
        String str = istr.getVal();
        int s = start.getVal();
        if (s < 0) {
            // I am not gonna emulate the WC3 bug for negative indexes here ...
            throw new InterpreterException("SubString called with negative start index: " + start);
        }
        int e = end.getVal();
        if (e >= str.length()) {
            // Warcraft does no bound checking here:
            e = str.length();
        }
        if (s > str.length()) {
            // if start is above string length, wc3 will return null
            // since this is most likely a bug in your code, the interpreter will throw an exception instead:
            throw new InterpreterException("SubString called with start index " + start + " greater than string length " + str.length());
        }
        return new ILconstString(str.substring(s, e));
    }

    public ILconstString StringCase(ILconstString string, ILconstBool upperCase) {
        return new ILconstString(
                upperCase.getVal() ?
                        string.getVal().toUpperCase()
                        : string.getVal().toLowerCase());
    }
}
