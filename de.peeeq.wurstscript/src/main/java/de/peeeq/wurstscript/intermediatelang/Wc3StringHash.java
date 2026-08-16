package de.peeeq.wurstscript.intermediatelang;

/**
 * {@code StringHash} over bytes.
 * <p>
 * The library's version takes text and encodes it as UTF-8 itself, which cannot express a string
 * holding half of a character - and half characters are exactly what the standard library hashes to
 * find out how the engine represents them. Decoding first would collapse every partial slice onto one
 * value, so the continuation bytes the standard library tells apart by hash would stop being
 * distinguishable.
 * <p>
 * The function is Bob Jenkins' lookup2, the same one the library implements; only the input differs.
 * {@code Wc3StringHashTest} checks the two agree on text, where both are defined.
 */
public final class Wc3StringHash {

    private Wc3StringHash() {
    }

    /**
     * @param bytes one byte per char, all below 256
     */
    public static int hash(String bytes) {
        if (bytes.isEmpty()) {
            return 0;
        }
        byte[] normalized = new byte[bytes.length()];
        for (int i = 0; i < bytes.length(); i++) {
            byte b = (byte) bytes.charAt(i);
            // Case insensitive, and either slash names the same file. Both comparisons are on signed
            // bytes, so anything above 127 - every byte of a multibyte character - is left alone.
            if (b >= 'a' && b <= 'z') {
                b -= 32;
            } else if (b == '/') {
                b = '\\';
            }
            normalized[i] = b;
        }
        return lookup2(normalized);
    }

    private static int lookup2(byte[] k) {
        int a = 0x9e3779b9;
        int b = 0x9e3779b9;
        int c = 0;
        int len = k.length;
        int i = 0;
        while (len >= 12) {
            a += u(k[i]) + (u(k[i + 1]) << 8) + (u(k[i + 2]) << 16) + (u(k[i + 3]) << 24);
            b += u(k[i + 4]) + (u(k[i + 5]) << 8) + (u(k[i + 6]) << 16) + (u(k[i + 7]) << 24);
            c += u(k[i + 8]) + (u(k[i + 9]) << 8) + (u(k[i + 10]) << 16) + (u(k[i + 11]) << 24);
            // mix
            a -= b; a -= c; a ^= (c >>> 13);
            b -= c; b -= a; b ^= (a << 8);
            c -= a; c -= b; c ^= (b >>> 13);
            a -= b; a -= c; a ^= (c >>> 12);
            b -= c; b -= a; b ^= (a << 16);
            c -= a; c -= b; c ^= (b >>> 5);
            a -= b; a -= c; a ^= (c >>> 3);
            b -= c; b -= a; b ^= (a << 10);
            c -= a; c -= b; c ^= (b >>> 15);
            i += 12;
            len -= 12;
        }
        c += k.length;
        // The low byte of c holds the length, so the tail starts at the second.
        switch (len) {
            case 11: c += u(k[i + 10]) << 24;
            case 10: c += u(k[i + 9]) << 16;
            case 9: c += u(k[i + 8]) << 8;
            case 8: b += u(k[i + 7]) << 24;
            case 7: b += u(k[i + 6]) << 16;
            case 6: b += u(k[i + 5]) << 8;
            case 5: b += u(k[i + 4]);
            case 4: a += u(k[i + 3]) << 24;
            case 3: a += u(k[i + 2]) << 16;
            case 2: a += u(k[i + 1]) << 8;
            case 1: a += u(k[i]);
            default:
        }
        a -= b; a -= c; a ^= (c >>> 13);
        b -= c; b -= a; b ^= (a << 8);
        c -= a; c -= b; c ^= (b >>> 13);
        a -= b; a -= c; a ^= (c >>> 12);
        b -= c; b -= a; b ^= (a << 16);
        c -= a; c -= b; c ^= (b >>> 5);
        a -= b; a -= c; a ^= (c >>> 3);
        b -= c; b -= a; b ^= (a << 10);
        c -= a; c -= b; c ^= (b >>> 15);
        return c;
    }

    private static int u(byte b) {
        return b & 0xFF;
    }
}
