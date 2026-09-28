package de.peeeq.wurstio.jassinterpreter.providers;

/**
 * The pseudorandom number generator used by the JASS random natives.
 *
 * <p>This is the generator used by game.dll 1.26.0.6401. The real-number
 * operations intentionally work on float bit patterns: Warcraft's native
 * does not have the same rounding and subnormal behavior as Java float
 * arithmetic.</p>
 */
final class JassPrng {

    private static final byte[] CONSTANTS = decodeHex(
            "8e142799fdaac708d5e63e1ff6"
                    + "bb55da75a04a6ae8bd97ffde9bbc9f81"
                    + "8aa1466e0be363767a6c5d88d369cac3"
                    + "47b92583aba23fa6417cbae5ac95017e"
                    + "cf09c1d96270718ddb05022487ef54c6"
                    + "d43730d01bcb7bb8e4d8ec49ceaddc13"
                    + "a994c48f39ae0d1852dd0e78faf58558"
                    + "d2af6da4b2533b51a550befc2df41148"
                    + "9816f186df3d665e442e2f36076b178b"
                    + "294cb6e2895fe7cda721e14dc965edfe"
                    + "ee9c23337db7049e9a2a40b3105bf382"
                    + "771c92204e1e572272068c672c73fb59"
                    + "c20abf795cf90c281a126874341942b1"
                    + "c084f838f0159d60f23a6fb490eb911d"
                    + "7f35615a320356a3c52b93800f4b43f7"
                    + "a8e03c96d16426d745cc4fc8b0e9b500"
                    + "d631ea68756e7465722067726567616c");

    private int seedBits;
    private int current;

    void setRandomSeed(int seed) {
        setSeed(seed);
        step();
    }

    int getRandomInt(int min, int max) {
        if (min == max) {
            return min;
        }

        long range = max >= min ? (long) max - min : (long) min - max;
        long random = Integer.toUnsignedLong(step());
        long t = (random * (range + 1)) >>> 32;
        return (int) (min + t);
    }

    float getRandomReal(float min, float max) {
        int minBits = Float.floatToRawIntBits(min);
        int maxBits = Float.floatToRawIntBits(max);
        int widthBits = f32Sub(minBits, maxBits);

        if (Math.abs(Float.intBitsToFloat(widthBits)) < Float.intBitsToFloat(0x3456bf95)) {
            return Float.intBitsToFloat(minBits);
        }

        if (min <= max) {
            widthBits = f32Sub(maxBits, minBits);
        } else {
            widthBits = f32Sub(minBits, maxBits);
        }

        int random = step();
        int oneToTwoBits = (random & 0x007fffff) | 0x3f800000;
        int randomUnitBits = f32Add(oneToTwoBits, s32ToF32(-1));
        int scaledBits = f32Mult(widthBits, randomUnitBits);
        return Float.intBitsToFloat(f32Add(minBits, scaledBits));
    }

    private void setSeed(int seed) {
        seedBits = Math.floorMod(seed, 0x3d) << 2;
        seedBits |= Math.floorMod(seed, 0x3b) << 10;
        seedBits |= Math.floorMod(seed, 0x35) << 18;
        seedBits |= (Math.floorDiv(seed, 0x2f) * 0x11 + seed) << 26;
        current = seed;
    }

    private int step() {
        int b3 = (seedBits >>> 24) & 0xff;
        int b2 = (seedBits >>> 16) & 0xff;
        int b1 = (seedBits >>> 8) & 0xff;
        int b0 = seedBits & 0xff;

        int i0 = b3 - 4;
        if (i0 < 0) {
            i0 = b3 + 0xb8;
        }
        int i1 = b2 - 0x0c;
        if (i1 < 0) {
            i1 = b2 + 200;
        }
        int i2 = b1 - 0x18;
        if (i2 < 0) {
            i2 = b1 + 0xd4;
        }
        int i3 = b0 - 0x1c;
        if (i3 < 0) {
            i3 = b0 + 0xd8;
        }

        int mix = Integer.rotateLeft(constantAt(i2), 3)
                ^ Integer.rotateLeft(constantAt(i1), 2)
                ^ constantAt(i3)
                ^ Integer.rotateLeft(constantAt(i0), 1);
        int newValue = current + mix;

        seedBits = ((i0 & 0xff) << 24)
                | ((i1 & 0xff) << 16)
                | ((i2 & 0xff) << 8)
                | (i3 & 0xff);
        current = newValue;
        return newValue;
    }

