package org.barrelmc.barrel.utils;

public class Utils {

    public static byte[] toByteArray(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (int) (value & 0xFFL);
            value >>= 8L;
        }

        return result;
    }

    public static String lengthCutter(String bedrockName, int leng) {
        if (bedrockName == null) {
            return "null";
        }

        if (bedrockName.length() > leng) {
            return bedrockName.substring(0, leng);
        } else {
            return bedrockName;
        }
    }
}
