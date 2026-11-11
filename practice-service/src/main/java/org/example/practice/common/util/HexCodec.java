package org.example.practice.common.util;


import java.util.concurrent.ThreadLocalRandom;

public final class HexCodec {
    static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getRandomId() {
        long randomNum = getRandomNum();
        String ret = convertNumToId(randomNum);
        return ret;
    }

    public static long getRandomNum() {
        long ret = ThreadLocalRandom.current().nextLong();
        return ret;
    }

    public static long convertIdToNum(String input) {
        long ret = lowerHexToUnsignedLong(input);
        return ret;
    }

    public static String convertNumToId(long num) {
        String ret = toLowerHex(num);
        return ret;
    }

    public static long lowerHexToUnsignedLong(String lowerHex) {
        int length = lowerHex.length();
        if (length >= 1 && length <= 32) {
            int beginIndex = length > 16 ? length - 16 : 0;
            return lowerHexToUnsignedLong(lowerHex, beginIndex);
        } else {
            throw isntLowerHexLong(lowerHex);
        }
    }

    public static long lowerHexToUnsignedLong(String lowerHex, int index) {
        long result = 0L;

        for(int endIndex = Math.min(index + 16, lowerHex.length()); index < endIndex; ++index) {
            char c = lowerHex.charAt(index);
            result <<= 4;
            if (c >= '0' && c <= '9') {
                result |= (long)(c - 48);
            } else {
                if (c < 'a' || c > 'f') {
                    throw isntLowerHexLong(lowerHex);
                }

                result |= (long)(c - 97 + 10);
            }
        }

        return result;
    }

    static NumberFormatException isntLowerHexLong(String lowerHex) {
        throw new NumberFormatException(lowerHex + " should be a 1 to 32 character lower-hex string with no prefix");
    }

    public static String toLowerHex(long high, long low) {
        char[] result = new char[high != 0L ? 32 : 16];
        int pos = 0;
        if (high != 0L) {
            writeHexLong(result, pos, high);
            pos += 16;
        }

        writeHexLong(result, pos, low);
        return new String(result);
    }

    public static String toLowerHex(long v) {
        char[] data = new char[16];
        writeHexLong(data, 0, v);
        return new String(data);
    }

    public static void writeHexLong(char[] data, int pos, long v) {
        writeHexByte(data, pos, (byte)((int)(v >>> 56 & 255L)));
        writeHexByte(data, pos + 2, (byte)((int)(v >>> 48 & 255L)));
        writeHexByte(data, pos + 4, (byte)((int)(v >>> 40 & 255L)));
        writeHexByte(data, pos + 6, (byte)((int)(v >>> 32 & 255L)));
        writeHexByte(data, pos + 8, (byte)((int)(v >>> 24 & 255L)));
        writeHexByte(data, pos + 10, (byte)((int)(v >>> 16 & 255L)));
        writeHexByte(data, pos + 12, (byte)((int)(v >>> 8 & 255L)));
        writeHexByte(data, pos + 14, (byte)((int)(v & 255L)));
    }

    static void writeHexByte(char[] data, int pos, byte b) {
        data[pos] = HEX_DIGITS[b >> 4 & 15];
        data[pos + 1] = HEX_DIGITS[b & 15];
    }

    HexCodec() {
    }
}