    private static int constantAt(int index) {
        return (CONSTANTS[index] & 0xff)
                | ((CONSTANTS[index + 1] & 0xff) << 8)
                | ((CONSTANTS[index + 2] & 0xff) << 16)
                | ((CONSTANTS[index + 3] & 0xff) << 24);
    }

    private static int f32Add(int a, int b) {
        int expA = a & 0x7f800000;
        if (expA == 0) {
            return b;
        }
        int expB = b & 0x7f800000;
        if (expB == 0) {
            return a;
        }

        int sigA = signedSignificandTimesTwo(a);
        int sigB = signedSignificandTimesTwo(b);
        int diff = expB - expA;
        int expBig;
        if (diff < 1) {
            if (diff < -0x0b7fffff) {
                return a;
            }
            sigB >>= ((expA - expB) >>> 23) & 0x1f;
            expBig = expA;
        } else {
            if (diff > 0x0b7fffff) {
                return b;
            }
            sigA >>= (diff >>> 23) & 0x1f;
            expBig = expB;
        }

        int sum = sigA + sigB;
        if (sum == 0) {
            return 0;
        }

        int sign = sum < 0 ? 0x80000000 : 0;
        int magnitude = sum < 0 ? -sum : sum;
        int leadingZeros = Integer.numberOfLeadingZeros(magnitude);
        int shift = -leadingZeros + 8;
        if (shift < 0) {
            magnitude <<= -shift;
        } else {
            magnitude >>= shift;
        }

        return ((-leadingZeros + 7) * 0x800000 + expBig)
                | (magnitude & 0x7fffff)
                | sign;
    }

    private static int signedSignificandTimesTwo(int bits) {
        int significand = ((bits & 0x007fffff) | 0x00800000) * 2;
        int sign = bits >> 31;
        return (significand ^ sign) - sign;
    }

    private static int f32Sub(int a, int b) {
        return f32Add(a, b ^ 0x80000000);
    }

    private static int f32Mult(int a, int b) {
        int sign = (a ^ b) & 0x80000000;
        int expA = a & 0x7f800000;
        int expB = b & 0x7f800000;
        if (expA == 0 || expB == 0) {
            return 0;
        }

        long significandA = (1L << 23) | (a & 0x007fffff);
        long significandB = (1L << 23) | (b & 0x007fffff);
        long product = significandA * significandB;
        int topBit = (int) (product >>> 47) & 1;
        int mantissa = (int) (product >>> (23 + topBit)) & 0x007fffff;

        int exponent = expA + expB + 0xc0800000;
        if (topBit != 0) {
            exponent += 0x00800000;
        }
        if (Integer.compareUnsigned(exponent, 0x00800000) < 0) {
            return 0;
        }
        return sign | (exponent & 0x7f800000) | mantissa;
    }

    private static int s32ToF32(int value) {
        if (value == 0) {
            return 0;
        }
        int sign = value & 0x80000000;
        if (value < 0) {
            value = -value;
        }
        int leadingZeros = Integer.numberOfLeadingZeros(value);
        int shift = leadingZeros - 8;
        int shifted = shift < 0 ? value >> -shift : value << shift;
        return ((0x9e - leadingZeros) * 0x800000)
                | (shifted & 0x7fffff)
                | sign;
    }

    private static byte[] decodeHex(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }
}
