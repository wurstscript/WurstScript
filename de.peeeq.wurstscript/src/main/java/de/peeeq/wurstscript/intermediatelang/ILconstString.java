package de.peeeq.wurstscript.intermediatelang;

import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeString;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A string as Warcraft III has it: a sequence of bytes. {@code StringLength} counts them and
 * {@code SubString} indexes them, so a slice may stop between the bytes of one character. Lua agrees,
 * its strings being byte arrays too.
 * <p>
 * The value is held one char per byte, so every character in it is below 256 and Java's own length
 * and substring already give the game's answers. Text arriving from source or from the host has to be
 * encoded on the way in ({@link #fromText}), and anything leaving the interpreter for a file or a
 * screen decoded on the way out ({@link #text()}). A half character has no text to decode to, which is
 * the point: it keeps its byte until the other half is added back.
 */
public class ILconstString extends ILconstAbstract implements ILconstAddable {

    /** One char per byte, so all chars are < 256. */
    private final String val;

    private ILconstString(String byteView) {
        this.val = byteView;
    }

    /** Encodes text, which Wurst source and the host both give as UTF-8. */
    public static ILconstString fromText(String text) {
        return new ILconstString(encode(text));
    }

    /** Wraps bytes which are already one per char, as produced by slicing or joining. */
    public static ILconstString ofBytes(String byteView) {
        return new ILconstString(byteView);
    }

    static String encode(String text) {
        return new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
    }

    static String decode(String byteView) {
        return new String(byteView.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    /** The bytes, one per char. This is what the string natives count and index. */
    public String getVal() {
        return val;
    }

    /** The text those bytes spell, for anything which leaves the interpreter. */
    public String text() {
        return decode(val);
    }

    @Override
    public String print() {
        return "\"" + text() + "\"";
    }

    public WurstType getType() {
        return WurstTypeString.instance();
    }

    @Override
    public ILconstAddable add(ILconstAddable other) {
        if (other instanceof ILconstNull) {
            return this;
        }
        return new ILconstString(val + ((ILconstString) other).val);
    }

    @Override
    public boolean isEqualTo(ILconst other) {
        if (other instanceof ILconstString) {
            return ((ILconstString) other).val.equals(val);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }
}
