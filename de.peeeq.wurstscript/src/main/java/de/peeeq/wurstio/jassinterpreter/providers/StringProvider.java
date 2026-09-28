package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.InterpreterException;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstNull;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.Wc3StringHash;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringProvider extends Provider {

    public StringProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public ILconstString I2S(ILconstInt i) {
        return ILconstString.fromText("" + i.getVal());
    }

    /** Measured on the 3.0.0 client, as C's strtol: leading
     *  whitespace is skipped, then a sign and digits are read, and the value saturates at the integer
     *  limits ("2147483648" is 2147483647). */
    public ILconstInt S2I(@Nullable ILconstString s) {
        if (s == null) {
            return ILconstInt.create(0);
        }
        String str = s.getVal();
        int i = 0;
        while (i < str.length() && " \t\n\u000B\f\r".indexOf(str.charAt(i)) >= 0) {
            i++;
        }
        boolean negative = false;
        if (i < str.length() && (str.charAt(i) == '+' || str.charAt(i) == '-')) {
            negative = str.charAt(i) == '-';
            i++;
        }
        long value = 0;
        while (i < str.length() && str.charAt(i) >= '0' && str.charAt(i) <= '9') {
            // Stop growing once past the limit, so a long run of digits cannot overflow the long.
            value = Math.min(value * 10 + (str.charAt(i) - '0'), (long) Integer.MAX_VALUE + 1);
            i++;
        }
        long signed = negative ? -value : value;
        return ILconstInt.create((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, signed)));
    }

    private static final Pattern s2rpattern = Pattern.compile("[+\\-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)");
    /** Measured on the 3.0.0 client: unlike S2I, leading whitespace is not skipped, ".5" reads 0.5, the
     *  number stops at an exponent or a comma, and digits too large for a real give 0. */
    public ILconstReal S2R(@Nullable ILconstString s) {
        if (s == null) {
            return new ILconstReal(0);
        }
        Matcher matcher = s2rpattern.matcher(s.getVal());
        if (!matcher.lookingAt()) {
            return new ILconstReal(0);
        }
        float value = Float.parseFloat(matcher.group());
        return new ILconstReal(Float.isInfinite(value) ? 0 : value);
    }

    /** Measured on the 3.0.0 client: three decimals, rounded half up. The integer part wraps to 32 bits
     *  (3e9 prints -1294967296.000), and a negative value which rounds to zero keeps its sign. */
    public ILconstString R2S(ILconstReal r) {
        float x = r.getVal();
        long[] parts = roundedParts(Math.abs(x), 3);
        int whole = (int) (x < 0 ? -parts[0] : parts[0]);
        String sign = x < 0 && whole == 0 ? "-" : "";
        return ILconstString.fromText(sign + whole + "." + StringUtils.leftPad(Long.toString(parts[1]), 3, '0'));
    }

    /** Measured on the 3.0.0 client: the sign comes first, then the integer part left-padded to
     *  {@code width - precision}, then the fraction; precision 0 prints ".0". R2SW(1.5, 8, 2) is
     *  "     1.50" and R2SW(-1.5, 8, 3) is "-    1.500". */
    public ILconstString R2SW(ILconstReal r, ILconstInt width, ILconstInt precision) {
        float x = r.getVal();
        int digits = Math.max(0, precision.getVal());
        long[] parts = roundedParts(Math.abs(x), digits);
        String whole = StringUtils.leftPad(Integer.toString((int) parts[0]), width.getVal() - digits);
        String fraction = digits == 0 ? "0" : StringUtils.leftPad(Long.toString(parts[1]), digits, '0');
        return ILconstString.fromText((x < 0 ? "-" : "") + whole + "." + fraction);
    }

    /**
     * The whole part and the fraction digits of a non-negative value, rounded half up at the given
     * precision and carried into the whole part. The fraction is scaled in float arithmetic, as the
     * engine does, which is what shows at nine digits: its R2SW(r, 1, 9) is not the exact decimal.
     */
    private static long[] roundedParts(float value, int precision) {
        long whole = (long) value;
        float fraction = value - whole;
        long limit = (long) Math.pow(10, precision);
        long digits = (long) (fraction * (float) limit + 0.5f);
        if (digits >= limit) {
            whole++;
            digits -= limit;
        }
        return new long[] {whole, digits};
    }

    /** Measured on the 3.0.0 client: truncates, and wraps to 32 bits rather than saturating
     *  (1e10 is 1410065408). */
    public ILconstInt R2I(ILconstReal i) {
        return new ILconstInt((int) (long) i.getVal());
    }

    public ILconstReal I2R(ILconstInt i) {
        return new ILconstReal(i.getVal());
    }


    public ILconstInt StringHash(ILconstString s) {
        if (s == null) {
            return new ILconstInt(0);
        }
        return new ILconstInt(Wc3StringHash.hash(s.getVal()));
    }

    public ILconstInt StringLength(@Nullable ILconstString string) {
        return new ILconstInt(string == null ? 0 : string.getVal().length());
    }

    public ILconst SubString(@Nullable ILconstString istr, ILconstInt start, ILconstInt end) {
        if (istr == null) {
            return ILconstNull.instance();
        }
        String str = istr.getVal();
        int s = start.getVal();
        if (s < 0) {
            // I am not gonna emulate the WC3 bug for negative indexes here ...
            throw new InterpreterException("SubString called with negative start index: " + start);
        }
        if (s > str.length()) {
            // if start is above string length, wc3 will return null
            // since this is most likely a bug in your code, the interpreter will throw an exception instead:
            throw new InterpreterException("SubString called with start index " + start + " greater than string length " + str.length());
        }
        if (str.isEmpty()) {
            // Measured on the 3.0.0 client: every substring of the empty string is null.
            return ILconstNull.instance();
        }
        int e = end.getVal();
        if (e >= str.length() || e < s) {
            // Warcraft does no bound checking here, and an end before the start reads to the end
            // (measured on the 3.0.0 client: SubString("abcdef", 3, 1) is "def").
            e = str.length();
        }
        return ILconstString.ofBytes(str.substring(s, e));
    }

    /**
     * Only ascii letters change case. A string is a sequence of bytes, and the bytes of a multibyte
     * character are not letters to case at all - folding them the way a latin-1 char would fold
     * rewrites the character into a different one.
     */
    public ILconst StringCase(@Nullable ILconstString string, ILconstBool upperCase) {
        if (string == null) {
            return ILconstNull.instance();
        }
        String bytes = string.getVal();
        StringBuilder result = new StringBuilder(bytes.length());
        for (int i = 0; i < bytes.length(); i++) {
            char c = bytes.charAt(i);
            if (upperCase.getVal() && c >= 'a' && c <= 'z') {
                c -= 32;
            } else if (!upperCase.getVal() && c >= 'A' && c <= 'Z') {
                c += 32;
            }
            result.append(c);
        }
        return ILconstString.ofBytes(result.toString());
    }
}
