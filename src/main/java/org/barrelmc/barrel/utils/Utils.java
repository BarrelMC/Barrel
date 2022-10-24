package org.barrelmc.barrel.utils;

import com.github.steveice10.mc.protocol.data.game.chunk.BitStorage;
import com.github.steveice10.mc.protocol.data.game.chunk.DataPalette;
import com.github.steveice10.mc.protocol.data.game.chunk.palette.SingletonPalette;

public class Utils {

    public static byte[] toByteArray(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (int) (value & 0xFFL);
            value >>= 8L;
        }

        return result;
    }

    public static String lengthCutter(String bedrockName, int length) {
        if (bedrockName == null) {
            return "null";
        }

        if (bedrockName.length() > length) {
            return bedrockName.substring(0, length);
        } else {
            return bedrockName;
        }
    }

    public static void fillPalette(DataPalette dataPalette) {
        fillPalette(dataPalette, 0);
    }

    public static void fillPalette(DataPalette dataPalette, int state) {
        BitStorage bitStorage = dataPalette.getStorage();
        dataPalette.setPalette(new SingletonPalette(0));
        for (int i = 0; i < bitStorage.getSize(); i++) {
            bitStorage.set(i, state);
        }
    }
}
