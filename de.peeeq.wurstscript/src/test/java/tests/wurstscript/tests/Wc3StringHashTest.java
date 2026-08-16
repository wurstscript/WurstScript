package tests.wurstscript.tests;

import de.peeeq.wurstscript.intermediatelang.ILconstString;
import de.peeeq.wurstscript.intermediatelang.Wc3StringHash;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * Checks the byte hash against the library's, which is the same function reached through text. They
 * are only comparable where the library is defined - a string of whole characters - so that is what
 * is compared, and it is enough: the arithmetic is the same for every input, only the bytes differ.
 */
public class Wc3StringHashTest extends WurstScriptTest {

    private static void agrees(String text) throws UnsupportedEncodingException {
        assertEquals(Wc3StringHash.hash(ILconstString.fromText(text).getVal()),
            net.moonlightflower.wc3libs.misc.StringHash.hash(text),
            "hash of " + text);
    }

    @Test
    public void agreesOnShortStrings() throws UnsupportedEncodingException {
        for (String s : new String[]{"", "a", "ab", "abc", "abcd", "abcde", "abcdef", "abcdefg",
            "abcdefgh", "abcdefghi", "abcdefghij", "abcdefghijk", "abcdefghijkl", "abcdefghijklm"}) {
            agrees(s);
        }
    }

    /** Case folding and the slash rule are part of the hash, not of the caller. */
    @Test
    public void agreesOnStringsNeedingNormalisation() throws UnsupportedEncodingException {
        for (String s : new String[]{"ABC", "AbC", "path/to/file", "path\\to\\file",
            "Units\\Human\\Footman.mdx", "MIXED/Case\\Path"}) {
            agrees(s);
        }
    }

    /** Multibyte text still agrees, because whole characters decode back to themselves. */
    @Test
    public void agreesOnMultibyteText() throws UnsupportedEncodingException {
        for (String s : new String[]{"ä", "äöü", "ЀЁЂЃ", "日本語", "😀", "aäböcü1234567890"}) {
            agrees(s);
        }
    }

    @Test
    public void agreesOnRandomAsciiOfEveryLength() throws UnsupportedEncodingException {
        Random random = new Random(20260816);
        for (int length = 0; length < 40; length++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append((char) (32 + random.nextInt(95)));
            }
            agrees(sb.toString());
        }
    }

    /**
     * The reason for hashing bytes rather than text. Each half of a two byte character has to keep a
     * hash of its own; decoding first would turn both into the same replacement character.
     */
    @Test
    public void halvesOfACharacterHashApart() {
        String bytes = ILconstString.fromText("ä").getVal();
        String lead = bytes.substring(0, 1);
        String continuation = bytes.substring(1, 2);
        assertNotEquals(Wc3StringHash.hash(lead), Wc3StringHash.hash(continuation),
            "the two bytes of a character must not hash alike");
        assertNotEquals(Wc3StringHash.hash(lead), Wc3StringHash.hash(bytes),
            "half a character must not hash like the whole");
    }

    /**
     * The standard library enumerates every continuation byte by slicing one literal and keeps them
     * apart by hash, so all 64 have to be distinct.
     */
    @Test
    public void everyContinuationByteHashesApart() {
        java.util.Set<Integer> hashes = new java.util.HashSet<>();
        for (int b = 0x80; b <= 0xBF; b++) {
            hashes.add(Wc3StringHash.hash(String.valueOf((char) b)));
        }
        assertEquals(hashes.size(), 64, "all 64 continuation bytes should hash apart");
    }

    /**
     * The Lua test runtime carries a second implementation of this hash, because a program running
     * there computes it too. Two transcriptions of one algorithm drift, so this runs the inputs
     * through both and compares — asserting the Java side against written-down numbers would stay
     * green if only the Lua side changed, which is the case worth catching.
     */
    @Test
    public void agreesWithTheLuaRuntimeImplementation() throws Exception {
        String[] inputs = {"abc", "Hello World", "Units\\Human\\Footman.mdx", "ä",
            "abcdefghijklmnop", "", "MIXED/Case\\Path", "日本語"};

        for (String input : inputs) {
            assertEquals(luaHashOf(input), Wc3StringHash.hash(ILconstString.fromText(input).getVal()),
                "hash of " + input);
        }
    }

    /** Runs the shim's StringHash on one input, as a program on that target would. */
    private int luaHashOf(String text) throws Exception {
        // Escaped byte by byte, so what the shim hashes is what the Java side was handed rather
        // than whatever the command line did to it.
        StringBuilder literal = new StringBuilder();
        for (byte b : text.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            literal.append("\\").append(b & 0xFF);
        }
        String script = "dofile('src/test/resources/luaruntime/wc3shim.lua') "
            + "print(StringHash('" + literal + "'))";
        Process p = new ProcessBuilder(getLuaExecutable(), "-e", script)
            .redirectErrorStream(true)
            .start();
        String out;
        try (java.io.BufferedReader r = new java.io.BufferedReader(
            new java.io.InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            out = r.lines().collect(java.util.stream.Collectors.joining("\n")).trim();
        }
        p.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
        try {
            return Integer.parseInt(out);
        } catch (NumberFormatException e) {
            throw new AssertionError("lua did not return a hash for \"" + text + "\": " + out);
        }
    }
}
